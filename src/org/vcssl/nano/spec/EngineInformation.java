/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;

public class EngineInformation {

	public static final String ENGINE_NAME = "Vnano Engine";

	public static final String ENGINE_VERSION = "VNE0000";

	public static final String[] EXTENTIONS = { "vnano" };

	public static final String[] MIME_TYPES = { };

	public static final String[] NAMES = {
			"vnano",
			"Vnano",
			"VnanoEngine",
			"vnanoengine",
			"Vnano Engine",
			"vnano engine",
			ENGINE_NAME,
	};

	public static final String LANGUAGE_NAME = "Vnano";

	public static final String LANGUAGE_VERSION = "0.0.1";


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

