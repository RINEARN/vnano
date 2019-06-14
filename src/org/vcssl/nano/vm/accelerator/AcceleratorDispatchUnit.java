/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;

import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.processor.Processor;

public class AcceleratorDispatchUnit {


	// 命令列の内容を全て演算器に割り当て、演算を実行するための演算ノード（演算器内部に実装）の列を返す
	public AcceleratorExecutionNode[] dispatch (
			Processor processor, Memory memory, Interconnect interconnect,
			AcceleratorInstruction[] instructions, AcceleratorDataManagementUnit dataManager,
			BypassUnit bypassUnit, InternalFunctionControlUnit functionControlUnit) {

		int instructionLength = instructions.length;
		AcceleratorExecutionNode[] nodes = new AcceleratorExecutionNode[instructionLength];


		// 命令列から演算ノード列を生成（ノードのコンストラクタで次ノードを指定するため、ループは命令列末尾から先頭へ辿る）
		AcceleratorExecutionNode nextNode = null; // 現在の対象命令の次の命令（＝前ループでの対象命令）を控える
		for (int instructionIndex = instructionLength-1; 0<=instructionIndex; instructionIndex--) {
		//for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {

			AcceleratorInstruction instruction = instructions[instructionIndex];

			// 命令からオペランドのデータアドレスを取得
			Memory.Partition[] partitions = instructions[instructionIndex].getOperandPartitions();
			int[] addresses = instructions[instructionIndex].getOperandAddresses();
			int operandLength = addresses.length;

			// アドレスからオペランドのデータコンテナを取得
			DataContainer<?>[] operandContainers = new DataContainer[operandLength];
			for (int operandIndex=0; operandIndex<operandLength; operandIndex++) {
				try {
					operandContainers[operandIndex] = memory.getDataContainer(partitions[operandIndex], addresses[operandIndex]);
				} catch (VnanoFatalException e) {
					// 命令が指しているデータアドレスにアクセスできないのはアセンブラかメモリ初期化の異常
					throw new VnanoFatalException(e);
				}
			}

			// オペランドの状態とキャッシュ参照などを控える配列を用意
			boolean[] operandConstant = new boolean[operandLength];
			boolean[] operandScalar = new boolean[operandLength];
			boolean[] operandCached = new boolean[operandLength];
			ScalarCache[] operandCaches = new ScalarCache[operandLength];

			// データマネージャから、オペランドのスカラ判定結果、キャッシュ有無、キャッシュ参照、定数かどうかの状態を控える
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


			// 対象命令（1個）を演算器にディスパッチして演算ノードを取得
			AcceleratorExecutionNode currentNode = null;
			try {
				currentNode = this.dispatchToAcceleratorExecutionUnit(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant,
					bypassUnit, functionControlUnit,
					nextNode
				);
			} catch (Exception causeException) {
				AcceleratorInstruction causeInstruction = instruction;
				int unreorderedAddress = causeInstruction.getUnreorderedAddress();
				int reorderedAddress = causeInstruction.getReorderedAddress();
				throw new VnanoFatalException(
						"Accelerator dispatch failed at:"
						+ " address=" + unreorderedAddress
						+ " reorderedAddressOfThisInstruction=" + reorderedAddress
						+ " instruction=" + causeInstruction,
						causeException
				);
			}

			// エラー発生時に原因命令を辿れるように、ノードに元の命令を格納
			currentNode.setSourceInstruction(instruction);

			// 生成したノードをノード列に格納
			nodes[instructionIndex] = currentNode;

			// 次ループ（命令末尾から先頭へ辿る）内で仕様するため、現在のノードを（アドレス的に）次のノードとして控える
			nextNode = currentNode;
		}

		// 別の命令アドレスに飛ぶ処理のノードに、着地先ノードの参照を持たせる
		for (int instructionIndex = instructionLength-1; 0<=instructionIndex; instructionIndex--) {
			OperationCode opcode = instructions[instructionIndex].getOperationCode();

			// 分岐命令と内部関数コール命令: 飛び先の命令アドレスは静的に確定しているので、着地先ノードを求めて持たせる
			if (opcode == OperationCode.JMP || opcode == OperationCode.JMPN || opcode == OperationCode.CALL) {
				int branchedAddress = instructions[instructionIndex].getReorderedLabelAddress(); // 注：命令再配置で飛び先アドレスは変わる
				nodes[instructionIndex].setLaundingPointNodes(nodes[branchedAddress]);
			}

			// 関数のRET命令などは、スタックに積まれた値を読んでその命令アドレスに飛ぶので、
			// 実行時まで着地先がわからないため、全ノードを持たせておく必要がある。
			// そのため、それらの命令を管理する InternalFunctionControlUnit に全ノードを持たせておき、
			// 実行時に飛び先ノードを特定する。
		}

		return nodes;
	}

	// 命令を1つ演算器にディスパッチし、それを実行する演算ノードを返す
	private AcceleratorExecutionNode dispatchToAcceleratorExecutionUnit (
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			ScalarCache[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			BypassUnit bypassUnit, InternalFunctionControlUnit functionControlUnit,
			AcceleratorExecutionNode nextNode) {

		// 演算器タイプを取得
		AcceleratorExecutionType accelType = instruction.getAccelerationType();
		switch (accelType) {

			// 算術演算

			case I64V_ARITHMETIC : {
				return new Int64VectorArithmeticUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}
			case I64S_ARITHMETIC : {
				return new Int64ScalarArithmeticUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}
			case I64CS_ARITHMETIC : {
				return new Int64CachedScalarArithmeticUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}
			case I64V_DUAL_ARITHMETIC : {
				return new Int64VectorDualArithmeticUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}
			case I64CS_DUAL_ARITHMETIC : {
				return new Int64CachedScalarDualArithmeticUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}

			case F64V_ARITHMETIC : {
				return new Float64VectorArithmeticUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}
			case F64S_ARITHMETIC : {
				return new Float64ScalarArithmeticUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}
			case F64CS_ARITHMETIC : {
				return new Float64CachedScalarArithmeticUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}
			case F64V_DUAL_ARITHMETIC : {
				return new Float64VectorDualArithmeticUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}
			case F64CS_DUAL_ARITHMETIC : {
				return new Float64CachedScalarDualArithmeticUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}


			// 比較演算

			case I64V_COMPARISON : {
				return new Int64VectorComparisonUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}
			case I64S_COMPARISON : {
				return new Int64ScalarComparisonUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}
			case I64CS_COMPARISON : {
				return new Int64CachedScalarComparisonUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}

			case F64V_COMPARISON : {
				return new Float64VectorComparisonUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}
			case F64S_COMPARISON : {
				return new Float64ScalarComparisonUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}
			case F64CS_COMPARISON : {
				return new Float64CachedScalarComparisonUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}


			// 論理演算

			case BV_LOGICAL : {
				return new BoolVectorLogicalUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}
			case BS_LOGICAL : {
				return new BoolScalarLogicalUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}
			case BCS_LOGICAL : {
				return new BoolCachedScalarLogicalUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}


				// データ転送

			case I64V_TRANSFER : {
				return new Int64VectorTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}
			case I64S_TRANSFER : {
				return new Int64ScalarTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}
			case I64CS_TRANSFER : {
				return new Int64CachedScalarTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}

			case F64V_TRANSFER : {
				return new Float64VectorTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}
			case F64S_TRANSFER : {
				return new Float64ScalarTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}
			case F64CS_TRANSFER : {
				return new Float64CachedScalarTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}

			case BV_TRANSFER : {
				return new BoolVectorTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}
			case BS_TRANSFER : {
				return new BoolScalarTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}
			case BCS_TRANSFER : {
				return new BoolCachedScalarTransferUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}



			// 分岐

			case BS_BRANCH : {
				return new BoolScalarBranchUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}
			case BCS_BRANCH : {
				return new BoolCachedScalarBranchUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}

			// NOP（分岐先の着地点に存在）

			case NOP : {
				return new NopUnit().generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}

			// 内部関数関連の命令

			case FUNCTION_CONTROL : {
				return functionControlUnit.generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}


			// このアクセラレータで未対応の場合（下層のプロセッサにそのまま投げるノードを生成）

			case BYPASS : {
				return bypassUnit.generateNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
			}


			default : {
				// 実装時点では存在しないはずの種類が検出された場合（AcceleratorTypeに要素を追加した場合など）
				throw new VnanoFatalException("Unknown acceleration type detected: " + accelType);
			}
		}
	}






	/*
	// スカラのALLOC命令は、スケジューリングでコード先頭に移動させて最初に行うようにしたため、複数回実行のための高速化はもう不要？
	private final class ScalarAllocExecutorNode extends AcceleratorExecutionNode {
		private final DataType dataType;
		private final DataContainer<?> allocTargetContainer;
		private boolean allocated = false;
		private CacheSynchronizer synchronizer;
		private ExecutionUnit executionUnit;

		public ScalarAllocExecutorNode(Instruction instruction, DataContainer<?> target, CacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.dataType = instruction.getDataTypes()[0];
			this.allocTargetContainer = target;
			this.synchronizer = synchronizer;
			this.executionUnit = new ExecutionUnit();
		}

		public final AcceleratorExecutionNode execute() {

			if (this.allocated) {
				return this.nextNode;

			} else {

				this.executionUnit.allocScalar(this.dataType, this.allocTargetContainer);
				this.synchronizer.synchronizeFromCacheToMemory(); // 確保したメモリにキャッシュ値を書き込んでおく
				this.allocated = true;
				return this.nextNode;
			}
		}
	}
	*/



}