/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

/**
 * The class defining precedence of operators.
 */
public class OperatorPrecedence {

	// !!! Important Note !!!   The smaller value of the precedence makes the priority of the operator higher.


	// --------------------------------------------------
	// Highest / Lowest Precedences
	// (WARNING: Don't use Integer.MAX_VALUE or MIN_VALUE,
	//  otherwise relative precedence may overflow.)
	// --------------------------------------------------

	/** The least prior precedence. */
	public static final int LEAST_PRIOR = 10000000;

	/** The most prior precedence. */
	public static final int MOST_PRIOR = -1;

	/** The precedence of open parenthesis: "(". */
	public static final int PARENTHESIS_BEGIN = MOST_PRIOR;

	/** The precedence of closing parenthesis: ")". */
	// Note: Precedence of non-beginning parts of MULTIARY type operators are always LEAST_PRIOR.
	public static final int PARENTHESIS_END = LEAST_PRIOR;


	// --------------------------------------------------
	// Multiary Operators
	// --------------------------------------------------

	/** The precedence of the function call operator: "(". */
	public static final int CALL_BEGIN = 1000;

	/** The precedence of the argument-separator of the function call operator: ",". */
	// Note: Precedence of non-beginning parts of MULTIARY type operators are always LEAST_PRIOR.
	public static final int CALL_SEPARATOR = LEAST_PRIOR;

	/** * The precedence of the end of the function call operator: ")". */
	// Note: Precedence of non-beginning parts of MULTIARY type operators are always LEAST_PRIOR.
	public static final int CALL_END = LEAST_PRIOR;

	/** The precedence of the subscript (array index) operator: "[". */
	public static final int SUBSCRIPT_BEGIN = 1000;

	/** The precedence of the dimension-separator of the subscript (array index) operator: "][". */
	// Note: Precedence of non-beginning parts of MULTIARY type operators are always LEAST_PRIOR.
	public static final int SUBSCRIPT_SEPARATOR = LEAST_PRIOR;

	/** The precedence of the end of the subscript (array index) operator: "]". */
	// Note: Precedence of non-beginning parts of MULTIARY type operators are always LEAST_PRIOR.
	public static final int SUBSCRIPT_END = LEAST_PRIOR;

	// --------------------------------------------------
	// Postfix Operators
	// --------------------------------------------------

	/** The precedence of the postfix increment operator: "++". */
	public static final int POSTFIX_INCREMENT = 1000;

	/** The precedence of the postfix decrement operator: "--". */
	public static final int POSTFIX_DECREMENT = 1000;


	// --------------------------------------------------
	// Prefix Operators
	// --------------------------------------------------

 	/** The precedence of the prefix increment operator: "++". */
	public static final int PREFIX_INCREMENT = 2000;

	/** The precedence of the prefix decrement operator: "--". */
	public static final int PREFIX_DECREMENT = 2000;

	/** The precedence of the unary plus operator: "+". */
	public static final int PREFIX_PLUS = 2000;

	/** The precedence of the unary minus operator: "-". */
	public static final int PREFIX_MINUS = 2000;

	/** The precedence of the logical-not operator: "!". */
	public static final int NOT = 2000;


	/** The precedence of the cast operator: "(". */
	public static final int CAST_BEGIN = 2000;

	/**
	 * The precedence of the end of the cast operator: ")".
	 */
	// Note: The lowest precedence is set to this symbol for the convenience in the implementation of the parser.
	public static final int CAST_END = LEAST_PRIOR;


	// --------------------------------------------------
	// Arithmetic Binary Operators
	// --------------------------------------------------

	/** The precedence of the multiplication operator: "*". */
	public static final int MULTIPLICATION = 3000;

	/** The precedence of the division operator: "/". */
	public static final int DIVISION = 3000;

	/** The precedence of the remainder operator: "%". */
	public static final int REMAINDER = 3000;

	/** The precedence of the addition operator: "+". */
	public static final int ADDITION = 3100;

	/** The precedence of the subtraction operator: "-". */
	public static final int SUBTRACTION = 3100;


	// --------------------------------------------------
	// Comparison Binary Operators
	// --------------------------------------------------

	/** The precedence of the "less-than" comparison operator: "&lt;". */
	public static final int LESS_THAN = 4000;

	/** The precedence of the "grater-equals" comparison operator: "&lt;=". */
	public static final int LESS_EQUAL = 4000;

	/** The precedence of the "greater-than" comparison operator: "&gt;". */
	public static final int GREATER_THAN = 4000;

	/** The precedence of the "greater-equals" comparison operator: "&gt;=". */
	public static final int GREATER_EQUAL = 4000;

	/** The precedence of the equality comparison operator: "==". */
	public static final int EQUAL = 4100;

	/** The precedence of the "non-equality" comparison operator: "&#33;=". */
	public static final int NOT_EQUAL = 4100;


	// --------------------------------------------------
	// Logical Binary Operators
	// --------------------------------------------------

	/** The precedence of logical-and operator: "&amp;&amp;". */
	public static final int SHORT_CIRCUIT_AND = 5000;

	/** The precedence of logical-or operator: "||". */
	public static final int SHORT_CIRCUIT_OR = 5100;


	// --------------------------------------------------
	// Assignment and Compound Assignment Operators
	// --------------------------------------------------

	/** The precedence of the assignment operator: "=". */
	public static final int ASSIGNMENT = 6000;

	/** The precedence of the compound assignment operator of the multiplication: "*=". */
	public static final int MULTIPLICATION_ASSIGNMENT = 6000;

	/** The precedence of the compound assignment operator of the division: "/=". */
	public static final int DIVISION_ASSIGNMENT = 6000;

	/** The precedence of the compound assignment operator of the remainder: "%=". */
	public static final int REMAINDER_ASSIGNMENT = 6000;

	/** The precedence of the compound assignment operator of the addition: "+=". */
	public static final int ADDITION_ASSIGNMENT = 6000;

	/** The precedence of the compound assignment operator of the subtraction: "-=". */
	public static final int SUBTRACTION_ASSIGNMENT = 6000;

}

