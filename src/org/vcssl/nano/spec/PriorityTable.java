/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;


/**
 * 演算子の優先順位が定義されたクラスです。
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class PriorityTable {

	// （重要）数字が小さいほど優先度が高くなります。


	// 最高優先度と最低優先度
	// (Integer.MAX_VALUE や MIN_VALUE を使うと相対優先度がオーバーフローするので注意)

	/** あらゆる演算子の優先度よりも高い、最高優先度です。 */
	public static final int LEAST_PRIOR = 10000000; // 最低優先度

	/** あらゆる演算子の優先度よりも低い、最低優先度です。 */
	public static final int MOST_PRIOR = -1;        // 最高優先度

	/** 式中における開き括弧の優先度です。非演算子ですが、式の構文解析対象トークンであるため、優先度を持っています。 */
	public static final int PARENTHESIS_BEGIN = MOST_PRIOR;

	/** 式中における閉じ括弧の優先度です。非演算子ですが、式の構文解析対象トークンであるため、優先度を持っています。 */
	public static final int PARENTHESIS_END = LEAST_PRIOR; // MULTIARY系の演算子は先頭以外全て優先度最低

	// 後置演算子

	/** 関数呼び出し演算子の先頭の優先度（＝関数呼び出し演算子の優先度）です。 */
	public static final int CALL_BEGIN = 1000;

	/** 関数呼び出し演算子の終端の優先度です。 */
	public static final int CALL_SEPARATOR = LEAST_PRIOR; // MULTIARY系の演算子は先頭以外全て優先度最低

	/** 関数呼び出し演算子の引数区切りの優先度です。 */
	public static final int CALL_END = LEAST_PRIOR; // MULTIARY系の演算子は先頭以外全て優先度最低

	/** 配列要素アクセス演算子の先頭の優先度（＝配列要素アクセス演算子の優先度）です。 */
	public static final int INDEX_BEGIN = 1000;

	/** 配列要素アクセス演算子の次元区切りの優先度です。 */
	public static final int INDEX_SEPARATOR = LEAST_PRIOR; // MULTIARY系の演算子は先頭以外全て優先度最低

	/** 配列要素アクセス演算子の終端の優先度です。 */
	public static final int INDEX_END = LEAST_PRIOR; // MULTIARY系の演算子は先頭以外全て優先度最低

	/** 後置インクリメント演算子（++）の優先度です。 */
	public static final int POSTFIX_INCREMENT = 1000;

	/** 後置デクリメント演算子（--）の優先度です。 */
	public static final int POSTFIX_DECREMENT = 1000;


	// 前置演算子

	/** 前置インクリメント演算子（++）の優先度です。 */
	public static final int PREFIX_INCREMENT = 2000;

	/** 前置デクリメント演算子（--）の優先度です。 */
	public static final int PREFIX_DECREMENT = 2000;

	/** 単項プラス演算子（+）の優先度です。 */
	public static final int PREFIX_PLUS = 2000;

	/** 単項マイナス演算子（-）の優先度です。 */
	public static final int PREFIX_MINUS = 2000;

	/** 論理否定演算子（!）の優先度です。 */
	public static final int NOT = 2000;


	// 算術二項演算子

	/** 乗算演算子（*）の優先度です。 */
	public static final int MULTIPLICATION = 3000;

	/** 除算演算子（/）の優先度です。 */
	public static final int DIVISION       = 3000;

	/** 剰余演算子（%）の優先度です。 */
	public static final int REMAINDER      = 3000;

	/** 加算演算子（+）の優先度です。 */
	public static final int ADDITION    = 3100;

	/** 減算演算子（-）の優先度です。 */
	public static final int SUBTRACTION = 3100;


	// 比較二項演算子

	/** 小なり演算子（&lt;）の優先度です。 */
	public static final int LESS_THAN    = 4000;

	/** 小なり等価演算子（&lt;=）の優先度です。 */
	public static final int LESS_EQUAL   = 4000;

	/** 大なり演算子（&gt;）の優先度です。 */
	public static final int GRATER_THAN  = 4000;

	/** 大なり等価演算子（&gt;=）の優先度です。 */
	public static final int GRATER_EQUAL = 4000;

	/** 等値演算子の優先度（==）です。 */
	public static final int EQUAL   = 4100;

	/** 非等値演算子（!=）の優先度です。 */
	public static final int NOT_EQUAL   = 4100;


	// 論理二項演算子

	/** 論理積演算子（&amp;&amp;）の優先度です。 */
	public static final int AND = 5000;

	/** 論理和演算子（||）の優先度です。 */
	public static final int OR  = 5100;


	// 代入演算子および複合代入演算子

	/** 代入演算子（=）の優先度です。 */
	public static final int ASSIGNMENT = 6000;

	/** 乗算代入演算子（*=）の優先度です。 */
	public static final int MULTIPLICATION_ASSIGNMENT = 6000;

	/** 除算代入演算子（/=）の優先度です。 */
	public static final int DIVISION_ASSIGNMENT       = 6000;

	/** 剰余代入演算子（%=）の優先度です。 */
	public static final int REMAINDER_ASSIGNMENT      = 6000;

	/** 加算代入演算子（+=）の優先度です。 */
	public static final int ADDITION_ASSIGNMENT       = 6000;

	/** 減算代入演算子（/=）の優先度です。 */
	public static final int SUBTRACTION_ASSIGNMENT    = 6000;

}
