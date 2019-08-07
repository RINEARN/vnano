/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano;

import java.util.Arrays;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import org.vcssl.nano.spec.EngineInformation;

//Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/VnanoScriptEngineFactory.html
//ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/VnanoScriptEngineFactory.html

/**
 * <span class="lang-en">
 * The factory class to create instances of {@link VnanoScriptEngine VnanoScriptEngine} class
 * </span>
 * <span class="lang-ja">
 * {@link VnanoScriptEngine VnanoScriptEngine} のインスタンスを生成するためのファクトリークラスです
 * </span>
 * .
 *
 * <p>
 * &raquo; <a href="../../../../src/org/vcssl/nano/VnanoScriptEngineFactory.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../api/org/vcssl/nano/VnanoScriptEngineFactory.html">Public Only</a>
 * | <a href="../../../../api-all/org/vcssl/nano/VnanoScriptEngineFactory.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class VnanoScriptEngineFactory implements ScriptEngineFactory {

	@Override
	public String getEngineName() {
		return EngineInformation.ENGINE_NAME;
	}

	@Override
	public String getEngineVersion() {
		return EngineInformation.ENGINE_VERSION;
	}

	@Override
	public List<String> getExtensions() {
		return Arrays.asList(EngineInformation.EXTENTIONS);
	}

	@Override
	public List<String> getMimeTypes() {
		return Arrays.asList(EngineInformation.MIME_TYPES);
	}

	@Override
	public List<String> getNames() {
		return Arrays.asList(EngineInformation.NAMES);
	}

	@Override
	public String getLanguageName() {
		return EngineInformation.LANGUAGE_NAME;
	}

	@Override
	public String getLanguageVersion() {
		return EngineInformation.LANGUAGE_VERSION;
	}

	// エンジン情報をScriptEngineのキーで指定して呼び出す
	@Override
	public Object getParameter(String key) {
		return EngineInformation.getValue(key);
	}

	// メソッドを呼び出すスクリプト記述
	@Override
	public String getMethodCallSyntax(String obj, String m, String... args) {
		return null;
	}

	// 出力を行うスクリプト記述
	@Override
	public String getOutputStatement(String toDisplay) {
		return null;
	}

	// 文をプログラムに変換
	@Override
	public String getProgram(String... statements) {
		return null;
	}

	@Override
	public ScriptEngine getScriptEngine() {
		return new VnanoScriptEngine();
	}

}
