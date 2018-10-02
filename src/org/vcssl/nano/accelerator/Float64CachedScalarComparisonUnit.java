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
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		BoolScalarCache caches0 = (BoolScalarCache)operandCaches[0];
		Float64ScalarCache caches1 = (Float64ScalarCache)operandCaches[1];
		Float64ScalarCache caches2 = (Float64ScalarCache)operandCaches[2];

		Float64CachedScalarComparisonExecutor executor = null;
		switch (opcode) {
			case LT : {
				executor = new Float64CachedScalarLtExecutor(caches0, caches1, caches2, nextNode);
				break;
			}
			case GT : {
				executor = new Float64CachedScalarGtExecutor(caches0, caches1, caches2, nextNode);
				break;
			}
			case LEQ : {
				executor = new Float64CachedScalarLeqExecutor(caches0, caches1, caches2, nextNode);
				break;
			}
			case GEQ : {
				executor = new Float64CachedScalarGeqExecutor(caches0, caches1, caches2, nextNode);
				break;
			}
			case EQ : {
				executor = new Float64CachedScalarEqExecutor(caches0, caches1, caches2, nextNode);
				break;
			}
			case NEQ : {
				executor = new Float64CachedScalarNeqExecutor(caches0, caches1, caches2, nextNode);
				break;
			}
			default : {
				break;
			}
		}
		return executor;
	}

	private abstract class Float64CachedScalarComparisonExecutor extends AccelerationExecutorNode {
		protected final BoolScalarCache cache0;
		protected final Float64ScalarCache cache1;
		protected final Float64ScalarCache cache2;

		public Float64CachedScalarComparisonExecutor(BoolScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.cache0 = cache0;
			this.cache1 = cache1;
			this.cache2 = cache2;
		}
	}

	private final class Float64CachedScalarLtExecutor extends Float64CachedScalarComparisonExecutor {
		public Float64CachedScalarLtExecutor(BoolScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value < this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarGtExecutor extends Float64CachedScalarComparisonExecutor {
		public Float64CachedScalarGtExecutor(BoolScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value > this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarLeqExecutor extends Float64CachedScalarComparisonExecutor {
		public Float64CachedScalarLeqExecutor(BoolScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value <= this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarGeqExecutor extends Float64CachedScalarComparisonExecutor {
		public Float64CachedScalarGeqExecutor(BoolScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value >= this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarEqExecutor extends Float64CachedScalarComparisonExecutor {
		public Float64CachedScalarEqExecutor(BoolScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value == this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarNeqExecutor extends Float64CachedScalarComparisonExecutor {
		public Float64CachedScalarNeqExecutor(BoolScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value != this.cache2.value;
			return this.nextNode;
		}
	}
}
