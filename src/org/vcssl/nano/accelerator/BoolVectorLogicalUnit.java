/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.processor.OperationCode;

public class BoolVectorLogicalUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant) {

		DataContainer<boolean[]>[] containers = (DataContainer<boolean[]>[])operandContainers;

		BoolVectorLogicalExecutor executor = null;
		switch (opcode) {
			case AND : {
				Boolx3CacheSynchronizer synchronizer = new Boolx3CacheSynchronizer(
						operandContainers, operandCaches, operandCached);
				executor = new BoolVectorAndExecutor(containers[0], containers[1], containers[2], synchronizer);
				break;
			}
			case OR : {
				Boolx3CacheSynchronizer synchronizer = new Boolx3CacheSynchronizer(
						operandContainers, operandCaches, operandCached);
				executor = new BoolVectorOrExecutor(containers[0], containers[1], containers[2], synchronizer);
				break;
			}
			case NOT : {
				Boolx2CacheSynchronizer synchronizer = new Boolx2CacheSynchronizer(
						operandContainers, operandCaches, operandCached);
				executor = new BoolVectorNotExecutor(containers[0], containers[1], synchronizer);
				break;
			}
			default : {
				break;
			}
		}
		return executor;
	}

	private abstract class BoolVectorLogicalExecutor extends AccelerationExecutorNode {
		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<boolean[]> container1;
		protected final DataContainer<boolean[]> container2;
		protected final CacheSynchronizer synchronizer;

		public BoolVectorLogicalExecutor(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1, DataContainer<boolean[]> container2,
				Boolx3CacheSynchronizer synchronizer) {

			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}
		public BoolVectorLogicalExecutor(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1,
				Boolx2CacheSynchronizer synchronizer) {

			this.container0 = container0;
			this.container1 = container1;
			this.container2 = null;
			this.synchronizer = synchronizer;
		}
	}

	private final class BoolVectorAndExecutor extends BoolVectorLogicalExecutor {

		public BoolVectorAndExecutor(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1, DataContainer<boolean[]> container2,
				Boolx3CacheSynchronizer synchronizer) {

			super(container0, container1, container2, synchronizer);
		}

		public final int execute(int programCounter) {
			this.synchronizer.readCache();
			boolean[] data0 = this.container0.getData();
			boolean[] data1 = this.container1.getData();
			boolean[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] & data2[i];
			}

			this.synchronizer.writeCache();
			return programCounter + 1;
		}
	}

	private final class BoolVectorOrExecutor extends BoolVectorLogicalExecutor {

		public BoolVectorOrExecutor(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1, DataContainer<boolean[]> container2,
				Boolx3CacheSynchronizer synchronizer) {

			super(container0, container1, container2, synchronizer);
		}

		public final int execute(int programCounter) {
			this.synchronizer.readCache();
			boolean[] data0 = this.container0.getData();
			boolean[] data1 = this.container1.getData();
			boolean[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = data1[i] | data2[i];
			}

			this.synchronizer.writeCache();
			return programCounter + 1;
		}
	}

	private final class BoolVectorNotExecutor extends BoolVectorLogicalExecutor {

		public BoolVectorNotExecutor(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1,
				Boolx2CacheSynchronizer synchronizer) {

			super(container0, container1, synchronizer);
		}

		public final int execute(int programCounter) {
			this.synchronizer.readCache();
			boolean[] data0 = this.container0.getData();
			boolean[] data1 = this.container1.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = !data1[i];
			}

			this.synchronizer.writeCache();
			return programCounter + 1;
		}
	}
}

