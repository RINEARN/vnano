/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.processor.OperationCode;

public class BoolScalarLogicalUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant) {

		DataContainer<boolean[]>[] containers = (DataContainer<boolean[]>[])operandContainers;

		AccelerationExecutorNode executor = null;
		switch (opcode) {
			case AND : {
				Boolx3CacheSynchronizer synchronizer = new Boolx3CacheSynchronizer(
						operandContainers, operandCaches, operandCached);
				executor = new BoolScalarAndExecutor(containers[0], containers[1], containers[2], synchronizer);
				break;
			}
			case OR : {
				Boolx3CacheSynchronizer synchronizer = new Boolx3CacheSynchronizer(
						operandContainers, operandCaches, operandCached);
				executor = new BoolScalarOrExecutor(containers[0], containers[1], containers[2], synchronizer);
				break;
			}
			case NOT : {
				Boolx2CacheSynchronizer synchronizer = new Boolx2CacheSynchronizer(
						operandContainers, operandCaches, operandCached);
				executor = new BoolScalarNotExecutor(containers[0], containers[1], synchronizer);
				break;
			}
			default : {
				break;
			}
		}
		return executor;
	}

	private abstract class BoolScalarLogicalExecutor extends AccelerationExecutorNode {
		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<boolean[]> container1;
		protected final DataContainer<boolean[]> container2;
		protected final CacheSynchronizer synchronizer;

		public BoolScalarLogicalExecutor(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1, DataContainer<boolean[]> container2,
				Boolx3CacheSynchronizer synchronizer) {

			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}

		public BoolScalarLogicalExecutor(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1,
				Boolx2CacheSynchronizer synchronizer) {

			this.container0 = container0;
			this.container1 = container1;
			this.container2 = null;
			this.synchronizer = synchronizer;
		}
	}

	private final class BoolScalarAndExecutor extends BoolScalarLogicalExecutor {

		public BoolScalarAndExecutor(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1, DataContainer<boolean[]> container2,
				Boolx3CacheSynchronizer synchronizer) {

			super(container0, container1, container2, synchronizer);
		}

		public final int execute(int programCounter) {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] &
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return programCounter + 1;
		}
	}

	private final class BoolScalarOrExecutor extends BoolScalarLogicalExecutor {

		public BoolScalarOrExecutor(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1, DataContainer<boolean[]> container2,
				Boolx3CacheSynchronizer synchronizer) {

			super(container0, container1, container2, synchronizer);
		}

		public final int execute(int programCounter) {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] |
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return programCounter + 1;
		}
	}

	private final class BoolScalarNotExecutor extends BoolScalarLogicalExecutor {

		public BoolScalarNotExecutor(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1,
				Boolx2CacheSynchronizer synchronizer) {

			super(container0, container1, synchronizer);
		}

		public final int execute(int programCounter) {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			!this.container1.getData()[ this.container1.getOffset() ];
			this.synchronizer.writeCache();
			return programCounter + 1;
		}
	}
}
