/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class Float64CachedScalarComparisonUnit extends AcceleratorExecutionUnit {

	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		BoolScalarCache caches0 = (BoolScalarCache)operandCaches[0];
		Float64ScalarCache caches1 = (Float64ScalarCache)operandCaches[1];
		Float64ScalarCache caches2 = (Float64ScalarCache)operandCaches[2];

		Float64CachedScalarComparisonNode node = null;
		switch (instruction.getOperationCode()) {
			case LT : {
				node = new Float64CachedScalarLtNode(caches0, caches1, caches2, nextNode);
				break;
			}
			case GT : {
				node = new Float64CachedScalarGtNode(caches0, caches1, caches2, nextNode);
				break;
			}
			case LEQ : {
				node = new Float64CachedScalarLeqNode(caches0, caches1, caches2, nextNode);
				break;
			}
			case GEQ : {
				node = new Float64CachedScalarGeqNode(caches0, caches1, caches2, nextNode);
				break;
			}
			case EQ : {
				node = new Float64CachedScalarEqNode(caches0, caches1, caches2, nextNode);
				break;
			}
			case NEQ : {
				node = new Float64CachedScalarNeqNode(caches0, caches1, caches2, nextNode);
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

	private abstract class Float64CachedScalarComparisonNode extends AcceleratorExecutionNode {
		protected final BoolScalarCache cache0;
		protected final Float64ScalarCache cache1;
		protected final Float64ScalarCache cache2;

		public Float64CachedScalarComparisonNode(BoolScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.cache0 = cache0;
			this.cache1 = cache1;
			this.cache2 = cache2;
		}
	}

	private final class Float64CachedScalarLtNode extends Float64CachedScalarComparisonNode {
		public Float64CachedScalarLtNode(BoolScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.data = this.cache1.data < this.cache2.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarGtNode extends Float64CachedScalarComparisonNode {
		public Float64CachedScalarGtNode(BoolScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.data = this.cache1.data > this.cache2.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarLeqNode extends Float64CachedScalarComparisonNode {
		public Float64CachedScalarLeqNode(BoolScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.data = this.cache1.data <= this.cache2.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarGeqNode extends Float64CachedScalarComparisonNode {
		public Float64CachedScalarGeqNode(BoolScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.data = this.cache1.data >= this.cache2.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarEqNode extends Float64CachedScalarComparisonNode {
		public Float64CachedScalarEqNode(BoolScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.data = this.cache1.data == this.cache2.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarNeqNode extends Float64CachedScalarComparisonNode {
		public Float64CachedScalarNeqNode(BoolScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.data = this.cache1.data != this.cache2.data;
			return this.nextNode;
		}
	}
}
