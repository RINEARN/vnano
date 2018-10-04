/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.processor.OperationCode;

public class BoolCachedScalarBranchUnit extends AccelerationUnit {

	@Override
	public AccelerationExecutorNode generateExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		BoolScalarCache cache0 = (BoolScalarCache)operandCaches[0];
		Int64ScalarCache cache1 = (Int64ScalarCache)operandCaches[1];

		AccelerationExecutorNode executor = null;
		switch (opcode) {
			case JMP : {
				if (operandConstant[0]) {
					boolean condition = ( (boolean[])operandContainers[0].getData() )[0];
					if (condition) {
						executor = new CachedScalarUnconditionalJmpExecutor(nextNode);
					} else {
						executor = new CachedScalarUnconditionalNeverJmpExecutor(nextNode);
					}
				} else {
					executor = new CachedScalarJmpExecutor(cache0, nextNode);
				}
				break;
			}
			case JMPN : {
				if (operandConstant[0]) {
					boolean condition = ( (boolean[])operandContainers[0].getData() )[0];
					if (condition) {
						executor = new CachedScalarUnconditionalNeverJmpExecutor(nextNode);
					} else {
						executor = new CachedScalarUnconditionalJmpExecutor(nextNode);
					}
				} else {
					executor = new CachedScalarJmpnExecutor(cache0, nextNode);
				}
				break;
			}
			default : {
				break;
			}
		}
		return executor;
	}


	private final class CachedScalarJmpExecutor extends AccelerationExecutorNode {
		private final BoolScalarCache cache0;
		private AccelerationExecutorNode branchedNode = null;

		public CachedScalarJmpExecutor(BoolScalarCache cache0, AccelerationExecutorNode nextNode) {
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


	private final class CachedScalarJmpnExecutor extends AccelerationExecutorNode {
		private final BoolScalarCache cache0;
		private AccelerationExecutorNode branchedNode = null;

		public CachedScalarJmpnExecutor(BoolScalarCache cache0, AccelerationExecutorNode nextNode) {
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


	private final class CachedScalarUnconditionalJmpExecutor extends AccelerationExecutorNode {
		private AccelerationExecutorNode branchedNode = null;

		public CachedScalarUnconditionalJmpExecutor(AccelerationExecutorNode nextNode) {
			super(nextNode);
		}

		public void setBranchedNode(AccelerationExecutorNode branchedNode) {
			this.branchedNode = branchedNode;
		}

		public final AccelerationExecutorNode execute() {
			return this.branchedNode;
		}
	}

	private final class CachedScalarUnconditionalNeverJmpExecutor extends AccelerationExecutorNode {

		public CachedScalarUnconditionalNeverJmpExecutor(AccelerationExecutorNode nextNode) {
			super(nextNode);
		}

		public final AccelerationExecutorNode execute() {
			return this.nextNode;
		}
	}


}
