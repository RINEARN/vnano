/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.memory;

import java.util.HashMap;

import org.vcssl.nano.lang.DataType;

@SuppressWarnings("serial")
public class DataException extends Exception {
	
	// 要リファクタリング
	
	public static final int REFERENCE_TO_DIFFERENT_DATA_TYPE = 1;
	public static final int UNCONVERTIBLE_DATA_TYPE = 2;
	public static final int UNCONVERTIBLE_DATA = 3;
	public static final int UNCONVERTIBLE_ARRAY = 4;
	public static final int UNKNOWN_DATA_TYPE = 5;
	public static final int UNOPERATABLE_DATA_TYPE = 6;
	public static final int UNEXPECTED_DATA_TYPE = 7;

	public static final HashMap<Integer, String> MESSAGE = new HashMap<Integer, String>();
	static {
		MESSAGE.put(new Integer(REFERENCE_TO_DIFFERENT_DATA_TYPE), "Reference to different data type");
		MESSAGE.put(new Integer(UNCONVERTIBLE_DATA_TYPE), "Unconvertible data type");
		MESSAGE.put(new Integer(UNCONVERTIBLE_DATA), "Unconvertible data");
		MESSAGE.put(new Integer(UNCONVERTIBLE_ARRAY), "Unconvertible array format");
		MESSAGE.put(new Integer(UNKNOWN_DATA_TYPE), "Unknown data type");
		MESSAGE.put(new Integer(UNOPERATABLE_DATA_TYPE), "Unoperatable data type");
		MESSAGE.put(new Integer(UNEXPECTED_DATA_TYPE), "Unexpected data type");
	}

	public DataException(int code) {
		super(MESSAGE.get(new Integer(code)));
	}
	public DataException(int code, String word) {
		super(MESSAGE.get(new Integer(code)) + " (" + word + ")");
	}
	public DataException(int code, DataType dataType) {
		super(MESSAGE.get(new Integer(code)) + " (" + dataType + ")");
	}
	public DataException(int code, DataType dataTypeA, DataType dataTypeB) {
		super(MESSAGE.get(new Integer(code)) + " (" + dataTypeA + ", " + dataTypeB + ")");
	}
}
