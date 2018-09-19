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

	private static final String META_DIRECTIVE = "#META\t\"line=123, file=Test.vnano\""; // メタ情報行の内容
	private static final String EOI = AssemblyWord.INSTRUCTION_SEPARATOR + AssemblyWord.LINE_SEPARATOR; // 命令末尾記号+改行
	private static final String WS = AssemblyWord.WORD_SEPARATOR;
	private static final String VS = AssemblyWord.VALUE_SEPARATOR;
	private static final String IND = AssemblyWord.INDENT;
	private static final String IINT = AssemblyWord.OPERAND_PREFIX_IMMEDIATE + DataTypeName.INT;
	private static final String IFLOAT = AssemblyWord.OPERAND_PREFIX_IMMEDIATE + DataTypeName.FLOAT;

	private static final char R = AssemblyWord.OPERAND_PREFIX_REGISTER;
	private static final String IVA = AssemblyWord.OPERAND_PREFIX_IDENTIFIER + "intVecA";
	private static final String IVB = AssemblyWord.OPERAND_PREFIX_IDENTIFIER + "intVecB";
	//private static final String FVA = AssemblyWord.OPERAND_PREFIX_IDENTIFIER + "floatVecA";
	//private static final String FVB = AssemblyWord.OPERAND_PREFIX_IDENTIFIER + "floatVecB";
	//private static final String BVA = AssemblyWord.OPERAND_PREFIX_IDENTIFIER + "boolVecA";
	//private static final String BVB = AssemblyWord.OPERAND_PREFIX_IDENTIFIER + "boolVecB";

	private static final String GLOBAL_DIRECTIVE_IVA = "#GLOBAL	_intVecA";
	private static final String GLOBAL_DIRECTIVE_IVB = "#GLOBAL	_intVecB";
	//private static final String GLOBAL_DIRECTIVE_FVA = "#GLOBAL	_floatVecA";
	//private static final String GLOBAL_DIRECTIVE_FVB = "#GLOBAL	_floatVecB";
	//private static final String GLOBAL_DIRECTIVE_BVA = "#GLOBAL	_boolVecA";
	//private static final String GLOBAL_DIRECTIVE_BVB = "#GLOBAL	_boolVecB";

	private Interconnect interconnect;

	@Before
	public void setUp() throws Exception {
		this.interconnect = new Interconnect();
		this.interconnect.connect(new Variable("intVecA", DataType.INT64, 1));
		this.interconnect.connect(new Variable("intVecB", DataType.INT64, 1));
		this.interconnect.connect(new Variable("floatVecA", DataType.FLOAT64, 1));
		this.interconnect.connect(new Variable("floatVecB", DataType.FLOAT64, 1));
		this.interconnect.connect(new Variable("boolVecA", DataType.BOOL, 1));
		this.interconnect.connect(new Variable("boolVecB", DataType.BOOL, 1));
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

		this.testGenerateArithmeticBinaryOperatorCodeScalar(ScriptWord.PLUS, PriorityTable.ADDITION, OperationCode.ADD);
		this.testGenerateArithmeticBinaryOperatorCodeScalar(ScriptWord.MINUS, PriorityTable.SUBTRACTION, OperationCode.SUB);
		this.testGenerateArithmeticBinaryOperatorCodeScalar(ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, OperationCode.MUL);
		this.testGenerateArithmeticBinaryOperatorCodeScalar(ScriptWord.DIVISION, PriorityTable.DIVISION, OperationCode.DIV);
		this.testGenerateArithmeticBinaryOperatorCodeScalar(ScriptWord.REMAINDER, PriorityTable.REMAINDER, OperationCode.REM);

		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(ScriptWord.PLUS, PriorityTable.ADDITION, OperationCode.ADD);
		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(ScriptWord.MINUS, PriorityTable.SUBTRACTION, OperationCode.SUB);
		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, OperationCode.MUL);
		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(ScriptWord.DIVISION, PriorityTable.DIVISION, OperationCode.DIV);
		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(ScriptWord.REMAINDER, PriorityTable.REMAINDER, OperationCode.REM);

		this.testGenerateArithmeticBinaryOperatorCodeVector(ScriptWord.PLUS, PriorityTable.ADDITION, OperationCode.ADD);
		this.testGenerateArithmeticBinaryOperatorCodeVector(ScriptWord.MINUS, PriorityTable.SUBTRACTION, OperationCode.SUB);
		this.testGenerateArithmeticBinaryOperatorCodeVector(ScriptWord.MULTIPLICATION, PriorityTable.MULTIPLICATION, OperationCode.MUL);
		this.testGenerateArithmeticBinaryOperatorCodeVector(ScriptWord.DIVISION, PriorityTable.DIVISION, OperationCode.DIV);
		this.testGenerateArithmeticBinaryOperatorCodeVector(ScriptWord.REMAINDER, PriorityTable.REMAINDER, OperationCode.REM);
	}
	public void testGenerateArithmeticBinaryOperatorCodeScalar(String symbol, int priority, OperationCode operationCode) {

		//「 1 symbol 2; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DataTypeName.INT, 0);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DataTypeName.INT, 0
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createLiteralNode("1", DataTypeName.INT, 0),
			this.createLiteralNode("2", DataTypeName.INT, 0),
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
		AstNode exprNode = this.createExpressionNode(DataTypeName.FLOAT, 0);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DataTypeName.FLOAT, 0
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createLiteralNode("1", DataTypeName.FLOAT, 0),
			this.createLiteralNode("2", DataTypeName.INT, 0),
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

		//「 1 symbol 2; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DataTypeName.INT, 0);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DataTypeName.INT, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intVecA", DataTypeName.INT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intVecB", DataTypeName.INT, 1, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// コード生成
		String generatedCode = new CodeGenerator().generate(rootNode);

		/* 期待コードを用意（内容は下記コメントの通り、ただし空白幅は見やすいよう調整）

		  #GLOBAL	_intVecB
		  #GLOBAL	_intVecA
		  #META  "line=123, file=Test.vnano";
		      LEN     int    R1    _vecIntA;
		      ALLOC   int    R0    R1;
		      ???     int    R0    _vecIntA    _vecIntB;   (???の箇所に算術演算の命令コードが入る)
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



}
