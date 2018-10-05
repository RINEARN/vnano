/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.memory.DataContainer;

public class Int64ScalarComparisonUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutor(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		DataContainer<boolean[]> cont0 = (DataContainer<boolean[]>)operandContainers[0];
		DataContainer<long[]> cont1 = (DataContainer<long[]>)operandContainers[1];
		DataContainer<long[]> cont2 = (DataContainer<long[]>)operandContainers[2];
		Boolx1Int64x2ScalarCacheSynchronizer synchronizer
				= new Boolx1Int64x2ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);

		AccelerationExecutorNode executor = null;
		switch (instruction.getOperationCode()) {
			case LT : {
				executor = new Int64ScalarLtExecutor(cont0, cont1, cont2, synchronizer, nextNode);
				break;
			}
			case GT : {
				executor = new Int64ScalarGtExecutor(cont0, cont1, cont2, synchronizer, nextNode);
				break;
			}
			case LEQ : {
				executor = new Int64ScalarLeqExecutor(cont0, cont1, cont2, synchronizer, nextNode);
				break;
			}
			case GEQ : {
				executor = new Int64ScalarGeqExecutor(cont0, cont1, cont2, synchronizer, nextNode);
				break;
			}
			case EQ : {
				executor = new Int64ScalarEqExecutor(cont0, cont1, cont2, synchronizer, nextNode);
				break;
			}
			case NEQ : {
				executor = new Int64ScalarNeqExecutor(cont0, cont1, cont2, synchronizer, nextNode);
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

	private abstract class Int64ScalarComparisonExecutor extends AccelerationExecutorNode {
		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<long[]> container1;
		protected final DataContainer<long[]> container2;
		protected final Boolx1Int64x2ScalarCacheSynchronizer synchronizer;

		public Int64ScalarComparisonExecutor(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Boolx1Int64x2ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}
	}

	private final class Int64ScalarLtExecutor extends Int64ScalarComparisonExecutor {

		public Int64ScalarLtExecutor(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
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

	private final class Int64ScalarGtExecutor extends Int64ScalarComparisonExecutor {

		public Int64ScalarGtExecutor(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
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

	private final class Int64ScalarLeqExecutor extends Int64ScalarComparisonExecutor {

		public Int64ScalarLeqExecutor(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
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

	private final class Int64ScalarGeqExecutor extends Int64ScalarComparisonExecutor {

		public Int64ScalarGeqExecutor(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
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


	private final class Int64ScalarEqExecutor extends Int64ScalarComparisonExecutor {

		public Int64ScalarEqExecutor(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
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

	private final class Int64ScalarNeqExecutor extends Int64ScalarComparisonExecutor {

		public Int64ScalarNeqExecutor(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
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
