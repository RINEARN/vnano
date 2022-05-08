package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.processor.Instruction;

public class AcceleratorInstruction extends Instruction implements Cloneable {

	/** この命令を処理する {AcceleratorExecutionUnit AcceleratorExecutionUnit} を区別するために割りふられる分類タイプです。 */
	private AcceleratorExecutionType acceleratorExecutionType = null;

	/** 最適化による再配置後における、この命令の命令アドレスです。 */
	private int reorderedAddress = -1;

	/** 最適化による再配置前における、この命令の命令アドレスです。 */
	private int unreorderedAddress = -1;

	/** 最適化による再配置後における、分岐先ラベルの命令アドレスです。インライン展開された分岐命令でも、実行時の分岐先はこの値が参照されます。 */
	private int reorderedLabelAddress = -1;

	/** この命令がインライン展開によって生成された場合における、展開「直後」における命令アドレスです。 */
	private int expandedAddress = -1;

	/** この命令がインライン展開によって生成された場合における、展開「直後」における分岐先ラベルの命令アドレスです。 */
	private int expandedLabelAddress = -1;

	/** 最適化による複数命令の融合において、元の命令（複数）のオペレーションコードをまとめる配列です。 */
	private OperationCode[] fusedOperationCodes = null;

	/** 最適化による複合命令の融合において、入力オペランドの位置の識別に使用される配列です。 */
	private int[] fusedInputOperandIndices = null;

	/** 拡張命令（オペレーションコード EX）の処理内容を区別するための、拡張オペレーションコードを保持します。 */
	private AcceleratorExtendedOperationCode extendedOperationCode = null;

	@Override
	public AcceleratorInstruction clone() {

		// まず Instruction 型の範囲内でフィールドをディープコピーしたインスタンスを生成
		Instruction clonedInstruction = super.clone();

		// 上記を元に、このクラスのインスタンスを生成（この時点では、このクラスで拡張されたフィールド値は未コピー）
		AcceleratorInstruction clonedAccelInstruction = new AcceleratorInstruction( clonedInstruction );

		// このクラスで拡張されたフィールド値をコピー（配列はディープコピー）
		clonedAccelInstruction.acceleratorExecutionType = this.acceleratorExecutionType;
		clonedAccelInstruction.reorderedAddress = this.reorderedAddress;
		clonedAccelInstruction.unreorderedAddress = this.unreorderedAddress;
		clonedAccelInstruction.expandedAddress = this.expandedAddress;
		clonedAccelInstruction.reorderedLabelAddress = this.reorderedLabelAddress;
		clonedAccelInstruction.expandedLabelAddress = this.expandedLabelAddress;
		clonedAccelInstruction.extendedOperationCode = this.extendedOperationCode;
		if (this.fusedOperationCodes != null) {
			int length = this.fusedOperationCodes.length;
			clonedAccelInstruction.fusedOperationCodes = new OperationCode[ length ];
			System.arraycopy(this.fusedOperationCodes, 0, clonedAccelInstruction.fusedOperationCodes, 0, length);
		}
		if (this.fusedInputOperandIndices != null) {
			int length = this.fusedInputOperandIndices.length;
			clonedAccelInstruction.fusedInputOperandIndices = new int[ length ];
			System.arraycopy(this.fusedInputOperandIndices, 0, clonedAccelInstruction.fusedInputOperandIndices, 0, length);
		}
		return clonedAccelInstruction;
	}

	public AcceleratorInstruction(Instruction instruction) {
		super(
				instruction.getOperationCode(), instruction.getDataTypes(),
				instruction.getOperandPartitions(), instruction.getOperandAddresses(),
				instruction.getMetaPartition(), instruction.getMetaAddress()
		);
	}

	public AcceleratorInstruction(Instruction instruction, int unreorderedAddress) {
		this(instruction);
		this.unreorderedAddress = unreorderedAddress;
	}


	// AcceleratorInstruction をコピーし、オペランド部だけを置き換えたものを生成
	public AcceleratorInstruction(AcceleratorInstruction instruction,
			Memory.Partition[] operandPartitions, int[] operandAddresses) {

		super(
				instruction.getOperationCode(), instruction.getDataTypes(),
				operandPartitions, operandAddresses,
				instruction.getMetaPartition(), instruction.getMetaAddress()
		);
		this.acceleratorExecutionType = instruction.acceleratorExecutionType;
		this.fusedOperationCodes = instruction.fusedOperationCodes;
		this.reorderedAddress = instruction.reorderedAddress;
		this.unreorderedAddress = instruction.unreorderedAddress;
		this.expandedAddress = instruction.expandedAddress;
		this.reorderedLabelAddress = instruction.reorderedLabelAddress;
		this.expandedLabelAddress = instruction.expandedLabelAddress;
	}

	// AcceleratorInstruction をコピーし、オペランド部だけを置き換えたものを生成
	public AcceleratorInstruction(AcceleratorInstruction instruction,
			OperationCode operationCode, Memory.Partition[] operandPartitions, int[] operandAddresses) {

		super(
				operationCode, instruction.getDataTypes(),
				operandPartitions, operandAddresses,
				instruction.getMetaPartition(), instruction.getMetaAddress()
		);
		this.acceleratorExecutionType = instruction.acceleratorExecutionType;
		this.fusedOperationCodes = instruction.fusedOperationCodes;
		this.reorderedAddress = instruction.reorderedAddress;
		this.unreorderedAddress = instruction.unreorderedAddress;
		this.expandedAddress = instruction.expandedAddress;
		this.reorderedLabelAddress = instruction.reorderedLabelAddress;
		this.expandedLabelAddress = instruction.expandedLabelAddress;
	}


	// この命令と、指定された命令を融合した拡張命令（InstructionオペコードはEX）を生成
	public AcceleratorInstruction fuse(AcceleratorInstruction fusingInstructions, AcceleratorExecutionType fusedAccelerationType) {

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

		// リオーダリング用の情報を、融合の前側にある命令からコピー
		// (後側の命令の値を持たせたい場合は別途 set する)
		fusedInstruction.reorderedAddress = this.reorderedAddress;
		fusedInstruction.unreorderedAddress = this.unreorderedAddress;
		fusedInstruction.reorderedLabelAddress = this.reorderedLabelAddress;
		fusedInstruction.expandedAddress = this.reorderedLabelAddress;
		fusedInstruction.expandedLabelAddress = this.expandedLabelAddress;

		return fusedInstruction;
	}


	public boolean isFused() {
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


	public void setAccelerationType(AcceleratorExecutionType acceleratorExecutionType) {
		this.acceleratorExecutionType = acceleratorExecutionType;
	}

	public AcceleratorExecutionType getAccelerationType() {
		return this.acceleratorExecutionType;
	}

	public void setExtendedOperationCode(AcceleratorExtendedOperationCode extendedOperationCode) {
		this.extendedOperationCode = extendedOperationCode;
	}

	public AcceleratorExtendedOperationCode getExtendedOperationCode() {
		return this.extendedOperationCode;
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

	public void setExpandedAddress(int expandedAddress) {
		this.expandedAddress = expandedAddress;
	}

	public int getExpandedAddress() {
		return this.expandedAddress;
	}

	public boolean isExpanded() {
		return expandedAddress != -1;
	}

	public void setReorderedLabelAddress(int reorderedLabelAddress) {
		this.reorderedLabelAddress = reorderedLabelAddress;
	}

	public int getReorderedLabelAddress() {
		return this.reorderedLabelAddress;
	}

	public boolean isLabelAddressReordered() {
		return reorderedLabelAddress != -1;
	}

	public void setExpandedLabelAddress(int expandedLabelAddress) {
		this.expandedLabelAddress = expandedLabelAddress;
	}

	public int getExpandedLabelAddress() {
		return this.expandedLabelAddress;
	}

	public boolean isLabelAddressExpanded() {
		return expandedLabelAddress != -1;
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
				builder.append(":");
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

		if (this.acceleratorExecutionType != null) {
			builder.append("\t");
			builder.append(this.getAccelerationType());
			if (this.isFused()) {
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

		boolean isBranchOperation = this.getOperationCode() == OperationCode.JMP || this.getOperationCode() == OperationCode.JMPN;
		boolean isFusedBranchOperation = this.isFused()
				&& (this.fusedOperationCodes[1] == OperationCode.JMP || this.fusedOperationCodes[1] == OperationCode.JMPN);

		if (isBranchOperation || isFusedBranchOperation) {
			builder.append(" (reorderedLabelAddress=" + this.reorderedLabelAddress + ")");
		}

		builder.append(" ]");
		return builder.toString();
	}
}
