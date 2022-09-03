/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.memory;

import java.util.HashMap;

import org.vcssl.connect.ArrayDataAccessorInterface1;
import org.vcssl.connect.BoolScalarDataAccessorInterface1;
import org.vcssl.connect.Float64ScalarDataAccessorInterface1;
import org.vcssl.connect.Int64ScalarDataAccessorInterface1;
import org.vcssl.connect.StringScalarDataAccessorInterface1;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.DataType;


/**
 * The class of the data-container, which is the unit to store data into the memory, 
 * in the virtual machine of the Vnano.
 * 
 * The architecture of the VM of the Vnano is a kind of a vector processor, 
 * so any data is stored in a data-container as an 1D array (the "data" field), 
 * even if it is a scalar.
 * 
 * When a data container represents a scalar, the scalar value is stored in the "data" field array, 
 * and the "offset" field points to the index at which the scalar value is stored.
 * 
 * When a data container represents a 1D array, it is stored in the "data" field as it is.
 * 
 * When a data container represents a multi dimensional array, it is stored in the "data", 
 * with serialized as a 1D array.
 * 
 * Also, the length of each dimension of the array are stored in the "lengths" field.
 * For example, the "lengths" of an array[10] is { 10 }, the "lengths" of an array[1][2][3] is {1, 2, 3}, 
 * and the "lengths" of a scalar is an empty { }.
 * 
 * We call the number of the dimensions (= the length of the "lengths" field) of an array as the "array-rank".
 * To keep consistency with the above specification about the "lengths" field, 
 * we define the array-rank of a scalar as 0.
 * 
 * As similar field as the "lengths", the "size" field is also defined.
 * It means the number of elements of the array represented by this data-container. 
 * Note that, the "size" necessarily doesn't match with the number of elements of "data" field.
 * For example, when a data-container represents a scalar value, the "size" of it is 1, 
 * but the number of elements of "data" field may be more long, 
 * and in which the scalar value is stored as the "offset"-th element.
 * 
 * This data-container also has the "size" field.
 *
 * @param <T> The type of the data to be stored (specify the type of 1D array, even when it stores a scalar or a multi dimensional array).
 */
public class DataContainer<T> implements ArrayDataAccessorInterface1<T>,
		Float64ScalarDataAccessorInterface1, Int64ScalarDataAccessorInterface1,
		BoolScalarDataAccessorInterface1, StringScalarDataAccessorInterface1 {

	/** The array-rank of a scalar value. */
	public static final int   ARRAY_RANK_OF_SCALAR = 0;

	/** The array-lengths of a scalar value. */
	public static final int[] ARRAY_LENGTHS_OF_SCALAR = { };

	/** The size of a scalar value. */
	public static final int   ARRAY_SIZE_OF_SCALAR = 1;


	/**
	 * The Map for converting a class specified as the type parameter T 
	 * to the corresponding element of the {@link org.vcssl.nano.spec.DataType DataType} enum.
	 */
	private static final HashMap<Class<?>, DataType> CLASS_DATA_TYPE_MAP = new HashMap<Class<?>, DataType>();
	static {
		CLASS_DATA_TYPE_MAP.put(long[].class, DataType.INT64);
		CLASS_DATA_TYPE_MAP.put(double[].class, DataType.FLOAT64);
		CLASS_DATA_TYPE_MAP.put(boolean[].class, DataType.BOOL);
		CLASS_DATA_TYPE_MAP.put(String[].class, DataType.STRING);
	}


	/** The serialized 1D array data, of the scalar/array represented by this data-container. */
	private T data;

	/** The number of elements of the array represented by this data container. */
	private int size;

	/** Stores the length of each dimension of the array represented by this data container. */
	private int[] lengths;

	// CAUTION: The above array might be shared between multiple data-containers, so don't modify its value.
	//          When you change the lengths, create an new array and set.

	/** Represents the index in the "data" field array, at which the scalar value is stored. */
	private int offset;

	/**
	 * Stores the root data-container of the tree of references, when this data-container is referencing to the other data-container.
	 * 
	 * In general, the referenced data-container also may reference to the other data-container, 
	 * so the relationship of references between data-container become forms a tree-shape (we calls it as a "reference tree").
	 * It is a waste of processing cost that walks in a reference tree to the root, for each data-I/O to the data belongs to the tree.
	 * So all data-containers belongs to a reference tree caches the reference to the root as the following field.
	 */
	private DataContainer<T> referenceTreeRoot;


	/**
	 * Creates an empty data-container.
	 *
	 * For storing data, use data-setter methods: setArrayData(...), setFloat64ScalarData(...), and so on.
	 */
	public DataContainer() {
		this.initialize();
	}


	/**
	 * Initializes/re-initializes this instace.
	 * 
	 * Initialized instance by this method stores no actual data yet, 
	 * so for storing data, use data-setter methods: setArrayData(...), setFloat64ScalarData(...), and so on.
	 */
	public final void initialize() {
		this.data = null;
		this.referenceTreeRoot = null;
		this.size = ARRAY_SIZE_OF_SCALAR;
		this.lengths = ARRAY_LENGTHS_OF_SCALAR;
		this.offset = 0;
	}


	/**
	 * Set the data to this data-container.
	 * 
	 * @param data The serialized 1d array data to be stored.
	 * @param offset If this data-container represents a scalar, specify the index in the "data" at which the scalar value is stored, and otherwise specify 0.
	 * @param lengths The array storing the length of each dimension of the array represented by this data-container. 
	 */
	@Override
	public final void setArrayData(T data, int offset, int[] lengths) {

		// Compute the size: equals to the product of lengths of all dimensions.
		int productOfLengths = 1;
		for (int length: lengths) {
			productOfLengths *= length;
		}

		// If this data-container don't refer to other container:
		// Update fields of this container.
		if (this.referenceTreeRoot == null) {
			this.data = data;
			this.offset = offset;
			this.lengths = lengths;
			this.size = productOfLengths;

		// If this data-container is referencing to other container: 
		// Update fields of the root container of the reference tree to which this container belongs.
		} else {
			// Don't call referenceTreeRoot.setArrayData(...), because we want to eliminate recursive calls in this script engine, for any case.
			this.referenceTreeRoot.data = data;
			this.referenceTreeRoot.offset = offset;
			this.referenceTreeRoot.lengths = lengths;
			this.referenceTreeRoot.size = productOfLengths;
		}
	}


	/**
	 * Get the serialized 1D array data stored in this data-container.
	 *
	 * @return data The serialized 1D array data.
	 */
	@Override
	public final T getArrayData() {
		return (this.referenceTreeRoot == null) ? this.data : this.referenceTreeRoot.data;
	}


	/**
	 * Returns whether this data-container has any serialized 1D array data.
	 * 
	 * Note that, when this container stores a scalar value, it is internally stored in a 1D array, so this method returns true.
	 * This method returns false only when this container has completely no data.
	 *
	 * @return Returns true if this data-container has any serialized 1D array data.
	 */
	@Override
	public final boolean hasArrayData() {
		if (this.referenceTreeRoot == null) {
			return this.data != null;
		} else {
			return this.referenceTreeRoot.data != null;
		}
	}


	/**
	 * Gets the "offset" value of this data-container.
	 * 
	 * When this data-container represents a scalar, 
	 * the scalar value is stored at the "offset"-th index, in the serialized 1D array.
	 *
	 * @return The "offset" value.
	 */
	@Override
	public final int getArrayOffset() {
		return (this.referenceTreeRoot == null) ? this.offset : this.referenceTreeRoot.offset;
	}


	/**
	 * Gets the "size" value of this data-container.
	 * 
	 * The "size"value  means the number of elements of the array represented by this data-container. 
	 * Note that, the "size" necessarily doesn't match with the number of elements of "data" field.
	 * For example, when a data-container represents a scalar value, the "size" of it is 1, 
	 * but the number of elements of "data" field may be more long, 
	 * and in which the scalar value is stored as the "offset"-th element.
	 *
	 * @return The "size" value.
	 */
	@Override
	public final int getArraySize() {
		return (this.referenceTreeRoot == null) ? this.size : this.referenceTreeRoot.size;
	}


	/**
	 * Gets the array storing the length of each dimension of the array, represented by this data-container.
	 * We call it as "array-lengths", or simply "lengths".
	 * 
	 * For example, the "lengths" of an array[10] is { 10 }, the "lengths" of an array[1][2][3] is {1, 2, 3}, 
	 * and the "lengths" of a scalar is an empty { }.
	 *
	 * @return The array storing the length of each dimension of the array ("array-lengths").
	 */
	@Override
	public final int[] getArrayLengths() {
		return (this.referenceTreeRoot == null) ? this.lengths : this.referenceTreeRoot.lengths;
	}


	/**
	 * Gets the number of dimensions of the array represented by this data-container.
	 * We call it as "array-rank".
	 * 
	 * Note that, we define that the array-rank of a scalar is 0.
	 *
	 * @return The number of dimensions of the array ("array-rank")
	 */
	@Override
	public final int getArrayRank() {
		return (this.referenceTreeRoot == null) ? this.lengths.length : this.referenceTreeRoot.lengths.length;
	}


	/**
	 * Gets the data-type of the data stored in this data-container.
	 * The returned data type is independent of whether this data-container represents a scalar or an array.
	 * 
	 * For example, this method returns {@link org.vcssl.nano.spec.DataType#STRING STRING} for all of the following cases: 
	 * When this container represents a string-type scalar, a string-type 1D array, a string-type 3D array, and so on.
	 * 
	 * Also, when this container stores no data, returns {@link org.vcssl.nano.spec.DataType#VOID VOID}.
	 *
	 * @return The data-type of the data stored in this data-container.
	 */
	public final DataType getDataType() {

		// Converts (resolves) the class of the stored data to the element of DataType enum.
		T storingData = (this.referenceTreeRoot == null) ? this.data : this.referenceTreeRoot.data;
		DataType dataType = DataType.VOID;
		if (storingData != null) {
			dataType = CLASS_DATA_TYPE_MAP.get(storingData.getClass());

			// Regards an unknown type to VOID.
			// (Might be better to define "UNKNOWN" type in DataType enum, in future.)
			if (dataType == null) {
				dataType = DataType.VOID;
			}
		}

		return dataType;

		// Why this data-container don't have the data-type as a field:
		// 
		// It may seems to be more natural to do the above class-DataType resolution 
		// at the timing when the data is set to this container, and store the resolved data-type as a field, 
		// and simply returns it by this method.
		// 
		// However, if we do so, it requires to do the above resolution 
		// for every time when data is set/updated. It may become a bottleneck of data I/O.
		// 
		// On the other hand, this "getter" of the data-type is rarely called in run-time,
		// because the Vnano is a static typing language. So it don't be a bottleneck of data I/O.
		// 
		// Therefore we do the data-type resolution in this "getter", not the "setter" of data, 
		// so it dont require the field for storing the resolved data-type.
	}


	/**
	 * Sets this data-container to refers to the specified data-container.
	 * 
	 * If the referred container is also referring to an other container, 
	 * this container refers to the container at the root of the reference tree.
	 * 
	 * After setting the reference by this method, 
	 * all I/O operations from/to this container will be synchronized to the referenced container.
	 *
	 * @param referredDataContainer The data-container to be referred.
	 */
	public final void refer(DataContainer<T> referredDataContainer) {
		this.referenceTreeRoot = referredDataContainer;
		while (this.referenceTreeRoot.referenceTreeRoot != null) {
			this.referenceTreeRoot = this.referenceTreeRoot.referenceTreeRoot;
		}
	}


	/**
	 * Cancels the setting of the reference by "refer" method.
	 */
	public final void derefer() {
		this.referenceTreeRoot = null;
	}


	/**
	 * Stores a double ({@link org.vcssl.nano.spec.DataType#FLOAT64 FLOAT64}) type scalar data.
	 * 
	 * For using this method,
	 * it is necessary that the "data" field is uninitialized yet, or initialized by the same data-type.
	 * If the data-type of the "data" field is incompatible, the VnanoFatalException will be thrown.
	 *
	 * @param data The scalar data to be stored.
	 * @throws VnanoFatalException Thrown when the data-type of the "data" field is incompatible.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public final void setFloat64ScalarData(double data) {
		this.size = ARRAY_SIZE_OF_SCALAR;
		this.lengths = ARRAY_LENGTHS_OF_SCALAR;

		// Get the serialized 1D data array of this container.
		// ( !!! CAUTION !!!  Don't access to "data" field directly here, because this container may refer to the other container. )
		Object arrayData = this.getArrayData();

		// If the serialized 1D data array is already initialized by the compatible type, simply set the scalar data in it.
		if (arrayData instanceof double[]) {
			( (double[])arrayData )[ this.getArrayOffset() ] = data;

		// If the array is uninitialized yet, allocate a new array storing the scalar data, and set it to this container.
		} else if (arrayData == null) {
			try {
				T newArrayData = (T)( new double[]{ data } );
				this.setArrayData(newArrayData, 0, ARRAY_LENGTHS_OF_SCALAR);
			} catch (ClassCastException cce) {
				throw new VnanoFatalException("Data type is incorrect");
			}

		// If the array is initialized by an incompatible data-type: error.
		} else {
			throw new VnanoFatalException("Data type is incorrect");
		}
	}


	/**
	 * Gets the double ({@link org.vcssl.nano.spec.DataType#FLOAT64 FLOAT64}) type scalar data, stored in this container.
	 *
	 * @return The stored scalar data.
	 * @throws VnanoFatalException Thrown when incompatible data is stored, or no data is stored.
	 */
	@Override
	public final double getFloat64ScalarData() {

		// Get the serialized 1D data array of this container.
		// ( !!! CAUTION !!!  Don't access to "data" field directly here, because this container may refer to the other container. )
		Object arrayData = this.getArrayData();

		if (arrayData instanceof double[]) {
			return ( (double[])arrayData )[ this.getArrayOffset() ];
		} else if (arrayData == null) {
			throw new VnanoFatalException("No data is stored");
		} else {
			throw new VnanoFatalException("Data type is incorrect");
		}
	}


	/**
	 * Returns whether this data-container stores a double ({@link org.vcssl.nano.spec.DataType#FLOAT64 FLOAT64}) type scalar data.
	 *
	 * @return Returns true if this data-container stores it.
	 */
	@Override
	public final boolean hasFloat64ScalarData() {
		if (this.getArrayData() == null) {
			return false;
		}
		if (!(this.getArrayData() instanceof double[])) {
			return false;
		}
		if (this.getArrayRank() == ARRAY_RANK_OF_SCALAR) {
			return false;
		}
		return true;
	}


	/**
	 * Stores a long ({@link org.vcssl.nano.spec.DataType#INT64 INT64}) type scalar data.
	 * 
	 * For using this method,
	 * it is necessary that the "data" field is uninitialized yet, or initialized by the same data-type.
	 * If the data-type of the "data" field is incompatible, the VnanoFatalException will be thrown.
	 *
	 * @param data The scalar data to be stored.
	 * @throws VnanoFatalException Thrown when the data-type of the "data" field is incompatible.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public final void setInt64ScalarData(long data) {
		this.size = ARRAY_SIZE_OF_SCALAR;
		this.lengths = ARRAY_LENGTHS_OF_SCALAR;

		// Get the serialized 1D data array of this container.
		// ( !!! CAUTION !!!  Don't access to "data" field directly here, because this container may refer to the other container. )
		Object arrayData = this.getArrayData();

		// If the serialized 1D data array is already initialized by the compatible type, simply set the scalar data in it.
		if (arrayData instanceof long[]) {
			( (long[])arrayData )[ this.getArrayOffset() ] = data;

		// If the array is uninitialized yet, allocate a new array storing the scalar data, and set it to this container.
		} else if (arrayData == null) {
			try {
				T newArrayData = (T)( new long[]{ data } );
				this.setArrayData(newArrayData, 0, ARRAY_LENGTHS_OF_SCALAR);
			} catch (ClassCastException cce) {
				throw new VnanoFatalException("Data type is incorrect");
			}

		// If the array is initialized by an incompatible data-type: error.
		} else {
			throw new VnanoFatalException("Data type is incorrect");
		}
	}


	/**
	 * Gets the long ({@link org.vcssl.nano.spec.DataType#INT64 INT64}) type scalar data, stored in this container.
	 *
	 * @return The stored scalar data.
	 * @throws VnanoFatalException Thrown when incompatible data is stored, or no data is stored.
	 */
	@Override
	public final long getInt64ScalarData() {

		// Get the serialized 1D data array of this container.
		// ( !!! CAUTION !!!  Don't access to "data" field directly here, because this container may refer to the other container. )
		Object arrayData = this.getArrayData();

		if (arrayData instanceof long[]) {
			return ( (long[])arrayData )[ this.getArrayOffset() ];
		} else if (arrayData == null) {
			throw new VnanoFatalException("No data is stored");
		} else {
			throw new VnanoFatalException("Data type is incorrect");
		}
	}


	/**
	 * Returns whether this data-container stores a long ({@link org.vcssl.nano.spec.DataType#INT64 INT64}) type scalar data.
	 *
	 * @return Returns true if this data-container stores it.
	 */
	@Override
	public final boolean hasInt64ScalarData() {
		if (this.getArrayData() == null) {
			return false;
		}
		if (!(this.getArrayData() instanceof long[])) {
			return false;
		}
		if (this.getArrayRank() == ARRAY_RANK_OF_SCALAR) {
			return false;
		}
		return true;
	}


	/**
	 * Stores a boolean ({@link org.vcssl.nano.spec.DataType#BOOL BOOL}) type scalar data.
	 * 
	 * For using this method,
	 * it is necessary that the "data" field is uninitialized yet, or initialized by the same data-type.
	 * If the data-type of the "data" field is incompatible, the VnanoFatalException will be thrown.
	 *
	 * @param data The scalar data to be stored.
	 * @throws VnanoFatalException Thrown when the data-type of the "data" field is incompatible.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public final void setBoolScalarData(boolean data) {
		this.size = ARRAY_SIZE_OF_SCALAR;
		this.lengths = ARRAY_LENGTHS_OF_SCALAR;

		// Get the serialized 1D data array of this container.
		// ( !!! CAUTION !!!  Don't access to "data" field directly here, because this container may refer to the other container. )
		Object arrayData = this.getArrayData();

		// If the serialized 1D data array is already initialized by the compatible type, simply set the scalar data in it.
		if (arrayData instanceof boolean[]) {
			( (boolean[])arrayData )[ this.getArrayOffset() ] = data;

		// If the array is uninitialized yet, allocate a new array storing the scalar data, and set it to this container.
		} else if (arrayData == null) {
			try {
				T newArrayData = (T)( new boolean[]{ data } );
				this.setArrayData(newArrayData, 0, ARRAY_LENGTHS_OF_SCALAR);
			} catch (ClassCastException cce) {
				throw new VnanoFatalException("Data type is incorrect");
			}

		// If the array is initialized by an incompatible data-type: error.
		} else {
			throw new VnanoFatalException("Data type is incorrect");
		}
	}


	/**
	 * Gets the boolean ({@link org.vcssl.nano.spec.DataType#BOOL BOOL}) type scalar data, stored in this container.
	 *
	 * @return The stored scalar data.
	 * @throws VnanoFatalException Thrown when incompatible data is stored, or no data is stored.
	 */
	@Override
	public final boolean getBoolScalarData() {

		// Get the serialized 1D data array of this container.
		// ( !!! CAUTION !!!  Don't access to "data" field directly here, because this container may refer to the other container. )
		Object arrayData = this.getArrayData();

		if (arrayData instanceof boolean[]) {
			return ( (boolean[])arrayData )[ this.getArrayOffset() ];
		} else if (arrayData == null) {
			throw new VnanoFatalException("No data is stored");
		} else {
			throw new VnanoFatalException("Data type is incorrect");
		}
	}


	/**
	 * Returns whether this data-container stores a boolean ({@link org.vcssl.nano.spec.DataType#BOOL BOOL}) type scalar data.
	 *
	 * @return Returns true if this data-container stores it.
	 */
	@Override
	public final boolean hasBoolScalarData() {
		if (this.getArrayData() == null) {
			return false;
		}
		if (!(this.getArrayData() instanceof boolean[])) {
			return false;
		}
		if (this.getArrayRank() == ARRAY_RANK_OF_SCALAR) {
			return false;
		}
		return true;
	}


	/**
	 * Stores a String ({@link org.vcssl.nano.spec.DataType#STRING STRING}) type scalar data.
	 * 
	 * For using this method,
	 * it is necessary that the "data" field is uninitialized yet, or initialized by the same data-type.
	 * If the data-type of the "data" field is incompatible, the VnanoFatalException will be thrown.
	 *
	 * @param data The scalar data to be stored.
	 * @throws VnanoFatalException Thrown when the data-type of the "data" field is incompatible.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public final void setStringScalarData(String data) {
		this.size = ARRAY_SIZE_OF_SCALAR;
		this.lengths = ARRAY_LENGTHS_OF_SCALAR;

		// Get the serialized 1D data array of this container.
		// ( !!! CAUTION !!!  Don't access to "data" field directly here, because this container may refer to the other container. )
		Object arrayData = this.getArrayData();

		// If the serialized 1D data array is already initialized by the compatible type, simply set the scalar data in it.
		if (arrayData instanceof String[]) {
			( (String[])arrayData )[ this.getArrayOffset() ] = data;

		// If the array is uninitialized yet, allocate a new array storing the scalar data, and set it to this container.
		} else if (arrayData == null) {
			try {
				T newArrayData = (T)( new String[]{ data } );
				this.setArrayData(newArrayData, 0, ARRAY_LENGTHS_OF_SCALAR);
			} catch (ClassCastException cce) {
				throw new VnanoFatalException("Data type is incorrect");
			}

		// If the array is initialized by an incompatible data-type: error.
		} else {
			throw new VnanoFatalException("Data type is incorrect");
		}
	}


	/**
	 * Gets the String ({@link org.vcssl.nano.spec.DataType#STRING STRING}) type scalar data, stored in this container.
	 *
	 * @return The stored scalar data.
	 * @throws VnanoFatalException Thrown when incompatible data is stored, or no data is stored.
	 */
	@Override
	public final String getStringScalarData() {

		// Get the serialized 1D data array of this container.
		// ( !!! CAUTION !!!  Don't access to "data" field directly here, because this container may refer to the other container. )
		Object arrayData = this.getArrayData();

		if (arrayData instanceof String[]) {
			return ( (String[])arrayData )[ this.getArrayOffset() ];
		} else if (arrayData == null) {
			throw new VnanoFatalException("No data is stored");
		} else {
			throw new VnanoFatalException("Data type is incorrect");
		}
	}


	/**
	 * Returns whether this data-container stores a String ({@link org.vcssl.nano.spec.DataType#STRING STRING}) type scalar data.
	 *
	 * @return Returns true if this data-container stores it.
	 */
	@Override
	public final boolean hasStringScalarData() {
		if (this.getArrayData() == null) {
			return false;
		}
		if (!(this.getArrayData() instanceof String[])) {
			return false;
		}
		if (this.getArrayRank() == ARRAY_RANK_OF_SCALAR) {
			return false;
		}
		return true;
	}

}
