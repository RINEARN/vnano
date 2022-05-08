/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.vm.memory.DataContainer;

public class Int64CachedScalarTransferUnit extends AcceleratorExecutionUnit {

	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		AcceleratorExecutionNode node = null;
		switch (instruction.getOperationCode()) {
			case MOV : {
				node = new Int64CachedScalarMovNode(
						(Int64ScalarCache)operandCaches[0], (Int64ScalarCache)operandCaches[1], nextNode);
				break;
			}
			case CAST : {
				if (instruction.getDataTypes()[1] == DataType.INT64) {
					node = new Int64CachedScalarMovNode(
							(Int64ScalarCache)operandCaches[0], (Int64ScalarCache)operandCaches[1], nextNode);
				} else if (instruction.getDataTypes()[1] == DataType.FLOAT64) {
					node = new Int64FromFloat64CachedScalarCastNode(
							(Int64ScalarCache)operandCaches[0], (Float64ScalarCache)operandCaches[1], nextNode);
				} else {
					throw new VnanoFatalException(
							instruction.getDataTypes()[1] + "-type operand of " + instruction.getOperationCode()
							+ " instruction is invalid for " + this.getClass().getCanonicalName()
					);
				}
				break;
			}
			case FILL : {
				node = new Int64CachedScalarMovNode(
						(Int64ScalarCache)operandCaches[0], (Int64ScalarCache)operandCaches[1], nextNode);
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

	private class Int64CachedScalarMovNode extends AcceleratorExecutionNode {
		protected final Int64ScalarCache cache0;
		protected final Int64ScalarCache cache1;

		public Int64CachedScalarMovNode(Int64ScalarCache cache0, Int64ScalarCache cache1, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.cache0 = cache0;
			this.cache1 = cache1;
		}

		public final AcceleratorExecutionNode execute() {
			this.cache0.data = this.cache1.data;
			return this.nextNode;
		}
	}

	private class Int64FromFloat64CachedScalarCastNode extends AcceleratorExecutionNode {
		protected final Int64ScalarCache cache0;
		protected final Float64ScalarCache cache1;

		public Int64FromFloat64CachedScalarCastNode(Int64ScalarCache cache0, Float64ScalarCache cache1,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.cache0 = cache0;
			this.cache1 = cache1;
		}

		public final AcceleratorExecutionNode execute() {
			this.cache0.data = (long)this.cache1.data;
			return this.nextNode;
		}
	}
}
