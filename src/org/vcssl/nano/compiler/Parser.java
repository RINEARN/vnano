/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.vcssl.nano.VnanoSyntaxException;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.PriorityTable;
import org.vcssl.nano.spec.ScriptWord;
import org.vcssl.nano.spec.ErrorType;


/**
 * <p>
 * コンパイラ内において、
 * {@link LexicalAnalyzer LexicalAnalyzer}（字句解析器）
 * が出力した {@link Token Token} 配列に対して構文解析処理を行い、
 * {@link AstNode AstNode} を組み合わせたAST（抽象構文木）へと変換する、
 * 構文解析器のクラスです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class Parser {


	/**
	 * このクラスは状態を保持するフィールドを持たないため、コンストラクタは何もしません。
	 */
	public Parser() {
	}


	/**
	 * ソースコード全体のトークン配列に対して構文解析を行い、
	 * AST（抽象構文木）を構築して返します。
	 *
	 * このメソッドの入力値であるトークンの配列は、ソースコードに対し、
	 * {@link LexicalAnalyzer#analyze LexicalAnalyzer.analyze}
	 * メソッドによって字句解析を行って得る事ができます。
	 *
	 * このメソッドが出力するASTは、まだ各トークンの構文的な関係をツリー構造に表現した段階のものであり、
	 * 中間コード生成に必要な情報が全て揃ってはいません。
	 * 従って、中間コード生成のステージよりも前に、まずこのメソッドが出力するASTに対して、
	 * {@link SemanticAnalyzer#analyze SemanticAnalyzer.analyze}
	 * メソッドによって意味解析を行い、各種情報を補完する必要があります。
	 *
	 * @param tokens 字句解析によって生成されたトークン配列
	 * @return 構築したAST（抽象構文木）のルートノード
	 * @throws VnanoSyntaxException 文の終端が見つからない場合にスローされます。
	 */
	public AstNode parse(Token[] tokens) throws VnanoSyntaxException {

		// パース作業用のスタックとして使用する双方向キュー
		Deque<AstNode> statementStack = new ArrayDeque<AstNode>();

		int tokenLength = tokens.length; // トークンの総数
		int statementBegin = 0; // 文の始点のインデックスを格納する

		while(statementBegin < tokenLength) {

			// 文の終端（文末記号）と、次のブロック始点・終端のインデックスを取得
			int statementEnd = this.getTokenIndex(tokens, ScriptWord.END_OF_STATEMENT, statementBegin);
			int blockBegin = this.getTokenIndex(tokens, ScriptWord.BLOCK_BEGIN, statementBegin);
			int blockEnd = this.getTokenIndex(tokens, ScriptWord.BLOCK_END, statementBegin);

			// （3つめの条件は、ブロック終端後に文が無い場合のため）
			if (statementEnd < 0 && blockBegin < 0 && statementBegin!=blockEnd) {
				throw new VnanoSyntaxException(
						ErrorType.STATEMENT_END_IS_NOT_FOUND,
						tokens[statementBegin].getFileName(), tokens[statementBegin].getLineNumber()
				);
			}

			// ブロック文の始点 or 終点の場合
			if (tokens[statementBegin].getType()==Token.Type.BLOCK) {

				// ブロック始点 -> スタックに目印のフタをつめる（第二引数は目印とするマーカー）
				if (tokens[statementBegin].getValue().equals(ScriptWord.BLOCK_BEGIN)) {
					this.pushLid(statementStack);
					statementBegin++;

				// ブロック終点
				} else {

					// 目印のフタの位置まで、スタックから文のノード（=ブロックの中身の文）を全て回収
					AstNode[] statementsInBlock = this.popStatementNodes(statementStack);

					// ブロック文ノードを生成し、上で取り出した文のノードを全てぶら下げる
					AstNode blockNode = new AstNode(
							AstNode.Type.BLOCK, tokens[statementBegin].getLineNumber(), tokens[statementBegin].getFileName()
					);
					for (AstNode statementNode: statementsInBlock) {
						blockNode.addChildNode(statementNode);
					}

					// ブロック文ノードをプッシュ
					statementStack.push(blockNode);
					statementBegin++;
				}

			// 制御文の場合
			} else if (tokens[statementBegin].getType()==Token.Type.CONTROL) {

				// if / for / while文 (この処理系では直後にブロックが必須)
				if (tokens[statementBegin].getValue().equals(ScriptWord.IF)
						|| tokens[statementBegin].getValue().equals(ScriptWord.FOR)
						|| tokens[statementBegin].getValue().equals(ScriptWord.WHILE)) {

					Token[] subTokens = Arrays.copyOfRange(tokens, statementBegin, blockBegin);
					statementStack.push(this.parseControlStatement(subTokens));
					statementBegin = blockBegin;

				// else文
				} else if (tokens[statementBegin].getValue().equals(ScriptWord.ELSE)) {
					Token[] subTokens = Arrays.copyOfRange(tokens, statementBegin, statementBegin+1);
					statementStack.push(this.parseControlStatement(subTokens));
					statementBegin++;

				// break / continue文など
				} else {
					Token[] subTokens = Arrays.copyOfRange(tokens, statementBegin, statementEnd);
					statementStack.push(this.parseControlStatement(subTokens));
					statementBegin = statementEnd + 1;
				}

			// 変数宣言文の場合
			} else if (tokens[statementBegin].getType()==Token.Type.DATA_TYPE) {

				Token[] subTokens = Arrays.copyOfRange(tokens, statementBegin, statementEnd);
				statementStack.push(this.parseVariableDeclarationStatement(subTokens));
				statementBegin = statementEnd + 1;

			// 式文の場合
			} else {
				Token[] subTokens = Arrays.copyOfRange(tokens, statementBegin, statementEnd);
				statementStack.push(this.parseExpression(subTokens));
				statementBegin = statementEnd + 1;
			}
		}

		// ルートノードに文を全てぶら下げて返す(スタックに積まれている順序に注意)
		AstNode rootNode = new AstNode(AstNode.Type.ROOT, tokens[0].getLineNumber(), tokens[0].getFileName());
		while (statementStack.size() != 0) {
			rootNode.addChildNode(statementStack.pollLast());
		}
		return rootNode;
	}

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
	 * @return 構築したAST（抽象構文木）のルートノード
	 * @throws VnanoSyntaxException 文の構文に異常があった場合にスローされます。
	 */
	private AstNode parseVariableDeclarationStatement(Token[] tokens) throws VnanoSyntaxException {

		AstNode variableNode = new AstNode(AstNode.Type.VARIABLE, tokens[0].getLineNumber(), tokens[0].getFileName());

		LinkedList<Token> tokenList = new LinkedList<Token>();
		for (Token token: tokens) {
			tokenList.add(token);
		}

		int readingIndex = 0;

		// 型情報を付加
		Token typeToken = tokens[readingIndex];
		variableNode.addAttribute(AttributeKey.DATA_TYPE, typeToken.getValue());
		readingIndex++;

		// 次のトークンが存在しないか、識別子トークンではない場合は構文エラー
		if (tokens.length <= readingIndex || tokens[readingIndex].getType()!=Token.Type.LEAF
			|| !tokens[readingIndex].getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.VARIABLE_IDENTIFIER)) {

			throw new VnanoSyntaxException(
					ErrorType.NO_IDENTIFIER_IN_VARIABLE_DECLARATION,
					tokens[readingIndex-1].getFileName(), tokens[readingIndex-1].getLineNumber()
			);
		}

		// 識別子情報を付加
		Token nameToken = tokens[readingIndex];
		variableNode.addAttribute(AttributeKey.IDENTIFIER_VALUE, nameToken.getValue());
		readingIndex++;

		// 配列要素数の検出
		AstNode arrayLengthNode = null;
		if (readingIndex<tokens.length-1 && tokens[readingIndex].getValue().equals(ScriptWord.INDEX_BEGIN)) {
			int lengthsEnd = getLengthEndIndex(tokens, readingIndex);
			Token[] lengthsTokens = Arrays.copyOfRange(tokens, readingIndex, lengthsEnd+1);
			arrayLengthNode = this.parseVariableDeclarationArrayLengths(lengthsTokens);
			readingIndex = lengthsEnd + 1;
		}

		// 配列情報を付加
		int arrayRank = 0;
		if (arrayLengthNode != null) {
			arrayRank = arrayLengthNode.getChildNodes(AstNode.Type.EXPRESSION).length;
			variableNode.addChildNode(arrayLengthNode);
		}
		variableNode.addAttribute(AttributeKey.RANK, Integer.toString(arrayRank));

		// 初期化式
		if(readingIndex<tokens.length-1 && tokens[readingIndex].getValue().equals(ScriptWord.ASSIGNMENT)) {
			int initTokenLength = tokens.length - readingIndex + 1;
			Token[] initTokens = new Token[initTokenLength];
			initTokens[0] = nameToken;
			for (int initTokenIndex=1; initTokenIndex<initTokenLength; initTokenIndex++) {
				initTokens[initTokenIndex] = tokens[readingIndex];
				readingIndex++;
			}
			variableNode.addChildNode(this.parseExpression(initTokens));
		}

		// それ以上トークンが続く場合は、余計なトークンであるため構文エラー
		if (readingIndex < tokens.length) {
			throw new VnanoSyntaxException(
					ErrorType.TOO_MANY_TOKENS_FOR_VARIABLE_DECLARATION,
					tokens[readingIndex-1].getFileName(), tokens[readingIndex-1].getLineNumber()
			);
		}

		return variableNode;
	}


	/**
	 * 変数宣言文の内で、配列の要素数の宣言部を構成するトークン配列に対して構文解析を行い、
	 * AST（抽象構文木）を構築して返します。
	 *
	 * このメソッドが返すASTのルートは、{@link AstNode.Type#LENGTHS LENGTHS} タイプのノードとなります。
	 * その下には、各次元の要素数の計算式に対応する {@link AstNode.Type#EXPRESSION EXPRESSION}
	 * タイプのノードが、次元の数だけ次元順にぶら下がります。
	 *
	 * @param tokens 要素数宣言部のトークン配列
	 * @return 構築したAST（抽象構文木）のルートノード
	 * @throws VnanoSyntaxException 文の構文に異常があった場合にスローされます。
	 */
	private AstNode parseVariableDeclarationArrayLengths(Token[] tokens) throws VnanoSyntaxException {
		AstNode lengthsNode = new AstNode(AstNode.Type.LENGTHS, tokens[0].getLineNumber(), tokens[0].getFileName());
		int currentExprBegin = -1;

		int tokenLength = tokens.length;
		int depth = 0;
		for(int i=0; i<tokenLength; i++) {
			String word = tokens[i].getValue();

			// 「 [ 」記号
			if(word.equals(ScriptWord.INDEX_BEGIN)) {

				// 階層が 0 なら要素数宣言の開始（それ以外は、要素数の式を構成する配列インデックス演算子のもの）
				if (depth==0) {
					currentExprBegin = i + 1;
				}
				depth++;

			// 「 ] 」記号か「 ][ 」記号
			} else if(word.equals(ScriptWord.INDEX_END) || word.equals(ScriptWord.INDEX_SEPARATOR)) {

				// 階層が 1 なら要素数宣言の次元区切り
				if (depth==1) {
					Token[] exprTokens = Arrays.copyOfRange(tokens, currentExprBegin, i);

					// 要素数宣言の内容が省略されている場合は、要素数 0 の宣言と同じものとする（言語仕様）
					if (exprTokens.length == 0) {
						AstNode zeroNode = this.createLeafNode(
								"0", AttributeValue.LITERAL, tokens[i].getFileName(), tokens[i].getLineNumber()
						);
						lengthsNode.addChildNode(zeroNode);

					// 省略されていなければ、内容を式として解釈してぶら下げる
					} else {
						lengthsNode.addChildNode( this.parseExpression(exprTokens) );
					}
				}
				if (word.equals(ScriptWord.INDEX_END)) {
					depth--;
				} else {
					currentExprBegin = i + 1;
				}
			}
		}

		return lengthsNode;
	}


	/**
	 * 制御文の構文解析の前処理として、文を構成するトークン配列に対して、
	 * 括弧や条件式、その他必要な文の有無などを検査します。
	 * 検査の結果、問題が無かった場合には何もせず、問題が見つかった場合には例外をスローします。
	 *
	 * @param tokens 検査対象のトークン配列
	 * @throws VnanoSyntaxException トークン配列に、制御文の構成トークンとしての問題があった場合にスローされます。
	 */
	private void checkControlStatementTokens(Token[] tokens) throws VnanoSyntaxException {
		Token controlTypeToken = tokens[0];
		int lineNumber = controlTypeToken.getLineNumber();
		String fileName = controlTypeToken.getFileName();

		// if文の場合
		if(controlTypeToken.getValue().equals(ScriptWord.IF)) {

			// 直後が「 ( 」でないか、終端が「 ) 」でない場合は構文エラー
			if (  tokens.length < 3 || !tokens[1].getValue().equals(ScriptWord.PARENTHESIS_BEGIN)
					 || !tokens[tokens.length-1].getValue().equals(ScriptWord.PARENTHESIS_END)  ) {
				throw new VnanoSyntaxException(ErrorType.NO_PARENTHESIS_OF_IF_STATEMENT, fileName, lineNumber);
			}
			// 条件式が空の場合は構文エラー
			if (tokens.length == 3) {
				throw new VnanoSyntaxException(ErrorType.NO_CONDITION_EXPRESSION_OF_IF_STATEMENT, fileName, lineNumber);
			}

		// while文の場合
		} else if(controlTypeToken.getValue().equals(ScriptWord.WHILE)) {

			// 直後が「 ( 」でないか、終端が「 ) 」でない場合は構文エラー
			if (  tokens.length < 3 || !tokens[1].getValue().equals(ScriptWord.PARENTHESIS_BEGIN)
					 || !tokens[tokens.length-1].getValue().equals(ScriptWord.PARENTHESIS_END)  ) {
				throw new VnanoSyntaxException(ErrorType.NO_PARENTHESIS_OF_WHILE_STATEMENT, fileName, lineNumber);
			}
			// 条件式が空の場合は構文エラー
			if (tokens.length == 3) {
				throw new VnanoSyntaxException(ErrorType.NO_CONDITION_EXPRESSION_OF_WHILE_STATEMENT, fileName, lineNumber);
			}

		// for文の場合
		} else if(controlTypeToken.getValue().equals(ScriptWord.FOR)) {

			// 直後が「 ( 」でないか、終端が「 ) 」でない場合は構文エラー
			if (  tokens.length < 3 || !tokens[1].getValue().equals(ScriptWord.PARENTHESIS_BEGIN)
					 || !tokens[tokens.length-1].getValue().equals(ScriptWord.PARENTHESIS_END)  ) {
				throw new VnanoSyntaxException(ErrorType.NO_PARENTHESIS_OF_FOR_STATEMENT, fileName, lineNumber);
			}

			// 初期化式と条件式の終端トークンインデックスを取得
			int initializationEnd = this.getTokenIndex(tokens, ScriptWord.END_OF_STATEMENT, 0);
			int conditionEnd = this.getTokenIndex(tokens, ScriptWord.END_OF_STATEMENT, initializationEnd+1);

			if (initializationEnd < 0 || conditionEnd < 0) {
				throw new VnanoSyntaxException(
						ErrorType.ELEMENTS_OF_FOR_STATEMENT_IS_DEFICIENT, fileName, lineNumber
				);
			}

		// else文の場合: else文ノードを生成するのみ
		} else if(controlTypeToken.getValue().equals(ScriptWord.ELSE)
				|| controlTypeToken.getValue().equals(ScriptWord.BREAK)
				|| controlTypeToken.getValue().equals(ScriptWord.CONTINUE) ) {

			if (tokens.length > 1) {
				throw new VnanoSyntaxException(
						ErrorType.TOO_MANY_TOKENS_FOR_CONTROL_STATEMENT, controlTypeToken.getValue(), fileName, lineNumber
				);
			}

		} else {
			// ここに到達するのはLexicalAnalyzerの異常（不明な種類の制御構文）
			throw new VnanoFatalException("Unknown controll statement: " + controlTypeToken.getValue());
		}
	}


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
	 * @throws VnanoSyntaxException 文の構文に異常があった場合にスローされます。
	 */
	private AstNode parseControlStatement(Token[] tokens) throws VnanoSyntaxException {

		// 最初に、括弧の存在や条件式・文の存在などを検査しておく（問題がある場合はここで例外発生）
		this.checkControlStatementTokens(tokens);

		Token controlTypeToken = tokens[0];
		int lineNumber = controlTypeToken.getLineNumber();
		String fileName = controlTypeToken.getFileName();

		// if文の場合: if文ノードを生成し、条件式をパースしてぶら下げる
		if(controlTypeToken.getValue().equals(ScriptWord.IF)) {
			AstNode node = new AstNode(AstNode.Type.IF, lineNumber, fileName);
			node.addChildNode(this.parseExpression(Arrays.copyOfRange(tokens, 2, tokens.length-1)));
			return node;

		// else文の場合: else文ノードを生成するのみ
		} else if(controlTypeToken.getValue().equals(ScriptWord.ELSE)) {
			AstNode node = new AstNode(AstNode.Type.ELSE, lineNumber, fileName);
			return node;

		// whilw文の場合: while文ノードを生成し、条件式をパースしてぶら下げる
		} else if(controlTypeToken.getValue().equals(ScriptWord.WHILE)) {
			AstNode node = new AstNode(AstNode.Type.WHILE, lineNumber, fileName);
			node.addChildNode(this.parseExpression(Arrays.copyOfRange(tokens, 2, tokens.length-1)));
			return node;

		// for文の場合: for文ノードを生成し、初期化文（変数宣言文または式文）・条件式・更新式をパースしてぶら下げる
		} else if(controlTypeToken.getValue().equals(ScriptWord.FOR)) {
			AstNode node = new AstNode(AstNode.Type.FOR, lineNumber, fileName);

			// 初期化式と条件式の終端トークンインデックスを取得
			int initializationEnd = this.getTokenIndex(tokens, ScriptWord.END_OF_STATEMENT, 0);
			int conditionEnd = this.getTokenIndex(tokens, ScriptWord.END_OF_STATEMENT, initializationEnd+1);

			// 初期化文をパースしてfor文ノードにぶら下げる: 初期化文が変数宣言文の場合
			if (DataTypeName.isDataTypeName(tokens[2].getValue())) {
				node.addChildNode(this.parseVariableDeclarationStatement(Arrays.copyOfRange(tokens, 2, initializationEnd)));

			// そうでなければ式文（しか許されない）
			} else {
				node.addChildNode(this.parseExpression(Arrays.copyOfRange(tokens, 2, initializationEnd)));
			}

			// 条件式と更新式をパースしてfor文ノードにぶら下げる
			node.addChildNode(this.parseExpression(Arrays.copyOfRange(tokens, initializationEnd+1, conditionEnd)));
			node.addChildNode(this.parseExpression(Arrays.copyOfRange(tokens, conditionEnd+1, tokens.length-1)));
			return node;

		// break文の場合: break文ノードを生成するのみ
		} else if(controlTypeToken.getValue().equals(ScriptWord.BREAK)) {
			AstNode node = new AstNode(AstNode.Type.BREAK, lineNumber, fileName);
			return node;

		// continue文の場合: continue文ノードを生成するのみ
		} else if(controlTypeToken.getValue().equals(ScriptWord.CONTINUE)) {
			AstNode node = new AstNode(AstNode.Type.CONTINUE, lineNumber, fileName);
			return node;

		} else {
			// ここに到達するのはLexicalAnalyzerの異常（不明な種類の制御構文）
			throw new VnanoFatalException("Unknown controll statement: " + controlTypeToken.getValue());
		}
	}



	/**
	 * トークン配列を解析し、各要素に、次（右）の演算子要素の優先度を設定します。
	 *
	 * @param tokens 解析・設定対象のトークン配列
	 */
	private int[] getRightOperatorPriorities(Token[] tokens) {

		int length = tokens.length;
		int[] rightOperatorPriorities = new int[ length ];

		// 最も右にある演算子は必ず優先になるよう、最小優先度を初期値とする
		int rightOperatorPriority = PriorityTable.LEAST_PRIOR;

		// 末尾から先頭へ向かって要素を見ていく
		for(int i = length-1; 0 <= i; i--) {

			// i 番の要素に、右の演算子の優先度を設定
			rightOperatorPriorities[i] = rightOperatorPriority;

			// i 番の要素が演算子なら、その優先度を新たな右演算子優先度に設定
			if (tokens[i].getType() == Token.Type.OPERATOR) {
				rightOperatorPriority = tokens[i].getPriority();
			}

			// 括弧の内部の部分式は外側よりも常に高優先度となるよう、括弧の境界部で調整する
			if (tokens[i].getType() == Token.Type.PARENTHESIS) {

				// 部分式内部が常に優先になるよう、開き括弧 ( では右側演算子優先度を最高値に設定する
				if(tokens[i].getValue().equals(ScriptWord.PARENTHESIS_BEGIN)){
					rightOperatorPriority = PriorityTable.MOST_PRIOR;

				// 閉じ括弧 ) は文末と同じ効果なので、右側演算子優先度を最低に設定する
				} else {
					rightOperatorPriority = PriorityTable.LEAST_PRIOR;

				}
			}
		}

		return rightOperatorPriorities;
	}

	/**
	 * 式のトークン配列内における、開き括弧「 ( 」と閉じ括弧「 ) 」の個数が合っているかどうかを検査します。
	 * 検査の結果、個数が合っていた場合には何もせず、合っていなかった場合には例外をスローします。
	 *
	 * @param tokens 検査対象のトークン配列
	 * @throws VnanoSyntaxException 開き括弧と閉じ括弧の個数が合っていなかった場合にスローされます。
	 */
	private void checkNumberOfParenthesesInExpression(Token[] tokens) throws VnanoSyntaxException {
		int tokenLength = tokens.length;
		int hierarchy = 0; // 開き括弧で上がり、閉じ括弧で下がる階層カウンタ
		for (int tokenIndex=0; tokenIndex<tokenLength; tokenIndex++) {
			Token token = tokens[tokenIndex];
			if (token.getType() == Token.Type.PARENTHESIS) {
				if (token.getValue().equals(ScriptWord.PARENTHESIS_BEGIN)) {
					hierarchy++;
				} else if (token.getValue().equals(ScriptWord.PARENTHESIS_END)) {
					hierarchy--;
				}
			}
			// 階層が負になった場合は、その時点で明らかに開き括弧が足りない
			if (hierarchy < 0) {
				throw new VnanoSyntaxException(
					ErrorType.OPENING_PARENTHESES_IS_DEFICIENT,
					tokens[0].getFileName(), tokens[0].getLineNumber() // 階層が0でない時点でトークンは1個以上あるので[0]で参照可能
				);
			}
		}
		// 式のトークンを全て読み終えた時点で階層が1以上残っているなら、閉じ括弧が足りない
		if (hierarchy > 0) {
			throw new VnanoSyntaxException(
				ErrorType.CLOSING_PARENTHESES_IS_DEFICIENT,
				tokens[0].getFileName(), tokens[0].getLineNumber()
			);
		}
	}


	/**
	 * 式のトークン配列のトークンタイプを検査し、式の構成要素になり得ないタイプのトークンが存在していないか検査します。
	 * 検査の結果、問題が無かった場合には何もせず、問題が見つかった場合には例外をスローします。
	 *
	 * @param tokens 検査対象のトークン配列
	 * @throws VnanoSyntaxException 式の構成要素になり得ないタイプのトークンが存在していた場合にスローされます。
	 */
	private void checkTypeOfTokensInExpression(Token[] tokens) throws VnanoSyntaxException {
		for(Token token: tokens) {
			switch (token.getType()) {

				// 演算子、リーフ、括弧は式の構成要素になる
				case OPERATOR : break;
				case LEAF : break;
				case PARENTHESIS : break;

				// キャスト演算子をサポートする場合は、DATA_TYPEを通すように追加する必要がある

				// それ以外は式の構成要素になり得ない
				default : {
					throw new VnanoSyntaxException(
							ErrorType.INVALID_TYPE_TOKEN_IN_EXPRESSION,
							token.getValue(),
							token.getFileName(), token.getLineNumber()
					);
				}
			}
		}
	}


	/**
	 * 式のトークン配列内における、開き括弧「 ( 」と閉じ括弧「 ) 」に囲まれた部分式の中身が、
	 * 空になっている箇所が無いか検査します。ただし、関数呼び出し演算子の ( ) は検査対象に含まれません。
	 * 検査の結果、問題が無かった場合には何もせず、問題が見つかった場合には例外をスローします。
	 *
	 * @param tokens 検査対象のトークン配列
	 * @throws VnanoSyntaxException 空の部分式が含まれていた場合にスローされます。
	 */
	private void checkBlankParenthesesInExpression(Token[] tokens) throws VnanoSyntaxException {
		int tokenLength = tokens.length;
		int contentCounter = 0;
		for (int tokenIndex=0; tokenIndex<tokenLength; tokenIndex++) {
			Token token = tokens[tokenIndex];
			if (token.getType() == Token.Type.PARENTHESIS) {
				if (token.getValue().equals(ScriptWord.PARENTHESIS_BEGIN)) {
					contentCounter = 0;
				} else if (token.getValue().equals(ScriptWord.PARENTHESIS_END)) {
					if (contentCounter == 0) {
						throw new VnanoSyntaxException(
								ErrorType.NO_PARTIAL_EXPRESSION,
								token.getFileName(), token.getLineNumber()
						);
					}
					contentCounter = 0;
				}
			} else {
				contentCounter++;
			}
		}
	}


	/**
	 * 式のトークン配列内で、演算子トークンの左右に位置するオペランドが、適切な種類のトークンであるか検査します。
	 * 検査の結果、問題が無かった場合には何もせず、問題が見つかった場合には例外をスローします。
	 *
	 * @param tokens 検査対象のトークン配列
	 * @throws VnanoSyntaxException 問題が見つかった場合にスローされます。
	 */
	private void checkOperandsInExpression(Token[] tokens) throws VnanoSyntaxException {
		int tokenLength = tokens.length;

		// トークンを先頭から末尾まで辿って検査
		for (int tokenIndex=0; tokenIndex<tokenLength; tokenIndex++) {
			Token token = tokens[tokenIndex];

			boolean nextIsLeaf = tokenIndex!=tokenLength-1 && tokens[tokenIndex+1].getType()==Token.Type.LEAF;
			boolean prevIsLeaf = tokenIndex!=0 && tokens[tokenIndex-1].getType()==Token.Type.LEAF;

			boolean nextIsOpenParenthesis = tokenIndex != tokenLength-1
					&& tokens[tokenIndex+1].getType() == Token.Type.PARENTHESIS
					&& tokens[tokenIndex+1].getValue().equals(ScriptWord.PARENTHESIS_BEGIN);

			//boolean nextIsCloseParenthesis = tokenIndex != tokenLength-1
			//		&& tokens[tokenIndex+1].getType() == Token.Type.PARENTHESIS
			//		&& tokens[tokenIndex+1].getValue().equals(ScriptWord.PARENTHESIS_END);
			boolean prevIsCloseParenthesis = tokenIndex != 0
					&& tokens[tokenIndex-1].getType() == Token.Type.PARENTHESIS
					&& tokens[tokenIndex-1].getValue().equals(ScriptWord.PARENTHESIS_END);

			boolean nextIsMultialyBegin = tokenIndex != tokenLength-1
					&& tokens[tokenIndex+1].getType() == Token.Type.OPERATOR
					&& tokens[tokenIndex+1].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.MULTIARY);
			//boolean prevIsMultialyBegin = tokenIndex != 0
			//		&& tokens[tokenIndex-1].getType() == Token.Type.OPERATOR
			//		&& tokens[tokenIndex-1].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.MULTIARY);

			//boolean nextIsMultialyEnd = tokenIndex != tokenLength-1
			//		&& tokens[tokenIndex+1].getType() == Token.Type.OPERATOR
			//		&& tokens[tokenIndex+1].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.MULTIARY_END);

			boolean prevIsMultialyEnd = tokenIndex != 0
					&& tokens[tokenIndex-1].getType() == Token.Type.OPERATOR
					&& tokens[tokenIndex-1].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.MULTIARY_END);

			// 演算子の場合
			if (token.getType() == Token.Type.OPERATOR) {
				String operatorSyntax = token.getAttribute(AttributeKey.OPERATOR_SYNTAX);

				// 前置演算子の場合は、右がリーフか開き括弧か多項演算子（関数呼び出しや配列アクセス）始点でないとエラー
				if (operatorSyntax.equals(AttributeValue.PREFIX)
						&& !(  nextIsLeaf || nextIsOpenParenthesis || nextIsMultialyBegin  ) ) {

					throw new VnanoSyntaxException(
							ErrorType.OPERAND_IS_MISSING_AT_RIGHT,
							token.getValue(), token.getFileName(), token.getLineNumber()
					);
				} // 前置演算子の場合

				// 後置演算子の場合は、右がリーフか閉じ括弧か多項演算子（関数呼び出しや配列アクセス）終点でないとエラー
				if (operatorSyntax.equals(AttributeValue.POSTFIX)
						&& !(  prevIsLeaf || prevIsCloseParenthesis || prevIsMultialyEnd ) ) {

					throw new VnanoSyntaxException(
							ErrorType.OPERAND_IS_MISSING_AT_LEFT,
							token.getValue(), token.getFileName(), token.getLineNumber()
					);
				} // 後置演算子の場合

				// 二項演算子や、多項演算子（関数呼び出しや配列アクセス）の区切りの場合
				if (operatorSyntax.equals(AttributeValue.BINARY)
						|| operatorSyntax.equals(AttributeValue.MULTIARY_SEPARATOR)) {

					// 右がリーフか開き括弧か多項演算子（関数呼び出しや配列アクセス）の始点でないとエラー
					if( !(  nextIsLeaf || nextIsOpenParenthesis || nextIsMultialyBegin  ) ) {
						throw new VnanoSyntaxException(
							ErrorType.OPERAND_IS_MISSING_AT_RIGHT,
							token.getValue(), token.getFileName(), token.getLineNumber()
						);
					}
					// 左のトークンがリーフか閉じ括弧か多項演算子（関数呼び出しや配列アクセス）の終点でないとエラー
					if( !(  prevIsLeaf || prevIsCloseParenthesis || prevIsMultialyEnd  ) ) {
						throw new VnanoSyntaxException(
							ErrorType.OPERAND_IS_MISSING_AT_LEFT,
							token.getValue(), token.getFileName(), token.getLineNumber()
						);
					}
				} // 二項演算子や、多項演算子（関数呼び出しや配列アクセス）の区切りの場合

			} // 演算子の場合
		} // トークンを先頭から末尾まで辿るループ
	}


	/**
	 * 式の構文解析の前処理として、式を構成するトークン配列に対して、
	 * トークンタイプや括弧の開き閉じ対応などの検査を行います。
	 * 検査の結果、問題が無かった場合には何もせず、問題が見つかった場合には例外をスローします。
	 *
	 * @param tokens 検査対象のトークン配列
	 * @throws VnanoSyntaxException トークン配列に、式の構成トークンとしての問題があった場合にスローされます。
	 */
	private void checkTokensInExpression(Token[] tokens) throws VnanoSyntaxException {

		// トークン列内の開き括弧と閉じ括弧の対応を確認（合っていなければここで例外発生）
		this.checkNumberOfParenthesesInExpression(tokens);

		// 式の構成要素になり得ない種類のトークンが存在しないか確認（存在すればここで例外発生）
		this.checkTypeOfTokensInExpression(tokens);

		// 式の中に空の括弧が存在しないか確認（関数呼び出し演算子は除く）
		this.checkBlankParenthesesInExpression(tokens);

		// 演算子に対して適切な位置にリーフトークンが存在しているか確認
		this.checkOperandsInExpression(tokens);
	}


	/**
	 * 式のトークン配列を解析し、AST（抽象構文木）を構築して返します。
	 *
	 * @param tokens 式のトークン配列
	 * @return 構築したAST（抽象構文木）のルートノード
	 * @throws VnanoSyntaxException 式の構文に異常があった場合にスローされます。
	 */
	private AstNode parseExpression(Token[] tokens) throws VnanoSyntaxException {

		// 最初に、トークンの種類や括弧の数などに、式の構成トークンとして問題無いか検査
		checkTokensInExpression(tokens);

		// パース作業用のスタックとして使用する双方向キューを用意
		Deque<AstNode> stack = new ArrayDeque<AstNode>();

		int tokenLength = tokens.length; // トークン数
		int readingIndex = 0; // 注目トークンのインデックス

		int[] rightOperatorPriorities = this.getRightOperatorPriorities(tokens);

		// トークンを左から順に末尾まで読み進むループ
		do {

			Token readingToken = tokens[readingIndex];         // このループでの読み込み対象トークン
			int readingPriority = readingToken.getPriority();  // トークンの読み込み優先度（≒演算子優先度）
			AstNode operatorNode = null;                       // 生成した演算子ノードを控える

			// 識別子やリテラルなどのリーフ（末端オペランド）ノードの場合 -> スタックにプッシュ
			if (readingToken.getType() == Token.Type.LEAF) {

				stack.push(this.createLeafNode(readingToken));
				readingIndex++;
				continue;

			// 括弧の場合
			} else if (readingToken.getType()==Token.Type.PARENTHESIS) {

				// 開き括弧の場合、部分式の境界前後が結合しないよう、スタックに非演算子のフタをつめる（第二引数は回収時の目印）
				if (readingToken.getValue().equals(ScriptWord.PARENTHESIS_BEGIN)) {
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
						operatorNode.addChildNode(stack.pop());
						break;
					}

					// 前置演算子
					case AttributeValue.PREFIX : {

						// 優先度が次の演算子よりも強い場合、右トークンを先読みし、リーフノードとして演算子ノードにぶら下げる
						if (readingPriority <= rightOperatorPriorities[readingIndex]) { // 数字が小さい方が優先度が高い
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
						if (readingPriority <= rightOperatorPriorities[readingIndex]) { // 数字が小さい方が優先度が高い
							operatorNode.addChildNode( this.createLeafNode(tokens[readingIndex+1]) );
							readingIndex++; // 次のトークンは先読みして処理を終えたので1つ余分に進める -> これのせいで関数切り分けできない
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
			while (this.shouldAddOperatorToStackedOperatorAsChild(rightOperatorPriorities[readingIndex], stack)) {
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

	private boolean shouldAddOperatorToStackedOperatorAsChild(int rightOperatorPriority, Deque<AstNode> stack) {

		// スタック上の演算子の優先度が、注目トークンの次の演算子よりも強い場合、スタック上の演算子に注目トークンの演算子をぶら下げる
		// （LexicalAnalyzer の処理により、各トークンは次（右）に来る演算子の優先度を知っている）

		if (stack.size() == 0) {
			return false;
		}

		if (stack.peek().getType() != AstNode.Type.OPERATOR) {
			return false;
		}

		// 数字が小さい方が優先度が高い
		int stackedOperatorPriority = Integer.parseInt(stack.peek().getAttribute(AttributeKey.OPERATOR_PRIORITY));
		return stackedOperatorPriority <= rightOperatorPriority; // 数字が小さい方が優先度が高い
	}


	/**
	 * 演算子のトークンの情報に基づいて、
	 * 演算子ノードである {@link AstNode.Type#OPERATOR OPERATOR} タイプのASTノードを生成して返します。
	 *
	 * @param token 演算子のトークン
	 * @return 生成した演算子ノード
	 */
	private AstNode createOperatorNode(Token token) {
		AstNode operatorNode = new AstNode(AstNode.Type.OPERATOR, token.getLineNumber(), token.getFileName());
		operatorNode.addAttribute(AttributeKey.OPERATOR_SYNTAX, token.getAttribute(AttributeKey.OPERATOR_SYNTAX));
		operatorNode.addAttribute(AttributeKey.OPERATOR_EXECUTOR, token.getAttribute(AttributeKey.OPERATOR_EXECUTOR));
		operatorNode.addAttribute(AttributeKey.OPERATOR_SYMBOL, token.getValue());
		operatorNode.addAttribute(AttributeKey.OPERATOR_PRIORITY, Integer.toString(token.getPriority()));
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
		node.addAttribute(AttributeKey.LEAF_TYPE, token.getAttribute(AttributeKey.LEAF_TYPE));

		// リテラルなら値を LITERAL 属性に設定
		if (token.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.LITERAL)) {
			node.addAttribute(AttributeKey.LITERAL_VALUE, token.getValue());

		// それ以外は識別子なので値を IDENTIFIER 属性に設定
		} else {
			node.addAttribute(AttributeKey.IDENTIFIER_VALUE, token.getValue());
		}
		return node;
	}


	/**
	 * 識別子またはリテラルの表す文字列に基づいて、
	 * 式のリーフノードとなる {@link AstNode.Type#LEAF LEAF} タイプのASTノードを生成して返します。
	 *
	 * @param tokenValue 識別子またはリテラルの文字列
	 * @param leafType {@link AttributeKey#LEAF_TYPE LEAF_TYPE}属性の値
	 * @return 生成したリーフノード
	 */
	private AstNode createLeafNode(String tokenValue, String leafType, String fileName, int lineNumber) {
		Token token = new Token(tokenValue, lineNumber, fileName);
		token.setType(Token.Type.LEAF);
		token.addAttribute(AttributeKey.LEAF_TYPE, leafType);
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
		stackLid.addAttribute(AttributeKey.OPERATOR_PRIORITY, Integer.toString(PriorityTable.LEAST_PRIOR));
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
		stackLid.addAttribute(AttributeKey.OPERATOR_PRIORITY, Integer.toString(PriorityTable.LEAST_PRIOR));
		stackLid.addAttribute(AttributeKey.LID_MARKER, marker);
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

		List<AstNode> statementNodeList = new LinkedList<AstNode>();
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
	 * @throws VnanoSyntaxException 部分式が空の場合にスローされます。
	 */
	private AstNode[] popPartialExpressionNodes(Deque<AstNode> stack, String marker) throws VnanoSyntaxException {

		List<AstNode> partialExprNodeList = new LinkedList<AstNode>();
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
	 * トークン配列から、指定された値を持つトークンを検索し、最初に見つかったものを返します。
	 *
	 * @param tokens トークン配列
	 * @param tokenValue 検索対象のトークンの値
	 * @param fromIndex 検索を開始する位置のインデックス
	 * @return 見つかったトークンのインデックス（見つからなかった場合は -1）
	 */
	private int getTokenIndex(Token[] tokens, String tokenValue, int fromIndex) {
		int n = tokens.length;
		for(int i=fromIndex; i<n; i++) {
			if (tokens[i].getValue().equals(tokenValue)) {
				return i;
			}
		}
		return -1;
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
			if (depth==0 && word.equals(ScriptWord.ASSIGNMENT)) {
				return -1;
			}

			// 「 [ 」記号があれば階層を上がる
			// 階層0が要素数宣言、1以上は要素数の式中の配列要素アクセス演算子のもの
			if(word.equals(ScriptWord.INDEX_BEGIN)) {
				depth++;

			// 「 ] 」記号があれば階層を下がる
			// （この処理系では、次元区切り「 ][ 」は別種のトークンなのでここにはヒットしない）
			} else if(word.equals(ScriptWord.INDEX_END)) {
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
