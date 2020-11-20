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

public class BoolCachedScalarMultipleTransferUnit extends AcceleratorExecutionUnit {

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
					node = new BoolCachedScalarMovX2Node(
						(BoolScalarCache)operandCaches[0], (BoolScalarCache)operandCaches[1], // dest, src
						(BoolScalarCache)operandCaches[2], (BoolScalarCache)operandCaches[3], // dest, src
						nextNode
					);
					break;
				}
				case 3: {
					node = new BoolCachedScalarMovX3Node(
						(BoolScalarCache)operandCaches[0], (BoolScalarCache)operandCaches[1], // dest, src
						(BoolScalarCache)operandCaches[2], (BoolScalarCache)operandCaches[3], // dest, src
						(BoolScalarCache)operandCaches[4], (BoolScalarCache)operandCaches[5], // dest, src
						nextNode
					);
					break;
				}
				case 4: {
					node = new BoolCachedScalarMovX4Node(
						(BoolScalarCache)operandCaches[0], (BoolScalarCache)operandCaches[1], // dest, src
						(BoolScalarCache)operandCaches[2], (BoolScalarCache)operandCaches[3], // dest, src
						(BoolScalarCache)operandCaches[4], (BoolScalarCache)operandCaches[5], // dest, src
						(BoolScalarCache)operandCaches[6], (BoolScalarCache)operandCaches[7], // dest, src
						nextNode
					);
					break;
				}
				case 5: {
					node = new BoolCachedScalarMovX5Node(
						(BoolScalarCache)operandCaches[0], (BoolScalarCache)operandCaches[1], // dest, src
						(BoolScalarCache)operandCaches[2], (BoolScalarCache)operandCaches[3], // dest, src
						(BoolScalarCache)operandCaches[4], (BoolScalarCache)operandCaches[5], // dest, src
						(BoolScalarCache)operandCaches[6], (BoolScalarCache)operandCaches[7], // dest, src
						(BoolScalarCache)operandCaches[8], (BoolScalarCache)operandCaches[9], // dest, src
						nextNode
					);
					break;
				}
				case 6: {
					node = new BoolCachedScalarMovX6Node(
						(BoolScalarCache)operandCaches[0], (BoolScalarCache)operandCaches[1],   // dest, src
						(BoolScalarCache)operandCaches[2], (BoolScalarCache)operandCaches[3],   // dest, src
						(BoolScalarCache)operandCaches[4], (BoolScalarCache)operandCaches[5],   // dest, src
						(BoolScalarCache)operandCaches[6], (BoolScalarCache)operandCaches[7],   // dest, src
						(BoolScalarCache)operandCaches[8], (BoolScalarCache)operandCaches[9],   // dest, src
						(BoolScalarCache)operandCaches[10], (BoolScalarCache)operandCaches[11], // dest, src
						nextNode
					);
					break;
				}
				case 7: {
					node = new BoolCachedScalarMovX7Node(
						(BoolScalarCache)operandCaches[0], (BoolScalarCache)operandCaches[1],   // dest, src
						(BoolScalarCache)operandCaches[2], (BoolScalarCache)operandCaches[3],   // dest, src
						(BoolScalarCache)operandCaches[4], (BoolScalarCache)operandCaches[5],   // dest, src
						(BoolScalarCache)operandCaches[6], (BoolScalarCache)operandCaches[7],   // dest, src
						(BoolScalarCache)operandCaches[8], (BoolScalarCache)operandCaches[9],   // dest, src
						(BoolScalarCache)operandCaches[10], (BoolScalarCache)operandCaches[11], // dest, src
						(BoolScalarCache)operandCaches[12], (BoolScalarCache)operandCaches[13], // dest, src
						nextNode
					);
					break;
				}
				case 8: {
					node = new BoolCachedScalarMovX8Node(
						(BoolScalarCache)operandCaches[0], (BoolScalarCache)operandCaches[1],   // dest, src
						(BoolScalarCache)operandCaches[2], (BoolScalarCache)operandCaches[3],   // dest, src
						(BoolScalarCache)operandCaches[4], (BoolScalarCache)operandCaches[5],   // dest, src
						(BoolScalarCache)operandCaches[6], (BoolScalarCache)operandCaches[7],   // dest, src
						(BoolScalarCache)operandCaches[8], (BoolScalarCache)operandCaches[9],   // dest, src
						(BoolScalarCache)operandCaches[10], (BoolScalarCache)operandCaches[11], // dest, src
						(BoolScalarCache)operandCaches[12], (BoolScalarCache)operandCaches[13], // dest, src
						(BoolScalarCache)operandCaches[14], (BoolScalarCache)operandCaches[15], // dest, src
						nextNode
					);
					break;
				}
				case 9: {
					node = new BoolCachedScalarMovX9Node(
						(BoolScalarCache)operandCaches[0], (BoolScalarCache)operandCaches[1],   // dest, src
						(BoolScalarCache)operandCaches[2], (BoolScalarCache)operandCaches[3],   // dest, src
						(BoolScalarCache)operandCaches[4], (BoolScalarCache)operandCaches[5],   // dest, src
						(BoolScalarCache)operandCaches[6], (BoolScalarCache)operandCaches[7],   // dest, src
						(BoolScalarCache)operandCaches[8], (BoolScalarCache)operandCaches[9],   // dest, src
						(BoolScalarCache)operandCaches[10], (BoolScalarCache)operandCaches[11], // dest, src
						(BoolScalarCache)operandCaches[12], (BoolScalarCache)operandCaches[13], // dest, src
						(BoolScalarCache)operandCaches[14], (BoolScalarCache)operandCaches[15], // dest, src
						(BoolScalarCache)operandCaches[16], (BoolScalarCache)operandCaches[17], // dest, src
						nextNode
					);
					break;
				}
				case 10: {
					node = new BoolCachedScalarMovX10Node(
						(BoolScalarCache)operandCaches[0], (BoolScalarCache)operandCaches[1],   // dest, src
						(BoolScalarCache)operandCaches[2], (BoolScalarCache)operandCaches[3],   // dest, src
						(BoolScalarCache)operandCaches[4], (BoolScalarCache)operandCaches[5],   // dest, src
						(BoolScalarCache)operandCaches[6], (BoolScalarCache)operandCaches[7],   // dest, src
						(BoolScalarCache)operandCaches[8], (BoolScalarCache)operandCaches[9],   // dest, src
						(BoolScalarCache)operandCaches[10], (BoolScalarCache)operandCaches[11], // dest, src
						(BoolScalarCache)operandCaches[12], (BoolScalarCache)operandCaches[13], // dest, src
						(BoolScalarCache)operandCaches[14], (BoolScalarCache)operandCaches[15], // dest, src
						(BoolScalarCache)operandCaches[16], (BoolScalarCache)operandCaches[17], // dest, src
						(BoolScalarCache)operandCaches[18], (BoolScalarCache)operandCaches[19], // dest, src
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

	private class BoolCachedScalarMovX2Node extends AcceleratorExecutionNode {
		protected final BoolScalarCache dest0;
		protected final BoolScalarCache src0;
		protected final BoolScalarCache dest1;
		protected final BoolScalarCache src1;

		public BoolCachedScalarMovX2Node(
				BoolScalarCache dest0, BoolScalarCache src0,
				BoolScalarCache dest1, BoolScalarCache src1,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2); // 第二引数は一括処理命令数
			this.src0 = src0;
			this.src1 = src1;
			this.dest0 = dest0;
			this.dest1 = dest1;
		}

		public final AcceleratorExecutionNode execute() {
			this.dest0.data = this.src0.data;
			this.dest1.data = this.src1.data;
			return this.nextNode;
		}
	}

	private class BoolCachedScalarMovX3Node extends AcceleratorExecutionNode {
		protected final BoolScalarCache dest0;
		protected final BoolScalarCache src0;
		protected final BoolScalarCache dest1;
		protected final BoolScalarCache src1;
		protected final BoolScalarCache dest2;
		protected final BoolScalarCache src2;

		public BoolCachedScalarMovX3Node(
				BoolScalarCache dest0, BoolScalarCache src0,
				BoolScalarCache dest1, BoolScalarCache src1,
				BoolScalarCache dest2, BoolScalarCache src2,
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
			this.dest0.data = this.src0.data;
			this.dest1.data = this.src1.data;
			this.dest2.data = this.src2.data;
			return this.nextNode;
		}
	}

	private class BoolCachedScalarMovX4Node extends AcceleratorExecutionNode {
		protected final BoolScalarCache dest0;
		protected final BoolScalarCache src0;
		protected final BoolScalarCache dest1;
		protected final BoolScalarCache src1;
		protected final BoolScalarCache dest2;
		protected final BoolScalarCache src2;
		protected final BoolScalarCache dest3;
		protected final BoolScalarCache src3;

		public BoolCachedScalarMovX4Node(
				BoolScalarCache dest0, BoolScalarCache src0,
				BoolScalarCache dest1, BoolScalarCache src1,
				BoolScalarCache dest2, BoolScalarCache src2,
				BoolScalarCache dest3, BoolScalarCache src3,
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
			this.dest0.data = this.src0.data;
			this.dest1.data = this.src1.data;
			this.dest2.data = this.src2.data;
			this.dest3.data = this.src3.data;
			return this.nextNode;
		}
	}

	private class BoolCachedScalarMovX5Node extends AcceleratorExecutionNode {
		protected final BoolScalarCache dest0;
		protected final BoolScalarCache src0;
		protected final BoolScalarCache dest1;
		protected final BoolScalarCache src1;
		protected final BoolScalarCache dest2;
		protected final BoolScalarCache src2;
		protected final BoolScalarCache dest3;
		protected final BoolScalarCache src3;
		protected final BoolScalarCache dest4;
		protected final BoolScalarCache src4;

		public BoolCachedScalarMovX5Node(
				BoolScalarCache dest0, BoolScalarCache src0,
				BoolScalarCache dest1, BoolScalarCache src1,
				BoolScalarCache dest2, BoolScalarCache src2,
				BoolScalarCache dest3, BoolScalarCache src3,
				BoolScalarCache dest4, BoolScalarCache src4,
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
			this.dest0.data = this.src0.data;
			this.dest1.data = this.src1.data;
			this.dest2.data = this.src2.data;
			this.dest3.data = this.src3.data;
			this.dest4.data = this.src4.data;
			return this.nextNode;
		}
	}

	private class BoolCachedScalarMovX6Node extends AcceleratorExecutionNode {
		protected final BoolScalarCache dest0;
		protected final BoolScalarCache src0;
		protected final BoolScalarCache dest1;
		protected final BoolScalarCache src1;
		protected final BoolScalarCache dest2;
		protected final BoolScalarCache src2;
		protected final BoolScalarCache dest3;
		protected final BoolScalarCache src3;
		protected final BoolScalarCache dest4;
		protected final BoolScalarCache src4;
		protected final BoolScalarCache dest5;
		protected final BoolScalarCache src5;

		public BoolCachedScalarMovX6Node(
				BoolScalarCache dest0, BoolScalarCache src0,
				BoolScalarCache dest1, BoolScalarCache src1,
				BoolScalarCache dest2, BoolScalarCache src2,
				BoolScalarCache dest3, BoolScalarCache src3,
				BoolScalarCache dest4, BoolScalarCache src4,
				BoolScalarCache dest5, BoolScalarCache src5,
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
			this.dest0.data = this.src0.data;
			this.dest1.data = this.src1.data;
			this.dest2.data = this.src2.data;
			this.dest3.data = this.src3.data;
			this.dest4.data = this.src4.data;
			this.dest5.data = this.src5.data;
			return this.nextNode;
		}
	}

	private class BoolCachedScalarMovX7Node extends AcceleratorExecutionNode {
		protected final BoolScalarCache dest0;
		protected final BoolScalarCache src0;
		protected final BoolScalarCache dest1;
		protected final BoolScalarCache src1;
		protected final BoolScalarCache dest2;
		protected final BoolScalarCache src2;
		protected final BoolScalarCache dest3;
		protected final BoolScalarCache src3;
		protected final BoolScalarCache dest4;
		protected final BoolScalarCache src4;
		protected final BoolScalarCache dest5;
		protected final BoolScalarCache src5;
		protected final BoolScalarCache dest6;
		protected final BoolScalarCache src6;

		public BoolCachedScalarMovX7Node(
				BoolScalarCache dest0, BoolScalarCache src0,
				BoolScalarCache dest1, BoolScalarCache src1,
				BoolScalarCache dest2, BoolScalarCache src2,
				BoolScalarCache dest3, BoolScalarCache src3,
				BoolScalarCache dest4, BoolScalarCache src4,
				BoolScalarCache dest5, BoolScalarCache src5,
				BoolScalarCache dest6, BoolScalarCache src6,
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
			this.dest0.data = this.src0.data;
			this.dest1.data = this.src1.data;
			this.dest2.data = this.src2.data;
			this.dest3.data = this.src3.data;
			this.dest4.data = this.src4.data;
			this.dest5.data = this.src5.data;
			this.dest6.data = this.src6.data;
			return this.nextNode;
		}
	}

	private class BoolCachedScalarMovX8Node extends AcceleratorExecutionNode {
		protected final BoolScalarCache dest0;
		protected final BoolScalarCache src0;
		protected final BoolScalarCache dest1;
		protected final BoolScalarCache src1;
		protected final BoolScalarCache dest2;
		protected final BoolScalarCache src2;
		protected final BoolScalarCache dest3;
		protected final BoolScalarCache src3;
		protected final BoolScalarCache dest4;
		protected final BoolScalarCache src4;
		protected final BoolScalarCache dest5;
		protected final BoolScalarCache src5;
		protected final BoolScalarCache dest6;
		protected final BoolScalarCache src6;
		protected final BoolScalarCache dest7;
		protected final BoolScalarCache src7;

		public BoolCachedScalarMovX8Node(
				BoolScalarCache dest0, BoolScalarCache src0,
				BoolScalarCache dest1, BoolScalarCache src1,
				BoolScalarCache dest2, BoolScalarCache src2,
				BoolScalarCache dest3, BoolScalarCache src3,
				BoolScalarCache dest4, BoolScalarCache src4,
				BoolScalarCache dest5, BoolScalarCache src5,
				BoolScalarCache dest6, BoolScalarCache src6,
				BoolScalarCache dest7, BoolScalarCache src7,
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
			this.dest0.data = this.src0.data;
			this.dest1.data = this.src1.data;
			this.dest2.data = this.src2.data;
			this.dest3.data = this.src3.data;
			this.dest4.data = this.src4.data;
			this.dest5.data = this.src5.data;
			this.dest6.data = this.src6.data;
			this.dest7.data = this.src7.data;
			return this.nextNode;
		}
	}

	private class BoolCachedScalarMovX9Node extends AcceleratorExecutionNode {
		protected final BoolScalarCache dest0;
		protected final BoolScalarCache src0;
		protected final BoolScalarCache dest1;
		protected final BoolScalarCache src1;
		protected final BoolScalarCache dest2;
		protected final BoolScalarCache src2;
		protected final BoolScalarCache dest3;
		protected final BoolScalarCache src3;
		protected final BoolScalarCache dest4;
		protected final BoolScalarCache src4;
		protected final BoolScalarCache dest5;
		protected final BoolScalarCache src5;
		protected final BoolScalarCache dest6;
		protected final BoolScalarCache src6;
		protected final BoolScalarCache dest7;
		protected final BoolScalarCache src7;
		protected final BoolScalarCache dest8;
		protected final BoolScalarCache src8;

		public BoolCachedScalarMovX9Node(
				BoolScalarCache dest0, BoolScalarCache src0,
				BoolScalarCache dest1, BoolScalarCache src1,
				BoolScalarCache dest2, BoolScalarCache src2,
				BoolScalarCache dest3, BoolScalarCache src3,
				BoolScalarCache dest4, BoolScalarCache src4,
				BoolScalarCache dest5, BoolScalarCache src5,
				BoolScalarCache dest6, BoolScalarCache src6,
				BoolScalarCache dest7, BoolScalarCache src7,
				BoolScalarCache dest8, BoolScalarCache src8,
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
			this.dest0.data = this.src0.data;
			this.dest1.data = this.src1.data;
			this.dest2.data = this.src2.data;
			this.dest3.data = this.src3.data;
			this.dest4.data = this.src4.data;
			this.dest5.data = this.src5.data;
			this.dest6.data = this.src6.data;
			this.dest7.data = this.src7.data;
			this.dest8.data = this.src8.data;
			return this.nextNode;
		}
	}

	private class BoolCachedScalarMovX10Node extends AcceleratorExecutionNode {
		protected final BoolScalarCache dest0;
		protected final BoolScalarCache src0;
		protected final BoolScalarCache dest1;
		protected final BoolScalarCache src1;
		protected final BoolScalarCache dest2;
		protected final BoolScalarCache src2;
		protected final BoolScalarCache dest3;
		protected final BoolScalarCache src3;
		protected final BoolScalarCache dest4;
		protected final BoolScalarCache src4;
		protected final BoolScalarCache dest5;
		protected final BoolScalarCache src5;
		protected final BoolScalarCache dest6;
		protected final BoolScalarCache src6;
		protected final BoolScalarCache dest7;
		protected final BoolScalarCache src7;
		protected final BoolScalarCache dest8;
		protected final BoolScalarCache src8;
		protected final BoolScalarCache dest9;
		protected final BoolScalarCache src9;

		public BoolCachedScalarMovX10Node(
				BoolScalarCache dest0, BoolScalarCache src0,
				BoolScalarCache dest1, BoolScalarCache src1,
				BoolScalarCache dest2, BoolScalarCache src2,
				BoolScalarCache dest3, BoolScalarCache src3,
				BoolScalarCache dest4, BoolScalarCache src4,
				BoolScalarCache dest5, BoolScalarCache src5,
				BoolScalarCache dest6, BoolScalarCache src6,
				BoolScalarCache dest7, BoolScalarCache src7,
				BoolScalarCache dest8, BoolScalarCache src8,
				BoolScalarCache dest9, BoolScalarCache src9,
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
			this.dest0.data = this.src0.data;
			this.dest1.data = this.src1.data;
			this.dest2.data = this.src2.data;
			this.dest3.data = this.src3.data;
			this.dest4.data = this.src4.data;
			this.dest5.data = this.src5.data;
			this.dest6.data = this.src6.data;
			this.dest7.data = this.src7.data;
			this.dest8.data = this.src8.data;
			this.dest9.data = this.src9.data;
			return this.nextNode;
		}
	}
}
