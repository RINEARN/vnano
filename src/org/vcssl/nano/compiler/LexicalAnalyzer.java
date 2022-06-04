/*
 * Copyright(C) 2017-2022 RINEARN
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

/**
 * The class performing the function of the lexical analyzer in the compiler of the Vnano.
 *
 * The lexical analysis of this class takes script code as the input,
 * then split it into tokens and outputs them.
 * In add, in the analysis, some attributes are analyzed and set for tokens,
 * for example: types of tokens, data types of literals, precedences of operators, and so on.
 */
public class LexicalAnalyzer {

	/**
	 * Create a new lexical analyzer.
	 */
	public LexicalAnalyzer() {
	}


	/**
	 * Splits code of the script into tokens, and returns them after analyzing and setting their attributes.
	 * 
	 * @param script The script to be processed.
	 * @param fileName The filename of the script to be processed.
	 * @return Tokens.
	 * @throws VnanoException Thrown when any syntax error has detected.
	 */
	public Token[] analyze(String script, String fileName) throws VnanoException {

		// Extract all string literals in code, and replace them to escaped literals: "1", "2", "3", ... "N".
		// In the return value, code in which literals are replaced is stored in [0],
		// and contents of literals are stored in [1], [2], [3], ..., [N].
		String[] stringLiteralExtractResult = LiteralSyntax.extractStringLiterals(script);
		String stringLiteralReplacedCode = stringLiteralExtractResult[0];

		// Perform the lexical analysis, and get tokens.
		Token[] tokens = this.tokenize(stringLiteralReplacedCode, fileName);

		// Analyze types, operator-precedences, and so on of tokens.
		this.analyzeTokenTypes(tokens);
		this.analyzePrecedences(tokens);
		this.analyzeAssociativities(tokens);
		this.analyzeLiteralAttributes(tokens);

		// Recover escaped string literals in code.
		this.embedStringLiterals(tokens, stringLiteralExtractResult);

		return tokens;
	}


	/**
	 * Splits code of the script into tokens.
	 * 
	 * @param script The script to be processed.
	 * @param fileName The filename of the script to be processed.
	 * @return Tokens.
	 */
	private Token[] tokenize(String script, String fileName) {

		// The current implementation assumes the maximum character length of symbols (of operators and so on) is 3, 
		// where symbols are defined in ScriptWord.symbolSet.
		// The above restriction is for reducing cost of looking-ahead chars.

		ArrayList<Token> tokenList = new ArrayList<Token>();
		char[] chars = script.toCharArray();
		int length = chars.length;
		int pointer = 0;
		int lineNumber = 1;

		// The buffer for constructing the content of a word token.
		// (Chars will be pushed to it while the end of a token has not been detected.)
		StringBuilder wordTokenBuilder = new StringBuilder();
		String singleCharSymbol = null;
		String doubleCharSymbol = null;
		String tripleCharSymbol = null;
		boolean isReadingNumericLiteral = false;

		while(pointer < length) {

			// Prepare candidate of a 1-char symbol token.
			singleCharSymbol = Character.toString(chars[pointer]);

			// Prepare candidate of a 2-char symbol token.
			if (pointer<length-1) {
				doubleCharSymbol = new String(new char[] {chars[pointer], chars[pointer+1]} );
			} else {
				doubleCharSymbol = null;
			}

			// Prepare candidate of a 3-char symbol token.
			if (pointer<length-2) {
				tripleCharSymbol = new String(new char[] {chars[pointer], chars[pointer+1], chars[pointer+2]} );
			} else {
				tripleCharSymbol = null;
			}

			// Symbol "+" or "-" in the exponent part of a numeric literal should not be handled as a symbol token.
			if (0<pointer && isReadingNumericLiteral) {
				if (Character.toString(chars[pointer-1]).matches(LiteralSyntax.FLOAT_LITERAL_EXPONENT_PREFIX)) {
					singleCharSymbol = null;
				}
			}

			// When the next is symbol token or white space or other "token splitter" chars, 
			// create a word token from the content stored in the buffer.
			if (Character.toString(chars[pointer]).matches(ScriptWord.TOKEN_SEPARATOR_REGEX)
					|| ScriptWord.SYMBOL_SET.contains(singleCharSymbol)
					|| ScriptWord.SYMBOL_SET.contains(doubleCharSymbol)
					|| ScriptWord.SYMBOL_SET.contains(tripleCharSymbol) ) {

				if (wordTokenBuilder.length() != 0) {
					tokenList.add(new Token(
						new String(wordTokenBuilder.toString()), lineNumber, fileName
					));
					wordTokenBuilder = new StringBuilder();
					wordTokenBuilder.delete(0, wordTokenBuilder.length());
					isReadingNumericLiteral = false;
				}
			}

			// Important note:
			// the following order of branches should be kept for satisfying "longest match" rule.

			// When the next is a 3-char symbol, create a symbol token.
			if (ScriptWord.SYMBOL_SET.contains(tripleCharSymbol)) {
				tokenList.add(new Token(tripleCharSymbol, lineNumber, fileName));
				pointer += 3; // 3 chars are looked-ahead.
				continue;

			// When the next is a 2-char symbol, create a symbol token.
			} else if (ScriptWord.SYMBOL_SET.contains(doubleCharSymbol)) {
				tokenList.add(new Token(doubleCharSymbol, lineNumber, fileName));
				pointer += 2; // 2 chars are looked-ahead.
				continue;

			// When the next is a 1-char symbol, create a symbol token.
			} else if (ScriptWord.SYMBOL_SET.contains(singleCharSymbol)) {
				tokenList.add(new Token(singleCharSymbol, lineNumber, fileName));
				pointer += 1; // 1 char are looked-ahead.
				continue;

			// When the next is a white space, line feed, or other "token splitter" char, don't create any token here.
			} else if (chars[pointer]==' ' || chars[pointer]=='\n' || chars[pointer]=='\t') {
				if (chars[pointer] == '\n') {
					lineNumber++;
				}
				pointer++;
				continue;

			// Other char is a part of a word token, so push it to the buffer.
			} else {
				// A word token starts with a number is a numeric literal.
				if (wordTokenBuilder.length() == 0 && Character.toString(chars[pointer]).matches("^[0-9]$")) {
					isReadingNumericLiteral = true;
				}
				wordTokenBuilder.append(chars[pointer]);
				pointer++;
				continue;
			}
		}

		// Extract the last word token from the buffer, if exists.
		if (wordTokenBuilder.length() != 0) {
			tokenList.add(new Token(
				new String(wordTokenBuilder.toString()), lineNumber, fileName
			));
		}

		Token[] tokens = (Token[])tokenList.toArray(new Token[tokenList.size()]);
		return tokens;
	}


	/**
	 * Analyze types of tokens and set to them.
	 * 
	 * @parak tokens Tokens to be analyzed.
	 */
	private void analyzeTokenTypes(Token[] tokens) {

		int tokenLength = tokens.length;
		Token lastToken = null;
		String lastWord = null;

		// A variable for counting up hierarchy of parentheses.
		// (Incremented when the reading flow enters in a parenthesis, and decremented when it exit from a parenthesis.)
		// 括弧の中に入ると可算, 出ると減算していく括弧階層カウンタ
		int parenthesisStage = 0;

		// A variable for storing the hierarchy of parenthesis of the function call currently analyzed.
		Set<Integer> callParenthesisStages = new HashSet<Integer>();

		// A flag for storing whether the currently analized token is in (...) of a cast operator.
		boolean inCastParenthesis = false;

		for(int i=0; i<tokenLength; i++) {
			String word = tokens[i].getValue();

			// Block: "{" or "}"
			if (word.equals(ScriptWord.BLOCK_BEGIN) || word.equals(ScriptWord.BLOCK_END)) {
				tokens[i].setType(Token.Type.BLOCK);

			// Open parenthesis: "("
			} else if (word.equals(ScriptWord.PARENTHESIS_BEGIN)) {
				parenthesisStage++;

				// Cast operator:
				if (i<tokenLength-1 && DataTypeName.isDataTypeName(tokens[i+1].getValue())) {
					tokens[i].setType(Token.Type.OPERATOR);
					tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.CAST);
					tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.PREFIX);
					inCastParenthesis = true;

				// Function-call operator:
				} else if (lastToken!=null && lastToken.getType()==Token.Type.LEAF
						&& lastToken.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.FUNCTION_IDENTIFIER)
						&& !ScriptWord.STATEMENT_NAME_SET.contains(lastWord)) {

					callParenthesisStages.add(parenthesisStage);
					tokens[i].setType(Token.Type.OPERATOR);
					tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.CALL);
					tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.MULTIARY);

				// The beginning of a partial expression:
				} else {
					tokens[i].setType(Token.Type.PARENTHESIS);
				}

			// Closing parenthesis: ")"
			} else if (word.equals(ScriptWord.PARENTHESIS_END)) {

				// Cast operator:
				if (inCastParenthesis) {

					tokens[i].setType(Token.Type.OPERATOR);
					tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.CAST);
					tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.PREFIX);
					inCastParenthesis = false;

				// Function-call operator:
				} else if (callParenthesisStages.contains(parenthesisStage)) {
					callParenthesisStages.remove(parenthesisStage);
					tokens[i].setType(Token.Type.OPERATOR);
					tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.CALL);
					tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.MULTIARY_END);

				// The end of a partial expression:
				} else {
					tokens[i].setType(Token.Type.PARENTHESIS);
				}
				parenthesisStage--;

			// Comma as a separator of arguments in a function call: ","
			} else if (word.equals(ScriptWord.ARGUMENT_SEPARATOR)) {
				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.CALL);
				tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.MULTIARY_SEPARATOR);

			// The beginning of a subscript operator: "["
			} else if (word.equals(ScriptWord.SUBSCRIPT_BEGIN)) {
				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.SUBSCRIPT);
				tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.MULTIARY);

			// The separator of a subscript operator: "]["
			} else if (word.equals(ScriptWord.SUBSCRIPT_END + ScriptWord.SUBSCRIPT_BEGIN)) {

				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.SUBSCRIPT);
				tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.MULTIARY_SEPARATOR);

			// The end of a subscript operator: "]"
			} else if (word.equals(ScriptWord.SUBSCRIPT_END)) {

				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.SUBSCRIPT);
				tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.MULTIARY_END);

			// The end of a statement: ";"
			} else if (word.equals(ScriptWord.END_OF_STATEMENT)) {
				tokens[i].setType(Token.Type.END_OF_STATEMENT);

			// Modifiers: "&" (as call-by-ref), "...", and so on.
			} else if (ScriptWord.MODIFIER_SET.contains(word)) {
				tokens[i].setType(Token.Type.MODIFIER);

			// Assignment operator: "="
			} else if (word.equals(ScriptWord.ASSIGNMENT)) {
				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.ASSIGNMENT);
				tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.BINARY);

			// "+" or "-"
			} else if (word.equals(ScriptWord.PLUS_OR_ADDITION) || word.equals(ScriptWord.MINUS_OR_SUBTRACTION)) {

				// Bynary addition or subtraction:
				if (lastToken!=null && (lastToken.getType()==Token.Type.LEAF
						|| lastToken.getValue().equals(ScriptWord.PARENTHESIS_END)
						|| lastToken.getValue().equals(ScriptWord.SUBSCRIPT_END))) {

					tokens[i].setType(Token.Type.OPERATOR);
					tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.ARITHMETIC);
					tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.BINARY);

				// Unary minus or plus:
				} else {
					tokens[i].setType(Token.Type.OPERATOR);
					tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.ARITHMETIC);
					tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.PREFIX);
				}

			// Other arithmetic binary operators:
			} else if (word.equals(ScriptWord.MULTIPLICATION)
					|| word.equals(ScriptWord.DIVISION)
					|| word.equals(ScriptWord.REMAINDER)) {

				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.ARITHMETIC);
				tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.BINARY);

			// Arithmetic-assignment compound operators:
			} else if (word.equals(ScriptWord.ADDITION_ASSIGNMENT)
					|| word.equals(ScriptWord.SUBTRACTION_ASSIGNMENT)
					|| word.equals(ScriptWord.MULTIPLICATION_ASSIGNMENT)
					|| word.equals(ScriptWord.DIVISION_ASSIGNMENT)
					|| word.equals(ScriptWord.REMAINDER_ASSIGNMENT)) {

				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT);
				tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.BINARY);

			// Comparison binary operators:
			} else if (word.equals(ScriptWord.EQUAL)
					|| word.equals(ScriptWord.NOT_EQUAL)
					|| word.equals(ScriptWord.GREATER_THAN)
					|| word.equals(ScriptWord.GREATER_EQUAL)
					|| word.equals(ScriptWord.LESS_THAN)
					|| word.equals(ScriptWord.LESS_EQUAL)) {

				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.COMPARISON);
				tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.BINARY);

			// Logical binary operators:
			} else if (word.equals(ScriptWord.SHORT_CIRCUIT_AND)
					|| word.equals(ScriptWord.SHORT_CIRCUIT_OR)) {

				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.LOGICAL);
				tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.BINARY);

			// Logical prefix operators:
			} else if (word.equals(ScriptWord.NOT)) {
				tokens[i].setType(Token.Type.OPERATOR);
				tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.LOGICAL);
				tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.PREFIX);

			// Increment and decrement: "++" or "--"
			} else if (word.equals(ScriptWord.INCREMENT) || word.equals(ScriptWord.DECREMENT)) {

				if (lastToken!=null && ( lastToken.getType() == Token.Type.LEAF
						|| lastToken.getType() == Token.Type.OPERATOR
						&& lastToken.getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.POSTFIX) ) ) {

					tokens[i].setType(Token.Type.OPERATOR);
					tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT);
					tokens[i].setAttribute(AttributeKey.OPERATOR_SYNTAX, AttributeValue.POSTFIX);
				} else {
					tokens[i].setType(Token.Type.OPERATOR);
					tokens[i].setAttribute(AttributeKey.OPERATOR_EXECUTOR, AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT);
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

			// Data-type names:
			} else if (DataTypeName.isDataTypeName(word)) {
				tokens[i].setType(Token.Type.DATA_TYPE);

			// Liefs: literal, identifier, and so on.
			} else {
				tokens[i].setType(Token.Type.LEAF);

				// Literal:
				if (LiteralSyntax.isValidLiteral(word)) {
					tokens[i].setAttribute(AttributeKey.LEAF_TYPE, AttributeValue.LITERAL);

				// Function identifier:
				} else if (i<tokenLength-1 && tokens[i+1].getValue().equals(ScriptWord.PARENTHESIS_BEGIN)) {
					tokens[i].setAttribute(AttributeKey.LEAF_TYPE, AttributeValue.FUNCTION_IDENTIFIER);

				// Variable identifier:
				} else {
					tokens[i].setAttribute(AttributeKey.LEAF_TYPE, AttributeValue.VARIABLE_IDENTIFIER);
				}
			}
			lastToken = tokens[i];
			lastWord = word;
		}
	}


	/**
	 * Analyze precedences of operator-tokens and set to them.
	 * 
	 * @parak tokens Tokens to be analyzed.
	 */
	private void analyzePrecedences(Token[] tokens) {
		int length = tokens.length;
		for(int i=0; i<length; i++) {

			String symbol = tokens[i].getValue();
			if (symbol.equals(ScriptWord.PARENTHESIS_BEGIN)) {

					// Operators (function calls, cast operators)
					if (tokens[i].getType() == Token.Type.OPERATOR) {
						String executor = tokens[i].getAttribute(AttributeKey.OPERATOR_EXECUTOR);
						if (executor.equals(AttributeValue.CAST)) {
							tokens[i].setPrecedence(OperatorPrecedence.CAST_BEGIN);
						} else if (executor.equals(AttributeValue.CALL)) {
							tokens[i].setPrecedence(OperatorPrecedence.CALL_BEGIN);
						}

					// Syntactic parentheses
					} else {
						tokens[i].setPrecedence(OperatorPrecedence.PARENTHESIS_BEGIN);
					}

			} else if (symbol.equals(ScriptWord.PARENTHESIS_END)) {

					// Operators (function calls, cast operators)
					if (tokens[i].getType() == Token.Type.OPERATOR) {
						String executor = tokens[i].getAttribute(AttributeKey.OPERATOR_EXECUTOR);
						if (executor.equals(AttributeValue.CAST)) {
							tokens[i].setPrecedence(OperatorPrecedence.CAST_END);
						} else if (executor.equals(AttributeValue.CALL)) {
							tokens[i].setPrecedence(OperatorPrecedence.CALL_END);
						}

					// Syntactic parentheses
					} else {
						tokens[i].setPrecedence(OperatorPrecedence.PARENTHESIS_END);
					}

			} else if (symbol.equals(ScriptWord.ARGUMENT_SEPARATOR)) {
					tokens[i].setPrecedence(OperatorPrecedence.CALL_SEPARATOR);

			} else if (symbol.equals(ScriptWord.SUBSCRIPT_BEGIN)) {
					tokens[i].setPrecedence(OperatorPrecedence.SUBSCRIPT_BEGIN);

			} else if (symbol.equals(ScriptWord.SUBSCRIPT_END)) {
					tokens[i].setPrecedence(OperatorPrecedence.SUBSCRIPT_END);

			} else if (symbol.equals(ScriptWord.SUBSCRIPT_SEPARATOR)) {
					tokens[i].setPrecedence(OperatorPrecedence.SUBSCRIPT_SEPARATOR);

			} else if (symbol.equals(ScriptWord.INCREMENT)) {
					if (tokens[i].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.PREFIX)) {
						tokens[i].setPrecedence(OperatorPrecedence.PREFIX_INCREMENT);
					} else {
						tokens[i].setPrecedence(OperatorPrecedence.POSTFIX_INCREMENT);
					}

			} else if (symbol.equals(ScriptWord.DECREMENT)) {
					if (tokens[i].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.PREFIX)) {
						tokens[i].setPrecedence(OperatorPrecedence.PREFIX_DECREMENT);
					} else {
						tokens[i].setPrecedence(OperatorPrecedence.POSTFIX_DECREMENT);
					}

			} else if (symbol.equals(ScriptWord.NOT)) {
					tokens[i].setPrecedence(OperatorPrecedence.NOT);

			} else if (symbol.equals(ScriptWord.PLUS_OR_ADDITION)) {
					if (tokens[i].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.PREFIX)) {
						tokens[i].setPrecedence(OperatorPrecedence.PREFIX_PLUS);
					} else {
						tokens[i].setPrecedence(OperatorPrecedence.ADDITION);
					}

			} else if (symbol.equals(ScriptWord.MINUS_OR_SUBTRACTION)) {
					if (tokens[i].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.PREFIX)) {
						tokens[i].setPrecedence(OperatorPrecedence.PREFIX_MINUS);
					} else {
						tokens[i].setPrecedence(OperatorPrecedence.SUBTRACTION);
					}

			} else if (symbol.equals(ScriptWord.MULTIPLICATION)) {
					tokens[i].setPrecedence(OperatorPrecedence.MULTIPLICATION);

			} else if (symbol.equals(ScriptWord.DIVISION)) {
					tokens[i].setPrecedence(OperatorPrecedence.DIVISION);

			} else if (symbol.equals(ScriptWord.REMAINDER)) {
					tokens[i].setPrecedence(OperatorPrecedence.REMAINDER);

			} else if (symbol.equals(ScriptWord.LESS_THAN)) {
					tokens[i].setPrecedence(OperatorPrecedence.LESS_THAN);

			} else if (symbol.equals(ScriptWord.LESS_EQUAL)) {
					tokens[i].setPrecedence(OperatorPrecedence.LESS_EQUAL);

			} else if (symbol.equals(ScriptWord.GREATER_THAN)) {
					tokens[i].setPrecedence(OperatorPrecedence.GREATER_THAN);

			} else if (symbol.equals(ScriptWord.GREATER_EQUAL)) {
					tokens[i].setPrecedence(OperatorPrecedence.GREATER_EQUAL);

			} else if (symbol.equals(ScriptWord.EQUAL)) {
					tokens[i].setPrecedence(OperatorPrecedence.EQUAL);

			} else if (symbol.equals(ScriptWord.NOT_EQUAL)) {
					tokens[i].setPrecedence(OperatorPrecedence.NOT_EQUAL);

			} else if (symbol.equals(ScriptWord.SHORT_CIRCUIT_AND)) {
					tokens[i].setPrecedence(OperatorPrecedence.SHORT_CIRCUIT_AND);

			} else if (symbol.equals(ScriptWord.SHORT_CIRCUIT_OR)) {
					tokens[i].setPrecedence(OperatorPrecedence.SHORT_CIRCUIT_OR);

			} else if (symbol.equals(ScriptWord.ASSIGNMENT)) {
					tokens[i].setPrecedence(OperatorPrecedence.ASSIGNMENT);

			} else if (symbol.equals(ScriptWord.ADDITION_ASSIGNMENT)) {
					tokens[i].setPrecedence(OperatorPrecedence.ADDITION_ASSIGNMENT);

			} else if (symbol.equals(ScriptWord.SUBTRACTION_ASSIGNMENT)) {
					tokens[i].setPrecedence(OperatorPrecedence.SUBTRACTION_ASSIGNMENT);

			} else if (symbol.equals(ScriptWord.MULTIPLICATION_ASSIGNMENT)) {
					tokens[i].setPrecedence(OperatorPrecedence.MULTIPLICATION_ASSIGNMENT);

			} else if (symbol.equals(ScriptWord.DIVISION_ASSIGNMENT)) {
					tokens[i].setPrecedence(OperatorPrecedence.DIVISION_ASSIGNMENT);

			} else if (symbol.equals(ScriptWord.REMAINDER_ASSIGNMENT)) {
					tokens[i].setPrecedence(OperatorPrecedence.REMAINDER_ASSIGNMENT);
			}
		}
	}


	/**
	 * Analyze associativities of operator-tokens and set to them.
	 * 
	 * @parak tokens Tokens to be analyzed.
	 */
	private void analyzeAssociativities(Token[] tokens) {
		int length = tokens.length;
		for(int i=0; i<length; i++) {

			// Skip non-operator tokens.
			if (tokens[i].getType() != Token.Type.OPERATOR) {
				continue;
			}

			// Most operators are left-associative, so set it as default,
			// and overwrite the value if the operator is right-associative.
			String associativity = AttributeValue.LEFT;

			String symbol = tokens[i].getValue();

			// Open parenthesis: (Cast operators are right-associative)
			if (symbol.equals(ScriptWord.PARENTHESIS_BEGIN)) {
					if (tokens[i].getAttribute(AttributeKey.OPERATOR_EXECUTOR).equals(AttributeValue.CAST)) {
						associativity = AttributeValue.RIGHT;
					}

			// Closing parenthesis: (Cast operators are right-associative)
			} else if (symbol.equals(ScriptWord.PARENTHESIS_END)) {
					if (tokens[i].getAttribute(AttributeKey.OPERATOR_EXECUTOR).equals(AttributeValue.CAST)) {
						associativity = AttributeValue.RIGHT;
					}

			// Prefix increment:
			} else if (symbol.equals(ScriptWord.INCREMENT)) {
					if (tokens[i].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.PREFIX)) {
						associativity = AttributeValue.RIGHT;
					}

			// Prefix decrement:
			} else if (symbol.equals(ScriptWord.DECREMENT)) {
					if (tokens[i].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.PREFIX)) {
						associativity = AttributeValue.RIGHT;
					}

			// Logical not:
			} else if (symbol.equals(ScriptWord.NOT)) {
					associativity = AttributeValue.RIGHT;

			// Unary plus:
			} else if (symbol.equals(ScriptWord.PLUS_OR_ADDITION)) {
					if (tokens[i].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.PREFIX)) {
						associativity = AttributeValue.RIGHT;
					}

			// Unary minus:
			} else if (symbol.equals(ScriptWord.MINUS_OR_SUBTRACTION)) {
					if (tokens[i].getAttribute(AttributeKey.OPERATOR_SYNTAX).equals(AttributeValue.PREFIX)) {
						associativity = AttributeValue.RIGHT;
					}

			// Assignment:
			} else if (symbol.equals(ScriptWord.ASSIGNMENT)) {
					associativity = AttributeValue.RIGHT;

			// Addition-assignment:
			} else if (symbol.equals(ScriptWord.ADDITION_ASSIGNMENT)) {
					associativity = AttributeValue.RIGHT;

			// Subtraction-assignment:
			} else if (symbol.equals(ScriptWord.SUBTRACTION_ASSIGNMENT)) {
					associativity = AttributeValue.RIGHT;

			// Multiplication-assignment:
			} else if (symbol.equals(ScriptWord.MULTIPLICATION_ASSIGNMENT)) {
					associativity = AttributeValue.RIGHT;

			// Division-assignment:
			} else if (symbol.equals(ScriptWord.DIVISION_ASSIGNMENT)) {
					associativity = AttributeValue.RIGHT;

			// Remainder-assignment:
			} else if (symbol.equals(ScriptWord.REMAINDER_ASSIGNMENT)) {
					associativity = AttributeValue.RIGHT;
			}

			tokens[i].setAttribute(AttributeKey.OPERATOR_ASSOCIATIVITY, associativity);
		}
	}


	/**
	 * Analyze data types of literal-tokens and set to them.
	 * 
	 * @parak tokens Tokens to be analyzed.
	 */
	private void analyzeLiteralAttributes(Token[] tokens) {
		for (Token token: tokens) {
			if (token.getType() == Token.Type.LEAF
				&& token.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.LITERAL)) {

				String literal = token.getValue();
				String dataTypeName = LiteralSyntax.getDataTypeNameOfLiteral(literal);
				token.setAttribute(AttributeKey.DATA_TYPE, dataTypeName);
				token.setAttribute(AttributeKey.RANK, "0"); // Array literals have not been supported, so all literals are scalars.
			}
		}
	}


	/**
	 * Recovers escaped string literals ("1", "2", "3", ...) in tokens.
	 *
	 * @param tokens Tokens in which string literal is escaped.
	 * @param stringLiteralExtractResult 
	 *            The result of {@link org.vcssl.nano.spec.LiteralSyntax#extractStringLiterals} method,
	 *            in which original contents of string literals are stored.
	 */
	private void embedStringLiterals(Token[] tokens, String[] stringLiteralExtractResult) {
		int tokenLength = tokens.length;

		// Traverse all tokens:
		for (int tokenIndex=0; tokenIndex<tokenLength; tokenIndex++) {
			Token token = tokens[tokenIndex];
			String value = token.getValue();

			// If the token is a (escaped) string literal, recover the content of it:
			if (token.getType() == Token.Type.LEAF
					&& token.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.LITERAL)
					&& LiteralSyntax.getDataTypeNameOfLiteral(value).equals(DataTypeName.STRING)) {

				int index = LiteralSyntax.getIndexOfNumberedStringLiteral(value);
				token.setValue(stringLiteralExtractResult[index]);
			}
		}
	}

}

