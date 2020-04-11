/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class BoolVectorBranchUnit extends AcceleratorExecutionUnit {

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
				node = new VectorJmpNode(conditionContainer, synchronizer, nextNode);
				break;
			}
			case JMPN : {
				node = new VectorJmpnNode(conditionContainer, synchronizer, nextNode);
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


	private final class VectorJmpNode extends AcceleratorExecutionNode {
		private final DataContainer<boolean[]> conditionContainer;
		private final Boolx1ScalarCacheSynchronizer synchronizer;
		private AcceleratorExecutionNode branchedNode = null;

		public VectorJmpNode(DataContainer<boolean[]> conditionContainer, Boolx1ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.conditionContainer = conditionContainer;
			this.synchronizer = synchronizer;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			boolean[] conditions = this.conditionContainer.getData();

			// ベクトルJMP命令は、条件オペランドベクトルの全要素がtrueなら飛ぶものと定義する。
			// そう定義する事で、中間コードにおいて、
			// ベクトル論理演算とスカラ論理演算の短絡評価処理を統一的かつ簡潔に表現できる。

			boolean shouldJump = true; // 飛ぶべき場合にtrue、飛んではいけない場合にfalseにする
			for (boolean condition: conditions) {
				shouldJump &= condition; // オペランド[2]の要素に1つでもfalseがあればfalseになり、飛ばなくなる
			}

			// 飛ぶべき場合： 分岐先命令に飛ぶ
			if (shouldJump) {
				return this.branchedNode;

			// 飛んではいけない場合： 次の命令に進む
			} else {
				return this.nextNode;
			}
		}
	}


	private final class VectorJmpnNode extends AcceleratorExecutionNode {
		private final DataContainer<boolean[]> conditionContainer;
		private final Boolx1ScalarCacheSynchronizer synchronizer;
		private AcceleratorExecutionNode branchedNode = null;

		public VectorJmpnNode(DataContainer<boolean[]> conditionContainer, Boolx1ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.conditionContainer = conditionContainer;
			this.synchronizer = synchronizer;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			boolean[] conditions = this.conditionContainer.getData();

			// ベクトルJMPN命令は、条件オペランドベクトルの全要素がfalseなら飛ぶものと定義する。
			// そう定義する事で、中間コードにおいて、
			// ベクトル論理演算とスカラ論理演算の短絡評価処理を統一的かつ簡潔に表現できる。

			boolean shouldNotJump = false; // 飛んではいけない場合にtrue、飛ぶべき場合にfalseにする
			for (boolean condition: conditions) {
				shouldNotJump |= condition; // オペランド[2]の要素に1つでもtrueがあればtrueになり、飛ばなくなる
			}

			// 飛んではいけない場合： 次の命令に進む
			if (shouldNotJump) {
				return this.nextNode;

			// 飛ぶべき場合： 分岐先命令に飛ぶ
			} else {
				return this.branchedNode;
			}
		}
	}

}
