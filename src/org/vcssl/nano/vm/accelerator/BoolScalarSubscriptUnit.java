/*
 * Copyright(C) 2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class BoolScalarSubscriptUnit extends AcceleratorExecutionUnit {

	// このユニットで処理できる、ELEM命令対象配列の最大次元数
	//（処理できない場合、Processorが任意次元対応なので、スケジューラ側でそちらへバイパス割り当てが必要）
	public static final int ELEM_MAX_AVAILABLE_RANK = 1;

	@SuppressWarnings("unchecked")
	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		AcceleratorExecutionNode node = null;
		switch (instruction.getOperationCode()) {
			case ELEM : {
				// 要素を参照したい配列の次元数（＝indicesオペランド数なので全オペランド数-2）
				int targetArrayRank = operandContainers.length - 2;
				if (targetArrayRank == 1) {
					Int64x1ScalarCacheSynchronizer synchronizer = new Int64x1ScalarCacheSynchronizer(
						new DataContainer<?>[] { operandContainers[0] }, // index部のみ対象 (この命令のdestはuncachable, srcは配列なので)
						new Object[] { operandCaches[0] },
						new boolean[] { operandCachingEnabled[0] }
					);
					node = new BoolScalarElem1DNode(
						(DataContainer<boolean[]>)operandContainers[0], (DataContainer<boolean[]>)operandContainers[1],
						(DataContainer<long[]>)operandContainers[2], synchronizer, nextNode
					);

				// 2次元配列場合
				} else if (targetArrayRank == 2) {
					Int64x2ScalarCacheSynchronizer synchronizer = new Int64x2ScalarCacheSynchronizer(
						new DataContainer<?>[] { operandContainers[2], operandContainers[3] }, // 上述参照
						new Object[] { operandCaches[2], operandCaches[3] },
						new boolean[] { operandCachingEnabled[2], operandCachingEnabled[3] }
					);
					node = new BoolScalarElem2DNode(
						(DataContainer<boolean[]>)operandContainers[0], (DataContainer<boolean[]>)operandContainers[1],
						(DataContainer<long[]>)operandContainers[2], (DataContainer<long[]>)operandContainers[3],
						synchronizer, nextNode
					);

				// 3次元配列場合
				} else if (targetArrayRank == 3) {
					Int64x3ScalarCacheSynchronizer synchronizer = new Int64x3ScalarCacheSynchronizer(
						new DataContainer<?>[] { operandContainers[2], operandContainers[3], operandContainers[4] }, // 上述参照
						new Object[] { operandCaches[2], operandCaches[3], operandCaches[4] },
						new boolean[] { operandCachingEnabled[2], operandCachingEnabled[3], operandCachingEnabled[4] }
					);
					node = new BoolScalarElem3DNode(
						(DataContainer<boolean[]>)operandContainers[0], (DataContainer<boolean[]>)operandContainers[1],
						(DataContainer<long[]>)operandContainers[2], (DataContainer<long[]>)operandContainers[3],
						(DataContainer<long[]>)operandContainers[4], synchronizer, nextNode
					);

				} else {
					throw new VnanoFatalException(
						"Operands of a ELEM instructions are too many for this unit (max: " + (targetArrayRank+2) + ")"
					);
				}
				break;
			}

			default : {
				throw new VnanoFatalException(
						"Operation code " + instruction.getOperationCode() + " is invalid for " + this.getClass().getCanonicalName()
				);
			}
		}
		return node;
	}

	private final class BoolScalarElem1DNode extends AcceleratorExecutionNode {

		protected final DataContainer<boolean[]> container0; // dest
		protected final DataContainer<boolean[]> container1; // src
		protected final DataContainer<long[]> container2;   // indices[0]
		protected final Int64x1ScalarCacheSynchronizer synchronizer; // index部のみ対象 (この命令のdestはuncacheable, srcは配列なので)

		public BoolScalarElem1DNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1, DataContainer<long[]> container2,
				Int64x1ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory(); // この演算ユニットは CachedScalar 系ではないので sync する（要/不要はスケジューラ側で判断）
			int index = (int)this.container2.getData()[ this.container2.getOffset() ];
			this.container0.setData( this.container1.getData(), index, DataContainer.SCALAR_LENGTHS ); // 注: この命令はデータ参照を同期する
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class BoolScalarElem2DNode extends AcceleratorExecutionNode {

		protected final DataContainer<boolean[]> container0; // dest
		protected final DataContainer<boolean[]> container1; // src
		protected final DataContainer<long[]> container2; // indices[0]
		protected final DataContainer<long[]> container3; // indices[1]
		protected final Int64x2ScalarCacheSynchronizer synchronizer; // index部のみ対象 (この命令のdestはuncacheable, srcは配列なので)

		public BoolScalarElem2DNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1,
				DataContainer<long[]> container2, DataContainer<long[]> container3,
				Int64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.container3 = container3;
			this.synchronizer = synchronizer;
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory(); // この演算ユニットは CachedScalar 系ではないので sync する（要/不要はスケジューラ側で判断）
			int[] lengths = this.container1.getLengths();
			int index0 = (int)this.container2.getData()[ this.container2.getOffset() ];
			int index1 = (int)this.container3.getData()[ this.container3.getOffset() ];

			// 2次元インデックスから1次元インデックスへの変換
			// (次元は左から 0, 1, 2, ... で、注目インデックスより右にある次元の要素数の積が、そのインデックスの1増加による移動単位)
			int index = lengths[1]*index0 + index1;

			this.container0.setData( this.container1.getData(), index, DataContainer.SCALAR_LENGTHS ); // 注: この命令はデータ参照を同期する
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class BoolScalarElem3DNode extends AcceleratorExecutionNode {

		protected final DataContainer<boolean[]> container0; // dest
		protected final DataContainer<boolean[]> container1; // src
		protected final DataContainer<long[]> container2; // indices[0]
		protected final DataContainer<long[]> container3; // indices[1]
		protected final DataContainer<long[]> container4; // indices[2]
		protected final Int64x3ScalarCacheSynchronizer synchronizer; // index部のみ対象 (この命令のdestはuncacheable, srcは配列なので)

		public BoolScalarElem3DNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1,
				DataContainer<long[]> container2, DataContainer<long[]> container3, DataContainer<long[]> container4,
				Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.container3 = container3;
			this.container4 = container4;
			this.synchronizer = synchronizer;
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory(); // この演算ユニットは CachedScalar 系ではないので sync する（要/不要はスケジューラ側で判断）
			int[] lengths = this.container1.getLengths();
			int index0 = (int)this.container2.getData()[ this.container2.getOffset() ];
			int index1 = (int)this.container3.getData()[ this.container3.getOffset() ];
			int index2 = (int)this.container4.getData()[ this.container4.getOffset() ];

			// 3次元インデックスから1次元インデックスへの変換
			// (次元は左から 0, 1, 2, ... で、注目インデックスより右にある次元の要素数の積が、そのインデックスの1増加による移動単位)
			int index = lengths[1]*lengths[2]*index0 + lengths[2]*index1 + index2;

			this.container0.setData( this.container1.getData(), index, DataContainer.SCALAR_LENGTHS ); // 注: この命令はデータ参照を同期する
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

}
