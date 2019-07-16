/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import java.util.HashMap;
import java.util.HashSet;

import org.vcssl.nano.VnanoException;

/**
 * <p>
 * <span>
 * <span class="lang-en">
 * The class to define name of data types of the Vnano, and to provides converter methods
 * between them and elements of {@link DataType DataType} enum
 * </span>
 * <span class="lang-ja">
 * Vnano のデータ型の名称が定義されたクラスであり、データ型名と
 * {@link DataType DataType} 列挙子の要素とを相互変換するメソッドなども提供します
 * </span>
 * .
 * </p>
 *
 * <p>
 * &raquo <a href="../../../../../src/org/vcssl/nano/spec/DataTypeName.java">Source code</a>
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class DataTypeName {


	/**
	 * <span class="lang-en">
	 * The name of the standard 64-bit signed integer type
	 * (64-bit precision for this script engin): "int"
	 * </span>
	 * <span class="lang-ja">
	 * 標準の符号付き整数型（このスクリプトエンジンでは 64bit 精度）の名称 "int" です
	 * </span>
	 * .
	 */
	public static final String INT = "int";


	/**
	 * <span class="lang-en">The alias of the name of the 64-bit signed integer type: "long"</span>
	 * <span class="lang-ja">64bit 符号付き整数型の別名 "long" です</span>
	 * .
	 */
	public static final String LONG = "long";


	/**
	 * <span class="lang-en">
	 * The name of the standard 64-bit floating-point number type
	 *  (64-bit precision for this script engin): "float"
	 * </span>
	 * <span class="lang-ja">
	 * 標準の浮動小数点数型（このスクリプトエンジンでは 64bit 精度）の名称 "float" です
	 * </span>
	 * .
	 */
	public static final String FLOAT = "float";


	/**
	 * <span class="lang-en">The alias of the 64-bit floating-point number type: "double"</span>
	 * <span class="lang-ja">64-bit 浮動小数点数型の別名 "double" です</span>
	 * .
	 */
	public static final String DOUBLE = "double";


	/**
	 * <span class="lang-en">The name of the boolean type: "bool"</span>
	 * <span class="lang-ja">論理型（真偽型）の名称 "bool" です</span>
	 * .
	 */
	public static final String BOOL = "bool";

	/**
	 * <span class="lang-en">The name of the character string type: "string"</span>
	 * <span class="lang-ja">文字列型の名称 "string" です</span>
	 * .
	 */
	public static final String STRING = "string";

	/**
	 * <span class="lang-en">The name of the void type: "void"</span>
	 * <span class="lang-ja">void 型の名称 "void" です</span>
	 * .
	 */
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


	private DataTypeName() {
	}


	/**
	 * <span class="lang-en">Checks whether the specified string is the name of the data type or not</span>
	 * <span class="lang-ja">指定された文字列が, データ型名であるかどうかを判定します</span>
	 * .
	 * @param dataTypeName
	 *   <span class="lang-en">Strings to be checked</span>
	 *   <span class="lang-ja">判定対象の文字列</span>
	 *
	 * @return
	 *   <span class="lang-en">The check result ("true" if it is the name of the data type)</span>
	 *   <span class="lang-ja">判定結果（データ型名であれば true ）</span>
	 */
	public static boolean isDataTypeName(String dataTypeName) {
		return DATA_TYPE_NAME_SET.contains(dataTypeName);
	}


	/**
	 * <span class="lang-en">
	 * Converts a element of {@link DataType DataType} enum to the name of the data type
	 * </span>
	 * <span class="lang-ja">
	 * {@link DataType DataType} 列挙子の要素を, データ型の名称に変換して返します
	 * </span>
	 * .
	 * @param dataType
	 *   <span class="lang-en">The data type to be converted to the name.</span>
	 *   <span class="lang-ja">データ型名に変換したいデータ型.</span>
	 *
	 * @return
	 *   <span class="lang-en">The name of the data type.</span>
	 *   <span class="lang-ja">データ型の名称.</span>
	 */
	public static final String getDataTypeNameOf(DataType dataType) {
		return DATA_TYPE_ENUM_NAME_MAP.get(dataType);
	}


	/**
	 * <span class="lang-en">
	 * Checks whether the specified string equals to the name of the specified data type or not
	 * </span>
	 * <span class="lang-ja">指定された文字列が, 指定されたデータ型の名称と一致するかどうかを判定します</span>
	 * .
	 * @param dataType
	 *   <span class="lang-en">The data type to be checked</span>
	 *   <span class="lang-ja">判定対象のデータ型</span>
	 *
	 * @param dataTypeName
	 *   <span class="lang-en">Strings to be checked</span>
	 *   <span class="lang-ja">判定対象の文字列</span>
	 *
	 * @return
	 *   <span class="lang-en">
	 *   The check result ("true" if the passed string equals to the name of the passed data type)
	 *    </span>
	 *   <span class="lang-ja">判定結果（一致すれば true ）</span>
	 */
	public static final boolean isDataTypeNameOf(DataType dataType, String dataTypeName) {
		return DATA_TYPE_NAME_ENUM_MAP.get(dataTypeName) == dataType;
	}


	/**
	 * <span class="lang-en">
	 * Converts elements of {@link DataType DataType} enum to names of the data types
	 * </span>
	 * <span class="lang-ja">
	 * 複数の {@link DataType DataType} 列挙子の要素を, 一括でデータ型の名称に変換して返します
	 * </span>
	 * .
	 * @param dataTypes
	 *   <span class="lang-en">Data types to be converted to the name.</span>
	 *   <span class="lang-ja">データ型名に変換したいデータ型（複数）.</span>
	 *
	 * @return
	 *   <span class="lang-en">Names of the data type.</span>
	 *   <span class="lang-ja">データ型の名称（複数）.</span>
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
	 * <span class="lang-en">
	 * Converts the name of a data type to the element of  {@link DataType DataType} enum
	 * </span>
	 * <span class="lang-ja">
	 * データ型の名称を, {@link DataType DataType} 列挙子の要素に変換して返します
	 * </span>
	 * .
	 * @param dataType
	 *   <span class="lang-en">The name of the data type to be converted to the element of the name.</span>
	 *   <span class="lang-ja">列挙子の要素に変換したいデータ型名.</span>
	 *
	 * @return
	 *   <span class="lang-en">The converted enum element.</span>
	 *   <span class="lang-ja">変換された列挙子の要素.</span>
	 */
	public static final DataType getDataTypeOf(String dataTypeName)
			throws VnanoException {

		if (DATA_TYPE_NAME_ENUM_MAP.containsKey(dataTypeName)) {
			return DATA_TYPE_NAME_ENUM_MAP.get(dataTypeName);
		} else {
			VnanoException e = new VnanoException(ErrorType.UNKNOWN_DATA_TYPE, new String[] { dataTypeName });
			throw e;
		}
	}


	/**
	 * <span class="lang-en">
	 * Converts names of a data types to the elements of  {@link DataType DataType} enum
	 * </span>
	 * <span class="lang-ja">
	 * 複数のデータ型の名称を, 一括で {@link DataType DataType} 列挙子の要素に変換して返します
	 * </span>
	 * .
	 * @param dataType
	 *   <span class="lang-en">Names of the data types to be converted to the elements of the name.</span>
	 *   <span class="lang-ja">列挙子の要素に変換したいデータ型名（複数）.</span>
	 *
	 * @return
	 *   <span class="lang-en">The converted enum elements.</span>
	 *   <span class="lang-ja">変換された列挙子の要素（複数）.</span>
	 */
	public static final DataType[] getDataTypesOf(String[] dataTypeNames)
			throws VnanoException {

		int length = dataTypeNames.length;
		DataType[] dataTypes = new DataType[length];
		for (int index=0; index<length; index++) {
			dataTypes[index] = getDataTypeOf(dataTypeNames[index]);
		}
		return dataTypes;
	}
}
