/*
 * ==================================================
 * Connector Parmission Value
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2020-2022 by RINEARN
 * ==================================================
 */

// THE STATUS OF THE SPECIFICATION OF THIS CLASS IS "EXTENSION ONLY".
// IN PRINCIPLE, DON'T DELETE EXISTING CONSTRUCTORS/METHODS/FIELDS.

package org.vcssl.connect;


/**
 * A class defining permission values.
 *
 * Fields of this class are natural to be defined as elements of an enum, 
 * however, they are defined as "public static final String" fields, 
 * instead of enum elements.
 * This is to prevent unexpected behaviour when definition order of them are changed, 
 * and plug-ins referencing them have not been re-compiled.
 * 
 * Please note that, it is not recommended to describe values of them directly (as String literals).
 * Refer fields of this class as possible as.
 */
public class ConnectorPermissionValue {

	/** Requests for permission items having this value will always be allowed. */
	public static final String ALLOW = "ALLOW";

	/** Requests for permission items having this value will always be denied. */
	public static final String DENY = "DENY";

	/** When permission items having this value are requested, the scripting engine asks the user whether allows it. */
	public static final String ASK = "ASK";
}
