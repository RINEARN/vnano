package org.vcssl.nano;

//Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/VnanoFatalException.html
//ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/VnanoFatalException.html

/**
 * <span class="lang-en">
 * The exception class thrown when the unexpected problem occurred in the script engine
 * </span>
 * <span class="lang-ja">
 * スクリプトエンジン内で, 何らかの予期しない異常な状態が検出された際にスローされる例外です
 * </span>
 * .
 *
 * <p>
 * &raquo; <a href="../../../../src/org/vcssl/nano/VnanoFatalException.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../api/org/vcssl/nano/VnanoFatalException.html">Public Only</a>
 * | <a href="../../../../api-all/org/vcssl/nano/VnanoFatalException.html">All</a> |
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
