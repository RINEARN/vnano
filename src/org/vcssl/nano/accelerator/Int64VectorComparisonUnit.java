/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.processor.OperationCode;

public class Int64VectorComparisonUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant) {

		DataContainer<boolean[]> container0 = (DataContainer<boolean[]>)operandContainers[0];
		DataContainer<long[]> container1 = (DataContainer<long[]>)operandContainers[1];
		DataContainer<long[]> container2 = (DataContainer<long[]>)operandContainers[2];
		Int64x3CacheSynchronizer synchronizer
				= new Int64x3CacheSynchronizer(operandContainers, operandCaches, operandCached);

		Int64VectorComparisonExecutor containers = null;
		switch (opcode) {
			case LT : {
				containers = new Int64VectorLtExecutor(container0, container1, container2, synchronizer);
				break;
			}
			case GT : {
				containers = new Int64VectorGtExecutor(container0, container1, container2, synchronizer);
				break;
			}
			case LEQ : {
				containers = new Int64VectorLeqExecutor(container0, container1, container2, synchronizer);
				break;
			}
			case GEQ : {
				containers = new Int64VectorGeqExecutor(container0, container1, container2, synchronizer);
				break;
			}
			case EQ : {
				containers = new Int64VectorEqExecutor(container0, container1, container2, synchronizer);
				break;
			}
			case NEQ : {
				containers = new Int64VectorNeqExecutor(container0, container1, container2, synchronizer);
				break;
			}
			default : {
				break;
			}
		}
		return containers;
	}

	private abstract class Int64VectorComparisonExecutor extends AccelerationExecutorNode {
		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<long[]> container1;
		protected final DataContainer<long[]> container2;
		protected final Int64x3CacheSynchronizer synchronizer;

		public Int64VectorComparisonExecutor(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3CacheSynchronizer synchronizer) {

			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}
	}

	private final class Int64VectorLtExecutor extends Int64VectorComparisonExecutor {

		public Int64VectorLtExecutor(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3CacheSynchronizer synchronizer) {

			super(container0, container1, container2, synchronizer);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			boolean[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			long[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] < data2[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Int64VectorGtExecutor extends Int64VectorComparisonExecutor {

		public Int64VectorGtExecutor(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3CacheSynchronizer synchronizer) {

			super(container0, container1, container2, synchronizer);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			boolean[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			long[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] > data2[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Int64VectorLeqExecutor extends Int64VectorComparisonExecutor {

		public Int64VectorLeqExecutor(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3CacheSynchronizer synchronizer) {

			super(container0, container1, container2, synchronizer);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			boolean[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			long[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] <= data2[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Int64VectorGeqExecutor extends Int64VectorComparisonExecutor {

		public Int64VectorGeqExecutor(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3CacheSynchronizer synchronizer) {

			super(container0, container1, container2, synchronizer);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			boolean[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			long[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] >= data2[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Int64VectorEqExecutor extends Int64VectorComparisonExecutor {

		public Int64VectorEqExecutor(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3CacheSynchronizer synchronizer) {

			super(container0, container1, container2, synchronizer);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			boolean[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			long[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] == data2[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Int64VectorNeqExecutor extends Int64VectorComparisonExecutor {

		public Int64VectorNeqExecutor(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3CacheSynchronizer synchronizer) {

			super(container0, container1, container2, synchronizer);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			boolean[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			long[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] != data2[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}
}

