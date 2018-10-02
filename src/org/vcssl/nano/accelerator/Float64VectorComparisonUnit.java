/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.processor.OperationCode;

public class Float64VectorComparisonUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		DataContainer<boolean[]> container0 = (DataContainer<boolean[]>)operandContainers[0];
		DataContainer<double[]> container1 = (DataContainer<double[]>)operandContainers[1];
		DataContainer<double[]> container2 = (DataContainer<double[]>)operandContainers[2];
		Float64x3ScalarCacheSynchronizer synchronizer
				= new Float64x3ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);

		Float64VectorComparisonExecutor executor = null;
		switch (opcode) {
			case LT : {
				executor = new Float64VectorLtExecutor(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case GT : {
				executor = new Float64VectorGtExecutor(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case LEQ : {
				executor = new Float64VectorLeqExecutor(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case GEQ : {
				executor = new Float64VectorGeqExecutor(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case EQ : {
				executor = new Float64VectorEqExecutor(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case NEQ : {
				executor = new Float64VectorNeqExecutor(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			default : {
				break;
			}
		}
		return executor;
	}

	private abstract class Float64VectorComparisonExecutor extends AccelerationExecutorNode {
		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<double[]> container1;
		protected final DataContainer<double[]> container2;
		protected final Float64x3ScalarCacheSynchronizer synchronizer;

		public Float64VectorComparisonExecutor(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}
	}

	private final class Float64VectorLtExecutor extends Float64VectorComparisonExecutor {

		public Float64VectorLtExecutor(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			boolean[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] < data2[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorGtExecutor extends Float64VectorComparisonExecutor {

		public Float64VectorGtExecutor(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			boolean[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] > data2[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorLeqExecutor extends Float64VectorComparisonExecutor {

		public Float64VectorLeqExecutor(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			boolean[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] <= data2[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorGeqExecutor extends Float64VectorComparisonExecutor {

		public Float64VectorGeqExecutor(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			boolean[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] >= data2[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorEqExecutor extends Float64VectorComparisonExecutor {

		public Float64VectorEqExecutor(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			boolean[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] == data2[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorNeqExecutor extends Float64VectorComparisonExecutor {

		public Float64VectorNeqExecutor(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			boolean[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] != data2[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}
}

