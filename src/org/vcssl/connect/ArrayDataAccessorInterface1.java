/*
 * ==================================================
 * Array Data Accessor Interface 1 (ADAI 1)
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2017-2022 by RINEARN
 * ==================================================
 */

// THE SPECIFICATION OF THIS INTERFACE HAD BEEN FINALIZED AT 2022/08/31.
// NO MODIFICATIONS WILL BE APPLIED FOR THIS INTERFACE, EXCLUDING DOCUMENTS/COMMENTS.

package org.vcssl.connect;

/**
 * A data-I/O interface (abbreviated as ADAI1), mainly implemented by data container objects of language processor systems.
 *
 * In this org.vcssl.connect package, multiple data I/O interfaces are provided for passing/receiving data 
 * without any data-conversions, between script-engine-side and plug-in-side, if required.
 * 
 * In them, this interface ADAI1 provides I/O methods of multi-dimensional array data.
 * 
 * @param <T> The type of data stored in the data container implementing this interface.
 */
public interface ArrayDataAccessorInterface1<T> {


	/** The type ID of this interface (value: "ADAI") referred when the plug-in will be loaded. */
	public static final String INTERFACE_TYPE_ID = "ADAI";

	/** The generation of this interface (value: "1"). */
	public static final String INTERFACE_GENERATION = "1";

 	/** The value of the size of a scalar value (value: 1). */
	public static final int ARRAY_SIZE_OF_SCALAR = 1;

 	/** The number of dimensions (array-rank) of a scalar value (value: 0). */
	public static final int ARRAY_RANK_OF_SCALAR = 0;

	/** An array storing dimension-lengths of a scalar value (value: { }). */
	public static final int[] ARRAY_LENGTHS_OF_SCALAR = { };


	/**
	 * Sets the serialized 1D array data with related information.
	 *
	 * On this interface, contents of any data including a scalar value, an 1-dimensional (1D) array, 
	 * and a multi-dimensional array are always handled as an 1D array, 
	 * so specify an 1D array for the argument "data".
	 * We call it as "serialized 1D array data" in this document. 
	 * 
	 * How to store elements into the serialized 1D array data is explained in the specification document,
	 * bundled with this interface file.
	 * 
	 * @param data The serialized 1D array data to be set.
	 * @param offset The index at wich the scalar value is stored(see the above explanation).
	 * @param lengths An array storing lengths of each dimension (see the above explanation).
	 */
	public abstract void setArrayData(T data, int offset, int[] lengths);


	/**
	 * Returns the serialized 1D the array data.
	 * 
	 * How a scalar or a multi dimensional array is stored in the serialized 1D array data
	 * is explained in the specification document, bundled with this interface file.
	 * 
	 * @return The serialized 1D array data.
	 */
	public abstract T getArrayData();


	/**
	 * Returns whether any serialized 1D array data can be gotten.
	 * 
 	 * @return Returns true if any serialized 1D array data can be gotton.
	 */
	public abstract boolean hasArrayData();


	/**
	 * Returns the index of the scalar value in the serialized 1D array data.
	 * 
	 * For details, see the description of "setArrayData" method in the specification document,
	 * bundled with this interface file.
	 *
 	 * @return The index of the scalar value in the serialized 1D array data.
	 */
	public abstract int getArrayOffset();


	/**
	 * Returns the array storing lengths of dimensions of the array.
	 * 
	 * For details, see the description of "setArrayData" method in the specification document,
	 * bundled with this interface file.
	 * 
	 * @return The array storing lengths of dimensions of the array.
	 */
	public abstract int[] getArrayLengths();


	/**
	 * Returns the size (lenth) of the serialized 1D array data.
	 * 
	 * When data is a scalar value, the size always is 1.
	 * When data is an 1-dimensional array, the size is its length.
	 * When data is a multi-dimensional array of which lengths are [N1][N2][N3]...[NM], 
	 * the size is the value of the product of them: N1*n2*n3*...*NM.
	 * 
	 * @return The size (lenth) of the serialized 1D array data.
	 */
	public abstract int getArraySize();


	/**
	 * Returns the number of dimensions (array-rank) of the array.
	 *
	 * When data is a scalar value, the array-rank always is 0.
	 * When data is an 1-dimensional array, the array-rank always is 1.
	 * When data is an N-dimensional array, the array-rank is N.
	 * 
	 * @return The number of dimensions (array-rank) of multi-dimensional array data.
	 */
	public abstract int getArrayRank();

}
