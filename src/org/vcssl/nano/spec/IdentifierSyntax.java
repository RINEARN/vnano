/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.compiler.AstNode;
import org.vcssl.nano.compiler.AttributeKey;
import org.vcssl.nano.lang.AbstractFunction;
import org.vcssl.nano.lang.AbstractVariable;
import org.vcssl.nano.lang.DataType;


// SignatureSyntax に変更する？
public class IdentifierSyntax {


	public static String getSignatureOf(String functionName,
			String[] parameterDataTypeNames, int[] parameterArrayRanks) {

		StringBuilder builder = new StringBuilder();

		builder.append(functionName);
		builder.append("(");

		int parameterLength = parameterDataTypeNames.length;
		for (int parameterIndex=0; parameterIndex<parameterLength; parameterIndex++) {

			builder.append(parameterDataTypeNames[parameterIndex]);
			int paramRank = parameterArrayRanks[parameterIndex];
			for (int dim=0; dim<paramRank; dim++) {
				builder.append("[]");
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
		AstNode[] argumentNodes = functionDeclarationNode.getChildNodes();
		int argumentNodeLength = argumentNodes.length;

		//DataType[] argumentDataTypes = new DataType[argumentNodeLength];
		String[] argumentDataTypeNames = new String[argumentNodeLength];
		int[] argumentArrayRanks = new int[argumentNodeLength];

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
				functionName, argumentDataTypeNames, argumentArrayRanks
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
				functionName, argumentDataTypeNames, argumentArrayRanks
		);

		return signature;
	}


	public static String getSignatureOf(AbstractFunction connector) {

		DataType[] parameterDataTypes = connector.getParameterDataTypes();
		int[] parameterArrayRanks = connector.getParameterArrayRanks();

		int parameterLength = parameterDataTypes.length;
		String[] parameterDataTypeNames = new String[parameterLength];
		for (int parameterIndex=0; parameterIndex<parameterLength; parameterIndex++) {
			parameterDataTypeNames[parameterIndex]
					= DataTypeName.getDataTypeNameOf(parameterDataTypes[parameterIndex]);
		}

		String signature = getSignatureOf(
				connector.getFunctionName(), parameterDataTypeNames, parameterArrayRanks
		);

		return signature;
	}


	// 以下はもう呼び出し側で行ってもいいのでは？　
	// 関数定義サポートしたのでローカルかグローバルかでprefix変わってくるし

	public static String getAssemblyIdentifierOf(String functionName,
			String[] parameterDataTypeNames, int[] parameterArrayRanks) {

		return AssemblyWord.OPERAND_PREFIX_IDENTIFIER
				+ getSignatureOf(functionName, parameterDataTypeNames, parameterArrayRanks);
	}

	public static String getAssemblyIdentifierOfCalleeFunctionOf(AstNode callerNode) {
		return AssemblyWord.OPERAND_PREFIX_IDENTIFIER
				+ getSignatureOfCalleeFunctionOf(callerNode);
	}

	public static String getAssemblyIdentifierOf(AbstractFunction connector) {
		return AssemblyWord.OPERAND_PREFIX_IDENTIFIER
				+ getSignatureOf(connector);
	}

	// 後の工程での削除候補
	public static String getAssemblyIdentifierOf(String variableName) {
		return AssemblyWord.OPERAND_PREFIX_IDENTIFIER + variableName;
	}

	public static String getAssemblyIdentifierOf(AbstractVariable variable) {
		return AssemblyWord.OPERAND_PREFIX_IDENTIFIER + variable.getVariableName();
	}

}
