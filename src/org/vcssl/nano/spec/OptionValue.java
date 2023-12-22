package org.vcssl.nano.spec;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.vcssl.nano.VnanoException;

/**
 * The class defining values of the option map (option values).
 */
public final class OptionValue {


	/**
	 * (A value of {@link OptionKey#DUMPER_TARGET DUMPER_TARGET} option)
	 *
	 * Dump all contents.
	 */
	public static final String DUMPER_TARGET_ALL = "ALL";


	/**
	 * (A value of {@link OptionKey#DUMPER_TARGET DUMPER_TARGET} option)
	 *
	 * Dump the inutted script code.
	 */
	public static final String DUMPER_TARGET_INPUTTED_CODE = "INPUTTED_CODE";


	/**
	 * (A value of {@link OptionKey#DUMPER_TARGET DUMPER_TARGET} option)
	 *
	 * Dump pre-processed script code.
	 */
	public static final String DUMPER_TARGET_PREPROCESSED_CODE = "PREPROCESSED_CODE";


	/**
	 * (A value of {@link OptionKey#DUMPER_TARGET DUMPER_TARGET} option)
	 *
	 * Dump tokens, which are output of the {@link org.vcssl.nano.compiler.LexicalAnalyzer}.
	 */
	public static final String DUMPER_TARGET_TOKEN = "TOKEN";


	/**
	 * (A value of {@link OptionKey#DUMPER_TARGET DUMPER_TARGET} option)
	 *
	 * Dump the Abstract Syntax Tree (AST), which is the output of the {@link org.vcssl.nano.compiler.Parser}.
	 */
	public static final String DUMPER_TARGET_PARSED_AST = "PARSED_AST";


	/**
	 * (A value of {@link OptionKey#DUMPER_TARGET DUMPER_TARGET} option)
	 *
	 * Dump the semantic-analyzed AST, which is the output of the {@link org.vcssl.nano.compiler.SemanticAnalyzer}.
	 */
	public static final String DUMPER_TARGET_ANALYZED_AST = "ANALYZED_AST";


	/**
	 * (A value of {@link OptionKey#DUMPER_TARGET DUMPER_TARGET} option)
	 *
	 * Dump the VRIL code, which is the compilation result, output of the {@link org.vcssl.nano.compiler.CodeGenerator}.
	 */
	public static final String DUMPER_TARGET_ASSEMBLY_CODE = "ASSEMBLY_CODE";


	/**
	 * (A value of {@link OptionKey#DUMPER_TARGET DUMPER_TARGET} option)
	 *
	 * Dump the VM object code, output of the {@link org.vcssl.nano.vm.assembler.Assembler}.
	 */
	public static final String DUMPER_TARGET_OBJECT_CODE = "OBJECT_CODE";


	/**
	 * (A value of {@link OptionKey#DUMPER_TARGET DUMPER_TARGET} option)
	 *
	 * Dump instructions of {@link org.vcssl.nano.vm.accelerator.Accelerator},
	 * output of the {@link org.vcssl.nano.vm.accelerator.AcceleratorSchedulingUnit}.
	 */
	public static final String DUMPER_TARGET_ACCELERATOR_CODE = "ACCELERATOR_CODE";


	/**
	 * (A value of {@link OptionKey#DUMPER_TARGET DUMPER_TARGET} option)
	 *
	 * Dump the internal state (dispatchments of execution units, and so on) of {@link org.vcssl.nano.vm.accelerator.Accelerator}.
	 */
	public static final String DUMPER_TARGET_ACCELERATOR_STATE = "ACCELERATOR_STATE";


	/**
	 * The default value of {@link OptionKey#MAIN_SCRIPT_NAME} option.
	 */
	public static final String MAIN_SCRIPT_NAME_DEFAULT = "main script";


	/**
	 * The default value of {@link OptionKey#MAIN_SCRIPT_DIRECTORY MAIN_DIRECTORY_PATH} option.
	 */
	private static final String MAIN_DIRECTORY_PATH_DEFAULT = ".";


	/**
	 * (A value of {@link OptionKey#ACCELERATOR_OPTIMIZATION_LEVEL ACCELERATOR_OPTIMIZATION_LEVEL} option)
	 *
	 * The optimization level to execute code in the most naive way on the Accelerator.
	 */
	public static final int ACCELERATOR_OPTIMIZATION_LEVEL_0 = 0;


	/**
	 * (A value of {@link OptionKey#ACCELERATOR_OPTIMIZATION_LEVEL ACCELERATOR_OPTIMIZATION_LEVEL} option)
	 *
	 * The optimization level to optimize only I/O of data, without enabling any optimizations of code.
	 */
	public static final int ACCELERATOR_OPTIMIZATION_LEVEL_1 = 1;


	/**
	 * (A value of {@link OptionKey#ACCELERATOR_OPTIMIZATION_LEVEL ACCELERATOR_OPTIMIZATION_LEVEL} option)
	 *
	 * The optimization level to reduce overhead processing costs by replacing operands/instructions in code,
	 * by removing unnecessary instructions, and by fusing multiple instructions into a instruction.
	 */
	public static final int ACCELERATOR_OPTIMIZATION_LEVEL_2 = 2;


	/**
	 * (A value of {@link OptionKey#ACCELERATOR_OPTIMIZATION_LEVEL ACCELERATOR_OPTIMIZATION_LEVEL} option)
	 *
	 * The optimization level to enable optimizations with modifications of code structures,
	 * such as inline expansions of functions, and so on.
	 */
	public static final int ACCELERATOR_OPTIMIZATION_LEVEL_3 = 3;


	/**
	 * (A value of {@link OptionKey#ACCELERATOR_OPTIMIZATION_LEVEL ACCELERATOR_OPTIMIZATION_LEVEL} option)
	 *
	 * The maximum optimization level currently supported.
	 */
	public static final int ACCELERATOR_OPTIMIZATION_LEVEL_MAX = ACCELERATOR_OPTIMIZATION_LEVEL_3;


	/**
	 * (A value of {@link OptionKey#ACCELERATOR_OPTIMIZATION_LEVEL ACCELERATOR_OPTIMIZATION_LEVEL} option)
	 *
	 * The default optimization level.
	 */
	public static final int ACCELERATOR_OPTIMIZATION_LEVEL_DEFAULT = ACCELERATOR_OPTIMIZATION_LEVEL_3;


	/** A map contains default values of the option map. */
	private static final Map<String, Object> DEFAULT_VALUE_MAP = new LinkedHashMap<String, Object>();
	static {
		DEFAULT_VALUE_MAP.put(OptionKey.EVAL_INT_LITERAL_AS_FLOAT, Boolean.FALSE);
		DEFAULT_VALUE_MAP.put(OptionKey.EVAL_ONLY_FLOAT, Boolean.FALSE);
		DEFAULT_VALUE_MAP.put(OptionKey.EVAL_ONLY_EXPRESSION, Boolean.FALSE);
		DEFAULT_VALUE_MAP.put(OptionKey.LOCALE, Locale.getDefault());
		DEFAULT_VALUE_MAP.put(OptionKey.ACCELERATOR_ENABLED, Boolean.TRUE);
		DEFAULT_VALUE_MAP.put(OptionKey.ACCELERATOR_OPTIMIZATION_LEVEL, ACCELERATOR_OPTIMIZATION_LEVEL_DEFAULT);
		DEFAULT_VALUE_MAP.put(OptionKey.TERMINATOR_ENABLED, Boolean.FALSE);
		DEFAULT_VALUE_MAP.put(OptionKey.PERFORMANCE_MONITOR_ENABLED, Boolean.FALSE);
		DEFAULT_VALUE_MAP.put(OptionKey.DUMPER_ENABLED, Boolean.FALSE);
		DEFAULT_VALUE_MAP.put(OptionKey.DUMPER_TARGET, DUMPER_TARGET_ALL);
		DEFAULT_VALUE_MAP.put(OptionKey.DUMPER_STREAM, System.out);
		DEFAULT_VALUE_MAP.put(OptionKey.RUNNING_ENABLED, Boolean.TRUE);
		DEFAULT_VALUE_MAP.put(OptionKey.AUTOMATIC_ACTIVATION_ENABLED, Boolean.TRUE);
		DEFAULT_VALUE_MAP.put(OptionKey.MAIN_SCRIPT_NAME, MAIN_SCRIPT_NAME_DEFAULT);
		DEFAULT_VALUE_MAP.put(OptionKey.MAIN_SCRIPT_DIRECTORY, MAIN_DIRECTORY_PATH_DEFAULT);
		DEFAULT_VALUE_MAP.put(OptionKey.FILE_IO_ENCODING, "UTF-8");
		DEFAULT_VALUE_MAP.put(OptionKey.FILE_IO_EOL, System.getProperty("line.separator"));
		DEFAULT_VALUE_MAP.put(OptionKey.UI_MODE, "GUI");
		DEFAULT_VALUE_MAP.put(OptionKey.TERMINAL_IO_EOL, System.getProperty("line.separator"));
		DEFAULT_VALUE_MAP.put(OptionKey.ENVIRONMENT_EOL, System.getProperty("line.separator"));
		DEFAULT_VALUE_MAP.put(OptionKey.STDIN_STREAM, System.in);
		DEFAULT_VALUE_MAP.put(OptionKey.STDOUT_STREAM, System.out);
		DEFAULT_VALUE_MAP.put(OptionKey.STDERR_STREAM, System.err);
	}


	/**
	 * Normalizes contents of a specified option map, to the expected format in the script engine.
	 *
	 * For example, some items will be supplemented with defailt values if they are not contained in the map.
	 * In addition, specific characters in values will be escaped for some items.
	 *
	 * @param optionMap The option map to be normalized.
	 * @return The normalized option map.
	 */
	public static final Map<String, Object> normalizeValuesOf(Map<String, Object> optionMap) {

		// Create a Map to be returned.
		Map<String, Object> returnMap = new LinkedHashMap<String, Object>();

		// Copy contents of inputMap to returnMap.
		Set<Entry<String, Object>> inputEntrySet = optionMap.entrySet();
		for (Entry<String, Object> entry: inputEntrySet) {
			returnMap.put(entry.getKey(), entry.getValue());
		}

		// Supplement default values, whiches are stored in DEFAULT_VALUE_MAP.
		Set<Entry<String, Object>> defaultEntrySet = DEFAULT_VALUE_MAP.entrySet();
		for (Entry<String, Object> entry: defaultEntrySet) {
			if (!returnMap.containsKey(entry.getKey())) {
				returnMap.put(entry.getKey(), entry.getValue());
			}
		}

		// Replace special characters in the main script name.
		if (returnMap.get(OptionKey.MAIN_SCRIPT_NAME) instanceof String) {
			String mainScriptName = (String)returnMap.get(OptionKey.MAIN_SCRIPT_NAME);
			returnMap.put(
				OptionKey.MAIN_SCRIPT_NAME,
				IdentifierSyntax.normalizeScriptIdentifier(mainScriptName)
			);
		}

		return returnMap;
	}


	/**
	 * Checks contents (keys and values) of items stored in an option map.
	 *
	 * @param optionMap The option map to be checked.
	 * @throws VnanoException Thrown if invalid contents are detected.
	 */
	public static final void checkContentsOf(Map<String, Object> optionMap) throws VnanoException {
		checkKeysOf(optionMap);
		checkValuesOf(optionMap);
	}


	/**
	 * Checks keys (option names) of items stored in an option map.
	 *
	 * @param optionMap The option map to be checked.
	 * @throws VnanoException Thrown if invalid names are detected.
	 */
	public static final void checkKeysOf(Map<String, Object> optionMap) throws VnanoException {

		// Old option names
		if(optionMap.containsKey("EVAL_NUMBER_AS_FLOAT")) {
			throw new VnanoException(
				ErrorType.OPTION_KEY_HAD_CHANGED,  new String[] { "EVAL_NUMBER_AS_FLOAT", OptionKey.EVAL_INT_LITERAL_AS_FLOAT }
			);
		}
		if(optionMap.containsKey("MAIN_DIRECTORY_PATH")) {
			throw new VnanoException(
				ErrorType.OPTION_KEY_HAD_CHANGED, new String[] { "MAIN_DIRECTORY_PATH", OptionKey.MAIN_SCRIPT_DIRECTORY }
			);
		}
	}


	/**
	 * Checks values of items stored in an option map.
	 *
	 * @param optionMap The option map to be checked.
	 * @throws VnanoException Thrown if invalid values are detected.
	 */
	private static final void checkValuesOf(Map<String, Object> optionMap) throws VnanoException {
		checkValueOf(OptionKey.EVAL_INT_LITERAL_AS_FLOAT, optionMap, Boolean.class);
		checkValueOf(OptionKey.EVAL_ONLY_FLOAT, optionMap, Boolean.class);
		checkValueOf(OptionKey.EVAL_ONLY_EXPRESSION, optionMap, Boolean.class);
		checkValueOf(OptionKey.LOCALE, optionMap, Locale.class);
		checkValueOf(OptionKey.ACCELERATOR_ENABLED, optionMap, Boolean.class);
		checkValueOf(OptionKey.TERMINATOR_ENABLED, optionMap, Boolean.class);
		checkValueOf(OptionKey.PERFORMANCE_MONITOR_ENABLED, optionMap, Boolean.class);
		checkValueOf(OptionKey.DUMPER_ENABLED, optionMap, Boolean.class);
		checkValueOf(OptionKey.DUMPER_TARGET, optionMap, String.class);
		checkValueOf(OptionKey.DUMPER_STREAM, optionMap, PrintStream.class);
		checkValueOf(OptionKey.RUNNING_ENABLED, optionMap, Boolean.class);
		checkValueOf(OptionKey.MAIN_SCRIPT_NAME, optionMap, String.class);
		checkValueOf(OptionKey.MAIN_SCRIPT_DIRECTORY, optionMap, String.class);
		checkValueOf(OptionKey.FILE_IO_ENCODING, optionMap, String.class);
		checkValueOf(OptionKey.FILE_IO_EOL, optionMap, String.class);
		checkValueOf(OptionKey.UI_MODE, optionMap, String.class);
		checkValueOf(OptionKey.TERMINAL_IO_EOL, optionMap, String.class);
		checkValueOf(OptionKey.ENVIRONMENT_EOL, optionMap, String.class);
		checkValueOf(OptionKey.STDIN_STREAM, optionMap, InputStream.class);
		checkValueOf(OptionKey.STDOUT_STREAM, optionMap, PrintStream.class);
		checkValueOf(OptionKey.STDERR_STREAM, optionMap, PrintStream.class);
	}


	/**
	 * Checks the content of an item of an option map.
	 *
	 * @param optionKey The key of an item to be checked.
	 * @param optionMap The option map to be checked.
	 * @param optionMap The expected class of the value.
	 * @throws VnanoException Thrown if the value is invalid, or does not exist.
	 */
	private static final void checkValueOf(String optionKey, Map<String, Object> optionMap, Class<?> classOfValue)
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

}

