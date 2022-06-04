/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.OptionKey;
import org.vcssl.nano.spec.OptionValue;


/**
 * The class performing the function of a compiler in the script engine of the Vnano.
 *
 * This class compiles script code written in the Vnano to a kind of intermediate code, named as "VRIL" code.
 * VRIL (Vector Register Intermediate Language) is a low-level (but readable text format) language
 * designed as a virtual assembly code of the VM (Virtual Machine) layer of Vnano Engine.
 */
public class Compiler {

	/**
	 * Create a new compiler.
	 */
	public Compiler() {
	}


	/**
	 * Compiles the script code written in the Vnano to VRIL code.
	 *
	 * @param scripts Code of scripts to be compiled.
	 * @param names Names of scripts.
	 * @param interconnect The interconnect to which external functions/variables are connected.
	 * @return The compiled VRIL code.
	 * @throws VnanoException Thrown when a syntax error will be detected for the content of the script.
	 */
	public String compile(String[] scripts, String[] names, Interconnect interconnect)
					throws VnanoException {

		if (scripts.length != names.length) {
			throw new VnanoFatalException("Array-lengths of \"scripts\" and \"names\" arguments are mismatching.");
		}

		// Get the option map, which is a map storing sets of names and values of options.
		// (The contents of the option map had already been normalized in Interconnect.)
		Map<String, Object> optionMap = interconnect.getOptionMap();

		// Get the total number of scripts.
		int scriptLength = scripts.length;

		// Check whether EVAL_INT_LITERAL_AS_FLOAT option is enabled, 
		// where EVAL_INT_LITERAL_AS_FLOAT is the option to handle all integer literals in scripts as float-type values.
		// It is useful for purposes calculating values of expressions.
		boolean evalNumberAsFloat = (Boolean)optionMap.get(OptionKey.EVAL_INT_LITERAL_AS_FLOAT);

		// Get values of options to dump parsed/compiled contents for debugging.
		boolean shouldDump = (Boolean)optionMap.get(OptionKey.DUMPER_ENABLED);
		String dumpTarget = (String)optionMap.get(OptionKey.DUMPER_TARGET);
		boolean dumpTargetIsAll = dumpTarget.equals(OptionValue.DUMPER_TARGET_ALL);
		PrintStream dumpStream = (PrintStream)optionMap.get(OptionKey.DUMPER_STREAM);


		// Dump inputted scripts.
		if (shouldDump && (dumpTargetIsAll || dumpTarget.equals(OptionValue.DUMPER_TARGET_INPUTTED_CODE)) ) {
			this.dumpInputtedCode(scripts, names, dumpTargetIsAll, dumpStream);
		}


		// By preprocessor, remove comments, and replace line feeds to LF (0x0A).
		String[] preprocessedScripts = new String[scriptLength];
		for (int scriptIndex=0; scriptIndex<scriptLength; scriptIndex++) {
			preprocessedScripts[scriptIndex] = new Preprocessor().preprocess(scripts[scriptIndex]);
		}

		// Dump preprocessed scripts.
		if (shouldDump && (dumpTargetIsAll || dumpTarget.equals(OptionValue.DUMPER_TARGET_PREPROCESSED_CODE)) ) {
			this.dumpPreprocessedCode(preprocessedScripts, names, dumpTargetIsAll, dumpStream);
		}


		// By LexicalAnalyzer, split the script into tokens.
		LexicalAnalyzer lexer = new LexicalAnalyzer();
		Token[][] tokens = new Token[scriptLength][]; // [ index of script ][ index of token ]
		for (int scriptIndex=0; scriptIndex<scriptLength; scriptIndex++) {
			tokens[scriptIndex] = lexer.analyze(preprocessedScripts[scriptIndex], names[scriptIndex]);
		}

		// If EVAL_INT_LITERALS_AS_FLOAT option is enabled, replaces data-types of all integer literals to "float".
		if (evalNumberAsFloat) {
			tokens[scriptLength-1] = this.replaceDataTypeOfLiteralTokens(
				tokens[scriptLength-1], DataTypeName.DEFAULT_INT, DataTypeName.DEFAULT_FLOAT
			);
			// where tokens[scriptLength-1] are tokens of the (main) script 
			// passes as an argument of "executeScript(String script)" method of VnanoEngine.
		}

		// Marge tokens of all scripts.
		// Why we haven't marge them before here is: to display correct cause information when any error is detected.
		// Line numbers and script names are set to tokens by LexicalAnalyzer, so if we link scripts before lexical analysis, 
		// incorrect line numbers and script names will be set to tokens, and displayed in error messages.
		List<Token> tokenList = new ArrayList<Token>();
		for (int scriptIndex=0; scriptIndex<scriptLength; scriptIndex++) {
			for (Token token: tokens[scriptIndex]) {
				tokenList.add(token);
			}
		}
		Token[] unifiedTokens = tokenList.toArray(new Token[0]);


		// Dump tokens.
		if (shouldDump && (dumpTargetIsAll || dumpTarget.equals(OptionValue.DUMPER_TARGET_TOKEN)) ) {
			this.dumpTokens(unifiedTokens, dumpTargetIsAll, dumpStream);
		}


		// By Parser, construct an AST from tokens.
		AstNode parsedAstRootNode = new Parser().parse(unifiedTokens);

		// Dump the parsed AST.
		if (shouldDump && (dumpTargetIsAll || dumpTarget.equals(OptionValue.DUMPER_TARGET_PARSED_AST)) ) {
			this.dumpParsedAst(parsedAstRootNode, dumpTargetIsAll, dumpStream);
		}


		// By SemanticAnalyzer, analyze/supplement data-types and so on of nodes in the AST.
		AstNode analyzedAstRootNode = new SemanticAnalyzer().analyze(parsedAstRootNode, interconnect);

		// Dump the analyzed AST.
		if (shouldDump && (dumpTargetIsAll || dumpTarget.equals(OptionValue.DUMPER_TARGET_ANALYZED_AST)) ) {
			this.dumpAnalyzedAst(analyzedAstRootNode, dumpTargetIsAll, dumpStream);
		}


		// By CodeGenerator, generate the intermediate assembly code (VRIL assembly code) processable on the VM.
		String assemblyCode = new CodeGenerator().generate(analyzedAstRootNode);

		// Dump the VRIL assembly code.
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
				dumpStream.println("(" + scriptNames[scriptIndex] + ")");
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
			dumpStream.println("= VRIL Assembly Code");
			dumpStream.println("= - Output of: org.vcssl.nano.compiler.CodeGenerator");
			dumpStream.println("= - Input  of: org.vcssl.nano.vm.assembler.Assembler");
			dumpStream.println("================================================================================");
		}

		dumpStream.print(assemblyCode);

		if (withHeader) {
			dumpStream.println("");
		}
	}


	// For EVAL_INT_LITERAL_AS_FLOAT option
	private Token[] replaceDataTypeOfLiteralTokens(Token[] tokens, String fromTypeName, String toTypeName) {
		int tokenLength = tokens.length;
		Token[] replacedTokens = new Token[tokenLength];
		for (int tokenIndex=0; tokenIndex<tokenLength; tokenIndex++) {
			Token token = tokens[tokenIndex].clone();
			if (token.getType() == Token.Type.LEAF
				&& token.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.LITERAL)
				&& token.getAttribute(AttributeKey.DATA_TYPE).equals(DataTypeName.DEFAULT_INT) ) {

				token.setAttribute(AttributeKey.DATA_TYPE, DataTypeName.DEFAULT_FLOAT);
			}
			replacedTokens[tokenIndex] = token;
		}
		return replacedTokens;
	}

}
