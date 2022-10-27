package org.vcssl.nano.compiler;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vcssl.nano.spec.AssemblyWord;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.spec.OperatorPrecedence;
import org.vcssl.nano.spec.ScriptWord;

public class CodeGeneratorTest {

	private static final int RANK_OF_SCALAR = 0;

	private final String EOI = AssemblyWord.INSTRUCTION_SEPARATOR + AssemblyWord.LINE_SEPARATOR; // 命令末尾記号+改行
	private final String WS = AssemblyWord.WORD_SEPARATOR;
	private final String VS = AssemblyWord.VALUE_SEPARATOR;
	private final String IND = AssemblyWord.INDENT;
	private final String IINT = AssemblyWord.IMMEDIATE_OPERAND_PREFIX + DataTypeName.DEFAULT_INT;
	private final String IFLOAT = AssemblyWord.IMMEDIATE_OPERAND_PREFIX + DataTypeName.DEFAULT_FLOAT;

	private final char R = AssemblyWord.REGISTER_OPERAND_PREFIX;
	private final String IVA = AssemblyWord.IDENTIFIER_OPERAND_PREFIX + "intVectorA";
	private final String IVB = AssemblyWord.IDENTIFIER_OPERAND_PREFIX + "intVectorB";
	private final String ISA = AssemblyWord.IDENTIFIER_OPERAND_PREFIX + "intScalarA";
	private final String ISB = AssemblyWord.IDENTIFIER_OPERAND_PREFIX + "intScalarB";
	private final String FVA = AssemblyWord.IDENTIFIER_OPERAND_PREFIX + "floatVectorA";
	//private static final String FVB = AssemblyWord.OPERAND_PREFIX_IDENTIFIER + "floatVectorB";
	//private static final String FSA = AssemblyWord.OPERAND_PREFIX_IDENTIFIER + "floatScalarA";
	private final String FSB = AssemblyWord.IDENTIFIER_OPERAND_PREFIX + "floatScalarB";
	//private static final String BVA = AssemblyWord.OPERAND_PREFIX_IDENTIFIER + "boolVectorA";
	//private static final String BVB = AssemblyWord.OPERAND_PREFIX_IDENTIFIER + "boolVectorB";

	private static final String GLOBAL_DIRECTIVE_IVA = "#GLOBAL_VARIABLE	_intVectorA";
	private static final String GLOBAL_DIRECTIVE_IVB = "#GLOBAL_VARIABLE	_intVectorB";
	private static final String GLOBAL_DIRECTIVE_ISA = "#GLOBAL_VARIABLE	_intScalarA";
	private static final String GLOBAL_DIRECTIVE_ISB = "#GLOBAL_VARIABLE	_intScalarB";
	private static final String GLOBAL_DIRECTIVE_FVA = "#GLOBAL_VARIABLE	_floatVectorA";
	//private static final String GLOBAL_DIRECTIVE_FVB = "#GLOBAL_VARIABLE	_floatVectorB";
	//private static final String GLOBAL_DIRECTIVE_FSA = "#GLOBAL_VARIABLE	_floatScalarA";
	private static final String GLOBAL_DIRECTIVE_FSB = "#GLOBAL_VARIABLE	_floatScalarB";
	//private static final String GLOBAL_DIRECTIVE_BVA = "#GLOBAL_VARIABLE	_boolVectorA";
	//private static final String GLOBAL_DIRECTIVE_BVB = "#GLOBAL_VARIABLE	_boolVectorB";

	private final String END_CODE = IND + OperationCode.END + WS + DataTypeName.VOID
	                                     + WS + AssemblyWord.PLACEHOLDER_OPERAND_PREFIX + EOI;

	// Generated code always begins with the following contents:
	private final String HEADER =
			AssemblyWord.ASSEMBLY_LANGUAGE_IDENTIFIER_DIRECTIVE + AssemblyWord.WORD_SEPARATOR
			+
			"\"" + AssemblyWord.ASSEMBLY_LANGUAGE_NAME + "\"" + EOI
			+
			AssemblyWord.ASSEMBLY_LANGUAGE_VERSION_DIRECTIVE + AssemblyWord.WORD_SEPARATOR
			+
			"\"" + AssemblyWord.ASSEMBLY_LANGUAGE_VERSION + "\"" + EOI
			+
			AssemblyWord.SCRIPT_LANGUAGE_IDENTIFIER_DIRECTIVE + AssemblyWord.WORD_SEPARATOR
			+
			"\"" + ScriptWord.SCRIPT_LANGUAGE_NAME + "\"" + EOI
			+
			AssemblyWord.SCRIPT_LANGUAGE_VERSION_DIRECTIVE + AssemblyWord.WORD_SEPARATOR
			+
			"\"" + ScriptWord.SCRIPT_LANGUAGE_VERSION + "\"" + EOI
			+
			AssemblyWord.LINE_SEPARATOR;

	// Code of a dummy meta information directive.
	private final String META = AssemblyWord.LINE_SEPARATOR
			+ AssemblyWord.META_DIRECTIVE + AssemblyWord.WORD_SEPARATOR + "\"line=123, file=Test.vnano\"";


	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}


	private AstNode createRootNode() {
		return new AstNode(AstNode.Type.ROOT, 123, "Test.vnano");
	}

	private AstNode createExpressionNode(String dataTypeName, int rank) {
		return new AstNode(AstNode.Type.EXPRESSION, 123, "Test.vnano");
	}

	private AstNode createOperatorNode(String symbol, int priority, String syntax, String executor,
			String dataTypeName, int rank) {

		AstNode node = new AstNode(AstNode.Type.OPERATOR, 123, "Test.vnano");
		node.setAttribute(AttributeKey.OPERATOR_SYMBOL, symbol);
		node.setAttribute(AttributeKey.OPERATOR_PRECEDENCE, Integer.toString(priority));
		node.setAttribute(AttributeKey.OPERATOR_SYNTAX, syntax);
		node.setAttribute(AttributeKey.OPERATOR_EXECUTOR, executor);
		node.setAttribute(AttributeKey.OPERATOR_EXECUTION_DATA_TYPE, dataTypeName);
		node.setAttribute(AttributeKey.DATA_TYPE, dataTypeName);
		node.setAttribute(AttributeKey.ARRAY_RANK, Integer.toString(rank));
		return node;
	}

	private AstNode createLiteralNode(String value, String dataTypeName, int rank) {
		AstNode node = new AstNode(AstNode.Type.LEAF, 123, "Test.vnano");
		node.setAttribute(AttributeKey.LEAF_TYPE, AttributeValue.LITERAL);
		node.setAttribute(AttributeKey.LITERAL_VALUE, value);
		node.setAttribute(AttributeKey.DATA_TYPE, dataTypeName);
		node.setAttribute(AttributeKey.ARRAY_RANK, Integer.toString(rank));
		return node;
	}

	private AstNode createVariableIdentifierNode(String identifier, String dataTypeName, int rank, String scope) {
		AstNode node = new AstNode(AstNode.Type.LEAF, 123, "Test.vnano");
		node.setAttribute(AttributeKey.LEAF_TYPE, AttributeValue.VARIABLE_IDENTIFIER);
		node.setAttribute(AttributeKey.IDENTIFIER_VALUE, identifier);
		node.setAttribute(AttributeKey.DATA_TYPE, dataTypeName);
		node.setAttribute(AttributeKey.ARRAY_RANK, Integer.toString(rank));
		node.setAttribute(AttributeKey.SCOPE, scope);
		return node;
	}


	@Test
	public void testGenerateAssignmentOperatorCode() {
		this.testGenerateAssignmentOperatorCodeVector();
	}

	public void testGenerateAssignmentOperatorCodeVector() {

		// Prepare AST of "intVectorA = intVectorB;"
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DataTypeName.DEFAULT_INT, 1);
		AstNode operatorNode = this.createOperatorNode(
				ScriptWord.ASSIGNMENT, OperatorPrecedence.ASSIGNMENT, AttributeValue.BINARY,
				AttributeValue.ASSIGNMENT, DataTypeName.DEFAULT_INT, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intVectorA", DataTypeName.DEFAULT_INT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intVectorB", DataTypeName.DEFAULT_INT, 1, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		String generatedCode = new CodeGenerator().generate(rootNode);

		/* Expected code:

		  #GLOBAL	_intVectorA
		  #GLOBAL	_intVectorB
		  #META  "line=123, file=Test.vnano";
		      ALLOCR  int    _intVectorA    _intVectorB;
		      MOV     int    _intVectorA    _intVectorB;
		      END     void    -;
		 */
		String expectedCode = HEADER + GLOBAL_DIRECTIVE_IVA + EOI + GLOBAL_DIRECTIVE_IVB + EOI + META + EOI
			+ IND + OperationCode.ALLOCR + WS + DataTypeName.DEFAULT_INT + WS + IVA + WS + IVB + EOI
			+ IND + OperationCode.MOV + WS + DataTypeName.DEFAULT_INT + WS + IVA + WS + IVB + EOI
			+ END_CODE;

		assertEquals(expectedCode.replace(AssemblyWord.LINE_SEPARATOR,""), generatedCode.replace(AssemblyWord.LINE_SEPARATOR,""));
	}


	@Test
	public void testGenerateArithmeticBinaryOperatorCode() {

		this.testGenerateArithmeticBinaryOperatorCodeScalar(
			ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalar(
			ScriptWord.MINUS_OR_SUBTRACTION, OperatorPrecedence.SUBTRACTION, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalar(
			ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalar(
			ScriptWord.DIVISION, OperatorPrecedence.DIVISION, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalar(
			ScriptWord.REMAINDER, OperatorPrecedence.REMAINDER, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(
			ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(
			ScriptWord.MINUS_OR_SUBTRACTION, OperatorPrecedence.SUBTRACTION, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(
			ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(
			ScriptWord.DIVISION, OperatorPrecedence.DIVISION, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(
			ScriptWord.REMAINDER, OperatorPrecedence.REMAINDER, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeVector(
			ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeVector(
			ScriptWord.MINUS_OR_SUBTRACTION, OperatorPrecedence.SUBTRACTION, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeVector(
			ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeVector(
			ScriptWord.DIVISION, OperatorPrecedence.DIVISION, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeVector(
			ScriptWord.REMAINDER, OperatorPrecedence.REMAINDER, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeVectorCast(
			ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorCast(
			ScriptWord.MINUS_OR_SUBTRACTION, OperatorPrecedence.SUBTRACTION, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorCast(
			ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorCast(
			ScriptWord.DIVISION, OperatorPrecedence.DIVISION, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorCast(
			ScriptWord.REMAINDER, OperatorPrecedence.REMAINDER, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(
			ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(
			ScriptWord.MINUS_OR_SUBTRACTION, OperatorPrecedence.SUBTRACTION, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(
			ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(
			ScriptWord.DIVISION, OperatorPrecedence.DIVISION, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(
			ScriptWord.REMAINDER, OperatorPrecedence.REMAINDER, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(
			ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(
			ScriptWord.MINUS_OR_SUBTRACTION, OperatorPrecedence.SUBTRACTION, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(
			ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(
			ScriptWord.DIVISION, OperatorPrecedence.DIVISION, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(
			ScriptWord.REMAINDER, OperatorPrecedence.REMAINDER, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(
			ScriptWord.PLUS_OR_ADDITION, OperatorPrecedence.ADDITION, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(
			ScriptWord.MINUS_OR_SUBTRACTION, OperatorPrecedence.SUBTRACTION, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(
			ScriptWord.MULTIPLICATION, OperatorPrecedence.MULTIPLICATION, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(
			ScriptWord.DIVISION, OperatorPrecedence.DIVISION, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(
			ScriptWord.REMAINDER, OperatorPrecedence.REMAINDER, OperationCode.REM
		);
	}


	public void testGenerateArithmeticBinaryOperatorCodeScalar(String symbol, int priority, OperationCode operationCode) {

		// Prepare AST of "1 symbol 2;", where the symbol is an arithmetic operator.
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DataTypeName.DEFAULT_INT, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DataTypeName.DEFAULT_INT, RANK_OF_SCALAR
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createLiteralNode("1", DataTypeName.DEFAULT_INT, RANK_OF_SCALAR),
			this.createLiteralNode("2", DataTypeName.DEFAULT_INT, RANK_OF_SCALAR),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		String generatedCode = new CodeGenerator().generate(rootNode);

		/* Expected code:

		  #META  "line=123, file=Test.vnano";
		      ALLOC   int    R0;
		      ???     int    R0    ~int:1    ~int:2;   (??? is the specified operation code)
		      END     void    -;
		 */
		String expectedCode = HEADER + META + EOI
			+ IND + OperationCode.ALLOC + WS + DataTypeName.DEFAULT_INT + WS + R + "0" + EOI
			+ IND + operationCode + WS + DataTypeName.DEFAULT_INT + WS + R + "0" + WS + IINT + VS + "1" + WS + IINT + VS + "2" + EOI
			+ END_CODE;

		assertEquals(expectedCode.replace(AssemblyWord.LINE_SEPARATOR,""), generatedCode.replace(AssemblyWord.LINE_SEPARATOR,""));
	}

	public void testGenerateArithmeticBinaryOperatorCodeScalarCast(String symbol, int priority, OperationCode operationCode) {

		// Prepare AST of "1 symbol 2;", where the symbol is an arithmetic operator.
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DataTypeName.DEFAULT_FLOAT, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DataTypeName.DEFAULT_FLOAT, RANK_OF_SCALAR
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createLiteralNode("1", DataTypeName.DEFAULT_FLOAT, RANK_OF_SCALAR),
			this.createLiteralNode("2", DataTypeName.DEFAULT_INT, RANK_OF_SCALAR),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		String generatedCode = new CodeGenerator().generate(rootNode);

		/* Expected code:

		  #META  "line=123, file=Test.vnano";
		      ALLOC   float      R1;
		      CAST    float:int  R1    ~int:2;
		      ALLOC   float      R0;
		      ???     float      R0    ~float:1    ~int:2;   (??? is the specified operation code)
		      END     void    -;
		 */
		String expectedCode = HEADER + META + EOI
			+ IND + OperationCode.ALLOC + WS + DataTypeName.DEFAULT_FLOAT + WS + R + "1" + EOI
			+ IND + OperationCode.CAST + WS + DataTypeName.DEFAULT_FLOAT + VS + DataTypeName.DEFAULT_INT + WS + R + "1" + WS + IINT + VS + "2" + EOI
			+ IND + OperationCode.ALLOC + WS + DataTypeName.DEFAULT_FLOAT + WS + R + "0" + EOI
			+ IND + operationCode + WS + DataTypeName.DEFAULT_FLOAT + WS + R + "0" + WS + IFLOAT + VS + "1" + WS + R + "1" + EOI
			+ END_CODE;

		assertEquals(expectedCode.replace(AssemblyWord.LINE_SEPARATOR,""), generatedCode.replace(AssemblyWord.LINE_SEPARATOR,""));
	}
	public void testGenerateArithmeticBinaryOperatorCodeVector(String symbol, int priority, OperationCode operationCode) {

		// Prepare AST of "intVectorA symbol intVectorB;", where the symbol is an arithmetic operator.
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DataTypeName.DEFAULT_INT, 1);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DataTypeName.DEFAULT_INT, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intVectorA", DataTypeName.DEFAULT_INT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intVectorB", DataTypeName.DEFAULT_INT, 1, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		String generatedCode = new CodeGenerator().generate(rootNode);

		/* Expected code:

		  #GLOBAL	_intVectorA
		  #GLOBAL	_intVectorB
		  #META  "line=123, file=Test.vnano";
		      ALLOCR  int    R0    _intVectorA;
		      ???     int    R0    _intVectorA    _intVectorB;   (??? is the specified operation code)
		      END     void    -;
		 */
		String expectedCode = HEADER + GLOBAL_DIRECTIVE_IVA + EOI + GLOBAL_DIRECTIVE_IVB + EOI + META + EOI
			+ IND + OperationCode.ALLOCR + WS + DataTypeName.DEFAULT_INT + WS + R + "0" + WS + IVA + EOI
			+ IND + operationCode + WS + DataTypeName.DEFAULT_INT + WS + R + "0" + WS + IVA + WS + IVB + EOI
			+ END_CODE;

		assertEquals(expectedCode.replace(AssemblyWord.LINE_SEPARATOR,""), generatedCode.replace(AssemblyWord.LINE_SEPARATOR,""));
	}

	public void testGenerateArithmeticBinaryOperatorCodeVectorCast(String symbol, int priority, OperationCode operationCode) {

		// Prepare AST of "floatVectorA symbol intVectorB;", where the symbol is an arithmetic operator.
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DataTypeName.DEFAULT_FLOAT, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DataTypeName.DEFAULT_FLOAT, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("floatVectorA", DataTypeName.DEFAULT_FLOAT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intVectorB", DataTypeName.DEFAULT_INT, 1, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		String generatedCode = new CodeGenerator().generate(rootNode);

		/* Expected code:

		  #GLOBAL	_floatVectorA
		  #GLOBAL	_intVectorB
		  #META  "line=123, file=Test.vnano";
		      ALLOCR  float      R1    _intVectorB;
		      CAST    float:int  R1    _intVectorB;
		      ALLOCR  float      R0    _floatVectorA;
		      ???     float      R0    _floatVectorA    R1;   (??? is the specified operation code)
		      END     void    -;
		 */
		String expectedCode = HEADER + GLOBAL_DIRECTIVE_FVA + EOI + GLOBAL_DIRECTIVE_IVB + EOI + META + EOI
			+ IND + OperationCode.ALLOCR + WS + DataTypeName.DEFAULT_FLOAT + WS + R + "1" + WS + IVB + EOI
			+ IND + OperationCode.CAST + WS + DataTypeName.DEFAULT_FLOAT + VS + DataTypeName.DEFAULT_INT + WS + R + "1" + WS + IVB + EOI
			+ IND + OperationCode.ALLOCR + WS + DataTypeName.DEFAULT_FLOAT + WS + R + "0" + WS + FVA + EOI
			+ IND + operationCode + WS + DataTypeName.DEFAULT_FLOAT + WS + R + "0" + WS + FVA + WS + R + "1" + EOI
			+ END_CODE;

		assertEquals(expectedCode.replace(AssemblyWord.LINE_SEPARATOR,""), generatedCode.replace(AssemblyWord.LINE_SEPARATOR,""));
	}
	public void testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(String symbol, int priority, OperationCode operationCode) {

		// Prepare AST of "intVectorA symbol floatVectorB;", where the symbol is an arithmetic operator.
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DataTypeName.DEFAULT_INT, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DataTypeName.DEFAULT_INT, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intVectorA", DataTypeName.DEFAULT_INT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intScalarB", DataTypeName.DEFAULT_INT, RANK_OF_SCALAR, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		String generatedCode = new CodeGenerator().generate(rootNode);

		/* Expected code:

		  #GLOBAL	_floatVectorA
		  #GLOBAL	_intVectorB
		  #META  "line=123, file=Test.vnano";
		      ALLOCR  int        R1    _intVectorA;
              FILL    int        R1    _intScalarB;
		      ALLOCR  int        R0    _intVectorA;
		      ???     int        R0    _intVectorA    R1;   (??? is the specified operation code)
		      END     void    -;
		 */
		String expectedCode = HEADER + GLOBAL_DIRECTIVE_IVA + EOI + GLOBAL_DIRECTIVE_ISB + EOI + META + EOI
			+ IND + OperationCode.ALLOCR + WS + DataTypeName.DEFAULT_INT + WS + R + "1" + WS + IVA + EOI
			+ IND + OperationCode.FILL + WS + DataTypeName.DEFAULT_INT + WS + R + "1" + WS + ISB + EOI
			+ IND + OperationCode.ALLOCR + WS + DataTypeName.DEFAULT_INT + WS + R + "0" + WS + IVA + EOI
			+ IND + operationCode + WS + DataTypeName.DEFAULT_INT + WS + R + "0" + WS + IVA + WS + R + "1" + EOI
			+ END_CODE;

		assertEquals(expectedCode.replace(AssemblyWord.LINE_SEPARATOR,""), generatedCode.replace(AssemblyWord.LINE_SEPARATOR,""));
	}
	public void testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(String symbol, int priority, OperationCode operationCode) {

		// Prepare AST of "intVectorA symbol floatScalarB;", where the symbol is an arithmetic operator.
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DataTypeName.DEFAULT_FLOAT, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DataTypeName.DEFAULT_FLOAT, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intVectorA", DataTypeName.DEFAULT_INT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("floatScalarB", DataTypeName.DEFAULT_FLOAT, RANK_OF_SCALAR, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		String generatedCode = new CodeGenerator().generate(rootNode);

		/* Expected code:

		  #GLOBAL	_floatVectorA
		  #GLOBAL	_intVectorB
		  #META  "line=123, file=Test.vnano";
		      ALLOCR  float      R1    _intVectorA;
		      CAST    float:int  R1    _intVectorA;
		      ALLOCR  float      R2    _intVectorA;
              FILL    float      R2    _floatScalarB;
		      ALLOCR  float      R0    _intVectorA;
		      ???     float      R0    R1    R2;   (??? is the specified operation code)
		      END     void    -;
		 */
		String expectedCode = HEADER + GLOBAL_DIRECTIVE_IVA + EOI + GLOBAL_DIRECTIVE_FSB + EOI + META + EOI
			+ IND + OperationCode.ALLOCR + WS + DataTypeName.DEFAULT_FLOAT + WS + R + "1" + WS + IVA + EOI
			+ IND + OperationCode.CAST + WS + DataTypeName.DEFAULT_FLOAT + VS + DataTypeName.DEFAULT_INT + WS + R + "1" + WS + IVA + EOI
			+ IND + OperationCode.ALLOCR + WS + DataTypeName.DEFAULT_FLOAT + WS + R + "2" + WS + IVA + EOI
			+ IND + OperationCode.FILL + WS + DataTypeName.DEFAULT_FLOAT + WS + R + "2" + WS + FSB + EOI
			+ IND + OperationCode.ALLOCR + WS + DataTypeName.DEFAULT_FLOAT + WS + R + "0" + WS + IVA + EOI
			+ IND + operationCode + WS + DataTypeName.DEFAULT_FLOAT + WS + R + "0" + WS + R + "1" + WS + R + "2" + EOI
			+ END_CODE;

		assertEquals(expectedCode.replace(AssemblyWord.LINE_SEPARATOR,""), generatedCode.replace(AssemblyWord.LINE_SEPARATOR,""));
	}
	public void testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(String symbol, int priority, OperationCode operationCode) {

		// Prepare AST of "floatVectorA symbol intScalarB;", where the symbol is an arithmetic operator.
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DataTypeName.DEFAULT_FLOAT, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DataTypeName.DEFAULT_FLOAT, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("floatVectorA", DataTypeName.DEFAULT_FLOAT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intScalarB", DataTypeName.DEFAULT_INT, RANK_OF_SCALAR, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		String generatedCode = new CodeGenerator().generate(rootNode);

		/* Expected code:

		  #GLOBAL	_floatVectorA
		  #GLOBAL	_intVectorB
		  #META  "line=123, file=Test.vnano";
		      ALLOC   int        R1;
		      CAST    float:int  R1    _intScalarB;
		      ALLOCR  float      R2    _floatVectorA;
              FILL 	  float      R2    R1;
		      ALLOCR  float      R0    _floatVectorA;
		      ???     float      R0    _floatVectorA    R2;   (??? is the specified operation code)
		      END     void    -;
		 */
		String expectedCode = HEADER + GLOBAL_DIRECTIVE_FVA + EOI + GLOBAL_DIRECTIVE_ISB + EOI + META + EOI
			+ IND + OperationCode.ALLOC + WS + DataTypeName.DEFAULT_FLOAT + WS + R + "1" + EOI
			+ IND + OperationCode.CAST + WS + DataTypeName.DEFAULT_FLOAT + VS + DataTypeName.DEFAULT_INT + WS + R + "1" + WS + ISB + EOI
			+ IND + OperationCode.ALLOCR + WS + DataTypeName.DEFAULT_FLOAT + WS + R + "2" + WS + FVA + EOI
			+ IND + OperationCode.FILL + WS + DataTypeName.DEFAULT_FLOAT + WS + R + "2" + WS + R + "1" + EOI
			+ IND + OperationCode.ALLOCR + WS + DataTypeName.DEFAULT_FLOAT + WS + R + "0" + WS + FVA + EOI
			+ IND + operationCode + WS + DataTypeName.DEFAULT_FLOAT + WS + R + "0" + WS + FVA + WS + R + "2" + EOI
			+ END_CODE;

		assertEquals(expectedCode.replace(AssemblyWord.LINE_SEPARATOR,""), generatedCode.replace(AssemblyWord.LINE_SEPARATOR,""));
	}


	@Test
	public void testGenerateArithmeticCompoundAssignmentOperatorCode() {

		this.testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(
			ScriptWord.ADDITION_ASSIGNMENT, OperatorPrecedence.ADDITION_ASSIGNMENT, OperationCode.ADD
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(
			ScriptWord.SUBTRACTION_ASSIGNMENT, OperatorPrecedence.SUBTRACTION_ASSIGNMENT, OperationCode.SUB
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(
			ScriptWord.MULTIPLICATION_ASSIGNMENT, OperatorPrecedence.MULTIPLICATION_ASSIGNMENT, OperationCode.MUL
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(
			ScriptWord.DIVISION_ASSIGNMENT, OperatorPrecedence.DIVISION_ASSIGNMENT, OperationCode.DIV
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(
			ScriptWord.REMAINDER_ASSIGNMENT, OperatorPrecedence.REMAINDER_ASSIGNMENT, OperationCode.REM
		);

		this.testGenerateArithmeticCompoundAssignmentOperatorCodeVector(
			ScriptWord.ADDITION_ASSIGNMENT, OperatorPrecedence.ADDITION_ASSIGNMENT, OperationCode.ADD
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeVector(
			ScriptWord.SUBTRACTION_ASSIGNMENT, OperatorPrecedence.SUBTRACTION_ASSIGNMENT, OperationCode.SUB
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeVector(
			ScriptWord.MULTIPLICATION_ASSIGNMENT, OperatorPrecedence.MULTIPLICATION_ASSIGNMENT, OperationCode.MUL
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeVector(
			ScriptWord.DIVISION_ASSIGNMENT, OperatorPrecedence.DIVISION_ASSIGNMENT, OperationCode.DIV
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeVector(
			ScriptWord.REMAINDER_ASSIGNMENT, OperatorPrecedence.REMAINDER_ASSIGNMENT, OperationCode.REM
		);
	}

	public void testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(String symbol, int priority, OperationCode operationCode) {

		// Prepare AST of "intScalarA symbol intScalarB;", where the symbol is an arithmetic-compound operator.
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DataTypeName.DEFAULT_INT, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT, DataTypeName.DEFAULT_INT, 0
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intScalarA", DataTypeName.DEFAULT_INT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intScalarB", DataTypeName.DEFAULT_INT, 1, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		String generatedCode = new CodeGenerator().generate(rootNode);

		/* Expected code:

		  #META  "line=123, file=Test.vnano";
		      ???     int    _intScalarA    _intScalarA    _intScalarB;   (??? is the specified operation code)
		      END     void    -;
		 */
		String expectedCode = HEADER + GLOBAL_DIRECTIVE_ISA + EOI + GLOBAL_DIRECTIVE_ISB + EOI + META + EOI
			+ IND + operationCode + WS + DataTypeName.DEFAULT_INT + WS + ISA + WS + ISA + WS + ISB + EOI
			+ END_CODE;

		assertEquals(expectedCode.replace(AssemblyWord.LINE_SEPARATOR,""), generatedCode.replace(AssemblyWord.LINE_SEPARATOR,""));
	}
	public void testGenerateArithmeticCompoundAssignmentOperatorCodeVector(String symbol, int priority, OperationCode operationCode) {

		// Prepare AST of "intVectorA symbol intVectorB;", where the symbol is an arithmetic-compound operator.
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DataTypeName.DEFAULT_INT, 1);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT, DataTypeName.DEFAULT_INT, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intVectorA", DataTypeName.DEFAULT_INT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intVectorB", DataTypeName.DEFAULT_INT, 1, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		String generatedCode = new CodeGenerator().generate(rootNode);

		/* Expected code:

		  #GLOBAL	_intVectorA
		  #GLOBAL	_intVectorB
		  #META  "line=123, file=Test.vnano";
		      ???     int    _intVectorA    _intVectorA    _intVectorB;   (??? is the specified operation code)
		      END     void    -;
		 */
		String expectedCode = HEADER + GLOBAL_DIRECTIVE_IVA + EOI + GLOBAL_DIRECTIVE_IVB + EOI + META + EOI
			+ IND + operationCode + WS + DataTypeName.DEFAULT_INT + WS + IVA + WS + IVA + WS + IVB + EOI
			+ END_CODE;

		assertEquals(expectedCode.replace(AssemblyWord.LINE_SEPARATOR,""), generatedCode.replace(AssemblyWord.LINE_SEPARATOR,""));
	}


}
