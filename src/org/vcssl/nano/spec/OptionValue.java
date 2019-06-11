package org.vcssl.nano.spec;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.vcssl.nano.VnanoFatalException;

public class OptionValue {

	public static final String DUMPER_TARGET_ALL = "DUMPER_TARGET_ALL";
	public static final String DUMPER_TARGET_INPUTTED_CODE = "DUMPER_TARGET_INPUTTED_CODE";
	public static final String DUMPER_TARGET_PREPROCESSED_CODE = "DUMPER_TARGET_PREPROCESSED_CODE";
	public static final String DUMPER_TARGET_TOKEN = "DUMPER_TARGET_TOKEN";
	public static final String DUMPER_TARGET_PARSED_AST = "DUMPER_TARGET_PARSED_AST";
	public static final String DUMPER_TARGET_ANALYZED_AST = "DUMPER_TARGET_ANALYZED_AST";
	public static final String DUMPER_TARGET_ASSEMBLY_CODE = "DUMPER_TARGET_ASSEMBLY_CODE";
	public static final String DUMPER_TARGET_OBJECT_CODE = "DUMPER_TARGET_OBJECT_CODE";
	public static final String DUMPER_TARGET_ACCELERATOR_CODE = "DUMPER_TARGET_ACCELERATOR_CODE";


	private static final Map<String, Object> DEFAULT_VALUE_MAP = new HashMap<String, Object>();
	static {
		DEFAULT_VALUE_MAP.put(OptionKey.EVAL_SCRIPT_NAME, "EVAL_SCRIPT");
		DEFAULT_VALUE_MAP.put(OptionKey.EVAL_NUMBER_AS_FLOAT, Boolean.valueOf(false));
		DEFAULT_VALUE_MAP.put(OptionKey.LIBRARY_SCRIPTS, new String[0]);
		DEFAULT_VALUE_MAP.put(OptionKey.LIBRARY_SCRIPT_NAMES, new String[0]);
		DEFAULT_VALUE_MAP.put(OptionKey.LOCALE, Locale.getDefault());
		DEFAULT_VALUE_MAP.put(OptionKey.ACCELERATOR_ENABLED, Boolean.valueOf(true));
		DEFAULT_VALUE_MAP.put(OptionKey.DUMPER_ENABLED, false);
		DEFAULT_VALUE_MAP.put(OptionKey.DUMPER_TARGET, DUMPER_TARGET_ALL);
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
