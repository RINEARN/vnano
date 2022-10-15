/*
 * Copyright(C) 2019-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import java.util.Locale;
import java.util.Map;

import org.vcssl.connect.ConnectorException;
import org.vcssl.connect.EngineConnectorInterface1;
import org.vcssl.connect.PermissionAuthorizerConnectorInterface1;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.spec.ErrorMessage;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.OptionKey;

/**
 * The connector class to access the script engine of the Vnano from plug-ins.
 * In Vnano Engine, we call this kind of object as an "engine connector".
 */
public final class EngineConnector implements EngineConnectorInterface1 {

	// Don't create setters of fields of this class.
	// An instance of this class will be accessed from plug-ins, not only from the engine.
	// It may be cause of unexpected problems when the fields are freely modified from plug-ins.
	// To get an instance having the field you want, use "create*UpdatedInstance" methods.

	/** The Map (option map) storing names and values of options. */
	private final Map<String, Object> optionMap;

	/** The Map (permission map) storing names and values of permission items. */
	private final Map<String, String> permissionMap;

	/** The permission authorizer, which is an object for authorizing requests of permissions from plug-ins. */
	private final PermissionAuthorizerConnectorInterface1 permissionAuthorizer;


	/**
	 * Creates an instance of the engine connector, having no information.
	 * To add information, call "create*UpdatedInstance" methods
	 * which creates new instance having the specified information.
	 */
	public EngineConnector() {
		this.optionMap = null;
		this.permissionMap = null;
		this.permissionAuthorizer = null;
	}

	/**
	 * Creates an instance of an engine connector, having the specified information.
	 *
	 * @param optionMap The Map (option map) storing names and values of options.
	 * @param permissionMap The Map (permission map) storing names and values of permission items.
	 * @param permissionAuthorizer The permission authorizer.
	 */
	private EngineConnector(
			Map<String, Object> optionMap, Map<String, String> permissionMap,
			PermissionAuthorizerConnectorInterface1 permissionAuthorizer) {

		// Note: This constructor only sets references of fields.
		//       It doesn't update relationship between them (because it may throw exceptions).
		//
		//       For example, the permission authorizer shoud has reference to the permission map.
		//       Such ancillary settings will be performed by "create*UpdatedInstance" methods.
		//       This constructor will be used at only inside of this class,
		//       and "create*UpdatedInstance" methods will be used from the outside of this class.

		this.optionMap = optionMap;
		this.permissionMap = permissionMap;
		this.permissionAuthorizer = permissionAuthorizer;
	}


	/**
	 * Creates a copy of this instance having the specified option map.
	 *
	 * @param updatedOptionMap The Map (option map) storing names and values of options.
	 */
	public final EngineConnector createOptionMapUpdatedInstance(Map<String, Object> updatedOptionMap) {
		return new EngineConnector(updatedOptionMap, this.permissionMap, this.permissionAuthorizer);
	}


	/**
	 * Creates a copy of this instance having the specified permission map.
	 *
	 * @param updatedPermissionMap The Map (permission map) storing names and values of permission items.
	 */
	public final EngineConnector createPermissionMapUpdatedInstance(Map<String, String> updatedPermissionMap)
			throws VnanoException {

		EngineConnector updatedEngineConnector = new EngineConnector(this.optionMap, updatedPermissionMap, this.permissionAuthorizer);
		updatedEngineConnector.reflectPermissionSettings();
		return updatedEngineConnector;
	}


	/**
	 * Creates a copy of this instance having the specified permission authorizer.
	 *
	 * @param updatedPermissionAuthorizer The permission authorizer, which is an object for authorizing requests of permissions from plug-ins.
	 */
	public final EngineConnector createPermissionAuthorizerUpdatedInstance(PermissionAuthorizerConnectorInterface1 updatedPermissionAuthorizer)
			throws VnanoException {

		EngineConnector updatedEngineConnector = new EngineConnector(this.optionMap, this.permissionMap, updatedPermissionAuthorizer);
		updatedEngineConnector.reflectPermissionSettings();
		return updatedEngineConnector;
	}


	/**
	 * Refrects the current permission map to the permission authorizer.
	 *
	 * @throws VnanoException Thrown when invalid names/values of permission items have been detected.
	 */
	private final void reflectPermissionSettings() throws VnanoException {
		if (this.permissionAuthorizer != null) {
			try {
				this.permissionAuthorizer.setPermissionMap(permissionMap, true);
			} catch (ConnectorException e) {
				throw new VnanoException(
					ErrorType.PERMISSION_AUTHORIZER_PLUGIN_CRASHED,
					new String[] { this.permissionAuthorizer.getClass().getCanonicalName(), e.getMessage() }, e
				);
			}
		}
	}


	/**
	 * Checks whether the option is set.
	 *
	 * @param optionKey The name (key) of the option (option name) to be checked.
	 * @return Returns true if the specified option is set.
	 */
	@Override
	public final boolean hasOptionValue(String optionKey) {
		return this.optionMap.containsKey(optionKey);
	}


	/**
	 * Gets the value of the option.
	 *
	 * @param optionKey The name (key) of the option (option name).
	 * @return The option value.
	 */
	@Override
	public final Object getOptionValue(String optionName) {
		return this.optionMap.get(optionName);
	}


	/**
	 * Requests the specified permission.
	 * @param permissionName The name of the permission item to request.
	 * @param requester The plug-in requesting the permission.
	 * @param metaInformation The information to be notified to the user, especially when the permission is set to "ASK".
  	 * @throws ConnectorException Thrown when the requested permission has been denied.
	 */
	@Override
	public final void requestPermission(String permissionName, Object requester, Object metaInformation)
			throws ConnectorException {

		// Request to the permission authorizer plug-in.
		if (this.permissionAuthorizer != null) {

			// If the request will be allowed, nothing will occur.
			// If the request will be denied, a ConnectorException will be thrown.
			this.permissionAuthorizer.requestPermission(permissionName, requester, metaInformation);

		// If no permission authorizer plug-in is connecter: Error
		} else {
			String errorMessage = ErrorMessage.generateErrorMessage(
				ErrorType.NO_PERMISSION_AUTHORIZER_IS_CONNECTED, (Locale)this.optionMap.get(OptionKey.LOCALE)
			);
			throw new ConnectorException(errorMessage);
		}
	}


	/**
	 * Returns whether the other type of engine connector is available.
	 *
	 * @param engineConnectorClass The class of the engine connector you want to use.
	 * @return Returns true if the specified engine connector is available.
	 */
	@Override
	public boolean isOtherEngineConnectorAvailable(Class<?> engineConnectorClass) {
		return engineConnectorClass == this.getClass()
				|| engineConnectorClass == EngineConnectorInterface1.class;
	}


	/**
	 * Returns the other type of engine connector.
	 *
	 * @param engineConnectorClass The class of the engine connector you want to use.
	 * @return The specified type of engine connector.
	 */
	@Override
	public <T> T getOtherEngineConnector(Class<T> engineConnectorClass) {
		if (this.isOtherEngineConnectorAvailable(engineConnectorClass)) {
			return engineConnectorClass.cast(this);
		} else {
			return null;
		}
	}

}
