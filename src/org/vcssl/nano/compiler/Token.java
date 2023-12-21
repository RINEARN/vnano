/*
 * Copyright(C) 2017-2022 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The class represents a token in the compiler of the Vnano.
 */
public class Token implements Cloneable {

	/**
	 * The enum to distinguish types of {@link Token Token} in the compiler of the Vnano.
	 */
	public static enum Type {

		/** Represents the token of a data-type. */
		DATA_TYPE,

		/** Represents the token of a leaf element (literal, variable identifier, and so ons). */
		LEAF,

		/** Represents the token of a syntactic parenthesis. */
		PARENTHESIS,

		/** Represents the beginning/ending token of a block. */
		BLOCK,

		/** Represents the token of the name of a control statement ("if", "for", and so on). */
		CONTROL,

		/** Represents the token of an end-of-statement (";"). */
		END_OF_STATEMENT,

		/** Represents the token of an operator. */
		OPERATOR,

		/** Represents the token of an import/include declaration.. */
		DEPENDENCY_DECLARATOR,

		/** Represents the token of a modifier ("const", "&", and so on). */
		MODIFIER,
	}


	/** Stores the value of this token. */
	private String value;

	/** Stores the type of this token. */
	private Type type;

	/** Stores the operator precedence of this token. Note that, smaller value gives higher precedence. */
	private int precedence;

	/** Stores the number of the line in which this token is written. */
	private int lineNumber;

	/** Stores the name of the script in which this token is written. */
	private String fileName;

	/** Stores attributes (data-type of literal, syntactic-type of operators, and so on) of this token. */
	private Map<AttributeKey, String> attributeMap;


	/**
	 * Creates a token having the specified value.
	 *
	 * @param value The value of the token.
	 * @param lineNumber The number of the line in which this token is written.
	 * @param fileName The name of the script in which this token is written.
	 */
	public Token(String value, int lineNumber, String fileName){
		this.value = value;
		this.type = null;
		this.precedence = 0;
		this.lineNumber = lineNumber;
		this.fileName = fileName;

		// To keep the order of attributes in the content of "toString()", using LinkedHashMap instead of HashMap.
		this.attributeMap = new LinkedHashMap<AttributeKey, String>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Token clone() {
		Token clone = new Token(this.value, this.lineNumber, this.fileName);
		clone.type = this.type;
		clone.precedence = this.precedence;
		clone.attributeMap = (Map<AttributeKey,String>)( ((LinkedHashMap<AttributeKey,String>)this.attributeMap).clone() );
		return clone;
	}


	/**
	 * Sets the value of this token.
	 *
	 * @param value The value of this token.
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Gets the value of this token.
	 *
	 * @return The value of this token.
	 */
	public String getValue() {
		return this.value;
	}


	/**
	 * Gets the number of the line in which this token is written.
	 *
	 * @return The number of the line in which this token is written.
	 */
	public int getLineNumber() {
		return this.lineNumber;
	}


	/**
	 * Gets the name of the script in which this token is written.
	 *
	 * @return The name of the script in which this token is written.
	 */
	public String getFileName() {
		return this.fileName;
	}


	/**
	 * Sets the type of this token.
	 *
	 * @param type The type of this token.
	 */
	public void setType(Type type) {
		this.type = type;
	}


	/**
	 * Gets the type of this token.
	 *
	 * @return The type of this token.
	 */
	public Type getType() {
		return this.type;
	}


	/**
	 * Set the attribute of this token.
	 * When the attribute having the same key is already registered, the value of it will be overwritten.
	 *
	 * @param attributeKey The key (name) of the attribute to be set.
	 * @param attributeValue The value of the attribute to be set.
	 */
	public void setAttribute(AttributeKey attributeKey, String attributeValue) {
		if (this.attributeMap.containsKey(attributeKey)) {
			this.attributeMap.remove(attributeKey);
		}
		this.attributeMap.put(attributeKey, attributeValue);
	}


	/**
	 * Get the value of the attribute having the specified key (name).
	 *
	 * @param key The key of the attribute.
	 * @return The value of the attribute.
	 */
	public String getAttribute(AttributeKey attributeKey) {
		return this.attributeMap.get(attributeKey);
	}


	/**
	 * Returns whether this token has the attribute corresponding the specified key or hasn't.
	 *
	 * @param attributeKey The key of the attribute.
	 * @return Returns true if this token has the specified attribute.
	 */
	public boolean hasAttribute(AttributeKey attributeKey) {
		return this.attributeMap.containsKey(attributeKey);
	}


	/**
	 * Sets the operator precedence of this token.
	 * Note that, smaller value gives higher precedence.
	 *
	 * @param precedence The operator precedence of this token.
	 */
	public void setPrecedence(int precedence) {
		this.precedence = precedence;
	}


	/**
	 * Gets the operator precedence of this token.
	 * Note that, smaller value gives higher precedence.
	 *
	 * @return The operator precedence of this token.
	 */
	public int getPrecedence() {
		return this.precedence;
	}


	/**
	 * Finds the token having the specified value form an array of tokens.
	 *
	 * @param tokens The array of tokens.
	 * @param tokenValue The value of the token you want to find.
	 * @param fromIndex The token will be searched in the range from this index to the end, in the array.
	 * @return Returns the index of the firstly found token, or -1 when no token hasn't been found.
	 */
	public static int getIndexOf(Token[] tokens, String tokenValue, int fromIndex) {
		int n = tokens.length;
		for(int i=fromIndex; i<n; i++) {
			if (tokens[i].getValue().equals(tokenValue)) {
				return i;
			}
		}
		return -1;
	}


	/**
	 * Returns the string representation (debugging information, not the value) of this token.
	 *
	 * @return The string representation of this token.
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(
				"[Token word=\"" + this.value + "\""
				+ ", lineNumber=" + this.lineNumber
				+ ", fileName=\"" + this.fileName + "\""
				+ ", type=" + this.type
				+ ", precedence=" + precedence
		);
		Set<Map.Entry<AttributeKey,String>> attibutes = this.attributeMap.entrySet();
		for (Map.Entry<AttributeKey,String> attribute: attibutes) {
			builder.append(", " + attribute.getKey() + "=\"" + attribute.getValue() + "\"");
		}
		builder.append("]");
		return builder.toString();
	}

}

