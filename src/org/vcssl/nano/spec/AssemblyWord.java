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
	public String ASSEMBLY_LANGUAGE_NAME = "Vector Register Intermediate Language (VRIL)";

	// 中間アセンブリ言語のバージョン
	public String ASSEMBLY_LANGUAGE_VERSION = "0.0.1";


	// 書き込み用
	public String LINE_SEPARATOR = System.getProperty("line.separator");
	public String INSTRUCTION_SEPARATOR = ";";
	public String WORD_SEPARATOR = "\t";
	public String VALUE_SEPARATOR = ":";
	public String IDENTIFIER_SERIAL_NUMBER_SEPARATOR = "@";
	public String INDENT = "\t";

	// 読み込み用
	public String LINE_SEPARATOR_REGEX = "\\r\\n|\\r|\\n";
	public String INSTRUCTION_SEPARATOR_REGEX = ";";
	public String WORD_SEPARATOR_REGEX = "\t| ";
	public String VALUE_SEPARATOR_REGEX = ":";
	public String IDENTIFIER_ADDRESS_SEPARATOR_REGEX = "@";
	public String INDENT_REGEX = "\t| ";

	public char   DIRECTIVE_PREFIX = '#';
	public String ASSEMBLY_LANGUAGE_IDENTIFIER_DIRECTIVE = "#ASSEMBLY_LANGUAGE_IDENTIFIER";
	public String ASSEMBLY_LANGUAGE_VERSION_DIRECTIVE = "#ASSEMBLY_LANGUAGE_VERSION";
	public String SCRIPT_LANGUAGE_IDENTIFIER_DIRECTIVE = "#SCRIPT_LANGUAGE_IDENTIFIER";
	public String SCRIPT_LANGUAGE_VERSION_DIRECTIVE = "#SCRIPT_LANGUAGE_VERSION";
	public String LOCAL_VARIABLE_DIRECTIVE = "#LOCAL_VARIABLE";
	public String GLOBAL_VARIABLE_DIRECTIVE = "#GLOBAL_VARIABLE";
	public String LOCAL_FUNCTION_DIRECTIVE = "#LOCAL_FUNCTION";
	public String GLOBAL_FUNCTION_DIRECTIVE = "#GLOBAL_FUNCTION";
	public String META_DIRECTIVE = "#META";
	public String LABEL_DIRECTIVE = "#LABEL";
	public String COMMENT_DIRECTIVE = "#COMMENT";

	public char OPERAND_PREFIX_GLOBAL = 'G';
	public char OPERAND_PREFIX_LOCAL = 'L';
	public char OPERAND_PREFIX_REGISTER = 'R';
	public char OPERAND_PREFIX_CONSTANT = 'C';   // 定数のアドレスを指定

	public char OPERAND_PREFIX_IDENTIFIER = '_';
	public char OPERAND_PREFIX_IMMEDIATE = '~';  // 定数の値そのものを即値で記述( % はインラインアセンブラでレジスタに使われている
	public char OPERAND_PREFIX_LABEL = '&';
	public char OPERAND_PREFIX_PLACEHOLDER = '-';   // オペランド順序を統一するため、値が無いオペランド位置に便宜的に置くプレースホルダ

	@SuppressWarnings("serial")
	public Map<Character, Memory.Partition> OPERAND_PREFIX_PARTITION_MAP = new HashMap<Character, Memory.Partition>() {{
		put(Character.valueOf(OPERAND_PREFIX_GLOBAL), Memory.Partition.GLOBAL);
		put(Character.valueOf(OPERAND_PREFIX_LOCAL), Memory.Partition.LOCAL);
		put(Character.valueOf(OPERAND_PREFIX_REGISTER), Memory.Partition.REGISTER);
		put(Character.valueOf(OPERAND_PREFIX_CONSTANT), Memory.Partition.CONSTANT);
	}};

	public String getImmediateValueOf(String dataTypeName, String literal) {
		StringBuilder builder = new StringBuilder();
		builder.append(OPERAND_PREFIX_IMMEDIATE);
		builder.append(dataTypeName);
		builder.append(VALUE_SEPARATOR);
		builder.append(literal);
		return builder.toString();
	}

}
