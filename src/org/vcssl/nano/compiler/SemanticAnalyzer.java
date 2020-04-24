/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;


import java.util.ArrayDeque;
import java.util.Deque;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.compiler.AstNode;
import org.vcssl.nano.interconnect.AbstractFunction;
import org.vcssl.nano.interconnect.AbstractVariable;
import org.vcssl.nano.interconnect.InternalFunction;
import org.vcssl.nano.interconnect.FunctionTable;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.interconnect.InternalVariable;
import org.vcssl.nano.interconnect.VariableTable;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.IdentifierSyntax;
import org.vcssl.nano.spec.ScriptWord;

//Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/compiler/SemanticAnalyzer.html
//ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/compiler/SemanticAnalyzer.html

/**
 * <p>
 * <span class="lang-en">
 * The class performing the function of the semantic analyzer in the compiler of the Vnano
 * </span>
 * <span class="lang-ja">
 * Vnano のコンパイラ内において, セマンティックアナライザ（意味解析器）の機能を担うクラスです
 * </span>
 * .
 * </p>
 *
 * <p>
 * &raquo; <a href="../../../../../src/org/vcssl/nano/compiler/SemanticAnalyzer.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/compiler/SemanticAnalyzer.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/compiler/SemanticAnalyzer.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class SemanticAnalyzer {

	/**
	 * <span class="lang-en">
	 * This constructor does nothing, because this class has no fields for storing state
	 * </span>
	 * <span class="lang-ja">
	 * このクラスは状態を保持するフィールドを持たないため, コンストラクタは何もしません
	 * </span>
	 * .
	 */
	public SemanticAnalyzer() {
	}


	/**
	 * <span class="lang-en">
	 * Analyze semantics of the AST and, returns new AST of which information
	 * required for generating intermediate code are supplemented
	 * </span>
	 * <span class="lang-ja">
	 * ASTの意味（セマンティクス）を解析し、
	 * 中間コード生成に必要な各種情報を補完した、新しいASTを生成して返します
	 * <span>
	 * .
	 * @param inputAst
	 *   <span class="lang-en">The root node of the AST to be analyzed.</span>
	 *   <span class="lang-ja">解析対象のASTのルートノード.</span>
	 *
	 * @param Intterconnect interconnect
	 *   <span class="lang-en">The interconnect to which external variables/functions are connected.</span>
	 *   <span class="lang-ja">外部変数・関数が接続されているインターコネクト.</span>
	 *
	 * @return
	 *   <span class="lang-en">The semantic-analyzed/information-supplemented AST.</span>
	 *   <span class="lang-ja">意味解析/情報補間済みのAST.</span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">Thrown when any semantic error has detected.</span>
	 *   <span class="lang-ja">セマンティクスにエラーが検出された場合にスローされます.</span>
	 */
	public AstNode analyze(AstNode inputAst, Interconnect interconnect)
			throws VnanoException {

		// インターコネクトから外部変数・外部関数のテーブルを取得
		VariableTable globalVariableTable = interconnect.getExternalVariableTable();
		FunctionTable globalFunctionTable = interconnect.getExternalFunctionTable();

		// ASTを入力ASTをクローンして出力ASTを生成
		AstNode outputAst = inputAst.clone();

		// リテラルタイプのリーフノードの属性値を設定（シグネチャ確定のため、関数識別子リーフノードの解析よりも前に済ませる必要がある）
		this.supplementLiteralLeafAttributes(outputAst);

		// 変数識別子タイプのリーフノード（変数宣言文ノードではない）に対して、参照している変数を判定し、その属性値を設定
		this.supplementVariableIdentifierLeafAttributes(outputAst, globalVariableTable);

		// 内部関数を抽出して関数テーブルを取得
		FunctionTable localFunctionTable = this.extractFunctions(outputAst);

		// 演算子ノードの属性値を設定
		this.supplementOperatorAttributes(outputAst, globalFunctionTable, localFunctionTable);

		// 式ノードの属性値を設定
		this.supplementExpressionAttributes(outputAst);

		// 関数ノードの属性を検査
		this.checkFunctionAttributes(outputAst);

		// 代入を伴う演算の左辺（インクリメント含む）が、代入/書き換え可能なものかどうか検査
		this.checkAssignmentTargetWritabilities(outputAst);

		// 配列アクセス演算子のアクセス対象を検査
		this.checkSubscriptTargetSubscriptabilities(outputAst);

		// 識別子を検査
		this.checkIdentifiers(outputAst);

		// return 文で返している戻り値の型や、return 文の位置を検査
		this.checkReturnValueTypesAndLocations(outputAst);

		return outputAst;
	}


	/**
	 * 引数に渡されたAST（抽象構文木）の内容を解析し、その中の変数参照箇所の箇所の識別子リーフノード
	 * （{@link AttributeKey#LEAF_TYPE LEAF_TYPE} 属性の値が {@link AttributeValue#VARIABLE_IDENTIFIER VARIABLE_IDENTIFIER}）
	 * に対して、中間コード生成を行うために不足している属性値を追加設定します
	 * （従って、このメソッドは破壊的メソッドです）。
	 *
	 * 具体的な例としては、呼び出し対象の関数を判定し、その戻り値のデータ型を、
	 * 関数識別子ノードの {@link AttributeKey#DATA_TYPE DATA_TYPE} 属性の値として設定されます。
	 * 同様に、戻り値の配列次元数が {@link AttributeKey#RANK RANK} 属性の値として設定されます。
	 *
	 * 同時に、このメソッド内では、関数のスコープ検査や多重宣言検査なども行われます。
	 *
	 * @param astRootNode 解析・設定対象のASTのルートノード（メソッド実行後、各ノードに属性値が追加されます）
	 * @param globalFunctionTable AST内で参照しているグローバル変数情報を持つ変数テーブル
	 * @throws VnanoException
	 *     存在しない変数を参照している場合や、スコープ外での参照、変数の重複宣言などが検出された場合にスローされます。
	 */
	private void supplementVariableIdentifierLeafAttributes(AstNode astRootNode, VariableTable globalVariableTable)
					throws VnanoException {

		if (!astRootNode.hasChildNodes()) {
			return;
		}

		// 同じ識別子/シグネチャのローカル変数を区別できるようにするため、識別子に加えてシリアルナンバー情報を付加する
		int localVariableSerialNumber = 0;

		// ローカル変数テーブル
		VariableTable localVariableTable = new VariableTable();

		AstNode currentNode = astRootNode;
		int currentBlockDepth = 0; // ブロック終端による変数削除などで使用
		int lastBlockDepth = 0;

		// ブロックスコープ内で宣言されたローカル変素の数を控えるカウンタ
		//（ブロックスコープ脱出時に変数をテーブルから削除するため）
		int currentBlockVariableCounter = 0;

		// 関数の引数や、for文の初期化文での宣言変数等は、宣言文の次のブロックに属すると見なす必要がある
		int nextBlockVariableCounter = 0;       // そのためのカウンタ
		boolean shouldCountToNextBlock = false; // 関数や for 文を踏むと true にし、true の場合に上のカウンタを使うようにする

		// 入れ子ブロックに入る際に、上記のローカル変数カウンタの値を退避するためのスタック
		Deque<Integer>scopeLocalVariableCounterStack = new ArrayDeque<Integer>();

		// ASTノードを、行がけ順の深さ優先走査で辿って処理していく
		do {
			currentNode = currentNode.getPreorderDftNextNode();
			lastBlockDepth = currentBlockDepth;
			currentBlockDepth = currentNode.getBlockDepth();

			// 関数宣言や for 文を踏むと、それ以降、ブロック開始までの宣言変数を、次のブロックに属するとカウントするよう設定
			if (currentNode.getType() == AstNode.Type.FUNCTION || currentNode.getType() == AstNode.Type.FOR) {
				shouldCountToNextBlock = true;
				nextBlockVariableCounter = 0;
			}

			// ブロックに入った or 出たポイントかどうかを判定して控える
			boolean entersNewBlock = currentNode.getType() == AstNode.Type.BLOCK;
			boolean exitsFromBlock = currentBlockDepth < lastBlockDepth     // ブロックの外の階層に降りた場合（ブロック深度減る）
				|| (entersNewBlock && currentBlockDepth == lastBlockDepth); // 出ると同時に別ブロックに入った場合（深度変わらず）

			// ブロック文の終端での処理: その階層のローカル変数/関数を削除し、スコープ内ローカル変数/関数リストをスタックから復元
			if (exitsFromBlock) {

				// ブロック内の変数は末尾に連続して詰まっているはずなので、末尾から連続で削除
				for (int i=0; i<currentBlockVariableCounter; i++) {
					localVariableTable.removeLastVariable();
				}
				// 脱出先ブロックスコープ内の変数の数をスタックから復元
				currentBlockVariableCounter = scopeLocalVariableCounterStack.pop();
			}

			// ブロック文に入った際の処理: 上階層のスコープ内ローカル変数カウンタの値をスタックに退避し、リセット
			// (ブロックを抜けたノードが別ブロックに入るノードだったりもするため、順序的には上の終端処理よりも後で行う)
			if (entersNewBlock) {
				scopeLocalVariableCounterStack.push(currentBlockVariableCounter); // add だと別の端への追加になるので注意
				currentBlockVariableCounter = 0;

				// 関数や for 文を踏んでいた場合、次のブロックに属するとカウントしていた変数があるので、それを加算
				if (shouldCountToNextBlock) {
					currentBlockVariableCounter += nextBlockVariableCounter;
					nextBlockVariableCounter = 0;
					shouldCountToNextBlock = false;
				}
			}

			// ローカル変数宣言文ノードの場合: ローカル変数マップに追加し、ノード自身にローカル変数インデックスやスコープも設定
			if (currentNode.getType() == AstNode.Type.VARIABLE) {
				String variableName = currentNode.getAttribute(AttributeKey.IDENTIFIER_VALUE);
				String dataTypeName = currentNode.getDataTypeName();
				int rank = currentNode.getRank();

				// ローカル変数の情報を保持するインスタンスを生成して変数テーブルに登録
				InternalVariable internalVariable = new InternalVariable(variableName, dataTypeName, rank, localVariableSerialNumber);
				localVariableTable.addVariable(internalVariable);
				if (shouldCountToNextBlock) {
					nextBlockVariableCounter++;
				} else {
					currentBlockVariableCounter++;
				}

				// ノードに属性を付加
				currentNode.setAttribute(AttributeKey.SCOPE, AttributeValue.LOCAL);
				currentNode.setAttribute(AttributeKey.IDENTIFIER_SERIAL_NUMBER, Integer.toString(localVariableSerialNumber));

				localVariableSerialNumber++;
			}

			// 変数の参照箇所のリーフノードの場合: 属性値を求めて設定
			if (currentNode.getType() == AstNode.Type.LEAF
					&& currentNode.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.VARIABLE_IDENTIFIER)) {

				String variableName = currentNode.getAttribute(AttributeKey.IDENTIFIER_VALUE);

				// ローカル変数
				if (localVariableTable.containsVariableWithName(variableName)) {

					AbstractVariable variable = localVariableTable.getVariableByName(variableName);
					currentNode.setAttribute(AttributeKey.IDENTIFIER_SERIAL_NUMBER, Integer.toString(variable.getSerialNumber()));
					currentNode.setAttribute(AttributeKey.SCOPE, AttributeValue.LOCAL);
					currentNode.setAttribute(AttributeKey.RANK, Integer.toString(variable.getRank()));
					currentNode.setAttribute(AttributeKey.DATA_TYPE, variable.getDataTypeName());
					if (variable.hasNameSpace()) {
						currentNode.setAttribute(AttributeKey.NAME_SPACE, variable.getNameSpace());
					}

				// グローバル変数
				} else if (globalVariableTable.containsVariableWithName(variableName)) {

					AbstractVariable variable = globalVariableTable.getVariableByName(variableName);
					currentNode.setAttribute(AttributeKey.SCOPE, AttributeValue.GLOBAL);
					currentNode.setAttribute(AttributeKey.RANK, Integer.toString(variable.getRank()));
					currentNode.setAttribute(AttributeKey.DATA_TYPE, variable.getDataTypeName());
					if (variable.hasNameSpace()) {
						currentNode.setAttribute(AttributeKey.NAME_SPACE, variable.getNameSpace());
					}

				} else {
					throw new VnanoException(
							ErrorType.VARIABLE_IS_NOT_FOUND, variableName,
							currentNode.getFileName(), currentNode.getLineNumber()
					);
				}
			}
		} while (!currentNode.isPreorderDftLastNode());
	}


	/**
	 * 引数に渡されたAST（抽象構文木）から関数宣言文を読み込み、
	 * その中で宣言されている関数（ローカル関数）を抽出して、関数テーブルにまとめて返します。
	 *
	 * @param astRootNode 関数を抽出する対象のASTのルートノード（メソッド実行後、各ノードに属性値が追加されます）
	 * @throws VnanoException 関数を宣言できない場所で宣言していた場合や、宣言内容に誤りがあった場合にスローされます。
	 */
	private FunctionTable extractFunctions(AstNode astRootNode) throws VnanoException {

		FunctionTable localFunctionTable = new FunctionTable();

		if (!astRootNode.hasChildNodes()) {
			return localFunctionTable;
		}

		AstNode currentNode = astRootNode;

		// ASTノードを、行がけ順の深さ優先走査で辿って処理していく
		do {
			currentNode = currentNode.getPreorderDftNextNode();

			// ローカル関数宣言文ノードの場合: ローカル関数マップに追加し、ノード自身にローカル関数インデックスやスコープも設定
			if (currentNode.getType() == AstNode.Type.FUNCTION) {

				// ローカル関数はルート直下の階層でしか宣言を許さない
				//（後方参照を可能にするので、あまり宣言場所が自由すぎるとコード上でも紛らわしくなりそうなので、少なくとも今は制約しておく）
				if (currentNode.getDepth() != 1) {
					throw new VnanoException(
							ErrorType.FUNCTION_IS_DECLARED_IN_INVALID_PLASE,
							currentNode.getFileName(), currentNode.getLineNumber()
					);
				}

				// 関数名を取得
				String functionName = currentNode.getAttribute(AttributeKey.IDENTIFIER_VALUE);

				// 戻り値のデータ型と次元を取得
				String returnTypeName = currentNode.getAttribute(AttributeKey.DATA_TYPE);
				int returnRank = currentNode.getRank();

				// 引数のノードを一括で取得
				AstNode[] argNodes = currentNode.getChildNodes();
				int argLength = argNodes.length;

				// 引数ノードの内容を検査
				this.checkArgumentDeclarationNodes(argNodes);

				// 引数の名前、データ型、次元、参照渡し宣言の有無、および定数宣言の有無を取得
				String[] argNames = new String[argLength];
				String[] argTypeNames = new String[argLength];
				int[] argRanks = new int[argLength];
				boolean[] argRefs = new boolean[argLength];
				boolean[] argConsts = new boolean[argLength];
				for (int argIndex=0; argIndex<argLength; argIndex++) {
					argNames[argIndex] = argNodes[argIndex].getAttribute(AttributeKey.IDENTIFIER_VALUE);
					argTypeNames[argIndex] = argNodes[argIndex].getAttribute(AttributeKey.DATA_TYPE);
					argRanks[argIndex] = argNodes[argIndex].getRank();
					if (argNodes[argIndex].hasAttribute(AttributeKey.MODIFIER)) {
						argRefs[argIndex] = argNodes[argIndex].getAttribute(AttributeKey.MODIFIER).contains(ScriptWord.REFERENCE);
					}
					argConsts[argIndex] = false; // スクリプト内での const 修飾子は未サポート
				}

				// 関数情報を保持するインスタンスを生成してテーブルに登録
				InternalFunction internalFunction = new InternalFunction(
					functionName, argNames, argTypeNames, argRanks, argRefs, argConsts, returnTypeName, returnRank
				);
				localFunctionTable.addFunction(internalFunction);
			}

		} while (!currentNode.isPreorderDftLastNode());

		return localFunctionTable;
	}


	/**
	 * 関数の引数宣言のASTノードが正しいか検査します。
	 * 検査の結果、正しかった場合には何も行わず、正しくなかった場合には例外をスローします。
	 *
	 * @param argNodes 全ての引数宣言のASTノードを格納する配列
	 * @throws VnanoException 引数宣言が正しくなかった場合にスローされます。
	 */
	private void checkArgumentDeclarationNodes(AstNode[] argNodes) throws VnanoException {
		for (AstNode argNode: argNodes) {
			// そもそも変数宣言ノードでなければ明らかにNG
			if (argNode.getType() != AstNode.Type.VARIABLE) {
				throw new VnanoException(
						ErrorType.INVALID_ARGUMENT_DECLARATION,
						argNode.getFileName(), argNode.getLineNumber()
				);
			}

			// デフォルト引数などはサポートされていないので、余計なASTがぶら下がっていたらNG
			if (argNode.hasChildNodes()) {
				AstNode[] argChildNodes = argNode.getChildNodes();
				for (AstNode argChildNode: argChildNodes) {
					AstNode.Type type = argChildNode.getType();

					// 配列である事を示す [ ] のノードだけはOKで、後は全てNG
					if (type != AstNode.Type.LENGTHS) {
						throw new VnanoException(
								ErrorType.INVALID_ARGUMENT_DECLARATION,
								argNode.getFileName(), argNode.getLineNumber()
						);
					}
				}
			}
		}
	}


	/**
	 * 引数に渡されたAST（抽象構文木）の内容を解析し、その中のリテラルのリーフノード
	 * （{@link AttributeKey#LEAF_TYPE LEAF_TYPE} 属性の値が {@link AttributeValue#LITERAL LITERAL}）
	 * に対して中間コード生成を行うために不足している属性値を追加設定します
	 * （従って、このメソッドは破壊的メソッドです）。
	 *
	 * 具体的な例としては、リテラルの記述内容からデータ型が解析され、
	 * 各ノードの {@link AttributeKey#DATA_TYPE DATA_TYPE} 属性の値として設定されます。
	 * 同様に配列次元数も {@link AttributeKey#RANK RANK} 属性の値として設定されます。
	 *
	 * @param astRootNode 解析・設定対象のASTのルートノード（メソッド実行後、各ノードに属性値が追加されます）
	 */
	private void supplementLiteralLeafAttributes(AstNode astRootNode) {

		if (!astRootNode.hasChildNodes()) {
			return;
		}

		// ASTノードを、行がけ順の深さ優先走査で辿って処理していく
		AstNode currentNode = astRootNode;
		do {
			currentNode = currentNode.getPreorderDftNextNode();

			// リテラルのリーフノードの場合: 属性値を求めて設定
			if (currentNode.getType() == AstNode.Type.LEAF
					&& currentNode.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.LITERAL)) {

				currentNode.setAttribute(AttributeKey.RANK, "0"); // 現状では配列のリテラルは存在しないため、常にスカラ

				// リテラルのデータ型はLexicalAnalyzerの時点で自明なので、その段階で既に設定されている
				//（ EVAL_NUMBER_AS_FLOATオプションの実装上も、そうした方が都合が良い ）
				// String literal = currentNode.getAttribute(AttributeKey.LITERAL_VALUE);
				// currentNode.setAttribute(AttributeKey.DATA_TYPE, LiteralSyntax.getDataTypeNameOfLiteral(literal));
			}
		} while (!currentNode.isPreorderDftLastNode());
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
	 * @param globalFunctionTable AST内で参照している外部関数の情報を持つ関数テーブル
	 * @param localFunctionTable AST内で参照している内部関数の情報を持つ関数テーブル
	 * @throws VnanoException ASTの内容が構文的に正しくない場合にスローされます。
	 */
	private void supplementOperatorAttributes(AstNode astRootNode,
			FunctionTable globalFunctionTable, FunctionTable localFunctionTable) throws VnanoException {


		// !!! 重複が多いので切り出して要リファクタ


		// 構文木の全ノードに対し、帰りがけ順の深さ優先走査で辿り、末端から処理していく
		AstNode currentNode = astRootNode.getPostorderDftFirstNode();
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
								rank = analyzeArithmeticComparisonLogicalBinaryOperatorRank(
										inputNodes[0].getRank(), inputNodes[1].getRank(),
										currentNode.getAttribute(AttributeKey.OPERATOR_SYMBOL),
										currentNode.getFileName(), currentNode.getLineNumber()
								);
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
						rank = analyzeArithmeticComparisonLogicalBinaryOperatorRank(
								inputNodes[0].getRank(), inputNodes[1].getRank(),
								currentNode.getAttribute(AttributeKey.OPERATOR_SYMBOL),
								currentNode.getFileName(), currentNode.getLineNumber()
						);
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
								rank = analyzeArithmeticComparisonLogicalBinaryOperatorRank(
										inputNodes[0].getRank(), inputNodes[1].getRank(),
										currentNode.getAttribute(AttributeKey.OPERATOR_SYMBOL),
										currentNode.getFileName(), currentNode.getLineNumber()
								);
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
						String leftOperandType = inputNodes[0].getDataTypeName();
						String rightOperandType = inputNodes[1].getDataTypeName();
						dataType = inputNodes[0].getDataTypeName();
						operationDataType = this.analyzeArithmeticCompoundAssignmentOperatorDataType(
								leftOperandType, rightOperandType,
								currentNode.getAttribute(AttributeKey.OPERATOR_SYMBOL),
								currentNode.getFileName(), currentNode.getLineNumber()
						);
						rank = analyzeCompoundAssignmentOperatorRank(
								inputNodes[0].getRank(), inputNodes[1].getRank(),
								currentNode.getAttribute(AttributeKey.OPERATOR_SYMBOL),
								currentNode.getFileName(), currentNode.getLineNumber()
						);
						break;
					}

					// 関数呼び出し演算子の場合
					case AttributeValue.CALL : {

						// 関数テーブルから、呼び出し対象の関数を検索
						AbstractFunction function = null;

						// ローカル関数
						if (localFunctionTable.hasCalleeFunctionOf(currentNode)) {
							currentNode.setAttribute(AttributeKey.SCOPE, AttributeValue.LOCAL);
							function = localFunctionTable.getCalleeFunctionOf(currentNode);

						// グローバル関数
						} else if (globalFunctionTable.hasCalleeFunctionOf(currentNode)) {
							currentNode.setAttribute(AttributeKey.SCOPE, AttributeValue.GLOBAL);
							function = globalFunctionTable.getCalleeFunctionOf(currentNode);

						} else {
							throw new VnanoException(
									ErrorType.FUNCTION_IS_NOT_FOUND,
									IdentifierSyntax.getSignatureOfCalleeFunctionOf(currentNode),
									currentNode.getFileName(), currentNode.getLineNumber()
							);
						}

						// 検索結果の関数を、呼び出し演算子の実引数で呼べるかどうか検査
						//（定数は参照渡しできない等の制約により、型は整合しても呼べないケースがある）
						this.checkFunctionCallablility(function, currentNode);

						currentNode.setAttribute(AttributeKey.CALLEE_SIGNATURE, IdentifierSyntax.getSignatureOf(function));
						if (function.hasNameSpace()) {
							currentNode.setAttribute(AttributeKey.NAME_SPACE, function.getNameSpace());
						}

						String[] argumentDataTypeNames = this.getArgumentDataTypeNames(currentNode);
						int[] argumentArrayRanks = this.getArgumentArrayRanks(currentNode);
						dataType = function.getReturnDataTypeName(argumentDataTypeNames, argumentArrayRanks);
						operationDataType = dataType;
						rank = function.getReturnArrayRank(argumentDataTypeNames, argumentArrayRanks);
						//rank = function.getReturnArrayRank();
						break;
					}

					// 配列要素アクセス演算子の場合
					case AttributeValue.SUBSCRIPT : {
						AstNode[] inputNodes = currentNode.getChildNodes();
						dataType = inputNodes[0].getDataTypeName();
						rank = 0;
						break;
					}

					// キャスト演算子の場合
					case AttributeValue.CAST : {
						dataType = currentNode.getDataTypeName();
						operationDataType = dataType;
						rank = currentNode.getRank();
						break;
					}
				}

				// 演算子ノードに属性値を設定
				if (dataType != null) {
					currentNode.setAttribute(AttributeKey.DATA_TYPE, dataType);
				}
				if (operationDataType != null) {
					currentNode.setAttribute(AttributeKey.OPERATOR_EXECUTION_DATA_TYPE, operationDataType);
				}
				if (rank != -1) {
					currentNode.setAttribute(AttributeKey.RANK, Integer.toString(rank));
				}
			}

			// 次のノードへボトムアップの順序で移動
			currentNode = currentNode.getPostorderDftNextNode();
		}
	}

	private String[] getArgumentDataTypeNames(AstNode callOperatorNode) {
		AstNode[] childNodes = callOperatorNode.getChildNodes();
		int argumentN = childNodes.length - 1; // [0] は関数識別子のノードなので -1
		String[] dataTypeNames = new String[argumentN];
		for (int argumentIndex=0; argumentIndex<argumentN; argumentIndex++) {
			dataTypeNames[argumentIndex] = childNodes[argumentIndex+1].getDataTypeName();
		}
		return dataTypeNames;
	}

	private int[] getArgumentArrayRanks(AstNode callOperatorNode) {
		AstNode[] childNodes = callOperatorNode.getChildNodes();
		int argumentN = childNodes.length - 1; // [0] は関数識別子のノードなので -1
		int[] arrayRanks = new int[argumentN];
		for (int argumentIndex=0; argumentIndex<argumentN; argumentIndex++) {
			arrayRanks[argumentIndex] = childNodes[argumentIndex+1].getRank();
		}
		return arrayRanks;
	}

	// 引数のデータ型に基づいて検索した結果の関数を、呼び出し演算子の実引数で呼べるかどうか検査する
	//（定数は参照渡しできない等の制約により、型は整合しても呼べないケースがある）
	private void checkFunctionCallablility(AbstractFunction function, AstNode callerNode) throws VnanoException {
		AstNode[] argNodes = callerNode.getChildNodes(); // 注：[0]番要素は関数識別子、[1]以降が引数ノード
		String[] parameterNames = function.getParameterNames();
		boolean[] areParamConst = function.getParameterConstantnesses();
		boolean[] areParamRef = function.getParameterReferencenesses();
		int paramN = areParamRef.length;

		// 参照渡しかつ非 const な引数の場合、変数か配列要素（Subsctipt演算子）しか渡せないので、渡せるかどうか検査
		//   理由1：リテラルなどの定数値が呼び出し先で書き換えられると色々とまずい
		//   理由2：式の評価値のレジスタを参照渡しして書き換えられると、そのレジスタに依存する処理が色々とまずい
		for (int paramIndex=0; paramIndex<paramN; paramIndex++) {
			if (areParamRef[paramIndex] && !areParamConst[paramIndex]) {

				// 注：[0]番要素は関数識別子、[1]以降が引数ノード
				AstNode argNode = argNodes[paramIndex+1];

				// 変数かどうか
				boolean isVariable = argNode.getType() == AstNode.Type.LEAF
					&& argNode.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.VARIABLE_IDENTIFIER);

				// 配列要素かどうか
				boolean isSubscript = argNode.getType() == AstNode.Type.OPERATOR
					&& argNode.getAttribute(AttributeKey.OPERATOR_EXECUTOR).equals(AttributeValue.SUBSCRIPT);

				// 変数でも配列要素でもなければエラー
				if (!isVariable && !isSubscript) {
					String[] errorWords = new String[] {
						Integer.toString(paramIndex+1), parameterNames[paramIndex], function.getFunctionName()
					};
					throw new VnanoException(
						ErrorType.NON_VARIABLE_IS_PASSED_BY_REFERENCE, errorWords,
						callerNode.getFileName(), callerNode.getLineNumber()
					);
				}
			}
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

		// ASTノードを、帰りがけ順の深さ優先走査で辿って検査していく
		AstNode currentNode = astRootNode.getPostorderDftFirstNode();
		while(currentNode != astRootNode) {

			if(currentNode.getType() == AstNode.Type.EXPRESSION) {
				AstNode[] inputNodes = currentNode.getChildNodes();
				currentNode.setAttribute(AttributeKey.DATA_TYPE, inputNodes[0].getDataTypeName());
				currentNode.setAttribute(AttributeKey.RANK, Integer.toString(inputNodes[0].getRank()));
			}

			currentNode = currentNode.getPostorderDftNextNode();
		}
	}


	private boolean containsDataTypeInOperands(String dataType, String leftOperandType, String rightOperandType) {
		return leftOperandType.equals(dataType) || rightOperandType.equals(dataType);
	}

	/**
	 * 算術復号代入演算子のオペランドのデータ型を解析し、演算実行データ型を決定して返します。
	 *
	 * オペランドの値は、このメソッドが返す演算実行データ型の値に型変換されてから、演算が実行されます。
	 * ただし、演算後にキャストが行われ、最終的な演算子の値としてのデータ型は、左辺値と同じ型になります。
	 *
	 * このメソッドが返す演算実行データ型は、指定された複合演算子に対応する算術二項演算子における、
	 * {@link SemanticAnalyzer#analyzeArithmeticBinaryOperatorDataType(String, String, String, String, int)}
	 * メソッドの呼び出し結果と同様です。
	 *
	 * @param leftOperandType 左オペランドのデータ型の名前
	 * @param rightOperandType 右オペランドのデータ型の名前
	 * @param operatorSymbol 演算子の記号
	 * @param fileName 対象処理が記述されたファイル名（例外発生時のエラー情報に使用）
	 * @param lineNumber 対象処理が記述された行番号（例外発生時のエラー情報に使用）
	 * @return 演算子の演算実行データ型の名前
	 * @throws VnanoException 対象演算子に対して使用できないデータ型であった場合にスローされます。
	 */
	private String analyzeArithmeticCompoundAssignmentOperatorDataType(
			String leftOperandType, String rightOperandType, String operatorSymbol,
			String fileName, int lineNumber) throws VnanoException {

		String arithmeticBinaryOperatorSymbol = null;
		switch (operatorSymbol) {
			case ScriptWord.ADDITION_ASSIGNMENT : {
				arithmeticBinaryOperatorSymbol = ScriptWord.PLUS;
				break;
			}
			case ScriptWord.SUBTRACTION_ASSIGNMENT : {
				arithmeticBinaryOperatorSymbol = ScriptWord.MINUS;
				break;
			}
			case ScriptWord.MULTIPLICATION_ASSIGNMENT : {
				arithmeticBinaryOperatorSymbol = ScriptWord.MULTIPLICATION;
				break;
			}
			case ScriptWord.DIVISION_ASSIGNMENT : {
				arithmeticBinaryOperatorSymbol = ScriptWord.DIVISION;
				break;
			}
			case ScriptWord.REMAINDER_ASSIGNMENT : {
				arithmeticBinaryOperatorSymbol = ScriptWord.REMAINDER;
				break;
			}
			default : {
				throw new VnanoFatalException("Invalid arithmetic compound operator: " + operatorSymbol);
			}
		}
		return this.analyzeArithmeticBinaryOperatorDataType(
			leftOperandType, rightOperandType, arithmeticBinaryOperatorSymbol, fileName, lineNumber
		);
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
	 * @param lineNumber 対象処理が記述された行番号（例外発生時のエラー情報に使用）
	 * @return 演算子の演算実行データ型の名前
	 * @throws VnanoException 対象演算子に対して使用できないデータ型であった場合にスローされます。
	 */
	private String analyzeArithmeticBinaryOperatorDataType(
			String leftOperandType, String rightOperandType, String operatorSymbol,
			String fileName, int lineNumber) throws VnanoException {

		// 文字列型を含む場合は文字列
		if (DataTypeName.isDataTypeNameOf(DataType.STRING,leftOperandType)
				|| DataTypeName.isDataTypeNameOf(DataType.STRING,rightOperandType) ) {

			// 加算だけ許可する
			if (operatorSymbol.equals(ScriptWord.PLUS)) {
				return DataTypeName.STRING;
			}
		}

		// 整数同士は整数
		if (DataTypeName.isDataTypeNameOf(DataType.INT64,leftOperandType)
				&& DataTypeName.isDataTypeNameOf(DataType.INT64,rightOperandType) ) {
			if (this.containsDataTypeInOperands(DataTypeName.LONG, leftOperandType, rightOperandType)) {
				return DataTypeName.LONG;
			} else {
				return DataTypeName.INT;
			}
		}

		// 浮動小数点数同士は浮動小数点数
		if (DataTypeName.isDataTypeNameOf(DataType.FLOAT64,leftOperandType)
				&& DataTypeName.isDataTypeNameOf(DataType.FLOAT64,rightOperandType) ) {
			if (this.containsDataTypeInOperands(DataTypeName.DOUBLE, leftOperandType, rightOperandType)) {
				return DataTypeName.DOUBLE;
			} else {
				return DataTypeName.FLOAT;
			}
		}

		// 整数と浮動小数点数の混合は浮動小数点数
		if (DataTypeName.isDataTypeNameOf(DataType.INT64,leftOperandType)
				&& DataTypeName.isDataTypeNameOf(DataType.FLOAT64,rightOperandType) ) {
			if (this.containsDataTypeInOperands(DataTypeName.DOUBLE, leftOperandType, rightOperandType)) {
				return DataTypeName.DOUBLE;
			} else {
				return DataTypeName.FLOAT;
			}
		}

		// 浮動小数点数と整数の混合は浮動小数点数
		if (DataTypeName.isDataTypeNameOf(DataType.FLOAT64,leftOperandType)
				&& DataTypeName.isDataTypeNameOf(DataType.INT64,rightOperandType) ) {
			if (this.containsDataTypeInOperands(DataTypeName.DOUBLE, leftOperandType, rightOperandType)) {
				return DataTypeName.DOUBLE;
			} else {
				return DataTypeName.FLOAT;
			}
		}

		throw new VnanoException(
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
	 * @param lineNumber 対象処理が記述された行番号（例外発生時のエラー情報に使用）
	 * @return 演算子の演算実行データ型の名前
	 * @throws VnanoException 対象演算子に対して使用できないデータ型であった場合にスローされます。
	 */
	private String analyzeComparisonBinaryOperatorDataType(
			String leftOperandType, String rightOperandType, String operatorSymbol,
			String fileName, int lineNumber) throws VnanoException {

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
			if (this.containsDataTypeInOperands(DataTypeName.LONG, leftOperandType, rightOperandType)) {
				return DataTypeName.LONG;
			} else {
				return DataTypeName.INT;
			}
		}

		// 浮動小数点数同士は浮動小数点数
		if (DataTypeName.isDataTypeNameOf(DataType.FLOAT64,leftOperandType)
				&& DataTypeName.isDataTypeNameOf(DataType.FLOAT64,rightOperandType) ) {
			if (this.containsDataTypeInOperands(DataTypeName.DOUBLE, leftOperandType, rightOperandType)) {
				return DataTypeName.DOUBLE;
			} else {
				return DataTypeName.FLOAT;
			}
		}

		// 整数と浮動小数点数の混合は浮動小数点数
		if (DataTypeName.isDataTypeNameOf(DataType.INT64,leftOperandType)
				&& DataTypeName.isDataTypeNameOf(DataType.FLOAT64,rightOperandType) ) {
			if (this.containsDataTypeInOperands(DataTypeName.DOUBLE, leftOperandType, rightOperandType)) {
				return DataTypeName.DOUBLE;
			} else {
				return DataTypeName.FLOAT;
			}
		}

		// 浮動小数点数と整数の混合は浮動小数点数
		if (DataTypeName.isDataTypeNameOf(DataType.FLOAT64,leftOperandType)
				&& DataTypeName.isDataTypeNameOf(DataType.INT64,rightOperandType) ) {
			if (this.containsDataTypeInOperands(DataTypeName.DOUBLE, leftOperandType, rightOperandType)) {
				return DataTypeName.DOUBLE;
			} else {
				return DataTypeName.FLOAT;
			}
		}

		throw new VnanoException(
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
	 * @param lineNumber 対象処理が記述された行番号（例外発生時のエラー情報に使用）
	 * @return 演算子の演算実行データ型の名前
	 * @throws VnanoException 対象演算子に対して使用できないデータ型であった場合にスローされます。
	 */
	private String analyzeLogicalBinaryOperatorDataType(
			String leftOperandType, String rightOperandType, String operatorSymbol,
			String fileName, int lineNumber) throws VnanoException {

		if (DataTypeName.isDataTypeNameOf(DataType.BOOL,leftOperandType)
				&& DataTypeName.isDataTypeNameOf(DataType.BOOL,rightOperandType) ) {
			return DataTypeName.BOOL;
		}

		throw new VnanoException(
			ErrorType.INVALID_DATA_TYPES_FOR_BINARY_OPERATOR,
			new String[] {operatorSymbol, leftOperandType, rightOperandType},
			fileName, lineNumber
		);
	}


	/**
	 * 算術(Arithmetic)、比較(Comparison)、論理(Logical)二項演算子のオペランドの配列次元数を解析し、
	 * 演算結果の配列次元数を決定して返します。
	 *
	 * 演算結果の配列次元数は、スカラ同士の演算では 0 (スカラの次元数) となります。
	 * 配列同士の演算では、両者の次元数が等しい必要があり、結果はその配列の次元数となります
	 * （両者の次元数が等しくない場合はエラーとなります）。
	 * また、スカラと配列の演算では、結果は配列の次元数となります。
	 *
	 * @param leftOperandRank 左オペランドの配列次元数
	 * @param rightOperandRank 右オペランドの配列次元数
	 * @param operatorSymbol 演算子の記号
	 * @param fileName 対象処理が記述されたファイル名（例外発生時のエラー情報に使用）
	 * @param lineNumber 対象処理が記述された行番号（例外発生時のエラー情報に使用）
	 * @return 演算子の演算実行データ型の名前
	 * @throws VnanoException 両オペランドの配列次元数が、演算を行えない組み合わせであった場合にスローされます。
	 */
	int analyzeArithmeticComparisonLogicalBinaryOperatorRank(
			int leftOperandRank, int rightOperandRank, String operatorSymbol,
			String fileName, int lineNumber) throws VnanoException {

		// スカラ同士の演算はスカラ(0次元)
		if (leftOperandRank==0 && rightOperandRank==0) {
			return 0;
		}

		// 左オペランドがスカラで右オペランドが配列の場合
		if (leftOperandRank==0 && rightOperandRank!=0) {
			return rightOperandRank;
		}

		// 左オペランドが配列で右オペランドがスカラの場合
		if (leftOperandRank!=0 && rightOperandRank==0) {
			return leftOperandRank;
		}

		// ここまで残るのは配列同士の演算
		if (leftOperandRank == rightOperandRank) {
			return leftOperandRank;

		// 次元数が等しくない配列同士の演算はエラーとする
		} else {
			throw new VnanoException(
				ErrorType.INVALID_RANKS_FOR_VECTOR_OPERATION,
				new String[] {operatorSymbol}, fileName, lineNumber
			);
		}
	}


	/**
	 * 複合代入演算子のオペランドの配列次元数を解析し、演算結果の配列次元数を決定して返します。
	 *
	 * 演算結果の配列次元数は、スカラ同士の演算では 0 となります。
	 * 配列同士の演算では、両者の次元数が等しい必要があり、結果はその配列の次元数となります
	 * （両者の次元数が等しくない場合はエラーとなります）。
	 * また、左オペランドが配列で右オペランドがスカラの場合は、結果は配列の次元数となります。
	 * 逆に、左オペランドがスカラで右オペランドが配列の場合は、
	 * 演算結果が配列となり、スカラに再代入できないため、エラーとなります。
	 *
	 * @param leftOperandRank 左オペランドの配列次元数
	 * @param rightOperandRank 右オペランドの配列次元数
	 * @param operatorSymbol 演算子の記号
	 * @param fileName 対象処理が記述されたファイル名（例外発生時のエラー情報に使用）
	 * @param lineNumber 対象処理が記述された行番号（例外発生時のエラー情報に使用）
	 * @return 演算子の演算実行データ型の名前
	 * @throws VnanoException 両オペランドの配列次元数が、演算を行えない組み合わせであった場合にスローされます。
	 */
	int analyzeCompoundAssignmentOperatorRank(
			int leftOperandRank, int rightOperandRank, String operatorSymbol,
			String fileName, int lineNumber) throws VnanoException {

		// スカラ同士の演算はスカラ(0次元)
		if (leftOperandRank==0 && rightOperandRank==0) {
			return 0;
		}

		// 左オペランドがスカラで右オペランドが配列の場合は、
		// 演算時にスカラがベクトルに昇格されるためベクトル演算自体は可能だが、
		// 左辺のスカラへの再代入を行えないため、エラーとする必要がある
		if (leftOperandRank==0 && rightOperandRank!=0) {
			throw new VnanoException(
				ErrorType.INVALID_COMPOUND_ASSIGNMENT_BETWEEN_SCALAR_AND_ARRAY,
				new String[] {operatorSymbol}, fileName, lineNumber
			);
		}

		// 左オペランドが配列で右オペランドがスカラの場合
		if (leftOperandRank!=0 && rightOperandRank==0) {
			return leftOperandRank;
		}

		// ここまで残るのは配列同士の演算
		if (leftOperandRank == rightOperandRank) {
			return leftOperandRank;

		// 次元数が等しくない配列同士の演算はエラーとする
		//（代入以前にベクトル演算を行えないため、エラー種類はベクトル演算のエラー）
		} else {
			throw new VnanoException(
				ErrorType.INVALID_RANKS_FOR_VECTOR_OPERATION,
				new String[] {operatorSymbol}, fileName, lineNumber
			);
		}
	}


	/**
	 * 引数に渡されたAST（抽象構文木）の内容を解析し、その中の関数ノードの属性値を検査します。
	 *
	 * 例えば、{@link Parser Parser} による構文解析の段階では、引数の識別子は省略が許されています。
	 * （外部関数の接続などにおいて、そのようなコールシグネチャをパースする用途などがあるためです。）
	 * 一方で、スクリプト内でシグネチャ宣言と同時に実装が定義されている関数では、
	 * 引数の識別子の省略は許されず、実際にこのメソッド内で検査されます。
	 *
	 * @param astRootNode 検査対象のASTのルートノード
	 * @throws VnanoException 属性値に異常があった場合にスローされます。
	 */
	private void checkFunctionAttributes(AstNode astRootNode) throws VnanoException {

		// ASTノードを、帰りがけ順の深さ優先走査で辿り、関数ノードがあれば検査
		AstNode currentNode = astRootNode.getPostorderDftFirstNode();
		while(currentNode != astRootNode) {

			// 関数ノードの場合
			if(currentNode.getType() == AstNode.Type.FUNCTION) {

				// 全ての子ノードを走査して検査
				AstNode[] childNodes = currentNode.getChildNodes();
				for (AstNode childNode: childNodes) {

					// 変数ノード以外は引数ではないのでスキップ
					if (childNode.getType() != AstNode.Type.VARIABLE) {
						continue;
					}

					// 以下は引数の変数ノードに対する検査

					// 識別子属性が無ければエラーにする
					if (!childNode.hasAttribute(AttributeKey.IDENTIFIER_VALUE)) {

						throw new VnanoException(
							ErrorType.NO_IDENTIFIER_IN_VARIABLE_DECLARATION,
							childNode.getFileName(), childNode.getLineNumber()
						);
					}
				} // 全ての子ノードに対する検査

			} // 関数ノードの場合

			currentNode = currentNode.getPostorderDftNextNode();
		} // ASTを辿るループ
	}


	/**
	 * 引数に渡されたAST（抽象構文木）の内容を解析し、その中の代入を伴う演算（インクリメント含む）
	 * ノードの代入対象が、書き換え可能なものかどうか検査します。
	 *
	 * @param astRootNode 検査対象のASTのルートノード
	 * @throws VnanoException 書き換え不可能なものへの代入が検出された場合にスローされます。
	 */
	private void checkAssignmentTargetWritabilities(AstNode astRootNode) throws VnanoException {

		// ASTノードを、帰りがけ順の深さ優先走査で辿り、演算子ノードがあれば検査
		AstNode currentNode = astRootNode.getPostorderDftFirstNode();
		while(currentNode != astRootNode) {

			// 演算子ノードの場合
			if(currentNode.getType() == AstNode.Type.OPERATOR) {

				String execType = currentNode.getAttribute(AttributeKey.OPERATOR_EXECUTOR);
				String symbol = currentNode.getAttribute(AttributeKey.OPERATOR_SYMBOL);

				// 検査対象となる演算子ノードの場合は、左辺ノードを検査
				switch (execType) {

					// 代入演算子の場合
					case AttributeValue.ASSIGNMENT : {
						this.checkWritability( currentNode.getChildNodes()[0] );
						break;
					}
					// 複合代入演算子の場合
					case AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT : {
						this.checkWritability( currentNode.getChildNodes()[0] );
						break;
					}
					// インクリメント/デクリメント演算子の場合
					case AttributeValue.ARITHMETIC : {
						if (symbol.equals(ScriptWord.INCREMENT) || symbol.equals(ScriptWord.DECREMENT)) {
							this.checkWritability( currentNode.getChildNodes()[0] );
						}
						break;
					}
					default : {
						break;
					}
				}

			} // 演算子ノードの場合

			currentNode = currentNode.getPostorderDftNextNode();
		} // ASTを辿るループ
	}


	/**
	 * 引数に渡されたASTノードが、書き換え可能なものかどうか検査します。
	 *
	 * @param node 検査対象のASTノード
	 * @throws VnanoException 検査対象が書き換え不可能であった場合にスローされます。
	 */
	private void checkWritability (AstNode node) throws VnanoException {
		String fileName = node.getFileName();
		int lineNumber = node.getLineNumber();

		// 配列アクセス演算子の場合は、アクセス対象の配列の検査に置き換える
		if(node.getType() == AstNode.Type.OPERATOR
			&& node.getAttribute(AttributeKey.OPERATOR_EXECUTOR).equals(AttributeValue.SUBSCRIPT) ) {

			node = node.getChildNodes()[0];
		}

		// リーフ以外は書き換え不可能なのでエラー
		if (node.getType() != AstNode.Type.LEAF) {
			throw new VnanoException(ErrorType.WRITING_TO_UNWRITABLE_SOMETHING, fileName, lineNumber);
		}

		String leafType = node.getAttribute(AttributeKey.LEAF_TYPE);

		// 変数は現状では const をサポートしていないため、常に許可
		if (leafType.equals(AttributeValue.VARIABLE_IDENTIFIER)) {

			// const をサポートした際はここで検査

		// リテラルは書き換え不可能なのでエラー
		} else if (leafType.equals(AttributeValue.LITERAL)) {
			String[] errorWords = { node.getAttribute(AttributeKey.LITERAL_VALUE) };
			throw new VnanoException(ErrorType.WRITING_TO_UNWRITABLE_SOMETHING, errorWords, fileName, lineNumber);

		// それ以外は、現状の仕様では全て書き換え不可能なため、常にエラー
		} else {
			throw new VnanoException(ErrorType.WRITING_TO_UNWRITABLE_SOMETHING, fileName, lineNumber);
		}
	}


	/**
	 * 引数に渡されたAST（抽象構文木）の内容を解析し、その中の配列アクセス対象が、
	 * 配列かどうか、配列の場合は次元は一致するかどうかを検査します。
	 *
	 * @param astRootNode 検査対象のASTのルートノード
	 * @throws VnanoException 正しくない配列アクセス対象が検出された場合にスローされます。
	 */
	private void checkSubscriptTargetSubscriptabilities(AstNode astRootNode) throws VnanoException {

		// ASTノードを、帰りがけ順の深さ優先走査で辿り、演算子ノードがあれば検査
		AstNode currentNode = astRootNode.getPostorderDftFirstNode();
		while(currentNode != astRootNode) {

			// 配列アクセス演算子ノードの場合
			if(currentNode.getType() == AstNode.Type.OPERATOR
					&& currentNode.getAttribute(AttributeKey.OPERATOR_EXECUTOR).equals(AttributeValue.SUBSCRIPT)) {

				String fileName = currentNode.getFileName();
				int lineNumber = currentNode.getLineNumber();

				// アクセス対象のノードを取得して検査
				AstNode accessingNode = currentNode.getChildNodes()[0];

				// リーフノードでなければエラー
				if (accessingNode.getType() != AstNode.Type.LEAF) {
					throw new VnanoException(ErrorType.SUBSCRIPTING_TO_UNSUBSCRIPTABLE_SOMETHING, fileName, lineNumber);
				}

				// 変数でなければエラー
				if (!accessingNode.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.VARIABLE_IDENTIFIER)) {
					String[] errorWords = null;
					if (accessingNode.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.LITERAL)) {
						errorWords = new String[]{ accessingNode.getAttribute(AttributeKey.LITERAL_VALUE) };
					}
					throw new VnanoException(ErrorType.SUBSCRIPTING_TO_UNSUBSCRIPTABLE_SOMETHING, errorWords, fileName, lineNumber);
				}

				// スカラ変数の場合はエラー
				if (accessingNode.getRank() == 0) {
					String[] errorWords = { accessingNode.getAttribute(AttributeKey.IDENTIFIER_VALUE) };
					throw new VnanoException(
						ErrorType.SUBSCRIPTING_TO_UNSUBSCRIPTABLE_SOMETHING, errorWords, fileName, lineNumber
					);
				}

				// 配列変数の場合は、配列の次元数と、アクセス演算子のインデックスの数が一致しなければエラー
				int numIndices = currentNode.getChildNodes().length-1;
				if (accessingNode.getRank() != numIndices) {
					String[] errorWords = {
						accessingNode.getAttribute(AttributeKey.IDENTIFIER_VALUE),
						Integer.toString(accessingNode.getRank()),
						Integer.toString(numIndices),
					};
					throw new VnanoException(ErrorType.INVALID_SUBSCRIPT_RANK, errorWords, fileName, lineNumber);
				}

			} // 配列アクセス演算子ノードの場合

			currentNode = currentNode.getPostorderDftNextNode();
		} // ASTを辿るループ
	}


	/**
	 * 引数に渡されたAST（抽象構文木）の内容を解析し、その中の識別子が有効かどうかを検査します。
	 *
	 * @param astRootNode 検査対象のASTのルートノード
	 * @throws VnanoException 無効な識別子が検出された場合にスローされます。
	 */
	private void checkIdentifiers(AstNode astRootNode) throws VnanoException {

		// ASTノードを、帰りがけ順の深さ優先走査で辿り、演算子ノードがあれば検査
		AstNode currentNode = astRootNode.getPostorderDftFirstNode();
		while(currentNode != astRootNode) {

			// 変数/関数宣言ノードの場合に、宣言されている識別子を検査する
			if(currentNode.getType() == AstNode.Type.VARIABLE || currentNode.getType() == AstNode.Type.FUNCTION) {

				String fileName = currentNode.getFileName();
				int lineNumber = currentNode.getLineNumber();
				String identifier = currentNode.getAttribute(AttributeKey.IDENTIFIER_VALUE);

				// 数字で始まったり記号を含むなど、構文上のルールに引っかかる場合はエラー
				if (!IdentifierSyntax.isValidSyntaxIdentifier(identifier)) {
					throw new VnanoException(ErrorType.INVALID_IDENTIFIER_SYNTAX, identifier, fileName, lineNumber);
				}

				// 予約語の場合はエラー
				if (ScriptWord.RESERVED_WORD_SET.contains(identifier)) {
					throw new VnanoException(ErrorType.IDENTIFIER_IS_RESERVED_WORD, identifier, fileName, lineNumber);
				}
			}

			currentNode = currentNode.getPostorderDftNextNode();
		} // ASTを辿るループ
	}


	/**
	 * 引数に渡されたAST（抽象構文木）の内容を解析し、関数内の return 文の対象が、
	 * 関数の戻り値の型と一致しているかどうかを検査します。
	 * また、return 文が関数外で使用されていないかどうかも検査します。
	 *
	 * @param astRootNode 検査対象のASTのルートノード
	 * @throws VnanoException 正しくない return 文が検出された場合にスローされます。
	 */
	private void checkReturnValueTypesAndLocations(AstNode astRootNode) throws VnanoException {

		// 現在検査中の関数の戻り値情報を控える
		String currentFunctionReturType = "";
		int currentFunctionReturnRank = -1;

		// ブロック深度が非 0 から 0 になった箇所を検出するため、直前のノードのブロック深度を控える
		int lastBlockDepth = 0;

		// 関数内を辿っている最中は true にする
		boolean inFunction = false;

		// ASTノードを、行がけ順の深さ優先走査で辿って検査していく
		AstNode currentNode = astRootNode;
		do {
			currentNode = currentNode.getPreorderDftNextNode();

			// ブロック深度が非 0 から 0 になった場合: 関数の終端の可能性があるため、控えている関数情報をリセットする
			// ( 関数以外のブロックの可能性もあるが、深度 0 になった時点で関数外なのは確実なので、リセットして問題ない )
			if (lastBlockDepth != 0 && currentNode.getBlockDepth() == 0) {
				currentFunctionReturType = "";
				currentFunctionReturnRank = -1;
				inFunction = false;
			}

			// 関数宣言ノードの場合: 戻り値の型を控える
			if (currentNode.getType() == AstNode.Type.FUNCTION) {
				currentFunctionReturType = currentNode.getDataTypeName();
				currentFunctionReturnRank = currentNode.getRank();
				inFunction = true;
			}

			// return 文の場合: 戻り値の型を検査
			if (currentNode.getType() == AstNode.Type.RETURN) {

				// 関数の中に無い場合はその時点でエラー
				if (!inFunction) {
					throw new VnanoException(ErrorType.RETURN_STATEMENT_IS_OUTSIDE_FUNCTIONS);
				}

				// return 文に戻り値が指定されている場合: 型を検査
				if (currentNode.hasChildNodes()) {
					AstNode returnedValueNode = currentNode.getChildNodes()[0];

					// 型や次元が一致しない場合はエラー
					if (!returnedValueNode.getDataTypeName().equals(currentFunctionReturType)
						|| returnedValueNode.getRank() != currentFunctionReturnRank) {

						// エラーメッセージに用いる型情報を用意し、それを持たせて例外を投げる
						String returnedTypeDescription = returnedValueNode.getDataTypeName();
						for (int dim=0; dim<returnedValueNode.getRank(); dim++) {
							returnedTypeDescription += ScriptWord.SUBSCRIPT_BEGIN + ScriptWord.SUBSCRIPT_END;
						}
						String expectedTypeDescription = currentFunctionReturType;
						for (int dim=0; dim<currentFunctionReturnRank; dim++) {
							expectedTypeDescription += ScriptWord.SUBSCRIPT_BEGIN + ScriptWord.SUBSCRIPT_END;
						}
						String[] errorWords = {returnedTypeDescription, expectedTypeDescription};
						throw new VnanoException(
							ErrorType.INVALID_RETURNED_VALUE_DATA_TYPE, errorWords,
							currentNode.getFileName(), currentNode.getLineNumber()
						);
					}

				// return 文に戻り値が指定されている場合: void 型関数でなければエラーにする
				} else if (!currentFunctionReturType.equals(DataTypeName.VOID)) {
					throw new VnanoException(
						ErrorType.RETURNED_VALUE_IS_MISSING, currentNode.getFileName(), currentNode.getLineNumber()
					);
				}
			}

			lastBlockDepth = currentNode.getBlockDepth();
		} while (!currentNode.isPreorderDftLastNode());
	}

}
