/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano;

import java.io.IOException;
import java.io.Reader;
import java.util.Locale;

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
import org.vcssl.nano.vm.VirtualMachine;
import org.vcssl.nano.vm.memory.MemoryAccessException;


/**
 * Vnanoで記述されたコードを実行するためのスクリプトエンジン（Vnanoエンジン）であり、
 * Vnano処理系の最上階層となるクラスです。
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class VnanoEngine implements ScriptEngine {

	private static final String DEFAULT_SCRIPT_NAME = "EVAL_CODE";

	/**
	 * コンテキストを指定しない {@link VnanoEngine#eval eval} メソッドの呼び出し時に使用される、
	 * デフォルトのコンテキストを保持します。
	 */
	private ScriptContext scriptContext = null;


	/**
	 * エラーメッセージの表示言語指定などに使用されるロケールを保持します。
	 */
	private Locale locale = Locale.getDefault();


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

			// コンパイラでVnanoスクリプトから中間アセンブリコード（VRILコード）に変換
			Compiler compiler = new Compiler();
			String assemblyCode = compiler.compile(script, DEFAULT_SCRIPT_NAME, interconnect);

			// VMで中間アセンブリコード（VRILコード）を実行
			VirtualMachine vm = new VirtualMachine();
			Object evalValue = vm.eval(assemblyCode, interconnect);
			return evalValue;

		// 発生し得る例外は ScriptException でラップして投げる
		} catch (VnanoSyntaxException e) {

			String message = ErrorMessage.generateErrorMessage(e.getErrorType(), e.getErrorWords(), this.locale);
			if (e.hasFileName() && e.hasLineNumber()) {
				throw new ScriptException(message + ":", e.getFileName(), e.getLineNumber());
			} else {
				throw new ScriptException(message);
			}

		} catch (MemoryAccessException e) {

			// 実装最終段階において、例外の種類や原因に応じてより細かく処理を分けて、
			// エラーメッセージの生成・格納などを行う。現在は暫定的な処理

			ScriptException scriptException = new ScriptException(e);
			throw scriptException;

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
		this.scriptContext.getBindings(ScriptContext.ENGINE_SCOPE).put(name, value);
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


}
