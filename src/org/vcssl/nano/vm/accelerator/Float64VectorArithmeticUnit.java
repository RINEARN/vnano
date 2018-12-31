/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class Float64VectorArithmeticUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutorNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		DataContainer<double[]>[] containers = (DataContainer<double[]>[])operandContainers;
		Float64x3ScalarCacheSynchronizer synchronizer
				= new Float64x3ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);

		Float64VectorArithmeticExecutorNode executor = null;
		switch (instruction.getOperationCode()) {
			case ADD : {
				executor = new Float64VectorAddExecutorNode(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case SUB : {
				executor = new Float64VectorSubExecutorNode(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case MUL : {
				executor = new Float64VectorMulExecutorNode(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case DIV : {
				executor = new Float64VectorDivExecutorNode(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case REM : {
				executor = new Float64VectorRemExecutorNode(containers[0], containers[1], containers[2], synchronizer, nextNode);
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

	private abstract class Float64VectorArithmeticExecutorNode extends AccelerationExecutorNode {
		protected final DataContainer<double[]> container0;
		protected final DataContainer<double[]> container1;
		protected final DataContainer<double[]> container2;
		protected final Float64x3ScalarCacheSynchronizer synchronizer;

		public Float64VectorArithmeticExecutorNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}
	}


	private final class Float64VectorAddExecutorNode extends Float64VectorArithmeticExecutorNode {
		public Float64VectorAddExecutorNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

			double[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			final int size = this.container0.getSize();

			for (int i=0; i<size; ++i) {
				data0[i] = data1[i] + data2[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}


	private final class Float64VectorSubExecutorNode extends Float64VectorArithmeticExecutorNode {

		public Float64VectorSubExecutorNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			double[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			final int size = this.container0.getSize();

			for (int i=0; i<size; ++i) {
				data0[i] = data1[i] - data2[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorMulExecutorNode extends Float64VectorArithmeticExecutorNode {

		public Float64VectorMulExecutorNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			double[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			final int size = this.container0.getSize();

			for (int i=0; i<size; ++i) {
				data0[i] = data1[i] * data2[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorDivExecutorNode extends Float64VectorArithmeticExecutorNode {

		public Float64VectorDivExecutorNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			double[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			final int size = this.container0.getSize();

			for (int i=0; i<size; ++i) {
				data0[i] = data1[i] / data2[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorRemExecutorNode extends Float64VectorArithmeticExecutorNode {

		public Float64VectorRemExecutorNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			double[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			final int size = this.container0.getSize();

			for (int i=0; i<size; ++i) {
				data0[i] = data1[i] % data2[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

}

