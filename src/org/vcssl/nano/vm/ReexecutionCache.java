/*
 * Copyright(C) 2023 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm;

import org.vcssl.nano.interconnect.DataConverter;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.processor.Instruction;

/**
 * The container class for storing resources generated for executing the last code,
 * to accelerate the re-executions of the same code.
 */
public class ReexecutionCache {

	/** The VM object code executed last time (call "last code" in this class). */
	private volatile VirtualMachineObjectCode lastObjectCode;

	/** The memory instance allocated for running the last code. */
	private volatile Memory memory = null;

	/** The container of the return value of the last code. */
	private volatile DataContainer<?> resultDataContainer = null;

	/** The converter of the return value of the last code. */
	private volatile DataConverter resultDataConverter = null;

	/** The flag representing whether the accelerator is enabled. */
	private boolean acceleratorEnabled = false;


	/**
	 * Create an empty instance storing nothing.
	 */
	public ReexecutionCache() {
	}


	/**
	 * Sets the VM object code executed last time (call "last code" in this class).
	 *
	 * @param lastObjectCode The VM object code executed last time.
	 */
	public void setLastObjectCode(VirtualMachineObjectCode lastObjectCode) {
		this.lastObjectCode = lastObjectCode;
	}

	/**
	 * Gets the VM object code executed last time (call "last code" in this class).
	 *
	 * @return The VM object code executed last time.
	 */
	public VirtualMachineObjectCode getLastObjectCode() {
		return this.lastObjectCode;
	}


	/**
	 * Sets the memory instance allocated for running the last code.
	 *
	 * @param memory The memory instance allocated for running the last code.
	 */
	public void setMemory(Memory memory) {
		this.memory = memory;
	}

	/**
	 * Gets the memory instance allocated for running the last code.
	 *
	 * @return The memory instance allocated for running the last code.
	 */
	public Memory getMemory() {
		return this.memory;
	}


	/**
	 * Sets the resources for converting the return value of the last code.
	 *
	 * @param resultDataContainer The container of the return value of the last code.
	 * @param resultDataConverter The converter of the return value of the last code.
	 */
	public void setResultDataResources(DataContainer<?> resultDataContainer, DataConverter resultDataConverter) {
		this.resultDataContainer = resultDataContainer;
		this.resultDataConverter = resultDataConverter;
	}

	/**
	 * Checks whether this instance has the resources for converting the return value of the last code.
	 *
	 * @return Returns true if this instance has the resources for converting the return value.
	 */
	public boolean hasResultDataResources() {
		return (this.resultDataContainer != null);
	}

	/**
	 * Gets the the container of the return value of the last code.
	 *
	 * @return The container of the return value of the last code.
	 */
	public DataContainer<?> getResultDataContainer() {
		return this.resultDataContainer;
	}

	/**
	 * Gets the converter of the return value of the last code.
	 *
	 * @return The converter of the return value of the last code.
	 */
	public DataConverter getResultDataConverter() {
		return this.resultDataConverter;
	}


	/**
	 * Sets whether the accelerator is enabled.
	 *
	 * @param enabled Specify true if the accelerator is enabled.
	 */
	public void setAcceleratorEnabled(boolean enabled) {
		this.acceleratorEnabled = enabled;
	}

	/**
	 * Checks whether the accelerator is enabled.
	 *
	 * @return Returns true if the accelerator is enabled.
	 */
	public boolean isAcceleratorEnabled() {
		return this.acceleratorEnabled;
	}
}
