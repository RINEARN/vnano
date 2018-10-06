/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.memory.DataContainer;

public class BoolScalarLogicalUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutorNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		DataContainer<boolean[]>[] containers = (DataContainer<boolean[]>[])operandContainers;

		AccelerationExecutorNode executor = null;
		switch (instruction.getOperationCode()) {
			case AND : {
				Boolx3ScalarCacheSynchronizer synchronizer = new Boolx3ScalarCacheSynchronizer(
						operandContainers, operandCaches, operandCached);
				executor = new BoolScalarAndExecutorNode(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case OR : {
				Boolx3ScalarCacheSynchronizer synchronizer = new Boolx3ScalarCacheSynchronizer(
						operandContainers, operandCaches, operandCached);
				executor = new BoolScalarOrExecutorNode(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case NOT : {
				Boolx2ScalarCacheSynchronizer synchronizer = new Boolx2ScalarCacheSynchronizer(
						operandContainers, operandCaches, operandCached);
				executor = new BoolScalarNotExecutorNode(containers[0], containers[1], synchronizer, nextNode);
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

	private abstract class BoolScalarLogicalExecutorNode extends AccelerationExecutorNode {
		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<boolean[]> container1;
		protected final DataContainer<boolean[]> container2;
		protected final CacheSynchronizer synchronizer;

		public BoolScalarLogicalExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1, DataContainer<boolean[]> container2,
				Boolx3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}

		public BoolScalarLogicalExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1,
				Boolx2ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = null;
			this.synchronizer = synchronizer;
		}
	}

	private final class BoolScalarAndExecutorNode extends BoolScalarLogicalExecutorNode {

		public BoolScalarAndExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1, DataContainer<boolean[]> container2,
				Boolx3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] &
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class BoolScalarOrExecutorNode extends BoolScalarLogicalExecutorNode {

		public BoolScalarOrExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1, DataContainer<boolean[]> container2,
				Boolx3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] |
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class BoolScalarNotExecutorNode extends BoolScalarLogicalExecutorNode {

		public BoolScalarNotExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1,
				Boolx2ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			!this.container1.getData()[ this.container1.getOffset() ];
			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}
}
