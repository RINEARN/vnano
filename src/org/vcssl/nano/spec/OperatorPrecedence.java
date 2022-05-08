/*
 * Copyright(C) 2017-2021 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

// Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/spec/OperatorPrecedence.html
// ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/spec/OperatorPrecedence.html

/**
 * <p>
 * <span>
 * <span class="lang-en">
 * The class to define precedences of operators and some syntax symbols
 * </span>
 * <span class="lang-ja">
 * 演算子やいくつかの構文記号の優先度が定義されたクラスです
 * </span>
 * .
 * </p>
 *
 * <p>
 * &raquo; <a href="../../../../../src/org/vcssl/nano/spec/OperatorPrecedence.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/spec/OperatorPrecedence.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/spec/OperatorPrecedence.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class OperatorPrecedence {

	// !!! 重要 !!!        数字が小さいほど優先度が高くなります.
	// !!! Important !!!   The smaller value of the precedence makes the priority of the operator higher.


	// --------------------------------------------------
	// 最高優先度と最低優先度
	// Highest / Lowest Precedences
	// (Integer.MAX_VALUE や MIN_VALUE を使うと相対優先度がオーバーフローするので注意)
	// --------------------------------------------------

	/**
	 * <span class="lang-en">The least prior precedence</span>
	 * <span class="lang-ja">最低の優先度です</span>
	 * .
	 */
	public static final int LEAST_PRIOR = 10000000; // 最低優先度

	/**
	 * <span class="lang-en">The most prior precedence</span>
	 * <span class="lang-ja">最高の優先度です</span>
	 * .
	 */
	public static final int MOST_PRIOR = -1;        // 最高優先度

	/**
	 * <span class="lang-en">The precedence of open parenthesis: "("</span>
	 * <span class="lang-ja">開き括弧「 ( 」の優先度です</span>
	 * .
	 * <span class="lang-en">
	 * This is not the operator, but the lowest precedence is set to this symbol
	 * for the convenience in the implementation of the parser.
	 * </span>
	 * <span class="lang-ja">
	 * 演算子ではありませんが, パーサの実装の都合上, この記号には最低の優先度が設定されています.
	 * </span>
	 */
	public static final int PARENTHESIS_BEGIN = MOST_PRIOR;

	/**
	 * <span class="lang-en">The precedence of closing parenthesis: ")"</span>
	 * <span class="lang-ja">閉じ括弧「 ) 」の優先度です</span>
	 * .
	 * <span class="lang-en">
	 * This is not the operator, but the lowest precedence is set to this symbol
	 * for the convenience in the implementation of the parser.
	 * </span>
	 * <span class="lang-ja">
	 * 演算子ではありませんが, パーサの実装の都合上, この記号には最低の優先度が設定されています.
	 * </span>
	 */
	public static final int PARENTHESIS_END = LEAST_PRIOR; // MULTIARY系の演算子は先頭以外全て優先度最低


	// --------------------------------------------------
	// Multiary Operators
	// 多項演算子
	// --------------------------------------------------

	/**
	 * <span class="lang-en">The precedence of the function call operator: "("</span>
	 * <span class="lang-ja">関数呼び出し演算子「 ( 」の優先度です</span>
	 * .
	 */
	public static final int CALL_BEGIN = 1000;

	/**
	 * <span class="lang-en">The precedence of the argument-separator of the function call operator: ","</span>
	 * <span class="lang-ja">配列インデックス演算子の次元区切り「 ][ 」の優先度です</span>
	 * .
	 * <span class="lang-en">
	 * The lowest precedence is set to this symbol for the convenience in the implementation of the parser.
	 * </span>
	 * <span class="lang-ja">パーサの実装の都合上, この記号には最低の優先度が設定されています.</span>
	 */
	public static final int CALL_SEPARATOR = LEAST_PRIOR;

	/**
	 * <span class="lang-en">The precedence of the end of the function call operator: ")"</span>
	 * <span class="lang-ja">関数呼び出し演算子の終端「 ) 」の優先度です</span>
	 * .
	 * <span class="lang-en">
	 * The lowest precedence is set to this symbol for the convenience in the implementation of the parser.
	 * </span>
	 * <span class="lang-ja">パーサの実装の都合上, この記号には最低の優先度が設定されています.</span>
	 */
	public static final int CALL_END = LEAST_PRIOR;

	/**
	 * <span class="lang-en">The precedence of the subscript (array index) operator: "["</span>
	 * <span class="lang-ja">配列アクセス演算子「 [ 」の優先度です</span>
	 * .
	 */
	public static final int SUBSCRIPT_BEGIN = 1000;

	/**
	 * <span class="lang-en">The precedence of the dimension-separator of the subscript (array index) operator: "]["</span>
	 * <span class="lang-ja">配列アクセス演算子の次元区切り「 ][ 」の優先度です</span>
	 * .
	 * <span class="lang-en">
	 * The lowest precedence is set to this symbol for the convenience in the implementation of the parser.
	 * </span>
	 * <span class="lang-ja">パーサの実装の都合上, この記号には最低の優先度が設定されています.</span>
	 */
	public static final int SUBSCRIPT_SEPARATOR = LEAST_PRIOR;

	/**
	 * <span class="lang-en">The precedence of the end of the subscript (array index) operator: "]"</span>
	 * <span class="lang-ja">配列アクセス演算子の終端「 ] 」の優先度です</span>
	 * .
	 * <span class="lang-en">
	 * The lowest precedence is set to this symbol for the convenience in the implementation of the parser.
	 * </span>
	 * <span class="lang-ja">パーサの実装の都合上, この記号には最低の優先度が設定されています.</span>
	 */
	public static final int SUBSCRIPT_END = LEAST_PRIOR;

	// --------------------------------------------------
	// Postfix Operators
	// 後置演算子
	// --------------------------------------------------

	/**
	 * <span class="lang-en">The precedence of the postfix increment operator: "++"</span>
	 * <span class="lang-ja">後置インクリメント演算子「 ++ 」の優先度です</span>
	 * .
	 */
	public static final int POSTFIX_INCREMENT = 1000;

	/**
	 * <span class="lang-en">The precedence of the postfix decrement operator: "--"</span>
	 * <span class="lang-ja">後置デクリメント演算子「 -- 」の優先度です</span>
	 * .
	 */
	public static final int POSTFIX_DECREMENT = 1000;


	// --------------------------------------------------
	// Prefix Operators
	// 前置演算子
	// --------------------------------------------------

	/**
	 * <span class="lang-en">The precedence of the prefix increment operator: "++"</span>
	 * <span class="lang-ja">前置インクリメント演算子「 ++ 」の優先度です</span>
	 * .
	 */
	public static final int PREFIX_INCREMENT = 2000;

	/**
	 * <span class="lang-en">The precedence of the prefix decrement operator: "--"</span>
	 * <span class="lang-ja">前置デクリメント演算子「 -- 」の優先度です</span>
	 * .
	 */
	public static final int PREFIX_DECREMENT = 2000;

	/**
	 * <span class="lang-en">The precedence of the unary plus operator: "+"</span>
	 * <span class="lang-ja">単項プラス演算子「 + 」の優先度です</span>
	 * .
	 */
	public static final int PREFIX_PLUS = 2000;

	/**
	 * <span class="lang-en">The precedence of the unary minus operator: "-"</span>
	 * <span class="lang-ja">単項マイナス演算子「 - 」の優先度です</span>
	 * .
	 */
	public static final int PREFIX_MINUS = 2000;

	/**
	 * <span class="lang-en">The precedence of the logical-not operator: "!"</span>
	 * <span class="lang-ja">論理否定演算子「 ! 」の優先度です</span>
	 * .
	 */
	public static final int NOT = 2000;


	/**
	 * <span class="lang-en">The precedence of the cast operator: "("</span>
	 * <span class="lang-ja">キャスト演算子「 (...) 」の優先度です</span>
	 * .
	 */
	public static final int CAST_BEGIN = 2000;

	/**
	 * <span class="lang-en">The precedence of the end of the cast operator: ")"</span>
	 * <span class="lang-ja">キャスト演算子の終端「 ) 」の優先度です</span>
	 * .
	 * <span class="lang-en">
	 * The lowest precedence is set to this symbol for the convenience in the implementation of the parser.
	 * </span>
	 * <span class="lang-ja">パーサの実装の都合上, この記号には最低の優先度が設定されています.</span>
	 */
	public static final int CAST_END = LEAST_PRIOR;


	// --------------------------------------------------
	// Arithmetic Binary Operators
	// 算術二項演算子
	// --------------------------------------------------

	/**
	 * <span class="lang-en">The precedence of the multiplication operator: "*"</span>
	 * <span class="lang-ja">乗算演算子「 * 」の優先度です</span>
	 * .
	 */
	public static final int MULTIPLICATION = 3000;

	/**
	 * <span class="lang-en">The precedence of the division operator: "/"</span>
	 * <span class="lang-ja">除算演算子「 / 」の優先度です</span>
	 * .
	 */
	public static final int DIVISION = 3000;

	/**
	 * <span class="lang-en">The precedence of the remainder operator: "%"</span>
	 * <span class="lang-ja">剰余演算子「 % 」の優先度です</span>
	 * .
	 */
	public static final int REMAINDER = 3000;

	/**
	 * <span class="lang-en">The precedence of the addition operator: "+"</span>
	 * <span class="lang-ja">加算演算子「 + 」の優先度です</span>
	 * .
	 */
	public static final int ADDITION = 3100;

	/**
	 * <span class="lang-en">The precedence of the subtraction operator: "-"</span>
	 * <span class="lang-ja">減算演算子「 - 」の優先度です</span>
	 * .
	 */
	public static final int SUBTRACTION = 3100;


	// --------------------------------------------------
	// Comparison Binary Operators
	// 比較二項演算子
	// --------------------------------------------------

	/**
	 * <span class="lang-en">The precedence of the "less-than" comparison operator: "&lt;"</span>
	 * <span class="lang-ja">小なり比較演算子「 &lt; 」の優先度です</span>
	 * .
	 */
	public static final int LESS_THAN = 4000;

	/**
	 * <span class="lang-en">The precedence of the "grater-equals" comparison operator: "&lt;="</span>
	 * <span class="lang-ja">小なり等価（以下）比較演算子「 &lt; 」の優先度です</span>
	 * .
	 */
	public static final int LESS_EQUAL = 4000;

	/**
	 * <span class="lang-en">The precedence of the "greater-than" comparison operator: "&gt;"</span>
	 * <span class="lang-ja">大なり比較演算子「 &gt; 」の優先度です</span>
	 * .
	 */
	public static final int GREATER_THAN = 4000;

	/**
	 * <span class="lang-en">The precedence of the "greater-equals" comparison operator: "&gt;="</span>
	 * <span class="lang-ja">大なり等価（以上）比較演算子「 &gt; 」の優先度です</span>
	 * .
	 */
	public static final int GREATER_EQUAL = 4000;

	/**
	 * <span class="lang-en">The precedence of the equality comparison operator: "=="</span>
	 * <span class="lang-ja">等値比較演算子「 == 」の優先度です</span>
	 * .
	 */
	public static final int EQUAL = 4100;

	/**
	 * <span class="lang-en">The precedence of the "non-equality" comparison operator: "&#33;="</span>
	 * <span class="lang-ja">非等値比較演算子「 &#33;= 」の優先度です</span>
	 * .
	 */
	public static final int NOT_EQUAL = 4100;


	// --------------------------------------------------
	// Logical Binary Operators
	// 論理二項演算子
	// --------------------------------------------------

	/**
	 * <span class="lang-en">The precedence of logical-and operator: "&amp;&amp;"</span>
	 * <span class="lang-ja">論理積演算子「 &amp;&amp; 」の優先度です</span>
	 * .
	 */
	public static final int SHORT_CIRCUIT_AND = 5000;

	/**
	 * <span class="lang-en">The precedence of logical-or operator: "||"</span>
	 * <span class="lang-ja">論理和演算子「 || 」の優先度です</span>
	 * .
	 */
	public static final int SHORT_CIRCUIT_OR = 5100;


	// --------------------------------------------------
	// Assignment and Compound Assignment Operators
	// 代入演算子および複合代入演算子
	// --------------------------------------------------

	/**
	 * <span class="lang-en">The precedence of the assignment operator: "="</span>
	 * <span class="lang-ja">代入演算子「 = 」の優先度です</span>
	 * .
	 */
	public static final int ASSIGNMENT = 6000;

	/**
	 * <span class="lang-en">The precedence of the compound assignment operator of the multiplication: "*="</span>
	 * <span class="lang-ja">乗算との複合代入演算子「 *= 」の優先度です</span>
	 * .
	 */
	public static final int MULTIPLICATION_ASSIGNMENT = 6000;

	/**
	 * <span class="lang-en">The precedence of the compound assignment operator of the division: "/="</span>
	 * <span class="lang-ja">除算との複合代入演算子「 /= 」の優先度です</span>
	 * .
	 */
	public static final int DIVISION_ASSIGNMENT = 6000;

	/**
	 * <span class="lang-en">The precedence of the compound assignment operator of the remainder: "%="</span>
	 * <span class="lang-ja">剰余演算との複合代入演算子「 %= 」の優先度です</span>
	 * .
	 */
	public static final int REMAINDER_ASSIGNMENT = 6000;

	/**
	 * <span class="lang-en">The precedence of the compound assignment operator of the addition: "+="</span>
	 * <span class="lang-ja">可算との複合代入演算子「 += 」の優先度です</span>
	 * .
	 */
	public static final int ADDITION_ASSIGNMENT = 6000;

	/**
	 * <span class="lang-en">The precedence of the compound assignment operator of the subtraction: "-="</span>
	 * <span class="lang-ja">減算との複合代入演算子「 -= 」の優先度です</span>
	 * .
	 */
	public static final int SUBTRACTION_ASSIGNMENT = 6000;

}
