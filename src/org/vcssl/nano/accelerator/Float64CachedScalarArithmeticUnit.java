/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.processor.OperationCode;

public class Float64CachedScalarArithmeticUnit extends AccelerationUnit {

	@Override
	public AccelerationExecutorNode generateExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant) {

		Float64Cache[] caches = new Float64Cache[]{
				(Float64Cache)operandCaches[0],
				(Float64Cache)operandCaches[1],
				(Float64Cache)operandCaches[2]
		};

		Float64CachedScalarArithmeticExecutor executor = null;
		switch (opcode) {
			case ADD : {
				executor = new Float64CachedScalarAddExecutor(caches[0], caches[1], caches[2]);
				break;
			}
			case SUB : {
				executor = new Float64CachedScalarSubExecutor(caches[0], caches[1], caches[2]);
				break;
			}
			case MUL : {
				executor = new Float64CachedScalarMulExecutor(caches[0], caches[1], caches[2]);
				break;
			}
			case DIV : {
				executor = new Float64CachedScalarDivExecutor(caches[0], caches[1], caches[2]);
				break;
			}
			case REM : {
				executor = new Float64CachedScalarRemExecutor(caches[0], caches[1], caches[2]);
				break;
			}
			default : {
				break;
			}
		}
		return executor;
	}

	private abstract class Float64CachedScalarArithmeticExecutor extends AccelerationExecutorNode {
		protected final Float64Cache cache0;
		protected final Float64Cache cache1;
		protected final Float64Cache cache2;

		public Float64CachedScalarArithmeticExecutor(Float64Cache cache0, Float64Cache cache1, Float64Cache cache2) {
			this.cache0 = cache0;
			this.cache1 = cache1;
			this.cache2 = cache2;
		}
	}

	private final class Float64CachedScalarAddExecutor extends Float64CachedScalarArithmeticExecutor {
		public Float64CachedScalarAddExecutor(Float64Cache cache0, Float64Cache cache1, Float64Cache cache2) {
			super(cache0, cache1, cache2);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value + this.cache2.value;
			return this.nextNode;
		}
	}


	private final class Float64CachedScalarSubExecutor extends Float64CachedScalarArithmeticExecutor {
		public Float64CachedScalarSubExecutor(Float64Cache cache0, Float64Cache cache1, Float64Cache cache2) {
			super(cache0, cache1, cache2);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value - this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarMulExecutor extends Float64CachedScalarArithmeticExecutor {
		public Float64CachedScalarMulExecutor(Float64Cache cache0, Float64Cache cache1, Float64Cache cache2) {
			super(cache0, cache1, cache2);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value * this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarDivExecutor extends Float64CachedScalarArithmeticExecutor {
		public Float64CachedScalarDivExecutor(Float64Cache cache0, Float64Cache cache1, Float64Cache cache2) {
			super(cache0, cache1, cache2);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value / this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarRemExecutor extends Float64CachedScalarArithmeticExecutor {
		public Float64CachedScalarRemExecutor(Float64Cache cache0, Float64Cache cache1, Float64Cache cache2) {
			super(cache0, cache1, cache2);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value % this.cache2.value;
			return this.nextNode;
		}
	}
}
