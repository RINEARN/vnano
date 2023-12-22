/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import java.util.HashMap;
import java.util.Map;

import org.vcssl.nano.vm.memory.Memory;


/**
 * The class to define keywords and symbols of the virtual assembly language (VRIL)
 * which is interpreted by the VM in the script engine of the Vnano.
 */
public final class AssemblyWord {

	/** The name of the assembly language. */
	public static final String ASSEMBLY_LANGUAGE_NAME = "Vector Register Intermediate Language (VRIL)";

	/** The version of the assembly language. */
	public static final String ASSEMBLY_LANGUAGE_VERSION = "0.0.1";


	/** The line feed code, used when generating assembly code. */
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	/** The symbol which should be put at the end of each instruction. */
	public static final String INSTRUCTION_SEPARATOR = ";";

	/** The separator which should be put between words (operation codes, operands, and so on). */
	public static final String WORD_SEPARATOR = "\t";

	/** The separator which should be put between multiple values in an operand. */
	public static final String VALUE_SEPARATOR = ":";

	/** The separator which should be put between a name and a serial number, in an identifier operand. */
	public static final String IDENTIFIER_SERIAL_NUMBER_SEPARATOR = "@";

	/** The space character(s) for indenting, used when generating assembly code. */
	public static final String INDENT = "\t";


	/** The regular expression of line separators. */
	public static final String LINE_SEPARATOR_REGEX = "\\r\\n|\\r|\\n";

	/** The regular expression of the instruction-separator symbol. */
	public static final String INSTRUCTION_SEPARATOR_REGEX = ";";

	/** The regular expression of the word-separator symbol. */
	public static final String WORD_SEPARATOR_REGEX = "\t| ";

	/** The regular expression of the value-separator symbol. */
	public static final String VALUE_SEPARATOR_REGEX = ":";

	/** The regular expression of a serial number in a variable identifier. */
	public static final String IDENTIFIER_SERIAL_NUMBER_REGEX = "@";

	/** The regular expression of the indent characters. */
	public static final String INDENT_REGEX = "\t| ";


	/** The symbol put at the beginnings of directive lines. */
	public static final char   DIRECTIVE_PREFIX = '#';

	/** The name of the intermediate assembly language, supported by the VM of this script engine. */
	public static final String ASSEMBLY_LANGUAGE_IDENTIFIER_DIRECTIVE = "#ASSEMBLY_LANGUAGE_IDENTIFIER";

	/** The version of the intermediate assembly language, supported by the VM of this script engine. */
	public static final String ASSEMBLY_LANGUAGE_VERSION_DIRECTIVE = "#assemblyLanguageVersion";

	/** The name of the scripting language, supported by the VM of this script engine. */
	public static final String SCRIPT_LANGUAGE_IDENTIFIER_DIRECTIVE = "#SCRIPT_LANGUAGE_IDENTIFIER";

	/** The version of the scripting language, supported by the VM of this script engine. */
	public static final String SCRIPT_LANGUAGE_VERSION_DIRECTIVE = "#scriptLanguageVersion";

	/** The beginning string of a directive for declaring a local variable. */
	public static final String LOCAL_VARIABLE_DIRECTIVE = "#LOCAL_VARIABLE";

	/** The beginning string of a directive for declaring a global variable. */
	public static final String GLOBAL_VARIABLE_DIRECTIVE = "#GLOBAL_VARIABLE";

	/** The beginning string of a directive for declaring a local function. */
	public static final String LOCAL_FUNCTION_DIRECTIVE = "#LOCAL_FUNCTION";

	/** The beginning string of a directive for declaring a global variable. */
	public static final String GLOBAL_FUNCTION_DIRECTIVE = "#GLOBAL_FUNCTION";

	/** The beginning string of a meta-information directive. */
	public static final String META_DIRECTIVE = "#META";

	/** The beginning string of a directive for declaring a label. */
	public static final String LABEL_DIRECTIVE = "#LABEL";

	/** The beginning string of a comment directive. */
	public static final String COMMENT_DIRECTIVE = "#COMMENT";

	/** The prefix of global address operands. */
	public static final char GLOBAL_OPERAND_PREFIX = 'G';

	/** The prefix of local address operands. */
	public static final char LOCAL_OPERAND_PREFIX = 'L';

	/** The prefix of constant address operands. */
	public static final char CONSTANT_OPERAND_PREFIX = 'C';

	/** The prefix of register operands. */
	public static final char REGISTER_OPERAND_PREFIX = 'R';

	/** The prefix of identifier operands. */
	public static final char IDENTIFIER_OPERAND_PREFIX = '_';

	/** The prefix of immediate value operands. */
	public static final char IMMEDIATE_OPERAND_PREFIX = '~';

	/** The prefix of label operands. */
	public static final char LABEL_OPERAND_PREFIX = '&';

	/** The prefix of placeholder operands. */
	public static final char PLACEHOLDER_OPERAND_PREFIX = '-';

	/** The Map for converting each operand-prefix character to the corresponding element of Memory.Partition enum. */
	@SuppressWarnings("serial")
	public static final Map<Character, Memory.Partition> OPERAND_PREFIX_PARTITION_MAP = new HashMap<Character, Memory.Partition>() {{
		put(Character.valueOf(GLOBAL_OPERAND_PREFIX), Memory.Partition.GLOBAL);
		put(Character.valueOf(LOCAL_OPERAND_PREFIX), Memory.Partition.LOCAL);
		put(Character.valueOf(REGISTER_OPERAND_PREFIX), Memory.Partition.REGISTER);
		put(Character.valueOf(CONSTANT_OPERAND_PREFIX), Memory.Partition.CONSTANT);
	}};

	/**
	 * Returns the immediate value of the specified literal, available in assembly code.
	 *
	 * @param dataTypeName The name of the data type of the literal.
	 * @param literal The value of the literal in script code.
	 * @return The immediate value available in the assembly code.
	 */
	public static final String getImmediateValueOf(String dataTypeName, String literal) {
		StringBuilder builder = new StringBuilder();
		builder.append(IMMEDIATE_OPERAND_PREFIX);
		builder.append(dataTypeName);
		builder.append(VALUE_SEPARATOR);
		builder.append(literal);
		return builder.toString();
	}

}
