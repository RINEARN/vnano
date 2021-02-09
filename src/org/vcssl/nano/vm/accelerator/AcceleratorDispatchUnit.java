/*
 * Copyright(C) 2017-2021 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.VnanoFatalException;

import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.MetaInformationSyntax;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.processor.Processor;

public class AcceleratorDispatchUnit {

	// 命令列の内容を全て演算器に割り当て、演算を実行するための演算ノード（演算器内部に実装）の列を返す
	public AcceleratorExecutionNode[] dispatch (
			Processor processor, Memory memory, Interconnect interconnect,
			AcceleratorInstruction[] instructions, AcceleratorDataManagementUnit dataManager,
			BypassUnit bypassUnit, InternalFunctionControlUnit internalFunctionControlUnit,
			ExternalFunctionControlUnit externalFunctionControlUnit) throws VnanoException {

		// !!!!!
		// 長すぎ  きりのいい時に要リファクタ
		// !!!!!

		int instructionLength = instructions.length;
		AcceleratorExecutionNode[] nodes = new AcceleratorExecutionNode[instructionLength];


		// 命令列から演算ノード列を生成（ノードのコンストラクタで次ノードを指定するため、ループは命令列末尾から先頭へ辿る）
		AcceleratorExecutionNode nextNode = null; // 現在の対象命令の次の命令（＝前ループでの対象命令）を控える
		for (int instructionIndex = instructionLength-1; 0<=instructionIndex; instructionIndex--) {
		//for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {

			AcceleratorInstruction instruction = instructions[instructionIndex];

			try {

				// 命令からオペランドのデータアドレスを取得
				Memory.Partition[] partitions = instructions[instructionIndex].getOperandPartitions();
				int[] addresses = instructions[instructionIndex].getOperandAddresses();
				int operandLength = addresses.length;

				// アドレスからオペランドのデータコンテナを取得
				DataContainer<?>[] operandContainers = new DataContainer[operandLength];
				for (int operandIndex=0; operandIndex<operandLength; operandIndex++) {
					operandContainers[operandIndex] = memory.getDataContainer(partitions[operandIndex], addresses[operandIndex]);
				}

				// オペランドの状態とキャッシュ参照などを控える配列を用意
				boolean[] operandConstant = new boolean[operandLength];
				boolean[] operandScalar = new boolean[operandLength];
				boolean[] operandCachingEnabled = new boolean[operandLength];
				ScalarCache[] operandCaches = new ScalarCache[operandLength];

				// データマネージャから、オペランドのスカラ判定結果、キャッシュ有無、キャッシュ参照、定数かどうかの状態を控える
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


				// 対象命令（1個）を演算器にディスパッチして演算ノードを取得
				AcceleratorExecutionNode currentNode = null;
				currentNode = this.dispatchToAcceleratorExecutionUnit(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant,
					bypassUnit, internalFunctionControlUnit, externalFunctionControlUnit,
					nextNode
				);


				// エラー発生時に原因命令を辿れるように、ノードに元の命令を格納
				currentNode.setSourceInstruction(instruction);

				// 生成したノードをノード列に格納
				nodes[instructionIndex] = currentNode;

				// 次ループ（命令末尾から先頭へ辿る）内で仕様するため、現在のノードを（アドレス的に）次のノードとして控える
				nextNode = currentNode;

			} catch (Exception causeException) {

				// 原因命令のアドレス類、およびスクリプト内で命令に対応する箇所のファイル名や行番号を抽出
				int unreorderedAddress = instruction.getUnreorderedAddress();
				int reorderedAddress = instruction.getReorderedAddress();
				int lineNumber = MetaInformationSyntax.extractLineNumber(instruction, memory);
				String fileName = MetaInformationSyntax.extractFileName(instruction, memory);
				String[] errorWords = {
						Integer.toString(unreorderedAddress), Integer.toString(reorderedAddress), instruction.toString()
				};
				throw new VnanoException(ErrorType.UNEXPECTED_ACCELERATOR_CRASH, errorWords, causeException, fileName, lineNumber);
			}
		}

		// 別の命令アドレスに飛ぶ処理のノードに、着地先ノードの参照を持たせる
		for (int instructionIndex = 0; instructionIndex<instructionLength; instructionIndex++) {
			AcceleratorInstruction instruction = instructions[instructionIndex];
			OperationCode opcode = instruction.getOperationCode();
			OperationCode[] fusedOpcodes = instruction.isFused() ? instruction.getFusedOperationCodes() : null;

			// 分岐系命令（内部関数コード含む）かどうかを確認して控える。
			// JMP & JMPN & CALL は静的に設定されたラベルに飛ぶ。
			// RET は動的にスタックから取ったアドレスに飛ぶが、IFCU制御用に所属関数のアドレスも持ってる。
			boolean isBranchOperation = opcode == OperationCode.JMP || opcode == OperationCode.JMPN
					|| opcode == OperationCode.CALL || opcode == OperationCode.RET;

			// 比較演算などと融合された分岐系命令かどうかを確認して控える（融合対象になり得るのは JMP と JMPN のみ）
			boolean isFusedBranchOperation = instruction.isFused()
					&& (fusedOpcodes[1] == OperationCode.JMP || fusedOpcodes[1] == OperationCode.JMPN);

			// 上記で検出した命令の場合は飛び先の命令アドレスは静的に確定しているので、着地先ノードを求めて持たせる
			if (isBranchOperation || isFusedBranchOperation) {
				int branchedAddress = instruction.getReorderedLabelAddress(); // 注：命令再配置で飛び先アドレスは変わる
				nodes[instructionIndex].setLaundingPointNodes(nodes[branchedAddress]);
			}

			// 関数のRET命令などは、スタックに積まれた値を読んでその命令アドレスに飛ぶので、
			// 実行時まで着地先がわからないため、全ノードを持たせておく必要がある。
			// そのため、それらの命令を管理する InternalFunctionControlUnit に全ノードを持たせておき、
			// 実行時に飛び先ノードを特定する。
		}

		// 無条件分岐のノードは、その直前のノードの nextNode を書き変えて、そこから直接飛ぶようにする
		// (VMの命令実行を1サイクル削れて、特にループのオーバーヘッド削減で結構効く)
		for (int instructionIndex = 0; instructionIndex<instructionLength; instructionIndex++) {
			AcceleratorInstruction instruction = instructions[instructionIndex];
			OperationCode opcode = instruction.getOperationCode();

			// 分岐命令以外はスキップ
			if (opcode != OperationCode.JMP && opcode != OperationCode.JMPN) {
				continue;
			}

			// 命令列の最初が分岐の場合はそのまま実行するしかなく、最後に分岐が来るのはEND命令的にあり得ないので、
			// 両者の場合は何もせずスキップ（しないと後で listBeforeNode と listNextNode を参照する場合に対処が必要になる）
			if (instructionIndex == 0 || instructionIndex == instructionLength-1) {
				continue;
			}

			// 分岐条件が定数でなければスキップ
			if (instruction.getOperandPartitions()[2] != Memory.Partition.CONSTANT) {
				continue;
			}

			// 分岐条件の値を取得
			DataContainer<?> branchConditionContainer = memory.getDataContainer(
				instruction.getOperandPartitions()[2], instruction.getOperandAddresses()[2]
			);
			boolean branchCondition = ( (boolean[])branchConditionContainer.getArrayData() )[ branchConditionContainer.getArrayOffset() ];

			AcceleratorExecutionNode listBeforeNode = nodes[instructionIndex - 1];     // 命令列内での順序的に、分岐命令の直前にあるノード
			AcceleratorExecutionNode listNextNode   = nodes[instructionIndex + 1];     // 命令列内での順序的に、分岐命令の直後にあるノード
			AcceleratorExecutionNode branchInstructionNode = nodes[instructionIndex];  // 注目対象の無条件分岐命令ノード
			AcceleratorExecutionNode branchDestinationNode = branchInstructionNode.getLaundingPointNodes()[0]; // 分岐先（飛び先）ノード

			// JMP 命令で常に飛ぶ場合: 直前の命令から、分岐先のノードに直接飛ぶようにする
			if (opcode == OperationCode.JMP && branchCondition == true) {
				listBeforeNode.setNextNode(branchDestinationNode);

			// JMP 命令で常に飛ばない場合: 直前の命令から、分岐命令の下のノードにバイパスするようにする
			} else if (opcode == OperationCode.JMP && branchCondition == false) {
				listBeforeNode.setNextNode(listNextNode);

			// JMPN 命令で常に飛ぶ場合: 直前の命令から、分岐先のノードに直接飛ぶようにする
			} else if (opcode == OperationCode.JMPN && branchCondition == false) {
				listBeforeNode.setNextNode(branchDestinationNode);

			// JMPN 命令で常に飛ばない場合: 直前の命令から、分岐命令の下のノードにバイパスするようにする
			} else if (opcode == OperationCode.JMPN && branchCondition == true) {
				listBeforeNode.setNextNode(listNextNode);

			// 上で何か間違いが無ければここに達する事は無いはず
			} else {
				throw new VnanoFatalException("Unexpected case detected.");
			}

			// ※ 上のコードは、上から下へ流れるフローにおいて branchInstructionNode の実行を省略できるようにしているだけで、
			//    さらに別の場所にある分岐から branchInstructionNode へ飛んでくる場合を全く考えていない雰囲気を醸し出しているが、
			//    以下の理由により、そういう場合への特別な対処は必要ない：
			//
			// ・ 上記コードでは、branchInstructionNode を飛び先とする分岐命令から、branchInstructionNode への参照を削除してはいないので、
			//    そのような分岐命令が実行されると普通に branchInstructionNode に着地し、その作用で正しい場所に飛ぶ。ので動作的に問題ない。
			//    （ただしそういう場合は当然 branchInstructionNode を踏むオーバーヘッドは食う）
			//
			// ・ beforeInListNode が分岐系以外の命令の場合、仮にそれが実行されれば次に branchInstructionNode を踏むのは確実で、
			//    実際のフローが「 beforeInListNode を踏まずに別の所から branchInstructionNode に直接飛んでくる 」ものかどうかに関わらず、
			//    beforeInListNode に上記コードのように次ノードを設定する事自体は正しい（踏んだ後に起こる作用的に等価）なはず。
			//
			// ・ beforeInListNode が分岐系命令の場合、その実行後に branchInstructionNode を踏まずに、どこかに飛んでいく可能性があるが、
			//    そういう命令のノードでも、setNextNode で設定している nextNode は分岐「不成立」時のノードを指すという仕様になっていて、
			//    その結果として全種のノードの nextNode は単純に命令順序的に次にあるノードを指すように統一できているので問題にはならない。
			//    つまり beforeInListNode の分岐「成立」時の飛び先ノードを壊してしまっている事にはならない。
		}

		return nodes;
	}


	// 命令を1つ演算器にディスパッチし、それを実行する演算ノードを返す
	private AcceleratorExecutionNode dispatchToAcceleratorExecutionUnit (
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			ScalarCache[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			BypassUnit bypassUnit, InternalFunctionControlUnit internalFunctionControlUnit,
			ExternalFunctionControlUnit externalFunctionControlUnit, AcceleratorExecutionNode nextNode) {

		// 演算器タイプを取得
		AcceleratorExecutionType accelType = instruction.getAccelerationType();
		switch (accelType) {

			// 算術演算

			case I64V_ARITHMETIC : {
				return new Int64VectorArithmeticUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case I64S_ARITHMETIC : {
				return new Int64ScalarArithmeticUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case I64CS_ARITHMETIC : {
				return new Int64CachedScalarArithmeticUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case I64V_DUAL_ARITHMETIC : {
				return new Int64VectorDualArithmeticUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case I64CS_DUAL_ARITHMETIC : {
				return new Int64CachedScalarDualArithmeticUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}

			case F64V_ARITHMETIC : {
				return new Float64VectorArithmeticUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case F64S_ARITHMETIC : {
				return new Float64ScalarArithmeticUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case F64CS_ARITHMETIC : {
				return new Float64CachedScalarArithmeticUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case F64V_DUAL_ARITHMETIC : {
				return new Float64VectorDualArithmeticUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case F64CS_DUAL_ARITHMETIC : {
				return new Float64CachedScalarDualArithmeticUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}


			// 比較演算

			case I64V_COMPARISON : {
				return new Int64VectorComparisonUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case I64S_COMPARISON : {
				return new Int64ScalarComparisonUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case I64CS_COMPARISON : {
				return new Int64CachedScalarComparisonUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}

			case F64V_COMPARISON : {
				return new Float64VectorComparisonUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case F64S_COMPARISON : {
				return new Float64ScalarComparisonUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case F64CS_COMPARISON : {
				return new Float64CachedScalarComparisonUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}


			// 論理演算

			case BV_LOGICAL : {
				return new BoolVectorLogicalUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case BS_LOGICAL : {
				return new BoolScalarLogicalUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case BCS_LOGICAL : {
				return new BoolCachedScalarLogicalUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}


			// データ転送（配列要素アクセス以外）

			case I64V_TRANSFER : {
				return new Int64VectorTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case I64S_TRANSFER : {
				return new Int64ScalarTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case I64CS_TRANSFER : {
				return new Int64CachedScalarTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case I64VS_TRANSFER : {
				return new Int64VectorScalarTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case I64SV_TRANSFER : {
				return new Int64ScalarVectorTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case I64CS_MULTIPLE_TRANSFER : {
				return new Int64CachedScalarMultipleTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}

			case F64V_TRANSFER : {
				return new Float64VectorTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case F64S_TRANSFER : {
				return new Float64ScalarTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case F64CS_TRANSFER : {
				return new Float64CachedScalarTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case F64VS_TRANSFER : {
				return new Float64VectorScalarTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case F64SV_TRANSFER : {
				return new Float64ScalarVectorTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case F64CS_MULTIPLE_TRANSFER : {
				return new Float64CachedScalarMultipleTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}


			case BV_TRANSFER : {
				return new BoolVectorTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case BS_TRANSFER : {
				return new BoolScalarTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case BCS_TRANSFER : {
				return new BoolCachedScalarTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case BVS_TRANSFER : {
				return new BoolVectorScalarTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case BSV_TRANSFER : {
				return new BoolScalarVectorTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case BCS_MULTIPLE_TRANSFER : {
				return new BoolCachedScalarMultipleTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}



			// 配列要素アクセス

			case I64S_SUBSCRIPT : {
				return new Int64ScalarSubscriptUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case I64CS_SUBSCRIPT : {
				return new Int64CachedScalarSubscriptUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case F64S_SUBSCRIPT : {
				return new Float64ScalarSubscriptUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case F64CS_SUBSCRIPT : {
				return new Float64CachedScalarSubscriptUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case BS_SUBSCRIPT : {
				return new BoolScalarSubscriptUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case BCS_SUBSCRIPT : {
				return new BoolCachedScalarSubscriptUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}


			// 分岐

			case BV_BRANCH : {
				return new BoolVectorBranchUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case BS_BRANCH : {
				return new BoolScalarBranchUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}
			case BCS_BRANCH : {
				return new BoolCachedScalarBranchUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}

			// 何もしない命令（分岐先の着地点などに存在）

			case NOP : {
				return new NopUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}

			// 内部関数関連の命令

			case INTERNAL_FUNCTION_CONTROL : {
				return internalFunctionControlUnit.generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}

			// 外部関数関連の命令

			case EXTERNAL_FUNCTION_CONTROL : {
				return externalFunctionControlUnit.generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}


			// このアクセラレータで未対応の場合（下層のプロセッサにそのまま投げるノードを生成）

			case BYPASS : {
				return bypassUnit.generateNode(
					instruction, operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
				);
			}


			default : {
				// 実装時点では存在しないはずの種類が検出された場合（AcceleratorTypeに要素を追加した場合など）
				throw new VnanoFatalException("Unknown acceleration type detected: " + accelType);
			}
		}
	}



}
