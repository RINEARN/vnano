/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
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
import javax.script.SimpleScriptContext;

import org.vcssl.nano.spec.SpecialBindingKey;


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
 * &raquo <a href="../../../../src/org/vcssl/nano/VnanoScriptEngine.java">Source code</a>
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class VnanoScriptEngine implements ScriptEngine {

	/**
	 * <span class="lang-en">A context storing bindings of external functions, variables, and so on</span>
	 * <span class="lang-ja">外部関数/変数のバインディング情報などを保持するコンテキストです</span>
	 * .
	 */
	private ScriptContext scriptContext = null;


	/**
	 * <span class="lang-en">A Vnano Engine to be wrapped by ScriptEngine interface</span>
	 * <span class="lang-ja">ScriptEngine インターフェースでラップする対象の Vnano エンジンです</span>
	 * .
	 */
	VnanoEngine vnanoEngine = null;


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

			// バインディング情報などを保持するスクリプトコンテキストを生成
			this.scriptContext = new SimpleScriptContext();

			// バインディング要素を取り出す順序が、登録順と一致する事を保証するため、
			// LinkedHashMapを用いた Bindings を生成し、コンテキストで使用するよう設定
			Bindings bindings = new SimpleBindings(new LinkedHashMap<String, Object>());
			scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

			// デフォルトの設定で Vnano エンジンのインスタンスを生成
			this.vnanoEngine = new VnanoEngine();

		// ScriptEngineManager 経由でインスタンスを取得している場合（失敗時は null が返る）に
		// エラー情報の詳細を把握しやすいようにスタックトレースを出力しておく
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}


	/**
	 * <span class="lang-en">Executes an expression or script code passed as an argument</span>
	 * <span class="lang-ja">引数に指定された式またはスクリプトコードを実行します</span>
	 * .
	 * @param script
	 *   <span class="lang-en">The expression or script code to be executed.</span>
	 *   <span class="lang-ja">実行対象の式またはスクリプトコード.</span>
	 *
	 * @return
	 *   <span class="lang-en">
	 *   The evaluated value of the expression, or the last expression statement in script code.
	 *   If there is no evaluated value, returns null.
	 *   </span>
	 *   <span class="lang-ja">
	 *   式, またはスクリプトコード内の最後の式文の評価値. もしも評価値が無かった場合は null が返されます.
	 *   </span>
	 *
	 * @throws ScriptException
	 *   <span class="lang-en">Thrown when an error will be detected for the content or the processing of the script.</span>
	 *   <span class="lang-ja">スクリプトの内容または実行過程にエラーが検出された場合にスローされます.</span>
	 */
	@Override
	public Object eval(String script) throws ScriptException {
		return this.eval(script, this.scriptContext);
	}


	/**
	 * <span class="lang-en">Executes an expression or a script code passed as an argument</span>
	 * <span class="lang-ja">引数に指定された式またはスクリプトコードを実行します</span>
	 * .
	 *
	 * @param script
	 *   <span class="lang-en">The expression or the script code to execute.</span>
	 *   <span class="lang-ja">実行対象の式またはスクリプトコード.</span>
	 *
	 * @param context
	 *   <span class="lang-en">The context storing bindings of external functions, variables, and so on.</span>
	 *   <span class="lang-ja">外部関数/変数のバインディング情報などを保持するコンテキスト.</span>
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
	public Object eval(String script, ScriptContext context) throws ScriptException {
		Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
		return this.eval(script, bindings);
	}


	/**
	 * <span class="lang-en">Executes an expression or a script code passed as an argument</span>
	 * <span class="lang-ja">引数に指定された式またはスクリプトコードを実行します</span>
	 * .
	 * @param script
	 *   <span class="lang-en">The expression or the script code to execute.</span>
	 *   <span class="lang-ja">実行対象の式またはスクリプトコード.</span>
	 *
	 * @param bindings
	 *   <span class="lang-en">The bindings of external functions, variables, and so on.</span>
	 *   <span class="lang-ja">外部関数/変数などのバインディング.</span>
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
	public Object eval(String scriptCode, Bindings bindings) throws ScriptException {
		try {

			// Bindings から1個ずつ全ての要素を取り出して、プラグインとしてVnanoEngineに接続
			for (Entry<String,Object> pair: bindings.entrySet()) {
				this.vnanoEngine.connectPlugin(pair.getKey(), pair.getValue());
			}

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
		Bindings bindings = this.scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
		return this.eval(reader, bindings);
	}


	/**
	 * <span class="lang-en">Executes an expression or a script code read from Reader (for example: FileReader)</span>
	 * <span class="lang-ja">FileReader などの Reader からスクリプトコードを読み込んで実行します</span>
	 * .
	 * @param reader
	 *   <span class="lang-en">The Reader instance to read script code.</span>
	 *   <span class="lang-ja">スクリプトコードを読み込む Reader インスタンス.</span>
	 *
	 * @param context
	 *   <span class="lang-en">The context storing bindings of external functions, variables, and so on.</span>
	 *   <span class="lang-ja">外部関数/変数のバインディング情報などを保持するコンテキスト.</span>
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
	public Object eval(Reader reader, ScriptContext context) throws ScriptException {
		Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
		return this.eval(reader, bindings);
	}


	/**
	 * <span class="lang-en">Executes an expression or a script code read from Reader (for example: FileReader)</span>
	 * <span class="lang-ja">FileReader などの Reader からスクリプトコードを読み込んで実行します</span>
	 * .
	 * @param reader
	 *   <span class="lang-en">The Reader instance to read script code.</span>
	 *   <span class="lang-ja">スクリプトコードを読み込む Reader インスタンス.</span>
	 *
	 * @param bindings
	 *   <span class="lang-en">The bindings of external functions, variables, and so on.</span>
	 *   <span class="lang-ja">外部関数/変数などのバインディング.</span>
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
	public Object eval(Reader reader, Bindings bindings) throws ScriptException {
		try {

			StringBuilder builder = new StringBuilder();
			int charcode = -1;
			while ((charcode = reader.read()) != -1) {
				builder.append((char)charcode);
			}

			String script = builder.toString();
			return this.eval(script, bindings);

		} catch (IOException e) {
			throw new ScriptException(e);
		}
	}


	/**
	 * <span class="lang-en">
	 * This method is used for connecting an external function/variable, or setting options,
	 * or taking some special operations, and so on
	 * </span>
	 * <span class="lang-ja">
	 * このメソッドは, 外部関数や変数を接続したり, オプションを設定したり,
	 * またはいくつかの特別な操作を行ったりするのに使用します</span>
	 * </span>
	 * .
	 * <span class="lang-en">
	 * If the value of the argument "name" is "___VNANO_OPTION_MAP",
	 * this method behaves as a wrapper of {@link VnanoEngine#setOptionMap(Map) VnanoEngine.setOptionMap(Map)}
	 * method, which is to set options.
	 * The other cases, this method behaves as a wrapper of
	 * {@link VnanoEngine#connectPlugin(String,Object) VnanoEngine.connectPlugin(String, Object)}
	 * method, which is to connect plug-ins provides external functions and variables.
	 * </span>
	 *
	 * <span class="lang-ja">
	 * 引数 "name" の値に "___VNANO_OPTION_MAP" が指定されている場合, このメソッドは, オプション設定を行う
	 * {@link VnanoEngine#setOptionMap(Map) setOptionMap(Map)} メソッドのラッパーとして振舞います.
	 * それ以外の場合には, このメソッドは, 外部関数や変数を提供するプラグインを接続する
	 * {@link VnanoEngine#connectPlugin(String,Object) VnanoEngine.connectPlugin(String, Object)}
	 * メソッドのラッパーとして振舞います.
	 * </span>
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

		// 外部変数/関数プラグインのバインディングの場合
		} else {
			try {
				this.vnanoEngine.connectPlugin(name, value);
			} catch (VnanoException e) {
				throw new VnanoFatalException(e);
			}
		}
	}


	/**
	 * <span class="lang-en">Gets the value setted by {@link VnanoScriptEngine#put put} method</span>
	 * <span class="lang-ja">{@link VnanoScriptEngine#put put} メソッドで設定した値を取得します</span>
	 * .
	 */
	@Override
	public Object get(String name) {
		return this.scriptContext.getBindings(ScriptContext.ENGINE_SCOPE).get(name);
	}


	/**
	 * <span class="lang-en">Not normally used</span>
	 * <span class="lang-ja">通常は使用しません</span>
	 * .
	 */
	@Override
	public Bindings getBindings(int scope) {
		return this.scriptContext.getBindings(scope);
	}


	/**
	 * <span class="lang-en">Not normally used</span>
	 * <span class="lang-ja">通常は使用しません</span>
	 * .
	 */
	@Override
	public void setBindings(Bindings bind, int scope) {
		this.scriptContext.setBindings(bind, scope);
	}


	/**
	 * <span class="lang-en">Not normally used</span>
	 * <span class="lang-ja">通常は使用しません</span>
	 * .
	 */
	@Override
	public Bindings createBindings() {
		// バインディング要素を取り出す順序が、登録順と一致する事を保証するため、LinkedHashMapを用いたものを生成
		return new SimpleBindings(new LinkedHashMap<String, Object>());
	}


	/**
	 * <span class="lang-en">Not normally used</span>
	 * <span class="lang-ja">通常は使用しません</span>
	 * .
	 */
	@Override
	public ScriptContext getContext() {
		return this.scriptContext;
	}


	/**
	 * <span class="lang-en">Not normally used</span>
	 * <span class="lang-ja">通常は使用しません</span>
	 * .
	 */
	@Override
	public void setContext(ScriptContext context) {
		this.scriptContext = context;
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
