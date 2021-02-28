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

public class Float64MemoryAllocationUnit extends AcceleratorExecutionUnit {

	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		if (instruction.getOperationCode() == OperationCode.ALLOC) {

			// 先頭オペランドは確保対象なので、「 オペランド数 - 1 」が要素数オペランドの数 = 次元数
			int arrayRank = operandContainers.length - 1;
			if (arrayRank == DataContainer.ARRAY_RANK_OF_SCALAR) {
				return new Float64ScalarAllocNode(operandContainers[0], nextNode);
			} else {
				return new Float64VectorAllocNode(
					operandContainers, operandCaches, operandCachingEnabled, arrayRank, nextNode
				);
			}

		} else if (instruction.getOperationCode() == OperationCode.ALLOCR) {

			return new Float64AllocrNode(
				operandContainers, nextNode
			);

		} else {
			throw new VnanoFatalException(
					"Operation code " + instruction.getOperationCode() + " is invalid for " + this.getClass().getCanonicalName()
			);
		}
	}


	private final class Float64ScalarAllocNode extends AcceleratorExecutionNode {
		protected final DataContainer<?> container0;

		public Float64ScalarAllocNode(DataContainer<?> container0, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.container0 = container0;
		}

		@SuppressWarnings("unchecked")
		public final AcceleratorExecutionNode execute() {

			// 既にメモリ確保されていて、値を十分格納できる場合は何もしない
			Object currentData = this.container0.getArrayData();
			if (currentData instanceof double[] && ((double[])currentData).length != 0) {
				// ※ offset 値が 0 でない対象に対して ALLOC される事は、現状の言語仕様では発生しない
				return this.nextNode;
			}

			// メモリ確保が必要なので、確保してデータコンテナに持たせる
			((DataContainer<double[]>)this.container0).setArrayData(
				new double[ DataContainer.ARRAY_SIZE_OF_SCALAR ],    // data
				0,                                                   // offset
				DataContainer.ARRAY_LENGTHS_OF_SCALAR                // lengths
			);

			return this.nextNode;
		}
	}


	private final class Float64VectorAllocNode extends AcceleratorExecutionNode {
		protected final DataContainer<?> targetContainer;
		protected final DataContainer<?>[] lengthsContainers;
		protected final Object[] lengthsCaches;
		protected final GeneralScalarCacheSynchronizer lengthsCacheSynchronizer;
		int rank;

		public Float64VectorAllocNode(
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
			if (currentData instanceof double[] && ((double[])currentData).length == requiredSize) {
				// ※ offset 値が 0 でない対象に対して ALLOC される事は、現状の言語仕様では発生しない
				return this.nextNode;
			}

			// メモリ確保が必要なので、確保してデータコンテナに持たせる
			new ExecutionUnit().allocVector(DataType.FLOAT64, this.targetContainer, this.lengthsContainers);

			return this.nextNode;
		}
	}


	private final class Float64AllocrNode extends AcceleratorExecutionNode {
		protected final DataContainer<?> targetContainer;
		protected final DataContainer<?> sameLengthContainer;

		public Float64AllocrNode(
				DataContainer<?>[] containers, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.targetContainer = containers[0];
			this.sameLengthContainer = containers[1];
		}

		public final AcceleratorExecutionNode execute() {
			new ExecutionUnit().allocSameLengths(DataType.FLOAT64, this.targetContainer, this.sameLengthContainer);
			return this.nextNode;
		}
	}

}

