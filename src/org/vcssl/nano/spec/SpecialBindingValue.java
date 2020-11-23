/*
 * Copyright(C) 2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

// Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/spec/SpecialBindingValue.html
// ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/spec/SpecialBindingValue.html

/**
 * <p>
 * <span>
 * <span class="lang-en">
 * The class to define special binding values
 * </span>
 * <span class="lang-ja">
 * 特別な意味を持つバインディングの値が定義されたクラスです
 * </span>
 * .
 * </p>
 *
 * <p>
 * &raquo; <a href="../../../../../src/org/vcssl/nano/spec/SpecialBindingValue.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/spec/SpecialBindingValue.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/spec/SpecialBindingValue.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class SpecialBindingValue {

	public static final String COMMAND_REMOVE_PLUGIN = "REMOVE_PLUGIN";
	public static final String COMMAND_REMOVE_LIBRARY = "REMOVE_LIBRARY";
	public static final String COMMAND_RELOAD_PLUGIN = "RELOAD_PLUGIN";
	public static final String COMMAND_RELOAD_LIBRARY = "RELOAD_LIBRARY";


	// 各要素のコメント、具体的な内容を書くべきか、ラップしているメソッドを参照すべきか...
	// このクラスのコメントをちゃんと整備するタイミングで要検討。以下はとりあえず参照だけしている

	/**
	 * <span class="lang-en">
	 * When specified with "___VNANO_COMMAND" key, calls {@link org.vcssl.nano.VnanoEngine#terminateScript() } method
	 * </span>
	 * <span class="lang-ja">
	 * キー "___VNANO_COMMAND" への値に指定すると, {@link org.vcssl.nano.VnanoEngine#terminateScript() } を実行します
	 * </span>
	 */
	public static final String COMMAND_TERMINATE_SCRIPT = "TERMINATE_SCRIPT";


	/**
	 * <span class="lang-en">
	 * When specified with "___VNANO_COMMAND" key, calls {@link org.vcssl.nano.VnanoEngine#resetTerminator() } method
	 * </span>
	 * <span class="lang-ja">
	 * キー "___VNANO_COMMAND" への値に指定すると, {@link org.vcssl.nano.VnanoEngine#resetTerminator() } を実行します
	 * </span>
	 */
	public static final String COMMAND_SESET_TERMINATOR = "RESET_TERMINATOR";
}
