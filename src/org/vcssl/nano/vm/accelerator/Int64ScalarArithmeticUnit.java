/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class Int64ScalarArithmeticUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutorNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		DataContainer<long[]>[] containers = (DataContainer<long[]>[])operandContainers;
		Int64x3ScalarCacheSynchronizer synchronizer
				= new Int64x3ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);

		AccelerationExecutorNode executor = null;
		switch (instruction.getOperationCode()) {
			case ADD : {
				executor = new Int64ScalarAddExecutorNode(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case SUB : {
				executor = new Int64ScalarSubExecutorNode(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case MUL : {
				executor = new Int64ScalarMulExecutorNode(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case DIV : {
				executor = new Int64ScalarDivExecutorNode(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case REM : {
				executor = new Int64ScalarRemExecutorNode(containers[0], containers[1], containers[2], synchronizer, nextNode);
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

	private abstract class Int64ScalarArithmeticExecutorNode extends AccelerationExecutorNode {
		protected final DataContainer<long[]> container0;
		protected final DataContainer<long[]> container1;
		protected final DataContainer<long[]> container2;
		protected final Int64x3ScalarCacheSynchronizer synchronizer;

		public Int64ScalarArithmeticExecutorNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}
	}

	private final class Int64ScalarAddExecutorNode extends Int64ScalarArithmeticExecutorNode {

		public Int64ScalarAddExecutorNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] +
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Int64ScalarSubExecutorNode extends Int64ScalarArithmeticExecutorNode {

		public Int64ScalarSubExecutorNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] -
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Int64ScalarMulExecutorNode extends Int64ScalarArithmeticExecutorNode {

		public Int64ScalarMulExecutorNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] *
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Int64ScalarDivExecutorNode extends Int64ScalarArithmeticExecutorNode {

		public Int64ScalarDivExecutorNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] /
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Int64ScalarRemExecutorNode extends Int64ScalarArithmeticExecutorNode {

		public Int64ScalarRemExecutorNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Int64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] %
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

}
