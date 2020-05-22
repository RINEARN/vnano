/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.vcssl.nano.interconnect.PluginLoader;
import org.vcssl.nano.interconnect.ScriptLoader;
import org.vcssl.nano.spec.EngineInformation;
import org.vcssl.nano.spec.LanguageSpecContainer;
import org.vcssl.nano.spec.SpecialBindingKey;
import org.vcssl.nano.spec.SpecialBindingValue;

//Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/VnanoScriptEngine.html
//ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/VnanoScriptEngine.html

/**
 * <span class="lang-en">
 * The wrapper class of {@link VnanoEngine VnanoEngine} class to use it
 * through "ScriptEngine" interface of the Scripting API of the standard library
 * </span>
 * <span class="lang-ja">
 * {@link VnanoEngine VnanoEngine} クラスを, 標準ライブラリの Scripting API における
 * ScriptEngine インターフェースを介して用いるためのラッパークラスです
 * </span>
 * .
 *
 * <p>
 * &raquo; <a href="../../../../src/org/vcssl/nano/VnanoScriptEngine.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../api/org/vcssl/nano/VnanoScriptEngine.html">Public Only</a>
 * | <a href="../../../../api-all/org/vcssl/nano/VnanoScriptEngine.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class VnanoScriptEngine implements ScriptEngine {

	private final LanguageSpecContainer LANG_SPEC;
	private static final String DEFAULT_ENCODING = "UTF-8";


	/**
	 * <span class="lang-en">A Vnano Engine to be wrapped by ScriptEngine interface</span>
	 * <span class="lang-ja">ScriptEngine インターフェースでラップする対象の Vnano エンジンです</span>
	 * .
	 */
	private VnanoEngine vnanoEngine = null;


	/**
	 * <span class="lang-en">A loader for loading library scripts from files</span>
	 * <span class="lang-ja">ファイルからライブラリスクリプトを読み込むためのローダーです</span>
	 * .
	 */
	private ScriptLoader libraryScriptLoader = null;


	/**
	 * <span class="lang-en">A loader for loading plug-ins from files</span>
	 * <span class="lang-ja">ファイルからプラグインを読み込むためのローダーです</span>
	 * .
	 */
	private PluginLoader pluginLoader = null;


	/**
	 * <span class="lang-en">Stores plug-ins registerd by "put" method to be connected</span>
	 * <span class="lang-ja">put メソッドによって接続登録されたプラグインを保持します</span>
	 * .
	 */
	private Bindings putPluginBindings = null;


	/**
	 * <span class="lang-en">
	 * Stores whether plug-ins are added by "put" method after the previous execution or not
	 * </span>
	 * <span class="lang-ja">
	 * 前回の実行後に、プラグインが put メソッドによって追加されたかどうかを保持します
	 * </span>
	 */
	private boolean putPluginBindingsUpdated = false;


	/**
	 * <span class="lang-en">
	 * Stores whether plug-ins are added/removed by re-loadings after the previous execution or not
	 * </span>
	 * <span class="lang-ja">
	 * 前回の実行後に、プラグインが再読み込みによって追加/削除されたかどうかを保持します
	 * </span>
	 */
	private boolean loadedPluginUpdated = false;


	/**
	 * <span class="lang-en">
	 * Stores whether library scripts are added/removed by re-loadings after the previous execution or not
	 * </span>
	 * <span class="lang-ja">
	 * 前回の実行後に、ライブラリが再読み込みによって追加/削除されたかどうかを保持します
	 * </span>
	 */
	private boolean loadedLibraryUpdated = false;


	/**
	 * <span class="lang-en">
	 * Create an script engine of the Vnano, however,
	 * use "getEngineByName" method of "ScriptEngineManager" class with an argument "Vnano",
	 * instead of using this constructor directly
	 * </span>
	 * <span class="lang-ja">
	 * 標準設定でVnanoのスクリプトエンジンを生成しますが,
	 * 通常はこのコンストラクタを直接用いる代わりに,
	 * ScriptEngineManager クラスの getEngineByName メソッドを, 引数 "Vnano" を指定して用いてください
	 * </span>
	 * .
	 * <span class="lang-en">
	 * If you want to create and use an instance of the script engine of the Vnano directly,
	 * use {@link VnanoEngine VnanoEngine}.
	 * This class is its wrapper for using it through the Scripting API of the standard library.
	 * </span>
	 *
	 * <span class="lang-ja">
	 * もしもVnanoのスクリプトエンジンのインスタンスを直接生成して使用したい場合は,
	 * {@link VnanoEngine VnanoEngine} クラスを使用してください.
	 * このクラスはそのラッパーで, 標準ライブラリの Scripting API を介して利用するためのものです.
	 * </span>
	 */
	protected VnanoScriptEngine() {
		try {
			// デフォルトの言語仕様設定を生成
			LANG_SPEC = new LanguageSpecContainer();

			// 要素を取り出す順序が登録順と一致する事を保証するため、LinkedHashMapを用いた Bindings を生成
			this.putPluginBindings = new SimpleBindings(new LinkedHashMap<String, Object>());

			// スクリプトをファイルから読み込むためのローダーを生成（ライブラリはこのローダーに登録）
			this.libraryScriptLoader = new ScriptLoader(DEFAULT_ENCODING, LANG_SPEC);

			// プラグインをファイルから読み込むためのローダーを生成
			this.pluginLoader = new PluginLoader(DEFAULT_ENCODING, LANG_SPEC);

			// デフォルトの設定で Vnano エンジンのインスタンスを生成
			this.vnanoEngine = new VnanoEngine(LANG_SPEC);

		// ScriptEngineManager 経由でインスタンスを取得している場合（失敗時は null が返る）に
		// エラー情報の詳細を把握しやすいようにスタックトレースを出力しておく
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}


	/**
	 * <span class="lang-en">Executes an expression or a script code passed as an argument</span>
	 * <span class="lang-ja">引数に指定された式またはスクリプトコードを実行します</span>
	 * .
	 * @param script
	 *   <span class="lang-en">The expression or the script code to execute.</span>
	 *   <span class="lang-ja">実行対象の式またはスクリプトコード.</span>
	 *
	 * @return
	 *   <span class="lang-en">The evaluated value of the expression, or last expression statement in the script code.</span>
	 *   <span class="lang-ja">式、またはスクリプトコード内の最後の式文の評価値.</span>
	 *
	 * @throws ScriptException
	 *   <span class="lang-en">Thrown when an error will be detected for the content or the processing of the script.</span>
	 *   <span class="lang-ja">スクリプトの内容または実行過程にエラーが検出された場合にスローされます.</span>
	 */
	@Override
	public Object eval(String scriptCode) throws ScriptException {
		try {

			// ライブラリとプラグインをエンジンに登録
			this.updatePluginConnections();
			this.updateLibraryInclusions();

			// スクリプトコードを実行
			Object value = this.vnanoEngine.executeScript(scriptCode);
			return value;

		// 発生し得る例外は ScriptException でラップして投げる
		} catch (VnanoException vnanoException) {

			// 行番号などを除いたエラーメッセージを取得（ScriptException側が行番号などを付加するので、重複しないように）
			String message = vnanoException.getMessageWithoutLocation();

			// エラーメッセージがある場合は、そのメッセージで ScriptException を生成して投げる
			if (message != null) {

				// エラーメッセージを指定してScriptExceptionを生成
				ScriptException scriptException = null;
				if (vnanoException.hasFileName() && vnanoException.hasLineNumber()) {
					scriptException = new ScriptException(
						message + ":", vnanoException.getFileName(), vnanoException.getLineNumber()
					);
				} else {
					scriptException = new ScriptException(message);
				}

				// 原因となった例外の情報（cause情報）を持たせる
				try {
					scriptException.initCause(vnanoException); // Throwable のメソッド
				} catch (IllegalStateException ise) {
					// Throwableのcause情報は、既に持っていた場合は更新できないので、失敗した場合は既に持っている
					// （現状ではあり得ないが、将来的な事を考えて catch しておく）
				}

				throw scriptException;

			// エラーメッセージが無い場合はそのままラップして投げる
			} else {
				throw new ScriptException(vnanoException);
			}

		// 実装の不備等による予期しない例外も ScriptException でラップして投げる（上層を落としたくない用途のため）
		} catch (Exception unexpectedException) {

			ScriptException scriptException = new ScriptException(unexpectedException);
			throw scriptException;
		}
	}


	/**
	 * <span class="lang-en">Executes an expression or a script code read from Reader (for example: FileReader)</span>
	 * <span class="lang-ja">FileReader などの Reader からスクリプトコードを読み込んで実行します</span>
	 * .
	 * @param reader
	 *   <span class="lang-en">The Reader instance to read script code.</span>
	 *   <span class="lang-ja">スクリプトコードを読み込む Reader インスタンス.</span>
	 *
	 * @return
	 *   <span class="lang-en">The evaluated value of the expression, or last expression statement in the script code.</span>
	 *   <span class="lang-ja">式、またはスクリプトコード内の最後の式文の評価値.</span>
	 *
	 * @throws ScriptException
	 *   <span class="lang-en">Thrown when an error will be detected for the content or the processing of the script.</span>
	 *   <span class="lang-ja">スクリプトの内容または実行過程にエラーが検出された場合にスローされます.</span>
	 */
	@Override
	public Object eval(Reader reader) throws ScriptException {
		try {
			StringBuilder builder = new StringBuilder();
			int charcode = -1;
			while ((charcode = reader.read()) != -1) {
				builder.append((char)charcode);
			}
			String script = builder.toString();
			return this.eval(script);

		} catch (IOException ioe) {
			throw new ScriptException(ioe);
		}
	}


	/**
	 * <span class="lang-en">
	 * Updates connections between the VnanoEngine and plug-ins stored in fields of this class
	 * </span>
	 * <span class="lang-ja">
	 * このクラスのフィールドに保持しているプラグインと, VnanoEngine との間の接続を更新します
	 * </span>
	 * .
	 * @throws VnanoException
	 *   <span class="lang-en">
	 *   Thrown if plug-ins could not be connected,
	 *   caused by unsupported interfaces, incompatibility of data types, and so on.
	 *   </span>
	 *   <span class="lang-ja">
	 *   サポートされていないインターフェースの使用や, データ型の互換性などの原因により,
	 *   プラグインの接続に失敗した場合にスローされます.
	 *   </span>
	 */
	private void updatePluginConnections() throws VnanoException {

		 // 前回実行時から登録変更が無ければ更新不要
		if (!this.putPluginBindingsUpdated && !this.loadedPluginUpdated) {
			return;
		}
		this.putPluginBindingsUpdated = false;
		this.loadedPluginUpdated = false;

		// 現在接続されているプラグインを一旦全て接続解除
		this.vnanoEngine.disconnectAllPlugins();

		// ファイルから読み込まれたプラグインを接続（あれば）
		if (this.pluginLoader.hasPlugins()) {
			String[] loadedPluginNames = this.pluginLoader.getPluginNames();
			Object[] loadedPluginInstances = this.pluginLoader.getPluginInstances();
			for (int pluginIndex=0; pluginIndex<loadedPluginNames.length; pluginIndex++) {
				//this.vnanoEngine.connectPlugin(loadedPluginNames[pluginIndex], loadedPluginInstances[pluginIndex]);
				this.vnanoEngine.connectPlugin(SpecialBindingKey.AUTO_KEY, loadedPluginInstances[pluginIndex]); // キーは文法に則っていないといけない
			}
		}

		// 直接 put されたプラグインを接続（直接 put の方を高優先度にするため、上記よりも後で接続する）
		for (Entry<String,Object> pair: this.putPluginBindings.entrySet()) {
			this.vnanoEngine.connectPlugin(pair.getKey(), pair.getValue());
		}
	}


	/**
	 * <span class="lang-en">
	 * Updates "include"-registrations between the VnanoEngine and libraries stored in fields of this class
	 * </span>
	 * <span class="lang-ja">
	 * このクラスのフィールドに保持しているライブラリと, VnanoEngine との間の include 登録を更新します
	 * </span>
	 * .
	 * @throws VnanoException
	 *   <span class="lang-en">
	 *   Thrown if libraries could not be included, caused by "duplicate include" and so on.
	 *   </span>
	 *   <span class="lang-ja">
	 *   多重 include などにより, ライブラリの include 登録に失敗した場合にスローされます.
	 *   </span>
	 */
	private void updateLibraryInclusions() throws VnanoException {

		 // 前回実行時から登録変更が無ければ更新不要
		if (!this.loadedLibraryUpdated) {
			return;
		}
		this.loadedLibraryUpdated = false;

		// 現在 include 登録されているライブラリを一旦全て登録解除
		this.vnanoEngine.unincludeAllLibraryScripts();

		// ファイルから読み込まれたライブラリをエンジンに include 登録
		if(this.libraryScriptLoader.hasLibraryScripts()) {
			String[] libNames = this.libraryScriptLoader.getLibraryScriptNames();
			String[] libContents = this.libraryScriptLoader.getLibraryScriptContents();
			int libN = libNames.length;
			for (int libIndex=0; libIndex<libN; libIndex++) {
				this.vnanoEngine.includeLibraryScript(libNames[libIndex], libContents[libIndex]);
			}
		}
	}


	/**
	 * <span class="lang-en">
	 * This method is used for connecting an external function/variable, or setting options/permission,
	 * or taking some special operations, and so on
	 * </span>
	 * <span class="lang-ja">
	 * このメソッドは, 外部関数や変数を接続したり, オプション/パーミッションを設定したり,
	 * またはいくつかの特別な操作を行ったりするのに使用します</span>
	 * </span>
	 * .
	 * <span class="lang-en">
	 * If the value of the argument "name" is "___VNANO_OPTION_MAP",
	 * this method behaves as a wrapper of {@link VnanoEngine#setOptionMap(Map) VnanoEngine.setOptionMap(Map)}
	 * method, which is to set options.
	 * <br>
	 * If the value of the argument "name" is "___VNANO_PERMISSION_MAP",
	 * this method behaves as a wrapper of {@link VnanoEngine#setPermissionMap(Map) VnanoEngine.setPermissionMap(Map)}
	 * method, which is to set permissions.
	 * <br>
	 * If the value of the argument "name" is "___VNANO_LIBRARY_LIST_FILE",
	 * this method loads library scripts of which paths are described in the specified list file,
	 * and register them  to be "include"-ed in the execution script by the engine.
	 * <br>
	 * If the value of the argument "name" is "___VNANO_PLUGIN_LIST_FILE",
	 * this method loads plug-ins of which paths are described in the specified list file,
	 * and connect them to the engine.
	 * <br>
	 * If the value of the argument "name" is "___VNANO_COMMAND", this method invokes special commands of the engine.
	 * Available commands are defined in {@link org.vcssl.nano.spec.SpecialBindingValue}.
	 * <br>
	 * Other than the above, this method behaves as a wrapper of
	 * {@link VnanoEngine#connectPlugin(String,Object) VnanoEngine.connectPlugin(String, Object)}
	 * method, which is to connect instances of plug-ins.
	 * In this case, the argument "name" will be the identifier for accessing it from the script,
	 * so it should be described in the valid syntax.
	 * If you want to generate a valid identifier automatically, specify "___VNANO_AUTO_KEY" as the argument "name".
	 * </span>
	 *
	 * <span class="lang-ja">
	 * 引数 "name" の値に "___VNANO_OPTION_MAP" が指定されている場合, このメソッドは, オプション設定を行う
	 * {@link VnanoEngine#setOptionMap(Map) setOptionMap(Map)} メソッドのラッパーとして振舞います.
	 * <br>
	 * 引数 "name" の値に "___VNANO_PERMISSION_MAP" が指定されている場合, このメソッドは, オプション設定を行う
	 * {@link VnanoEngine#setPermissionMap(Map) setPermissionMap(Map)} メソッドのラッパーとして振舞います.
	 * <br>
	 * 引数 "name" の値に "___VNANO_LIBRARY_LIST_FILE" が指定されている場合, このメソッドは,
	 * 指定されたリストファイルにパスが記載されたライブラリスクリプトを読み込み,
	 * それらが実行時に実行対象スクリプトに "include" されるよう, エンジンに登録します.
	 * <br>
	 * 引数 "name" の値に "___VNANO_PLUGIN_LIST_FILE" が指定されている場合, このメソッドは,
	 * 指定されたリストファイルにパスが記載されたプラグインを読み込み, それらをエンジンに接続します.
	 * <br>
	 * 引数 "name" の値に "___VNANO_COMMAND" が指定されている場合, このメソッドはエンジンの特別なコマンドを実行します.
	 * 利用可能な値は {@link org.vcssl.nano.spec.SpecialBindingValue} に定義されています.
	 * <br>
	 * 上記の全てに該当しない場合には, このメソッドは, プラグインのインスタンスを接続する
	 * {@link VnanoEngine#connectPlugin(String,Object) VnanoEngine.connectPlugin(String, Object)}
	 * メソッドのラッパーとして振舞います.
	 * この場合, 引数 "name" はスクリプト内からアクセスする際の識別子として機能するため, 正しい構文で記述されている必要があります.
	 * 面倒な場合は, "___VNANO_AUTO_KEY" を引数 "name" に指定すると, 構文的に適切な識別子が自動生成されます.
	 * </span>
	 *
	 * @param name
	 *   <span class="lang-en">See the above description</span>
	 *   <span class="lang-ja">上記説明を参照してください</span>
	 *
	 * @param value
	 *   <span class="lang-en">See the above description</span>
	 *   <span class="lang-ja">上記説明を参照してください</span>
	 */
	@Override
	public void put(String name, Object value) {

		// オプションマップの場合
		if (name.equals(SpecialBindingKey.OPTION_MAP)) {
			if (value instanceof Map) {

				@SuppressWarnings("unchecked")
				Map<String, Object> castedMap = (Map<String, Object>)value;
				try {
					this.vnanoEngine.setOptionMap(castedMap);
				} catch (VnanoException e) {
					throw new VnanoFatalException(e);
				}

			} else {
				throw new VnanoFatalException(
					"The type of \"" + SpecialBindingKey.OPTION_MAP + "\" should be \"Map<String,Object>\""
				);
			}

		// パーミッションマップの場合
		} else if (name.equals(SpecialBindingKey.PERMISSION_MAP)) {
			if (value instanceof Map) {

				@SuppressWarnings("unchecked")
				Map<String, String> castedMap = (Map<String, String>)value;
				try {
					this.vnanoEngine.setPermissionMap(castedMap);
				} catch (VnanoException e) {
					throw new VnanoFatalException(e);
				}

			} else {
				throw new VnanoFatalException(
					"The type of \"" + SpecialBindingKey.PERMISSION_MAP + "\" should be \"Map<String,String>\""
				);
			}

		// 制御コマンドの場合
		} else if (name.equals(SpecialBindingKey.COMMAND)){

			// プラグインの接続解除コマンドの場合
			if (value instanceof String && value.equals(SpecialBindingValue.COMMAND_REMOVE_PLUGIN)) {
				try {
					this.vnanoEngine.disconnectAllPlugins();
				} catch (VnanoException e) {
					throw new VnanoFatalException(e);
				}
				this.pluginLoader = new PluginLoader(DEFAULT_ENCODING, LANG_SPEC);
				this.loadedPluginUpdated = true;

			// ライブラリの登録解除コマンドの場合
			} if (value instanceof String && value.equals(SpecialBindingValue.COMMAND_REMOVE_LIBRARY)) {
				this.libraryScriptLoader = new ScriptLoader(DEFAULT_ENCODING, LANG_SPEC);
				this.loadedLibraryUpdated = true;

			// ライブラリの再読み込みコマンドの場合
			} else if (value instanceof String && value.equals(SpecialBindingValue.COMMAND_RELOAD_LIBRARY)) {
				this.loadLibraries();

			// プラグインの再読み込みコマンドの場合
			} else if (value instanceof String && value.equals(SpecialBindingValue.COMMAND_RELOAD_PLUGIN)) {
				this.loadPlugins();
			}

		// ライブラリの読み込みリストファイル指定の場合
		} else if (name.equals(SpecialBindingKey.LIBRARY_LIST_FILE)) {
			this.libraryScriptLoader.setLibraryScriptListPath((String)value);
			this.loadLibraries();

		// プラグインの読み込みリストファイル指定の場合
		} else if (name.equals(SpecialBindingKey.PLUGIN_LIST_FILE)) {
			this.pluginLoader.setPluginListPath((String)value);
			this.loadPlugins();

		// 外部変数/関数プラグインのインスタンスを直接登録する場合
		} else {
			this.putPluginBindings.put(name, value);
			this.putPluginBindingsUpdated = true;
		}
	}
	private void loadPlugins() {
		try {
			this.pluginLoader.load();
			this.loadedPluginUpdated = true;
		} catch (VnanoException e) {
			throw new VnanoFatalException("Plugin loading failed", e);
		}
	}
	private void loadLibraries() {
		try {
			this.libraryScriptLoader.load();
			this.loadedLibraryUpdated = true;
		} catch (VnanoException e) {
			throw new VnanoFatalException("Library loading failed", e);
		}
	}


	/**
	 * <span class="lang-en">Gets the value setted by {@link VnanoScriptEngine#put put} method</span>
	 * <span class="lang-ja">{@link VnanoScriptEngine#put put} メソッドで設定した値を取得します</span>
	 * .
	 */
	@Override
	public Object get(String name) {
		if (name.equals(ScriptEngine.NAME)) {
			return EngineInformation.LANGUAGE_NAME;
		}
		if (name.equals(ScriptEngine.LANGUAGE)) {
			return EngineInformation.LANGUAGE_NAME;
		}
		if (name.equals(ScriptEngine.LANGUAGE_VERSION)) {
			return EngineInformation.LANGUAGE_VERSION;
		}
		if (name.equals(ScriptEngine.ENGINE)) {
			return EngineInformation.ENGINE_NAME;
		}
		if (name.equals(ScriptEngine.ENGINE_VERSION)) {
			return EngineInformation.ENGINE_VERSION;
		}
		return this.putPluginBindings.get(name);
	}


	/**
	 * <span class="lang-en"Unsupported on this script engine implementation</span>
	 * <span class="lang-ja">このスクリプトエンジンではサポートされていません</span>
	 * .
	 */
	@Override
	public Object eval(String script, Bindings bindings) throws ScriptException {
		throw new VnanoFatalException("This feature is unsupported");
	}


	/**
	 * <span class="lang-en"Unsupported on this script engine implementation</span>
	 * <span class="lang-ja">このスクリプトエンジンではサポートされていません</span>
	 * .
	 */
	@Override
	public Object eval(String script, ScriptContext context) throws ScriptException {
		throw new VnanoFatalException("This feature is unsupported");
	}

	/**
	 * <span class="lang-en"Unsupported on this script engine implementation</span>
	 * <span class="lang-ja">このスクリプトエンジンではサポートされていません</span>
	 * .
	 */
	@Override
	public Object eval(Reader reader, ScriptContext context) throws ScriptException {
		throw new VnanoFatalException("This feature is unsupported");
	}


	/**
	 * <span class="lang-en"Unsupported on this script engine implementation</span>
	 * <span class="lang-ja">このスクリプトエンジンではサポートされていません</span>
	 * .
	 */
	@Override
	public Object eval(Reader reader, Bindings bindings) throws ScriptException {
		throw new VnanoFatalException("This feature is unsupported");
	}


	/**
	 * <span class="lang-en"Unsupported on this script engine implementation</span>
	 * <span class="lang-ja">このスクリプトエンジンではサポートされていません</span>
	 * .
	 */
	@Override
	public Bindings getBindings(int scope) {
		//Scripting API側での読み込み/初期化時に落とさないため、例外は投げない
		//throw new VnanoFatalException("This feature is unsupported");
		return null;
	}


	/**
	 * <span class="lang-en"Unsupported on this script engine implementation</span>
	 * <span class="lang-ja">このスクリプトエンジンではサポートされていません</span>
	 * .
	 */
	@Override
	public void setBindings(Bindings bind, int scope) {
		//Scripting API側での読み込み/初期化時に落とさないため、例外は投げない
		//throw new VnanoFatalException("This feature is unsupported");
	}


	/**
	 * <span class="lang-en"Unsupported on this script engine implementation</span>
	 * <span class="lang-ja">このスクリプトエンジンではサポートされていません</span>
	 * .
	 */
	@Override
	public Bindings createBindings() {
		//Scripting API側での読み込み/初期化時に落とさないため、例外は投げない
		//throw new VnanoFatalException("This feature is unsupported");
		return null;
	}


	/**
	 * <span class="lang-en"Unsupported on this script engine implementation</span>
	 * <span class="lang-ja">このスクリプトエンジンではサポートされていません</span>
	 * .
	 */
	@Override
	public ScriptContext getContext() {
		//Scripting API側での読み込み/初期化時に落とさないため、例外は投げない
		//throw new VnanoFatalException("This feature is unsupported");
		return null;
	}


	/**
	 * <span class="lang-en"Unsupported on this script engine implementation</span>
	 * <span class="lang-ja">このスクリプトエンジンではサポートされていません</span>
	 * .
	 */
	@Override
	public void setContext(ScriptContext context) {
		//Scripting API側での読み込み/初期化時に落とさないため、例外は投げない
		//throw new VnanoFatalException("This feature is unsupported");
	}


	/**
	 * <span class="lang-en">Not normally used</span>
	 * <span class="lang-ja">通常は使用しません</span>
	 * .
	 */
	@Override
	public ScriptEngineFactory getFactory() {
		return new VnanoScriptEngineFactory();
	}

}
