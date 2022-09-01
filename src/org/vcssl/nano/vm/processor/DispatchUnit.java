/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.processor;


import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;


/**
 * The class which decodes each instruction and dispatch it to {@link ExecutionUnit ExecutionUnit}.
 */
public class DispatchUnit {

	/**
	 * Create a new dispatch unit.
	 */
	public DispatchUnit() {
	}


	/**
	 * Dispatches the specified instruction to the execution unit, to execute it.
	 *
	 * @param instruction The instruction to be dispatched to the execution unit (to be executed).
	 * @param memory The memory to/from which data will be written/read.
	 * @param interconnect The interconnect having external variables/functions.
	 * @param executionUnit The execution unit to execute the instruction.s
	 * @param functionRunningFlags The flags representing whether each function is being processed
	 *           (might be modified by the execution of the instruction).
	 * @param programCounter The value of the program cunter just before execution of the specified instruction.
	 * @return The value of the program cunter just after execution of the specified instruction.
	 * @throws VnanoException Thrown when an incorrect/unsupported instruction is specified, or when any run-time error has occurred.
	 */
	public final int dispatch(Instruction instruction, Memory memory, Interconnect interconnect,
			ExecutionUnit executionUnit, boolean[] functionRunningFlags, int programCounter)
					throws VnanoException {

		OperationCode opcode = instruction.getOperationCode();
		DataType[] dataTypes = instruction.getDataTypes();

		// Load data containers of operands from the memory.
		DataContainer<?>[] operands = this.loadOperandData(instruction, memory);
		int operandLength = operands.length;

		// Dispatch to the execution unit, and execute.
		switch (opcode) {

			// --------------------------------------------------------------------------------
			//   For the detail of each instruction, see:
			//       https://www.vcssl.org/en-us/vril/isa/instruction
			// --------------------------------------------------------------------------------

			// Arithmetic instructions:
			case ADD : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.add(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case SUB : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.sub(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case MUL : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.mul(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case DIV : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.div(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case REM : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.rem(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case NEG : {
				this.checkNumberOfOperands(instruction, 2);
				executionUnit.neg(dataTypes[0], operands[0], operands[1]);
				return programCounter + 1;
			}

			// Comparison instructions:
			case EQ : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.eq(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case NEQ : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.neq(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case GEQ : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.geq(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case LEQ : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.leq(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case GT : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.gt(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case LT : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.lt(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}

			// Logical instructions:
			case ANDM : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.and(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case ORM : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.or(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case NOT : {
				this.checkNumberOfOperands(instruction, 2);
				executionUnit.not(dataTypes[0], operands[0], operands[1]);
				return programCounter + 1;
			}

			// Memory management instructions:
			case ALLOC : {

				// Scalar:
				if (operands.length == 1) {
					executionUnit.allocScalar(dataTypes[0], operands[0]);

				// Array:
				} else {
					DataContainer<?>[] lengths = new DataContainer<?>[operands.length-1 ];
					System.arraycopy(operands, 1, lengths, 0, operands.length-1);
					executionUnit.allocVector(dataTypes[0], operands[0], lengths);
				}
				return programCounter + 1;
			}
			// 第2オペランドと同じ配列要素数で、第1オペランドをメモリ確保
			case ALLOCR : {
				// Alloc the memory for the 1st operand, as the data container having same rank/lengths with the 2nd operand.
				executionUnit.allocSameLengths(dataTypes[0], operands[0], operands[1]);
				return programCounter + 1;
			}
			case ALLOCP : {
				// Alloc the memory for the 1st operand, as the data container having same rank/lengths with the data at the top of the stack.
				executionUnit.allocSameLengths(dataTypes[0], operands[0], memory.peek());
				return programCounter + 1;
			}
			// 可読性や最適化のための型宣言命令なので何もしない（Processor は最適化を行わないので無くても動作する）
			case ALLOCT : {
				// This instruction do nothing when it is executed.
				// (This instruction is used for making the optimization of code easy.)
				return programCounter + 1;
			}
			case FREE : {
				this.checkNumberOfOperands(instruction, 1);
				operands[0].initialize();
				return programCounter + 1;
			}

			// Transfer instructions:
			case MOV : {
				this.checkNumberOfOperands(instruction, 2);
				executionUnit.mov(dataTypes[0], operands[0], operands[1]);
				return programCounter + 1;
			}
			case REF : {
				this.checkNumberOfOperands(instruction, 2);
				executionUnit.ref(dataTypes[0], operands[0], operands[1]);
				return programCounter + 1;
			}
			case POP : {
				this.checkNumberOfOperands(instruction, 1);
				memory.pop();
				return programCounter + 1;
			}
			case MOVPOP : {
				this.checkNumberOfOperands(instruction, 1);
				DataContainer<?> src = memory.pop();
				executionUnit.mov(dataTypes[0], operands[0], src);
				return programCounter + 1;
			}
			case REFPOP : {
				this.checkNumberOfOperands(instruction, 1);
				DataContainer<?> src = memory.pop();
				executionUnit.ref(dataTypes[0], operands[0], src);
				return programCounter + 1;
			}
			case CAST : {
				this.checkNumberOfOperands(instruction, 2);
				executionUnit.cast(dataTypes[0], dataTypes[1], operands[0], operands[1]);
				return programCounter + 1;
			}
			case FILL : {
				this.checkNumberOfOperands(instruction, 2);
				executionUnit.fill(dataTypes[0], operands[0], operands[1]);
				return programCounter + 1;
			}
			case MOVELM : {
				executionUnit.movelm(dataTypes[0], operands[0], operands[1], operands, 2);
				return programCounter + 1;
			}
			case REFELM : {
				executionUnit.refelm(dataTypes[0], operands[0], operands[1], operands, 2);
				return programCounter + 1;
			}

			/*
			case REORD : {
				this.checkNumberOfOperands(instruction, 2);
				executionUnit.reord(dataTypes[0], operands[0], operands[1]);
				return programCounter + 1;
			}
			*/

			// Control instructions:
			case JMP : {

				// operands[0]: placeholder
				// operands[1]: the instruction address to jump to there
				// operands[2]: the condition value
				this.checkNumberOfOperands(instruction, 3);
				boolean[] conditions = (boolean[])operands[2].getArrayData();

				// The flag represents whether the flow should jump.
				boolean shouldJump = true;

				// If the condition is an array, jump only when all elements are true.
				// This specification corresponds with the behaviour of short-circuit evaluations of vector logical operators.
				for (boolean condition: conditions) {
					shouldJump &= condition;
				}

				// Jump:
				if (shouldJump) {
					return (int)( (long[])operands[1].getArrayData() )[0];
				
				// Don't jump:
				} else {
					return programCounter + 1;
				}
			}
			case JMPN : {

				// operands[0]: placeholder
				// operands[1]: the instruction address to jump to there
				// operands[2]: the condition value
				this.checkNumberOfOperands(instruction, 3);
				boolean[] conditions = (boolean[])operands[2].getArrayData();

				// The flag represents whether the flow should NOT jump.
				boolean shouldNotJump = false;

				// If the condition is an array, don't jump when one or more elements are true.
				// This specification corresponds with the behaviour of short-circuit evaluations of vector logical operators.
				for (boolean condition: conditions) {
					shouldNotJump |= condition;
				}

				// Don't jump:
				if (shouldNotJump) {
					return programCounter + 1;

				// Jump:
				} else {
					return (int)( (long[])operands[1].getArrayData() )[0];
				}
			}

			case CALL : {

				// Push the instruction address to which the flow will be returned (the next of the current instruction) to the stack.
				int returnAddress = programCounter + 1;
				DataContainer<long[]> returnAddressContainer = new DataContainer<long[]>();
				returnAddressContainer.setArrayData(new long[] { returnAddress }, 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR);
				memory.push(returnAddressContainer);

				// Push arguments to the stack.
				for (int operandIndex=2; operandIndex<operandLength; operandIndex++) {
					memory.push(operands[operandIndex]);
				}

				// Get the instruction address of the top of the function's code, which is specified as the operands[1].
				int functionAddress = (int)( (long[])operands[1].getArrayData() )[0];

				// If the specified function is being processd: Error.
				// (Because this interpreter does not support recursive calls of functions.)
				if(functionRunningFlags[functionAddress]) {
					throw new VnanoException(ErrorType.RECURSIVE_FUNCTION_CALL);
				}
				functionRunningFlags[functionAddress] = true;

				// The next instruction is the top of the function's code.
				return functionAddress;
			}

			case RET : {
				
				// Pops the instruction address to which the processing flow should return, from the stack.
				DataContainer<?> returnAddressContainer = memory.pop();
				int returnAddress = (int)( (long[])returnAddressContainer.getArrayData() )[0];

				// If the instruction has no return value, push an empty data container to the stack.
				if (operands.length <= 2) { // operands[0] is a placeholder, operands[1] is the address.
					memory.push(new DataContainer<Void>());

				// If the instruction has a return value, push it to the stack.
				} else {
					memory.push(operands[2]); // operands[2] is the return value.
				}

				// Turn off the flag representing the function is being processed.
				int functionAddress = (int)( (long[])operands[1].getArrayData() )[0];
				functionRunningFlags[functionAddress] = false;

				// Go to the return address, which is the next of the CALL instruction.
				return returnAddress;
			}

			case CALLX : {
				int externalFunctionIndex = (int)( (long[])operands[1].getArrayData() )[0];
				int argumentLength = operands.length - 2;
				DataContainer<?>[] arguments = new DataContainer[argumentLength];
				System.arraycopy(operands, 2, arguments, 0, argumentLength);
				interconnect.callExternalFunction(externalFunctionIndex, arguments, operands[0]);
				return programCounter + 1;
			}

			case ENDFUN : {
				String functionName = ( (String[])operands[0].getArrayData() )[0];
				throw new VnanoException(ErrorType.FUNCTION_ENDED_WITHOUT_RETURNING_VALUE, functionName);
			}

			case END : {

				// If the evaluation result value is specified as an operand, store it to the memory.
				if (operandLength == 2) {

					// Depends on its partition, data container may be going to released when the execution of script has end, 
					// so copy the content of the data container, not reference of it..
					DataContainer<?> result = new DataContainer<Object>();
					executionUnit.allocSameLengths(dataTypes[0], result, operands[1]);
					executionUnit.mov(dataTypes[0], result, operands[1]);
					memory.setResultDataContainer(operands[1]);
				}

				// When the program counter points to out of the range of instructions, the execution ends, so specify -1 to terminate it.
				return -1;
			}

			case NOP :
			case LABEL :
			case ENDPRM : {
				this.checkNumberOfOperands(instruction, 1);
				return programCounter + 1;
			}

			default : {
				throw new VnanoFatalException("Unsupported operation code: " +  opcode);
			}
		}
	}


	/**
	 * Check whether the number of operands matches with the expected number.
	 *
	 * @param instruction The instruction to be checked.
	 * @param expectedValue The expected number of operands.
	 * @throws VnanoFatalException Thrown when the number of operands does not match with the expected number.
	 */
	private void checkNumberOfOperands(Instruction instruction, int numberOfOperands) {

		int partitionLength = instruction.getOperandPartitions().length;
		int addressLength = instruction.getOperandPartitions().length;

		if (addressLength != numberOfOperands) {
			throw new VnanoFatalException("Invalid number of operands: " + Integer.toString(addressLength));
		}

		if (partitionLength != numberOfOperands) {
			throw new VnanoFatalException("Invalid number of operands: " + Integer.toString(addressLength));
		}
	}


	/**
	 * Load the data containers referred by the operand addresses of the specified instruction, from the memory.
	 *
	 * @param instruction The instruction.
	 * @param memoryController The memory in which data of the operands are stored.
	 * @return Data containers of operands.
	 * @throws VnanoFatalException Thrown when invalid memory access has been detected.
	 */
	private DataContainer<?>[] loadOperandData(Instruction instruction, Memory memory) {

		Memory.Partition[] operandPartitions = instruction.getOperandPartitions();
		int[] operandAddress = instruction.getOperandAddresses();
		int operandLength = operandAddress.length;
		DataContainer<?>[] operandVariable = new DataContainer<?>[operandLength];
		for (int i=0; i<operandLength; i++) {
			operandVariable[i] = memory.getDataContainer(operandPartitions[i], operandAddress[i]);
		}
		return operandVariable;
	}

}
