/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.processor.Instruction;

public class AcceleratorSchedulingUnit {


	private List<AcceleratorInstruction> acceleratorInstructionList;
	private AcceleratorInstruction[] buffer;
	private Map<Integer,Integer> addressReorderingMap;
	private int registerWrittenPointCount[];
	private int registerReadPointCount[];

	// 演算結果格納レジスタからのMOV命令でのコピーを削る最適化を、適用してもよい命令の集合
	// (算術演算命令などは可能、ELEM命令などのメモリー関連命令では不可能)
	private static final HashSet<OperationCode> movReducableOpcodeSet = new HashSet<OperationCode>();
	static {
		movReducableOpcodeSet.add(OperationCode.ADD);
		movReducableOpcodeSet.add(OperationCode.SUB);
		movReducableOpcodeSet.add(OperationCode.MUL);
		movReducableOpcodeSet.add(OperationCode.DIV);
		movReducableOpcodeSet.add(OperationCode.REM);
		movReducableOpcodeSet.add(OperationCode.NEG);

		movReducableOpcodeSet.add(OperationCode.EQ);
		movReducableOpcodeSet.add(OperationCode.NEQ);
		movReducableOpcodeSet.add(OperationCode.GT);
		movReducableOpcodeSet.add(OperationCode.LT);
		movReducableOpcodeSet.add(OperationCode.GEQ);
		movReducableOpcodeSet.add(OperationCode.LEQ);

		movReducableOpcodeSet.add(OperationCode.ANDM);
		movReducableOpcodeSet.add(OperationCode.ORM);
		movReducableOpcodeSet.add(OperationCode.NOT);
		movReducableOpcodeSet.add(OperationCode.CAST);

		movReducableOpcodeSet.add(OperationCode.MOVPOP); // MOVPOPでスタックから取って直後にコピーするだけのは削っても安全（REFPOPは無理）
		movReducableOpcodeSet.add(OperationCode.MOVELM); // MOVELMは要素の単純コピーなのでその直後にMOVするのは削っても安全（REFELMは無理）
	}


	public AcceleratorInstruction[] schedule(
			Instruction[] instructions, Memory memory, AcceleratorDataManagementUnit dataManager) {

		// 命令列を読み込んで AcceleratorInstruction に変換し、フィールドのリストを初期化して格納
		this.initializeeAcceleratorInstructionList(instructions, memory);

		// CALL命令の直後（NOPが置かれている）の箇所に、RETURNED命令を生成して置き換える
		this.generateReturnedInstructions();

		// 全てのレジスタに対し、それぞれ書き込み・読み込み箇所の数をカウントする（最適化に使用）
		this.createRegisterWrittenPointCount(memory);
		this.createRegisterReadPointCount(memory);

		// 各命令の AcceleratorExecutionType を判定して設定
		this.detectAccelerationTypes(memory, dataManager);

		// スカラのALLOC命令をコード先頭に並べ替える（メモリコストが低いので、ループ内に混ざるよりも利点が多い）
		this.reorderAllocAndAllocrInstructions(dataManager);

		// オペランドを並び替え、不要になったMOV命令を削る
		this.reduceMovInstructions(dataManager);


		// 連続する算術スカラ演算命令2個を融合させて1個の拡張命令に置き換える
		this.fuseArithmeticInstructions( // Float64 Cached-Scalar Arithmetic
				AcceleratorExecutionType.F64CS_ARITHMETIC, AcceleratorExecutionType.F64CS_DUAL_ARITHMETIC
		);
		this.fuseArithmeticInstructions( // Int64 Cached-Scalar Arithmetic
				AcceleratorExecutionType.I64CS_ARITHMETIC, AcceleratorExecutionType.I64CS_DUAL_ARITHMETIC
		);

		// 連続する算術ベクトル演算命令2個を融合させて1個の拡張命令に置き換える
		// >> 実行環境バージョンやPCの状態等により性能が上下どちらにも大きく振れるので、少なくとも現時点では無効化
		/*
		this.fuseArithmeticInstructions( // Float64 Vector Arithmetic
				AcceleratorExecutionType.F64V_ARITHMETIC, AcceleratorExecutionType.F64V_DUAL_ARITHMETIC
		);
		this.fuseArithmeticInstructions( // Int64 Vector Arithmetic
				AcceleratorExecutionType.I64V_ARITHMETIC, AcceleratorExecutionType.I64V_DUAL_ARITHMETIC
		);
		*/


		// 再配列後の命令アドレスを設定
		this.updateReorderedAddresses();

		// 再配列前と再配列後の命令アドレスの対応を保持するマップを生成
		this.generateAddressReorderingMap();

		// ジャンプ命令の飛び先アドレスを補正したものを求めて設定
		this.resolveReorderedLabelAddress(memory);

		return this.acceleratorInstructionList.toArray(new AcceleratorInstruction[0]);
	}


	// フィールドの命令リストを生成して初期化
	private void initializeeAcceleratorInstructionList(Instruction[] instructions, Memory memory) {

		int instructionLength = instructions.length;
		this.acceleratorInstructionList = new ArrayList<AcceleratorInstruction>();
		this.buffer = new AcceleratorInstruction[instructionLength];

		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {

			// InstructionをAcceleratorInstruction（Instructionのサブクラス）に変換
			Instruction instruction = instructions[instructionIndex];
			AcceleratorInstruction acceleratorInstruction = new AcceleratorInstruction(instruction);

			// 再配置前の命令アドレスを書き込む
			acceleratorInstruction.setUnreorderedAddress(instructionIndex);

			// リストに追加
			this.acceleratorInstructionList.add(acceleratorInstruction);
		}
	}


	// CALL命令の直後（ラベルのためNOPが置かれている）の箇所に、RETURNED拡張命令を生成して置き換える
	private void generateReturnedInstructions() {
		int instructionLength = acceleratorInstructionList.size();

		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			Instruction instruction = acceleratorInstructionList.get(instructionIndex);
			if (instruction.getOperationCode() == OperationCode.CALL) {

				// オペランドのパーティションとアドレスを取り出す
				Memory.Partition[] operandPartitions = instruction.getOperandPartitions();
				int[] operandAddresses = instruction.getOperandAddresses();
				int functionArgN = operandAddresses.length - 2; // 先頭2個はプレースホルダと関数アドレス

				// 上記から、関数に渡す引数のもののみを抜き出す
				//（ただし命令の基本仕様により、先頭オペランドは書き込み先と決まっているので、プレースホルダを置く）
				Memory.Partition[] functionArgPartitions = new Memory.Partition[functionArgN+1];
				int[] functionArgAddresses = new int[functionArgN+1];
				functionArgPartitions[0] = Memory.Partition.NONE;
				functionArgAddresses[0] = 0;
				for (int argIndex=0; argIndex<functionArgN; argIndex++) {
					functionArgPartitions[argIndex+1] = operandPartitions[argIndex + 2]; // 右辺の先頭2個はプレースホルダと関数アドレス
					functionArgAddresses[argIndex+1] = operandAddresses[argIndex + 2];
				}

				// それらをオペランドとして、RETURNED拡張命令を生成
				AcceleratorInstruction returnedInstruction = new AcceleratorInstruction(
					new Instruction(
						OperationCode.EX, instruction.getDataTypes(),
						functionArgPartitions, functionArgAddresses,
						instruction.getMetaPartition(), instruction.getMetaAddress()
					)
				);
				returnedInstruction.setExtendedOperationCode(AcceleratorExtendedOperationCode.RETURNED);
				returnedInstruction.setUnreorderedAddress(instructionIndex);

				// この命令直後にあるはずのNOP命令を、生成したRETURNED拡張命令で置き換える
				acceleratorInstructionList.set(instructionIndex+1, returnedInstruction);
			}
		}
	}


	// データに対して書き込みを行う命令なら true を返す（ただしレジスタの確保・解放は除外）
	private boolean isDataWritingOperationCode(OperationCode operationCode) {

		// 書き込みを「行わない」ものを以下に列挙し、それ以外なら true を返す
		return operationCode != OperationCode.ALLOC
		     && operationCode != OperationCode.ALLOCT
		     && operationCode != OperationCode.ALLOCP
		     && operationCode != OperationCode.ALLOCR
		     && operationCode != OperationCode.FREE
		     && operationCode != OperationCode.NOP
		     && operationCode != OperationCode.JMP
		     && operationCode != OperationCode.JMPN
		     && operationCode != OperationCode.RET
		     && operationCode != OperationCode.POP      // POP はスタックからデータを取り出すだけで何もしない
		     && operationCode != OperationCode.NOP      // NOP は何もしないので明らかに何も読まない
		     && operationCode != OperationCode.END
		     && operationCode != OperationCode.ENDFUN
		     ;

		// CALL & RET 命令について：
		//   関数の引数や戻り値はスタックに積まれるので、CALL命令やRET命令自体はオペランドには何も書き込まないが、
		//   スタックに積まれた後のデータがどこで取り出されて読み書きされるかを静的に解析するのは複雑なので、
		//   CALL & RET 命令のオペランドは便宜的に、書き込み箇所カウントに含める（最適化予防のため）
	}


	// レジスタ書き込み箇所カウンタ配列を生成して初期化
	private void createRegisterWrittenPointCount(Memory memory) {
		int registerLength = memory.getSize(Memory.Partition.REGISTER);
		this.registerWrittenPointCount = new int[registerLength];
		Arrays.fill(this.registerWrittenPointCount, 0);

		int instructionLength = acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			AcceleratorInstruction instruction = acceleratorInstructionList.get(instructionIndex);

			// 値が変更されている可能性を調べるために使うので、非書き換え命令や確保・解放はカウントから除外
			if (!this.isDataWritingOperationCode(instruction.getOperationCode())) {
				continue;
			}

			// 値の書き込み用である第0オペランドがレジスタではない場合も、この時点で除外
			Memory.Partition writingPartition = instruction.getOperandPartitions()[0];
			if (writingPartition != Memory.Partition.REGISTER) {
				continue;
			}

			// ここまで残るのはレジスタに書き込んでいる場合なので、レジスタ書き込み箇所カウンタを加算
			int writingRegisterAddress = instruction.getOperandAddresses()[0];
			this.registerWrittenPointCount[writingRegisterAddress]++;
		}
	}



	// オペランドからデータの読み込みを行う命令なら true を返す
	//（ただしメモリの確保・解放やなどは除外）
	private boolean isDataReadingOperationCode(OperationCode operationCode) {

		// 読み込みを「行わない」ものを以下に列挙し、それ以外なら true を返す
		return operationCode != OperationCode.ALLOC
		     && operationCode != OperationCode.ALLOCT
		     && operationCode != OperationCode.ALLOCP
		     && operationCode != OperationCode.ALLOCR
		     && operationCode != OperationCode.FREE
		     && operationCode != OperationCode.POP      // POP はスタックからデータを取り出すだけで何もしない
		     && operationCode != OperationCode.MOVPOP   // MOVPOP はスタックからデータを読むので、オペランドからは読まない（書く）
		     && operationCode != OperationCode.REFPOP   // REFPOP はスタックからデータを読むので、オペランドからは読まない（書く）
		     && operationCode != OperationCode.NOP      // NOP は何もしないので明らかに何も読まない
		     && operationCode != OperationCode.ENDFUN
		     ;

		// スタックとデータをやり取りする CALL & RET 命令は要注意
		// ( isDataWritingOperationCode のコメント参照 )
	}


	// レジスタ読み込み箇所カウンタ配列を生成して初期化
	private void createRegisterReadPointCount(Memory memory) {
		int registerLength = memory.getSize(Memory.Partition.REGISTER);
		this.registerReadPointCount = new int[registerLength];
		Arrays.fill(this.registerReadPointCount, 0);

		int instructionLength = acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			AcceleratorInstruction instruction = acceleratorInstructionList.get(instructionIndex);
			OperationCode opcode = instruction.getOperationCode();

			// 値が変更されている可能性を調べるために使うので、非書き換え命令や確保・解放はカウントから除外
			if (!this.isDataReadingOperationCode(opcode)) {
				continue;
			}

			// オペランドの個数
			int operandLength = instruction.getOperandLength();

			// 入力オペランドが始まるインデックス ... 命令の仕様上、先頭は必ず書き込みオペランドで、入力はその次（1番）から始まる
			int inputOperandsBeginIndex = 1;

			// 入力オペランドは個数可変や省略可能な命令もあるため、オペランド数が2未満の場合＝入力オペランドが無い場合はスキップする
			if (operandLength < 2) {
				continue;
			}

			// オペランドのメモリパーティションとアドレスを取得
			Memory.Partition[] operandPartitions = instruction.getOperandPartitions();
			int[] operandAddresses = instruction.getOperandAddresses();

			// 2個目以降（インデックス1以上）のオペランドがレジスタなら、レジスタ読み込み箇所カウンタを加算
			for (int operandIndex=inputOperandsBeginIndex; operandIndex<operandLength; operandIndex++) {
				if (operandPartitions[operandIndex] == Memory.Partition.REGISTER) {
					int readingRegisterAddress = operandAddresses[operandIndex];
					this.registerReadPointCount[readingRegisterAddress]++;
				}
			}
		}
	}



	private boolean isAllCached(boolean[] operandCached) {
		for (boolean cached: operandCached) {
			if (!cached) {
				return false;
			}
		}
		return true;
	}

	private boolean isAllScalar(boolean[] operandScalar) {
		for (boolean scalar: operandScalar) {
			if (!scalar) {
				return false;
			}
		}
		return true;
	}

	private boolean isAllVector(boolean[] operandScalar) {
		for (boolean scalar: operandScalar) {
			if (scalar) {
				return false;
			}
		}
		return true;
	}


	private void detectAccelerationTypes(Memory memory, AcceleratorDataManagementUnit dataManager) {

		int instructionLength = this.acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) { // 後でイテレータ使うループにする

			AcceleratorInstruction instruction = acceleratorInstructionList.get(instructionIndex);
			DataType[] dataTypes = instruction.getDataTypes();
			OperationCode opcode = instruction.getOperationCode();


			// 命令からデータアドレスを取得
			Memory.Partition[] partitions = instruction.getOperandPartitions();
			int[] addresses = instruction.getOperandAddresses();
			int operandLength = instruction.getOperandLength();

			DataContainer<?>[] containers = new DataContainer[operandLength];
			for (int operandIndex=0; operandIndex<operandLength; operandIndex++) {
				containers[operandIndex] = memory.getDataContainer(partitions[operandIndex], addresses[operandIndex]);
			}


			// オペランドの状態とキャッシュ参照などを控える配列を用意
			boolean[] operandConstant = new boolean[operandLength];
			boolean[] operandScalar = new boolean[operandLength];
			boolean[] operandCachingEnabled = new boolean[operandLength];
			ScalarCache[] operandCaches = new ScalarCache[operandLength];


			// データマネージャから、オペランドのスカラ判定結果とキャッシュ有無およびキャッシュ参照を取得し、
			// 定数かどうかも控える
			for (int operandIndex=0; operandIndex<operandLength; operandIndex++) {
				operandScalar[operandIndex] = dataManager.isScalar(partitions[operandIndex], addresses[operandIndex]);
				operandCachingEnabled[operandIndex] = dataManager.isCachingEnabled(partitions[operandIndex], addresses[operandIndex]);
				if (operandCachingEnabled[operandIndex]) {
					operandCaches[operandIndex] = dataManager.getCache(partitions[operandIndex], addresses[operandIndex]);
				}
				if (partitions[operandIndex] == Memory.Partition.CONSTANT) {
					operandConstant[operandIndex] = true;
				}
			}


			switch (opcode) {

				// メモリ確保命令 Memory allocation opcodes
				case ALLOC :
				case ALLOCR :
				{
					instruction.setAccelerationType(AcceleratorExecutionType.BYPASS);
					break;
				}

				// 算術演算命令 Arithmetic instruction opcodes
				case ADD :
				case SUB :
				case MUL :
				case DIV :
				case REM :
				{
					if(dataTypes[0] == DataType.INT64) {

						// 全部ベクトルの場合の演算
						if (isAllVector(operandScalar)) {
							instruction.setAccelerationType(AcceleratorExecutionType.I64V_ARITHMETIC);
						// 全部キャッシュ可能なスカラの場合の演算
						} else if (isAllScalar(operandScalar) && isAllCached(operandCachingEnabled)) {
							instruction.setAccelerationType(AcceleratorExecutionType.I64CS_ARITHMETIC);
						// キャッシュ不可能なスカラ演算の場合（インデックス参照がある場合や、長さ1のベクトルとの混合演算など）
						} else {
							instruction.setAccelerationType(AcceleratorExecutionType.I64S_ARITHMETIC);
						// ベクトル・スカラ混合演算で、スカラをベクトルに昇格する場合は？
						// →それはスクリプト側の仕様で、中間コードレベルではサポートしていない
						}

					} else if (dataTypes[0] == DataType.FLOAT64) {

						if (isAllVector(operandScalar)) {
							instruction.setAccelerationType(AcceleratorExecutionType.F64V_ARITHMETIC);
						} else if (isAllScalar(operandScalar) && isAllCached(operandCachingEnabled)) {
							instruction.setAccelerationType(AcceleratorExecutionType.F64CS_ARITHMETIC);
						} else {
							instruction.setAccelerationType(AcceleratorExecutionType.F64S_ARITHMETIC);
						}

					} else {
						instruction.setAccelerationType(AcceleratorExecutionType.BYPASS);
					}
					break;
				}

				// 比較演算命令 Comparison instruction opcodes
				case LT :
				case GT :
				case LEQ :
				case GEQ :
				case EQ :
				case NEQ :
				{
					if(dataTypes[0] == DataType.INT64) {

						if (isAllVector(operandScalar)) {
							instruction.setAccelerationType(AcceleratorExecutionType.I64V_COMPARISON);
						} else if (isAllScalar(operandScalar) && isAllCached(operandCachingEnabled)) {
							instruction.setAccelerationType(AcceleratorExecutionType.I64CS_COMPARISON);
						} else {
							instruction.setAccelerationType(AcceleratorExecutionType.I64S_COMPARISON);
						}

					} else if (dataTypes[0] == DataType.FLOAT64) {

						if (isAllVector(operandScalar)) {
							instruction.setAccelerationType(AcceleratorExecutionType.F64V_COMPARISON);
						} else if (isAllScalar(operandScalar) && isAllCached(operandCachingEnabled)) {
							instruction.setAccelerationType(AcceleratorExecutionType.F64CS_COMPARISON);
						} else {
							instruction.setAccelerationType(AcceleratorExecutionType.F64S_COMPARISON);
						}

					} else {
						instruction.setAccelerationType(AcceleratorExecutionType.BYPASS);
					}
					break;
				}

				// 論理演算命令 Logical instruction opcodes
				case ANDM :
				case ORM :
				case NOT :
				{
					if(dataTypes[0] == DataType.BOOL) {

						if (isAllVector(operandScalar)) {
							instruction.setAccelerationType(AcceleratorExecutionType.BV_LOGICAL);
						} else if (isAllScalar(operandScalar) && isAllCached(operandCachingEnabled)) {
							instruction.setAccelerationType(AcceleratorExecutionType.BCS_LOGICAL);
						} else {
							instruction.setAccelerationType(AcceleratorExecutionType.BS_LOGICAL);
						}

					} else {
						instruction.setAccelerationType(AcceleratorExecutionType.BYPASS);
					}
					break;
				}

				// 転送命令 Trsndfer instruction opcodes
				case MOV :
				case FILL :
				{
					if(dataTypes[0] == DataType.INT64) {

						if (isAllVector(operandScalar)) {
							instruction.setAccelerationType(AcceleratorExecutionType.I64V_TRANSFER);
						} else if (isAllScalar(operandScalar) && isAllCached(operandCachingEnabled)) {
							instruction.setAccelerationType(AcceleratorExecutionType.I64CS_TRANSFER);
						} else if (!operandScalar[0] && operandScalar[1]) {
							instruction.setAccelerationType(AcceleratorExecutionType.I64VS_TRANSFER);
						} else {
							instruction.setAccelerationType(AcceleratorExecutionType.I64S_TRANSFER);
						}

					} else if (dataTypes[0] == DataType.FLOAT64) {

						if (isAllVector(operandScalar)) {
							instruction.setAccelerationType(AcceleratorExecutionType.F64V_TRANSFER);
						} else if (isAllScalar(operandScalar) && isAllCached(operandCachingEnabled)) {
							instruction.setAccelerationType(AcceleratorExecutionType.F64CS_TRANSFER);
						} else if (!operandScalar[0] && operandScalar[1]) {
							instruction.setAccelerationType(AcceleratorExecutionType.F64VS_TRANSFER);
						} else {
							instruction.setAccelerationType(AcceleratorExecutionType.F64S_TRANSFER);
						}

					} else if (dataTypes[0] == DataType.BOOL) {

						if (isAllVector(operandScalar)) {
							instruction.setAccelerationType(AcceleratorExecutionType.BV_TRANSFER);
						} else if (isAllScalar(operandScalar) && isAllCached(operandCachingEnabled)) {
							instruction.setAccelerationType(AcceleratorExecutionType.BCS_TRANSFER);
						} else if (!operandScalar[0] && operandScalar[1]) {
							instruction.setAccelerationType(AcceleratorExecutionType.BVS_TRANSFER);
						} else {
							instruction.setAccelerationType(AcceleratorExecutionType.BS_TRANSFER);
						}

					} else {
						instruction.setAccelerationType(AcceleratorExecutionType.BYPASS);
					}
					break;
				}

				// 配列要素アクセス命令 Array subscript opcodes
				case MOVELM :
				case REFELM : {

					int indicesLength = operandLength - 2; // インデックス数: オペランド[2]以降がインデックス部なので -2
					//boolean isDestCached = operandCachingEnabled[0]; // 結果の格納先がキャッシュ可能かどうか
					boolean isAllIndicesCached = true; // インデックスオペランドが全てキャッシュ可能かどうか
					for (int i=0; i<indicesLength; i++) {
						isAllIndicesCached &= operandCachingEnabled[i + 2]; // 右辺が1度でもfalseになるとその後左辺がfalseになる
					}

					// ※ 現状では、ELEMの dest はREFの dest と同様の扱いのため、上の isDestCached が true になる事は無く、
					//    従って以下の分岐の中で、一番速い CachedScalar 系のユニットに実際に割り当てられる事は無い。
					//    CachedScalar 系のユニットを使えるようにするには、
					//    Accelerator 内のスケジューラで dest のキャッシュ可能性を判断する精度を上げるか、
					//    またはELEM命令自体を参照リンクの有無で2種類の命令に分割して、
					//    コンパイラ側で使い分けるコードを出力する必要がある（参照リンクが無ければキャッシュ可能な場面が簡単に分かる）。
					//
					// どちらがいいか要検討、そのうち要実装（配列要素アクセスの高速化はメリットが大きいので）
					// > 後者を採用する準備として ELEM を REFELM に名称変更した。また後々で参照リンクを伴わない MOVELM を追加

					if(dataTypes[0] == DataType.INT64) {
						if (isAllIndicesCached // この命令に対しては dest の cacheability の分岐はユニット内で行うので、インデックス部のみで判断
								&& indicesLength <= Int64CachedScalarSubscriptUnit.MAX_AVAILABLE_RANK) {
							instruction.setAccelerationType(AcceleratorExecutionType.I64CS_SUBSCRIPT);
						} else if (indicesLength <= Int64ScalarSubscriptUnit.MAX_AVAILABLE_RANK) {
							instruction.setAccelerationType(AcceleratorExecutionType.I64S_SUBSCRIPT);
						} else {
							// Accelerator では任意次元ELEMには未対応なので Processor へ投げる（対応した際は上の if の3番目の条件を削る）
							instruction.setAccelerationType(AcceleratorExecutionType.BYPASS);
						}

					} else if (dataTypes[0] == DataType.FLOAT64) {
						if (isAllIndicesCached // この命令に対しては dest の cacheability の分岐はユニット内で行うので、インデックス部のみで判断
								&& indicesLength <= Float64CachedScalarSubscriptUnit.MAX_AVAILABLE_RANK) {
							instruction.setAccelerationType(AcceleratorExecutionType.F64CS_SUBSCRIPT);
						} else if (indicesLength <= Float64ScalarSubscriptUnit.MAX_AVAILABLE_RANK) {
							instruction.setAccelerationType(AcceleratorExecutionType.F64S_SUBSCRIPT);
						} else {
							// Accelerator では任意次元ELEMには未対応なので Processor へ投げる（対応した際は上の if の3番目の条件を削る）
							instruction.setAccelerationType(AcceleratorExecutionType.BYPASS);
						}

					} else if (dataTypes[0] == DataType.BOOL) {
						if (isAllIndicesCached // この命令に対しては dest の cacheability の分岐はユニット内で行うので、インデックス部のみで判断
								&& indicesLength <= BoolCachedScalarSubscriptUnit.MAX_AVAILABLE_RANK) {
							instruction.setAccelerationType(AcceleratorExecutionType.BCS_SUBSCRIPT);
						} else if (indicesLength <= BoolScalarSubscriptUnit.MAX_AVAILABLE_RANK) {
							instruction.setAccelerationType(AcceleratorExecutionType.BS_SUBSCRIPT);
						} else {
							// Accelerator では任意次元ELEMには未対応なので Processor へ投げる（対応した際は上の if の3番目の条件を削る）
							instruction.setAccelerationType(AcceleratorExecutionType.BYPASS);
						}

					} else {
						instruction.setAccelerationType(AcceleratorExecutionType.BYPASS);
					}
					break;
				}

				// 型変換命令 Cast instruction opcodes
				case CAST :
				{
					if(dataTypes[0] == DataType.INT64
							&& (dataTypes[1] == DataType.INT64 || dataTypes[1] == DataType.FLOAT64)) {

						if (isAllVector(operandScalar)) {
							instruction.setAccelerationType(AcceleratorExecutionType.I64V_TRANSFER);
						} else if (isAllScalar(operandScalar) && isAllCached(operandCachingEnabled)) {
							instruction.setAccelerationType(AcceleratorExecutionType.I64CS_TRANSFER);
						} else {
							instruction.setAccelerationType(AcceleratorExecutionType.I64S_TRANSFER);
						}

					} else if (dataTypes[0] == DataType.FLOAT64
							&& (dataTypes[1] == DataType.INT64 || dataTypes[1] == DataType.FLOAT64)) {

						if (isAllVector(operandScalar)) {
							instruction.setAccelerationType(AcceleratorExecutionType.F64V_TRANSFER);
						} else if (isAllScalar(operandScalar) && isAllCached(operandCachingEnabled)) {
							instruction.setAccelerationType(AcceleratorExecutionType.F64CS_TRANSFER);
						} else {
							instruction.setAccelerationType(AcceleratorExecutionType.F64S_TRANSFER);
						}

					} else {
						instruction.setAccelerationType(AcceleratorExecutionType.BYPASS);
					}
					break;
				}

				// 分岐命令 Branch instruction opcodes
				case JMP :
				case JMPN :
				{
					if(dataTypes[0] == DataType.BOOL) {

						// 条件オペランドがベクトルの場合（ベクトル論理演算での短絡評価などで使用される）
						// ※ 命令仕様上、条件オペランド以外は常にスカラ
						if (!operandScalar[2]) {
							instruction.setAccelerationType(AcceleratorExecutionType.BV_BRANCH);

						// 条件オペランドやその他オペランドが全てキャッシュ可能なスカラの場合（大半の場合はこれ）
						} else if (isAllScalar(operandScalar) && isAllCached(operandCachingEnabled)) {
							instruction.setAccelerationType(AcceleratorExecutionType.BCS_BRANCH);

						// 配列の要素などが条件に指定される場合など、キャッシュ不可能なスカラも一応あり得る
						} else {
							instruction.setAccelerationType(AcceleratorExecutionType.BS_BRANCH);
						}

					} else {
						instruction.setAccelerationType(AcceleratorExecutionType.BYPASS);
					}
					break;
				}

				// 内部関数関連命令 Internal function related opcode
				case CALL :
				case RET :
				case POP :
				case MOVPOP :
				case REFPOP :
				case ALLOCP :
				{
					instruction.setAccelerationType(AcceleratorExecutionType.FUNCTION_CONTROL);
					break;
				}
				case EX :
				{
					if (instruction.getExtendedOperationCode() == AcceleratorExtendedOperationCode.RETURNED) {
						instruction.setAccelerationType(AcceleratorExecutionType.FUNCTION_CONTROL);
						break;
					}
				}

				// 何もしない命令（ジャンプ先に配置されている） Nop instruction opcode
				case NOP :
				case ALLOCT : // この命令もコード内での型明示と最適化情報のための命令で、動作的には何もしない
				{
					instruction.setAccelerationType(AcceleratorExecutionType.NOP);
					break;

				}

				// その他の命令は全て現時点で未対応
				default : {
					instruction.setAccelerationType(AcceleratorExecutionType.BYPASS);
				}
			}
		}

	}



	// 全命令に対して再配置済み命令アドレスを書き込む
	private void updateReorderedAddresses() {
		int instructionLength = acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			acceleratorInstructionList.get(instructionIndex).setReorderedAddress(instructionIndex);
		}
	}


	// 全命令アドレスの再配置前→再配置後の対応を格納したアドレス変換マップを生成
	private void generateAddressReorderingMap() {
		this.addressReorderingMap = new HashMap<Integer,Integer>();

		int instructionLength = acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			AcceleratorInstruction instruction = this.acceleratorInstructionList.get(instructionIndex);
			addressReorderingMap.put(instruction.getUnreorderedAddress(), instruction.getReorderedAddress());
		}
	}


	// JMP命令など、ラベルオペランドを持つ命令は、ラベルの命令アドレスが命令再配置によって変わるため、再配置後のアドレス情報を追加
	private void resolveReorderedLabelAddress(Memory memory) {
		int instructionLength = this.acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			AcceleratorInstruction instruction = this.acceleratorInstructionList.get(instructionIndex);
			OperationCode opcode = instruction.getOperationCode();

			// JMP & JMPN & CALL はオペランドアドレスに飛ぶ。RETはスタックから取ったアドレスに飛ぶが、オペランドに所属関数ラベルを持っている
			if (opcode == OperationCode.JMP || opcode == OperationCode.JMPN || opcode == OperationCode.CALL || opcode == OperationCode.RET) {

				// ラベルの命令アドレスの値を格納するオペランドのデータコンテナをメモリから取得（必ずオペランド[1]にあるよう統一されている）
				DataContainer<?> addressContiner = null;
				addressContiner = memory.getDataContainer(
						instruction.getOperandPartitions()[1],
						instruction.getOperandAddresses()[1]
				);

				// データコンテナからラベルの命令アドレスの値を読む
				int labelAddress = -1;
				Object addressData = addressContiner.getData();
				if (addressData instanceof long[]) {
					labelAddress = (int)( ((long[])addressData)[0] );
				} else {
					throw new VnanoFatalException("Non-integer instruction address (label) operand detected.");
				}

				// ラベル命令アドレスの、命令再配置前における位置に対応する、再配置後のラベル命令アドレスを取得
				int reorderedLabelAddress = this.addressReorderingMap.get(labelAddress);

				// 再配置後のラベル命令アドレス情報を命令に持たせる
				instruction.setReorderedLabelAddress(reorderedLabelAddress);
			}
		}
	}


	// スカラのALLOC/ALLOCR命令をコード先頭に移す
	private void reorderAllocAndAllocrInstructions(AcceleratorDataManagementUnit dataManager) {

		int instructionLength = this.acceleratorInstructionList.size();

		// 再配置済みの命令列を格納する配列（最後にacceleratorInstructionList移す）
		if (this.buffer.length < instructionLength) {
			this.buffer = new AcceleratorInstruction[instructionLength]; // 足りなければ再確保（余っていればそのまま使う）
		} else {
			Arrays.fill(this.buffer, null);
		}

		// 元の命令列 instructions の要素の内、再配置された要素のインデックスをマークする配列
		boolean[] reordered = new boolean[instructionLength];
		Arrays.fill(reordered, false);

		int reorderedInstructionIndex = 0;

		// acceleratorInstructionList を先頭から末尾まで辿り、スカラALLOC/ALLOCR命令を reorderedInstruction に移す
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {

			AcceleratorInstruction instruction = acceleratorInstructionList.get(instructionIndex);
			OperationCode opcode = instruction.getOperationCode();

			if ( (opcode == OperationCode.ALLOC || opcode == OperationCode.ALLOCR)
					&& dataManager.isScalar(instruction.getOperandPartitions()[0], instruction.getOperandAddresses()[0])) {

				// reorderedInstruction に積む
				this.buffer[reorderedInstructionIndex] = instruction;
				reorderedInstructionIndex++;

				// このインデックスの命令は再配列済みである事をマークする
				reordered[instructionIndex] = true;
			}
		}

		// 再び acceleratorInstructionList を辿り、再配置されていない（スカラALLOC以外の）命令を移す
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			if (!reordered[instructionIndex]) {
				AcceleratorInstruction instruction = acceleratorInstructionList.get(instructionIndex);
				this.buffer[reorderedInstructionIndex] = instruction;
				reorderedInstructionIndex++;
			}
		}

		// バッファの中の完成した結果をacceleratorInstructionList移す
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			this.acceleratorInstructionList.set(instructionIndex, this.buffer[instructionIndex]);
		}
	}


	// 連続する2つの演算命令を融合させて1つの拡張命令にする
	private void fuseArithmeticInstructions(
			AcceleratorExecutionType fromAccelerationType, AcceleratorExecutionType toAccelerationType) {

		int instructionLength = this.acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength-1; instructionIndex++) { // 後でイテレータ使うループにする

			AcceleratorInstruction currentInstruction = this.acceleratorInstructionList.get(instructionIndex);
			AcceleratorInstruction nextInstruction = this.acceleratorInstructionList.get(instructionIndex+1);
			AcceleratorExecutionType currentAccelType = currentInstruction.getAccelerationType();
			AcceleratorExecutionType nextAccelType = nextInstruction.getAccelerationType();

			// 対象命令と次の命令が指定された演算タイプでなければ、その時点でスキップ
			if (currentAccelType != fromAccelerationType
					|| nextAccelType != fromAccelerationType) {

				continue;
			}

			// 対象命令のオペランドを全て取得
			Memory.Partition[] currentOperandPartitions = currentInstruction.getOperandPartitions();
			int[] currentOperandAddresses = currentInstruction.getOperandAddresses();

			// 次の命令のオペランドを全て取得
			int nextOperandLength = nextInstruction.getOperandLength();
			Memory.Partition[] nextOperandPartitions = nextInstruction.getOperandPartitions();
			int[] nextOperandAddresses = nextInstruction.getOperandAddresses();

			// 次の命令の入力オペランド（1番以降）の中から、対象命令の出力オペランド（0番）と一致するものを探す
			int sameInputIndex = -1;
			int sameInputCount = 0;
			for (int operandIndex=1; operandIndex<nextOperandLength; operandIndex++) {
				if (nextOperandPartitions[operandIndex] == currentOperandPartitions[0]
						&& nextOperandAddresses[operandIndex] == currentOperandAddresses[0]) {

					sameInputIndex = operandIndex;
					sameInputCount++;
				}
			}

			// 対象命令の出力オペランドと次命令の入力オペランドが、全く一致しなかった場合や、複数一致した場合はスキップ
			if (sameInputCount != 1) {
				continue;
			}


			// ここまで到達するのは、対象命令と次命令がキャッシュ可能な算術スカラ演算であり、かつ、
			// 対象命令の出力オペランドを、次命令の入力オペランドに一回だけ使っている場合なので、
			// それら2命令を1つに融合した拡張命令に変換する


			// 象命令と次の命令を融合した拡張命令を生成
			AcceleratorInstruction fusedInstruction = currentInstruction.fuse(
				nextInstruction, toAccelerationType
			);
			fusedInstruction.setFusedInputOperandIndices(new int[]{ sameInputIndex });

			// リスト内の対象命令を融合拡張命令で置き換える
			this.acceleratorInstructionList.set(instructionIndex, fusedInstruction);

			// 次の命令は既に融合したので、リストにnullを置く（後処理で効率的に削除して詰める）
			this.acceleratorInstructionList.set(instructionIndex + 1, null);

			// 2命令分処理したので、カウンタを1つ余計に進める
			instructionIndex++;
		}

		// リスト内で空いた要素（上でnullを置いている）を削除して詰める
		this.removeNullInstructions();
	}


	// レジスタへの無駄なMOV命令を削減し、算術演算の出力オペランド等で直接レジスタに代入するようにする
	private void reduceMovInstructions(AcceleratorDataManagementUnit dataManager) {

		int instructionLength = this.acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength-1; instructionIndex++) { // 後でイテレータ使うループにする
			AcceleratorInstruction currentInstruction = this.acceleratorInstructionList.get(instructionIndex);
			AcceleratorInstruction nextInstruction = this.acceleratorInstructionList.get(instructionIndex+1);

			// 対象命令がデータ書き込みをしない場合は命令はスキップ
			if (!this.isDataWritingOperationCode(currentInstruction.getOperationCode())) {
				continue;
			}

			// 対象命令の書き込み先（0番オペランド）がレジスタでない場合はスキップ
			if (currentInstruction.getOperandPartitions()[0] != Memory.Partition.REGISTER) {
				continue;
			}

			// 次にMOV命令が続いていなければスキップ
			if (nextInstruction.getOperationCode() != OperationCode.MOV) {
				continue;
			}

			// そのMOV命令のコピー元（1番オペランド）がレジスタではない場合はスキップ
			if (nextInstruction.getOperandPartitions()[1] != Memory.Partition.REGISTER) {
				continue;
			}

			// 対象命令で書き込んでいるレジスタ（ = 0番オペランド）のアドレスを取得
			int writingRegisterAddress = currentInstruction.getOperandAddresses()[0];

			// 次のMOV命令でのコピー元レジスタを取得
			int movingInputRegisterAddress = nextInstruction.getOperandAddresses()[1];

			// 書き込み先レジスタと次命令でのMOVコピー「元」レジスタが異なる場合はスキップ
			if (writingRegisterAddress != movingInputRegisterAddress) {
				continue;
			}

			// そのレジスタに書き込む/読み込む箇所がそこだけでない（書き込みが複数、または読み込みが複数ある）場合はスキップ
			if (this.registerWrittenPointCount[writingRegisterAddress] != 1
					|| this.registerReadPointCount[writingRegisterAddress] != 1) {

				continue;
			}

			// 書き込み先とMOVコピー「先」が、スカラであるかやキャッシュ可能か等の特性が一致していない場合はスキップ
			// (例えば参照リンクされているなどのキャッシュ不可能なもの（=他の箇所への影響解析の難度が高い）に出力していた箇所を、
			//  直後のMOVの出力先で置き換えてしまうと、元の出力先に他の地点から別アドレス経由でアクセスしていた場合などにまずい。
			//  MOV削り条件では対象アドレスへの読み書き箇所を追ってはいるが、別アドレスと参照リンクされているものは追えないため。)
			Memory.Partition movOutputPartition = nextInstruction.getOperandPartitions()[0];
			int movOutputAddress = nextInstruction.getOperandAddresses()[0];
			if (dataManager.isScalar(Memory.Partition.REGISTER, writingRegisterAddress)
					!= dataManager.isScalar(movOutputPartition, movOutputAddress)) {
				continue;
			}
			if (dataManager.isCachingEnabled(Memory.Partition.REGISTER, writingRegisterAddress)
					!= dataManager.isCachingEnabled(movOutputPartition, movOutputAddress)) {
				continue;
			}

			// (以下最適化案、全部のCached系演算周りの実装規模を倍増させてもいいくらいの気力がある時に要検討）
			//
			// 上記の if 文、この条件の影響で、例えば
			//
			//   REFELM  R0(dest) L0(src) C0(index);  // L0[C0] の参照をR0にリンク
			//   MOVELM  R1(dest) L1(src) C1(index);  // L1[C1] の値をR1に単純コピー
			//   MOV     R0(dest) R1(src)             // R1の値をR0に単純コピー (リンクされているL0[C0]も書き換わる)
			//
			// の場合に、R1 への他からの直接（そのアドレスで）アクセスさえ無ければ、
			//
			//   REFELM  R0(dest) L0(src) C0(index);  // L0[C0] の参照をR0にリンク
			//   MOVELM  R0(dest) R1(src) C1(index);  // L1[C1] の値をR0に単純コピー (リンクされているL0[C0]も書き換わる)
			//
			// に削る事は可能なはず。
			// しかし REFELM の dest は参照リンク対象のため常に uncacheable と判定されるので、
			// 上の if 文の条件では movOutputAddress が uncachable となり、現状のままではMOV削りが行われない。
			// なので現状の除外条件はもう少し絞り込めるはずで、可能性の検出があまり大がかりにならなければ絞り込みたい。
			//
			//   > MOV直前命令が uncacheable な dest（参照リンクされたレジスタとか）に書き込んでいる場合は置き換えて dest 削るとまずいけど、
			//     MOV直前命令が cacheable な dest（非リンクなスカラレジスタとか / 上の例でのR1）に書き込んでいて、
			//     それを直後に uncachable な別のもの dest2 (上の例での R0) へと MOV している場合は、
			//     MOV直前命令の dest を dest2 にして元の dest 削っても大丈夫だと思う(他のMOV削り条件を満たしていれば)。
			//     cacheable 判定されているという事は、本来の dest 先を別アドレス経由で読もうとする事はされていないはずなので。
			//     とすると上のif文は、cacheability に関してはやはり writingRegister... の条件のみ必要で、movOutput... に対しては不要だと思う。
			//     > ただそれによって、MOV直前命令が非Cached系のユニットで処理されるようになるので、それが果たして総合的に特になるかどうか ?
			//       MOV直前命令をCached系ユニットで処理して、直後に非CachedなMOVでコピーする方が総合的には速いかもしれない。
			//       MOV直前命令の処理は恐らくMOVよりは複雑な実装なので、それがCachedから非Cachedになるオーバーヘッド増が、
			//       果たして非Cached MOVを1個削れる分を上回るかどうか? 全種の命令において。微妙な所かもしれない。
			//       > MOVELM/REFELM は dest が uncacheable でも index が cacheable ならCached系ユニットで処理される。
			//         その場合は dest アクセスのみ仮想メモリに直書きされるだけなので、非Cached MOVを削った分以上に処理コストが増える事はないはず。
			//         > 上の例だけではなく一般に他の命令との組み合わせも有り得るので、命令とそのユニットの実装によっては遅くなる場合もあるはず。
			//           例えばADDの非Cached演算は全オペランドをキャッシュ介さず仮想メモリから読むので、Cached演算よりだいぶオーバーヘッド食う。
			//           > それなら全Cached系ユニットで、dest のみ uncacheable な場合を(最小コスト増で済む実装で)サポートすればデメリット無くMOV削り可能?
			//             例えばa[i] = b + c みたいなパターンだとa[i]部のみが(REFELMで参照した) uncacheable なレジスタになるけど、
			//             b と c は cacheable な普通のスカラである場合、CachedScalarArithmeticUnit 内で Float64CachedScalarAddNode の
			//             代入左辺のみ仮想メモリアクセスに直書きするような Float64SemiCached...Node みたいなのを作って、それを割り当てるなど。
			//             > 今思いつく限りではそうすればオーバーヘッド関連の懸念は解決しそうだけれど、全Cached系ユニットの実装規模がほぼ倍増する…
			//               普通にキャッシュ内で閉じた演算して直後に非Cached MOVで同期 兼 書き込みするのと比べて、それやるだけのメリットがあるかどうか…
			//               > でも右辺で単純な算術演算やって左辺の配列要素に格納するという場合は結構たくさんあると思うので、効く頻度は高そうな気がする。
			//                 パターンが累乗で増える Dual/TrippleArithmetic とかと比べれば2倍で済んで効果期待できそうなら意外とありかも。
			//                 (ただ他の優先度高い最適化案が一通り済んだ後に)
			//
			// またそのうち要検討

			// 次に続くMOV命令を削るとまずい命令ならスキップ（ELEM命令など）
			if(!movReducableOpcodeSet.contains(currentInstruction.getOperationCode())) {
				continue;
			}


			// --------------------------------------------------------------------------------
			// ここまで到達するのは：
			//
			// ・演算結果をレジスタに格納し、
			// ・次で別の領域にMOVしていて、
			// ・そのレジスタを他のどこでも使用しておらず、
			// ・命令の演算結果を、MOV先に直接格納するように改変しても問題ない場合
			//
			// なので、オペランドを入れ替えて冗長なMOV命令を削る
			// --------------------------------------------------------------------------------


			// 対象演算命令のオペランド部を複製
			int modifiedOperandLength = currentInstruction.getOperandLength();
			Memory.Partition[] modifiedOperandPartitions = new Memory.Partition[modifiedOperandLength];
			System.arraycopy(currentInstruction.getOperandPartitions(), 0, modifiedOperandPartitions, 0, modifiedOperandLength);
			int[] modifiedOperandAddresses = new int[modifiedOperandLength];
			System.arraycopy(currentInstruction.getOperandAddresses(), 0, modifiedOperandAddresses, 0, modifiedOperandLength);

			// 複製したオペランド部の出力オペランドに、MOVコピー「先」オペランドを写す
			modifiedOperandPartitions[0] = movOutputPartition;
			modifiedOperandAddresses[0] = movOutputAddress;

			// それをオペランド部として持つ、対象演算命令のコピーを生成し、元の対象演算命令を置き換える
			AcceleratorInstruction modifiedInstruction = new AcceleratorInstruction(
				currentInstruction, modifiedOperandPartitions, modifiedOperandAddresses
			);
			this.acceleratorInstructionList.set(instructionIndex, modifiedInstruction);

			// MOV命令を null で置き換える（すぐ後で削除する）
			this.acceleratorInstructionList.set(instructionIndex+1, null);

			// 次の命令（= MOV）はもう削除したので、カウンタを1つ余計に進める
			instructionIndex++;

		}

		this.removeNullInstructions();
		// MOV命令を削除した位置の null を詰める
	}


	// 命令列内の null 要素を削除して詰める（removeを何度も行う処理を効率化するため、nullを置いて後でこのメソッドで一括で詰める）
	private void removeNullInstructions() {

		int instructionLength = this.acceleratorInstructionList.size();

		// 再配置済みの命令列を格納する配列（最後にacceleratorInstructionList移す）
		if (this.buffer.length < instructionLength) {
			this.buffer = new AcceleratorInstruction[instructionLength]; // 足りなければ再確保（余っていればそのまま使う）
		} else {
			Arrays.fill(this.buffer, null);
		}

		// acceleratorInstructionList 内の非null要素を詰めながらbufferに移す
		int bufferIndex = 0;
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			AcceleratorInstruction instruction = this.acceleratorInstructionList.get(instructionIndex);
			if (instruction != null) {
				this.buffer[bufferIndex] = instruction;
				bufferIndex++;
			}
		}

		instructionLength = bufferIndex;
		this.acceleratorInstructionList = new ArrayList<AcceleratorInstruction>(instructionLength);

		// bufferからacceleratorInstructionListに戻す
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			this.acceleratorInstructionList.add(buffer[instructionIndex]);
		}
	}

}
