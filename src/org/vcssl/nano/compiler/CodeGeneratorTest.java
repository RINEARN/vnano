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

	private final String EOI = ASSEMBLY_WORD.instructionSeparator + ASSEMBLY_WORD.lineSeparator; // 命令末尾記号+改行
	private final String WS = ASSEMBLY_WORD.wordSeparator;
	private final String VS = ASSEMBLY_WORD.valueSeparator;
	private final String IND = ASSEMBLY_WORD.indent;
	private final String IINT = ASSEMBLY_WORD.immediateOperandPrefix + DATA_TYPE_NAME.defaultInt;
	private final String IFLOAT = ASSEMBLY_WORD.immediateOperandPrefix + DATA_TYPE_NAME.defaultFloat;

	private final char R = ASSEMBLY_WORD.registerOperandOprefix;
	private final String IVA = ASSEMBLY_WORD.identifierOperandPrefix + "intVectorA";
	private final String IVB = ASSEMBLY_WORD.identifierOperandPrefix + "intVectorB";
	private final String ISA = ASSEMBLY_WORD.identifierOperandPrefix + "intScalarA";
	private final String ISB = ASSEMBLY_WORD.identifierOperandPrefix + "intScalarB";
	private final String FVA = ASSEMBLY_WORD.identifierOperandPrefix + "floatVectorA";
	//private static final String FVB = ASSEMBLY_WORD.OPERAND_PREFIX_IDENTIFIER + "floatVectorB";
	//private static final String FSA = ASSEMBLY_WORD.OPERAND_PREFIX_IDENTIFIER + "floatScalarA";
	private final String FSB = ASSEMBLY_WORD.identifierOperandPrefix + "floatScalarB";
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

	private final String END_CODE = IND + OperationCode.END + WS + DATA_TYPE_NAME.voidPlaceholder
	                                     + WS + ASSEMBLY_WORD.placeholderOperandPrefix + EOI;

	// テストコードの先頭に常に付く定型コード（バージョン情報など）
	private final String HEADER =
			ASSEMBLY_WORD.assemblyLanguageIdentifierDirective + ASSEMBLY_WORD.wordSeparator
			+
			"\"" + ASSEMBLY_WORD.assemblyLanguageName + "\"" + EOI
			+
			ASSEMBLY_WORD.assemblyLanguageVersionDirective + ASSEMBLY_WORD.wordSeparator
			+
			"\"" + ASSEMBLY_WORD.assemblyLanguageVersion + "\"" + EOI
			+
			ASSEMBLY_WORD.scriptLanguageIdentifierDirective + ASSEMBLY_WORD.wordSeparator
			+
			"\"" + SCRIPT_WORD.scriptLanguageName + "\"" + EOI
			+
			ASSEMBLY_WORD.scriptLanguageVersionDirective + ASSEMBLY_WORD.wordSeparator
			+
			"\"" + SCRIPT_WORD.scriptLanguageVersion + "\"" + EOI
			+
			ASSEMBLY_WORD.lineSeparator;

	// メタディレクティブコード
	private final String META = ASSEMBLY_WORD.lineSeparator
			+ ASSEMBLY_WORD.metaDirective + ASSEMBLY_WORD.wordSeparator + "\"line=123, file=Test.vnano\"";


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
		AstNode exprNode = this.createExpressionNode(DATA_TYPE_NAME.defaultInt, 1);
		AstNode operatorNode = this.createOperatorNode(
				SCRIPT_WORD.assignment, OPERATOR_PRECEDENCE.assignment, AttributeValue.BINARY,
				AttributeValue.ASSIGNMENT, DATA_TYPE_NAME.defaultInt, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intVectorA", DATA_TYPE_NAME.defaultInt, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intVectorB", DATA_TYPE_NAME.defaultInt, 1, AttributeValue.GLOBAL),
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
			+ IND + OperationCode.ALLOCR + WS + DATA_TYPE_NAME.defaultInt + WS + IVA + WS + IVB + EOI
			+ IND + OperationCode.MOV + WS + DATA_TYPE_NAME.defaultInt + WS + IVA + WS + IVB + EOI
			+ END_CODE;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode.replace(ASSEMBLY_WORD.lineSeparator,""), generatedCode.replace(ASSEMBLY_WORD.lineSeparator,""));
	}


	// 算術二項演算子のテスト
	@Test
	public void testGenerateArithmeticBinaryOperatorCode() {

		this.testGenerateArithmeticBinaryOperatorCodeScalar(
			SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalar(
			SCRIPT_WORD.minusOrSubtraction, OPERATOR_PRECEDENCE.subtraction, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalar(
			SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalar(
			SCRIPT_WORD.division, OPERATOR_PRECEDENCE.division, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalar(
			SCRIPT_WORD.remainder, OPERATOR_PRECEDENCE.remainder, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(
			SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(
			SCRIPT_WORD.minusOrSubtraction, OPERATOR_PRECEDENCE.subtraction, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(
			SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(
			SCRIPT_WORD.division, OPERATOR_PRECEDENCE.division, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeScalarCast(
			SCRIPT_WORD.remainder, OPERATOR_PRECEDENCE.remainder, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeVector(
			SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeVector(
			SCRIPT_WORD.minusOrSubtraction, OPERATOR_PRECEDENCE.subtraction, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeVector(
			SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeVector(
			SCRIPT_WORD.division, OPERATOR_PRECEDENCE.division, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeVector(
			SCRIPT_WORD.remainder, OPERATOR_PRECEDENCE.remainder, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeVectorCast(
			SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorCast(
			SCRIPT_WORD.minusOrSubtraction, OPERATOR_PRECEDENCE.subtraction, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorCast(
			SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorCast(
			SCRIPT_WORD.division, OPERATOR_PRECEDENCE.division, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorCast(
			SCRIPT_WORD.remainder, OPERATOR_PRECEDENCE.remainder, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(
			SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(
			SCRIPT_WORD.minusOrSubtraction, OPERATOR_PRECEDENCE.subtraction, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(
			SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(
			SCRIPT_WORD.division, OPERATOR_PRECEDENCE.division, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(
			SCRIPT_WORD.remainder, OPERATOR_PRECEDENCE.remainder, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(
			SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(
			SCRIPT_WORD.minusOrSubtraction, OPERATOR_PRECEDENCE.subtraction, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(
			SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(
			SCRIPT_WORD.division, OPERATOR_PRECEDENCE.division, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(
			SCRIPT_WORD.remainder, OPERATOR_PRECEDENCE.remainder, OperationCode.REM
		);

		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(
			SCRIPT_WORD.plusOrAddition, OPERATOR_PRECEDENCE.addition, OperationCode.ADD
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(
			SCRIPT_WORD.minusOrSubtraction, OPERATOR_PRECEDENCE.subtraction, OperationCode.SUB
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(
			SCRIPT_WORD.multiplication, OPERATOR_PRECEDENCE.multiplication, OperationCode.MUL
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(
			SCRIPT_WORD.division, OPERATOR_PRECEDENCE.division, OperationCode.DIV
		);
		this.testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(
			SCRIPT_WORD.remainder, OPERATOR_PRECEDENCE.remainder, OperationCode.REM
		);
	}


	public void testGenerateArithmeticBinaryOperatorCodeScalar(String symbol, int priority, OperationCode operationCode) {

		//「 1 symbol 2; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DATA_TYPE_NAME.defaultInt, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DATA_TYPE_NAME.defaultInt, RANK_OF_SCALAR
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createLiteralNode("1", DATA_TYPE_NAME.defaultInt, RANK_OF_SCALAR),
			this.createLiteralNode("2", DATA_TYPE_NAME.defaultInt, RANK_OF_SCALAR),
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
			+ IND + OperationCode.ALLOC + WS + DATA_TYPE_NAME.defaultInt + WS + R + "0" + EOI
			+ IND + operationCode + WS + DATA_TYPE_NAME.defaultInt + WS + R + "0" + WS + IINT + VS + "1" + WS + IINT + VS + "2" + EOI
			+ END_CODE;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode.replace(ASSEMBLY_WORD.lineSeparator,""), generatedCode.replace(ASSEMBLY_WORD.lineSeparator,""));
	}
	public void testGenerateArithmeticBinaryOperatorCodeScalarCast(String symbol, int priority, OperationCode operationCode) {

		//「 1 symbol 2; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DATA_TYPE_NAME.defaultFloat, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DATA_TYPE_NAME.defaultFloat, RANK_OF_SCALAR
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createLiteralNode("1", DATA_TYPE_NAME.defaultFloat, RANK_OF_SCALAR),
			this.createLiteralNode("2", DATA_TYPE_NAME.defaultInt, RANK_OF_SCALAR),
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
			+ IND + OperationCode.ALLOC + WS + DATA_TYPE_NAME.defaultFloat + WS + R + "1" + EOI
			+ IND + OperationCode.CAST + WS + DATA_TYPE_NAME.defaultFloat + VS + DATA_TYPE_NAME.defaultInt + WS + R + "1" + WS + IINT + VS + "2" + EOI
			+ IND + OperationCode.ALLOC + WS + DATA_TYPE_NAME.defaultFloat + WS + R + "0" + EOI
			+ IND + operationCode + WS + DATA_TYPE_NAME.defaultFloat + WS + R + "0" + WS + IFLOAT + VS + "1" + WS + R + "1" + EOI
			+ END_CODE;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode.replace(ASSEMBLY_WORD.lineSeparator,""), generatedCode.replace(ASSEMBLY_WORD.lineSeparator,""));
	}
	public void testGenerateArithmeticBinaryOperatorCodeVector(String symbol, int priority, OperationCode operationCode) {

		//「 intVectorA symbol intVectorB; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DATA_TYPE_NAME.defaultInt, 1);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DATA_TYPE_NAME.defaultInt, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intVectorA", DATA_TYPE_NAME.defaultInt, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intVectorB", DATA_TYPE_NAME.defaultInt, 1, AttributeValue.GLOBAL),
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
			+ IND + OperationCode.ALLOCR + WS + DATA_TYPE_NAME.defaultInt + WS + R + "0" + WS + IVA + EOI
			+ IND + operationCode + WS + DATA_TYPE_NAME.defaultInt + WS + R + "0" + WS + IVA + WS + IVB + EOI
			+ END_CODE;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode.replace(ASSEMBLY_WORD.lineSeparator,""), generatedCode.replace(ASSEMBLY_WORD.lineSeparator,""));
	}

	public void testGenerateArithmeticBinaryOperatorCodeVectorCast(String symbol, int priority, OperationCode operationCode) {

		//「 floatVectorA symbol intVectorB; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DATA_TYPE_NAME.defaultFloat, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DATA_TYPE_NAME.defaultFloat, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("floatVectorA", DATA_TYPE_NAME.defaultFloat, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intVectorB", DATA_TYPE_NAME.defaultInt, 1, AttributeValue.GLOBAL),
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
			+ IND + OperationCode.ALLOCR + WS + DATA_TYPE_NAME.defaultFloat + WS + R + "1" + WS + IVB + EOI
			+ IND + OperationCode.CAST + WS + DATA_TYPE_NAME.defaultFloat + VS + DATA_TYPE_NAME.defaultInt + WS + R + "1" + WS + IVB + EOI
			+ IND + OperationCode.ALLOCR + WS + DATA_TYPE_NAME.defaultFloat + WS + R + "0" + WS + FVA + EOI
			+ IND + operationCode + WS + DATA_TYPE_NAME.defaultFloat + WS + R + "0" + WS + FVA + WS + R + "1" + EOI
			+ END_CODE;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode.replace(ASSEMBLY_WORD.lineSeparator,""), generatedCode.replace(ASSEMBLY_WORD.lineSeparator,""));
	}
	public void testGenerateArithmeticBinaryOperatorCodeVectorScalarMixed(String symbol, int priority, OperationCode operationCode) {

		//「 intVectorA symbol floatVectorB; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DATA_TYPE_NAME.defaultInt, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DATA_TYPE_NAME.defaultInt, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intVectorA", DATA_TYPE_NAME.defaultInt, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intScalarB", DATA_TYPE_NAME.defaultInt, RANK_OF_SCALAR, AttributeValue.GLOBAL),
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
			+ IND + OperationCode.ALLOCR + WS + DATA_TYPE_NAME.defaultInt + WS + R + "1" + WS + IVA + EOI
			+ IND + OperationCode.FILL + WS + DATA_TYPE_NAME.defaultInt + WS + R + "1" + WS + ISB + EOI
			+ IND + OperationCode.ALLOCR + WS + DATA_TYPE_NAME.defaultInt + WS + R + "0" + WS + IVA + EOI
			+ IND + operationCode + WS + DATA_TYPE_NAME.defaultInt + WS + R + "0" + WS + IVA + WS + R + "1" + EOI
			+ END_CODE;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode.replace(ASSEMBLY_WORD.lineSeparator,""), generatedCode.replace(ASSEMBLY_WORD.lineSeparator,""));
	}
	public void testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastVector(String symbol, int priority, OperationCode operationCode) {

		//「 intVectorA symbol floatScalarB; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DATA_TYPE_NAME.defaultFloat, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DATA_TYPE_NAME.defaultFloat, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intVectorA", DATA_TYPE_NAME.defaultInt, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("floatScalarB", DATA_TYPE_NAME.defaultFloat, RANK_OF_SCALAR, AttributeValue.GLOBAL),
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
			+ IND + OperationCode.ALLOCR + WS + DATA_TYPE_NAME.defaultFloat + WS + R + "1" + WS + IVA + EOI
			+ IND + OperationCode.CAST + WS + DATA_TYPE_NAME.defaultFloat + VS + DATA_TYPE_NAME.defaultInt + WS + R + "1" + WS + IVA + EOI
			+ IND + OperationCode.ALLOCR + WS + DATA_TYPE_NAME.defaultFloat + WS + R + "2" + WS + IVA + EOI
			+ IND + OperationCode.FILL + WS + DATA_TYPE_NAME.defaultFloat + WS + R + "2" + WS + FSB + EOI
			+ IND + OperationCode.ALLOCR + WS + DATA_TYPE_NAME.defaultFloat + WS + R + "0" + WS + IVA + EOI
			+ IND + operationCode + WS + DATA_TYPE_NAME.defaultFloat + WS + R + "0" + WS + R + "1" + WS + R + "2" + EOI
			+ END_CODE;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode.replace(ASSEMBLY_WORD.lineSeparator,""), generatedCode.replace(ASSEMBLY_WORD.lineSeparator,""));
	}
	public void testGenerateArithmeticBinaryOperatorCodeVectorScalarMixedCastScalar(String symbol, int priority, OperationCode operationCode) {

		//「 floatVectorA symbol intScalarB; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DATA_TYPE_NAME.defaultFloat, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC, DATA_TYPE_NAME.defaultFloat, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("floatVectorA", DATA_TYPE_NAME.defaultFloat, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intScalarB", DATA_TYPE_NAME.defaultInt, RANK_OF_SCALAR, AttributeValue.GLOBAL),
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
			+ IND + OperationCode.ALLOC + WS + DATA_TYPE_NAME.defaultFloat + WS + R + "1" + EOI
			+ IND + OperationCode.CAST + WS + DATA_TYPE_NAME.defaultFloat + VS + DATA_TYPE_NAME.defaultInt + WS + R + "1" + WS + ISB + EOI
			+ IND + OperationCode.ALLOCR + WS + DATA_TYPE_NAME.defaultFloat + WS + R + "2" + WS + FVA + EOI
			+ IND + OperationCode.FILL + WS + DATA_TYPE_NAME.defaultFloat + WS + R + "2" + WS + R + "1" + EOI
			+ IND + OperationCode.ALLOCR + WS + DATA_TYPE_NAME.defaultFloat + WS + R + "0" + WS + FVA + EOI
			+ IND + operationCode + WS + DATA_TYPE_NAME.defaultFloat + WS + R + "0" + WS + FVA + WS + R + "2" + EOI
			+ END_CODE;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode.replace(ASSEMBLY_WORD.lineSeparator,""), generatedCode.replace(ASSEMBLY_WORD.lineSeparator,""));
	}


	// 算術複合代入演算子のテスト
	@Test
	public void testGenerateArithmeticCompoundAssignmentOperatorCode() {

		this.testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(
			SCRIPT_WORD.additionAssignment, OPERATOR_PRECEDENCE.additionAssignment, OperationCode.ADD
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(
			SCRIPT_WORD.subtractionAssignment, OPERATOR_PRECEDENCE.subtractionAssignment, OperationCode.SUB
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(
			SCRIPT_WORD.multiplicationAssignment, OPERATOR_PRECEDENCE.multiplicationAssignment, OperationCode.MUL
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(
			SCRIPT_WORD.divisionAssignment, OPERATOR_PRECEDENCE.divisionAssignment, OperationCode.DIV
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(
			SCRIPT_WORD.remainderAssignment, OPERATOR_PRECEDENCE.remainderAssignment, OperationCode.REM
		);

		this.testGenerateArithmeticCompoundAssignmentOperatorCodeVector(
			SCRIPT_WORD.additionAssignment, OPERATOR_PRECEDENCE.additionAssignment, OperationCode.ADD
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeVector(
			SCRIPT_WORD.subtractionAssignment, OPERATOR_PRECEDENCE.subtractionAssignment, OperationCode.SUB
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeVector(
			SCRIPT_WORD.multiplicationAssignment, OPERATOR_PRECEDENCE.multiplicationAssignment, OperationCode.MUL
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeVector(
			SCRIPT_WORD.divisionAssignment, OPERATOR_PRECEDENCE.divisionAssignment, OperationCode.DIV
		);
		this.testGenerateArithmeticCompoundAssignmentOperatorCodeVector(
			SCRIPT_WORD.remainderAssignment, OPERATOR_PRECEDENCE.remainderAssignment, OperationCode.REM
		);
	}
	public void testGenerateArithmeticCompoundAssignmentOperatorCodeScalar(String symbol, int priority, OperationCode operationCode) {

		//「 intScalarA symbol intScalarB; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DATA_TYPE_NAME.defaultInt, RANK_OF_SCALAR);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT, DATA_TYPE_NAME.defaultInt, 0
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intScalarA", DATA_TYPE_NAME.defaultInt, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intScalarB", DATA_TYPE_NAME.defaultInt, 1, AttributeValue.GLOBAL),
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
			+ IND + operationCode + WS + DATA_TYPE_NAME.defaultInt + WS + ISA + WS + ISA + WS + ISB + EOI
			+ END_CODE;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode.replace(ASSEMBLY_WORD.lineSeparator,""), generatedCode.replace(ASSEMBLY_WORD.lineSeparator,""));
	}
	public void testGenerateArithmeticCompoundAssignmentOperatorCodeVector(String symbol, int priority, OperationCode operationCode) {

		//「 intVectorA symbol intVectorB; 」のASTを用意（symbolは引数に渡された二項算術演算子）
		AstNode rootNode = this.createRootNode();
		AstNode exprNode = this.createExpressionNode(DATA_TYPE_NAME.defaultInt, 1);
		AstNode operatorNode = this.createOperatorNode(
				symbol, priority, AttributeValue.BINARY, AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT, DATA_TYPE_NAME.defaultInt, 1
		);
		AstNode[] operandNodes = new AstNode[] {
			this.createVariableIdentifierNode("intVectorA", DATA_TYPE_NAME.defaultInt, 1, AttributeValue.GLOBAL),
			this.createVariableIdentifierNode("intVectorB", DATA_TYPE_NAME.defaultInt, 1, AttributeValue.GLOBAL),
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
			+ IND + operationCode + WS + DATA_TYPE_NAME.defaultInt + WS + IVA + WS + IVA + WS + IVB + EOI
			+ END_CODE;

		// 生成コードと期待コードの内容を確認
		//System.out.println(generatedCode);
		//System.out.println(expectedCode);

		// 生成コードと期待コードを比較検査
		assertEquals(expectedCode.replace(ASSEMBLY_WORD.lineSeparator,""), generatedCode.replace(ASSEMBLY_WORD.lineSeparator,""));
	}


}
