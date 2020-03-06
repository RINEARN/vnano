/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import java.util.Locale;

// Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/spec/ErrorMessage.html
// ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/spec/ErrorMessage.html

/**
 * <p>
 * <span>
 * <span class="lang-en">
 * The class to define error messages of the script engine of the Vnano
 * </span>
 * <span class="lang-ja">
 * Vnano のスクリプトエンジンのエラーメッセージが定義されたクラスです
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
			case VARIABLE_IS_NOT_FOUND : return "宣言されていない変数「 " + words[0] + " 」を使用しています。";
			case FUNCTION_IS_NOT_FOUND : return "存在しない関数「 " + words[0] +  " 」を呼び出しています。";
			case STATEMENT_END_IS_NOT_FOUND : return "文の終端がありません（「 ; 」が必要です）。";
			case OPENING_PARENTHESES_IS_DEFICIENT : return "開き括弧「 ( 」が不足しています。";
			case CLOSING_PARENTHESES_IS_DEFICIENT : return "閉じ括弧「 ) 」が不足しています。";
			case INVALID_DATA_TYPES_FOR_UNARY_OPERATOR : return "前置・後置演算子の「 " + words[0] + " 」は、" + words[1] + "型の値や変数に対しては使用できません。";
			case INVALID_DATA_TYPES_FOR_BINARY_OPERATOR : return "二項演算子の「 " + words[0] + " 」は、" + words[1] + "型と" + words[2] + "型の値や変数の組み合わせに対しては使用できません。";
			case INVALID_TYPE_TOKEN_IN_EXPRESSION : return "単語「 " + words[0] + " 」は、式の中では使用できません。";
			case STRING_LITERAL_IS_NOT_CLOSED : return "閉じていない文字列リテラル \"...\" が存在します。";
			case NO_IDENTIFIER_IN_VARIABLE_DECLARATION : return "変数の宣言では、データ型名の後に変数名が必要です。";
			case TOO_MANY_TOKENS_FOR_VARIABLE_DECLARATION : return "変数宣言文の後半に、余分な記述が存在します。";
			case NO_PARTIAL_EXPRESSION : return "式の中に空の括弧 ( ) があります。";
			case OPERAND_IS_MISSING_AT_RIGHT : return "「 " + words[0] + " 」の右側に、値や変数などが必要です。";
			case OPERAND_IS_MISSING_AT_LEFT : return "「 " + words[0] + " 」の左側に、値や変数などが必要です。";
			case OPERATOR_IS_MISSING_AT_RIGHT : return "「 " + words[0] + " 」と「 " + words[1] + " 」の間に演算子（「 + 」などの記号）が必要です。";
			case OPERATOR_IS_MISSING_AT_LEFT : return "「 " + words[0] + " 」と「 " + words[1] + " 」の間に演算子（「 + 」などの記号）が必要です。";
			case DATA_TYPE_IS_MISSING_AT_RIGHT : return "「 " + words[0] + " 」の右側に、データ型が必要です。";
			case CLOSE_PARENTHESIS_IS_MISSING_AT_RIGHT : return "「 " + words[0] + " 」の右側に、閉じ括弧「 ) 」が必要です。";
			case NO_OPEN_PARENTHESIS_OF_CONTROL_STATEMENT : return "「 " + words[0] + " 」の後には括弧 (...) が必要です。";
			case NO_CLOSING_PARENTHESIS_OF_CONTROL_STATEMENT : return words[0] + " 文の括弧 (...) が閉じていません。";
			case NO_CONDITION_EXPRESSION_OF_IF_STATEMENT : return "if 文の括弧 (...) の中に条件の記述がありません。";
			case NO_CONDITION_EXPRESSION_OF_WHILE_STATEMENT : return "while 文の括弧 (...) の中に条件の記述がありません。";
			case ELEMENTS_OF_FOR_STATEMENT_IS_DEFICIENT : return "for 文の括弧 (...) 内は、「 ( 変数宣言や初期化 ; 繰り返し条件 ; 繰り返し毎の更新処理 ) 」のように記述する必要があります。";
			case TOO_MANY_TOKENS_FOR_CONTROL_STATEMENT : return words[0] + " 文の末尾に、余分な記述が存在します。";
			case NO_BLOCK_AFTER_CONTROL_STATEMENT : return "この言語では、" + words[0] + " 文には常にブロック {...} が必要です。";
			case UNKNOWN_DATA_TYPE : return "存在しないデータ型「 " + words[0] + " 」を使用しています。";
			case INVALID_IMMEDIATE_VALUE : return "解釈できないリテラルまたは即値「 " + words[0] + " 」が記述されています。";
			case UNCONVERTIBLE_DATA_TYPE : return "外部のデータ型「 " + words[0] + " 」は、このスクリプトエンジンがサポートしているデータ型に変換できません。" ;
			case UNCONVERTIBLE_ARRAY : return "外部の配列型「 " + words[0] + " 」は、次元数またはデータ型などの問題で、このスクリプトエンジンでは扱えません。" ;
			case UNCONVERTIBLE_INTERNAL_ARRAY : return "スクリプト内の配列型「 " + words[0] + " 」は、次元数またはデータ型などの問題で、スクリプトエンジン外部のデータ型に変換できません。" ;
			case JAGGED_ARRAY : return "長さが異なる配列をまとめた配列、いわゆるジャグ配列は、このスクリプトエンジンでは扱えません。";
			case CAST_FAILED_DUE_TO_VALUE : return "データ「 " + words[0] + " 」の「 " + words[1] + " 」型への変換に失敗しました。";
			case CAST_FAILED_DUE_TO_TYPE : return words[0] + "型のデータの「 " + words[1] + " 」型への変換に失敗しました。";
			case FUNCTION_IS_DECLARED_IN_INVALID_PLASE : return "関数をここで宣言する事はできません。";
			case INVALID_ARGUMENT_DECLARATION : return "引数の宣言内容が正しくありません。";
			case RECURSIVE_FUNCTION_CALL : return "関数の再帰呼び出しが検出されましたが、このスクリプトエンジンではサポートされていません。";
			case INVALID_EXTERNAL_FUNCTION_SIGNATURE : return "外部関数の接続時の表記「 " + words[0] + " 」が正しくありません。正しい表記は「 " + words[1] + " 」か、そこから関数名のみを変更したものです。";
			case UNSUPPORTED_PLUGIN : return "この処理系では、「 " + words[0] + " 」型のオブジェクトをプラグインとして接続する事はサポートされていません。";
			case PLUGIN_NITIALIZATION_FAILED : return "プラグイン「 " + words[0] + " 」の初期化に失敗しました。";
			case OPTION_KEY_IS_NOT_FOUND : return "オプション「 " + words[0] + " 」の値が指定されていません。";
			case INVALID_OPTION_VALUE_TYPE : return "オプション「 " + words[0] + " 」の値は「 " + words[1] + " 」型で指定する必要があります。";
			case INVALID_OPTION_VALUE_CONTENT : return "オプション「 " + words[0] + " 」の値「 " + words[1] + " 」が、正しい内容ではありません。";
			case DATA_CONVERSION_OF_FUNCTION_PLUGIN_USING_OBJECT_TYPE_SHOULD_BE_DISABLED : return "外部関数「 " + words[0] + " 」のプラグインは、Object型の引数または戻り値を持つため、データ変換機能が無効に設定されていなければなりません。";
			case INVALID_ARRAY_INDEX : return "配列のアクセス可能範囲 [ 0 から " + words[1] + " まで ] の外を指すインデックス [ " + words[0] + " ] が指定されました。";
			case UNEXPECTED_ACCELERATOR_CRASH : return "予期しないVMエラー (命令アドレス: " + words[0] + ", 再配置後命令アドレス: " + words[1] + ")";
			case UNEXPECTED_PROCESSOR_CRASH : return "予期しないVMエラー（命令アドレス: " + words[0] + ")";
			case UNEXPECTED : return "予期しないエラー";
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
			case INVALID_DATA_TYPES_FOR_UNARY_OPERATOR : return "Prefix/postfix operator \"" + words[0] + "\" is not available for " + words[1] + "-type operands";
			case INVALID_DATA_TYPES_FOR_BINARY_OPERATOR : return "Binary operator \"" + words[0] + "\" is not available for the combination of " + words[0] + "-type and " + words[1] + "-type data";
			case INVALID_TYPE_TOKEN_IN_EXPRESSION : return "Token \"" + words[0] + "\" is not available in expressions";
			case STRING_LITERAL_IS_NOT_CLOSED : return "Unclosed string literal \"...\" exists in code";
			case NO_IDENTIFIER_IN_VARIABLE_DECLARATION : return "Variable name is required after the data type name for variable declarations";
			case TOO_MANY_TOKENS_FOR_VARIABLE_DECLARATION : return "Unexpected description exists in the latter part of the variable declaration statement";
			case NO_PARTIAL_EXPRESSION : return "Empty parentheses ( ) exist in the expression";
			case OPERAND_IS_MISSING_AT_RIGHT : return "A value or variable is necessary at the right of \"" + words[0] + "\"";
			case OPERAND_IS_MISSING_AT_LEFT : return "A value or variable is necessary at the left of \"" + words[0] + "\"";
			case OPERATOR_IS_MISSING_AT_RIGHT : return "An operator (e.g. \"+\") is required between \"" + words[0] + "\" and \"" + words[1] + "\"";
			case OPERATOR_IS_MISSING_AT_LEFT : return "An operator (e.g. \"+\") is required between \"" + words[0] + "\" and \"" + words[1] + "\"";
			case DATA_TYPE_IS_MISSING_AT_RIGHT : return "A data type is necessary at the right of \"" + words[0] + "\"";
			case CLOSE_PARENTHESIS_IS_MISSING_AT_RIGHT : return "A close parenthesis \")\" is necessary at the right of \"" + words[0] + "\"";
			case NO_OPEN_PARENTHESIS_OF_CONTROL_STATEMENT : return "Parentheses (...) are required after \"" + words[0] + "\"";
			case NO_CLOSING_PARENTHESIS_OF_CONTROL_STATEMENT : return "Parentheses (...) of \"" + words[0] + "\" statement are not closing";
			case NO_CONDITION_EXPRESSION_OF_IF_STATEMENT : return "A condition expression is required between parentheses (...) of the \"if\" statement";
			case NO_CONDITION_EXPRESSION_OF_WHILE_STATEMENT : return "A condition expression is required between parentheses (...) of the \"while\" statement";
			case ELEMENTS_OF_FOR_STATEMENT_IS_DEFICIENT : return "Elements such as \"( initialization ; loop_condition ; updating_per_loops )\" are required between parentheses (...) of the \"for\" statement";
			case TOO_MANY_TOKENS_FOR_CONTROL_STATEMENT : return "Unexpected description exists at the tail of the \"" + words[0] + "\" statement";
			case NO_BLOCK_AFTER_CONTROL_STATEMENT : return "A block {...} is always necessary for the " + words[0] + "statement in this language" ;
			case UNKNOWN_DATA_TYPE : return "Unknown data type \"" + words[0] + "\" is used";
			case INVALID_IMMEDIATE_VALUE : return "Invalid immediate value or literal \"" + words[0] + "\" is described";
			case UNCONVERTIBLE_DATA_TYPE : return "External data type \"" + words[0] + "\" is not convertible to supported data types in this script engine" ;
			case UNCONVERTIBLE_ARRAY : return "External array type \"" + words[0] + "\" is not convertible to supported array in this script engine due to the number of dimensions or the type-convertibility" ;
			case UNCONVERTIBLE_INTERNAL_ARRAY : return "Internal array type \"" + words[0] + "\" is not convertible to external array types due to the number of dimensions or the type-convertibility" ;
			case JAGGED_ARRAY : return "Jagged array (the array having arrays as elements and array-lengths of elements are different each other) is not available for this script engine";
			case CAST_FAILED_DUE_TO_VALUE : return "Cast operation of the data \"" + words[0] + "\" to \"" + words[1] + "\" type has failed";
			case CAST_FAILED_DUE_TO_TYPE : return "Cast operation from \"" + words[0] + "\" type to \"" + words[1] + "\" type has failed";
			case FUNCTION_IS_DECLARED_IN_INVALID_PLASE : return "A function is declared in the invalid place";
			case INVALID_ARGUMENT_DECLARATION : return "Invalid argument declaration is detected";
			case RECURSIVE_FUNCTION_CALL : return "A recursive call of the function (this script engine is not support it) is detected";
			case INVALID_EXTERNAL_FUNCTION_SIGNATURE : return "A signature \"" + words[0] + "\" of the connected external function is invalid. A valid example is \"" + words[1] + "\", and you can change the function name only";
			case UNSUPPORTED_PLUGIN : return "For this script engine, the class \"" + words[0] + "\" is not supported as a plug-in";
			case PLUGIN_NITIALIZATION_FAILED : return "Plug-in \"" + words[0] + "\" could not be initialized";
			case OPTION_KEY_IS_NOT_FOUND : return "The value of \"" + words[0] + "\" option is not found";
			case INVALID_OPTION_VALUE_TYPE : return "The type of the value of \"" + words[0] + "\" option should be \"" + words[1];
			case INVALID_OPTION_VALUE_CONTENT : return "The value of \"" + words[0] + "\" option \"" + words[1] + "\" is invalid";
			case DATA_CONVERSION_OF_FUNCTION_PLUGIN_USING_OBJECT_TYPE_SHOULD_BE_DISABLED : return "The data-conversion of the plugin of the external function\"" + words[0] + "\" should be disabled, because this function has Object-type parameters or the return value.";
			case INVALID_ARRAY_INDEX : return "The array element with the index [ " + words[0] + " ] is accessed, but it is out of the available range [ from 0 to " + words[1] + " ]";
			case UNEXPECTED_ACCELERATOR_CRASH : return "Unexpected VM Error (instruction-addr: " + words[0] + ", reordered-instruction-addr: " + words[1] + ")";
			case UNEXPECTED_PROCESSOR_CRASH : return "Unexpected VM Error (instruction-addr: " + words[0] + ")";
			case UNEXPECTED : return "Unexpected Error";
			case UNKNOWN : return "Unknown Error";
			default : return "Unknown Error Type：" + errorType;
		}
	}
}
