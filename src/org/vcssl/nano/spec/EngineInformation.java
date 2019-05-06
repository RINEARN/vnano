/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;

public class EngineInformation {

	public static final String ENGINE_NAME = "RINEARN Vnano Engine";

	public static final String ENGINE_VERSION = "0.1.2";

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

