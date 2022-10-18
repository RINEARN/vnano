package org.vcssl.nano;

import java.util.Locale;
import org.vcssl.nano.spec.ErrorMessage;
import org.vcssl.nano.spec.ErrorType;

/**
 * The exception class thrown when an abnormal condition/problem has been detected in the scripting engine.
 *
 * For example, this exception will be thrown when it is strongly presumed that there is an incorrect something (e.g.: bugs)
 * in the implementation code of the scripting engine.
 *
 * In addition,
 * this exception will also be thrown when the operation of the scripting engine by the application is not following the expected (correct) procedure.
 *
 * For example, if the application calls {@link org.vcssl.nano.VnanoEngine#terminateScript() terminateScript()}> method of the engine
 * although {@link org.vcssl.nano.VnanoEngine#isTerminatorEnabled() isTerminatorEnabled()} method returns "false",
 * then this exception occurs.
 *
 * In principle, when implementations of both the scripting engine and the application are correct, this exception does not occur.
 * Conversely, if this error has occurred, it probably indicates that it requires to fix the implementation of the scripting engine or the application.
 * Hence, it is not recommended that "catch" this exception and ignore it casually.
 * For the above reason, this exception is designed as an "unchecked exception", which does not require to be encrosed by try/catch statements.
 *
 * See also: {@link org.vcssl.nano.VnanoException VnanoException}.
 */

/**
 * The exception class thrown when the unexpected problem occurred in the script engine
 */
@SuppressWarnings("serial")
public class VnanoFatalException extends RuntimeException {

	public VnanoFatalException() {
		super();
	}
	public VnanoFatalException(ErrorType errorType) {
		super(ErrorMessage.generateErrorMessage(errorType, null, Locale.getDefault()));
	}
	public VnanoFatalException(ErrorType errorType, String[] errorWords) {
		super(ErrorMessage.generateErrorMessage(errorType, errorWords, Locale.getDefault()));
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
