/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.memory.DataContainer;

public class Int64CachedScalarArithmeticUnit extends AccelerationUnit {

	@Override
	public AccelerationExecutorNode generateExecutorNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		Int64ScalarCache[] caches = new Int64ScalarCache[]{
				(Int64ScalarCache)operandCaches[0],
				(Int64ScalarCache)operandCaches[1],
				(Int64ScalarCache)operandCaches[2]
		};

		Int64CachedScalarArithmeticExecutorNode executor = null;
		switch (instruction.getOperationCode()) {
			case ADD : {

				// 破壊代入で定数を加算する場合（ループカウンタの処理で頻出）
				if (operandContainers[0] == operandContainers[1] && operandConstant[2]) {
					long diff = ((long[])operandContainers[2].getData())[ operandContainers[2].getOffset() ];

					// インクリメント
					if (diff == 1) {
						executor = new Int64CachedScalarIncrementUnit(caches[0], nextNode);

					// 定数複合代入演算の減算(i+=2など)
					} else {
						executor = new Int64CachedScalarConstantAddUnit(caches[0], diff, nextNode);
					}

				// 通常の加算
				} else {
					executor = new Int64CachedScalarAddUnit(caches[0], caches[1], caches[2], nextNode);
				}
				break;
			}
			case SUB : {
				// 破壊代入で定数を減算する場合（ループカウンタの処理で頻出）
				if (operandContainers[0] == operandContainers[1] && operandConstant[2]) {
					long diff = ((long[])operandContainers[2].getData())[ operandContainers[2].getOffset() ];

					// デクリメント
					if (diff == 1) {
						executor = new Int64CachedScalarDecrementUnit(caches[0], nextNode);

					// 定数複合代入演算の減算(i-=2など)
					} else {
						executor = new Int64CachedScalarConstantSubUnit(caches[0], diff, nextNode);
					}

				// 通常の減算
				} else {
					executor = new Int64CachedScalarSubUnit(caches[0], caches[1], caches[2], nextNode);
				}
				break;
			}
			case MUL : {
				executor = new Int64CachedScalarMulUnit(caches[0], caches[1], caches[2], nextNode);
				break;
			}
			case DIV : {
				executor = new Int64CachedScalarDivUnit(caches[0], caches[1], caches[2], nextNode);
				break;
			}
			case REM : {
				executor = new Int64CachedScalarRemUnit(caches[0], caches[1], caches[2], nextNode);
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

	private abstract class Int64CachedScalarArithmeticExecutorNode extends AccelerationExecutorNode {
		protected final Int64ScalarCache cache0;
		protected final Int64ScalarCache cache1;
		protected final Int64ScalarCache cache2;

		public Int64CachedScalarArithmeticExecutorNode(Int64ScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.cache0 = cache0;
			this.cache1 = cache1;
			this.cache2 = cache2;
		}
		public Int64CachedScalarArithmeticExecutorNode(Int64ScalarCache cache0,
				AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.cache0 = cache0;
			this.cache1 = null;
			this.cache2 = null;
		}
	}

	private final class Int64CachedScalarAddUnit extends Int64CachedScalarArithmeticExecutorNode {
		public Int64CachedScalarAddUnit(Int64ScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value + this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarSubUnit extends Int64CachedScalarArithmeticExecutorNode {
		public Int64CachedScalarSubUnit(Int64ScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value - this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarMulUnit extends Int64CachedScalarArithmeticExecutorNode {
		public Int64CachedScalarMulUnit(Int64ScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value * this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarDivUnit extends Int64CachedScalarArithmeticExecutorNode {
		public Int64CachedScalarDivUnit(Int64ScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value / this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarRemUnit extends Int64CachedScalarArithmeticExecutorNode {
		public Int64CachedScalarRemUnit(Int64ScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value % this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarIncrementUnit extends Int64CachedScalarArithmeticExecutorNode {
		public Int64CachedScalarIncrementUnit(Int64ScalarCache cache0, AccelerationExecutorNode nextNode) {
			super(cache0, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			++this.cache0.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarDecrementUnit extends Int64CachedScalarArithmeticExecutorNode {
		public Int64CachedScalarDecrementUnit(Int64ScalarCache cache0, AccelerationExecutorNode nextNode) {
			super(cache0, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			--this.cache0.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarConstantAddUnit extends Int64CachedScalarArithmeticExecutorNode {
		private final long diff;
		public Int64CachedScalarConstantAddUnit(Int64ScalarCache cache0, long diff, AccelerationExecutorNode nextNode) {
			super(cache0, nextNode);
			this.diff = diff;
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value += diff;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarConstantSubUnit extends Int64CachedScalarArithmeticExecutorNode {
		private final long diff;
		public Int64CachedScalarConstantSubUnit(Int64ScalarCache cache0, long diff, AccelerationExecutorNode nextNode) {
			super(cache0, nextNode);
			this.diff = diff;
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value -= diff;
			return this.nextNode;
		}
	}

}
