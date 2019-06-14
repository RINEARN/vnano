/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class Float64VectorComparisonUnit extends AcceleratorExecutionUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		DataContainer<boolean[]> container0 = (DataContainer<boolean[]>)operandContainers[0];
		DataContainer<double[]> container1 = (DataContainer<double[]>)operandContainers[1];
		DataContainer<double[]> container2 = (DataContainer<double[]>)operandContainers[2];
		Float64x3ScalarCacheSynchronizer synchronizer
				= new Float64x3ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);

		Float64VectorComparisonNode node = null;
		switch (instruction.getOperationCode()) {
			case LT : {
				node = new Float64VectorLtNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case GT : {
				node = new Float64VectorGtNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case LEQ : {
				node = new Float64VectorLeqNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case GEQ : {
				node = new Float64VectorGeqNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case EQ : {
				node = new Float64VectorEqNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case NEQ : {
				node = new Float64VectorNeqNode(container0, container1, container2, synchronizer, nextNode);
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

	private abstract class Float64VectorComparisonNode extends AcceleratorExecutionNode {
		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<double[]> container1;
		protected final DataContainer<double[]> container2;
		protected final Float64x3ScalarCacheSynchronizer synchronizer;

		public Float64VectorComparisonNode(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}
	}

	private final class Float64VectorLtNode extends Float64VectorComparisonNode {

		public Float64VectorLtNode(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			boolean[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] < data2[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorGtNode extends Float64VectorComparisonNode {

		public Float64VectorGtNode(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			boolean[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] > data2[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorLeqNode extends Float64VectorComparisonNode {

		public Float64VectorLeqNode(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			boolean[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] <= data2[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorGeqNode extends Float64VectorComparisonNode {

		public Float64VectorGeqNode(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			boolean[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] >= data2[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorEqNode extends Float64VectorComparisonNode {

		public Float64VectorEqNode(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			boolean[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] == data2[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorNeqNode extends Float64VectorComparisonNode {

		public Float64VectorNeqNode(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			boolean[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] != data2[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}
}

