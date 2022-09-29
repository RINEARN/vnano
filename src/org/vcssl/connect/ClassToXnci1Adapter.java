/*
 * ==================================================
 * Class to XLCI Plug-in Adapter
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2019-2022 by RINEARN
 * ==================================================
 */

package org.vcssl.connect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * An adapter class converting a host-language-side class to a 
 * {@link org.vcssl.connect.ExternalNamespaceConnectorInterface1 XNCI 1} plug-in, 
 * to access to its fields and methods from scripts.
 */
public class ClassToXnci1Adapter implements ExternalNamespaceConnectorInterface1 {

	/** The class to be accessed from scripts. */
	private Class<?> pluginClass = null;

	/** The instance of "pluginClass" class. */
	private Object pluginInstance = null;


	/** Creates a new adapter to access to all static and non-static fields/methods of the class.
	 * 
	 * @param pluginClass The class to which fields/methods to be accessed belongs to.
	 * @param pluginInstance The instance of the class specified to the argument "pluginClass".
	 */
	public ClassToXnci1Adapter(Class<?> pluginClass, Object pluginInstance) {
		this.pluginClass = pluginClass;
		this.pluginInstance = pluginInstance;
	}


	/**
	 * Creates a new adapter to access to all static fields/methods of the class.
	 * 
	 * @param pluginClass The class to which fields/methods to be accessed belongs to.
	 */
	public ClassToXnci1Adapter(Class<?> pluginClass) {
		this.pluginClass = pluginClass;
	}


	@Override
	public String getNamespaceName() {
		return null;
	}

	@Override
	public boolean isMandatoryToAccessMembers() {
		return false;
	}

	@Override
	public ExternalFunctionConnectorInterface1[] getFunctions() {

		// Gets all methods.
		Method[] methods = this.pluginClass.getDeclaredMethods();

		// The list storing adapters converting above methods to XFCI1 plug-ins.
		List<ExternalFunctionConnectorInterface1> xfciList = new ArrayList<ExternalFunctionConnectorInterface1>();

		// Converts each method to XFCI1 a plug-in, and put it into the list.
		for (Method method: methods) {
			int modifiers = method.getModifiers();

			// Skip non-public methods because it can not be accessed from the script engine.
			if (!Modifier.isPublic(modifiers)) {
				continue;
			}

			// If the method is static, always converts it.
			if (Modifier.isStatic(modifiers)) {
				xfciList.add(new MethodToXfci1Adapter(method));

			// If the method is non-static, converts it only when this adapter has an object instance of the class
			// (It can be specified as an argument of the constructor).
			} else if (this.pluginInstance != null) {
				xfciList.add(new MethodToXfci1Adapter(method, this.pluginInstance));
			}
		}
		return xfciList.toArray(new ExternalFunctionConnectorInterface1[0]);
	}

	@Override
	public ExternalVariableConnectorInterface1[] getVariables() {

		// Gets all fields.
		Field[] fields = this.pluginClass.getDeclaredFields();

		// The list storing adapters converting above fields to XVCI1 plug-ins.
		List<ExternalVariableConnectorInterface1> xvciList = new ArrayList<ExternalVariableConnectorInterface1>();

		// Converts each field to XVCI1 a plug-in, and put it into the list.
		for (Field field: fields) {
			int modifiers = field.getModifiers();

			// Skip non-public fields because it can not be accessed from the script engine.
			if (!Modifier.isPublic(modifiers)) {
				continue;
			}

			// If the field is static, always converts it.
			if (Modifier.isStatic(modifiers)) {
				xvciList.add(new FieldToXvci1Adapter(field));

			// If the field is non-static, converts it only when this adapter has an object instance of the class
			// (It can be specified as an argument of the constructor).
			} else if (this.pluginInstance != null) {
				xvciList.add(new FieldToXvci1Adapter(field, this.pluginInstance));
			}
		}
		return xvciList.toArray(new ExternalVariableConnectorInterface1[0]);
	}

	@Override
	public ExternalStructConnectorInterface1[] getStructs() {
		return new ExternalStructConnectorInterface1[0];
	}

	@Override
	public Class<?> getEngineConnectorClass() {
		return EngineConnectorInterface1.class;
	}

	@Override
	public void preInitializeForConnection(Object engineConnector) throws ConnectorException {
	}

	@Override
	public void postInitializeForConnection(Object engineConnector) throws ConnectorException {
	}

	@Override
	public void preFinalizeForDisconnection(Object engineConnector) throws ConnectorException {
	}

	@Override
	public void postFinalizeForDisconnection(Object engineConnector) throws ConnectorException {
	}

	@Override
	public void preInitializeForExecution(Object engineConnector) throws ConnectorException {
	}

	@Override
	public void postInitializeForExecution(Object engineConnector) throws ConnectorException {
	}

	@Override
	public void preFinalizeForTermination(Object engineConnector) throws ConnectorException {
	}

	@Override
	public void postFinalizeForTermination(Object engineConnector) throws ConnectorException {
	}
}
