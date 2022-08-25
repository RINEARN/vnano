/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import java.util.HashSet;
import java.util.Set;

/**
 * The class defining keywords and symbols of Vnano (as a language).
 */
public class ScriptWord {

	/** The name of the scripting language: "Vnano". */
	public static final String SCRIPT_LANGUAGE_NAME = "Vnano";

	/** The version of the scripting language: "Vnano". */
	public static final String SCRIPT_LANGUAGE_VERSION = EngineInformation.ENGINE_VERSION;

	/** The regular expression of separators of tokens (spaces, line feed code, and so on). */
	public static final String TOKEN_SEPARATOR_REGEX = "( |　|\t|\n|\r|\r\n|\n\r)";

	/** The separator of namespaces. */
	public static final String NAMESPACE_SEPARATOR = ".";

	/** The symbol of the end of statements: ";". */
	public static final String END_OF_STATEMENT = ";";

	/** The symbol of the assignment operator: "=". */
	public static final String ASSIGNMENT = "=";

	/** The symbol of the unary plus operator and the addition operator: "+". */
	public static final String PLUS_OR_ADDITION = "+";

	/** The symbol of the unary minus operator and the subtraction operator: "-". */
	public static final String MINUS_OR_SUBTRACTION = "-";

	/** The symbol of the multiplication operator: "*". */
	public static final String MULTIPLICATION = "*";

	/** The symbol of the division operator: "/". */
	public static final String DIVISION = "/";

	/** The symbol of the remainder operator: "%". */
	public static final String REMAINDER = "%";

	/** The symbol of the compound assignment operator of the addition: "+=". */
	public static final String ADDITION_ASSIGNMENT = "+=";

	/** The symbol of the compound assignment operator of the subtraction: "-=". */
	public static final String SUBTRACTION_ASSIGNMENT = "-=";

	/** The symbol of the compound assignment operator of the multiplication: "*=". */
	public static final String MULTIPLICATION_ASSIGNMENT = "*=";

	/** The symbol of the compound assignment operator of the division: "/=". */
	public static final String DIVISION_ASSIGNMENT = "/=";

	/** The symbol of the compound assignment operator of the remainder: "%=". */
	public static final String REMAINDER_ASSIGNMENT = "%=";

	/** The symbol of the prefix/postfix increment operator: "++". */
	public static final String INCREMENT = "++";

	/** The symbol of the prefix/postfix decrement operator: "--". */
	public static final String DECREMENT = "--";

	/** The symbol of the equality comparison operator: "==". */
	public static final String EQUAL = "==";

	/** The symbol of the "non-equality" comparison operator: "&#33;=". */
	public static final String NOT_EQUAL = "!=";

	/** The symbol of the "greater-than" comparison operator: "&gt;". */
	public static final String GREATER_THAN = ">";

	/** The symbol of the "greater-equal" comparison operator: "&gt;=". */
	public static final String GREATER_EQUAL = ">=";

	/** The symbol of the "less-than" comparison operator: "&lt;". */
	public static final String LESS_THAN = "<";

	/** The symbol of the "less-equal" comparison operator: "&lt;=". */
	public static final String LESS_EQUAL = "<=";

	/** The symbol of logical-and operator with short-circuit evaluation: "&amp;&amp;". */
	public static final String SHORT_CIRCUIT_AND = "&&";

	/** The symbol of logical-or operator with short-curcuit evaluation: "||". */
	public static final String SHORT_CIRCUIT_OR = "||";

	/** The symbol of logical-not operator: "&#33;". */
	public static final String NOT = "!";

	/** The symbol of the beginning of the parenthesis: "(". */
	public static final String PARENTHESIS_BEGIN = "(";

	/** The symbol of the end of the parenthesis: ")". */
	public static final String PARENTHESIS_END = ")";

	/** The symbol of separators of arguments: ",". */
	public static final String ARGUMENT_SEPARATOR = ",";

	/** The symbol of the beginning of the array index: "[". */
	public static final String SUBSCRIPT_BEGIN = "[";

	/** The symbol of the end of the array index: "[". */
	public static final String SUBSCRIPT_END = "]";

	/** The symbol of the beginning of the multi-dimensional array indices: "][". */
	public static final String SUBSCRIPT_SEPARATOR = "][";

	/** The symbol of the beginning of the block: "{". */
	public static final String BLOCK_BEGIN = "{";

	/** The symbol of the beginning of the block: "}". */
	public static final String BLOCK_END = "}";

	/** The keyword of the beginning of if statements: "if". */
	public static final String IF = "if";

	/** The keyword of the beginning of else statements: "else". */
	public static final String ELSE = "else";

	/** The keyword of the beginning of for statements: "for". */
	public static final String FOR = "for";

	/** The keyword of the beginning of while statements: "while". */
	public static final String WHILE = "while";

	/** The keyword of break statements: "break". */
	public static final String BREAK = "break";

	/** The keyword of continue statements: "continue". */
	public static final String CONTINUE = "continue";

	/** The keyword of the beginning of return statements: "return". */
	public static final String RETURN = "return";

	/** The symbol represents that the number of somethings is arbitrary: "...". */
	public static final String ARBITRARY_COUNT_MODIFIER = "...";

	/**
	 * The symbol representing the reference: "&amp;".
	 * 
	 * In the current version,
	 * this symbol is used only for a kind of modifier representing call-by-reference,
	 * and it has not supported as an operator.
	 */
	public static final String REF_MODIFIER = "&";

	/** The modifier representing being constModifier: "const". */
	public static final String CONST_MODIFIER = "const";

	/** The symbol of the beginning of line comments: "//". */
	public static final String LINE_COMMENT_PREFIX = "//";

	/** The symbol of the beginning of block comments: "/&#42;". */
	public static final String BLOCK_COMMENT_BEGIN = "/*";

	/** The symbol of the beginning of block comments: "&#42;/". */
	public static final String BLOCK_COMMENT_END = "*/";


	/** The HashSet storing all syntax keywords. */
	@SuppressWarnings("serial")
	public static final Set<String> STATEMENT_NAME_SET = new HashSet<String>() {{
		add(IF);
		add(ELSE);
		add(FOR);
		add(WHILE);
		add(BREAK);
		add(CONTINUE);
		add(RETURN);
	}};

	/** The HashSet storing all syntax symbols. */
	@SuppressWarnings("serial")
	public static final Set<String> SYMBOL_SET = new HashSet<String>() {{

		// For the current implementation of the LexicalAnalyzer class, it is required that 
		// the first 1 or 2 character(s) of an operator consists of multiple characters also can be operator.
		// In the contrast, the first 1 or 2 character(s) of a syntax keyword (if, for, ...) must not match as an operator.

    	add(ASSIGNMENT);
    	add(PLUS_OR_ADDITION);
    	add(MINUS_OR_SUBTRACTION);
    	add(MULTIPLICATION);
    	add(DIVISION);
    	add(REMAINDER);

    	add(ADDITION_ASSIGNMENT);
    	add(SUBTRACTION_ASSIGNMENT);
    	add(MULTIPLICATION_ASSIGNMENT);
    	add(DIVISION_ASSIGNMENT);
    	add(REMAINDER_ASSIGNMENT);

    	add(INCREMENT);
    	add(DECREMENT);

    	add(GREATER_THAN);
    	add(GREATER_EQUAL);

    	add(LESS_THAN);
    	add(LESS_EQUAL);

    	add(EQUAL);
    	add(NOT_EQUAL);

    	add(SHORT_CIRCUIT_AND);
    	add(SHORT_CIRCUIT_OR);
    	add(NOT);

    	add(ARGUMENT_SEPARATOR);
    	add(ARBITRARY_COUNT_MODIFIER);
    	add(REF_MODIFIER);

    	add(PARENTHESIS_BEGIN);
    	add(PARENTHESIS_END);
    	add(BLOCK_BEGIN);
    	add(BLOCK_END);
    	add(SUBSCRIPT_BEGIN);
    	add(SUBSCRIPT_SEPARATOR);
    	add(SUBSCRIPT_END);

    	add(END_OF_STATEMENT);
    }};


	/** The HashSet storing all modifiers. */
	@SuppressWarnings("serial")
	public static final Set<String> MODIFIER_SET = new HashSet<String>() {{
		add(CONST_MODIFIER);
		add(ARBITRARY_COUNT_MODIFIER);
		add(REF_MODIFIER);
	}};

	/** The HashSet storing modifiers which will be put before the type name. */
	@SuppressWarnings("serial")
	public static final Set<String> PREFIX_MODIFIER_SET = new HashSet<String>() {{
		add(CONST_MODIFIER);
		add(ARBITRARY_COUNT_MODIFIER);
	}};

	/** The HashSet storing modifiers which will be put after the type name. */
	@SuppressWarnings("serial")
	public static final Set<String> POSTFIX_MODIFIER_SET = new HashSet<String>() {{
		add(REF_MODIFIER);
	}};

	/** The HashSet storing reserved words. */
	@SuppressWarnings("serial")
	public static final Set<String> RESERVED_WORD_SET = new HashSet<String>() {{

		add("int");
		add("float");
		add("long");
		add("double");
		add("string");
		add("bool");
		add("void");

		add("int8");
		add("int16");
		add("int32");
		add("int64");
		add("int128");
		add("int256");
		add("int512");
		add("float8");
		add("float16");
		add("float32");
		add("float64");
		add("float128");
		add("float256");
		add("float512");

		add("byte");
		add("bit");

		add("null");
		add("NULL");

		add("class");
		add("struct");
		add("enum");

		add(IF);
		add(ELSE);
		add(FOR);
		add(WHILE);
		add(BREAK);
		add(CONTINUE);
		add(RETURN);
		add("switch");
		add("case");
		add("default");
		add("do");
		add("goto");
		add("try");
		add("catch");
		add("throw");

		add("alloc");
		add("free");
		add("new");
		add("delete");

		add("const");
		add("public");
		add("private");
		add("protected");

		add("include");
		add("import");
		add("extern");
		add("coding");

		add("this");
		add("super");

		add("reference");
		add("ref");
	}};
}

