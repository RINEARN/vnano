/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
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
		output.setData( new long[]{ -1L }, 0 );
		inputA.setData( new long[]{ 1L }, 0 );
		inputB.setData( new long[]{ 2L }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().add(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 3L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new long[]{ -1L, -1L, -1L }, 1 );
		inputA.setData( new long[]{ 0L, 1L, 0L }, 1 );
		inputB.setData( new long[]{ 0L, 2L, 0L }, 1 );
		try {
			new ExecutionUnit().add(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testAddInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new long[]{ -1L, -1L, -1L }, new int[]{3});
		inputA.setData( new long[]{ 0L, 1L, 2L }, new int[]{3});
		inputB.setData( new long[]{ 3L, 4L, 5L }, new int[]{3});

		// 演算を実行
		try {
			new ExecutionUnit().add(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testAddFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0 }, 0 );
		inputA.setData( new double[]{ 0.25 }, 0 );  // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.125 }, 0 ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().add(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 0.375) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 }, 1 );
		inputA.setData( new double[]{ 0.0, 0.25,  0.0 }, 1 ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.0, 0.125, 0.0 }, 1 ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().add(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testAddFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0, -1.0, -1.0 }, new int[]{3} );
		inputA.setData( new double[]{ 1.0, 0.5, 0.25 }, new int[]{3} );         // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.125, 0.0625, 0.03125 }, new int[]{3} ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().add(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testAddStringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<String[]> inputA = new DataContainer<String[]>();
		DataContainer<String[]> inputB = new DataContainer<String[]>();
		output.setData( new String[]{ "Init" }, 0 );
		inputA.setData( new String[]{ "Hello" }, 0 );
		inputB.setData( new String[]{ "World" }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().add(DataType.STRING, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getData()[0].equals("HelloWorld")) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new String[]{ "Init0", "Init1", "Init2" }, 1 );
		inputA.setData( new String[]{ "", "Hello", "" }, 1 );
		inputB.setData( new String[]{ "", "World", "" }, 1 );
		try {
			new ExecutionUnit().add(DataType.STRING, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testAddStringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<String[]> inputA = new DataContainer<String[]>();
		DataContainer<String[]> inputB = new DataContainer<String[]>();
		output.setData( new String[]{ "Init0", "Init1", "Init2" }, new int[]{3} );
		inputA.setData( new String[]{ "Good", "Hello", "Thank" }, new int[]{3} );
		inputB.setData( new String[]{ "Morning", "World", "You" }, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().add(DataType.STRING, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testAddUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new boolean[]{ true }, 0 );
		inputB.setData( new boolean[]{ false }, 0 );

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
		output.setData( new long[]{ -1L }, 0 );
		inputA.setData( new long[]{ 5L }, 0 );
		inputB.setData( new long[]{ 3L }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().sub(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 2L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new long[]{ -1L, -1L, -1L }, 1 );
		inputA.setData( new long[]{ 0L, 5L, 0L }, 1 );
		inputB.setData( new long[]{ 0L, 3L, 0L }, 1 );
		try {
			new ExecutionUnit().sub(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testSubInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new long[]{ -1L, -1L, -1L }, new int[]{3} );
		inputA.setData( new long[]{ 5L, 1L, 7L }, new int[]{3} );
		inputB.setData( new long[]{ 3L, 8L, 7L }, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().sub(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testSubFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0 }, 0 );
		inputA.setData( new double[]{ 0.5 }, 0 );  // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.125 }, 0 ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().sub(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 0.375) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 }, 1 );
		inputA.setData( new double[]{ 0.0, 0.5,  0.0 }, 1 ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.0, 0.125, 0.0 }, 1 ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().sub(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testSubFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0, -1.0, -1.0 }, new int[]{3} );
		inputA.setData( new double[]{ 1.0, 0.5, 0.03125 }, new int[]{3} );         // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.125, 0.0625, 0.25 }, new int[]{3} ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().sub(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testSubUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new boolean[]{ true }, 0 );
		inputB.setData( new boolean[]{ false }, 0 );

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
	// mul
	// ==================================================

	// --------------------------------------------------
	// mul, INTT64 (long)
	// --------------------------------------------------

	@Test
	public void testMulInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new long[]{ -1L }, 0 );
		inputA.setData( new long[]{ 5L }, 0 );
		inputB.setData( new long[]{ 3L }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().mul(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 15L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new long[]{ -1L, -1L, -1L }, 1 );
		inputA.setData( new long[]{ 0L, 5L, 0L }, 1 );
		inputB.setData( new long[]{ 0L, 3L, 0L }, 1 );
		try {
			new ExecutionUnit().mul(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testMulInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new long[]{ -1L, -1L, -1L }, new int[]{3} );
		inputA.setData( new long[]{ 5L, 1L, -7L }, new int[]{3} );
		inputB.setData( new long[]{ 3L, -8L, -2L }, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().mul(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testMulFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0 }, 0 );
		inputA.setData( new double[]{ 0.5 }, 0 );   // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.125 }, 0 ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().mul(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 0.0625) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 }, 1 );
		inputA.setData( new double[]{ 0.0, 0.5,  0.0 }, 1 );  // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.0, 0.125, 0.0 }, 1 ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().mul(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testMulFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0, -1.0, -1.0 }, new int[]{3} );
		inputA.setData( new double[]{ 1.0, -0.5, -0.03125 }, new int[]{3} );  // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.125, 0.0625, -0.25 }, new int[]{3} ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().mul(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testMulUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new boolean[]{ true }, 0 );
		inputB.setData( new boolean[]{ false }, 0 );

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
	// div, INTT64 (long)
	// --------------------------------------------------

	@Test
	public void testDivInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new long[]{ -1L }, 0 );
		inputA.setData( new long[]{ 8L }, 0 );
		inputB.setData( new long[]{ 3L }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().div(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 2L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new long[]{ -1L, -1L, -1L }, 1 );
		inputA.setData( new long[]{ 0L, 8L, 0L }, 1 );
		inputB.setData( new long[]{ 0L, 3L, 0L }, 1 );
		try {
			new ExecutionUnit().div(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testDivInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new long[]{ -1L, -1L, -1L }, new int[]{3} );
		inputA.setData( new long[]{ 8L, 5L, -7L }, new int[]{3} );
		inputB.setData( new long[]{ 3L, -1L, -2L }, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().div(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testDivFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0 }, 0 );
		inputA.setData( new double[]{ 0.5 }, 0 );   // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.125 }, 0 ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().div(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 4.0) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 }, 1 );
		inputA.setData( new double[]{ 0.0, 0.5,  0.0 }, 1 );  // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.0, 0.125, 0.0 }, 1 ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().div(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testDivFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0, -1.0, -1.0 }, new int[]{3} );
		inputA.setData( new double[]{ 1.0, -0.5, -0.03125 }, new int[]{3} );  // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.125, 0.0625, -0.25 }, new int[]{3} ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().div(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testDivUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new boolean[]{ true }, 0 );
		inputB.setData( new boolean[]{ false }, 0 );

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
	// rem, INTT64 (long)
	// --------------------------------------------------

	@Test
	public void testRemInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new long[]{ -1L }, 0 );
		inputA.setData( new long[]{ 8L }, 0 );
		inputB.setData( new long[]{ 3L }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().rem(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 2L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new long[]{ -1L, -1L, -1L }, 1 );
		inputA.setData( new long[]{ 0L, 8L, 0L }, 1 );
		inputB.setData( new long[]{ 0L, 3L, 0L }, 1 );
		try {
			new ExecutionUnit().rem(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testRemInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new long[]{ -1L, -1L, -1L, -1L }, new int[]{4} );
		inputA.setData( new long[]{ 8L, 8L, -8L, -8L }, new int[]{4} );
		inputB.setData( new long[]{ 3L, -3L, 3L, -3L }, new int[]{4} );

		// 演算を実行
		try {
			new ExecutionUnit().rem(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testRemFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0 }, 0 );
		inputA.setData( new double[]{ 0.8 }, 0 );
		inputB.setData( new double[]{ 0.3 }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().rem(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (FLOAT64_PERMISSIBLE_ERROR < Math.abs(output.getData()[0]-0.2)) { // 期待値 0.2
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 }, 1 );
		inputA.setData( new double[]{ 0.0, 0.8,  0.0 }, 1 );
		inputB.setData( new double[]{ 0.0, 0.3, 0.0 }, 1 );
		try {
			new ExecutionUnit().rem(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testRemFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0, -1.0, -1.0, -1.0, -1.0 }, new int[]{5} );
		inputA.setData( new double[]{ 1.0, 0.8, 0.8, -0.8, -0.8 }, new int[]{5} );
		inputB.setData( new double[]{ 0.125, 0.3, -0.3, 0.3, -0.3 }, new int[]{5} );

		// 演算を実行
		try {
			new ExecutionUnit().rem(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testRemUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new boolean[]{ true }, 0 );
		inputB.setData( new boolean[]{ false }, 0 );

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
	// neg, INTT64 (long)
	// --------------------------------------------------

	@Test
	public void testNegInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setData( new long[]{ -1L }, 0 );
		input.setData( new long[]{ 8L }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().neg(DataType.INT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != -8L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new long[]{ -1L, -1L, -1L }, 1 );
		input.setData( new long[]{ 0L, 8L, 0L }, 1 );
		try {
			new ExecutionUnit().neg(DataType.INT64, output, input);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testNegInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setData( new long[]{ -1L, -1L, -1L }, new int[]{3} );
		input.setData( new long[]{ 0L, 5L, -7L }, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().neg(DataType.INT64, output, input);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testNegFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0 }, 0 );
		input.setData( new double[]{ 0.8 }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().neg(DataType.FLOAT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != -0.8) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 }, 1 );
		input.setData( new double[]{ 0, 0.8, 0 }, 1 );
		try {
			new ExecutionUnit().neg(DataType.FLOAT64, output, input);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testNegFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0, -1.0, -1.0 }, new int[]{3} );
		input.setData( new double[]{ 0L, 0.8, -0.2 }, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().neg(DataType.FLOAT64, output, input);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testNegUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false }, 0 );
		input.setData( new boolean[]{ true }, 0 );

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
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new long[]{ 1L }, 0 );
		inputB.setData( new long[]{ 2L }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false }, 1 );
		inputA.setData( new long[]{ 0L, 1L, 0L }, 1 );
		inputB.setData( new long[]{ 0L, 2L, 0L }, 1 );
		try {
			new ExecutionUnit().eq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().eq(DataType.INT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testEqInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new boolean[]{ false, false, false }, new int[]{3} );
		inputA.setData( new long[]{ 0L, 1L, 2L }, new int[]{3} );
		inputB.setData( new long[]{ 0L, 1L, 3L }, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testEqFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new double[]{ 0.125 }, 0 ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.5 }, 0 );   // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false }, 1 );
		inputA.setData( new double[]{ -1.0, 0.125, -1.0 }, 1 ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ -1.0, 0.5, -1.0 }, 1 );   // 2進表現で割り切れる値
		try {
			new ExecutionUnit().eq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().eq(DataType.FLOAT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testEqFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false, false, false }, new int[]{3} );
		inputA.setData( new double[]{ 0.0, 0.125, 0.5  }, new int[]{3} );   // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.0, 0.125, 0.25 }, new int[]{3} ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testEqStringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<String[]> inputA = new DataContainer<String[]>();
		DataContainer<String[]> inputB = new DataContainer<String[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new String[]{ "Hello" }, 0 );
		inputB.setData( new String[]{ "World" }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.STRING, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false }, 1 );
		inputA.setData( new String[]{ "", "Hello", "" }, 1 );
		inputB.setData( new String[]{ "", "World", "" }, 1 );
		try {
			new ExecutionUnit().eq(DataType.STRING, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().eq(DataType.STRING, output, inputA, inputA);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testEqStringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<String[]> inputA = new DataContainer<String[]>();
		DataContainer<String[]> inputB = new DataContainer<String[]>();
		output.setData( new boolean[]{ false, false, false }, new int[]{3} );
		inputA.setData( new String[]{ "", "Hello", "Good"  }, new int[]{3} );
		inputB.setData( new String[]{ "", "Hello", "Morning" }, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.STRING, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testEqBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new boolean[]{ true }, 0 );
		inputB.setData( new boolean[]{ false }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false }, 1 );
		inputA.setData( new boolean[]{ false, true, false }, 1 );
		inputB.setData( new boolean[]{ false, false, false }, 1 );
		try {
			new ExecutionUnit().eq(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().eq(DataType.BOOL, output, inputA, inputA);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testEqBoolVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false, false, false }, new int[]{3} );
		inputA.setData( new boolean[]{ false, true, true  }, new int[]{3} );
		inputB.setData( new boolean[]{ false, true, false }, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().eq(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testEqUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<Object[]> inputA = new DataContainer<Object[]>();
		DataContainer<Object[]> inputB = new DataContainer<Object[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new Object[]{ null }, 0 );
		inputB.setData( new Object[]{ null }, 0 );

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
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new long[]{ 1L }, 0 );
		inputB.setData( new long[]{ 2L }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false }, 1 );
		inputA.setData( new long[]{ 0L, 1L, 0L }, 1 );
		inputB.setData( new long[]{ 0L, 2L, 0L }, 1 );
		try {
			new ExecutionUnit().neq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().neq(DataType.INT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testNeqInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new boolean[]{ false, false, false }, new int[]{3} );
		inputA.setData( new long[]{ 0L, 1L, 2L }, new int[]{3} );
		inputB.setData( new long[]{ 0L, 1L, 3L }, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testNeqFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new double[]{ 0.125 }, 0 ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.5 }, 0 );   // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false }, 1 );
		inputA.setData( new double[]{ -1.0, 0.125, -1.0 }, 1 ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ -1.0, 0.5, -1.0 }, 1 );   // 2進表現で割り切れる値
		try {
			new ExecutionUnit().neq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().neq(DataType.FLOAT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testNeqFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false, false, false }, new int[]{3} );
		inputA.setData( new double[]{ 0.0, 0.125, 0.5  }, new int[]{3} );   // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.0, 0.125, 0.25 }, new int[]{3} ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testNeqStringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<String[]> inputA = new DataContainer<String[]>();
		DataContainer<String[]> inputB = new DataContainer<String[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new String[]{ "Hello" }, 0 );
		inputB.setData( new String[]{ "World" }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.STRING, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false }, 1 );
		inputA.setData( new String[]{ "", "Hello", "" }, 1 );
		inputB.setData( new String[]{ "", "World", "" }, 1 );
		try {
			new ExecutionUnit().neq(DataType.STRING, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().neq(DataType.STRING, output, inputA, inputA);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testNeqStringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<String[]> inputA = new DataContainer<String[]>();
		DataContainer<String[]> inputB = new DataContainer<String[]>();
		output.setData( new boolean[]{ false, false, false }, new int[]{3} );
		inputA.setData( new String[]{ "", "Hello", "Good"  }, new int[]{3} );
		inputB.setData( new String[]{ "", "Hello", "Morning" }, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.STRING, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testNeqBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new boolean[]{ true }, 0 );
		inputB.setData( new boolean[]{ false }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false }, 1 );
		inputA.setData( new boolean[]{ false, true, false }, 1 );
		inputB.setData( new boolean[]{ false, false, false }, 1 );
		try {
			new ExecutionUnit().neq(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().neq(DataType.BOOL, output, inputA, inputA);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testNeqBoolVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false, false, false }, new int[]{3} );
		inputA.setData( new boolean[]{ false, true, true  }, new int[]{3} );
		inputB.setData( new boolean[]{ false, true, false }, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().neq(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testNeqUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<Object[]> inputA = new DataContainer<Object[]>();
		DataContainer<Object[]> inputB = new DataContainer<Object[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new Object[]{ null }, 0 );
		inputB.setData( new Object[]{ null }, 0 );

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
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new long[]{ 1L }, 0 );
		inputB.setData( new long[]{ 2L }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().geq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false }, 1 );
		inputA.setData( new long[]{ 0L, 1L, 0L }, 1 );
		inputB.setData( new long[]{ 0L, 2L, 0L }, 1 );
		try {
			new ExecutionUnit().geq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().geq(DataType.INT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().geq(DataType.INT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testGeqInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new boolean[]{ false, false, false }, new int[]{3} );
		inputA.setData( new long[]{ 1L, 2L, 3L }, new int[]{3} );
		inputB.setData( new long[]{ 1L, 3L, 2L }, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().geq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testGeqFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new double[]{ 0.125 }, 0 ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.5 }, 0 );   // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().geq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false }, 1 );
		inputA.setData( new double[]{ 0.0, 0.125, 0.0 }, 1 ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.0, 0.5,   0.0 }, 1 ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().geq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().geq(DataType.FLOAT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().geq(DataType.FLOAT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testGeqFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false, false, false }, new int[]{3} );
		inputA.setData( new double[]{ 0.5, 0.125, 0.25 }, new int[]{3} ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.5, 0.25, 0.125 }, new int[]{3} ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().geq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testGeqUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new boolean[]{ true }, 0 );
		inputB.setData( new boolean[]{ false }, 0 );

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
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new long[]{ 1L }, 0 );
		inputB.setData( new long[]{ 2L }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().leq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false }, 1 );
		inputA.setData( new long[]{ 0L, 1L, 0L }, 1 );
		inputB.setData( new long[]{ 0L, 2L, 0L }, 1 );
		try {
			new ExecutionUnit().leq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().leq(DataType.INT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().leq(DataType.INT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testLeqInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new boolean[]{ false, false, false }, new int[]{3} );
		inputA.setData( new long[]{ 1L, 2L, 3L }, new int[]{3} );
		inputB.setData( new long[]{ 1L, 3L, 2L }, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().leq(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testLeqFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new double[]{ 0.125 }, 0 ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.5 }, 0 );   // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().leq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false }, 1 );
		inputA.setData( new double[]{ 0.0, 0.125, 0.0 }, 1 ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.0, 0.5,   0.0 }, 1 ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().leq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().leq(DataType.FLOAT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().leq(DataType.FLOAT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testLeqFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false, false, false }, new int[]{3} );
		inputA.setData( new double[]{ 0.5, 0.125, 0.25 }, new int[]{3} ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.5, 0.25, 0.125 }, new int[]{3} ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().leq(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testLeqUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new boolean[]{ true }, 0 );
		inputB.setData( new boolean[]{ false }, 0 );

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
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new long[]{ 1L }, 0 );
		inputB.setData( new long[]{ 2L }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().gt(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false }, 1 );
		inputA.setData( new long[]{ 0L, 1L, 0L }, 1 );
		inputB.setData( new long[]{ 0L, 2L, 0L }, 1 );
		try {
			new ExecutionUnit().gt(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().gt(DataType.INT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().gt(DataType.INT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testGtInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new boolean[]{ false, false, false }, new int[]{3} );
		inputA.setData( new long[]{ 1L, 2L, 3L }, new int[]{3} );
		inputB.setData( new long[]{ 1L, 3L, 2L }, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().gt(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testGtFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new double[]{ 0.125 }, 0 ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.5 }, 0 );   // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().gt(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false }, 1 );
		inputA.setData( new double[]{ 0.0, 0.125, 0.0 }, 1 ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.0, 0.5,   0.0 }, 1 ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().gt(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().gt(DataType.FLOAT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().gt(DataType.FLOAT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testGtFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false, false, false }, new int[]{3} );
		inputA.setData( new double[]{ 0.5, 0.125, 0.25 }, new int[]{3} ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.5, 0.25, 0.125 }, new int[]{3} ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().gt(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testGtUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new boolean[]{ true }, 0 );
		inputB.setData( new boolean[]{ false }, 0 );

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
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new long[]{ 1L }, 0 );
		inputB.setData( new long[]{ 2L }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().lt(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false }, 1 );
		inputA.setData( new long[]{ 0L, 1L, 0L }, 1 );
		inputB.setData( new long[]{ 0L, 2L, 0L }, 1 );
		try {
			new ExecutionUnit().lt(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().lt(DataType.INT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().lt(DataType.INT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testLtInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<long[]> inputA = new DataContainer<long[]>();
		DataContainer<long[]> inputB = new DataContainer<long[]>();
		output.setData( new boolean[]{ false, false, false }, new int[]{3} );
		inputA.setData( new long[]{ 1L, 2L, 3L }, new int[]{3} );
		inputB.setData( new long[]{ 1L, 3L, 2L }, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().lt(DataType.INT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testLtFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new double[]{ 0.125 }, 0 ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.5 }, 0 );   // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().lt(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false }, 1 );
		inputA.setData( new double[]{ 0.0, 0.125, 0.0 }, 1 ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.0, 0.5,   0.0 }, 1 ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().lt(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) {  // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().lt(DataType.FLOAT64, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) {  // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().lt(DataType.FLOAT64, output, inputA, inputA);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testLtFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false, false, false }, new int[]{3} );
		inputA.setData( new double[]{ 0.5, 0.125, 0.25 }, new int[]{3} ); // 2進表現で割り切れる値
		inputB.setData( new double[]{ 0.5, 0.25, 0.125 }, new int[]{3} ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().lt(DataType.FLOAT64, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testLtUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new boolean[]{ true }, 0 );
		inputB.setData( new boolean[]{ false }, 0 );

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
	// and, BOOL (boolean)
	// --------------------------------------------------

	@Test
	public void testAndBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new boolean[]{ true }, 0 );
		inputB.setData( new boolean[]{ false }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false }, 1 );
		inputA.setData( new boolean[]{ false, true, false }, 1 );
		inputB.setData( new boolean[]{ false, false, false }, 1 );
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputB, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testAndBoolVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false, false, false, false }, new int[]{4} );
		inputA.setData( new boolean[]{ false, true, false, true }, new int[]{4} );
		inputB.setData( new boolean[]{ false, false, true, true }, new int[]{4} );

		// 演算を実行
		try {
			new ExecutionUnit().and(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testAndUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new double[]{ 0.125 }, 0 );
		inputB.setData( new double[]{ 0.25 }, 0 );

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
	// or, BOOL (boolean)
	// --------------------------------------------------

	@Test
	public void testOrBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new boolean[]{ true }, 0 );
		inputB.setData( new boolean[]{ false }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false }, 1 );
		inputA.setData( new boolean[]{ false, true, false }, 1 );
		inputB.setData( new boolean[]{ false, false, false }, 1 );
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputB, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputA, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputB, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testOrBoolVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false, false, false, false }, new int[]{4} );
		inputA.setData( new boolean[]{ false, true, false, true }, new int[]{4} );
		inputB.setData( new boolean[]{ false, false, true, true }, new int[]{4} );

		// 演算を実行
		try {
			new ExecutionUnit().or(DataType.BOOL, output, inputA, inputB);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testOrUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<double[]> inputA = new DataContainer<double[]>();
		DataContainer<double[]> inputB = new DataContainer<double[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new double[]{ 0.125 }, 0 );
		inputB.setData( new double[]{ 0.25 }, 0 );

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
	// not, BOOL (boolean)
	// --------------------------------------------------

	@Test
	public void testNotBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputA = new DataContainer<boolean[]>();
		DataContainer<boolean[]> inputB = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false }, 0 );
		inputA.setData( new boolean[]{ true }, 0 );
		inputB.setData( new boolean[]{ false }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().not(DataType.BOOL, output, inputA);
		} catch (VnanoFatalException e) {
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
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false }, 1 );
		inputA.setData( new boolean[]{ false, true, false }, 1 );
		inputB.setData( new boolean[]{ false, false, false }, 1 );
		try {
			new ExecutionUnit().not(DataType.BOOL, output, inputA);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}
		if (output.getData()[1] != false) { // 期待値はfalse
			fail("Incorrect output value");
		}
		try {
			new ExecutionUnit().not(DataType.BOOL, output, inputB);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[1] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}
	}

	@Test
	public void testNotBoolVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false, false}, new int[]{2} );
		input.setData( new boolean[]{ false, true }, new int[]{2} );

		// 演算を実行
		try {
			new ExecutionUnit().not(DataType.BOOL, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=true || output.getData()[1]!=false) {
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
		output.setData( new double[]{ -1.0 }, 0 );
		input.setData( new double[]{ 0.125 }, 0 );

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
		Object data = target.getData();
		if (!(data instanceof long[])
				|| ((long[])data).length <= target.getOffset()
				|| target.getSize() != 1
				|| target.getLengths().length != 0) {

			fail("Incorrect allocated data");
		}
	}

	@Test
	public void testAllocInt64Array1D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<long[]> len = new DataContainer<long[]>();
		len.setData(new long[]{ 3L }, 0);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocVector(DataType.INT64, target, len);
		} catch (VnanoFatalException e) {
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
		len.setData(new long[]{ 5L }, 0);
		try {
			new ExecutionUnit().allocVector(DataType.INT64, target, len);
		} catch (VnanoFatalException e) {
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
		len.setData(new long[]{ 5L }, 0);
		try {
			new ExecutionUnit().allocVector(DataType.INT64, target, len);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testAllocInt64Array3D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();

		DataContainer<long[]> len0 = new DataContainer<long[]>();
		DataContainer<long[]> len1 = new DataContainer<long[]>();
		DataContainer<long[]> len2 = new DataContainer<long[]>();
		len0.setData(new long[] { 2L }, 0);
		len1.setData(new long[] { 3L }, 0);
		len2.setData(new long[] { 4L }, 0);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocVector(DataType.INT64, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
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
		len0.setData(new long[] { 5L }, 0);
		len1.setData(new long[] { 6L }, 0);
		len2.setData(new long[] { 7L }, 0);
		try {
			new ExecutionUnit().allocVector(DataType.INT64, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
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
		len0.setData(new long[] { 5L }, 0);
		len1.setData(new long[] { 6L }, 0);
		len2.setData(new long[] { 7L }, 0);
		try {
			new ExecutionUnit().allocVector(DataType.INT64, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
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
		Object data = target.getData();
		if (!(data instanceof double[])
				|| ((double[])data).length <= target.getOffset()
				|| target.getSize() != 1
				|| target.getLengths().length != 0) {

			fail("Incorrect allocated data");
		}
	}

	@Test
	public void testAllocFloat64Array1D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<long[]> len = new DataContainer<long[]>();
		len.setData(new long[]{ 3L }, 0);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocVector(DataType.FLOAT64, target, len);
		} catch (VnanoFatalException e) {
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
		len.setData(new long[]{ 5L }, 0);
		try {
			new ExecutionUnit().allocVector(DataType.FLOAT64, target, len);
		} catch (VnanoFatalException e) {
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
		len.setData(new long[]{ 5L }, 0);
		try {
			new ExecutionUnit().allocVector(DataType.FLOAT64, target, len);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testAllocFloat64Array3D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();

		DataContainer<long[]> len0 = new DataContainer<long[]>();
		DataContainer<long[]> len1 = new DataContainer<long[]>();
		DataContainer<long[]> len2 = new DataContainer<long[]>();
		len0.setData(new long[] { 2L }, 0);
		len1.setData(new long[] { 3L }, 0);
		len2.setData(new long[] { 4L }, 0);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocVector(DataType.FLOAT64, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
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
		len0.setData(new long[] { 5L }, 0);
		len1.setData(new long[] { 6L }, 0);
		len2.setData(new long[] { 7L }, 0);
		try {
			new ExecutionUnit().allocVector(DataType.FLOAT64, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
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
		len0.setData(new long[] { 5L }, 0);
		len1.setData(new long[] { 6L }, 0);
		len2.setData(new long[] { 7L }, 0);
		try {
			new ExecutionUnit().allocVector(DataType.FLOAT64, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
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
		Object data = target.getData();
		if (!(data instanceof boolean[])
				|| ((boolean[])data).length <= target.getOffset()
				|| target.getSize() != 1
				|| target.getLengths().length != 0) {

			fail("Incorrect allocated data");
		}
	}

	@Test
	public void testAllocBoolArray1D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<long[]> len = new DataContainer<long[]>();
		len.setData(new long[]{ 3L }, 0);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocVector(DataType.BOOL, target, len);
		} catch (VnanoFatalException e) {
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
		len.setData(new long[]{ 5L }, 0);
		try {
			new ExecutionUnit().allocVector(DataType.BOOL, target, len);
		} catch (VnanoFatalException e) {
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
		len.setData(new long[]{ 5L }, 0);
		try {
			new ExecutionUnit().allocVector(DataType.BOOL, target, len);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testAllocBoolArray3D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();

		DataContainer<long[]> len0 = new DataContainer<long[]>();
		DataContainer<long[]> len1 = new DataContainer<long[]>();
		DataContainer<long[]> len2 = new DataContainer<long[]>();
		len0.setData(new long[] { 2L }, 0);
		len1.setData(new long[] { 3L }, 0);
		len2.setData(new long[] { 4L }, 0);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocVector(DataType.BOOL, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
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
		len0.setData(new long[] { 5L }, 0);
		len1.setData(new long[] { 6L }, 0);
		len2.setData(new long[] { 7L }, 0);
		try {
			new ExecutionUnit().allocVector(DataType.BOOL, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
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
		len0.setData(new long[] { 5L }, 0);
		len1.setData(new long[] { 6L }, 0);
		len2.setData(new long[] { 7L }, 0);
		try {
			new ExecutionUnit().allocVector(DataType.BOOL, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
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
		Object data = target.getData();
		if (!(data instanceof String[])
				|| ((String[])data).length <= target.getOffset()
				|| target.getSize() != 1
				|| target.getLengths().length != 0) {

			fail("Incorrect allocated data");
		}
	}

	@Test
	public void testAllocStringArray1D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<long[]> len = new DataContainer<long[]>();
		len.setData(new long[]{ 3L }, 0);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocVector(DataType.STRING, target, len);
		} catch (VnanoFatalException e) {
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
		len.setData(new long[]{ 5L }, 0);
		try {
			new ExecutionUnit().allocVector(DataType.STRING, target, len);
		} catch (VnanoFatalException e) {
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
		len.setData(new long[]{ 5L }, 0);
		try {
			new ExecutionUnit().allocVector(DataType.STRING, target, len);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testAllocStringArray3D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();

		DataContainer<long[]> len0 = new DataContainer<long[]>();
		DataContainer<long[]> len1 = new DataContainer<long[]>();
		DataContainer<long[]> len2 = new DataContainer<long[]>();
		len0.setData(new long[] { 2L }, 0);
		len1.setData(new long[] { 3L }, 0);
		len2.setData(new long[] { 4L }, 0);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocVector(DataType.STRING, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
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
		len0.setData(new long[] { 5L }, 0);
		len1.setData(new long[] { 6L }, 0);
		len2.setData(new long[] { 7L }, 0);
		try {
			new ExecutionUnit().allocVector(DataType.STRING, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
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
		len0.setData(new long[] { 5L }, 0);
		len1.setData(new long[] { 6L }, 0);
		len2.setData(new long[] { 7L }, 0);
		try {
			new ExecutionUnit().allocVector(DataType.STRING, target, len0, len1, len2);
		} catch (VnanoFatalException e) {
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
		len.setData(new long[]{ 3 }, 0);
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
		src.setData(new long[1], new int[0]);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.INT64, target, src);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testAllocrInt64Array1D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<Object> target = new DataContainer<Object>();
		DataContainer<long[]> src = new DataContainer<long[]>();
		src.setData(new long[3], new int[] { 3 });

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.INT64, target, src);
		} catch (VnanoFatalException e) {
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

		// 要素数を変えての再allocrも検査
		src.setData(new long[5], new int[] { 5 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.INT64, target, src);
		} catch (VnanoFatalException e) {
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
		src.setData(new long[5], new int[] { 5 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.INT64, target, src);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testAllocrInt64Array3D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<long[]> src = new DataContainer<long[]>();
		src.setData(new long[ 2 * 3 * 4 ], new int[] { 2, 3, 4 });

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.INT64, target, src);
		} catch (VnanoFatalException e) {
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
		src.setData(new long[ 5 * 6 * 7 ], new int[] { 5, 6, 7 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.INT64, target, src);
		} catch (VnanoFatalException e) {
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
		src.setData(new long[ 5 * 6 * 7 ], new int[] { 5, 6, 7 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.INT64, target, src);
		} catch (VnanoFatalException e) {
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
	// allocr, FLOAT64 (double)
	// --------------------------------------------------

	@Test
	public void testAllocrFloat64Scalar() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<double[]> src = new DataContainer<double[]>();
		src.setData(new double[1], new int[0]);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.FLOAT64, target, src);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testAllocrFloat64Array1D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<Object> target = new DataContainer<Object>();
		DataContainer<double[]> src = new DataContainer<double[]>();
		src.setData(new double[3], new int[] { 3 });

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.FLOAT64, target, src);
		} catch (VnanoFatalException e) {
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

		// 要素数を変えての再allocrも検査
		src.setData(new double[5], new int[] { 5 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.FLOAT64, target, src);
		} catch (VnanoFatalException e) {
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
		src.setData(new double[5], new int[] { 5 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.FLOAT64, target, src);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testAllocrFloat64Array3D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<double[]> src = new DataContainer<double[]>();
		src.setData(new double[ 2 * 3 * 4 ], new int[] { 2, 3, 4 });

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.FLOAT64, target, src);
		} catch (VnanoFatalException e) {
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
		src.setData(new double[ 5 * 6 * 7 ], new int[] { 5, 6, 7 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.FLOAT64, target, src);
		} catch (VnanoFatalException e) {
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
		src.setData(new double[ 5 * 6 * 7 ], new int[] { 5, 6, 7 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.FLOAT64, target, src);
		} catch (VnanoFatalException e) {
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
	// allocr, BOOL (boolean)
	// --------------------------------------------------

	@Test
	public void testAllocrBoolScalar() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<boolean[]> src = new DataContainer<boolean[]>();
		src.setData(new boolean[1], new int[0]);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.BOOL, target, src);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testAllocrBoolArray1D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<Object> target = new DataContainer<Object>();
		DataContainer<boolean[]> src = new DataContainer<boolean[]>();
		src.setData(new boolean[3], new int[] { 3 });

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.BOOL, target, src);
		} catch (VnanoFatalException e) {
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

		// 要素数を変えての再allocrも検査
		src.setData(new boolean[5], new int[] { 5 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.BOOL, target, src);
		} catch (VnanoFatalException e) {
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
		src.setData(new boolean[5], new int[] { 5 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.BOOL, target, src);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testAllocrBoolArray3D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<boolean[]> src = new DataContainer<boolean[]>();
		src.setData(new boolean[ 2 * 3 * 4 ], new int[] { 2, 3, 4 });

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.BOOL, target, src);
		} catch (VnanoFatalException e) {
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
		src.setData(new boolean[ 5 * 6 * 7 ], new int[] { 5, 6, 7 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.BOOL, target, src);
		} catch (VnanoFatalException e) {
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
		src.setData(new boolean[ 5 * 6 * 7 ], new int[] { 5, 6, 7 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.BOOL, target, src);
		} catch (VnanoFatalException e) {
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
	// allocr, STRIG (String)
	// --------------------------------------------------

	@Test
	public void testAllocrStringScalar() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<String[]> src = new DataContainer<String[]>();
		src.setData(new String[1], new int[0]);

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.STRING, target, src);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testAllocrStringArray1D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<Object> target = new DataContainer<Object>();
		DataContainer<String[]> src = new DataContainer<String[]>();
		src.setData(new String[3], new int[] { 3 });

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.STRING, target, src);
		} catch (VnanoFatalException e) {
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

		// 要素数を変えての再allocrも検査
		src.setData(new String[5], new int[] { 5 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.STRING, target, src);
		} catch (VnanoFatalException e) {
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
		src.setData(new String[5], new int[] { 5 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.STRING, target, src);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testAllocrStringArray3D() {

		// 確保用のデータコンテナを生成して値をセット
		DataContainer<?> target = new DataContainer<Object>();
		DataContainer<String[]> src = new DataContainer<String[]>();
		src.setData(new String[ 2 * 3 * 4 ], new int[] { 2, 3, 4 });

		// 確保処理を実行
		try {
			new ExecutionUnit().allocSameLengths(DataType.STRING, target, src);
		} catch (VnanoFatalException e) {
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
		src.setData(new String[ 5 * 6 * 7 ], new int[] { 5, 6, 7 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.STRING, target, src);
		} catch (VnanoFatalException e) {
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
		src.setData(new String[ 5 * 6 * 7 ], new int[] { 5, 6, 7 });
		try {
			new ExecutionUnit().allocSameLengths(DataType.STRING, target, src);
		} catch (VnanoFatalException e) {
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
		output.setData( new long[]{ -1L }, 0 );
		input.setData( new long[]{ 2L }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.INT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 2L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new long[]{ -1L, -1L, -1L }, 1 );
		input.setData( new long[]{ 0L, 2L, 0L }, 1);
		try {
			new ExecutionUnit().mov(DataType.INT64, output, input);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testMovInt64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setData( new long[]{ -1L, -1L, -1L }, new int[]{3} );
		input.setData( new long[]{ 1L, 2L, 3L }, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.INT64, output, input);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testMovFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0 }, 0 );
		input.setData( new double[]{ 0.25 }, 0 ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.FLOAT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != 0.25) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 }, 1 );
		input.setData( new double[]{ 0.0, 0.25, 0.0 }, 1 ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().mov(DataType.FLOAT64, output, input);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testMovFloat64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0, -1.0, -1.0 }, new int[]{3} );
		input.setData( new double[]{ 0.5, -0.25, 0.125 }, new int[]{3} ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.FLOAT64, output, input);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testMovBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false }, 0 );
		input.setData( new boolean[]{ true }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.BOOL, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false }, 1 );
		input.setData( new boolean[]{ false, true, false }, 1 );
		try {
			new ExecutionUnit().mov(DataType.BOOL, output, input);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testMovBoolVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false, false, false }, new int[]{3} );
		input.setData( new boolean[]{ true, false, true }, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.BOOL, output, input);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testMovStringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setData( new String[]{ "Init" }, 0 );
		input.setData( new String[]{ "Hello" }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.STRING, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getData()[0].equals("Hello")) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new String[]{ "Init0", "Init1", "Init2" }, 1 );
		input.setData( new String[]{ "", "Hello", "" }, 1 );
		try {
			new ExecutionUnit().mov(DataType.STRING, output, input);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testMovStringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setData( new String[]{ "Init0", "Init1", "Init2" }, new int[]{3} );
		input.setData( new String[]{ "Hello", "World", "!" }, new int[]{3} );

		// 演算を実行
		try {
			new ExecutionUnit().mov(DataType.STRING, output, input);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testMovUnoperatableData() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<Object> output = new DataContainer<Object>();
		DataContainer<Object> input = new DataContainer<Object>();
		output.setData(this, 0); // thisは、とにかく演算できないデータを格納しておくため
		input.setData(this, 0);

		// 対応していないデータ型を指定して演算を実行
		try {
			new ExecutionUnit().mov(DataType.VOID, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}

		// 指定したデータ型と異なる型のデータで演算を実行
		try {
			new ExecutionUnit().mov(DataType.INT64, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().mov(DataType.FLOAT64, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().mov(DataType.BOOL, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			new ExecutionUnit().mov(DataType.STRING, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
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
		output.setData( new long[]{ -1L, -1L, -1L }, new int[]{3} );
		input.setData( new long[]{ 2L }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().fill(DataType.INT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=2L || output.getData()[1]!=2L || output.getData()[2]!=2L) {
			fail("Incorrect output value");
		}

		// inoutがオフセット設定された場合でも正常に動作するか検査
		output.setData( new long[]{ -1L, -1L, -1L }, new int[] {3} );
		input.setData( new long[]{ 0L, 8L, 0L }, 1 );
		try {
			new ExecutionUnit().fill(DataType.INT64, output, input);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testFillFloat64() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0, -1.0, -1.0 }, new int[]{3} );
		input.setData( new double[]{ 0.25 }, 0 ); // 2進表現で割り切れる値

		// 演算を実行
		try {
			new ExecutionUnit().fill(DataType.FLOAT64, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=0.25 || output.getData()[1]!=0.25 || output.getData()[2]!=0.25) {
			fail("Incorrect output value");
		}

		// inoutがオフセット設定された場合でも正常に動作するか検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 }, new int[] {3} );
		input.setData( new double[]{ 0.0, 0.125, 0.0 }, 1 );
		try {
			new ExecutionUnit().fill(DataType.FLOAT64, output, input);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testFillBool() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false, false, false }, new int[]{3} );
		input.setData( new boolean[]{ true }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().fill(DataType.BOOL, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (output.getData()[0]!=true || output.getData()[1]!=true || output.getData()[2]!=true) {
			fail("Incorrect output value");
		}

		// inoutがオフセット設定された場合でも正常に動作するか検査
		output.setData( new boolean[]{ false, false, false }, new int[]{3} );
		input.setData( new boolean[]{ false, true, false }, 1 );
		try {
			new ExecutionUnit().fill(DataType.BOOL, output, input);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testFillString() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setData( new String[]{ "Init0", "Init1", "Init2" }, new int[]{3} );
		input.setData( new String[]{ "Hello" }, 0 );

		// 演算を実行
		try {
			new ExecutionUnit().fill(DataType.STRING, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい演算結果が格納されているか検査
		if (!output.getData()[0].equals("Hello")
				|| !output.getData()[1].equals("Hello")
				|| !output.getData()[2].equals("Hello") ) {

			fail("Incorrect output value");
		}

		// inputがオフセット設定された場合でも正常に動作するか検査
		output.setData( new String[]{ "Init0", "Init1", "Init2" }, new int[]{3} );
		input.setData( new String[]{ "", "Hello", "" }, 1 );
		try {
			new ExecutionUnit().fill(DataType.STRING, output, input);
		} catch (VnanoFatalException e) {
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
	// elem
	// ==================================================

	// --------------------------------------------------
	// elem, INT64 (long)
	// --------------------------------------------------

	@Test
	public void testElemInt64() throws VnanoException {

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
		src.setData(arrayData, srcLengths);

		// [1][0][2] の要素(=14)を参照する
		index0.setData(new long[] {1}, 0);
		index1.setData(new long[] {0}, 0);
		index2.setData(new long[] {2}, 0);
		try {
			new ExecutionUnit().elem(DataType.INT64, dest, src, index0, index1, index2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (dest.getData()[ dest.getOffset() ] != 14L) {
			fail("Incorrect output value");
		}

		// [0][2][3] の要素(=11)を参照する
		index0.setData(new long[] {0}, 0);
		index1.setData(new long[] {2}, 0);
		index2.setData(new long[] {3}, 0);
		try {
			new ExecutionUnit().elem(DataType.INT64, dest, src, index0, index1, index2);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testElemFloat64() throws VnanoException {

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
		src.setData(arrayData, srcLengths);

		// [1][0][2] の要素(=14)を参照する
		index0.setData(new long[] {1}, 0);
		index1.setData(new long[] {0}, 0);
		index2.setData(new long[] {2}, 0);
		try {
			new ExecutionUnit().elem(DataType.FLOAT64, dest, src, index0, index1, index2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (dest.getData()[ dest.getOffset() ] != 14.0) {
			fail("Incorrect output value");
		}

		// [0][2][3] の要素(=11)を参照する
		index0.setData(new long[] {0}, 0);
		index1.setData(new long[] {2}, 0);
		index2.setData(new long[] {3}, 0);
		try {
			new ExecutionUnit().elem(DataType.FLOAT64, dest, src, index0, index1, index2);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testElemBool() throws VnanoException {

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
		src.setData(arrayData, srcLengths);

		// [1][0][2] の要素(=true)を参照する
		index0.setData(new long[] {1}, 0);
		index1.setData(new long[] {0}, 0);
		index2.setData(new long[] {2}, 0);
		try {
			new ExecutionUnit().elem(DataType.BOOL, dest, src, index0, index1, index2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (dest.getData()[ dest.getOffset() ] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// [0][2][3] の要素(=false)を参照する
		index0.setData(new long[] {0}, 0);
		index1.setData(new long[] {2}, 0);
		index2.setData(new long[] {3}, 0);
		try {
			new ExecutionUnit().elem(DataType.BOOL, dest, src, index0, index1, index2);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testElemString() throws VnanoException {

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
		src.setData(arrayData, srcLengths);

		// [1][0][2] の要素(=true)を参照する
		index0.setData(new long[] {1}, 0);
		index1.setData(new long[] {0}, 0);
		index2.setData(new long[] {2}, 0);
		try {
			new ExecutionUnit().elem(DataType.STRING, dest, src, index0, index1, index2);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 正しい結果が格納されているか検査
		if (!dest.getData()[ dest.getOffset() ].equals("102")) {
			fail("Incorrect output value");
		}

		// [0][2][3] の要素(=false)を参照する
		index0.setData(new long[] {0}, 0);
		index1.setData(new long[] {2}, 0);
		index2.setData(new long[] {3}, 0);
		try {
			new ExecutionUnit().elem(DataType.STRING, dest, src, index0, index1, index2);
		} catch (VnanoFatalException e) {
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

	@Test
	public void testElemUnoperatableData() throws VnanoException {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<?> dest = new DataContainer<Object>();  // 要素を格納するコンテナ
		DataContainer<ExecutionUnitTest[]> src = new DataContainer<ExecutionUnitTest[]>();   // 参照する配列のコンテナ(演算不能な型の例として便宜的にExecutionUnitTest[]を使用)

		DataContainer<long[]> index0 = new DataContainer<long[]>();
		DataContainer<long[]> index1 = new DataContainer<long[]>();
		DataContainer<long[]> index2 = new DataContainer<long[]>();

		src.setData((ExecutionUnitTest[])null, new int[] { 1, 2, 3 });

		// [1][0][2] の要素(=true)を参照する
		index0.setData(new long[] {1}, 0);
		index1.setData(new long[] {0}, 0);
		index2.setData(new long[] {2}, 0);

		// 演算を実行
		try {
			new ExecutionUnit().elem(DataType.VOID, dest, src, index0, index1, index2);
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
		output.setData( new long[]{ -1L }, 0 );
		input.setData( new long[]{ 2L }, 0 );

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
		if (output.getData()[0] != 2L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new long[]{ -1L, -1L, -1L }, 1 );
		input.setData( new long[]{ 0L, 0L, 2L, 0L, 0L }, 2 );
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
		if (output.getData()[ output.getOffset() ] != 2L) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1L || output.getData()[2] != -1L) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testCastInt64Int64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setData( new long[]{ -1L, -1L, -1L }, new int[]{ 3 } );
		input.setData( new long[]{ 1L, 2L, 3L }, new int[]{ 3 } );

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
		if (output.getData()[0] != 1L || output.getData()[1] != 2L || output.getData()[2] != 3L) {
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
		output.setData( new double[]{ -1.0 }, 0 );
		input.setData( new long[]{ 2L }, 0 );

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
		if (output.getData()[0] != 2.0) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 }, 1 );
		input.setData( new long[]{ 0L, 0L, 2L, 0L, 0L }, 2 );
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
		if (output.getData()[ output.getOffset() ] != 2.0) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1.0 || output.getData()[2] != -1.0) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testCastInt64Float64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setData( new double[]{ -1.0, -1.0, -1.0 }, new int[]{ 3 } );
		input.setData( new long[]{ 1L, 2L, 3L }, new int[]{ 3 } );

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
		if (output.getData()[0] != 1.0 || output.getData()[1] != 2.0 || output.getData()[2] != 3.0) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, INT64 to STRING (long to String)
	// --------------------------------------------------

	@Test
	public void testCastInt64StringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setData( new String[]{ "Init" }, 0 );
		input.setData( new long[]{ 2L }, 0 );

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
		if (!output.getData()[0].equals("2")) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new String[]{ "Init0", "Init1", "Init2" }, 1 );
		input.setData( new long[]{ 0L, 0L, 2L, 0L, 0L }, 2 );
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
		if (!output.getData()[ output.getOffset() ].equals("2")) {
			fail("Incorrect output value");
		}
		if (!output.getData()[0].equals("Init0") || !output.getData()[2].equals("Init2")) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testCastInt64StringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<long[]> input = new DataContainer<long[]>();
		output.setData( new String[]{ "Init0", "Init1", "Init2" }, new int[]{ 3 } );
		input.setData( new long[]{ 1L, 2L, 3L }, new int[]{ 3 } );

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
		if (!output.getData()[0].equals("1")
				|| !output.getData()[1].equals("2")
				|| !output.getData()[2].equals("3") ) {

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
		output.setData( new double[]{ -1.0 }, 0 );
		input.setData( new double[]{ 2.25 }, 0 ); // 2進表現で割り切れる値

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
		if (output.getData()[0] != 2.25) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 }, 1 );
		input.setData( new double[]{ 0.0, 0.0, 2.25, 0.0, 0.0 }, 2 );
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
		if (output.getData()[ output.getOffset() ] != 2.25) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1.0 || output.getData()[2] != -1.0) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testCastFloat64Float64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setData( new double[]{ -1.0, -1.0, -1.0 }, new int[]{ 3 } );
		input.setData( new double[]{ 1.125, 2.25, 3.5 }, new int[]{ 3 } ); // 2進表現で割り切れる値

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
		if (output.getData()[0]!=1.125 || output.getData()[1]!=2.25 || output.getData()[2]!=3.5) {
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
		output.setData( new long[]{ -1L }, 0 );
		input.setData( new double[]{ 2.25 }, 0 ); // 2進表現で割り切れる値

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
		if (output.getData()[0] != 2L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new long[]{ -1L, -1L, -1L }, 1 );
		input.setData( new double[]{ 0.0, 0.0, 2.25, 0.0, 0.0 }, 2 );
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
		if (output.getData()[ output.getOffset() ] != 2L) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1L || output.getData()[2] != -1L) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testCastFloat64Int64Vector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setData( new long[]{ -1L, -1L, -1L }, new int[]{ 3 } );
		input.setData( new double[]{ 1.125, 2.25, 3.5 }, new int[]{ 3 } ); // 2進表現で割り切れる値

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
		if (output.getData()[0] != 1L || output.getData()[1] != 2L || output.getData()[2] != 3L) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, FLOAT64 to STRING (double to String)
	// --------------------------------------------------

	@Test
	public void testCastFloat64StringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setData( new String[]{ "Init" }, 0 );
		input.setData( new double[]{ 2.25 }, 0 ); // 2進表現で割り切れる値

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
		if (!output.getData()[0].equals("2.25")) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new String[]{ "Init0", "Init1", "Init2" }, 1 );
		input.setData( new double[]{ 0.0, 0.0, 2.25, 0.0, 0.0 }, 2 );
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
		if (!output.getData()[ output.getOffset() ].equals("2.25")) {
			fail("Incorrect output value");
		}
		if (!output.getData()[0].equals("Init0") || !output.getData()[2].equals("Init2")) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testCastFloat64StringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<double[]> input = new DataContainer<double[]>();
		output.setData( new String[]{ "Init0", "Init1", "Init2" }, new int[]{ 3 } );
		input.setData( new double[]{ 1.125, 2.25, 3.5 }, new int[]{ 3 } ); // 2進表現で割り切れる値

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
		if (!output.getData()[0].equals("1.125")
				|| !output.getData()[1].equals("2.25")
				|| !output.getData()[2].equals("3.5") ) {

			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, BOOL to BOOL (boolean to String)
	// --------------------------------------------------

	@Test
	public void testCastBoolBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false }, 0 );
		input.setData( new boolean[]{ true }, 0 );

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
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false }, 1 );
		input.setData( new boolean[]{ false, false, true, false, false }, 2 );
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
		if (output.getData()[ output.getOffset() ] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}
		if (output.getData()[0] != false || output.getData()[2] != false) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testCastBoolBoolVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setData( new boolean[]{ false, false, false }, new int[]{ 3 } );
		input.setData( new boolean[]{ true, false, true }, new int[]{ 3 } );

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
		if (output.getData()[0]!=true || output.getData()[1]!=false || output.getData()[2]!=true ) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, BOOL to STRING (boolean to String)
	// --------------------------------------------------

	@Test
	public void testCastBoolStringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setData( new String[]{ "Init" }, 0 );
		input.setData( new boolean[]{ true }, 0 );

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
		if (!output.getData()[0].equals("true")) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new String[]{ "Init0", "Init1", "Init2" }, 1 );
		input.setData( new boolean[]{ false, false, true, false, false }, 2 );
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
		if (!output.getData()[ output.getOffset() ].equals("true")) {
			fail("Incorrect output value");
		}
		if (!output.getData()[0].equals("Init0") || !output.getData()[2].equals("Init2")) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testCastBoolStringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<boolean[]> input = new DataContainer<boolean[]>();
		output.setData( new String[]{ "Init0", "Init1", "Init2" }, new int[]{ 3 } );
		input.setData( new boolean[]{ true, false, true }, new int[]{ 3 } );

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
		if (!output.getData()[0].equals("true")
				|| !output.getData()[1].equals("false")
				|| !output.getData()[2].equals("true") ) {

			fail("Incorrect output value");
		}
	}


	// --------------------------------------------------
	// cast, STRING to STRING (String to String)
	// --------------------------------------------------

	@Test
	public void testCastStringStringScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setData( new String[]{ "Init" }, 0 );
		input.setData( new String[]{ "Hello" }, 0 );

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
		if (!output.getData()[0].equals("Hello")) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new String[]{ "Init0", "Init1", "Init2" }, 1 );
		input.setData( new String[]{ "", "", "Hello", "", "" }, 2 );
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
		if (!output.getData()[ output.getOffset() ].equals("Hello")) {
			fail("Incorrect output value");
		}
		if (!output.getData()[0].equals("Init0") || !output.getData()[2].equals("Init2")) {
			fail("Incorrect output value");
		}
	}

	@Test
	public void testCastStringStringVector() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<String[]> output = new DataContainer<String[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setData( new String[]{ "Init0", "Init1", "Init2" }, new int[]{ 3 } );
		input.setData( new String[]{ "Hello", "World", "!" }, new int[]{ 3 } );

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
		if (!output.getData()[0].equals("Hello")
				|| !output.getData()[1].equals("World")
				|| !output.getData()[2].equals("!") ) {

			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, STRING to INT64 (String to long)
	// --------------------------------------------------

	@Test
	public void testCastStringInt64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<long[]> output = new DataContainer<long[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setData( new long[]{ -1L }, 0 );
		input.setData( new String[]{ "2" }, 0 );

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
		if (output.getData()[0] != 2L) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new long[]{ -1L, -1L, -1L }, 1 );
		input.setData( new String[]{ "0", "0", "2", "0", "0" }, 2 );
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
		if (output.getData()[ output.getOffset() ] != 2L) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1L || output.getData()[2] != -1L) {
			fail("Incorrect output value");
		}

		// 浮動小数点として解釈できる文字列から整数への変換も検査(内部処理が異なる)
		output.setData( new long[]{ -1L, -1L, -1L }, 1 );
		input.setData( new String[]{ "0", "0", "8.25", "0", "0" }, 2 ); // 2進表現で割り切れる値
		try {
			new ExecutionUnit().cast(DataType.INT64, DataType.STRING, output, input);
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Cast failed");
		}
		if (output.getData()[ output.getOffset() ] != 8L) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1L || output.getData()[2] != -1L) {
			fail("Incorrect output value");
		}

		// 整数に変換できない文字列からの変換も検査
		output.setData( new long[]{ -1L, -1L, -1L }, 1 );
		input.setData( new String[]{ "0", "0", "Hello", "0", "0" }, 2 ); // 2進表現で割り切れる値
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
		output.setData( new long[]{ -1L, -1L, -1L }, new int[]{ 3 } );
		input.setData( new String[]{ "1", "2", "3" }, new int[]{ 3 } );

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
		if (output.getData()[0] != 1L || output.getData()[1] != 2L || output.getData()[2] != 3L) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, STRING to FLOAT64 (String to double)
	// --------------------------------------------------

	@Test
	public void testCastStringFloat64Scalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<double[]> output = new DataContainer<double[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setData( new double[]{ -1.0 }, 0 );
		input.setData( new String[]{ "2.25" }, 0 ); // 2進表現で割り切れる値

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
		if (output.getData()[0] != 2.25) {
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 }, 1 );
		input.setData( new String[]{ "0.0", "0.0", "2.25", "0.0", "0.0" }, 2 );
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
		if (output.getData()[ output.getOffset() ] != 2.25) {
			fail("Incorrect output value");
		}
		if (output.getData()[0] != -1.0 || output.getData()[2] != -1.0) {
			fail("Incorrect output value");
		}

		// 浮動小数点数に変換できない文字列からの変換も検査
		output.setData( new double[]{ -1.0, -1.0, -1.0 }, 1 );
		input.setData( new String[]{ "0.0", "0.0", "Hello", "0.0", "0.0" }, 2 ); // 2進表現で割り切れる値
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
		output.setData( new double[]{ -1.0, -1.0, -1.0 }, new int[]{ 3 } );
		input.setData( new String[]{ "1.125", "2.25", "3.5" }, new int[]{ 3 } ); // 2進表現で割り切れる値

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
		if (output.getData()[0]!=1.125 || output.getData()[1]!=2.25 || output.getData()[2]!=3.5) {
			fail("Incorrect output value");
		}
	}

	// --------------------------------------------------
	// cast, STRING to BOOL (String to boolean)
	// --------------------------------------------------

	@Test
	public void testCastStringBoolScalar() {

		// 入力・出力用のデータコンテナを生成して値をセット
		DataContainer<boolean[]> output = new DataContainer<boolean[]>();
		DataContainer<String[]> input = new DataContainer<String[]>();
		output.setData( new boolean[]{ false }, 0 );
		input.setData( new String[]{ "true" }, 0 ); // 2進表現で割り切れる値

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
		if (output.getData()[0] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}

		// オフセット設定された条件下でも正しく機能するか検査
		output.setData( new boolean[]{ false, false, false }, 1 );
		input.setData( new String[]{ "false", "false", "true", "false", "false" }, 2 );
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
		if (output.getData()[ output.getOffset() ] != true) { // 期待値はtrue
			fail("Incorrect output value");
		}
		if (output.getData()[0] != false || output.getData()[2] != false) {
			fail("Incorrect output value");
		}

		// 論理値に変換できない文字列からの変換も検査
		output.setData( new boolean[]{ false, false, false }, 1 );
		input.setData( new String[]{ "0.0", "0.0", "Hello", "0.0", "0.0" }, 2 ); // 2進表現で割り切れる値
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
		output.setData( new boolean[]{ false, false, false }, new int[]{ 3 } );
		input.setData( new String[]{ "true", "false", "true" }, new int[]{ 3 } ); // 2進表現で割り切れる値

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
		if (output.getData()[0]!=true || output.getData()[1]!=false || output.getData()[2]!=true) {
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
			((DataContainer<long[]>)output).setData(new long[]{ -1L }, 0);
			((DataContainer<boolean[]>)input).setData(new boolean[]{ false }, 0);
			new ExecutionUnit().cast(DataType.INT64, DataType.BOOL, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<long[]>)output).setData(new long[]{ -1L }, 0);
			((DataContainer<Object[]>)input).setData(new Object[]{ "" }, 0);
			new ExecutionUnit().cast(DataType.INT64, DataType.VOID, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<double[]>)output).setData(new double[]{ -1.0 }, 0);
			((DataContainer<boolean[]>)input).setData(new boolean[]{ false }, 0);
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.BOOL, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<double[]>)output).setData(new double[]{ -1.0 }, 0);
			((DataContainer<Object[]>)input).setData(new Object[]{ "" }, 0);
			new ExecutionUnit().cast(DataType.FLOAT64, DataType.VOID, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<boolean[]>)output).setData(new boolean[]{ false }, 0);
			((DataContainer<long[]>)input).setData(new long[]{ 123L }, 0);
			new ExecutionUnit().cast(DataType.BOOL, DataType.INT64, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<boolean[]>)output).setData(new boolean[]{ false }, 0);
			((DataContainer<double[]>)input).setData(new double[]{ 2.25 }, 0);
			new ExecutionUnit().cast(DataType.BOOL, DataType.FLOAT64, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<boolean[]>)output).setData(new boolean[]{ false }, 0);
			((DataContainer<Object[]>)input).setData(new Object[]{ "" }, 0);
			new ExecutionUnit().cast(DataType.BOOL, DataType.VOID, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<String[]>)output).setData(new String[]{ "Init" }, 0);
			((DataContainer<Object[]>)input).setData(new Object[]{ "" }, 0);
			new ExecutionUnit().cast(DataType.STRING, DataType.VOID, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
		try {
			((DataContainer<Object[]>)output).setData(new Object[]{ "" }, 0);
			((DataContainer<Object[]>)input).setData(new Object[]{ "" }, 0);
			new ExecutionUnit().cast(DataType.VOID, DataType.VOID, output, input);
			fail("Expected exception did not occured");
		} catch (VnanoException e) {
			// 例外が発生するのが正しい挙動
		}
	}


}
