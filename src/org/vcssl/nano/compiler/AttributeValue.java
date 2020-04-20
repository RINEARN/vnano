/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

/**
 * <p>
 * コンパイラ内において、 {@link Token Token}（トークン、字句）や {@link AstNode AstNode}（ASTノード、
 * 抽象構文木ノード）が保持する属性情報の、定型的な文字列値が定数として定義されたクラスです。
 * </p>
 *
 * <p>
 * このコンパイラでは、移植の簡易さや小規模な実装を優先させるため、 サブクラスやインターフェース実装クラスの作成によって
 * トークンやASTノードに多態性を持たせる設計は採用していません。 代わりに、各トークンやASTノードが属性情報を持ち、
 * それによって役割や値などを表現する、古典的なスタイルの設計を採用しています。
 * </p>
 *
 * <p>
 * 属性の区分（属性キー）は {@link AttributeKey Attribute.Key} 列挙子で、属性値は文字列で表されます。
 * 属性には、識別子や配列次元数のように未知の値を取り得るものもありますが、 数通りの定型的な（あらかじめ決まった）値のどれかを取るようなものもあります。
 * このクラスには、そのような定型的な属性値が列挙的に定義されています。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class AttributeValue {

	// OPERATOR_EXECUTIOR

	/**
	 * {@link AttributeKey#OPERATOR_ASSOCIATIVITY OPERATOR_ASSOCIATIVITY}属性の値であり、右結合を表します。
	 */
	public static final String RIGHT = "right";

	/**
	 * {@link AttributeKey#OPERATOR_ASSOCIATIVITY OPERATOR_ASSOCIATIVITY}属性の値であり、右結合を表します。
	 */
	public static final String LEFT = "left";

	/**
	 * {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTIOR}属性の値であり、算術演算子を表します。
	 */
	public static final String ARITHMETIC = "arithmetic";

	/**
	 * {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTIOR}属性の値であり、算術複合代入演算子を表します。
	 */
	public static final String ARITHMETIC_COMPOUND_ASSIGNMENT = "arithmeticCompoundAssignment";

	/**
	 * {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTIOR}属性の値であり、論理演算子を表します。
	 */
	public static final String LOGICAL = "logical";

	/**
	 * {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTIOR}属性の値であり、比較演算子を表します。
	 */
	public static final String COMPARISON = "comparison";

	/**
	 * {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTIOR}属性の値であり、符号演算子を表します。
	 */
	public static final String SIGN = "sign";

	/**
	 * {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTIOR}属性の値であり、代入演算子を表します。
	 */
	public static final String ASSIGNMENT = "assignment";

	/**
	 * {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTIOR}属性の値であり、関数呼び出し演算子を表します。
	 */
	public static final String CALL = "call";

	/**
	 * {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTIOR}属性の値であり、配列要素アクセス演算子を表します。
	 */
	public static final String SUBSCRIPT = "subscript";

	/**
	 * {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTIOR} 属性の値であり、キャスト演算子を表します。
	 */
	public static final String CAST = "cast";

	// OPERATOR_SYNTAX

	/**
	 * {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX}属性の値であり、前置演算子を表します。
	 */
	public static final String PREFIX = "prefix";

	/**
	 * {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX}属性の値であり、後置演算子を表します。
	 */
	public static final String POSTFIX = "postfix";

	/**
	 * {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX}属性の値であり、二項演算子を表します。
	 */
	public static final String BINARY = "binary";

	/**
	 * {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX}属性の値であり、
	 * 多項演算子やその始点トークンを表します。
	 */
	public static final String MULTIARY = "multiary";

	/**
	 * {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX}属性の値であり、多項演算子の区切りトークンを表します。
	 */
	public static final String MULTIARY_SEPARATOR = "multialySeparator";

	/**
	 * {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX}属性の値であり、多項演算子の終点トークンを表します。
	 */
	public static final String MULTIARY_END = "multialyEnd";

	// LEAF_TYPE

	/** {@link AttributeKey#LEAF_TYPE LEAF_TYPE}属性の値であり、変数識別子を表します。 */
	public static final String VARIABLE_IDENTIFIER = "variableIdentifier";

	/** {@link AttributeKey#LEAF_TYPE LEAF_TYPE}属性の値であり、関数識別子を表します。 */
	public static final String FUNCTION_IDENTIFIER = "functionIdentifier";

	/** {@link AttributeKey#LEAF_TYPE LEAF_TYPE}属性の値であり、リテラルを表します。 */
	public static final String LITERAL = "literal";

	// SCOPE

	//    以下のGLOBAL/LOCALの説明文、
	//    スクリプト内で宣言されたものがローカル、スクリプト外由来のものがグローバルとされる事の補足が居るかも
	//    名前も含めて要検討(external/internalにする可能性も含めて)

	/** {@link AttributeKey#SCOPE SCOPE}属性の値であり、グローバルスコープを表します。 */
	public static final String GLOBAL = "global";

	/** {@link AttributeKey#SCOPE SCOPE}属性の値であり、ローカルスコープを表します。 */
	public static final String LOCAL = "local";

	/**
	 * {@link AttributeKey#MODIFIER MODIFIER}属性の値の中に、複数の修飾子を格納する際の区切り文字です。
	 * 個々の修飾子が、値として含み得ない文字である必要があります。
	 */
	public static final String MODIFIER_SEPARATOR = ",";

	// LID_MARKER

	/** {@link AttributeKey#LID_MARKER LID_MARKER}属性の値であり、部分式のマーカーを表します。 */
	public static final String PARTIAL_EXPRESSION = "partialExpression";

	/** {@link AttributeKey#LID_MARKER LID_MARKER}属性の値であり、ブロック文のマーカーを表します。 */
	public static final String BLOCK_STATEMENT = "blockStatement";
}
