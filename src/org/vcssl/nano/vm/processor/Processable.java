/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.processor;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.vm.memory.Memory;


/**
 * The interface defining the fundamental methods of the Processor-compatible class.
 * 
 * In this virtual machine, 
 * {@link Processor} class and {@link org.vcssl.nano.vm.accelerator.Accelerator} class
 * implement this interface.
 */
public interface Processable {

	/**
	 * Processes the list of instructions.
	 * 
	 * The processing-flow begins with the top of the list of the instructions.
	 * Ordinary, the flow goes towards the end of the list,
	 * with processing each instruction in the list in the serial order.
	 * However, sometimes the processing-flow jumps to any point in the list, 
	 * by the effect of JMP instruction and so on.
	 * 
	 * This method ends when the instruction at the end of the list has been processed,
	 * or when the flow has jumped to out of bounds of the list of the instructions.
	 *
	 * @param instructions The list of the instructions to be processed.
	 * @param memory The memory to which data I/O will be performed.
	 * @param interconnect The interconnect having the external function plug-ins which may be called by the instructions.
	 * @throws VnanoException Thrown when any normal run-time error has been occurred (errors of cast, array indexing, and so on).
	 * @throws VnanoFatalException Thrown when any abnormal error (might be a bug of the VM or the compiler) occurred.
	 */
	public void process(
		Instruction[] instructions, Memory memory, Interconnect interconnect
	) throws VnanoException;

	/**
	 * Process the specified instruction, and returns the updated value of the program counter.
	 * 
	 * Note that,
	 * if {@link OperationCode#CALL CALL} or {@link OperationCode#RET RET} instruction is processed by this method,
	 * the detection of recursive calls (unsupported on this interpreter) will not work.
	 *
	 * @param instruction The instruction to be processed.
	 * @param memory The memory to which data I/O will be performed.
	 * @param interconnect The interconnect having the external function plug-ins which may be called by the instructions.
	 * @param programCounter The value of the program counter at when just before processing the specified instruction.
	 * @return The value of the program counter at when just after processing the specified instruction.
	 * @throws VnanoException Thrown when any normal run-time error has been occurred (errors of cast, array indexing, and so on).
	 * @throws VnanoFatalException Thrown when any abnormal error (might be a bug of the VM or the compiler) occurred.
	*/
	public int process(
		Instruction instruction, Memory memory, Interconnect interconnect, int programCounter
	) throws VnanoException;

}
