/*
 * Copyright(C) 2017-2021 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.processor;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.VnanoException;

public class ExecutionUnitTest {

	private static final double FLOAT64_PERMISSIBLE_ERROR = 1.0E-10;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}


	// ==================================================
	// add
	// ==================================================

	// --------------------------------------------------
	// add, INT64 (long)
	// --------------------------------------------------

	@Test
	public void testAddInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setArrayData( new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new long[]{ 1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new long[]{ 2L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().add(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 3L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new long[]{ -1L, -1L, -1L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new long[]{ 0L, 1L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new long[]{ 0L, 2L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().add(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getArrayData()[1] != 3L) {
			fail("Incorrect output value");
		}
		if (output.getArrayData()[0] != -1L || output.getArrayData()[2] != -1L) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testAddInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setArrayData( new long[]{ -1L, -1L, -1L }, 0, new int[]{3});
		inputA.setArrayData( new long[]{ 0L, 1L, 2L }, 0, new int[]{3});
		inputB.setArrayData( new long[]{ 3L, 4L, 5L }, 0, new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().add(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 3L || output.getArrayData()[1] != 5L || output.getArrayData()[2] != 7L) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// add, FLOAT64 (double)
	// --------------------------------------------------

	@Test
	public void testAddFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new double[]{ -1.0 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ 0.25 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );  // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.125 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().add(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 0.375) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new double[]{ -1.0, -1.0, -1.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ 0.0, 0.25,  0.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.0, 0.125, 0.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().add(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getArrayData()[1] != 0.375) {
			fail("Incorrect output value");
		}
		if (output.getArrayData()[0] != -1.0 || output.getArrayData()[2] != -1.0) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testAddFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new double[]{ -1.0, -1.0, -1.0 }, 0, new int[]{3} );
		inputA.setArrayData( new double[]{ 1.0, 0.5, 0.25 }, 0, new int[]{3} );         // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.125, 0.0625, 0.03125 }, 0, new int[]{3} ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().add(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=1.125 || output.getArrayData()[1]!=0.5625 || output.getArrayData()[2]!=0.28125) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// add, STRING (String)
	// --------------------------------------------------

	@Test
	public void testAddStringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<String[]> inputA = new DataContainer<String[]>();
		DataContainer<String[]> inputB = new DataContainer<String[]>();
		output.setArrayData( new String[]{ "Init" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new String[]{ "Hello" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new String[]{ "World" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().add(DataType.STRING, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getArrayData()[0].equals("HelloWorld")) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new String[]{ "Init0", "Init1", "Init2" }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new String[]{ "", "Hello", "" }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new String[]{ "", "World", "" }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().add(DataType.STRING, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (!output.getArrayData()[1].equals("HelloWorld")) {
			fail("Incorrect output value");
		}
		if (!output.getArrayData()[0].equals("Init0") || !output.getArrayData()[2].equals("Init2")) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testAddStringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<String[]> inputA = new DataContainer<String[]>();
		DataContainer<String[]> inputB = new DataContainer<String[]>();
		output.setArrayData( new String[]{ "Init0", "Init1", "Init2" }, 0, new int[]{3} );
		inputA.setArrayData( new String[]{ "Good", "Hello", "Thank" }, 0, new int[]{3} );
		inputB.setArrayData( new String[]{ "Morning", "World", "You" }, 0, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().add(DataType.STRING, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getArrayData()[0].equals("GoodMorning")
				|| !output.getArrayData()[1].equals("HelloWorld")
				|| !output.getArrayData()[2].equals("ThankYou")) {

			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// add, Unoperatable type (e.g. BOOL)
	// --------------------------------------------------

	@Test
	public void testAddUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().add(DataType.BOOL, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().add(DataType.INT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().add(DataType.FLOAT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().add(DataType.STRING, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}





	// ==================================================
	// sub
	// ==================================================

	// --------------------------------------------------
	// sub, INT64 (long)
	// --------------------------------------------------

	@Test
	public void testSubInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setArrayData( new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new long[]{ 5L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new long[]{ 3L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().sub(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 2L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new long[]{ -1L, -1L, -1L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new long[]{ 0L, 5L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new long[]{ 0L, 3L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().sub(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getArrayData()[1] != 2L) {
			fail("Incorrect output value");
		}
		if (output.getArrayData()[0] != -1L || output.getArrayData()[2] != -1L) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testSubInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setArrayData( new long[]{ -1L, -1L, -1L }, 0, new int[]{3} );
		inputA.setArrayData( new long[]{ 5L, 1L, 7L }, 0, new int[]{3} );
		inputB.setArrayData( new long[]{ 3L, 8L, 7L }, 0, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().sub(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 2L || output.getArrayData()[1] != -7L || output.getArrayData()[2] != 0L) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// sub, FLOAT64 (double)
	// --------------------------------------------------

	@Test
	public void testSubFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new double[]{ -1.0 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ 0.5 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );  // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.125 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().sub(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 0.375) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new double[]{ -1.0, -1.0, -1.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ 0.0, 0.5,  0.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.0, 0.125, 0.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().sub(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getArrayData()[1] != 0.375) {
			fail("Incorrect output value");
		}
		if (output.getArrayData()[0] != -1.0 || output.getArrayData()[2] != -1.0) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testSubFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new double[]{ -1.0, -1.0, -1.0 }, 0, new int[]{3} );
		inputA.setArrayData( new double[]{ 1.0, 0.5, 0.03125 }, 0, new int[]{3} );         // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.125, 0.0625, 0.25 }, 0, new int[]{3} ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().sub(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=0.875 || output.getArrayData()[1]!=0.4375 || output.getArrayData()[2]!=-0.21875) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// sub, Unoperatable type (e.g. BOOL)
	// --------------------------------------------------

	@Test
	public void testSubUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().sub(DataType.BOOL, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().sub(DataType.INT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().sub(DataType.FLOAT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}





	// ==================================================
	// nul
	// ==================================================

	// --------------------------------------------------
	// nul, INT64 (long)
	// --------------------------------------------------

	@Test
	public void testMulInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setArrayData( new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new long[]{ 5L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new long[]{ 3L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().mul(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 15L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new long[]{ -1L, -1L, -1L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new long[]{ 0L, 5L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new long[]{ 0L, 3L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().mul(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getArrayData()[1] != 15L) {
			fail("Incorrect output value");
		}
		if (output.getArrayData()[0] != -1L || output.getArrayData()[2] != -1L) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testMulInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setArrayData( new long[]{ -1L, -1L, -1L }, 0, new int[]{3} );
		inputA.setArrayData( new long[]{ 5L, 1L, -7L }, 0, new int[]{3} );
		inputB.setArrayData( new long[]{ 3L, -8L, -2L }, 0, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().mul(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=15L || output.getArrayData()[1]!=-8L || output.getArrayData()[2]!=14L) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// nul, FLOAT64 (double)
	// --------------------------------------------------

	@Test
	public void testMulFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new double[]{ -1.0 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ 0.5 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );   // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.125 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().mul(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 0.0625) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new double[]{ -1.0, -1.0, -1.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ 0.0, 0.5,  0.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );  // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.0, 0.125, 0.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().mul(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getArrayData()[1] != 0.0625) {
			fail("Incorrect output value");
		}
		if (output.getArrayData()[0] != -1.0 || output.getArrayData()[2] != -1.0) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testMulFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new double[]{ -1.0, -1.0, -1.0 }, 0, new int[]{3} );
		inputA.setArrayData( new double[]{ 1.0, -0.5, -0.03125 }, 0, new int[]{3} );  // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.125, 0.0625, -0.25 }, 0, new int[]{3} ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().mul(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=0.125 || output.getArrayData()[1]!=-0.03125 || output.getArrayData()[2]!=0.0078125) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// nul, Unoperatable type (e.g. BOOL)
	// --------------------------------------------------

	@Test
	public void testMulUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().mul(DataType.BOOL, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().mul(DataType.INT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().mul(DataType.FLOAT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}





	// ==================================================
	// div
	// ==================================================

	// --------------------------------------------------
	// div, INT64 (long)
	// --------------------------------------------------

	@Test
	public void testDivInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setArrayData( new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new long[]{ 8L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new long[]{ 3L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().div(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 2L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new long[]{ -1L, -1L, -1L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new long[]{ 0L, 8L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new long[]{ 0L, 3L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().div(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getArrayData()[1] != 2L) {
			fail("Incorrect output value");
		}
		if (output.getArrayData()[0] != -1L || output.getArrayData()[2] != -1L) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testDivInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setArrayData( new long[]{ -1L, -1L, -1L }, 0, new int[]{3} );
		inputA.setArrayData( new long[]{ 8L, 5L, -7L }, 0, new int[]{3} );
		inputB.setArrayData( new long[]{ 3L, -1L, -2L }, 0, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().div(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=2L || output.getArrayData()[1]!=-5L || output.getArrayData()[2]!=3L) {
			fail("Incorrect output value");
		}
	}



	// --------------------------------------------------
	// div, FLOAT64 (double)
	// --------------------------------------------------

	@Test
	public void testDivFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new double[]{ -1.0 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ 0.5 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );   // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.125 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().div(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 4.0) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new double[]{ -1.0, -1.0, -1.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ 0.0, 0.5,  0.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );  // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.0, 0.125, 0.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().div(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getArrayData()[1] != 4.0) {
			fail("Incorrect output value");
		}
		if (output.getArrayData()[0] != -1.0 || output.getArrayData()[2] != -1.0) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testDivFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new double[]{ -1.0, -1.0, -1.0 }, 0, new int[]{3} );
		inputA.setArrayData( new double[]{ 1.0, -0.5, -0.03125 }, 0, new int[]{3} );  // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.125, 0.0625, -0.25 }, 0, new int[]{3} ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().div(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=8.0 || output.getArrayData()[1]!=-8.0 || output.getArrayData()[2]!=0.125) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// div, Unoperatable type (e.g. BOOL)
	// --------------------------------------------------

	@Test
	public void testDivUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().div(DataType.BOOL, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().div(DataType.INT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().div(DataType.FLOAT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}





	// ==================================================
	// rem
	// ==================================================

	// --------------------------------------------------
	// rem, INT64 (long)
	// --------------------------------------------------

	@Test
	public void testRemInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setArrayData( new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new long[]{ 8L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new long[]{ 3L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().rem(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 2L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new long[]{ -1L, -1L, -1L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new long[]{ 0L, 8L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new long[]{ 0L, 3L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().rem(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getArrayData()[1] != 2L) {
			fail("Incorrect output value");
		}
		if (output.getArrayData()[0] != -1L || output.getArrayData()[2] != -1L) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testRemInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setArrayData( new long[]{ -1L, -1L, -1L, -1L }, 0, new int[]{4} );
		inputA.setArrayData( new long[]{ 8L, 8L, -8L, -8L }, 0, new int[]{4} );
		inputB.setArrayData( new long[]{ 3L, -3L, 3L, -3L }, 0, new int[]{4} );

		// 演算を実行
		try {
			new ExecutionUnit().rem(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=2L || output.getArrayData()[1]!=2L || output.getArrayData()[2]!=-2L || output.getArrayData()[3]!=-2L) {
			fail("Incorrect output value");
		}
	}



	// --------------------------------------------------
	// rem, FLOAT64 (double)
	// --------------------------------------------------

	@Test
	public void testRemFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new double[]{ -1.0 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ 0.8 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new double[]{ 0.3 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().rem(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (FLOAT64_PERMISSIBLE_ERROR < Math.abs(output.getArrayData()[0]-0.2)) { // 期待値 0.2
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new double[]{ -1.0, -1.0, -1.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ 0.0, 0.8,  0.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new double[]{ 0.0, 0.3, 0.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().rem(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (FLOAT64_PERMISSIBLE_ERROR < Math.abs(output.getArrayData()[1]-0.2)) { // 期待値 0.2
			fail("Incorrect output value");
		}
		if (output.getArrayData()[0] != -1.0 || output.getArrayData()[2] != -1.0) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testRemFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new double[]{ -1.0, -1.0, -1.0, -1.0, -1.0 }, 0, new int[]{5} );
		inputA.setArrayData( new double[]{ 1.0, 0.8, 0.8, -0.8, -0.8 }, 0, new int[]{5} );
		inputB.setArrayData( new double[]{ 0.125, 0.3, -0.3, 0.3, -0.3 }, 0, new int[]{5} );

		// 演算を実行
		try {
			new ExecutionUnit().rem(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=0.0
				|| FLOAT64_PERMISSIBLE_ERROR < Math.abs(output.getArrayData()[1]-0.2)    // 期待値 0.2
				|| FLOAT64_PERMISSIBLE_ERROR < Math.abs(output.getArrayData()[2]-0.2)    // 期待値 0.2
				|| FLOAT64_PERMISSIBLE_ERROR < Math.abs(output.getArrayData()[3]+0.2)    // 期待値 -0.2
				|| FLOAT64_PERMISSIBLE_ERROR < Math.abs(output.getArrayData()[4]+0.2)) { // 期待値 -0.2

			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// rem, Unoperatable type (e.g. BOOL)
	// --------------------------------------------------

	@Test
	public void testRemUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().rem(DataType.BOOL, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().rem(DataType.INT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().rem(DataType.FLOAT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}





	// ==================================================
	// neg
	// ==================================================

	// --------------------------------------------------
	// neg, INT64 (long)
	// --------------------------------------------------

	@Test
	public void testNegInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setArrayData( new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new long[]{ 8L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().neg(DataType.INT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != -8L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new long[]{ -1L, -1L, -1L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new long[]{ 0L, 8L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().neg(DataType.INT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getArrayData()[1] != -8L) {
			fail("Incorrect output value");
		}
		if (output.getArrayData()[0] != -1L || output.getArrayData()[2] != -1L) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testNegInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setArrayData( new long[]{ -1L, -1L, -1L }, 0, new int[]{3} );
		input.setArrayData( new long[]{ 0L, 5L, -7L }, 0, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().neg(DataType.INT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=0L || output.getArrayData()[1]!=-5L || output.getArrayData()[2]!=7L) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// neg, INT64 (long)
	// --------------------------------------------------

	@Test
	public void testNegFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setArrayData( new double[]{ -1.0 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new double[]{ 0.8 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().neg(DataType.FLOAT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != -0.8) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new double[]{ -1.0, -1.0, -1.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new double[]{ 0, 0.8, 0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().neg(DataType.FLOAT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getArrayData()[1] != -0.8) {
			fail("Incorrect output value");
		}
		if (output.getArrayData()[0] != -1.0 || output.getArrayData()[2] != -1.0) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testNegFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setArrayData( new double[]{ -1.0, -1.0, -1.0 }, 0, new int[]{3} );
		input.setArrayData( new double[]{ 0L, 0.8, -0.2 }, 0, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().neg(DataType.FLOAT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=0.0 || output.getArrayData()[1]!=-0.8 || output.getArrayData()[2]!=0.2) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// neg, Unoperatable type (e.g. BOOL)
	// --------------------------------------------------

	@Test
	public void testNegUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().neg(DataType.BOOL, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().neg(DataType.INT64, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().neg(DataType.FLOAT64, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}





	// ==================================================
	// eq
	// ==================================================

	// --------------------------------------------------
	// eq, INT64 (long)
	// --------------------------------------------------

	@Test
	public void testEqInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new long[]{ 1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new long[]{ 2L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().eq(DataType.INT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new long[]{ 0L, 1L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new long[]{ 0L, 2L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().eq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().eq(DataType.INT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getArrayData()[0] != false || output.getArrayData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testEqInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setArrayData( new boolean[]{ false, false, false }, 0, new int[]{3} );
		inputA.setArrayData( new long[]{ 0L, 1L, 2L }, 0, new int[]{3} );
		inputB.setArrayData( new long[]{ 0L, 1L, 3L }, 0, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=true || output.getArrayData()[1]!=true || output.getArrayData()[2]!=false) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// eq, FLOAT64 (long)
	// --------------------------------------------------

	@Test
	public void testEqFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ 0.125 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.5 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );   // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().eq(DataType.FLOAT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ -1.0, 0.125, -1.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ -1.0, 0.5, -1.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );   // 2進表現で割り切れる値
		try {
			new ExecutionUnit().eq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().eq(DataType.FLOAT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getArrayData()[0] != false || output.getArrayData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testEqFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new boolean[]{ false, false, false }, 0, new int[]{3} );
		inputA.setArrayData( new double[]{ 0.0, 0.125, 0.5  }, 0, new int[]{3} );   // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.0, 0.125, 0.25 }, 0, new int[]{3} ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=true || output.getArrayData()[1]!=true || output.getArrayData()[2]!=false) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// eq, string (String)
	// --------------------------------------------------

	@Test
	public void testEqStringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<String[]> inputA = new DataContainer<String[]>();
		DataContainer<String[]> inputB = new DataContainer<String[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new String[]{ "Hello" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new String[]{ "World" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.STRING, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().eq(DataType.STRING, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new String[]{ "", "Hello", "" }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new String[]{ "", "World", "" }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().eq(DataType.STRING, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().eq(DataType.STRING, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getArrayData()[0] != false || output.getArrayData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testEqStringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<String[]> inputA = new DataContainer<String[]>();
		DataContainer<String[]> inputB = new DataContainer<String[]>();
		output.setArrayData( new boolean[]{ false, false, false }, 0, new int[]{3} );
		inputA.setArrayData( new String[]{ "", "Hello", "Good"  }, 0, new int[]{3} );
		inputB.setArrayData( new String[]{ "", "Hello", "Morning" }, 0, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.STRING, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=true || output.getArrayData()[1]!=true || output.getArrayData()[2]!=false) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// eq, bool (boolean)
	// --------------------------------------------------

	@Test
	public void testEqBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().eq(DataType.BOOL, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new boolean[]{ false, true, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().eq(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().eq(DataType.BOOL, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getArrayData()[0] != false || output.getArrayData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testEqBoolVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false, false, false }, 0, new int[]{3} );
		inputA.setArrayData( new boolean[]{ false, true, true  }, 0, new int[]{3} );
		inputB.setArrayData( new boolean[]{ false, true, false }, 0, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=true || output.getArrayData()[1]!=true || output.getArrayData()[2]!=false) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// eq, Unoperatable type (e.g. VOID)
	// --------------------------------------------------

	@Test
	public void testEqUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<Object[]> inputA = new DataContainer<Object[]>();
		DataContainer<Object[]> inputB = new DataContainer<Object[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new Object[]{ null }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new Object[]{ null }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().eq(DataType.VOID, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}





	// ==================================================
	// neq
	// ==================================================

	// --------------------------------------------------
	// neq, INT64 (long)
	// --------------------------------------------------

	@Test
	public void testNeqInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new long[]{ 1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new long[]{ 2L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().neq(DataType.INT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new long[]{ 0L, 1L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new long[]{ 0L, 2L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().neq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().neq(DataType.INT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getArrayData()[0] != false || output.getArrayData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testNeqInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setArrayData( new boolean[]{ false, false, false }, 0, new int[]{3} );
		inputA.setArrayData( new long[]{ 0L, 1L, 2L }, 0, new int[]{3} );
		inputB.setArrayData( new long[]{ 0L, 1L, 3L }, 0, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=false || output.getArrayData()[1]!=false || output.getArrayData()[2]!=true) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// neq, FLOAT64 (long)
	// --------------------------------------------------

	@Test
	public void testNeqFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ 0.125 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.5 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );   // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().neq(DataType.FLOAT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ -1.0, 0.125, -1.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ -1.0, 0.5, -1.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );   // 2進表現で割り切れる値
		try {
			new ExecutionUnit().neq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().neq(DataType.FLOAT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getArrayData()[0] != false || output.getArrayData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testNeqFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new boolean[]{ false, false, false }, 0, new int[]{3} );
		inputA.setArrayData( new double[]{ 0.0, 0.125, 0.5  }, 0, new int[]{3} );   // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.0, 0.125, 0.25 }, 0, new int[]{3} ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=false || output.getArrayData()[1]!=false || output.getArrayData()[2]!=true) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// neq, string (String)
	// --------------------------------------------------

	@Test
	public void testNeqStringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<String[]> inputA = new DataContainer<String[]>();
		DataContainer<String[]> inputB = new DataContainer<String[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new String[]{ "Hello" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new String[]{ "World" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.STRING, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().neq(DataType.STRING, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new String[]{ "", "Hello", "" }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new String[]{ "", "World", "" }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().neq(DataType.STRING, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().neq(DataType.STRING, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getArrayData()[0] != false || output.getArrayData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testNeqStringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<String[]> inputA = new DataContainer<String[]>();
		DataContainer<String[]> inputB = new DataContainer<String[]>();
		output.setArrayData( new boolean[]{ false, false, false }, 0, new int[]{3} );
		inputA.setArrayData( new String[]{ "", "Hello", "Good"  }, 0, new int[]{3} );
		inputB.setArrayData( new String[]{ "", "Hello", "Morning" }, 0, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.STRING, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=false || output.getArrayData()[1]!=false || output.getArrayData()[2]!=true) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// neq, bool (boolean)
	// --------------------------------------------------

	@Test
	public void testNeqBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().neq(DataType.BOOL, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new boolean[]{ false, true, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().neq(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().neq(DataType.BOOL, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getArrayData()[0] != false || output.getArrayData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testNeqBoolVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false, false, false }, 0, new int[]{3} );
		inputA.setArrayData( new boolean[]{ false, true, true  }, 0, new int[]{3} );
		inputB.setArrayData( new boolean[]{ false, true, false }, 0, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=false || output.getArrayData()[1]!=false || output.getArrayData()[2]!=true) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// neq, Unoperatable type (e.g. VOID)
	// --------------------------------------------------

	@Test
	public void testNeqUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<Object[]> inputA = new DataContainer<Object[]>();
		DataContainer<Object[]> inputB = new DataContainer<Object[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new Object[]{ null }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new Object[]{ null }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().neq(DataType.VOID, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}


	// ==================================================
	// geq
	// ==================================================

	// --------------------------------------------------
	// geq, INT64 (long)
	// --------------------------------------------------

	@Test
	public void testGeqInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new long[]{ 1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new long[]{ 2L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().geq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().geq(DataType.INT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().geq(DataType.INT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new long[]{ 0L, 1L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new long[]{ 0L, 2L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().geq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().geq(DataType.INT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().geq(DataType.INT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getArrayData()[0] != false || output.getArrayData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testGeqInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setArrayData( new boolean[]{ false, false, false }, 0, new int[]{3} );
		inputA.setArrayData( new long[]{ 1L, 2L, 3L }, 0, new int[]{3} );
		inputB.setArrayData( new long[]{ 1L, 3L, 2L }, 0, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().geq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=true || output.getArrayData()[1]!=false || output.getArrayData()[2]!=true) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// geq, FLOAT64 (long)
	// --------------------------------------------------

	@Test
	public void testGeqFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ 0.125 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.5 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );   // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().geq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().geq(DataType.FLOAT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().geq(DataType.FLOAT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ 0.0, 0.125, 0.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.0, 0.5,   0.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().geq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().geq(DataType.FLOAT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().geq(DataType.FLOAT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getArrayData()[0] != false || output.getArrayData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testGeqFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new boolean[]{ false, false, false }, 0, new int[]{3} );
		inputA.setArrayData( new double[]{ 0.5, 0.125, 0.25 }, 0, new int[]{3} ); // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.5, 0.25, 0.125 }, 0, new int[]{3} ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().geq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=true || output.getArrayData()[1]!=false || output.getArrayData()[2]!=true) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// geq, Unoperatable type (e.g. BOOL)
	// --------------------------------------------------

	@Test
	public void testGeqUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().geq(DataType.BOOL, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().geq(DataType.INT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().geq(DataType.FLOAT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}



	// ==================================================
	// leq
	// ==================================================

	// --------------------------------------------------
	// leq, INT64 (long)
	// --------------------------------------------------

	@Test
	public void testLeqInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new long[]{ 1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new long[]{ 2L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().leq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().leq(DataType.INT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().leq(DataType.INT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new long[]{ 0L, 1L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new long[]{ 0L, 2L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().leq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().leq(DataType.INT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().leq(DataType.INT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getArrayData()[0] != false || output.getArrayData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testLeqInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setArrayData( new boolean[]{ false, false, false }, 0, new int[]{3} );
		inputA.setArrayData( new long[]{ 1L, 2L, 3L }, 0, new int[]{3} );
		inputB.setArrayData( new long[]{ 1L, 3L, 2L }, 0, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().leq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=true || output.getArrayData()[1]!=true || output.getArrayData()[2]!=false) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// leq, FLOAT64 (long)
	// --------------------------------------------------

	@Test
	public void testLeqFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ 0.125 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.5 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );   // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().leq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().leq(DataType.FLOAT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().leq(DataType.FLOAT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ 0.0, 0.125, 0.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.0, 0.5,   0.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().leq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().leq(DataType.FLOAT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().leq(DataType.FLOAT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getArrayData()[0] != false || output.getArrayData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testLeqFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new boolean[]{ false, false, false }, 0, new int[]{3} );
		inputA.setArrayData( new double[]{ 0.5, 0.125, 0.25 }, 0, new int[]{3} ); // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.5, 0.25, 0.125 }, 0, new int[]{3} ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().leq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=true || output.getArrayData()[1]!=true || output.getArrayData()[2]!=false) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// leq, Unoperatable type (e.g. BOOL)
	// --------------------------------------------------

	@Test
	public void testLeqUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().leq(DataType.BOOL, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().leq(DataType.INT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().leq(DataType.FLOAT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}





	// ==================================================
	// gt
	// ==================================================

	// --------------------------------------------------
	// gt, INT64 (long)
	// --------------------------------------------------

	@Test
	public void testGtInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new long[]{ 1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new long[]{ 2L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().gt(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().gt(DataType.INT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().gt(DataType.INT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new long[]{ 0L, 1L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new long[]{ 0L, 2L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().gt(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().gt(DataType.INT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().gt(DataType.INT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getArrayData()[0] != false || output.getArrayData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testGtInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setArrayData( new boolean[]{ false, false, false }, 0, new int[]{3} );
		inputA.setArrayData( new long[]{ 1L, 2L, 3L }, 0, new int[]{3} );
		inputB.setArrayData( new long[]{ 1L, 3L, 2L }, 0, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().gt(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=false || output.getArrayData()[1]!=false || output.getArrayData()[2]!=true) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// gt, FLOAT64 (long)
	// --------------------------------------------------

	@Test
	public void testGtFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ 0.125 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.5 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );   // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().gt(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().gt(DataType.FLOAT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().gt(DataType.FLOAT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ 0.0, 0.125, 0.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.0, 0.5,   0.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().gt(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().gt(DataType.FLOAT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().gt(DataType.FLOAT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getArrayData()[0] != false || output.getArrayData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testGtFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new boolean[]{ false, false, false }, 0, new int[]{3} );
		inputA.setArrayData( new double[]{ 0.5, 0.125, 0.25 }, 0, new int[]{3} ); // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.5, 0.25, 0.125 }, 0, new int[]{3} ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().gt(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=false || output.getArrayData()[1]!=false || output.getArrayData()[2]!=true) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// gt, Unoperatable type (e.g. BOOL)
	// --------------------------------------------------

	@Test
	public void testGtUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().gt(DataType.BOOL, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().gt(DataType.INT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().gt(DataType.FLOAT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}



	// ==================================================
	// lt
	// ==================================================

	// --------------------------------------------------
	// lt, INT64 (long)
	// --------------------------------------------------

	@Test
	public void testLtInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new long[]{ 1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new long[]{ 2L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().lt(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().lt(DataType.INT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().lt(DataType.INT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new long[]{ 0L, 1L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new long[]{ 0L, 2L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().lt(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().lt(DataType.INT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().lt(DataType.INT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getArrayData()[0] != false || output.getArrayData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testLtInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setArrayData( new boolean[]{ false, false, false }, 0, new int[]{3} );
		inputA.setArrayData( new long[]{ 1L, 2L, 3L }, 0, new int[]{3} );
		inputB.setArrayData( new long[]{ 1L, 3L, 2L }, 0, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().lt(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=false || output.getArrayData()[1]!=true || output.getArrayData()[2]!=false) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// lt, FLOAT64 (long)
	// --------------------------------------------------

	@Test
	public void testLtFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ 0.125 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.5 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );   // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().lt(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().lt(DataType.FLOAT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().lt(DataType.FLOAT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ 0.0, 0.125, 0.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.0, 0.5,   0.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().lt(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().lt(DataType.FLOAT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().lt(DataType.FLOAT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getArrayData()[0] != false || output.getArrayData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testLtFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new boolean[]{ false, false, false }, 0, new int[]{3} );
		inputA.setArrayData( new double[]{ 0.5, 0.125, 0.25 }, 0, new int[]{3} ); // 2進表現で割り切れる値
		inputB.setArrayData( new double[]{ 0.5, 0.25, 0.125 }, 0, new int[]{3} ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().lt(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=false || output.getArrayData()[1]!=true || output.getArrayData()[2]!=false) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// lt, Unoperatable type (e.g. BOOL)
	// --------------------------------------------------

	@Test
	public void testLtUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().lt(DataType.BOOL, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().lt(DataType.INT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().lt(DataType.FLOAT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}



	// ==================================================
	// and
	// ==================================================

	// --------------------------------------------------
	// and, bool (boolean)
	// --------------------------------------------------

	@Test
	public void testAndBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputB, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new boolean[]{ false, true, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputB, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getArrayData()[0] != false || output.getArrayData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testAndBoolVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false, false, false, false }, 0, new int[]{4} );
		inputA.setArrayData( new boolean[]{ false, true, false, true }, 0, new int[]{4} );
		inputB.setArrayData( new boolean[]{ false, false, true, true }, 0, new int[]{4} );

		// 演算を実行
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=false
				|| output.getArrayData()[1]!=false
				|| output.getArrayData()[2]!=false
				|| output.getArrayData()[3]!=true) {

			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// and, Unoperatable type (e.g. FLOAT64)
	// --------------------------------------------------

	@Test
	public void testAndUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ 0.125 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new double[]{ 0.25 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().and(DataType.FLOAT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}



	// ==================================================
	// or
	// ==================================================

	// --------------------------------------------------
	// or, bool (boolean)
	// --------------------------------------------------

	@Test
	public void testOrBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputB, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new boolean[]{ false, true, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputB, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getArrayData()[0] != false || output.getArrayData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testOrBoolVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false, false, false, false }, 0, new int[]{4} );
		inputA.setArrayData( new boolean[]{ false, true, false, true }, 0, new int[]{4} );
		inputB.setArrayData( new boolean[]{ false, false, true, true }, 0, new int[]{4} );

		// 演算を実行
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=false
				|| output.getArrayData()[1]!=true
				|| output.getArrayData()[2]!=true
				|| output.getArrayData()[3]!=true) {

			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// or, Unoperatable type (e.g. FLOAT64)
	// --------------------------------------------------

	@Test
	public void testOrUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new double[]{ 0.125 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new double[]{ 0.25 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().or(DataType.FLOAT64, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputA, inputB);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}



	// ==================================================
	// not
	// ==================================================

	// --------------------------------------------------
	// not, bool (boolean)
	// --------------------------------------------------

	@Test
	public void testNotBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().not(DataType.BOOL, output, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オペランドを変えて演算を実行
		try {
			new ExecutionUnit().not(DataType.BOOL, output, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputA.setArrayData( new boolean[]{ false, true, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		inputB.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().not(DataType.BOOL, output, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().not(DataType.BOOL, output, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[1] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}
	}

	@Test
	public void testNotBoolVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false, false}, 0, new int[]{2} );
		input.setArrayData( new boolean[]{ false, true }, 0, new int[]{2} );

		// 演算を実行
		try {
			new ExecutionUnit().not(DataType.BOOL, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=true || output.getArrayData()[1]!=false) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// not, Unoperatable type (e.g. FLOAT64)
	// --------------------------------------------------

	@Test
	public void testNotUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setArrayData( new double[]{ -1.0 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new double[]{ 0.125 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().not(DataType.FLOAT64, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().not(DataType.BOOL, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}





	// ==================================================
	// alloc
	// ==================================================

	// --------------------------------------------------
	// alloc, INT64 (long)
	// --------------------------------------------------

	@Test
	public void testAllocInt64Scalar() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();

		// 確保処理を実行
		try {
			new ExecutionUnit().allocScalar(DataType.INT64, target);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof long[])
				|| ((long[])data).length <= target.getArrayOffset()
				|| target.getArraySize() != 1
				|| target.getArrayLengths().length != 0) {

			fail("Incorrect allocated data");
		}
	}

	@Test
	public void testAllocInt64Array1D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<long[]> len = new DataContainer<long[]>();
		len.setArrayData(new long[]{ 3L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocVector(DataType.INT64, target, len);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof long[])
				|| ((long[])data).length != 3
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 3
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 3) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocも検査
		len.setArrayData(new long[]{ 5L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().allocVector(DataType.INT64, target, len);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof long[])
				|| ((long[])data).length != 5
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 5
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		len.setArrayData(new long[]{ 5L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().allocVector(DataType.INT64, target, len);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof long[])
				|| ((long[])data).length != 5
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 5
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}
	}

	@Test
	public void testAllocInt64Array3D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();

		DataContainer<long[]> len0 = new DataContainer<long[]>();
		DataContainer<long[]> len1 = new DataContainer<long[]>();
		DataContainer<long[]> len2 = new DataContainer<long[]>();
		len0.setArrayData(new long[] { 2L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len1.setArrayData(new long[] { 3L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len2.setArrayData(new long[] { 4L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocVector(DataType.INT64, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof long[])
				|| ((long[])data).length != 24
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 24
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 2
				|| target.getArrayLengths()[1] != 3
				|| target.getArrayLengths()[2] != 4) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocも検査
		len0.setArrayData(new long[] { 5L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len1.setArrayData(new long[] { 6L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len2.setArrayData(new long[] { 7L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().allocVector(DataType.INT64, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof long[])
				|| ((long[])data).length != 210
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 210
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 5
				|| target.getArrayLengths()[1] != 6
				|| target.getArrayLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		len0.setArrayData(new long[] { 5L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len1.setArrayData(new long[] { 6L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len2.setArrayData(new long[] { 7L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().allocVector(DataType.INT64, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof long[])
				|| ((long[])data).length != 210
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 210
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 5
				|| target.getArrayLengths()[1] != 6
				|| target.getArrayLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}
	}


	// --------------------------------------------------
	// alloc, FLOAT64 (double)
	// --------------------------------------------------

	@Test
	public void testAllocFloat64Scalar() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();

		// 確保処理を実行
		try {
			new ExecutionUnit().allocScalar(DataType.FLOAT64, target);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof double[])
				|| ((double[])data).length <= target.getArrayOffset()
				|| target.getArraySize() != 1
				|| target.getArrayLengths().length != 0) {

			fail("Incorrect allocated data");
		}
	}

	@Test
	public void testAllocFloat64Array1D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<long[]> len = new DataContainer<long[]>();
		len.setArrayData(new long[]{ 3L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocVector(DataType.FLOAT64, target, len);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof double[])
				|| ((double[])data).length != 3
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 3
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 3) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocも検査
		len.setArrayData(new long[]{ 5L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().allocVector(DataType.FLOAT64, target, len);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof double[])
				|| ((double[])data).length != 5
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 5
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		len.setArrayData(new long[]{ 5L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().allocVector(DataType.FLOAT64, target, len);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof double[])
				|| ((double[])data).length != 5
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 5
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}
	}

	@Test
	public void testAllocFloat64Array3D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();

		DataContainer<long[]> len0 = new DataContainer<long[]>();
		DataContainer<long[]> len1 = new DataContainer<long[]>();
		DataContainer<long[]> len2 = new DataContainer<long[]>();
		len0.setArrayData(new long[] { 2L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len1.setArrayData(new long[] { 3L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len2.setArrayData(new long[] { 4L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocVector(DataType.FLOAT64, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof double[])
				|| ((double[])data).length != 24
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 24
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 2
				|| target.getArrayLengths()[1] != 3
				|| target.getArrayLengths()[2] != 4) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocも検査
		len0.setArrayData(new long[] { 5L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len1.setArrayData(new long[] { 6L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len2.setArrayData(new long[] { 7L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().allocVector(DataType.FLOAT64, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof double[])
				|| ((double[])data).length != 210
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 210
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 5
				|| target.getArrayLengths()[1] != 6
				|| target.getArrayLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		len0.setArrayData(new long[] { 5L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len1.setArrayData(new long[] { 6L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len2.setArrayData(new long[] { 7L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().allocVector(DataType.FLOAT64, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof double[])
				|| ((double[])data).length != 210
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 210
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 5
				|| target.getArrayLengths()[1] != 6
				|| target.getArrayLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}
	}


	// --------------------------------------------------
	// alloc, bool (boolean)
	// --------------------------------------------------

	@Test
	public void testAllocBoolScalar() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();

		// 確保処理を実行
		try {
			new ExecutionUnit().allocScalar(DataType.BOOL, target);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof boolean[])
				|| ((boolean[])data).length <= target.getArrayOffset()
				|| target.getArraySize() != 1
				|| target.getArrayLengths().length != 0) {

			fail("Incorrect allocated data");
		}
	}

	@Test
	public void testAllocBoolArray1D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<long[]> len = new DataContainer<long[]>();
		len.setArrayData(new long[]{ 3L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocVector(DataType.BOOL, target, len);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof boolean[])
				|| ((boolean[])data).length != 3
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 3
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 3) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocも検査
		len.setArrayData(new long[]{ 5L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().allocVector(DataType.BOOL, target, len);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof boolean[])
				|| ((boolean[])data).length != 5
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 5
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		len.setArrayData(new long[]{ 5L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().allocVector(DataType.BOOL, target, len);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof boolean[])
				|| ((boolean[])data).length != 5
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 5
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}
	}

	@Test
	public void testAllocBoolArray3D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();

		DataContainer<long[]> len0 = new DataContainer<long[]>();
		DataContainer<long[]> len1 = new DataContainer<long[]>();
		DataContainer<long[]> len2 = new DataContainer<long[]>();
		len0.setArrayData(new long[] { 2L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len1.setArrayData(new long[] { 3L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len2.setArrayData(new long[] { 4L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocVector(DataType.BOOL, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof boolean[])
				|| ((boolean[])data).length != 24
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 24
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 2
				|| target.getArrayLengths()[1] != 3
				|| target.getArrayLengths()[2] != 4) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocも検査
		len0.setArrayData(new long[] { 5L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len1.setArrayData(new long[] { 6L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len2.setArrayData(new long[] { 7L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().allocVector(DataType.BOOL, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof boolean[])
				|| ((boolean[])data).length != 210
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 210
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 5
				|| target.getArrayLengths()[1] != 6
				|| target.getArrayLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		len0.setArrayData(new long[] { 5L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len1.setArrayData(new long[] { 6L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len2.setArrayData(new long[] { 7L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().allocVector(DataType.BOOL, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof boolean[])
				|| ((boolean[])data).length != 210
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 210
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 5
				|| target.getArrayLengths()[1] != 6
				|| target.getArrayLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}
	}


	// --------------------------------------------------
	// alloc, string (String)
	// --------------------------------------------------

	@Test
	public void testAllocStringScalar() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();

		// 確保処理を実行
		try {
			new ExecutionUnit().allocScalar(DataType.STRING, target);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof String[])
				|| ((String[])data).length <= target.getArrayOffset()
				|| target.getArraySize() != 1
				|| target.getArrayLengths().length != 0) {

			fail("Incorrect allocated data");
		}
	}

	@Test
	public void testAllocStringArray1D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<long[]> len = new DataContainer<long[]>();
		len.setArrayData(new long[]{ 3L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocVector(DataType.STRING, target, len);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof String[])
				|| ((String[])data).length != 3
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 3
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 3) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocも検査
		len.setArrayData(new long[]{ 5L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().allocVector(DataType.STRING, target, len);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof String[])
				|| ((String[])data).length != 5
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 5
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		len.setArrayData(new long[]{ 5L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().allocVector(DataType.STRING, target, len);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof String[])
				|| ((String[])data).length != 5
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 5
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}
	}

	@Test
	public void testAllocStringArray3D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();

		DataContainer<long[]> len0 = new DataContainer<long[]>();
		DataContainer<long[]> len1 = new DataContainer<long[]>();
		DataContainer<long[]> len2 = new DataContainer<long[]>();
		len0.setArrayData(new long[] { 2L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len1.setArrayData(new long[] { 3L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len2.setArrayData(new long[] { 4L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocVector(DataType.STRING, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof String[])
				|| ((String[])data).length != 24
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 24
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 2
				|| target.getArrayLengths()[1] != 3
				|| target.getArrayLengths()[2] != 4) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocも検査
		len0.setArrayData(new long[] { 5L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len1.setArrayData(new long[] { 6L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len2.setArrayData(new long[] { 7L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().allocVector(DataType.STRING, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof String[])
				|| ((String[])data).length != 210
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 210
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 5
				|| target.getArrayLengths()[1] != 6
				|| target.getArrayLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		len0.setArrayData(new long[] { 5L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len1.setArrayData(new long[] { 6L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len2.setArrayData(new long[] { 7L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().allocVector(DataType.STRING, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof String[])
				|| ((String[])data).length != 210
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 210
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 5
				|| target.getArrayLengths()[1] != 6
				|| target.getArrayLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}
	}

	// --------------------------------------------------
	// alloc, Unoperatable type (e.g. VOID)
	// --------------------------------------------------

	@Test
	public void testAllocUnoperatableData() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> target = new DataContainer<boolean[]>();

		// スカラの確保処理を実行
		try {
			new ExecutionUnit().allocScalar(DataType.VOID, target);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}

		// 配列の確保でもテスト
		DataContainer<long[]> len = new DataContainer<long[]>();
		len.setArrayData(new long[]{ 3 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().allocVector(DataType.VOID, target, len);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}

	}



	// ==================================================
	// allocr
	// ==================================================

	// --------------------------------------------------
	// allocr, INT64 (long)
	// --------------------------------------------------

	@Test
	public void testAllocrInt64Scalar() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<long[]> src = new DataContainer<long[]>();
		src.setArrayData(new long[1], 0, new int[0]);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.INT64, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof long[])
				|| ((long[])data).length <= target.getArrayOffset()
				|| target.getArraySize() != 1
				|| target.getArrayLengths().length != 0) {

			fail("Incorrect allocated data");
		}
	}

	@Test
	public void testAllocrInt64Array1D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<Object> target = new DataContainer<Object>();
		DataContainer<long[]> src = new DataContainer<long[]>();
		src.setArrayData(new long[3], 0, new int[] { 3 });

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.INT64, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof long[])
				|| ((long[])data).length != 3
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 3
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 3) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocrも検査
		src.setArrayData(new long[5], 0, new int[] { 5 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.INT64, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof long[])
				|| ((long[])data).length != 5
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 5
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		src.setArrayData(new long[5], 0, new int[] { 5 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.INT64, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof long[])
				|| ((long[])data).length != 5
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 5
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}
	}

	@Test
	public void testAllocrInt64Array3D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<long[]> src = new DataContainer<long[]>();
		src.setArrayData(new long[ 2 * 3 * 4 ], 0, new int[] { 2, 3, 4 });

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.INT64, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof long[])
				|| ((long[])data).length != 24
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 24
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 2
				|| target.getArrayLengths()[1] != 3
				|| target.getArrayLengths()[2] != 4) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocも検査
		src.setArrayData(new long[ 5 * 6 * 7 ], 0, new int[] { 5, 6, 7 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.INT64, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof long[])
				|| ((long[])data).length != 210
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 210
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 5
				|| target.getArrayLengths()[1] != 6
				|| target.getArrayLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		src.setArrayData(new long[ 5 * 6 * 7 ], 0, new int[] { 5, 6, 7 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.INT64, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof long[])
				|| ((long[])data).length != 210
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 210
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 5
				|| target.getArrayLengths()[1] != 6
				|| target.getArrayLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}
	}

	// --------------------------------------------------
	// allocr, FLOAT64 (double)
	// --------------------------------------------------

	@Test
	public void testAllocrFloat64Scalar() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<double[]> src = new DataContainer<double[]>();
		src.setArrayData(new double[1], 0, new int[0]);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.FLOAT64, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof double[])
				|| ((double[])data).length <= target.getArrayOffset()
				|| target.getArraySize() != 1
				|| target.getArrayLengths().length != 0) {

			fail("Incorrect allocated data");
		}
	}

	@Test
	public void testAllocrFloat64Array1D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<Object> target = new DataContainer<Object>();
		DataContainer<double[]> src = new DataContainer<double[]>();
		src.setArrayData(new double[3], 0, new int[] { 3 });

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.FLOAT64, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof double[])
				|| ((double[])data).length != 3
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 3
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 3) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocrも検査
		src.setArrayData(new double[5], 0, new int[] { 5 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.FLOAT64, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof double[])
				|| ((double[])data).length != 5
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 5
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		src.setArrayData(new double[5], 0, new int[] { 5 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.FLOAT64, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof double[])
				|| ((double[])data).length != 5
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 5
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}
	}

	@Test
	public void testAllocrFloat64Array3D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<double[]> src = new DataContainer<double[]>();
		src.setArrayData(new double[ 2 * 3 * 4 ], 0, new int[] { 2, 3, 4 });

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.FLOAT64, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof double[])
				|| ((double[])data).length != 24
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 24
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 2
				|| target.getArrayLengths()[1] != 3
				|| target.getArrayLengths()[2] != 4) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocも検査
		src.setArrayData(new double[ 5 * 6 * 7 ], 0, new int[] { 5, 6, 7 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.FLOAT64, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof double[])
				|| ((double[])data).length != 210
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 210
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 5
				|| target.getArrayLengths()[1] != 6
				|| target.getArrayLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		src.setArrayData(new double[ 5 * 6 * 7 ], 0, new int[] { 5, 6, 7 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.FLOAT64, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof double[])
				|| ((double[])data).length != 210
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 210
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 5
				|| target.getArrayLengths()[1] != 6
				|| target.getArrayLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}
	}

	// --------------------------------------------------
	// allocr, bool (boolean)
	// --------------------------------------------------

	@Test
	public void testAllocrBoolScalar() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<boolean[]> src = new DataContainer<boolean[]>();
		src.setArrayData(new boolean[1], 0, new int[0]);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.BOOL, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof boolean[])
				|| ((boolean[])data).length <= target.getArrayOffset()
				|| target.getArraySize() != 1
				|| target.getArrayLengths().length != 0) {

			fail("Incorrect allocated data");
		}
	}

	@Test
	public void testAllocrBoolArray1D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<Object> target = new DataContainer<Object>();
		DataContainer<boolean[]> src = new DataContainer<boolean[]>();
		src.setArrayData(new boolean[3], 0, new int[] { 3 });

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.BOOL, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof boolean[])
				|| ((boolean[])data).length != 3
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 3
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 3) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocrも検査
		src.setArrayData(new boolean[5], 0, new int[] { 5 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.BOOL, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof boolean[])
				|| ((boolean[])data).length != 5
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 5
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		src.setArrayData(new boolean[5], 0, new int[] { 5 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.BOOL, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof boolean[])
				|| ((boolean[])data).length != 5
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 5
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}
	}

	@Test
	public void testAllocrBoolArray3D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<boolean[]> src = new DataContainer<boolean[]>();
		src.setArrayData(new boolean[ 2 * 3 * 4 ], 0, new int[] { 2, 3, 4 });

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.BOOL, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof boolean[])
				|| ((boolean[])data).length != 24
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 24
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 2
				|| target.getArrayLengths()[1] != 3
				|| target.getArrayLengths()[2] != 4) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocも検査
		src.setArrayData(new boolean[ 5 * 6 * 7 ], 0, new int[] { 5, 6, 7 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.BOOL, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof boolean[])
				|| ((boolean[])data).length != 210
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 210
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 5
				|| target.getArrayLengths()[1] != 6
				|| target.getArrayLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		src.setArrayData(new boolean[ 5 * 6 * 7 ], 0, new int[] { 5, 6, 7 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.BOOL, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof boolean[])
				|| ((boolean[])data).length != 210
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 210
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 5
				|| target.getArrayLengths()[1] != 6
				|| target.getArrayLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}
	}

	// --------------------------------------------------
	// allocr, STRIG (String)
	// --------------------------------------------------

	@Test
	public void testAllocrStringScalar() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<String[]> src = new DataContainer<String[]>();
		src.setArrayData(new String[1], 0, new int[0]);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.STRING, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof String[])
				|| ((String[])data).length <= target.getArrayOffset()
				|| target.getArraySize() != 1
				|| target.getArrayLengths().length != 0) {

			fail("Incorrect allocated data");
		}
	}

	@Test
	public void testAllocrStringArray1D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<Object> target = new DataContainer<Object>();
		DataContainer<String[]> src = new DataContainer<String[]>();
		src.setArrayData(new String[3], 0, new int[] { 3 });

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.STRING, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof String[])
				|| ((String[])data).length != 3
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 3
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 3) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocrも検査
		src.setArrayData(new String[5], 0, new int[] { 5 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.STRING, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof String[])
				|| ((String[])data).length != 5
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 5
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		src.setArrayData(new String[5], 0, new int[] { 5 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.STRING, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof String[])
				|| ((String[])data).length != 5
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 5
				|| target.getArrayLengths().length != 1
				|| target.getArrayLengths()[0] != 5) {

			fail("Incorrect allocated data");
		}
	}

	@Test
	public void testAllocrStringArray3D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<String[]> src = new DataContainer<String[]>();
		src.setArrayData(new String[ 2 * 3 * 4 ], 0, new int[] { 2, 3, 4 });

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.STRING, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しくデータ領域が確保されているか検査
		Object data = target.getArrayData();
		if (!(data instanceof String[])
				|| ((String[])data).length != 24
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 24
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 2
				|| target.getArrayLengths()[1] != 3
				|| target.getArrayLengths()[2] != 4) {

			fail("Incorrect allocated data");
		}

		// 要素数を変えての再allocも検査
		src.setArrayData(new String[ 5 * 6 * 7 ], 0, new int[] { 5, 6, 7 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.STRING, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof String[])
				|| ((String[])data).length != 210
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 210
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 5
				|| target.getArrayLengths()[1] != 6
				|| target.getArrayLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}

		// 同じ要素数での再allocも検査
		src.setArrayData(new String[ 5 * 6 * 7 ], 0, new int[] { 5, 6, 7 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.STRING, target, src);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		data = target.getArrayData();
		if (!(data instanceof String[])
				|| ((String[])data).length != 210
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 210
				|| target.getArrayLengths().length != 3
				|| target.getArrayLengths()[0] != 5
				|| target.getArrayLengths()[1] != 6
				|| target.getArrayLengths()[2] != 7) {

			fail("Incorrect allocated data");
		}
	}



	// ==================================================
	// mov
	// ==================================================

	// --------------------------------------------------
	// mov, INT64 (long)
	// --------------------------------------------------

	@Test
	public void testMovInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setArrayData( new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new long[]{ 2L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.INT64, output, input);
		} catch (VnanoFatalException | VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 2L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new long[]{ -1L, -1L, -1L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new long[]{ 0L, 2L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().mov(DataType.INT64, output, input);
		} catch (VnanoFatalException | VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != 2L) {
			fail("Incorrect output value");
		}
		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getArrayData()[0] != -1L || output.getArrayData()[2] != -1L) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testMovInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setArrayData( new long[]{ -1L, -1L, -1L }, 0, new int[]{3} );
		input.setArrayData( new long[]{ 1L, 2L, 3L }, 0, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.INT64, output, input);
		} catch (VnanoFatalException | VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 1L || output.getArrayData()[1] != 2L || output.getArrayData()[2] != 3L) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// mov, FLOAT64 (double)
	// --------------------------------------------------

	@Test
	public void testMovFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setArrayData( new double[]{ -1.0 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new double[]{ 0.25 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.FLOAT64, output, input);
		} catch (VnanoFatalException | VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 0.25) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new double[]{ -1.0, -1.0, -1.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new double[]{ 0.0, 0.25, 0.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().mov(DataType.FLOAT64, output, input);
		} catch (VnanoFatalException | VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != 0.25) {
			fail("Incorrect output value");
		}
		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getArrayData()[0] != -1.0 || output.getArrayData()[2] != -1.0) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testMovFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setArrayData( new double[]{ -1.0, -1.0, -1.0 }, 0, new int[]{3} );
		input.setArrayData( new double[]{ 0.5, -0.25, 0.125 }, 0, new int[]{3} ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.FLOAT64, output, input);
		} catch (VnanoFatalException | VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=0.5 || output.getArrayData()[1]!=-0.25 || output.getArrayData()[2]!=0.125) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// mov, bool (boolean)
	// --------------------------------------------------

	@Test
	public void testMovBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.BOOL, output, input);
		} catch (VnanoFatalException | VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new boolean[]{ false, true, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().mov(DataType.BOOL, output, input);
		} catch (VnanoFatalException | VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[1] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (output.getArrayData()[0] != false || output.getArrayData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testMovBoolVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false, false, false }, 0, new int[]{3} );
		input.setArrayData( new boolean[]{ true, false, true }, 0, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.BOOL, output, input);
		} catch (VnanoFatalException | VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=true || output.getArrayData()[1]!=false || output.getArrayData()[2]!=true) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// mov, string (String)
	// --------------------------------------------------

	@Test
	public void testMovStringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setArrayData( new String[]{ "Init" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new String[]{ "Hello" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.STRING, output, input);
		} catch (VnanoFatalException | VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getArrayData()[0].equals("Hello")) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new String[]{ "Init0", "Init1", "Init2" }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new String[]{ "", "Hello", "" }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().mov(DataType.STRING, output, input);
		} catch (VnanoFatalException | VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (!output.getArrayData()[1].equals("Hello")) {
			fail("Incorrect output value");
		}

		// オフセット指定位置以外の値が書き換わっていないか検査
		if (!output.getArrayData()[0].equals("Init0") || !output.getArrayData()[2].equals("Init2")) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testMovStringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setArrayData( new String[]{ "Init0", "Init1", "Init2" }, 0, new int[]{3} );
		input.setArrayData( new String[]{ "Hello", "World", "!" }, 0, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.STRING, output, input);
		} catch (VnanoFatalException | VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getArrayData()[0].equals("Hello")
				|| !output.getArrayData()[1].equals("World")
				|| !output.getArrayData()[2].equals("!")) {

			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// mov, Unoperatable type (e.g. VOID)
	// --------------------------------------------------

	@Test
	public void testMovUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<Object> output = new DataContainer<Object>();
		DataContainer<Object> input = new DataContainer<Object>();
		output.setArrayData(this, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR); // thisは、とにかく演算できないデータを格納しておくため
		input.setArrayData(this, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().mov(DataType.VOID, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException | VnanoException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().mov(DataType.INT64, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException | VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().mov(DataType.FLOAT64, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException | VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().mov(DataType.BOOL, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException | VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().mov(DataType.STRING, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException | VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
	}





	// ==================================================
	// fill
	// ==================================================

	// --------------------------------------------------
	// fill, INT64 (long)
	// --------------------------------------------------

	@Test
	public void testFillInt64() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setArrayData( new long[]{ -1L, -1L, -1L }, 0, new int[]{3} );
		input.setArrayData( new long[]{ 2L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().fill(DataType.INT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=2L || output.getArrayData()[1]!=2L || output.getArrayData()[2]!=2L) {
			fail("Incorrect output value");
		}

		// inoutがオフセット設定された場合でも正常に動作するか検査
		output.setArrayData( new long[]{ -1L, -1L, -1L }, 0, new int[] {3} );
		input.setArrayData( new long[]{ 0L, 8L, 0L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().fill(DataType.INT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[0]!=8L || output.getArrayData()[1]!=8L || output.getArrayData()[2]!=8L) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// fill, FLOAT64 (double)
	// --------------------------------------------------

	@Test
	public void testFillFloat64() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setArrayData( new double[]{ -1.0, -1.0, -1.0 }, 0, new int[]{3} );
		input.setArrayData( new double[]{ 0.25 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().fill(DataType.FLOAT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=0.25 || output.getArrayData()[1]!=0.25 || output.getArrayData()[2]!=0.25) {
			fail("Incorrect output value");
		}

		// inoutがオフセット設定された場合でも正常に動作するか検査
		output.setArrayData( new double[]{ -1.0, -1.0, -1.0 }, 0, new int[] {3} );
		input.setArrayData( new double[]{ 0.0, 0.125, 0.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().fill(DataType.FLOAT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[0]!=0.125 || output.getArrayData()[1]!=0.125 || output.getArrayData()[2]!=0.125) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// fill, bool (boolean)
	// --------------------------------------------------

	@Test
	public void testFillBool() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false, false, false }, 0, new int[]{3} );
		input.setArrayData( new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().fill(DataType.BOOL, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=true || output.getArrayData()[1]!=true || output.getArrayData()[2]!=true) {
			fail("Incorrect output value");
		}

		// inoutがオフセット設定された場合でも正常に動作するか検査
		output.setArrayData( new boolean[]{ false, false, false }, 0, new int[]{3} );
		input.setArrayData( new boolean[]{ false, true, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().fill(DataType.BOOL, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getArrayData()[0]!=true || output.getArrayData()[1]!=true || output.getArrayData()[2]!=true) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// fill, string (String)
	// --------------------------------------------------

	@Test
	public void testFillString() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setArrayData( new String[]{ "Init0", "Init1", "Init2" }, 0, new int[]{3} );
		input.setArrayData( new String[]{ "Hello" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().fill(DataType.STRING, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getArrayData()[0].equals("Hello")
				|| !output.getArrayData()[1].equals("Hello")
				|| !output.getArrayData()[2].equals("Hello") ) {

			fail("Incorrect output value");
		}

		// inputがオフセット設定された場合でも正常に動作するか検査
		output.setArrayData( new String[]{ "Init0", "Init1", "Init2" }, 0, new int[]{3} );
		input.setArrayData( new String[]{ "", "Hello", "" }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().fill(DataType.STRING, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (!output.getArrayData()[0].equals("Hello")
				|| !output.getArrayData()[1].equals("Hello")
				|| !output.getArrayData()[2].equals("Hello") ) {
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// fill, Unoperatable type (e.g. VOID)
	// --------------------------------------------------

	@Test
	public void testFillUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<?> output = new DataContainer<Object>();
		DataContainer<?> input = new DataContainer<Object>();

		// 演算を実行
		try {
			new ExecutionUnit().fill(DataType.VOID, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}



	// ==================================================
	// refelm
	// ==================================================

	// --------------------------------------------------
	// refelm, INT64 (long)
	// --------------------------------------------------

	@Test
	public void testRefelmInt64() throws VnanoException {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> dest = new DataContainer<long[]>();  // 要素を格納するコンテナ
		DataContainer<long[]> src = new DataContainer<long[]>();   // 参照する配列のコンテナ

		DataContainer<long[]> index0 = new DataContainer<long[]>();
		DataContainer<long[]> index1 = new DataContainer<long[]>();
		DataContainer<long[]> index2 = new DataContainer<long[]>();

		int[] srcLengths = new int[]{2, 3, 4}; // 参照する配列の要素数は [2][3][4]

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
		src.setArrayData(arrayData, 0, srcLengths);

		// [1][0][2] の要素(=14)を参照する
		index0.setArrayData(new long[] {1}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index1.setArrayData(new long[] {0}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index2.setArrayData(new long[] {2}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		DataContainer<?>[] operands = new DataContainer<?>[] {
			dest, src, index0, index1, index2
		};
		try {
			new ExecutionUnit().refelm(DataType.INT64, dest, src, operands, 2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (dest.getArrayData()[ dest.getArrayOffset() ] != 14L) {
			fail("Incorrect output value");
		}

		// [0][2][3] の要素(=11)を参照する
		index0.setArrayData(new long[] {0}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index1.setArrayData(new long[] {2}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index2.setArrayData(new long[] {3}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().refelm(DataType.INT64, dest, src, operands, 2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (dest.getArrayData()[ dest.getArrayOffset() ] != 11L) {
			fail("Incorrect output value");
		}

		// 参照中の [0][2][3] の値を書き換えて、元の配列データの要素値が変わる（参照が切れてない）事を検査
		dest.getArrayData()[ dest.getArrayOffset() ] = 123L;
		if (arrayData[11] != 123L) {  // [0][2][3]は元の配列データでは[11]番目
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// refelm, FLOAT64 (double)
	// --------------------------------------------------

	@Test
	public void testRefelmFloat64() throws VnanoException {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> dest = new DataContainer<double[]>();  // 要素を格納するコンテナ
		DataContainer<double[]> src = new DataContainer<double[]>();   // 参照する配列のコンテナ

		DataContainer<long[]> index0 = new DataContainer<long[]>();
		DataContainer<long[]> index1 = new DataContainer<long[]>();
		DataContainer<long[]> index2 = new DataContainer<long[]>();

		int[] srcLengths = new int[]{2, 3, 4}; // 参照する配列の要素数は [2][3][4]

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
		src.setArrayData(arrayData, 0, srcLengths);

		// [1][0][2] の要素(=14)を参照する
		index0.setArrayData(new long[] {1}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index1.setArrayData(new long[] {0}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index2.setArrayData(new long[] {2}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		DataContainer<?>[] operands = new DataContainer<?>[] {
			dest, src, index0, index1, index2
		};
		try {
			new ExecutionUnit().refelm(DataType.FLOAT64, dest, src, operands, 2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (dest.getArrayData()[ dest.getArrayOffset() ] != 14.0) {
			fail("Incorrect output value");
		}

		// [0][2][3] の要素(=11)を参照する
		index0.setArrayData(new long[] {0}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index1.setArrayData(new long[] {2}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index2.setArrayData(new long[] {3}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().refelm(DataType.FLOAT64, dest, src, operands, 2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (dest.getArrayData()[ dest.getArrayOffset() ] != 11.0) {
			fail("Incorrect output value");
		}

		// 参照中の [0][2][3] の値を書き換えて、元の配列データの要素値が変わる（参照が切れてない）事を検査
		dest.getArrayData()[ dest.getArrayOffset() ] = 123.0;
		if (arrayData[11] != 123.0) {  // [0][2][3]は元の配列データでは[11]番目
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// refelm, bool (boolean)
	// --------------------------------------------------

	@Test
	public void testRefelmBool() throws VnanoException {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> dest = new DataContainer<boolean[]>();  // 要素を格納するコンテナ
		DataContainer<boolean[]> src = new DataContainer<boolean[]>();   // 参照する配列のコンテナ

		DataContainer<long[]> index0 = new DataContainer<long[]>();
		DataContainer<long[]> index1 = new DataContainer<long[]>();
		DataContainer<long[]> index2 = new DataContainer<long[]>();

		int[] srcLengths = new int[]{2, 3, 4}; // 参照する配列の要素数は [2][3][4]

		// テスト用の配列データを src に設定
		boolean[] arrayData = new boolean[]{
				false, false, false, false,
				false, false, false, false,
				false, false, false, false,

				false, false, true,  false,
				false, false, false, false,
				false, false, false, false,
		};
		src.setArrayData(arrayData, 0, srcLengths);

		// [1][0][2] の要素(=true)を参照する
		index0.setArrayData(new long[] {1}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index1.setArrayData(new long[] {0}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index2.setArrayData(new long[] {2}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		DataContainer<?>[] operands = new DataContainer<?>[] {
			dest, src, index0, index1, index2
		};
		try {
			new ExecutionUnit().refelm(DataType.BOOL, dest, src, operands, 2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (dest.getArrayData()[ dest.getArrayOffset() ] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// [0][2][3] の要素(=false)を参照する
		index0.setArrayData(new long[] {0}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index1.setArrayData(new long[] {2}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index2.setArrayData(new long[] {3}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().refelm(DataType.BOOL, dest, src, operands, 2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (dest.getArrayData()[ dest.getArrayOffset() ] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// 参照中の [0][2][3] の値を書き換えて、元の配列データの要素値が変わる（参照が切れてない）事を検査
		dest.getArrayData()[ dest.getArrayOffset() ] = true;
		if (arrayData[11] != true) {  // [0][2][3]は元の配列データでは[11]番目
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// refelm, string (String)
	// --------------------------------------------------

	@Test
	public void testRefelmString() throws VnanoException {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> dest = new DataContainer<String[]>();  // 要素を格納するコンテナ
		DataContainer<String[]> src = new DataContainer<String[]>();   // 参照する配列のコンテナ

		DataContainer<long[]> index0 = new DataContainer<long[]>();
		DataContainer<long[]> index1 = new DataContainer<long[]>();
		DataContainer<long[]> index2 = new DataContainer<long[]>();

		int[] srcLengths = new int[]{2, 3, 4}; // 参照する配列の要素数は [2][3][4]

		// テスト用の配列データを src に設定
		String[] arrayData = new String[]{
				"000", "001", "002", "003",
				"010", "011", "012", "013",
				"020", "021", "022", "023",

				"100", "101", "102", "103",
				"110", "111", "112", "113",
				"120", "121", "122", "123",
		};
		src.setArrayData(arrayData, 0, srcLengths);

		// [1][0][2] の要素(=true)を参照する
		index0.setArrayData(new long[] {1}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index1.setArrayData(new long[] {0}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index2.setArrayData(new long[] {2}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		DataContainer<?>[] operands = new DataContainer<?>[] {
			dest, src, index0, index1, index2
		};
		try {
			new ExecutionUnit().refelm(DataType.STRING, dest, src, operands, 2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (!dest.getArrayData()[ dest.getArrayOffset() ].equals("102")) {
			fail("Incorrect output value");
		}

		// [0][2][3] の要素(=false)を参照する
		index0.setArrayData(new long[] {0}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index1.setArrayData(new long[] {2}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index2.setArrayData(new long[] {3}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().refelm(DataType.STRING, dest, src, operands, 2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (!dest.getArrayData()[ dest.getArrayOffset() ].equals("023")) {
			fail("Incorrect output value");
		}

		// 参照中の [0][1][3] の値を書き換えて、元の配列データの要素値が変わる（参照が切れてない）事を検査
		dest.getArrayData()[ dest.getArrayOffset() ] = "888";
		if (!arrayData[11].equals("888")) { // [0][2][3]は元の配列データでは[11]番目
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// refelm, Unoperatable type (e.g. VOID)
	// --------------------------------------------------

	@Test
	public void testRefelmUnoperatableData() throws VnanoException {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<?> dest = new DataContainer<Object>();  // 要素を格納するコンテナ
		DataContainer<ExecutionUnitTest[]> src = new DataContainer<ExecutionUnitTest[]>();   // 参照する配列のコンテナ(演算不能な型の例として便宜的にExecutionUnitTest[]を使用)

		DataContainer<long[]> index0 = new DataContainer<long[]>();
		DataContainer<long[]> index1 = new DataContainer<long[]>();
		DataContainer<long[]> index2 = new DataContainer<long[]>();

		src.setArrayData((ExecutionUnitTest[])null, 0, new int[] { 1, 2, 3 });

		// [1][0][2] の要素(=true)を参照する
		index0.setArrayData(new long[] {1}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index1.setArrayData(new long[] {0}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index2.setArrayData(new long[] {2}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		DataContainer<?>[] operands = new DataContainer<?>[] {
			dest, src, index0, index1, index2
		};

		// 演算を実行
		try {
			new ExecutionUnit().refelm(DataType.VOID, dest, src, operands, 2);
			fail("Expected exception did not occured");
		} catch (VnanoException e) {
			// 以下の例外が発生するのが正しい挙動
			if(e.getErrorType() != ErrorType.INVALID_ARRAY_INDEX) {
				fail("Expected exception did not occured");
			}
		}
	}



	// ==================================================
	// movelm
	// ==================================================

	// --------------------------------------------------
	// movelm, INT64 (long)
	// --------------------------------------------------

	@Test
	public void testMovelmInt64() throws VnanoException {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> dest = new DataContainer<long[]>();  // 要素を格納するコンテナ
		DataContainer<long[]> src = new DataContainer<long[]>();   // 参照する配列のコンテナ

		DataContainer<long[]> index0 = new DataContainer<long[]>();
		DataContainer<long[]> index1 = new DataContainer<long[]>();
		DataContainer<long[]> index2 = new DataContainer<long[]>();

		int[] srcLengths = new int[]{2, 3, 4}; // 参照する配列の要素数は [2][3][4]

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
		src.setArrayData(arrayData, 0, srcLengths);
		dest.setArrayData(new long[] { 0L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);

		// [1][0][2] の要素(=14)を参照する
		index0.setArrayData(new long[] {1}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index1.setArrayData(new long[] {0}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index2.setArrayData(new long[] {2}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		DataContainer<?>[] operands = new DataContainer<?>[] {
			dest, src, index0, index1, index2
		};
		try {
			new ExecutionUnit().movelm(DataType.INT64, dest, src, operands, 2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (dest.getArrayData()[ dest.getArrayOffset() ] != 14L) {
			fail("Incorrect output value");
		}

		// [0][2][3] の要素(=11)を参照する
		index0.setArrayData(new long[] {0}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index1.setArrayData(new long[] {2}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index2.setArrayData(new long[] {3}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().movelm(DataType.INT64, dest, src, operands, 2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (dest.getArrayData()[ dest.getArrayOffset() ] != 11L) {
			fail("Incorrect output value");
		}

		// 参照中の [0][2][3] の値を書き換えて、元の配列データの要素値が変わらない（参照ではなくコピーである）事を検査
		dest.getArrayData()[ dest.getArrayOffset() ] = 123L;
		if (arrayData[11] != 11L) {  // [0][2][3]は元の配列データでは[11]番目
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// refelm, FLOAT64 (double)
	// --------------------------------------------------

	@Test
	public void testMovelmFloat64() throws VnanoException {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> dest = new DataContainer<double[]>();  // 要素を格納するコンテナ
		DataContainer<double[]> src = new DataContainer<double[]>();   // 参照する配列のコンテナ

		DataContainer<long[]> index0 = new DataContainer<long[]>();
		DataContainer<long[]> index1 = new DataContainer<long[]>();
		DataContainer<long[]> index2 = new DataContainer<long[]>();

		int[] srcLengths = new int[]{2, 3, 4}; // 参照する配列の要素数は [2][3][4]

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
		src.setArrayData(arrayData, 0, srcLengths);
		dest.setArrayData(new double[] { 0.0 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);

		// [1][0][2] の要素(=14)を参照する
		index0.setArrayData(new long[] {1}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index1.setArrayData(new long[] {0}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index2.setArrayData(new long[] {2}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		DataContainer<?>[] operands = new DataContainer<?>[] {
			dest, src, index0, index1, index2
		};
		try {
			new ExecutionUnit().movelm(DataType.FLOAT64, dest, src, operands, 2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (dest.getArrayData()[ dest.getArrayOffset() ] != 14.0) {
			fail("Incorrect output value");
		}

		// [0][2][3] の要素(=11)を参照する
		index0.setArrayData(new long[] {0}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index1.setArrayData(new long[] {2}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index2.setArrayData(new long[] {3}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().movelm(DataType.FLOAT64, dest, src, operands, 2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (dest.getArrayData()[ dest.getArrayOffset() ] != 11.0) {
			fail("Incorrect output value");
		}

		// 参照中の [0][2][3] の値を書き換えて、元の配列データの要素値が変わらない（参照ではなくコピーである）事を検査
		dest.getArrayData()[ dest.getArrayOffset() ] = 123.0;
		if (arrayData[11] != 11.0) {  // [0][2][3]は元の配列データでは[11]番目
			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// refelm, bool (boolean)
	// --------------------------------------------------

	@Test
	public void testMovelmBool() throws VnanoException {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> dest = new DataContainer<boolean[]>();  // 要素を格納するコンテナ
		DataContainer<boolean[]> src = new DataContainer<boolean[]>();   // 参照する配列のコンテナ

		DataContainer<long[]> index0 = new DataContainer<long[]>();
		DataContainer<long[]> index1 = new DataContainer<long[]>();
		DataContainer<long[]> index2 = new DataContainer<long[]>();

		int[] srcLengths = new int[]{2, 3, 4}; // 参照する配列の要素数は [2][3][4]

		// テスト用の配列データを src に設定
		boolean[] arrayData = new boolean[]{
				false, false, false, false,
				false, false, false, false,
				false, false, false, false,

				false, false, true,  false,
				false, false, false, false,
				false, false, false, false,
		};
		src.setArrayData(arrayData, 0, srcLengths);
		dest.setArrayData(new boolean[] { false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);

		// [1][0][2] の要素(=true)を参照する
		index0.setArrayData(new long[] {1}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index1.setArrayData(new long[] {0}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index2.setArrayData(new long[] {2}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		DataContainer<?>[] operands = new DataContainer<?>[] {
			dest, src, index0, index1, index2
		};
		try {
			new ExecutionUnit().movelm(DataType.BOOL, dest, src, operands, 2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (dest.getArrayData()[ dest.getArrayOffset() ] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// [0][2][3] の要素(=false)を参照する
		index0.setArrayData(new long[] {0}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index1.setArrayData(new long[] {2}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index2.setArrayData(new long[] {3}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().movelm(DataType.BOOL, dest, src, operands, 2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (dest.getArrayData()[ dest.getArrayOffset() ] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// 参照中の [0][2][3] の値を書き換えて、元の配列データの要素値が変わらない（参照ではなくコピーである）事を検査
		dest.getArrayData()[ dest.getArrayOffset() ] = true;
		if (arrayData[11] != false) {  // [0][2][3]は元の配列データでは[11]番目
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// refelm, string (String)
	// --------------------------------------------------

	@Test
	public void testMovelmString() throws VnanoException {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> dest = new DataContainer<String[]>();  // 要素を格納するコンテナ
		DataContainer<String[]> src = new DataContainer<String[]>();   // 参照する配列のコンテナ

		DataContainer<long[]> index0 = new DataContainer<long[]>();
		DataContainer<long[]> index1 = new DataContainer<long[]>();
		DataContainer<long[]> index2 = new DataContainer<long[]>();

		int[] srcLengths = new int[]{2, 3, 4}; // 参照する配列の要素数は [2][3][4]

		// テスト用の配列データを src に設定
		String[] arrayData = new String[]{
				"000", "001", "002", "003",
				"010", "011", "012", "013",
				"020", "021", "022", "023",

				"100", "101", "102", "103",
				"110", "111", "112", "113",
				"120", "121", "122", "123",
		};
		src.setArrayData(arrayData, 0, srcLengths);
		dest.setArrayData(new String[] { "" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);

		// [1][0][2] の要素(=true)を参照する
		index0.setArrayData(new long[] {1}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index1.setArrayData(new long[] {0}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index2.setArrayData(new long[] {2}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		DataContainer<?>[] operands = new DataContainer<?>[] {
			dest, src, index0, index1, index2
		};
		try {
			new ExecutionUnit().movelm(DataType.STRING, dest, src, operands, 2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (!dest.getArrayData()[ dest.getArrayOffset() ].equals("102")) {
			fail("Incorrect output value");
		}

		// [0][2][3] の要素(=false)を参照する
		index0.setArrayData(new long[] {0}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index1.setArrayData(new long[] {2}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index2.setArrayData(new long[] {3}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		try {
			new ExecutionUnit().movelm(DataType.STRING, dest, src, operands, 2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (!dest.getArrayData()[ dest.getArrayOffset() ].equals("023")) {
			fail("Incorrect output value");
		}

		// 参照中の [0][1][3] の値を書き換えて、元の配列データの要素値が変わらない（参照ではなくコピーである）事を検査
		dest.getArrayData()[ dest.getArrayOffset() ] = "888";
		if (!arrayData[11].equals("023")) { // [0][2][3]は元の配列データでは[11]番目
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// movelm, Unoperatable type (e.g. VOID)
	// --------------------------------------------------

	@Test
	public void testMovelmUnoperatableData() throws VnanoException {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<?> dest = new DataContainer<Object>();  // 要素を格納するコンテナ
		DataContainer<ExecutionUnitTest[]> src = new DataContainer<ExecutionUnitTest[]>();   // 参照する配列のコンテナ(演算不能な型の例として便宜的にExecutionUnitTest[]を使用)

		DataContainer<long[]> index0 = new DataContainer<long[]>();
		DataContainer<long[]> index1 = new DataContainer<long[]>();
		DataContainer<long[]> index2 = new DataContainer<long[]>();

		src.setArrayData((ExecutionUnitTest[])null, 0, new int[] { 1, 2, 3 });

		// [1][0][2] の要素(=true)を参照する
		index0.setArrayData(new long[] {1}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index1.setArrayData(new long[] {0}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		index2.setArrayData(new long[] {2}, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		DataContainer<?>[] operands = new DataContainer<?>[] {
			dest, src, index0, index1, index2
		};

		// 演算を実行
		try {
			new ExecutionUnit().movelm(DataType.VOID, dest, src, operands, 2);
			fail("Expected exception did not occured");
		} catch (VnanoException e) {
			// 以下の例外が発生するのが正しい挙動
			if(e.getErrorType() != ErrorType.INVALID_ARRAY_INDEX) {
				fail("Expected exception did not occured");
			}
		}
	}



	// ==================================================
	// cast
	// ==================================================

	// --------------------------------------------------
	// cast, INT64 to INT64 (long to long)
	// --------------------------------------------------

	@Test
	public void testCastInt64Int64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setArrayData( new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new long[]{ 2L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.INT64, DataType.INT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 2L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new long[]{ -1L, -1L, -1L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new long[]{ 0L, 0L, 2L, 0L, 0L }, 2, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().cast(DataType.INT64, DataType.INT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getArrayData()[ output.getArrayOffset() ] != 2L) {
			fail("Incorrect output value");
		}
		if (output.getArrayData()[0] != -1L || output.getArrayData()[2] != -1L) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testCastInt64Int64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setArrayData( new long[]{ -1L, -1L, -1L }, 0, new int[]{ 3 } );
		input.setArrayData( new long[]{ 1L, 2L, 3L }, 0, new int[]{ 3 } );

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.INT64, DataType.INT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 1L || output.getArrayData()[1] != 2L || output.getArrayData()[2] != 3L) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, INT64 to FLOAT64 (long to double)
	// --------------------------------------------------

	@Test
	public void testCastInt64Float64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setArrayData( new double[]{ -1.0 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new long[]{ 2L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.INT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 2.0) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new double[]{ -1.0, -1.0, -1.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new long[]{ 0L, 0L, 2L, 0L, 0L }, 2, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.INT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getArrayData()[ output.getArrayOffset() ] != 2.0) {
			fail("Incorrect output value");
		}
		if (output.getArrayData()[0] != -1.0 || output.getArrayData()[2] != -1.0) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testCastInt64Float64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setArrayData( new double[]{ -1.0, -1.0, -1.0 }, 0, new int[]{ 3 } );
		input.setArrayData( new long[]{ 1L, 2L, 3L }, 0, new int[]{ 3 } );

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.INT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 1.0 || output.getArrayData()[1] != 2.0 || output.getArrayData()[2] != 3.0) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, INT64 to string (long to String)
	// --------------------------------------------------

	@Test
	public void testCastInt64StringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setArrayData( new String[]{ "Init" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new long[]{ 2L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.INT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getArrayData()[0].equals("2")) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new String[]{ "Init0", "Init1", "Init2" }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new long[]{ 0L, 0L, 2L, 0L, 0L }, 2, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.INT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (!output.getArrayData()[ output.getArrayOffset() ].equals("2")) {
			fail("Incorrect output value");
		}
		if (!output.getArrayData()[0].equals("Init0") || !output.getArrayData()[2].equals("Init2")) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testCastInt64StringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setArrayData( new String[]{ "Init0", "Init1", "Init2" }, 0, new int[]{ 3 } );
		input.setArrayData( new long[]{ 1L, 2L, 3L }, 0, new int[]{ 3 } );

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.INT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getArrayData()[0].equals("1")
				|| !output.getArrayData()[1].equals("2")
				|| !output.getArrayData()[2].equals("3") ) {

			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// cast, FLOAT64 to FLOAT64 (double to double)
	// --------------------------------------------------

	@Test
	public void testCastFloat64Float64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setArrayData( new double[]{ -1.0 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new double[]{ 2.25 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.FLOAT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 2.25) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new double[]{ -1.0, -1.0, -1.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new double[]{ 0.0, 0.0, 2.25, 0.0, 0.0 }, 2, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.FLOAT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getArrayData()[ output.getArrayOffset() ] != 2.25) {
			fail("Incorrect output value");
		}
		if (output.getArrayData()[0] != -1.0 || output.getArrayData()[2] != -1.0) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testCastFloat64Float64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setArrayData( new double[]{ -1.0, -1.0, -1.0 }, 0, new int[]{ 3 } );
		input.setArrayData( new double[]{ 1.125, 2.25, 3.5 }, 0, new int[]{ 3 } ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.FLOAT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=1.125 || output.getArrayData()[1]!=2.25 || output.getArrayData()[2]!=3.5) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, FLOAT64 to INT64 (double to long)
	// --------------------------------------------------

	@Test
	public void testCastFloat64Int64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setArrayData( new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new double[]{ 2.25 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.INT64, DataType.FLOAT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 2L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new long[]{ -1L, -1L, -1L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new double[]{ 0.0, 0.0, 2.25, 0.0, 0.0 }, 2, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().cast(DataType.INT64, DataType.FLOAT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getArrayData()[ output.getArrayOffset() ] != 2L) {
			fail("Incorrect output value");
		}
		if (output.getArrayData()[0] != -1L || output.getArrayData()[2] != -1L) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testCastFloat64Int64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setArrayData( new long[]{ -1L, -1L, -1L }, 0, new int[]{ 3 } );
		input.setArrayData( new double[]{ 1.125, 2.25, 3.5 }, 0, new int[]{ 3 } ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.INT64, DataType.FLOAT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 1L || output.getArrayData()[1] != 2L || output.getArrayData()[2] != 3L) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, FLOAT64 to string (double to String)
	// --------------------------------------------------

	@Test
	public void testCastFloat64StringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setArrayData( new String[]{ "Init" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new double[]{ 2.25 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.FLOAT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getArrayData()[0].equals("2.25")) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new String[]{ "Init0", "Init1", "Init2" }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new double[]{ 0.0, 0.0, 2.25, 0.0, 0.0 }, 2, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.FLOAT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (!output.getArrayData()[ output.getArrayOffset() ].equals("2.25")) {
			fail("Incorrect output value");
		}
		if (!output.getArrayData()[0].equals("Init0") || !output.getArrayData()[2].equals("Init2")) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testCastFloat64StringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setArrayData( new String[]{ "Init0", "Init1", "Init2" }, 0, new int[]{ 3 } );
		input.setArrayData( new double[]{ 1.125, 2.25, 3.5 }, 0, new int[]{ 3 } ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.FLOAT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getArrayData()[0].equals("1.125")
				|| !output.getArrayData()[1].equals("2.25")
				|| !output.getArrayData()[2].equals("3.5") ) {

			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, bool to bool (boolean to String)
	// --------------------------------------------------

	@Test
	public void testCastBoolBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.BOOL, DataType.BOOL, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new boolean[]{ false, false, true, false, false }, 2, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().cast(DataType.BOOL, DataType.BOOL, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getArrayData()[ output.getArrayOffset() ] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}
		if (output.getArrayData()[0] != false || output.getArrayData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testCastBoolBoolVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setArrayData( new boolean[]{ false, false, false }, 0, new int[]{ 3 } );
		input.setArrayData( new boolean[]{ true, false, true }, 0, new int[]{ 3 } );

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.BOOL, DataType.BOOL, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=true || output.getArrayData()[1]!=false || output.getArrayData()[2]!=true ) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, bool to string (boolean to String)
	// --------------------------------------------------

	@Test
	public void testCastBoolStringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setArrayData( new String[]{ "Init" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.BOOL, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getArrayData()[0].equals("true")) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new String[]{ "Init0", "Init1", "Init2" }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new boolean[]{ false, false, true, false, false }, 2, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.BOOL, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (!output.getArrayData()[ output.getArrayOffset() ].equals("true")) {
			fail("Incorrect output value");
		}
		if (!output.getArrayData()[0].equals("Init0") || !output.getArrayData()[2].equals("Init2")) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testCastBoolStringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setArrayData( new String[]{ "Init0", "Init1", "Init2" }, 0, new int[]{ 3 } );
		input.setArrayData( new boolean[]{ true, false, true }, 0, new int[]{ 3 } );

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.BOOL, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getArrayData()[0].equals("true")
				|| !output.getArrayData()[1].equals("false")
				|| !output.getArrayData()[2].equals("true") ) {

			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// cast, string to string (String to String)
	// --------------------------------------------------

	@Test
	public void testCastStringStringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setArrayData( new String[]{ "Init" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new String[]{ "Hello" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.STRING, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getArrayData()[0].equals("Hello")) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new String[]{ "Init0", "Init1", "Init2" }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new String[]{ "", "", "Hello", "", "" }, 2, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.STRING, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (!output.getArrayData()[ output.getArrayOffset() ].equals("Hello")) {
			fail("Incorrect output value");
		}
		if (!output.getArrayData()[0].equals("Init0") || !output.getArrayData()[2].equals("Init2")) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testCastStringStringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setArrayData( new String[]{ "Init0", "Init1", "Init2" }, 0, new int[]{ 3 } );
		input.setArrayData( new String[]{ "Hello", "World", "!" }, 0, new int[]{ 3 } );

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.STRING, DataType.STRING, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getArrayData()[0].equals("Hello")
				|| !output.getArrayData()[1].equals("World")
				|| !output.getArrayData()[2].equals("!") ) {

			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, string to INT64 (String to long)
	// --------------------------------------------------

	@Test
	public void testCastStringInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setArrayData( new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new String[]{ "2" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.INT64, DataType.STRING, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 2L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new long[]{ -1L, -1L, -1L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new String[]{ "0", "0", "2", "0", "0" }, 2, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().cast(DataType.INT64, DataType.STRING, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getArrayData()[ output.getArrayOffset() ] != 2L) {
			fail("Incorrect output value");
		}
		if (output.getArrayData()[0] != -1L || output.getArrayData()[2] != -1L) {
			fail("Incorrect output value");
		}

		// 浮動小数点として解釈できる文字列から整数への変換も検査(内部処理が異なる)
		output.setArrayData( new long[]{ -1L, -1L, -1L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new String[]{ "0", "0", "8.25", "0", "0" }, 2, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().cast(DataType.INT64, DataType.STRING, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}
		if (output.getArrayData()[ output.getArrayOffset() ] != 8L) {
			fail("Incorrect output value");
		}
		if (output.getArrayData()[0] != -1L || output.getArrayData()[2] != -1L) {
			fail("Incorrect output value");
		}

		// 整数に変換できない文字列からの変換も検査
		output.setArrayData( new long[]{ -1L, -1L, -1L }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new String[]{ "0", "0", "Hello", "0", "0" }, 2, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().cast(DataType.INT64, DataType.STRING, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
	}

	@Test
	public void testCastStringInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setArrayData( new long[]{ -1L, -1L, -1L }, 0, new int[]{ 3 } );
		input.setArrayData( new String[]{ "1", "2", "3" }, 0, new int[]{ 3 } );

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.INT64, DataType.STRING, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 1L || output.getArrayData()[1] != 2L || output.getArrayData()[2] != 3L) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, string to FLOAT64 (String to double)
	// --------------------------------------------------

	@Test
	public void testCastStringFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setArrayData( new double[]{ -1.0 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new String[]{ "2.25" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.STRING, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != 2.25) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new double[]{ -1.0, -1.0, -1.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new String[]{ "0.0", "0.0", "2.25", "0.0", "0.0" }, 2, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.STRING, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getArrayData()[ output.getArrayOffset() ] != 2.25) {
			fail("Incorrect output value");
		}
		if (output.getArrayData()[0] != -1.0 || output.getArrayData()[2] != -1.0) {
			fail("Incorrect output value");
		}

		// 浮動小数点数に変換できない文字列からの変換も検査
		output.setArrayData( new double[]{ -1.0, -1.0, -1.0 }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new String[]{ "0.0", "0.0", "Hello", "0.0", "0.0" }, 2, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.STRING, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
	}

	@Test
	public void testCastStringFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setArrayData( new double[]{ -1.0, -1.0, -1.0 }, 0, new int[]{ 3 } );
		input.setArrayData( new String[]{ "1.125", "2.25", "3.5" }, 0, new int[]{ 3 } ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.STRING, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=1.125 || output.getArrayData()[1]!=2.25 || output.getArrayData()[2]!=3.5) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, string to bool (String to boolean)
	// --------------------------------------------------

	@Test
	public void testCastStringBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setArrayData( new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new String[]{ "true" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.BOOL, DataType.STRING, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new String[]{ "false", "false", "true", "false", "false" }, 2, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		try {
			new ExecutionUnit().cast(DataType.BOOL, DataType.STRING, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// オフセット指定位置の値が正しく書き換わり、他の値が書き換わっていないか検査
		if (output.getArrayData()[ output.getArrayOffset() ] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}
		if (output.getArrayData()[0] != false || output.getArrayData()[2] != false) {
			fail("Incorrect output value");
		}

		// 論理値に変換できない文字列からの変換も検査
		output.setArrayData( new boolean[]{ false, false, false }, 1, DataContainer.ARRAY_LENGTHS_OF_SCALAR );
		input.setArrayData( new String[]{ "0.0", "0.0", "Hello", "0.0", "0.0" }, 2, DataContainer.ARRAY_LENGTHS_OF_SCALAR ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().cast(DataType.BOOL, DataType.STRING, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
	}

	@Test
	public void testCastStringBoolVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setArrayData( new boolean[]{ false, false, false }, 0, new int[]{ 3 } );
		input.setArrayData( new String[]{ "true", "false", "true" }, 0, new int[]{ 3 } ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().cast(DataType.BOOL, DataType.STRING, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getArrayData()[0]!=true || output.getArrayData()[1]!=false || output.getArrayData()[2]!=true) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, Unoperatable type (e.g. VOID)
	// --------------------------------------------------

	@SuppressWarnings("unchecked")
	@Test
	public void testCastUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<?> output = new DataContainer<Object>();
		DataContainer<?> input = new DataContainer<Object>();

		// 演算を実行
		try {
			((DataContainer<long[]>)output).setArrayData(new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
			((DataContainer<boolean[]>)input).setArrayData(new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
			new ExecutionUnit().cast(DataType.INT64, DataType.BOOL, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<long[]>)output).setArrayData(new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
			((DataContainer<Object[]>)input).setArrayData(new Object[]{ "" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
			new ExecutionUnit().cast(DataType.INT64, DataType.VOID, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<double[]>)output).setArrayData(new double[]{ -1.0 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
			((DataContainer<boolean[]>)input).setArrayData(new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.BOOL, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<double[]>)output).setArrayData(new double[]{ -1.0 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
			((DataContainer<Object[]>)input).setArrayData(new Object[]{ "" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.VOID, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<boolean[]>)output).setArrayData(new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
			((DataContainer<long[]>)input).setArrayData(new long[]{ 123L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
			new ExecutionUnit().cast(DataType.BOOL, DataType.INT64, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<boolean[]>)output).setArrayData(new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
			((DataContainer<double[]>)input).setArrayData(new double[]{ 2.25 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
			new ExecutionUnit().cast(DataType.BOOL, DataType.FLOAT64, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<boolean[]>)output).setArrayData(new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
			((DataContainer<Object[]>)input).setArrayData(new Object[]{ "" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
			new ExecutionUnit().cast(DataType.BOOL, DataType.VOID, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<String[]>)output).setArrayData(new String[]{ "Init" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
			((DataContainer<Object[]>)input).setArrayData(new Object[]{ "" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
			new ExecutionUnit().cast(DataType.STRING, DataType.VOID, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<Object[]>)output).setArrayData(new Object[]{ "" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
			((DataContainer<Object[]>)input).setArrayData(new Object[]{ "" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
			new ExecutionUnit().cast(DataType.VOID, DataType.VOID, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
	}


}
