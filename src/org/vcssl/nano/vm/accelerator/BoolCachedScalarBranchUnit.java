/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class BoolCachedScalarBranchUnit extends AcceleratorExecutionUnit {

	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		BoolScalarCache conditionCache = (BoolScalarCache)operandCaches[2];

		AcceleratorExecutionNode node = null;
		switch (instruction.getOperationCode()) {
			case JMP : {
				// 条件値が定数の場合は、常に成功か失敗のどちらか（固定）に飛ぶノードを生成
				if (operandConstant[2]) {
					boolean condition = ( (boolean[])operandContainers[2].getData() )[0]; // 条件の低数値
					if (condition) {
						node = new CachedScalarUnconditionalJmpNode(nextNode);
					} else {
						node = new CachedScalarUnconditionalNeverJmpNode(nextNode);
					}
				// そうでない通常の場合は、スカラキャッシュから条件値を読んで飛ぶノードを生成
				} else {
					node = new CachedScalarJmpNode(conditionCache, nextNode);
				}
				break;
			}
			case JMPN : {
				// 条件値が定数の場合は、常に成功か失敗のどちらか（固定）に飛ぶノードを生成
				if (operandConstant[2]) {
					boolean condition = ( (boolean[])operandContainers[2].getData() )[0]; // 条件の低数値
					if (condition) {
						node = new CachedScalarUnconditionalNeverJmpNode(nextNode);
					} else {
						node = new CachedScalarUnconditionalJmpNode(nextNode);
					}
				// そうでない通常の場合は、スカラキャッシュから条件値を読んで飛ぶノードを生成
				} else {
					node = new CachedScalarJmpnNode(conditionCache, nextNode);
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


	private final class CachedScalarJmpNode extends AcceleratorExecutionNode {
		private final BoolScalarCache conditionCache;
		private AcceleratorExecutionNode branchedNode = null;

		public CachedScalarJmpNode(BoolScalarCache conditionCache, AcceleratorExecutionNode nextNode) {
			super(nextNode);
			this.conditionCache = conditionCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.conditionCache.value) {
				return this.branchedNode;
			} else {
				return this.nextNode;
			}

		}
	}


	private final class CachedScalarJmpnNode extends AcceleratorExecutionNode {
		private final BoolScalarCache conditionCache;
		private AcceleratorExecutionNode branchedNode = null;

		public CachedScalarJmpnNode(BoolScalarCache conditionCache, AcceleratorExecutionNode nextNode) {
			super(nextNode);

			this.conditionCache = conditionCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.conditionCache.value) {
				return this.nextNode;
			} else {
				return this.branchedNode;
			}
		}
	}


	private final class CachedScalarUnconditionalJmpNode extends AcceleratorExecutionNode {
		private AcceleratorExecutionNode branchedNode = null;

		public CachedScalarUnconditionalJmpNode(AcceleratorExecutionNode nextNode) {
			super(nextNode);
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			return this.branchedNode;
		}
	}

	private final class CachedScalarUnconditionalNeverJmpNode extends AcceleratorExecutionNode {

		public CachedScalarUnconditionalNeverJmpNode(AcceleratorExecutionNode nextNode) {
			super(nextNode);
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			return this.nextNode;
		}
	}


}
