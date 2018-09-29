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
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant) {

		BoolCache cache0 = (BoolCache)operandCaches[0];

		// ラベル番地はメモリマッピング時点で確定していて不変なので、この段階で控える
		int jumpAddress = (int)( (long[])operandContainers[1].getData() )[0];

		AccelerationExecutorNode executor = null;
		switch (opcode) {
			case JMP : {
				if (operandConstant[0]) {
					boolean condition = ( (boolean[])operandContainers[0].getData() )[0];
					if (condition) {
						executor = new CachedScalarUnconditionalJmpExecutor(jumpAddress);
					} else {
						executor = new CachedScalarUnconditionalNeverJmpExecutor();
					}
				} else {
					executor = new CachedScalarJmpExecutor(cache0, jumpAddress);
				}
				break;
			}
			case JMPN : {
				if (operandConstant[0]) {
					boolean condition = ( (boolean[])operandContainers[0].getData() )[0];
					if (condition) {
						executor = new CachedScalarUnconditionalNeverJmpExecutor();
					} else {
						executor = new CachedScalarUnconditionalJmpExecutor(jumpAddress);
					}
				} else {
					executor = new CachedScalarJmpnExecutor(cache0, jumpAddress);
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
		private final BoolCache cache0;
		private final int jumpAddress;
		private AccelerationExecutorNode branchedNode = null;

		public CachedScalarJmpExecutor(BoolCache cache0, int jumpAddress) {

			this.cache0 = cache0;
			this.jumpAddress = jumpAddress;
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
		private final BoolCache cache0;
		private final int jumpAddress;
		private AccelerationExecutorNode branchedNode = null;

		public CachedScalarJmpnExecutor(BoolCache cache0, int jumpAddress) {

			this.cache0 = cache0;
			this.jumpAddress = jumpAddress;
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
		private final int jumpAddress;
		private AccelerationExecutorNode branchedNode = null;

		public CachedScalarUnconditionalJmpExecutor(int jumpAddress) {
			this.jumpAddress = jumpAddress;
		}

		public void setBranchedNode(AccelerationExecutorNode branchedNode) {
			this.branchedNode = branchedNode;
		}

		public final AccelerationExecutorNode execute() {
			return this.branchedNode;
		}
	}

	private final class CachedScalarUnconditionalNeverJmpExecutor extends AccelerationExecutorNode {

		public CachedScalarUnconditionalNeverJmpExecutor() {
		}

		public final AccelerationExecutorNode execute() {
			return this.nextNode;
		}
	}


}
