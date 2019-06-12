/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vcssl.nano.VnanoScriptEngine;
import org.vcssl.nano.spec.AssemblyWord;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.processor.Instruction;


/**
 * <p>
 * VM内部での実行に適した形に変換された中間コードである、VMオブジェクトコードのクラスです。
 * </p>
 *
 * <p>
 * VMオブジェクトコードは、仮想プロセッサで直接実行可能な命令オブジェクト列と、
 * 仮想メモリー確保のためのシンボルテーブルなどの情報を保持しています。
 * </p>
 *
 * <p>
 * VnanoのVM層は、可読なテキスト形式の中間アセンブリコード（VRILコード）を入力として受け取りますが、
 * それをVM内部のアセンブラにより、VMオブジェクトコードに変換してから実行します。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class VirtualMachineObjectCode implements Cloneable {

	private List<Instruction> instructionList = null;

	private List<Integer> registerAddressList = null;
	private List<Integer> constantDataAddressList = null;
	private List<Integer> localVariableAddressList = null;
	private List<Integer> globalVariableAddressList = null;
	private List<Integer> functionAddressList = null;
	private List<Integer> labelAddressList = null;

	private List<String> constantDataValueList = null;
	private List<String> localVariableIdentifierList = null;
	private List<String> globalVariableIdentifierList = null;
	private List<String> functionIdentifierList = null;
	private List<String> labelIdentifierList = null;

	private Map<Integer, String> constantDataAddressValueMap = null;
	private Map<Integer, String> localVariableAddressIdentifierMap = null;
	private Map<Integer, String> globalVariableAddressIdentifierMap = null;
	private Map<Integer, String> functionAddressIdentifierMap = null;
	private Map<Integer, String> labelAddressIdentifierMap = null;

	/** {@link VnanoScriptEngine#eval VnanoScriptEngine.eval} の戻り値が格納される、ローカルパーティション内アドレスを保持します。 */
	private int evalValueAddress = -1;

	public VirtualMachineObjectCode() {

		this.instructionList = new ArrayList<Instruction>();

		this.registerAddressList = new ArrayList<Integer>();
		this.constantDataAddressList = new ArrayList<Integer>();
		this.localVariableAddressList = new ArrayList<Integer>();
		this.globalVariableAddressList = new ArrayList<Integer>();
		this.functionAddressList = new ArrayList<Integer>();
		this.labelAddressList = new ArrayList<Integer>();

		this.constantDataValueList = new ArrayList<String>();
		this.localVariableIdentifierList = new ArrayList<String>();
		this.globalVariableIdentifierList = new ArrayList<String>();
		this.functionIdentifierList = new ArrayList<String>();
		this.labelIdentifierList = new ArrayList<String>();

		this.constantDataAddressValueMap = new HashMap<Integer, String>();
		this.localVariableAddressIdentifierMap = new HashMap<Integer, String>();
		this.globalVariableAddressIdentifierMap = new HashMap<Integer, String>();
		this.functionAddressIdentifierMap = new HashMap<Integer, String>();
		this.labelAddressIdentifierMap = new HashMap<Integer, String>();
	}

	/**
	 * {@link VnanoScriptEngine#eval VnanoScriptEngine.eval} メソッドの戻り値として返すべき値が、存在するかどうかを返します。
	 *
	 * @return evalの戻り値に返すべき値があればtrue
	 */
	public boolean hasEvalValue() {
		return 0 <= this.evalValueAddress;
	}


	/**
	 * {@link VnanoScriptEngine#eval VnanoScriptEngine.eval} メソッドの戻り値として返すべき値が格納される、
	 * ローカルパーティション内アドレスを設定ます。
	 *
	 * @return evalの戻り値が格納されるローカルアドレス
	 */
	public void setEvalValueAddress(int address) {
		this.evalValueAddress = address;
	}


	/**
	 * {@link VnanoScriptEngine#eval VnanoScriptEngine.eval} メソッドの戻り値として返すべき値が格納される、
	 * ローカルパーティション内アドレスを設定ます。
	 *
	 * @return evalの戻り値が格納されるローカルアドレス
	 */
	public int getEvalValueAddress() {
		return this.evalValueAddress;
	}

	public void addInstruction(Instruction instruction) {
		this.instructionList.add(instruction);
	}

	public Instruction[] getInstructions() {
		return this.instructionList.toArray(new Instruction[0]);
	}

	public String[] getConstantImmediateValues() {
		return this.constantDataValueList.toArray(new String[0]);
	}

	public String[] getGlobalAssemblyIdentifiers() {
		return this.globalVariableIdentifierList.toArray(new String[0]);
	}

	public String[] getFunctionAssemblyIdentifiers() {
		return this.functionIdentifierList.toArray(new String[0]);
	}


	public void addRegister(int address) {
		this.registerAddressList.add(address);
	}
	public void addConstantData(String immediateValue, int address) {
		this.constantDataValueList.add(immediateValue);
		this.constantDataAddressList.add(address);
		this.constantDataAddressValueMap.put(address, immediateValue);
	}
	public void addLocalVariable(String uniqueIdentifier, int address) {
		this.localVariableIdentifierList.add(uniqueIdentifier);
		this.localVariableAddressList.add(address);
		this.localVariableAddressIdentifierMap.put(address, uniqueIdentifier);
	}
	public void addGlobalVariable(String uniqueIdentifier, int address) {
		this.globalVariableIdentifierList.add(uniqueIdentifier);
		this.globalVariableAddressList.add(address);
		this.globalVariableAddressIdentifierMap.put(address, uniqueIdentifier);
	}
	public void addFunction(String uniqueIdentifier, int address) {
		this.functionIdentifierList.add(uniqueIdentifier);
		this.functionAddressList.add(address);
		this.functionAddressIdentifierMap.put(address, uniqueIdentifier);
	}
	public void addLabel(String uniqueIdentifier, int address) {
		this.labelIdentifierList.add(uniqueIdentifier);
		this.labelAddressList.add(address);
		this.labelAddressIdentifierMap.put(address, uniqueIdentifier);
	}

	public boolean hasGlobalVariableRegisteredAt(int address) {
		return this.globalVariableAddressIdentifierMap.containsKey(address);
	}

	public int getLocalVariableAddress(String uniqueIdentifier) {
		int index = this.localVariableIdentifierList.indexOf(uniqueIdentifier);
		return this.localVariableAddressList.get(index);
	}
	public int getGlobalVariableAddress(String uniqueIdentifier) {
		int index = this.globalVariableIdentifierList.indexOf(uniqueIdentifier);
		return this.globalVariableAddressList.get(index);
	}
	public int getConstantDataAddress(String immediateValue) {
		int index = this.constantDataValueList.indexOf(immediateValue);
		return this.constantDataAddressList.get(index);
	}
	public int getLabelAddress(String uniqueIdentifier) {
		int index = this.labelIdentifierList.indexOf(uniqueIdentifier);
		return this.labelAddressList.get(index);
	}
	public int getFunctionAddress(String uniqueIdentifier) {
		int index = this.functionIdentifierList.indexOf(uniqueIdentifier);
		return this.functionAddressList.get(index);
	}

	public String getLocalVariableUniqueIdentifier(int address) {
		return this.localVariableAddressIdentifierMap.get(address);
	}
	public String getGlobalVariableUniqueIdentifier(int address) {
		return this.globalVariableAddressIdentifierMap.get(address);
	}
	public String getConstantDataImmediateValue(int address) {
		return this.constantDataAddressValueMap.get(address);
	}
	public String getFunctionUniqueIdentifier(int address) {
		return this.functionAddressIdentifierMap.get(address);
	}

	public boolean containsRegister(int address) {
		return this.registerAddressList.contains(address);
	}
	public boolean containsConstantData(String immediateValue) {
		return this.constantDataValueList.contains(immediateValue);
	}
	public boolean containsGlobalVariable(String uniqueIdentifier) {
		return this.globalVariableIdentifierList.contains(uniqueIdentifier);
	}
	public boolean containsLocalVariable(String uniqueIdentifier) {
		return this.localVariableIdentifierList.contains(uniqueIdentifier);
	}
	public boolean containsFunction(String uniqueIdentifier) {
		return this.functionIdentifierList.contains(uniqueIdentifier);
	}


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

	public int getMinimumRegisterAddress() {
		return 0;
	}
	public int getMaximumRegisterAddress() {
		return this.getMaxAddressOf(this.registerAddressList);
	}

	public int getMinimumLocalAddress() {
		return 0;
	}
	public int getMaximumLocalAddress() {
		return this.getMaxAddressOf(this.localVariableAddressList);
	}
	public int getMinimumGlobalAddress() {
		return 0;
	}
	public int getMaximumGlobalAddress() {
		return this.getMaxAddressOf(this.globalVariableAddressList);
	}
	public int getMinimumConstantAddress() {
		return 0;
	}
	public int getMaximumConstantAddress() {
		return this.getMaxAddressOf(this.constantDataAddressList);
	}
	public int getMinimumFunctionAddress() {
		return 0;
	}
	public int getMaximumFunctionAddress() {
		return this.getMaxAddressOf(this.functionAddressList);
	}

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
					builder.append(AssemblyWord.VALUE_SEPARATOR);
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
		for(int i=0; i<this.constantDataValueList.size(); i++) {
			int address = this.constantDataAddressList.get(i);
			String constantValue = this.constantDataValueList.get(i);
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
