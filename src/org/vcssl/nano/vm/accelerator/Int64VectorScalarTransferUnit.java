/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import java.util.Arrays;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class Int64VectorScalarTransferUnit extends AcceleratorExecutionUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		AcceleratorExecutionNode node = null;
		switch (instruction.getOperationCode()) {
			case FILL : {
				Int64x1ScalarCacheSynchronizer synchronizer
						= new Int64x1ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled, 1);
				node = new Int64VectorScalarFillNode(
						(DataContainer<long[]>)operandContainers[0], (DataContainer<long[]>)operandContainers[1],
						synchronizer, nextNode);
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

	private final class Int64VectorScalarFillNode extends AcceleratorExecutionNode {

		protected final DataContainer<long[]> container0;
		protected final DataContainer<long[]> container1;
		protected final Int64x1ScalarCacheSynchronizer synchronizer;

		public Int64VectorScalarFillNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1,
				Int64x1ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.synchronizer = synchronizer;
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			long value = this.container1.getData()[ this.container1.getOffset() ];
			int from = container0.getOffset();
			int to = from + container0.getSize(); // to-1 まで書き込まれ、to の要素には書き込まれない
			Arrays.fill(this.container0.getData(), from, to, value);
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}
}
