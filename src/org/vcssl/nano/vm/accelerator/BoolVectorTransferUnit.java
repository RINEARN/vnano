/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import java.util.Arrays;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.vm.memory.DataContainer;

public class BoolVectorTransferUnit extends AcceleratorExecutionUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		AcceleratorExecutionNode node = null;
		switch (instruction.getOperationCode()) {
			case MOV : {
				Boolx2ScalarCacheSynchronizer synchronizer
						= new Boolx2ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled);
				node = new BoolVectorMovNode(
						(DataContainer<boolean[]>)operandContainers[0], (DataContainer<boolean[]>)operandContainers[1],
						synchronizer, nextNode);
				break;
			}
			case CAST : {
				if (instruction.getDataTypes()[1] == DataType.BOOL) {
					Boolx2ScalarCacheSynchronizer synchronizer
							= new Boolx2ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled);
					node = new BoolVectorMovNode(
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
						= new Boolx2ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled);
				node = new BoolVectorFillNode(
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
		return node;
	}

	private final class BoolVectorMovNode extends AcceleratorExecutionNode {
		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<boolean[]> container1;
		protected final Boolx2ScalarCacheSynchronizer synchronizer;

		public BoolVectorMovNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1,
				Boolx2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.synchronizer = synchronizer;
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			boolean[] data0 = this.container0.getData();
			boolean[] data1 = this.container1.getData();
			int size = this.container0.getSize();

			System.arraycopy(data1, 0, data0, 0, size);

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class BoolVectorFillNode extends AcceleratorExecutionNode {
		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<boolean[]> container1;
		protected final Boolx2ScalarCacheSynchronizer synchronizer;

		public BoolVectorFillNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1,
				Boolx2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.synchronizer = synchronizer;
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			boolean[] data0 = this.container0.getData();
			boolean fillValue = this.container1.getData()[ this.container1.getOffset() ];

			Arrays.fill(data0, fillValue);

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}


}

