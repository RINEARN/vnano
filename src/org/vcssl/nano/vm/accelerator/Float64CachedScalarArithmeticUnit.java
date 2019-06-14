/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class Float64CachedScalarArithmeticUnit extends AcceleratorExecutionUnit {

	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		Float64ScalarCache[] caches = new Float64ScalarCache[]{
				(Float64ScalarCache)operandCaches[0],
				(Float64ScalarCache)operandCaches[1],
				(Float64ScalarCache)operandCaches[2]
		};

		Float64CachedScalarArithmeticNode node = null;
		switch (instruction.getOperationCode()) {
			case ADD : {
				node = new Float64CachedScalarAddNode(caches[0], caches[1], caches[2], nextNode);
				break;
			}
			case SUB : {
				node = new Float64CachedScalarSubNode(caches[0], caches[1], caches[2], nextNode);
				break;
			}
			case MUL : {
				node = new Float64CachedScalarMulNode(caches[0], caches[1], caches[2], nextNode);
				break;
			}
			case DIV : {
				node = new Float64CachedScalarDivNode(caches[0], caches[1], caches[2], nextNode);
				break;
			}
			case REM : {
				node = new Float64CachedScalarRemNode(caches[0], caches[1], caches[2], nextNode);
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

	private abstract class Float64CachedScalarArithmeticNode extends AcceleratorExecutionNode {
		protected final Float64ScalarCache cache0;
		protected final Float64ScalarCache cache1;
		protected final Float64ScalarCache cache2;

		public Float64CachedScalarArithmeticNode(Float64ScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.cache0 = cache0;
			this.cache1 = cache1;
			this.cache2 = cache2;
		}
	}

	private final class Float64CachedScalarAddNode extends Float64CachedScalarArithmeticNode {
		public Float64CachedScalarAddNode(Float64ScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.value = this.cache1.value + this.cache2.value;
			return this.nextNode;
		}
	}


	private final class Float64CachedScalarSubNode extends Float64CachedScalarArithmeticNode {
		public Float64CachedScalarSubNode(Float64ScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.value = this.cache1.value - this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarMulNode extends Float64CachedScalarArithmeticNode {
		public Float64CachedScalarMulNode(Float64ScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.value = this.cache1.value * this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarDivNode extends Float64CachedScalarArithmeticNode {
		public Float64CachedScalarDivNode(Float64ScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.value = this.cache1.value / this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarRemNode extends Float64CachedScalarArithmeticNode {
		public Float64CachedScalarRemNode(Float64ScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AcceleratorExecutionNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache0.value = this.cache1.value % this.cache2.value;
			return this.nextNode;
		}
	}
}
