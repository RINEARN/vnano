package org.vcssl.nano.accelerator;

import org.vcssl.nano.processor.OperationCode;

public class AcceleratorInstructionExtention {

	private final OperationCode[] operationCodes;
	private final AccelerationType accelerationType;

	public AcceleratorInstructionExtention(
			OperationCode operationCode, AccelerationType accelerationType) {

		this(new OperationCode[]{ operationCode }, accelerationType);
	}

	public AcceleratorInstructionExtention(
			OperationCode[] operationCodes, AccelerationType accelerationType) {

		this.operationCodes = operationCodes;
		this.accelerationType = accelerationType;
	}

	public OperationCode[] getOperationCodes() {
		return this.operationCodes;
	}

	public AccelerationType getAccelerationType() {
		return this.accelerationType;
	}

}
