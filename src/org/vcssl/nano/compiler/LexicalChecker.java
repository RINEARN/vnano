/*
 * Copyright(C) 2018-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.ScriptWord;

public class LexicalChecker {

	/**
	 * Create a new lexical checker.
	 */
	public LexicalChecker() {
	}


	/**
	 * Checks tokens after keywords of control statements (if, else, for, while, and so on).
	 * For example, after "if", there must be tokens of "(...){...}".
	 * This method throws an exception when any syntactic problem is detected, and otherwise do nothing.
	 *
	 * @param tokens Tokens to be checked.
	 * @param controlStatementBegin The array index of the beginning token of the control statement to be checked.
	 * @throws VnanoException Thrown when any syntactic problem is detected.
	 */
	protected void checkTokensAfterControlStatement(
			Token[] tokens, int controlStatementBegin, boolean arrowStatementsInParentheses)
					throws VnanoException {

		// The token of the keyword of the control statemen, e.g.t: "if", "for", and so on.
		Token controlToken = tokens[controlStatementBegin];
		String controlWord = controlToken.getValue();

		int readingIndex = controlStatementBegin + 1;

		// If parentheses (...) are necessary, check:
		if ( controlWord.equals(ScriptWord.IF)
				|| controlWord.equals(ScriptWord.WHILE)
				|| controlWord.equals(ScriptWord.FOR) ) {

			// If the next token of the keyword of the control statement isn't "(", throw an exception:
			if (tokens.length <= controlStatementBegin+1
					|| !tokens[controlStatementBegin+1].getValue().equals(ScriptWord.PARENTHESIS_BEGIN)) {

				throw new VnanoException(
					ErrorType.NO_OPEN_PARENTHESIS_OF_CONTROL_STATEMENT, controlToken.getValue(),
					controlToken.getFileName(), controlToken.getLineNumber()
				);
			}

			// Find the closing parenthesis ")" corresponding with the first "(" after the keyword of the control statement:
			int hierarchy = 0; // A variable for counting-up hierarchy of parentheses (incremented at "(", and decremented at ")").
			while (true) {
				Token readingToken = tokens[readingIndex];
				if (readingToken.getValue().equals(ScriptWord.PARENTHESIS_BEGIN)) {
					hierarchy++;
				}
				if (readingToken.getValue().equals(ScriptWord.PARENTHESIS_END)) {
					hierarchy--;
				}

				// ")" of which hierarchy is 0 corresponds with the first "(".
				if (hierarchy == 0) {
					break;
				}

				// If it does not exist, throw an exception:
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

		// If ")" has been detected in the above, here "readingIndex" is pointing to the next of ")".
		// Otherwise, it is pointing to the next of the keyword of the control statement (e.g.: "else").

		// Only when an "else" is followed by an "if", you can omit "{...}" after the "else".
		if ( controlWord.equals(ScriptWord.ELSE) && tokens[readingIndex].getValue().equals(ScriptWord.IF) ) {
			return;
		}

		// Otherwise, "{...}" is always required after an "if(...)" / "else" / "for(...)" / "while(...)", in Vnano.
		if ( controlWord.equals(ScriptWord.IF)
				|| controlWord.equals(ScriptWord.ELSE)
				|| controlWord.equals(ScriptWord.FOR)
				|| controlWord.equals(ScriptWord.WHILE) ) {

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
	 * Checks existences of conditional expressions and so on, of control statements.
	 * This method throws an exception when any syntactic problem is detected, and otherwise do nothing.
	 *
	 * @param tokens Tokens to be checked.
	 * @throws VnanoException Thrown when any syntactic problem is detected.
	 */
	protected void checkControlStatementTokens(Token[] tokens) throws VnanoException {
		Token controlTypeToken = tokens[0];
		int lineNumber = controlTypeToken.getLineNumber();
		String fileName = controlTypeToken.getFileName();

		// "if" statements:
		if(controlTypeToken.getValue().equals(ScriptWord.IF)) {

			// If it hasn't any conditional expression, throw an exception.
			if (tokens.length == 3) {
				throw new VnanoException(ErrorType.NO_CONDITION_EXPRESSION_OF_IF_STATEMENT, fileName, lineNumber);
			}

		// "while" statements:
		} else if(controlTypeToken.getValue().equals(ScriptWord.WHILE)) {

			// If it hasn't any conditional expression, throw an exception.
			if (tokens.length == 3) {
				throw new VnanoException(ErrorType.NO_CONDITION_EXPRESSION_OF_WHILE_STATEMENT, fileName, lineNumber);
			}

		// "for" statements:
		} else if(controlTypeToken.getValue().equals(ScriptWord.FOR)) {

			// Get index of ending tokens of an initialization statement and a condition expression.
			int initializationEnd = Token.getIndexOf(tokens, ScriptWord.END_OF_STATEMENT, 0);
			int conditionEnd = Token.getIndexOf(tokens, ScriptWord.END_OF_STATEMENT, initializationEnd+1);

			// If the number of separators ";" in "(...;...;...)" is deficient, throw an exception.
			if (initializationEnd < 0 || conditionEnd < 0) {
				throw new VnanoException(
						ErrorType.ELEMENTS_OF_FOR_STATEMENT_IS_DEFICIENT, fileName, lineNumber
				);
			}

		// "return" statements:
		} else if (controlTypeToken.getValue().equals(ScriptWord.RETURN)) {

		// "else", "brek", "continue" statements:
		} else if(controlTypeToken.getValue().equals(ScriptWord.ELSE)
				|| controlTypeToken.getValue().equals(ScriptWord.BREAK)
				|| controlTypeToken.getValue().equals(ScriptWord.CONTINUE) ) {

			// If it has unnecessary tokens, throw an exception.
			if (tokens.length > 1) {
				throw new VnanoException(
						ErrorType.TOO_MANY_TOKENS_FOR_CONTROL_STATEMENT,
						controlTypeToken.getValue(), fileName, lineNumber
				);
			}

		} else {
			throw new VnanoFatalException("Unknown controll statement: " + controlTypeToken.getValue());
		}
	}


	/**
	 * Checks types, orders, correspondence of open/closing parentheses and so on of tokens in an expression.
	 * This method throws an exception when any syntactic problem is detected, and otherwise do nothing.
	 *
	 * @param tokens Tokens to be checked.
	 * @throws VnanoException Thrown when any syntactic problem is detected.
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


	/**
	 * Checks correspondencies of open parentheses "(" and closing parentheses ")", in tokens of an expression.
	 * This method throws an exception when any syntactic problem is detected, and otherwise do nothing.
	 *
	 * @param tokens Tokens to be checked.
	 * @throws VnanoException Thrown when any syntactic problem is detected.
	 */
	protected void checkParenthesisOpeningClosings(Token[] tokens) throws VnanoException {
		int tokenLength = tokens.length;
		int hierarchy = 0; // A variable for counting-up hierarchy of parentheses (incremented at "(", and decremented at ")").

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
			if (hierarchy < 0) {
				throw new VnanoException(
					ErrorType.OPENING_PARENTHESES_IS_DEFICIENT,
					tokens[0].getFileName(), tokens[0].getLineNumber()
				);
			}
		}
		if (hierarchy > 0) {
			throw new VnanoException(
				ErrorType.CLOSING_PARENTHESES_IS_DEFICIENT,
				tokens[0].getFileName(), tokens[0].getLineNumber()
			);
		}
	}


	/**
	 * Checks correspondencies of "[" and "]", in tokens of an expression.
	 * This method throws an exception when any syntactic problem is detected, and otherwise do nothing.
	 *
	 * @param tokens Tokens to be checked.
	 * @throws VnanoException Thrown when any syntactic problem is detected.
	 */
	protected void checkSubscriptOpeningClosings(Token[] tokens) throws VnanoException {
		int tokenLength = tokens.length;
		int hierarchy = 0; // A variable for counting-up hierarchy of [ ] (incremented at "[", and decremented at "]").

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

			if (hierarchy < 0) {
				throw new VnanoException(
					ErrorType.OPENING_SUBSCRIPT_OPERATOR_IS_DEFICIENT,
					tokens[0].getFileName(), tokens[0].getLineNumber()
				);
			}
		}
		if (hierarchy > 0) {
			throw new VnanoException(
				ErrorType.CLOSING_SUBSCRIPT_OPERATOR_IS_DEFICIENT,
				tokens[0].getFileName(), tokens[0].getLineNumber()
			);
		}
	}


	/**
	 * Checks types of tokens in an expression.
	 * This method throws an exception when any syntactic problem is detected, and otherwise do nothing.
	 *
	 * @param tokens Tokens to be checked.
	 * @throws VnanoException Thrown when any syntactic problem is detected.
	 */
	protected void checkTypeOfTokensInExpression(Token[] tokens) throws VnanoException {
		for(Token token: tokens) {
			switch (token.getType()) {

				// Operators, leafs, and parentheses can be elements of expressions.
				case OPERATOR : break;
				case LEAF : break;
				case PARENTHESIS : break;

				// Data-types can be elements of expressions (as cast-operators).
				// キャスト演算子ではデータ型も構成要素になる
				case DATA_TYPE: break;

				// "{" and "}" can't be elements of expressions.
				// If they are contained in an expression,
				// the author of the script may have forgotten to put ";" to the end of an expression statement.
				// So sudgest it by the error message.
				case BLOCK : {
					throw new VnanoException(
							ErrorType.STATEMENT_END_IS_NOT_FOUND,
							token.getValue(),
							token.getFileName(), token.getLineNumber()
					);
				}

				// Other kinds of tokens can't be elements of expressions.
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
	 * Checks existences of contents in parentheses "( )" in an expression, excluding call operators.
	 * This method throws an exception when any syntactic problem is detected, and otherwise do nothing.
	 *
	 * @param tokens Tokens to be checked.
	 * @throws VnanoException Thrown when any syntactic problem is detected.
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
	 * Checks orders of operators and leafs in an expression.
	 * This method throws an exception when any syntactic problem is detected, and otherwise do nothing.
	 *
	 * @param tokens Tokens to be checked.
	 * @throws VnanoException Thrown when any syntactic problem is detected.
	 */
	protected void checkLocationsOfOperatorsAndLeafsInExpression(Token[] tokens) throws VnanoException {
		int tokenLength = tokens.length;
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

			// Operators:
			if (token.getType() == Token.Type.OPERATOR) {
				String operatorSyntax = token.getAttribute(AttributeKey.OPERATOR_SYNTAX);
				String operatorExecutor = token.getAttribute(AttributeKey.OPERATOR_EXECUTOR);

				// Prefix operators:
				if (operatorSyntax.equals(AttributeValue.PREFIX)) {

					// Beginning of a cast operator: the next token must be a data-type, and the next of it must be ")".
					if (operatorExecutor.equals(AttributeValue.CAST) && token.getValue().equals(ScriptWord.PARENTHESIS_BEGIN)) {
						if (!nextIsDataType) {
							throw new VnanoException(
								ErrorType.DATA_TYPE_IS_MISSING_AT_RIGHT,
								token.getValue(), token.getFileName(), token.getLineNumber()
							);
						}

						// One or multiple subscript symbols [] can be after the data-type, so lookahead tokens to the end of the cast operator:
						for (int tokenInCastOpIndex=tokenIndex+2; tokenInCastOpIndex<tokenLength; tokenInCastOpIndex++) {
							Token tokenInCastOp = tokens[tokenInCastOpIndex];

							boolean isSubscript = tokenInCastOp.getType() == Token.Type.OPERATOR
									&& tokenInCastOp.getAttribute(AttributeKey.OPERATOR_EXECUTOR).equals(AttributeValue.SUBSCRIPT);

							boolean isCastEnd = tokenInCastOp.getType() == Token.Type.OPERATOR
									&& tokenInCastOp.getAttribute(AttributeKey.OPERATOR_EXECUTOR).equals(AttributeValue.CAST)
									&& tokenInCastOp.getValue().equals(ScriptWord.PARENTHESIS_END);

							if (isCastEnd) {
								tokenIndex = tokenInCastOpIndex + 1;
								break;
							} else if (!isSubscript) {
								throw new VnanoException(
									ErrorType.CLOSE_PARENTHESIS_IS_MISSING_AT_RIGHT,
									tokens[tokenInCastOpIndex-1].getValue(), token.getFileName(), token.getLineNumber()
									// Note: tokens[tokenIndex+1] exists because nextIsDataType is true.
								);
							}
						}
						continue;

					// Otherwise: the next token is an operand, so it must be a leaf, or "(", or the beginning of a multiary operator, or a prefix-operator.
					} else if ( !(  nextIsLeaf || nextIsOpenParenthesis || nextIsMultialyBegin || nextIsPrefixOperator ) ) {
						throw new VnanoException(
							ErrorType.OPERAND_IS_MISSING_AT_RIGHT,
							token.getValue(), token.getFileName(), token.getLineNumber()
						);
					}

				} // Prefix operators

				// Postfix operator: the next token must be a leaf, or ")", or the end of a multiary operator, or a postfix-operator.
				if (operatorSyntax.equals(AttributeValue.POSTFIX)
						&& !(  prevIsLeaf || prevIsCloseParenthesis || prevIsMultialyEnd || prevIsPostfixOperator ) ) {

					throw new VnanoException(
						ErrorType.OPERAND_IS_MISSING_AT_LEFT,
						token.getValue(), token.getFileName(), token.getLineNumber()
					);
				} // Postfix operator

				// Binary operators, or separators in multiary operators:
				if (operatorSyntax.equals(AttributeValue.BINARY)
						|| operatorSyntax.equals(AttributeValue.MULTIARY_SEPARATOR)) {

					// The next token must be "(", or a prefix-operator, or the beginning of a multiary operator.
					if( !(  nextIsLeaf || nextIsOpenParenthesis || nextIsPrefixOperator || nextIsMultialyBegin  ) ) {
						throw new VnanoException(
							ErrorType.OPERAND_IS_MISSING_AT_RIGHT,
							token.getValue(), token.getFileName(), token.getLineNumber()
						);
					}

					// The previous token must be ")", or a postfix operator, or the end of a multiary operator.
					if( !(  prevIsLeaf || prevIsCloseParenthesis || prevIsPostfixOperator || prevIsMultialyEnd  ) ) {
						throw new VnanoException(
							ErrorType.OPERAND_IS_MISSING_AT_LEFT,
							token.getValue(), token.getFileName(), token.getLineNumber()
						);
					}
				} // Binary operators, or separators in multiary operators

			} // Operators

			// Leafs:
			if (token.getType() == Token.Type.LEAF) {

				// Some tokens can't be at the right of a leaf.
				if (nextIsOpenParenthesis || nextIsLeaf) {     // Don't filter out "nextIsMultialyBegin". It can be at the right of a leaf, e.g.: fun(..., a[..., and so on.
					throw new VnanoException(
						ErrorType.OPERATOR_IS_MISSING_AT_RIGHT,
						new String[] {token.getValue(), tokens[tokenIndex+1].getValue()},
						token.getFileName(), token.getLineNumber()
					);
				}

				// Some tokens can't be at the left of a leaf.
				if (prevIsCloseParenthesis || prevIsLeaf || prevIsMultialyEnd) {
					throw new VnanoException(
						ErrorType.OPERATOR_IS_MISSING_AT_LEFT,
						new String[] {tokens[tokenIndex-1].getValue(), token.getValue()},
						token.getFileName(), token.getLineNumber()
					);
				}
			} // Leafs

		}
	}

}
