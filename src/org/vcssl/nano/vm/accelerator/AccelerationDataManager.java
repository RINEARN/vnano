package org.vcssl.nano.vm.accelerator;

import java.util.Arrays;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.processor.Instruction;

// final はファイナライザ攻撃を防ぐため
public final class AccelerationDataManager {

	private static final int REGISTER_PARTITION_ORDINAL = Memory.Partition.REGISTER.ordinal();
	private static final int LOCAL_PARTITION_ORDINAL = Memory.Partition.LOCAL.ordinal();
	private static final int GLOBAL_PARTITION_ORDINAL = Memory.Partition.GLOBAL.ordinal();
	private static final int CONSTANT_PARTITION_ORDINAL = Memory.Partition.CONSTANT.ordinal();
	private static final int STACK_PARTITION_ORDINAL = Memory.Partition.STACK.ordinal();
	private static final int PARTITION_LENGTH = Memory.Partition.values().length;

	// [Partition][Address]
	private ScalarCache[][] caches = null;
	private boolean[][] cached = null;
	private boolean[][] scalar = null;

	// [Partition]
	CacheSynchronizer[] synchronizers;

	private int registerSize = -1;
	private int localSize = -1;
	private int globalSize = -1;
	private int constantSize = -1;
	private int stackSize = -1;

	/*
	public boolean isCachable(Memory.Partition partition, int address) {
		return this.cachable[ partition.ordinal() ][ address ];
	}
	*/

	public boolean isCached(Memory.Partition partition, int address) {
		return this.cached[ partition.ordinal() ][ address ];
	}

	public boolean[] getCachedFlags(Memory.Partition partition) {
		return this.cached[ partition.ordinal() ];
	}

	public ScalarCache getCache(Memory.Partition partition, int address) {
		return this.caches[ partition.ordinal() ][ address ];
	}

	public ScalarCache[] getCaches(Memory.Partition partition) {
		return this.caches[ partition.ordinal() ];
	}

	public boolean isScalar(Memory.Partition partition, int address) {
		return this.scalar[ partition.ordinal() ][ address ];
	}

	public CacheSynchronizer getCacheSynchronizers(Memory.Partition partition) {
		return this.synchronizers[partition.ordinal()];
	}

	public void allocate(Instruction[] instructions, Memory memory) {
		this.initializeFields(memory);
		this.allocateConstantScalarCaches(memory);
		this.allocateVariableScalarCaches(instructions, memory);
		this.initializeCacheSynchronizers(memory);
	}

	private void initializeFields(Memory memory) {

		this.registerSize = memory.getSize(Memory.Partition.REGISTER);
		this.localSize = memory.getSize(Memory.Partition.LOCAL);
		this.globalSize = memory.getSize(Memory.Partition.GLOBAL);
		this.constantSize = memory.getSize(Memory.Partition.CONSTANT);
		this.stackSize = 0;

		// [Partition][Address]
		this.caches = new ScalarCache[PARTITION_LENGTH][];
		this.cached = new boolean[PARTITION_LENGTH][];
		this.scalar = new boolean[PARTITION_LENGTH][];

		for (int partitionIndex=0; partitionIndex<PARTITION_LENGTH; partitionIndex++) {
			if (partitionIndex == REGISTER_PARTITION_ORDINAL) {
				this.scalar[partitionIndex] = new boolean[registerSize];
				this.cached[partitionIndex] = new boolean[registerSize];
				this.caches[partitionIndex] = new ScalarCache[registerSize];
			}
			else if (partitionIndex == LOCAL_PARTITION_ORDINAL) {
				this.scalar[partitionIndex] = new boolean[localSize];
				this.cached[partitionIndex] = new boolean[localSize];
				this.caches[partitionIndex] = new ScalarCache[localSize];
			}
			else if (partitionIndex == GLOBAL_PARTITION_ORDINAL) {
				this.scalar[partitionIndex] = new boolean[globalSize];
				this.cached[partitionIndex] = new boolean[globalSize];
				this.caches[partitionIndex] = new ScalarCache[globalSize];
			}
			else if (partitionIndex == CONSTANT_PARTITION_ORDINAL) {
				this.scalar[partitionIndex] = new boolean[constantSize];
				this.cached[partitionIndex] = new boolean[constantSize];
				this.caches[partitionIndex] = new ScalarCache[constantSize];
			}
			else if (partitionIndex == STACK_PARTITION_ORDINAL) {
				this.scalar[partitionIndex] = new boolean[stackSize];
				this.cached[partitionIndex] = new boolean[stackSize];
				this.caches[partitionIndex] = new ScalarCache[stackSize];
			}
			Arrays.fill(this.scalar[partitionIndex], false);
			Arrays.fill(this.cached[partitionIndex], false);
			Arrays.fill(this.caches[partitionIndex], null);
		}
	}

	private void allocateConstantScalarCaches(Memory memory) {

		// 定数のキャッシュ生成やスカラ判定など（値の書き込みは後でSynchronizerで行う）
		for (int constantIndex=0; constantIndex<constantSize; constantIndex++) {

			DataContainer<?> container = null;
			try {
				container = memory.getDataContainer(Memory.Partition.CONSTANT, constantIndex);

			// 存在するはずの定数アドレスにアクセスしているので、この例外が発生する場合は実装の異常
			} catch (VnanoFatalException e) {
				throw new VnanoFatalException(e);
			}

			this.scalar[CONSTANT_PARTITION_ORDINAL][constantIndex]
					= ( (container.getRank() == DataContainer.RANK_OF_SCALAR) );

			if (!this.scalar[CONSTANT_PARTITION_ORDINAL][constantIndex]) {
				continue;
			}

			DataType dataType = container.getDataType();
			switch (dataType) {
				case INT64 : {
					this.caches[CONSTANT_PARTITION_ORDINAL][constantIndex] = new Int64ScalarCache();
					this.cached[CONSTANT_PARTITION_ORDINAL][constantIndex] = true;
					//this.cachable[CONSTANT_PARTITION_ORDINAL][constantIndex] = true;
					break;
				}
				case FLOAT64 : {
					this.caches[CONSTANT_PARTITION_ORDINAL][constantIndex] = new Float64ScalarCache();
					this.cached[CONSTANT_PARTITION_ORDINAL][constantIndex] = true;
					//this.cachable[CONSTANT_PARTITION_ORDINAL][constantIndex] = true;
					break;
				}
				case BOOL : {
					this.caches[CONSTANT_PARTITION_ORDINAL][constantIndex] = new BoolScalarCache();
					this.cached[CONSTANT_PARTITION_ORDINAL][constantIndex] = true;
					//this.cachable[CONSTANT_PARTITION_ORDINAL][constantIndex] = true;
					break;
				}
				default : {
					break;
				}
			}
		}
	}

	private void allocateVariableScalarCaches(Instruction[] instructions, Memory memory) {

		int instructionLength = instructions.length;

		// アドレスに紐づけてキャッシュを持つ(同じデータコンテナに対して同じキャッシュが一意に対応するように)
		// 非キャッシュ演算ユニットはデータコンテナとキャッシュ要素を保持し、同期する

		// キャッシュ可能かどうか等のメタ情報をスキャンする
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			Instruction instruction = instructions[instructionIndex];
			Memory.Partition[] partitions = instruction.getOperandPartitions();
			int[] addresses = instruction.getOperandAddresses();
			int operandLength = addresses.length;

			switch (instruction.getOperationCode()) {

			case ALLOC : {
					// 1-オペランドのALLOC命令は、0次元なのでスカラ
					if (operandLength == 1) {
						this.scalar[ partitions[0].ordinal() ][ addresses[0] ] = true;

						switch (instruction.getDataTypes()[0]) {
							case INT64 : {
								//System.out.println("CACHE INT64 " + partitions[0] + " / " + addresses[0]);
								this.caches[ partitions[0].ordinal() ][ addresses[0] ] = new Int64ScalarCache();
								this.cached[ partitions[0].ordinal() ][ addresses[0] ] = true;
								break;
							}
							case FLOAT64 : {
								//System.out.println("CACHE FLOAT64 " + partitions[0] + " / " + addresses[0]);
								this.caches[ partitions[0].ordinal() ][ addresses[0] ] = new Float64ScalarCache();
								this.cached[ partitions[0].ordinal() ][ addresses[0] ] = true;
								break;
							}
							case BOOL : {
								//System.out.println("CACHE BOOL " + partitions[0] + " / " + addresses[0]);
								this.caches[ partitions[0].ordinal() ][ addresses[0] ] = new BoolScalarCache();
								this.cached[ partitions[0].ordinal() ][ addresses[0] ] = true;
								break;
							}
							default : break;
						}
					}
					break;
				}

				case ALLOCR : {

					// ALLOCR命令の場合はオペランド [1] と同次元・同要素数でメモリ確保するので、それがスカラであればスカラ
					if (this.scalar[ partitions[1].ordinal() ][ addresses[1] ]) {
						this.scalar[ partitions[0].ordinal() ][ addresses[0] ] = true;

						switch (instruction.getDataTypes()[0]) {
							case INT64 : {
								//System.out.println("CACHE INT64 " + partitions[0] + " / " + addresses[0]);
								this.caches[ partitions[0].ordinal() ][ addresses[0] ] = new Int64ScalarCache();
								this.cached[ partitions[0].ordinal() ][ addresses[0] ] = true;
								break;
							}
							case FLOAT64 : {
								//System.out.println("CACHE FLOAT64 " + partitions[0] + " / " + addresses[0]);
								this.caches[ partitions[0].ordinal() ][ addresses[0] ] = new Float64ScalarCache();
								this.cached[ partitions[0].ordinal() ][ addresses[0] ] = true;
								break;
							}
							case BOOL : {
								//System.out.println("CACHE BOOL " + partitions[0] + " / " + addresses[0]);
								this.caches[ partitions[0].ordinal() ][ addresses[0] ] = new BoolScalarCache();
								this.cached[ partitions[0].ordinal() ][ addresses[0] ] = true;
								break;
							}
							default : break;
						}
					}
					break;
				}

				case ELEM : {
					this.scalar[ partitions[0].ordinal() ][ addresses[0] ] = true;
					break;
				}
				default : {
					break;
				}
			}
		}
	}

	private void initializeCacheSynchronizers(Memory memory) {

		DataContainer<?>[] registerContainers = memory.getDataContainers(Memory.Partition.REGISTER);
		DataContainer<?>[] localContainers = memory.getDataContainers(Memory.Partition.LOCAL);
		DataContainer<?>[] globalContainers = memory.getDataContainers(Memory.Partition.GLOBAL);
		DataContainer<?>[] constantContainers = memory.getDataContainers(Memory.Partition.CONSTANT);

		this.synchronizers = new CacheSynchronizer[PARTITION_LENGTH];

		this.synchronizers[REGISTER_PARTITION_ORDINAL] = new GeneralScalarCacheSynchronizer(
				registerContainers, this.getCaches(Memory.Partition.REGISTER), this.getCachedFlags(Memory.Partition.REGISTER)
		);
		this.synchronizers[LOCAL_PARTITION_ORDINAL] = new GeneralScalarCacheSynchronizer(
				localContainers, this.getCaches(Memory.Partition.LOCAL), this.getCachedFlags(Memory.Partition.LOCAL)
		);
		this.synchronizers[GLOBAL_PARTITION_ORDINAL] = new GeneralScalarCacheSynchronizer(
				globalContainers, this.getCaches(Memory.Partition.GLOBAL), this.getCachedFlags(Memory.Partition.GLOBAL)
		);
		this.synchronizers[CONSTANT_PARTITION_ORDINAL] = new GeneralScalarCacheSynchronizer(
				constantContainers, this.getCaches(Memory.Partition.CONSTANT), this.getCachedFlags(Memory.Partition.CONSTANT)
		);
	}


}
