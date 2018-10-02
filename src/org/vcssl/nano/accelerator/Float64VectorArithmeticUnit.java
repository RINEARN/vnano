/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.processor.OperationCode;

public class Float64VectorArithmeticUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		DataContainer<double[]>[] containers = (DataContainer<double[]>[])operandContainers;
		Float64x3ScalarCacheSynchronizer synchronizer
				= new Float64x3ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);

		Float64VectorArithmeticExecutor executor = null;
		switch (opcode) {
			case ADD : {
				executor = new Float64VectorAddExecutor(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case SUB : {
				executor = new Float64VectorSubExecutor(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case MUL : {
				executor = new Float64VectorMulExecutor(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case DIV : {
				executor = new Float64VectorDivExecutor(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case REM : {
				executor = new Float64VectorRemExecutor(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			default : {
				break;
			}
		}
		return executor;
	}

	private abstract class Float64VectorArithmeticExecutor extends AccelerationExecutorNode {
		protected final DataContainer<double[]> container0;
		protected final DataContainer<double[]> container1;
		protected final DataContainer<double[]> container2;
		protected final Float64x3ScalarCacheSynchronizer synchronizer;

		public Float64VectorArithmeticExecutor(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}
	}


	private final class Float64VectorAddExecutor extends Float64VectorArithmeticExecutor {
		public Float64VectorAddExecutor(
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}


	private final class Float64VectorSubExecutor extends Float64VectorArithmeticExecutor {

		public Float64VectorSubExecutor(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			double[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			final int size = this.container0.getSize();

			for (int i=0; i<size; ++i) {
				data0[i] = data1[i] - data2[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorMulExecutor extends Float64VectorArithmeticExecutor {

		public Float64VectorMulExecutor(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			double[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			final int size = this.container0.getSize();

			for (int i=0; i<size; ++i) {
				data0[i] = data1[i] * data2[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorDivExecutor extends Float64VectorArithmeticExecutor {

		public Float64VectorDivExecutor(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			double[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			final int size = this.container0.getSize();

			for (int i=0; i<size; ++i) {
				data0[i] = data1[i] / data2[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorRemExecutor extends Float64VectorArithmeticExecutor {

		public Float64VectorRemExecutor(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			double[] data0 = this.container0.getData();
			double[] data1 = this.container1.getData();
			double[] data2 = this.container2.getData();
			final int size = this.container0.getSize();

			for (int i=0; i<size; ++i) {
				data0[i] = data1[i] % data2[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

}

