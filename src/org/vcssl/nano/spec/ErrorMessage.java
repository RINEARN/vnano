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
			case VARIABLE_NOT_FOUND : return "宣言されていない変数「" + words[0] + "」を使用しています";
			case FUNCTION_NOT_FOUND : return "存在しない関数「" + words[0] +  "」を呼び出しています";
			case UNKNOWN : return "不明なエラー";
			default : return "不明なエラー種類：" + errorType;
		}
	}

	public static String generateErrorMessageEnUS(ErrorType errorType, String[] words) {
		switch (errorType) {
			case VARIABLE_NOT_FOUND : return "Undeclared variable \"" + words[0] + "\" is used";
			case FUNCTION_NOT_FOUND : return "Unknown function \"" + words[0] + "\" is called";
			case UNKNOWN : return "Unknown Error";
			default : return "Unknown Error Type：" + errorType;
		}
	}
}
