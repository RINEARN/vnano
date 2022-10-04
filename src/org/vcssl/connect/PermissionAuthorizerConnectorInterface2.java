/*
 * ==================================================
 * Permission Authorizer Connector Interface 1
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2022 by RINEARN
 * ==================================================
 */

// AT THE PRESENT STAGE, THE SPECIFICATION OF THIS INTERFACE IS A "DRAFT".
// THE SPECIFICATION OF THIS INTERFACE MAY BE GOING TO BE CHANGED LARGELY IN FUTURE.

package org.vcssl.connect;

import java.util.Map;

/**
 * An interface (abbreviated as PACI 2) for implementing permission-based security plug-ins.
 * 
 * PACI 2 is a "draft" at the present stage, so it is not available yet on any scripting engine.
 */
public interface PermissionAuthorizerConnectorInterface2 {

	/** The type ID of this interface (value: "PACI") referred when the plug-in will be loaded. */
	public static final String INTERFACE_TYPE_ID = "PACI";

 	/** The generation of this interface (value: "2"). */
	public static final String INTERFACE_GENERATION = "2";


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
	 * Sets the value of the specified permission item.
	 *
	 * This method will be called from script engines/applications, 
	 * and may be called from other plug-ins through engine-connector interfaces.
	 * In the latter case, the permission for modifying/referencing permissions is required.
	 * 
	 * However, features which enable to modify/refer permission settings from other plug-ins are unsupported yet by 
	 * engine-connector interfaces (e.g.: {@link org.vcssl.connect.EngineConnectorInterface1 ECI1}. 
	 * Furthermore, it is unckear whether it will be supported in future. 
	 * This method is a kind of "mockup" of the above feature, at the present stage.
	 * 
	 * @param permissionName The name of the permission item to be set its value.
	 * @param value The value to be set.
	 * @param setsToBase Specify true to set the value of base permission settings, 
	 *     or false to set the value of temporary permission settings
	 * @param calledByEngine Specify true when calling this method by the scripting engine.
	 * 
	 * @throws ConnectorException
	 *     Thrown when the requested action has been denied, or unsupported, especially when "false" is specified to "calledByEngine".
	 *     Also, thrown when "false" is specified to "setsToBase" but the temporary permission settings does not exist.
	 */
	public abstract void setPermissionValue(String permissionName, String value, boolean setsToBase, boolean calledByEngine)
			throws ConnectorException;


	/**
	 * Gets the value of the specified permission item.
	 *
	 * This method will be called from script engines/applications, 
	 * and may be called from other plug-ins through engine-connector interfaces.
	 * In the latter case, the permission for modifying/referencing permissions is required (but unsupported yet).
	 * 
	 * However, features which enable to modify/refer permission settings from other plug-ins are unsupported yet by 
	 * engine-connector interfaces (e.g.: {@link org.vcssl.connect.EngineConnectorInterface1 ECI1}. 
	 * Furthermore, it is unckear whether it will be supported in future. 
	 * This method is a kind of "mockup" of the above feature, at the present stage.
	 * 
	 * @param permissionName The name of the permission item to be gotten its value.
	 * @param getFromBase Specify true to get the value of base permission settings, 
	 *     or false to get the value of temporary permission settings
	 * @param calledByEngine Specify true when calling this method by the scripting engine.
	 * @return The value of the specified permission item.
	 * 
	 * @throws ConnectorException
	 *     Thrown when the requested action has been denied, or unsupported, especially when or "false" is specified to "calledByEngine".
	 *     Also, thrown when "false" is specified to "getFromBase" but the temporary permission settings does not exist.
	 */
	public abstract String getPermissionValue(String permissionName, boolean getFromBase, boolean calledByEngine)
			throws ConnectorException;


	/**
	 * Stores current temporary permission settings.
	 *
	 * Stores current temporary permission settings
	 * for restoring it later by {@link restoreTemporaryPermissionValues(boolean)} method,
	 * or overwriting permanent permission settings
	 * 
	 * This method will be called from script engines/applications, 
	 * and may be called from other plug-ins through engine-connector interfaces.
	 * In the latter case, the permission for modifying/referencing permissions is required (but unsupported yet).
	 * 
	 * However, features which enable to modify/refer permission settings from other plug-ins are unsupported yet by 
	 * engine-connector interfaces (e.g.: {@link org.vcssl.connect.EngineConnectorInterface1 ECI1}. 
	 * Furthermore, it is unckear whether it will be supported in future. 
	 * This method is a kind of "mockup" of the above feature, at the present stage.
	 * 
	 * @param storesToBase Specify true to store by overwriting base permission settings, 
	 *     or false to store temporary for recovering later by {@link restoreTemporaryPermissionValues(boolean)} method.
	 * @param calledByEngine Specify true when calling this method by the scripting engine.
	 * 
	 * @throws ConnectorException
	 *     Thrown when the requested action has been denied, or unsupported, especially when or "false" is specified to "calledByEngine".
	 *     Also, thrown when "false" is specified to "getFromBase" but the temporary permission settings does not exist.
	 */
	public abstract void storeTemporaryPermissionValues(boolean storesToBase, boolean calledByEngine)
			throws ConnectorException;


	/**
	 * Restores current temporary permission settings.
	 *
	 * This method will be called from script engines/applications, 
	 * and may be called from other plug-ins through engine-connector interfaces.
	 * In the latter case, the permission for modifying/referencing permissions is required (but unsupported yet).
	 * 
	 * However, features which enable to modify/refer permission settings from other plug-ins are unsupported yet by 
	 * engine-connector interfaces (e.g.: {@link org.vcssl.connect.EngineConnectorInterface1 ECI1}. 
	 * Furthermore, it is unckear whether it will be supported in future. 
	 * This method is a kind of "mockup" of the above feature, at the present stage.
	 * 
	 * @param restoresFromBase Specify true to restore by copying values from base permission settings,
	 *     or false to restore by loading settings stored temporary by {@link storeTemporaryPermissionValues(boolean)} method.
	 * @param calledByEngine Specify true when calling this method by the scripting engine.
	 * 
	 * @throws ConnectorException
	 *     Thrown when the requested action has been denied, or unsupported, especially when or "false" is specified to "calledByEngine".
	 *     Also, thrown when "false" is specified to "getFromBase" but the temporary permission settings does not exist.
	 */
	public abstract void restoreTemporaryPermissionValues(boolean restoresFromBase, boolean calledByEngine)
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
