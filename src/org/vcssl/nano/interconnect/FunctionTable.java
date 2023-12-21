/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.compiler.AstNode;
import org.vcssl.nano.compiler.AttributeKey;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.IdentifierSyntax;
import org.vcssl.nano.spec.ScriptWord;

/**
 * The class acting as a function table.
 * Where "function table" is a table mapping each function's name to information of the corresponding function.
 */
public class FunctionTable {

	/** The List storing all functions.  */
	List<AbstractFunction> functionList = null;

	/**
	 * The Map mapping each function's signature to the corresponding function.
	 * Multiple functions may have the same signature, so this map stores a List as a value.
	 */
	Map<String, LinkedList<AbstractFunction>> signatureFunctionMap = null;

	/**
	 * The Map mapping each function name to the corresponding function.
	 * Multiple functions may have the same name, so this map stores a List as a value.
	 */
	Map<String, LinkedList<AbstractFunction>> nameFunctionMap = null;

	/**
	 * The Map mapping each function's fully qualified signature to the corresponding function.
	 * Multiple functions may have the same signature, so this map stores a List as a value.
	 */
	Map<String, LinkedList<AbstractFunction>> fullSignatureFunctionMap = null;

	/**
	 * The Map mapping each function's fully qualified name to the corresponding function.
	 * Multiple functions may have the same name, so this map stores a List as a value.
	 */
	Map<String, LinkedList<AbstractFunction>> fullNameFunctionMap = null;

	/**
	 * The Map mapping each function's index in this function table to the corresponding function.
	 * The "functionList" field is a LinkedList, so getting its element by an index takes a cost.
	 * This map is used for improving costs of such operations.
	 */
	Map<Integer, AbstractFunction> indexFunctionMap = null;

	/**
	 * The Map mapping each variable's signature to the function's index in this table.
	 * Multiple variables may have the same signature, so this map stores a List as a value.
	 */
	Map<String, LinkedList<Integer>> signatureIndexMap = null;

	/**
	 * The Map mapping each variable's fully qualified signature to the function's index in this table.
	 * Multiple variables may have the same signature, so this map stores a List as a value.
	 */
	Map<String, LinkedList<Integer>> fullSignatureIndexMap = null;

	/** The total count of currently registered function. */
	int size;


	/**
	 * Creates an empty function table.
	 */
	public FunctionTable() {
		this.functionList = new ArrayList<AbstractFunction>();

		this.signatureFunctionMap = new LinkedHashMap<String, LinkedList<AbstractFunction>>();
		this.nameFunctionMap = new LinkedHashMap<String, LinkedList<AbstractFunction>>();

		this.fullSignatureFunctionMap = new LinkedHashMap<String, LinkedList<AbstractFunction>>();
		this.fullNameFunctionMap = new LinkedHashMap<String, LinkedList<AbstractFunction>>();

		this.indexFunctionMap = new LinkedHashMap<Integer, AbstractFunction>();
		this.signatureIndexMap = new LinkedHashMap<String, LinkedList<Integer>>();
		this.fullSignatureIndexMap = new LinkedHashMap<String, LinkedList<Integer>>();

		this.size = 0;
	}


	/**
	 * Add (regisiter) a new function to this table.
	 *
	 * @param function The function to be added.
	 */
	public void addFunction(AbstractFunction function) {
		int functionIndex = this.size;
		this.size++;

		// Get the fully qualified name/signature, from the (simple) signature/name of the function.
		String namespacePrefix = function.hasNamespaceName() ? function.getNamespaceName() +  ScriptWord.NAMESPACE_SEPARATOR : "";
		String functionName = function.getFunctionName();
		String fullFunctionName = namespacePrefix + functionName;
		String signature = IdentifierSyntax.getSignatureOf(function);
		String fullSignature = IdentifierSyntax.getSignatureOf(function, namespacePrefix);

		// Add the function to lists and maps.
		this.functionList.add(function);
		IdentifierMapManager.putToMap(this.signatureFunctionMap, signature, function);
		IdentifierMapManager.putToMap(this.nameFunctionMap, functionName, function);
		IdentifierMapManager.putToMap(this.fullSignatureFunctionMap, fullSignature, function);
		IdentifierMapManager.putToMap(this.fullNameFunctionMap, fullFunctionName, function);

		// Register the function's index to some maps as a key, for reducing costs when we want to get the function from the index.
		// (LinkedList requires the cost of the order O(N) for such operation, but Map can do it with the cost of the order O(1).)
		this.indexFunctionMap.put(functionIndex, function);
		IdentifierMapManager.putToMap(this.signatureIndexMap, signature, functionIndex);
		IdentifierMapManager.putToMap(this.fullSignatureIndexMap, fullSignature, functionIndex);
	}


	/**
	 * Gets all functions registered to this table.
	 *
	 * @return An array sotring all functions registered to this table.
	 */
	public AbstractFunction[] getFunctions() {
		return this.functionList.toArray(new AbstractFunction[0]);
	}


	/**
	 * Gets the function having the specified index in this table.
	 *
	 * In this table, each function has an unique index.
	 *
	 * @param index The index of the function to be gotten.
	 * @return The function having the specified index.
	 */
	public AbstractFunction getFunctionByIndex(int index) {
		return this.functionList.get(index);
	}


	/**
	 * Gets the index of the specified function.
	 *
	 * In this table, each function has an unique index.
	 *
	 * @param function The function.
	 * @return The index of the specified function.
	 */
	public int getIndexOf(AbstractFunction function) {

		// Get the fully qualified signature of the function.
		String namespacePrefix = function.hasNamespaceName() ? function.getNamespaceName() +  ScriptWord.NAMESPACE_SEPARATOR : "";
		String signature = IdentifierSyntax.getSignatureOf(function);
		String fullSignature = IdentifierSyntax.getSignatureOf(function, namespacePrefix);

		// Get/return index of the function,
		// by using map(s) mapping each signature to the corresponding function's index.
		if (signatureIndexMap.containsKey(signature)) {
			return IdentifierMapManager.getLastFromMap(this.signatureIndexMap, signature);
		}
		if (fullSignatureIndexMap.containsKey(fullSignature)) {
			return IdentifierMapManager.getLastFromMap(this.fullSignatureIndexMap, fullSignature);
		}
		throw new VnanoFatalException("Function index not found: " + fullSignature);
	}


	/**
	 * Gets the function having the specified information composing the signature.
	 *
	 * @param functionName The name of the function to be gotten.
	 * @param parameterDataTypes Data-types of all parameters.
	 * @param parameterArrayRanks Array-ranks of all parameters (The rank of a scalar is 0).
	 * @oaram parameterDataTypeArbitrarinesses Flags representing whether data-types of parameters vary depending on actual arguments.
	 * @oaram parameterArrayRankArbitrarinesses Flags representing whether array-ranks of parameters vary depending on actual arguments.
	 * @param parameterCountArbitrary The flags representing whether the number of parameters vary depending on the number of actual arguments.
	 * @param parameterVariadic The flags representing whether the function has variadic parameters.
	 * @return The function having the specified signature.
	 */
	public AbstractFunction getFunctionBySignature(
			String functionName, DataType[] parameterDataTypes, int[] parameterArrayRanks,
			boolean[] parameterDataTypeArbitrarinesses, boolean[] parameterArrayRankArbitrarinesses,
			boolean parameterCountArbitrary, boolean parameterVariadic) {

		String[] parameterDataTypeNames = DataTypeName.getDataTypeNamesOf(parameterDataTypes);
		String signature = IdentifierSyntax.getSignatureOf(
				functionName, parameterDataTypeNames, parameterArrayRanks,
				parameterDataTypeArbitrarinesses, parameterArrayRankArbitrarinesses,
				parameterCountArbitrary, parameterVariadic

		);
		return this.getFunctionBySignature(signature);
	}


	/**
	 * Gets the function having the specified signature.
	 *
	 * @param signature The signature of the function to be gotten.
	 * @return The function having the specified signature.
	 */
	public AbstractFunction getFunctionBySignature(String signature) {
		if (this.signatureFunctionMap.containsKey(signature)) {
			return IdentifierMapManager.getLastFromMap(this.signatureFunctionMap, signature);
		}
		if (this.fullSignatureFunctionMap.containsKey(signature)) {
			return IdentifierMapManager.getLastFromMap(this.fullSignatureFunctionMap, signature);
		}
		throw new VnanoFatalException("Function not found: " + signature);
	}


	/**
	 * Returns whether the function having the specified signature is registered to this table.
	 *
	 * @param signature The signature of the variable to be checked.
	 * @return Returns true if the specified function is registered to this table.
	 */
	public boolean hasFunctionWithSignature(String signature) {
		return this.signatureFunctionMap.containsKey(signature)
				|| this.fullSignatureFunctionMap.containsKey(signature);
	}


	/**
	 * Returns whether the callee function of the specified function-call operator is registered to this table.
	 *
	 * @param callerNode The AST node of the function-call operator.
	 * @return Returns true if the callee function of the specified function-call operator is registered to this table.
	 */
	public boolean hasCalleeFunctionOf(AstNode callerNode) {
		return this.getCalleeFunctionOf(callerNode) != null;
	}


	/**
	 * Gets the callee function of the specified function-call operator.
	 *
	 * @param callerNode The AST node of the function-call operator.
	 * @return The callee function of the specified function-call operator.
	 */
	public AbstractFunction getCalleeFunctionOf(AstNode callerNode) {

		// Firstly, create the call-signature of the callee function by actual arguments,
		// and search the function by the signature.
		// This is the fastest way to search the function, if the completely same signature is registered.
		String signature = IdentifierSyntax.getSignatureOfCalleeFunctionOf(callerNode);
		if (this.hasFunctionWithSignature(signature)) {
			return this.getFunctionBySignature(signature);
		}

		// Note that, a signature of a function having arbitrary data-type/array-rank parameters
		// may not match with the call-signature created by actual arguments,
		// even when the function is callable by the actual arguments.

		// So, if the signature does not match with registered signatures,
		// extract all functions having the same name as the specified function, from this table.
		String functionName = callerNode.getChildNodes()[0].getAttribute(AttributeKey.IDENTIFIER_VALUE);
		List<AbstractFunction> functionList = null;
		if (this.nameFunctionMap.containsKey(functionName)) {
			functionList = IdentifierMapManager.getAllFromMap(this.nameFunctionMap, functionName);
		} else if (this.fullNameFunctionMap.containsKey(functionName)) {
			functionList = IdentifierMapManager.getAllFromMap(this.fullNameFunctionMap, functionName);
		}

		// If there is no function having the same name as the specified function:
		if (functionList == null) {
			return null;
		}

		// If functions have the same name as the specified function exist,
		// determine whether each function can be called by actual arguments of the specified function-call operator.
		AstNode[] childNodes = callerNode.getChildNodes();
		int argumentLength = childNodes.length - 1;
		int[] argumentRanks = new int[argumentLength];
		String[] argumentDataTypeNames = new String[argumentLength];
		for (int argumentIndex=0; argumentIndex<argumentLength; argumentIndex++) {
			argumentRanks[argumentIndex] = childNodes[argumentIndex+1].getArrayRank();
			argumentDataTypeNames[argumentIndex] = childNodes[argumentIndex+1].getDataTypeName();
		}

		int functionN = functionList.size();
		for (int functionIndex=functionN-1; 0<=functionIndex; functionIndex--) {

			AbstractFunction function = functionList.get(functionIndex);

			// Get information of all parameters.
			int[] parameterRanks = function.getParameterArrayRanks();
			String[] parameterDataTypeNames = function.getParameterDataTypeNames();
			boolean[] parameterDataTypeArbitrarinesses = function.getParameterDataTypeArbitrarinesses();
			boolean[] parameterArrayRankArbitrarinesses = function.getParameterArrayRankArbitrarinesses();
			int parameterLength = parameterRanks.length;

			// If the number of parameters is arbitrary,
			// expand the number of information of parameters, depending on the number of actual arguments.
			if (function.isParameterCountArbitrary()) {
				int unexpandedParameterLength = parameterLength;
				parameterLength = argumentLength;
				parameterRanks = Arrays.copyOf(parameterRanks, parameterLength);
				parameterDataTypeNames = Arrays.copyOf(parameterDataTypeNames, parameterLength);
				parameterDataTypeArbitrarinesses = Arrays.copyOf(parameterDataTypeArbitrarinesses, parameterLength);
				parameterArrayRankArbitrarinesses = Arrays.copyOf(parameterArrayRankArbitrarinesses, parameterLength);

				// Note that the caller can omit to pass the actual argument corresponding to the last parameter,
				// when isParameterCountArbitrary() returns true.
				if (unexpandedParameterLength - 1 < parameterLength) {
					Arrays.fill(
						parameterRanks, // the destination array
						unexpandedParameterLength - 1, parameterLength, // index of the dest's head, index of the dest's tail + 1
						parameterRanks[unexpandedParameterLength - 1] // the element to be copied
					);
					Arrays.fill(
						parameterDataTypeNames,
						unexpandedParameterLength - 1, parameterLength,
						parameterDataTypeNames[unexpandedParameterLength - 1]
					);
					Arrays.fill(
						parameterDataTypeArbitrarinesses,
						unexpandedParameterLength - 1, parameterLength,
						parameterDataTypeArbitrarinesses[unexpandedParameterLength - 1]
					);
					Arrays.fill(
						parameterArrayRankArbitrarinesses,
						unexpandedParameterLength - 1, parameterLength,
						parameterArrayRankArbitrarinesses[unexpandedParameterLength - 1]
					);
				}
			}

			// If the number of parameters is different with the number of actual arguments, the function is not callable.
			if (parameterLength != argumentLength) {
				continue;
			}

			// Check compatibility of data-types between each parameter and actual argument.
			boolean isCallable = true; // Set false if any parameter/argument is incompatible.
			for (int parameterIndex=0; parameterIndex<parameterLength; parameterIndex++) {

				String paramTypeName = parameterDataTypeNames[parameterIndex];
				String argTypeName = argumentDataTypeNames[parameterIndex];
				int paramRank = parameterRanks[parameterIndex];
				int argRank = argumentRanks[parameterIndex];

				boolean isParamAnyType = parameterDataTypeArbitrarinesses[parameterIndex];
				boolean isParamAnyRank = parameterArrayRankArbitrarinesses[parameterIndex];
				boolean isDataTypeSame = paramTypeName.equals(argTypeName);
				boolean isRankSame = (paramRank == argRank);

				// Check the compatibility between i-th parameter/argument.
				// If they are compatible, go to the next parameter/argument by a "continue" statement.
				// If they aren't compatible, determine that this function is not callable, and "break" from this loop.

				// If both the data-type and the array-rank of the parameter/argument are completely the same: compatible.
				if (isDataTypeSame && isRankSame) {
					continue;
				}

				// If both the data-type and the array-rank of parameters are arbitrary: compatible.
				if (isParamAnyType && isParamAnyRank) {
					continue;
				}

				// If the parameter's data-type is arbitrary, but the array-rank is not arbitrary:
				// compatible if the parameter's array-rank and the argument's array-rank are the same.
				if (isParamAnyType && !isParamAnyRank && isRankSame) {
					continue;
				}

				// If the parameter's array-rank is arbitrary, but the data-type is not arbitrary:
				// compatible if the parameter's data-type and the argument's data-type are the same.
				if (isParamAnyRank && !isParamAnyType && isDataTypeSame) {
					continue;
				}

				// Otherwise, the parameter/argument is not compatible, so this function is not callable.
				isCallable = false;
				break;
			}

			if (isCallable) {
				return function;
			}
		}

		// This method returns null when there is no callable function.
		// You can check whether the callable function exists by "hasCalleeFunctionOf" method explicitly.
		return null;
	}


	/**
	 * Presumes the signatures of the callee functions, called by the specified function-call operator,
	 * which will be displayed in error messages, when no matched function has been found.
	 *
	 * @param callerNode The AST node of the function-call operator.
	 * @return The signatures of the presumed callee functions.
	 */
	public String[] presumeCalleeFunctionSignaturesOf(AstNode callerNode) {
		String calleeFunctionName = callerNode.getChildNodes()[0].getAttribute(AttributeKey.IDENTIFIER_VALUE);
		List<String> presumedFunctionSignatureList = new ArrayList<String>();

		LinkedList<AbstractFunction> sameNameFunctionList = this.nameFunctionMap.get(calleeFunctionName);
		if (sameNameFunctionList == null) {
			return new String[0];
		}
		for (AbstractFunction function: sameNameFunctionList) {
			//String namespacePrefix = function.hasNamespaceName() ? function.getNamespaceName() +  ScriptWord.NAMESPACE_SEPARATOR : "";
			//String fullSignature = IdentifierSyntax.getSignatureOf(function, namespacePrefix);
			String signature = IdentifierSyntax.getSignatureOf(function);
			presumedFunctionSignatureList.add(signature);
		}
		String[] result = new String[sameNameFunctionList.size()];
		result = presumedFunctionSignatureList.toArray(result);
		return result;
	}
}
