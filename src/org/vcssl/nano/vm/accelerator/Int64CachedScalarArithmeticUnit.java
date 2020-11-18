/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class Int64CachedScalarArithmeticUnit extends AcceleratorExecutionUnit {

	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		int cacheLength = operandCaches.length;
		Int64ScalarCache[] caches = new Int64ScalarCache[cacheLength];
		for (int i=0; i<cacheLength; i++) {
			caches[i] = (Int64ScalarCache)operandCaches[i];
		}

		Int64CachedScalarArithmeticNode node = null;
		switch (instruction.getOperationCode()) {
			case ADD : {

				// 破壊代入で定数を加算する場合（ループカウンタの処理で頻出）
				if (operandContainers[0] == operandContainers[1] && operandConstant[2]) {
					long diff = ((long[])operandContainers[2].getArrayData())[ operandContainers[2].getArrayOffset() ];

					// インクリメント
					if (diff == 1) {
						node = new Int64CachedScalarIncrementUnit(caches[0], nextNode);

					// 定数複合代入演算の減算(i+=2など)
					} else {
						node = new Int64CachedScalarConstantAddUnit(caches[0], diff, nextNode);
					}

				// 通常の加算
				} else {
					node = new Int64CachedScalarAddUnit(caches[0], caches[1], caches[2], nextNode);
				}
				break;
			}
			case SUB : {
				// 破壊代入で定数を減算する場合（ループカウンタの処理で頻出）
				if (operandContainers[0] == operandContainers[1] && operandConstant[2]) {
					long diff = ((long[])operandContainers[2].getArrayData())[ operandContainers[2].getArrayOffset() ];

					// デクリメント
					if (diff == 1) {
						node = new Int64CachedScalarDecrementUnit(caches[0], nextNode);

					// 定数複合代入演算の減算(i-=2など)
					} else {
						node = new Int64CachedScalarConstantSubUnit(caches[0], diff, nextNode);
					}

				// 通常の減算
				} else {
					node = new Int64CachedScalarSubUnit(caches[0], caches[1], caches[2], nextNode);
				}
				break;
			}
			case MUL : {
				node = new Int64CachedScalarMulUnit(caches[0], caches[1], caches[2], nextNode);
				break;
			}
			case DIV : {
				node = new Int64CachedScalarDivUnit(caches[0], caches[1], caches[2], nextNode);
				break;
			}
			case REM : {
				node = new Int64CachedScalarRemUnit(caches[0], caches[1], caches[2], nextNode);
				break;
			}
			case NEG : {
				node = new Int64CachedScalarNegUnit(caches[0], caches[1], nextNode);
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

	private abstract class Int64CachedScalarArithmeticNode extends AcceleratorExecutionNode {
		protected final Int64ScalarCache cache0;
		protected final Int64ScalarCache cache1;
		protected final Int64ScalarCache cache2;

		public Int64CachedScalarArithmeticNode(Int64ScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.cache0 = cache0;
			this.cache1 = cache1;
			this.cache2 = cache2;
		}
		public Int64CachedScalarArithmeticNode(Int64ScalarCache cache0, Int64ScalarCache cache1,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.cache0 = cache0;
			this.cache1 = cache1;
			this.cache2 = null;
		}
		public Int64CachedScalarArithmeticNode(Int64ScalarCache cache0,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.cache0 = cache0;
			this.cache1 = null;
			this.cache2 = null;
		}
	}

	private final class Int64CachedScalarAddUnit extends Int64CachedScalarArithmeticNode {
		public Int64CachedScalarAddUnit(Int64ScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.value = this.cache1.value + this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarSubUnit extends Int64CachedScalarArithmeticNode {
		public Int64CachedScalarSubUnit(Int64ScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.value = this.cache1.value - this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarMulUnit extends Int64CachedScalarArithmeticNode {
		public Int64CachedScalarMulUnit(Int64ScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.value = this.cache1.value * this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarDivUnit extends Int64CachedScalarArithmeticNode {
		public Int64CachedScalarDivUnit(Int64ScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.value = this.cache1.value / this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarRemUnit extends Int64CachedScalarArithmeticNode {
		public Int64CachedScalarRemUnit(Int64ScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.value = this.cache1.value % this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarNegUnit extends Int64CachedScalarArithmeticNode {
		public Int64CachedScalarNegUnit(Int64ScalarCache cache0, Int64ScalarCache cache1,
				AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.value = - this.cache1.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarIncrementUnit extends Int64CachedScalarArithmeticNode {
		public Int64CachedScalarIncrementUnit(Int64ScalarCache cache0, AcceleratorExecutionNode nextNode) {
			super(cache0, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			++this.cache0.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarDecrementUnit extends Int64CachedScalarArithmeticNode {
		public Int64CachedScalarDecrementUnit(Int64ScalarCache cache0, AcceleratorExecutionNode nextNode) {
			super(cache0, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			--this.cache0.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarConstantAddUnit extends Int64CachedScalarArithmeticNode {
		private final long diff;
		public Int64CachedScalarConstantAddUnit(Int64ScalarCache cache0, long diff, AcceleratorExecutionNode nextNode) {
			super(cache0, nextNode);
			this.diff = diff;
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.value += diff;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarConstantSubUnit extends Int64CachedScalarArithmeticNode {
		private final long diff;
		public Int64CachedScalarConstantSubUnit(Int64ScalarCache cache0, long diff, AcceleratorExecutionNode nextNode) {
			super(cache0, nextNode);
			this.diff = diff;
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.value -= diff;
			return this.nextNode;
		}
	}

}
