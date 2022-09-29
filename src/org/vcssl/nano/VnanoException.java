/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano;

import java.util.Locale;

import org.vcssl.nano.spec.ErrorMessage;
import org.vcssl.nano.spec.ErrorType;


/**
 * The exception class thrown when the (usual type of) error detected for contents or processing of scripts
 */
@SuppressWarnings("serial")
public class VnanoException extends Exception implements Cloneable {

	private static final int LINE_NUMBER_DEFAULT_VALUE = -1;

	private ErrorType errorType = null;
	private String fileName = null;
	private int lineNumber = LINE_NUMBER_DEFAULT_VALUE;
	private String[] errorWords = null;
	private String errorMessage = null;
	private Locale locale = Locale.getDefault();

	public VnanoException clone() {
		VnanoException clonedVnanoException = new VnanoException(this.getMessage(), this.getCause());
		clonedVnanoException.errorType = this.errorType;
		clonedVnanoException.fileName = this.fileName;
		clonedVnanoException.lineNumber = this.lineNumber;
		clonedVnanoException.errorMessage = this.errorMessage;
		clonedVnanoException.errorWords = new String[ this.errorWords.length ];
		System.arraycopy(this.errorWords, 0, clonedVnanoException.errorWords, 0, this.errorWords.length);
		clonedVnanoException.locale = this.locale;
		return clonedVnanoException;
	}

	// Used only in clone() method.
	// Don't change this to "public" method, because error messages of VnanoException(s) should not be hardcoded.
	private VnanoException(String message, Throwable errorCauseThrowable) {
		super(message, errorCauseThrowable);
	}

	public VnanoException(Throwable errorCauseThrowable) {
		super(errorCauseThrowable);
		this.errorType = ErrorType.UNEXPECTED;
	}

	public VnanoException(ErrorType errorType) {
		this(errorType, (String)null, -1);
	}

	public VnanoException(ErrorType errorType, Throwable cause) {
		this(errorType, (String)null, cause);
	}

	public VnanoException(ErrorType errorType, String fileName, int lineNumber) {
		this(errorType, (String)null, fileName, lineNumber);
	}

	public VnanoException(ErrorType errorType, String errorWord, String fileName, int lineNumber) {
		this(errorType, new String[] {errorWord}, fileName, lineNumber);
	}

	public VnanoException(ErrorType errorType, String errorWord) {
		this(errorType, new String[] {errorWord}, (String)null, -1);
	}

	public VnanoException(ErrorType errorType, String[] errorWords) {
		this(errorType, errorWords, (String)null, -1);
	}

	public VnanoException(ErrorType errorType, String[] errorWords, String fileName, int lineNumber) {
		super(ErrorMessage.generateErrorMessage(errorType, errorWords, Locale.getDefault())); // We want to get this Locale from settings but...
		this.errorType = errorType;
		this.errorWords = errorWords;
		this.fileName = fileName;
		this.lineNumber = lineNumber;
	}

	public VnanoException(ErrorType errorType, String errorWord, Throwable cause) {
		this(errorType, new String[] {errorWord}, cause);
	}

	public VnanoException(ErrorType errorType, String[] errorWords, Throwable cause) {
		this(errorType, errorWords, cause, (String)null, -1);
	}


	public VnanoException(ErrorType errorType, Throwable cause, String fileName, int lineNumber) {
		this(errorType, (String[])null, cause);
	}

	public VnanoException(ErrorType errorType, String[] errorWords, Throwable cause, String fileName, int lineNumber) {
		super(ErrorMessage.generateErrorMessage(errorType, errorWords, Locale.getDefault()), cause); // We want to get this Locale from settings but...
		this.errorType = errorType;
		this.errorWords = errorWords;
		this.fileName = fileName;
		this.lineNumber = lineNumber;
	}


	public ErrorType getErrorType() {
		return this.errorType;
	}

	public void setErrorType(ErrorType errorType) {
		this.errorType = errorType;
	}

	public String[] getErrorWords() {
		return this.errorWords;
	}

	public void setErrorWords(String[] errorWords) {
		this.errorWords = errorWords;
	}

	public boolean hasFileName() {
		return this.fileName != null;
	}

	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public boolean hasLineNumber() {
		return this.lineNumber != LINE_NUMBER_DEFAULT_VALUE;
	}

	public int getLineNumber() {
		return this.lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public void setMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public String getMessage() {
		String message = this.getMessageWithoutLocation();
		if (this.hasFileName() && this.hasLineNumber()) {

			if (   ( this.locale.getLanguage()!=null && this.locale.getLanguage().equals("ja") )
				   || ( this.locale.getCountry()!=null && this.locale.getCountry().equals("JP") )   ) {

				message += " (ファイル: " + this.getFileName() + ", 行番号: " + this.getLineNumber() + ")";
			} else {
				message += " (file: " + this.getFileName() + ", line: " + this.getLineNumber() + ")";
			}
		}
		return message;
	}


	public String getMessageWithoutLocation() {
		String message = null;

		if (this.errorMessage != null) {
			message = this.errorMessage;
		}

		if (this.errorType != null) {
			message = ErrorMessage.generateErrorMessage(this.errorType, this.errorWords, this.locale);
		}

		return message;
	}
}
