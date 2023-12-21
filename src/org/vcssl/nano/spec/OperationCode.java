/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

/**
 * The enum to define operation code of instructions of the VM (Virtual Machine) in the script engine of the Vnano.
 *
 * Names of elements of this enum are the same as the mnemonics of VRIL (Vector Register Intermediate Language),
 * which is the virtual assembly language for the VM.
 *
 * For the detailed description/specification of each operation code, see the following document of VRIL:
 *
 *     https://www.vcssl.org/en-us/vril/isa/instruction    (English)
 *     https://www.vcssl.org/ja-jp/vril/isa/instruction    (Japanese)
 */
public enum OperationCode {

	/** The instruction to perform an addition operation. */
	ADD,

	/** The instruction to perform a subtraction operation. */
	SUB,

	/** The instruction to perform a multiplication operation. */
	MUL,

	/** The instruction to perform a division operation.  */
	DIV,

	/** The instruction to perform the remainder operation. */
	REM,

	/** The instruction to perform a sign inversion operation. */
	NEG,

	/** The instruction to perform the equality comparison operation. */
	EQ,

	/** The instruction to perform the "non-equality" comparison operation. */
	NEQ,

	/** The instruction to perform the "grater-than" comparison operation. */
	GT,

	/** The instruction to perform the "less-than" comparison operation. */
	LT,

	/** The instruction to perform the "grater-equal" comparison operation, */
	GEQ,

	/** The instruction to perform the "less-equal" comparison operation. */
	LEQ,

	/** The instruction to perform the logical-and comparison operation. */
	ANDM,

	/** The instruction to perform the logical-or comparison operation. */
	ORM,

	/** The instruction to perform the logical-not comparison operation */
	NOT,

	/** The instruction to perform the copy-assignment operation. */
	MOV,

	/** The instruction to perform the reference-assignment operation. */
	REF,

	/** The instruction to pop data from the stack, but does not store it in anywhere. */
	POP,

	/** The instruction to pop data from the stack and performs the copy-assignment operation. */
	MOVPOP,

	/** The instruction to pop data from the stack and performs the reference-assignment operation. */
	REFPOP,

	/** The instruction to allocate memory. */
	ALLOC,

	/**
	 * A variation of the {@link OperationCode#ALLOC ALLOC} instruction,
	 * to allocate memory of which size is the same as the other data.
	 */
	ALLOCR,

	/**
	 * A variation of the {@link OperationCode#ALLOC ALLOC} instruction to allocate memory,
	 * of which size is the same as the data which is at the top of the stack.
	 */
	ALLOCP,

	/**
	 * A variation of the {@link OperationCode#ALLOC ALLOC} instruction,
	 * to only declare data type and array ranks/lengths for readability and optimizability of code,
	 * without allocating the actual memory.
	 */
	ALLOCT,

	/** The instruction to release memory. */
	FREE,

	/** The instruction to perform the type-cast operation. */
	CAST,

	/**
	 * (Unused) The instruction to perform assignment operations of elements
	 * of which indices is the same between multi-dimensional arrays having different lengths.
	 */
	REORD,

	/** The instruction to fill all elements of the array with the same value. */
	FILL,

	/** The instruction to copy the value of an element of an array. */
	MOVELM,

	/** The instruction to refer to an element of an array. */
	REFELM,

	/** The instruction to jump when the condition is true. */
	JMP,

	/** The instruction to jump when the condition is false, */
	JMPN,

	/** The instruction to call the internal function */
	CALL,

	/** The instruction to call the external function. */
	CALLX,

	/** The instruction to return from the internal function. */
	RET,

	/** The special instruction which is put at the end of a function. */
	ENDFUN,

	/**
	 * The special instruction which is put at the end of the transfer part
	 * between arguments and parameters in a function.
	 */
	ENDPRM,

	/** The special instruction which is put at the end of code. */
	END,

	/** The operation code to make extended instructions. */
	EX,

	/** The instruction to perform nothing, but it isn't removed by optimizations. */
	NOP,

	/** Same as {@link OperationCode#NOP}, performs nothing, but may be removed by optimizations. */
	LABEL,
}
