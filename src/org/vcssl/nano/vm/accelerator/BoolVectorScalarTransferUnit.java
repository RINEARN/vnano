/*
 * Copyright(C) 2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import java.util.Arrays;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class BoolVectorScalarTransferUnit extends AcceleratorExecutionUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		AcceleratorExecutionNode node = null;
		switch (instruction.getOperationCode()) {
			case FILL : {
				Boolx1ScalarCacheSynchronizer synchronizer
						= new Boolx1ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled, 1);
				node = new BoolVectorScalarFillNode(
						(DataContainer<boolean[]>)operandContainers[0], (DataContainer<boolean[]>)operandContainers[1],
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

	private final class BoolVectorScalarFillNode extends AcceleratorExecutionNode {

		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<boolean[]> container1;
		protected final Boolx1ScalarCacheSynchronizer synchronizer;

		public BoolVectorScalarFillNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1,
				Boolx1ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.container0 = container0;
			this.container1 = container1;
			this.synchronizer = synchronizer;
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			boolean value = this.container1.getArrayData()[ this.container1.getArrayOffset() ];
			int from = container0.getArrayOffset();
			int to = from + container0.getArraySize(); // to-1 まで書き込まれ、to の要素には書き込まれない
			Arrays.fill(this.container0.getArrayData(), from, to, value);
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}
}
