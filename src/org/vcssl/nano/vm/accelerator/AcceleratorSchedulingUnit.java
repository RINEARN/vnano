/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;

public class AcceleratorSchedulingUnit {

	// List 内の null 要素を removeAll する際に渡す (removeAllの引数は Collection インスタンスであるべきなので素の null は渡せない)
	private static final List<Object> LIST_OF_NULL = Arrays.asList((Object)null);

	private List<AcceleratorInstruction> acceleratorInstructionList;
	private Map<Integer,Integer> addressReorderingMap;

	public AcceleratorInstruction[] schedule(
			AcceleratorInstruction[] instructions, Memory memory, AcceleratorDataManagementUnit dataManager) {

		this.acceleratorInstructionList = new ArrayList<AcceleratorInstruction>();
		for (AcceleratorInstruction instruction: instructions) {
			this.acceleratorInstructionList.add( instruction.clone() );
		}


		// 要検討： AcceleratorExecutionType の名前を変えたい。
		//          割り当て先の演算ユニットを一意に指しているものと分かりやすい名前に。
		//          そして下で処理を行っているメソッドの名前もそれにあわせて変えたい。
		//          割り当て先の演算ユニットを判断する的な名前に。
		//          ( ただし "dispatch～" は AcceleratorDispatchUnit で
		//            演算ユニットに紐づけて演算ノードを生成する処理をそう呼んでいるので使えない ）

		// 各命令の AcceleratorExecutionType を判定して設定
		this.detectAccelerationTypes(memory, dataManager);

		// 連続する算術スカラ演算命令2個を融合させて1個の拡張命令に置き換える
		this.fuseArithmeticInstructions( // Float64 Cached-Scalar Arithmetic
				AcceleratorExecutionType.F64CS_ARITHMETIC, AcceleratorExecutionType.F64CS_DUAL_ARITHMETIC
		);
		this.fuseArithmeticInstructions( // Int64 Cached-Scalar Arithmetic
				AcceleratorExecutionType.I64CS_ARITHMETIC, AcceleratorExecutionType.I64CS_DUAL_ARITHMETIC
		);

		// 最新の（再配列後の）命令アドレスを設定し、新旧の対応を保持するマップを更新
		// -> このあたりの処理は AcceleratorOptimizationUnit と重複しているので、きりのいい時になんとかしたい
		this.updateReorderedAddresses();
		this.generateAddressReorderingMap();

		// ジャンプ命令の飛び先アドレスを補正したものを求めて設定
		// -> ここも AcceleratorOptimizationUnit と重複しているので、きりのいい時になんとかしたい
		this.resolveReorderedLabelAddress(memory);

		return this.acceleratorInstructionList.toArray(new AcceleratorInstruction[0]);
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
				case NEG :
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

		// 注:
		//
		// 最適化で命令1個だった所が複数命令になっている箇所がある場合 (CALLの引数直接MOV化とか) など、
		// 複数命令が同じ unreorderedAddress を持っていると、このマップはそれらの最後の命令の reorderedAddress を返す。
		//
		// このマップは現状ではJMP/JMPN/CALLの着地点ラベル（由来のNOP命令）の位置を補正するために使用されるが、
		// ラベル由来NOPにはそういった複数命令への分裂は起こらないため、その目的においては問題は生じない。
		//
		// ただし、別の目的でこのマップを参照する際は、影響の有無について要注意。
		// 場合によってはもっと手の込んだ仕組みを用意する必要が出てくるが、現状では単純で済むマップを使っている。

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

				// データコンテナから飛び先ラベル（アセンブル後はNOPになっている）の命令アドレスの値を読む
				int labelAddress = -1;
				Object addressData = addressContiner.getData();
				if (addressData instanceof long[]) {
					labelAddress = (int)( ((long[])addressData)[0] );
				} else {
					throw new VnanoFatalException("Non-integer instruction address (label) operand detected.");
				}

				// ラベル（由来のNOP命令）アドレスの、命令再配置前における位置に対応する、再配置後の位置の命令アドレスを取得
				int reorderedLabelAddress = this.addressReorderingMap.get(labelAddress);

				// 再配置後のラベル命令アドレス情報を命令に持たせる
				instruction.setReorderedLabelAddress(reorderedLabelAddress);
			}
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

			// DualArithmetic系演算ユニットでサポートしていないオペランドの場合はスキップ (例えば符号反転など)
			if (fromAccelerationType == AcceleratorExecutionType.F64CS_ARITHMETIC) {
				if( !Float64CachedScalarDualArithmeticUnit.AVAILABLE_OPERAND_SET.contains(currentInstruction.getOperationCode())
				    ||
				    !Float64CachedScalarDualArithmeticUnit.AVAILABLE_OPERAND_SET.contains(nextInstruction.getOperationCode()) ) {
					continue;
				}
			}
			if (fromAccelerationType == AcceleratorExecutionType.I64CS_ARITHMETIC) {
				if( !Int64CachedScalarDualArithmeticUnit.AVAILABLE_OPERAND_SET.contains(currentInstruction.getOperationCode())
				    ||
				    !Int64CachedScalarDualArithmeticUnit.AVAILABLE_OPERAND_SET.contains(nextInstruction.getOperationCode()) ) {
					continue;
				}
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
		this.acceleratorInstructionList.removeAll(LIST_OF_NULL);
	}
}
