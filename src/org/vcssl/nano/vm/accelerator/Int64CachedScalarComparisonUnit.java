/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.memory.DataContainer;

public class Int64CachedScalarComparisonUnit extends AccelerationUnit {

	@Override
	public AccelerationExecutorNode generateExecutorNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		BoolScalarCache caches0 = (BoolScalarCache)operandCaches[0];
		Int64ScalarCache caches1 = (Int64ScalarCache)operandCaches[1];
		Int64ScalarCache caches2 = (Int64ScalarCache)operandCaches[2];

		Int64CachedScalarComparisonExecutorNode executor = null;
		switch (instruction.getOperationCode()) {
			case LT : {
				executor = new Int64CachedScalarLtExecutorNode(caches0, caches1, caches2, nextNode);
				break;
			}
			case GT : {
				executor = new Int64CachedScalarGtExecutorNode(caches0, caches1, caches2, nextNode);
				break;
			}
			case LEQ : {
				executor = new Int64CachedScalarLeqExecutorNode(caches0, caches1, caches2, nextNode);
				break;
			}
			case GEQ : {
				executor = new Int64CachedScalarGeqExecutorNode(caches0, caches1, caches2, nextNode);
				break;
			}
			case EQ : {
				executor = new Int64CachedScalarEqExecutorNode(caches0, caches1, caches2, nextNode);
				break;
			}
			case NEQ : {
				executor = new Int64CachedScalarNeqExecutorNode(caches0, caches1, caches2, nextNode);
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

	private abstract class Int64CachedScalarComparisonExecutorNode extends AccelerationExecutorNode {
		protected final BoolScalarCache cache0;
		protected final Int64ScalarCache cache1;
		protected final Int64ScalarCache cache2;

		public Int64CachedScalarComparisonExecutorNode(BoolScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.cache0 = cache0;
			this.cache1 = cache1;
			this.cache2 = cache2;
		}
	}

	private final class Int64CachedScalarLtExecutorNode extends Int64CachedScalarComparisonExecutorNode {
		public Int64CachedScalarLtExecutorNode(BoolScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value < this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarGtExecutorNode extends Int64CachedScalarComparisonExecutorNode {
		public Int64CachedScalarGtExecutorNode(BoolScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value > this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarLeqExecutorNode extends Int64CachedScalarComparisonExecutorNode {
		public Int64CachedScalarLeqExecutorNode(BoolScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value <= this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarGeqExecutorNode extends Int64CachedScalarComparisonExecutorNode {
		public Int64CachedScalarGeqExecutorNode(BoolScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value >= this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarEqExecutorNode extends Int64CachedScalarComparisonExecutorNode {
		public Int64CachedScalarEqExecutorNode(BoolScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value == this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarNeqExecutorNode extends Int64CachedScalarComparisonExecutorNode {
		public Int64CachedScalarNeqExecutorNode(BoolScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value != this.cache2.value;
			return this.nextNode;
		}
	}
}
