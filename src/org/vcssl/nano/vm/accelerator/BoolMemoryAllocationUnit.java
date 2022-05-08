/*
 * Copyright(C) 2021 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.processor.ExecutionUnit;

public class BoolMemoryAllocationUnit extends AcceleratorExecutionUnit {

	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		if (instruction.getOperationCode() == OperationCode.ALLOC) {

			// 先頭オペランドは確保対象なので、「 オペランド数 - 1 」が要素数オペランドの数 = 次元数
			int arrayRank = operandContainers.length - 1;
			if (arrayRank == DataContainer.ARRAY_RANK_OF_SCALAR) {
				return new BoolScalarAllocNode(operandContainers[0], nextNode);
			} else {
				return new BoolVectorAllocNode(
					operandContainers, operandCaches, operandCachingEnabled, arrayRank, nextNode
				);
			}

		} else if (instruction.getOperationCode() == OperationCode.ALLOCR) {

			return new BoolAllocrNode(
				operandContainers, nextNode
			);

		} else {
			throw new VnanoFatalException(
					"Operation code " + instruction.getOperationCode() + " is invalid for " + this.getClass().getCanonicalName()
			);
		}
	}


	private final class BoolScalarAllocNode extends AcceleratorExecutionNode {
		protected final DataContainer<?> container0;

		public BoolScalarAllocNode(DataContainer<?> container0, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.container0 = container0;
		}

		@SuppressWarnings("unchecked")
		public final AcceleratorExecutionNode execute() {

			// 既にメモリ確保されていて、値を十分格納できる場合は何もしない
			Object currentData = this.container0.getArrayData();
			if (currentData instanceof boolean[] && ((boolean[])currentData).length != 0) {
				// ※ offset 値が 0 でない対象に対して ALLOC される事は、現状の言語仕様では発生しない
				return this.nextNode;
			}

			// メモリ確保が必要なので、確保してデータコンテナに持たせる
			((DataContainer<boolean[]>)this.container0).setArrayData(
				new boolean[ DataContainer.ARRAY_SIZE_OF_SCALAR ],    // data
				0,                                                    // offset
				DataContainer.ARRAY_LENGTHS_OF_SCALAR                 // lengths
			);

			return this.nextNode;
		}
	}


	private final class BoolVectorAllocNode extends AcceleratorExecutionNode {
		protected final DataContainer<?> targetContainer;
		protected final DataContainer<?>[] lengthsContainers;
		protected final Object[] lengthsCaches;
		protected final GeneralScalarCacheSynchronizer lengthsCacheSynchronizer;
		int rank;

		public BoolVectorAllocNode(
				DataContainer<?>[] containers, Object[] caches, boolean[] cachingEnabled, int arrayRank,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.rank = arrayRank;
			this.targetContainer = containers[0];
			this.lengthsContainers = new DataContainer<?>[this.rank];
			this.lengthsCaches = new Object[this.rank];
			boolean[] lengthsCachingEnabled = new boolean[this.rank];
			System.arraycopy(containers, 1, this.lengthsContainers, 0, this.rank);
			System.arraycopy(caches, 1, this.lengthsCaches, 0, this.rank);
			System.arraycopy(cachingEnabled, 1, lengthsCachingEnabled, 0, this.rank);
			this.lengthsCacheSynchronizer = new GeneralScalarCacheSynchronizer(
				this.lengthsContainers, this.lengthsCaches, lengthsCachingEnabled
			);
		}

		@SuppressWarnings("unchecked")
		public final AcceleratorExecutionNode execute() {

			// 要素数オペランドはスカラ値なのでキャッシュされている可能性があり、
			// その最新値を lengthsContainers に反映させる
			this.lengthsCacheSynchronizer.synchronizeFromCacheToMemory();

			// 必要な配列サイズを計算する
			int requiredSize = 1;
			for (int dim=0; dim<rank; dim++) {
				long[] lengthContainerData = ( (DataContainer<long[]>)lengthsContainers[dim] ).getArrayData();
				requiredSize *= (int)( lengthContainerData[ lengthsContainers[dim].getArrayOffset() ] );
			}

			// 既にメモリ確保されていて、値を十分格納できる場合は何もしない
			Object currentData = this.targetContainer.getArrayData();
			if (currentData instanceof boolean[] && ((boolean[])currentData).length == requiredSize) {
				// ※ offset 値が 0 でない対象に対して ALLOC される事は、現状の言語仕様では発生しない
				return this.nextNode;
			}

			// メモリ確保が必要なので、確保してデータコンテナに持たせる
			new ExecutionUnit().allocVector(DataType.BOOL, this.targetContainer, this.lengthsContainers);

			return this.nextNode;
		}
	}


	private final class BoolAllocrNode extends AcceleratorExecutionNode {
		protected final DataContainer<?> targetContainer;
		protected final DataContainer<?> sameLengthContainer;

		public BoolAllocrNode(
				DataContainer<?>[] containers, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.targetContainer = containers[0];
			this.sameLengthContainer = containers[1];
		}

		public final AcceleratorExecutionNode execute() {
			new ExecutionUnit().allocSameLengths(DataType.BOOL, this.targetContainer, this.sameLengthContainer);
			return this.nextNode;
		}
	}

}

