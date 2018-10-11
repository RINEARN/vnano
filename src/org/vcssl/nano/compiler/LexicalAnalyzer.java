/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.vcssl.nano.VnanoSyntaxException;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.LiteralSyntax;
import org.vcssl.nano.spec.PriorityTable;
import org.vcssl.nano.spec.ScriptWord;


/**
 * <p>
 * コンパイラ内において、
 * スクリプトコード（文字列）に対して字句解析処理を行い、
 * {@link Token Token} （トークン、字句）の配列へと変換する、字句解析器のクラスです。
 * </p>
 *
 * <p>
 * なお、このクラスの字句解析処理の実装は、現時点では簡易的なものであり、
 * 演算子等において、2文字シンボルの1文字目も必ずシンボルとなるように字句が設定されている事を前提として、
 * 処理を大幅に単純化しています。
 * そのような前提を外すには、最長一致の原則に則った、より汎用的な実装へと置き換える必要があります。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class LexicalAnalyzer {


	/**
	 * このクラスは状態を保持するフィールドを持たないため、コンストラクタは何もしません。
	 */
	public LexicalAnalyzer() {
	}


	/**
	 * ソースコードの文字列をトークン（字句）の単位に分割し、
	 * 各種情報を解析・設定した上で、配列にまとめて返します。
	 *
	 * @param sourceCode ソースコード文字列
	 * @param fileName ソースコードのファイル名
	 * @return 分割・解析されたトークンの配列
	 * @throws VnanoSyntaxException ソースコードの内容に異常があった場合にスローされます。
	 */
	public Token[] analyze(String sourceCode, String fileName) throws VnanoSyntaxException {

		// 最初に、コード内の文字列リテラルを全て "1", "2", ... などのように番号化リテラルで置き換える
		String[] stringLiteralExtractResult = LiteralSyntax.extractStringLiterals(sourceCode);

		// 戻り値の [0] に置き換え済みコードが、[1] 以降に番号に対応する文字列リテラルが格納されている
		String stringLiteralReplacedCode = stringLiteralExtractResult[0];

		// 置き換え済みコードを字句解析し、トークン配列を生成
		Token[] tokens = this.tokenize(stringLiteralReplacedCode, fileName);

		// トークン配列の内容を追加で解析し、トークンタイプや演算子優先度などの情報を付加
		this.analyzeTokenType(tokens);
		this.analyzePriority(tokens);

		// トークン配列が保持する文字列リテラルを復元する
		this.embedStringLiterals(tokens, stringLiteralExtractResult);

		return tokens;
	}


	/**
	 * ソースコードの文字列をトークン（字句）の単位に分割し、配列にまとめて返します。
	 *
	 * （ここに実装の制約について）
	 *
	 * @param sourceCode ソースコード文字列
	 * @param fileName ソースコードのファイル名
	 * @return 分割されたトークンの配列
	 */
	private Token[] tokenize(String sourceCode, String fileName) {


		ArrayList<Token> tokenList = new ArrayList<Token>();
		char[] chars = sourceCode.toCharArray();
		int length = chars.length;
		int pointer = 0;
		int lineNumber = 1; // 行番号は1から始まる

		while(pointer < length) {

			// "&&" が "&" と "&" に分割されてしまう
			// -> 最長一致の原則を入れないと

			// 記号（=トークンの区切り）を検索し、記号がヒットするまでに読んだ文字を繋ぐ
			StringBuilder wordBuilder = new StringBuilder();
			while(!ScriptWord.SYMBOL_SET.contains(Character.toString(chars[pointer])) && pointer<length-1) {
				wordBuilder.append(chars[pointer]);
				pointer++;
			}

			// リテラルや識別子、制御構文などのワードトークンの場合
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
	 * トークン配列を解析し、各要素にトークンタイプを設定します。
	 *
	 * @param tokens 解析・設定対象のトークン配列（情報が追加されます）
	 * @throws VnanoSyntaxException 開き括弧と閉じ括弧の数が合っていない場合にスローされます。
	 */
	private void analyzeTokenType(Token[] tokens) { //throws ScriptCodeException {

		int tokenLength = tokens.length;
		Token lastToken = null;
		String lastWord = null;

		// 括弧の中に入ると可算、出ると減算していく括弧階層カウンタ
		int parenthesisStage = 0;

		// 関数呼び出し演算子が始まった括弧階層を控えて置き、閉じ括弧が関数呼び出しの後端であるかの判定で使用
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
					tokens[i].addAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.CALL);
					tokens[i].addAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.MULTIARY);

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
					tokens[i].addAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.CALL);
					tokens[i].addAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.MULTIARY_END);

				// 部分式の括弧
				} else {
					tokens[i].setType(Token.Type.PARENTHESIS);
				}
				parenthesisStage--;

			// 関数呼び出し演算子内の引数区切りのカンマ（独立したカンマ演算子は非サポート）
			} else if (word.equals(ScriptWord.ARGUMENT_SEPARATOR)) {
				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].addAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.CALL);
				tokens[i].addAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.MULTIARY_SEPARATOR);

			// インデックスの開き括弧「 [ 」
			} else if (word.equals(ScriptWord.INDEX_BEGIN)) {
				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].addAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.INDEX);
				tokens[i].addAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.MULTIARY);

			// 多次元インデックスの区切り「 ][ 」
			// (この処理系では、多次元配列は「配列の配列」ではなく、あくまでもインデックスを複数持つ1個の配列)
			} else if (word.equals(ScriptWord.INDEX_END + ScriptWord.INDEX_BEGIN)) {

				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].addAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.INDEX);
				tokens[i].addAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.MULTIARY_SEPARATOR);

			// インデックスの閉じ括弧「 ] 」
			} else if (word.equals(ScriptWord.INDEX_END)) {

				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].addAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.INDEX);
				tokens[i].addAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.MULTIARY_END);

			// 文末
			} else if (word.equals(ScriptWord.END_OF_STATEMENT)) {
				tokens[i].setType(Token.Type.END_OF_STATEMENT);

			// 代入演算子
			} else if (word.equals(ScriptWord.ASSIGNMENT)) {
				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].addAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.ASSIGNMENT);
				tokens[i].addAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.BINARY);

			// 加減算もしくは符号
			} else if (word.equals(ScriptWord.PLUS) || word.equals(ScriptWord.MINUS)) {

				// 前が「ワードかリテラルか識別子（WORDに分類）か閉じ括弧か配列インデックス閉じ括弧」なら算術二項演算子の加減算
				if (lastToken!=null && (lastToken.getType()==Token.Type.LEAF
						|| lastToken.getValue().equals(ScriptWord.PARENTHESIS_END)
						|| lastToken.getValue().equals(ScriptWord.INDEX_END))) {

					tokens[i].setType(Token.Type.OPERATOR);
					tokens[i].addAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.ARITHMETIC);
					tokens[i].addAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.BINARY);

				// そうでなければ符号演算子の単項プラスマイナス
				} else {
					tokens[i].setType(Token.Type.OPERATOR);
					tokens[i].addAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.ARITHMETIC);
					tokens[i].addAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.PREFIX);
				}

			// 	上記の加減算以外の算術二項演算子
			} else if (word.equals(ScriptWord.MULTIPLICATION)
					|| word.equals(ScriptWord.DIVISION)
					|| word.equals(ScriptWord.REMAINDER)) {

				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].addAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.ARITHMETIC);
				tokens[i].addAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.BINARY);

			// 	算術系の複合代入演算子
			} else if (word.equals(ScriptWord.ADDITION_ASSIGNMENT)
					|| word.equals(ScriptWord.SUBTRACTION_ASSIGNMENT)
					|| word.equals(ScriptWord.MULTIPLICATION_ASSIGNMENT)
					|| word.equals(ScriptWord.DIVISION_ASSIGNMENT)) {

				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].addAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT);
				tokens[i].addAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.BINARY);

			// 	比較二項演算子
			} else if (word.equals(ScriptWord.EQUAL)
					|| word.equals(ScriptWord.NOT_EQUAL)
					|| word.equals(ScriptWord.GRATER_THAN)
					|| word.equals(ScriptWord.GRATER_EQUAL)
					|| word.equals(ScriptWord.LESS_THAN)
					|| word.equals(ScriptWord.LESS_EQUAL)) {

				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].addAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.COMPARISON);
				tokens[i].addAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.BINARY);

			// 	論理二項演算子
			} else if (word.equals(ScriptWord.AND)
					|| word.equals(ScriptWord.OR)) {

				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].addAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.LOGICAL);
				tokens[i].addAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.BINARY);

			// 論理前置演算子
			} else if (word.equals(ScriptWord.NOT)) {
				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].addAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.LOGICAL);
				tokens[i].addAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.PREFIX);

			// インクリメントとデクリメント
			} else if (word.equals(ScriptWord.INCREMENT) || word.equals(ScriptWord.DECREMENT)) {

				// 前が識別子(WORDに分類)かリテラルか後置演算子(INDEXなど)なら後置インクリメント/デクリメント、そうでなければ前置インクリメント/デクリメント
				if (lastToken!=null && ( lastToken.getType() == Token.Type.LEAF
						|| lastToken.getType() == Token.Type.OPERATOR
						&& lastToken.getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.POSTFIX) ) ) {

					tokens[i].setType(Token.Type.OPERATOR);
					tokens[i].addAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.ARITHMETIC);
					tokens[i].addAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.POSTFIX);
				} else {
					tokens[i].setType(Token.Type.OPERATOR);
					tokens[i].addAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.ARITHMETIC);
					tokens[i].addAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.PREFIX);
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
					tokens[i].addAttribute(AttributeKey.LEAF_TYPE, AttributeValue.LITERAL);
				// 次のトークンが「 ( 」記号 -> 関数識別子
				} else if (i<tokenLength-1 && tokens[i+1].getValue().equals(ScriptWord.PARENTHESIS_BEGIN)) {
					tokens[i].addAttribute(AttributeKey.LEAF_TYPE, AttributeValue.FUNCTION_IDENTIFIER);
				// それ以外は変数識別子
				} else {
					tokens[i].addAttribute(AttributeKey.LEAF_TYPE, AttributeValue.VARIABLE_IDENTIFIER);
				}
			}
			lastToken = tokens[i];
			lastWord = word;
		}
	}


	/**
	 * トークン配列を解析し、各要素に演算子としての優先度を設定します。
	 *
	 * @param tokens 解析・設定対象のトークン配列
	 */
	private void analyzePriority(Token[] tokens) {
		int length = tokens.length;
		for(int i=0; i<length; i++) {

			String symbol = tokens[i].getValue();
			switch (symbol) {

				case ScriptWord.PARENTHESIS_BEGIN:
					//if (1<=i && tokens[i-1].getType()==Token.Type.LEAF) {
					if (tokens[i].getType() == Token.Type.OPERATOR) { // 関数コールの場合
						tokens[i].setPriority(PriorityTable.CALL_BEGIN);
					} else {
						tokens[i].setPriority(PriorityTable.PARENTHESIS_BEGIN);
					}
					break;

				case ScriptWord.PARENTHESIS_END:
					if (tokens[i].getType() == Token.Type.OPERATOR) { // 関数コールの場合
						tokens[i].setPriority(PriorityTable.CALL_END); // MULTIARY系演算子の終端は優先度最低にする必要がある(そうしないと結合してしまう)
					} else {
						tokens[i].setPriority(PriorityTable.PARENTHESIS_END);
					}

				case ScriptWord.ARGUMENT_SEPARATOR :
					tokens[i].setPriority(PriorityTable.CALL_SEPARATOR); // 引数区切りのカンマも優先度最低にする必要がある(そうしないと結合してしまう)
					break;

				case ScriptWord.INDEX_BEGIN:
					tokens[i].setPriority(PriorityTable.INDEX_BEGIN);
					break;

				case ScriptWord.INDEX_END:
					tokens[i].setPriority(PriorityTable.INDEX_END); // MULTIARY系演算子の終端は優先度最低にする必要がある(そうしないと結合してしまう)
					break;

				case ScriptWord.INDEX_SEPARATOR :
					tokens[i].setPriority(PriorityTable.INDEX_SEPARATOR); // 次元区切りのカンマも優先度最低にする必要がある(そうしないと結合してしまう)
					break;

				case ScriptWord.INCREMENT:
					if (tokens[i].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.PREFIX)) {
						tokens[i].setPriority(PriorityTable.PREFIX_INCREMENT); //前置インクリメント
					} else {
						tokens[i].setPriority(PriorityTable.POSTFIX_INCREMENT); //後置インクリメント
					}
					break;

				case ScriptWord.DECREMENT:
					if (tokens[i].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.PREFIX)) {
						tokens[i].setPriority(PriorityTable.PREFIX_DECREMENT); //前置デクリメント
					} else {
						tokens[i].setPriority(PriorityTable.POSTFIX_DECREMENT); //後置デクリメント
					}
					break;

				case ScriptWord.NOT:
					tokens[i].setPriority(PriorityTable.NOT);
					break;

				case ScriptWord.PLUS:
					if (tokens[i].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.PREFIX)) {
						tokens[i].setPriority(PriorityTable.PREFIX_PLUS); //単項プラス
					} else {
						tokens[i].setPriority(PriorityTable.ADDITION); //加算
					}
					break;

				case ScriptWord.MINUS:
					if (tokens[i].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.PREFIX)) {
						tokens[i].setPriority(PriorityTable.PREFIX_MINUS); //単項マイナス
					} else {
						tokens[i].setPriority(PriorityTable.SUBTRACTION); //減算
					}
					break;

				case ScriptWord.MULTIPLICATION:
					tokens[i].setPriority(PriorityTable.MULTIPLICATION);
					break;

				case ScriptWord.DIVISION:
					tokens[i].setPriority(PriorityTable.DIVISION);
					break;

				case ScriptWord.REMAINDER:
					tokens[i].setPriority(PriorityTable.REMAINDER);
					break;

				case ScriptWord.LESS_THAN:
					tokens[i].setPriority(PriorityTable.LESS_THAN);
					break;

				case ScriptWord.LESS_EQUAL:
					tokens[i].setPriority(PriorityTable.LESS_EQUAL);
					break;

				case ScriptWord.GRATER_THAN:
					tokens[i].setPriority(PriorityTable.GRATER_THAN);
					break;

				case ScriptWord.GRATER_EQUAL:
					tokens[i].setPriority(PriorityTable.GRATER_EQUAL);
					break;

				case ScriptWord.EQUAL:
					tokens[i].setPriority(PriorityTable.EQUAL);
					break;

				case ScriptWord.NOT_EQUAL:
					tokens[i].setPriority(PriorityTable.NOT_EQUAL);
					break;

				case ScriptWord.AND:
					tokens[i].setPriority(PriorityTable.AND);
					break;

				case ScriptWord.OR:
					tokens[i].setPriority(PriorityTable.OR);
					break;

				case ScriptWord.ASSIGNMENT:
					tokens[i].setPriority(PriorityTable.ASSIGNMENT);
					break;

				case ScriptWord.ADDITION_ASSIGNMENT:
					tokens[i].setPriority(PriorityTable.ADDITION_ASSIGNMENT);
					break;

				case ScriptWord.SUBTRACTION_ASSIGNMENT:
					tokens[i].setPriority(PriorityTable.SUBTRACTION_ASSIGNMENT);
					break;

				case ScriptWord.MULTIPLICATION_ASSIGNMENT:
					tokens[i].setPriority(PriorityTable.MULTIPLICATION_ASSIGNMENT);
					break;

				case ScriptWord.DIVISION_ASSIGNMENT:
					tokens[i].setPriority(PriorityTable.DIVISION_ASSIGNMENT);
					break;

				case ScriptWord.REMAINDER_ASSIGNMENT:
					tokens[i].setPriority(PriorityTable.REMAINDER_ASSIGNMENT);
					break;

				default : {
					break;
				}
			}
		}
	}


	/**
	 * トークン配列内の文字列トークンが保持する、前処理で番号化された文字列リテラル（"1", "2", ... 等）を、
	 * 本来の文字列リテラルで置き換えます。
	 *
	 * 番号化された文字列リテラルの番号をインデックスとする配列要素に、
	 * 本来の文字列リテラルを格納している配列を、引数 stringLiteralExtractResult に指定してください。
	 *
	 * なお、番号リテラルの番号は、トークン配列内で、
	 * 登場順に1番から1ずつ増えていく昇順で割りふられている必要があります。
	 *
	 * @param tokens 処理対象のトークン配列（内容が変更されます）
	 * @param stringLiteralExtractResult 番号化された文字列リテラルの番号
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

				// この条件下では、value は "1", "2" などのように番号化された文字列リテラルが入っている

				// 元のリテラル値が stringLiteralExtractResult 配列に格納されているインデックスを取得（番号化リテラルの番号）
				int index = LiteralSyntax.getIndexOfNumberedStringLiteral(value);

				// 番号化リテラルを、本来の文字列リテラルで置き換える
				token.setValue(stringLiteralExtractResult[index]);
			}
		}
	}
}
