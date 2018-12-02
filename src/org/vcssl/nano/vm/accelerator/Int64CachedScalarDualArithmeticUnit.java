/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.processor.OperationCode;

public class Int64CachedScalarDualArithmeticUnit extends AccelerationUnit {

	@Override
	public AccelerationExecutorNode generateExecutorNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		int cacheLength = operandCached.length;
		Int64ScalarCache[] caches = new Int64ScalarCache[cacheLength];
		for (int cacheIndex=0; cacheIndex<cacheLength; cacheIndex++) {
			caches[cacheIndex] = (Int64ScalarCache)operandCaches[cacheIndex];
		};

		OperationCode[] fusedOpcodes = instruction.getFusedOperationCodes();
		int fusedInputOperandIndex = instruction.getFusedInputOperandIndices()[0];

		Int64CachedScalarDualArithmeticExecutorNode executor = null;
		if (fusedInputOperandIndex == 1) {
			executor = this.generateLeftInputExecutorNode(fusedOpcodes, caches, nextNode);
		} else if (fusedInputOperandIndex == 2) {
			executor = this.generateRightInputExecutorNode(fusedOpcodes, caches, nextNode);
		} else {
			throw new VnanoFatalException("Invalid fused input operand index: " + fusedInputOperandIndex);
		}

		return executor;
	}


	private Int64CachedScalarDualArithmeticExecutorNode generateLeftInputExecutorNode(
			OperationCode[] fusedOpcodes, Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {

		Int64CachedScalarDualArithmeticExecutorNode executor = null;
		switch (fusedOpcodes[0]) {
			case ADD : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						executor = new Int64CachedScalarAddAddLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case SUB : {
						executor = new Int64CachedScalarAddSubLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case MUL : {
						executor = new Int64CachedScalarAddMulLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case DIV : {
						executor = new Int64CachedScalarAddDivLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case REM : {
						executor = new Int64CachedScalarAddRemLeftInputExecutorNode(caches, nextNode);
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
						executor = new Int64CachedScalarSubAddLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case SUB : {
						executor = new Int64CachedScalarSubSubLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case MUL : {
						executor = new Int64CachedScalarSubMulLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case DIV : {
						executor = new Int64CachedScalarSubDivLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case REM : {
						executor = new Int64CachedScalarSubRemLeftInputExecutorNode(caches, nextNode);
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
						executor = new Int64CachedScalarMulAddLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case SUB : {
						executor = new Int64CachedScalarMulSubLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case MUL : {
						executor = new Int64CachedScalarMulMulLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case DIV : {
						executor = new Int64CachedScalarMulDivLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case REM : {
						executor = new Int64CachedScalarMulRemLeftInputExecutorNode(caches, nextNode);
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
						executor = new Int64CachedScalarDivAddLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case SUB : {
						executor = new Int64CachedScalarDivSubLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case MUL : {
						executor = new Int64CachedScalarDivMulLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case DIV : {
						executor = new Int64CachedScalarDivDivLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case REM : {
						executor = new Int64CachedScalarDivRemLeftInputExecutorNode(caches, nextNode);
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
						executor = new Int64CachedScalarRemAddLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case SUB : {
						executor = new Int64CachedScalarRemSubLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case MUL : {
						executor = new Int64CachedScalarRemMulLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case DIV : {
						executor = new Int64CachedScalarRemDivLeftInputExecutorNode(caches, nextNode);
						break;
					}
					case REM : {
						executor = new Int64CachedScalarRemRemLeftInputExecutorNode(caches, nextNode);
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


	private Int64CachedScalarDualArithmeticExecutorNode generateRightInputExecutorNode(
			OperationCode[] fusedOpcodes, Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {

		Int64CachedScalarDualArithmeticExecutorNode executor = null;
		switch (fusedOpcodes[0]) {
			case ADD : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						executor = new Int64CachedScalarAddAddRightInputExecutorNode(caches, nextNode);
						break;
					}
					case SUB : {
						executor = new Int64CachedScalarAddSubRightInputExecutorNode(caches, nextNode);
						break;
					}
					case MUL : {
						executor = new Int64CachedScalarAddMulRightInputExecutorNode(caches, nextNode);
						break;
					}
					case DIV : {
						executor = new Int64CachedScalarAddDivRightInputExecutorNode(caches, nextNode);
						break;
					}
					case REM : {
						executor = new Int64CachedScalarAddRemRightInputExecutorNode(caches, nextNode);
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
						executor = new Int64CachedScalarSubAddRightInputExecutorNode(caches, nextNode);
						break;
					}
					case SUB : {
						executor = new Int64CachedScalarSubSubRightInputExecutorNode(caches, nextNode);
						break;
					}
					case MUL : {
						executor = new Int64CachedScalarSubMulRightInputExecutorNode(caches, nextNode);
						break;
					}
					case DIV : {
						executor = new Int64CachedScalarSubDivRightInputExecutorNode(caches, nextNode);
						break;
					}
					case REM : {
						executor = new Int64CachedScalarSubRemRightInputExecutorNode(caches, nextNode);
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
						executor = new Int64CachedScalarMulAddRightInputExecutorNode(caches, nextNode);
						break;
					}
					case SUB : {
						executor = new Int64CachedScalarMulSubRightInputExecutorNode(caches, nextNode);
						break;
					}
					case MUL : {
						executor = new Int64CachedScalarMulMulRightInputExecutorNode(caches, nextNode);
						break;
					}
					case DIV : {
						executor = new Int64CachedScalarMulDivRightInputExecutorNode(caches, nextNode);
						break;
					}
					case REM : {
						executor = new Int64CachedScalarMulRemRightInputExecutorNode(caches, nextNode);
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
						executor = new Int64CachedScalarDivAddRightInputExecutorNode(caches, nextNode);
						break;
					}
					case SUB : {
						executor = new Int64CachedScalarDivSubRightInputExecutorNode(caches, nextNode);
						break;
					}
					case MUL : {
						executor = new Int64CachedScalarDivMulRightInputExecutorNode(caches, nextNode);
						break;
					}
					case DIV : {
						executor = new Int64CachedScalarDivDivRightInputExecutorNode(caches, nextNode);
						break;
					}
					case REM : {
						executor = new Int64CachedScalarDivRemRightInputExecutorNode(caches, nextNode);
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
						executor = new Int64CachedScalarRemAddRightInputExecutorNode(caches, nextNode);
						break;
					}
					case SUB : {
						executor = new Int64CachedScalarRemSubRightInputExecutorNode(caches, nextNode);
						break;
					}
					case MUL : {
						executor = new Int64CachedScalarRemMulRightInputExecutorNode(caches, nextNode);
						break;
					}
					case DIV : {
						executor = new Int64CachedScalarRemDivRightInputExecutorNode(caches, nextNode);
						break;
					}
					case REM : {
						executor = new Int64CachedScalarRemRemRightInputExecutorNode(caches, nextNode);
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



	private abstract class Int64CachedScalarDualArithmeticExecutorNode extends AccelerationExecutorNode {
		protected final Int64ScalarCache cache00;
		protected final Int64ScalarCache cache01;
		protected final Int64ScalarCache cache02;
		protected final Int64ScalarCache cache10;
		protected final Int64ScalarCache cache11;
		protected final Int64ScalarCache cache12;

		public Int64CachedScalarDualArithmeticExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {

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

	private final class Int64CachedScalarAddAddLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarAddAddLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarAddAddRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarAddAddRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}

	// ADD SUB

	private final class Int64CachedScalarAddSubLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarAddSubLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarAddSubRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarAddSubRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}

	// ADD MUL

	private final class Int64CachedScalarAddMulLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarAddMulLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarAddMulRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarAddMulRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}

	// ADD DIV

	private final class Int64CachedScalarAddDivLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarAddDivLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarAddDivRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarAddDivRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}

	// ADD REM

	private final class Int64CachedScalarAddRemLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarAddRemLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarAddRemRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarAddRemRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}




	// SUB ADD

	private final class Int64CachedScalarSubAddLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarSubAddLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarSubAddRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarSubAddRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}

	// SUB SUB

	private final class Int64CachedScalarSubSubLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarSubSubLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarSubSubRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarSubSubRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}

	// SUB MUL

	private final class Int64CachedScalarSubMulLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarSubMulLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarSubMulRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarSubMulRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}

	// SUB DIV

	private final class Int64CachedScalarSubDivLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarSubDivLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarSubDivRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarSubDivRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}

	// SUB REM

	private final class Int64CachedScalarSubRemLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarSubRemLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarSubRemRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarSubRemRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}





	// MUL ADD

	private final class Int64CachedScalarMulAddLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarMulAddLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarMulAddRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarMulAddRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}

	// MUL SUB

	private final class Int64CachedScalarMulSubLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarMulSubLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarMulSubRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarMulSubRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}

	// MUL MUL

	private final class Int64CachedScalarMulMulLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarMulMulLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarMulMulRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarMulMulRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}

	// MUL DIV

	private final class Int64CachedScalarMulDivLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarMulDivLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarMulDivRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarMulDivRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}

	// MUL REM

	private final class Int64CachedScalarMulRemLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarMulRemLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarMulRemRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarMulRemRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}





	// DIV ADD

	private final class Int64CachedScalarDivAddLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarDivAddLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarDivAddRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarDivAddRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}

	// DIV SUB

	private final class Int64CachedScalarDivSubLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarDivSubLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarDivSubRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarDivSubRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}

	// DIV MUL

	private final class Int64CachedScalarDivMulLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarDivMulLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarDivMulRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarDivMulRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}

	// DIV DIV

	private final class Int64CachedScalarDivDivLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarDivDivLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarDivDivRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarDivDivRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}

	// DIV REM

	private final class Int64CachedScalarDivRemLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarDivRemLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarDivRemRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarDivRemRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}





	// REM ADD

	private final class Int64CachedScalarRemAddLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarRemAddLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarRemAddRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarRemAddRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}

	// REM SUB

	private final class Int64CachedScalarRemSubLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarRemSubLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarRemSubRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarRemSubRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}

	// DIV MUL

	private final class Int64CachedScalarRemMulLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarRemMulLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarRemMulRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarRemMulRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}

	// DIV DIV

	private final class Int64CachedScalarRemDivLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarRemDivLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarRemDivRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarRemDivRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}

	// DIV REM

	private final class Int64CachedScalarRemRemLeftInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarRemRemLeftInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarRemRemRightInputExecutorNode extends Int64CachedScalarDualArithmeticExecutorNode {
		public Int64CachedScalarRemRemRightInputExecutorNode(Int64ScalarCache[] caches, AccelerationExecutorNode nextNode) {
			super(caches, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}
}
