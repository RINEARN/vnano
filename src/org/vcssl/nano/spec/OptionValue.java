package org.vcssl.nano.spec;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.vcssl.nano.VnanoFatalException;

public class OptionValue {

	private static final Map<String, Object> DEFAULT_VALUE_MAP = new HashMap<String, Object>();
	static {
		DEFAULT_VALUE_MAP.put(OptionName.EVAL_SCRIPT_NAME, "EVAL_SCRIPT");
		DEFAULT_VALUE_MAP.put(OptionName.EVAL_NUMBER_AS_FLOAT, Boolean.valueOf(false));
		DEFAULT_VALUE_MAP.put(OptionName.LIBRARY_SCRIPTS, new String[0]);
		DEFAULT_VALUE_MAP.put(OptionName.LIBRARY_SCRIPT_NAMES, new String[0]);
		DEFAULT_VALUE_MAP.put(OptionName.LOCALE, Locale.getDefault());
		DEFAULT_VALUE_MAP.put(OptionName.ACCELERATOR_ENABLED, Boolean.valueOf(false));
	}

	@SuppressWarnings("unchecked")
	public static <T> T valueOf(String optionName, Map<String, Object> optionMap, Class<?> classOfValue) {
		Object valueObject = null;
		if (optionMap.containsKey(optionName)) {
			valueObject = optionMap.get(optionName);
		} else {
			valueObject = DEFAULT_VALUE_MAP.get(optionName);
		}
		if (!classOfValue.isInstance(valueObject)) {
			throw new VnanoFatalException("The type of \"" + optionName + "\" should be \"" + classOfValue + "\"");
		}
		return (T)valueObject;
	}

	/*
	public static Object objectValueOf(String optionName, Map<String, Object> optionMap) {
		if (optionMap.containsKey(optionName)) {
			return optionMap.get(optionName);
		} else {
			return DEFAULT_VALUE_MAP.get(optionName);
		}
	}
	*/

	public static boolean booleanValueOf(String optionName, Map<String, Object> optionMap) {
		Object valueObject = valueOf(optionName, optionMap, Boolean.class);
		return ((Boolean)valueObject).booleanValue();
	}

	public static String stringValueOf(String optionName, Map<String, Object> optionMap) {
		Object valueObject = valueOf(optionName, optionMap, String.class);
		return ((String)valueObject);
	}

	public static String[] stringArrayValueOf(String optionName, Map<String, Object> optionMap) {
		Object valueObject = valueOf(optionName, optionMap, String[].class);
		String[] valueArray = (String[])valueObject;
		int length = valueArray.length;
		String[] copyArray = new String[length];
		System.arraycopy(valueArray, 0, copyArray, 0, length);
		return copyArray;
	}

}
