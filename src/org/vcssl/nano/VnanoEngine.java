/*
 * Copyright(C) 2019-2022 RINEARN
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
import org.vcssl.nano.spec.IdentifierSyntax;
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

	/** Stores contents of library scripts, with their names as keys. */
	private Map<String, String> libraryNameContentMap = null;


	/**
	 * Create a Vnano Engine with default settings.
	 */
	public VnanoEngine() {
		this.libraryNameContentMap = new LinkedHashMap<String, String>();
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

		try {

			// Remove the encoding declaration if exists, and normalize environment-dependent contents, and so on.
			try {
				script = MetaQualifiedFileLoader.postprocess(null, script); // The first arg is the file name to display in error messages.
			} catch (VnanoException vne) {
				String message = vne.getMessageWithoutLocation();
				throw new ScriptException(message);
			}

			// Activate the interconnect, for executing the script.
			// (All connected plug-ins are initialized at this timing.)
			this.interconnect.activate();

			// Store the specified (main) script and library scripts into an array.
			int libN = this.libraryNameContentMap.size();
			String[] scripts = new String[libN  + 1];
			String[] names   = new String[libN + 1];
			names[libN] = (String)this.interconnect.getOptionMap().get(OptionKey.MAIN_SCRIPT_NAME); // This name is already been normalized.
			scripts[libN] = script;
			int libIndex = 0;
			for (Map.Entry<String, String> nameContentPair: this.libraryNameContentMap.entrySet()) {
				names[libIndex] = IdentifierSyntax.normalizeScriptIdentifier( nameContentPair.getKey() ); // This name has not been normalized yet, so normalize.
				scripts[libIndex] = nameContentPair.getValue();
				// The name of the main script must not duplicate with the names of the library scripts.
				if (names[libIndex].equals(names[libN])) {
					throw new VnanoException(ErrorType.LIBRARY_SCRIPT_NAME_IS_CONFLICTING_WITH_MAIN_SCRIPT_NAME, names[libIndex]);
				}
				libIndex++;
			}

			// Translate scripts to a VRIL code (intermediate assembly code) by a compiler.
			String assemblyCode = new Compiler().compile(scripts, names, this.interconnect);

			// Execute the VRIL code on a VM.
			Object evalValue = this.virtualMachine.executeAssemblyCode(assemblyCode, this.interconnect);

			// Deactivate the interconnect.
			// (All Connected plug-ins are finalized at this timing.)
			this.interconnect.deactivate();

			return evalValue;

		// If any error is occurred for the content/processing of the script,
		// set the locale to switch the language of error messages, and re-throw the exception to upper layers.
		} catch (VnanoException e) {
			Locale locale = (Locale)this.interconnect.getOptionMap().get(OptionKey.LOCALE); // Type was already checked.
			e.setLocale(locale);

			// もしも VnanoException が、外部関数が投げる ConnectorException をラップしている場合で、
			if (e.getCause() instanceof ConnectorException && ((ConnectorException)e.getCause()).getMessage().startsWith("___")) {
				this.handleSpecialConnectorException((ConnectorException)e.getCause(), e);
				return null; // 上の行で VnanoException が再スローされなかった場合は何もしない（ exit 関数での終了など ）
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
	 * Terminates the currently running script as soon as possible.
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
	 * @throws VnanoException
	 *       Thrown when the option {@link org.vcssl.spec.OptionKey#TERMINATOR_ENABLED} is disabled.
	 */
	public void terminateScript() throws VnanoException {
		if (! (boolean)this.interconnect.getOptionMap().get(OptionKey.TERMINATOR_ENABLED) ) {
			throw new VnanoException(ErrorType.TERMINATOR_IS_DISABLED);
		}
		this.virtualMachine.terminate();
	}


	/**
	 * Resets the engine which had terminated by {@link VnanoEngine#terminateScript()} method, 
	 * for processing new scripts.
	 * 
	 * Please note that, if an execution of code is requested by another thread
	 * when this method is being processed, the execution request might be missed.
	 * 
	 * @throws VnanoException
	 *       Thrown when the option {@link org.vcssl.spec.OptionKey#TERMINATOR_ENABLED} is disabled.
	 */
	public void resetTerminator() throws VnanoException {
		if (! (boolean)this.interconnect.getOptionMap().get(OptionKey.TERMINATOR_ENABLED) ) {
			throw new VnanoException(ErrorType.TERMINATOR_IS_DISABLED);
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
		this.interconnect.connectPlugin(bindingName, plugin);
	}


	/**
	 * Disconnects all plug-ins.
	 * 
	 * @throws VnanoException Thrown when an exception occurred on the finalization of the plug-in to be disconnected.
	 */
	public void disconnectAllPlugins() throws VnanoException {
		this.interconnect.disconnectAllPlugins();
	}


	/**
	 * Add a library script which will be "include"-ed at the head of a executed script.
	 * 
	 * @param libraryScriptName Names of the library script (displayed in error messages).
	 * @param libraryScriptContent Content (code) of the library script.
	 * @throws VnanoException Thrown when incorrect somethings have been detected for the specified library.
	 */
	public void includeLibraryScript(String libraryScriptName, String libraryScriptContent) throws VnanoException {
		if (libraryScriptName == null || libraryScriptContent == null) {
			throw new NullPointerException();
		}
		if (this.libraryNameContentMap.containsKey(libraryScriptName)) {
			throw new VnanoException(ErrorType.LIBRARY_IS_ALREADY_INCLUDED, libraryScriptName);
		}
		this.libraryNameContentMap.put(libraryScriptName, libraryScriptContent);
	}


	/**
	 * Uninclude all library scripts.
	 * 
	 * @throws VnanoException
	 *   Will not be thrown on the current implementation,
	 *   but it requires to be "catch"-ed for keeping compatibility in future.
	 */
	public void unincludeAllLibraryScripts() throws VnanoException {
		this.libraryNameContentMap = new LinkedHashMap<String, String>();
	}


	/**
	 * Sets options, by a Map (option map) storing names and values of options you want to set.
	 * 
	 * Type of the option map is Map<String,Object>, and its keys represents option names.
	 * For details of option names and values,
	 * see {@link org.vcssl.nano.spec.OptionKey} and {@link org.vcssl.nano.spec.OptionValue}.
	 *
	 * @param optionMap The Map (option map) storing names and values of options.
	 * @throws VnanoException Thrown if invalid option settings is detected.
	 */
	public void setOptionMap(Map<String,Object> optionMap) throws VnanoException {
		if (optionMap == null) {
			throw new NullPointerException();
		}
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
	 */
	public Map<String,Object> getOptionMap() {
		return this.interconnect.getOptionMap();
	}


	/**
	 * Sets permissions, by a Map (permission map) storing names and values of permission items you want to set.
	 * 
	 * Type of the permission map is Map<String,String>, and its keys represents names of permission items.
	 * For details of names and values of permission items,
	 * see {@link org.vcssl.connect.ConnectorPermissionName} and {@link org.vcssl.connect.ConnectorPermissionValue}.
	 *
	 * @param permissionMap The Map (permission map) storing names and values of permission items.
	 *
	 * @throws VnanoException Thrown if invalid permission settings is detected.
	 */
	public void setPermissionMap(Map<String, String> permissionMap) throws VnanoException {
		if (permissionMap == null) {
			throw new NullPointerException();
		}
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
	 * @throws VnanoException Thrown when the read-access to the permission map has been denied.
	 */
	public Map<String, String> getPermissionMap() throws VnanoException {
		return this.interconnect.getPermissionMap();
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
	 *
	 * @throws VnanoException
	 *   Thrown when the option {@link org.vcssl.nano.spec.OptionKey#PERFORMANCE_MONITOR_ENABLED PERFORMANCE_MONITOR_ENABLED} is disabled.
	 */
	public Map<String, Object> getPerformanceMap() throws VnanoException {
		synchronized (this) {
			if (! (boolean)this.interconnect.getOptionMap().get(OptionKey.PERFORMANCE_MONITOR_ENABLED) ) {
				throw new VnanoException(ErrorType.PERFORMANCE_MONITOR_IS_DISABLED);
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

