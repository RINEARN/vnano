/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class Float64VectorArithmeticUnit extends AcceleratorExecutionUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		DataContainer<double[]>[] containers = (DataContainer<double[]>[])operandContainers;

		Float64x2ScalarCacheSynchronizer synchronizerF64x2 = null;
		Float64x3ScalarCacheSynchronizer synchronizerF64x3 = null;
		if (operandContainers.length == 2) {
			synchronizerF64x2 = new Float64x2ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled);
		} else if (operandContainers.length == 3) {
			synchronizerF64x3 = new Float64x3ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled);
		} else {
			throw new VnanoFatalException("Unexpected number of operands detected.");
		}

		Float64VectorArithmeticNode node = null;
		switch (instruction.getOperationCode()) {
			case ADD : {
				node = new Float64VectorAddNode(containers[0], containers[1], containers[2], synchronizerF64x3, nextNode);
				break;
			}
			case SUB : {
				node = new Float64VectorSubNode(containers[0], containers[1], containers[2], synchronizerF64x3, nextNode);
				break;
			}
			case MUL : {
				node = new Float64VectorMulNode(containers[0], containers[1], containers[2], synchronizerF64x3, nextNode);
				break;
			}
			case DIV : {
				node = new Float64VectorDivNode(containers[0], containers[1], containers[2], synchronizerF64x3, nextNode);
				break;
			}
			case REM : {
				node = new Float64VectorRemNode(containers[0], containers[1], containers[2], synchronizerF64x3, nextNode);
				break;
			}
			case NEG : {
				node = new Float64VectorNegNode(containers[0], containers[1], synchronizerF64x2, nextNode);
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

	private abstract class Float64VectorArithmeticNode extends AcceleratorExecutionNode {
		protected final DataContainer<double[]> container0;
		protected final DataContainer<double[]> container1;
		protected final DataContainer<double[]> container2;
		protected final Float64x3ScalarCacheSynchronizer synchronizerF64x3;
		protected final Float64x2ScalarCacheSynchronizer synchronizerF64x2;

		public Float64VectorArithmeticNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizerF64x2 = null;
			this.synchronizerF64x3 = synchronizer;
		}

		public Float64VectorArithmeticNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1,
				Float64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = null;
			this.synchronizerF64x2 = synchronizer;
			this.synchronizerF64x3 = null;
		}
	}


	private final class Float64VectorAddNode extends Float64VectorArithmeticNode {
		public Float64VectorAddNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			double[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			final int size = this.container0.getSize();

			for (int i=0; i<size; ++i) {
				data0[i] = data1[i] + data2[i];
			}

			this.synchronizerF64x3.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}


	private final class Float64VectorSubNode extends Float64VectorArithmeticNode {

		public Float64VectorSubNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizerF64x3.synchronizeFromCacheToMemory();
			double[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			final int size = this.container0.getSize();

			for (int i=0; i<size; ++i) {
				data0[i] = data1[i] - data2[i];
			}

			this.synchronizerF64x3.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorMulNode extends Float64VectorArithmeticNode {

		public Float64VectorMulNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizerF64x3.synchronizeFromCacheToMemory();
			double[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			final int size = this.container0.getSize();

			for (int i=0; i<size; ++i) {
				data0[i] = data1[i] * data2[i];
			}

			this.synchronizerF64x3.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorDivNode extends Float64VectorArithmeticNode {

		public Float64VectorDivNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizerF64x3.synchronizeFromCacheToMemory();
			double[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			final int size = this.container0.getSize();

			for (int i=0; i<size; ++i) {
				data0[i] = data1[i] / data2[i];
			}

			this.synchronizerF64x3.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorRemNode extends Float64VectorArithmeticNode {

		public Float64VectorRemNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizerF64x3.synchronizeFromCacheToMemory();
			double[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			final int size = this.container0.getSize();

			for (int i=0; i<size; ++i) {
				data0[i] = data1[i] % data2[i];
			}

			this.synchronizerF64x3.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorNegNode extends Float64VectorArithmeticNode {

		public Float64VectorNegNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1,
				Float64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizerF64x2.synchronizeFromCacheToMemory();
			double[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			final int size = this.container0.getSize();

			for (int i=0; i<size; ++i) {
				data0[i] = - data1[i];
			}

			this.synchronizerF64x2.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

}

