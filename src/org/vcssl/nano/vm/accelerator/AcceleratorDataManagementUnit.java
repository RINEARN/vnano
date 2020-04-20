/*
 * Copyright(C) 2018-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import java.util.Arrays;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.processor.Instruction;

// final はファイナライザ攻撃を防ぐため
public final class AcceleratorDataManagementUnit {

	private static final int REGISTER_PARTITION_ORDINAL = Memory.Partition.REGISTER.ordinal();
	private static final int LOCAL_PARTITION_ORDINAL = Memory.Partition.LOCAL.ordinal();
	private static final int GLOBAL_PARTITION_ORDINAL = Memory.Partition.GLOBAL.ordinal();
	private static final int CONSTANT_PARTITION_ORDINAL = Memory.Partition.CONSTANT.ordinal();
	private static final int STACK_PARTITION_ORDINAL = Memory.Partition.STACK.ordinal();
	private static final int NONE_PARTITION_ORDINAL = Memory.Partition.NONE.ordinal();
	private static final int PARTITION_LENGTH = Memory.Partition.values().length;

	// [Partition][Address]
	private ScalarCache[][] caches = null;
	private boolean[][] cachingEnabled = null;
	private boolean[][] scalar = null;

	// [Partition]
	CacheSynchronizer[] synchronizers;

	private int registerSize = -1;
	private int localSize = -1;
	private int globalSize = -1;
	private int constantSize = -1;
	private int stackSize = -1;
	private int noneSize = -1;

	public boolean isCachingEnabled(Memory.Partition partition, int address) {
		return this.cachingEnabled[ partition.ordinal() ][ address ];
	}

	public boolean[] getCachedFlags(Memory.Partition partition) {
		return this.cachingEnabled[ partition.ordinal() ];
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
		this.detectScalarFromMemory(memory, Memory.Partition.CONSTANT);
		this.detectScalarFromMemory(memory, Memory.Partition.GLOBAL);
		this.detectScalarFromInstructions(instructions, memory);
		this.initializeCacheSynchronizers(memory);
	}

	private void initializeFields(Memory memory) {

		this.registerSize = memory.getSize(Memory.Partition.REGISTER);
		this.localSize = memory.getSize(Memory.Partition.LOCAL);
		this.globalSize = memory.getSize(Memory.Partition.GLOBAL);
		this.constantSize = memory.getSize(Memory.Partition.CONSTANT);
		this.stackSize = 0;
		this.noneSize = 1;

		// [Partition][Address]
		this.caches = new ScalarCache[PARTITION_LENGTH][];
		this.cachingEnabled = new boolean[PARTITION_LENGTH][];
		this.scalar = new boolean[PARTITION_LENGTH][];

		this.scalar[REGISTER_PARTITION_ORDINAL] = new boolean[registerSize];
		this.cachingEnabled[REGISTER_PARTITION_ORDINAL] = new boolean[registerSize];
		this.caches[REGISTER_PARTITION_ORDINAL] = new ScalarCache[registerSize];

		this.scalar[LOCAL_PARTITION_ORDINAL] = new boolean[localSize];
		this.cachingEnabled[LOCAL_PARTITION_ORDINAL] = new boolean[localSize];
		this.caches[LOCAL_PARTITION_ORDINAL] = new ScalarCache[localSize];

		this.scalar[GLOBAL_PARTITION_ORDINAL] = new boolean[globalSize];
		this.cachingEnabled[GLOBAL_PARTITION_ORDINAL] = new boolean[globalSize];
		this.caches[GLOBAL_PARTITION_ORDINAL] = new ScalarCache[globalSize];

		this.scalar[CONSTANT_PARTITION_ORDINAL] = new boolean[constantSize];
		this.cachingEnabled[CONSTANT_PARTITION_ORDINAL] = new boolean[constantSize];
		this.caches[CONSTANT_PARTITION_ORDINAL] = new ScalarCache[constantSize];

		this.scalar[STACK_PARTITION_ORDINAL] = new boolean[stackSize];
		this.cachingEnabled[STACK_PARTITION_ORDINAL] = new boolean[stackSize];
		this.caches[STACK_PARTITION_ORDINAL] = new ScalarCache[stackSize];

		this.scalar[NONE_PARTITION_ORDINAL] = new boolean[noneSize];
		this.cachingEnabled[NONE_PARTITION_ORDINAL] = new boolean[noneSize];
		this.caches[NONE_PARTITION_ORDINAL] = new ScalarCache[noneSize];

		for (int partitionIndex=0; partitionIndex<PARTITION_LENGTH; partitionIndex++) {
			Arrays.fill(this.scalar[partitionIndex], false);
			Arrays.fill(this.cachingEnabled[partitionIndex], false);
			Arrays.fill(this.caches[partitionIndex], null);
		}

		// NONEパーティションのオペランドは読み書きされないプレースホルダなので、最適化が効くようにキャッシュ済みとマークしておく
		Arrays.fill(this.scalar[NONE_PARTITION_ORDINAL], true);
		Arrays.fill(this.cachingEnabled[NONE_PARTITION_ORDINAL], true);
		Arrays.fill(this.caches[NONE_PARTITION_ORDINAL], new NoneCache());
	}

	// 定数領域やグローバル領域など、メモリ上にデータが確保済みのものについて、
	// メモリを読みながらスカラ判定を行い、スカラに対してはキャッシュ確保を行う
	// （定数値のキャッシュへの書き込みは後でSynchronizerで行う）
	private void detectScalarFromMemory(Memory memory, Memory.Partition partition) {
		int partitionOrdinal = partition.ordinal();
		int partitionSize = memory.getSize(partition);

		for (int address=0; address<partitionSize; address++) {

			DataContainer<?> container = null;
			try {
				container = memory.getDataContainer(partition, address);

			// 存在するはずの定数アドレスにアクセスしているので、この例外が発生する場合は実装の異常
			} catch (VnanoFatalException e) {
				throw new VnanoFatalException(e);
			}

			this.scalar[partitionOrdinal][address]
					= ( (container.getRank() == DataContainer.RANK_OF_SCALAR) );

			if (!this.scalar[partitionOrdinal][address]) {
				continue;
			}

			DataType dataType = container.getDataType();
			switch (dataType) {
				case INT64 : {
					this.caches[partitionOrdinal][address] = new Int64ScalarCache();
					this.cachingEnabled[partitionOrdinal][address] = true;
					break;
				}
				case FLOAT64 : {
					this.caches[partitionOrdinal][address] = new Float64ScalarCache();
					this.cachingEnabled[partitionOrdinal][address] = true;
					break;
				}
				case BOOL : {
					this.caches[partitionOrdinal][address] = new BoolScalarCache();
					this.cachingEnabled[partitionOrdinal][address] = true;
					break;
				}
				default : {
					break;
				}
			}
		}
	}

	// ローカル領域やレジスタ領域など、実行前にはメモリ上にデータが確保されていないものについて、
	// 命令列の中の確保命令を読んでスカラ判定を行い、スカラに対してはキャッシュ確保を行う
	private void detectScalarFromInstructions(Instruction[] instructions, Memory memory) {

		// アドレスに紐づけてキャッシュを持つ(同じデータコンテナに対して同じキャッシュが一意に対応するように)
		// 非キャッシュ演算ユニットはデータコンテナとキャッシュ要素を保持し、同期する

		int instructionLength = instructions.length;

		// 注意：グローバル領域に置かれた外部変数は、
		//       実行対象コード内でALLOCされていない場合もある（外部変数など）ため、
		//       以下と同一の方法ではスカラかどうかの完全判定はできない。
		//       そのため detectScalarFromMemory の方法で判定する

		// 以下、命令列内でデータ確保命令を呼んでいる箇所などをスキャンし、
		// スカラかどうか、キャッシュ可能かどうか等のメタ情報を判定して控える
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			Instruction instruction = instructions[instructionIndex];
			Memory.Partition[] partitions = instruction.getOperandPartitions();
			int[] addresses = instruction.getOperandAddresses();
			int operandLength = addresses.length;

			// このメソッドが判定対象とするのはローカル領域とレジスタ領域のデータのみ
			if (partitions[0] != Memory.Partition.LOCAL && partitions[0] != Memory.Partition.REGISTER) {
				continue;
			}

			switch (instruction.getOperationCode()) {

				case ALLOC :
				case ALLOCT : {

					// 1-オペランドのALLOC命令は、0次元なのでスカラ
					if (operandLength == 1) {
						this.scalar[ partitions[0].ordinal() ][ addresses[0] ] = true;

						// このALLOC命令の時点では、スカラであれば暫定的に cacheable と見なしておく。
						// このループはコードを命令順に読んでいってるので、ALLOC後にELEMしている場合などは、
						// 後でその ELEM を読んだ時点で uncacheable に訂正される

						switch (instruction.getDataTypes()[0]) {
							case INT64 : {
								//System.out.println("CACHE INT64 " + partitions[0] + " / " + addresses[0]);
								this.caches[ partitions[0].ordinal() ][ addresses[0] ] = new Int64ScalarCache();
								this.cachingEnabled[ partitions[0].ordinal() ][ addresses[0] ] = true;
								break;
							}
							case FLOAT64 : {
								//System.out.println("CACHE FLOAT64 " + partitions[0] + " / " + addresses[0]);
								this.caches[ partitions[0].ordinal() ][ addresses[0] ] = new Float64ScalarCache();
								this.cachingEnabled[ partitions[0].ordinal() ][ addresses[0] ] = true;
								break;
							}
							case BOOL : {
								//System.out.println("CACHE BOOL " + partitions[0] + " / " + addresses[0]);
								this.caches[ partitions[0].ordinal() ][ addresses[0] ] = new BoolScalarCache();
								this.cachingEnabled[ partitions[0].ordinal() ][ addresses[0] ] = true;
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
								this.cachingEnabled[ partitions[0].ordinal() ][ addresses[0] ] = true;
								break;
							}
							case FLOAT64 : {
								//System.out.println("CACHE FLOAT64 " + partitions[0] + " / " + addresses[0]);
								this.caches[ partitions[0].ordinal() ][ addresses[0] ] = new Float64ScalarCache();
								this.cachingEnabled[ partitions[0].ordinal() ][ addresses[0] ] = true;
								break;
							}
							case BOOL : {
								//System.out.println("CACHE BOOL " + partitions[0] + " / " + addresses[0]);
								this.caches[ partitions[0].ordinal() ][ addresses[0] ] = new BoolScalarCache();
								this.cachingEnabled[ partitions[0].ordinal() ][ addresses[0] ] = true;
								break;
							}
							default : break;
						}
					}
					break;
				}

				case ELEM : {

					// 現在の仕様では、ELEMで取り出したデータは必ずスカラ
					this.scalar[ partitions[0].ordinal() ][ addresses[0] ] = true;

					// ELEM命令は、ベクトルの要素（スカラ）への参照を第0オペランドのレジスタと同期するため、
					// 第0オペランドはスカラであるが、別の箇所で参照が共有されて書き換えられる可能性があるため、
					// キャッシュ可能ではない
					//（異なるアドレスのレジスタが同一データを参照を保持できるため、アドレスベースのキャッシュでは対応不可）
					this.cachingEnabled[ partitions[0].ordinal() ][ addresses[0] ] = false;
					break;
				}

				case REF :
				case REFPOP : {

					// ELEMと同様、REF系の対象も他の箇所のデータと参照を共有するようになるため、
					// アドレスベースのキャッシュでは対応不可（アドレス-データが一対一対応にならない）
					this.cachingEnabled[ partitions[0].ordinal() ][ addresses[0] ] = false;
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
