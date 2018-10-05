/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.memory.DataContainer;

public class Float64CachedScalarArithmeticUnit extends AccelerationUnit {

	@Override
	public AccelerationExecutorNode generateExecutor(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		Float64ScalarCache[] caches = new Float64ScalarCache[]{
				(Float64ScalarCache)operandCaches[0],
				(Float64ScalarCache)operandCaches[1],
				(Float64ScalarCache)operandCaches[2]
		};

		Float64CachedScalarArithmeticExecutor executor = null;
		switch (instruction.getOperationCode()) {
			case ADD : {
				executor = new Float64CachedScalarAddExecutor(caches[0], caches[1], caches[2], nextNode);
				break;
			}
			case SUB : {
				executor = new Float64CachedScalarSubExecutor(caches[0], caches[1], caches[2], nextNode);
				break;
			}
			case MUL : {
				executor = new Float64CachedScalarMulExecutor(caches[0], caches[1], caches[2], nextNode);
				break;
			}
			case DIV : {
				executor = new Float64CachedScalarDivExecutor(caches[0], caches[1], caches[2], nextNode);
				break;
			}
			case REM : {
				executor = new Float64CachedScalarRemExecutor(caches[0], caches[1], caches[2], nextNode);
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

	private abstract class Float64CachedScalarArithmeticExecutor extends AccelerationExecutorNode {
		protected final Float64ScalarCache cache0;
		protected final Float64ScalarCache cache1;
		protected final Float64ScalarCache cache2;

		public Float64CachedScalarArithmeticExecutor(Float64ScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.cache0 = cache0;
			this.cache1 = cache1;
			this.cache2 = cache2;
		}
	}

	private final class Float64CachedScalarAddExecutor extends Float64CachedScalarArithmeticExecutor {
		public Float64CachedScalarAddExecutor(Float64ScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value + this.cache2.value;
			return this.nextNode;
		}
	}


	private final class Float64CachedScalarSubExecutor extends Float64CachedScalarArithmeticExecutor {
		public Float64CachedScalarSubExecutor(Float64ScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value - this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarMulExecutor extends Float64CachedScalarArithmeticExecutor {
		public Float64CachedScalarMulExecutor(Float64ScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value * this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarDivExecutor extends Float64CachedScalarArithmeticExecutor {
		public Float64CachedScalarDivExecutor(Float64ScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value / this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarRemExecutor extends Float64CachedScalarArithmeticExecutor {
		public Float64CachedScalarRemExecutor(Float64ScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value % this.cache2.value;
			return this.nextNode;
		}
	}
}
