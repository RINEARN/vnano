/*
 * Copyright(C) 2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.main;

import java.util.List;
import java.io.FileReader;
import java.io.IOException;
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

import org.vcssl.connect.MethodXfci1Adapter;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.compiler.AstNode;
import org.vcssl.nano.compiler.CodeGenerator;
import org.vcssl.nano.compiler.Parser;
import org.vcssl.nano.compiler.LexicalAnalyzer;
import org.vcssl.nano.compiler.SemanticAnalyzer;
import org.vcssl.nano.compiler.Token;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.vm.VirtualMachine;
import org.vcssl.nano.vm.VirtualMachineObjectCode;
import org.vcssl.nano.vm.assembler.Assembler;

public final class VnanoCommandLineApplication {

	private static final String EXTENSION_VNANO = ".vnano";
	private static final String EXTENSION_VRIL = ".vril";
	private static final String ARGUMENT_OPTION_PREFIX = "--";
	private static final String ARGUMENT_OPTION_FILE = "file";
	private static final String ARGUMENT_OPTION_DUMP = "dump";
	private static final String ARGUMENT_OPTION_RUN = "run";
	private static final String ARGUMENT_OPTION_DEFAULT = ARGUMENT_OPTION_FILE;
	private static final String DUMP_TARGET_TOKEN = "token";
	private static final String DUMP_TARGET_PARSED_AST = "parsedAst";
	private static final String DUMP_TARGET_ANALYZED_AST = "analyzedAst";
	private static final String DUMP_TARGET_ASSEMBLY_CODE = "assemblyCode";
	private static final String DUMP_TARGET_OBJECT_CODE = "objectCode";
	private static final String DUMP_TARGET_ALL = "all";
	private static final String DUMP_TARGET_DEFAULT = DUMP_TARGET_ALL;

	private boolean runRequired = true;

	// スクリプトからアクセスするメソッドを提供するクラス
	public class ScriptIO {
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
	}

	public static void main(String[] args) {
		VnanoCommandLineApplication application = new VnanoCommandLineApplication();
		application.dispatch(args);
	}

	public VnanoCommandLineApplication() {
	}

	public void help() {

	}

	public void dispatch(String[] args) {
		int argLength = args.length;

		// 引数が無ければヘルプを表示して終了
		if (argLength == 0) {
			this.help();
			return;
		}

		// 引数を解釈し、オプション名をキーとしてオプション値を返すマップを取得
		Map<String, String> optionNameValueMap = this.parseArguments(args);

		// 読み込むファイルのパス（デフォルトオプションなので名称未指定の引数もこれに該当）を取得
		if (!optionNameValueMap.containsKey(ARGUMENT_OPTION_FILE)) {
			System.err.println("No script file is specified.");
		}
		String inputFilePath = optionNameValueMap.get(ARGUMENT_OPTION_FILE);

		// オプションを一つずつ読み、対応する処理を実行する
		Set<Map.Entry<String, String>> optionNameValueSet = optionNameValueMap.entrySet();
		for (Map.Entry<String, String> optionNameValuePair : optionNameValueSet) {
			String optionName = optionNameValuePair.getKey();
			String optionValue = optionNameValuePair.getValue();
			this.dispatchOptionProcessing(optionName, optionValue, inputFilePath);
		}

		// スクリプトの実行が必要なら実行する
		if (this.runRequired) {
			this.executeFile(inputFilePath);
		}
	}

	public void dispatchOptionProcessing (String optionName, String optionValue, String inputFilePath) {
		if (optionName == null) {
			System.err.println("Fatal error: option name is null.");
			return;
		}
		switch (optionName) {

			// --file または無名（デフォルト）オプションの場合
			case ARGUMENT_OPTION_FILE : {
				// このオプションの値は、事前に dispatch 側で取得され、
				// スクリプトの実行が必要な場合もそちら側で行うため、
				// ここでは何もしない
				return;
			}

			// --run オプションの場合
			case ARGUMENT_OPTION_RUN : {
				this.setRunRequired(optionValue);
				return;
			}

			// --dump オプションの場合
			case ARGUMENT_OPTION_DUMP : {
				this.dump(inputFilePath, optionValue);
				return;
			}

			// その他のオプションの場合
			default : {
				System.err.println("Unknown option name: " + optionName);
				return;
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
			if (args[argIndex].startsWith(ARGUMENT_OPTION_PREFIX)) {
				currentArgIsOption = true;
				currentOptionName = args[argIndex].substring(ARGUMENT_OPTION_PREFIX.length(), args[argIndex].length());
				optionNameList.add(currentOptionName);

			} else {
				// 事前にオプション名が指定されていた場合は、その名前をキーとして、引数値をマップに追加
				if (currentArgIsOption) {
					optionNameValueMap.put(currentOptionName, args[argIndex]);

				// 事前にオプション名が無指定だった場合は、デフォルトオプション名をキーとし、引数値をマップに追加
				} else {
					optionNameValueMap.put(ARGUMENT_OPTION_DEFAULT, args[argIndex]);
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

	private void setRunRequired(String optionValue) {
		// Boolean.valueOf だと true 以外が全て false になるので直球で比較
		if (optionValue.equals("true")) {
			this.runRequired = true;
		} else if (optionValue.equals("false")) {
			this.runRequired = false;
		} else {
			System.err.println(
					"Invalid value for " + ARGUMENT_OPTION_PREFIX + ARGUMENT_OPTION_RUN + "option: " + optionValue
			);
		}
	}

	private ScriptEngine createInitializedScriptEngine() {

		// ScriptEngineManagerでVnanoのスクリプトエンジンを検索して取得
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("vnano");
		if (engine == null) {
			System.err.println("Faital error: ScriptEngine not found.");
			return null;
		}

		// メソッド・フィールドを外部関数・変数としてスクリプトエンジンに接続
		try {
			ScriptIO ioInstance = new ScriptIO();
			engine.put("output(int)",    new Object[]{ ScriptIO.class.getMethod("output",long.class    ), ioInstance } );
			engine.put("output(float)",  new Object[]{ ScriptIO.class.getMethod("output",double.class ), ioInstance } );
			engine.put("output(bool)",   new Object[]{ ScriptIO.class.getMethod("output",boolean.class), ioInstance } );
			engine.put("output(string)", new Object[]{ ScriptIO.class.getMethod("output",String.class ), ioInstance } );

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
			interconnect.connect( new MethodXfci1Adapter( ScriptIO.class.getMethod("output",long.class    ), ioInstance) );
			interconnect.connect( new MethodXfci1Adapter( ScriptIO.class.getMethod("output",double.class ), ioInstance) );
			interconnect.connect( new MethodXfci1Adapter( ScriptIO.class.getMethod("output",boolean.class), ioInstance) );
			interconnect.connect( new MethodXfci1Adapter( ScriptIO.class.getMethod("output",String.class ), ioInstance) );

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
			List<String> lines = Files.readAllLines(Paths.get(inputFilePath));
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

	private void dump(String inputFilePath, String dumpTarget) {
		if (dumpTarget == null) {
			dumpTarget = DUMP_TARGET_DEFAULT;
		}
		switch (dumpTarget) {
			case DUMP_TARGET_TOKEN : {
				this.dump(inputFilePath, false, true, false, false, false, false);
				return;
			}
			case DUMP_TARGET_PARSED_AST : {
				this.dump(inputFilePath, false, false, true, false, false, false);
				return;
			}
			case DUMP_TARGET_ANALYZED_AST : {
				this.dump(inputFilePath, false, false, false, true, false, false);
				return;
			}
			case DUMP_TARGET_ASSEMBLY_CODE : {
				this.dump(inputFilePath, false, false, false, false, true, false);
				return;
			}
			case DUMP_TARGET_OBJECT_CODE : {
				this.dump(inputFilePath, false, false, false, false, false, true);
				return;
			}
			case DUMP_TARGET_ALL : {
				this.dump(inputFilePath, true, true, true, true, true, true);
				return;
			}
			default : {
				System.err.println("Fatal error: invalid dump target: " + dumpTarget);
				return;
			}
		}
	}


	private void dump(String inputFilePath, boolean withHeader,
			boolean dumpTokens, boolean dumpParsedAst, boolean dumpAnalyzedAst,
			boolean dumpAssemblyCode, boolean dumpObjectCode) {

		// ファイルからスクリプトコードを全部読み込む
		String scriptCode = this.loadCode(inputFilePath);
		if (scriptCode == null) {
			return;
		}

		// メソッド接続済みのインターコネクトを生成して取得
		Interconnect interconnect = this.createInitializedInterconnect();
		if (interconnect == null) {
			return;
		}

		if (withHeader) {
			System.out.println("================================================================================");
			System.out.println("= DUMP BEGIN");
			System.out.println("================================================================================");
		}


		try {
			// 字句解析器でトークンを生成
			Token[] tokens = new LexicalAnalyzer().analyze(scriptCode, inputFilePath);

			// トークンをダンプ
			if (dumpTokens) {
				if (withHeader) {
					System.out.println("");
					System.out.println("================================================================================");
					System.out.println("= Tokens");
					System.out.println("================================================================================");
				}
				for (Token token : tokens) {
					System.out.println(token);
				}
			}

			// 構文解析器でAST（抽象構文木）を生成
			AstNode parsedNode = new Parser().parse(tokens);

			// ASTをダンプ
			if (dumpParsedAst) {
				if (withHeader) {
					System.out.println("");
					System.out.println("================================================================================");
					System.out.println("= Parsed AST");
					System.out.println("================================================================================");
				}
				System.out.println(parsedNode.toString());
			}

			// 意味解析器でASTの情報を補間したものを取得
			AstNode analyzedNode = new SemanticAnalyzer().analyze(parsedNode, interconnect);

			// 意味解析済みASTをダンプ
			if (dumpAnalyzedAst) {
				if (withHeader) {
					System.out.println("");
					System.out.println("================================================================================");
					System.out.println("= Analyzed AST");
					System.out.println("================================================================================");
				}
				System.out.println(analyzedNode.toString());
			}

			// ASTから中間アセンブリコード（VRILコード）を生成
			String assemblyCode = new CodeGenerator().generate(analyzedNode);

			// 中間アセンブリコードをダンプ
			if (dumpAssemblyCode) {
				if (withHeader) {
					System.out.println("");
					System.out.println("================================================================================");
					System.out.println("= Assembly Code (VRIL Code)");
					System.out.println("================================================================================");
				}
				System.out.println(assemblyCode);
			}

			// 生成した中間アセンブリコード（VRILコード）をアセンブルし、VM用オブジェクトコードを生成
			VirtualMachineObjectCode vmObjectCode = new Assembler().assemble(assemblyCode, interconnect);

			// VMオブジェクトコードをダンプ
			if (dumpObjectCode) {
				if (withHeader) {
					System.out.println("");
					System.out.println("================================================================================");
					System.out.println("= VM Object Code");
					System.out.println("================================================================================");
				}
				vmObjectCode.dump();
			}

			if (withHeader) {
				System.out.println("================================================================================");
				System.out.println("= DUMP END");
				System.out.println("================================================================================");
			}

		} catch (VnanoException e) {
			e.printStackTrace();
			return;
		}
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

		// メソッド接続済みのスクリプトエンジンを生成して取得
		ScriptEngine engine = this.createInitializedScriptEngine();
		if (engine == null) {
			return;
		}

		// スクリプトを実行
		try (FileReader fileReader = new FileReader(inputFilePath)) {
			engine.eval(fileReader);
		} catch (ScriptException | IOException e) {
			e.printStackTrace();
			return;
		}
	}

	public void executeVrilCodeFile(String inputFilePath) {

		// ファイルからVRILコードを全部読み込む
		String vrilCode = this.loadCode(inputFilePath);
		if (vrilCode == null) {
			return;
		}

		// メソッド接続済みのインターコネクトを生成して取得
		Interconnect interconnect = this.createInitializedInterconnect();
		if (interconnect == null) {
			return;
		}

		// プロセス仮想マシンを生成し、VRILコードを渡して実行
		VirtualMachine vm = new VirtualMachine();
		try {
			vm.eval(vrilCode, interconnect);
		} catch (VnanoException e) {
			e.printStackTrace();
			return;
		}
	}
}
