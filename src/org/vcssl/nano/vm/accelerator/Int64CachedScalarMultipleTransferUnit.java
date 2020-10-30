/*
 * Copyright(C) 2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import java.util.HashSet;
import java.util.Set;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.DataContainer;

public class Int64CachedScalarMultipleTransferUnit extends AcceleratorExecutionUnit {

	public static final Set<OperationCode> AVAILABLE_OPERAND_SET = new HashSet<OperationCode>();
	static {
		AVAILABLE_OPERAND_SET.add(OperationCode.MOV);
	}

	public static final int MAX_AVAILABLE_TRANSFER_COUNT = 10;


	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		AcceleratorExecutionNode node = null;
		OperationCode[] fusedOpcodes = instruction.getFusedOperationCodes();

		// 全オペランドが MOV かどうか検査
		boolean isAllMov = true;
		for (OperationCode fusedOpcode: fusedOpcodes) {
			isAllMov = isAllMov & fusedOpcode == OperationCode.MOV;
		}

		// 全オペランドが MOV の場合
		//（別のオペコードに対応した際は、同様に isAll～ を調べて else if として追加していく）
		if (isAllMov) {

			int transferCount = operandContainers.length / 2; // 一括で転送を行う回数 ( = オペランド内の dest と src のペアの数)
			switch (transferCount) {
				case 2: {
					node = new Int64CachedScalarMovX2Node(
						(Int64ScalarCache)operandCaches[0], (Int64ScalarCache)operandCaches[1], // dest, src
						(Int64ScalarCache)operandCaches[2], (Int64ScalarCache)operandCaches[3], // dest, src
						nextNode
					);
					break;
				}
				case 3: {
					node = new Int64CachedScalarMovX3Node(
						(Int64ScalarCache)operandCaches[0], (Int64ScalarCache)operandCaches[1], // dest, src
						(Int64ScalarCache)operandCaches[2], (Int64ScalarCache)operandCaches[3], // dest, src
						(Int64ScalarCache)operandCaches[4], (Int64ScalarCache)operandCaches[5], // dest, src
						nextNode
					);
					break;
				}
				case 4: {
					node = new Int64CachedScalarMovX4Node(
						(Int64ScalarCache)operandCaches[0], (Int64ScalarCache)operandCaches[1], // dest, src
						(Int64ScalarCache)operandCaches[2], (Int64ScalarCache)operandCaches[3], // dest, src
						(Int64ScalarCache)operandCaches[4], (Int64ScalarCache)operandCaches[5], // dest, src
						(Int64ScalarCache)operandCaches[6], (Int64ScalarCache)operandCaches[7], // dest, src
						nextNode
					);
					break;
				}
				case 5: {
					node = new Int64CachedScalarMovX5Node(
						(Int64ScalarCache)operandCaches[0], (Int64ScalarCache)operandCaches[1], // dest, src
						(Int64ScalarCache)operandCaches[2], (Int64ScalarCache)operandCaches[3], // dest, src
						(Int64ScalarCache)operandCaches[4], (Int64ScalarCache)operandCaches[5], // dest, src
						(Int64ScalarCache)operandCaches[6], (Int64ScalarCache)operandCaches[7], // dest, src
						(Int64ScalarCache)operandCaches[8], (Int64ScalarCache)operandCaches[9], // dest, src
						nextNode
					);
					break;
				}
				case 6: {
					node = new Int64CachedScalarMovX6Node(
						(Int64ScalarCache)operandCaches[0], (Int64ScalarCache)operandCaches[1],   // dest, src
						(Int64ScalarCache)operandCaches[2], (Int64ScalarCache)operandCaches[3],   // dest, src
						(Int64ScalarCache)operandCaches[4], (Int64ScalarCache)operandCaches[5],   // dest, src
						(Int64ScalarCache)operandCaches[6], (Int64ScalarCache)operandCaches[7],   // dest, src
						(Int64ScalarCache)operandCaches[8], (Int64ScalarCache)operandCaches[9],   // dest, src
						(Int64ScalarCache)operandCaches[10], (Int64ScalarCache)operandCaches[11], // dest, src
						nextNode
					);
					break;
				}
				case 7: {
					node = new Int64CachedScalarMovX7Node(
						(Int64ScalarCache)operandCaches[0], (Int64ScalarCache)operandCaches[1],   // dest, src
						(Int64ScalarCache)operandCaches[2], (Int64ScalarCache)operandCaches[3],   // dest, src
						(Int64ScalarCache)operandCaches[4], (Int64ScalarCache)operandCaches[5],   // dest, src
						(Int64ScalarCache)operandCaches[6], (Int64ScalarCache)operandCaches[7],   // dest, src
						(Int64ScalarCache)operandCaches[8], (Int64ScalarCache)operandCaches[9],   // dest, src
						(Int64ScalarCache)operandCaches[10], (Int64ScalarCache)operandCaches[11], // dest, src
						(Int64ScalarCache)operandCaches[12], (Int64ScalarCache)operandCaches[13], // dest, src
						nextNode
					);
					break;
				}
				case 8: {
					node = new Int64CachedScalarMovX8Node(
						(Int64ScalarCache)operandCaches[0], (Int64ScalarCache)operandCaches[1],   // dest, src
						(Int64ScalarCache)operandCaches[2], (Int64ScalarCache)operandCaches[3],   // dest, src
						(Int64ScalarCache)operandCaches[4], (Int64ScalarCache)operandCaches[5],   // dest, src
						(Int64ScalarCache)operandCaches[6], (Int64ScalarCache)operandCaches[7],   // dest, src
						(Int64ScalarCache)operandCaches[8], (Int64ScalarCache)operandCaches[9],   // dest, src
						(Int64ScalarCache)operandCaches[10], (Int64ScalarCache)operandCaches[11], // dest, src
						(Int64ScalarCache)operandCaches[12], (Int64ScalarCache)operandCaches[13], // dest, src
						(Int64ScalarCache)operandCaches[14], (Int64ScalarCache)operandCaches[15], // dest, src
						nextNode
					);
					break;
				}
				case 9: {
					node = new Int64CachedScalarMovX9Node(
						(Int64ScalarCache)operandCaches[0], (Int64ScalarCache)operandCaches[1],   // dest, src
						(Int64ScalarCache)operandCaches[2], (Int64ScalarCache)operandCaches[3],   // dest, src
						(Int64ScalarCache)operandCaches[4], (Int64ScalarCache)operandCaches[5],   // dest, src
						(Int64ScalarCache)operandCaches[6], (Int64ScalarCache)operandCaches[7],   // dest, src
						(Int64ScalarCache)operandCaches[8], (Int64ScalarCache)operandCaches[9],   // dest, src
						(Int64ScalarCache)operandCaches[10], (Int64ScalarCache)operandCaches[11], // dest, src
						(Int64ScalarCache)operandCaches[12], (Int64ScalarCache)operandCaches[13], // dest, src
						(Int64ScalarCache)operandCaches[14], (Int64ScalarCache)operandCaches[15], // dest, src
						(Int64ScalarCache)operandCaches[16], (Int64ScalarCache)operandCaches[17], // dest, src
						nextNode
					);
					break;
				}
				case 10: {
					node = new Int64CachedScalarMovX10Node(
						(Int64ScalarCache)operandCaches[0], (Int64ScalarCache)operandCaches[1],   // dest, src
						(Int64ScalarCache)operandCaches[2], (Int64ScalarCache)operandCaches[3],   // dest, src
						(Int64ScalarCache)operandCaches[4], (Int64ScalarCache)operandCaches[5],   // dest, src
						(Int64ScalarCache)operandCaches[6], (Int64ScalarCache)operandCaches[7],   // dest, src
						(Int64ScalarCache)operandCaches[8], (Int64ScalarCache)operandCaches[9],   // dest, src
						(Int64ScalarCache)operandCaches[10], (Int64ScalarCache)operandCaches[11], // dest, src
						(Int64ScalarCache)operandCaches[12], (Int64ScalarCache)operandCaches[13], // dest, src
						(Int64ScalarCache)operandCaches[14], (Int64ScalarCache)operandCaches[15], // dest, src
						(Int64ScalarCache)operandCaches[16], (Int64ScalarCache)operandCaches[17], // dest, src
						(Int64ScalarCache)operandCaches[18], (Int64ScalarCache)operandCaches[19], // dest, src
						nextNode
					);
					break;
				}
				default: {
					throw new VnanoFatalException(
						"The plural transfer x" + transferCount + " is not supported by "+ this.getClass().getCanonicalName()
					);
				}
			}

		} else {
			throw new VnanoFatalException(
				"Unsupported fused operation codes has been detected in " + this.getClass().getCanonicalName()
			);
		}
		return node;
	}

	private class Int64CachedScalarMovX2Node extends AcceleratorExecutionNode {
		protected final Int64ScalarCache dest0;
		protected final Int64ScalarCache src0;
		protected final Int64ScalarCache dest1;
		protected final Int64ScalarCache src1;

		public Int64CachedScalarMovX2Node(
				Int64ScalarCache dest0, Int64ScalarCache src0,
				Int64ScalarCache dest1, Int64ScalarCache src1,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2); // 第二引数は一括処理命令数
			this.src0 = src0;
			this.src1 = src1;
			this.dest0 = dest0;
			this.dest1 = dest1;
		}

		public final AcceleratorExecutionNode execute() {
			this.dest0.value = this.src0.value;
			this.dest1.value = this.src1.value;
			return this.nextNode;
		}
	}

	private class Int64CachedScalarMovX3Node extends AcceleratorExecutionNode {
		protected final Int64ScalarCache dest0;
		protected final Int64ScalarCache src0;
		protected final Int64ScalarCache dest1;
		protected final Int64ScalarCache src1;
		protected final Int64ScalarCache dest2;
		protected final Int64ScalarCache src2;

		public Int64CachedScalarMovX3Node(
				Int64ScalarCache dest0, Int64ScalarCache src0,
				Int64ScalarCache dest1, Int64ScalarCache src1,
				Int64ScalarCache dest2, Int64ScalarCache src2,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 3);
			this.src0 = src0;
			this.src1 = src1;
			this.src2 = src2;
			this.dest0 = dest0;
			this.dest1 = dest1;
			this.dest2 = dest2;
		}

		public final AcceleratorExecutionNode execute() {
			this.dest0.value = this.src0.value;
			this.dest1.value = this.src1.value;
			this.dest2.value = this.src2.value;
			return this.nextNode;
		}
	}

	private class Int64CachedScalarMovX4Node extends AcceleratorExecutionNode {
		protected final Int64ScalarCache dest0;
		protected final Int64ScalarCache src0;
		protected final Int64ScalarCache dest1;
		protected final Int64ScalarCache src1;
		protected final Int64ScalarCache dest2;
		protected final Int64ScalarCache src2;
		protected final Int64ScalarCache dest3;
		protected final Int64ScalarCache src3;

		public Int64CachedScalarMovX4Node(
				Int64ScalarCache dest0, Int64ScalarCache src0,
				Int64ScalarCache dest1, Int64ScalarCache src1,
				Int64ScalarCache dest2, Int64ScalarCache src2,
				Int64ScalarCache dest3, Int64ScalarCache src3,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 4);
			this.src0 = src0;
			this.src1 = src1;
			this.src2 = src2;
			this.src3 = src3;
			this.dest0 = dest0;
			this.dest1 = dest1;
			this.dest2 = dest2;
			this.dest3 = dest3;
		}

		public final AcceleratorExecutionNode execute() {
			this.dest0.value = this.src0.value;
			this.dest1.value = this.src1.value;
			this.dest2.value = this.src2.value;
			this.dest3.value = this.src3.value;
			return this.nextNode;
		}
	}

	private class Int64CachedScalarMovX5Node extends AcceleratorExecutionNode {
		protected final Int64ScalarCache dest0;
		protected final Int64ScalarCache src0;
		protected final Int64ScalarCache dest1;
		protected final Int64ScalarCache src1;
		protected final Int64ScalarCache dest2;
		protected final Int64ScalarCache src2;
		protected final Int64ScalarCache dest3;
		protected final Int64ScalarCache src3;
		protected final Int64ScalarCache dest4;
		protected final Int64ScalarCache src4;

		public Int64CachedScalarMovX5Node(
				Int64ScalarCache dest0, Int64ScalarCache src0,
				Int64ScalarCache dest1, Int64ScalarCache src1,
				Int64ScalarCache dest2, Int64ScalarCache src2,
				Int64ScalarCache dest3, Int64ScalarCache src3,
				Int64ScalarCache dest4, Int64ScalarCache src4,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 5);
			this.src0 = src0;
			this.src1 = src1;
			this.src2 = src2;
			this.src3 = src3;
			this.src4 = src4;
			this.dest0 = dest0;
			this.dest1 = dest1;
			this.dest2 = dest2;
			this.dest3 = dest3;
			this.dest4 = dest4;
		}

		public final AcceleratorExecutionNode execute() {
			this.dest0.value = this.src0.value;
			this.dest1.value = this.src1.value;
			this.dest2.value = this.src2.value;
			this.dest3.value = this.src3.value;
			this.dest4.value = this.src4.value;
			return this.nextNode;
		}
	}

	private class Int64CachedScalarMovX6Node extends AcceleratorExecutionNode {
		protected final Int64ScalarCache dest0;
		protected final Int64ScalarCache src0;
		protected final Int64ScalarCache dest1;
		protected final Int64ScalarCache src1;
		protected final Int64ScalarCache dest2;
		protected final Int64ScalarCache src2;
		protected final Int64ScalarCache dest3;
		protected final Int64ScalarCache src3;
		protected final Int64ScalarCache dest4;
		protected final Int64ScalarCache src4;
		protected final Int64ScalarCache dest5;
		protected final Int64ScalarCache src5;

		public Int64CachedScalarMovX6Node(
				Int64ScalarCache dest0, Int64ScalarCache src0,
				Int64ScalarCache dest1, Int64ScalarCache src1,
				Int64ScalarCache dest2, Int64ScalarCache src2,
				Int64ScalarCache dest3, Int64ScalarCache src3,
				Int64ScalarCache dest4, Int64ScalarCache src4,
				Int64ScalarCache dest5, Int64ScalarCache src5,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 6);
			this.src0 = src0;
			this.src1 = src1;
			this.src2 = src2;
			this.src3 = src3;
			this.src4 = src4;
			this.src5 = src5;
			this.dest0 = dest0;
			this.dest1 = dest1;
			this.dest2 = dest2;
			this.dest3 = dest3;
			this.dest4 = dest4;
			this.dest5 = dest5;
		}

		public final AcceleratorExecutionNode execute() {
			this.dest0.value = this.src0.value;
			this.dest1.value = this.src1.value;
			this.dest2.value = this.src2.value;
			this.dest3.value = this.src3.value;
			this.dest4.value = this.src4.value;
			this.dest5.value = this.src5.value;
			return this.nextNode;
		}
	}

	private class Int64CachedScalarMovX7Node extends AcceleratorExecutionNode {
		protected final Int64ScalarCache dest0;
		protected final Int64ScalarCache src0;
		protected final Int64ScalarCache dest1;
		protected final Int64ScalarCache src1;
		protected final Int64ScalarCache dest2;
		protected final Int64ScalarCache src2;
		protected final Int64ScalarCache dest3;
		protected final Int64ScalarCache src3;
		protected final Int64ScalarCache dest4;
		protected final Int64ScalarCache src4;
		protected final Int64ScalarCache dest5;
		protected final Int64ScalarCache src5;
		protected final Int64ScalarCache dest6;
		protected final Int64ScalarCache src6;

		public Int64CachedScalarMovX7Node(
				Int64ScalarCache dest0, Int64ScalarCache src0,
				Int64ScalarCache dest1, Int64ScalarCache src1,
				Int64ScalarCache dest2, Int64ScalarCache src2,
				Int64ScalarCache dest3, Int64ScalarCache src3,
				Int64ScalarCache dest4, Int64ScalarCache src4,
				Int64ScalarCache dest5, Int64ScalarCache src5,
				Int64ScalarCache dest6, Int64ScalarCache src6,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 7);
			this.src0 = src0;
			this.src1 = src1;
			this.src2 = src2;
			this.src3 = src3;
			this.src4 = src4;
			this.src5 = src5;
			this.src6 = src6;
			this.dest0 = dest0;
			this.dest1 = dest1;
			this.dest2 = dest2;
			this.dest3 = dest3;
			this.dest4 = dest4;
			this.dest5 = dest5;
			this.dest6 = dest6;
		}

		public final AcceleratorExecutionNode execute() {
			this.dest0.value = this.src0.value;
			this.dest1.value = this.src1.value;
			this.dest2.value = this.src2.value;
			this.dest3.value = this.src3.value;
			this.dest4.value = this.src4.value;
			this.dest5.value = this.src5.value;
			this.dest6.value = this.src6.value;
			return this.nextNode;
		}
	}

	private class Int64CachedScalarMovX8Node extends AcceleratorExecutionNode {
		protected final Int64ScalarCache dest0;
		protected final Int64ScalarCache src0;
		protected final Int64ScalarCache dest1;
		protected final Int64ScalarCache src1;
		protected final Int64ScalarCache dest2;
		protected final Int64ScalarCache src2;
		protected final Int64ScalarCache dest3;
		protected final Int64ScalarCache src3;
		protected final Int64ScalarCache dest4;
		protected final Int64ScalarCache src4;
		protected final Int64ScalarCache dest5;
		protected final Int64ScalarCache src5;
		protected final Int64ScalarCache dest6;
		protected final Int64ScalarCache src6;
		protected final Int64ScalarCache dest7;
		protected final Int64ScalarCache src7;

		public Int64CachedScalarMovX8Node(
				Int64ScalarCache dest0, Int64ScalarCache src0,
				Int64ScalarCache dest1, Int64ScalarCache src1,
				Int64ScalarCache dest2, Int64ScalarCache src2,
				Int64ScalarCache dest3, Int64ScalarCache src3,
				Int64ScalarCache dest4, Int64ScalarCache src4,
				Int64ScalarCache dest5, Int64ScalarCache src5,
				Int64ScalarCache dest6, Int64ScalarCache src6,
				Int64ScalarCache dest7, Int64ScalarCache src7,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 8);
			this.src0 = src0;
			this.src1 = src1;
			this.src2 = src2;
			this.src3 = src3;
			this.src4 = src4;
			this.src5 = src5;
			this.src6 = src6;
			this.src7 = src7;
			this.dest0 = dest0;
			this.dest1 = dest1;
			this.dest2 = dest2;
			this.dest3 = dest3;
			this.dest4 = dest4;
			this.dest5 = dest5;
			this.dest6 = dest6;
			this.dest7 = dest7;
		}

		public final AcceleratorExecutionNode execute() {
			this.dest0.value = this.src0.value;
			this.dest1.value = this.src1.value;
			this.dest2.value = this.src2.value;
			this.dest3.value = this.src3.value;
			this.dest4.value = this.src4.value;
			this.dest5.value = this.src5.value;
			this.dest6.value = this.src6.value;
			this.dest7.value = this.src7.value;
			return this.nextNode;
		}
	}

	private class Int64CachedScalarMovX9Node extends AcceleratorExecutionNode {
		protected final Int64ScalarCache dest0;
		protected final Int64ScalarCache src0;
		protected final Int64ScalarCache dest1;
		protected final Int64ScalarCache src1;
		protected final Int64ScalarCache dest2;
		protected final Int64ScalarCache src2;
		protected final Int64ScalarCache dest3;
		protected final Int64ScalarCache src3;
		protected final Int64ScalarCache dest4;
		protected final Int64ScalarCache src4;
		protected final Int64ScalarCache dest5;
		protected final Int64ScalarCache src5;
		protected final Int64ScalarCache dest6;
		protected final Int64ScalarCache src6;
		protected final Int64ScalarCache dest7;
		protected final Int64ScalarCache src7;
		protected final Int64ScalarCache dest8;
		protected final Int64ScalarCache src8;

		public Int64CachedScalarMovX9Node(
				Int64ScalarCache dest0, Int64ScalarCache src0,
				Int64ScalarCache dest1, Int64ScalarCache src1,
				Int64ScalarCache dest2, Int64ScalarCache src2,
				Int64ScalarCache dest3, Int64ScalarCache src3,
				Int64ScalarCache dest4, Int64ScalarCache src4,
				Int64ScalarCache dest5, Int64ScalarCache src5,
				Int64ScalarCache dest6, Int64ScalarCache src6,
				Int64ScalarCache dest7, Int64ScalarCache src7,
				Int64ScalarCache dest8, Int64ScalarCache src8,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 9);
			this.src0 = src0;
			this.src1 = src1;
			this.src2 = src2;
			this.src3 = src3;
			this.src4 = src4;
			this.src5 = src5;
			this.src6 = src6;
			this.src7 = src7;
			this.src8 = src8;
			this.dest0 = dest0;
			this.dest1 = dest1;
			this.dest2 = dest2;
			this.dest3 = dest3;
			this.dest4 = dest4;
			this.dest5 = dest5;
			this.dest6 = dest6;
			this.dest7 = dest7;
			this.dest8 = dest8;
		}

		public final AcceleratorExecutionNode execute() {
			this.dest0.value = this.src0.value;
			this.dest1.value = this.src1.value;
			this.dest2.value = this.src2.value;
			this.dest3.value = this.src3.value;
			this.dest4.value = this.src4.value;
			this.dest5.value = this.src5.value;
			this.dest6.value = this.src6.value;
			this.dest7.value = this.src7.value;
			this.dest8.value = this.src8.value;
			return this.nextNode;
		}
	}

	private class Int64CachedScalarMovX10Node extends AcceleratorExecutionNode {
		protected final Int64ScalarCache dest0;
		protected final Int64ScalarCache src0;
		protected final Int64ScalarCache dest1;
		protected final Int64ScalarCache src1;
		protected final Int64ScalarCache dest2;
		protected final Int64ScalarCache src2;
		protected final Int64ScalarCache dest3;
		protected final Int64ScalarCache src3;
		protected final Int64ScalarCache dest4;
		protected final Int64ScalarCache src4;
		protected final Int64ScalarCache dest5;
		protected final Int64ScalarCache src5;
		protected final Int64ScalarCache dest6;
		protected final Int64ScalarCache src6;
		protected final Int64ScalarCache dest7;
		protected final Int64ScalarCache src7;
		protected final Int64ScalarCache dest8;
		protected final Int64ScalarCache src8;
		protected final Int64ScalarCache dest9;
		protected final Int64ScalarCache src9;

		public Int64CachedScalarMovX10Node(
				Int64ScalarCache dest0, Int64ScalarCache src0,
				Int64ScalarCache dest1, Int64ScalarCache src1,
				Int64ScalarCache dest2, Int64ScalarCache src2,
				Int64ScalarCache dest3, Int64ScalarCache src3,
				Int64ScalarCache dest4, Int64ScalarCache src4,
				Int64ScalarCache dest5, Int64ScalarCache src5,
				Int64ScalarCache dest6, Int64ScalarCache src6,
				Int64ScalarCache dest7, Int64ScalarCache src7,
				Int64ScalarCache dest8, Int64ScalarCache src8,
				Int64ScalarCache dest9, Int64ScalarCache src9,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 10);
			this.src0 = src0;
			this.src1 = src1;
			this.src2 = src2;
			this.src3 = src3;
			this.src4 = src4;
			this.src5 = src5;
			this.src6 = src6;
			this.src7 = src7;
			this.src8 = src8;
			this.src9 = src9;
			this.dest0 = dest0;
			this.dest1 = dest1;
			this.dest2 = dest2;
			this.dest3 = dest3;
			this.dest4 = dest4;
			this.dest5 = dest5;
			this.dest6 = dest6;
			this.dest7 = dest7;
			this.dest8 = dest8;
			this.dest9 = dest9;
		}

		public final AcceleratorExecutionNode execute() {
			this.dest0.value = this.src0.value;
			this.dest1.value = this.src1.value;
			this.dest2.value = this.src2.value;
			this.dest3.value = this.src3.value;
			this.dest4.value = this.src4.value;
			this.dest5.value = this.src5.value;
			this.dest6.value = this.src6.value;
			this.dest7.value = this.src7.value;
			this.dest8.value = this.src8.value;
			this.dest9.value = this.src9.value;
			return this.nextNode;
		}
	}
}
