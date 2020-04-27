/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vcssl.nano.spec.AssemblyWord;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.LanguageSpecContainer;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.spec.OperatorPrecedence;
import org.vcssl.nano.spec.ScriptWord;

public class CodeGeneratorTest {

	private final LanguageSpecContainer LANG_SPEC = new LanguageSpecContainer();
	private final ScriptWord SCRIPT_WORD = LANG_SPEC.SCRIPT_WORD;
	private final AssemblyWord ASSEMBLY_WORD = LANG_SPEC.ASSEMBLY_WORD;
	private final OperatorPrecedence OPERATOR_PRECEDENCE = LANG_SPEC.OPERATOR_PRECEDENCE;
	private final DataTypeName DATA_TYPE_NAME = LANG_SPEC.DATA_TYPE_NAME;

	private static final int RANK_OF_SCALAR = 0;

	private final String EOI = ASSEMBLY_WORD.INSTRUCTION_SEPARATOR + ASSEMBLY_WORD.LINE_SEPARATOR; // 命令末尾記号+改行
	private final String WS = ASSEMBLY_WORD.WORD_SEPARATOR;
	private final String VS = ASSEMBLY_WORD.VALUE_SEPARATOR;
	private final String IND = ASSEMBLY_WORD.INDENT;
	private final String IINT = ASSEMBLY_WORD.OPERAND_PREFIX_IMMEDIATE + DATA_TYPE_NAME.INT;
	private final String IFLOAT = ASSEMBLY_WORD.OPERAND_PREFIX_IMMEDIATE + DATA_TYPE_NAME.FLOAT;

	private final char R = ASSEMBLY_WORD.OPERAND_PREFIX_REGISTER;
	private final String IVA = ASSEMBLY_WORD.OPERAND_PREFIX_IDENTIFIER + "intVectorA";
	private final String IVB = ASSEMBLY_WORD.OPERAND_PREFIX_IDENTIFIER + "intVectorB";
	private final String ISA = ASSEMBLY_WORD.OPERAND_PREFIX_IDENTIFIER + "intScalarA";
	private final String ISB = ASSEMBLY_WORD.OPERAND_PREFIX_IDENTIFIER + "intScalarB";
	private final String FVA = ASSEMBLY_WORD.OPERAND_PREFIX_IDENTIFIER + "floatVectorA";
	//private static final String FVB = ASSEMBLY_WORD.OPERAND_PREFIX_IDENTIFIER + "floatVectorB";
	//private static final String FSA = ASSEMBLY_WORD.OPERAND_PREFIX_IDENTIFIER + "floatScalarA";
	private final String FSB = ASSEMBLY_WORD.OPERAND_PREFIX_IDENTIFIER + "floatScalarB";
	//private static final String BVA = ASSEMBLY_WORD.OPERAND_PREFIX_IDENTIFIER + "boolVectorA";
	//private static final String BVB = ASSEMBLY_WORD.OPERAND_PREFIX_IDENTIFIER + "boolVectorB";

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

	private final String END_CODE = IND + OperationCode.END + WS + DATA_TYPE_NAME.VOID
	                                     + WS + ASSEMBLY_WORD.OPERAND_PREFIX_PLACEHOLDER + EOI;

	// テストコードの先頭に常に付く定型コード（バージョン情報など）
	private final String HEADER =
			ASSEMBLY_WORD.ASSEMBLY_LANGUAGE_IDENTIFIER_DIRECTIVE + ASSEMBLY_WORD.WORD_SEPARATOR
			+
			"\"" + ASSEMBLY_WORD.ASSEMBLY_LANGUAGE_NAME + "\"" + EOI
			+
			ASSEMBLY_WORD.ASSEMBLY_LANGUAGE_VERSION_DIRECTIVE + ASSEMBLY_WORD.WORD_SEPARATOR
			+
			"\"" + ASSEMBLY_WORD.ASSEMBLY_LANGUAGE_VERSION + "\"" + EOI
			+
			ASSEMBLY_WORD.SCRIPT_LANGUAGE_IDENTIFIER_DIRECTIVE + ASSEMBLY_WORD.WORD_SEPARATOR
			+
			"\"" + SCRIPT_WORD.SCRIPT_LANGUAGE_NAME + "\"" + EOI
			+
			ASSEMBLY_WORD.SCRIPT_LANGUAGE_VERSION_DIRECTIVE + ASSEMBLY_WORD.WORD_SEPARATOR
			+
			"\"" + SCRIPT_WORD.SCRIPT_LANGUAGE_VERSION + "\"" + EOI
			+
			ASSEMBLY_WORD.LINE_SEPARATOR;

	// メタディレクティブコード
	private final String META = ASSEMBLY_WORD.LINE_SEPARATOR
			+ ASSEMBLY_WORD.META_DIRECTIVE + ASSEMBLY_WORD.WORD_SEPARATOR + "\"line=123, file=Test.vnano\"";


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
		node.setAttribute(AttributeKey.RANK, Integer.toString(rank));
		return node;
	}

	private AstNode createLiteralNode(String value, String dataTypeName, int rank) {
		AstNode node = new AstNode(AstNode.Type.LEAF, 123, "Test.vnano");
		node.setAttribute(AttributeKey.LEAF_TYPE, AttributeValue.LITERAL);
		node.setAttribute(AttributeKey.LITERAL_VALUE, value);
		node.setAttribute(AttributeKey.DATA_TYPE, dataTypeName);
		node.setAttribute(AttributeKey.RANK, Integer.toString(rank));
		return node;
	}

	private AstNode createVariableIdentifierNode(String identifier, String dataTypeName, int rank, String scope) {
		AstNode node = new AstNode(AstNode.Type.LEAF, 123, "Test.vnano");
		node.setAttribute(AttributeKey.LEAF_TYPE, AttributeValue.VARIABLE_IDENTIFIER);
		node.setAttribute(AttributeKey.IDENTIFIER_VALUE, identifier);
		node.setAttribute(AttributeKey.DATA_TYPE, dataTypeName);
		node.setAttribute(AttributeKey.RANK, Integer.toString(rank));
		node.setAttribute(AttributeKey.SCOPE, scope);
		return node;
	}


	// 代入演算子のテスト
	@Test
	public void testGenerateAssignmentOperatorCode() {
		this.testGenerateAssignmentOperatorCodeVector();
	}

	public void testGenerateAssignmentOperatorCodeVector() {

		//「 intVectorA = intVectorB; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DATA_TYPE_NAME.INT, 1);
		AstNode operatorNode = this.createOperatorNode(
				SCRIPT_WORD.ASSIGNMENT, OPERATOR_PRECEDENCE.ASSIGNMENT, AttributeValue.BINARY,
				AttributeValue.ASSIGNMENT, DATA_TYPE_NAME.INT, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intVectorA", DATA_TYPE_NAME.INT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intVectorB", DATA_TYPE_NAME.INT, 1, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// コード生成
		String generatedCode = new CodeGenerator(LANG_SPEC).generate(rootNode);

		/* 期待コードを用意（内容は下記コメントの通り、ただし空白幅は見やすいよう調整）

		  #GLOBAL	_intVectorA
		  #GLOBAL	_intVectorB
		  #META  "line=123, file=Test.vnano";
		      ALLOCR  int    _intVectorA    _intVectorB;
		      MOV     int    _intVectorA    _intVectorB;
		      END     void    -;
		 */
		String expectedCode = HEADER + GLOBAL_DIRECTIVE_IVA + EOI + GLOBAL_DIRECTIVE_IVB + EOI + META + EOI
			+ IND + OperationCode.ALLOCR + WS + DATA_TYPE_NAME.INT + WS + IVA + WS + IVB + EOI
			+ IND + OperationCode.MOV + WS + DATA_TYPE_NAME.INT + WS + IVA + WS + IVB + EOI
			+ END_CODE;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode.replace(ASSEMBLY_WORD.LINE_SEPARATOR,""), generatedCode.replace(ASSEMBLY_WORD.LINE_SEPARATOR,""));
	}


	// 算術二項演算子のテスト
	@Test
	public void testGenerateArithmeticBinaryOperatorCode() {

		this.testGenerateArithmeticBinaryOperatorCodeScalar(
			SCRIPT_WORD.PLUS, OPERATOR_PRECEDENCE.ADDITION, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalar(
			SCRIPT_WORD.MINUS, OPERATOR_PRECEDENCE.SUBTRACTION, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalar(
			SCRIPT_WORD.MULTIPLICATION, OPERATOR_PRECEDENCE.MULTIPLICATION, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalar(
			SCRIPT_WORD.DIVISION, OPERATOR_PRECEDENCE.DIVISION, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalar(
			SCRIPT_WORD.REMAINDER, OPERATOR_PRECEDENCE.REMAINDER, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(
			SCRIPT_WORD.PLUS, OPERATOR_PRECEDENCE.ADDITION, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(
			SCRIPT_WORD.MINUS, OPERATOR_PRECEDENCE.SUBTRACTION, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(
			SCRIPT_WORD.MULTIPLICATION, OPERATOR_PRECEDENCE.MULTIPLICATION, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(
			SCRIPT_WORD.DIVISION, OPERATOR_PRECEDENCE.DIVISION, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(
			SCRIPT_WORD.REMAINDER, OPERATOR_PRECEDENCE.REMAINDER, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeVector(
			SCRIPT_WORD.PLUS, OPERATOR_PRECEDENCE.ADDITION, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeVector(
			SCRIPT_WORD.MINUS, OPERATOR_PRECEDENCE.SUBTRACTION, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeVector(
			SCRIPT_WORD.MULTIPLICATION, OPERATOR_PRECEDENCE.MULTIPLICATION, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeVector(
			SCRIPT_WORD.DIVISION, OPERATOR_PRECEDENCE.DIVISION, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeVector(
			SCRIPT_WORD.REMAINDER, OPERATOR_PRECEDENCE.REMAINDER, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeVectorCast(
			SCRIPT_WORD.PLUS, OPERATOR_PRECEDENCE.ADDITION, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorCast(
			SCRIPT_WORD.MINUS, OPERATOR_PRECEDENCE.SUBTRACTION, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorCast(
			SCRIPT_WORD.MULTIPLICATION, OPERATOR_PRECEDENCE.MULTIPLICATION, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorCast(
			SCRIPT_WORD.DIVISION, OPERATOR_PRECEDENCE.DIVISION, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorCast(
			SCRIPT_WORD.REMAINDER, OPERATOR_PRECEDENCE.REMAINDER, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(
			SCRIPT_WORD.PLUS, OPERATOR_PRECEDENCE.ADDITION, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(
			SCRIPT_WORD.MINUS, OPERATOR_PRECEDENCE.SUBTRACTION, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(
			SCRIPT_WORD.MULTIPLICATION, OPERATOR_PRECEDENCE.MULTIPLICATION, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(
			SCRIPT_WORD.DIVISION, OPERATOR_PRECEDENCE.DIVISION, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(
			SCRIPT_WORD.REMAINDER, OPERATOR_PRECEDENCE.REMAINDER, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(
			SCRIPT_WORD.PLUS, OPERATOR_PRECEDENCE.ADDITION, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(
			SCRIPT_WORD.MINUS, OPERATOR_PRECEDENCE.SUBTRACTION, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(
			SCRIPT_WORD.MULTIPLICATION, OPERATOR_PRECEDENCE.MULTIPLICATION, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(
			SCRIPT_WORD.DIVISION, OPERATOR_PRECEDENCE.DIVISION, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(
			SCRIPT_WORD.REMAINDER, OPERATOR_PRECEDENCE.REMAINDER, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(
			SCRIPT_WORD.PLUS, OPERATOR_PRECEDENCE.ADDITION, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(
			SCRIPT_WORD.MINUS, OPERATOR_PRECEDENCE.SUBTRACTION, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(
			SCRIPT_WORD.MULTIPLICATION, OPERATOR_PRECEDENCE.MULTIPLICATION, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(
			SCRIPT_WORD.DIVISION, OPERATOR_PRECEDENCE.DIVISION, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(
			SCRIPT_WORD.REMAINDER, OPERATOR_PRECEDENCE.REMAINDER, OperationCode.REM
		);
	}


	public void testGenerateArithmeticBinaryOperatorCodeScalar(String symbol, int priority, OperationCode operationCode) {

		//「 1 symbol 2; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DATA_TYPE_NAME.INT, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DATA_TYPE_NAME.INT, RANK_OF_SCALAR
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createLiteralNode("1", DATA_TYPE_NAME.INT, RANK_OF_SCALAR),
			this.createLiteralNode("2", DATA_TYPE_NAME.INT, RANK_OF_SCALAR),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// コード生成
		String generatedCode = new CodeGenerator(LANG_SPEC).generate(rootNode);

		/* 期待コードを用意（内容は下記コメントの通り、ただし空白幅は見やすいよう調整）

		  #META  "line=123, file=Test.vnano";
		      ALLOC   int    R0;
		      ???     int    R0    ~int:1    ~int:2;   (???の箇所に算術演算の命令コードが入る)
		      END     void    -;
		 */
		String expectedCode = HEADER + META + EOI
			+ IND + OperationCode.ALLOC + WS + DATA_TYPE_NAME.INT + WS + R + "0" + EOI
			+ IND + operationCode + WS + DATA_TYPE_NAME.INT + WS + R + "0" + WS + IINT + VS + "1" + WS + IINT + VS + "2" + EOI
			+ END_CODE;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode.replace(ASSEMBLY_WORD.LINE_SEPARATOR,""), generatedCode.replace(ASSEMBLY_WORD.LINE_SEPARATOR,""));
	}
	public void testGenerateArithmeticBinaryOperatorCodeScalarCast(String symbol, int priority, OperationCode operationCode) {

		//「 1 symbol 2; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DATA_TYPE_NAME.FLOAT, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DATA_TYPE_NAME.FLOAT, RANK_OF_SCALAR
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createLiteralNode("1", DATA_TYPE_NAME.FLOAT, RANK_OF_SCALAR),
			this.createLiteralNode("2", DATA_TYPE_NAME.INT, RANK_OF_SCALAR),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// コード生成
		String generatedCode = new CodeGenerator(LANG_SPEC).generate(rootNode);

		/* 期待コードを用意（内容は下記コメントの通り、ただし空白幅は見やすいよう調整）

		  #META  "line=123, file=Test.vnano";
		      ALLOC   float      R1;
		      CAST    float:int  R1    ~int:2;
		      ALLOC   float      R0;
		      ???     float      R0    ~float:1    ~int:2;   (???の箇所に算術演算の命令コードが入る)
		      END     void    -;
		 */
		String expectedCode = HEADER + META + EOI
			+ IND + OperationCode.ALLOC + WS + DATA_TYPE_NAME.FLOAT + WS + R + "1" + EOI
			+ IND + OperationCode.CAST + WS + DATA_TYPE_NAME.FLOAT + VS + DATA_TYPE_NAME.INT + WS + R + "1" + WS + IINT + VS + "2" + EOI
			+ IND + OperationCode.ALLOC + WS + DATA_TYPE_NAME.FLOAT + WS + R + "0" + EOI
			+ IND + operationCode + WS + DATA_TYPE_NAME.FLOAT + WS + R + "0" + WS + IFLOAT + VS + "1" + WS + R + "1" + EOI
			+ END_CODE;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode.replace(ASSEMBLY_WORD.LINE_SEPARATOR,""), generatedCode.replace(ASSEMBLY_WORD.LINE_SEPARATOR,""));
	}
	public void testGenerateArithmeticBinaryOperatorCodeVector(String symbol, int priority, OperationCode operationCode) {

		//「 intVectorA symbol intVectorB; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DATA_TYPE_NAME.INT, 1);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DATA_TYPE_NAME.INT, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intVectorA", DATA_TYPE_NAME.INT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intVectorB", DATA_TYPE_NAME.INT, 1, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// コード生成
		String generatedCode = new CodeGenerator(LANG_SPEC).generate(rootNode);

		/* 期待コードを用意（内容は下記コメントの通り、ただし空白幅は見やすいよう調整）

		  #GLOBAL	_intVectorA
		  #GLOBAL	_intVectorB
		  #META  "line=123, file=Test.vnano";
		      ALLOCR  int    R0    _intVectorA;
		      ???     int    R0    _intVectorA    _intVectorB;   (???の箇所に算術演算の命令コードが入る)
		      END     void    -;
		 */
		String expectedCode = HEADER + GLOBAL_DIRECTIVE_IVA + EOI + GLOBAL_DIRECTIVE_IVB + EOI + META + EOI
			+ IND + OperationCode.ALLOCR + WS + DATA_TYPE_NAME.INT + WS + R + "0" + WS + IVA + EOI
			+ IND + operationCode + WS + DATA_TYPE_NAME.INT + WS + R + "0" + WS + IVA + WS + IVB + EOI
			+ END_CODE;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode.replace(ASSEMBLY_WORD.LINE_SEPARATOR,""), generatedCode.replace(ASSEMBLY_WORD.LINE_SEPARATOR,""));
	}

	public void testGenerateArithmeticBinaryOperatorCodeVectorCast(String symbol, int priority, OperationCode operationCode) {

		//「 floatVectorA symbol intVectorB; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DATA_TYPE_NAME.FLOAT, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DATA_TYPE_NAME.FLOAT, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("floatVectorA", DATA_TYPE_NAME.FLOAT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intVectorB", DATA_TYPE_NAME.INT, 1, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// コード生成
		String generatedCode = new CodeGenerator(LANG_SPEC).generate(rootNode);

		/* 期待コードを用意（内容は下記コメントの通り、ただし空白幅は見やすいよう調整）

		  #GLOBAL	_floatVectorA
		  #GLOBAL	_intVectorB
		  #META  "line=123, file=Test.vnano";
		      ALLOCR  float      R1    _intVectorB;
		      CAST    float:int  R1    _intVectorB;
		      ALLOCR  float      R0    _floatVectorA;
		      ???     float      R0    _floatVectorA    R1;   (???の箇所に算術演算の命令コードが入る)
		      END     void    -;
		 */
		String expectedCode = HEADER + GLOBAL_DIRECTIVE_FVA + EOI + GLOBAL_DIRECTIVE_IVB + EOI + META + EOI
			+ IND + OperationCode.ALLOCR + WS + DATA_TYPE_NAME.FLOAT + WS + R + "1" + WS + IVB + EOI
			+ IND + OperationCode.CAST + WS + DATA_TYPE_NAME.FLOAT + VS + DATA_TYPE_NAME.INT + WS + R + "1" + WS + IVB + EOI
			+ IND + OperationCode.ALLOCR + WS + DATA_TYPE_NAME.FLOAT + WS + R + "0" + WS + FVA + EOI
			+ IND + operationCode + WS + DATA_TYPE_NAME.FLOAT + WS + R + "0" + WS + FVA + WS + R + "1" + EOI
			+ END_CODE;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode.replace(ASSEMBLY_WORD.LINE_SEPARATOR,""), generatedCode.replace(ASSEMBLY_WORD.LINE_SEPARATOR,""));
	}
	public void testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(String symbol, int priority, OperationCode operationCode) {

		//「 intVectorA symbol floatVectorB; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DATA_TYPE_NAME.INT, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DATA_TYPE_NAME.INT, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intVectorA", DATA_TYPE_NAME.INT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intScalarB", DATA_TYPE_NAME.INT, RANK_OF_SCALAR, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// コード生成
		String generatedCode = new CodeGenerator(LANG_SPEC).generate(rootNode);

		/* 期待コードを用意（内容は下記コメントの通り、ただし空白幅は見やすいよう調整）

		  #GLOBAL	_floatVectorA
		  #GLOBAL	_intVectorB
		  #META  "line=123, file=Test.vnano";
		      ALLOCR  int        R1    _intVectorA;
              FILL    int        R1    _intScalarB;
		      ALLOCR  int        R0    _intVectorA;
		      ???     int        R0    _intVectorA    R1;   (???の箇所に算術演算の命令コードが入る)
		      END     void    -;
		 */
		String expectedCode = HEADER + GLOBAL_DIRECTIVE_IVA + EOI + GLOBAL_DIRECTIVE_ISB + EOI + META + EOI
			+ IND + OperationCode.ALLOCR + WS + DATA_TYPE_NAME.INT + WS + R + "1" + WS + IVA + EOI
			+ IND + OperationCode.FILL + WS + DATA_TYPE_NAME.INT + WS + R + "1" + WS + ISB + EOI
			+ IND + OperationCode.ALLOCR + WS + DATA_TYPE_NAME.INT + WS + R + "0" + WS + IVA + EOI
			+ IND + operationCode + WS + DATA_TYPE_NAME.INT + WS + R + "0" + WS + IVA + WS + R + "1" + EOI
			+ END_CODE;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode.replace(ASSEMBLY_WORD.LINE_SEPARATOR,""), generatedCode.replace(ASSEMBLY_WORD.LINE_SEPARATOR,""));
	}
	public void testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(String symbol, int priority, OperationCode operationCode) {

		//「 intVectorA symbol floatScalarB; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DATA_TYPE_NAME.FLOAT, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DATA_TYPE_NAME.FLOAT, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intVectorA", DATA_TYPE_NAME.INT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("floatScalarB", DATA_TYPE_NAME.FLOAT, RANK_OF_SCALAR, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// コード生成
		String generatedCode = new CodeGenerator(LANG_SPEC).generate(rootNode);

		/* 期待コードを用意（内容は下記コメントの通り、ただし空白幅は見やすいよう調整）

		  #GLOBAL	_floatVectorA
		  #GLOBAL	_intVectorB
		  #META  "line=123, file=Test.vnano";
		      ALLOCR  float      R1    _intVectorA;
		      CAST    float:int  R1    _intVectorA;
		      ALLOCR  float      R2    _intVectorA;
              FILL    float      R2    _floatScalarB;
		      ALLOCR  float      R0    _intVectorA;
		      ???     float      R0    R1    R2;   (???の箇所に算術演算の命令コードが入る)
		      END     void    -;
		 */
		String expectedCode = HEADER + GLOBAL_DIRECTIVE_IVA + EOI + GLOBAL_DIRECTIVE_FSB + EOI + META + EOI
			+ IND + OperationCode.ALLOCR + WS + DATA_TYPE_NAME.FLOAT + WS + R + "1" + WS + IVA + EOI
			+ IND + OperationCode.CAST + WS + DATA_TYPE_NAME.FLOAT + VS + DATA_TYPE_NAME.INT + WS + R + "1" + WS + IVA + EOI
			+ IND + OperationCode.ALLOCR + WS + DATA_TYPE_NAME.FLOAT + WS + R + "2" + WS + IVA + EOI
			+ IND + OperationCode.FILL + WS + DATA_TYPE_NAME.FLOAT + WS + R + "2" + WS + FSB + EOI
			+ IND + OperationCode.ALLOCR + WS + DATA_TYPE_NAME.FLOAT + WS + R + "0" + WS + IVA + EOI
			+ IND + operationCode + WS + DATA_TYPE_NAME.FLOAT + WS + R + "0" + WS + R + "1" + WS + R + "2" + EOI
			+ END_CODE;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode.replace(ASSEMBLY_WORD.LINE_SEPARATOR,""), generatedCode.replace(ASSEMBLY_WORD.LINE_SEPARATOR,""));
	}
	public void testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(String symbol, int priority, OperationCode operationCode) {

		//「 floatVectorA symbol intScalarB; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DATA_TYPE_NAME.FLOAT, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DATA_TYPE_NAME.FLOAT, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("floatVectorA", DATA_TYPE_NAME.FLOAT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intScalarB", DATA_TYPE_NAME.INT, RANK_OF_SCALAR, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// コード生成
		String generatedCode = new CodeGenerator(LANG_SPEC).generate(rootNode);

		/* 期待コードを用意（内容は下記コメントの通り、ただし空白幅は見やすいよう調整）

		  #GLOBAL	_floatVectorA
		  #GLOBAL	_intVectorB
		  #META  "line=123, file=Test.vnano";
		      ALLOC   int        R1;
		      CAST    float:int  R1    _intScalarB;
		      ALLOCR  float      R2    _floatVectorA;
              FILL 	  float      R2    R1;
		      ALLOCR  float      R0    _floatVectorA;
		      ???     float      R0    _floatVectorA    R2;   (???の箇所に算術演算の命令コードが入る)
		      END     void    -;
		 */
		String expectedCode = HEADER + GLOBAL_DIRECTIVE_FVA + EOI + GLOBAL_DIRECTIVE_ISB + EOI + META + EOI
			+ IND + OperationCode.ALLOC + WS + DATA_TYPE_NAME.FLOAT + WS + R + "1" + EOI
			+ IND + OperationCode.CAST + WS + DATA_TYPE_NAME.FLOAT + VS + DATA_TYPE_NAME.INT + WS + R + "1" + WS + ISB + EOI
			+ IND + OperationCode.ALLOCR + WS + DATA_TYPE_NAME.FLOAT + WS + R + "2" + WS + FVA + EOI
			+ IND + OperationCode.FILL + WS + DATA_TYPE_NAME.FLOAT + WS + R + "2" + WS + R + "1" + EOI
			+ IND + OperationCode.ALLOCR + WS + DATA_TYPE_NAME.FLOAT + WS + R + "0" + WS + FVA + EOI
			+ IND + operationCode + WS + DATA_TYPE_NAME.FLOAT + WS + R + "0" + WS + FVA + WS + R + "2" + EOI
			+ END_CODE;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode.replace(ASSEMBLY_WORD.LINE_SEPARATOR,""), generatedCode.replace(ASSEMBLY_WORD.LINE_SEPARATOR,""));
	}


	// 算術複合代入演算子のテスト
	@Test
	public void testGenerateArithmeticCompoundAssignmentOperatorCode() {

		this.testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(
			SCRIPT_WORD.ADDITION_ASSIGNMENT, OPERATOR_PRECEDENCE.ADDITION_ASSIGNMENT, OperationCode.ADD
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(
			SCRIPT_WORD.SUBTRACTION_ASSIGNMENT, OPERATOR_PRECEDENCE.SUBTRACTION_ASSIGNMENT, OperationCode.SUB
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(
			SCRIPT_WORD.MULTIPLICATION_ASSIGNMENT, OPERATOR_PRECEDENCE.MULTIPLICATION_ASSIGNMENT, OperationCode.MUL
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(
			SCRIPT_WORD.DIVISION_ASSIGNMENT, OPERATOR_PRECEDENCE.DIVISION_ASSIGNMENT, OperationCode.DIV
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(
			SCRIPT_WORD.REMAINDER_ASSIGNMENT, OPERATOR_PRECEDENCE.REMAINDER_ASSIGNMENT, OperationCode.REM
		);

		this.testGenerateArithmeticCompoundAssignmentOperatorCodeVector(
			SCRIPT_WORD.ADDITION_ASSIGNMENT, OPERATOR_PRECEDENCE.ADDITION_ASSIGNMENT, OperationCode.ADD
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeVector(
			SCRIPT_WORD.SUBTRACTION_ASSIGNMENT, OPERATOR_PRECEDENCE.SUBTRACTION_ASSIGNMENT, OperationCode.SUB
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeVector(
			SCRIPT_WORD.MULTIPLICATION_ASSIGNMENT, OPERATOR_PRECEDENCE.MULTIPLICATION_ASSIGNMENT, OperationCode.MUL
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeVector(
			SCRIPT_WORD.DIVISION_ASSIGNMENT, OPERATOR_PRECEDENCE.DIVISION_ASSIGNMENT, OperationCode.DIV
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeVector(
			SCRIPT_WORD.REMAINDER_ASSIGNMENT, OPERATOR_PRECEDENCE.REMAINDER_ASSIGNMENT, OperationCode.REM
		);
	}
	public void testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(String symbol, int priority, OperationCode operationCode) {

		//「 intScalarA symbol intScalarB; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DATA_TYPE_NAME.INT, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT, DATA_TYPE_NAME.INT, 0
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intScalarA", DATA_TYPE_NAME.INT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intScalarB", DATA_TYPE_NAME.INT, 1, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// コード生成
		String generatedCode = new CodeGenerator(LANG_SPEC).generate(rootNode);

		/* 期待コードを用意（内容は下記コメントの通り、ただし空白幅は見やすいよう調整）

		  #META  "line=123, file=Test.vnano";
		      ???     int    _intScalarA    _intScalarA    _intScalarB;   (???の箇所に算術演算の命令コードが入る)
		      END     void    -;
		 */
		String expectedCode = HEADER + GLOBAL_DIRECTIVE_ISA + EOI + GLOBAL_DIRECTIVE_ISB + EOI + META + EOI
			+ IND + operationCode + WS + DATA_TYPE_NAME.INT + WS + ISA + WS + ISA + WS + ISB + EOI
			+ END_CODE;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode.replace(ASSEMBLY_WORD.LINE_SEPARATOR,""), generatedCode.replace(ASSEMBLY_WORD.LINE_SEPARATOR,""));
	}
	public void testGenerateArithmeticCompoundAssignmentOperatorCodeVector(String symbol, int priority, OperationCode operationCode) {

		//「 intVectorA symbol intVectorB; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DATA_TYPE_NAME.INT, 1);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT, DATA_TYPE_NAME.INT, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intVectorA", DATA_TYPE_NAME.INT, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intVectorB", DATA_TYPE_NAME.INT, 1, AttributeValue.GLOBAL),
		};
		rootNode.addChildNode(exprNode);
		exprNode.addChildNode(operatorNode);
		operatorNode.addChildNodes(operandNodes);

		// ASTの内容を確認
		//System.out.println(rootNode);

		// コード生成
		String generatedCode = new CodeGenerator(LANG_SPEC).generate(rootNode);

		/* 期待コードを用意（内容は下記コメントの通り、ただし空白幅は見やすいよう調整）

		  #GLOBAL	_intVectorA
		  #GLOBAL	_intVectorB
		  #META  "line=123, file=Test.vnano";
		      ???     int    _intVectorA    _intVectorA    _intVectorB;   (???の箇所に算術演算の命令コードが入る)
		      END     void    -;
		 */
		String expectedCode = HEADER + GLOBAL_DIRECTIVE_IVA + EOI + GLOBAL_DIRECTIVE_IVB + EOI + META + EOI
			+ IND + operationCode + WS + DATA_TYPE_NAME.INT + WS + IVA + WS + IVA + WS + IVB + EOI
			+ END_CODE;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode.replace(ASSEMBLY_WORD.LINE_SEPARATOR,""), generatedCode.replace(ASSEMBLY_WORD.LINE_SEPARATOR,""));
	}


}
