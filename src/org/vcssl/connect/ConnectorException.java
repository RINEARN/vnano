/*
 * ==================================================
 * Connector Exception
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2019-2022 by RINEARN
 * ==================================================
 */

// THE STATUS OF THE SPECIFICATION OF THIS CLASS IS "EXTENSION ONLY".
// IN PRINCIPLE, DON'T DELETE EXISTING CONSTRUCTORS/METHODS/FIELDS.

package org.vcssl.connect;

/**
 * A checked Exception class thrown when errors have occurred, 
 * cause by expected normal problems (excluding bugs of implementations), such as an access failure to a file.
 * 
 * If the error is caused by incorrect implementations (bugs and so on) of plug-ins or scripting engines,
 * throw {@link ConnectorFatalException} instead, which is an unchecked Exception.
 */
@SuppressWarnings("serial")
public class ConnectorException extends Exception {

	/**
	 * Creates a ConnectorException having no error message.
	 */
	public ConnectorException() {
	}

	/**
	 * Creates a ConnectorException having the specified error message.
	 * 
	 * @param errorMessage The error message.
	 */
	public ConnectorException(Throwable wrappedThrowable) {
		super(wrappedThrowable);
	}

	/**
	 * Creates a ConnectorException wrapping the specified Throwable.
	 * 
	 * @param wrappedThrowable The throwable of the cause, to be wrapped by the created ConnectorException.
	 */
	public ConnectorException(String errorMessage) {
		super(errorMessage);
	}

	/**
	 * Creates a ConnectorException having the specified error message, and wrapping the specified Throwable.
	 * 
	 * @param errorMessage The error message.
	 * @param wrappedThrowable The throwable of the cause, to be wrapped by the created ConnectorException.
	 */
	public ConnectorException(String errorMessage, Throwable wrappedThrowable) {
		super(errorMessage, wrappedThrowable);
	}
}
