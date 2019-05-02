/*
 * ==================================================
 * Connector Exception
 * ( for VCSSL / Vnano Plug-in Development )
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2019 by RINEARN (Fumihiro Matsui)
 * ==================================================
 */

package org.vcssl.connect;

/**
 * <p>
 * プラグインにおいて、実行時に生じた例外をラップし、処理系側に通知するための例外です。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
@SuppressWarnings("serial")
public class ConnectorException extends Exception {
	public ConnectorException() {
	}
	public ConnectorException(Throwable wrappedThrowable) {
		super(wrappedThrowable);
	}
	public ConnectorException(String errorMessage) {
		super(errorMessage);
	}
	public ConnectorException(String errorMessage, Throwable wrappedThrowable) {
		super(errorMessage, wrappedThrowable);
	}
}
