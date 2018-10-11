/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import java.util.Locale;

public class ErrorMessage {

	public static String generateErrorMessage(ErrorType errorType) {
		return generateErrorMessage(errorType, null, Locale.getDefault());
	}

	public static String generateErrorMessage(ErrorType errorType, String[] words) {
		return generateErrorMessage(errorType, words, Locale.getDefault());
	}

	public static String generateErrorMessage(ErrorType errorType, String[] words, Locale locale) {

		if (   ( locale.getLanguage()!=null && locale.getLanguage().equals("ja") )
			   || ( locale.getCountry()!=null && locale.getCountry().equals("JP") )   ) {

			return generateErrorMessageJaJP(errorType, words);
		} else {
			return generateErrorMessageEnUS(errorType, words);
		}
	}

	public static String generateErrorMessageJaJP(ErrorType errorType, String[] words) {
		switch (errorType) {
			case VARIABLE_IS_NOT_FOUND : return "宣言されていない変数「 " + words[0] + " 」を使用しています";
			case FUNCTION_IS_NOT_FOUND : return "存在しない関数「 " + words[0] +  " 」を呼び出しています";
			case STATEMENT_END_IS_NOT_FOUND : return "文の終端がありません（「 ; 」が必要です）";
			case OPENING_PARENTHESES_IS_DEFICIENT : return "開き括弧「 ( 」が不足しています。";
			case CLOSING_PARENTHESES_IS_DEFICIENT : return "閉じ括弧「 ) 」が不足しています。";
			case INVALID_DATA_TYPES_FOR_UNARY_OPERATOR : return "単項演算子の「 " + words[0] + " 」は、" + words[1] + "型の値や変数に対しては使用できません。";
			case INVALID_DATA_TYPES_FOR_BINARY_OPERATOR : return "二項算子の「 " + words[0] + " 」は、" + words[1] + "型と" + words[2] + "型の値や変数の組み合わせに対しては使用できません。";
			case INVALID_TYPE_TOKEN_IN_EXPRESSION : return "単語「 " + words[0] + " 」は、式の中では使用できません";
			case STRING_LITERAL_IS_NOT_CLOSED : return "閉じていない文字列リテラル \"...\" が存在します";
			case NO_IDENTIFIER_IN_VARIABLE_DECLARATION : return "変数の宣言では、データ型名の後に変数名が必要です";
			case TOO_MANY_TOKENS_FOR_VARIABLE_DECLARATION : return "変数宣言文の後半に、余分な記述が存在します";
			case NO_PARTIAL_EXPRESSION : return "式の中に空の括弧 ( ) があります";
			case OPERAND_IS_MISSING_AT_RIGHT : return "「 " + words[0] + " 」の右側に、値や変数などが必要です";
			case OPERAND_IS_MISSING_AT_LEFT : return "「 " + words[0] + " 」の左側に、値や変数などが必要です";
			case NO_PARENTHESIS_OF_IF_STATEMENT : return "「 if 」の後には括弧 (...) を付けて、その中に条件を記述する必要があります";
			case NO_PARENTHESIS_OF_WHILE_STATEMENT : return "「 while 」の後には括弧 (...) を付けて、その中に条件を記述する必要があります";
			case NO_PARENTHESIS_OF_FOR_STATEMENT : return "「 for 」の後には括弧 (...) を付けて、「 ( 変数宣言や初期化 ; 繰り返し条件 ; 繰り返し毎の更新処理 ) 」のように記述する必要があります";
			case NO_CONDITION_EXPRESSION_OF_IF_STATEMENT : return "if 文の括弧 (...) の中に条件の記述がありません";
			case NO_CONDITION_EXPRESSION_OF_WHILE_STATEMENT : return "while 文の括弧 (...) の中に条件の記述がありません";
			case ELEMENTS_OF_FOR_STATEMENT_IS_DEFICIENT : return "for 文の括弧 (...) 内は、「 ( 変数宣言や初期化 ; 繰り返し条件 ; 繰り返し毎の更新処理 ) 」のように記述する必要があります";
			case TOO_MANY_TOKENS_FOR_CONTROL_STATEMENT : return words[0] + " 文の末尾に、余分な記述が存在します";
			case UNKNOWN : return "不明なエラー";
			default : return "不明なエラー種類：" + errorType;
		}
	}

	public static String generateErrorMessageEnUS(ErrorType errorType, String[] words) {
		switch (errorType) {
			case VARIABLE_IS_NOT_FOUND : return "Undeclared variable \"" + words[0] + "\" is used";
			case FUNCTION_IS_NOT_FOUND : return "Unknown function \"" + words[0] + "\" is called";
			case STATEMENT_END_IS_NOT_FOUND : return "End-point of the statement is not found (\";\" is required)";
			case OPENING_PARENTHESES_IS_DEFICIENT : return "Opening parenthesis \"(\" is deficient";
			case CLOSING_PARENTHESES_IS_DEFICIENT : return "Closing parenthesis \")\" is deficient";
			case INVALID_DATA_TYPES_FOR_UNARY_OPERATOR : return "Unary operator \"" + words[0] + "\" is not available for " + words[1] + "-type operands";
			case INVALID_DATA_TYPES_FOR_BINARY_OPERATOR : return "Binary operator \"" + words[0] + "\" is not available for the combination of " + words[0] + "-type and " + words[1] + "-type data";
			case INVALID_TYPE_TOKEN_IN_EXPRESSION : return "Token \"" + words[0] + "\" is not available in expressions";
			case STRING_LITERAL_IS_NOT_CLOSED : return "Unclosed string literal \"...\" exists in code";
			case NO_IDENTIFIER_IN_VARIABLE_DECLARATION : return "Variable name is required after the data type name for variable declarations";
			case TOO_MANY_TOKENS_FOR_VARIABLE_DECLARATION : return "Unexpected description exists in the latter part of the variable declaration statement";
			case NO_PARTIAL_EXPRESSION : return "Blank parentheses ( ) exists in the expression";
			case OPERAND_IS_MISSING_AT_RIGHT : return "Value or variable is necessary at the right of \"" + words[0] + "\"";
			case OPERAND_IS_MISSING_AT_LEFT : return "Value or variable is necessary at the left of \"" + words[0] + "\"";
			case NO_PARENTHESIS_OF_IF_STATEMENT : return "Parentheses (...) containing a condition expression are required after \"if\"";
			case NO_PARENTHESIS_OF_WHILE_STATEMENT : return "Parentheses (...) containing a condition expression are required after \"while\"";
			case NO_PARENTHESIS_OF_FOR_STATEMENT : return "Parentheses (...) containing elements such as \"( initialization ; loop_condition ; updating_per_loops )\" is required after \"for\"";
			case NO_CONDITION_EXPRESSION_OF_IF_STATEMENT : return "A condition expression is required between parentheses (...) of the \"if\" statement";
			case NO_CONDITION_EXPRESSION_OF_WHILE_STATEMENT : return "A condition expression is required between parentheses (...) of the \"while\" statement";
			case ELEMENTS_OF_FOR_STATEMENT_IS_DEFICIENT : return "Elements such as \"( initialization ; loop_condition ; updating_per_loops )\" are required between parentheses (...) of the \"for\" statement";
			case TOO_MANY_TOKENS_FOR_CONTROL_STATEMENT : return "Unexpected description exists at the tail of the \"" + words[0] + "\" statement";
			case UNKNOWN : return "Unknown Error";
			default : return "Unknown Error Type：" + errorType;
		}
	}
}
