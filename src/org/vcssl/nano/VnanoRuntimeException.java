/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano;

import javax.script.ScriptException;

/**
 * 開発途上で簡易的に用いる暫定的な実行時例外
 * 
 * @author RINEARN (Fumihiro Matsui)
 */
@SuppressWarnings("serial")
public class VnanoRuntimeException extends RuntimeException {

	public ScriptException toScriptException() {
		return new ScriptException(this);
	}
}
