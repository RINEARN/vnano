/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * コンパイラ内において、
 * トークン（字句）として扱うクラスです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class Token implements Cloneable {

	/**
	 * <p>
	 * コンパイラ内において、
	 * {@link Token Token} （トークン、字句）の種類を区別するための列挙子です。
	 * </p>
	 *
	 * @author RINEARN (Fumihiro Matsui)
	 */
	public static enum Type {

		/** データ型のトークンを表します。 */
		DATA_TYPE,

		/** 識別子やリテラルなど、ASTノードの葉要素となるトークンを表します。 */
		LEAF,

		/** 式の優先度を指定する括弧（かっこ）のトークンを表します。関数呼び出し演算子の括弧は含まれません。 */
		PARENTHESIS,

		/** ブロックの始点および終点のトークンを表します。 */
		BLOCK,

		/** 制御構文の名称（ if や for など ）のトークンを表します。 */
		CONTROL,

		/** 文末のトークンを表します。 */
		END_OF_STATEMENT,

		/** 演算子のトークンを表します。 */
		OPERATOR,

		/** 修飾子のトークンを表します。 */
		MODIFIER,
	}


	/** トークンの値を保持します。 */
	private String value;

	/** トークンのタイプ（種類）を保持します。 */
	private Type type;

	/** トークンの演算子優先度を保持します（小さい方が高優先度）。 */
	private int precedence;

	/** スクリプト内での行番号を保持します。 */
	private int lineNumber;

	/** スクリプトのファイル名を保持します。 */
	private String fileName;

	/** トークンの属性情報を保持するマップです。 */
	private Map<AttributeKey, String> attributeMap;


	/**
	 * 指定された値を持つトークンを生成します。
	 *
	 * 生成時点では、トークンの値（字句そのもの）、スクリプト内での行番号、
	 * およびスクリプトのファイル名の情報が設定されます。
	 * それ以外の情報においては、生成時点では有効な値は設定されず、
	 * {@link LexicalAnalyzer LexicalAnalyzer} による字句解析の過程で外部から設定されます。
	 *
	 * @param value トークンの値（字句）
	 * @param lineNumber スクリプト内での行番号
	 * @param fileName スクリプトのファイル名
	 */
	public Token(String value, int lineNumber, String fileName){
		this.value = value;
		this.type = null;
		this.precedence = 0;
		this.lineNumber = lineNumber;
		this.fileName = fileName;

		// toStringでダンプする際など、順序が不定だとテストやデバッグで面倒なので、HashMapではなくLinkedHashMapを使用
		this.attributeMap = new LinkedHashMap<AttributeKey, String>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Token clone() {
		Token clone = new Token(this.value, this.lineNumber, this.fileName);
		clone.type = this.type;
		clone.precedence = this.precedence;
		clone.attributeMap = (Map<AttributeKey,String>)( ((LinkedHashMap<AttributeKey,String>)this.attributeMap).clone() );
		return clone;
	}


	/**
	 * トークンの値を設定（変更）します。
	 *
	 * @param トークンの値
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * トークンの値を取得します。
	 *
	 * @return トークンの値
	 */
	public String getValue() {
		return this.value;
	}


	/**
	 * スクリプト内での行番号を取得します。
	 *
	 * @return スクリプト内での行番号
	 */
	public int getLineNumber() {
		return this.lineNumber;
	}


	/**
	 * このトークンが含まれていたスクリプトのファイル名を取得します。
	 *
	 * @return スクリプトのファイル名
	 */
	public String getFileName() {
		return this.fileName;
	}


	/**
	 * トークンの種類を区別するトークンタイプを設定します。
	 *
	 * @param type トークンタイプ
	 */
	public void setType(Type type) {
		this.type = type;
	}


	/**
	 * トークンの種類を区別するトークンタイプを取得します。
	 *
	 * @return type トークンタイプ
	 */
	public Type getType() {
		return this.type;
	}


	/**
	 * 属性情報を設定します。既に同じキーの属性が存在する場合は、上書きされます。
	 *
	 * @param attributeKey 属性キー
	 * @param attributeValue 属性値
	 */
	public void setAttribute(AttributeKey attributeKey, String attributeValue) {
		if (this.attributeMap.containsKey(attributeKey)) {
			this.attributeMap.remove(attributeKey);
		}
		this.attributeMap.put(attributeKey, attributeValue);
	}


	/**
	 * 指定された属性キーに対応する属性値を取得します。
	 *
	 * @param key 属性キー
	 * @return 属性値
	 */
	public String getAttribute(AttributeKey attributeKey) {
		return this.attributeMap.get(attributeKey);
	}


	/**
	 * 指定された属性キーに対応する属性値を保持しているかどうかを判定します。
	 *
	 * @param attributeKey 属性キー
	 * @return 保持していればtrue
	 */
	public boolean hasAttribute(AttributeKey attributeKey) {
		return this.attributeMap.containsKey(attributeKey);
	}


	/**
	 * トークンの演算子優先度を設定します。
	 *
	 * @param precedence 演算子優先度
	 */
	public void setPrecedence(int precedence) {
		this.precedence = precedence;
	}


	/**
	 * トークンの演算子優先度を取得します。
	 *
	 * @return 演算子優先度
	 */
	public int getPrecedence() {
		return this.precedence;
	}


	/**
	 * トークン配列から、指定された値を持つトークンを検索し、最初に見つかったものを返します。
	 *
	 * @param tokens トークン配列
	 * @param tokenValue 検索対象のトークンの値
	 * @param fromIndex 検索を開始する位置のインデックス
	 * @return 見つかったトークンのインデックス（見つからなかった場合は -1）
	 */
	public static int getIndexOf(Token[] tokens, String tokenValue, int fromIndex) {
		int n = tokens.length;
		for(int i=fromIndex; i<n; i++) {
			if (tokens[i].getValue().equals(tokenValue)) {
				return i;
			}
		}
		return -1;
	}


	/**
	 * トークンの文字列表現を返します（デバッグ用）。
	 *
	 * @return トークンの文字列表現
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(
				"[Token word=\"" + this.value + "\""
				+ ", lineNumber=" + this.lineNumber
				+ ", fileName=\"" + this.fileName + "\""
				+ ", type=" + this.type
				+ ", precedence=" + precedence
		);
		Set<Map.Entry<AttributeKey,String>> attibutes = this.attributeMap.entrySet();
		for (Map.Entry<AttributeKey,String> attribute: attibutes) {
			builder.append(", " + attribute.getKey() + "=\"" + attribute.getValue() + "\"");
		}
		builder.append("]");
		return builder.toString();
	}

}
