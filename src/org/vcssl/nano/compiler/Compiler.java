/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.OptionKey;
import org.vcssl.nano.spec.OptionValue;

/**
 * <p>
 * <span>
 * <span class="lang-en">
 * The class performing the function of a compiler in the script engine of the Vnano
 * </span>
 * <span class="lang-ja">
 * Vnanoのスクリプトエンジン内で, コンパイラの機能を担うクラスです
 * </span>
 * .
 * </p>
 *
 * <p>
 * <span class="lang-en">
 * This class compiles script code written in the Vnano
 * to a kind of intermediate code, named as "VRIL" code.
 * VRIL ― Vector Register Intermediate Language ― is a low-level (but readable text format) language
 * designed as a virtual assembly code of the VM (Virtual Machine) layer of Vnano Engine.
 * </span>
 *
 * <span class="lang-ja">
 * このクラスは,
 * Vnanoのスクリプトコードを, "VRILコード" と呼ぶ一種の中間コードへと変換します.
 * VRIL（Vector Register Intermediate Language; ベクトルレジスタ中間言語）は, Vnanoエンジンの
 * VM（仮想マシン）層の単位動作に対応するレベルの低抽象度な命令を提供する,  仮想的なアセンブリ言語です.
 * VRILコードは, 実在のアセンブリコードと同様に, 人間にとって可読なテキスト形式のコードです.
 * </span>
 * </p>
 *
 * <p>
 * &raquo <a href="../../../../../src/org/vcssl/nano/compiler/Compiler.java">Source code</a>
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class Compiler {

	/**
	 * <span class="lang-en">
	 * This constructor does nothing, because this class has no fields for storing state
	 * </span>
	 * <span class="lang-ja">
	 * このクラスは状態を保持するフィールドを持たないため, コンストラクタは何もしません
	 * </span>
	 * .
	 */
	public Compiler() {
	}


	/**
	 * <span class="lang-en">
	 * Compiles the script code written in the Vnano to VRIL code
	 * </span>
	 * <span class="lang-ja">
	 * Vnanoで記述されたスクリプトコードを, VRILコードにコンパイルします
	 * </span>
	 * .
	 *
	 * @param scripts
	 *   <span class="lang-en">Code of scripts to be compiled.</span>
	 *   <span class="lang-ja">コンパイルしたいスクリプト（複数）のコード.</span>
	 *
	 * @param names
	 *   <span class="lang-en">Names of scripts.</span>
	 *   <span class="lang-ja">スクリプト（複数）の名前.</span>
	 *
	 * @param interconnect
	 *   <span class="lang-en">The interconnect to which external functions/variables are connected.</span>
	 *   <span class="lang-ja">外部変数/関数が接続されているインターコネクト.</span>
	 *
	 * @param optionMap
	 *   <span class="lang-en">The Map (option map) storing names and values of options.</span>
	 *   <span class="lang-ja">オプションの名前と値を格納するマップ（オプションマップ）.</span>
	 *
	 * @return
	 *   <span class="lang-en">The compiled VRIL code.</span>
	 *   <span class="lang-ja">コンパイルされたVRILコード.</span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">Thrown when a syntax error will be detected for the content of the script.</span>
	 *   <span class="lang-ja">スクリプトの内容に構文エラーが検出された場合にスローされます.</span>
	 */
	public String compile(String[] scripts, String[] names, Interconnect interconnect, Map<String, Object> optionMap)
					throws VnanoException {

		// スクリプトコードの枚数とスクリプト名の個数が違う場合はエラー
		if (scripts.length != names.length) {
			throw new VnanoFatalException("Array-lengths of \"scripts\" and \"names\" arguments are mismatching.");
		}

		// VRIL生成用途でアプリケーションから直接呼ばれる事も考えられるため、オプション内容の正規化を再度行っておく
		optionMap = OptionValue.normalizeValuesOf(optionMap);

		// スクリプトコードの枚数を取得
		int scriptLength = scripts.length;

		// EVAL_NUMBER_AS_FLOAT オプションの値を取得
		boolean evalNumberAsFloat = (Boolean)optionMap.get(OptionKey.EVAL_NUMBER_AS_FLOAT);

		// ダンプ関連のオプション指定内容を取得
		boolean shouldDump = (Boolean)optionMap.get(OptionKey.DUMPER_ENABLED);        // ダンプするかどうか
		String dumpTarget = (String)optionMap.get(OptionKey.DUMPER_TARGET);           // ダンプ対象
		boolean dumpTargetIsAll = dumpTarget.equals(OptionValue.DUMPER_TARGET_ALL);   // ダンプ対象が全てかどうか
		PrintStream dumpStream = (PrintStream)optionMap.get(OptionKey.DUMPER_STREAM); // ダンプ先ストリーム

		// 入力スクリプトコードをダンプ
		if (shouldDump && (dumpTargetIsAll || dumpTarget.equals(OptionValue.DUMPER_TARGET_INPUTTED_CODE)) ) {
			this.dumpInputtedCode(scripts, names, dumpTargetIsAll, dumpStream);
		}


		// プリプロセッサでコメントを削除し、改行コードを LF (0x0A) に統一
		String[] preprocessedScripts = new String[scriptLength];
		for (int scriptIndex=0; scriptIndex<scriptLength; scriptIndex++) {
			preprocessedScripts[scriptIndex] = new Preprocessor().preprocess(scripts[scriptIndex]);
		}

		// プリプロセッサ処理後のコードをダンプ
		if (shouldDump && (dumpTargetIsAll || dumpTarget.equals(OptionValue.DUMPER_TARGET_PREPROCESSED_CODE)) ) {
			this.dumpPreprocessedCode(preprocessedScripts, names, dumpTargetIsAll, dumpStream);
		}


		// 字句解析でトークン配列を生成
		LexicalAnalyzer lexer = new LexicalAnalyzer();
		Token[][] tokens = new Token[scriptLength][]; // [ スクリプトのインデックス ][ トークンのインデックス ]
		for (int scriptIndex=0; scriptIndex<scriptLength; scriptIndex++) {
			tokens[scriptIndex] = lexer.analyze(preprocessedScripts[scriptIndex], names[scriptIndex]);
		}

		// EVAL_NUMBER_AS_FLOAT オプションが有効な場合、エンジンのevalに渡されたスクリプト内のintリテラルをfloat型に変更
		if (evalNumberAsFloat) {
			tokens[scriptLength-1] = lexer.replaceDataTypeOfLiteralTokens( // [scriptLength-1]番目はeval対象のスクリプト
				tokens[scriptLength-1], DataTypeName.INT, DataTypeName.FLOAT
			);
		}

		// 全スクリプトのトークン配列を結合 ( 最初にスクリプトそのものを結合せず、わざわざ
		// 字句解析後に結合している理由は、エラー情報などで使用する、行番号やファイル名情報のずれを防ぐため ）
		List<Token> tokenList = new LinkedList<Token>();
		for (int scriptIndex=0; scriptIndex<scriptLength; scriptIndex++) {
			for (Token token: tokens[scriptIndex]) {
				tokenList.add(token);
			}
		}
		Token[] unifiedTokens = tokenList.toArray(new Token[0]);


		// トークン配列をダンプ
		if (shouldDump && (dumpTargetIsAll || dumpTarget.equals(OptionValue.DUMPER_TARGET_TOKEN)) ) {
			this.dumpTokens(unifiedTokens, dumpTargetIsAll, dumpStream);
		}


		// 構文解析でAST（抽象構文木）を生成
		AstNode parsedAstRootNode = new Parser().parse(unifiedTokens);

		// 構文解析後のASTをダンプ
		if (shouldDump && (dumpTargetIsAll || dumpTarget.equals(OptionValue.DUMPER_TARGET_PARSED_AST)) ) {
			this.dumpParsedAst(parsedAstRootNode, dumpTargetIsAll, dumpStream);
		}


		// 意味解析でASTの情報を補間
		AstNode analyzedAstRootNode = new SemanticAnalyzer().analyze(parsedAstRootNode, interconnect);

		// 意味解析後のASTをダンプ
		if (shouldDump && (dumpTargetIsAll || dumpTarget.equals(OptionValue.DUMPER_TARGET_ANALYZED_AST)) ) {
			this.dumpAnalyzedAst(analyzedAstRootNode, dumpTargetIsAll, dumpStream);
		}


		// 中間アセンブリコードを生成
		String assemblyCode = new CodeGenerator().generate(analyzedAstRootNode);

		// 中間アセンブリコードをダンプ
		if (shouldDump && (dumpTargetIsAll || dumpTarget.equals(OptionValue.DUMPER_TARGET_ASSEMBLY_CODE)) ) {
			this.dumpAssemblyCode(assemblyCode, dumpTargetIsAll, dumpStream);
		}

		return assemblyCode;
	}

	private void dumpInputtedCode(
			String[] inputtedCode, String[] scriptNames, boolean withHeader, PrintStream dumpStream) {

		int scriptLength = scriptNames.length;

		if (withHeader) {
			dumpStream.println("================================================================================");
			dumpStream.println("= Inputted Code");
			dumpStream.println("= - Input  of: org.vcssl.nano.compiler.Preprocessor");
			dumpStream.println("================================================================================");
		}

		for (int scriptIndex=0; scriptIndex<scriptLength; scriptIndex++) {
			if (2 <= scriptLength) {
				dumpStream.println("( " + scriptNames[scriptIndex] + ")");
			}
			dumpStream.println(inputtedCode[scriptIndex]);
		}

		if (withHeader) {
			dumpStream.println("");
		}
	}


	private void dumpPreprocessedCode(
			String[] preprocessedCode, String[] scriptNames, boolean withHeader, PrintStream dumpStream) {

		int scriptLength = scriptNames.length;

		if (withHeader) {
			dumpStream.println("================================================================================");
			dumpStream.println("= Preprocessed Code" );
			dumpStream.println("= - Output of: org.vcssl.nano.compiler.Preprocessor");
			dumpStream.println("= - Input  of: org.vcssl.nano.compiler.LexicalAnalyzer");
			dumpStream.println("================================================================================");
		}

		for (int scriptIndex=0; scriptIndex<scriptLength; scriptIndex++) {
			if (2 <= scriptLength) {
				dumpStream.println("( " + scriptNames[scriptIndex] + ")");
			}
			dumpStream.println(preprocessedCode[scriptIndex]);
		}

		if (withHeader) {
			dumpStream.println("");
		}
	}

	private void dumpTokens(Token[] tokens, boolean withHeader, PrintStream dumpStream) {

		if (withHeader) {
			dumpStream.println("================================================================================");
			dumpStream.println("= Tokens");
			dumpStream.println("= - Output of: org.vcssl.nano.compiler.LexicalAnalyzer");
			dumpStream.println("= - Input  of: org.vcssl.nano.compiler.Parser");
			dumpStream.println("================================================================================");
		}

		for (Token token: tokens) {
			dumpStream.println(token.toString());
		}

		if (withHeader) {
			dumpStream.println("");
		}
	}

	private void dumpParsedAst(AstNode astRootNode, boolean withHeader, PrintStream dumpStream) {

		if (withHeader) {
			dumpStream.println("================================================================================");
			dumpStream.println("= Parsed AST");
			dumpStream.println("= - Output of: org.vcssl.nano.compiler.Parser");
			dumpStream.println("= - Input  of: org.vcssl.nano.compiler.SemanticAnalyzer");
			dumpStream.println("================================================================================");
		}

		dumpStream.print(astRootNode.dump());

		if (withHeader) {
			dumpStream.println("");
		}
	}


	private void dumpAnalyzedAst(AstNode astRootNode, boolean withHeader, PrintStream dumpStream) {

		if (withHeader) {
			dumpStream.println("================================================================================");
			dumpStream.println("= Analyzed AST");
			dumpStream.println("= - Output of: org.vcssl.nano.compiler.SemanticAnalyzer");
			dumpStream.println("= - Input  of: org.vcssl.nano.compiler.CodeGenerator");
			dumpStream.println("================================================================================");
		}

		dumpStream.print(astRootNode.dump());

		if (withHeader) {
			dumpStream.println("");
		}
	}


	private void dumpAssemblyCode(String assemblyCode, boolean withHeader, PrintStream dumpStream) {

		if (withHeader) {
			dumpStream.println("================================================================================");
			dumpStream.println("= Assembly Code (VRIL Code)");
			dumpStream.println("= - Output of: org.vcssl.nano.compiler.CodeGenerator");
			dumpStream.println("= - Input  of: org.vcssl.nano.vm.assembler.Assembler");
			dumpStream.println("================================================================================");
		}

		dumpStream.print(assemblyCode);

		if (withHeader) {
			dumpStream.println("");
		}
	}

}
