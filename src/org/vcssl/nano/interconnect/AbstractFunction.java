/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.vm.memory.DataContainer;


/**
 * The abstract class of functions processable in Vnano Engine.
 * 
 * In Vnano Engine, internal functions will be handled as instances of 
 * {@link InternalFunction InternalFunction} class,
 * and it is a subclass of this abstract class.
 * 
 * In addition, external function plug-ins will be connected through 
 * adapters extending this abstract class, 
 * e.g.: {@link Xfci1ToFunctionAdapter Xfci1ToFunctionAdapter}.
 */
public abstract class AbstractFunction {


	/**
	 * The empty constructor.
	 */
	protected AbstractFunction() {}


	/**
	 * Sets the name of this function.
	 * 
	 * This method is used for setting an alias for an external function.
	 * This method isn't available when this function is an internal function, 
	 * because the name of internal functions isn't modifiable.
	 * 
	 * @param functionName The name of this function.
	 * @throws VnanoFatalException
	 *      Thrown when the name of this function isn't modifiable.
	 */
	public abstract void setFunctionName(String functionName);


	/**
	 * Gets the name of this function.
	 *
	 * @return The name of this function.
	 */
	public abstract String getFunctionName();


	/**
	 * Returns whether this function belongs to any namespace.
	 *
	 * @return Returns true if this function belongs to a namespace.
	 */
	public abstract boolean hasNamespaceName();


	/**
	 * Gets the name of the namespace to which this function belongs.
	 *
	 * @return The name of the namespace to which this function belongs.
	 */
	public abstract String getNamespaceName();


	/**
	 * Sets the name of the namespace to which this funcion belongs.
	 *
	 * @namespaceName The name of the namespace to which this funcion belongs.
	 */
	public abstract void setNamespaceName(String namespaceName);


	/**
	 * Gets mames of all parameters.
	 *
	 * @return The array storing all names of parameters.
	 */
	public abstract String[] getParameterNames();


	/**
	 * Gets names of data-types of all parameters.
	 * In data-type names, array declaration parts [][]...[] aren't contained.
	 *
	 * @return The array storing names of data-types of all parameters.
	 */
	public abstract String[] getParameterDataTypeNames();


	/**
	 * Gets array-ranks of all parameters.
	 * 
	 * Note that, the array-rank of an scalar is 0.
	 *
	 * @return The array storing array-ranks of all parameters.
	 */
	public abstract int[] getParameterArrayRanks();


	/**
	 * Gets flags representing whether data-types of parameters are arbitrary.
	 * 
	 * If the value of an element of the returned array is true,
	 * the data-type of the corresponding parameter is arbitrary.
	 *
	 * @return The array storing flags representing whether data-types of parameters are arbitrary.
	 */
	public abstract boolean[] getParameterDataTypeArbitrarinesses();


	/**
	 * Gets flags representing whether array-ranks of parameters are arbitrary.
	 * 
	 * If the value of an element of the returned array is true,
	 * the array-rank of the corresponding parameter is arbitrary.
	 *
	 * @return The array storing flags representing whether array-ranks of parameters are arbitrary.
	 */
	public abstract boolean[] getParameterArrayRankArbitrarinesses();


	/**
	 * Gets flags representing whether parameters are passed by references.
	 * 
	 * If the value of an element of the returned array is true,
	 * the corresponding parameter will be passed by reference
	 * when this function will be invoked.
	 *
	 * @return The array storing flags representing whether parameters are passed by references.
	 */
	public abstract boolean[] getParameterReferencenesses();


	/**
	 * Gets flags representing whether parameters are constant.
	 * 
	 * If the value of an element of the returned array is true,
	 * the corresponding parameter is constant, 
	 * so its value must not be modified in the process of this function.
	 *
	 * @return The array storing flags representing whether parameters are constant.
	 */
	public abstract boolean[] getParameterConstantnesses();


	/**
	 * Returns whether the number of parameters of this function is arbitrary.
	 *
	 * @return Return true if the number of parameters of this function is arbitrary.
	 */
	public abstract boolean isParameterCountArbitrary();


	/**
	 * (Unsupported yet) Returns whether this function has variadic parameters.
	 *
	 * @return (Unsupported yet) Returns true if this function has variadic parameters.
	 */
	public abstract boolean hasVariadicParameters();


	/**
	 * Gets the name of the data-type of the return value.
	 * In the data-type name, array declaration part [][]...[] isn't contained.
	 * 
	 * If {@link AbstractFunction#isReturnDataTypeArbitrary() isReturnDataTypeArbitrary()} method
	 * returns true, data-types and array-ranks of actual arguments will be given as 
	 * "argumentDataTypeNames" and "argumentArrayRanks".
	 * 
	 * In the contrast,
	 * if {@link AbstractFunction#isReturnDataTypeArbitrary() isReturnDataTypeArbitrary()} method
	 * returns false, the result of this method must not vary depending on
	 * "argumentDataTypeNames" and "argumentArrayRanks".
	 * On the latter case, don't access to "argumentDataTypeNames" and "argumentArrayRanks"
	 * because it is not guaranteed that valid values will be passed to them.
	 *
	 * @param argumentDataTypeNames The array storing names of data-types of all actual arguments.
	 * @param argumentArrayRanks The array storing array-ranks of all actual arguments.
	 * @return The name of the data-type of the return value.
	 */
	public abstract String getReturnDataTypeName(String[] argumentDataTypeNames, int[] argumentArrayRanks);


	/**
	 * Gets the arraya-rank of the return value.
	 * 
	 * If {@link AbstractFunction#isReturnDataTypeArbitrary() isReturnDataTypeArbitrary()} method
	 * returns true, data-types and array-ranks of actual arguments will be given as 
	 * "argumentDataTypeNames" and "argumentArrayRanks".
	 * 
	 * In the contrast,
	 * if {@link AbstractFunction#isReturnDataTypeArbitrary() isReturnDataTypeArbitrary()} method
	 * returns false, the result of this method must not vary depending on
	 * "argumentDataTypeNames" and "argumentArrayRanks".
	 * On the latter case, don't access to "argumentDataTypeNames" and "argumentArrayRanks"
	 * because it is not guaranteed that valid values will be passed to them.
	 *
	 * @param argumentDataTypeNames The array storing names of data-types of all actual arguments.
	 * @param argumentArrayRanks The array storing array-ranks of all actual arguments.
	 * @return The array-rank of the return value.
	 */
	public abstract int getReturnArrayRank(String[] argumentDataTypeNames, int[] argumentArrayRanks);


	/**
	 * Gets whether the data-type of the return value varies 
	 * depending on data-types and array-ranks of actual arguments.
	 * 
	 * @return Returns true if the data-type of the return value varies depending on actual arguments.
	 */
	public abstract boolean isReturnDataTypeArbitrary();


	/**
	 * Gets whether the array-rank of the return value varies 
	 * depending on data-types and array-ranks of actual arguments.
	 * 
	 * @return Returns true if the array-rank of the return value varies depending on actual arguments.
	 */
	public abstract boolean isReturnArrayRankArbitrary();


	/**
	 * Checks this function is invokable without problems which are detectable before invoking.
	 * 
	 * If any problem has been found, this method will throw an exception.
	 * If no problem has been found, nothing will occur.
	 *
	 * For example, types of data I/O interfaces used in an external function plug-in 
	 * must be compatible with the data-types and array-ranks of actual arguments passed in scripts.
	 * So this method throw an exception if they are incompatible.
	 *
	 * @param argumentDataTypeNames The array storing names of data-types of all actual arguments.
	 * @param argumentArrayRanks The array storing array-ranks of all actual arguments.
	 * @throws VnanoException
	 *     Thrown when problems which are detectable before invoking have been found.
	 */
	public abstract void checkInvokability(String[] argumentDataTypeNames, int[] argumentArrayRanks) throws VnanoException;


	/**
	 * Invoke this function.
	 *
	 * @param returnDataUnit The data unit to which the return value will be stored.
	 * @param argumentDataUnits The array storing data units of all actual arguments.
	 */
	public abstract void invoke(DataContainer<?> returnDataUnit, DataContainer<?>[] argumentDataUnits) throws VnanoException;

}
