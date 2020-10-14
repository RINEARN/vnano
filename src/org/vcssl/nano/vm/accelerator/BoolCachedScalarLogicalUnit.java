/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class BoolCachedScalarLogicalUnit extends AcceleratorExecutionUnit {

	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		BoolCachedScalarLogicalNode node = null;
		switch (instruction.getOperationCode()) {
			case ANDM : {
				node = new BoolCachedScalarAndNode(
						(BoolScalarCache)operandCaches[0],
						(BoolScalarCache)operandCaches[1],
						(BoolScalarCache)operandCaches[2],
						nextNode);
				break;
			}
			case ORM : {
				node = new BoolCachedScalarOrNode(
						(BoolScalarCache)operandCaches[0],
						(BoolScalarCache)operandCaches[1],
						(BoolScalarCache)operandCaches[2],
						nextNode);
				break;
			}
			case NOT : {
				node = new BoolCachedScalarNotNode(
						(BoolScalarCache)operandCaches[0],
						(BoolScalarCache)operandCaches[1],
						nextNode);
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

	private abstract class BoolCachedScalarLogicalNode extends AcceleratorExecutionNode {
		protected final BoolScalarCache cache0;
		protected final BoolScalarCache cache1;
		protected final BoolScalarCache cache2;

		public BoolCachedScalarLogicalNode(BoolScalarCache cache0, BoolScalarCache cache1, BoolScalarCache cache2, AcceleratorExecutionNode nextNode) {
			super(nextNode, 1);
			this.cache0 = cache0;
			this.cache1 = cache1;
			this.cache2 = cache2;
		}

		public BoolCachedScalarLogicalNode(BoolScalarCache cache0, BoolScalarCache cache1, AcceleratorExecutionNode nextNode) {
			super(nextNode, 1);
			this.cache0 = cache0;
			this.cache1 = cache1;
			this.cache2 = null;
		}
	}

	private final class BoolCachedScalarAndNode extends BoolCachedScalarLogicalNode {
		public BoolCachedScalarAndNode(BoolScalarCache cache0, BoolScalarCache cache1, BoolScalarCache cache2, AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			// 注： 短絡評価により、左辺(container1) が false の場合には
			//      右辺(container2) は null になっている可能性があるので、
			//      左辺から結果が自明な場合には右辺を参照してはいけない。
			//      (アクセスコストの面からも恐らく不利)
			//      そのため、以下の && 演算子を & 演算子にしてはいけない。

			this.cache0.value = this.cache1.value && this.cache2.value;
			return this.nextNode;
		}
	}

	private final class BoolCachedScalarOrNode extends BoolCachedScalarLogicalNode {
		public BoolCachedScalarOrNode(BoolScalarCache cache0, BoolScalarCache cache1, BoolScalarCache cache2, AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			// 注： 短絡評価により、左辺(container1) が false の場合には
			//      右辺(container2) は null になっている可能性があるので、
			//      左辺から結果が自明な場合には右辺を参照してはいけない。
			//      (アクセスコストの面からも恐らく不利)
			//      そのため、以下の || 演算子を | 演算子にしてはいけない。

			this.cache0.value = this.cache1.value || this.cache2.value;
			return this.nextNode;
		}
	}

	private final class BoolCachedScalarNotNode extends BoolCachedScalarLogicalNode {
		public BoolCachedScalarNotNode(BoolScalarCache cache0, BoolScalarCache cache1, AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.value = !this.cache1.value;
			return this.nextNode;
		}
	}
}
