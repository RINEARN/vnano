/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

import org.vcssl.nano.compiler.Compiler;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.spec.ErrorMessage;
import org.vcssl.nano.spec.OptionName;
import org.vcssl.nano.spec.OptionValue;
import org.vcssl.nano.vm.VirtualMachine;


/**
 * Vnanoで記述されたコードを実行するためのスクリプトエンジン（Vnanoエンジン）であり、
 * Vnano処理系の最上階層となるクラスです。
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class VnanoEngine implements ScriptEngine {

	private static final String DEFAULT_EVAL_SCRIPT_NAME = "EVAL_SCRIPT";
	private static final String DEFAULT_LIBRARY_SCRIPT_NAME = "LIBRARY_SCRIPT";

	/**
	 * コンテキストを指定しない {@link VnanoEngine#eval eval} メソッドの呼び出し時に使用される、
	 * デフォルトのコンテキストを保持します。
	 */
	private ScriptContext scriptContext = null;


	/** 全てのオプション名と値を保持するマップです。 */
	private Map<String, Object> optionMap = new HashMap<String, Object>();

	/** エラーメッセージの言語を決めるロケール設定を保持します。 */
	private Locale locale = Locale.getDefault();

	private String[] libraryScriptNames = new String[0];
	private String[] libraryScriptCode = new String[0];
	private String evalScriptName = DEFAULT_EVAL_SCRIPT_NAME;



	/**
	 * 標準設定のVnanoエンジンを生成しますが、通常利用では
	 * {@link VnanoEngineFactory VnanoEngineFactory} クラスの
	 * {@link VnanoEngineFactory#getScriptEngine getScriptEngine} メソッドや、
	 * ScriptEngineManager クラスの getEngineByName メソッドを使用してください。
	 */
	protected VnanoEngine() {
		this.scriptContext = new SimpleScriptContext();
	}


	@Override
	public Object eval(String script, Bindings bindings) throws ScriptException {
		try {

			// Bindingsを処理系内の接続仲介オブジェクト（インターコネクト）に変換
			Interconnect interconnect = new Interconnect(bindings);

			// eval対象のコードとライブラリコードを配列にまとめる
			String[] scripts = new String[this.libraryScriptCode.length  + 1];
			String[] names   = new String[this.libraryScriptNames.length + 1];
			System.arraycopy(this.libraryScriptCode,  0, scripts, 0, this.libraryScriptCode.length );
			System.arraycopy(this.libraryScriptNames, 0, names,   0, this.libraryScriptNames.length);
			scripts[this.libraryScriptCode.length] = script;
			names[this.libraryScriptNames.length ] = this.evalScriptName;

			// コンパイラでVnanoスクリプトから中間アセンブリコード（VRILコード）に変換
			Compiler compiler = new Compiler();
			String assemblyCode = compiler.compile(scripts, names, interconnect, this.optionMap);

			// VMで中間アセンブリコード（VRILコード）を実行
			VirtualMachine vm = new VirtualMachine();
			Object evalValue = vm.eval(assemblyCode, interconnect, this.optionMap);
			return evalValue;

		// 発生し得る例外は ScriptException でラップして投げる
		} catch (VnanoException e) {

			// ロケール設定に応じた言語でエラーメッセージを生成
			String message = ErrorMessage.generateErrorMessage(e.getErrorType(), e.getErrorWords(), this.locale);

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

		// オプションの場合
		if (name.equals(OptionName.OPTION_MAP)) {
			if (value instanceof Map) {
				try {
					// 型パラメータ情報はコンパイル時に落ちているので、String をキーとして渡してみてエラーになれば弾く
					@SuppressWarnings({ "unused" })
					boolean b = ((Map<?,?>)value).containsKey("abcde");

					// エラーにならなければキャストして読み込み
					@SuppressWarnings("unchecked")
					Map<String, Object> castedMap = (Map<String, Object>)value;
					this.setOptions(castedMap);

				} catch (Exception e) {
					throw new VnanoFatalException("The type of \"" + optionMap + "\" should be \"Map<String,Object>\"", e);
				}
			}

		// 外部変数/関数のバインディングの場合
		} else {
			this.scriptContext.getBindings(ScriptContext.ENGINE_SCOPE).put(name, value);
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
		return new VnanoEngineFactory();
	}


	/**
	 * 全オプションの名前と値を格納するマップによって、オプションを設定します。
	 *
	 * オプションマップは Map<String,Object> 型で、そのキーにはオプション名を指定します。
	 * オプション名の具体的な値は {@link org.vcssl.nano.spec.OptionName} クラスに文字列定数として定義されています。
	 * オプションマップの値は Object 型ですが、実際の値は対応するオプション名によって異なります。
	 * こちらも、具体的な内容は {@link org.vcssl.nano.spec.OptionName} クラスに記載されている説明を参照してください。
	 *
	 * @param optionMap 全オプションの名前と値を格納するマップ
	 */
	private void setOptions(Map<String,Object> optionMap) {

		this.optionMap = optionMap;

		// ロケール設定を、このインスタンスの locale フィールドに反映
		this.locale = OptionValue.valueOf(OptionName.LOCALE, this.optionMap, Locale.class);

		// eval対象スクリプト名の設定を、このインスタンスの evalScriptName フィールドに反映
		this.evalScriptName = OptionValue.valueOf(OptionName.EVAL_SCRIPT_NAME, this.optionMap, String.class);

		// ライブラリ名の設定を、このインスタンスの libraryScriptNames フィールドに反映
		if (this.optionMap.containsKey(OptionName.LIBRARY_SCRIPT_NAME)) {
			Object value = this.optionMap.get(OptionName.LIBRARY_SCRIPT_NAME);
			if (value instanceof String) {
				this.libraryScriptNames = new String[] { (String)value };
			} else if (value instanceof String[]) {
				this.libraryScriptNames = OptionValue.stringArrayValueOf(OptionName.LIBRARY_SCRIPT_NAME, optionMap);
			} else {
				throw new VnanoFatalException(
					"The type of \"" + OptionName.LIBRARY_SCRIPT_NAME + "\" option should be \"String\" or \"String[]\""
				);
			}
		}

		// ライブラリコードの設定を、このインスタンスの libraryScriptCode フィールドに反映
		if (this.optionMap.containsKey(OptionName.LIBRARY_SCRIPT_CODE)) {
			Object value = this.optionMap.get(OptionName.LIBRARY_SCRIPT_CODE);
			if (value instanceof String) {
				this.libraryScriptCode = new String[] { (String)value };
			} else if (value instanceof String[]) {
				this.libraryScriptCode = OptionValue.stringArrayValueOf(OptionName.LIBRARY_SCRIPT_CODE, optionMap);
			} else {
				throw new VnanoFatalException(
					"The type of \"" + OptionName.LIBRARY_SCRIPT_CODE + "\" option should be \"String\" or \"String[]\""
				);
			}

			// ライブラリ名が指定されていない場合は、デフォルト値 + "[ライブラリインデックス]" として生成しておく
			if (this.optionMap.containsKey(OptionName.LIBRARY_SCRIPT_NAME)) {
				int libraryLength = this.libraryScriptCode.length;
				this.libraryScriptNames = new String[libraryLength];
				for (int libraryIndex=0; libraryIndex<libraryLength; libraryIndex++) {
					this.libraryScriptNames[libraryIndex] = DEFAULT_LIBRARY_SCRIPT_NAME + "[" + libraryIndex + "]";
				}
			}
		}
	}

}
