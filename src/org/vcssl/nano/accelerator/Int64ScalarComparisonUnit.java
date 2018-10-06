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
	public AccelerationExecutorNode generateExecutorNode(
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
				executor = new Int64ScalarLtExecutorNode(cont0, cont1, cont2, synchronizer, nextNode);
				break;
			}
			case GT : {
				executor = new Int64ScalarGtExecutorNode(cont0, cont1, cont2, synchronizer, nextNode);
				break;
			}
			case LEQ : {
				executor = new Int64ScalarLeqExecutorNode(cont0, cont1, cont2, synchronizer, nextNode);
				break;
			}
			case GEQ : {
				executor = new Int64ScalarGeqExecutorNode(cont0, cont1, cont2, synchronizer, nextNode);
				break;
			}
			case EQ : {
				executor = new Int64ScalarEqExecutorNode(cont0, cont1, cont2, synchronizer, nextNode);
				break;
			}
			case NEQ : {
				executor = new Int64ScalarNeqExecutorNode(cont0, cont1, cont2, synchronizer, nextNode);
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

	private abstract class Int64ScalarComparisonExecutorNode extends AccelerationExecutorNode {
		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<long[]> container1;
		protected final DataContainer<long[]> container2;
		protected final Boolx1Int64x2ScalarCacheSynchronizer synchronizer;

		public Int64ScalarComparisonExecutorNode(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Boolx1Int64x2ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}
	}

	private final class Int64ScalarLtExecutorNode extends Int64ScalarComparisonExecutorNode {

		public Int64ScalarLtExecutorNode(
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

	private final class Int64ScalarGtExecutorNode extends Int64ScalarComparisonExecutorNode {

		public Int64ScalarGtExecutorNode(
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

	private final class Int64ScalarLeqExecutorNode extends Int64ScalarComparisonExecutorNode {

		public Int64ScalarLeqExecutorNode(
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

	private final class Int64ScalarGeqExecutorNode extends Int64ScalarComparisonExecutorNode {

		public Int64ScalarGeqExecutorNode(
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


	private final class Int64ScalarEqExecutorNode extends Int64ScalarComparisonExecutorNode {

		public Int64ScalarEqExecutorNode(
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

	private final class Int64ScalarNeqExecutorNode extends Int64ScalarComparisonExecutorNode {

		public Int64ScalarNeqExecutorNode(
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
