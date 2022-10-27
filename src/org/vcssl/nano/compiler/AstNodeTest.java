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

		// Prepare a parent node and its child nodes.
		AstNode parent = new AstNode(AstNode.Type.BLOCK, 123, "Test.vnano");
		AstNode childA = new AstNode(AstNode.Type.IF, 124, "Test.vnano");
		AstNode childB = new AstNode(AstNode.Type.EXPRESSION, 125, "Test.vnano");
		AstNode childC = new AstNode(AstNode.Type.ELSE, 126, "Test.vnano");
		AstNode childD = new AstNode(AstNode.Type.EXPRESSION, 125, "Test.vnano");

		// "hasChildNodes()" should return "false" when we have not added any child node yet.
		assertEquals(false, parent.hasChildNodes());

		// Add child nodes to the parent node.
		parent.addChildNode(childA);
		parent.addChildNodes(new AstNode[]{ childB, childC, childD });

		// "hasChildNodes()" should return "true" after when we have added child nodes.
		assertTrue(parent.hasChildNodes());

		// Tests results of "hasChildNodes(AstNode.Type)" method.
		assertTrue(parent.hasChildNodes(AstNode.Type.IF));
		assertFalse(parent.hasChildNodes(AstNode.Type.FOR));

		// Get all child nodes, and check them.
		AstNode[] children = parent.getChildNodes();
		assertEquals(4, children.length);
		assertEquals(children[0], childA);
		assertEquals(children[1], childB);
		assertEquals(children[2], childC);
		assertEquals(children[3], childD);

		// Get child nodes of the specified type, and check them.
		children = parent.getChildNodes(AstNode.Type.EXPRESSION);
		assertEquals(2, children.length);
		assertEquals(children[0], childB);
		assertEquals(children[1], childD);
	}

	@Test
	public void testAddGetHasParentNode() {

		// Prepare a parent node and its child node.
		AstNode parent = new AstNode(AstNode.Type.ROOT, 0, "Test.vnano");
		AstNode child = new AstNode(AstNode.Type.BLOCK, 123, "Test.vnano");

		// "hasParentNode()" should return "false" when we have not added the child node to the parent node.
		assertFalse(child.hasParentNode());

		// "hasChildNodes()" should return "true" after when we have added the child node to the parent node.
		parent.addChildNode(child);
		assertTrue(child.hasParentNode());

		// Get the parent node and check it.
		assertEquals(parent, child.getParentNode());
	}


	@Test
	public void testGetSiblingIndex() {

		// Prepare a parent node and its child nodes.
		AstNode parent = new AstNode(AstNode.Type.BLOCK, 123, "Test.vnano");
		AstNode childA = new AstNode(AstNode.Type.IF, 124, "Test.vnano");
		AstNode childB = new AstNode(AstNode.Type.EXPRESSION, 125, "Test.vnano");
		AstNode childC = new AstNode(AstNode.Type.ELSE, 126, "Test.vnano");
		AstNode childD = new AstNode(AstNode.Type.EXPRESSION, 125, "Test.vnano");

		// Add child nodes to the parent node.
		parent.addChildNode(childA);
		parent.addChildNode(childB);
		parent.addChildNode(childC);
		parent.addChildNode(childD);

		// Get and check the sibling index, 
		// which is an order index of a child node in all child nodes added to a same parent, 
		// for each child node.
		assertEquals(0, childA.getSiblingIndex());
		assertEquals(1, childB.getSiblingIndex());
		assertEquals(2, childC.getSiblingIndex());
		assertEquals(3, childD.getSiblingIndex());
	}

	@Test
	public void testGetDepth() {

		// Prepare a root node and child nodes.
		AstNode root = new AstNode(AstNode.Type.ROOT, 123, "Test.vnano");
		AstNode childDepth1 = new AstNode(AstNode.Type.BLOCK, 124, "Test.vnano");
		AstNode childDepth2 = new AstNode(AstNode.Type.IF, 125, "Test.vnano");
		AstNode childDepth3 = new AstNode(AstNode.Type.EXPRESSION, 126, "Test.vnano");
		AstNode childDepth4 = new AstNode(AstNode.Type.OPERATOR, 125, "Test.vnano");

		// Link nodes in a "linear" form.
		root.addChildNode(childDepth1);
		childDepth1.addChildNode(childDepth2);
		childDepth2.addChildNode(childDepth3);
		childDepth3.addChildNode(childDepth4);

		// Update depth values of all nodes in the AST.
		root.updateDepths();

		// Get and check depth values of all nodes.
		assertEquals(0, root.getDepth());
		assertEquals(1, childDepth1.getDepth());
		assertEquals(2, childDepth2.getDepth());
		assertEquals(3, childDepth3.getDepth());
		assertEquals(4, childDepth4.getDepth());
	}

	@Test
	public void testSetGetHasAttribute() {

		// Prepare a node.
		AstNode node = new AstNode(AstNode.Type.LEAF, 123, "Test.vnano");

		// "hasAttribute(...)" should return "false" before when we have set any attributes.
		assertFalse(node.hasAttribute(AttributeKey.IDENTIFIER_VALUE));

		// "hasAttribute(...)" should return "true" after when we have set any attributes.
		node.setAttribute(AttributeKey.IDENTIFIER_VALUE, "hello");
		assertTrue(node.hasAttribute(AttributeKey.IDENTIFIER_VALUE));

		// Get and check the attribute we set.
		assertEquals("hello", node.getAttribute(AttributeKey.IDENTIFIER_VALUE));
	}

	@Test
	public void testSetGetDataTypeName() {

		// Prepare a node, and set the data-type name to it as an attribute.
		AstNode node = new AstNode(AstNode.Type.OPERATOR, 123, "Test.vnano");
		node.setAttribute(AttributeKey.DATA_TYPE, "int");

		// Get and check the above attribute.
		assertEquals("int", node.getDataTypeName());
	}

	@Test
	public void testSetGetRank() {

		// Prepare a node, and set the array-rank to it as an attribute.
		AstNode node = new AstNode(AstNode.Type.OPERATOR, 123, "Test.vnano");
		node.setAttribute(AttributeKey.ARRAY_RANK, "3");

		// Get and check the above attribute.
		assertEquals(3, node.getRank());
	}

	@Test
	public void testPreorderTraversal() {

		// Construct the following AST.
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

		// Check all nodes in the AST by traversing it in the order of preorder-depth-first traversal.

		AstNode node = r;

		assertFalse(node.isPreorderDftLastNode());
		node = node.getPreorderDftNextNode();
		assertEquals(a, node);

		assertFalse(node.isPreorderDftLastNode());
		node = node.getPreorderDftNextNode();
		assertEquals(b, node);

		assertFalse(node.isPreorderDftLastNode());
		node = node.getPreorderDftNextNode();
		assertEquals(b0, node);

		assertFalse(node.isPreorderDftLastNode());
		node = node.getPreorderDftNextNode();
		assertEquals(c, node);

		assertFalse(node.isPreorderDftLastNode());
		node = node.getPreorderDftNextNode();
		assertEquals(c0, node);

		assertFalse(node.isPreorderDftLastNode());
		node = node.getPreorderDftNextNode();
		assertEquals(c1, node);

		assertFalse(node.isPreorderDftLastNode());
		node = node.getPreorderDftNextNode();
		assertEquals(d, node);

		assertFalse(node.isPreorderDftLastNode());
		node = node.getPreorderDftNextNode();
		assertEquals(d0, node);

		assertFalse(node.isPreorderDftLastNode());
		node = node.getPreorderDftNextNode();
		assertEquals(d1, node);

		assertFalse(node.isPreorderDftLastNode());
		node = node.getPreorderDftNextNode();
		assertEquals(d2, node);

		// Check of the detection of the last node of the traversal.
		assertTrue(node.isPreorderDftLastNode());
		assertNull(node.getPreorderDftNextNode());
	}

	@Test
	public void testPostorderTraversal() {

		// Construct the following AST.
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

		// Check all nodes in the AST by traversing it in the order of preorder-depth-first traversal.

		AstNode node = r.getPostorderDftFirstNode();
		assertEquals(a, node);

		assertFalse(node.isPostorderDftLastNode());
		node = node.getPostorderDftNextNode();
		assertEquals(b0, node);

		assertFalse(node.isPostorderDftLastNode());
		node = node.getPostorderDftNextNode();
		assertEquals(b, node);

		assertFalse(node.isPostorderDftLastNode());
		node = node.getPostorderDftNextNode();
		assertEquals(c0, node);

		assertFalse(node.isPostorderDftLastNode());
		node = node.getPostorderDftNextNode();
		assertEquals(c1, node);

		assertFalse(node.isPostorderDftLastNode());
		node = node.getPostorderDftNextNode();
		assertEquals(c, node);

		assertFalse(node.isPostorderDftLastNode());
		node = node.getPostorderDftNextNode();
		assertEquals(d0, node);

		assertFalse(node.isPostorderDftLastNode());
		node = node.getPostorderDftNextNode();
		assertEquals(d1, node);

		assertFalse(node.isPostorderDftLastNode());
		node = node.getPostorderDftNextNode();
		assertEquals(d2, node);

		assertFalse(node.isPostorderDftLastNode());
		node = node.getPostorderDftNextNode();
		assertEquals(d, node);

		assertFalse(node.isPostorderDftLastNode());
		node = node.getPostorderDftNextNode();
		assertEquals(r, node);

		// Check of the detection of the last node of the traversal.
		assertTrue(node.isPostorderDftLastNode());
		assertNull(node.getPostorderDftNextNode());
	}
}
