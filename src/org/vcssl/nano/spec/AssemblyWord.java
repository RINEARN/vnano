/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import java.util.HashMap;

import org.vcssl.nano.vm.memory.Memory;


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
 * &raquo <a href="../../../../../src/org/vcssl/nano/spec/AssemblyWord.java">Source code</a>
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public final class AssemblyWord {

	// 中間アセンブリ言語の名称
	public static final String ASSEMBLY_LANGUAGE_NAME = "Vector Register Intermediate Language (VRIL)";

	// 中間アセンブリ言語のバージョン
	public static final String ASSEMBLY_LANGUAGE_VERSION = "0.0.1";


	// 書き込み用
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	public static final String INSTRUCTION_SEPARATOR = ";";
	public static final String WORD_SEPARATOR = "\t";
	public static final String VALUE_SEPARATOR = ":";
	public static final String IDENTIFIER_SERIAL_NUMBER_SEPARATOR = "@";
	public static final String INDENT = "\t";

	// 読み込み用
	public static final String LINE_SEPARATOR_REGEX = "\\r\\n|\\r|\\n";
	public static final String INSTRUCTION_SEPARATOR_REGEX = ";";
	public static final String WORD_SEPARATOR_REGEX = "\t| ";
	public static final String VALUE_SEPARATOR_REGEX = ":";
	public static final String IDENTIFIER_ADDRESS_SEPARATOR_REGEX = "@";
	public static final String INDENT_REGEX = "\t| ";

	public static final char   DIRECTIVE_PREFIX = '#';
	public static final String ASSEMBLY_LANGUAGE_IDENTIFIER_DIRECTIVE = "#ASSEMBLY_LANGUAGE_IDENTIFIER";
	public static final String ASSEMBLY_LANGUAGE_VERSION_DIRECTIVE = "#ASSEMBLY_LANGUAGE_VERSION";
	public static final String SCRIPT_LANGUAGE_IDENTIFIER_DIRECTIVE = "#SCRIPT_LANGUAGE_IDENTIFIER";
	public static final String SCRIPT_LANGUAGE_VERSION_DIRECTIVE = "#SCRIPT_LANGUAGE_VERSION";
	public static final String LOCAL_VARIABLE_DIRECTIVE = "#LOCAL_VARIABLE";
	public static final String GLOBAL_VARIABLE_DIRECTIVE = "#GLOBAL_VARIABLE";
	public static final String LOCAL_FUNCTION_DIRECTIVE = "#LOCAL_FUNCTION";
	public static final String GLOBAL_FUNCTION_DIRECTIVE = "#GLOBAL_FUNCTION";
	public static final String META_DIRECTIVE = "#META";
	public static final String LABEL_DIRECTIVE = "#LABEL";
	public static final String COMMENT_DIRECTIVE = "#COMMENT";

	public static final char OPERAND_PREFIX_GLOBAL = 'G';
	public static final char OPERAND_PREFIX_LOCAL = 'L';
	public static final char OPERAND_PREFIX_REGISTER = 'R';
	public static final char OPERAND_PREFIX_CONSTANT = 'C';   // 定数のアドレスを指定

	public static final char OPERAND_PREFIX_IDENTIFIER = '_';
	public static final char OPERAND_PREFIX_IMMEDIATE = '~';  // 定数の値そのものを即値で記述( % はインラインアセンブラでレジスタに使われている
	public static final char OPERAND_PREFIX_LABEL = '&';
	public static final char OPERAND_PREFIX_PLACEHOLDER = '-';   // オペランド順序を統一するため、値が無いオペランド位置に便宜的に置くプレースホルダ

	public static final HashMap<Character, Memory.Partition> OPERAND_PREFIX_PARTITION_MAP = new HashMap<Character, Memory.Partition>();
	static {
		OPERAND_PREFIX_PARTITION_MAP.put(Character.valueOf(OPERAND_PREFIX_GLOBAL), Memory.Partition.GLOBAL);
		OPERAND_PREFIX_PARTITION_MAP.put(Character.valueOf(OPERAND_PREFIX_LOCAL), Memory.Partition.LOCAL);
		OPERAND_PREFIX_PARTITION_MAP.put(Character.valueOf(OPERAND_PREFIX_REGISTER), Memory.Partition.REGISTER);
		OPERAND_PREFIX_PARTITION_MAP.put(Character.valueOf(OPERAND_PREFIX_CONSTANT), Memory.Partition.CONSTANT);
	}
	public static String getImmediateValueOf(String dataTypeName, String literal) {
		StringBuilder builder = new StringBuilder();
		builder.append(OPERAND_PREFIX_IMMEDIATE);
		builder.append(dataTypeName);
		builder.append(VALUE_SEPARATOR);
		builder.append(literal);
		return builder.toString();
	}

}
