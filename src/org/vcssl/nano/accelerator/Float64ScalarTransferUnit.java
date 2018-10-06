/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;

public class Float64ScalarTransferUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutorNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		AccelerationExecutorNode executor = null;
		switch (instruction.getOperationCode()) {
			case MOV :
			case FILL : {
				Float64x2ScalarCacheSynchronizer synchronizer
						= new Float64x2ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);
				executor = new Float64ScalarMovExecutorNode(
						(DataContainer<double[]>)operandContainers[0], (DataContainer<double[]>)operandContainers[1],
						synchronizer, nextNode);
				break;
			}
			case CAST : {
				if (instruction.getDataTypes()[1] == DataType.FLOAT64) {
					Float64x2ScalarCacheSynchronizer synchronizer
							= new Float64x2ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);
					executor = new Float64ScalarMovExecutorNode(
							(DataContainer<double[]>)operandContainers[0], (DataContainer<double[]>)operandContainers[1],
							synchronizer, nextNode);
				} else if (instruction.getDataTypes()[1] == DataType.INT64) {
					Int64x1Float64x1ScalarCacheSynchronizer synchronizer
							= new Int64x1Float64x1ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);
					executor = new Float64FromInt64ScalarCastExecutorNode(
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
		return executor;
	}

	private final class Float64ScalarMovExecutorNode extends AccelerationExecutorNode {

		protected final DataContainer<double[]> container0;
		protected final DataContainer<double[]> container1;
		protected final Float64x2ScalarCacheSynchronizer synchronizer;

		public Float64ScalarMovExecutorNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1,
				Float64x2ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.synchronizer = synchronizer;
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ];
			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64FromInt64ScalarCastExecutorNode extends AccelerationExecutorNode {

		protected final DataContainer<double[]> container0;
		protected final DataContainer<long[]> container1;
		protected final Int64x1Float64x1ScalarCacheSynchronizer synchronizer;

		public Float64FromInt64ScalarCastExecutorNode(
				DataContainer<double[]> container0, DataContainer<long[]> container1,
				Int64x1Float64x1ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.synchronizer = synchronizer;
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ];
			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

}
