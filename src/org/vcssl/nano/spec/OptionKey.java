/*
 * Copyright(C) 2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

/**
 * <span class="lang-en">The class to define keys of the option map (option names)</span>
 * <span class="lang-ja">オプションマップのキー（オプション名）が定義されたクラスです</span>
 * .
 * @author RINEARN (Fumihiro Matsui)
 */
public class OptionKey {

	/**
	 * <span class="lang-en">The locale to switch the language of error messages</span>
	 * <span class="lang-ja">エラーメッセージの言語を決めるロケールです</span>
	 * .
	 * <span class="lang-en">The value of this option is "Locale" type.</span>
	 * <span class="lang-ja">このオプションの値は Locale 型です.</span>
	 */
	public static final String LOCALE = "LOCALE";


	/**
	 * <span class="lang-en">The default/specified name of evaluated script</span>
	 * <span class="lang-ja">デフォルトまたは指定された、実行対象スクリプトの名前です</span>
	 * .
	 * <span class="lang-en">The value of this option is "String" type.</span>
	 * <span class="lang-ja">このオプションの値は "String" 型です.</span>
	 */
	public static final String EVAL_SCRIPT_NAME = "EVAL_SCRIPT_NAME";


	/**
	 * <span class="lang-en">
	 * An option to regard integer literals as float type in the execution/evaluation target (not library) script
	 * </span>
	 * <span class="lang-ja">
	 * 実行/評価対象のスクリプト（ライブラリ以外）内に出現する整数リテラルを, float型と見なして扱うオプションです
	 * </span>
	 * .
	 * <span class="lang-en">The value of this option is "Boolean" type. Specify "Boolean.TRUE" to enable this option.</span>
	 * <span class="lang-ja">このオプションの値は "boolean" 型です. 有効にするには "Boolean.TRUE" を指定してください.</span>
	 */
	public static final String EVAL_NUMBER_AS_FLOAT = "EVAL_NUMBER_AS_FLOAT";


	/**
	 * <span class="lang-en">Code of library scripts spacified by options</span>
	 * <span class="lang-ja">ライブラリスクリプトのコードです</span>
	 * .
	 * <span class="lang-en">The value of this option is "String[]" type.</span>
	 * <span class="lang-ja">このオプションの値は "String[]" 型です.</span>
	 */
	public static final String LIBRARY_SCRIPTS = "LIBRARY_SCRIPTS";


	/**
	 * <span class="lang-en">Names of library scripts spacified by options</span>
	 * <span class="lang-ja">ライブラリスクリプトの名前です</span>
	 * .
	 * <span class="lang-en">The value of this option is "String[]" type.</span>
	 * <span class="lang-ja">このオプションの値は "String[]" 型です.</span>
	 */
	public static final String LIBRARY_SCRIPT_NAMES = "LIBRARY_SCRIPT_NAMES";


	/**
	 * <span class="lang-en">
	 * An option to enable/disable {@link org.vcssl.nano.vm.accelerator.Accelerator Accelerator},
	 * which is the high-speed virtual processor implementation in the VM
	 * </span>
	 * <span class="lang-ja">
	 * VM内の高速な仮想プロセッサ実装である {@link org.vcssl.nano.vm.accelerator.Accelerator Accelerator}
	 * の有効/無効を切り替えるためのオプションです
	 * </span>
	 * .
	 * <span class="lang-en">The value of this option is "Boolean" type. Specify "Boolean.FALSE" to disable this option.</span>
	 * <span class="lang-ja">このオプションの値は "Boolean" 型です. 無効にするには "Boolean.FALSE" を指定してください.</span>
	 */
	public static final String ACCELERATOR_ENABLED = "ACCELERATOR_ENABLED";


	/**
	 * <span class="lang-en">An option to dump states and intermediate representations in the compiler, VM, etc</span>
	 * <span class="lang-ja">コンパイラやVM内などでの状態や中間表現をダンプするためのオプションです</span>
	 * .
	 * <span class="lang-en">The value of this option is "Boolean" type. Specify "Boolean.TRUE" to enable this option.</span>
	 * <span class="lang-ja">このオプションの値は "Boolean" 型です. 有効にするには "Boolean.TRUE" を指定してください.</span>
	 */
	public static final String DUMPER_ENABLED = "DUMPER_ENABLED";


	/**
	 * <span class="lang-en">Specify the target of to dump</span>
	 * <span class="lang-ja">ダンプ対象を指定します</span>
	 * .
	 * <span class="lang-en">The value of this option is "String" type, defined in {@link OptionValue}.</span>
	 * <span class="lang-ja">このオプションの値は "String" 型で, {@link OptionValue} 内に定義されています.</span>
	 */
	public static final String DUMPER_TARGET = "DUMPER_TARGET";


	/**
	 * <span class="lang-en">Specify the stream to output dumped contents</span>
	 * <span class="lang-ja">ダンプの出力に用いるストリームを指定します</span>
	 * .
	 * <span class="lang-en">The value of this option is "PrintStream" type.</span>
	 * <span class="lang-ja">このオプションの値は "PrintStream" 型です.</span>
	 */
	public static final String DUMPER_STREAM = "DUMPER_OUTPUT_STREAM";


	/**
	 * <span class="lang-en">
	 * An option to switch whether execute script or don't
	 * </span>
	 * <span class="lang-ja">
	 * スクリプトを実行するかしないかを指定するためのオプションです
	 * </span>
	 * .
	 * <span class="lang-en">The value of this option is "Boolean" type. Specify "Boolean.FALSE" to disable this option.</span>
	 * <span class="lang-ja">このオプションの値は "Boolean" 型です. 無効にするには "Boolean.FALSE" を指定してください.</span>
	 *
	 * <span class="lang-en">
	 * This option might be useful when you want to dump the compiled result for debugging but don't want to run it.
	 * </span>
	 * <span class="lang-ja">
	 * このオプションは, コンパイルした結果をデバッグ用にダンプしつつ実行はしたくない場合などに有用かもしれません.
	 * </span>
	 */
	public static final String RUNNING_ENABLED = "RUNNING_ENABLED";


	// 以下は将来的に追加するオプション項目の暫定案（未サポート）

	/*
	public static final String EVAL_CACHE_ENABLED = "EVAL_CACHE_ENABLED";
	public static final String LOOP_ENABLED = "LOOP_ENABLED";
	public static final String BRANCH_ENABLED = "BRANCH_ENABLED";
	public static final String INTERNAL_FUNCTION_ENABLED = "INTERNAL_FUNCTION_ENABLED";
	public static final String INTERNAL_SCALAR_VARIABLE_ENABLED = "INTERNAL_SCALAR_VARIABLE_ENABLED";
	public static final String INTERNAL_ARRAY_VARIABLE_ENABLED = "INTERNAL_ARRAY_VARIABLE_ENABLED";
	public static final String VECTOR_OPERATION_ENABLED = "VECTOR_OPERATION_ENABLED";
	*/
}
