/*
 * Copyright(C) 2020-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

/**
 * The class defining special binding values.
 */
public class SpecialBindingValue {

	/**
	 * When specified as an argument of {@link org.vcssl.nano.VnanoScriptEngine#put(String, Object)} 
	 * method with the key {@link SpecialBindingKey#COMMAND},
	 * the engine removes all plug-ins.
	 */
	public static final String COMMAND_REMOVE_PLUGIN = "REMOVE_PLUGIN";

	/**
	 * When specified as an argument of {@link org.vcssl.nano.VnanoScriptEngine#put(String, Object)} 
	 * method with the key {@link SpecialBindingKey#COMMAND},
	 * the engine un-includes all library scripts.
	 */
	public static final String COMMAND_REMOVE_LIBRARY = "REMOVE_LIBRARY";

	/**
	 * When specified as an argument of {@link org.vcssl.nano.VnanoScriptEngine#put(String, Object)} 
	 * method with the key {@link SpecialBindingKey#COMMAND},
	 * the engine reloads all plug-ins.
	 */
	public static final String COMMAND_RELOAD_PLUGIN = "RELOAD_PLUGIN";

	/**
	 * When specified as an argument of {@link org.vcssl.nano.VnanoScriptEngine#put(String, Object)} 
	 * method with the key {@link SpecialBindingKey#COMMAND},
	 * the engine reloads all library scripts.
	 */
	public static final String COMMAND_RELOAD_LIBRARY = "RELOAD_LIBRARY";

	/**
	 * When specified as an argument of {@link org.vcssl.nano.VnanoScriptEngine#put(String, Object)} 
	 * method with the key {@link SpecialBindingKey#COMMAND},
	 * the engine terminates the currently running script.
	 */
	public static final String COMMAND_TERMINATE_SCRIPT = "TERMINATE_SCRIPT";

	/**
	 * When specified as an argument of {@link org.vcssl.nano.VnanoScriptEngine#put(String, Object)} 
	 * method with the key {@link SpecialBindingKey#COMMAND},
	 * the engine resets the currently running script.
	 */
	public static final String COMMAND_SESET_TERMINATOR = "RESET_TERMINATOR";
}
