/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.memory.DataException;

/**
 * <p>
 * Vnano処理系内において、
 * Vnanoで記述されたスクリプトコード（文字列）を、
 * {@link org.vcssl.nano.assembler.Assembler Assembler}
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
	 * {@link org.vcssl.nano.assembler.Assembler Assembler}
	 * が解釈可能な仮想アセンブリコード（文字列）に変換して返します。
	 *
	 * @param script スクリプトコード
	 * @param fileName スクリプトのファイル名
	 * @param Intterconnect interconnect スクリプト内で参照する外部変数・関数の情報を保持しているインターコネクト
	 * @return 仮想アセンブリコード
	 * @throws ScriptCodeException スクリプトコードの内容に異常があった場合にスローされます。
	 */
	public String compile(String script, String fileName, Interconnect interconnect)
					throws ScriptCodeException, DataException { // スクリプト内の型エラーはScriptCodeExceptionに入れるべき？


		// デバッグ用出力（入力スクリプト）
		System.out.println(script);
		System.out.println("-----");


		// 字句解析でトークン配列を生成
		Token[] tokens = new LexicalAnalyzer().analyze(script, fileName);


		// デバッグ用出力（トークン配列）
		for (Token token : tokens) {
			System.out.println(token);
		}
		System.out.println("-----");


		// 構文解析でAST（抽象構文木）を生成
		AstNode parsedNode = new Parser().parse(tokens);


		// デバッグ用出力（構文解析後のAST）
		System.out.println(parsedNode.toString());
		System.out.println("-----");



		// 意味解析でASTの情報を補間
		AstNode analyzedNode = new SemanticAnalyzer().analyze(parsedNode, interconnect);


		// デバッグ用出力（意味解析後のAST）
		System.out.println(analyzedNode.toString());
		System.out.println("-----");


		// 中間アセンブリコードを生成
		String assemblyCode = new CodeGenerator().generate(analyzedNode);


		// デバッグ用出力（中間アセンブリコード）
		System.out.println(assemblyCode);
		System.out.println("-----");


		return assemblyCode;
	}

}
