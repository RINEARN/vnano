/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.processor.OperationCode;

public class Float64CachedScalarComparisonUnit extends AccelerationUnit {

	@Override
	public AccelerationExecutorNode generateExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant) {

		BoolCache caches0 = (BoolCache)operandCaches[0];
		Float64Cache caches1 = (Float64Cache)operandCaches[1];
		Float64Cache caches2 = (Float64Cache)operandCaches[2];

		Float64CachedScalarComparisonExecutor executor = null;
		switch (opcode) {
			case LT : {
				executor = new Float64CachedScalarLtExecutor(caches0, caches1, caches2);
				break;
			}
			case GT : {
				executor = new Float64CachedScalarGtExecutor(caches0, caches1, caches2);
				break;
			}
			case LEQ : {
				executor = new Float64CachedScalarLeqExecutor(caches0, caches1, caches2);
				break;
			}
			case GEQ : {
				executor = new Float64CachedScalarGeqExecutor(caches0, caches1, caches2);
				break;
			}
			case EQ : {
				executor = new Float64CachedScalarEqExecutor(caches0, caches1, caches2);
				break;
			}
			case NEQ : {
				executor = new Float64CachedScalarNeqExecutor(caches0, caches1, caches2);
				break;
			}
			default : {
				break;
			}
		}
		return executor;
	}

	private abstract class Float64CachedScalarComparisonExecutor extends AccelerationExecutorNode {
		protected final BoolCache cache0;
		protected final Float64Cache cache1;
		protected final Float64Cache cache2;

		public Float64CachedScalarComparisonExecutor(BoolCache cache0, Float64Cache cache1, Float64Cache cache2) {
			this.cache0 = cache0;
			this.cache1 = cache1;
			this.cache2 = cache2;
		}

		public abstract int execute(int programCounter);
	}

	private final class Float64CachedScalarLtExecutor extends Float64CachedScalarComparisonExecutor {
		public Float64CachedScalarLtExecutor(BoolCache cache0, Float64Cache cache1, Float64Cache cache2) {
			super(cache0, cache1, cache2);
		}
		public final int execute(int programCounter) {
			this.cache0.value = this.cache1.value < this.cache2.value;
			return programCounter + 1;
		}
	}

	private final class Float64CachedScalarGtExecutor extends Float64CachedScalarComparisonExecutor {
		public Float64CachedScalarGtExecutor(BoolCache cache0, Float64Cache cache1, Float64Cache cache2) {
			super(cache0, cache1, cache2);
		}
		public final int execute(int programCounter) {
			this.cache0.value = this.cache1.value > this.cache2.value;
			return programCounter + 1;
		}
	}

	private final class Float64CachedScalarLeqExecutor extends Float64CachedScalarComparisonExecutor {
		public Float64CachedScalarLeqExecutor(BoolCache cache0, Float64Cache cache1, Float64Cache cache2) {
			super(cache0, cache1, cache2);
		}
		public final int execute(int programCounter) {
			this.cache0.value = this.cache1.value <= this.cache2.value;
			return programCounter + 1;
		}
	}

	private final class Float64CachedScalarGeqExecutor extends Float64CachedScalarComparisonExecutor {
		public Float64CachedScalarGeqExecutor(BoolCache cache0, Float64Cache cache1, Float64Cache cache2) {
			super(cache0, cache1, cache2);
		}
		public final int execute(int programCounter) {
			this.cache0.value = this.cache1.value >= this.cache2.value;
			return programCounter + 1;
		}
	}

	private final class Float64CachedScalarEqExecutor extends Float64CachedScalarComparisonExecutor {
		public Float64CachedScalarEqExecutor(BoolCache cache0, Float64Cache cache1, Float64Cache cache2) {
			super(cache0, cache1, cache2);
		}
		public final int execute(int programCounter) {
			this.cache0.value = this.cache1.value == this.cache2.value;
			return programCounter + 1;
		}
	}

	private final class Float64CachedScalarNeqExecutor extends Float64CachedScalarComparisonExecutor {
		public Float64CachedScalarNeqExecutor(BoolCache cache0, Float64Cache cache1, Float64Cache cache2) {
			super(cache0, cache1, cache2);
		}
		public final int execute(int programCounter) {
			this.cache0.value = this.cache1.value != this.cache2.value;
			return programCounter + 1;
		}
	}
}
