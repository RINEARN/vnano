/*
 * ================================================================
 * Scalar Data Accessor Interface 1 for Bool Type Data (Bool SDAI1)
 * ----------------------------------------------------------------
 * This file is released under CC0.
 * Written in 2020-2022 by RINEARN
 * ================================================================
 */

// THE SPECIFICATION OF THIS INTERFACE HAD BEEN FINALIZED AT 2022/08/31.
// NO MODIFICATIONS WILL BE APPLIED FOR THIS INTERFACE, EXCLUDING DOCUMENTS/COMMENTS.

package org.vcssl.connect;

/**
 * A data-I/O interface (abbreviated as Bool SDAI 1), mainly implemented by data container objects of language processor systems.
 *
 * In this org.vcssl.connect package, multiple data I/O interfaces are provided for passing/receiving data 
 * without any data-conversions, between script-engine-side and plug-in-side, if required.
 * 
 * In them, this interface Bool SDAI 1 provides I/O methods of a boolean-type scalar value.
 */
public interface BoolScalarDataAccessorInterface1 {


	/** The type ID of this interface (value: "BOOL_SDAI") referred when the plug-in will be loaded. */
	public static final String INTERFACE_TYPE_ID = "BOOL_SDAI";

	/** The generation of this interface (value: "1"). */
	public static final String INTERFACE_GENERATION = "1";


	/**
	 * Sets the boolean-type scalar value.
	 * 
	 * @param data The scalar value to be set.
	 */
	public abstract void setBoolScalarData(boolean data);


	/**
	 * Gets the boolean-type scalar value.
	 * 
	 * @return The scalar value.
	 */
	public abstract boolean getBoolScalarData();


	/**
	 * Returns whether any boolean-type scalar value can be gotten.
	 * 
	 * @return Returns true if any boolean-type scalar value can be gotton.
	 */
	public abstract boolean hasBoolScalarData();
}
