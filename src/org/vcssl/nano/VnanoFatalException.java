package org.vcssl.nano;

/**
 * 実装上の異常に起因する例外です。
 * この例外が発生した場合は、処理系が開発時に想定されていない状態や挙動に陥った事を意味しているため、
 * 処理系の実装を修正する必要があります。
 * 処理系が実装が正常であれば、実行するスクリプトの内容によらず、この例外は本来発生しないべきです。
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
