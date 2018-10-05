/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import java.util.Arrays;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;

public class BoolVectorTransferUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutor(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		AccelerationExecutorNode executor = null;
		switch (instruction.getOperationCode()) {
			case MOV : {
				Boolx2ScalarCacheSynchronizer synchronizer
						= new Boolx2ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);
				executor = new BoolVectorMovExecutor(
						(DataContainer<boolean[]>)operandContainers[0], (DataContainer<boolean[]>)operandContainers[1],
						synchronizer, nextNode);
				break;
			}
			case CAST : {
				if (instruction.getDataTypes()[1] == DataType.BOOL) {
					Boolx2ScalarCacheSynchronizer synchronizer
							= new Boolx2ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);
					executor = new BoolVectorMovExecutor(
							(DataContainer<boolean[]>)operandContainers[0], (DataContainer<boolean[]>)operandContainers[1],
							synchronizer, nextNode);
				} else {
					throw new VnanoFatalException(
							instruction.getDataTypes()[1] + "-type operand of " + instruction.getOperationCode()
							+ " instruction is invalid for " + this.getClass().getCanonicalName()
					);
				}
				break;
			}
			case FILL : {
				Boolx2ScalarCacheSynchronizer synchronizer
						= new Boolx2ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);
				executor = new BoolVectorFillExecutor(
						(DataContainer<boolean[]>)operandContainers[0], (DataContainer<boolean[]>)operandContainers[1],
						synchronizer, nextNode);
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

	private final class BoolVectorMovExecutor extends AccelerationExecutorNode {
		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<boolean[]> container1;
		protected final Boolx2ScalarCacheSynchronizer synchronizer;

		public BoolVectorMovExecutor(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1,
				Boolx2ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
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
		protected final Boolx2ScalarCacheSynchronizer synchronizer;

		public BoolVectorFillExecutor(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1,
				Boolx2ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
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

