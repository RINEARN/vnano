/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.processor.OperationCode;

public class Float64VectorDualArithmeticUnit extends AccelerationUnit {

	@SuppressWarnings("unchecked")
	@Override
	public AccelerationExecutorNode generateExecutorNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		DataContainer<double[]>[] containers = (DataContainer<double[]>[])operandContainers;
		Float64x3ScalarCacheSynchronizer synchronizer
				= new Float64x3ScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);

		OperationCode[] fusedOpcodes = instruction.getFusedOperationCodes();
		int fusedInputOperandIndex = instruction.getFusedInputOperandIndices()[0];

		Float64VectorDualArithmeticExecutorNode executor = null;
		if (fusedInputOperandIndex == 1) {
			executor = this.generateLeftInputExecutorNode(fusedOpcodes, containers, synchronizer, nextNode);
		} else if (fusedInputOperandIndex == 2) {
			executor = this.generateRightInputExecutorNode(fusedOpcodes, containers, synchronizer, nextNode);
		} else {
			throw new VnanoFatalException("Invalid fused input operand index: " + fusedInputOperandIndex);
		}

		return executor;
	}


	private Float64VectorDualArithmeticExecutorNode generateLeftInputExecutorNode(
			OperationCode[] fusedOpcodes, DataContainer<double[]>[] containers,
			Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

		Float64VectorDualArithmeticExecutorNode executor = null;
		switch (fusedOpcodes[0]) {
			case ADD : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						executor = new Float64VectorAddAddLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						executor = new Float64VectorAddSubLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						executor = new Float64VectorAddMulLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						executor = new Float64VectorAddDivLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						executor = new Float64VectorAddRemLeftInputExecutorNode(containers,synchronizer,nextNode);
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
						executor = new Float64VectorSubAddLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						executor = new Float64VectorSubSubLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						executor = new Float64VectorSubMulLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						executor = new Float64VectorSubDivLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						executor = new Float64VectorSubRemLeftInputExecutorNode(containers,synchronizer,nextNode);
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
						executor = new Float64VectorMulAddLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						executor = new Float64VectorMulSubLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						executor = new Float64VectorMulMulLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						executor = new Float64VectorMulDivLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						executor = new Float64VectorMulRemLeftInputExecutorNode(containers,synchronizer,nextNode);
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
						executor = new Float64VectorDivAddLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						executor = new Float64VectorDivSubLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						executor = new Float64VectorDivMulLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						executor = new Float64VectorDivDivLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						executor = new Float64VectorDivRemLeftInputExecutorNode(containers,synchronizer,nextNode);
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
						executor = new Float64VectorRemAddLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						executor = new Float64VectorRemSubLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						executor = new Float64VectorRemMulLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						executor = new Float64VectorRemDivLeftInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						executor = new Float64VectorRemRemLeftInputExecutorNode(containers,synchronizer,nextNode);
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


	private Float64VectorDualArithmeticExecutorNode generateRightInputExecutorNode(
			OperationCode[] fusedOpcodes, DataContainer<double[]>[] containers,
			Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

		Float64VectorDualArithmeticExecutorNode executor = null;
		switch (fusedOpcodes[0]) {
			case ADD : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						executor = new Float64VectorAddAddRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						executor = new Float64VectorAddSubRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						executor = new Float64VectorAddMulRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						executor = new Float64VectorAddDivRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						executor = new Float64VectorAddRemRightInputExecutorNode(containers,synchronizer,nextNode);
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
						executor = new Float64VectorSubAddRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						executor = new Float64VectorSubSubRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						executor = new Float64VectorSubMulRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						executor = new Float64VectorSubDivRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						executor = new Float64VectorSubRemRightInputExecutorNode(containers,synchronizer,nextNode);
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
						executor = new Float64VectorMulAddRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						executor = new Float64VectorMulSubRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						executor = new Float64VectorMulMulRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						executor = new Float64VectorMulDivRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						executor = new Float64VectorMulRemRightInputExecutorNode(containers,synchronizer,nextNode);
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
						executor = new Float64VectorDivAddRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						executor = new Float64VectorDivSubRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						executor = new Float64VectorDivMulRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						executor = new Float64VectorDivDivRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						executor = new Float64VectorDivRemRightInputExecutorNode(containers,synchronizer,nextNode);
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
						executor = new Float64VectorRemAddRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case SUB : {
						executor = new Float64VectorRemSubRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case MUL : {
						executor = new Float64VectorRemMulRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case DIV : {
						executor = new Float64VectorRemDivRightInputExecutorNode(containers,synchronizer,nextNode);
						break;
					}
					case REM : {
						executor = new Float64VectorRemRemRightInputExecutorNode(containers,synchronizer,nextNode);
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


	private abstract class Float64VectorDualArithmeticExecutorNode extends AccelerationExecutorNode {
		//protected final DataContainer<double[]> container00;
		protected final DataContainer<double[]> container01;
		protected final DataContainer<double[]> container02;
		protected final DataContainer<double[]> container10;
		protected final DataContainer<double[]> container11;
		protected final DataContainer<double[]> container12;
		protected final Float64x3ScalarCacheSynchronizer synchronizer;

		public Float64VectorDualArithmeticExecutorNode(DataContainer<double[]>[] containers,
				Float64x3ScalarCacheSynchronizer synchronizer, AccelerationExecutorNode nextNode) {

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

	private final class Float64VectorAddAddLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorAddAddLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorAddAddRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorAddAddRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	// ADD SUB

	private final class Float64VectorAddSubLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorAddSubLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorAddSubRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorAddSubRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	// ADD MUL

	private final class Float64VectorAddMulLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorAddMulLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorAddMulRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorAddMulRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	// ADD DIV

	private final class Float64VectorAddDivLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorAddDivLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorAddDivRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorAddDivRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	// ADD REM

	private final class Float64VectorAddRemLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorAddRemLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorAddRemRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorAddRemRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}





	// SUB ADD

	private final class Float64VectorSubAddLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorSubAddLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorSubAddRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorSubAddRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	// SUB SUB

	private final class Float64VectorSubSubLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorSubSubLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorSubSubRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorSubSubRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	// SUB MUL

	private final class Float64VectorSubMulLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorSubMulLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorSubMulRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorSubMulRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	// SUB DIV

	private final class Float64VectorSubDivLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorSubDivLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorSubDivRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorSubDivRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	// SUB REM

	private final class Float64VectorSubRemLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorSubRemLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorSubRemRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorSubRemRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}





	// MUL ADD

	private final class Float64VectorMulAddLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorMulAddLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorMulAddRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorMulAddRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	// MUL SUB

	private final class Float64VectorMulSubLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorMulSubLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorMulSubRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorMulSubRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	// MUL MUL

	private final class Float64VectorMulMulLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorMulMulLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorMulMulRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorMulMulRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	// MUL DIV

	private final class Float64VectorMulDivLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorMulDivLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorMulDivRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorMulDivRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	// MUL REM

	private final class Float64VectorMulRemLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorMulRemLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorMulRemRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorMulRemRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	// DIV ADD

	private final class Float64VectorDivAddLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorDivAddLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorDivAddRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorDivAddRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	// DIV SUB

	private final class Float64VectorDivSubLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorDivSubLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorDivSubRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorDivSubRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	// DIV MUL

	private final class Float64VectorDivMulLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorDivMulLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorDivMulRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorDivMulRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	// DIV DIV

	private final class Float64VectorDivDivLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorDivDivLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorDivDivRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorDivDivRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	// DIV REM

	private final class Float64VectorDivRemLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorDivRemLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorDivRemRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorDivRemRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}





	// REM ADD

	private final class Float64VectorRemAddLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorRemAddLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorRemAddRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorRemAddRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	// REM SUB

	private final class Float64VectorRemSubLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorRemSubLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorRemSubRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorRemSubRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	// REM MUL

	private final class Float64VectorRemMulLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorRemMulLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorRemMulRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorRemMulRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	// REM DIV

	private final class Float64VectorRemDivLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorRemDivLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorRemDivRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorRemDivRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	// REM REM

	private final class Float64VectorRemRemLeftInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorRemRemLeftInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

	private final class Float64VectorRemRemRightInputExecutorNode extends Float64VectorDualArithmeticExecutorNode {
		public Float64VectorRemRemRightInputExecutorNode(
				DataContainer<double[]>[] containers, Float64x3ScalarCacheSynchronizer synchronizer,
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

			this.synchronizer.writeCache();
			return this.nextNode;
		}
	}

}

