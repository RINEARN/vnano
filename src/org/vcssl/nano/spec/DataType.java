/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

// Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/spec/DataType.html
// ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/spec/DataType.html

/**
 * <p>
 * <span>
 * <span class="lang-en">
 * The enum to define data types of the Vnano
 * </span>
 * <span class="lang-ja">
 * Vnano のデータ型が定義された列挙子です
 * </span>
 * .
 * <span class="lang-en">
 * Nemes of data types are defined in {@link DataTypeName DataTypeName} class,
 * and conversion methods between elements of this enum and names of data types
 * are provided by the class.
 * </span>
 *
 * <span class="lang-ja">
 * データ型の名称は {@link DataTypeName DataTypeName} クラスで定義され,
 * この列挙子の要素と, データ型名との間の変換メソッドも, 同クラスによって提供されます.
 * </span>
 * </p>
 *
 * <p>
 * &raquo; <a href="../../../../../src/org/vcssl/nano/spec/DataType.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/spec/DataType.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/spec/DataType.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public enum DataType {

	/**
	 * <span class="lang-en">The 64-bit signed integer type (name in scripts: "int")</span>
	 * <span class="lang-ja">64ビット精度の符号付き整数型（ 型名は int ）です</span>
	 * .
	 */
	INT64,


	/**
	 * <span class="lang-en">The 64-bit floating-point number type (name: "float")</span>
	 * <span class="lang-ja">64ビット精度の浮動小数点数型（ 型名は float ）です</span>
	 * .
	 */
	FLOAT64,


	/**
	 * <span class="lang-en">The boolean type (name: "bool")</span>
	 * <span class="lang-ja">論理型/真偽型（ 型名は bool ）です</span>
	 * .
	 */
	BOOL,


	/**
	 * <span class="lang-en">The character string type (name: "string")</span>
	 * <span class="lang-ja">文字列型（ 型名は string ）です</span>
	 * .
	 */
	STRING,


	/**
	 * <span class="lang-en">The special type to represent that any type is available for the argument, the return value, and so on  (name: "any")</span>
	 * <span class="lang-ja">関数の引数や戻り値などが、任意の型であり得る事を示す、特別な型（ 型名は any ）です</span>
	 * .
	 */
	ANY,


	/**
	 * <span class="lang-en">The void type (name: "void")</span>
	 * <span class="lang-ja">void 型（ 型名は void ）です</span>
	 * .
	 */
	VOID;

}
