/*
 * Copyright(C) 2017-2021 RINEARN (Fumihiro Matsui)
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

public class ProcessorTest {

	private static final int REGISTER_N = 200;
	private Interconnect interconnect;
	private Memory memory;
	private DataContainer<?>[] registers;

	private static final int META_ADDR = 888;
	private static final Memory.Partition META_PART = Memory.Partition.CONSTANT;

	// 処理系から関数が呼ばれた事を確認する
	private boolean connectedMethodCalled = false;

	// 処理系から関数として呼ぶメソッド
	public long methodToConnect(long a, long b) {
		this.connectedMethodCalled = true;
		return a + b;
	}

	@Before
	public void setUp() throws Exception {

		// 何も接続されていない、デフォルトのインターコネクトを用意（接続する場合は各テスト内で別途生成）
		this.interconnect = new Interconnect();

		// レジスタを生成してメモリに配置
		this.memory = new Memory();
		this.registers = new DataContainer<?>[REGISTER_N];
		for (int addr=0; addr<REGISTER_N; addr++) {
			registers[addr] = new DataContainer<Object>();
			memory.setDataContainer(Memory.Partition.REGISTER, addr, registers[addr]);
		}

		// 命令のメタ情報をメモリに配置（命令の生成に必ず必要）
		DataContainer<String[]> metaContainer = new DataContainer<String[]>();
		metaContainer.setArrayData(new String[]{ "meta" }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		memory.setDataContainer(META_PART, META_ADDR, metaContainer);

		// デフォルトのオプションマップを用意し、インターコネクトに設定
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

	// 単一命令の実行
	@SuppressWarnings("unchecked")
	@Test
	public void testProcessSingleInstruction() {

		// レジスタにテスト値を設定
		this.initializeRegisters();
		((DataContainer<long[]>)this.registers[0]).setArrayData(new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R0=-1
		((DataContainer<long[]>)this.registers[1]).setArrayData(new long[]{ 123L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR); // R1=123
		((DataContainer<long[]>)this.registers[2]).setArrayData(new long[]{ 456L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR); // R2=456

		// R0 = R1 + R2 の値を求める加算命令を生成
		Instruction instruction = this.generateInstruction(OperationCode.ADD, DataType.INT64, 0, 1, 2); // ADD INT64 R0 R1 R2

		// 命令を単一実行
		int programCounter = 10; // 実行時点でのプログラムカウンタの値（実行によって戻り値で更新される）
		try {
			programCounter = new Processor().process(instruction, this.memory, this.interconnect, programCounter);
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値と演算結果の検査
		assertEquals(11, programCounter);
		assertEquals(579L, ((DataContainer<long[]>)this.registers[0]).getArrayData()[0]); // R0==579
	}

	// 分岐の無い命令列の逐次実行
	@SuppressWarnings("unchecked")
	@Test
	public void testProcessSerialInstructions() {

		// レジスタにテスト値を設定
		this.initializeRegisters();
		((DataContainer<long[]>)this.registers[0]).setArrayData(new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R0=-1
		((DataContainer<long[]>)this.registers[1]).setArrayData(new long[]{ 123L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR); // R1=123
		((DataContainer<long[]>)this.registers[2]).setArrayData(new long[]{ 456L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR); // R2=456
		((DataContainer<long[]>)this.registers[3]).setArrayData(new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R3=-1
		((DataContainer<long[]>)this.registers[4]).setArrayData(new long[]{ 200L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR); // R4=200
		((DataContainer<long[]>)this.registers[5]).setArrayData(new long[]{ 100L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR); // R5=100
		((DataContainer<long[]>)this.registers[6]).setArrayData(new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R6=-1

		// R0 = (R1+R2) * (R4-$5) の値を求める逐次演算の命令列を生成
		Instruction[] instructions = new Instruction[]{
				this.generateInstruction(OperationCode.ADD, DataType.INT64, 0, 1, 2), // ADD INT64 R0 R1 R2 (R0=R1+R2)
				this.generateInstruction(OperationCode.MOV, DataType.INT64, 3, 0),    // MOV R4 R0          (R3=R0)
				this.generateInstruction(OperationCode.SUB, DataType.INT64, 0, 4, 5), // SUB INT64 R0 R4 R5 (R0=R4-R5)
				this.generateInstruction(OperationCode.MOV, DataType.INT64, 6, 0),    // MOV R6 R0          (R6=R0)
				this.generateInstruction(OperationCode.MUL, DataType.INT64, 0, 3, 6), // MUL INT64 R0 R3 R6 (R0=R3*R6)
		};

		// 命令列を逐次実行
		try {
			new Processor().process(instructions, this.memory, this.interconnect);
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// 演算結果の検査
		assertEquals(57900L, ((DataContainer<long[]>)this.registers[0]).getArrayData()[0]); // R0==57900
	}

	// 分岐のある命令列の逐次実行
	@SuppressWarnings("unchecked")
	@Test
	public void testProcessBranchedInstructions() {

		// レジスタにテスト値を設定
		this.initializeRegisters();
		((DataContainer<long[]>)this.registers[1]).setArrayData(new long[]{ 111L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR); // R1=111
		((DataContainer<long[]>)this.registers[2]).setArrayData(new long[]{ 222L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR); // R2=222
		((DataContainer<long[]>)this.registers[3]).setArrayData(new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R3=-1
		((DataContainer<long[]>)this.registers[4]).setArrayData(new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R4=-1
		((DataContainer<long[]>)this.registers[10]).setArrayData(new long[]{ 3L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R10=3 ... 分岐先の命令位置

		// R0 = (R1+R2) * (R4-$5) の値を求める逐次演算の命令列を生成
		Instruction[] instructions = new Instruction[]{
				this.generateInstruction(OperationCode.MOV, DataType.INT64, 3, 1),   // MOV INT64 R3 R1  (R3=R1)
				this.generateInstruction(OperationCode.JMP, DataType.BOOL,  100, 10, 0),  // JMP BOOL  R100 R10 R0 (R0がtrueならR10番命令に飛ぶ / 先頭オペランドは読み書きされないプレースホルダ)
				this.generateInstruction(OperationCode.MOV, DataType.INT64, 3, 2),   // MOV INT64 R3 R2  (R3=R2 ... 分岐成立で飛ばされる)
				this.generateInstruction(OperationCode.MOV, DataType.INT64, 4, 3),   // MOV INT64 R4 R3  (R4=R3 ... 分岐成立でここに着地)
		};

		// 分岐成立の条件（R0==true）で命令列を逐次実行
		((DataContainer<boolean[]>)this.registers[0]).setArrayData(new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R0=true
		try {
			new Processor().process(instructions, this.memory, this.interconnect);
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// 演算結果の検査
		assertEquals(111L, ((DataContainer<long[]>)this.registers[4]).getArrayData()[0]); // R4==111 (==R1)

		// 分岐不成立の条件（R0==false）で命令列を逐次実行
		((DataContainer<boolean[]>)this.registers[0]).setArrayData(new boolean[]{ false }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R0=false
		try {
			new Processor().process(instructions, this.memory, this.interconnect);
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// 演算結果の検査
		assertEquals(222L, ((DataContainer<long[]>)this.registers[4]).getArrayData()[0]); // R4==222 (==R2)


		// 分岐で命令列の領域外(-1)に飛ぶと実行が終了する事を検査
		((DataContainer<long[]>)this.registers[4]).setArrayData(new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);   // R4=-1
		((DataContainer<long[]>)this.registers[10]).setArrayData(new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R10=-1 ... 分岐先の命令位置
		((DataContainer<boolean[]>)this.registers[0]).setArrayData(new boolean[]{ true }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R0=true
		try {
			new Processor().process(instructions, this.memory, this.interconnect);
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}
		assertEquals(-1L, ((DataContainer<long[]>)this.registers[4]).getArrayData()[0]); // R4==-1 (初期値)
	}


	// 関数コール命令の実行（Processor.processの引数に渡すInterconnectが繋がっている事の確認）
	@SuppressWarnings("unchecked")
	@Test
	public void testProcessCallInstructions() {

		// レジスタにテスト値を設定
		this.initializeRegisters();
		((DataContainer<long[]>)this.registers[0]).setArrayData(new long[]{ -1L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);  // R0=-1
		((DataContainer<long[]>)this.registers[1]).setArrayData(new long[]{ 123L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR); // R1=123
		((DataContainer<long[]>)this.registers[2]).setArrayData(new long[]{ 456L }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR); // R2=456

		// このテスト用のInterconnectを生成し、"methodToConnect" メソッドを接続
		Interconnect interconnect = new Interconnect();
		Method method;
		try {
			method = this.getClass().getMethod("methodToConnect", long.class, long.class);
			interconnect.connectPlugin(SpecialBindingKey.AUTO_KEY, new Object[] {method,this} );
		} catch (NoSuchMethodException | SecurityException | VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// 接続された "methodToConnect" メソッドをR10レジスタに設定
		int functionAddress = 0;  // 関数は1個しか接続していないので0番なはず
		((DataContainer<long[]>)this.registers[10]).setArrayData(new long[]{ (long)functionAddress }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);

		// メソッドを呼び出す CALLX 命令を生成
		Instruction[] instructions = new Instruction[]{
				this.generateInstruction(OperationCode.CALLX, DataType.INT64, 0, 10, 1, 2), // CALLX R0 R10 R1 R2 (R0=methodToConnect(R1,R2))
		};

		// 命令を実行
		try {
			new Processor().process(instructions, this.memory, interconnect);
		} catch (VnanoException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// メソッドが呼ばれた事と戻り値の検査
		assertEquals(true, this.connectedMethodCalled);
		assertEquals(579L, ((DataContainer<long[]>)this.registers[0]).getArrayData()[0]); // R0==579
	}
}
