/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano;

import java.util.Locale;

import org.vcssl.nano.spec.ErrorMessage;
import org.vcssl.nano.spec.ErrorType;

/**
 * <span class="lang-ja">
 * スクリプトコードの内容や実行過程に, 通常想定される範囲内のエラーが検出された際にスローされる例外です
 * </span>
 * <span class="lang-en">
 * The exception class thrown when the (usual type of) error detected for contents or processing of scripts
 * </span>
 * .
 *
 * <p>
 * &raquo <a href="../../../../src/org/vcssl/nano/VnanoException.java">Source code</a>
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
@SuppressWarnings("serial")
public class VnanoException extends Exception {

	private static final int LINE_NUMBER_DEFAULT_VALUE = -1;

	private ErrorType errorType = null;
	private String fileName = null;
	private int lineNumber = LINE_NUMBER_DEFAULT_VALUE;
	private String[] errorWords = null;
	private String errorMessage = null;

	private Locale locale = Locale.getDefault();

	public VnanoException(Throwable errorCauseThrowable) {
		super(errorCauseThrowable);
	}

	public VnanoException(ErrorType errorType) {
		this(errorType, (String)null, -1);
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
		super(ErrorMessage.generateErrorMessage(errorType, errorWords));
		this.errorType = errorType;
		this.errorWords = errorWords;
		this.fileName = fileName;
		this.lineNumber = lineNumber;
	}

	public VnanoException(ErrorType errorType, String[] errorWords, Throwable cause) {
		super(cause);
		this.errorType = errorType;
		this.errorWords = errorWords;
	}

	public VnanoException(ErrorType errorType, String errorWord, Throwable cause) {
		this(errorType, new String[] {errorWord}, cause);
	}

	public VnanoException(ErrorType errorType, Throwable cause) {
		this(errorType, (String)null, cause);
	}

	public ErrorType getErrorType() {
		return this.errorType;
	}

	public String[] getErrorWords() {
		return this.errorWords;
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

	public int getLineNumber() {
		return this.lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public boolean hasLineNumber() {
		return this.lineNumber != LINE_NUMBER_DEFAULT_VALUE;
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
			message += " (script:" + this.getFileName() + ", line:" + this.getLineNumber() + ")";
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
