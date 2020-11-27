/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vcssl.nano.VnanoFatalException;


/**
 * <span class="lang-ja">
 * Vnano のコンパイラ内において, AST（抽象構文木）の構成ノードとなるクラスです
 * </span>
 * <span class="lang-en">
 * The class represents a node of the AST (Abstract Syntax Tree) in the compiler of the Vnano
 * </span>
 * .
 *
 * <p>
 * &raquo; <a href="../../../../../src/org/vcssl/nano/compiler/AstNode.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/compiler/AstNode.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/compiler/AstNode.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class AstNode implements Cloneable {


	/**
	 * <span class="lang-en">
	 * The enum to distinguish types of {@link AstNode AstNode} in the compiler of the Vnano
	 * </span>
	 * <span class="lang-ja">
	 * Vnano のコンパイラ内において, {@link AstNode AstNode} の種類を区別するための列挙子です
	 * </span>
	 * .
	 *
	 * @author RINEARN (Fumihiro Matsui)
	 */
	public enum Type {

		/**
		 * <span class="lang-en">Represents the node at the root of the AST</span>
		 * <span class="lang-ja">ASTの頂点（根）に唯一位置する, ルートノードを表します</span>
		 * .
		 */
		ROOT,

		/**
		 * <span class="lang-en">Represents the node of a empty statement</span>
		 * <span class="lang-ja">空文（内容が全く無い文）のノードを表します</span>
		 * .
		 */
		EMPTY,

		/**
		 * <span class="lang-en">Represents the node of a variable declaration statement</span>
		 * <span class="lang-ja">変数宣言文のノードを表します</span>
		 * .
		 */
		VARIABLE,

		/**
		 * <span class="lang-en">Represents the node of a function declaration statement</span>
		 * <span class="lang-ja">関数宣言文のノードを表します</span>
		 * .
		 */
		FUNCTION,

		/**
		 * <span class="lang-en">Represents the node of a expression statement</span>
		 * <span class="lang-ja">式文のノードを表します</span>
		 * .
		 */
		EXPRESSION,

		/**
		 * <span class="lang-en">Represents the node of a block statement or a block of a function</span>
		 * <span class="lang-ja">ブロック文または関数ブロックのノードを表します</span>
		 * .
		 */
		BLOCK,

		/**
		 * <span class="lang-en">Represents the node of an if statement</span>
		 * <span class="lang-ja">if 文のノードを表します</span>
		 * .
		 */
		IF,

		/**
		 * <span class="lang-en">Represents the node of a for statement</span>
		 * <span class="lang-ja">for 文のノードを表します</span>
		 * .
		 */
		FOR,

		/**
		 * <span class="lang-en">Represents the node of a while statement</span>
		 * <span class="lang-ja">while 文のノードを表します</span>
		 * .
		 */
		WHILE,

		/**
		 * <span class="lang-en">Represents the node of an else statement</span>
		 * <span class="lang-ja">else 文のノードを表します</span>
		 * .
		 */
		ELSE,

		/**
		 * <span class="lang-en">Represents the node of a break statement</span>
		 * <span class="lang-ja">break 文のノードを表します</span>
		 * .
		 */
		BREAK,

		/**
		 * <span class="lang-en">Represents the node of a continue statement</span>
		 * <span class="lang-ja">continue 文のノードを表します</span>
		 * .
		 */
		CONTINUE,

		/**
		 * <span class="lang-en">Represents the node of a return statement</span>
		 * <span class="lang-ja">return 文のノードを表します</span>
		 * .
		 */
		RETURN,

		/**
		 * <span class="lang-en">Represents the node at an end-point (leaf) in an expression</span>
		 * <span class="lang-ja">式の中における末端（葉）ノードを表します</span>
		 * .
		 */
		LEAF,

		/**
		 * <span class="lang-en">Represents the node of an operator in an expression</span>
		 * <span class="lang-ja">式の中における演算子ノードを表します</span>
		 * .
		 */
		OPERATOR,

		/**
		 * <span class="lang-en">Represents the node of the array lengths of a variable declaration statement</span>
		 * <span class="lang-ja">変数宣言文における, 配列要素数情報のノードを表します</span>
		 * .
		 */
		LENGTHS,

		/**
		 * <span class="lang-en">
		 * (Used temporarily in the parser) The node for representing a parenthesis,
		 * but does not be contained in the AST after the parsing is completed
		 * </span>
		 * <span class="lang-ja">
		 * 式の中での括弧を表しますが, 式の構文解析の最中でのみ使用され, 最終的なASTの構成要素には含まれません
		 * </span>
		 * .
		 */
		PARENTHESIS,

		/**
		 * <span class="lang-en">
		 * (Used temporarily in the parser) The node for representing a boundary in the parsing-stack,
		 * but does not be contained in the AST after the parsing is completed
		 * </span>
		 * <span class="lang-ja">
		 * 式の構文解析の最中において, スタックの領域を区切るために使用され, 最終的なASTの構成要素には含まれません
		 * </span>
		 * .
		 */
		STACK_LID,
	}


	/**
	 * <span class="lang-en">The strings used for indenting when this object is dumped</span>
	 * <span class="lang-ja">このオブジェクトのダンプ時に使用される, デフォルトのインデント文字列です</span>
	 * .
	 */
	private static final String DEFAULT_INDENT = "  ";

	/**
	 * <span class="lang-en">Stores the type of this node</span>
	 * <span class="lang-ja">このノードのタイプ（種類）を保持します</span>
	 * .
	 */
	private Type type;

	/**
	 * <span class="lang-en">Stores the parent node of this node</span>
	 * <span class="lang-ja">このノードの親ノードを保持します</span>
	 * .
	 */
	private AstNode parentNode;

	/**
	 * <span class="lang-en">Stores children nodes of this node</span>
	 * <span class="lang-ja">このノードの子ノードを保持するリストです</span>
	 * .
	 */
	private List<AstNode> childNodeList;

	/**
	 * <span class="lang-en">
	 * Stores the index which represents where this node is stored in the list storing child nodes in the parent node
	 * </span>
	 * <span class="lang-ja">
	 * このノードが, 親ノード内で子ノードを格納するリスト内で, 何番目に格納されているかを示すインデックスを保持します
	 * </span>
	 * .
	 */
	private int siblingIndex = 0;

	/**
	 * <span class="lang-en">Stores where this node is in the absolute-hierarchy of the AST (0 for the root node)</span>
	 * <span class="lang-ja">このノードの, AST内での絶対的な階層深度（根が深度0）を保持します</span>
	 * .
	 */
	private int depth = -1;

	/**
	 * <span class="lang-en">Stores where this node is in the block-hierarchy of the AST (0 for the root node)</span>
	 * <span class="lang-ja">このノードの, AST内でのブロックの階層深度（根が深度0）を保持します</span>
	 * .
	 */
	private int blockDepth = -1;

	/**
	 * <span class="lang-en">
	 * Stores the number of the line of the script at which the corresponding code with this node is
	 * </span>
	 * <span class="lang-ja">
	 * このノードが対応するコードがある, スクリプト内での行番号を保持します
	 * </span>
	 * .
	 */
	private int lineNumber = -1;

	/**
	 * <span class="lang-en">Stores the name of the script in which the corresponding code with this node is</span>
	 * <span class="lang-ja">このノードが対応するコードがある, スクリプトのファイル名を保持します</span>
	 * .
	 */
	private String fileName = null;

	/**
	 * <span class="lang-en">Stores attributes of this node</span>
	 * <span class="lang-ja">ASTノードの属性情報を保持するマップです</span>
	 * .
	 */
	private Map<AttributeKey, String> attributeMap = null;


	/**
	 * <span class="lang-en">Creates an AST node of the specified type</span>
	 * <span class="lang-ja">指定されたタイプのASTノードを生成します</span>
	 * .
	 * @param value
	 *   <span class="lang-en">The type of the node to be created.</span>
	 *   <span class="lang-ja">生成するノードのタイプ.</span>
	 *
	 * @param lineNumber
	 *   <span class="lang-en">The number of the line of the script at which the corresponding code with the creating node is.</span>
	 *   <span class="lang-ja">生成するノードに対応するコードがある, スクリプト内での行番号.</span>
	 *
	 * @param fileName
	 *   <span class="lang-en">The name of the script in which the corresponding code with the creating node is.</span>
	 *   <span class="lang-ja">生成するノードに対応するコードがある, スクリプトのファイル名.</span>
	 */
	public AstNode(Type type, int lineNumber, String fileName) {
		this.type = type;
		this.childNodeList = new ArrayList<AstNode>();
		this.lineNumber = lineNumber;
		this.fileName = fileName;

		// toStringでダンプする際など, 順序が不定だとテストやデバッグで面倒なので, HashMapではなくLinkedHashMapを使用
		this.attributeMap = new LinkedHashMap<AttributeKey, String>();
	}


	/**
	 * <span class="lang-en">Creates a deep-copy of this node</span>
	 * <span class="lang-ja">ディープコピーによる複製を生成します</span>
	 * .
	 * @return
	 *   <span class="lang-en">A deep-copy of this node.</span>
	 *   <span class="lang-ja">ディープコピーによる複製.</span>
	 */
	@Override
	public AstNode clone() {
		AstNode cloneNode = new AstNode(this.type, this.lineNumber, this.fileName);
		cloneNode.depth = this.depth;
		cloneNode.blockDepth = this.blockDepth;
		cloneNode.parentNode = this.parentNode;
		cloneNode.siblingIndex = this.siblingIndex;
		for (final AstNode childNode: this.childNodeList) {
			AstNode cloneChildNode = childNode.clone();
			cloneNode.addChildNode(cloneChildNode);
		}
		cloneNode.attributeMap = new LinkedHashMap<AttributeKey, String>(this.attributeMap); // コピー
		return cloneNode;
	}


	/**
	 * <span class="lang-en">Gets the type of this node</span>
	 * <span class="lang-ja">このノードのタイプを取得します</span>
	 * .
	 * @return
	 *   <span class="lang-en">The type of this node.</span>
	 *   <span class="lang-ja">このノードのタイプ.</span>
	 */
	public Type getType() {
		return this.type;
	}


	/**
	 * <span class="lang-en">
	 * Gets the number of the line of the script at which the corresponding code with this node is
	 * </span>
	 * <span class="lang-ja">このノードに対応するコードがある, スクリプト内での行番号を取得します</span>
	 * .
	 * @return
	 *   <span class="lang-en">The number of the line.</span>
	 *   <span class="lang-ja">行番号.</span>
	 */
	public int getLineNumber() {
		return this.lineNumber;
	}


	/**
	 * <span class="lang-en">
	 * Gets the name of the script in which the corresponding code with this node is
	 * </span>
	 * <span class="lang-ja">このノードに対応するコードがある, スクリプトのファイル名を取得します</span>
	 * .
	 * @return
	 *   <span class="lang-en">The file name of the script.</span>
	 *   <span class="lang-ja">スクリプトのファイル名.</span>
	 */
	public String getFileName() {
		return this.fileName;
	}


	/**
	 * <span class="lang-en">Sets an attribute</span>
	 * <span class="lang-ja">属性を設定します</span>
	 * .
	 * <span class="lang-en">If the attribute having the same key already exists, it will be overwritten.</span>
	 * <span class="lang-ja">既に同じキーの属性が存在する場合は, 上書きされます.</span>
	 *
	 * @param attributeKey
	 *   <span class="lang-en">The key of the attribute to be set.</span>
	 *   <span class="lang-ja">設定する属性のキー.</span>
	 *
	 * @param attributeValue
	 *   <span class="lang-en">The value of the attribute to be set.</span>
	 *   <span class="lang-ja">設定する属性の値.</span>
	 */
	public void setAttribute(AttributeKey attributeKey, String attributeValue) {
		if (attributeValue == null) {
			throw new VnanoFatalException("null can not be the value of an attribute");
		}
		if (this.attributeMap.containsKey(attributeKey)) {
			this.attributeMap.remove(attributeKey);
		}
		this.attributeMap.put(attributeKey, attributeValue);
	}


	/**
	 * <span class="lang-en">Get the value of the attribute corresponding the specified key</span>
	 * <span class="lang-ja">指定された属性キーに対応する属性の値を取得します</span>
	 * .
	 * @param attributeKey
	 *   <span class="lang-en">The key of the attribute to be returned.</span>
	 *   <span class="lang-ja">取得する属性のキー.</span>
	 *
	 * @return
	 *   <span class="lang-en">The value of the attribute.</span>
	 *   <span class="lang-ja">属性の値.</span>
	 */
	public String getAttribute(AttributeKey attributeKey) {
		return this.attributeMap.get(attributeKey);
	}


	/**
	 * <span class="lang-en">Remove the specified attribute</span>
	 * <span class="lang-ja">指定された属性を削除します</span>
	 *
	 * @param attributeKey
	 *   <span class="lang-en">The key of the attribute to be removed.</span>
	 *   <span class="lang-ja">削除する属性のキー.</span>
	 */
	public void removeAttribute(AttributeKey attributeKey) {
		this.attributeMap.remove(attributeKey);
	}


	/**
	 * <span class="lang-en">Checks whether this node has the specified attribute or not</span>
	 * <span class="lang-ja">このノードが, 指定された属性キーに対応する値を保持しているかどうかを確認します</span>
	 * .
	 * @param attributeKey
	 *   <span class="lang-en">The key (name) of the attribute to be checked.</span>
	 *   <span class="lang-ja">取得する属性のキー（名前）.</span>
	 *
	 * @return
	 *   <span class="lang-en">True if this node has the specified attribute, and false if don't have.</span>
	 *   <span class="lang-ja">このノードが指定された属性を保持している場合は true, していなければ false.</span>
	 */
	public boolean hasAttribute(AttributeKey attributeKey) {
		return this.attributeMap.containsKey(attributeKey);
	}


	/**
	 * <span class="lang-en">Adds a node as a child of this node</span>
	 * <span class="lang-ja">子ノードを追加します</span>
	 * .
	 * <span class="lang-en">
	 * This method also modifies fields of the added child node, for example,
	 * the reference to the parent node, the index in siblings.
	 * </span>
	 * <span class="lang-ja">
	 * このメソッドは, 指定された子ノードをこのノードに登録するだけでなく,
	 * 子ノードの保持する情報も書き変えます. 具体的には, 子ノードが内部で保持する親ノードの参照や,
	 * 兄弟ノード内での順序情報などが設定されます.
	 * </span>
	 *
	 * <span class="lang-en">
	 * PLEASE NOTE THAT this class is implemented without considering the case that
	 * one instance is added to multiple parent nodes as a child,
	 * because this class has fields representing the location in the AST as mentioned above.
	 * </span>
	 * <span class="lang-ja">
	 * なお, このAstNodeクラスの実装では, 上述の通りAST内での位置に関する情報を保持しているため,
	 * 同一の子ノードのインスタンスを, 複数の親ノードに追加する事は想定されていません.
	 * </span>
	 *
	 * @param node
	 *   <span class="lang-en">The node to be added as a child.</span>
	 *   <span class="lang-ja">追加する子ノード.</span>
	 */
	public void addChildNode(AstNode node) {

		// Add the specified node to this node as a child.
		// このASTノードに子ノードを登録
		this.childNodeList.add(node);

		// Set the parent node of the specified child node to this node.
		// 子ノードの親ノードをこのASTノードに設定
		node.parentNode = this;

		// Set the index in siblings of the specified child node.
		// このASTノードから見た, 子ノードの順番を示すインデックスを, 子ノードに設定
		node.siblingIndex = this.childNodeList.size() - 1;
	}


	/**
	 * <span class="lang-en">Adds multiple nodes as children</span>
	 * <span class="lang-ja">子ノードを複数追加します</span>
	 * .
	 * @param nodes
	 *   <span class="lang-en">The array storing nodes to be added as children.</span>
	 *   <span class="lang-ja">子ノードを格納する配列.</span>
	 */
	public void addChildNodes(AstNode[] nodes) {
		for (AstNode node : nodes) {
			this.addChildNode(node);
		}
	}


	/**
	 * <span class="lang-en">Gets all child (children) nodes</span>
	 * <span class="lang-ja">子ノードを全て取得します</span>
	 * .
	 * @return
	 *   <span class="lang-en">The array storing all child (children) nodes.</span>
	 *   <span class="lang-ja">全ての子ノードを格納する配列.</span>
	 */
	public AstNode[] getChildNodes() {
		return (AstNode[])this.childNodeList.toArray(new AstNode[this.childNodeList.size()]);
	}


	/**
	 * <span class="lang-en">Gets all child (children) nodes of the specified type</span>
	 * <span class="lang-ja">指定されたタイプの子ノードを全て取得します</span>
	 * .
	 * @param type
	 *   <span class="lang-en">The type of child (children) nodes to be returned.</span>
	 *   <span class="lang-ja">取得したい子ノードのタイプ.</span>
	 *
	 * @return
	 *   <span class="lang-en">The array storing all child (children) nodes of specified type.</span>
	 *   <span class="lang-ja">指定されたタイプの全ての子ノードを格納する配列.</span>
	 */
	public AstNode[] getChildNodes(Type type) {
		List<AstNode> resultList = new ArrayList<AstNode>();
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
	 * <span class="lang-en">Checks whether any children of this node exist or not.</span>
	 * <span class="lang-ja">子ノードが存在するかどうかを確認します</span>
	 * .
	 * @return
	 *   <span class="lang-en">True if any children exists, false if don't exist.</span>
	 *   <span class="lang-ja">存在すれば true, しなければ false が返されます.</span>
	 */
	public boolean hasChildNodes() {
		return this.childNodeList.size() != 0;
	}


	/**
	 * <span class="lang-en">Checks whether any children of this node of the specified type exist or not.</span>
	 * <span class="lang-ja">指定されたタイプの子ノードが存在するかどうかを確認します</span>
	 * .
	 * @param type
	 *   <span class="lang-en">The type of children nodes to be checked.</span>
	 *   <span class="lang-ja">確認したい子ノードのタイプ.</span>
	 *
	 * @return
	 *   <span class="lang-en">True if any children of the specified type exists, false if don't exist.</span>
	 *   <span class="lang-ja">存在すれば true, しなければ false が返されます.</span>
	 */
	public boolean hasChildNodes(AstNode.Type type) {
		return this.getChildNodes(type).length != 0;
	}


	/**
	 * <span class="lang-en">Gets the parent node of this node</span>
	 * <span class="lang-ja">親ノードを取得します</span>
	 * .
	 * <span class="lang-en">
	 * The parent node is set automatically in {@link AstNode.addChildNode addChildNode} method
	 * when this node is added to the parent node as a child.
	 * To prevent breaking the tree-structure of the AST, there is no setter of the parent node.
	 * </span>
	 * <span class="lang-ja">
	 * なお, 親ノードの設定は {@link AstNode.addChildNode addChildNode} メソッド内で自動的に行われ,
	 * 追加する子ノードに対して自身が親となるように設定されます.
	 * ASTの木構造の破壊を防ぐため, 親ノードの setter はありません.
	 * </span>
	 *
	 * @return
	 *   <span class="lang-en">The parent node of this node.</span>
	 *   <span class="lang-ja">親ノード.</span>
	 */
	public AstNode getParentNode() {
		return this.parentNode;
	}


	/**
	 * <span class="lang-en">Checks whether the parent node of this node exists or not.</span>
	 * <span class="lang-ja">親ノードが存在するかどうかを確認します</span>
	 * .
	 * @return
	 *   <span class="lang-en">True if the parent node exists, false if don't exist.</span>
	 *   <span class="lang-ja">存在すれば true, しなければ false が返されます.</span>
	 */
	public boolean hasParentNode() {
		return this.parentNode != null;
	}


	/**
	 * <span class="lang-en">
	 * Gets the index which represents where this node is stored in the list storing child nodes in the parent node
	 * </span>
	 * <span class="lang-ja">
	 * このノードが, 親ノード内で子ノードを格納するリスト内で, 何番目に格納されているかを示すインデックスを取得します
	 * </span>
	 * .
	 * @return
	 *   <span class="lang-en">The index of this node in the list storing child nodes in the parent node.</span>
	 *   <span class="lang-ja">親ノード内で子ノードを格納するリスト内での, このノードのインデックス.</span>
	 */
	public int getSiblingIndex() {
		return this.siblingIndex;
	}


	/**
	 * <span class="lang-en">
	 * Sets values of a depth and a block-depth to this node and all descendant nodes,
	 * where the depth of this node will be defined to be 0.
	 * </span>
	 * <span class="lang-ja">
	 * このノードを深度 0 と見なし, このノードおよび全ての子孫ノードに対して,
	 *  AST内での深度およびブロック深度を設定します</span>
	 * .
	 */
	public void updateDepths() {

		// このメソッドが呼び出されたノードの深度を 0 とする（従って通常は ROOT ノードに対して呼び出す）
		this.depth = 0;
		this.blockDepth = 0;

		// 子ノードが無ければ上記の設定のみで完了
		if (!this.hasChildNodes()) {
			return;
		}

		// ASTノードを, 行がけ順の深さ優先走査で辿って検査していく
		AstNode currentNode = this;
		do {
			currentNode = currentNode.getPreorderDftNextNode();

			// 走査順序により, 子ノードを辿っている時点で親ノードは既に走査済み（深度設定済み）のはずなので
			// 親ノードの深度情報から子ノードの深度情報を求めて設定する
			AstNode parentNode = currentNode.getParentNode();
			currentNode.depth = parentNode.depth + 1;
			if (currentNode.type == AstNode.Type.BLOCK) {
				currentNode.blockDepth = parentNode.blockDepth + 1;
			} else {
				currentNode.blockDepth = parentNode.blockDepth;
			}

		} while (!currentNode.isPreorderDftLastNode());
	}


	/**
	 * <span class="lang-en">Gets where this node is in the absolute-hierarchy of the AST (0 for the root node)</span>
	 * <span class="lang-ja">このノードの, AST内での絶対深度（根が深度0）を取得します</span>
	 * .
	 * <span class="lang-en">
	 * The value to be returned by this method should be set/updated by calling
	 * {@link AstNode#updateDepths() updateDepths()} method.
	 * </span>
	 * <span class="lang-ja">
	 * このメソッドが返す値は, 事前に {@link AstNode#updateDepths() updateDepths()}
	 * メソッドを呼び出して設定/更新されている必要があります.
	 * </span>
	 *
	 * @return
	 *   <span class="lang-en">The absolute depth of this node in the AST.</span>
	 *   <span class="lang-ja">AST内でのこのノードの絶対深度.</span>
	 */
	public int getDepth() {
		if (this.depth < 0) {
			throw new VnanoFatalException(
				"The depth value is not set yet."
				+
				"Before getting the depth value of a node, "
				+
				"call updateDepths() method of the ROOT node of the AST "
				+
				"which initializes/updates values of depths and the block-depths of all nodes in the AST."
			);
		}
		return this.depth;
	}


	/**
	 * <span class="lang-en">Gets where this node is in the block-hierarchy of the AST (0 for the root node)</span>
	 * <span class="lang-ja">このノードの, AST内でのブロック深度（根が深度0）を取得します</span>
	 * .
	 * <span class="lang-en">
	 * The value to be returned by this method should be set/updated by calling
	 * {@link AstNode#updateDepths() updateDepths()} method.
	 * </span>
	 * <span class="lang-ja">
	 * このメソッドが返す値は, 事前に {@link AstNode#updateDepths() updateDepths()}
	 * メソッドを呼び出して設定/更新されている必要があります.
	 * </span>
	 *
	 * @return
	 *   <span class="lang-en">The block depth of this node in the AST.</span>
	 *   <span class="lang-ja">AST内でのこのノードのブロック深度.</span>
	 */
	public int getBlockDepth() {
		if (this.blockDepth < 0) {
			throw new VnanoFatalException(
				"The block-depth value is not set yet."
				+
				"Before getting the block-depth value of a node, "
				+
				"call updateDepths() method of the ROOT node of the AST "
				+
				"which initializes/updates values of depths and the block-depths of all nodes in the AST."
			);
		}
		return this.blockDepth;
	}


	/**
	 * <span class="lang-en">
	 * Gets the name of the data type which is set as {@link AttributeKey#DATA_TYPE DATA_TYPE} attribute
	 * </span>
	 * <span class="lang-ja">
	 * {@link AttributeKey#DATA_TYPE DATA_TYPE} 属性として設定されているデータ型の名称を取得します
	 * </span>
	 * .
	 * @return
	 *   <span class="lang-en">The name of the data type.</span>
	 *   <span class="lang-ja">データ型の名称.</span>
	 */
	public String getDataTypeName() {
		return this.attributeMap.get(AttributeKey.DATA_TYPE);
	}


	/**
	 * <span class="lang-en">Gets the array rank which is set as {@link AttributeKey#RANK RANK} attribute</span>
	 * <span class="lang-ja">{@link AttributeKey#RANK RANK} 属性として設定されている配列次元数を取得します</span>
	 * .
	 * @return
	 *   <span class="lang-en">The array rank.</span>
	 *   <span class="lang-ja">配列次元数.</span>
	 */
	public int getRank() {
		String rankWord = this.attributeMap.get(AttributeKey.RANK);
		return Integer.parseInt(rankWord);
	}


	/**
	 * <span class="lang-en">
	 * Checks whether this node has the specified modifier in the value of {@link AttributeKey#MODIFIER MODIFIER} attribute
	 * </span>
	 * <span class="lang-ja">
	 * このノードが, 指定された修飾子を {@link AttributeKey#MODIFIER MODIFIER} 属性の中に持っているかどうかを判定します
	 * </span>
	 * .
	 * @param modifier
	 *   <span class="lang-en">The modifier to be checked.</span>
	 *   <span class="lang-ja">判定対象の修飾子.</span>
	 *
	 * @return
	 *   <span class="lang-en">True if this node has the spedicied modifiers, false if has'nt.</span>
	 *   <span class="lang-ja">持っていれば true, しなければ false が返されます.</span>
	 */
	public boolean hasModifier(String modifier) {
		return this.hasAttribute(AttributeKey.MODIFIER) && this.getAttribute(AttributeKey.MODIFIER).contains(modifier);
	}


	/**
	 * <span class="lang-en">Append a modifier into the value of {@link AttributeKey#MODIFIER MODIFIER} attribute</span>
	 * <span class="lang-ja">{@link AttributeKey#MODIFIER MODIFIER} 属性に, 新しい修飾子を追記します</span>
	 * .
	 * @param modifier
	 *   <span class="lang-en">The modifier to be appended.</span>
	 *   <span class="lang-ja">追加する修飾子.</span>
	 */
	public void addModifier(String modifier) {
		if (this.hasAttribute(AttributeKey.MODIFIER)) {
			String attributeValue = this.getAttribute(AttributeKey.MODIFIER);
			attributeValue += AttributeValue.MODIFIER_SEPARATOR + modifier;
			this.setAttribute(AttributeKey.MODIFIER, attributeValue);
		} else {
			this.setAttribute(AttributeKey.MODIFIER, modifier);
		}
	}


	/**
	 * <span class="lang-en">
	 * Gets the next node of this node in the order of the pre-order depth-first traversal (DFT)
	 * </span>
	 * <span class="lang-ja">
	 * AST内のノードを, 行がけ順で深さ優先走査(DFT)する場合における, このノードの次のノードを取得します
	 * </span>
	 * .
	 * <span class="lang-en">
	 * Please note that, preorder DFS visits the parent node only BEFORE when their children nodes are traversed.
	 * </span>
	 * <span class="lang-ja">
	 * なお, 行がけ順のDFTでは, 親ノードは子ノードを走査する前にのみ訪問され,
	 * 子ノードの走査終了後には素通りされて訪問されない事に注意が必要です.
	 * </span>
	 *
	 * @return
	 *   <span class="lang-en">The next node.</span>
	 *   <span class="lang-ja">次のノード.</span>
	 */
	public AstNode getPreorderDftNextNode() {
		return this.getPreorderDftNextNode(null, null);
	}


	/**
	 * <span class="lang-en">
	 * Checks whether this node is the last node in the order of the pre-order depth-first traversal (DFT)
	 * </span>
	 * <span class="lang-ja">
	 * AST内のノードを, 行がけ順で深さ優先走査(DFT)する場合において, このノードが最後のノードかどうかを判定します
	 * </span>
	 * .
	 * @return
	 *   <span class="lang-en">True if this node is the last node, false if isn't.</span>
	 *   <span class="lang-ja">このノードが最後なら true, 最後でなければ false.</span>
	 */
	public boolean isPreorderDftLastNode() {
		return this.getPreorderDftNextNode() == null;
	}


	/**
	 * <span class="lang-en">
	 * Gets the next node of this node in the order of the pre-order depth-first traversal (DFT)
	 * </span>
	 * <span class="lang-ja">
	 * AST内のノードを, 行がけ順で深さ優先走査(DFT)する場合における, このノードの次のノードを取得します
	 * </span>
	 * .
	 * <span class="lang-en">
	 * Please note that, preorder DFS visits the parent node only BEFORE when their children nodes are traversed.
	 * </span>
	 * <span class="lang-ja">
	 * なお, 行がけ順のDFTでは, 親ノードは子ノードを走査する前にのみ訪問され,
	 * 子ノードの走査終了後には素通りされて訪問されない事に注意が必要です.
	 * </span>
	 *
	 * <span class="lang-en">
	 * However, sometimes it is necessary to do something at both of opening/closing points of some types of nodes
	 * when traversing AST, in Semantic Analyzer, Code Generator, and so on.
	 * The argument "detectClosedNodeType" and "closedBlockStack" is useful in such cases.
	 * </span>
	 * <span class="lang-ja">
	 * しかしながら, 意味解析やコード生成のステージにおいては, しばしば特定の種類のノードの開き閉じの両方の地点において,
	 * 何らかの処理を行いたい場合もあります. 引数 detectClosedNodeType と closedBlockStack はそのような場合に有用です.
	 * </span>
	 *
	 * <span class="lang-en">
	 * If there are "closing points" (points at which the traversing route goes outside of nodes)
	 * of specified types of nodes (specified by the argument "detectClosedNodeType") on the route to the next node,
	 * those nodes will be pushed to the stack passed as the argument "closedNodeStack".
	 * </span>
	 * <span class="lang-ja">
	 * 次のノードへの移動経路において, detectClosedNodeType に含まれる種類のノードが閉じた(走査が脱出した)
	 * 地点が存在する場合は, そのブロックのノードが, 引数 closedNodeStack に渡されたスタックに push されます.
	 * </span>
	 *
	 * @param closedNodeStack
	 *   <span class="lang-en">The stack for storing closed nodes on the route to the next node.</span>
	 *   <span class="lang-ja">次のノードへの経路上に存在する, 閉じたノードを格納するスタック.</span>
	 *
	 * @param closedNodeDetectionTypes
	 *   <span class="lang-en">Specify the types of closing nodes to be detected.</span>
	 *   <span class="lang-ja">検出対象とする閉じノードの種類を指定します.</span>
	 *
	 * @return
	 *   <span class="lang-en">The next node.</span>
	 *   <span class="lang-ja">次のノード.</span>
	 */
	public AstNode getPreorderDftNextNode(Deque<AstNode> closedNodeStack, AstNode.Type[] closedNodeDetectionTypes) {

		// If children exist, go to the first child node.
		// 子ノードがある場合は先頭の子ノードに移動
		if (this.hasChildNodes()) {
			return this.getChildNodes()[0];
		}

		AstNode currentNode = this;
		AstNode parent = currentNode.getParentNode();
		AstNode[] siblings = parent.getChildNodes();

		Set<AstNode.Type> closedNodeDetectionTypesSet = new HashSet<AstNode.Type>();
		if (closedNodeDetectionTypes != null) {
			for (AstNode.Type type: closedNodeDetectionTypes) {
				closedNodeDetectionTypesSet.add(type);
			}
		}

		// If there are no children for this node, traverse other branches in the AST.
		// 子ノードが無い場合は, AST内の他の枝を走査する
		while (true) {

			// Before go to the next (sibling or parent) node, add the current traversing node to the closedNodeStack,
			// because traversing of its child nodes has finished (= the current traversing node has closed).
			// この時点で注視ノードの子ノードの走査は全て終了している（= 注視ノードがちょうど閉じた）ので,
			// 次のノード（兄弟かさらに親）に移動する前に, 注視ノードを closedNodeStack に追加
			if (closedNodeStack != null && closedNodeDetectionTypesSet.contains(currentNode.getType())) {
				closedNodeStack.push(currentNode);
			}

			// If a sibling added after the current node, go to it.
			// 現在の走査ノードよりも後に追加された兄弟ノードがあれば移動
			if (currentNode.getSiblingIndex() < siblings.length-1) {
				return siblings[currentNode.getSiblingIndex() + 1];
			}

			// If there is no sibling after the current node,
			// go to the hierarchy of the parent node.
			// 後の兄弟ノードがもう無ければ, 親ノードの階層に浮上する
			if (parent.hasParentNode()) {
				currentNode = parent;
				parent = currentNode.getParentNode();
				siblings = parent.getChildNodes();

			// If there is no parent, the current node is the root node, so the traversal is completed.
			// (See also: the implementation of isPreorderDftLastNode method)
			// 親ノードが無ければ, 現在のノードがルートノードなので, 走査は完了
			// (isPreorderDftLastNode メソッドの実装も参照)
			} else {
				return null;
			}
		}
	}


	/**
	 * <span class="lang-en">Gets the first node in the order of the post-order depth-first traversal (DFT)</span>
	 * <span class="lang-ja">AST内のノードを, 帰りがけ順で深さ優先走査する場合における, 最初のノードを取得します</span>
	 * .
	 * <span class="lang-en">
	 * This method is implemented for using only for the root node of the AST.
	 * </span>
	 * <span class="lang-ja">
	 * このメソッドは, ASTのルート（根の位置にある）ノードに対して使用する事のみを前提に実装されています.
	 * </span>
	 *
	 * @return
	 *   <span class="lang-en">The first node.</span>
	 *   <span class="lang-ja">最初のノード.</span>
	 */
	public AstNode getPostorderDftFirstNode() {

		// Go to the first leaf node.
		// 最初の末端（葉）ノードまで降りる
		AstNode currentNode = this;
		while (currentNode.hasChildNodes()) {
			currentNode = currentNode.getChildNodes()[0];
		}
		return currentNode;
	}


	/**
	 * <span class="lang-en">
	 * Gets the next node of this node in the order of the post-order depth-first traversal (DFT)
	 * </span>
	 * <span class="lang-ja">
	 * AST内のノードを, 帰りがけ順で深さ優先走査(DFT)する場合における, このノードの次のノードを取得します
	 * </span>
	 * .
	 * <span class="lang-en">
	 * Please note that, postorder DFS visits the parent node only AFTER when their children nodes are traversed.
	 * </span>
	 * <span class="lang-ja">
	 * なお, 帰りがけ順のDFTでは, 親ノードは子ノードを走査した直後にのみ訪問され,
	 * 子ノードの走査前には素通りされて訪問されない事に注意が必要です.
	 * </span>
	 *
	 * @return
	 *   <span class="lang-en">The next node.</span>
	 *   <span class="lang-ja">次のノード.</span>
	 */
	public AstNode getPostorderDftNextNode() {

		// If there is no parent, the current node is the root node, so the traversal is completed.
		// 親ノードが無ければ, 現在のノードがルートノードなので, 走査は完了
		// ( 本来は呼び出し側で isPostorderDftUpLastNode で確認しておくのが好ましい )
		if (!this.hasParentNode()) {
			return null;
		}

		AstNode parent = this.getParentNode();
		AstNode[] siblings = parent.getChildNodes();

		// If this node is the last child in siblings, go to the parent node.
		// 自分が兄弟の中で一番最後の子ノードなら, 親階層に上がる
		if (siblings.length-1 == this.getSiblingIndex()) {
			return parent;

		// If there is a sibling node added after this node, go to its first leaf node.
		// 後にまだ兄弟ノードがある場合は, そのブランチの最初の末端（葉）ノードまで降りる
		} else {
			return siblings[this.getSiblingIndex() + 1].getPostorderDftFirstNode();
		}
	}


	/**
	 * <span class="lang-en">
	 * Checks whether this node is the last node in the order of the post-order depth-first traversal (DFT)
	 * </span>
	 * <span class="lang-ja">
	 * AST内のノードを, 帰りがけ順で深さ優先走査(DFT)する場合において, このノードが最後のノードかどうかを判定します
	 * </span>
	 * .
	 * @return
	 *   <span class="lang-en">True if this node is the last node, false if isn't.</span>
	 *   <span class="lang-ja">このノードが最後なら true, 最後でなければ false.</span>
	 */
	public boolean isPostorderDftLastNode() {
		return !this.hasParentNode();
	}


	/**
	 * <span class="lang-en">
	 * Gets the string representation of the (maybe partial) AST of which this node is the root
	 * </span>
	 * <span class="lang-ja">
	 * このノードを頂点とする, （場合によっては部分的な）AST構造の文字列表現を返します
	 * </span>
	 * .
	 * <span class="lang-en">
	 * The string representation of the AST returned by this method is expressed in XML-like format.
	 * </span>
	 * <span class="lang-ja">
	 * このメソッドが返すASTの文字列表現は, XMLライクな書式で記述されたものになります.
	 * </span>
	 *
	 * @return
	 *   <span class="lang-en">The string representation of the AST.</span>
	 *   <span class="lang-ja">ASTの文字列表現.</span>
	 */
	public String dump() {
		return this.dump(true, AstNode.DEFAULT_INDENT);
	}


	/**
	 * <span class="lang-en">
	 * Gets the string representation of the (maybe partial) AST of which this node is the root
	 * </span>
	 * <span class="lang-ja">
	 * このノードを頂点とする, （場合によっては部分的な）AST構造の文字列表現を返します
	 * </span>
	 * .
	 * <span class="lang-en">
	 * The string representation of the AST returned by this method is expressed in XML-like format.
	 * </span>
	 * <span class="lang-ja">
	 * このメソッドが返すASTの文字列表現は, XMLライクな書式で記述されたものになります.
	 * </span>
	 *
	 * @param containsChildNodes
	 *   <span class="lang-en">Specify false if you want to dump this node only, not the tree.</span>
	 *   <span class="lang-ja">木構造ではなく, このノードのみをダンプしたい場合は, false を指定してください.</span>
	 *
	 * @param indentString
	 *   <span class="lang-ja">インデントに使用する文字列.</span>
	 *   <span class="lang-en">The string to be used for indenting.</span>
	 *
	 * @return
	 *   <span class="lang-en">The string representation of the AST.</span>
	 *   <span class="lang-ja">ASTの文字列表現.</span>
	 */
	public String dump(boolean containsChildNodes, String indentString) {
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
					sb.append(nodes[i].dump(true, ""));
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
		return this.indent(sb.toString(), indentString) + eol;
	}


	/**
	 * <span class="lang-en">Indents the non-indented string expression of the AST</span>
	 * <span class="lang-ja">未インデントのASTの文字列表現に, インデントを付加します</span>
	 * .
	 * <span class="lang-en">
	 * Ths method is used in {@link AstNode#dump(boolean,String) dump(boolean,String)} method.
	 * </sapn>
	 * <span class="lang-ja">
	 * このメソッドは {@link AstNode#dump(boolean,String) dump(boolean,String)} メソッド内で使用されます.
	 * </sapn>
	 *
	 * @param dumpString
	 *   <span class="lang-en">The string representation of the AST to be indented.</span>
	 *   <span class="lang-ja">インデントを付加したい, ASTの文字列表現.</span>
	 *
	 * @param indentString
	 *   <span class="lang-en">The string to be used for indenting.</span>
	 *   <span class="lang-ja">インデントに使用する文字列.</span>
	 *
	 * @return
	 *   <span class="lang-en">The indented string representation of the AST.</span>
	 *   <span class="lang-ja">インデントが付加された, ASTの文字列表現.</span>
	 */
	private String indent(String dumpString, String indentString) {
		String eol = System.getProperty("line.separator");
		String[] line = dumpString.split(eol);
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
