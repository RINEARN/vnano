/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;

public class BoolCachedScalarTransferUnit extends AccelerationUnit {

	@Override
	public AccelerationExecutorNode generateExecutor(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		AccelerationExecutorNode executor = null;
		switch (instruction.getOperationCode()) {
			case FILL :
			case MOV : {
				executor = new BoolCachedScalarMovExecutor(
						(BoolScalarCache)operandCaches[0], (BoolScalarCache)operandCaches[1], nextNode);
				break;
			}
			case CAST : {
				if (instruction.getDataTypes()[1] == DataType.BOOL) {
					executor = new BoolCachedScalarMovExecutor(
							(BoolScalarCache)operandCaches[0], (BoolScalarCache)operandCaches[1], nextNode);
					break;
				} else {
					throw new VnanoFatalException(
							instruction.getDataTypes()[1] + "-type operand of " + instruction.getOperationCode()
							+ " instruction is invalid for " + this.getClass().getCanonicalName()
					);
				}
			}
			default : {
				throw new VnanoFatalException(
						"Operation code " + instruction.getOperationCode() + " is invalid for " + this.getClass().getCanonicalName()
				);
			}
		}
		return executor;
	}

	private class BoolCachedScalarMovExecutor extends AccelerationExecutorNode {
		protected final BoolScalarCache cache0;
		protected final BoolScalarCache cache1;

		public BoolCachedScalarMovExecutor(BoolScalarCache cache0, BoolScalarCache cache1, AccelerationExecutorNode nextNode) {
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
