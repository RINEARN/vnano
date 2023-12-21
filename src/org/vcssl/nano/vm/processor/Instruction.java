/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.processor;

import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.Memory;


/**
 * The class of an instruction of the {@link Processor}.
 *
 * The specifications of the instructions supported by the Processor
 * are defined as the virtual assembly language VRIL ( https://www.vcssl.org/en-us/vril/isa/instruction ).
 *
 * An instance of this class represents an instruction (one line) in the VRIL assembly code.
 * (The VRIL assembly code is a text code, but it is converted to instances of this class by the Assembler in the VM.)
 */
public class Instruction implements Cloneable {

	/** The operation code of this instruction. */
	private final OperationCode operationCode;

	/** The data-types of the operands of this instruction. */
	private final DataType[] dataTypes;

	/** The memory partitions in which the data of the operands of this instruction are stored. */
	private final Memory.Partition[] operandPartitions;

	/** The memory addresses at which the data of the operands of this instruction are stored. */
	private final int[] operandAddresses;

	/** The memory partition in which the meta information of this instruction is stored. */
	private final Memory.Partition metaPartition;

	/** The memory address at which the meta information of this instruction is stored. */
	private final int metaAddress;

	/** Stores additional information, if this instruction is an extension instruction (operation code: EX). */
	private final Object extension;

	/**
	 * Creates a new instruction having the specified information.
	 *
	 * @param operationCode The operation code.
	 * @param dataTypes The data-type of the operation (what it means depends on the specification for each operation code).
	 * @param operandPartitions The memory partitions in which the data of the operands are stored.
	 * @param operandAddresses The memory addresses at which the data of the operands are stored.
	 * @param metaPartition The memory partition in which the meta information is stored.
	 * @param metaAddress The memory address at which the meta information is stored.
	 */
	public Instruction(
			OperationCode operationCode, DataType[] dataTypes,
			Memory.Partition[] operandPartitions, int[] operandAddresses,
			Memory.Partition metaPartition, int metaAddress) {

		this(operationCode, dataTypes, operandPartitions, operandAddresses, metaPartition, metaAddress, null);
	}

	/**
	 * Creates a new extended instruction having the specified information.
	 *
	 * @param operationCode The operation code.
	 * @param dataTypes The data-type of the operation (what it means depends on the specification for each operation code).
	 * @param operandPartitions The memory partitions in which the data of the operands are stored.
	 * @param operandAddresses The memory addresses at which the data of the operands are stored.
	 * @param metaPartition The memory partition in which the meta information is stored.
	 * @param metaAddress The memory address at which the meta information is stored.
	 * @param extension The additional information for the extended instruction.
	 */
	public Instruction(
			OperationCode operationCode, DataType[] dataTypes,
			Memory.Partition[] operandPartitions, int[] operandAddresses,
			Memory.Partition metaPartition, int metaAddress,
			Object extension) {

		this.operationCode = operationCode;
		this.dataTypes = dataTypes;
		this.operandPartitions = operandPartitions;
		this.operandAddresses = operandAddresses;
		this.metaPartition = metaPartition;
		this.metaAddress = metaAddress;
		this.extension = extension;
	}


	/**
	 * Gets the data-types of the operation.
	 *
	 * What it means depends on the specification for each operation code
	 * (see: https://www.vcssl.org/en-us/vril/isa/instruction ).
	 *
	 * @return The data-types of the operation.
	 */
	public DataType[] getDataTypes() {
		return this.dataTypes;
	}


	/**
	 * Gets the operation code of this instruction.
	 *
	 * @return The operation code.
	 */
	public OperationCode getOperationCode() {
		return this.operationCode;
	}


	/**
	 * Gets the number of the operands of this instruction.
	 *
	 * @return The number of the operands.
	 */
	public int getOperandLength() {
		return this.operandPartitions.length;
	}


	/**
	 * Gets the memory partitions in which the data of the operands of this instruction are stored.
	 *
	 * @return The memory partitions of the operands.
	 */
	public Memory.Partition[] getOperandPartitions() {
		return this.operandPartitions;
	}


	/**
	 * Gets the memory addresses at which the data of the operands of this instruction are stored.
	 *
	 * @return The memory addresses of the operands.
	 */
	public int[] getOperandAddresses() {
		return this.operandAddresses;
	}


	/**
	 * Gets the memory partition in which the meta information of this instruction is stored.
	 *
	 * @return The memory partition of the meta information.
	 */
	public Memory.Partition getMetaPartition() {
		return this.metaPartition;
	}


	/**
	 * Gets the memory address at which the meta information of this instruction is stored.
	 *
	 * @return The memory address of the meta information.
	 */
	public int getMetaAddress() {
		return this.metaAddress;
	}


	/**
	 * Returns whether this instructions has the information object for the extended instruction.
	 *
	 * @return Returns true if this instructions has the information for the extended instruction.
	 */
	public boolean hasExtention() {
		return this.extension != null;
	}


	/**
	 * Gets the information object for the extended instruction.
	 *
	 * @return The information object for the extended instruction.
	 */
	public Object getExtension() {
		return this.extension;
	}


	/**
	 * Clones this instruction.
	 *
	 * All fields excluding "extension" will be deep-copied.
	 * The field "extension" will be shallow-copied.
	 *
	 * @return The cloned instruction.
	 */
	public Instruction clone() {

		DataType[] cloneDataTypes = new DataType[this.dataTypes.length];
		System.arraycopy(this.dataTypes, 0, cloneDataTypes, 0, this.dataTypes.length);

		Memory.Partition[] cloneOperandPartitions = new Memory.Partition[this.operandPartitions.length];
		System.arraycopy(this.operandPartitions, 0, cloneOperandPartitions, 0, this.operandPartitions.length);

		int[] cloneOperandAddresses = new int[this.operandAddresses.length];
		System.arraycopy(this.operandAddresses, 0, cloneOperandAddresses, 0, this.operandAddresses.length);

		Instruction cloneInstruction = new Instruction(
				this.operationCode, cloneDataTypes,
				cloneOperandPartitions, cloneOperandAddresses,
				this.metaPartition, this.metaAddress,
				this.extension
		);

		return cloneInstruction;
	}


	/**
	 * Clone this instruction with specifying the information object of the extended instruction.
	 *
	 * @param extension The information object of the extended instruction.
	 * @return The cloned instruction.
	 */
	public Instruction clone(Object extension) {

		DataType[] cloneDataTypes = new DataType[this.dataTypes.length];
		System.arraycopy(this.dataTypes, 0, cloneDataTypes, 0, this.dataTypes.length);

		Memory.Partition[] cloneOperandPartitions = new Memory.Partition[this.operandPartitions.length];
		System.arraycopy(this.operandPartitions, 0, cloneOperandPartitions, 0, this.operandPartitions.length);

		int[] cloneOperandAddresses = new int[this.operandAddresses.length];
		System.arraycopy(this.operandAddresses, 0, cloneOperandAddresses, 0, this.operandAddresses.length);

		Instruction cloneInstruction = new Instruction(
				this.operationCode, cloneDataTypes,
				cloneOperandPartitions, cloneOperandAddresses,
				this.metaPartition, this.metaAddress,
				extension
		);

		return cloneInstruction;
	}


	/**
	 * Returns the string to dump the content of this instruction.
	 *
	 * @return The string to dump the content of this instruction.
	 */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[ ");
		builder.append(this.operationCode);
		builder.append("\t");
		int dataTypeLength = this.dataTypes.length;
		for (int i=0; i<dataTypeLength; i++) {
			builder.append(this.dataTypes[i]);
			if (i != dataTypeLength - 1) {
				builder.append(":");
			}
		}
		builder.append("\t");
		int operandLength = this.operandAddresses.length;
		for (int i=0; i<operandLength; i++) {
			builder.append(this.operandPartitions[i].toString().charAt(0));
			builder.append(this.operandAddresses[i]);
			builder.append("\t");
		}
		builder.append(this.metaPartition.toString().charAt(0));
		builder.append(this.metaAddress);

		if (this.extension != null) {
			builder.append("\t extension={ ");
			builder.append(this.extension.toString());
			builder.append(" }");
		}

		builder.append(" ]");
		return builder.toString();
	}
}
