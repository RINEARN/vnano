/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class BoolVectorLogicalUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutorNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		DataContainer<boolean[]>[] containers = (DataContainer<boolean[]>[])operandContainers;

		BoolVectorLogicalExecutorNode executor = null;
		switch (instruction.getOperationCode()) {
			case AND : {
				Boolx3ScalarCacheSynchronizer synchronizer = new Boolx3ScalarCacheSynchronizer(
						operandContainers, operandCaches, operandCached);
				executor = new BoolVectorAndExecutorNode(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case OR : {
				Boolx3ScalarCacheSynchronizer synchronizer = new Boolx3ScalarCacheSynchronizer(
						operandContainers, operandCaches, operandCached);
				executor = new BoolVectorOrExecutorNode(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case NOT : {
				Boolx2ScalarCacheSynchronizer synchronizer = new Boolx2ScalarCacheSynchronizer(
						operandContainers, operandCaches, operandCached);
				executor = new BoolVectorNotExecutorNode(containers[0], containers[1], synchronizer, nextNode);
				break;
			}
			default : {
				throw new VnanoFatalException(
						"Operation code " + instruction.getOperationCode() + " is invalid for " + this.getClass().getCanonicalName()
				);
			}
		}
		return executor;
	}

	private abstract class BoolVectorLogicalExecutorNode extends AccelerationExecutorNode {
		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<boolean[]> container1;
		protected final DataContainer<boolean[]> container2;
		protected final CacheSynchronizer synchronizer;

		public BoolVectorLogicalExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1, DataContainer<boolean[]> container2,
				Boolx3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}
		public BoolVectorLogicalExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1,
				Boolx2ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = null;
			this.synchronizer = synchronizer;
		}
	}

	private final class BoolVectorAndExecutorNode extends BoolVectorLogicalExecutorNode {

		public BoolVectorAndExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1, DataContainer<boolean[]> container2,
				Boolx3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			boolean[] data0 = this.container0.getData();
			boolean[] data1 = this.container1.getData();
			boolean[] data2 = this.container2.getData();
			int size = this.container0.getSize();

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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class BoolVectorOrExecutorNode extends BoolVectorLogicalExecutorNode {

		public BoolVectorOrExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1, DataContainer<boolean[]> container2,
				Boolx3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			boolean[] data0 = this.container0.getData();
			boolean[] data1 = this.container1.getData();
			boolean[] data2 = this.container2.getData();
			int size = this.container0.getSize();

			// 短絡評価により「 || 」演算子の右オペランドが評価されなかった場合、レジスタが確保されずに null が入っている
			if (data2 == null) {

				// 短絡評価になったという事は、「 || 」演算子の結果は左オペランドから自明に true である場合なので、
				// AND命令においても、右オペランドのデータが未確保の場合の挙動はそのように定義する
				for (int i=0; i<size; i++) {
					data0[i] = true;
				}

			// 普通に「 && 」演算子の両オペランドが評価されて、両者の結果が入力されている場合
			} else {
				for (int i=0; i<size; i++) {
					data0[i] = data1[i] | data2[i];
				}
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class BoolVectorNotExecutorNode extends BoolVectorLogicalExecutorNode {

		public BoolVectorNotExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1,
				Boolx2ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			boolean[] data0 = this.container0.getData();
			boolean[] data1 = this.container1.getData();
			int size = this.container0.getSize();

			for (int i=0; i<size; i++) {
				data0[i] = !data1[i];
			}

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}
}

