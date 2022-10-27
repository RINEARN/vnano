/*
 * Copyright(C) 2017-2022 RINEARN
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
import org.vcssl.nano.spec.ErrorType;

/**
 * <span class="lang-en">
 * The class performing the function of the parser in the compiler of the Vnano.
 * 
 * The processing of this class takes tokens as input,
 * then constructs the AST (Abstract Syntax Tree) and outputs it.
 */
public class Parser {

	/** The array-rank of the scalar. */
	private static final int RANK_OF_SCALAR = 0;


	/**
	 * Create a new parser.
	 */
	public Parser() {
	}


	/**
	 * Constructs and returns the AST by parsing tokens.
	 * 
	 * @param tokens Tokens to be parsed.
	 * @return The constructed AST.
	 * @throws VnanoException Thrown when any syntax error has detected.
	 */
	public AstNode parse(Token[] tokens) throws VnanoException {

		LexicalChecker lexicalChecker = new LexicalChecker();

		// Working stack to form multiple AstNode instances into a tree-shape.
		Deque<AstNode> statementStack = new ArrayDeque<AstNode>();

		int tokenLength = tokens.length; // The total number of tokens.
		int statementBegin = 0; // Stores the array index of the beginning token of a statement.

		while(statementBegin < tokenLength) {

			// Get indices of the end of the statement, and the beginning/end of the next block.
			int statementEnd = Token.getIndexOf(tokens, ScriptWord.END_OF_STATEMENT, statementBegin);
			int blockBegin = Token.getIndexOf(tokens, ScriptWord.BLOCK_BEGIN, statementBegin);
			int blockEnd = Token.getIndexOf(tokens, ScriptWord.BLOCK_END, statementBegin);

			Token beginToken = tokens[statementBegin];

			// Throw an exception when the statement end symbol ";" is missing.
			// The third condition is for excluding the case that there is no statement after a block-end.
			if (statementEnd < 0 && blockBegin < 0 && statementBegin!=blockEnd) {
				throw new VnanoException(
						ErrorType.STATEMENT_END_IS_NOT_FOUND, beginToken.getFileName(), beginToken.getLineNumber()
				);
			}

			// Empty statement:
			if (statementBegin == statementEnd) {
				AstNode emptyStatementNode = new AstNode(
						AstNode.Type.EMPTY, tokens[statementBegin].getLineNumber(), beginToken.getFileName()
				);
				statementStack.push(emptyStatementNode);
				statementBegin++;

			// Beginning/end of a block:
			} else if (beginToken.getType()==Token.Type.BLOCK) {

				// Beginning of a block:
				// Put a "lid" to the stack, as a marker of the beginning of the block, 
				// for collecting statements in the block later.
				if (beginToken.getValue().equals(ScriptWord.BLOCK_BEGIN)) {
					this.pushLid(statementStack);
					statementBegin++;

				// End of a block:
				} else {

					// Collect nodes of statements in the current block, from the stack.
					AstNode[] statementsInBlock = this.popStatementNodes(statementStack);

					// Create a block node, and link nodes of statements in the block as child nodes.
					AstNode blockNode = new AstNode(
						AstNode.Type.BLOCK, beginToken.getLineNumber(), beginToken.getFileName()
					);
					for (AstNode statementNode: statementsInBlock) {
						blockNode.addChildNode(statementNode);
					}

					// Push the created block node to the stack.
					statementStack.push(blockNode);
					statementBegin++;
				}

			// Control statements:
			} else if (beginToken.getType()==Token.Type.CONTROL) {
				String word = beginToken.getValue();

				// "if", "for", or "while" statements:
				// (In Vnano, a block {...} is always required for an above statement.)
				if (word.equals(ScriptWord.IF) || word.equals(ScriptWord.FOR) || word.equals(ScriptWord.WHILE)) {

					// "for" statement contains ";" in its "(...)", and other statements doesn't contain it.
					boolean allowsStatementEndSymbolsInControlStatement = word.equals(ScriptWord.FOR);

					// Check existence of a block.
					lexicalChecker.checkTokensAfterControlStatement(tokens, statementBegin, allowsStatementEndSymbolsInControlStatement);

					Token[] subTokens = Arrays.copyOfRange(tokens, statementBegin, blockBegin);
					statementStack.push(this.parseControlStatement(subTokens));
					statementBegin = blockBegin;

				// "else" statement:
				} else if (beginToken.getValue().equals(ScriptWord.ELSE)) {

					// Check existence of a block.
					lexicalChecker.checkTokensAfterControlStatement(tokens, statementBegin, false);

					Token[] subTokens = Arrays.copyOfRange(tokens, statementBegin, statementBegin+1);
					statementStack.push(this.parseControlStatement(subTokens));
					statementBegin++;

				// "break", "continue", or "return" statements:
				} else {
					Token[] subTokens = Arrays.copyOfRange(tokens, statementBegin, statementEnd);
					statementStack.push(this.parseControlStatement(subTokens));
					statementBegin = statementEnd + 1;
				}

			// Dependency declaration statement (import / include):
			} else if (beginToken.getType()==Token.Type.DEPENDENCY_DECLARATOR) {
				Token[] subTokens = Arrays.copyOfRange(tokens, statementBegin, statementEnd);
				statementStack.push(this.parseDependencyDeclarationStatement(subTokens));
				statementBegin = statementEnd + 1;

			// Function declaration statement:
			} else if (this.startsWithFunctionDeclarationTokens(tokens, statementBegin)) {
				Token[] subTokens = Arrays.copyOfRange(tokens, statementBegin, blockBegin);
				statementStack.push(this.parseFunctionDeclarationStatement(subTokens));
				statementBegin = blockBegin;

			// Variable declaration statement:
			} else if (beginToken.getType()==Token.Type.DATA_TYPE || beginToken.getType()==Token.Type.MODIFIER) {
				Token[] subTokens = Arrays.copyOfRange(tokens, statementBegin, statementEnd);
				statementStack.push(this.parseVariableDeclarationStatement(subTokens, true));
				statementBegin = statementEnd + 1;

			// Expression statement:
			} else {
				Token[] subTokens = Arrays.copyOfRange(tokens, statementBegin, statementEnd);
				statementStack.push(this.parseExpression(subTokens));
				statementBegin = statementEnd + 1;
			}
		}

		// Extract the file name and the line number of the first token, to set it to the root node.
		String fileName = null;
		int lineNumber = -1;
		if (tokens.length != 0) {
			fileName = tokens[0].getFileName();
			lineNumber = tokens[0].getLineNumber();
		}

		// Create the root node of the AST, and link nodes at the top-hierarchy as child nodes.
		AstNode rootNode = new AstNode(AstNode.Type.ROOT, lineNumber, fileName);
		while (statementStack.size() != 0) {
			// The order of nodes on the stack is reversed, so use "pollLast" instead of "pop", to extract them.
			rootNode.addChildNode(statementStack.pollLast());
		}

		// Set depth values of all nodes in the AST.
		rootNode.updateDepths();

		return rootNode;
	}





	// ====================================================================================================
	// Parsing of Expressions
	// ====================================================================================================


	/**
	 * Parses an expression.
	 *
	 * @param tokens Tokens composing an expression.
	 * @return The constructed AST.
	 * @throws VnanoException Thrown when any syntactic error is detected.
	 */
	private AstNode parseExpression(Token[] tokens) throws VnanoException {

		// Check types of tokens, correspondence of parentheses, and so on.
		// (VnanoException will be thrown when any problem is detected.)
		new LexicalChecker().checkTokensInExpression(tokens);

		// Syntax of cast operators are little irregular, 
		// so replace it to single-token prefix operators to simplify its parsing.
		tokens = preprocessCastSequentialTokens(tokens);

		int tokenLength = tokens.length;  // The total number ot tokens.
		int readingIndex = 0;             // The array-index of the currently reading token.
		int lineNumber = tokens[0].getLineNumber(); // Displayed in error messages.
		String fileName = tokens[0].getFileName();  // Displayed in error messages.

		// Working stack to form multiple AstNode instances into a tree-shape.
		Deque<AstNode> stack = new ArrayDeque<AstNode>();

		// The array storing next operator's precedence for each token.
		// At [i], it is stored that the precedence of the first operator of which token-index is greater than i.
		int[] nextOperatorPrecedence = this.getNextOperatorPrecedence(tokens);

		// Read tokens from left to right.
		do {
			AstNode operatorNode = null;

			Token readingToken = tokens[readingIndex];
			int readingOpPrecedence = readingToken.getPrecedence();      // The operator-precedence of the currently reading token, if it is an operator.
			int nextOpPrecedence = nextOperatorPrecedence[readingIndex]; // The operator-precedence of the next operator.
			String readingOpAssociativity = readingToken.getAttribute(AttributeKey.OPERATOR_ASSOCIATIVITY); // The associativity of the currently reading operator-token.

			// Leaf nodes (identifier, literal, and so on):
			if (readingToken.getType() == Token.Type.LEAF) {

				// For leafs, simply push the node to the stack. It will be popped and linked to an operator token later.
				stack.push(this.createLeafNode(readingToken));
				readingIndex++;
				continue;

			// Syntactic parentheses (excluding function-call and cast operators):
			} else if (readingToken.getType()==Token.Type.PARENTHESIS) {
				if (readingToken.getValue().equals(ScriptWord.PARENTHESIS_BEGIN)) { // Case of "("

					// Push a lid to the stack, for isolating tokens in a partial expression (...), from tokens pushed before them.
					// For details, see the description of {@link Parser#pushLid} method.
					this.pushLid(stack, AttributeValue.PARTIAL_EXPRESSION);
					readingIndex++;
					continue;

				} else { // Case of ")"
					operatorNode = this.popPartialExpressionNodes(stack, AttributeValue.PARTIAL_EXPRESSION, fileName, lineNumber)[0];
				}

			// Operators:
			} else if (readingToken.getType() == Token.Type.OPERATOR) {

				operatorNode = this.createOperatorNode(readingToken);
				switch (readingToken.getAttribute(AttributeKey.OPERATOR_SYNTAX)) {

					// Postfix operators:
					case AttributeValue.POSTFIX : {

						// The left operand is at the top of the stack, so pop it and link to this operator.
						operatorNode.addChildNode( this.popNode(stack, fileName, lineNumber, AstNode.Type.LEAF, AstNode.Type.OPERATOR) );
						break;
					}

					// Prefix operator:
					case AttributeValue.PREFIX : {

						// If the precedence of this operator is stronger than the next operator, 
						// look-ahead the next token as an leaf, and link its node to this operator node as a child.
						// (At the top of this method, it should be checked that the leaf token exists at the next of this operator.)
						if (this.shouldAddRightOperand(readingOpAssociativity, readingOpPrecedence, nextOpPrecedence)) {
							operatorNode.addChildNode( this.createLeafNode(tokens[readingIndex+1]) );
							readingIndex++; // The next token has been looked-ahead.
						}
						break;
					}

					// Binary operators:
					case AttributeValue.BINARY : {

						// The left operand is at the top of the stack, so pop it and link to this operator.
						operatorNode.addChildNode(stack.pop());

						// If the precedence of this operator is stronger than the next operator, 
						// look-ahead the next token as an leaf, and link its node to this operator node as a child.
						// (At the top of this method, it should be checked that the leaf token exists at the next of this operator.)
						if (this.shouldAddRightOperand(readingOpAssociativity, readingOpPrecedence, nextOpPrecedence)) {
							operatorNode.addChildNode( this.createLeafNode(tokens[readingIndex+1]) );
							readingIndex++; // The next token has been looked-ahead.
						}
						break;
					}

					// Beginning tokens of multiary operators:
					// (e.g.: "(" of a function-call operator, "[" of a subscript operator)
					case AttributeValue.MULTIARY : {

						// The identifier node is at the top of the stack, so pop it and link to this operator, and push this operator.
						operatorNode.addChildNode( this.popNode(stack, fileName, lineNumber, AstNode.Type.LEAF) );
						stack.push(operatorNode);

						// Push a lid to the stack, for isolating tokens of a partial expression of an argument/index, from tokens pushed before them.
						// For details, see the description of {@link Parser#pushLid} method.
						this.pushLid(stack, readingToken.getAttribute(AttributeKey.OPERATOR_EXECUTOR));
						readingIndex++;
						continue;
					}

					// Separator tokens of multiary operators:
					// (e.g.: "," of a function-call operator, "][" of a subscript operator)
					case AttributeValue.MULTIARY_SEPARATOR : {

						// Push a lid to the stack, for isolating tokens of a partial expression of an argument/index, from tokens pushed before them.
						this.pushLid(stack);
						readingIndex++;
						continue;
					}

					// Ending tokens of multiary operators:
					// (e.g.: ")" of a function-call operator, "]" of a subscript operator)
					case AttributeValue.MULTIARY_END : {

						// Extract nodes of partial expressions of all arguments/indices.
						// (Each partial expression is formed as an isolated AST on the stack, separated by "stack-lid"s.)
						AstNode[] argumentNodes = this.popPartialExpressionNodes(
								stack, readingToken.getAttribute(AttributeKey.OPERATOR_EXECUTOR), fileName, lineNumber
						);

						// Pop the function-call/subscript operator node, and link arguments/indices nodes to it as child nodes.
						operatorNode = this.popNode(stack, fileName, lineNumber, AstNode.Type.OPERATOR);
						operatorNode.addChildNodes(argumentNodes);
						break;
					}

					default : {
						throw new VnanoFatalException("Unknown operator syntax");
					}
				}

			} else {
				throw new VnanoFatalException("Unknown token type");
			}

			// If the precedence of the operator at the top of the stack is stronger than the next operator, link its operands stored in the stack to it.
			while (this.shouldAddRightOperandToStackedOperator(stack, nextOperatorPrecedence[readingIndex])) {
				AstNode oldOperatorNode = operatorNode;
				operatorNode = this.popNode(stack, fileName, lineNumber, AstNode.Type.OPERATOR);
				operatorNode.addChildNode(oldOperatorNode);
			}

			stack.push(operatorNode);
			readingIndex++;
		} while (readingIndex < tokenLength);

		// If the parsing completed expectedly, only the root node of the constructed AST should be in the stack.
		// Otherwise something in inputted tokens should be syntactically incorrect.
		if (stack.size() != 1) {
			throw new VnanoException(ErrorType.INVALID_EXPRESSION_SYNTAX, fileName, lineNumber);
		}

		// Pop the root node of the constructed AST from the stack, and wrap it by an EXPRESSION type node.
		AstNode exprRootNode = this.popNode(stack, fileName, lineNumber, AstNode.Type.LEAF, AstNode.Type.OPERATOR);
		AstNode expressionNode = new AstNode(AstNode.Type.EXPRESSION, lineNumber, fileName);
		expressionNode.addChildNode(exprRootNode);
		return expressionNode;
	}

	/**
	 * Judges whether the right-side token should be connected directly to the target operator as an operand, 
	 * in {@link Parser#parseExpression} method.
	 *
	 * @param targetOperatorAssociativity The associativity (right/left) of the target opeartor.
	 * @param targetOperatorPrecedence The precedence of the target operator (smaller value gives higher precedence).
	 * @param nextOperatorPrecedence The precedence of the next operator (smaller value gives higher precedence).
	 * @return Returns true if the right-side token (operand) should be connected to the target operator.
	 */
	private boolean shouldAddRightOperand(
			String targetOperatorAssociativity, int targetOperatorPrecedence, int nextOperatorPrecedence) {

		// If the precedence of the target operator is stronger than the next operator, return true.
		// If the precedence of the next operator is stronger than the target operator, return false.
		// If the precedence of both operators is the same:
		//         Return true if the target operator is left-associative.
		//         Return false if the target operator is right-associative.

		boolean targetOpPrecedenceIsStrong = targetOperatorPrecedence < nextOperatorPrecedence; // Smaller value gives higher precedence.
		boolean targetOpPrecedenceIsEqual = targetOperatorPrecedence == nextOperatorPrecedence; // Smaller value gives higher precedence.
		boolean targetOpAssociativityIsLeft = targetOperatorAssociativity.equals(AttributeValue.LEFT);
		return targetOpPrecedenceIsStrong || (targetOpPrecedenceIsEqual && targetOpAssociativityIsLeft);
	}


	/**
	 * Judges whether the right-side token should be connected directly as an operand, to the operator at the top of the working stack,
	 * in {@link Parser#parseExpression} method.
	 *
	 * @param stack The working stack used for the parsing.
	 * @param nextOperatorPrecedence The precedence of the next operator (smaller value gives higher precedence).
	 * @return Returns true if the right-side token (operand) should be connected to the operator at the top of the stack.
	 */
	private boolean shouldAddRightOperandToStackedOperator(Deque<AstNode> stack, int nextOperatorPrecedence) {

		// When there is no operator node at the top of the stack, return false.
		if (stack.size() == 0) {
			return false;
		}
		if (stack.peek().getType() != AstNode.Type.OPERATOR) {
			return false;
		}

		// Return the result depending on the precedence/associativity of the operator at the top of the stack.
		int stackedOperatorPrecedence = Integer.parseInt(stack.peek().getAttribute(AttributeKey.OPERATOR_PRECEDENCE));
		String stackedOperatorAssociativity = stack.peek().getAttribute(AttributeKey.OPERATOR_ASSOCIATIVITY);
		return this.shouldAddRightOperand(stackedOperatorAssociativity, stackedOperatorPrecedence, nextOperatorPrecedence);
	}


	/**
	 * Returns an array storing next operator's precedence for each token.
	 * In the returned array, it will stored at [i] that
	 * precedence of the first operator of which token-index is greater than i.
	 *
	 * @param tokens All tokens to be parsed.
	 * @return The array storing next operator's precedence for each token.
	 */
	private int[] getNextOperatorPrecedence(Token[] tokens) {

		int length = tokens.length;
		int[] rightOpPrecedences = new int[ length ];

		// Stores the precedence of the operator being on the right-side of the currently reading token.
		// The operator at the right end hasn't any "right operator", so set LEAST_PRIOR as the initial value.
		int rightOpPrecedence = OperatorPrecedence.LEAST_PRIOR;

		// Read tokens from the right to the left.
		for(int i = length-1; 0 <= i; i--) {

			// Store the precedence of the operator being on the right-side of i-th token, to the array to be returned.
			rightOpPrecedences[i] = rightOpPrecedence;

			// If i-th token is an operator, update the value of rightOpPrecedence.
			if (tokens[i].getType() == Token.Type.OPERATOR) {
				rightOpPrecedence = tokens[i].getPrecedence();
			}

			// Parentheses are not operators, but they have precedences, 
			// for realizing syntactic role of them in the parsing algorithm used by this parser.
			if (tokens[i].getType() == Token.Type.PARENTHESIS) {

				// "(" has strongest precedence value, for always isolating the right operand from the left operand.
				if(tokens[i].getValue().equals(ScriptWord.PARENTHESIS_BEGIN)){
					rightOpPrecedence = OperatorPrecedence.MOST_PRIOR;

				// ")" has weakest precedence value, to complete linking of tokens in the partial expression.
				} else {
					rightOpPrecedence = OperatorPrecedence.LEAST_PRIOR;
				}
			}
		}

		return rightOpPrecedences;
	}





	// ====================================================================================================
	// Parsing of Variable Declarations
	// ====================================================================================================


	/**
	 * Parses an variable declaration statement.
	 * 
	 * In the root node of the result of this method, 
	 * the identifier of the variable will be stored as IDENTIFIER attribute, 
	 * and the data-type will be stored as DATA_TYPE attribute, 
	 * and the array-rank will be stored as RANK attribute.
	 * If the array-rank is non-zero (non-scalar), a LENGTHS type node generated by 
	 * {@link Parser#parseVariableDeclarationArrayLengths parseVariableDeclarationArrayLengths} method
	 * will be linked as a child node.
	 * Also, if there is an expression for initialization of the declared variable, 
	 * an EXPRESSION type node will be linked as a child node.
	 *
	 * @param tokens Tokens of the variable declaration statement.
	 * @param requiresIdentifier Specify true if the identifier can't be omitted.
	 * @return The constructed AST.
	 * @throws VnanoException Thrown when any syntactic error is detected.
	 */
	private AstNode parseVariableDeclarationStatement(Token[] tokens, boolean requiresIdentifier)
			throws VnanoException {

		AstNode variableNode = new AstNode(AstNode.Type.VARIABLE, tokens[0].getLineNumber(), tokens[0].getFileName());
		List<String> modifierList = new ArrayList<String>();
		List<Token> tokenList = new ArrayList<Token>();

		for (Token token: tokens) {
			tokenList.add(token);
		}

		int readingIndex = 0;

		// Extract modifiers put before the data-type name, and register to the list.
		if (tokens[readingIndex].getType() == Token.Type.MODIFIER) {
			if (ScriptWord.PREFIX_MODIFIER_SET.contains(tokens[readingIndex].getValue()) ) {
				modifierList.add(tokens[readingIndex].getValue());
				readingIndex++;
			} else {
				throw new VnanoException(
					ErrorType.POSTFIX_MODIFIER_BEFORE_TYPE_NAME, new String[] { tokens[readingIndex].getValue() },
					tokens[readingIndex].getFileName(), tokens[readingIndex].getLineNumber()
				);
			}
		}

		// Extract data-type name, and set it as an attribute of the node.
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

		// Extract modifiers put after the data-type name, and register to the list.
		if (readingIndex < tokens.length && tokens[readingIndex].getType() == Token.Type.MODIFIER) {
			if (ScriptWord.POSTFIX_MODIFIER_SET.contains(tokens[readingIndex].getValue()) ) {
				modifierList.add(tokens[readingIndex].getValue());
				readingIndex++;
			} else {
				throw new VnanoException(
					ErrorType.PREFIX_MODIFIER_AFTER_TYPE_NAME, new String[] { tokens[readingIndex].getValue() },
					tokens[readingIndex].getFileName(), tokens[readingIndex].getLineNumber()
				);
			}
		}

		// To this variable, store an extracted variable name (identifier).
		Token nameToken = null;

		// If there is no more token, the variable name is missing, so throw an exception.
		// However, if requiresIdentifier is set to false, allow missing of the name.
		// (it occurs when parsing declarations of parameter variables in function signatures, e.g.: fun(int,float))
		if (tokens.length <= readingIndex) {
			if (requiresIdentifier) {
				throw new VnanoException(
					ErrorType.NO_IDENTIFIER_IN_VARIABLE_DECLARATION,
					tokens[readingIndex-1].getFileName(), tokens[readingIndex-1].getLineNumber()
				);
			}

		// If the next token isn't the variable name,  throw an exception.
		// However, if requiresIdentifier is set to false, allow missing of the name.
		} else if (tokens[readingIndex].getType()!=Token.Type.LEAF
			|| !tokens[readingIndex].getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.VARIABLE_IDENTIFIER)) {
			if (requiresIdentifier) {
				throw new VnanoException(
					ErrorType.INVALID_IDENTIFIER_TYPE,
					new String[] { tokens[readingIndex].getValue() },
					tokens[readingIndex-1].getFileName(), tokens[readingIndex-1].getLineNumber()
				);
			}

		// Extract the variable name, and set it as an attribute of the node.
		} else {
			nameToken = tokens[readingIndex];
			variableNode.setAttribute(AttributeKey.IDENTIFIER_VALUE, nameToken.getValue());
			readingIndex++;
		}

		// Extract array-rank and lengths.
		int arrayRank = RANK_OF_SCALAR;
		AstNode arrayLengthNode = null;
		if (readingIndex<tokens.length-1 && tokens[readingIndex].getValue().equals(ScriptWord.SUBSCRIPT_BEGIN)) {
			int lengthsEnd = getLengthEndIndex(tokens, readingIndex);
			Token[] lengthsTokens = Arrays.copyOfRange(tokens, readingIndex, lengthsEnd+1);
			arrayRank = this.parseVariableDeclarationArrayRank(lengthsTokens);
			if (RANK_OF_SCALAR < arrayRank) {
				arrayLengthNode = this.parseVariableDeclarationArrayLengths(lengthsTokens);
			}
			readingIndex = lengthsEnd + 1;
		}

		// Set aboves to the node.
		variableNode.setAttribute(AttributeKey.ARRAY_RANK, Integer.toString(arrayRank));
		if (arrayLengthNode != null) {
			arrayRank = arrayLengthNode.getChildNodes(AstNode.Type.EXPRESSION).length;
			variableNode.addChildNode(arrayLengthNode);
		}

		// Latter tokens compose an expression for initialization of the declared variable.

		// When the variable name is omitted, it can't have an expression for initialization.
		if (!requiresIdentifier && readingIndex < tokens.length) {
			throw new VnanoException(
					ErrorType.TOO_MANY_TOKENS_FOR_VARIABLE_DECLARATION,
					tokens[readingIndex-1].getFileName(), tokens[readingIndex-1].getLineNumber()
			);
		}

		// Construct the AST of the expression for the initialization, 
		// and link it to the node of the variable declaration as a child node.
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

		// If there are more tokens, their positions are syntactically incorrect.
		if (readingIndex < tokens.length) {
			throw new VnanoException(
					ErrorType.TOO_MANY_TOKENS_FOR_VARIABLE_DECLARATION,
					tokens[readingIndex-1].getFileName(), tokens[readingIndex-1].getLineNumber()
			);
		}

		// Register modifiers to the variable declaration node.
		for (String modifier: modifierList) {
				variableNode.addModifier(modifier);
		}

		return variableNode;
	}


	/**
	 * Parses a part declaring array lengths in a variable declaration statement, 
	 * and returns the declared array-rank.
	 * 
	 * @param tokens Tokens of a part declaring array lengths in a variable declaration statement.
	 * @return The array-rank of the declared variable (returns -1 for an any-rank arary).
	 * @throws VnanoException Thrown when any syntactic error is detected.
	 */
	private int parseVariableDeclarationArrayRank(Token[] tokens) throws VnanoException {
		int tokenLength = tokens.length;

		// If the declaration of array lengths is "[...]", it means the array is "any rank".
		// For detecting syntactical mistake and pointing it out in an error message, 
		// firstly check whether "..." is contained in tokens or not, and then check tokens more strictly.
		boolean isArbitraryRank = false;
		for(int i=0; i<tokenLength; i++) {
			if (tokens[i].getValue().equals(ScriptWord.ARBITRARY_COUNT_MODIFIER)) { // ARBITRARY_COUNT_MODIFIER is "..."
				isArbitraryRank = true;
			}
		}
		if (isArbitraryRank) {

			// If "..." is contained in tokens, contants of tokens should be:  "["   "..."   "]" .
			if (tokenLength == 3
					&& tokens[0].getValue().equals(ScriptWord.SUBSCRIPT_BEGIN)
					&& tokens[1].getValue().equals(ScriptWord.ARBITRARY_COUNT_MODIFIER)
					&& tokens[2].getValue().equals(ScriptWord.SUBSCRIPT_END)) {

				return -1; // Returns -1 for any rank array.

			// Otherwise it probably be a syntactical mistake.
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

		// For non-any rank array, count up its rank.
		int rank = 0;
		int depth = 0; // Incremented at "[", and decremented at "]".
		for(int i=0; i<tokenLength; i++) {
			String word = tokens[i].getValue();

			// Be careful of the case that subscript operators are used in the declaration part of array lengths, e.g.:
			//     int a[ len[0] ][ len[1] ];
			// where "len" is an int-type array.

			if(word.equals(ScriptWord.SUBSCRIPT_BEGIN)) { // "["

				// If the current depth is 0, 
				// the "[" is the beginning of the declaration of the length of the first dimension, so increment the rank.
				// Note that, if the depth isn't 0, the "[" is a subscript operator.
				if (depth==0) {
					rank++;
				}
				depth++;

			} else if(word.equals(ScriptWord.SUBSCRIPT_SEPARATOR)) { // "]["

				// If the current depth is 1, 
				// the "][" is the beginning of the declaration of the length of a new dimension, so increment the rank.
				// Note that, if the depth isn't 1, the "][" is a subscript operator.
				if (depth==1) {
					rank++;
				}

			} else if (word.equals(ScriptWord.SUBSCRIPT_END)) { // "]"
				depth--;
			}
		}
		return rank;
	}


	/**
	 * Parses a part declaring array lengths in a variable declaration statement, 
	 * and returns ASTs of expressions of array-lengths.
	 * 
	 * @param tokens Tokens of a part declaring array lengths in a variable declaration statement.
	 * @return ASTs of expressions of array-lengths.
	 * @throws VnanoException Thrown when any syntactic error is detected.
	 */
	private AstNode parseVariableDeclarationArrayLengths(Token[] tokens) throws VnanoException {
		AstNode lengthsNode = new AstNode(AstNode.Type.LENGTHS, tokens[0].getLineNumber(), tokens[0].getFileName());
		int currentExprBegin = -1;

		int tokenLength = tokens.length;
		int depth = 0; // Incremented at "[", and decremented at "]".
		for(int i=0; i<tokenLength; i++) {
			String word = tokens[i].getValue();

			// Be careful of the case that subscript operators are used in the declaration part of array lengths, e.g.:
			//     int a[ len[0] ][ len[1] ];
			// where "len" is an int-type array.

			if(word.equals(ScriptWord.SUBSCRIPT_BEGIN)) { // "["

				// If the current depth is 0, 
				// the "[" is the beginning of the declaration of the length of the first dimension.
				// Note that, if the depth isn't 0, the "[" is a subscript operator.
				if (depth==0) {
					currentExprBegin = i + 1;
				}
				depth++;

			} else if(word.equals(ScriptWord.SUBSCRIPT_END) || word.equals(ScriptWord.SUBSCRIPT_SEPARATOR)) { // "]" or "]["

				// If the current depth is 1, 
				// the "][" is the beginning of the declaration of the length of a new dimension, so increment the rank.
				// Note that, if the depth isn't 1, the "][" is a subscript operator.
				if (depth==1) {
					Token[] exprTokens = Arrays.copyOfRange(tokens, currentExprBegin, i);

					// If the content between "[" and "]" is empty, regard it as the same as "[0]".
					if (exprTokens.length == 0) {
						AstNode zeroExprNode = new AstNode(AstNode.Type.EXPRESSION, tokens[i].getLineNumber(), tokens[i].getFileName());
						AstNode zeroLeafNode = this.createLeafNode(
								"0", AttributeValue.LITERAL, DataTypeName.DEFAULT_INT, tokens[i].getFileName(), tokens[i].getLineNumber()
						);
						zeroExprNode.addChildNode(zeroLeafNode);
						lengthsNode.addChildNode(zeroExprNode);

					// Otherwise, parse the content between "[" and "]" as an expression, 
					// and link its AST to the variable declaration node as a child node.
					} else {
						lengthsNode.addChildNode( this.parseExpression(exprTokens) );
					}
				}
				if (word.equals(ScriptWord.SUBSCRIPT_END)) {
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
	// ====================================================================================================


	/**
	 * Parses a function declaration statement.
	 *
	 * In the root node of the result of this method, 
	 * the identifier of the function will be stored as IDENTIFIER attribute, 
	 * and the data-type of the return value will be stored as DATA_TYPE attribute, 
	 * and the array-rank of the return value will be stored as RANK attribute.
	 * Also, if there are parameters variables, nodes of their declaration statements
	 * (as VARIABLE type nodes) will be linked as child nodes.
	 * 
	 * Note that, the returned node by this method storing only information of signature of the function.
	 * Internal code of the function written in "{...}" will be parsed as a BLOCK type node later.
	 *
	 * @param tokens Token of a function declaration statement.
	 * @return The constructed AST.
	 * @throws VnanoException Thrown when any syntactic error is detected.
	 */
	private AstNode parseFunctionDeclarationStatement(Token[] tokens) throws VnanoException {

		int tokenLength = tokens.length;
		int lineNumber = tokens[0].getLineNumber();
		String fileName = tokens[0].getFileName();

		int rank = 0;
		Token identifierToken = null;

		// The first token represents the data type of the return value.
		Token dataTypeToken = tokens[0];

		// Read tokens of array information of the return value, and identifier of the function.
		int readingIndex = 1;
		while(readingIndex < tokenLength) {

			// Identifier of the function:
			if (tokens[readingIndex].getType() == Token.Type.LEAF
					&& tokens[readingIndex].getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.FUNCTION_IDENTIFIER) ) {
				identifierToken = tokens[readingIndex];
				readingIndex++;
				break;

			// Array information of the return value:
			} else {

				// Increment the array rank if the token is "[" or "][".
				String operatorSyntax = tokens[readingIndex].getAttribute(AttributeKey.OPERATOR_SYNTAX);
				if (operatorSyntax.equals(AttributeValue.MULTIARY) || operatorSyntax.equals(AttributeValue.MULTIARY_SEPARATOR)) {
					rank++;
				}
			}
			readingIndex++;
		}

		// Skip the next token because it is "(".
		readingIndex++;

		// Parse declarations of parameter variables.
		int argumentBegin = readingIndex;
		List<AstNode> argumentNodeList = new ArrayList<AstNode>();
		while (readingIndex < tokenLength) {

			// Split tokens at the point of "," or ")", 
			// and parse tokens before it as a declaration statement of a parameter variable.
			if (tokens[readingIndex].getValue().equals(ScriptWord.ARGUMENT_SEPARATOR) ||
					tokens[readingIndex].getValue().equals(ScriptWord.PARENTHESIS_END)) {

				Token[] argTokens = Arrays.copyOfRange(tokens, argumentBegin, readingIndex);
				if (0 < argTokens.length) {

					// This method is also used for parsing signature, so set the parameter name omittable here.
					// They will be checked by SemanticAnalyzer later, if they are mandatory.
					boolean requiresParameterNames = false;
					AstNode argNode = this.parseVariableDeclarationStatement(argTokens, requiresParameterNames);
					argumentNodeList.add(argNode);
					argumentBegin = readingIndex + 1;
				}
			}
			readingIndex++;
		}

		// Create an AST node of the function declaration statement, and register information and child nodes.
		AstNode node = new AstNode(AstNode.Type.FUNCTION, lineNumber, fileName);
		node.setAttribute(AttributeKey.IDENTIFIER_VALUE, identifierToken.getValue());
		node.setAttribute(AttributeKey.DATA_TYPE, dataTypeToken.getValue());
		node.setAttribute(AttributeKey.ARRAY_RANK, Integer.toString(rank));
		for (AstNode argNode: argumentNodeList) {
			node.addChildNode(argNode);
		}
		return node;
	}


	/**
	 * Looks ahead tokens from the specified index, and determine whether it is a function declaration statement or not.
	 *
	 * @param tokens Tokens in which a function declaration statement probably be contained.
	 * @param begin The index from which a function declaration statement probably begin.
	 * @return Returns true if a function declaration statement begins from the specified index.
	 */
	private boolean startsWithFunctionDeclarationTokens(Token[] tokens, int begin) {
		int tokenLength = tokens.length;

		// In vnano, a function declaration statement always begins with a data-type.
		if (tokens[begin].getType() != Token.Type.DATA_TYPE) {
			return false;
		}

		// Read latter tokens.
		int readingIndex = begin + 1;
		while (readingIndex < tokenLength) {

			Token readingToken = tokens[readingIndex];

			// True if the reading token is a function identifier.
			boolean readingTokenIsFunctionIdenfifier = readingToken.getType() == Token.Type.LEAF
					&& readingToken.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.FUNCTION_IDENTIFIER);

			// True if the reading token is "[" or "]".
			boolean readingTokenIsIndex = readingToken.getType() == Token.Type.OPERATOR
					&& readingToken.getAttribute(AttributeKey.OPERATOR_EXECUTOR) == AttributeValue.SUBSCRIPT;

			// If a function identifier found after the data-type (including [ ]), it is a function declaration.
			if (readingTokenIsFunctionIdenfifier) {
				return true;

			// If any other kinds of token exists here, excluding "[" or "]", the tokens is not a function declaration statement.
			} else if (!readingTokenIsIndex) {
				return false;
			}
			readingIndex++;
		}

		// When no function identifier has found, the tokens is not a function declaration statement.
		return false;
	}





	// ====================================================================================================
	// Parsing of Control Statements
	// ====================================================================================================


	/**
	 * Parses a control statement (if, else, for, and so on).
	 *
	 * When the control statement is "if" or "while", to its node returned by this method,
	 * an AST of a conditional expression is linked as a child node.
	 *
	 * When the control statement is "for", to its node returned by this method, 
	 * ASTs of a declaraton statement of a counter variable, 
	 * of a conditional expression, and of an process for updating the counter are linked as child nodes.
	 * 
	 * @param tokens Tokens composing a control statement.
	 * @return The constructed AST.
	 * @throws VnanoException VnanoException Thrown when any syntactic error is detected.
	 */
	private AstNode parseControlStatement(Token[] tokens) throws VnanoException {
		new LexicalChecker().checkControlStatementTokens(tokens);

		Token controlTypeToken = tokens[0];
		int lineNumber = controlTypeToken.getLineNumber();
		String fileName = controlTypeToken.getFileName();

		// "if" statement: 
		// create its node, and parse the conditional expression, and link the latter node to the former node as a child node.
		if(controlTypeToken.getValue().equals(ScriptWord.IF)) {
			AstNode node = new AstNode(AstNode.Type.IF, lineNumber, fileName);
			node.addChildNode(this.parseExpression(Arrays.copyOfRange(tokens, 2, tokens.length-1)));
			return node;

		// "else" statement:
		// create its node.
		} else if(controlTypeToken.getValue().equals(ScriptWord.ELSE)) {
			AstNode node = new AstNode(AstNode.Type.ELSE, lineNumber, fileName);
			return node;

		// "while" statement: 
		// create its node, and parse the conditional expression, and link the latter node to the former node as a child node.
		} else if(controlTypeToken.getValue().equals(ScriptWord.WHILE)) {
			AstNode node = new AstNode(AstNode.Type.WHILE, lineNumber, fileName);
			node.addChildNode(this.parseExpression(Arrays.copyOfRange(tokens, 2, tokens.length-1)));
			return node;

		// "for" statement: 
		// create its node, 
		// and parse the counter declaration statement, the conditional expression, and process for updating the counter.
		// Then link their node to the "for" statement node as child nodees.
		} else if(controlTypeToken.getValue().equals(ScriptWord.FOR)) {
			AstNode node = new AstNode(AstNode.Type.FOR, lineNumber, fileName);

			// Get indices at the end of the counter declaration statement and the conditional expression.
			int counterDeclEnd = Token.getIndexOf(tokens, ScriptWord.END_OF_STATEMENT, 0);
			int conditionEnd = Token.getIndexOf(tokens, ScriptWord.END_OF_STATEMENT, counterDeclEnd+1);

			// Parse the counter declaration statement. Note that, syntactically, it may be other kinds of statement.
			// When it is a variable declaration statement:
			if (DataTypeName.isDataTypeName(tokens[2].getValue())) {
				node.addChildNode(this.parseVariableDeclarationStatement(Arrays.copyOfRange(tokens, 2, counterDeclEnd), true));
			// When it is an empty statement:
			} else if (counterDeclEnd == 2) {
				node.addChildNode(new AstNode(AstNode.Type.EMPTY, tokens[0].getLineNumber(), tokens[0].getFileName()));
			// When it is an expression statement:
			} else {
				node.addChildNode(this.parseExpression(Arrays.copyOfRange(tokens, 2, counterDeclEnd)));
			}

			// Parse the conditional expression. Note that, syntactically, it may be other kinds of statement.
			// When it is an empty statement:
			if (counterDeclEnd+1 == conditionEnd) {
				node.addChildNode(new AstNode(AstNode.Type.EMPTY, tokens[0].getLineNumber(), tokens[0].getFileName()));
			// When it is an expression statement:
			} else {
				node.addChildNode(this.parseExpression(Arrays.copyOfRange(tokens, counterDeclEnd+1, conditionEnd)));
			}

			// Parse the process for updating the counter. Note that, syntactically, it may be other kinds of statement.
			// When it is an empty statement:
			if (conditionEnd+1 == tokens.length-1) {
				node.addChildNode(new AstNode(AstNode.Type.EMPTY, tokens[0].getLineNumber(), tokens[0].getFileName()));
			// When it is an expression statement:
			} else {
				node.addChildNode(this.parseExpression(Arrays.copyOfRange(tokens, conditionEnd+1, tokens.length-1)));
			}

			return node;

		// "return" statement: 
		// create its node, and link the node of the value (may be an expression) to be returned as a child node.
		} else if(controlTypeToken.getValue().equals(ScriptWord.RETURN)) {
			AstNode node = new AstNode(AstNode.Type.RETURN, lineNumber, fileName);
			if (2 <= tokens.length) {
				node.addChildNode(this.parseExpression(Arrays.copyOfRange(tokens, 1, tokens.length)));
			}
			return node;

		// "break" statement: 
		// create its node.
		} else if(controlTypeToken.getValue().equals(ScriptWord.BREAK)) {
			AstNode node = new AstNode(AstNode.Type.BREAK, lineNumber, fileName);
			return node;

		// "continue" statement: 
		// create its node.
		} else if(controlTypeToken.getValue().equals(ScriptWord.CONTINUE)) {
			AstNode node = new AstNode(AstNode.Type.CONTINUE, lineNumber, fileName);
			return node;

		} else {
			throw new VnanoFatalException("Unknown controll statement: " + controlTypeToken.getValue());
		}
	}





	// ====================================================================================================
	// Parsing of Dependency Declaration Statements (import / include)
	// ====================================================================================================


	/**
	 * Parses a dependency declaration statement (import / include).
	 * 
	 * @param tokens Tokens composing a declaration declaration statement.
	 * @return The constructed AST.
	 * @throws VnanoException VnanoException Thrown when any syntactic error is detected.
	 */
	private AstNode parseDependencyDeclarationStatement(Token[] tokens) throws VnanoException {
		
		// If there is no token, it will not be passed to this method, so the tokens[0] always exists.
		Token declaratorToken = tokens[0];
		int lineNumber = declaratorToken.getLineNumber();
		String fileName = declaratorToken.getFileName();

		// Check the number of tokens: must be 2.
		if (tokens.length != 2) {
			throw new VnanoException(ErrorType.INVALID_DEPENDENCY_DECLARATION_SYNTAX, fileName, lineNumber);
		}

		// Create a IMPORT/INCLUDE node.
		AstNode node = null;
		if (declaratorToken.getValue().equals(ScriptWord.IMPORT)) {
			node = new AstNode(AstNode.Type.IMPORT, lineNumber, fileName);
		} else if (declaratorToken.getValue().equals(ScriptWord.INCLUDE)) {
			node = new AstNode(AstNode.Type.INCLUDE, lineNumber, fileName);			
		} else {
			throw new VnanoFatalException("Unknown dependency declarator: " + declaratorToken.getValue());
		}

		// Create a LEAF node of the dependency identifier node, and connect it to the above node.
		node.addChildNode(this.createLeafNode(tokens[1]));

		return node;
	}





	// ====================================================================================================
	// Others (Utilities, etc.)
	// ====================================================================================================


	/**
	 * Creates an AST node of an operator.
	 *
	 * @param token The token of an operator.
	 * @return The created node.
	 */
	private AstNode createOperatorNode(Token token) {
		AstNode operatorNode = new AstNode(AstNode.Type.OPERATOR, token.getLineNumber(), token.getFileName());
		operatorNode.setAttribute(AttributeKey.OPERATOR_ASSOCIATIVITY, token.getAttribute(AttributeKey.OPERATOR_ASSOCIATIVITY));
		operatorNode.setAttribute(AttributeKey.OPERATOR_SYNTAX, token.getAttribute(AttributeKey.OPERATOR_SYNTAX));
		operatorNode.setAttribute(AttributeKey.OPERATOR_EXECUTOR, token.getAttribute(AttributeKey.OPERATOR_EXECUTOR));
		operatorNode.setAttribute(AttributeKey.OPERATOR_SYMBOL, token.getValue());
		operatorNode.setAttribute(AttributeKey.OPERATOR_PRECEDENCE, Integer.toString(token.getPrecedence()));

		// Some kinds of operator tokens have data-type / array-ranks (e.g.: cast operators).
		// Copy attributes of them for such operators.
		if (token.hasAttribute(AttributeKey.DATA_TYPE)) {
			operatorNode.setAttribute(AttributeKey.DATA_TYPE, token.getAttribute(AttributeKey.DATA_TYPE));
		}
		if (token.hasAttribute(AttributeKey.ARRAY_RANK)) {
			operatorNode.setAttribute(AttributeKey.ARRAY_RANK, token.getAttribute(AttributeKey.ARRAY_RANK));
		}

		return operatorNode;
	}


	/**
	* Creates an AST node of a leaf (an identifier or a literal), from a token.
	 *
	 * @param token The token of an identifier or a literal.
	 * @return The created node.
	 */
	private AstNode createLeafNode(Token token) {
		AstNode node = new AstNode(AstNode.Type.LEAF, token.getLineNumber(), token.getFileName());
		node.setAttribute(AttributeKey.LEAF_TYPE, token.getAttribute(AttributeKey.LEAF_TYPE));

		// Literal:
		if (token.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.LITERAL)) {
			node.setAttribute(AttributeKey.LITERAL_VALUE, token.getValue());
			node.setAttribute(AttributeKey.DATA_TYPE, token.getAttribute(AttributeKey.DATA_TYPE));

		// Identifier:
		} else {
			node.setAttribute(AttributeKey.IDENTIFIER_VALUE, token.getValue());
		}
		return node;
	}


	/**
	 * Creates an AST node of a leaf, from a string value of an identifier or a literal.
	 * 
	 * @param tokenValue A string value of an identifier or a literal.
	 * @param leafType The type of the leaf (The value of {@link AttributeKey#LEAF_TYPE LEAF_TYPE} attribute)
	 * @param dataType The data-type of the leaf.
	 * @return The created node.
	 */
	private AstNode createLeafNode(String tokenValue, String leafType, String dataType, String fileName, int lineNumber) {
		Token token = new Token(tokenValue, lineNumber, fileName);
		token.setType(Token.Type.LEAF);
		token.setAttribute(AttributeKey.LEAF_TYPE, leafType);
		token.setAttribute(AttributeKey.DATA_TYPE, dataType);
		return this.createLeafNode(token);
	}


	/**
	 * Push a stack-lid node to the stack.
	 * 
	 * A stack node is a temporary node for isolating inner nodes in a partial expression from its outer nodes.
	 * The parser links nodes in an expression based on precedence of operators.
	 * Hence, if a temporary nodes having the least-prior precedence are inserted to positions of "(", 
	 * nodes of subsequent tokens can't be linked to the node of the previous operator of "(". 
	 * So tokens after "(" will be constructed as an lsolated AST.
	 * Then, when the parser read ")", the AST of the partial expression on the stack will be popped 
	 * and will be linked to the node of the previous operator of "(".
	 *
	 * @param stack The working stack used in {@link Parser#parseExpression} method.
	 */
	private void pushLid(Deque<AstNode> stack) {
		AstNode stackLid = new AstNode(AstNode.Type.STACK_LID, 0, "");
		stackLid.setAttribute(AttributeKey.OPERATOR_PRECEDENCE, Integer.toString(OperatorPrecedence.LEAST_PRIOR));
		stack.push(stackLid);
	}


	/**
	 * Push a stack-lid node to the stack, with a marker to identify the lid.
	 * 
	 * This method is a derived version of {@link Parser# pushLid(Deque<AstNode> stack)} method, 
	 * having an additional parameter "marker".
	 * The parameter "marker" is used for identify multiple partial expressions of 
	 * arguments of a call operator, or of indices of a subscript operator.
	 * They may be nested each other (e.g.: a[ f(1+2) + 3 ], f(a[1+2]+3), and so on), 
	 * so the parser is required to distinguish them by using markers.
	 * 
	 * @param stack The working stack used in {@link Parser#parseExpression} method.
	 * @param marker The marker to identify the lid to be pushed.
	 * @see Parser#popPartialExpressionNodes(Deque<AstNode>, String, String, int) popPartialExpressionNodes
	 */
	private void pushLid(Deque<AstNode> stack, String marker) {
		AstNode stackLid = new AstNode(AstNode.Type.STACK_LID, 0, "");
		stackLid.setAttribute(AttributeKey.OPERATOR_PRECEDENCE, Integer.toString(OperatorPrecedence.LEAST_PRIOR));
		stackLid.setAttribute(AttributeKey.LID_MARKER, marker);
		stack.push(stackLid);
	}


	/**
	 * Pops a node from the top of the stack, with checking its type.
	 * 
	 * @param stack The working stack used in {@link Parser#parseExpression} method.
	 * @param fileName The name of the file in which the currently parsing expression is contained (usen in an error message).
	 * @param lineNumber The line number of the currently parsing expression is contained (usen in an error message).
	 * @param expectedTypes Expected type(s) of the node at the top of the stack.
	 * @return The node popped from the top of the stack.
	 * @throws VnanoException
	 *           Thrown when the type of the node at the top of the stack doesn't match expected types,
	 *           or when there is no node in the stack.
	 */
	private AstNode popNode(Deque<AstNode> stack, String fileName, int lineNumber, AstNode.Type ...expectedTypes)
			throws VnanoException {

		if (stack.size() == 0) {
			throw new VnanoException(ErrorType.INVALID_EXPRESSION_SYNTAX, fileName, lineNumber);
		}

		// Check the type of the node at the top of the stack.
		if (expectedTypes.length != 0) {
			boolean matched = false;
			AstNode.Type type = stack.peek().getType();
			for (AstNode.Type expectedType: expectedTypes) {
				if (type == expectedType) {
					matched = true;
					break;
				}
			}
			if (!matched) {
				throw new VnanoException(ErrorType.INVALID_EXPRESSION_SYNTAX, fileName, lineNumber);
			}
		}

		return stack.pop();
	}



	/**
	 * From the stack, pops all nodes being above an stack lid, and returns them as an array.
	 * Order of nodes in the returned array is FIFO.
	 * This method is used for collecting nodes of statements in a block "{...}", 
	 * when the parser has read the end of the block "}".
	 *
	 * @param stack The working stack used in {@link Parser#parse} method.
	 * @return An array in which nodes of statements are stored (their order is FIFO).
	 */
	private AstNode[] popStatementNodes(Deque<AstNode> stack) {

		List<AstNode> statementNodeList = new ArrayList<AstNode>();
		while(stack.size() != 0) {

			// If a "lid" node is found, remove it and finish collecting.
			if (stack.peek().getType()==AstNode.Type.STACK_LID) {
				stack.pop();
				break;
			}

			// Pop a statement node.
			AstNode statementNode = stack.pop();
			statementNodeList.add(statementNode);
		}

		// Reorder nodes to the order of FIFO.
		Collections.reverse(statementNodeList);

		return statementNodeList.toArray(new AstNode[0]);
	}


	/**
	 * From the stack, pops all nodes being above an stack lid having the specified marker, 
	 * and returns them as an array. Order of nodes in the returned array is FIFO.
	 * This method is used for collecting nodes of partial expressions.
	 * 
	 * In the stack, multiple nodes of partial expressions to be collected 
	 * must be separated by stack lids having different markers from the specified argument "marker".
	 *
	 * @param stack The working stack used in {@link Parser#parseExpression} method.
	 * @param marker The marker of the lid at the beginning of the first partial expression's node to be collected.
	 * @param fileName The name of the file in which the currently parsing expression is contained (usen in an error message).
	 * @param lineNumber The line number of the currently parsing expression is contained (usen in an error message).
	 * @return An array in which nodes of partial expressions are stored (their order is FIFO).
	 * @throws VnanoException Thrown when contents of the stack is incorrect.
	 */
	private AstNode[] popPartialExpressionNodes(Deque<AstNode> stack, String marker, String fileName, int lineNumber)
			throws VnanoException {

		if (stack.size() == 0) {
			throw new VnanoException(ErrorType.INVALID_EXPRESSION_SYNTAX, fileName, lineNumber);
		}

		List<AstNode> partialExprNodeList = new ArrayList<AstNode>();
		while(stack.size() != 0) {

			// If the parsing of a partial expression completed expectedly, 
			// a root node of the AST of it is pushed to the stack. So pop it.
			// (Note that, when the content of the partial expression is empty, there is no node between stack lids.)
			if (stack.peek().getType() != AstNode.Type.STACK_LID) { // 部分式が無い場合はフタがあるだけ
				partialExprNodeList.add(stack.pop());
			}

			// If the parsing of a partial expression completed expectedly, the next node on the stack is a lid.
			if (stack.size() == 0) {
				throw new VnanoException(ErrorType.INVALID_EXPRESSION_SYNTAX, fileName, lineNumber);
			}

			// Remove the lid.
			if (stack.peek().getType() == AstNode.Type.STACK_LID) {
				AstNode stackLid = stack.pop();

				// If the marker of the lid is the same as the marker specified as an argument, finish collecting.
				if (stackLid.hasAttribute(AttributeKey.LID_MARKER)
						&& stackLid.getAttribute(AttributeKey.LID_MARKER).equals(marker)) {

					break;
				}
			}
		}

		// Reorder nodes to the order of FIFO.
		Collections.reverse(partialExprNodeList);

		return partialExprNodeList.toArray(new AstNode[0]);
	}


	/**
	 * Replace all tokens composing cast operators (e.g.: "(", "int", ")" ) contained in specified tokens, 
	 * to single-token cast operators.
	 *
	 * @param Tokens in which all tokens composing cast operators are replaced to single-token cast operators.
	 */
	private Token[] preprocessCastSequentialTokens(Token[] tokens) {

		int tokenLength = tokens.length;
		int readingIndex = 0;

		// Stores tokens to be returned.
		List<Token> tokenList = new ArrayList<Token>();

		while (readingIndex<tokenLength) {

			Token readingToken = tokens[readingIndex];

			boolean isCastBeginToken = readingIndex < tokenLength-2
				&& readingToken.getType() == Token.Type.OPERATOR
				&& readingToken.getAttribute(AttributeKey.OPERATOR_EXECUTOR).equals(AttributeValue.CAST);

			// If the reading token is a beginning of a cast operator, to remake it as a single-token cast operator,
			// add attributes of the data-type and the array-rank read from latter tokens composing the cast operator.
			// And add only the single-token cast operator to tokenList.
			if (isCastBeginToken) {
				String dataType = tokens[ readingIndex+1 ].getValue();
				readingToken.setAttribute(AttributeKey.DATA_TYPE, dataType);
				readingToken.setAttribute(AttributeKey.ARRAY_RANK, Integer.toString(RANK_OF_SCALAR)); // The cast of arrays have not been supported yet.
				readingToken.setValue(ScriptWord.PARENTHESIS_BEGIN + dataType + ScriptWord.PARENTHESIS_END);
				tokenList.add(readingToken);
				readingIndex += 3;

			// Other kinds of tokens:
			} else {
				tokenList.add(readingToken);
				readingIndex++;
			}
		}

		Token[] resultTokens = tokenList.toArray(new Token[0]);
		return resultTokens;
	}


	/**
	 * Finds the token "]" at the end of declaration of array lengths, 
	 * from tokens of a variable declaration statements.
	 *
	 * @param tokens Tokens of a variable declaration statements.
	 * @param fromIndex The beginning index of the search.
	 * @return The index of the found token, or -1 if it hasn't been found.
	 */
	private int getLengthEndIndex(Token[] tokens, int fromIndex) {

		int tokenLength = tokens.length;
		int depth = 0; // Incremented at "[", and decremented at "]".

		for(int i=fromIndex; i<tokenLength; i++) {
			String word = tokens[i].getValue();

			// If it reaches to the end of tokens without finding "]", 
			// the declared variable is not an array.
			// Then this method should return -1.
			if (depth==0 && word.equals(ScriptWord.ASSIGNMENT)) {
				return -1;
			}

			// "[": Increment the depth.
			if(word.equals(ScriptWord.SUBSCRIPT_BEGIN)) {
				depth++;

			// "]": Dencrement the depth.
			} else if(word.equals(ScriptWord.SUBSCRIPT_END)) {
				depth--;

				// If the depth is 0, it is the end of declaration of array lengths. So return the index of it.
				// (If the depth is non-zero, it is an end of a subscript operator, not declaration of array lengths.)
				if (depth == 0) {
					return i;
				}
			}
		}
		return -1;
	}

}

