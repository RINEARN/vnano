/*
 * Copyright(C) 2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.main;

import java.util.List;
import java.util.Locale;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.spec.SpecialBindingKey;
import org.vcssl.nano.spec.EngineInformation;
import org.vcssl.nano.spec.ErrorMessage;
import org.vcssl.nano.spec.OptionKey;
import org.vcssl.nano.spec.OptionValue;
import org.vcssl.nano.vm.VirtualMachine;

public final class VnanoCommandLineApplication {

	/**
	 * エラーメッセージの表示言語指定などに使用されるロケールを保持します。
	 */
	private Locale locale = Locale.getDefault();

	private static final String EXTENSION_VNANO = ".vnano";
	private static final String EXTENSION_VRIL = ".vril";

	private static final String OPTION_NAME_PREFIX = "--";
	private static final String OPTION_NAME_FILE = "file";
	private static final String OPTION_NAME_HELP = "help";
	private static final String OPTION_NAME_DUMP = "dump";
	private static final String OPTION_NAME_RUN = "run";
	private static final String OPTION_NAME_LOCALE = "locale";
	private static final String OPTION_NAME_ACCELERATOR = "accelerator";
	private static final String OPTION_NAME_ENCODING = "encoding";
	private static final String OPTION_NAME_DEFAULT = OPTION_NAME_FILE;

	private static final String DUMP_TARGET_INPUTTED_CODE = "inputtedCode";
	private static final String DUMP_TARGET_PREPROCESSED_CODE = "preprocessedCode";
	private static final String DUMP_TARGET_TOKEN = "token";
	private static final String DUMP_TARGET_PARSED_AST = "parsedAst";
	private static final String DUMP_TARGET_ANALYZED_AST = "analyzedAst";
	private static final String DUMP_TARGET_ASSEMBLY_CODE = "assemblyCode";
	private static final String DUMP_TARGET_OBJECT_CODE = "objectCode";
	private static final String DUMP_TARGET_ACCELERATOR_CODE = "acceleratorCode";
	private static final String DUMP_TARGET_ACCELERATOR_STATE = "acceleratorState";
	private static final String DUMP_TARGET_ALL = "all";
	private static final String DUMP_TARGET_DEFAULT = DUMP_TARGET_ALL;

	// コマンドラインでの--dumpオプションの値を、スクリプトエンジンのオプションマップ用の値に変換するマップ
	private static final Map<String, String> DUMP_TARGET_ARGVALUE_OPTVALUE_MAP = new HashMap<String, String>();
	static {
		DUMP_TARGET_ARGVALUE_OPTVALUE_MAP.put(DUMP_TARGET_INPUTTED_CODE, OptionValue.DUMPER_TARGET_INPUTTED_CODE);
		DUMP_TARGET_ARGVALUE_OPTVALUE_MAP.put(DUMP_TARGET_PREPROCESSED_CODE, OptionValue.DUMPER_TARGET_PREPROCESSED_CODE);
		DUMP_TARGET_ARGVALUE_OPTVALUE_MAP.put(DUMP_TARGET_TOKEN, OptionValue.DUMPER_TARGET_TOKEN);
		DUMP_TARGET_ARGVALUE_OPTVALUE_MAP.put(DUMP_TARGET_PARSED_AST, OptionValue.DUMPER_TARGET_PARSED_AST);
		DUMP_TARGET_ARGVALUE_OPTVALUE_MAP.put(DUMP_TARGET_ANALYZED_AST, OptionValue.DUMPER_TARGET_ANALYZED_AST);
		DUMP_TARGET_ARGVALUE_OPTVALUE_MAP.put(DUMP_TARGET_ASSEMBLY_CODE, OptionValue.DUMPER_TARGET_ASSEMBLY_CODE);
		DUMP_TARGET_ARGVALUE_OPTVALUE_MAP.put(DUMP_TARGET_OBJECT_CODE, OptionValue.DUMPER_TARGET_OBJECT_CODE);
		DUMP_TARGET_ARGVALUE_OPTVALUE_MAP.put(DUMP_TARGET_ACCELERATOR_CODE, OptionValue.DUMPER_TARGET_ACCELERATOR_CODE);
		DUMP_TARGET_ARGVALUE_OPTVALUE_MAP.put(DUMP_TARGET_ACCELERATOR_STATE, OptionValue.DUMPER_TARGET_ACCELERATOR_STATE);
		DUMP_TARGET_ARGVALUE_OPTVALUE_MAP.put(DUMP_TARGET_ALL, OptionValue.DUMPER_TARGET_ALL);
	}

	private HashMap<String, Object> optionMap = new HashMap<String, Object>();

	private String encoding = null;

	// スクリプトからアクセスするメソッドを提供するクラス
	public class ScriptIO {
		private long launchedTime = System.nanoTime() / 1000000l;

		public void output(long value) {
			System.out.print(value);
		}
		public void output(double value) {
			System.out.print(value);
		}
		public void output(boolean value) {
			System.out.print(value);
		}
		public void output(String value) {
			System.out.print(value);
		}
		public long time() {
			return System.nanoTime() / 1000000l - launchedTime;
		}
	}

	public static void main(String[] args) {
		//args = new String[] { "Example.vnano", "--dump" };
		VnanoCommandLineApplication application = new VnanoCommandLineApplication();
		application.dispatch(args);
	}

	public VnanoCommandLineApplication() {
	}

	public void help() {
		System.out.print("Vnano " + EngineInformation.ENGINE_VERSION);
		System.out.println("  (Command-Line Mode for Developments and Debuggings)");

		System.out.println("");
		System.out.println("[ Usage ]");
		System.out.println("");
		System.out.println("      java -jar Vnano.jar fileName");
		System.out.println("");
		System.out.println("        or,");
		System.out.println("");
		System.out.println("      java -jar Vnano.jar fileName --option1 value1 --option2 value2 ...");
		System.out.println("");
		System.out.println("[ Options ]");
		System.out.println("");

		System.out.println("  --help");
		System.out.println("");
		System.out.println("    Show this help messages.");
		System.out.println("");
		System.out.println("");

		System.out.println("  --file <filePath>");
		System.out.println("");
		System.out.println("    Load the script code (.vnano) or the VRIL code (.vril) from the file.");
		System.out.println("    This is the default option for 1-argument.");
		System.out.println("    You can skip to describe this option name for simplicity of the commands.");
		System.out.println("");
		System.out.println("    e.g.");
		System.out.println("");
		System.out.println("      java -jar Vnano.jar --file Example.vnano");
		System.out.println("      java -jar Vnano.jar --file Example.vril");
		System.out.println("      java -jar Vnano.jar Example.vnano");
		System.out.println("      java -jar Vnano.jar Example.vril");
		System.out.println("");
		System.out.println("");

		System.out.println("  --encoding <encodingName>");
		System.out.println("");
		System.out.println("      Specify the text-encoding of the script file.");
		System.out.println("      The default text-encoding on this mode is UTF-8.");
		System.out.println("");
		System.out.println("    e.g.");
		System.out.println("");
		System.out.println("      java -jar Vnano.jar Example.vnano --encoding UTF-8");
		System.out.println("      java -jar Vnano.jar Example.vnano --encoding Shift_JIS");
		System.out.println("");
		System.out.println("");

		System.out.println("  --locale <localeCode>");
		System.out.println("");
		System.out.println("      Specify the locale to determine the language of error messages.");
		System.out.println("      The default locale depends on your environment.");
		System.out.println("");
		System.out.println("    e.g.");
		System.out.println("");
		System.out.println("      java -jar Vnano.jar Example.vnano --locale En-US");
		System.out.println("      java -jar Vnano.jar Example.vnano --locale Ja-JP");
		System.out.println("");
		System.out.println("");

		System.out.println("  --dump <dumpTarget>");
		System.out.println("");
		System.out.println("      Dump the intermediate information to the standard output.");
		System.out.println("      You can choose and specify the <dumpTarget> from the following list:");
		System.out.println("");
		System.out.println("        inputtedCode     : Content of the script code loaded from the file.");
		System.out.println("        preprocessedCode : Comment-removed script code generated by the preprocessor.");
		System.out.println("        token            : Tokens generated by the lexical analyzer.");
		System.out.println("        parsedAst        : Abstract Syntax Tree (AST) generated by the parser.");
		System.out.println("        analyzedAst      : Information-appended AST generated by the semantic analyzer.");
		System.out.println("        assemblyCode     : Virtual assembly code written in the VRIL, generated by the code generator.");
		System.out.println("        objectCode       : Virtual object code running on the VM, generated by the assembler.");
		System.out.println("        acceleratorCode  : Optimized instruction code running on the VM when the accelerator is enabled.");
		System.out.println("        acceleratorState : Internal state of the accelerator.");
		System.out.println("        all (default)    : All of the above dump targets.");
		System.out.println("");
		System.out.println("    e.g.");
		System.out.println("");
		System.out.println("      java -jar Vnano.jar Example.vnano --dump");
		System.out.println("      java -jar Vnano.jar Example.vnano --dump all");
		System.out.println("      java -jar Vnano.jar Example.vnano --dump assemblyCode");
		System.out.println("");
		System.out.println("");

		System.out.println("  --run <runOrNot>");
		System.out.println("");
		System.out.println("      Specify whether you want to run the code loaded from the file or not.");
		System.out.println("      This option is specified by default, and the default value is true.");
		System.out.println("      You can choose and specify the value of <runOrNot> from the followings:");
		System.out.println("");
		System.out.println("        true (default) : Run the code.");
		System.out.println("        false          : Don't run the code.");
		System.out.println("");
		System.out.println("      This option is useful for the combination usage with the --dump option.");
		System.out.println("");
		System.out.println("    e.g.");
		System.out.println("");
		System.out.println("      java -jar Vnano.jar Example.vnano --run false");
		System.out.println("      java -jar Vnano.jar Example.vnano --run false --dump assemblyCode");
		System.out.println("      java -jar Vnano.jar Example.vnano --run false --dump assemblyCode > debug.txt");
		System.out.println("");
		System.out.println("");

		System.out.println("  --accelerator <enableOrDisable>");
		System.out.println("");
		System.out.println("      Specify whether you want to enable the accelerator.");
		System.out.println("      This option is specified by default, and the default value is true.");
		System.out.println("      You can choose and specify the value of <enableOrDisable> from the followings:");
		System.out.println("");
		System.out.println("        true (default) : Enable the accelerator.");
		System.out.println("        false          : Disable the accelerator.");
		System.out.println("");
		System.out.println("    e.g.");
		System.out.println("");
		System.out.println("      java -jar Vnano.jar Example.vnano --accelerator true");
		System.out.println("      java -jar Vnano.jar Example.vnano --accelerator false");
		System.out.println("");

		System.out.println("[ Supported Functions ]");
		System.out.println("");
		System.out.println("    For development and debugging, following functions are available");
		System.out.println("    in the script code running on this mode:");
		System.out.println("");
		System.out.println("      void output(int)");
		System.out.println("      void output(long)");
		System.out.println("      void output(float)");
		System.out.println("      void output(double)");
		System.out.println("      void output(bool)");
		System.out.println("      void output(string)");
		System.out.println("      int  time()");
		System.out.println("");
		System.out.println("    Please note that these functions are supported by default ONLY ON THIS MODE.");
		System.out.println("    No default functions are supported when the script engine is embedded in ");
		System.out.println("    other applications, so it is necessary to implement and connect ");
		System.out.println("    functions(methods) you want to use in the other application to the script engine.");
		System.out.println("");
		System.out.println("    Please see the source code of:");
		System.out.println("");
		System.out.println("      src/org/vcssl/nano/main/VnanoCommanLineApplication.java");
		System.out.println("");
		System.out.println("    as a reference.");
		System.out.println("");
	}

	public void dispatch(String[] args) {
		int argLength = args.length;

		// 引数が無ければヘルプを表示して終了
		if (argLength == 0) {
			this.help();
			return;
		}

		// 引数が--helpの場合もヘルプを表示して終了
		if (argLength == 1 && args[0].equals(OPTION_NAME_PREFIX + OPTION_NAME_HELP)) {
			this.help();
			return;
		}

		// 引数を解釈し、オプション名をキーとしてオプション値を返すマップを取得
		Map<String, String> optionNameValueMap = this.parseArguments(args);

		// 読み込むファイルのパス（デフォルトオプションなので名称未指定の引数もこれに該当）を取得
		if (!optionNameValueMap.containsKey(OPTION_NAME_FILE)) {
			System.err.println("No script file is specified.");
		}
		String inputFilePath = optionNameValueMap.get(OPTION_NAME_FILE);

		// オプションを一つずつ読み、対応する処理を実行する
		boolean optionProcessingSucceeded = true;
		Set<Map.Entry<String, String>> optionNameValueSet = optionNameValueMap.entrySet();
		for (Map.Entry<String, String> optionNameValuePair : optionNameValueSet) {
			String optionName = optionNameValuePair.getKey();
			String optionValue = optionNameValuePair.getValue();
			optionProcessingSucceeded &= this.dispatchOptionProcessing(optionName, optionValue, inputFilePath);
		}

		// スクリプトの実行が必要なら実行する（ただし、オプション処理に失敗していた場合は実行しない）
		if (optionProcessingSucceeded) {
			this.executeFile(inputFilePath);
		}
	}


	// 戻り値は成功:true/失敗:false
	public boolean dispatchOptionProcessing (String optionName, String optionValue, String inputFilePath) {
		if (optionName == null) {
			System.err.println("Fatal error: option name is null.");
			return false;
		}
		switch (optionName) {

			// --file または無名（デフォルト）オプションの場合
			case OPTION_NAME_FILE : {
				// このオプションの値は、事前に dispatch 側で取得され、
				// スクリプトの実行が必要な場合もそちら側で行うため、
				// ここでは何もしない
				return true;
			}

			// --help オプションの場合
			case OPTION_NAME_HELP : {
				this.help();
				return true;
			}

			// --run オプションの場合
			case OPTION_NAME_RUN : {
				this.optionMap.put(OptionKey.RUNNING_ENABLED, Boolean.valueOf(optionValue));
				return true;
			}

			// --accelerator オプションの場合
			case OPTION_NAME_ACCELERATOR : {
				if (optionValue.equals("true") || optionValue.equals("false")) {
					this.optionMap.put(OptionKey.ACCELERATOR_ENABLED, Boolean.valueOf(optionValue));
				} else {
					System.err.println(
							"Invalid value for " + OPTION_NAME_PREFIX + OPTION_NAME_ACCELERATOR + "option: " + optionValue
					);
					return false;
				}
				return true;
			}

			// --locale オプションの場合
			case OPTION_NAME_LOCALE : {
				// 先頭と末尾以外に「 - 」がある場合は、言語コードと国コードの区切りなので、分割して解釈
				if (0 < optionValue.indexOf("-") && optionValue.indexOf("-") < optionValue.length()-1) {
					String[] localeStrings = optionValue.split("-");
					this.optionMap.put(OptionKey.LOCALE, new Locale(localeStrings[0], localeStrings[1]));

				// それ以外は言語コードとして解釈
				} else {
					this.optionMap.put(OptionKey.LOCALE, new Locale(optionValue));
				}
				return true;
			}

			// --encoding オプションの場合
			case OPTION_NAME_ENCODING : {
				this.setEncoding(optionValue);
				return true;
			}

			// --dump オプションの場合
			case OPTION_NAME_DUMP : {
				if (optionValue == null) {
					optionValue = DUMP_TARGET_DEFAULT;
				}
				String convertedOptionValue = null;
				if (DUMP_TARGET_ARGVALUE_OPTVALUE_MAP.containsKey(optionValue)) {
					convertedOptionValue = DUMP_TARGET_ARGVALUE_OPTVALUE_MAP.get(optionValue);
				} else {
					System.err.println("Invalid value for " + OPTION_NAME_PREFIX + OPTION_NAME_DUMP + " option: " + optionValue);
					return false;
				}
				this.optionMap.put(OptionKey.DUMPER_TARGET, convertedOptionValue);
				this.optionMap.put(OptionKey.DUMPER_ENABLED, Boolean.valueOf(true));
				return true;
			}

			// その他のオプションの場合
			default : {
				System.err.println("Unknown option name: " + optionName);
				return false;
			}
		}
	}

	private Map<String, String> parseArguments(String[] args) {
		int argLength = args.length;

		List<String> optionNameList = new LinkedList<String>();

		// オプションの名前をキーとし、その指定内容を値とする紐づけるマップ
		HashMap<String, String> optionNameValueMap = new HashMap<String, String>();

		// オプション名の指定（「--」で始まる引数）があった場合に内容を控え、値を読んでマップ登録する際に使う
		boolean currentArgIsOption = false;
		String currentOptionName = null;

		// 全ての引数を先頭から読んでいく
		for (int argIndex=0; argIndex<argLength; argIndex++) {

			// オプションプレフィックス(--)で始まる場合は、オプション名の指定と見なし、次引数の解釈のために保持
			if (args[argIndex].startsWith(OPTION_NAME_PREFIX)) {
				currentArgIsOption = true;
				currentOptionName = args[argIndex].substring(OPTION_NAME_PREFIX.length(), args[argIndex].length());
				optionNameList.add(currentOptionName);

			} else {
				// 事前にオプション名が指定されていた場合は、その名前をキーとして、引数値をマップに追加
				if (currentArgIsOption) {
					optionNameValueMap.put(currentOptionName, args[argIndex]);

				// 事前にオプション名が無指定だった場合は、デフォルトオプション名をキーとし、引数値をマップに追加
				} else {
					optionNameValueMap.put(OPTION_NAME_DEFAULT, args[argIndex]);
				}

				// オプション名指定をリセット
				currentArgIsOption = false;
				currentOptionName = null;
			}
		}

		// オプション名が存在したのにマップで値と紐づけられていないものは、値が省略されているので、マップにnullを値として入れておく
		Iterator<String> optionNameIterator = optionNameList.iterator();
		while (optionNameIterator.hasNext()) {
			String optionName = optionNameIterator.next();
			if (!optionNameValueMap.containsKey(optionName)) {
				optionNameValueMap.put(optionName, null);
			}
		}

		return optionNameValueMap;
	}

	private void setEncoding(String encoding) {
		if (encoding == null) {
			System.err.println("No encoding is specified.");
		}
		this.encoding = encoding;
	}


	private ScriptEngine createInitializedScriptEngine() {

		// ScriptEngineManagerでVnanoのスクリプトエンジンを検索して取得
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("vnano");
		if (engine == null) {
			System.err.println("Fatal error: ScriptEngine not found.");
			return null;
		}

		// メソッド・フィールドを外部関数・変数としてスクリプトエンジンに接続
		try {
			ScriptIO ioInstance = new ScriptIO();
			engine.put("output(int)",    new Object[]{ ScriptIO.class.getMethod("output",long.class    ), ioInstance } );
			engine.put("output(float)",  new Object[]{ ScriptIO.class.getMethod("output",double.class ), ioInstance } );
			engine.put("output(bool)",   new Object[]{ ScriptIO.class.getMethod("output",boolean.class), ioInstance } );
			engine.put("output(string)", new Object[]{ ScriptIO.class.getMethod("output",String.class ), ioInstance } );
			engine.put("time()",         new Object[]{ ScriptIO.class.getMethod("time"), ioInstance } );

		} catch (NoSuchMethodException e){
			System.err.println("Method/field not found.");
			e.printStackTrace();
			return null;
		}
		return engine;
	}

	private Interconnect createInitializedInterconnect() {

		// 何も接続されていない、空のインターコネクトを生成
		Interconnect interconnect = new Interconnect();

		// メソッド・フィールドを外部関数・変数としてインターコネクトに接続
		try {
			ScriptIO ioInstance = new ScriptIO();
			interconnect.connectPlugin("output(int)",    new Object[]{ ScriptIO.class.getMethod("output",long.class    ), ioInstance } );
			interconnect.connectPlugin("output(float)",  new Object[]{ ScriptIO.class.getMethod("output",double.class ), ioInstance } );
			interconnect.connectPlugin("output(bool)",   new Object[]{ ScriptIO.class.getMethod("output",boolean.class), ioInstance } );
			interconnect.connectPlugin("output(string)", new Object[]{ ScriptIO.class.getMethod("output",String.class ), ioInstance } );
			interconnect.connectPlugin("time()",         new Object[]{ ScriptIO.class.getMethod("time"), ioInstance } );

		} catch (NoSuchMethodException e){
			System.err.println("Method/field not found.");
			e.printStackTrace();
			return null;
		} catch (VnanoException e) {
			System.err.println("Method/field could not be connected.");
			e.printStackTrace();
		}
		return interconnect;
	}

	private String loadCode(String inputFilePath) {
		String code = "";
		try {
			List<String> lines;
			if (this.encoding == null) {
				lines = Files.readAllLines(Paths.get(inputFilePath));
			} else {
				lines = Files.readAllLines(Paths.get(inputFilePath), Charset.forName(this.encoding));
			}
			StringBuilder codeBuilder = new StringBuilder();
			String eol = System.getProperty("line.separator");
			for (String line: lines) {
				codeBuilder.append(line);
				codeBuilder.append(eol);
			}
			code = codeBuilder.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return code;
	}

	private void executeFile(String inputFilePath) {
		if (inputFilePath.endsWith(EXTENSION_VNANO)) {
			this.executeVnanoScriptFile(inputFilePath);
		} else if (inputFilePath.endsWith(EXTENSION_VRIL)) {
			this.executeVrilCodeFile(inputFilePath);
		} else {
			System.err.println("Unknown file type (extension): " + inputFilePath);
		}
	}

	public void executeVnanoScriptFile(String inputFilePath) {

		// ファイルからVRILコードを全部読み込む
		String scriptCode = this.loadCode(inputFilePath);
		if (scriptCode == null) {
			return;
		}

		// メソッド接続済みのスクリプトエンジンを生成して取得
		ScriptEngine engine = this.createInitializedScriptEngine();
		if (engine == null) {
			return;
		}

		// オプションマップにスクリプト名を設定
		this.optionMap.put(OptionKey.EVAL_SCRIPT_NAME, inputFilePath);

		// オプションマップをスクリプトエンジンに設定
		engine.put(SpecialBindingKey.OPTION_MAP, this.optionMap);

		// スクリプトを実行
		try {
			engine.eval(scriptCode);
		} catch (ScriptException e) {
			e.printStackTrace();
			return;
		}
	}

	public void executeVrilCodeFile(String inputFilePath) {

		// ファイルから仮想アセンブリコード（VRILコード）を全部読み込む
		String assemblyCode = this.loadCode(inputFilePath);
		if (assemblyCode == null) {
			return;
		}

		// メソッド接続済みのインターコネクトを生成して取得
		Interconnect interconnect = this.createInitializedInterconnect();
		if (interconnect == null) {
			return;
		}

		// オプションマップにスクリプト名を設定
		this.optionMap.put(OptionKey.EVAL_SCRIPT_NAME, inputFilePath);

		// プロセス仮想マシンを生成し、VRILコードを渡して実行
		VirtualMachine vm = new VirtualMachine();
		try {
			vm.eval(assemblyCode, interconnect, this.optionMap);
		} catch (VnanoException vne) {
			this.dumpException(vne);
			return;
		}
	}

	public void dumpException(VnanoException vne) {

		String message = ErrorMessage.generateErrorMessage(vne.getErrorType(), vne.getErrorWords(), this.locale);
		ScriptException se = null;
		if (vne.hasFileName() && vne.hasLineNumber()) {
			se = new ScriptException(message + ":", vne.getFileName(), vne.getLineNumber());
		} else {
			se = new ScriptException(message);
		}
		se.printStackTrace();

		System.err.println("Cause: ");
		vne.printStackTrace();
	}
}
