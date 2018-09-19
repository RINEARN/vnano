/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import java.util.HashSet;


/**
 * {@link org.vcssl.nano.VnanoEngine VnanoEngine}
 * がサポートするスクリプト言語 （無改造状態ではVnano）
 * における、キーワードや記号などが定義されたクラスです。
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class ScriptWord {

	/** 空文字や改行など、トークンの区切りとなる文字の正規表現です。 */
	public static final String TOKEN_SEPARATOR_REGEX = "( |　|\t|\n|\r|\r\n|\n\r)";

	/** 文末記号です。 */
	public static final String END_OF_STATEMENT = ";";

	/** 代入演算子（=）の記号です。 */
	public static final String ASSIGNMENT = "=";

	/** 単項プラスや加算演算子（+）の記号です。 */
	public static final String PLUS = "+";

	/** 単項マイナスや減算演算子（-）の記号です。 */
	public static final String MINUS = "-";

	/** 乗算演算子（*）の記号です。 */
	public static final String MULTIPLICATION = "*";

	/** 除算演算子（*）の記号です。 */
	public static final String DIVISION = "/";

	/** 剰余演算子（%）の記号です。 */
	public static final String REMAINDER = "%";

	/** 可算代入演算子（+=）の記号です。 */
	public static final String ADDITION_ASSIGNMENT = "+=";

	/** 減算代入演算子（-=）の記号です。 */
	public static final String SUBTRACTION_ASSIGNMENT = "-=";

	/** 乗算代入演算子（*=）の記号です。 */
	public static final String MULTIPLICATION_ASSIGNMENT = "*=";

	/** 除算代入演算子（/=）の記号です。 */
	public static final String DIVISION_ASSIGNMENT = "/=";

	/** 剰余代入演算子（%=）の記号です。 */
	public static final String REMAINDER_ASSIGNMENT = "%=";

	/** 前置および後置インクリメント演算子（++）の記号です。 */
	public static final String INCREMENT = "++";

	/** 前置および後置デクリメント演算子（--）の記号です。 */
	public static final String DECREMENT = "--";

	/** 等値演算子（=）の記号です。 */
	public static final String EQUAL = "==";

	/** 非等値演算子（!=）の記号です。 */
	public static final String NOT_EQUAL = "!=";

	/** 大なり演算子（&gt;）の記号です。 */
	public static final String GRATER_THAN = ">";

	/** 大なり等価演算子（&gt;=）の記号です。 */
	public static final String GRATER_EQUAL = ">=";

	/** 小なり演算子（&lt;）の記号です。 */
	public static final String LESS_THAN = "<";

	/** 小なり等価演算子（&lt;=）の記号です。 */
	public static final String LESS_EQUAL = "<=";

	/** ビット論理積演算子（&amp;）の記号です。 */
	public static final String BIT_AND = "&";

	/** ビット論理和演算子（|）の記号です。 */
	public static final String BIT_OR = "|";

	/** 論理積演算子（&amp;&amp;）の記号です。 */
	public static final String AND = "&&";

	/** 論理和演算子（||）の記号です。 */
	public static final String OR = "||";

	/** 論理否定演算子（!）の記号です。 */
	public static final String NOT = "!";

	/** 括弧の始点記号（ ( ）です。 */
	public static final String PARENTHESIS_BEGIN = "(";

	/** 括弧の終点記号（ ( ）です。 */
	public static final String PARENTHESIS_END = ")";

	/** 引数の区切り記号（ , ）です。 */
	public static final String ARGUMENT_SEPARATOR = ",";

	/** 配列インデックスの始点記号（ [ ）です。 */
	public static final String INDEX_BEGIN = "[";

	/** 配列インデックスの始点記号（ ] ）です。 */
	public static final String INDEX_END = "]";

	/** 配列インデックスの次元区切り記号（ ][ ）です。 */
	public static final String INDEX_SEPARATOR = "][";

	/** ブロック文の始点記号（{）です。 */
	public static final String BLOCK_BEGIN = "{";

	/** ブロック文の終点記号（}）です。 */
	public static final String BLOCK_END = "}";

	/** if文のキーワードです。 */
	public static final String IF = "if";

	/** else文のキーワードです。 */
	public static final String ELSE = "else";

	/** for文のキーワードです。 */
	public static final String FOR = "for";

	/** while文のキーワードです。 */
	public static final String WHILE = "while";

	/** break文のキーワードです。 */
	public static final String BREAK = "break";

	/** continue文のキーワードです。 */
	public static final String CONTINUE = "continue";

	/** return文のキーワードです。 */
	public static final String RETURN = "return";


	// 以下のセットに投げて予約語判定するようなメソッドが居る。意味解析で識別子を検査するため。

	/** 制御文の名称（キーワード）を全て含むハッシュセットです。*/
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


	/** 演算子や括弧、文末や空白・改行など、構文上の意味を持つ記号列を全て含むハッシュセットです。*/
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

    	SYMBOL_SET.add(BIT_AND); // 2文字シンボルの1文字目もまたシンボルでないといけないため、現状の字句解析の実装では削れない
    	SYMBOL_SET.add(AND);

    	SYMBOL_SET.add(BIT_OR); // 2文字シンボルの1文字目もまたシンボルでないといけないため、現状の字句解析の実装では削れない
    	SYMBOL_SET.add(OR);
    	SYMBOL_SET.add(NOT);

    	SYMBOL_SET.add(ARGUMENT_SEPARATOR);

    	SYMBOL_SET.add(PARENTHESIS_BEGIN);
    	SYMBOL_SET.add(PARENTHESIS_END);
    	SYMBOL_SET.add(BLOCK_BEGIN);
    	SYMBOL_SET.add(BLOCK_END);
    	SYMBOL_SET.add(INDEX_BEGIN);
    	SYMBOL_SET.add(INDEX_END);

    	SYMBOL_SET.add(END_OF_STATEMENT);

    	// これが実装上の都合でシンボルセットに含まれててしまうのは微妙
    	SYMBOL_SET.add(" ");
    	SYMBOL_SET.add("　");
    	SYMBOL_SET.add("\t");
    	SYMBOL_SET.add("\r");
    	SYMBOL_SET.add("\n");
    }
}
