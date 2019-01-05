/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AstNodeTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetType() {
		AstNode node = new AstNode(AstNode.Type.EXPRESSION, 123, "Test.vnano");
		assertEquals(AstNode.Type.EXPRESSION, node.getType());
	}

	@Test
	public void testGetLineNumber() {
		AstNode node = new AstNode(AstNode.Type.EXPRESSION, 123, "Test.vnano");
		assertEquals(123, node.getLineNumber());
	}

	@Test
	public void testGetFileName() {
		AstNode node = new AstNode(AstNode.Type.EXPRESSION, 123, "Test.vnano");
		assertEquals("Test.vnano", node.getFileName());
	}

	@Test
	public void testAddGetHasChildNodes() {

		// 親ノードと子ノードを用意
		AstNode parent = new AstNode(AstNode.Type.BLOCK, 123, "Test.vnano");
		AstNode childA = new AstNode(AstNode.Type.IF, 124, "Test.vnano");
		AstNode childB = new AstNode(AstNode.Type.EXPRESSION, 125, "Test.vnano");
		AstNode childC = new AstNode(AstNode.Type.ELSE, 126, "Test.vnano");
		AstNode childD = new AstNode(AstNode.Type.EXPRESSION, 125, "Test.vnano");

		// 子ノードを追加していなければ hasChildNodes() は false を返す
		assertEquals(false, parent.hasChildNodes());

		// 子ノードを追加
		parent.addChildNode(childA);
		parent.addChildNodes(new AstNode[]{ childB, childC, childD });

		// 子ノードが1個以上あれば hasChildNodes() は true を返す
		assertTrue(parent.hasChildNodes());

		// hasChildNodes にタイプを指定して子ノード検索の検査
		assertTrue(parent.hasChildNodes(AstNode.Type.IF));
		assertFalse(parent.hasChildNodes(AstNode.Type.FOR));

		// 全ての子ノードを取り出して一致検査
		AstNode[] children = parent.getChildNodes();
		assertEquals(4, children.length);
		assertEquals(children[0], childA);
		assertEquals(children[1], childB);
		assertEquals(children[2], childC);
		assertEquals(children[3], childD);

		// 特定のタイプの子ノードのみを取り出して一致検査
		children = parent.getChildNodes(AstNode.Type.EXPRESSION);
		assertEquals(2, children.length);
		assertEquals(children[0], childB);
		assertEquals(children[1], childD);
	}

	@Test
	public void testAddGetHasParentNode() {

		// 親ノードと子ノードを用意
		AstNode parent = new AstNode(AstNode.Type.ROOT, 0, "Test.vnano");
		AstNode child = new AstNode(AstNode.Type.BLOCK, 123, "Test.vnano");

		// 親ノードに追加していない時点では、子ノードの hasParentNode() は false を返す
		assertFalse(child.hasParentNode());

		// 親ノードに子ノードを追加した後は true を返す
		parent.addChildNode(child);
		assertTrue(child.hasParentNode());

		// 親ノードを取得して一致検査
		assertEquals(parent, child.getParentNode());
	}


	@Test
	public void testGetSiblingIndex() {

		// 親ノードと子ノードを用意
		AstNode parent = new AstNode(AstNode.Type.BLOCK, 123, "Test.vnano");
		AstNode childA = new AstNode(AstNode.Type.IF, 124, "Test.vnano");
		AstNode childB = new AstNode(AstNode.Type.EXPRESSION, 125, "Test.vnano");
		AstNode childC = new AstNode(AstNode.Type.ELSE, 126, "Test.vnano");
		AstNode childD = new AstNode(AstNode.Type.EXPRESSION, 125, "Test.vnano");

		// 配置
		parent.addChildNode(childA);
		parent.addChildNode(childB);
		parent.addChildNode(childC);
		parent.addChildNode(childD);

		// 親ノードから見た子ノードのインデックス（兄弟ノードの中での順序）を取得して検査
		assertEquals(0, childA.getSiblingIndex());
		assertEquals(1, childB.getSiblingIndex());
		assertEquals(2, childC.getSiblingIndex());
		assertEquals(3, childD.getSiblingIndex());
	}

	@Test
	public void testGetDepth() {

		// ルートノードと子ノードを用意
		AstNode root = new AstNode(AstNode.Type.ROOT, 123, "Test.vnano");
		AstNode childDepth1 = new AstNode(AstNode.Type.BLOCK, 124, "Test.vnano");
		AstNode childDepth2 = new AstNode(AstNode.Type.IF, 125, "Test.vnano");
		AstNode childDepth3 = new AstNode(AstNode.Type.EXPRESSION, 126, "Test.vnano");
		AstNode childDepth4 = new AstNode(AstNode.Type.OPERATOR, 125, "Test.vnano");

		// 階層が root -> childDepth1 -> childDepth2 ... と深くなるよう直列に配置
		root.addChildNode(childDepth1);
		childDepth1.addChildNode(childDepth2);
		childDepth2.addChildNode(childDepth3);
		childDepth3.addChildNode(childDepth4);

		// 各ノードの階層の深さを取得して検査
		assertEquals(0, root.getDepth());
		assertEquals(1, childDepth1.getDepth());
		assertEquals(2, childDepth2.getDepth());
		assertEquals(3, childDepth3.getDepth());
		assertEquals(4, childDepth4.getDepth());
	}

	@Test
	public void testSetGetHasAttribute() {

		// ノードを用意
		AstNode node = new AstNode(AstNode.Type.LEAF, 123, "Test.vnano");

		// 属性を追加していない時点では hasAttributes は false を返す
		assertFalse(node.hasAttribute(AttributeKey.IDENTIFIER_VALUE));

		// 属性を追加すると、その属性キーに対して  hasAttributes は true を返すようになる
		node.addAttribute(AttributeKey.IDENTIFIER_VALUE, "hello");
		assertTrue(node.hasAttribute(AttributeKey.IDENTIFIER_VALUE));

		// 属性値を取得して一致比較
		assertEquals("hello", node.getAttribute(AttributeKey.IDENTIFIER_VALUE));
	}

	@Test
	public void testSetGetDataTypeName() {

		// ノードを用意し、データ型名を属性値として設定
		AstNode node = new AstNode(AstNode.Type.OPERATOR, 123, "Test.vnano");
		node.addAttribute(AttributeKey.DATA_TYPE, "int");

		// データ型名を取得して一致比較
		assertEquals("int", node.getDataTypeName());
	}

	@Test
	public void testSetGetRank() {

		// ノードを用意し、次元を属性値として設定
		AstNode node = new AstNode(AstNode.Type.OPERATOR, 123, "Test.vnano");
		node.addAttribute(AttributeKey.RANK, "3");

		// データ型名を取得して一致比較
		assertEquals(3, node.getRank());
	}

	@Test
	public void testPreorderTraversal() {

		// 下図の形のASTを用意
		/*
                      r
             _________|___________________
            |         |         |         |
            a         b        _c_     ___d___
                      |       |   |   |   |   |
                      b0      c0  c1  d0  d1  d2
        */
		AstNode r = new AstNode(AstNode.Type.EXPRESSION, 0, "Test.vnano");
		AstNode a = new AstNode(AstNode.Type.OPERATOR, 1, "Test.vnano");
		AstNode b = new AstNode(AstNode.Type.OPERATOR, 2, "Test.vnano");
		AstNode b0 = new AstNode(AstNode.Type.LEAF, 3, "Test.vnano");
		AstNode c = new AstNode(AstNode.Type.OPERATOR, 4, "Test.vnano");
		AstNode c0 = new AstNode(AstNode.Type.LEAF, 5, "Test.vnano");
		AstNode c1 = new AstNode(AstNode.Type.LEAF, 6, "Test.vnano");
		AstNode d = new AstNode(AstNode.Type.OPERATOR, 7, "Test.vnano");
		AstNode d0 = new AstNode(AstNode.Type.LEAF, 8, "Test.vnano");
		AstNode d1 = new AstNode(AstNode.Type.LEAF, 9, "Test.vnano");
		AstNode d2 = new AstNode(AstNode.Type.LEAF, 10, "Test.vnano");
		r.addChildNode(a);
		r.addChildNode(b);
		r.addChildNode(c);
		r.addChildNode(d);
		b.addChildNode(b0);
		c.addChildNode(c0);
		c.addChildNode(c1);
		d.addChildNode(d0);
		d.addChildNode(d1);
		d.addChildNode(d2);

		// 以下、行がけ順の深さ優先走査でノードを移動しながら、正しいノードと一致検査

		AstNode node = r;

		assertFalse(node.isPreorderDfsTraversalLastNode());
		node = node.getPreorderDfsTraversalNextNode();
		assertEquals(a, node);

		assertFalse(node.isPreorderDfsTraversalLastNode());
		node = node.getPreorderDfsTraversalNextNode();
		assertEquals(b, node);

		assertFalse(node.isPreorderDfsTraversalLastNode());
		node = node.getPreorderDfsTraversalNextNode();
		assertEquals(b0, node);

		assertFalse(node.isPreorderDfsTraversalLastNode());
		node = node.getPreorderDfsTraversalNextNode();
		assertEquals(c, node);

		assertFalse(node.isPreorderDfsTraversalLastNode());
		node = node.getPreorderDfsTraversalNextNode();
		assertEquals(c0, node);

		assertFalse(node.isPreorderDfsTraversalLastNode());
		node = node.getPreorderDfsTraversalNextNode();
		assertEquals(c1, node);

		assertFalse(node.isPreorderDfsTraversalLastNode());
		node = node.getPreorderDfsTraversalNextNode();
		assertEquals(d, node);

		assertFalse(node.isPreorderDfsTraversalLastNode());
		node = node.getPreorderDfsTraversalNextNode();
		assertEquals(d0, node);

		assertFalse(node.isPreorderDfsTraversalLastNode());
		node = node.getPreorderDfsTraversalNextNode();
		assertEquals(d1, node);

		assertFalse(node.isPreorderDfsTraversalLastNode());
		node = node.getPreorderDfsTraversalNextNode();
		assertEquals(d2, node);

		// 走査の終端まで来たので、最終ノードと判定されているはず
		assertTrue(node.isPreorderDfsTraversalLastNode());
		assertNull(node.getPreorderDfsTraversalNextNode());
	}

	@Test
	public void testPostorderTraversal() {

		// 下図の形のASTを用意
		/*
                      r
             _________|___________________
            |         |         |         |
            a         b        _c_     ___d___
                      |       |   |   |   |   |
                      b0      c0  c1  d0  d1  d2
        */
		AstNode r = new AstNode(AstNode.Type.EXPRESSION, 0, "Test.vnano");
		AstNode a = new AstNode(AstNode.Type.OPERATOR, 1, "Test.vnano");
		AstNode b = new AstNode(AstNode.Type.OPERATOR, 2, "Test.vnano");
		AstNode b0 = new AstNode(AstNode.Type.LEAF, 3, "Test.vnano");
		AstNode c = new AstNode(AstNode.Type.OPERATOR, 4, "Test.vnano");
		AstNode c0 = new AstNode(AstNode.Type.LEAF, 5, "Test.vnano");
		AstNode c1 = new AstNode(AstNode.Type.LEAF, 6, "Test.vnano");
		AstNode d = new AstNode(AstNode.Type.OPERATOR, 7, "Test.vnano");
		AstNode d0 = new AstNode(AstNode.Type.LEAF, 8, "Test.vnano");
		AstNode d1 = new AstNode(AstNode.Type.LEAF, 9, "Test.vnano");
		AstNode d2 = new AstNode(AstNode.Type.LEAF, 10, "Test.vnano");
		r.addChildNode(a);
		r.addChildNode(b);
		r.addChildNode(c);
		r.addChildNode(d);
		b.addChildNode(b0);
		c.addChildNode(c0);
		c.addChildNode(c1);
		d.addChildNode(d0);
		d.addChildNode(d1);
		d.addChildNode(d2);

		// 以下、帰りがけ順の深さ優先走査でノードを移動しながら、正しいノードと一致検査

		AstNode node = r.getPostorderDfsTraversalFirstNode();
		assertEquals(a, node);

		assertFalse(node.isPostorderDfsTraversalLastNode());
		node = node.getPostorderDfsTraversalNextNode();
		assertEquals(b0, node);

		assertFalse(node.isPostorderDfsTraversalLastNode());
		node = node.getPostorderDfsTraversalNextNode();
		assertEquals(b, node);

		assertFalse(node.isPostorderDfsTraversalLastNode());
		node = node.getPostorderDfsTraversalNextNode();
		assertEquals(c0, node);

		assertFalse(node.isPostorderDfsTraversalLastNode());
		node = node.getPostorderDfsTraversalNextNode();
		assertEquals(c1, node);

		assertFalse(node.isPostorderDfsTraversalLastNode());
		node = node.getPostorderDfsTraversalNextNode();
		assertEquals(c, node);

		assertFalse(node.isPostorderDfsTraversalLastNode());
		node = node.getPostorderDfsTraversalNextNode();
		assertEquals(d0, node);

		assertFalse(node.isPostorderDfsTraversalLastNode());
		node = node.getPostorderDfsTraversalNextNode();
		assertEquals(d1, node);

		assertFalse(node.isPostorderDfsTraversalLastNode());
		node = node.getPostorderDfsTraversalNextNode();
		assertEquals(d2, node);

		assertFalse(node.isPostorderDfsTraversalLastNode());
		node = node.getPostorderDfsTraversalNextNode();
		assertEquals(d, node);

		assertFalse(node.isPostorderDfsTraversalLastNode());
		node = node.getPostorderDfsTraversalNextNode();
		assertEquals(r, node);

		// 走査の終端まで来たので、最終ノードと判定されているはず
		assertTrue(node.isPostorderDfsTraversalLastNode());
		assertNull(node.getPostorderDfsTraversalNextNode());
	}
}
