/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import java.util.HashMap;

import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.ScriptWord;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.VnanoException;

/**
 * The class for performing conversions of data-types between inside/outside of Vnano Engine.
 * 
 * In comments/documents of this class,
 * "external" data-type means a data-type on the outside of the Vnano Engine, 
 * and "internal" data-type means a data-type in the inside of the Vnano Engine.
 */
public class DataConverter {

	/** The array-rank of a scalar. */
	private static final int ARRAY_RANK_OF_SCALAR = 0;


	/**
	 * The enum representing external data-types.
	 */
	private enum ExternalType {

		/** Represents the 32-bit signed integer type (int). */
		INT32,

		/** Represents the 64-bit signed integer type (long). */
		INT64,

		/** Represents the 32-bit floating point number type (float). */
		FLOAT32,

		/** Represents the 64-bit floating point number type (float). */
		FLOAT64,

		/** Represents the character-string type (String). */
		STRING,

		/** Represents the boolean type (boolean). */
		BOOL,

		/** Represents the special data-type which can wrap data of any data-types (Object). */
		ANY,

		/** Represents the special data-type meaning that no data exists (void). */
		VOID;
	}


	/** The name of the external data-type of the 32-bit signed integer type (int). */
	private static final String EXTERNAL_TYPE_NAME_INT32 = "int";

	/** The name of the external data-type of the 64-bit signed integer type (long). */
	private static final String EXTERNAL_TYPE_NAME_INT64 = "long";

	/** The name of the external data-type of the 32-bit floating point number type (float). */
	private static final String EXTERNAL_TYPE_NAME_FLOAT32 = "float";

	/** The name of the external data-type of the 64-bit floating point number type (double). */
	private static final String EXTERNAL_TYPE_NAME_FLOAT64 = "double";

	/** The name of the external data-type of the boolean type (boolean). */
	private static final String EXTERNAL_TYPE_NAME_BOOL = "boolean";

	/** The name of the external data-type of the character-string type (String). */
	private static final String EXTERNAL_TYPE_NAME_STRING = "java.lang.String";

	/** The name of the external data-type of the special data-type which can wrap data of any data-types (Object). */
	private static final String EXTERNAL_TYPE_NAME_ANY = "java.lang.Object";

	/** The name of the external data-type of the special data-type meaning that no data exists (void). */
	private static final String EXTERNAL_TYPE_NAME_VOID = "void";

	/** The name of the external data-type of the wrapper class of the 32-bit signed integer type (Integer). */
	private static final String EXTERNAL_TYPE_NAME_INT32_WRAPPER = "java.lang.Integer";

	/** The name of the wrapper class of the external 64-bit signed integer type (Long). */
	private static final String EXTERNAL_TYPE_NAME_INT64_WRAPPER = "java.lang.Long";

	/** The name of the wrapper class of the external 32-bit floating point number type (Float). */
	private static final String EXTERNAL_TYPE_NAME_FLOAT32_WRAPPER = "java.lang.Float";

	/** The name of the wrapper class of the external 64-bit floating point number type (Float). */
	private static final String EXTERNAL_TYPE_NAME_FLOAT64_WRAPPER = "java.lang.Double";

	/** The name of the wrapper class of the external boolean type (Boolean). */
	private static final String EXTERNAL_TYPE_NAME_BOOL_WRAPPER = "java.lang.Boolean";

	/** The name of the wrapper class of the external special data-type meaning that no data exists (Void). */
	private static final String EXTERNAL_TYPE_NAME_VOID_WRAPPER = "java.lang.Void";

	/** The notation of the array part of external data-types ([]). */
	private static final String EXTERNAL_ARRAY_BRACKET = "[]";

	/** The regular experssion of the array part of external data-types. */
	private static final String EXTERNAL_ARRAY_BRACKET_REGEX = "\\[\\]";


	/**
	 * The Map for converting the name of a external data-type, 
	 * to an element of {@link org.vcssl.nano.spec.DataType DataType} enum, 
	 * which represents an internal data-type.
	 */
	private static final HashMap<String,DataType> EXTERNAL_NAME_DATA_TYPE_MAP = new HashMap<String,DataType>();
	static {
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_INT32, DataType.INT64);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_INT64, DataType.INT64);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_FLOAT32, DataType.FLOAT64);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_FLOAT64, DataType.FLOAT64);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_STRING, DataType.STRING);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_BOOL, DataType.BOOL);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_ANY, DataType.ANY);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_VOID, DataType.VOID);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_INT32_WRAPPER, DataType.INT64);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_INT64_WRAPPER, DataType.INT64);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_FLOAT32_WRAPPER, DataType.FLOAT64);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_FLOAT64_WRAPPER, DataType.FLOAT64);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_BOOL_WRAPPER, DataType.BOOL);
		EXTERNAL_NAME_DATA_TYPE_MAP.put(EXTERNAL_TYPE_NAME_VOID_WRAPPER, DataType.VOID);
	}


	/**
	 * The Map for converting the name of a data-types, 
	 * to an element of {@link org.vcssl.nano.spec.DataConverter.ExternalType ExternalType} enum, 
	 * which represents an external data-type.
	 */
	private static final HashMap<String,ExternalType> EXTERNAL_NAME_EXTERNAL_TYPE_MAP = new HashMap<String,ExternalType>();
	static {
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_INT32, ExternalType.INT32);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_INT64, ExternalType.INT64);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_FLOAT32, ExternalType.FLOAT32);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_FLOAT64, ExternalType.FLOAT64);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_STRING, ExternalType.STRING);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_BOOL, ExternalType.BOOL);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_ANY, ExternalType.ANY);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_VOID, ExternalType.VOID);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_INT32_WRAPPER, ExternalType.INT32);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_INT64_WRAPPER, ExternalType.INT64);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_FLOAT32_WRAPPER, ExternalType.FLOAT32);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_FLOAT64_WRAPPER, ExternalType.FLOAT64);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_BOOL_WRAPPER, ExternalType.BOOL);
		EXTERNAL_NAME_EXTERNAL_TYPE_MAP.put(EXTERNAL_TYPE_NAME_VOID_WRAPPER, ExternalType.VOID);
	}


	/**
	 * The Map for converting an element of 
	 * {@link org.vcssl.nano.spec.DataConverter.ExternalType ExternalType} enum
	 * to the corresponding element of {@link org.vcssl.nano.spec.DataType DataType} enum.
	 */
	private static final HashMap<ExternalType, DataType> EXTERNAL_TYPE_DATA_TYPE_MAP = new HashMap<ExternalType, DataType>();
	static {
		EXTERNAL_TYPE_DATA_TYPE_MAP.put(ExternalType.INT64, DataType.INT64);
		EXTERNAL_TYPE_DATA_TYPE_MAP.put(ExternalType.FLOAT64, DataType.FLOAT64);
		EXTERNAL_TYPE_DATA_TYPE_MAP.put(ExternalType.BOOL, DataType.BOOL);
		EXTERNAL_TYPE_DATA_TYPE_MAP.put(ExternalType.STRING, DataType.STRING);
		EXTERNAL_TYPE_DATA_TYPE_MAP.put(ExternalType.ANY, DataType.ANY);
		EXTERNAL_TYPE_DATA_TYPE_MAP.put(ExternalType.VOID, DataType.VOID);
	}


	/**
	 * The Map for converting an element of {@link org.vcssl.nano.spec.DataType DataType} enum
	 * to the corresponding element of {@link org.vcssl.nano.spec.DataConverter.ExternalType ExternalType} enum.
	 */
	private static final HashMap<DataType, ExternalType> DATA_TYPE_EXTERNAL_TYPE_MAP = new HashMap<DataType, ExternalType>();
	static {
		DATA_TYPE_EXTERNAL_TYPE_MAP.put(DataType.INT64, ExternalType.INT64);
		DATA_TYPE_EXTERNAL_TYPE_MAP.put(DataType.FLOAT64, ExternalType.FLOAT64);
		DATA_TYPE_EXTERNAL_TYPE_MAP.put(DataType.BOOL, ExternalType.BOOL);
		DATA_TYPE_EXTERNAL_TYPE_MAP.put(DataType.STRING, ExternalType.STRING);
		DATA_TYPE_EXTERNAL_TYPE_MAP.put(DataType.ANY, ExternalType.ANY);
		DATA_TYPE_EXTERNAL_TYPE_MAP.put(DataType.VOID, ExternalType.VOID);
	}


	/** Stores the name of the external data-type to be converted by this instance. */
	private ExternalType externalType = null;

	/** Stores the name of the internal data-type to be converted by this instance. */
	private DataType dataType = null;

	/** The array-rank of data to be converted by this instance. */
	private int arrayRank = -1;


	/**
	 * Creates a new instance for converting data of the specified data-type.
	 * 
	 * Fot the argument "objectClass", specify the class of the external data-type.
	 * The corresponding internal data-type will be set automatically from the external data-type. 
	 * 
	 * If the external data-type to be converted is a primitive type, 
	 * specify the wrapper class of it
	 * (e.g.: Integer for int).
	 * 
	 * Also, if data to be converted is an array, specify the array type class
	 * (e.g.: int[], double[][], etc.).
	 *
	 * @param objectClass The class of the external data-type to be converted.
	 * @throws VnanoException Thrown if an unsupported data-type is specified.
	 */
	public DataConverter(Class<?> objectClass) throws VnanoException {

		this.arrayRank = getArrayRankOf(objectClass);
		String externalDataTypeName = getExternalTypeNameOf(objectClass);
		this.externalType = EXTERNAL_NAME_EXTERNAL_TYPE_MAP.get(externalDataTypeName);
		this.dataType = EXTERNAL_NAME_DATA_TYPE_MAP.get(externalDataTypeName);

		if (this.dataType == null) {
			throw new VnanoException(
				ErrorType.UNCONVERTIBLE_DATA_TYPE,
				new String[] {objectClass.getCanonicalName()}
			);
		}
	}


	/**
	 * Creates a new instance for converting data of the specified data-type.
	 * 
	 * Fot the argument "dataType", specify the internal data-type.
	 * The corresponding external data-type will be set automatically from the internal data-type. 
	 * 
	 * @param objectClass The class of the internal data-type to be converted.
	 * @param rank The array-rank of data to be converted.
	 * @throws VnanoException Thrown if an unsupported data-type is specified.
	 */
	public DataConverter(DataType dataType, int rank) throws VnanoException {
		this.arrayRank = rank;
		this.dataType = dataType;
		this.externalType = DATA_TYPE_EXTERNAL_TYPE_MAP.get(dataType);
	}


	/**
	 * Returns whether data of an external data-type specified as "objectClass"
	 * is convertible to data of an internal data-type.
	 * 
	 * This method is used for generating information in error messages, from the outside of this class.
	 *
	 * @param objectClass The class representing an external data-type.
	 * @return Returns true if the specified external data-type is convertible to an internal data-type.
	 */
	public static boolean isConvertible(Class<?> objectClass) {
		String externalDataTypeName = getExternalTypeNameOf(objectClass);
		DataType dataType = EXTERNAL_NAME_DATA_TYPE_MAP.get(externalDataTypeName);
		return dataType != null;
	}


	/**
	 * Returns the name of the external data-type corresponding with the specified class.
	 * 
	 * For example, this method returns "int" if Integer.class or int.class is specified.
	 * 
	 * Note that, the type name returned by this method does not contain the array part "[][]...".
	 * You can get the array-rank by {@link DataConverter#}
	 * 
	 * @param objectClass The class representing the external data-type.
	 * @return The name of the specified external data-type.
	 */
	private static final String getExternalTypeNameOf(Class<?> objectClass) {
		String className = objectClass.getCanonicalName();
		String typeName = null;
		boolean isArray = 0 <= className.indexOf(EXTERNAL_ARRAY_BRACKET);
		if (isArray) {
			typeName = className.substring(0, className.indexOf(EXTERNAL_ARRAY_BRACKET));
		} else {
			typeName = className;
		}
		return typeName;
	}


	/**
	 * Returns the internal data-type corresponding with the specified external data-type.
	 *
	 * @param objectClass The class representing the external data-type.
	 * @return The internal data-type corresponding the specifed external data-type.
	 */
	public static DataType getDataTypeOf(Class<?> objectClass) {
		String externalDataTypeName = getExternalTypeNameOf(objectClass);
		return EXTERNAL_NAME_DATA_TYPE_MAP.get(externalDataTypeName);
	}


	/**
	 * Returns the external data-type corresponding with the specified internal data-type and the array-rank.
	 *
	 * @param dataType The internal data-type.
	 * @param rank The array-rank.
	 * @return The external data-type corresponding the specifed external data-type and the array-rank.
	 */
	public static Class<?> getExternalClassOf(DataType dataType, int rank) {
		ExternalType externalType = DATA_TYPE_EXTERNAL_TYPE_MAP.get(dataType);
		if (rank == 0) {
			switch (externalType) {
				case INT64 : return long.class;
				case FLOAT64 : return double.class;
				case BOOL : return boolean.class;
				case STRING : return String.class;
				case ANY : return Object.class;
				case VOID : return Void.class;
				default: throw new VnanoFatalException("Unknown data type: " + dataType);
			}
		}
		if (rank == 1) {
			switch (externalType) {
				case INT64 : return long[].class;
				case FLOAT64 : return double[].class;
				case BOOL : return boolean[].class;
				case STRING : return String[].class;
				case ANY : return Object[].class;
				case VOID : return Void[].class;
				default: throw new VnanoFatalException("Unknown data type: " + dataType);
			}
		}
		if (rank == 2) {
			switch (externalType) {
				case INT64 : return long[][].class;
				case FLOAT64 : return double[][].class;
				case BOOL : return boolean[][].class;
				case STRING : return String[][].class;
				case ANY : return Object[][].class;
				case VOID : return Void[][].class;
				default: throw new VnanoFatalException("Unknown data type: " + dataType);
			}
		}
		if (rank == 3) {
			switch (externalType) {
				case INT64 : return long[][][].class;
				case FLOAT64 : return double[][][].class;
				case BOOL : return boolean[][][].class;
				case STRING : return String[][][].class;
				case ANY : return Object[][][].class;
				case VOID : return Void[][][].class;
				default: throw new VnanoFatalException("Unknown data type: " + dataType);
			}
		}

		String arrayBlocks = "";
		for (int i=0; i<rank; i++) {
			arrayBlocks += "[]";
		}
		throw new VnanoFatalException("Unconvertible array: " + dataType + arrayBlocks);
	}

	public static Class<?>[] getExternalClassesOf(DataType[] dataTypes, int[] arrayRanks){
		int length = dataTypes.length;
		Class<?>[] classes = new Class<?>[length];
		for (int i=0; i<length; i++) {
			classes[i] = getExternalClassOf(dataTypes[i], arrayRanks[i]);
		}
		return classes;
	}


	/**
	 * Returns the array-rank of the specified class.
	 * 
	 * For example, this method returns 3 for int[][][].class, and rerturns 0 for int.class.
	 * 
	 * @param objectClass The class of the external data.
	 * @return The array-rank of the specified class.
	 */
	public static int getArrayRankOf(Class<?> objectClass) {
		String className = objectClass.getCanonicalName();
		int arrayRank = -1;

		// If the class name contains "[]", it is an array class.
		boolean isArray = 0 <= className.indexOf(EXTERNAL_ARRAY_BRACKET);

		if (isArray) {
			// If the class is an array, the number of "[]" in the class name is the array-rank.
			arrayRank = className.split(EXTERNAL_ARRAY_BRACKET_REGEX, -1).length - 1;
		} else {
			// If the class is a scalar, returns 0.
			arrayRank = DataContainer.ARRAY_RANK_OF_SCALAR;
		}
		return arrayRank;
	}

	
	/**
	 * Gets the internal data-type of conversions performed by this instance.
	 *
	 * @return The internal data-type of conversions.
	 */
	public DataType getDataType() {
		return this.dataType;
	}

	/**
	 * Gets the array-rank of conversions performed by this instance.
	 * Note that, the array-rank of a scalar is 0.
	 *
	 * @return The array-rank of conversions.
	 */
	public int getArrayRank() {
		return this.arrayRank;
	}


	/**
	 * Creates a deep-copy of the specified data container.
	 * 
	 * The stored data will also be deep-copied.
	 *
	 * @param srcDataContainer The data container to be deep-copied.
	 * @return The deep-copied instance of the specified data container.
	 */
	public static DataContainer<?> copyDataContainer(DataContainer<?> srcDataContainer) {
		DataContainer<Object> destDataContainer = new DataContainer<Object>();
		int srcRank = srcDataContainer.getArrayRank();
		int[] destLengths = new int[srcRank];
		System.arraycopy(srcDataContainer.getArrayLengths(), 0, destLengths, 0, srcRank);

		Object srcDataOject = srcDataContainer.getArrayData();
		Object destDataObject = null;

		if (srcDataOject instanceof long[]) {
			long[] srcData = (long[])srcDataOject;
			long[] destData = new long[ srcData.length ];
			System.arraycopy(srcData, 0, destData, 0, srcData.length);
			destDataObject = destData;

		} else if (srcDataOject instanceof double[]) {
			double[] srcData = (double[])srcDataOject;
			double[] destData = new double[ srcData.length ];
			System.arraycopy(srcData, 0, destData, 0, srcData.length);
			destDataObject = destData;

		} else if (srcDataOject instanceof boolean[]) {
			boolean[] srcData = (boolean[])srcDataOject;
			boolean[] destData = new boolean[ srcData.length ];
			System.arraycopy(srcData, 0, destData, 0, srcData.length);
			destDataObject = destData;

		} else if (srcDataOject instanceof String[]) {
			String[] srcData = (String[])srcDataOject;
			String[] destData = new String[ srcData.length ];
			System.arraycopy(srcData, 0, destData, 0, srcData.length);
			destDataObject = destData;

		} else {
			throw new VnanoFatalException("Unexpected class for data: " + srcDataOject.getClass().getCanonicalName());
		}

		if (srcRank == ARRAY_RANK_OF_SCALAR) {
			destDataContainer.setArrayData(destDataObject, srcDataContainer.getArrayOffset(), DataContainer.ARRAY_LENGTHS_OF_SCALAR);
		} else {
			destDataContainer.setArrayData(destDataObject, 0, destLengths);
		}

		return destDataContainer;
	}


	/**
	 * Converts an external object to data of the internal data-type, and returns its data container.
	 *
	 * @param externalObject The external object to be converted.
	 * @return The data container in which the converted data is stored.
	 * @throws VnanoException
	 *     Thrown when the data-type of the specified object is incompatible, 
	 *     or the array-rank exceeds the upper limit of this converter.
	 */
	public DataContainer<?> convertToDataContainer(Object externalObject) throws VnanoException {
		DataContainer<?> internalData = (DataContainer<?>)new DataContainer<Void>();
		this.convertToDataContainer(externalObject, internalData);
		return internalData;
	}


	/**
	 * Almost the same method as
	 * {@link DataConverter#convertToDataContainer(Object) convertToDataContainer(Object)},
	 * but this method stores the result to the second argument, instead of returning it.
	 *
	 * @param externalObject The external object to be converted.
	 * @param resultDataContainer The data container to which the converted data is stored.
	 * @throws VnanoException
	 *     Thrown when the data-type of the specified object is incompatible, 
	 *     or the array-rank exceeds the upper limit of this converter.
	 */
	public void convertToDataContainer(Object object, DataContainer<?> resultDataContainer)
			throws VnanoException {

		switch (this.arrayRank) {
			case 0 : {
				this.convertToDataContainer0D(object, resultDataContainer);
				return;
			}
			case 1 : {
				this.convertToDataContainer1D(object, resultDataContainer);
				return;
			}
			case 2 : {
				this.convertToDataContainer2D(object, resultDataContainer);
				return;
			}
			case 3 : {
				this.convertToDataContainer3D(object, resultDataContainer);
				return;
			}
			default : {
				VnanoException e = new VnanoException(
						ErrorType.UNCONVERTIBLE_ARRAY,
						new String[] {getExternalTypeNameOf(object.getClass())}
				);
				throw e;
			}
		}
	}


	/**
	 * Perfroms the internal process of 
	 * {@link DataConverter#convertToDataContainer(Object,DataContainer) convertToDataContainer(Object,DataContainer)}
	 * method, when the array-rank of the data is 0 (scalar).
	 * 
	 * @param externalObject The external object to be converted.
	 * @param resultDataContainer The data container to which the converted data is stored.
	 * @throws VnanoException Thrown when the external data-type of this converter is "void".
	 */
	@SuppressWarnings("unchecked")
	private void convertToDataContainer0D(Object object, DataContainer<?> resultDataContainer)
			throws VnanoException {

		int[] arrayLength = DataContainer.ARRAY_LENGTHS_OF_SCALAR;
		switch (this.externalType) {
			case INT32 : {
				long[] data = new long[]{ ((Integer)object).longValue() };
				((DataContainer<long[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				return;
			}
			case INT64 : {
				long[] data = new long[]{ ((Long)object).longValue() };
				((DataContainer<long[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				return;
			}
			case FLOAT32 : {
				double[] data = new double[]{ ((Float)object).doubleValue() };
				((DataContainer<double[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				return;
			}
			case FLOAT64 : {
				double[] data = new double[]{ ((Double)object).doubleValue() };
				((DataContainer<double[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				return;
			}
			case BOOL : {
				boolean[] data = new boolean[]{ ((Boolean)object).booleanValue() };
				((DataContainer<boolean[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				return;
			}
			case STRING : {
				String[] data = new String[]{ (String)object };
				((DataContainer<String[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				return;
			}
			case ANY : {
				// The "ANY" type is used only when the automatic data conversion is disabled,
				// so it should not be specified as the external data-type of this converter.
				throw new VnanoFatalException("Unexpected conversion executed.");
			}
			case VOID : {
				VnanoException e = new VnanoException(
						ErrorType.UNCONVERTIBLE_DATA_TYPE,
						new String[] { DataTypeName.getDataTypeNameOf(DataType.VOID) }
				);
				throw e;
			}
		}
	}


	/**
	 * Perfroms the internal process of 
	 * {@link DataConverter#convertToDataContainer(Object,DataContainer) convertToDataContainer(Object,DataContainer)}
	 * method, when the array-rank of the data is 1 (1-D array).
	 * 
	 * @param externalObject The external object to be converted.
	 * @param resultDataContainer The data container to which the converted data is stored.
	 */
	@SuppressWarnings("unchecked")
	private void convertToDataContainer1D(Object object, DataContainer<?> resultDataContainer)
			throws VnanoException {

		int[] arrayLength = new int[1];
		int dataLength = -1;
		switch (this.externalType) {
			case INT32 : {
				dataLength = ((int[])object).length; // This is external
				arrayLength[0] = dataLength;
				long[] data = new long[dataLength]; // This is internal
				for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
					((long[])data)[dataIndex] = ((int[])object)[dataIndex];
				}
				((DataContainer<long[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				break;
			}
			case INT64 : {
				dataLength = ((long[])object).length; // This is external
				arrayLength[0] = dataLength;
				long[] data = new long[dataLength]; // This is internal
				for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
					((long[])data)[dataIndex] = ((long[])object)[dataIndex];
				}
				((DataContainer<long[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				break;
			}
			case FLOAT32 : {
				dataLength = ((float[])object).length; // This is external
				arrayLength[0] = dataLength;
				double[] data = new double[dataLength]; // This is internal
				for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
					((double[])data)[dataIndex] = ((float[])object)[dataIndex];
				}
				((DataContainer<double[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				break;
			}
			case FLOAT64 : {
				dataLength = ((double[])object).length; // This is external
				arrayLength[0] = dataLength;
				double[] data = new double[dataLength]; // This is internal
				for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
					((double[])data)[dataIndex] = ((double[])object)[dataIndex];
				}
				((DataContainer<double[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				break;
			}
			case BOOL : {
				dataLength = ((boolean[])object).length; // This is external
				arrayLength[0] = dataLength;
				boolean[] data = new boolean[dataLength]; // This is internal
				for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
					((boolean[])data)[dataIndex] = ((boolean[])object)[dataIndex];
				}
				((DataContainer<boolean[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				break;
			}
			case STRING : {
				dataLength = ((String[])object).length; // This is external
				arrayLength[0] = dataLength;
				String[] data = new String[dataLength]; // This is internal
				for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
					((String[])data)[dataIndex] = ((String[])object)[dataIndex];
				}
				((DataContainer<String[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				break;
			}
			case ANY : {
				// The "ANY" type is used only when the automatic data conversion is disabled,
				// so it should not be specified as the external data-type of this converter.
				throw new VnanoFatalException("Unexpected conversion executed.");
			}
			case VOID : {
				// Void-type array does not exist in external data-types, 
				// so it should not be specified as the external data-type of this converter.
				throw new VnanoFatalException("Unexpected conversion executed.");
			}
		}
	}


	/**
	 * Perfroms the internal process of 
	 * {@link DataConverter#convertToDataContainer(Object,DataContainer) convertToDataContainer(Object,DataContainer)}
	 * method, when the array-rank of the data is 2 (2-D array).
	 * 
	 * @param externalObject The external object to be converted.
	 * @param resultDataContainer The data container to which the converted data is stored.
	 * @param VnanoException thrown when the external data is a "jagged" array.
	 */
	@SuppressWarnings("unchecked")
	private void convertToDataContainer2D(Object object, DataContainer<?> resultDataContainer)
			throws VnanoException {

		int[] arrayLength = new int[2];
		int dataLength = -1;
		switch (this.externalType) {
			case INT32 : {
				arrayLength[0] = ((int[][])object).length; // This is external
				arrayLength[1] = ((int[][])object)[0].length; // This is external
				dataLength = arrayLength[0] * arrayLength[1];
				long[] data = new long[ dataLength ]; // This is internal
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// Check that the external data is not a "jagged" array.
					if ( ((int[][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					// Convert elements of the external array, and store them into the internal array.
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						((long[])data)[dataIndex] = ((int[][])object)[arrayIndex0][arrayIndex1];
						dataIndex++;
					}
				}
				((DataContainer<long[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				break;
			}
			case INT64 : {
				arrayLength[0] = ((long[][])object).length; // This is external
				arrayLength[1] = ((long[][])object)[0].length; // This is external
				dataLength = arrayLength[0] * arrayLength[1];
				long[] data = new long[ dataLength ]; // This is internal
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// Check that the external data is not a "jagged" array.
					if ( ((long[][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					// Convert elements of the external array, and store them into the internal array.
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						((long[])data)[dataIndex] = ((long[][])object)[arrayIndex0][arrayIndex1];
						dataIndex++;
					}
				}
				((DataContainer<long[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				break;
			}
			case FLOAT32 : {
				arrayLength[0] = ((float[][])object).length; // This is external
				arrayLength[1] = ((float[][])object)[0].length; // This is external
				dataLength = arrayLength[0] * arrayLength[1];
				double[] data = new double[ dataLength ]; // This is internal
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// Check that the external data is not a "jagged" array.
					if ( ((float[][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					// Convert elements of the external array, and store them into the internal array.
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						((double[])data)[dataIndex] = ((float[][])object)[arrayIndex0][arrayIndex1];
						dataIndex++;
					}
				}
				((DataContainer<double[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				break;
			}
			case FLOAT64 : {
				arrayLength[0] = ((double[][])object).length; // This is external
				arrayLength[1] = ((double[][])object)[0].length; // This is external
				dataLength = arrayLength[0] * arrayLength[1];
				double[] data = new double[ dataLength ]; // This is internal
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// Check that the external data is not a "jagged" array.
					if ( ((double[][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					// Convert elements of the external array, and store them into the internal array.
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						((double[])data)[dataIndex] = ((double[][])object)[arrayIndex0][arrayIndex1];
						dataIndex++;
					}
				}
				((DataContainer<double[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				break;
			}
			case BOOL : {
				arrayLength[0] = ((boolean[][])object).length; // This is external
				arrayLength[1] = ((boolean[][])object)[0].length; // This is external
				dataLength = arrayLength[0] * arrayLength[1];
				boolean[] data = new boolean[ dataLength ]; // This is internal
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// Check that the external data is not a "jagged" array.
					if ( ((boolean[][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					// Convert elements of the external array, and store them into the internal array.
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						((boolean[])data)[dataIndex] = ((boolean[][])object)[arrayIndex0][arrayIndex1];
						dataIndex++;
					}
				}
				((DataContainer<boolean[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				break;
			}
			case STRING : {
				arrayLength[0] = ((String[][])object).length; // This is external
				arrayLength[1] = ((String[][])object)[0].length; // This is external
				dataLength = arrayLength[0] * arrayLength[1];
				String[] data = new String[ dataLength ]; // This is internal
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// Check that the external data is not a "jagged" array.
					if ( ((String[][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					// Convert elements of the external array, and store them into the internal array.
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						((String[])data)[dataIndex] = ((String[][])object)[arrayIndex0][arrayIndex1];
						dataIndex++;
					}
				}
				((DataContainer<String[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				break;
			}
			case ANY : {
				// The "ANY" type is used only when the automatic data conversion is disabled,
				// so it should not be specified as the external data-type of this converter.
				throw new VnanoFatalException("Unexpected conversion executed.");
			}
			case VOID : {
				// Void-type array does not exist in external data-types, 
				// so it should not be specified as the external data-type of this converter.
				throw new VnanoFatalException("Unexpected conversion executed.");
			}
		}
		return;
	}


	/**
	 * Perfroms the internal process of 
	 * {@link DataConverter#convertToDataContainer(Object,DataContainer) convertToDataContainer(Object,DataContainer)}
	 * method, when the array-rank of the data is 3 (3-D array).
	 * 
	 * @param externalObject The external object to be converted.
	 * @param resultDataContainer The data container to which the converted data is stored.
	 * @param VnanoException thrown when the external data is a "jagged" array.
	 */
	@SuppressWarnings("unchecked")
	private void convertToDataContainer3D(Object object, DataContainer<?> resultDataContainer)
			throws VnanoException {

		int[] arrayLength = new int[3];
		int dataLength = -1;
		switch (this.externalType) {
			case INT32 : {
				arrayLength[0] = ((int[][][])object).length; // This is external
				arrayLength[1] = ((int[][][])object)[0].length; // This is external
				arrayLength[2] = ((int[][][])object)[0][0].length; // This is external
				dataLength = arrayLength[0] * arrayLength[1] * arrayLength[2];
				long[] data = new long[ dataLength ]; // This is internal
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// Check that the external data is not a "jagged" array.
					if ( ((int[][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						// Check that the external data is not a "jagged" array.
						if ( ((int[][][])object)[arrayIndex0][arrayIndex1].length != arrayLength[2] ) {
							throw new VnanoException(ErrorType.JAGGED_ARRAY);
						}
						// Convert elements of the external array, and store them into the internal array.
						for (int arrayIndex2=0; arrayIndex2<arrayLength[2]; arrayIndex2++) {
							((long[])data)[dataIndex] = ((int[][][])object)[arrayIndex0][arrayIndex1][arrayIndex2];
							dataIndex++;
						}
					}
				}
				((DataContainer<long[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				break;
			}
			case INT64 : {
				arrayLength[0] = ((long[][][])object).length; // This is external
				arrayLength[1] = ((long[][][])object)[0].length; // This is external
				arrayLength[2] = ((long[][][])object)[0][0].length; // This is external
				dataLength = arrayLength[0] * arrayLength[1] * arrayLength[2];
				long[] data = new long[ dataLength ]; // This is internal
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// Check that the external data is not a "jagged" array.
					if ( ((long[][][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						// Check that the external data is not a "jagged" array.
						if ( ((long[][][])object)[arrayIndex0][arrayIndex1].length != arrayLength[2] ) {
							throw new VnanoException(ErrorType.JAGGED_ARRAY);
						}
						// Convert elements of the external array, and store them into the internal array.
						for (int arrayIndex2=0; arrayIndex2<arrayLength[2]; arrayIndex2++) {
							((long[])data)[dataIndex] = ((long[][][])object)[arrayIndex0][arrayIndex1][arrayIndex2];
							dataIndex++;
						}
					}
				}
				((DataContainer<long[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				break;
			}
			case FLOAT32 : {
				arrayLength[0] = ((float[][][])object).length; // This is external
				arrayLength[1] = ((float[][][])object)[0].length; // This is external
				arrayLength[2] = ((float[][][])object)[0][0].length; // This is external
				dataLength = arrayLength[0] * arrayLength[1] * arrayLength[2];
				double[] data = new double[ dataLength ]; // This is internal
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// Check that the external data is not a "jagged" array.
					if ( ((float[][][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						// Check that the external data is not a "jagged" array.
						if ( ((float[][][])object)[arrayIndex0][arrayIndex1].length != arrayLength[2] ) {
							throw new VnanoException(ErrorType.JAGGED_ARRAY);
						}
						// Convert elements of the external array, and store them into the internal array.
						for (int arrayIndex2=0; arrayIndex2<arrayLength[2]; arrayIndex2++) {
							((double[])data)[dataIndex] = ((float[][][])object)[arrayIndex0][arrayIndex1][arrayIndex2];
							dataIndex++;
						}
					}
				}
				((DataContainer<double[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				break;
			}
			case FLOAT64 : {
				arrayLength[0] = ((double[][][])object).length; // This is external
				arrayLength[1] = ((double[][][])object)[0].length; // This is external
				arrayLength[2] = ((double[][][])object)[0][0].length; // This is external
				dataLength = arrayLength[0] * arrayLength[1] * arrayLength[2];
				double[] data = new double[ dataLength ]; // This is internal
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// Check that the external data is not a "jagged" array.
					if ( ((double[][][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						// Check that the external data is not a "jagged" array.
						if ( ((double[][][])object)[arrayIndex0][arrayIndex1].length != arrayLength[2] ) {
							throw new VnanoException(ErrorType.JAGGED_ARRAY);
						}
						// Convert elements of the external array, and store them into the internal array.
						for (int arrayIndex2=0; arrayIndex2<arrayLength[2]; arrayIndex2++) {
							((double[])data)[dataIndex] = ((double[][][])object)[arrayIndex0][arrayIndex1][arrayIndex2];
							dataIndex++;
						}
					}
				}
				((DataContainer<double[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				break;
			}
			case BOOL : {
				arrayLength[0] = ((boolean[][][])object).length; // This is external
				arrayLength[1] = ((boolean[][][])object)[0].length; // This is external
				arrayLength[2] = ((boolean[][][])object)[0][0].length; // This is external
				dataLength = arrayLength[0] * arrayLength[1] * arrayLength[2];
				boolean[] data = new boolean[ dataLength ]; // This is internal
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// Check that the external data is not a "jagged" array.
					if ( ((boolean[][][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						// Check that the external data is not a "jagged" array.
						if ( ((boolean[][][])object)[arrayIndex0][arrayIndex1].length != arrayLength[2] ) {
							throw new VnanoException(ErrorType.JAGGED_ARRAY);
						}
						// Convert elements of the external array, and store them into the internal array.
						for (int arrayIndex2=0; arrayIndex2<arrayLength[2]; arrayIndex2++) {
							((boolean[])data)[dataIndex] = ((boolean[][][])object)[arrayIndex0][arrayIndex1][arrayIndex2];
							dataIndex++;
						}
					}
				}
				((DataContainer<boolean[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				break;
			}
			case STRING : {
				arrayLength[0] = ((String[][][])object).length; // This is external
				arrayLength[1] = ((String[][][])object)[0].length; // This is external
				arrayLength[2] = ((String[][][])object)[0][0].length; // This is external
				dataLength = arrayLength[0] * arrayLength[1] * arrayLength[2];
				String[] data = new String[ dataLength ]; // This is internal
				int dataIndex = 0;
				for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
					// Check that the external data is not a "jagged" array.
					if ( ((String[][][])object)[arrayIndex0].length != arrayLength[1] ) {
						throw new VnanoException(ErrorType.JAGGED_ARRAY);
					}
					for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
						// Check that the external data is not a "jagged" array.
						if ( ((String[][][])object)[arrayIndex0][arrayIndex1].length != arrayLength[2] ) {
							throw new VnanoException(ErrorType.JAGGED_ARRAY);
						}
						// Convert elements of the external array, and store them into the internal array.
						for (int arrayIndex2=0; arrayIndex2<arrayLength[2]; arrayIndex2++) {
							((String[])data)[dataIndex] = ((String[][][])object)[arrayIndex0][arrayIndex1][arrayIndex2];
							dataIndex++;
						}
					}
				}
				((DataContainer<String[]>)resultDataContainer).setArrayData(data, 0, arrayLength);
				break;
			}
			case ANY : {
				// The "ANY" type is used only when the automatic data conversion is disabled,
				// so it should not be specified as the external data-type of this converter.
				throw new VnanoFatalException("Unexpected conversion executed.");
			}
			case VOID : {
				// Void-type array does not exist in external data-types, 
				// so it should not be specified as the external data-type of this converter.
				throw new VnanoFatalException("Unexpected conversion executed.");
			}
		}
	}



	/**
	 * Converts an internal data to an object of the corresponding external data-type.
	 * 
	 * For example, an internal "int" value will be converted to an external "Long" type value,
	 * an internal "int[]" array will be converted to an external "long[]" type array,
	 * and an internal "float[][]" array will be converted to an external "double[][]" type array.
	 *
	 * @param dataContainer The container of the data to be converted.
	 * @return The external data converted from the specified internal data.
	 * @throws VnanoException
	 *     Thrown when the data-type of the specified data is "void",
	 *     or the array-rank exceeds the upper limit of this converter.
	 */
	public Object convertToExternalObject(DataContainer<?> dataContainer) throws VnanoException {

		Object internalData = dataContainer.getArrayData();
		int[] arrayLength = dataContainer.getArrayLengths();
		int dataLength = dataContainer.getArraySize();

		switch (this.arrayRank) {

			case DataContainer.ARRAY_RANK_OF_SCALAR : {
				int dataIndex = dataContainer.getArrayOffset();

				switch (this.externalType) {

					case INT32 : {
						return Integer.valueOf( (int)( ((long[])internalData)[dataIndex] ) );
					}
					case INT64 : {
						return Long.valueOf( ((long[])internalData)[dataIndex] );
					}
					case FLOAT32 : {
						return Float.valueOf( (float)( ((double[])internalData)[dataIndex] ) );
					}
					case FLOAT64 : {
						return Double.valueOf( ((double[])internalData)[dataIndex] );
					}
					case BOOL : {
						return Boolean.valueOf( ((boolean[])internalData)[dataIndex] );
					}
					case STRING : {
						return ((String[])internalData)[dataIndex];
					}
					case ANY : {
						// The "ANY" type is used only when the automatic data conversion is disabled,
						// so it should not be specified as the external data-type of this converter.
						throw new VnanoFatalException("Unexpected conversion executed.");
					}
					case VOID : {
						VnanoException e = new VnanoException(
								ErrorType.UNCONVERTIBLE_DATA_TYPE,
								new String[] { DataTypeName.getDataTypeNameOf(DataType.VOID) }
						);
						throw e;
					}
				}
				break;
			}

			case 1 : {
				switch (this.externalType) {
					case INT32 : {
						int[] externalData = new int[dataLength];
						for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
							externalData[dataIndex] = (int)( ((long[])internalData)[dataIndex] );
						}
						return externalData;
					}
					case INT64 : {
						long[] externalData = new long[dataLength];
						for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
							externalData[dataIndex] = ((long[])internalData)[dataIndex];
						}
						return externalData;
					}
					case FLOAT32 : {
						float[] externalData = new float[dataLength];
						for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
							externalData[dataIndex] = (float)( ((double[])internalData)[dataIndex] );
						}
						return externalData;
					}
					case FLOAT64 : {
						double[] externalData = new double[dataLength];
						for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
							externalData[dataIndex] = ((double[])internalData)[dataIndex];
						}
						return externalData;
					}
					case BOOL : {
						boolean[] externalData = new boolean[dataLength];
						for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
							externalData[dataIndex] = ((boolean[])internalData)[dataIndex];
						}
						return externalData;
					}
					case STRING : {
						String[] externalData = new String[dataLength];
						for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
							externalData[dataIndex] = ((String[])internalData)[dataIndex];
						}
						return externalData;
					}
					case ANY : {
						// The "ANY" type is used only when the automatic data conversion is disabled,
						// so it should not be specified as the external data-type of this converter.
						throw new VnanoFatalException("Unexpected conversion executed.");
					}
					case VOID : {
						VnanoException e = new VnanoException(
								ErrorType.UNCONVERTIBLE_DATA_TYPE,
								new String[] { DataTypeName.getDataTypeNameOf(DataType.VOID) }
						);
						throw e;
					}
				}
				break;
			}

			case 2 : {
				switch (this.externalType) {
					case INT32 : {
						int[][] externalData = new int[ arrayLength[0] ][ arrayLength[1] ];
						int dataIndex = 0;
						for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
							for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
								externalData[arrayIndex0][arrayIndex1] = (int)( ((long[])internalData)[dataIndex] );
								dataIndex++;
							}
						}
						return externalData;
					}
					case INT64 : {
						long[][] externalData = new long[ arrayLength[0] ][ arrayLength[1] ];
						int dataIndex = 0;
						for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
							for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
								externalData[arrayIndex0][arrayIndex1] = ((long[])internalData)[dataIndex];
								dataIndex++;
							}
						}
						return externalData;
					}
					case FLOAT32 : {
						float[][] externalData = new float[ arrayLength[0] ][ arrayLength[1] ];
						int dataIndex = 0;
						for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
							for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
								externalData[arrayIndex0][arrayIndex1] = (float)( ((double[])internalData)[dataIndex] );
								dataIndex++;
							}
						}
						return externalData;
					}
					case FLOAT64 : {
						double[][] externalData = new double[ arrayLength[0] ][ arrayLength[1] ];
						int dataIndex = 0;
						for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
							for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
								externalData[arrayIndex0][arrayIndex1] = ((double[])internalData)[dataIndex];
								dataIndex++;
							}
						}
						return externalData;
					}
					case BOOL : {
						boolean[][] externalData = new boolean[ arrayLength[0] ][ arrayLength[1] ];
						int dataIndex = 0;
						for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
							for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
								externalData[arrayIndex0][arrayIndex1] = ((boolean[])internalData)[dataIndex];
								dataIndex++;
							}
						}
						return externalData;
					}
					case STRING : {
						String[][] externalData = new String[ arrayLength[0] ][ arrayLength[1] ];
						int dataIndex = 0;
						for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
							for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
								externalData[arrayIndex0][arrayIndex1] = ((String[])internalData)[dataIndex];
								dataIndex++;
							}
						}
						return externalData;
					}
					case ANY : {
						// The "ANY" type is used only when the automatic data conversion is disabled,
						// so it should not be specified as the external data-type of this converter.
						throw new VnanoFatalException("Unexpected conversion executed.");
					}
					case VOID : {
						VnanoException e = new VnanoException(
								ErrorType.UNCONVERTIBLE_DATA_TYPE,
								new String[] { DataTypeName.getDataTypeNameOf(DataType.VOID) }
						);
						throw e;
					}
				}
			}

			case 3 : {
				switch (this.externalType) {
					case INT32 : {
						int[][][] externalData = new int[ arrayLength[0] ][ arrayLength[1] ][ arrayLength[2] ];
						int dataIndex = 0;
						for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
							for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
								for (int arrayIndex2=0; arrayIndex2<arrayLength[2]; arrayIndex2++) {
									externalData[arrayIndex0][arrayIndex1][arrayIndex2] = (int)( ((long[])internalData)[dataIndex] );
									dataIndex++;
								}
							}
						}
						return externalData;
					}
					case INT64 : {
						long[][][] externalData = new long[ arrayLength[0] ][ arrayLength[1] ][ arrayLength[2] ];
						int dataIndex = 0;
						for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
							for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
								for (int arrayIndex2=0; arrayIndex2<arrayLength[2]; arrayIndex2++) {
									externalData[arrayIndex0][arrayIndex1][arrayIndex2] = ((long[])internalData)[dataIndex];
									dataIndex++;
								}
							}
						}
						return externalData;
					}
					case FLOAT32 : {
						float[][][] externalData = new float[ arrayLength[0] ][ arrayLength[1] ][ arrayLength[2] ];
						int dataIndex = 0;
						for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
							for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
								for (int arrayIndex2=0; arrayIndex2<arrayLength[2]; arrayIndex2++) {
									externalData[arrayIndex0][arrayIndex1][arrayIndex2] = (float)( ((double[])internalData)[dataIndex] );
									dataIndex++;
								}
							}
						}
						return externalData;
					}
					case FLOAT64 : {
						double[][][] externalData = new double[ arrayLength[0] ][ arrayLength[1] ][ arrayLength[2] ];
						int dataIndex = 0;
						for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
							for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
								for (int arrayIndex2=0; arrayIndex2<arrayLength[2]; arrayIndex2++) {
									externalData[arrayIndex0][arrayIndex1][arrayIndex2] = ((double[])internalData)[dataIndex];
									dataIndex++;
								}
							}
						}
						return externalData;
					}
					case BOOL : {
						boolean[][][] externalData = new boolean[ arrayLength[0] ][ arrayLength[1] ][ arrayLength[2] ];
						int dataIndex = 0;
						for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
							for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
								for (int arrayIndex2=0; arrayIndex2<arrayLength[2]; arrayIndex2++) {
									externalData[arrayIndex0][arrayIndex1][arrayIndex2] = ((boolean[])internalData)[dataIndex];
									dataIndex++;
								}
							}
						}
						return externalData;
					}
					case STRING : {
						String[][][] externalData = new String[ arrayLength[0] ][ arrayLength[1] ][ arrayLength[2] ];
						int dataIndex = 0;
						for (int arrayIndex0=0; arrayIndex0<arrayLength[0]; arrayIndex0++) {
							for (int arrayIndex1=0; arrayIndex1<arrayLength[1]; arrayIndex1++) {
								for (int arrayIndex2=0; arrayIndex2<arrayLength[2]; arrayIndex2++) {
									externalData[arrayIndex0][arrayIndex1][arrayIndex2] = ((String[])internalData)[dataIndex];
									dataIndex++;
								}
							}
						}
						return externalData;
					}
					case ANY : {
						// The "ANY" type is used only when the automatic data conversion is disabled,
						// so it should not be specified as the external data-type of this converter.
						throw new VnanoFatalException("Unexpected conversion executed.");
					}
					case VOID : {
						VnanoException e = new VnanoException(
								ErrorType.UNCONVERTIBLE_DATA_TYPE,
								new String[] { DataTypeName.getDataTypeNameOf(DataType.VOID) }
						);
						throw e;
					}
				}
			}

			default : {

				// Prepare a string representing the data-type/array-rank, (e.g. "int[][][]"), 
				// for embedding it into the error message.
				String externalTypeName = DataConverter.getExternalTypeNameOf(internalData.getClass());
				DataType internalType = EXTERNAL_NAME_DATA_TYPE_MAP.get(externalTypeName);
				String internalTypeName = DataTypeName.getDataTypeNameOf(internalType);
				String internalArrayTypeName = internalTypeName;
				for(int dim=0; dim<this.arrayRank; dim++) {
					internalArrayTypeName += ScriptWord.SUBSCRIPT_BEGIN + ScriptWord.SUBSCRIPT_END; // "[]" を追加
				}

				VnanoException e = new VnanoException(
						ErrorType.UNCONVERTIBLE_INTERNAL_ARRAY,
						new String[] {internalArrayTypeName}
				);
				throw e;
			}
		}

		// The processing flow will not reach to this line, if it works as we expected.
		throw new VnanoFatalException("Unexpected conversion executed.");
	}

}
