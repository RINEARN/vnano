/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.processor;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vcssl.nano.interconnect.AbstractFunction;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.spec.SpecialBindingKey;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.VnanoException;


public class DispatchUnitTest {

	private Memory memory;
	private Interconnect interconnect;

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
		this.int64Output.setData(new long[3], new int[]{ 3 });
		this.int64InputA.setData(new long[3], new int[]{ 3 });
		this.int64InputB.setData(new long[3], new int[]{ 3 });

		this.boolOutput = new DataContainer<boolean[]>();
		this.boolInputA = new DataContainer<boolean[]>();
		this.boolInputB = new DataContainer<boolean[]>();
		this.boolOutput.setData(new boolean[3], new int[]{ 3 });
		this.boolInputA.setData(new boolean[3], new int[]{ 3 });
		this.boolInputB.setData(new boolean[3], new int[]{ 3 });

		this.memory = new Memory();
		this.memory.setDataContainer(INT64_OUTPUT_PART, INT64_OUTPUT_ADDR, this.int64Output);
		this.memory.setDataContainer(INT64_INPUT_A_PART, INT64_INPUT_A_ADDR, this.int64InputA);
		this.memory.setDataContainer(INT64_INPUT_B_PART, INT64_INPUT_B_ADDR, this.int64InputB);
		this.memory.setDataContainer(BOOL_OUTPUT_PART, BOOL_OUTPUT_ADDR, this.boolOutput);
		this.memory.setDataContainer(BOOL_INPUT_A_PART, BOOL_INPUT_A_ADDR, this.boolInputA);
		this.memory.setDataContainer(BOOL_INPUT_B_PART, BOOL_INPUT_B_ADDR, this.boolInputB);

		this.interconnect = new Interconnect();
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
		this.testDispatchElem();
		//this.testDispatchLen();
		this.testDispatchFree();
		this.testDispatchJmp();
		this.testDispatchJmpn();
		this.testDispatchAllocScalar();
		this.testDispatchAllocVector();
		this.testCall();
		this.testDispatchNop();
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
				instruction, this.memory, this.interconnect, new ExecutionUnit(), new boolean[] {false}, pc
		);
	}

	private void testDispatchAdd() {

		// 入出力オペランドに値を設定
		this.int64InputA.setData(new long[]{ 1L, 2L, 3L }, new int[] {3});
		this.int64InputB.setData(new long[]{ 4L, 5L, 6L }, new int[] {3});
		this.int64Output.setData(new long[]{ -1L, -1L, -1L }, new int[] {3});

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = this.generateInt64x3Instruction(OperationCode.ADD);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// 命令実行結果の値を検査
		long[] output = this.int64Output.getData();
		if (output[0] != 5L || output[1] != 7L || output[2] != 9L) {
			fail("Incorrect output");
		}

		// オペランドの個数が間違っている場合の検査
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.ADD);
			pc = this.dispatch(instruction, pc);
			fail("Unexpected exception occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}

	private void testDispatchSub() {

		// 入出力オペランドに値を設定
		this.int64InputA.setData(new long[]{ 10L, 5L, 3L }, new int[] {3});
		this.int64InputB.setData(new long[]{ 2L, 5L, 7L }, new int[] {3});
		this.int64Output.setData(new long[]{ -1L, -1L, -1L }, new int[] {3});

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = this.generateInt64x3Instruction(OperationCode.SUB);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// 命令実行結果の値を検査
		long[] output = this.int64Output.getData();
		if (output[0] != 8L || output[1] != 0L || output[2] != -4L) {
			fail("Incorrect output");
		}

		// オペランドの個数が間違っている場合の検査
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.SUB);
			pc = this.dispatch(instruction, pc);
			fail("Unexpected exception occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}

	private void testDispatchMul() {

		// 入出力オペランドに値を設定
		this.int64InputA.setData(new long[]{ 1L, 2L, 3L }, new int[] {3});
		this.int64InputB.setData(new long[]{ 4L, 5L, 6L }, new int[] {3});
		this.int64Output.setData(new long[]{ -1L, -1L, -1L }, new int[] {3});

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = this.generateInt64x3Instruction(OperationCode.MUL);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// 命令実行結果の値を検査
		long[] output = this.int64Output.getData();
		if (output[0] != 4L || output[1] != 10L || output[2] != 18L) {
			fail("Incorrect output");
		}

		// オペランドの個数が間違っている場合の検査
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.MUL);
			pc = this.dispatch(instruction, pc);
			fail("Unexpected exception occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}

	private void testDispatchDiv() {

		// 入出力オペランドに値を設定
		this.int64InputA.setData(new long[]{ 10L, 11L, 100L }, new int[] {3});
		this.int64InputB.setData(new long[]{ 5L, 3L, 20L }, new int[] {3});
		this.int64Output.setData(new long[]{ -1L, -1L, -1L }, new int[] {3});

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = this.generateInt64x3Instruction(OperationCode.DIV);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// 命令実行結果の値を検査
		long[] output = this.int64Output.getData();
		if (output[0] != 2L || output[1] != 3L || output[2] != 5L) {
			fail("Incorrect output");
		}

		// オペランドの個数が間違っている場合の検査
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.DIV);
			pc = this.dispatch(instruction, pc);
			fail("Unexpected exception occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}

	private void testDispatchRem() {

		// 入出力オペランドに値を設定
		this.int64InputA.setData(new long[]{ 10L, 11L, 100L }, new int[] {3});
		this.int64InputB.setData(new long[]{ 5L, 3L, 20L }, new int[] {3});
		this.int64Output.setData(new long[]{ -1L, -1L, -1L }, new int[] {3});

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = this.generateInt64x3Instruction(OperationCode.REM);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// 命令実行結果の値を検査
		long[] output = this.int64Output.getData();
		if (output[0] != 0L || output[1] != 2L || output[2] != 0L) {
			fail("Incorrect output");
		}

		// オペランドの個数が間違っている場合の検査
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.REM);
			pc = this.dispatch(instruction, pc);
			fail("Unexpected exception occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}

	private void testDispatchNeg() {

		// 入出力オペランドに値を設定
		this.int64InputA.setData(new long[]{ 1L, -2L, 3L }, new int[] {3});
		this.int64Output.setData(new long[]{ -1L, -1L, -1L }, new int[] {3});

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = this.generateInt64x2Instruction(OperationCode.NEG);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// 命令実行結果の値を検査
		long[] output = this.int64Output.getData();
		if (output[0] != -1L || output[1] != 2L || output[2] != -3L) {
			fail("Incorrect output");
		}

		// オペランドの個数が間違っている場合の検査
		try {
			instruction = this.generateInt64x3Instruction(OperationCode.NEG);
			pc = this.dispatch(instruction, pc);
			fail("Unexpected exception occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}

	private void testDispatchEq() {

		// 入出力オペランドに値を設定
		this.int64InputA.setData(new long[]{ 200L, 500L, 700L }, new int[] {3});
		this.int64InputB.setData(new long[]{ 100L, 500L, 800L }, new int[] {3});
		this.boolOutput.setData(new boolean[]{ false, false, false }, new int[] {3});

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = this.generateBoolx1Int64x2Instruction(OperationCode.EQ);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// 命令実行結果の値を検査
		boolean[] output = this.boolOutput.getData();
		if (output[0] != false || output[1] != true || output[2] != false) {
			fail("Incorrect output");
		}

		// オペランドの個数が間違っている場合の検査
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.EQ);
			pc = this.dispatch(instruction, pc);
			fail("Unexpected exception occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}

	private void testDispatchNeq() {

		// 入出力オペランドに値を設定
		this.int64InputA.setData(new long[]{ 200L, 500L, 700L }, new int[] {3});
		this.int64InputB.setData(new long[]{ 100L, 500L, 800L }, new int[] {3});
		this.boolOutput.setData(new boolean[]{ false, false, false }, new int[] {3});

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = this.generateBoolx1Int64x2Instruction(OperationCode.NEQ);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// 命令実行結果の値を検査
		boolean[] output = this.boolOutput.getData();
		if (output[0] != true || output[1] != false || output[2] != true) {
			fail("Incorrect output");
		}

		// オペランドの個数が間違っている場合の検査
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.NEQ);
			pc = this.dispatch(instruction, pc);
			fail("Unexpected exception occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}


	private void testDispatchGeq() {

		// 入出力オペランドに値を設定
		this.int64InputA.setData(new long[]{ 200L, 500L, 700L }, new int[] {3});
		this.int64InputB.setData(new long[]{ 100L, 500L, 800L }, new int[] {3});
		this.boolOutput.setData(new boolean[]{ false, false, false }, new int[] {3});

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = this.generateBoolx1Int64x2Instruction(OperationCode.GEQ);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// 命令実行結果の値を検査
		boolean[] output = this.boolOutput.getData();
		if (output[0] != true || output[1] != true || output[2] != false) {
			fail("Incorrect output");
		}

		// オペランドの個数が間違っている場合の検査
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.GEQ);
			pc = this.dispatch(instruction, pc);
			fail("Unexpected exception occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}


	private void testDispatchLeq() {

		// 入出力オペランドに値を設定
		this.int64InputA.setData(new long[]{ 200L, 500L, 700L }, new int[] {3});
		this.int64InputB.setData(new long[]{ 100L, 500L, 800L }, new int[] {3});
		this.boolOutput.setData(new boolean[]{ false, false, false }, new int[] {3});

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = this.generateBoolx1Int64x2Instruction(OperationCode.LEQ);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// 命令実行結果の値を検査
		boolean[] output = this.boolOutput.getData();
		if (output[0] != false || output[1] != true || output[2] != true) {
			fail("Incorrect output");
		}

		// オペランドの個数が間違っている場合の検査
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.LEQ);
			pc = this.dispatch(instruction, pc);
			fail("Unexpected exception occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}


	private void testDispatchGt() {

		// 入出力オペランドに値を設定
		this.int64InputA.setData(new long[]{ 200L, 500L, 700L }, new int[] {3});
		this.int64InputB.setData(new long[]{ 100L, 500L, 800L }, new int[] {3});
		this.boolOutput.setData(new boolean[]{ false, false, false }, new int[] {3});

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = this.generateBoolx1Int64x2Instruction(OperationCode.GT);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// 命令実行結果の値を検査
		boolean[] output = this.boolOutput.getData();
		if (output[0] != true || output[1] != false || output[2] != false) {
			fail("Incorrect output");
		}

		// オペランドの個数が間違っている場合の検査
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.GT);
			pc = this.dispatch(instruction, pc);
			fail("Unexpected exception occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}


	private void testDispatchLt() {

		// 入出力オペランドに値を設定
		this.int64InputA.setData(new long[]{ 200L, 500L, 700L }, new int[] {3});
		this.int64InputB.setData(new long[]{ 100L, 500L, 800L }, new int[] {3});
		this.boolOutput.setData(new boolean[]{ false, false, false }, new int[] {3});

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = this.generateBoolx1Int64x2Instruction(OperationCode.LT);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// 命令実行結果の値を検査
		boolean[] output = this.boolOutput.getData();
		if (output[0] != false || output[1] != false || output[2] != true) {
			fail("Incorrect output");
		}

		// オペランドの個数が間違っている場合の検査
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.LT);
			pc = this.dispatch(instruction, pc);
			fail("Unexpected exception occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}


	private void testDispatchAnd() {

		// 入出力オペランドに値を設定
		this.boolInputA.setData(new boolean[]{ false, true,  true }, new int[] {3});
		this.boolInputB.setData(new boolean[]{ false, false, true }, new int[] {3});
		this.boolOutput.setData(new boolean[]{ false, false, false }, new int[] {3});

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = this.generateBoolx3Instruction(OperationCode.AND);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// 命令実行結果の値を検査
		boolean[] output = this.boolOutput.getData();
		if (output[0] != false || output[1] != false || output[2] != true) {
			fail("Incorrect output");
		}

		// オペランドの個数が間違っている場合の検査
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.AND);
			pc = this.dispatch(instruction, pc);
			fail("Unexpected exception occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}

	private void testDispatchOr() {

		// 入出力オペランドに値を設定
		this.boolInputA.setData(new boolean[]{ false, true,  true }, new int[] {3});
		this.boolInputB.setData(new boolean[]{ false, false, true }, new int[] {3});
		this.boolOutput.setData(new boolean[]{ false, false, false }, new int[] {3});

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = this.generateBoolx3Instruction(OperationCode.OR);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// 命令実行結果の値を検査
		boolean[] output = this.boolOutput.getData();
		if (output[0] != false || output[1] != true || output[2] != true) {
			fail("Incorrect output");
		}

		// オペランドの個数が間違っている場合の検査
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.OR);
			pc = this.dispatch(instruction, pc);
			fail("Unexpected exception occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}

	private void testDispatchNot() {

		// 入出力オペランドに値を設定
		this.boolInputA.setData(new boolean[]{ false, true,  false }, new int[] {3});
		this.boolOutput.setData(new boolean[]{ false, false, false }, new int[] {3});

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = this.generateBoolx2Instruction(OperationCode.NOT);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// 命令実行結果の値を検査
		boolean[] output = this.boolOutput.getData();
		if (output[0] != true || output[1] != false || output[2] != true) {
			fail("Incorrect output");
		}

		// オペランドの個数が間違っている場合の検査
		try {
			instruction = this.generateInt64x2Instruction(OperationCode.NOT);
			pc = this.dispatch(instruction, pc);
			fail("Unexpected exception occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}
/*
	private void testDispatchVec() {

		// ベクトルにまとめるスカラを雑用アドレスに用意
		DataContainer<long[]> scalarA = new DataContainer<long[]>();
		DataContainer<long[]> scalarB = new DataContainer<long[]>();
		DataContainer<long[]> scalarC = new DataContainer<long[]>();
		this.memory.setDataContainer(TMP_A_PART, TMP_A_ADDR, scalarA);
		this.memory.setDataContainer(TMP_B_PART, TMP_B_ADDR, scalarB);
		this.memory.setDataContainer(TMP_C_PART, TMP_C_ADDR, scalarC);

		// 入出力オペランドに値を設定
		this.int64Output.setData(new long[]{ -1L, -1L, -1L });
		scalarA.setData(new long[]{ 11L });
		scalarB.setData(new long[]{ 22L });
		scalarC.setData(new long[]{ 33L });

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = new Instruction(
				OperationCode.VEC, INT64_TYPE,
				new Memory.Partition[]{ INT64_OUTPUT_PART, TMP_A_PART, TMP_B_PART, TMP_C_PART },
				new int[]{ INT64_OUTPUT_ADDR, TMP_A_ADDR, TMP_B_ADDR, TMP_C_ADDR },
				META_PART, META_ADDR
		);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = new DispatchUnit().dispatch(instruction, this.memory, this.interconnect, new ExecutionUnit(), pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// 命令実行結果の値を検査
		long[] output = this.int64Output.getData();
		if (output[0] != 11L || output[1] != 22L || output[2] != 33L) {
			fail("Incorrect output");
		}
	}
*/
	private void testDispatchMov() {

		// 入出力オペランドに値を設定
		this.int64InputA.setData(new long[]{ 11L, 22L, 33L }, new int[] {3});
		this.int64Output.setData(new long[]{ -1L, -1L, -1L }, new int[] {3});

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = this.generateInt64x2Instruction(OperationCode.MOV);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// 命令実行結果の値を検査
		long[] output = this.int64Output.getData();
		if (output[0] != 11L || output[1] != 22L || output[2] != 33L) {
			fail("Incorrect output");
		}

		// オペランドの個数が間違っている場合の検査
		try {
			instruction = this.generateInt64x3Instruction(OperationCode.MOV);
			pc = this.dispatch(instruction, pc);
			fail("Unexpected exception occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}

	private void testDispatchCast() {

		// キャスト元のFLOAT64データを雑用アドレスに用意
		DataContainer<double[]> float64Input = new DataContainer<double[]>();
		this.memory.setDataContainer(TMP_A_PART, TMP_A_ADDR, float64Input);

		// 入出力オペランドに値を設定
		float64Input.setData(new double[]{ 1.25, 2.25, 3.5 }, new int[] {3}); // 2進表現で割り切れる値
		this.int64Output.setData(new long[]{ -1L, -1L, -1L }, new int[] {3});

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = new Instruction(
				OperationCode.CAST,
				new DataType[]{ DataType.INT64, DataType.FLOAT64 },
				new Memory.Partition[]{ INT64_OUTPUT_PART, TMP_A_PART },
				new int[]{ INT64_OUTPUT_ADDR, TMP_A_ADDR },
				META_PART, META_ADDR
		);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// 命令実行結果の値を検査
		long[] output = this.int64Output.getData();
		if (output[0] != 1L || output[1] != 2L || output[2] != 3L) {
			fail("Incorrect output");
		}
	}

	private void testDispatchFill() {

		// ベクトルにまとめるスカラを雑用アドレスに用意
		DataContainer<long[]> scalar = new DataContainer<long[]>();
		this.memory.setDataContainer(TMP_A_PART, TMP_A_ADDR, scalar);

		// 入出力オペランドに値を設定
		this.int64Output.setData(new long[]{ -1L, -1L, -1L }, new int[] {3});
		scalar.setData(new long[]{ 123L }, new int[] {3});

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = new Instruction(
				OperationCode.FILL, INT64_TYPE,
				new Memory.Partition[]{ INT64_OUTPUT_PART, TMP_A_PART },
				new int[]{ INT64_OUTPUT_ADDR, TMP_A_ADDR },
				META_PART, META_ADDR
		);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// 命令実行結果の値を検査
		long[] output = this.int64Output.getData();
		if (output[0] != 123L || output[1] != 123L || output[2] != 123L) {
			fail("Incorrect output");
		}
	}

	private void testDispatchElem() {

		// 参照要素やインデックスを格納するコンテナを雑用アドレスに用意
		DataContainer<long[]> element = new DataContainer<long[]>();
		DataContainer<long[]> index = new DataContainer<long[]>();
		this.memory.setDataContainer(TMP_A_PART, TMP_A_ADDR, element);
		this.memory.setDataContainer(TMP_B_PART, TMP_B_ADDR, index);

		// 入出力オペランドに値を設定
		this.int64InputA.setData(new long[]{ 11L, 22L, 33L }, new int[] {3}); // 要素を参照する配列
		index.setData(new long[]{ 1L }, 0);
		element.setData(new long[]{ -1L }, 0);

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = new Instruction(
				OperationCode.ELEM, INT64_TYPE,
				new Memory.Partition[]{ TMP_A_PART, INT64_INPUT_A_PART, TMP_B_PART },
				new int[]{ TMP_A_ADDR, INT64_INPUT_A_ADDR, TMP_B_ADDR },
				META_PART, META_ADDR
		);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// 命令実行結果の値を検査
		if (element.getData()[ element.getOffset() ] != 22L) {
			fail("Incorrect output");
		}
	}

	/*
	private void testDispatchLen() {

		// 配列や要素数を格納するコンテナを雑用アドレスに用意
		DataContainer<long[]> len = new DataContainer<long[]>();
		DataContainer<long[]> array = new DataContainer<long[]>();
		int[] lenLengths = new int[]{ 3 };
		int[] arrayLengths = new int[]{ 2, 3, 4 };
		this.memory.setDataContainer(TMP_A_PART, TMP_A_ADDR, len);
		this.memory.setDataContainer(TMP_B_PART, TMP_B_ADDR, array);

		// 入出力オペランドに値を設定
		array.setData(new long[]{0L,1L,2L,3L,4L,5L,6L,7L,8L,9L,10L,11L,12L,13L,14L,15L,16L,17L,18L,19L,20L,21L,22L,23L}, arrayLengths);
		len.setData(new long[]{ -1L, -1L, -1L }, lenLengths);

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = new Instruction(
				OperationCode.LEN, INT64_TYPE,
				new Memory.Partition[]{ TMP_A_PART, TMP_B_PART },
				new int[]{ TMP_A_ADDR, TMP_B_ADDR },
				META_PART, META_ADDR
		);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = new DispatchUnit().dispatch(instruction, this.memory, this.interconnect, new ExecutionUnit(), pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// 命令実行結果の値を検査
		long[] output = len.getData();
		if (output[0] != 2L || output[1] != 3L || output[2] != 4L) {
			fail("Incorrect output");
		}
	}
*/

	private void testDispatchFree() {

		// 解放するコンテナを雑用アドレスに用意
		DataContainer<long[]> target = new DataContainer<long[]>();
		int[] targetLengths = new int[]{ 4 }; // 5-offset
		this.memory.setDataContainer(TMP_A_PART, TMP_A_ADDR, target);

		// データを格納
		long[] data = new long[]{ 1L, 2L, 3L, 4L, 5L };
		target.setData(data, targetLengths);

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = new Instruction(
				OperationCode.FREE, INT64_TYPE,
				new Memory.Partition[]{ TMP_A_PART },
				new int[]{ TMP_A_ADDR },
				META_PART, META_ADDR
		);

		// データ解放前の状態を念のため検査
		if (target.getData() != data
				|| target.getOffset() != 0
				|| target.getSize() != 4
				|| target.getLengths()[0] != 4) {

			fail("Incorrect output");
		}

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// データが解放されてコンテナが初期状態になっている事を検査
		if (target.getData() != null
				|| target.getSize() != 1
				|| target.getLengths().length != 0
				|| target.getOffset() != 0) {

			fail("Incorrect output");
		}
	}


	private void testDispatchJmp() {

		// 分岐条件や分岐先アドレスを格納するコンテナを雑用アドレスに用意
		DataContainer<boolean[]> condition = new DataContainer<boolean[]>();
		DataContainer<long[]> jumpAddress = new DataContainer<long[]>();
		this.memory.setDataContainer(TMP_A_PART, TMP_A_ADDR, jumpAddress);
		this.memory.setDataContainer(TMP_B_PART, TMP_B_ADDR, condition);

		// 入出力オペランドに値を設定
		condition.setData(new boolean[]{ true }, new int[] {3});
		jumpAddress.setData(new long[]{ 256L }, new int[] {3});

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = new Instruction(
				OperationCode.JMP, BOOL_TYPE,
				new Memory.Partition[]{ Memory.Partition.NONE, TMP_A_PART, TMP_B_PART },
				new int[]{ 0, TMP_A_ADDR, TMP_B_ADDR },
				META_PART, META_ADDR
		);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 256) {
			fail("Incorrect program counter");
		}

		// 分岐条件を変更して再実行
		condition.setData(new boolean[]{ false }, new int[] {3});
		pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}
	}


	private void testDispatchJmpn() {

		// 分岐条件や分岐先アドレスを格納するコンテナを雑用アドレスに用意
		DataContainer<boolean[]> condition = new DataContainer<boolean[]>();
		DataContainer<long[]> jumpAddress = new DataContainer<long[]>();
		this.memory.setDataContainer(TMP_A_PART, TMP_A_ADDR, jumpAddress);
		this.memory.setDataContainer(TMP_B_PART, TMP_B_ADDR, condition);

		// 入出力オペランドに値を設定
		condition.setData(new boolean[]{ true }, new int[] {3});
		jumpAddress.setData(new long[]{ 256L }, new int[] {3});

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = new Instruction(
				OperationCode.JMPN, BOOL_TYPE,
				new Memory.Partition[]{ Memory.Partition.NONE, TMP_A_PART, TMP_B_PART },
				new int[]{ 0, TMP_A_ADDR, TMP_B_ADDR },
				META_PART, META_ADDR
		);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// 分岐条件を変更して再実行
		condition.setData(new boolean[]{ false }, new int[] {3});
		pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 256) {
			fail("Incorrect program counter");
		}
	}

	// 処理系から関数として呼ぶメソッド
	public long methodToConnect(long a, long b) {
		this.connectedMethodCalled = true;
		return a + b;
	}

	private void testDispatchAllocScalar() {

		// データ確保対象のコンテナを雑用アドレスに用意
		DataContainer<long[]> target = new DataContainer<long[]>();
		this.memory.setDataContainer(TMP_A_PART, TMP_A_ADDR, target);

		// 1オペランドのALLOC命令（スカラ確保）を生成
		Instruction instruction = new Instruction(
				OperationCode.ALLOC, INT64_TYPE,
				new Memory.Partition[]{ TMP_A_PART },
				new int[]{ TMP_A_ADDR },
				META_PART, META_ADDR
		);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		this.connectedMethodCalled = false;
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// 確保されている事を検査
		if (!(target.getData() instanceof long[])) {
			fail("Incorrect type of data");
		}
		if (target.getSize() != 1) {
			fail("Incorrect size");
		}
		if (target.getRank() != 0) {
			fail("Incorrect rank");
		}
		if (target.getLengths().length != 0) {
			fail("Incorrect lengths");
		}
		long[] targetData = target.getData();
		if (!(0 < targetData.length) || !(target.getSize()+target.getOffset() <= targetData.length)) {
			fail("Incorrect length of data");
		}
	}

	private void testDispatchAllocVector() {

		// データ確保対象および要素数指定のコンテナを雑用アドレスに用意
		DataContainer<long[]> target = new DataContainer<long[]>();
		DataContainer<long[]> len0 = new DataContainer<long[]>();
		DataContainer<long[]> len1 = new DataContainer<long[]>();
		DataContainer<long[]> len2 = new DataContainer<long[]>();
		this.memory.setDataContainer(TMP_A_PART, TMP_A_ADDR, target);
		this.memory.setDataContainer(TMP_B_PART, TMP_B_ADDR, len0);
		this.memory.setDataContainer(TMP_C_PART, TMP_C_ADDR, len1);
		this.memory.setDataContainer(TMP_D_PART, TMP_D_ADDR, len2);

		// 要素数指定値を設定（ [2][3][4] ）
		len0.setData(new long[] { 2L }, 0);
		len1.setData(new long[] { 3L }, 0);
		len2.setData(new long[] { 4L }, 0);

		// ALLOC命令（スカラ確保）を生成
		Instruction instruction = new Instruction(
				OperationCode.ALLOC, INT64_TYPE,
				new Memory.Partition[]{ TMP_A_PART, TMP_B_PART, TMP_C_PART, TMP_D_PART },
				new int[]{ TMP_A_ADDR, TMP_B_ADDR, TMP_C_ADDR, TMP_D_ADDR },
				META_PART, META_ADDR
		);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		this.connectedMethodCalled = false;
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// 確保されている事を検査
		if (!(target.getData() instanceof long[])) {
			fail("Incorrect type of instance");
		}
		if (target.getSize() != 24) {
			fail("Incorrect size");
		}
		if (target.getRank() != 3) {
			fail("Incorrect rank");
		}
		if (target.getLengths()[0] != 2 || target.getLengths()[1] != 3L || target.getLengths()[2] != 4L) {
			fail("Incorrect lengths");
		}
		long[] targetData = target.getData();
		if (!(0 < targetData.length) || !(target.getSize()+target.getOffset() <= targetData.length)) {
			fail("Incorrect actual length of data");
		}
	}

	private void testCall() {

		// 処理系からコールするメソッドをリフレクションで用意
		Method method = null;
		try {
			method = this.getClass().getMethod("methodToConnect", long.class, long.class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			fail("Reflection failed");
		}

		// 処理系にメソッドを接続
		try {
			this.interconnect.connectPlugin(SpecialBindingKey.AUTO_KEY, new Object[] {method,this} );
		} catch (VnanoException e) {
			fail("Connection failed");
		}

		// 接続された、処理系内部形式の関数オブジェクトを取得（関数テーブルからアドレスを引き出すのに使用）
		AbstractFunction function = this.interconnect.getExternalFunctionTable().getFunctionBySignature(
				"methodToConnect",
				new DataType[]{ DataType.INT64, DataType.INT64 },
				new int[]{ 0, 0 },
				new boolean[] { false, false },
				new boolean[] { false, false },
				false, false
		);

		// 引数・戻り値・関数アドレスを格納するオペランドを用意して値を設定
		DataContainer<long[]> argA = new DataContainer<long[]>();
		DataContainer<long[]> argB = new DataContainer<long[]>();
		DataContainer<long[]> ret = new DataContainer<long[]>();
		DataContainer<long[]> functionAddress = new DataContainer<long[]>();
		this.memory.setDataContainer(TMP_A_PART, TMP_A_ADDR, argA);
		this.memory.setDataContainer(TMP_B_PART, TMP_B_ADDR, argB);
		this.memory.setDataContainer(TMP_C_PART, TMP_C_ADDR, ret);
		this.memory.setDataContainer(TMP_D_PART, TMP_D_ADDR, functionAddress);
		argA.setData(new long[]{ 123L }, 0);
		argB.setData(new long[]{ 456L }, 0);
		ret.setData(new long[]{ -1 }, 0);
		functionAddress.setData(new long[]{ this.interconnect.getExternalFunctionTable().indexOf(function) }, 0);

		// 上記オペランドでメソッドコールを行う命令を生成
		Instruction instruction = new Instruction(
				OperationCode.CALLX, INT64_TYPE,
				new Memory.Partition[]{ TMP_C_PART, TMP_D_PART, TMP_A_PART, TMP_B_PART },
				new int[]{ TMP_C_ADDR, TMP_D_ADDR, TMP_A_ADDR, TMP_B_ADDR },
				META_PART, META_ADDR
		);

		// 命令を実行
		int pc = 10; // プログラムカウンタ
		this.connectedMethodCalled = false;
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// メソッドがコールされたかどうか検査
		if (!this.connectedMethodCalled) {
			fail("Method did not called");
		}

		// 戻り値の値を検査（コールしたメソッドは2つの引数の和、つまり 123 + 456 を返す）
		if(ret.getData()[0] != 579) {
			fail("Incorrect returned value");
		}
	}

	private void testDispatchNop() {

		// 上記オペランドで演算を行う命令を生成
		//Instruction instruction = this.generateInt64x3Instruction(OperationCode.ADD);

		// 上記オペランドで演算を行う命令を生成
		Instruction instruction = new Instruction(
				OperationCode.NOP, VOID_TYPE,
				new Memory.Partition[] {Memory.Partition.NONE}, new int[] {0},
				META_PART, META_ADDR
		);


		// 命令を実行
		int pc = 10; // プログラムカウンタ
		try {
			pc = this.dispatch(instruction, pc);
		} catch (VnanoException | VnanoFatalException e) {
			e.printStackTrace();
			fail("Unexpected exception occurred");
		}

		// プログラムカウンタの更新値を検査
		if (pc != 11) {
			fail("Incorrect program counter");
		}

		// オペランドの個数が間違っている場合の検査
		try {
			instruction = this.generateInt64x3Instruction(OperationCode.NOP);
			pc = this.dispatch(instruction, pc);
			fail("Unexpected exception occurred");
		} catch (VnanoException | VnanoFatalException e) {
			// 例外が発生するのが正しい挙動
		}
	}




}
