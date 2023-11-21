/*
 * Copyright(C) 2018-2023 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm;

import java.io.PrintStream;
import java.util.Map;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.interconnect.DataConverter;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.OptionKey;
import org.vcssl.nano.spec.OptionValue;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.accelerator.Accelerator;
import org.vcssl.nano.vm.assembler.Assembler;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.processor.Instruction;
import org.vcssl.nano.vm.processor.Processor;

/**
 * The class performing the function of the VM of the script engine of the Vnano.
 *
 * This class executes a kind of intermediate code, named as "VRIL" code,
 * compiled from the script code of the Vnano by the {@link org.vcssl.nano.compiler compiler}.
 * VRIL ― Vector Register Intermediate Language ― is a low-level (but readable text format) language
 * designed as a virtual assembly code for this VM.
 * This class internally assemble the VRIL code to more less-overhead format,
 * and then executes it on a kind of register machines.
 */
public class VirtualMachine {

	/** Stores the processor, which executes instructions. */
	private Processor processor = null;

	/** Stores the accelerator, which is an high-speed implementation of the processor. */
	private Accelerator accelerator = null;

	/** Stores the generated resources for the last execution, to accelerate the re-executions of the same code. */
	private ReexecutionCache reexecutionCache = null;

	/**
	 * The counter for counting the number of executed instructions.
	 * For details, see {@link VirtualMachine#getExecutedInstructionCountIntValue()} method.
	 */
	private int vmProcessedInstructionCount;

	/** The counter for counting the number of instructions executed by the processor. */
	private int processorLastProcCount;

	/** The counter for counting the number of instructions executed by the accelerator. */
	private int acceleratorLastProcCount;


	/**
	 * Create a new VM.
	 */
	public VirtualMachine() {
		this.processor = new Processor();
		this.accelerator = new Accelerator();
		this.vmProcessedInstructionCount = 0;
		this.processorLastProcCount = 0;
		this.acceleratorLastProcCount = 0;
	}


	/**
	 * Executes virtual assembly code written in VRIL (VRIL code).
	 *
	 * @param assemblyCode Virtual assembly code written in VRIL (VRIL code) to be executed.
	 * @param interconnect The interconnect to which external functions/variables are connected.
	 * @return
	 *   The value specified by {@link org.vcssl.nano.spec.OperationCode#END END} instruction at the end of VRIL code.
	 *   If no value is specified, returns null.
	 *
	 * @throws VnanoException Thrown when a runtime error is occurred.
	 */
	public Object executeAssemblyCode(String assemblyCode, Interconnect interconnect)
			throws VnanoException {

		// Extract some option values.
		boolean acceleratorEnabled, shouldDump, dumpTargetIsAll;
		String dumpTarget;
		PrintStream dumpStream = null;
		synchronized (this) {
			Map<String, Object> optionMap = interconnect.getOptionMap();
			acceleratorEnabled = (Boolean)optionMap.get(OptionKey.ACCELERATOR_ENABLED);
			shouldDump = (Boolean)optionMap.get(OptionKey.DUMPER_ENABLED);
			dumpTarget = (String)optionMap.get(OptionKey.DUMPER_TARGET);
			dumpTargetIsAll = dumpTarget.equals(OptionValue.DUMPER_TARGET_ALL);
			dumpStream = (PrintStream)optionMap.get(OptionKey.DUMPER_STREAM);
		}

		// Convert the VRIL assembly code to the VM object code, which can run on the Processor/Accelerator directly.
		Assembler assembler = new Assembler();
		VirtualMachineObjectCode vmObjectCode = assembler.assemble(assemblyCode, interconnect);

		// Dump the VM object code.
		if (shouldDump && (dumpTargetIsAll || dumpTarget.equals(OptionValue.DUMPER_TARGET_OBJECT_CODE)) ) {
			if (dumpTargetIsAll) {
				dumpStream.println("================================================================================");
				dumpStream.println("= VM Object Code");
				dumpStream.println("= - Output of: org.vcssl.nano.vm.assembler.Assembler");
				dumpStream.println("= - Input  of: org.vcssl.nano.vm.processor.Processor");
				dumpStream.println("= -        or: org.vcssl.nano.vm.accelerator.Accelerator");
				dumpStream.println("================================================================================");
			}
			dumpStream.print(vmObjectCode.dump());
			if (dumpTargetIsAll) {
				dumpStream.println("");
			}
		}

		// Allocate memory for the execution, and load data of external variables.
		Memory memory = new Memory();
		memory.allocate(vmObjectCode, interconnect.getExternalVariableTable());

		// Execute the VM object code.
		Instruction[] instructions = vmObjectCode.getInstructions();
		if (acceleratorEnabled) {
			this.accelerator.process(instructions, memory, interconnect, this.processor);
		} else {
			this.processor.process(instructions, memory, interconnect);
		}

		// Write back data of external variables from the memory (may had been modified by the executed VM object code).
		interconnect.writebackExternalVariables(memory, vmObjectCode); // vmObjectCode has the table of variable names and memory addresses

		// Caches some resources to accelerate re-executions of the same code.
		this.reexecutionCache = new ReexecutionCache();
		this.reexecutionCache.setLastObjectCode(vmObjectCode);
		this.reexecutionCache.setMemory(memory);
		this.reexecutionCache.setAcceleratorEnabled(acceleratorEnabled);

		// Convert the data-type of the result value (from the internal data-type to the external one), and return it.
		Object returnValue = null;
		if (memory.hasResultDataContainer()) {
			DataContainer<?> resultDataContainer = memory.getResultDataContainer();
			DataConverter converter = new DataConverter(
				resultDataContainer.getDataType(), resultDataContainer.getArrayRank()
			);
			returnValue = converter.convertToExternalObject(resultDataContainer);
			this.reexecutionCache.setResultDataResources(resultDataContainer, converter);
		}
		return returnValue;
	}


	/**
	 * Re-executes the assembly code, which was executed by executeAssemblyCode() method last time.
	 *
	 * @param interconnect The interconnect to which external functions/variables are connected.
	 * @return
	 *   The value specified by {@link org.vcssl.nano.spec.OperationCode#END END} instruction at the end of VRIL code.
	 *   If no value is specified, returns null.
	 *
	 * @throws VnanoException Thrown when a runtime error is occurred.
	 */
	public Object reexecuteLastAssemblyCode(Interconnect interconnect) throws VnanoException {
		if (this.reexecutionCache == null) {
			throw new VnanoException(ErrorType.INVALID_REEXECUTION_REQUEST);
		}

		// Extract the cached last code, and the cached memory instance for running the code.
		VirtualMachineObjectCode lastObjectCode = this.reexecutionCache.getLastObjectCode();
		Instruction[] instructions = lastObjectCode.getInstructions();
		Memory memory = this.reexecutionCache.getMemory();

		// Reload the (may be updated) values of external variables to GLOBAL partition of the memory.
		memory.updateGlobalPartition(lastObjectCode, interconnect.getExternalVariableTable());

		// Execute the last code.
		if (this.reexecutionCache.isAcceleratorEnabled()) {
			this.accelerator.process(instructions, memory, interconnect, this.processor);
		} else {
			this.processor.process(instructions, memory, interconnect);
		}

		// Write back data of external variables from the memory (may had been modified by the executed VM object code).
		interconnect.writebackExternalVariables(memory, lastObjectCode); // lastObjectCode has the table of variable names and memory addresses

		// Convert the data-type of the result value (from the internal data-type to the external one), and return it.
		Object returnValue = null;
		if (this.reexecutionCache.hasResultDataResources()) {
			DataContainer<?> resultDataContainer = this.reexecutionCache.getResultDataContainer();
			DataConverter converter = this.reexecutionCache.getResultDataConverter();
			returnValue = converter.convertToExternalObject(resultDataContainer);
		}
		return returnValue;
	}


	/**
	 * Terminates the currently running code after when the processing of the current instruction ends,
	 * without processing remained instructions after it in code.
	 * 
	 * If multiple code are being processed, only processes executed under the condition that
	 * {@link org.vcssl.nano.spec.OptionKey#TERMINATOR_ENABLED TERMINATOR_ENABLED}
	 * option is true will be terminated, and other processes will continue.
	 * Also, if you used this method, call {@link VirtualMachine#resetTerminator() resetTerminator()}
	 * method before the next execution of code,
	 * otherwise the next execution will end immediately without processing any instructions.
	 *
	 * By the above behavior, even if a termination request by this method and
	 * an execution request by another thread are conflict, the execution will be terminated certainly
	 * (unless {@link VirtualMachine#resetTerminator() resetTerminator()} will be called before
	 * when the execution will have been terminated).
	 */
	public void terminate() {

		// This method probably be called from a different thread from the thread executing the code.
		// So the followings are enclosed by a synchronized block, to avoid effects of thread caches.
		synchronized (this) {

			if (this.accelerator != null) {
				this.accelerator.terminate();
			}
			if (this.processor != null) {
				this.processor.terminate();
			}
		}
	}


	/**
	 * Resets the VM which had terminated by {@link VirtualMachine#terminate() terminate()} method, for processing new code.
	 * 
	 * Please note that, if an execution of code is requested by another thread
	 * when this method is being processed, the execution request might be missed.
	 */
	public void resetTerminator() {
		synchronized (this) {
			if (this.accelerator != null) {
				this.accelerator.resetTerminator();
			}
			if (this.processor != null) {
				this.processor.resetTerminator();
			}
		}
	}


	/**
	 * Returns the total number of processed instructions from when this VM was instantiated.
	 * 
	 * Note that, to lighten the decreasing of the performance caused by the counting/monitoring,
	 * the cached old value of the counter by the caller thread may be returned,
	 * so the precision of the returned value is not perfect.
	 * Especially when multiple processes are running on this VM,
	 * the counting will not be performed as exclusive process, so miscounting will occur to some extent.
	 * Also, please note that, when the counter value exceeds the positive maximum value of the int-type,
	 * it will not be reset to 0, and it will be the negative maximum value (minimum value on the number line)
	 * of the int-type, and will continue to be incremented from that value.
	 * For the above reason, it is recommended to get the value frequently enough
	 * (for example, --perf option of the command-line mode of the Vnano gets this value about 100 times per second),
	 * and use differences between them, not a raw value returned by this method.
	 *
	 * @return The total number of processed instructions from when this VM was instantiated.
	 */
	// Don't remove "Int" from the following method name, because we perhaps support "...Long..." version in future.
	public int getExecutedInstructionCountIntValue() { 

		// This method probably be called from a different thread from the thread executing the code.
		// So the followings are enclosed by a synchronized block, to avoid effects of thread caches.
		synchronized (this) {
			if (this.processor != null) {
				int processorCurrentProcCount = this.processor.getExecutedInstructionCountIntValue();
				this.vmProcessedInstructionCount += processorCurrentProcCount - this.processorLastProcCount;
				this.processorLastProcCount = processorCurrentProcCount;
			}
			if (this.accelerator != null) {
				int acceleratorCurrentProcCount = this.accelerator.getExecutedInstructionCountIntValue();
				this.vmProcessedInstructionCount += acceleratorCurrentProcCount - this.acceleratorLastProcCount;
				this.acceleratorLastProcCount = acceleratorCurrentProcCount;
			}
			return this.vmProcessedInstructionCount;
		}
	}


	/**
	 * Returns operation code(s) of currently executed instruction(s) on this instance of the VM.
	 * 
	 * This method returns an array, because generary this VM may execute multiple instructions in 1 cycle.
	 * Also, when no instructins are being executed, an empty array will be returned.
	 *
	 * @return The opecode(s) of currently executed instruction(s) on this VM.
	 */
	public OperationCode[] getCurrentlyExecutedOperationCodes() {

		// This method probably be called from a different thread from the thread executing the code.
		// So the followings are enclosed by a synchronized block, to avoid effects of thread caches.
		synchronized (this) {

			// The Processor and the Accelerator of fields of this instance are initialized by the constructor of this class.
			// So we expect that they are not null here.
			if (this.processor == null || this.accelerator == null) {
				throw new VnanoFatalException("The processor/accelerator have not initialized yet.");
			}

			// Get opcodes of the currently executed instructions, from the Processor and Accelerator.
			OperationCode[] acceleratorOperationCodes = this.accelerator.getCurrentlyExecutedOperationCodes();
			OperationCode[] prpcessorOperationCodes = this.processor.getCurrentlyExecutedOperationCodes();

			// When the Accelerator has the valid counter value, it means that the instructions are mainly being processed on the Accelerator.
			// Some instructions are bypassed to and being processed on the Processor, but the counter value of the Accelerator contains their count.
			// So simply return the counter of the Accelerator.
			if (acceleratorOperationCodes.length != 0) {
				return acceleratorOperationCodes;

			// When the Accelerator has no valid counter value,
			// it means that the Accelerator is disabled, and the Processor are processing all instructions.
			// So return the counter value of the Processor.
			// Processor 側の値を返す
			} else if (prpcessorOperationCodes.length != 0) {
				return prpcessorOperationCodes;

			// When both the Processor and the Accelerator have no valid counter values, 
			// it means that no instructions are executed on this VM yet.
			// So return an empty array.
			} else {
				return new OperationCode[0];
			}
		}
	}
}
