/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
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
 * Vnano処理系内において、
 * Vnanoで記述されたスクリプトコード（文字列）を、
 * {@link org.vcssl.nano.vm.assembler.Assembler Assembler}
 * が解釈可能な仮想アセンブリコード（文字列）に変換する、コンパイラのクラスです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class Compiler {

	/**
	 * このクラスは状態を保持するフィールドを持たないため、コンストラクタは何もしません。
	 */
	public Compiler() {
	}


	/**
	 * Vnanoで記述されたスクリプトコード（文字列）を、
	 * {@link org.vcssl.nano.vm.assembler.Assembler Assembler}
	 * が解釈可能な仮想アセンブリコード（文字列）に変換して返します。
	 *
	 * @param scripts スクリプトコード
	 * @param names スクリプトのファイル名
	 * @param Intterconnect interconnect スクリプト内で参照する外部変数・関数の情報を保持しているインターコネクト
	 * @param optionMap オプション名と値を保持するマップ
	 * @return 仮想アセンブリコード
	 * @throws VnanoException スクリプトコードの内容に異常があった場合にスローされます。
	 */
	public String compile(String[] scripts, String[] names, Interconnect interconnect, Map<String, Object> optionMap)
					throws VnanoException { // スクリプト内の型エラーはScriptCodeExceptionに入れるべき？

		// スクリプトコードの枚数とスクリプト名の個数が違う場合はエラー
		if (scripts.length != names.length) {
			throw new VnanoFatalException(
				"The array-length of \"scripts\" argument should be same with the length of \"names\" argument"
			);
		}

		// 最初に全スクリプトコードを結合してから処理すると、エラーメッセージの行番号などがずれてしまうため、
		// 字句解析までは別々に処理し、行番号コードなどを保持するトークン配列まで変換してから結合する

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

		// EVAL_NUMBER_AS_FLOAT オプションが有効な場合、eval対象スクリプトコードのintリテラルの型をfloatに変更
		if (evalNumberAsFloat) {
			int evalScriptIndex = scriptLength - 1; // eval対象スクリプトコードは、引数 scripts の最終要素に格納されている
			tokens[evalScriptIndex] = lexer.replaceDataTypeOfLiteralTokens(
				tokens[evalScriptIndex], DataTypeName.INT, DataTypeName.FLOAT
			);
		}

		// 全スクリプトのトークン配列を結合（各トークンが行番号情報などを保持しているため、結合によってずれる事はない）
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
			this.dumpAnalyzedAst(parsedAstRootNode, dumpTargetIsAll, dumpStream);
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
