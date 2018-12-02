/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class Float64CachedScalarArithmeticUnit extends AccelerationUnit {

	@Override
	public AccelerationExecutorNode generateExecutorNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		Float64ScalarCache[] caches = new Float64ScalarCache[]{
				(Float64ScalarCache)operandCaches[0],
				(Float64ScalarCache)operandCaches[1],
				(Float64ScalarCache)operandCaches[2]
		};

		Float64CachedScalarArithmeticExecutorNode executor = null;
		switch (instruction.getOperationCode()) {
			case ADD : {
				executor = new Float64CachedScalarAddExecutorNode(caches[0], caches[1], caches[2], nextNode);
				break;
			}
			case SUB : {
				executor = new Float64CachedScalarSubExecutorNode(caches[0], caches[1], caches[2], nextNode);
				break;
			}
			case MUL : {
				executor = new Float64CachedScalarMulExecutorNode(caches[0], caches[1], caches[2], nextNode);
				break;
			}
			case DIV : {
				executor = new Float64CachedScalarDivExecutorNode(caches[0], caches[1], caches[2], nextNode);
				break;
			}
			case REM : {
				executor = new Float64CachedScalarRemExecutorNode(caches[0], caches[1], caches[2], nextNode);
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

	private abstract class Float64CachedScalarArithmeticExecutorNode extends AccelerationExecutorNode {
		protected final Float64ScalarCache cache0;
		protected final Float64ScalarCache cache1;
		protected final Float64ScalarCache cache2;

		public Float64CachedScalarArithmeticExecutorNode(Float64ScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.cache0 = cache0;
			this.cache1 = cache1;
			this.cache2 = cache2;
		}
	}

	private final class Float64CachedScalarAddExecutorNode extends Float64CachedScalarArithmeticExecutorNode {
		public Float64CachedScalarAddExecutorNode(Float64ScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value + this.cache2.value;
			return this.nextNode;
		}
	}


	private final class Float64CachedScalarSubExecutorNode extends Float64CachedScalarArithmeticExecutorNode {
		public Float64CachedScalarSubExecutorNode(Float64ScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value - this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarMulExecutorNode extends Float64CachedScalarArithmeticExecutorNode {
		public Float64CachedScalarMulExecutorNode(Float64ScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value * this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarDivExecutorNode extends Float64CachedScalarArithmeticExecutorNode {
		public Float64CachedScalarDivExecutorNode(Float64ScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value / this.cache2.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarRemExecutorNode extends Float64CachedScalarArithmeticExecutorNode {
		public Float64CachedScalarRemExecutorNode(Float64ScalarCache cache0, Float64ScalarCache cache1, Float64ScalarCache cache2,
				AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value % this.cache2.value;
			return this.nextNode;
		}
	}
}
