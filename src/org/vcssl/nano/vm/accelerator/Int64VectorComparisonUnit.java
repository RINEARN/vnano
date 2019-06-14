/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class Int64VectorComparisonUnit extends AcceleratorExecutionUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		DataContainer<boolean[]> container0 = (DataContainer<boolean[]>)operandContainers[0];
		DataContainer<long[]> container1 = (DataContainer<long[]>)operandContainers[1];
		DataContainer<long[]> container2 = (DataContainer<long[]>)operandContainers[2];
		Int64x3ScalarCacheSynchronizer synchronizer
				= new Int64x3ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled);

		Int64VectorComparisonNode node = null;
		switch (instruction.getOperationCode()) {
			case LT : {
				node = new Int64VectorLtNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case GT : {
				node = new Int64VectorGtNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case LEQ : {
				node = new Int64VectorLeqNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case GEQ : {
				node = new Int64VectorGeqNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case EQ : {
				node = new Int64VectorEqNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case NEQ : {
				node = new Int64VectorNeqNode(container0, container1, container2, synchronizer, nextNode);
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

	private abstract class Int64VectorComparisonNode extends AcceleratorExecutionNode {
		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<long[]> container1;
		protected final DataContainer<long[]> container2;
		protected final Int64x3ScalarCacheSynchronizer synchronizer;

		public Int64VectorComparisonNode(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}
	}

	private final class Int64VectorLtNode extends Int64VectorComparisonNode {

		public Int64VectorLtNode(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			boolean[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			long[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] < data2[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64VectorGtNode extends Int64VectorComparisonNode {

		public Int64VectorGtNode(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			boolean[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			long[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] > data2[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64VectorLeqNode extends Int64VectorComparisonNode {

		public Int64VectorLeqNode(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			boolean[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			long[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] <= data2[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64VectorGeqNode extends Int64VectorComparisonNode {

		public Int64VectorGeqNode(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			boolean[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			long[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] >= data2[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64VectorEqNode extends Int64VectorComparisonNode {

		public Int64VectorEqNode(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			boolean[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			long[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] == data2[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64VectorNeqNode extends Int64VectorComparisonNode {

		public Int64VectorNeqNode(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			boolean[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			long[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] != data2[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}
}

