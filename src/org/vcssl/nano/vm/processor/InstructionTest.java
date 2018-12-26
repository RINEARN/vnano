/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.processor;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.Memory;

public class InstructionTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {

		Instruction instruction = new Instruction(
			OperationCode.CAST,
			new DataType[]{ DataType.INT64, DataType.FLOAT64 },
			new Memory.Partition[]{ Memory.Partition.REGISTER, Memory.Partition.LOCAL, Memory.Partition.GLOBAL },
			new int[]{ 123, 456, 789 },
			Memory.Partition.CONSTANT,
			888
		);

		if (instruction.getOperationCode() != OperationCode.CAST) {
			fail("Incorrect operation code");
		}

		DataType[] types = instruction.getDataTypes();
		if (types.length != 2 || types[0] != DataType.INT64 || types[1] != DataType.FLOAT64) {
			fail("Incorrect data types");
		}

		Memory.Partition[] partitions = instruction.getOperandPartitions();
		if (partitions.length != 3 || partitions[0] != Memory.Partition.REGISTER
				|| partitions[1] != Memory.Partition.LOCAL || partitions[2] != Memory.Partition.GLOBAL) {

			fail("Incorrect operand partitions");
		}

		int[] addresses = instruction.getOperandAddresses();
		if (addresses.length != 3 || addresses[0] != 123 || addresses[1] != 456 || addresses[2] != 789) {

			fail("Incorrect operand addresses");
		}

		if (instruction.getMetaPartition() != Memory.Partition.CONSTANT) {
			fail("Incorrect meta partition");
		}

		if (instruction.getMetaAddress() != 888) {
			fail("Incorrect meta address");
		}

		if (!instruction.toString().equals("[ CAST\tINT64:FLOAT64\tR123\tL456\tG789\tC888 ]")) {
			fail("Incorrect string");
		}
	}
}
