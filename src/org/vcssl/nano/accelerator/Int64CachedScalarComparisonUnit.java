/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.processor.OperationCode;

public class Int64CachedScalarComparisonUnit extends AccelerationUnit {

	@Override
	public AccelerationExecutorNode generateExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant) {

		BoolCache caches0 = (BoolCache)operandCaches[0];
		Int64Cache caches1 = (Int64Cache)operandCaches[1];
		Int64Cache caches2 = (Int64Cache)operandCaches[2];

		Int64CachedScalarComparisonExecutor executor = null;
		switch (opcode) {
			case LT : {
				executor = new Int64CachedScalarLtExecutor(caches0, caches1, caches2);
				break;
			}
			case GT : {
				executor = new Int64CachedScalarGtExecutor(caches0, caches1, caches2);
				break;
			}
			case LEQ : {
				executor = new Int64CachedScalarLeqExecutor(caches0, caches1, caches2);
				break;
			}
			case GEQ : {
				executor = new Int64CachedScalarGeqExecutor(caches0, caches1, caches2);
				break;
			}
			case EQ : {
				executor = new Int64CachedScalarEqExecutor(caches0, caches1, caches2);
				break;
			}
			case NEQ : {
				executor = new Int64CachedScalarNeqExecutor(caches0, caches1, caches2);
				break;
			}
			default : {
				break;
			}
		}
		return executor;
	}

	private abstract class Int64CachedScalarComparisonExecutor extends AccelerationExecutorNode {
		protected final BoolCache cache0;
		protected final Int64Cache cache1;
		protected final Int64Cache cache2;

		public Int64CachedScalarComparisonExecutor(BoolCache cache0, Int64Cache cache1, Int64Cache cache2) {
			this.cache0 = cache0;
			this.cache1 = cache1;
			this.cache2 = cache2;
		}

		public abstract int execute(int programCounter);
	}

	private final class Int64CachedScalarLtExecutor extends Int64CachedScalarComparisonExecutor {
		public Int64CachedScalarLtExecutor(BoolCache cache0, Int64Cache cache1, Int64Cache cache2) {
			super(cache0, cache1, cache2);
		}
		public final int execute(int programCounter) {
			this.cache0.value = this.cache1.value < this.cache2.value;
			return programCounter + 1;
		}
	}

	private final class Int64CachedScalarGtExecutor extends Int64CachedScalarComparisonExecutor {
		public Int64CachedScalarGtExecutor(BoolCache cache0, Int64Cache cache1, Int64Cache cache2) {
			super(cache0, cache1, cache2);
		}
		public final int execute(int programCounter) {
			this.cache0.value = this.cache1.value > this.cache2.value;
			return programCounter + 1;
		}
	}

	private final class Int64CachedScalarLeqExecutor extends Int64CachedScalarComparisonExecutor {
		public Int64CachedScalarLeqExecutor(BoolCache cache0, Int64Cache cache1, Int64Cache cache2) {
			super(cache0, cache1, cache2);
		}
		public final int execute(int programCounter) {
			this.cache0.value = this.cache1.value <= this.cache2.value;
			return programCounter + 1;
		}
	}

	private final class Int64CachedScalarGeqExecutor extends Int64CachedScalarComparisonExecutor {
		public Int64CachedScalarGeqExecutor(BoolCache cache0, Int64Cache cache1, Int64Cache cache2) {
			super(cache0, cache1, cache2);
		}
		public final int execute(int programCounter) {
			this.cache0.value = this.cache1.value >= this.cache2.value;
			return programCounter + 1;
		}
	}

	private final class Int64CachedScalarEqExecutor extends Int64CachedScalarComparisonExecutor {
		public Int64CachedScalarEqExecutor(BoolCache cache0, Int64Cache cache1, Int64Cache cache2) {
			super(cache0, cache1, cache2);
		}
		public final int execute(int programCounter) {
			this.cache0.value = this.cache1.value == this.cache2.value;
			return programCounter + 1;
		}
	}

	private final class Int64CachedScalarNeqExecutor extends Int64CachedScalarComparisonExecutor {
		public Int64CachedScalarNeqExecutor(BoolCache cache0, Int64Cache cache1, Int64Cache cache2) {
			super(cache0, cache1, cache2);
		}
		public final int execute(int programCounter) {
			this.cache0.value = this.cache1.value != this.cache2.value;
			return programCounter + 1;
		}
	}
}
