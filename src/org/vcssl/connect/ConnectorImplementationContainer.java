/*
 * ==================================================
 * Connector Implementation Container
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2019-2022 by RINEARN
 * ==================================================
 */

package org.vcssl.connect;

/**
 * An object for storing the loaded result of {@link ConnectorImplementationLoader}.
 */
public class ConnectorImplementationContainer {

	/** Stores the loaded object implementing plug-in connector interfaces. */
	private Object connectorImplementation = null;

	/** Stores the type ID (abbreviated name) of the interface, implemented by the loaded plug-in. */
	private String interfaceTypeId = null;

	/** Stores the generation of the interface, implemented by the loaded plug-in. */
	private String interfacaGeneration = null;


	/**
	 * Creates an instance storing specified loaded results.
	 * 
	 * @param connectorImplementation The loaded object implementing plug-in connector interfaces.
	 * @param interfaceTypeId The type ID (abbreviated name) of the interface.
	 * @param interfaceGeneration The generation of the interface.
	 */
	public ConnectorImplementationContainer(
			Object connectorImplementation, String interfaceTypeId, String interfaceGeneration) {

		this.connectorImplementation = connectorImplementation;
		this.interfaceTypeId = interfaceTypeId;
		this.interfacaGeneration = interfaceGeneration;
	}


	/**
	 * Returns the loaded object implementing plug-in connector interfaces.
	 * 
	 * @return The loaded object implementing plug-in connector interfaces.
	 */
	public Object getConnectorImplementation() {
		return this.connectorImplementation;
	}


	/**
	 * Returns the type ID (abbreviated name) of the interface, implemented by the loaded plug-in.
	 * 
	 * @return The type ID (abbreviated name) of the interface.
	 */
	public String getInterfaceTypeId() {
		return this.interfaceTypeId;
	}


	/**
	 * Returns the generation of the interface, implemented by the loaded plug-in.
	 * 
	 * @return The generation of the interface.
	 */
	public String getInterfaceGeneration() {
		return this.interfacaGeneration;
	}
}
