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

public class Float64CachedScalarMultipleTransferUnit extends AcceleratorExecutionUnit {

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
					node = new Float64CachedScalarMovX2Node(
						(Float64ScalarCache)operandCaches[0], (Float64ScalarCache)operandCaches[1], // dest, src
						(Float64ScalarCache)operandCaches[2], (Float64ScalarCache)operandCaches[3], // dest, src
						nextNode
					);
					break;
				}
				case 3: {
					node = new Float64CachedScalarMovX3Node(
						(Float64ScalarCache)operandCaches[0], (Float64ScalarCache)operandCaches[1], // dest, src
						(Float64ScalarCache)operandCaches[2], (Float64ScalarCache)operandCaches[3], // dest, src
						(Float64ScalarCache)operandCaches[4], (Float64ScalarCache)operandCaches[5], // dest, src
						nextNode
					);
					break;
				}
				case 4: {
					node = new Float64CachedScalarMovX4Node(
						(Float64ScalarCache)operandCaches[0], (Float64ScalarCache)operandCaches[1], // dest, src
						(Float64ScalarCache)operandCaches[2], (Float64ScalarCache)operandCaches[3], // dest, src
						(Float64ScalarCache)operandCaches[4], (Float64ScalarCache)operandCaches[5], // dest, src
						(Float64ScalarCache)operandCaches[6], (Float64ScalarCache)operandCaches[7], // dest, src
						nextNode
					);
					break;
				}
				case 5: {
					node = new Float64CachedScalarMovX5Node(
						(Float64ScalarCache)operandCaches[0], (Float64ScalarCache)operandCaches[1], // dest, src
						(Float64ScalarCache)operandCaches[2], (Float64ScalarCache)operandCaches[3], // dest, src
						(Float64ScalarCache)operandCaches[4], (Float64ScalarCache)operandCaches[5], // dest, src
						(Float64ScalarCache)operandCaches[6], (Float64ScalarCache)operandCaches[7], // dest, src
						(Float64ScalarCache)operandCaches[8], (Float64ScalarCache)operandCaches[9], // dest, src
						nextNode
					);
					break;
				}
				case 6: {
					node = new Float64CachedScalarMovX6Node(
						(Float64ScalarCache)operandCaches[0], (Float64ScalarCache)operandCaches[1],   // dest, src
						(Float64ScalarCache)operandCaches[2], (Float64ScalarCache)operandCaches[3],   // dest, src
						(Float64ScalarCache)operandCaches[4], (Float64ScalarCache)operandCaches[5],   // dest, src
						(Float64ScalarCache)operandCaches[6], (Float64ScalarCache)operandCaches[7],   // dest, src
						(Float64ScalarCache)operandCaches[8], (Float64ScalarCache)operandCaches[9],   // dest, src
						(Float64ScalarCache)operandCaches[10], (Float64ScalarCache)operandCaches[11], // dest, src
						nextNode
					);
					break;
				}
				case 7: {
					node = new Float64CachedScalarMovX7Node(
						(Float64ScalarCache)operandCaches[0], (Float64ScalarCache)operandCaches[1],   // dest, src
						(Float64ScalarCache)operandCaches[2], (Float64ScalarCache)operandCaches[3],   // dest, src
						(Float64ScalarCache)operandCaches[4], (Float64ScalarCache)operandCaches[5],   // dest, src
						(Float64ScalarCache)operandCaches[6], (Float64ScalarCache)operandCaches[7],   // dest, src
						(Float64ScalarCache)operandCaches[8], (Float64ScalarCache)operandCaches[9],   // dest, src
						(Float64ScalarCache)operandCaches[10], (Float64ScalarCache)operandCaches[11], // dest, src
						(Float64ScalarCache)operandCaches[12], (Float64ScalarCache)operandCaches[13], // dest, src
						nextNode
					);
					break;
				}
				case 8: {
					node = new Float64CachedScalarMovX8Node(
						(Float64ScalarCache)operandCaches[0], (Float64ScalarCache)operandCaches[1],   // dest, src
						(Float64ScalarCache)operandCaches[2], (Float64ScalarCache)operandCaches[3],   // dest, src
						(Float64ScalarCache)operandCaches[4], (Float64ScalarCache)operandCaches[5],   // dest, src
						(Float64ScalarCache)operandCaches[6], (Float64ScalarCache)operandCaches[7],   // dest, src
						(Float64ScalarCache)operandCaches[8], (Float64ScalarCache)operandCaches[9],   // dest, src
						(Float64ScalarCache)operandCaches[10], (Float64ScalarCache)operandCaches[11], // dest, src
						(Float64ScalarCache)operandCaches[12], (Float64ScalarCache)operandCaches[13], // dest, src
						(Float64ScalarCache)operandCaches[14], (Float64ScalarCache)operandCaches[15], // dest, src
						nextNode
					);
					break;
				}
				case 9: {
					node = new Float64CachedScalarMovX9Node(
						(Float64ScalarCache)operandCaches[0], (Float64ScalarCache)operandCaches[1],   // dest, src
						(Float64ScalarCache)operandCaches[2], (Float64ScalarCache)operandCaches[3],   // dest, src
						(Float64ScalarCache)operandCaches[4], (Float64ScalarCache)operandCaches[5],   // dest, src
						(Float64ScalarCache)operandCaches[6], (Float64ScalarCache)operandCaches[7],   // dest, src
						(Float64ScalarCache)operandCaches[8], (Float64ScalarCache)operandCaches[9],   // dest, src
						(Float64ScalarCache)operandCaches[10], (Float64ScalarCache)operandCaches[11], // dest, src
						(Float64ScalarCache)operandCaches[12], (Float64ScalarCache)operandCaches[13], // dest, src
						(Float64ScalarCache)operandCaches[14], (Float64ScalarCache)operandCaches[15], // dest, src
						(Float64ScalarCache)operandCaches[16], (Float64ScalarCache)operandCaches[17], // dest, src
						nextNode
					);
					break;
				}
				case 10: {
					node = new Float64CachedScalarMovX10Node(
						(Float64ScalarCache)operandCaches[0], (Float64ScalarCache)operandCaches[1],   // dest, src
						(Float64ScalarCache)operandCaches[2], (Float64ScalarCache)operandCaches[3],   // dest, src
						(Float64ScalarCache)operandCaches[4], (Float64ScalarCache)operandCaches[5],   // dest, src
						(Float64ScalarCache)operandCaches[6], (Float64ScalarCache)operandCaches[7],   // dest, src
						(Float64ScalarCache)operandCaches[8], (Float64ScalarCache)operandCaches[9],   // dest, src
						(Float64ScalarCache)operandCaches[10], (Float64ScalarCache)operandCaches[11], // dest, src
						(Float64ScalarCache)operandCaches[12], (Float64ScalarCache)operandCaches[13], // dest, src
						(Float64ScalarCache)operandCaches[14], (Float64ScalarCache)operandCaches[15], // dest, src
						(Float64ScalarCache)operandCaches[16], (Float64ScalarCache)operandCaches[17], // dest, src
						(Float64ScalarCache)operandCaches[18], (Float64ScalarCache)operandCaches[19], // dest, src
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

	private class Float64CachedScalarMovX2Node extends AcceleratorExecutionNode {
		protected final Float64ScalarCache dest0;
		protected final Float64ScalarCache src0;
		protected final Float64ScalarCache dest1;
		protected final Float64ScalarCache src1;

		public Float64CachedScalarMovX2Node(
				Float64ScalarCache dest0, Float64ScalarCache src0,
				Float64ScalarCache dest1, Float64ScalarCache src1,
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

	private class Float64CachedScalarMovX3Node extends AcceleratorExecutionNode {
		protected final Float64ScalarCache dest0;
		protected final Float64ScalarCache src0;
		protected final Float64ScalarCache dest1;
		protected final Float64ScalarCache src1;
		protected final Float64ScalarCache dest2;
		protected final Float64ScalarCache src2;

		public Float64CachedScalarMovX3Node(
				Float64ScalarCache dest0, Float64ScalarCache src0,
				Float64ScalarCache dest1, Float64ScalarCache src1,
				Float64ScalarCache dest2, Float64ScalarCache src2,
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

	private class Float64CachedScalarMovX4Node extends AcceleratorExecutionNode {
		protected final Float64ScalarCache dest0;
		protected final Float64ScalarCache src0;
		protected final Float64ScalarCache dest1;
		protected final Float64ScalarCache src1;
		protected final Float64ScalarCache dest2;
		protected final Float64ScalarCache src2;
		protected final Float64ScalarCache dest3;
		protected final Float64ScalarCache src3;

		public Float64CachedScalarMovX4Node(
				Float64ScalarCache dest0, Float64ScalarCache src0,
				Float64ScalarCache dest1, Float64ScalarCache src1,
				Float64ScalarCache dest2, Float64ScalarCache src2,
				Float64ScalarCache dest3, Float64ScalarCache src3,
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

	private class Float64CachedScalarMovX5Node extends AcceleratorExecutionNode {
		protected final Float64ScalarCache dest0;
		protected final Float64ScalarCache src0;
		protected final Float64ScalarCache dest1;
		protected final Float64ScalarCache src1;
		protected final Float64ScalarCache dest2;
		protected final Float64ScalarCache src2;
		protected final Float64ScalarCache dest3;
		protected final Float64ScalarCache src3;
		protected final Float64ScalarCache dest4;
		protected final Float64ScalarCache src4;

		public Float64CachedScalarMovX5Node(
				Float64ScalarCache dest0, Float64ScalarCache src0,
				Float64ScalarCache dest1, Float64ScalarCache src1,
				Float64ScalarCache dest2, Float64ScalarCache src2,
				Float64ScalarCache dest3, Float64ScalarCache src3,
				Float64ScalarCache dest4, Float64ScalarCache src4,
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

	private class Float64CachedScalarMovX6Node extends AcceleratorExecutionNode {
		protected final Float64ScalarCache dest0;
		protected final Float64ScalarCache src0;
		protected final Float64ScalarCache dest1;
		protected final Float64ScalarCache src1;
		protected final Float64ScalarCache dest2;
		protected final Float64ScalarCache src2;
		protected final Float64ScalarCache dest3;
		protected final Float64ScalarCache src3;
		protected final Float64ScalarCache dest4;
		protected final Float64ScalarCache src4;
		protected final Float64ScalarCache dest5;
		protected final Float64ScalarCache src5;

		public Float64CachedScalarMovX6Node(
				Float64ScalarCache dest0, Float64ScalarCache src0,
				Float64ScalarCache dest1, Float64ScalarCache src1,
				Float64ScalarCache dest2, Float64ScalarCache src2,
				Float64ScalarCache dest3, Float64ScalarCache src3,
				Float64ScalarCache dest4, Float64ScalarCache src4,
				Float64ScalarCache dest5, Float64ScalarCache src5,
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

	private class Float64CachedScalarMovX7Node extends AcceleratorExecutionNode {
		protected final Float64ScalarCache dest0;
		protected final Float64ScalarCache src0;
		protected final Float64ScalarCache dest1;
		protected final Float64ScalarCache src1;
		protected final Float64ScalarCache dest2;
		protected final Float64ScalarCache src2;
		protected final Float64ScalarCache dest3;
		protected final Float64ScalarCache src3;
		protected final Float64ScalarCache dest4;
		protected final Float64ScalarCache src4;
		protected final Float64ScalarCache dest5;
		protected final Float64ScalarCache src5;
		protected final Float64ScalarCache dest6;
		protected final Float64ScalarCache src6;

		public Float64CachedScalarMovX7Node(
				Float64ScalarCache dest0, Float64ScalarCache src0,
				Float64ScalarCache dest1, Float64ScalarCache src1,
				Float64ScalarCache dest2, Float64ScalarCache src2,
				Float64ScalarCache dest3, Float64ScalarCache src3,
				Float64ScalarCache dest4, Float64ScalarCache src4,
				Float64ScalarCache dest5, Float64ScalarCache src5,
				Float64ScalarCache dest6, Float64ScalarCache src6,
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

	private class Float64CachedScalarMovX8Node extends AcceleratorExecutionNode {
		protected final Float64ScalarCache dest0;
		protected final Float64ScalarCache src0;
		protected final Float64ScalarCache dest1;
		protected final Float64ScalarCache src1;
		protected final Float64ScalarCache dest2;
		protected final Float64ScalarCache src2;
		protected final Float64ScalarCache dest3;
		protected final Float64ScalarCache src3;
		protected final Float64ScalarCache dest4;
		protected final Float64ScalarCache src4;
		protected final Float64ScalarCache dest5;
		protected final Float64ScalarCache src5;
		protected final Float64ScalarCache dest6;
		protected final Float64ScalarCache src6;
		protected final Float64ScalarCache dest7;
		protected final Float64ScalarCache src7;

		public Float64CachedScalarMovX8Node(
				Float64ScalarCache dest0, Float64ScalarCache src0,
				Float64ScalarCache dest1, Float64ScalarCache src1,
				Float64ScalarCache dest2, Float64ScalarCache src2,
				Float64ScalarCache dest3, Float64ScalarCache src3,
				Float64ScalarCache dest4, Float64ScalarCache src4,
				Float64ScalarCache dest5, Float64ScalarCache src5,
				Float64ScalarCache dest6, Float64ScalarCache src6,
				Float64ScalarCache dest7, Float64ScalarCache src7,
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

	private class Float64CachedScalarMovX9Node extends AcceleratorExecutionNode {
		protected final Float64ScalarCache dest0;
		protected final Float64ScalarCache src0;
		protected final Float64ScalarCache dest1;
		protected final Float64ScalarCache src1;
		protected final Float64ScalarCache dest2;
		protected final Float64ScalarCache src2;
		protected final Float64ScalarCache dest3;
		protected final Float64ScalarCache src3;
		protected final Float64ScalarCache dest4;
		protected final Float64ScalarCache src4;
		protected final Float64ScalarCache dest5;
		protected final Float64ScalarCache src5;
		protected final Float64ScalarCache dest6;
		protected final Float64ScalarCache src6;
		protected final Float64ScalarCache dest7;
		protected final Float64ScalarCache src7;
		protected final Float64ScalarCache dest8;
		protected final Float64ScalarCache src8;

		public Float64CachedScalarMovX9Node(
				Float64ScalarCache dest0, Float64ScalarCache src0,
				Float64ScalarCache dest1, Float64ScalarCache src1,
				Float64ScalarCache dest2, Float64ScalarCache src2,
				Float64ScalarCache dest3, Float64ScalarCache src3,
				Float64ScalarCache dest4, Float64ScalarCache src4,
				Float64ScalarCache dest5, Float64ScalarCache src5,
				Float64ScalarCache dest6, Float64ScalarCache src6,
				Float64ScalarCache dest7, Float64ScalarCache src7,
				Float64ScalarCache dest8, Float64ScalarCache src8,
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

	private class Float64CachedScalarMovX10Node extends AcceleratorExecutionNode {
		protected final Float64ScalarCache dest0;
		protected final Float64ScalarCache src0;
		protected final Float64ScalarCache dest1;
		protected final Float64ScalarCache src1;
		protected final Float64ScalarCache dest2;
		protected final Float64ScalarCache src2;
		protected final Float64ScalarCache dest3;
		protected final Float64ScalarCache src3;
		protected final Float64ScalarCache dest4;
		protected final Float64ScalarCache src4;
		protected final Float64ScalarCache dest5;
		protected final Float64ScalarCache src5;
		protected final Float64ScalarCache dest6;
		protected final Float64ScalarCache src6;
		protected final Float64ScalarCache dest7;
		protected final Float64ScalarCache src7;
		protected final Float64ScalarCache dest8;
		protected final Float64ScalarCache src8;
		protected final Float64ScalarCache dest9;
		protected final Float64ScalarCache src9;

		public Float64CachedScalarMovX10Node(
				Float64ScalarCache dest0, Float64ScalarCache src0,
				Float64ScalarCache dest1, Float64ScalarCache src1,
				Float64ScalarCache dest2, Float64ScalarCache src2,
				Float64ScalarCache dest3, Float64ScalarCache src3,
				Float64ScalarCache dest4, Float64ScalarCache src4,
				Float64ScalarCache dest5, Float64ScalarCache src5,
				Float64ScalarCache dest6, Float64ScalarCache src6,
				Float64ScalarCache dest7, Float64ScalarCache src7,
				Float64ScalarCache dest8, Float64ScalarCache src8,
				Float64ScalarCache dest9, Float64ScalarCache src9,
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
