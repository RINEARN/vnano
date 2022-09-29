/*
 * ==================================================
 * Connector Implementation Loader
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2019-2022 by RINEARN
 * ==================================================
 */

package org.vcssl.connect;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * A class for loading plug-in objects, implementing plug-in connector interfaces provided in this package.
 */
public class ConnectorImplementationLoader {

	/** The default path from which plug-ins will be loaded. */
	private static final String[] DEFAULT_LOADING_PATHS = { "." };

	/** The name of the field declaring the type ID of the interface. */
	private static final String INTERFACE_TYPE_ID_FIELD_NAME = "INTERFACE_TYPE_ID";

	/** The name of the field declaring the generation of the interface. */
	private static final String INTERFACE_GENERATION_FIELD_NAME = "INTERFACE_GENERATION";

	/** The ClassLoader for loading classes of plug-ins. */
	private ClassLoader classLoader = null;

	/** Stores whether filter out loaded plug-ins in which no interface type name is declared. */
	private boolean interfaceFilterEnabled = false;


	/**
	 * Creates an new loader.
	 */
	public ConnectorImplementationLoader() {
		this.classLoader = null;
	}


	/**
	 * Creates an new loader using the specified ClassLoader.
	 * 
	 * @param classLoader The ClassLoader for loading classes of plug-ins.
	 */
	public ConnectorImplementationLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}


	/**
	 * Initializes the ClassLoader by default procedures.
	 * 
	 * @param directoryPaths An array storing path of directories from which plug-in will be loaded.
	 * @throws MalformedURLException Thrown when paths in "directoryPaths" are invalid.
	 */
	private void initializeDefaultClassLoader(String[] directoryPaths) throws MalformedURLException {
		int directoryLength = directoryPaths.length;
		URL directoryURLs[] = new URL[directoryLength];
		for (int directoryIndex=0; directoryIndex<directoryLength; directoryIndex++) {
			File directoryFile = new File(directoryPaths[directoryIndex]);
			URL directoryURL = directoryFile.toURI().toURL();
			directoryURLs[directoryIndex] = directoryURL;
		}
		this.classLoader = new URLClassLoader(directoryURLs);
	}


	/**
	 * Sets whether filter out plug-ins in which no interface type name is declared.
	 * 
	 * @param enabled Specify true for filtering out.
	 */
	public void setInterfaceFilterEnabled(boolean enabled) {
		this.interfaceFilterEnabled = enabled;
	}


	/**
	 * Returns whether filter out plug-ins in which no interface type name is declared.
	 * 
	 * @return Returns true it it will be filtered out.
	 */
	public boolean isInterfaceFilterEnabled() {
		return this.interfaceFilterEnabled;
	}

	/**
	 * Loads the plug-in class and instantiate it.
	 * 
	 * @param connectorImplementationName 
	 *     The name of the plug-in class to be loaded, which plug-in connector interfaces provided in this package.
	 * 
	 * @return
	 *     An object storing the loaded plug-in and information of implemented the interface.
	 * 
	 * @throws ConnectorException
	 *     Thrown when the loading has failed, or the loaded plug-in has been filtered out.
	 */
	public ConnectorImplementationContainer load(String connectorImplementationName)
			throws ConnectorException {

		if (this.classLoader == null) {
			try {
				this.initializeDefaultClassLoader(DEFAULT_LOADING_PATHS);
			} catch (MalformedURLException e) {
				throw new ConnectorException(
					"ClassLoader initialization failed.", e
				);
			}
		}

		Class<?> connectorImplClass = null;
		try {
			connectorImplClass = this.classLoader.loadClass(connectorImplementationName);
		} catch (ClassNotFoundException e) {
			throw new ConnectorException(
				"Loading failed: " + connectorImplementationName, e
			);
		}

		Object connectorImplInstance = null;
		try {
			connectorImplInstance = connectorImplClass.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {

			throw new ConnectorException(
				"Instantiation failed: " + connectorImplementationName, e
			);
		}

		String interfaceTypeId = null;
		String interfaceGeneration = null;
		try {
			Field interfaceTypeIdField = connectorImplClass.getField(INTERFACE_TYPE_ID_FIELD_NAME);
			Field interfaceVersionField = connectorImplClass.getField(INTERFACE_GENERATION_FIELD_NAME);
			interfaceTypeId = interfaceTypeIdField.get(null).toString();
			interfaceGeneration = interfaceVersionField.get(null).toString();

		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {

			if (connectorImplInstance instanceof GeneralProcessConnectorInterface2) {
				interfaceTypeId = "GPCI";
				interfaceGeneration = "2";
			} else if (connectorImplInstance instanceof GeneralProcessConnectorInterface1) {
				interfaceTypeId = "GPCI";
				interfaceGeneration = "1";
			} else {
				if (this.interfaceFilterEnabled) {
					throw new ConnectorException(
						"Invalid implementation (unknown interface): "
						+ connectorImplementationName
						+ " (interface-type-id: " + interfaceTypeId + ", "
						+ "interface-generation: " + interfaceGeneration + ")", e
					);
				}
			}
		}

		ConnectorImplementationContainer container = new ConnectorImplementationContainer(
			connectorImplInstance, interfaceTypeId, interfaceGeneration
		);

		this.checkImplementation(container, connectorImplementationName);

		return container;
	}


	/**
	 * Checks whether the loaded result is valid.
	 * 
	 * @param container
	 *     An object storing the loaded plug-in and information of implemented the interface.
	 * 
	 * @param connectorImplementationName
	 *     The name of the loaded plug-in class.
	 * 
	 * @throws ConnectorException
	 *     Thrown when the loaded result is invalid.
	 */
	private void checkImplementation(
			ConnectorImplementationContainer container, String connectorImplementationName)
					throws ConnectorException {

		String interfaceTypeId = container.getInterfaceTypeId();
		String interfaceGeneration = container.getInterfaceGeneration();
		if (interfaceTypeId == null) {
			if (!this.interfaceFilterEnabled) {
				return;
			}
			throw new ConnectorException(
				"Invalid implementation (null interface-type-id): " + connectorImplementationName
			);
		}
		if (interfaceGeneration == null) {
			throw new ConnectorException(
				"Invalid implementation (null interface-generation): " + connectorImplementationName
			);
		}

		Object implementation = container.getConnectorImplementation();
		String interfaceCode = container.getInterfaceTypeId() + container.getInterfaceGeneration();

		switch (interfaceCode) {

			case "XFCI1" : {
				if ( !(implementation instanceof ExternalFunctionConnectorInterface1) ) {
					throw new ConnectorException(
						"Invalid implementation"
						+ " (should implement org.vcssl.connect.ExternalFunctionConnectorInterface1): "
						+ connectorImplementationName
					);
				}
				break;
			}

			case "XVCI1" : {
				if ( !(implementation instanceof ExternalVariableConnectorInterface1) ) {
					throw new ConnectorException(
						"Invalid implementation"
						+ " (should implement org.vcssl.connect.ExternalVariableConnectorInterface1): "
						+ connectorImplementationName
					);
				}
				break;
			}

			case "GPCI1" : {
				if ( !(implementation instanceof GeneralProcessConnectorInterface1) ) {
					throw new ConnectorException(
						"Invalid implementation"
						+ " (should implement org.vcssl.connect.GeneralProcessConnectorInterface1): "
						+ connectorImplementationName
					);
				}
				break;
			}

			case "GPCI2" : {
				if ( !(implementation instanceof GeneralProcessConnectorInterface2) ) {
					throw new ConnectorException(
						"Invalid implementation"
						+ " (should implement org.vcssl.connect.GeneralProcessConnectorInterface2): "
						+ connectorImplementationName
					);
				}
				break;
			}

			case "GPCI3" : {
				if ( !(implementation instanceof GeneralProcessConnectorInterface3) ) {
					throw new ConnectorException(
						"Invalid implementation"
						+ " (should implement org.vcssl.connect.GeneralProcessConnectorInterface3): "
						+ connectorImplementationName
					);
				}
				break;
			}

			default : {
				if (this.interfaceFilterEnabled) {
					throw new ConnectorException(
						"Invalid implementation (unsupported interface): "
						+ connectorImplementationName
						+ " (interface-type-id: " + interfaceTypeId + ", "
						+ "interface-generation: " + interfaceGeneration + ")"
					);
				}
			}

		} // end of switch

	} // end of method

}
