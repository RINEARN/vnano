/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class BoolScalarBranchUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutorNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		DataContainer<boolean[]> container0 = (DataContainer<boolean[]>)operandContainers[0];
		Boolx1ScalarCacheSynchronizer synchronizer
				= new Boolx1ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);

		AccelerationExecutorNode executor = null;
		switch (instruction.getOperationCode()) {
			case JMP : {
				executor = new ScalarJmpExecutorNode(container0, synchronizer, nextNode);
				break;
			}
			case JMPN : {
				executor = new ScalarJmpnExecutorNode(container0, synchronizer, nextNode);
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


	private final class ScalarJmpExecutorNode extends AccelerationExecutorNode {
		private final DataContainer<boolean[]> container0;
		private final Boolx1ScalarCacheSynchronizer synchronizer;
		private AccelerationExecutorNode branchedNode = null;

		public ScalarJmpExecutorNode(
				DataContainer<boolean[]> container0, Boolx1ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.synchronizer = synchronizer;
		}

		public void setLaundingPointNodes(AccelerationExecutorNode[] branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			if (this.container0.getData()[ this.container0.getOffset() ]) {
				return this.branchedNode;
			} else {
				return this.nextNode;
			}

		}
	}
	private final class ScalarJmpnExecutorNode extends AccelerationExecutorNode {
		private final DataContainer<boolean[]> container0;
		private final Boolx1ScalarCacheSynchronizer synchronizer;
		private AccelerationExecutorNode branchedNode = null;

		public ScalarJmpnExecutorNode(
				DataContainer<boolean[]> container0, Boolx1ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.synchronizer = synchronizer;
		}

		public void setLaundingPointNodes(AccelerationExecutorNode[] branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			if (this.container0.getData()[ this.container0.getOffset() ]) {
				return this.nextNode;
			} else {
				return this.branchedNode;
			}
		}
	}

}
