/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */
package org.vcssl.nano.vm.accelerator;

public abstract class AccelerationExecutorNode {

	protected final AccelerationExecutorNode nextNode;

	// 演算には使用しないが、エラー発生時に実行対象命令を辿れるように保持
	AcceleratorInstruction sourceInstruction;

	public AccelerationExecutorNode(AccelerationExecutorNode nextNode) {
		this.nextNode = nextNode;
	}

	public void setSourceInstruction(AcceleratorInstruction instruction) {
		this.sourceInstruction = instruction;
	}
	public AcceleratorInstruction getSourceInstruction() {
		return this.sourceInstruction;
	}

	public abstract AccelerationExecutorNode execute();

	public void setBranchedNode(AccelerationExecutorNode branchedNode) {
	}
}
