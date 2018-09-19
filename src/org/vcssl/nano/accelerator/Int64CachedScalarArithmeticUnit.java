/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.processor.OperationCode;

public class Int64CachedScalarArithmeticUnit extends AccelerationUnit {

	@Override
	public AccelerationExecutorNode generateExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant) {

		Int64Cache[] caches = new Int64Cache[]{
				(Int64Cache)operandCaches[0],
				(Int64Cache)operandCaches[1],
				(Int64Cache)operandCaches[2]
		};

		Int64CachedScalarArithmeticExecutor executor = null;
		switch (opcode) {
			case ADD : {

				// 破壊代入で定数を加算する場合（ループカウンタの処理で頻出）
				if (operandContainers[0] == operandContainers[1] && operandConstant[2]) {
					long diff = ((long[])operandContainers[2].getData())[ operandContainers[2].getOffset() ];

					// インクリメント
					if (diff == 1) {
						executor = new Int64CachedScalarIncrementUnit(caches[0]);

					// 定数複合代入演算の減算(i+=2など)
					} else {
						executor = new Int64CachedScalarConstantAddUnit(caches[0], diff);
					}

				// 通常の加算
				} else {
					executor = new Int64CachedScalarAddUnit(caches[0], caches[1], caches[2]);
				}
				break;
			}
			case SUB : {
				// 破壊代入で定数を減算する場合（ループカウンタの処理で頻出）
				if (operandContainers[0] == operandContainers[1] && operandConstant[2]) {
					long diff = ((long[])operandContainers[2].getData())[ operandContainers[2].getOffset() ];

					// デクリメント
					if (diff == 1) {
						executor = new Int64CachedScalarDecrementUnit(caches[0]);

					// 定数複合代入演算の減算(i-=2など)
					} else {
						executor = new Int64CachedScalarConstantSubUnit(caches[0], diff);
					}

				// 通常の減算
				} else {
					executor = new Int64CachedScalarSubUnit(caches[0], caches[1], caches[2]);
				}
				break;
			}
			case MUL : {
				executor = new Int64CachedScalarMulUnit(caches[0], caches[1], caches[2]);
				break;
			}
			case DIV : {
				executor = new Int64CachedScalarDivUnit(caches[0], caches[1], caches[2]);
				break;
			}
			case REM : {
				executor = new Int64CachedScalarRemUnit(caches[0], caches[1], caches[2]);
				break;
			}
			default : {
				break;
			}
		}
		return executor;
	}

	private abstract class Int64CachedScalarArithmeticExecutor extends AccelerationExecutorNode {
		protected final Int64Cache cache0;
		protected final Int64Cache cache1;
		protected final Int64Cache cache2;

		public Int64CachedScalarArithmeticExecutor(Int64Cache cache0, Int64Cache cache1, Int64Cache cache2) {
			this.cache0 = cache0;
			this.cache1 = cache1;
			this.cache2 = cache2;
		}
		public Int64CachedScalarArithmeticExecutor(Int64Cache cache0) {
			this.cache0 = cache0;
			this.cache1 = null;
			this.cache2 = null;
		}
		public abstract int execute(int programCounter);
	}

	private final class Int64CachedScalarAddUnit extends Int64CachedScalarArithmeticExecutor {
		public Int64CachedScalarAddUnit(Int64Cache cache0, Int64Cache cache1, Int64Cache cache2) {
			super(cache0, cache1, cache2);
		}
		public final int execute(int programCounter) {
			this.cache0.value = this.cache1.value + this.cache2.value;
			return programCounter + 1;
		}
	}

	private final class Int64CachedScalarSubUnit extends Int64CachedScalarArithmeticExecutor {
		public Int64CachedScalarSubUnit(Int64Cache cache0, Int64Cache cache1, Int64Cache cache2) {
			super(cache0, cache1, cache2);
		}
		public final int execute(int programCounter) {
			this.cache0.value = this.cache1.value - this.cache2.value;
			return programCounter + 1;
		}
	}

	private final class Int64CachedScalarMulUnit extends Int64CachedScalarArithmeticExecutor {
		public Int64CachedScalarMulUnit(Int64Cache cache0, Int64Cache cache1, Int64Cache cache2) {
			super(cache0, cache1, cache2);
		}
		public final int execute(int programCounter) {
			this.cache0.value = this.cache1.value * this.cache2.value;
			return programCounter + 1;
		}
	}

	private final class Int64CachedScalarDivUnit extends Int64CachedScalarArithmeticExecutor {
		public Int64CachedScalarDivUnit(Int64Cache cache0, Int64Cache cache1, Int64Cache cache2) {
			super(cache0, cache1, cache2);
		}
		public final int execute(int programCounter) {
			this.cache0.value = this.cache1.value / this.cache2.value;
			return programCounter + 1;
		}
	}

	private final class Int64CachedScalarRemUnit extends Int64CachedScalarArithmeticExecutor {
		public Int64CachedScalarRemUnit(Int64Cache cache0, Int64Cache cache1, Int64Cache cache2) {
			super(cache0, cache1, cache2);
		}
		public final int execute(int programCounter) {
			this.cache0.value = this.cache1.value % this.cache2.value;
			return programCounter + 1;
		}
	}

	private final class Int64CachedScalarIncrementUnit extends Int64CachedScalarArithmeticExecutor {
		public Int64CachedScalarIncrementUnit(Int64Cache cache0) {
			super(cache0);
		}
		public final int execute(int programCounter) {
			++this.cache0.value;
			return programCounter + 1;
		}
	}

	private final class Int64CachedScalarDecrementUnit extends Int64CachedScalarArithmeticExecutor {
		public Int64CachedScalarDecrementUnit(Int64Cache cache0) {
			super(cache0);
		}
		public final int execute(int programCounter) {
			--this.cache0.value;
			return programCounter + 1;
		}
	}

	private final class Int64CachedScalarConstantAddUnit extends Int64CachedScalarArithmeticExecutor {
		private final long diff;
		public Int64CachedScalarConstantAddUnit(Int64Cache cache0, long diff) {
			super(cache0);
			this.diff = diff;
		}
		public final int execute(int programCounter) {
			this.cache0.value += diff;
			return programCounter + 1;
		}
	}

	private final class Int64CachedScalarConstantSubUnit extends Int64CachedScalarArithmeticExecutor {
		private final long diff;
		public Int64CachedScalarConstantSubUnit(Int64Cache cache0, long diff) {
			super(cache0);
			this.diff = diff;
		}
		public final int execute(int programCounter) {
			this.cache0.value -= diff;
			return programCounter + 1;
		}
	}

}
