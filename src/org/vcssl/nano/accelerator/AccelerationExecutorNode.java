/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */
package org.vcssl.nano.accelerator;

public abstract class AccelerationExecutorNode {
	protected AccelerationExecutorNode nextNode = null;

	public abstract AccelerationExecutorNode execute();

	public final void setNextNode(AccelerationExecutorNode nextNode) {
		this.nextNode = nextNode;
	}

	public void setBranchedNode(AccelerationExecutorNode branchedNode) {
	}
}
