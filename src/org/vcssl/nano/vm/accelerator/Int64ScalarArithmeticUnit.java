/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class Int64ScalarArithmeticUnit extends AcceleratorExecutionUnit {

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

		AcceleratorExecutionNode node = null;
		switch (instruction.getOperationCode()) {
			case ADD : {
				node = new Int64ScalarAddNode(containers[0], containers[1], containers[2], synchronizerI64x3, nextNode);
				break;
			}
			case SUB : {
				node = new Int64ScalarSubNode(containers[0], containers[1], containers[2], synchronizerI64x3, nextNode);
				break;
			}
			case MUL : {
				node = new Int64ScalarMulNode(containers[0], containers[1], containers[2], synchronizerI64x3, nextNode);
				break;
			}
			case DIV : {
				node = new Int64ScalarDivNode(containers[0], containers[1], containers[2], synchronizerI64x3, nextNode);
				break;
			}
			case REM : {
				node = new Int64ScalarRemNode(containers[0], containers[1], containers[2], synchronizerI64x3, nextNode);
				break;
			}
			case NEG : {
				node = new Int64ScalarNegNode(containers[0], containers[1], synchronizerI64x2, nextNode);
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

	private abstract class Int64ScalarArithmeticNode extends AcceleratorExecutionNode {
		protected final DataContainer<long[]> container0;
		protected final DataContainer<long[]> container1;
		protected final DataContainer<long[]> container2;
		protected final Int64x3ScalarCacheSynchronizer synchronizerI64x3;
		protected final Int64x2ScalarCacheSynchronizer synchronizerI64x2;

		public Int64ScalarArithmeticNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizerI64x2 = null;
			this.synchronizerI64x3 = synchronizer;
		}

		public Int64ScalarArithmeticNode(
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

	private final class Int64ScalarAddNode extends Int64ScalarArithmeticNode {

		public Int64ScalarAddNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizerI64x3.synchronizeFromCacheToMemory();
			this.container0.getArrayData()[ this.container0.getArrayOffset() ] =
			this.container1.getArrayData()[ this.container1.getArrayOffset() ] +
			this.container2.getArrayData()[ this.container2.getArrayOffset() ] ;
			this.synchronizerI64x3.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64ScalarSubNode extends Int64ScalarArithmeticNode {

		public Int64ScalarSubNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizerI64x3.synchronizeFromCacheToMemory();
			this.container0.getArrayData()[ this.container0.getArrayOffset() ] =
			this.container1.getArrayData()[ this.container1.getArrayOffset() ] -
			this.container2.getArrayData()[ this.container2.getArrayOffset() ] ;
			this.synchronizerI64x3.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64ScalarMulNode extends Int64ScalarArithmeticNode {

		public Int64ScalarMulNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizerI64x3.synchronizeFromCacheToMemory();
			this.container0.getArrayData()[ this.container0.getArrayOffset() ] =
			this.container1.getArrayData()[ this.container1.getArrayOffset() ] *
			this.container2.getArrayData()[ this.container2.getArrayOffset() ] ;
			this.synchronizerI64x3.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64ScalarDivNode extends Int64ScalarArithmeticNode {

		public Int64ScalarDivNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizerI64x3.synchronizeFromCacheToMemory();
			this.container0.getArrayData()[ this.container0.getArrayOffset() ] =
			this.container1.getArrayData()[ this.container1.getArrayOffset() ] /
			this.container2.getArrayData()[ this.container2.getArrayOffset() ] ;
			this.synchronizerI64x3.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64ScalarRemNode extends Int64ScalarArithmeticNode {

		public Int64ScalarRemNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizerI64x3.synchronizeFromCacheToMemory();
			this.container0.getArrayData()[ this.container0.getArrayOffset() ] =
			this.container1.getArrayData()[ this.container1.getArrayOffset() ] %
			this.container2.getArrayData()[ this.container2.getArrayOffset() ] ;
			this.synchronizerI64x3.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64ScalarNegNode extends Int64ScalarArithmeticNode {

		public Int64ScalarNegNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1,
				Int64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizerI64x2.synchronizeFromCacheToMemory();
			this.container0.getArrayData()[ this.container0.getArrayOffset() ] = - this.container1.getArrayData()[ this.container1.getArrayOffset() ];
			this.synchronizerI64x2.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

}
