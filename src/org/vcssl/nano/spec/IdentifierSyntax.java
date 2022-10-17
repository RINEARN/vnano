/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import java.util.Arrays;
import java.util.HashSet;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.compiler.AstNode;
import org.vcssl.nano.compiler.AttributeKey;
import org.vcssl.nano.interconnect.AbstractFunction;
import org.vcssl.nano.interconnect.AbstractVariable;

/**
 * The class to generate syntactically valid identifiers and signatures.
 */
public final class IdentifierSyntax {

	/**
	 * Judges whether the specified identifier is syntactically valid.
	 * 
	 * @param identifier The identifier to be judged.
	 * @return Returns true if the specified identifier is syntactically valid.
	 */
	public static final boolean isValidSyntaxIdentifier(String identifier) {

		// If the identifier begins with numbers: NG.
		if (identifier.matches("^[0-9].*$")) {
			return false;
		}

		// If the identifier begins with some special symbols: NG.
		// (Note: Symbols of operators will not be contained in an identifier token, as long as LexicalAnalyzer works expectedly.)
		char[] invalidChars = { '#', '$', '%', '&', '~', '@', ':', '`', '^', '.', '?', '\"', '\'', '\\' };
		HashSet<Character> invalidCharSet = new HashSet<Character>();
		for (char invalidChar: invalidChars) {
			invalidCharSet.add( Character.valueOf(invalidChar) );
		}
		for (char identifierChar: identifier.toCharArray()) {
			if ( invalidCharSet.contains(Character.valueOf(identifierChar)) ) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Returns the signature of the function having the specified information.
	 * 
	 * @param functionName The name of the function.
	 * @param parameterDataTypeNames The names of the data-types of the parameters.
	 * @param parameterArrayRanks The array-ranks of the parameters.
	 * @param parameterDataTypeArbitrarinesses The flags representing whether the data-types of the parameters are arbitrary.
	 * @param parameterArrayRankArbitrarinesses The flags representing whether the array-rank of the parameters are arbitrary.
	 * @param parameterCountArbitrary The flag representing whether the total number of the parameters is arbitrary.
	 * @param parameterVariadic The flag representing whether the function has variadic parameters.
	 * @return The signature of the function.
	 */
	public static final String getSignatureOf(String functionName,
			String[] parameterDataTypeNames, int[] parameterArrayRanks,
			boolean[] parameterDataTypeArbitrarinesses, boolean[] parameterArrayRankArbitrarinesses,
			boolean parameterCountArbitrary, boolean parameterVariadic) {

		StringBuilder builder = new StringBuilder();

		builder.append(functionName);
		builder.append("(");

		if (parameterCountArbitrary) {
			builder.append("...");
			if (parameterDataTypeArbitrarinesses[0]) {
				builder.append(DataTypeName.ANY);
			} else {
				builder.append(parameterDataTypeNames[0]);
			}
			if (parameterArrayRankArbitrarinesses[0]) {
				builder.append("[...])");
			} else {
				builder.append("[])");
			}
			return builder.toString();
		}

		int parameterLength = parameterDataTypeNames.length;
		for (int parameterIndex=0; parameterIndex<parameterLength; parameterIndex++) {

			if (parameterDataTypeArbitrarinesses[parameterIndex]) {
				builder.append(DataTypeName.ANY);
			} else {
				builder.append(parameterDataTypeNames[parameterIndex]);
			}

			if (parameterArrayRankArbitrarinesses[parameterIndex]) {
				builder.append("[...]");
			} else {
				int paramRank = parameterArrayRanks[parameterIndex];
				for (int dim=0; dim<paramRank; dim++) {
					builder.append("[]");
				}
			}

			if (parameterIndex != parameterLength-1) {
				builder.append(",");
			}
		}

		builder.append(")");
		return builder.toString();
	}


	/**
	 * Returns the signature of the function declared by the specified AST.
	 * 
	 * @param functionDeclarationNode The AST node of the function declaration.
	 * @return The signature of the function.
	 */
	public static final String getSignatureOf(AstNode functionDeclarationNode) {

		String functionName = functionDeclarationNode.getAttribute(AttributeKey.IDENTIFIER_VALUE);
		AstNode[] parameterNodes = functionDeclarationNode.getChildNodes();
		int parameterNodeLength = parameterNodes.length;

		//DataType[] argumentDataTypes = new DataType[argumentNodeLength];
		String[] parameterDataTypeNames = new String[parameterNodeLength];
		int[] parameterArrayRanks = new int[parameterNodeLength];
		boolean[] parameterDataTypeArbitrarinesses = new boolean[parameterNodeLength];
		boolean[] parameterArrayRankArbitrarinesses = new boolean[parameterNodeLength];
		Arrays.fill(parameterDataTypeArbitrarinesses, false);
		Arrays.fill(parameterArrayRankArbitrarinesses, false);

		for (int parameterNodeIndex=0; parameterNodeIndex<parameterNodeLength; parameterNodeIndex++) {
			String dataTypeName = parameterNodes[parameterNodeIndex].getAttribute(AttributeKey.DATA_TYPE);

			// Replace aliases of the data-type to the canonical name, 
			// by converting the name to DataType enum and re-converting it to the name again.
			try {
				DataType dataType = DataTypeName.getDataTypeOf(dataTypeName);
				dataTypeName = DataTypeName.getDataTypeNameOf(dataType);
			} catch (VnanoException e) {
				// Do nothing for data-type names which are undefined in DataType enum.
			}
			parameterDataTypeNames[parameterNodeIndex] = dataTypeName;
			parameterArrayRanks[parameterNodeIndex] = parameterNodes[parameterNodeIndex].getRank();
		}

		String signature = getSignatureOf(
				functionName, parameterDataTypeNames, parameterArrayRanks,
				parameterDataTypeArbitrarinesses, parameterArrayRankArbitrarinesses,
				false, false
		);

		return signature;
	}


	/**
	 * Returns the signature of the function which is called from the function-call operator of the specified AST node.
	 * 
	 * @param callerNode The AST node of the function-call operator.
	 * @return The signature of the callee function of the specified function-call operator.
	 */
	public static final String getSignatureOfCalleeFunctionOf(AstNode callerNode) {

		AstNode[] childNodes = callerNode.getChildNodes();

		// The function name is stored in the first child node (= function identifier node).
		String functionName = childNodes[0].getAttribute(AttributeKey.IDENTIFIER_VALUE);

		// Store all argument's nodes into an array.
		int argumentNodeLength = childNodes.length-1;
		AstNode[] argumentNodes = new AstNode[argumentNodeLength];
		System.arraycopy(childNodes, 1, argumentNodes, 0, argumentNodeLength);

		// Stores all argument's data-type names, array-ranks, and flags into arrays.
		String[] argumentDataTypeNames = new String[argumentNodeLength];
		int[] argumentArrayRanks = new int[argumentNodeLength];
		boolean[] argumentDataTypeArbitrarinesses = new boolean[argumentNodeLength];
		boolean[] argumentArrayRankArbitrarinesses = new boolean[argumentNodeLength];
		Arrays.fill(argumentDataTypeArbitrarinesses, false);
		Arrays.fill(argumentArrayRankArbitrarinesses, false);

		for (int argumentNodeIndex=0; argumentNodeIndex<argumentNodeLength; argumentNodeIndex++) {
			String dataTypeName = argumentNodes[argumentNodeIndex].getAttribute(AttributeKey.DATA_TYPE);

			// Replace aliases of the data-type to the canonical name, 
			// by converting the name to DataType enum and re-converting it to the name again.
			try {
				DataType dataType = DataTypeName.getDataTypeOf(dataTypeName);
				dataTypeName = DataTypeName.getDataTypeNameOf(dataType);
			} catch (VnanoException e) {
				// Do nothing for data-type names which are undefined in DataType enum.
			}
			argumentDataTypeNames[argumentNodeIndex] = dataTypeName;
			argumentArrayRanks[argumentNodeIndex] = argumentNodes[argumentNodeIndex].getRank();
		}

		String signature = getSignatureOf(
				functionName, argumentDataTypeNames, argumentArrayRanks,
				argumentDataTypeArbitrarinesses, argumentArrayRankArbitrarinesses,
				false, false
		);

		return signature;
	}


	/**
	 * Returns the signature of the specified function.
	 * 
	 * @param function The function.
	 * @return The signature of the specified function.
	 */
	public static final String getSignatureOf(AbstractFunction function) {
		return getSignatureOf(function, "");
	}
	public static final String getSignatureOf(AbstractFunction function, String nameSpacePrefix) {
		String[] parameterDataTypeNames = function.getParameterDataTypeNames();
		int[] parameterArrayRanks = function.getParameterArrayRanks();
		boolean[] parameterDataTypeArbitrarinesses = function.getParameterDataTypeArbitrarinesses();
		boolean[] parameterArrayRankArbitrarinesses = function.getParameterArrayRankArbitrarinesses();
		String functionName = nameSpacePrefix + function.getFunctionName();
		String signature = getSignatureOf(
				functionName, parameterDataTypeNames, parameterArrayRanks,
				parameterDataTypeArbitrarinesses, parameterArrayRankArbitrarinesses,
				function.isParameterCountArbitrary(), function.hasVariadicParameters()
		);

		return signature;
	}



	// Should remove?
	/**
	 * Returns the identifier in assembly code, corresponding with the specified variable name.
	 * 
	 * @param variableName The name of the variable.
	 * @return The identifier of the variable in assembly code.
	 */
	/*
	public static final String getAssemblyIdentifierOf(String variableName) {
		return AssemblyWord.IDENTIFIER_OPERAND_PREFIX + variableName;
	}
	*/


	/**
	 * Returns the identifier in assembly code, corresponding with the specified variable's AST node.
	 * 
	 * @param variableNode The AST node of the variable declaration.
	 * @return The identifier of the variable in assembly code.
	 */
	public static final String getAssemblyIdentifierOf(AstNode variableNode) {
		String variableName = variableNode.getAttribute(AttributeKey.IDENTIFIER_VALUE);
		String assemblyIdentifier = AssemblyWord.IDENTIFIER_OPERAND_PREFIX + variableName;
		if (variableNode.hasAttribute(AttributeKey.IDENTIFIER_SERIAL_NUMBER)) {
			String serialNumber = variableNode.getAttribute(AttributeKey.IDENTIFIER_SERIAL_NUMBER);
			assemblyIdentifier += AssemblyWord.IDENTIFIER_SERIAL_NUMBER_SEPARATOR + serialNumber;
		}
		return assemblyIdentifier;
	}


	/**
	 * Returns the identifier in assembly code of the specified variable.
	 * 
	 * @param variable The variable.
	 * @return The identifier in assembly code of the specified variable.
	 */
	public static final String getAssemblyIdentifierOf(AbstractVariable variable) {
		return getAssemblyIdentifierOf(variable, "");
	}

	/**
	 * Returns the identifier in assembly code of the specified variable.
	 * 
	 * @param variable The variable.
	 * @param nameSpacePrefix The prefix representing the namespace to which the variable belongs.
	 * @return The identifier in assembly code of the specified variable.
	 */
	public static final String getAssemblyIdentifierOf(AbstractVariable variable, String nameSpacePrefix) {
		String variableName = variable.getVariableName();
		if (variable.hasSerialNumber()) {
			variableName += AssemblyWord.IDENTIFIER_SERIAL_NUMBER_SEPARATOR + variable.getSerialNumber();
		}
		return AssemblyWord.IDENTIFIER_OPERAND_PREFIX + nameSpacePrefix + variableName;
	}

	/**
	 * Normalizes the name of the specified script.
	 * 
	 * File names of scripts (script names) may contain symbols 
	 * which have syntactic meaning in the virtual assembly language of the VM (VRIL).
	 * In assembly code, script names of scripts will be embedded as meta-information directives,
	 * so we must replace special symbols in script names to non-special symbols.
	 * This method performs the above, and we call it as "normalization of script names".
	 * 
	 * @param scriptPathOrName The file path or the name of the script, to be normalized.
	 * @return The normalized script name.
	 */
	public static final String normalizeScriptIdentifier(String scriptPathOrName) {

		String normalizedName = scriptPathOrName;

		// Note: In the current specification, we allow dots "." to be contained in script names.

		// Replace special symbols to the following symbol "_".
		String escapedWord = "_";

		// Replace double-quotations (").
		normalizedName = normalizedName.replaceAll("\"", escapedWord);

		// Replace "," and "=".
		// They have syntactic meanings in meta-information directives as: #META "key1=value1,key2=value2,..."
		normalizedName = normalizedName.replaceAll("=", escapedWord);
		normalizedName = normalizedName.replaceAll(",", escapedWord);

		// Replace ";". It has syntactic meanings to indicate the end of each instruction/directive.
		normalizedName = normalizedName.replaceAll(";", escapedWord);

		// Line-feeds don't cause syntactic problems, but they make assembly code hard to read, so replace them.
		normalizedName = normalizedName.replaceAll("\t", escapedWord);
		normalizedName = normalizedName.replaceAll("\r", escapedWord);
		normalizedName = normalizedName.replaceAll("\n", escapedWord);

		// Replace backslashes to slashes.
		normalizedName = normalizedName.replace("\\", "/");

		// Remove redundant "./" in the path.
		if (normalizedName.startsWith("./")) {
			normalizedName = normalizedName.substring(2);
		}

		// To avoid confusion, replace white-spaces.
		// 
		// Note that, to the script code passed as an argument of "eval" method directly,
		// the special script name "main script" (OptionValue.MAIN_SCRIPT_NAME_DEFAULT) will be assigned by default.
		// It contains a white space on purpose to avoid conflict of names with other (library) scripts.
		// So we must not replace a white space in MAIN_SCRIPT_NAME_DEFAULT.
		if (!normalizedName.equals(OptionValue.MAIN_SCRIPT_NAME_DEFAULT)) {
			normalizedName = normalizedName.replaceAll(" ", escapedWord);
		}

		return normalizedName;
	}
}
