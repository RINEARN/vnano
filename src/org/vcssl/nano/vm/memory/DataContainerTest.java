/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.memory;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.DataType;

public class DataContainerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		this.testDefaultState(new DataContainer<long[]>());
		this.testInitialize();
		this.testSetGetArrayData();
		this.testGetArrayOffset();
		this.testGetArrayLengths();
		this.testGetArrayRank();
		this.testSetGetFloat64ScalarData();
		this.testSetGetInt64ScalarData();
		this.testSetGetBoolScalarData();
		this.testSetGetStringScalarData();
		this.testGetDataType();
	}

	private void testDefaultState(DataContainer<?> container) {

		// デフォルトではデータを保持していない
		if (container.getArrayData() != null) {
			fail("Incorrect data");
		}

		// デフォルトではスカラなのでサイズ1
		if (container.getArraySize() != 1) {
			fail("Incorrect size");
		}

		// デフォルトではスカラなので0次元
		if (container.getArrayRank() != 0
				|| container.getArrayLengths().length != 0) {

			fail("Incorrect rank");
		}
		// オフセットは配列の要素参照でしか使用しないのでデフォルトは0
		if (container.getArrayOffset() != 0) {
			fail("Incorrect offset");
		}
		// データ未格納の時点での型はVOID
		if (container.getDataType() != DataType.VOID) {
			fail("Incorrect data type");
		}
	}

	private void testInitialize() {
		DataContainer<long[]> container = new DataContainer<long[]>();

		// 状態をデフォルトから変える
		int lengths[] = new int[] { 5 };
		container.setArrayData(new long[4], 0, lengths);

		// 初期化
		container.initialize();

		// デフォルトに戻っているか検査
		this.testDefaultState(container);

		// オフセット値を変える場合も試す
		container = new DataContainer<long[]>();
		int offset = 1;
		container.setArrayData(new long[4], offset, DataContainer.ARRAY_LENGTHS_OF_SCALAR);

		// 初期化
		container.initialize();

		// デフォルトに戻っているか検査
		this.testDefaultState(container);
	}

	private void testSetGetArrayData() {
		DataContainer<long[]> container = new DataContainer<long[]>();
		long[] data = new long[]{1L, 2L, 3L};
		container.setArrayData(data, 0, new int[] {3});
		if (container.getArrayData() != data) {
			fail("Incorrect data");
		}

		// 参照リンク経由での読み込みテスト
		DataContainer<long[]> refContainer = new DataContainer<long[]>();
		refContainer.refer(container);
		if (refContainer.getArrayData() != data) {
			fail("Incorrect data");
		}

		// 参照リンク経由での書き込みテスト
		long[] newData = new long[]{4L, 5L, 6L};
		refContainer.setArrayData(newData, 0, new int[] {3});
		if (refContainer.getArrayData() != newData) {
			fail("Incorrect data");
		}
		if (container.getArrayData() != newData) {
			fail("Incorrect data");
		}
	}

	private void testGetArrayOffset() {
		DataContainer<long[]> container = new DataContainer<long[]>();
		int offset = 3;
		container.setArrayData(new long[5], offset, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		if (container.getArrayOffset() != offset) {
			fail("Incorrect offset");
		}

		// 参照リンク経由での読み込みテスト
		DataContainer<long[]> refContainer = new DataContainer<long[]>();
		refContainer.refer(container);
		if (refContainer.getArrayOffset() != offset) {
			fail("Incorrect offset");
		}

		// 参照リンク経由での書き込みテスト
		refContainer.setArrayData(new long[5], 4, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		int newOffset = 4;
		if (refContainer.getArrayOffset() != newOffset) {
			fail("Incorrect offset");
		}
		if (container.getArrayOffset() != newOffset) {
			fail("Incorrect offset");
		}
	}

	private void testGetArrayLengths() {
		int[] lengths = new int[] {1, 2, 3};
		DataContainer<long[]> container = new DataContainer<long[]>();
		container.setArrayData(new long[] {1l, 2l, 3l, 4l, 5l, 6l}, 0, lengths);
		if (container.getArrayLengths().length != 3
				|| container.getArrayLengths()[0] != 1
				|| container.getArrayLengths()[1] != 2
				|| container.getArrayLengths()[2] != 3 ) {

			fail("Incorrect lengths");
		}

		// 参照リンク経由での読み込みテスト
		DataContainer<long[]> refContainer = new DataContainer<long[]>();
		refContainer.refer(container);
		if (refContainer.getArrayLengths() != lengths) {
			fail("Incorrect lengths");
		}

		// 参照リンク経由での書き込みテスト
		int[] newLengths = new int[] {3, 2, 1};
		refContainer.setArrayData(new long[] {1l, 2l, 3l, 4l, 5l, 6l}, 0, newLengths);
		if (refContainer.getArrayLengths() != newLengths) {
			fail("Incorrect lengths");
		}
		if (container.getArrayLengths() != newLengths) {
			fail("Incorrect lengths");
		}
	}

	private void testGetArrayRank() {
		int[] lengths = new int[]{1, 2, 3};
		int rank = lengths.length;
		DataContainer<long[]> container = new DataContainer<long[]>();
		container.setArrayData(new long[] {1l, 2l, 3l, 4l, 5l, 6l}, 0, lengths);
		if (container.getArrayRank() != rank) {
			fail("Incorrect rank");
		}

		// 参照リンク経由での読み込みテスト
		DataContainer<long[]> refContainer = new DataContainer<long[]>();
		refContainer.refer(container);
		if (refContainer.getArrayRank() != rank) {
			fail("Incorrect rank");
		}

		// 参照リンク経由での書き込みテスト
		int[] newLengths = new int[] {1, 2, 3, 1};
		int newRank = newLengths.length;
		refContainer.setArrayData(new long[] {1l, 2l, 3l, 4l, 5l, 6l}, 0, newLengths);
		if (refContainer.getArrayRank() != newRank) {
			fail("Incorrect rank");
		}
		if (container.getArrayRank() != newRank) {
			fail("Incorrect rank");
		}
	}

	private void testSetGetFloat64ScalarData() {
		DataContainer<double[]> container = new DataContainer<double[]>();

		// 何も格納していない状態で値を取り出すテスト
		try {
			container.getFloat64ScalarData();
			fail("Expected exception did not occurred");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}

		// 値を設定して取り出すテスト
		container.setFloat64ScalarData(1.25);
		assertTrue(1.25 == container.getFloat64ScalarData()); // 2進表現で割り切れる値なので一致するはず (assertEquals では警告が出るがむやみに suppress したくない)
		container.setFloat64ScalarData(2.5);
		assertTrue(2.5 == container.getFloat64ScalarData()); // 上記コメント参照

		// 次元や要素数などを確認
		assertEquals(DataContainer.ARRAY_RANK_OF_SCALAR, container.getArrayRank());
		assertTrue(Arrays.equals(DataContainer.ARRAY_LENGTHS_OF_SCALAR, container.getArrayLengths()));
		assertEquals(0, container.getArrayOffset());

		// 別の型で初期化してから値を設定するテスト
		DataContainer<long[]> container2 = new DataContainer<long[]>();
		container2.setInt64ScalarData(123);
		try {
			container2.setFloat64ScalarData(1.25);
			fail("Expected exception did not occurred");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}

	private void testSetGetInt64ScalarData() {
		DataContainer<long[]> container = new DataContainer<long[]>();

		// 何も格納していない状態で値を取り出すテスト
		try {
			container.getFloat64ScalarData();
			fail("Expected exception did not occurred");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}

		// 値を設定して取り出すテスト
		container.setInt64ScalarData(123);
		assertEquals(123, container.getInt64ScalarData());
		container.setInt64ScalarData(456);
		assertEquals(456, container.getInt64ScalarData());

		// 次元や要素数などを確認
		assertEquals(DataContainer.ARRAY_RANK_OF_SCALAR, container.getArrayRank());
		assertTrue(Arrays.equals(DataContainer.ARRAY_LENGTHS_OF_SCALAR, container.getArrayLengths()));
		assertEquals(0, container.getArrayOffset());

		// 別の型で初期化してから値を設定するテスト
		DataContainer<double[]> container2 = new DataContainer<double[]>();
		container2.setFloat64ScalarData(1.25);
		try {
			container2.setInt64ScalarData(123);
			fail("Expected exception did not occurred");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}

	private void testSetGetBoolScalarData() {
		DataContainer<boolean[]> container = new DataContainer<boolean[]>();

		// 何も格納していない状態で値を取り出すテスト
		try {
			container.getFloat64ScalarData();
			fail("Expected exception did not occurred");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}

		// 値を設定して取り出すテスト
		container.setBoolScalarData(true);
		assertTrue(container.getBoolScalarData());
		container.setBoolScalarData(false);
		assertFalse(container.getBoolScalarData());

		// 次元や要素数などを確認
		assertEquals(DataContainer.ARRAY_RANK_OF_SCALAR, container.getArrayRank());
		assertTrue(Arrays.equals(DataContainer.ARRAY_LENGTHS_OF_SCALAR, container.getArrayLengths()));
		assertEquals(0, container.getArrayOffset());

		// 別の型で初期化してから値を設定するテスト
		DataContainer<long[]> container2 = new DataContainer<long[]>();
		container2.setFloat64ScalarData(123);
		try {
			container2.setBoolScalarData(true);
			fail("Expected exception did not occurred");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}

	private void testSetGetStringScalarData() {
		DataContainer<String[]> container = new DataContainer<String[]>();

		// 何も格納していない状態で値を取り出すテスト
		try {
			container.getStringScalarData();
			fail("Expected exception did not occurred");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}

		// 値を設定して取り出すテスト
		container.setStringScalarData("aiueo");
		assertEquals("aiueo", container.getStringScalarData());
		container.setStringScalarData("kakikukeko");
		assertEquals("kakikukeko", container.getStringScalarData());

		// 次元や要素数などを確認
		assertEquals(DataContainer.ARRAY_RANK_OF_SCALAR, container.getArrayRank());
		assertTrue(Arrays.equals(DataContainer.ARRAY_LENGTHS_OF_SCALAR, container.getArrayLengths()));
		assertEquals(0, container.getArrayOffset());

		// 別の型で初期化してから値を設定するテスト
		DataContainer<long[]> container2 = new DataContainer<long[]>();
		container2.setFloat64ScalarData(123);
		try {
			container2.setStringScalarData("aiueo");
			fail("Expected exception did not occurred");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}

	@SuppressWarnings("unchecked")
	private void testGetDataType() {
		DataContainer<?> container = new DataContainer<Object>();
		DataContainer<?> refContainer = new DataContainer<Object>();

		// データ未格納の時点での型はVOIDである事を検査
		if (container.getDataType() != DataType.VOID) {
			fail("Incorrect data type");
		}
		((DataContainer<Object>)refContainer).refer((DataContainer<Object>)container);
		if (refContainer.getDataType() != DataType.VOID) {
			fail("Incorrect data type");
		}

		// long[] のデータを持たせるとINT64型になる事を検査
		((DataContainer<long[]>)container).setArrayData(new long[]{ 1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		if (container.getDataType() != DataType.INT64) {
			fail("Incorrect data type");
		}
		((DataContainer<long[]>)refContainer).refer((DataContainer<long[]>)container);
		if (refContainer.getDataType() != DataType.INT64) {
			fail("Incorrect data type");
		}

		// double[] のデータを持たせるとFLOAT64型になる事を検査
		((DataContainer<double[]>)container).setArrayData(new double[]{ 1.0 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		if (container.getDataType() != DataType.FLOAT64) {
			fail("Incorrect data type");
		}
		((DataContainer<double[]>)refContainer).refer((DataContainer<double[]>)container);
		if (refContainer.getDataType() != DataType.FLOAT64) {
			fail("Incorrect data type");
		}

		// boolean[] のデータを持たせるとBOOL型になる事を検査
		((DataContainer<boolean[]>)container).setArrayData(new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		if (container.getDataType() != DataType.BOOL) {
			fail("Incorrect data type");
		}
		((DataContainer<boolean[]>)refContainer).refer((DataContainer<boolean[]>)container);
		if (refContainer.getDataType() != DataType.BOOL) {
			fail("Incorrect data type");
		}

		// String[] のデータを持たせるとSTRING型になる事を検査
		((DataContainer<String[]>)container).setArrayData(new String[]{ "Hello" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		if (container.getDataType() != DataType.STRING) {
			fail("Incorrect data type");
		}
		((DataContainer<String[]>)refContainer).refer((DataContainer<String[]>)container);
		if (refContainer.getDataType() != DataType.STRING) {
			fail("Incorrect data type");
		}

		// null を渡してデータ未格納状態に戻すとVOIDに戻る事を検査
		((DataContainer<Object>)container).setArrayData(null, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		if (container.getDataType() != DataType.VOID) {
			fail("Incorrect data type");
		}
		((DataContainer<Object>)refContainer).refer((DataContainer<Object>)container);
		if (refContainer.getDataType() != DataType.VOID) {
			fail("Incorrect data type");
		}
	}

}
