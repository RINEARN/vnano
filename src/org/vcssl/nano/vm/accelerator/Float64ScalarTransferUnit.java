/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.vm.memory.DataContainer;

public class Float64ScalarTransferUnit extends AcceleratorExecutionUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		AcceleratorExecutionNode node = null;
		switch (instruction.getOperationCode()) {
			case MOV :
			case FILL : {
				Float64x2ScalarCacheSynchronizer synchronizer
						= new Float64x2ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled);
				node = new Float64ScalarMovNode(
						(DataContainer<double[]>)operandContainers[0], (DataContainer<double[]>)operandContainers[1],
						synchronizer, nextNode);
				break;
			}
			case CAST : {
				if (instruction.getDataTypes()[1] == DataType.FLOAT64) {
					Float64x2ScalarCacheSynchronizer synchronizer
							= new Float64x2ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled);
					node = new Float64ScalarMovNode(
							(DataContainer<double[]>)operandContainers[0], (DataContainer<double[]>)operandContainers[1],
							synchronizer, nextNode);
				} else if (instruction.getDataTypes()[1] == DataType.INT64) {
					Float64x1Int64x1ScalarCacheSynchronizer synchronizer
							= new Float64x1Int64x1ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled);
					node = new Float64FromInt64ScalarCastNode(
							(DataContainer<double[]>)operandContainers[0], (DataContainer<long[]>)operandContainers[1],
							synchronizer, nextNode);
				} else {
					throw new VnanoFatalException(
							instruction.getDataTypes()[1] + "-type operand of " + instruction.getOperationCode()
							+ " instruction is invalid for " + this.getClass().getCanonicalName()
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

	private final class Float64ScalarMovNode extends AcceleratorExecutionNode {

		protected final DataContainer<double[]> container0;
		protected final DataContainer<double[]> container1;
		protected final Float64x2ScalarCacheSynchronizer synchronizer;

		public Float64ScalarMovNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1,
				Float64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.synchronizer = synchronizer;
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ];
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64FromInt64ScalarCastNode extends AcceleratorExecutionNode {

		protected final DataContainer<double[]> container0;
		protected final DataContainer<long[]> container1;
		protected final Float64x1Int64x1ScalarCacheSynchronizer synchronizer;

		public Float64FromInt64ScalarCastNode(
				DataContainer<double[]> container0, DataContainer<long[]> container1,
				Float64x1Int64x1ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.synchronizer = synchronizer;
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ];
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

}
