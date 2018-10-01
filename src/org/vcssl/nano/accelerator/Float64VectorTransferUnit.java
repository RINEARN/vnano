/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import java.util.Arrays;

import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.processor.OperationCode;

public class Float64VectorTransferUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		AccelerationExecutorNode executor = null;
		switch (opcode) {
			case MOV : {
				Float64x2CacheSynchronizer synchronizer
						= new Float64x2CacheSynchronizer(operandContainers, operandCaches, operandCached);
				executor = new Float64VectorMovExecutor(
						(DataContainer<double[]>)operandContainers[0], (DataContainer<double[]>)operandContainers[1],
						synchronizer, nextNode);
				break;
			}
			case CAST : {
				if (dataTypes[1] == DataType.FLOAT64) {
					Float64x2CacheSynchronizer synchronizer
							= new Float64x2CacheSynchronizer(operandContainers, operandCaches, operandCached);
					executor = new Float64VectorMovExecutor(
							(DataContainer<double[]>)operandContainers[0], (DataContainer<double[]>)operandContainers[1],
							synchronizer, nextNode);
				}
				if (dataTypes[1] == DataType.INT64) {
					Float64x1Int64x1CacheSynchronizer synchronizer
							= new Float64x1Int64x1CacheSynchronizer(operandContainers, operandCaches, operandCached);
					executor = new Float64FromInt64VectorCastExecutor(
							(DataContainer<double[]>)operandContainers[0], (DataContainer<long[]>)operandContainers[1],
							synchronizer, nextNode);
				}
				break;
			}
			case FILL : {
				Float64x2CacheSynchronizer synchronizer
						= new Float64x2CacheSynchronizer(operandContainers, operandCaches, operandCached);
				executor = new Float64VectorFillExecutor(
						(DataContainer<double[]>)operandContainers[0], (DataContainer<double[]>)operandContainers[1],
						synchronizer, nextNode);
				break;
			}
			default : {
				break;
			}
		}
		return executor;
	}

	private final class Float64VectorMovExecutor extends AccelerationExecutorNode {
		protected final DataContainer<double[]> container0;
		protected final DataContainer<double[]> container1;
		protected final Float64x2CacheSynchronizer synchronizer;

		public Float64VectorMovExecutor(
				DataContainer<double[]> container0, DataContainer<double[]> container1,
				Float64x2CacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.synchronizer = synchronizer;
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			double[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			int size = this.container0.getSize();

			System.arraycopy(data1, 0, data0, 0, size);

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64FromInt64VectorCastExecutor extends AccelerationExecutorNode {
		protected final DataContainer<double[]> container0;
		protected final DataContainer<long[]> container1;
		protected final Float64x1Int64x1CacheSynchronizer synchronizer;

		public Float64FromInt64VectorCastExecutor(
				DataContainer<double[]> container0, DataContainer<long[]> container1,
				Float64x1Int64x1CacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.synchronizer = synchronizer;
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			double[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorFillExecutor extends AccelerationExecutorNode {
		protected final DataContainer<double[]> container0;
		protected final DataContainer<double[]> container1;
		protected final Float64x2CacheSynchronizer synchronizer;

		public Float64VectorFillExecutor(
				DataContainer<double[]> container0, DataContainer<double[]> container1,
				Float64x2CacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.synchronizer = synchronizer;
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			double[] data0 = this.container0.getData();
			double fillValue = this.container1.getData()[ this.container1.getOffset() ];

			Arrays.fill(data0, fillValue);

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

}
