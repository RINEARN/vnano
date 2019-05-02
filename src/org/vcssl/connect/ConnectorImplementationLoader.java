/*
 * ==================================================
 * Connector Implementation Loader
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2019 by RINEARN (Fumihiro Matsui)
 * ==================================================
 */

package org.vcssl.connect;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class ConnectorImplementationLoader {

	private static final String[] DEFAULT_LOADING_PATHS = { "." };
	private static final String INTERFACE_TYPE_FIELD_NAME = "INTERFACE_TYPE";
	private static final String INTERFACE_GENERATION_FIELD_NAME = "INTERFACE_GENERATION";

	private ClassLoader classLoader = null;

	public ConnectorImplementationLoader() {
		this.classLoader = null;
	}

	public ConnectorImplementationLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	private void initializeDefaultClassLoader() throws MalformedURLException {
		this.initializeDefaultClassLoader(DEFAULT_LOADING_PATHS);
	}

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

	public ConnectorImplementationContainer load(String connectorImplementationName)
			throws ConnectorException {

		if (this.classLoader == null) {
			try {
				this.initializeDefaultClassLoader();
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


		String interfaceType = null;
		String interfaceGeneration = null;
		try {
			Field interfaceTypeField = connectorImplClass.getField(INTERFACE_TYPE_FIELD_NAME);
			Field interfaceVersionField = connectorImplClass.getField(INTERFACE_GENERATION_FIELD_NAME);
			interfaceType = interfaceTypeField.get(null).toString();
			interfaceGeneration = interfaceVersionField.get(null).toString();

		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {

			if (connectorImplInstance instanceof GeneralProcessConnector2) {
				interfaceType = "GPCI";
				interfaceGeneration = "2";
			} else if (connectorImplInstance instanceof GeneralProcessConnector1) {
				interfaceType = "GPCI";
				interfaceGeneration = "1";
			} else {
				throw new ConnectorException(
					"Invalid implementation (unknown interface): "
					+ connectorImplementationName
					+ " (interface-type: " + interfaceType + ", "
					+ "interface-generation: " + interfaceGeneration + ")", e
				);
			}
		}

		ConnectorImplementationContainer container = new ConnectorImplementationContainer(
			connectorImplInstance, interfaceType, interfaceGeneration
		);

		this.checkImplementation(container, connectorImplementationName);

		return container;
	}

	private void checkImplementation(
			ConnectorImplementationContainer container, String connectorImplementationName)
					throws ConnectorException {

		String interfaceType = container.getInterfaceType();
		String interfaceGeneration = container.getInterfaceGeneration();
		if (interfaceType == null) {
			throw new ConnectorException(
				"Invalid implementation (null interface-type): " + connectorImplementationName
			);
		}
		if (interfaceGeneration == null) {
			throw new ConnectorException(
				"Invalid implementation (null interface-generation): " + connectorImplementationName
			);
		}

		Object implementation = container.getConnectorImplementation();
		String interfaceCode = container.getInterfaceType() + container.getInterfaceGeneration();

		switch (interfaceCode) {

			case "XFCI1" : {
				if ( !(implementation instanceof ExternalFunctionConnector1) ) {
					throw new ConnectorException(
						"Invalid implementation"
						+ " (should implement org.vcssl.connect.ExternalFunctionConnector1): "
						+ connectorImplementationName
					);
				}
				break;
			}

			case "XVCI1" : {
				if ( !(implementation instanceof ExternalVariableConnector1) ) {
					throw new ConnectorException(
						"Invalid implementation"
						+ " (should implement org.vcssl.connect.ExternalVariableConnector1): "
						+ connectorImplementationName
					);
				}
				break;
			}

			case "GPCI1" : {
				if ( !(implementation instanceof GeneralProcessConnector1) ) {
					throw new ConnectorException(
						"Invalid implementation"
						+ " (should implement org.vcssl.connect.GeneralProcessConnector1): "
						+ connectorImplementationName
					);
				}
				break;
			}

			case "GPCI2" : {
				if ( !(implementation instanceof GeneralProcessConnector2) ) {
					throw new ConnectorException(
						"Invalid implementation"
						+ " (should implement org.vcssl.connect.GeneralProcessConnector2): "
						+ connectorImplementationName
					);
				}
				break;
			}

			case "GPCI3" : {
				if ( !(implementation instanceof GeneralProcessConnector3) ) {
					throw new ConnectorException(
						"Invalid implementation"
						+ " (should implement org.vcssl.connect.GeneralProcessConnector3): "
						+ connectorImplementationName
					);
				}
				break;
			}

			default : {
				throw new ConnectorException(
					"Invalid implementation (unsupported interface): "
					+ connectorImplementationName
					+ " (interface-type: " + interfaceType + ", "
					+ "interface-generation: " + interfaceGeneration + ")"
				);
			}

		} // end of switch

	} // end of method

}
