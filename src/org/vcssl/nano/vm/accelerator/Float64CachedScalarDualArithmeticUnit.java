/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.processor.OperationCode;

public class Float64CachedScalarDualArithmeticUnit extends AccelerationUnit {

	@Override
	public AccelerationExecutorNode generateExecutorNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		int cacheLength = operandCached.length;
		Float64ScalarCache[] caches = new Float64ScalarCache[cacheLength];
		for (int cacheIndex=0; cacheIndex<cacheLength; cacheIndex++) {
			caches[cacheIndex] = (Float64ScalarCache)operandCaches[cacheIndex];
		};

		OperationCode[] fusedOpcodes = instruction.getFusedOperationCodes();
		int fusedInputOperandIndex = instruction.getFusedInputOperandIndices()[0];

		Float64CachedScalarDualArithmeticExecutorNode executor = null;
		if (fusedInputOperandIndex == 1) {
			executor = this.generateLeftInputExecutorNode(fusedOpcodes, caches, nextNode);
		} else if (fusedInputOperandIndex == 2) {
			executor = this.generateRightInputExecutorNode(fusedOpcodes, caches, nextNode);
		} else {
			throw new VnanoFatalException("Invalid fused input operand index: " + fusedInputOperandIndex);
		}

		return executor;
	}


	private Float64CachedScalarDualArithmeticExecutorNode generateLeftInputExecutorNode(
			OperationCode[] fusedOpcodes, Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {

		Float64CachedScalarDualArithmeticExecutorNode executor = null;
		switch (fusedOpcodes[0]) {
			case ADD : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						executor = new Float64CachedScalarAddAddLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case SUB : {
						executor = new Float64CachedScalarAddSubLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case MUL : {
						executor = new Float64CachedScalarAddMulLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case DIV : {
						executor = new Float64CachedScalarAddDivLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case REM : {
						executor = new Float64CachedScalarAddRemLeftInputExecutorNode(caches, nextNode);
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
						executor = new Float64CachedScalarSubAddLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case SUB : {
						executor = new Float64CachedScalarSubSubLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case MUL : {
						executor = new Float64CachedScalarSubMulLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case DIV : {
						executor = new Float64CachedScalarSubDivLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case REM : {
						executor = new Float64CachedScalarSubRemLeftInputExecutorNode(caches, nextNode);
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
						executor = new Float64CachedScalarMulAddLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case SUB : {
						executor = new Float64CachedScalarMulSubLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case MUL : {
						executor = new Float64CachedScalarMulMulLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case DIV : {
						executor = new Float64CachedScalarMulDivLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case REM : {
						executor = new Float64CachedScalarMulRemLeftInputExecutorNode(caches, nextNode);
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
						executor = new Float64CachedScalarDivAddLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case SUB : {
						executor = new Float64CachedScalarDivSubLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case MUL : {
						executor = new Float64CachedScalarDivMulLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case DIV : {
						executor = new Float64CachedScalarDivDivLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case REM : {
						executor = new Float64CachedScalarDivRemLeftInputExecutorNode(caches, nextNode);
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
						executor = new Float64CachedScalarRemAddLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case SUB : {
						executor = new Float64CachedScalarRemSubLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case MUL : {
						executor = new Float64CachedScalarRemMulLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case DIV : {
						executor = new Float64CachedScalarRemDivLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case REM : {
						executor = new Float64CachedScalarRemRemLeftInputExecutorNode(caches, nextNode);
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


	private Float64CachedScalarDualArithmeticExecutorNode generateRightInputExecutorNode(
			OperationCode[] fusedOpcodes, Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {

		Float64CachedScalarDualArithmeticExecutorNode executor = null;
		switch (fusedOpcodes[0]) {
			case ADD : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						executor = new Float64CachedScalarAddAddRightInputExecutorNode(caches, nextNode);
						break;
					}
					case SUB : {
						executor = new Float64CachedScalarAddSubRightInputExecutorNode(caches, nextNode);
						break;
					}
					case MUL : {
						executor = new Float64CachedScalarAddMulRightInputExecutorNode(caches, nextNode);
						break;
					}
					case DIV : {
						executor = new Float64CachedScalarAddDivRightInputExecutorNode(caches, nextNode);
						break;
					}
					case REM : {
						executor = new Float64CachedScalarAddRemRightInputExecutorNode(caches, nextNode);
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
						executor = new Float64CachedScalarSubAddRightInputExecutorNode(caches, nextNode);
						break;
					}
					case SUB : {
						executor = new Float64CachedScalarSubSubRightInputExecutorNode(caches, nextNode);
						break;
					}
					case MUL : {
						executor = new Float64CachedScalarSubMulRightInputExecutorNode(caches, nextNode);
						break;
					}
					case DIV : {
						executor = new Float64CachedScalarSubDivRightInputExecutorNode(caches, nextNode);
						break;
					}
					case REM : {
						executor = new Float64CachedScalarSubRemRightInputExecutorNode(caches, nextNode);
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
						executor = new Float64CachedScalarMulAddRightInputExecutorNode(caches, nextNode);
						break;
					}
					case SUB : {
						executor = new Float64CachedScalarMulSubRightInputExecutorNode(caches, nextNode);
						break;
					}
					case MUL : {
						executor = new Float64CachedScalarMulMulRightInputExecutorNode(caches, nextNode);
						break;
					}
					case DIV : {
						executor = new Float64CachedScalarMulDivRightInputExecutorNode(caches, nextNode);
						break;
					}
					case REM : {
						executor = new Float64CachedScalarMulRemRightInputExecutorNode(caches, nextNode);
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
						executor = new Float64CachedScalarDivAddRightInputExecutorNode(caches, nextNode);
						break;
					}
					case SUB : {
						executor = new Float64CachedScalarDivSubRightInputExecutorNode(caches, nextNode);
						break;
					}
					case MUL : {
						executor = new Float64CachedScalarDivMulRightInputExecutorNode(caches, nextNode);
						break;
					}
					case DIV : {
						executor = new Float64CachedScalarDivDivRightInputExecutorNode(caches, nextNode);
						break;
					}
					case REM : {
						executor = new Float64CachedScalarDivRemRightInputExecutorNode(caches, nextNode);
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
						executor = new Float64CachedScalarRemAddRightInputExecutorNode(caches, nextNode);
						break;
					}
					case SUB : {
						executor = new Float64CachedScalarRemSubRightInputExecutorNode(caches, nextNode);
						break;
					}
					case MUL : {
						executor = new Float64CachedScalarRemMulRightInputExecutorNode(caches, nextNode);
						break;
					}
					case DIV : {
						executor = new Float64CachedScalarRemDivRightInputExecutorNode(caches, nextNode);
						break;
					}
					case REM : {
						executor = new Float64CachedScalarRemRemRightInputExecutorNode(caches, nextNode);
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



	private abstract class Float64CachedScalarDualArithmeticExecutorNode extends AccelerationExecutorNode {
		protected final Float64ScalarCache cache00;
		protected final Float64ScalarCache cache01;
		protected final Float64ScalarCache cache02;
		protected final Float64ScalarCache cache10;
		protected final Float64ScalarCache cache11;
		protected final Float64ScalarCache cache12;

		public Float64CachedScalarDualArithmeticExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.cache00 = caches[0];
			this.cache01 = caches[1];
			this.cache02 = caches[2];
			this.cache10 = caches[3];
			this.cache11 = caches[4];
			this.cache12 = caches[5];
		}
	}


	// ADD ADD

	private final class Float64CachedScalarAddAddLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarAddAddLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarAddAddRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarAddAddRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}

	// ADD SUB

	private final class Float64CachedScalarAddSubLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarAddSubLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarAddSubRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarAddSubRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}

	// ADD MUL

	private final class Float64CachedScalarAddMulLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarAddMulLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarAddMulRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarAddMulRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}

	// ADD DIV

	private final class Float64CachedScalarAddDivLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarAddDivLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarAddDivRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarAddDivRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}

	// ADD REM

	private final class Float64CachedScalarAddRemLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarAddRemLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarAddRemRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarAddRemRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}




	// SUB ADD

	private final class Float64CachedScalarSubAddLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarSubAddLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarSubAddRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarSubAddRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}

	// SUB SUB

	private final class Float64CachedScalarSubSubLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarSubSubLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarSubSubRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarSubSubRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}

	// SUB MUL

	private final class Float64CachedScalarSubMulLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarSubMulLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarSubMulRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarSubMulRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}

	// SUB DIV

	private final class Float64CachedScalarSubDivLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarSubDivLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarSubDivRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarSubDivRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}

	// SUB REM

	private final class Float64CachedScalarSubRemLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarSubRemLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarSubRemRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarSubRemRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}





	// MUL ADD

	private final class Float64CachedScalarMulAddLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarMulAddLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarMulAddRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarMulAddRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}

	// MUL SUB

	private final class Float64CachedScalarMulSubLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarMulSubLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarMulSubRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarMulSubRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}

	// MUL MUL

	private final class Float64CachedScalarMulMulLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarMulMulLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarMulMulRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarMulMulRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}

	// MUL DIV

	private final class Float64CachedScalarMulDivLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarMulDivLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarMulDivRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarMulDivRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}

	// MUL REM

	private final class Float64CachedScalarMulRemLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarMulRemLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarMulRemRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarMulRemRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}





	// DIV ADD

	private final class Float64CachedScalarDivAddLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarDivAddLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarDivAddRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarDivAddRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}

	// DIV SUB

	private final class Float64CachedScalarDivSubLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarDivSubLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarDivSubRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarDivSubRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}

	// DIV MUL

	private final class Float64CachedScalarDivMulLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarDivMulLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarDivMulRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarDivMulRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}

	// DIV DIV

	private final class Float64CachedScalarDivDivLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarDivDivLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarDivDivRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarDivDivRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}

	// DIV REM

	private final class Float64CachedScalarDivRemLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarDivRemLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarDivRemRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarDivRemRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}





	// REM ADD

	private final class Float64CachedScalarRemAddLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarRemAddLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarRemAddRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarRemAddRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}

	// REM SUB

	private final class Float64CachedScalarRemSubLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarRemSubLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarRemSubRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarRemSubRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}

	// DIV MUL

	private final class Float64CachedScalarRemMulLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarRemMulLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarRemMulRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarRemMulRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}

	// DIV DIV

	private final class Float64CachedScalarRemDivLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarRemDivLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarRemDivRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarRemDivRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}

	// DIV REM

	private final class Float64CachedScalarRemRemLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarRemRemLeftInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarRemRemRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarRemRemRightInputExecutorNode(Float64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}
}
