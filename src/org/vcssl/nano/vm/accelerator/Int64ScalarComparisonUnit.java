/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class Int64ScalarComparisonUnit extends AcceleratorExecutionUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		DataContainer<boolean[]> cont0 = (DataContainer<boolean[]>)operandContainers[0];
		DataContainer<long[]> cont1 = (DataContainer<long[]>)operandContainers[1];
		DataContainer<long[]> cont2 = (DataContainer<long[]>)operandContainers[2];
		Boolx1Int64x2ScalarCacheSynchronizer synchronizer
				= new Boolx1Int64x2ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled);

		AcceleratorExecutionNode node = null;
		switch (instruction.getOperationCode()) {
			case LT : {
				node = new Int64ScalarLtNode(cont0, cont1, cont2, synchronizer, nextNode);
				break;
			}
			case GT : {
				node = new Int64ScalarGtNode(cont0, cont1, cont2, synchronizer, nextNode);
				break;
			}
			case LEQ : {
				node = new Int64ScalarLeqNode(cont0, cont1, cont2, synchronizer, nextNode);
				break;
			}
			case GEQ : {
				node = new Int64ScalarGeqNode(cont0, cont1, cont2, synchronizer, nextNode);
				break;
			}
			case EQ : {
				node = new Int64ScalarEqNode(cont0, cont1, cont2, synchronizer, nextNode);
				break;
			}
			case NEQ : {
				node = new Int64ScalarNeqNode(cont0, cont1, cont2, synchronizer, nextNode);
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

	private abstract class Int64ScalarComparisonNode extends AcceleratorExecutionNode {
		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<long[]> container1;
		protected final DataContainer<long[]> container2;
		protected final Boolx1Int64x2ScalarCacheSynchronizer synchronizer;

		public Int64ScalarComparisonNode(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Boolx1Int64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}
	}

	private final class Int64ScalarLtNode extends Int64ScalarComparisonNode {

		public Int64ScalarLtNode(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Boolx1Int64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			this.container0.getArrayData()[ this.container0.getArrayOffset() ] =
			this.container1.getArrayData()[ this.container1.getArrayOffset() ] <
			this.container2.getArrayData()[ this.container2.getArrayOffset() ] ;
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64ScalarGtNode extends Int64ScalarComparisonNode {

		public Int64ScalarGtNode(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Boolx1Int64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			this.container0.getArrayData()[ this.container0.getArrayOffset() ] =
			this.container1.getArrayData()[ this.container1.getArrayOffset() ] >
			this.container2.getArrayData()[ this.container2.getArrayOffset() ] ;
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64ScalarLeqNode extends Int64ScalarComparisonNode {

		public Int64ScalarLeqNode(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Boolx1Int64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			this.container0.getArrayData()[ this.container0.getArrayOffset() ] =
			this.container1.getArrayData()[ this.container1.getArrayOffset() ] <=
			this.container2.getArrayData()[ this.container2.getArrayOffset() ] ;
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64ScalarGeqNode extends Int64ScalarComparisonNode {

		public Int64ScalarGeqNode(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Boolx1Int64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			this.container0.getArrayData()[ this.container0.getArrayOffset() ] =
			this.container1.getArrayData()[ this.container1.getArrayOffset() ] >=
			this.container2.getArrayData()[ this.container2.getArrayOffset() ] ;
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}


	private final class Int64ScalarEqNode extends Int64ScalarComparisonNode {

		public Int64ScalarEqNode(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Boolx1Int64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			this.container0.getArrayData()[ this.container0.getArrayOffset() ] =
			this.container1.getArrayData()[ this.container1.getArrayOffset() ] ==
			this.container2.getArrayData()[ this.container2.getArrayOffset() ] ;
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64ScalarNeqNode extends Int64ScalarComparisonNode {

		public Int64ScalarNeqNode(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Boolx1Int64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			this.container0.getArrayData()[ this.container0.getArrayOffset() ] =
			this.container1.getArrayData()[ this.container1.getArrayOffset() ] !=
			this.container2.getArrayData()[ this.container2.getArrayOffset() ] ;
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

}
