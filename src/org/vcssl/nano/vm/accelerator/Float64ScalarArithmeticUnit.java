/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class Float64ScalarArithmeticUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutorNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		DataContainer<double[]>[] containers = (DataContainer<double[]>[])operandContainers;
		Float64x3ScalarCacheSynchronizer synchronizer =
				new Float64x3ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);

		AccelerationExecutorNode executor = null;
		switch (instruction.getOperationCode()) {
			case ADD : {
				executor = new Float64ScalarAddExecutorNode(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case SUB : {
				executor = new Float64ScalarSubExecutorNode(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case MUL : {
				executor = new Float64ScalarMulExecutorNode(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case DIV : {
				executor = new Float64ScalarDivExecutorNode(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case REM : {
				executor = new Float64ScalarRemExecutorNode(containers[0], containers[1], containers[2], synchronizer, nextNode);
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

	private abstract class Float64ScalarArithmeticExecutorNode extends AccelerationExecutorNode {
		protected final DataContainer<double[]> container0;
		protected final DataContainer<double[]> container1;
		protected final DataContainer<double[]> container2;
		protected final Float64x3ScalarCacheSynchronizer synchronizer;

		public Float64ScalarArithmeticExecutorNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}
	}

	private final class Float64ScalarAddExecutorNode extends Float64ScalarArithmeticExecutorNode {

		public Float64ScalarAddExecutorNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] +
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64ScalarSubExecutorNode extends Float64ScalarArithmeticExecutorNode {

		public Float64ScalarSubExecutorNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] -
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64ScalarMulExecutorNode extends Float64ScalarArithmeticExecutorNode {

		public Float64ScalarMulExecutorNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] *
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64ScalarDivExecutorNode extends Float64ScalarArithmeticExecutorNode {

		public Float64ScalarDivExecutorNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] /
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64ScalarRemExecutorNode extends Float64ScalarArithmeticExecutorNode {

		public Float64ScalarRemExecutorNode(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] %
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

}
