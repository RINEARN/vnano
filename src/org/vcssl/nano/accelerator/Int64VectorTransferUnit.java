/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import java.util.Arrays;

import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.processor.OperationCode;

public class Int64VectorTransferUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		AccelerationExecutorNode executor = null;
		switch (opcode) {
			case MOV : {
				Int64x2CacheSynchronizer synchronizer
						= new Int64x2CacheSynchronizer(operandContainers, operandCaches, operandCached);
				executor = new Int64VectorMovExecutor(
						(DataContainer<long[]>)operandContainers[0], (DataContainer<long[]>)operandContainers[1],
						synchronizer, nextNode);
				break;
			}
			case CAST : {
				if (dataTypes[1] == DataType.INT64) {
					Int64x2CacheSynchronizer synchronizer
							= new Int64x2CacheSynchronizer(operandContainers, operandCaches, operandCached);
					executor = new Int64VectorMovExecutor(
							(DataContainer<long[]>)operandContainers[0], (DataContainer<long[]>)operandContainers[1],
							synchronizer, nextNode);
				}
				if (dataTypes[1] == DataType.FLOAT64) {
					Int64x1Float64x1CacheSynchronizer synchronizer
							= new Int64x1Float64x1CacheSynchronizer(operandContainers, operandCaches, operandCached);
					executor = new Int64FromFloat64VectorCastExecutor(
							(DataContainer<long[]>)operandContainers[0], (DataContainer<double[]>)operandContainers[1],
							synchronizer, nextNode);
				}
				break;
			}
			case FILL : {
				Int64x2CacheSynchronizer synchronizer
						= new Int64x2CacheSynchronizer(operandContainers, operandCaches, operandCached);
				executor = new Int64VectorFillExecutor(
						(DataContainer<long[]>)operandContainers[0], (DataContainer<long[]>)operandContainers[1],
						synchronizer, nextNode);
				break;
			}
			default : {
				break;
			}
		}
		return executor;
	}

	private final class Int64VectorMovExecutor extends AccelerationExecutorNode {
		protected final DataContainer<long[]> container0;
		protected final DataContainer<long[]> container1;
		protected final Int64x2CacheSynchronizer synchronizer;

		public Int64VectorMovExecutor(
				DataContainer<long[]> container0, DataContainer<long[]> container1,
				Int64x2CacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.synchronizer = synchronizer;
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			long[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			int size = this.container0.getSize();

			System.arraycopy(data1, 0, data0, 0, size);

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Int64FromFloat64VectorCastExecutor extends AccelerationExecutorNode {
		protected final DataContainer<long[]> container0;
		protected final DataContainer<double[]> container1;
		protected final Int64x1Float64x1CacheSynchronizer synchronizer;

		public Int64FromFloat64VectorCastExecutor(
				DataContainer<long[]> container0, DataContainer<double[]> container1,
				Int64x1Float64x1CacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.synchronizer = synchronizer;
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			long[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = (long)data1[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Int64VectorFillExecutor extends AccelerationExecutorNode {
		protected final DataContainer<long[]> container0;
		protected final DataContainer<long[]> container1;
		protected final Int64x2CacheSynchronizer synchronizer;

		public Int64VectorFillExecutor(
				DataContainer<long[]> container0, DataContainer<long[]> container1,
				Int64x2CacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.synchronizer = synchronizer;
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			long[] data0 = this.container0.getData();
			long fillValue = this.container1.getData()[ this.container1.getOffset() ];

			Arrays.fill(data0, fillValue);

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}


}

