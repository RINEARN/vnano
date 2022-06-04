/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;


/**
 * The enum to define keys of attributes of the AST node ({@link AstNode})
 * and the token ({@link Token}) in the compiler of the Vnano.
 */
public enum AttributeKey {

	/** The key of the attribute for storing a data type. */
	DATA_TYPE,

	/** The key of the attribute for storing an array-rank. */
	RANK,

	/** The key of the attribute for storing an (multi dimensional) array-lengths. */
	LENGTHS,

	/** The key of the attribute for storing an identifier. */
	IDENTIFIER_VALUE,

	/**
	 * The key of the attribute for storing the serial number 
	 * to distinguish different variables/functions having the same identifier.
	 */
	IDENTIFIER_SERIAL_NUMBER,

	/** The key of the attribute for storing the content of a literal. */
	LITERAL_VALUE,

	/** The key of the attribute for storing a scope type. */
	SCOPE,

	/** The key of the attribute for storing an name space. */
	NAME_SPACE,

	/** The key of the attribute for storing a beginning label of a loop and so on. */
	BEGIN_LABEL,

	/** The key of the attribute for storing a update-point label of a loop and so on. */
	UPDATE_LABEL,

	/** The key of the attribute for storing an end label of a loop, a short-circuit evaluation, and so on. */
	END_LABEL,

	/** The key of the attribute for storing a type of a leef node. */
	LEAF_TYPE,

	/** The key of the attribute for storing a symbol of an operator. */
	OPERATOR_SYMBOL,

	/** The key of the attribute for storing a precedence of an operator. */
	OPERATOR_PRECEDENCE,

	/** The key of the attribute for storing an associativity of an operator. */
	OPERATOR_ASSOCIATIVITY,

	/**
	 * The key of the attribute for storing a type of syntax 
	 * (for example, binary operator, prefix operator, and so on) of an operator.
	 */
	OPERATOR_SYNTAX,

	/**
	 * The key of the attribute for storing a type of operation 
	 * (for example, arithmetic operator, logical operator, and so on) of an operator.
	 */
	OPERATOR_EXECUTOR,

	/**
	 * The key of the attribute for storing a data-type to perform operation 
	 * (int, float, and so on) of an operator.
	 */
	OPERATOR_EXECUTION_DATA_TYPE,

	/**
	 * The key of the attribute for storing a description in the intermediate code 
	 * (for example, immediate value, register name, and so on).
	 */
	ASSEMBLY_VALUE,

	/**
	 * The key of the attribute for storing the name of a register,
	 * when the AST node represents an operator and an new register is necessary 
	 * to store the evaluated value of that operator.
	 */
	NEW_REGISTER,

	/** The key of the attribute for storing a signature of a callee function. */
	CALLEE_SIGNATURE,

	/** 
	 * The key of the attribute for storing modifiers.
	 * 
	 * When the AST node has multiple modifiers,
	 * they will be stored as a attribute value which delimited by {@link AttributeValue#MODIFIER_SEPARATOR}.
	 */
	MODIFIER,

	/**
	 * The key of the attribute for storing a marker of the AST node of {@link AstNode.Type#STACK_LID} type
	 * which is used temporary in the parser.
	 */
	LID_MARKER,
}
