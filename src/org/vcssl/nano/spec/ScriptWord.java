/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import java.util.HashSet;
import java.util.Set;

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


	// 各フィールドは元々は static final でしたが、カスタマイズの事を考慮して、動的なフィールドに変更されました。
	// これにより、このクラスのインスタンスを生成して値を変更し、
	// それを LanguageSpecContainer に持たせて VnanoEngle クラスのコンストラクタに渡す事で、
	// 処理系内のソースコードを保ったまま（再ビルド不要で）定義類を差し替える事ができます。


	/**
	 * <span class="lang-en">The name of the script language: "Vnano"</span>
	 * <span class="lang-ja">スクリプト言語の名称（ Vnano ）です</span>
	 * .
	 */
	public String scriptLanguageName = "Vnano";


	/**
	 * <span class="lang-en">The version of the script language: "Vnano"</span>
	 * <span class="lang-ja">スクリプト言語のバージョンです</span>
	 * .
	 */
	public String scriptLanguageVersion = EngineInformation.ENGINE_VERSION;


	/**
	 * <span class="lang-en">The regular expression of separators of tokens (spaces, line feed code, and so on)</span>
	 * <span class="lang-ja">空白や改行など、トークンの区切りとなる文字の正規表現です</span>
	 * .
	 */
	public String tokenSeparatorRegex = "( |　|\t|\n|\r|\r\n|\n\r)";


	/**
	 * <span class="lang-en">The separator of name spaces</span>
	 * <span class="lang-ja">名前空間の区切りです</span>
	 * .
	 */
	public String nameSpaceSeparator = ".";


	/**
	 * <span class="lang-en">The symbol of the end of statements: ";"</span>
	 * <span class="lang-ja">文末記号「 ; 」です</span>
	 * .
	 */
	public String endOfStatement = ";";


	/**
	 * <span class="lang-en">The symbol of the assignment operator: "="</span>
	 * <span class="lang-ja">代入演算子の記号「 = 」です</span>
	 * .
	 */
	public String assignment = "=";


	/**
	 * <span class="lang-en">The symbol of the unary plus operator and the addition operator: "+"</span>
	 * <span class="lang-ja">単項プラス演算子および加算演算子の記号「 + 」です</span>
	 * .
	 */
	public String plusOrAddition = "+";


	/**
	 * <span class="lang-en">The symbol of the unary minus operator and the subtraction operator: "-"</span>
	 * <span class="lang-ja">単項マイナス演算子および加算演算子の記号「 - 」です</span>
	 * .
	 */
	public String minusOrSubtraction = "-";


	/**
	 * <span class="lang-en">The symbol of the multiplication operator: "*"</span>
	 * <span class="lang-ja">乗算演算子の記号「 * 」です</span>
	 * .
	 */
	public String multiplication = "*";


	/**
	 * <span class="lang-en">The symbol of the division operator: "/"</span>
	 * <span class="lang-ja">除算演算子の記号「 / 」です</span>
	 * .
	 */
	public String division = "/";


	/**
	 * <span class="lang-en">The symbol of the remainder operator: "%"</span>
	 * <span class="lang-ja">剰余演算子の記号「 % 」です</span>
	 * .
	 */
	public String remainder = "%";


	/**
	 * <span class="lang-en">The symbol of the compound assignment operator of the addition: "+="</span>
	 * <span class="lang-ja">可算との複合代入演算子の記号「 += 」です</span>
	 * .
	 */
	public String additionAssignment = "+=";


	/**
	 * <span class="lang-en">The symbol of the compound assignment operator of the subtraction: "-="</span>
	 * <span class="lang-ja">減算との複合代入演算子の記号「 -= 」です</span>
	 * .
	 */
	public String subtractionAssignment = "-=";


	/**
	 * <span class="lang-en">The symbol of the compound assignment operator of the multiplication: "*="</span>
	 * <span class="lang-ja">乗算との複合代入演算子の記号「 *= 」です</span>
	 * .
	 */
	public String multiplicationAssignment = "*=";


	/**
	 * <span class="lang-en">The symbol of the compound assignment operator of the division: "/="</span>
	 * <span class="lang-ja">除算との複合代入演算子の記号「 /= 」です</span>
	 * .
	 */
	public String divisionAssignment = "/=";


	/**
	 * <span class="lang-en">The symbol of the compound assignment operator of the remainder: "%="</span>
	 * <span class="lang-ja">剰余演算との複合代入演算子の記号「 %= 」です</span>
	 * .
	 */
	public String remainderAssignment = "%=";


	/**
	 * <span class="lang-en">The symbol of the prefix/postfix increment operator: "++"</span>
	 * <span class="lang-ja">前置/後置インクリメント演算子の記号「 ++ 」です</span>
	 * .
	 */
	public String increment = "++";


	/**
	 * <span class="lang-en">The symbol of the prefix/postfix decrement operator: "--"</span>
	 * <span class="lang-ja">前置/後置デクリメント演算子の記号「 -- 」です</span>
	 * .
	 */
	public String decrement = "--";


	/**
	 * <span class="lang-en">The symbol of the equality comparison operator: "=="</span>
	 * <span class="lang-ja">等値比較演算子の記号「 == 」です</span>
	 * .
	 */
	public String equal = "==";


	/**
	 * <span class="lang-en">The symbol of the "non-equality" comparison operator: "&#33;="</span>
	 * <span class="lang-ja">非等値比較演算子の記号「 &#33;= 」です</span>
	 * .
	 */
	public String notEqual = "!=";


	/**
	 * <span class="lang-en">The symbol of the "greater-than" comparison operator: "&gt;"</span>
	 * <span class="lang-ja">大なり比較演算子の記号「 &gt; 」です</span>
	 * .
	 */
	public String greaterThan = ">";


	/**
	 * <span class="lang-en">The symbol of the "greater-equal" comparison operator: "&gt;="</span>
	 * <span class="lang-ja">大なり等値（以上）比較演算子の記号「 &gt;= 」です</span>
	 * .
	 */
	public String greaterEqual = ">=";


	/**
	 * <span class="lang-en">The symbol of the "less-than" comparison operator: "&lt;"</span>
	 * <span class="lang-ja">小なり比較演算子の記号「 &lt; 」です</span>
	 * .
	 */
	public String lessThan = "<";


	/**
	 * <span class="lang-en">The symbol of the "less-equal" comparison operator: "&lt;="</span>
	 * <span class="lang-ja">小なり等値（以下）比較演算子の記号「 &lt;= 」です</span>
	 * .
	 */
	public String lessEqual = "<=";


	/**
	 * <span class="lang-en">The symbol of logical-and operator with short-circuit evaluation: "&amp;&amp;"</span>
	 * <span class="lang-ja">短絡評価を行う論理積演算子の記号「 &amp;&amp; 」です</span>
	 * .
	 */
	public String shortCircuitAnd = "&&";


	/**
	 * <span class="lang-en">The symbol of logical-or operator with short-curcuit evaluation: "||"</span>
	 * <span class="lang-ja">短絡評価を行う論理和演算子の記号「 || 」です</span>
	 * .
	 */
	public String shortCircuitOr = "||";


	/**
	 * <span class="lang-en">The symbol of logical-not operator: "&#33;"</span>
	 * <span class="lang-ja">論理否定演算子の記号「 &#33; 」です</span>
	 * .
	 */
	public String not = "!";


	/**
	 * <span class="lang-en">The symbol of the beginning of the parenthesis: "("</span>
	 * <span class="lang-ja">括弧の始点記号「 ( 」です</span>
	 * .
	 */
	public String parenthesisBegin = "(";


	/**
	 * <span class="lang-en">The symbol of the end of the parenthesis: ")"</span>
	 * <span class="lang-ja">括弧の終点記号「 ) 」です</span>
	 * .
	 */
	public String paranthesisEnd = ")";


	/**
	 * <span class="lang-en">The symbol of separators of arguments: ","</span>
	 * <span class="lang-ja">引数の区切り記号「 , 」です</span>
	 * .
	 */
	public String argumentSeparator = ",";


	/**
	 * <span class="lang-en">The symbol of the beginning of the array index: "["</span>
	 * <span class="lang-ja">配列インデックスの始点記号「 [ 」です</span>
	 * .
	 */
	public String subscriptBegin = "[";


	/**
	 * <span class="lang-en">The symbol of the end of the array index: "["</span>
	 * <span class="lang-ja">配列インデックスの終点記号「 [ 」です</span>
	 * .
	 */
	public String subscriptEnd = "]";


	/**
	 * <span class="lang-en">The symbol of the beginning of the multi-dimensional array indices: "]["</span>
	 * <span class="lang-ja">多次元配列インデックスの区切り記号「 ][ 」です</span>
	 * .
	 */
	public String subscriptSeparator = "][";


	/**
	 * <span class="lang-en">The symbol of the beginning of the block: "{"</span>
	 * <span class="lang-ja">ブロックの始点記号「 { 」です</span>
	 * .
	 */
	public String blockBegin = "{";


	/**
	 * <span class="lang-en">The symbol of the beginning of the block: "}"</span>
	 * <span class="lang-ja">ブロックの終点記号「 } 」です</span>
	 * .
	 */
	public String blockEnd = "}";


	/**
	 * <span class="lang-en">The keyword of the beginning of if statements: "if"</span>
	 * <span class="lang-ja">if 文の始点キーワード「 if 」です</span>
	 * .
	 */
	public String ifStatement = "if"; // 変数名、if だけとかは予約語なので無理


	/**
	 * <span class="lang-en">The keyword of the beginning of else statements: "else"</span>
	 * <span class="lang-ja">else 文の始点キーワード「 else 」です</span>
	 * .
	 */
	public String elseStatement = "else";


	/**
	 * <span class="lang-en">The keyword of the beginning of for statements: "for"</span>
	 * <span class="lang-ja">for 文の始点キーワード「 for 」です</span>
	 * .
	 */
	public String forStatement = "for";


	/**
	 * <span class="lang-en">The keyword of the beginning of while statements: "while"</span>
	 * <span class="lang-ja">while 文の始点キーワード「 while 」です</span>
	 * .
	 */
	public String whileStatement = "while";


	/**
	 * <span class="lang-en">The keyword of break statements: "break"</span>
	 * <span class="lang-ja">break 文のキーワード「 break 」です</span>
	 * .
	 */
	public String breakStatement = "break";


	/**
	 * <span class="lang-en">The keyword of continue statements: "continue"</span>
	 * <span class="lang-ja">continue 文のキーワード「 continue 」です</span>
	 * .
	 */
	public String continueStatement = "continue";


	/**
	 * <span class="lang-en">The keyword of the beginning of return statements: "return"</span>
	 * <span class="lang-ja">return 文の始点キーワード「 return 」です</span>
	 * .
	 */
	public String returnStatement = "return";


	/**
	 * <span class="lang-en">The symbol represents that the number of somethings is arbitrary: "..."</span>
	 * <span class="lang-ja">任意の個数を表す記号「 ... 」です。</span>
	 * .
	 */
	public String arbitraryCountModifier = "...";


	/**
	 * <span class="lang-en">The symbol representing the reference: "&amp;"</span>
	 * <span class="lang-ja">参照を表す記号「 &amp; 」です</span>
	 * .
	 * <span class="lang-en">
	 * In the current version,
	 * this symbol is used only for a kind of modifier representing call-by-reference,
	 * and it has not supported as an operator.
	 * </span>
	 * <span class="lang-ja">
	 * 現状では, この記号は関数の参照渡しを表す修飾子の一種としてのみ使用され,
	 * 演算子としてはサポートされていません
	 * </span>
	 */
	public String refModifier = "&";


	/**
	 * <span class="lang-en">The modifier representing being constModifier: "const"</span>
	 * <span class="lang-ja">定数である事を表す修飾子「 const 」です</span>
	 * .
	 */
	public String constModifier = "const";


	/**
	 * <span class="lang-en">The symbol of the beginning of line comments: "//"</span>
	 * <span class="lang-ja">行コメントの始点記号「 // 」です</span>
	 * .
	 */
	public String lineCommentPrefix = "//";


	/**
	 * <span class="lang-en">The symbol of the beginning of block comments: "/&#42;"</span>
	 * <span class="lang-ja">ブロックコメントの始点記号「 /&#42; 」です</span>
	 * .
	 */
	public String blockCommentBegin = "/*";


	/**
	 * <span class="lang-en">The symbol of the beginning of block comments: "&#42;/"</span>
	 * <span class="lang-ja">ブロックコメントの始点記号「 &#42;/ 」です</span>
	 * .
	 */
	public String blockCommentEnd = "*/";


	// LexicalAnalyzer での制御文トークンの判定に使用
	/**
	 * <span class="lang-en">The HashSet storing all syntax keywords</span>
	 * <span class="lang-ja">制御文の名称（キーワード）を全て格納している HashSet です</span>
	 * .
	 */
	@SuppressWarnings("serial")
	public Set<String> statementNameSet = new HashSet<String>() {{
		add(ifStatement);
		add(elseStatement);
		add(forStatement);
		add(whileStatement);
		add(breakStatement);
		add(continueStatement);
		add(returnStatement);
	}};


	/**
	 * <span class="lang-en">The HashSet storing all syntax symbols</span>
	 * <span class="lang-ja">構文上の意味を持つ記号列を全て格納している HashSet です</span>
	 * .
	 */
	@SuppressWarnings("serial")
	public Set<String> symbolSet = new HashSet<String>() {{

    	// 現状のLexicalAnalyzerの仕様では、2文字記号系演算子は、必ず1文字目も単体で演算子としてヒットする必要がある。
    	// ただし if などのワード系シンボルは、逆に1文字目が単体でヒットしてはいけない。
    	// これらは字句解析を簡単にするための仕様であり、解決したい場合は LexicalAnalyzer の再実装が必要。
    	// 3文字シンボルを採用したい場合も同様。

    	// 現状のLexicalAnalyzerの実装のまま、もしも2文字トークンの1文字目を言語としてサポートしたくない場合は、
    	// 便宜的にその1文字のシンボルを定義した上でINVALIDを指定する事で実現可能。

    	add(assignment);
    	add(plusOrAddition);
    	add(minusOrSubtraction);
    	add(multiplication);
    	add(division);
    	add(remainder);

    	add(additionAssignment);
    	add(subtractionAssignment);
    	add(multiplicationAssignment);
    	add(divisionAssignment);
    	add(remainderAssignment);

    	add(increment);
    	add(decrement);

    	add(greaterThan);
    	add(greaterEqual);

    	add(lessThan);
    	add(lessEqual);

    	add(equal);
    	add(notEqual);

    	add(shortCircuitAnd);
    	add(shortCircuitOr);
    	add(not);

    	add(argumentSeparator);
    	add(arbitraryCountModifier);
    	add(refModifier);

    	add(parenthesisBegin);
    	add(paranthesisEnd);
    	add(blockBegin);
    	add(blockEnd);
    	add(subscriptBegin);
    	add(subscriptSeparator);
    	add(subscriptEnd);

    	add(endOfStatement);
    }};


	@SuppressWarnings("serial")
	public Set<String> modifierSet = new HashSet<String>() {{
		add(constModifier);
		add(arbitraryCountModifier);
		add(refModifier);
	}};


	/**
	 * <span class="lang-en">The HashSet storing modifiers which will be put before the type name</span>
	 * <span class="lang-ja">型名の前に置かれる修飾子を格納している HashSet です</span>
	 * .
	 */
	@SuppressWarnings("serial")
	public Set<String> prefixModifierSet = new HashSet<String>() {{
		add(constModifier);
		add(arbitraryCountModifier);
	}};


	/**
	 * <span class="lang-en">The HashSet storing modifiers which will be put after the type name</span>
	 * <span class="lang-ja">型名の後に置かれる修飾子を格納している HashSet です</span>
	 * .
	 */
	@SuppressWarnings("serial")
	public Set<String> postfixModifierSet = new HashSet<String>() {{
		add(refModifier);
	}};


	// SemanticAnalyzer での識別子検査に使用
	/**
	 * <span class="lang-en">The HashSet storing reserved words</span>
	 * <span class="lang-ja">予約語を格納している HashSet です</span>
	 */
	@SuppressWarnings("serial")
	public Set<String> reservedWordSet = new HashSet<String>() {{

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

		add(ifStatement);
		add(elseStatement);
		add(forStatement);
		add(whileStatement);
		add(breakStatement);
		add(continueStatement);
		add(returnStatement);
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
