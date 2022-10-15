/*
 * Copyright(C) 2017-2022 RINEARN
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

/**
 * The test of DataContainer class.
 */
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

		// A data-container doesn't have any data by default.
		if (container.getArrayData() != null) {
			fail("Incorrect data");
		}

		// It represents a scala by default, so its size should be 1.
		if (container.getArraySize() != 1) {
			fail("Incorrect size");
		}

		// It represents a scala by default, so its array-rank should be 1.
		if (container.getArrayRank() != 0
				|| container.getArrayLengths().length != 0) {

			fail("Incorrect rank");
		}

		// The default value of the offset should be 0.
		if (container.getArrayOffset() != 0) {
			fail("Incorrect offset");
		}

		// The default data-type is VOID because the container stored no data yet.
		if (container.getDataType() != DataType.VOID) {
			fail("Incorrect data type");
		}
	}

	private void testInitialize() {
		DataContainer<long[]> container = new DataContainer<long[]>();

		// Change the state from the default.
		int lengths[] = new int[] { 5 };
		container.setArrayData(new long[4], 0, lengths);

		// Re-initialize.
		container.initialize();

		// Check that the state is re-initialized to the default state.
		this.testDefaultState(container);

		// Re-test with changing the offset value.
		// オフセット値を変える場合も試す
		container = new DataContainer<long[]>();
		int offset = 1;
		container.setArrayData(new long[4], offset, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		container.initialize();
		this.testDefaultState(container);
	}

	private void testSetGetArrayData() {
		DataContainer<long[]> container = new DataContainer<long[]>();
		long[] data = new long[]{1L, 2L, 3L};
		container.setArrayData(data, 0, new int[] {3});
		if (container.getArrayData() != data) {
			fail("Incorrect data");
		}

		// Reading the data through the reference.
		DataContainer<long[]> refContainer = new DataContainer<long[]>();
		refContainer.refer(container);
		if (refContainer.getArrayData() != data) {
			fail("Incorrect data");
		}

		// Writing the data through the reference.
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

		// Reading the offset value through the reference.
		DataContainer<long[]> refContainer = new DataContainer<long[]>();
		refContainer.refer(container);
		if (refContainer.getArrayOffset() != offset) {
			fail("Incorrect offset");
		}

		// Writing the offset value through the reference.
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

		// Reading the array-lengths through the reference.
		DataContainer<long[]> refContainer = new DataContainer<long[]>();
		refContainer.refer(container);
		if (refContainer.getArrayLengths() != lengths) {
			fail("Incorrect lengths");
		}

		// Writing the array-lengths through the reference.
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

		// Reading the array-rank through the reference.
		DataContainer<long[]> refContainer = new DataContainer<long[]>();
		refContainer.refer(container);
		if (refContainer.getArrayRank() != rank) {
			fail("Incorrect rank");
		}

		// Writing the array-rank through the reference.
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

		// Try to read data when no data is stored.
		try {
			container.getFloat64ScalarData();
			fail("Expected exception did not occurred");
		} catch (VnanoFatalException e) {
			// We expect that the exception occurs.
		}

		// Store and read a scalar data.
		container.setFloat64ScalarData(1.25);
		assertTrue(1.25 == container.getFloat64ScalarData()); // 1.25 is divisible in the binary representation, so we can use == operator.
		container.setFloat64ScalarData(2.5);
		assertTrue(2.5 == container.getFloat64ScalarData()); // See the above.

		// Check the array-rank and lengths.
		assertEquals(DataContainer.ARRAY_RANK_OF_SCALAR, container.getArrayRank());
		assertTrue(Arrays.equals(DataContainer.ARRAY_LENGTHS_OF_SCALAR, container.getArrayLengths()));
		assertEquals(0, container.getArrayOffset());

		// Re-test with initializing the data-container by a incompatible data-type.
		DataContainer<long[]> container2 = new DataContainer<long[]>();
		container2.setInt64ScalarData(123);
		try {
			container2.setFloat64ScalarData(1.25);
			fail("Expected exception did not occurred");
		} catch (VnanoFatalException e) {
			// We expect that the exception occurs.
		}
	}

	private void testSetGetInt64ScalarData() {
		DataContainer<long[]> container = new DataContainer<long[]>();

		// Try to read data when no data is stored.
		try {
			container.getFloat64ScalarData();
			fail("Expected exception did not occurred");
		} catch (VnanoFatalException e) {
			// We expect that the exception occurs.
		}

		// Store and read a scalar data.
		container.setInt64ScalarData(123);
		assertEquals(123, container.getInt64ScalarData());
		container.setInt64ScalarData(456);
		assertEquals(456, container.getInt64ScalarData());

		// Check the array-rank and lengths.
		assertEquals(DataContainer.ARRAY_RANK_OF_SCALAR, container.getArrayRank());
		assertTrue(Arrays.equals(DataContainer.ARRAY_LENGTHS_OF_SCALAR, container.getArrayLengths()));
		assertEquals(0, container.getArrayOffset());

		// Re-test with initializing the data-container by a incompatible data-type.
		DataContainer<double[]> container2 = new DataContainer<double[]>();
		container2.setFloat64ScalarData(1.25);
		try {
			container2.setInt64ScalarData(123);
			fail("Expected exception did not occurred");
		} catch (VnanoFatalException e) {
			// We expect that the exception occurs.
		}
	}

	private void testSetGetBoolScalarData() {
		DataContainer<boolean[]> container = new DataContainer<boolean[]>();

		// Try to read data when no data is stored.
		try {
			container.getFloat64ScalarData();
			fail("Expected exception did not occurred");
		} catch (VnanoFatalException e) {
			// We expect that the exception occurs.
		}

		// Store and read a scalar data.
		container.setBoolScalarData(true);
		assertTrue(container.getBoolScalarData());
		container.setBoolScalarData(false);
		assertFalse(container.getBoolScalarData());

		// Check the array-rank and lengths.
		assertEquals(DataContainer.ARRAY_RANK_OF_SCALAR, container.getArrayRank());
		assertTrue(Arrays.equals(DataContainer.ARRAY_LENGTHS_OF_SCALAR, container.getArrayLengths()));
		assertEquals(0, container.getArrayOffset());

		// Re-test with initializing the data-container by a incompatible data-type.
		DataContainer<long[]> container2 = new DataContainer<long[]>();
		container2.setFloat64ScalarData(123);
		try {
			container2.setBoolScalarData(true);
			fail("Expected exception did not occurred");
		} catch (VnanoFatalException e) {
			// We expect that the exception occurs.
		}
	}

	private void testSetGetStringScalarData() {
		DataContainer<String[]> container = new DataContainer<String[]>();

		// Try to read data when no data is stored.
		try {
			container.getStringScalarData();
			fail("Expected exception did not occurred");
		} catch (VnanoFatalException e) {
			// We expect that the exception occurs.
		}

		// Store and read a scalar data.
		container.setStringScalarData("aiueo");
		assertEquals("aiueo", container.getStringScalarData());
		container.setStringScalarData("kakikukeko");
		assertEquals("kakikukeko", container.getStringScalarData());

		// Check the array-rank and lengths.
		assertEquals(DataContainer.ARRAY_RANK_OF_SCALAR, container.getArrayRank());
		assertTrue(Arrays.equals(DataContainer.ARRAY_LENGTHS_OF_SCALAR, container.getArrayLengths()));
		assertEquals(0, container.getArrayOffset());

		// Re-test with initializing the data-container by a incompatible data-type.
		DataContainer<long[]> container2 = new DataContainer<long[]>();
		container2.setFloat64ScalarData(123);
		try {
			container2.setStringScalarData("aiueo");
			fail("Expected exception did not occurred");
		} catch (VnanoFatalException e) {
			// We expect that the exception occurs.
		}
	}

	@SuppressWarnings("unchecked")
	private void testGetDataType() {
		DataContainer<?> container = new DataContainer<Object>();
		DataContainer<?> refContainer = new DataContainer<Object>();

		// Check that the data-type is VOID when no data is stored.
		if (container.getDataType() != DataType.VOID) {
			fail("Incorrect data type");
		}
		((DataContainer<Object>)refContainer).refer((DataContainer<Object>)container);
		if (refContainer.getDataType() != DataType.VOID) {
			fail("Incorrect data type");
		}

		// Check that the data-type is INT64 when the container stores long[] type (serialized) data.
		((DataContainer<long[]>)container).setArrayData(new long[]{ 1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		if (container.getDataType() != DataType.INT64) {
			fail("Incorrect data type");
		}
		((DataContainer<long[]>)refContainer).refer((DataContainer<long[]>)container);
		if (refContainer.getDataType() != DataType.INT64) {
			fail("Incorrect data type");
		}

		// Check that the data-type is FLOAT64 when the container stores double[] type (serialized) data.
		((DataContainer<double[]>)container).setArrayData(new double[]{ 1.0 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		if (container.getDataType() != DataType.FLOAT64) {
			fail("Incorrect data type");
		}
		((DataContainer<double[]>)refContainer).refer((DataContainer<double[]>)container);
		if (refContainer.getDataType() != DataType.FLOAT64) {
			fail("Incorrect data type");
		}

		// Check that the data-type is BOOL when the container stores boolean[] type (serialized) data.
		((DataContainer<boolean[]>)container).setArrayData(new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		if (container.getDataType() != DataType.BOOL) {
			fail("Incorrect data type");
		}
		((DataContainer<boolean[]>)refContainer).refer((DataContainer<boolean[]>)container);
		if (refContainer.getDataType() != DataType.BOOL) {
			fail("Incorrect data type");
		}

		// Check that the data-type is STRING when the container stores String[] type (serialized) data.
		((DataContainer<String[]>)container).setArrayData(new String[]{ "Hello" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		if (container.getDataType() != DataType.STRING) {
			fail("Incorrect data type");
		}
		((DataContainer<String[]>)refContainer).refer((DataContainer<String[]>)container);
		if (refContainer.getDataType() != DataType.STRING) {
			fail("Incorrect data type");
		}

		// Check that the data-type turns to VOID when we set null as the (serialized) data.
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
