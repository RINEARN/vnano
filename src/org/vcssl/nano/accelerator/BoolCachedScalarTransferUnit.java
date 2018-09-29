/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.processor.OperationCode;

public class BoolCachedScalarTransferUnit extends AccelerationUnit {

	@Override
	public AccelerationExecutorNode generateExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant) {

		AccelerationExecutorNode executor = null;
		switch (opcode) {
			case FILL :
			case MOV : {
				executor = new BoolCachedScalarMovExecutor(
						(BoolCache)operandCaches[0], (BoolCache)operandCaches[1]);
				break;
			}
			case CAST : {
				if (dataTypes[1] == DataType.BOOL) {
					executor = new BoolCachedScalarMovExecutor(
							(BoolCache)operandCaches[0], (BoolCache)operandCaches[1]);
					break;
				}
			}
			default : {
				break;
			}
		}
		return executor;
	}

	private class BoolCachedScalarMovExecutor extends AccelerationExecutorNode {
		protected final BoolCache cache0;
		protected final BoolCache cache1;

		public BoolCachedScalarMovExecutor(BoolCache cache0, BoolCache cache1) {
			this.cache0 = cache0;
			this.cache1 = cache1;
		}

		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value;
			return this.nextNode;
		}
	}
}
