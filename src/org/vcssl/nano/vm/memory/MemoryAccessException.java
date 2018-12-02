/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.memory;

import java.util.HashMap;

/**
 * <p>
 * {@link Memory Memory} へのアクセスにおいて、
 * 確保領域外のアドレス指定などの異常があった場合にスローされます。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
@SuppressWarnings("serial")
public class MemoryAccessException extends Exception {

	// 要リファクタリング
	
	public static final int INVALID_PARTITION = 1;
	public static final int ADDRESS_OUT_OF_BOUNDS = 1;
	public static final int NO_DATA = 2;

	public static final HashMap<Integer, String> MESSAGE = new HashMap<Integer, String>();
	static {
		MESSAGE.put(new Integer(INVALID_PARTITION), "Illegal address type");
		MESSAGE.put(new Integer(ADDRESS_OUT_OF_BOUNDS), "Address out of bounds");
		MESSAGE.put(new Integer(NO_DATA), "No data");
	}

	public MemoryAccessException(int code) {
		super(MESSAGE.get(new Integer(code)));
	}
	public MemoryAccessException(int code, String word) {
		super(MESSAGE.get(new Integer(code)) + " (" + word + ")");
	}
	public MemoryAccessException(int code, Memory.Partition addressType) {
		super(MESSAGE.get(new Integer(code)) + " (" + addressType.toString() + ")");
	}
	public MemoryAccessException(int code, int addressType, int address) {
		super(MESSAGE.get(new Integer(code)) + " (addressType:" + addressType + ", address:" + address + ")");
	}
	public MemoryAccessException(int code, Memory.Partition addressType, int address) {
		super(MESSAGE.get(new Integer(code)) + " (addressType:" + addressType.toString() + ", address:" + address + ")");
	}
}
