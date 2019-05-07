/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano;

import java.io.IOException;
import java.io.Reader;
import java.util.Locale;
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
import org.vcssl.nano.spec.ErrorMessage;
import org.vcssl.nano.spec.OptionKey;


/**
 * Vnanoで記述されたコードを実行するためのスクリプトエンジン（Vnanoエンジン）であり、
 * Vnano処理系の最上階層となるクラスです。
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class VnanoScriptEngine implements ScriptEngine {

	/**
	 * コンテキストを指定しない {@link VnanoScriptEngine#eval eval} メソッドの呼び出し時に使用される、
	 * デフォルトのコンテキストを保持します。
	 */
	private ScriptContext scriptContext = null;

	VnanoEngine vnanoEngine = null;


	/**
	 * 標準設定のVnanoエンジンを生成しますが、通常利用では
	 * {@link VnanoScriptEngineFactory VnanoEngineFactory} クラスの
	 * {@link VnanoScriptEngineFactory#getScriptEngine getScriptEngine} メソッドや、
	 * ScriptEngineManager クラスの getEngineByName メソッドを使用してください。
	 */
	protected VnanoScriptEngine() {
		this.scriptContext = new SimpleScriptContext();
		this.vnanoEngine = new VnanoEngine();
	}


	@Override
	public Object eval(String scriptCode, Bindings bindings) throws ScriptException {
		try {

			// Bindings から1個ずつ全ての要素を取り出して、プラグインとしてVnanoEngineに接続
			// 注: 要素を取り出す順序については、登録順と一致する事は保証されていない模様（実際にしばしば異なる）
			// -> SimpleBindingsを使う場合は、コンストラクタで LinkedHashMap を指定する等して対応可能、
			//    しかしBindingsはインターフェースなので、実際に外側からどのような実装が渡されるかは未知
			//    -> また後の段階で要検討
			for (Entry<String,Object> pair: bindings.entrySet()) {
				this.vnanoEngine.connectPlugin(pair.getKey(), pair.getValue());
			}

			// スクリプトコードを実行
			Object value = this.vnanoEngine.executeScript(scriptCode);
			return value;

		// 発生し得る例外は ScriptException でラップして投げる
		} catch (VnanoException e) {

			// 設定されているロケールを取得
			Locale locale = Locale.getDefault();
			Map<String,Object> optionMap = this.vnanoEngine.getOptionMap();
			if (optionMap.containsKey(OptionKey.LOCALE)) {
				Object optionLocale = this.vnanoEngine.getOptionMap().get(OptionKey.LOCALE);
				if (optionLocale instanceof Locale) {
					locale = (Locale)optionLocale;
				}
			}

			// ロケールに応じた言語でエラーメッセージを生成
			String message = ErrorMessage.generateErrorMessage(e.getErrorType(), e.getErrorWords(), locale);

			// エラーメッセージから ScriptException を生成して投げる
			if (e.hasFileName() && e.hasLineNumber()) {
				throw new ScriptException(message + ":", e.getFileName(), e.getLineNumber());
			} else {
				throw new ScriptException(message);
			}

		// 実装の不備等による予期しない例外も ScriptException でラップする（上層を落としたくない用途のため）
		} catch (Exception unexpectedException) {

			ScriptException scriptException = new ScriptException(unexpectedException);
			throw scriptException;
		}
	}

	@Override
	public Object eval(String script, ScriptContext context) throws ScriptException {
		Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
		return this.eval(script, bindings);
	}


	@Override
	public Object eval(String script) throws ScriptException {
		return this.eval(script, this.scriptContext);
	}




	@Override
	public Object eval(Reader reader, ScriptContext context) throws ScriptException {
		Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
		return this.eval(reader, bindings);
	}

	@Override
	public Object eval(Reader reader) throws ScriptException {
		Bindings bindings = this.scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
		return this.eval(reader, bindings);
	}

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


	@Override
	public void put(String name, Object value) {

		// オプションマップの場合
		if (name.equals(SpecialBindingKey.OPTION_MAP)) {
			if (value instanceof Map) {

				@SuppressWarnings("unchecked")
				Map<String, Object> castedMap = (Map<String, Object>)value;

				this.vnanoEngine.setOptionMap(castedMap);

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


	@Override
	public Object get(String name) {
		return this.scriptContext.getBindings(ScriptContext.ENGINE_SCOPE).get(name);
	}

	@Override
	public Bindings getBindings(int scope) {
		return this.scriptContext.getBindings(scope);
	}

	@Override
	public void setBindings(Bindings bind, int scope) {
		this.scriptContext.setBindings(bind, scope);
	}

	@Override
	public Bindings createBindings() {
		return new SimpleBindings();
	}

	@Override
	public ScriptContext getContext() {
		return this.scriptContext;
	}

	@Override
	public void setContext(ScriptContext context) {
		this.scriptContext = context;
	}

	@Override
	public ScriptEngineFactory getFactory() {
		return new VnanoScriptEngineFactory();
	}
}
