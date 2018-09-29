/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;


import java.util.Arrays;

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

		public CacheSynchronizer[] synchronizers = null; // [partitionIndex]
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

		// 命令コードの長さ
		int instructionLength = instructions.length;

		AccelerationResource resource = this.generateResource(
				instructions, memory, interconnect, processor
		);

		resource.synchronizers[Memory.Partition.CONSTANT.ordinal()].writeCache();
		resource.synchronizers[Memory.Partition.GLOBAL.ordinal()].writeCache();

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

		int instructionLength = instructions.length;
		AccelerationExecutorNode[] executors = new AccelerationExecutorNode[instructionLength];

		// アドレスに紐づけてキャッシュを持つ(同じデータコンテナに対して同じキャッシュが一意に対応するように)
		// 非キャッシュ演算ユニットはデータコンテナとキャッシュ要素を保持し、同期する

		int registerSize = memory.getSize(Memory.Partition.REGISTER);
		int localSize = memory.getSize(Memory.Partition.LOCAL);
		int globalSize = memory.getSize(Memory.Partition.GLOBAL);
		int constantSize = memory.getSize(Memory.Partition.CONSTANT);

		int registerPartitionOrdinal = Memory.Partition.REGISTER.ordinal();
		int localPartitionOrdinal = Memory.Partition.LOCAL.ordinal();
		int globalPartitionOrdinal = Memory.Partition.GLOBAL.ordinal();
		int constantPartitionOrdinal = Memory.Partition.CONSTANT.ordinal();


		int partitionLength = Memory.Partition.values().length;

		// [Partition][Address]
		Object[][] caches = new Object[partitionLength][];
		boolean[][] cached = new boolean[partitionLength][];
		boolean[][] cachable = new boolean[partitionLength][];
		boolean[][] scalar = new boolean[partitionLength][];

		for (int partitionIndex=0; partitionIndex<partitionLength; partitionIndex++) {
			if (partitionIndex == registerPartitionOrdinal) {
				scalar[partitionIndex] = new boolean[registerSize];
				cachable[partitionIndex] = new boolean[registerSize];
				cached[partitionIndex] = new boolean[registerSize];
				caches[partitionIndex] = new Object[registerSize];
			}
			else if (partitionIndex == localPartitionOrdinal) {
				scalar[partitionIndex] = new boolean[localSize];
				cachable[partitionIndex] = new boolean[localSize];
				cached[partitionIndex] = new boolean[localSize];
				caches[partitionIndex] = new Object[localSize];
			}
			else if (partitionIndex == globalPartitionOrdinal) {
				scalar[partitionIndex] = new boolean[globalSize];
				cachable[partitionIndex] = new boolean[globalSize];
				cached[partitionIndex] = new boolean[globalSize];
				caches[partitionIndex] = new Object[globalSize];
			}
			else if (partitionIndex == constantPartitionOrdinal) {
				scalar[partitionIndex] = new boolean[constantSize];
				cachable[partitionIndex] = new boolean[constantSize];
				cached[partitionIndex] = new boolean[constantSize];
				caches[partitionIndex] = new Object[constantSize];
			}
			else {
				throw new MemoryAccessException(MemoryAccessException.INVALID_PARTITION);
			}
			Arrays.fill(scalar[partitionIndex], false);
			Arrays.fill(cachable[partitionIndex], false);
			Arrays.fill(cached[partitionIndex], false);
			Arrays.fill(caches[partitionIndex], null);
		}


		// キャッシュ可能かどうか等のメタ情報をスキャンする
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			Instruction instruction = instructions[instructionIndex];
			Memory.Partition[] partitions = instruction.getOperandPartitions();
			int[] addresses = instruction.getOperandAddresses();
			int operandLength = addresses.length;

			switch (instruction.getOperationCode()) {
				case ALLOC : {
					if (operandLength == 1) {
						scalar[ partitions[0].ordinal() ][ addresses[0] ] = true;

						switch (instruction.getDataTypes()[0]) {
							case INT64 : {
								//System.out.println("CACHE INT64 " + partitions[0].ordinal() + " / " + addresses[0]);
								caches[ partitions[0].ordinal() ][ addresses[0] ] = new Int64Cache();
								cached[ partitions[0].ordinal() ][ addresses[0] ] = true;
								cachable[ partitions[0].ordinal() ][ addresses[0] ] = true;
								break;
							}
							case FLOAT64 : {
								//System.out.println("CACHE FLOAT64 " + partitions[0].ordinal() + " / " + addresses[0]);
								caches[ partitions[0].ordinal() ][ addresses[0] ] = new Float64Cache();
								cached[ partitions[0].ordinal() ][ addresses[0] ] = true;
								cachable[ partitions[0].ordinal() ][ addresses[0] ] = true;
								break;
							}
							case BOOL : {
								//System.out.println("CACHE BOOL " + partitions[0].ordinal() + " / " + addresses[0]);
								caches[ partitions[0].ordinal() ][ addresses[0] ] = new BoolCache();
								cached[ partitions[0].ordinal() ][ addresses[0] ] = true;
								cachable[ partitions[0].ordinal() ][ addresses[0] ] = true;
								break;
							}
							default : break;
						}
					}
					break;
				}
				case ELEM : {
					scalar[ partitions[0].ordinal() ][ addresses[0] ] = true;
					break;
				}
				default : {
					break;
				}
			}
		}


		// 定数のキャッシュ生成やスカラ判定など（値の書き込みは後でSynchronizerで行う）
		for (int constantIndex=0; constantIndex<constantSize; constantIndex++) {

			DataContainer<?> container = memory.getDataContainer(Memory.Partition.CONSTANT, constantIndex);
			scalar[constantPartitionOrdinal][constantIndex]
					= ( (container.getRank() == DataContainer.RANK_OF_SCALAR) );

			if (!scalar[constantPartitionOrdinal][constantIndex]) {
				continue;
			}

			DataType dataType = container.getDataType();
			switch (dataType) {
				case INT64 : {
					caches[constantPartitionOrdinal][constantIndex] = new Int64Cache();
					cached[constantPartitionOrdinal][constantIndex] = true;
					cachable[constantPartitionOrdinal][constantIndex] = true;
					break;
				}
				case FLOAT64 : {
					caches[constantPartitionOrdinal][constantIndex] = new Float64Cache();
					cached[constantPartitionOrdinal][constantIndex] = true;
					cachable[constantPartitionOrdinal][constantIndex] = true;
					break;
				}
				case BOOL : {
					caches[constantPartitionOrdinal][constantIndex] = new BoolCache();
					cached[constantPartitionOrdinal][constantIndex] = true;
					cachable[constantPartitionOrdinal][constantIndex] = true;
					break;
				}
				default : {
					break;
				}
			}
		}


		// 命令列から演算ノード列を生成
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {

			Instruction instruction = instructions[instructionIndex];
			DataType[] dataTypes = instruction.getDataTypes();
			OperationCode opcode = instruction.getOperationCode();

			// 命令からデータアドレスを取得
			Memory.Partition[] partitions = instructions[instructionIndex].getOperandPartitions();
			int[] addresses = instructions[instructionIndex].getOperandAddresses();
			int operandLength = addresses.length;

			DataContainer<?>[] containers = new DataContainer[operandLength];
			for (int operandIndex=0; operandIndex<operandLength; operandIndex++) {
				containers[operandIndex] = memory.getDataContainer(partitions[operandIndex], addresses[operandIndex]);
			}

			boolean[] operandConstant = new boolean[operandLength];
			boolean[] operandScalar = new boolean[operandLength];
			boolean[] operandCached = new boolean[operandLength];
			Object[] operandCaches = new Object[operandLength];
			for (int operandIndex=0; operandIndex<operandLength; operandIndex++) {
				operandScalar[operandIndex] = scalar[ partitions[operandIndex].ordinal() ][ addresses[operandIndex] ];
				operandCached[operandIndex] = cached[ partitions[operandIndex].ordinal() ][ addresses[operandIndex] ];

				if (operandCached[operandIndex]) {
					operandCaches[operandIndex] = caches[ partitions[operandIndex].ordinal() ][ addresses[operandIndex] ];
				}
				if (partitions[operandIndex] == Memory.Partition.CONSTANT) {
					operandConstant[operandIndex] = true;
				}
			}

			if (opcode == OperationCode.ALLOC && scalar[partitions[0].ordinal()][addresses[0]] ) {

				CacheSynchronizer synchronizer = new GeneralCacheSynchronizer(containers, operandCaches, operandCached);

				executors[instructionIndex] = new ScalarAllocExecutor(
					instructions[instructionIndex], memory, interconnect, processor, synchronizer
				);

				continue;
			}



			switch (opcode) {

				// 算術演算命令 Arithmetic instruction opcodes
				case ADD :
				case SUB :
				case MUL :
				case DIV :
				case REM :
				{
					executors[instructionIndex] = this.generateArithmeticExecutor(opcode, dataTypes,
							containers, operandCaches, operandCached, operandScalar, operandConstant);
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
					executors[instructionIndex] = this.generateComparisonExecutor(opcode, dataTypes,
							containers, operandCaches, operandCached, operandScalar, operandConstant);
					break;
				}

				// 論理演算命令 Logical instruction opcodes
				case AND :
				case OR :
				case NOT :
				{
					executors[instructionIndex] = this.generateLogicalExecutor(opcode, dataTypes,
							containers, operandCaches, operandCached, operandScalar, operandConstant);
					break;
				}

				// 転送命令 Trsndfer instruction opcodes
				case MOV :
				case CAST :
				case FILL :
				{
					executors[instructionIndex] = this.generateTransferExecutor(opcode, dataTypes,
							containers, operandCaches, operandCached, operandScalar, operandConstant);
					break;
				}

				// 分岐命令 Branch instruction opcodes
				case JMP :
				case JMPN :
				{
					executors[instructionIndex] = this.generateBranchExecutor(opcode, dataTypes,
							containers, operandCaches, operandCached, operandScalar, operandConstant);
					break;
				}

				// 非対応命令 Un-acceleratable Opcodes
				default : {
					break;
				}

			} // Arithmetic Operations

			if (executors[instructionIndex] == null) {

				CacheSynchronizer synchronizer = new GeneralCacheSynchronizer(
					containers, operandCaches, operandCached
				);
				executors[instructionIndex] = new PassThroughExecutor(
					instructions[instructionIndex], memory, interconnect, processor, synchronizer
				);
			}

			//System.out.println("[" + instructionIndex + "] " + opcode + " \t" + executors[instructionIndex].getClass().getName().split("\\$")[1]);
		}

		// 演算ノード列の各要素に対し、次のノード（及び必要に分岐先ノード）の参照を設定し、参照リスト的に結合
		for (int instructionIndex=0; instructionIndex<instructionLength-1; instructionIndex++) {

			executors[instructionIndex].setNextNode( executors[instructionIndex+1] );

			OperationCode opcode = instructions[instructionIndex].getOperationCode();
			if (opcode == OperationCode.JMP || opcode == OperationCode.JMPN) {

				DataContainer<?> branchedAddressContainer = memory.getDataContainer(
						Memory.Partition.CONSTANT,
						instructions[instructionIndex].getOperandAddresses()[1]
				);

				long[] branchedAddressContainerData = (long[])branchedAddressContainer.getData();
				int branchedAddress = (int)branchedAddressContainerData[0];
				executors[instructionIndex].setBranchedNode( executors[branchedAddress] );
			}
		}

		DataContainer<?>[] registerContainers = memory.getDataContainers(Memory.Partition.REGISTER);
		DataContainer<?>[] localContainers = memory.getDataContainers(Memory.Partition.LOCAL);
		DataContainer<?>[] globalContainers = memory.getDataContainers(Memory.Partition.GLOBAL);
		DataContainer<?>[] constantContainers = memory.getDataContainers(Memory.Partition.CONSTANT);


		CacheSynchronizer[] synchronizers = new CacheSynchronizer[partitionLength];
		synchronizers[registerPartitionOrdinal] = new GeneralCacheSynchronizer(
				registerContainers, caches[registerPartitionOrdinal], cached[registerPartitionOrdinal]
		);
		synchronizers[localPartitionOrdinal] = new GeneralCacheSynchronizer(
				localContainers, caches[localPartitionOrdinal], cached[localPartitionOrdinal]
		);
		synchronizers[globalPartitionOrdinal] = new GeneralCacheSynchronizer(
				globalContainers, caches[globalPartitionOrdinal], cached[globalPartitionOrdinal]
		);
		synchronizers[constantPartitionOrdinal] = new GeneralCacheSynchronizer(
				constantContainers, caches[constantPartitionOrdinal], cached[constantPartitionOrdinal]
		);


		AccelerationResource resource = new AccelerationResource();
		resource.accelerationUnits = executors;
		resource.synchronizers = synchronizers;

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
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant) {

		AccelerationExecutorNode executor = null;

		switch (dataTypes[0]) {
			case INT64 : {
				if (isAllVector(operandScalar)) {
					// 全部ベクトルの場合の演算
					executor = new Int64VectorArithmeticUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
				} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
					// 全部キャッシュ可能なスカラの場合の演算
					executor = new Int64CachedScalarArithmeticUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
				} else {
					// 要素数1の配列をスカラに代入したり、インデックス参照がある場合のスカラ演算など
					executor = new Int64ScalarArithmeticUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
				}
				break;
			}

			case FLOAT64 : {
				if (isAllVector(operandScalar)) {
					executor = new Float64VectorArithmeticUnit().generateExecutor(
							opcode, dataTypes, operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
				} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
						executor = new Float64CachedScalarArithmeticUnit().generateExecutor(
							opcode, dataTypes, operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
				} else {
					executor = new Float64ScalarArithmeticUnit().generateExecutor(
							opcode, dataTypes, operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
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
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant) {

		AccelerationExecutorNode executor = null;

		switch (dataTypes[0]) {
			case INT64 : {
				if (isAllVector(operandScalar)) {
					executor = new Int64VectorComparisonUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
				} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
					executor = new Int64CachedScalarComparisonUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
				} else {
					executor = new Int64ScalarComparisonUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
				}
				break;
			}

			case FLOAT64 : {
				if (isAllVector(operandScalar)) {
					executor = new Float64VectorComparisonUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
				} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
					executor = new Float64CachedScalarComparisonUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
				} else {
					executor = new Float64ScalarComparisonUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
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
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant) {

		AccelerationExecutorNode executor = null;

		switch (dataTypes[0]) {
			case BOOL : {
				if (isAllVector(operandScalar)) {
					executor = new BoolVectorLogicalUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
				} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
					executor = new BoolCachedScalarLogicalUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
				} else {
					executor = new BoolScalarLogicalUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
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
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant) {

		AccelerationExecutorNode executor = null;

		switch (dataTypes[0]) {
			case INT64 : {
				if (isAllVector(operandScalar)) {
					executor = new Int64VectorTransferUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
				} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
					executor = new Int64CachedScalarTransferUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
				} else {
					executor = new Int64ScalarTransferUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
				}
				break;
			}
			case FLOAT64 : {
				if (isAllVector(operandScalar)) {
					executor = new Float64VectorTransferUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
				} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
					executor = new Float64CachedScalarTransferUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
				} else {
					executor = new Float64ScalarTransferUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
				}
				break;
			}
			case BOOL : {
				if (isAllVector(operandScalar)) {
					executor = new BoolVectorTransferUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
				} else if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
					executor = new BoolCachedScalarTransferUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
				} else {
					executor = new BoolScalarTransferUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
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
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant) {

		AccelerationExecutorNode executor = null;

		switch (dataTypes[0]) {
			case BOOL : {
				if (isAllScalar(operandScalar) && isAllCached(operandCached)) {
					executor = new BoolCachedScalarBranchUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
				// ベクトルの場合はあり得ない(要素数1なら可能？)
				} else {
					executor = new BoolScalarBranchUnit().generateExecutor(opcode, dataTypes,
							operandContainers, operandCaches, operandCached, operandScalar, operandConstant);
				}
				break;
			}
			default : {
				break;
			}
		}
		return executor;
	}












	private final class ScalarAllocExecutor extends AccelerationExecutorNode {
		private final Instruction instruction;
		private final Interconnect interconnect;
		private final Processor processor;
		private final Memory memory;
		private final CacheSynchronizer synchronizer;
		private boolean allocated = false;

		public ScalarAllocExecutor(Instruction instruction, Memory memory, Interconnect interconnect,
				Processor processor, CacheSynchronizer synchronizer) {
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
				Processor processor, CacheSynchronizer synchronizer) {
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
