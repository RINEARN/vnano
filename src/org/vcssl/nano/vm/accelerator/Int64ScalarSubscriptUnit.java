/*
 * Copyright(C) 2020-2021 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class Int64ScalarSubscriptUnit extends AcceleratorExecutionUnit {

	// このユニットで処理できる、MOVELM / REFELM 命令対象配列の最大次元数
	//（処理できない場合、Processorが任意次元対応なので、スケジューラ側でそちらへバイパス割り当てが必要）
	public static final int MAX_AVAILABLE_RANK = 3;

	@SuppressWarnings("unchecked")
	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		AcceleratorExecutionNode node = null;

		// 要素を参照したい配列の次元数（＝indicesオペランド数なので全オペランド数-2）
		int targetArrayRank = operandContainers.length - 2;

		switch (instruction.getOperationCode()) {

			case MOVELM : {

				// 1次元配列の場合
				if (targetArrayRank == 1) {
					Int64x2ScalarCacheSynchronizer synchronizer = new Int64x2ScalarCacheSynchronizer(
						new DataContainer<?>[] { operandContainers[0], operandContainers[2] }, // dest と index部のみ対象 (srcは配列なので)
						new Object[] { operandCaches[0], operandCaches[2] },
						new boolean[] { operandCachingEnabled[0], operandCachingEnabled[2] }
					);
					node = new Int64ScalarMovelm1DNode(
						(DataContainer<long[]>)operandContainers[0], (DataContainer<long[]>)operandContainers[1],
						(DataContainer<long[]>)operandContainers[2], synchronizer, nextNode
					);

				// 2次元配列の場合
				} else if (targetArrayRank == 2) {
					Int64x3ScalarCacheSynchronizer synchronizer = new Int64x3ScalarCacheSynchronizer(
						new DataContainer<?>[] { operandContainers[0], operandContainers[2], operandContainers[3] }, // 上述参照
						new Object[] { operandCaches[0], operandCaches[2], operandCaches[3] },
						new boolean[] { operandCachingEnabled[0], operandCachingEnabled[2], operandCachingEnabled[3] }
					);
					node = new Int64ScalarMovelm2DNode(
						(DataContainer<long[]>)operandContainers[0], (DataContainer<long[]>)operandContainers[1],
						(DataContainer<long[]>)operandContainers[2], (DataContainer<long[]>)operandContainers[3],
						synchronizer, nextNode
					);

				// 3次元配列の場合
				} else if (targetArrayRank == 3) {
					Int64x4ScalarCacheSynchronizer synchronizer = new Int64x4ScalarCacheSynchronizer(
						new DataContainer<?>[] { operandContainers[0], operandContainers[2], operandContainers[3], operandContainers[4] }, // 上述参照
						new Object[] { operandCaches[0], operandCaches[2], operandCaches[3], operandCaches[4] },
						new boolean[] { operandCachingEnabled[0], operandCachingEnabled[2], operandCachingEnabled[3], operandCachingEnabled[4] }
					);
					node = new Int64ScalarMovelm3DNode(
						(DataContainer<long[]>)operandContainers[0], (DataContainer<long[]>)operandContainers[1],
						(DataContainer<long[]>)operandContainers[2], (DataContainer<long[]>)operandContainers[3],
						(DataContainer<long[]>)operandContainers[4], synchronizer, nextNode
					);

				} else {
					throw new VnanoFatalException(
						"Operands of a MOVELM instructions are too many for this unit (max: " + MAX_AVAILABLE_RANK + ")"
					);
				}
				break;
			}

			case REFELM : {

				// 1次元配列の場合
				if (targetArrayRank == 1) {
					Int64x1ScalarCacheSynchronizer synchronizer = new Int64x1ScalarCacheSynchronizer(
						new DataContainer<?>[] { operandContainers[2] }, // index部のみ対象 (この命令のdestはuncachable, srcは配列なので)
						new Object[] { operandCaches[2] },
						new boolean[] { operandCachingEnabled[2] }
					);
					node = new Int64ScalarRefelm1DNode(
						(DataContainer<long[]>)operandContainers[0], (DataContainer<long[]>)operandContainers[1],
						(DataContainer<long[]>)operandContainers[2], synchronizer, nextNode
					);

				// 2次元配列の場合
				} else if (targetArrayRank == 2) {
					Int64x2ScalarCacheSynchronizer synchronizer = new Int64x2ScalarCacheSynchronizer(
						new DataContainer<?>[] { operandContainers[2], operandContainers[3] }, // 上述参照
						new Object[] { operandCaches[2], operandCaches[3] },
						new boolean[] { operandCachingEnabled[2], operandCachingEnabled[3] }
					);
					node = new Int64ScalarRefelm2DNode(
						(DataContainer<long[]>)operandContainers[0], (DataContainer<long[]>)operandContainers[1],
						(DataContainer<long[]>)operandContainers[2], (DataContainer<long[]>)operandContainers[3],
						synchronizer, nextNode
					);

				// 3次元配列の場合
				} else if (targetArrayRank == 3) {
					Int64x3ScalarCacheSynchronizer synchronizer = new Int64x3ScalarCacheSynchronizer(
						new DataContainer<?>[] { operandContainers[2], operandContainers[3], operandContainers[4] }, // 上述参照
						new Object[] { operandCaches[2], operandCaches[3], operandCaches[4] },
						new boolean[] { operandCachingEnabled[2], operandCachingEnabled[3], operandCachingEnabled[4] }
					);
					node = new Int64ScalarRefelm3DNode(
						(DataContainer<long[]>)operandContainers[0], (DataContainer<long[]>)operandContainers[1],
						(DataContainer<long[]>)operandContainers[2], (DataContainer<long[]>)operandContainers[3],
						(DataContainer<long[]>)operandContainers[4], synchronizer, nextNode
					);

				} else {
					throw new VnanoFatalException(
						"Operands of a REFELM instructions are too many for this unit (max: " + MAX_AVAILABLE_RANK + ")"
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

	// --------------------------------------------------------------------------------
	// MOVELM
	// --------------------------------------------------------------------------------

	private final class Int64ScalarMovelm1DNode extends AcceleratorExecutionNode {

		protected final DataContainer<long[]> dest;
		protected final DataContainer<long[]> src;
		protected final DataContainer<long[]> index0;
		protected final Int64x2ScalarCacheSynchronizer synchronizer; // destとindex部のみ対象 (srcは配列なので)

		public Int64ScalarMovelm1DNode(
				DataContainer<long[]> dest, DataContainer<long[]> src, DataContainer<long[]> index0,
				Int64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.dest = dest;
			this.src = src;
			this.index0 = index0;
			this.synchronizer = synchronizer;
		}

		public final AcceleratorExecutionNode execute() {
			// このユニットは非Cached系なのでメモリ値ベースで処理するため、先にCached系による変更をライトバックする
			this.synchronizer.synchronizeFromCacheToMemory();

			// 要素のコピー処理
			int index = (int)this.index0.getArrayData()[ this.index0.getArrayOffset() ];
			this.dest.getArrayData()[ this.dest.getArrayOffset() ] = this.src.getArrayData()[ index ];

			// 後でCached系が処理で使うためにキャッシュを更新しておく
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64ScalarMovelm2DNode extends AcceleratorExecutionNode {

		protected final DataContainer<long[]> dest;
		protected final DataContainer<long[]> src;
		protected final DataContainer<long[]> index0;
		protected final DataContainer<long[]> index1;
		protected final Int64x3ScalarCacheSynchronizer synchronizer; // destとindex部のみ対象 (srcは配列なので)

		public Int64ScalarMovelm2DNode(
				DataContainer<long[]> dest, DataContainer<long[]> src,
				DataContainer<long[]> index0, DataContainer<long[]> index1,
				Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.dest = dest;
			this.src = src;
			this.index0 = index0;
			this.index1 = index1;
			this.synchronizer = synchronizer;
		}

		public final AcceleratorExecutionNode execute() {

			// このユニットは非Cached系なのでメモリ値ベースで処理するため、先にCached系による変更をライトバックする
			this.synchronizer.synchronizeFromCacheToMemory();

			// 2次元インデックスから1次元インデックスへの変換
			// (次元は左から 0, 1, 2, ... で、注目インデックスより右にある次元の要素数の積が、そのインデックスの1増加による移動単位)
			int[] lengths = this.src.getArrayLengths(); // 各次元の要素数を格納する配列
			int indexValue0 = (int)this.index0.getArrayData()[ this.index0.getArrayOffset() ];
			int indexValue1 = (int)this.index1.getArrayData()[ this.index1.getArrayOffset() ];
			int index = lengths[1]*indexValue0 + indexValue1;

			// 要素のコピー処理
			this.dest.getArrayData()[ this.dest.getArrayOffset() ] = this.src.getArrayData()[ index ];

			// 後でCached系が処理で使うためにキャッシュを更新しておく
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64ScalarMovelm3DNode extends AcceleratorExecutionNode {

		protected final DataContainer<long[]> dest;
		protected final DataContainer<long[]> src;
		protected final DataContainer<long[]> index0;
		protected final DataContainer<long[]> index1;
		protected final DataContainer<long[]> index2;
		protected final Int64x4ScalarCacheSynchronizer synchronizer; // destとindex部のみ対象 (srcは配列なので)

		public Int64ScalarMovelm3DNode(
				DataContainer<long[]> dest, DataContainer<long[]> src,
				DataContainer<long[]> index0, DataContainer<long[]> index1, DataContainer<long[]> index2,
				Int64x4ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.dest = dest;
			this.src = src;
			this.index0 = index0;
			this.index1 = index1;
			this.index2 = index2;
			this.synchronizer = synchronizer;
		}

		public final AcceleratorExecutionNode execute() {

			// このユニットは非Cached系なのでメモリ値ベースで処理するため、先にCached系による変更をライトバックする
			this.synchronizer.synchronizeFromCacheToMemory();

			// 3次元インデックスから1次元インデックスへの変換
			// (次元は左から 0, 1, 2, ... で、注目インデックスより右にある次元の要素数の積が、そのインデックスの1増加による移動単位)
			int[] lengths = this.src.getArrayLengths(); // 各次元の要素数を格納する配列
			int indexValue0 = (int)this.index0.getArrayData()[ this.index0.getArrayOffset() ];
			int indexValue1 = (int)this.index1.getArrayData()[ this.index1.getArrayOffset() ];
			int indexValue2 = (int)this.index2.getArrayData()[ this.index2.getArrayOffset() ];
			int index = lengths[1]*lengths[2]*indexValue0 + lengths[2]*indexValue1 + indexValue2;

			// 要素のコピー処理
			this.dest.getArrayData()[ this.dest.getArrayOffset() ] = this.src.getArrayData()[ index ];

			// 後でCached系が処理で使うためにキャッシュを更新しておく
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}


	// --------------------------------------------------------------------------------
	// REFELM
	// --------------------------------------------------------------------------------

	private final class Int64ScalarRefelm1DNode extends AcceleratorExecutionNode {

		protected final DataContainer<long[]> dest;
		protected final DataContainer<long[]> src;
		protected final DataContainer<long[]> index0;
		protected final Int64x1ScalarCacheSynchronizer synchronizer; // index部のみ対象 (この命令のdestはuncacheable, srcは配列なので)

		public Int64ScalarRefelm1DNode(
				DataContainer<long[]> dest, DataContainer<long[]> src, DataContainer<long[]> index0,
				Int64x1ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.dest = dest;
			this.src = src;
			this.index0 = index0;
			this.synchronizer = synchronizer;
		}

		public final AcceleratorExecutionNode execute() {

			// このユニットは非Cached系なのでメモリ値ベースで処理するため、先にCached系による変更をライトバックする
			this.synchronizer.synchronizeFromCacheToMemory();

			// 要素の参照代入処理（この命令は dest のデータ参照を src のものとリンクし、dest の offset 値として index の値を設定する）
			int index = (int)this.index0.getArrayData()[ this.index0.getArrayOffset() ];
			this.dest.setArrayData( this.src.getArrayData(), index, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

			// 後でCached系が処理で使うためにキャッシュを更新しておく
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64ScalarRefelm2DNode extends AcceleratorExecutionNode {

		protected final DataContainer<long[]> dest;
		protected final DataContainer<long[]> src;
		protected final DataContainer<long[]> index0;
		protected final DataContainer<long[]> index1;
		protected final Int64x2ScalarCacheSynchronizer synchronizer; // index部のみ対象 (この命令のdestはuncacheable, srcは配列なので)

		public Int64ScalarRefelm2DNode(
				DataContainer<long[]> dest, DataContainer<long[]> src,
				DataContainer<long[]> index0, DataContainer<long[]> index1,
				Int64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.dest = dest;
			this.src = src;
			this.index0 = index0;
			this.index1 = index1;
			this.synchronizer = synchronizer;
		}

		public final AcceleratorExecutionNode execute() {

			// このユニットは非Cached系なのでメモリ値ベースで処理するため、先にCached系による変更をライトバックする
			this.synchronizer.synchronizeFromCacheToMemory();

			// 2次元インデックスから1次元インデックスへの変換
			// (次元は左から 0, 1, 2, ... で、注目インデックスより右にある次元の要素数の積が、そのインデックスの1増加による移動単位)
			int[] lengths = this.src.getArrayLengths();
			int indexValue0 = (int)this.index0.getArrayData()[ this.index0.getArrayOffset() ];
			int indexValue1 = (int)this.index1.getArrayData()[ this.index1.getArrayOffset() ];
			int index = lengths[1]*indexValue0 + indexValue1;

			// 要素の参照代入処理（この命令は dest のデータ参照を src のものとリンクし、dest の offset 値として index の値を設定する）
			this.dest.setArrayData( this.src.getArrayData(), index, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

			// 後でCached系が処理で使うためにキャッシュを更新しておく
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64ScalarRefelm3DNode extends AcceleratorExecutionNode {

		protected final DataContainer<long[]> dest;
		protected final DataContainer<long[]> src;
		protected final DataContainer<long[]> index0;
		protected final DataContainer<long[]> index1;
		protected final DataContainer<long[]> index2;
		protected final Int64x3ScalarCacheSynchronizer synchronizer; // index部のみ対象 (この命令のdestはuncacheable, srcは配列なので)

		public Int64ScalarRefelm3DNode(
				DataContainer<long[]> dest, DataContainer<long[]> src,
				DataContainer<long[]> index0, DataContainer<long[]> index1, DataContainer<long[]> index2,
				Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.dest = dest;
			this.src = src;
			this.index0 = index0;
			this.index1 = index1;
			this.index2 = index2;
			this.synchronizer = synchronizer;
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();


			// 3次元インデックスから1次元インデックスへの変換
			// (次元は左から 0, 1, 2, ... で、注目インデックスより右にある次元の要素数の積が、そのインデックスの1増加による移動単位)
			int[] lengths = this.src.getArrayLengths();
			int indexValue0 = (int)this.index0.getArrayData()[ this.index0.getArrayOffset() ];
			int indexValue1 = (int)this.index1.getArrayData()[ this.index1.getArrayOffset() ];
			int indexValue2 = (int)this.index2.getArrayData()[ this.index2.getArrayOffset() ];
			int index = lengths[1]*lengths[2]*indexValue0 + lengths[2]*indexValue1 + indexValue2;

			// 要素の参照代入処理（この命令は dest のデータ参照を src のものとリンクし、dest の offset 値として index の値を設定する）
			this.dest.setArrayData( this.src.getArrayData(), index, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

			// 後でCached系が処理で使うためにキャッシュを更新しておく
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

}
