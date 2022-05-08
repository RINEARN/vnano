/*
 * Copyright(C) 2017-2021 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.memory;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vcssl.nano.VnanoFatalException;

public class MemoryTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		this.testGetSize(Memory.Partition.REGISTER);
		this.testGetSize(Memory.Partition.LOCAL);
		this.testGetSize(Memory.Partition.GLOBAL);
		this.testGetSize(Memory.Partition.CONSTANT);

		this.testSetGetDataContainer(Memory.Partition.REGISTER);
		this.testSetGetDataContainer(Memory.Partition.LOCAL);
		this.testSetGetDataContainer(Memory.Partition.GLOBAL);
		this.testSetGetDataContainer(Memory.Partition.CONSTANT);

		this.testSetGetDataContainers(Memory.Partition.REGISTER);
		this.testSetGetDataContainers(Memory.Partition.LOCAL);
		this.testSetGetDataContainers(Memory.Partition.GLOBAL);
		this.testSetGetDataContainers(Memory.Partition.CONSTANT);
	}


	private void testGetSize(Memory.Partition partition) {
		Memory memory = new Memory();

		// 何も保持していない時点でのサイズを検査
		if (memory.getSize(partition) != 0) {
			fail("Incorrect value");
		}

		// データを保持している状態でのサイズを検査
		memory.setDataContainer(partition, 255, new DataContainer<long[]>());
		if (memory.getSize(partition) != 256) { // 全データ中の最大アドレス+1 がサイズのはず
			fail("Incorrect value");
		}

		// サイズよりも小さいアドレスにデータを置いた時点でのサイズを検査
		memory.setDataContainer(partition, 100, new DataContainer<long[]>());
		if (memory.getSize(partition) != 256) { // 最大アドレスは変わっていないのでサイズも不変のはず
			fail("Incorrect value");
		}

		// サイズを超えるアドレスにデータを置いた時点でのサイズを検査
		memory.setDataContainer(partition, 1000, new DataContainer<long[]>());
		if (memory.getSize(partition) != 1001) { // 最大アドレスが更新されたのでサイズが拡張されているはず
			fail("Incorrect value");
		}
	}

	private void testSetGetDataContainer(Memory.Partition partition) {

		Memory memory = new Memory();
		DataContainer<long[]> containerA = new DataContainer<long[]>();
		DataContainer<long[]> containerB = new DataContainer<long[]>();
		DataContainer<long[]> containerC = new DataContainer<long[]>();
		DataContainer<long[]> containerD = new DataContainer<long[]>();

		// set 直後に get し、同じデータコンテナかどうかを検査（このアドレスが暫定的な最大アドレスになる）
		memory.setDataContainer(partition, 8, containerA);
		try {
			if (containerA != memory.getDataContainer(partition, 8)) {
				fail("Incorrect container");
			}
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 最大アドレスよりも小さいアドレスに対して set / get 検査
		memory.setDataContainer(partition, 2, containerB);
		try {
			if (containerB != memory.getDataContainer(partition, 2)) {
				fail("Incorrect container");
			}
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 最大アドレスよりも大きなアドレスに対して set / get 検査（最大アドレスは更新される）
		memory.setDataContainer(partition, 1000, containerC);
		try {
			if (containerC != memory.getDataContainer(partition, 1000)) {
				fail("Incorrect container");
			}
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 最大アドレス+1 のアドレスに対して set / get 検査（内部処理が異なる）
		memory.setDataContainer(partition, 1001, containerD);
		try {
			if (containerD != memory.getDataContainer(partition, 1001)) {
				fail("Incorrect container");
			}
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// データの追加によって、それまでに追加したデータが飛んだり移動したりしてないか検証
		try {
			if (containerA != memory.getDataContainer(partition, 8)
					|| containerB != memory.getDataContainer(partition, 2)
					|| containerC != memory.getDataContainer(partition, 1000) ) {

				fail("Incorrect container");
			}
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// 最大アドレスよりも大きいアドレスに対して、set せずに get のみ行う検査
		try {
			@SuppressWarnings("unused")
			DataContainer<?> c = memory.getDataContainer(partition, 2000);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}

	private void testSetGetDataContainers(Memory.Partition partition) {

		DataContainer<?>[] beforeContainers = new DataContainer<?>[5];
		beforeContainers[0] = new DataContainer<long[]>();
		beforeContainers[1] = new DataContainer<long[]>();
		beforeContainers[2] = new DataContainer<long[]>();
		beforeContainers[3] = new DataContainer<long[]>();
		beforeContainers[4] = new DataContainer<long[]>();

		Memory memory = new Memory();
		memory.setDataContainers(partition, beforeContainers);

		DataContainer<?>[] afterContainers = memory.getDataContainers(partition);

		if (beforeContainers[0] != afterContainers[0]
				|| beforeContainers[1] != afterContainers[1]
				|| beforeContainers[2] != afterContainers[2]
				|| beforeContainers[3] != afterContainers[3]
				|| beforeContainers[4] != afterContainers[4] ) {

			fail("Incorrect container");
		}
	}

}
