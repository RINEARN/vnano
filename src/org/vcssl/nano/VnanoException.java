/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano;

import org.vcssl.nano.compiler.Compiler;
import org.vcssl.nano.spec.ErrorMessage;
import org.vcssl.nano.spec.ErrorType;

/**
 * <p>
 * コンパイラ内において、
 * スクリプトコードの内容に異常がある場合に、
 * {@link Compiler Compiler} やその構成クラスがスローする例外です。
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

	public VnanoException(ErrorType errorType) {
		this(errorType, (String)null, -1);
	}

	public VnanoException(ErrorType errorType, String fileName, int lineNumber) {
		this(errorType, (String)null, fileName, lineNumber);
	}

	public VnanoException(ErrorType errorType, String errorWord, String fileName, int lineNumber) {
		this(errorType, new String[] {errorWord}, fileName, lineNumber);
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
}
