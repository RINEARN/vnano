/*
 * Copyright(C) 2017-2024 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.HashSet;

import org.vcssl.connect.ClassToXnci1Adapter;
import org.vcssl.connect.ConnectorException;
import org.vcssl.connect.ConnectorPermissionName;
import org.vcssl.connect.ConnectorPermissionValue;
import org.vcssl.connect.ExternalFunctionConnectorInterface1;
import org.vcssl.connect.ExternalNamespaceConnectorInterface1;
import org.vcssl.connect.ExternalVariableConnectorInterface1;
import org.vcssl.connect.FieldToXvci1Adapter;
import org.vcssl.connect.MethodToXfci1Adapter;
import org.vcssl.connect.PermissionAuthorizerConnectorInterface1;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.IdentifierSyntax;
import org.vcssl.nano.spec.OptionValue;
import org.vcssl.nano.spec.SpecialBindingKey;
import org.vcssl.nano.vm.VirtualMachineObjectCode;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.VnanoFatalException;

/**
 * The class performing functions to manage and to provide some information
 * shared between multiple components in the script engine of the Vnano.
 *
 * For example, information to resolve references of variables and functions
 * are managed by this class in the script engine.
 * Also, calling of external functions and synchronization of data of external variables
 * are taken through this class.
 * This class supports various types of plug-in connection interfaces,
 * and plug-ins are internally connected to this class in the script engine.
 * This kind of object is referred as "Interconnect" in the script engine of Vnano.
 */
public class Interconnect {

	/** The table storing information of external functions. */
	private FunctionTable externalFunctionTable = null;

	/** The table storing information of external vaiables. */
	private VariableTable externalVariableTable = null;

	/** The engine connector, which is an object for providing information of the script engine to plug-ins. */
	private EngineConnector engineConnector = null;

	/** The permission authorizer, which is a special plug-in for authorizing permission-requests from other plug-ins. */
	private PermissionAuthorizerConnectorInterface1 permissionAuthorizer = null;

	/** The List storing all XNCI1 plug-ins. */
	private List<ExternalNamespaceConnectorInterface1> xnci1PluginList = null;

	/** The List storing all XFCI1 plug-ins. */
	private List<ExternalFunctionConnectorInterface1> xfci1PluginList = null;

	/** The List storing all XVCI1 plug-ins. */
	private List<ExternalVariableConnectorInterface1> xvci1PluginList = null;

	/** A map to store all names and values of option items. */
	private Map<String, Object> optionMap = null;

	/** A map to store all names and values of permission items. */
	private Map<String, String> permissionMap = null;

	/** Stores contents of library scripts, with their file paths as keys. */
	private Map<String, String> libraryFilePathContentMap = null;

	/** Stores the name of the main script. */
	private String mainScriptName = null;

	/** Stores the content of the main script. */
	private String mainScriptContent = null;

	/** Stores "import paths" of libraries, which can be specified as values of "import" declarations. */
	private Set<String> libraryImportPathSet;

	/** Stores "import paths" of namespaces provided by plug-ins, which can be specified as values of "import" declarations. */
	private Set<String> pluginImportPathSet;


	/**
	 * Creates a blank interconnect to which nothing are connected.
	 */
	public Interconnect() {
		this.engineConnector = new EngineConnector();
		this.externalFunctionTable = new FunctionTable();
		this.externalVariableTable = new VariableTable();
		this.xnci1PluginList = new ArrayList<ExternalNamespaceConnectorInterface1>();
		this.xfci1PluginList = new ArrayList<ExternalFunctionConnectorInterface1>();
		this.xvci1PluginList = new ArrayList<ExternalVariableConnectorInterface1>();
		this.libraryFilePathContentMap = new LinkedHashMap<String, String>();
		this.libraryImportPathSet = new HashSet<String>();
		this.pluginImportPathSet = new HashSet<String>();


		// Create an option map and set default values, and reflect to the engine connector.
		this.optionMap = new LinkedHashMap<String, Object>();
		this.optionMap = OptionValue.normalizeValuesOf(optionMap);
		this.engineConnector = this.engineConnector.createOptionMapUpdatedInstance(this.optionMap);

		// Create an empty permission map and set "DENY" to the default value, and reflect to the engine connector.
		this.permissionMap = new LinkedHashMap<String, String>();
		this.permissionMap.put(ConnectorPermissionName.DEFAULT, ConnectorPermissionValue.DENY);
		try {
			this.engineConnector = this.engineConnector.createPermissionMapUpdatedInstance(this.permissionMap);
		} catch (VnanoException vne) {
			// A permission authorizer plug-in should not be loaded yet, so the above process should not throw any exception.
			// If it is thrown, there is unexpected (incorrect) something in the implementation.
			throw new VnanoFatalException("Unexpected exception occurred", vne);
		}
	}


	/**
	 * Turns to the active state on which scripts are executable, with initializing all connected plug-ins.
	 */
	public void activate() throws VnanoException {
		this.initializeAllPluginsForExecution();
	}


	/**
	 * Turns to the idle state (on which scripts are not executable), with finalizing all connected plug-ins.
	 */
	public void deactivate() throws VnanoException {
		this.finalizeAllPluginsForTermination();
	}


	/**
	 * Sets options, by a Map (option map) storing names and values of options you want to set.
	 *
	 * Type of the option map is Map<String,Object>, and its keys represents option names.
	 * For details of option names and values,
	 * see {@link org.vcssl.nano.spec.OptionKey} and {@link org.vcssl.nano.spec.OptionValue}.
	 *
	 * @param optionMap A Map (option map) storing names and values of options.
	 * @throws VnanoException Thrown if invalid option settings is detected.
	 */
	public void setOptionMap(Map<String, Object> optionMap) throws VnanoException {

		// Supplement some option items by default values, and store the map to the field of this class.
		this.optionMap = OptionValue.normalizeValuesOf(optionMap);

		// Check the content of option settings.
		OptionValue.checkContentsOf(this.optionMap);

		// Reflect to the engine connector, because option values may be referred from plug-ins.
		this.engineConnector = this.engineConnector.createOptionMapUpdatedInstance(this.optionMap);
	}


	/**
	 * Gets the Map (option map) storing names and values of options.
	 *
	 * Type of the option map is Map<String,Object>, and its keys represents option names.
	 * For details of option names and values,
	 * see {@link org.vcssl.nano.spec.OptionKey} and {@link org.vcssl.nano.spec.OptionValue}.
	 *
	 * @return A Map (option map) storing names and values of options.
	 */
	public Map<String, Object> getOptionMap() {
		return this.optionMap;
	}


	/**
	 * Sets permissions, by a Map (permission map) storing names and values of permission items you want to set.
	 *
	 * Type of the permission map is Map<String,String>, and its keys represents names of permission items.
	 * For details of names and values of permission items,
	 * see {@link org.vcssl.connect.ConnectorPermissionName} and {@link org.vcssl.connect.ConnectorPermissionValue}.
	 *
	 * @param permissionMap A Map (permission map) storing names and values of permission items.
	 * @throws VnanoException Thrown if invalid permission settings is detected.
	 */
	public void setPermissionMap(Map<String, String> permissionMap) throws VnanoException {
		this.permissionMap = permissionMap;

		// Reflect to the engine connector, because permission values will be referred from plug-ins.
		this.engineConnector = this.engineConnector.createPermissionMapUpdatedInstance(permissionMap);
	}


	/**
	 * Gets the Map (permission map) storing names and values of permission items.
	 *
	 * Type of the permission map is Map<String,String>, and its keys represents names of permission items.
	 * For details of names and values of permission items,
	 * see {@link org.vcssl.connect.ConnectorPermissionName} and {@link org.vcssl.connect.ConnectorPermissionValue}.
	 *
	 * @return A Map (permission map) storing names and values of permission items.
	 */
	public Map<String, String> getPermissionMap() {
		return this.permissionMap;
	}


	/**
	 * Returns the table storing information of external functions (external function table).
	 *
	 * @return The external function table.
	 */
	public FunctionTable getExternalFunctionTable() {
		return this.externalFunctionTable;
	}


	/**
	 * Returns the table storing information of external variable (external variable table).
	 *
	 * @return The external variable table.
	 */
	public VariableTable getExternalVariableTable() {
		return this.externalVariableTable;
	}


	/**
	 * Calls the external function registered at the element with specified index in the external function table.
	 *
	 * @param functionIndex The index of the extenral function to be called.
	 * @param returnData Data container storing returned value from the callee function.
	 * @param arguments Data containers storing arguments to be passed to the callee function.
	 */
	public void callExternalFunction(int functionIndex, DataContainer<?> returnData, DataContainer<?>[] arguments)
			throws VnanoException {

		this.externalFunctionTable.getFunctionByIndex(functionIndex).invoke(returnData, arguments);
	}


	/**
	 * Writebacks data to external variables from the virtual memory of VM.
	 * This method will be used for synchronization of data after execution of scripts.
	 *
	 * @param memory The virtual memory which was used for execution of the script.
	 */
	public void writebackExternalVariables(Memory memory, VirtualMachineObjectCode intermediateCode)
			throws VnanoException {

		// Write-back values of external (global) variables.
		int maxGlobalAddress = intermediateCode.getMaximumGlobalAddress();
		int minGlobalAddress = intermediateCode.getMinimumGlobalAddress();
		for (int address=minGlobalAddress; address<=maxGlobalAddress; address++) {

			// Skip if the address of the variable is not accessed from the script.
			if (!intermediateCode.hasGlobalVariableRegisteredAt(address)) {
				continue;
			}

			// Get the data container corresponding with the address of the variable.
			DataContainer<?> dataContainer = memory.getDataContainer(Memory.Partition.GLOBAL, address);

			// Get the unique identifier of the variable, from the address.
			String identifier = intermediateCode.getGlobalVariableUniqueIdentifier(address);

			// Get the external variable object, from the unique identifier.
			AbstractVariable variable = this.externalVariableTable.getVariableByAssemblyIdentifier(identifier);

			// Skip if the value of the variable is unmodifiable.
			if (variable.isConstant()) {
				continue;
			}

			// Update the value of the external variable object.
			variable.setDataContainer(dataContainer);
		}
	}


	/**
	 * Connects various types of plug-ins which provides external functions/variables.
	 *
	 * @param bindingName
	 *   A name in scripts of the variable/function/namespace provided by the connected plug-in.
	 *   If the passed argument contains a white space or a character "(", the content after it will be ignored.
	 *   By the above specification, for a function plug-in,
	 *   you can specify a signature containing parameter-declarations like "foo(int,float)"
	 *   (note that, syntax or correctness of parameter-declarations will not be checked).
	 *   In addition, for plug-ins providing elements belonging to the same namespace "Bar",
	 *   you can specify "Bar 1", "Bar 2", and so on.
	 *   This is helpful to avoid the duplication of keys when you use
	 *   {@link org.vcssl.nano.VnanoScriptEngine#put(String, Object) VnanoScriptEngine.put(String, Object) }
	 *   method which wraps this method.
	 *   Also, you can specify "___VNANO_AUTO_KEY" for using a valid value generated automatically.
	 *
	 * @param plugin
	 *   The plug-in to be connected.
	 *   General "Object" type instances can be connected as a plug-in,
	 *   for accessing their methods/fields from the script code as external functions/variables.
	 *   For accessing only static methods and fields, "Class&lt;T&gt;" type instance can also be connected.
	 *   In addition, if you want to choose a method/field to be accessible from script code,
	 *   a "Method"/"Field" type instance can be connected.
	 *   ( In that case, if the method/field is static, pass an Object type array for this argument,
	 *     and store the Method/Field type instance at [0],
	 *     and store "Class&lt;T&gt;" type instance of the class defining the method/field at [1] ).
	 *   Furthermore, the instance of the class implementing
	 *   {@link org.vcssl.connect.ExternalFunctionConnectorInterface1 XFCI1} /
	 *   {@link org.vcssl.connect.ExternalVariableConnectorInterface1 XVCI1} /
	 *   {@link org.vcssl.connect.ExternalNamespaceConnectorInterface1 XNCI1}
	 *   type less-overhead plug-in interface can be connected.
	 *   Also, this method is used for connecting
	 *   {@link org.vcssl.connect.PermissionAuthorizerConnectorInterface1 PACI1}
	 *   type plug-ins which is used for managing permissions (permission authorizer).
	 *
	 * @throws VnanoException
	 *   Thrown if the plug-in could not be connected,
	 *   caused by unsupported interfaces, incompatibility of data types, and so on.
	 */
	public void connectPlugin(String bindingName, Object plugin) throws VnanoException {

		// In error messages, display the binding name for a Field, Method, and so on.
		// For a plug-in implementing special interfaces (e.g.: XFCI1), display the name of the plug-in class.
		String nameInErrorMessage = bindingName;

		try {
			// Replace the binding name with auto-generated one if, it it is requested.
			if (bindingName.equals(SpecialBindingKey.AUTO_KEY)) {
				bindingName = this.generateBindingNameOf(plugin);
				nameInErrorMessage = bindingName;

				// The plug-in is has not been initialized yet in this step,
				// but its name should be determined before the initialization, for XFCI1/XVCI1/XNCI1.
			}

			// Remove after a white space or "(" in the binding name.
			bindingName = bindingName.split("\\s|\\(")[0];

			// PACI1 type security plug-in:
			if (plugin instanceof PermissionAuthorizerConnectorInterface1) {
				nameInErrorMessage = plugin.getClass().getName();
				this.connectPaci1Plugin( (PermissionAuthorizerConnectorInterface1)plugin );

			// XVCI1 type variable plug-in:
			} else if (plugin instanceof ExternalVariableConnectorInterface1) {
				nameInErrorMessage = plugin.getClass().getName();
				this.connectXvci1Plugin( (ExternalVariableConnectorInterface1)plugin, true, bindingName, false, null );

			// XFCI1 type function plug-in:
			} else if (plugin instanceof ExternalFunctionConnectorInterface1) {
				nameInErrorMessage = plugin.getClass().getName();
				this.connectXfci1Plugin( (ExternalFunctionConnectorInterface1)plugin, true, bindingName, false, null);

			// XNCI1 type namespace plug-in:
			} else if (plugin instanceof ExternalNamespaceConnectorInterface1) {
				nameInErrorMessage = plugin.getClass().getName();
				this.connectXnci1Plugin( (ExternalNamespaceConnectorInterface1)plugin, true, bindingName, false );

			// Field of a class:
			} else if (plugin instanceof Field) {
				this.connectFieldAsPlugin( (Field)plugin, null, true, bindingName );

			// Method of a class:
			} else if (plugin instanceof Method) {
				this.connectMethodAsPlugin( (Method)plugin, null, true, bindingName );

			// Class (without any instance): connect its all static methods/fields:
			} else if (plugin instanceof Class) {
				this.connectClassAsPlugin( (Class<?>)plugin, null, true, bindingName );

			// Fields/methods of an instance, which will be passed with the instance:
			} else if (plugin instanceof Object[]) {

				Object[] objects = (Object[])plugin;

				// Field of an instance:
				if (objects.length == 2 && objects[0] instanceof Field) {
					Field field = (Field)objects[0]; // [0] is the field
					Object instance = objects[1];    // [1] is the instance to which the field belongs
					this.connectFieldAsPlugin( field, instance, true, bindingName );

				// Method of an instance:
				} else if (objects.length == 2 && objects[0] instanceof Method) {
					Method method = (Method)objects[0]; // [0] is the method
					Object instance = objects[1];       // [1] is the instance to which the method belongs
					this.connectMethodAsPlugin( method, instance, true, bindingName );

				// Class (with instance): connect its all methods/fields:
				} else if (objects.length == 2 && objects[0] instanceof Class) {
					Class<?> pluginClass = (Class<?>)objects[0];
					Object instance = objects[1];
					this.connectClassAsPlugin( pluginClass, instance, true, bindingName );

				} else {
					throw new VnanoException(
						ErrorType.UNSUPPORTED_PLUGIN, new String[] {objects[0].getClass().getCanonicalName()}
					);
				}

			// Other objects: get its Class and connect it:
			} else {
				Class<?> pluginClass = plugin.getClass();
				this.connectClassAsPlugin( pluginClass, plugin, true, bindingName );
			}

		// Re-throw the VnanoException, with adding information of the cause plug-in.
		} catch (VnanoException vne) {
			throw new VnanoException(ErrorType.PLUGIN_CONNECTION_FAILED, nameInErrorMessage, vne);
		}
	}


	/**
	 * Disconnects all plug-ins.
	 *
	 * If the finalization (for disconnection) method is implemented on the plug-in,
	 * it will be called when the plug-in will be disconnected by this method.
	 *
	 * @throws VnanoException Thrown when an exception occurred on the finalization of the plug-in to be disconnected.
	 */
	public void disconnectAllPlugins() throws VnanoException {
		this.finalizeAllPluginsForDisconnection();
		this.externalFunctionTable = new FunctionTable();
		this.externalVariableTable = new VariableTable();
		this.xnci1PluginList = new ArrayList<ExternalNamespaceConnectorInterface1>();
		this.xfci1PluginList = new ArrayList<ExternalFunctionConnectorInterface1>();
		this.xvci1PluginList = new ArrayList<ExternalVariableConnectorInterface1>();
		this.pluginImportPathSet = new HashSet<String>();
		this.permissionAuthorizer = null;
	}


	/**
	 * Generate the value of the argument "bindingName" of
	 * {@link Interconnect#connectPlugin connectPlugin(String bindingKey, Object plugin)}
	 * method automatically.
	 *
	 * @param plugin
	 *   The value passed as the argument "plugin" of
	 *   {@link Interconnect#connectPlugin connectPlugin(String bindingKey, Object plugin)} method.
	 *
	 * @return The generated value.
	 * @throws VnanoException
	 *   Thrown if the plug-in could not be analyzed,
	 *   caused by unsupported interfaces, incompatibility of data types, and so on.
	 */
	private String generateBindingNameOf(Object plugin) throws VnanoException {

		// Variable object compatible with the variable format in this engine (e.g.: Xvci1ToVariableAdapter):
		if (plugin instanceof AbstractVariable) {
			return ((AbstractVariable)plugin).getVariableName();

		// Function object compatible with the variable format in this engine (e.g.: Xfci1ToFunctionAdapter):
		} else if (plugin instanceof AbstractFunction) {
			return IdentifierSyntax.getSignatureOf((AbstractFunction)plugin);

		// XVCI1 type external variable plug-in:
		} else if (plugin instanceof ExternalVariableConnectorInterface1) {
			return ((ExternalVariableConnectorInterface1)plugin).getVariableName();

		// XFCI1 type external function plug-in:
		} else if (plugin instanceof ExternalFunctionConnectorInterface1) {
			AbstractFunction functionAdapter =
					new Xfci1ToFunctionAdapter((ExternalFunctionConnectorInterface1)plugin);
			return IdentifierSyntax.getSignatureOf(functionAdapter);

		// XNCI1 type namespace plug-in:
		} else if (plugin instanceof ExternalNamespaceConnectorInterface1) {
			return ((ExternalNamespaceConnectorInterface1)plugin).getNamespaceName();

		// Field of a class:
		} else if (plugin instanceof Field) {
			return ((Field)plugin).getName();

		// Method of a class:
		} else if (plugin instanceof Method) {
			ExternalFunctionConnectorInterface1 xfci1Adapter = new MethodToXfci1Adapter((Method)plugin);
			AbstractFunction functionAdapter = new Xfci1ToFunctionAdapter(xfci1Adapter);
			return IdentifierSyntax.getSignatureOf(functionAdapter);

		// Class (without any instance):
		} else if (plugin instanceof Class) {
			return ((Class<?>)plugin).getCanonicalName();

		// Fields/methods of an instance, which will be passed with the instance:
		} else if (plugin instanceof Object[]) {

			Object[] objects = (Object[])plugin;

			// Field of an instance:
			if (objects.length == 2 && objects[0] instanceof Field) {
				Field field = (Field)objects[0];
				return generateBindingNameOf(field);

			// Method of an instance:
			} else if (objects.length == 2 && objects[0] instanceof Method) {
				Method method = (Method)objects[0];
				return generateBindingNameOf(method);

			// Class (with instance):
			} else if (objects.length == 2 && objects[0] instanceof Class) {
				Class<?> pluginClass = (Class<?>)objects[0];
				return generateBindingNameOf(pluginClass);
			} else {
				throw new VnanoException(
					ErrorType.UNSUPPORTED_PLUGIN, new String[] {objects[0].getClass().getCanonicalName()}
				);
			}

		// Other objects: get its Class and connect it:
		} else {
			Class<?> pluginClass = plugin.getClass();
			return generateBindingNameOf(pluginClass);
		}
	}


	/**
	 * Connects a Field type instance as a plug-in.
	 *
	 * This connection makes the field reflected by the passed Field type instance accessible
	 * from scripts as the external variable.
	 *
	 * @param field The Field type instance reflecting the field to be accessed from scripts.
	 * @param instance The instance of the class in which the method is defined. If the method is static, specify null.
	 * @param aliasingRequired Whether use the alias for accessing from scripts or not ("true" for use).
	 * @param aliasName The alias for accessing from scripts.
	 * @throws VnanoException Thrown if the plug-in could not be connected, caused by incompatibility of data types, and so on.
	 */
	private void connectFieldAsPlugin(Field field, Object instance, boolean aliasingRequired, String aliasName)
			throws VnanoException {

		FieldToXvci1Adapter adapter = new FieldToXvci1Adapter(field, instance);
		this.connectXvci1Plugin(adapter, aliasingRequired, aliasName, false, null);
	}


	/**
	 * Connects a Method type instance as a plug-in.
	 *
	 * This connection makes the method reflected by the passed Method type instance accessible
	 * from scripts as the external function.
	 *
	 * @param method The Method type instance reflecting the method to be accessed from scripts.
	 * @param instance The instance of the class to which the method belongs. If the method is static, specify null.
	 * @param aliasingRequired Whether use the alias for accessing from scripts or not ("true" for use).
	 * @param aliasName The alias for accessing from scripts.
	 * @throws VnanoException Thrown if the plug-in could not be connected, caused by incompatibility of data types, and so on.
	 */
	private void connectMethodAsPlugin(Method method, Object instance, boolean aliasingRequired, String aliasSignature)
			throws VnanoException {

		MethodToXfci1Adapter adapter = new MethodToXfci1Adapter(method,instance);
		this.connectXfci1Plugin(adapter, aliasingRequired, aliasSignature, false, null);
	}


	/**
	 * Connects a Class&lt;T&gt; type instance as a plug-in.
	 *
	 * This connection makes methods and fields which belong to
	 * "the class T reflected by the passed Class&lt;T&gt; type instance"
	 * accessible from scripts as the external functions and variables.
	 *
	 * @param pluginClass
	 *   The Class&lt;T&gt; type instance reflecting the class T to which
	 *   methods and fields to be accessed from scripts belong.
	 *
	 * @param instance
	 *   The instance of the class of T.
	 *   If all methods and fields to be accessed are static, it allows null to be passed.
	 *
	 * @param aliasingRequired
	 *   Whether use the alias for accessing from scripts or not ("true" for use).
	 *
	 * @param aliasName
	 *   The alias for accessing from scripts.
	 *
	 * @throws VnanoException
	 *   Thrown if the plug-in could not be connected, caused by incompatibility of data types, and so on.
	 */
	private void connectClassAsPlugin(Class<?> pluginClass, Object instance, boolean aliasingRequired, String aliasName)
			throws VnanoException {
		ClassToXnci1Adapter adapter = new ClassToXnci1Adapter(pluginClass,instance);
		this.connectXnci1Plugin(adapter, aliasingRequired, aliasName, true);
	}


	/**
	 * Connects the plug-in of {@link org.vcssl.connect.PermissionAuthorizerConnectorInterface1 PACI1} format,
	 * for authorization of permissions.
	 *
	 * @param plugin The plug-in to be connected.
	 * @throws VnanoException Thrown if the plug-in could not be connected, caused by initialization errors and so on.
	 */
	private void connectPaci1Plugin(PermissionAuthorizerConnectorInterface1 plugin) throws VnanoException {

		// Can not connect multiple permission authorizer plug-ins,
		// so if any permission authorizer plug-in is already connected, throw an exception.
		if (this.permissionAuthorizer != null) {
			throw new VnanoException(
				ErrorType.MULTIPLE_PERMISSION_AUTHORIZERS_ARE_CONNECTED,
				new String[]{ plugin.getClass().getCanonicalName(), this.permissionAuthorizer.getClass().getCanonicalName() }
			);
		}

		// Initialize the plug-in.
		try {
			plugin.initializeForConnection(this.engineConnector);
		} catch (ConnectorException e) {
			throw new VnanoException(
				ErrorType.PLUGIN_INITIALIZATION_FAILED, plugin.getClass().getCanonicalName(), e
			);
		}

		// Connect only when the initialization has succeeded.
		this.permissionAuthorizer = plugin;

		// Reflect to the engine connector, because permissions will be requested from other plug-ins.
		this.engineConnector = this.engineConnector.createPermissionAuthorizerUpdatedInstance(
			this.permissionAuthorizer
		);
	}


	/**
	 * Connects the plug-in of {@link org.vcssl.connect.ExternalVariableConnectorInterface1 XVCI1} format,
	 * which provides an external variable.
	 *
  	 * @param plugin The plug-in to be connected.
	 * @param aliasingRequired Whether use the alias for accessing from scripts or not ("true" for using).
	 * @param aliasName The alias for accessing from scripts.
	 * @param belongsToNamespace Whether the variable provided by the plug-in belongs to any namespaces.
	 * @param namespaceName The name of the namespace to which the variable provided by the plug-in belongs.
	 * @throws VnanoException Thrown if the plug-in could not be connected, caused by incompatibility of data types, and so on.
	 */
	private void connectXvci1Plugin(ExternalVariableConnectorInterface1 plugin,
			boolean aliasingRequired, String aliasName, boolean belongsToNamespace, String namespaceName) throws VnanoException {

		// Initialize the plug-in.
		try {
			plugin.initializeForConnection(this.engineConnector);
		} catch (ConnectorException e) {
			throw new VnanoException(
				ErrorType.PLUGIN_INITIALIZATION_FAILED, plugin.getClass().getCanonicalName(), e
			);
		}

		// Add to the list, in which all XVCI1 plug-ins are registered.
		this.xvci1PluginList.add(plugin);

		// Cast to an AbstractVariable, which is a base class of variables in this engine.
		AbstractVariable adapter = new Xvci1ToVariableAdapter(plugin);

		// Set the namespace or the alias if necessary.
		if (belongsToNamespace) {
			adapter.setNamespaceName(namespaceName);
		}
		if (aliasingRequired) {
			adapter.setVariableName(aliasName);
		}

		// Connect the plug-in.
		this.connectVariable(adapter);
	}


	/**
	 * Connects the plug-in of {@link org.vcssl.connect.ExternalFunctionConnectorInterface1 XFCI1} format,
	 * which provides an external function.
	 *
	 * @param plugin The plug-in to be connected.
	 * @param aliasingRequired Whether use the alias for accessing from scripts or not ("true" for using).
	 * @param aliasName The alias for accessing from scripts.
	 * @param belongsToNamespace Whether the function provided by the plug-in belongs to any namespaces.
	 * @param namespaceName The name of the namespace to which the function provided by the plug-in belongs.
	 * @throws VnanoException Thrown if the plug-in could not be connected, caused by incompatibility of data types, and so on.
	 */
	private void connectXfci1Plugin(ExternalFunctionConnectorInterface1 plugin,
			boolean aliasingRequired, String aliasName, boolean belongsToNamespace, String namespaceName) throws VnanoException {

		// Initialize the plug-in.
		try {
			plugin.initializeForConnection(this.engineConnector);
		} catch (ConnectorException e) {
			throw new VnanoException(
				ErrorType.PLUGIN_INITIALIZATION_FAILED, plugin.getClass().getCanonicalName(), e
			);
		}

		// Add to the list, in which all XFCI1 plug-ins are registered.
		this.xfci1PluginList.add(plugin);

		// Cast to an AbstractFunction, which is a base class of functions in this engine.
		AbstractFunction adapter = new Xfci1ToFunctionAdapter(plugin);

		// Set the namespace or the alias if necessary.
		if (belongsToNamespace) {
			adapter.setNamespaceName(namespaceName);
		}
		if (aliasingRequired) {
			adapter.setFunctionName(aliasName);
		}

		// Connect the plug-in.
		this.connectFunction(adapter);
	}


	/**
	 * Connects the plug-in of {@link org.vcssl.connect.ExternalNamespaceConnectorInterface1 XNCI1} format,
	 * which provides a set of external variables and external functions.
	 *
	 * @param plugin The plug-in to be connected.
	 * @param aliasingRequired Whether use the alias for accessing from scripts or not ("true" for using).
	 * @param aliasName The alias for accessing from scripts.
	 * @throws VnanoException Thrown if the plug-in could not be connected, caused by incompatibility of data types, and so on.
	 */
	private void connectXnci1Plugin(
			ExternalNamespaceConnectorInterface1 plugin, boolean aliasingRequired, String aliasName,
				boolean ignoreIncompatibles) throws VnanoException {

		// Execute pre-initialization, which should be executed before when the plug-in is connected.
		try {
			plugin.preInitializeForConnection(this.engineConnector);
		} catch (ConnectorException e) {
			throw new VnanoException(
				ErrorType.PLUGIN_INITIALIZATION_FAILED, plugin.getClass().getCanonicalName(), e
			);
		}

		// Get the namespace or its alias.
		String nameapaceName = plugin.getNamespaceName();
		if (aliasingRequired) {
			nameapaceName = aliasName;
		}

		// Register the namespace to make it available as the value of "import" declaration.
		this.pluginImportPathSet.add(nameapaceName);

		// Connect all function plug-ins, which belong to the namespace to be connected.
		ExternalFunctionConnectorInterface1[] xfciPlugins = plugin.getFunctions();
		for (ExternalFunctionConnectorInterface1 xfciPlugin: xfciPlugins) {
			try {
				this.connectXfci1Plugin(xfciPlugin, false, null, true, nameapaceName);
			} catch (VnanoException e) {
				if (!ignoreIncompatibles) {
					throw e;
				}
			}
		}

		// Connect all variable plug-ins, which belong to the namespace to be connected.
		ExternalVariableConnectorInterface1[] xvciPlugins = plugin.getVariables();
		for (ExternalVariableConnectorInterface1 xvciPlugin: xvciPlugins) {
			try {
				this.connectXvci1Plugin(xvciPlugin, false, null, true, nameapaceName);
			} catch (VnanoException e) {
				if (!ignoreIncompatibles) {
					throw e;
				}
			}
		}

		// Execute post-initialization, which should be executed after when the plug-in is connected.
		try {
			plugin.postInitializeForConnection(this.engineConnector);
		} catch (ConnectorException e) {
			throw new VnanoException(
				ErrorType.PLUGIN_INITIALIZATION_FAILED, plugin.getClass().getCanonicalName(), e
			);
		}

		// Add to the list, in which all XNCI1 plug-ins are registered.
		this.xnci1PluginList.add(plugin);
	}


	/**
	 * Connects a variable object compatible with the variable format in this engine (Xvci1ToVariableAdapter and so on).
	 *
	 * @param variable variable object to be connected.
	 */
	private void connectVariable(AbstractVariable variable) {
		this.externalVariableTable.addVariable(variable);
	}


	/**
	 * Connects a function object compatible with the function format in this engine (Xfci1ToFunctionAdapter and so on).
	 *
	 * @param function function object to be connected.
	 */
	private void connectFunction(AbstractFunction function) {
		this.externalFunctionTable.addFunction(function);
	}


	/**
	 * Initializes all connected plug-ins, just before when a (new) script is executed.
	 */
	private void initializeAllPluginsForExecution() throws VnanoException {

		// Store a plug-in just before when its finalization method is invoked,
		// to embed information of cause plug-in into an error message, when any error occurred.
		Object initializingPlugin = null;

		try {
			// Initialize of the permission authorizer plug-in should be invoked
			// before when initializations of all plug-ins are invoked,
			// because initialization processes of other plug-ins may request permission-related actions.
			if (this.permissionAuthorizer != null) {
				this.permissionAuthorizer.initializeForExecution(this.engineConnector);
			}

			// Invoke initialization methods of all plug-ins.
			// Be careful of the order of initializations.

			for (ExternalNamespaceConnectorInterface1 plugin: xnci1PluginList) {
				initializingPlugin = plugin;
				plugin.preInitializeForExecution(this.engineConnector);
			}
			for (ExternalFunctionConnectorInterface1 plugin: xfci1PluginList) {
				initializingPlugin = plugin;
				plugin.initializeForExecution(this.engineConnector);
			}
			for (ExternalVariableConnectorInterface1 plugin: xvci1PluginList) {
				initializingPlugin = plugin;
				plugin.initializeForExecution(this.engineConnector);
			}
			for (ExternalNamespaceConnectorInterface1 plugin: xnci1PluginList) {
				initializingPlugin = plugin;
				plugin.postInitializeForExecution(this.engineConnector);
			}
		} catch (ConnectorException e) {
			throw new VnanoException(
				ErrorType.PLUGIN_INITIALIZATION_FAILED, initializingPlugin.getClass().getCanonicalName(), e
			);
		}
	}


	/**
	 * Finalizes all connected plug-ins, when the execution of a script is terminated or completed.
	 */
	private void finalizeAllPluginsForTermination() throws VnanoException {

		// Store a plug-in just before when its finalization method is invoked,
		// to embed information of cause plug-in into an error message, when any error occurred.
		Object finalizingPlugin = null;
		try {

			// Invoke finalization methods of all plug-ins.
			// Be careful of the order of finalizations.

			for (ExternalNamespaceConnectorInterface1 plugin: xnci1PluginList) {
				finalizingPlugin = plugin;
				plugin.preFinalizeForTermination(this.engineConnector);
			}
			for (ExternalFunctionConnectorInterface1 plugin: xfci1PluginList) {
				finalizingPlugin = plugin;
				plugin.finalizeForTermination(this.engineConnector);
			}
			for (ExternalVariableConnectorInterface1 plugin: xvci1PluginList) {
				finalizingPlugin = plugin;
				plugin.finalizeForTermination(this.engineConnector);
			}
			for (ExternalNamespaceConnectorInterface1 plugin: xnci1PluginList) {
				finalizingPlugin = plugin;
				plugin.postFinalizeForTermination(this.engineConnector);
			}

			// Finalization of the permission authorizer plug-in should be invoked
			// after when finalizations of all plug-ins have been completed,
			// because finalization processes of other plug-ins may request permission-related actions.
			if (this.permissionAuthorizer != null) {
				this.permissionAuthorizer.finalizeForTermination(this.engineConnector);
			}

		} catch (ConnectorException e) {
			throw new VnanoException(
				ErrorType.PLUGIN_FINALIZATION_FAILED, finalizingPlugin.getClass().getCanonicalName(), e
			);
		}
	}


	/**
	 * Finalizes all connected plug-ins, when they are disconnected.
	 */
	private void finalizeAllPluginsForDisconnection() throws VnanoException {

		// Store a plug-in just before when its finalization method is invoked,
		// to embed information of cause plug-in into an error message, when any error occurred.
		Object finalizingPlugin = null;

		try {

			// Invoke finalization methods of all plug-ins.
			// Be careful of the order of finalizations.

			for (ExternalNamespaceConnectorInterface1 plugin: xnci1PluginList) {
				finalizingPlugin = plugin;
				plugin.preFinalizeForDisconnection(this.engineConnector);
			}
			for (ExternalFunctionConnectorInterface1 plugin: xfci1PluginList) {
				finalizingPlugin = plugin;
				plugin.finalizeForDisconnection(this.engineConnector);
			}
			for (ExternalVariableConnectorInterface1 plugin: xvci1PluginList) {
				finalizingPlugin = plugin;
				plugin.finalizeForDisconnection(this.engineConnector);
			}
			for (ExternalNamespaceConnectorInterface1 plugin: xnci1PluginList) {
				finalizingPlugin = plugin;
				plugin.postFinalizeForDisconnection(this.engineConnector);
			}

			// Finalization of the permission authorizer plug-in should be invoked
			// after when finalizations of all plug-ins have been completed,
			// because finalization processes of other plug-ins may request permission-related actions.
			if (this.permissionAuthorizer != null) {
				this.permissionAuthorizer.finalizeForDisconnection(this.engineConnector);
			}

		} catch (ConnectorException e) {
			throw new VnanoException(
				ErrorType.PLUGIN_FINALIZATION_FAILED, finalizingPlugin.getClass().getCanonicalName(), e
			);
		}
	}


	/**
	 * Add a library script.
	 *
	 * @param libraryScriptName The names of the library script.
	 * @param libraryScriptContent The content (code) of the library script.
	 * @throws VnanoException Thrown when incorrect somethings have been detected for the specified library.
	 */
	public void addLibraryScript(String libraryScriptName, String libraryScriptContent) throws VnanoException {
		if (libraryScriptName == null || libraryScriptContent == null) {
			throw new NullPointerException();
		}
		if (this.libraryFilePathContentMap.containsKey(libraryScriptName)) {
			throw new VnanoException(ErrorType.LIBRARY_IS_ALREADY_INCLUDED, libraryScriptName);
		}
		String normalizedName = IdentifierSyntax.normalizeScriptIdentifier(libraryScriptName);
		this.libraryFilePathContentMap.put(normalizedName, libraryScriptContent);

		String importPath = this.getImportPathOf(normalizedName);
		this.libraryImportPathSet.add(importPath);
	}


	/**
	 * Remove all library scripts.
	 */
	public void removeAllLibraryScripts() {
		this.libraryFilePathContentMap = new LinkedHashMap<String, String>();
		this.libraryImportPathSet = new HashSet<String>();
	}


	/**
	 * Set the main script.
	 *
	 * @param mainScriptName The name of the main script.
	 * @param mainScriptContent The content of the main script.
	 */
	public void setMainScript(String mainScriptName, String mainScriptContent) {
		this.mainScriptName = mainScriptName;
		this.mainScriptContent = mainScriptContent;
	}


	/**
	 * Remove the main script.
	 */
	public void removeMainScript() {
		this.mainScriptName = null;
		this.mainScriptContent = null;
	}


	/**
	 * Gets the number of the registered library scripts.
	 *
	 * @return The number of the registered library scripts.
	 */
	public int getLibraryScriptCount() {
		return this.libraryFilePathContentMap.size();
	}


	/**
	 * Gets the file paths of all scripts (the main script and the library scripts).
	 *
	 * File paths of libraries are stored as elements from [0] to [N-2] of the returned array,
	 * where N is the length of the returned array.
	 *
	 * The "name" of the main script is stored at [N-1] (the last element) in the returned array.
	 * It is not a file path, and may not be an actual file name (specified as the value of "MAIN_SCRIPT_NAME" option).
	 *
	 * @return The file paths of all scripts.
	 */
	public String[] getScriptPaths() {
		List<String> scriptPathList = new ArrayList<String>();
		for (Map.Entry<String, String> pathContentPair: this.libraryFilePathContentMap.entrySet()) {
			String libName = pathContentPair.getKey();
			scriptPathList.add(libName);
		}
		scriptPathList.add(this.mainScriptName);
		String[] scriptPaths = new String[scriptPathList.size()];
		scriptPaths = scriptPathList.toArray(scriptPaths);
		return scriptPaths;
	}


	/**
	 * Gets the contents of all scripts (the main script and the library scripts).
	 *
	 * @return The contents of all scripts.
	 */
	public String[] getScriptContents() {
		List<String> scriptContentList = new ArrayList<String>();
		for (Map.Entry<String, String> pathContentPair: this.libraryFilePathContentMap.entrySet()) {
			String libContent = pathContentPair.getValue();
			scriptContentList.add(libContent);
		}
		scriptContentList.add(this.mainScriptContent);
		String[] scriptContents = new String[scriptContentList.size()];
		scriptContents = scriptContentList.toArray(scriptContents);
		return scriptContents;
	}


	/**
	 * Converts the specified script name/path to the "import path".
	 *
	 * An import path is a string which can be specified as a value of a "import" declaration,
	 * e.g.: "dir1.dir2.ExampleScript" for the script "dir1/dir2/ExampleScript.vnano".
	 *
	 * @param scriptName The name or path of the script.
	 * @return The "import path" of the script.
	 */
	private String getImportPathOf(String scriptName) {
		String identifier = scriptName;
		if (identifier.toLowerCase().endsWith(".vnano")) {
			identifier = identifier.substring(0, identifier.length() - ".vnano".length());
		}
		identifier = identifier.replace('/', '.');
		identifier = identifier.replace('\\', '.');
		identifier = identifier.replace(':', '.');
		identifier = identifier.replaceAll("\\.+", ".");
		identifier = identifier.replaceAll("^\\.", "");
		return identifier;
	}


	/**
	 * Returns whether this interconnect has the library or plug-in, corresponding with the specified "import path".
	 *
	 * @param importPath The "import path" of the library/plug-in.
	 * @return Returns true if the corresponding library/plug-in exists in this interconnect.
	 */
	public boolean hasDependentLibraryOrPlugin(String importPath) {
		if (this.libraryImportPathSet.contains(importPath)) {
			return true;
		}
		if (this.pluginImportPathSet.contains(importPath)) {
			return true;
		}
		return false;
	}
}
