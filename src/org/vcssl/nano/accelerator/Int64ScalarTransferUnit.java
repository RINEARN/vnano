/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.processor.OperationCode;

public class Int64ScalarTransferUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		AccelerationExecutorNode executor = null;
		switch (opcode) {
			case MOV :
			case FILL : {
				Int64x2ScalarCacheSynchronizer synchronizer
						= new Int64x2ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);
				executor = new Int64ScalarMovExecutor(
						(DataContainer<long[]>)operandContainers[0], (DataContainer<long[]>)operandContainers[1],
						synchronizer, nextNode);
				break;
			}
			case CAST : {
				if (dataTypes[1] == DataType.INT64) {
					Int64x2ScalarCacheSynchronizer synchronizer
							= new Int64x2ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);
					executor = new Int64ScalarMovExecutor(
							(DataContainer<long[]>)operandContainers[0], (DataContainer<long[]>)operandContainers[1],
							synchronizer, nextNode);
				}
				if (dataTypes[1] == DataType.FLOAT64) {
					Int64x1Float64x1ScalarCacheSynchronizer synchronizer
							= new Int64x1Float64x1ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);
					executor = new Int64FromFloat64ScalarCastExecutor(
							(DataContainer<long[]>)operandContainers[0], (DataContainer<double[]>)operandContainers[1],
							synchronizer, nextNode);
				}
				break;
			}

			default : {
				break;
			}
		}
		return executor;
	}

	private final class Int64ScalarMovExecutor extends AccelerationExecutorNode {

		protected final DataContainer<long[]> container0;
		protected final DataContainer<long[]> container1;
		protected final Int64x2ScalarCacheSynchronizer synchronizer;

		public Int64ScalarMovExecutor(
				DataContainer<long[]> container0, DataContainer<long[]> container1,
				Int64x2ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

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

	private final class Int64FromFloat64ScalarCastExecutor extends AccelerationExecutorNode {

		protected final DataContainer<long[]> container0;
		protected final DataContainer<double[]> container1;
		protected final Int64x1Float64x1ScalarCacheSynchronizer synchronizer;

		public Int64FromFloat64ScalarCastExecutor(
				DataContainer<long[]> container0, DataContainer<double[]> container1,
				Int64x1Float64x1ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.synchronizer = synchronizer;
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			(long)this.container1.getData()[ this.container1.getOffset() ];
			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

}
