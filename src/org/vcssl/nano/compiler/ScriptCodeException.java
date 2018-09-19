/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import java.util.HashMap;

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

	// エラーメッセージは spec の設定に移すべきで、後で根本的に変更予定

	// 行番号や式番号などの情報が必要

	public static final int INVALID_SYMBOL = 1;

	public static final HashMap<Integer, String> MESSAGE = new HashMap<Integer, String>();
	static {
		MESSAGE.put(new Integer(INVALID_SYMBOL), "Invalid symbol");
	}

	public ScriptCodeException(int code) {
		super(MESSAGE.get(new Integer(code)));
	}
	public ScriptCodeException(int code, String word) {
		super(MESSAGE.get(new Integer(code)) + " (" + word + ")");
	}

}
