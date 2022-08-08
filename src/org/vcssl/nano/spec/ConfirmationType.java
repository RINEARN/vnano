/*
 * Copyright(C) 2020-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

/**
 * The enum to distinguish types of confirmation messages.
 */
public enum ConfirmationType {

	/** Represents the message confirming whether the user allow the requested permission. */
	PERMISSION_REQUESTED,

	/**
	 * Represents the message confirming whether the user want to allow automatically the same kind of permission,
	 * which had already been allowed in the currently running script.
	 */
	ALLOW_SAME_PERMISSION_AUTOMATICALLY,
}
