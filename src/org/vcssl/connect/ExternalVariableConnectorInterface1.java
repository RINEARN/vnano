/*
 * ==================================================
 * External Variable Connector Interface 1 (XVCI 1)
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2017-2022 by RINEARN
 * ==================================================
 */

// THE SPECIFICATION OF THIS INTERFACE HAD BEEN FINALIZED AT 2022/08/31.
// NO MODIFICATIONS WILL BE APPLIED FOR THIS INTERFACE, EXCLUDING DOCUMENTS/COMMENTS.

package org.vcssl.connect;


/**
 * An interface (abbreviated as XVCI1) for implementing external variable plug-ins 
 * which provide variables available in scripts.
 * 
 * Currently, this interface is supported on the Vnano Engine, 
 * and has not been supported on the VCSSL Engine yet.
 */
public interface ExternalVariableConnectorInterface1 {

	/** The type ID of this interface (value: "XVCI") referred when the plug-in will be loaded. */
	public static final String INTERFACE_TYPE_ID = "XVCI";

	/** The generation of this interface (value: "1"). */
	public static final String INTERFACE_GENERATION = "1";


	/**
	 * Gets the name of this variable.
	 * 
	 * @return The name of this variable.
	 */
	public abstract String getVariableName();


	/**
	 * Returns the instance of "Class" class representing the data-type and the array-rank of this variable.
	 * 
	 * For example, 
	 * returns double.class for double-type ("float" in scripts) variable, 
	 * and returns long[][].class for long[][]-type ("int[][]" in scripts) variable.
	 * 
	 * @return The Class representing the data-type and the array-rank of this variable.
	 */
	public abstract Class<?> getDataClass();


	/**
	 * Returns the instance of "Class" class representing data-I/O interfaces for accessing to data of this variable, 
	 * when {@link isDataConversionNecessary() data-conversion feature} is disabled.
	 * 
	 * As interfaces for accessing to data of a scalar variable, 
	 * {@link Int64ScalarDataAccessorInterface1 Int64 SDAI} (for long-type variable),
	 * {@link Float64ScalarDataAccessorInterface1 Float64 SDAI} (for double-type variable),
	 * {@link BoolScalarDataAccessorInterface1 Bool SDAI} (for boolean-type variable),
	 * {@link StringScalarDataAccessorInterface1 String SDAI} (for String-type variable),
	 * and
	 * {@link ArrayDataAccessorInterface1 ADAI} (generic) 
	 * are available.
	 * For an array variable, only 
	 * {@link ArrayDataAccessorInterface1 ADAI}
	 * is available for accessing to data.
	 * 
	 * @return The Class representing the I/O interface for accessing to the data of this variable.
	 */
	public abstract Class<?> getDataUnconvertedClass();


	/**
	 * Returns whether this variable is a constant.
	 * 
	 * @return Returns true if this variable is a constant.
	 */
	public abstract boolean isConstant();


	/**
	 * (Unsupported feature on the current version of VCSSL/Vnano Engine) Returns whether this variable is a reference.
	 * 
	 * @return Returns true if this variable is a reference.
	 */
	public abstract boolean isReference();


	/**
	 * (Unsupported feature on the current version of VCSSL/Vnano Engine) Returns whether the data-type of this variable varies arbitrary.
	 * 
	 * @return Returns true if the data-type of this variable varies arbitrary.
	 */
	public abstract boolean isDataTypeArbitrary();


	/**
	 * (Unsupported feature on the current version of VCSSL/Vnano Engine) Returns whether the array-rank of this variable varies arbitrary.
	 * 
	 * @return Returns true if the array-rank of this variable varies arbitrary.
	 */
	public abstract boolean isArrayRankArbitrary();


	/**
	 * Returns whether the data-conversions for accessing data of this variable is necessary.
	 * 
	 * When this feature is enabled (when this method returns "true"),
	 * you can set/get data of this variable by using simple data-types.
	 * For example, you can get/set Double instance for double-type ("float" in scripts) variable, 
	 * long[][] instance for long[][]-type ("int[][]" in scripts) variable, and so on.
	 *
	 * On the other hand, when this feature is disabled (when this method retunrs "false"),
	 * it is necessary to access to data through a data-I/O interface, 
	 * of which type is specified as a return values of {@link getDataUnconvertedClass()} method. 
	 *
	 * As interfaces for accessing to data of a scalar variable, 
	 * {@link Int64ScalarDataAccessorInterface1 Int64 SDAI} (for long-type variable),
	 * {@link Float64ScalarDataAccessorInterface1 Float64 SDAI} (for double-type variable),
	 * {@link BoolScalarDataAccessorInterface1 Bool SDAI} (for boolean-type variable),
	 * {@link StringScalarDataAccessorInterface1 String SDAI} (for String-type variable),
	 * and
	 * {@link ArrayDataAccessorInterface1 ADAI} (generic) 
	 * are available.
	 * For an array variable, only 
	 * {@link ArrayDataAccessorInterface1 ADAI}
	 * is available for accessing to data.
	 * 
	 * @return Returns true if the data-conversions are necessary.
	 */
	public abstract boolean isDataConversionNecessary();


	/**
	 * Returns the data of this variable.
	 *
	 * This method is used when the data-conversion feature is enabled 
	 * (when {@link isDataConversionNecessary()} returns false).
	 *
	 * @return The data of this variable.
	 * @throws ConnectorException Thrown when failed to access to data.
	 */
	public abstract Object getData() throws ConnectorException;


	/**
	 * Gets the data of this variable through the argument.
	 *
	 * This method is used when the data-conversion feature is disabled 
	 * (when {@link isDataConversionNecessary()} returns true).
	 *
	 * @param dataContainer The data container object for storing the data to be gotten.
	 * @throws ConnectorException Thrown when failed to access to the data.
	 */
	public abstract void getData(Object dataContainer) throws ConnectorException;


	/**
	 * Sets the data of this variable.
	 * 
	 * @param data The data of this variable to be set.
	 * @throws ConnectorException Thrown when failed to access to the data.
	 */
	public abstract void setData(Object data) throws ConnectorException;


	/**
	 * Returns the instance of "Class" class, representing the interface or the class of the engine connector, 
	 * which is an object for communicating with the scripting engine.
	 * 
	 * The instance of the specified interface/class by this method will be passed to the argument of 
	 * {@link initializeForConnection(Object)}, {@link initializeForExecution(Object)},
	 * {@link finalizeForTermination(Object)}, {@link finalizeForDisconnection(Object)} methods.
	 * 
	 * What type of interfaces are available depend on the implementation of the scripting engine, but at least, 
	 * {@link EngineConnectorInterface1 ECI1} is guaranteed to be available by the specification of XVCI1.
	 * 
	 * @return The Class representing the interface/class for communicating with the scripting engine.
	 */
	public abstract Class<?> getEngineConnectorClass();


	/**
	 * Performs the initialization process necessary when this plug-in is connected to the scripting engine.
	 *
	 * @param engineConnector The engine connector (see: {@link ExternalVariableConnectorInterface1#getEngineConnectorClass()} method).
	 * @throws ConnectorException Thrown when the initialization has failed.
	 */
	public abstract void initializeForConnection(Object engineConnector) throws ConnectorException;


	/**
	 * Performs the finalization process necessary when this plug-in is disconnected from the scripting engine.
	 *
	 * @param engineConnector The engine connector (see: {@link ExternalVariableConnectorInterface1#getEngineConnectorClass()} method).
	 * @throws ConnectorException Thrown when the finalization has failed.
	 */
	public abstract void finalizeForDisconnection(Object engineConnector) throws ConnectorException;


	/**
	 * Performs the initialization process necessary for each execution of a script.
	 *
	 * @param engineConnector The engine connector (see: {@link ExternalVariableConnectorInterface1#getEngineConnectorClass()} method).
	 * @throws ConnectorException Thrown when the initialization has failed.
	 */
	public abstract void initializeForExecution(Object engineConnector) throws ConnectorException;


	/**
	 * Performs the finalization process necessary for each execution of a script.
	 *
	 * @param engineConnector The engine connector (see: {@link ExternalVariableConnectorInterface1#getEngineConnectorClass()} method).
	 * @throws ConnectorException Thrown when the finalization has failed.
	 */
	public abstract void finalizeForTermination(Object engineConnector) throws ConnectorException;

}
