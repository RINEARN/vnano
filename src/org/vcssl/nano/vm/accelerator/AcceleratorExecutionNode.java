/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */
package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoException;

public abstract class AcceleratorExecutionNode {

	protected final AcceleratorExecutionNode nextNode;

	protected final int INSTRUCTIONS_PER_NODE;

	// 演算には使用しないが、エラー発生時に実行対象命令を辿れるように保持
	AcceleratorInstruction sourceInstruction;

	public AcceleratorExecutionNode(AcceleratorExecutionNode nextNode, int instructionsPerNode) {
		this.nextNode = nextNode;
		this.INSTRUCTIONS_PER_NODE = instructionsPerNode;
	}

	public void setSourceInstruction(AcceleratorInstruction instruction) {
		this.sourceInstruction = instruction;
	}
	public AcceleratorInstruction getSourceInstruction() {
		return this.sourceInstruction;
	}

	public abstract AcceleratorExecutionNode execute() throws VnanoException;

	public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
	}
}
