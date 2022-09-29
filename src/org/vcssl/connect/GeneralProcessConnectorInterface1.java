/*
 * ==================================================
 * General Process Connector Interface 1 (GPCI 1)
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2017-2022 by RINEARN
 * ==================================================
 */

package org.vcssl.connect;

/**
 * An legacy interface for implementing function plug-ins, supported by only the scripting engine of VCSSL.
 */
public interface GeneralProcessConnectorInterface1 {

	/**
	 * Returns whether this plug-in can process the function having the specified name.
	 * 
	 * @param functionName The name of the function.
	 * @return Returns true if this plug-in can process the function having the specified name.
	 */
	public boolean isProcessable(String functionName);


	/**
	 * Processes the function having the spacified name.
	 * 
	 * @param functionName The name of the function to be processed.
	 * @param arguments The array storing values of all actual arguments.
	 * @return The array storing return value of the function.
	 */
	public String[] process(String functionName, String[] arguments);
}
