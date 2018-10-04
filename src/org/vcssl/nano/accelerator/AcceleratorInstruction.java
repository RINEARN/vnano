package org.vcssl.nano.accelerator;

import org.vcssl.nano.processor.Instruction;
import org.vcssl.nano.processor.OperationCode;

public class AcceleratorInstruction extends Instruction {

	private AccelerationType accelerationType = null;
	private OperationCode[] fusedOperationCodes = null;
	private int jumpLabelId = -1;
	private int reorderedAddress = -1;
	private int unreorderedAddress = -1;
	private int reorderedJumpAddress = -1;
	private boolean jumpAddressReordered = false;


	public AcceleratorInstruction(Instruction instruction) {
		super(
				instruction.getOperationCode(), instruction.getDataTypes(),
				instruction.getOperandPartitions(), instruction.getOperandAddresses(),
				instruction.getMetaPartition(), instruction.getMetaAddress()
		);
	}


	public boolean hasFusedOperationCodes() {
		return this.fusedOperationCodes != null;
	}

	public void setFusedOperationCodes(OperationCode fusedOperationCode) {
		this.fusedOperationCodes = new OperationCode[]{ fusedOperationCode };
	}

	public void setFusedOperationCodes(OperationCode[] fusedOperationCodes) {
		this.fusedOperationCodes = fusedOperationCodes;
	}

	public OperationCode[] getFusedOperationCodes() {
		return this.fusedOperationCodes;
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

	public void setReorderedAddress(int reorderedAddress) {
		this.reorderedAddress = reorderedAddress;
	}

	public int getReorderedAddress() {
		return this.reorderedAddress;
	}

	public void setUnreorderedAddress(int unreorderedAddress) {
		this.unreorderedAddress = unreorderedAddress;
	}

	public int getUnreorderedAddress() {
		return this.unreorderedAddress;
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
