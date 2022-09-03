/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.memory;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;

import org.vcssl.nano.vm.VirtualMachineObjectCode;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.interconnect.AbstractVariable;
import org.vcssl.nano.interconnect.VariableTable;
import org.vcssl.nano.VnanoException;


/**
 * The class takes on the memory in the virtual machine of the Vnano.
 * 
 * The architecture of the VM of the Vnano is a kind of a vector processor, so the unit of this memory is an array.
 * This memory internally has lists of data-containers (instances of {@link DataContainer} class), 
 * and each data-container can store an array data.
 * An unique address is assigned for each data-container.
 * 
 * This memory has multiple lists of data-containers. We call them as "partitions".
 * For example, "LOCAL" partition stores data of local variables,
 * "CONSTANT" partition stores data of constant literal values, 
 * and "REGISTER" partiton stores temporary values of operations performed by processors.
 */
public final class Memory {

	/**
	 * The enum for specifying each partition in the {@link Memory Memory}.
	 */
	public static enum Partition {

		/** Represents the GLOBAL partition, in which data of global (external) variables are stored. */
		GLOBAL,

		/** Represents the LOCAL partition, in which data of local (internal) variables are stored. */
		LOCAL,

		/** Represents the CONSTANT partition, in which constant values (immediate literal values and so on) are stored. */
		CONSTANT,

		/** Represents the REGISTER partition, in which temporary values of operations performed by processors are stored. */
		REGISTER,

		/** Represents the STACK partition, in which arguments and return values/addresses of function calls are stored. */
		STACK,

		/** Represents the special partition storing only an empty data-container, used for placeholder operands of some instructions. */
		NONE,
	}


	/** The list of data-containers of the GLOBAL partition, in which data of global (external) variables are stored. */
	private List<DataContainer<?>> globalList;

	/** The list of data-containers of the LOCAL partition, in which data of local (internal) variables are stored. */
	private List<DataContainer<?>> localList;

	/** The list of data-containers of the CONSTANT partition, in which constant values (immediate literal values and so on) are stored. */
	private List<DataContainer<?>> constantList;

	/** The list of data-containers of the REGISTER partition, in which temporary values of operations performed by processors are stored. */
	private List<DataContainer<?>> registerList;

	/** The list of data-containers of the STACK partition, in which arguments and return values/addresses of function calls are stored. */
	private Deque<DataContainer<?>> stack;

	/** The data-container for storing the evaluation result of a script, if it exists. */
	private DataContainer<?> resultContainer;

	/** The empty data-container returned as a data in NONE partition, used for placeholder operands of some instructions. */
	private DataContainer<Void> voidContainer;

	/** The Map mapping each element of Partition enum to the corresponding list of data-containers. */
	private HashMap<Partition, List<DataContainer<?>>> containerListMap;


	/**
	 * Creates an empty memory instance.
	 */
	public Memory() {
		this.registerList = new ArrayList<DataContainer<?>>();
		this.localList = new ArrayList<DataContainer<?>>();
		this.globalList = new ArrayList<DataContainer<?>>();
		this.constantList = new ArrayList<DataContainer<?>>();
		this.stack = new ArrayDeque<DataContainer<?>>();

		this.containerListMap = new HashMap<Partition, List<DataContainer<?>>>();
		this.containerListMap.put(Memory.Partition.REGISTER, this.registerList);
		this.containerListMap.put(Memory.Partition.LOCAL, this.localList);
		this.containerListMap.put(Memory.Partition.GLOBAL, this.globalList);
		this.containerListMap.put(Memory.Partition.CONSTANT, this.constantList);

		this.voidContainer = new DataContainer<Void>();
	}


	/**
	 * Get the number of storable data-containers (size) of the specified partition.
	 *
	 * @param partition The partition you want to get the size of it.
	 * @return The number of storable data-containers (size).
	 */
	public final int getSize(Memory.Partition partition) {
		if (partition == Memory.Partition.STACK) {
			return this.stack.size();
		} else {
			return this.containerListMap.get(partition).size();
		}
	}


	/**
	 * Gets the data-container stored at the specified address in the specified partition.
	 *
	 * @param partition The partition in which the data-container is stored.
	 * @param address The address at which the data-container is stored.
	 * @return The data-container stored at the specified address in the specified partition.
	 * @throws VnanoFatalException Thrown when the specified address is out of bounds.
	 */
	public final DataContainer<?> getDataContainer(Partition partition, int address) {
		if (partition == Memory.Partition.NONE) {
			return this.voidContainer;
		}
		if (!containerListMap.containsKey(partition)) {
			throw new VnanoFatalException("Unsupported operation for " + partition + " partition.");
		}
		List<DataContainer<?>> list = this.containerListMap.get(partition);
		try {
			return list.get(address);
		} catch (IndexOutOfBoundsException e){
			throw new VnanoFatalException("Address " + address + " is out of bounds of the " + partition + " partition.");
		}
	}


	/**
	 * Stores the specified data-container, at the specified address in the specified partition.
	 * 
	 * @param partition The partition in which you want to store the data-container.
	 * @param address The address at which you want to store the data-container.
	 * @param container The data-container to be stored.
	 */
	public final void setDataContainer(Partition partition, int address, DataContainer<?> container) {
		if (!containerListMap.containsKey(partition)) {
			throw new VnanoFatalException("Unsupported operation for " + partition + " partition.");
		}
		List<DataContainer<?>> list = this.containerListMap.get(partition);
		if (address < list.size()) {
			list.set(address, container);
		} else if (address == list.size()) {
			list.add(container);
		} else {
			this.paddList(list, address-list.size());
			list.add(container);
		}
	}


	/**
	 * Sets all data-containers in the specified partition.
	 * 
	 * @param partition The partition in which stores the data-containers.
	 * @param containers The data-containers to be stored in the partition.
	 * @throws VnanoFatalException Thrown when the NONE partition is specified.
	 */
	public final void setDataContainers(Partition partition, DataContainer<?>[] containers) {
		if (!containerListMap.containsKey(partition)) {
			throw new VnanoFatalException("Unsupported operation for " + partition + " partition.");
		}
		List<DataContainer<?>> list = this.containerListMap.get(partition);
		list.clear();
		for (DataContainer<?> container: containers) {
			list.add(container);
		}
	}


	/**
	 * Gets all data-containers stored in the specified partition.
	 * 
	 * @param partition The partition from which you want to get all data-container.
	 * @param containers The data-containers stored in the partition.
	 * @throws VnanoFatalException Thrown when the NONE partition is specified.
	 */
	public final DataContainer<?>[] getDataContainers(Memory.Partition partition) {
		if (!containerListMap.containsKey(partition)) {
			throw new VnanoFatalException("Unsupported operation for " + partition + " partition.");
		}
		List<DataContainer<?>> list = this.containerListMap.get(partition);
		return list.toArray(new DataContainer<?>[]{});
	}


	/**
	 * Padds empty data-containers into the specified list.
	 * This method is used for initializing the register partition and so on.
	 * 
	 * @param list The list of data-containers.
	 * @param n The number of data-containers to be padded to the list.
	 */
	private final void paddList(List<DataContainer<?>> list, int n) {
		for (int i=0; i<n; i++) {
			list.add(new DataContainer<Object>());
		}
	}


	/**
	 * Push the data-container to the top of the STACK partition.
	 *
	 * @param dataContainer The data-container to be pushed.
	 */
	public final void push(DataContainer<?> dataContainer) {
		this.stack.push(dataContainer);
	}


	/**
	 * Pops the data-container from the top the STACK partition.
	 *
	 * @return The poped data-container.
	 */
	public final DataContainer<?> pop() {
		return this.stack.pop();
	}


	/**
	 * Get the data-container at the top of the STACK partition, without popping it.
	 *
	 * @return The data-container at the top of the STACK partition.
	 */
	public final DataContainer<?> peek() {
		return this.stack.peek();
	}


	/**
	 * Returns whether the data-container of the evaluation result of the executed script exists.
	 *
	 * @return Returns true if the evaluation result exists.
	 */
	public final boolean hasResultDataContainer() {
		return (this.resultContainer != null);
	}


	/**
	 * Sets the data-container of the evaluation result of the executed script.
	 *
	 * @param resultContainer The data-container of the evaluation result of the executed script.
	 */
	public final void setResultDataContainer(DataContainer<?> resultContainer) {
		this.resultContainer = resultContainer;
	}


	/**
	 * Gets the data-container of the evaluation result of the executed script.
	 *
	 * @return The data-container of the evaluation result of the executed script.
	 */
	public final DataContainer<?> getResultDataContainer() {
		return this.resultContainer;
	}


	/**
	 * Allocates lists in this memory, for executing the specified VM object code.
	 *
	 * @param vmObjectCode The VM object code, executed with using this memory.
	 * @param globalVariableTable The table of the global (external) variables.
	 * @throws VnanoException Thrown when failed to get the data-container of a global (external) variable.
	 */
	public final void allocate(VirtualMachineObjectCode vmObjectCode, VariableTable globalVariableTable)
			throws VnanoException {

		// Allocate REGISTER parition.
		int maxRegisterAddress = vmObjectCode.getMaximumRegisterAddress();
		for (int registerAddress=0; registerAddress<=maxRegisterAddress; registerAddress++) {
			this.registerList.add(new DataContainer<Void>());
		}

		// Allocate LOCAL parition.
		int maxLocalAddress = vmObjectCode.getMaximumLocalAddress();
		for (int localAddress=0; localAddress<=maxLocalAddress; localAddress++) {
			this.localList.add(new DataContainer<Void>());
		}

		// Allocate GLOBAL parition.
		int globalSize = globalVariableTable.getSize();
		for (int globalIndex=0; globalIndex<globalSize; globalIndex++) {
			AbstractVariable variable = globalVariableTable.getVariableByIndex(globalIndex);
			this.globalList.add(variable.getDataContainer());
		}

		// Allocate CONSTANT parition.
		int maxConstantAddress = vmObjectCode.getMaximumConstantAddress();
		DataContainer<?>[] constantDataContainers = vmObjectCode.getConstantDataContainers();
		for (int constantAddress=0; constantAddress<=maxConstantAddress; constantAddress++) {
			this.constantList.add(constantDataContainers[constantAddress]);
		}
	}
}
