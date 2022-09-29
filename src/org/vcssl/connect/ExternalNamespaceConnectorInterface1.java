/*
 * ==================================================
 * External Namespace Connector Interface 1 (XNCI 1)
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2019-2022 by RINEARN
 * ==================================================
 */

// THE SPECIFICATION OF THIS INTERFACE HAD BEEN FINALIZED AT 2022/08/31.
// NO MODIFICATIONS WILL BE APPLIED FOR THIS INTERFACE, EXCLUDING DOCUMENTS/COMMENTS.

package org.vcssl.connect;


/**
 * An interface (abbreviated as XNCI1) for implementing namespace plug-ins 
 * which provides multiple functions and variables available in scripts.
 * 
 * Currently, this interface is supported on the Vnano Engine, 
 * and has not been supported on the VCSSL Engine yet.
 */
public interface ExternalNamespaceConnectorInterface1 {

	/** The type ID of this interface (value: "XNCI") referred when the plug-in will be loaded. */
	public static final String INTERFACE_TYPE_ID = "XNCI";

	/** The generation of this interface (value: "1"). */
	public static final String INTERFACE_GENERATION = "1";


	/**
	 * Gets the name of this namespace.
	 * 
	 * @return The name of this namespace.
	 */
	public abstract String getNamespaceName();


	/**
	 * Returns whether it is mandatory to specify of this namespace explicitly 
	 * when accessing member functions/variables.
	 * 
	 * This feature may be ignored on the script engine which does not support it.
	 * 
 	 * @return Returns true if this namespace is mandatory to be specified to access members.
	 * 
	 */
	public abstract boolean isMandatoryToAccessMembers();


	/**
	 * Gets all functions belong to this namespace.
	 * 
	 * @return The array storing all functions belong to this namespace.
	 */
	public abstract ExternalFunctionConnectorInterface1[] getFunctions();


	/**
	 * Gets all variables belong to this namespace.
	 * 
	 * @return The array storing all variables belong to this namespace.
	 */
	public abstract ExternalVariableConnectorInterface1[] getVariables();


	/**
	 * Gets all structs belong to this namespace.
	 * 
	 * However, the specification of ExternalStructConnectorInterface1 (XSCI1) has not been determined yet, 
	 * so as for now, the content of the XSCI1's interface file is almost empty.
	 * 
	 * Hence, at least now, this method has no meanings so it always returns (should be implemented to return) an empty array.
	 * This method will be available after when XSCI1's specification is determined.
	 * 
	 * @return The array storing all structs belong to this namespace.
	 */
	public abstract ExternalStructConnectorInterface1[] getStructs();


	/**
	 * Gets the instance of "Class" class, representing the interface or the class of the engine connector, 
	 * which is an object for communicating with the scripting engine.
	 * 
	 * The instance of the specified interface/class by this method will be passed to the argument of 
	 * {@link initializeForConnection(Object)}, {@link initializeForExecution(Object)},
	 * {@link finalizeForTermination(Object)}, {@link finalizeForDisconnection(Object)} methods.
	 * 
	 * What type of interfaces are available depend on the implementation of the scripting engine, but at least, 
	 * {@link EngineConnectorInterface1 ECI1} is guaranteed to be available by the specification of XNCI1.
	 * 
	 * @return The Class representing the interface/class for communicating with the scripting engine.
	 */
	public abstract Class<?> getEngineConnectorClass();


	/**
	 * Performs the pre-initialization process necessary when this plug-in is connected to the script engine.
	 * 
	 * On this interface, two connection-initialization (pre- and post-) processes can be implemented.
	 * This process (pre-) will be performed before when all member variables/functions are initialized.
	 *
	 * @param engineConnector The object for communicating with the script engine.
	 * @throws ConnectorException Thrown when the initialization has failed.
	 */
	public abstract void preInitializeForConnection(Object engineConnector) throws ConnectorException;


	/**
	 * Performs the post-initialization process necessary when this plug-in is connected to the script engine.
	 * 
	 * On this interface, two connection-initialization (pre- and post-) processes can be implemented.
	 * This process (post-) will be performed after when all member variables/functions are initialized.
	 *
	 * @param engineConnector The object for communicating with the script engine.
	 * @throws ConnectorException Thrown when the initialization has failed.
	 */
	public abstract void postInitializeForConnection(Object engineConnector) throws ConnectorException;


	/**
	 * Performs the pre-finalization process necessary when this plug-in is disconnected from the script engine.
	 * 
	 * On this interface, two connection-finalization (pre- and post-) processes can be implemented.
	 * This process (pre-) will be performed before when all member variables/functions are finalized.
	 *
	 * @param engineConnector The object for communicating with the script engine.
	 * @throws ConnectorException Thrown when the finalization has failed.
	 */
	public abstract void preFinalizeForDisconnection(Object engineConnector) throws ConnectorException;


	/**
	 * Performs the post-finalization process necessary when this plug-in is disconnected from the script engine.
	 * 
	 * On this interface, two connection-finalization (pre- and post-) processes can be implemented.
	 * This process (post-) will be performed after when all member variables/functions are finalized.
	 *
	 * @param engineConnector The object for communicating with the script engine.
	 * @throws ConnectorException Thrown when the finalization has failed.
	 */
	public abstract void postFinalizeForDisconnection(Object engineConnector) throws ConnectorException;


	/**
	 * Performs the pre-initialization process necessary for each execution of a script.
	 * 
	 * On this interface, two execution-initialization (pre- and post-) processes can be implemented.
	 * This process (pre-) will be performed before when all member variables/functions are initialized.
	 *
	 * @param engineConnector The object for communicating with the script engine.
	 * @throws ConnectorException Thrown when the initialization has failed.
	 */
	public abstract void preInitializeForExecution(Object engineConnector) throws ConnectorException;


	/**
	 * Performs the post-initialization process necessary for each execution of a script.
	 * 
	 * On this interface, two execution-initialization (pre- and post-) processes can be implemented.
	 * This process (post-) will be performed after when all member variables/functions are initialized.
	 *
	 * @param engineConnector The object for communicating with the script engine.
	 * @throws ConnectorException Thrown when the initialization has failed.
	 */
	public abstract void postInitializeForExecution(Object engineConnector) throws ConnectorException;


	/**
	 * Performs the pre-finalization process necessary for each execution of a script.
	 * 
	 * On this interface, two execution-finalization (pre- and post-) processes can be implemented.
	 * This process (pre-) will be performed before when all member variables/functions are finalized.
	 *
	 * @param engineConnector The object for communicating with the script engine.
	 * @throws ConnectorException Thrown when the finalization has failed.
	 */
	public abstract void preFinalizeForTermination(Object engineConnector) throws ConnectorException;


	/**
	 * Performs the post-finalization process necessary for each execution of a script.
	 * 
	 * On this interface, two execution-finalization (pre- and post-) processes can be implemented.
	 * This process (post-) will be performed after when all member variables/functions are finalized.
	 *
	 * @param engineConnector The object for communicating with the script engine.
	 * @throws ConnectorException Thrown when the finalization has failed.
	 */
	public abstract void postFinalizeForTermination(Object engineConnector) throws ConnectorException;

}

