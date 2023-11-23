/*
 * Copyright(C) 2019-2023 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.script.ScriptException;

import org.vcssl.connect.ConnectorException;
import org.vcssl.nano.compiler.Compiler;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.interconnect.MetaQualifiedFileLoader;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.spec.OptionKey;
import org.vcssl.nano.spec.PerformanceKey;
import org.vcssl.nano.vm.VirtualMachine;

/**
 * The class of the scripting engine of the Vnano (Vnano Engine).
 */
public final class VnanoEngine {

	/** Stores a process virtual machine executing the currently running intermediate code. */
	private VirtualMachine virtualMachine = null;

	/** Stores an object to mediate information/connections between components ("interconnect"). */
	private Interconnect interconnect = null;

	/** Stores the script which was executed last time. */
	private String lastScript = null;

	/** The flag representing that this engine's state is the same with it of when "lastScript" was executed. */
	private boolean lastStateIsSame = false;

	/** The flag representing that "AUTOMATIC_ACTIVATION_ENABLED" option was enabled when "lastScript" was executed. */
	private boolean lastAutoActivationIsEnabled = false;


	/**
	 * Create a Vnano Engine with default settings.
	 */
	public VnanoEngine() {
		this.interconnect = new Interconnect();
		this.virtualMachine = new VirtualMachine();
	}


	/**
	 * Executes an expression or script code specified as the argument.
	 *
	 * Please note that,
	 * you must not call this method of the same instance at the same time from multiple threads,
	 * for processing multiple scripts in parallel.
	 * For such parallel executions, create an independent instance of the engine for each thread and use them.
	 *
	 * @param script An expression or script code to be executed.
	 *
	 * @return
	 *     The evaluated value of the expression, or the last expression statement in script code.
	 *     If there is no evaluated value, returns null.
	 *
	 * @throws VnanoException Thrown when any error has detected for the content or the processing of the script.
	 */
	public Object executeScript(String script) throws VnanoException {
		if (script == null) {
			throw new NullPointerException();
		}

		// If the input script is the same with the last executed script, we can re-execute it with less overheads, using caches.
		if (this.lastScript != null || this.lastStateIsSame) {

			// When the references of two String variables are the same, their values are also the same.
			// (Because they point to the same String instance.)
			// Of course, their values can be the same when their references are different.
			if (script == this.lastScript || script.equals(this.lastScript)) {
				return this.reexecuteLastScript();
			}
		}

		try {

			// Remove the encoding declaration if exists, and normalize environment-dependent contents, and so on.
			try {
				script = MetaQualifiedFileLoader.postprocess(null, script); // The first arg is the file name to display in error messages.
			} catch (VnanoException vne) {
				String message = vne.getMessageWithoutLocation();
				throw new ScriptException(message);
			}

			// Get the name of the main script from the option map, if it is set.
			// (The main script name has already been normalized,
			//  because all option values are normalized when the option map is set to the engine.)
			String mainScriptName = (String)this.interconnect.getOptionMap().get(OptionKey.MAIN_SCRIPT_NAME);

			// Set the name/content of the main script, to the interconnect.
			this.interconnect.setMainScript(mainScriptName, script);

			// Check whether the automatic-activation feature is enabled.
			boolean autoActivationIsEnabled = (Boolean)this.interconnect.getOptionMap().get(OptionKey.AUTOMATIC_ACTIVATION_ENABLED);

			// Activate the interconnect, for executing the script.
			// (All connected plug-ins are initialized at this timing.)
			if (autoActivationIsEnabled) {
				this.interconnect.activate();
			}

			// Get the file paths and contents of all scripts (the main script and all library scripts), from the interconnect.
			String[] scripts = this.interconnect.getScriptContents();
			String[] paths   = this.interconnect.getScriptPaths();

			// Translate scripts to a VRIL code (intermediate assembly code) by a compiler.
			String assemblyCode = new Compiler().compile(scripts, paths, this.interconnect);

			// Execute the VRIL code on a VM.
			Object evalValue = this.virtualMachine.executeAssemblyCode(assemblyCode, this.interconnect);

			// Deactivate the interconnect.
			// (All Connected plug-ins are finalized at this timing.)
			if (autoActivationIsEnabled) {
				this.interconnect.deactivate();
			}

			// Stores the input script, to detect that the same script is input again.
			// Also, set some flags, to reduce overhead costs of re-executions of the same script.
			this.lastScript = script;
			this.lastStateIsSame = true;
			this.lastAutoActivationIsEnabled = autoActivationIsEnabled;

			return evalValue;

		// If any error is occurred for the content/processing of the script,
		// set the locale to switch the language of error messages, and re-throw the exception to upper layers.
		} catch (VnanoException e) {
			Locale locale = (Locale)this.interconnect.getOptionMap().get(OptionKey.LOCALE); // Type was already checked.
			e.setLocale(locale);

			if (e.getCause() instanceof ConnectorException && ((ConnectorException)e.getCause()).getMessage().startsWith("___")) {
				this.handleSpecialConnectorException((ConnectorException)e.getCause(), e);
				return null;
			} else {
				throw e;
			}

		// If unexpected exception is occurred, wrap it by the VnanoException and re-throw,
		// to prevent the stall of the host-application.
		} catch (Exception unexpectedException) {
			throw new VnanoException(unexpectedException);
		}
	}


	/**
	 * Re-execute the script which was executed last time,
	 * in less overhead way using cached resources in the VM.
	 * 
	 * @return
	 *     The evaluated value of the expression, or the last expression statement in script code.
	 *     If there is no evaluated value, returns null.
	 * 
	 * @throws VnanoException Thrown when any error has detected for the content or the processing of the script.
	 */
	private Object reexecuteLastScript() throws VnanoException {

		// Activate the interconnect, for executing the script.
		// (All connected plug-ins are initialized at this timing.
		if (this.lastAutoActivationIsEnabled) {
			this.interconnect.activate();
		}

		// On the VM, re-execute the last executed VRIL code, using caches.
		Object evalValue = this.virtualMachine.reexecuteLastAssemblyCode(this.interconnect);

		// Deactivate the interconnect.
		// (All Connected plug-ins are finalized at this timing.)
		if (this.lastAutoActivationIsEnabled) {
			this.interconnect.deactivate();
		}

		return evalValue;
	}


	/**
	 * Handles a ConnectorException thrown when executing a script, if the exception requires special handling.
	 *
	 * @param exception The thrown ConnectorException.
	 * @param callerScriptName
	      The VnanoException wrapping the thrown ConnectorException
	      (provides line-number in the script and so on).
	 */
	private void handleSpecialConnectorException(ConnectorException exception, VnanoException wrapperVnanoException)
			throws VnanoException {

		String message = exception.getMessage();

		// If the message is "___EXIT",
		// it means that the ConnectorException had been thrown by exit() function, for terminating the script normally.
		// So we should not display any error message in this case.
		if (message.startsWith("___EXIT")) {
			return;
		}

		// If the message is "___ERROR",
		// it means that the ConnectorException had been thrown by error(string errorMessage) function,
		// for terminating the script with the error message specified as the arg "errorMessage".
		// So extract the content of "errorMessage" from the message of the ConnectorException,
		// and wrap it by VnanoException, and rethrow it to display.
		if (message.startsWith("___ERROR")) {
			String passedErrorMessage = message.split(":")[1]; // = The argument of error(...) function.
			VnanoException vne = wrapperVnanoException.clone();
			vne.setErrorType(ErrorType.UNMODIFIED);
			vne.setErrorWords( new String[] { passedErrorMessage } );
			throw vne;
		}
	}


	/**
	 * Requests this engine to terminate the currently running script as soon as possible.
	 *
	 * To be precise, the {@link org.vcssl.nano.vm.VirtualMachine VirtualMachine}
	 * (which is processing instructions compiled from the script) in the engine
	 * will be terminated after when the processing of a currently executed instruction has been completed,
	 * without processing remained instructions.
	 *
	 * Also, if you used this method, call {@link VnanoEngine#resetTerminator()}
	 * method before the next execution of a new script,
	 * otherwise the next execution will end immediately without processing any instructions.
	 *
	 * By the above behavior, even if a termination request by this method and
	 * an execution request by another thread are conflict, the execution will be terminated certainly
	 * (unless {@link VnanoEngine#resetTerminator() resetTerminator()} will be called before
	 * when the execution will have been terminated).
	 *
	 * @throws VnanoFatalException (Unchecked Exception)
	 *       Thrown if this method is called in a state in which {@link VnanoEngine#isTerminatorEnabled()} returns false.
	 *       Note that, if any exceptions occurred on the finalization processes of the connected plug-ins,
	 *       it will be throws by the currently running
	 *       {@link VnanoEngine#executeScript(String script) executeScript(String script)} method,
	 *       not by this method.
	 *       This method throws the exception only when it failed in requesting the termination.
	 */
	public void terminateScript() {
		if (!this.isTerminatorEnabled()) {
			throw new VnanoFatalException(ErrorType.TERMINATOR_IS_DISABLED);
		}
		this.virtualMachine.terminate();
	}


	/**
	 * Returns whether the "terminator" which is the feature to terminate scripts, is enabled.
	 *
	 * Internally, this method checks the value of "TERMINATOR_ENABLED" option (disabled by default) and returns it.
	 *
	 * If this method returns true, {@link VnanoEngine#terminateScript() terminateScript()} method and
	 * {@link VnanoEngine#resetTerminator() resetTerminator()} method are available.
	 *
	 * Please note that, even when this method returns true, some errors may occur in the termination processes
	 * (for example, erros caused by failures of finalization processes of the connected plug-ins, and so on).
	 * For details, see the explanation about exceptions,
	 * in the description of {@link VnanoEngine#terminateScript() terminateScript()} method.
	 *
	 * @return Returns true if the "terminator" is enabled.
	 */
	public boolean isTerminatorEnabled() {
		return (boolean)this.interconnect.getOptionMap().get(OptionKey.TERMINATOR_ENABLED);
	}


	/**
	 * Resets the engine which had terminated by {@link VnanoEngine#terminateScript()} method,
	 * for processing new scripts.
	 *
	 * Please note that, if an execution of code is requested by another thread
	 * when this method is being processed, the execution request might be missed.
	 *
	 * @throws VnanoFatalException (Unchecked Exception)
	 *       Thrown if this method is called in a state in which {@link VnanoEngine#isTerminatorEnabled()} returns false.
	 */
	public void resetTerminator() {
		if (!this.isTerminatorEnabled()) {
			throw new VnanoFatalException(ErrorType.TERMINATOR_IS_DISABLED);
		}
		this.virtualMachine.resetTerminator();
	}


	/**
	 * Connects various types of plug-ins which provides external functions/variables.
	 *
	 * @param bindingName
	 *   The name in scripts of the variable/function/namespace provided by the connected plug-in.
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
	 *   type plug-ins which is used for managing permissions.
	 *
	 * @throws VnanoException
	 *   Thrown if the plug-in could not be connected,
	 *   caused by unsupported interfaces, incompatibility of data types, and so on.
	 */
	public void connectPlugin(String bindingName, Object plugin) throws VnanoException {
		if (bindingName == null || plugin == null) {
			throw new NullPointerException();
		}
		this.lastStateIsSame = false;
		this.interconnect.connectPlugin(bindingName, plugin);
	}


	/**
	 * Disconnects all plug-ins.
	 *
	 * @throws VnanoException Thrown when an exception occurred on the finalization of the plug-in to be disconnected.
	 */
	public void disconnectAllPlugins() throws VnanoException {
		this.lastStateIsSame = false;
		this.interconnect.disconnectAllPlugins();
	}


	/**
	 * Register a library script which will be "include"-ed at the head of a executed script.
	 *
	 * @param libraryScriptName The file path (or name) of the library script.
	 * @param libraryScriptContent Content (code) of the library script.
	 * @throws VnanoException Thrown when incorrect something have been detected for the specified library.
	 */
	public void registerLibraryScript(String libraryScriptPath, String libraryScriptContent) throws VnanoException {
		if (libraryScriptPath == null || libraryScriptContent == null) {
			throw new NullPointerException();
		}
		this.lastStateIsSame = false;
		this.interconnect.addLibraryScript(libraryScriptPath, libraryScriptContent);
	}


	/**
	 * Unregister all library scripts.
	 *
	 * @throws VnanoException
	 *   Will not be thrown on the current implementation,
	 *   but it requires to be "catch"-ed for keeping compatibility in future.
	 */
	public void unregisterAllLibraryScripts() throws VnanoException {
		this.lastStateIsSame = false;
		this.interconnect.removeAllLibraryScripts();
	}


	/**
	 * Sets options, by a Map (option map) storing names and values of options you want to set.
	 *
	 * Type of the option map is Map<String,Object>, and its keys represents option names.
	 * For details of option names and values,
	 * see {@link org.vcssl.nano.spec.OptionKey} and {@link org.vcssl.nano.spec.OptionValue}.
	 *
	 * Please note that,
	 * if any value stored in the option map is changed after setting the map by this method,
	 * it is NOT guaranteed that the engine reflect the change.
	 * Hence, when any value is changed, re-set the option map again by this method.
	 *
	 * @param optionMap The Map (option map) storing names and values of options.
	 * @throws VnanoException Thrown if invalid option settings is detected.
	 */
	public void setOptionMap(Map<String,Object> optionMap) throws VnanoException {
		if (optionMap == null) {
			throw new NullPointerException();
		}
		this.lastStateIsSame = false;
		this.interconnect.setOptionMap(optionMap);
	}


	/**
	 * Returns whether {VnanoEngine#getOptionMap() getOptionMap()} method can return a Map.
	 *
	 * @return Returns true if {VnanoEngine#getOptionMap() getOptionMap()} method can return a Map.
	 */
	public boolean hasOptionMap() {
		return this.interconnect.getOptionMap() != null;
	}


	/**
	 * Gets the Map (option map) storing names and values of options.
	 *
	 * Type of the option map is Map<String,Object>, and its keys represents option names.
	 * For details of option names and values,
	 * see {@link org.vcssl.nano.spec.OptionKey} and {@link org.vcssl.nano.spec.OptionValue}.
	 *
	 * @return The Map (option map) storing names and values of options.
	 * @throws VnanoFatalException (Unchecked Exception)
	 *       Thrown if this method is called in a state in which {@link VnanoEngine#hasOptionMap()} returns false.
	 */
	public Map<String,Object> getOptionMap() {
		if (!this.hasOptionMap()) {
			throw new VnanoFatalException(ErrorType.CAN_NOT_GET_OPTION_MAP);
		}
		return this.interconnect.getOptionMap();
	}


	/**
	 * Sets permissions, by a Map (permission map) storing names and values of permission items you want to set.
	 *
	 * Type of the permission map is Map<String,String>, and its keys represents names of permission items.
	 * For details of names and values of permission items,
	 * see {@link org.vcssl.connect.ConnectorPermissionName} and {@link org.vcssl.connect.ConnectorPermissionValue}.
	 *
	 * Please note that,
	 * if any value stored in the permission map is changed after setting the map by this method,
	 * it is NOT guaranteed that the engine reflect the change.
	 * Hence, when any value is changed, re-set the permission map again by this method.
	 *
	 * @param permissionMap The Map (permission map) storing names and values of permission items.
	 *
	 * @throws VnanoException Thrown if invalid permission settings is detected.
	 */
	public void setPermissionMap(Map<String, String> permissionMap) throws VnanoException {
		if (permissionMap == null) {
			throw new NullPointerException();
		}
		this.lastStateIsSame = false;
		this.interconnect.setPermissionMap(permissionMap);
	}


	/**
	 * Returns whether {VnanoEngine#getPermissionMap() getPermissionMap()} method can return a Map.
	 *
	 * @return Returns true if {VnanoEngine#getPermissionMap() getPermissionMap()} method can return a Map.
	 */
	public boolean hasPermissionMap() {
		return this.interconnect.getPermissionMap() != null;
	}


	/**
	 * Gets the Map (permission map) storing names and values of permission items.
	 *
	 * Type of the permission map is Map<String,String>, and its keys represents names of permission items.
	 * For details of names and values of permission items,
	 * see {@link org.vcssl.connect.ConnectorPermissionName} and {@link org.vcssl.connect.ConnectorPermissionValue}.
	 *
	 * @return The Map (permission map) storing names and values of permission items.
	 * @throws VnanoFatalException (Unchecked Exception)
	 *       Thrown if this method is called in a state in which {@link VnanoEngine#hasPermissionMap()} returns false.
	 */
	public Map<String, String> getPermissionMap() {
		if (!this.hasPermissionMap()) {
			throw new VnanoFatalException(ErrorType.CAN_NOT_GET_PERMISSION_MAP);
		}
		return this.interconnect.getPermissionMap();
	}


	/**
	 * Returns whether {VnanoEngine#getPerformanceMap() getPerformanceMap()} method can return a Map.
	 *
	 * Internally, this method checks the value of "PERFORMANCE_MONITOR_ENABLED" option and returns it.
	 *
	 * @return Returns true if {VnanoEngine#getPerformanceMap() getPerformanceMap()} method can return a Map.
	 */
	public boolean hasPerformanceMap() {
		return (boolean)this.interconnect.getOptionMap().get(OptionKey.PERFORMANCE_MONITOR_ENABLED);
	}


	/**
	 * Gets the Map (performance map) storing names and values of performance monitoring items.
	 *
	 * Note that, when some measured values for some monitoring items don't exist
	 * (e.g.: when any scripts are not running, or running but their performance values are not measualable yet),
	 * the returned performance map does not contain values for such monitoring items,
	 * so sometimes the returned performance map is incomplete (missing values for some items) or empty.
	 * Please be careful of the above point when you "get" measured performance values from the returned performance map.
	 *
	 * @return The Map (performance map) storing names and values of performance monitoring items.
	 * @throws VnanoFatalException (Unchecked Exception)
	 *       Thrown if this method is called in a state in which {@link VnanoEngine#hasPerformanceMap()} returns false.
	 */
	public Map<String, Object> getPerformanceMap() {
		synchronized (this) {
			if (!this.hasPerformanceMap()) {
				throw new VnanoFatalException(ErrorType.PERFORMANCE_MONITOR_IS_DISABLED);
			}

			Map<String, Object> performanceMap = new LinkedHashMap<String, Object>();

			// If the VM is already instantiated, get and store performance monitoring values.
			if (this.virtualMachine != null) {

				// Get/store the counter value of the instructions executed from when the VM had been instantiated.
				int instructionCount = this.virtualMachine.getExecutedInstructionCountIntValue();
				performanceMap.put(PerformanceKey.EXECUTED_INSTRUCTION_COUNT_INT_VALUE, instructionCount);

				// Get/store the operation codes of the currently executed instructions.
				// (We get an empty array when the VM is idling. In that case, We put nothing to the Map.)
				OperationCode[] currentOpcodes = this.virtualMachine.getCurrentlyExecutedOperationCodes();
				if (currentOpcodes.length != 0) {
					String[] opcodeStrings = new String[ currentOpcodes.length ];
					for (int i=0; i<currentOpcodes.length; i++) {
						opcodeStrings[i] = currentOpcodes[i].toString();
					}
					performanceMap.put(PerformanceKey.CURRENTLY_EXECUTED_OPERATION_CODE, opcodeStrings);
				}
			}

			return performanceMap;
		}
	}
}

