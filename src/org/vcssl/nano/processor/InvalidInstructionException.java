/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.processor;

import java.util.HashMap;

/**
 * <p>
 * 仮想プロセッサ内において、命令の内容に異常があった場合にスローされる例外です。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 *
 */
@SuppressWarnings("serial")
public class InvalidInstructionException extends Exception {

	// 要リファクタリング

	public static final int ILLEGAL_NUMBER_OF_OPERANDS = 1;
	public static final int ILLEGAL_OPERAND_TYPE = 2;
	public static final int ILLEGAL_OPERATION_CODE = 3;

	public static final HashMap<Integer, String> MESSAGE = new HashMap<Integer, String>();
	static {
		MESSAGE.put(new Integer(ILLEGAL_NUMBER_OF_OPERANDS), "Illegal number of operands");
		MESSAGE.put(new Integer(ILLEGAL_OPERAND_TYPE), "Illegal operand type");
		MESSAGE.put(new Integer(ILLEGAL_OPERATION_CODE), "Illegal operation code");
	}

	public InvalidInstructionException() {
	}
	public InvalidInstructionException(int code) {
		super(MESSAGE.get(new Integer(code)));
	}
	public InvalidInstructionException(int code, String word) {
		super(MESSAGE.get(new Integer(code)) + " (" + word + ")");
	}
	public InvalidInstructionException(int code, String wordA, String wordB) {
		super(MESSAGE.get(new Integer(code)) + " (" + wordA + ", " + wordB + ")");
	}
	public InvalidInstructionException(int code, OperationCode opcode) {
		super(MESSAGE.get(new Integer(code)) + " (" + opcode.toString() + ")");
	}
}
