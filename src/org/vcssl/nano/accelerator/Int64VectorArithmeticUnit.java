/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.memory.DataContainer;

public class Int64VectorArithmeticUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutor(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		DataContainer<long[]>[] containers = (DataContainer<long[]>[])operandContainers;
		Int64x3ScalarCacheSynchronizer synchronizer
				= new Int64x3ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);

		Int64VectorArithmeticExecutor executor = null;
		switch (instruction.getOperationCode()) {
			case ADD : {
				executor = new Int64VectorAddExecutor(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case SUB : {
				executor = new Int64VectorSubExecutor(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case MUL : {
				executor = new Int64VectorMulExecutor(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case DIV : {
				executor = new Int64VectorDivExecutor(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case REM : {
				executor = new Int64VectorRemExecutor(containers[0], containers[1], containers[2], synchronizer, nextNode);
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

	private abstract class Int64VectorArithmeticExecutor extends AccelerationExecutorNode {
		protected final DataContainer<long[]> container0;
		protected final DataContainer<long[]> container1;
		protected final DataContainer<long[]> container2;
		protected final Int64x3ScalarCacheSynchronizer synchronizer;

		public Int64VectorArithmeticExecutor(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}
	}

	private final class Int64VectorAddExecutor extends Int64VectorArithmeticExecutor {

		public Int64VectorAddExecutor(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			long[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			long[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] + data2[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Int64VectorSubExecutor extends Int64VectorArithmeticExecutor {

		public Int64VectorSubExecutor(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			long[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			long[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] - data2[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Int64VectorMulExecutor extends Int64VectorArithmeticExecutor {

		public Int64VectorMulExecutor(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			long[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			long[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] * data2[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Int64VectorDivExecutor extends Int64VectorArithmeticExecutor {

		public Int64VectorDivExecutor(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			long[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			long[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] / data2[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Int64VectorRemExecutor extends Int64VectorArithmeticExecutor {

		public Int64VectorRemExecutor(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			long[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			long[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] % data2[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

}

