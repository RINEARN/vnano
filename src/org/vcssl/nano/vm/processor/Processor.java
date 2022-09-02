/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.processor;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.MetaInformationSyntax;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.spec.OptionKey;
import org.vcssl.nano.spec.OptionValue;
import org.vcssl.nano.vm.memory.Memory;


/**
 * The class takes on the function of a processor, in the virtual machine of the Vnano.
 * 
 * The processor provided by this class executes instructions assembled by a VRIL assembly code 
 * (About the specification of VRIL instructions, see: https://www.vcssl.org/en-us/vril/isa/instruction ).
 * 
 * Each instruction is expressed by an instance of the {@link Instruction} class.
 * Data I/O will be performed from/to an instance of the {@link org.vcssl.nano.vm.memory.Memory} class.
 */
public class Processor implements Processable {


	/**
	 * The flag representing whether the process should continue.
	 * 
	 * If this flag is turned to false,
	 * the process of {@link Processor#process(Instruction[], Memory, Interconnect)} method will be terminated immediately, 
	 * just after when the process of the currently executed instruction completed.
	 * 
	 * To turn to true/false this flag, use terminate() / resetTerminator() methods.
	 */
	private volatile boolean continuable;

	/**
	 * The counter of the number of the instructions executed by this instance.
	 * This value is useful for performance monitoring/analysis.
	 * 
	 * Note that, when this counter has reached to the maximum limit of the int type, 
	 * the next counted value jumps to the minimum (negative) limit value of the int type, by so-called "overflow" behaviour.
	 * 
	 * This counter frequently overflows, so the caller-side should monitor this value frequently, 
	 * and should accumulate differentials of monitored values (current value - last monitored value) to more long-precision counter.
	 * 
	 * Why we use "int" for this value, not "long", is to avoid to make the updating process of this counter "synchromized".
	 * If this counter is "long", the writing to it may consists of two operations (32bit x 2), depends on environment.
	 * So if the value is read from an other thread just when its value is being written, the broken value may be read, 
	 * if we don't make the writting/reading action "synchronized".
	 * However, "synchronized" might be a bottleneck of performance, so we avoid the above by using "int" type for this counter.
	 * 
	 * For the same reason, we don't make this value "volatile", 
	 * so some time lag may occurs for this value, by the effect of the "thread cache" mechanism.
	 */
	private int executedInstructionCount;

	/**
	 * Stores the operation code of the currently executed instruction.
	 * This value is useful for performance monitoring/analysis.
	 */
	private OperationCode currentOperationCode;


	/**
	 * Create a new processor.
	 */
	public Processor() {
		this.continuable = true;
		this.executedInstructionCount = 0;
		this.currentOperationCode = null;
	}


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
	@Override
	public void process(Instruction[] instructions, Memory memory, Interconnect interconnect) throws VnanoException {

		// Read values of related options.
		boolean terminatable, monitorable, shouldDump, dumpTargetIsAll, shouldRun;
		String dumpTarget;
		PrintStream dumpStream = null;
		synchronized (this) {
			Map<String, Object> optionMap = interconnect.getOptionMap();
			shouldDump = (Boolean)optionMap.get(OptionKey.DUMPER_ENABLED);
			dumpTarget = (String)optionMap.get(OptionKey.DUMPER_TARGET);
			dumpTargetIsAll = dumpTarget.equals(OptionValue.DUMPER_TARGET_ALL);
			dumpStream = (PrintStream)optionMap.get(OptionKey.DUMPER_STREAM);
			shouldRun = (Boolean)optionMap.get(OptionKey.RUNNING_ENABLED);
			terminatable = (Boolean)optionMap.get(OptionKey.TERMINATOR_ENABLED);
			monitorable = (Boolean)optionMap.get(OptionKey.PERFORMANCE_MONITOR_ENABLED);
		}

		// The unit performing operations (addition, subtraction, ...).
		ExecutionUnit executionUnit = new ExecutionUnit();

		// The unit for dispatching each instruction to the corresponding method in executionUnit.
		DispatchUnit dispatchUnit = new DispatchUnit();

		// The program counter, which is the index of the instruction to be executed at the next.
		int programCounter = 0;

		// The total length of the instructions to be executed.
		int instructionLength = instructions.length;

		// The flags for detecting recursive calls of functions.
		// This VM does not support recursive calls, so we should detect it and should throw an exception.
		// The index of the following array corresponds with the address of the top of the code of each function.
		// The value of each element is set true when the corresponding function is being processed.
		boolean[] functionRunningFlags = new boolean[instructionLength];
		Arrays.fill(functionRunningFlags, false);

		// If the option for running code is disabled, do nothing.
		if (!shouldRun) {
			return;
		}

		// Prints the header of the "Run" section, into the dump-ed content.
		if (shouldDump && dumpTargetIsAll) {
			dumpStream.println("================================================================================");
			dumpStream.println("= Run");
			dumpStream.println("================================================================================");
		}


		// The loop for processing each instruction.
		while (0 <= programCounter && programCounter < instructionLength) {
			if (terminatable && !this.continuable) {  // この continuable は volatile
				break;
			}

			try {

				// Execute an instruction, and update the program counter.
				programCounter = dispatchUnit.dispatch(
					instructions[programCounter], memory, interconnect, executionUnit, functionRunningFlags, programCounter
				);

				// For the performance monitoring:
				if (monitorable) {

					// Stores the operation code of the currently executed instruction.
					this.currentOperationCode = instructions[programCounter].getOperationCode();

					// Increment the counter of the instructions executed by this instance.
					this.executedInstructionCount++;
				}

			} catch (Exception e) {

				// Wrap the Exception by a VnanoException.
				// If the Exception is aleady a VnanoException, keep the type as it is.
				VnanoException vne = null;
				if (e instanceof VnanoException) {
					vne = (VnanoException)e;
				} else {
					vne = new VnanoException(
						ErrorType.UNEXPECTED_PROCESSOR_CRASH, new String[] {Integer.toString(programCounter)}, e
					);
				}

				// Extract the script file name and the line number, from the meta information of the currently executed instruction.
				int lineNumber = MetaInformationSyntax.extractLineNumber(instructions[programCounter], memory);
				String fileName = MetaInformationSyntax.extractFileName(instructions[programCounter], memory);

				// Re-throw the exception with supplementing the script file name and the line number.
				vne.setFileName(fileName);
				vne.setLineNumber(lineNumber);
				throw vne;
			}
		}
		this.currentOperationCode = null;


		// Prints the footer of the "Run" section, into the dump-ed content.
		if (shouldDump && dumpTargetIsAll) {
			dumpStream.println("");
			dumpStream.println("================================================================================");
			dumpStream.println("= End");
			dumpStream.println("================================================================================");
		}
	}


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
	@Override
	public int process(Instruction instruction, Memory memory, Interconnect interconnect, int programCounter)
			throws VnanoException {

		ExecutionUnit executionUnit = new ExecutionUnit();
		DispatchUnit dispatchUnit = new DispatchUnit();
		return dispatchUnit.dispatch(instruction, memory, interconnect, executionUnit, null, programCounter);
	}


	/**
	 * Terminates the process of {@link Processor#process(Instruction[], Memory, Interconnect)} method immediately, 
	 * just after when the process of the currently executed instruction completed.
	 * 
	 * However, if the {@link org.vcssl.nano.spec.OptionKey#TERMINATOR_ENABLED TERMINATOR_ENABLED} option had been enabled
	 * when the process started, the process will not be terminated even if this method is called.
	 * 
	 * Also, after terminated the process by this method, if you want to process new instructions, 
	 * it requires to reset the flag for the termination by calling {@link Processor#resetTerminator()} method.
	 */
	public void terminate() {
		this.continuable = false;  // volatile
	}


	/**
	 * Resets the flag for the termination.
	 * 
	 * When you want to process new instructions after using {@link terminate()} method, 
	 * it requires to call this method before call {@link Processor#process(Instruction[], Memory, Interconnect)} method.
	 * If you don't do it, the process of new instructons will be terminated immediately.
	 */
	public void resetTerminator() {
		this.continuable = true;  // volatile

		// Why we don't reset the above flag automatically in the process(...) method is:
		// If we do so, when the terminate() method is called at the moment just after when process(...) method called, 
		// the modification of the flag by terminate() method may be reset in process(...) method, 
		// so we may fail to terminate the process.
		// We (and probably most of users) don't want to consider that terminate() method may fail, in any situation.
		// Therefore we consign the reset of the flag to application side, by providing this resetTerminator() method.
	}


	/**
	 * Gets the counter value of the instructions executed by this instance.
	 * This value is useful for performance monitoring/analysis.
	 * 
	 * Note that, when the counter has reached to the maximum limit of the int type, 
	 * the next counted value jumps to the minimum (negative) limit value of the int type, by so-called "overflow" behaviour.
	 * This counter frequently overflows, so the caller-side should monitor this value frequently, 
	 * and should accumulate differentials of monitored values (current value - last monitored value) to more long-precision counter.
	 * 
	 * Also, some time lag may occurs for the updating of the counter, by the effect of the "thread cache" mechanism.
	 * So please consider that the returned value is a rough value.
	 *
	 * @return The counter value of the instructions executed by this instance (a rough value, frequently overflows).
	 */
	public int getExecutedInstructionCountIntValue() {
		
		// About the method name:
		//   Don't remove ...Int... because we may be going to support ...Long... version in future.
		
		synchronized (this) {
			return this.executedInstructionCount;
		}
	}


	/**
	 * Gets the operation code of the currently executed instruction.
	 * This value is useful for performance monitoring/analysis.
	 *
	 * The return value is an array, 
	 * but this processor implementation can not execute multiple instructions at once, 
	 * so the array always has one or zero element.
	 * 
	 * (When this processor is processing instructions, the array has one element.
	 *  Otherwise it has no element.)
	 * 
	 * Note that, even when multiple process are running in parallel on this instance, 
	 * the array of the return value don't have multiple elements.
	 * In such case, the operation code of the most recently executed instruction in all threads will be returned.
	 *
	 * @return The operation code of the currently executed instruction.
	 */
	public OperationCode[] getCurrentlyExecutedOperationCodes() {
		synchronized (this) {
			if (this.currentOperationCode == null) {
				return new OperationCode[0];
			}
			return new OperationCode[] { this.currentOperationCode };
		}
	}

}
