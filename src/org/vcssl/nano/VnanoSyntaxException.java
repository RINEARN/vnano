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
public class VnanoSyntaxException extends Exception {

	private static final int LINE_NUMBER_DEFAULT_VALUE = -1;

	private ErrorType errorType = null;
	private String[] errorWords = null;
	private String fileName = null;
	private int lineNumber = LINE_NUMBER_DEFAULT_VALUE;

	public VnanoSyntaxException(ErrorType errorType) {
		this(errorType, (String)null, -1);
	}

	public VnanoSyntaxException(ErrorType errorType, String fileName, int lineNumber) {
		this(errorType, (String)null, fileName, lineNumber);
	}

	public VnanoSyntaxException(ErrorType errorType, String errorWord, String fileName, int lineNumber) {
		this(errorType, new String[] {errorWord}, fileName, lineNumber);
	}

	public VnanoSyntaxException(ErrorType errorType, String[] errorWords, String fileName, int lineNumber) {
		super(ErrorMessage.generateErrorMessage(errorType, errorWords));

		this.errorType = errorType;
		this.fileName = fileName;
		this.lineNumber = lineNumber;

		this.setErrorWords(errorWords);
	}


	public ErrorType getErrorType() {
		return this.errorType;
	}

	public String[] getErrorWords() {
		return this.errorWords;
	}

	public void setErrorWords(String[] errorWords) {
		this.errorWords = new String[ errorWords.length ];
		System.arraycopy(errorWords, 0, this.errorWords, 0, errorWords.length);
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
