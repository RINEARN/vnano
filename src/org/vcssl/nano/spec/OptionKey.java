/*
 * Copyright(C) 2019-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

// Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/spec/OptionKey.html
// ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/spec/OptionKey.html

/**
 * <p>
 * <span class="lang-en">The class to define keys of the option map (option names)</span>
 * <span class="lang-ja">オプションマップのキー（オプション名）が定義されたクラスです</span>
 * .
 * </p>
 *
 * <p>
 * &raquo; <a href="../../../../../src/org/vcssl/nano/spec/OptionKey.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/spec/OptionKey.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/spec/OptionKey.html">All</a> |
 * </p>
 *
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
	 * <span class="lang-en">The default/specified name of the execution target script</span>
	 * <span class="lang-ja">デフォルトまたは指定された、実行対象スクリプトの名前です</span>
	 * .
	 * <span class="lang-en">The value of this option is "String" type.</span>
	 * <span class="lang-ja">このオプションの値は "String" 型です.</span>
	 */
	public static final String MAIN_SCRIPT_NAME = "MAIN_SCRIPT_NAME";


	/**
	 * <span class="lang-en">The path of the directory in which the execution target script is locating</span>
	 * <span class="lang-ja">実行対象スクリプトがあるディレクトリのパスです</span>
	 * .
	 * <span class="lang-en">The value of this option is "String" type.</span>
	 * <span class="lang-ja">このオプションの値は "String" 型です.</span>
	 */
	public static final String MAIN_SCRIPT_DIRECTORY = "MAIN_SCRIPT_DIRECTORY";


	/**
	 * <span class="lang-en">
	 * An option to regard integer literals as float type
	 * in the execution/evaluation target expressions and scripts (excepting library scripts)
	 * </span>
	 * <span class="lang-ja">
	 * 実行/評価対象の式やスクリプト（ライブラリ以外）内に出現する整数リテラルを, float型と見なして扱うオプションです
	 * </span>
	 * .
	 * <span class="lang-en">The value of this option is "Boolean" type. Specify "Boolean.TRUE" to enable this option.</span>
	 * <span class="lang-ja">このオプションの値は "boolean" 型です. 有効にするには "Boolean.TRUE" を指定してください.</span>
	 */
	public static final String EVAL_INT_LITERAL_AS_FLOAT = "EVAL_INT_LITERAL_AS_FLOAT";


	/**
	 * <span class="lang-en">
	 * An option to restrict types of available statements
	 * in the execution target scripts (excepting library scripts) to only "expression"
	 * </span>
	 * <span class="lang-ja">
	 * 実行対象のスクリプト（ライブラリ以外）内で使用可能な文を, 式文のみに制限するオプションです
	 * </span>
	 * .
	 * <span class="lang-en">The value of this option is "Boolean" type. Specify "Boolean.TRUE" to enable this option.</span>
	 * <span class="lang-ja">このオプションの値は "boolean" 型です. 有効にするには "Boolean.TRUE" を指定してください.</span>
	 */
	public static final String EVAL_ONLY_EXPRESSION = "EVAL_ONLY_EXPRESSION";


	/**
	 * <span class="lang-en">
	 * An option to restrict available data types of operators/operands in
	 * in the execution target scripts (excepting library scripts) to only "float"
	 * </span>
	 * <span class="lang-ja">
	 * 実行対象のスクリプト（ライブラリ以外）内で使用可能な演算子やオペランドの型を, float 型のみに制限するオプションです
	 * </span>
	 * .
	 * <span class="lang-en">The value of this option is "Boolean" type. Specify "Boolean.TRUE" to enable this option.</span>
	 * <span class="lang-ja">このオプションの値は "boolean" 型です. 有効にするには "Boolean.TRUE" を指定してください.</span>
	 */
	public static final String EVAL_ONLY_FLOAT = "EVAL_ONLY_FLOAT";


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


	/**
	 * <span class="lang-en">Specify the mode of UI for inputting/outputting values and so on</span>
	 * <span class="lang-ja">値の入出力などに用いるユーザーインターフェースの形式を指定します</span>
	 * .
	 * <span class="lang-en">
	 * The value of this option is "String" type. Specify "GUI" or "CUI".
	 * The default value is "GUI", but it will be set to "CUI" automatically when you execute the Vnano engine in the command-line mode.
	 * </span>
	 * <span class="lang-ja">
	 * このオプションの値は "String" 型です. "GUI" か "CUI" を選択してください.
	 * デフォルト値は "GUI" ですが, コマンドラインモードでVnanoエンジンを起動した場合は自動的に "CUI" に設定されます.
	 * </span>
	 *
	 * <span class="lang-en">This option is referred by I/O plug-ins if they are connected.</span>
	 * <span class="lang-ja">このオプションは, 入出力系のプラグインが接続されている場合に, それらによって参照されます.</span>
	 */
	public static final String UI_MODE = "UI_MODE";


	/**
	 * <span class="lang-en">Specify the default line-feed code on the environment</span>
	 * <span class="lang-ja">環境における, デフォルトの改行コードを指定します</span>
	 * .
	 * <span class="lang-en">The value of this option is "String" type.</span>
	 * <span class="lang-ja">このオプションの値は "String" 型です.</span>
	 *
	 * <span class="lang-en">This option is referred by plug-ins providing environment-dependent values, if they are connected.</span>
	 * <span class="lang-ja">このオプションは, 環境依存の値を提供するプラグインが接続されている場合に, それらによって参照されます.</span>
	 */
	public static final String ENVIRONMENT_EOL = "ENVIRONMENT_EOL";


	/**
	 * <span class="lang-en">Specify the default line-feed code for file I/O</span>
	 * <span class="lang-ja">ファイルの入出力に用いる, デフォルトの改行コードを指定します</span>
	 * .
	 * <span class="lang-en">The value of this option is "String" type.</span>
	 * <span class="lang-ja">このオプションの値は "String" 型です.</span>
	 *
	 * <span class="lang-en">This option is referred by I/O plug-ins if they are connected.</span>
	 * <span class="lang-ja">このオプションは, 入出力系のプラグインが接続されている場合に, それらによって参照されます.</span>
	 */
	public static final String FILE_IO_EOL = "FILE_IO_EOL";


	/**
	 * <span class="lang-en">Specify the default line-feed code for terminal I/O</span>
	 * <span class="lang-ja">端末との入出力に用いる, デフォルトの改行コードを指定します</span>
	 * .
	 * <span class="lang-en">The value of this option is "String" type.</span>
	 * <span class="lang-ja">このオプションの値は "String" 型です.</span>
	 *
	 * <span class="lang-en">This option is referred by I/O plug-ins if they are connected.</span>
	 * <span class="lang-ja">このオプションは, 入出力系のプラグインが接続されている場合に, それらによって参照されます.</span>
	 */
	public static final String TERMINAL_IO_EOL = "TERMINAL_IO_EOL";


	/**
	 * <span class="lang-en">Specify the name of the default encoding for reading reading script files</span>
	 * <span class="lang-ja">スクリプトファイルの読み込みに用いるデフォルトの文字コード名を指定します</span>
	 * .
	 * <span class="lang-en">The value of this option is "String" type.</span>
	 * <span class="lang-ja">このオプションの値は "String" 型です.</span>
	 */
	//public static final String SCRIPT_FILE_ENCODING = "SCRIPT_FILE_ENCODING"; // 文字コード宣言を既にサポートしているので重要度が低いため保留、将来的にサポート検討


	/**
	 * <span class="lang-en">Specify the name of the default encoding for writing to / reading files in scripts</span>
	 * <span class="lang-ja">スクリプト内でのファイルの読み書きに用いるデフォルトの文字コード名を指定します</span>
	 * .
	 * <span class="lang-en">The value of this option is "String" type.</span>
	 * <span class="lang-ja">このオプションの値は "String" 型です.</span>
	 *
	 * <span class="lang-en">This option is referred by I/O plug-ins if they are connected.</span>
	 * <span class="lang-ja">このオプションは, 入出力系のプラグインが接続されている場合に, それらによって参照されます.</span>
	 */
	public static final String FILE_IO_ENCODING = "FILE_IO_ENCODING";


	/**
	 * <span class="lang-en">
	 * Specify the stream for standard input used when {@link OptionKey#TERMINAL_IO_UI TERMINAL_IO_UI} is set to "CUI"
	 * </span>
	 * <span class="lang-ja">
	 * {@link OptionKey#TERMINAL_IO_UI TERMINAL_IO_UI} が "CUI" に設定されている際に、標準入力に用いるストリームを指定します
	 * </span>
	 * .
	 * <span class="lang-en">The value of this option is "InputStream" type.</span>
	 * <span class="lang-ja">このオプションの値は "InputStream" 型です.</span>
	 *
	 * <span class="lang-en">This option is referred by I/O plug-ins if they are connected.</span>
	 * <span class="lang-ja">このオプションは, 入出力系のプラグインが接続されている場合に, それらによって参照されます.</span>
	 */
	public static final String STDIN_STREAM = "STDIN_STREAM";


	/**
	 * <span class="lang-en">
	 * Specify the stream for standard output used when {@link OptionKey#TERMINAL_IO_UI TERMINAL_IO_UI} is set to "CUI"
	 * </span>
	 * <span class="lang-ja">
	 * {@link OptionKey#TERMINAL_IO_UI TERMINAL_IO_UI} が "CUI" に設定されている際に、標準出力に用いるストリームを指定します
	 * </span>
	 * .
	 * <span class="lang-en">The value of this option is "PrintStream" type.</span>
	 * <span class="lang-ja">このオプションの値は "PrintStream" 型です.</span>
	 *
	 * <span class="lang-en">This option is referred by I/O plug-ins if they are connected.</span>
	 * <span class="lang-ja">このオプションは, 入出力系のプラグインが接続されている場合に, それらによって参照されます.</span>
	 */
	public static final String STDOUT_STREAM = "STDOUT_STREAM";


	/**
	 * <span class="lang-en">
	 * Specify the stream for standard error output when {@link OptionKey#TERMINAL_IO_UI TERMINAL_IO_UI} is set to "CUI"
	 * </span>
	 * <span class="lang-ja">
	 * {@link OptionKey#TERMINAL_IO_UI TERMINAL_IO_UI} が "CUI" に設定されている際に、標準エラー出力に用いるストリームを指定します
	 * </span>
	 * .
	 * <span class="lang-en">The value of this option is "PrintStream" type.</span>
	 * <span class="lang-ja">このオプションの値は "PrintStream" 型です.</span>
	 *
	 * <span class="lang-en">This option is referred by I/O plug-ins if they are connected.</span>
	 * <span class="lang-ja">このオプションは, 入出力系のプラグインが接続されている場合に, それらによって参照されます.</span>
	 */
	public static final String STDERR_STREAM = "STDERR_STREAM";


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
