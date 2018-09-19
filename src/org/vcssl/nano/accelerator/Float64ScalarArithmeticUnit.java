/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.processor.OperationCode;

public class Float64ScalarArithmeticUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant) {

		DataContainer<double[]>[] containers = (DataContainer<double[]>[])operandContainers;
		Float64x3CacheSynchronizer synchronizer =
				new Float64x3CacheSynchronizer(operandContainers, operandCaches, operandCached);

		AccelerationExecutorNode executor = null;
		switch (opcode) {
			case ADD : {
				executor = new Float64ScalarAddExecutor(containers[0], containers[1], containers[2], synchronizer);
				break;
			}
			case SUB : {
				executor = new Float64ScalarSubExecutor(containers[0], containers[1], containers[2], synchronizer);
				break;
			}
			case MUL : {
				executor = new Float64ScalarMulExecutor(containers[0], containers[1], containers[2], synchronizer);
				break;
			}
			case DIV : {
				executor = new Float64ScalarDivExecutor(containers[0], containers[1], containers[2], synchronizer);
				break;
			}
			case REM : {
				executor = new Float64ScalarRemExecutor(containers[0], containers[1], containers[2], synchronizer);
				break;
			}
			default : {
				break;
			}
		}
		return executor;
	}

	private abstract class Float64ScalarArithmeticExecutor extends AccelerationExecutorNode {
		protected final DataContainer<double[]> container0;
		protected final DataContainer<double[]> container1;
		protected final DataContainer<double[]> container2;
		protected final Float64x3CacheSynchronizer synchronizer;

		public Float64ScalarArithmeticExecutor(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3CacheSynchronizer synchronizer) {

			this.container0 = container0;
			this.container1 = container1;
			this.container2 = container2;
			this.synchronizer = synchronizer;
		}
	}

	private final class Float64ScalarAddExecutor extends Float64ScalarArithmeticExecutor {

		public Float64ScalarAddExecutor(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3CacheSynchronizer synchronizer) {

			super(container0, container1, container2, synchronizer);
		}

		public final int execute(int programCounter) {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] +
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return programCounter + 1;
		}
	}

	private final class Float64ScalarSubExecutor extends Float64ScalarArithmeticExecutor {

		public Float64ScalarSubExecutor(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3CacheSynchronizer synchronizer) {

			super(container0, container1, container2, synchronizer);
		}

		public final int execute(int programCounter) {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] -
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return programCounter + 1;
		}
	}

	private final class Float64ScalarMulExecutor extends Float64ScalarArithmeticExecutor {

		public Float64ScalarMulExecutor(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3CacheSynchronizer synchronizer) {

			super(container0, container1, container2, synchronizer);
		}

		public final int execute(int programCounter) {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] *
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return programCounter + 1;
		}
	}

	private final class Float64ScalarDivExecutor extends Float64ScalarArithmeticExecutor {

		public Float64ScalarDivExecutor(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3CacheSynchronizer synchronizer) {

			super(container0, container1, container2, synchronizer);
		}

		public final int execute(int programCounter) {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] /
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return programCounter + 1;
		}
	}

	private final class Float64ScalarRemExecutor extends Float64ScalarArithmeticExecutor {

		public Float64ScalarRemExecutor(
				DataContainer<double[]> container0, DataContainer<double[]> container1, DataContainer<double[]> container2,
				Float64x3CacheSynchronizer synchronizer) {

			super(container0, container1, container2, synchronizer);
		}

		public final int execute(int programCounter) {
			this.synchronizer.readCache();
			this.container0.getData()[ this.container0.getOffset() ] =
			this.container1.getData()[ this.container1.getOffset() ] %
			this.container2.getData()[ this.container2.getOffset() ] ;
			this.synchronizer.writeCache();
			return programCounter + 1;
		}
	}

}
