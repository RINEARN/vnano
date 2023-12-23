/*
 * Copyright(C) 2017-2023 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;
import java.util.HashMap;
import java.util.Map;
import javax.script.ScriptEngine;

/**
 * The class to define basic information (language name, version, and so on) of this implementation of the script engine of the Vnano.
 */
public class EngineInformation {

	/** The name of this script engine. */
	public static final String ENGINE_NAME = "RINEARN Vnano Engine";

	/** The version of this script engine. */
	public static final String ENGINE_VERSION = "1.1.2";

	/** The extension of script files which can run on this script engine. */
	public static final String[] EXTENTIONS = { "vnano" };

	/** The MIME types of script files which can run on this script engine. */
	public static final String[] MIME_TYPES = { };

	/** The names of this script engine, for searching this engine by name. */
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

	/** The name of the scripting language supported by this script engine. */
	public static final String LANGUAGE_NAME = "Vnano";

	/** The version of the scripting language supported by this script engine. */
	public static final String LANGUAGE_VERSION = ENGINE_VERSION;


	/** The Map mapping from each key of {@link EngineInformation#getValue(key)} method to the corresponding value. */
	private static final Map<String, Object> KEY_INFORMATION_MAP = new HashMap<String, Object>();
	static {
		KEY_INFORMATION_MAP.put(ScriptEngine.ENGINE, ENGINE_NAME);
		KEY_INFORMATION_MAP.put(ScriptEngine.ENGINE_VERSION, ENGINE_VERSION);
		KEY_INFORMATION_MAP.put(ScriptEngine.LANGUAGE, LANGUAGE_NAME);
		KEY_INFORMATION_MAP.put(ScriptEngine.LANGUAGE_VERSION, LANGUAGE_VERSION);
		KEY_INFORMATION_MAP.put(ScriptEngine.NAME, NAMES[0]);
	}

	/**
	 * Returns the value of the engine information corresponding with the specified key.
	 *
	 * @param key The key of the engine information.
	 * @return The value of the engine information corresponding with the specified key.
	 */
	public static final Object getValue(String key) {
		return KEY_INFORMATION_MAP.get(key);
	}
}

