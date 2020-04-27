/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
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


	// 各フィールドは元々は static final でしたが、カスタマイズの事を考慮して、動的なフィールドに変更されました。
	// これにより、このクラスのインスタンスを生成して値を変更し、
	// それを LanguageSpecContainer に持たせて VnanoEngle クラスのコンストラクタに渡す事で、
	// 処理系内のソースコードを保ったまま（再ビルド不要で）定義類を差し替える事ができます。


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
	public int leastPrior = 10000000; // 最低優先度

	/**
	 * <span class="lang-en">The most prior precedence</span>
	 * <span class="lang-ja">最高の優先度です</span>
	 * .
	 */
	public int mostPrior = -1;        // 最高優先度

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
	public int parenthesisBegin = mostPrior;

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
	public int parenthesisEnd = leastPrior; // MULTIARY系の演算子は先頭以外全て優先度最低


	// --------------------------------------------------
	// Multiary Operators
	// 多項演算子
	// --------------------------------------------------

	/**
	 * <span class="lang-en">The precedence of the function call operator: "("</span>
	 * <span class="lang-ja">関数呼び出し演算子「 ( 」の優先度です</span>
	 * .
	 */
	public int callBegin = 1000;

	/**
	 * <span class="lang-en">The precedence of the argument-separator of the function call operator: ","</span>
	 * <span class="lang-ja">配列インデックス演算子の次元区切り「 ][ 」の優先度です</span>
	 * .
	 * <span class="lang-en">
	 * The lowest precedence is set to this symbol for the convenience in the implementation of the parser.
	 * </span>
	 * <span class="lang-ja">パーサの実装の都合上, この記号には最低の優先度が設定されています.</span>
	 */
	public int callSeparator = leastPrior;

	/**
	 * <span class="lang-en">The precedence of the end of the function call operator: ")"</span>
	 * <span class="lang-ja">関数呼び出し演算子の終端「 ) 」の優先度です</span>
	 * .
	 * <span class="lang-en">
	 * The lowest precedence is set to this symbol for the convenience in the implementation of the parser.
	 * </span>
	 * <span class="lang-ja">パーサの実装の都合上, この記号には最低の優先度が設定されています.</span>
	 */
	public int callEnd = leastPrior;

	/**
	 * <span class="lang-en">The precedence of the subscript (array index) operator: "["</span>
	 * <span class="lang-ja">配列アクセス演算子「 [ 」の優先度です</span>
	 * .
	 */
	public int subscriptBegin = 1000;

	/**
	 * <span class="lang-en">The precedence of the dimension-separator of the subscript (array index) operator: "]["</span>
	 * <span class="lang-ja">配列アクセス演算子の次元区切り「 ][ 」の優先度です</span>
	 * .
	 * <span class="lang-en">
	 * The lowest precedence is set to this symbol for the convenience in the implementation of the parser.
	 * </span>
	 * <span class="lang-ja">パーサの実装の都合上, この記号には最低の優先度が設定されています.</span>
	 */
	public int subscriptSeparator = leastPrior;

	/**
	 * <span class="lang-en">The precedence of the end of the subscript (array index) operator: "]"</span>
	 * <span class="lang-ja">配列アクセス演算子の終端「 ] 」の優先度です</span>
	 * .
	 * <span class="lang-en">
	 * The lowest precedence is set to this symbol for the convenience in the implementation of the parser.
	 * </span>
	 * <span class="lang-ja">パーサの実装の都合上, この記号には最低の優先度が設定されています.</span>
	 */
	public int subscriptEnd = leastPrior;

	// --------------------------------------------------
	// Postfix Operators
	// 後置演算子
	// --------------------------------------------------

	/**
	 * <span class="lang-en">The precedence of the postfix increment operator: "++"</span>
	 * <span class="lang-ja">後置インクリメント演算子「 ++ 」の優先度です</span>
	 * .
	 */
	public int postfixIncrement = 1000;

	/**
	 * <span class="lang-en">The precedence of the postfix decrement operator: "--"</span>
	 * <span class="lang-ja">後置デクリメント演算子「 -- 」の優先度です</span>
	 * .
	 */
	public int postfixDecrement = 1000;


	// --------------------------------------------------
	// Prefix Operators
	// 前置演算子
	// --------------------------------------------------

	/**
	 * <span class="lang-en">The precedence of the prefix increment operator: "++"</span>
	 * <span class="lang-ja">前置インクリメント演算子「 ++ 」の優先度です</span>
	 * .
	 */
	public int prefixIncrement = 2000;

	/**
	 * <span class="lang-en">The precedence of the prefix decrement operator: "--"</span>
	 * <span class="lang-ja">前置デクリメント演算子「 -- 」の優先度です</span>
	 * .
	 */
	public int prefixDecrement = 2000;

	/**
	 * <span class="lang-en">The precedence of the unary plus operator: "+"</span>
	 * <span class="lang-ja">単項プラス演算子「 + 」の優先度です</span>
	 * .
	 */
	public int prefixPlus = 2000;

	/**
	 * <span class="lang-en">The precedence of the unary minus operator: "-"</span>
	 * <span class="lang-ja">単項マイナス演算子「 - 」の優先度です</span>
	 * .
	 */
	public int prefixMinus = 2000;

	/**
	 * <span class="lang-en">The precedence of the logical-not operator: "!"</span>
	 * <span class="lang-ja">論理否定演算子「 ! 」の優先度です</span>
	 * .
	 */
	public int not = 2000;


	/**
	 * <span class="lang-en">The precedence of the cast operator: "("</span>
	 * <span class="lang-ja">キャスト演算子「 (...) 」の優先度です</span>
	 * .
	 */
	public int castBegin = 2000;

	/**
	 * <span class="lang-en">The precedence of the end of the cast operator: ")"</span>
	 * <span class="lang-ja">キャスト演算子の終端「 ) 」の優先度です</span>
	 * .
	 * <span class="lang-en">
	 * The lowest precedence is set to this symbol for the convenience in the implementation of the parser.
	 * </span>
	 * <span class="lang-ja">パーサの実装の都合上, この記号には最低の優先度が設定されています.</span>
	 */
	public int castEnd = leastPrior;


	// --------------------------------------------------
	// Arithmetic Binary Operators
	// 算術二項演算子
	// --------------------------------------------------

	/**
	 * <span class="lang-en">The precedence of the multiplication operator: "*"</span>
	 * <span class="lang-ja">乗算演算子「 * 」の優先度です</span>
	 * .
	 */
	public int multiplication = 3000;

	/**
	 * <span class="lang-en">The precedence of the division operator: "/"</span>
	 * <span class="lang-ja">除算演算子「 / 」の優先度です</span>
	 * .
	 */
	public int division       = 3000;

	/**
	 * <span class="lang-en">The precedence of the remainder operator: "%"</span>
	 * <span class="lang-ja">剰余演算子「 % 」の優先度です</span>
	 * .
	 */
	public int remainder      = 3000;

	/**
	 * <span class="lang-en">The precedence of the addition operator: "+"</span>
	 * <span class="lang-ja">加算演算子「 + 」の優先度です</span>
	 * .
	 */
	public int addition    = 3100;

	/**
	 * <span class="lang-en">The precedence of the subtraction operator: "-"</span>
	 * <span class="lang-ja">減算演算子「 - 」の優先度です</span>
	 * .
	 */
	public int subtraction = 3100;


	// --------------------------------------------------
	// Comparison Binary Operators
	// 比較二項演算子
	// --------------------------------------------------

	/**
	 * <span class="lang-en">The precedence of the "less-than" comparison operator: "&lt;"</span>
	 * <span class="lang-ja">小なり比較演算子「 &lt; 」の優先度です</span>
	 * .
	 */
	public int lessThan    = 4000;

	/**
	 * <span class="lang-en">The precedence of the "grater-equals" comparison operator: "&lt;="</span>
	 * <span class="lang-ja">小なり等価（以下）比較演算子「 &lt; 」の優先度です</span>
	 * .
	 */
	public int lessEqual   = 4000;

	/**
	 * <span class="lang-en">The precedence of the "greater-than" comparison operator: "&gt;"</span>
	 * <span class="lang-ja">大なり比較演算子「 &gt; 」の優先度です</span>
	 * .
	 */
	public int greaterThan  = 4000;

	/**
	 * <span class="lang-en">The precedence of the "greater-equals" comparison operator: "&gt;="</span>
	 * <span class="lang-ja">大なり等価（以上）比較演算子「 &gt; 」の優先度です</span>
	 * .
	 */
	public int greaterEqual = 4000;

	/**
	 * <span class="lang-en">The precedence of the equality comparison operator: "=="</span>
	 * <span class="lang-ja">等値比較演算子「 == 」の優先度です</span>
	 * .
	 */
	public int equal   = 4100;

	/**
	 * <span class="lang-en">The precedence of the "non-equality" comparison operator: "&#33;="</span>
	 * <span class="lang-ja">非等値比較演算子「 &#33;= 」の優先度です</span>
	 * .
	 */
	public int notEqual   = 4100;


	// --------------------------------------------------
	// Logical Binary Operators
	// 論理二項演算子
	// --------------------------------------------------

	/**
	 * <span class="lang-en">The precedence of logical-and operator: "&amp;&amp;"</span>
	 * <span class="lang-ja">論理積演算子「 &amp;&amp; 」の優先度です</span>
	 * .
	 */
	public int shortCircuitAnd = 5000;

	/**
	 * <span class="lang-en">The precedence of logical-or operator: "||"</span>
	 * <span class="lang-ja">論理和演算子「 || 」の優先度です</span>
	 * .
	 */
	public int shortCircuitOr  = 5100;


	// --------------------------------------------------
	// Assignment and Compound Assignment Operators
	// 代入演算子および複合代入演算子
	// --------------------------------------------------

	/**
	 * <span class="lang-en">The precedence of the assignment operator: "="</span>
	 * <span class="lang-ja">代入演算子「 = 」の優先度です</span>
	 * .
	 */
	public int assignment = 6000;

	/**
	 * <span class="lang-en">The precedence of the compound assignment operator of the multiplication: "*="</span>
	 * <span class="lang-ja">乗算との複合代入演算子「 *= 」の優先度です</span>
	 * .
	 */
	public int multiplicationAssignment = 6000;

	/**
	 * <span class="lang-en">The precedence of the compound assignment operator of the division: "/="</span>
	 * <span class="lang-ja">除算との複合代入演算子「 /= 」の優先度です</span>
	 * .
	 */
	public int divisionAssignment       = 6000;

	/**
	 * <span class="lang-en">The precedence of the compound assignment operator of the remainder: "%="</span>
	 * <span class="lang-ja">剰余演算との複合代入演算子「 %= 」の優先度です</span>
	 * .
	 */
	public int remainderAssignment      = 6000;

	/**
	 * <span class="lang-en">The precedence of the compound assignment operator of the addition: "+="</span>
	 * <span class="lang-ja">可算との複合代入演算子「 += 」の優先度です</span>
	 * .
	 */
	public int additionAssignment       = 6000;

	/**
	 * <span class="lang-en">The precedence of the compound assignment operator of the subtraction: "-="</span>
	 * <span class="lang-ja">減算との複合代入演算子「 -= 」の優先度です</span>
	 * .
	 */
	public int subtractionAssignment    = 6000;

}
