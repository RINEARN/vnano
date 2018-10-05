/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.memory.DataContainer;

public class BoolScalarBranchUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutor(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		DataContainer<boolean[]> container0 = (DataContainer<boolean[]>)operandContainers[0];
		Boolx1ScalarCacheSynchronizer synchronizer
				= new Boolx1ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);

		AccelerationExecutorNode executor = null;
		switch (instruction.getOperationCode()) {
			case JMP : {
				executor = new ScalarJmpExecutor(container0, synchronizer, nextNode);
				break;
			}
			case JMPN : {
				executor = new ScalarJmpnExecutor(container0, synchronizer, nextNode);
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


	private final class ScalarJmpExecutor extends AccelerationExecutorNode {
		private final DataContainer<boolean[]> container0;
		private final Boolx1ScalarCacheSynchronizer synchronizer;
		private AccelerationExecutorNode branchedNode = null;

		public ScalarJmpExecutor(
				DataContainer<boolean[]> container0, Boolx1ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.synchronizer = synchronizer;
		}

		public void setBranchedNode(AccelerationExecutorNode branchedNode) {
			this.branchedNode = branchedNode;
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			if (this.container0.getData()[ this.container0.getOffset() ]) {
				return this.branchedNode;
			} else {
				return this.nextNode;
			}

		}
	}
	private final class ScalarJmpnExecutor extends AccelerationExecutorNode {
		private final DataContainer<boolean[]> container0;
		private final Boolx1ScalarCacheSynchronizer synchronizer;
		private AccelerationExecutorNode branchedNode = null;

		public ScalarJmpnExecutor(
				DataContainer<boolean[]> container0, Boolx1ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.synchronizer = synchronizer;
		}

		public void setBranchedNode(AccelerationExecutorNode branchedNode) {
			this.branchedNode = branchedNode;
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			if (this.container0.getData()[ this.container0.getOffset() ]) {
				return this.nextNode;
			} else {
				return this.branchedNode;
			}
		}
	}

}
