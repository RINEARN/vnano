/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.spec.OperatorPrecedence;
import org.vcssl.nano.spec.ScriptWord;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.LanguageSpecContainer;

public class ParserTest {

	private final LanguageSpecContainer LANG_SPEC = new LanguageSpecContainer();
	private final OperatorPrecedence OPERATOR_PRECEDENCE = LANG_SPEC.OPERATOR_PRECEDENCE;
	private final ScriptWord SCRIPT_WORD = LANG_SPEC.SCRIPT_WORD;
	private final DataTypeName DATA_TYPE_NAME = LANG_SPEC.DATA_TYPE_NAME;

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
		if (word.equals(SCRIPT_WORD.parenthesisBegin)) {
			token.setPrecedence(OPERATOR_PRECEDENCE.parenthesisBegin);
		} else {
			token.setPrecedence(OPERATOR_PRECEDENCE.leastPrior);
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
		Token token = new Token(SCRIPT_WORD.endOfStatement, 123, "Test.vnano");
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

		// "int x;" のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createDataTypeToken(DATA_TYPE_NAME.defaultInt),
			this.createVariableIdentifierToken("x"),
			this.createEndToken()
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		// 変数ノードの検査
		AstNode varNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.VARIABLE, varNode.getType());
		assertEquals(DATA_TYPE_NAME.defaultInt, varNode.getAttribute(AttributeKey.DATA_TYPE));
		assertEquals("x", varNode.getAttribute(AttributeKey.IDENTIFIER_VALUE));
		assertEquals("0", varNode.getAttribute(AttributeKey.RANK));
	}

	//@Test
	public void testParseVariableDeclarationStatementArray1D() throws VnanoException {

		// "int x [ 2 ];" のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createDataTypeToken(DATA_TYPE_NAME.defaultInt),
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken("[", OPERATOR_PRECEDENCE.subscriptBegin, AttributeValue.LEFT, AttributeValue.MULTIARY, AttributeValue.SUBSCRIPT),
			this.createLiteralToken("2", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken("]", OPERATOR_PRECEDENCE.subscriptEnd, AttributeValue.LEFT, AttributeValue.MULTIARY, AttributeValue.SUBSCRIPT),
			this.createEndToken()
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		// 変数ノードの検査
		AstNode varNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.VARIABLE, varNode.getType());
		assertEquals(DATA_TYPE_NAME.defaultInt, varNode.getAttribute(AttributeKey.DATA_TYPE));
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
			this.createDataTypeToken(DATA_TYPE_NAME.defaultInt),
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken("[", OPERATOR_PRECEDENCE.subscriptBegin, AttributeValue.LEFT, AttributeValue.MULTIARY, AttributeValue.SUBSCRIPT),
			this.createLiteralToken("1", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken("+", OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("2", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken("*", OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("3", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken("]", OPERATOR_PRECEDENCE.subscriptEnd, AttributeValue.LEFT, AttributeValue.MULTIARY, AttributeValue.SUBSCRIPT),
			this.createEndToken()
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		// 変数ノードの検査
		AstNode varNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.VARIABLE, varNode.getType());
		assertEquals(DATA_TYPE_NAME.defaultInt, varNode.getAttribute(AttributeKey.DATA_TYPE));
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
		this.checkOperatorNode(addNode, "+", OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(addNode.getChildNodes()[0], "1");

		AstNode mulNode = addNode.getChildNodes()[1];
		this.checkOperatorNode(mulNode, "*", OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(mulNode.getChildNodes()[0], "2");
		this.checkLiteralNode(mulNode.getChildNodes()[1], "3");
	}

	@Test
	public void testParseVariableDeclarationStatementArray3D() throws VnanoException {

		// "int x [ 2 ][ 3 ][ 4 ];" のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createDataTypeToken(DATA_TYPE_NAME.defaultInt),
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken("[", OPERATOR_PRECEDENCE.subscriptBegin, AttributeValue.LEFT, AttributeValue.MULTIARY, AttributeValue.SUBSCRIPT),
			this.createLiteralToken("2", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken("][", OPERATOR_PRECEDENCE.subscriptSeparator, AttributeValue.LEFT, AttributeValue.MULTIARY, AttributeValue.SUBSCRIPT),
			this.createLiteralToken("3", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken("][", OPERATOR_PRECEDENCE.subscriptSeparator, AttributeValue.LEFT, AttributeValue.MULTIARY, AttributeValue.SUBSCRIPT),
			this.createLiteralToken("4", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken("]", OPERATOR_PRECEDENCE.subscriptEnd, AttributeValue.LEFT, AttributeValue.MULTIARY, AttributeValue.SUBSCRIPT),
			this.createEndToken()
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		// 変数ノードの検査
		AstNode varNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.VARIABLE, varNode.getType());
		assertEquals(DATA_TYPE_NAME.defaultInt, varNode.getAttribute(AttributeKey.DATA_TYPE));
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
			this.createDataTypeToken(DATA_TYPE_NAME.defaultInt),
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken("=", OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createLiteralToken("1", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken("+", OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("2", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken("*", OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("3", DATA_TYPE_NAME.defaultInt),
			this.createEndToken()
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// ルートノードの検査
		assertEquals(AstNode.Type.ROOT, rootNode.getType());
		assertEquals(1, rootNode.getChildNodes().length);

		// 変数ノードの検査
		AstNode varNode = rootNode.getChildNodes()[0];
		assertEquals(AstNode.Type.VARIABLE, varNode.getType());
		assertEquals(DATA_TYPE_NAME.defaultInt, varNode.getAttribute(AttributeKey.DATA_TYPE));
		assertEquals("x", varNode.getAttribute(AttributeKey.IDENTIFIER_VALUE));
		assertEquals("0", varNode.getAttribute(AttributeKey.RANK));

		// 初期化式ノード、およびその下のASTの検査
		AstNode initExprNode = varNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, initExprNode.getType());
		assertEquals(1, initExprNode.getChildNodes().length);

		AstNode assignNode = initExprNode.getChildNodes()[0];
		this.checkOperatorNode(assignNode, "=", OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		this.checkVariableIdentifierNode(assignNode.getChildNodes()[0], "x");

		AstNode addNode = assignNode.getChildNodes()[1];
		this.checkOperatorNode(addNode, "+", OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(addNode.getChildNodes()[0], "1");

		AstNode mulNode = addNode.getChildNodes()[1];
		this.checkOperatorNode(mulNode, "*", OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(mulNode.getChildNodes()[0], "2");
		this.checkLiteralNode(mulNode.getChildNodes()[1], "3");
	}








	@Test
	public void testBlockStatement() throws VnanoException {

		// "{ 1; 2; { 3; 4; { 5; } 6; } 7; }" のトークン配列を用意
		Token[] tokens = new Token[]{
				this.createBlockToken(SCRIPT_WORD.blockBegin),
			this.createLiteralToken("1", DATA_TYPE_NAME.defaultInt),
			this.createEndToken(),
			this.createLiteralToken("2", DATA_TYPE_NAME.defaultInt),
			this.createEndToken(),
			this.createBlockToken(SCRIPT_WORD.blockBegin),
			this.createLiteralToken("3", DATA_TYPE_NAME.defaultInt),
			this.createEndToken(),
			this.createLiteralToken("4", DATA_TYPE_NAME.defaultInt),
			this.createEndToken(),
			this.createBlockToken(SCRIPT_WORD.blockBegin),
			this.createLiteralToken("5", DATA_TYPE_NAME.defaultInt),
			this.createEndToken(),
			this.createBlockToken(SCRIPT_WORD.blockEnd),
			this.createLiteralToken("6", DATA_TYPE_NAME.defaultInt),
			this.createEndToken(),
			this.createBlockToken(SCRIPT_WORD.blockEnd),
			this.createLiteralToken("7", DATA_TYPE_NAME.defaultInt),
			this.createEndToken(),
			this.createBlockToken(SCRIPT_WORD.blockEnd),
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
			this.createControlToken(SCRIPT_WORD.ifStatement),
			this.createParenthesisToken(SCRIPT_WORD.parenthesisBegin),
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken(SCRIPT_WORD.equal, OPERATOR_PRECEDENCE.equal, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.COMPARISON),
			this.createLiteralToken("2", DATA_TYPE_NAME.defaultInt),
			this.createParenthesisToken(SCRIPT_WORD.paranthesisEnd),
			this.createBlockToken(SCRIPT_WORD.blockBegin),
			this.createBlockToken(SCRIPT_WORD.blockEnd),
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
		this.checkOperatorNode(equalNode, SCRIPT_WORD.equal, OPERATOR_PRECEDENCE.equal, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.COMPARISON);
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
			this.createControlToken(SCRIPT_WORD.ifStatement),
			this.createParenthesisToken(SCRIPT_WORD.parenthesisBegin),
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken(SCRIPT_WORD.equal, OPERATOR_PRECEDENCE.equal, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.COMPARISON),
			this.createLiteralToken("2", DATA_TYPE_NAME.defaultInt),
			this.createParenthesisToken(SCRIPT_WORD.paranthesisEnd),
			this.createBlockToken(SCRIPT_WORD.blockBegin),
			this.createLiteralToken("1", DATA_TYPE_NAME.defaultInt),
			this.createEndToken(),
			this.createBlockToken(SCRIPT_WORD.blockEnd),
			this.createControlToken(SCRIPT_WORD.elseStatement),
			this.createBlockToken(SCRIPT_WORD.blockBegin),
			this.createLiteralToken("2", DATA_TYPE_NAME.defaultInt),
			this.createEndToken(),
			this.createBlockToken(SCRIPT_WORD.blockEnd),
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
		this.checkOperatorNode(equalNode, SCRIPT_WORD.equal, OPERATOR_PRECEDENCE.equal, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.COMPARISON);
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
			this.createControlToken(SCRIPT_WORD.whileStatement),
			this.createParenthesisToken(SCRIPT_WORD.parenthesisBegin),
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken(SCRIPT_WORD.equal, OPERATOR_PRECEDENCE.equal, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.COMPARISON),
			this.createLiteralToken("2", DATA_TYPE_NAME.defaultInt),
			this.createParenthesisToken(SCRIPT_WORD.paranthesisEnd),
			this.createBlockToken(SCRIPT_WORD.blockBegin),
			this.createBlockToken(SCRIPT_WORD.blockEnd),
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
		this.checkOperatorNode(equalNode, SCRIPT_WORD.equal, OPERATOR_PRECEDENCE.equal, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.COMPARISON);
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
			this.createControlToken(SCRIPT_WORD.forStatement),
			this.createParenthesisToken(SCRIPT_WORD.parenthesisBegin),
			this.createVariableIdentifierToken("i"),
			this.createOperatorToken(SCRIPT_WORD.assignment, OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createLiteralToken("0", DATA_TYPE_NAME.defaultInt),
			this.createEndToken(),
			this.createVariableIdentifierToken("i"),
			this.createOperatorToken(SCRIPT_WORD.lessThan, OPERATOR_PRECEDENCE.lessThan, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.COMPARISON),
			this.createLiteralToken("10", DATA_TYPE_NAME.defaultInt),
			this.createEndToken(),
			this.createOperatorToken(SCRIPT_WORD.increment, OPERATOR_PRECEDENCE.prefixIncrement, AttributeValue.LEFT, AttributeValue.PREFIX, AttributeValue.ARITHMETIC),
			this.createVariableIdentifierToken("i"),
			this.createParenthesisToken(SCRIPT_WORD.paranthesisEnd),
			this.createBlockToken(SCRIPT_WORD.blockBegin),
			this.createBlockToken(SCRIPT_WORD.blockEnd),
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
		this.checkOperatorNode(assignNode, SCRIPT_WORD.assignment, OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		assertEquals(2, assignNode.getChildNodes().length);
		this.checkVariableIdentifierNode(assignNode.getChildNodes()[0], "i");
		this.checkLiteralNode(assignNode.getChildNodes()[1], "0");

		// 条件式のノードおよびASTの検査
		AstNode conditionExprNode = forNode.getChildNodes()[1];
		assertEquals(AstNode.Type.EXPRESSION, conditionExprNode.getType());
		assertEquals(1, conditionExprNode.getChildNodes().length);
		AstNode ltNode = conditionExprNode.getChildNodes()[0];
		this.checkOperatorNode(ltNode, SCRIPT_WORD.lessThan, OPERATOR_PRECEDENCE.lessThan, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.COMPARISON);
		assertEquals(2, ltNode.getChildNodes().length);
		this.checkVariableIdentifierNode(ltNode.getChildNodes()[0], "i");
		this.checkLiteralNode(ltNode.getChildNodes()[1], "10");

		// 更新式およびASTの検査
		AstNode updateExprNode = forNode.getChildNodes()[2];
		assertEquals(AstNode.Type.EXPRESSION, updateExprNode.getType());
		assertEquals(1, updateExprNode.getChildNodes().length);
		assertEquals(1, updateExprNode.getChildNodes().length);
		AstNode incrementNode = updateExprNode.getChildNodes()[0];
		this.checkOperatorNode(incrementNode, SCRIPT_WORD.increment, OPERATOR_PRECEDENCE.prefixIncrement, AttributeValue.LEFT, AttributeValue.PREFIX, AttributeValue.ARITHMETIC);
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
			this.createControlToken(SCRIPT_WORD.forStatement),
			this.createParenthesisToken(SCRIPT_WORD.parenthesisBegin),
			this.createDataTypeToken(DATA_TYPE_NAME.defaultInt),
			this.createVariableIdentifierToken("i"),
			this.createOperatorToken(SCRIPT_WORD.assignment, OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createLiteralToken("0", DATA_TYPE_NAME.defaultInt),
			this.createEndToken(),
			this.createVariableIdentifierToken("i"),
			this.createOperatorToken(SCRIPT_WORD.lessThan, OPERATOR_PRECEDENCE.lessThan, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.COMPARISON),
			this.createLiteralToken("10", DATA_TYPE_NAME.defaultInt),
			this.createEndToken(),
			this.createOperatorToken(SCRIPT_WORD.increment, OPERATOR_PRECEDENCE.prefixIncrement, AttributeValue.LEFT, AttributeValue.PREFIX, AttributeValue.ARITHMETIC),
			this.createVariableIdentifierToken("i"),
			this.createParenthesisToken(SCRIPT_WORD.paranthesisEnd),
			this.createBlockToken(SCRIPT_WORD.blockBegin),
			this.createBlockToken(SCRIPT_WORD.blockEnd),
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
		assertEquals(DATA_TYPE_NAME.defaultInt, varNode.getAttribute(AttributeKey.DATA_TYPE));
		assertEquals("i", varNode.getAttribute(AttributeKey.IDENTIFIER_VALUE));
		assertEquals("0", varNode.getAttribute(AttributeKey.RANK));
		assertEquals(1, varNode.getChildNodes().length);

		// 初期化式のノードおよびASTの検査
		AstNode initExprNode = varNode.getChildNodes()[0];
		assertEquals(AstNode.Type.EXPRESSION, initExprNode.getType());
		assertEquals(1, initExprNode.getChildNodes().length);
		AstNode assignNode = initExprNode.getChildNodes()[0];
		this.checkOperatorNode(assignNode, SCRIPT_WORD.assignment, OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		assertEquals(2, assignNode.getChildNodes().length);
		this.checkVariableIdentifierNode(assignNode.getChildNodes()[0], "i");
		this.checkLiteralNode(assignNode.getChildNodes()[1], "0");

		// 条件式のノードおよびASTの検査
		AstNode conditionExprNode = forNode.getChildNodes()[1];
		assertEquals(AstNode.Type.EXPRESSION, conditionExprNode.getType());
		assertEquals(1, conditionExprNode.getChildNodes().length);
		AstNode ltNode = conditionExprNode.getChildNodes()[0];
		this.checkOperatorNode(ltNode, SCRIPT_WORD.lessThan, OPERATOR_PRECEDENCE.lessThan, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.COMPARISON);
		assertEquals(2, ltNode.getChildNodes().length);
		this.checkVariableIdentifierNode(ltNode.getChildNodes()[0], "i");
		this.checkLiteralNode(ltNode.getChildNodes()[1], "10");

		// 更新式およびASTの検査
		AstNode updateExprNode = forNode.getChildNodes()[2];
		assertEquals(AstNode.Type.EXPRESSION, updateExprNode.getType());
		assertEquals(1, updateExprNode.getChildNodes().length);
		assertEquals(1, updateExprNode.getChildNodes().length);
		AstNode incrementNode = updateExprNode.getChildNodes()[0];
		this.checkOperatorNode(incrementNode, SCRIPT_WORD.increment, OPERATOR_PRECEDENCE.prefixIncrement, AttributeValue.LEFT, AttributeValue.PREFIX, AttributeValue.ARITHMETIC);
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
			this.createControlToken(SCRIPT_WORD.whileStatement),
			this.createParenthesisToken(SCRIPT_WORD.parenthesisBegin),
			this.createLiteralToken("true", DATA_TYPE_NAME.bool),
			this.createParenthesisToken(SCRIPT_WORD.paranthesisEnd),
			this.createBlockToken(SCRIPT_WORD.blockBegin),
			this.createControlToken(SCRIPT_WORD.continueStatement),
			this.createEndToken(),
			this.createBlockToken(SCRIPT_WORD.blockEnd),
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
			this.createControlToken(SCRIPT_WORD.whileStatement),
			this.createParenthesisToken(SCRIPT_WORD.parenthesisBegin),
			this.createLiteralToken("true", DATA_TYPE_NAME.bool),
			this.createParenthesisToken(SCRIPT_WORD.paranthesisEnd),
			this.createBlockToken(SCRIPT_WORD.blockBegin),
			this.createControlToken(SCRIPT_WORD.breakStatement),
			this.createEndToken(),
			this.createBlockToken(SCRIPT_WORD.blockEnd),
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
			SCRIPT_WORD.increment, OPERATOR_PRECEDENCE.prefixIncrement, AttributeValue.LEFT, AttributeValue.PREFIX, AttributeValue.ARITHMETIC
		);
		Token[] tokens = new Token[]{
			operatorToken,
			rightToken,
			this.createEndToken()
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
				operatorNode, SCRIPT_WORD.increment, OPERATOR_PRECEDENCE.prefixDecrement, AttributeValue.LEFT,
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
			SCRIPT_WORD.increment, OPERATOR_PRECEDENCE.postfixIncrement, AttributeValue.LEFT, AttributeValue.POSTFIX, AttributeValue.ARITHMETIC
		);
		Token[] tokens = new Token[]{
			leftToken,
			operatorToken,
			this.createEndToken()
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
			operatorNode, SCRIPT_WORD.increment, OPERATOR_PRECEDENCE.postfixDecrement, AttributeValue.LEFT,
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
		Token leftToken = this.createLiteralToken("1", DATA_TYPE_NAME.defaultInt);
		Token rightToken = this.createVariableIdentifierToken("x");
		Token operatorToken = createOperatorToken(
			SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token[] tokens = new Token[]{
			leftToken,
			operatorToken,
			rightToken,
			this.createEndToken()
		};

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
			operatorNode, SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT,
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
			SCRIPT_WORD.parenthesisBegin, OPERATOR_PRECEDENCE.callBegin, AttributeValue.LEFT, AttributeValue.MULTIARY, AttributeValue.CALL
		);
		Token leftOperandToken = this.createLiteralToken("1", DATA_TYPE_NAME.defaultInt);
		Token operatorSeparatorToken = createOperatorToken( // "," のトークン
			SCRIPT_WORD.argumentSeparator, OPERATOR_PRECEDENCE.callBegin, AttributeValue.LEFT, AttributeValue.MULTIARY_SEPARATOR, AttributeValue.CALL
		);
		Token rightOperandToken = this.createVariableIdentifierToken("x");
		Token operatorEndToken = createOperatorToken( // ")" のトークン
			SCRIPT_WORD.paranthesisEnd, OPERATOR_PRECEDENCE.callBegin, AttributeValue.LEFT, AttributeValue.MULTIARY_END, AttributeValue.CALL
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

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
			operatorNode, SCRIPT_WORD.parenthesisBegin, OPERATOR_PRECEDENCE.callBegin, AttributeValue.LEFT,
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
		Token one = this.createLiteralToken("1", DATA_TYPE_NAME.defaultInt);
		Token two = this.createLiteralToken("2", DATA_TYPE_NAME.defaultInt);
		Token three = this.createLiteralToken("3", DATA_TYPE_NAME.defaultInt);
		Token add = createOperatorToken(
			SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token mul = createOperatorToken(
			SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token open = createParenthesisToken(SCRIPT_WORD.parenthesisBegin);
		Token close = createParenthesisToken(SCRIPT_WORD.paranthesisEnd);

		Token[] tokens = new Token[]{ one, mul, open, two, add, three, close, this.createEndToken() };

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
			mulNode, SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT,
			AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);

		// "*" 演算子の左オペランドの検査
		assertEquals(2, mulNode.getChildNodes().length);
		this.checkLiteralNode(mulNode.getChildNodes()[0], "1");

		// "*" 演算子の右オペランド ＝ "+" 演算子ノードの検査
		AstNode addNode = mulNode.getChildNodes()[1];
		this.checkOperatorNode(
			addNode, SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT,
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
		Token one = this.createLiteralToken("1", DATA_TYPE_NAME.defaultInt);
		Token two = this.createLiteralToken("2", DATA_TYPE_NAME.defaultInt);
		Token three = this.createLiteralToken("3", DATA_TYPE_NAME.defaultInt);
		Token add = createOperatorToken(
			SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token mul = createOperatorToken(
			SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token open = createParenthesisToken(SCRIPT_WORD.parenthesisBegin);
		Token close = createParenthesisToken(SCRIPT_WORD.paranthesisEnd);

		Token[] tokens = new Token[]{ one, add, open, two, mul, three, close, this.createEndToken() };

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
			addNode, SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT,
			AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);

		// "+" 演算子の左オペランドの検査
		assertEquals(2, addNode.getChildNodes().length);
		this.checkLiteralNode(addNode.getChildNodes()[0], "1");

		// "+" 演算子の右オペランド ＝ "*" 演算子ノードの検査
		AstNode mulNode = addNode.getChildNodes()[1];
		this.checkOperatorNode(
			mulNode, SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT,
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
		Token one = this.createLiteralToken("1", DATA_TYPE_NAME.defaultInt);
		Token two = this.createLiteralToken("2", DATA_TYPE_NAME.defaultInt);
		Token three = this.createLiteralToken("3", DATA_TYPE_NAME.defaultInt);
		Token add = createOperatorToken(
			SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token mul = createOperatorToken(
			SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token open = createParenthesisToken(SCRIPT_WORD.parenthesisBegin);
		Token close = createParenthesisToken(SCRIPT_WORD.paranthesisEnd);

		Token[] tokens = new Token[]{ open, one, add, two, close, mul, three, this.createEndToken() };

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
				mulNode, SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT,
			AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);

		// "*" 演算子の右オペランドの検査
		assertEquals(2, mulNode.getChildNodes().length);
		this.checkLiteralNode(mulNode.getChildNodes()[1], "3");

		// "*" 演算子の左オペランド ＝ "+" 演算子ノードの検査
		AstNode addNode = mulNode.getChildNodes()[0];
		this.checkOperatorNode(
			addNode, SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT,
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
		Token one = this.createLiteralToken("1", DATA_TYPE_NAME.defaultInt);
		Token two = this.createLiteralToken("2", DATA_TYPE_NAME.defaultInt);
		Token three = this.createLiteralToken("3", DATA_TYPE_NAME.defaultInt);
		Token add = createOperatorToken(
			SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token mul = createOperatorToken(
			SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);
		Token open = createParenthesisToken(SCRIPT_WORD.parenthesisBegin);
		Token close = createParenthesisToken(SCRIPT_WORD.paranthesisEnd);

		Token[] tokens = new Token[]{ open, one, mul, two, close, add, three, this.createEndToken() };

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
			addNode, SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT,
			AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);

		// "+" 演算子の右オペランドの検査
		assertEquals(2, addNode.getChildNodes().length);
		this.checkLiteralNode(addNode.getChildNodes()[1], "3");

		// "+" 演算子の左オペランド ＝ "*" 演算子ノードの検査
		AstNode mulNode = addNode.getChildNodes()[0];
		this.checkOperatorNode(
			mulNode, SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT,
			AttributeValue.BINARY, AttributeValue.ARITHMETIC
		);

		// "*" 演算子のオペランドの検査
		assertEquals(2, mulNode.getChildNodes().length);
		this.checkLiteralNode(mulNode.getChildNodes()[0], "1");
		this.checkLiteralNode(mulNode.getChildNodes()[1], "2");
	}


	// 複数演算子が混合する式のテスト ( + + + + のパターン )
	@Test
	public void testParseExpressionAddAddAddAdd() throws VnanoException {

		// "1 + 2 + 3 + 4 + 5 ;" のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createLiteralToken("1", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken(SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("2", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken(SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("3", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken(SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("4", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken(SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("5", DATA_TYPE_NAME.defaultInt),
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

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
		this.checkOperatorNode(operatorNode, SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "5");

		// 第2階層の演算子・オペランドの検査
		operatorNode = operatorNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "4");

		// 第3階層の演算子・オペランドの検査
		operatorNode = operatorNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "3");

		// 第4階層の演算子・オペランドの検査
		operatorNode = operatorNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[0], "1");
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "2");
	}


	// 複数演算子が混合する式のテスト ( + * * + のパターン )
	@Test
	public void testParseExpressionAddMulMulAdd() throws VnanoException {

		// "1 + 2 * 3 * 4 + 5 ;" のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createLiteralToken("1", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken(SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("2", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken(SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("3", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken(SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("4", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken(SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("5", DATA_TYPE_NAME.defaultInt),
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

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
		this.checkOperatorNode(operatorNode, SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "5");

		// 第2階層の演算子・オペランドの検査
		operatorNode = operatorNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[0], "1");

		// 第3階層の演算子・オペランドの検査
		operatorNode = operatorNode.getChildNodes()[1];
		this.checkOperatorNode(operatorNode, SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "4");

		// 第4階層の演算子・オペランドの検査
		operatorNode = operatorNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[0], "2");
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "3");
	}


	// 複数演算子が混合する式のテスト ( * + + * のパターン )
	@Test
	public void testParseExpressionMulAddAddMul() throws VnanoException {

		// "1 * 2 + 3 + 4 * 5 ;" のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createLiteralToken("1", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken(SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("2", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken(SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("3", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken(SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("4", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken(SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("5", DATA_TYPE_NAME.defaultInt),
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

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
		this.checkOperatorNode(operatorNode, SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);

		// 第2階層(左)の演算子・オペランドの検査
		AstNode leftOperatorNode = operatorNode.getChildNodes()[0];
		this.checkOperatorNode(leftOperatorNode, SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(leftOperatorNode.getChildNodes()[1], "3");

		// 第2階層(右)の演算子・オペランドの検査
		AstNode rightOperatorNode = operatorNode.getChildNodes()[1];
		this.checkOperatorNode(rightOperatorNode, SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(rightOperatorNode.getChildNodes()[0], "4");
		this.checkLiteralNode(rightOperatorNode.getChildNodes()[1], "5");

		// 第3階層の演算子・オペランドの検査
		operatorNode = leftOperatorNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[0], "1");
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "2");
	}

	// 複数演算子が混合する式のテスト ( + * + * のパターン )
	@Test
	public void testParseExpressionAddMulAddMul() throws VnanoException {

		// "1 + 2 * 3 + 4 * 5 ;" のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createLiteralToken("1", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken(SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("2", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken(SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("3", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken(SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("4", DATA_TYPE_NAME.defaultInt),
			this.createOperatorToken(SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC),
			this.createLiteralToken("5", DATA_TYPE_NAME.defaultInt),
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

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
		this.checkOperatorNode(operatorNode, SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);

		// 第2階層(左)の演算子・オペランドの検査
		AstNode leftOperatorNode = operatorNode.getChildNodes()[0];
		this.checkOperatorNode(leftOperatorNode, SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(leftOperatorNode.getChildNodes()[0], "1");

		// 第2階層(右)の演算子・オペランドの検査
		AstNode rightOperatorNode = operatorNode.getChildNodes()[1];
		this.checkOperatorNode(rightOperatorNode, SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(rightOperatorNode.getChildNodes()[0], "4");
		this.checkLiteralNode(rightOperatorNode.getChildNodes()[1], "5");

		// 第3階層の演算子・オペランドの検査
		operatorNode = leftOperatorNode.getChildNodes()[1];
		this.checkOperatorNode(operatorNode, SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, AttributeValue.LEFT, AttributeValue.BINARY, AttributeValue.ARITHMETIC);
		this.checkLiteralNode(operatorNode.getChildNodes()[0], "2");
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "3");
	}


	// 複数演算子が混合する式のテスト ( = = のパターン )
	@Test
	public void testParseExpressionDualAssignment() throws VnanoException {

		// "x = y = 1 ;" のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken(SCRIPT_WORD.assignment, OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createVariableIdentifierToken("y"),
			this.createOperatorToken(SCRIPT_WORD.assignment, OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createLiteralToken("1", DATA_TYPE_NAME.defaultInt),
			this.createEndToken()
		};

		/*
		   // 期待されるASTの構造（※ 代入演算子「 = 」は右結合）

		       ROOT
		        |
		       EXPR
		        |
		      __=__    < 第1階層
		     |    _=_  < 第2階層
		     |   |   |
		     x   y   1
		*/

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
		this.checkOperatorNode(operatorNode, SCRIPT_WORD.assignment, OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		this.checkVariableIdentifierNode(operatorNode.getChildNodes()[0], "x");

		// 第2階層の演算子・オペランドの検査
		operatorNode = operatorNode.getChildNodes()[1];
		this.checkOperatorNode(operatorNode, SCRIPT_WORD.assignment, OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		this.checkVariableIdentifierNode(operatorNode.getChildNodes()[0], "y");
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "1");
	}


	// 複数演算子が混合する式のテスト ( = = = のパターン )
	@Test
	public void testParseExpressionTrippleAssignment() throws VnanoException {

		// "x = y = z = 1 ;" のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken(SCRIPT_WORD.assignment, OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createVariableIdentifierToken("y"),
			this.createOperatorToken(SCRIPT_WORD.assignment, OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createVariableIdentifierToken("z"),
			this.createOperatorToken(SCRIPT_WORD.assignment, OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createLiteralToken("1", DATA_TYPE_NAME.defaultInt),
			this.createEndToken()
		};

		/*
		   // 期待されるASTの構造（※ 代入演算子「 = 」は右結合）

		       ROOT
		        |
		       EXPR
		        |
		      __=__         < 第1階層
		     |    _=___     < 第2階層
		     |   |    _=_   < 第3階層
		     |   |   |   |
		     x   y   z   1
		*/

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
		this.checkOperatorNode(operatorNode, SCRIPT_WORD.assignment, OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		this.checkVariableIdentifierNode(operatorNode.getChildNodes()[0], "x");

		// 第2階層の演算子・オペランドの検査
		operatorNode = operatorNode.getChildNodes()[1];
		this.checkOperatorNode(operatorNode, SCRIPT_WORD.assignment, OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		this.checkVariableIdentifierNode(operatorNode.getChildNodes()[0], "y");

		// 第3階層の演算子・オペランドの検査
		operatorNode = operatorNode.getChildNodes()[1];
		this.checkOperatorNode(operatorNode, SCRIPT_WORD.assignment, OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		this.checkVariableIdentifierNode(operatorNode.getChildNodes()[0], "z");
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "1");
	}


	// 複数演算子が混合する式のテスト ( = = = = のパターン )
	@Test
	public void testParseExpressionQuadrupleAssignment() throws VnanoException {

		// "x = y = z = w = 1 ;" のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createVariableIdentifierToken("x"),
			this.createOperatorToken(SCRIPT_WORD.assignment, OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createVariableIdentifierToken("y"),
			this.createOperatorToken(SCRIPT_WORD.assignment, OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createVariableIdentifierToken("z"),
			this.createOperatorToken(SCRIPT_WORD.assignment, OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createVariableIdentifierToken("w"),
			this.createOperatorToken(SCRIPT_WORD.assignment, OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT),
			this.createLiteralToken("1", DATA_TYPE_NAME.defaultInt),
			this.createEndToken()
		};

		/*
		   // 期待されるASTの構造（※ 代入演算子「 = 」は右結合）

		       ROOT
		        |
		       EXPR
		        |
		      __=__             < 第1階層
		     |    _=___         < 第2階層
		     |   |    _=___     < 第3階層
		     |   |   |    _=_   < 第4海藻
		     |   |   |   |   |
		     x   y   z   w   1
		*/

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
		this.checkOperatorNode(operatorNode, SCRIPT_WORD.assignment, OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		this.checkVariableIdentifierNode(operatorNode.getChildNodes()[0], "x");

		// 第2階層の演算子・オペランドの検査
		operatorNode = operatorNode.getChildNodes()[1];
		this.checkOperatorNode(operatorNode, SCRIPT_WORD.assignment, OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		this.checkVariableIdentifierNode(operatorNode.getChildNodes()[0], "y");

		// 第3階層の演算子・オペランドの検査
		operatorNode = operatorNode.getChildNodes()[1];
		this.checkOperatorNode(operatorNode, SCRIPT_WORD.assignment, OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		this.checkVariableIdentifierNode(operatorNode.getChildNodes()[0], "z");

		// 第4階層の演算子・オペランドの検査
		operatorNode = operatorNode.getChildNodes()[1];
		this.checkOperatorNode(operatorNode, SCRIPT_WORD.assignment, OPERATOR_PRECEDENCE.assignment, AttributeValue.RIGHT, AttributeValue.BINARY, AttributeValue.ASSIGNMENT);
		this.checkVariableIdentifierNode(operatorNode.getChildNodes()[0], "w");
		this.checkLiteralNode(operatorNode.getChildNodes()[1], "1");
	}


	// 複数演算子が混合する式のテスト ( 前置 ++ -- のパターン )
	@Test
	public void testParseExpressionPrefixIncrementDecrement() throws VnanoException {

		// "++ -- x ;" のトークン配列を用意
		Token[] tokens = new Token[]{
			this.createOperatorToken(SCRIPT_WORD.increment, OPERATOR_PRECEDENCE.prefixIncrement, AttributeValue.RIGHT, AttributeValue.PREFIX, AttributeValue.ARITHMETIC),
			this.createOperatorToken(SCRIPT_WORD.decrement, OPERATOR_PRECEDENCE.prefixDecrement, AttributeValue.RIGHT, AttributeValue.PREFIX, AttributeValue.ARITHMETIC),
			this.createVariableIdentifierToken("x"),
			this.createEndToken()
		};

		/*
		   // 期待されるASTの構造（※ 前置インクリメント/デクリメントは右結合）

		       ROOT
		        |
		       EXPR
		        |
		       ++      < 第1階層
		        |
		       --      < 第2階層
		        |
		        x
		*/

		// トークン配列の内容を確認
		//this.dumpTokens(tokens);

		AstNode rootNode = new Parser(LANG_SPEC).parse(tokens);

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
		this.checkOperatorNode(operatorNode, SCRIPT_WORD.increment, OPERATOR_PRECEDENCE.prefixIncrement, AttributeValue.RIGHT, AttributeValue.PREFIX, AttributeValue.ARITHMETIC);

		// 第2階層の演算子・オペランドの検査
		operatorNode = operatorNode.getChildNodes()[0];
		this.checkOperatorNode(operatorNode, SCRIPT_WORD.decrement, OPERATOR_PRECEDENCE.prefixIncrement, AttributeValue.RIGHT, AttributeValue.PREFIX, AttributeValue.ARITHMETIC);
		this.checkVariableIdentifierNode(operatorNode.getChildNodes()[0], "x");
	}

}
