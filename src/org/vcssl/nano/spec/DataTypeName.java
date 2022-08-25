/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.vcssl.nano.VnanoException;

/**
 * The enum which defines the names of the data types available in scripts.
 * 
 * The class to define name of data types of the Vnano, and to provides converter methods
 * between them and elements of {@link DataType DataType} enum.
 */
public class DataTypeName {

	/** The name of the standard 64-bit signed integer type (64-bit precision for this script engin): "int". */
	public static final String DEFAULT_INT = "int";

	/** The alias of the name of the 64-bit signed integer type: "long". */
	public static final String LONG_INT = "long";

	/** The name of the standard 64-bit floating-point number type (64-bit precision for this script engin): "float". */
	public static final String DEFAULT_FLOAT = "float";

 	/** The alias of the 64-bit floating-point number type: "double". */
	public static final String DOUBLE_FLOAT = "double";

	/** The name of the boolean type: "bool". */
	public static final String BOOL = "bool";

	/** The name of the character string type: "string". */
	public static final String STRING = "string";

	/** The name of the special type to represent that any type is available for the argument, the return value, and so on: "any". */
	public static final String ANY = "any";

	/** The name of the void (placeholder) type: "void". */
	public static final String VOID = "void";

	/** The Set which contains all names of the data types defined in this class. */
	@SuppressWarnings("serial")
	private static final Set<String> DATA_TYPE_NAME_SET = new HashSet<String>() {{
    	add(DEFAULT_INT);
    	add(LONG_INT);
    	add(DEFAULT_FLOAT);
    	add(DOUBLE_FLOAT);
    	add(BOOL);
    	add(STRING);
    	add(ANY);
    	add(VOID);
    }};

	/** The Map for converting each datat type name to the corresponding element of {org.vcssl.nano.lang.DataType DataType} enum. */
	@SuppressWarnings("serial")
	private static final Map<String,DataType> DATA_TYPE_NAME_ENUM_MAP =  new HashMap<String,DataType>() {{
		put(DEFAULT_INT, DataType.INT64);
		put(LONG_INT, DataType.INT64);
		put(DEFAULT_FLOAT, DataType.FLOAT64);
		put(DOUBLE_FLOAT, DataType.FLOAT64);
		put(BOOL, DataType.BOOL);
		put(STRING, DataType.STRING);
		put(ANY, DataType.ANY);
		put(VOID, DataType.VOID);
	}};

	/** The Map for converting each element of {org.vcssl.nano.lang.DataType DataType} enum to the corresponding datat type name. */
	@SuppressWarnings("serial")
	private static final Map<DataType,String> DATA_TYPE_ENUM_NAME_MAP =  new HashMap<DataType,String>() {{
		put(DataType.INT64, DEFAULT_INT);
		put(DataType.FLOAT64, DEFAULT_FLOAT);
		put(DataType.BOOL, BOOL);
		put(DataType.STRING, STRING);
		put(DataType.ANY, ANY);
		put(DataType.VOID, VOID);
	}};


	/**
	 * Determines whether the specified string is the name of a data type.
	 * 
	 * @param dataTypeName The string to be checked whether it is the name of a data type.
	 * @return Returns "true" if it is the name of the data type.
	 */
	public static final boolean isDataTypeName(String dataTypeName) {
		return DATA_TYPE_NAME_SET.contains(dataTypeName);
	}


	/**
	 * Converts each element of {@link DataType DataType} enum to the name of the corresponding data type.
	 * 
	 * @param dataType The data type to be converted to the name.
	 * @return The name of the data type.
	 */
	public static final String getDataTypeNameOf(DataType dataType) {
		return DATA_TYPE_ENUM_NAME_MAP.get(dataType);
	}


	/**
	 * Determines whether the specified string equals to the name of the specified data type.
	 * 
	 * @param dataType The expected data type.
	 * @param dataTypeName The string to be checked whether it is the name of the specified data type.
	 * @return Returns "true" if the passed string equals to the name of the passed data type.
	 */
	public static final boolean isDataTypeNameOf(DataType dataType, String dataTypeName) {
		return DATA_TYPE_NAME_ENUM_MAP.get(dataTypeName) == dataType;
	}


	/**
	 * Converts the specified elements of {@link DataType DataType} enum to the names of the corresponding data types.
	 * 
	 * @param dataTypes Data types to be converted to the name.
	 * @return Names of the data type.
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
	 * Converts the specified data type name to the corresponding element of {@link DataType DataType} enum.
	 * 
	 * @param dataTypeName The name of the data type to be converted to the element of {@link DataType DataType} enum.
	 * @return The converted element of {@link DataType DataType} enum.
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
	 * Converts the specified data type names to the corresponding elements of {@link DataType DataType} enum.
	 * 
	 * @param dataTypeNames The data type names to be converted to the elements of {@link DataType DataType} enum.
	 * @return The converted elements of {@link DataType DataType} enum.
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

