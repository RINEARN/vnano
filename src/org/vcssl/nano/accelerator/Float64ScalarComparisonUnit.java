/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.memory.DataContainer;

public class Float64ScalarComparisonUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutor(
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
				executor = new Float64ScalarLtExecutor(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case GT : {
				executor = new Float64ScalarGtExecutor(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case LEQ : {
				executor = new Float64ScalarLeqExecutor(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case GEQ : {
				executor = new Float64ScalarGeqExecutor(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case EQ : {
				executor = new Float64ScalarEqExecutor(container0, container1, container2, synchronizer, nextNode);
				break;
			}
			case NEQ : {
				executor = new Float64ScalarNeqExecutor(container0, container1, container2, synchronizer, nextNode);
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

	private abstract class Float64ScalarComparisonExecutor extends AccelerationExecutorNode {
		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<double[]> container1;
		protected final DataContainer<double[]> container2;
		protected final Boolx1Int64x2ScalarCacheSynchronizer synchronizer;

		public Float64ScalarComparisonExecutor(
				DataContainer<boolean[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Boolx1Int64x2ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}
	}

	private final class Float64ScalarLtExecutor extends Float64ScalarComparisonExecutor {

		public Float64ScalarLtExecutor(
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

	private final class Float64ScalarGtExecutor extends Float64ScalarComparisonExecutor {

		public Float64ScalarGtExecutor(
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

	private final class Float64ScalarLeqExecutor extends Float64ScalarComparisonExecutor {

		public Float64ScalarLeqExecutor(
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

	private final class Float64ScalarGeqExecutor extends Float64ScalarComparisonExecutor {

		public Float64ScalarGeqExecutor(
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


	private final class Float64ScalarEqExecutor extends Float64ScalarComparisonExecutor {

		public Float64ScalarEqExecutor(
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

	private final class Float64ScalarNeqExecutor extends Float64ScalarComparisonExecutor {

		public Float64ScalarNeqExecutor(
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
