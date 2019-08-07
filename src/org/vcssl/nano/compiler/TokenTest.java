/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vcssl.nano.spec.OperatorPrecedence;

public class TokenTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetValue() {
		Token token = new Token("Hello", 123, "Test.vnano");
		assertEquals("Hello", token.getValue());
	}

	@Test
	public void testGetLineNumber() {
		Token token = new Token("Hello", 123, "Test.vnano");
		assertEquals(123, token.getLineNumber());
	}

	@Test
	public void testGetFileName() {
		Token token = new Token("Hello", 123, "Test.vnano");
		assertEquals("Test.vnano", token.getFileName());
	}

	@Test
	public void testSetGetType() {
		Token token = new Token("Hello", 123, "Test.vnano");

		token.setType(Token.Type.DATA_TYPE);
		assertEquals(Token.Type.DATA_TYPE, token.getType());

		token.setType(Token.Type.LEAF);
		assertEquals(Token.Type.LEAF, token.getType());

		token.setType(Token.Type.PARENTHESIS);
		assertEquals(Token.Type.PARENTHESIS, token.getType());

		token.setType(Token.Type.BLOCK);
		assertEquals(Token.Type.BLOCK, token.getType());

		token.setType(Token.Type.CONTROL);
		assertEquals(Token.Type.CONTROL, token.getType());

		token.setType(Token.Type.END_OF_STATEMENT);
		assertEquals(Token.Type.END_OF_STATEMENT, token.getType());

		token.setType(Token.Type.OPERATOR);
		assertEquals(Token.Type.OPERATOR, token.getType());
	}

	@Test
	public void testSetGetHasAttribute() {
		Token token = new Token("Hello", 123, "Test.vnano");
		assertFalse(token.hasAttribute(AttributeKey.IDENTIFIER_VALUE));
		token.setAttribute(AttributeKey.IDENTIFIER_VALUE, "world");
		assertTrue(token.hasAttribute(AttributeKey.IDENTIFIER_VALUE));
		assertEquals("world", token.getAttribute(AttributeKey.IDENTIFIER_VALUE));
	}

	@Test
	public void testSetGetOperatorPriority() {
		Token token = new Token("Hello", 123, "Test.vnano");
		token.setPrecedence(OperatorPrecedence.ADDITION);
		assertEquals(OperatorPrecedence.ADDITION, token.getPrecedence());
	}


	@Test
	public void testToString() {
		Token token = new Token("+", 123, "Test.vnano");
		token.setType(Token.Type.OPERATOR);
		token.setAttribute(AttributeKey.DATA_TYPE, "int");
		token.setAttribute(AttributeKey.OPERATOR_SYMBOL, "+");
		token.setPrecedence(1);

		assertEquals(
			"[Token word=\"+\", lineNumber=123, fileName=\"Test.vnano\", type=OPERATOR, priority=1, DATA_TYPE=\"int\", OPERATOR_SYMBOL=\"+\"]",
			token.toString()
		);
	}


}
