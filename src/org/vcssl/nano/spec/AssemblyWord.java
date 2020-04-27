/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import java.util.HashMap;
import java.util.Map;

import org.vcssl.nano.vm.memory.Memory;

// Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/spec/AssemblyWord.html
// ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/spec/AssemblyWord.html

/**
 * <p>
 * <span>
 * <span class="lang-en">
 * The class to define keywords and symbols of the virtual assembly language (VRIL)
 * which is interpreted by the VM in the script engine of the Vnano
 * </span>
 * <span class="lang-ja">
 * Vnano のスクリプトエンジン内のVMが解釈する仮想的なアセンブリ言語（ VRIL ）における,
 * キーワードや記号などが定義されたクラスです
 * </span>
 * .
 * </p>
 *
 * <p>
 * &raquo; <a href="../../../../../src/org/vcssl/nano/spec/AssemblyWord.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/spec/AssemblyWord.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/spec/AssemblyWord.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public final class AssemblyWord {


	// 各フィールドは元々は static final でしたが、カスタマイズの事を考慮して、動的なフィールドに変更されました。
	// これにより、このクラスのインスタンスを生成して値を変更し、
	// それを LanguageSpecContainer に持たせて VnanoEngle クラスのコンストラクタに渡す事で、
	// 処理系内のソースコードを保ったまま（再ビルド不要で）定義類を差し替える事ができます。


	// 中間アセンブリ言語の名称
	public String assemblyLanguageName = "Vector Register Intermediate Language (VRIL)";

	// 中間アセンブリ言語のバージョン
	public String assemblyLanguageVersion = "0.0.1";


	// 書き込み用
	public String lineSeparator = System.getProperty("line.separator");
	public String instructionSeparator = ";";
	public String wordSeparator = "\t";
	public String valueSeparator = ":";
	public String identifierSerialNumberSeparator = "@";
	public String indent = "\t";

	// 読み込み用
	public String lineSeparatorRegex = "\\r\\n|\\r|\\n";
	public String instructionSeparatorRegex = ";";
	public String wordSeparatorRegex = "\t| ";
	public String valueSeparatorRegex = ":";
	public String identifierSerianNumberSeparatorRegex = "@";
	public String indentRegex = "\t| ";

	public char   directivePrefix = '#';
	public String assemblyLanguageIdentifierDirective = "#ASSEMBLY_LANGUAGE_IDENTIFIER";
	public String assemblyLanguageVersionDirective = "#assemblyLanguageVersion";
	public String scriptLanguageIdentifierDirective = "#SCRIPT_LANGUAGE_IDENTIFIER";
	public String scriptLanguageVersionDirective = "#scriptLanguageVersion";
	public String localVariableDirective = "#LOCAL_VARIABLE";
	public String globalVariableDirective = "#GLOBAL_VARIABLE";
	public String localFunctionDirective = "#LOCAL_FUNCTION";
	public String globalFunctionDirective = "#GLOBAL_FUNCTION";
	public String metaDirective = "#META";
	public String labelDirective = "#LABEL";
	public String commentDirective = "#COMMENT";

	public char globalOperandPrefix = 'G';
	public char localOperandPrefix = 'L';
	public char registerOperandOprefix = 'R';
	public char constantOperandPrefix = 'C';   // 定数のアドレスを指定

	public char identifierOperandPrefix = '_';
	public char immediateOperandPrefix = '~';  // 定数の値そのものを即値で記述( % はインラインアセンブラでレジスタに使われている
	public char labelOperandPrefix = '&';
	public char placeholderOperandPrefix = '-';   // オペランド順序を統一するため、値が無いオペランド位置に便宜的に置くプレースホルダ

	@SuppressWarnings("serial")
	public Map<Character, Memory.Partition> operandPrefixPartitionMap = new HashMap<Character, Memory.Partition>() {{
		put(Character.valueOf(globalOperandPrefix), Memory.Partition.GLOBAL);
		put(Character.valueOf(localOperandPrefix), Memory.Partition.LOCAL);
		put(Character.valueOf(registerOperandOprefix), Memory.Partition.REGISTER);
		put(Character.valueOf(constantOperandPrefix), Memory.Partition.CONSTANT);
	}};

	public String getImmediateValueOf(String dataTypeName, String literal) {
		StringBuilder builder = new StringBuilder();
		builder.append(immediateOperandPrefix);
		builder.append(dataTypeName);
		builder.append(valueSeparator);
		builder.append(literal);
		return builder.toString();
	}

}
