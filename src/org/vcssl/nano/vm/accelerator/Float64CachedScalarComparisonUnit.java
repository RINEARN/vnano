/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class Float64CachedScalarComparisonUnit extends AccelerationUnit {

	@Override
	public AccelerationExecutorNode generateExecutorNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		BoolScalarCache caches0 = (BoolScalarCache)operandCaches[0];
		Float64ScalarCache caches1 = (Float64ScalarCache)operandCaches[1];
		Float64ScalarCache caches2 = (Float64ScalarCache)operandCaches[2];

		Float64CachedScalarComparisonExecutorNode executor = null;
		switch (instruction.getOperationCode()) {
			case LT : {
				executor = new Float64CachedScalarLtExecutorNode(caches0, caches1, caches2, nextNode);
				break;
			}
			case GT : {
				executor = new Float64CachedScalarGtExecutorNode(caches0, caches1, caches2, nextNode);
				break;
			}
			case LEQ : {
				executor = new Float64CachedScalarLeqExecutorNode(caches0, caches1, caches2, nextNode);
				break;
			}
			case GEQ : {
				executor = new Float64CachedScalarGeqExecutorNode(caches0, caches1, caches2, nextNode);
				break;
			}
			case EQ : {
				executor = new Float64CachedScalarEqExecutorNode(caches0, caches1, caches2, nextNode);
				break;
			}
			case NEQ : {
				executor = new Float64CachedScalarNeqExecutorNode(caches0, caches1, caches2, nextNode);
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

	private abstract class Float64CachedScalarComparisonExecutorNode extends AccelerationExecutorNode {
		protected final BoolScalarCache cache0;
		protected final Float64ScalarCache cache1;
		protected final Float64ScalarCache cache2;

		public Float64CachedScalarComparisonExecutorNode(BoolScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.cache0 = cache0;
			this.cache1 = cache1;
			this.cache2 = cache2;
		}
	}

	private final class Float64CachedScalarLtExecutorNode extends Float64CachedScalarComparisonExecutorNode {
		public Float64CachedScalarLtExecutorNode(BoolScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value < this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarGtExecutorNode extends Float64CachedScalarComparisonExecutorNode {
		public Float64CachedScalarGtExecutorNode(BoolScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value > this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarLeqExecutorNode extends Float64CachedScalarComparisonExecutorNode {
		public Float64CachedScalarLeqExecutorNode(BoolScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value <= this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarGeqExecutorNode extends Float64CachedScalarComparisonExecutorNode {
		public Float64CachedScalarGeqExecutorNode(BoolScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value >= this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarEqExecutorNode extends Float64CachedScalarComparisonExecutorNode {
		public Float64CachedScalarEqExecutorNode(BoolScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value == this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarNeqExecutorNode extends Float64CachedScalarComparisonExecutorNode {
		public Float64CachedScalarNeqExecutorNode(BoolScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value != this.cache2.value;
			return this.nextNode;
		}
	}
}
