/*
 * Copyright(C) 2019-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

/**
 * The class defining special binding keys.
 */
public class SpecialBindingKey {

	/** The special key to generate valid key of a function/variable/namespace plug-in automatically. */
	public static final String AUTO_KEY = "___VNANO_AUTO_KEY";

	/** The special key to set the option map. */
	public static final String OPTION_MAP = "___VNANO_OPTION_MAP";

	/** The special key to set the permission map. */
	public static final String PERMISSION_MAP = "___VNANO_PERMISSION_MAP";

	/** The special key to get the performance map. */
	public static final String PERFORMANCE_MAP = "___VNANO_PERFORMANCE_MAP";

	/** The special key to set the library list file. */
	public static final String LIBRARY_LIST_FILE = "___VNANO_LIBRARY_LIST_FILE";

	/** The special key to set the plug-in list file. */
	public static final String PLUGIN_LIST_FILE = "___VNANO_PLUGIN_LIST_FILE";

	/** The special key to execute some commands defined in {@link SpecialBindingValue}. */
	public static final String COMMAND = "___VNANO_COMMAND";

}
