/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.processor.OperationCode;

public class Float64CachedScalarDualArithmeticUnit extends AccelerationUnit {

	@Override
	public AccelerationExecutorNode generateExecutorNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		Float64ScalarCache[] caches = new Float64ScalarCache[]{
				(Float64ScalarCache)operandCaches[0],
				(Float64ScalarCache)operandCaches[1],
				(Float64ScalarCache)operandCaches[2],
				(Float64ScalarCache)operandCaches[3],
				(Float64ScalarCache)operandCaches[4],
				(Float64ScalarCache)operandCaches[5]
		};

		OperationCode[] fusedOpcodes = instruction.getFusedOperationCodes();

		Float64CachedScalarDualArithmeticExecutorNode executor = null;
		switch (fusedOpcodes[0]) {
			case ADD : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarAddAddLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarAddAddRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case SUB : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarAddSubLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarAddSubRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case MUL : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarAddMulLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarAddMulRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case DIV : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarAddDivLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarAddDivRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case REM : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarAddRemLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarAddRemRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					default : {
						throw new VnanoFatalException(
								"Operation code " + fusedOpcodes[1] + " is invalid for " + this.getClass().getCanonicalName()
						);
					}
				}
				break;
			}


			case SUB : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarSubAddLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarSubAddRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case SUB : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarSubSubLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarSubSubRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case MUL : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarSubMulLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarSubMulRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case DIV : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarSubDivLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarSubDivRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case REM : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarSubRemLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarSubRemRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					default : {
						throw new VnanoFatalException(
								"Operation code " + fusedOpcodes[1] + " is invalid for " + this.getClass().getCanonicalName()
						);
					}
				}
				break;
			}


			case MUL : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarMulAddLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarMulAddRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case SUB : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarMulSubLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarMulSubRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case MUL : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarMulMulLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarMulMulRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case DIV : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarMulDivLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarMulDivRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case REM : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarMulRemLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarMulRemRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					default : {
						throw new VnanoFatalException(
								"Operation code " + fusedOpcodes[1] + " is invalid for " + this.getClass().getCanonicalName()
						);
					}
				}
				break;
			}


			case DIV : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarDivAddLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarDivAddRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case SUB : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarDivSubLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarDivSubRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case MUL : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarDivMulLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarDivMulRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case DIV : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarDivDivLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarDivDivRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case REM : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarDivRemLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarDivRemRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					default : {
						throw new VnanoFatalException(
								"Operation code " + fusedOpcodes[1] + " is invalid for " + this.getClass().getCanonicalName()
						);
					}
				}
				break;
			}


			case REM : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarRemAddLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarRemAddRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case SUB : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarRemSubLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarRemSubRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case MUL : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarRemMulLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarRemMulRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case DIV : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarRemDivLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarRemDivRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case REM : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarRemRemLeftInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarRemRemRightInputExecutorNode(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					default : {
						throw new VnanoFatalException(
								"Operation code " + fusedOpcodes[1] + " is invalid for " + this.getClass().getCanonicalName()
						);
					}
				}
				break;
			}


			default : {
				throw new VnanoFatalException(
						"Operation code " + fusedOpcodes[0] + " is invalid for " + this.getClass().getCanonicalName()
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

		public Float64CachedScalarDualArithmeticExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.cache00 = cache00;
			this.cache01 = cache01;
			this.cache02 = cache02;
			this.cache10 = cache10;
			this.cache11 = cache11;
			this.cache12 = cache12;
		}
	}


	// ADD ADD

	private final class Float64CachedScalarAddAddLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarAddAddLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarAddAddRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarAddAddRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}


	// ADD SUB

	private final class Float64CachedScalarAddSubLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarAddSubLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarAddSubRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarAddSubRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}


	// ADD MUL

	private final class Float64CachedScalarAddMulLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarAddMulLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarAddMulRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarAddMulRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}


	// ADD DIV

	private final class Float64CachedScalarAddDivLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarAddDivLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarAddDivRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarAddDivRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}


	// ADD REM

	private final class Float64CachedScalarAddRemLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarAddRemLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarAddRemRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarAddRemRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}




	// SUB ADD

	private final class Float64CachedScalarSubAddLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarSubAddLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarSubAddRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarSubAddRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}


	// SUB SUB

	private final class Float64CachedScalarSubSubLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarSubSubLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarSubSubRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarSubSubRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}


	// SUB MUL

	private final class Float64CachedScalarSubMulLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarSubMulLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarSubMulRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarSubMulRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}


	// SUB DIV

	private final class Float64CachedScalarSubDivLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarSubDivLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarSubDivRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarSubDivRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}


	// SUB REM

	private final class Float64CachedScalarSubRemLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarSubRemLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarSubRemRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarSubRemRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}





	// MUL ADD

	private final class Float64CachedScalarMulAddLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarMulAddLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarMulAddRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarMulAddRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}


	// MUL SUB

	private final class Float64CachedScalarMulSubLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarMulSubLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarMulSubRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarMulSubRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}


	// MUL MUL

	private final class Float64CachedScalarMulMulLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarMulMulLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarMulMulRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarMulMulRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}


	// MUL DIV

	private final class Float64CachedScalarMulDivLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarMulDivLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarMulDivRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarMulDivRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}


	// MUL REM

	private final class Float64CachedScalarMulRemLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarMulRemLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarMulRemRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarMulRemRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}





	// DIV ADD

	private final class Float64CachedScalarDivAddLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarDivAddLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarDivAddRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarDivAddRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}


	// DIV SUB

	private final class Float64CachedScalarDivSubLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarDivSubLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarDivSubRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarDivSubRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}


	// DIV MUL

	private final class Float64CachedScalarDivMulLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarDivMulLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarDivMulRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarDivMulRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}


	// DIV DIV

	private final class Float64CachedScalarDivDivLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarDivDivLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarDivDivRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarDivDivRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}


	// DIV REM

	private final class Float64CachedScalarDivRemLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarDivRemLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarDivRemRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarDivRemRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}





	// REM ADD

	private final class Float64CachedScalarRemAddLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarRemAddLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarRemAddRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarRemAddRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}


	// REM SUB

	private final class Float64CachedScalarRemSubLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarRemSubLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarRemSubRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarRemSubRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}


	// DIV MUL

	private final class Float64CachedScalarRemMulLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarRemMulLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarRemMulRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarRemMulRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}


	// DIV DIV

	private final class Float64CachedScalarRemDivLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarRemDivLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarRemDivRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarRemDivRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}


	// DIV REM

	private final class Float64CachedScalarRemRemLeftInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarRemRemLeftInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Float64CachedScalarRemRemRightInputExecutorNode extends Float64CachedScalarDualArithmeticExecutorNode {
		public Float64CachedScalarRemRemRightInputExecutorNode(
				Float64ScalarCache cache00, Float64ScalarCache cache01, Float64ScalarCache cache02,
				Float64ScalarCache cache10, Float64ScalarCache cache11, Float64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}

}
