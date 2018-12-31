/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.DataContainer;

public class Int64VectorDualArithmeticUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutorNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		DataContainer<double[]>[] containers = (DataContainer<double[]>[])operandContainers;
		Int64x3ScalarCacheSynchronizer synchronizer
				= new Int64x3ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);

		OperationCode[] fusedOpcodes = instruction.getFusedOperationCodes();
		int fusedInputOperandIndex = instruction.getFusedInputOperandIndices()[0];

		Int64VectorDualArithmeticExecutorNode executor = null;
		if (fusedInputOperandIndex == 1) {
			executor = this.generateLeftInputExecutorNode(fusedOpcodes, containers, synchronizer, nextNode);
		} else if (fusedInputOperandIndex == 2) {
			executor = this.generateRightInputExecutorNode(fusedOpcodes, containers, synchronizer, nextNode);
		} else {
			throw new VnanoFatalException("Invalid fused input operand index: " + fusedInputOperandIndex);
		}

		return executor;
	}


	private Int64VectorDualArithmeticExecutorNode generateLeftInputExecutorNode(
			OperationCode[] fusedOpcodes, DataContainer<double[]>[] containers,
			Int64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

		Int64VectorDualArithmeticExecutorNode executor = null;
		switch (fusedOpcodes[0]) {
			case ADD : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						executor = new Int64VectorAddAddLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						executor = new Int64VectorAddSubLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						executor = new Int64VectorAddMulLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						executor = new Int64VectorAddDivLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						executor = new Int64VectorAddRemLeftInputExecutorNode(containers,synchronizer,nextNode);
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
						executor = new Int64VectorSubAddLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						executor = new Int64VectorSubSubLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						executor = new Int64VectorSubMulLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						executor = new Int64VectorSubDivLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						executor = new Int64VectorSubRemLeftInputExecutorNode(containers,synchronizer,nextNode);
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
						executor = new Int64VectorMulAddLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						executor = new Int64VectorMulSubLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						executor = new Int64VectorMulMulLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						executor = new Int64VectorMulDivLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						executor = new Int64VectorMulRemLeftInputExecutorNode(containers,synchronizer,nextNode);
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
						executor = new Int64VectorDivAddLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						executor = new Int64VectorDivSubLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						executor = new Int64VectorDivMulLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						executor = new Int64VectorDivDivLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						executor = new Int64VectorDivRemLeftInputExecutorNode(containers,synchronizer,nextNode);
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
						executor = new Int64VectorRemAddLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						executor = new Int64VectorRemSubLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						executor = new Int64VectorRemMulLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						executor = new Int64VectorRemDivLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						executor = new Int64VectorRemRemLeftInputExecutorNode(containers,synchronizer,nextNode);
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
		return executor;

	}


	private Int64VectorDualArithmeticExecutorNode generateRightInputExecutorNode(
			OperationCode[] fusedOpcodes, DataContainer<double[]>[] containers,
			Int64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

		Int64VectorDualArithmeticExecutorNode executor = null;
		switch (fusedOpcodes[0]) {
			case ADD : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						executor = new Int64VectorAddAddRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						executor = new Int64VectorAddSubRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						executor = new Int64VectorAddMulRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						executor = new Int64VectorAddDivRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						executor = new Int64VectorAddRemRightInputExecutorNode(containers,synchronizer,nextNode);
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
						executor = new Int64VectorSubAddRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						executor = new Int64VectorSubSubRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						executor = new Int64VectorSubMulRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						executor = new Int64VectorSubDivRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						executor = new Int64VectorSubRemRightInputExecutorNode(containers,synchronizer,nextNode);
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
						executor = new Int64VectorMulAddRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						executor = new Int64VectorMulSubRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						executor = new Int64VectorMulMulRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						executor = new Int64VectorMulDivRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						executor = new Int64VectorMulRemRightInputExecutorNode(containers,synchronizer,nextNode);
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
						executor = new Int64VectorDivAddRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						executor = new Int64VectorDivSubRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						executor = new Int64VectorDivMulRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						executor = new Int64VectorDivDivRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						executor = new Int64VectorDivRemRightInputExecutorNode(containers,synchronizer,nextNode);
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
						executor = new Int64VectorRemAddRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						executor = new Int64VectorRemSubRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						executor = new Int64VectorRemMulRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						executor = new Int64VectorRemDivRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						executor = new Int64VectorRemRemRightInputExecutorNode(containers,synchronizer,nextNode);
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
		return executor;
	}


	private abstract class Int64VectorDualArithmeticExecutorNode extends AccelerationExecutorNode {
		//protected final DataContainer<double[]> container00;
		protected final DataContainer<double[]> container01;
		protected final DataContainer<double[]> container02;
		protected final DataContainer<double[]> container10;
		protected final DataContainer<double[]> container11;
		protected final DataContainer<double[]> container12;
		protected final Int64x3ScalarCacheSynchronizer synchronizer;

		public Int64VectorDualArithmeticExecutorNode(DataContainer<double[]>[] containers,
				Int64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

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

	private final class Int64VectorAddAddLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorAddAddLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorAddAddRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorAddAddRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorAddSubLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorAddSubLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorAddSubRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorAddSubRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorAddMulLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorAddMulLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorAddMulRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorAddMulRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorAddDivLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorAddDivLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorAddDivRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorAddDivRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorAddRemLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorAddRemLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorAddRemRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorAddRemRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorSubAddLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorSubAddLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorSubAddRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorSubAddRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorSubSubLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorSubSubLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorSubSubRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorSubSubRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorSubMulLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorSubMulLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorSubMulRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorSubMulRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorSubDivLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorSubDivLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorSubDivRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorSubDivRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorSubRemLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorSubRemLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorSubRemRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorSubRemRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorMulAddLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorMulAddLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorMulAddRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorMulAddRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorMulSubLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorMulSubLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorMulSubRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorMulSubRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorMulMulLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorMulMulLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorMulMulRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorMulMulRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorMulDivLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorMulDivLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorMulDivRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorMulDivRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorMulRemLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorMulRemLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorMulRemRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorMulRemRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorDivAddLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorDivAddLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorDivAddRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorDivAddRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorDivSubLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorDivSubLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorDivSubRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorDivSubRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorDivMulLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorDivMulLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorDivMulRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorDivMulRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorDivDivLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorDivDivLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorDivDivRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorDivDivRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorDivRemLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorDivRemLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorDivRemRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorDivRemRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorRemAddLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorRemAddLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorRemAddRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorRemAddRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorRemSubLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorRemSubLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorRemSubRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorRemSubRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorRemMulLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorRemMulLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorRemMulRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorRemMulRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorRemDivLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorRemDivLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorRemDivRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorRemDivRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorRemRemLeftInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorRemRemLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

	private final class Int64VectorRemRemRightInputExecutorNode extends Int64VectorDualArithmeticExecutorNode {
		public Int64VectorRemRemRightInputExecutorNode(
				DataContainer<double[]>[] containers, Int64x3ScalarCacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(containers, synchronizer, nextNode);
		}
		public final AccelerationExecutorNode execute() {

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

