/*
 * Copyright(C) 2017-2021 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import java.util.List;
import java.util.ArrayList;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.VnanoException;

// Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/spec/LiteralSyntax.html
// ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/spec/LiteralSyntax.html

/**
 * <p>
 * <span>
 * <span class="lang-en">
 * The class performing functions to interpret literals in script code of the Vnano
 * </span>
 * <span class="lang-ja">
 * Vnano のスクリプトコード内のリテラルを解釈する機能を提供するクラスです
 * </span>
 * .
 * </p>
 *
 * <p>
 * &raquo; <a href="../../../../../src/org/vcssl/nano/spec/LiteralSyntax.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/spec/LiteralSyntax.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/spec/LiteralSyntax.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class LiteralSyntax {

	/**
	 * <span class="lang-en">The value of the bool type literal: "true"</span>
	 * <span class="lang-ja">bool 型のリテラル値 true です</span>
	 * .
	 */
	public static final String TRUE = "true";


	/**
	 * <span class="lang-en">The value of the bool type literal: "false"</span>
	 * <span class="lang-ja">bool 型のリテラル値 false です</span>
	 * .
	 */
	public static final String FALSE = "false";


	/**
	 * <span class="lang-en">The prefix of hexadecimal int literals</span>
	 * <span class="lang-ja">16 進数の int 型リテラルのプレフィックスです</span>
	 * .
	 */
	public static final String INT_LITERAL_HEX_PREFIX = "0x";


	/**
	 * <span class="lang-en">The prefix of octal int literals</span>
	 * <span class="lang-ja">8 進数の int 型リテラルのプレフィックスです</span>
	 * .
	 */
	public static final String INT_LITERAL_OCT_PREFIX = "0o";


	/**
	 * <span class="lang-en">The prefix of binary int literals</span>
	 * <span class="lang-ja">2 進数の int 型リテラルのプレフィックスです</span>
	 * .
	 */
	public static final String INT_LITERAL_BIN_PREFIX = "0b";


	/**
	 * <span class="lang-en">The regular expression of the prefix of an exponent part of "float" type literals</span>
	 * <span class="lang-ja">float 型リテラルにおける指数部のプレフィックスの正規表現です</span>
	 * .
	 */
	public static final String FLOAT_LITERAL_EXPONENT_PREFIX = "e|E";


	/**
	 * <span class="lang-en">The regular expression of "int" type literals</span>
	 * <span class="lang-ja">int 型リテラルの正規表現です</span>
	 * .
	 */
	protected static final String INT_LITERAL_REGEX =
			// 始点
			"^"
			// 注: 符号は単項マイナス/プラス演算子と解釈するため、リテラルには含まない
			+
			// 16進リテラルの場合： 16進数プレフィックス 0x の後に1個以上の「0～9までの数字またはA～Fまでのアルファベット」の列
			"(0x[0-9a-zA-F]+)"
			+
			// または
			"|"
			+
			// 8進数リテラルの場合： 8進数プレフィックス 0o の後に1個以上の「0～7までの数字」の列
			"(0o[0-7]+)"
			+
			// または
			"|"
			+
			// 2進数リテラルの場合： 2進数プレフィックス 0b の後に1個以上の「0～1までの数字」の列
			"(0b[0-1]+)"
			+
			// または
			"|"
			+
			// 10進数リテラルの場合(一般的な場合)： プレフィックス無しで「0～9までの数字」の列
			"([0-9]+)"
			+
			// 最後に long 型のサフィックス（有無は任意）
			"(l|L)?"
			+
			// 終端
			"$";


	/**
	 * <span class="lang-en">The regular expression of "float" type literals</span>
	 * <span class="lang-ja">float 型リテラルの正規表現です</span>
	 * .
	 */
	protected static final String FLOAT_LITERAL_REGEX =
			// 始点
			"^"
			// 注: 符号は単項マイナス/プラス演算子と解釈するため、リテラルには含まない
			+
			// 数字列のみの後に float/double 型のサフィックスが付く場合（この場合サフィックスは、無いとintリテラルになってしまうので必須）
			"([0-9]+(d|f|D|F))"
			+
			// または
			"|"
			+
			// それ以外の一般的な場合
			"("
				+
				//「 1個以上の数字列 . 0個以上の数字列 」または「 0個以上の数字列 . 1個以上の数字列 」
				"(([0-9]+\\.[0-9]*)|([0-9]*\\.[0-9]+))"
				+
				// その後に指数部（あっても無くてもいい）
				"((e|E)(\\+|-)?[0-9]+)?"
				+
				// その後に float/double 型のサフィックス（この場合サフィックスの有無は任意）
				"(d|f|D|F)?"
				+
			")"
			+
			// 終端
			"$";


	/**
	 * <span class="lang-en">The regular expression of "bool" type literals</span>
	 * <span class="lang-ja">bool 型リテラルの正規表現です</span>
	 * .
	 */
	protected static final String BOOL_LITERAL_REGEX = "^" + TRUE + "|" + FALSE + "$";


	/**
	 * <span class="lang-en">The prefix character of escape sequences in string literals: \</span>
	 * <span class="lang-ja">文字列型リテラル内のエスケープシーケンスのプレフィックス記号「 \ 」です</span>
	 * .
	 */
	private static final char STEING_LITERAL_ESCAPE = '\\';


	/**
	 * <span class="lang-en">The beginning/end character of string-literals: "</span>
	 * <span class="lang-ja">文字列型リテラルの始点・終点記号「 " 」です</span>
	 * .
	 */
	public static final char STRING_LITERAL_QUOT = '"';


	/**
	 * <span class="lang-en">The beginning/end character of char-literals (unsupported): '</span>
	 * <span class="lang-ja">文字型リテラル（非サポート）の始点・終点記号「 ' 」です</span>
	 * .
	 */
	public static final char CHAR_LITERAL_QUOT = '\'';


	/**
	 * <span class="lang-en">Checks whether the specified token can be interpreted as the literal or not</span>
	 * <span class="lang-ja">指定された字句が, リテラルとして解釈できるかどうかを判定します</span>
	 * .
	 * @param token
	 *   <span class="lang-en">The token to be checked.</span>
	 *   <span class="lang-en">判定対象の字句.</span>
	 *
	 * @return
	 *   <span class="lang-en">The check result ("true" if it can be interpreted as the literal).</span>
	 *   <span class="lang-ja">判定結果（リテラルとみなせる場合に true ）.</span>
	 */
	public static final boolean isValidLiteral(String token) {
		try {
			getDataTypeNameOfLiteral(token);
			return true;
		} catch (VnanoFatalException e) {
			return false;
		}
	}


	/**
	 * <span class="lang-en">Determines the data type of the specified literal and returns its name</span>
	 * <span class="lang-ja">指定されたリテラルのデータ型を判定し, その型の名称を返します</span>
	 * .
	 * @param literal
	 *   <span class="lang-en">The literal for which get the name of the data type.</span>
	 *   <span class="lang-ja">データ型の名称を取得したいリテラル.</span>
	 *
	 * @return
	 *   <span class="lang-en">The name of the data type of the literal.</span>
	 *   <span class="lang-ja">リテラルのデータ型の名称.</span>
	 *
	 * @throws VnanoFatalException
	 *   <span class="lang-en">Thrown when the specified literal could not be interpreted.</span>
	 *   <span class="lang-ja">指定されたリテラルを解釈できなかった場合にスローされます.</span>
	 */
	public static final String getDataTypeNameOfLiteral(String literal) throws VnanoFatalException {

		int literalLength = literal.length();

		if (literal.matches(INT_LITERAL_REGEX)) {
			return DataTypeName.DEFAULT_INT;
		}

		if (literal.matches(FLOAT_LITERAL_REGEX)) {
			return DataTypeName.DEFAULT_FLOAT;
		}

		if (literal.matches(BOOL_LITERAL_REGEX)) {
			return DataTypeName.BOOL;
		}

		if (literal.charAt(0) == STRING_LITERAL_QUOT
				&& literal.charAt(literalLength-1) == STRING_LITERAL_QUOT
				&& literal.length()>=2) {
			return DataTypeName.STRING;
		}

		throw new VnanoFatalException("Invalid literal: " + literal);
	}


	/**
	 * <span class="lang-ja">指定された文字列リテラル内のエスケープシーケンスを処理した文字列を返します</span>
	 * .
	 * <span class="lang-ja">
	 * 引数に渡される文字列リテラルは単一である必要があります.
	 * 即ち、エスケープされていない文字列リテラルの始点・終点記号は,
	 * 引数 stringLiteral の先頭および末尾のみに存在が許されます（省略可能です）.
	 * </span>
	 *
	 * @param stringLiteral
	 *   <span class="lang-ja">処理対象の文字列リテラル内容</span>
	 *
	 * @return
	 *   <span class="lang-ja">エスケープシーケンスが処理済みの内容</span>
	 */
	public static final String decodeEscapeSequences(String stringLiteral) {

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
					default : throw new VnanoFatalException("Unknown escape sequence: " + chars[i]);
				}
				// エスケープ対象文字の処理が完了したら、直前エスケープフラグは階乗する
				previousIsEscapeChar = false;
				continue;
			}

			// エスケープ文字があった場合はフラグに記録し、すぐに次へ進む。
			// ただし、エスケープ文字をエスケープしている場合もあるので、
			// 直前がエスケープ文字であった場合はこの処理は飛ばす
			if (chars[i] == STEING_LITERAL_ESCAPE && !previousIsEscapeChar) {
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
	 * <span class="lang-ja">
	 * 指定されたコード内の文字列リテラルを全て抽出し,
	 * それらのリテラルがあった箇所を番号化リテラル（後述）で置き換えたコードと,
	 * 全リテラルの中身を配列にまとめて返します
	 * <span>
	 * .
	 * <span class="lang-ja">
	 * 戻り値配列の [0] 番要素には、code 内の文字列リテラルを先頭から順に,
	 * "1", "2" ... などのように「 番号を文字列リテラル記号で挟んだもの（番号化リテラル） 」
	 * で置き換えたものが格納されます.
	 * その番号をインデックスとする、戻り値配列の要素に,
	 * その箇所にあった文字列リテラルの中身が格納されます.
	 *
	 * 結果の配列から元の文字列リテラル値を取り出すためのインデックスは,
	 * 番号化リテラルの文字列を引数として,
	 * {@link LiteralSyntax#getIndexOfNumberedStringLiteral getIndexOfNumberedStringLiteral}
	 * メソッドで得る事もできます.
	 *
	 * 番号は、1番から順に、出現する順序で1ずつ加算して割りふられます.
	 * <span>
	 *
	 * @param code
	 *   <span class="lang-ja">文字列リテラルを抽出したいコード.</span>
	 *
	 * @return
	 *   <span class="lang-ja">抽出済みコードと全リテラル内容を格納する配列.<span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-ja">文字列リテラルが全て閉じていない場合にスローされます.</span>
	 */
	public static final String[] extractStringLiterals(String code) throws VnanoException {

		// コードのchar配列表現と要素数を取得
		char[] chars = code.toCharArray();
		int charLength = chars.length;

		StringBuilder resultCodeBuilder = new StringBuilder(charLength);
		StringBuilder literalBuilder = null;
		List<String> literalList = new ArrayList<String>();
		int literalNumber = 1;

		boolean previousIsEscapeChar = false; // 直前がエスケープ文字だったかどうかのフラグ
		boolean inLiteral = false; // 文字列リテラル内かどうかのフラグ

		for (int i=0; i<charLength; i++) {

			// 文字列リテラルの始点・終点記号の場合に行う処理（エスケープ文字直後は行わない）
			if (chars[i] == STRING_LITERAL_QUOT && !previousIsEscapeChar) {

				// 文字列リテラル終了
				if (inLiteral) {
					inLiteral = false;

					// 読み進めていたリテラルを閉じて取り出す
					literalBuilder.append(STRING_LITERAL_QUOT);
					String literal = literalBuilder.toString();
					literalBuilder = null;

					// リテラルリストにリテラルを追加
					literalList.add(literal);

					// 抽出済みコードに番号化リテラルを追記し、番号を進める
					resultCodeBuilder.append(STRING_LITERAL_QUOT);
					resultCodeBuilder.append(literalNumber);
					resultCodeBuilder.append(STRING_LITERAL_QUOT);
					literalNumber++;

				// 文字列リテラル開始
				} else {
					inLiteral = true;

					// バッファを生成してリテラル開始記号を追記
					literalBuilder = new StringBuilder();
					literalBuilder.append(STRING_LITERAL_QUOT);
				}
				continue;
			}

			// リテラル内を読んでいる最中にエスケープ文字があった場合は、
			// 文字列リテラル終点判定に影響するためフラグに記録した上で、
			// リテラルに追記し、すぐに次へ進む。
			// ただし、エスケープ文字をエスケープしている場合もあるので、
			// 直前がエスケープ文字であった場合はこの処理は飛ばす
			if (inLiteral && chars[i] == STEING_LITERAL_ESCAPE && !previousIsEscapeChar) {
				literalBuilder.append(STEING_LITERAL_ESCAPE);
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

		// コードを全て読み終えても文字列リテラル内なら、閉じていない文字列リテラルがある
		if (inLiteral) {
			throw new VnanoException(
					ErrorType.STRING_LITERAL_IS_NOT_CLOSED
			);
		}

		// 以下、抽出済みコードと全リテラルを仕様通りの順で配列に詰めて返す

		String[] result = new String[literalList.size() + 1];
		result[0] = resultCodeBuilder.toString();

		int resultIndex = 1;
		for (String literal: literalList) {
			result[resultIndex] = literal;
			resultIndex++;
		}

		return result;
	}


	/**
	 * <span class="lang-ja">
	 * 指定された文字列を, {@link LiteralSyntax#extractStringLiterals extractStringLiterals}
	 * メソッドで置き換えられた番号化リテラルであると見なして,
	 * 同メソッドの戻り値配列から元の文字列リテラル値を取り出して復元するためのインデックスを求めて返します
	 * </span>
	 * .
	 * @param numberedLiteral
	 *   <span class="lang-ja">復元したい番号化リテラル</span>
	 *
	 * @return
	 *   <span class="lang-ja">
	 *   {@link LiteralSyntax#extractStringLiterals extractStringLiterals} メソッドの戻り値の中で,
	 *   対象リテラルが格納されている要素のインデックス
	 *   </span>
	 */
	public static final int getIndexOfNumberedStringLiteral(String numberedLiteral) {

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
