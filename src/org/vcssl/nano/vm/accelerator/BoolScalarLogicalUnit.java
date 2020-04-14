/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class BoolScalarLogicalUnit extends AcceleratorExecutionUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		DataContainer<boolean[]>[] containers = (DataContainer<boolean[]>[])operandContainers;

		AcceleratorExecutionNode node = null;
		switch (instruction.getOperationCode()) {
			case AND : {
				Boolx3ScalarCacheSynchronizer synchronizer = new Boolx3ScalarCacheSynchronizer(
						operandContainers, operandCaches, operandCachingEnabled);
				node = new BoolScalarAndNode(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case OR : {
				Boolx3ScalarCacheSynchronizer synchronizer = new Boolx3ScalarCacheSynchronizer(
						operandContainers, operandCaches, operandCachingEnabled);
				node = new BoolScalarOrNode(containers[0], containers[1], containers[2], synchronizer, nextNode);
				break;
			}
			case NOT : {
				Boolx2ScalarCacheSynchronizer synchronizer = new Boolx2ScalarCacheSynchronizer(
						operandContainers, operandCaches, operandCachingEnabled);
				node = new BoolScalarNotNode(containers[0], containers[1], synchronizer, nextNode);
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

	private abstract class BoolScalarLogicalNode extends AcceleratorExecutionNode {
		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<boolean[]> container1;
		protected final DataContainer<boolean[]> container2;
		protected final CacheSynchronizer synchronizer;

		public BoolScalarLogicalNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1, DataContainer<boolean[]> container2,
				Boolx3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}

		public BoolScalarLogicalNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1,
				Boolx2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = null;
			this.synchronizer = synchronizer;
		}
	}

	private final class BoolScalarAndNode extends BoolScalarLogicalNode {

		public BoolScalarAndNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1, DataContainer<boolean[]> container2,
				Boolx3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();

			// 注： 短絡評価により、左辺(container1) が false の場合には
			//      右辺(container2) は null になっている可能性があるので、
			//      左辺から結果が自明な場合には右辺を参照してはいけない
			//      (アクセスコストの面からも恐らく不利)
			boolean leftHandValue = this.container1.getData()[ this.container1.getOffset() ];
			if (leftHandValue) {
				this.container0.getData()[ this.container0.getOffset() ] =
					leftHandValue & this.container2.getData()[ this.container2.getOffset() ] ;
			} else {
				this.container0.getData()[ this.container0.getOffset() ] = false;
			}
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class BoolScalarOrNode extends BoolScalarLogicalNode {

		public BoolScalarOrNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1, DataContainer<boolean[]> container2,
				Boolx3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();

			// 注： 短絡評価により、左辺(container1) が true の場合には
			//      右辺(container2) は null になっている可能性があるので、
			//      左辺から結果が自明な場合には右辺を参照してはいけない
			//      (アクセスコストの面からも恐らく不利)
			boolean leftHandValue = this.container1.getData()[ this.container1.getOffset() ];
			if (leftHandValue) {
				this.container0.getData()[ this.container0.getOffset() ] = true;
			} else {
				this.container0.getData()[ this.container0.getOffset() ] =
					leftHandValue | this.container2.getData()[ this.container2.getOffset() ] ;
			}
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class BoolScalarNotNode extends BoolScalarLogicalNode {

		public BoolScalarNotNode(
				DataContainer<boolean[]> container0, DataContainer<boolean[]> container1,
				Boolx2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			this.container0.getData()[ this.container0.getOffset() ] =
			!this.container1.getData()[ this.container1.getOffset() ];
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}
}
