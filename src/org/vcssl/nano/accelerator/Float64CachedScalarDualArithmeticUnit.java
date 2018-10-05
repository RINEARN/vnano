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
	public AccelerationExecutorNode generateExecutor(
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

		Float64CachedScalarDualArithmeticExecutor executor = null;
		switch (fusedOpcodes[0]) {
			case ADD : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarAddAddLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarAddAddRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case SUB : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarAddSubLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarAddSubRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case MUL : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarAddMulLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarAddMulRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case DIV : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarAddDivLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarAddDivRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case REM : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarAddRemLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarAddRemRightInputExecutor(
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
							executor = new Float64CachedScalarSubAddLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarSubAddRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case SUB : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarSubSubLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarSubSubRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case MUL : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarSubMulLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarSubMulRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case DIV : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarSubDivLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarSubDivRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case REM : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarSubRemLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarSubRemRightInputExecutor(
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
							executor = new Float64CachedScalarMulAddLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarMulAddRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case SUB : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarMulSubLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarMulSubRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case MUL : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarMulMulLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarMulMulRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case DIV : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarMulDivLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarMulDivRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case REM : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarMulRemLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarMulRemRightInputExecutor(
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
							executor = new Float64CachedScalarDivAddLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarDivAddRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case SUB : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarDivSubLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarDivSubRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case MUL : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarDivMulLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarDivMulRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case DIV : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarDivDivLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarDivDivRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case REM : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarDivRemLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarDivRemRightInputExecutor(
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
							executor = new Float64CachedScalarRemAddLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarRemAddRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case SUB : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarRemSubLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarRemSubRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case MUL : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarRemMulLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarRemMulRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case DIV : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarRemDivLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarRemDivRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case REM : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Float64CachedScalarRemRemLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Float64CachedScalarRemRemRightInputExecutor(
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


	private abstract class Float64CachedScalarDualArithmeticExecutor extends AccelerationExecutorNode {
		protected final Float64ScalarCache cache00;
		protected final Float64ScalarCache cache01;
		protected final Float64ScalarCache cache02;
		protected final Float64ScalarCache cache10;
		protected final Float64ScalarCache cache11;
		protected final Float64ScalarCache cache12;

		public Float64CachedScalarDualArithmeticExecutor(
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

	private final class Float64CachedScalarAddAddLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarAddAddLeftInputExecutor(
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

	private final class Float64CachedScalarAddAddRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarAddAddRightInputExecutor(
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

	private final class Float64CachedScalarAddSubLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarAddSubLeftInputExecutor(
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

	private final class Float64CachedScalarAddSubRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarAddSubRightInputExecutor(
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

	private final class Float64CachedScalarAddMulLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarAddMulLeftInputExecutor(
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

	private final class Float64CachedScalarAddMulRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarAddMulRightInputExecutor(
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

	private final class Float64CachedScalarAddDivLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarAddDivLeftInputExecutor(
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

	private final class Float64CachedScalarAddDivRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarAddDivRightInputExecutor(
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

	private final class Float64CachedScalarAddRemLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarAddRemLeftInputExecutor(
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

	private final class Float64CachedScalarAddRemRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarAddRemRightInputExecutor(
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

	private final class Float64CachedScalarSubAddLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarSubAddLeftInputExecutor(
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

	private final class Float64CachedScalarSubAddRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarSubAddRightInputExecutor(
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

	private final class Float64CachedScalarSubSubLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarSubSubLeftInputExecutor(
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

	private final class Float64CachedScalarSubSubRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarSubSubRightInputExecutor(
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

	private final class Float64CachedScalarSubMulLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarSubMulLeftInputExecutor(
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

	private final class Float64CachedScalarSubMulRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarSubMulRightInputExecutor(
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

	private final class Float64CachedScalarSubDivLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarSubDivLeftInputExecutor(
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

	private final class Float64CachedScalarSubDivRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarSubDivRightInputExecutor(
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

	private final class Float64CachedScalarSubRemLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarSubRemLeftInputExecutor(
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

	private final class Float64CachedScalarSubRemRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarSubRemRightInputExecutor(
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

	private final class Float64CachedScalarMulAddLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarMulAddLeftInputExecutor(
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

	private final class Float64CachedScalarMulAddRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarMulAddRightInputExecutor(
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

	private final class Float64CachedScalarMulSubLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarMulSubLeftInputExecutor(
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

	private final class Float64CachedScalarMulSubRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarMulSubRightInputExecutor(
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

	private final class Float64CachedScalarMulMulLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarMulMulLeftInputExecutor(
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

	private final class Float64CachedScalarMulMulRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarMulMulRightInputExecutor(
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

	private final class Float64CachedScalarMulDivLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarMulDivLeftInputExecutor(
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

	private final class Float64CachedScalarMulDivRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarMulDivRightInputExecutor(
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

	private final class Float64CachedScalarMulRemLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarMulRemLeftInputExecutor(
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

	private final class Float64CachedScalarMulRemRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarMulRemRightInputExecutor(
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

	private final class Float64CachedScalarDivAddLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarDivAddLeftInputExecutor(
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

	private final class Float64CachedScalarDivAddRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarDivAddRightInputExecutor(
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

	private final class Float64CachedScalarDivSubLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarDivSubLeftInputExecutor(
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

	private final class Float64CachedScalarDivSubRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarDivSubRightInputExecutor(
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

	private final class Float64CachedScalarDivMulLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarDivMulLeftInputExecutor(
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

	private final class Float64CachedScalarDivMulRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarDivMulRightInputExecutor(
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

	private final class Float64CachedScalarDivDivLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarDivDivLeftInputExecutor(
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

	private final class Float64CachedScalarDivDivRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarDivDivRightInputExecutor(
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

	private final class Float64CachedScalarDivRemLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarDivRemLeftInputExecutor(
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

	private final class Float64CachedScalarDivRemRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarDivRemRightInputExecutor(
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

	private final class Float64CachedScalarRemAddLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarRemAddLeftInputExecutor(
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

	private final class Float64CachedScalarRemAddRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarRemAddRightInputExecutor(
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

	private final class Float64CachedScalarRemSubLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarRemSubLeftInputExecutor(
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

	private final class Float64CachedScalarRemSubRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarRemSubRightInputExecutor(
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

	private final class Float64CachedScalarRemMulLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarRemMulLeftInputExecutor(
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

	private final class Float64CachedScalarRemMulRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarRemMulRightInputExecutor(
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

	private final class Float64CachedScalarRemDivLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarRemDivLeftInputExecutor(
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

	private final class Float64CachedScalarRemDivRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarRemDivRightInputExecutor(
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

	private final class Float64CachedScalarRemRemLeftInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarRemRemLeftInputExecutor(
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

	private final class Float64CachedScalarRemRemRightInputExecutor extends Float64CachedScalarDualArithmeticExecutor {
		public Float64CachedScalarRemRemRightInputExecutor(
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
