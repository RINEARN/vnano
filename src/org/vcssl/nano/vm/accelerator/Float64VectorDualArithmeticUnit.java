/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.DataContainer;

public class Float64VectorDualArithmeticUnit extends AcceleratorExecutionUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		DataContainer<double[]>[] containers = (DataContainer<double[]>[])operandContainers;
		Float64x3ScalarCacheSynchronizer synchronizer
				= new Float64x3ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);

		OperationCode[] fusedOpcodes = instruction.getFusedOperationCodes();
		int fusedInputOperandIndex = instruction.getFusedInputOperandIndices()[0];

		Float64VectorDualArithmeticNode node = null;
		if (fusedInputOperandIndex == 1) {
			node = this.generateLeftInputNode(fusedOpcodes, containers, synchronizer, nextNode);
		} else if (fusedInputOperandIndex == 2) {
			node = this.generateRightInputNode(fusedOpcodes, containers, synchronizer, nextNode);
		} else {
			throw new VnanoFatalException("Invalid fused input operand index: " + fusedInputOperandIndex);
		}

		return node;
	}


	private Float64VectorDualArithmeticNode generateLeftInputNode(
			OperationCode[] fusedOpcodes, DataContainer<double[]>[] containers,
			Float64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

		Float64VectorDualArithmeticNode node = null;
		switch (fusedOpcodes[0]) {
			case ADD : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						node = new Float64VectorAddAddLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						node = new Float64VectorAddSubLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						node = new Float64VectorAddMulLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						node = new Float64VectorAddDivLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						node = new Float64VectorAddRemLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					default : {
						throw new VnanoFatalException(
								"Operation code " + fusedOpcodes[1] + " is invalid for this unit"
						);
					}
				}
				break;
			}

			case SUB : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						node = new Float64VectorSubAddLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						node = new Float64VectorSubSubLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						node = new Float64VectorSubMulLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						node = new Float64VectorSubDivLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						node = new Float64VectorSubRemLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					default : {
						throw new VnanoFatalException(
								"Operation code " + fusedOpcodes[1] + " is invalid for this unit"
						);
					}
				}
				break;
			}

			case MUL : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						node = new Float64VectorMulAddLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						node = new Float64VectorMulSubLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						node = new Float64VectorMulMulLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						node = new Float64VectorMulDivLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						node = new Float64VectorMulRemLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					default : {
						throw new VnanoFatalException(
								"Operation code " + fusedOpcodes[1] + " is invalid for this unit"
						);
					}
				}
				break;
			}

			case DIV : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						node = new Float64VectorDivAddLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						node = new Float64VectorDivSubLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						node = new Float64VectorDivMulLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						node = new Float64VectorDivDivLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						node = new Float64VectorDivRemLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					default : {
						throw new VnanoFatalException(
								"Operation code " + fusedOpcodes[1] + " is invalid for this unit"
						);
					}
				}
				break;
			}

			case REM : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						node = new Float64VectorRemAddLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						node = new Float64VectorRemSubLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						node = new Float64VectorRemMulLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						node = new Float64VectorRemDivLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						node = new Float64VectorRemRemLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					default : {
						throw new VnanoFatalException(
								"Operation code " + fusedOpcodes[1] + " is invalid for this unit"
						);
					}
				}
				break;
			}

			default : {
				throw new VnanoFatalException(
						"Operation code " + fusedOpcodes[0] + " is invalid for for this unit"
				);
			}
		}
		return node;

	}


	private Float64VectorDualArithmeticNode generateRightInputNode(
			OperationCode[] fusedOpcodes, DataContainer<double[]>[] containers,
			Float64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

		Float64VectorDualArithmeticNode node = null;
		switch (fusedOpcodes[0]) {
			case ADD : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						node = new Float64VectorAddAddRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						node = new Float64VectorAddSubRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						node = new Float64VectorAddMulRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						node = new Float64VectorAddDivRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						node = new Float64VectorAddRemRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					default : {
						throw new VnanoFatalException(
								"Operation code " + fusedOpcodes[1] + " is invalid for this unit"
						);
					}
				}
				break;
			}

			case SUB : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						node = new Float64VectorSubAddRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						node = new Float64VectorSubSubRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						node = new Float64VectorSubMulRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						node = new Float64VectorSubDivRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						node = new Float64VectorSubRemRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					default : {
						throw new VnanoFatalException(
								"Operation code " + fusedOpcodes[1] + " is invalid for this unit"
						);
					}
				}
				break;
			}

			case MUL : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						node = new Float64VectorMulAddRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						node = new Float64VectorMulSubRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						node = new Float64VectorMulMulRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						node = new Float64VectorMulDivRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						node = new Float64VectorMulRemRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					default : {
						throw new VnanoFatalException(
								"Operation code " + fusedOpcodes[1] + " is invalid for this unit"
						);
					}
				}
				break;
			}

			case DIV : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						node = new Float64VectorDivAddRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						node = new Float64VectorDivSubRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						node = new Float64VectorDivMulRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						node = new Float64VectorDivDivRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						node = new Float64VectorDivRemRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					default : {
						throw new VnanoFatalException(
								"Operation code " + fusedOpcodes[1] + " is invalid for this unit"
						);
					}
				}
				break;
			}

			case REM : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						node = new Float64VectorRemAddRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						node = new Float64VectorRemSubRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						node = new Float64VectorRemMulRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						node = new Float64VectorRemDivRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						node = new Float64VectorRemRemRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					default : {
						throw new VnanoFatalException(
								"Operation code " + fusedOpcodes[1] + " is invalid for this unit"
						);
					}
				}
				break;
			}

			default : {
				throw new VnanoFatalException(
						"Operation code " + fusedOpcodes[0] + " is invalid for this unit"
				);
			}
		}
		return node;
	}


	private abstract class Float64VectorDualArithmeticNode extends AcceleratorExecutionNode {
		//protected final DataContainer<double[]> container00;
		protected final DataContainer<double[]> container01;
		protected final DataContainer<double[]> container02;
		protected final DataContainer<double[]> container10;
		protected final DataContainer<double[]> container11;
		protected final DataContainer<double[]> container12;
		protected final Float64x3ScalarCacheSynchronizer synchronizer;

		public Float64VectorDualArithmeticNode(DataContainer<double[]>[] containers,
				Float64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode);
			//this.container00 = containers[0];
			this.container01 = containers[1];
			this.container02 = containers[2];
			this.container10 = containers[0];
			this.container11 = containers[1];
			this.container12 = containers[2];
			this.synchronizer = synchronizer;
		}
	}


	// ADD ADD

	private final class Float64VectorAddAddLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorAddAddLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] + data02[i]) + data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorAddAddRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorAddAddRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] + (data01[i] + data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	// ADD SUB

	private final class Float64VectorAddSubLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorAddSubLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] + data02[i]) - data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorAddSubRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorAddSubRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] - (data01[i] + data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	// ADD MUL

	private final class Float64VectorAddMulLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorAddMulLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] + data02[i]) * data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorAddMulRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorAddMulRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] * (data01[i] + data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	// ADD DIV

	private final class Float64VectorAddDivLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorAddDivLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] + data02[i]) / data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorAddDivRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorAddDivRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] / (data01[i] + data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	// ADD REM

	private final class Float64VectorAddRemLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorAddRemLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] + data02[i]) % data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorAddRemRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorAddRemRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] % (data01[i] + data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}





	// SUB ADD

	private final class Float64VectorSubAddLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorSubAddLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] - data02[i]) + data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorSubAddRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorSubAddRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] + (data01[i] - data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	// SUB SUB

	private final class Float64VectorSubSubLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorSubSubLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] - data02[i]) - data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorSubSubRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorSubSubRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] - (data01[i] - data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	// SUB MUL

	private final class Float64VectorSubMulLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorSubMulLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] - data02[i]) * data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorSubMulRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorSubMulRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] * (data01[i] - data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	// SUB DIV

	private final class Float64VectorSubDivLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorSubDivLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] - data02[i]) / data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorSubDivRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorSubDivRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] / (data01[i] - data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	// SUB REM

	private final class Float64VectorSubRemLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorSubRemLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] - data02[i]) % data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorSubRemRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorSubRemRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] % (data01[i] - data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}





	// MUL ADD

	private final class Float64VectorMulAddLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorMulAddLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] * data02[i]) + data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorMulAddRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorMulAddRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] + (data01[i] * data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	// MUL SUB

	private final class Float64VectorMulSubLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorMulSubLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] * data02[i]) - data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorMulSubRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorMulSubRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] - (data01[i] * data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	// MUL MUL

	private final class Float64VectorMulMulLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorMulMulLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] * data02[i]) * data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorMulMulRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorMulMulRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] * (data01[i] * data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	// MUL DIV

	private final class Float64VectorMulDivLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorMulDivLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] * data02[i]) / data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorMulDivRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorMulDivRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] / (data01[i] * data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	// MUL REM

	private final class Float64VectorMulRemLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorMulRemLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] * data02[i]) % data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorMulRemRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorMulRemRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] % (data01[i] * data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	// DIV ADD

	private final class Float64VectorDivAddLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorDivAddLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] / data02[i]) + data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorDivAddRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorDivAddRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] + (data01[i] / data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	// DIV SUB

	private final class Float64VectorDivSubLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorDivSubLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] / data02[i]) - data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorDivSubRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorDivSubRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] - (data01[i] / data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	// DIV MUL

	private final class Float64VectorDivMulLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorDivMulLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] / data02[i]) * data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorDivMulRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorDivMulRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] * (data01[i] / data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	// DIV DIV

	private final class Float64VectorDivDivLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorDivDivLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] / data02[i]) / data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorDivDivRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorDivDivRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] / (data01[i] / data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	// DIV REM

	private final class Float64VectorDivRemLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorDivRemLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] / data02[i]) % data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorDivRemRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorDivRemRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] % (data01[i] / data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}





	// REM ADD

	private final class Float64VectorRemAddLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorRemAddLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] % data02[i]) + data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorRemAddRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorRemAddRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] + (data01[i] % data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	// REM SUB

	private final class Float64VectorRemSubLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorRemSubLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] % data02[i]) - data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorRemSubRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorRemSubRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] - (data01[i] % data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	// REM MUL

	private final class Float64VectorRemMulLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorRemMulLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] % data02[i]) * data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorRemMulRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorRemMulRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] * (data01[i] % data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	// REM DIV

	private final class Float64VectorRemDivLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorRemDivLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] % data02[i]) / data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorRemDivRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorRemDivRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] / (data01[i] % data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	// REM REM

	private final class Float64VectorRemRemLeftInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorRemRemLeftInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			//double[] data11 = this.container11.getData();
			double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = (data01[i] % data02[i]) % data12[i];
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorRemRemRightInputNode extends Float64VectorDualArithmeticNode {
		public Float64VectorRemRemRightInputNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AcceleratorExecutionNode execute() {

			//double[] data00 = this.container00.getData();
			double[] data01 = this.container01.getData();
			double[] data02 = this.container02.getData();
			double[] data10 = this.container10.getData();
			double[] data11 = this.container11.getData();
			//double[] data12 = this.container12.getData();
			final int size = this.container10.getSize();

			for (int i=0; i<size; ++i) {
				data10[i] = data11[i] % (data01[i] % data02[i]);
			}

			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

}

