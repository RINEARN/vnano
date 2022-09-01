/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.processor;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.spec.OptionValue;
import org.vcssl.nano.spec.SpecialBindingKey;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.VnanoException;

/**
 * The test of Processor class.
 */
public class ProcessorTest {

	private static final int REGISTER_N = 200;
	private Interconnect interconnect;
	private Memory memory;
	private DataContainer<?>[] registers;

	private static final int META_ADDR = 888;
	private static final Memory.Partition META_PART = Memory.Partition.CONSTANT;

	// The method to be called from the processor.
	public long methodToConnect(long a, long b) {
		this.connectedMethodCalled = true;
		return a + b;
	}

	// Set true in the above method, for checking that it is called.
	private boolean connectedMethodCalled = false;


	@Before
	public void setUp() throws Exception {

		// Create an empty interconnect.
		this.interconnect = new Interconnect();

		// Create a memory, and initialize the data-containers of the registers in the memory.
		this.memory = new Memory();
		this.registers = new DataContainer<?>[REGISTER_N];
		for (int addr=0; addr<REGISTER_N; addr++) {
			registers[addr] = new DataContainer<Object>();
			memory.setDataContainer(Memory.Partition.REGISTER, addr, registers[addr]);
		}

		// Prepare meta information referred by instructions.
		DataContainer<String[]> metaContainer = new DataContainer<String[]>();
		metaContainer.setArrayData(new String[]{ "meta" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		memory.setDataContainer(META_PART, META_ADDR, metaContainer);

		// Create a default option map, and set it to the interconnect.
		Map<String, Object> optionMap = new LinkedHashMap<String, Object>();
		optionMap = OptionValue.normalizeValuesOf(optionMap);
		this.interconnect.setOptionMap(optionMap);
	}

	@After
	public void tearDown() throws Exception {
	}

	private void initializeRegisters() {
		for (DataContainer<?> register: registers) {
			register.initialize();
		}
	}


	private Instruction generateInstruction(OperationCode operationCode, DataType dataType,
			int ... addresses) {

		int operandLength = addresses.length;
		Memory.Partition[] partitions = new Memory.Partition[ operandLength ];
		Arrays.fill(partitions, Memory.Partition.REGISTER);

		Instruction instruction = new Instruction(
				operationCode, new DataType[]{ dataType },
				partitions, addresses, META_PART, META_ADDR
		);

		return instruction;
	}


	@SuppressWarnings("unchecked")
	@Test
	public void testProcessSingleInstruction() {

		// Set the values to the registers.
s		this.initializeRegisters();
		((DataContainer<long[]>)this.registers[0]).setArrayData(new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R0=-1
		((DataContainer<long[]>)this.registers[1]).setArrayData(new long[]{ 123L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR); // R1=123
		((DataContainer<long[]>)this.registers[2]).setArrayData(new long[]{ 456L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR); // R2=456

		// Create an instruction performing the addition "R0 = R1 + R2".
		Instruction instruction = this.generateInstruction(OperationCode.ADD, DataType.INT64, 0, 1, 2); // ADD INT64 R0 R1 R2

		// Execute the instruction.
		int programCounter = 10;
		try {
			programCounter = new Processor().process(instruction, this.memory, this.interconnect, programCounter);
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// Check the updated program counter, and the operation result.
		assertEquals(11, programCounter);
		assertEquals(579L, ((DataContainer<long[]>)this.registers[0]).getArrayData()[0]); // R0==579
	}


	@SuppressWarnings("unchecked")
	@Test
	public void testProcessSerialInstructions() {

		// Set the values to the registers.
		this.initializeRegisters();
		((DataContainer<long[]>)this.registers[0]).setArrayData(new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R0=-1
		((DataContainer<long[]>)this.registers[1]).setArrayData(new long[]{ 123L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR); // R1=123
		((DataContainer<long[]>)this.registers[2]).setArrayData(new long[]{ 456L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR); // R2=456
		((DataContainer<long[]>)this.registers[3]).setArrayData(new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R3=-1
		((DataContainer<long[]>)this.registers[4]).setArrayData(new long[]{ 200L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR); // R4=200
		((DataContainer<long[]>)this.registers[5]).setArrayData(new long[]{ 100L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR); // R5=100
		((DataContainer<long[]>)this.registers[6]).setArrayData(new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R6=-1

		// Create instructions performing the series of operations "R0 = (R1+R2) * (R4-R5)".
		Instruction[] instructions = new Instruction[]{
				this.generateInstruction(OperationCode.ADD, DataType.INT64, 0, 1, 2), // ADD INT64 R0 R1 R2 (R0=R1+R2)
				this.generateInstruction(OperationCode.MOV, DataType.INT64, 3, 0),    // MOV R4 R0          (R3=R0)
				this.generateInstruction(OperationCode.SUB, DataType.INT64, 0, 4, 5), // SUB INT64 R0 R4 R5 (R0=R4-R5)
				this.generateInstruction(OperationCode.MOV, DataType.INT64, 6, 0),    // MOV R6 R0          (R6=R0)
				this.generateInstruction(OperationCode.MUL, DataType.INT64, 0, 3, 6), // MUL INT64 R0 R3 R6 (R0=R3*R6)
		};

		// Execute instructions.
		try {
			new Processor().process(instructions, this.memory, this.interconnect);
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// Check the operation result.
		assertEquals(57900L, ((DataContainer<long[]>)this.registers[0]).getArrayData()[0]); // R0==57900
	}


	@SuppressWarnings("unchecked")
	@Test
	public void testProcessBranchedInstructions() {

		// Set the values to the registers.
		this.initializeRegisters();
		((DataContainer<long[]>)this.registers[1]).setArrayData(new long[]{ 111L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR); // R1=111
		((DataContainer<long[]>)this.registers[2]).setArrayData(new long[]{ 222L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR); // R2=222
		((DataContainer<long[]>)this.registers[3]).setArrayData(new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R3=-1
		((DataContainer<long[]>)this.registers[4]).setArrayData(new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R4=-1
		((DataContainer<long[]>)this.registers[10]).setArrayData(new long[]{ 3L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R10=3 ... 分岐先の命令位置

		// Create instructions containing a branch instruction (JMP).
		Instruction[] instructions = new Instruction[]{
				this.generateInstruction(OperationCode.MOV, DataType.INT64, 3, 1),        // MOV INT64 R3 R1         (R3=R1)
				this.generateInstruction(OperationCode.JMP, DataType.BOOL,  100, 10, 0),  // JMP BOOL  R100 R10 R0   (Jumps to the instruction of which address is R10, when R0 is true. The first operand R100 is a placeholder.)
				this.generateInstruction(OperationCode.MOV, DataType.INT64, 3, 2),        // MOV INT64 R3 R2         (R3=R2 ... Will be skipped when R0 is true.)
				this.generateInstruction(OperationCode.MOV, DataType.INT64, 4, 3),        // MOV INT64 R4 R3         (R4=R3 ... Jumped to this point when R0 is true.)
		};

		// Execute instructions under the condition of R0 is true. (So the flow should jump.)
		((DataContainer<boolean[]>)this.registers[0]).setArrayData(new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R0=true
		try {
			new Processor().process(instructions, this.memory, this.interconnect);
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// Check the result.
		assertEquals(111L, ((DataContainer<long[]>)this.registers[4]).getArrayData()[0]); // R4==111 (==R1)

		// Execute instructions under the condition of R0 is false. (So the flow should not jump.)
		((DataContainer<boolean[]>)this.registers[0]).setArrayData(new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R0=false
		try {
			new Processor().process(instructions, this.memory, this.interconnect);
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// Check the result.
		assertEquals(222L, ((DataContainer<long[]>)this.registers[4]).getArrayData()[0]); // R4==222 (==R2)


		// Check that the process ends when the flow has jumped to out of bounds of instructions (-1).
		((DataContainer<long[]>)this.registers[4]).setArrayData(new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);   // R4=-1
		((DataContainer<long[]>)this.registers[10]).setArrayData(new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R10=-1 ... The address to jump.
		((DataContainer<boolean[]>)this.registers[0]).setArrayData(new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R0=true
		try {
			new Processor().process(instructions, this.memory, this.interconnect);
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}
		assertEquals(-1L, ((DataContainer<long[]>)this.registers[4]).getArrayData()[0]); // R4==-1 (初期値)
	}


	@SuppressWarnings("unchecked")
	@Test
	public void testProcessCallInstructions() {

		// Set the values to the registers.
		this.initializeRegisters();
		((DataContainer<long[]>)this.registers[0]).setArrayData(new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R0=-1
		((DataContainer<long[]>)this.registers[1]).setArrayData(new long[]{ 123L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR); // R1=123
		((DataContainer<long[]>)this.registers[2]).setArrayData(new long[]{ 456L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR); // R2=456

		// Create an interconnect, and connect the method: "methodToConnect".
		Interconnect interconnect = new Interconnect();
		Method method;
		try {
			method = this.getClass().getMethod("methodToConnect", long.class, long.class);
			interconnect.connectPlugin(SpecialBindingKey.AUTO_KEY, new Object[] {method,this} );
		} catch (NoSuchMethodException | SecurityException | VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// Store the external function address of the "methodToConnect" (is 0) to R10 register.
		int functionAddress = 0;
		((DataContainer<long[]>)this.registers[10]).setArrayData(new long[]{ (long)functionAddress }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);

		// Create a CALLX instruction for calling the "methodToConnect" method from the processor.
		Instruction[] instructions = new Instruction[]{
				this.generateInstruction(OperationCode.CALLX, DataType.INT64, 0, 10, 1, 2), // CALLX R0 R10 R1 R2 (R0=methodToConnect(R1,R2))
		};

		// Execute the instruction.
		try {
			new Processor().process(instructions, this.memory, interconnect);
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// Check that the method has been called, and the return value is correct.
		assertEquals(true, this.connectedMethodCalled);
		assertEquals(579L, ((DataContainer<long[]>)this.registers[0]).getArrayData()[0]); // R0==579
	}
}
