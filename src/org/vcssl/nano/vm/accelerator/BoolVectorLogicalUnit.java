/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class BoolVectorLogicalUnit extends AcceleratorExecutionUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		DataContainer<boolean[]>[] containers = (DataContainer<boolean[]>[])operandContainers;

		BoolVectorLogicalNode node = null;
		switch (instruction.getOperationCode()) {
			case ANDM : {
				Boolx3ScalarCacheSynchronizer synchronizer = new Boolx3ScalarCacheSynchronizer(
						operandContainers, operandCaches, operandCachingEnabled);
				node = new BoolVectorAndNode(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case ORM : {
				Boolx3ScalarCacheSynchronizer synchronizer = new Boolx3ScalarCacheSynchronizer(
						operandContainers, operandCaches, operandCachingEnabled);
				node = new BoolVectorOrNode(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case NOT : {
				Boolx2ScalarCacheSynchronizer synchronizer = new Boolx2ScalarCacheSynchronizer(
						operandContainers, operandCaches, operandCachingEnabled);
				node = new BoolVectorNotNode(containers[0], containers[1], synchronizer, nextNode);
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

	private abstract class BoolVectorLogicalNode extends AcceleratorExecutionNode {
		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<boolean[]> container1;
		protected final DataContainer<boolean[]> container2;
		protected final CacheSynchronizer synchronizer;

		public BoolVectorLogicalNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1, DataContainer<boolean[]> container2,
				Boolx3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}
		public BoolVectorLogicalNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1,
				Boolx2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = null;
			this.synchronizer = synchronizer;
		}
	}

	private final class BoolVectorAndNode extends BoolVectorLogicalNode {

		public BoolVectorAndNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1, DataContainer<boolean[]> container2,
				Boolx3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			boolean[] data0 = this.container0.getArrayData();
			boolean[] data1 = this.container1.getArrayData();
			boolean[] data2 = this.container2.getArrayData();
			int size = this.container0.getArraySize();

			// 短絡評価により「 && 」演算子の右オペランドが評価されなかった場合、レジスタが確保されずに null が入っている
			if (data2 == null) {

				// 短絡評価になったという事は、「 && 」演算子の結果は左オペランドから自明に false である場合なので、
				// AND命令においても、右オペランドのデータが未確保の場合の挙動はそのように定義する
				for (int i=0; i<size; i++) {
					data0[i] = false;
				}

			// 普通に「 && 」演算子の両オペランドが評価されて、両者の結果が入力されている場合
			} else {
				for (int i=0; i<size; i++) {
					data0[i] = data1[i] & data2[i];
				}
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class BoolVectorOrNode extends BoolVectorLogicalNode {

		public BoolVectorOrNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1, DataContainer<boolean[]> container2,
				Boolx3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			boolean[] data0 = this.container0.getArrayData();
			boolean[] data1 = this.container1.getArrayData();
			boolean[] data2 = this.container2.getArrayData();
			int size = this.container0.getArraySize();

			// 短絡評価により「 || 」演算子の右オペランドが評価されなかった場合、レジスタが確保されずに null が入っている
			if (data2 == null) {

				// 短絡評価になったという事は、「 || 」演算子の結果は左オペランドから自明に true である場合なので、
				// AND命令においても、右オペランドのデータが未確保の場合の挙動はそのように定義する
				for (int i=0; i<size; i++) {
					data0[i] = true;
				}

			// 普通に「 || 」演算子の両オペランドが評価されて、両者の結果が入力されている場合
			} else {
				for (int i=0; i<size; i++) {
					data0[i] = data1[i] | data2[i];
				}
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class BoolVectorNotNode extends BoolVectorLogicalNode {

		public BoolVectorNotNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1,
				Boolx2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			boolean[] data0 = this.container0.getArrayData();
			boolean[] data1 = this.container1.getArrayData();
			int size = this.container0.getArraySize();

			for (int i=0; i<size; i++) {
				data0[i] = !data1[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}
}

