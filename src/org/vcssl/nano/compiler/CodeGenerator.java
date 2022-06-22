/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.AssemblyWord;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.IdentifierSyntax;
import org.vcssl.nano.spec.LiteralSyntax;
import org.vcssl.nano.spec.MetaInformationSyntax;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.spec.ScriptWord;


/**
 * The class performing the function of the code generator in the compiler of the Vnano.
 *
 * The processing of this class takes the semantic-analyzed AST as the input,
 * and outputs a kind of the intermediate code written in the VRIL 
 * (Vector Register Intermediate Language)
 * which is a virtual assembly language interpreted by the VM
 * ({@link org.vcssl.nano.vm.VirtualMachine}) of the Vnano.
 * 
 * @see <a href="https://www.vcssl.org/en-us/vril/">Specifications of VRIL</a>
 */
public class CodeGenerator {

	/** The array-rank of the scalar. */
	private static final int RANK_OF_SCALAR = 0;

	/** The string used as the name of labels excluding numbers */
	private static final String LABEL_NAME = "LABEL";

	/** The immediate value of "true", used in code of jump instructions */
	private final String IMMEDIATE_TRUE =
		AssemblyWord.IMMEDIATE_OPERAND_PREFIX + DataTypeName.BOOL + AssemblyWord.VALUE_SEPARATOR + LiteralSyntax.TRUE;

	/** The string of the placeholder which is put at the position of the unused operand for some instructions. */
	private final String PLACE_HOLDER = Character.toString(AssemblyWord.PLACEHOLDER_OPERAND_PREFIX);

	/** The counter to assign new registers. */
	private int registerCounter;

	/** The counter to assign new labels. */
	private int labelCounter;


	/**
	 * Creates an new code generator of which counters (e.g. counter to assign registers) are initialized by 0.
	 */
	public CodeGenerator() {
		this.registerCounter = 0;
		this.labelCounter = 0;
	}


	/**
	 * The class for storing information (context) depending on before/after statements which are
	 * necessary for transforming sequentially a statement to the (virtual) assembly code from the top.
	 * 
	 * For example, labels for breaking from the loop should be put at the end the instructions
	 * corresponding with statements in the loop,
	 * but should be determined when the instructions at the top of the loop are generated.
	 * It is because instructions at the top of the loop contains a jump instruction
	 * to jump to outside of the loop when the condition is false,
	 * and the jump instruction takes the label as an operand.
	 *
	 * This class is used in {@link CodeGenerator#trackAllStatements}
	 * and {@link CodeGenerator#generateStatementCode} methods.
	 */
	private class StatementTrackingContext implements Cloneable {

		/**
		 * When it should generate a jump instruction to the beginning point of a loop after generating other code, 
		 * store the label at the beginning point into this field.
		 */
		private String beginPointLabel = null;

		/**
		 * When it should put a label at the top of the updating process (of counter and so on) of a loop, 
		 * store the label into this field.
		 */
		private String updatePointLabel = null;

		/**
		 * Stores the statement of the updating process (of counter and so on) of a "for" loop.
		 */
		private String updatePointStatement = null;

		/**
		 * When it should put a label after instructions of latter statements, store the label into this field.
		 * For example, landing point labels of jump instructions of: "if" statements, short circuit evaluations, and so on.
		 * This field may store two labels for handling the combination of "if" and "else" statements (so called "if-else").
		 */
		private ArrayList<String> endPointLabelList = null;

		/**
		 * When it should put an statement (e.g.: "return" statement in a function) at the end of a block,
		 * store the statement into this field.
		 */
		private String endPointStatement = null;

		/**
		 * Stores a register of the value of the condition expression of the last-traversed "if" statement, 
		 * for using it in code generation of "else" statement.
		 */
		private String lastIfConditionRegister = null;

		/** Stores the beginning point label of the last-traversed loop. */
		private String lastLoopBeginPointLabel = null;

		/** Stores the updating point label of the last-traversed loop. */
		private String lastLoopUpdatePointLabel = null;
		
		/** Stores the end point label of the last-traversed loop. */
		private String lastLoopEndPointLabel = null;

		/** Stores the beginning point label of the last-traversed function. */
		private String lastFunctionLabel = null;

		/** Stores the beginning point label of the outer loop, used for generating code of "continue" statements. */
		private String outerLoopBeginPointLabel = null;

		/** Stores the beginning point label of the outer loop, used for generating code of "continue" statements. */
		private String outerLoopUpdatePointLabel = null;

		/** Stores the beginning point label of the outer loop, used for generating code of "break" statements. */
		private String outerLoopEndPointLabel = null;

		/** Stores whether the next block is a loop or not. */
		private boolean isNextBlockLoop = false;

		/** Stores nodes of statements in the currently traversed block. */
		private AstNode[] statementNodes = null;

		/** Stores the total number of statements in the currently traversed block. */
		private int statementLength = -1;

		/** Stores index of the currently traversed statement in the block. */
		private int statementIndex = -1;

		/** Stores generated code of a statement, by "generateStatementCode" method (see its description). */
		private String lastStatementCode = null;

		public StatementTrackingContext() {
			this.endPointLabelList = new ArrayList<String>();
		}

		public StatementTrackingContext clone() {
			StatementTrackingContext clone = new StatementTrackingContext();

			clone.beginPointLabel = this.beginPointLabel;
			clone.updatePointLabel = this.updatePointLabel;
			clone.updatePointStatement = this.updatePointStatement;
			clone.endPointStatement = this.endPointStatement;
			clone.endPointLabelList = new ArrayList<String>();
			for (String endPointLabel: this.endPointLabelList) {
				clone.endPointLabelList.add(endPointLabel);
			}

			clone.lastIfConditionRegister = this.lastIfConditionRegister;
			clone.lastLoopBeginPointLabel = this.lastLoopBeginPointLabel;
			clone.lastLoopUpdatePointLabel = this.lastLoopUpdatePointLabel;
			clone.lastLoopEndPointLabel = this.lastLoopEndPointLabel;
			clone.lastFunctionLabel = this.lastFunctionLabel;
			clone.outerLoopBeginPointLabel = this.outerLoopBeginPointLabel;
			clone.outerLoopUpdatePointLabel = this.outerLoopUpdatePointLabel;
			clone.outerLoopEndPointLabel = this.outerLoopEndPointLabel;

			clone.isNextBlockLoop = this.isNextBlockLoop;

			clone.statementNodes = this.statementNodes;
			clone.statementIndex = this.statementIndex;
			clone.statementLength = this.statementLength;
			clone.lastStatementCode = this.lastStatementCode;

			return clone;
		}

		public String getBeginPointLabel() {
			return this.beginPointLabel;
		}
		public void setBeginPointLabel(String beginPointLabel) {
			this.beginPointLabel = beginPointLabel;
		}
		public boolean hasBeginPointLabel() {
			return this.beginPointLabel != null;
		}
		public void clearBeginPointLabel() {
			this.beginPointLabel = null;
		}

		public String getUpdatePointLabel() {
			return this.updatePointLabel;
		}
		public void setUpdatePointLabel(String updatePointLabel) {
			this.updatePointLabel = updatePointLabel;
		}
		public boolean hasUpdatePointLabel() {
			return this.updatePointLabel != null;
		}
		public void clearUpdatePointLabel() {
			this.updatePointLabel = null;
		}

		public String[] getEndPointLabels() {
			return this.endPointLabelList.toArray(new String[0]);
		}
		public void addEndPointLabel(String endPointLabel) {
			this.endPointLabelList.add(endPointLabel);
		}
		public boolean hasEndPointLabel() {
			return !this.endPointLabelList.isEmpty();
		}
		public void clearEndPointLabel() {
			this.endPointLabelList.clear();
		}

		public boolean hasEndPointStatement() {
			return this.endPointStatement != null;
		}
		public void setEndPointStatement(String endPointStatement) {
			this.endPointStatement = endPointStatement;
		}
		public String getEndPointStatement() {
			return this.endPointStatement;
		}
		public void clearEndPointStatement() {
			this.endPointStatement = null;
		}

		public String getUpdatePointStatement() {
			return this.updatePointStatement;
		}
		public void setUpdatePointStatement(String updatePointStatement) {
			this.updatePointStatement = updatePointStatement;
		}
		public boolean hasUpdatePointStatement() {
			return this.updatePointStatement != null;
		}
		public void clearUpdatePointStatement() {
			this.updatePointStatement = null;
		}

		public boolean hasLastIfConditionRegister() {
			return this.lastIfConditionRegister != null;
		}
		public String getLastIfConditionRegister() {
			return this.lastIfConditionRegister;
		}
		public void setLastIfConditionValue(String lastIfConditionValue) {
			this.lastIfConditionRegister = lastIfConditionValue;
		}

		public String getLastLoopBeginPointLabel() {
			return this.lastLoopBeginPointLabel;
		}

		public void setLastLoopBeginPointLabel(String lastLoopBeginPointLabel) {
			this.lastLoopBeginPointLabel = lastLoopBeginPointLabel;
		}

		public boolean hasLastLoopUpdatePointLabel() {
			return this.lastLoopUpdatePointLabel != null;
		}

		public String getLastLoopUpdatePointLabel() {
			return this.lastLoopUpdatePointLabel;
		}

		public void setLastLoopUpdatePointLabel(String lastLoopUpdatePointLabel) {
			this.lastLoopUpdatePointLabel = lastLoopUpdatePointLabel;
		}

		public String getLastLoopEndPointLabel() {
			return lastLoopEndPointLabel;
		}

		public void setLastLoopEndPointLabel(String lastLoopEndPointLabel) {
			this.lastLoopEndPointLabel = lastLoopEndPointLabel;
		}


		public String getOuterLoopBeginPointLabel() {
			return this.outerLoopBeginPointLabel;
		}

		public void setOuterLoopBeginPointLabel(String outerLoopBeginPointLabel) {
			this.outerLoopBeginPointLabel = outerLoopBeginPointLabel;
		}

		public String getOuterLoopEndPointLabel() {
			return this.outerLoopEndPointLabel;
		}

		public void setOuterLoopEndPointLabel(String outerLoopEndPointLabel) {
			this.outerLoopEndPointLabel = outerLoopEndPointLabel;
		}

		public String getOuterLoopUpdatePointLabel() {
			return this.outerLoopUpdatePointLabel;
		}

		public void setOuterLoopUpdatePointLabel(String outerLoopUpdatePointLabel) {
			this.outerLoopUpdatePointLabel = outerLoopUpdatePointLabel;
		}


		public String getLastFunctionLabel() {
			return this.lastFunctionLabel;
		}

		public void setLastFunctionLabel(String lastFunctionLabel) {
			this.lastFunctionLabel = lastFunctionLabel;
		}


		public void setNextBlockLoop(boolean isLoop) {
			this.isNextBlockLoop = isLoop;
		}

		public boolean isNextBlockLoop() {
			return this.isNextBlockLoop;
		}


		public AstNode[] getStatementNodes() {
			return this.statementNodes;
		}

		public void setStatementNodes(AstNode[] statementNodes) {
			this.statementNodes = statementNodes;
		}

		public int getStatementLength() {
			return this.statementLength;
		}

		public void setStatementLength(int statementLength) {
			this.statementLength = statementLength;
		}

		public int getStatementIndex() {
			return this.statementIndex;
		}

		public void setStatementIndex(int statementIndex) {
			this.statementIndex = statementIndex;
		}
		public String getLastStatementCode() {
			return this.lastStatementCode;
		}
		public void setLastStatementCode(String lastStatementCode) {
			this.lastStatementCode = lastStatementCode;
		}
	}


	/**
	 * Generates intermediate code written in the VRIL from the semantic-analyzed AST.
	 * 
	 * Intermediate code generated by this method can be executed on the VM
	 * ({@link org.vcssl.nano.vm.VirtualMachine}) of the Vnano.
	 *
	 * @param inputAst The root node of the sematic-analyzed AST.
	 * @return Intermediate code written in the VRIL.
	 */
	public String generate(AstNode inputAst) {

		// The buffer to which generated code will be stored/appended.
		StringBuilder codeBuilder = new StringBuilder();

		// We should not modify the passed AST instance, so clone it at first.
		AstNode cloneAst = inputAst.clone();

		// Assign values in VRIL code to AST nodes, 
		// e.g.; register names, immediate values, labels, etc.
		this.assignAssemblyValues(cloneAst);
		this.assignLabels(cloneAst);

		// Generate directives declaring language information (version, language name, etc).
		codeBuilder.append( this.generateLanguageInformationDirectives() );

		// Generate function directives, 
		// for declaring signatures of (both internal/external) functions in VRIL code.
		codeBuilder.append( generateFunctionIdentifierDirectives(cloneAst) );

		// Generate global variable directives, 
		// for declaring names of external variables in VRIL code.
		// (For local variables, we will generate their declaration directives 
		//  just before their registers are allocated, for readability.)
		codeBuilder.append( generateGlobalIdentifierDirectives(cloneAst) );

		// Traverse all nodes in the AST, and generate their code.
		codeBuilder.append( this.trackAllStatements(cloneAst) );

		// Generate finalization code, for returning a result value of the script 
		// to the caller of "evaluateScript" method of VanoEngine.
		codeBuilder.append( generateFinalizationCode(cloneAst) );

		// Get the generated code from the buffer.
		String code = codeBuilder.toString();

		// Arrange the form of code a little for readability.
		String realignedCode = this.realign(code);
		return realignedCode;
	}


	/**
	 * Assigns the value in VRIL code, for each node in the AST
	 * (operator node, literal node, and so on).
	 * 
	 * For example, immediate value will be assigned to literal nodes, 
	 * and identifier in VRIL notation will be assigned to variable nodes.
	 * Also, register names for storing operation results are assigned 
	 * to operator nodes.
	 * 
	 * In each AST node, the assigned value will be stored as the value of 
	 * {@link AttributeKey#ASSEMBLY_VALUE ASSEMBLY_VALUE} attribute.
	 * (So this method modify the state of the passed argument.)
	 *
	 * @param inputAst The AST you want to assign assembly values to.
	 */
	private void assignAssemblyValues(AstNode inputAst) {

		AstNode currentNode = inputAst.getPostorderDftFirstNode();
		while (currentNode != inputAst) {

			AstNode.Type nodeType = currentNode.getType();

			// Variable declaration nodes and leaf (variable/function identifier and literal) nodes:
			// assign identifiers or immediate values in VRIL notations.
			if (nodeType == AstNode.Type.VARIABLE || nodeType == AstNode.Type.LEAF) {

				boolean isVariable = nodeType == AstNode.Type.VARIABLE;
				boolean isLeaf = nodeType == AstNode.Type.LEAF;
				String leafType = currentNode.getAttribute(AttributeKey.LEAF_TYPE);

				// Function identifiers
				if(isLeaf && leafType == AttributeValue.FUNCTION_IDENTIFIER) {
					AstNode callOperatorNode = currentNode.getParentNode();
					String calleeSignature = callOperatorNode.getAttribute(AttributeKey.CALLEE_SIGNATURE);
					String assemblyValue = AssemblyWord.IDENTIFIER_OPERAND_PREFIX + calleeSignature;
					//String assemblyValue = IdentifierSyntax.getAssemblyIdentifierOfCalleeFunctionOf(callOperatorNode);
					currentNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, assemblyValue);

				// Variable declarations or variable identifiers
				} else if(isVariable || (isLeaf && leafType == AttributeValue.VARIABLE_IDENTIFIER) ) {
					String identifier = currentNode.getAttribute(AttributeKey.IDENTIFIER_VALUE);
					String assemblyValue = IdentifierSyntax.getAssemblyIdentifierOf(identifier);
					if (currentNode.hasAttribute(AttributeKey.IDENTIFIER_SERIAL_NUMBER)) {
						assemblyValue += AssemblyWord.IDENTIFIER_SERIAL_NUMBER_SEPARATOR
						              + currentNode.getAttribute(AttributeKey.IDENTIFIER_SERIAL_NUMBER);
					}
					currentNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, assemblyValue);

				// Literals
				} else if(isLeaf && leafType == AttributeValue.LITERAL) {

					String dataTypeName = currentNode.getAttribute(AttributeKey.DATA_TYPE);
					String literal = currentNode.getAttribute(AttributeKey.LITERAL_VALUE);
					String assemblyValue = AssemblyWord.getImmediateValueOf(dataTypeName, literal);
					currentNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, assemblyValue);

				} else {
					throw new VnanoFatalException("Unknown leaf type: " + leafType);
				}
			}

			// Operator nodes:
			if (currentNode.getType() == AstNode.Type.OPERATOR) {

				String execType = currentNode.getAttribute(AttributeKey.OPERATOR_EXECUTOR);
				String syntaxType = currentNode.getAttribute(AttributeKey.OPERATOR_SYNTAX);
				String operatorSymbol = currentNode.getAttribute(AttributeKey.OPERATOR_SYMBOL);
				switch (execType) {

					// Assignment operators: its value is the same as the left-hand-side node.
					case AttributeValue.ASSIGNMENT : {
						String value = currentNode.getChildNodes()[0].getAttribute(AttributeKey.ASSEMBLY_VALUE);
						currentNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, value);
						break;
					}

					// Arithmetic compound assignment operators (++, --, +=, -=, *=, /=, %=):
					case AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT : {
						if (operatorSymbol.equals(ScriptWord.INCREMENT) || operatorSymbol.equals(ScriptWord.DECREMENT)) {

							// Prefix increment/decrement: its value is the same as the operand variable.
							if(syntaxType.equals(AttributeValue.PREFIX)) {
								String value = currentNode.getChildNodes()[0].getAttribute(AttributeKey.ASSEMBLY_VALUE);
								currentNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, value);
								break;

							// Postfix increment/decrement: 
							// its value must be the value of the operand variable before operation is performed, 
							// so assign a new register to it, for storing the value before operation.
							} else {
								String register = AssemblyWord.REGISTER_OPERAND_PREFIX + Integer.toString(registerCounter);
								this.registerCounter++;
								currentNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, register);

								// Mark that memory allocation for this register is required when generate code of this node.
								currentNode.setAttribute(AttributeKey.NEW_REGISTER, register);
								break;
							}

						// Compound assignment operators excluding increments/decrements: its value is the same as the left-hand-side node.
						} else {
							String value = currentNode.getChildNodes()[0].getAttribute(AttributeKey.ASSEMBLY_VALUE);
							currentNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, value);
							break;
						}
					}

					// Non-assignment operators: assign a new register for storing the result of operations.
					default : {
						String register = AssemblyWord.REGISTER_OPERAND_PREFIX + Integer.toString(registerCounter);
						this.registerCounter++;
						currentNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, register);

						// Mark that memory allocation for this register is required when generate code of this node.
						currentNode.setAttribute(AttributeKey.NEW_REGISTER, register);
						break;
					}
				}
			}

			// Expression node: its value is the root operator node of the partial AST of the expression, 
			// where the root operator node is added to the expression node as a child.
			if (currentNode.getType() == AstNode.Type.EXPRESSION) {
				String value = currentNode.getChildNodes()[0].getAttribute(AttributeKey.ASSEMBLY_VALUE);
				currentNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, value);
			}

			currentNode = currentNode.getPostorderDftNextNode();
		}
	}



	/**
	 * Assigns labels in VRIL code, for each node in the AST
	 * ("if" statement node, "while" statement node, and so on).
	 * 
	 * In each AST node, the assigned labels will be stored as the value of 
	 * laben-related attributes.
	 * (So this method modify the state of the passed argument.)
	 *
	 * @param inputAst The AST you want to assign assembly labels to.
	 */
	private void assignLabels(AstNode inputAst) {

		AstNode currentNode = inputAst.getPostorderDftFirstNode();
		while (currentNode != inputAst) {

			// Control statement nodes:
			if (currentNode.getType() == AstNode.Type.IF) {
				currentNode.setAttribute(AttributeKey.END_LABEL, this.generateLabelOperandCode());
			}
			if (currentNode.getType() == AstNode.Type.ELSE) {
				currentNode.setAttribute(AttributeKey.END_LABEL, this.generateLabelOperandCode());
			}
			if (currentNode.getType() == AstNode.Type.FOR) {
				currentNode.setAttribute(AttributeKey.BEGIN_LABEL, this.generateLabelOperandCode());
				currentNode.setAttribute(AttributeKey.UPDATE_LABEL, this.generateLabelOperandCode());
				currentNode.setAttribute(AttributeKey.END_LABEL, this.generateLabelOperandCode());
			}
			if (currentNode.getType() == AstNode.Type.WHILE) {
				currentNode.setAttribute(AttributeKey.BEGIN_LABEL, this.generateLabelOperandCode());
				currentNode.setAttribute(AttributeKey.END_LABEL, this.generateLabelOperandCode());
			}
			if (currentNode.getType() == AstNode.Type.FUNCTION) {
				currentNode.setAttribute(AttributeKey.END_LABEL, this.generateLabelOperandCode());
			}

			// Operator nodes:
			if (currentNode.getType() == AstNode.Type.OPERATOR) {
				String symbol = currentNode.getAttribute(AttributeKey.OPERATOR_SYMBOL);
				
				// "&&" and "||" operators skip the evaluation of the right operand,
				// when the result can be determined only by the left operand (so-called "short circuit evaluation").
				// So they require labels for skipping it by JMP/JMPN instructions.
				if (symbol.equals(ScriptWord.SHORT_CIRCUIT_AND) || symbol.equals(ScriptWord.SHORT_CIRCUIT_OR)) {
					currentNode.setAttribute(AttributeKey.END_LABEL, this.generateLabelOperandCode());
				}
			}

			currentNode = currentNode.getPostorderDftNextNode();
		}
	}


	/**
	 * Generates VRIL code corresponding with the specified AST.
	 * 
	 * This method traverses all nodes in the AST, 
	 * and generates code for each statement node, 
	 * and connect them in the order of execution.
	 *
	 * @param inputAst The AST you want to generate its code.
	 * @return The generated VRIL code.
	 */
	private String trackAllStatements(AstNode inputAst) {

		// The buffer storing generated code.
		StringBuilder codeBuilder = new StringBuilder();

		AstNode[] statementNodes = inputAst.getChildNodes();
		int statementLength = statementNodes.length;
		int statementIndex = 0;

		StatementTrackingContext context = new StatementTrackingContext();
		Deque<StatementTrackingContext> contextStack = new ArrayDeque<StatementTrackingContext>();

		while (statementIndex < statementLength) {

			AstNode currentNode = statementNodes[ statementIndex ];
			AstNode.Type nodeType = currentNode.getType();

			// Generate a meta directive to comment that what generated code is/are doing, 
			// excluding block statement nodes.
			if (!nodeType.equals(AstNode.Type.BLOCK)) {
				String metaDirective = this.generateMetaDirectiveCode(currentNode);
				codeBuilder.append(metaDirective);
			}

			// Generate VRIL code for each statement node, in the current block:
			switch (nodeType) {

				// When the traversal-flow enters into a block:
				case BLOCK : {

					// Store the current context to the stack, and create a new context.
					context.setStatementIndex(statementIndex);
					context.setStatementLength(statementLength);
					context.setStatementNodes(statementNodes);
					contextStack.push(context);
					context = new StatementTrackingContext();

					// The context of the outer block, pushed to the stack just now.
					StatementTrackingContext outerBlockContext = contextStack.peek();

					// Copy some values in the context, 
					// which require to be "inherited" from the context of the outer block.
					context.setLastLoopBeginPointLabel(outerBlockContext.getLastLoopBeginPointLabel());
					context.setLastLoopUpdatePointLabel(outerBlockContext.getLastLoopUpdatePointLabel());
					context.setLastLoopEndPointLabel(outerBlockContext.getLastLoopEndPointLabel());
					context.setOuterLoopBeginPointLabel(outerBlockContext.getOuterLoopBeginPointLabel());
					context.setOuterLoopUpdatePointLabel(outerBlockContext.getOuterLoopUpdatePointLabel());
					context.setOuterLoopEndPointLabel(outerBlockContext.getOuterLoopEndPointLabel());
					context.setLastFunctionLabel(outerBlockContext.getLastFunctionLabel());

					// If the entering block belongs to a loop, register the loop as "outer loop" of the (new) context of the entering block.
					// It will be used in code generation for "break", "continue", and some other statements in the entering block.
					if (outerBlockContext.isNextBlockLoop()) {
						
						// A block statement node does not have information of the loop (beginning label, end label, etc.). 
						// The loop statement ("for" or "while") node at just before the block statement node has such information.
						// And you can get it by using "getLastLoop*" methods of the outerBlockContext.
						// So get them from outerBlockContext, and set them as information of "outer loop", to the current context.
						context.setOuterLoopBeginPointLabel(outerBlockContext.getLastLoopBeginPointLabel());
						context.setOuterLoopUpdatePointLabel(outerBlockContext.getLastLoopUpdatePointLabel());
						context.setOuterLoopEndPointLabel(outerBlockContext.getLastLoopEndPointLabel());
					}

					// Extract statements in the entering block, to be traversed.
					statementNodes = currentNode.getChildNodes();
					statementLength = statementNodes.length;
					statementIndex = 0;
					break;
				}

				// When walking on statement nodes in a block:
				// generates code of the statement, and push it at the end of the buffer.
				case VARIABLE :
				case FUNCTION :
				case IF :
				case ELSE :
				case WHILE :
				case FOR :
				case BREAK :
				case CONTINUE :
				case RETURN :
				case EXPRESSION : {
					context = this.generateStatementCode(currentNode, context);
					codeBuilder.append( context.getLastStatementCode() );
					statementIndex++;
					break;
				}

				// Non-statement nodes (operators, literals, and so on):
				// They will be handled in code generation of "EXPRESSION" statement node, 
				// so we should not do nothing here.
				default : {
					statementIndex++;
					break;
				}
			}

			// When code generation of the last statement in the current block has completed, return to the outer block.
			// Note that, we should use "while" instead of "if", 
			// because of multiple (nested) blocks may end at the same point in the AST.
			while (statementIndex == statementLength) {

				// If no context remain in the stack, 
				// it means that the code generations of all statements in the root block have completed, 
				// so finish the code generation.
				if (contextStack.isEmpty()) {
					break;
				}

				// Otherwise, we should return to the outer block, so recover the context by popping it from the stack.
				context = contextStack.pop();
				statementIndex = context.getStatementIndex();
				statementLength = context.getStatementLength();
				statementNodes = context.getStatementNodes();
				statementIndex++;

				// Put some code at the end of the block, if necessary.

				// Some labels used by code of "for" statement:
				// (It must be before of: code to jump to the top)
				if (context.hasUpdatePointLabel()) {
					codeBuilder.append(this.generateLabelDirectiveCode(context.getUpdatePointLabel()));
					context.clearUpdatePointLabel();
				}
				if (context.hasUpdatePointStatement()) {
					codeBuilder.append(context.getUpdatePointStatement());
					context.clearUpdatePointStatement();
				}

				// Jump instructions for returning to the top of a loop:
				if (context.hasBeginPointLabel()) {
					String jumpCode = this.generateInstruction(
							OperationCode.JMP.name(), DataTypeName.BOOL,
							PLACE_HOLDER, context.getBeginPointLabel(), IMMEDIATE_TRUE
					);
					codeBuilder.append(jumpCode);
					context.clearBeginPointLabel();
				}

				// Default return instructions of functions, and so on:
				if (context.hasEndPointStatement()) {
					codeBuilder.append(context.getEndPointStatement());
					context.clearEndPointStatement();
				}

				// Landing point labels used by "break" statements, function-skipping labels, and so on.
				// Where "function-skipping labels" are the labels at ends of code of function declarations, 
				// for skipping execution of them when whole code of a script is executed from the top to the bottom.
				// (For the same purpose, JMP instructions will be put at beginnings of function declarations.)
				// Code of function declarations will be processed only when the flow will jumps into them by CALL instructions.
				if (context.hasEndPointLabel()) {
					String[] endPointLabels = context.getEndPointLabels();
					for (String endPointLabel: endPointLabels) {
						codeBuilder.append(this.generateLabelDirectiveCode(endPointLabel));
					}
					context.clearEndPointLabel();
				}
			}
		}

		return codeBuilder.toString();
	}


	/**
	 * Generates VRIL code corresponding with the specified statement node, and updates the context.
	 * 
	 * Code generation of some statements reuires to update the context, so this method returns the updated context.
	 * In the returned context, the generated code is stored. 
	 * You can get it by {@link StatementTrackingContext#getLastStatementCode} method.
	 *
	 * @param node The AST node of a statement.
	 * @param context The current context.
	 * @return The context updated by the code generation of this method.
	 */
	private StatementTrackingContext generateStatementCode(AstNode node, StatementTrackingContext context) {
		context = context.clone();

		AstNode.Type nodeType = node.getType();

		// Generate code, depends on the kind of the specified statement:
		String code = null;
		switch (nodeType) {

			// Variable declaration statements:
			case VARIABLE : {
				code = this.generateVariableDeclarationStatementCode(node);
				break;
			}

			// Function declaration statements:
			case FUNCTION : {

				// Generate a label at the top of the function.
				String signature = IdentifierSyntax.getSignatureOf(node);
				String functionLabelName = Character.toString(AssemblyWord.LABEL_OPERAND_PREFIX)
					+ Character.toString(AssemblyWord.IDENTIFIER_OPERAND_PREFIX) + signature;

				// Generate internal code of the function.
				code = this.generateFunctionDeclarationStatementCode(node, functionLabelName);

				// Prepare a string-type immediate value used as an operand of ENDFUNC instruction.
				String functionNameOperand = AssemblyWord.IMMEDIATE_OPERAND_PREFIX
						+ DataTypeName.STRING + AssemblyWord.VALUE_SEPARATOR
						+ LiteralSyntax.STRING_LITERAL_QUOT + signature + LiteralSyntax.STRING_LITERAL_QUOT;

				// Generate ENDFUN instruction at the end of internal code of the function.
				// In addition, also generate RET instruction for void-type functions.
				String endPointStatement = this.generateInstruction(OperationCode.ENDFUN.name(), DataTypeName.STRING, functionNameOperand);
				if (node.getDataTypeName().equals(DataTypeName.VOID)) {
					endPointStatement = this.generateInstruction(
						OperationCode.RET.name(), DataTypeName.VOID, PLACE_HOLDER, functionLabelName
					) + endPointStatement;
				}

				context.setEndPointStatement(endPointStatement);
				context.setNextBlockLoop(false);
				context.setLastFunctionLabel(functionLabelName);
				context.addEndPointLabel(node.getAttribute(AttributeKey.END_LABEL));
				break;
			}

			// "if" statements:
			case IF : {
				context.addEndPointLabel(node.getAttribute(AttributeKey.END_LABEL));
				String lastIfConditionRegister = null;
				boolean lastIfConditionRegisterAllocRequired = false;
				if( context.hasLastIfConditionRegister() ) {
					lastIfConditionRegister = context.getLastIfConditionRegister();
				} else {
					lastIfConditionRegister = this.generateRegisterOperandCode();
					lastIfConditionRegisterAllocRequired = true;
				}
				context.setLastIfConditionValue(lastIfConditionRegister);
				context.setNextBlockLoop(false);
				code = this.generateIfStatementCode(node, lastIfConditionRegister, lastIfConditionRegisterAllocRequired);
				break;
			}

			// "else" statements:
			case ELSE : {
				code = this.generateElseStatementCode(node, context.getLastIfConditionRegister());
				context.addEndPointLabel(node.getAttribute(AttributeKey.END_LABEL));
				context.setNextBlockLoop(false);
				//context.clearLastIfConditionValue(); // "else" may locate after "else if", so don't do this.
				break;
			}

			// "while" statements:
			case WHILE : {
				code = this.generateWhileStatementCode(node);
				context.setBeginPointLabel(node.getAttribute(AttributeKey.BEGIN_LABEL));
				context.addEndPointLabel(node.getAttribute(AttributeKey.END_LABEL));
				context.setLastLoopBeginPointLabel( context.getBeginPointLabel() );
				context.setLastLoopUpdatePointLabel( context.getUpdatePointLabel() );
				context.setLastLoopEndPointLabel( context.getEndPointLabels()[0] );
				context.setNextBlockLoop(true);
				break;
			}

			// "for" statements:
			case FOR : {
				code = this.generateForStatementCode(node);
				context.setBeginPointLabel(node.getAttribute(AttributeKey.BEGIN_LABEL));
				context.setUpdatePointLabel(node.getAttribute(AttributeKey.UPDATE_LABEL));
				context.addEndPointLabel(node.getAttribute(AttributeKey.END_LABEL));

				// Statement to update a conter (3rd statement of for(...;...;...) ):
				// Usually it is an expression statement, so generate code for evaluation of it.
				// However, it may be an empty statement, and we should do nothing for it.
				if (node.getChildNodes()[2].getType() == AstNode.Type.EXPRESSION) { // 式文の場合は評価コード生成
					context.setUpdatePointStatement(this.generateExpressionCode(node.getChildNodes()[2]));
				}

				context.setLastLoopBeginPointLabel( context.getBeginPointLabel() );
				context.setLastLoopUpdatePointLabel( context.getUpdatePointLabel() );
				context.setLastLoopEndPointLabel( context.getEndPointLabels()[0] );
				context.setNextBlockLoop(true);
				break;
			}

			// "break" statement: generate a jump instruction to the end of the outer loop.
			case BREAK : {
				code = this.generateInstruction(
					OperationCode.JMP.name(), DataTypeName.BOOL, PLACE_HOLDER, context.getOuterLoopEndPointLabel(), IMMEDIATE_TRUE
				);
				break;
			}

			// "continue" statement: generate a jump instruction to the top (or the statement to update a counter) of the outer loop.
			case CONTINUE : {

				// Prepare the landing point label. By default, use the label at the top of the outerloop (for "while" loops).
				String continueJumpPointLabel = context.getOuterLoopBeginPointLabel();

				// If the label at the statement to update a counter of the outer loop exists, use it (for "for" loops).
				if (context.hasLastLoopUpdatePointLabel()) {
					continueJumpPointLabel = context.getOuterLoopUpdatePointLabel();
				}

				// Generate code jumping to the above label.
				code = this.generateInstruction(
					OperationCode.JMP.name(), DataTypeName.BOOL, PLACE_HOLDER, continueJumpPointLabel, IMMEDIATE_TRUE
				);
				break;
			}

			// "return" statement:
			case RETURN : {
				code = this.generateReturnStatementCode(node, context.getLastFunctionLabel());
				break;
			}

			// Expression statements:
			case EXPRESSION : {
				code = this.generateExpressionCode(node);
				break;
			}

			// Other kinds of (non-statement) nodes:
			// They will be handled in code generation of "EXPRESSION" statement node, so we should not do nothing here.
			default : {
				break;
			}
		}
		context.setLastStatementCode(code);

		return context;
	}


	/**
	 * Generates code of a variable declaration node
	 *
	 * @param node The AST node of variable declaration statement.
	 * @return The generated code.
	 */
	private String generateVariableDeclarationStatementCode (AstNode node) {

		StringBuilder codeBuilder = new StringBuilder();
		String variableOperand = node.getAttribute(AttributeKey.ASSEMBLY_VALUE);
		String variableType = node.getDataTypeName();

		// Generate the identifier directive.
		codeBuilder.append(AssemblyWord.LOCAL_VARIABLE_DIRECTIVE);
		codeBuilder.append(AssemblyWord.WORD_SEPARATOR);
		codeBuilder.append(variableOperand);
		codeBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);
		codeBuilder.append(AssemblyWord.LINE_SEPARATOR);

		// Firstly, generate code of all child nodes, 
		// and store array rank/lengths of the variable to be declared.
		int rank = RANK_OF_SCALAR;
		String[] arrayLengthValues = null;
		if (node.hasChildNodes(AstNode.Type.LENGTHS)) {
			AstNode[] lengthExprNodes
				= node.getChildNodes(AstNode.Type.LENGTHS)[0].getChildNodes(AstNode.Type.EXPRESSION);

			rank = lengthExprNodes.length;
			arrayLengthValues = new String[rank];
			for (int dim=0; dim<rank; dim++) {
				arrayLengthValues[dim] = lengthExprNodes[dim].getAttribute(AttributeKey.ASSEMBLY_VALUE);
				codeBuilder.append( this.generateExpressionCode(lengthExprNodes[dim]) );
			}
		}

		// If the variable is a scalar, generate an 1-operand ALLOC instruction.
		if (rank == RANK_OF_SCALAR) {
			codeBuilder.append(
				this.generateInstruction(OperationCode.ALLOC.name(), variableType, variableOperand)
			);

		// If the variable is an array, generate a multi-operand ALLOC instruction, 
		// and specify array lengths to its operands.
		} else {

			String[] allocOperands = new String[ arrayLengthValues.length + 1 ];
			allocOperands[0] = variableOperand;
			System.arraycopy(arrayLengthValues, 0, allocOperands, 1, arrayLengthValues.length);
			codeBuilder.append(
				this.generateInstruction(OperationCode.ALLOC.name(), variableType, allocOperands)
			);
		}

		// If the statement having an expression for initialize the value of the declared variable,
		// generate code of it.
		AstNode[] initExprNodes = node.getChildNodes(AstNode.Type.EXPRESSION);
		if (initExprNodes.length == 1) {
			codeBuilder.append( this.generateExpressionCode(initExprNodes[0]) );

		// Otherwise, generate code for initializing the variable by a default value.
		} else {
			String defaultValueOperand = this.generateDefaultValueImmediateOperandCode(variableType);

			// Use MOV for a scalar variable.
			if (rank == RANK_OF_SCALAR) {
				codeBuilder.append(
					this.generateInstruction(OperationCode.MOV.name(), variableType, variableOperand, defaultValueOperand)
				);

			// Use FILL for an array variable.
			} else {
				codeBuilder.append(
					this.generateInstruction(OperationCode.FILL.name(), variableType, variableOperand, defaultValueOperand)
				);
			}
		}

		return codeBuilder.toString();
	}


	/**
	 * Generates code of a function declaration node
	 *
	 * @param node The AST node of the function declaration statement.
	 * @param functionLabelName The label to be put at the top of the function's code.
	 * @return The generated code.
	 */
	private String generateFunctionDeclarationStatementCode (AstNode node, String functionLabelName) {

		// Important Note:
		// 
		// At the top of function, a JMP instruction (to the end of the function) will be put, 
		// for skipping execution of the function's code 
		// when whole code of a script is executed from the top to the bottom.
		// 
		// At the next of the JMP instruction, a label specified as the argument "functionLabelName" will be put.
		// When the function is called, the processing flow jumps to the label by CALL instruction.
		// 
		// At end of function's code, a label (function skipping label) will be put, 
		// for skipping execution of the function's code, 
		// when whole code of a script is executed from the top to the bottom.
		// (For the same purpose, JMP instructions will be put at beginnings of function declarations.)
		// Code of function declarations will be processed only when the flow will jumps into them by CALL instructions.

		// The buffer storing generated code.
		StringBuilder codeBuilder = new StringBuilder();

		// Generate a JMP instruction for skipping function's code 
		// when whole code of a script is executed from the top to the bottom (see the above note).
		String skipLabel = node.getAttribute(AttributeKey.END_LABEL);
		codeBuilder.append(
			this.generateInstruction(OperationCode.JMP.name(), DataTypeName.BOOL, PLACE_HOLDER, skipLabel, IMMEDIATE_TRUE)
		);

		// Generate the specified label at the top of the function's code.
		codeBuilder.append( this.generateLabelDirectiveCode(functionLabelName) );

		// Generate code transferring values of actual arguments on the stack to parameter variables, 
		// where nodes of parameter variables are linked to the function declaration statement as child nodes.
		// Note: the stack is LIFO, so the order of actual arguments on the stack is reversed.
		AstNode[] argNodes = node.getChildNodes();
		int argLength = argNodes.length;
		for (int argIndex=argLength-1; 0 <= argIndex; argIndex--) {

			AstNode argNode = argNodes[argIndex];

			String argIdentifier = argNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);
			String argDataType = argNode.getAttribute(AttributeKey.DATA_TYPE);
			int argRank = argNode.getRank();

			// Prepare operands of an ALLOCT instruction (see comments at lines generating them):
			// The first operand is the identifier of the parameter variable.
			// After of it, Put N zeros as operands, where N is the array rank.
			String[] alloctOperands = new String[argRank+1];
			Arrays.fill(alloctOperands, this.generateImmediateOperandCode(DataTypeName.DEFAULT_INT, "0") );
			alloctOperands[0] = argIdentifier;

			// Generate local variable directive of the parameter variable.
			// 引数のローカル変数ディレクティブを生成
			codeBuilder.append(AssemblyWord.LOCAL_VARIABLE_DIRECTIVE);
			codeBuilder.append(AssemblyWord.WORD_SEPARATOR);
			codeBuilder.append(argIdentifier);
			codeBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);
			codeBuilder.append(AssemblyWord.LINE_SEPARATOR);

			// For call-by-reference: it doesn't requier memory allocation.
			if (argNode.hasAttribute(AttributeKey.MODIFIER)
					&& argNode.getAttribute(AttributeKey.MODIFIER).contains(ScriptWord.REF_MODIFIER) ) {

				// It is difficult for the VM to statically analyze the type/rank of a value popped from the stack, 
				// because any type/rank value can be stored in the stack. 
				// On the other hand, the compiler knows the type/rank of such value.
				// So, for optimization and readability, generate ALLOCT instruction 
				// which only declares type and rank of the variable without allocating memory actually.
				codeBuilder.append(
					this.generateInstruction(OperationCode.ALLOCT.name(), argDataType, alloctOperands)
				);

				// Generate REFPOP instruction, 
				// for setting data-reference of the parameter variable to data popped from the stack.
				// (It is a copy operation of "reference", not data, so it does not require the memory allocation.)
				codeBuilder.append(
					this.generateInstruction(OperationCode.REFPOP.name(), argDataType, argIdentifier)
				);

			// For call-by-value: it requires memory allocation.
			} else {

				// Generate ALLOCT instruction, for the same reason as code of "call-by-reference".
				codeBuilder.append(
					this.generateInstruction(OperationCode.ALLOCT.name(), argDataType, alloctOperands)
				);

				// Allocate memory for storing data at the top of the stack, by using ALLOCP instruction.
				codeBuilder.append(
					this.generateInstruction(OperationCode.ALLOCP.name(), argDataType, argIdentifier)
				);

				// Generate MOFPOP instruction, 
				// for copying data from the top of the stack to the parameter variable.
				codeBuilder.append(
					this.generateInstruction(OperationCode.MOVPOP.name(), argDataType, argIdentifier)
				);
			}
		}

		// Prepare the signature of the function as the immediate value.
		String functionSignatureOperand = AssemblyWord.IMMEDIATE_OPERAND_PREFIX
				+ DataTypeName.STRING + AssemblyWord.VALUE_SEPARATOR
				+ LiteralSyntax.STRING_LITERAL_QUOT + IdentifierSyntax.getSignatureOf(node) + LiteralSyntax.STRING_LITERAL_QUOT;

		// Put ENDPRM instruction at the end of parameter transfering code, for optimization and readability.
		codeBuilder.append(
			this.generateInstruction(OperationCode.ENDPRM.name(), DataTypeName.STRING, functionSignatureOperand)
		);

		return codeBuilder.toString();
	}


	/**
	 * Generates code of a "return" statement.
	 *
	 * @param node The AST node of the "return" statement.
	 * @param functionLabelName The label of the function to which the return statement belongs.
	 * @return The generated code.
	 */
	private String generateReturnStatementCode(AstNode node, String functionLabelName) {

		StringBuilder codeBuilder = new StringBuilder();

		AstNode[] childNodes = node.getChildNodes();

		// For void functions:
		if (childNodes.length == 0) {
			codeBuilder.append(
				this.generateInstruction(OperationCode.RET.name(), DataTypeName.VOID, PLACE_HOLDER, functionLabelName)
			);

		// For non-void functions:
		// evaluate the value of (the expression of) the return value, and specify it as an operand of the RET instruction.
		} else {
			AstNode exprNode = childNodes[0];
			String exprValue = exprNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);
			String exprCode = this.generateExpressionCode(exprNode);
			codeBuilder.append(exprCode);
			codeBuilder.append(
				this.generateInstruction(OperationCode.RET.name(), DataTypeName.VOID, PLACE_HOLDER, functionLabelName, exprValue)
			);
		}

		return codeBuilder.toString();
	}


	/**
	 * Generates code of a "if" statement.
	 * 
	 * Behavior of a "if" statement is realized by a JMPN instruction,
	 * which jumps to a "end-point" label at the end of the block "{...}" when the condition value is false.
	 * 
	 * Note that, code generated by this method does not contain the end-point label, 
	 * because code of internal processes of "{...}" will be generated after generating code of this "if" statement.
	 * Hence, the caller side must put the end-point label after generating code of "{...}".
	 *
	 * @param node The AST node of the "if" statement.
	 * 
	 * @param lastIfConditionRegister
	 *     The register for storing the evaluated value of the condition.
	 *     When multiple "else if" or "else" sections continues after this "if" ("else if"), 
	 *     the same register will be shared between them.
	 * 
	 * @param lastIfConditionRegisterAllocRequired
	 *     Specify true if it should newly allocate the memory for "lastIfConditionRegister".
	 * 
	 * @return The generated code.
	 */
	private String generateIfStatementCode(AstNode node,
			String lastIfConditionRegister, boolean lastIfConditionRegisteAllocRequired) {

		StringBuilder codeBuilder = new StringBuilder();

		// Generate code evaluating the value of the condition expression.
		AstNode conditionExprNode = node.getChildNodes(AstNode.Type.EXPRESSION)[0];
		codeBuilder.append( this.generateExpressionCode(conditionExprNode) );

		// The value of the condition expression must be a scalar
		// (should had been checked by SemanticAnalyzer).
		if (conditionExprNode.getRank() != RANK_OF_SCALAR) {
			return null;
		}

		// Generate code for storing the evaluated condition value in "lastIfConditionRegister".
		if (lastIfConditionRegisteAllocRequired) {
			codeBuilder.append( this.generateInstruction(OperationCode.ALLOC.name(), DataTypeName.BOOL, lastIfConditionRegister));
		}
		String lastIfConditionMovCode = this.generateInstruction(
				OperationCode.MOV.name(), DataTypeName.BOOL,
				lastIfConditionRegister, conditionExprNode.getAttribute(AttributeKey.ASSEMBLY_VALUE)
		);
		codeBuilder.append(lastIfConditionMovCode);

		// Generate an instruction to jump to the end of "{...}", when the condition is not satisfied.
		String endLabel = node.getAttribute(AttributeKey.END_LABEL);
		String conditionExprValue = conditionExprNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);
		codeBuilder.append(
			this.generateInstruction(OperationCode.JMPN.name(), DataTypeName.BOOL, PLACE_HOLDER, endLabel, conditionExprValue)
		);

		return codeBuilder.toString();
	}


	/**
	 * Generates code of a "else" statement.
	 *
	 * Behavior of a "else" statement is realized by a JMP instruction,
	 * which jumps to a "end-point" label at the end of the block "{...}", 
	 * when the value of the last evaluated condition of a "if" (including "else if") statement is true.
	 * 
	 * Note that, code generated by this method does not contain the end-point label, 
	 * because code of internal processes of "{...}" will be generated after generating code of this "else" statement.
	 * Hence, the caller side must put the end-point label after generating code of "{...}".
	 * 
	 * @param node The AST node of the "else" statement.
	 * @param lastIfConditionRegister The register in which the last evaluated condition value of "if" / "else if" is stored.
	 * @return The generated code
	 */
	private String generateElseStatementCode(AstNode node, String lastIfConditionValue) {
		StringBuilder codeBuilder = new StringBuilder();

		String endLabel = node.getAttribute(AttributeKey.END_LABEL);

		// Generate an instruction to jump at the end of "{...}",
		// when the last evaluated condition of "if" / "else if" is satisified.
		codeBuilder.append(
			this.generateInstruction(OperationCode.JMP.name(), DataTypeName.BOOL, PLACE_HOLDER, endLabel, lastIfConditionValue)
		);

		return codeBuilder.toString();
	}


	/**
	 * Generates code of a "while" statement.
	 *
	 * Behavior of a "while" statement is realized by a JMP and a JMPN instructions.
	 * 
	 * At the location of a "while" statement, a JMPN instruction will be put.
	 * By this instruction, the processing flow jumps to the end of the block "{...}"
	 * (an "end-point" laben will be put there) when the loop condition isn't satisfied.
	 * 
	 * On the other hand, when the loop condition is satisfied, 
	 * the flow doesn't jump and enters into {...}.
	 * Then, the flow returns to the location of code evaluating the loop condition 
	 * (a "beginning-point" label will be put there), 
	 * by a JMP instruction at just before of the end-point label at the end of {...}.
	 * 
	 * Note that, code generated by this method does not contain the end-point label,
	 * and JMP instruction to return to the beginning-point label,
	 * because code of internal processes of "{...}" will be generated after generating code of this "while" statement.
	 * Hence, the caller side must put the end-point label and the JMP instruction after generating code of "{...}".
	 *
	 * @param node The AST node of the "while" statement.
	 * @return The generated code.
	 */
	private String generateWhileStatementCode(AstNode node) {
		StringBuilder codeBuilder = new StringBuilder();

		String beginLabel = node.getAttribute(AttributeKey.BEGIN_LABEL);
		String endLabel = node.getAttribute(AttributeKey.END_LABEL);

		// Put the beginning-point label.
		codeBuilder.append(this.generateLabelDirectiveCode(beginLabel));

		// Generate code evaluating the value of the condition expression.
		AstNode conditionExprNode = node.getChildNodes(AstNode.Type.EXPRESSION)[0];
		codeBuilder.append( this.generateExpressionCode(conditionExprNode) );

		// The value of the condition expression must be a scalar
		// (should had been checked by SemanticAnalyzer).
		if (conditionExprNode.getRank() != RANK_OF_SCALAR) {
			return null;
		}

		// Generate an instruction to jump to the end of "{...}", when the condition is not satisfied.
		String conditionExprValue = conditionExprNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);
		codeBuilder.append(
			this.generateInstruction(OperationCode.JMPN.name(), DataTypeName.BOOL, PLACE_HOLDER, endLabel, conditionExprValue)
		);

		return codeBuilder.toString();
	}


	/**
	 * Generates code of a "for" statement.
	 *
	 * Behavior of a "for" statement is realized by a JMP and a JMPN instructions.
	 * 
	 * When the processing flow reaches to the "for" statement, 
	 * firstly, code to allocate/initialize a counter variable will be executed.
	 * Then, if the loop condition isn't satisfied, the flow will jump to the end of the block "{...}"
	 * (an "end-point" laben will be put there) by JMPN instruction.
	 * 
	 * On the other hand, if the loop condition is satisfied, the flow will not jump, so it will enter into {...}.
	 * When the flow will have reached to the end of {...}, code to update the counter variable will be executed,
	 * and then the flow will return to the location of code evaluating the loop condition 
	 * (a "beginning-point" label will be put there), 
	 * by a JMP instruction.
	 * 
	 * Note that, code generated by this method does not contain the end-point label,
	 * and JMP instruction to return to the beginning-point label,
	 * because code of internal processes of "{...}" will be generated after generating code of this "for" statement.
	 * Hence, the caller side must put the end-point label and the JMP instruction after generating code of "{...}".
	 *
	 * @param node The AST node of the "for" statement.
	 * @return The generated code.
	 */
	private String generateForStatementCode(AstNode node) {
		StringBuilder codeBuilder = new StringBuilder();

		AstNode[] childNodes =  node.getChildNodes();
		String beginLabel = node.getAttribute(AttributeKey.BEGIN_LABEL);
		String endLabel = node.getAttribute(AttributeKey.END_LABEL);

		String initStatementCode = null;      // Stores code of the initializaton (or variable declaration) statement of the counter variable.
		String conditionStatementCode = null; // Stores code of the condition expression statement.
		String conditionValue = null;         // Stores a register or a immediate value of as the value of the condition expression.

		// Generate code of the initialization (or variable declaration) statement of the counter variable.
		if (childNodes[0].getType() == AstNode.Type.VARIABLE) {
			initStatementCode = this.generateVariableDeclarationStatementCode(childNodes[0]);
		} else if (childNodes[0].getType() == AstNode.Type.EXPRESSION) {
			initStatementCode = this.generateExpressionCode(childNodes[0]);
		} else if (childNodes[0].getType() == AstNode.Type.EMPTY) {
			initStatementCode = "";
		}
		codeBuilder.append(initStatementCode);

		// Put the beginning-point label.
		codeBuilder.append(this.generateLabelDirectiveCode(beginLabel));

		// Generate code of the condition expression statement.
		if (childNodes[1].getType() == AstNode.Type.EXPRESSION) {
			conditionStatementCode = this.generateExpressionCode(childNodes[1]);
			conditionValue = childNodes[1].getAttribute(AttributeKey.ASSEMBLY_VALUE);
		} else if (childNodes[1].getType() == AstNode.Type.EMPTY) {
			conditionStatementCode = "";
			conditionValue = IMMEDIATE_TRUE;
		}
		codeBuilder.append(conditionStatementCode);

		// Generate an instruction to jump to the end of "{...}", when the condition is not satisfied.
		codeBuilder.append(
			this.generateInstruction(OperationCode.JMPN.name(), DataTypeName.BOOL, PLACE_HOLDER, endLabel, conditionValue)
		);

		return codeBuilder.toString();
	}





	/**
	 * Generate code of an expression statement.
	 *
	 * @param node The AST node of the expression statement.
	 * @return The generated code.
	 */
	private String generateExpressionCode(AstNode exprRootNode) {

		StringBuilder codeBuilder = new StringBuilder();

		// Traverse AST nodes composing the expression.
		AstNode currentNode = exprRootNode.getPostorderDftFirstNode();
		while(currentNode != exprRootNode) {

			// Generate code of operation corresponding with the type of the operator.
			if (currentNode.getType() == AstNode.Type.OPERATOR) {
				String operatorSyntax = currentNode.getAttribute(AttributeKey.OPERATOR_SYNTAX);
				String operatorExecution = currentNode.getAttribute(AttributeKey.OPERATOR_EXECUTOR);

				switch (operatorExecution) {
					case AttributeValue.CALL : {
						codeBuilder.append( this.generateCallOperatorCode(currentNode) );
						break;
					}
					case AttributeValue.ASSIGNMENT : {
						codeBuilder.append( this.generateAssignmentOperatorCode(currentNode) );
						break;
					}
					case AttributeValue.ARITHMETIC : {
						switch (operatorSyntax) {
							case AttributeValue.BINARY : {
								codeBuilder.append( this.generateArithmeticBinaryOperatorCode(currentNode) );
								break;
							}
							case AttributeValue.PREFIX : {
								codeBuilder.append( this.generateArithmeticPrefixOperatorCode(currentNode) );
								break;
							}
							default : {
								throw new VnanoFatalException("Invalid operator syntax for arithmetic operators: " + operatorSyntax);
							}
						}
						break;
					}
					case AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT : {

						switch (operatorSyntax) {
							case AttributeValue.BINARY : {
								codeBuilder.append( this.generateArithmeticCompoundAssignmentBinaryOperatorCode(currentNode) );
								break;
							}
							case AttributeValue.PREFIX : {
								codeBuilder.append( this.generateArithmeticCompoundPrefixOperatorCode(currentNode) );
								break;
							}
							case AttributeValue.POSTFIX : {
								codeBuilder.append( this.generateArithmeticCompoundPostfixOperatorCode(currentNode) );
								break;
							}
							default : {
								throw new VnanoFatalException("Invalid operator syntax for arithmetic compound operators: " + operatorSyntax);
							}
						}
						break;
					}
					case AttributeValue.COMPARISON : {
						codeBuilder.append( this.generateComparisonBinaryOperatorCode(currentNode) );
						break;
					}
					case AttributeValue.LOGICAL : {
						switch (operatorSyntax) {
							case AttributeValue.BINARY : {
								codeBuilder.append( this.generateLogicalBinaryOperatorCode(currentNode) );
								break;
							}
							case AttributeValue.PREFIX : {
								codeBuilder.append( this.generateLogicalPrefixOperatorCode(currentNode) );
								break;
							}
							default : {
								throw new VnanoFatalException("Invalid operator syntax for logical operators: " + operatorSyntax);
							}
						}
						break;
					}
					case AttributeValue.SUBSCRIPT : {
						codeBuilder.append( this.generateIndexOperatorCode(currentNode) );
						break;
					}
					case AttributeValue.CAST : {
						codeBuilder.append( this.generateCastOperatorCode(currentNode) );
						break;
					}
					default : {
						throw new VnanoFatalException("Unknown operator execution type: " + operatorExecution);
					}
				}
			} // Code generations of operators


			// Some operators require to execute some additional processes 
			// after when its operand/s (may be a partial expressions) is/are evaluated.
			AstNode parentNode = currentNode.getParentNode();
			if (parentNode.getType() == AstNode.Type.OPERATOR) {
				String parentOperatorSymbol = currentNode.getParentNode().getAttribute(AttributeKey.OPERATOR_SYMBOL);

				// "&&" and "||": require code for performing the short-circuit evaluation.
				if (parentOperatorSymbol.equals(ScriptWord.SHORT_CIRCUIT_AND) || parentOperatorSymbol.equals(ScriptWord.SHORT_CIRCUIT_OR)) {

					String jumpLabel = parentNode.getAttribute(AttributeKey.END_LABEL);
					String leftOperandValue = currentNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);

					// Prepare a jump instruction (JMP for "||", and JMPN for "&&").
					String jumpOpcode = OperationCode.JMP.name();
					if (parentOperatorSymbol.equals(ScriptWord.SHORT_CIRCUIT_AND)) {
						jumpOpcode = OperationCode.JMPN.name();
					}

					// After the evaluation code of the left-operand, put the above jump instruction.
					if (currentNode == parentNode.getChildNodes()[0]) {
						codeBuilder.append(
								this.generateInstruction(jumpOpcode, DataTypeName.BOOL, PLACE_HOLDER, jumpLabel, leftOperandValue)
						);

					// After the evaluation code of the right-operand, put the label to be jumped to.
					} else {
						codeBuilder.append(
								this.generateLabelDirectiveCode(parentNode.getAttribute(AttributeKey.END_LABEL))
						);
					}
				}
			}

			currentNode = currentNode.getPostorderDftNextNode();
		}

		return codeBuilder.toString();
	}



	/**
	 * Generate VRIL code for allocating a register.
	 * 
	 * The generated code by this method uses ALLOC instruction for allocating a scalar register,
	 * and uses ALLOCR instruction for allocating an array register.
	 *
	 * @param dataType The data type of the register to be allocated.
	 * @param target The name of the register to be allocated.
	 * @param lengthsDeterminer
	            The register/variable/value having the same array-rank and lengths as the register to be allocated.
	            When this method is used for allocating a scalar register, this arguments will not be used.
	 * @param rank The array-rank of the register to be allocated (specify 0 for allocating a scalar register).
	 * @return The generated code.
	 */
	private String generateRegisterAllocationCode(String dataType, String target, String lengthsDeterminer, int rank) {
		if (rank == RANK_OF_SCALAR) {
			return this.generateInstruction(OperationCode.ALLOC.name(), dataType, target);
		} else {
			return this.generateInstruction(OperationCode.ALLOCR.name(), dataType, target, lengthsDeterminer);
		}
	}


	/**
	 * Generates code of an arithmetic binary operation.
	 *
	 * @param operatorNode The AST node of the arithmetic binary operator.
	 * @return The generated code.
	 */
	private String generateArithmeticBinaryOperatorCode(AstNode operatorNode) {
		String operatorSymbol = operatorNode.getAttribute(AttributeKey.OPERATOR_SYMBOL);

		String opcode = null;

		if (operatorSymbol.equals(ScriptWord.PLUS_OR_ADDITION)) {
			opcode = OperationCode.ADD.name();

		} else if(operatorSymbol.equals(ScriptWord.MINUS_OR_SUBTRACTION)) {
			opcode = OperationCode.SUB.name();

		} else if(operatorSymbol.equals(ScriptWord.MULTIPLICATION)) {
			opcode = OperationCode.MUL.name();

		} else if(operatorSymbol.equals(ScriptWord.DIVISION)) {
			opcode = OperationCode.DIV.name();

		} else if(operatorSymbol.equals(ScriptWord.REMAINDER)) {
			opcode = OperationCode.REM.name();

		} else {
			throw new VnanoFatalException("Invalid operator symbol for arithmetic binary operators: " + operatorSymbol);
		}

		return this.generateBinaryOperatorCode(operatorNode, opcode, false, operatorNode.getChildNodes());
	}


	/**
	 * Generates code of an arithmetic prefix operation.
	 *
	 * @param operatorNode The AST node of the arithmetic prefix operator.
	 * @return The generated code.
	 */
	private String generateArithmeticPrefixOperatorCode(AstNode operatorNode) {

		String operatorSymbol = operatorNode.getAttribute(AttributeKey.OPERATOR_SYMBOL);

		// Unary plus operator
		if (operatorSymbol.equals(ScriptWord.PLUS_OR_ADDITION)) {
			return operatorNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);

		// Unary minus operator
		} else if (operatorSymbol.equals(ScriptWord.MINUS_OR_SUBTRACTION)) {

			StringBuilder codeBuilder = new StringBuilder();

			AstNode operandNode = operatorNode.getChildNodes()[0];
			String operandValue = operandNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);
			String accumulatorRegister = operatorNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);

			// If necessary, allocate memory for the register in which the operation result will be stored.
			if (operatorNode.hasAttribute(AttributeKey.NEW_REGISTER)) {
				codeBuilder.append(
					this.generateRegisterAllocationCode(
						operatorNode.getDataTypeName(), operatorNode.getAttribute(AttributeKey.NEW_REGISTER),
						operandValue, operatorNode.getRank()
					)
				);
			}

			// Generate code of the operation.
			codeBuilder.append(
				this.generateInstruction(OperationCode.NEG.name(), operandNode.getDataTypeName(), accumulatorRegister, operandValue)
			);

			return codeBuilder.toString();

		} else {
			throw new VnanoFatalException("Unexpected arithmetic prefix operator detected.");
		}
	}


	/**
	 * Generates code of a logical binary operation.
	 *
	 * @param operatorNode The AST node of the logical binray operator.
	 * @return The generated code.
	 */
	private String generateLogicalBinaryOperatorCode(AstNode operatorNode) {
		String operatorSymbol = operatorNode.getAttribute(AttributeKey.OPERATOR_SYMBOL);

		String opcode = null;
		if (operatorSymbol.equals(ScriptWord.SHORT_CIRCUIT_AND)) {
			opcode = OperationCode.ANDM.name();
		} else if (operatorSymbol.equals(ScriptWord.SHORT_CIRCUIT_OR)) {
			opcode = OperationCode.ORM.name();
		} else {
			throw new VnanoFatalException("Invalid operator symbol for logical binary operators: " + operatorSymbol);
		}

		return this.generateBinaryOperatorCode(operatorNode, opcode, false, operatorNode.getChildNodes());
	}


	/**
	 * Generates code of a logical prefix operation.
	 *
	 * @param operatorNode The AST node of the logical prefix operator.
	 * @return The generated code.
	 */
	private String generateLogicalPrefixOperatorCode(AstNode operatorNode) {
		String operatorSymbol = operatorNode.getAttribute(AttributeKey.OPERATOR_SYMBOL);
		if (!operatorSymbol.equals(ScriptWord.NOT)) {
			throw new VnanoFatalException("Invalid operator symbol for logical prefix operators: " + operatorSymbol);
		}

		StringBuilder codeBuilder = new StringBuilder();

		AstNode operandNode = operatorNode.getChildNodes()[0];
		String operandValue = operandNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);
		String accumulatorRegister = operatorNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);

		// If necessary, allocate memory for the register in which the operation result will be stored.
		if (operatorNode.hasAttribute(AttributeKey.NEW_REGISTER)) {
			codeBuilder.append(
				this.generateRegisterAllocationCode(
					DataTypeName.BOOL, operatorNode.getAttribute(AttributeKey.NEW_REGISTER), operandValue, operatorNode.getRank()
				)
			);
		}

		// Generate code of the operation.
		codeBuilder.append(
			this.generateInstruction(OperationCode.NOT.name(), DataTypeName.BOOL, accumulatorRegister, operandValue)
		);

		return codeBuilder.toString();
	}


	/**
	 * Generates code of a comparison binary operation.
	 *
	 * @param operatorNode The AST node of the comparison binary operator.
	 * @return The generated code.
	 */
	private String generateComparisonBinaryOperatorCode(AstNode operatorNode) {
		String operatorSymbol = operatorNode.getAttribute(AttributeKey.OPERATOR_SYMBOL);

		String opcode = null;
		if(operatorSymbol.equals(ScriptWord.EQUAL)) {
			opcode = OperationCode.EQ.name();

		} else if (operatorSymbol.equals(ScriptWord.NOT_EQUAL)) {
			opcode = OperationCode.NEQ.name();

		} else if (operatorSymbol.equals(ScriptWord.LESS_THAN)) {
			opcode = OperationCode.LT.name();

		} else if (operatorSymbol.equals(ScriptWord.LESS_EQUAL)) {
			opcode = OperationCode.LEQ.name();

		} else if (operatorSymbol.equals(ScriptWord.GREATER_THAN)) {
			opcode = OperationCode.GT.name();

		} else if (operatorSymbol.equals(ScriptWord.GREATER_EQUAL)) {
			opcode = OperationCode.GEQ.name();

		} else {
			throw new VnanoFatalException("Invalid operator symbol for comparison binary operators: " + operatorSymbol);
		}

		// Generate code of the operation.
		return this.generateBinaryOperatorCode(operatorNode, opcode, false, operatorNode.getChildNodes());
	}


	/**
	 * Generates code of an assignment operation.
	 *
	 * @param operatorNode The AST node of the assignment operator.
	 * @return The generated code.
	 */
	private String generateAssignmentOperatorCode(AstNode operatorNode) {
		StringBuilder codeBuilder = new StringBuilder();

		AstNode[] operandNodes = operatorNode.getChildNodes();
		int operandLength = operandNodes.length;

		String[] operandValues = new String[operandLength];
		for (int operandIndex=0; operandIndex<operandLength; operandIndex++) {
			operandValues[operandIndex] = operandNodes[operandIndex].getAttribute(AttributeKey.ASSEMBLY_VALUE);
		}

		// If data-types of left and right operands are different, it requires a cast operation before copying the value.
		String rightHandValue = operandValues[1];
		String toType = operandNodes[0].getDataTypeName();
		String fromType = operandNodes[1].getDataTypeName();
		if (!toType.equals(fromType)) {

			// Allocate memory for a register in which the casted value will be stored.
			String castedRegister = this.generateRegisterOperandCode();
			codeBuilder.append(
				this.generateRegisterAllocationCode(toType, castedRegister, operandValues[1], operatorNode.getRank())
			);

			// Cast the value of the right operand, and store it in the register.
			String typeSpecification = toType + AssemblyWord.VALUE_SEPARATOR + fromType;
			codeBuilder.append(
				this.generateInstruction(
					OperationCode.CAST.name(), typeSpecification, castedRegister, operandValues[1]
				)
			);

			rightHandValue = castedRegister;
		}

		// Followings are code generations of the assignment operation.

		// Scalar to scalar: simply copy the value by a MOV instruction.
		if (operandNodes[0].getRank()==RANK_OF_SCALAR && operandNodes[1].getRank()==RANK_OF_SCALAR) {
			codeBuilder.append(
				this.generateInstruction(OperationCode.MOV.name(), toType, operandValues[0], rightHandValue)
			);

		// Array to array: allocate memory by an ALLOCR instruction, and copy values of all elements by a MOV instruction.
		} else if (operandNodes[0].getRank()!=RANK_OF_SCALAR && operandNodes[1].getRank()!=RANK_OF_SCALAR) {
			codeBuilder.append(
				this.generateInstruction(OperationCode.ALLOCR.name(), toType, operandValues[0], rightHandValue)
			);
			codeBuilder.append(
				this.generateInstruction(OperationCode.MOV.name(), toType, operandValues[0], rightHandValue)
			);

		// Scalar to array: fill values of all elements of the array by the scalar value, by a FILL instruction.
		} else if (operandNodes[0].getRank()!=RANK_OF_SCALAR && operandNodes[1].getRank()==RANK_OF_SCALAR) {
			codeBuilder.append(
				this.generateInstruction(OperationCode.FILL.name(), toType, operandValues[0], rightHandValue)
			);

		// Array to scalar: copy an element of the array to the scalar by MOV instruction, where the size of the array must be 1 (dynamically checked).
		} else {
			codeBuilder.append(
				this.generateInstruction(OperationCode.MOV.name(), toType, operandValues[0], rightHandValue)
			);
		}

		return codeBuilder.toString();
	}


	/**
	 * Generates code of an arithmetic-assignment compound operation.
	 *
	 * @param operatorNode The AST node of the arithmetic-assignment compound operator.
	 * @return The generated code.
	 */
	private String generateArithmeticCompoundAssignmentBinaryOperatorCode(AstNode operatorNode) {

		// Note: For arithmetic-assignment compound operation, 
		//       array-ranks and lengths of left and right operands must be the same.

		String operatorSymbol = operatorNode.getAttribute(AttributeKey.OPERATOR_SYMBOL);

		String arithmeticOpcode = null;
		if(operatorSymbol.equals(ScriptWord.ADDITION_ASSIGNMENT)) {
			arithmeticOpcode = OperationCode.ADD.name();

		} else if (operatorSymbol.equals(ScriptWord.SUBTRACTION_ASSIGNMENT)) {
			arithmeticOpcode = OperationCode.SUB.name();

		} else if (operatorSymbol.equals(ScriptWord.MULTIPLICATION_ASSIGNMENT)) {
			arithmeticOpcode = OperationCode.MUL.name();

		} else if (operatorSymbol.equals(ScriptWord.DIVISION_ASSIGNMENT)) {
			arithmeticOpcode = OperationCode.DIV.name();

		} else if (operatorSymbol.equals(ScriptWord.REMAINDER_ASSIGNMENT)) {
			arithmeticOpcode = OperationCode.REM.name();

		} else {
			throw new VnanoFatalException("Invalid operator symbol for arithmetic compound assignment operators: " + operatorSymbol);
		}

		AstNode[] childNodes = operatorNode.getChildNodes();
		AstNode[] arithmeticOperandNodes = { childNodes[0], childNodes[1] };

		String executionType = operatorNode.getAttribute(AttributeKey.OPERATOR_EXECUTION_DATA_TYPE);
		String resultType = operatorNode.getAttribute(AttributeKey.DATA_TYPE);
		boolean castBeforeStoringNecessary = !executionType.equals(resultType);

		return this.generateBinaryOperatorCode(operatorNode, arithmeticOpcode, castBeforeStoringNecessary, arithmeticOperandNodes);
	}


	/**
	* Generates code of an arithmetic-assignment postfix operation (prefix increment and decrement).
	 *
	 * @param operatorNode The AST node of the arithmetic postfix operator.
	 * @return The generated code.
	 */
	private String generateArithmeticCompoundPostfixOperatorCode(AstNode operatorNode) {
		StringBuilder codeBuilder = new StringBuilder();

		String operatorSymbol = operatorNode.getAttribute(AttributeKey.OPERATOR_SYMBOL);
		String executionDataType = operatorNode.getAttribute(AttributeKey.OPERATOR_EXECUTION_DATA_TYPE);

		String opcode = null;
		if (operatorSymbol.equals(ScriptWord.INCREMENT)) {
			opcode = OperationCode.ADD.name();
		} else if (operatorSymbol.equals(ScriptWord.DECREMENT)) {
			opcode = OperationCode.SUB.name();
		} else {
			throw new VnanoFatalException("Invalid operator symbol for arithmetic compound prefix operators: " + operatorSymbol);
		}

		// The variable to be incremented/decremented.
		AstNode variableNode = operatorNode.getChildNodes()[0];
		String variableValue = variableNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);

		// Store the value of the variable before incremented/decremented, in a register.
		String storageRegister = operatorNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);
		codeBuilder.append(
			this.generateRegisterAllocationCode(executionDataType, storageRegister, variableValue, operatorNode.getRank())
		);
		codeBuilder.append(
			this.generateInstruction(OperationCode.MOV.name(), executionDataType, storageRegister, variableValue)
		);

		// Create an AST node representing the amount (step) of the increment/decrement as an immediate value.
		AstNode stepNode = new AstNode(AstNode.Type.LEAF, variableNode.getLineNumber(), variableNode.getFileName());
		stepNode.setAttribute(AttributeKey.DATA_TYPE, executionDataType);
		stepNode.setAttribute(AttributeKey.RANK, Integer.toString(RANK_OF_SCALAR));
		if (executionDataType.equals(DataTypeName.DEFAULT_INT)) {
			String immediateValue = this.generateImmediateOperandCode(executionDataType, "1");
			stepNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, immediateValue);
		} else if (executionDataType.equals(DataTypeName.DEFAULT_FLOAT)) {
			String immediateValue = this.generateImmediateOperandCode(executionDataType, "1.0");
			stepNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, immediateValue);
		}

		// Create a copy of the operator node, and modify its "dest" information, 
		// for generating code by using methods generating arithmetic binary operations.
		AstNode destModifiedNode = operatorNode.clone();
		destModifiedNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, variableValue);
		destModifiedNode.removeAttribute(AttributeKey.NEW_REGISTER);

		// Generate code of the increment/decrement operation (by using methods generating binary addition/subtraction operations).
		String binaryOperationCode = this.generateBinaryOperatorCode(destModifiedNode, opcode, false, variableNode, stepNode);
		codeBuilder.append(binaryOperationCode);

		return codeBuilder.toString();
	}


	/**
	 * Generates code of an arithmetic-assignment prefix operation (postfix increment and decrement).
	 *
	 * @param operatorNode The AST node of the arithmetic prefix operator.
	 * @return The generated code.
	 */
	private String generateArithmeticCompoundPrefixOperatorCode(AstNode operatorNode) {

		String operatorSymbol = operatorNode.getAttribute(AttributeKey.OPERATOR_SYMBOL);

		String opcode = null;
		if (operatorSymbol.equals(ScriptWord.INCREMENT)) {
			opcode = OperationCode.ADD.name();
		} else if (operatorSymbol.equals(ScriptWord.DECREMENT)) {
			opcode = OperationCode.SUB.name();
		} else {
			throw new VnanoFatalException("Invalid operator symbol for arithmetic compound prefix operators: " + operatorSymbol);
		}

		// The variable to be incremented/decremented.
		AstNode variableNode = operatorNode.getChildNodes()[0];

		// Create an AST node representing the amount (step) of the increment/decrement as an immediate value.
		AstNode stepNode = new AstNode(AstNode.Type.LEAF, variableNode.getLineNumber(), variableNode.getFileName());
		String executionDataType = operatorNode.getAttribute(AttributeKey.OPERATOR_EXECUTION_DATA_TYPE);
		stepNode.setAttribute(AttributeKey.DATA_TYPE, executionDataType);
		stepNode.setAttribute(AttributeKey.RANK, Integer.toString(RANK_OF_SCALAR));
		if (executionDataType.equals(DataTypeName.DEFAULT_INT)) {
			String immediateValue = this.generateImmediateOperandCode(executionDataType, "1");
			stepNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, immediateValue);
		} else if (executionDataType.equals(DataTypeName.DEFAULT_FLOAT)) {
			String immediateValue = this.generateImmediateOperandCode(executionDataType, "1.0");
			stepNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, immediateValue);
		}

		// Generate code of the increment/decrement operation (by using methods generating binary addition/subtraction operations).
		return this.generateBinaryOperatorCode(operatorNode, opcode, false, variableNode, stepNode);
	}


	/**
	 * Generates code of a binary operation.
	 *
	 * @param operatorNode The AST node of the binray operator.
	 * @param castBeforeStoring Specify "true" if the data-type of the output (dest) operand with the data-type of the operation.
	 * @param inputNodes AST nodes of input values (operands) of the operation.
	 * @return The generated code.
	 */
	private String generateBinaryOperatorCode(AstNode operatorNode, String operationCode, boolean castBeforeStoring, AstNode ...inputNodes) {

		StringBuilder codeBuilder = new StringBuilder();

		String executionDataType = operatorNode.getAttribute(AttributeKey.OPERATOR_EXECUTION_DATA_TYPE);
		String resultDataType = operatorNode.getAttribute(AttributeKey.DATA_TYPE);

		int rank = operatorNode.getRank();
		int inputLength = inputNodes.length;

		// Extract information of the input (operand) registers or variables of the operation from the AST.
		String[] input = new String[inputLength];
		for (int inputIndex=0; inputIndex<inputLength; inputIndex++) {
			input[inputIndex] = inputNodes[inputIndex].getAttribute(AttributeKey.ASSEMBLY_VALUE);
		}

		// Extract information of the output (dest) register or variable of the operation from the AST.
		String output = operatorNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);

		// Check whether there are both arrays(vectors) and scalars in operands.
		// (If true, it requires to convert a scalar to an array to perform the operation.)
		boolean vectorScalarMixed = false;
		if (rank != RANK_OF_SCALAR) {
			for (AstNode inputNode: inputNodes) {
				if (inputNode.getRank() == RANK_OF_SCALAR) {
					vectorScalarMixed = true;
					break;
				}
			}
		}

		// Check whether type-conversions (implicit cast) are necessary.
		boolean castNecessary = false;
		for (int inputIndex=0; inputIndex<inputLength; inputIndex++) {
			if (!inputNodes[inputIndex].getDataTypeName().equals(executionDataType)) {
				castNecessary = true;
				break;
			}
		}

		// Search the "length-determiner" operand, which determines the array-rank and lengths of operands of code to be generated.
		// (It is the first array operand, or simply the first operand if there are no array operands.)
		String lengthsDeterminer = input[0];
		for (int inputIndex=0; inputIndex<inputLength; inputIndex++) {
			if (inputNodes[inputIndex].getRank() != RANK_OF_SCALAR) {
				lengthsDeterminer = input[inputIndex];
				break;
			}
		}

		// If data-types of some operands are different with the data-type of operation, cast them (so-called "implicit cast").
		if (castNecessary) {

			for (int inputIndex=0; inputIndex<inputLength; inputIndex++) {
				String operandDataType = inputNodes[inputIndex].getDataTypeName();

				if (!operandDataType.equals(executionDataType)) {

					// Allocate a new register, for storing the casted value in it.
					String castTarget = input[inputIndex];
					int castTargetRank = inputNodes[inputIndex].getRank();
					String castedRegister = this.generateRegisterOperandCode();
					codeBuilder.append(
						this.generateRegisterAllocationCode(executionDataType, castedRegister, castTarget, castTargetRank)
					);

					// Generate a CAST instruction to cast the operand value.
					String typeSpecification = executionDataType + AssemblyWord.VALUE_SEPARATOR + operandDataType;
					codeBuilder.append(
						this.generateInstruction(OperationCode.CAST.name(), typeSpecification, castedRegister, castTarget
						)
					);
					input[inputIndex] = castedRegister;
				}
			}
		}

		// If necessary, convert values of scalar operands to arrays.
		if (vectorScalarMixed) {
			for (int inputIndex=0; inputIndex<inputLength; inputIndex++) {
				if (inputNodes[inputIndex].getRank() == RANK_OF_SCALAR) {

					// Allocate an scalar(vector) register.
					String filledRegister = this.generateRegisterOperandCode();
					codeBuilder.append(
						this.generateRegisterAllocationCode(executionDataType, filledRegister, lengthsDeterminer, rank)
					);

					// Fill all elements of the above register by the value of the scalar operand.
					codeBuilder.append(
						this.generateInstruction(OperationCode.FILL.name(), executionDataType, filledRegister, input[inputIndex])
					);

					input[inputIndex] = filledRegister;
				}
			}
		}


		// If necessary, allocate memory for the output (dest) register of the operation.
		if (operatorNode.hasAttribute(AttributeKey.NEW_REGISTER)) {
			codeBuilder.append(
				this.generateRegisterAllocationCode(
					resultDataType, operatorNode.getAttribute(AttributeKey.NEW_REGISTER),
					lengthsDeterminer, rank)
			);
		}

		// Generate code of the operation.
		// If necessary, cast the result of the operation.
		if (castBeforeStoring) {

			// Allocate a temporary register to store the result of the operation.
			String storeRegister = this.generateRegisterOperandCode();
			codeBuilder.append(
				this.generateRegisterAllocationCode(executionDataType, storeRegister, lengthsDeterminer, rank)
			);
			// Generate an instruction performing operation, where its dest is the above temporary register.
			codeBuilder.append(
				this.generateInstruction(operationCode, executionDataType, storeRegister, input[0], input[1])
			);
			// Cast the value of the temporary register to the output variable/register.
			String castTypeOperand = resultDataType + AssemblyWord.VALUE_SEPARATOR + executionDataType;
			codeBuilder.append(
				this.generateInstruction(OperationCode.CAST.name(), castTypeOperand, output, storeRegister)
			);

		// If the cast isn't necessary, 
		// we can directly specify the output register/variable to the dest of the instruction performing the operation.
		} else {
			codeBuilder.append(
				this.generateInstruction(operationCode, executionDataType, output, input[0], input[1])
			);
		}

		return codeBuilder.toString();
	}


	/**
	 * Generate code of a function-call operation.
	 *
	 * @param operatorNode The AST node of the function-call operator.
	 * @return The generated code.
	 */
	private String generateCallOperatorCode(AstNode operatorNode) {
		StringBuilder codeBuilder = new StringBuilder();

		String returnRegister = operatorNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);
		String returnDataTypeName = operatorNode.getDataTypeName();

		AstNode[] childNodes = operatorNode.getChildNodes();
		int childNLength = childNodes.length;

		String scope = operatorNode.getAttribute(AttributeKey.SCOPE);

		int operandLength = childNLength + 1;
		String[] operands = new String[operandLength];
		for (int operandIndex=1; operandIndex<operandLength; operandIndex++) {
			operands[operandIndex] = childNodes[operandIndex-1].getAttribute(AttributeKey.ASSEMBLY_VALUE);
		}

		// If the callee is an external function: generate CALLX instruction.
		if (scope.equals(AttributeValue.GLOBAL)) {
			if (returnDataTypeName.equals(DataTypeName.VOID)) {
				operands[0] = PLACE_HOLDER;
			} else {
				operands[0] = returnRegister;
			}
			codeBuilder.append(
				this.generateInstruction(OperationCode.CALLX.name(), operatorNode.getDataTypeName(), operands)
			);

		// If the callee is an external function: generate CALL instruction, and code receiving the return value.
		} else if (scope.equals(AttributeValue.LOCAL)) {

			// As the result of a CALL instruction, the return value will be pushed on the stack.
			// So specify a place-holder "-" to the dest operand of the CALL instruction.
			operands[0] = Character.toString(AssemblyWord.PLACEHOLDER_OPERAND_PREFIX);

			// The first operand of a CALL instruction is a label, so put a label-prefix to it.
			operands[1] = AssemblyWord.LABEL_OPERAND_PREFIX + operands[1];

			// Generate a CALL instruction.
			codeBuilder.append(
				this.generateInstruction(OperationCode.CALL.name(), operatorNode.getDataTypeName(), operands)
			);

			// If the return value is declared as "void", dispose the dummy return value pushed on the stack.
			if (returnDataTypeName.equals(DataTypeName.VOID)) {
				codeBuilder.append(
					this.generateInstruction(OperationCode.POP.name(), operatorNode.getDataTypeName(), PLACE_HOLDER)
				);

			// Otherwise, generate code receiving the return value from the stack.
			} else {
				if(operatorNode.getRank() == RANK_OF_SCALAR) {
					codeBuilder.append(
						this.generateInstruction(OperationCode.ALLOC.name(), operatorNode.getDataTypeName(), returnRegister)
					);
				} else {
					codeBuilder.append(
						this.generateInstruction(OperationCode.ALLOCP.name(), operatorNode.getDataTypeName(), returnRegister)
					);
				}
				codeBuilder.append(
					this.generateInstruction(OperationCode.MOVPOP.name(), operatorNode.getDataTypeName(), returnRegister)
				);
			}

		} else {
			throw new VnanoFatalException("Unknown function scope: " + scope);
		}

		return codeBuilder.toString();
	}


	/**
	 * Generate code of a subscript (array indexing) operation.
	 *
	 * @param operatorNode The AST node of the subscript operator.
	 * @return The generated code.
	 */
	private String generateIndexOperatorCode(AstNode operatorNode) {
		StringBuilder codeBuilder = new StringBuilder();

		AstNode[] inputNodes = operatorNode.getChildNodes();
		int rank = inputNodes.length - 1;

		String targetOperand = inputNodes[0].getAttribute(AttributeKey.ASSEMBLY_VALUE);

		// Prepare array-indices operands.
		String[] indexOperands = new String[rank];
		for (int dim=0; dim<rank; dim++) {
			indexOperands[dim] = inputNodes[dim + 1].getAttribute(AttributeKey.ASSEMBLY_VALUE);
		}

		// Prepare the register in which the element of the array will be stored.
		String accumulator = operatorNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);

		// Prepare operands of an ELEM instruction to be generated.
		String[] allOperands = new String[indexOperands.length + 2];
		allOperands[0] = accumulator;
		allOperands[1] = targetOperand;
		System.arraycopy(indexOperands, 0, allOperands, 2, indexOperands.length);

		// Chech whether the parent node is an assignment (or assignment compound) operator node or not.
		AstNode parentNode = operatorNode.getParentNode();
		boolean isParentAssignment = parentNode.getType() == AstNode.Type.OPERATOR && (
				parentNode.getAttribute(AttributeKey.OPERATOR_EXECUTOR).equals(AttributeValue.ASSIGNMENT)
				||
				parentNode.getAttribute(AttributeKey.OPERATOR_EXECUTOR).equals(AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT
		));

		// Chech whether the parent node is an function-call operator or not.
		boolean isParentCall = parentNode.getType() == AstNode.Type.OPERATOR && (
				parentNode.getAttribute(AttributeKey.OPERATOR_EXECUTOR).equals(AttributeValue.CALL)
		);
		// 同様に親ノードが、関数呼び出し演算子である場合、参照渡し先で代入される可能性があるので、すぐ後の判定条件に使うために控える

		// Check whether the result (dest) of this operation may be assigned a value later or not.
		boolean mayBeModified =
			(isParentAssignment && operatorNode.getSiblingIndex() == 0) // 代入の左辺かどうか
			||
			isParentCall; // 関数の引数かどうか

		// If the result (dest) of this operation may be assigned a value later, generate a REFELM instruction, 
		// which lins the data-reference of the dest operand to the array element.
		if (mayBeModified) {
			codeBuilder.append(
				this.generateInstruction(
					OperationCode.REFELM.name(), inputNodes[0].getDataTypeName(), allOperands
				)
			);

		// Otherwise, generate a MOVELM instruction, 
		// which copies the value of the array element to the dest operand.
		// (MOVELM gives more advantage than REFELM, to the optimization in the VM.)
		} else {
			codeBuilder.append(
				this.generateInstruction(OperationCode.ALLOC.name(), operatorNode.getDataTypeName(), accumulator)
			);
			codeBuilder.append(
				this.generateInstruction(
					OperationCode.MOVELM.name(), inputNodes[0].getDataTypeName(), allOperands
				)
			);
		}
		return codeBuilder.toString();
	}


	/**
	 * Generates code of a cast operation.
	 *
	 * @param operatorNode The AST node of the cast operator.
	 * @return The generated code.
	 */
	private String generateCastOperatorCode(AstNode operatorNode) {
		StringBuilder codeBuilder = new StringBuilder();

		// Prepare the value, data-type, and array-rank of data to be casted.
		AstNode targetNode = operatorNode.getChildNodes()[0];
		String fromDataType = targetNode.getAttribute(AttributeKey.DATA_TYPE);
		String fromValue = targetNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);
		int fromRank = targetNode.getRank();

		// Prepare the data-type of the result of the cast operation.
		String toDataType = operatorNode.getAttribute(AttributeKey.DATA_TYPE);

		// Allocate a register in which the casted value will be stored.
		String castedRegister = operatorNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);
		codeBuilder.append(
			this.generateRegisterAllocationCode(toDataType, castedRegister, fromValue, fromRank) // fromValue is a "length determiner"
		);

		// Generate code of the cast operation.
		String typeSpecification = toDataType + AssemblyWord.VALUE_SEPARATOR + fromDataType;
		codeBuilder.append(
			this.generateInstruction(OperationCode.CAST.name(), typeSpecification, castedRegister, fromValue
			)
		);

		return codeBuilder.toString();
	}


	/**
	 * Generates code of dierctives of language information.
	 *
	 * @return The generated code.
	 */
	private String generateLanguageInformationDirectives() {
		StringBuilder codeBuilder = new StringBuilder();

		// Intermediate language name (VRIL)
		codeBuilder.append(AssemblyWord.ASSEMBLY_LANGUAGE_IDENTIFIER_DIRECTIVE);
		codeBuilder.append(AssemblyWord.WORD_SEPARATOR);
		codeBuilder.append("\"");
		codeBuilder.append(AssemblyWord.ASSEMBLY_LANGUAGE_NAME);
		codeBuilder.append("\"");
		codeBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);
		codeBuilder.append(AssemblyWord.LINE_SEPARATOR);

		// Intermediate language version
		codeBuilder.append(AssemblyWord.ASSEMBLY_LANGUAGE_VERSION_DIRECTIVE);
		codeBuilder.append(AssemblyWord.WORD_SEPARATOR);
		codeBuilder.append("\"");
		codeBuilder.append(AssemblyWord.ASSEMBLY_LANGUAGE_VERSION);
		codeBuilder.append("\"");
		codeBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);
		codeBuilder.append(AssemblyWord.LINE_SEPARATOR);

		// Scripting language name (Vnano)
		codeBuilder.append(AssemblyWord.SCRIPT_LANGUAGE_IDENTIFIER_DIRECTIVE);
		codeBuilder.append(AssemblyWord.WORD_SEPARATOR);
		codeBuilder.append("\"");
		codeBuilder.append(ScriptWord.SCRIPT_LANGUAGE_NAME);
		codeBuilder.append("\"");
		codeBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);
		codeBuilder.append(AssemblyWord.LINE_SEPARATOR);

		// Scripting language version
		codeBuilder.append(AssemblyWord.SCRIPT_LANGUAGE_VERSION_DIRECTIVE);
		codeBuilder.append(AssemblyWord.WORD_SEPARATOR);
		codeBuilder.append("\"");
		codeBuilder.append(ScriptWord.SCRIPT_LANGUAGE_VERSION);
		codeBuilder.append("\"");
		codeBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);
		codeBuilder.append(AssemblyWord.LINE_SEPARATOR);

		return codeBuilder.toString();
	}


	/**
	 * Generates code of function identifier directives, 
	 * of all functions called from the AST (of the script).
	 *
	 * @param inputAst The root node of the AST (of the script).
	 * @return The generated code
	 */
	private String generateFunctionIdentifierDirectives(AstNode inputAst) {
		StringBuilder codeBuilder = new StringBuilder();
		AstNode currentNode = inputAst.getPostorderDftFirstNode();

		// A Set for storing idenfires of which directives had already been generated.
		Set<String> generatedSet = new HashSet<String>();

		// Traverse all nodes:
		while(currentNode != inputAst) {

			// If the node is a function-call operator:
			if (currentNode.getType() == AstNode.Type.OPERATOR
				&& currentNode.getAttribute(AttributeKey.OPERATOR_EXECUTOR).equals(AttributeValue.CALL)) {

				// Prepare the identifier of the callee function.
				String calleeSignature = currentNode.getAttribute(AttributeKey.CALLEE_SIGNATURE);
				String identifier = AssemblyWord.IDENTIFIER_OPERAND_PREFIX + calleeSignature;

				// Prepare the scope of the callee function.
				String scope = currentNode.getAttribute(AttributeKey.SCOPE);

				// Generate a function identifier directive.
				if (!generatedSet.contains(identifier)) {
					generatedSet.add(identifier);
					if (scope.equals(AttributeValue.GLOBAL)) {
						codeBuilder.append(AssemblyWord.GLOBAL_FUNCTION_DIRECTIVE);
					} else if (scope.equals(AttributeValue.LOCAL)) {
						codeBuilder.append(AssemblyWord.LOCAL_FUNCTION_DIRECTIVE);
					} else {
						throw new VnanoFatalException("Unknown function scope: " + currentNode.getAttribute(AttributeKey.SCOPE));
					}
					codeBuilder.append(AssemblyWord.WORD_SEPARATOR);
					codeBuilder.append(identifier);
					codeBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);
					codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
				}
			}

			currentNode = currentNode.getPostorderDftNextNode();
		}
		return codeBuilder.toString();
	}


	/**
	 * Generate code of global variable identifier directives, 
	 * of all external variables accessed from the AST (of the script).
	 *
	 * @param inputAst The root node of the AST (of the script).
	 * @return The generated code
	 */
	private String generateGlobalIdentifierDirectives(AstNode inputAst) {
		StringBuilder codeBuilder = new StringBuilder();
		AstNode currentNode = inputAst.getPostorderDftFirstNode();

		// A Set for storing idenfires of which directives had already been generated.
		Set<String> generatedSet = new HashSet<String>();

		// Traverse all nodes:
		while(currentNode != inputAst) {

			// If the node is a leaf node accessing to an external variable:
			if (currentNode.getType()==AstNode.Type.LEAF
				&&
				currentNode.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.VARIABLE_IDENTIFIER)
				&&
				currentNode.getAttribute(AttributeKey.SCOPE).equals(AttributeValue.GLOBAL) ){


				String variableName = currentNode.getAttribute(AttributeKey.IDENTIFIER_VALUE);
				String identifier = IdentifierSyntax.getAssemblyIdentifierOf(variableName);

				// Generate a global variable identifier directive.
				if (!generatedSet.contains(identifier)) {
					codeBuilder.append(AssemblyWord.GLOBAL_VARIABLE_DIRECTIVE);
					codeBuilder.append(AssemblyWord.WORD_SEPARATOR);
					codeBuilder.append(identifier);
					codeBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);
					codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
				}
			}

			currentNode = currentNode.getPostorderDftNextNode();
		}
		return codeBuilder.toString();
	}


	/**
	 * Generates code of a meta information directives.
	 *
	 * @param inputAst The node of the AST.
	 * @return The generated code
	 */
	private String generateMetaDirectiveCode(AstNode node) {

		// The script name should already be normalized, but normalize it again here.
		String escapedFileName = IdentifierSyntax.normalizeScriptIdentifier(node.getFileName());

		StringBuilder codeBuilder = new StringBuilder();

		codeBuilder.append(AssemblyWord.META_DIRECTIVE);
		codeBuilder.append(AssemblyWord.WORD_SEPARATOR);

		codeBuilder.append("\"");
		codeBuilder.append(MetaInformationSyntax.generateMetaInformation(node.getLineNumber(), escapedFileName));
		codeBuilder.append("\"");

		codeBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);
		codeBuilder.append(AssemblyWord.LINE_SEPARATOR);

		return codeBuilder.toString();
	}



	/**
	 * Generate code of a label directives, of the specified label.
	 *
	 * @param labelName The name of the label.
	 * @return The generated code.
	 */
	private String generateLabelDirectiveCode(String labelName) {
		StringBuilder labelBuilder = new StringBuilder();
		labelBuilder.append(AssemblyWord.LABEL_DIRECTIVE);
		labelBuilder.append(AssemblyWord.WORD_SEPARATOR);
		labelBuilder.append(labelName);
		labelBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);
		labelBuilder.append(AssemblyWord.LINE_SEPARATOR);
		return labelBuilder.toString();
	}


	/**
	 * Generate a new label.
	 *
	 * @return The notation in code of the generated label.
	 */
	private String generateLabelOperandCode() {
		StringBuilder labelBuilder = new StringBuilder();
		labelBuilder.append(AssemblyWord.LABEL_OPERAND_PREFIX);
		labelBuilder.append(CodeGenerator.LABEL_NAME);
		labelBuilder.append(Integer.toString(this.labelCounter));
		this.labelCounter++;
		return labelBuilder.toString();
	}


	/**
	 * Generate a new register.
	 *
	 * @return The notation in code of the generated register.
	 */
	private String generateRegisterOperandCode() {

		StringBuilder returnBuilder = new StringBuilder();
		returnBuilder.append(AssemblyWord.REGISTER_OPERAND_PREFIX);
		returnBuilder.append(this.registerCounter);
		this.registerCounter++;

		return returnBuilder.toString();
	}


	/**
	 * Generates an immediate value corresponds with the specified literal.
	 *
	 * @param typeName The name of the data-type.
	 * @param literal The value of the literal.
	 * @return The notation in code of the immediate value.
	 */
	private String generateImmediateOperandCode(String typeName, String literal) {
		StringBuilder returnBuilder = new StringBuilder();
		returnBuilder.append(AssemblyWord.IMMEDIATE_OPERAND_PREFIX);
		returnBuilder.append(typeName);
		returnBuilder.append(AssemblyWord.VALUE_SEPARATOR);
		returnBuilder.append(literal);

		return returnBuilder.toString();
	}


	/**
	 * Generates an immediate value of the default value corresponds with the specified data-type.
	 *
	 * @param typeName The name of the data-type.
	 * @return The notation in code of the immediate value.
	 */
	private String generateDefaultValueImmediateOperandCode(String typeName) {
		StringBuilder returnBuilder = new StringBuilder();
		returnBuilder.append(AssemblyWord.IMMEDIATE_OPERAND_PREFIX);
		returnBuilder.append(typeName);
		returnBuilder.append(AssemblyWord.VALUE_SEPARATOR);

		DataType dataType = null;
		try {
			dataType = DataTypeName.getDataTypeOf(typeName);
		} catch (VnanoException vne) {
			throw new VnanoFatalException("Unexpected data type: " + typeName);
		}

		String defaultValueLiteral = "";
		switch (dataType) {
			case INT64 : {
				defaultValueLiteral = "0";
				break;
			}
			case FLOAT64 : {
				defaultValueLiteral = "0.0";
				break;
			}
			case BOOL : {
				defaultValueLiteral = "false";
				break;
			}
			case STRING : {
				String quot = Character.toString(LiteralSyntax.STRING_LITERAL_QUOT);
				defaultValueLiteral = quot + quot; // ""
				break;
			}
			default : { throw new VnanoFatalException("Unexpected data type: " + typeName); }
		}

		returnBuilder.append(defaultValueLiteral);

		return returnBuilder.toString();
	}


	/**
	 * Generate code of an instruction.
	 *
	 * @param opcode The operation code.
	 * @param dataType The data-type of the operation.
	 * @param operands Operands of the instruction.
	 * @return The generated code.
	 */
	private String generateInstruction(String opcode, String dataType, String... operands) {
		StringBuilder codeBuilder = new StringBuilder();
		codeBuilder.append(AssemblyWord.INDENT);
		codeBuilder.append(opcode);
		codeBuilder.append(AssemblyWord.WORD_SEPARATOR);
		codeBuilder.append(dataType);
		for (String operand: operands) {
			codeBuilder.append(AssemblyWord.WORD_SEPARATOR);
			codeBuilder.append(operand);
		}
		codeBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);
		codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
		return codeBuilder.toString();
	}


	/**
	* Arranges (reshapes) the form of code for readability.
	 *
	 * @param code The original code.
	 * @return The reshaped code.
	 */
	private String realign(String code) {
		StringBuilder codeBuilder = new StringBuilder();

		String[] lines = code.split(AssemblyWord.LINE_SEPARATOR_REGEX);
		int lineLength = lines.length;

		// Extract language information directives, and relocate them to the top of code.
		boolean languageDirectiveExist = false;
		for (int lineIndex=0; lineIndex<lineLength; lineIndex++) {
			if (lines[lineIndex].startsWith(AssemblyWord.ASSEMBLY_LANGUAGE_IDENTIFIER_DIRECTIVE)
					|| lines[lineIndex].startsWith(AssemblyWord.ASSEMBLY_LANGUAGE_VERSION_DIRECTIVE)
					|| lines[lineIndex].startsWith(AssemblyWord.SCRIPT_LANGUAGE_IDENTIFIER_DIRECTIVE)
					|| lines[lineIndex].startsWith(AssemblyWord.SCRIPT_LANGUAGE_VERSION_DIRECTIVE)) {

				languageDirectiveExist = true;
				codeBuilder.append(lines[lineIndex]);
				codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
				lines[lineIndex] = "";
			}
		}

		// Insert a blank line.
		if (languageDirectiveExist) {
			codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
		}

		// Extract global function identifier directives, and relocate them.
		boolean globalFunctionDirectiveExist = false;
		for (int lineIndex=0; lineIndex<lineLength; lineIndex++) {
			if (lines[lineIndex].startsWith(AssemblyWord.GLOBAL_FUNCTION_DIRECTIVE)) {
				globalFunctionDirectiveExist = true;
				codeBuilder.append(lines[lineIndex]);
				codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
				lines[lineIndex] = "";
			}
		}

		// Insert a blank line.
		if (globalFunctionDirectiveExist) {
			codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
		}

		// Extract local function identifier directives, and relocate them.
		boolean localFunctionDirectiveExist = false;
		for (int lineIndex=0; lineIndex<lineLength; lineIndex++) {
			if (lines[lineIndex].startsWith(AssemblyWord.LOCAL_FUNCTION_DIRECTIVE)) {
				localFunctionDirectiveExist = true;
				codeBuilder.append(lines[lineIndex]);
				codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
				lines[lineIndex] = "";
			}
		}

		// Insert a blank line.
		if (localFunctionDirectiveExist) {
			codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
		}

		// Extract global variable identifier directives, and relocate them.
		boolean globalVariableDirectiveExist = false;
		for (int lineIndex=0; lineIndex<lineLength; lineIndex++) {
			if (lines[lineIndex].startsWith(AssemblyWord.GLOBAL_VARIABLE_DIRECTIVE)) {
				globalVariableDirectiveExist = true;
				codeBuilder.append(lines[lineIndex]);
				codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
				lines[lineIndex] = "";
			}
		}

		// Insert a blank line.
		if (globalVariableDirectiveExist) {
			codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
		}

		// Extract local variable identifier directives, and relocate them.
		@SuppressWarnings("unused")
		boolean localVariableDirectiveExist = false;
		for (int lineIndex=0; lineIndex<lineLength; lineIndex++) {
			if (lines[lineIndex].startsWith(AssemblyWord.LOCAL_VARIABLE_DIRECTIVE)) {
				localVariableDirectiveExist = true;
				codeBuilder.append(lines[lineIndex]);
				codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
				lines[lineIndex] = "";
			}
		}

		// Extract other lines, and relocate them, if necessary:
		for (int lineIndex=0; lineIndex<lineLength; lineIndex++) {

			// Delete blank lines in original code.
			// 元のコード内にあった空白行は削る
			if (lines[lineIndex].length() == 0) {
				continue;
			}

			// Insert a blank line before a meta information directive.
			if (lines[lineIndex].startsWith(AssemblyWord.META_DIRECTIVE)) {
				codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
			}

			codeBuilder.append(lines[lineIndex]);
			codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
			lines[lineIndex] = "";
		}

		return codeBuilder.toString();
	}


	/**
	 * Generate finalization code, which will be put at the end of code
	 * for returning the evaluation value of the script to the caller side of the script engine.
	 *
	 * @param inputAst The root node of the AST (of the script).
	 * @return The generated code.
	 */
	private String generateFinalizationCode(AstNode inputAst) {
		StringBuilder codeBuilder = new StringBuilder();

		// Extract all chold nodes of the root node.
		AstNode[] topLevelStatementNodes = inputAst.getChildNodes();
		int statementLength = topLevelStatementNodes.length;

		// If the last statement is an expression statement, 
		// its evaluated value must be returned to the caller side of the script engine.
		// So allocate memory for storing its evaluated value.
		String evalDataType = null;
		String evalValue = null;
		if (statementLength != 0) {
			AstNode lastStatementNode = topLevelStatementNodes[statementLength-1];
			if (lastStatementNode.getType() == AstNode.Type.EXPRESSION) {
				evalDataType = lastStatementNode.getAttribute(AttributeKey.DATA_TYPE);
				evalValue = lastStatementNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);
			}
		}

		// If there is no evaluated value to be returned to the engine, generate a END instruction having no operand.
		if (evalDataType == null || evalDataType.equals(DataTypeName.VOID)) {
			codeBuilder.append(
				this.generateInstruction(OperationCode.END.name(), DataTypeName.VOID, PLACE_HOLDER)
			);

		// If there is an evaluated value, specify it as an operand of the END instruction.
		} else {
			codeBuilder.append(
				this.generateInstruction(OperationCode.END.name(), evalDataType, PLACE_HOLDER, evalValue)
			);
		}

		return codeBuilder.toString();
	}


}



