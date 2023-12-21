/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.IdentifierSyntax;
import org.vcssl.nano.spec.ScriptWord;

/**
 * The class acting as a variable table.
 * Where "variable table" is a table mapping each variable's name to information of the corresponding variable.
 */
public class VariableTable implements Cloneable {

	/** The List storing all variables. */
	LinkedList<AbstractVariable> variableList = null;

	/**
	 * The Map mapping each variable name to the corresponding variable.
	 * Multiple variables may have the same name, so this map stores a List as a value.
	 */
	Map<String, LinkedList<AbstractVariable>> nameVariableMap = null;

	/**
	 * The Map mapping each variable fully qualified name to the corresponding variable.
	 * Multiple variables may have the same fully qualified name, so this map stores a List as a value.
	 */
	Map<String, LinkedList<AbstractVariable>> fullNameVariableMap = null;

	/**
	 * The Map mapping each variable's identifier in assembly code to the corresponding variable.
	 * Multiple variables may have the same assembly identifier, so this map stores a List as a value.
	 */
	Map<String, LinkedList<AbstractVariable>> assemblyIdentifierVariableMap = null;

	/**
	 * The Map mapping each variable's fully qualified identifier in assembly code to the corresponding variable.
	 * Multiple variables may have the same assembly identifier, so this map stores a List as a value.
	 */
	Map<String, LinkedList<AbstractVariable>> fullAssemblyIdentifierVariableMap = null;

	/**
	 * The Map mapping each variable's index in this variable table to the corresponding variable.
	 * The "variableList" field is a LinkedList, so getting its element by an index takes a cost.
	 * This map is used for improving costs of such operations.
	 */
	Map<Integer, AbstractVariable> indexVariableMap = null;

	/**
	 * The Map mapping each variable's identifier in assembly code to the variable's index in this table.
	 * Multiple variables may have the same assembly identifier, so this map stores a List as a value.
	 */
	Map<String, LinkedList<Integer>> assemblyIdentifierIndexMap = null;

	/**
	 * The Map mapping each variable's fully qualified identifier in assembly code to the variable's index in this table.
	 * Multiple variables may have the same assembly identifier, so this map stores a List as a value.
	 */
	Map<String, LinkedList<Integer>> fullAssemblyIdentifierIndexMap = null;

	/** The total count of currently registered variables. */
	int size;


	/**
	 * Creates an empty variable table.
	 */
	public VariableTable() {
		this.variableList = new LinkedList<AbstractVariable>();
		this.nameVariableMap = new LinkedHashMap<String, LinkedList<AbstractVariable>>();
		this.fullNameVariableMap = new LinkedHashMap<String, LinkedList<AbstractVariable>>();
		this.assemblyIdentifierVariableMap = new LinkedHashMap<String, LinkedList<AbstractVariable>>();
		this.fullAssemblyIdentifierVariableMap = new LinkedHashMap<String, LinkedList<AbstractVariable>>();

		this.indexVariableMap = new LinkedHashMap<Integer, AbstractVariable>();
		this.assemblyIdentifierIndexMap = new LinkedHashMap<String, LinkedList<Integer>>();
		this.fullAssemblyIdentifierIndexMap = new LinkedHashMap<String, LinkedList<Integer>>();

		this.size = 0;
	}


	/**
	 * Add (regisiter) a new variable to this table.
	 *
	 * @param variable The variable to be added.
	 */
	public void addVariable(AbstractVariable variable) {
		int variableIndex = this.size;
		this.size++;

		// Get the fully qualified name, and the identifier in assembly code, from the (simple) name of the variable.
		String namespacePrefix = variable.hasNamespaceName() ? variable.getNamespaceName() + ScriptWord.NAMESPACE_SEPARATOR : "";
		String varName = variable.getVariableName();
		String asmName = IdentifierSyntax.getAssemblyIdentifierOf(variable);
		String fullAsmName = IdentifierSyntax.getAssemblyIdentifierOf(variable, namespacePrefix);

		// Add the variable to lists and maps.
		this.variableList.add(variable);
		IdentifierMapManager.putToMap(this.nameVariableMap, varName, variable); // 重複キーに対応するためのマップ操作メソッド
		IdentifierMapManager.putToMap(this.fullNameVariableMap, namespacePrefix + varName, variable);
		IdentifierMapManager.putToMap(this.assemblyIdentifierVariableMap, asmName, variable);
		IdentifierMapManager.putToMap(this.fullAssemblyIdentifierVariableMap, fullAsmName, variable);

		// Register the variable's index to some maps as a key, for reducing costs when we want to get the variable from the index.
		// (LinkedList requires the cost of the order O(N) for such operation, but Map can do it with the cost of the order O(1).)
		this.indexVariableMap.put(variableIndex, variable);
		IdentifierMapManager.putToMap(this.assemblyIdentifierIndexMap, asmName, variableIndex);
		IdentifierMapManager.putToMap(this.fullAssemblyIdentifierIndexMap, fullAsmName, variableIndex);
	}


	/**
	 * Remove the variable having the specified (simple) name.
	 *
	 * If multiple variables having the same name exist in the table, the last added one will be removed.
	 *
	 * @param variableName The (simple) name of the variable to be removed.
	 */
	public void removeLastVariable() {
		this.size--;
		int variableIndex = this.size;

		// Get the fully qualified name, and the identifier in assembly code, from the (simple) name of the variable.
		AbstractVariable variable = this.variableList.getLast();
		String namespacePrefix = variable.hasNamespaceName() ? variable.getNamespaceName() + ScriptWord.NAMESPACE_SEPARATOR : "";
		String varName = variable.getVariableName();
		String asmName = IdentifierSyntax.getAssemblyIdentifierOf(variable);
		String fullAsmName = IdentifierSyntax.getAssemblyIdentifierOf(variable, namespacePrefix);

		// Remove the variable from lists and maps.
		this.variableList.removeLast();
		IdentifierMapManager.removeLastFromMap(this.nameVariableMap, varName); // 重複キーに対応するためのマップ操作メソッド
		IdentifierMapManager.removeLastFromMap(this.fullNameVariableMap, namespacePrefix + varName);
		IdentifierMapManager.removeLastFromMap(this.assemblyIdentifierVariableMap, asmName);
		IdentifierMapManager.removeLastFromMap(this.fullAssemblyIdentifierVariableMap, fullAsmName);
		this.indexVariableMap.remove(variableIndex, variable);
		IdentifierMapManager.removeLastFromMap(this.assemblyIdentifierIndexMap, asmName);
		IdentifierMapManager.removeLastFromMap(this.fullAssemblyIdentifierIndexMap, fullAsmName);
	}


	/**
	 * Gets all variables registered to this table.
	 *
	 * @return An array sotring all variables registered to this table.
	 */
	public AbstractVariable[] getVariables() {

		// Add all variables to the following list, and convert it to an array.
		List<AbstractVariable> variableList = new ArrayList<AbstractVariable>();

		Set<Entry<String, LinkedList<AbstractVariable>>> entrySet = this.nameVariableMap.entrySet();
		for (Entry<String, LinkedList<AbstractVariable>> entry: entrySet) {
			variableList.addAll(entry.getValue());
		}
		return variableList.toArray(new AbstractVariable[0]);
	}


	/**
	 * Returns whether the variable having the specified name is registered to this table.
	 *
	 * @param name The name of the variable to be checked.
	 * @return Returns true if the specified variable is registered to this table.
	 */
	public boolean containsVariableWithName(String name) {
		return this.nameVariableMap.containsKey(name) || this.fullNameVariableMap.containsKey(name);
	}


	/**
	 * Gets the variable having the specified name.
	 * If multiple variables in this table have the specified name, returns the last added one.
	 *
	 * @param name The name of the variable to be gotten.
	 * @return The variable having the specified name.
	 */
	public AbstractVariable getVariableByName(String name) {
		if (this.nameVariableMap.containsKey(name)) {
			return IdentifierMapManager.getLastFromMap(this.nameVariableMap, name);
		}
		if (this.fullNameVariableMap.containsKey(name)) {
			return IdentifierMapManager.getLastFromMap(this.fullNameVariableMap, name);
		}
		throw new VnanoFatalException("Variable not found: " + name);
	}


	/**
	 * Returns whether the variable having the specified identifier in assembly code is registered to this table.
	 *
	 * @param identifier The identifier (in assembly code) of the variable to be checked.
	 * @return Returns true if the specified variable is registered to this table.
	 */
	public boolean containsVariableWithAssemblyIdentifier(String identifier) {
		return this.assemblyIdentifierVariableMap.containsKey(identifier)
				|| this.fullAssemblyIdentifierVariableMap.containsKey(identifier);
	}


	/**
	 * Gets the variable having the specified identifier in assembly code.
	 * If multiple variables in this table have the specified identifier, returns the last added one.
	 *
	 * @param identifier The identifier (in assembly code) of the variable to be gotten.
	 * @return The variable having the specified identifier.
	 */
	public AbstractVariable getVariableByAssemblyIdentifier(String identifier) {
		if (assemblyIdentifierVariableMap.containsKey(identifier)) {
			return IdentifierMapManager.getLastFromMap(this.assemblyIdentifierVariableMap, identifier);
		}
		if (fullAssemblyIdentifierVariableMap.containsKey(identifier)) {
			return IdentifierMapManager.getLastFromMap(this.fullAssemblyIdentifierVariableMap, identifier);
		}
		throw new VnanoFatalException("Variable not found: " + identifier);
	}


	/**
	 * Gets the variable having the specified index in this table.
	 *
	 * In this table, each variable has an unique index.
	 *
	 * @param index The index of the variable to be gotten.
	 * @return The variable having the specified index.
	 */
	public AbstractVariable getVariableByIndex(int index) {
		return this.indexVariableMap.get(index);
	}


	/**
	 * Gets the index of the specified variable.
	 *
	 * In this table, each variable has an unique index.
	 *
	 * @param variable The variable.
	 * @return The index of the specified variable.
	 */
	public int getIndexOf(AbstractVariable variable) {

		// Get the assembly idenfitier of the variable.
		String namespacePrefix = variable.hasNamespaceName() ? variable.getNamespaceName() + ScriptWord.NAMESPACE_SEPARATOR : "";
		String asmName = IdentifierSyntax.getAssemblyIdentifierOf(variable);
		String fullAsmName = IdentifierSyntax.getAssemblyIdentifierOf(variable, namespacePrefix);

		// Get/return index of the variable,
		// by using map(s) mapping each assembly identifier to the corresponding variable's index.
		if (assemblyIdentifierIndexMap.containsKey(asmName)) {
			return IdentifierMapManager.getLastFromMap(this.assemblyIdentifierIndexMap, asmName);
		}
		if (fullAssemblyIdentifierIndexMap.containsKey(fullAsmName)) {
			return IdentifierMapManager.getLastFromMap(this.fullAssemblyIdentifierIndexMap, fullAsmName);
		}

		throw new VnanoFatalException("Variable index not found: " + fullAsmName);
	}


	/**
	 * Gets the total count of currently registered variables in this table.
	 *
	 * @return The total count of currently registered variables in this table.
	 */
	public int getSize() {
		return this.size;
	}
}
