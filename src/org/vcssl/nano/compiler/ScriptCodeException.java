/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

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
public class ScriptCodeException extends Exception {

	private ErrorType errorType = null;
	private String[] errorWords = null;
	private String fileName = null;
	private int lineNumber = -1;

	public ScriptCodeException(ErrorType errorType, String fileName, int lineNumber) {
		this(errorType, (String)null, fileName, lineNumber);
	}

	public ScriptCodeException(ErrorType errorType, String errorWord, String fileName, int lineNumber) {
		this(errorType, new String[] {errorWord}, fileName, lineNumber);
	}

	public ScriptCodeException(ErrorType errorType, String[] errorWords, String fileName, int lineNumber) {
		super(ErrorMessage.generateErrorMessage(errorType, errorWords));

		this.errorType = errorType;
		this.errorWords = new String[ errorWords.length ];
		System.arraycopy(errorWords, 0, this.errorWords, 0, errorWords.length);

		this.fileName = fileName;
		this.lineNumber = lineNumber;
	}

	public ErrorType getErrorType() {
		return this.errorType;
	}

	public String[] getErrorWords() {
		return this.errorWords;
	}

	public String getFileName() {
		return this.fileName;
	}

	public int getLineNumber() {
		return this.lineNumber;
	}
}
