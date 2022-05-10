/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

//Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/compiler/AttributeKey.html
//ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/compiler/AttributeKey.html

/**
 * <p>
 * <span class="lang-en">
 * The enum to define keys of attributes of the AST node ({@link AstNode})
 * and the token ({@link Token}) in the compiler of the Vnano
 * </span>
 * <span class="lang-ja">
 * Vnano のコンパイラ内において, ASTノード ({@link AstNode}) やトークン ({@link Token})
 * が保持する属性の, キーを定義するための列挙子です
 * </span>
 * .
 *
 * <p>
 * &raquo; <a href="../../../../../src/org/vcssl/nano/compiler/AttributeKey.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/compiler/AttributeKey.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/compiler/AttributeKey.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public enum AttributeKey {

	/**
	 * <span class="lang-en">The key of the attribute for storing a data type</span>
	 * <span class="lang-ja">データ型を格納する属性のキーです</span>
	 * .
	 */
	DATA_TYPE,

	/**
	 * <span class="lang-en">The key of the attribute for storing an array-rank</span>
	 * <span class="lang-ja">配列次元数を格納する属性のキーです</span>
	 * .
	 */
	RANK,

	/**
	 * <span class="lang-en">The key of the attribute for storing an (multi dimensional) array-lengths</span>
	 * <span class="lang-ja">配列要素数（多次元）を格納する属性のキーです</span>
	 * .
	 */
	LENGTHS,

	/**
	 * <span class="lang-en">The key of the attribute for storing an identifier</span>
	 * <span class="lang-ja">識別子を格納する属性のキーです</span>
	 * .
	 */
	IDENTIFIER_VALUE,

	/**
	 * <span class="lang-en">
	 * The key of the attribute for storing the serial number to distinguish
	 * different variables/functions having the same identifier
	 * </span>
	 * <span class="lang-ja">
	 * 同じ識別子を持つ, 別の変数/関数を区別するための, シリアルナンバーを格納する属性のキーです
	 * </span>
	 * .
	 */
	IDENTIFIER_SERIAL_NUMBER,

	/**
	 * <span class="lang-en">The key of the attribute for storing the content of a literal</span>
	 * <span class="lang-ja">リテラルの記述内容を格納する属性のキーです</span>
	 * .
	 */
	LITERAL_VALUE,

	/**
	 * <span class="lang-en">The key of the attribute for storing a scope type</span>
	 * <span class="lang-ja">スコープのタイプを保持する属性のキーです</span>
	 * .
	 */
	SCOPE,

	/**
	 * <span class="lang-en">The key of the attribute for storing an name space</span>
	 * <span class="lang-ja">名前空間を保持する属性のキーです</span>
	 * .
	 */
	NAME_SPACE,

	/**
	 * <span class="lang-en">The key of the attribute for storing a beginning label of a loop and so on</span>
	 * <span class="lang-ja">ループなどの始点ラベルを保持する属性のキーです</span>
	 * .
	 */
	BEGIN_LABEL,

	/**
	 * <span class="lang-en">The key of the attribute for storing a update-point label of a loop and so on</span>
	 * <span class="lang-ja">ループなどの更新地点ラベルを保持する属性のキーです</span>
	 * .
	 */
	UPDATE_LABEL,

	/**
	 * <span class="lang-en">The key of the attribute for storing an end label of a loop and so on</span>
	 * <span class="lang-ja">ループなどの終点ラベルを保持する属性のキーです</span>
	 * .
	 */
	END_LABEL, // 短絡評価用にも使用

	/**
	 * <span class="lang-en">The key of the attribute for storing a type of a leef node</span>
	 * <span class="lang-ja">リーフノードの種類を保持する属性のキーです</span>
	 * .
	 */
	LEAF_TYPE,

	/**
	 * <span class="lang-en">The key of the attribute for storing a symbol of an operator</span>
	 * <span class="lang-ja">演算子の記号を保持する属性のキーです</span>
	 * .
	 */
	OPERATOR_SYMBOL,

	/**
	 * <span class="lang-en">The key of the attribute for storing a precedence of an operator</span>
	 * <span class="lang-ja">演算子の優先度を保持する属性のキーです</span>
	 * .
	 */
	OPERATOR_PRECEDENCE,

	/**
	 * <span class="lang-en">The key of the attribute for storing an associativity of an operator</span>
	 * <span class="lang-ja">演算子の結合性を保持する属性のキーです</span>
	 * .
	 */
	OPERATOR_ASSOCIATIVITY,

	/**
	 * <span class="lang-en">
	 * The key of the attribute for storing a type of syntax
	 * (for example, binary operator, prefix operator, and so on) of an operator
	 * </span>
	 * <span class="lang-ja">
	 * 演算子の構文の種類（二項演算子や前置演算子など）を保持する属性のキーです
	 * </span>
	 * .
	 */
	OPERATOR_SYNTAX,

	/**
	 * <span class="lang-en">
	 * The key of the attribute for storing a type of operation of
	 * (for example, arithmetic operator, logical operator, and so on) of an operator
	 * </span>
	 * <span class="lang-ja">
	 * 演算子の演算の種類（算術演算子や論理演算子など）を保持する属性のキーです
	 * </span>
	 * .
	 */
	OPERATOR_EXECUTOR,

	/**
	 * <span class="lang-en">
	 * The key of the attribute for storing a data-type to perform operation of
	 * (int, float, and so on) of an operator
	 * </span>
	 * <span class="lang-ja">
	 * 演算子の演算を実行する際のデータ型（int型やfloat型など）を保持する属性のキーです
	 * </span>
	 * .
	 */
	OPERATOR_EXECUTION_DATA_TYPE,

	/**
	 * <span class="lang-en">
	 * The key of the attribute for storing a description in the intermediate code
	 * (for example, immediate value, register name, and so on)
	 * </span>
	 * <span class="lang-ja">
	 * 即値やレジスタ名など, 中間アセンブリコード内での値を保持する属性のキーです
	 * </span>
	 * .
	 */
	ASSEMBLY_VALUE,

	/**
	 * <span class="lang-en">
	 * The key of the attribute for storing the name of a register,
	 * when the AST node represents an operator and an new register is necessary
	 * to store the evaluated value of that operator
	 * </span>
	 * <span class="lang-ja">
	 * 演算子のASTノードにおいて,
	 * その演算子の評価値を控えるためにレジスタを新規生成する必要がある場合に,
	 * そのレジスタ名を保持する属性のキーです
	 * </span>
	 * .
	 */
	NEW_REGISTER,

	/**
	 * <span class="lang-en">
	 * The key of the attribute for storing a signature of a callee function
	 * </span>
	 * <span class="lang-ja">
	 * 呼び出し先関数のシグネチャを保持する属性のキーです
	 * </span>
	 * .
	 */
	CALLEE_SIGNATURE,

	/**
	 * <span class="lang-en">
	 * The key of the attribute for storing modifiers
	 * </span>
	 * <span class="lang-ja">
	 * 修飾子を保持する属性のキーです
	 * </span>
	 * .
	 * <span class="lang-en">
	 * When the AST node has multiple modifiers,
	 * they will be stored as a attribute value which delimited by AttributeValue.MODIFIER_SEPARATOR.
	 * </span>
	 * <span class="lang-ja">
	 * 修飾子が複数ある場合でも, このキーに対応する属性値は 1 つで, 修飾子はその中に
	 * {AttributeValue#MODIFIER_SEPARATOR AttributeValue.MODIFIER_SEPARATOR}
	 * で区切って格納されます.
	 * </span>
	 */
	MODIFIER,

	/**
	 * <span class="lang-en">
	 * The key of the attribute for storing a marker of the AST node of {@link AstNode.Type#STACK_LID} type
	 * which is used temporary in the parser
	 * </span>
	 * <span class="lang-ja">
	 * 構文解析の際に一時的に使用される,
	 * {@link AstNode.Type#STACK_LID} 型ASTノードのマーカー値を保持する属性のキーです.
	 * </span>
	 * .
	 */
	LID_MARKER,
}
