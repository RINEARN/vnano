/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.processor;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.lang.AbstractFunction;
import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.VnanoSyntaxException;

public class ProcessorTest {

	private static final int REGISTER_N = 100;
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

		// テストで関数として呼び出すメソッドを接続したインターコネクトを用意
		this.interconnect = new Interconnect();
		this.interconnect.connect(this.getClass().getMethod("methodToConnect", long.class, long.class), this);

		// レジスタを生成してメモリに配置
		this.memory = new Memory();
		this.registers = new DataContainer<?>[REGISTER_N];
		for (int addr=0; addr<REGISTER_N; addr++) {
			registers[addr] = new DataContainer<Object>();
			memory.setDataContainer(Memory.Partition.REGISTER, addr, registers[addr]);
		}

		// 命令のメタ情報をメモリに配置（命令の生成に必ず必要）
		DataContainer<String[]> metaContainer = new DataContainer<String[]>();
		metaContainer.setData(new String[]{ "meta" });
		memory.setDataContainer(META_PART, META_ADDR, metaContainer);
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
		((DataContainer<long[]>)this.registers[0]).setData(new long[]{ -1L });  // R0=-1
		((DataContainer<long[]>)this.registers[1]).setData(new long[]{ 123L }); // R1=123
		((DataContainer<long[]>)this.registers[2]).setData(new long[]{ 456L }); // R2=456

		// R0 = R1 + R2 の値を求める加算命令を生成
		Instruction instruction = this.generateInstruction(OperationCode.ADD, DataType.INT64, 0, 1, 2); // ADD INT64 R0 R1 R2

		// 命令を単一実行
		int programCounter = 10; // 実行時点でのプログラムカウンタの値（実行によって戻り値で更新される）
		try {
			programCounter = new Processor().process(instruction, this.memory, this.interconnect, programCounter);
		} catch (VnanoSyntaxException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値と演算結果の検査
		assertEquals(11, programCounter);
		assertEquals(579L, ((DataContainer<long[]>)this.registers[0]).getData()[0]); // R0==579
	}

	// 分岐の無い命令列の逐次実行
	@SuppressWarnings("unchecked")
	@Test
	public void testProcessSerialInstructions() {

		// レジスタにテスト値を設定
		this.initializeRegisters();
		((DataContainer<long[]>)this.registers[0]).setData(new long[]{ -1L });  // R0=-1
		((DataContainer<long[]>)this.registers[1]).setData(new long[]{ 123L }); // R1=123
		((DataContainer<long[]>)this.registers[2]).setData(new long[]{ 456L }); // R2=456
		((DataContainer<long[]>)this.registers[3]).setData(new long[]{ -1L });  // R3=-1
		((DataContainer<long[]>)this.registers[4]).setData(new long[]{ 200L }); // R4=200
		((DataContainer<long[]>)this.registers[5]).setData(new long[]{ 100L }); // R5=100
		((DataContainer<long[]>)this.registers[6]).setData(new long[]{ -1L });  // R6=-1

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
		} catch (VnanoSyntaxException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// 演算結果の検査
		assertEquals(57900L, ((DataContainer<long[]>)this.registers[0]).getData()[0]); // R0==57900
	}

	// 分岐のある命令列の逐次実行
	@SuppressWarnings("unchecked")
	@Test
	public void testProcessBranchedInstructions() {

		// レジスタにテスト値を設定
		this.initializeRegisters();
		((DataContainer<long[]>)this.registers[1]).setData(new long[]{ 111L }); // R1=111
		((DataContainer<long[]>)this.registers[2]).setData(new long[]{ 222L }); // R2=222
		((DataContainer<long[]>)this.registers[3]).setData(new long[]{ -1L });  // R3=-1
		((DataContainer<long[]>)this.registers[4]).setData(new long[]{ -1L });  // R4=-1
		((DataContainer<long[]>)this.registers[10]).setData(new long[]{ 3L });  // R10=3 ... 分岐先の命令位置

		// R0 = (R1+R2) * (R4-$5) の値を求める逐次演算の命令列を生成
		Instruction[] instructions = new Instruction[]{
				this.generateInstruction(OperationCode.MOV, DataType.INT64, 3, 1),   // MOV INT64 R3 R1  (R3=R1)
				this.generateInstruction(OperationCode.JMP, DataType.BOOL,  0, 10),  // JMP BOOL  R0 R10 (R0がtrueならR10番命令に飛ぶ)
				this.generateInstruction(OperationCode.MOV, DataType.INT64, 3, 2),   // MOV INT64 R3 R2  (R3=R2 ... 分岐成立で飛ばされる)
				this.generateInstruction(OperationCode.MOV, DataType.INT64, 4, 3),   // MOV INT64 R4 R3  (R4=R3 ... 分岐成立でここに着地)
		};

		// 分岐成立の条件（R0==true）で命令列を逐次実行
		((DataContainer<boolean[]>)this.registers[0]).setData(new boolean[]{ true });  // R0=true
		try {
			new Processor().process(instructions, this.memory, this.interconnect);
		} catch (VnanoSyntaxException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// 演算結果の検査
		assertEquals(111L, ((DataContainer<long[]>)this.registers[4]).getData()[0]); // R4==111 (==R1)

		// 分岐不成立の条件（R0==false）で命令列を逐次実行
		((DataContainer<boolean[]>)this.registers[0]).setData(new boolean[]{ false });  // R0=false
		try {
			new Processor().process(instructions, this.memory, this.interconnect);
		} catch (VnanoSyntaxException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// 演算結果の検査
		assertEquals(222L, ((DataContainer<long[]>)this.registers[4]).getData()[0]); // R4==222 (==R2)


		// 分岐で命令列の領域外(-1)に飛ぶと実行が終了する事を検査
		((DataContainer<long[]>)this.registers[4]).setData(new long[]{ -1L });   // R4=-1
		((DataContainer<long[]>)this.registers[10]).setData(new long[]{ -1L });  // R10=-1 ... 分岐先の命令位置
		((DataContainer<boolean[]>)this.registers[0]).setData(new boolean[]{ true });  // R0=true
		try {
			new Processor().process(instructions, this.memory, this.interconnect);
		} catch (VnanoSyntaxException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}
		assertEquals(-1L, ((DataContainer<long[]>)this.registers[4]).getData()[0]); // R4==-1 (初期値)
	}


	// 関数コール命令の実行（Processor.processの引数に渡すInterconnectが繋がっている事の確認）
	@SuppressWarnings("unchecked")
	@Test
	public void testProcessCallInstructions() {

		// レジスタにテスト値を設定
		this.initializeRegisters();
		((DataContainer<long[]>)this.registers[0]).setData(new long[]{ -1L });  // R0=-1
		((DataContainer<long[]>)this.registers[1]).setData(new long[]{ 123L }); // R1=123
		((DataContainer<long[]>)this.registers[2]).setData(new long[]{ 456L }); // R2=456

		// Interconnect に接続された"methodToConnect" メソッドの関数アドレスを取得し、R10レジスタに設定
		AbstractFunction function = this.interconnect.getGlobalFunctionTable().getFunctionBySignature(
				"methodToConnect", new DataType[]{DataType.INT64, DataType.INT64}, new int[]{0, 0}
		);
		int functionAddress = this.interconnect.getGlobalFunctionTable().indexOf(function);
		((DataContainer<long[]>)this.registers[10]).setData(new long[]{ (long)functionAddress });

		// メソッドを呼び出す CALL 命令を生成
		Instruction[] instructions = new Instruction[]{
				this.generateInstruction(OperationCode.CALL, DataType.INT64, 0, 10, 1, 2), // CALL R0 R10 R1 R2 (R0=methodToConnect(R1,R2))
		};

		// 命令を実行
		try {
			new Processor().process(instructions, this.memory, this.interconnect);
		} catch (VnanoSyntaxException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// メソッドが呼ばれた事と戻り値の検査
		assertEquals(true, this.connectedMethodCalled);
		assertEquals(579L, ((DataContainer<long[]>)this.registers[0]).getData()[0]); // R0==579
	}
}
