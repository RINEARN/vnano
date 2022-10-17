/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;


import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.interconnect.AbstractFunction;
import org.vcssl.nano.interconnect.AbstractVariable;
import org.vcssl.nano.interconnect.InternalFunction;
import org.vcssl.nano.interconnect.FunctionTable;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.interconnect.InternalVariable;
import org.vcssl.nano.interconnect.VariableTable;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.IdentifierSyntax;
import org.vcssl.nano.spec.LiteralSyntax;
import org.vcssl.nano.spec.ScriptWord;
import org.vcssl.nano.spec.OptionKey;


/**
 * The class performing the function of the semantic analyzer in the compiler of the Vnano.
 */
public class SemanticAnalyzer {

	/**
	 * Create a new semantic analyzer.
	 */
	public SemanticAnalyzer() {
	}


	/**
	 * Analyze semantics of the AST and, returns new AST of which information
	 * required for generating intermediate code are supplemented.
	 *
	 * @param inputAst The root node of the AST to be analyzed.
	 * @param Intterconnect interconnect The interconnect to which external variables/functions are connected.
	 * @return The semantic-analyzed/information-supplemented AST.
	 * @throws VnanoException Thrown when any semantic error has detected.
	 */
	public AstNode analyze(AstNode inputAst, Interconnect interconnect)
			throws VnanoException {

		AstNode outputAst = inputAst.clone();
		if (!inputAst.hasChildNodes()) {
			return outputAst;
		}

		Map<String, Object> optionMap = interconnect.getOptionMap();
		VariableTable globalVariableTable = interconnect.getExternalVariableTable();
		FunctionTable globalFunctionTable = interconnect.getExternalFunctionTable();

		// Check values and locations of dependency declaration statements ("import", "include", etc.).
		// Note that, this should be done before analyzing variable/function identifiers referred in the script.
		// Because: If the dependent libraries/plug-ins are not loaded/connected,
		//          their member variables/functions should not be found. In such case,
		//          the latter errors are "side effects" of the missing of libraries/plug-ins, 
		//          so we should indicate the user of the "direct cause" as an error message.
		this.checkDependencyDeclarationsAndLocations(outputAst, interconnect);

		// Replace all aliases of data-type names (e.g.: double to float, long to int, and so on).
		this.replaceAliasDataTypeNames(outputAst);

		// Set attributes of literal type leaf nodes.
		this.supplementLiteralLeafAttributes(outputAst);

		// For variable identifier leaf nodes, set attributes of referred variables.
		this.supplementVariableIdentifierLeafAttributes(outputAst, globalVariableTable);

		// Create a table of internal functions.
		FunctionTable localFunctionTable = this.extractFunctions(outputAst);

		// Set attributes of operator nodes.
		this.supplementOperatorAttributes(outputAst, globalFunctionTable, localFunctionTable);

		// Set attributes of function identifier leaf nodes from attributes of function call operators.
		this.supplementFunctionIdentifierLeafAttributes(outputAst);

		// Set attributes of expression nodes.
		this.supplementExpressionAttributes(outputAst);

		// Set attributes of function declaraton nodes.
		this.checkFunctionAttributes(outputAst);

		// Check types of left-hand-side nodes of assignment operators.
		this.checkAssignmentTargetWritabilities(outputAst);

		// Check target nodes of subscript operators.
		this.checkSubscriptTargetSubscriptabilities(outputAst);

		// Check identifiers (detect invalid identifiers).
		this.checkIdentifiers(outputAst);

		// Check literals (detect invalid literals).
		// リテラルを検査
		this.checkLiterals(outputAst);

		// Check locations of return statement nodes, data-types of returned values, and so on.
		this.checkReturnValueTypesAndLocations(outputAst);

		if (optionMap.containsKey(OptionKey.EVAL_ONLY_EXPRESSION)
				&& optionMap.get(OptionKey.EVAL_ONLY_EXPRESSION).equals(Boolean.TRUE)) {
			this.checkConsistsOfExpressions(outputAst, (String)optionMap.get(OptionKey.MAIN_SCRIPT_NAME));
		}

		if (optionMap.containsKey(OptionKey.EVAL_ONLY_FLOAT)
				&& optionMap.get(OptionKey.EVAL_ONLY_FLOAT).equals(Boolean.TRUE)) {
			this.checkConsistsOfFloats(outputAst, (String)optionMap.get(OptionKey.MAIN_SCRIPT_NAME));
		}

		return outputAst;
	}


	/**
	 * Replaces all aliases of data-type names.
	 * For example, "double" will be replaced to "float", and "long" will be replaced to "int".
	 *
	 * @param astRootNode The root node of the AST to be processed.
	 */
	private void replaceAliasDataTypeNames(AstNode astRootNode) {

		if (!astRootNode.hasChildNodes()) {
			return;
		}

		AstNode currentNode = astRootNode;

		// Traverse all nodes in the AST.
		do {
			currentNode = currentNode.getPreorderDftNextNode();

			// Skip the node if it hasn't a data-type information.
			// 属性値に型名を持っていなければスキップし、持っていれば取得する
			if (!currentNode.hasAttribute(AttributeKey.DATA_TYPE)) {
				continue;
			}
			String dataTypeName = currentNode.getAttribute(AttributeKey.DATA_TYPE);

			// Replace "double" to "float".
			if (dataTypeName.equals(DataTypeName.DOUBLE_FLOAT)) {
				currentNode.setAttribute(AttributeKey.DATA_TYPE, DataTypeName.DEFAULT_FLOAT);
				continue;
			}

			// Replace "long" to "int".
			if (dataTypeName.equals(DataTypeName.LONG_INT)) {
				currentNode.setAttribute(AttributeKey.DATA_TYPE, DataTypeName.DEFAULT_INT);
				continue;
			}

		} while (!currentNode.isPreorderDftLastNode());
	}


	/**
	 * Supplements some attributes of leaf nodes referencing variables.
	 *
	 * For example, this method determine the referred variable by a variable identifier node
	 * based on scopes and the global variable table,
	 * and set its data-type, array-rank, and so on to the node as attribtues.
	 *
	 * This method also checks duplicate declarations of multiple variables in the same scope.
	 *
	 * @param astRootNode astRootNode The root node of the AST to be processed.
	 * @param globalVariableTable The table to resolve references to global variables.
	 * @throws VnanoException
	 *            Thrown when referred variables are not found in the scope,
	 *            or multiple variables are declared in the same scope.
	 */
	private void supplementVariableIdentifierLeafAttributes(AstNode astRootNode, VariableTable globalVariableTable)
					throws VnanoException {

		if (!astRootNode.hasChildNodes()) {
			return;
		}

		// Create a table storing local variables accessible from the currently traversed node.
		// (During we are traversing the AST, variables are dynamically added to / removed from this table, based on scopes.)
		VariableTable localVariableTable = new VariableTable();

		// To distinguish multiple variables having the same name, assign a unique serial number for each local variables.
		int localVariableSerialNumber = 0;

		// The counter of the number of variables declared in a block scope.
		// (For removing them from the table when the traversal flow exits from the block.)
		int currentBlockVariableCounter = 0;

		// Parameter variable of functions, and counter variables of "for" statements, and so on are belongs to the next block.
		int nextBlockVariableCounter = 0;       // The counter for the above. Used when the following flag is true.
		boolean shouldCountToNextBlock = false; // Set true when the traversal flow has stepped on a function declaration or a "for" statement.

		// The stack for storing the value of currentBlockVariableCounter when the traversal flow enters a new (nested) block.
		Deque<Integer>scopeLocalVariableCounterStack = new ArrayDeque<Integer>();

		// The stack for storing closing blocks if they exist on the traversal path to the next node.
		// See the description of: AstNode#getPreorderDfsNextNode(...)
		Deque<AstNode> closedBlockStack = new ArrayDeque<AstNode>();


		// Traverse all nodes in the AST.
		AstNode currentNode = astRootNode;
		do {
			// Go to the next node. If closing blocks exist on the path to it, they will be pushed to closedBlockStack.
			currentNode = currentNode.getPreorderDftNextNode(closedBlockStack, new AstNode.Type[]{ AstNode.Type.BLOCK } );

			// When the traversal flow steps on a function declaraton or a "for" statement,
			// enable the flag to use "nextBlockVariableCounter" instead of "currentBlockVariableCounter".
			// (This flag will be reset when the flow enters into the next block.)
			if (currentNode.getType() == AstNode.Type.FUNCTION || currentNode.getType() == AstNode.Type.FOR) {
				shouldCountToNextBlock = true;
				nextBlockVariableCounter = 0;
			}

			// The process when the traversal flow exits from the current block:
			// Remove local variables declared in the block from the table, and restore the value of "currentBlockVariableCounter" from the stack.
			// Note that, we must use "while" instead of "if" here, because multiple blocks may close at 1-step of the traversal.
			while (closedBlockStack.size() != 0) {
				closedBlockStack.pop();
				for (int i=0; i<currentBlockVariableCounter; i++) {
					localVariableTable.removeLastVariable();
				}
				currentBlockVariableCounter = scopeLocalVariableCounterStack.pop();
			}

			// The process when the traversal flow enters a new block:
			// Stores the current value of "currentBlockVariableCounter" to the stack, and reset its value to 0.
			if (currentNode.getType() == AstNode.Type.BLOCK) {
				scopeLocalVariableCounterStack.push(currentBlockVariableCounter); // add だと別の端への追加になるので注意
				currentBlockVariableCounter = 0;

				// When the new block is at the next of a function declaration node or a "for" statement node,
				// add the number of parameter/counter variables counted by "nextBlockVariableCounter".
				if (shouldCountToNextBlock) {
					currentBlockVariableCounter += nextBlockVariableCounter;
					nextBlockVariableCounter = 0;
					shouldCountToNextBlock = false;
				}
			}

			// Local variable declaration node:
			// Register the variable to the table, and set a serial number and its scope to the variable node.
			if (currentNode.getType() == AstNode.Type.VARIABLE) {
				String variableName = currentNode.getAttribute(AttributeKey.IDENTIFIER_VALUE);
				String dataTypeName = currentNode.getDataTypeName();
				int rank = currentNode.getRank();
				boolean isFunctionParam = currentNode.getParentNode().getType() == AstNode.Type.FUNCTION;
				boolean isConstant = currentNode.hasModifier(ScriptWord.CONST_MODIFIER);

				// Throw an exception when a variable having the same name already exists in the same scope,
				// excluding parameter variables of functions.
				if (localVariableTable.containsVariableWithName(variableName) && !isFunctionParam) {
					throw new VnanoException(
						ErrorType.DUPLICATE_VARIABLE_IDENTIFIER, new String[] {variableName},
						currentNode.getFileName(), currentNode.getLineNumber()
					);
				}

				// Create an instance storing information of the variable, and register it to the table.
				InternalVariable internalVariable = new InternalVariable(
					variableName, dataTypeName, rank, isConstant, localVariableSerialNumber
				);
				localVariableTable.addVariable(internalVariable);
				if (shouldCountToNextBlock) {
					nextBlockVariableCounter++;
				} else {
					currentBlockVariableCounter++;
				}

				// Set a serial number and scope to the variable node.
				currentNode.setAttribute(AttributeKey.IDENTIFIER_SERIAL_NUMBER, Integer.toString(localVariableSerialNumber));
				currentNode.setAttribute(AttributeKey.SCOPE, AttributeValue.LOCAL);

				localVariableSerialNumber++;
			}

			// Leaf node referencing a variable:
			// Resolve the referred variable, and set its information to the node.
			if (currentNode.getType() == AstNode.Type.LEAF
					&& currentNode.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.VARIABLE_IDENTIFIER)) {

				String variableName = currentNode.getAttribute(AttributeKey.IDENTIFIER_VALUE);
				AbstractVariable variable = null;

				// Local variable:
				if (localVariableTable.containsVariableWithName(variableName)) {
					variable = localVariableTable.getVariableByName(variableName);
					currentNode.setAttribute(AttributeKey.SCOPE, AttributeValue.LOCAL);
					currentNode.setAttribute(AttributeKey.IDENTIFIER_SERIAL_NUMBER, Integer.toString(variable.getSerialNumber()));

				// Global variable:
				} else if (globalVariableTable.containsVariableWithName(variableName)) {
					variable = globalVariableTable.getVariableByName(variableName);
					currentNode.setAttribute(AttributeKey.SCOPE, AttributeValue.GLOBAL);

				// When no variable has been found:
				} else {
					throw new VnanoException(
						ErrorType.VARIABLE_IS_NOT_FOUND, variableName, currentNode.getFileName(), currentNode.getLineNumber()
					);
				}

				// Set information of the variable, to the node referencing the variable.
				currentNode.setAttribute(AttributeKey.RANK, Integer.toString(variable.getRank()));
				currentNode.setAttribute(AttributeKey.DATA_TYPE, variable.getDataTypeName());
				if (variable.isConstant()) {
					currentNode.addModifier(ScriptWord.CONST_MODIFIER);
				}
				if (variable.hasNamespaceName()) {
					currentNode.setAttribute(AttributeKey.NAME_SPACE, variable.getNamespaceName());
				}
			}

		} while (!currentNode.isPreorderDftLastNode());
	}


	/**
	 * Extracts local function declared in the specified ASt, and returns the function table of them.
	 *
	 * This method also supplements some attributes for function declaration nodes in the specified AST.
	 *
	 * @param astRootNode The root node of the AST to be processed.
	 * @throws VnanoException Thrown when an incorrect function declaration has been detected.s
	 */
	private FunctionTable extractFunctions(AstNode astRootNode) throws VnanoException {

		FunctionTable localFunctionTable = new FunctionTable();

		if (!astRootNode.hasChildNodes()) {
			return localFunctionTable;
		}

		AstNode currentNode = astRootNode;

		// Traverse all nodes in the AST.
		do {
			currentNode = currentNode.getPreorderDftNextNode();

			// If the node is the local function declaration node:
			//   Register it to the local function table, and supplement some attributes to the node.
			if (currentNode.getType() == AstNode.Type.FUNCTION) {

				// In Vnano, a local function can be declared only at the next hierarchy of the root.
				if (currentNode.getDepth() != 1) {
					throw new VnanoException(
							ErrorType.FUNCTION_IS_DECLARED_IN_INVALID_PLASE,
							currentNode.getFileName(), currentNode.getLineNumber()
					);
				}

				// Get the name of the function, and the data-type of the return value.
				String functionName = currentNode.getAttribute(AttributeKey.IDENTIFIER_VALUE);
				String returnTypeName = currentNode.getAttribute(AttributeKey.DATA_TYPE);
				int returnRank = currentNode.getRank();

				// Get parameter variable declaration nodes, and check them.
				AstNode[] paramNodes = currentNode.getChildNodes();
				int paramLength = paramNodes.length;
				this.checkParameterDeclarationNodes(paramNodes);

				// Extract information of parameters.
				String[] paramNames = new String[paramLength];
				String[] paramTypeNames = new String[paramLength];
				int[] paramRanks = new int[paramLength];
				boolean[] paramRefs = new boolean[paramLength];
				boolean[] paramConsts = new boolean[paramLength];
				for (int paramIndex=0; paramIndex<paramLength; paramIndex++) {
					paramNames[paramIndex] = paramNodes[paramIndex].getAttribute(AttributeKey.IDENTIFIER_VALUE);
					paramTypeNames[paramIndex] = paramNodes[paramIndex].getAttribute(AttributeKey.DATA_TYPE);
					paramRanks[paramIndex] = paramNodes[paramIndex].getRank();
					paramRefs[paramIndex] = paramNodes[paramIndex].hasModifier(ScriptWord.REF_MODIFIER);
					paramConsts[paramIndex] = paramNodes[paramIndex].hasModifier(ScriptWord.CONST_MODIFIER);
				}

				// Create an instance of InternalFunction, for storing information of the function.
				InternalFunction internalFunction = new InternalFunction(
					functionName, paramNames, paramTypeNames, paramRanks, paramRefs, paramConsts, returnTypeName, returnRank
				);

				// Signatures of local functions must not conflict in the same scope.
				String signature = IdentifierSyntax.getSignatureOf(internalFunction);
				if (localFunctionTable.hasFunctionWithSignature(signature)) {
					throw new VnanoException(
						ErrorType.DUPLICATE_FUNCTION_SIGNATURE, new String[] {signature},
						currentNode.getFileName(), currentNode.getLineNumber()
					);
				}

				// Register the information of the function to the table.
				localFunctionTable.addFunction(internalFunction);
			}

		} while (!currentNode.isPreorderDftLastNode());

		return localFunctionTable;
	}


	/**
	 * Checks parameter variable declaration nodes of a function.
	 *
	 * @param paramNodes Parameter variable declaration nodes to be checked.
	 * @throws VnanoException Thrown when an incorrect node has been detected.
	 */
	private void checkParameterDeclarationNodes(AstNode[] paramNodes) throws VnanoException {

		// Check nodes of all parameter.
		for (AstNode paramNode: paramNodes) {

			// The type of the node must be the variable declaration.
			if (paramNode.getType() != AstNode.Type.VARIABLE) {
				throw new VnanoException(
						ErrorType.INVALID_ARGUMENT_DECLARATION,
						paramNode.getFileName(), paramNode.getLineNumber()
				);
			}

			// Declaration statements of parameter variables can't have initialization statements.
			// So its node must not have any child nodes, excepting array-length declaration nodes ([]).
			if (paramNode.hasChildNodes()) {
				AstNode[] paramChildNodes = paramNode.getChildNodes();
				for (AstNode paramChildNode: paramChildNodes) {
					AstNode.Type type = paramChildNode.getType();
					if (type != AstNode.Type.LENGTHS) {
						throw new VnanoException(
								ErrorType.INVALID_ARGUMENT_DECLARATION,
								paramNode.getFileName(), paramNode.getLineNumber()
						);
					}
				}
			}
		}
	}


	/**
	 * Supplements some attributes of literal-type leaf nodes, in the specified AST.
	 *
	 * For example, array-ranks will be set to nodes of literals as attribtues.
	 *
	 * @param astRootNode The root node of the AST to be processed.
	 */
	private void supplementLiteralLeafAttributes(AstNode astRootNode) {

		if (!astRootNode.hasChildNodes()) {
			return;
		}

		// Traverse all nodes in the AST.
		AstNode currentNode = astRootNode;
		do {
			currentNode = currentNode.getPreorderDftNextNode();

			// If the node is a literal-type leaf node:
			if (currentNode.getType() == AstNode.Type.LEAF
					&& currentNode.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.LITERAL)) {

				currentNode.setAttribute(AttributeKey.RANK, "0");   // In the current specification of Vnano, array literals are not supported.
				currentNode.addModifier(ScriptWord.CONST_MODIFIER); // Values of literals must not be modified in programs, so set them as constants.

				// Here data-types of literals have already been determined,
				// by Lexical Analyzer, because they are clear from contents of tokens.
			}
		} while (!currentNode.isPreorderDftLastNode());
	}


	/**
	 * Supplements some attributes of literal-type leaf nodes, in the specified AST.
	 *
	 * For example, data-types and array-ranks of values of operators will be set to operator noses as attribtues.
	 * Note that, before using this method, supplement attributes of leaf nodes by using
	 * {@link SemanticAnalyzer#supplementLeafAttributes supplementLeafAttributes} method.
	 *
	 * @param astRootNode The root node of the AST to be processed.
	 * @param globalFunctionTable The table storing information of global functions called in the specified AST.
	 * @param localFunctionTable The table storing information of local functions called in the specified AST.
	 * @throws VnanoException Thrown when an incorrect node has been detected.
	 */
	private void supplementOperatorAttributes(AstNode astRootNode,
			FunctionTable globalFunctionTable, FunctionTable localFunctionTable) throws VnanoException {

		// Traverse all nodes in the AST;
		AstNode currentNode = astRootNode.getPostorderDftFirstNode();
		while(currentNode != astRootNode) {

			if(currentNode.getType() == AstNode.Type.OPERATOR) {

				int rank = -1;
				String dataType = null;
				String operationDataType = null;

				String execType = currentNode.getAttribute(AttributeKey.OPERATOR_EXECUTOR);
				String syntaxType = currentNode.getAttribute(AttributeKey.OPERATOR_SYNTAX);

				switch (execType) {

					// Assignment operator:  (=)
					case AttributeValue.ASSIGNMENT : {
						AstNode[] inputNodes = currentNode.getChildNodes();
						dataType = inputNodes[0].getDataTypeName();
						operationDataType = dataType;
						rank = inputNodes[0].getRank();
						break;
					}

					// Arithmetic operators:  (+, -, *, /, %)
					case AttributeValue.ARITHMETIC : {
						switch (syntaxType) {
							case AttributeValue.BINARY : {
								AstNode[] inputNodes = currentNode.getChildNodes();
								String leftOperandType = inputNodes[0].getDataTypeName();
								String rightOperandType = inputNodes[1].getDataTypeName();
								dataType = this.analyzeArithmeticBinaryOperationDataType(
										leftOperandType, rightOperandType,
										currentNode.getAttribute(AttributeKey.OPERATOR_SYMBOL),
										currentNode.getFileName(), currentNode.getLineNumber()
								);
								operationDataType = dataType;
								rank = analyzeArithmeticComparisonLogicalBinaryOperationRank(
										inputNodes[0].getRank(), inputNodes[1].getRank(),
										currentNode.getAttribute(AttributeKey.OPERATOR_SYMBOL),
										currentNode.getFileName(), currentNode.getLineNumber()
								);
								break;
							}
							// Unary-minus:
							case AttributeValue.PREFIX : {
								AstNode[] inputNodes = currentNode.getChildNodes();
								dataType = inputNodes[0].getDataTypeName();
								operationDataType = dataType;
								rank = inputNodes[0].getRank();
								break;
							}
							case AttributeValue.POSTFIX : {
								throw new VnanoFatalException("Unexpected position of an arithmetic operator detected");
							}
						}
						break;
					}

					// Comparison operators:  (==, !=, <, >, <=, >=)
					case AttributeValue.COMPARISON : {
						AstNode[] inputNodes = currentNode.getChildNodes();
						String leftOperandType = inputNodes[0].getDataTypeName();
						String rightOperandType = inputNodes[1].getDataTypeName();
						dataType = DataTypeName.BOOL;
						operationDataType = this.analyzeComparisonBinaryOperatorDataType(
								leftOperandType, rightOperandType,
								currentNode.getAttribute(AttributeKey.OPERATOR_SYMBOL),
								currentNode.getFileName(), currentNode.getLineNumber()
						);
						rank = analyzeArithmeticComparisonLogicalBinaryOperationRank(
								inputNodes[0].getRank(), inputNodes[1].getRank(),
								currentNode.getAttribute(AttributeKey.OPERATOR_SYMBOL),
								currentNode.getFileName(), currentNode.getLineNumber()
						);
						break;
					}

					// Logical operators:  (&&, ||, !)
					case AttributeValue.LOGICAL : {
						switch (syntaxType) {
							case AttributeValue.BINARY : {
								AstNode[] inputNodes = currentNode.getChildNodes();
								String leftOperandType = inputNodes[0].getDataTypeName();
								String rightOperandType = inputNodes[1].getDataTypeName();
								dataType = DataTypeName.BOOL;
								operationDataType = this.analyzeLogicalBinaryOperationDataType(
										leftOperandType, rightOperandType,
										currentNode.getAttribute(AttributeKey.OPERATOR_SYMBOL),
										currentNode.getFileName(), currentNode.getLineNumber()
								);
								rank = analyzeArithmeticComparisonLogicalBinaryOperationRank(
										inputNodes[0].getRank(), inputNodes[1].getRank(),
										currentNode.getAttribute(AttributeKey.OPERATOR_SYMBOL),
										currentNode.getFileName(), currentNode.getLineNumber()
								);
								break;
							}
							case AttributeValue.PREFIX : {
								AstNode[] inputNodes = currentNode.getChildNodes();
								dataType = DataTypeName.BOOL;
								operationDataType = DataTypeName.BOOL;
								rank = inputNodes[0].getRank();
								break;
							}
						}
						break;
					}

					// Arithmetic-compound assignment operators:  (++, --, +=, -=, *=, /=, %=)
					case AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT : {
						switch (syntaxType) {
							case AttributeValue.BINARY : {
								AstNode[] inputNodes = currentNode.getChildNodes();
								String leftOperandType = inputNodes[0].getDataTypeName();
								String rightOperandType = inputNodes[1].getDataTypeName();
								dataType = inputNodes[0].getDataTypeName();
								operationDataType = this.analyzeArithmeticCompoundAssignmentOperationDataType(
										leftOperandType, rightOperandType,
										currentNode.getAttribute(AttributeKey.OPERATOR_SYMBOL),
										currentNode.getFileName(), currentNode.getLineNumber()
								);
								rank = analyzeCompoundAssignmentOperationRank(
										inputNodes[0].getRank(), inputNodes[1].getRank(),
										currentNode.getAttribute(AttributeKey.OPERATOR_SYMBOL),
										currentNode.getFileName(), currentNode.getLineNumber()
								);
								break;
							}
							// Prefix increments/decrements:
							case AttributeValue.PREFIX : {
								AstNode[] inputNodes = currentNode.getChildNodes();
								dataType = inputNodes[0].getDataTypeName();
								operationDataType = dataType;
								rank = inputNodes[0].getRank();
								break;
							}
							// Postfix increments/decrements:
							case AttributeValue.POSTFIX : {
								AstNode[] inputNodes = currentNode.getChildNodes();
								dataType = inputNodes[0].getDataTypeName();
								operationDataType = dataType;
								rank = inputNodes[0].getRank();
								break;
							}
						}
						break;
					}

					// Function call operator:
					case AttributeValue.CALL : {

						// Find the callee function from global/local function tables.
						AbstractFunction function = null;
						if (localFunctionTable.hasCalleeFunctionOf(currentNode)) {
							currentNode.setAttribute(AttributeKey.SCOPE, AttributeValue.LOCAL);
							function = localFunctionTable.getCalleeFunctionOf(currentNode);
						} else if (globalFunctionTable.hasCalleeFunctionOf(currentNode)) {
							currentNode.setAttribute(AttributeKey.SCOPE, AttributeValue.GLOBAL);
							function = globalFunctionTable.getCalleeFunctionOf(currentNode);
						} else {
							throw new VnanoException(
									ErrorType.FUNCTION_IS_NOT_FOUND,
									IdentifierSyntax.getSignatureOfCalleeFunctionOf(currentNode),
									currentNode.getFileName(), currentNode.getLineNumber()
							);
						}

						// Check callability of the function syntactically.
						// (e.g.: Data-types and array-ranks of actual args must match with parameter declarations of the function.)
						this.checkFunctionCallablility(function, currentNode);

						currentNode.setAttribute(AttributeKey.CALLEE_SIGNATURE, IdentifierSyntax.getSignatureOf(function));
						if (function.hasNamespaceName()) {
							currentNode.setAttribute(AttributeKey.NAME_SPACE, function.getNamespaceName());
						}

						String[] argumentDataTypeNames = this.getArgumentDataTypeNames(currentNode);
						int[] argumentArrayRanks = this.getArgumentArrayRanks(currentNode);
						dataType = function.getReturnDataTypeName(argumentDataTypeNames, argumentArrayRanks);
						operationDataType = dataType;
						rank = function.getReturnArrayRank(argumentDataTypeNames, argumentArrayRanks);

						// Check invocability depending on external function plug-ins and so on.
						// (e.g.: Types of data I/O interfaces must compatible with the data-types and array-ranks of actual args.)
						function.checkInvokability(argumentDataTypeNames, argumentArrayRanks);

						break;
					}

					// Array subscript operators:
					// ([])
					case AttributeValue.SUBSCRIPT : {
						AstNode[] inputNodes = currentNode.getChildNodes();
						dataType = inputNodes[0].getDataTypeName();
						rank = 0;
						break;
					}

					// Cast operators:
					case AttributeValue.CAST : {
						dataType = currentNode.getDataTypeName();
						operationDataType = dataType;
						rank = currentNode.getRank();
						break;
					}
				}

				// Set attributes to the operator node.
				if (dataType != null) {
					currentNode.setAttribute(AttributeKey.DATA_TYPE, dataType);
				}
				if (operationDataType != null) {
					currentNode.setAttribute(AttributeKey.OPERATOR_EXECUTION_DATA_TYPE, operationDataType);
				}
				if (rank != -1) {
					currentNode.setAttribute(AttributeKey.RANK, Integer.toString(rank));
				}
			}

			currentNode = currentNode.getPostorderDftNextNode();
		}
	}

	private String[] getArgumentDataTypeNames(AstNode callOperatorNode) {
		AstNode[] childNodes = callOperatorNode.getChildNodes();
		int argumentN = childNodes.length - 1;  // childNodes[0] is the node of the function identifier.
		String[] dataTypeNames = new String[argumentN];
		for (int argumentIndex=0; argumentIndex<argumentN; argumentIndex++) {
			dataTypeNames[argumentIndex] = childNodes[argumentIndex+1].getDataTypeName();
		}
		return dataTypeNames;
	}

	private int[] getArgumentArrayRanks(AstNode callOperatorNode) {
		AstNode[] childNodes = callOperatorNode.getChildNodes();
		int argumentN = childNodes.length - 1;  // childNodes[0] is the node of the function identifier.
		int[] arrayRanks = new int[argumentN];
		for (int argumentIndex=0; argumentIndex<argumentN; argumentIndex++) {
			arrayRanks[argumentIndex] = childNodes[argumentIndex+1].getRank();
		}
		return arrayRanks;
	}

	/**
	 * Checks callability of specified function syntactically.
	 *
	 * For example, data types and array-ranks of actual arguments must match with parameter declarations of the function.
	 * Note that, there are some additional rules restricting callability of functions,
	 * e.g.: constant values cannot be passed by reference.
	 *
	 * @param function The calleeFunction
	 * @param callerNode The AST node of the function-call operator.
	 * @throws VnanoException
	 *      Thrown when the specified function can't called with arguments of the specified function-call operator.
	 */
	private void checkFunctionCallablility(AbstractFunction function, AstNode callerNode) throws VnanoException {
		AstNode[] argNodes = callerNode.getChildNodes(); // [0] is the function identifier, and [1]...[N] are arguments.
		String[] parameterTypes = function.getParameterDataTypeNames();
		String[] parameterNames = function.getParameterNames();
		boolean[] areParamConst = function.getParameterConstantnesses();
		boolean[] areParamRef = function.getParameterReferencenesses();
		int paramN = parameterTypes.length; // Total number of parameters.
		// Note: parameterNames may be omitted for external functions, but parameterTypes always exist, so get paramN from the latter.

		// Check consistency of each argument and the corresponding parameter declaration:
		for (int paramIndex=0; paramIndex<paramN; paramIndex++) {

			// Get a node of an argument (argNodes[0] is the function identifier, and [1]...[param-1] are arguments).
			AstNode argNode = argNodes[paramIndex+1];

			// If the function hasn't any parameter: Error
			if (argNode.getAttribute(AttributeKey.DATA_TYPE).equals(DataTypeName.VOID)) {

				// Get the function name for displaying it in the error message.
				String argFunctionName = null; // "null" will not be displayed in the error message.
				if (argNode.getType() == AstNode.Type.OPERATOR
						&& argNode.getAttribute(AttributeKey.OPERATOR_EXECUTOR).equals(AttributeValue.CALL)) {
					AstNode argFunctionIdentifierNode = argNode.getChildNodes()[0];
					argFunctionName = argFunctionIdentifierNode.getAttribute(AttributeKey.IDENTIFIER_VALUE);
				}
				throw new VnanoException(
					ErrorType.VOID_RETURN_VALUE_PASSED_AS_ARGUMENT, argFunctionName,
					callerNode.getFileName(), callerNode.getLineNumber()
				);
			}

			// If the parameter is declared as "reference to non-constant variable", such as "int &x", not "const int &x":
			// Only a variable or an array-subscript operator can be passed.
			// Because a value at an address of a literal or a temporary value (register) must not be modified.
			if (areParamRef[paramIndex] && !areParamConst[paramIndex]) {

				boolean isVariable = argNode.getType() == AstNode.Type.LEAF
					&& argNode.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.VARIABLE_IDENTIFIER);
				boolean isSubscript = argNode.getType() == AstNode.Type.OPERATOR
					&& argNode.getAttribute(AttributeKey.OPERATOR_EXECUTOR).equals(AttributeValue.SUBSCRIPT);

				// If the arg is neither a variable nor an array-subscript operator: Error
				if (!isVariable && !isSubscript) {
					String[] errorWords = new String[] {
						Integer.toString(paramIndex+1), parameterNames[paramIndex], function.getFunctionName()
					};
					throw new VnanoException(
						ErrorType.NON_VARIABLE_IS_PASSED_BY_REFERENCE, errorWords,
						callerNode.getFileName(), callerNode.getLineNumber()
					);
				}
			}
		}
	}


	/**
	 * Supplements some attributes of literal-type leaf nodes of function identifiers, in the specified AST.
	 *
	 * Before using this method, it is necessary to supplement attributes of operators by using
	 * {@link SemanticAnalyzer#supplementOperatorAttributes supplementOperatorAttributes} method.
	 *
	 * @param astRootNode The root node of the AST to be processed.
	 */
	private void supplementFunctionIdentifierLeafAttributes(AstNode astRootNode) {

		// Traverse all nodes in the AST:
		AstNode currentNode = astRootNode.getPostorderDftFirstNode();
		while(currentNode != astRootNode) {

			// If the node is a function identifier:
			if(currentNode.getType() == AstNode.Type.LEAF &&
					currentNode.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.FUNCTION_IDENTIFIER)) {

				// Get the parent node, which is a node of a function call operator.
				AstNode callOperatorNode = currentNode.getParentNode();

				// Copy some attributes from the call operator node to the identifier node.
				currentNode.setAttribute(AttributeKey.DATA_TYPE, callOperatorNode.getAttribute(AttributeKey.DATA_TYPE));
				currentNode.setAttribute(AttributeKey.RANK, callOperatorNode.getAttribute(AttributeKey.RANK));
			}

			currentNode = currentNode.getPostorderDftNextNode();
		}
	}


	/**
	 * Supplements some attriutes of expression statement nodes, in the specified AST.
	 *
	 * Specifically, supplements values of data-type and array-rank attributes,
	 * from types/ranks of the node of the evaluated value of the expression.
	 *
	 * @param astRootNode The root node of the AST to be processed.
	 */
	private void supplementExpressionAttributes(AstNode astRootNode) {

		// Traverse all nodes in the AST, and set attribute to expression statement nodes:
		AstNode currentNode = astRootNode.getPostorderDftFirstNode();
		while(currentNode != astRootNode) {
			if(currentNode.getType() == AstNode.Type.EXPRESSION) {
				AstNode[] inputNodes = currentNode.getChildNodes();
				currentNode.setAttribute(AttributeKey.DATA_TYPE, inputNodes[0].getDataTypeName());
				currentNode.setAttribute(AttributeKey.RANK, Integer.toString(inputNodes[0].getRank()));
			}
			currentNode = currentNode.getPostorderDftNextNode();
		}
	}


	/**
	 * Determines the data-type of the "arithmetic operation" of (not the type of operated value of)
	 * arithmetic-assignment compound operator, from data-types of operands.
	 *
	 * On run-time, operands will be converted to the data-type returned by this method,
	 * and the arithmetic operation will be performed to them.
	 * Then, the operated value will be converted to the data-type of the right-hand-side variable,
	 * and stored to the variable.
	 *
	 * @param leftOperandType The data-type of the left operand.
	 * @param rightOperandType The data-type of the right operand.
	 * @param operatorSymbol The symbol of the operator.s
	 * @param fileName The name of the file in which the operator is written (will be displayed in the error message).
	 * @param lineNumber The line number at which the operator is written (will be displayed in the error message).
	 * @return The data-type of the arithmetic operation.
	 * @throws VnanoException Thrown when an inoperable data-type is specified.
	 */
	private String analyzeArithmeticCompoundAssignmentOperationDataType(
			String leftOperandType, String rightOperandType, String operatorSymbol,
			String fileName, int lineNumber) throws VnanoException {

		String arithmeticBinaryOperatorSymbol = null;

		if (operatorSymbol.equals(ScriptWord.ADDITION_ASSIGNMENT)) {
				arithmeticBinaryOperatorSymbol = ScriptWord.PLUS_OR_ADDITION;

		} else if (operatorSymbol.equals(ScriptWord.SUBTRACTION_ASSIGNMENT)) {
				arithmeticBinaryOperatorSymbol = ScriptWord.MINUS_OR_SUBTRACTION;

		} else if (operatorSymbol.equals(ScriptWord.MULTIPLICATION_ASSIGNMENT)) {
				arithmeticBinaryOperatorSymbol = ScriptWord.MULTIPLICATION;

		} else if (operatorSymbol.equals(ScriptWord.DIVISION_ASSIGNMENT)) {
				arithmeticBinaryOperatorSymbol = ScriptWord.DIVISION;

		} else if (operatorSymbol.equals(ScriptWord.REMAINDER_ASSIGNMENT)) {
				arithmeticBinaryOperatorSymbol = ScriptWord.REMAINDER;

		} else {
				throw new VnanoFatalException("Invalid arithmetic compound operator: " + operatorSymbol);
		}
		return this.analyzeArithmeticBinaryOperationDataType(
			leftOperandType, rightOperandType, arithmeticBinaryOperatorSymbol, fileName, lineNumber
		);
	}


	/**
	 * Determines the data-type of the arithmetic binary operation, from data-types of operands.
	 *
	 * On run-time, operands will be converted to the data-type returned by this method,
	 * and the comparison operation will be performed to them.
	 *
	 * @param leftOperandType The data-type of the left operand.
	 * @param rightOperandType The data-type of the right operand.
	 * @param operatorSymbol The symbol of the operator.s
	 * @param fileName The name of the file in which the operator is written (will be displayed in the error message).
	 * @param lineNumber The line number at which the operator is written (will be displayed in the error message).
	 * @return The data-type of the operation.
	 * @throws VnanoException Thrown when an inoperable data-type is specified.
	 */
	private String analyzeArithmeticBinaryOperationDataType(
			String leftOperandTypeName, String rightOperandTypeName, String operatorSymbol,
			String fileName, int lineNumber) throws VnanoException {

		DataType leftType = DataTypeName.getDataTypeOf(leftOperandTypeName);
		DataType rightType = DataTypeName.getDataTypeOf(rightOperandTypeName);

		// If at least one of operands is a "string", the result is string.
		if (leftType == DataType.STRING || rightType == DataType.STRING) {
			// Only "+" is available for string-type operands.
			if (operatorSymbol.equals(ScriptWord.PLUS_OR_ADDITION)) {
				return DataTypeName.STRING;
			}
		}

		// If both operands are "int", the result is "int".
		if (leftType == DataType.INT64 && rightType == DataType.INT64) {
			return DataTypeName.DEFAULT_INT;
		}

		// If both operands are "float", the result is "float".
		if (leftType == DataType.FLOAT64 && rightType == DataType.FLOAT64) {
			return DataTypeName.DEFAULT_FLOAT;
		}

		// The result of the operation between "int" and "float" is "float".
		if (leftType == DataType.INT64 && rightType == DataType.FLOAT64) {
			return DataTypeName.DEFAULT_FLOAT;
		}

		// The result of the operation between "float" and "int" is "float".
		if (leftType == DataType.FLOAT64 && rightType == DataType.INT64) {
			return DataTypeName.DEFAULT_FLOAT;
		}

		throw new VnanoException(
			ErrorType.INVALID_DATA_TYPES_FOR_BINARY_OPERATOR,
			new String[] {operatorSymbol, leftOperandTypeName, rightOperandTypeName},
			fileName, lineNumber
		);
	}


	/**
	 * Determines the data-type of the comparison binary operation (not the type of the operated value),
	 * from data-types of operands.
	 *
	 * On run-time, operands will be converted to the data-type returned by this method,
	 * and the comparison operation will be performed to them.
	 * The data-type of the operation result value (the value of the operator) is always "bool".
	 *
	 * @param leftOperandType The data-type of the left operand.
	 * @param rightOperandType The data-type of the right operand.
	 * @param operatorSymbol The symbol of the operator.s
	 * @param fileName The name of the file in which the operator is written (will be displayed in the error message).
	 * @param lineNumber The line number at which the operator is written (will be displayed in the error message).
	 * @return The data-type of the operation.
	 * @throws VnanoException Thrown when an inoperable data-type is specified.
	 */
	private String analyzeComparisonBinaryOperatorDataType(
			String leftOperandTypeName, String rightOperandTypeName, String operatorSymbol,
			String fileName, int lineNumber) throws VnanoException {

		DataType leftType = DataTypeName.getDataTypeOf(leftOperandTypeName);
		DataType rightType = DataTypeName.getDataTypeOf(rightOperandTypeName);

		// If at least one of operands is a "string", the result is string.
		if (leftType == DataType.STRING || rightType == DataType.STRING) {
			return DataTypeName.STRING;
		}

		// If both operands are "bool", the operation type is "bool".
		if (leftType == DataType.BOOL && rightType == DataType.BOOL) {
			return DataTypeName.BOOL;
		}

		// If both operands are "int", the operation type is "int".
		if (leftType == DataType.INT64 && rightType == DataType.INT64) {
			return DataTypeName.DEFAULT_INT;
		}

		// If both operands are "float", the operation type is "float".
		if (leftType == DataType.FLOAT64 && rightType == DataType.FLOAT64) {
			return DataTypeName.DEFAULT_FLOAT;
		}

		// The data-type of the operation between "int" and "float" is "float".
		if (leftType == DataType.INT64 && rightType == DataType.FLOAT64) {
			return DataTypeName.DEFAULT_FLOAT;
		}

		// The data-type of the operation between "float" and "int" is "float".
		if (leftType == DataType.FLOAT64 && rightType == DataType.INT64) {
			return DataTypeName.DEFAULT_FLOAT;
		}

		throw new VnanoException(
			ErrorType.INVALID_DATA_TYPES_FOR_BINARY_OPERATOR,
			new String[] {operatorSymbol, leftOperandTypeName, rightOperandTypeName},
			fileName, lineNumber
		);
	}


	/**
	 * Determines the data-type of the logical binary operation (not the type of the operated value),
	 * from data-types of operands.
	 *
	 * @param leftOperandType The data-type of the left operand.
	 * @param rightOperandType The data-type of the right operand.
	 * @param operatorSymbol The symbol of the operator.s
	 * @param fileName The name of the file in which the operator is written (will be displayed in the error message).
	 * @param lineNumber The line number at which the operator is written (will be displayed in the error message).
	 * @return The data-type of the operation.
	 * @throws VnanoException Thrown when an inoperable data-type is specified.
	 */
	private String analyzeLogicalBinaryOperationDataType(
			String leftOperandTypeName, String rightOperandTypeName, String operatorSymbol,
			String fileName, int lineNumber) throws VnanoException {

		DataType leftType = DataTypeName.getDataTypeOf(leftOperandTypeName);
		DataType rightType = DataTypeName.getDataTypeOf(rightOperandTypeName);

		// All operands of a logical operation must be "bool" type.
		if (leftType == DataType.BOOL && rightType == DataType.BOOL) {
			return DataTypeName.BOOL;
		}
		throw new VnanoException(
			ErrorType.INVALID_DATA_TYPES_FOR_BINARY_OPERATOR,
			new String[] {operatorSymbol, leftOperandTypeName, rightOperandTypeName},
			fileName, lineNumber
		);
	}


	/**
	 * Determines the data-type of arithmetic/comparison/logical binary operations,
	 * from arra-ranks of operands.
	 *
	 * Generally, array-ranks of both operands must be the same, and then this method returns that rank.
	 *
	 * However, in Vnano, operation between a scalar (rank is 0) and a non-scalar is supported.
	 * In such case, the scalar value will be converted to the array of which rank is the same as the non-scalar operand.
	 * So in such case, this method returns the rank of the non-scalar operand.
	 *
	 * @param leftOperandRank The array-rank of the left operand.
	 * @param rightOperandRank The array-rank of the right operand.
	 * @param operatorSymbol The symbol of the operator.s
	 * @param fileName The name of the file in which the operator is written (will be displayed in the error message).
	 * @param lineNumber The line number at which the operator is written (will be displayed in the error message).
	 * @return The array-rank of the operation.
	 * @throws VnanoException
	 *   Thrown when array-ranks of operands are not the same, excluding when one is a scalar and the other is a non-scalar.
	 */
	int analyzeArithmeticComparisonLogicalBinaryOperationRank(
			int leftOperandRank, int rightOperandRank, String operatorSymbol,
			String fileName, int lineNumber) throws VnanoException {

		// If both operands are scalars, the result is a scalar.
		if (leftOperandRank==0 && rightOperandRank==0) {
			return 0;
		}

		// If one of operands is a scalar, and the other is a non-scalar,
		// the result is the array-rank of the non-scalar operand.
		if (leftOperandRank==0 && rightOperandRank!=0) {
			return rightOperandRank;
		}
		if (leftOperandRank!=0 && rightOperandRank==0) {
			return leftOperandRank;
		}

		// Otherwise, both operands are non-scalars, so array-ranks of them must be the same.
		if (leftOperandRank == rightOperandRank) {
			return leftOperandRank;
		} else {
			throw new VnanoException(
				ErrorType.INVALID_RANKS_FOR_VECTOR_OPERATION,
				new String[] {operatorSymbol}, fileName, lineNumber
			);
		}
	}


	/**
	 * Determines the array-rank of the compound assignment binary operation (not the type of the operated value),
	 * from array-ranks of operands.
	 *
	 * On run-time, operands will be converted to arrays having the specified rank returned by this method,
	 * and the operation will be performed to them. Then the result will be stored to the left-side variable.
	 *
	 * @param leftOperandRank The array-rank of the left operand.
	 * @param rightOperandRank The array-rank of the right operand.
	 * @param operatorSymbol The symbol of the operator.s
	 * @param fileName The name of the file in which the operator is written (will be displayed in the error message).
	 * @param lineNumber The line number at which the operator is written (will be displayed in the error message).
	 * @return The array-rank of the operation.
	 * @throws VnanoException Thrown when an inoperable array-rank is specified.
	 */
	int analyzeCompoundAssignmentOperationRank(
			int leftOperandRank, int rightOperandRank, String operatorSymbol,
			String fileName, int lineNumber) throws VnanoException {

		// If both operands are scalar (rank=0) values, the operation result is a scalar.
		if (leftOperandRank==0 && rightOperandRank==0) {
			return 0;
		}

		// If the left-side operand is a scalar, and the right-side operand is an array,
		// Throw an exception because
		// the operation result (array) can not be stored into the left-side value (scalar).
		if (leftOperandRank==0 && rightOperandRank!=0) {
			throw new VnanoException(
				ErrorType.INVALID_COMPOUND_ASSIGNMENT_BETWEEN_SCALAR_AND_ARRAY,
				new String[] {operatorSymbol}, fileName, lineNumber
			);
		}

		// If the left-side operand is an array, and the right-side operand is a scalar,
		// the right-side operand will be converted to the array having the same rank as the left-side operand.
		// So this method returns the array-rank of the left-side operand.
		if (leftOperandRank!=0 && rightOperandRank==0) {
			return leftOperandRank;
		}

		// If both operands are arrays, their rank should be the same.
		if (leftOperandRank == rightOperandRank) {
			return leftOperandRank;
		} else {
			throw new VnanoException(
				ErrorType.INVALID_RANKS_FOR_VECTOR_OPERATION,
				new String[] {operatorSymbol}, fileName, lineNumber
			);
		}
	}


	/**
	 * Checks attributes of function declaration nodes in the sepcifed AST.
	 *
	 * For example, if names of parameters of a function declaration are omitted,
	 * the Parser parse it without throwing any exception.
	 * It is because the Parser sometimes used for parsing function-signatures
	 * from which parameter names are omitted, e.g.: fun(int,float).
	 * So this method detects missing parameter names in function declarations,
	 * and throws an Exception.
	 *
	 * @param astRootNode The root node of the AST to be checked.
	 * @throws VnanoException Thrown when an incorrect attribute has been detected.
	 */
	private void checkFunctionAttributes(AstNode astRootNode) throws VnanoException {

		// Traverse all nodes in the AST.
		AstNode currentNode = astRootNode.getPostorderDftFirstNode();
		while(currentNode != astRootNode) {

			// If the node is a function declaration:
			if(currentNode.getType() == AstNode.Type.FUNCTION) {

				// Check all nodes of parameter variables:
				AstNode[] childNodes = currentNode.getChildNodes();
				for (AstNode childNode: childNodes) {
					if (childNode.getType() != AstNode.Type.VARIABLE) {
						continue;
					}

					// If the parameter variable hasn't an identifier: Error
					if (!childNode.hasAttribute(AttributeKey.IDENTIFIER_VALUE)) {

						throw new VnanoException(
							ErrorType.NO_IDENTIFIER_IN_VARIABLE_DECLARATION,
							childNode.getFileName(), childNode.getLineNumber()
						);
					}
				}
			}
			currentNode = currentNode.getPostorderDftNextNode();
		}
	}


	/**
	 * Check modifiability of destination operands of assignment operators
	 * (containig arithmetic-assignment compound operators).
	 *
	 * @param astRootNode The root node of the AST to be checked.
	 * @throws VnanoException Thrown when an unmodifiable operand is specified as a dest of an assignment.
	 */
	private void checkAssignmentTargetWritabilities(AstNode astRootNode) throws VnanoException {

		// Traverse all nodes in the AST.
		AstNode currentNode = astRootNode.getPostorderDftFirstNode();
		while(currentNode != astRootNode) {

			// If the node is an operator:
			if(currentNode.getType() == AstNode.Type.OPERATOR) {

				String execType = currentNode.getAttribute(AttributeKey.OPERATOR_EXECUTOR);
				switch (execType) {

					// Assignment operator:
					case AttributeValue.ASSIGNMENT : {

						// Check whether the assignment is an initializer or not,
						// because a constant variables can be modified by initializer,
						// and cannot be modified by (non-initializer) assignment operator.
						boolean isInitializer = (
							currentNode.getParentNode() != null
							&& currentNode.getParentNode().getParentNode() != null
							&& currentNode.getParentNode().getParentNode().getType() == AstNode.Type.VARIABLE
						);
						this.checkModifiability( currentNode.getChildNodes()[0], isInitializer);
						break;
					}
					// Compound assignment operator (containing increments/decrements):
					case AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT : {
						this.checkModifiability( currentNode.getChildNodes()[0], false );
						break;
					}
					default : {
						break;
					}
				}

			}

			currentNode = currentNode.getPostorderDftNextNode();
		}
	}


	/**
	 * Check modifiability of the specified AST node.
	 *
	 * @param node The node to be checked.
	 * @param writtenByInitializer Specify true for checking modifiability by initializers.
	 * @throws VnanoException Thrown when the specified node is unmodifiable.
	 */
	private void checkModifiability (AstNode node, boolean writtenByInitializer) throws VnanoException {
		String fileName = node.getFileName();
		int lineNumber = node.getLineNumber();

		// When the specified node is an array-subscript operator, return the modifiability of the array.
		if(node.getType() == AstNode.Type.OPERATOR
				&& node.getAttribute(AttributeKey.OPERATOR_EXECUTOR).equals(AttributeValue.SUBSCRIPT) ) {
			node = node.getChildNodes()[0];
		}

		// All non-leaf nodes are unmodifiable.
		if (node.getType() != AstNode.Type.LEAF) {
			throw new VnanoException(ErrorType.WRITING_TO_NON_LVALUE, fileName, lineNumber);
		}

		String leafType = node.getAttribute(AttributeKey.LEAF_TYPE);

		// If the leaf node is referencing a variable:
		if (leafType.equals(AttributeValue.VARIABLE_IDENTIFIER)) {

			// If the variable is "const", it is unmodifiable.
			if (node.hasModifier(ScriptWord.CONST_MODIFIER) && !writtenByInitializer) {
				throw new VnanoException(
					ErrorType.WRITING_TO_CONST_VARIABLE,
					new String[] { node.getAttribute(AttributeKey.IDENTIFIER_VALUE) },
					fileName, lineNumber
				);
			}

		// All literals are unmodifiable.
		} else if (leafType.equals(AttributeValue.LITERAL)) {
			String[] errorWords = { node.getAttribute(AttributeKey.LITERAL_VALUE) };
			throw new VnanoException(ErrorType.WRITING_TO_LITERAL, errorWords, fileName, lineNumber);

		// Other kinds of leaf nodes are unmodifiable.
		} else {
			throw new VnanoException(ErrorType.WRITING_TO_NON_LVALUE, fileName, lineNumber);
		}
	}


	/**
	 * Check first operands of array-subscript operators are arrays having correct rank, in the specified AST.
	 *
	 * @param astRootNode The root node of the AST to be checked.
	 * @throws VnanoException Thrown when an incorrect first operand of an array-subscript operator has been detected.
	 */
	private void checkSubscriptTargetSubscriptabilities(AstNode astRootNode) throws VnanoException {

		// Traverse all nodes in the AST.
		AstNode currentNode = astRootNode.getPostorderDftFirstNode();
		while(currentNode != astRootNode) {

			// If the node is an array-subscript operator:
			if(currentNode.getType() == AstNode.Type.OPERATOR
					&& currentNode.getAttribute(AttributeKey.OPERATOR_EXECUTOR).equals(AttributeValue.SUBSCRIPT)) {

				String fileName = currentNode.getFileName();
				int lineNumber = currentNode.getLineNumber();

				// Get the node of the first operand of the subscript operator.
				AstNode accessingNode = currentNode.getChildNodes()[0];

				// If the type of the operand node is not leaf: Error
				if (accessingNode.getType() != AstNode.Type.LEAF) {
					throw new VnanoException(ErrorType.SUBSCRIPTING_TO_UNSUBSCRIPTABLE_SOMETHING, fileName, lineNumber);
				}

				// If the operand node is not a variable: Error
				if (!accessingNode.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.VARIABLE_IDENTIFIER)) {
					String[] errorWords = null;
					if (accessingNode.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.LITERAL)) {
						errorWords = new String[]{ accessingNode.getAttribute(AttributeKey.LITERAL_VALUE) };
					}
					throw new VnanoException(ErrorType.SUBSCRIPTING_TO_UNSUBSCRIPTABLE_SOMETHING, errorWords, fileName, lineNumber);
				}

				// If the operand node is not a scalar: Error
				if (accessingNode.getRank() == 0) {
					String[] errorWords = { accessingNode.getAttribute(AttributeKey.IDENTIFIER_VALUE) };
					throw new VnanoException(
						ErrorType.SUBSCRIPTING_TO_UNSUBSCRIPTABLE_SOMETHING, errorWords, fileName, lineNumber
					);
				}

				// If the operand node is an array but its rank does not match with the number of indices: Error
				int numIndices = currentNode.getChildNodes().length-1;
				if (accessingNode.getRank() != numIndices) {
					String[] errorWords = {
						accessingNode.getAttribute(AttributeKey.IDENTIFIER_VALUE),
						Integer.toString(accessingNode.getRank()),
						Integer.toString(numIndices),
					};
					throw new VnanoException(ErrorType.INVALID_SUBSCRIPT_RANK, errorWords, fileName, lineNumber);
				}

			}

			currentNode = currentNode.getPostorderDftNextNode();
		}
	}


	/**
	 * Checks syntactic validity of identifiers in the specified AST.
	 *
	 * @param astRootNode The root node of the AST to be checked.
	 * @throws VnanoException Thrown when an invalid identifier has been detected.
	 */
	private void checkIdentifiers(AstNode astRootNode) throws VnanoException {

		// Traverse all nodes in the AST.
		AstNode currentNode = astRootNode.getPostorderDftFirstNode();
		while(currentNode != astRootNode) {

			// If the node is an variable/function declaration node:
			if(currentNode.getType() == AstNode.Type.VARIABLE || currentNode.getType() == AstNode.Type.FUNCTION) {

				String fileName = currentNode.getFileName();
				int lineNumber = currentNode.getLineNumber();
				String identifier = currentNode.getAttribute(AttributeKey.IDENTIFIER_VALUE);

				// If the content of the identifier is syntactically invalid, throw an exception.
				// (For example, identifiers must not start with a number.)
				if (!IdentifierSyntax.isValidSyntaxIdentifier(identifier)) {
					throw new VnanoException(ErrorType.INVALID_IDENTIFIER_SYNTAX, identifier, fileName, lineNumber);
				}

				// If the content of the identifier is the same as a reserved word, throw an exception.
				if (ScriptWord.RESERVED_WORD_SET.contains(identifier)) {
					throw new VnanoException(ErrorType.IDENTIFIER_IS_RESERVED_WORD, identifier, fileName, lineNumber);
				}
			}

			currentNode = currentNode.getPostorderDftNextNode();
		}
	}


	/**
	 * Checks syntactic validity of literals in the specified AST.
	 *
	 * @param astRootNode The root node of the AST to be checked.
	 * @throws VnanoException Thrown when an invalid literal has been detected.
	 */
	private void checkLiterals(AstNode astRootNode) throws VnanoException {

		// Traverse all nodes in the AST.
		AstNode currentNode = astRootNode.getPostorderDftFirstNode();
		while(currentNode != astRootNode) {

			// If the node is a lief node of a literal:
			if(currentNode.getType() == AstNode.Type.LEAF
					&& currentNode.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.LITERAL)) {

				String fileName = currentNode.getFileName();
				int lineNumber = currentNode.getLineNumber();
				String dataType = currentNode.getAttribute(AttributeKey.DATA_TYPE);
				String literal = currentNode.getAttribute(AttributeKey.LITERAL_VALUE);

				// In Vnano, integer literals start with 0 excluding 0x/0o/0b are invalid.
				// (Integer literals begin with 0 are regarded as octal literal in some languages,
				//  but it is confusing for most users. So in Vnano, use the prefix "0o" for octal literals.)
				if (dataType.equals(DataTypeName.DEFAULT_INT) || dataType.equals(DataTypeName.LONG_INT)) {

					if (literal.startsWith("0") && !literal.equals("0")  // If the number is not "0" but starts with "0".
						&& !literal.startsWith(LiteralSyntax.INT_LITERAL_HEX_PREFIX)  // And it does not start with "0x".
						&& !literal.startsWith(LiteralSyntax.INT_LITERAL_OCT_PREFIX)  // And it does not start with "0o".
						&& !literal.startsWith(LiteralSyntax.INT_LITERAL_BIN_PREFIX)  // And it does not start with "0b".
					) {
						throw new VnanoException(ErrorType.INT_LITERAL_STARTS_WITH_ZERO, fileName, lineNumber);
					}
				}
			}

			currentNode = currentNode.getPostorderDftNextNode();
		}
	}


	/**
	 * Checks returned values and locations of return statements.
	 * For example, the data-type of a returned value must match with the declaration of the function.
	 * In addition, return statements must not be outside of functions.
	 *
	 * @param astRootNode The root node of the AST to be checked.
	 * @throws VnanoException Thrown when an invalid return statement has been detected.
	 */
	private void checkReturnValueTypesAndLocations(AstNode astRootNode) throws VnanoException {

		// Variables for storing information of the currently checked function.
		String currentFunctionReturType = "";
		int currentFunctionReturnRank = -1;
		AstNode currentFunctionBlock = null;

		// The flag, will be set "true" when the traversal flow is traversing nodes in a function.
		boolean inFunction = false;

		// The stack for storing nodes of closing blocks in the traversal flow.
		// (See the comment of AstNode#getPreorderDfsNextNode(closedBlockStack) method.)
		Deque<AstNode> closedBlockStack = new ArrayDeque<AstNode>();


		// Traverse all nodes in the AST.
		AstNode currentNode = astRootNode;
		do {
			// Go to the next node, and push nodes of closed blocks to the stack, if they exist on the path to the next node.
			// (Multiple blocks may close in 1-step of the traversal.)
			currentNode = currentNode.getPreorderDftNextNode(closedBlockStack, new AstNode.Type[]{ AstNode.Type.BLOCK } );

			// If there are closed blocks on the path of the above,
			// and the block of the currently checked function is contained in them,
			// finish checking of the function so reset stored information of the function.
			if (closedBlockStack.size() != 0 && closedBlockStack.contains(currentFunctionBlock)) {
				closedBlockStack.clear();
				currentFunctionReturType = "";
				currentFunctionReturnRank = -1;
				currentFunctionBlock = null;
				inFunction = false;
			}

			// If the current node is a function declaration node:
			// store data-type and array-rank of the return value,
			// and store block statement node existing just after of the function declaration.
			if (currentNode.getType() == AstNode.Type.FUNCTION) {
				currentFunctionReturType = currentNode.getDataTypeName();
				currentFunctionReturnRank = currentNode.getRank();
				AstNode[] siblingNodes = currentNode.getParentNode().getChildNodes();   // Sibling nodes (containing the current node)
				currentFunctionBlock = siblingNodes[ currentNode.getSiblingIndex()+1 ]; // The next sibling node of the current node
				inFunction = true;
			}

			// If the current node is a return statement node:
			if (currentNode.getType() == AstNode.Type.RETURN) {

				// If the current traversal flow isn't isn't walking through inside of function block, throw an exception.
				if (!inFunction) {
					throw new VnanoException(ErrorType.RETURN_STATEMENT_IS_OUTSIDE_FUNCTIONS);
				}

				// If the return statement returns any value: check the data-type nad array-rank of it.
				if (currentNode.hasChildNodes()) {
					AstNode returnedValueNode = currentNode.getChildNodes()[0];

					// If the data-type or array-rank does not match with the declaration of the function, throw an exception.
					if (!returnedValueNode.getDataTypeName().equals(currentFunctionReturType)
						|| returnedValueNode.getRank() != currentFunctionReturnRank) {

						// Embed information into the error message, and throw it as an exception.
						String returnedTypeDescription = returnedValueNode.getDataTypeName();
						for (int dim=0; dim<returnedValueNode.getRank(); dim++) {
							returnedTypeDescription += ScriptWord.SUBSCRIPT_BEGIN + ScriptWord.SUBSCRIPT_END;
						}
						String expectedTypeDescription = currentFunctionReturType;
						for (int dim=0; dim<currentFunctionReturnRank; dim++) {
							expectedTypeDescription += ScriptWord.SUBSCRIPT_BEGIN + ScriptWord.SUBSCRIPT_END;
						}
						String[] errorWords = {returnedTypeDescription, expectedTypeDescription};
						throw new VnanoException(
							ErrorType.INVALID_RETURNED_VALUE_DATA_TYPE, errorWords,
							currentNode.getFileName(), currentNode.getLineNumber()
						);
					}

				// If the return statement has no value though the function is not void-type, throw an exception.
				} else if (!currentFunctionReturType.equals(DataTypeName.VOID)) {
					throw new VnanoException(
						ErrorType.RETURNED_VALUE_IS_MISSING, currentNode.getFileName(), currentNode.getLineNumber()
					);
				}
			}

		} while (!currentNode.isPreorderDftLastNode());
	}


	/**
	 * Checks values and locations of dependency declaration statements ("import" and so on).
	 *
	 * @param astRootNode The root node of the AST to be checked.
	 * @param interconnect The Interconnect having libraries/plug-ins, which may be referred by dependency declarations.
	 * @throws VnanoException
	 * 	   Thrown if a dependency declaration is out of the header section,
	 *     or specified library/namespace (provided by a plug-in) has not been found.
	 */
	private void checkDependencyDeclarationsAndLocations(AstNode astRootNode, Interconnect interconnect) throws VnanoException {

		// The flag representing that the currently traversed node is in header the section of the script.
		// ( Only an encoding declaration and dependency declarations can be described in the header section,
		//   and the encoding declaration should has been removed by Preprocessor. )
		boolean isInHeader = true;

		// Traverse all nodes in the AST.
		AstNode currentNode = astRootNode;
		do {
			currentNode = currentNode.getPreorderDftNextNode();

			// "import" or "include" declarations: check availability of the specified library or namespace (provided by a plug-in).
			if (currentNode.getType() == AstNode.Type.IMPORT || currentNode.getType() == AstNode.Type.INCLUDE) {
				if (isInHeader) {

					// The specified value of "import" / "include" declaration is connected to the IMPORT/INCLUDE node,
					// as a LITERAL type child node ("literalType" attribute is "dependencyIdentifier"), so check it.
					if (!currentNode.hasChildNodes()) {
						throw new VnanoFatalException("An expected child node of IMPORT/INCLUDE node has not been found.");
					}
					AstNode dependencyIdentifierNode = currentNode.getChildNodes()[0];
					if (dependencyIdentifierNode.getType() != AstNode.Type.LEAF
							|| !dependencyIdentifierNode.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.DEPENDENCY_IDENTIFIER)) {
						throw new VnanoFatalException("Incorrect node type has been detected for the child node of IMPORT/INCLUDE node.");
					}
					
					// Get the value of the specified dependency identifier (= "import path").
					String dependencyIdentifier = dependencyIdentifierNode.getAttribute(AttributeKey.IDENTIFIER_VALUE);

					// If the specified dependency is available, do nothing.
					boolean isDependencyAvailable = interconnect.hasDependentLibraryOrPlugin(dependencyIdentifier);
					if (isDependencyAvailable) {
						continue;

					// If the specified dependency is not available: Error.
					} else {
						throw new VnanoException(
							ErrorType.DECLARED_DEPENDENCY_IS_NOT_AVAILABLE, new String[] {dependencyIdentifier},
							currentNode.getFileName(), currentNode.getLineNumber()
						);
					}
					
				// If the dependency declaration exists at the out of the header section: Error.
				} else {
					throw new VnanoException(
						ErrorType.INVALID_DEPENDENCY_DECLARATION_LOCATION,
						currentNode.getFileName(), currentNode.getLineNumber()
					);
				}
			
			// Other node: ends the header section.
			} else {
				isInHeader = false;
			}

		} while (!currentNode.isPreorderDftLastNode());
	}


	/**
	 * In the specified AST,
	 * checks all statement nodes belonging to the specified script are expression statements.
	 *
	 * This method is used when EVAL_ONLY_EXPRESSION option is enabled.
	 *
	 * @param astRootNode The root node of the AST to be checked.
	 * @param targetScriptName The name of the script in which only expression statements should be written.
	 * @throws Thrown when non-expression statement belonging to the specified script has been detected.
	 */
	private void checkConsistsOfExpressions(AstNode astRootNode, String targetScriptName) throws VnanoException {

		// A HashSet containing allowable types of AST nodes.
		Set<AstNode.Type> targetNodeTypeSet = new HashSet<AstNode.Type>();
		targetNodeTypeSet.add(AstNode.Type.EXPRESSION);
		targetNodeTypeSet.add(AstNode.Type.OPERATOR);
		targetNodeTypeSet.add(AstNode.Type.LEAF);
		targetNodeTypeSet.add(AstNode.Type.PARENTHESIS);
		targetNodeTypeSet.add(AstNode.Type.ROOT);

		// Traverse all nodes in the AST.
		AstNode currentNode = astRootNode;
		do {
			currentNode = currentNode.getPreorderDftNextNode();
			String fileName = currentNode.getFileName();
			int lineNumber = currentNode.getLineNumber();

			// If the current node belongs to the specified script, check the type of the node.
			if (fileName.equals(targetScriptName) && !targetNodeTypeSet.contains(currentNode.getType())) {
				throw new VnanoException(ErrorType.NON_EXPRESSION_STATEMENTS_ARE_RESTRICTED, fileName, lineNumber);
			}

		} while (!currentNode.isPreorderDftLastNode());
	}


	/**
	 * In the specified AST,
	 * checks all data-types of expression elements (literals, variables, and so on)
	 * belonging to the specified script are "float" type.
	 *
	 * This method is used when EVAL_ONLY_FLOAT option is enabled.
	 *
	 * @param astRootNode The root node of the AST to be checked.
	 * @param targetScriptName The name of the script in which only expression statements should be written.
	 * @throws Thrown when non-float type expression element (literal, variable, and so on) has been detected.
	 */
	private void checkConsistsOfFloats(AstNode astRootNode, String targetScriptName) throws VnanoException {

		// A HashSet containing types of AST nodes to be checked.
		Set<AstNode.Type> targetNodeTypeSet = new HashSet<AstNode.Type>();
		targetNodeTypeSet.add(AstNode.Type.LEAF);
		targetNodeTypeSet.add(AstNode.Type.OPERATOR);

		// Traverse all nodes in the AST.
		AstNode currentNode = astRootNode;
		do {
			currentNode = currentNode.getPreorderDftNextNode();
			String fileName = currentNode.getFileName();
			int lineNumber = currentNode.getLineNumber();

			// If the current node belongs to the specified script, and it should be checked:
			if (fileName.equals(targetScriptName) && targetNodeTypeSet.contains(currentNode.getType())) {

				// If the data-type of the value of the node is non-float type, throw an exception.
				if (!currentNode.getAttribute(AttributeKey.DATA_TYPE).equals(DataTypeName.DEFAULT_FLOAT)) {
					throw new VnanoException(ErrorType.NON_FLOAT_DATA_TYPES_ARE_RESTRICTED, fileName, lineNumber);
				}
			}

		} while (!currentNode.isPreorderDftLastNode());
	}

}

