package org.vcssl.nano;

/**
 * The exception class thrown when the unexpected problem occurred in the script engine
 */
@SuppressWarnings("serial")
public class VnanoFatalException extends RuntimeException {

	public VnanoFatalException() {
		super();
	}
	public VnanoFatalException(String errorMessage) {
		super(errorMessage);
	}
	public VnanoFatalException(Throwable errorCauseThrowable) {
		super(errorCauseThrowable);
	}
	public VnanoFatalException(String errorMessage, Throwable errorCauseThrowable) {
		super(errorMessage, errorCauseThrowable);
	}
}
