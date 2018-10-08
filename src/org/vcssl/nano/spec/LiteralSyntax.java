/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.VnanoRuntimeException;

/**
 * リテラルの書式や値、判定処理、およびリテラルの解釈に必要な処理などが定義・実装されたクラスです。
 *
 * @author RINEARN (Fumihiro Matsui)
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

	/** この処理系における、文字列型リテラルの始点・終点記号です。 */
	private static final char STRING_LITERAL_QUOTATION = '"';

	/** この処理系における、文字列型リテラル内のエスケープ記号です。 */
	private static final char STRING_LITERAL_ESCAPE = '\\';


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

		int literalLength = literal.length();

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

		if (literal.charAt(0) == STRING_LITERAL_QUOTATION
				&& literal.charAt(literalLength-1) == STRING_LITERAL_QUOTATION
				&& literal.length()>=2) {
			return DataTypeName.STRING;
		}

		return DataTypeName.VOID;
	}


	/**
	 * 指定された文字列リテラル内のエスケープシーケンスを処理した文字列を返します。
	 *
	 * 引数に渡される文字列リテラルは単一である必要があります。
	 * 即ち、エスケープされていない文字列リテラルの始点・終点記号は、
	 * 引数 stringLiteral の先頭および末尾のみに存在が許されます
	 * （始点・終点記号が省略され、全く存在しなくても、処理に影響はありません）。
	 *
	 * @param stringLiteral 処理対象の文字列リテラル内容
	 * @return エスケープシーケンスが処理済みの内容
	 */
	public static String decodeEscapeSequences(String stringLiteral) {

		// 文字列リテラルのchar配列表現と要素数を取得
		char[] chars = stringLiteral.toCharArray();
		int charLength = chars.length;

		StringBuilder resultBuilder = new StringBuilder(charLength);

		boolean previousIsEscapeChar = false; // 直前がエスケープ文字だったかどうかのフラグ

		for (int i=0; i<charLength; i++) {

			// 直前がエスケープ文字だった場合は、デコードして出力に追加
			if (previousIsEscapeChar) {
				switch (chars[i]) {
					case 't' : resultBuilder.append('\t'); break;
					case 'n' : resultBuilder.append('\n'); break;
					case 'r' : resultBuilder.append('\r'); break;
					case '"' : resultBuilder.append('"'); break;
					default : throw new VnanoRuntimeException("Unknown escape sequence: \\" + chars[i]);
				}
				// エスケープ対象文字の処理が完了したら、直前エスケープフラグは階乗する
				previousIsEscapeChar = false;
				continue;
			}

			// エスケープ文字があった場合はフラグに記録し、すぐに次へ進む。
			// ただし、エスケープ文字をエスケープしている場合もあるので、
			// 直前がエスケープ文字であった場合はこの処理は飛ばす
			if (chars[i] == STRING_LITERAL_ESCAPE && !previousIsEscapeChar) {
				previousIsEscapeChar = true;
				continue;

			// 上の条件に該当しない条件下で1文字読んだら、直前エスケープフラグは解除する
			} else {
				previousIsEscapeChar = false;
			}

			// ここまで到達するのは普通の文字なので、そのまま出力に追加
			resultBuilder.append(chars[i]);
		}

		return resultBuilder.toString();
	}


	/**
	 * 指定されたコード内の文字列リテラルを全て抽出し、
	 * それらのリテラルがあった箇所を番号化リテラル（下記参照）で置き換えたコードと、
	 * 全リテラルの中身を配列にまとめて返します。
	 *
	 * 戻り値配列の [0] 番要素には、code 内の文字列リテラルを先頭から順に、
	 * "1", "2" ... などのように「 番号を文字列リテラル記号で挟んだもの（番号化リテラル） 」
	 * で置き換えたものが格納されます。
	 * その番号をインデックスとする、戻り値配列の要素に、
	 * その箇所にあった文字列リテラルの中身が格納されます。
	 *
	 * 結果の配列から元の文字列リテラル値を取り出すためのインデックスは、
	 * 番号化リテラルの文字列を引数として、
	 * {@link LiteralSyntax#getIndexOfNumberedStringLiteral getIndexOfNumberedStringLiteral}
	 * メソッドで得る事もできます。
	 *
	 * 番号は、1番から順に、出現する順序で1ずつ加算して割りふられます。
	 *
	 * @param code 文字列リテラルを抽出したいコード
	 * @param return 抽出済みコードと全リテラル内容を格納する配列
	 */
	public static String[] extractStringLiterals(String code) {

		// コードのchar配列表現と要素数を取得
		char[] chars = code.toCharArray();
		int charLength = chars.length;

		StringBuilder resultCodeBuilder = new StringBuilder(charLength);
		StringBuilder literalBuilder = null;
		List<String> literalList = new LinkedList<String>();
		int literalNumber = 1;

		boolean previousIsEscapeChar = false; // 直前がエスケープ文字だったかどうかのフラグ
		boolean inLiteral = false; // 文字列リテラル内かどうかのフラグ

		for (int i=0; i<charLength; i++) {

			// 文字列リテラルの始点・終点記号の場合に行う処理（エスケープ文字直後は行わない）
			if (chars[i] == STRING_LITERAL_QUOTATION && !previousIsEscapeChar) {

				// 文字列リテラル終了
				if (inLiteral) {
					inLiteral = false;

					// 読み進めていたリテラルを閉じて取り出す
					literalBuilder.append(STRING_LITERAL_QUOTATION);
					String literal = literalBuilder.toString();
					literalBuilder = null;

					// リテラルリストにリテラルを追加
					literalList.add(literal);

					// 抽出済みコードに番号化リテラルを追記し、番号を進める
					resultCodeBuilder.append(STRING_LITERAL_QUOTATION);
					resultCodeBuilder.append(literalNumber);
					resultCodeBuilder.append(STRING_LITERAL_QUOTATION);
					literalNumber++;

				// 文字列リテラル開始
				} else {
					inLiteral = true;

					// バッファを生成してリテラル開始記号を追記
					literalBuilder = new StringBuilder();
					literalBuilder.append(STRING_LITERAL_QUOTATION);
				}
				continue;
			}

			// リテラル内を読んでいる最中にエスケープ文字があった場合は、
			// 文字列リテラル終点判定に影響するためフラグに記録した上で、
			// リテラルに追記し、すぐに次へ進む。
			// ただし、エスケープ文字をエスケープしている場合もあるので、
			// 直前がエスケープ文字であった場合はこの処理は飛ばす
			if (inLiteral && chars[i] == STRING_LITERAL_ESCAPE && !previousIsEscapeChar) {
				literalBuilder.append(STRING_LITERAL_ESCAPE);
				previousIsEscapeChar = true;
				continue;

			// 上の条件に該当しない条件下で1文字読んだら、直前エスケープフラグは解除する
			} else {
				previousIsEscapeChar = false;
			}

			// それ以外の文字は、リテラル内の時はリテラルに、そうではない時は抽出済みコードに追記
			if (inLiteral) {
				literalBuilder.append(chars[i]);
			} else {
				resultCodeBuilder.append(chars[i]);
			}
		}

		// 以下、抽出済みコードと全リテラルを仕様通りの順で配列に詰めて返す

		String[] result = new String[literalList.size() + 1];
		result[0] = resultCodeBuilder.toString();

		int resultIndex = 1;
		Iterator<String> iterator = literalList.iterator();
		while(iterator.hasNext()) {
			result[resultIndex] = iterator.next();
			resultIndex++;
		}

		return result;
	}


	/**
	 * 指定された文字列を、{@link LiteralSyntax#extractStringLiterals extractStringLiterals}
	 * メソッドで置き換えられた番号化リテラルであると見なして、
	 * 同メソッドの戻り値配列から元の文字列リテラル値を取り出すためのインデックスを求めて返します。
	 *
	 * @param numberedLiteral
	 * @return
	 */
	public static int getIndexOfNumberedStringLiteral(String numberedLiteral) {

		// 前後の「 " 」を除去
		numberedLiteral = numberedLiteral.substring(1, numberedLiteral.length()-1);

		// 番号を整数に変換
		int index = -1;
		try {
			index = Integer.parseInt(numberedLiteral);
		} catch (NumberFormatException e) {
			// 番号部が整数と解釈できないのは extractStringLiterals メソッドの十道の異常
			throw new VnanoFatalException("Invalid numbered string literal: " + numberedLiteral, e);
		}
		return index;
	}

}
