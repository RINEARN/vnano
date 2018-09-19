/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vcssl.nano.memory.Memory;
import org.vcssl.nano.processor.Instruction;
import org.vcssl.nano.spec.AssemblyWord;


/**
 * <p>
 * Vnano処理系における中間コードのクラスです。
 * </p>
 *
 * <p>
 * 中間コードは、仮想プロセッサで直接実行可能な命令オブジェクト列と、
 * 仮想メモリー確保のためのシンボルテーブルなどの情報を保持しています。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class VnanoIntermediateCode extends CompiledScript implements Cloneable {

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

	/** {@link VnanoEngine#eval VnanoEngine.eval} の戻り値が格納される、ローカルパーティション内アドレスを保持します。 */
	private int evalValueAddress = -1;

	private VnanoEngine engine = null;

	public VnanoIntermediateCode() {

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
	 * {@link VnanoEngine#eval VnanoEngine.eval} メソッドの戻り値として返すべき値が、存在するかどうかを返します。
	 *
	 * @return evalの戻り値に返すべき値があればtrue
	 */
	public boolean hasEvalValue() {
		return 0 <= this.evalValueAddress;
	}


	/**
	 * {@link VnanoEngine#eval VnanoEngine.eval} メソッドの戻り値として返すべき値が格納される、
	 * ローカルパーティション内アドレスを設定ます。
	 *
	 * @return evalの戻り値が格納されるローカルアドレス
	 */
	public void setEvalValueAddress(int address) {
		this.evalValueAddress = address;
	}


	/**
	 * {@link VnanoEngine#eval VnanoEngine.eval} メソッドの戻り値として返すべき値が格納される、
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




	public int getMinimumRegisterAddress() {
		return 0;
	}
	public int getMaximumRegisterAddress() {
		return registerAddressList.size() - 1;
	}

	public int getMinimumLocalAddress() {
		return 0;
	}
	public int getMaximumLocalAddress() {
		return this.localVariableIdentifierList.size() - 1;
	}
	public int getMinimumGlobalAddress() {
		return 0;
	}
	public int getMaximumGlobalAddress() {
		return this.globalVariableIdentifierList.size() - 1;
	}
	public int getMinimumConstantAddress() {
		return 0;
	}
	public int getMaximumConstantAddress() {
		return this.constantDataValueList.size() - 1;
	}
	public int getMinimumFunctionAddress() {
		return 0;
	}
	public int getMaximumFunctionAddress() {
		return this.functionIdentifierList.size() - 1;
	}

	public void dump() {
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
			builder.append("\t" + i + "\t" + this.functionIdentifierList.get(i) + eol);
		}
		builder.append(eol);
		builder.append("#GLOBAL" + eol);
		for(int i=0; i<this.globalVariableIdentifierList.size(); i++) {
			builder.append("\t" + Memory.Partition.GLOBAL.toString().charAt(0) + i + "\t" + this.globalVariableIdentifierList.get(i) + eol);
		}
		builder.append(eol);
		builder.append("#LOCAL" + eol);
		for(int i=0; i<this.localVariableIdentifierList.size(); i++) {
			builder.append("\t" + Memory.Partition.LOCAL.toString().charAt(0) + i + "\t" + this.localVariableIdentifierList.get(i) + eol);
		}
		builder.append(eol);
		builder.append("#CONSTANT" + eol);
		for(int i=0; i<this.constantDataValueList.size(); i++) {
			builder.append("\t" + Memory.Partition.CONSTANT.toString().charAt(0) + i + "\t" + this.constantDataValueList.get(i) + eol);
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
		System.out.println(builder.toString());
	}


	public void setEngine(ScriptEngine engine) {
		this.engine = (VnanoEngine)engine;
	}

	@Override
	public ScriptEngine getEngine() {
		return (ScriptEngine)this.engine;
	}

	@Override
	public Object eval() {
		return null;
	}

	@Override
	public Object eval(Bindings scriptBindings) {
		return null;
	}

	@Override
	public Object eval(ScriptContext scriptContext) {
		return null;
	}
}
