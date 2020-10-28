/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class Float64ScalarArithmeticUnit extends AcceleratorExecutionUnit {

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

		AcceleratorExecutionNode node = null;
		switch (instruction.getOperationCode()) {
			case ADD : {
				node = new Float64ScalarAddNode(containers[0], containers[1], containers[2], synchronizerF64x3, nextNode);
				break;
			}
			case SUB : {
				node = new Float64ScalarSubNode(containers[0], containers[1], containers[2], synchronizerF64x3, nextNode);
				break;
			}
			case MUL : {
				node = new Float64ScalarMulNode(containers[0], containers[1], containers[2], synchronizerF64x3, nextNode);
				break;
			}
			case DIV : {
				node = new Float64ScalarDivNode(containers[0], containers[1], containers[2], synchronizerF64x3, nextNode);
				break;
			}
			case REM : {
				node = new Float64ScalarRemNode(containers[0], containers[1], containers[2], synchronizerF64x3, nextNode);
				break;
			}
			case NEG : {
				node = new Float64ScalarNegNode(containers[0], containers[1], synchronizerF64x2, nextNode);
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

	private abstract class Float64ScalarArithmeticNode extends AcceleratorExecutionNode {
		protected final DataContainer<double[]> container0;
		protected final DataContainer<double[]> container1;
		protected final DataContainer<double[]> container2;
		protected final Float64x2ScalarCacheSynchronizer synchronizerF64x2;
		protected final Float64x3ScalarCacheSynchronizer synchronizerF64x3;

		public Float64ScalarArithmeticNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizerF64x2 = null;
			this.synchronizerF64x3 = synchronizer;
		}

		public Float64ScalarArithmeticNode(
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

	private final class Float64ScalarAddNode extends Float64ScalarArithmeticNode {

		public Float64ScalarAddNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizerF64x3.synchronizeFromCacheToMemory();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] +
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizerF64x3.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64ScalarSubNode extends Float64ScalarArithmeticNode {

		public Float64ScalarSubNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizerF64x3.synchronizeFromCacheToMemory();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] -
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizerF64x3.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64ScalarMulNode extends Float64ScalarArithmeticNode {

		public Float64ScalarMulNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizerF64x3.synchronizeFromCacheToMemory();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] *
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizerF64x3.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64ScalarDivNode extends Float64ScalarArithmeticNode {

		public Float64ScalarDivNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizerF64x3.synchronizeFromCacheToMemory();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] /
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizerF64x3.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64ScalarRemNode extends Float64ScalarArithmeticNode {

		public Float64ScalarRemNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizerF64x3.synchronizeFromCacheToMemory();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] %
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizerF64x3.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64ScalarNegNode extends Float64ScalarArithmeticNode {

		public Float64ScalarNegNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1,
				Float64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizerF64x2.synchronizeFromCacheToMemory();
			this.container0.getData()[ this.container0.getOffset() ] = - this.container1.getData()[ this.container1.getOffset() ];
			this.synchronizerF64x2.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

}
