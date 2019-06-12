package org.vcssl.nano.spec;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.VnanoFatalException;

public class OptionValue {

	public static final String DUMPER_TARGET_ALL = "ALL";
	public static final String DUMPER_TARGET_INPUTTED_CODE = "INPUTTED_CODE";
	public static final String DUMPER_TARGET_PREPROCESSED_CODE = "PREPROCESSED_CODE";
	public static final String DUMPER_TARGET_TOKEN = "TOKEN";
	public static final String DUMPER_TARGET_PARSED_AST = "PARSED_AST";
	public static final String DUMPER_TARGET_ANALYZED_AST = "ANALYZED_AST";
	public static final String DUMPER_TARGET_ASSEMBLY_CODE = "ASSEMBLY_CODE";
	public static final String DUMPER_TARGET_OBJECT_CODE = "OBJECT_CODE";
	public static final String DUMPER_TARGET_ACCELERATOR_CODE = "ACCELERATOR_CODE";


	private static final Map<String, Object> DEFAULT_VALUE_MAP = new LinkedHashMap<String, Object>();
	static {
		DEFAULT_VALUE_MAP.put(OptionKey.EVAL_SCRIPT_NAME, "EVAL_SCRIPT");
		DEFAULT_VALUE_MAP.put(OptionKey.EVAL_NUMBER_AS_FLOAT, Boolean.valueOf(false));
		DEFAULT_VALUE_MAP.put(OptionKey.LIBRARY_SCRIPTS, new String[0]);
		DEFAULT_VALUE_MAP.put(OptionKey.LIBRARY_SCRIPT_NAMES, new String[0]);
		DEFAULT_VALUE_MAP.put(OptionKey.LOCALE, Locale.getDefault());
		DEFAULT_VALUE_MAP.put(OptionKey.ACCELERATOR_ENABLED, Boolean.valueOf(true));
		DEFAULT_VALUE_MAP.put(OptionKey.DUMPER_ENABLED, false);
		DEFAULT_VALUE_MAP.put(OptionKey.DUMPER_TARGET, DUMPER_TARGET_ALL);
		DEFAULT_VALUE_MAP.put(OptionKey.DUMPER_STREAM, System.out);
		DEFAULT_VALUE_MAP.put(OptionKey.RUNNING_ENABLED, Boolean.valueOf(true));
	}

	/**
	 * 指定されたオプションマップ内を走査し、未指定のキーを、デフォルト値を用いて補完して返します。
	 * 結果、{@link OptionKey OptionKey} 列挙子に定義された全てのキーを持つオプションマップが得られます。
	 *
	 * @param optionMap 補完対象のオプションマップ
	 * @return 補完済みのオプションマップ
	 */
	public static Map<String, Object> supplementDefaultValuesOf(Map<String, Object> optionMap) {

		// 補完結果として返すマップを生成
		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();

		// inputMap 内に既に存在する内容を returnMap にコピー
		Set<Entry<String, Object>> inputEntrySet = optionMap.entrySet();
		for (Entry<String, Object> entry: inputEntrySet) {
			returnMap.put(entry.getKey(), entry.getValue());
		}

		// DEFAULT_VALUE_MAP からデフォルト値を resultMap にコピー（未指定のもののみ）
		Set<Entry<String, Object>> defaultEntrySet = DEFAULT_VALUE_MAP.entrySet();
		for (Entry<String, Object> entry: defaultEntrySet) {
			if (!returnMap.containsKey(entry.getKey())) {
				returnMap.put(entry.getKey(), entry.getValue());
			}
		}

		return returnMap;
	}


	/**
	 * 指定されたオプションマップ内を走査し、値の型や内容などを検査します。
	 *
	 * @param optionMap 検査対象のオプションマップ
	 * @throws VnanoException  正しくない値が検出された場合にスローされます。
	 */
	public static void checkValuesOf(Map<String, Object> optionMap) throws VnanoException {
		checkValueOf(OptionKey.EVAL_SCRIPT_NAME, optionMap, String.class);
		checkValueOf(OptionKey.EVAL_NUMBER_AS_FLOAT, optionMap, Boolean.class);
		checkValueOf(OptionKey.LIBRARY_SCRIPTS, optionMap, String[].class);
		checkValueOf(OptionKey.LIBRARY_SCRIPT_NAMES, optionMap, String[].class);
		checkValueOf(OptionKey.LOCALE, optionMap, Locale.class);
		checkValueOf(OptionKey.ACCELERATOR_ENABLED, optionMap, Boolean.class);
		checkValueOf(OptionKey.DUMPER_ENABLED, optionMap, Boolean.class);
		checkValueOf(OptionKey.DUMPER_TARGET, optionMap, String.class);
		checkValueOf(OptionKey.DUMPER_STREAM, optionMap, PrintStream.class);
		checkValueOf(OptionKey.RUNNING_ENABLED, optionMap, Boolean.class);
	}

	/**
	 * オプションマップ内の、指定されたオプション値の存在や、型、および内容などを検査します。
	 *
	 * @param optionKey 検査対象のオプションのキー
	 * @param optionMap 検査対象のオプションを含むオプションマップ
	 * @throws VnanoException  値が存在しないか、正しくない場合にスローされます。
	 */
	private static void checkValueOf(String optionKey, Map<String, Object> optionMap, Class<?> classOfValue)
			throws VnanoException {

		if (!optionMap.containsKey(optionKey)) {
			throw new VnanoException(ErrorType.OPTION_KEY_IS_NOT_FOUND, optionKey);
		}

		Object optionValue = optionMap.get(optionKey);
		if (!classOfValue.isInstance(optionValue)) {
			throw new VnanoException(
				ErrorType.INVALID_OPTION_VALUE_TYPE, new String[] { optionKey, classOfValue.getCanonicalName() }
			);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T valueOf(String optionName, Map<String, Object> optionMap, Class<?> classOfValue) {
		Object valueObject = null;
		if (optionMap.containsKey(optionName)) {
			valueObject = optionMap.get(optionName);
		} else {
			//valueObject = DEFAULT_VALUE_MAP.get(optionName);
			throw new VnanoFatalException("The option key \"" + optionName + "\" is not contained in the option map");
		}
		if (!classOfValue.isInstance(valueObject)) {
			throw new VnanoFatalException("The type of \"" + optionName + "\" should be \"" + classOfValue + "\"");
		}
		return (T)valueObject;
	}

/*
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
*/
}
