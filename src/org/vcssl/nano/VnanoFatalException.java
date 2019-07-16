package org.vcssl.nano;


/**
 * <span class="lang-ja">
 * スクリプトエンジン内で, 何らかの予期しない異常な状態が検出された際にスローされる例外です
 * </span>
 * <span class="lang-en">
 * The exception class thrown when the unexpected problem occurred in the script engine
 * </span>
 * .
 *
 * <p>
 * &raquo <a href="../../../../src/org/vcssl/nano/VnanoFatalException.java">Source code</a>
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
@SuppressWarnings("serial")
public class VnanoFatalException extends RuntimeException {

	public VnanoFatalException() {
		super();
	}
	public VnanoFatalException(String message) {
		super(message);
	}
	public VnanoFatalException(Throwable cause) {
		super(cause);
	}
	public VnanoFatalException(String message, Throwable cause) {
		super(message, cause);
	}
}
