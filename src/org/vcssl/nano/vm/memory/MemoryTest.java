/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.memory;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vcssl.nano.VnanoFatalException;


/**
 * The test of Memory class.
 */
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

		// Check the size when storing no data-container.
		if (memory.getSize(partition) != 0) {
			fail("Incorrect value");
		}

		// Check the size when storing some data-containers.
		memory.setDataContainer(partition, 255, new DataContainer<long[]>());
		if (memory.getSize(partition) != 256) {  // The current size should be the maximum address (255) + 1.
			fail("Incorrect value");
		}

		// Check the size after when we put a data-container at the address less than the current maximum address.
		memory.setDataContainer(partition, 100, new DataContainer<long[]>());
		if (memory.getSize(partition) != 256) {  // The size should not change for this case.
			fail("Incorrect value");
		}

		// Check the size after when we put a data-container at the address greater than the current maximum address.
		memory.setDataContainer(partition, 1000, new DataContainer<long[]>());
		if (memory.getSize(partition) != 1001) { // The size should be expanded for this case.
			fail("Incorrect value");
		}
	}

	private void testSetGetDataContainer(Memory.Partition partition) {

		Memory memory = new Memory();
		DataContainer<long[]> containerA = new DataContainer<long[]>();
		DataContainer<long[]> containerB = new DataContainer<long[]>();
		DataContainer<long[]> containerC = new DataContainer<long[]>();
		DataContainer<long[]> containerD = new DataContainer<long[]>();

		// Check setting/getting a data-container.
		// (The maximum address will be expanded to 8 automatically.)
		memory.setDataContainer(partition, 8, containerA);
		try {
			if (containerA != memory.getDataContainer(partition, 8)) {
				fail("Incorrect container");
			}
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// Check setting/getting a data-container at the address less than the current maximum address.
		memory.setDataContainer(partition, 2, containerB);
		try {
			if (containerB != memory.getDataContainer(partition, 2)) {
				fail("Incorrect container");
			}
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// Check setting/getting a data-container at the address greater than the current maximum address.
		memory.setDataContainer(partition, 1000, containerC);
		try {
			if (containerC != memory.getDataContainer(partition, 1000)) {
				fail("Incorrect container");
			}
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// Check setting/getting a data-container at the current maximum address + 1.
		// (The internal process is little different from the previous case.)
		memory.setDataContainer(partition, 1001, containerD);
		try {
			if (containerD != memory.getDataContainer(partition, 1001)) {
				fail("Incorrect container");
			}
		} catch (VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occured");
		}

		// Check that the already stored data-containers are not moved/removed when we store a new data-container.
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

		// Check that we can not read data at the address exceeds the current maximum address.
		try {
			@SuppressWarnings("unused")
			DataContainer<?> c = memory.getDataContainer(partition, 2000);
			fail("Expected exception did not occured");
		} catch (VnanoFatalException e) {
			// We expect that the exception occurs.
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
