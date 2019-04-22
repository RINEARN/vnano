/*
 * ==================================================
 * Connector Implementation Exception
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2019 by RINEARN (Fumihiro Matsui)
 * ==================================================
 */

package org.vcssl.connect;

@SuppressWarnings("serial")
public class ConnectorImplementationException extends Exception {
	public ConnectorImplementationException(String errorMessage) {
		super(errorMessage);
	}
	public ConnectorImplementationException(String errorMessage, Exception causeException) {
		super(errorMessage, causeException);
	}
}
