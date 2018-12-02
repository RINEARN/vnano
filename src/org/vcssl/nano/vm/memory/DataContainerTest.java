/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.memory;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vcssl.nano.lang.DataType;

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
		this.testSetGetData();
		this.testSetGetOffset();
		this.testSetGetLengths();
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
		container.setSize(5);
		container.setLengths(new int[]{ 5 });
		container.setOffset(1); // 本来ベクトルは常にオフセット0であるが初期化検証のため設定
		container.setData(new long[4]);

		// 初期化
		container.initialize();

		// デフォルトに戻っているか検査
		this.testDefaultState(container);
	}

	private void testSetGetData() {

		DataContainer<long[]> container = new DataContainer<long[]>();

		long[] data = new long[]{1L, 2L, 3L};
		container.setData(data);
		if (container.getData() != data) {
			fail("Incorrect data");
		}

		// ここで未対応のデータを設定してエラーが出る事を確認すべき？
	}

	private void testSetGetOffset() {
		DataContainer<long[]> container = new DataContainer<long[]>();
		container.setOffset(3);
		if (container.getOffset() != 3) {
			fail("Incorrect value");
		}
	}

	private void testSetGetLengths() {
		int[] lengths = new int[]{1, 2, 3};
		DataContainer<long[]> container = new DataContainer<long[]>();
		container.setLengths(lengths);
		if (container.getLengths().length != 3
				|| container.getLengths()[0] != 1
				|| container.getLengths()[1] != 2
				|| container.getLengths()[2] != 3 ) {

			fail("Incorrect value");
		}
	}

	private void testGetRank() {
		int[] lengths = new int[]{1, 2, 3};
		DataContainer<long[]> container = new DataContainer<long[]>();
		container.setLengths(lengths);
		if (container.getRank() != 3) {
			fail("Incorrect value");
		}
	}

	@SuppressWarnings("unchecked")
	private void testGetDataType() {
		DataContainer<?> container = new DataContainer<Object>();

		// データ未格納の時点での型はVOIDである事を検査
		if (container.getDataType() != DataType.VOID) {
			fail("Incorrect data type");
		}

		// long[] のデータを持たせるとINT64型になる事を検査
		((DataContainer<long[]>)container).setData(new long[]{ 1L });
		if (container.getDataType() != DataType.INT64) {
			fail("Incorrect data type");
		}

		// double[] のデータを持たせるとFLOAT64型になる事を検査
		((DataContainer<double[]>)container).setData(new double[]{ 1.0 });
		if (container.getDataType() != DataType.FLOAT64) {
			fail("Incorrect data type");
		}

		// boolean[] のデータを持たせるとBOOL型になる事を検査
		((DataContainer<boolean[]>)container).setData(new boolean[]{ true });
		if (container.getDataType() != DataType.BOOL) {
			fail("Incorrect data type");
		}

		// String[] のデータを持たせるとSTRING型になる事を検査
		((DataContainer<String[]>)container).setData(new String[]{ "Hello" });
		if (container.getDataType() != DataType.STRING) {
			fail("Incorrect data type");
		}

		// null を渡してデータ未格納状態に戻すとVOIDに戻る事を検査
		((DataContainer<Object>)container).setData(null);
		if (container.getDataType() != DataType.VOID) {
			fail("Incorrect data type");
		}
	}

}
