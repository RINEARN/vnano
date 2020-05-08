package org.vcssl.nano.spec;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.vcssl.nano.VnanoException;

// Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/spec/OptionValue.html
// ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/spec/OptionValue.html

/**
 * <p>
 * <span class="lang-en">The class to define values of the option map (option values)</span>
 * <span class="lang-ja">オプションマップの値（オプション値）が定義されたクラスです</span>
 * .
 * </p>
 *
 * <p>
 * &raquo; <a href="../../../../../src/org/vcssl/nano/spec/OptionValue.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/spec/OptionValue.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/spec/OptionValue.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class OptionValue {


	/**
	 * <span class="lang-en">
	 * (A value of {@link OptionKey#DUMPER_TARGET DUMPER_TARGET} option)
	 * Dump all contents
	 * </span>
	 * <span class="lang-ja">
	 * ({@link OptionKey#DUMPER_TARGET DUMPER_TARGET} オプションの値)
	 * 全ての内容をダンプします
	 * </span>
	 * .
	 * <span class="lang-en">The value is "ALL".</span>
	 * <span class="lang-ja">値は "ALL" です.</span>
	 */
	public static final String DUMPER_TARGET_ALL = "ALL";


	/**
	 * <span class="lang-en">
	 * (A value of {@link OptionKey#DUMPER_TARGET DUMPER_TARGET} option)
	 * Dump the inutted script code
	 * </span>
	 * <span class="lang-ja">
	 * ({@link OptionKey#DUMPER_TARGET DUMPER_TARGET} オプションの値)
	 * 入力されたスクリプトコードをそのままダンプします
	 * </span>
	 * .
	 * <span class="lang-en">The value is "INPUTTED_CODE".</span>
	 * <span class="lang-ja">値は "INPUTTED_CODE" です.</span>
	 */
	public static final String DUMPER_TARGET_INPUTTED_CODE = "INPUTTED_CODE";


	/**
	 * <span class="lang-en">
	 * (A value of {@link OptionKey#DUMPER_TARGET DUMPER_TARGET} option)
	 * Dump pre-processed script code
	 * </span>
	 * <span class="lang-ja">
	 * ({@link OptionKey#DUMPER_TARGET DUMPER_TARGET} オプションの値)
	 * プリプロセス済みのスクリプトコードをダンプします
	 * </span>
	 * .
	 * <span class="lang-en">The value is "PREPROCESSED_CODE".</span>
	 * <span class="lang-ja">値は "PREPROCESSED_CODE" です.</span>
	 */
	public static final String DUMPER_TARGET_PREPROCESSED_CODE = "PREPROCESSED_CODE";


	/**
	 * <span class="lang-en">
	 * (A value of {@link OptionKey#DUMPER_TARGET DUMPER_TARGET} option)
	 * Dump tokens, which are output of the {@link org.vcssl.nano.compiler.LexicalAnalyzer LexicalAnalyzer}
	 * </span>
	 * <span class="lang-ja">
	 * ({@link OptionKey#DUMPER_TARGET DUMPER_TARGET} オプションの値)
	 * {@link org.vcssl.nano.compiler.LexicalAnalyzer LexicalAnalyzer} が出力したトークン配列をダンプします
	 * </span>
	 * .
	 * <span class="lang-en">The value is "TOKEN".</span>
	 * <span class="lang-ja">値は "TOKEN" です.</span>
	 */
	public static final String DUMPER_TARGET_TOKEN = "TOKEN";


	/**
	 * <span class="lang-en">
	 * (A value of {@link OptionKey#DUMPER_TARGET DUMPER_TARGET} option)
	 * Dump the Abstract Syntax Tree (AST), which is the output of the {@link org.vcssl.nano.compiler.Parser Parser}
	 * </span>
	 * <span class="lang-ja">
	 * ({@link OptionKey#DUMPER_TARGET DUMPER_TARGET} オプションの値)
	 * {@link org.vcssl.nano.compiler.Parser Parser} が出力した抽象構文木（AST）をダンプします
	 * </span>
	 * .
	 * <span class="lang-en">The value is "PARSED_AST".</span>
	 * <span class="lang-ja">値は "PARSED_AST" です.</span>
	 */
	public static final String DUMPER_TARGET_PARSED_AST = "PARSED_AST";


	/**
	 * <span class="lang-en">
	 * (A value of {@link OptionKey#DUMPER_TARGET DUMPER_TARGET} option)
	 * Dump the semantic-analyzed AST, which is the output of the
	 * {@link org.vcssl.nano.compiler.SemanticAnalyzer SemanticAnalyzer}
	 * </span>
	 * <span class="lang-ja">
	 * ({@link OptionKey#DUMPER_TARGET DUMPER_TARGET} オプションの値)
	 * {@link org.vcssl.nano.compiler.SemanticAnalyzer SemanticAnalyzer}
	 * が出力した, 意味解析済みのASTをダンプします
	 * </span>
	 * .
	 * <span class="lang-en">The value is "ANALYZED_AST".</span>
	 * <span class="lang-ja">値は "ANALYZED_AST" です.</span>
	 */
	public static final String DUMPER_TARGET_ANALYZED_AST = "ANALYZED_AST";


	/**
	 * <span class="lang-en">
	 * (A value of {@link OptionKey#DUMPER_TARGET DUMPER_TARGET} option)
	 * Dump the VRIL code, which is the compilation result, output of the
	 * {@link org.vcssl.nano.compiler.CodeGenerator CodeGenerator}
	 * </span>
	 * <span class="lang-ja">
	 * ({@link OptionKey#DUMPER_TARGET DUMPER_TARGET} オプションの値)
	 * {@link org.vcssl.nano.compiler.CodeGenerator CodeGenerator}
	 * が出力した, コンパイル結果であるVRILコードをダンプします
	 * </span>
	 * .
	 * <span class="lang-en">The value is "ASSEMBLY_CODE".</span>
	 * <span class="lang-ja">値は "ASSEMBLY_CODE" です.</span>
	 */
	public static final String DUMPER_TARGET_ASSEMBLY_CODE = "ASSEMBLY_CODE";


	/**
	 * <span class="lang-en">
	 * (A value of {@link OptionKey#DUMPER_TARGET DUMPER_TARGET} option)
	 * Dump the VM object code, output of the {@link org.vcssl.nano.vm.assembler.Assembler Assembler}
	 * </span>
	 * <span class="lang-ja">
	 * ({@link OptionKey#DUMPER_TARGET DUMPER_TARGET} オプションの値)
	 * {@link org.vcssl.nano.vm.assembler.Assembler Assembler} が出力した, VMオブジェクトコードをダンプします
	 * </span>
	 * .
	 * <span class="lang-en">The value is "OBJECT_CODE".</span>
	 * <span class="lang-ja">値は "OBJECT_CODE" です.</span>
	 */
	public static final String DUMPER_TARGET_OBJECT_CODE = "OBJECT_CODE";


	/**
	 * <span class="lang-en">
	 * (A value of {@link OptionKey#DUMPER_TARGET DUMPER_TARGET} option)
	 * Dump instructions of {@link org.vcssl.nano.vm.accelerator.Accelerator Accelerator},
	 * output of the {@link org.vcssl.nano.vm.accelerator.AcceleratorSchedulingUnit AcceleratorSchedulingUnit}
	 * </span>
	 * <span class="lang-ja">
	 * ({@link OptionKey#DUMPER_TARGET DUMPER_TARGET} オプションの値)
	 * {@link org.vcssl.nano.vm.accelerator.AcceleratorSchedulingUnit AcceleratorSchedulingUnit} が出力した,
	 * {@link org.vcssl.nano.vm.accelerator.Accelerator Accelerator} 用の命令列をダンプします
	 * </span>
	 * .
	 * <span class="lang-en">The value is "ACCELERATOR_CODE".</span>
	 * <span class="lang-ja">値は "ACCELERATOR_CODE" です.</span>
	 */
	public static final String DUMPER_TARGET_ACCELERATOR_CODE = "ACCELERATOR_CODE";


	/**
	 * <span class="lang-en">
	 * (A value of {@link OptionKey#DUMPER_TARGET DUMPER_TARGET} option)
	 * Dump the internal state (dispatchments of execution units, and so on) of
	 * {@link org.vcssl.nano.vm.accelerator.Accelerator Accelerator}
	 * </span>
	 * <span class="lang-ja">
	 * ({@link OptionKey#DUMPER_TARGET DUMPER_TARGET} オプションの値)
	 * {@link org.vcssl.nano.vm.accelerator.Accelerator Accelerator}
	 * の内部状態（演算器の割り当て内容など）をダンプします
	 * </span>
	 * .
	 * <span class="lang-en">The value is "ACCELERATOR_STATE".</span>
	 * <span class="lang-ja">値は "ACCELERATOR_STATE" です.</span>
	 */
	public static final String DUMPER_TARGET_ACCELERATOR_STATE = "ACCELERATOR_STATE";


	/**
	 * <span class="lang-en">The default value of {@link OptionKey#EVAL_SCRIPT_NAME EVAL_SCRIPT_NAME} option</span>
	 * <span class="lang-ja">{@link OptionKey#EVAL_SCRIPT_NAME EVAL_SCRIPT_NAME} オプションのデフォルト値です</span>
	 * .
	 */
	private static final String DEFAULT_EVAL_SCRIPT_NAME = "EVAL_SCRIPT";


	/**
	 * <span class="lang-en">A map contains default values of the option map</span>
	 * <span class="lang-ja">オプションマップのデフォルト値を保持するマップです</span>
	 * .
	 */
	private static final Map<String, Object> DEFAULT_VALUE_MAP = new LinkedHashMap<String, Object>();
	static {
		DEFAULT_VALUE_MAP.put(OptionKey.EVAL_SCRIPT_NAME, "EVAL_SCRIPT");
		DEFAULT_VALUE_MAP.put(OptionKey.EVAL_NUMBER_AS_FLOAT, Boolean.FALSE);
		DEFAULT_VALUE_MAP.put(OptionKey.EVAL_ONLY_FLOAT, Boolean.FALSE);
		DEFAULT_VALUE_MAP.put(OptionKey.EVAL_ONLY_EXPRESSION, Boolean.FALSE);
		DEFAULT_VALUE_MAP.put(OptionKey.LOCALE, Locale.getDefault());
		DEFAULT_VALUE_MAP.put(OptionKey.ACCELERATOR_ENABLED, Boolean.TRUE);
		DEFAULT_VALUE_MAP.put(OptionKey.DUMPER_ENABLED, Boolean.FALSE);
		DEFAULT_VALUE_MAP.put(OptionKey.DUMPER_TARGET, DUMPER_TARGET_ALL);
		DEFAULT_VALUE_MAP.put(OptionKey.DUMPER_STREAM, System.out);
		DEFAULT_VALUE_MAP.put(OptionKey.RUNNING_ENABLED, Boolean.TRUE);
		DEFAULT_VALUE_MAP.put(OptionKey.EVAL_SCRIPT_NAME, DEFAULT_EVAL_SCRIPT_NAME);
	}


	/**
	 * <span class="lang-en">
	 * Normalizes contents of a specified option map, to the expected format in the script engine
	 * </span>
	 * <span class="lang-ja">
	 * 指定されたオプションマップの内容を, この処理系内での各処理において期待される状態に正規化します
	 * </span>
	 * .
	 *
	 * <span class="lang-en">
	 * For example, some items will be supplemented with defailt values if they are not contained in the map.
	 * In addition, specific characters in values will be escaped for some items.
	 * </span>
	 *
	 * <span class="lang-ja">
	 * 具体的には, 未指定の項目をデフォルト値で補完したり, 一部項目内の特殊文字をエスケープしたりします.
	 * </span>
	 *
	 * @param optionMap
	 *   <span class="lang-en">The option map to be normalized</span>
	 *   <span class="lang-ja">正規化したいオプションマップ</span>
	 *
	 * @return
	 *   <span class="lang-en">The normalized option map</span>
	 *   <span class="lang-ja">正規化されたオプションマップ</span>
	 */
	public static Map<String, Object> normalizeValuesOf(
			Map<String, Object> optionMap, LanguageSpecContainer langSpec) {

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

		// スクリプト名の中の特殊文字をエスケープ（VRILコード内にメタ情報として記載されるため）
		if (returnMap.get(OptionKey.EVAL_SCRIPT_NAME) instanceof String) {
			String evalScriptName = (String)returnMap.get(OptionKey.EVAL_SCRIPT_NAME);
			returnMap.put(
				OptionKey.EVAL_SCRIPT_NAME,
				langSpec.IDENTIFIER_SYNTAX.normalizeScriptIdentifier(evalScriptName)
			);
		}

		return returnMap;
	}


	/**
	 * <span class="lang-en">
	 * Checks contents of all items of an option map
	 * </span>
	 * <span class="lang-ja">
	 * オプションマップの全ての項目の内容を検査します
	 * </span>
	 * .
	 *
	 * @param optionMap
	 *   <span class="lang-en">The option map to be checked</span>
	 *   <span class="lang-ja">検査したいオプションマップ</span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">Thrown if invalid contents are detected.</span>
	 *   <span class="lang-ja">無効な内容が検出された場合にスローされます.</span>
	 */
	public static void checkValuesOf(Map<String, Object> optionMap) throws VnanoException {
		checkValueOf(OptionKey.EVAL_SCRIPT_NAME, optionMap, String.class);
		checkValueOf(OptionKey.EVAL_NUMBER_AS_FLOAT, optionMap, Boolean.class);
		checkValueOf(OptionKey.EVAL_ONLY_FLOAT, optionMap, Boolean.class);
		checkValueOf(OptionKey.EVAL_ONLY_EXPRESSION, optionMap, Boolean.class);
		checkValueOf(OptionKey.LOCALE, optionMap, Locale.class);
		checkValueOf(OptionKey.ACCELERATOR_ENABLED, optionMap, Boolean.class);
		checkValueOf(OptionKey.DUMPER_ENABLED, optionMap, Boolean.class);
		checkValueOf(OptionKey.DUMPER_TARGET, optionMap, String.class);
		checkValueOf(OptionKey.DUMPER_STREAM, optionMap, PrintStream.class);
		checkValueOf(OptionKey.RUNNING_ENABLED, optionMap, Boolean.class);
	}


	/**
	 * <span class="lang-en">
	 * Checks the content of an item of an option map
	 * </span>
	 * <span class="lang-ja">
	 * オプションマップの特定の項目の内容を検査します
	 * </span>
	 * .
	 *
	 * @param optionKey
	 *   <span class="lang-en">The key of an item to be checked</span>
	 *   <span class="lang-ja">検査したい項目のキー</span>
	 *
	 * @param optionMap
	 *   <span class="lang-en">The option map to be checked</span>
	 *   <span class="lang-ja">検査したいオプションマップ</span>
	 *
	 * @param optionMap
	 *   <span class="lang-en">The expected class of the value</span>
	 *   <span class="lang-ja">値に期待されているクラス</span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">Thrown if the value is invalid, or does not exist.</span>
	 *   <span class="lang-ja">値が無効か, または存在しない場合にスローされます.</span>
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

}
