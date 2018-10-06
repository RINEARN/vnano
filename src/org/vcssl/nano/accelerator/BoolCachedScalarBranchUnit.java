/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.memory.DataContainer;

public class BoolCachedScalarBranchUnit extends AccelerationUnit {

	@Override
	public AccelerationExecutorNode generateExecutorNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		BoolScalarCache cache0 = (BoolScalarCache)operandCaches[0];

		AccelerationExecutorNode executor = null;
		switch (instruction.getOperationCode()) {
			case JMP : {
				if (operandConstant[0]) {
					boolean condition = ( (boolean[])operandContainers[0].getData() )[0];
					if (condition) {
						executor = new CachedScalarUnconditionalJmpExecutorNode(nextNode);
					} else {
						executor = new CachedScalarUnconditionalNeverJmpExecutorNode(nextNode);
					}
				} else {
					executor = new CachedScalarJmpExecutorNode(cache0, nextNode);
				}
				break;
			}
			case JMPN : {
				if (operandConstant[0]) {
					boolean condition = ( (boolean[])operandContainers[0].getData() )[0];
					if (condition) {
						executor = new CachedScalarUnconditionalNeverJmpExecutorNode(nextNode);
					} else {
						executor = new CachedScalarUnconditionalJmpExecutorNode(nextNode);
					}
				} else {
					executor = new CachedScalarJmpnExecutorNode(cache0, nextNode);
				}
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


	private final class CachedScalarJmpExecutorNode extends AccelerationExecutorNode {
		private final BoolScalarCache cache0;
		private AccelerationExecutorNode branchedNode = null;

		public CachedScalarJmpExecutorNode(BoolScalarCache cache0, AccelerationExecutorNode nextNode) {
			super(nextNode);
			this.cache0 = cache0;
		}

		public void setBranchedNode(AccelerationExecutorNode branchedNode) {
			this.branchedNode = branchedNode;
		}

		public final AccelerationExecutorNode execute() {
			if (this.cache0.value) {
				return this.branchedNode;
			} else {
				return this.nextNode;
			}

		}
	}


	private final class CachedScalarJmpnExecutorNode extends AccelerationExecutorNode {
		private final BoolScalarCache cache0;
		private AccelerationExecutorNode branchedNode = null;

		public CachedScalarJmpnExecutorNode(BoolScalarCache cache0, AccelerationExecutorNode nextNode) {
			super(nextNode);

			this.cache0 = cache0;
		}

		public void setBranchedNode(AccelerationExecutorNode branchedNode) {
			this.branchedNode = branchedNode;
		}

		public final AccelerationExecutorNode execute() {
			if (this.cache0.value) {
				return this.nextNode;
			} else {
				return this.branchedNode;
			}
		}
	}


	private final class CachedScalarUnconditionalJmpExecutorNode extends AccelerationExecutorNode {
		private AccelerationExecutorNode branchedNode = null;

		public CachedScalarUnconditionalJmpExecutorNode(AccelerationExecutorNode nextNode) {
			super(nextNode);
		}

		public void setBranchedNode(AccelerationExecutorNode branchedNode) {
			this.branchedNode = branchedNode;
		}

		public final AccelerationExecutorNode execute() {
			return this.branchedNode;
		}
	}

	private final class CachedScalarUnconditionalNeverJmpExecutorNode extends AccelerationExecutorNode {

		public CachedScalarUnconditionalNeverJmpExecutorNode(AccelerationExecutorNode nextNode) {
			super(nextNode);
		}

		public final AccelerationExecutorNode execute() {
			return this.nextNode;
		}
	}


}
