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
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		BoolScalarCache caches0 = (BoolScalarCache)operandCaches[0];
		Int64ScalarCache caches1 = (Int64ScalarCache)operandCaches[1];
		Int64ScalarCache caches2 = (Int64ScalarCache)operandCaches[2];

		Int64CachedScalarComparisonExecutor executor = null;
		switch (opcode) {
			case LT : {
				executor = new Int64CachedScalarLtExecutor(caches0, caches1, caches2, nextNode);
				break;
			}
			case GT : {
				executor = new Int64CachedScalarGtExecutor(caches0, caches1, caches2, nextNode);
				break;
			}
			case LEQ : {
				executor = new Int64CachedScalarLeqExecutor(caches0, caches1, caches2, nextNode);
				break;
			}
			case GEQ : {
				executor = new Int64CachedScalarGeqExecutor(caches0, caches1, caches2, nextNode);
				break;
			}
			case EQ : {
				executor = new Int64CachedScalarEqExecutor(caches0, caches1, caches2, nextNode);
				break;
			}
			case NEQ : {
				executor = new Int64CachedScalarNeqExecutor(caches0, caches1, caches2, nextNode);
				break;
			}
			default : {
				break;
			}
		}
		return executor;
	}

	private abstract class Int64CachedScalarComparisonExecutor extends AccelerationExecutorNode {
		protected final BoolScalarCache cache0;
		protected final Int64ScalarCache cache1;
		protected final Int64ScalarCache cache2;

		public Int64CachedScalarComparisonExecutor(BoolScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.cache0 = cache0;
			this.cache1 = cache1;
			this.cache2 = cache2;
		}
	}

	private final class Int64CachedScalarLtExecutor extends Int64CachedScalarComparisonExecutor {
		public Int64CachedScalarLtExecutor(BoolScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value < this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarGtExecutor extends Int64CachedScalarComparisonExecutor {
		public Int64CachedScalarGtExecutor(BoolScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value > this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarLeqExecutor extends Int64CachedScalarComparisonExecutor {
		public Int64CachedScalarLeqExecutor(BoolScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value <= this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarGeqExecutor extends Int64CachedScalarComparisonExecutor {
		public Int64CachedScalarGeqExecutor(BoolScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value >= this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarEqExecutor extends Int64CachedScalarComparisonExecutor {
		public Int64CachedScalarEqExecutor(BoolScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value == this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarNeqExecutor extends Int64CachedScalarComparisonExecutor {
		public Int64CachedScalarNeqExecutor(BoolScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value != this.cache2.value;
			return this.nextNode;
		}
	}
}
