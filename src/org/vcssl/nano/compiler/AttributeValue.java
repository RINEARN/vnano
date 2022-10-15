/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;


/**
 * The class defining values of attributes of the AST node ({@link AstNode}),
 * and the token ({@link Token}) in the compiler of the Vnano.
 */
public class AttributeValue {

	/**
	 * Represents right-associations, 
	 * as the value of {@link AttributeKey#OPERATOR_EXECUTIOR OPERATOR_ASSOCIATIVITY} attribute.
	 */
	public static final String RIGHT = "right";

	/**
	 * Represents left-associations, 
	 * as the value of {@link AttributeKey#OPERATOR_EXECUTIOR OPERATOR_ASSOCIATIVITY} attribute.
	 */
	public static final String LEFT = "left";

	/**
	 * Represents arithmetic operators, 
	 * as the value of {@link AttributeKey#OPERATOR_EXECUTIOR OPERATOR_EXECUTIOR} attribute.
	 */
	public static final String ARITHMETIC = "arithmetic";

	/**
	 * Represents arithmetic-compound-assignment operators, 
	 * as the value of {@link AttributeKey#OPERATOR_EXECUTIOR OPERATOR_EXECUTIOR} attribute.
	 */
	public static final String ARITHMETIC_COMPOUND_ASSIGNMENT = "arithmeticCompoundAssignment";

	/**
	 * Represents logical operators, 
	 * as the value of {@link AttributeKey#OPERATOR_EXECUTIOR OPERATOR_EXECUTIOR} attribute.
	 */
	public static final String LOGICAL = "logical";

	/**
	 * Represents comparison operators, 
	 * as the value of {@link AttributeKey#OPERATOR_EXECUTIOR OPERATOR_EXECUTIOR} attribute.
	 */
	public static final String COMPARISON = "comparison";

	/**
	 * Represents sign operators, 
	 * as the value of {@link AttributeKey#OPERATOR_EXECUTIOR OPERATOR_EXECUTIOR} attribute.
	 */
	public static final String SIGN = "sign";

	/**
	 * Represents assignment operators, 
	 * as the value of {@link AttributeKey#OPERATOR_EXECUTIOR OPERATOR_EXECUTIOR} attribute.
	 */
	public static final String ASSIGNMENT = "assignment";

	/**
	 * Represents function-call operators, 
	 * as the value of {@link AttributeKey#OPERATOR_EXECUTIOR OPERATOR_EXECUTIOR} attribute.
	 */
	public static final String CALL = "call";

	/**
	 * Represents subscript operators, 
	 * as the value of {@link AttributeKey#OPERATOR_EXECUTIOR OPERATOR_EXECUTIOR} attribute.
	 */
	public static final String SUBSCRIPT = "subscript";

	/**
	 * Represents cast operators, 
	 * as the value of {@link AttributeKey#OPERATOR_EXECUTIOR OPERATOR_EXECUTIOR} attribute.
	 */
	public static final String CAST = "cast";


	/**
	 * Represents prefix operators, 
	 * as the value of {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX} attribute.
	 */
	public static final String PREFIX = "prefix";

	/**
	 * Represents postfix operators, 
	 * as the value of {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX} attribute.
	 */
	public static final String POSTFIX = "postfix";

	/**
	 * Represents binary operators, 
	 * as the value of {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX} attribute.
	 */
	public static final String BINARY = "binary";

	/**
	 * Represents multiary operators and its beginning tokens, 
	 * as the value of {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX} attribute.
	 */
	public static final String MULTIARY = "multiary";

	/**
	 * Represents separator tokens of multiary operators, 
	 * as the value of {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX} attribute.
	 */
	public static final String MULTIARY_SEPARATOR = "multialySeparator";

	/**
	 * Represents end-point tokens of multiary operators, 
	 * as the value of {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX} attribute.
	 */
	public static final String MULTIARY_END = "multialyEnd";


	/**
	 * Represents variable identifiers, 
	 * as the value of {@link AttributeKey#LEAF_TYPE LEAF_TYPE} attribute.
	 */
	public static final String VARIABLE_IDENTIFIER = "variableIdentifier";

	/**
	 * Represents function identifiers, 
	 * as the value of {@link AttributeKey#LEAF_TYPE LEAF_TYPE} attribute.
	 */
	public static final String FUNCTION_IDENTIFIER = "functionIdentifier";

	/**
	 * Represents dependency identifiers (values of import/include declarations),
	 * as the value of {@link AttributeKey#LEAF_TYPE LEAF_TYPE} attribute.
	 */
	public static final String NAMESPACE_IDENTIFIER = "dependencyIdentifier";

	/**
	 * Represents literals, 
	 * as the value of {@link AttributeKey#LEAF_TYPE LEAF_TYPE} attribute.
	 */
	public static final String LITERAL = "literal";


	/**
	 * Represents the global scppe, 
	 * as the value of {@link AttributeKey#SCOPE SCOPE} attribute.
	 * 
	 * In the Vnano, all external functions/variables provided by plug-ins belong to the global scope.
	 */
	public static final String GLOBAL = "global";

	/**
	 * Represents local scppes, 
	 * as the value of {@link AttributeKey#SCOPE SCOPE} attribute.
	 * 
	 * In the Vnano, all internal functions/variables defined in scripts belong to local scopes.
	 */
	public static final String LOCAL = "local";


	/**
	 * The splitter symbol to store multiple values 
	 * in the value of {@link AttributeKey#MODIFIER MODIFIER} attribute.
	 */
	public static final String MODIFIER_SEPARATOR = ",";


	/**
	 * Represents a marker of a partial expression, 
	 * as the value of {@link AttributeKey#LID_MARKER LID_MARKER} attribute.
	 */
	public static final String PARTIAL_EXPRESSION = "partialExpression";

	/**
	 * Represents a marker of a a block statement, 
	 * as the value of {@link AttributeKey#LID_MARKER LID_MARKER} attribute.
	 */
	public static final String BLOCK_STATEMENT = "blockStatement";
}
