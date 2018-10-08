/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import java.util.HashMap;
import java.util.HashSet;

import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataException;

/**
 * <p>
 * データ型の名称が定義されたクラスであり、データ型名と
 * {@link org.vcssl.nano.lang.DataType DataType} 列挙子の要素とを相互変換するメソッドなども提供します。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class DataTypeName {

	/** 符号付き整数型（この処理系では64bit）のデータ型名です。 */
	public static final String INT = "int";

	/** 符号付き整数型（int型のエイリアス）のデータ型名です。 */
	public static final String LONG = "long";

	/** 符号付き浮動小数点型（この処理系では64bit）のデータ型名です。 */
	public static final String FLOAT = "float";

	/** 符号付き浮動小数点型（float型のエイリアス）のデータ型名です。 */
	public static final String DOUBLE = "double";

	/** 論理型のデータ型名です。 */
	public static final String BOOL = "bool";

	/** 文字列型のデータ型名です。 */
	public static final String STRING = "string";

	/** void型のデータ型名です。 */
	public static final String VOID = "void";


	/** データ型の名称を全て含むハッシュセットです。*/
	private static final HashSet<String> DATA_TYPE_NAME_SET;
	static {
    	DATA_TYPE_NAME_SET = new HashSet<String>();
    	DATA_TYPE_NAME_SET.add(DataTypeName.INT);
    	DATA_TYPE_NAME_SET.add(DataTypeName.LONG);
    	DATA_TYPE_NAME_SET.add(DataTypeName.FLOAT);
    	DATA_TYPE_NAME_SET.add(DataTypeName.DOUBLE);
    	DATA_TYPE_NAME_SET.add(DataTypeName.BOOL);
    	DATA_TYPE_NAME_SET.add(DataTypeName.STRING);
    	DATA_TYPE_NAME_SET.add(DataTypeName.VOID);
    }

	/** データ型の名称を、{org.vcssl.nano.lang.DataType DataType} 列挙子の要素に変換するマップです。 */
	private static final HashMap<String,DataType> DATA_TYPE_NAME_ENUM_MAP =  new HashMap<String,DataType>();
	static {
		DATA_TYPE_NAME_ENUM_MAP.put(INT, DataType.INT64);
		DATA_TYPE_NAME_ENUM_MAP.put(LONG, DataType.INT64);
		DATA_TYPE_NAME_ENUM_MAP.put(FLOAT, DataType.FLOAT64);
		DATA_TYPE_NAME_ENUM_MAP.put(DOUBLE, DataType.FLOAT64);
		DATA_TYPE_NAME_ENUM_MAP.put(BOOL, DataType.BOOL);
		DATA_TYPE_NAME_ENUM_MAP.put(STRING, DataType.STRING);
		DATA_TYPE_NAME_ENUM_MAP.put(VOID, DataType.VOID);
	}

	/** {org.vcssl.nano.lang.DataType DataType} 列挙子の要素を、データ型の名称に変換するマップです。 */
	private static final HashMap<DataType,String> DATA_TYPE_ENUM_NAME_MAP =  new HashMap<DataType,String>();
	static {
		DATA_TYPE_ENUM_NAME_MAP.put(DataType.INT64, INT);
		DATA_TYPE_ENUM_NAME_MAP.put(DataType.FLOAT64, FLOAT);
		DATA_TYPE_ENUM_NAME_MAP.put(DataType.BOOL, BOOL);
		DATA_TYPE_ENUM_NAME_MAP.put(DataType.STRING, STRING);
		DATA_TYPE_ENUM_NAME_MAP.put(DataType.VOID, VOID);
	}

	/**
	 * 指定された文字列が、データ型名であるかどうかを判定します。
	 *
	 * @param dataTypeName 判定対象の文字列
	 * @return 判定結果（データ型名であれば true ）
	 */
	public static boolean isDataTypeName(String dataTypeName) {
		return DATA_TYPE_NAME_SET.contains(dataTypeName);
	}


	/**
	 * {@link DataType DataType} 列挙子の要素を、データ型の名称に変換して返します。
	 *
	 * @param dataType データ型
	 * @return データ型の名称
	 */
	public static final String getDataTypeNameOf(DataType dataType) {
		return DATA_TYPE_ENUM_NAME_MAP.get(dataType);
	}


	/**
	 * 指定されたデータ型名が、指定されたデータ型の名称かどうか（エイリアスも含む）を判定します。
	 *
	 * @param dataType データ型
	 * @param dataTypeName データ型の名称（エイリアス）
	 * @return データ型の名称またはエイリアスに一致すればtrue
	 */
	public static final boolean isDataTypeNameOf(DataType dataType, String dataTypeName) {
		return DATA_TYPE_NAME_ENUM_MAP.get(dataTypeName) == dataType;
	}


	/**
	 * {@link DataType DataType} 列挙子の要素の配列を、
	 * データ型の名称の配列に一括変換して返します。
	 *
	 * @param dataType データ型の配列
	 * @return データ型の名称の配列
	 */
	public static final String[] getDataTypeNamesOf(DataType[] dataTypes) {
		int length = dataTypes.length;
		String[] dataTypeNames = new String[length];
		for (int index=0; index<length; index++) {
			dataTypeNames[index] = getDataTypeNameOf(dataTypes[index]);
		}
		return dataTypeNames;
	}


	/**
	 * データ型の名称を、{@link DataType DataType} 列挙子の要素に変換して返します。
	 *
	 * @param dataTypeName データ型の名称
	 * @return データ型
	 */
	public static final DataType getDataTypeOf(String dataTypeName)
			throws DataException {

		if (DATA_TYPE_NAME_ENUM_MAP.containsKey(dataTypeName)) {
			return DATA_TYPE_NAME_ENUM_MAP.get(dataTypeName);
		} else {
			throw new DataException(DataException.UNKNOWN_DATA_TYPE, dataTypeName);
		}
	}


	/**
	 * データ型の名称の配列を、{@link DataType DataType}
	 * 列挙子の要素の配列に一括変換して返します。
	 *
	 * @param dataTypeName データ型の名称の配列
	 * @return データ型の配列
	 * @throws DataException
	 */
	public static final DataType[] getDataTypesOf(String[] dataTypeNames)
			throws DataException {

		int length = dataTypeNames.length;
		DataType[] dataTypes = new DataType[length];
		for (int index=0; index<length; index++) {
			dataTypes[index] = getDataTypeOf(dataTypeNames[index]);
		}
		return dataTypes;
	}
}
