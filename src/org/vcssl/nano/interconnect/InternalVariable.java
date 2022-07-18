/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

/**
 * The class of an internal variable.
 */
public class InternalVariable extends AbstractVariable {

	/** The name of this variable. */
	private String variableName = null;

	/** The name of the data-type of this variable. */
	private String dataTypeName = null;

	/** The array-rank (0 for a scalar) of this variable. */
	private int rank = -1;

	/** The flag representing whether this variable is a constant (unmodifiable). */
	private boolean constantness = false;

	/** The data-container in which data of this variable will be stored. */
	private DataContainer<?> dataContainer;

	/** The serial number of this variable, to distinguish multiple variables having the same name. */
	private int serialNumber = -1;

	/** The flag representing whether this variable has a serial number. */
	private boolean hasSerialNumber = false;


	/**
	 * Creates a new variable having specified information.
	 * 
	 * Note that, the data-container for storing data will not generated automatically, 
	 * so create it separately, and set it by {@link InternalVariable#setDataUnit dataUnit} method.
	 * 
	 * @param variableName The name of this variable.
	 * @param dataTypeName The name of the data-type of this variable.
	 * @param rank The array-rank (0 for a scalar) of this variable.
	 * @param constantness The flag representing whether this variable is a constant (unmodifiable).
	 */
	public InternalVariable(String variableName, String dataTypeName, int rank, boolean constantness) {
		this.variableName = variableName;
		this.dataTypeName = dataTypeName;
		this.rank = rank;
		this.constantness = constantness;
	}

	/**
	 * Creates a new variable having specified information, including a serial number.
	 * 
	 * Note that, the data-container for storing data will not generated automatically, 
	 * so create it separately, and set it by {@link InternalVariable#setDataUnit dataUnit} method.
	 * 
	 * @param variableName The name of this variable.
	 * @param dataTypeName The name of the data-type of this variable.
	 * @param rank The array-rank (0 for a scalar) of this variable.
	 * @param constantness The flag representing whether this variable is a constant (unmodifiable).
	 * @param serialNumber The serial number of this variable, to distinguish multiple variables having the same name.
	 */
	public InternalVariable(String variableName, String dataTypeName, int rank, boolean isConstant, int serialNumber) {
		this(variableName, dataTypeName, rank, isConstant);
		this.serialNumber = serialNumber;
		this.hasSerialNumber = true;
	}


	/**
	 * Gets the name of this variable.
	 *
	 * @return The name of this variable.
	 */
	@Override
	public String getVariableName() {
		return this.variableName;
	}


	/**
	 * Sets the name of this variable.
	 * However, this method isn't available, on the current version.
	 * 
	 * This method is used for setting an alias for external variables.
	 * On the other hand, on the current language specification of Vnano, 
	 * an internal variables can not have multiple names (aliases),
	 * so VnanoFatalException will be thrown when this method is invoked.
	 *
	 * @param variableName The name of this variable.
	 */
	@Override
	public void setVariableName(String variableName) {
		throw new VnanoFatalException("Names of internal variables should not be changed.");
	}


	/**
	 * Returns whether this variable belongs to any namespace.
	 *
	 * @return Returns true if this variable belongs to a namespace.
	 */
	@Override
	public boolean hasNamespaceName() {
		return false;
	}


	/**
	 * Gets the name of the namespace to which this variable belongs.
	 *
	 * @return The name of the namespace to which this variable belongs.
	 */
	@Override
	public final String getNamespaceName() {
		throw new VnanoFatalException("Internal variables can not belongs to any namespaces.");
	}


	/**
	 * Sets the name of the namespace to which this variable belongs.
	 * However, this method isn't available, on the current version.
	 *
	 * @namespaceName The name of the namespace to which this variable belongs.
	 */
	@Override
	public final void setNamespaceName(String namespaceName) {
		throw new VnanoFatalException("Internal variables can not belongs to any namespaces.");
	}


	/**
	 * Gets the name of the data-type of this variable.
	 * In the data-type name, array declaration parts [][]...[] aren't contained.
	 *
	 * @return The name of the data-type of this variable.
	 */
	@Override
	public String getDataTypeName() {
		return this.dataTypeName;
	}


	/**
	 * Gets the data-container for storing data of this variable.
	 * 
	 * @return The data-container for storing data of this variable.
	 */
	@Override
	public DataContainer<?> getDataContainer() {
		return this.dataContainer;
	}


	/**
	 * Sets the data-container for storing data of this variable.
	 * 
	 * @param dataContainer The data-container for storing data of this variable.
	 */
	@Override
	public void setDataContainer(DataContainer<?> dataContainer) {
		this.dataContainer = dataContainer;
	}


	/**
	 * Gets array-ranks of all parameters.
	 * 
	 * Note that, the array-rank of an scalar is 0.
	 *
	 * @return The array storing array-ranks of all parameters.
	 */
	@Override
	public int getRank() {
		return this.rank;
	}
	// TO DO: rename to: getArrayRank()


	/**
	 * Returns whether this variable is constant.
	 * 
	 * @return Returns true if this variable is constant.
	 */
	@Override
	public boolean isConstant() {
		return this.constantness;
	}


	/**
	 * Returns whether this variable has a serial number,
	 * which is a number to distinguish multiple variables having the same name.
	 *
	 * @return Returns true if this variable has a serial number.
	 */
	@Override
	public boolean hasSerialNumber() {
		return this.hasSerialNumber;
	}


	/**
	 * Gets the serial number which is a number to distinguish multiple variables having the same name.
	 * 
	 * @return The serial number.
	 */
	@Override
	public int getSerialNumber() {
		return this.serialNumber;
	}
}
