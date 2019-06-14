package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class NopUnit extends AcceleratorExecutionUnit {

	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		AcceleratorExecutionNode node = null;
		switch (instruction.getOperationCode()) {
			case NOP : {
				node = new NopNode(nextNode);
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

	private final class NopNode extends AcceleratorExecutionNode {

		public NopNode(AcceleratorExecutionNode nextNode) {
			super(nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			return this.nextNode;
		}
	}
}
