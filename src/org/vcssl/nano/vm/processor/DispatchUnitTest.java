/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.processor;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.spec.SpecialBindingKey;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.VnanoException;


/**
 * The test of DispatchUnit class.
 */
public class DispatchUnitTest {

	private Memory memory;

	private DataContainer<long[]> int64Output;
	private DataContainer<long[]> int64InputA;
	private DataContainer<long[]> int64InputB;

	private DataContainer<boolean[]> boolOutput;
	private DataContainer<boolean[]> boolInputA;
	private DataContainer<boolean[]> boolInputB;

	private static final int INT64_OUTPUT_ADDR = 1;
	private static final int INT64_INPUT_A_ADDR = 2;
	private static final int INT64_INPUT_B_ADDR = 3;

	private static final int BOOL_OUTPUT_ADDR = 101;
	private static final int BOOL_INPUT_A_ADDR = 102;
	private static final int BOOL_INPUT_B_ADDR = 103;

	private static final int TMP_A_ADDR = 1001;
	private static final int TMP_B_ADDR = 1002;
	private static final int TMP_C_ADDR = 1003;
	private static final int TMP_D_ADDR = 1004;

	private static final int META_ADDR = 0;

	private static final Memory.Partition INT64_OUTPUT_PART = Memory.Partition.REGISTER;
	private static final Memory.Partition INT64_INPUT_A_PART = Memory.Partition.REGISTER;
	private static final Memory.Partition INT64_INPUT_B_PART = Memory.Partition.REGISTER;

	private static final Memory.Partition BOOL_OUTPUT_PART = Memory.Partition.REGISTER;
	private static final Memory.Partition BOOL_INPUT_A_PART = Memory.Partition.REGISTER;
	private static final Memory.Partition BOOL_INPUT_B_PART = Memory.Partition.REGISTER;

	private static final Memory.Partition TMP_A_PART = Memory.Partition.REGISTER;
	private static final Memory.Partition TMP_B_PART = Memory.Partition.REGISTER;
	private static final Memory.Partition TMP_C_PART = Memory.Partition.REGISTER;
	private static final Memory.Partition TMP_D_PART = Memory.Partition.REGISTER;

	private static final Memory.Partition META_PART = Memory.Partition.CONSTANT;

	private static final DataType[] INT64_TYPE = new DataType[]{
			DataType.INT64
	};
	private static final DataType[] BOOL_TYPE = new DataType[]{
			DataType.BOOL
	};
	private static final DataType[] VOID_TYPE = new DataType[]{
			DataType.VOID
	};



	private static final int[] INT64X2_ADDRS = new int[]{
			INT64_OUTPUT_ADDR, INT64_INPUT_A_ADDR
	};
	private static final int[] INT64X3_ADDRS = new int[]{
			INT64_OUTPUT_ADDR, INT64_INPUT_A_ADDR, INT64_INPUT_B_ADDR
	};
	private static final int[] BOOLX1_INT64X2_ADDRS = new int[]{
			BOOL_OUTPUT_ADDR, INT64_INPUT_A_ADDR, INT64_INPUT_B_ADDR
	};
	private static final int[] BOOLX2_ADDRS = new int[]{
			BOOL_OUTPUT_ADDR, BOOL_INPUT_A_ADDR
	};
	private static final int[] BOOLX3_ADDRS = new int[]{
			BOOL_OUTPUT_ADDR, BOOL_INPUT_A_ADDR, BOOL_INPUT_B_ADDR
	};

	private static final Memory.Partition[] INT64X2_PARTS = new Memory.Partition[]{
			INT64_OUTPUT_PART, INT64_INPUT_A_PART
	};
	private static final Memory.Partition[] INT64X3_PARTS = new Memory.Partition[]{
			INT64_OUTPUT_PART, INT64_INPUT_A_PART, INT64_INPUT_B_PART
	};
	private static final Memory.Partition[] BOOLX1_INT64X2_PARTS = new Memory.Partition[]{
			BOOL_OUTPUT_PART, INT64_INPUT_A_PART, INT64_INPUT_B_PART
	};
	private static final Memory.Partition[] BOOLX2_PARTS = new Memory.Partition[]{
			BOOL_OUTPUT_PART, BOOL_INPUT_A_PART
	};
	private static final Memory.Partition[] BOOLX3_PARTS = new Memory.Partition[]{
			BOOL_OUTPUT_PART, BOOL_INPUT_A_PART, BOOL_INPUT_B_PART
	};

	private boolean connectedMethodCalled = false;

	@Before
	public void setUp() throws Exception {

		this.int64Output = new DataContainer<long[]>();
		this.int64InputA = new DataContainer<long[]>();
		this.int64InputB = new DataContainer<long[]>();
		this.int64Output.setArrayData(new long[3], 0, new int[]{ 3 });
		this.int64InputA.setArrayData(new long[3], 0, new int[]{ 3 });
		this.int64InputB.setArrayData(new long[3], 0, new int[]{ 3 });

		this.boolOutput = new DataContainer<boolean[]>();
		this.boolInputA = new DataContainer<boolean[]>();
		this.boolInputB = new DataContainer<boolean[]>();
		this.boolOutput.setArrayData(new boolean[3], 0, new int[]{ 3 });
		this.boolInputA.setArrayData(new boolean[3], 0, new int[]{ 3 });
		this.boolInputB.setArrayData(new boolean[3], 0, new int[]{ 3 });

		this.memory = new Memory();
		this.memory.setDataContainer(INT64_OUTPUT_PART, INT64_OUTPUT_ADDR, this.int64Output);
		this.memory.setDataContainer(INT64_INPUT_A_PART, INT64_INPUT_A_ADDR, this.int64InputA);
		this.memory.setDataContainer(INT64_INPUT_B_PART, INT64_INPUT_B_ADDR, this.int64InputB);
		this.memory.setDataContainer(BOOL_OUTPUT_PART, BOOL_OUTPUT_ADDR, this.boolOutput);
		this.memory.setDataContainer(BOOL_INPUT_A_PART, BOOL_INPUT_A_ADDR, this.boolInputA);
		this.memory.setDataContainer(BOOL_INPUT_B_PART, BOOL_INPUT_B_ADDR, this.boolInputB);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		this.testDispatchAdd();
		this.testDispatchSub();
		this.testDispatchMul();
		this.testDispatchDiv();
		this.testDispatchRem();
		this.testDispatchNeg();
		this.testDispatchEq();
		this.testDispatchNeq();
		this.testDispatchGeq();
		this.testDispatchLeq();
		this.testDispatchGt();
		this.testDispatchLt();
		this.testDispatchAnd();
		this.testDispatchOr();
		this.testDispatchNot();
		//this.testDispatchVec();
		this.testDispatchMov();
		this.testDispatchCast();
		this.testDispatchFill();
		this.testDispatchMovelm();
		this.testDispatchRefelm();
		//this.testDispatchLen();
		this.testDispatchFree();
		this.testDispatchJmp();
		this.testDispatchJmpn();
		this.testDispatchAllocScalar();
		this.testDispatchAllocArray();
		this.testCallx();
		this.testDispatchNop();
		this.testDispatchLabel();
	}

	private Instruction generateInt64x2Instruction(OperationCode operationCode) {
		return new Instruction(
				operationCode, INT64_TYPE, INT64X2_PARTS, INT64X2_ADDRS, META_PART, META_ADDR
		);
	}

	private Instruction generateInt64x3Instruction(OperationCode operationCode) {
		return new Instruction(
				operationCode, INT64_TYPE, INT64X3_PARTS, INT64X3_ADDRS, META_PART, META_ADDR
		);
	}

	private Instruction generateBoolx1Int64x2Instruction(OperationCode operationCode) {
		return new Instruction(
				operationCode, INT64_TYPE, BOOLX1_INT64X2_PARTS, BOOLX1_INT64X2_ADDRS, META_PART, META_ADDR
		);
	}

	private Instruction generateBoolx3Instruction(OperationCode operationCode) {
		return new Instruction(
				operationCode, BOOL_TYPE, BOOLX3_PARTS, BOOLX3_ADDRS, META_PART, META_ADDR
		);
	}
	private Instruction generateBoolx2Instruction(OperationCode operationCode) {
		return new Instruction(
				operationCode, BOOL_TYPE, BOOLX2_PARTS, BOOLX2_ADDRS, META_PART, META_ADDR
		);
	}

	private int dispatch(Instruction instruction, int pc) throws VnanoException, VnanoFatalException {
		return new DispatchUnit().dispatch(
				instruction, this.memory, new Interconnect(), new ExecutionUnit(), new boolean[] {false}, pc
		);
	}
	private int dispatch(Instruction instruction, Interconnect interconnect, int pc) throws VnanoException, VnanoFatalException {
		return new DispatchUnit().dispatch(
				instruction, this.memory, interconnect, new ExecutionUnit(), new boolean[] {false}, pc
		);
	}

	private void testDispatchAdd() {

		// Set values to operands.
		this.int64InputA.setArrayData(new long[]{ 1L, 2L, 3L }, 0, new int[] {3});
		this.int64InputB.setArrayData(new long[]{ 4L, 5L, 6L }, 0, new int[] {3});
		this.int64Output.setArrayData(new long[]{ -1L, -1L, -1L }, 0, new int[] {3});

		// Create an instruction.
		Instruction instruction = this.generateInt64x3Instruction(OperationCode.ADD);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Check the data of the output operand.
		long[] output = this.int64Output.getArrayData();
		if (output[0] != 5L || output[1] != 7L || output[2] != 9L) {
			fail("Incorrect output");
		}

		// Test of the behaviour when the number of operands is incorrect.
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.ADD);
			programCounter = this.dispatch(instruction, programCounter);
			fail("Expected exception has not occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// The exception should be thrown for this case.
		}
	}

	private void testDispatchSub() {

		// Set values to operands.
		this.int64InputA.setArrayData(new long[]{ 10L, 5L, 3L }, 0, new int[] {3});
		this.int64InputB.setArrayData(new long[]{ 2L, 5L, 7L }, 0, new int[] {3});
		this.int64Output.setArrayData(new long[]{ -1L, -1L, -1L }, 0, new int[] {3});

		// Create an instruction.
		Instruction instruction = this.generateInt64x3Instruction(OperationCode.SUB);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Check the data of the output operand.
		long[] output = this.int64Output.getArrayData();
		if (output[0] != 8L || output[1] != 0L || output[2] != -4L) {
			fail("Incorrect output");
		}

		// Test of the behaviour when the number of operands is incorrect.
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.SUB);
			programCounter = this.dispatch(instruction, programCounter);
			fail("Expected exception has not occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// The exception should be thrown for this case.
		}
	}

	private void testDispatchMul() {

		// Set values to operands.
		this.int64InputA.setArrayData(new long[]{ 1L, 2L, 3L }, 0, new int[] {3});
		this.int64InputB.setArrayData(new long[]{ 4L, 5L, 6L }, 0, new int[] {3});
		this.int64Output.setArrayData(new long[]{ -1L, -1L, -1L }, 0, new int[] {3});

		// Create an instruction.
		Instruction instruction = this.generateInt64x3Instruction(OperationCode.MUL);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Check the data of the output operand.
		long[] output = this.int64Output.getArrayData();
		if (output[0] != 4L || output[1] != 10L || output[2] != 18L) {
			fail("Incorrect output");
		}

		// Test of the behaviour when the number of operands is incorrect.
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.MUL);
			programCounter = this.dispatch(instruction, programCounter);
			fail("Expected exception has not occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// The exception should be thrown for this case.
		}
	}

	private void testDispatchDiv() {

		// Set values to operands.
		this.int64InputA.setArrayData(new long[]{ 10L, 11L, 100L }, 0, new int[] {3});
		this.int64InputB.setArrayData(new long[]{ 5L, 3L, 20L }, 0, new int[] {3});
		this.int64Output.setArrayData(new long[]{ -1L, -1L, -1L }, 0, new int[] {3});

		// Create an instruction.
		Instruction instruction = this.generateInt64x3Instruction(OperationCode.DIV);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Check the data of the output operand.
		long[] output = this.int64Output.getArrayData();
		if (output[0] != 2L || output[1] != 3L || output[2] != 5L) {
			fail("Incorrect output");
		}

		// Test of the behaviour when the number of operands is incorrect.
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.DIV);
			programCounter = this.dispatch(instruction, programCounter);
			fail("Expected exception has not occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// The exception should be thrown for this case.
		}
	}

	private void testDispatchRem() {

		// Set values to operands.
		this.int64InputA.setArrayData(new long[]{ 10L, 11L, 100L }, 0, new int[] {3});
		this.int64InputB.setArrayData(new long[]{ 5L, 3L, 20L }, 0, new int[] {3});
		this.int64Output.setArrayData(new long[]{ -1L, -1L, -1L }, 0, new int[] {3});

		// Create an instruction.
		Instruction instruction = this.generateInt64x3Instruction(OperationCode.REM);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Check the data of the output operand.
		long[] output = this.int64Output.getArrayData();
		if (output[0] != 0L || output[1] != 2L || output[2] != 0L) {
			fail("Incorrect output");
		}

		// Test of the behaviour when the number of operands is incorrect.
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.REM);
			programCounter = this.dispatch(instruction, programCounter);
			fail("Expected exception has not occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// The exception should be thrown for this case.
		}
	}

	private void testDispatchNeg() {

		// Set values to operands.
		this.int64InputA.setArrayData(new long[]{ 1L, -2L, 3L }, 0, new int[] {3});
		this.int64Output.setArrayData(new long[]{ -1L, -1L, -1L }, 0, new int[] {3});

		// Create an instruction.
		Instruction instruction = this.generateInt64x2Instruction(OperationCode.NEG);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Check the data of the output operand.
		long[] output = this.int64Output.getArrayData();
		if (output[0] != -1L || output[1] != 2L || output[2] != -3L) {
			fail("Incorrect output");
		}

		// Test of the behaviour when the number of operands is incorrect.
		try {
			instruction = this.generateInt64x3Instruction(OperationCode.NEG);
			programCounter = this.dispatch(instruction, programCounter);
			fail("Expected exception has not occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// The exception should be thrown for this case.
		}
	}

	private void testDispatchEq() {

		// Set values to operands.
		this.int64InputA.setArrayData(new long[]{ 200L, 500L, 700L }, 0, new int[] {3});
		this.int64InputB.setArrayData(new long[]{ 100L, 500L, 800L }, 0, new int[] {3});
		this.boolOutput.setArrayData(new boolean[]{ false, false, false }, 0, new int[] {3});

		// Create an instruction.
		Instruction instruction = this.generateBoolx1Int64x2Instruction(OperationCode.EQ);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Check the data of the output operand.
		boolean[] output = this.boolOutput.getArrayData();
		if (output[0] != false || output[1] != true || output[2] != false) {
			fail("Incorrect output");
		}

		// Test of the behaviour when the number of operands is incorrect.
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.EQ);
			programCounter = this.dispatch(instruction, programCounter);
			fail("Expected exception has not occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// The exception should be thrown for this case.
		}
	}

	private void testDispatchNeq() {

		// Set values to operands.
		this.int64InputA.setArrayData(new long[]{ 200L, 500L, 700L }, 0, new int[] {3});
		this.int64InputB.setArrayData(new long[]{ 100L, 500L, 800L }, 0, new int[] {3});
		this.boolOutput.setArrayData(new boolean[]{ false, false, false }, 0, new int[] {3});

		// Create an instruction.
		Instruction instruction = this.generateBoolx1Int64x2Instruction(OperationCode.NEQ);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Check the data of the output operand.
		boolean[] output = this.boolOutput.getArrayData();
		if (output[0] != true || output[1] != false || output[2] != true) {
			fail("Incorrect output");
		}

		// Test of the behaviour when the number of operands is incorrect.
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.NEQ);
			programCounter = this.dispatch(instruction, programCounter);
			fail("Expected exception has not occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// The exception should be thrown for this case.
		}
	}


	private void testDispatchGeq() {

		// Set values to operands.
		this.int64InputA.setArrayData(new long[]{ 200L, 500L, 700L }, 0, new int[] {3});
		this.int64InputB.setArrayData(new long[]{ 100L, 500L, 800L }, 0, new int[] {3});
		this.boolOutput.setArrayData(new boolean[]{ false, false, false }, 0, new int[] {3});

		// Create an instruction.
		Instruction instruction = this.generateBoolx1Int64x2Instruction(OperationCode.GEQ);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Check the data of the output operand.
		boolean[] output = this.boolOutput.getArrayData();
		if (output[0] != true || output[1] != true || output[2] != false) {
			fail("Incorrect output");
		}

		// Test of the behaviour when the number of operands is incorrect.
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.GEQ);
			programCounter = this.dispatch(instruction, programCounter);
			fail("Expected exception has not occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// The exception should be thrown for this case.
		}
	}


	private void testDispatchLeq() {

		// Set values to operands.
		this.int64InputA.setArrayData(new long[]{ 200L, 500L, 700L }, 0, new int[] {3});
		this.int64InputB.setArrayData(new long[]{ 100L, 500L, 800L }, 0, new int[] {3});
		this.boolOutput.setArrayData(new boolean[]{ false, false, false }, 0, new int[] {3});

		// Create an instruction.
		Instruction instruction = this.generateBoolx1Int64x2Instruction(OperationCode.LEQ);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Check the data of the output operand.
		boolean[] output = this.boolOutput.getArrayData();
		if (output[0] != false || output[1] != true || output[2] != true) {
			fail("Incorrect output");
		}

		// Test of the behaviour when the number of operands is incorrect.
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.LEQ);
			programCounter = this.dispatch(instruction, programCounter);
			fail("Expected exception has not occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// The exception should be thrown for this case.
		}
	}


	private void testDispatchGt() {

		// Set values to operands.
		this.int64InputA.setArrayData(new long[]{ 200L, 500L, 700L }, 0, new int[] {3});
		this.int64InputB.setArrayData(new long[]{ 100L, 500L, 800L }, 0, new int[] {3});
		this.boolOutput.setArrayData(new boolean[]{ false, false, false }, 0, new int[] {3});

		// Create an instruction.
		Instruction instruction = this.generateBoolx1Int64x2Instruction(OperationCode.GT);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Check the data of the output operand.
		boolean[] output = this.boolOutput.getArrayData();
		if (output[0] != true || output[1] != false || output[2] != false) {
			fail("Incorrect output");
		}

		// Test of the behaviour when the number of operands is incorrect.
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.GT);
			programCounter = this.dispatch(instruction, programCounter);
			fail("Expected exception has not occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// The exception should be thrown for this case.
		}
	}


	private void testDispatchLt() {

		// Set values to operands.
		this.int64InputA.setArrayData(new long[]{ 200L, 500L, 700L }, 0, new int[] {3});
		this.int64InputB.setArrayData(new long[]{ 100L, 500L, 800L }, 0, new int[] {3});
		this.boolOutput.setArrayData(new boolean[]{ false, false, false }, 0, new int[] {3});

		// Create an instruction.
		Instruction instruction = this.generateBoolx1Int64x2Instruction(OperationCode.LT);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Check the data of the output operand.
		boolean[] output = this.boolOutput.getArrayData();
		if (output[0] != false || output[1] != false || output[2] != true) {
			fail("Incorrect output");
		}

		// Test of the behaviour when the number of operands is incorrect.
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.LT);
			programCounter = this.dispatch(instruction, programCounter);
			fail("Expected exception has not occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// The exception should be thrown for this case.
		}
	}


	private void testDispatchAnd() {

		// Set values to operands.
		this.boolInputA.setArrayData(new boolean[]{ false, true,  true }, 0, new int[] {3});
		this.boolInputB.setArrayData(new boolean[]{ false, false, true }, 0, new int[] {3});
		this.boolOutput.setArrayData(new boolean[]{ false, false, false }, 0, new int[] {3});

		// Create an instruction.
		Instruction instruction = this.generateBoolx3Instruction(OperationCode.ANDM);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Check the data of the output operand.
		boolean[] output = this.boolOutput.getArrayData();
		if (output[0] != false || output[1] != false || output[2] != true) {
			fail("Incorrect output");
		}

		// Test of the behaviour when the number of operands is incorrect.
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.ANDM);
			programCounter = this.dispatch(instruction, programCounter);
			fail("Expected exception has not occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// The exception should be thrown for this case.
		}
	}

	private void testDispatchOr() {

		// Set values to operands.
		this.boolInputA.setArrayData(new boolean[]{ false, true,  true }, 0, new int[] {3});
		this.boolInputB.setArrayData(new boolean[]{ false, false, true }, 0, new int[] {3});
		this.boolOutput.setArrayData(new boolean[]{ false, false, false }, 0, new int[] {3});

		// Create an instruction.
		Instruction instruction = this.generateBoolx3Instruction(OperationCode.ORM);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Check the data of the output operand.
		boolean[] output = this.boolOutput.getArrayData();
		if (output[0] != false || output[1] != true || output[2] != true) {
			fail("Incorrect output");
		}

		// Test of the behaviour when the number of operands is incorrect.
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.ORM);
			programCounter = this.dispatch(instruction, programCounter);
			fail("Expected exception has not occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// The exception should be thrown for this case.
		}
	}

	private void testDispatchNot() {

		// Set values to operands.
		this.boolInputA.setArrayData(new boolean[]{ false, true,  false }, 0, new int[] {3});
		this.boolOutput.setArrayData(new boolean[]{ false, false, false }, 0, new int[] {3});

		// Create an instruction.
		Instruction instruction = this.generateBoolx2Instruction(OperationCode.NOT);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Check the data of the output operand.
		boolean[] output = this.boolOutput.getArrayData();
		if (output[0] != true || output[1] != false || output[2] != true) {
			fail("Incorrect output");
		}

		// Test of the behaviour when the number of operands is incorrect.
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.NOT);
			programCounter = this.dispatch(instruction, programCounter);
			fail("Expected exception has not occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// The exception should be thrown for this case.
		}
	}

	private void testDispatchMov() {

		// Set values to operands.
		this.int64InputA.setArrayData(new long[]{ 11L, 22L, 33L }, 0, new int[] {3});
		this.int64Output.setArrayData(new long[]{ -1L, -1L, -1L }, 0, new int[] {3});

		// Create an instruction.
		Instruction instruction = this.generateInt64x2Instruction(OperationCode.MOV);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Check the data of the output operand.
		long[] output = this.int64Output.getArrayData();
		if (output[0] != 11L || output[1] != 22L || output[2] != 33L) {
			fail("Incorrect output");
		}

		// Test of the behaviour when the number of operands is incorrect.
		try {
			instruction = this.generateInt64x3Instruction(OperationCode.MOV);
			programCounter = this.dispatch(instruction, programCounter);
			fail("Expected exception has not occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// The exception should be thrown for this case.
		}
	}

	private void testDispatchCast() {

		// Create a FLOAT64 data to be casted, and put it at a temporary address.
		DataContainer<double[]> float64Input = new DataContainer<double[]>();
		this.memory.setDataContainer(TMP_A_PART, TMP_A_ADDR, float64Input);

		// Set values to operands.
		float64Input.setArrayData(new double[]{ 1.25, 2.25, 3.5 }, 0, new int[] {3}); // Divisible in binary representations.
		this.int64Output.setArrayData(new long[]{ -1L, -1L, -1L }, 0, new int[] {3});

		// Create an instruction.
		Instruction instruction = new Instruction(
				OperationCode.CAST,
				new DataType[]{ DataType.INT64, DataType.FLOAT64 },
				new Memory.Partition[]{ INT64_OUTPUT_PART, TMP_A_PART },
				new int[]{ INT64_OUTPUT_ADDR, TMP_A_ADDR },
				META_PART, META_ADDR
		);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Check the data of the output operand.
		long[] output = this.int64Output.getArrayData();
		if (output[0] != 1L || output[1] != 2L || output[2] != 3L) {
			fail("Incorrect output");
		}
	}

	private void testDispatchFill() {

		// Create a scalar data to fill the vector, and put it at a temporary address.
		DataContainer<long[]> scalar = new DataContainer<long[]>();
		this.memory.setDataContainer(TMP_A_PART, TMP_A_ADDR, scalar);

		// Set values to operands.
		this.int64Output.setArrayData(new long[]{ -1L, -1L, -1L }, 0, new int[] {3});
		scalar.setArrayData(new long[]{ 123L }, 0, new int[] {3});

		// Create an instruction.
		Instruction instruction = new Instruction(
				OperationCode.FILL, INT64_TYPE,
				new Memory.Partition[]{ INT64_OUTPUT_PART, TMP_A_PART },
				new int[]{ INT64_OUTPUT_ADDR, TMP_A_ADDR },
				META_PART, META_ADDR
		);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Check the data of the output operand.
		long[] output = this.int64Output.getArrayData();
		if (output[0] != 123L || output[1] != 123L || output[2] != 123L) {
			fail("Incorrect output");
		}
	}

	private void testDispatchMovelm() {

		// Create data containers for storing element's value and index, and put them at temporary addresses.
		DataContainer<long[]> element = new DataContainer<long[]>();
		DataContainer<long[]> index = new DataContainer<long[]>();
		this.memory.setDataContainer(TMP_A_PART, TMP_A_ADDR, element);
		this.memory.setDataContainer(TMP_B_PART, TMP_B_ADDR, index);

		// Set values to operands.
		this.int64InputA.setArrayData(new long[]{ 11L, 22L, 33L }, 0, new int[] {3}); // Indices to access the element[11][22][33].
		index.setArrayData(new long[]{ 1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		element.setArrayData(new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);

		// Create an instruction.
		Instruction instruction = new Instruction(
				OperationCode.MOVELM, INT64_TYPE,
				new Memory.Partition[]{ TMP_A_PART, INT64_INPUT_A_PART, TMP_B_PART },
				new int[]{ TMP_A_ADDR, INT64_INPUT_A_ADDR, TMP_B_ADDR },
				META_PART, META_ADDR
		);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Check the data of the output operand.
		if (element.getArrayData()[ element.getArrayOffset() ] != 22L) {
			fail("Incorrect output");
		}
	}

	private void testDispatchRefelm() {

		// Create data containers for storing element's value and index, and put them at temporary addresses.
		DataContainer<long[]> element = new DataContainer<long[]>();
		DataContainer<long[]> index = new DataContainer<long[]>();
		this.memory.setDataContainer(TMP_A_PART, TMP_A_ADDR, element);
		this.memory.setDataContainer(TMP_B_PART, TMP_B_ADDR, index);

		// Set values to operands.
		this.int64InputA.setArrayData(new long[]{ 11L, 22L, 33L }, 0, new int[] {3}); // Indices to access the element[11][22][33].
		index.setArrayData(new long[]{ 1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		element.setArrayData(new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);

		// Create an instruction.
		Instruction instruction = new Instruction(
				OperationCode.REFELM, INT64_TYPE,
				new Memory.Partition[]{ TMP_A_PART, INT64_INPUT_A_PART, TMP_B_PART },
				new int[]{ TMP_A_ADDR, INT64_INPUT_A_ADDR, TMP_B_ADDR },
				META_PART, META_ADDR
		);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Check the data of the output operand.
		if (element.getArrayData()[ element.getArrayOffset() ] != 22L) {
			fail("Incorrect output");
		}
	}

	private void testDispatchFree() {

		// Create a data container to be free-ed.
		DataContainer<long[]> target = new DataContainer<long[]>();
		int[] targetLengths = new int[]{ 4 }; // 5-offset
		this.memory.setDataContainer(TMP_A_PART, TMP_A_ADDR, target);

		// Store data.
		long[] data = new long[]{ 1L, 2L, 3L, 4L, 5L };
		target.setArrayData(data, 0, targetLengths);

		// Create an instruction.
		Instruction instruction = new Instruction(
				OperationCode.FREE, INT64_TYPE,
				new Memory.Partition[]{ TMP_A_PART },
				new int[]{ TMP_A_ADDR },
				META_PART, META_ADDR
		);

		// Check the state of the data container before free it.
		if (target.getArrayData() != data
				|| target.getArrayOffset() != 0
				|| target.getArraySize() != 4
				|| target.getArrayLengths()[0] != 4) {

			fail("Incorrect output");
		}

		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Check the state of the data container after it has been free-ed.
		if (target.getArrayData() != null
				|| target.getArraySize() != 1
				|| target.getArrayLengths().length != 0
				|| target.getArrayOffset() != 0) {

			fail("Incorrect output");
		}
	}


	private void testDispatchJmp() {

		// Create data containers for storing the branching condition and the jump address.
		DataContainer<boolean[]> condition = new DataContainer<boolean[]>();
		DataContainer<long[]> jumpAddress = new DataContainer<long[]>();
		this.memory.setDataContainer(TMP_A_PART, TMP_A_ADDR, jumpAddress);
		this.memory.setDataContainer(TMP_B_PART, TMP_B_ADDR, condition);

		// Set values to operands.
		condition.setArrayData(new boolean[]{ true }, 0, new int[] {3});
		jumpAddress.setArrayData(new long[]{ 256L }, 0, new int[] {3});

		// Create an instruction.
		Instruction instruction = new Instruction(
				OperationCode.JMP, BOOL_TYPE,
				new Memory.Partition[]{ Memory.Partition.NONE, TMP_A_PART, TMP_B_PART },
				new int[]{ 0, TMP_A_ADDR, TMP_B_ADDR },
				META_PART, META_ADDR
		);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 256) {
			fail("Incorrect program counter");
		}

		// Modify the branch condition, and re-test the behaviour.
		condition.setArrayData(new boolean[]{ false }, 0, new int[] {3});
		programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}
	}


	private void testDispatchJmpn() {

		// Create data containers for storing the branching condition and the jump address.
		DataContainer<boolean[]> condition = new DataContainer<boolean[]>();
		DataContainer<long[]> jumpAddress = new DataContainer<long[]>();
		this.memory.setDataContainer(TMP_A_PART, TMP_A_ADDR, jumpAddress);
		this.memory.setDataContainer(TMP_B_PART, TMP_B_ADDR, condition);

		// Set values to operands.
		condition.setArrayData(new boolean[]{ true }, 0, new int[] {3});
		jumpAddress.setArrayData(new long[]{ 256L }, 0, new int[] {3});

		// Create an instruction.
		Instruction instruction = new Instruction(
				OperationCode.JMPN, BOOL_TYPE,
				new Memory.Partition[]{ Memory.Partition.NONE, TMP_A_PART, TMP_B_PART },
				new int[]{ 0, TMP_A_ADDR, TMP_B_ADDR },
				META_PART, META_ADDR
		);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Modify the branch condition, and re-test the behaviour.
		condition.setArrayData(new boolean[]{ false }, 0, new int[] {3});
		programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 256) {
			fail("Incorrect program counter");
		}
	}

	private void testDispatchAllocScalar() {

		// Create a data container of which memory will be allocated by ALLOC instruction.
		DataContainer<long[]> target = new DataContainer<long[]>();
		this.memory.setDataContainer(TMP_A_PART, TMP_A_ADDR, target);

		// Create an 1-operand ALLOC instruction, which allocates memory for storing a scalar value.
		Instruction instruction = new Instruction(
				OperationCode.ALLOC, INT64_TYPE,
				new Memory.Partition[]{ TMP_A_PART },
				new int[]{ TMP_A_ADDR },
				META_PART, META_ADDR
		);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		this.connectedMethodCalled = false;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check that the memory is allocated correctly.
		if (!(target.getArrayData() instanceof long[])) {
			fail("Incorrect type of data");
		}
		if (target.getArraySize() != 1) {
			fail("Incorrect size");
		}
		if (target.getArrayRank() != 0) {
			fail("Incorrect rank");
		}
		if (target.getArrayLengths().length != 0) {
			fail("Incorrect lengths");
		}
		long[] targetData = target.getArrayData();
		if (!(0 < targetData.length) || !(target.getArraySize()+target.getArrayOffset() <= targetData.length)) {
			fail("Incorrect length of data");
		}
	}

	private void testDispatchAllocArray() {

		// Prepare a data container for storing the allocated array,
		// and operands representing lengths of the array to be allocated.
		DataContainer<long[]> target = new DataContainer<long[]>();
		DataContainer<long[]> len0 = new DataContainer<long[]>();
		DataContainer<long[]> len1 = new DataContainer<long[]>();
		DataContainer<long[]> len2 = new DataContainer<long[]>();
		this.memory.setDataContainer(TMP_A_PART, TMP_A_ADDR, target);
		this.memory.setDataContainer(TMP_B_PART, TMP_B_ADDR, len0);
		this.memory.setDataContainer(TMP_C_PART, TMP_C_ADDR, len1);
		this.memory.setDataContainer(TMP_D_PART, TMP_D_ADDR, len2);

		// Set values of the length-operands ([2][3][4]).
		len0.setArrayData(new long[] { 2L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len1.setArrayData(new long[] { 3L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		len2.setArrayData(new long[] { 4L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);

		// Create an multi-operands ALLOC instruction, which allocates memory for storing an array.
		Instruction instruction = new Instruction(
				OperationCode.ALLOC, INT64_TYPE,
				new Memory.Partition[]{ TMP_A_PART, TMP_B_PART, TMP_C_PART, TMP_D_PART },
				new int[]{ TMP_A_ADDR, TMP_B_ADDR, TMP_C_ADDR, TMP_D_ADDR },
				META_PART, META_ADDR
		);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		this.connectedMethodCalled = false;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check that the memory is allocated correctly.
		if (!(target.getArrayData() instanceof long[])) {
			fail("Incorrect type of instance");
		}
		if (target.getArraySize() != 24) {
			fail("Incorrect size");
		}
		if (target.getArrayRank() != 3) {
			fail("Incorrect rank");
		}
		if (target.getArrayLengths()[0] != 2 || target.getArrayLengths()[1] != 3L || target.getArrayLengths()[2] != 4L) {
			fail("Incorrect lengths");
		}
		long[] targetData = target.getArrayData();
		if (!(0 < targetData.length) || !(target.getArraySize()+target.getArrayOffset() <= targetData.length)) {
			fail("Incorrect actual length of data");
		}
	}


	// The merhod to be called by CALLX instruction.
	public long methodToConnect(long a, long b) {
		this.connectedMethodCalled = true;
		return a + b;
	}

	private void testCallx() {

		// Prepare the method to be called by the CALLX instruction..
		Method method = null;
		try {
			method = this.getClass().getMethod("methodToConnect", long.class, long.class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			fail("Reflection failed");
		}

		// Create an interconnect, and connect the method to it.
		Interconnect interconnect = new Interconnect();
		try {
			interconnect.connectPlugin(SpecialBindingKey.AUTO_KEY, new Object[] {method,this} );
		} catch (VnanoException e) {
			fail("Connection failed");
		}

		// Prepare operands.
		DataContainer<long[]> argA = new DataContainer<long[]>();
		DataContainer<long[]> argB = new DataContainer<long[]>();
		DataContainer<long[]> ret = new DataContainer<long[]>();
		DataContainer<long[]> functionAddressContainer = new DataContainer<long[]>();
		this.memory.setDataContainer(TMP_A_PART, TMP_A_ADDR, argA);
		this.memory.setDataContainer(TMP_B_PART, TMP_B_ADDR, argB);
		this.memory.setDataContainer(TMP_C_PART, TMP_C_ADDR, ret);
		this.memory.setDataContainer(TMP_D_PART, TMP_D_ADDR, functionAddressContainer);
		argA.setArrayData(new long[]{ 123L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		argB.setArrayData(new long[]{ 456L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		ret.setArrayData(new long[]{ -1 }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		int functionAddress = 0; // Only 1 function is connected, so the address of it should be 0.
		functionAddressContainer.setArrayData(new long[]{ functionAddress }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);

		// Create an instruction.
		Instruction instruction = new Instruction(
				OperationCode.CALLX, INT64_TYPE,
				new Memory.Partition[]{ TMP_C_PART, TMP_D_PART, TMP_A_PART, TMP_B_PART },
				new int[]{ TMP_C_ADDR, TMP_D_ADDR, TMP_A_ADDR, TMP_B_ADDR },
				META_PART, META_ADDR
		);

		// Dispatch/execute the instruction.
		int programCounter = 10;
		this.connectedMethodCalled = false;
		try {
			programCounter = this.dispatch(instruction, interconnect, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check tha the method has been called.
		if (!this.connectedMethodCalled) {
			fail("Method did not called");
		}

		// Check the return value (sum of two args: 123 + 456)
		if(ret.getArrayData()[0] != 579) {
			fail("Incorrect returned value");
		}
	}

	private void testDispatchNop() {

		// Create a NOP instruction.
		Instruction instruction = new Instruction(
				OperationCode.NOP, VOID_TYPE,
				new Memory.Partition[] {Memory.Partition.NONE}, new int[] {0},
				META_PART, META_ADDR
		);


		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Test of the behaviour when the number of operands is incorrect.
		try {
			instruction = this.generateInt64x3Instruction(OperationCode.NOP);
			programCounter = this.dispatch(instruction, programCounter);
			fail("Expected exception has not occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// The exception should be thrown for this case.
		}
	}


	private void testDispatchLabel() {

		// Create a LABEL instruction.
		Instruction instruction = new Instruction(
				OperationCode.LABEL, VOID_TYPE,
				new Memory.Partition[] {Memory.Partition.NONE}, new int[] {0},
				META_PART, META_ADDR
		);


		// Dispatch/execute the instruction.
		int programCounter = 10;
		try {
			programCounter = this.dispatch(instruction, programCounter);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Expected exception has not occurred");
		}

		// Check the updated value of the program counter.
		if (programCounter != 11) {
			fail("Incorrect program counter");
		}

		// Test of the behaviour when the number of operands is incorrect.
		try {
			instruction = this.generateInt64x3Instruction(OperationCode.LABEL);
			programCounter = this.dispatch(instruction, programCounter);
			fail("Expected exception has not occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// The exception should be thrown for this case.
		}
	}



}
