/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.assembler;

import java.util.HashMap;

/**
 * <p>
 * 中間アセンブリコードの内容に異常がある場合に、{@link Assembler Assembler} がスローする例外です。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
@SuppressWarnings("serial")
public class AssemblyCodeException extends Exception {

	// エラーメッセージは spec の設定に移すべきで、後で根本的に変更予定

	public static final int ILLEGAL_IMMEDIATE_VALUE_TYPE = 1;
	public static final int INVALID_IMMEDIATE_VALUE = 2;
	public static final int ILLEGAL_ADDRESS_TYPE = 3;
	public static final int ILLEGAL_ADDRESS = 4;
	public static final int ILLEGAL_DATA_TYPE = 5;
	public static final int IDENTIFIRE_DUPLICATION = 100;
	public static final int LABEL_DUPLICATION = 200;

	public static final HashMap<Integer, String> MESSAGE = new HashMap<Integer, String>();
	static {
		MESSAGE.put(new Integer(ILLEGAL_IMMEDIATE_VALUE_TYPE), "Illegal immidiae value type");
		MESSAGE.put(new Integer(INVALID_IMMEDIATE_VALUE), "Illegal immidiae value");
		MESSAGE.put(new Integer(ILLEGAL_ADDRESS_TYPE), "Illegal address type");
		MESSAGE.put(new Integer(ILLEGAL_ADDRESS), "Illegal address");
		MESSAGE.put(new Integer(ILLEGAL_DATA_TYPE), "Illegal data type");
		MESSAGE.put(new Integer(IDENTIFIRE_DUPLICATION), "Identifier Duplication");
		MESSAGE.put(new Integer(LABEL_DUPLICATION), "Label Duplication");
	}

	public AssemblyCodeException(int code) {
		super(MESSAGE.get(new Integer(code)));
	}
	public AssemblyCodeException(int code, String word) {
		super(MESSAGE.get(new Integer(code)) + " (" + word + ")");
	}

}
