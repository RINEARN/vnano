/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

public enum ErrorType {
	VARIABLE_IS_NOT_FOUND,
	FUNCTION_IS_NOT_FOUND,
	STATEMENT_END_IS_NOT_FOUND,
	INVALID_CONTROLL_STATEMENT_TYPE,
	OPENING_PARENTHESES_IS_DEFICIENT,
	CLOSING_PARENTHESES_IS_DEFICIENT,
	UNKNOWN,
}
