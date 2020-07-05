/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.OperatorPrecedence;
import org.vcssl.nano.spec.ScriptWord;
import org.vcssl.nano.spec.LanguageSpecContainer;
import org.vcssl.nano.spec.ErrorType;

//Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/compiler/Parser.html
//ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/compiler/Parser.html

/**
 * <p>
 * <span class="lang-en">
 * The class performing the function of the parser in the compiler of the Vnano
 * </span>
 * <span class="lang-ja">
 * Vnano のコンパイラ内において, パーサ（構文解析器）の機能を担うクラスです
 * </span>
 * .
 * </p>
 *
 * <p>
 * <span class="lang-en">
 * The processing of this class takes tokens as input,
 * then constructs the AST (Abstract Syntax Tree) and outputs it.
 * </span>
 * <span class="lang-ja">
 * このクラスが行う構文解析処理は, 入力としてトークン列を受け取り,
 * AST（抽象構文木）を構築して出力します.
 * </span>
 * </p>
 *
 * <p>
 * &raquo; <a href="../../../../../src/org/vcssl/nano/compiler/Parser.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/compiler/Parser.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/compiler/Parser.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class Parser {

	/** 各種の言語仕様設定類を格納するコンテナを保持します。 */
	private final LanguageSpecContainer LANG_SPEC;

	/** 上記コンテナ内の、スクリプト言語の語句が定義された設定オブジェクトを保持します。 */
	private final ScriptWord SCRIPT_WORD;

	/** 上記コンテナ内の、データ型名が定義された設定オブジェクトを保持します。 */
	private final DataTypeName DATA_TYPE_NAME;

	/** 上記コンテナ内の、演算子の優先度が定義された設定オブジェクトを保持します。 */
	private final OperatorPrecedence OPERATOR_PRECEDENCE;


	/**
	 * <span class="lang-ja">スカラの配列次元数です</span>
	 * <span class="lang-en">The array-rank of the scalar</span>
	 * .
	 */
	private static final int RANK_OF_SCALAR = 0;


	/**
	 * <span class="lang-en">
	 * Create a new parser with the specified language specification settings
	 * </span>
	 * <span class="lang-ja">
	 * 指定された言語仕様設定で, パーサを生成します
	 * </span>
	 * .
	 * @param langSpec
	 *   <span class="lang-en">language specification settings.</span>
	 *   <span class="lang-ja">言語仕様設定.</span>
	 */
	public Parser(LanguageSpecContainer langSpec) {
		this.LANG_SPEC = langSpec;
		this.SCRIPT_WORD = langSpec.SCRIPT_WORD;
		this.DATA_TYPE_NAME = langSpec.DATA_TYPE_NAME;
		this.OPERATOR_PRECEDENCE = langSpec.OPERATOR_PRECEDENCE;
	}


	/**
	 * <span class="lang-en">Constructs and returns the AST by parsing tokens</span>
	 * <span class="lang-ja">トークン列を構文解析し, ASTを構築して返します</span>
	 * .
	 * @param tokens
	 *   <span class="lang-en">Tokens to be parsed.</span>
	 *   <span class="lang-ja">解析対象のトークン列.</span>
	 *
	 * @return
	 *   <span class="lang-en">The constructed AST.</span>
	 *   <span class="lang-ja">構築されたAST.</span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">Thrown when any syntax error has detected.</span>
	 *   <span class="lang-ja">構文エラーが検出された場合にスローされます.</span>
	 */
	public AstNode parse(Token[] tokens) throws VnanoException {

		LexicalChecker lexicalChecker = new LexicalChecker(LANG_SPEC);

		// パース作業用のスタックとして使用する双方向キュー
		Deque<AstNode> statementStack = new ArrayDeque<AstNode>();

		int tokenLength = tokens.length; // トークンの総数
		int statementBegin = 0; // 文の始点のインデックスを格納する

		while(statementBegin < tokenLength) {

			// 文の終端（文末記号）と、次のブロック始点・終端のインデックスを取得
			int statementEnd = Token.getIndexOf(tokens, SCRIPT_WORD.endOfStatement, statementBegin);
			int blockBegin = Token.getIndexOf(tokens, SCRIPT_WORD.blockBegin, statementBegin);
			int blockEnd = Token.getIndexOf(tokens, SCRIPT_WORD.blockEnd, statementBegin);

			Token beginToken = tokens[statementBegin];

			// 文末記号が無い場合のエラー（3つめの条件は、ブロック終端後に文が無い場合のため）
			if (statementEnd < 0 && blockBegin < 0 && statementBegin!=blockEnd) {
				throw new VnanoException(
						ErrorType.STATEMENT_END_IS_NOT_FOUND, beginToken.getFileName(), beginToken.getLineNumber()
				);
			}

			// 空文（内容が無い文）の場合
			if (statementBegin == statementEnd) {
				AstNode emptyStatementNode = new AstNode(
						AstNode.Type.EMPTY, tokens[statementBegin].getLineNumber(), beginToken.getFileName()
				);
				statementStack.push(emptyStatementNode);
				statementBegin++;

			// ブロック文の始点 or 終点の場合
			} else if (beginToken.getType()==Token.Type.BLOCK) {

				// ブロック始点 -> スタックに目印のフタをつめる（第二引数は目印とするマーカー）
				if (beginToken.getValue().equals(SCRIPT_WORD.blockBegin)) {
					this.pushLid(statementStack);
					statementBegin++;

				// ブロック終点
				} else {

					// 目印のフタの位置まで、スタックから文のノード（=ブロックの中身の文）を全て回収
					AstNode[] statementsInBlock = this.popStatementNodes(statementStack);

					// ブロック文ノードを生成し、上で取り出した文のノードを全てぶら下げる
					AstNode blockNode = new AstNode(
						AstNode.Type.BLOCK, beginToken.getLineNumber(), beginToken.getFileName()
					);
					for (AstNode statementNode: statementsInBlock) {
						blockNode.addChildNode(statementNode);
					}

					// ブロック文ノードをプッシュ
					statementStack.push(blockNode);
					statementBegin++;
				}

			// 制御文の場合
			} else if (beginToken.getType()==Token.Type.CONTROL) {
				String word = beginToken.getValue();

				// if / for / while文 (この処理系では直後にブロックが必須)
				if (word.equals(SCRIPT_WORD.ifStatement) || word.equals(SCRIPT_WORD.forStatement) || word.equals(SCRIPT_WORD.whileStatement)) {

					// 括弧 (...) やブロック {...} が存在するか確認（この言語ではif/else/for/whileのブロックは必須）
					lexicalChecker.checkTokensAfterControlStatement(tokens, statementBegin, word.equals(SCRIPT_WORD.forStatement)); // 最後の引数は、括弧内に文末記号を許すかどうか

					Token[] subTokens = Arrays.copyOfRange(tokens, statementBegin, blockBegin);
					statementStack.push(this.parseControlStatement(subTokens));
					statementBegin = blockBegin;

				// else文
				} else if (beginToken.getValue().equals(SCRIPT_WORD.elseStatement)) {

					// ブロック {...} が存在するか確認（この言語ではif/else/for/whileのブロックは必須）
					lexicalChecker.checkTokensAfterControlStatement(tokens, statementBegin, false); // 最後の引数は、括弧内に文末記号を許すかどうか

					Token[] subTokens = Arrays.copyOfRange(tokens, statementBegin, statementBegin+1);
					statementStack.push(this.parseControlStatement(subTokens));
					statementBegin++;

				// break / continue / return 文など
				} else {
					Token[] subTokens = Arrays.copyOfRange(tokens, statementBegin, statementEnd);
					statementStack.push(this.parseControlStatement(subTokens));
					statementBegin = statementEnd + 1;
				}

			// 関数宣言文の場合）
			} else if (this.startsWithFunctionDeclarationTokens(tokens, statementBegin)) {

				//LexicalChecker.checkTokensAfterFunctionDeclarationStatement(tokens, statementBegin, false);
				Token[] subTokens = Arrays.copyOfRange(tokens, statementBegin, blockBegin);
				statementStack.push(this.parseFunctionDeclarationStatement(subTokens));
				statementBegin = blockBegin;

			// 変数宣言文の場合
			} else if (beginToken.getType()==Token.Type.DATA_TYPE || beginToken.getType()==Token.Type.MODIFIER) {

				Token[] subTokens = Arrays.copyOfRange(tokens, statementBegin, statementEnd);
				statementStack.push(this.parseVariableDeclarationStatement(subTokens, true));
				statementBegin = statementEnd + 1;

			// 式文の場合
			} else {
				Token[] subTokens = Arrays.copyOfRange(tokens, statementBegin, statementEnd);
				statementStack.push(this.parseExpression(subTokens));
				statementBegin = statementEnd + 1;
			}
		}

		// ルートノードの保持情報用にファイル名と行番号を用意（空のスクリプトなどではトークンが全く無いため、個数検査が必要）
		String fileName = null;
		int lineNumber = -1;
		if (tokens.length != 0) {
			fileName = tokens[0].getFileName();
			lineNumber = tokens[0].getLineNumber();
		}

		// ルートノードを生成し、最上階層のノードを全てぶら下げる(スタックに積まれている順序に注意)
		AstNode rootNode = new AstNode(AstNode.Type.ROOT, lineNumber, fileName);
		while (statementStack.size() != 0) {
			rootNode.addChildNode(statementStack.pollLast());
		}

		// AST内の各ノードに、ルートノードを深度 0 とする深度情報を設定
		rootNode.updateDepths();

		return rootNode;
	}





	// ====================================================================================================
	// Parsing of Expressions
	// 式の構文解析関連
	// ====================================================================================================


	/**
	 * 式のトークン配列を解析し、AST（抽象構文木）を構築して返します。
	 *
	 * @param tokens 式のトークン配列
	 * @return 構築したAST（抽象構文木）のルートノード
	 * @throws VnanoException 式の構文に異常があった場合にスローされます。
	 */
	private AstNode parseExpression(Token[] tokens) throws VnanoException {

		// 最初に、トークンの種類や括弧の数などに、式の構成トークンとして問題無いか検査
		new LexicalChecker(LANG_SPEC).checkTokensInExpression(tokens);

		// キャスト演算子は複数トークンから成る特別な前置演算子なので、先に単一トークンの前置演算子に変換する
		tokens = preprocessCastSequentialTokens(tokens);

		int tokenLength = tokens.length; // トークン数
		int readingIndex = 0; // 注目トークンのインデックス

		// パース作業用のスタックとして使用する双方向キューを用意
		Deque<AstNode> stack = new ArrayDeque<AstNode>();

		// トークン配列をスキャンし、個々のトークンの次（右側の最も近く）にある演算子の優先度を配列に格納（結合の判断で使用）
		int[] nextOperatorPriorities = this.getNextOperatorPrecedence(tokens);

		// トークンを左から順に末尾まで読み進むループ
		do {
			AstNode operatorNode = null; // 生成した演算子ノードを控える

			Token readingToken = tokens[readingIndex];                 // このループでの読み込み対象トークン
			int readingOpPrecedence = readingToken.getPrecedence();        // 読み込み対象トークンの演算子優先度
			int nextOpPrecedence = nextOperatorPriorities[readingIndex]; // 読み込み対象トークンの後方（右側）で最初にある演算子の優先度
			String readingOpAssociativity = readingToken.getAttribute(AttributeKey.OPERATOR_ASSOCIATIVITY); // 演算子の結合性（右/左）

			// 識別子やリテラルなどのリーフ（末端オペランド）ノードの場合 -> スタックにプッシュ
			if (readingToken.getType() == Token.Type.LEAF) {

				stack.push(this.createLeafNode(readingToken));
				readingIndex++;
				continue;

			// 括弧の場合
			} else if (readingToken.getType()==Token.Type.PARENTHESIS) {

				// 開き括弧の場合、部分式の境界前後が結合しないよう、スタックに非演算子のフタをつめる（第二引数は回収時の目印）
				if (readingToken.getValue().equals(SCRIPT_WORD.parenthesisBegin)) {
					this.pushLid(stack, AttributeValue.PARTIAL_EXPRESSION);
					readingIndex++;
					continue;

				// 閉じ括弧の場合、スタックから構築済みの部分式構文木ノード（括弧の場合は1個しか無いはず）を取り出し、フタも除去
				} else {
					operatorNode = this.popPartialExpressionNodes(stack, AttributeValue.PARTIAL_EXPRESSION)[0];
				}

			// 演算子ノードの場合
			} else if (readingToken.getType() == Token.Type.OPERATOR) {

				// 演算子ノードを生成し、演算子の（構文的な）種類ごとに分岐
				operatorNode = this.createOperatorNode(readingToken);
				switch (readingToken.getAttribute(AttributeKey.OPERATOR_SYNTAX)) {

					// 後置演算子
					case AttributeValue.POSTFIX : {

						// スタックに左オペランドノードが積まれているので取り出してぶら下げる
						// ※ 後置演算子はトークンの並び的に左結合しかありえない上に、必ず直前のものに結合するので、単純に処理
						operatorNode.addChildNode(stack.pop());
						break;
					}

					// 前置演算子
					case AttributeValue.PREFIX : {

						// 優先度が次の演算子よりも強い場合、右トークンを先読みし、リーフノードとして演算子ノードにぶら下げる
						if (this.shouldAddRightOperand(readingOpAssociativity, readingOpPrecedence, nextOpPrecedence)) {
							operatorNode.addChildNode( this.createLeafNode(tokens[readingIndex+1]) );
							readingIndex++; // 次のトークンは先読みして処理を終えたので1つ余分に進める
						}
						break;
					}

					// 二項演算子
					case AttributeValue.BINARY : {

						// スタックに左オペランドノードが積まれているので取り出してぶら下げる
						operatorNode.addChildNode(stack.pop());

						// 優先度が次の演算子よりも強い場合、右トークンを先読みし、リーフノードとして演算子ノードにぶら下げる
						if (this.shouldAddRightOperand(readingOpAssociativity, readingOpPrecedence, nextOpPrecedence)) {
							operatorNode.addChildNode( this.createLeafNode(tokens[readingIndex+1]) );
							readingIndex++; // 次のトークンは先読みして処理を終えたので1つ余分に進める
						}
						break;
					}

					// マルチオペランド演算子の始点（関数呼び出しの「 ( 」や配列参照の「 [ 」）
					case AttributeValue.MULTIARY : {

						// スタックに識別子ノードが積まれているので、取り出して演算子ノードにぶら下げ、それをプッシュする
						operatorNode.addChildNode(stack.pop());
						stack.push(operatorNode);

						// 引数部分式の境界前後が結合しないよう、スタックに非演算子のフタをつめる（第二引数は回収時の目印）
						this.pushLid(stack, readingToken.getAttribute(AttributeKey.OPERATOR_EXECUTOR));
						readingIndex++;
						continue;
					}

					// マルチオペランド演算子のセパレータ（関数呼び出しの「 , 」や多次元配列参照の「 ][ 」）
					case AttributeValue.MULTIARY_SEPARATOR : {

						// 引数部分式の境界前後が結合しないよう、スタックに非演算子のフタをつめる
						this.pushLid(stack);
						readingIndex++;
						continue;
					}

					// マルチオペランド演算子の終点（関数呼び出しの「 ) 」や配列参照の「 ] 」）
					case AttributeValue.MULTIARY_END : {

						// スタックからの引数ノードを全て回収する（フタ区切りで独立構文木として形成されている）
						AstNode[] argumentNodes = this.popPartialExpressionNodes(
								stack, readingToken.getAttribute(AttributeKey.OPERATOR_EXECUTOR)
						);

						// スタックから呼び出し演算子ノードを取り出し、引数ノードをぶら下げる
						operatorNode = stack.pop();
						operatorNode.addChildNodes(argumentNodes);
						break;
					}

					// ここに到達するのはLexicalAnalyzerの異常（不明な種類の演算子構文種類）
					default : {
						throw new VnanoFatalException("Unknown operator syntax");
					}
				}

			// ここに到達するのはLexicalAnalyzerの異常（不明な種類のトークン）
			} else {
				throw new VnanoFatalException("Unknown token type");
			}

			// 次に出現する演算子よりも、スタック上の演算子の方が高優先度の場合、スタック上の演算子において必要な子ノード連結を全て済ませる
			while (this.shouldAddRightOperandToStackedOperator(stack, nextOperatorPriorities[readingIndex])) {
				stack.peek().addChildNode(operatorNode);
				operatorNode = stack.pop();
			}

			stack.push(operatorNode);
			readingIndex++;

		// トークンを左から順に末尾まで読み進むループ
		} while (readingIndex < tokenLength);

		// スタックの一番奥（双方向キューの先頭）が構文木のルートノードなので、それを式ノードにぶら下げて返す
		AstNode expressionNode = new AstNode(AstNode.Type.EXPRESSION, tokens[0].getLineNumber(), tokens[0].getFileName());
		expressionNode.addChildNode(stack.peekFirst());
		return expressionNode;
	}


	/**
	 * {@link Parser#parseExpression parseExpression} メソッド内で、
	 * 演算子の右オペランドをすぐにぶら下げるか、それとも後方のトークン列の構文解析が終わった後にすべきかを、
	 * 演算子の優先度や結合性を考慮して判定します。
	 *
	 * トークン列を構文解析しながらASTノードを生成・連結していく際、
	 * 演算子と別の演算子の間に存在するオペランド（リーフ、または演算子もあり得ます）が、
	 * どちらの演算子ノードの子要素としてぶら下がるべきかは、優先度や結合性などによって異なります。
	 *
	 * このメソッドでは、注目している演算子と次の演算子の情報に基づいて、
	 * その間にあるオペランドがどちらにぶら下がるべきかを判定します。
	 * 注目している演算子にぶら下がるべき場合に true が返されます。
	 *
	 * @param targetOperatorAssociativity 注目演算子の結合性（右結合や左結合）
	 * @param targetOperatorPrecedence 注目演算子の優先度（数字が小さい方が高優先度）
	 * @param nextOperatorPrecedence 次の演算子の優先度（数字が小さい方が高優先度）
	 * @return 注目演算子にぶら下げるべきなら true
	 */
	private boolean shouldAddRightOperand(
			String targetOperatorAssociativity, int targetOperatorPrecedence, int nextOperatorPrecedence) {

		// 注目演算子の優先度が、次の演算子よりも強い場合に true
		boolean targetOpPrecedenceIsStrong = targetOperatorPrecedence < nextOperatorPrecedence; // ※数字が小さい方が高優先度

		// 注目演算子の優先度が、次の演算子と等しい場合に true
		boolean targetOpPrecedenceIsEqual = targetOperatorPrecedence == nextOperatorPrecedence; // ※数字が小さい方が高優先度

		// 注目演算子が左結合なら true、右結合なら false
		boolean targetOpAssociativityIsLeft = targetOperatorAssociativity.equals(AttributeValue.LEFT);

		// 結果を以下の通りに返す。
		// ・注目演算子の方が次の演算子よりも強い場合は true、弱い場合は false。
		// ・優先度がちょうど等しい場合、注目演算子が左結合であれば true、右結合なら false。
		return targetOpPrecedenceIsStrong || (targetOpPrecedenceIsEqual && targetOpAssociativityIsLeft);
	}


	/**
	 * {@link Parser#parseExpression parseExpression} メソッド内において、
	 * 注目演算子ノードを、現在作業用スタック上の先頭にある演算子ノードの下にぶら下げるべきかどうかを、
	 * 演算子の優先度や結合性を考慮して判定します。
	 *
	 * @param stack 構文解析の作業用スタックとして使用している双方向キュー
	 * @param nextOperatorPrecedence 注目演算子の次（右側の最も近く）にある演算子の優先度
	 * @return ぶら下げるべきなら true
	 */
	private boolean shouldAddRightOperandToStackedOperator(Deque<AstNode> stack, int nextOperatorPrecedence) {

		// スタック上にノードが無い場合や、あっても演算子ではない場合は、その時点でfalse
		if (stack.size() == 0) {
			return false;
		}
		if (stack.peek().getType() != AstNode.Type.OPERATOR) {
			return false;
		}

		// スタック上の演算子の優先度（※数字が小さい方が優先度が高い）
		int stackedOperatorPrecedence = Integer.parseInt(stack.peek().getAttribute(AttributeKey.OPERATOR_PRECEDENCE));

		// スタック上の演算子の結合性（右/左）
		String stackedOperatorAssociativity = stack.peek().getAttribute(AttributeKey.OPERATOR_ASSOCIATIVITY);

		// 次の演算子の優先度などを考慮した上で判断した結果を返す
		return this.shouldAddRightOperand(stackedOperatorAssociativity, stackedOperatorPrecedence, nextOperatorPrecedence);
	}


	/**
	 * トークン配列を解析し、各要素に、次（右）の演算子要素の優先度を設定します。
	 *
	 * @param tokens 解析・設定対象のトークン配列
	 */
	private int[] getNextOperatorPrecedence(Token[] tokens) {

		int length = tokens.length;
		int[] rightOpPrecedences = new int[ length ];

		// 最も右にある演算子は必ず優先になるよう、最小優先度を初期値とする
		int rightOpPrecedence = OPERATOR_PRECEDENCE.leastPrior;

		// 末尾から先頭へ向かって要素を見ていく
		for(int i = length-1; 0 <= i; i--) {

			// i 番の要素に、右の演算子の優先度を設定
			rightOpPrecedences[i] = rightOpPrecedence;

			// i 番の要素が演算子なら、その優先度を新たな右演算子優先度に設定
			if (tokens[i].getType() == Token.Type.OPERATOR) {
				rightOpPrecedence = tokens[i].getPrecedence();
			}

			// 括弧の内部の部分式は外側よりも常に高優先度となるよう、括弧の境界部で調整する
			if (tokens[i].getType() == Token.Type.PARENTHESIS) {

				// 部分式内部が常に優先になるよう、開き括弧 ( では右側演算子優先度を最高値に設定する
				if(tokens[i].getValue().equals(SCRIPT_WORD.parenthesisBegin)){
					rightOpPrecedence = OPERATOR_PRECEDENCE.mostPrior;

				// 閉じ括弧 ) は文末と同じ効果なので、右側演算子優先度を最低に設定する
				} else {
					rightOpPrecedence = OPERATOR_PRECEDENCE.leastPrior;

				}
			}
		}

		return rightOpPrecedences;
	}





	// ====================================================================================================
	// Parsing of Variable Declarations
	// 変数宣言の構文解析関連
	// ====================================================================================================


	/**
	 * 変数宣言文を構成するトークン配列に対して構文解析を行い、AST（抽象構文木）を構築して返します。
	 *
	 * このメソッドが返すASTのルートは、{@link AstNode.Type#VARIABLE VARIABLE} タイプのノードとなります。
	 * また、変数名（識別子）を {@link AttributeKey#IDENTIFIER_VALUE IDENTIFIER} 属性、
	 * データ型を {@link AttributeKey#DATA_TYPE DATA_TYPE} 属性、
	 * 配列次元を {@link AttributeKey#RANK RANK} 属性の値に持ちます。
	 * さらに、初期化式がある場合には、子ノードとして {@link AstNode.Type#EXPRESSION EXPRESSION}
	 * タイプのノードがぶら下がります。
	 * 加えて、変数が配列である場合には、
	 * {@link Parser#parseVariableDeclarationArrayLengths parseVariableDeclarationArrayLengths}
	 * メソッドによって生成される {@link AstNode.Type#LENGTHS LENGTHS} タイプのノードがぶら下がります。
	 *
	 * @param tokens 文のトークン配列（文末記号は含まない）
	 * @param requiresIdentifier 識別子を必須にするかどうか（trueなら必須）
	 * @return 構築したAST（抽象構文木）のルートノード
	 * @throws VnanoException 文の構文に異常があった場合にスローされます。
	 */
	private AstNode parseVariableDeclarationStatement(Token[] tokens, boolean requiresIdentifier)
			throws VnanoException {

		AstNode variableNode = new AstNode(AstNode.Type.VARIABLE, tokens[0].getLineNumber(), tokens[0].getFileName());
		List<String> modifierList = new ArrayList<String>(); // 修飾子を控える
		List<Token> tokenList = new ArrayList<Token>();

		for (Token token: tokens) {
			tokenList.add(token);
		}

		int readingIndex = 0;

		// 型名の前に置かれる修飾子を検出して修飾子リストに追加（あとで型名後方のものとまとめて属性値に持たせる）
		if (tokens[readingIndex].getType() == Token.Type.MODIFIER) {
			if (SCRIPT_WORD.prefixModifierSet.contains(tokens[readingIndex].getValue()) ) {
				modifierList.add(tokens[readingIndex].getValue());
				readingIndex++;
			} else {
				throw new VnanoException(
					ErrorType.POSTFIX_MODIFIER_BEFORE_TYPE_NAME, new String[] { tokens[readingIndex].getValue() },
					tokens[readingIndex].getFileName(), tokens[readingIndex].getLineNumber()
				);
			}
		}

		// 型情報を読んで付加
		if (readingIndex < tokens.length) {
			Token typeToken = tokens[readingIndex];
			variableNode.setAttribute(AttributeKey.DATA_TYPE, typeToken.getValue());
			readingIndex++;
		} else {
			throw new VnanoException(
				ErrorType.NO_DATA_TYPE_IN_VARIABLE_DECLARATION,
				tokens[readingIndex-1].getFileName(), tokens[readingIndex-1].getLineNumber()
			);
		}

		// 型名の後に置かれる修飾子を検出して修飾子リストに追加（あとで型名前方のものとまとめて属性値に持たせる）
		if (readingIndex < tokens.length && tokens[readingIndex].getType() == Token.Type.MODIFIER) {
			if (SCRIPT_WORD.postfixModifierSet.contains(tokens[readingIndex].getValue()) ) {
				modifierList.add(tokens[readingIndex].getValue());
				readingIndex++;
			} else {
				throw new VnanoException(
					ErrorType.PREFIX_MODIFIER_AFTER_TYPE_NAME, new String[] { tokens[readingIndex].getValue() },
					tokens[readingIndex].getFileName(), tokens[readingIndex].getLineNumber()
				);
			}
		}

		// 識別子トークンを抽出して控える
		Token nameToken = null;

		// 次にトークンが存在しない場合は、識別子が無いので構文エラー
		//   ただし識別子が省略可能と指定されている場合はエラーにしない（関数シグネチャ内での引数宣言など）
		if (tokens.length <= readingIndex) {
			if (requiresIdentifier) {
				throw new VnanoException(
					ErrorType.NO_IDENTIFIER_IN_VARIABLE_DECLARATION,
					tokens[readingIndex-1].getFileName(), tokens[readingIndex-1].getLineNumber()
				);
			}

		// 次にトークンがあっても、識別子トークンではない場合は構文エラー
		} else if (tokens[readingIndex].getType()!=Token.Type.LEAF
			|| !tokens[readingIndex].getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.VARIABLE_IDENTIFIER)) {

			// 識別子が省略可能と指定されている場合はエラーにしない（関数シグネチャでの引数宣言など）
			if (requiresIdentifier) {
				throw new VnanoException(
					ErrorType.INVALID_IDENTIFIER_TYPE,
					new String[] { tokens[readingIndex].getValue() },
					tokens[readingIndex-1].getFileName(), tokens[readingIndex-1].getLineNumber()
				);
			}

		// 識別子トークンがあるので、識別子情報を付加
		} else {
			nameToken = tokens[readingIndex];
			variableNode.setAttribute(AttributeKey.IDENTIFIER_VALUE, nameToken.getValue());
			readingIndex++;
		}

		// 配列次元数と要素数の検出
		int arrayRank = RANK_OF_SCALAR;
		AstNode arrayLengthNode = null;
		if (readingIndex<tokens.length-1 && tokens[readingIndex].getValue().equals(SCRIPT_WORD.subscriptBegin)) {
			int lengthsEnd = getLengthEndIndex(tokens, readingIndex);
			Token[] lengthsTokens = Arrays.copyOfRange(tokens, readingIndex, lengthsEnd+1);
			arrayRank = this.parseVariableDeclarationArrayRank(lengthsTokens);
			if (RANK_OF_SCALAR < arrayRank) {
				arrayLengthNode = this.parseVariableDeclarationArrayLengths(lengthsTokens);
			}
			readingIndex = lengthsEnd + 1;
		}

		// 配列情報を付加
		variableNode.setAttribute(AttributeKey.RANK, Integer.toString(arrayRank));
		if (arrayLengthNode != null) {
			arrayRank = arrayLengthNode.getChildNodes(AstNode.Type.EXPRESSION).length;
			variableNode.addChildNode(arrayLengthNode);
		}

		// ここ以降は初期化式であるが、識別子が省略されているのに初期化式がある場合はエラー
		if (!requiresIdentifier && readingIndex < tokens.length) {
			throw new VnanoException(
					ErrorType.TOO_MANY_TOKENS_FOR_VARIABLE_DECLARATION,
					tokens[readingIndex-1].getFileName(), tokens[readingIndex-1].getLineNumber()
			);
		}

		// 初期化式のトークン列を、parseExpressionで解釈してASTを構築して付加
		if(readingIndex<tokens.length-1 && tokens[readingIndex].getValue().equals(SCRIPT_WORD.assignment)) {
			int initTokenLength = tokens.length - readingIndex + 1;
			Token[] initTokens = new Token[initTokenLength];
			initTokens[0] = nameToken;
			for (int initTokenIndex=1; initTokenIndex<initTokenLength; initTokenIndex++) {
				initTokens[initTokenIndex] = tokens[readingIndex];
				readingIndex++;
			}
			variableNode.addChildNode(this.parseExpression(initTokens));
		}

		// それ以上トークンが続く場合は、明らかに余計なトークンであるため構文エラー
		if (readingIndex < tokens.length) {
			throw new VnanoException(
					ErrorType.TOO_MANY_TOKENS_FOR_VARIABLE_DECLARATION,
					tokens[readingIndex-1].getFileName(), tokens[readingIndex-1].getLineNumber()
			);
		}

		// 識別子をリストから取り出してノードに持たせる
		for (String modifier: modifierList) {
				variableNode.addModifier(modifier);
		}

		return variableNode;
	}


	/**
	 * 変数宣言文の内で、配列の要素数の宣言部を構成するトークン配列に対して構文解析を行い、
	 * 配列次元数を返します。
	 *
	 * @param tokens 要素数宣言部のトークン配列
	 * @return 配列次元数（任意次元として宣言されていた場合は -1）
	 * @throws VnanoException 文の構文に異常があった場合にスローされます。
	 */
	private int parseVariableDeclarationArrayRank(Token[] tokens) throws VnanoException {
		int tokenLength = tokens.length;

		// 任意次元宣言は表記が「 [...] 」に限られるので、まずそれかどうか検査（その方が後で考慮パターンを減らせる）
		boolean isArbitraryRank = false;
		for(int i=0; i<tokenLength; i++) {
			if (tokens[i].getValue().equals(LANG_SPEC.SCRIPT_WORD.arbitraryCountModifier)) {
				isArbitraryRank = true;
			}
		}
		if (isArbitraryRank) {

			// 構文的に正しい任意次元宣言の場合
			if (tokenLength == 3
					&& tokens[0].getValue().equals(LANG_SPEC.SCRIPT_WORD.subscriptBegin)
					&& tokens[1].getValue().equals(LANG_SPEC.SCRIPT_WORD.arbitraryCountModifier)
					&& tokens[2].getValue().equals(LANG_SPEC.SCRIPT_WORD.subscriptEnd)) {

				return -1; // 任意次元の場合は -1 を返す仕様

			// 構文的に正しくない任意次元宣言の場合
			} else {
				String errorWord = "";
				for(int i=0; i<tokenLength; i++) {
					errorWord += tokens[i].getValue();
				}
				throw new VnanoException(
					ErrorType.INVALID_ARBITRARY_RANK_SYNTAX, errorWord, tokens[0].getFileName(), tokens[0].getLineNumber()
				);
			}
		}

		// 任意次元ではない、一般の場合の次元数カウント
		int rank = 0;
		int depth = 0;
		for(int i=0; i<tokenLength; i++) {
			String word = tokens[i].getValue();

			// 「 [ 」記号
			if(word.equals(SCRIPT_WORD.subscriptBegin)) {

				// 階層が 0 なら要素数宣言の開始なので次元数を加算（それ以外は、要素数の式を構成する配列インデックス演算子のもの）
				if (depth==0) {
					rank++;
				}
				depth++;

			// 「 ][ 」記号
			} else if(word.equals(SCRIPT_WORD.subscriptSeparator)) {

				// 階層が 1 なら次の要素数宣言の次元区切りなので次元数を加算（それ以外は上記コメントの説明と同様）
				if (depth==1) {
					rank++;
				}

			// 「 ] 」記号
			} else if (word.equals(SCRIPT_WORD.subscriptEnd)) {
				// 開き点と閉じ点で両方カウントすると重複カウントになるので、この場合はカウントせず階層を降りるだけ
				depth--;
			}
		}
		return rank;
	}


	/**
	 * 変数宣言文の内で、配列の要素数の宣言部を構成するトークン配列に対して構文解析を行い、
	 * 配列要素数情報の AST（抽象構文木）を構築して返します。
	 *
	 * このメソッドが返すASTのルートは、{@link AstNode.Type#LENGTHS LENGTHS} タイプのノードとなります。
	 * その下には、各次元の要素数の計算式に対応する {@link AstNode.Type#EXPRESSION EXPRESSION}
	 * タイプのノードが、次元の数だけ次元順にぶら下がります。
	 *
	 * @param tokens 要素数宣言部のトークン配列
	 * @return 構築したAST（抽象構文木）のルートノード
	 * @throws VnanoException 文の構文に異常があった場合にスローされます。
	 */
	private AstNode parseVariableDeclarationArrayLengths(Token[] tokens) throws VnanoException {
		AstNode lengthsNode = new AstNode(AstNode.Type.LENGTHS, tokens[0].getLineNumber(), tokens[0].getFileName());
		int currentExprBegin = -1;

		int tokenLength = tokens.length;
		int depth = 0;
		for(int i=0; i<tokenLength; i++) {
			String word = tokens[i].getValue();

			// 「 [ 」記号
			if(word.equals(SCRIPT_WORD.subscriptBegin)) {

				// 階層が 0 なら要素数宣言の開始（それ以外は、要素数の式を構成する配列インデックス演算子のもの）
				if (depth==0) {
					currentExprBegin = i + 1;
				}
				depth++;

			// 「 ] 」記号か「 ][ 」記号
			} else if(word.equals(SCRIPT_WORD.subscriptEnd) || word.equals(SCRIPT_WORD.subscriptSeparator)) {

				// 階層が 1 なら要素数宣言の次元区切り
				if (depth==1) {
					Token[] exprTokens = Arrays.copyOfRange(tokens, currentExprBegin, i);

					// 要素数宣言の内容が省略されている場合は、要素数 0 の宣言と同じものとする（言語仕様）
					if (exprTokens.length == 0) {
						AstNode zeroExprNode = new AstNode(AstNode.Type.EXPRESSION, tokens[i].getLineNumber(), tokens[i].getFileName());
						AstNode zeroLeafNode = this.createLeafNode(
								"0", AttributeValue.LITERAL, DATA_TYPE_NAME.defaultInt, tokens[i].getFileName(), tokens[i].getLineNumber()
						);
						zeroExprNode.addChildNode(zeroLeafNode);
						lengthsNode.addChildNode(zeroExprNode);

					// 省略されていなければ、内容を式として解釈してぶら下げる
					} else {
						lengthsNode.addChildNode( this.parseExpression(exprTokens) );
					}
				}
				if (word.equals(SCRIPT_WORD.subscriptEnd)) {
					depth--;
				} else {
					currentExprBegin = i + 1;
				}
			}
		}

		return lengthsNode;
	}





	// ====================================================================================================
	// Parsing of Function Declarations
	// 関数宣言の構文解析関連
	// ====================================================================================================


	/**
	 * 関数宣言文を構成するトークン配列に対して構文解析を行い、AST（抽象構文木）を構築して返します。
	 *
	 * このメソッドが返すASTのルートは、{@link AstNode.Type#FUNCTION FUNCTION} タイプのノードとなります。
	 * また、関数名（識別子）を {@link AttributeKey#IDENTIFIER_VALUE IDENTIFIER} 属性、
	 * データ型を {@link AttributeKey#DATA_TYPE DATA_TYPE} 属性、
	 * 配列次元を {@link AttributeKey#RANK RANK} 属性の値に持ちます。
	 * さらに、引数がある場合には、子ノードとして {@link AstNode.Type#VARIABLE VARIABLE}
	 * タイプのノードがぶら下がります。
	 *
	 * なお、関数のシグネチャ宣言部を除いた、ブロック { ... } の中身のコードに関する内容は、
	 * この {@link AstNode.Type#FUNCTION FUNCTION}
	 * タイプのノードやその子ノードには含まれません。
	 * 関数のブロック内の内容は、
	 * {@link Parser#parse(Token[]) parse} メソッドによるコード全体の構文解析結果のASTにおいて、
	 * この {@link AstNode.Type#FUNCTION FUNCTION} タイプのノードの直後に続く
	 * {@link AstNode.Type#BLOCK BLOCK} タイプのノードとして保持されます。
	 *
	 * @param tokens 文のトークン配列（関数宣言の先頭からブロック直前までの、いわゆるシグネチャ部分）
	 * @return 構築したAST（抽象構文木）のルートノード
	 * @throws VnanoException 文の構文に異常があった場合にスローされます。
	 */
	private AstNode parseFunctionDeclarationStatement(Token[] tokens) throws VnanoException {

		int tokenLength = tokens.length;
		int lineNumber = tokens[0].getLineNumber();
		String fileName = tokens[0].getFileName();

		int rank = 0;
		Token identifierToken = null;

		// 最初のトークンはデータ型
		Token dataTypeToken = tokens[0];

		// それ以降のトークンを、識別子が来るまで読み進める
		int readingIndex = 1;
		while(readingIndex < tokenLength) {

			// 識別子トークンの場合
			if (tokens[readingIndex].getType() == Token.Type.LEAF
					&& tokens[readingIndex].getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.FUNCTION_IDENTIFIER) ) {
				identifierToken = tokens[readingIndex];
				readingIndex++;
				break;

			// それ以外は、データ型の後に付く配列の「 [ 」か「 ][ 」か「 ] 」しか有り得ないので、配列次元数をカウントする
			// (文の種類の判断時に startsWithFunctionDeclarationTokens メソッドで既に検査されている)
			} else {

				// 以下、後でもっと開き閉じが対応しているかの検査などが追加で必要
				// -> 処理中に検査入れると読むの難しくなるし、検査は LexicalChecker に分離して対応すべき（どうせ引数部の検査もあるし）

				String operatorSyntax = tokens[readingIndex].getAttribute(AttributeKey.OPERATOR_SYNTAX);

				// 「 [ 」か「 ][ 」の場合は次元を1つ上げる（検査ではなくただの次元カウントなので、「 ] 」では特に何もしない）
				if (operatorSyntax.equals(AttributeValue.MULTIARY) || operatorSyntax.equals(AttributeValue.MULTIARY_SEPARATOR)) {
					rank++;
				}
			}
			readingIndex++;
		}

		// 次のトークンは引数部の始点の「 ( 」なので読み飛ばす
		readingIndex++;

		// 引数部を読み進む
		int argumentBegin = readingIndex;
		List<AstNode> argumentNodeList = new ArrayList<AstNode>();
		while (readingIndex < tokenLength) {

			// 「 , 」 か 「 ) 」が出現する度に、そこまでで1つの引数宣言として一旦切って解釈
			if (tokens[readingIndex].getValue().equals(SCRIPT_WORD.argumentSeparator) ||
					tokens[readingIndex].getValue().equals(SCRIPT_WORD.paranthesisEnd)) {

				// 引数のトークンを変数宣言文として解釈してASTノードを生成（トークン数が0の場合はvoidなので無視）
				Token[] argTokens = Arrays.copyOfRange(tokens, argumentBegin, readingIndex);
				if (0 < argTokens.length) {

					// シグネチャのみをパースする用途の事を考えて、ここでは引数名は省略可能とし、意味解析で検査する
					AstNode argNode = this.parseVariableDeclarationStatement(argTokens, false);
					argumentNodeList.add(argNode);
					argumentBegin = readingIndex + 1;
				}
			}
			readingIndex++;
		}

		// 関数宣言文のASTノードを生成し、属性値や引数ノードを登録
		AstNode node = new AstNode(AstNode.Type.FUNCTION, lineNumber, fileName);
		node.setAttribute(AttributeKey.IDENTIFIER_VALUE, identifierToken.getValue());
		node.setAttribute(AttributeKey.DATA_TYPE, dataTypeToken.getValue());
		node.setAttribute(AttributeKey.RANK, Integer.toString(rank));
		for (AstNode argNode: argumentNodeList) {
			node.addChildNode(argNode);
		}
		return node;
	}


	/**
	 * トークン配列内の指定位置から読み進み、それが関数宣言文で始まっているかを判定します。
	 *
	 * @param tokens トークン配列
	 * @param begin 読み進む視点
	 * @return 判定結果（関数宣言文で始まっていれば true ）
	 */
	private boolean startsWithFunctionDeclarationTokens(Token[] tokens, int begin) {
		int tokenLength = tokens.length;

		// 最初がデータ型でなければ明らかに関数宣言ではない
		if (tokens[begin].getType() != Token.Type.DATA_TYPE) {
			return false;
		}

		// 識別子が来るまで読み進める
		int readingIndex = begin + 1;
		while (readingIndex < tokenLength) {

			Token readingToken = tokens[readingIndex];

			boolean readingTokenIsFunctionIdenfifier = readingToken.getType() == Token.Type.LEAF
					&& readingToken.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.FUNCTION_IDENTIFIER);

			boolean readingTokenIsIndex = readingToken.getType() == Token.Type.OPERATOR
					&& readingToken.getAttribute(AttributeKey.OPERATOR_EXECUTOR) == AttributeValue.SUBSCRIPT;

			// 最初に見つかったのが関数識別子であれば関数宣言
			if (readingTokenIsFunctionIdenfifier) {
				return true;

			// 識別子の前に、データ型の後に付いて配列である事を示す [ ] が付いている事は有り得る。
			// しかし、それ以外のトークンがそこに存在する事はあり得ないので、その場合は関数宣言ではない。
			} else if (!readingTokenIsIndex) {
				return false;
			}
			readingIndex++;
		}

		// ここに到達するのは、データ型の後に [ ] のみが続いてトークン末尾に達した場合なので、関数宣言文ではない
		return false;
	}





	// ====================================================================================================
	// Parsing of Control Statements
	// 制御文の構文解析関連
	// ====================================================================================================


	/**
	 * 制御文を構成するトークン配列に対して構文解析を行い、AST（抽象構文木）を構築して返します。
	 *
	 * このメソッドが返すASTのルートは、制御文の種類に応じたタイプのノードとなります。
	 * 例えば if 文なら {@link AstNode.Type#IF IF} タイプ、
	 * for 文なら {@link AstNode.Type#FOR FOR} タイプ、
	 * break 文なら {@link AstNode.Type#BREAK BREAK} タイプといった具合です。
	 *
	 * if文とwhile文のノードは、その直下に、条件式に対応する
	 * {@link AstNode.Type#EXPRESSION EXPRESSION}
	 * タイプのノードがぶら下がります。
	 *
	 * また、for 文のノードの直下には、それぞれ初期化文と条件式および更新式に対応する、
	 * {@link AstNode.Type#EXPRESSION EXPRESSION} タイプのノードが、この順に3つぶら下がります。
	 * ただし、初期化文で変数宣言を行っている場合、
	 * 初期化文のノードは {@link AstNode.Type#VARIABLE VARIABLE} タイプとなります。
	 *
	 * その他の制御文については、直下に何もぶら下がりません。
	 *
	 * @param tokens 制御文を構成するトークン配列
	 * @return 構築したAST（抽象構文木）のルートノード
	 * @throws VnanoException 文の構文に異常があった場合にスローされます。
	 */
	private AstNode parseControlStatement(Token[] tokens) throws VnanoException {

		// 最初に、括弧の存在や条件式・文の存在などを検査しておく（問題がある場合はここで例外発生）
		new LexicalChecker(LANG_SPEC).checkControlStatementTokens(tokens);

		Token controlTypeToken = tokens[0];
		int lineNumber = controlTypeToken.getLineNumber();
		String fileName = controlTypeToken.getFileName();

		// if文の場合: if文ノードを生成し、条件式をパースしてぶら下げる
		if(controlTypeToken.getValue().equals(SCRIPT_WORD.ifStatement)) {
			AstNode node = new AstNode(AstNode.Type.IF, lineNumber, fileName);
			node.addChildNode(this.parseExpression(Arrays.copyOfRange(tokens, 2, tokens.length-1)));
			return node;

		// else文の場合: else文ノードを生成するのみ
		} else if(controlTypeToken.getValue().equals(SCRIPT_WORD.elseStatement)) {
			AstNode node = new AstNode(AstNode.Type.ELSE, lineNumber, fileName);
			return node;

		// whilw文の場合: while文ノードを生成し、条件式をパースしてぶら下げる
		} else if(controlTypeToken.getValue().equals(SCRIPT_WORD.whileStatement)) {
			AstNode node = new AstNode(AstNode.Type.WHILE, lineNumber, fileName);
			node.addChildNode(this.parseExpression(Arrays.copyOfRange(tokens, 2, tokens.length-1)));
			return node;

		// for文の場合: for文ノードを生成し、初期化文（変数宣言文または式文）・条件文・更新文をパースしてぶら下げる
		} else if(controlTypeToken.getValue().equals(SCRIPT_WORD.forStatement)) {
			AstNode node = new AstNode(AstNode.Type.FOR, lineNumber, fileName);

			// 初期化文と条件文の終端トークンインデックスを取得
			int initializationEnd = Token.getIndexOf(tokens, SCRIPT_WORD.endOfStatement, 0);
			int conditionEnd = Token.getIndexOf(tokens, SCRIPT_WORD.endOfStatement, initializationEnd+1);

			// 初期化文をパースしてfor文ノードにぶら下げる: 初期化文が変数宣言文の場合
			if (DATA_TYPE_NAME.isDataTypeName(tokens[2].getValue())) {
				node.addChildNode(this.parseVariableDeclarationStatement(Arrays.copyOfRange(tokens, 2, initializationEnd), true));
			} else if (initializationEnd == 2) { // 空文の場合
				node.addChildNode(new AstNode(AstNode.Type.EMPTY, tokens[0].getLineNumber(), tokens[0].getFileName()));
			} else { // それ以外の場合は式文（しか許されない）
				node.addChildNode(this.parseExpression(Arrays.copyOfRange(tokens, 2, initializationEnd)));
			}

			// 条件文をパースしてfor文ノードにぶら下げる
			if (initializationEnd+1 == conditionEnd) { // 空文の場合
				node.addChildNode(new AstNode(AstNode.Type.EMPTY, tokens[0].getLineNumber(), tokens[0].getFileName()));
			} else { // 式文の場合
				node.addChildNode(this.parseExpression(Arrays.copyOfRange(tokens, initializationEnd+1, conditionEnd)));
			}

			// 更新文をパースしてfor文ノードにぶら下げる
			if (conditionEnd+1 == tokens.length-1) { // 空文の場合
				node.addChildNode(new AstNode(AstNode.Type.EMPTY, tokens[0].getLineNumber(), tokens[0].getFileName()));
			} else { // 式文の場合
				node.addChildNode(this.parseExpression(Arrays.copyOfRange(tokens, conditionEnd+1, tokens.length-1)));
			}

			return node;

		// return文の場合: return文ノードを生成し、戻り値の式をパースしてぶら下げる
		} else if(controlTypeToken.getValue().equals(SCRIPT_WORD.returnStatement)) {
			AstNode node = new AstNode(AstNode.Type.RETURN, lineNumber, fileName);
			if (2 <= tokens.length) {
				node.addChildNode(this.parseExpression(Arrays.copyOfRange(tokens, 1, tokens.length)));
			}
			return node;

		// break文の場合: break文ノードを生成するのみ
		} else if(controlTypeToken.getValue().equals(SCRIPT_WORD.breakStatement)) {
			AstNode node = new AstNode(AstNode.Type.BREAK, lineNumber, fileName);
			return node;

		// continue文の場合: continue文ノードを生成するのみ
		} else if(controlTypeToken.getValue().equals(SCRIPT_WORD.continueStatement)) {
			AstNode node = new AstNode(AstNode.Type.CONTINUE, lineNumber, fileName);
			return node;

		} else {
			// ここに到達するのはLexicalAnalyzerの異常（不明な種類の制御構文）
			throw new VnanoFatalException("Unknown controll statement: " + controlTypeToken.getValue());
		}
	}





	// ====================================================================================================
	// Others (Utilities, etc.)
	// その他の部品的な処理など
	// ====================================================================================================


	/**
	 * 演算子のトークンの情報に基づいて、
	 * 演算子ノードである {@link AstNode.Type#OPERATOR OPERATOR} タイプのASTノードを生成して返します。
	 *
	 * @param token 演算子のトークン
	 * @return 生成した演算子ノード
	 */
	private AstNode createOperatorNode(Token token) {
		AstNode operatorNode = new AstNode(AstNode.Type.OPERATOR, token.getLineNumber(), token.getFileName());
		operatorNode.setAttribute(AttributeKey.OPERATOR_ASSOCIATIVITY, token.getAttribute(AttributeKey.OPERATOR_ASSOCIATIVITY));
		operatorNode.setAttribute(AttributeKey.OPERATOR_SYNTAX, token.getAttribute(AttributeKey.OPERATOR_SYNTAX));
		operatorNode.setAttribute(AttributeKey.OPERATOR_EXECUTOR, token.getAttribute(AttributeKey.OPERATOR_EXECUTOR));
		operatorNode.setAttribute(AttributeKey.OPERATOR_SYMBOL, token.getValue());
		operatorNode.setAttribute(AttributeKey.OPERATOR_PRECEDENCE, Integer.toString(token.getPrecedence()));

		// キャスト演算子など、演算子トークンがランク・型情報を持っている場合があるので、その場合はノードに設定
		if (token.hasAttribute(AttributeKey.DATA_TYPE)) {
			operatorNode.setAttribute(AttributeKey.DATA_TYPE, token.getAttribute(AttributeKey.DATA_TYPE));
		}
		if (token.hasAttribute(AttributeKey.RANK)) {
			operatorNode.setAttribute(AttributeKey.RANK, token.getAttribute(AttributeKey.RANK));
		}

		return operatorNode;
	}


	/**
	 * 識別子（変数名や関数名）またはリテラルを表すトークンの情報に基づいて、
	 * 式のリーフノードとなる {@link AstNode.Type#LEAF LEAF} タイプのASTノードを生成して返します。
	 *
	 * @param token 識別子またはリテラルのトークン
	 * @return 生成したリーフノード
	 */
	private AstNode createLeafNode(Token token) {
		AstNode node = new AstNode(AstNode.Type.LEAF, token.getLineNumber(), token.getFileName());
		node.setAttribute(AttributeKey.LEAF_TYPE, token.getAttribute(AttributeKey.LEAF_TYPE));

		// リテラルなら値を LITERAL 属性に設定し、LexicalAnalyzerで設定されているデータ型も設定
		if (token.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.LITERAL)) {
			node.setAttribute(AttributeKey.LITERAL_VALUE, token.getValue());
			node.setAttribute(AttributeKey.DATA_TYPE, token.getAttribute(AttributeKey.DATA_TYPE));

		// それ以外は識別子なので値を IDENTIFIER 属性に設定
		} else {
			node.setAttribute(AttributeKey.IDENTIFIER_VALUE, token.getValue());
		}
		return node;
	}


	/**
	 * 識別子またはリテラルの表す文字列に基づいて、
	 * 式のリーフノードとなる {@link AstNode.Type#LEAF LEAF} タイプのASTノードを生成して返します。
	 *
	 * @param tokenValue 識別子またはリテラルの文字列
	 * @param leafType {@link AttributeKey#LEAF_TYPE LEAF_TYPE}属性の値
	 * @param dataType リテラルのデータ型名
	 * @return 生成したリーフノード
	 */
	private AstNode createLeafNode(String tokenValue, String leafType, String dataType, String fileName, int lineNumber) {
		Token token = new Token(tokenValue, lineNumber, fileName);
		token.setType(Token.Type.LEAF);
		token.setAttribute(AttributeKey.LEAF_TYPE, leafType);
		token.setAttribute(AttributeKey.DATA_TYPE, dataType);
		return this.createLeafNode(token);
	}


	/**
	 * 構文解析の作業用スタック上に、フタノード（{@link AstNode.Type#STACK_LID STACK_LID} タイプのノード）を積みます。
	 *
	 * フタノードは、最終的なAST（抽象構文木）の中には現れませんが、
	 * 構文解析作業中において、スタックの領域を分割したり、特定の領域が始まる目印として使用されます。
	 *
	 * このメソッドは、{@link AttributeKey#LID_MARKER LID_MARKER} 属性値を持たないフタノードを積むもので、
	 * ブロック文の始点や、複数の部分式の区切りなどの際に使用されます。
	 *
	 * @param stack 構文解析の作業用スタックとして使用している双方向キュー
	 */
	private void pushLid(Deque<AstNode> stack) {
		AstNode stackLid = new AstNode(AstNode.Type.STACK_LID, 0, "");
		stackLid.setAttribute(AttributeKey.OPERATOR_PRECEDENCE, Integer.toString(OPERATOR_PRECEDENCE.leastPrior));
		stack.push(stackLid);
	}


	/**
	 * 構文解析の作業用スタック上に、フタノード（{@link AstNode.Type#STACK_LID STACK_LID} タイプのノード）を積み、
	 * その {@link AttributeKey#LID_MARKER LID_MARKER} 属性値に、引数 marker の値を設定します。
	 *
	 * フタノードは、最終的なAST（抽象構文木）の中には現れませんが、
	 * 構文解析作業中において、スタックの領域を分割したり、特定の領域が始まる目印として使用されます。
	 *
	 * このメソッドは、関数呼び出し演算子や配列要素アクセス演算子において、
	 * 引数やインデックスを指定する部分式（複数あり得る）の始点を、
	 * スタック上で示すために使用されます。
	 * 両者は互いに入れ子になる可能性があるため、スタックからの回収時に区別するために
	 * {@link AttributeKey#LID_MARKER LID_MARKER} 属性値が使用されます。
	 * 詳細は {@link Parser#popPartialExpressionNodes popPartialExpressionNodes} メソッドの説明を参照してください。
	 *
	 * @param stack 構文解析の作業用スタックとして使用している双方向キュー
	 * @param marker フタノードに設定する {@link AttributeKey#LID_MARKER LID_MARKER} 属性値
	 */
	private void pushLid(Deque<AstNode> stack, String marker) {
		AstNode stackLid = new AstNode(AstNode.Type.STACK_LID, 0, "");
		stackLid.setAttribute(AttributeKey.OPERATOR_PRECEDENCE, Integer.toString(OPERATOR_PRECEDENCE.leastPrior));
		stackLid.setAttribute(AttributeKey.LID_MARKER, marker);
		stack.push(stackLid);
	}


	/**
	 * 構文解析の作業用スタック上に構築されている、文のAST（抽象構文木）ノードの内、
	 * フタノード（{@link AstNode.Type#STACK_LID STACK_LID} タイプのノード）以降にあるものを全て回収します。
	 *
	 * このメソッドによって返される配列は、要素として文のASTのルートノードを、
	 * スタック上のフタノード以降に置かれている数だけ格納しています。
	 * その順序は、スタックの深い側（最初に積まれた側）にあるものが先頭になるよう整列されます。
	 *
	 * なお、このメソッドは、構文解析がブロック文の終端トークンに達した際に、
	 * ブロック文の内側の文のASTノードを、スタック上から回収するために使用されます。
	 *
	 * @param stack 構文解析の作業用スタックとして使用している双方向キュー
	 * @return 文式のASTのルートノードを格納する配列（スタック上で深い側にあるものが先頭）
	 */
	private AstNode[] popStatementNodes(Deque<AstNode> stack) {

		List<AstNode> statementNodeList = new ArrayList<AstNode>();
		while(stack.size() != 0) {

			// フタノードに到達したら、フタを除去して回収完了
			if (stack.peek().getType()==AstNode.Type.STACK_LID) {
				stack.pop();
				break;
			}

			// 文の構文木ノードを1個取り出す
			AstNode statementNode = stack.pop();
			statementNodeList.add(statementNode);

		}

		// スタックに入れた順と取り出し順は逆になっているので、逆転させて戻す
		Collections.reverse(statementNodeList);

		return statementNodeList.toArray(new AstNode[0]);
	}


	/**
	 * 構文解析の作業用スタック上に構築されている、
	 * 部分式のAST（抽象構文木）のルートノードを全て回収します。
	 *
	 * ただし、作業用スタック上には、部分式のルートノードが積まれている始点（深い側）に、
	 * 目印として引数 marker と同じ値を {@link AttributeKey#LID_MARKER LID_MARKER}
	 * 属性値に持つフタノード（{@link AstNode.Type#STACK_LID STACK_LID} タイプのノード）
	 * が配置されている事が必要です。
	 * また、部分式が複数ある場合、スタック上でそれらのルートノードの間には、
	 * 引数 marker とは異なる値を {@link AttributeKey#LID_MARKER LID_MARKER}
	 * 属性値に持つフタノードが挟まれている事が必要です。
	 *
	 * このメソッドによって返される配列は、要素として部分式の構文木のルートノードを、
	 * 部分式の数だけ格納しています。
	 * その順序は、スタックの深い側（最初に積まれた側）にあるものが先頭になるよう整列されます。
	 *
	 * なお、このメソッドで回収する部分式は、例えば式の構文解析において、
	 * 演算子の優先度を調整するための括弧（かっこ）や、
	 * 関数呼び出し演算子、および配列要素アクセス演算子の括弧で挟まれた箇所に存在します。
	 * このParserでは、構文解析がそれらの括弧の終端トークンに達した時点で、
	 * スタック上に部分式のASTのルートノードが積まれた状態になるよう実装されています。
	 * そのタイミングでこのメソッドが使用され、それらが回収されて、
	 * 演算子ノードの直下にオペランドとしてぶら下げられます。
	 *
	 * @param stack 構文解析の作業用スタックとして使用している双方向キュー
	 * @param marker 回収の最深部に配置してあるフタノードのマーカー値
	 * @return 回収した部分式のASTのルートノードを格納する配列（スタック上で深い側にあるものが先頭）
	 * @throws VnanoException 部分式が空の場合にスローされます。
	 */
	private AstNode[] popPartialExpressionNodes(Deque<AstNode> stack, String marker) throws VnanoException {

		List<AstNode> partialExprNodeList = new ArrayList<AstNode>();
		while(stack.size() != 0) {

			// 部分式の構文木ノードを1個取り出す
			// （引数が無い関数の呼び出し fun() の場合など、部分式の中身が無い場合もある事に注意）
			if (stack.peek().getType() != AstNode.Type.STACK_LID) { // 中身が無い場合は直前にフタがあるだけ
				partialExprNodeList.add(stack.pop());
			}

			// ここでフタが残って無ければ処理が異常
			if (stack.size() == 0) {
				throw new VnanoFatalException("State of the working-stack of the parser is inconsistent");
			}

			// フタを除去
			if (stack.peek().getType() == AstNode.Type.STACK_LID) {
				AstNode stackLid = stack.pop();

				// フタに記載されたマーカーが指定値なら、部分式の回収は完了
				if (stackLid.hasAttribute(AttributeKey.LID_MARKER)
						&& stackLid.getAttribute(AttributeKey.LID_MARKER).equals(marker)) {

					break;
				}
			}
		}

		// スタックに入れた順と取り出し順は逆になっているので、逆転させて戻す
		Collections.reverse(partialExprNodeList);

		return partialExprNodeList.toArray(new AstNode[0]);
	}


	/**
	 * トークン配列の中に含まれる、全キャスト演算子の「 始点, データ型, 終端 」と並ぶトークン列をまとめ、
	 * それぞれ単一のトークンに変換したもので置き換えて返します。
	 *
	 * @param 変換対象のトークン配列
	 */
	private Token[] preprocessCastSequentialTokens(Token[] tokens) {

		int tokenLength = tokens.length; // トークン数
		int readingIndex = 0; // 注目トークンのインデックス

		// 変換後のトークンを格納するリスト
		List<Token> tokenList = new ArrayList<Token>();

		while (readingIndex<tokenLength) {

			Token readingToken = tokens[readingIndex];

			boolean isCastBeginToken = readingIndex < tokenLength-2
				&& readingToken.getType() == Token.Type.OPERATOR
				&& readingToken.getAttribute(AttributeKey.OPERATOR_EXECUTOR).equals(AttributeValue.CAST);

			// キャスト始点トークンの場合は、次がデータ型トークン、
			// その後が終端トークン（配列キャストは未サポート）なので、情報を1つにまとめてリストに格納
			if (isCastBeginToken) {
				String dataType = tokens[ readingIndex+1 ].getValue();
				readingToken.setAttribute(AttributeKey.DATA_TYPE, dataType);
				readingToken.setAttribute(AttributeKey.RANK, Integer.toString(RANK_OF_SCALAR)); // 現在は配列のキャストに未対応なため
				readingToken.setValue(SCRIPT_WORD.parenthesisBegin + dataType + SCRIPT_WORD.paranthesisEnd);
				tokenList.add(readingToken);
				readingIndex += 3;

			// その他のトークンの場合はそのまま変換後リストに格納
			} else {
				tokenList.add(readingToken);
				readingIndex++;
			}
		}

		Token[] resultTokens = tokenList.toArray(new Token[0]);
		return resultTokens;
	}


	/**
	 * 変数宣言文のトークン配列から、配列変数の要素数宣言部の後端トークン（ ] ）を検索し、
	 * そのインデックスを返します。
	 *
	 * なお、宣言されている配列が多次元の場合、最後の次元の後端トークンが検索されます。
	 * また、配列要素数を記述する式の中に、配列要素アクセス演算子が存在する場合
	 * （つまり、別の配列の要素の値を、宣言配列の要素数の式中で使用している場合）
	 * においても、その後端の「 ] 」記号の存在は、結果には影響しません。
	 * このメソッドは、あくまでも配列変数の要素数宣言部の後端の位置を返します。
	 *
	 * @param tokens 変数宣言文のトークン配列
	 * @param fromIndex 検索を開始する位置のインデックス
	 * @return 見つかった後端トークン（ ] ）のインデックス（見つからなかった場合は -1）
	 */
	private int getLengthEndIndex(Token[] tokens, int fromIndex) {

		int tokenLength = tokens.length; // トークン総数
		int depth = 0; // 「 [ 」で上がり「 ] 」で下がる階層をカウントする

		for(int i=fromIndex; i<tokenLength; i++) {

			// トークン内容の文字列
			String word = tokens[i].getValue();

			// 見つからないまま初期化式に入った場合は要素数宣言無し
			if (depth==0 && word.equals(SCRIPT_WORD.assignment)) {
				return -1;
			}

			// 「 [ 」記号があれば階層を上がる
			// 階層0が要素数宣言、1以上は要素数の式中の配列要素アクセス演算子のもの
			if(word.equals(SCRIPT_WORD.subscriptBegin)) {
				depth++;

			// 「 ] 」記号があれば階層を下がる
			// （この処理系では、次元区切り「 ][ 」は別種のトークンなのでここにはヒットしない）
			} else if(word.equals(SCRIPT_WORD.subscriptEnd)) {
				depth--;

				// 階層0の「 ] 」は配列要素数宣言の終端なので、インデックスを返す
				if (depth == 0) {
					return i;
				}
			}
		}
		return -1;
	}

}
