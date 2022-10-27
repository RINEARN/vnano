package org.vcssl.nano.compiler;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.spec.OperatorPrecedence;
import org.vcssl.nano.spec.ScriptWord;
import org.vcssl.nano.spec.DataTypeName;

public class ParserTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	private Token createLiteralToken(String word, String dataTypeName) {
		Token token = new Token(word, 123, "Test.vnano");
		token.setType(Token.Type.LEAF);
		token.setAttribute(AttributeKey.LEAF_TYPE, AttributeValue.LITERAL);
		token.setAttribute(AttributeKey.DATA_TYPE, dataTypeName);
		return token;
	}

	private Token createVariableIdentifierToken(String word) {
		Token token = new Token(word, 123, "Test.vnano");
		token.setType(Token.Type.LEAF);
		token.setAttribute(AttributeKey.LEAF_TYPE, AttributeValue.VARIABLE_IDENTIFIER);
		return token;
	}

	private Token createDataTypeToken(String word) {
		Token token = new Token(word, 123, "Test.vnano");
		token.setType(Token.Type.DATA_TYPE);
		return token;
	}

	private Token createOperatorToken(String word, int priority, String associativity, String syntax, String executor) {

		Token token = new Token(word, 124, "Test.vnano");
		token.setType(Token.Type.OPERATOR);
		token.setPrecedence(priority);
		token.setAttribute(AttributeKey.OPERATOR_ASSOCIATIVITY, associativity);
		token.setAttribute(AttributeKey.OPERATOR_SYNTAX, syntax);
		token.setAttribute(AttributeKey.OPERATOR_EXECUTOR, executor);
		return token;
	}

	private Token createParenthesisToken(String word) {
		Token token = new Token(word, 124, "Test.vnano");
		token.setType(Token.Type.PARENTHESIS);
		if (word.equals(ScriptWord.PARENTHESIS_BEGIN)) {
			token.setPrecedence(OperatorPrecedence.PARENTHESIS_BEGIN);
		} else {
			token.setPrecedence(OperatorPrecedence.LEAST_PRIOR);
		}
		return token;
	}

	private Token createBlockToken(String word) {
		Token token = new Token(word, 123, "Test.vnano");
		token.setType(Token.Type.BLOCK);
		return token;
	}

	private Token createControlToken(String word) {
		Token token = new Token(word, 123, "Test.vnano");
		token.setType(Token.Type.CONTROL);
		return token;
	}

	private Token createEndToken() {
		Token token = new Token(ScriptWord.END_OF_STATEMENT, 123, "Test.vnano");
		token.setType(Token.Type.END_OF_STATEMENT);
		return token;
	}

	@SuppressWarnings("unused")
	private void dumpTokens(Token[] tokens) {
		for (Token token: tokens) {
			System.out.println(token);
		}
	}

	private void checkLiteralNode(AstNode node, String word) {
		assertEquals(0, node.getChildNodes().length);
		assertEquals(AstNode.Type.LEAF, node.getType());
		assertEquals(AttributeValue.LITERAL, node.getAttribute(AttributeKey.LEAF_TYPE));
		assertEquals(word, node.getAttribute(AttributeKey.LITERAL_VALUE));
	}

	private void checkVariableIdentifierNode(AstNode node, String word) {
		assertEquals(0, node.getChildNodes().length);
		assertEquals(AstNode.Type.LEAF, node.getType());
		assertEquals(AttributeValue.VARIABLE_IDENTIFIER, node.getAttribute(AttributeKey.LEAF_TYPE));
		assertEquals(word, node.getAttribute(AttributeKey.IDENTIFIER_VALUE));
	}

	private void checkOperatorNode(AstNode node, String symbol, int priority, String associativity, String syntax, String executor) {
		assertEquals(AstNode.Type.OPERATOR, node.getType());
		assertEquals(symbol, node.getAttribute(AttributeKey.OPERATOR_SYMBOL));
		assertEquals(associativity, node.getAttribute(AttributeKey.OPERATOR_ASSOCIATIVITY));
		assertEquals(syntax, node.getAttribute(AttributeKey.OPERATOR_SYNTAX));
		assertEquals(executor, node.getAttribute(AttributeKey.OPERATOR_EXECUTOR));
		assertEquals(Integer.toString(priority), node.getAttribute(AttributeKey.OPERATOR_PRECEDENCE));
	}





	@Test
	public void testParseVariableDeclarationStatementScalar() throws VnanoException {

		// "int x;"
		Token[] tokens = new Token[]{
			this.createDataTypeToken(DataTypeName.DEFAULT_INT),
			this.createVariableIdentifierToken("x"),
			this.createEndToken()
		};

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		AstNode varNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.VARIABLE, varNode.getType());
		assertEquals(DataTypeName.DEFAULT_INT, varNode.getAttribute(AttributeKey.DATA_TYPE));
		assertEquals("x", varNode.getAttribute(AttributeKey.IDENTIFIER_VALUE));
		assertEquals("0", varNode.getAttribute(AttributeKey.ARRAY_RANK));
	}

	//@Test
	public void testParseVariableDeclarationStatementArray1D() throws VnanoException {

		// "int x [ 2 ];"
		Token[] tokens = new Token[]{
			this.createDataTypeToken(DataTypeName.DEFAULT_INT),
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken("[", OperatorPrecedence.SUBSCRIPT_BEGIN, AttributeValue.LEFT, AttributeValue.MULTIARY, AttributeValue.SUBSCRIPT),
			this.createLiteralToken("2", DataTypeName.DEFAULT_INT),
			this.createOperatorToken("]", OperatorPrecedence.SUBSCRIPT_END, AttributeValue.LEFT, AttributeValue.MULTIARY, AttributeValue.SUBSCRIPT),
			this.createEndToken()
		};

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		AstNode varNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.VARIABLE, varNode.getType());
		assertEquals(DataTypeName.DEFAULT_INT, varNode.getAttribute(AttributeKey.DATA_TYPE));
		assertEquals("x", varNode.getAttribute(AttributeKey.IDENTIFIER_VALUE));
		assertEquals("1", varNode.getAttribute(AttributeKey.ARRAY_RANK));

		AstNode lengthNode = varNode.getChildNodes()[0];
		assertEquals(AstNode.Type.LENGTHS, lengthNode.getType());
		assertEquals(1, lengthNode.getChildNodes().length);

		AstNode lengthExprNode = lengthNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, lengthExprNode.getType());
		assertEquals(1, lengthExprNode.getChildNodes().length);
		this.checkLiteralNode(lengthExprNode.getChildNodes()[0], "2");
	}

	@Test
	public void testParseVariableDeclarationStatementArray1DWithLengthExpr() throws VnanoException {

		// "int x [ 1 + 2 * 3 ];"
		Token[] tokens = new Token[]{
			this.createDataTypeToken(DataTypeName.DEFAULT_INT),
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken("[", OperatorPrecedence.SUBSCRIPT_BEGIN, AttributeValue.LEFT, AttributeValue.MULTIARY, AttributeValue.SUBSCRIPT),
			this.createLiteralToken("1", DataTypeName.DEFAULT_INT),
			this.createOperatorToken("+", OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("2", DataTypeName.DEFAULT_INT),
			this.createOperatorToken("*", OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("3", DataTypeName.DEFAULT_INT),
			this.createOperatorToken("]", OperatorPrecedence.SUBSCRIPT_END, AttributeValue.LEFT, AttributeValue.MULTIARY, AttributeValue.SUBSCRIPT),
			this.createEndToken()
		};

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		AstNode varNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.VARIABLE, varNode.getType());
		assertEquals(DataTypeName.DEFAULT_INT, varNode.getAttribute(AttributeKey.DATA_TYPE));
		assertEquals("x", varNode.getAttribute(AttributeKey.IDENTIFIER_VALUE));
		assertEquals("1", varNode.getAttribute(AttributeKey.ARRAY_RANK));

		AstNode lengthNode = varNode.getChildNodes()[0];
		assertEquals(AstNode.Type.LENGTHS, lengthNode.getType());
		assertEquals(1, lengthNode.getChildNodes().length);

		AstNode lengthExprNode = lengthNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, lengthExprNode.getType());
		assertEquals(1, lengthExprNode.getChildNodes().length);

		AstNode addNode = lengthExprNode.getChildNodes()[0];
		this.checkOperatorNode(addNode, "+", OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(addNode.getChildNodes()[0], "1");

		AstNode mulNode = addNode.getChildNodes()[1];
		this.checkOperatorNode(mulNode, "*", OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(mulNode.getChildNodes()[0], "2");
		this.checkLiteralNode(mulNode.getChildNodes()[1], "3");
	}

	@Test
	public void testParseVariableDeclarationStatementArray3D() throws VnanoException {

		// "int x [ 2 ][ 3 ][ 4 ];"
		Token[] tokens = new Token[]{
			this.createDataTypeToken(DataTypeName.DEFAULT_INT),
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken("[", OperatorPrecedence.SUBSCRIPT_BEGIN, AttributeValue.LEFT, AttributeValue.MULTIARY, AttributeValue.SUBSCRIPT),
			this.createLiteralToken("2", DataTypeName.DEFAULT_INT),
			this.createOperatorToken("][", OperatorPrecedence.SUBSCRIPT_SEPARATOR, AttributeValue.LEFT, AttributeValue.MULTIARY, AttributeValue.SUBSCRIPT),
			this.createLiteralToken("3", DataTypeName.DEFAULT_INT),
			this.createOperatorToken("][", OperatorPrecedence.SUBSCRIPT_SEPARATOR, AttributeValue.LEFT, AttributeValue.MULTIARY, AttributeValue.SUBSCRIPT),
			this.createLiteralToken("4", DataTypeName.DEFAULT_INT),
			this.createOperatorToken("]", OperatorPrecedence.SUBSCRIPT_END, AttributeValue.LEFT, AttributeValue.MULTIARY, AttributeValue.SUBSCRIPT),
			this.createEndToken()
		};

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		AstNode varNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.VARIABLE, varNode.getType());
		assertEquals(DataTypeName.DEFAULT_INT, varNode.getAttribute(AttributeKey.DATA_TYPE));
		assertEquals("x", varNode.getAttribute(AttributeKey.IDENTIFIER_VALUE));
		assertEquals("3", varNode.getAttribute(AttributeKey.ARRAY_RANK));

		AstNode lengthNode = varNode.getChildNodes()[0];
		assertEquals(AstNode.Type.LENGTHS, lengthNode.getType());
		assertEquals(3, lengthNode.getChildNodes().length);

		AstNode lengthExprNode0 = lengthNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, lengthExprNode0.getType());
		assertEquals(1, lengthExprNode0.getChildNodes().length);
		this.checkLiteralNode(lengthExprNode0.getChildNodes()[0], "2");

		AstNode lengthExprNode1 = lengthNode.getChildNodes()[1];
		assertEquals(AstNode.Type.EXPRESSION, lengthExprNode1.getType());
		assertEquals(1, lengthExprNode1.getChildNodes().length);
		this.checkLiteralNode(lengthExprNode1.getChildNodes()[0], "3");

		AstNode lengthExprNode2 = lengthNode.getChildNodes()[2];
		assertEquals(AstNode.Type.EXPRESSION, lengthExprNode2.getType());
		assertEquals(1, lengthExprNode2.getChildNodes().length);
		this.checkLiteralNode(lengthExprNode2.getChildNodes()[0], "4");
	}

	@Test
	public void testParseVariableDeclarationStatementWithInitExpr() throws VnanoException {

		// "int x = 1 + 2 * 3;"
		Token[] tokens = new Token[]{
			this.createDataTypeToken(DataTypeName.DEFAULT_INT),
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken("=", OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createLiteralToken("1", DataTypeName.DEFAULT_INT),
			this.createOperatorToken("+", OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("2", DataTypeName.DEFAULT_INT),
			this.createOperatorToken("*", OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("3", DataTypeName.DEFAULT_INT),
			this.createEndToken()
		};

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		AstNode varNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.VARIABLE, varNode.getType());
		assertEquals(DataTypeName.DEFAULT_INT, varNode.getAttribute(AttributeKey.DATA_TYPE));
		assertEquals("x", varNode.getAttribute(AttributeKey.IDENTIFIER_VALUE));
		assertEquals("0", varNode.getAttribute(AttributeKey.ARRAY_RANK));

		AstNode initExprNode = varNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, initExprNode.getType());
		assertEquals(1, initExprNode.getChildNodes().length);

		AstNode assignNode = initExprNode.getChildNodes()[0];
		this.checkOperatorNode(assignNode, "=", OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		this.checkVariableIdentifierNode(assignNode.getChildNodes()[0], "x");

		AstNode addNode = assignNode.getChildNodes()[1];
		this.checkOperatorNode(addNode, "+", OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(addNode.getChildNodes()[0], "1");

		AstNode mulNode = addNode.getChildNodes()[1];
		this.checkOperatorNode(mulNode, "*", OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(mulNode.getChildNodes()[0], "2");
		this.checkLiteralNode(mulNode.getChildNodes()[1], "3");
	}








	@Test
	public void testBlockStatement() throws VnanoException {

		// "{ 1; 2; { 3; 4; { 5; } 6; } 7; }"
		Token[] tokens = new Token[]{
				this.createBlockToken(ScriptWord.BLOCK_BEGIN),
			this.createLiteralToken("1", DataTypeName.DEFAULT_INT),
			this.createEndToken(),
			this.createLiteralToken("2", DataTypeName.DEFAULT_INT),
			this.createEndToken(),
			this.createBlockToken(ScriptWord.BLOCK_BEGIN),
			this.createLiteralToken("3", DataTypeName.DEFAULT_INT),
			this.createEndToken(),
			this.createLiteralToken("4", DataTypeName.DEFAULT_INT),
			this.createEndToken(),
			this.createBlockToken(ScriptWord.BLOCK_BEGIN),
			this.createLiteralToken("5", DataTypeName.DEFAULT_INT),
			this.createEndToken(),
			this.createBlockToken(ScriptWord.BLOCK_END),
			this.createLiteralToken("6", DataTypeName.DEFAULT_INT),
			this.createEndToken(),
			this.createBlockToken(ScriptWord.BLOCK_END),
			this.createLiteralToken("7", DataTypeName.DEFAULT_INT),
			this.createEndToken(),
			this.createBlockToken(ScriptWord.BLOCK_END),
		};

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);
		assertEquals(AstNode.Type.BLOCK, rootNode.getChildNodes()[0].getType());



		AstNode blockNode = rootNode.getChildNodes()[0];
		AstNode[] nodesInBlock = blockNode.getChildNodes();
		AstNode exprNode;

		exprNode = nodesInBlock[0];
		assertEquals(AstNode.Type.EXPRESSION, exprNode.getType());
		this.checkLiteralNode(exprNode.getChildNodes()[0], "1");

		exprNode = nodesInBlock[1];
		assertEquals(AstNode.Type.EXPRESSION, exprNode.getType());
		this.checkLiteralNode(exprNode.getChildNodes()[0], "2");

		exprNode = nodesInBlock[2];
		assertEquals(AstNode.Type.BLOCK, exprNode.getType());

		exprNode = nodesInBlock[3];
		assertEquals(AstNode.Type.EXPRESSION, exprNode.getType());
		this.checkLiteralNode(exprNode.getChildNodes()[0], "7");


		blockNode = nodesInBlock[2];
		nodesInBlock = blockNode.getChildNodes();

		exprNode = nodesInBlock[0];
		assertEquals(AstNode.Type.EXPRESSION, exprNode.getType());
		this.checkLiteralNode(exprNode.getChildNodes()[0], "3");

		exprNode = nodesInBlock[1];
		assertEquals(AstNode.Type.EXPRESSION, exprNode.getType());
		this.checkLiteralNode(exprNode.getChildNodes()[0], "4");

		exprNode = nodesInBlock[2];
		assertEquals(AstNode.Type.BLOCK, exprNode.getType());

		exprNode = nodesInBlock[3];
		assertEquals(AstNode.Type.EXPRESSION, exprNode.getType());
		this.checkLiteralNode(exprNode.getChildNodes()[0], "6");


		blockNode = nodesInBlock[2];
		nodesInBlock = blockNode.getChildNodes();

		exprNode = nodesInBlock[0];
		assertEquals(AstNode.Type.EXPRESSION, exprNode.getType());
		this.checkLiteralNode(exprNode.getChildNodes()[0], "5");
	}



	@Test
	public void testIfStatement() throws VnanoException {

		// " if (x == 2) { } "
		Token[] tokens = new Token[]{
			this.createControlToken(ScriptWord.IF),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_BEGIN),
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken(ScriptWord.EQUAL, OperatorPrecedence.EQUAL, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.COMPARISON),
			this.createLiteralToken("2", DataTypeName.DEFAULT_INT),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_END),
			this.createBlockToken(ScriptWord.BLOCK_BEGIN),
			this.createBlockToken(ScriptWord.BLOCK_END),
		};

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(2, rootNode.getChildNodes().length);

		AstNode ifNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.IF, ifNode.getType());
		assertEquals(1, ifNode.getChildNodes().length);

		AstNode conditionExprNode = ifNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, conditionExprNode.getType());
		assertEquals(1, conditionExprNode.getChildNodes().length);

		AstNode equalNode = conditionExprNode.getChildNodes()[0];
		this.checkOperatorNode(equalNode, ScriptWord.EQUAL, OperatorPrecedence.EQUAL, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.COMPARISON);
		assertEquals(2, equalNode.getChildNodes().length);
		this.checkVariableIdentifierNode(equalNode.getChildNodes()[0], "x");
		this.checkLiteralNode(equalNode.getChildNodes()[1], "2");

		AstNode blockNode = rootNode.getChildNodes()[1];
		assertEquals(AstNode.Type.BLOCK, blockNode.getType());
		assertEquals(0, blockNode.getChildNodes().length);
	}


	@Test
	public void testElseStatement() throws VnanoException {

		// " if (x == 2) { 1; } else { 2; }"
		Token[] tokens = new Token[]{
			this.createControlToken(ScriptWord.IF),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_BEGIN),
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken(ScriptWord.EQUAL, OperatorPrecedence.EQUAL, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.COMPARISON),
			this.createLiteralToken("2", DataTypeName.DEFAULT_INT),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_END),
			this.createBlockToken(ScriptWord.BLOCK_BEGIN),
			this.createLiteralToken("1", DataTypeName.DEFAULT_INT),
			this.createEndToken(),
			this.createBlockToken(ScriptWord.BLOCK_END),
			this.createControlToken(ScriptWord.ELSE),
			this.createBlockToken(ScriptWord.BLOCK_BEGIN),
			this.createLiteralToken("2", DataTypeName.DEFAULT_INT),
			this.createEndToken(),
			this.createBlockToken(ScriptWord.BLOCK_END),
		};

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(4, rootNode.getChildNodes().length);

		AstNode ifNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.IF, ifNode.getType());
		assertEquals(1, ifNode.getChildNodes().length);

		AstNode conditionExprNode = ifNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, conditionExprNode.getType());
		assertEquals(1, conditionExprNode.getChildNodes().length);

		AstNode equalNode = conditionExprNode.getChildNodes()[0];
		this.checkOperatorNode(equalNode, ScriptWord.EQUAL, OperatorPrecedence.EQUAL, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.COMPARISON);
		assertEquals(2, equalNode.getChildNodes().length);
		this.checkVariableIdentifierNode(equalNode.getChildNodes()[0], "x");
		this.checkLiteralNode(equalNode.getChildNodes()[1], "2");

		AstNode ifBlockNode = rootNode.getChildNodes()[1];
		assertEquals(AstNode.Type.BLOCK, ifBlockNode.getType());
		assertEquals(1, ifBlockNode.getChildNodes().length);
		assertEquals(AstNode.Type.EXPRESSION, ifBlockNode.getChildNodes()[0].getType());
		this.checkLiteralNode(ifBlockNode.getChildNodes()[0].getChildNodes()[0], "1");

		AstNode elseNode = rootNode.getChildNodes()[2];
		assertEquals(AstNode.Type.ELSE, elseNode.getType());
		assertEquals(0, elseNode.getChildNodes().length);

		AstNode elseBlockNode = rootNode.getChildNodes()[3];
		assertEquals(AstNode.Type.BLOCK, elseBlockNode.getType());
		assertEquals(1, elseBlockNode.getChildNodes().length);
		assertEquals(AstNode.Type.EXPRESSION, elseBlockNode.getChildNodes()[0].getType());
		this.checkLiteralNode(elseBlockNode.getChildNodes()[0].getChildNodes()[0], "2");
	}


	@Test
	public void testWhileStatement() throws VnanoException {

		// " while (x == 2) { } "
		Token[] tokens = new Token[]{
			this.createControlToken(ScriptWord.WHILE),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_BEGIN),
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken(ScriptWord.EQUAL, OperatorPrecedence.EQUAL, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.COMPARISON),
			this.createLiteralToken("2", DataTypeName.DEFAULT_INT),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_END),
			this.createBlockToken(ScriptWord.BLOCK_BEGIN),
			this.createBlockToken(ScriptWord.BLOCK_END),
		};

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(2, rootNode.getChildNodes().length);

		AstNode ifNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.WHILE, ifNode.getType());
		assertEquals(1, ifNode.getChildNodes().length);

		AstNode conditionExprNode = ifNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, conditionExprNode.getType());
		assertEquals(1, conditionExprNode.getChildNodes().length);

		AstNode equalNode = conditionExprNode.getChildNodes()[0];
		this.checkOperatorNode(equalNode, ScriptWord.EQUAL, OperatorPrecedence.EQUAL, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.COMPARISON);
		assertEquals(2, equalNode.getChildNodes().length);
		this.checkVariableIdentifierNode(equalNode.getChildNodes()[0], "x");
		this.checkLiteralNode(equalNode.getChildNodes()[1], "2");

		AstNode blockNode = rootNode.getChildNodes()[1];
		assertEquals(AstNode.Type.BLOCK, blockNode.getType());
		assertEquals(0, blockNode.getChildNodes().length);
	}


	@Test
	public void testForStatement() throws VnanoException {

		// " for (i=0; i<10; ++i) { } "
		Token[] tokens = new Token[]{
			this.createControlToken(ScriptWord.FOR),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_BEGIN),
			this.createVariableIdentifierToken("i"),
			this.createOperatorToken(ScriptWord.ASSIGNMENT, OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createLiteralToken("0", DataTypeName.DEFAULT_INT),
			this.createEndToken(),
			this.createVariableIdentifierToken("i"),
			this.createOperatorToken(ScriptWord.LESS_THAN, OperatorPrecedence.LESS_THAN, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.COMPARISON),
			this.createLiteralToken("10", DataTypeName.DEFAULT_INT),
			this.createEndToken(),
			this.createOperatorToken(ScriptWord.INCREMENT, OperatorPrecedence.PREFIX_INCREMENT, AttributeValue.LEFT, AttributeValue.PREFIX, AttributeValue.ARITHMETIC),
			this.createVariableIdentifierToken("i"),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_END),
			this.createBlockToken(ScriptWord.BLOCK_BEGIN),
			this.createBlockToken(ScriptWord.BLOCK_END),
		};

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(2, rootNode.getChildNodes().length);

		AstNode forNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.FOR, forNode.getType());
		assertEquals(3, forNode.getChildNodes().length);

		AstNode initExprNode = forNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, initExprNode.getType());
		assertEquals(1, initExprNode.getChildNodes().length);
		AstNode assignNode = initExprNode.getChildNodes()[0];
		this.checkOperatorNode(assignNode, ScriptWord.ASSIGNMENT, OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		assertEquals(2, assignNode.getChildNodes().length);
		this.checkVariableIdentifierNode(assignNode.getChildNodes()[0], "i");
		this.checkLiteralNode(assignNode.getChildNodes()[1], "0");

		AstNode conditionExprNode = forNode.getChildNodes()[1];
		assertEquals(AstNode.Type.EXPRESSION, conditionExprNode.getType());
		assertEquals(1, conditionExprNode.getChildNodes().length);
		AstNode ltNode = conditionExprNode.getChildNodes()[0];
		this.checkOperatorNode(ltNode, ScriptWord.LESS_THAN, OperatorPrecedence.LESS_THAN, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.COMPARISON);
		assertEquals(2, ltNode.getChildNodes().length);
		this.checkVariableIdentifierNode(ltNode.getChildNodes()[0], "i");
		this.checkLiteralNode(ltNode.getChildNodes()[1], "10");

		AstNode updateExprNode = forNode.getChildNodes()[2];
		assertEquals(AstNode.Type.EXPRESSION, updateExprNode.getType());
		assertEquals(1, updateExprNode.getChildNodes().length);
		assertEquals(1, updateExprNode.getChildNodes().length);
		AstNode incrementNode = updateExprNode.getChildNodes()[0];
		this.checkOperatorNode(incrementNode, ScriptWord.INCREMENT, OperatorPrecedence.PREFIX_INCREMENT, AttributeValue.LEFT, AttributeValue.PREFIX, AttributeValue.ARITHMETIC);
		this.checkVariableIdentifierNode(incrementNode.getChildNodes()[0], "i");

		AstNode blockNode = rootNode.getChildNodes()[1];
		assertEquals(AstNode.Type.BLOCK, blockNode.getType());
		assertEquals(0, blockNode.getChildNodes().length);
	}

	@Test
	public void testForStatementWithCounterVariableDeclaration() throws VnanoException {

		// " for (int i=0; i<10; ++i) { } "
		Token[] tokens = new Token[]{
			this.createControlToken(ScriptWord.FOR),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_BEGIN),
			this.createDataTypeToken(DataTypeName.DEFAULT_INT),
			this.createVariableIdentifierToken("i"),
			this.createOperatorToken(ScriptWord.ASSIGNMENT, OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createLiteralToken("0", DataTypeName.DEFAULT_INT),
			this.createEndToken(),
			this.createVariableIdentifierToken("i"),
			this.createOperatorToken(ScriptWord.LESS_THAN, OperatorPrecedence.LESS_THAN, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.COMPARISON),
			this.createLiteralToken("10", DataTypeName.DEFAULT_INT),
			this.createEndToken(),
			this.createOperatorToken(ScriptWord.INCREMENT, OperatorPrecedence.PREFIX_INCREMENT, AttributeValue.LEFT, AttributeValue.PREFIX, AttributeValue.ARITHMETIC),
			this.createVariableIdentifierToken("i"),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_END),
			this.createBlockToken(ScriptWord.BLOCK_BEGIN),
			this.createBlockToken(ScriptWord.BLOCK_END),
		};

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(2, rootNode.getChildNodes().length);

		AstNode forNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.FOR, forNode.getType());
		assertEquals(3, forNode.getChildNodes().length);

		AstNode varNode = forNode.getChildNodes()[0];
		assertEquals(AstNode.Type.VARIABLE, varNode.getType());
		assertEquals(DataTypeName.DEFAULT_INT, varNode.getAttribute(AttributeKey.DATA_TYPE));
		assertEquals("i", varNode.getAttribute(AttributeKey.IDENTIFIER_VALUE));
		assertEquals("0", varNode.getAttribute(AttributeKey.ARRAY_RANK));
		assertEquals(1, varNode.getChildNodes().length);

		AstNode initExprNode = varNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, initExprNode.getType());
		assertEquals(1, initExprNode.getChildNodes().length);
		AstNode assignNode = initExprNode.getChildNodes()[0];
		this.checkOperatorNode(assignNode, ScriptWord.ASSIGNMENT, OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		assertEquals(2, assignNode.getChildNodes().length);
		this.checkVariableIdentifierNode(assignNode.getChildNodes()[0], "i");
		this.checkLiteralNode(assignNode.getChildNodes()[1], "0");

		AstNode conditionExprNode = forNode.getChildNodes()[1];
		assertEquals(AstNode.Type.EXPRESSION, conditionExprNode.getType());
		assertEquals(1, conditionExprNode.getChildNodes().length);
		AstNode ltNode = conditionExprNode.getChildNodes()[0];
		this.checkOperatorNode(ltNode, ScriptWord.LESS_THAN, OperatorPrecedence.LESS_THAN, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.COMPARISON);
		assertEquals(2, ltNode.getChildNodes().length);
		this.checkVariableIdentifierNode(ltNode.getChildNodes()[0], "i");
		this.checkLiteralNode(ltNode.getChildNodes()[1], "10");

		AstNode updateExprNode = forNode.getChildNodes()[2];
		assertEquals(AstNode.Type.EXPRESSION, updateExprNode.getType());
		assertEquals(1, updateExprNode.getChildNodes().length);
		assertEquals(1, updateExprNode.getChildNodes().length);
		AstNode incrementNode = updateExprNode.getChildNodes()[0];
		this.checkOperatorNode(incrementNode, ScriptWord.INCREMENT, OperatorPrecedence.PREFIX_INCREMENT, AttributeValue.LEFT, AttributeValue.PREFIX, AttributeValue.ARITHMETIC);
		this.checkVariableIdentifierNode(incrementNode.getChildNodes()[0], "i");

		AstNode blockNode = rootNode.getChildNodes()[1];
		assertEquals(AstNode.Type.BLOCK, blockNode.getType());
		assertEquals(0, blockNode.getChildNodes().length);
	}


	@Test
	public void testContinueStatement() throws VnanoException {

		// "while (true) { continue; }"
		Token[] tokens = new Token[]{
			this.createControlToken(ScriptWord.WHILE),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_BEGIN),
			this.createLiteralToken("true", DataTypeName.BOOL),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_END),
			this.createBlockToken(ScriptWord.BLOCK_BEGIN),
			this.createControlToken(ScriptWord.CONTINUE),
			this.createEndToken(),
			this.createBlockToken(ScriptWord.BLOCK_END),
		};

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(2, rootNode.getChildNodes().length);

		AstNode ifNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.WHILE, ifNode.getType());
		assertEquals(1, ifNode.getChildNodes().length);

		AstNode conditionExprNode = ifNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, conditionExprNode.getType());
		assertEquals(1, conditionExprNode.getChildNodes().length);
		this.checkLiteralNode(conditionExprNode.getChildNodes()[0], "true");

		AstNode blockNode = rootNode.getChildNodes()[1];
		assertEquals(AstNode.Type.BLOCK, blockNode.getType());
		assertEquals(1, blockNode.getChildNodes().length);

		AstNode continueNode = blockNode.getChildNodes()[0];
		assertEquals(AstNode.Type.CONTINUE, continueNode.getType());
		assertEquals(0, continueNode.getChildNodes().length);
	}


	@Test
	public void testBreakStatement() throws VnanoException {

		// "while (true) { break; }"
		Token[] tokens = new Token[]{
			this.createControlToken(ScriptWord.WHILE),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_BEGIN),
			this.createLiteralToken("true", DataTypeName.BOOL),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_END),
			this.createBlockToken(ScriptWord.BLOCK_BEGIN),
			this.createControlToken(ScriptWord.BREAK),
			this.createEndToken(),
			this.createBlockToken(ScriptWord.BLOCK_END),
		};

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(2, rootNode.getChildNodes().length);

		AstNode ifNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.WHILE, ifNode.getType());
		assertEquals(1, ifNode.getChildNodes().length);

		AstNode conditionExprNode = ifNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, conditionExprNode.getType());
		assertEquals(1, conditionExprNode.getChildNodes().length);
		this.checkLiteralNode(conditionExprNode.getChildNodes()[0], "true");

		AstNode blockNode = rootNode.getChildNodes()[1];
		assertEquals(AstNode.Type.BLOCK, blockNode.getType());
		assertEquals(1, blockNode.getChildNodes().length);

		AstNode continueNode = blockNode.getChildNodes()[0];
		assertEquals(AstNode.Type.BREAK, continueNode.getType());
		assertEquals(0, continueNode.getChildNodes().length);
	}




	@Test
	public void testParseExpressionPrefixOperator() throws VnanoException {

		// "++x"
		Token rightToken = this.createVariableIdentifierToken("x");
		Token operatorToken = createOperatorToken(
			ScriptWord.INCREMENT, OperatorPrecedence.PREFIX_INCREMENT, AttributeValue.LEFT, AttributeValue.PREFIX, AttributeValue.ARITHMETIC
		);
		Token[] tokens = new Token[]{
			operatorToken,
			rightToken,
			this.createEndToken()
		};

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		AstNode operatorNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(
				operatorNode, ScriptWord.INCREMENT, OperatorPrecedence.PREFIX_DECREMENT, AttributeValue.LEFT,
				AttributeValue.PREFIX, AttributeValue.ARITHMETIC
		);

		assertEquals(1, operatorNode.getChildNodes().length);
		AstNode[] operandNodes = operatorNode.getChildNodes();
		this.checkVariableIdentifierNode(operandNodes[0], "x");
	}

	@Test
	public void testParseExpressionPostfixOperator() throws VnanoException {

		// "x++"
		Token leftToken = this.createVariableIdentifierToken("x");
		Token operatorToken = createOperatorToken(
			ScriptWord.INCREMENT, OperatorPrecedence.POSTFIX_INCREMENT, AttributeValue.LEFT, AttributeValue.POSTFIX, AttributeValue.ARITHMETIC
		);
		Token[] tokens = new Token[]{
			leftToken,
			operatorToken,
			this.createEndToken()
		};

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		AstNode operatorNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(
			operatorNode, ScriptWord.INCREMENT, OperatorPrecedence.POSTFIX_DECREMENT, AttributeValue.LEFT,
			AttributeValue.POSTFIX, AttributeValue.ARITHMETIC
		);

		assertEquals(1, operatorNode.getChildNodes().length);
		AstNode[] operandNodes = operatorNode.getChildNodes();
		this.checkVariableIdentifierNode(operandNodes[0], "x");
	}

	@Test
	public void testParseExpressionBinaryOperator() throws VnanoException {

		// "1 + x"
		Token leftToken = this.createLiteralToken("1", DataTypeName.DEFAULT_INT);
		Token rightToken = this.createVariableIdentifierToken("x");
		Token operatorToken = createOperatorToken(
			ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token[] tokens = new Token[]{
			leftToken,
			operatorToken,
			rightToken,
			this.createEndToken()
		};

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		AstNode operatorNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(
			operatorNode, ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT,
			AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);

		assertEquals(2, operatorNode.getChildNodes().length);
		AstNode[] operandNodes = operatorNode.getChildNodes();
		this.checkLiteralNode(operandNodes[0], "1");
		this.checkVariableIdentifierNode(operandNodes[1], "x");
	}

	@Test
	public void testParseExpressionMultiaryOperator() throws VnanoException {

		// "func(1,x)"
		Token identifierToken = this.createVariableIdentifierToken("func");
		Token operatorBeginToken = createOperatorToken( // "("
			ScriptWord.PARENTHESIS_BEGIN, OperatorPrecedence.CALL_BEGIN, AttributeValue.LEFT, AttributeValue.MULTIARY, AttributeValue.CALL
		);
		Token leftOperandToken = this.createLiteralToken("1", DataTypeName.DEFAULT_INT);
		Token operatorSeparatorToken = createOperatorToken( // ","
			ScriptWord.ARGUMENT_SEPARATOR, OperatorPrecedence.CALL_BEGIN, AttributeValue.LEFT, AttributeValue.MULTIARY_SEPARATOR, AttributeValue.CALL
		);
		Token rightOperandToken = this.createVariableIdentifierToken("x");
		Token operatorEndToken = createOperatorToken( // ")"
			ScriptWord.PARENTHESIS_END, OperatorPrecedence.CALL_BEGIN, AttributeValue.LEFT, AttributeValue.MULTIARY_END, AttributeValue.CALL
		);
		Token[] tokens = new Token[]{
			identifierToken,
			operatorBeginToken,
			leftOperandToken,
			operatorSeparatorToken,
			rightOperandToken,
			operatorEndToken,
			this.createEndToken()
		};

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		AstNode operatorNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(
			operatorNode, ScriptWord.PARENTHESIS_BEGIN, OperatorPrecedence.CALL_BEGIN, AttributeValue.LEFT,
			AttributeValue.MULTIARY, AttributeValue.CALL
		);

		assertEquals(3, operatorNode.getChildNodes().length);
		AstNode[] operandNodes = operatorNode.getChildNodes();
		this.checkVariableIdentifierNode(operandNodes[0], "func");
		this.checkLiteralNode(operandNodes[1], "1");
		this.checkVariableIdentifierNode(operandNodes[2], "x");
	}


	@Test
	public void testParseExpressionParenthesis1() throws VnanoException {

		// "1 * (2 + 3)"
		Token one = this.createLiteralToken("1", DataTypeName.DEFAULT_INT);
		Token two = this.createLiteralToken("2", DataTypeName.DEFAULT_INT);
		Token three = this.createLiteralToken("3", DataTypeName.DEFAULT_INT);
		Token add = createOperatorToken(
			ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token mul = createOperatorToken(
			ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token open = createParenthesisToken(ScriptWord.PARENTHESIS_BEGIN);
		Token close = createParenthesisToken(ScriptWord.PARENTHESIS_END);

		Token[] tokens = new Token[]{ one, mul, open, two, add, three, close, this.createEndToken() };

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		AstNode mulNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(
			mulNode, ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT,
			AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);

		assertEquals(2, mulNode.getChildNodes().length);
		this.checkLiteralNode(mulNode.getChildNodes()[0], "1");

		AstNode addNode = mulNode.getChildNodes()[1];
		this.checkOperatorNode(
			addNode, ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT,
			AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);

		assertEquals(2, addNode.getChildNodes().length);
		this.checkLiteralNode(addNode.getChildNodes()[0], "2");
		this.checkLiteralNode(addNode.getChildNodes()[1], "3");
	}

	@Test
	public void testParseExpressionParenthesis2() throws VnanoException {

		// "1 + (2 * 3)"
		Token one = this.createLiteralToken("1", DataTypeName.DEFAULT_INT);
		Token two = this.createLiteralToken("2", DataTypeName.DEFAULT_INT);
		Token three = this.createLiteralToken("3", DataTypeName.DEFAULT_INT);
		Token add = createOperatorToken(
			ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token mul = createOperatorToken(
			ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token open = createParenthesisToken(ScriptWord.PARENTHESIS_BEGIN);
		Token close = createParenthesisToken(ScriptWord.PARENTHESIS_END);

		Token[] tokens = new Token[]{ one, add, open, two, mul, three, close, this.createEndToken() };

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		AstNode addNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(
			addNode, ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT,
			AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);

		assertEquals(2, addNode.getChildNodes().length);
		this.checkLiteralNode(addNode.getChildNodes()[0], "1");

		AstNode mulNode = addNode.getChildNodes()[1];
		this.checkOperatorNode(
			mulNode, ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT,
			AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);

		assertEquals(2, mulNode.getChildNodes().length);
		this.checkLiteralNode(mulNode.getChildNodes()[0], "2");
		this.checkLiteralNode(mulNode.getChildNodes()[1], "3");
	}


	@Test
	public void testParseExpressionParenthesis3() throws VnanoException {

		// "(1 + 2) * 3"
		Token one = this.createLiteralToken("1", DataTypeName.DEFAULT_INT);
		Token two = this.createLiteralToken("2", DataTypeName.DEFAULT_INT);
		Token three = this.createLiteralToken("3", DataTypeName.DEFAULT_INT);
		Token add = createOperatorToken(
			ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token mul = createOperatorToken(
			ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token open = createParenthesisToken(ScriptWord.PARENTHESIS_BEGIN);
		Token close = createParenthesisToken(ScriptWord.PARENTHESIS_END);

		Token[] tokens = new Token[]{ open, one, add, two, close, mul, three, this.createEndToken() };

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		AstNode mulNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(
				mulNode, ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT,
			AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);

		assertEquals(2, mulNode.getChildNodes().length);
		this.checkLiteralNode(mulNode.getChildNodes()[1], "3");

		AstNode addNode = mulNode.getChildNodes()[0];
		this.checkOperatorNode(
			addNode, ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT,
			AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);

		assertEquals(2, addNode.getChildNodes().length);
		this.checkLiteralNode(addNode.getChildNodes()[0], "1");
		this.checkLiteralNode(addNode.getChildNodes()[1], "2");
	}

	@Test
	public void testParseExpressionParenthesis4() throws VnanoException {

		// "(1 + 2) * 3"
		Token one = this.createLiteralToken("1", DataTypeName.DEFAULT_INT);
		Token two = this.createLiteralToken("2", DataTypeName.DEFAULT_INT);
		Token three = this.createLiteralToken("3", DataTypeName.DEFAULT_INT);
		Token add = createOperatorToken(
			ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token mul = createOperatorToken(
			ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token open = createParenthesisToken(ScriptWord.PARENTHESIS_BEGIN);
		Token close = createParenthesisToken(ScriptWord.PARENTHESIS_END);

		Token[] tokens = new Token[]{ open, one, mul, two, close, add, three, this.createEndToken() };

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		AstNode addNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(
			addNode, ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT,
			AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);

		assertEquals(2, addNode.getChildNodes().length);
		this.checkLiteralNode(addNode.getChildNodes()[1], "3");

		AstNode mulNode = addNode.getChildNodes()[0];
		this.checkOperatorNode(
			mulNode, ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT,
			AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);

		assertEquals(2, mulNode.getChildNodes().length);
		this.checkLiteralNode(mulNode.getChildNodes()[0], "1");
		this.checkLiteralNode(mulNode.getChildNodes()[1], "2");
	}


	@Test
	public void testParseExpressionAddAddAddAdd() throws VnanoException {

		// "1 + 2 + 3 + 4 + 5 ;"
		Token[] tokens = new Token[]{
			this.createLiteralToken("1", DataTypeName.DEFAULT_INT),
			this.createOperatorToken(ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("2", DataTypeName.DEFAULT_INT),
			this.createOperatorToken(ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("3", DataTypeName.DEFAULT_INT),
			this.createOperatorToken(ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("4", DataTypeName.DEFAULT_INT),
			this.createOperatorToken(ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("5", DataTypeName.DEFAULT_INT),
			this.createEndToken()
		};

		/*
		   // Expected AST

	          ROOT
		           |
		          EXPR
		           |
		          _+_  < 1-st hierarchy
		        _+_  | < 2-nd hierarchy
		      _+_  | | < 3-rd hierarchy
		    _+_  | | | < 4-th hierarchy
		   |   | | | |
		   1   2 3 4 5
		*/

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		// 1-st hierarchy
		AstNode operatorNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "5");

		// 2-nd hierarchy
		operatorNode = operatorNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "4");

		// 3-rd hierarchy
		operatorNode = operatorNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "3");

		// 4-th hierarchy
		operatorNode = operatorNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[0], "1");
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "2");
	}


	@Test
	public void testParseExpressionAddMulMulAdd() throws VnanoException {

		// "1 + 2 * 3 * 4 + 5 ;"
		Token[] tokens = new Token[]{
			this.createLiteralToken("1", DataTypeName.DEFAULT_INT),
			this.createOperatorToken(ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("2", DataTypeName.DEFAULT_INT),
			this.createOperatorToken(ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("3", DataTypeName.DEFAULT_INT),
			this.createOperatorToken(ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("4", DataTypeName.DEFAULT_INT),
			this.createOperatorToken(ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("5", DataTypeName.DEFAULT_INT),
			this.createEndToken()
		};

		/*
		   // Expected AST

		          ROOT
		           |
		          EXPR
		           |
		        ___+__  < 1-st hierarchy
		     __+__    | < 2-nd hierarchy
		    |    _*_  | < 3-rd hierarchy
		    |  _*_  | | < 4-th hierarchy
		    | |   | | |
		    1 2   3 4 5
		*/

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		// 1-st hierarchy
		AstNode operatorNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "5");

		// 2-nd hierarchy
		operatorNode = operatorNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[0], "1");

		// 3-rd hierarchy
		operatorNode = operatorNode.getChildNodes()[1];
		this.checkOperatorNode(operatorNode, ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "4");

		// 4-th hierarchy
		operatorNode = operatorNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[0], "2");
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "3");
	}


	@Test
	public void testParseExpressionMulAddAddMul() throws VnanoException {

		// "1 * 2 + 3 + 4 * 5 ;"
		Token[] tokens = new Token[]{
			this.createLiteralToken("1", DataTypeName.DEFAULT_INT),
			this.createOperatorToken(ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("2", DataTypeName.DEFAULT_INT),
			this.createOperatorToken(ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("3", DataTypeName.DEFAULT_INT),
			this.createOperatorToken(ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("4", DataTypeName.DEFAULT_INT),
			this.createOperatorToken(ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("5", DataTypeName.DEFAULT_INT),
			this.createEndToken()
		};

		/*
		   // Expected AST

		          ROOT
		           |
		          EXPR
		           |
		         __+__     < 1-st hierarchy
		       _+_   _*_   < 2-nd hierarchy
		     _*_  | |   |  < 3-rd hierarchy
		    |   | | |   |
		    1   2 3 4   5
		*/

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		// 1-st hierarchy
		AstNode operatorNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);

		// 2-nd hierarchy (left)
		AstNode leftOperatorNode = operatorNode.getChildNodes()[0];
		this.checkOperatorNode(leftOperatorNode, ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(leftOperatorNode.getChildNodes()[1], "3");

		// 2-nd hierarchy (right)
		AstNode rightOperatorNode = operatorNode.getChildNodes()[1];
		this.checkOperatorNode(rightOperatorNode, ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(rightOperatorNode.getChildNodes()[0], "4");
		this.checkLiteralNode(rightOperatorNode.getChildNodes()[1], "5");

		// 3-rd hierarchy
		operatorNode = leftOperatorNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[0], "1");
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "2");
	}

	@Test
	public void testParseExpressionAddMulAddMul() throws VnanoException {

		// "1 + 2 * 3 + 4 * 5 ;"
		Token[] tokens = new Token[]{
			this.createLiteralToken("1", DataTypeName.DEFAULT_INT),
			this.createOperatorToken(ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("2", DataTypeName.DEFAULT_INT),
			this.createOperatorToken(ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("3", DataTypeName.DEFAULT_INT),
			this.createOperatorToken(ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("4", DataTypeName.DEFAULT_INT),
			this.createOperatorToken(ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("5", DataTypeName.DEFAULT_INT),
			this.createEndToken()
		};

		/*
		   // Expected AST

		          ROOT
		           |
		          EXPR
		           |
		       ____+__    < 1-st hierarchy
		     _+_     _*_  < 2-nd hierarchy
		    |  _*_  |   | < 3-rd hierarchy
		    | |   | |   |
		    1 2   3 4   5
		*/

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		// 1-st hierarchy
		AstNode operatorNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);

		// 2-nd hierarchy (left)
		AstNode leftOperatorNode = operatorNode.getChildNodes()[0];
		this.checkOperatorNode(leftOperatorNode, ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(leftOperatorNode.getChildNodes()[0], "1");

		// 2-nd hierarchy (right)
		AstNode rightOperatorNode = operatorNode.getChildNodes()[1];
		this.checkOperatorNode(rightOperatorNode, ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(rightOperatorNode.getChildNodes()[0], "4");
		this.checkLiteralNode(rightOperatorNode.getChildNodes()[1], "5");

		// 3-rd hierarchy
		operatorNode = leftOperatorNode.getChildNodes()[1];
		this.checkOperatorNode(operatorNode, ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[0], "2");
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "3");
	}


	@Test
	public void testParseExpressionDualAssignment() throws VnanoException {

		// "x = y = 1 ;"
		Token[] tokens = new Token[]{
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken(ScriptWord.ASSIGNMENT, OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createVariableIdentifierToken("y"),
			this.createOperatorToken(ScriptWord.ASSIGNMENT, OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createLiteralToken("1", DataTypeName.DEFAULT_INT),
			this.createEndToken()
		};

		/*
		   // Expected AST

		       ROOT
		        |
		       EXPR
		        |
		      __=__    < 1-st hierarchy
		     |    _=_  < 2-nd hierarchy
		     |   |   |
		     x   y   1
		*/

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		// 1-st hierarchy
		AstNode operatorNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.ASSIGNMENT, OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		this.checkVariableIdentifierNode(operatorNode.getChildNodes()[0], "x");

		// 2-nd hierarchy
		operatorNode = operatorNode.getChildNodes()[1];
		this.checkOperatorNode(operatorNode, ScriptWord.ASSIGNMENT, OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		this.checkVariableIdentifierNode(operatorNode.getChildNodes()[0], "y");
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "1");
	}


	@Test
	public void testParseExpressionTrippleAssignment() throws VnanoException {

		// "x = y = z = 1 ;"
		Token[] tokens = new Token[]{
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken(ScriptWord.ASSIGNMENT, OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createVariableIdentifierToken("y"),
			this.createOperatorToken(ScriptWord.ASSIGNMENT, OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createVariableIdentifierToken("z"),
			this.createOperatorToken(ScriptWord.ASSIGNMENT, OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createLiteralToken("1", DataTypeName.DEFAULT_INT),
			this.createEndToken()
		};

		/*
		   // Expected AST

		       ROOT
		        |
		       EXPR
		        |
		      __=__         < 1-st hierarchy
		     |    _=___     < 2-nd hierarchy
		     |   |    _=_   < 3-rd hierarchy
		     |   |   |   |
		     x   y   z   1
		*/

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		// 1-st hierarchy
		AstNode operatorNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.ASSIGNMENT, OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		this.checkVariableIdentifierNode(operatorNode.getChildNodes()[0], "x");

		// 2-nd hierarchy
		operatorNode = operatorNode.getChildNodes()[1];
		this.checkOperatorNode(operatorNode, ScriptWord.ASSIGNMENT, OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		this.checkVariableIdentifierNode(operatorNode.getChildNodes()[0], "y");

		// 3-rd hierarchy
		operatorNode = operatorNode.getChildNodes()[1];
		this.checkOperatorNode(operatorNode, ScriptWord.ASSIGNMENT, OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		this.checkVariableIdentifierNode(operatorNode.getChildNodes()[0], "z");
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "1");
	}


	@Test
	public void testParseExpressionQuadrupleAssignment() throws VnanoException {

		// "x = y = z = w = 1 ;"
		Token[] tokens = new Token[]{
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken(ScriptWord.ASSIGNMENT, OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createVariableIdentifierToken("y"),
			this.createOperatorToken(ScriptWord.ASSIGNMENT, OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createVariableIdentifierToken("z"),
			this.createOperatorToken(ScriptWord.ASSIGNMENT, OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createVariableIdentifierToken("w"),
			this.createOperatorToken(ScriptWord.ASSIGNMENT, OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createLiteralToken("1", DataTypeName.DEFAULT_INT),
			this.createEndToken()
		};

		/*
		   // Expected AST

		       ROOT
		        |
		       EXPR
		        |
		      __=__             < 1-st hierarchy
		     |    _=___         < 2-nd hierarchy
		     |   |    _=___     < 3-rd hierarchy
		     |   |   |    _=_   < 4-th hierarchy
		     |   |   |   |   |
		     x   y   z   w   1
		*/

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		// 1-st hierarchy
		AstNode operatorNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.ASSIGNMENT, OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		this.checkVariableIdentifierNode(operatorNode.getChildNodes()[0], "x");

		// 2-nd hierarchy
		operatorNode = operatorNode.getChildNodes()[1];
		this.checkOperatorNode(operatorNode, ScriptWord.ASSIGNMENT, OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		this.checkVariableIdentifierNode(operatorNode.getChildNodes()[0], "y");

		// 3-rd hierarchy
		operatorNode = operatorNode.getChildNodes()[1];
		this.checkOperatorNode(operatorNode, ScriptWord.ASSIGNMENT, OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		this.checkVariableIdentifierNode(operatorNode.getChildNodes()[0], "z");

		// 4-th hierarchy
		operatorNode = operatorNode.getChildNodes()[1];
		this.checkOperatorNode(operatorNode, ScriptWord.ASSIGNMENT, OperatorPrecedence.ASSIGNMENT, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		this.checkVariableIdentifierNode(operatorNode.getChildNodes()[0], "w");
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "1");
	}


	@Test
	public void testParseExpressionPrefixIncrementDecrement() throws VnanoException {

		// "++ -- x ;"
		Token[] tokens = new Token[]{
			this.createOperatorToken(ScriptWord.INCREMENT, OperatorPrecedence.PREFIX_INCREMENT, AttributeValue.RIGHT, AttributeValue.PREFIX, AttributeValue.ARITHMETIC),
			this.createOperatorToken(ScriptWord.DECREMENT, OperatorPrecedence.PREFIX_DECREMENT, AttributeValue.RIGHT, AttributeValue.PREFIX, AttributeValue.ARITHMETIC),
			this.createVariableIdentifierToken("x"),
			this.createEndToken()
		};

		/*
		   // Expected AST

		       ROOT
		        |
		       EXPR
		        |
		       ++      < 1-st hierarchy
		        |
		       --      < 2-nd hierarchy
		        |
		        x
		*/

		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		//System.out.println(rootNode);

		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		// 1-st hierarchy
		AstNode operatorNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.INCREMENT, OperatorPrecedence.PREFIX_INCREMENT, AttributeValue.RIGHT, AttributeValue.PREFIX, AttributeValue.ARITHMETIC);

		// 2-nd hierarchy
		operatorNode = operatorNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.DECREMENT, OperatorPrecedence.PREFIX_INCREMENT, AttributeValue.RIGHT, AttributeValue.PREFIX, AttributeValue.ARITHMETIC);
		this.checkVariableIdentifierNode(operatorNode.getChildNodes()[0], "x");
	}

}

