/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.lang;

import java.util.HashMap;


/**
 * <p>
 * Vnano処理系内部において、データ型を表現・区別するための列挙子です。
 * </p>
 *
 * <p>
 * 各データ型の名称や、この列挙子要素とデータ型名との相互変換機能などは、
 * {@link org.vcssl.nano.spec.DataTypeName DataTypeName} クラスによって提供されます。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public enum DataType {

	/** 64ビット符号付き整数型です。 */
	INT64,

	/** 64ビット符号付き浮動小数点数型です。 */
	FLOAT64,

	/** 論理型です。 */
	BOOL,

	/** 文字列型です。*/
	STRING,

	/** データが存在しない場合や、型付け前の段階などで使用される、便宜的なデータ型です。*/
	VOID;

	/** データのクラスから、この列挙子要素へと変換するためのマップです。 */
	public static final HashMap<Class<?>, DataType> CLASS_DATA_TYPE_MAP = new HashMap<Class<?>, DataType>();
	static {
		CLASS_DATA_TYPE_MAP.put(long[].class, DataType.INT64);
		CLASS_DATA_TYPE_MAP.put(double[].class, DataType.FLOAT64);
		CLASS_DATA_TYPE_MAP.put(boolean[].class, DataType.BOOL);
		CLASS_DATA_TYPE_MAP.put(String[].class, DataType.STRING);
	}
}
