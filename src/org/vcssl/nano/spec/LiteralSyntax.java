/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

/**
 * リテラルの書式や値、判定処理などが定義されたクラスです。
 *
 * @author 松井文宏 - Fumihiro Matsui
 */
public class LiteralSyntax {

	/** この処理系における、論理型のtrueを表すリテラル値です。 */
	public static final String TRUE = "true";

	/** この処理系における、論理型のfalseを表すリテラル値です。 */
	public static final String FALSE = "false";


	/** この処理系における、整数型リテラルの正規表現です。 */
	private static final String INT_LITERAL_REGEX = "^(\\+|-)?[0-9]+$";

	/** この処理系における、浮動小数点数型リテラルの正規表現です。 */
	private static final String FLOAT_LITERAL_REGEX = 
		"^(\\+|-)?([0-9]+(d|f|D|F))|((([0-9]+\\.[0-9]*)|([0-9]*\\.[0-9]+))(((e|E)(\\+|-)?[0-9]+)|)|([0-9]*\\.?[0-9]+)(e|E)(\\+|-)?[0-9]+)(d|f|D|F)?$";

	/** この処理系における、論理型リテラルの正規表現です。 */
	private static final String BOOL_LITERAL_REGEX = "^" + TRUE + "|" + FALSE + "$";

	/** この処理系における、文字列型リテラルの始点記号です。 */
	private static final String STRING_LITERAL_BEGIN = "\"";

	/** この処理系における、文字列型リテラルの終点記号です。 */
	private static final String STRING_LITERAL_END = "\"";


	/**
	 * 指定されたトークン（字句）の値が、
	 * この処理系におけるリテラルとみなせるかどうかを判定します。
	 *
	 * @param token 判定対象のトークン（字句）の値
	 * @return 判定結果（リテラルとみなせる場合に true ）
	 */
	public static boolean isValidLiteral(String token) {
		String dataTypeName = getDataTypeNameOfLiteral(token);
		return !dataTypeName.equals(DataTypeName.VOID);
	}


	/**
	 * 指定されたリテラルの記述内容から、データ型を判定し、
	 * その名称（{@link ScriptWord Word} クラスにおいて定義）を返します。
	 *
	 * 引数に指定するリテラルは、事前に
	 * {@link LiteralSyntax#isValidLiteral isValidLiteral} メソッドを用いて、
	 * リテラルとしてい有効な記述内容である事が確認されている必要があります。
	 *
	 * 無効な記述内容が指定された場合、このメソッドは
	 * 便宜的に {@link DataTypeName#VOID Word.VOID} を返しますが、これは
	 * {@link LiteralSyntax#isValidLiteral isValidLiteral} メソッドの実装を簡略化するためであり、
	 * この仕様に依存すべきではありません。
	 *
	 * @param literal リテラルの記述内容
	 * @return データ型の名称
	 */
	public static String getDataTypeNameOfLiteral(String literal) {

		if (literal.matches(INT_LITERAL_REGEX)) {
			return DataTypeName.INT;

			// もし整数リテラルも浮動小数点数として認識させたい場合、
			// 以下のコメントのようにFLOAT64 を返すよう変更して下さい。
			//return Word.FLOAT64;
		}

		if (literal.matches(FLOAT_LITERAL_REGEX)) {
			return DataTypeName.FLOAT;
		}

		if (literal.matches(BOOL_LITERAL_REGEX)) {
			return DataTypeName.BOOL;
		}

		if (literal.startsWith(STRING_LITERAL_BEGIN) && literal.endsWith(STRING_LITERAL_END) && literal.length()>=2) {
			return DataTypeName.STRING;
		}

		return DataTypeName.VOID;
	}

}
