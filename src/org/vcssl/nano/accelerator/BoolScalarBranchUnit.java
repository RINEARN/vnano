/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.processor.OperationCode;

public class BoolScalarBranchUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant) {

		DataContainer<boolean[]> container0 = (DataContainer<boolean[]>)operandContainers[0];
		Boolx1CacheSynchronizer synchronizer
				= new Boolx1CacheSynchronizer(operandContainers, operandCaches, operandCached);

		// ラベル番地はメモリマッピング時点で確定していて不変なので、この段階で控える
		int jumpAddress = (int)( (long[])operandContainers[1].getData() )[0];

		AccelerationExecutorNode executor = null;
		switch (opcode) {
			case JMP : {
				executor = new ScalarJmpExecutor(container0, jumpAddress, synchronizer);
				break;
			}
			case JMPN : {
				executor = new ScalarJmpnExecutor(container0, jumpAddress, synchronizer);
				break;
			}
			default : {
				break;
			}
		}
		return executor;
	}


	private final class ScalarJmpExecutor extends AccelerationExecutorNode {
		private final DataContainer<boolean[]> container0;
		private final Boolx1CacheSynchronizer synchronizer;
		private final int jumpAddress;

		public ScalarJmpExecutor(
				DataContainer<boolean[]> container0, int jumpAddress,
				Boolx1CacheSynchronizer synchronizer) {

			this.container0 = container0;
			this.jumpAddress = jumpAddress;
			this.synchronizer = synchronizer;
		}

		public final int execute(int programCounter) {
			this.synchronizer.readCache();
			if (this.container0.getData()[ this.container0.getOffset() ]) {
				return this.jumpAddress;
			} else {
				return programCounter + 1;
			}

		}
	}
	private final class ScalarJmpnExecutor extends AccelerationExecutorNode {
		private final DataContainer<boolean[]> container0;
		private final Boolx1CacheSynchronizer synchronizer;
		private final int jumpAddress;

		public ScalarJmpnExecutor(
				DataContainer<boolean[]> container0, int jumpAddress,
				Boolx1CacheSynchronizer synchronizer) {

			this.container0 = container0;
			this.jumpAddress = jumpAddress;
			this.synchronizer = synchronizer;
		}

		public final int execute(int programCounter) {
			this.synchronizer.readCache();
			if (this.container0.getData()[ this.container0.getOffset() ]) {
				return programCounter + 1;
			} else {
				return this.jumpAddress;
			}
		}
	}

}
