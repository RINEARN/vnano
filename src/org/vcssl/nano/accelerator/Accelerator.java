/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;


import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.memory.DataException;
import org.vcssl.nano.memory.MemoryAccessException;
import org.vcssl.nano.processor.Instruction;
import org.vcssl.nano.processor.InvalidInstructionException;
import org.vcssl.nano.processor.OperationCode;
import org.vcssl.nano.processor.Processor;
import org.vcssl.nano.memory.Memory;


/**
 * <p>
 * <span class="lang-en">
 * The class of accelerator to process some instructions faster
 * at the upper layer of {@link Processor Processor}
 * in in case of that some options are enabled
 * </span>
 * <span class="lang-ja">
 * オプション設定に応じて使用される、
 * 特定の条件が揃った命令を {@link Processor Processor}
 * よりも上流で高速実行するアクセラレータのクラスです。
 * </span>
 * </p>
 *
 * <p>
 * Vnano処理系は、全体的にはパフォーマンスよりも実装の簡素さを優先していますが、
 * このクラスは、それでは処理速度が不足してしまう用途に対応するために存在します。
 * そのため、このクラスの実装コードでは、簡素さや保守性よりも処理速度が最も優先されています。
 * その代わりとして、Vnano処理系は、このクラスを全く使用しなくても、機能上は完全に成立するようにできています。
 * Vnano処理系をカスタマイズする際などには、このクラスは除外しておく方が無難です。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class Accelerator {

	/**
	 * このクラスは定数以外のフィールドを持たないため、コンストラクタは何もしません。
	 */
	public Accelerator() {
	}

	private class AccelerationResource {
		public AccelerationExecutorNode[] accelerationUnits = null;

		public AccelerationDataManager dataManager = null; // [partitionIndex]
	}


	/**
	 * 命令配列に含まれる各命令を逐次実行します。
	 *
	 * 呼び出し側から見た機能としては、{@link ControllUnit#process} メソッドと同様です。
	 * ただし、高速実行の対象外となる命令に対しては、このクラス内では処理を行わず、
	 * そのまま下層の {@link ControllUnit#dispatch} メソッドに投げます。
	 *
	 * @param instructions 実行対象の命令配列
	 * @param memory データの入出力に用いる仮想メモリー
	 * @param processor 高速実行の対象外の命令を処理する仮想プロセッサ
	 * @throws InvalidInstructionException
	 * 		このコントロールユニットが対応していない命令が実行要求された場合や、
	 * 		オペランドの数が期待値と異なる場合など、命令内容が不正である場合に発生します。
	 * @throws MemoryAccessException
	 * 		命令のオペランドに指定された仮想メモリーアドレスが使用領域外であった場合など、
	 * 		不正な仮想メモリーアクセスが生じた場合などに発生します。
	 */
	public void process(Instruction[] instructions, Memory memory, Interconnect interconnect, Processor processor)
					throws MemoryAccessException, InvalidInstructionException {

		AccelerationResource resource = this.generateResource(
				instructions, memory, interconnect, processor
		);

		resource.dataManager.getCacheSynchronizers(Memory.Partition.CONSTANT).writeCache();
		resource.dataManager.getCacheSynchronizers(Memory.Partition.GLOBAL).writeCache();

		AccelerationExecutorNode[] AccelerationUnits = resource.accelerationUnits;

		// 命令の逐次実行ループ
		AccelerationExecutorNode nextNode = AccelerationUnits[0];
		while (nextNode != null) {
			nextNode = nextNode.execute();
		}

	}


	/**
	 * 命令列を事前解釈し、高速実行に必要なリソースを確保した上で、実行用のコプロセッサユニットを一括生成します。
	 *
	 * @param instructions 実行対象の命令配列
	 * @param memory データの入出力に用いる仮想メモリー
	 * @param interconnect 外部変数・外部関数が接続されているインターコネクト
	 * @param processor 高速実行の対象外の命令を処理する仮想プロセッサ
	 * @return 実行用のコプロセッサユニットの配列
	 * @throws MemoryAccessException
	 * 		命令のオペランドに指定された仮想メモリーアドレスが使用領域外であった場合など、
	 * 		不正な仮想メモリーアクセスが生じた場合などに発生します。
	 */
	private AccelerationResource generateResource (Instruction[] instructions, Memory memory,
			Interconnect interconnect, Processor processor)
					throws MemoryAccessException, InvalidInstructionException {

		// アドレスに紐づけてキャッシュを持つ(同じデータコンテナに対して同じキャッシュが一意に対応するように)
		// 非キャッシュ演算ユニットはデータコンテナとキャッシュ要素を保持し、同期する


		// スカラ判定やキャッシュ確保などの高速化用データ解析を実行
		AccelerationDataManager dataManager = new AccelerationDataManager();
		dataManager.allocate(instructions, memory);


		/*
		System.out.println("===== INPUT INSTRUCTIONS =====");
		for (int i=0; i<instructions.length; i++) {
			System.out.println("[" + i + "]\t" + instructions[i]);
			//System.out.println(i + ":\t" + instructions[i]);
		}
		*/


		// 命令スケジューラで命令列を高速化用に再配置・変換
		AccelerationScheduler scheduler = new AccelerationScheduler();
		AcceleratorInstruction[] acceleratorInstructions = scheduler.schedule(instructions, memory, dataManager);


		/*
		System.out.println("===== SCHEDULED INSTRUCTIONS =====");
		for (int i=0; i<acceleratorInstructions.length; i++) {
			AcceleratorInstruction instruction = acceleratorInstructions[i];
			System.out.println("[" + instruction.getReorderedAddress() + "(" + instruction.getUnreorderedAddress() + ")" + "]\t" + instruction);
			//System.out.println(instruction.getReorderedAddress() + ":\t" + instruction);
		}
		*/


		AccelerationDispatcher dispatcher = new AccelerationDispatcher();
		AccelerationExecutorNode[] executors = dispatcher.dispatch(
				processor, memory, interconnect, acceleratorInstructions, dataManager
		);

		AccelerationResource resource = new AccelerationResource();
		resource.accelerationUnits = executors;
		resource.dataManager = dataManager;

		return resource;
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


	private AccelerationExecutorNode generateArithmeticExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		AccelerationExecutorNode executor = null;

		switch (dataTypes[0]) {
			case INT64 : {
				if (isAllVector(operandScalar)) {
					// 全部ベクトルの場合の演算
					executor = new Int64VectorArithmeticUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
					// 全部キャッシュ可能なスカラの場合の演算
					executor = new Int64CachedScalarArithmeticUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				} else {
					// 要素数1の配列をスカラに代入したり、インデックス参照がある場合のスカラ演算など
					executor = new Int64ScalarArithmeticUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				}
				break;
			}

			case FLOAT64 : {
				if (isAllVector(operandScalar)) {
					executor = new Float64VectorArithmeticUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
						executor = new Float64CachedScalarArithmeticUnit().generateExecutor(opcode, dataTypes,
								operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
						);
				} else {
					executor = new Float64ScalarArithmeticUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				}
				break;
			}

			default : {
				break;
			}
		}
		return executor;
	}

	private AccelerationExecutorNode generateComparisonExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		AccelerationExecutorNode executor = null;

		switch (dataTypes[0]) {
			case INT64 : {
				if (isAllVector(operandScalar)) {
					executor = new Int64VectorComparisonUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
					executor = new Int64CachedScalarComparisonUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				} else {
					executor = new Int64ScalarComparisonUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				}
				break;
			}

			case FLOAT64 : {
				if (isAllVector(operandScalar)) {
					executor = new Float64VectorComparisonUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
					executor = new Float64CachedScalarComparisonUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				} else {
					executor = new Float64ScalarComparisonUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				}
				break;
			}

			default : {
				break;
			}
		}
		return executor;
	}

	private AccelerationExecutorNode generateLogicalExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		AccelerationExecutorNode executor = null;

		switch (dataTypes[0]) {
			case BOOL : {
				if (isAllVector(operandScalar)) {
					executor = new BoolVectorLogicalUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
					executor = new BoolCachedScalarLogicalUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				} else {
					executor = new BoolScalarLogicalUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				}
				break;
			}

			default : {
				break;
			}
		}
		return executor;
	}

	private AccelerationExecutorNode generateTransferExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		AccelerationExecutorNode executor = null;

		switch (dataTypes[0]) {
			case INT64 : {
				if (isAllVector(operandScalar)) {
					executor = new Int64VectorTransferUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
					executor = new Int64CachedScalarTransferUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				} else {
					executor = new Int64ScalarTransferUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				}
				break;
			}
			case FLOAT64 : {
				if (isAllVector(operandScalar)) {
					executor = new Float64VectorTransferUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
					executor = new Float64CachedScalarTransferUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				} else {
					executor = new Float64ScalarTransferUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				}
				break;
			}
			case BOOL : {
				if (isAllVector(operandScalar)) {
					executor = new BoolVectorTransferUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
					executor = new BoolCachedScalarTransferUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				} else {
					executor = new BoolScalarTransferUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				}
				break;
			}
			default : {
				break;
			}
		}
		return executor;
	}

	private AccelerationExecutorNode generateBranchExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			int reorderedJumpAddress, AccelerationExecutorNode nextNode) {

		AccelerationExecutorNode executor = null;

		switch (dataTypes[0]) {
			case BOOL : {
				if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
					executor = new BoolCachedScalarBranchUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				// ベクトルの場合はあり得ない(要素数1なら可能？)
				} else {
					executor = new BoolScalarBranchUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant, nextNode
					);
				}
				break;
			}
			default : {
				break;
			}
		}
		return executor;
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
