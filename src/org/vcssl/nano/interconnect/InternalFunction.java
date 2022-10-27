/*
 * Copyright(C) 2019-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import java.util.Arrays;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

/**
 * The class of an internal function.
 */
public final class InternalFunction extends AbstractFunction {

	/** The name of this function. */
	private String functionName = null;

	/** Names of parameters of this function. */
	private String[] parameterNames = null;

	/** Data-types of parameters of this function. */
	private String[] parameterDataTypeNames = null;

	/** Array-ranks of parameters of this function. */
	private int[] parameterRanks = null;

	/** Flags representing whether data-types of parameters vary arbitrarily corresponding with actual arguments. */
	private boolean[] parameterDataTypeArbitrarinesses = null;

	/** Flags representing whether array-ranks of parameters vary arbitrarily corresponding with actual arguments. */
	private boolean[] parameterRankArbitrarinesses = null;

	/** Flags representing whether data of parameters will be passed by references. */
	private boolean[] parameterReferencenesses = null;

	/** Flags representing whether parameters are constants (unmodifiable). */
	private boolean[] parameterConstantnesses = null;

	/** The name of the data-type of the return value of this function. */
	private String returnDataTypeName = null;

	/** The array-rank of the return value of this function. */
	private int returnRank = -1;


	/**
	 * Creates a new internal function having specified information.
	 * 
	 * @param functionName The name of the function.
	 * @param parameterNames Names of parameters.
	 * @param parameterDataTypeNames Data-types of parameters.
	 * @param parameterRanks Array-ranks of parameters.
	 * @param parameterReferencenesses Frags representing whether data of parameters will be passed by references.
	 * @param parameterConstantnesses Flags representing whether parameters are constants (unmodifiable).
	 * @param returnDataTypeName The name of the data-type of the return value.
	 * @param returnRank The array-rank of the return value.
	 */
	public InternalFunction (String functionName,
			String[] parameterNames, String[] parameterDataTypeNames, int[] parameterRanks,
			boolean[] parameterReferencenesses, boolean[] parameterConstantnesses,
			String returnDataTypeName, int returnRank) {

		this.functionName = functionName;
		this.parameterNames = parameterNames;
		this.returnDataTypeName = returnDataTypeName;
		this.returnRank = returnRank;
		this.parameterDataTypeNames = parameterDataTypeNames;
		this.parameterRanks = parameterRanks;

		int numParameters = parameterDataTypeNames.length;
		this.parameterDataTypeArbitrarinesses = new boolean[numParameters];
		this.parameterRankArbitrarinesses = new boolean[numParameters];
		this.parameterReferencenesses = parameterReferencenesses;
		this.parameterConstantnesses = parameterConstantnesses;
		Arrays.fill(parameterDataTypeArbitrarinesses, false);
		Arrays.fill(parameterRankArbitrarinesses, false);
	}


	/**
	 * Gets the name of this function.
	 *
	 * @return The name of this function.
	 */
	@Override
	public final String getFunctionName() {
		return this.functionName;
	}


	/**
	 * Sets the name of this function.
	 * However, this method isn't available, on the current version.
	 * 
	 * This method is used for setting an alias for external functions.
	 * On the other hand, on the current language specification of Vnano, 
	 * an internal functions can not have multiple names (aliases),
	 * so VnanoFatalException will be thrown when this method is invoked.
	 *
	 * @param functionName The name of this function.
	 */
	@Override
	public void setFunctionName(String functionName) {
		throw new VnanoFatalException("Names of internal functions should not be changed.");
	}


	/**
	 * Returns whether this function belongs to any namespace.
	 *
	 * @return Returns true if this function belongs to a namespace.
	 */
	@Override
	public final boolean hasNamespaceName() {
		return false;
	}


	/**
	 * Gets the name of the namespace to which this function belongs.
	 *
	 * @return The name of the namespace to which this function belongs.
	 */
	@Override
	public final String getNamespaceName() {
		throw new VnanoFatalException("Internal functions can not belongs to any namespaces.");
	}


	/**
	 * Sets the name of the namespace to which this funcion belongs.
	 * However, this method isn't available, on the current version.
	 *
	 * @namespaceName The name of the namespace to which this funcion belongs.
	 */
	@Override
	public final void setNamespaceName(String namespaceName) {
		throw new VnanoFatalException("Internal functions can not belongs to any namespaces.");
	}


	/**
	 * Gets mames of all parameters.
	 *
	 * @return The array storing all names of parameters.
	 */
	@Override
	public final String[] getParameterNames() {
		return this.parameterNames;
	}


	/**
	 * Gets names of data-types of all parameters.
	 * In data-type names, array declaration parts [][]...[] aren't contained.
	 *
	 * @return The array storing names of data-types of all parameters.
	 */
	@Override
	public final String[] getParameterDataTypeNames() {
		return this.parameterDataTypeNames;
	}


	/**
	 * Gets array-ranks of all parameters.
	 * 
	 * Note that, the array-rank of an scalar is 0.
	 *
	 * @return The array storing array-ranks of all parameters.
	 */
	@Override
	public final int[] getParameterArrayRanks() {
		return this.parameterRanks;
	}


	/**
	 * Gets flags representing whether data-types of parameters are arbitrary.
	 * 
	 * If the value of an element of the returned array is true,
	 * the data-type of the corresponding parameter is arbitrary.
	 *
	 * @return The array storing flags representing whether data-types of parameters are arbitrary.
	 */
	@Override
	public final boolean[] getParameterDataTypeArbitrarinesses() {
		return this.parameterDataTypeArbitrarinesses;
	}


	/**
	 * Gets flags representing whether array-ranks of parameters are arbitrary.
	 * 
	 * If the value of an element of the returned array is true,
	 * the array-rank of the corresponding parameter is arbitrary.
	 *
	 * @return The array storing flags representing whether array-ranks of parameters are arbitrary.
	 */
	@Override
	public final boolean[] getParameterArrayRankArbitrarinesses() {
		return this.parameterRankArbitrarinesses;
	}


	/**
	 * Gets flags representing whether parameters are passed by references.
	 * 
	 * If the value of an element of the returned array is true,
	 * the corresponding parameter will be passed by reference
	 * when this function will be invoked.
	 *
	 * @return The array storing flags representing whether parameters are passed by references.
	 */
	@Override
	public boolean[] getParameterReferencenesses() {
		return this.parameterReferencenesses;
	}


	/**
	 * Sets flags representing whether parameters are passed by references.
	 *
	 * @param parameterReferencenesses The array storing flags representing whether parameters are passed by references.
	 */
	public void setParameterReferencenesses(boolean[] parameterReferencenesses) {
		this.parameterReferencenesses = parameterReferencenesses;
	}


	/**
	 * Gets flags representing whether parameters are constant.
	 * 
	 * If the value of an element of the returned array is true,
	 * the corresponding parameter is constant, 
	 * so its value must not be modified in the process of this function.
	 *
	 * @return The array storing flags representing whether parameters are constant.
	 */
	@Override
	public boolean[] getParameterConstantnesses() {
		return this.parameterConstantnesses;
	}


	/**
	 * Returns whether the number of parameters of this function is arbitrary.
	 *
	 * @return Return true if the number of parameters of this function is arbitrary.
	 */
	@Override
	public final boolean isParameterCountArbitrary() {
		return false;
	}


	/**
	 * (Unsupported yet) Returns whether this function has variadic parameters.
	 *
	 * @return (Unsupported yet) Returns true if this function has variadic parameters.
	 */
	@Override
	public final boolean hasVariadicParameters() {
		// Unsupported on the current version.
		return false;
	}


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
	@Override
	public final String getReturnDataTypeName(String[] argumentDataTypeNames, int[] argumentArrayRanks) {
		return this.returnDataTypeName;
	}


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
	@Override
	public final int getReturnArrayRank(String[] argumentDataTypeNames, int[] argumentArrayRanks) {
		return this.returnRank;
	}


	/**
	 * Gets whether the data-type of the return value varies 
	 * depending on data-types and array-ranks of actual arguments.
	 * 
	 * @return Returns true if the data-type of the return value varies depending on actual arguments.
	 */
	@Override
	public final boolean isReturnDataTypeArbitrary() {
		return false;
	}


	/**
	 * Gets whether the array-rank of the return value varies 
	 * depending on data-types and array-ranks of actual arguments.
	 * 
	 * @return Returns true if the array-rank of the return value varies depending on actual arguments.
	 */
	@Override
	public final boolean isReturnArrayRankArbitrary() {
		return false;
	}


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
	@Override
	public final void checkInvokability(String[] argumentDataTypeNames, int[] argumentArrayRanks) {
		// The above problems don't occur for internal functions.
	}


	/**
	 * Invoke this function.
	 * 
	 * However, this method is not available for internal functions.
	 * Processes of internal functions will be compiled to intermidiate code, 
	 * and invoked by CALL instruction on VirtualMachine.
	 *
	 * @param returnDataUnit The data unit to which the return value will be stored.
	 * @param argumentDataUnits The array storing data units of all actual arguments.
	 */
	@Override
	public final void invoke(DataContainer<?> returnDataUnit, DataContainer<?>[] argumentDataUnits) {
		throw new VnanoFatalException("The invocation of the internal function from the outside has not implemented yet.");
	}
}
