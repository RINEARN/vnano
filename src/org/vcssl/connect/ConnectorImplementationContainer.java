/*
 * ==================================================
 * Connector Implementation Container
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2019 by RINEARN (Fumihiro Matsui)
 * ==================================================
 */

package org.vcssl.connect;

public class ConnectorImplementationContainer {
	private Object connectorImplementation = null;
	private String interfaceType = null;
	private String interfacaGeneration = null;

	public ConnectorImplementationContainer(
			Object connectorImplementation, String interfaceType, String interfaceGeneration) {

		this.connectorImplementation = connectorImplementation;
		this.interfaceType = interfaceType;
		this.interfacaGeneration = interfaceGeneration;
	}

	public Object getConnectorImplementation() {
		return this.connectorImplementation;
	}

	public String getInterfaceType() {
		return this.interfaceType;
	}

	public String getInterfaceGeneration() {
		return this.interfacaGeneration;
	}
}
