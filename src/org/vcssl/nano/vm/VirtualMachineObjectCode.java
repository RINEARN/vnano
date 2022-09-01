/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vcssl.nano.VnanoScriptEngine;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.processor.Instruction;


/**
 * The class of low-level intermediate code executable on the Processor/Accelerator of the VM, 
 * assembled from VRIL assembly code.
 */
public class VirtualMachineObjectCode implements Cloneable {

	/** The list of instructions, to be executed by the Processor/Accelerator of the VM. */
	private List<Instruction> instructionList = null;

	/** The list of addresses of registers. */
	private List<Integer> registerAddressList = null;

	/** The list of addresses of constant values. */
	private List<Integer> constantDataAddressList = null;

	/** The list of addresses of local (internal) variables. */
	private List<Integer> localVariableAddressList = null;

	/** The list of addresses of global (external) variables. */
	private List<Integer> globalVariableAddressList = null;

	/** The list of addresses of functions. */
	private List<Integer> functionAddressList = null;

	/** The list of addresses of labels. */
	private List<Integer> labelAddressList = null;

	/** The list of immediate value literals of constant values. */
	private List<String> constantDataImmediateValueList = null;

	/** The list of data containers of constant values. */ 
	private List<DataContainer<?>> constantDataContainerList = null;

	/** The list of identifiers of local (internal) variables. */
	private List<String> localVariableIdentifierList = null;

	/** The list of identifiers of global (external) variables. */
	private List<String> globalVariableIdentifierList = null;

	/** The list of identifiers of functions. */
	private List<String> functionIdentifierList = null;

	/** The list of identifiers of labels. */
	private List<String> labelIdentifierList = null;

	/** The Map mapping the address of each constant value to its immediate value literal. */
	private Map<Integer, String> constantDataAddressImmediateValueMap = null;

	/** The Map mapping the address of each constant value to its data container. */
	private Map<Integer, DataContainer<?>> constantDataAddressContainerMap = null;

	/** The Map mapping the address of each local (internal) variable to its identifier. */
	private Map<Integer, String> localVariableAddressIdentifierMap = null;

	/** The Map mapping the address of each global (external) variable to its identifier. */
	private Map<Integer, String> globalVariableAddressIdentifierMap = null;

	/** The Map mapping the address of each function to its identifier. */
	private Map<Integer, String> functionAddressIdentifierMap = null;

	/** The Map mapping the address of each label to its identifier. */
	private Map<Integer, String> labelAddressIdentifierMap = null;

	/** The address of the evaluation result value of this code, if exist. */
	private int evalValueAddress = -1;

	/**
	 * Create an instance of an empty code.
	 */
	public VirtualMachineObjectCode() {

		this.instructionList = new ArrayList<Instruction>();

		this.registerAddressList = new ArrayList<Integer>();
		this.constantDataAddressList = new ArrayList<Integer>();
		this.localVariableAddressList = new ArrayList<Integer>();
		this.globalVariableAddressList = new ArrayList<Integer>();
		this.functionAddressList = new ArrayList<Integer>();
		this.labelAddressList = new ArrayList<Integer>();

		this.constantDataImmediateValueList = new ArrayList<String>();
		this.constantDataContainerList = new ArrayList<DataContainer<?>>();
		this.localVariableIdentifierList = new ArrayList<String>();
		this.globalVariableIdentifierList = new ArrayList<String>();
		this.functionIdentifierList = new ArrayList<String>();
		this.labelIdentifierList = new ArrayList<String>();

		this.constantDataAddressImmediateValueMap = new HashMap<Integer, String>();
		this.constantDataAddressContainerMap = new HashMap<Integer, DataContainer<?>>();
		this.localVariableAddressIdentifierMap = new HashMap<Integer, String>();
		this.globalVariableAddressIdentifierMap = new HashMap<Integer, String>();
		this.functionAddressIdentifierMap = new HashMap<Integer, String>();
		this.labelAddressIdentifierMap = new HashMap<Integer, String>();
	}


	/**
	 * Returns whether this code has the evaluation result value.
	 *
	 * @return Returns true if this code has the evaluation result value.
	 */
	public boolean hasEvalValue() {
		return 0 <= this.evalValueAddress;
	}

	/**
	 * Sets the memory address (LOCAL partition) in which the evaluation result value will be stored.
	 *
	 * @param address The memory address (LOCAL partition) in which the evaluation result value will be stored.
	 */
	public void setEvalValueAddress(int address) {
		this.evalValueAddress = address;
	}

	/**
	 * Gets the memory address (LOCAL partition) in which the evaluation result value will be stored.
	 *
	 * @return The memory address (LOCAL partition) in which the evaluation result value will be stored.
	 */
	public int getEvalValueAddress() {
		return this.evalValueAddress;
	}


	/**
	 * Add an instruction to this code.
	 * 
	 * @param instruction The instruction to be added.
	 */
	public void addInstruction(Instruction instruction) {
		this.instructionList.add(instruction);
	}

	/**
	 * Gets all instructions.
	 * 
	 * @return All instructions.
	 */
	public Instruction[] getInstructions() {
		return this.instructionList.toArray(new Instruction[0]);
	}

	/**
	 * Gets immediate value literals of all constant values.
	 * 
	 * @return Immediate value literals of all constant values.
	 */
	public String[] getConstantImmediateValues() {
		return this.constantDataImmediateValueList.toArray(new String[0]);
	}

	/**
	 * Gets data containers of all constant values.
	 * 
	 * @return Data containers of all constant values.
	 */
	public DataContainer<?>[] getConstantDataContainers() {
		return this.constantDataContainerList.toArray(new DataContainer<?>[0]);
	}

	/**
	 * Gets identifiers of all global variables.
	 * 
	 * @return Identifiers of all global variables.
	 */
	public String[] getGlobalAssemblyIdentifiers() {
		return this.globalVariableIdentifierList.toArray(new String[0]);
	}

	/**
	 * Gets identifiers of all functions.
	 * 
	 * @return Identifiers of all functions.
	 */
	public String[] getFunctionAssemblyIdentifiers() {
		return this.functionIdentifierList.toArray(new String[0]);
	}


	/**
	 * Add a new register to the register list of this code.
	 * 
	 * @param address The memory address (REGISTER partition) of the register to be added (0 for R0, 1 for R1, ...).
	 */
	public void addRegister(int address) {
		this.registerAddressList.add(address);
	}

	/**
	 * Add a new constant data to the constant data list of this code.
	 * 
	 * @param immediateValue The immediate value literal of the constant data to be added.
	 * @param container The data container in which the constant data is stored.
	 * @param address The memory address (CONSTANT partition) of the constant data.
	 */
	public void addConstantData(String immediateValue, DataContainer<?> container, int address) {
		this.constantDataImmediateValueList.add(immediateValue);
		this.constantDataContainerList.add(container);
		this.constantDataAddressList.add(address);
		this.constantDataAddressImmediateValueMap.put(address, immediateValue);
		this.constantDataAddressContainerMap.put(address, container);
	}

	/**
	 * Add a new local variable to the local variable list(s) of this code.
	 * 
	 * @param uniqueIdentifier The unique identifier of the local variable to be added.
	 * @param address The memory address (LOCAL partition) to which data of the variable will be stored.
	 */
	public void addLocalVariable(String uniqueIdentifier, int address) {
		this.localVariableIdentifierList.add(uniqueIdentifier);
		this.localVariableAddressList.add(address);
		this.localVariableAddressIdentifierMap.put(address, uniqueIdentifier);
	}

	/**
	 * Add a new global variable to the global variable list(s) of this code.
	 * 
	 * @param uniqueIdentifier The unique identifier of the global variable to be added.
	 * @param address The memory address (GLOBAL partition) to which data of the variable will be stored.
	 */
	public void addGlobalVariable(String uniqueIdentifier, int address) {
		this.globalVariableIdentifierList.add(uniqueIdentifier);
		this.globalVariableAddressList.add(address);
		this.globalVariableAddressIdentifierMap.put(address, uniqueIdentifier);
	}

	/**
	 * Add a new function to the function list(s) of this code.
	 * 
	 * @param uniqueIdentifier The unique identifier of the function to be added.
	 * @param address The address of the function.
	 */
	public void addFunction(String uniqueIdentifier, int address) {
		this.functionIdentifierList.add(uniqueIdentifier);
		this.functionAddressList.add(address);
		this.functionAddressIdentifierMap.put(address, uniqueIdentifier);
	}

	/**
	 * Add a new label to the label list(s) of this code.
	 * 
	 * @param uniqueIdentifier The unique identifier of the label to be added.
	 * @param address The address of the label.
	 */
	public void addLabel(String uniqueIdentifier, int address) {
		this.labelIdentifierList.add(uniqueIdentifier);
		this.labelAddressList.add(address);
		this.labelAddressIdentifierMap.put(address, uniqueIdentifier);
	}

	/**
	 * Returns whether a global variable having the specified address is registered.
	 * 
	 * @return Returns true if a global variable having the specified address is registered.
	 */
	public boolean hasGlobalVariableRegisteredAt(int address) {
		return this.globalVariableAddressIdentifierMap.containsKey(address);
	}

	/**
	 * Gets the address (LOCAL partition) of the local variable having the specified unique identifier.
	 * 
	 * @param uniqueIdentifier The unique identifier of the local variable.
	 * @return The address (LOCAL partition) of the local variable.
	 */
	public int getLocalVariableAddress(String uniqueIdentifier) {
		int index = this.localVariableIdentifierList.indexOf(uniqueIdentifier);
		return this.localVariableAddressList.get(index);
	}

	/**
	 * Gets the address (GLOBAL partition) of the global variable having the specified unique identifier.
	 * 
	 * @param uniqueIdentifier The unique identifier of the local variable.
	 * @return The address (GLOBAL partition) of the local variable.
	 */
	public int getGlobalVariableAddress(String uniqueIdentifier) {
		int index = this.globalVariableIdentifierList.indexOf(uniqueIdentifier);
		return this.globalVariableAddressList.get(index);
	}

	/**
	 * Gets the address (CONSTANT partition) of the constant data corresponding with the specified immediate value literal.
	 * 
	 * @param immediateValue The immediate value literal of the constant data.
	 * @return The address (CONSTANT partition) of the constant data.
	 */
	public int getConstantDataAddress(String immediateValue) {
		int index = this.constantDataImmediateValueList.indexOf(immediateValue);
		return this.constantDataAddressList.get(index);
	}

	/**
	 * Gets the instruction address of the label having the specified unique identifier.
	 * 
	 * @param uniqueIdentifier The unique identifier of the label.
	 * @return The instruction address of the label.
	 */
	public int getLabelAddress(String uniqueIdentifier) {
		int index = this.labelIdentifierList.indexOf(uniqueIdentifier);
		return this.labelAddressList.get(index);
	}

	/**
	 * Gets the address of the function having the specified unique identifier.
	 *
	 * @param uniqueIdentifier The unique identifier of the function.
	 * @return The address of the function.
	 */
	public int getFunctionAddress(String uniqueIdentifier) {
		int index = this.functionIdentifierList.indexOf(uniqueIdentifier);
		return this.functionAddressList.get(index);
	}


	/**
	 * Gets the unique identifier of the local variable corresponding with the address.
	 * 
	 * @param address The address (LOCAL partition) corresponding with the local variable.
	 * @return The unique identifier of the local variable.
	 */
	public String getLocalVariableUniqueIdentifier(int address) {
		return this.localVariableAddressIdentifierMap.get(address);
	}

	/**
	 * Gets the unique identifier of the global variable corresponding with the address.
	 * 
	 * @param address The address (GLOBAL partition) corresponding with the global variable.
	 * @return The unique identifier of the global variable.
	 */
	public String getGlobalVariableUniqueIdentifier(int address) {
		return this.globalVariableAddressIdentifierMap.get(address);
	}

	/**
	 * Gets the immediate value literal of the constant data corresponding with the address.
	 * 
	 * @param address The address (CONSTANT partition) corresponding with the constant data.
	 * @return The immediate value literal of the constant data.
	 */
	public String getConstantDataImmediateValue(int address) {
		return this.constantDataAddressImmediateValueMap.get(address);
	}

	/**
	 * Gets the unique identifier of the function corresponding with the address.
	 * 
	 * @param address The address corresponding with the function.
	 * @return The unique identifier of the function.
	 */
	public String getFunctionUniqueIdentifier(int address) {
		return this.functionAddressIdentifierMap.get(address);
	}

	/**
	 * Returns whether the register corresponding with the specified address is registered.
	 * 
	 * @param address The address (REGISTER partition) of the register.
	 * @return Returns true if the register corresponding with the specified address is registered.
	 */
	public boolean containsRegister(int address) {
		return this.registerAddressList.contains(address);
	}

	/**
	 * Returns whether the constant data corresponding with the specified address is registered.
	 * 
	 * @param address The address (CONSTANT partition) of the constant data.
	 * @return Returns true if the constant data corresponding with the specified address is registered.
	 */
	public boolean containsConstantData(String immediateValue) {
		return this.constantDataImmediateValueList.contains(immediateValue);
	}

	/**
	 * Returns whether the global variable corresponding with the specified address is registered.
	 * 
	 * @param address The address (GLOBAL partition) of the global variable.
	 * @return Returns true if the global variable corresponding with the specified address is registered.
	 */
	public boolean containsGlobalVariable(String uniqueIdentifier) {
		return this.globalVariableIdentifierList.contains(uniqueIdentifier);
	}

	/**
	 * Returns whether the local variable corresponding with the specified address is registered.
	 * 
	 * @param address The address (LOCAL partition) of the local variable.
	 * @return Returns true if the local variable corresponding with the specified address is registered.
	 */
	public boolean containsLocalVariable(String uniqueIdentifier) {
		return this.localVariableIdentifierList.contains(uniqueIdentifier);
	}

	/**
	 * Returns whether the function corresponding with the specified address is registered.
	 * 
	 * @param address The address of the function.
	 * @return Returns true if the function corresponding with the specified address is registered.
	 */
	public boolean containsFunction(String uniqueIdentifier) {
		return this.functionIdentifierList.contains(uniqueIdentifier);
	}


	/**
	 * Returns the maximum address contained in the specified address list.
	 * 
	 * @param addressList The List storing addresses.
	 * @return The maximum address contained in addressList.
	 */
	private int getMaxAddressOf(List<Integer> addressList) {
		int max = -1;
		int size = addressList.size();
		for (int i=0; i<size; i++) {
			int address = addressList.get(i);
			if (max < address) {
				max = address;
			}
		}
		return max;
	}

	/**
	 * Gets the minimum address of registers.
	 * 
	 * @return The minimum address of registers.
	 */
	public int getMinimumRegisterAddress() {
		return 0;
	}

	/**
	 * Gets the maximum address of registers.
	 * 
	 * @return The maximum address of registers.
	 */
	public int getMaximumRegisterAddress() {
		return this.getMaxAddressOf(this.registerAddressList);
	}

	/**
	 * Gets the minimum address of local variables.
	 * 
	 * @return The minimum address of local variables.
	 */
	public int getMinimumLocalAddress() {
		return 0;
	}

	/**
	 * Gets the maximum address of local variables.
	 * 
	 * @return The maximum address of local variables.
	 */
	public int getMaximumLocalAddress() {
		return this.getMaxAddressOf(this.localVariableAddressList);
	}

	/**
	 * Gets the minimum address of global variables.
	 * 
	 * @return The minimum address of global variables.
	 */
	public int getMinimumGlobalAddress() {
		return 0;
	}

	/**
	 * Gets the maximum address of global variables.
	 * 
	 * @return The maximum address of global variables.
	 */
	public int getMaximumGlobalAddress() {
		return this.getMaxAddressOf(this.globalVariableAddressList);
	}

	/**
	 * Gets the minimum address of constant data.
	 * 
	 * @return The minimum address of constant data.
	 */
	public int getMinimumConstantAddress() {
		return 0;
	}

	/**
	 * Gets the maximum address of constant data.
	 * 
	 * @return The maximum address of constant data.
	 */
	public int getMaximumConstantAddress() {
		return this.getMaxAddressOf(this.constantDataAddressList);
	}

	/**
	 * Gets the minimum address of functions.
	 * 
	 * @return The minimum address of functions.
	 */
	public int getMinimumFunctionAddress() {
		return 0;
	}

	/**
	 * Gets the maximum address of functions.
	 * 
	 * @return The maximum address of functions.
	 */
	public int getMaximumFunctionAddress() {
		return this.getMaxAddressOf(this.functionAddressList);
	}

	/**
	 * Generate the String to dump the content of this code.
	 * 
	 * @return The content of this code.
	 */
	public String dump() {
		String eol = System.getProperty("line.separator");
		StringBuilder builder = new StringBuilder();

		builder.append("#INSTRUCTION");
		builder.append(eol);

		int instructionLength = this.instructionList.size();
		for(int i=0; i<instructionLength; i++) {
			Instruction instruction = this.instructionList.get(i);

			builder.append("\t");
			builder.append(i);
			builder.append("\t");
			builder.append(instruction.getOperationCode());
			builder.append("\t");
			int dataTypeLength = instruction.getDataTypes().length;
			for (int d=0; d<dataTypeLength; d++) {
				builder.append(instruction.getDataTypes()[d]);
				if (d != dataTypeLength - 1) {
					builder.append(":");
				}
			}
			if (dataTypeLength == 1) {
				builder.append("\t");
			}
			builder.append("\t");
			int operandLength = instruction.getOperandAddresses().length;
			for (int o=0; o<operandLength; o++) {
				builder.append(instruction.getOperandPartitions()[o].toString().charAt(0));
				builder.append(instruction.getOperandAddresses()[o]);
				builder.append("\t");
			}
			builder.append(instruction.getMetaPartition().toString().charAt(0));
			builder.append(instruction.getMetaAddress());
			builder.append(eol);
		}
		builder.append(eol);

		builder.append("#LABEL" + eol);
		for(int i=0; i<this.labelIdentifierList.size(); i++) {
			builder.append("\t" + this.labelAddressList.get(i) + "\t" + this.labelIdentifierList.get(i) + eol);
		}
		builder.append(eol);
		builder.append("#FUNCTION" + eol);
		for(int i=0; i<this.functionIdentifierList.size(); i++) {
			int address = this.functionAddressList.get(i);
			String identifier = this.functionIdentifierList.get(i);
			builder.append("\t" + address + "\t" + identifier + eol);
		}
		builder.append(eol);

		builder.append("#GLOBAL_DATA" + eol);
		for(int i=0; i<this.globalVariableIdentifierList.size(); i++) {
			int address = this.globalVariableAddressList.get(i);
			String identifier = this.globalVariableIdentifierList.get(i);
			builder.append("\t" + Memory.Partition.GLOBAL.toString().charAt(0) + address + "\t" + identifier + eol);
		}
		builder.append(eol);

		builder.append("#LOCAL_DATA" + eol);
		for(int i=0; i<this.localVariableIdentifierList.size(); i++) {
			int address = this.localVariableAddressList.get(i);
			String identifier = this.localVariableIdentifierList.get(i);
			builder.append("\t" + Memory.Partition.LOCAL.toString().charAt(0) + address + "\t" + identifier + eol);
		}
		builder.append(eol);

		builder.append("#CONSTANT_DATA" + eol);
		for(int i=0; i<this.constantDataImmediateValueList.size(); i++) {
			int address = this.constantDataAddressList.get(i);
			String constantValue = this.constantDataImmediateValueList.get(i);
			builder.append("\t" + Memory.Partition.CONSTANT.toString().charAt(0) + address + "\t" + constantValue + eol);
		}
		builder.append(eol);

		builder.append("#REGISTER" + eol);
		int minRegisterAddress = this.getMinimumRegisterAddress();
		int maxRegisterAddress = this.getMaximumRegisterAddress();
		for(int i=minRegisterAddress; i<=maxRegisterAddress; i++) {
			if (i==minRegisterAddress || i==maxRegisterAddress) {
				builder.append("\t" + Memory.Partition.REGISTER.toString().charAt(0) + i + eol);
			} else if (i==minRegisterAddress+1) {
				builder.append("\t" + "..." + eol);
			}
		}

		return builder.toString();
	}

}
