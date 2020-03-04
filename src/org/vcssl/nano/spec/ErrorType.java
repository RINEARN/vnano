/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

// Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/spec/ErrorType.html
// ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/spec/ErrorType.html

/**
 * <p>
 * <span>
 * <span class="lang-en">
 * The enum to define types of errors of the script engine of the Vnano
 * </span>
 * <span class="lang-ja">
 * Vnano のスクリプトエンジンのエラーの種類が定義された列挙子です
 * </span>
 * .
 * </p>
 *
 * <p>
 * &raquo; <a href="../../../../../src/org/vcssl/nano/spec/ErrorMessage.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/spec/ErrorMessage.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/spec/ErrorMessage.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public enum ErrorType {
	VARIABLE_IS_NOT_FOUND,
	FUNCTION_IS_NOT_FOUND,
	STATEMENT_END_IS_NOT_FOUND,
	INVALID_CONTROLL_STATEMENT_TYPE,
	OPENING_PARENTHESES_IS_DEFICIENT,
	CLOSING_PARENTHESES_IS_DEFICIENT,
	STRING_LITERAL_IS_NOT_CLOSED,
	INVALID_DATA_TYPES_FOR_UNARY_OPERATOR,
	INVALID_DATA_TYPES_FOR_BINARY_OPERATOR,
	INVALID_TYPE_TOKEN_IN_EXPRESSION,
	NO_IDENTIFIER_IN_VARIABLE_DECLARATION,
	TOO_MANY_TOKENS_FOR_VARIABLE_DECLARATION,
	NO_PARTIAL_EXPRESSION,
	OPERAND_IS_MISSING_AT_RIGHT,
	OPERAND_IS_MISSING_AT_LEFT,
	OPERATOR_IS_MISSING_AT_RIGHT,
	OPERATOR_IS_MISSING_AT_LEFT,
	DATA_TYPE_IS_MISSING_AT_RIGHT,
	CLOSE_PARENTHESIS_IS_MISSING_AT_RIGHT,
	NO_OPEN_PARENTHESIS_OF_CONTROL_STATEMENT,
	NO_CLOSING_PARENTHESIS_OF_CONTROL_STATEMENT,
	NO_CONDITION_EXPRESSION_OF_IF_STATEMENT,
	NO_CONDITION_EXPRESSION_OF_WHILE_STATEMENT,
	ELEMENTS_OF_FOR_STATEMENT_IS_DEFICIENT,
	TOO_MANY_TOKENS_FOR_CONTROL_STATEMENT,
	NO_BLOCK_AFTER_CONTROL_STATEMENT,
	UNKNOWN_DATA_TYPE,
	INVALID_IMMEDIATE_VALUE,
	UNCONVERTIBLE_DATA_TYPE,
	UNCONVERTIBLE_ARRAY,
	UNCONVERTIBLE_INTERNAL_ARRAY,
	JAGGED_ARRAY,
	CAST_FAILED_DUE_TO_VALUE,
	CAST_FAILED_DUE_TO_TYPE,
	FUNCTION_IS_DECLARED_IN_INVALID_PLASE,
	INVALID_ARGUMENT_DECLARATION,
	RECURSIVE_FUNCTION_CALL,
	INVALID_EXTERNAL_FUNCTION_SIGNATURE,
	UNSUPPORTED_PLUGIN,
	PLUGIN_NITIALIZATION_FAILED,
	OPTION_KEY_IS_NOT_FOUND,
	INVALID_OPTION_VALUE_TYPE,
	INVALID_OPTION_VALUE_CONTENT,
	DATA_CONVERSION_OF_FUNCTION_PLUGIN_USING_OBJECT_TYPE_SHOULD_BE_DISABLED,
	ACCELERATOR_CRASHED,
	UNEXPECTED,
	UNKNOWN,
}
