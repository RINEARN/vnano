/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.DataContainer;

public class Int64CachedScalarDualArithmeticUnit extends AcceleratorExecutionUnit {

	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		int cacheLength = operandCachingEnabled.length;
		Int64ScalarCache[] caches = new Int64ScalarCache[cacheLength];
		for (int cacheIndex=0; cacheIndex<cacheLength; cacheIndex++) {
			caches[cacheIndex] = (Int64ScalarCache)operandCaches[cacheIndex];
		};

		OperationCode[] fusedOpcodes = instruction.getFusedOperationCodes();
		int fusedInputOperandIndex = instruction.getFusedInputOperandIndices()[0];

		Int64CachedScalarDualArithmeticNode node = null;
		if (fusedInputOperandIndex == 1) {
			node = this.generateLeftInputNode(fusedOpcodes, caches, nextNode);
		} else if (fusedInputOperandIndex == 2) {
			node = this.generateRightInputNode(fusedOpcodes, caches, nextNode);
		} else {
			throw new VnanoFatalException("Invalid fused input operand index: " + fusedInputOperandIndex);
		}

		return node;
	}


	private Int64CachedScalarDualArithmeticNode generateLeftInputNode(
			OperationCode[] fusedOpcodes, Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {

		Int64CachedScalarDualArithmeticNode node = null;
		switch (fusedOpcodes[0]) {
			case ADD : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						node = new Int64CachedScalarAddAddLeftInputNode(caches, nextNode);
						break;
					}
					case SUB : {
						node = new Int64CachedScalarAddSubLeftInputNode(caches, nextNode);
						break;
					}
					case MUL : {
						node = new Int64CachedScalarAddMulLeftInputNode(caches, nextNode);
						break;
					}
					case DIV : {
						node = new Int64CachedScalarAddDivLeftInputNode(caches, nextNode);
						break;
					}
					case REM : {
						node = new Int64CachedScalarAddRemLeftInputNode(caches, nextNode);
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
						node = new Int64CachedScalarSubAddLeftInputNode(caches, nextNode);
						break;
					}
					case SUB : {
						node = new Int64CachedScalarSubSubLeftInputNode(caches, nextNode);
						break;
					}
					case MUL : {
						node = new Int64CachedScalarSubMulLeftInputNode(caches, nextNode);
						break;
					}
					case DIV : {
						node = new Int64CachedScalarSubDivLeftInputNode(caches, nextNode);
						break;
					}
					case REM : {
						node = new Int64CachedScalarSubRemLeftInputNode(caches, nextNode);
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
						node = new Int64CachedScalarMulAddLeftInputNode(caches, nextNode);
						break;
					}
					case SUB : {
						node = new Int64CachedScalarMulSubLeftInputNode(caches, nextNode);
						break;
					}
					case MUL : {
						node = new Int64CachedScalarMulMulLeftInputNode(caches, nextNode);
						break;
					}
					case DIV : {
						node = new Int64CachedScalarMulDivLeftInputNode(caches, nextNode);
						break;
					}
					case REM : {
						node = new Int64CachedScalarMulRemLeftInputNode(caches, nextNode);
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
						node = new Int64CachedScalarDivAddLeftInputNode(caches, nextNode);
						break;
					}
					case SUB : {
						node = new Int64CachedScalarDivSubLeftInputNode(caches, nextNode);
						break;
					}
					case MUL : {
						node = new Int64CachedScalarDivMulLeftInputNode(caches, nextNode);
						break;
					}
					case DIV : {
						node = new Int64CachedScalarDivDivLeftInputNode(caches, nextNode);
						break;
					}
					case REM : {
						node = new Int64CachedScalarDivRemLeftInputNode(caches, nextNode);
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
						node = new Int64CachedScalarRemAddLeftInputNode(caches, nextNode);
						break;
					}
					case SUB : {
						node = new Int64CachedScalarRemSubLeftInputNode(caches, nextNode);
						break;
					}
					case MUL : {
						node = new Int64CachedScalarRemMulLeftInputNode(caches, nextNode);
						break;
					}
					case DIV : {
						node = new Int64CachedScalarRemDivLeftInputNode(caches, nextNode);
						break;
					}
					case REM : {
						node = new Int64CachedScalarRemRemLeftInputNode(caches, nextNode);
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


	private Int64CachedScalarDualArithmeticNode generateRightInputNode(
			OperationCode[] fusedOpcodes, Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {

		Int64CachedScalarDualArithmeticNode node = null;
		switch (fusedOpcodes[0]) {
			case ADD : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						node = new Int64CachedScalarAddAddRightInputNode(caches, nextNode);
						break;
					}
					case SUB : {
						node = new Int64CachedScalarAddSubRightInputNode(caches, nextNode);
						break;
					}
					case MUL : {
						node = new Int64CachedScalarAddMulRightInputNode(caches, nextNode);
						break;
					}
					case DIV : {
						node = new Int64CachedScalarAddDivRightInputNode(caches, nextNode);
						break;
					}
					case REM : {
						node = new Int64CachedScalarAddRemRightInputNode(caches, nextNode);
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
						node = new Int64CachedScalarSubAddRightInputNode(caches, nextNode);
						break;
					}
					case SUB : {
						node = new Int64CachedScalarSubSubRightInputNode(caches, nextNode);
						break;
					}
					case MUL : {
						node = new Int64CachedScalarSubMulRightInputNode(caches, nextNode);
						break;
					}
					case DIV : {
						node = new Int64CachedScalarSubDivRightInputNode(caches, nextNode);
						break;
					}
					case REM : {
						node = new Int64CachedScalarSubRemRightInputNode(caches, nextNode);
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
						node = new Int64CachedScalarMulAddRightInputNode(caches, nextNode);
						break;
					}
					case SUB : {
						node = new Int64CachedScalarMulSubRightInputNode(caches, nextNode);
						break;
					}
					case MUL : {
						node = new Int64CachedScalarMulMulRightInputNode(caches, nextNode);
						break;
					}
					case DIV : {
						node = new Int64CachedScalarMulDivRightInputNode(caches, nextNode);
						break;
					}
					case REM : {
						node = new Int64CachedScalarMulRemRightInputNode(caches, nextNode);
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
						node = new Int64CachedScalarDivAddRightInputNode(caches, nextNode);
						break;
					}
					case SUB : {
						node = new Int64CachedScalarDivSubRightInputNode(caches, nextNode);
						break;
					}
					case MUL : {
						node = new Int64CachedScalarDivMulRightInputNode(caches, nextNode);
						break;
					}
					case DIV : {
						node = new Int64CachedScalarDivDivRightInputNode(caches, nextNode);
						break;
					}
					case REM : {
						node = new Int64CachedScalarDivRemRightInputNode(caches, nextNode);
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
						node = new Int64CachedScalarRemAddRightInputNode(caches, nextNode);
						break;
					}
					case SUB : {
						node = new Int64CachedScalarRemSubRightInputNode(caches, nextNode);
						break;
					}
					case MUL : {
						node = new Int64CachedScalarRemMulRightInputNode(caches, nextNode);
						break;
					}
					case DIV : {
						node = new Int64CachedScalarRemDivRightInputNode(caches, nextNode);
						break;
					}
					case REM : {
						node = new Int64CachedScalarRemRemRightInputNode(caches, nextNode);
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



	private abstract class Int64CachedScalarDualArithmeticNode extends AcceleratorExecutionNode {
		protected final Int64ScalarCache cache00;
		protected final Int64ScalarCache cache01;
		protected final Int64ScalarCache cache02;
		protected final Int64ScalarCache cache10;
		protected final Int64ScalarCache cache11;
		protected final Int64ScalarCache cache12;

		public Int64CachedScalarDualArithmeticNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {

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

	private final class Int64CachedScalarAddAddLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarAddAddLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarAddAddRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarAddAddRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}

	// ADD SUB

	private final class Int64CachedScalarAddSubLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarAddSubLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarAddSubRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarAddSubRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}

	// ADD MUL

	private final class Int64CachedScalarAddMulLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarAddMulLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarAddMulRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarAddMulRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}

	// ADD DIV

	private final class Int64CachedScalarAddDivLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarAddDivLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarAddDivRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarAddDivRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}

	// ADD REM

	private final class Int64CachedScalarAddRemLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarAddRemLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarAddRemRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarAddRemRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}




	// SUB ADD

	private final class Int64CachedScalarSubAddLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarSubAddLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarSubAddRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarSubAddRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}

	// SUB SUB

	private final class Int64CachedScalarSubSubLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarSubSubLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarSubSubRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarSubSubRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}

	// SUB MUL

	private final class Int64CachedScalarSubMulLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarSubMulLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarSubMulRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarSubMulRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}

	// SUB DIV

	private final class Int64CachedScalarSubDivLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarSubDivLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarSubDivRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarSubDivRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}

	// SUB REM

	private final class Int64CachedScalarSubRemLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarSubRemLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarSubRemRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarSubRemRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}





	// MUL ADD

	private final class Int64CachedScalarMulAddLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarMulAddLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarMulAddRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarMulAddRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}

	// MUL SUB

	private final class Int64CachedScalarMulSubLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarMulSubLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarMulSubRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarMulSubRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}

	// MUL MUL

	private final class Int64CachedScalarMulMulLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarMulMulLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarMulMulRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarMulMulRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}

	// MUL DIV

	private final class Int64CachedScalarMulDivLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarMulDivLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarMulDivRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarMulDivRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}

	// MUL REM

	private final class Int64CachedScalarMulRemLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarMulRemLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarMulRemRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarMulRemRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}





	// DIV ADD

	private final class Int64CachedScalarDivAddLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarDivAddLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarDivAddRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarDivAddRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}

	// DIV SUB

	private final class Int64CachedScalarDivSubLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarDivSubLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarDivSubRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarDivSubRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}

	// DIV MUL

	private final class Int64CachedScalarDivMulLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarDivMulLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarDivMulRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarDivMulRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}

	// DIV DIV

	private final class Int64CachedScalarDivDivLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarDivDivLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarDivDivRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarDivDivRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}

	// DIV REM

	private final class Int64CachedScalarDivRemLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarDivRemLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarDivRemRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarDivRemRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}





	// REM ADD

	private final class Int64CachedScalarRemAddLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarRemAddLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarRemAddRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarRemAddRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}

	// REM SUB

	private final class Int64CachedScalarRemSubLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarRemSubLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarRemSubRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarRemSubRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}

	// DIV MUL

	private final class Int64CachedScalarRemMulLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarRemMulLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarRemMulRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarRemMulRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}

	// DIV DIV

	private final class Int64CachedScalarRemDivLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarRemDivLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarRemDivRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarRemDivRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}

	// DIV REM

	private final class Int64CachedScalarRemRemLeftInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarRemRemLeftInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarRemRemRightInputNode extends Int64CachedScalarDualArithmeticNode {
		public Int64CachedScalarRemRemRightInputNode(Int64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}
}
