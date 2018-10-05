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
	public AccelerationExecutorNode generateExecutor(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		Int64ScalarCache[] caches = new Int64ScalarCache[]{
				(Int64ScalarCache)operandCaches[0],
				(Int64ScalarCache)operandCaches[1],
				(Int64ScalarCache)operandCaches[2],
				(Int64ScalarCache)operandCaches[3],
				(Int64ScalarCache)operandCaches[4],
				(Int64ScalarCache)operandCaches[5]
		};

		OperationCode[] fusedOpcodes = instruction.getFusedOperationCodes();

		Int64CachedScalarDualArithmeticExecutor executor = null;
		switch (fusedOpcodes[0]) {
			case ADD : {
				switch (fusedOpcodes[1]) {
					case ADD : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Int64CachedScalarAddAddLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarAddAddRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case SUB : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Int64CachedScalarAddSubLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarAddSubRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case MUL : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Int64CachedScalarAddMulLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarAddMulRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case DIV : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Int64CachedScalarAddDivLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarAddDivRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case REM : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Int64CachedScalarAddRemLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarAddRemRightInputExecutor(
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
							executor = new Int64CachedScalarSubAddLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarSubAddRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case SUB : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Int64CachedScalarSubSubLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarSubSubRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case MUL : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Int64CachedScalarSubMulLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarSubMulRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case DIV : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Int64CachedScalarSubDivLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarSubDivRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case REM : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Int64CachedScalarSubRemLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarSubRemRightInputExecutor(
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
							executor = new Int64CachedScalarMulAddLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarMulAddRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case SUB : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Int64CachedScalarMulSubLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarMulSubRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case MUL : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Int64CachedScalarMulMulLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarMulMulRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case DIV : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Int64CachedScalarMulDivLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarMulDivRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case REM : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Int64CachedScalarMulRemLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarMulRemRightInputExecutor(
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
							executor = new Int64CachedScalarDivAddLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarDivAddRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case SUB : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Int64CachedScalarDivSubLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarDivSubRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case MUL : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Int64CachedScalarDivMulLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarDivMulRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case DIV : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Int64CachedScalarDivDivLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarDivDivRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case REM : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Int64CachedScalarDivRemLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarDivRemRightInputExecutor(
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
							executor = new Int64CachedScalarRemAddLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarRemAddRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case SUB : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Int64CachedScalarRemSubLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarRemSubRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case MUL : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Int64CachedScalarRemMulLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarRemMulRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case DIV : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Int64CachedScalarRemDivLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarRemDivRightInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						}
						break;
					}
					case REM : {
						if (instruction.getFusedInputOperandIndices()[0] == 1) {
							executor = new Int64CachedScalarRemRemLeftInputExecutor(
								caches[0], caches[1], caches[2], caches[3], caches[4], caches[5], nextNode
							);
						} else {
							executor = new Int64CachedScalarRemRemRightInputExecutor(
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


	private abstract class Int64CachedScalarDualArithmeticExecutor extends AccelerationExecutorNode {
		protected final Int64ScalarCache cache00;
		protected final Int64ScalarCache cache01;
		protected final Int64ScalarCache cache02;
		protected final Int64ScalarCache cache10;
		protected final Int64ScalarCache cache11;
		protected final Int64ScalarCache cache12;

		public Int64CachedScalarDualArithmeticExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
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

	private final class Int64CachedScalarAddAddLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarAddAddLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarAddAddRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarAddAddRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}


	// ADD SUB

	private final class Int64CachedScalarAddSubLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarAddSubLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarAddSubRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarAddSubRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}


	// ADD MUL

	private final class Int64CachedScalarAddMulLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarAddMulLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarAddMulRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarAddMulRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}


	// ADD DIV

	private final class Int64CachedScalarAddDivLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarAddDivLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarAddDivRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarAddDivRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}


	// ADD REM

	private final class Int64CachedScalarAddRemLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarAddRemLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value + this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarAddRemRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarAddRemRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value + this.cache02.value);
			return this.nextNode;
		}
	}




	// SUB ADD

	private final class Int64CachedScalarSubAddLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarSubAddLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarSubAddRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarSubAddRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}


	// SUB SUB

	private final class Int64CachedScalarSubSubLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarSubSubLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarSubSubRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarSubSubRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}


	// SUB MUL

	private final class Int64CachedScalarSubMulLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarSubMulLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarSubMulRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarSubMulRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}


	// SUB DIV

	private final class Int64CachedScalarSubDivLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarSubDivLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarSubDivRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarSubDivRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}


	// SUB REM

	private final class Int64CachedScalarSubRemLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarSubRemLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value - this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarSubRemRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarSubRemRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value - this.cache02.value);
			return this.nextNode;
		}
	}





	// MUL ADD

	private final class Int64CachedScalarMulAddLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarMulAddLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarMulAddRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarMulAddRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}


	// MUL SUB

	private final class Int64CachedScalarMulSubLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarMulSubLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarMulSubRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarMulSubRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}


	// MUL MUL

	private final class Int64CachedScalarMulMulLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarMulMulLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarMulMulRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarMulMulRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}


	// MUL DIV

	private final class Int64CachedScalarMulDivLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarMulDivLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarMulDivRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarMulDivRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}


	// MUL REM

	private final class Int64CachedScalarMulRemLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarMulRemLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value * this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarMulRemRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarMulRemRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value * this.cache02.value);
			return this.nextNode;
		}
	}





	// DIV ADD

	private final class Int64CachedScalarDivAddLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarDivAddLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarDivAddRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarDivAddRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}


	// DIV SUB

	private final class Int64CachedScalarDivSubLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarDivSubLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarDivSubRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarDivSubRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}


	// DIV MUL

	private final class Int64CachedScalarDivMulLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarDivMulLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarDivMulRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarDivMulRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}


	// DIV DIV

	private final class Int64CachedScalarDivDivLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarDivDivLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarDivDivRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarDivDivRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}


	// DIV REM

	private final class Int64CachedScalarDivRemLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarDivRemLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value / this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarDivRemRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarDivRemRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value / this.cache02.value);
			return this.nextNode;
		}
	}





	// REM ADD

	private final class Int64CachedScalarRemAddLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarRemAddLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) + this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarRemAddRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarRemAddRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value + (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}


	// REM SUB

	private final class Int64CachedScalarRemSubLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarRemSubLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) - this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarRemSubRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarRemSubRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value - (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}


	// DIV MUL

	private final class Int64CachedScalarRemMulLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarRemMulLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) * this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarRemMulRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarRemMulRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value * (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}


	// DIV DIV

	private final class Int64CachedScalarRemDivLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarRemDivLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) / this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarRemDivRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarRemDivRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value / (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}


	// DIV REM

	private final class Int64CachedScalarRemRemLeftInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarRemRemLeftInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = (this.cache00.value = this.cache01.value % this.cache02.value) % this.cache12.value;
			return this.nextNode;
		}
	}

	private final class Int64CachedScalarRemRemRightInputExecutor extends Int64CachedScalarDualArithmeticExecutor {
		public Int64CachedScalarRemRemRightInputExecutor(
				Int64ScalarCache cache00, Int64ScalarCache cache01, Int64ScalarCache cache02,
				Int64ScalarCache cache10, Int64ScalarCache cache11, Int64ScalarCache cache12,
				AccelerationExecutorNode nextNode) {
			super(cache00, cache01, cache02, cache10, cache11, cache12, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache10.value = this.cache11.value % (this.cache00.value = this.cache01.value % this.cache02.value);
			return this.nextNode;
		}
	}

}
