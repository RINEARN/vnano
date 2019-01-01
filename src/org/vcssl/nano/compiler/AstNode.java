/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * <p>
 * コンパイラ内において、AST（抽象構文木）の構成ノードとなるクラスです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class AstNode implements Cloneable {


	/**
	 * Vnano処理系のコンパイラ内において、
	 * {@link AstNode AstNode}（抽象構文木ノード）の種類を区別するための列挙子です。
	 *
	 * @author RINEARN (Fumihiro Matsui)
	 */
	public enum Type {

		/** ASTの頂点（根）に唯一位置する、ルートノードを表します。 */
		ROOT,

		/** 空文（内容が全く無い文）のノードを表します */
		EMPTY,

		/** 変数宣言文のノードを表します。 */
		VARIABLE,

		/** 関数宣言文のノードを表します。 */
		FUNCTION,

		/** 式文のノードを表します。 */
		EXPRESSION,

		/** ブロック文または関数ブロックのノードを表します。 */
		BLOCK,

		/** if 文のノードを表します。 */
		IF,

		/** for 文のノードを表します。 */
		FOR,

		/** while 文のノードを表します。 */
		WHILE,

		/** else 文のノードを表します。 */
		ELSE,

		/** break 文のノードを表します。 */
		BREAK,

		/** continue 文のノードを表します。 */
		CONTINUE,

		/** return 文のノードを表します。 */
		RETURN,

		/** 式の構文木における末端（根）ノードを表します。 */
		LEAF,

		/** 式の構文木における演算子ノードを表します。 */
		OPERATOR,

		/** 変数宣言文における、配列要素数情報のノードを表します。 */
		LENGTHS,

		/** 式の中での括弧を表しますが、式の構文解析の最中でのみ使用され、最終的なASTの構成要素には含まれません。 */
		PARENTHESIS,

		/** 式の構文解析の最中において、スタックの領域を区切るために使用され、最終的なASTの構成要素には含まれません。 */
		STACK_LID,
	}


	/** このオブジェクトの文字列表現に使用される、デフォルトのインデント文字列です。 */
	private static final String DEFAULT_INDENT = "  ";

	/** ASTノードのタイプ（種類）を保持します。 */
	private Type type;

	/** ASTノードの親ノードを保持します。 */
	private AstNode parentNode;

	/** ASTノードの子ノードを保持するリストです。 */
	private List<AstNode> childNodeList;

	/** このASTノードが、親ノードから見て、何番目の子ノードかを表すインデックスを保持します。 */
	private int siblingIndex = 0;

	/** スクリプト内での行番号を保持します。 */
	private int lineNumber = -1;

	/** スクリプトのファイル名を保持します。 */
	private String fileName = null;

	/** ASTノードの属性情報を保持するマップです。 */
	private Map<AttributeKey, String> attributeMap = null;


	/**
	 * 指定されたタイプのASTノードを生成します。
	 *
	 * @param value ノードタイプ
	 * @param lineNumber スクリプト内での行番号
	 * @param fileName スクリプトのファイル名
	 */
	public AstNode(Type type, int lineNumber, String fileName) {
		this.type = type;
		this.childNodeList = new ArrayList<AstNode>();
		this.lineNumber = lineNumber;
		this.fileName = fileName;

		// toStringでダンプする際など、順序が不定だとテストやデバッグで面倒なので、HashMapではなくLinkedHashMapを使用
		this.attributeMap = new LinkedHashMap<AttributeKey, String>();
	}


	/**
	 * ディープコピーによる複製を生成して返します。
	 *
	 * @return このノードの複製
	 */
	@Override
	public AstNode clone() {
		AstNode cloneNode = new AstNode(this.type, this.lineNumber, this.fileName);
		for (final AstNode childNode: this.childNodeList) {
			AstNode cloneChildNode = childNode.clone();
			cloneNode.addChildNode(cloneChildNode);
		}
		cloneNode.attributeMap = new LinkedHashMap<AttributeKey, String>(this.attributeMap); // コピー
		return cloneNode;
	}


	/**
	 * ASTノードの種類を区別するノードタイプを取得します。
	 *
	 * @return このノードのノードタイプ
	 */
	public Type getType() {
		return this.type;
	}


	/**
	 * このノードに変換されたコードが記載されていた、スクリプト内での行番号を取得します。
	 *
	 * @return スクリプト内での行番号
	 */
	public int getLineNumber() {
		return this.lineNumber;
	}


	/**
	 * このノードに変換されたコードが記載されていた、スクリプトのファイル名を取得します。
	 *
	 * @return スクリプトのファイル名
	 */
	public String getFileName() {
		return this.fileName;
	}


	/**
	 * 属性情報を追加します。
	 *
	 * @param attributeKey 属性キー
	 * @param attributeValue 属性値
	 */
	public void addAttribute(AttributeKey attributeKey, String attributeValue) {
		this.attributeMap.put(attributeKey, attributeValue);
	}


	/**
	 * 指定された属性キーに対応する属性値を取得します。
	 *
	 * @param attributeKey 属性キー
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
	 * 子ノードを追加します。
	 *
	 * @param node 子ノード
	 */
	public void addChildNode(AstNode node) {
		node.parentNode = this;
		node.siblingIndex = this.childNodeList.size();
		this.childNodeList.add(node);
	}


	/**
	 * 子ノードを複数追加します。
	 *
	 * @param nodes 子ノードを格納する配列
	 */
	public void addChildNodes(AstNode[] nodes) {
		for (AstNode node : nodes) {
			this.addChildNode(node);
		}
	}


	/**
	 * 子ノードを全て取得します。
	 *
	 * @return 子ノードを格納する配列
	 */
	public AstNode[] getChildNodes() {
		return (AstNode[])this.childNodeList.toArray(new AstNode[this.childNodeList.size()]);
	}


	/**
	 * 指定されたノードタイプの子ノードを全て取得します。
	 *
	 * @param type 取得したい子ノードのノードタイプ
	 * @return 子ノードを格納する配列
	 */
	public AstNode[] getChildNodes(Type type) {
		LinkedList<AstNode> resultList = new LinkedList<AstNode>();
		AstNode[] allChildNodes = this.getChildNodes();
		for (AstNode child : allChildNodes) {
			if (child.getType() == type) {
				resultList.add(child);
			}
		}
		AstNode[] results = resultList.toArray(new AstNode[0]);
		return results;
	}


	/**
	 * 子ノードが存在するか判定します。
	 *
	 * @return 判定結果（存在する場合に true）
	 */
	public boolean hasChildNodes() {
		return this.childNodeList.size() != 0;
	}


	/**
	 * 指定されたノードタイプの子ノードが存在するか判定します。
	 *
	 * @return 判定結果（存在する場合に true）
	 */
	public boolean hasChildNodes(AstNode.Type type) {
		return this.getChildNodes(type).length != 0;
	}


	/**
	 * 親ノードを取得します。
	 *
	 * なお、親ノードの設定は {@link AstNode.addChildNode addChildNode} メソッド内で自動的に行われ、
	 * 追加する子ノードに対して自身が親となるように設定されます。
	 * 木構造の破壊を防ぐため、setter はありません。
	 *
	 * @return 親ノード
	 */
	public AstNode getParentNode() {
		return this.parentNode;
	}


	/**
	 * 親ノードが存在するか判定します。
	 *
	 * @return 判定結果（存在する場合に true）
	 */
	public boolean hasParentNode() {
		return this.parentNode != null;
	}


	/**
	 * このASTノードが、親ノードから見て、何番目の子ノードかを表すインデックスを取得します。
	 *
	 * なお、インデックスの設定は {@link AstNode.addChildNode addChildNode}
	 * メソッド内で自動的に行われます。
	 *
	 * @return 親ノードから見た、このノードのインデックス
	 */
	public int getSiblingIndex() {
		return this.siblingIndex;
	}


	/**
	 * このASTノードの、構文木内での階層深度（根が深度0）を取得します。
	 *
	 * @return 構文木内での階層深度（根が深度0）
	 */
	public int getDepth() {
		if (this.hasParentNode()) {
			return this.getParentNode().getDepth() + 1;
		} else {
			return 0;
		}
	}


	/**
	 * このASTノードの、構文木内でのブロックの階層深度（根が深度0）を取得します。
	 *
	 * @return 構文木内でのブロックの階層深度（根が深度0）
	 */
	public int getBlockDepth() {
		if (this.hasParentNode()) {
			if (this.type == AstNode.Type.BLOCK) {
				return this.getParentNode().getBlockDepth() + 1;
			} else {
				return this.getParentNode().getBlockDepth();
			}
		} else {
			return 0;
		}
	}


	/**
	 * {@link AttributeKey#DATA_TYPE DATA_TYPE} 属性の値を参照する事により、
	 * このASTノードにおけるデータ型の名称を取得します。
	 *
	 * @return データ型の名称
	 */
	public String getDataTypeName() {
		return this.attributeMap.get(AttributeKey.DATA_TYPE);
	}


	/**
	 * {@link AttributeKey#RANK RANK} 属性の値を参照する事により、
	 * このASTノードにおける配列の次元数を取得します。
	 *
	 * @return 配列の次元数
	 */
	public int getRank() {
		String rankWord = this.attributeMap.get(AttributeKey.RANK);
		return Integer.parseInt(rankWord);
	}


	/**
	 * AST内のノードを、行がけ順で深さ優先走査する場合における、
	 * このノードの次のノードを取得します。
	 *
	 * @return 次のノード
	 */
	public AstNode getPreorderTraversalNextNode() {

		// 子ノードがある場合は子ノードに移動
		if (this.hasChildNodes()) {
			return this.getChildNodes()[0];
		}

		// 親階層の兄弟ノードがあれば移動
		AstNode curretnNode = this;
		AstNode parent = curretnNode.getParentNode();
		AstNode[] brothers = parent.getChildNodes();
		while (true) {
			if (curretnNode.getSiblingIndex() < brothers.length-1) {
				return brothers[curretnNode.getSiblingIndex() + 1];
			}
			if (!parent.hasParentNode()) {
				return null;
			}
			curretnNode = parent;
			parent = curretnNode.getParentNode();
			brothers = parent.getChildNodes();
		}
	}


	/**
	 * AST内のノードを、行がけ順で深さ優先走査する場合における、
	 * このノードが最後のノードかどうかを判定します。
	 *
	 * @return 判定結果（このノードが最後なら true ）
	 */
	public boolean isPreorderTraversalLastNode() {
		return this.getPreorderTraversalNextNode() == null;
	}


	/**
	 * AST内のノードを、帰りがけ順で深さ優先走査する場合における、
	 * 最初のノードを取得します。
	 *
	 * @return 最初のノード
	 */
	public AstNode getPostorderTraversalFirstNode() {
		AstNode currentNode = this;
		while (currentNode.hasChildNodes()) {
			currentNode = currentNode.getChildNodes()[0];
		}
		return currentNode;
	}


	/**
	 * AST内のノードを、帰りがけ順で深さ優先走査する場合における、
	 * このノードの次のノードを取得します。
	 *
	 * @return 次のノード
	 */
	public AstNode getPostorderTraversalNextNode() {

		// 親階層が無ければルートノードなので終了
		// ( 本来は呼び出し側で isBottomUpLastNode で確認しておくのが好ましい )
		if (!this.hasParentNode()) {
			return null;
		}

		// 親と兄弟を取得
		AstNode parent = this.getParentNode();
		AstNode[] brothers = parent.getChildNodes();

		// 自分が兄弟の一番右端なら、親階層に上がる
		if (brothers.length-1 == this.getSiblingIndex()) {
			return parent;

		// 右にまだ兄弟が居る場合は、右隣の兄弟の末端まで降りる
		} else {
			return brothers[this.getSiblingIndex() + 1].getPostorderTraversalFirstNode();
		}
	}


	/**
	 * AST内のノードを、帰りがけ順で深さ優先走査する場合における、
	 * このノードが最後のノードかどうかを判定します。
	 *
	 * @return 判定結果（このノードが最後なら true ）
	 */
	public boolean isPostorderTraversalLastNode() {
		return !this.hasParentNode();
	}


	/**
	 * このASTノードの文字列表現を返します。
	 *
	 * 文字列表現の書式は、XMLライクな入れ子構造のタグによって、
	 * 構文木のツリー構造が記述されたものとなります。
	 *
	 * 内容に子ノードを含めるかどうかや、インデントの幅を調整したい場合には、
	 * {@link AstNode#toString(boolean,String) toString(boolean,String)}
	 * メソッドの方を使用してください。
	 *
	 * @return ASTノードの文字列表現
	 */
	@Override
	public String toString() {
		return this.toString(true, AstNode.DEFAULT_INDENT);
	}


	/**
	 * このASTノードの文字列表現を返します。
	 *
	 * 文字列表現の書式は、XMLライクな入れ子構造のタグによって、
	 * 構文木のツリー構造が記述されたものとなります。
	 *
	 * @param containsChildNodes 内容に子ノードを含めるかどうか（含める場合に true を指定）
	 * @param indentString インデントに使用する文字列
	 * @return ASTノードの文字列表現
	 */
	public String toString(boolean containsChildNodes, String indentString) {
		String eol = System.getProperty("line.separator");
		AstNode[] nodes = (AstNode[])childNodeList.toArray(new AstNode[childNodeList.size()]);
		StringBuilder sb = new StringBuilder();

		sb.append('<');
		sb.append(this.type);
		Set<Map.Entry<AttributeKey,String>> attibutes = this.attributeMap.entrySet();
		for (Map.Entry<AttributeKey,String> attribute: attibutes) {
			sb.append(" ");
			sb.append(attribute.getKey());
			sb.append("=\"");
			sb.append(attribute.getValue());
			sb.append("\"");
		}

		if (0 < nodes.length) {
			sb.append('>');
			if (containsChildNodes) {
				sb.append(eol);
				for(int i=0; i<nodes.length; i++){
					sb.append(nodes[i].toString(true, ""));
					sb.append(eol);
				}
			} else {
				sb.append("...");
			}
			sb.append("</");
			sb.append(this.type);
			sb.append('>');
		} else {
			sb.append(" />");
		}
		return this.indent(sb.toString(), indentString);
	}


	/**
	 * {@link AstNode#toString(boolean,String) toString(boolean,String)}
	 * メソッド内で使用され、出力コードにインデントを付加する処理を行います。
	 *
	 * @param codeString toStringの出力コード（未インデント）
	 * @param indentString インデントに使用する文字列
	 * @return 引数codeStringの内容にインデントが付加されたコード
	 */
	private String indent(String codeString, String indentString) {
		String eol = System.getProperty("line.separator");
		String[] line = codeString.split(eol);
		int n = line.length;
		StringBuilder sb = new StringBuilder();
		int indent = 0;
		for(int i=0; i<n; i++) {
			if (line[i].startsWith("</")) {
				indent--;
				for(int j=0; j<indent; j++) {
					sb.append(indentString);
				}
			} else if(line[i].startsWith("<") && !line[i].endsWith("/>")) {
				for(int j=0; j<indent; j++) {
					sb.append(indentString);
				}
				indent++;
			} else {
				for(int j=0; j<indent; j++) {
					sb.append(indentString);
				}
			}
			sb.append(line[i]);
			if (i != n-1) {
				sb.append(eol);
			}
		}
		return sb.toString();
	}

}
