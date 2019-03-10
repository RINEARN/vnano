/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

/**
 * <p>
 * コンパイラ内において、
 * {@link Token Token}（トークン、字句）や {@link AstNode AstNode}（ASTノード、
 * 抽象構文木ノード）が保持する属性情報の、キーとなる列挙子です。
 * </p>
 *
 * <p>
 * このコンパイラでは、移植の簡易さや小規模な実装を優先させるため、
 * サブクラスやインターフェース実装クラスの作成によって
 * トークンやASTノードに多態性を持たせる設計は採用していません。
 * 代わりに、各トークンやASTノードが属性情報を持ち、
 * それによって役割や値などを表現する、古典的なスタイルの設計を採用しています。
 * </p>
 *
 * <p>
 * 属性の区分（属性キー）はこの列挙子で、属性値は文字列で表されます。
 * 定型的な属性値は {@link AttributeValue Attribute.Value} クラス内に文字列定数として定義されています。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public enum AttributeKey {

	/** データ型を保持する属性のキーです。 */
	DATA_TYPE,

	/** 配列の次元数を保持する属性のキーです。 */
	RANK,

	/** 配列の要素数を保持する属性のキーです。 */
	LWNGRHS,

	/** 識別子を保持する属性のキーです。 */
	IDENTIFIER_VALUE,

	/** 複数の同じ識別子を区別するためのシリアルナンバー属性のキーです。 */
	IDENTIFIER_SERIAL_NUMBER,

	/** リテラルの記載内容を保持する属性のキーです。 */
	LITERAL_VALUE,

	/** スコープ情報を保持する属性のキーです。 */
	SCOPE,

	/** 始点ラベルを保持する属性のキーです。 */
	BEGIN_LABEL,

	/** 更新地点ラベルを保持する属性のキーです。 */
	UPDATE_LABEL,

	/** 終点ラベルを保持する属性のキーです。 */
	END_LABEL, // 短絡評価用にも使用

	/** リーフノードの種類を区別する属性のキーです。 */
	LEAF_TYPE,

	/** 演算子ノードにおいて、演算子の記号を保持する属性のキーです。 */
	OPERATOR_SYMBOL,

	/** 演算子ノードにおいて、演算子の優先度を保持する属性のキーです。 */
	OPERATOR_PRIORITY,

	/** 演算子ノードにおいて、結合性（右結合か左結合か）を区別する属性のキーです。 */
	OPERATOR_ASSOCIATIVITY,

	/** 演算子ノードにおいて、演算子の構文の種類（二項演算子など）を区別する属性のキーです。 */
	OPERATOR_SYNTAX,

	/** 演算子ノードにおいて、演算子の演算の種類（算術演算子など）を区別する属性のキーです。 */
	OPERATOR_EXECUTOR,

	/** 演算子ノードにおいて、演算実行時の（入力値の）データ型を保持する属性のキーです。 */
	OPERATOR_EXECUTION_DATA_TYPE,

	/** レジスタや即値など、中間アセンブリコード内での値を保持する属性のキーです。 */
	ASSEMBLY_VALUE,

	/** 構文解析の際に使用される {@link AstNode.Type#STACK_LID STACK_LID} 型ノードのマーカー値を保持する属性のキーです。 */
	LID_MARKER,
}
