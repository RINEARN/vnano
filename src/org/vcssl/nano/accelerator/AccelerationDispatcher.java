package org.vcssl.nano.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.memory.DataException;
import org.vcssl.nano.memory.Memory;
import org.vcssl.nano.memory.MemoryAccessException;
import org.vcssl.nano.processor.Instruction;
import org.vcssl.nano.processor.InvalidInstructionException;
import org.vcssl.nano.processor.OperationCode;
import org.vcssl.nano.processor.Processor;

public class AccelerationDispatcher {


	// 命令列の内容を全て演算器に割り当て、演算を実行するための演算ノード（演算器内部に実装）の列を返す
	public AccelerationExecutorNode[] dispatch (
			Processor processor, Memory memory, Interconnect interconnect,
			AcceleratorInstruction[] instructions, AccelerationDataManager dataManager) {


		// いきなり Executor を生成するのではなく、
		// まず拡張命令の配列を生成し、それを読んでExecutorを生成するように2段階に分ける。
		// そうしないと、間にリオーダーやオペランド入れ替え最適化などを行えない。
		// FMA演算を入れる上でも必要
		// スカラALLOCを移す上でもスカラALLOCである事が判定済みである必要があるので、まず最初の一歩は拡張命令変換


		int instructionLength = instructions.length;
		AccelerationExecutorNode[] executors = new AccelerationExecutorNode[instructionLength];


		// 命令列から演算ノード列を生成（ループは命令列の末尾から先頭の順で辿る）
		AccelerationExecutorNode nextNode = null; // 現在の対象命令の次の命令（＝前ループでの対象命令）を控える
		for (int instructionIndex = instructionLength-1; 0<=instructionIndex; instructionIndex--) {
		//for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {

			AcceleratorInstruction instruction = instructions[instructionIndex];
			DataType[] dataTypes = instruction.getDataTypes();
			OperationCode opcode = instruction.getOperationCode();


			// 命令からデータアドレスを取得
			Memory.Partition[] partitions = instructions[instructionIndex].getOperandPartitions();
			int[] addresses = instructions[instructionIndex].getOperandAddresses();
			int operandLength = addresses.length;

			DataContainer<?>[] operandContainers = new DataContainer[operandLength];
			for (int operandIndex=0; operandIndex<operandLength; operandIndex++) {
				try {
					operandContainers[operandIndex] = memory.getDataContainer(partitions[operandIndex], addresses[operandIndex]);
				} catch (MemoryAccessException e) {
					// 命令が指しているデータアドレスにアクセスできないのはアセンブラかメモリ初期化の異常
					throw new VnanoFatalException(e);
				}
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

			// 演算器タイプを取得
			AccelerationType accelType = instruction.getAccelerationType();

			// 対象命令（1個）を演算器にディスパッチして演算ノードを取得
			AccelerationExecutorNode currentNode = this.dispatchToAccelerationUnit(
					accelType, opcode, dataTypes,
					operandContainers, operandCaches, operandCached, operandScalar, operandConstant,
					instruction, processor, memory, interconnect,
					nextNode
			);


			// 生成したノードをノード列に格納
			executors[instructionIndex] = currentNode;

			// 次ループ（命令末尾から先頭へ辿る）内で仕様するため、現在のノードを（アドレス的に）次のノードとして控える
			nextNode = currentNode;
		}

		// 分岐ノードに分岐先ノードの参照を設定
		for (int instructionIndex = instructionLength-1; 0<=instructionIndex; instructionIndex--) {

			OperationCode opcode = instructions[instructionIndex].getOperationCode();
			if (opcode == OperationCode.JMP || opcode == OperationCode.JMPN) {

				// 命令再配置でジャンプ命令先アドレスは変化するので、補正後のアドレスを使う
				int branchedAddress = instructions[instructionIndex].getReorderedJumpAddress();
				executors[instructionIndex].setBranchedNode( executors[branchedAddress] );
			}
		}


		return executors;
	}

	// 命令を1つ演算器にディスパッチし、それを実行する演算ノードを返す
	private AccelerationExecutorNode dispatchToAccelerationUnit (
			AccelerationType accelType,
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorInstruction instruction, Processor processor, Memory memory, Interconnect interconnect,
			AccelerationExecutorNode nextNode) {

		AccelerationExecutorNode currentNode = null;
		switch (accelType) {

			// 算術演算

			case I64V_ARITHMETIC : {
				currentNode = new Int64VectorArithmeticUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}
			case I64S_ARITHMETIC : {
				currentNode = new Int64ScalarArithmeticUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}
			case I64CS_ARITHMETIC : {
				currentNode = new Int64CachedScalarArithmeticUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}
			case I64CS_DUAL_ARITHMETIC : {
				currentNode = new Int64CachedScalarDualArithmeticUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}

			case F64V_ARITHMETIC : {
				currentNode = new Float64VectorArithmeticUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}
			case F64S_ARITHMETIC : {
				currentNode = new Float64ScalarArithmeticUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}
			case F64CS_ARITHMETIC : {
				currentNode = new Float64CachedScalarArithmeticUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}
			case F64CS_DUAL_ARITHMETIC : {
				currentNode = new Float64CachedScalarDualArithmeticUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}


			// 比較演算

			case I64V_COMPARISON : {
				currentNode = new Int64VectorComparisonUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}
			case I64S_COMPARISON : {
				currentNode = new Int64ScalarComparisonUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}
			case I64CS_COMPARISON : {
				currentNode = new Int64CachedScalarComparisonUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}

			case F64V_COMPARISON : {
				currentNode = new Float64VectorComparisonUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}
			case F64S_COMPARISON : {
				currentNode = new Float64ScalarComparisonUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}
			case F64CS_COMPARISON : {
				currentNode = new Float64CachedScalarComparisonUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}


			// 論理演算

			case BV_LOGICAL : {
				currentNode = new BoolVectorLogicalUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}
			case BS_LOGICAL : {
				currentNode = new BoolScalarLogicalUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}
			case BCS_LOGICAL : {
				currentNode = new BoolCachedScalarLogicalUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}


				// データ転送

			case I64V_TRANSFER : {
				currentNode = new Int64VectorTransferUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}
			case I64S_TRANSFER : {
				currentNode = new Int64ScalarTransferUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}
			case I64CS_TRANSFER : {
				currentNode = new Int64CachedScalarTransferUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}

			case F64V_TRANSFER : {
				currentNode = new Float64VectorTransferUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}
			case F64S_TRANSFER : {
				currentNode = new Float64ScalarTransferUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}
			case F64CS_TRANSFER : {
				currentNode = new Float64CachedScalarTransferUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}

			case BV_TRANSFER : {
				currentNode = new BoolVectorTransferUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}
			case BS_TRANSFER : {
				currentNode = new BoolScalarTransferUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}
			case BCS_TRANSFER : {
				currentNode = new BoolCachedScalarTransferUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}



			// 分岐

			case BS_BRANCH : {
				currentNode = new BoolScalarBranchUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}
			case BCS_BRANCH : {
				currentNode = new BoolCachedScalarBranchUnit().generateExecutorNode(
					instruction, operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
				);
				break;
			}



			// スカラALLOC

			case S_ALLOC : {
				CacheSynchronizer synchronizer = new GeneralScalarCacheSynchronizer(
						operandContainers, operandCaches, operandCached
				);
				currentNode = new ScalarAllocExecutor(
						instruction, memory, interconnect, processor, synchronizer, nextNode
				);
				break;
			}



			// NOP（分岐先の着地点に存在）

			case NOP : {
				currentNode = new NopExecutor(nextNode);
				break;
			}



			// このアクセラレータで未対応の場合（下層のプロセッサにそのまま投げるノードを生成）

			case Unsupported : {
				CacheSynchronizer synchronizer = new GeneralScalarCacheSynchronizer(
						operandContainers, operandCaches, operandCached
				);
				currentNode = new PassThroughExecutor(
						instruction, memory, interconnect, processor, synchronizer, nextNode
				);
				break;
			}


			default : {
				// 実装時点では存在しないはずの種類が検出された場合（AcceleratorTypeに要素を追加した場合など）
				throw new VnanoFatalException("Unknown acceleration type detected: " + accelType);
			}
		}

		return currentNode;
	}





	// 以下は後で別の所属に移す

	private final class NopExecutor extends AccelerationExecutorNode {

		public NopExecutor(AccelerationExecutorNode nextNode) {
			super(nextNode);
		}

		public final AccelerationExecutorNode execute() {
			return this.nextNode;
		}
	}



	private final class ScalarAllocExecutor extends AccelerationExecutorNode {
		private final Instruction instruction;
		private final Interconnect interconnect;
		private final Processor processor;
		private final Memory memory;
		private final CacheSynchronizer synchronizer;
		private boolean allocated = false;

		public ScalarAllocExecutor(Instruction instruction, Memory memory, Interconnect interconnect,
				Processor processor, CacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.instruction = instruction;
			this.interconnect = interconnect;
			this.processor = processor;
			this.memory = memory;
			this.synchronizer = synchronizer;
		}

		public final AccelerationExecutorNode execute() {
			if (this.allocated) {
				return this.nextNode;
			} else {
				try {
					int programCounter = 0; // この命令はプログラムカウンタの値に依存しないため、便宜的に 0 を指定
					this.processor.process(this.instruction, this.memory, this.interconnect, programCounter);
					this.synchronizer.writeCache();
					this.allocated = true;
					return this.nextNode;
				} catch (DataException | InvalidInstructionException | MemoryAccessException e) {
					e.printStackTrace();
					return null;
				}
			}
		}
	}


	private final class PassThroughExecutor extends AccelerationExecutorNode {
		private final Instruction instruction;
		private final Interconnect interconnect;
		private final Processor processor;
		private final Memory memory;
		private final CacheSynchronizer synchronizer;

		public PassThroughExecutor(Instruction instruction, Memory memory, Interconnect interconnect,
				Processor processor, CacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.instruction = instruction;
			this.interconnect = interconnect;
			this.processor = processor;
			this.memory = memory;
			this.synchronizer = synchronizer;
		}

		public final AccelerationExecutorNode execute() {
			try {

				this.synchronizer.readCache();
				int programCounter = 0; // 暫定的なダミー値
				this.processor.process(this.instruction, this.memory, this.interconnect, programCounter);
				this.synchronizer.writeCache();
				return this.nextNode;

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

}
