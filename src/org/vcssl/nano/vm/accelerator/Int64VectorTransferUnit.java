/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import java.util.Arrays;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.vm.memory.DataContainer;

public class Int64VectorTransferUnit extends AcceleratorExecutionUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		AcceleratorExecutionNode node = null;
		switch (instruction.getOperationCode()) {
			case MOV : {
				Int64x2ScalarCacheSynchronizer synchronizer
						= new Int64x2ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled);
				node = new Int64VectorMovNode(
						(DataContainer<long[]>)operandContainers[0], (DataContainer<long[]>)operandContainers[1],
						synchronizer, nextNode);
				break;
			}
			case CAST : {
				if (instruction.getDataTypes()[1] == DataType.INT64) {
					Int64x2ScalarCacheSynchronizer synchronizer
							= new Int64x2ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled);
					node = new Int64VectorMovNode(
							(DataContainer<long[]>)operandContainers[0], (DataContainer<long[]>)operandContainers[1],
							synchronizer, nextNode);
				} else if (instruction.getDataTypes()[1] == DataType.FLOAT64) {
					Int64x1Float64x1ScalarCacheSynchronizer synchronizer
							= new Int64x1Float64x1ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled);
					node = new Int64FromFloat64VectorCastNode(
							(DataContainer<long[]>)operandContainers[0], (DataContainer<double[]>)operandContainers[1],
							synchronizer, nextNode);
				} else {
					throw new VnanoFatalException(
							instruction.getDataTypes()[1] + "-type operand of " + instruction.getOperationCode()
							+ " instruction is invalid for " + this.getClass().getCanonicalName()
					);
				}
				break;
			}
			case FILL : {
				Int64x2ScalarCacheSynchronizer synchronizer
						= new Int64x2ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled);
				node = new Int64VectorFillNode(
						(DataContainer<long[]>)operandContainers[0], (DataContainer<long[]>)operandContainers[1],
						synchronizer, nextNode);
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

	private final class Int64VectorMovNode extends AcceleratorExecutionNode {
		protected final DataContainer<long[]> container0;
		protected final DataContainer<long[]> container1;
		protected final Int64x2ScalarCacheSynchronizer synchronizer;

		public Int64VectorMovNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1,
				Int64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.container0 = container0;
			this.container1 = container1;
			this.synchronizer = synchronizer;
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			long[] data0 = this.container0.getArrayData();
			long[] data1 = this.container1.getArrayData();
			int size = this.container0.getArraySize();

			System.arraycopy(data1, 0, data0, 0, size);

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64FromFloat64VectorCastNode extends AcceleratorExecutionNode {
		protected final DataContainer<long[]> container0;
		protected final DataContainer<double[]> container1;
		protected final Int64x1Float64x1ScalarCacheSynchronizer synchronizer;

		public Int64FromFloat64VectorCastNode(
				DataContainer<long[]> container0, DataContainer<double[]> container1,
				Int64x1Float64x1ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.container0 = container0;
			this.container1 = container1;
			this.synchronizer = synchronizer;
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			long[] data0 = this.container0.getArrayData();
			double[] data1 = this.container1.getArrayData();
			int size = this.container0.getArraySize();

			for (int i=0; i<size; i++) {
				data0[i] = (long)data1[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Int64VectorFillNode extends AcceleratorExecutionNode {
		protected final DataContainer<long[]> container0;
		protected final DataContainer<long[]> container1;
		protected final Int64x2ScalarCacheSynchronizer synchronizer;

		public Int64VectorFillNode(
				DataContainer<long[]> container0, DataContainer<long[]> container1,
				Int64x2ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.container0 = container0;
			this.container1 = container1;
			this.synchronizer = synchronizer;
		}

		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			long[] data0 = this.container0.getArrayData();
			long fillValue = this.container1.getArrayData()[ this.container1.getArrayOffset() ];

			Arrays.fill(data0, fillValue);

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}


}

