/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import java.util.Arrays;

import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.processor.OperationCode;

public class BoolVectorTransferUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant) {

		AccelerationExecutorNode executor = null;
		switch (opcode) {
			case MOV : {
				Boolx2CacheSynchronizer synchronizer
						= new Boolx2CacheSynchronizer(operandContainers, operandCaches, operandCached);
				executor = new BoolVectorMovExecutor(
						(DataContainer<boolean[]>)operandContainers[0], (DataContainer<boolean[]>)operandContainers[1],
						synchronizer);
				break;
			}
			case CAST : {
				if (dataTypes[1] == DataType.BOOL) {
					Boolx2CacheSynchronizer synchronizer
							= new Boolx2CacheSynchronizer(operandContainers, operandCaches, operandCached);
					executor = new BoolVectorMovExecutor(
							(DataContainer<boolean[]>)operandContainers[0], (DataContainer<boolean[]>)operandContainers[1],
							synchronizer);
				}
				break;
			}
			case FILL : {
				Boolx2CacheSynchronizer synchronizer
						= new Boolx2CacheSynchronizer(operandContainers, operandCaches, operandCached);
				executor = new BoolVectorFillExecutor(
						(DataContainer<boolean[]>)operandContainers[0], (DataContainer<boolean[]>)operandContainers[1],
						synchronizer);
				break;
			}
			default : {
				break;
			}
		}
		return executor;
	}

	private final class BoolVectorMovExecutor extends AccelerationExecutorNode {
		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<boolean[]> container1;
		protected final Boolx2CacheSynchronizer synchronizer;

		public BoolVectorMovExecutor(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1,
				Boolx2CacheSynchronizer synchronizer) {

			this.container0 = container0;
			this.container1 = container1;
			this.synchronizer = synchronizer;
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			boolean[] data0 = this.container0.getData();
			boolean[] data1 = this.container1.getData();
			int size = this.container0.getSize();

			System.arraycopy(data1, 0, data0, 0, size);

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class BoolVectorFillExecutor extends AccelerationExecutorNode {
		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<boolean[]> container1;
		protected final Boolx2CacheSynchronizer synchronizer;

		public BoolVectorFillExecutor(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1,
				Boolx2CacheSynchronizer synchronizer) {

			this.container0 = container0;
			this.container1 = container1;
			this.synchronizer = synchronizer;
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			boolean[] data0 = this.container0.getData();
			boolean fillValue = this.container1.getData()[ this.container1.getOffset() ];

			Arrays.fill(data0, fillValue);

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}


}

