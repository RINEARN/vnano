/*
 * ==================================================
 * XVCI Exception
 * ( for VCSSL / Vnano Plug-in Development )
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2017-2018 by RINEARN (Fumihiro Matsui)
 * ==================================================
 */

package org.vcssl.connect;

/**
 * <p>
 * XFCI 準拠の変数プラグインにおいて、アクセス時に生じた例外をラップし、処理系側に通知するための例外です。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
@SuppressWarnings("serial")
public class ExternalVariableException extends Exception {
	public ExternalVariableException() {
	}
	public ExternalVariableException(Throwable wrappedThrowable) {
		super(wrappedThrowable);
	}
	public ExternalVariableException(String errorMessage) {
		super(errorMessage);
	}
	public ExternalVariableException(String errorMessage, Throwable wrappedThrowable) {
		super(errorMessage, wrappedThrowable);
	}
}
