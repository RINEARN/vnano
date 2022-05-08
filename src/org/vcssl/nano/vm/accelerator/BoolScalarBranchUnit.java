/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class BoolScalarBranchUnit extends AcceleratorExecutionUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		DataContainer<boolean[]> conditionContainer = (DataContainer<boolean[]>)operandContainers[2];
		Boolx1ScalarCacheSynchronizer synchronizer
				= new Boolx1ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled, 2);

		AcceleratorExecutionNode node = null;
		switch (instruction.getOperationCode()) {
			case JMP : {
				node = new ScalarJmpNode(conditionContainer, synchronizer, nextNode);
				break;
			}
			case JMPN : {
				node = new ScalarJmpnNode(conditionContainer, synchronizer, nextNode);
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


	private final class ScalarJmpNode extends AcceleratorExecutionNode {
		private final DataContainer<boolean[]> conditionContainer;
		private final Boolx1ScalarCacheSynchronizer synchronizer;
		private AcceleratorExecutionNode branchedNode = null;

		public ScalarJmpNode(DataContainer<boolean[]> conditionContainer, Boolx1ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.conditionContainer = conditionContainer;
			this.synchronizer = synchronizer;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			if (this.conditionContainer.getArrayData()[ this.conditionContainer.getArrayOffset() ]) {
				return this.branchedNode;
			} else {
				return this.nextNode;
			}
		}
	}
	private final class ScalarJmpnNode extends AcceleratorExecutionNode {
		private final DataContainer<boolean[]> conditionContainer;
		private final Boolx1ScalarCacheSynchronizer synchronizer;
		private AcceleratorExecutionNode branchedNode = null;

		public ScalarJmpnNode(DataContainer<boolean[]> conditionContainer, Boolx1ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.conditionContainer = conditionContainer;
			this.synchronizer = synchronizer;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			if (this.conditionContainer.getArrayData()[ this.conditionContainer.getArrayOffset() ]) {
				return this.nextNode;
			} else {
				return this.branchedNode;
			}
		}
	}

}
