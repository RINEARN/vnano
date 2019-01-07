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

		DataContainer<boolean[]> conditionContainer = (DataContainer<boolean[]>)operandContainers[1];
		Boolx1ScalarCacheSynchronizer synchronizer
				= new Boolx1ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached, 1);

		AccelerationExecutorNode executor = null;
		switch (instruction.getOperationCode()) {
			case JMP : {
				executor = new ScalarJmpExecutorNode(conditionContainer, synchronizer, nextNode);
				break;
			}
			case JMPN : {
				executor = new ScalarJmpnExecutorNode(conditionContainer, synchronizer, nextNode);
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
		private final DataContainer<boolean[]> conditionContainer;
		private final Boolx1ScalarCacheSynchronizer synchronizer;
		private AccelerationExecutorNode branchedNode = null;

		public ScalarJmpExecutorNode(DataContainer<boolean[]> conditionContainer, Boolx1ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.conditionContainer = conditionContainer;
			this.synchronizer = synchronizer;
		}

		@Override
		public void setLaundingPointNodes(AccelerationExecutorNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public final AccelerationExecutorNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			if (this.conditionContainer.getData()[ this.conditionContainer.getOffset() ]) {
				return this.branchedNode;
			} else {
				return this.nextNode;
			}
		}
	}
	private final class ScalarJmpnExecutorNode extends AccelerationExecutorNode {
		private final DataContainer<boolean[]> conditionContainer;
		private final Boolx1ScalarCacheSynchronizer synchronizer;
		private AccelerationExecutorNode branchedNode = null;

		public ScalarJmpnExecutorNode(DataContainer<boolean[]> conditionContainer, Boolx1ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.conditionContainer = conditionContainer;
			this.synchronizer = synchronizer;
		}

		@Override
		public void setLaundingPointNodes(AccelerationExecutorNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public final AccelerationExecutorNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			if (this.conditionContainer.getData()[ this.conditionContainer.getOffset() ]) {
				return this.nextNode;
			} else {
				return this.branchedNode;
			}
		}
	}

}
