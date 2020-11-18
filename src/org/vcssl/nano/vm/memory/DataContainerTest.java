/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.memory;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
		this.testSetGetData();
		this.testGetOffset();
		this.testGetLengths();
		this.testGetRank();
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

	private void testSetGetData() {
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

	private void testGetOffset() {
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

	private void testGetLengths() {
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

	private void testGetRank() {
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
