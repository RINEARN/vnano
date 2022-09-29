/*
 * ==================================================
 * Permission Authorizer Connector Interface 1
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2020-2022 by RINEARN
 * ==================================================
 */

// THE SPECIFICATION OF THIS INTERFACE HAD BEEN FINALIZED AT 2022/09/30.
// NO MODIFICATIONS WILL BE APPLIED FOR THIS INTERFACE, EXCLUDING DOCUMENTS/COMMENTS.

package org.vcssl.connect;

import java.util.Map;


/**
 * An interface (abbreviated as PACI 1) for implementing permission-based security plug-ins.
 * 
 * PACI 1 plug-ins receive requests of permissions from other plug-ins (through script engines),
 * and determine whether allow or deny it (or ask to the user to determine it, if necessary).
 * 
 * What form of UI is suitable for the above determination highly depends on 
 * the application's UI, purpose, and so on. 
 * Hence, the application-side can design UI for the above determination,
 * and can connect (and use) it to script engines, by implementing this interface.
 * 
 * Also, on PACI 1, following two types of permission settings are assumed to exist:
 * 
 * <dl class="lang-en" style="margin-left: 30px;">
 *     <dt>Base permission settings:</dt>
 *         <dd>
 *         Settings which is set to applications/script engines explicitly, 
 *         and applied automatically to each script at the beginning time of its execution.
 *         </dd>
 *     <dt>Temporary permission settings:</dt>
 *         <dd>
 *         Temporary settings kept only during each script is running, 
 *         of which values may change by user's responses for requests of permissions.
 *         </dd>
 * 
 * When the script is executed, at the beginning, values of the base permission settings 
 * are copied to the temporary permission settings.
 * During the script is running, values of temporary permission settings are referred 
 * (and modified if necessary), when any permission is requested.
 * When the next script is executed, values of the temporary permission settings
 * are re-initialized by copying values of the base permission settings. 
 * 
 * The reason of why the temporary permission settings exists is: to avoid repeating the same request to an user.
 * For example, when the base permission for writing files is set to {@link ConnectorPermissionValue#ASK ASK},
 * if the temporary permission does not exist, we should ask the user to determine whether allows it or not, 
 * for every time when write a line to a file (we should ask 100 times for writing 100 lines).
 * It must be troublesome, so it is necessary to provide an option such as: 
 * "Allow the same request automatically, during the current script is running".
 * In order to implement the above feature, the temporary permission settings exists.
 */
public interface PermissionAuthorizerConnectorInterface1 {

	/** The type ID of this interface (value: "PACI") referred when the plug-in will be loaded. */
	public static final String INTERFACE_TYPE_ID = "PACI";

 	/** The generation of this interface (value: "1"). */
	public static final String INTERFACE_GENERATION = "1";


	/**
	 * Sets values of permission items, by a Map (permission map) storing names and values of permission items.
	 * 
	 * This method will be called from script engines or applications.
	 * 
	 * @param permissionMap The Map (permission map) storing names and values of permission items.
	 * @param setsToBase Specify "true" to set base permission settings, "false" to set temporary permission settings.
	 * 
	 * @throws ConnectorException 
	 *     Thrown when invalid permission settings are detected.
	 *     Also, thrown when "false" is specified to "setsToBase" but the temporary permission settings does not exist 
	 *     (not created yet or already disposed).
	 * 
	 */
	public abstract void setPermissionMap(Map<String, String> permissionMap, boolean setsToBase)
			throws ConnectorException;


	/**
	 * Gets the Map (permission map) storing names and current values of permission items.
	 * 
	 * This method will be called from script engines or applications.
	 * 
	 * @param getsFromBase Specify "true" to get base permission settings, "false" to get temporary permission settings.
	 * @return The Map (permission map) storing names and values of permission items.
	 * 
	 * @throws ConnectorException
	 *     Thrown when failed to get the specified permission settings
	 *     Also, thrown when "false" is specified to "setsToBase" but the temporary permission settings does not exist 
	 *     (not created yet or already disposed).
	 */
	public abstract Map<String, String> getPermissionMap(boolean getsFromBase)
			throws ConnectorException;


	/**
	 * Receives the request of the specified permission.
	 * 
	 * If the specified permission should be allowed, this method is required to do nothing (explicitly).
	 * If the specified permission should be denied, this method throws an ConnectorException.
	 * 
	 * This method will be called from other plug-ins through engine-connector interfaces.
	 * 
	 * @param permissionName The name of the requested permission item.
	 * @param requester The plug-in requesting the permission.
	 * @param metaInformation 
	 *     The information to be notified to the user 
	 *     (especially when the current value of the permission is set to {@link ConnectorPermissionValue#ASK ASK})
	 * 
	 * @throws ConnectorException Thrown when the requested permission has been denied.
	 */
	public abstract void requestPermission(String permissionName, Object requester, Object metaInformation)
			throws ConnectorException;


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
