package org.vcssl.nano.accelerator;

import org.vcssl.nano.processor.OperationCode;

public class AcceleratorInstructionExtention implements Cloneable {

	private OperationCode[] operationCodes = null;
	private AccelerationType accelerationType = null;
	private int jumpLabelId = -1;
	private int reorderedJumpAddress = -1;
	private boolean jumpAddressReordered = false;


	public void setOperationCodes(OperationCode operationCode) {
		this.operationCodes = new OperationCode[]{ operationCode };
	}

	public void setOperationCodes(OperationCode[] operationCodes) {
		this.operationCodes = operationCodes;
	}

	public OperationCode[] getOperationCodes() {
		return this.operationCodes;
	}


	public void setAccelerationType(AccelerationType accelerationType) {
		this.accelerationType = accelerationType;
	}

	public AccelerationType getAccelerationType() {
		return this.accelerationType;
	}

	public void setJumpLabelId(int jumpLabelId) {
		this.jumpLabelId = jumpLabelId;
	}

	public int getJumpLabelId() {
		return jumpLabelId;
	}

	public void setReorderedJumpAddress(int reorderedJumpAddress) {
		this.reorderedJumpAddress = reorderedJumpAddress;
		this.jumpAddressReordered = true;
	}

	public int getReorderedJumpAddress() {
		return this.reorderedJumpAddress;
	}

	public boolean isJumpAddressReordered() {
		return this.jumpAddressReordered;
	}

}
