/*
 * Copyright(C) 2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class Float64CachedScalarSubscriptUnit extends AcceleratorExecutionUnit {

	// このユニットで処理できる、REFELEM命令対象配列の最大次元数
	//（処理できない場合、Processorが任意次元対応なので、スケジューラ側でそちらへバイパス割り当てが必要）
	public static final int REFELEM_MAX_AVAILABLE_RANK = 1;

	// ※ このユニットはまだVM内最適化 or コンパイラ側が対応するまでスケジューリング条件が満たされずに呼ばれないので、
	//    暫定的に1次元のみに対応

	@SuppressWarnings("unchecked")
	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		AcceleratorExecutionNode node = null;
		switch (instruction.getOperationCode()) {
			case REFELEM : {
				// 要素を参照したい配列の次元数（＝indicesオペランド数なので全オペランド数-2）
				int targetArrayRank = operandContainers.length - 2;
				if (targetArrayRank == 1) {
				node = new Float64CachedScalarRefElem1DNode(
					(Float64ScalarCache)operandCaches[0], (DataContainer<double[]>)operandContainers[1],
					(Int64ScalarCache)operandCaches[2], nextNode
				);
				} else {
					throw new VnanoFatalException(
						"Operands of a REFELEM instructions are too many for this unit (max: " + (targetArrayRank+2) + ")"
					);
				}
				break;
			}

			default : {
				throw new VnanoFatalException(
						"Operation code " + instruction.getOperationCode() + " is invalid for " + this.getClass().getCanonicalName()
				);
			}
		}
		return node;
	}

	private final class Float64CachedScalarRefElem1DNode extends AcceleratorExecutionNode {

		protected final Float64ScalarCache cache0; // dest
		protected final DataContainer<double[]> container1; // src
		protected final Int64ScalarCache cache2;   // indices[0]

		public Float64CachedScalarRefElem1DNode(
				Float64ScalarCache cache0, DataContainer<double[]> container1, Int64ScalarCache chache2,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.cache0 = cache0;
			this.container1 = container1;
			this.cache2 = chache2;
		}

		public final AcceleratorExecutionNode execute() {
			// REFELEM 系は本来は参照同期だが、dest が cacheable と判定されてこのユニットが使われているという事は、
			// 参照元の変化に非依存である事を確認済みという事なので、参照同期を考えなくていい
			// (その検証に対応できるまでこのユニットは使われない)
			this.cache0.value = this.container1.getData()[ (int)this.cache2.value ];
			return this.nextNode;
		}
	}
}
