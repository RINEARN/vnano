/*
 * Copyright(C) 2021 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.vm.memory.DataContainer;

public class Float64ScalarVectorTransferUnit extends AcceleratorExecutionUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		AcceleratorExecutionNode node = null;
		switch (instruction.getOperationCode()) {
			case MOV : {
				Float64x1ScalarCacheSynchronizer synchronizer
						= new Float64x1ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled, 0);
				node = new Float64ScalarVectorMovNode(
						(DataContainer<double[]>)operandContainers[0], (DataContainer<double[]>)operandContainers[1],
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

	private final class Float64ScalarVectorMovNode extends AcceleratorExecutionNode {
		protected final DataContainer<double[]> container0;
		protected final DataContainer<double[]> container1;
		protected final Float64x1ScalarCacheSynchronizer synchronizer;

		public Float64ScalarVectorMovNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1,
				Float64x1ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.container0 = container0;
			this.container1 = container1;
			this.synchronizer = synchronizer;
		}

		public final AcceleratorExecutionNode execute() throws VnanoException {
			if (this.container1.getArraySize() != 1) {
				throw new VnanoException(ErrorType.ARRAY_SIZE_IS_TOO_LARGE_TO_BE_ASSIGNED_TO_SCALAR_VARIABLE);
			}

			double[] data0 = this.container0.getArrayData();
			double[] data1 = this.container1.getArrayData();
			data0[ this.container0.getArrayOffset() ] = data1[ 0 ];

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

}

