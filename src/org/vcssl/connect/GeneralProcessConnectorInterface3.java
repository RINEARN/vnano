/*
 * ==================================================
 * General Process Connector Interface 3 (GPCI 3)
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2017-2022 by RINEARN
 * ==================================================
 */

// THE SPECIFICATION OF THIS INTERFACE HAD NOT BEEN FINALIZED YET.
// NOTE THAT ANY MODIFICATIONS MIGHT BE APPLIED FOR THIS INTERFACE.

package org.vcssl.connect;


/**
 * An interface for implementing function plug-ins, but it has not been supported by any scripting engine yet.
 */
public interface GeneralProcessConnectorInterface3 {

	/** The type ID of this interface (value: "GPCI") referred when the plug-in will be loaded. */
	public static final String INTERFACE_TYPE_ID = "GPCI";

	/** The generation of this interface (value: "3"). */
	public static final String INTERFACE_GENERATION = "3";


	/**
	 * Returns whether this plug-in can process the function having the specified name.
	 * 
	 * @param functionName The name of the function.
	 * @return Returns true if this plug-in can process the function having the specified name.
	 */
	public abstract boolean isProcessable(String functionName);


	/**
	 * Processes the function having the spacified name.
	 * 
	 * @param functionName The name of the function to be processed.
	 * @param arguments An array storing values of all actual arguments.
	 * @return An array storing return value of the function.
	 */
	public abstract String[] process(String functionName, String[] arguments);


	/**
	 * Returns the instance of "Class" class, representing the interface or the class of the engine connector, 
	 * which is an object for communicating with the scripting engine.
	 * 
	 * The instance of the specified interface/class by this method will be passed to the argument of 
	 * {@link initializeForConnection(Object)}, {@link initializeForExecution(Object)},
	 * {@link finalizeForTermination(Object)}, {@link finalizeForDisconnection(Object)} methods.
	 * 
	 * What type of interfaces are available depend on the implementation of the scripting engine, but at least, 
	 * {@link EngineConnectorInterface1 ECI1} is guaranteed to be available by the specification of GPCI3.
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
