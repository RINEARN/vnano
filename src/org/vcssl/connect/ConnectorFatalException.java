/*
 * ==================================================
 * Connector Fatal Exception
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2022 by RINEARN
 * ==================================================
 */

// THE STATUS OF THE SPECIFICATION OF THIS CLASS IS "EXTENSION ONLY".
// IN PRINCIPLE, DON'T DELETE EXISTING CONSTRUCTORS/METHODS/FIELDS.

package org.vcssl.connect;


/**
 * An unchecked Exception class thrown when errors have occurred, 
 * caused by incorrect implementations (might be bugs) of plug-ins or scripting engines.
 * 
 * If the error is normally expected, 
 * and is NOT caused by incorrect implementations (bugs and so on) of plug-ins or scripting engines,
 * throw {@link ConnectorException} instead, which is a checked Exception.
 */
@SuppressWarnings("serial")
public class ConnectorFatalException extends RuntimeException {

	/**
	 * Creates a ConnectorFatalException having no error message.
	 */
	public ConnectorFatalException() {
		super();
	}

	/**
	 * Creates a ConnectorFatalException having the specified error message.
	 * 
	 * @param errorMessage The error message.
	 */
	public ConnectorFatalException(String errorMessage) {
		super(errorMessage);
	}

	/**
	 * Creates a ConnectorFatalException wrapping the specified Throwable.
	 * 
	 * @param wrappedThrowable The throwable of the cause, to be wrapped by the created ConnectorFatalException.
	 */
	public ConnectorFatalException(Throwable wrappedThrowable) {
		super(wrappedThrowable);
	}

	/**
	 * Creates a ConnectorFatalException having the specified error message, and wrapping the specified Throwable.
	 * 
	 * @param errorMessage The error message.
	 * @param wrappedThrowable The throwable of the cause, to be wrapped by the created ConnectorFatalException.
	 */
	public ConnectorFatalException(String errorMessage, Throwable wrappedThrowable) {
		super(errorMessage, wrappedThrowable);
	}
}

