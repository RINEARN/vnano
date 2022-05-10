/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import java.util.HashSet;
import java.util.Set;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.DataContainer;

public class Float64CachedScalarDualArithmeticUnit extends AcceleratorExecutionUnit {

	public static final Set<OperationCode> AVAILABLE_OPERAND_SET = new HashSet<OperationCode>();
	static {
		AVAILABLE_OPERAND_SET.add(OperationCode.ADD);
		AVAILABLE_OPERAND_SET.add(OperationCode.SUB);
		AVAILABLE_OPERAND_SET.add(OperationCode.MUL);
		AVAILABLE_OPERAND_SET.add(OperationCode.DIV);
		AVAILABLE_OPERAND_SET.add(OperationCode.REM);
	}

	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		int cacheLength = operandCachingEnabled.length;
		Float64ScalarCache[] caches = new Float64ScalarCache[cacheLength];
		for (int cacheIndex=0; cacheIndex<cacheLength; cacheIndex++) {
			caches[cacheIndex] = (Float64ScalarCache)operandCaches[cacheIndex];
		};

		OperationCode[] fusedOpcodes = instruction.getFusedOperationCodes();
		int fusedInputOperandIndex = instruction.getFusedInputOperandIndices()[0];

		Float64CachedScalarDualNode node = null;
		if (fusedInputOperandIndex == 1) {
			node = this.generateLeftInputNode(fusedOpcodes, caches, nextNode);
		} else if (fusedInputOperandIndex == 2) {
			node = this.generateRightInputNode(fusedOpcodes, caches, nextNode);
		} else {
			throw new VnanoFatalException("Invalid fused input operand index: " + fusedInputOperandIndex);
		}

		return node;
	}


	private Float64CachedScalarDualNode generateLeftInputNode(
			OperationCode[] fusedOpcodes, Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {

		Float64CachedScalarDualNode node = null;
		switch (fusedOpcodes[0]) {
			case ADD : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						node = new Float64CachedScalarAddAddLeftInputNode(caches, nextNode);
						break;
					}
					case SUB : {
						node = new Float64CachedScalarAddSubLeftInputNode(caches, nextNode);
						break;
					}
					case MUL : {
						node = new Float64CachedScalarAddMulLeftInputNode(caches, nextNode);
						break;
					}
					case DIV : {
						node = new Float64CachedScalarAddDivLeftInputNode(caches, nextNode);
						break;
					}
					case REM : {
						node = new Float64CachedScalarAddRemLeftInputNode(caches, nextNode);
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
						node = new Float64CachedScalarSubAddLeftInputNode(caches, nextNode);
						break;
					}
					case SUB : {
						node = new Float64CachedScalarSubSubLeftInputNode(caches, nextNode);
						break;
					}
					case MUL : {
						node = new Float64CachedScalarSubMulLeftInputNode(caches, nextNode);
						break;
					}
					case DIV : {
						node = new Float64CachedScalarSubDivLeftInputNode(caches, nextNode);
						break;
					}
					case REM : {
						node = new Float64CachedScalarSubRemLeftInputNode(caches, nextNode);
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
						node = new Float64CachedScalarMulAddLeftInputNode(caches, nextNode);
						break;
					}
					case SUB : {
						node = new Float64CachedScalarMulSubLeftInputNode(caches, nextNode);
						break;
					}
					case MUL : {
						node = new Float64CachedScalarMulMulLeftInputNode(caches, nextNode);
						break;
					}
					case DIV : {
						node = new Float64CachedScalarMulDivLeftInputNode(caches, nextNode);
						break;
					}
					case REM : {
						node = new Float64CachedScalarMulRemLeftInputNode(caches, nextNode);
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
						node = new Float64CachedScalarDivAddLeftInputNode(caches, nextNode);
						break;
					}
					case SUB : {
						node = new Float64CachedScalarDivSubLeftInputNode(caches, nextNode);
						break;
					}
					case MUL : {
						node = new Float64CachedScalarDivMulLeftInputNode(caches, nextNode);
						break;
					}
					case DIV : {
						node = new Float64CachedScalarDivDivLeftInputNode(caches, nextNode);
						break;
					}
					case REM : {
						node = new Float64CachedScalarDivRemLeftInputNode(caches, nextNode);
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
						node = new Float64CachedScalarRemAddLeftInputNode(caches, nextNode);
						break;
					}
					case SUB : {
						node = new Float64CachedScalarRemSubLeftInputNode(caches, nextNode);
						break;
					}
					case MUL : {
						node = new Float64CachedScalarRemMulLeftInputNode(caches, nextNode);
						break;
					}
					case DIV : {
						node = new Float64CachedScalarRemDivLeftInputNode(caches, nextNode);
						break;
					}
					case REM : {
						node = new Float64CachedScalarRemRemLeftInputNode(caches, nextNode);
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


	private Float64CachedScalarDualNode generateRightInputNode(
			OperationCode[] fusedOpcodes, Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {

		Float64CachedScalarDualNode node = null;
		switch (fusedOpcodes[0]) {
			case ADD : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						node = new Float64CachedScalarAddAddRightInputNode(caches, nextNode);
						break;
					}
					case SUB : {
						node = new Float64CachedScalarAddSubRightInputNode(caches, nextNode);
						break;
					}
					case MUL : {
						node = new Float64CachedScalarAddMulRightInputNode(caches, nextNode);
						break;
					}
					case DIV : {
						node = new Float64CachedScalarAddDivRightInputNode(caches, nextNode);
						break;
					}
					case REM : {
						node = new Float64CachedScalarAddRemRightInputNode(caches, nextNode);
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
						node = new Float64CachedScalarSubAddRightInputNode(caches, nextNode);
						break;
					}
					case SUB : {
						node = new Float64CachedScalarSubSubRightInputNode(caches, nextNode);
						break;
					}
					case MUL : {
						node = new Float64CachedScalarSubMulRightInputNode(caches, nextNode);
						break;
					}
					case DIV : {
						node = new Float64CachedScalarSubDivRightInputNode(caches, nextNode);
						break;
					}
					case REM : {
						node = new Float64CachedScalarSubRemRightInputNode(caches, nextNode);
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
						node = new Float64CachedScalarMulAddRightInputNode(caches, nextNode);
						break;
					}
					case SUB : {
						node = new Float64CachedScalarMulSubRightInputNode(caches, nextNode);
						break;
					}
					case MUL : {
						node = new Float64CachedScalarMulMulRightInputNode(caches, nextNode);
						break;
					}
					case DIV : {
						node = new Float64CachedScalarMulDivRightInputNode(caches, nextNode);
						break;
					}
					case REM : {
						node = new Float64CachedScalarMulRemRightInputNode(caches, nextNode);
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
						node = new Float64CachedScalarDivAddRightInputNode(caches, nextNode);
						break;
					}
					case SUB : {
						node = new Float64CachedScalarDivSubRightInputNode(caches, nextNode);
						break;
					}
					case MUL : {
						node = new Float64CachedScalarDivMulRightInputNode(caches, nextNode);
						break;
					}
					case DIV : {
						node = new Float64CachedScalarDivDivRightInputNode(caches, nextNode);
						break;
					}
					case REM : {
						node = new Float64CachedScalarDivRemRightInputNode(caches, nextNode);
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
						node = new Float64CachedScalarRemAddRightInputNode(caches, nextNode);
						break;
					}
					case SUB : {
						node = new Float64CachedScalarRemSubRightInputNode(caches, nextNode);
						break;
					}
					case MUL : {
						node = new Float64CachedScalarRemMulRightInputNode(caches, nextNode);
						break;
					}
					case DIV : {
						node = new Float64CachedScalarRemDivRightInputNode(caches, nextNode);
						break;
					}
					case REM : {
						node = new Float64CachedScalarRemRemRightInputNode(caches, nextNode);
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



	private abstract class Float64CachedScalarDualNode extends AcceleratorExecutionNode {
		protected final Float64ScalarCache cache00;
		protected final Float64ScalarCache cache01;
		protected final Float64ScalarCache cache02;
		protected final Float64ScalarCache cache10;
		protected final Float64ScalarCache cache11;
		protected final Float64ScalarCache cache12;

		public Float64CachedScalarDualNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.cache00 = caches[0];
			this.cache01 = caches[1];
			this.cache02 = caches[2];
			this.cache10 = caches[3];
			this.cache11 = caches[4];
			this.cache12 = caches[5];
		}
	}


	// ADD ADD

	private final class Float64CachedScalarAddAddLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarAddAddLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data + this.cache02.data) + this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarAddAddRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarAddAddRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data + (this.cache00.data = this.cache01.data + this.cache02.data);
			return this.nextNode;
		}
	}

	// ADD SUB

	private final class Float64CachedScalarAddSubLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarAddSubLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data + this.cache02.data) - this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarAddSubRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarAddSubRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data - (this.cache00.data = this.cache01.data + this.cache02.data);
			return this.nextNode;
		}
	}

	// ADD MUL

	private final class Float64CachedScalarAddMulLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarAddMulLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data + this.cache02.data) * this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarAddMulRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarAddMulRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data * (this.cache00.data = this.cache01.data + this.cache02.data);
			return this.nextNode;
		}
	}

	// ADD DIV

	private final class Float64CachedScalarAddDivLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarAddDivLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data + this.cache02.data) / this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarAddDivRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarAddDivRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data / (this.cache00.data = this.cache01.data + this.cache02.data);
			return this.nextNode;
		}
	}

	// ADD REM

	private final class Float64CachedScalarAddRemLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarAddRemLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data + this.cache02.data) % this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarAddRemRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarAddRemRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data % (this.cache00.data = this.cache01.data + this.cache02.data);
			return this.nextNode;
		}
	}




	// SUB ADD

	private final class Float64CachedScalarSubAddLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarSubAddLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data - this.cache02.data) + this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarSubAddRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarSubAddRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data + (this.cache00.data = this.cache01.data - this.cache02.data);
			return this.nextNode;
		}
	}

	// SUB SUB

	private final class Float64CachedScalarSubSubLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarSubSubLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data - this.cache02.data) - this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarSubSubRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarSubSubRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data - (this.cache00.data = this.cache01.data - this.cache02.data);
			return this.nextNode;
		}
	}

	// SUB MUL

	private final class Float64CachedScalarSubMulLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarSubMulLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data - this.cache02.data) * this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarSubMulRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarSubMulRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data * (this.cache00.data = this.cache01.data - this.cache02.data);
			return this.nextNode;
		}
	}

	// SUB DIV

	private final class Float64CachedScalarSubDivLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarSubDivLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data - this.cache02.data) / this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarSubDivRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarSubDivRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data / (this.cache00.data = this.cache01.data - this.cache02.data);
			return this.nextNode;
		}
	}

	// SUB REM

	private final class Float64CachedScalarSubRemLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarSubRemLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data - this.cache02.data) % this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarSubRemRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarSubRemRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data % (this.cache00.data = this.cache01.data - this.cache02.data);
			return this.nextNode;
		}
	}





	// MUL ADD

	private final class Float64CachedScalarMulAddLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarMulAddLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data * this.cache02.data) + this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarMulAddRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarMulAddRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data + (this.cache00.data = this.cache01.data * this.cache02.data);
			return this.nextNode;
		}
	}

	// MUL SUB

	private final class Float64CachedScalarMulSubLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarMulSubLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data * this.cache02.data) - this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarMulSubRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarMulSubRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data - (this.cache00.data = this.cache01.data * this.cache02.data);
			return this.nextNode;
		}
	}

	// MUL MUL

	private final class Float64CachedScalarMulMulLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarMulMulLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data * this.cache02.data) * this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarMulMulRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarMulMulRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data * (this.cache00.data = this.cache01.data * this.cache02.data);
			return this.nextNode;
		}
	}

	// MUL DIV

	private final class Float64CachedScalarMulDivLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarMulDivLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data * this.cache02.data) / this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarMulDivRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarMulDivRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data / (this.cache00.data = this.cache01.data * this.cache02.data);
			return this.nextNode;
		}
	}

	// MUL REM

	private final class Float64CachedScalarMulRemLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarMulRemLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data * this.cache02.data) % this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarMulRemRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarMulRemRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data % (this.cache00.data = this.cache01.data * this.cache02.data);
			return this.nextNode;
		}
	}





	// DIV ADD

	private final class Float64CachedScalarDivAddLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarDivAddLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data / this.cache02.data) + this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarDivAddRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarDivAddRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data + (this.cache00.data = this.cache01.data / this.cache02.data);
			return this.nextNode;
		}
	}

	// DIV SUB

	private final class Float64CachedScalarDivSubLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarDivSubLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data / this.cache02.data) - this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarDivSubRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarDivSubRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data - (this.cache00.data = this.cache01.data / this.cache02.data);
			return this.nextNode;
		}
	}

	// DIV MUL

	private final class Float64CachedScalarDivMulLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarDivMulLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data / this.cache02.data) * this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarDivMulRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarDivMulRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data * (this.cache00.data = this.cache01.data / this.cache02.data);
			return this.nextNode;
		}
	}

	// DIV DIV

	private final class Float64CachedScalarDivDivLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarDivDivLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data / this.cache02.data) / this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarDivDivRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarDivDivRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data / (this.cache00.data = this.cache01.data / this.cache02.data);
			return this.nextNode;
		}
	}

	// DIV REM

	private final class Float64CachedScalarDivRemLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarDivRemLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data / this.cache02.data) % this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarDivRemRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarDivRemRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data % (this.cache00.data = this.cache01.data / this.cache02.data);
			return this.nextNode;
		}
	}





	// REM ADD

	private final class Float64CachedScalarRemAddLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarRemAddLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data % this.cache02.data) + this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarRemAddRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarRemAddRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data + (this.cache00.data = this.cache01.data % this.cache02.data);
			return this.nextNode;
		}
	}

	// REM SUB

	private final class Float64CachedScalarRemSubLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarRemSubLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data % this.cache02.data) - this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarRemSubRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarRemSubRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data - (this.cache00.data = this.cache01.data % this.cache02.data);
			return this.nextNode;
		}
	}

	// DIV MUL

	private final class Float64CachedScalarRemMulLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarRemMulLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data % this.cache02.data) * this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarRemMulRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarRemMulRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data * (this.cache00.data = this.cache01.data % this.cache02.data);
			return this.nextNode;
		}
	}

	// DIV DIV

	private final class Float64CachedScalarRemDivLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarRemDivLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data % this.cache02.data) / this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarRemDivRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarRemDivRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data / (this.cache00.data = this.cache01.data % this.cache02.data);
			return this.nextNode;
		}
	}

	// DIV REM

	private final class Float64CachedScalarRemRemLeftInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarRemRemLeftInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = (this.cache00.data = this.cache01.data % this.cache02.data) % this.cache12.data;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarRemRemRightInputNode extends Float64CachedScalarDualNode {
		public Float64CachedScalarRemRemRightInputNode(Float64ScalarCache[] caches, AcceleratorExecutionNode nextNode) {
			super(caches, nextNode);
		}
		public final AcceleratorExecutionNode execute() {
			this.cache10.data = this.cache11.data % (this.cache00.data = this.cache01.data % this.cache02.data);
			return this.nextNode;
		}
	}
}
