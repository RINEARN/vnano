/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */
package org.vcssl.nano.accelerator;

public abstract class AccelerationExecutorNode {
	protected final AccelerationExecutorNode nextNode;

	public AccelerationExecutorNode(AccelerationExecutorNode nextNode) {
		this.nextNode = nextNode;
	}

	public abstract AccelerationExecutorNode execute();

	public void setBranchedNode(AccelerationExecutorNode branchedNode) {
	}
}
