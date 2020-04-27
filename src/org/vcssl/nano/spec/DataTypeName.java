/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.vcssl.nano.VnanoException;

// Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/spec/DataTypeName.html
// ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/spec/DataTypeName.html

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
 * &raquo; <a href="../../../../../src/org/vcssl/nano/spec/DataTypeName.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/spec/DataTypeName.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/spec/DataTypeName.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class DataTypeName {


	// 各フィールドは元々は static final でしたが、カスタマイズの事を考慮して、動的なフィールドに変更されました。
	// これにより、このクラスのインスタンスを生成して値を変更（または継承してメソッド実装なども変更）し、
	// それを LanguageSpecContainer に持たせて VnanoEngle クラスのコンストラクタに渡す事で、
	// 処理系内のソースコードを保ったまま（再ビルド不要で）定義類を差し替える事ができます。


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
	public String INT = "int";


	/**
	 * <span class="lang-en">The alias of the name of the 64-bit signed integer type: "long"</span>
	 * <span class="lang-ja">64bit 符号付き整数型の別名 "long" です</span>
	 * .
	 */
	public String LONG = "long";


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
	public String FLOAT = "float";


	/**
	 * <span class="lang-en">The alias of the 64-bit floating-point number type: "double"</span>
	 * <span class="lang-ja">64-bit 浮動小数点数型の別名 "double" です</span>
	 * .
	 */
	public String DOUBLE = "double";


	/**
	 * <span class="lang-en">The name of the boolean type: "bool"</span>
	 * <span class="lang-ja">論理型（真偽型）の名称 "bool" です</span>
	 * .
	 */
	public String BOOL = "bool";

	/**
	 * <span class="lang-en">The name of the character string type: "string"</span>
	 * <span class="lang-ja">文字列型の名称 "string" です</span>
	 * .
	 */
	public String STRING = "string";


	/**
	 * <span class="lang-en">The name of the special type to represent that any type is available for the argument, the return value, and so on: "any"</span>
	 * <span class="lang-ja">関数の引数や戻り値などが、任意の型であり得る事を示す、特別な型の名称 "any" です</span>
	 * .
	 */
	public String ANY = "any";


	/**
	 * <span class="lang-en">The name of the void type: "void"</span>
	 * <span class="lang-ja">void 型の名称 "void" です</span>
	 * .
	 */
	public String VOID = "void";


	/** データ型の名称を全て含むハッシュセットです。*/
	@SuppressWarnings("serial")
	private Set<String> DATA_TYPE_NAME_SET = new HashSet<String>() {{
    	add(INT);
    	add(LONG);
    	add(FLOAT);
    	add(DOUBLE);
    	add(BOOL);
    	add(STRING);
    	add(ANY);
    	add(VOID);
    }};

	/** データ型の名称を、{org.vcssl.nano.lang.DataType DataType} 列挙子の要素に変換するマップです。 */
	@SuppressWarnings("serial")
	private Map<String,DataType> DATA_TYPE_NAME_ENUM_MAP =  new HashMap<String,DataType>() {{
		put(INT, DataType.INT64);
		put(LONG, DataType.INT64);
		put(FLOAT, DataType.FLOAT64);
		put(DOUBLE, DataType.FLOAT64);
		put(BOOL, DataType.BOOL);
		put(STRING, DataType.STRING);
		put(ANY, DataType.ANY);
		put(VOID, DataType.VOID);
	}};

	/** {org.vcssl.nano.lang.DataType DataType} 列挙子の要素を、データ型の名称に変換するマップです。 */
	@SuppressWarnings("serial")
	private Map<DataType,String> DATA_TYPE_ENUM_NAME_MAP =  new HashMap<DataType,String>() {{
		put(DataType.INT64, INT);
		put(DataType.FLOAT64, FLOAT);
		put(DataType.BOOL, BOOL);
		put(DataType.STRING, STRING);
		put(DataType.ANY, ANY);
		put(DataType.VOID, VOID);
	}};


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
	public boolean isDataTypeName(String dataTypeName) {
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
	public String getDataTypeNameOf(DataType dataType) {
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
	public boolean isDataTypeNameOf(DataType dataType, String dataTypeName) {
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
	public String[] getDataTypeNamesOf(DataType[] dataTypes) {
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
	public DataType getDataTypeOf(String dataTypeName)
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
	public DataType[] getDataTypesOf(String[] dataTypeNames)
			throws VnanoException {

		int length = dataTypeNames.length;
		DataType[] dataTypes = new DataType[length];
		for (int index=0; index<length; index++) {
			dataTypes[index] = getDataTypeOf(dataTypeNames[index]);
		}
		return dataTypes;
	}
}
