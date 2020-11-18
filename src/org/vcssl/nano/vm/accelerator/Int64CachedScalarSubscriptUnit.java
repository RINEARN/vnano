/*
 * Copyright(C) 2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class Int64CachedScalarSubscriptUnit extends AcceleratorExecutionUnit {

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
		boolean isDestCacheable = operandCachingEnabled[0];

		switch (instruction.getOperationCode()) {

			case MOVELM : {

				// 1次元配列の場合
				if (targetArrayRank == 1) {
					if (isDestCacheable) { // dest と indices の両方が cacheable な場合
						node = new Int64FullCachedScalarMovelm1DNode(
							(Int64ScalarCache)operandCaches[0], (DataContainer<long[]>)operandContainers[1],
							(Int64ScalarCache)operandCaches[2], nextNode
						);
					} else { // indices のみ cacheable な場合
						node = new Int64SemiCachedScalarMovelm1DNode(
							(DataContainer<long[]>)operandContainers[0], (DataContainer<long[]>)operandContainers[1],
							(Int64ScalarCache)operandCaches[2], nextNode
						);
					}

				// 2次元配列の場合
				} else if (targetArrayRank == 2) {
					if (isDestCacheable) { // dest と indices の両方が cacheable な場合
						node = new Int64FullCachedScalarMovelm2DNode(
							(Int64ScalarCache)operandCaches[0], (DataContainer<long[]>)operandContainers[1],
							(Int64ScalarCache)operandCaches[2], (Int64ScalarCache)operandCaches[3], nextNode
						);
					} else { // indices のみ cacheable な場合
						node = new Int64SemiCachedScalarMovelm2DNode(
							(DataContainer<long[]>)operandContainers[0], (DataContainer<long[]>)operandContainers[1],
							(Int64ScalarCache)operandCaches[2], (Int64ScalarCache)operandCaches[3], nextNode
						);
					}

				// 3次元配列の場合
				} else if (targetArrayRank == 3) {
					if (isDestCacheable) { // dest と indices の両方が cacheable な場合
						node = new Int64FullCachedScalarMovelm3DNode(
							(Int64ScalarCache)operandCaches[0], (DataContainer<long[]>)operandContainers[1],
							(Int64ScalarCache)operandCaches[2], (Int64ScalarCache)operandCaches[3],
							(Int64ScalarCache)operandCaches[4], nextNode
						);
					} else { // indices のみ cacheable な場合
						node = new Int64SemiCachedScalarMovelm3DNode(
							(DataContainer<long[]>)operandContainers[0], (DataContainer<long[]>)operandContainers[1],
							(Int64ScalarCache)operandCaches[2], (Int64ScalarCache)operandCaches[3],
							(Int64ScalarCache)operandCaches[4], nextNode
						);
					}

				// このユニットで扱えない次元の配列の場合
				} else {
					throw new VnanoFatalException(
						"Operands of a MOVELM instructions are too many for this unit (max: " + MAX_AVAILABLE_RANK + ")"
					);
				}
				break;
			}

			case REFELM : {

				// 現状のスケジューラの実装では、参照リンクされる REFELM 命令の dest オペランドを
				// cacheable であると判定する事はできないはずなので、できていればエラー
				if (isDestCacheable) {
					throw new VnanoFatalException(
						"Invalid cacheability setting detected for the dest operand of a REFELM instruction"
					);
				}

				// 以下、全て dest は uncacheable で、 indices のみ cacheable

				// 1次元配列の場合
				if (targetArrayRank == 1) {
					node = new Int64SemiCachedScalarRefelm1DNode(
						(DataContainer<long[]>)operandContainers[0], (DataContainer<long[]>)operandContainers[1],
						(Int64ScalarCache)operandCaches[2], nextNode
					);

				// 2次元配列の場合
				} else if (targetArrayRank == 2) {
					node = new Int64SemiCachedScalarRefelm2DNode(
						(DataContainer<long[]>)operandContainers[0], (DataContainer<long[]>)operandContainers[1],
						(Int64ScalarCache)operandCaches[2], (Int64ScalarCache)operandCaches[3], nextNode
					);

				// 3次元配列の場合
				} else if (targetArrayRank == 3) {
					node = new Int64SemiCachedScalarRefelm3DNode(
						(DataContainer<long[]>)operandContainers[0], (DataContainer<long[]>)operandContainers[1],
						(Int64ScalarCache)operandCaches[2], (Int64ScalarCache)operandCaches[3],
						(Int64ScalarCache)operandCaches[4], nextNode
					);

				// このユニットで扱えない次元の配列の場合
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
	// MOVELM, Full-Cached (dest も indices も cacheable な場合)
	// --------------------------------------------------------------------------------

	private final class Int64FullCachedScalarMovelm1DNode extends AcceleratorExecutionNode {

		protected final Int64ScalarCache dest;
		protected final DataContainer<long[]> src;
		protected final Int64ScalarCache index0;

		public Int64FullCachedScalarMovelm1DNode(
				Int64ScalarCache dest, DataContainer<long[]> src, Int64ScalarCache index0,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.dest = dest;
			this.src = src;
			this.index0 = index0;
		}

		public final AcceleratorExecutionNode execute() {
			this.dest.value = this.src.getArrayData()[ (int)this.index0.value ];
			return this.nextNode;
		}
	}

	private final class Int64FullCachedScalarMovelm2DNode extends AcceleratorExecutionNode {

		protected final Int64ScalarCache dest;
		protected final DataContainer<long[]> src;
		protected final Int64ScalarCache index0;
		protected final Int64ScalarCache index1;

		public Int64FullCachedScalarMovelm2DNode(
				Int64ScalarCache dest, DataContainer<long[]> src,
				Int64ScalarCache index0, Int64ScalarCache index1,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.dest = dest;
			this.src = src;
			this.index0 = index0;
			this.index1 = index1;
		}

		public final AcceleratorExecutionNode execute() {
			int[] lengths = this.src.getArrayLengths(); // 各次元の要素数を格納する配列

			// 2次元インデックスから1次元インデックスへの変換
			// (次元は左から 0, 1, 2, ... で、注目インデックスより右にある次元の要素数の積が、そのインデックスの1増加による移動単位)
			int index = lengths[1]*(int)index0.value + (int)index1.value;

			this.dest.value = this.src.getArrayData()[ index ];
			return this.nextNode;
		}
	}

	private final class Int64FullCachedScalarMovelm3DNode extends AcceleratorExecutionNode {

		protected final Int64ScalarCache dest;
		protected final DataContainer<long[]> src;
		protected final Int64ScalarCache index0;
		protected final Int64ScalarCache index1;
		protected final Int64ScalarCache index2;

		public Int64FullCachedScalarMovelm3DNode(
				Int64ScalarCache dest, DataContainer<long[]> src,
				Int64ScalarCache index0, Int64ScalarCache index1, Int64ScalarCache index2,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.dest = dest;
			this.src = src;
			this.index0 = index0;
			this.index1 = index1;
			this.index2 = index2;
		}

		public final AcceleratorExecutionNode execute() {
			int[] lengths = this.src.getArrayLengths(); // 各次元の要素数を格納する配列

			// 3次元インデックスから1次元インデックスへの変換
			// (次元は左から 0, 1, 2, ... で、注目インデックスより右にある次元の要素数の積が、そのインデックスの1増加による移動単位)
			int index = lengths[1]*lengths[2]*(int)index0.value + lengths[2]*(int)index1.value + (int)index2.value;

			this.dest.value = this.src.getArrayData()[ index ];
			return this.nextNode;
		}
	}


	// --------------------------------------------------------------------------------
	// MOVELM, Semi-Cached (indices のみが cacheable な場合)
	// --------------------------------------------------------------------------------

	private final class Int64SemiCachedScalarMovelm1DNode extends AcceleratorExecutionNode {

		protected final DataContainer<long[]> dest;
		protected final DataContainer<long[]> src;
		protected final Int64ScalarCache index0;

		// ※ このノードが割り当てられる場面では、dest は uncacheable と判定されているので同期不要、
		//    src は配列なので uncacheable, そしてインデックスは cacheable だがキャッシュから読むので、
		//    cache synchronizer は要らない

		public Int64SemiCachedScalarMovelm1DNode(
				DataContainer<long[]> dest, DataContainer<long[]> src, Int64ScalarCache index0,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.dest = dest;
			this.src = src;
			this.index0 = index0;
		}

		public final AcceleratorExecutionNode execute() {
			this.dest.getArrayData()[ this.dest.getArrayOffset() ] = this.src.getArrayData()[ (int)this.index0.value ];
			return this.nextNode;
		}
	}

	private final class Int64SemiCachedScalarMovelm2DNode extends AcceleratorExecutionNode {

		protected final DataContainer<long[]> dest;
		protected final DataContainer<long[]> src;
		protected final Int64ScalarCache index0;
		protected final Int64ScalarCache index1;

		// ※ このノードが割り当てられる場面では、dest は uncacheable と判定されているので同期不要、
		//    src は配列なので uncacheable, そしてインデックスは cacheable だがキャッシュから読むので、
		//    cache synchronizer は要らない

		public Int64SemiCachedScalarMovelm2DNode(
				DataContainer<long[]> dest, DataContainer<long[]> src,
				Int64ScalarCache index0, Int64ScalarCache index1,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.dest = dest;
			this.src = src;
			this.index0 = index0;
			this.index1 = index1;
		}

		public final AcceleratorExecutionNode execute() {
			int[] lengths = this.src.getArrayLengths(); // 各次元の要素数を格納する配列

			// 2次元インデックスから1次元インデックスへの変換
			// (次元は左から 0, 1, 2, ... で、注目インデックスより右にある次元の要素数の積が、そのインデックスの1増加による移動単位)
			int index = lengths[1]*(int)index0.value + (int)index1.value;

			this.dest.getArrayData()[ this.dest.getArrayOffset() ] = this.src.getArrayData()[ index ];
			return this.nextNode;
		}
	}

	private final class Int64SemiCachedScalarMovelm3DNode extends AcceleratorExecutionNode {

		protected final DataContainer<long[]> dest;
		protected final DataContainer<long[]> src;
		protected final Int64ScalarCache index0;
		protected final Int64ScalarCache index1;
		protected final Int64ScalarCache index2;

		// ※ このノードが割り当てられる場面では、dest は uncacheable と判定されているので同期不要、
		//    src は配列なので uncacheable, そしてインデックスは cacheable だがキャッシュから読むので、
		//    cache synchronizer は要らない

		public Int64SemiCachedScalarMovelm3DNode(
				DataContainer<long[]> dest, DataContainer<long[]> src,
				Int64ScalarCache index0, Int64ScalarCache index1, Int64ScalarCache index2,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.dest = dest;
			this.src = src;
			this.index0 = index0;
			this.index1 = index1;
			this.index2 = index2;
		}

		public final AcceleratorExecutionNode execute() {
			int[] lengths = this.src.getArrayLengths(); // 各次元の要素数を格納する配列

			// 3次元インデックスから1次元インデックスへの変換
			// (次元は左から 0, 1, 2, ... で、注目インデックスより右にある次元の要素数の積が、そのインデックスの1増加による移動単位)
			int index = lengths[1]*lengths[2]*(int)index0.value + lengths[2]*(int)index1.value + (int)index2.value;

			this.dest.getArrayData()[ this.dest.getArrayOffset() ] = this.src.getArrayData()[ index ];
			return this.nextNode;
		}
	}


	// --------------------------------------------------------------------------------
	// REFELM, Semi-Cached (indices のみが cacheable な場合)
	// --------------------------------------------------------------------------------

	private final class Int64SemiCachedScalarRefelm1DNode extends AcceleratorExecutionNode {

		protected final DataContainer<long[]> dest;
		protected final DataContainer<long[]> src;
		protected final Int64ScalarCache index0;

		// ※ このノードが割り当てられる場面では、dest は uncacheable と判定されているので同期不要、
		//    src は配列なので uncacheable, そしてインデックスは cacheable だがキャッシュから読むので、
		//    cache synchronizer は要らない

		public Int64SemiCachedScalarRefelm1DNode(
				DataContainer<long[]> dest, DataContainer<long[]> src, Int64ScalarCache index0,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.dest = dest;
			this.src = src;
			this.index0 = index0;
		}

		public final AcceleratorExecutionNode execute() {
			this.dest.setArrayData(this.src.getArrayData(), (int)this.index0.value, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
			return this.nextNode;
		}
	}

	private final class Int64SemiCachedScalarRefelm2DNode extends AcceleratorExecutionNode {

		protected final DataContainer<long[]> dest;
		protected final DataContainer<long[]> src;
		protected final Int64ScalarCache index0;
		protected final Int64ScalarCache index1;

		// ※ このノードが割り当てられる場面では、dest は uncacheable と判定されているので同期不要、
		//    src は配列なので uncacheable, そしてインデックスは cacheable だがキャッシュから読むので、
		//    cache synchronizer は要らない

		public Int64SemiCachedScalarRefelm2DNode(
				DataContainer<long[]> dest, DataContainer<long[]> src,
				Int64ScalarCache index0, Int64ScalarCache index1,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.dest = dest;
			this.src = src;
			this.index0 = index0;
			this.index1 = index1;
		}

		public final AcceleratorExecutionNode execute() {
			int[] lengths = this.src.getArrayLengths(); // 各次元の要素数を格納する配列

			// 2次元インデックスから1次元インデックスへの変換
			// (次元は左から 0, 1, 2, ... で、注目インデックスより右にある次元の要素数の積が、そのインデックスの1増加による移動単位)
			int index = lengths[1]*(int)index0.value + (int)index1.value;

			this.dest.setArrayData(this.src.getArrayData(), index, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
			return this.nextNode;
		}
	}

	private final class Int64SemiCachedScalarRefelm3DNode extends AcceleratorExecutionNode {

		protected final DataContainer<long[]> dest;
		protected final DataContainer<long[]> src;
		protected final Int64ScalarCache index0;
		protected final Int64ScalarCache index1;
		protected final Int64ScalarCache index2;

		// ※ このノードが割り当てられる場面では、dest は uncacheable と判定されているので同期不要、
		//    src は配列なので uncacheable, そしてインデックスは cacheable だがキャッシュから読むので、
		//    cache synchronizer は要らない

		public Int64SemiCachedScalarRefelm3DNode(
				DataContainer<long[]> dest, DataContainer<long[]> src,
				Int64ScalarCache index0, Int64ScalarCache index1, Int64ScalarCache index2,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.dest = dest;
			this.src = src;
			this.index0 = index0;
			this.index1 = index1;
			this.index2 = index2;
		}

		public final AcceleratorExecutionNode execute() {
			int[] lengths = this.src.getArrayLengths(); // 各次元の要素数を格納する配列

			// 3次元インデックスから1次元インデックスへの変換
			// (次元は左から 0, 1, 2, ... で、注目インデックスより右にある次元の要素数の積が、そのインデックスの1増加による移動単位)
			int index = lengths[1]*lengths[2]*(int)index0.value + lengths[2]*(int)index1.value + (int)index2.value;

			this.dest.setArrayData(this.src.getArrayData(), index, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
			return this.nextNode;
		}
	}

}
