/*
 * Copyright(C) 2017-2022 RINEARN
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
 * The class represents a node of the AST (Abstract Syntax Tree) in the compiler of the Vnano.
 */
public class AstNode implements Cloneable {


	/**
	 * The enum to distinguish types of {@link AstNode AstNode} in the compiler of the Vnano.
	 */
	public enum Type {

		/** Represents the node at the root of the AST. */
		ROOT,

		/** Represents the node of a empty statement. */
		EMPTY,

		/** Represents the node of a variable declaration statement. */
		VARIABLE,

		/** Represents the node of a function declaration statement. */
		FUNCTION,

		/** Represents the node of a expression statement. */
		EXPRESSION,

		/** Represents the node of a block statement or a block of a function. */
		BLOCK,

		/** Represents the node of an if statement. */
		IF,

		/** Represents the node of a for statement. */
		FOR,

		/** Represents the node of a while statement. */
		WHILE,

		/** Represents the node of an else statement. */
		ELSE,

		/** Represents the node of a break statement. */
		BREAK,

		/**  Represents the node of a continue statement. */
		CONTINUE,

		/** Represents the node of a return statement. */
		RETURN,

		/** Represents the node at an end-point (leaf) in an expression. */
		LEAF,

		/** Represents the node of an operator in an expression. */
		OPERATOR,

		/** Represents the node of the array lengths of a variable declaration statement. */
		LENGTHS,

		/**
		 * (Used temporarily in the parser) The node for representing a parenthesis,
		 * but does not be contained in the AST after the parsing is completed.
		 */
		PARENTHESIS,

		/**
		 * (Used temporarily in the parser) The node for representing a boundary in the parsing-stack,
		 * but does not be contained in the AST after the parsing is completed.
		 */
		STACK_LID,
	}


	/** The strings used for indenting when this object is dumped. */
	private static final String DEFAULT_INDENT = "  ";

	/** Stores the type of this node. */
	private Type type;

	/** Stores the parent node of this node. */
	private AstNode parentNode;

	/** Stores children nodes of this node. */
	private List<AstNode> childNodeList;

	/** Stores the index which represents where this node is stored in the list storing child nodes in the parent node. */
	private int siblingIndex = 0;

	/** Stores where this node is in the absolute-hierarchy of the AST (0 for the root node). */
	private int depth = -1;

	/** Stores where this node is in the block-hierarchy of the AST (0 for the root node). */
	private int blockDepth = -1;

	/** Stores the number of the line of the script at which the corresponding code with this node is. */
	private int lineNumber = -1;

	/** Stores the name of the script in which the corresponding code with this node is. */
	private String fileName = null;

	/** Stores attributes of this node. */
	private Map<AttributeKey, String> attributeMap = null;


	/**
	 * Creates an AST node of the specified type.
	 * 
	 * @param value The type of the node to be created.
	 * @param lineNumber The number of the line of the script at which the corresponding code with the creating node is.
	 * @param fileName The name of the script in which the corresponding code with the creating node is.
	 */
	public AstNode(Type type, int lineNumber, String fileName) {
		this.type = type;
		this.childNodeList = new ArrayList<AstNode>();
		this.lineNumber = lineNumber;
		this.fileName = fileName;

		// Why we use LikedHashMap instead of HashMap is: for keeping their displaying order in the result of "dump" method.
		this.attributeMap = new LinkedHashMap<AttributeKey, String>();
	}


	/**
	 * Creates a deep-copy of this node.
	 * 
	 * @return A deep-copy of this node.
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
	 * Gets the type of this node.
	 * 
	 * @return The type of this node.
	 */
	public Type getType() {
		return this.type;
	}


	/**
	 * Gets the number of the line of the script at which the corresponding code with this node is.
	 * 
	 * @return The number of the line.
	 */
	public int getLineNumber() {
		return this.lineNumber;
	}


	/**
	 * Gets the name of the script in which the corresponding code with this node is.
	 * 
	 * @return The file name of the script.
	 */
	public String getFileName() {
		return this.fileName;
	}


	/**
	 * Sets an attribute.
	 * 
	 * If the attribute having the same key already exists, it will be overwritten.
	 *
	 * @param attributeKey The key of the attribute to be set.
	 * @param attributeValue The value of the attribute to be set.
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
	 * Get the value of the attribute corresponding the specified key.
	 * 
	 * @param attributeKey The key of the attribute to be returned.
	 * @return The value of the attribute.
	 */
	public String getAttribute(AttributeKey attributeKey) {
		return this.attributeMap.get(attributeKey);
	}


	/**
	 * Remove the specified attribute.
	 *
	 * @param attributeKey The key of the attribute to be removed.
	 */
	public void removeAttribute(AttributeKey attributeKey) {
		this.attributeMap.remove(attributeKey);
	}


	/**
	 * Checks whether this node has the specified attribute or not.
	 * 
	 * @param attributeKey The key (name) of the attribute to be checked.
	 *
	 * @return True if this node has the specified attribute, and false if don't have.
	 */
	public boolean hasAttribute(AttributeKey attributeKey) {
		return this.attributeMap.containsKey(attributeKey);
	}


	/**
	 * Adds a node as a child of this node.
	 * 
	 * This method also modifies fields of the added child node, for example,
	 * the reference to the parent node, the index in siblings.
	 *
	 * PLEASE NOTE THAT this class is implemented without considering the case that
	 * a same instance is added to multiple parent nodes as a child,
	 * because this class has fields representing the location in the AST as mentioned above.
	 *
	 * @param node The node to be added as a child.
	 */
	public void addChildNode(AstNode node) {

		// Add the specified node to this node as a child.
		this.childNodeList.add(node);

		// Set the parent node of the specified child node to this node.
		node.parentNode = this;

		// Set the index in siblings of the specified child node.
		node.siblingIndex = this.childNodeList.size() - 1;
	}


	/**
	 * Adds multiple nodes as children.
	 * 
	 * @param nodes The array storing nodes to be added as children.
	 */
	public void addChildNodes(AstNode[] nodes) {
		for (AstNode node : nodes) {
			this.addChildNode(node);
		}
	}


	/**
	 * Gets all child (children) nodes.
	 * 
	 * @return The array storing all child (children) nodes.
	 */
	public AstNode[] getChildNodes() {
		return (AstNode[])this.childNodeList.toArray(new AstNode[this.childNodeList.size()]);
	}


	/**
	 * Gets all child (children) nodes of the specified type.
	 * 
	 * @param type The type of child (children) nodes to be returned.
	 * @return The array storing all child (children) nodes of specified type.
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
	 * Checks whether any children of this node exist or not.
	 * 
	 * @return True if any children exists, false if don't exist.
	 */
	public boolean hasChildNodes() {
		return this.childNodeList.size() != 0;
	}


	/**
	 * Checks whether any children of this node of the specified type exist or not.
	 * 
	 * @param type The type of children nodes to be checked.
	 * @return True if any children of the specified type exists, false if don't exist.
	 */
	public boolean hasChildNodes(AstNode.Type type) {
		return this.getChildNodes(type).length != 0;
	}


	/**
	 * Gets the parent node of this node.
	 * 
	 * The parent node is set automatically in {@link AstNode.addChildNode addChildNode} method
	 * when this node is added to the parent node as a child.
	 * To prevent breaking the tree-structure of the AST, there is no setter of the parent node.
	 *
	 * @return The parent node of this node.
	 */
	public AstNode getParentNode() {
		return this.parentNode;
	}


	/**
	 * Checks whether the parent node of this node exists or not.
	 * 
	 * @return True if the parent node exists, false if don't exist.
	 */
	public boolean hasParentNode() {
		return this.parentNode != null;
	}


	/**
	 * Gets the index which represents where this node is stored in the list storing child nodes in the parent node.
	 * 
	 * @return The index of this node in the list storing child nodes in the parent node.
	 */
	public int getSiblingIndex() {
		return this.siblingIndex;
	}


	/**
	 * Sets values of a depth and a block-depth to this node and all descendant nodes,
	 * where the depth of this node will be defined to be 0.
	 */
	public void updateDepths() {

		// Regard the depth of this node as 0 (so this method is usually called for a root node).
		this.depth = 0;
		this.blockDepth = 0;

		// If this node has no child nodes, no need to do anything additionally.
		if (!this.hasChildNodes()) {
			return;
		}

		// If this node has child nodes, traverses them and sets their depths.
		AstNode currentNode = this;
		do {
			currentNode = currentNode.getPreorderDftNextNode();

			// We are traversing nodes by the preorder-depth-first traversal, 
			// so when we set the depth of "currentNode", the depth of its parent should had already been set.
			// Hence, we can compute the depth of the currentNode from the depth of the parent node simply as follows:
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
	 * Gets where this node is in the absolute-hierarchy of the AST (0 for the root node).
	 * 
	 * The value to be returned by this method should be set/updated by calling
	 * {@link AstNode#updateDepths() updateDepths()} method.
	 *
	 * @return The absolute depth of this node in the AST.
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
	 * Gets where this node is in the block-hierarchy of the AST (0 for the root node).
	 * 
	 * The value to be returned by this method should be set/updated by calling
	 * {@link AstNode#updateDepths() updateDepths()} method.
	 *
	 * @return The block depth of this node in the AST.
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
	 * Gets the name of the data type which is set as {@link AttributeKey#DATA_TYPE DATA_TYPE} attribute.
	 * 
	 * @return The name of the data type.
	 */
	public String getDataTypeName() {
		return this.attributeMap.get(AttributeKey.DATA_TYPE);
	}


	/**
	 * Gets the array rank which is set as {@link AttributeKey#RANK RANK} attribute.
	 * 
	 * @return The array rank.
	 */
	public int getRank() {
		String rankWord = this.attributeMap.get(AttributeKey.RANK);
		return Integer.parseInt(rankWord);
	}


	/**
	 * Checks whether this node has the specified modifier in the value of {@link AttributeKey#MODIFIER MODIFIER} attribute.
	 * 
	 * @param modifier The modifier to be checked.
	 *
	 * @return True if this node has the spedicied modifiers, false if has'nt.
	 */
	public boolean hasModifier(String modifier) {
		return this.hasAttribute(AttributeKey.MODIFIER) && this.getAttribute(AttributeKey.MODIFIER).contains(modifier);
	}


	/**
	 * Append a modifier into the value of {@link AttributeKey#MODIFIER MODIFIER} attribute.
	 * 
	 * @param modifier The modifier to be appended.
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
	 * Gets the next node of this node in the order of the pre-order depth-first traversal (DFT).
	 * 
	 * Note that, preorder DFS visits the parent node only BEFORE when their children nodes are traversed.
	 *
	 * @return The next node.
	 */
	public AstNode getPreorderDftNextNode() {
		return this.getPreorderDftNextNode(null, null);
	}


	/**
	 * Checks whether this node is the last node in the order of the pre-order depth-first traversal (DFT).
	 * 
	 * @return True if this node is the last node, false if isn't.
	 */
	public boolean isPreorderDftLastNode() {
		return this.getPreorderDftNextNode() == null;
	}


	/**
	 * Gets the next node of this node in the order of the pre-order depth-first traversal (DFT).
	 * 
	 * Note that, preorder DFS visits the parent node only BEFORE when their children nodes are traversed.
	 *
	 * However, sometimes it is necessary to do something at both of opening/closing points of some types of nodes
	 * when traversing AST, in Semantic Analyzer, Code Generator, and so on.
	 * The argument "detectClosedNodeType" and "closedBlockStack" is useful in such cases.
	 *
	 * If there are "closing points" (points at which the traversing route goes outside of nodes)
	 * of specified types of nodes (specified by the argument "detectClosedNodeType") on the route to the next node,
	 * those nodes will be pushed to the stack passed as the argument "closedNodeStack".
	 *
	 * @param closedNodeStack The stack for storing closed nodes on the route to the next node.
	 * @param closedNodeDetectionTypes Specify the types of closing nodes to be detected.
	 * @return The next node.
	 */
	public AstNode getPreorderDftNextNode(Deque<AstNode> closedNodeStack, AstNode.Type[] closedNodeDetectionTypes) {

		// If children exist, go to the first child node.
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
		while (true) {

			// Before go to the next (sibling or parent) node, add the current traversing node to the closedNodeStack,
			// because traversing of its child nodes has finished (= the current traversing node has closed).
			if (closedNodeStack != null && closedNodeDetectionTypesSet.contains(currentNode.getType())) {
				closedNodeStack.push(currentNode);
			}

			// If a sibling added after the current node, go to it.
			if (currentNode.getSiblingIndex() < siblings.length-1) {
				return siblings[currentNode.getSiblingIndex() + 1];
			}

			// If there is no sibling after the current node,
			// go to the hierarchy of the parent node.
			if (parent.hasParentNode()) {
				currentNode = parent;
				parent = currentNode.getParentNode();
				siblings = parent.getChildNodes();

			// If there is no parent, the current node is the root node, so the traversal is completed.
			// (See also: the implementation of isPreorderDftLastNode method)
			} else {
				return null;
			}
		}
	}


	/**
	 * Gets the first node in the order of the post-order depth-first traversal (DFT).
	 * 
	 * This method is implemented for using only for the root node of the AST.
	 *
	 * @return The first node.
	 */
	public AstNode getPostorderDftFirstNode() {

		// Go to the first leaf node.
		AstNode currentNode = this;
		while (currentNode.hasChildNodes()) {
			currentNode = currentNode.getChildNodes()[0];
		}
		return currentNode;
	}


	/**
	 * Gets the next node of this node in the order of the post-order depth-first traversal (DFT).
	 * 
	 * Note that, postorder DFS visits the parent node only AFTER when their children nodes are traversed.
	 *
	 * @return The next node.
	 */
	public AstNode getPostorderDftNextNode() {

		// If there is no parent, the current node is the root node, so the traversal is completed.
		if (!this.hasParentNode()) {
			return null;
		}

		AstNode parent = this.getParentNode();
		AstNode[] siblings = parent.getChildNodes();

		// If this node is the last child in siblings, go to the parent node.
		if (siblings.length-1 == this.getSiblingIndex()) {
			return parent;

		// If there is a sibling node added after this node, go to its first leaf node.
		} else {
			return siblings[this.getSiblingIndex() + 1].getPostorderDftFirstNode();
		}
	}


	/**
	 * Checks whether this node is the last node in the order of the post-order depth-first traversal (DFT).
	 * 
	 * @return True if this node is the last node, false if isn't.
	 */
	public boolean isPostorderDftLastNode() {
		return !this.hasParentNode();
	}


	/**
	 * Gets the string representation of the (maybe partial) AST of which this node is the root.
	 * 
	 * The string representation of the AST returned by this method is expressed in XML-like format.
	 *
	 * @return The string representation of the AST.
	 */
	public String dump() {
		return this.dump(true, AstNode.DEFAULT_INDENT);
	}


	/**
	 * Gets the string representation of the (maybe partial) AST of which this node is the root.
	 * 
	 * The string representation of the AST returned by this method is expressed in XML-like format.
	 *
	 * @param containsChildNodes Specify false if you want to dump this node only, not the tree.
	 * @param indentString The string to be used for indenting.
	 * @return The string representation of the AST.
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
	 * Indents the non-indented string expression of the AST.
	 * 
	 * Ths method is used in {@link AstNode#dump(boolean,String) dump(boolean,String)} method.
	 *
	 * @param dumpString The string representation of the AST to be indented.
	 * @param indentString The string to be used for indenting.
	 * @return The indented string representation of the AST.
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
