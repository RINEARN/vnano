/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.lang.Variable;
import org.vcssl.nano.processor.OperationCode;
import org.vcssl.nano.spec.AssemblyWord;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.PriorityTable;
import org.vcssl.nano.spec.ScriptWord;

public class CodeGeneratorTest {

	private static final int RANK_OF_SCALAR = 0;

	private static final String META_DIRECTIVE = "#META\t\"line=123, file=Test.vnano\""; // メタ情報行の内容
	private static final String EOI = AssemblyWord.INSTRUCTION_SEPARATOR + AssemblyWord.LINE_SEPARATOR; // 命令末尾記号+改行
	private static final String WS = AssemblyWord.WORD_SEPARATOR;
	private static final String VS = AssemblyWord.VALUE_SEPARATOR;
	private static final String IND = AssemblyWord.INDENT;
	private static final String IINT = AssemblyWord.OPERAND_PREFIX_IMMEDIATE + DataTypeName.INT;
	private static final String IFLOAT = AssemblyWord.OPERAND_PREFIX_IMMEDIATE + DataTypeName.FLOAT;

	private static final char R = AssemblyWord.OPERAND_PREFIX_REGISTER;
	private static final String IVA = AssemblyWord.OPERAND_PREFIX_IDENTIFIER + "intVectorA";
	private static final String IVB = AssemblyWord.OPERAND_PREFIX_IDENTIFIER + "intVectorB";
	private static final String ISA = AssemblyWord.OPERAND_PREFIX_IDENTIFIER + "intScalarA";
	private static final String ISB = AssemblyWord.OPERAND_PREFIX_IDENTIFIER + "intScalarB";
	private static final String FVA = AssemblyWord.OPERAND_PREFIX_IDENTIFIER + "floatVectorA";
	//private static final String FVB = AssemblyWord.OPERAND_PREFIX_IDENTIFIER + "floatVectorB";
	//private static final String FSA = AssemblyWord.OPERAND_PREFIX_IDENTIFIER + "floatScalarA";
	private static final String FSB = AssemblyWord.OPERAND_PREFIX_IDENTIFIER + "floatScalarB";
	//private static final String BVA = AssemblyWord.OPERAND_PREFIX_IDENTIFIER + "boolVectorA";
	//private static final String BVB = AssemblyWord.OPERAND_PREFIX_IDENTIFIER + "boolVectorB";

	private static final String GLOBAL_DIRECTIVE_IVA = "#GLOBAL	_intVectorA";
	private static final String GLOBAL_DIRECTIVE_IVB = "#GLOBAL	_intVectorB";
	private static final String GLOBAL_DIRECTIVE_ISA = "#GLOBAL	_intScalarA";
	private static final String GLOBAL_DIRECTIVE_ISB = "#GLOBAL	_intScalarB";
	private static final String GLOBAL_DIRECTIVE_FVA = "#GLOBAL	_floatVectorA";
	//private static final String GLOBAL_DIRECTIVE_FVB = "#GLOBAL	_floatVectorB";
	//private static final String GLOBAL_DIRECTIVE_FSA = "#GLOBAL	_floatScalarA";
	private static final String GLOBAL_DIRECTIVE_FSB = "#GLOBAL	_floatScalarB";
	//private static final String GLOBAL_DIRECTIVE_BVA = "#GLOBAL	_boolVectorA";
	//private static final String GLOBAL_DIRECTIVE_BVB = "#GLOBAL	_boolVectorB";

	private Interconnect interconnect;

	@Before
	public void setUp() throws Exception {
		this.interconnect = new Interconnect();
		this.interconnect.connect(new Variable("intVectorA", DataType.INT64, 1));
		this.interconnect.connect(new Variable("intVectorB", DataType.INT64, 1));
		this.interconnect.connect(new Variable("intScalarA", DataType.INT64, RANK_OF_SCALAR));
		this.interconnect.connect(new Variable("intScalarB", DataType.INT64, RANK_OF_SCALAR));
		this.interconnect.connect(new Variable("floatVectorA", DataType.FLOAT64, 1));
		this.interconnect.connect(new Variable("floatVectorB", DataType.FLOAT64, 1));
		this.interconnect.connect(new Variable("boolVectorA", DataType.BOOL, 1));
		this.interconnect.connect(new Variable("boolVectorB", DataType.BOOL, 1));
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
		node.addAttribute(AttributeKey.OPERATOR_SYMBOL, symbol);
		node.addAttribute(AttributeKey.OPERATOR_PRIORITY, Integer.toString(priority));
		node.addAttribute(AttributeKey.OPERATOR_SYNTAX, syntax);
		node.addAttribute(AttributeKey.OPERATOR_EXECUTOR, executor);
		node.addAttribute(AttributeKey.OPERATOR_EXECUTION_DATA_TYPE, dataTypeName);
		node.addAttribute(AttributeKey.DATA_TYPE, dataTypeName);
		node.addAttribute(AttributeKey.RANK, Integer.toString(rank));
		return node;
	}

	private AstNode createLiteralNode(String value, String dataTypeName, int rank) {
		AstNode node = new AstNode(AstNode.Type.LEAF, 123, "Test.vnano");
		node.addAttribute(AttributeKey.LEAF_TYPE, AttributeValue.LITERAL);
		node.addAttribute(AttributeKey.LITERAL_VALUE, value);
		node.addAttribute(AttributeKey.DATA_TYPE, dataTypeName);
		node.addAttribute(AttributeKey.RANK, Integer.toString(rank));
		return node;
	}

	private AstNode createVariableIdentifierNode(String identifier, String dataTypeName, int rank, String scope) {
		AstNode node = new AstNode(AstNode.Type.LEAF, 123, "Test.vnano");
		node.addAttribute(AttributeKey.LEAF_TYPE, AttributeValue.VARIABLE_IDENTIFIER);
		node.addAttribute(AttributeKey.IDENTIFIER_VALUE, identifier);
		node.addAttribute(AttributeKey.DATA_TYPE, dataTypeName);
		node.addAttribute(AttributeKey.RANK, Integer.toString(rank));
		node.addAttribute(AttributeKey.SCOPE, scope);
		return node;
	}


	// 算術二項演算子のテスト
	@Test
	public void testGenerateArithmeticBinaryOperatorCode() {

		this.testGenerateArithmeticBinaryOperatorCodeScalar(
			ScriptWord.PLUS, PriorityTable.ADDITION, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalar(
			ScriptWord.MINUS, PriorityTable.SUBTRACTION, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalar(
			ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalar(
			ScriptWord.DIVISION, PriorityTable.DIVISION, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalar(
			ScriptWord.REMAINDER, PriorityTable.REMAINDER, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(
			ScriptWord.PLUS, PriorityTable.ADDITION, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(
			ScriptWord.MINUS, PriorityTable.SUBTRACTION, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(
			ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(
			ScriptWord.DIVISION, PriorityTable.DIVISION, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(
			ScriptWord.REMAINDER, PriorityTable.REMAINDER, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeVector(
			ScriptWord.PLUS, PriorityTable.ADDITION, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeVector(
			ScriptWord.MINUS, PriorityTable.SUBTRACTION, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeVector(
			ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeVector(
			ScriptWord.DIVISION, PriorityTable.DIVISION, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeVector(
			ScriptWord.REMAINDER, PriorityTable.REMAINDER, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeVectorCast(
			ScriptWord.PLUS, PriorityTable.ADDITION, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorCast(
			ScriptWord.MINUS, PriorityTable.SUBTRACTION, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorCast(
			ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorCast(
			ScriptWord.DIVISION, PriorityTable.DIVISION, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorCast(
			ScriptWord.REMAINDER, PriorityTable.REMAINDER, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(
			ScriptWord.PLUS, PriorityTable.ADDITION, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(
			ScriptWord.MINUS, PriorityTable.SUBTRACTION, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(
			ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(
			ScriptWord.DIVISION, PriorityTable.DIVISION, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(
			ScriptWord.REMAINDER, PriorityTable.REMAINDER, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(
			ScriptWord.PLUS, PriorityTable.ADDITION, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(
			ScriptWord.MINUS, PriorityTable.SUBTRACTION, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(
			ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(
			ScriptWord.DIVISION, PriorityTable.DIVISION, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(
			ScriptWord.REMAINDER, PriorityTable.REMAINDER, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(
			ScriptWord.PLUS, PriorityTable.ADDITION, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(
			ScriptWord.MINUS, PriorityTable.SUBTRACTION, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(
			ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(
			ScriptWord.DIVISION, PriorityTable.DIVISION, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(
			ScriptWord.REMAINDER, PriorityTable.REMAINDER, OperationCode.REM
		);
	}
	public void testGenerateArithmeticBinaryOperatorCodeScalar(String symbol, int priority, OperationCode operationCode) {

		//「 1 symbol 2; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DataTypeName.INT, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DataTypeName.INT, RANK_OF_SCALAR
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createLiteralNode("1", DataTypeName.INT, RANK_OF_SCALAR),
			this.createLiteralNode("2", DataTypeName.INT, RANK_OF_SCALAR),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// コード生成
		String generatedCode = new CodeGenerator().generate(rootNode);

		/* 期待コードを用意（内容は下記コメントの通り、ただし空白幅は見やすいよう調整）

		  #META  "line=123, file=Test.vnano";
		      ALLOC   int    R0;
		      ???     int    R0    ~int:1    ~int:2;   (???の箇所に算術演算の命令コードが入る)
		 */
		String expectedCode = META_DIRECTIVE + EOI
			+ IND + OperationCode.ALLOC + WS + DataTypeName.INT + WS + R + "0" + EOI
			+ IND + operationCode + WS + DataTypeName.INT + WS + R + "0" + WS + IINT + VS + "1" + WS + IINT + VS + "2" + EOI;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode, generatedCode);
	}
	public void testGenerateArithmeticBinaryOperatorCodeScalarCast(String symbol, int priority, OperationCode operationCode) {

		//「 1 symbol 2; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DataTypeName.FLOAT, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DataTypeName.FLOAT, RANK_OF_SCALAR
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createLiteralNode("1", DataTypeName.FLOAT, RANK_OF_SCALAR),
			this.createLiteralNode("2", DataTypeName.INT, RANK_OF_SCALAR),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// コード生成
		String generatedCode = new CodeGenerator().generate(rootNode);

		/* 期待コードを用意（内容は下記コメントの通り、ただし空白幅は見やすいよう調整）

		  #META  "line=123, file=Test.vnano";
		      ALLOC   float      R1;
		      CAST    float:int  R1    ~int:2;
		      ALLOC   float      R0;
		      ???     float      R0    ~int:1    ~int:2;   (???の箇所に算術演算の命令コードが入る)
		 */
		String expectedCode = META_DIRECTIVE + EOI
			+ IND + OperationCode.ALLOC + WS + DataTypeName.FLOAT + WS + R + "1" + EOI
			+ IND + OperationCode.CAST + WS + DataTypeName.FLOAT + VS + DataTypeName.INT + WS + R + "1" + WS + IINT + VS + "2" + EOI
			+ IND + OperationCode.ALLOC + WS + DataTypeName.FLOAT + WS + R + "0" + EOI
			+ IND + operationCode + WS + DataTypeName.FLOAT + WS + R + "0" + WS + IFLOAT + VS + "1" + WS + R + "1" + EOI;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode, generatedCode);
	}
	public void testGenerateArithmeticBinaryOperatorCodeVector(String symbol, int priority, OperationCode operationCode) {

		//「 intVectorA symbol intVectorB; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DataTypeName.INT, 1);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DataTypeName.INT, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intVectorA", DataTypeName.INT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intVectorB", DataTypeName.INT, 1, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// コード生成
		String generatedCode = new CodeGenerator().generate(rootNode);

		/* 期待コードを用意（内容は下記コメントの通り、ただし空白幅は見やすいよう調整）

		  #GLOBAL	_intVectorA
		  #GLOBAL	_intVectorB
		  #META  "line=123, file=Test.vnano";
		      LEN     int    R1    _intVectorA;
		      ALLOC   int    R0    R1;
		      ???     int    R0    _intVectorA    _intVectorB;   (???の箇所に算術演算の命令コードが入る)
		 */
		String expectedCode = GLOBAL_DIRECTIVE_IVA + EOI + GLOBAL_DIRECTIVE_IVB + EOI + META_DIRECTIVE + EOI
			+ IND + OperationCode.LEN + WS + DataTypeName.INT + WS + R + 1 + WS + IVA + EOI
			+ IND + OperationCode.ALLOC + WS + DataTypeName.INT + WS + R + "0" + WS + R + 1 + EOI
			+ IND + operationCode + WS + DataTypeName.INT + WS + R + "0" + WS + IVA + WS + IVB + EOI;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode, generatedCode);
	}

	public void testGenerateArithmeticBinaryOperatorCodeVectorCast(String symbol, int priority, OperationCode operationCode) {

		//「 floatVectorA symbol intVectorB; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DataTypeName.FLOAT, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DataTypeName.FLOAT, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("floatVectorA", DataTypeName.FLOAT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intVectorB", DataTypeName.INT, 1, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// コード生成
		String generatedCode = new CodeGenerator().generate(rootNode);

		/* 期待コードを用意（内容は下記コメントの通り、ただし空白幅は見やすいよう調整）

		  #GLOBAL	_floatVectorA
		  #GLOBAL	_intVectorB
		  #META  "line=123, file=Test.vnano";
		      LEN     int        R1    _intVectorB;
		      ALLOC   float      R2    R1;
		      CAST    float:int  R2    _intVectorB;
		      LEN     int        R3    _floatVectorA;
		      ALLOC   float      R0    R3;
		      ???     float      R0    _floatVectorA    R2;   (???の箇所に算術演算の命令コードが入る)
		 */
		String expectedCode = GLOBAL_DIRECTIVE_FVA + EOI + GLOBAL_DIRECTIVE_IVB + EOI + META_DIRECTIVE + EOI
			+ IND + OperationCode.LEN + WS + DataTypeName.INT + WS + R + "1" + WS + IVB + EOI
			+ IND + OperationCode.ALLOC + WS + DataTypeName.FLOAT + WS + R + "2" + WS + R + "1" + EOI
			+ IND + OperationCode.CAST + WS + DataTypeName.FLOAT + VS + DataTypeName.INT + WS + R + "2" + WS + IVB + EOI
			+ IND + OperationCode.LEN + WS + DataTypeName.INT + WS + R + "3" + WS + FVA + EOI
			+ IND + OperationCode.ALLOC + WS + DataTypeName.FLOAT + WS + R + "0" + WS + R + "3" + EOI
			+ IND + operationCode + WS + DataTypeName.FLOAT + WS + R + "0" + WS + FVA + WS + R + "2" + EOI;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode, generatedCode);
	}
	public void testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(String symbol, int priority, OperationCode operationCode) {

		//「 intVectorA symbol floatVectorB; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DataTypeName.INT, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DataTypeName.INT, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intVectorA", DataTypeName.INT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intScalarB", DataTypeName.INT, RANK_OF_SCALAR, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// コード生成
		String generatedCode = new CodeGenerator().generate(rootNode);

		/* 期待コードを用意（内容は下記コメントの通り、ただし空白幅は見やすいよう調整）

		  #GLOBAL	_floatVectorA
		  #GLOBAL	_intVectorB
		  #META  "line=123, file=Test.vnano";
		      LEN     int        R1    _intVectorA;
		      ALLOC   int        R2    R1;
		      FILL    int        R2    _intScalarB;
		      LEN     int        R3    _intVectorA;
		      ALLOC   int        R0    R3;
		      ???     int        R0    _intVectorA    R2;   (???の箇所に算術演算の命令コードが入る)
		 */
		String expectedCode = GLOBAL_DIRECTIVE_IVA + EOI + GLOBAL_DIRECTIVE_ISB + EOI + META_DIRECTIVE + EOI
			+ IND + OperationCode.LEN + WS + DataTypeName.INT + WS + R + "1" + WS + IVA + EOI
			+ IND + OperationCode.ALLOC + WS + DataTypeName.INT + WS + R + "2" + WS + R + "1" + EOI
			+ IND + OperationCode.FILL + WS + DataTypeName.INT + WS + R + "2" + WS + ISB + EOI
			+ IND + OperationCode.LEN + WS + DataTypeName.INT + WS + R + "3" + WS + IVA + EOI
			+ IND + OperationCode.ALLOC + WS + DataTypeName.INT + WS + R + "0" + WS + R + "3" + EOI
			+ IND + operationCode + WS + DataTypeName.INT + WS + R + "0" + WS + IVA + WS + R + "2" + EOI;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode, generatedCode);
	}
	public void testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(String symbol, int priority, OperationCode operationCode) {

		//「 intVectorA symbol floatScalarB; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DataTypeName.FLOAT, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DataTypeName.FLOAT, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intVectorA", DataTypeName.INT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("floatScalarB", DataTypeName.FLOAT, RANK_OF_SCALAR, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// コード生成
		String generatedCode = new CodeGenerator().generate(rootNode);

		/* 期待コードを用意（内容は下記コメントの通り、ただし空白幅は見やすいよう調整）

		  #GLOBAL	_floatVectorA
		  #GLOBAL	_intVectorB
		  #META  "line=123, file=Test.vnano";
		      LEN     int        R1    _intVectorA;
		      ALLOC   float      R2    R1;
		      CAST    float:int  R2    _intVectorA;
		      LEN     int        R3    _intVectorA;
		      ALLOC   float      R4    R3;
		      FILL    float      R4    _floatScalarB;
		      LEN     int        R5    _intVectorA;
		      ALLOC   float      R0    R5;
		      ???     float      R0    R2    R4;   (???の箇所に算術演算の命令コードが入る)
		 */
		String expectedCode = GLOBAL_DIRECTIVE_IVA + EOI + GLOBAL_DIRECTIVE_FSB + EOI + META_DIRECTIVE + EOI
			+ IND + OperationCode.LEN + WS + DataTypeName.INT + WS + R + "1" + WS + IVA + EOI
			+ IND + OperationCode.ALLOC + WS + DataTypeName.FLOAT + WS + R + "2" + WS + R + "1" + EOI
			+ IND + OperationCode.CAST + WS + DataTypeName.FLOAT + VS + DataTypeName.INT + WS + R + "2" + WS + IVA + EOI
			+ IND + OperationCode.LEN + WS + DataTypeName.INT + WS + R + "3" + WS + IVA + EOI
			+ IND + OperationCode.ALLOC + WS + DataTypeName.FLOAT + WS + R + "4" + WS + R + "3" + EOI
			+ IND + OperationCode.FILL + WS + DataTypeName.FLOAT + WS + R + "4" + WS + FSB + EOI
			+ IND + OperationCode.LEN + WS + DataTypeName.INT + WS + R + "5" + WS + IVA + EOI
			+ IND + OperationCode.ALLOC + WS + DataTypeName.FLOAT + WS + R + "0" + WS + R + "5" + EOI
			+ IND + operationCode + WS + DataTypeName.FLOAT + WS + R + "0" + WS + R + "2" + WS + R + "4" + EOI;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode, generatedCode);
	}
	public void testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(String symbol, int priority, OperationCode operationCode) {

		//「 floatVectorA symbol intScalarB; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DataTypeName.FLOAT, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DataTypeName.FLOAT, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("floatVectorA", DataTypeName.FLOAT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intScalarB", DataTypeName.INT, RANK_OF_SCALAR, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// コード生成
		String generatedCode = new CodeGenerator().generate(rootNode);

		/* 期待コードを用意（内容は下記コメントの通り、ただし空白幅は見やすいよう調整）

		  #GLOBAL	_floatVectorA
		  #GLOBAL	_intVectorB
		  #META  "line=123, file=Test.vnano";
		      LEN     int        R1;
		      CAST    float:int  R1    _intScalarB;
		      LEN     int        R2    _floatVectorA;
		      ALLOC   float      R3    R2;
              FILL 	  float      R3    R1;
		      LEN     int        R4    _floatVectorA;
		      ALLOC   float      R0    R4;
		      ???     float      R0    _floatVectorA    R3;   (???の箇所に算術演算の命令コードが入る)
		 */
		String expectedCode = GLOBAL_DIRECTIVE_FVA + EOI + GLOBAL_DIRECTIVE_ISB + EOI + META_DIRECTIVE + EOI
			+ IND + OperationCode.ALLOC + WS + DataTypeName.FLOAT + WS + R + "1" + EOI
			+ IND + OperationCode.CAST + WS + DataTypeName.FLOAT + VS + DataTypeName.INT + WS + R + "1" + WS + ISB + EOI
			+ IND + OperationCode.LEN + WS + DataTypeName.INT + WS + R + "2" + WS + FVA + EOI
			+ IND + OperationCode.ALLOC + WS + DataTypeName.FLOAT + WS + R + "3" + WS + R + "2" + EOI
			+ IND + OperationCode.FILL + WS + DataTypeName.FLOAT + WS + R + "3" + WS + R + "1" + EOI
			+ IND + OperationCode.LEN + WS + DataTypeName.INT + WS + R + "4" + WS + FVA + EOI
			+ IND + OperationCode.ALLOC + WS + DataTypeName.FLOAT + WS + R + "0" + WS + R + "4" + EOI
			+ IND + operationCode + WS + DataTypeName.FLOAT + WS + R + "0" + WS + FVA + WS + R + "3" + EOI;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode, generatedCode);
	}

	// 算術複合代入演算子のテスト
	@Test
	public void testGenerateArithmeticCompoundAssignmentOperatorCode() {

		this.testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(
			ScriptWord.ADDITION_ASSIGNMENT, PriorityTable.ADDITION_ASSIGNMENT, OperationCode.ADD
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(
			ScriptWord.SUBTRACTION_ASSIGNMENT, PriorityTable.SUBTRACTION_ASSIGNMENT, OperationCode.SUB
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(
			ScriptWord.MULTIPLICATION_ASSIGNMENT, PriorityTable.MULTIPLICATION_ASSIGNMENT, OperationCode.MUL
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(
			ScriptWord.DIVISION_ASSIGNMENT, PriorityTable.DIVISION_ASSIGNMENT, OperationCode.DIV
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(
			ScriptWord.REMAINDER_ASSIGNMENT, PriorityTable.REMAINDER_ASSIGNMENT, OperationCode.REM
		);

		this.testGenerateArithmeticCompoundAssignmentOperatorCodeVector(
			ScriptWord.ADDITION_ASSIGNMENT, PriorityTable.ADDITION_ASSIGNMENT, OperationCode.ADD
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeVector(
			ScriptWord.SUBTRACTION_ASSIGNMENT, PriorityTable.SUBTRACTION_ASSIGNMENT, OperationCode.SUB
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeVector(
			ScriptWord.MULTIPLICATION_ASSIGNMENT, PriorityTable.MULTIPLICATION_ASSIGNMENT, OperationCode.MUL
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeVector(
			ScriptWord.DIVISION_ASSIGNMENT, PriorityTable.DIVISION_ASSIGNMENT, OperationCode.DIV
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeVector(
			ScriptWord.REMAINDER_ASSIGNMENT, PriorityTable.REMAINDER_ASSIGNMENT, OperationCode.REM
		);
	}
	public void testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(String symbol, int priority, OperationCode operationCode) {

		//「 intScalarA symbol intScalarB; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DataTypeName.INT, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT, DataTypeName.INT, 0
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intScalarA", DataTypeName.INT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intScalarB", DataTypeName.INT, 1, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// コード生成
		String generatedCode = new CodeGenerator().generate(rootNode);

		/* 期待コードを用意（内容は下記コメントの通り、ただし空白幅は見やすいよう調整）

		  #META  "line=123, file=Test.vnano";
		      ???     int    _intScalarA    _intScalarA    _intScalarB;   (???の箇所に算術演算の命令コードが入る)
		 */
		String expectedCode = GLOBAL_DIRECTIVE_ISA + EOI + GLOBAL_DIRECTIVE_ISB + EOI + META_DIRECTIVE + EOI
			+ IND + operationCode + WS + DataTypeName.INT + WS + ISA + WS + ISA + WS + ISB + EOI;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode, generatedCode);
	}
	public void testGenerateArithmeticCompoundAssignmentOperatorCodeVector(String symbol, int priority, OperationCode operationCode) {

		//「 intVectorA symbol intVectorB; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DataTypeName.INT, 1);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT, DataTypeName.INT, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intVectorA", DataTypeName.INT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intVectorB", DataTypeName.INT, 1, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// コード生成
		String generatedCode = new CodeGenerator().generate(rootNode);

		/* 期待コードを用意（内容は下記コメントの通り、ただし空白幅は見やすいよう調整）

		  #GLOBAL	_intVectorA
		  #GLOBAL	_intVectorB
		  #META  "line=123, file=Test.vnano";
		      ???     int    _intVectorA    _intVectorA    _intVectorB;   (???の箇所に算術演算の命令コードが入る)
		 */
		String expectedCode = GLOBAL_DIRECTIVE_IVA + EOI + GLOBAL_DIRECTIVE_IVB + EOI + META_DIRECTIVE + EOI
			+ IND + operationCode + WS + DataTypeName.INT + WS + IVA + WS + IVA + WS + IVB + EOI;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode, generatedCode);
	}


}
