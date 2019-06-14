/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.DataContainer;

public class Int64VectorDualArithmeticUnit extends AcceleratorExecutionUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		DataContainer<double[]>[] containers = (DataContainer<double[]>[])operandContainers;
		Int64x3ScalarCacheSynchronizer synchronizer
				= new Int64x3ScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled);

		OperationCode[] fusedOpcodes = instruction.getFusedOperationCodes();
		int fusedInputOperandIndex = instruction.getFusedInputOperandIndices()[0];

		Int64VectorDualArithmeticNode node = null;
		if (fusedInputOperandIndex == 1) {
			node = this.generateLeftInputNode(fusedOpcodes, containers, synchronizer, nextNode);
		} else if (fusedInputOperandIndex == 2) {
			node = this.generateRightInputNode(fusedOpcodes, containers, synchronizer, nextNode);
		} else {
			throw new VnanoFatalException("Invalid fused input operand index: " + fusedInputOperandIndex);
		}

		return node;
	}


	private Int64VectorDualArithmeticNode generateLeftInputNode(
			OperationCode[] fusedOpcodes, DataContainer<double[]>[] containers,
			Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

		Int64VectorDualArithmeticNode node = null;
		switch (fusedOpcodes[0]) {
			case ADD : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						node = new Int64VectorAddAddLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						node = new Int64VectorAddSubLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						node = new Int64VectorAddMulLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						node = new Int64VectorAddDivLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						node = new Int64VectorAddRemLeftInputNode(containers,synchronizer,nextNode);
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
						node = new Int64VectorSubAddLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						node = new Int64VectorSubSubLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						node = new Int64VectorSubMulLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						node = new Int64VectorSubDivLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						node = new Int64VectorSubRemLeftInputNode(containers,synchronizer,nextNode);
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
						node = new Int64VectorMulAddLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						node = new Int64VectorMulSubLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						node = new Int64VectorMulMulLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						node = new Int64VectorMulDivLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						node = new Int64VectorMulRemLeftInputNode(containers,synchronizer,nextNode);
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
						node = new Int64VectorDivAddLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						node = new Int64VectorDivSubLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						node = new Int64VectorDivMulLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						node = new Int64VectorDivDivLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						node = new Int64VectorDivRemLeftInputNode(containers,synchronizer,nextNode);
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
						node = new Int64VectorRemAddLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						node = new Int64VectorRemSubLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						node = new Int64VectorRemMulLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						node = new Int64VectorRemDivLeftInputNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						node = new Int64VectorRemRemLeftInputNode(containers,synchronizer,nextNode);
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


	private Int64VectorDualArithmeticNode generateRightInputNode(
			OperationCode[] fusedOpcodes, DataContainer<double[]>[] containers,
			Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

		Int64VectorDualArithmeticNode node = null;
		switch (fusedOpcodes[0]) {
			case ADD : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						node = new Int64VectorAddAddRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						node = new Int64VectorAddSubRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						node = new Int64VectorAddMulRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						node = new Int64VectorAddDivRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						node = new Int64VectorAddRemRightInputNode(containers,synchronizer,nextNode);
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
						node = new Int64VectorSubAddRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						node = new Int64VectorSubSubRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						node = new Int64VectorSubMulRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						node = new Int64VectorSubDivRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						node = new Int64VectorSubRemRightInputNode(containers,synchronizer,nextNode);
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
						node = new Int64VectorMulAddRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						node = new Int64VectorMulSubRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						node = new Int64VectorMulMulRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						node = new Int64VectorMulDivRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						node = new Int64VectorMulRemRightInputNode(containers,synchronizer,nextNode);
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
						node = new Int64VectorDivAddRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						node = new Int64VectorDivSubRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						node = new Int64VectorDivMulRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						node = new Int64VectorDivDivRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						node = new Int64VectorDivRemRightInputNode(containers,synchronizer,nextNode);
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
						node = new Int64VectorRemAddRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						node = new Int64VectorRemSubRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						node = new Int64VectorRemMulRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						node = new Int64VectorRemDivRightInputNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						node = new Int64VectorRemRemRightInputNode(containers,synchronizer,nextNode);
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


	private abstract class Int64VectorDualArithmeticNode extends AcceleratorExecutionNode {
		//protected final DataContainer<double[]> container00;
		protected final DataContainer<double[]> container01;
		protected final DataContainer<double[]> container02;
		protected final DataContainer<double[]> container10;
		protected final DataContainer<double[]> container11;
		protected final DataContainer<double[]> container12;
		protected final Int64x3ScalarCacheSynchronizer synchronizer;

		public Int64VectorDualArithmeticNode(DataContainer<double[]>[] containers,
				Int64x3ScalarCacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

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

	private final class Int64VectorAddAddLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorAddAddLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorAddAddRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorAddAddRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorAddSubLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorAddSubLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorAddSubRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorAddSubRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorAddMulLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorAddMulLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorAddMulRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorAddMulRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorAddDivLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorAddDivLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorAddDivRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorAddDivRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorAddRemLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorAddRemLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorAddRemRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorAddRemRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorSubAddLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorSubAddLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorSubAddRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorSubAddRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorSubSubLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorSubSubLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorSubSubRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorSubSubRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorSubMulLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorSubMulLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorSubMulRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorSubMulRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorSubDivLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorSubDivLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorSubDivRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorSubDivRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorSubRemLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorSubRemLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorSubRemRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorSubRemRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorMulAddLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorMulAddLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorMulAddRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorMulAddRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorMulSubLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorMulSubLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorMulSubRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorMulSubRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorMulMulLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorMulMulLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorMulMulRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorMulMulRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorMulDivLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorMulDivLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorMulDivRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorMulDivRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorMulRemLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorMulRemLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorMulRemRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorMulRemRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorDivAddLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorDivAddLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorDivAddRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorDivAddRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorDivSubLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorDivSubLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorDivSubRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorDivSubRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorDivMulLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorDivMulLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorDivMulRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorDivMulRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorDivDivLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorDivDivLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorDivDivRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorDivDivRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorDivRemLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorDivRemLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorDivRemRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorDivRemRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorRemAddLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorRemAddLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorRemAddRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorRemAddRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorRemSubLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorRemSubLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorRemSubRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorRemSubRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorRemMulLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorRemMulLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorRemMulRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorRemMulRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorRemDivLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorRemDivLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorRemDivRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorRemDivRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorRemRemLeftInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorRemRemLeftInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

	private final class Int64VectorRemRemRightInputNode extends Int64VectorDualArithmeticNode {
		public Int64VectorRemRemRightInputNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
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

