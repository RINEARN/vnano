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
		if (container.getData() != null) {
			fail("Incorrect data");
		}

		// デフォルトではスカラなのでサイズ1
		if (container.getSize() != 1) {
			fail("Incorrect size");
		}

		// デフォルトではスカラなので0次元
		if (container.getRank() != 0
				|| container.getLengths().length != 0) {

			fail("Incorrect rank");
		}
		// オフセットは配列の要素参照でしか使用しないのでデフォルトは0
		if (container.getOffset() != 0) {
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
		container.setData(new long[4], 0, lengths);

		// 初期化
		container.initialize();

		// デフォルトに戻っているか検査
		this.testDefaultState(container);

		// オフセット値を変える場合も試す
		container = new DataContainer<long[]>();
		int offset = 1;
		container.setData(new long[4], offset, DataContainer.SCALAR_LENGTHS);

		// 初期化
		container.initialize();

		// デフォルトに戻っているか検査
		this.testDefaultState(container);
	}

	private void testSetGetData() {
		DataContainer<long[]> container = new DataContainer<long[]>();
		long[] data = new long[]{1L, 2L, 3L};
		container.setData(data, 0, new int[] {3});
		if (container.getData() != data) {
			fail("Incorrect data");
		}

		// 参照リンク経由での読み込みテスト
		DataContainer<long[]> refContainer = new DataContainer<long[]>();
		refContainer.refer(container);
		if (refContainer.getData() != data) {
			fail("Incorrect data");
		}

		// 参照リンク経由での書き込みテスト
		long[] newData = new long[]{4L, 5L, 6L};
		refContainer.setData(newData, 0, new int[] {3});
		if (refContainer.getData() != newData) {
			fail("Incorrect data");
		}
		if (container.getData() != newData) {
			fail("Incorrect data");
		}
	}

	private void testGetOffset() {
		DataContainer<long[]> container = new DataContainer<long[]>();
		int offset = 3;
		container.setData(new long[5], offset, DataContainer.SCALAR_LENGTHS);
		if (container.getOffset() != offset) {
			fail("Incorrect offset");
		}

		// 参照リンク経由での読み込みテスト
		DataContainer<long[]> refContainer = new DataContainer<long[]>();
		refContainer.refer(container);
		if (refContainer.getOffset() != offset) {
			fail("Incorrect offset");
		}

		// 参照リンク経由での書き込みテスト
		refContainer.setData(new long[5], 4, DataContainer.SCALAR_LENGTHS);
		int newOffset = 4;
		if (refContainer.getOffset() != newOffset) {
			fail("Incorrect offset");
		}
		if (container.getOffset() != newOffset) {
			fail("Incorrect offset");
		}
	}

	private void testGetLengths() {
		int[] lengths = new int[] {1, 2, 3};
		DataContainer<long[]> container = new DataContainer<long[]>();
		container.setData(new long[] {1l, 2l, 3l, 4l, 5l, 6l}, 0, lengths);
		if (container.getLengths().length != 3
				|| container.getLengths()[0] != 1
				|| container.getLengths()[1] != 2
				|| container.getLengths()[2] != 3 ) {

			fail("Incorrect lengths");
		}

		// 参照リンク経由での読み込みテスト
		DataContainer<long[]> refContainer = new DataContainer<long[]>();
		refContainer.refer(container);
		if (refContainer.getLengths() != lengths) {
			fail("Incorrect lengths");
		}

		// 参照リンク経由での書き込みテスト
		int[] newLengths = new int[] {3, 2, 1};
		refContainer.setData(new long[] {1l, 2l, 3l, 4l, 5l, 6l}, 0, newLengths);
		if (refContainer.getLengths() != newLengths) {
			fail("Incorrect lengths");
		}
		if (container.getLengths() != newLengths) {
			fail("Incorrect lengths");
		}
	}

	private void testGetRank() {
		int[] lengths = new int[]{1, 2, 3};
		int rank = lengths.length;
		DataContainer<long[]> container = new DataContainer<long[]>();
		container.setData(new long[] {1l, 2l, 3l, 4l, 5l, 6l}, 0, lengths);
		if (container.getRank() != rank) {
			fail("Incorrect rank");
		}

		// 参照リンク経由での読み込みテスト
		DataContainer<long[]> refContainer = new DataContainer<long[]>();
		refContainer.refer(container);
		if (refContainer.getRank() != rank) {
			fail("Incorrect rank");
		}

		// 参照リンク経由での書き込みテスト
		int[] newLengths = new int[] {1, 2, 3, 1};
		int newRank = newLengths.length;
		refContainer.setData(new long[] {1l, 2l, 3l, 4l, 5l, 6l}, 0, newLengths);
		if (refContainer.getRank() != newRank) {
			fail("Incorrect rank");
		}
		if (container.getRank() != newRank) {
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
		((DataContainer<long[]>)container).setData(new long[]{ 1L }, 0, DataContainer.SCALAR_LENGTHS);
		if (container.getDataType() != DataType.INT64) {
			fail("Incorrect data type");
		}
		((DataContainer<long[]>)refContainer).refer((DataContainer<long[]>)container);
		if (refContainer.getDataType() != DataType.INT64) {
			fail("Incorrect data type");
		}

		// double[] のデータを持たせるとFLOAT64型になる事を検査
		((DataContainer<double[]>)container).setData(new double[]{ 1.0 }, 0, DataContainer.SCALAR_LENGTHS);
		if (container.getDataType() != DataType.FLOAT64) {
			fail("Incorrect data type");
		}
		((DataContainer<double[]>)refContainer).refer((DataContainer<double[]>)container);
		if (refContainer.getDataType() != DataType.FLOAT64) {
			fail("Incorrect data type");
		}

		// boolean[] のデータを持たせるとBOOL型になる事を検査
		((DataContainer<boolean[]>)container).setData(new boolean[]{ true }, 0, DataContainer.SCALAR_LENGTHS);
		if (container.getDataType() != DataType.BOOL) {
			fail("Incorrect data type");
		}
		((DataContainer<boolean[]>)refContainer).refer((DataContainer<boolean[]>)container);
		if (refContainer.getDataType() != DataType.BOOL) {
			fail("Incorrect data type");
		}

		// String[] のデータを持たせるとSTRING型になる事を検査
		((DataContainer<String[]>)container).setData(new String[]{ "Hello" }, 0, DataContainer.SCALAR_LENGTHS);
		if (container.getDataType() != DataType.STRING) {
			fail("Incorrect data type");
		}
		((DataContainer<String[]>)refContainer).refer((DataContainer<String[]>)container);
		if (refContainer.getDataType() != DataType.STRING) {
			fail("Incorrect data type");
		}

		// null を渡してデータ未格納状態に戻すとVOIDに戻る事を検査
		((DataContainer<Object>)container).setData(null, 0, DataContainer.SCALAR_LENGTHS);
		if (container.getDataType() != DataType.VOID) {
			fail("Incorrect data type");
		}
		((DataContainer<Object>)refContainer).refer((DataContainer<Object>)container);
		if (refContainer.getDataType() != DataType.VOID) {
			fail("Incorrect data type");
		}
	}

}
