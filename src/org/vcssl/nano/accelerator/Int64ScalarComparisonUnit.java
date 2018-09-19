/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.processor.OperationCode;

public class Int64ScalarComparisonUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant) {

		DataContainer<boolean[]> cont0 = (DataContainer<boolean[]>)operandContainers[0];
		DataContainer<long[]> cont1 = (DataContainer<long[]>)operandContainers[1];
		DataContainer<long[]> cont2 = (DataContainer<long[]>)operandContainers[2];
		Boolx1Int64x2CacheSynchronizer synchronizer
				= new Boolx1Int64x2CacheSynchronizer(operandContainers, operandCaches, operandCached);

		AccelerationExecutorNode executor = null;
		switch (opcode) {
			case LT : {
				executor = new Int64ScalarLtExecutor(cont0, cont1, cont2, synchronizer);
				break;
			}
			case GT : {
				executor = new Int64ScalarGtExecutor(cont0, cont1, cont2, synchronizer);
				break;
			}
			case LEQ : {
				executor = new Int64ScalarLeqExecutor(cont0, cont1, cont2, synchronizer);
				break;
			}
			case GEQ : {
				executor = new Int64ScalarGeqExecutor(cont0, cont1, cont2, synchronizer);
				break;
			}
			case EQ : {
				executor = new Int64ScalarEqExecutor(cont0, cont1, cont2, synchronizer);
				break;
			}
			case NEQ : {
				executor = new Int64ScalarNeqExecutor(cont0, cont1, cont2, synchronizer);
				break;
			}
			default : {
				break;
			}
		}
		return executor;
	}

	private abstract class Int64ScalarComparisonExecutor extends AccelerationExecutorNode {
		protected final DataContainer<boolean[]> container0;
		protected final DataContainer<long[]> container1;
		protected final DataContainer<long[]> container2;
		protected final Boolx1Int64x2CacheSynchronizer synchronizer;

		public Int64ScalarComparisonExecutor(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Boolx1Int64x2CacheSynchronizer synchronizer) {

			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}
	}

	private final class Int64ScalarLtExecutor extends Int64ScalarComparisonExecutor {

		public Int64ScalarLtExecutor(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Boolx1Int64x2CacheSynchronizer synchronizer) {

			super(container0, container1, container2, synchronizer);
		}

		public final int execute(int programCounter) {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] <
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return programCounter + 1;
		}
	}

	private final class Int64ScalarGtExecutor extends Int64ScalarComparisonExecutor {

		public Int64ScalarGtExecutor(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Boolx1Int64x2CacheSynchronizer synchronizer) {

			super(container0, container1, container2, synchronizer);
		}

		public final int execute(int programCounter) {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] >
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return programCounter + 1;
		}
	}

	private final class Int64ScalarLeqExecutor extends Int64ScalarComparisonExecutor {

		public Int64ScalarLeqExecutor(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Boolx1Int64x2CacheSynchronizer synchronizer) {

			super(container0, container1, container2, synchronizer);
		}

		public final int execute(int programCounter) {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] <=
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return programCounter + 1;
		}
	}

	private final class Int64ScalarGeqExecutor extends Int64ScalarComparisonExecutor {

		public Int64ScalarGeqExecutor(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Boolx1Int64x2CacheSynchronizer synchronizer) {

			super(container0, container1, container2, synchronizer);
		}

		public final int execute(int programCounter) {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] >=
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return programCounter + 1;
		}
	}


	private final class Int64ScalarEqExecutor extends Int64ScalarComparisonExecutor {

		public Int64ScalarEqExecutor(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Boolx1Int64x2CacheSynchronizer synchronizer) {

			super(container0, container1, container2, synchronizer);
		}

		public final int execute(int programCounter) {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] ==
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return programCounter + 1;
		}
	}

	private final class Int64ScalarNeqExecutor extends Int64ScalarComparisonExecutor {

		public Int64ScalarNeqExecutor(
				DataContainer<boolean[]> container0, DataContainer<long[]> container1, DataContainer<long[]> container2,
				Boolx1Int64x2CacheSynchronizer synchronizer) {

			super(container0, container1, container2, synchronizer);
		}

		public final int execute(int programCounter) {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] !=
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return programCounter + 1;
		}
	}

}
