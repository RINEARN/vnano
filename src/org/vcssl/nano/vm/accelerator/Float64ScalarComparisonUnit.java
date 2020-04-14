/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class Float64ScalarComparisonUnit extends AcceleratorExecutionUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		Boolx1Float64x2ScalarCacheSynchronizer synchronizer
				= new Boolx1Float64x2ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled);

		DataContainer<boolean[]> container0 = (DataContainer<boolean[]>)operandContainers[0];
		DataContainer<double[]> container1 = (DataContainer<double[]>)operandContainers[1];
		DataContainer<double[]> container2 = (DataContainer<double[]>)operandContainers[2];

		AcceleratorExecutionNode node = null;
		switch (instruction.getOperationCode()) {
			case LT : {
				node = new Float64ScalarLtNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case GT : {
				node = new Float64ScalarGtNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case LEQ : {
				node = new Float64ScalarLeqNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case GEQ : {
				node = new Float64ScalarGeqNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case EQ : {
				node = new Float64ScalarEqNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case NEQ : {
				node = new Float64ScalarNeqNode(container0, container1, container2, synchronizer, nextNode);
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

	private abstract class Float64ScalarComparisonNode extends AcceleratorExecutionNode {
		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<double[]> container1;
		protected final DataContainer<double[]> container2;
		protected final Boolx1Float64x2ScalarCacheSynchronizer synchronizer;

		public Float64ScalarComparisonNode(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Boolx1Float64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}
	}

	private final class Float64ScalarLtNode extends Float64ScalarComparisonNode {

		public Float64ScalarLtNode(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Boolx1Float64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] <
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64ScalarGtNode extends Float64ScalarComparisonNode {

		public Float64ScalarGtNode(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Boolx1Float64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] >
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64ScalarLeqNode extends Float64ScalarComparisonNode {

		public Float64ScalarLeqNode(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Boolx1Float64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] <=
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64ScalarGeqNode extends Float64ScalarComparisonNode {

		public Float64ScalarGeqNode(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Boolx1Float64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] >=
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}


	private final class Float64ScalarEqNode extends Float64ScalarComparisonNode {

		public Float64ScalarEqNode(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Boolx1Float64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] ==
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64ScalarNeqNode extends Float64ScalarComparisonNode {

		public Float64ScalarNeqNode(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Boolx1Float64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] !=
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

}
