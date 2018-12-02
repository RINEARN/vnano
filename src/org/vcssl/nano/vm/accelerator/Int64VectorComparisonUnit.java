/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class Int64VectorComparisonUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutorNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		DataContainer<boolean[]> container0 = (DataContainer<boolean[]>)operandContainers[0];
		DataContainer<long[]> container1 = (DataContainer<long[]>)operandContainers[1];
		DataContainer<long[]> container2 = (DataContainer<long[]>)operandContainers[2];
		Int64x3ScalarCacheSynchronizer synchronizer
				= new Int64x3ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);

		Int64VectorComparisonExecutorNode containers = null;
		switch (instruction.getOperationCode()) {
			case LT : {
				containers = new Int64VectorLtExecutorNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case GT : {
				containers = new Int64VectorGtExecutorNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case LEQ : {
				containers = new Int64VectorLeqExecutorNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case GEQ : {
				containers = new Int64VectorGeqExecutorNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case EQ : {
				containers = new Int64VectorEqExecutorNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case NEQ : {
				containers = new Int64VectorNeqExecutorNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			default : {
				throw new VnanoFatalException(
						"Operation code " + instruction.getOperationCode() + " is invalid for " + this.getClass().getCanonicalName()
				);
			}
		}
		return containers;
	}

	private abstract class Int64VectorComparisonExecutorNode extends AccelerationExecutorNode {
		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<long[]> container1;
		protected final DataContainer<long[]> container2;
		protected final Int64x3ScalarCacheSynchronizer synchronizer;

		public Int64VectorComparisonExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}
	}

	private final class Int64VectorLtExecutorNode extends Int64VectorComparisonExecutorNode {

		public Int64VectorLtExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
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

	private final class Int64VectorGtExecutorNode extends Int64VectorComparisonExecutorNode {

		public Int64VectorGtExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
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

	private final class Int64VectorLeqExecutorNode extends Int64VectorComparisonExecutorNode {

		public Int64VectorLeqExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
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

	private final class Int64VectorGeqExecutorNode extends Int64VectorComparisonExecutorNode {

		public Int64VectorGeqExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
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

	private final class Int64VectorEqExecutorNode extends Int64VectorComparisonExecutorNode {

		public Int64VectorEqExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
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

	private final class Int64VectorNeqExecutorNode extends Int64VectorComparisonExecutorNode {

		public Int64VectorNeqExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
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

