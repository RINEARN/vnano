/*
 * Copyright(C) 2018-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.main;

import java.util.List;
import java.util.Locale;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptException;

import org.vcssl.connect.ConnectorException;
import org.vcssl.connect.ConnectorImplementationContainer;
import org.vcssl.connect.ConnectorImplementationLoader;
import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.combinedtest.CombinedTestException;
import org.vcssl.nano.combinedtest.CombinedTestExecutor;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.interconnect.PluginLoader;
import org.vcssl.nano.interconnect.ScriptLoader;
import org.vcssl.nano.spec.SpecialBindingKey;
import org.vcssl.nano.spec.EngineInformation;
import org.vcssl.nano.spec.ErrorMessage;
import org.vcssl.nano.spec.OptionKey;
import org.vcssl.nano.spec.OptionValue;
import org.vcssl.nano.spec.LanguageSpecContainer;
import org.vcssl.nano.vm.VirtualMachine;

public final class VnanoCommandLineApplication {

	// 各種の言語仕様設定類を格納するコンテナを保持する
	private final LanguageSpecContainer LANG_SPEC = new LanguageSpecContainer(); // デフォルトの言語仕様設定を生成

	// エラーメッセージの表示言語指定などに使用されるロケールを保持する
	private Locale locale = Locale.getDefault();

	// デフォルトの文字コードやファイル類の読み込み場所などの定義
	private static final String DEFAULT_ENCODING = "UTF-8";
	private static final String DEFAULT_LIBRARY_LIST_FILE_PATH = "lib/VnanoLibraryList.txt";
	private static final String DEFAULT_PLUGIN_LIST_FILE_PATH = "plugin/VnanoPluginList.txt";
	@SuppressWarnings("unused")
	private static final String DEFAULT_LIBRARY_DIR = "lib/";
	private static final String DEFAULT_PLUGIN_DIR = "plugin/";

	// 実行可能ファイルの拡張子の定義
	private static final String EXTENSION_VNANO = ".vnano";
	private static final String EXTENSION_VRIL = ".vril";

	// コマンドラインオプション名の定義
	private static final String COMMAND_OPTNAME_PREFIX = "--";
	private static final String COMMAND_OPTNAME_FILE = "file";
	private static final String COMMAND_OPTNAME_HELP = "help";
	private static final String COMMAND_OPTNAME_DUMP = "dump";
	private static final String COMMAND_OPTNAME_RUN = "run";
	private static final String COMMAND_OPTNAME_LOCALE = "locale";
	private static final String COMMAND_OPTNAME_VERSION = "version";
	private static final String COMMAND_OPTNAME_ACCELERATOR = "accelerator";
	private static final String COMMAND_OPTNAME_TERMINATOR = "terminator";
	private static final String COMMAND_OPTNAME_ENCODING = "encoding";
	private static final String COMMAND_OPTNAME_PLUGIN_DIR = "pluginDir";
	private static final String COMMAND_OPTNAME_PLUGIN = "plugin";
	private static final String COMMAND_OPTNAME_PLUGIN_LIST = "pluginList";
	private static final String COMMAND_OPTNAME_LIBRARY_LIST = "libList";
	private static final String COMMAND_OPTNAME_TEST = "test";
	private static final String COMMAND_OPTNAME_DEFAULT = COMMAND_OPTNAME_FILE;

	// --dump オプションで指定する値の定義
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
	private static final Map<String, String> DUMP_TARGET_COMMANDOPT_ENGINEOPT_MAP = new HashMap<String, String>();
	static {
		DUMP_TARGET_COMMANDOPT_ENGINEOPT_MAP.put(DUMP_TARGET_INPUTTED_CODE, OptionValue.DUMPER_TARGET_INPUTTED_CODE);
		DUMP_TARGET_COMMANDOPT_ENGINEOPT_MAP.put(DUMP_TARGET_PREPROCESSED_CODE, OptionValue.DUMPER_TARGET_PREPROCESSED_CODE);
		DUMP_TARGET_COMMANDOPT_ENGINEOPT_MAP.put(DUMP_TARGET_TOKEN, OptionValue.DUMPER_TARGET_TOKEN);
		DUMP_TARGET_COMMANDOPT_ENGINEOPT_MAP.put(DUMP_TARGET_PARSED_AST, OptionValue.DUMPER_TARGET_PARSED_AST);
		DUMP_TARGET_COMMANDOPT_ENGINEOPT_MAP.put(DUMP_TARGET_ANALYZED_AST, OptionValue.DUMPER_TARGET_ANALYZED_AST);
		DUMP_TARGET_COMMANDOPT_ENGINEOPT_MAP.put(DUMP_TARGET_ASSEMBLY_CODE, OptionValue.DUMPER_TARGET_ASSEMBLY_CODE);
		DUMP_TARGET_COMMANDOPT_ENGINEOPT_MAP.put(DUMP_TARGET_OBJECT_CODE, OptionValue.DUMPER_TARGET_OBJECT_CODE);
		DUMP_TARGET_COMMANDOPT_ENGINEOPT_MAP.put(DUMP_TARGET_ACCELERATOR_CODE, OptionValue.DUMPER_TARGET_ACCELERATOR_CODE);
		DUMP_TARGET_COMMANDOPT_ENGINEOPT_MAP.put(DUMP_TARGET_ACCELERATOR_STATE, OptionValue.DUMPER_TARGET_ACCELERATOR_STATE);
		DUMP_TARGET_COMMANDOPT_ENGINEOPT_MAP.put(DUMP_TARGET_ALL, OptionValue.DUMPER_TARGET_ALL);
	}

	// エンジンに渡すオプションを格納するマップ
	private HashMap<String, Object> engineOptionMap = new HashMap<String, Object>();

	// コマンドラインオプション --pluginDir の値を控える
	private List<String> optPluginDirList = new ArrayList<String>();

	// コマンドラインオプション --plugin の値を控える
	private List<Object> optPluginList = new ArrayList<Object>();

	// コマンドラインオプション --pluginList の値を控える
	private String optPluginListFilePath = null;

	// コマンドラインオプション --libList の値を控える
	private String optLibraryListFilePath = null;

	// コマンドラインオプション --test の値を控える
	private boolean optCombinedTestRequired = false;

	// スクリプトからアクセスするメソッドを提供するクラス
	public class ScriptIO {
		private long launchedTime = System.nanoTime() / 1000000l;

		public void output(long value) {
			System.out.println(value);
		}
		public void output(double value) {
			System.out.println(value);
		}
		public void output(boolean value) {
			System.out.println(value);
		}
		public void output(String value) {
			System.out.println(value);
		}
		public long time() {
			return System.nanoTime() / 1000000l - launchedTime;
		}
	}

	public static void main(String[] args) {
		VnanoCommandLineApplication application = new VnanoCommandLineApplication();
		application.dispatch(args);
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
		System.out.println("");

		System.out.println("  --terminator <enableOrDisable>");
		System.out.println("");
		System.out.println("      Specify whether you want to enable the terminator.");
		System.out.println("      This option is specified by default, and the default value is false.");
		System.out.println("      You can choose and specify the value of <enableOrDisable> from the followings:");
		System.out.println("");
		System.out.println("        true            : Enable the terminator.");
		System.out.println("        false (default) : Disable the terminator.");
		System.out.println("");
		System.out.println("    e.g.");
		System.out.println("");
		System.out.println("      java -jar Vnano.jar Example.vnano --terminator true");
		System.out.println("      java -jar Vnano.jar Example.vnano --terminator false");
		System.out.println("");
		System.out.println("");

		System.out.println("  --plugin <pluginPath>");
		System.out.println("");
		System.out.println("      Specify the path of the plug-in to be connected.");
		System.out.println("      Multiple paths can be specified by separating with \":\" or \";\"");
		System.out.println("      (depends on the environment).");
		System.out.println("      If the plug-in is not in the current directory,");
		System.out.println("      specify the plug-in directory by --pluginDir option BEFORE this option.");
		System.out.println("");
		System.out.println("    e.g.");
		System.out.println("");
		System.out.println("      java -jar Vnano.jar Example.vnano --plugin ExamplePlugin");
		System.out.println("      java -jar Vnano.jar Example.vnano --plugin examplepackage.ExamplePlugin");
		System.out.println("      java -jar Vnano.jar Example.vnano --plugin \"Plugin1;Plugin2;Plugin3\"");
		System.out.println("      java -jar Vnano.jar Example.vnano --plugin \"Plugin1:Plugin2:Plugin3\"");
		System.out.println("      java -jar Vnano.jar Example.vnano --pluginDir \"./exampleDir/\" --plugin \"ExamplePlugin\"");
		System.out.println("");
		System.out.println("");

		System.out.println("  --pluginDir <pluginDirectoryPath>");
		System.out.println("");
		System.out.println("      Specify the path of the directory in which plug-ins specified by --plugin option are.");
		System.out.println("      Multiple paths can be specified by separating with \":\" or \";\"");
		System.out.println("      (depends on the environment).");
		System.out.println("      The default value is \"" + DEFAULT_PLUGIN_DIR + "\".");
		System.out.println("");
		System.out.println("");

		System.out.println("  --pluginList <pluginListFilePath>");
		System.out.println("");
		System.out.println("      Specify the path of the plugin-list file in which file paths of plug-ins ");
		System.out.println("      to be loaded are described.");
		System.out.println("      The default value is \""+ DEFAULT_PLUGIN_LIST_FILE_PATH + "\".");
		System.out.println("");
		System.out.println("");

		System.out.println("  --libList <libraryListFilePath>");
		System.out.println("");
		System.out.println("      Specify the path of the library-list file in which file paths of library-scripts ");
		System.out.println("      to be loaded are described.");
		System.out.println("      The default value is \""+ DEFAULT_LIBRARY_LIST_FILE_PATH + "\".");
		System.out.println("");
		System.out.println("");

		System.out.println("  --test");
		System.out.println("");
		System.out.println("      Execute combined tests of the script engine of the Vnano.");
		System.out.println("");
		System.out.println("    e.g.");
		System.out.println("");
		System.out.println("      java -jar Vnano.jar --test");
		System.out.println("");
		System.out.println("");

		System.out.println("[ Default Supported Functions ]");
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
		if (argLength == 1 && args[0].equals(COMMAND_OPTNAME_PREFIX + COMMAND_OPTNAME_HELP)) {
			this.help();
			return;
		}

		// 引数を解釈し、オプション名をキーとしてオプション値を返すマップを取得
		Map<String, String> optionNameValueMap = this.parseArguments(args);

		// オプションを一つずつ読み、対応する処理を実行する
		boolean optionProcessingSucceeded = true;
		Set<Map.Entry<String, String>> optionNameValueSet = optionNameValueMap.entrySet();
		for (Map.Entry<String, String> optionNameValuePair : optionNameValueSet) {
			if (!optionProcessingSucceeded) {
				break;
			}
			String optionName = optionNameValuePair.getKey();
			String optionValue = optionNameValuePair.getValue();

			// 1度でもオプション処理が失敗すると false にする
			optionProcessingSucceeded &= this.dispatchOptionProcessing(optionName, optionValue);
		}

		// スクリプトファイルの指定が必要かどうかを確認する（helpオプション等では不要になる）
		boolean scriptFileNecessary = this.isScriptFileNecessary(optionNameValueMap);

		// オプションで結合テストがリクエストされていた場合は、先にテストを実行する
		if (this.optCombinedTestRequired) {
			optionProcessingSucceeded &= this.executeCombinedTest();
		}

		// オプション処理（結合テスト含む）で失敗した場合は、その時点でステータスコード1で実行終了
		if (!optionProcessingSucceeded) {
			System.exit(1);
		}

		// スクリプトファイルの指定が必要なケースでは取得 (--help 時など、指定が必須でない場合もある)
		if (scriptFileNecessary && !optionNameValueMap.containsKey(COMMAND_OPTNAME_FILE)) {
			System.err.println("No script file is specified.");
			System.exit(1);
		}
		String inputFilePath = optionNameValueMap.get(COMMAND_OPTNAME_FILE); // ※ 無名引数として指定した場合もこのキーで格納されている

		// スクリプトファイルが指定されていれば実行する (指定が必須でない場合にも、指定されていれば実行)
		if (inputFilePath != null) {

			// 文字コード設定を取得
			String encoding = optionNameValueMap.containsKey(COMMAND_OPTNAME_ENCODING)
					? optionNameValueMap.get(COMMAND_OPTNAME_ENCODING) : DEFAULT_ENCODING;

			// ライブラリリストファイルのパスを取得（手順は以下の通り）
			// * オプションで明示指定されていればそれを採用（後で読み込みに失敗すればエラーになる）
			// * そうでなければ、デフォルトのパスにファイルが存在すればそれを採用（後で読み込みに失敗すればエラーになる）
			// * 指定もされず、デフォルトのパスにファイルも無い場合は、単純に何もしない（読み込みエラーも発生しない）
			String libraryListPath = null;   // null のままの場合は何も読み込まれない
			if (optLibraryListFilePath != null) {
				libraryListPath = optLibraryListFilePath;
			} else if (new File(DEFAULT_LIBRARY_LIST_FILE_PATH).exists()) {
				libraryListPath = DEFAULT_LIBRARY_LIST_FILE_PATH;
			}

			// プラグインリストファイルのパスを取得（手順は上のライブラリリストファイルの場合と同様）
			String pluginListPath = null;   // null のままの場合は何も読み込まれない
			if (optPluginListFilePath != null) {
				pluginListPath = optPluginListFilePath;
			} else if (new File(DEFAULT_PLUGIN_LIST_FILE_PATH).exists()) {
				pluginListPath = DEFAULT_PLUGIN_LIST_FILE_PATH;
			}

			// 実行
			try {
				this.executeFile(inputFilePath, libraryListPath, pluginListPath, encoding);

			} catch (VnanoException e) {
				this.dumpException(e);
				if (!this.engineOptionMap.containsKey(OptionKey.DUMPER_ENABLED)) {
					System.err.println("For more debug information, re-execute the script with \"--dump\" option.");
				}
				System.exit(1);
			}
		}
	}


	// オプション内容に基づいて、スクリプトファイルの指定を前提とする状況の場合は true を返す
	// (--help や --version などでは不要になる)
	private boolean isScriptFileNecessary(Map<String, String> optionNameValueMap) {
		if (optionNameValueMap.containsKey(COMMAND_OPTNAME_HELP) ) {
			return false;
		}
		if (optionNameValueMap.containsKey(COMMAND_OPTNAME_TEST) ) {
			return false;
		}
		if (optionNameValueMap.containsKey(COMMAND_OPTNAME_VERSION) ) {
			return false;
		}
		return true;
	}


	// 戻り値は成功:true/失敗:false
	public boolean dispatchOptionProcessing (String optionName, String optionValue) {
		if (optionName == null) {
			System.err.println("Fatal error: option name is null.");
			return false;
		}
		switch (optionName) {

			// --file または無名（デフォルト）オプションの場合
			case COMMAND_OPTNAME_FILE : {
				// このオプションの値は、事前に dispatch 側で取得され、
				// スクリプトの実行が必要な場合もそちら側で行うため、
				// ここでは何もしない
				return true;
			}

			// --help オプションの場合
			case COMMAND_OPTNAME_HELP : {
				this.help();
				return true;
			}

			// --run オプションの場合
			case COMMAND_OPTNAME_RUN : {
				this.engineOptionMap.put(OptionKey.RUNNING_ENABLED, Boolean.valueOf(optionValue));
				return true;
			}

			// --version オプションの場合
			case COMMAND_OPTNAME_VERSION : {
				System.out.println(EngineInformation.ENGINE_NAME + " " + EngineInformation.ENGINE_VERSION);
				return true;
			}

			// --accelerator オプションの場合
			case COMMAND_OPTNAME_ACCELERATOR : {
				if (optionValue.equals("true") || optionValue.equals("false")) {
					this.engineOptionMap.put(OptionKey.ACCELERATOR_ENABLED, Boolean.valueOf(optionValue));
				} else {
					System.err.println(
							"Invalid value for " + COMMAND_OPTNAME_PREFIX + COMMAND_OPTNAME_ACCELERATOR + "option: " + optionValue
					);
					return false;
				}
				return true;
			}

			// --terminator オプションの場合
			case COMMAND_OPTNAME_TERMINATOR : {
				if (optionValue.equals("true") || optionValue.equals("false")) {
					this.engineOptionMap.put(OptionKey.TERMINATOR_ENABLED, Boolean.valueOf(optionValue));
				} else {
					System.err.println(
							"Invalid value for " + COMMAND_OPTNAME_PREFIX + COMMAND_OPTNAME_TERMINATOR + "option: " + optionValue
					);
					return false;
				}
				return true;
			}

			// --locale オプションの場合
			case COMMAND_OPTNAME_LOCALE : {
				// 先頭と末尾以外に「 - 」がある場合は、言語コードと国コードの区切りなので、分割して解釈
				if (0 < optionValue.indexOf("-") && optionValue.indexOf("-") < optionValue.length()-1) {
					String[] localeStrings = optionValue.split("-");
					this.engineOptionMap.put(OptionKey.LOCALE, new Locale(localeStrings[0], localeStrings[1]));

				// それ以外は言語コードとして解釈
				} else {
					this.engineOptionMap.put(OptionKey.LOCALE, new Locale(optionValue));
				}
				return true;
			}

			// --encoding オプションの場合
			case COMMAND_OPTNAME_ENCODING : {
				// スクリプト実行時に参照されるため、ここでは何もしない
				return true;
			}

			// --dump オプションの場合
			case COMMAND_OPTNAME_DUMP : {
				if (optionValue == null) {
					optionValue = DUMP_TARGET_DEFAULT;
				}
				String convertedOptionValue = null;
				if (DUMP_TARGET_COMMANDOPT_ENGINEOPT_MAP.containsKey(optionValue)) {
					convertedOptionValue = DUMP_TARGET_COMMANDOPT_ENGINEOPT_MAP.get(optionValue);
				} else {
					System.err.println("Invalid value for " + COMMAND_OPTNAME_PREFIX + COMMAND_OPTNAME_DUMP + " option: " + optionValue);
					return false;
				}
				this.engineOptionMap.put(OptionKey.DUMPER_TARGET, convertedOptionValue);
				this.engineOptionMap.put(OptionKey.DUMPER_ENABLED, Boolean.valueOf(true));
				return true;
			}

			// --pluginDir オプションの場合
			case COMMAND_OPTNAME_PLUGIN_DIR : {

				// プラグインディレクトリを分割してリストに格納
				String[] pluginDirs = new String[0];
				if (optionValue != null) {
					pluginDirs = optionValue.split(System.getProperty("path.separator"));
				}
				for (String pluginDir: pluginDirs) {
					this.optPluginDirList.add(pluginDir);
				}
				return true;
			}

			// --plugin オプションの場合
			case COMMAND_OPTNAME_PLUGIN : {

				// プラグインパスを分割
				String[] pluginPaths = new String[0];
				if (optionValue != null) {
					pluginPaths = optionValue.split(System.getProperty("path.separator"));
				}

				// --pluginDir で指定されてリストに格納されている、プラグインディレクトリ（複数）をURLに変換
				String[] pluginDirs = new String[] { DEFAULT_PLUGIN_DIR };
				if (0 < this.optPluginDirList.size()) {
					pluginDirs = this.optPluginDirList.toArray(new String[0]);
				}
				int pluginDirLength = pluginDirs.length;
				URL[] pluginDirURLs = new URL[pluginDirLength];
				for (int dirIndex=0; dirIndex<pluginDirLength; dirIndex++) {
					try {
						pluginDirURLs[dirIndex] = new File(pluginDirs[dirIndex]).toURI().toURL();
					} catch (MalformedURLException e) {
						System.err.print("Invalid plugin directory: " + pluginDirs[dirIndex]);
						return false;
					}
				}

				// プラグインディレクトリを読み込み場所とするクラスローダと、それを用いるプラグインローダを生成
				URLClassLoader classLoader = new URLClassLoader(pluginDirURLs);
				ConnectorImplementationLoader pluginLoader = new ConnectorImplementationLoader(classLoader);

				// プラグイン（複数）を全て読み込み、リストに格納
				for (String pluginPath: pluginPaths) {
					try {
						ConnectorImplementationContainer pluginContainer = pluginLoader.load(pluginPath);
						Object plugin = pluginContainer.getConnectorImplementation();
						this.optPluginList.add(plugin);
					} catch (ConnectorException e) {
						System.err.println("Plug-in connection failed: " + pluginPath);
						e.printStackTrace();
					}
				}

				return true;
			}

			// --pluginList オプションの場合
			case COMMAND_OPTNAME_PLUGIN_LIST : {
				this.optPluginListFilePath = optionValue;
				return true;
			}

			// --libList オプションの場合
			case COMMAND_OPTNAME_LIBRARY_LIST : {
				this.optLibraryListFilePath = optionValue;
				return true;
			}

			// --test オプションの場合
			case COMMAND_OPTNAME_TEST : {
				// 後で結合テストを実行する（全オプション指定を反映した条件下でテストするため、ここではまだ実行しない）
				this.optCombinedTestRequired = true;
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

		List<String> optionNameList = new ArrayList<String>();

		// オプションの名前をキーとし、その指定内容を値とする紐づけるマップ
		Map<String, String> optionNameValueMap = new LinkedHashMap<String, String>();

		// オプション名の指定（「--」で始まる引数）があった場合に内容を控え、値を読んでマップ登録する際に使う
		boolean currentArgIsOption = false;
		String currentOptionName = null;

		// 全ての引数を先頭から読んでいく
		for (int argIndex=0; argIndex<argLength; argIndex++) {

			// オプションプレフィックス(--)で始まる場合は、オプション名の指定と見なし、次引数の解釈のために保持
			if (args[argIndex].startsWith(COMMAND_OPTNAME_PREFIX)) {
				currentArgIsOption = true;
				currentOptionName = args[argIndex].substring(COMMAND_OPTNAME_PREFIX.length(), args[argIndex].length());
				optionNameList.add(currentOptionName);

			} else {
				// 事前にオプション名が指定されていた場合は、その名前をキーとして、引数値をマップに追加
				if (currentArgIsOption) {
					optionNameValueMap.put(currentOptionName, args[argIndex]);

				// 事前にオプション名が無指定だった場合は、デフォルトオプション名をキーとし、引数値をマップに追加
				} else {
					optionNameValueMap.put(COMMAND_OPTNAME_DEFAULT, args[argIndex]);
				}

				// オプション名指定をリセット
				currentArgIsOption = false;
				currentOptionName = null;
			}
		}

		// オプション名が存在したのにマップで値と紐づけられていないものは、値が省略されているので、マップにnullを値として入れておく
		for (String optionName: optionNameList) {
			if (!optionNameValueMap.containsKey(optionName)) {
				optionNameValueMap.put(optionName, null);
			}
		}

		return optionNameValueMap;
	}


	// オプション設定やプラグイン接続の済んだスクリプトエンジンを生成して返す
	private VnanoEngine createInitializedVnanoEngine(Map<String, Object> optionMap, PluginLoader pluginLoader) {

		// Vnanoのスクリプトエンジンを生成
		VnanoEngine engine = new VnanoEngine();

		// プラグインが接続/初期化時にオプション値を参照する場合があるので、接続前にオプション設定を済ませる
		try {
			engine.setOptionMap(optionMap);
		} catch (VnanoException e) {
			System.err.println("Option setting failed.");
			e.printStackTrace();
			return null;
		}

		// デフォルトプラグインを外部関数・変数としてスクリプトエンジンに接続
		// -> ./plugin/ 以下に独立クラスとして切り出して、後述の処理で接続するように統一する？ 要検討
		try {
			ScriptIO ioInstance = new ScriptIO();
			engine.connectPlugin(SpecialBindingKey.AUTO_KEY, new Object[]{ ScriptIO.class.getMethod("output",long.class    ), ioInstance } );
			engine.connectPlugin(SpecialBindingKey.AUTO_KEY, new Object[]{ ScriptIO.class.getMethod("output",double.class ), ioInstance } );
			engine.connectPlugin(SpecialBindingKey.AUTO_KEY, new Object[]{ ScriptIO.class.getMethod("output",boolean.class), ioInstance } );
			engine.connectPlugin(SpecialBindingKey.AUTO_KEY, new Object[]{ ScriptIO.class.getMethod("output",String.class ), ioInstance } );
			engine.connectPlugin(SpecialBindingKey.AUTO_KEY, new Object[]{ ScriptIO.class.getMethod("time"), ioInstance } );

		} catch (NoSuchMethodException e){
			System.err.println("Method/field not found.");
			e.printStackTrace();
			return null;
		} catch (VnanoException e){
			System.err.println("Plug-in connection failed.");
			e.printStackTrace();
			return null;
		}

		// プラグインローダに登録されているプラグインを読み込み、エンジンに接続
		try {
			pluginLoader.load();
			if (pluginLoader.hasPlugins()) {
				String[] pluginNames = pluginLoader.getPluginNames();
				Object[] pluginInstances = pluginLoader.getPluginInstances();
				int pluginN = pluginNames.length;
				for (int pluginIndex=0; pluginIndex<pluginN; pluginIndex++) {
					//engine.connectPlugin(pluginNames[pluginIndex], pluginInstances[pluginIndex]);
					engine.connectPlugin("___VNANO_AUTO_KEY", pluginInstances[pluginIndex]); // キーは文法規則があるので自動生成
				}
			}
		} catch (VnanoException e) {
			System.err.println("Plug-in load failed.");
			e.printStackTrace();
		}

		return engine;
	}


	// オプション設定やプラグイン接続の済んだインターコネクトを生成して返す（VMコードを直接実行する場合に使用）
	private Interconnect createInitializedInterconnect(Map<String, Object> optionMap, PluginLoader pluginLoader) {

		// 何も接続されていない、空のインターコネクトを生成
		Interconnect interconnect = new Interconnect(LANG_SPEC);

		// プラグインが接続/初期化時にオプション値を参照する場合があるので、接続前にオプション設定を済ませる
		try {
			interconnect.setOptionMap(optionMap);
		} catch (VnanoException e) {
			System.out.println("Invalid option detected.");
			e.printStackTrace();
		}

		// メソッド・フィールドを外部関数・変数としてインターコネクトに接続
		// -> ./plugin/ 以下に独立クラスとして切り出して、後述の処理で接続するように統一する？ 要検討
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

		// プラグインローダに登録されているプラグインを読み込み、インターコネクトに接続
		try {
			pluginLoader.load();
			if (pluginLoader.hasPlugins()) {
				String[] pluginNames = pluginLoader.getPluginNames();
				Object[] pluginInstances = pluginLoader.getPluginInstances();
				int pluginN = pluginNames.length;
				for (int pluginIndex=0; pluginIndex<pluginN; pluginIndex++) {
					interconnect.connectPlugin(pluginNames[pluginIndex], pluginInstances[pluginIndex]);
				}
			}
		} catch (VnanoException e) {
			System.err.println("Plug-in load failed.");
			e.printStackTrace();
		}

		return interconnect;
	}


	private boolean executeCombinedTest() {

		// メソッド接続済みのスクリプトエンジンを生成して取得
		VnanoEngine engine = this.createInitializedVnanoEngine(
			this.engineOptionMap, new PluginLoader(DEFAULT_ENCODING, LANG_SPEC)
		);

		try {
			CombinedTestExecutor testExecutor = new CombinedTestExecutor();
			testExecutor.test(engine);
			return true;
		} catch (CombinedTestException e) {
			System.err.println("Combined test failed.");
			System.err.println("");
			System.err.println("[ Stack Trace ]");
			e.printStackTrace();
			System.err.println("");
			if (!this.engineOptionMap.containsKey(OptionKey.DUMPER_ENABLED)) {
				System.err.println("For more debug information, re-execute combined tests with \"--dump\" option.");
			}
			return false;
		}
	}


	private void executeFile(
			String inputFilePath, String libraryListFilePath, String pluginListFilePath, String defaultEncoding)
					throws VnanoException {

		// ライブラリの読み込み
		ScriptLoader scriptLoader = new ScriptLoader(defaultEncoding, LANG_SPEC);
		scriptLoader.setMainScriptPath(inputFilePath);
		if (libraryListFilePath != null) {
			scriptLoader.setLibraryScriptListPath(libraryListFilePath);
		}
		scriptLoader.load();

		// プラグインの読み込み
		PluginLoader pluginLoader = new PluginLoader(defaultEncoding, LANG_SPEC);
		if (pluginListFilePath != null) {
			pluginLoader.setPluginListPath(pluginListFilePath);
		}
		pluginLoader.load();

		// 実行
		if (inputFilePath.endsWith(EXTENSION_VNANO)) {
			this.executeVnanoScriptFile(scriptLoader, pluginLoader);
		} else if (inputFilePath.endsWith(EXTENSION_VRIL)) {
			this.executeVrilCodeFile(scriptLoader, pluginLoader);
		} else {
			System.err.println("Unknown file type (extension): " + inputFilePath);
		}
	}

	public void executeVnanoScriptFile(ScriptLoader scriptLoader, PluginLoader pluginLoader) throws VnanoException {

		// オプションマップにスクリプト名を設定し、I/O形式をCUIに設定
		//（プラグインが接続/初期化時にオプション値を参照する場合があるので、接続前に設定を済ませる）
		this.engineOptionMap.put(OptionKey.MAIN_SCRIPT_NAME, scriptLoader.getMainScriptName());
		this.engineOptionMap.put(OptionKey.UI_MODE, "CUI");

		// オプション設定済み＆プラグイン接続済みのスクリプトエンジンを生成
		VnanoEngine engine = this.createInitializedVnanoEngine(this.engineOptionMap, pluginLoader);

		// スクリプトエンジンにライブラリを include 登録
		if (scriptLoader.hasLibraryScripts()) {
			String[] libNames = scriptLoader.getLibraryScriptNames();
			String[] libContents = scriptLoader.getLibraryScriptContents();
			int libN = libNames.length;
			for (int libIndex=0; libIndex<libN; libIndex++) {
				engine.includeLibraryScript(libNames[libIndex], libContents[libIndex]);
			}
		}

		// スクリプトを実行
		engine.executeScript(scriptLoader.getMainScriptContent());

		// プラグインの接続を解除（プラグイン側でも接続解除用の終了時処理が実行される）
		engine.disconnectAllPlugins();
	}

	public void executeVrilCodeFile(ScriptLoader scriptLoader, PluginLoader pluginLoader) throws VnanoException {

		// オプションマップにスクリプト名を設定し、I/O形式をCUIに設定
		//（プラグインが接続/初期化時にオプション値を参照する場合があるので、接続前に設定を済ませる）
		this.engineOptionMap.put(OptionKey.MAIN_SCRIPT_NAME, scriptLoader.getMainScriptName());
		this.engineOptionMap.put(OptionKey.UI_MODE, "CUI");

		// オプション設定済み＆プラグイン接続済みのインターコネクトを生成して取得
		Interconnect interconnect = this.createInitializedInterconnect(this.engineOptionMap, pluginLoader);
		if (interconnect == null) {
			return;
		}

		// プラグインのスクリプト実行前の初期化処理などを実行
		interconnect.activate();

		// プロセス仮想マシンを生成し、VRILコードを渡して実行
		VirtualMachine vm = new VirtualMachine(LANG_SPEC);
		vm.executeAssemblyCode(scriptLoader.getMainScriptContent(), interconnect);

		// プラグインのスクリプト実行後の終了時処理などを実行
		interconnect.deactivate();

		// プラグインの接続を解除（プラグイン側でも接続解除用の終了時処理が実行される）
		interconnect.disconnectAllPlugins();
	}

	public void dumpException(Exception e) {

		// 例外が VnanoException の場合は、VnanoScriptException でラップした上で、そのエラー内容をダンプする
		// (アプリケーション上でスクリプトエンジンを動作させる場合と、エラー内容をなるべく一貫させるため)
		if (e instanceof VnanoException) {
			VnanoException vne = (VnanoException)e;
			String message = ErrorMessage.generateErrorMessage(vne.getErrorType(), vne.getErrorWords(), this.locale);
			ScriptException se = null;
			if (vne.hasFileName() && vne.hasLineNumber()) {
				se = new ScriptException(message + ":", vne.getFileName(), vne.getLineNumber());
			} else {
				se = new ScriptException(message);
			}
			se.printStackTrace();
			System.err.println("Cause: ");
			e.printStackTrace();

			System.err.println("");

		// それ以外の例外は、普通にスタックトレースをダンプする
		} else {
			e.printStackTrace();
		}
	}
}
