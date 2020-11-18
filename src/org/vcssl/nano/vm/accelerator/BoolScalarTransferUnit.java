/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.vm.memory.DataContainer;

public class BoolScalarTransferUnit extends AcceleratorExecutionUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		AcceleratorExecutionNode node = null;
		switch (instruction.getOperationCode()) {
			case MOV :
			case FILL : {
				Boolx2ScalarCacheSynchronizer synchronizer
						= new Boolx2ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled);
				node = new BoolScalarMovNode(
						(DataContainer<boolean[]>)operandContainers[0], (DataContainer<boolean[]>)operandContainers[1],
						synchronizer, nextNode);
				break;
			}
			case CAST : {
				if (instruction.getDataTypes()[1] == DataType.BOOL) {
					Boolx2ScalarCacheSynchronizer synchronizer
							= new Boolx2ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled);
					node = new BoolScalarMovNode(
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

			default : {
				throw new VnanoFatalException(
						"Operation code " + instruction.getOperationCode() + " is invalid for " + this.getClass().getCanonicalName()
				);
			}
		}
		return node;
	}

	private final class BoolScalarMovNode extends AcceleratorExecutionNode {

		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<boolean[]> container1;
		protected final Boolx2ScalarCacheSynchronizer synchronizer;

		public BoolScalarMovNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1,
				Boolx2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.container0 = container0;
			this.container1 = container1;
			this.synchronizer = synchronizer;
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			this.container0.getArrayData()[ this.container0.getArrayOffset() ] =
			this.container1.getArrayData()[ this.container1.getArrayOffset() ];
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

}
