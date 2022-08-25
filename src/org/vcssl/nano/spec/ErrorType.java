/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

/**
 * The enum to distinguish types of errors.
 */
public enum ErrorType {
	VARIABLE_IS_NOT_FOUND,
	FUNCTION_IS_NOT_FOUND,
	STATEMENT_END_IS_NOT_FOUND,
	INVALID_EXPRESSION_SYNTAX,
	OPENING_PARENTHESES_IS_DEFICIENT,
	CLOSING_PARENTHESES_IS_DEFICIENT,
	OPENING_SUBSCRIPT_OPERATOR_IS_DEFICIENT,
	CLOSING_SUBSCRIPT_OPERATOR_IS_DEFICIENT,
	STRING_LITERAL_IS_NOT_CLOSED,
	INT_LITERAL_STARTS_WITH_ZERO,
	INVALID_DATA_TYPES_FOR_UNARY_OPERATOR,
	INVALID_DATA_TYPES_FOR_BINARY_OPERATOR,
	INVALID_RANKS_FOR_VECTOR_OPERATION,
	INVALID_COMPOUND_ASSIGNMENT_BETWEEN_SCALAR_AND_ARRAY,
	INVALID_TYPE_TOKEN_IN_EXPRESSION,
	INVALID_IDENTIFIER_TYPE,
	INVALID_IDENTIFIER_SYNTAX,
	IDENTIFIER_IS_RESERVED_WORD,
	NO_IDENTIFIER_IN_VARIABLE_DECLARATION,
	NO_DATA_TYPE_IN_VARIABLE_DECLARATION,
	PREFIX_MODIFIER_AFTER_TYPE_NAME,
	POSTFIX_MODIFIER_BEFORE_TYPE_NAME,
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
	INCOMPATIBLE_DATA_ACCESSOR_INTERFACE,
	JAGGED_ARRAY,
	ARRAY_SIZE_IS_TOO_LARGE_TO_BE_ASSIGNED_TO_SCALAR_VARIABLE,
	CAST_FAILED_DUE_TO_VALUE,
	CAST_FAILED_DUE_TO_TYPE,
	FUNCTION_IS_DECLARED_IN_INVALID_PLASE,
	INVALID_ARGUMENT_DECLARATION,
	RECURSIVE_FUNCTION_CALL,
	INVALID_EXTERNAL_FUNCTION_SIGNATURE,
	UNSUPPORTED_PLUGIN,
	PLUGIN_INITIALIZATION_FAILED,
	PLUGIN_FINALIZATION_FAILED,
	OPTION_KEY_IS_NOT_FOUND,
	OPTION_KEY_HAD_CHANGED,
	INVALID_OPTION_VALUE_TYPE,
	INVALID_OPTION_VALUE_CONTENT,
	DATA_CONVERSION_OF_FUNCTION_PLUGIN_USING_OBJECT_TYPE_SHOULD_BE_DISABLED,
	INVALID_ARRAY_INDEX,
	WRITING_TO_CONST_VARIABLE,
	WRITING_TO_LITERAL,
	WRITING_TO_NON_LVALUE,
	SUBSCRIPTING_TO_UNSUBSCRIPTABLE_SOMETHING,
	INVALID_SUBSCRIPT_RANK,
	INVALID_ARBITRARY_RANK_SYNTAX,
	FUNCTION_ENDED_WITHOUT_RETURNING_VALUE,
	RETURN_STATEMENT_IS_OUTSIDE_FUNCTIONS,
	INVALID_RETURNED_VALUE_DATA_TYPE,
	RETURNED_VALUE_IS_MISSING,
	NON_VARIABLE_IS_PASSED_BY_REFERENCE,
	VOID_RETURN_VALUE_PASSED_AS_ARGUMENT,
	DUPLICATE_VARIABLE_IDENTIFIER,
	DUPLICATE_FUNCTION_SIGNATURE,
	META_QUALIFIED_FILE_DOES_NOT_EXIST,
	META_QUALIFIED_FILE_IS_NOT_ACCESSIBLE,
	SCRIPT_FILE_DOES_NOT_EXIST,
	SCRIPT_FILE_IS_NOT_ACCESSIBLE,
	LIBRARY_LIST_FILE_DOES_NOT_EXIST,
	LIBRARY_LIST_FILE_IS_NOT_ACCESSIBLE,
	LIBRARY_IS_ALREADY_INCLUDED,
	LIBRARY_SCRIPT_NAME_IS_CONFLICTING_WITH_MAIN_SCRIPT_NAME,
	PLUGIN_LIST_FILE_DOES_NOT_EXIST,
	PLUGIN_LIST_FILE_IS_NOT_ACCESSIBLE,
	PLUGIN_DIRECTORY_IS_NOT_ACCESSIBLE,
	PLUGIN_FILE_DOES_NOT_EXIST,
	PLUGIN_INSTANTIATION_FAILED,
	PLUGIN_CONNECTION_FAILED,
	DECLARED_ENCODING_IS_UNSUPPORTED,
	NO_ENCODING_DECLARATION_END,
	ENCODING_DECLARATION_CONTAINS_INVALID_SYMBOL,
	EXTERNAL_FUNCTION_PLUGIN_CRASHED,
	EXTERNAL_VARIABLE_PLUGIN_CRASHED,
	//UNSUPPORTED_PERMISSION_NAME,
	//UNSUPPORTED_PERMISSION_VALUE,
	//PERMISSION_DENIED,
	PERMISSION_AUTHORIZER_PLUGIN_CRASHED,
	NO_PERMISSION_AUTHORIZER_IS_CONNECTED,
	MULTIPLE_PERMISSION_AUTHORIZERS_ARE_CONNECTED,
	NON_EXPRESSION_STATEMENTS_ARE_RESTRICTED,
	NON_FLOAT_DATA_TYPES_ARE_RESTRICTED,
	TERMINATOR_IS_DISABLED,
	PERFORMANCE_MONITOR_IS_DISABLED,
	UNEXPECTED_ACCELERATOR_CRASH,
	UNEXPECTED_PROCESSOR_CRASH,
	UNEXPECTED,
	UNMODIFIED,
	UNKNOWN,
}
