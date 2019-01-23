/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.spec.PriorityTable;
import org.vcssl.nano.spec.ScriptWord;

public class ParserTest {



	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	private Token createLiteralToken(String word) {
		Token token = new Token(word, 123, "Test.vnano");
		token.setType(Token.Type.LEAF);
		token.setAttribute(AttributeKey.LEAF_TYPE, AttributeValue.LITERAL);
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

	private Token createOperatorToken(String word, int priority, String syntax, String executor) {

		Token token = new Token(word, 124, "Test.vnano");
		token.setType(Token.Type.OPERATOR);
		token.setPriority(priority);
		token.setAttribute(AttributeKey.OPERATOR_SYNTAX, syntax);
		token.setAttribute(AttributeKey.OPERATOR_EXECUTOR, executor);
		return token;
	}

	private Token createParenthesisToken(String word) {
		Token token = new Token(word, 124, "Test.vnano");
		token.setType(Token.Type.PARENTHESIS);
		if (word.equals(ScriptWord.PARENTHESIS_BEGIN)) {
			token.setPriority(PriorityTable.PARENTHESIS_BEGIN);
		} else {
			token.setPriority(PriorityTable.LEAST_PRIOR);
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

	/*
	private void dumpTokens(Token[] tokens) {
		for (Token token: tokens) {
			System.out.println(token);
		}
	}
	*/

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

	private void checkOperatorNode(AstNode node, String symbol, int priority, String syntax, String executor) {
		assertEquals(AstNode.Type.OPERATOR, node.getType());
		assertEquals(symbol, node.getAttribute(AttributeKey.OPERATOR_SYMBOL));
		assertEquals(syntax, node.getAttribute(AttributeKey.OPERATOR_SYNTAX));
		assertEquals(executor, node.getAttribute(AttributeKey.OPERATOR_EXECUTOR));
		assertEquals(Integer.toString(priority), node.getAttribute(AttributeKey.OPERATOR_PRIORITY));
	}





	@Test
	public void testParseVariableDeclarationStatementScalar() throws VnanoException {

		// "int x;" のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createDataTypeToken("int"),
			this.createVariableIdentifierToken("x"),
			this.createEndToken()
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		// 変数ノードの検査
		AstNode varNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.VARIABLE, varNode.getType());
		assertEquals("int", varNode.getAttribute(AttributeKey.DATA_TYPE));
		assertEquals("x", varNode.getAttribute(AttributeKey.IDENTIFIER_VALUE));
		assertEquals("0", varNode.getAttribute(AttributeKey.RANK));
	}

	//@Test
	public void testParseVariableDeclarationStatementArray1D() throws VnanoException {

		// "int x [ 2 ];" のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createDataTypeToken("int"),
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken("[", PriorityTable.INDEX_BEGIN, AttributeValue.MULTIARY, AttributeValue.INDEX),
			this.createLiteralToken("2"),
			this.createOperatorToken("]", PriorityTable.INDEX_END, AttributeValue.MULTIARY, AttributeValue.INDEX),
			this.createEndToken()
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		// 変数ノードの検査
		AstNode varNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.VARIABLE, varNode.getType());
		assertEquals("int", varNode.getAttribute(AttributeKey.DATA_TYPE));
		assertEquals("x", varNode.getAttribute(AttributeKey.IDENTIFIER_VALUE));
		assertEquals("1", varNode.getAttribute(AttributeKey.RANK));

		// 要素数ノードの検査
		AstNode lengthNode = varNode.getChildNodes()[0];
		assertEquals(AstNode.Type.LENGTHS, lengthNode.getType());
		assertEquals(1, lengthNode.getChildNodes().length);

		// 要素数の式ノード、およびその直下のリテラルノードの検査
		AstNode lengthExprNode = lengthNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, lengthExprNode.getType());
		assertEquals(1, lengthExprNode.getChildNodes().length);
		this.checkLiteralNode(lengthExprNode.getChildNodes()[0], "2");
	}

	@Test
	public void testParseVariableDeclarationStatementArray1DWithLengthExpr() throws VnanoException {

		// "int x [ 1 + 2 * 3 ];" のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createDataTypeToken("int"),
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken("[", PriorityTable.INDEX_BEGIN, AttributeValue.MULTIARY, AttributeValue.INDEX),
			this.createLiteralToken("1"),
			this.createOperatorToken("+", PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("2"),
			this.createOperatorToken("*", PriorityTable.MULTIPLICATION, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("3"),
			this.createOperatorToken("]", PriorityTable.INDEX_END, AttributeValue.MULTIARY, AttributeValue.INDEX),
			this.createEndToken()
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		// 変数ノードの検査
		AstNode varNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.VARIABLE, varNode.getType());
		assertEquals("int", varNode.getAttribute(AttributeKey.DATA_TYPE));
		assertEquals("x", varNode.getAttribute(AttributeKey.IDENTIFIER_VALUE));
		assertEquals("1", varNode.getAttribute(AttributeKey.RANK));

		// 要素数ノードの検査
		AstNode lengthNode = varNode.getChildNodes()[0];
		assertEquals(AstNode.Type.LENGTHS, lengthNode.getType());
		assertEquals(1, lengthNode.getChildNodes().length);

		// 要素数の式ノード、およびその下のASTの検査
		AstNode lengthExprNode = lengthNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, lengthExprNode.getType());
		assertEquals(1, lengthExprNode.getChildNodes().length);

		AstNode addNode = lengthExprNode.getChildNodes()[0];
		this.checkOperatorNode(addNode, "+", PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(addNode.getChildNodes()[0], "1");

		AstNode mulNode = addNode.getChildNodes()[1];
		this.checkOperatorNode(mulNode, "*", PriorityTable.MULTIPLICATION, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(mulNode.getChildNodes()[0], "2");
		this.checkLiteralNode(mulNode.getChildNodes()[1], "3");
	}

	@Test
	public void testParseVariableDeclarationStatementArray3D() throws VnanoException {

		// "int x [ 2 ][ 3 ][ 4 ];" のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createDataTypeToken("int"),
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken("[", PriorityTable.INDEX_BEGIN, AttributeValue.MULTIARY, AttributeValue.INDEX),
			this.createLiteralToken("2"),
			this.createOperatorToken("][", PriorityTable.INDEX_SEPARATOR, AttributeValue.MULTIARY, AttributeValue.INDEX),
			this.createLiteralToken("3"),
			this.createOperatorToken("][", PriorityTable.INDEX_SEPARATOR, AttributeValue.MULTIARY, AttributeValue.INDEX),
			this.createLiteralToken("4"),
			this.createOperatorToken("]", PriorityTable.INDEX_END, AttributeValue.MULTIARY, AttributeValue.INDEX),
			this.createEndToken()
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		// 変数ノードの検査
		AstNode varNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.VARIABLE, varNode.getType());
		assertEquals("int", varNode.getAttribute(AttributeKey.DATA_TYPE));
		assertEquals("x", varNode.getAttribute(AttributeKey.IDENTIFIER_VALUE));
		assertEquals("3", varNode.getAttribute(AttributeKey.RANK));

		// 要素数ノードの検査
		AstNode lengthNode = varNode.getChildNodes()[0];
		assertEquals(AstNode.Type.LENGTHS, lengthNode.getType());
		assertEquals(3, lengthNode.getChildNodes().length);

		// 要素数(左側次元)の式ノード、およびその直下のリテラルノードの検査
		AstNode lengthExprNode0 = lengthNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, lengthExprNode0.getType());
		assertEquals(1, lengthExprNode0.getChildNodes().length);
		this.checkLiteralNode(lengthExprNode0.getChildNodes()[0], "2");

		// 要素数(中央次元)の式ノード、およびその直下のリテラルノードの検査
		AstNode lengthExprNode1 = lengthNode.getChildNodes()[1];
		assertEquals(AstNode.Type.EXPRESSION, lengthExprNode1.getType());
		assertEquals(1, lengthExprNode1.getChildNodes().length);
		this.checkLiteralNode(lengthExprNode1.getChildNodes()[0], "3");

		// 要素数(右次元)の式ノード、およびその直下のリテラルノードの検査
		AstNode lengthExprNode2 = lengthNode.getChildNodes()[2];
		assertEquals(AstNode.Type.EXPRESSION, lengthExprNode2.getType());
		assertEquals(1, lengthExprNode2.getChildNodes().length);
		this.checkLiteralNode(lengthExprNode2.getChildNodes()[0], "4");
	}

	@Test
	public void testParseVariableDeclarationStatementWithInitExpr() throws VnanoException {

		// "int x = 1 + 2 * 3;" のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createDataTypeToken("int"),
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken("=", PriorityTable.ASSIGNMENT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createLiteralToken("1"),
			this.createOperatorToken("+", PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("2"),
			this.createOperatorToken("*", PriorityTable.MULTIPLICATION, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("3"),
			this.createEndToken()
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		// 変数ノードの検査
		AstNode varNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.VARIABLE, varNode.getType());
		assertEquals("int", varNode.getAttribute(AttributeKey.DATA_TYPE));
		assertEquals("x", varNode.getAttribute(AttributeKey.IDENTIFIER_VALUE));
		assertEquals("0", varNode.getAttribute(AttributeKey.RANK));

		// 初期化式ノード、およびその下のASTの検査
		AstNode initExprNode = varNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, initExprNode.getType());
		assertEquals(1, initExprNode.getChildNodes().length);

		AstNode assignNode = initExprNode.getChildNodes()[0];
		this.checkOperatorNode(assignNode, "=", PriorityTable.ASSIGNMENT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		this.checkVariableIdentifierNode(assignNode.getChildNodes()[0], "x");

		AstNode addNode = assignNode.getChildNodes()[1];
		this.checkOperatorNode(addNode, "+", PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(addNode.getChildNodes()[0], "1");

		AstNode mulNode = addNode.getChildNodes()[1];
		this.checkOperatorNode(mulNode, "*", PriorityTable.MULTIPLICATION, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(mulNode.getChildNodes()[0], "2");
		this.checkLiteralNode(mulNode.getChildNodes()[1], "3");
	}








	@Test
	public void testBlockStatement() throws VnanoException {

		// "{ 1; 2; { 3; 4; { 5; } 6; } 7; }" のトークン配列を用意
		Token[] tokens = new Token[]{
				this.createBlockToken(ScriptWord.BLOCK_BEGIN),
			this.createLiteralToken("1"),
			this.createEndToken(),
			this.createLiteralToken("2"),
			this.createEndToken(),
			this.createBlockToken(ScriptWord.BLOCK_BEGIN),
			this.createLiteralToken("3"),
			this.createEndToken(),
			this.createLiteralToken("4"),
			this.createEndToken(),
			this.createBlockToken(ScriptWord.BLOCK_BEGIN),
			this.createLiteralToken("5"),
			this.createEndToken(),
			this.createBlockToken(ScriptWord.BLOCK_END),
			this.createLiteralToken("6"),
			this.createEndToken(),
			this.createBlockToken(ScriptWord.BLOCK_END),
			this.createLiteralToken("7"),
			this.createEndToken(),
			this.createBlockToken(ScriptWord.BLOCK_END),
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);
		assertEquals(AstNode.Type.BLOCK, rootNode.getChildNodes()[0].getType());



		// 第1階層ブロック内のノードを検査
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


		// 第2階層ブロック内のノードを検査
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


		//  第3階層ブロック内のブロック内の階層のノードを検査
		blockNode = nodesInBlock[2];
		nodesInBlock = blockNode.getChildNodes();

		exprNode = nodesInBlock[0];
		assertEquals(AstNode.Type.EXPRESSION, exprNode.getType());
		this.checkLiteralNode(exprNode.getChildNodes()[0], "5");
	}



	@Test
	public void testIfStatement() throws VnanoException {

		// " if (x == 2) { } " のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createControlToken(ScriptWord.IF),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_BEGIN),
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken(ScriptWord.EQUAL, PriorityTable.EQUAL, AttributeValue.BINARY, AttributeValue.COMPARISON),
			this.createLiteralToken("2"),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_END),
			this.createBlockToken(ScriptWord.BLOCK_BEGIN),
			this.createBlockToken(ScriptWord.BLOCK_END),
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(2, rootNode.getChildNodes().length);

		// if 文ノードの検査
		AstNode ifNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.IF, ifNode.getType());
		assertEquals(1, ifNode.getChildNodes().length);

		// 条件式ノードの検査
		AstNode conditionExprNode = ifNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, conditionExprNode.getType());
		assertEquals(1, conditionExprNode.getChildNodes().length);

		// 条件式ASTの検査
		AstNode equalNode = conditionExprNode.getChildNodes()[0];
		this.checkOperatorNode(equalNode, ScriptWord.EQUAL, PriorityTable.EQUAL, AttributeValue.BINARY, AttributeValue.COMPARISON);
		assertEquals(2, equalNode.getChildNodes().length);
		this.checkVariableIdentifierNode(equalNode.getChildNodes()[0], "x");
		this.checkLiteralNode(equalNode.getChildNodes()[1], "2");

		// ブロックノードの検査
		AstNode blockNode = rootNode.getChildNodes()[1];
		assertEquals(AstNode.Type.BLOCK, blockNode.getType());
		assertEquals(0, blockNode.getChildNodes().length);
	}


	@Test
	public void testElseStatement() throws VnanoException {

		// " if (x == 2) { 1; } else { 2; }" のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createControlToken(ScriptWord.IF),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_BEGIN),
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken(ScriptWord.EQUAL, PriorityTable.EQUAL, AttributeValue.BINARY, AttributeValue.COMPARISON),
			this.createLiteralToken("2"),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_END),
			this.createBlockToken(ScriptWord.BLOCK_BEGIN),
			this.createLiteralToken("1"),
			this.createEndToken(),
			this.createBlockToken(ScriptWord.BLOCK_END),
			this.createControlToken(ScriptWord.ELSE),
			this.createBlockToken(ScriptWord.BLOCK_BEGIN),
			this.createLiteralToken("2"),
			this.createEndToken(),
			this.createBlockToken(ScriptWord.BLOCK_END),
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(4, rootNode.getChildNodes().length);

		// if 文ノードの検査
		AstNode ifNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.IF, ifNode.getType());
		assertEquals(1, ifNode.getChildNodes().length);

		// 条件式ノードの検査
		AstNode conditionExprNode = ifNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, conditionExprNode.getType());
		assertEquals(1, conditionExprNode.getChildNodes().length);

		// 条件式ASTの検査
		AstNode equalNode = conditionExprNode.getChildNodes()[0];
		this.checkOperatorNode(equalNode, ScriptWord.EQUAL, PriorityTable.EQUAL, AttributeValue.BINARY, AttributeValue.COMPARISON);
		assertEquals(2, equalNode.getChildNodes().length);
		this.checkVariableIdentifierNode(equalNode.getChildNodes()[0], "x");
		this.checkLiteralNode(equalNode.getChildNodes()[1], "2");

		// if 文直後のブロックノードの検査
		AstNode ifBlockNode = rootNode.getChildNodes()[1];
		assertEquals(AstNode.Type.BLOCK, ifBlockNode.getType());
		assertEquals(1, ifBlockNode.getChildNodes().length);
		assertEquals(AstNode.Type.EXPRESSION, ifBlockNode.getChildNodes()[0].getType());
		this.checkLiteralNode(ifBlockNode.getChildNodes()[0].getChildNodes()[0], "1");

		// else 文ノードの検査
		AstNode elseNode = rootNode.getChildNodes()[2];
		assertEquals(AstNode.Type.ELSE, elseNode.getType());
		assertEquals(0, elseNode.getChildNodes().length);

		// else 文直後のブロックノードの検査
		AstNode elseBlockNode = rootNode.getChildNodes()[3];
		assertEquals(AstNode.Type.BLOCK, elseBlockNode.getType());
		assertEquals(1, elseBlockNode.getChildNodes().length);
		assertEquals(AstNode.Type.EXPRESSION, elseBlockNode.getChildNodes()[0].getType());
		this.checkLiteralNode(elseBlockNode.getChildNodes()[0].getChildNodes()[0], "2");
	}


	@Test
	public void testWhileStatement() throws VnanoException {

		// " while (x == 2) { } " のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createControlToken(ScriptWord.WHILE),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_BEGIN),
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken(ScriptWord.EQUAL, PriorityTable.EQUAL, AttributeValue.BINARY, AttributeValue.COMPARISON),
			this.createLiteralToken("2"),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_END),
			this.createBlockToken(ScriptWord.BLOCK_BEGIN),
			this.createBlockToken(ScriptWord.BLOCK_END),
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(2, rootNode.getChildNodes().length);

		// while 文ノードの検査
		AstNode ifNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.WHILE, ifNode.getType());
		assertEquals(1, ifNode.getChildNodes().length);

		// 条件式ノードの検査
		AstNode conditionExprNode = ifNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, conditionExprNode.getType());
		assertEquals(1, conditionExprNode.getChildNodes().length);

		// 条件式ASTの検査
		AstNode equalNode = conditionExprNode.getChildNodes()[0];
		this.checkOperatorNode(equalNode, ScriptWord.EQUAL, PriorityTable.EQUAL, AttributeValue.BINARY, AttributeValue.COMPARISON);
		assertEquals(2, equalNode.getChildNodes().length);
		this.checkVariableIdentifierNode(equalNode.getChildNodes()[0], "x");
		this.checkLiteralNode(equalNode.getChildNodes()[1], "2");

		// ブロックノードの検査
		AstNode blockNode = rootNode.getChildNodes()[1];
		assertEquals(AstNode.Type.BLOCK, blockNode.getType());
		assertEquals(0, blockNode.getChildNodes().length);
	}


	@Test
	public void testForStatement() throws VnanoException {

		// " for (i=0; i<10; ++i) { } " のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createControlToken(ScriptWord.FOR),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_BEGIN),
			this.createVariableIdentifierToken("i"),
			this.createOperatorToken(ScriptWord.ASSIGNMENT, PriorityTable.ASSIGNMENT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createLiteralToken("0"),
			this.createEndToken(),
			this.createVariableIdentifierToken("i"),
			this.createOperatorToken(ScriptWord.LESS_THAN, PriorityTable.LESS_THAN, AttributeValue.BINARY, AttributeValue.COMPARISON),
			this.createLiteralToken("10"),
			this.createEndToken(),
			this.createOperatorToken(ScriptWord.INCREMENT, PriorityTable.PREFIX_INCREMENT, AttributeValue.PREFIX, AttributeValue.ARITHMETIC),
			this.createVariableIdentifierToken("i"),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_END),
			this.createBlockToken(ScriptWord.BLOCK_BEGIN),
			this.createBlockToken(ScriptWord.BLOCK_END),
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(2, rootNode.getChildNodes().length);

		// for 文ノードの検査
		AstNode forNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.FOR, forNode.getType());
		assertEquals(3, forNode.getChildNodes().length);

		// 初期化式のノードおよびASTの検査
		AstNode initExprNode = forNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, initExprNode.getType());
		assertEquals(1, initExprNode.getChildNodes().length);
		AstNode assignNode = initExprNode.getChildNodes()[0];
		this.checkOperatorNode(assignNode, ScriptWord.ASSIGNMENT, PriorityTable.ASSIGNMENT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		assertEquals(2, assignNode.getChildNodes().length);
		this.checkVariableIdentifierNode(assignNode.getChildNodes()[0], "i");
		this.checkLiteralNode(assignNode.getChildNodes()[1], "0");

		// 条件式のノードおよびASTの検査
		AstNode conditionExprNode = forNode.getChildNodes()[1];
		assertEquals(AstNode.Type.EXPRESSION, conditionExprNode.getType());
		assertEquals(1, conditionExprNode.getChildNodes().length);
		AstNode ltNode = conditionExprNode.getChildNodes()[0];
		this.checkOperatorNode(ltNode, ScriptWord.LESS_THAN, PriorityTable.LESS_THAN, AttributeValue.BINARY, AttributeValue.COMPARISON);
		assertEquals(2, ltNode.getChildNodes().length);
		this.checkVariableIdentifierNode(ltNode.getChildNodes()[0], "i");
		this.checkLiteralNode(ltNode.getChildNodes()[1], "10");

		// 更新式およびASTの検査
		AstNode updateExprNode = forNode.getChildNodes()[2];
		assertEquals(AstNode.Type.EXPRESSION, updateExprNode.getType());
		assertEquals(1, updateExprNode.getChildNodes().length);
		assertEquals(1, updateExprNode.getChildNodes().length);
		AstNode incrementNode = updateExprNode.getChildNodes()[0];
		this.checkOperatorNode(incrementNode, ScriptWord.INCREMENT, PriorityTable.PREFIX_INCREMENT, AttributeValue.PREFIX, AttributeValue.ARITHMETIC);
		this.checkVariableIdentifierNode(incrementNode.getChildNodes()[0], "i");

		// ブロックノードの検査
		AstNode blockNode = rootNode.getChildNodes()[1];
		assertEquals(AstNode.Type.BLOCK, blockNode.getType());
		assertEquals(0, blockNode.getChildNodes().length);
	}

	@Test
	public void testForStatementWithCounterVariableDeclaration() throws VnanoException {

		// " for (int i=0; i<10; ++i) { } " のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createControlToken(ScriptWord.FOR),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_BEGIN),
			this.createDataTypeToken("int"),
			this.createVariableIdentifierToken("i"),
			this.createOperatorToken(ScriptWord.ASSIGNMENT, PriorityTable.ASSIGNMENT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createLiteralToken("0"),
			this.createEndToken(),
			this.createVariableIdentifierToken("i"),
			this.createOperatorToken(ScriptWord.LESS_THAN, PriorityTable.LESS_THAN, AttributeValue.BINARY, AttributeValue.COMPARISON),
			this.createLiteralToken("10"),
			this.createEndToken(),
			this.createOperatorToken(ScriptWord.INCREMENT, PriorityTable.PREFIX_INCREMENT, AttributeValue.PREFIX, AttributeValue.ARITHMETIC),
			this.createVariableIdentifierToken("i"),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_END),
			this.createBlockToken(ScriptWord.BLOCK_BEGIN),
			this.createBlockToken(ScriptWord.BLOCK_END),
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(2, rootNode.getChildNodes().length);

		// for 文ノードの検査
		AstNode forNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.FOR, forNode.getType());
		assertEquals(3, forNode.getChildNodes().length);

		// 変数宣言文ノードの検査
		AstNode varNode = forNode.getChildNodes()[0];
		assertEquals(AstNode.Type.VARIABLE, varNode.getType());
		assertEquals("int", varNode.getAttribute(AttributeKey.DATA_TYPE));
		assertEquals("i", varNode.getAttribute(AttributeKey.IDENTIFIER_VALUE));
		assertEquals("0", varNode.getAttribute(AttributeKey.RANK));
		assertEquals(1, varNode.getChildNodes().length);

		// 初期化式のノードおよびASTの検査
		AstNode initExprNode = varNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, initExprNode.getType());
		assertEquals(1, initExprNode.getChildNodes().length);
		AstNode assignNode = initExprNode.getChildNodes()[0];
		this.checkOperatorNode(assignNode, ScriptWord.ASSIGNMENT, PriorityTable.ASSIGNMENT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		assertEquals(2, assignNode.getChildNodes().length);
		this.checkVariableIdentifierNode(assignNode.getChildNodes()[0], "i");
		this.checkLiteralNode(assignNode.getChildNodes()[1], "0");

		// 条件式のノードおよびASTの検査
		AstNode conditionExprNode = forNode.getChildNodes()[1];
		assertEquals(AstNode.Type.EXPRESSION, conditionExprNode.getType());
		assertEquals(1, conditionExprNode.getChildNodes().length);
		AstNode ltNode = conditionExprNode.getChildNodes()[0];
		this.checkOperatorNode(ltNode, ScriptWord.LESS_THAN, PriorityTable.LESS_THAN, AttributeValue.BINARY, AttributeValue.COMPARISON);
		assertEquals(2, ltNode.getChildNodes().length);
		this.checkVariableIdentifierNode(ltNode.getChildNodes()[0], "i");
		this.checkLiteralNode(ltNode.getChildNodes()[1], "10");

		// 更新式およびASTの検査
		AstNode updateExprNode = forNode.getChildNodes()[2];
		assertEquals(AstNode.Type.EXPRESSION, updateExprNode.getType());
		assertEquals(1, updateExprNode.getChildNodes().length);
		assertEquals(1, updateExprNode.getChildNodes().length);
		AstNode incrementNode = updateExprNode.getChildNodes()[0];
		this.checkOperatorNode(incrementNode, ScriptWord.INCREMENT, PriorityTable.PREFIX_INCREMENT, AttributeValue.PREFIX, AttributeValue.ARITHMETIC);
		this.checkVariableIdentifierNode(incrementNode.getChildNodes()[0], "i");

		// ブロックノードの検査
		AstNode blockNode = rootNode.getChildNodes()[1];
		assertEquals(AstNode.Type.BLOCK, blockNode.getType());
		assertEquals(0, blockNode.getChildNodes().length);
	}


	@Test
	public void testContinueStatement() throws VnanoException {

		// "while (true) { continue; }" のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createControlToken(ScriptWord.WHILE),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_BEGIN),
			this.createLiteralToken("true"),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_END),
			this.createBlockToken(ScriptWord.BLOCK_BEGIN),
			this.createControlToken(ScriptWord.CONTINUE),
			this.createEndToken(),
			this.createBlockToken(ScriptWord.BLOCK_END),
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(2, rootNode.getChildNodes().length);

		// while 文ノードの検査
		AstNode ifNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.WHILE, ifNode.getType());
		assertEquals(1, ifNode.getChildNodes().length);

		// 条件式ノードの検査
		AstNode conditionExprNode = ifNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, conditionExprNode.getType());
		assertEquals(1, conditionExprNode.getChildNodes().length);
		this.checkLiteralNode(conditionExprNode.getChildNodes()[0], "true");

		// ブロックノードの検査
		AstNode blockNode = rootNode.getChildNodes()[1];
		assertEquals(AstNode.Type.BLOCK, blockNode.getType());
		assertEquals(1, blockNode.getChildNodes().length);

		// continue 文ノードの検査
		AstNode continueNode = blockNode.getChildNodes()[0];
		assertEquals(AstNode.Type.CONTINUE, continueNode.getType());
		assertEquals(0, continueNode.getChildNodes().length);
	}


	@Test
	public void testBreakStatement() throws VnanoException {

		// "while (true) { break; }" のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createControlToken(ScriptWord.WHILE),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_BEGIN),
			this.createLiteralToken("true"),
			this.createParenthesisToken(ScriptWord.PARENTHESIS_END),
			this.createBlockToken(ScriptWord.BLOCK_BEGIN),
			this.createControlToken(ScriptWord.BREAK),
			this.createEndToken(),
			this.createBlockToken(ScriptWord.BLOCK_END),
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(2, rootNode.getChildNodes().length);

		// while 文ノードの検査
		AstNode ifNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.WHILE, ifNode.getType());
		assertEquals(1, ifNode.getChildNodes().length);

		// 条件式ノードの検査
		AstNode conditionExprNode = ifNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, conditionExprNode.getType());
		assertEquals(1, conditionExprNode.getChildNodes().length);
		this.checkLiteralNode(conditionExprNode.getChildNodes()[0], "true");

		// ブロックノードの検査
		AstNode blockNode = rootNode.getChildNodes()[1];
		assertEquals(AstNode.Type.BLOCK, blockNode.getType());
		assertEquals(1, blockNode.getChildNodes().length);

		// break 文ノードの検査
		AstNode continueNode = blockNode.getChildNodes()[0];
		assertEquals(AstNode.Type.BREAK, continueNode.getType());
		assertEquals(0, continueNode.getChildNodes().length);
	}




	// 前置演算子のテスト
	@Test
	public void testParseExpressionPrefixOperator() throws VnanoException {

		// "++x" のトークン配列を用意
		Token rightToken = this.createVariableIdentifierToken("x");
		Token operatorToken = createOperatorToken(
			ScriptWord.INCREMENT, PriorityTable.PREFIX_INCREMENT, AttributeValue.PREFIX, AttributeValue.ARITHMETIC
		);
		Token[] tokens = new Token[]{
			operatorToken,
			rightToken,
			this.createEndToken()
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		// 式ノードの検査
		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		// "++" 演算子ノードの検査
		AstNode operatorNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(
				operatorNode, ScriptWord.INCREMENT, PriorityTable.PREFIX_DECREMENT,
				AttributeValue.PREFIX, AttributeValue.ARITHMETIC
		);

		// オペランドの検査
		assertEquals(1, operatorNode.getChildNodes().length);
		AstNode[] operandNodes = operatorNode.getChildNodes();
		this.checkVariableIdentifierNode(operandNodes[0], "x");
	}

	// 後置演算子のテスト
	@Test
	public void testParseExpressionPostfixOperator() throws VnanoException {

		// "x++" のトークン配列を用意
		Token leftToken = this.createVariableIdentifierToken("x");
		Token operatorToken = createOperatorToken(
			ScriptWord.INCREMENT, PriorityTable.POSTFIX_INCREMENT, AttributeValue.POSTFIX, AttributeValue.ARITHMETIC
		);
		Token[] tokens = new Token[]{
			leftToken,
			operatorToken,
			this.createEndToken()
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		// 式ノードの検査
		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		// "++" 演算子ノードの検査
		AstNode operatorNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(
			operatorNode, ScriptWord.INCREMENT, PriorityTable.POSTFIX_DECREMENT,
			AttributeValue.POSTFIX, AttributeValue.ARITHMETIC
		);

		// オペランドの検査
		assertEquals(1, operatorNode.getChildNodes().length);
		AstNode[] operandNodes = operatorNode.getChildNodes();
		this.checkVariableIdentifierNode(operandNodes[0], "x");
	}

	// 二項演算子のテスト
	@Test
	public void testParseExpressionBinaryOperator() throws VnanoException {

		// "1 + x" のトークン配列を用意
		Token leftToken = this.createLiteralToken("1");
		Token rightToken = this.createVariableIdentifierToken("x");
		Token operatorToken = createOperatorToken(
			ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token[] tokens = new Token[]{
			leftToken,
			operatorToken,
			rightToken,
			this.createEndToken()
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		// 式ノードの検査
		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		// "+" 演算子ノードの検査
		AstNode operatorNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(
			operatorNode, ScriptWord.PLUS, PriorityTable.ADDITION,
			AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);

		// オペランドの検査
		assertEquals(2, operatorNode.getChildNodes().length);
		AstNode[] operandNodes = operatorNode.getChildNodes();
		this.checkLiteralNode(operandNodes[0], "1");
		this.checkVariableIdentifierNode(operandNodes[1], "x");
	}

	// 多項演算子のテスト
	@Test
	public void testParseExpressionMultiaryOperator() throws VnanoException {

		// "func(1,x)" のトークン配列を用意
		Token identifierToken = this.createVariableIdentifierToken("func");
		Token operatorBeginToken = createOperatorToken( // "(" のトークン
			ScriptWord.PARENTHESIS_BEGIN, PriorityTable.CALL_BEGIN, AttributeValue.MULTIARY, AttributeValue.CALL
		);
		Token leftOperandToken = this.createLiteralToken("1");
		Token operatorSeparatorToken = createOperatorToken( // "," のトークン
			ScriptWord.ARGUMENT_SEPARATOR, PriorityTable.CALL_BEGIN, AttributeValue.MULTIARY_SEPARATOR, AttributeValue.CALL
		);
		Token rightOperandToken = this.createVariableIdentifierToken("x");
		Token operatorEndToken = createOperatorToken( // ")" のトークン
			ScriptWord.PARENTHESIS_END, PriorityTable.CALL_BEGIN, AttributeValue.MULTIARY_END, AttributeValue.CALL
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

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		// 式ノードの検査
		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		// 関数呼び出し演算子ノードの検査
		AstNode operatorNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(
			operatorNode, ScriptWord.PARENTHESIS_BEGIN, PriorityTable.CALL_BEGIN,
			AttributeValue.MULTIARY, AttributeValue.CALL
		);

		// オペランドの検査
		assertEquals(3, operatorNode.getChildNodes().length); // 識別子と引数2個で合計3個の子ノードを持つはず
		AstNode[] operandNodes = operatorNode.getChildNodes();
		this.checkVariableIdentifierNode(operandNodes[0], "func");
		this.checkLiteralNode(operandNodes[1], "1");
		this.checkVariableIdentifierNode(operandNodes[2], "x");
	}


	// 括弧のテスト "1 * (2 + 3)"
	@Test
	public void testParseExpressionParenthesis1() throws VnanoException {

		// "1 * (2 + 3)" のトークン配列を用意
		Token one = this.createLiteralToken("1");
		Token two = this.createLiteralToken("2");
		Token three = this.createLiteralToken("3");
		Token add = createOperatorToken(
			ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token mul = createOperatorToken(
			ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token open = createParenthesisToken(ScriptWord.PARENTHESIS_BEGIN);
		Token close = createParenthesisToken(ScriptWord.PARENTHESIS_END);

		Token[] tokens = new Token[]{ one, mul, open, two, add, three, close, this.createEndToken() };

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		// 式ノードの検査
		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		// "*" 演算子ノードの検査
		AstNode mulNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(
			mulNode, ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION,
			AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);

		// "*" 演算子の左オペランドの検査
		assertEquals(2, mulNode.getChildNodes().length);
		this.checkLiteralNode(mulNode.getChildNodes()[0], "1");

		// "*" 演算子の右オペランド ＝ "+" 演算子ノードの検査
		AstNode addNode = mulNode.getChildNodes()[1];
		this.checkOperatorNode(
			addNode, ScriptWord.PLUS, PriorityTable.ADDITION,
			AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);

		// "+" 演算子のオペランドの検査
		assertEquals(2, addNode.getChildNodes().length);
		this.checkLiteralNode(addNode.getChildNodes()[0], "2");
		this.checkLiteralNode(addNode.getChildNodes()[1], "3");
	}

	// 括弧のテスト "1 + (2 * 3)"
	@Test
	public void testParseExpressionParenthesis2() throws VnanoException {

		// "1 + (2 * 3)" のトークン配列を用意
		Token one = this.createLiteralToken("1");
		Token two = this.createLiteralToken("2");
		Token three = this.createLiteralToken("3");
		Token add = createOperatorToken(
			ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token mul = createOperatorToken(
			ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token open = createParenthesisToken(ScriptWord.PARENTHESIS_BEGIN);
		Token close = createParenthesisToken(ScriptWord.PARENTHESIS_END);

		Token[] tokens = new Token[]{ one, add, open, two, mul, three, close, this.createEndToken() };

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		// 式ノードの検査
		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		// "+" 演算子ノードの検査
		AstNode addNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(
			addNode, ScriptWord.PLUS, PriorityTable.ADDITION,
			AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);

		// "+" 演算子の左オペランドの検査
		assertEquals(2, addNode.getChildNodes().length);
		this.checkLiteralNode(addNode.getChildNodes()[0], "1");

		// "+" 演算子の右オペランド ＝ "*" 演算子ノードの検査
		AstNode mulNode = addNode.getChildNodes()[1];
		this.checkOperatorNode(
			mulNode, ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION,
			AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);

		// "*" 演算子のオペランドの検査
		assertEquals(2, mulNode.getChildNodes().length);
		this.checkLiteralNode(mulNode.getChildNodes()[0], "2");
		this.checkLiteralNode(mulNode.getChildNodes()[1], "3");
	}


	// 括弧のテスト "(1 + 2) * 3"
	@Test
	public void testParseExpressionParenthesis3() throws VnanoException {

		// "(1 + 2) * 3" のトークン配列を用意
		Token one = this.createLiteralToken("1");
		Token two = this.createLiteralToken("2");
		Token three = this.createLiteralToken("3");
		Token add = createOperatorToken(
			ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token mul = createOperatorToken(
			ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token open = createParenthesisToken(ScriptWord.PARENTHESIS_BEGIN);
		Token close = createParenthesisToken(ScriptWord.PARENTHESIS_END);

		Token[] tokens = new Token[]{ open, one, add, two, close, mul, three, this.createEndToken() };

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		// 式ノードの検査
		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		// "*" 演算子ノードの検査
		AstNode mulNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(
				mulNode, ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION,
			AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);

		// "*" 演算子の右オペランドの検査
		assertEquals(2, mulNode.getChildNodes().length);
		this.checkLiteralNode(mulNode.getChildNodes()[1], "3");

		// "*" 演算子の左オペランド ＝ "+" 演算子ノードの検査
		AstNode addNode = mulNode.getChildNodes()[0];
		this.checkOperatorNode(
			addNode, ScriptWord.PLUS, PriorityTable.ADDITION,
			AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);

		// "+" 演算子のオペランドの検査
		assertEquals(2, addNode.getChildNodes().length);
		this.checkLiteralNode(addNode.getChildNodes()[0], "1");
		this.checkLiteralNode(addNode.getChildNodes()[1], "2");
	}

	// 括弧のテスト "(1 * 2) + 3"
	@Test
	public void testParseExpressionParenthesis4() throws VnanoException {

		// "(1 + 2) * 3" のトークン配列を用意
		Token one = this.createLiteralToken("1");
		Token two = this.createLiteralToken("2");
		Token three = this.createLiteralToken("3");
		Token add = createOperatorToken(
			ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token mul = createOperatorToken(
			ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token open = createParenthesisToken(ScriptWord.PARENTHESIS_BEGIN);
		Token close = createParenthesisToken(ScriptWord.PARENTHESIS_END);

		Token[] tokens = new Token[]{ open, one, mul, two, close, add, three, this.createEndToken() };

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		// 式ノードの検査
		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		// "+" 演算子ノードの検査
		AstNode addNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(
			addNode, ScriptWord.PLUS, PriorityTable.ADDITION,
			AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);

		// "+" 演算子の右オペランドの検査
		assertEquals(2, addNode.getChildNodes().length);
		this.checkLiteralNode(addNode.getChildNodes()[1], "3");

		// "+" 演算子の左オペランド ＝ "*" 演算子ノードの検査
		AstNode mulNode = addNode.getChildNodes()[0];
		this.checkOperatorNode(
			mulNode, ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION,
			AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);

		// "*" 演算子のオペランドの検査
		assertEquals(2, mulNode.getChildNodes().length);
		this.checkLiteralNode(mulNode.getChildNodes()[0], "1");
		this.checkLiteralNode(mulNode.getChildNodes()[1], "2");
	}


	// 複数演算子が混合する式のテスト / + + + + のパターン
	@Test
	public void testParseExpressionAddAddAddAdd() throws VnanoException {

		// "1 + 2 + 3 + 4 + 5" のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createLiteralToken("1"),
			this.createOperatorToken(ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("2"),
			this.createOperatorToken(ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("3"),
			this.createOperatorToken(ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("4"),
			this.createOperatorToken(ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("5"),
			this.createEndToken()
		};

		/*
		   // 期待されるASTの構造
		   // (全ての加算を左から順に計算する構造になるはず)

                  ROOT
                   |
                  EXPR
                   |
                  _+_  < 第1階層
                _+_  | < 第2階層
              _+_  | | < 第3階層
            _+_  | | | < 第4階層
           |   | | | |
           1   2 3 4 5
		*/

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		// 式ノードの検査
		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		// 第1階層の演算子・オペランドの検査
		AstNode operatorNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "5");

		// 第2階層の演算子・オペランドの検査
		operatorNode = operatorNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "4");

		// 第3階層の演算子・オペランドの検査
		operatorNode = operatorNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "3");

		// 第4階層の演算子・オペランドの検査
		operatorNode = operatorNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[0], "1");
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "2");
	}


	// 複数演算子が混合する式のテスト / + * * + のパターン
	@Test
	public void testParseExpressionAddMulMulAdd() throws VnanoException {

		// "1 + 2 * 3 * 4 + 5" のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createLiteralToken("1"),
			this.createOperatorToken(ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("2"),
			this.createOperatorToken(ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("3"),
			this.createOperatorToken(ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("4"),
			this.createOperatorToken(ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("5"),
			this.createEndToken()
		};

		/*
		   // 期待されるASTの構造
		   // (2*3*4 を左から順に計算した後に、1と加算し、その後に5と加算する構造になるはず)

                  ROOT
                   |
                  EXPR
                   |
                ___+__  < 第1階層
             __+__    | < 第2階層
            |    _*_  | < 第3階層
            |  _*_  | | < 第4階層
            | |   | | |
            1 2   3 4 5
		*/

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		// 式ノードの検査
		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		// 第1階層の演算子・オペランドの検査
		AstNode operatorNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "5");

		// 第2階層の演算子・オペランドの検査
		operatorNode = operatorNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[0], "1");

		// 第3階層の演算子・オペランドの検査
		operatorNode = operatorNode.getChildNodes()[1];
		this.checkOperatorNode(operatorNode, ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "4");

		// 第4階層の演算子・オペランドの検査
		operatorNode = operatorNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[0], "2");
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "3");
	}


	// 複数演算子が混合する式のテスト / * + + * のパターン
	@Test
	public void testParseExpressionMulAddAddMul() throws VnanoException {

		// "1 * 2 + 3 + 4 * 5" のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createLiteralToken("1"),
			this.createOperatorToken(ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("2"),
			this.createOperatorToken(ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("3"),
			this.createOperatorToken(ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("4"),
			this.createOperatorToken(ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("5"),
			this.createEndToken()
		};

		/*
		   // 期待されるASTの構造
		   // (1*2+3 と 4*5 をそれぞれ左から順に計算した後に、加算する構造になるはず)

                  ROOT
                   |
                  EXPR
                   |
                 __+__     < 第1階層
               _+_   _*_   < 第2階層
             _*_  | |   |  < 第3階層
            |   | | |   |
            1   2 3 4   5
		*/

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		// 式ノードの検査
		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		// 第1階層の演算子・オペランドの検査
		AstNode operatorNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC);

		// 第2階層(左)の演算子・オペランドの検査
		AstNode leftOperatorNode = operatorNode.getChildNodes()[0];
		this.checkOperatorNode(leftOperatorNode, ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(leftOperatorNode.getChildNodes()[1], "3");

		// 第2階層(右)の演算子・オペランドの検査
		AstNode rightOperatorNode = operatorNode.getChildNodes()[1];
		this.checkOperatorNode(rightOperatorNode, ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(rightOperatorNode.getChildNodes()[0], "4");
		this.checkLiteralNode(rightOperatorNode.getChildNodes()[1], "5");

		// 第3階層の演算子・オペランドの検査
		operatorNode = leftOperatorNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[0], "1");
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "2");
	}

	// 複数演算子が混合する式のテスト / + * + * のパターン
	@Test
	public void testParseExpressionAddMulAddMul() throws VnanoException {

		// "1 + 2 * 3 + 4 * 5" のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createLiteralToken("1"),
			this.createOperatorToken(ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("2"),
			this.createOperatorToken(ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("3"),
			this.createOperatorToken(ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("4"),
			this.createOperatorToken(ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("5"),
			this.createEndToken()
		};

		/*
		   // 期待されるASTの構造
		   // (2*3 と 4*5 をそれぞれ計算した後に、1と前者を加算し、それと後者を加算する構造になるはず)

                  ROOT
                   |
                  EXPR
                   |
               ____+__    < 第1階層
             _+_     _*_  < 第2階層
            |  _*_  |   | < 第3階層
            | |   | |   |
            1 2   3 4   5
		*/

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser().parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		// 式ノードの検査
		AstNode expressionNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, expressionNode.getType());
		assertEquals(1, expressionNode.getChildNodes().length);

		// 第1階層の演算子・オペランドの検査
		AstNode operatorNode = expressionNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC);

		// 第2階層(左)の演算子・オペランドの検査
		AstNode leftOperatorNode = operatorNode.getChildNodes()[0];
		this.checkOperatorNode(leftOperatorNode, ScriptWord.PLUS, PriorityTable.ADDITION, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(leftOperatorNode.getChildNodes()[0], "1");

		// 第2階層(右)の演算子・オペランドの検査
		AstNode rightOperatorNode = operatorNode.getChildNodes()[1];
		this.checkOperatorNode(rightOperatorNode, ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(rightOperatorNode.getChildNodes()[0], "4");
		this.checkLiteralNode(rightOperatorNode.getChildNodes()[1], "5");

		// 第3階層の演算子・オペランドの検査
		operatorNode = leftOperatorNode.getChildNodes()[1];
		this.checkOperatorNode(operatorNode, ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[0], "2");
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "3");
	}


}
