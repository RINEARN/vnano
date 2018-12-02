/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public class Float64ScalarComparisonUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutorNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		Boolx1Int64x2ScalarCacheSynchronizer synchronizer
				= new Boolx1Int64x2ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);

		DataContainer<boolean[]> container0 = (DataContainer<boolean[]>)operandContainers[0];
		DataContainer<double[]> container1 = (DataContainer<double[]>)operandContainers[1];
		DataContainer<double[]> container2 = (DataContainer<double[]>)operandContainers[2];

		AccelerationExecutorNode executor = null;
		switch (instruction.getOperationCode()) {
			case LT : {
				executor = new Float64ScalarLtExecutorNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case GT : {
				executor = new Float64ScalarGtExecutorNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case LEQ : {
				executor = new Float64ScalarLeqExecutorNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case GEQ : {
				executor = new Float64ScalarGeqExecutorNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case EQ : {
				executor = new Float64ScalarEqExecutorNode(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case NEQ : {
				executor = new Float64ScalarNeqExecutorNode(container0, container1, container2, synchronizer, nextNode);
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

	private abstract class Float64ScalarComparisonExecutorNode extends AccelerationExecutorNode {
		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<double[]> container1;
		protected final DataContainer<double[]> container2;
		protected final Boolx1Int64x2ScalarCacheSynchronizer synchronizer;

		public Float64ScalarComparisonExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Boolx1Int64x2ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}
	}

	private final class Float64ScalarLtExecutorNode extends Float64ScalarComparisonExecutorNode {

		public Float64ScalarLtExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Boolx1Int64x2ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] <
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64ScalarGtExecutorNode extends Float64ScalarComparisonExecutorNode {

		public Float64ScalarGtExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Boolx1Int64x2ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] >
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64ScalarLeqExecutorNode extends Float64ScalarComparisonExecutorNode {

		public Float64ScalarLeqExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Boolx1Int64x2ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] <=
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64ScalarGeqExecutorNode extends Float64ScalarComparisonExecutorNode {

		public Float64ScalarGeqExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Boolx1Int64x2ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] >=
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}


	private final class Float64ScalarEqExecutorNode extends Float64ScalarComparisonExecutorNode {

		public Float64ScalarEqExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Boolx1Int64x2ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] ==
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64ScalarNeqExecutorNode extends Float64ScalarComparisonExecutorNode {

		public Float64ScalarNeqExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Boolx1Int64x2ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(container0, container1, container2, synchronizer, nextNode);
		}

		public final AccelerationExecutorNode execute() {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] !=
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

}
