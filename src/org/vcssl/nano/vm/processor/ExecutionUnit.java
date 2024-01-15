/*
 * Copyright(C) 2017-2024 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.processor;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.interconnect.DataConverter;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.VnanoException;


/**
 * The class which executes an operation of each instruction, dispatched by {@link DispatchUnit DispatchUnit}.
 */
public class ExecutionUnit {

	// --------------------------------------------------------------------------------
	//   For the detail of each instruction, see:
	//       https://www.vcssl.org/en-us/vril/isa/instruction
	// --------------------------------------------------------------------------------

	/**
	 * Create a new execution unit.
	 */
	public ExecutionUnit() {
	}


	/**
	 * Execute the ADD instruction.
	 *
	 * @param type The data-type of operands.
	 * @param output The data container for storing the operation result.
	 * @param inputA The left operand of the addition operator.
	 * @param inputB The right operand of the addition operator.
	 */
	public void add(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		int outputOffset = output.getArrayOffset();
		int inputAOffset = inputA.getArrayOffset();
		int inputBOffset = inputB.getArrayOffset();
		int dataLength = output.getArraySize();
		this.checkDataType(output, type);
		this.checkDataType(inputA, type);
		this.checkDataType(inputB, type);

		switch(type) {
			case INT64 : {
				long[] inputDataA = (long[])inputA.getArrayData();
				long[] inputDataB = (long[])inputB.getArrayData();
				long[] outputData = (long[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] + inputDataB[inputBOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputDataA = (double[])inputA.getArrayData();
				double[] inputDataB = (double[])inputB.getArrayData();
				double[] outputData = (double[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] + inputDataB[inputBOffset+i];
				}
				return;
			}
			case STRING : {
				String[] inputDataA = (String[])inputA.getArrayData();
				String[] inputDataB = (String[])inputB.getArrayData();
				String[] outputData = (String[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] + inputDataB[inputBOffset+i];
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	/**
	 * Execute the SUB instruction.
	 *
	 * @param type The data-type of operands.
	 * @param output The data container for storing the operation result.
	 * @param inputA The left operand of the subtraction operator.
	 * @param inputB The right operand of the subtraction operator.
	 */
	public void sub(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		int outputOffset = output.getArrayOffset();
		int inputAOffset = inputA.getArrayOffset();
		int inputBOffset = inputB.getArrayOffset();
		int dataLength = output.getArraySize();

		this.checkDataType(output, type);
		this.checkDataType(inputA, type);
		this.checkDataType(inputB, type);

		switch(type) {
			case INT64 : {
				long[] inputDataA = (long[])inputA.getArrayData();
				long[] inputDataB = (long[])inputB.getArrayData();
				long[] outputData = (long[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] - inputDataB[inputBOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputDataA = (double[])inputA.getArrayData();
				double[] inputDataB = (double[])inputB.getArrayData();
				double[] outputData = (double[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] - inputDataB[inputBOffset+i];
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	/**
	 * Execute the MUL instruction.
	 *
	 * @param type The data-type of operands.
	 * @param output The data container for storing the operation result.
	 * @param inputA The left operand of the multiplication operator.
	 * @param inputB The right operand of the multiplication operator.
	 */
	public void mul(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		int outputOffset = output.getArrayOffset();
		int inputAOffset = inputA.getArrayOffset();
		int inputBOffset = inputB.getArrayOffset();
		int dataLength = output.getArraySize();

		this.checkDataType(output, type);
		this.checkDataType(inputA, type);
		this.checkDataType(inputB, type);

		switch(type) {
			case INT64 : {
				long[] inputDataA = (long[])inputA.getArrayData();
				long[] inputDataB = (long[])inputB.getArrayData();
				long[] outputData = (long[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] * inputDataB[inputBOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputDataA = (double[])inputA.getArrayData();
				double[] inputDataB = (double[])inputB.getArrayData();
				double[] outputData = (double[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] * inputDataB[inputBOffset+i];
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	/**
	 * Execute the DIV instruction.
	 *
	 * @param type The data-type of operands.
	 * @param output The data container for storing the operation result.
	 * @param inputA The left operand of the division operator.
	 * @param inputB The right operand of the division operator.
	 */
	public void div(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		int outputOffset = output.getArrayOffset();
		int inputAOffset = inputA.getArrayOffset();
		int inputBOffset = inputB.getArrayOffset();
		int dataLength = output.getArraySize();

		this.checkDataType(output, type);
		this.checkDataType(inputA, type);
		this.checkDataType(inputB, type);

		switch(type) {
			case INT64 : {
				long[] inputDataA = (long[])inputA.getArrayData();
				long[] inputDataB = (long[])inputB.getArrayData();
				long[] outputData = (long[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] / inputDataB[inputBOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputDataA = (double[])inputA.getArrayData();
				double[] inputDataB = (double[])inputB.getArrayData();
				double[] outputData = (double[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] / inputDataB[inputBOffset+i];
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	/**
	 * Execute the REM instruction.
	 *
	 * @param type The data-type of operands.
	 * @param output The data container for storing the operation result.
	 * @param inputA The left operand of the remainder operator.
	 * @param inputB The right operand of the remainder operator.
	 */
	public void rem(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		int outputOffset = output.getArrayOffset();
		int inputAOffset = inputA.getArrayOffset();
		int inputBOffset = inputB.getArrayOffset();
		int dataLength = output.getArraySize();

		this.checkDataType(output, type);
		this.checkDataType(inputA, type);
		this.checkDataType(inputB, type);

		switch(type) {
			case INT64 : {
				long[] inputDataA = (long[])inputA.getArrayData();
				long[] inputDataB = (long[])inputB.getArrayData();
				long[] outputData = (long[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] % inputDataB[inputBOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputDataA = (double[])inputA.getArrayData();
				double[] inputDataB = (double[])inputB.getArrayData();
				double[] outputData = (double[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] % inputDataB[inputBOffset+i];
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	/**
	 * Execute the NEG instruction.
	 *
	 * @param type The data-type of operands.
	 * @param output The data container for storing the operation result.
	 * @param input The operand of the unary minus operator.
	 */
	public void neg(DataType type, DataContainer<?> output, DataContainer<?> input) {

		int outputOffset = output.getArrayOffset();
		int inputOffset = input.getArrayOffset();
		int dataLength = output.getArraySize();

		this.checkDataType(output, type);
		this.checkDataType(input, type);

		switch(type) {
			case INT64 : {
				long[] inputData = (long[])input.getArrayData();
				long[] outputData = (long[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = -inputData[inputOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputData = (double[])input.getArrayData();
				double[] outputData = (double[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = -inputData[inputOffset+i];
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}




	/**
	 * Execute the EQ instruction.
	 *
	 * @param type The data-type of operands.
	 * @param output The data container for storing the operation result.
	 * @param inputA The left operand of the equarity operator.
	 * @param inputB The right operand of the equarity operator.
	 */
	public void eq(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		int outputOffset = output.getArrayOffset();
		int inputAOffset = inputA.getArrayOffset();
		int inputBOffset = inputB.getArrayOffset();
		int dataLength = output.getArraySize();

		this.checkDataType(output, DataType.BOOL);
		this.checkDataType(inputA, type);
		this.checkDataType(inputB, type);

		switch(type) {
			case INT64 : {
				long[] inputDataA = (long[])inputA.getArrayData();
				long[] inputDataB = (long[])inputB.getArrayData();
				boolean[] outputData = (boolean[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] == inputDataB[inputBOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputDataA = (double[])inputA.getArrayData();
				double[] inputDataB = (double[])inputB.getArrayData();
				boolean[] outputData = (boolean[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] == inputDataB[inputBOffset+i];
				}
				return;
			}
			case BOOL : {
				boolean[] inputDataA = (boolean[])inputA.getArrayData();
				boolean[] inputDataB = (boolean[])inputB.getArrayData();
				boolean[] outputData = (boolean[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] == inputDataB[inputBOffset+i];
				}
				return;
			}
			case STRING : {
				String[] inputDataA = (String[])inputA.getArrayData();
				String[] inputDataB = (String[])inputB.getArrayData();
				boolean[] outputData = (boolean[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i].equals(inputDataB[inputBOffset+i]);
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	/**
	 * Execute the NEQ instruction.
	 *
	 * @param type The data-type of operands.
	 * @param output The data container for storing the operation result.
	 * @param inputA The left operand of the non-equarity operator.
	 * @param inputB The right operand of the non-equarity operator.
	 */
	public void neq(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		int outputOffset = output.getArrayOffset();
		int inputAOffset = inputA.getArrayOffset();
		int inputBOffset = inputB.getArrayOffset();
		int dataLength = output.getArraySize();

		this.checkDataType(output, DataType.BOOL);
		this.checkDataType(inputA, type);
		this.checkDataType(inputB, type);

		switch(type) {
			case INT64 : {
				long[] inputDataA = (long[])inputA.getArrayData();
				long[] inputDataB = (long[])inputB.getArrayData();
				boolean[] outputData = (boolean[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] != inputDataB[inputBOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputDataA = (double[])inputA.getArrayData();
				double[] inputDataB = (double[])inputB.getArrayData();
				boolean[] outputData = (boolean[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] != inputDataB[inputBOffset+i];
				}
				return;
			}
			case BOOL : {
				boolean[] inputDataA = (boolean[])inputA.getArrayData();
				boolean[] inputDataB = (boolean[])inputB.getArrayData();
				boolean[] outputData = (boolean[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] != inputDataB[inputBOffset+i];
				}
				return;
			}
			case STRING : {
				String[] inputDataA = (String[])inputA.getArrayData();
				String[] inputDataB = (String[])inputB.getArrayData();
				boolean[] outputData = (boolean[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = !(inputDataA[inputAOffset+i].equals(inputDataB[inputBOffset+i]));
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	/**
	 * Execute the GEQ instruction.
	 *
	 * @param type The data-type of operands.
	 * @param output The data container for storing the operation result.
	 * @param inputA The left operand of the greater-equal operator.
	 * @param inputB The right operand of the greater-equal operator.
	 */
	public void geq(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		int outputOffset = output.getArrayOffset();
		int inputAOffset = inputA.getArrayOffset();
		int inputBOffset = inputB.getArrayOffset();
		int dataLength = output.getArraySize();

		this.checkDataType(output, DataType.BOOL);
		this.checkDataType(inputA, type);
		this.checkDataType(inputB, type);

		switch(type) {
			case INT64 : {
				long[] inputDataA = (long[])inputA.getArrayData();
				long[] inputDataB = (long[])inputB.getArrayData();
				boolean[] outputData = (boolean[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] >= inputDataB[inputBOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputDataA = (double[])inputA.getArrayData();
				double[] inputDataB = (double[])inputB.getArrayData();
				boolean[] outputData = (boolean[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] >= inputDataB[inputBOffset+i];
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	/**
	 * Execute the LEQ instruction.
	 *
	 * @param type The data-type of operands.
	 * @param output The data container for storing the operation result.
	 * @param inputA The left operand of the less-equal operator.
	 * @param inputB The right operand of the less-equal operator.
	 */
	public void leq(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		int outputOffset = output.getArrayOffset();
		int inputAOffset = inputA.getArrayOffset();
		int inputBOffset = inputB.getArrayOffset();
		int dataLength = output.getArraySize();

		this.checkDataType(output, DataType.BOOL);
		this.checkDataType(inputA, type);
		this.checkDataType(inputB, type);

		switch(type) {
			case INT64 : {
				long[] inputDataA = (long[])inputA.getArrayData();
				long[] inputDataB = (long[])inputB.getArrayData();
				boolean[] outputData = (boolean[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] <= inputDataB[inputBOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputDataA = (double[])inputA.getArrayData();
				double[] inputDataB = (double[])inputB.getArrayData();
				boolean[] outputData = (boolean[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] <= inputDataB[inputBOffset+i];
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	/**
	 * Execute the GT instruction.
	 *
	 * @param type The data-type of operands.
	 * @param output The data container for storing the operation result.
	 * @param inputA The left operand of the greater-than operator.
	 * @param inputB The right operand of the greater-than operator.
	 */
	public void gt(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		int outputOffset = output.getArrayOffset();
		int inputAOffset = inputA.getArrayOffset();
		int inputBOffset = inputB.getArrayOffset();
		int dataLength = output.getArraySize();

		this.checkDataType(output, DataType.BOOL);
		this.checkDataType(inputA, type);
		this.checkDataType(inputB, type);

		switch(type) {
			case INT64 : {
				long[] inputDataA = (long[])inputA.getArrayData();
				long[] inputDataB = (long[])inputB.getArrayData();
				boolean[] outputData = (boolean[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] > inputDataB[inputBOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputDataA = (double[])inputA.getArrayData();
				double[] inputDataB = (double[])inputB.getArrayData();
				boolean[] outputData = (boolean[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] > inputDataB[inputBOffset+i];
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	/**
	 * Execute the LT instruction.
	 *
	 * @param type The data-type of operands.
	 * @param output The data container for storing the operation result.
	 * @param inputA The left operand of the less-than operator.
	 * @param inputB The right operand of the less-than operator.
	 */
	public void lt(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		int outputOffset = output.getArrayOffset();
		int inputAOffset = inputA.getArrayOffset();
		int inputBOffset = inputB.getArrayOffset();
		int dataLength = output.getArraySize();

		this.checkDataType(output, DataType.BOOL);
		this.checkDataType(inputA, type);
		this.checkDataType(inputB, type);

		switch(type) {
			case INT64 : {
				long[] inputDataA = (long[])inputA.getArrayData();
				long[] inputDataB = (long[])inputB.getArrayData();
				boolean[] outputData = (boolean[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] < inputDataB[inputBOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputDataA = (double[])inputA.getArrayData();
				double[] inputDataB = (double[])inputB.getArrayData();
				boolean[] outputData = (boolean[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] < inputDataB[inputBOffset+i];
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}




	/**
	 * Execute the ANDM instruction.
	 *
	 * @param type The data-type of operands.
	 * @param output The data container for storing the operation result.
	 * @param inputA The left operand of the logical-and operator.
	 * @param inputB The right operand of the logical-and operator.
	 */
	public void and(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		if (type != DataType.BOOL) {
			throw new VnanoFatalException("Unoperatable data type: " + type);
		}

		int outputOffset = output.getArrayOffset();
		int inputAOffset = inputA.getArrayOffset();
		int inputBOffset = inputB.getArrayOffset();
		int dataLength = output.getArraySize();

		this.checkDataType(output, type);
		this.checkDataType(inputA, type);

		// The flag for short-circuit evaluation.
		boolean allLeftValuesAreFalse = true;

		// Evaluation of the left operand.
		boolean[] inputDataA = (boolean[])inputA.getArrayData();
		boolean[] outputData = (boolean[])output.getArrayData();
		for (int i=0; i<dataLength; i++) {
			outputData[outputOffset+i] = inputDataA[inputAOffset+i];

			// If at least 1 element in inputDataA is true, the value of allLeftValuesAreFalse should be false.
			allLeftValuesAreFalse &= !inputDataA[inputAOffset+i];
		}

		// Short-circuit evaluation: skip evaluation of the right operand.
		if (allLeftValuesAreFalse) {
			return;
		}

		// Evaluation of the right operand.
		boolean[] inputDataB = (boolean[])inputB.getArrayData();
		this.checkDataType(inputB, type);
		for (int i=0; i<dataLength; i++) {
			outputData[outputOffset+i] = outputData[outputOffset+i] && inputDataB[inputBOffset+i];
		}
	}


	/**
	 * Execute the ORM instruction.
	 *
	 * @param type The data-type of operands.
	 * @param output The data container for storing the operation result.
	 * @param inputA The left operand of the logical-or operator.
	 * @param inputB The right operand of the logical-or operator.
	 */
	public void or(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		if (type != DataType.BOOL) {
			throw new VnanoFatalException("Unoperatable data type: " + type);
		}

		int outputOffset = output.getArrayOffset();
		int inputAOffset = inputA.getArrayOffset();
		int inputBOffset = inputB.getArrayOffset();
		int dataLength = output.getArraySize();

		this.checkDataType(output, type);
		this.checkDataType(inputA, type);

		// The flag for short-circuit evaluation.
		boolean allLeftValuesAreTrue = true;

		// Evaluation of the left operand:
		boolean[] inputDataA = (boolean[])inputA.getArrayData();
		boolean[] outputData = (boolean[])output.getArrayData();
		for (int i=0; i<dataLength; i++) {
			outputData[outputOffset+i] = inputDataA[inputAOffset+i];

			// If at least 1 element in inputDataA is false, the value of allLeftValuesAreTrue should be false.
			allLeftValuesAreTrue &= inputDataA[inputAOffset+i];
		}

		// Short-circuit evaluation: skip evaluation of the right operand.
		if (allLeftValuesAreTrue) {
			return;
		}

		// Evaluation of the right operand:
		boolean[] inputDataB = (boolean[])inputB.getArrayData();
		this.checkDataType(inputB, type);
		for (int i=0; i<dataLength; i++) {
			outputData[outputOffset+i] = outputData[outputOffset+i] || inputDataB[inputBOffset+i];
		}
	}


	/**
	 * Execute the NOT instruction.
	 *
	 * @param type The data-type of operands.
	 * @param output The data container for storing the operation result.
	 * @param input The left operand of the logical-not operator.
	 */
	public void not(DataType type, DataContainer<?> output, DataContainer<?> input) {

		int outputOffset = output.getArrayOffset();
		int inputOffset = input.getArrayOffset();
		int dataLength = output.getArraySize();

		this.checkDataType(output, type);
		this.checkDataType(input, type);

		switch(type) {
			case BOOL : {
				boolean[] inputData = (boolean[])input.getArrayData();
				boolean[] outputData = (boolean[])output.getArrayData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = !inputData[inputOffset+i];
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}




	/**
	 * Execute the ALLOC instruction, to allocate memory for a scalar/array data.
	 *
	 * @param type The data-type of the data.
	 * @param target The data container for storing the data.
	 * @param dataLength The number of all elements of the data.
	 * @param arrayLengths The array-lengths of dimensions of the data.
	 */
	@SuppressWarnings("unchecked")
	public void alloc(DataType type, DataContainer<?> target, int dataLength, int[] arrayLengths) {

		Object currentData = target.getArrayData();
		int currentSize = target.getArraySize();
		switch (type) {
			case INT64 : {
				if (!(currentData instanceof long[]) || currentSize != dataLength) {
					((DataContainer<long[]>)target).setArrayData(new long[dataLength], 0, arrayLengths);
				}
				return;
			}
			case FLOAT64 : {
				if (!(currentData instanceof double[]) || currentSize != dataLength) {
					((DataContainer<double[]>)target).setArrayData(new double[dataLength], 0, arrayLengths);
				}
				return;
			}
			case BOOL : {
				if (!(currentData instanceof boolean[]) || currentSize != dataLength) {
					((DataContainer<boolean[]>)target).setArrayData(new boolean[dataLength], 0, arrayLengths);
				}
				return;
			}
			case STRING : {
				if (!(currentData instanceof String[]) || currentSize != dataLength) {
					((DataContainer<String[]>)target).setArrayData(new String[dataLength], 0, arrayLengths);
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}

	/**
	 * Execute the ALLOC instruction, to allocate memory for a scalar data.
	 *
	 * @param type The data-type of the scalar data.
	 * @param target The data container for storing the data.
	 */
	@SuppressWarnings("unchecked")
	public void allocScalar(DataType type, DataContainer<?> target) {
		switch (type) {
			case INT64 : {
				((DataContainer<long[]>)target).setArrayData(
					new long[DataContainer.ARRAY_SIZE_OF_SCALAR], 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR
				);
				return;
			}
			case FLOAT64 : {
				((DataContainer<double[]>)target).setArrayData(
					new double[DataContainer.ARRAY_SIZE_OF_SCALAR], 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR
				);
				return;
			}
			case BOOL : {
				((DataContainer<boolean[]>)target).setArrayData(
					new boolean[DataContainer.ARRAY_SIZE_OF_SCALAR], 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR
				);
				return;
			}
			case STRING : {
				((DataContainer<String[]>)target).setArrayData(
					new String[DataContainer.ARRAY_SIZE_OF_SCALAR], 0, DataContainer.ARRAY_LENGTHS_OF_SCALAR
				);
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	/**
	 * Execute the ALLOC instruction, to allocate memory for an array data.
	 *
	 * @param type The data-type of the array data.
	 * @param target The data container for storing the data.
	 * @param lengthsContainers The data containers storing lengths of dimensions of the array.
	 */
	@SuppressWarnings("unchecked")
	public void allocVector(DataType type, DataContainer<?> target, DataContainer<?> ... lengthsContainers) {
		int size = 1;
		int rank = lengthsContainers.length;

		// !!! CAUTON !!!
		// When we required to change values of "lengths"-array of the data container,
		// we must not modify the currently set "lengths"-array, because
		// sometimes it is being shared between multiple data containers having the same lengths.
		// So we must create a new array here.
		int[] lengths = new int[rank];
		for (int dim=0; dim<rank; dim++) {
			long[] lengthContainerData = ( (DataContainer<long[]>)lengthsContainers[dim] ).getArrayData();
			lengths[dim] = (int)( lengthContainerData[ lengthsContainers[dim].getArrayOffset() ] );
			size *= lengths[dim];
		}

		this.alloc(type, target, size, lengths);
	}


	/**
	 * Execute the ALLOCR instruction, to allocate memory for storing data
	 * having the same array-rank/lengths with the specified data container (sameLengthsContainer).
	 *
	 * @param type The data-type of the data.
	 * @param target The data container for storing the data.
	 * @param sameLengthsContainer The data containers having the same array-rank/lengths with the memory you want to allocate.
	 */
	public void allocSameLengths(DataType type, DataContainer<?> target, DataContainer<?> sameLengthsContainer) {

		int size = sameLengthsContainer.getArraySize();
		int rank = sameLengthsContainer.getArrayRank();

		int[] lengths = sameLengthsContainer.getArrayLengths();
		int[] copiedLengths = new int[rank];

		if (0 < rank) {
			System.arraycopy(lengths, 0, copiedLengths, 0, rank);
		}

		this.alloc(type, target, size, copiedLengths);
	}


	/**
	 * Execute the MOV instruction.
	 *
	 * @param type The data-type of operands.
	 * @param dest The left operand of the assignment operator.
	 * @param src The right operand of the assignment operator.
	 * @throws VnanoException
	 *     Thrown when an unexpected data-type is specified, or it does not match with the actual data-type.
	 */
	public void mov(DataType type, DataContainer<?> dest, DataContainer<?> src) throws VnanoException {
		this.checkDataType(dest, type);
		this.checkDataType(src, type);

		if (dest.getArraySize() != src.getArraySize()) {
			if (dest.getArrayRank() == DataContainer.ARRAY_RANK_OF_SCALAR && src.getArraySize() != 1) {
				throw new VnanoException(ErrorType.ARRAY_SIZE_IS_TOO_LARGE_TO_BE_ASSIGNED_TO_SCALAR_VARIABLE);
			} else {
				throw new VnanoFatalException("Array sizes of operands of the MOV instruction should be the same");
			}
		}

		try {
			System.arraycopy(src.getArrayData(), src.getArrayOffset(), dest.getArrayData(), dest.getArrayOffset(), dest.getArraySize());
		} catch (ArrayStoreException e) {
			throw new VnanoFatalException(e);
		}
	}


	/**
	 * Execute the REF instruction.
	 *
	 * @param type The data-type of operands.
	 * @param dest The destination operand of the reference-assignment operation.
	 * @param src The source-operand of the reference-assignment operation.
	 * @throws VnanoException
	 *     Thrown when an unexpected data-type is specified, or it does not match with the actual data-type.
	 */
	@SuppressWarnings("unchecked")
	public void ref(DataType type, DataContainer<?> dest, DataContainer<?> src) {
		// this.checkDataType(dest, type); // This instruction can be used even when the memory of the dest has not been allocated yet.
		this.checkDataType(src, type);     // The memory of the src must be already allocated.
		( (DataContainer<Object>)dest ).refer( (DataContainer<Object>)src );
	}


	/**
	 * Execute the REORD instruction.
	 *
	 * @param type The data-type of operands.
	 * @param dest The destination operand of the reorder-assignment operation.
	 * @param src The source-operand of the reorder-assignment operation.
	 * @throws VnanoException
	 *     Thrown when an unexpected data-type is specified, or it does not match with the actual data-type.
	 */
	// This method will be used if the VM will support REORD instruction in future.
	@SuppressWarnings("unused")
	public void reord(DataType type, DataContainer<?> dest, DataContainer<?> src)
			throws VnanoException {

		this.checkDataType(dest, type);
		this.checkDataType(src, type);

		int[] srcArrayLength = src.getArrayLengths();
		int[] destArrayLength = dest.getArrayLengths();
		int srcRank = src.getArrayRank();
		int destRank = dest.getArrayRank();
		if (srcRank != destRank) {
			// Error
		}

		int srcDataLength = src.getArraySize();


		// The increase amount of 1D index, when each index of each dimension is incremented.
		int scale[] = new int[ srcRank ];
		scale[0] = 1;
		int currentScale = 1;
		for( int i=srcRank-1; 1<=i; i-- ){
			currentScale *= srcArrayLength[i];
			scale[i] = currentScale;
		}

		// In the following loop, stores indices corresponding with fromData's 1D index.
		int address[] = new int[ srcRank ];

		// Loop for 1D index of all elements of fromData:
		for(int fromDataIndex=0; fromDataIndex<srcDataLength; fromDataIndex++){

			// Compute indicess corresponding fromDataIndex, and store them into "address".
			int mod = fromDataIndex;
			for( int dim=srcRank-1; 0<=dim; dim-- ){
				address[srcRank-1-dim] = mod / scale[dim];
				mod = mod % scale[dim];
			}

			// Check whether indices aren't out of bounds.
			for(int dim=0; dim<srcRank; dim++){
				if( destArrayLength[dim]-1 < address[dim] ){
					// Skipe elements at out of bounds.
					continue;
				}
			}

			// Compute the 1D index in toData.
			int toScale = 1;
			int toDataIndex = 0;
			for(int dim=srcRank-1; 0<=dim; dim-- ){
				toDataIndex += toScale * address[ dim ];
				toScale *= destArrayLength[ dim ];
			}

			// Copy the data.
			System.arraycopy(src.getArrayData(), fromDataIndex, dest.getArrayData(), toDataIndex, 1);
		}
	}


	/**
	 * Execute the FILL instruction.
	 *
	 * @param type The data-type of operands.
	 * @param dest The destination operand of the fill operation.
	 * @param src The source-operand of the fill operation.
	 * @throws VnanoException
	 *     Thrown when an unexpected data-type is specified, or it does not match with the actual data-type.
	 */
	public void fill(DataType type, DataContainer<?> dest, DataContainer<?> src) {

		int destOffset = dest.getArrayOffset();
		int fillerOffset = src.getArrayOffset();
		int destSize = dest.getArraySize();

		this.checkDataType(dest, type);
		this.checkDataType(src, type);

		switch(type) {
			case INT64 : {
				long fillerValue = ( (long[])src.getArrayData() )[fillerOffset];
				long[] outputData = (long[])dest.getArrayData();
				for (int i=0; i<destSize; i++) {
					outputData[destOffset + i] = fillerValue;
				}
				return;
			}
			case FLOAT64 : {
				double fillerValue = ( (double[])src.getArrayData() )[fillerOffset];
				double[] outputData = (double[])dest.getArrayData();
				for (int i=0; i<destSize; i++) {
					outputData[destOffset + i] = fillerValue;
				}
				return;
			}
			case BOOL : {
				boolean fillerValue = ( (boolean[])src.getArrayData() )[fillerOffset];
				boolean[] outputData = (boolean[])dest.getArrayData();
				for (int i=0; i<destSize; i++) {
					outputData[destOffset + i] = fillerValue;
				}
				return;
			}
			case STRING : {
				String fillerValue = ( (String[])src.getArrayData() )[fillerOffset];
				String[] outputData = (String[])dest.getArrayData();
				for (int i=0; i<destSize; i++) {
					outputData[destOffset + i] = fillerValue;
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	/**
	 * Execute the REFELM instruction.
	 *
	 * @param type The data-type of src/dest operands.
	 * @param dest The destination operand of the reference-assignment operation.
	 * @param src The source-operand of the reference-assignment operation.
	 * @param operands All operands of the REFELM instruction, including src, dest, indices.
	 * @param indicesBegin The index in operands, from which index operands begin.
	 * @throws VnanoException
	 *     Thrown when an unexpected data-type is specified, or it does not match with the actual data-type.
	 */
	@SuppressWarnings("unchecked")
	public void refelm(DataType type, DataContainer<?> dest, DataContainer<?> src, DataContainer<?>[] operands, int indicesBegin)
			throws VnanoException {

		this.checkDataType(src, type);

		int rank = operands.length - indicesBegin;
		int[] arrayLength = src.getArrayLengths();
		int dataIndex = this.compute1DIndexFromIndicesOperands(operands, indicesBegin, arrayLength, rank);

		switch (type) {
			case INT64 : {
				((DataContainer<long[]>)dest).setArrayData(
					((DataContainer<long[]>)src).getArrayData(),
					dataIndex + src.getArrayOffset(), DataContainer.ARRAY_LENGTHS_OF_SCALAR
					// In the above, we have add getArrayOffset() with considering that subarray may be going to supported in future.
				);
				break;
			}
			case FLOAT64 : {
				((DataContainer<double[]>)dest).setArrayData(
					((DataContainer<double[]>)src).getArrayData(),
					dataIndex + src.getArrayOffset(), DataContainer.ARRAY_LENGTHS_OF_SCALAR
				);
				break;
			}
			case BOOL : {
				((DataContainer<boolean[]>)dest).setArrayData(
					((DataContainer<boolean[]>)src).getArrayData(),
					dataIndex + src.getArrayOffset(), DataContainer.ARRAY_LENGTHS_OF_SCALAR
				);
				break;
			}
			case STRING : {
				((DataContainer<String[]>)dest).setArrayData(
					((DataContainer<String[]>)src).getArrayData(),
					dataIndex + src.getArrayOffset(), DataContainer.ARRAY_LENGTHS_OF_SCALAR
				);
				break;
			}
			default : {
				throw new VnanoFatalException("Unknown data type: " + type);
			}
		}
	}


	/**
	 * Execute the MOVELM instruction.
	 *
	 * @param type The data-type of src/dest operands.
	 * @param dest The destination operand of the element-assignment operation.
	 * @param src The source-operand of the element-assignment operation.
	 * @param operands All operands of the MOVELM instruction, including src, dest, indices.
	 * @param indicesBegin The index in operands, from which index operands begin.
	 * @throws VnanoException
	 *     Thrown when an unexpected data-type is specified, or it does not match with the actual data-type.
	 */
	public void movelm(DataType type, DataContainer<?> dest, DataContainer<?> src, DataContainer<?>[] operands, int indicesBegin)
			throws VnanoException {

		this.checkDataType(src, type);

		int rank = operands.length - indicesBegin;
		int[] arrayLength = src.getArrayLengths();
		int dataIndex = this.compute1DIndexFromIndicesOperands(operands, indicesBegin, arrayLength, rank);

		switch (type) {
			case INT64 : {
				long[] outputData = (long[])dest.getArrayData();
				long[] inputData = (long[])src.getArrayData();
				outputData[ dest.getArrayOffset() ] = inputData[ dataIndex + src.getArrayOffset() ];
				// In the r.h.s of the above, we have add getArrayOffset() with considering that subarray may be going to supported in future.
				break;
			}
			case FLOAT64 : {
				double[] outputData = (double[])dest.getArrayData();
				double[] inputData = (double[])src.getArrayData();
				outputData[ dest.getArrayOffset() ] = inputData[ dataIndex + src.getArrayOffset() ];
				break;
			}
			case BOOL : {
				boolean[] outputData = (boolean[])dest.getArrayData();
				boolean[] inputData = (boolean[])src.getArrayData();
				outputData[ dest.getArrayOffset() ] = inputData[ dataIndex + src.getArrayOffset() ];
				break;
			}
			case STRING : {
				String[] outputData = (String[])dest.getArrayData();
				String[] inputData = (String[])src.getArrayData();
				outputData[ dest.getArrayOffset() ] = inputData[ dataIndex + src.getArrayOffset() ];
				break;
			}
			default : {
				throw new VnanoFatalException("Unknown data type: " + type);
			}
		}
	}


	/**
	 * Execute the CAST instruction.
	 *
	 * @param destType The data-type of the destination operand.
	 * @param srcType The data-type of the source operand.
	 * @param dest The destination operand of the cast operation.
	 * @param src The source operand of the cast operation.
	 * @throws VnanoException
	 *     Thrown when an unexpected data-type is specified, or it can not be casted to the specified type.
	 */
	public void cast(DataType destType, DataType srcType, DataContainer<?> dest, DataContainer<?> src)
			throws VnanoException {

		int outputOffset = dest.getArrayOffset();
		int targetOffset = src.getArrayOffset();
		int dataLength = dest.getArraySize();

		this.checkDataType(dest, destType);
		this.checkDataType(src, srcType);

		switch(destType) {
			case INT64 : {
				long[] outputData = (long[])dest.getArrayData();
				switch(srcType) {
					case INT64 : {
						long[] targetData = (long[])src.getArrayData();
						for (int i=0; i<dataLength; i++) {
							outputData[outputOffset+i] = targetData[targetOffset + i];
						}
						return;
					}
					case FLOAT64 : {
						double[] targetData = (double[])src.getArrayData();
						for (int i=0; i<dataLength; i++) {
							outputData[outputOffset+i] = (long)targetData[targetOffset + i];
						}
						return;
					}
					case STRING : {
						String[] targetData = (String[])src.getArrayData();
						for (int i=0; i<dataLength; i++) {
							try {
								outputData[outputOffset+i] = Long.parseLong(targetData[targetOffset + i]);

							} catch (NumberFormatException nfe) {

								// The string "123.45" can not be converted to the long-type value directly because it has the floating point.
								// However, we can convert it to the double-type value, and then convert it to the long-type value.
								// For the compatibility with VCSSL, we try to do it here.
								try {
									double d = Double.parseDouble(targetData[targetOffset + i]);
									outputData[outputOffset + i] = (long)d;

								// Not a number.
								} catch (NumberFormatException nfe2){
									VnanoException e = new VnanoException(
										ErrorType.CAST_FAILED_DUE_TO_VALUE,
										new String[] {targetData[targetOffset + i], destType.name() }
									);
									throw e;
								}
							}
						}
						return;
					}
					default : {
						VnanoException e = new VnanoException(
								ErrorType.CAST_FAILED_DUE_TO_TYPE,
								new String[] { srcType.name(), destType.name() }
						);
						throw e;
					}
				}
			}
			case FLOAT64 : {
				double[] outputData = (double[])dest.getArrayData();
				switch(srcType) {
					case INT64 : {
						long[] targetData = (long[])src.getArrayData();
						for (int i=0; i<dataLength; i++) {
							outputData[outputOffset+i] = (double)targetData[targetOffset + i];
						}
						return;
					}
					case FLOAT64 : {
						double[] targetData = (double[])src.getArrayData();
						for (int i=0; i<dataLength; i++) {
							outputData[outputOffset+i] = targetData[targetOffset + i];
						}
						return;
					}
					case STRING : {
						String[] targetData = (String[])src.getArrayData();
						for (int i=0; i<dataLength; i++) {
							try {
								outputData[outputOffset+i] = Double.parseDouble(targetData[targetOffset + i]);
							} catch (NumberFormatException nfe){
								VnanoException e = new VnanoException(
										ErrorType.CAST_FAILED_DUE_TO_VALUE,
										new String[] {targetData[targetOffset + i], destType.name() }
								);
								throw e;
							}
						}
						return;
					}
					default : {
						VnanoException e = new VnanoException(
								ErrorType.CAST_FAILED_DUE_TO_TYPE,
								new String[] { srcType.name(), destType.name() }
						);
						throw e;
					}
				}
			}
			case BOOL : {
				boolean[] outputData = (boolean[])dest.getArrayData();
				switch(srcType) {
					case BOOL : {
						boolean[] targetData = (boolean[])src.getArrayData();
						for (int i=0; i<dataLength; i++) {
							outputData[outputOffset+i] = targetData[targetOffset + i];
						}
						return;
					}
					case STRING : {
						final String trueString = "true";
						final String falseString = "false";
						String[] targetData = (String[])src.getArrayData();
						for (int i=0; i<dataLength; i++) {
							if (targetData[targetOffset + i].equals(trueString)) {
								outputData[outputOffset+i] = true;
							} else if (targetData[targetOffset + i].equals(falseString)) {
								outputData[outputOffset+i] = false;
							} else {
								VnanoException e = new VnanoException(
										ErrorType.CAST_FAILED_DUE_TO_VALUE,
										new String[] {targetData[targetOffset + i], destType.name() }
								);
								throw e;
							}
						}
						return;
					}
					default : {
						VnanoException e = new VnanoException(
								ErrorType.CAST_FAILED_DUE_TO_TYPE,
								new String[] { srcType.name(), destType.name() }
						);
						throw e;
					}
				}
			}
			case STRING : {
				String[] outputData = (String[])dest.getArrayData();
				switch(srcType) {
					case INT64 : {
						long[] targetData = (long[])src.getArrayData();
						for (int i=0; i<dataLength; i++) {
							outputData[outputOffset+i] = Long.toString(targetData[targetOffset + i]);
						}
						return;
					}
					case FLOAT64 : {
						double[] targetData = (double[])src.getArrayData();
						for (int i=0; i<dataLength; i++) {
							outputData[outputOffset+i] = Double.toString(targetData[targetOffset + i]);
						}
						return;
					}
					case BOOL : {
						boolean[] targetData = (boolean[])src.getArrayData();
						for (int i=0; i<dataLength; i++) {
							outputData[outputOffset+i] = Boolean.toString(targetData[targetOffset + i]);
						}
						return;
					}
					case STRING : {
						String[] targetData = (String[])src.getArrayData();
						for (int i=0; i<dataLength; i++) {
							outputData[outputOffset+i] = targetData[targetOffset + i];
						}
						return;
					}
					default : {
						VnanoException e = new VnanoException(
								ErrorType.CAST_FAILED_DUE_TO_TYPE,
								new String[] { srcType.name(), destType.name() }
						);
						throw e;
					}
				}
			}
			default : {
				VnanoException e = new VnanoException(
						ErrorType.CAST_FAILED_DUE_TO_TYPE,
						new String[] { srcType.name(), destType.name() }
				);
				throw e;
			}
		}
	}




	/**
	 * Computes 1D index corresponding with the specified multi-dimensional indices.
	 *
	 * @param operands All operands of an instruction.
	 * @param indicesOperandsBegin The index in operands, from which index operands begin.
	 * @param arrayLength The array-lentghs of the array data.
	 * @param rank The array-rank of the array data.
	 * @throws VnanoException Thrown when indices are out of bounds.
	 */
	private int compute1DIndexFromIndicesOperands(
			DataContainer<?>[] operands, int indicesOperandsBegin, int[] arrayLength, int rank) throws VnanoException {

		// Stores the 1D index.
		int dataIndex = 0;

		// Stores the increase amount of 1D index, when each index of each dimension is incremented.
		int scale = 1;

		for (int i=rank-1; 0 <= i; i--) {

			// Get the (scalar) value of indices[i].
			DataContainer<?> indexOperand = operands[i+indicesOperandsBegin];
			long index = ( (long[])(indexOperand.getArrayData()) )[ indexOperand.getArrayOffset() ];

			if (arrayLength[i] <= index) {
				String[] errorWords = {Long.toString(index), Integer.toString(arrayLength[i]-1)};
				throw new VnanoException(ErrorType.INVALID_ARRAY_INDEX, errorWords);
			}

			// Add the increase amount by i-th dimension to 1D index, and update the scale.
			dataIndex += (int)index * scale;
			scale *= arrayLength[i];
		}
		return dataIndex;
	}


	/**
	 * {@link org.vcssl.nano.vm.memory.DataContainer Data} オブジェクトが保持するデータの型を検査し、
	 * 期待された型と異なれば
	 * {@link org.vcssl.nano.vm.memory.VnanoException InvalidDataTypeException}
	 * 例外をスローします。
	 *
	 * @param data 型検査するデータ
	 * @param type 期待されるデータ型
	 * @throws VnanoFatalException 実際のデータ型が、期待された型と異なる場合に発生します。
	 */
	private void checkDataType(DataContainer<?> data, DataType type) {

		switch(type) {
			case INT64 : {
				if (data.getArrayData() instanceof long[]) {
					return;
				}
				break;
			}
			case FLOAT64 : {
				if (data.getArrayData() instanceof double[]) {
					return;
				}
				break;
			}
			case BOOL : {
				if (data.getArrayData() instanceof boolean[]) {
					return;
				}
				break;
			}
			case STRING : {
				if (data.getArrayData() instanceof String[]) {
					return;
				}
				break;
			}
			case ANY : {
				break;
			}

			// VOID is used for the placeholder operand,
			// so anytime pass the check independently of the actual data in the data container.
			case VOID : {
				return;
			}

			default : {
				throw new VnanoFatalException("Unexpected data type: " + type);
			}
		}

		if (data.getArrayData() == null) {
			throw new VnanoFatalException(
				"Data of the operand is null."
			);
		} else {
			throw new VnanoFatalException(
				"Data of the operand is unexpected type: " + DataConverter.getDataTypeOf(data.getArrayData().getClass()) + " (expected: "+ type + ")"
			);
		}
	}

}


