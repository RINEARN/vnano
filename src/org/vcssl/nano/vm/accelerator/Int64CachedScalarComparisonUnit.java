/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class Int64CachedScalarComparisonUnit extends AcceleratorExecutionUnit {

	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		BoolScalarCache caches0 = (BoolScalarCache)operandCaches[0];
		Int64ScalarCache caches1 = (Int64ScalarCache)operandCaches[1];
		Int64ScalarCache caches2 = (Int64ScalarCache)operandCaches[2];

		Int64CachedScalarComparisonNode node = null;
		switch (instruction.getOperationCode()) {
			case LT : {
				node = new Int64CachedScalarLtNode(caches0, caches1, caches2, nextNode);
				break;
			}
			case GT : {
				node = new Int64CachedScalarGtNode(caches0, caches1, caches2, nextNode);
				break;
			}
			case LEQ : {
				node = new Int64CachedScalarLeqNode(caches0, caches1, caches2, nextNode);
				break;
			}
			case GEQ : {
				node = new Int64CachedScalarGeqNode(caches0, caches1, caches2, nextNode);
				break;
			}
			case EQ : {
				node = new Int64CachedScalarEqNode(caches0, caches1, caches2, nextNode);
				break;
			}
			case NEQ : {
				node = new Int64CachedScalarNeqNode(caches0, caches1, caches2, nextNode);
				break;
			}
			default : {
				throw new VnanoFatalException(
						"Operation code " + instruction.getOperationCode() + " is invalid for " + this.getClass().getCanonicalName()
				);
			}
		}
		return node;
	}

	private abstract class Int64CachedScalarComparisonNode extends AcceleratorExecutionNode {
		protected final BoolScalarCache cache0;
		protected final Int64ScalarCache cache1;
		protected final Int64ScalarCache cache2;

		public Int64CachedScalarComparisonNode(BoolScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.cache0 = cache0;
			this.cache1 = cache1;
			this.cache2 = cache2;
		}
	}

	private final class Int64CachedScalarLtNode extends Int64CachedScalarComparisonNode {
		public Int64CachedScalarLtNode(BoolScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.value = this.cache1.value < this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarGtNode extends Int64CachedScalarComparisonNode {
		public Int64CachedScalarGtNode(BoolScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.value = this.cache1.value > this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarLeqNode extends Int64CachedScalarComparisonNode {
		public Int64CachedScalarLeqNode(BoolScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.value = this.cache1.value <= this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarGeqNode extends Int64CachedScalarComparisonNode {
		public Int64CachedScalarGeqNode(BoolScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.value = this.cache1.value >= this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarEqNode extends Int64CachedScalarComparisonNode {
		public Int64CachedScalarEqNode(BoolScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.value = this.cache1.value == this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarNeqNode extends Int64CachedScalarComparisonNode {
		public Int64CachedScalarNeqNode(BoolScalarCache cache0, Int64ScalarCache cache1, Int64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.value = this.cache1.value != this.cache2.value;
			return this.nextNode;
		}
	}
}
