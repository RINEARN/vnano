/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.processor.OperationCode;

public class Float64CachedScalarTransferUnit extends AccelerationUnit {

	@Override
	public AccelerationExecutorNode generateExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant) {

		AccelerationExecutorNode executor = null;
		switch (opcode) {
			case MOV :
			case FILL : {
				executor = new Float64CachedScalarMovExecutor(
						(Float64Cache)operandCaches[0], (Float64Cache)operandCaches[1]);
				break;
			}
			case CAST : {
				if (dataTypes[1] == DataType.FLOAT64) {
					executor = new Float64CachedScalarMovExecutor(
							(Float64Cache)operandCaches[0], (Float64Cache)operandCaches[1]);
				}
				if (dataTypes[1] == DataType.INT64) {
					executor = new Float64FromInt64CachedScalarCastExecutor(
							(Float64Cache)operandCaches[0], (Int64Cache)operandCaches[1]);
				}
				break;
			}
			default : break;
		}
		return executor;
	}

	private class Float64CachedScalarMovExecutor extends AccelerationExecutorNode {
		protected final Float64Cache cache0;
		protected final Float64Cache cache1;

		public Float64CachedScalarMovExecutor(Float64Cache cache0, Float64Cache cache1) {
			this.cache0 = cache0;
			this.cache1 = cache1;
		}

		public final int execute(int programCounter) {
			this.cache0.value = this.cache1.value;
			return programCounter + 1;
		}
	}

	private class Float64FromInt64CachedScalarCastExecutor extends AccelerationExecutorNode {
		protected final Float64Cache cache0;
		protected final Int64Cache cache1;

		public Float64FromInt64CachedScalarCastExecutor(Float64Cache cache0, Int64Cache cache1) {
			this.cache0 = cache0;
			this.cache1 = cache1;
		}

		public final int execute(int programCounter) {
			this.cache0.value = this.cache1.value;
			return programCounter + 1;
		}
	}
}
