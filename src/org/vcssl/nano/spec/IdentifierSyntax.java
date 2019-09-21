/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import java.util.Arrays;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.compiler.AstNode;
import org.vcssl.nano.compiler.AttributeKey;
import org.vcssl.nano.interconnect.AbstractFunction;
import org.vcssl.nano.interconnect.AbstractVariable;

// Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/spec/IdentifierSyntax.html
// ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/spec/IdentifierSyntax.html

// SignatureSyntax に変更する？

/**
 * <p>
 * <span>
 * <span class="lang-en">
 * The utilities class for handling identifiers and signatures
 * </span>
 * <span class="lang-ja">
 * 識別子やシグネチャの扱いを補助する操作を提供するクラスです
 * </span>
 * .
 * </p>
 *
 * <p>
 * &raquo; <a href="../../../../../src/org/vcssl/nano/spec/IdentifierSyntax.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/spec/IdentifierSyntax.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/spec/IdentifierSyntax.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class IdentifierSyntax {


	public static String getSignatureOf(String functionName,
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

	public static String getSignatureOf(AstNode functionDeclarationNode) {

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

			// データ型名のエイリアス（floatに対するdoubleなど）を一意な型名に揃えるため、一旦DataTypeに変換して戻す
			try {
				DataType dataType = DataTypeName.getDataTypeOf(dataTypeName);
				dataTypeName = DataTypeName.getDataTypeNameOf(dataType);
			} catch (VnanoException e) {
				// DataTypeに定義されない未知の型の場合は、記述された型名をそのまま使用する
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

	public static String getSignatureOfCalleeFunctionOf(AstNode callerNode) {

		AstNode[] childNodes = callerNode.getChildNodes();

		// 最初の子ノードが関数識別子ノード
		String functionName = childNodes[0].getAttribute(AttributeKey.IDENTIFIER_VALUE);

		int argumentNodeLength = childNodes.length-1;
		AstNode[] argumentNodes = new AstNode[argumentNodeLength];
		System.arraycopy(childNodes, 1, argumentNodes, 0, argumentNodeLength);

		String[] argumentDataTypeNames = new String[argumentNodeLength];
		int[] argumentArrayRanks = new int[argumentNodeLength];
		boolean[] argumentDataTypeArbitrarinesses = new boolean[argumentNodeLength];
		boolean[] argumentArrayRankArbitrarinesses = new boolean[argumentNodeLength];
		Arrays.fill(argumentDataTypeArbitrarinesses, false);
		Arrays.fill(argumentArrayRankArbitrarinesses, false);

		for (int argumentNodeIndex=0; argumentNodeIndex<argumentNodeLength; argumentNodeIndex++) {
			String dataTypeName = argumentNodes[argumentNodeIndex].getAttribute(AttributeKey.DATA_TYPE);

			// データ型名のエイリアス（floatに対するdoubleなど）を一意な型名に揃えるため、一旦DataTypeに変換して戻す
			try {
				DataType dataType = DataTypeName.getDataTypeOf(dataTypeName);
				dataTypeName = DataTypeName.getDataTypeNameOf(dataType);
			} catch (VnanoException e) {
				// DataTypeに定義されない未知の型の場合は、記述された型名をそのまま使用する
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


	public static String getSignatureOf(AbstractFunction function) {
		return getSignatureOf(function, "");
	}
	public static String getSignatureOf(AbstractFunction function, String nameSpacePrefix) {
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

	// 後の工程での削除候補
	public static String getAssemblyIdentifierOf(String variableName) {
		return AssemblyWord.OPERAND_PREFIX_IDENTIFIER + variableName;
	}

	public static String getAssemblyIdentifierOf(AstNode variableNode) {
		String variableName = variableNode.getAttribute(AttributeKey.IDENTIFIER_VALUE);
		String serialNumber = variableNode.getAttribute(AttributeKey.IDENTIFIER_SERIAL_NUMBER);
		String assemblyIdentifier
				= AssemblyWord.OPERAND_PREFIX_IDENTIFIER
				+ variableName
				+ AssemblyWord.IDENTIFIER_SERIAL_NUMBER_SEPARATOR
				+ serialNumber;

		return assemblyIdentifier;
	}

	// 後で AbstractVariable がシリアルナンバーを持てるようにした場合は、持っていれば付けるべき
	public static String getAssemblyIdentifierOf(AbstractVariable variable) {
		return getAssemblyIdentifierOf(variable, "");
	}

	public static String getAssemblyIdentifierOf(AbstractVariable variable, String nameSpacePrefix) {
		String variableName = variable.getVariableName();
		return AssemblyWord.OPERAND_PREFIX_IDENTIFIER + nameSpacePrefix + variableName;
	}

	public static String getNameSpacePrefixOf(AbstractVariable variable) {
		if (variable.hasNameSpace()) {
			return variable.getNameSpace() + ScriptWord.NAME_SPACE_SEPARATOR;
		} else {
			return "";
		}
	}

	public static String getNameSpacePrefixOf(AbstractFunction function) {
		if (function.hasNameSpace()) {
			return function.getNameSpace() + ScriptWord.NAME_SPACE_SEPARATOR;
		} else {
			return "";
		}
	}

}
