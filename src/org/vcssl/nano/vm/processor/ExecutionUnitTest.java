/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.processor;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.memory.DataException;

public class ExecutionUnitTest {

	private static final double FLOAT64_PERMISSIBLE_ERROR = 1.0E-10;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {

		// ADD命令

		this.testAddInt64Scalar();
		this.testAddInt64Vector();
		this.testAddFloat64Scalar();
		this.testAddFloat64Vector();
		this.testAddStringScalar();
		this.testAddStringVector();
		this.testAddUnoperatableData();

		// SUB命令

		this.testSubInt64Scalar();
		this.testSubInt64Vector();
		this.testSubFloat64Scalar();
		this.testSubFloat64Vector();
		this.testSubUnoperatableData();

		// MUL命令

		this.testMulInt64Scalar();
		this.testMulInt64Vector();
		this.testMulFloat64Scalar();
		this.testMulFloat64Vector();
		this.testMulUnoperatableData();

		// DIV命令

		this.testDivInt64Scalar();
		this.testDivInt64Vector();
		this.testDivFloat64Scalar();
		this.testDivFloat64Vector();
		this.testDivUnoperatableData();

		// REM命令

		this.testRemInt64Scalar();
		this.testRemInt64Vector();
		this.testRemFloat64Scalar();
		this.testRemFloat64Vector();
		this.testRemUnoperatableData();

		// NEG命令

		this.testNegInt64Scalar();
		this.testNegInt64Vector();
		this.testNegFloat64Scalar();
		this.testNegFloat64Vector();
		this.testNegUnoperatableData();

		// EQ命令

		this.testEqInt64Scalar();
		this.testEqInt64Vector();
		this.testEqFloat64Scalar();
		this.testEqFloat64Vector();
		this.testEqStringScalar();
		this.testEqStringVector();
		this.testEqBoolScalar();
		this.testEqBoolVector();
		this.testEqUnoperatableData();

		// NEQ命令

		this.testNeqInt64Scalar();
		this.testNeqInt64Vector();
		this.testNeqFloat64Scalar();
		this.testNeqFloat64Vector();
		this.testNeqStringScalar();
		this.testNeqStringVector();
		this.testNeqBoolScalar();
		this.testNeqBoolVector();
		this.testNeqUnoperatableData();

		// GEQ命令

		this.testGeqInt64Scalar();
		this.testGeqInt64Vector();
		this.testGeqFloat64Scalar();
		this.testGeqFloat64Vector();
		this.testGeqUnoperatableData();

		// LEQ命令

		this.testLeqInt64Scalar();
		this.testLeqInt64Vector();
		this.testLeqFloat64Scalar();
		this.testLeqFloat64Vector();
		this.testLeqUnoperatableData();

		// GT命令

		this.testGtInt64Scalar();
		this.testGtInt64Vector();
		this.testGtFloat64Scalar();
		this.testGtFloat64Vector();
		this.testGtUnoperatableData();

		// LT命令

		this.testLtInt64Scalar();
		this.testLtInt64Vector();
		this.testLtFloat64Scalar();
		this.testLtFloat64Vector();
		this.testLtUnoperatableData();

		// AND命令

		this.testAndBoolScalar();
		this.testAndBoolVector();
		this.testAndUnoperatableData();

		// OR命令

		this.testOrBoolScalar();
		this.testOrBoolVector();
		this.testOrUnoperatableData();

		// NOT命令

		this.testNotBoolScalar();
		this.testNotBoolVector();
		this.testNotUnoperatableData();

		// ALLOC命令

		this.testAllocInt64Scalar();
		this.testAllocInt64Array1D();
		this.testAllocInt64Array3D();
		this.testAllocFloat64Scalar();
		this.testAllocFloat64Array1D();
		this.testAllocFloat64Array3D();
		this.testAllocBoolScalar();
		this.testAllocBoolArray1D();
		this.testAllocBoolArray3D();
		this.testAllocStringScalar();
		this.testAllocStringArray1D();
		this.testAllocStringArray3D();
		this.testAllocUnoperatableData();

		// MOV命令

		this.testMovInt64Scalar();
		this.testMovInt64Vector();
		this.testMovFloat64Scalar();
		this.testMovFloat64Vector();
		this.testMovBoolScalar();
		this.testMovBoolVector();
		this.testMovStringScalar();
		this.testMovStringVector();
		this.testMovUnoperatableData();

		// FILL命令

		this.testFillInt64();
		this.testFillFloat64();
		this.testFillBool();
		this.testFillString();
		this.testFillUnoperatableData();

		// ELEM命令

		this.testElemInt64();
		this.testElemFloat64();
		this.testElemBool();
		this.testElemString();
		this.testElemUnoperatableData();

		// VEC命令

		this.testVecInt64();
		this.testVecFloat64();
		this.testVecBool();
		this.testVecString();
		this.testVecUnoperatableData();

		// CAST命令

		this.testCastInt64Int64Scalar();
		this.testCastInt64Int64Vector();
		this.testCastInt64Float64Scalar();
		this.testCastInt64Float64Vector();
		this.testCastInt64StringScalar();
		this.testCastInt64StringVector();
		this.testCastFloat64Float64Scalar();
		this.testCastFloat64Float64Vector();
		this.testCastFloat64Int64Scalar();
		this.testCastFloat64Int64Vector();
		this.testCastFloat64StringScalar();
		this.testCastFloat64StringVector();
		this.testCastBoolBoolScalar();
		this.testCastBoolBoolVector();
		this.testCastBoolStringScalar();
		this.testCastBoolStringVector();
		this.testCastStringStringScalar();
		this.testCastStringStringVector();
		this.testCastStringInt64Scalar();
		this.testCastStringInt64Vector();
		this.testCastStringFloat64Scalar();
		this.testCastStringFloat64Vector();
		this.testCastStringBoolScalar();
		this.testCastStringBoolVector();
		this.testCastUnoperatableData();
	}


	// ==================================================
	// add
	// ==================================================

	// --------------------------------------------------
	// add, INT64 (long)
	// --------------------------------------------------

	private void testAddInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new long[]{ -1L } );
		inputA.setData( new long[]{ 1L } );
		inputB.setData( new long[]{ 2L } );

		// 演算を実行
		try {
			new ExecutionUnit().add(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 3L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new long[]{ -1L, -1L, -1L } );
		inputA.setData( new long[]{ 0L, 1L, 0L } );
		inputB.setData( new long[]{ 0L, 2L, 0L } );
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().add(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getData()[1] != 3L) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1L || output.getData()[2] != -1L) {
			fail("Incorrect output value");
		}
	}

	private void testAddInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new long[]{ -1L, -1L, -1L } );
		inputA.setData( new long[]{ 0L, 1L, 2L } );
		inputB.setData( new long[]{ 3L, 4L, 5L } );
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().add(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 3L || output.getData()[1] != 5L || output.getData()[2] != 7L) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// add, FLOAT64 (double)
	// --------------------------------------------------

	private void testAddFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0 } );
		inputA.setData( new double[]{ 0.25 } );  // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.125 } ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().add(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 0.375) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 } );
		inputA.setData( new double[]{ 0.0, 0.25,  0.0 } ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.0, 0.125, 0.0 } ); // 2進表現で割り切れる値
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().add(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getData()[1] != 0.375) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1.0 || output.getData()[2] != -1.0) {
			fail("Incorrect output value");
		}
	}

	private void testAddFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0, -1.0, -1.0 } );
		inputA.setData( new double[]{ 1.0, 0.5, 0.25 } );         // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.125, 0.0625, 0.03125 } ); // 2進表現で割り切れる値
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().add(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=1.125 || output.getData()[1]!=0.5625 || output.getData()[2]!=0.28125) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// add, STRING(String)
	// --------------------------------------------------

	private void testAddStringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<String[]> inputA = new DataContainer<String[]>();
		DataContainer<String[]> inputB = new DataContainer<String[]>();
		output.setData( new String[]{ "Init" } );
		inputA.setData( new String[]{ "Hello" } );
		inputB.setData( new String[]{ "World" } );

		// 演算を実行
		try {
			new ExecutionUnit().add(DataType.STRING, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getData()[0].equals("HelloWorld")) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new String[]{ "Init0", "Init1", "Init2" } );
		inputA.setData( new String[]{ "", "Hello", "" } );
		inputB.setData( new String[]{ "", "World", "" } );
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().add(DataType.STRING, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (!output.getData()[1].equals("HelloWorld")) {
			fail("Incorrect output value");
		}
		if (!output.getData()[0].equals("Init0") || !output.getData()[2].equals("Init2")) {
			fail("Incorrect output value");
		}
	}

	private void testAddStringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<String[]> inputA = new DataContainer<String[]>();
		DataContainer<String[]> inputB = new DataContainer<String[]>();
		output.setData( new String[]{ "Init0", "Init1", "Init2" } );
		inputA.setData( new String[]{ "Good", "Hello", "Thank" } );
		inputB.setData( new String[]{ "Morning", "World", "You" } );
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().add(DataType.STRING, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getData()[0].equals("GoodMorning")
				|| !output.getData()[1].equals("HelloWorld")
				|| !output.getData()[2].equals("ThankYou")) {

			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// add, Unoperatable type (e.g. BOOL)
	// --------------------------------------------------

	private void testAddUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new boolean[]{ true } );
		inputB.setData( new boolean[]{ false } );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().add(DataType.BOOL, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().add(DataType.INT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().add(DataType.FLOAT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().add(DataType.STRING, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
	}





	// ==================================================
	// sub
	// ==================================================

	// --------------------------------------------------
	// sub, INT64 (long)
	// --------------------------------------------------

	private void testSubInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new long[]{ -1L } );
		inputA.setData( new long[]{ 5L } );
		inputB.setData( new long[]{ 3L } );

		// 演算を実行
		try {
			new ExecutionUnit().sub(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 2L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new long[]{ -1L, -1L, -1L } );
		inputA.setData( new long[]{ 0L, 5L, 0L } );
		inputB.setData( new long[]{ 0L, 3L, 0L } );
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().sub(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getData()[1] != 2L) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1L || output.getData()[2] != -1L) {
			fail("Incorrect output value");
		}
	}

	private void testSubInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new long[]{ -1L, -1L, -1L } );
		inputA.setData( new long[]{ 5L, 1L, 7L } );
		inputB.setData( new long[]{ 3L, 8L, 7L } );
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().sub(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 2L || output.getData()[1] != -7L || output.getData()[2] != 0L) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// sub, FLOAT64 (double)
	// --------------------------------------------------

	private void testSubFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0 } );
		inputA.setData( new double[]{ 0.5 } );  // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.125 } ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().sub(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 0.375) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 } );
		inputA.setData( new double[]{ 0.0, 0.5,  0.0 } ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.0, 0.125, 0.0 } ); // 2進表現で割り切れる値
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().sub(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getData()[1] != 0.375) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1.0 || output.getData()[2] != -1.0) {
			fail("Incorrect output value");
		}
	}

	private void testSubFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0, -1.0, -1.0 } );
		inputA.setData( new double[]{ 1.0, 0.5, 0.03125 } );         // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.125, 0.0625, 0.25 } ); // 2進表現で割り切れる値
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().sub(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=0.875 || output.getData()[1]!=0.4375 || output.getData()[2]!=-0.21875) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// sub, Unoperatable type (e.g. BOOL)
	// --------------------------------------------------

	private void testSubUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new boolean[]{ true } );
		inputB.setData( new boolean[]{ false } );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().sub(DataType.BOOL, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().sub(DataType.INT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().sub(DataType.FLOAT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
	}





	// ==================================================
	// mul
	// ==================================================

	// --------------------------------------------------
	// mul, INTT64 (long)
	// --------------------------------------------------

	private void testMulInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new long[]{ -1L } );
		inputA.setData( new long[]{ 5L } );
		inputB.setData( new long[]{ 3L } );

		// 演算を実行
		try {
			new ExecutionUnit().mul(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 15L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new long[]{ -1L, -1L, -1L } );
		inputA.setData( new long[]{ 0L, 5L, 0L } );
		inputB.setData( new long[]{ 0L, 3L, 0L } );
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().mul(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getData()[1] != 15L) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1L || output.getData()[2] != -1L) {
			fail("Incorrect output value");
		}
	}

	private void testMulInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new long[]{ -1L, -1L, -1L } );
		inputA.setData( new long[]{ 5L, 1L, -7L } );
		inputB.setData( new long[]{ 3L, -8L, -2L } );
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().mul(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=15L || output.getData()[1]!=-8L || output.getData()[2]!=14L) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// mul, FLOAT64 (double)
	// --------------------------------------------------

	private void testMulFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0 } );
		inputA.setData( new double[]{ 0.5 } );   // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.125 } ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().mul(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 0.0625) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 } );
		inputA.setData( new double[]{ 0.0, 0.5,  0.0 } );  // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.0, 0.125, 0.0 } ); // 2進表現で割り切れる値
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().mul(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getData()[1] != 0.0625) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1.0 || output.getData()[2] != -1.0) {
			fail("Incorrect output value");
		}
	}

	private void testMulFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0, -1.0, -1.0 } );
		inputA.setData( new double[]{ 1.0, -0.5, -0.03125 } );  // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.125, 0.0625, -0.25 } ); // 2進表現で割り切れる値
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().mul(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=0.125 || output.getData()[1]!=-0.03125 || output.getData()[2]!=0.0078125) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// mul, Unoperatable type (e.g. BOOL)
	// --------------------------------------------------

	private void testMulUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new boolean[]{ true } );
		inputB.setData( new boolean[]{ false } );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().mul(DataType.BOOL, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().mul(DataType.INT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().mul(DataType.FLOAT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
	}





	// ==================================================
	// div
	// ==================================================

	// --------------------------------------------------
	// div, INTT64 (long)
	// --------------------------------------------------

	private void testDivInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new long[]{ -1L } );
		inputA.setData( new long[]{ 8L } );
		inputB.setData( new long[]{ 3L } );

		// 演算を実行
		try {
			new ExecutionUnit().div(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 2L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new long[]{ -1L, -1L, -1L } );
		inputA.setData( new long[]{ 0L, 8L, 0L } );
		inputB.setData( new long[]{ 0L, 3L, 0L } );
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().div(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getData()[1] != 2L) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1L || output.getData()[2] != -1L) {
			fail("Incorrect output value");
		}
	}

	private void testDivInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new long[]{ -1L, -1L, -1L } );
		inputA.setData( new long[]{ 8L, 5L, -7L } );
		inputB.setData( new long[]{ 3L, -1L, -2L } );
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().div(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=2L || output.getData()[1]!=-5L || output.getData()[2]!=3L) {
			fail("Incorrect output value");
		}
	}



	// --------------------------------------------------
	// div, FLOAT64 (double)
	// --------------------------------------------------

	private void testDivFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0 } );
		inputA.setData( new double[]{ 0.5 } );   // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.125 } ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().div(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 4.0) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 } );
		inputA.setData( new double[]{ 0.0, 0.5,  0.0 } );  // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.0, 0.125, 0.0 } ); // 2進表現で割り切れる値
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().div(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getData()[1] != 4.0) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1.0 || output.getData()[2] != -1.0) {
			fail("Incorrect output value");
		}
	}

	private void testDivFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0, -1.0, -1.0 } );
		inputA.setData( new double[]{ 1.0, -0.5, -0.03125 } );  // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.125, 0.0625, -0.25 } ); // 2進表現で割り切れる値
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().div(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=8.0 || output.getData()[1]!=-8.0 || output.getData()[2]!=0.125) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// div, Unoperatable type (e.g. BOOL)
	// --------------------------------------------------

	private void testDivUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new boolean[]{ true } );
		inputB.setData( new boolean[]{ false } );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().div(DataType.BOOL, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().div(DataType.INT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().div(DataType.FLOAT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
	}





	// ==================================================
	// rem
	// ==================================================

	// --------------------------------------------------
	// rem, INTT64 (long)
	// --------------------------------------------------

	private void testRemInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new long[]{ -1L } );
		inputA.setData( new long[]{ 8L } );
		inputB.setData( new long[]{ 3L } );

		// 演算を実行
		try {
			new ExecutionUnit().rem(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 2L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new long[]{ -1L, -1L, -1L } );
		inputA.setData( new long[]{ 0L, 8L, 0L } );
		inputB.setData( new long[]{ 0L, 3L, 0L } );
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().rem(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getData()[1] != 2L) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1L || output.getData()[2] != -1L) {
			fail("Incorrect output value");
		}
	}

	private void testRemInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new long[]{ -1L, -1L, -1L, -1L } );
		inputA.setData( new long[]{ 8L, 8L, -8L, -8L } );
		inputB.setData( new long[]{ 3L, -3L, 3L, -3L } );
		output.setSize(4);
		inputA.setSize(4);
		inputB.setSize(4);
		output.setLengths(new int[]{4});
		inputA.setLengths(new int[]{4});
		inputB.setLengths(new int[]{4});

		// 演算を実行
		try {
			new ExecutionUnit().rem(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=2L || output.getData()[1]!=2L || output.getData()[2]!=-2L || output.getData()[3]!=-2L) {
			fail("Incorrect output value");
		}
	}



	// --------------------------------------------------
	// rem, FLOAT64 (double)
	// --------------------------------------------------

	private void testRemFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0 } );
		inputA.setData( new double[]{ 0.8 } );
		inputB.setData( new double[]{ 0.3 } );

		// 演算を実行
		try {
			new ExecutionUnit().rem(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (FLOAT64_PERMISSIBLE_ERROR < Math.abs(output.getData()[0]-0.2)) { // 期待値 0.2
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 } );
		inputA.setData( new double[]{ 0.0, 0.8,  0.0 } );
		inputB.setData( new double[]{ 0.0, 0.3, 0.0 } );
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().rem(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (FLOAT64_PERMISSIBLE_ERROR < Math.abs(output.getData()[1]-0.2)) { // 期待値 0.2
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1.0 || output.getData()[2] != -1.0) {
			fail("Incorrect output value");
		}
	}

	private void testRemFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0, -1.0, -1.0, -1.0, -1.0 } );
		inputA.setData( new double[]{ 1.0, 0.8, 0.8, -0.8, -0.8 } );
		inputB.setData( new double[]{ 0.125, 0.3, -0.3, 0.3, -0.3 } );
		output.setSize(5);
		inputA.setSize(5);
		inputB.setSize(5);
		output.setLengths(new int[]{5});
		inputA.setLengths(new int[]{5});
		inputB.setLengths(new int[]{5});

		// 演算を実行
		try {
			new ExecutionUnit().rem(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=0.0
				|| FLOAT64_PERMISSIBLE_ERROR < Math.abs(output.getData()[1]-0.2)    // 期待値 0.2
				|| FLOAT64_PERMISSIBLE_ERROR < Math.abs(output.getData()[2]-0.2)    // 期待値 0.2
				|| FLOAT64_PERMISSIBLE_ERROR < Math.abs(output.getData()[3]+0.2)    // 期待値 -0.2
				|| FLOAT64_PERMISSIBLE_ERROR < Math.abs(output.getData()[4]+0.2)) { // 期待値 -0.2

			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// rem, Unoperatable type (e.g. BOOL)
	// --------------------------------------------------

	private void testRemUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new boolean[]{ true } );
		inputB.setData( new boolean[]{ false } );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().rem(DataType.BOOL, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().rem(DataType.INT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().rem(DataType.FLOAT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
	}





	// ==================================================
	// neg
	// ==================================================

	// --------------------------------------------------
	// neg, INTT64 (long)
	// --------------------------------------------------

	private void testNegInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setData( new long[]{ -1L } );
		input.setData( new long[]{ 8L } );

		// 演算を実行
		try {
			new ExecutionUnit().neg(DataType.INT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != -8L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new long[]{ -1L, -1L, -1L } );
		input.setData( new long[]{ 0L, 8L, 0L } );
		output.setOffset(1);
		input.setOffset(1);
		try {
			new ExecutionUnit().neg(DataType.INT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getData()[1] != -8L) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1L || output.getData()[2] != -1L) {
			fail("Incorrect output value");
		}
	}

	private void testNegInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setData( new long[]{ -1L, -1L, -1L } );
		input.setData( new long[]{ 0L, 5L, -7L } );
		output.setSize(3);
		input.setSize(3);
		output.setLengths(new int[]{3});
		input.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().neg(DataType.INT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=0L || output.getData()[1]!=-5L || output.getData()[2]!=7L) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// neg, INTT64 (long)
	// --------------------------------------------------

	private void testNegFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0 } );
		input.setData( new double[]{ 0.8 } );

		// 演算を実行
		try {
			new ExecutionUnit().neg(DataType.FLOAT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != -0.8) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 } );
		input.setData( new double[]{ 0, 0.8, 0 } );
		output.setOffset(1);
		input.setOffset(1);
		try {
			new ExecutionUnit().neg(DataType.FLOAT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getData()[1] != -0.8) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1.0 || output.getData()[2] != -1.0) {
			fail("Incorrect output value");
		}
	}

	private void testNegFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0, -1.0, -1.0 } );
		input.setData( new double[]{ 0L, 0.8, -0.2 } );
		output.setSize(3);
		input.setSize(3);
		output.setLengths(new int[]{3});
		input.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().neg(DataType.FLOAT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=0.0 || output.getData()[1]!=-0.8 || output.getData()[2]!=0.2) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// neg, Unoperatable type (e.g. BOOL)
	// --------------------------------------------------

	private void testNegUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false } );
		input.setData( new boolean[]{ true } );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().neg(DataType.BOOL, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().neg(DataType.INT64, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().neg(DataType.FLOAT64, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
	}





	// ==================================================
	// eq
	// ==================================================

	// --------------------------------------------------
	// eq, INT64 (long)
	// --------------------------------------------------

	private void testEqInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new long[]{ 1L } );
		inputB.setData( new long[]{ 2L } );

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().eq(DataType.INT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new long[]{ 0L, 1L, 0L } );
		inputB.setData( new long[]{ 0L, 2L, 0L } );
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().eq(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().eq(DataType.INT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getData()[0] != false || output.getData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	private void testEqInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new long[]{ 0L, 1L, 2L } );
		inputB.setData( new long[]{ 0L, 1L, 3L } );
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=true || output.getData()[1]!=true || output.getData()[2]!=false) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// eq, FLOAT64 (long)
	// --------------------------------------------------

	private void testEqFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new double[]{ 0.125 } ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.5 } );   // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().eq(DataType.FLOAT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new double[]{ -1.0, 0.125, -1.0 } ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ -1.0, 0.5, -1.0 } );   // 2進表現で割り切れる値
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().eq(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().eq(DataType.FLOAT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getData()[0] != false || output.getData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	private void testEqFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new double[]{ 0.0, 0.125, 0.5  } );   // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.0, 0.125, 0.25 } ); // 2進表現で割り切れる値
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=true || output.getData()[1]!=true || output.getData()[2]!=false) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// eq, STRING (String)
	// --------------------------------------------------

	private void testEqStringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<String[]> inputA = new DataContainer<String[]>();
		DataContainer<String[]> inputB = new DataContainer<String[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new String[]{ "Hello" } );
		inputB.setData( new String[]{ "World" } );

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.STRING, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().eq(DataType.STRING, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new String[]{ "", "Hello", "" } );
		inputB.setData( new String[]{ "", "World", "" } );
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().eq(DataType.STRING, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().eq(DataType.STRING, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getData()[0] != false || output.getData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	private void testEqStringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<String[]> inputA = new DataContainer<String[]>();
		DataContainer<String[]> inputB = new DataContainer<String[]>();
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new String[]{ "", "Hello", "Good"  } );
		inputB.setData( new String[]{ "", "Hello", "Morning" } );
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.STRING, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=true || output.getData()[1]!=true || output.getData()[2]!=false) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// eq, BOOL (boolean)
	// --------------------------------------------------

	private void testEqBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new boolean[]{ true } );
		inputB.setData( new boolean[]{ false } );

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.BOOL, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().eq(DataType.BOOL, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new boolean[]{ false, true, false } );
		inputB.setData( new boolean[]{ false, false, false } );
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().eq(DataType.BOOL, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().eq(DataType.BOOL, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getData()[0] != false || output.getData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	private void testEqBoolVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new boolean[]{ false, true, true  } );
		inputB.setData( new boolean[]{ false, true, false } );
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.BOOL, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=true || output.getData()[1]!=true || output.getData()[2]!=false) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// eq, Unoperatable type (e.g. VOID)
	// --------------------------------------------------

	private void testEqUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<Object[]> inputA = new DataContainer<Object[]>();
		DataContainer<Object[]> inputB = new DataContainer<Object[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new Object[]{ null } );
		inputB.setData( new Object[]{ null } );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().eq(DataType.VOID, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
	}





	// ==================================================
	// neq
	// ==================================================

	// --------------------------------------------------
	// neq, INT64 (long)
	// --------------------------------------------------

	private void testNeqInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new long[]{ 1L } );
		inputB.setData( new long[]{ 2L } );

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().neq(DataType.INT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new long[]{ 0L, 1L, 0L } );
		inputB.setData( new long[]{ 0L, 2L, 0L } );
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().neq(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().neq(DataType.INT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getData()[0] != false || output.getData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	private void testNeqInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new long[]{ 0L, 1L, 2L } );
		inputB.setData( new long[]{ 0L, 1L, 3L } );
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=false || output.getData()[1]!=false || output.getData()[2]!=true) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// neq, FLOAT64 (long)
	// --------------------------------------------------

	private void testNeqFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new double[]{ 0.125 } ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.5 } );   // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().neq(DataType.FLOAT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new double[]{ -1.0, 0.125, -1.0 } ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ -1.0, 0.5, -1.0 } );   // 2進表現で割り切れる値
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().neq(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().neq(DataType.FLOAT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getData()[0] != false || output.getData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	private void testNeqFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new double[]{ 0.0, 0.125, 0.5  } );   // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.0, 0.125, 0.25 } ); // 2進表現で割り切れる値
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=false || output.getData()[1]!=false || output.getData()[2]!=true) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// neq, STRING (String)
	// --------------------------------------------------

	private void testNeqStringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<String[]> inputA = new DataContainer<String[]>();
		DataContainer<String[]> inputB = new DataContainer<String[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new String[]{ "Hello" } );
		inputB.setData( new String[]{ "World" } );

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.STRING, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().neq(DataType.STRING, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new String[]{ "", "Hello", "" } );
		inputB.setData( new String[]{ "", "World", "" } );
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().neq(DataType.STRING, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().neq(DataType.STRING, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getData()[0] != false || output.getData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	private void testNeqStringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<String[]> inputA = new DataContainer<String[]>();
		DataContainer<String[]> inputB = new DataContainer<String[]>();
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new String[]{ "", "Hello", "Good"  } );
		inputB.setData( new String[]{ "", "Hello", "Morning" } );
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.STRING, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=false || output.getData()[1]!=false || output.getData()[2]!=true) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// neq, BOOL (boolean)
	// --------------------------------------------------

	private void testNeqBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new boolean[]{ true } );
		inputB.setData( new boolean[]{ false } );

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.BOOL, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().neq(DataType.BOOL, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new boolean[]{ false, true, false } );
		inputB.setData( new boolean[]{ false, false, false } );
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().neq(DataType.BOOL, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().neq(DataType.BOOL, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getData()[0] != false || output.getData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	private void testNeqBoolVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new boolean[]{ false, true, true  } );
		inputB.setData( new boolean[]{ false, true, false } );
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.BOOL, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=false || output.getData()[1]!=false || output.getData()[2]!=true) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// neq, Unoperatable type (e.g. VOID)
	// --------------------------------------------------

	private void testNeqUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<Object[]> inputA = new DataContainer<Object[]>();
		DataContainer<Object[]> inputB = new DataContainer<Object[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new Object[]{ null } );
		inputB.setData( new Object[]{ null } );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().neq(DataType.VOID, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
	}


	// ==================================================
	// geq
	// ==================================================

	// --------------------------------------------------
	// geq, INT64 (long)
	// --------------------------------------------------

	private void testGeqInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new long[]{ 1L } );
		inputB.setData( new long[]{ 2L } );

		// 演算を実行
		try {
			new ExecutionUnit().geq(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().geq(DataType.INT64, output, inputB, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().geq(DataType.INT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new long[]{ 0L, 1L, 0L } );
		inputB.setData( new long[]{ 0L, 2L, 0L } );
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().geq(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().geq(DataType.INT64, output, inputB, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().geq(DataType.INT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getData()[0] != false || output.getData()[2] != false) {
			fail("Incorrect output value");
		}
	}


	private void testGeqInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new long[]{ 1L, 2L, 3L } );
		inputB.setData( new long[]{ 1L, 3L, 2L } );
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().geq(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=true || output.getData()[1]!=false || output.getData()[2]!=true) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// geq, FLOAT64 (long)
	// --------------------------------------------------

	private void testGeqFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new double[]{ 0.125 } ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.5 } );   // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().geq(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().geq(DataType.FLOAT64, output, inputB, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().geq(DataType.FLOAT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new double[]{ 0.0, 0.125, 0.0 } ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.0, 0.5,   0.0 } ); // 2進表現で割り切れる値
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().geq(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().geq(DataType.FLOAT64, output, inputB, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().geq(DataType.FLOAT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getData()[0] != false || output.getData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	private void testGeqFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new double[]{ 0.5, 0.125, 0.25 } ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.5, 0.25, 0.125 } ); // 2進表現で割り切れる値
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().geq(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=true || output.getData()[1]!=false || output.getData()[2]!=true) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// geq, Unoperatable type (e.g. BOOL)
	// --------------------------------------------------

	private void testGeqUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new boolean[]{ true } );
		inputB.setData( new boolean[]{ false } );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().geq(DataType.BOOL, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().geq(DataType.INT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().geq(DataType.FLOAT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
	}



	// ==================================================
	// leq
	// ==================================================

	// --------------------------------------------------
	// leq, INT64 (long)
	// --------------------------------------------------

	private void testLeqInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new long[]{ 1L } );
		inputB.setData( new long[]{ 2L } );

		// 演算を実行
		try {
			new ExecutionUnit().leq(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().leq(DataType.INT64, output, inputB, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().leq(DataType.INT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new long[]{ 0L, 1L, 0L } );
		inputB.setData( new long[]{ 0L, 2L, 0L } );
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().leq(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().leq(DataType.INT64, output, inputB, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().leq(DataType.INT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getData()[0] != false || output.getData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	private void testLeqInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new long[]{ 1L, 2L, 3L } );
		inputB.setData( new long[]{ 1L, 3L, 2L } );
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().leq(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=true || output.getData()[1]!=true || output.getData()[2]!=false) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// leq, FLOAT64 (long)
	// --------------------------------------------------

	private void testLeqFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new double[]{ 0.125 } ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.5 } );   // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().leq(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().leq(DataType.FLOAT64, output, inputB, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().leq(DataType.FLOAT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new double[]{ 0.0, 0.125, 0.0 } ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.0, 0.5,   0.0 } ); // 2進表現で割り切れる値
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().leq(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().leq(DataType.FLOAT64, output, inputB, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().leq(DataType.FLOAT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getData()[0] != false || output.getData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	private void testLeqFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new double[]{ 0.5, 0.125, 0.25 } ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.5, 0.25, 0.125 } ); // 2進表現で割り切れる値
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().leq(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=true || output.getData()[1]!=true || output.getData()[2]!=false) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// leq, Unoperatable type (e.g. BOOL)
	// --------------------------------------------------

	private void testLeqUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new boolean[]{ true } );
		inputB.setData( new boolean[]{ false } );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().leq(DataType.BOOL, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().leq(DataType.INT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().leq(DataType.FLOAT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
	}





	// ==================================================
	// gt
	// ==================================================

	// --------------------------------------------------
	// gt, INT64 (long)
	// --------------------------------------------------

	private void testGtInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new long[]{ 1L } );
		inputB.setData( new long[]{ 2L } );

		// 演算を実行
		try {
			new ExecutionUnit().gt(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().gt(DataType.INT64, output, inputB, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().gt(DataType.INT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new long[]{ 0L, 1L, 0L } );
		inputB.setData( new long[]{ 0L, 2L, 0L } );
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().gt(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().gt(DataType.INT64, output, inputB, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().gt(DataType.INT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getData()[0] != false || output.getData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	private void testGtInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new long[]{ 1L, 2L, 3L } );
		inputB.setData( new long[]{ 1L, 3L, 2L } );
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().gt(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=false || output.getData()[1]!=false || output.getData()[2]!=true) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// gt, FLOAT64 (long)
	// --------------------------------------------------

	private void testGtFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new double[]{ 0.125 } ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.5 } );   // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().gt(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().gt(DataType.FLOAT64, output, inputB, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().gt(DataType.FLOAT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new double[]{ 0.0, 0.125, 0.0 } ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.0, 0.5,   0.0 } ); // 2進表現で割り切れる値
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().gt(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().gt(DataType.FLOAT64, output, inputB, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().gt(DataType.FLOAT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getData()[0] != false || output.getData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	private void testGtFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new double[]{ 0.5, 0.125, 0.25 } ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.5, 0.25, 0.125 } ); // 2進表現で割り切れる値
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().gt(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=false || output.getData()[1]!=false || output.getData()[2]!=true) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// gt, Unoperatable type (e.g. BOOL)
	// --------------------------------------------------

	private void testGtUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new boolean[]{ true } );
		inputB.setData( new boolean[]{ false } );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().gt(DataType.BOOL, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().gt(DataType.INT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().gt(DataType.FLOAT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
	}



	// ==================================================
	// lt
	// ==================================================

	// --------------------------------------------------
	// lt, INT64 (long)
	// --------------------------------------------------

	private void testLtInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new long[]{ 1L } );
		inputB.setData( new long[]{ 2L } );

		// 演算を実行
		try {
			new ExecutionUnit().lt(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().lt(DataType.INT64, output, inputB, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().lt(DataType.INT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new long[]{ 0L, 1L, 0L } );
		inputB.setData( new long[]{ 0L, 2L, 0L } );
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().lt(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().lt(DataType.INT64, output, inputB, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().lt(DataType.INT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getData()[0] != false || output.getData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	private void testLtInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new long[]{ 1L, 2L, 3L } );
		inputB.setData( new long[]{ 1L, 3L, 2L } );
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().lt(DataType.INT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=false || output.getData()[1]!=true || output.getData()[2]!=false) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// lt, FLOAT64 (long)
	// --------------------------------------------------

	private void testLtFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new double[]{ 0.125 } ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.5 } );   // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().lt(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().lt(DataType.FLOAT64, output, inputB, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().lt(DataType.FLOAT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new double[]{ 0.0, 0.125, 0.0 } ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.0, 0.5,   0.0 } ); // 2進表現で割り切れる値
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().lt(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().lt(DataType.FLOAT64, output, inputB, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().lt(DataType.FLOAT64, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getData()[0] != false || output.getData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	private void testLtFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new double[]{ 0.5, 0.125, 0.25 } ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.5, 0.25, 0.125 } ); // 2進表現で割り切れる値
		output.setSize(3);
		inputA.setSize(3);
		inputB.setSize(3);
		output.setLengths(new int[]{3});
		inputA.setLengths(new int[]{3});
		inputB.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().lt(DataType.FLOAT64, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=false || output.getData()[1]!=true || output.getData()[2]!=false) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// lt, Unoperatable type (e.g. BOOL)
	// --------------------------------------------------

	private void testLtUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new boolean[]{ true } );
		inputB.setData( new boolean[]{ false } );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().lt(DataType.BOOL, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().lt(DataType.INT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().lt(DataType.FLOAT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
	}



	// ==================================================
	// and
	// ==================================================

	// --------------------------------------------------
	// and, BOOL (boolean)
	// --------------------------------------------------

	private void testAndBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new boolean[]{ true } );
		inputB.setData( new boolean[]{ false } );

		// 演算を実行
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputB, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputB, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new boolean[]{ false, true, false } );
		inputB.setData( new boolean[]{ false, false, false } );
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputB, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputB, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getData()[0] != false || output.getData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	private void testAndBoolVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false, false, false, false } );
		inputA.setData( new boolean[]{ false, true, false, true } );
		inputB.setData( new boolean[]{ false, false, true, true } );
		output.setSize(4);
		inputA.setSize(4);
		inputB.setSize(4);
		output.setLengths(new int[]{4});
		inputA.setLengths(new int[]{4});
		inputB.setLengths(new int[]{4});

		// 演算を実行
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=false
				|| output.getData()[1]!=false
				|| output.getData()[2]!=false
				|| output.getData()[3]!=true) {

			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// and, Unoperatable type (e.g. FLOAT64)
	// --------------------------------------------------

	private void testAndUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new double[]{ 0.125 } );
		inputB.setData( new double[]{ 0.25 } );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().and(DataType.FLOAT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
	}



	// ==================================================
	// or
	// ==================================================

	// --------------------------------------------------
	// or, BOOL (boolean)
	// --------------------------------------------------

	private void testOrBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new boolean[]{ true } );
		inputB.setData( new boolean[]{ false } );

		// 演算を実行
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputB, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputB, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new boolean[]{ false, true, false } );
		inputB.setData( new boolean[]{ false, false, false } );
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputB, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputA, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputB, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getData()[0] != false || output.getData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	private void testOrBoolVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false, false, false, false } );
		inputA.setData( new boolean[]{ false, true, false, true } );
		inputB.setData( new boolean[]{ false, false, true, true } );
		output.setSize(4);
		inputA.setSize(4);
		inputB.setSize(4);
		output.setLengths(new int[]{4});
		inputA.setLengths(new int[]{4});
		inputB.setLengths(new int[]{4});

		// 演算を実行
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputA, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=false
				|| output.getData()[1]!=true
				|| output.getData()[2]!=true
				|| output.getData()[3]!=true) {

			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// or, Unoperatable type (e.g. FLOAT64)
	// --------------------------------------------------

	private void testOrUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new double[]{ 0.125 } );
		inputB.setData( new double[]{ 0.25 } );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().or(DataType.FLOAT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
	}



	// ==================================================
	// not
	// ==================================================

	// --------------------------------------------------
	// not, BOOL (boolean)
	// --------------------------------------------------

	private void testNotBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false } );
		inputA.setData( new boolean[]{ true } );
		inputB.setData( new boolean[]{ false } );

		// 演算を実行
		try {
			new ExecutionUnit().not(DataType.BOOL, output, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().not(DataType.BOOL, output, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false } );
		inputA.setData( new boolean[]{ false, true, false } );
		inputB.setData( new boolean[]{ false, false, false } );
		output.setOffset(1);
		inputA.setOffset(1);
		inputB.setOffset(1);
		try {
			new ExecutionUnit().not(DataType.BOOL, output, inputA);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().not(DataType.BOOL, output, inputB);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[1] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}
	}

	private void testNotBoolVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false, false} );
		input.setData( new boolean[]{ false, true } );
		output.setSize(2);
		input.setSize(2);
		output.setLengths(new int[]{2});
		input.setLengths(new int[]{2});

		// 演算を実行
		try {
			new ExecutionUnit().not(DataType.BOOL, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=true || output.getData()[1]!=false) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// or, Unoperatable type (e.g. FLOAT64)
	// --------------------------------------------------

	private void testNotUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0 } );
		input.setData( new double[]{ 0.125 } );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().not(DataType.FLOAT64, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().not(DataType.BOOL, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
	}





	// ==================================================
	// alloc
	// ==================================================

	// --------------------------------------------------
	// alloc, INT64 (long)
	// --------------------------------------------------

	private void testAllocInt64Scalar() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();

		// 確保処理を実行
		try {
			new ExecutionUnit().alloc(DataType.INT64, target);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getData();
		if (!(data instanceof long[])
				|| ((long[])data).length <= target.getOffset()
				|| target.getSize() != 1
				|| target.getLengths().length != 0) {

			fail("Incorrect allocated data");
		}
	}

	private void testAllocInt64Array1D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<long[]> len = new DataContainer<long[]>();
		len.setData(new long[]{ 3L });

		// 確保処理を実行
		try {
			new ExecutionUnit().alloc(DataType.INT64, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getData();
		if (!(data instanceof long[])
				|| ((long[])data).length != 3
				|| target.getOffset() != 0
				|| target.getSize() != 3
				|| target.getLengths().length != 1
				|| target.getLengths()[0] != 3) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocも検査
		len.setData(new long[]{ 5L });
		try {
			new ExecutionUnit().alloc(DataType.INT64, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getData();
		if (!(data instanceof long[])
				|| ((long[])data).length != 5
				|| target.getOffset() != 0
				|| target.getSize() != 5
				|| target.getLengths().length != 1
				|| target.getLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		len.setData(new long[]{ 5L });
		try {
			new ExecutionUnit().alloc(DataType.INT64, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getData();
		if (!(data instanceof long[])
				|| ((long[])data).length != 5
				|| target.getOffset() != 0
				|| target.getSize() != 5
				|| target.getLengths().length != 1
				|| target.getLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}
	}

	private void testAllocInt64Array3D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();

		DataContainer<long[]> len = new DataContainer<long[]>();
		len.setLengths(new int[]{ 1 });
		len.setSize(3);
		len.setData(new long[]{ 2L, 3L, 4L });

		// 確保処理を実行
		try {
			new ExecutionUnit().alloc(DataType.INT64, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getData();
		if (!(data instanceof long[])
				|| ((long[])data).length != 24
				|| target.getOffset() != 0
				|| target.getSize() != 24
				|| target.getLengths().length != 3
				|| target.getLengths()[0] != 2
				|| target.getLengths()[1] != 3
				|| target.getLengths()[2] != 4) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocも検査
		len.setData(new long[]{ 5L, 6L, 7L });
		try {
			new ExecutionUnit().alloc(DataType.INT64, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getData();
		if (!(data instanceof long[])
				|| ((long[])data).length != 210
				|| target.getOffset() != 0
				|| target.getSize() != 210
				|| target.getLengths().length != 3
				|| target.getLengths()[0] != 5
				|| target.getLengths()[1] != 6
				|| target.getLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		len.setData(new long[]{ 5L, 6L, 7L });
		try {
			new ExecutionUnit().alloc(DataType.INT64, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getData();
		if (!(data instanceof long[])
				|| ((long[])data).length != 210
				|| target.getOffset() != 0
				|| target.getSize() != 210
				|| target.getLengths().length != 3
				|| target.getLengths()[0] != 5
				|| target.getLengths()[1] != 6
				|| target.getLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}
	}


	// --------------------------------------------------
	// alloc, FLOAT64 (double)
	// --------------------------------------------------

	private void testAllocFloat64Scalar() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();

		// 確保処理を実行
		try {
			new ExecutionUnit().alloc(DataType.FLOAT64, target);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getData();
		if (!(data instanceof double[])
				|| ((double[])data).length <= target.getOffset()
				|| target.getSize() != 1
				|| target.getLengths().length != 0) {

			fail("Incorrect allocated data");
		}
	}

	private void testAllocFloat64Array1D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<long[]> len = new DataContainer<long[]>();
		len.setData(new long[]{ 3L });

		// 確保処理を実行
		try {
			new ExecutionUnit().alloc(DataType.FLOAT64, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getData();
		if (!(data instanceof double[])
				|| ((double[])data).length != 3
				|| target.getOffset() != 0
				|| target.getSize() != 3
				|| target.getLengths().length != 1
				|| target.getLengths()[0] != 3) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocも検査
		len.setData(new long[]{ 5L });
		try {
			new ExecutionUnit().alloc(DataType.FLOAT64, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getData();
		if (!(data instanceof double[])
				|| ((double[])data).length != 5
				|| target.getOffset() != 0
				|| target.getSize() != 5
				|| target.getLengths().length != 1
				|| target.getLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		len.setData(new long[]{ 5L });
		try {
			new ExecutionUnit().alloc(DataType.FLOAT64, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getData();
		if (!(data instanceof double[])
				|| ((double[])data).length != 5
				|| target.getOffset() != 0
				|| target.getSize() != 5
				|| target.getLengths().length != 1
				|| target.getLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}
	}

	private void testAllocFloat64Array3D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();

		DataContainer<long[]> len = new DataContainer<long[]>();
		len.setLengths(new int[]{ 1 });
		len.setSize(3);
		len.setData(new long[]{ 2L, 3L, 4L });

		// 確保処理を実行
		try {
			new ExecutionUnit().alloc(DataType.FLOAT64, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getData();
		if (!(data instanceof double[])
				|| ((double[])data).length != 24
				|| target.getOffset() != 0
				|| target.getSize() != 24
				|| target.getLengths().length != 3
				|| target.getLengths()[0] != 2
				|| target.getLengths()[1] != 3
				|| target.getLengths()[2] != 4) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocも検査
		len.setData(new long[]{ 5L, 6L, 7L });
		try {
			new ExecutionUnit().alloc(DataType.FLOAT64, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getData();
		if (!(data instanceof double[])
				|| ((double[])data).length != 210
				|| target.getOffset() != 0
				|| target.getSize() != 210
				|| target.getLengths().length != 3
				|| target.getLengths()[0] != 5
				|| target.getLengths()[1] != 6
				|| target.getLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		len.setData(new long[]{ 5L, 6L, 7L });
		try {
			new ExecutionUnit().alloc(DataType.FLOAT64, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getData();
		if (!(data instanceof double[])
				|| ((double[])data).length != 210
				|| target.getOffset() != 0
				|| target.getSize() != 210
				|| target.getLengths().length != 3
				|| target.getLengths()[0] != 5
				|| target.getLengths()[1] != 6
				|| target.getLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}
	}


	// --------------------------------------------------
	// alloc, BOOL (boolean)
	// --------------------------------------------------

	private void testAllocBoolScalar() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();

		// 確保処理を実行
		try {
			new ExecutionUnit().alloc(DataType.BOOL, target);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getData();
		if (!(data instanceof boolean[])
				|| ((boolean[])data).length <= target.getOffset()
				|| target.getSize() != 1
				|| target.getLengths().length != 0) {

			fail("Incorrect allocated data");
		}
	}

	private void testAllocBoolArray1D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<long[]> len = new DataContainer<long[]>();
		len.setData(new long[]{ 3L });

		// 確保処理を実行
		try {
			new ExecutionUnit().alloc(DataType.BOOL, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getData();
		if (!(data instanceof boolean[])
				|| ((boolean[])data).length != 3
				|| target.getOffset() != 0
				|| target.getSize() != 3
				|| target.getLengths().length != 1
				|| target.getLengths()[0] != 3) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocも検査
		len.setData(new long[]{ 5L });
		try {
			new ExecutionUnit().alloc(DataType.BOOL, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getData();
		if (!(data instanceof boolean[])
				|| ((boolean[])data).length != 5
				|| target.getOffset() != 0
				|| target.getSize() != 5
				|| target.getLengths().length != 1
				|| target.getLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		len.setData(new long[]{ 5L });
		try {
			new ExecutionUnit().alloc(DataType.BOOL, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getData();
		if (!(data instanceof boolean[])
				|| ((boolean[])data).length != 5
				|| target.getOffset() != 0
				|| target.getSize() != 5
				|| target.getLengths().length != 1
				|| target.getLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}
	}

	private void testAllocBoolArray3D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();

		DataContainer<long[]> len = new DataContainer<long[]>();
		len.setLengths(new int[]{ 1 });
		len.setSize(3);
		len.setData(new long[]{ 2L, 3L, 4L });

		// 確保処理を実行
		try {
			new ExecutionUnit().alloc(DataType.BOOL, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getData();
		if (!(data instanceof boolean[])
				|| ((boolean[])data).length != 24
				|| target.getOffset() != 0
				|| target.getSize() != 24
				|| target.getLengths().length != 3
				|| target.getLengths()[0] != 2
				|| target.getLengths()[1] != 3
				|| target.getLengths()[2] != 4) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocも検査
		len.setData(new long[]{ 5L, 6L, 7L });
		try {
			new ExecutionUnit().alloc(DataType.BOOL, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getData();
		if (!(data instanceof boolean[])
				|| ((boolean[])data).length != 210
				|| target.getOffset() != 0
				|| target.getSize() != 210
				|| target.getLengths().length != 3
				|| target.getLengths()[0] != 5
				|| target.getLengths()[1] != 6
				|| target.getLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		len.setData(new long[]{ 5L, 6L, 7L });
		try {
			new ExecutionUnit().alloc(DataType.BOOL, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getData();
		if (!(data instanceof boolean[])
				|| ((boolean[])data).length != 210
				|| target.getOffset() != 0
				|| target.getSize() != 210
				|| target.getLengths().length != 3
				|| target.getLengths()[0] != 5
				|| target.getLengths()[1] != 6
				|| target.getLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}
	}


	// --------------------------------------------------
	// alloc, STRING (String)
	// --------------------------------------------------

	private void testAllocStringScalar() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();

		// 確保処理を実行
		try {
			new ExecutionUnit().alloc(DataType.STRING, target);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getData();
		if (!(data instanceof String[])
				|| ((String[])data).length <= target.getOffset()
				|| target.getSize() != 1
				|| target.getLengths().length != 0) {

			fail("Incorrect allocated data");
		}
	}

	private void testAllocStringArray1D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<long[]> len = new DataContainer<long[]>();
		len.setData(new long[]{ 3L });

		// 確保処理を実行
		try {
			new ExecutionUnit().alloc(DataType.STRING, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getData();
		if (!(data instanceof String[])
				|| ((String[])data).length != 3
				|| target.getOffset() != 0
				|| target.getSize() != 3
				|| target.getLengths().length != 1
				|| target.getLengths()[0] != 3) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocも検査
		len.setData(new long[]{ 5L });
		try {
			new ExecutionUnit().alloc(DataType.STRING, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getData();
		if (!(data instanceof String[])
				|| ((String[])data).length != 5
				|| target.getOffset() != 0
				|| target.getSize() != 5
				|| target.getLengths().length != 1
				|| target.getLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		len.setData(new long[]{ 5L });
		try {
			new ExecutionUnit().alloc(DataType.STRING, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getData();
		if (!(data instanceof String[])
				|| ((String[])data).length != 5
				|| target.getOffset() != 0
				|| target.getSize() != 5
				|| target.getLengths().length != 1
				|| target.getLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}
	}

	private void testAllocStringArray3D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();

		DataContainer<long[]> len = new DataContainer<long[]>();
		len.setLengths(new int[]{ 1 });
		len.setSize(3);
		len.setData(new long[]{ 2L, 3L, 4L });

		// 確保処理を実行
		try {
			new ExecutionUnit().alloc(DataType.STRING, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getData();
		if (!(data instanceof String[])
				|| ((String[])data).length != 24
				|| target.getOffset() != 0
				|| target.getSize() != 24
				|| target.getLengths().length != 3
				|| target.getLengths()[0] != 2
				|| target.getLengths()[1] != 3
				|| target.getLengths()[2] != 4) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocも検査
		len.setData(new long[]{ 5L, 6L, 7L });
		try {
			new ExecutionUnit().alloc(DataType.STRING, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getData();
		if (!(data instanceof String[])
				|| ((String[])data).length != 210
				|| target.getOffset() != 0
				|| target.getSize() != 210
				|| target.getLengths().length != 3
				|| target.getLengths()[0] != 5
				|| target.getLengths()[1] != 6
				|| target.getLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		len.setData(new long[]{ 5L, 6L, 7L });
		try {
			new ExecutionUnit().alloc(DataType.STRING, target, len);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getData();
		if (!(data instanceof String[])
				|| ((String[])data).length != 210
				|| target.getOffset() != 0
				|| target.getSize() != 210
				|| target.getLengths().length != 3
				|| target.getLengths()[0] != 5
				|| target.getLengths()[1] != 6
				|| target.getLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}
	}

	// --------------------------------------------------
	// alloc, Unoperatable type (e.g. VOID)
	// --------------------------------------------------

	private void testAllocUnoperatableData() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> target = new DataContainer<boolean[]>();

		// スカラの確保処理を実行
		try {
			new ExecutionUnit().alloc(DataType.VOID, target);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}

		// 配列の確保でもテスト
		DataContainer<long[]> len = new DataContainer<long[]>();
		len.setData(new long[]{ 3 });
		try {
			new ExecutionUnit().alloc(DataType.VOID, target, len);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}

	}




	// ==================================================
	// mov
	// ==================================================

	// --------------------------------------------------
	// mov, INT64 (long)
	// --------------------------------------------------

	private void testMovInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setData( new long[]{ -1L } );
		input.setData( new long[]{ 2L } );

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.INT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 2L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new long[]{ -1L, -1L, -1L } );
		input.setData( new long[]{ 0L, 2L, 0L } );
		output.setOffset(1);
		input.setOffset(1);
		try {
			new ExecutionUnit().mov(DataType.INT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != 2L) {
			fail("Incorrect output value");
		}
		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getData()[0] != -1L || output.getData()[2] != -1L) {
			fail("Incorrect output value");
		}
	}

	private void testMovInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setData( new long[]{ -1L, -1L, -1L } );
		input.setData( new long[]{ 1L, 2L, 3L } );
		output.setSize(3);
		input.setSize(3);
		output.setLengths(new int[]{3});
		input.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.INT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 1L || output.getData()[1] != 2L || output.getData()[2] != 3L) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// mov, FLOAT64 (double)
	// --------------------------------------------------

	private void testMovFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0 } );
		input.setData( new double[]{ 0.25 } ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.FLOAT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 0.25) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 } );
		input.setData( new double[]{ 0.0, 0.25, 0.0 } ); // 2進表現で割り切れる値
		output.setOffset(1);
		input.setOffset(1);
		try {
			new ExecutionUnit().mov(DataType.FLOAT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != 0.25) {
			fail("Incorrect output value");
		}
		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getData()[0] != -1.0 || output.getData()[2] != -1.0) {
			fail("Incorrect output value");
		}
	}

	private void testMovFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0, -1.0, -1.0 } );
		input.setData( new double[]{ 0.5, -0.25, 0.125 } ); // 2進表現で割り切れる値
		output.setSize(3);
		input.setSize(3);
		output.setLengths(new int[]{3});
		input.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.FLOAT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=0.5 || output.getData()[1]!=-0.25 || output.getData()[2]!=0.125) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// mov, BOOL (boolean)
	// --------------------------------------------------

	private void testMovBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false } );
		input.setData( new boolean[]{ true } );

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.BOOL, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false } );
		input.setData( new boolean[]{ false, true, false } );
		output.setOffset(1);
		input.setOffset(1);
		try {
			new ExecutionUnit().mov(DataType.BOOL, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getData()[0] != false || output.getData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	private void testMovBoolVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false, false, false } );
		input.setData( new boolean[]{ true, false, true } );
		output.setSize(3);
		input.setSize(3);
		output.setLengths(new int[]{3});
		input.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.BOOL, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=true || output.getData()[1]!=false || output.getData()[2]!=true) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// mov, STRING (String)
	// --------------------------------------------------

	private void testMovStringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setData( new String[]{ "Init" } );
		input.setData( new String[]{ "Hello" } );

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.STRING, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getData()[0].equals("Hello")) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new String[]{ "Init0", "Init1", "Init2" } );
		input.setData( new String[]{ "", "Hello", "" } );
		output.setOffset(1);
		input.setOffset(1);
		try {
			new ExecutionUnit().mov(DataType.STRING, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (!output.getData()[1].equals("Hello")) {
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (!output.getData()[0].equals("Init0") || !output.getData()[2].equals("Init2")) {
			fail("Incorrect output value");
		}
	}

	private void testMovStringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setData( new String[]{ "Init0", "Init1", "Init2" } );
		input.setData( new String[]{ "Hello", "World", "!" } );
		output.setSize(3);
		input.setSize(3);
		output.setLengths(new int[]{3});
		input.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.STRING, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getData()[0].equals("Hello")
				|| !output.getData()[1].equals("World")
				|| !output.getData()[2].equals("!")) {

			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// mov, Unoperatable type (e.g. VOID)
	// --------------------------------------------------

	private void testMovUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<Object> output = new DataContainer<Object>();
		DataContainer<Object> input = new DataContainer<Object>();
		output.setData(this); // thisは、とにかく演算できないデータを格納しておくため
		input.setData(this);

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().mov(DataType.VOID, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().mov(DataType.INT64, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().mov(DataType.FLOAT64, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().mov(DataType.BOOL, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().mov(DataType.STRING, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
	}





	// ==================================================
	// fill
	// ==================================================

	// --------------------------------------------------
	// fill, INT64 (long)
	// --------------------------------------------------

	private void testFillInt64() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setSize(3);
		output.setLengths(new int[]{3});
		output.setData( new long[]{ -1L, -1L, -1L } );
		input.setData( new long[]{ 2L } );

		// 演算を実行
		try {
			new ExecutionUnit().fill(DataType.INT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=2L || output.getData()[1]!=2L || output.getData()[2]!=2L) {
			fail("Incorrect output value");
		}

		// inoutがオフセット設定された場合でも正常に動作するか検査
		output.setData( new long[]{ -1L, -1L, -1L } );
		input.setData( new long[]{ 0L, 8L, 0L } );
		input.setOffset(1);
		try {
			new ExecutionUnit().fill(DataType.INT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[0]!=8L || output.getData()[1]!=8L || output.getData()[2]!=8L) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// fill, FLOAT64 (double)
	// --------------------------------------------------

	private void testFillFloat64() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setSize(3);
		output.setLengths(new int[]{3});
		output.setData( new double[]{ -1.0, -1.0, -1.0 } );
		input.setData( new double[]{ 0.25 } ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().fill(DataType.FLOAT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=0.25 || output.getData()[1]!=0.25 || output.getData()[2]!=0.25) {
			fail("Incorrect output value");
		}

		// inoutがオフセット設定された場合でも正常に動作するか検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 } );
		input.setData( new double[]{ 0.0, 0.125, 0.0 } );
		input.setOffset(1);
		try {
			new ExecutionUnit().fill(DataType.FLOAT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[0]!=0.125 || output.getData()[1]!=0.125 || output.getData()[2]!=0.125) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// fill, BOOL (boolean)
	// --------------------------------------------------

	private void testFillBool() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setSize(3);
		output.setLengths(new int[]{3});
		output.setData( new boolean[]{ false, false, false } );
		input.setData( new boolean[]{ true } );

		// 演算を実行
		try {
			new ExecutionUnit().fill(DataType.BOOL, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=true || output.getData()[1]!=true || output.getData()[2]!=true) {
			fail("Incorrect output value");
		}

		// inoutがオフセット設定された場合でも正常に動作するか検査
		output.setData( new boolean[]{ false, false, false } );
		input.setData( new boolean[]{ false, true, false } );
		input.setOffset(1);
		try {
			new ExecutionUnit().fill(DataType.BOOL, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[0]!=true || output.getData()[1]!=true || output.getData()[2]!=true) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// fill, STRING (String)
	// --------------------------------------------------

	private void testFillString() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setSize(3);
		output.setLengths(new int[]{3});
		output.setData( new String[]{ "Init0", "Init1", "Init2" } );
		input.setData( new String[]{ "Hello" } );

		// 演算を実行
		try {
			new ExecutionUnit().fill(DataType.STRING, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getData()[0].equals("Hello")
				|| !output.getData()[1].equals("Hello")
				|| !output.getData()[2].equals("Hello") ) {

			fail("Incorrect output value");
		}

		// inoutがオフセット設定された場合でも正常に動作するか検査
		output.setData( new String[]{ "Init0", "Init1", "Init2" } );
		input.setData( new String[]{ "", "Hello", "" } );
		input.setOffset(1);
		try {
			new ExecutionUnit().fill(DataType.STRING, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (!output.getData()[0].equals("Hello")
				|| !output.getData()[1].equals("Hello")
				|| !output.getData()[2].equals("Hello") ) {

			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// fill, Unoperatable type (e.g. VOID)
	// --------------------------------------------------

	private void testFillUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<?> output = new DataContainer<Object>();
		DataContainer<?> input = new DataContainer<Object>();

		// 演算を実行
		try {
			new ExecutionUnit().fill(DataType.VOID, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
	}



	// ==================================================
	// elem
	// ==================================================

	// --------------------------------------------------
	// elem, INT64 (long)
	// --------------------------------------------------

	private void testElemInt64() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> dest = new DataContainer<long[]>();  // 要素を格納するコンテナ
		DataContainer<long[]> src = new DataContainer<long[]>();   // 参照する配列のコンテナ
		DataContainer<long[]> index = new DataContainer<long[]>(); // インデックス指定コンテナ

		src.setSize(6);
		src.setLengths(new int[]{2, 3, 4}); // 参照する配列の要素数は [2][3][4]
		index.setSize(3);
		index.setLengths(new int[]{ 3 }); // 3次元の参照なのでインデックスは3要素

		// テスト用の配列データを src に設定
		// { {{0,1,2,3}, {4,5,6,7}, {8,9,10,11}}, {{12,13,14,15}, {16,17,18,19}, {20,21,22,23}} }
		long[] arrayData = new long[]{
				0L, 1L, 2L, 3L,
				4L, 5L, 6L, 7L,
				8L, 9L, 10L, 11L,

				12L, 13L, 14L, 15L,
				16L, 17L, 18L, 19L,
				20L, 21L, 22L, 23L,
		};
		src.setData(arrayData);

		// [1][0][2] の要素(=14)を参照する
		index.setData(new long[]{1, 0, 2});
		try {
			new ExecutionUnit().elem(DataType.INT64, dest, src, index);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (dest.getData()[ dest.getOffset() ] != 14L) {
			fail("Incorrect output value");
		}

		// [0][2][3] の要素(=11)を参照する
		index.setData(new long[]{0, 2, 3});
		try {
			new ExecutionUnit().elem(DataType.INT64, dest, src, index);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (dest.getData()[ dest.getOffset() ] != 11L) {
			fail("Incorrect output value");
		}

		// 参照中の [0][2][3] の値を書き換えて、元の配列データの要素値が変わる（参照が切れてない）事を検査
		dest.getData()[ dest.getOffset() ] = 123L;
		if (arrayData[11] != 123L) {  // [0][2][3]は元の配列データでは[11]番目
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// elem, FLOAT64 (double)
	// --------------------------------------------------

	private void testElemFloat64() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> dest = new DataContainer<double[]>();  // 要素を格納するコンテナ
		DataContainer<double[]> src = new DataContainer<double[]>();   // 参照する配列のコンテナ
		DataContainer<long[]> index = new DataContainer<long[]>(); // インデックス指定コンテナ

		src.setSize(6);
		src.setLengths(new int[]{2, 3, 4}); // 参照する配列の要素数は [2][3][4]
		index.setSize(3);
		index.setLengths(new int[]{ 3 }); // 3次元の参照なのでインデックスは3要素

		// テスト用の配列データを src に設定
		// { {{0,1,2,3}, {4,5,6,7}, {8,9,10,11}}, {{12,13,14,15}, {16,17,18,19}, {20,21,22,23}} }
		double[] arrayData = new double[]{
				0.0, 1.0, 2.0, 3.0,
				4.0, 5.0, 6.0, 7.0,
				8.0, 9.0, 10.0, 11.0,

				12.0, 13.0, 14.0, 15.0,
				16.0, 17.0, 18.0, 19.0,
				20.0, 21.0, 22.0, 23.0,
		};
		src.setData(arrayData);

		// [1][0][2] の要素(=14)を参照する
		index.setData(new long[]{1, 0, 2});
		try {
			new ExecutionUnit().elem(DataType.FLOAT64, dest, src, index);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (dest.getData()[ dest.getOffset() ] != 14.0) {
			fail("Incorrect output value");
		}

		// [0][2][3] の要素(=11)を参照する
		index.setData(new long[]{0, 2, 3});
		try {
			new ExecutionUnit().elem(DataType.FLOAT64, dest, src, index);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (dest.getData()[ dest.getOffset() ] != 11.0) {
			fail("Incorrect output value");
		}

		// 参照中の [0][2][3] の値を書き換えて、元の配列データの要素値が変わる（参照が切れてない）事を検査
		dest.getData()[ dest.getOffset() ] = 123.0;
		if (arrayData[11] != 123.0) {  // [0][2][3]は元の配列データでは[11]番目
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// elem, BOOL (boolean)
	// --------------------------------------------------

	private void testElemBool() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> dest = new DataContainer<boolean[]>();  // 要素を格納するコンテナ
		DataContainer<boolean[]> src = new DataContainer<boolean[]>();   // 参照する配列のコンテナ
		DataContainer<long[]> index = new DataContainer<long[]>(); // インデックス指定コンテナ

		src.setSize(6);
		src.setLengths(new int[]{2, 3, 4}); // 参照する配列の要素数は [2][3][4]
		index.setSize(3);
		index.setLengths(new int[]{ 3 }); // 3次元の参照なのでインデックスは3要素

		// テスト用の配列データを src に設定
		boolean[] arrayData = new boolean[]{
				false, false, false, false,
				false, false, false, false,
				false, false, false, false,

				false, false, true,  false,
				false, false, false, false,
				false, false, false, false,
		};
		src.setData(arrayData);

		// [1][0][2] の要素(=true)を参照する
		index.setData(new long[]{1, 0, 2});
		try {
			new ExecutionUnit().elem(DataType.BOOL, dest, src, index);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (dest.getData()[ dest.getOffset() ] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// [0][2][3] の要素(=false)を参照する
		index.setData(new long[]{0, 2, 3});
		try {
			new ExecutionUnit().elem(DataType.BOOL, dest, src, index);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (dest.getData()[ dest.getOffset() ] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// 参照中の [0][2][3] の値を書き換えて、元の配列データの要素値が変わる（参照が切れてない）事を検査
		dest.getData()[ dest.getOffset() ] = true;
		if (arrayData[11] != true) {  // [0][2][3]は元の配列データでは[11]番目
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// elem, STRING (String)
	// --------------------------------------------------

	private void testElemString() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> dest = new DataContainer<String[]>();  // 要素を格納するコンテナ
		DataContainer<String[]> src = new DataContainer<String[]>();   // 参照する配列のコンテナ
		DataContainer<long[]> index = new DataContainer<long[]>(); // インデックス指定コンテナ

		src.setSize(6);
		src.setLengths(new int[]{2, 3, 4}); // 参照する配列の要素数は [2][3][4]
		index.setSize(3);
		index.setLengths(new int[]{ 3 }); // 3次元の参照なのでインデックスは3要素

		// テスト用の配列データを src に設定
		String[] arrayData = new String[]{
				"000", "001", "002", "003",
				"010", "011", "012", "013",
				"020", "021", "022", "023",

				"100", "101", "102", "103",
				"110", "111", "112", "113",
				"120", "121", "122", "123",
		};
		src.setData(arrayData);

		// [1][0][2] の要素(=true)を参照する
		index.setData(new long[]{1, 0, 2});
		try {
			new ExecutionUnit().elem(DataType.STRING, dest, src, index);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (!dest.getData()[ dest.getOffset() ].equals("102")) {
			fail("Incorrect output value");
		}

		// [0][2][3] の要素(=false)を参照する
		index.setData(new long[]{0, 2, 3});
		try {
			new ExecutionUnit().elem(DataType.STRING, dest, src, index);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (!dest.getData()[ dest.getOffset() ].equals("023")) {
			fail("Incorrect output value");
		}

		// 参照中の [0][1][3] の値を書き換えて、元の配列データの要素値が変わる（参照が切れてない）事を検査
		dest.getData()[ dest.getOffset() ] = "888";
		if (!arrayData[11].equals("888")) { // [0][2][3]は元の配列データでは[11]番目
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// elem, Unoperatable type (e.g. VOID)
	// --------------------------------------------------

	private void testElemUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<?> dest = new DataContainer<Object>();  // 要素を格納するコンテナ
		DataContainer<?> src = new DataContainer<Object>();   // 参照する配列のコンテナ
		DataContainer<long[]> index = new DataContainer<long[]>(); // インデックス指定コンテナ
		src.setSize(6);
		src.setLengths(new int[]{2, 3, 4}); // 参照する配列の要素数は [2][3][4]
		index.setSize(3);
		index.setLengths(new int[]{ 3 }); // 3次元の参照なのでインデックスは3要素
		index.setData(new long[]{1, 0, 2});

		// 演算を実行
		try {
			new ExecutionUnit().elem(DataType.VOID, dest, src, index);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
	}


	// ==================================================
	// vec
	// ==================================================

	// --------------------------------------------------
	// vec, INT64 (long)
	// --------------------------------------------------

	private void testVecInt64() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		output.setSize(3);
		output.setLengths(new int[]{ 3 });
		output.setData(new long[]{ -1L, -1L, -1L });

		DataContainer<long[]> elem0 = new DataContainer<long[]>();
		elem0.setData(new long[]{ 10L });
		DataContainer<long[]> elem1 = new DataContainer<long[]>();
		elem1.setData(new long[]{ 11L });
		DataContainer<long[]> elem2 = new DataContainer<long[]>();
		elem2.setData(new long[]{ 12L });

		DataContainer<?>[] input = new DataContainer<?>[]{ elem0, elem1, elem2 };


		// 演算を実行
		try {
			new ExecutionUnit().vec(DataType.INT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=10L || output.getData()[1]!=11L || output.getData()[2]!=12L) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// vec, FLOAT64 (double)
	// --------------------------------------------------

	private void testVecFloat64() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		output.setSize(3);
		output.setLengths(new int[]{ 3 });
		output.setData(new double[]{ -1.0, -1.0, -1.0 });

		DataContainer<double[]> elem0 = new DataContainer<double[]>();
		elem0.setData(new double[]{ 0.125 });
		DataContainer<double[]> elem1 = new DataContainer<double[]>();
		elem1.setData(new double[]{ 0.25 });
		DataContainer<double[]> elem2 = new DataContainer<double[]>();
		elem2.setData(new double[]{ 0.5 });

		DataContainer<?>[] input = new DataContainer<?>[]{ elem0, elem1, elem2 };


		// 演算を実行
		try {
			new ExecutionUnit().vec(DataType.FLOAT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=0.125 || output.getData()[1]!=0.25 || output.getData()[2]!=0.5) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// vec, BOOL (boolean)
	// --------------------------------------------------

	private void testVecBool() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		output.setSize(3);
		output.setLengths(new int[]{ 3 });
		output.setData(new boolean[]{ false, false, false });

		DataContainer<boolean[]> elem0 = new DataContainer<boolean[]>();
		elem0.setData(new boolean[]{ true });
		DataContainer<boolean[]> elem1 = new DataContainer<boolean[]>();
		elem1.setData(new boolean[]{ false });
		DataContainer<boolean[]> elem2 = new DataContainer<boolean[]>();
		elem2.setData(new boolean[]{ true });

		DataContainer<?>[] input = new DataContainer<?>[]{ elem0, elem1, elem2 };

		// 演算を実行
		try {
			new ExecutionUnit().vec(DataType.BOOL, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=true || output.getData()[1]!=false || output.getData()[2]!=true) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// vec, STRING (String)
	// --------------------------------------------------

	private void testVecString() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		output.setSize(3);
		output.setLengths(new int[]{ 3 });
		output.setData(new String[]{ "Init0", "Init1", "Init2" });

		DataContainer<String[]> elem0 = new DataContainer<String[]>();
		elem0.setData(new String[]{ "Hello" });
		DataContainer<String[]> elem1 = new DataContainer<String[]>();
		elem1.setData(new String[]{ "World" });
		DataContainer<String[]> elem2 = new DataContainer<String[]>();
		elem2.setData(new String[]{ "!" });

		DataContainer<?>[] input = new DataContainer<?>[]{ elem0, elem1, elem2 };

		// 演算を実行
		try {
			new ExecutionUnit().vec(DataType.STRING, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getData()[0].equals("Hello")
				|| !output.getData()[1].equals("World")
				|| !output.getData()[2].equals("!")) {

			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// vec, Unoperatable type (e.g. VOID)
	// --------------------------------------------------

	private void testVecUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<Object[]> output = new DataContainer<Object[]>();
		output.setSize(3);
		output.setLengths(new int[]{ 3 });
		output.setData(new Object[]{ this, this, this }); // thisは、とにかく演算できないデータを格納するため

		DataContainer<Object[]> elem0 = new DataContainer<Object[]>();
		elem0.setData(new Object[]{ this });
		DataContainer<Object[]> elem1 = new DataContainer<Object[]>();
		elem1.setData(new Object[]{ this });
		DataContainer<Object[]> elem2 = new DataContainer<Object[]>();
		elem2.setData(new Object[]{ this });

		DataContainer<?>[] input = new DataContainer<?>[]{ elem0, elem1, elem2 };

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().vec(DataType.VOID, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().vec(DataType.INT64, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().vec(DataType.FLOAT64, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().vec(DataType.BOOL, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().vec(DataType.STRING, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
	}


	// ==================================================
	// cast
	// ==================================================

	// --------------------------------------------------
	// cast, INT64 to INT64 (long to long)
	// --------------------------------------------------

	private void testCastInt64Int64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setData( new long[]{ -1L } );
		input.setData( new long[]{ 2L } );

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.INT64, DataType.INT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 2L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new long[]{ -1L, -1L, -1L } );
		output.setOffset(1);
		input.setData( new long[]{ 0L, 0L, 2L, 0L, 0L } );
		input.setOffset(2);
		try {
			new ExecutionUnit().cast(DataType.INT64, DataType.INT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getData()[ output.getOffset() ] != 2L) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1L || output.getData()[2] != -1L) {
			fail("Incorrect output value");
		}
	}

	private void testCastInt64Int64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setData( new long[]{ -1L, -1L, -1L } );
		input.setData( new long[]{ 1L, 2L, 3L } );
		output.setSize(3);
		input.setSize(3);
		output.setLengths(new int[]{3});
		input.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.INT64, DataType.INT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 1L || output.getData()[1] != 2L || output.getData()[2] != 3L) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, INT64 to FLOAT64 (long to double)
	// --------------------------------------------------

	private void testCastInt64Float64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setData( new double[]{ -1.0 } );
		input.setData( new long[]{ 2L } );

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.INT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 2.0) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 } );
		output.setOffset(1);
		input.setData( new long[]{ 0L, 0L, 2L, 0L, 0L } );
		input.setOffset(2);
		try {
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.INT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getData()[ output.getOffset() ] != 2.0) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1.0 || output.getData()[2] != -1.0) {
			fail("Incorrect output value");
		}
	}

	private void testCastInt64Float64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setData( new double[]{ -1.0, -1.0, -1.0 } );
		input.setData( new long[]{ 1L, 2L, 3L } );
		output.setSize(3);
		input.setSize(3);
		output.setLengths(new int[]{3});
		input.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.INT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 1.0 || output.getData()[1] != 2.0 || output.getData()[2] != 3.0) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, INT64 to STRING (long to String)
	// --------------------------------------------------

	private void testCastInt64StringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setData( new String[]{ "Init" } );
		input.setData( new long[]{ 2L } );

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.INT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getData()[0].equals("2")) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new String[]{ "Init0", "Init1", "Init2" } );
		output.setOffset(1);
		input.setData( new long[]{ 0L, 0L, 2L, 0L, 0L } );
		input.setOffset(2);
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.INT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (!output.getData()[ output.getOffset() ].equals("2")) {
			fail("Incorrect output value");
		}
		if (!output.getData()[0].equals("Init0") || !output.getData()[2].equals("Init2")) {
			fail("Incorrect output value");
		}
	}

	private void testCastInt64StringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setData( new String[]{ "Init0", "Init1", "Init2" } );
		input.setData( new long[]{ 1L, 2L, 3L } );
		output.setSize(3);
		input.setSize(3);
		output.setLengths(new int[]{3});
		input.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.INT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getData()[0].equals("1")
				|| !output.getData()[1].equals("2")
				|| !output.getData()[2].equals("3") ) {

			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// cast, FLOAT64 to FLOAT64 (double to double)
	// --------------------------------------------------

	private void testCastFloat64Float64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0 } );
		input.setData( new double[]{ 2.25 } ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.FLOAT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 2.25) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 } );
		output.setOffset(1);
		input.setData( new double[]{ 0.0, 0.0, 2.25, 0.0, 0.0 } );
		input.setOffset(2);
		try {
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.FLOAT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getData()[ output.getOffset() ] != 2.25) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1.0 || output.getData()[2] != -1.0) {
			fail("Incorrect output value");
		}
	}

	private void testCastFloat64Float64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0, -1.0, -1.0 } );
		input.setData( new double[]{ 1.125, 2.25, 3.5 } ); // 2進表現で割り切れる値
		output.setSize(3);
		input.setSize(3);
		output.setLengths(new int[]{3});
		input.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.FLOAT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=1.125 || output.getData()[1]!=2.25 || output.getData()[2]!=3.5) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, FLOAT64 to INT64 (double to long)
	// --------------------------------------------------

	private void testCastFloat64Int64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setData( new long[]{ -1L } );
		input.setData( new double[]{ 2.25 } ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.INT64, DataType.FLOAT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 2L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new long[]{ -1L, -1L, -1L } );
		output.setOffset(1);
		input.setData( new double[]{ 0.0, 0.0, 2.25, 0.0, 0.0 } );
		input.setOffset(2);
		try {
			new ExecutionUnit().cast(DataType.INT64, DataType.FLOAT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getData()[ output.getOffset() ] != 2L) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1L || output.getData()[2] != -1L) {
			fail("Incorrect output value");
		}
	}

	private void testCastFloat64Int64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setData( new long[]{ -1L, -1L, -1L } );
		input.setData( new double[]{ 1.125, 2.25, 3.5 } ); // 2進表現で割り切れる値
		output.setSize(3);
		input.setSize(3);
		output.setLengths(new int[]{3});
		input.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.INT64, DataType.FLOAT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 1L || output.getData()[1] != 2L || output.getData()[2] != 3L) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, FLOAT64 to STRING (double to String)
	// --------------------------------------------------

	private void testCastFloat64StringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setData( new String[]{ "Init" } );
		input.setData( new double[]{ 2.25 } ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.FLOAT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getData()[0].equals("2.25")) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new String[]{ "Init0", "Init1", "Init2" } );
		output.setOffset(1);
		input.setData( new double[]{ 0.0, 0.0, 2.25, 0.0, 0.0 } );
		input.setOffset(2);
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.FLOAT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (!output.getData()[ output.getOffset() ].equals("2.25")) {
			fail("Incorrect output value");
		}
		if (!output.getData()[0].equals("Init0") || !output.getData()[2].equals("Init2")) {
			fail("Incorrect output value");
		}
	}

	private void testCastFloat64StringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setData( new String[]{ "Init0", "Init1", "Init2" } );
		input.setData( new double[]{ 1.125, 2.25, 3.5 } ); // 2進表現で割り切れる値
		output.setSize(3);
		input.setSize(3);
		output.setLengths(new int[]{3});
		input.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.FLOAT64, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getData()[0].equals("1.125")
				|| !output.getData()[1].equals("2.25")
				|| !output.getData()[2].equals("3.5") ) {

			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, BOOL to BOOL (boolean to String)
	// --------------------------------------------------

	private void testCastBoolBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false } );
		input.setData( new boolean[]{ true } );

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.BOOL, DataType.BOOL, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false } );
		output.setOffset(1);
		input.setData( new boolean[]{ false, false, true, false, false } );
		input.setOffset(2);
		try {
			new ExecutionUnit().cast(DataType.BOOL, DataType.BOOL, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getData()[ output.getOffset() ] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}
		if (output.getData()[0] != false || output.getData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	private void testCastBoolBoolVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false, false, false } );
		input.setData( new boolean[]{ true, false, true } );
		output.setSize(3);
		input.setSize(3);
		output.setLengths(new int[]{3});
		input.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.BOOL, DataType.BOOL, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=true || output.getData()[1]!=false || output.getData()[2]!=true ) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, BOOL to STRING (boolean to String)
	// --------------------------------------------------

	private void testCastBoolStringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setData( new String[]{ "Init" } );
		input.setData( new boolean[]{ true } );

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.BOOL, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getData()[0].equals("true")) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new String[]{ "Init0", "Init1", "Init2" } );
		output.setOffset(1);
		input.setData( new boolean[]{ false, false, true, false, false } );
		input.setOffset(2);
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.BOOL, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (!output.getData()[ output.getOffset() ].equals("true")) {
			fail("Incorrect output value");
		}
		if (!output.getData()[0].equals("Init0") || !output.getData()[2].equals("Init2")) {
			fail("Incorrect output value");
		}
	}

	private void testCastBoolStringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setData( new String[]{ "Init0", "Init1", "Init2" } );
		input.setData( new boolean[]{ true, false, true } );
		output.setSize(3);
		input.setSize(3);
		output.setLengths(new int[]{3});
		input.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.BOOL, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getData()[0].equals("true")
				|| !output.getData()[1].equals("false")
				|| !output.getData()[2].equals("true") ) {

			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// cast, STRING to STRING (String to String)
	// --------------------------------------------------

	private void testCastStringStringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setData( new String[]{ "Init" } );
		input.setData( new String[]{ "Hello" } );

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.STRING, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getData()[0].equals("Hello")) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new String[]{ "Init0", "Init1", "Init2" } );
		output.setOffset(1);
		input.setData( new String[]{ "", "", "Hello", "", "" } );
		input.setOffset(2);
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.STRING, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (!output.getData()[ output.getOffset() ].equals("Hello")) {
			fail("Incorrect output value");
		}
		if (!output.getData()[0].equals("Init0") || !output.getData()[2].equals("Init2")) {
			fail("Incorrect output value");
		}
	}

	private void testCastStringStringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setData( new String[]{ "Init0", "Init1", "Init2" } );
		input.setData( new String[]{ "Hello", "World", "!" } );
		output.setSize(3);
		input.setSize(3);
		output.setLengths(new int[]{3});
		input.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.STRING, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getData()[0].equals("Hello")
				|| !output.getData()[1].equals("World")
				|| !output.getData()[2].equals("!") ) {

			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, STRING to INT64 (String to long)
	// --------------------------------------------------

	private void testCastStringInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setData( new long[]{ -1L } );
		input.setData( new String[]{ "2" } );

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.INT64, DataType.STRING, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 2L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new long[]{ -1L, -1L, -1L } );
		output.setOffset(1);
		input.setData( new String[]{ "0", "0", "2", "0", "0" } );
		input.setOffset(2);
		try {
			new ExecutionUnit().cast(DataType.INT64, DataType.STRING, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getData()[ output.getOffset() ] != 2L) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1L || output.getData()[2] != -1L) {
			fail("Incorrect output value");
		}

		// 浮動小数点として解釈できる文字列から整数への変換も検査(内部処理が異なる)
		output.setData( new long[]{ -1L, -1L, -1L } );
		input.setData( new String[]{ "0", "0", "8.25", "0", "0" } ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().cast(DataType.INT64, DataType.STRING, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[ output.getOffset() ] != 8L) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1L || output.getData()[2] != -1L) {
			fail("Incorrect output value");
		}

		// 整数に変換できない文字列からの変換も検査
		output.setData( new long[]{ -1L, -1L, -1L } );
		input.setData( new String[]{ "0", "0", "Hello", "0", "0" } ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().cast(DataType.INT64, DataType.STRING, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
	}

	private void testCastStringInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setData( new long[]{ -1L, -1L, -1L } );
		input.setData( new String[]{ "1", "2", "3" } );
		output.setSize(3);
		input.setSize(3);
		output.setLengths(new int[]{3});
		input.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.INT64, DataType.STRING, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 1L || output.getData()[1] != 2L || output.getData()[2] != 3L) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, STRING to FLOAT64 (String to double)
	// --------------------------------------------------

	private void testCastStringFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setData( new double[]{ -1.0 } );
		input.setData( new String[]{ "2.25" } ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.STRING, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 2.25) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 } );
		output.setOffset(1);
		input.setData( new String[]{ "0.0", "0.0", "2.25", "0.0", "0.0" } );
		input.setOffset(2);
		try {
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.STRING, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getData()[ output.getOffset() ] != 2.25) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1.0 || output.getData()[2] != -1.0) {
			fail("Incorrect output value");
		}

		// 浮動小数点数に変換できない文字列からの変換も検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 } );
		input.setData( new String[]{ "0.0", "0.0", "Hello", "0.0", "0.0" } ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.STRING, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
	}

	private void testCastStringFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setData( new double[]{ -1.0, -1.0, -1.0 } );
		input.setData( new String[]{ "1.125", "2.25", "3.5" } ); // 2進表現で割り切れる値
		output.setSize(3);
		input.setSize(3);
		output.setLengths(new int[]{3});
		input.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.STRING, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=1.125 || output.getData()[1]!=2.25 || output.getData()[2]!=3.5) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, STRING to BOOL (String to boolean)
	// --------------------------------------------------

	private void testCastStringBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setData( new boolean[]{ false } );
		input.setData( new String[]{ "true" } ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.BOOL, DataType.STRING, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false } );
		output.setOffset(1);
		input.setData( new String[]{ "false", "false", "true", "false", "false" } );
		input.setOffset(2);
		try {
			new ExecutionUnit().cast(DataType.BOOL, DataType.STRING, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getData()[ output.getOffset() ] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}
		if (output.getData()[0] != false || output.getData()[2] != false) {
			fail("Incorrect output value");
		}

		// 論理値に変換できない文字列からの変換も検査
		output.setData( new boolean[]{ false, false, false } );
		input.setData( new String[]{ "0.0", "0.0", "Hello", "0.0", "0.0" } ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().cast(DataType.BOOL, DataType.STRING, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
	}

	private void testCastStringBoolVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setData( new boolean[]{ false, false, false } );
		input.setData( new String[]{ "true", "false", "true" } ); // 2進表現で割り切れる値
		output.setSize(3);
		input.setSize(3);
		output.setLengths(new int[]{3});
		input.setLengths(new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.BOOL, DataType.STRING, output, input);
		} catch (DataException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=true || output.getData()[1]!=false || output.getData()[2]!=true) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, Unoperatable type (e.g. VOID)
	// --------------------------------------------------

	@SuppressWarnings("unchecked")
	private void testCastUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<?> output = new DataContainer<Object>();
		DataContainer<?> input = new DataContainer<Object>();

		// 演算を実行
		try {
			((DataContainer<long[]>)output).setData(new long[]{ -1L });
			((DataContainer<boolean[]>)input).setData(new boolean[]{ false });
			new ExecutionUnit().cast(DataType.INT64, DataType.BOOL, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<long[]>)output).setData(new long[]{ -1L });
			((DataContainer<Object[]>)input).setData(new Object[]{ "" });
			new ExecutionUnit().cast(DataType.INT64, DataType.VOID, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<double[]>)output).setData(new double[]{ -1.0 });
			((DataContainer<boolean[]>)input).setData(new boolean[]{ false });
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.BOOL, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<double[]>)output).setData(new double[]{ -1.0 });
			((DataContainer<Object[]>)input).setData(new Object[]{ "" });
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.VOID, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<boolean[]>)output).setData(new boolean[]{ false });
			((DataContainer<long[]>)input).setData(new long[]{ 123L });
			new ExecutionUnit().cast(DataType.BOOL, DataType.INT64, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<boolean[]>)output).setData(new boolean[]{ false });
			((DataContainer<double[]>)input).setData(new double[]{ 2.25 });
			new ExecutionUnit().cast(DataType.BOOL, DataType.FLOAT64, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<boolean[]>)output).setData(new boolean[]{ false });
			((DataContainer<Object[]>)input).setData(new Object[]{ "" });
			new ExecutionUnit().cast(DataType.BOOL, DataType.VOID, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<String[]>)output).setData(new String[]{ "Init" });
			((DataContainer<Object[]>)input).setData(new Object[]{ "" });
			new ExecutionUnit().cast(DataType.STRING, DataType.VOID, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<Object[]>)output).setData(new Object[]{ "" });
			((DataContainer<Object[]>)input).setData(new Object[]{ "" });
			new ExecutionUnit().cast(DataType.VOID, DataType.VOID, output, input);
			fail("Expected exception did not occured");
		} catch (DataException e) {
			// 例外が発生するのが正しい挙動
		}
	}


}
