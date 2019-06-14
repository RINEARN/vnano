package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.spec.AssemblyWord;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.processor.Instruction;

public class AcceleratorInstruction extends Instruction {

	/** この命令を処理する {AcceleratorExecutionUnit AcceleratorExecutionUnit} を区別するために割りふられる分類タイプです。 */
	private AccelerationType accelerationType = null;

	/** 最適化による再配置後における、この命令の命令アドレスです。 */
	private int reorderedAddress = -1;

	/** 最適化による再配置前における、この命令の命令アドレスです。 */
	private int unreorderedAddress = -1;

	/** 最適化による再配置後における、ラベルオペランドの命令アドレスです。 */
	private int reorderedLabelAddress = -1;

	/** ラベルオペランドの命令アドレスが、再配置済みかどうかを保持します。 */
	private boolean labelAddressReordered = false;

	/** 最適化による複数命令の融合において、元の命令（複数）のオペレーションコードをまとめる配列です。 */
	private OperationCode[] fusedOperationCodes = null;

	/** 最適化による複合命令の融合において、入力オペランドの位置の識別に使用される配列です。 */
	private int[] fusedInputOperandIndices = null;


	public AcceleratorInstruction(Instruction instruction) {
		super(
				instruction.getOperationCode(), instruction.getDataTypes(),
				instruction.getOperandPartitions(), instruction.getOperandAddresses(),
				instruction.getMetaPartition(), instruction.getMetaAddress()
		);
	}


	// AcceleratorInstruction をコピーし、オペランド部だけを置き換えたものを生成
	public AcceleratorInstruction(AcceleratorInstruction instruction,
			Memory.Partition[] operandPartitions, int[] operandAddresses) {

		super(
				instruction.getOperationCode(), instruction.getDataTypes(),
				operandPartitions, operandAddresses,
				instruction.getMetaPartition(), instruction.getMetaAddress()
		);
		this.accelerationType = instruction.accelerationType;
		this.fusedOperationCodes = instruction.fusedOperationCodes;
		this.reorderedAddress = instruction.reorderedAddress;
		this.unreorderedAddress = instruction.unreorderedAddress;
		this.reorderedLabelAddress = instruction.reorderedLabelAddress;
		this.labelAddressReordered = instruction.labelAddressReordered;
	}

	// AcceleratorInstruction をコピーし、オペランド部だけを置き換えたものを生成
	public AcceleratorInstruction(AcceleratorInstruction instruction,
			OperationCode operationCode, Memory.Partition[] operandPartitions, int[] operandAddresses) {

		super(
				operationCode, instruction.getDataTypes(),
				operandPartitions, operandAddresses,
				instruction.getMetaPartition(), instruction.getMetaAddress()
		);
		this.accelerationType = instruction.accelerationType;
		this.fusedOperationCodes = instruction.fusedOperationCodes;
		this.reorderedAddress = instruction.reorderedAddress;
		this.unreorderedAddress = instruction.unreorderedAddress;
		this.reorderedLabelAddress = instruction.reorderedLabelAddress;
		this.labelAddressReordered = instruction.labelAddressReordered;
	}


	// この命令と、指定された命令を融合した拡張命令（InstructionオペコードはEX）を生成
	public AcceleratorInstruction fuse(AcceleratorInstruction fusingInstructions, AccelerationType fusedAccelerationType) {

		int thisOperandLength = this.getOperandLength();
		int fusingOperandLength = fusingInstructions.getOperandLength();
		Memory.Partition[] thisOperandPartitions = this.getOperandPartitions();
		Memory.Partition[] fusingOperandPartitions = fusingInstructions.getOperandPartitions();
		int[] thisOperandAddresses = this.getOperandAddresses();
		int[] fusingOperandAddresses = fusingInstructions.getOperandAddresses();


		// 以下、融合オペランドを fusedOperandPartitions, fusedOperandAddresses に生成

		// 融合オペランドの長さを求める
		int fusedOperandLength = thisOperandLength + fusingOperandLength;

		// thisOperandPartitions と fusingOperandPartitions を結合した配列 fusedOperandPartitions を作る
		Memory.Partition[] fusedOperandPartitions = new Memory.Partition[ fusedOperandLength ];
		// thisOperandPartitions を fusedOperandPartitions へ頭から thisOperandLength 個転送
		System.arraycopy(thisOperandPartitions, 0, fusedOperandPartitions, 0, thisOperandLength);
		// fusingOperandPartitions を fusedOperandPartitions へ thisOperandLength 番目から fusingOperandLength 個転送
		System.arraycopy(fusingOperandPartitions, 0, fusedOperandPartitions, thisOperandLength, fusingOperandLength);

		// thisOperandAddresses と fusedOperandAddresses を結合した配列 fusedOperandAddresses を作る
		int[] fusedOperandAddresses = new int[ fusedOperandLength ];
		// thisOperandAddresses を fusedOperandAddresses へ頭から thisOperandLength 個転送
		System.arraycopy(thisOperandAddresses, 0, fusedOperandAddresses, 0, thisOperandLength);
		// fusingOperandAddresses を fusedOperandAddresses へ thisOperandLength 番目から fusingOperandLength 個転送
		System.arraycopy(fusingOperandAddresses, 0, fusedOperandAddresses, thisOperandLength, fusingOperandLength);


		// 融合オペコードを生成
		OperationCode[] newFusedOperationCodes = null;
		if (this.fusedOperationCodes == null) {
			newFusedOperationCodes = new OperationCode[]{ this.getOperationCode(), fusingInstructions.getOperationCode() };
		} else {
			int currentLength = this.fusedOperationCodes.length;
			OperationCode[] buffer = new OperationCode[ currentLength ];
			System.arraycopy(this.fusedOperationCodes, 0, buffer, 0, currentLength);
			newFusedOperationCodes = new OperationCode[ currentLength + 1 ];
			System.arraycopy(buffer, 0, newFusedOperationCodes, 0, currentLength);
			newFusedOperationCodes[ currentLength ] = fusingInstructions.getOperationCode();
		}

		// 融合オペコードとオペランドから融合命令を生成
		AcceleratorInstruction fusedInstruction = new AcceleratorInstruction(
				this, OperationCode.EX, fusedOperandPartitions, fusedOperandAddresses
		);
		fusedInstruction.setFusedOperationCodes(newFusedOperationCodes);
		fusedInstruction.setAccelerationType(fusedAccelerationType);

		return fusedInstruction;
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


	public void setFusedInputOperandIndices(int[] indices) {
		this.fusedInputOperandIndices = indices;
	}

	public int[] getFusedInputOperandIndices() {
		return this.fusedInputOperandIndices;
	}


	public void setAccelerationType(AccelerationType accelerationType) {
		this.accelerationType = accelerationType;
	}

	public AccelerationType getAccelerationType() {
		return this.accelerationType;
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

	public void setReorderedLabelAddress(int reorderedLabelAddress) {
		this.reorderedLabelAddress = reorderedLabelAddress;
		this.labelAddressReordered = true;
	}

	public int getReorderedLabelAddress() {
		return this.reorderedLabelAddress;
	}

	public boolean isLabelAddressReordered() {
		return this.labelAddressReordered;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[ ");
		builder.append(this.getOperationCode());

		builder.append("\t");
		int dataTypeLength = this.getDataTypes().length;
		for (int i=0; i<dataTypeLength; i++) {
			builder.append(this.getDataTypes()[i]);
			if (i != dataTypeLength - 1) {
				builder.append(AssemblyWord.VALUE_SEPARATOR);
			}
		}
		builder.append("\t");
		int operandLength = this.getOperandAddresses().length;
		for (int i=0; i<operandLength; i++) {
			builder.append(this.getOperandPartitions()[i].toString().charAt(0));
			builder.append(this.getOperandAddresses()[i]);
			builder.append("\t");
		}
		builder.append(this.getMetaPartition().toString().charAt(0));
		builder.append(this.getMetaAddress());

		if (this.accelerationType != null) {
			builder.append("\t");
			builder.append(this.getAccelerationType());
			if (this.hasFusedOperationCodes()) {
				builder.append("(");
				for (int j=0; j<this.fusedOperationCodes.length; j++) {
					builder.append(this.fusedOperationCodes[j]);
					if (j != this.fusedOperationCodes.length-1) {
						builder.append(",");
					}
				}
				builder.append(")");
			}
		}

		builder.append(" ]");
		return builder.toString();
	}
}
