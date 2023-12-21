/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.vm.memory.DataContainer;


/**
 * The abstract class of variables accessible in Vnano Engine.
 *
 * In Vnano Engine, internal variable will be handled as instances of
 * {@link InternalVariable InternalVariable} class,
 * and it is a subclass of this abstract class.
 *
 * In addition, external variable plug-ins will be connected through
 * adapters extending this abstract class,
 * e.g.: {@link Xvci1ToVariableAdapter Xvci1ToVariableAdapter}.
 */
public abstract class AbstractVariable {


	/**
	 * The empty constructor.
	 */
	protected AbstractVariable(){}


	/**
	 * Sets the name of this variable.
	 *
	 * This method is used for setting an alias for an external variable.
	 * This method isn't available when this function is an internal variable,
	 * because the name of internal variable isn't modifiable.
	 *
	 * @param variableName The name of this variable.
	 * @throws VnanoFatalException
	 *      Thrown when the name of this variable isn't modifiable.
	 */
	public abstract void setVariableName(String variableName);


	/**
	 * Gets the name of this variable.
	 *
	 * @return The name of this variable.
	 */
	public abstract String getVariableName();


	/**
	 * Returns whether this variable belongs to any namespace.
	 *
	 * @return Returns true if this variable belongs to a namespace.
	 */
	public abstract boolean hasNamespaceName();


	/**
	 * Gets the name of the namespace to which this variable belongs.
	 *
	 * @return The name of the namespace to which this variable belongs.
	 */
	public abstract String getNamespaceName();


	/**
	 * Sets the name of the namespace to which this variable belongs.
	 *
	 * @namespaceName The name of the namespace to which this variable belongs.
	 */
	public abstract void setNamespaceName(String namespaceName);


	/**
	 * Gets the name of the data-type of this variable.
	 * In the data-type name, array declaration parts [][]...[] aren't contained.
	 *
	 * @return The name of the data-type of this variable.
	 */
	public abstract String getDataTypeName();


	/**
	 * Gets the array-rank of this variable.
	 *
	 * Note that, the array-rank of an scalar is 0.
	 *
	 * @return The array-rank of this variable.
	 */
	public abstract int getArrayRank();


	/**
	 * Gets the data container storing data of this variable.
	 *
	 * @return The data container storing data of this variable.
	 */
	public abstract DataContainer<?> getDataContainer() throws VnanoException;


	/**
	 * Sets the data container storing data of this variable.
	 *
	 * @param dataContainer The data container storing data of this variable.
	 */
	public abstract void setDataContainer(DataContainer<?> dataContainer) throws VnanoException;


	/**
	 * Returns whether this variable is constant.
	 *
	 * @return Returns true if this variable is constant.
	 */
	public abstract boolean isConstant();


	/**
	 * Returns whether this variable has a serial number,
	 * which is a number to distinguish multiple variables having the same name.
	 *
	 * @return Returns true if this variable has a serial number.
	 */
	public abstract boolean hasSerialNumber();


	/**
	 * Gets the serial number which is a number to distinguish multiple variables having the same name.
	 *
	 * @return The serial number.
	 */
	public abstract int getSerialNumber();
}

