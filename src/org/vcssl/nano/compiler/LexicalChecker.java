/*
 * Copyright(C) 2018-2021 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.ScriptWord;

public class LexicalChecker {

	/**
	 * <span class="lang-en">
	 * Create a new lexical checker
	 * </span>
	 * <span class="lang-ja">
	 * レキシカルチェッカーを生成します
	 * </span>
	 * .
	 */
	public LexicalChecker() {
	}


	/**
	 * トークン配列に対して、if/else/for/whileの制御文の後方に、
	 * 必要な括弧やブロック（この言語では必須）などが存在する正しい構造になっているかを検査します。
	 * 検査の結果、問題が無かった場合には何もせず、問題が見つかった場合には例外をスローします。
	 *
	 * @param tokens 検査対象のトークン配列
	 * @param controlStatementBegin 対象の制御文の始点インデックス
	 * @throws VnanoException トークン配列に、制御文の構成トークンとしての問題があった場合にスローされます。
	 */
	protected void checkTokensAfterControlStatement(
			Token[] tokens, int controlStatementBegin, boolean arrowStatementsInParentheses)
					throws VnanoException {

		// 「 if 」や「 for 」などの制御文のキーワードのトークン
		Token controlToken = tokens[controlStatementBegin];
		String controlWord = controlToken.getValue();

		int readingIndex = controlStatementBegin + 1; // 読んでいるトークン位置

		// 括弧 (...) が必要な場合は検査
		if ( controlWord.equals(ScriptWord.IF)
				|| controlWord.equals(ScriptWord.WHILE)
				|| controlWord.equals(ScriptWord.FOR) ) {

			// 次のトークンが無いか、開き括弧「 ( 」でなければ構文エラー
			if (tokens.length <= controlStatementBegin+1
					|| !tokens[controlStatementBegin+1].getValue().equals(ScriptWord.PARENTHESIS_BEGIN)) {

				throw new VnanoException(
					ErrorType.NO_OPEN_PARENTHESIS_OF_CONTROL_STATEMENT, controlToken.getValue(),
					controlToken.getFileName(), controlToken.getLineNumber()
				);
			}

			// 括弧が閉じる箇所を探す
			int hierarchy = 0; // 開き括弧で上がり、閉じ括弧で下がる階層カウンタ
			while (true) {
				Token readingToken = tokens[readingIndex];
				if (readingToken.getValue().equals(ScriptWord.PARENTHESIS_BEGIN)) {
					hierarchy++;
				}
				if (readingToken.getValue().equals(ScriptWord.PARENTHESIS_END)) {
					hierarchy--;
				}

				// 階層が 0 になったら、それが最初の開き括弧に対応している閉じ括弧
				if (hierarchy == 0) {
					break;
				}

				// 開き括弧に対応している対応している閉じ括弧が見つからないまま文末に達した場合は構文エラー
				if (readingIndex == tokens.length-1
						|| readingToken.getValue().equals(ScriptWord.BLOCK_BEGIN)
						|| readingToken.getValue().equals(ScriptWord.BLOCK_END)
						|| (!arrowStatementsInParentheses&&readingToken.getValue().equals(ScriptWord.END_OF_STATEMENT)) ) {

					throw new VnanoException(
						ErrorType.NO_CLOSING_PARENTHESIS_OF_CONTROL_STATEMENT, controlToken.getValue(),
						controlToken.getFileName(), controlToken.getLineNumber()
					);
				}
				readingIndex++;
			}

			readingIndex++;
		}

		// この時点で readingIndex の値は、
		// if / for / while 文については閉じ丸括弧トークン「 ) 」の次、
		// else 文については「 else 」トークンの次を指している

		// else の後に if が続く場合は、else 直後に特例的にブロック始点が無くても許可する
		if ( controlWord.equals(ScriptWord.ELSE) && tokens[readingIndex].getValue().equals(ScriptWord.IF) ) {
			return;
		}

		// それ以外の if / else / for / while 文は、直後にブロック始点「 { 」が必要（この言語では仕様で必須化されている）
		if ( controlWord.equals(ScriptWord.IF)
				|| controlWord.equals(ScriptWord.ELSE)
				|| controlWord.equals(ScriptWord.FOR)
				|| controlWord.equals(ScriptWord.WHILE) ) {

			// 直後に「 { 」が続いていなければ構文エラーとする
			if (readingIndex == tokens.length-1
					|| !tokens[readingIndex].getValue().equals(ScriptWord.BLOCK_BEGIN)) {

				throw new VnanoException(
					ErrorType.NO_BLOCK_AFTER_CONTROL_STATEMENT, controlToken.getValue(),
					controlToken.getFileName(), controlToken.getLineNumber()
				);
			}
			return;
		}
	}


	/**
	 * 制御文の構文解析の前処理として、文を構成するトークン配列に対して、
	 * 括弧や条件式、その他必要な文の有無などを検査します。
	 * 検査の結果、問題が無かった場合には何もせず、問題が見つかった場合には例外をスローします。
	 *
	 * @param tokens 検査対象のトークン配列
	 * @throws VnanoException トークン配列に、制御文の構成トークンとしての問題があった場合にスローされます。
	 */
	protected void checkControlStatementTokens(Token[] tokens) throws VnanoException {
		Token controlTypeToken = tokens[0];
		int lineNumber = controlTypeToken.getLineNumber();
		String fileName = controlTypeToken.getFileName();

		// if文の場合
		if(controlTypeToken.getValue().equals(ScriptWord.IF)) {

			// 条件式が空の場合は構文エラー
			if (tokens.length == 3) {
				throw new VnanoException(ErrorType.NO_CONDITION_EXPRESSION_OF_IF_STATEMENT, fileName, lineNumber);
			}

		// while文の場合
		} else if(controlTypeToken.getValue().equals(ScriptWord.WHILE)) {

			// 条件式が空の場合は構文エラー
			if (tokens.length == 3) {
				throw new VnanoException(ErrorType.NO_CONDITION_EXPRESSION_OF_WHILE_STATEMENT, fileName, lineNumber);
			}

		// for文の場合
		} else if(controlTypeToken.getValue().equals(ScriptWord.FOR)) {

			// 初期化式と条件式の終端トークンインデックスを取得
			int initializationEnd = Token.getIndexOf(tokens, ScriptWord.END_OF_STATEMENT, 0);
			int conditionEnd = Token.getIndexOf(tokens, ScriptWord.END_OF_STATEMENT, initializationEnd+1);

			// 括弧 (...;...;...) 内の区切りが無いか足りない場合は構文エラー
			if (initializationEnd < 0 || conditionEnd < 0) {
				throw new VnanoException(
						ErrorType.ELEMENTS_OF_FOR_STATEMENT_IS_DEFICIENT, fileName, lineNumber
				);
			}

		} else if (controlTypeToken.getValue().equals(ScriptWord.RETURN)) {

			// 後に任意の式が続き、省略も可能なので、トークン列の段階では特に制約しない

		// else / break / continue 文の場合
		} else if(controlTypeToken.getValue().equals(ScriptWord.ELSE)
				|| controlTypeToken.getValue().equals(ScriptWord.BREAK)
				|| controlTypeToken.getValue().equals(ScriptWord.CONTINUE) ) {

			// 余計な記述が付いている
			if (tokens.length > 1) {
				throw new VnanoException(
						ErrorType.TOO_MANY_TOKENS_FOR_CONTROL_STATEMENT,
						controlTypeToken.getValue(), fileName, lineNumber
				);
			}

		} else {
			// ここに到達するのはLexicalAnalyzerの異常（不明な種類の制御構文）
			throw new VnanoFatalException("Unknown controll statement: " + controlTypeToken.getValue());
		}
	}


	/**
	 * 式のトークン配列内における、開き括弧「 ( 」と閉じ括弧「 ) 」の個数が合っているかどうかを検査します。
	 * なお、構文要素の括弧だけでなく、関数呼び出し演算子やキャスト演算子の括弧も含めて検査されます。
	 * 検査の結果、個数が合っていた場合には何もせず、合っていなかった場合には例外をスローします。
	 *
	 * @param tokens 検査対象のトークン配列
	 * @throws VnanoException 開き括弧と閉じ括弧の個数が合っていなかった場合にスローされます。
	 */
	protected void checkParenthesisOpeningClosings(Token[] tokens) throws VnanoException {
		int tokenLength = tokens.length;
		int hierarchy = 0; // 開き括弧で上がり、閉じ括弧で下がる階層カウンタ

		for (int tokenIndex=0; tokenIndex<tokenLength; tokenIndex++) {
			Token token = tokens[tokenIndex];

			boolean isParen = token.getType() == Token.Type.PARENTHESIS;
			boolean isCallOp = token.getType() == Token.Type.OPERATOR
					&& token.getAttribute(AttributeKey.OPERATOR_EXECUTOR).equals(AttributeValue.CALL);
			boolean isCastOp = token.getType() == Token.Type.OPERATOR
					&& token.getAttribute(AttributeKey.OPERATOR_EXECUTOR).equals(AttributeValue.CAST);

			if (isParen || isCallOp || isCastOp) {
				if (token.getValue().equals(ScriptWord.PARENTHESIS_BEGIN)) {
					hierarchy++;
				} else if (token.getValue().equals(ScriptWord.PARENTHESIS_END)) {
					hierarchy--;
				}
			}

			// 階層が負になった場合は、その時点で明らかに開き括弧が足りない
			if (hierarchy < 0) {
				throw new VnanoException(
					ErrorType.OPENING_PARENTHESES_IS_DEFICIENT,
					tokens[0].getFileName(), tokens[0].getLineNumber() // 階層が0でない時点でトークンは1個以上あるので[0]で参照可能
				);
			}
		}
		// 式のトークンを全て読み終えた時点で階層が1以上残っているなら、閉じ括弧が足りない
		if (hierarchy > 0) {
			throw new VnanoException(
				ErrorType.CLOSING_PARENTHESES_IS_DEFICIENT,
				tokens[0].getFileName(), tokens[0].getLineNumber()
			);
		}
	}


	/**
	 * 式のトークン配列内における、配列インデックスの開き「 [ 」と閉じ「 ] 」の個数が合っているかどうかを検査します。
	 * 検査の結果、個数が合っていた場合には何もせず、合っていなかった場合には例外をスローします。
	 *
	 * @param tokens 検査対象のトークン配列
	 * @throws VnanoException 開き括弧と閉じ括弧の個数が合っていなかった場合にスローされます。
	 */
	protected void checkSubscriptOpeningClosings(Token[] tokens) throws VnanoException {
		int tokenLength = tokens.length;
		int hierarchy = 0; // 開き括弧で上がり、閉じ括弧で下がる階層カウンタ

		for (int tokenIndex=0; tokenIndex<tokenLength; tokenIndex++) {
			Token token = tokens[tokenIndex];

			boolean isSubscript = token.getType() == Token.Type.OPERATOR
					&& token.getAttribute(AttributeKey.OPERATOR_EXECUTOR).equals(AttributeValue.SUBSCRIPT);

			if (isSubscript) {
				if (token.getValue().equals(ScriptWord.SUBSCRIPT_BEGIN)) {
					hierarchy++;
				} else if (token.getValue().equals(ScriptWord.SUBSCRIPT_END)) {
					hierarchy--;
				}
			}

			// 階層が負になった場合は、その時点で明らかに [ が足りない
			if (hierarchy < 0) {
				throw new VnanoException(
					ErrorType.OPENING_SUBSCRIPT_OPERATOR_IS_DEFICIENT,
					tokens[0].getFileName(), tokens[0].getLineNumber() // 階層が0でない時点でトークンは1個以上あるので[0]で参照可能
				);
			}
		}
		// 式のトークンを全て読み終えた時点で階層が1以上残っているなら、] が足りない
		if (hierarchy > 0) {
			throw new VnanoException(
				ErrorType.CLOSING_SUBSCRIPT_OPERATOR_IS_DEFICIENT,
				tokens[0].getFileName(), tokens[0].getLineNumber()
			);
		}
	}


	/**
	 * 式のトークン配列のトークンタイプを検査し、式の構成要素になり得ないタイプのトークンが存在していないか検査します。
	 * 検査の結果、問題が無かった場合には何もせず、問題が見つかった場合には例外をスローします。
	 *
	 * @param tokens 検査対象のトークン配列
	 * @throws VnanoException 式の構成要素になり得ないタイプのトークンが存在していた場合にスローされます。
	 */
	protected void checkTypeOfTokensInExpression(Token[] tokens) throws VnanoException {
		for(Token token: tokens) {
			switch (token.getType()) {

				// 演算子、リーフ、括弧は式の構成要素になる
				case OPERATOR : break;
				case LEAF : break;
				case PARENTHESIS : break;

				// キャスト演算子ではデータ型も構成要素になる
				case DATA_TYPE: break;

				// 式文の中にブロックの始点・終点がある場合は、スクリプト内に文末記号を書き忘れているので、
				// そういった方向のエラーメッセージで構文エラーとする
				case BLOCK : {
					throw new VnanoException(
							ErrorType.STATEMENT_END_IS_NOT_FOUND,
							token.getValue(),
							token.getFileName(), token.getLineNumber()
					);
				}

				// それ以外も式の構成要素になり得ないので、単純にそういったエラーメッセージで構文エラーとする
				default : {
					throw new VnanoException(
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
	 * 空になっている箇所が無いか検査します。
	 * ただし、関数呼び出し演算子の ( ) は、中身が空でも有効であるため、検査対象に含まれません。
	 * キャスト演算子に関しても、そもそも中身が空の場合はキャスト演算子と解釈されないはずであるため、検査されません。
	 * 検査の結果、問題が無かった場合には何もせず、問題が見つかった場合には例外をスローします。
	 *
	 * @param tokens 検査対象のトークン配列
	 * @throws VnanoException 空の部分式が含まれていた場合にスローされます。
	 */
	protected void checkEmptyParenthesesInExpression(Token[] tokens) throws VnanoException {
		int tokenLength = tokens.length;
		int contentCounter = 0;
		for (int tokenIndex=0; tokenIndex<tokenLength; tokenIndex++) {
			Token token = tokens[tokenIndex];
			if (token.getType() == Token.Type.PARENTHESIS) {
				if (token.getValue().equals(ScriptWord.PARENTHESIS_BEGIN)) {
					contentCounter = 0;
				} else if (token.getValue().equals(ScriptWord.PARENTHESIS_END)) {
					if (contentCounter == 0) {
						throw new VnanoException(
								ErrorType.NO_PARTIAL_EXPRESSION,
								token.getFileName(), token.getLineNumber()
						);
					}
				}
			} else {
				contentCounter++;
			}
		}
	}


	/**
	 * 式のトークン配列内で、演算子やリーフの位置が、適切な関係で並んでいるか検査します。
	 * 検査の結果、問題が無かった場合には何もせず、問題が見つかった場合には例外をスローします。
	 *
	 * @param tokens 検査対象のトークン配列
	 * @throws VnanoException 問題が見つかった場合にスローされます。
	 */
	protected void checkLocationsOfOperatorsAndLeafsInExpression(Token[] tokens) throws VnanoException {
		int tokenLength = tokens.length;

		// トークンを先頭から末尾まで辿って検査
		for (int tokenIndex=0; tokenIndex<tokenLength; tokenIndex++) {
			Token token = tokens[tokenIndex];

			boolean nextIsLeaf = tokenIndex!=tokenLength-1 && tokens[tokenIndex+1].getType()==Token.Type.LEAF;
			boolean prevIsLeaf = tokenIndex!=0 && tokens[tokenIndex-1].getType()==Token.Type.LEAF;

			boolean nextIsOpenParenthesis = tokenIndex < tokenLength-1
					&& tokens[tokenIndex+1].getType() == Token.Type.PARENTHESIS
					&& tokens[tokenIndex+1].getValue().equals(ScriptWord.PARENTHESIS_BEGIN);

			boolean prevIsCloseParenthesis = tokenIndex != 0
					&& tokens[tokenIndex-1].getType() == Token.Type.PARENTHESIS
					&& tokens[tokenIndex-1].getValue().equals(ScriptWord.PARENTHESIS_END);

			boolean nextIsMultialyBegin = tokenIndex < tokenLength-1
					&& tokens[tokenIndex+1].getType() == Token.Type.OPERATOR
					&& tokens[tokenIndex+1].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.MULTIARY);

			boolean prevIsMultialyEnd = tokenIndex != 0
					&& tokens[tokenIndex-1].getType() == Token.Type.OPERATOR
					&& tokens[tokenIndex-1].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.MULTIARY_END);

			boolean nextIsPrefixOperator = tokenIndex < tokenLength-1
					&& tokens[tokenIndex+1].getType()==Token.Type.OPERATOR
					&& tokens[tokenIndex+1].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.PREFIX);

			boolean prevIsPostfixOperator = tokenIndex != 0
					&& tokens[tokenIndex-1].getType()==Token.Type.OPERATOR
					&& tokens[tokenIndex-1].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.POSTFIX);

			boolean nextIsDataType = tokenIndex < tokenLength-1
					&& tokens[tokenIndex+1].getType()==Token.Type.DATA_TYPE;

			boolean nextNextIsCastEnd = tokenIndex < tokenLength-2
					&& tokens[tokenIndex+2].getType()==Token.Type.OPERATOR
					&& tokens[tokenIndex+2].getAttribute(AttributeKey.OPERATOR_EXECUTOR).equals(AttributeValue.CAST)
					&& tokens[tokenIndex+2].getValue().equals(ScriptWord.PARENTHESIS_END);

			// 演算子の場合
			if (token.getType() == Token.Type.OPERATOR) {
				String operatorSyntax = token.getAttribute(AttributeKey.OPERATOR_SYNTAX);
				String operatorExecutor = token.getAttribute(AttributeKey.OPERATOR_EXECUTOR);

				// 前置演算子の場合
				if (operatorSyntax.equals(AttributeValue.PREFIX)) {

					// キャスト演算子の始点の場合は、右がデータ型があり、かつその直後が閉じ括弧でないとエラー
					if (operatorExecutor.equals(AttributeValue.CAST) && token.getValue().equals(ScriptWord.PARENTHESIS_BEGIN)) {
						if (!nextIsDataType) {
							throw new VnanoException(
								ErrorType.DATA_TYPE_IS_MISSING_AT_RIGHT,
								token.getValue(), token.getFileName(), token.getLineNumber()
							);
						}
						if (!nextNextIsCastEnd) {
							throw new VnanoException(
								ErrorType.CLOSE_PARENTHESIS_IS_MISSING_AT_RIGHT,
								tokens[tokenIndex+1].getValue(), token.getFileName(), token.getLineNumber() // 上でnextIsDataTypeがtrueである時点で tokenIndex+1 番目へのアクセスは可能
							);
						}

					// それ以外の場合は、右がリーフか開き括弧か多項演算子（関数呼び出しや配列アクセス）始点か前置演算子でないとエラー
					} else if ( !(  nextIsLeaf || nextIsOpenParenthesis || nextIsMultialyBegin || nextIsPrefixOperator  ) ) {
						throw new VnanoException(
							ErrorType.OPERAND_IS_MISSING_AT_RIGHT,
							token.getValue(), token.getFileName(), token.getLineNumber()
						);
					}

				} // 前置演算子の場合

				// 後置演算子の場合は、右がリーフか閉じ括弧か多項演算子（関数呼び出しや配列アクセス）終点や後置演算子でないとエラー
				if (operatorSyntax.equals(AttributeValue.POSTFIX)
						&& !(  prevIsLeaf || prevIsCloseParenthesis || prevIsMultialyEnd || prevIsPostfixOperator ) ) {

					throw new VnanoException(
						ErrorType.OPERAND_IS_MISSING_AT_LEFT,
						token.getValue(), token.getFileName(), token.getLineNumber()
					);
				} // 後置演算子の場合

				// 二項演算子や、多項演算子（関数呼び出しや配列アクセス）の区切りの場合
				if (operatorSyntax.equals(AttributeValue.BINARY)
						|| operatorSyntax.equals(AttributeValue.MULTIARY_SEPARATOR)) {

					// 右がリーフか開き括弧か前置演算子か多項演算子（関数呼び出しや配列アクセス）の始点でないとエラー
					if( !(  nextIsLeaf || nextIsOpenParenthesis || nextIsPrefixOperator || nextIsMultialyBegin  ) ) {
						throw new VnanoException(
							ErrorType.OPERAND_IS_MISSING_AT_RIGHT,
							token.getValue(), token.getFileName(), token.getLineNumber()
						);
					}
					// 左のトークンがリーフか閉じ括弧か後置演算子か多項演算子（関数呼び出しや配列アクセス）の終点でないとエラー
					if( !(  prevIsLeaf || prevIsCloseParenthesis || prevIsPostfixOperator || prevIsMultialyEnd  ) ) {
						throw new VnanoException(
							ErrorType.OPERAND_IS_MISSING_AT_LEFT,
							token.getValue(), token.getFileName(), token.getLineNumber()
						);
					}
				} // 二項演算子や、多項演算子（関数呼び出しや配列アクセス）の区切りの場合

			} // 演算子の場合

			// リーフの場合
			if (token.getType() == Token.Type.LEAF) {

				// 右のトークンが開き括弧やリーフの場合はエラー
				if (nextIsOpenParenthesis || nextIsLeaf) {     // nextIsMultialyBegin は付加してはいけない（普通に配列アクセスや関数呼び出しが来る右に場合が該当してしまう）
					throw new VnanoException(
						ErrorType.OPERATOR_IS_MISSING_AT_RIGHT,
						new String[] {token.getValue(), tokens[tokenIndex+1].getValue()},
						token.getFileName(), token.getLineNumber()
					);
				}

				// 左のトークンが閉じ括弧やリーフの場合はエラー
				if (prevIsCloseParenthesis || prevIsLeaf || prevIsMultialyEnd) {
					throw new VnanoException(
						ErrorType.OPERATOR_IS_MISSING_AT_LEFT,
						new String[] {tokens[tokenIndex-1].getValue(), token.getValue()},
						token.getFileName(), token.getLineNumber()
					);
				}
			}

		} // トークンを先頭から末尾まで辿るループ
	}


	/**
	 * 式の構文解析の前処理として、式を構成するトークン配列に対して、
	 * トークンタイプや括弧の開き閉じ対応などの検査を行います。
	 * 検査の結果、問題が無かった場合には何もせず、問題が見つかった場合には例外をスローします。
	 *
	 * @param tokens 検査対象のトークン配列
	 * @throws VnanoException トークン配列に、式の構成トークンとしての問題があった場合にスローされます。
	 */
	protected void checkTokensInExpression(Token[] tokens) throws VnanoException {

		// トークン列内の開き括弧と閉じ括弧の対応を確認（合っていなければここで例外発生）
		checkParenthesisOpeningClosings(tokens);

		// トークン列内の配列インデックスの [ と ] の対応を確認（合っていなければここで例外発生）
		checkSubscriptOpeningClosings(tokens);

		// 式の構成要素になり得ない種類のトークンが存在しないか確認（存在すればここで例外発生）
		checkTypeOfTokensInExpression(tokens);

		// 式の中に空の括弧が存在しないか確認（関数呼び出し演算子は除く）
		checkEmptyParenthesesInExpression(tokens);

		// 演算子やリーフの位置が適切な関係で並んでいるか確認
		checkLocationsOfOperatorsAndLeafsInExpression(tokens);
	}

}
