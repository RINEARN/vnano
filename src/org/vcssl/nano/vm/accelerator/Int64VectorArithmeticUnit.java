/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class Int64VectorArithmeticUnit extends AcceleratorExecutionUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		DataContainer<long[]>[] containers = (DataContainer<long[]>[])operandContainers;

		Int64x2ScalarCacheSynchronizer synchronizerI64x2 = null;
		Int64x3ScalarCacheSynchronizer synchronizerI64x3 = null;
		if (operandContainers.length == 2) {
			synchronizerI64x2 = new Int64x2ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled);
		} else if (operandContainers.length == 3) {
			synchronizerI64x3 = new Int64x3ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled);
		} else {
			throw new VnanoFatalException("Unexpected number of operands detected.");
		}

		Int64VectorArithmeticNode node = null;
		switch (instruction.getOperationCode()) {
			case ADD : {
				node = new Int64VectorAddNode(containers[0], containers[1], containers[2], synchronizerI64x3, nextNode);
				break;
			}
			case SUB : {
				node = new Int64VectorSubNode(containers[0], containers[1], containers[2], synchronizerI64x3, nextNode);
				break;
			}
			case MUL : {
				node = new Int64VectorMulNode(containers[0], containers[1], containers[2], synchronizerI64x3, nextNode);
				break;
			}
			case DIV : {
				node = new Int64VectorDivNode(containers[0], containers[1], containers[2], synchronizerI64x3, nextNode);
				break;
			}
			case REM : {
				node = new Int64VectorRemNode(containers[0], containers[1], containers[2], synchronizerI64x3, nextNode);
				break;
			}
			case NEG : {
				node = new Int64VectorNegNode(containers[0], containers[1], synchronizerI64x2, nextNode);
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

	private abstract class Int64VectorArithmeticNode extends AcceleratorExecutionNode {
		protected final DataContainer<long[]> container0;
		protected final DataContainer<long[]> container1;
		protected final DataContainer<long[]> container2;
		protected final Int64x3ScalarCacheSynchronizer synchronizerI64x3;
		protected final Int64x2ScalarCacheSynchronizer synchronizerI64x2;

		public Int64VectorArithmeticNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizerI64x2 = null;
			this.synchronizerI64x3 = synchronizer;
		}

		public Int64VectorArithmeticNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1,
				Int64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = null;
			this.synchronizerI64x2 = synchronizer;
			this.synchronizerI64x3 = null;
		}
	}

	private final class Int64VectorAddNode extends Int64VectorArithmeticNode {

		public Int64VectorAddNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizerI64x3.synchronizeFromCacheToMemory();
			long[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			long[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] + data2[i];
			}

			this.synchronizerI64x3.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64VectorSubNode extends Int64VectorArithmeticNode {

		public Int64VectorSubNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizerI64x3.synchronizeFromCacheToMemory();
			long[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			long[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] - data2[i];
			}

			this.synchronizerI64x3.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64VectorMulNode extends Int64VectorArithmeticNode {

		public Int64VectorMulNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizerI64x3.synchronizeFromCacheToMemory();
			long[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			long[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] * data2[i];
			}

			this.synchronizerI64x3.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64VectorDivNode extends Int64VectorArithmeticNode {

		public Int64VectorDivNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizerI64x3.synchronizeFromCacheToMemory();
			long[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			long[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] / data2[i];
			}

			this.synchronizerI64x3.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64VectorRemNode extends Int64VectorArithmeticNode {

		public Int64VectorRemNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizerI64x3.synchronizeFromCacheToMemory();
			long[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			long[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] % data2[i];
			}

			this.synchronizerI64x3.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64VectorNegNode extends Int64VectorArithmeticNode {

		public Int64VectorNegNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1,
				Int64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizerI64x2.synchronizeFromCacheToMemory();
			long[] data0 = this.container0.getData();
			long[] data1 = this.container1.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = - data1[i];
			}

			this.synchronizerI64x2.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

}

