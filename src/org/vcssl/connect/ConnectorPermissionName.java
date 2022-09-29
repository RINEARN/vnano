/*
 * ==================================================
 * Connector Parmission Name
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2017-2022 by RINEARN
 * ==================================================
 */

// THE STATUS OF THE SPECIFICATION OF THIS CLASS IS "EXTENSION ONLY".
// IN PRINCIPLE, DON'T DELETE EXISTING CONSTRUCTORS/METHODS/FIELDS.

package org.vcssl.connect;


/**
 * A class defining names of permission items.
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
public class ConnectorPermissionName {

	/** The name of the meta item representing all permission items. */
	public static final String ALL = "ALL";

	/** The name of the meta item representing no permission item. */
	public static final String NONE = "NONE";

	/**
	 * The name of the meta item storing the default permission value.
	 * 
	 * For permission items of which values are not specified explicitly, 
	 * a default value (e.g. {@link ConnectorPermissionValue#DENY DENY}) will be set automatically.
	 * You can change that default value by setting the value to this meta permission item.
	 * For example, if you set the value {@link ConnectorPermissionValue#ASK ASK} to this permission item "DEFAULT", 
	 * the script engine will ask to the user when non-specified permissions are required.
	 */
	public static final String DEFAULT = "DEFAULT";

	/** The permission to exit the currently executed program (script).  */
	public static final String PROGRAM_EXIT = "PROGRAM_EXIT";

 	/** The permission to reset/restart the currently executed program (script). */
	public static final String PROGRAM_RESET = "PROGRAM_RESET";

	/** The permission to change the currently executed program (script). */
	public static final String PROGRAM_CHANGE = "PROGRAM_CHANGE";

	/** The permission to execute commands or other programs through the Operating System and so on. */
	public static final String SYSTEM_PROCESS = "SYSTEM_PROCESS";

	/** The permission to create a new directory (folder). */
	public static final String DIRECTORY_CREATE = "DIRECTORY_CREATE";

 	/** The permission to delete a directory (folder). */
	public static final String DIRECTORY_DELETE = "DIRECTORY_DELETE";

	/** The permission to get the list of files in a directory (folder). */
	public static final String DIRECTORY_LIST = "DIRECTORY_LIST";

	// note: The permission to change a directory name is substitutable by the combination of CREATE/DELETE/LIST permissions.

	/** The permission to create a new file. */
	public static final String FILE_CREATE = "FILE_CREATE";

	/** The permission to delete a file. */
	public static final String FILE_DELETE = "FILE_DELETE";

	/** The permission to write contents of a file. */
	public static final String FILE_WRITE = "FILE_WRITE";

 	/** The permission to read contents of a file. */
	public static final String FILE_READ = "FILE_READ";

	/** The permission to overwrite contents of a file. */
	public static final String FILE_OVERWRITE = "FILE_OVERWRITE";

	/** The permission to change information (last modified date, and so on) of a file. */
	public static final String FILE_INFORMATION_CHANGE = "FILE_INFORMATION_CHANGE";

	// note: The permission to change a file name is substitutable by the combination of CREATE/READ/WRITE/DELETE permissions.

}
