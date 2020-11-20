/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.vm.memory.DataContainer;

public class BoolCachedScalarTransferUnit extends AcceleratorExecutionUnit {

	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		AcceleratorExecutionNode node = null;
		switch (instruction.getOperationCode()) {
			case FILL :
			case MOV : {
				node = new BoolCachedScalarMovNode(
						(BoolScalarCache)operandCaches[0], (BoolScalarCache)operandCaches[1], nextNode);
				break;
			}
			case CAST : {
				if (instruction.getDataTypes()[1] == DataType.BOOL) {
					// このユニットでは bool 以外の型を含む演算は対応しないので、bool 同士のキャストしか有り得ず、従って単に mov する
					node = new BoolCachedScalarMovNode(
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
		return node;
	}

	private class BoolCachedScalarMovNode extends AcceleratorExecutionNode {
		protected final BoolScalarCache cache0;
		protected final BoolScalarCache cache1;

		public BoolCachedScalarMovNode(BoolScalarCache cache0, BoolScalarCache cache1, AcceleratorExecutionNode nextNode) {
			super(nextNode, 1);
			this.cache0 = cache0;
			this.cache1 = cache1;
		}

		public final AcceleratorExecutionNode execute() {
			this.cache0.data = this.cache1.data;
			return this.nextNode;
		}
	}
}
