/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;

// Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/spec/EngineInformation.html
// ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/spec/EngineInformation.html

/**
 * <p>
 * <span>
 * <span class="lang-en">
 * The class to define basic information (language name, version, and so on) of
 * this implementation of the script engine of the Vnano
 * </span>
 * <span class="lang-ja">
 * この Vnano のスクリプトエンジン実装の基本情報（言語名やバージョンなど）が定義されたクラスです
 * </span>
 * .
 * </p>
 *
 * <p>
 * &raquo; <a href="../../../../../src/org/vcssl/nano/spec/EngineInformation.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/spec/EngineInformation.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/spec/EngineInformation.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class EngineInformation {

	public static final String ENGINE_NAME = "RINEARN Vnano Engine";

	public static final String ENGINE_VERSION = "0.2.7";

	public static final String[] EXTENTIONS = { "vnano" };

	public static final String[] MIME_TYPES = { };

	public static final String[] NAMES = {
			"vnano",
			"Vnano",
			"VnanoEngine",
			"vnanoengine",
			"Vnano Engine",
			"vnano engine",
			"RINEARN Vnano Engine",
			"rinearn vnano engine",
			ENGINE_NAME,
	};

	public static final String LANGUAGE_NAME = ScriptWord.SCRIPT_LANGUAGE_NAME;

	public static final String LANGUAGE_VERSION = ScriptWord.SCRIPT_LANGUAGE_VERSION;


	private static final Map<String, Object> KEY_INFORMATION_MAP = new HashMap<String, Object>();
	static {
		KEY_INFORMATION_MAP.put(ScriptEngine.ENGINE, ENGINE_NAME);
		KEY_INFORMATION_MAP.put(ScriptEngine.ENGINE_VERSION, ENGINE_VERSION);
		KEY_INFORMATION_MAP.put(ScriptEngine.LANGUAGE, LANGUAGE_NAME);
		KEY_INFORMATION_MAP.put(ScriptEngine.LANGUAGE_VERSION, LANGUAGE_VERSION);
		KEY_INFORMATION_MAP.put(ScriptEngine.NAME, NAMES[0]);
	}

	public static final Object getValue(String key) {
		return KEY_INFORMATION_MAP.get(key);
	}
}

