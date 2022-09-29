/*
 * ==================================================
 * Engine Connector Interface 1
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2018-2022 by RINEARN
 * ==================================================
 */

// THE SPECIFICATION OF THIS INTERFACE HAD BEEN FINALIZED AT 2022/08/31.
// NO MODIFICATIONS WILL BE APPLIED FOR THIS INTERFACE, EXCLUDING DOCUMENTS/COMMENTS.

package org.vcssl.connect;


/**
 * An interface for mediate communication of some information between scripting engines and plug-ins.
 * 
 * Scripting engines pass an object implementing this interface to arguments 
 * of initialization/finalization methods of plug-ins. 
 */
public interface EngineConnectorInterface1 {

	/** The type ID of this interface (value: "ECI") referred when the plug-in will be loaded. */
	public static final String INTERFACE_TYPE_ID = "ECI";

	/** The generation of this interface (value: "1"). */
	public static final String INTERFACE_GENERATION = "1";


	/**
	 * Returns whether the engine has the value of the option with the specified name.
	 * 
	 * @param optionName The name of the option.
	 * @return Returns true if the engine has the value of the specified option.
	 */
	public abstract boolean hasOptionValue(String optionName);


	/**
	 * Gets the value of the specified option.
	 * 
	 * Before calling this method, check that the value of the specified option exists and is accessible, 
	 * by {@link EngineConnectorInterface1#hasOptionValue(String)} method.
	 * 
	 * @param optionName The name of the option.
	 * @return The value of the specified option.
	 * @Throws ConnectorFatalException
	 *    (Unchecked) Thrown when the value of the specified option does not exist, or is not accessible.
	 */
	public abstract Object getOptionValue(String optionName);


	/**
	 * Requests the specified permission.
	 * 
	 * If the requested permission is allowed, nothing will occur. On the other hand, 
	 * when the requested permission has been denied, this method throws a ConnectorException.
	 * Whether the request will be allowed or denied depends on 
	 * settings of an application, or decision of its user.
	 * Hence, in principle, it is necessary to assume that any request may be denied. 
	 * So it requires to catch (or rethrow) the exception explicitly.
	 * 
	 * @param permissionName The name of the permission item to request.
	 * @param requester The plug-in requesting the permission.
	 * @param metaInformation The information to be notified to the user 
	 *         (especially when the current value of the permission is set to {@link ConnectorPermissionValue#ASK ASK}).
	 * 
	 * @throws ConnectorException Thrown when the requested permission has been denied.
	 */
	public abstract void requestPermission(String permissionName, Object requester, Object metaInformation)
			throws ConnectorException;


	/**
	 * Returns whether the other type of engine connector is available.
	 * 
	 * @param engineConnectorClass The class of the engine connector you want to use.
	 * @return Returns true if the specified engine connector is available.
	 */
	public abstract boolean isOtherEngineConnectorAvailable(Class<?> engineConnectorClass);


	/**
	 * Gets the other type of engine connector.
	 * 
	 * Before calling this method, check that the specified type of the engine connector is available, by 
	 * [@link EngineConnectorInterface1#isOtherEngineConnectorAvailable(Class<?>)} method.
	 * 
	 * @param engineConnectorClass The class of the engine connector you want to use.
	 * @return The specified type of engine connector.
	 * 
	 * @Throws ConnectorFatalException 
	 *     (Unchecked) Thrown if the specified engine connector is not available.
	 */
	public abstract <T> T getOtherEngineConnector(Class<T> engineConnectorClass);
}
