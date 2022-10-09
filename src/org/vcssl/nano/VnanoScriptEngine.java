/*
 * Copyright(C) 2017-2022
 * This software is released under the MIT License.
 */

package org.vcssl.nano;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.vcssl.nano.interconnect.PluginLoader;
import org.vcssl.nano.interconnect.ScriptLoader;
import org.vcssl.nano.spec.EngineInformation;
import org.vcssl.nano.spec.SpecialBindingKey;
import org.vcssl.nano.spec.SpecialBindingValue;

/**
 * The wrapper class of {@link VnanoEngine VnanoEngine} class to use it
 * through "ScriptEngine" interface of the Scripting API of the standard library.
 */
public final class VnanoScriptEngine implements ScriptEngine {

	private static final String DEFAULT_ENCODING = "UTF-8";


	/** A Vnano Engine to be wrapped by ScriptEngine interface. */
	private VnanoEngine vnanoEngine = null;

	/** A loader for loading library scripts from files. */
	private ScriptLoader libraryScriptLoader = null;

	/** A loader for loading plug-ins from files. */
	private PluginLoader pluginLoader = null;

	/** Stores plug-ins registerd by "put" method to be connected. */
	private Bindings putPluginBindings = null;

	/** Stores whether plug-ins are added by "put" method after the previous execution. */
	private boolean putPluginBindingsUpdated = false;

	/** Stores whether plug-ins are added/removed by re-loadings after the previous execution. */
	private boolean loadedPluginUpdated = false;


	/** Stores whether library scripts are added/removed by re-loadings after the previous execution. */
	private boolean loadedLibraryUpdated = false;


	/**
	 * Create an script engine of the Vnano, however,
	 * use "getEngineByName" method of "ScriptEngineManager" class with an argument "Vnano",
	 * instead of using this constructor directly
	 * 
	 * If you want to create and use an instance of the script engine of the Vnano directly,
	 * use {@link VnanoEngine VnanoEngine}.
	 * This class is its wrapper for using it through the Scripting API of the standard library.
	 */
	protected VnanoScriptEngine() {
		try {
			this.putPluginBindings = new SimpleBindings(new LinkedHashMap<String, Object>());
			this.libraryScriptLoader = new ScriptLoader(DEFAULT_ENCODING);
			this.pluginLoader = new PluginLoader(DEFAULT_ENCODING);
			this.vnanoEngine = new VnanoEngine();

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}


	/**
	 * Executes an expression or a script code passed as an argument.
	 * 
	 * @param scriptCode The expression or the script code to execute.
	 * @return The evaluated value of the expression, or last expression statement in the script code.
	 * @throws ScriptException Thrown when an error will be detected for the content or the processing of the script.
	 */
	@Override
	public Object eval(String scriptCode) throws ScriptException {
		if (scriptCode == null) {
			throw new NullPointerException();
		}

		try {

			// Register plug-ins and library scripts to the engine.
			this.updatePluginConnections();
			this.updateLibraryInclusions();

			// Execute the script.
			Object value = this.vnanoEngine.executeScript(scriptCode);
			return value;

		// If a VnanoException has occurred, wrap it by a ScriptException and rethrow,
		} catch (VnanoException vnanoException) {

			// Get the error message without the line-number, because it will be appended by the ScriptException.
			String message = vnanoException.getMessageWithoutLocation();

			// If the error message exists, create an ScriptException from the message, and set the VnanoException as the cause.
			if (message != null) {
				ScriptException scriptException = null;
				if (vnanoException.hasFileName() && vnanoException.hasLineNumber()) {
					scriptException = new ScriptException(
						message + ":", vnanoException.getFileName(), vnanoException.getLineNumber()
					);
				} else {
					scriptException = new ScriptException(message);
				}

				try {
					scriptException.initCause(vnanoException);
				} catch (IllegalStateException ise) {
					// If the ScriptException already has any cause, this IllegalStateException will be thrown.
					// But we consider that it never occurs here.
				}

				throw scriptException;

			// If the VnanoException has no error message, simply wrap it by ScriptException and rethrow.
			} else {
				throw new ScriptException(vnanoException);
			}

		// Wrap and rethrow other kinds of Exceptions.
		} catch (Exception unexpectedException) {

			ScriptException scriptException = new ScriptException(unexpectedException);
			throw scriptException;
		}
	}


	/**
	 * Executes an expression or a script code read from Reader (for example: FileReader).
	 * .
	 * @param reader The Reader instance to read script code.
	 * @return The evaluated value of the expression, or last expression statement in the script code (or, null if it does not exist).
	 * @throws ScriptException Thrown when an error will be detected for the content or the processing of the script.
	 */
	@Override
	public Object eval(Reader reader) throws ScriptException {
		if (reader == null) {
			throw new NullPointerException();
		}

		try {
			StringBuilder builder = new StringBuilder();
			int charcode = -1;
			while ((charcode = reader.read()) != -1) {
				builder.append((char)charcode);
			}
			String script = builder.toString();
			return this.eval(script);

		} catch (IOException ioe) {
			throw new ScriptException(ioe);
		}
	}


	/**
	 * Updates connections between the VnanoEngine and plug-ins stored in fields of this class.
	 * 
	 * @throws VnanoException Thrown if plug-ins could not be connected,
	 *     caused by unsupported interfaces, incompatibility of data types, and so on.
	 */
	private void updatePluginConnections() throws VnanoException {

		// Skip updating if there is no need to do it.
		if (!this.putPluginBindingsUpdated && !this.loadedPluginUpdated) {
			return;
		}
		this.putPluginBindingsUpdated = false;
		this.loadedPluginUpdated = false;

		// Disconnect all plug-ins.
		this.vnanoEngine.disconnectAllPlugins();

		// Re-connect plug-ins loaded from files.
		if (this.pluginLoader.hasPlugins()) {
			String[] loadedPluginNames = this.pluginLoader.getPluginNames();
			Object[] loadedPluginInstances = this.pluginLoader.getPluginInstances();
			for (int pluginIndex=0; pluginIndex<loadedPluginNames.length; pluginIndex++) {
				//this.vnanoEngine.connectPlugin(loadedPluginNames[pluginIndex], loadedPluginInstances[pluginIndex]);
				this.vnanoEngine.connectPlugin(SpecialBindingKey.AUTO_KEY, loadedPluginInstances[pluginIndex]); // キーは文法に則っていないといけない
			}
		}

		// (Re-)Connect plug-ins directly registered by "put" method.
		// (Connect after the plug-ins loaded from files has been connected, because directly-put plug-ins has higher precedences.)
		for (Entry<String,Object> pair: this.putPluginBindings.entrySet()) {
			this.vnanoEngine.connectPlugin(pair.getKey(), pair.getValue());
		}
	}


	/**
	 * Updates "include"-registrations between the VnanoEngine and libraries stored in fields of this class.
	 * 
	 * @throws VnanoException Thrown if libraries could not be included, caused by "duplicate include" and so on.
	 */
	private void updateLibraryInclusions() throws VnanoException {

		// Skip updating if there is no need to do it.
		if (!this.loadedLibraryUpdated) {
			return;
		}
		this.loadedLibraryUpdated = false;

		// Remove library scripts.
		this.vnanoEngine.unincludeAllLibraryScripts();

		// (Re-)Include library scripts.
		if(this.libraryScriptLoader.hasLibraryScripts()) {
			String[] libNames = this.libraryScriptLoader.getLibraryScriptNames();
			String[] libContents = this.libraryScriptLoader.getLibraryScriptContents();
			int libN = libNames.length;
			for (int libIndex=0; libIndex<libN; libIndex++) {
				this.vnanoEngine.includeLibraryScript(libNames[libIndex], libContents[libIndex]);
			}
		}
	}


	/**
	 * Connects an external function/variable, or sets options/permission, or takes some special operations, and so on.
	 * <br>
	 * If the value of the argument "name" is "___VNANO_OPTION_MAP",
	 * this method behaves as a wrapper of {@link VnanoEngine#setOptionMap(Map) VnanoEngine.setOptionMap(Map)}
	 * method, which is to set options.
	 * <br>
	 * If the value of the argument "name" is "___VNANO_PERMISSION_MAP",
	 * this method behaves as a wrapper of {@link VnanoEngine#setPermissionMap(Map) VnanoEngine.setPermissionMap(Map)}
	 * method, which is to set permissions.
	 * <br>
	 * If the value of the argument "name" is "___VNANO_LIBRARY_LIST_FILE",
	 * this method loads library scripts of which paths are described in the specified list file,
	 * and register them  to be "include"-ed in the execution script by the engine.
	 * <br>
	 * If the value of the argument "name" is "___VNANO_PLUGIN_LIST_FILE",
	 * this method loads plug-ins of which paths are described in the specified list file,
	 * and connect them to the engine.
	 * <br>
	 * If the value of the argument "name" is "___VNANO_COMMAND", this method invokes special commands of the engine.
	 * Available commands are defined in {@link org.vcssl.nano.spec.SpecialBindingValue}.
	 * <br>
	 * Other than the above, this method behaves as a wrapper of
	 * {@link VnanoEngine#connectPlugin(String,Object) VnanoEngine.connectPlugin(String, Object)}
	 * method, which is to connect instances of plug-ins.
	 * In this case, the argument "name" will be a name in scripts of
	 * the variable/function/namespace provided by the connected plug-in.
	 * If the argument "name" contains a white space or a character "(", the content after it will be ignored.
	 * By the above specification, for a function plug-in,
	 * you can specify a signature containing parameter-declarations like "foo(int,float)"
	 * (note that, syntax or correctness of parameter-declarations will not be checked).
	 * In addition, for plug-ins providing elements belonging to the same namespace "Bar",
	 * you can specify "Bar 1", "Bar 2", and so on.
	 * This is helpful to avoid the duplication of keys when you use
	 * {@link org.vcssl.nano.VnanoScriptEngine#put(String, Object) VnanoScriptEngine.put(String, Object) }
	 * method which wraps this method.
	 * Also, you can specify "___VNANO_AUTO_KEY" for using a valid value generated automatically.
	 *
	 * @param name See the above description.
	 * @param value See the above description.
	 */
	@Override
	public void put(String name, Object value) {
		if (name == null || value == null) {
			throw new NullPointerException();
		}

		// OptionMap:
		if (name.equals(SpecialBindingKey.OPTION_MAP)) {
			if (value instanceof Map) {

				@SuppressWarnings("unchecked")
				Map<String, Object> castedMap = (Map<String, Object>)value;
				try {
					this.vnanoEngine.setOptionMap(castedMap);
				} catch (VnanoException e) {
					throw new VnanoFatalException(e);
				}

			} else {
				throw new VnanoFatalException(
					"The type of \"" + SpecialBindingKey.OPTION_MAP + "\" should be \"Map<String,Object>\""
				);
			}

		// PermissionMap:
		} else if (name.equals(SpecialBindingKey.PERMISSION_MAP)) {
			if (value instanceof Map) {

				@SuppressWarnings("unchecked")
				Map<String, String> castedMap = (Map<String, String>)value;
				try {
					this.vnanoEngine.setPermissionMap(castedMap);
				} catch (VnanoException e) {
					throw new VnanoFatalException(e);
				}

			} else {
				throw new VnanoFatalException(
					"The type of \"" + SpecialBindingKey.PERMISSION_MAP + "\" should be \"Map<String,String>\""
				);
			}

		// Commands:
		} else if (name.equals(SpecialBindingKey.COMMAND)){
			if (!(value instanceof String)) {
				throw new VnanoFatalException("Invalid command (should be a String)");
			}
			this.handleCommands((String)value);

		// Library list file:
		} else if (name.equals(SpecialBindingKey.LIBRARY_LIST_FILE)) {
			this.libraryScriptLoader.setLibraryScriptListPath((String)value);
			this.loadLibraries();

		// Plug-in list file:
		} else if (name.equals(SpecialBindingKey.PLUGIN_LIST_FILE)) {
			this.pluginLoader.setPluginListPath((String)value);
			this.loadPlugins();

		// Plug-in instance:
		} else {
			this.putPluginBindings.put(name, value);
			this.putPluginBindingsUpdated = true;
		}
	}

	private void loadPlugins() {
		try {
			this.pluginLoader.load();
			this.loadedPluginUpdated = true;
		} catch (VnanoException e) {
			throw new VnanoFatalException("Plugin loading failed", e);
		}
	}

	private void loadLibraries() {
		try {
			this.libraryScriptLoader.load();
			this.loadedLibraryUpdated = true;
		} catch (VnanoException e) {
			throw new VnanoFatalException("Library loading failed", e);
		}
	}

	private void handleCommands(String commandName) {
		switch (commandName) {

			case SpecialBindingValue.COMMAND_REMOVE_PLUGIN : {
				try {
					this.vnanoEngine.disconnectAllPlugins();
				} catch (VnanoException e) {
					throw new VnanoFatalException(e);
				}
				this.pluginLoader = new PluginLoader(DEFAULT_ENCODING);
				this.loadedPluginUpdated = true;
				break;
			}

			case SpecialBindingValue.COMMAND_REMOVE_LIBRARY : {
				this.libraryScriptLoader = new ScriptLoader(DEFAULT_ENCODING);
				this.loadedLibraryUpdated = true;
				break;
			}

			case SpecialBindingValue.COMMAND_RELOAD_PLUGIN : {
				this.loadPlugins();
				break;
			}

			case SpecialBindingValue.COMMAND_RELOAD_LIBRARY : {
				this.loadLibraries();
				break;
			}

			case SpecialBindingValue.COMMAND_TERMINATE_SCRIPT : {
				this.vnanoEngine.terminateScript();
				break;
			}

			case SpecialBindingValue.COMMAND_SESET_TERMINATOR : {
				this.vnanoEngine.resetTerminator();
				break;
			}

			default : {
				throw new VnanoFatalException("Unknown command: " + commandName);
			}
		}
	}


	/**
	 * Gets information values of the engine/language (e.g.: version), performance monitoring values,
	 * or the value which is put by {@link VnanoScriptEngine#put put} method.
	 */
	@Override
	public Object get(String name) {
		if (name == null) {
			throw new NullPointerException();
		}
		if (name.equals(SpecialBindingKey.PERFORMANCE_MAP)) {
			return this.vnanoEngine.getPerformanceMap();
		}
		if (name.equals(ScriptEngine.NAME)) {
			return EngineInformation.LANGUAGE_NAME;
		}
		if (name.equals(ScriptEngine.LANGUAGE)) {
			return EngineInformation.LANGUAGE_NAME;
		}
		if (name.equals(ScriptEngine.LANGUAGE_VERSION)) {
			return EngineInformation.LANGUAGE_VERSION;
		}
		if (name.equals(ScriptEngine.ENGINE)) {
			return EngineInformation.ENGINE_NAME;
		}
		if (name.equals(ScriptEngine.ENGINE_VERSION)) {
			return EngineInformation.ENGINE_VERSION;
		}
		return this.putPluginBindings.get(name);
	}


	/**
	 * Unsupported on this script engine implementation.
	 */
	@Override
	public Object eval(String script, Bindings bindings) throws ScriptException {
		throw new VnanoFatalException("This feature is unsupported");
	}


	/**
	 * Unsupported on this script engine implementation.
	 */
	@Override
	public Object eval(String script, ScriptContext context) throws ScriptException {
		throw new VnanoFatalException("This feature is unsupported");
	}

	/**
	 * Unsupported on this script engine implementation.
	 */
	@Override
	public Object eval(Reader reader, ScriptContext context) throws ScriptException {
		throw new VnanoFatalException("This feature is unsupported");
	}


	/**
	 * Unsupported on this script engine implementation.
	 */
	@Override
	public Object eval(Reader reader, Bindings bindings) throws ScriptException {
		throw new VnanoFatalException("This feature is unsupported");
	}


	/**
	 * Unsupported on this script engine implementation.
	 */
	@Override
	public Bindings getBindings(int scope) {
		// If we throw the below, we can't get an instance through the Scripting API.
		// throw new VnanoFatalException("This feature is unsupported");
		return null;
	}


	/**
	 * Unsupported on this script engine implementation.
	 */
	@Override
	public void setBindings(Bindings bind, int scope) {
		if (bind == null) {
			throw new NullPointerException();
		}
		// If we throw the below, we can't get an instance through the Scripting API.
		// throw new VnanoFatalException("This feature is unsupported");
	}


	/**
	 * Unsupported on this script engine implementation.
	 */
	@Override
	public Bindings createBindings() {
		// If we throw the below, we can't get an instance through the Scripting API.
		// throw new VnanoFatalException("This feature is unsupported");
		return null;
	}


	/**
	 * Unsupported on this script engine implementation.
	 */
	@Override
	public ScriptContext getContext() {
		// If we throw the below, we can't get an instance through the Scripting API.
		// throw new VnanoFatalException("This feature is unsupported");
		return null;
	}


	/**
	 * Unsupported on this script engine implementation.
	 */
	@Override
	public void setContext(ScriptContext context) {
		// If we throw the below, we can't get an instance through the Scripting API.
		// throw new VnanoFatalException("This feature is unsupported");
	}


	/**
	 * Unsupported on this script engine implementation.
	 */
	@Override
	public ScriptEngineFactory getFactory() {
		return new VnanoScriptEngineFactory();
	}

}
