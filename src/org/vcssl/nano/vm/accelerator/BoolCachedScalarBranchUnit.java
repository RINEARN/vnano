/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class BoolCachedScalarBranchUnit extends AccelerationUnit {

	@Override
	public AccelerationExecutorNode generateExecutorNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		BoolScalarCache conditionCache = (BoolScalarCache)operandCaches[2];

		AccelerationExecutorNode executor = null;
		switch (instruction.getOperationCode()) {
			case JMP : {
				// 条件値が定数の場合は、常に成功か失敗のどちらか（固定）に飛ぶノードを生成
				if (operandConstant[2]) {
					boolean condition = ( (boolean[])operandContainers[2].getData() )[0]; // 条件の低数値
					if (condition) {
						executor = new CachedScalarUnconditionalJmpExecutorNode(nextNode);
					} else {
						executor = new CachedScalarUnconditionalNeverJmpExecutorNode(nextNode);
					}
				// そうでない通常の場合は、スカラキャッシュから条件値を読んで飛ぶノードを生成
				} else {
					executor = new CachedScalarJmpExecutorNode(conditionCache, nextNode);
				}
				break;
			}
			case JMPN : {
				// 条件値が定数の場合は、常に成功か失敗のどちらか（固定）に飛ぶノードを生成
				if (operandConstant[2]) {
					boolean condition = ( (boolean[])operandContainers[2].getData() )[0]; // 条件の低数値
					if (condition) {
						executor = new CachedScalarUnconditionalNeverJmpExecutorNode(nextNode);
					} else {
						executor = new CachedScalarUnconditionalJmpExecutorNode(nextNode);
					}
				// そうでない通常の場合は、スカラキャッシュから条件値を読んで飛ぶノードを生成
				} else {
					executor = new CachedScalarJmpnExecutorNode(conditionCache, nextNode);
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
		private final BoolScalarCache conditionCache;
		private AccelerationExecutorNode branchedNode = null;

		public CachedScalarJmpExecutorNode(BoolScalarCache conditionCache, AccelerationExecutorNode nextNode) {
			super(nextNode);
			this.conditionCache = conditionCache;
		}

		@Override
		public void setLaundingPointNodes(AccelerationExecutorNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public final AccelerationExecutorNode execute() {
			if (this.conditionCache.value) {
				return this.branchedNode;
			} else {
				return this.nextNode;
			}

		}
	}


	private final class CachedScalarJmpnExecutorNode extends AccelerationExecutorNode {
		private final BoolScalarCache conditionCache;
		private AccelerationExecutorNode branchedNode = null;

		public CachedScalarJmpnExecutorNode(BoolScalarCache conditionCache, AccelerationExecutorNode nextNode) {
			super(nextNode);

			this.conditionCache = conditionCache;
		}

		@Override
		public void setLaundingPointNodes(AccelerationExecutorNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public final AccelerationExecutorNode execute() {
			if (this.conditionCache.value) {
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

		@Override
		public void setLaundingPointNodes(AccelerationExecutorNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public final AccelerationExecutorNode execute() {
			return this.branchedNode;
		}
	}

	private final class CachedScalarUnconditionalNeverJmpExecutorNode extends AccelerationExecutorNode {

		public CachedScalarUnconditionalNeverJmpExecutorNode(AccelerationExecutorNode nextNode) {
			super(nextNode);
		}

		@Override
		public final AccelerationExecutorNode execute() {
			return this.nextNode;
		}
	}


}
