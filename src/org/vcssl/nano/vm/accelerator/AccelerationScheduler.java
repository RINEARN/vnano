/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
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

public class AccelerationScheduler {


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

		movReducableOpcodeSet.add(OperationCode.AND);
		movReducableOpcodeSet.add(OperationCode.OR);
		movReducableOpcodeSet.add(OperationCode.NOT);
		movReducableOpcodeSet.add(OperationCode.CAST);

		movReducableOpcodeSet.add(OperationCode.MOVPOP); // MOVPOPでスタックから取って直後にMOVするだけのは削っても安全（REFPOPは無理）
	}


	public AcceleratorInstruction[] schedule(
			Instruction[] instructions, Memory memory, AccelerationDataManager dataManager) {

		// 命令列を読み込んで AcceleratorInstruction に変換し、フィールドのリストを初期化して格納
		this.initializeeAcceleratorInstructionList(instructions, memory);

		// 全てのレジスタに対し、それぞれ書き込み・読み込み箇所の数をカウントする（最適化に使用）
		this.createRegisterWrittenPointCount(memory);
		this.createRegisterReadPointCount(memory);

		// 各命令の AccelerationType を判定して設定
		this.detectAccelerationTypes(memory, dataManager);

		// スカラのALLOC命令をコード先頭に並べ替える（メモリコストが低いので、ループ内に混ざるよりも利点が多い）
		this.reorderAllocAndAllocrInstructions(dataManager);

		// オペランドを並び替え、不要になったMOV命令を削る
		this.reduceMovInstructions(dataManager);


		// 連続する算術スカラ演算命令2個を融合させて1個の拡張命令に置き換える
		this.fuseArithmeticInstructions( // Float64 Cached-Scalar Arithmetic
				AccelerationType.F64CS_ARITHMETIC, AccelerationType.F64CS_DUAL_ARITHMETIC
		);
		this.fuseArithmeticInstructions( // Int64 Cached-Scalar Arithmetic
				AccelerationType.I64CS_ARITHMETIC, AccelerationType.I64CS_DUAL_ARITHMETIC
		);

		// 連続する算術ベクトル演算命令2個を融合させて1個の拡張命令に置き換える
		// >> 実行環境バージョンやPCの状態等により性能が上下どちらにも大きく振れるので、少なくとも現時点では無効化
		/*
		this.fuseArithmeticInstructions( // Float64 Vector Arithmetic
				AccelerationType.F64V_ARITHMETIC, AccelerationType.F64V_DUAL_ARITHMETIC
		);
		this.fuseArithmeticInstructions( // Int64 Vector Arithmetic
				AccelerationType.I64V_ARITHMETIC, AccelerationType.I64V_DUAL_ARITHMETIC
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


	// データに対して書き込みを行う命令なら true を返す（ただしレジスタの確保・解放は除外）
	private boolean isDataWritingOperationCode(OperationCode operationCode) {

		// 書き込みを「行わない」ものを以下に列挙し、それ以外なら true を返す
		return operationCode != OperationCode.ALLOC
		     && operationCode != OperationCode.ALLOCP
		     && operationCode != OperationCode.ALLOCR
		     && operationCode != OperationCode.FREE
		     && operationCode != OperationCode.NOP
		     && operationCode != OperationCode.JMP
		     && operationCode != OperationCode.JMPN
		     && operationCode != OperationCode.RET
		     && operationCode != OperationCode.POP      // POP はスタックからデータを取り出すだけで何もしない
		     ;

		// IFCU & RET 命令について：
		//   関数の引数や戻り値はスタックに積まれるので、CALL命令やRET命令自体はオペランドには何も書き込まないが、
		//   スタックに積まれた後のデータがどこで取り出されて読み書きされるかを静的に解析するのは複雑なので、
		//   IFCU & RET 命令のオペランドは便宜的に、書き込み箇所カウントに含める（最適化予防のため）
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
		     && operationCode != OperationCode.ALLOCP
		     && operationCode != OperationCode.ALLOCR
		     && operationCode != OperationCode.FREE
		     && operationCode != OperationCode.POP      // POP はスタックからデータを取り出すだけで何もしない
		     && operationCode != OperationCode.MOVPOP   // MOVPOP はスタックからデータを読むので、オペランドからは読まない（書く）
		     && operationCode != OperationCode.REFPOP   // REFPOP はスタックからデータを読むので、オペランドからは読まない（書く）
		     && operationCode != OperationCode.NOP   // NOP は何もしないので明らかに何も読まない
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


	private void detectAccelerationTypes(Memory memory, AccelerationDataManager dataManager) {

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
			boolean[] operandCached = new boolean[operandLength];
			ScalarCache[] operandCaches = new ScalarCache[operandLength];


			// データマネージャから、オペランドのスカラ判定結果とキャッシュ有無およびキャッシュ参照を取得し、
			// 定数かどうかも控える
			for (int operandIndex=0; operandIndex<operandLength; operandIndex++) {
				operandScalar[operandIndex] = dataManager.isScalar(partitions[operandIndex], addresses[operandIndex]);
				operandCached[operandIndex] = dataManager.isCached(partitions[operandIndex], addresses[operandIndex]);
				if (operandCached[operandIndex]) {
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
					// スカラ確保の場合
					if (dataManager.isScalar(partitions[0], addresses[0])) {
						instruction.setAccelerationType(AccelerationType.S_ALLOC);
					// ベクトル確保の場合
					} else {
						instruction.setAccelerationType(AccelerationType.Unsupported);
					}
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
							instruction.setAccelerationType(AccelerationType.I64V_ARITHMETIC);
						// 全部キャッシュ可能なスカラの場合の演算
						} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
							instruction.setAccelerationType(AccelerationType.I64CS_ARITHMETIC);
						// キャッシュ不可能なスカラ演算の場合（インデックス参照がある場合や、長さ1のベクトルとの混合演算など）
						} else {
							instruction.setAccelerationType(AccelerationType.I64S_ARITHMETIC);
						// ベクトル・スカラ混合演算で、スカラをベクトルに昇格する場合は？
						// →それはスクリプト側の仕様で、中間コードレベルではサポートしていない
						}

					} else if (dataTypes[0] == DataType.FLOAT64) {

						if (isAllVector(operandScalar)) {
							instruction.setAccelerationType(AccelerationType.F64V_ARITHMETIC);
						} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
							instruction.setAccelerationType(AccelerationType.F64CS_ARITHMETIC);
						} else {
							instruction.setAccelerationType(AccelerationType.F64S_ARITHMETIC);
						}

					} else {
						instruction.setAccelerationType(AccelerationType.Unsupported);
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
							instruction.setAccelerationType(AccelerationType.I64V_COMPARISON);
						} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
							instruction.setAccelerationType(AccelerationType.I64CS_COMPARISON);
						} else {
							instruction.setAccelerationType(AccelerationType.I64S_COMPARISON);
						}

					} else if (dataTypes[0] == DataType.FLOAT64) {

						if (isAllVector(operandScalar)) {
							instruction.setAccelerationType(AccelerationType.F64V_COMPARISON);
						} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
							instruction.setAccelerationType(AccelerationType.F64CS_COMPARISON);
						} else {
							instruction.setAccelerationType(AccelerationType.F64S_COMPARISON);
						}

					} else {
						instruction.setAccelerationType(AccelerationType.Unsupported);
					}
					break;
				}

				// 論理演算命令 Logical instruction opcodes
				case AND :
				case OR :
				case NOT :
				{
					if(dataTypes[0] == DataType.BOOL) {

						if (isAllVector(operandScalar)) {
							instruction.setAccelerationType(AccelerationType.BV_LOGICAL);
						} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
							instruction.setAccelerationType(AccelerationType.BCS_LOGICAL);
						} else {
							instruction.setAccelerationType(AccelerationType.BS_LOGICAL);
						}

					} else {
						instruction.setAccelerationType(AccelerationType.Unsupported);
					}
					break;
				}

				// 転送命令 Trsndfer instruction opcodes
				case MOV :
				case CAST :
				case FILL :
				{
					if(dataTypes[0] == DataType.INT64) {

						if (isAllVector(operandScalar)) {
							instruction.setAccelerationType(AccelerationType.I64V_TRANSFER);
						} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
							instruction.setAccelerationType(AccelerationType.I64CS_TRANSFER);
						} else {
							instruction.setAccelerationType(AccelerationType.I64S_TRANSFER);
						}

					} else if (dataTypes[0] == DataType.FLOAT64) {

						if (isAllVector(operandScalar)) {
							instruction.setAccelerationType(AccelerationType.F64V_TRANSFER);
						} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
							instruction.setAccelerationType(AccelerationType.F64CS_TRANSFER);
						} else {
							instruction.setAccelerationType(AccelerationType.F64S_TRANSFER);
						}

					} else if (dataTypes[0] == DataType.BOOL) {

						if (isAllVector(operandScalar)) {
							instruction.setAccelerationType(AccelerationType.BV_TRANSFER);
						} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
							instruction.setAccelerationType(AccelerationType.BCS_TRANSFER);
						} else {
							instruction.setAccelerationType(AccelerationType.BS_TRANSFER);
						}

					} else {
						instruction.setAccelerationType(AccelerationType.Unsupported);
					}
					break;
				}

				// 分岐命令 Branch instruction opcodes
				case JMP :
				case JMPN :
				{
					if(dataTypes[0] == DataType.BOOL) {

						// 大半の場合、条件はキャッシュ可能なスカラ
						if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
							instruction.setAccelerationType(AccelerationType.BCS_BRANCH);

						// 配列の要素などが条件に指定される場合など、キャッシュ不可能なスカラも一応あり得る
						} else {
							instruction.setAccelerationType(AccelerationType.BS_BRANCH);
						}

					} else {
						instruction.setAccelerationType(AccelerationType.Unsupported);
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
					instruction.setAccelerationType(AccelerationType.IFCU);
					break;
				}

				// 何もしない命令（ジャンプ先に配置されている） Nop instruction opcode
				case NOP :
				{
					instruction.setAccelerationType(AccelerationType.NOP);
					break;

				}

				// その他の命令は全て現時点で未対応
				default : {
					instruction.setAccelerationType(AccelerationType.Unsupported);
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
			//System.out.println("Put reordering map: " + instruction.getUnreorderedAddress() + ", " + instruction.getReorderedAddress());
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
	private void reorderAllocAndAllocrInstructions(AccelerationDataManager dataManager) {

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
			AccelerationType fromAccelerationType, AccelerationType toAccelerationType) {

		int instructionLength = this.acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength-1; instructionIndex++) { // 後でイテレータ使うループにする

			AcceleratorInstruction currentInstruction = this.acceleratorInstructionList.get(instructionIndex);
			AcceleratorInstruction nextInstruction = this.acceleratorInstructionList.get(instructionIndex+1);
			AccelerationType currentAccelType = currentInstruction.getAccelerationType();
			AccelerationType nextAccelType = nextInstruction.getAccelerationType();

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
	private void reduceMovInstructions(AccelerationDataManager dataManager) {

		int instructionLength = this.acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength-1; instructionIndex++) { // 後でイテレータ使うループにする
			AcceleratorInstruction currentInstruction = this.acceleratorInstructionList.get(instructionIndex);
			AcceleratorInstruction nextInstruction = this.acceleratorInstructionList.get(instructionIndex+1);

			// 対象命令がデータ書き込みをしない場合は命令はスキップ
			if (!this.isDataWritingOperationCode(currentInstruction.getOperationCode())) {
				continue;
			}

			// 対象命令の書き込み先（0番オペランド）がジスタでない場合はスキップ
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
			Memory.Partition movOutputPartition = nextInstruction.getOperandPartitions()[0];
			int movOutputAddress = nextInstruction.getOperandAddresses()[0];
			if (dataManager.isScalar(Memory.Partition.REGISTER, writingRegisterAddress)
					!= dataManager.isScalar(movOutputPartition, movOutputAddress)) {
				continue;
			}
			if (dataManager.isCached(Memory.Partition.REGISTER, writingRegisterAddress)
					!= dataManager.isCached(movOutputPartition, movOutputAddress)) {
				continue;
			}

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
			//AcceleratorInstruction instruction = this.acceleratorInstructionList.get(instructionIndex);
			//System.out.println("ACCEL TYPE=" + instruction.getAccelerationType() + " INST=" + instruction);
		}
	}

}
