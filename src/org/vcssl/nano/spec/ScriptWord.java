/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import java.util.HashSet;

// Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/spec/ScriptWord.html
// ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/spec/ScriptWord.html

/**
 * <p>
 * <span>
 * <span class="lang-en">
 * The class to define keywords and symbols of the scripting language (default: Vnano)
 * provided by this script engine
 * </span>
 * <span class="lang-ja">
 * このスクリプトエンジンが提供するスクリプト言語（ 標準では Vnano ）における,
 * キーワードや記号などが定義されたクラスです
 * </span>
 * .
 * </p>
 *
 * <p>
 * &raquo; <a href="../../../../../src/org/vcssl/nano/spec/ScriptWord.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/spec/ScriptWord.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/spec/ScriptWord.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class ScriptWord {

	/**
	 * <span class="lang-en">The name of the script language: "Vnano"</span>
	 * <span class="lang-ja">スクリプト言語の名称（ Vnano ）です</span>
	 * .
	 */
	public static final String SCRIPT_LANGUAGE_NAME = "Vnano";


	/**
	 * <span class="lang-en">The version of the script language: "Vnano"</span>
	 * <span class="lang-ja">スクリプト言語のバージョンです</span>
	 * .
	 */
	public static final String SCRIPT_LANGUAGE_VERSION = EngineInformation.ENGINE_VERSION;


	/**
	 * <span class="lang-en">The regular expression of separators of tokens (spaces, line feed code, and so on)</span>
	 * <span class="lang-ja">空白や改行など、トークンの区切りとなる文字の正規表現です</span>
	 * .
	 */
	public static final String TOKEN_SEPARATOR_REGEX = "( |　|\t|\n|\r|\r\n|\n\r)";


	/**
	 * <span class="lang-en">The separator of name spaces</span>
	 * <span class="lang-ja">名前空間の区切りです</span>
	 * .
	 */
	public static final String NAME_SPACE_SEPARATOR = ".";


	/**
	 * <span class="lang-en">The symbol of the end of statements: ";"</span>
	 * <span class="lang-ja">文末記号「 ; 」です</span>
	 * .
	 */
	public static final String END_OF_STATEMENT = ";";


	/**
	 * <span class="lang-en">The symbol represents that the number of somethings is arbitrary: "..."</span>
	 * <span class="lang-ja">任意の個数を表す記号「 ... 」です。</span>
	 * .
	 */
	public static final String ARBITRARY_COUNT = "...";


	/**
	 * <span class="lang-en">The symbol of the assignment operator: "="</span>
	 * <span class="lang-ja">代入演算子の記号「 = 」です</span>
	 * .
	 */
	public static final String ASSIGNMENT = "=";


	/**
	 * <span class="lang-en">The symbol of the unary plus operator and the addition operator: "+"</span>
	 * <span class="lang-ja">単項プラス演算子および加算演算子の記号「 + 」です</span>
	 * .
	 */
	public static final String PLUS = "+";


	/**
	 * <span class="lang-en">The symbol of the unary minus operator and the subtraction operator: "-"</span>
	 * <span class="lang-ja">単項マイナス演算子および加算演算子の記号「 - 」です</span>
	 * .
	 */
	public static final String MINUS = "-";


	/**
	 * <span class="lang-en">The symbol of the multiplication operator: "*"</span>
	 * <span class="lang-ja">乗算演算子の記号「 * 」です</span>
	 * .
	 */
	public static final String MULTIPLICATION = "*";


	/**
	 * <span class="lang-en">The symbol of the division operator: "/"</span>
	 * <span class="lang-ja">除算演算子の記号「 / 」です</span>
	 * .
	 */
	public static final String DIVISION = "/";


	/**
	 * <span class="lang-en">The symbol of the remainder operator: "%"</span>
	 * <span class="lang-ja">剰余演算子の記号「 % 」です</span>
	 * .
	 */
	public static final String REMAINDER = "%";


	/**
	 * <span class="lang-en">The symbol of the compound assignment operator of the addition: "+="</span>
	 * <span class="lang-ja">可算との複合代入演算子の記号「 += 」です</span>
	 * .
	 */
	public static final String ADDITION_ASSIGNMENT = "+=";


	/**
	 * <span class="lang-en">The symbol of the compound assignment operator of the subtraction: "-="</span>
	 * <span class="lang-ja">減算との複合代入演算子の記号「 -= 」です</span>
	 * .
	 */
	public static final String SUBTRACTION_ASSIGNMENT = "-=";


	/**
	 * <span class="lang-en">The symbol of the compound assignment operator of the multiplication: "*="</span>
	 * <span class="lang-ja">乗算との複合代入演算子の記号「 *= 」です</span>
	 * .
	 */
	public static final String MULTIPLICATION_ASSIGNMENT = "*=";


	/**
	 * <span class="lang-en">The symbol of the compound assignment operator of the division: "/="</span>
	 * <span class="lang-ja">除算との複合代入演算子の記号「 /= 」です</span>
	 * .
	 */
	public static final String DIVISION_ASSIGNMENT = "/=";


	/**
	 * <span class="lang-en">The symbol of the compound assignment operator of the remainder: "%="</span>
	 * <span class="lang-ja">剰余演算との複合代入演算子の記号「 %= 」です</span>
	 * .
	 */
	public static final String REMAINDER_ASSIGNMENT = "%=";


	/**
	 * <span class="lang-en">The symbol of the prefix/postfix increment operator: "++"</span>
	 * <span class="lang-ja">前置/後置インクリメント演算子の記号「 ++ 」です</span>
	 * .
	 */
	public static final String INCREMENT = "++";


	/**
	 * <span class="lang-en">The symbol of the prefix/postfix decrement operator: "--"</span>
	 * <span class="lang-ja">前置/後置デクリメント演算子の記号「 -- 」です</span>
	 * .
	 */
	public static final String DECREMENT = "--";


	/**
	 * <span class="lang-en">The symbol of the equality comparison operator: "=="</span>
	 * <span class="lang-ja">等値比較演算子の記号「 == 」です</span>
	 * .
	 */
	public static final String EQUAL = "==";


	/**
	 * <span class="lang-en">The symbol of the "non-equality" comparison operator: "&#33;="</span>
	 * <span class="lang-ja">非等値比較演算子の記号「 &#33;= 」です</span>
	 * .
	 */
	public static final String NOT_EQUAL = "!=";


	/**
	 * <span class="lang-en">The symbol of the "grater-than" comparison operator: "&gt;"</span>
	 * <span class="lang-ja">大なり比較演算子の記号「 &gt; 」です</span>
	 * .
	 */
	public static final String GRATER_THAN = ">";


	/**
	 * <span class="lang-en">The symbol of the "grater-equal" comparison operator: "&gt;="</span>
	 * <span class="lang-ja">大なり等値（以上）比較演算子の記号「 &gt;= 」です</span>
	 * .
	 */
	public static final String GRATER_EQUAL = ">=";


	/**
	 * <span class="lang-en">The symbol of the "less-than" comparison operator: "&lt;"</span>
	 * <span class="lang-ja">小なり比較演算子の記号「 &lt; 」です</span>
	 * .
	 */
	public static final String LESS_THAN = "<";


	/**
	 * <span class="lang-en">The symbol of the "less-equal" comparison operator: "&lt;="</span>
	 * <span class="lang-ja">小なり等値（以下）比較演算子の記号「 &lt;= 」です</span>
	 * .
	 */
	public static final String LESS_EQUAL = "<=";


	/**
	 * <span class="lang-en">The symbol of logical-and operator: "&amp;&amp;"</span>
	 * <span class="lang-ja">論理積演算子の記号「 &amp;&amp; 」です</span>
	 * .
	 */
	public static final String AND = "&&";


	/**
	 * <span class="lang-en">The symbol of logical-or operator: "||"</span>
	 * <span class="lang-ja">論理和演算子の記号「 || 」です</span>
	 * .
	 */
	public static final String OR = "||";


	/**
	 * <span class="lang-en">The symbol of logical-not operator: "&#33;"</span>
	 * <span class="lang-ja">論理否定演算子の記号「 &#33; 」です</span>
	 * .
	 */
	public static final String NOT = "!";


	/**
	 * <span class="lang-en">The symbol of the beginning of the parenthesis: "("</span>
	 * <span class="lang-ja">括弧の始点記号「 ( 」です</span>
	 * .
	 */
	public static final String PARENTHESIS_BEGIN = "(";


	/**
	 * <span class="lang-en">The symbol of the end of the parenthesis: ")"</span>
	 * <span class="lang-ja">括弧の終点記号「 ) 」です</span>
	 * .
	 */
	public static final String PARENTHESIS_END = ")";


	/**
	 * <span class="lang-en">The symbol of separators of arguments: ","</span>
	 * <span class="lang-ja">引数の区切り記号「 , 」です</span>
	 * .
	 */
	public static final String ARGUMENT_SEPARATOR = ",";


	/**
	 * <span class="lang-en">The symbol of the beginning of the array index: "["</span>
	 * <span class="lang-ja">配列インデックスの始点記号「 [ 」です</span>
	 * .
	 */
	public static final String SUBSCRIPT_BEGIN = "[";


	/**
	 * <span class="lang-en">The symbol of the end of the array index: "["</span>
	 * <span class="lang-ja">配列インデックスの終点記号「 [ 」です</span>
	 * .
	 */
	public static final String SUBSCRIPT_END = "]";


	/**
	 * <span class="lang-en">The symbol of the beginning of the multi-dimensional array indices: "]["</span>
	 * <span class="lang-ja">多次元配列インデックスの区切り記号「 ][ 」です</span>
	 * .
	 */
	public static final String SUBSCRIPT_SEPARATOR = "][";


	/**
	 * <span class="lang-en">The symbol of the beginning of the block: "{"</span>
	 * <span class="lang-ja">ブロックの始点記号「 { 」です</span>
	 * .
	 */
	public static final String BLOCK_BEGIN = "{";


	/**
	 * <span class="lang-en">The symbol of the beginning of the block: "}"</span>
	 * <span class="lang-ja">ブロックの終点記号「 } 」です</span>
	 * .
	 */
	public static final String BLOCK_END = "}";


	/**
	 * <span class="lang-en">The keyword of the beginning of if statements: "if"</span>
	 * <span class="lang-ja">if 文の始点キーワード「 if 」です</span>
	 * .
	 */
	public static final String IF = "if";


	/**
	 * <span class="lang-en">The keyword of the beginning of else statements: "else"</span>
	 * <span class="lang-ja">else 文の始点キーワード「 else 」です</span>
	 * .
	 */
	public static final String ELSE = "else";


	/**
	 * <span class="lang-en">The keyword of the beginning of for statements: "for"</span>
	 * <span class="lang-ja">for 文の始点キーワード「 for 」です</span>
	 * .
	 */
	public static final String FOR = "for";


	/**
	 * <span class="lang-en">The keyword of the beginning of while statements: "while"</span>
	 * <span class="lang-ja">while 文の始点キーワード「 while 」です</span>
	 * .
	 */
	public static final String WHILE = "while";


	/**
	 * <span class="lang-en">The keyword of break statements: "break"</span>
	 * <span class="lang-ja">break 文のキーワード「 break 」です</span>
	 * .
	 */
	public static final String BREAK = "break";


	/**
	 * <span class="lang-en">The keyword of continue statements: "continue"</span>
	 * <span class="lang-ja">continue 文のキーワード「 continue 」です</span>
	 * .
	 */
	public static final String CONTINUE = "continue";


	/**
	 * <span class="lang-en">The keyword of the beginning of return statements: "return"</span>
	 * <span class="lang-ja">return 文の始点キーワード「 return 」です</span>
	 * .
	 */
	public static final String RETURN = "return";


	/**
	 * <span class="lang-en">The symbol of the beginning of line comments: "//"</span>
	 * <span class="lang-ja">行コメントの始点記号「 // 」です</span>
	 * .
	 */
	public static final String LINE_COMMENT_PREFIX = "//";


	/**
	 * <span class="lang-en">The symbol of the beginning of block comments: "/&#42;"</span>
	 * <span class="lang-ja">ブロックコメントの始点記号「 /&#42; 」です</span>
	 * .
	 */
	public static final String BLOCK_COMMENT_BEGIN = "/*";


	/**
	 * <span class="lang-en">The symbol of the beginning of block comments: "&#42;/"</span>
	 * <span class="lang-ja">ブロックコメントの始点記号「 &#42;/ 」です</span>
	 * .
	 */
	public static final String BLOCK_COMMENT_END = "*/";


	// 以下のセットに投げて予約語判定するようなメソッドが居る。意味解析で識別子を検査するため。

	/**
	 * <span class="lang-en">The HashSet storing all syntax keywords</span>
	 * <span class="lang-ja">制御文の名称（キーワード）を全て格納している HashSet です</span>
	 * .
	 */
	public static final HashSet<String> STATEMENT_NAME_SET;
	static{
		STATEMENT_NAME_SET = new HashSet<String>();
		STATEMENT_NAME_SET.add(IF);
		STATEMENT_NAME_SET.add(ELSE);
		STATEMENT_NAME_SET.add(FOR);
		STATEMENT_NAME_SET.add(WHILE);
		STATEMENT_NAME_SET.add(BREAK);
		STATEMENT_NAME_SET.add(CONTINUE);
		STATEMENT_NAME_SET.add(RETURN);
	}


	/**
	 * <span class="lang-en">The HashSet storing all syntax symbols containing spaces and line-feed code</span>
	 * <span class="lang-ja">や空白・改行も含めて, 構文上の意味を持つ記号列を全て格納している HashSet です</span>
	 * .
	 */
	public static final HashSet<String> SYMBOL_SET;
	static{

    	// 現状のLexicalAnalyzerの仕様では、2文字記号系演算子は、必ず1文字目も単体で演算子としてヒットする必要がある。
    	// ただし if などのワード系シンボルは、逆に1文字目が単体でヒットしてはいけない。
    	// これらは字句解析を簡単にするための仕様であり、解決したい場合は LexicalAnalyzer の再実装が必要。
    	// 3文字シンボルを採用したい場合も同様。

    	// 現状のLexicalAnalyzerの実装のまま、もしも2文字トークンの1文字目を言語としてサポートしたくない場合は、
    	// 便宜的にその1文字のシンボルを定義した上でINVALIDを指定する事で実現可能。

    	SYMBOL_SET = new HashSet<String>();

    	SYMBOL_SET.add(ASSIGNMENT);
    	SYMBOL_SET.add(PLUS);
    	SYMBOL_SET.add(MINUS);
    	SYMBOL_SET.add(MULTIPLICATION);
    	SYMBOL_SET.add(DIVISION);
    	SYMBOL_SET.add(REMAINDER);

    	SYMBOL_SET.add(ADDITION_ASSIGNMENT);
    	SYMBOL_SET.add(SUBTRACTION_ASSIGNMENT);
    	SYMBOL_SET.add(MULTIPLICATION_ASSIGNMENT);
    	SYMBOL_SET.add(DIVISION_ASSIGNMENT);
    	SYMBOL_SET.add(REMAINDER_ASSIGNMENT);

    	SYMBOL_SET.add(INCREMENT);
    	SYMBOL_SET.add(DECREMENT);

    	SYMBOL_SET.add(GRATER_THAN);
    	SYMBOL_SET.add(GRATER_EQUAL);

    	SYMBOL_SET.add(LESS_THAN);
    	SYMBOL_SET.add(LESS_EQUAL);

    	SYMBOL_SET.add(EQUAL);
    	SYMBOL_SET.add(NOT_EQUAL);

    	SYMBOL_SET.add(AND);
    	SYMBOL_SET.add(OR);
    	SYMBOL_SET.add(NOT);

    	SYMBOL_SET.add(ARGUMENT_SEPARATOR);
    	SYMBOL_SET.add(ARBITRARY_COUNT);

    	SYMBOL_SET.add(PARENTHESIS_BEGIN);
    	SYMBOL_SET.add(PARENTHESIS_END);
    	SYMBOL_SET.add(BLOCK_BEGIN);
    	SYMBOL_SET.add(BLOCK_END);
    	SYMBOL_SET.add(SUBSCRIPT_BEGIN);
    	SYMBOL_SET.add(SUBSCRIPT_SEPARATOR);
    	SYMBOL_SET.add(SUBSCRIPT_END);

    	SYMBOL_SET.add(END_OF_STATEMENT);
    }
}
