/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;

public class Float64CachedScalarTransferUnit extends AccelerationUnit {

	@Override
	public AccelerationExecutorNode generateExecutorNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		AccelerationExecutorNode executor = null;
		switch (instruction.getOperationCode()) {
			case MOV :
			case FILL : {
				executor = new Float64CachedScalarMovExecutorNode(
						(Float64ScalarCache)operandCaches[0], (Float64ScalarCache)operandCaches[1], nextNode);
				break;
			}
			case CAST : {
				if (instruction.getDataTypes()[1] == DataType.FLOAT64) {
					executor = new Float64CachedScalarMovExecutorNode(
							(Float64ScalarCache)operandCaches[0], (Float64ScalarCache)operandCaches[1], nextNode);
				} else if (instruction.getDataTypes()[1] == DataType.INT64) {
					executor = new Float64FromInt64CachedScalarCastExecutorNode(
							(Float64ScalarCache)operandCaches[0], (Int64ScalarCache)operandCaches[1], nextNode);
				} else {
					throw new VnanoFatalException(
							instruction.getDataTypes()[1] + "-type operand of " + instruction.getOperationCode()
							+ " instruction is invalid for " + this.getClass().getCanonicalName()
					);
				}
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

	private class Float64CachedScalarMovExecutorNode extends AccelerationExecutorNode {
		protected final Float64ScalarCache cache0;
		protected final Float64ScalarCache cache1;

		public Float64CachedScalarMovExecutorNode(Float64ScalarCache cache0, Float64ScalarCache cache1,
				AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.cache0 = cache0;
			this.cache1 = cache1;
		}

		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value;
			return this.nextNode;
		}
	}

	private class Float64FromInt64CachedScalarCastExecutorNode extends AccelerationExecutorNode {
		protected final Float64ScalarCache cache0;
		protected final Int64ScalarCache cache1;

		public Float64FromInt64CachedScalarCastExecutorNode(Float64ScalarCache cache0, Int64ScalarCache cache1,
				AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.cache0 = cache0;
			this.cache1 = cache1;
		}

		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value;
			return this.nextNode;
		}
	}
}
