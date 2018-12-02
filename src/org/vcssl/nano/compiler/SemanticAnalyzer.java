/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;


import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.vcssl.nano.VnanoSyntaxException;
import org.vcssl.nano.compiler.AstNode;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.lang.AbstractFunction;
import org.vcssl.nano.lang.AbstractVariable;
import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.lang.FunctionTable;
import org.vcssl.nano.lang.VariableTable;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.IdentifierSyntax;
import org.vcssl.nano.spec.LiteralSyntax;
import org.vcssl.nano.spec.ScriptWord;
import org.vcssl.nano.vm.memory.DataException;


/**
 * <p>
 * コンパイラ内において、
 * {@link Parser Parser} （構文解析器）が出力した
 * 抽象構文木（AST）に対して意味解析処理を行い、
 * コード生成に必要な情報の補間や異常の検出などを行う、意味解析器のクラスです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class SemanticAnalyzer {


	/**
	 * このクラスは状態を保持するフィールドを持たないため、コンストラクタは何もしません。
	 */
	public SemanticAnalyzer() {
	}


	/**
	 * AST(抽象構文木)の内容を解析し、コード生成に必要な各種情報を補完した、新しいASTを生成して返します。
	 *
	 * @param inputAst 解析対象のASTのルートノード
	 * @param Intterconnect interconnect 外部変数・関数の情報を保持しているインターコネクト
	 * @return 各種情報を補完したASTのルートノード
	 * @throws DataException ローカル変数のデータ型が無効な場合に発生します。
	 * @throws VnanoSyntaxException 存在しない変数を参照している場合に発生します。
	 */
	public AstNode analyze(AstNode inputAst, Interconnect interconnect)
			throws VnanoSyntaxException, DataException {

		// インターコネクトから外部変数・外部関数のテーブルを取得
		VariableTable globalVariableTable = interconnect.getGlobalVariableTable();
		FunctionTable functionTable = interconnect.getGlobalFunctionTable();

		// ASTを入力ASTをクローンして出力ASTを生成
		AstNode outputAst = inputAst.clone();

		// リーフノードの属性値を設定
		this.supplementLeafAttributes(outputAst, globalVariableTable);

		// 演算子ノードの属性値を設定
		this.supplementOperatorAttributes(outputAst, functionTable);

		// 式ノードの属性値を設定
		this.supplementExpressionAttributes(outputAst);

		return outputAst;
	}


	/**
	 * 引数に渡されたAST（抽象構文木）の内容を解析し、その中のリーフノードに対して、
	 * 中間コード生成を行うために不足している属性値を追加設定します
	 * （従って、このメソッドは破壊的メソッドです）。
	 *
	 * 具体的な例としては、式中のリテラルや、変数へのアクセスを行っているノードのデータ型が解析され、
	 * 各ノードの {@link AttributeKey#DATA_TYPE DATA_TYPE} 属性の値として設定されます。
	 * 同様に配列次元数も解析され、 {@link AttributeKey#RANK RANK} 属性の値として設定されます。
	 *
	 * @param astRootNode 解析・設定対象のASTのルートノード（メソッド実行後、各ノードに属性値が追加されます）
	 * @param globalVariableTable AST内で参照しているグローバル変数情報を持つ変数テーブル
	 * @throws DataException ローカル変数のデータ型が無効な場合にスローされます。
	 * @throws VnanoSyntaxException 存在しない変数を参照している場合にスローされます。
	 */
	private void supplementLeafAttributes(AstNode astRootNode, VariableTable globalVariableTable)
			throws VnanoSyntaxException, DataException {

		if (!astRootNode.hasChildNodes()) {
			return;
		}

		Map<String, String> localVariableTypeMap = new HashMap<String, String>();
		Map<String, Integer> localVariableRankMap = new HashMap<String, Integer>();

		AstNode currentNode = astRootNode;
		int currentDepth = 0; // ブロック終端による変数削除などで使用
		int lastDepth = 0;
		int localVariableCount = 0;
		Deque<Integer> localVariableCountStack = new ArrayDeque<Integer>();

		do {
			currentNode = currentNode.getPreorderTraversalNextNode();
			lastDepth = currentDepth;
			currentDepth = currentNode.getDepth();

			// ブロック文に入った場合: 上階層のローカル変数個数をスタックに退避してリセット
			if (currentDepth > lastDepth) {
				localVariableCountStack.push(localVariableCount);
				localVariableCount = 0;

			// ブロック文を抜けた場合: その階層のローカル変数を削除
			} else if (currentDepth < lastDepth) {
				localVariableCount = (int)localVariableCountStack.pop();
			}

			// ローカル変数ノードの場合: ローカル変数テーブルに追加
			if (currentNode.getType() == AstNode.Type.VARIABLE) {
				String variableName = currentNode.getAttribute(AttributeKey.IDENTIFIER_VALUE);
				localVariableTypeMap.put(variableName, currentNode.getDataTypeName());
				localVariableRankMap.put(variableName, currentNode.getRank());
				localVariableCount++;
			}

			// リーフノードの場合: 属性値を求めて設定
			if (currentNode.getType() == AstNode.Type.LEAF) {

				//Variable variable = null;
				String operandType = currentNode.getAttribute(AttributeKey.LEAF_TYPE);

				// 定数リテラル
				if (operandType.equals(AttributeValue.LITERAL)){
					String literal = currentNode.getAttribute(AttributeKey.LITERAL_VALUE);
					currentNode.addAttribute(AttributeKey.RANK, "0");
					currentNode.addAttribute(AttributeKey.DATA_TYPE, LiteralSyntax.getDataTypeNameOfLiteral(literal));
				}

				// 変数
				if (operandType.equals(AttributeValue.VARIABLE_IDENTIFIER)) {

					String identifier = currentNode.getAttribute(AttributeKey.IDENTIFIER_VALUE);

					// ローカル変数
					if (localVariableTypeMap.containsKey(identifier)) {

						currentNode.addAttribute(AttributeKey.SCOPE, AttributeValue.LOCAL);
						currentNode.addAttribute(AttributeKey.RANK, Integer.toString(localVariableRankMap.get(identifier)));
						currentNode.addAttribute(AttributeKey.DATA_TYPE, localVariableTypeMap.get(identifier));

					// グローバル変数
					} else if (globalVariableTable.containsVariableWithName(identifier)) {

						currentNode.addAttribute(AttributeKey.SCOPE, AttributeValue.GLOBAL);
						AbstractVariable variable = globalVariableTable.getVariableByName(identifier);
						currentNode.addAttribute(AttributeKey.RANK, Integer.toString(variable.getRank()));
						currentNode.addAttribute(
								AttributeKey.DATA_TYPE, DataTypeName.getDataTypeNameOf(variable.getDataType())
						);

					} else {
						throw new VnanoSyntaxException(
								ErrorType.VARIABLE_IS_NOT_FOUND, identifier,
								currentNode.getFileName(), currentNode.getLineNumber()
						);
					}
				}
			}
		} while (!currentNode.isPreorderTraversalLastNode());
	}


	/**
	 * 引数に渡されたAST（抽象構文木）の内容を解析し、その中の演算子ノードに対して、
	 * 中間コード生成を行うために不足している属性値を追加設定します
	 * （従って、このメソッドは破壊的メソッドです）。
	 *
	 * 具体的な例としては、式中の演算子ノードのデータ型が、リーフノードのデータ型に基づいて解析され、
	 * 各ノードの {@link AttributeKey#DATA_TYPE DATA_TYPE} 属性の値として設定されます。
	 * 同様に配列次元数も解析され、 {@link AttributeKey#RANK RANK} 属性の値として設定されます。
	 * 関数の呼び出しを行っている箇所のデータ型・配列次元数も、
	 * このメソッドによって解析され、該当箇所のノードの属性値に設定されます。
	 *
	 * このメソッドは、ASTを末端からボトムアップの順序で辿りながら、演算子の属性値を解決していくため、
	 * 先に {@link SemanticAnalyzer#supplementLeafAttributes supplementLeafAttributes} メソッドを使用して、
	 * リーフノードの属性値の設定を済ませておく必要があります。
	 *
	 * @param astRootNode 解析・設定対象のASTのルートノード（メソッド実行後、各ノードに属性値が追加されます）
	 * @param functionTable AST内で参照している関数情報を持つ関数テーブル
	 * @throws VnanoSyntaxException ASTの内容が構文的に正しくない場合にスローされます。
	 */
	private void supplementOperatorAttributes(AstNode astRootNode, FunctionTable functionTable) throws VnanoSyntaxException {


		// !!! 重複が多いので切り出して要リファクタ


		// 構文木の全ノードに対し、末端からボトムアップの順序で辿りながら処理する
		AstNode currentNode = astRootNode.getPostorderTraversalFirstNode();
		while(currentNode != astRootNode) {

			if(currentNode.getType() == AstNode.Type.OPERATOR) {

				// 子ノードの情報から、演算子ノードのデータ型や配列ランクなどを、以下の変数に求める
				int rank = -1;
				String dataType = null;
				String operationDataType = null;

				String execType = currentNode.getAttribute(AttributeKey.OPERATOR_EXECUTOR);
				String syntaxType = currentNode.getAttribute(AttributeKey.OPERATOR_SYNTAX);
				switch (execType) {
					// 代入演算子の場合
					case AttributeValue.ASSIGNMENT : {
						AstNode[] inputNodes = currentNode.getChildNodes();
						dataType = inputNodes[0].getDataTypeName();
						operationDataType = dataType;
						rank = inputNodes[0].getRank();
						break;
					}
					// 算術演算子の場合
					case AttributeValue.ARITHMETIC : {
						switch (syntaxType) {
							case AttributeValue.BINARY : {
								AstNode[] inputNodes = currentNode.getChildNodes();
								String leftOperandType = inputNodes[0].getDataTypeName();
								String rightOperandType = inputNodes[1].getDataTypeName();
								dataType = this.analyzeArithmeticBinaryOperatorDataType(
										leftOperandType, rightOperandType,
										currentNode.getAttribute(AttributeKey.OPERATOR_SYMBOL),
										currentNode.getFileName(), currentNode.getLineNumber()
								);
								operationDataType = dataType;
								rank = Math.max(inputNodes[0].getRank(), inputNodes[1].getRank());
								break;
							}
							case AttributeValue.PREFIX : {
								AstNode[] inputNodes = currentNode.getChildNodes();
								dataType = inputNodes[0].getDataTypeName();
								operationDataType = dataType;
								rank = inputNodes[0].getRank();
								break;
							}
							case AttributeValue.POSTFIX : {
								AstNode[] inputNodes = currentNode.getChildNodes();
								dataType = inputNodes[0].getDataTypeName();
								operationDataType = dataType;
								rank = inputNodes[0].getRank();
								break;
							}
						}
						break;
					}
					// 比較演算子の場合
					case AttributeValue.COMPARISON : {
						AstNode[] inputNodes = currentNode.getChildNodes();
						String leftOperandType = inputNodes[0].getDataTypeName();
						String rightOperandType = inputNodes[1].getDataTypeName();
						dataType = DataTypeName.BOOL;
						operationDataType = this.analyzeComparisonBinaryOperatorDataType(
								leftOperandType, rightOperandType,
								currentNode.getAttribute(AttributeKey.OPERATOR_SYMBOL),
								currentNode.getFileName(), currentNode.getLineNumber()
						);
						rank = Math.max(inputNodes[0].getRank(), inputNodes[1].getRank());
						break;
					}
					// 論理演算子の場合
					case AttributeValue.LOGICAL : {
						switch (syntaxType) {
							case AttributeValue.BINARY : {
								AstNode[] inputNodes = currentNode.getChildNodes();
								String leftOperandType = inputNodes[0].getDataTypeName();
								String rightOperandType = inputNodes[1].getDataTypeName();
								dataType = DataTypeName.BOOL;
								operationDataType = this.analyzeLogicalBinaryOperatorDataType(
										leftOperandType, rightOperandType,
										currentNode.getAttribute(AttributeKey.OPERATOR_SYMBOL),
										currentNode.getFileName(), currentNode.getLineNumber()
								);
								rank = Math.max(inputNodes[0].getRank(), inputNodes[1].getRank());
								break;
							}
							case AttributeValue.PREFIX : {
								AstNode[] inputNodes = currentNode.getChildNodes();
								dataType = DataTypeName.BOOL;
								operationDataType = DataTypeName.BOOL;
								rank = inputNodes[0].getRank();
								break;
							}
						}
						break;
					}
					// 複合代入演算子の場合
					case AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT : {
						AstNode[] inputNodes = currentNode.getChildNodes();
						dataType = inputNodes[0].getDataTypeName();
						operationDataType = dataType;
						rank = inputNodes[0].getRank();
						break;
					}
					// 関数呼び出し演算子の場合
					case AttributeValue.CALL : {
						// 関数テーブルから取り寄せられない場合は構文エラー
						if (!functionTable.hasCalleeFunctionOf(currentNode)) {
							String functionIdentifier = IdentifierSyntax.getUniqueIdentifierOfCalleeFunctionOf(currentNode);
							System.out.println(currentNode);
							throw new VnanoSyntaxException(
									ErrorType.FUNCTION_IS_NOT_FOUND, functionIdentifier,
									currentNode.getFileName(), currentNode.getLineNumber()
							);
						}
						AbstractFunction function = functionTable.getCalleeFunctionOf(currentNode);
						dataType = DataTypeName.getDataTypeNameOf(function.getReturnDataType());
						operationDataType = dataType;
						rank = function.getReturnArrayRank();
						break;
					}
					// 配列要素アクセス演算子の場合
					case AttributeValue.INDEX : {
						AstNode[] inputNodes = currentNode.getChildNodes();
						dataType = inputNodes[0].getDataTypeName();
						rank = 0;
						break;
					}
				}

				// 演算子ノードに属性値を設定
				if (dataType != null) {
					currentNode.addAttribute(AttributeKey.DATA_TYPE, dataType);
				}
				if (operationDataType != null) {
					currentNode.addAttribute(AttributeKey.OPERATOR_EXECUTION_DATA_TYPE, operationDataType);
				}
				if (rank != -1) {
					currentNode.addAttribute(AttributeKey.RANK, Integer.toString(rank));
				}
			}

			// 次のノードへボトムアップの順序で移動
			currentNode = currentNode.getPostorderTraversalNextNode();
		}
	}

	/**
	 * 引数に渡されたAST（抽象構文木）の内容を解析し、その中の式ノードに対して、
	 * 中間コード生成を行うために不足している属性値を追加設定します
	 * （従って、このメソッドは破壊的メソッドです）。
	 *
	 * 具体的には、式の値のデータ型が {@link AttributeKey#DATA_TYPE DATA_TYPE} 属性の値として、
	 * 配列次元数が {@link AttributeKey#RANK RANK} 属性の値として設定されます。
	 * なお、式ノードの直下に位置する演算子またはリーフノード（必ず一つ）が、その式の値となります。
	 *
	 * このメソッドを使用するよりも先に、
	 * {@link SemanticAnalyzer#supplementLeafAttributes supplementLeafAttributes} メソッドおよび
	 * {@link SemanticAnalyzer#supplementOperatorAttributes supplementOperatorAttributes} メソッドを使用して、
	 * 式のASTを構成する演算子ノードやリーフノードの属性値の設定を済ませておく必要があります。
	 *
	 * @param astRootNode 解析・設定対象のASTのルートノード（メソッド実行後、各ノードに属性値が追加されます）
	 */
	private void supplementExpressionAttributes(AstNode astRootNode) {

		AstNode currentNode = astRootNode.getPostorderTraversalFirstNode();
		while(currentNode != astRootNode) {

			if(currentNode.getType() == AstNode.Type.EXPRESSION) {
				AstNode[] inputNodes = currentNode.getChildNodes();
				currentNode.addAttribute(AttributeKey.DATA_TYPE, inputNodes[0].getDataTypeName());
				currentNode.addAttribute(AttributeKey.RANK, Integer.toString(inputNodes[0].getRank()));
			}

			currentNode = currentNode.getPostorderTraversalNextNode();
		}
	}


	/**
	 * 算術二項演算子のオペランドのデータ型を解析し、演算実行データ型を決定して返します。
	 *
	 * オペランドの値は、このメソッドが返す演算実行データ型の値に型変換されてから、演算が実行されます。
	 * また、算術演算結果のデータ型も、この演算実行データ型と同じものになります。
	 *
	 * 演算実行データ型は、オペランドの少なくとも一方が string 型なら、必ず string 型となります。
	 * また、一方が int 型で他方が float 型の場合は、float 型となります。
	 * それ以外の場合では、両オペランドの型が等しい必要があり、
	 * その型がそのまま演算実行データ型となります。
	 *
	 * ただし、bool 型のオペランドは算術演算では使用できません。
	 * また、string 型のオペランドは、加算においてのみ使用できます。
	 *
	 * @param leftOperandType 左オペランドのデータ型の名前
	 * @param rightOperandType 右オペランドのデータ型の名前
	 * @param operatorSymbol 演算子の記号
	 * @param fileName 対象処理が記述されたファイル名（例外発生時のエラー情報に使用）
	 * @param fileName 対象処理が記述された行番号（例外発生時のエラー情報に使用）
	 * @return 演算子の演算実行データ型の名前
	 * @throws VnanoSyntaxException 対象演算子に対して使用できないデータ型であった場合にスローされます。
	 */
	private String analyzeArithmeticBinaryOperatorDataType(
			String leftOperandType, String rightOperandType, String operatorSymbol,
			String fileName, int lineNumber) throws VnanoSyntaxException {

		// 文字列型を含む場合は文字列
		if (DataTypeName.isDataTypeNameOf(DataType.STRING,leftOperandType)
				|| DataTypeName.isDataTypeNameOf(DataType.STRING,rightOperandType) ) {

			if (operatorSymbol.equals(ScriptWord.PLUS)) {
				return DataTypeName.STRING;
			}
		}
		// 整数同士は整数
		if (DataTypeName.isDataTypeNameOf(DataType.INT64,leftOperandType)
				&& DataTypeName.isDataTypeNameOf(DataType.INT64,rightOperandType) ) {
			return DataTypeName.INT;
		}
		// 浮動小数点数同士は浮動小数点数
		if (DataTypeName.isDataTypeNameOf(DataType.FLOAT64,leftOperandType)
				&& DataTypeName.isDataTypeNameOf(DataType.FLOAT64,rightOperandType) ) {
			return DataTypeName.FLOAT;
		}
		// 整数と浮動小数点数の混合は浮動小数点数
		if (DataTypeName.isDataTypeNameOf(DataType.INT64,leftOperandType)
				&& DataTypeName.isDataTypeNameOf(DataType.FLOAT64,rightOperandType) ) {
			return DataTypeName.FLOAT;
		}
		// 浮動小数点数と整数の混合は浮動小数点数
		if (DataTypeName.isDataTypeNameOf(DataType.FLOAT64,leftOperandType)
				&& DataTypeName.isDataTypeNameOf(DataType.INT64,rightOperandType) ) {
			return DataTypeName.FLOAT;
		}

		throw new VnanoSyntaxException(
			ErrorType.INVALID_DATA_TYPES_FOR_BINARY_OPERATOR,
			new String[] {operatorSymbol, leftOperandType, rightOperandType},
			fileName, lineNumber
		);
	}

	/**
	 * 比較二項演算子のオペランドのデータ型を解析し、演算実行データ型を決定して返します。
	 *
	 * オペランドの値は、このメソッドが返す演算実行データ型の値に型変換されてから、演算が実行されます。
	 * ただし、比較演算結果のデータ型は常に bool 型です。
	 *
	 * 演算実行データ型は、オペランドの少なくとも一方が string 型なら、必ず string 型となります。
	 * また、一方が int 型で他方が float 型の場合は、float 型となります。
	 * それ以外の場合では、両オペランドの型が等しい必要があり、
	 * その型がそのまま演算実行データ型となります。
	 *
	 * @param leftOperandType 左オペランドのデータ型の名前
	 * @param rightOperandType 右オペランドのデータ型の名前
	 * @param operatorSymbol 演算子の記号
	 * @param fileName 対象処理が記述されたファイル名（例外発生時のエラー情報に使用）
	 * @param fileName 対象処理が記述された行番号（例外発生時のエラー情報に使用）
	 * @return 演算子の演算実行データ型の名前
	 * @throws VnanoSyntaxException 対象演算子に対して使用できないデータ型であった場合にスローされます。
	 */
	private String analyzeComparisonBinaryOperatorDataType(
			String leftOperandType, String rightOperandType, String operatorSymbol,
			String fileName, int lineNumber) throws VnanoSyntaxException {

		// 文字列型を含む場合は文字列
		if (DataTypeName.isDataTypeNameOf(DataType.STRING,leftOperandType)
				|| DataTypeName.isDataTypeNameOf(DataType.STRING,rightOperandType) ) {
				return DataTypeName.STRING;
		}
		// 論理型同士は論理型
		if (DataTypeName.isDataTypeNameOf(DataType.BOOL,leftOperandType)
				&& DataTypeName.isDataTypeNameOf(DataType.BOOL,rightOperandType) ) {
			return DataTypeName.BOOL;
		}
		// 整数同士は整数
		if (DataTypeName.isDataTypeNameOf(DataType.INT64,leftOperandType)
				&& DataTypeName.isDataTypeNameOf(DataType.INT64,rightOperandType) ) {
			return DataTypeName.INT;
		}
		// 浮動小数点数同士は浮動小数点数
		if (DataTypeName.isDataTypeNameOf(DataType.FLOAT64,leftOperandType)
				&& DataTypeName.isDataTypeNameOf(DataType.FLOAT64,rightOperandType) ) {
			return DataTypeName.FLOAT;
		}
		// 整数と浮動小数点数の混合は浮動小数点数
		if (DataTypeName.isDataTypeNameOf(DataType.INT64,leftOperandType)
				&& DataTypeName.isDataTypeNameOf(DataType.FLOAT64,rightOperandType) ) {
			return DataTypeName.FLOAT;
		}
		// 浮動小数点数と整数の混合は浮動小数点数
		if (DataTypeName.isDataTypeNameOf(DataType.FLOAT64,leftOperandType)
				&& DataTypeName.isDataTypeNameOf(DataType.INT64,rightOperandType) ) {
			return DataTypeName.FLOAT;
		}

		throw new VnanoSyntaxException(
			ErrorType.INVALID_DATA_TYPES_FOR_BINARY_OPERATOR,
			new String[] {operatorSymbol, leftOperandType, rightOperandType},
			fileName, lineNumber
		);
	}

	/**
	 * 論理二項演算子のオペランドのデータ型を解析し、演算実行データ型を決定して返します。
	 *
	 * オペランドの値は、このメソッドが返す演算実行データ型の値に型変換されてから、演算が実行されます。
	 * ただし、比較演算結果のデータ型は常に bool 型です。
	 *
	 * デフォルトの文法では、論理二項演算子のオペランドは全て bool 型であるべきであり、
	 * 演算実行データ型も常に bool 型です。
	 * そのため、スクリプトコードが正しい場合、このメソッドを使用しなくても、結果は bool で確定しています。
	 * しかしながら、スクリプトコードによっては bool 型以外のオペランドが入力される可能性もあるため、
	 * このメソッドはオペランドの型検査も兼ねて使用されます。
	 *
	 * @param leftOperandType 左オペランドのデータ型の名前
	 * @param rightOperandType 右オペランドのデータ型の名前
	 * @param operatorSymbol 演算子の記号
	 * @param fileName 対象処理が記述されたファイル名（例外発生時のエラー情報に使用）
	 * @param fileName 対象処理が記述された行番号（例外発生時のエラー情報に使用）
	 * @return 演算子の演算実行データ型の名前
	 * @throws VnanoSyntaxException 対象演算子に対して使用できないデータ型であった場合にスローされます。
	 */
	private String analyzeLogicalBinaryOperatorDataType(
			String leftOperandType, String rightOperandType, String operatorSymbol,
			String fileName, int lineNumber) throws VnanoSyntaxException {

		if (DataTypeName.isDataTypeNameOf(DataType.BOOL,leftOperandType)
				&& DataTypeName.isDataTypeNameOf(DataType.BOOL,rightOperandType) ) {
			return DataTypeName.BOOL;
		}

		throw new VnanoSyntaxException(
			ErrorType.INVALID_DATA_TYPES_FOR_BINARY_OPERATOR,
			new String[] {operatorSymbol, leftOperandType, rightOperandType},
			fileName, lineNumber
		);
	}

}
