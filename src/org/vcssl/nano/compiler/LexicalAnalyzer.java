/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.LiteralSyntax;
import org.vcssl.nano.spec.OperatorPrecedence;
import org.vcssl.nano.spec.ScriptWord;

//Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/compiler/LexicalAnalyzer.html
//ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/compiler/LexicalAnalyzer.html

/**
 * <p>
 * <span class="lang-en">
 * The class performing the function of the lexical analyzer in the compiler of the Vnano
 * </span>
 * <span class="lang-ja">
 * Vnano のコンパイラ内において, レキシカルアナライザ（字句解析器）の機能を担うクラスです
 * </span>
 * .
 * </p>
 *
 * <p>
 * <span class="lang-en">
 * The lexical analysis of this class takes script code as the input,
 * then split it into tokens and outputs them.
 * In addition, in the analysis, some attributes are analyzed and set for tokens,
 * for example: types of tokens, data types of literals, precedences of operators, and so on.
 * </span>
 * <span class="lang-ja">
 * このクラスが行う字句解析処理は, 入力としてスクリプトコードを受け取り,
 * それをトークンの列に分割して出力します.
 * その過程で, トークンのタイプや, リテラルのデータ型, および演算子の優先度など,
 * いくつかの属性が分析され, 各トークンに設定されます.
 * </span>
 * </p>
 *
 * <p>
 * <span class="lang-ja">
 * なお, このクラスの字句解析処理の実装は, 現時点では簡易的なものであり,
 * 演算子等において, 2文字シンボルの1文字目も必ずシンボルとなるように字句が設定されている事を前提として,
 * 処理を大幅に単純化しています.
 * そのような前提を外すには, 最長一致の原則に則った, より汎用的な実装へと置き換える必要があります.
 * </span>
 * </p>
 *
 * <p>
 * &raquo; <a href="../../../../../src/org/vcssl/nano/compiler/LexicalAnalyzer.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/compiler/LexicalAnalyzer.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/compiler/LexicalAnalyzer.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class LexicalAnalyzer {

	/**
	 * <span class="lang-en">
	 * This constructor does nothing, because this class has no fields for storing state
	 * </span>
	 * <span class="lang-ja">
	 * このクラスは状態を保持するフィールドを持たないため, コンストラクタは何もしません
	 * </span>
	 * .
	 */
	public LexicalAnalyzer() {
	}


	/**
	 * <span class="lang-en">
	 * Splits code of the script into tokens, and returns them after analyzing and setting their attributes
	 * </span>
	 * <span class="lang-ja">
	 * スクリプトのコードをトークンの列に分割し, 各トークンに属性値を設定した上で返します
	 * </span>
	 * .
	 * @param script
	 *   <span class="lang-en">The script to be processed.</span>
	 *   <span class="lang-ja">処理対象のスクリプト.</span>
	 *
	 * @param fileName
	 *   <span class="lang-en">The filename of the script to be processed.</span>
	 *   <span class="lang-ja">処理対象のスクリプトのファイル名.</span>
	 *
	 * @return
	 *   <span class="lang-en">Tokens.</span>
	 *   <span class="lang-ja">トークンの列.</span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">Thrown when any syntax error has detected.</span>
	 *   <span class="lang-ja">構文エラーが検出された場合にスローされます.</span>
	 */
	public Token[] analyze(String script, String fileName) throws VnanoException {

		// 最初に, コード内の文字列リテラルを全て "1", "2", ... などのように番号化リテラルで置き換える
		String[] stringLiteralExtractResult = LiteralSyntax.extractStringLiterals(script);

		// 戻り値の [0] に置き換え済みコードが, [1] 以降に番号に対応する文字列リテラルが格納されている
		String stringLiteralReplacedCode = stringLiteralExtractResult[0];

		// 置き換え済みコードを字句解析し, トークン配列を生成
		Token[] tokens = this.tokenize(stringLiteralReplacedCode, fileName);

		// トークン配列の内容を追加で解析し, トークンタイプや演算子の優先度/結合性, リテラルのデータ型などの情報を付加
		this.analyzeTokenTypes(tokens);         // トークンタイプ
		this.analyzePrecedences(tokens);          // 演算子の優先度
		this.analyzeAssociativities(tokens);     // 演算子の結合性
		this.analyzeLiteralAttributes(tokens); // リテラルのデータ型

		// トークン配列が保持する文字列リテラルを復元する
		this.embedStringLiterals(tokens, stringLiteralExtractResult);

		return tokens;
	}


	/**
	 * <span class="lang-en">Splits code of the script into tokens</span>
	 * <span class="lang-ja">ソースコードの文字列をトークン（字句）の単位に分割し, 配列にまとめて返します</span>
	 * .
	 * @param script
	 *   <span class="lang-en">The script to be processed.</span>
	 *   <span class="lang-ja">処理対象のスクリプト.</span>
	 *
	 * @param fileName
	 *   <span class="lang-en">The filename of the script to be processed.</span>
	 *   <span class="lang-ja">処理対象のスクリプトのファイル名.</span>
	 *
	 * @return
	 *   <span class="lang-en">Tokens.</span>
	 *   <span class="lang-ja">トークンの列.</span>
	 */
	private Token[] tokenize(String script, String fileName) {


		ArrayList<Token> tokenList = new ArrayList<Token>();
		char[] chars = script.toCharArray();
		int length = chars.length;
		int pointer = 0;
		int lineNumber = 1; // 行番号は1から始まる

		while(pointer < length) {

			// "&&" が "&" と "&" に分割されてしまう
			// -> 最長一致の原則を入れないと

			// 記号（=トークンの区切り）を検索し, 記号がヒットするまでに読んだ文字を繋ぐ
			StringBuilder wordBuilder = new StringBuilder();
			while(!ScriptWord.SYMBOL_SET.contains(Character.toString(chars[pointer])) && pointer<length-1) {
				wordBuilder.append(chars[pointer]);
				pointer++;
			}

			// リテラルや識別子, 制御構文などのワードトークンの場合
			if(0 < wordBuilder.length()) {

				tokenList.add(new Token(wordBuilder.toString(), lineNumber, fileName));
				continue;

			// 2文字記号トークンの場合（1文字先読みして判定）
			} else if (pointer<length-1
						&& ScriptWord.SYMBOL_SET.contains(new String(new char[]{chars[pointer],chars[pointer+1]}))) {

				tokenList.add(new Token(
					new String(new char[]{chars[pointer], chars[pointer+1]}),
					lineNumber, fileName
				));
				pointer += 2;	//1文字先読みしたので2つ加算
				continue;

			// 1文字記号トークンの場合
			} else if (!Character.toString(chars[pointer]).matches(ScriptWord.TOKEN_SEPARATOR_REGEX)) {

				tokenList.add(new Token(
					Character.toString(chars[pointer]),
					lineNumber, fileName
				));
				pointer++;
				continue;

			// 空白や改行などの無視する記号の場合
			} else {
				if (chars[pointer] == '\n') {
					lineNumber++;
				}
				pointer++;
				continue;
			}
		}
		Token[] tokens = (Token[])tokenList.toArray(new Token[tokenList.size()]);
		return tokens;
	}


	/**
	 * <span class="lang-en">Analyze types of tokens and set to them</span>
	 * <span class="lang-ja">トークンタイプを分析し, 各トークンに設定します</span>
	 * .
	 * @parak tokens
	 *   <span class="lang-en">Tokens to be analyzed.</span>
	 *   <span class="lang-ja">解析対象のトークン列.</span>
	 */
	private void analyzeTokenTypes(Token[] tokens) {

		int tokenLength = tokens.length;
		Token lastToken = null;
		String lastWord = null;

		// 括弧の中に入ると可算, 出ると減算していく括弧階層カウンタ
		int parenthesisStage = 0;

		// 関数呼び出し演算子が始まった括弧階層を控えて置き, 閉じ括弧が関数呼び出しの後端であるかの判定で使用
		Set<Integer> callParenthesisStages = new HashSet<Integer>();

		for(int i=0; i<tokenLength; i++) {
			String word = tokens[i].getValue();

			// ブロック
			if (word.equals(ScriptWord.BLOCK_BEGIN) || word.equals(ScriptWord.BLOCK_END)) {
				tokens[i].setType(Token.Type.BLOCK);

			// 開き括弧
			} else if (word.equals(ScriptWord.PARENTHESIS_BEGIN)) {
				parenthesisStage++;

				// 関数呼び出し演算子の括弧
				if (lastToken!=null && lastToken.getType()==Token.Type.LEAF
						&& lastToken.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.FUNCTION_IDENTIFIER)
						&& !ScriptWord.STATEMENT_NAME_SET.contains(lastWord)) {

					callParenthesisStages.add(parenthesisStage);
					tokens[i].setType(Token.Type.OPERATOR);
					tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.CALL);
					tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.MULTIARY);

				// 部分式の括弧
				} else {
					tokens[i].setType(Token.Type.PARENTHESIS);
				}

			// 閉じ括弧
			} else if (word.equals(ScriptWord.PARENTHESIS_END)) {

				// 関数呼び出し演算子の括弧
				if (callParenthesisStages.contains(parenthesisStage)) {
					callParenthesisStages.remove(parenthesisStage);
					tokens[i].setType(Token.Type.OPERATOR);
					tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.CALL);
					tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.MULTIARY_END);

				// 部分式の括弧
				} else {
					tokens[i].setType(Token.Type.PARENTHESIS);
				}
				parenthesisStage--;

			// 関数呼び出し演算子内の引数区切りのカンマ（独立したカンマ演算子は非サポート）
			} else if (word.equals(ScriptWord.ARGUMENT_SEPARATOR)) {
				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.CALL);
				tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.MULTIARY_SEPARATOR);

			// インデックスの開き括弧「 [ 」
			} else if (word.equals(ScriptWord.INDEX_BEGIN)) {
				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.INDEX);
				tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.MULTIARY);

			// 多次元インデックスの区切り「 ][ 」
			// (この処理系では, 多次元配列は「配列の配列」ではなく, あくまでもインデックスを複数持つ1個の配列)
			} else if (word.equals(ScriptWord.INDEX_END + ScriptWord.INDEX_BEGIN)) {

				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.INDEX);
				tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.MULTIARY_SEPARATOR);

			// インデックスの閉じ括弧「 ] 」
			} else if (word.equals(ScriptWord.INDEX_END)) {

				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.INDEX);
				tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.MULTIARY_END);

			// 文末
			} else if (word.equals(ScriptWord.END_OF_STATEMENT)) {
				tokens[i].setType(Token.Type.END_OF_STATEMENT);

			// 代入演算子
			} else if (word.equals(ScriptWord.ASSIGNMENT)) {
				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.ASSIGNMENT);
				tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.BINARY);

			// 加減算もしくは符号
			} else if (word.equals(ScriptWord.PLUS) || word.equals(ScriptWord.MINUS)) {

				// 前が「ワードかリテラルか識別子（WORDに分類）か閉じ括弧か配列インデックス閉じ括弧」なら算術二項演算子の加減算
				if (lastToken!=null && (lastToken.getType()==Token.Type.LEAF
						|| lastToken.getValue().equals(ScriptWord.PARENTHESIS_END)
						|| lastToken.getValue().equals(ScriptWord.INDEX_END))) {

					tokens[i].setType(Token.Type.OPERATOR);
					tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.ARITHMETIC);
					tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.BINARY);

				// そうでなければ符号演算子の単項プラスマイナス
				} else {
					tokens[i].setType(Token.Type.OPERATOR);
					tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.ARITHMETIC);
					tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.PREFIX);
				}

			// 	上記の加減算以外の算術二項演算子
			} else if (word.equals(ScriptWord.MULTIPLICATION)
					|| word.equals(ScriptWord.DIVISION)
					|| word.equals(ScriptWord.REMAINDER)) {

				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.ARITHMETIC);
				tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.BINARY);

			// 	算術系の複合代入演算子
			} else if (word.equals(ScriptWord.ADDITION_ASSIGNMENT)
					|| word.equals(ScriptWord.SUBTRACTION_ASSIGNMENT)
					|| word.equals(ScriptWord.MULTIPLICATION_ASSIGNMENT)
					|| word.equals(ScriptWord.DIVISION_ASSIGNMENT)) {

				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT);
				tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.BINARY);

			// 	比較二項演算子
			} else if (word.equals(ScriptWord.EQUAL)
					|| word.equals(ScriptWord.NOT_EQUAL)
					|| word.equals(ScriptWord.GRATER_THAN)
					|| word.equals(ScriptWord.GRATER_EQUAL)
					|| word.equals(ScriptWord.LESS_THAN)
					|| word.equals(ScriptWord.LESS_EQUAL)) {

				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.COMPARISON);
				tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.BINARY);

			// 	論理二項演算子
			} else if (word.equals(ScriptWord.AND)
					|| word.equals(ScriptWord.OR)) {

				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.LOGICAL);
				tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.BINARY);

			// 論理前置演算子
			} else if (word.equals(ScriptWord.NOT)) {
				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.LOGICAL);
				tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.PREFIX);

			// インクリメントとデクリメント
			} else if (word.equals(ScriptWord.INCREMENT) || word.equals(ScriptWord.DECREMENT)) {

				// 前が識別子(WORDに分類)かリテラルか後置演算子(INDEXなど)なら後置インクリメント/デクリメント, そうでなければ前置インクリメント/デクリメント
				if (lastToken!=null && ( lastToken.getType() == Token.Type.LEAF
						|| lastToken.getType() == Token.Type.OPERATOR
						&& lastToken.getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.POSTFIX) ) ) {

					tokens[i].setType(Token.Type.OPERATOR);
					tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.ARITHMETIC);
					tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.POSTFIX);
				} else {
					tokens[i].setType(Token.Type.OPERATOR);
					tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.ARITHMETIC);
					tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.PREFIX);
				}

			} else if (word.equals(ScriptWord.IF)
					|| word.equals(ScriptWord.ELSE)
					|| word.equals(ScriptWord.FOR)
					|| word.equals(ScriptWord.WHILE)
					|| word.equals(ScriptWord.RETURN)
					|| word.equals(ScriptWord.BREAK)
					|| word.equals(ScriptWord.CONTINUE)) {

				tokens[i].setType(Token.Type.CONTROL);

			// データ型名
			} else if (DataTypeName.isDataTypeName(word)) {
				tokens[i].setType(Token.Type.DATA_TYPE);

			// リテラルか識別子などのリーフ要素
			} else {
				tokens[i].setType(Token.Type.LEAF);

				// リテラルとして有効な内容 -> リテラル
				if (LiteralSyntax.isValidLiteral(word)) {
					tokens[i].setAttribute(AttributeKey.LEAF_TYPE, AttributeValue.LITERAL);
				// 次のトークンが「 ( 」記号 -> 関数識別子
				} else if (i<tokenLength-1 && tokens[i+1].getValue().equals(ScriptWord.PARENTHESIS_BEGIN)) {
					tokens[i].setAttribute(AttributeKey.LEAF_TYPE, AttributeValue.FUNCTION_IDENTIFIER);
				// それ以外は変数識別子
				} else {
					tokens[i].setAttribute(AttributeKey.LEAF_TYPE, AttributeValue.VARIABLE_IDENTIFIER);
				}
			}
			lastToken = tokens[i];
			lastWord = word;
		}
	}


	/**
	 * <span class="lang-en">Analyze precedences of operator-tokens and set to them</span>
	 * <span class="lang-ja">演算子トークンの優先度を分析し, 各トークンに設定します</span>
	 * .
	 * @parak tokens
	 *   <span class="lang-en">Tokens to be analyzed.</span>
	 *   <span class="lang-ja">解析対象のトークン列.</span>
	 */
	private void analyzePrecedences(Token[] tokens) {
		int length = tokens.length;
		for(int i=0; i<length; i++) {

			String symbol = tokens[i].getValue();
			switch (symbol) {

				case ScriptWord.PARENTHESIS_BEGIN:
					//if (1<=i && tokens[i-1].getType()==Token.Type.LEAF) {
					if (tokens[i].getType() == Token.Type.OPERATOR) { // 関数コールの場合
						tokens[i].setPrecedence(OperatorPrecedence.CALL_BEGIN);
					} else {
						tokens[i].setPrecedence(OperatorPrecedence.PARENTHESIS_BEGIN);
					}
					break;

				case ScriptWord.PARENTHESIS_END:
					if (tokens[i].getType() == Token.Type.OPERATOR) { // 関数コールの場合
						tokens[i].setPrecedence(OperatorPrecedence.CALL_END); // MULTIARY系演算子の終端は優先度最低にする必要がある(そうしないと結合してしまう)
					} else {
						tokens[i].setPrecedence(OperatorPrecedence.PARENTHESIS_END);
					}

				case ScriptWord.ARGUMENT_SEPARATOR :
					tokens[i].setPrecedence(OperatorPrecedence.CALL_SEPARATOR); // 引数区切りのカンマも優先度最低にする必要がある(そうしないと結合してしまう)
					break;

				case ScriptWord.INDEX_BEGIN:
					tokens[i].setPrecedence(OperatorPrecedence.INDEX_BEGIN);
					break;

				case ScriptWord.INDEX_END:
					tokens[i].setPrecedence(OperatorPrecedence.INDEX_END); // MULTIARY系演算子の終端は優先度最低にする必要がある(そうしないと結合してしまう)
					break;

				case ScriptWord.INDEX_SEPARATOR :
					tokens[i].setPrecedence(OperatorPrecedence.INDEX_SEPARATOR); // 次元区切りのカンマも優先度最低にする必要がある(そうしないと結合してしまう)
					break;

				case ScriptWord.INCREMENT:
					if (tokens[i].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.PREFIX)) {
						tokens[i].setPrecedence(OperatorPrecedence.PREFIX_INCREMENT); //前置インクリメント
					} else {
						tokens[i].setPrecedence(OperatorPrecedence.POSTFIX_INCREMENT); //後置インクリメント
					}
					break;

				case ScriptWord.DECREMENT:
					if (tokens[i].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.PREFIX)) {
						tokens[i].setPrecedence(OperatorPrecedence.PREFIX_DECREMENT); //前置デクリメント
					} else {
						tokens[i].setPrecedence(OperatorPrecedence.POSTFIX_DECREMENT); //後置デクリメント
					}
					break;

				case ScriptWord.NOT:
					tokens[i].setPrecedence(OperatorPrecedence.NOT);
					break;

				case ScriptWord.PLUS:
					if (tokens[i].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.PREFIX)) {
						tokens[i].setPrecedence(OperatorPrecedence.PREFIX_PLUS); //単項プラス
					} else {
						tokens[i].setPrecedence(OperatorPrecedence.ADDITION); //加算
					}
					break;

				case ScriptWord.MINUS:
					if (tokens[i].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.PREFIX)) {
						tokens[i].setPrecedence(OperatorPrecedence.PREFIX_MINUS); //単項マイナス
					} else {
						tokens[i].setPrecedence(OperatorPrecedence.SUBTRACTION); //減算
					}
					break;

				case ScriptWord.MULTIPLICATION:
					tokens[i].setPrecedence(OperatorPrecedence.MULTIPLICATION);
					break;

				case ScriptWord.DIVISION:
					tokens[i].setPrecedence(OperatorPrecedence.DIVISION);
					break;

				case ScriptWord.REMAINDER:
					tokens[i].setPrecedence(OperatorPrecedence.REMAINDER);
					break;

				case ScriptWord.LESS_THAN:
					tokens[i].setPrecedence(OperatorPrecedence.LESS_THAN);
					break;

				case ScriptWord.LESS_EQUAL:
					tokens[i].setPrecedence(OperatorPrecedence.LESS_EQUAL);
					break;

				case ScriptWord.GRATER_THAN:
					tokens[i].setPrecedence(OperatorPrecedence.GRATER_THAN);
					break;

				case ScriptWord.GRATER_EQUAL:
					tokens[i].setPrecedence(OperatorPrecedence.GRATER_EQUAL);
					break;

				case ScriptWord.EQUAL:
					tokens[i].setPrecedence(OperatorPrecedence.EQUAL);
					break;

				case ScriptWord.NOT_EQUAL:
					tokens[i].setPrecedence(OperatorPrecedence.NOT_EQUAL);
					break;

				case ScriptWord.AND:
					tokens[i].setPrecedence(OperatorPrecedence.AND);
					break;

				case ScriptWord.OR:
					tokens[i].setPrecedence(OperatorPrecedence.OR);
					break;

				case ScriptWord.ASSIGNMENT:
					tokens[i].setPrecedence(OperatorPrecedence.ASSIGNMENT);
					break;

				case ScriptWord.ADDITION_ASSIGNMENT:
					tokens[i].setPrecedence(OperatorPrecedence.ADDITION_ASSIGNMENT);
					break;

				case ScriptWord.SUBTRACTION_ASSIGNMENT:
					tokens[i].setPrecedence(OperatorPrecedence.SUBTRACTION_ASSIGNMENT);
					break;

				case ScriptWord.MULTIPLICATION_ASSIGNMENT:
					tokens[i].setPrecedence(OperatorPrecedence.MULTIPLICATION_ASSIGNMENT);
					break;

				case ScriptWord.DIVISION_ASSIGNMENT:
					tokens[i].setPrecedence(OperatorPrecedence.DIVISION_ASSIGNMENT);
					break;

				case ScriptWord.REMAINDER_ASSIGNMENT:
					tokens[i].setPrecedence(OperatorPrecedence.REMAINDER_ASSIGNMENT);
					break;

				default : {
					break;
				}
			}
		}
	}


	/**
	 * <span class="lang-en">Analyze associativities of operator-tokens and set to them</span>
	 * <span class="lang-ja">演算子トークンの結合性を分析し, 各トークンに設定します</span>
	 * .
	 * @parak tokens
	 *   <span class="lang-en">Tokens to be analyzed.</span>
	 *   <span class="lang-ja">解析対象のトークン列.</span>
	 */
	private void analyzeAssociativities(Token[] tokens) {
		int length = tokens.length;
		for(int i=0; i<length; i++) {

			// 演算子トークンのみが解析対象なので, それ以外のトークンはスキップ
			if (tokens[i].getType() != Token.Type.OPERATOR) {
				continue;
			}

			// 大半の演算子は左結合なので, デフォルトを左結合とし, 右結合の演算子のみを抽出して設定する
			String associativity = AttributeValue.LEFT;

			String symbol = tokens[i].getValue();
			switch (symbol) {

				// 前置インクリメント
				case ScriptWord.INCREMENT : {
					if (tokens[i].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.PREFIX)) {
						associativity = AttributeValue.RIGHT;
					}
					break;
				}
				// 前置デクリメント
				case ScriptWord.DECREMENT : {
					if (tokens[i].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.PREFIX)) {
						associativity = AttributeValue.RIGHT;
					}
					break;
				}
				// 論理否定
				case ScriptWord.NOT : {
					associativity = AttributeValue.RIGHT;
					break;
				}
				// 単項プラス
				case ScriptWord.PLUS : {
					if (tokens[i].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.PREFIX)) {
						associativity = AttributeValue.RIGHT;
					}
					break;
				}
				// 単項マイナス
				case ScriptWord.MINUS : {
					if (tokens[i].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.PREFIX)) {
						associativity = AttributeValue.RIGHT;
					}
					break;
				}
				// 代入
				case ScriptWord.ASSIGNMENT : {
					associativity = AttributeValue.RIGHT;
					break;
				}
				// 加算代入
				case ScriptWord.ADDITION_ASSIGNMENT : {
					associativity = AttributeValue.RIGHT;
					break;
				}
				// 減算代入
				case ScriptWord.SUBTRACTION_ASSIGNMENT : {
					associativity = AttributeValue.RIGHT;
					break;
				}
				// 乗算代入
				case ScriptWord.MULTIPLICATION_ASSIGNMENT : {
					associativity = AttributeValue.RIGHT;
					break;
				}
				// 除算代入
				case ScriptWord.DIVISION_ASSIGNMENT : {
					associativity = AttributeValue.RIGHT;
					break;
				}
				// 剰余算代入
				case ScriptWord.REMAINDER_ASSIGNMENT : {
					associativity = AttributeValue.RIGHT;
					break;
				}
				default : {
					break;
				}
			}

			// 求めた結合性情報を, トークンに属性値として設定
			tokens[i].setAttribute(AttributeKey.OPERATOR_ASSOCIATIVITY, associativity);
		}
	}


	/**
	 * <span class="lang-en">Analyze data types of literal-tokens and set to them</span>
	 * <span class="lang-ja">リテラルトークンのデータ型を分析し, 各トークンに設定します</span>
	 * .
	 * @parak tokens
	 *   <span class="lang-en">Tokens to be analyzed.</span>
	 *   <span class="lang-ja">解析対象のトークン列.</span>
	 */
	private void analyzeLiteralAttributes(Token[] tokens) {
		for (Token token: tokens) {
			if (token.getType() == Token.Type.LEAF
				&& token.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.LITERAL)) {

				String literal = token.getValue();
				String dataTypeName = LiteralSyntax.getDataTypeNameOfLiteral(literal);
				token.setAttribute(AttributeKey.DATA_TYPE, dataTypeName);
				token.setAttribute(AttributeKey.RANK, "0"); // 現状では配列のリテラルは存在しないため, 常にスカラ
			}
		}
	}


	/**
	 * <span class="lang-ja">
	 * トークン配列内の文字列トークンが保持する, 前処理で番号化された文字列リテラル（"1", "2", ... 等）を,
	 * 本来の文字列リテラルで置き換えます.
	 *
	 * 番号化された文字列リテラルの番号をインデックスとする配列要素に,
	 * 本来の文字列リテラルを格納している配列を, 引数 stringLiteralExtractResult に指定してください.
	 *
	 * なお, 番号リテラルの番号は, トークン配列内で,
	 * 登場順に1番から1ずつ増えていく昇順で割りふられている必要があります.
	 * </span>
	 *
	 * @param tokens
	 *   <span class="lang-ja">処理対象のトークン配列（内容が変更されます）.</span>
	 * @param
	 *   <span class="lang-ja">stringLiteralExtractResult 番号化された文字列リテラルの番号.</span>
	 */
	private void embedStringLiterals(Token[] tokens, String[] stringLiteralExtractResult) {
		int tokenLength = tokens.length;

		// トークンを先頭要素から順に見ていく
		for (int tokenIndex=0; tokenIndex<tokenLength; tokenIndex++) {
			Token token = tokens[tokenIndex];
			String value = token.getValue();

			// 文字列リテラルトークンの場合
			if (token.getType() == Token.Type.LEAF
					&& token.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.LITERAL)
					&& LiteralSyntax.getDataTypeNameOfLiteral(value).equals(DataTypeName.STRING)) {

				// この条件下では, value は "1", "2" などのように番号化された文字列リテラルが入っている

				// 元のリテラル値が stringLiteralExtractResult 配列に格納されているインデックスを取得（番号化リテラルの番号）
				int index = LiteralSyntax.getIndexOfNumberedStringLiteral(value);

				// 番号化リテラルを, 本来の文字列リテラルで置き換える
				token.setValue(stringLiteralExtractResult[index]);
			}
		}
	}
}
