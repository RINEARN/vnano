/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import org.vcssl.connect.ArrayDataAccessorInterface1;
import org.vcssl.connect.Int64ScalarDataAccessorInterface1;
import org.vcssl.connect.Float64ScalarDataAccessorInterface1;
import org.vcssl.connect.StringScalarDataAccessorInterface1;
import org.vcssl.connect.BoolScalarDataAccessorInterface1;

import org.vcssl.connect.ConnectorException;
import org.vcssl.connect.EngineConnectorInterface1;
import org.vcssl.connect.ExternalFunctionConnectorInterface1;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.vm.memory.DataContainer;


/**
 * The adapter class for converting 
 * a {@link org.vcssl.connect.ExternalFunctionConnectorInterface1 XFCI1} type external function plugin
 * to an {@link org.vcssl.nano.interconnect.AbstractFunction AbstractFunction} type function object.
 */
public final class Xfci1ToFunctionAdapter extends AbstractFunction {

	/** The XFCI1 type external function plugin to be converted. */
	private ExternalFunctionConnectorInterface1 xfciPlugin = null;

	/** The name of the function. */
	private String functionName = null;

	/** The data converters for converting data of the parameters of this function. */
	private DataConverter[] parameterDataConverters = null;

	/** The data-types of all parameters.  */
	private DataType[] parameterDataTypes = null;

	/** The array-ranks of all parameters. Note that, the array-rank of a scalar is 0. */
	private int[] parameterArrayRanks = null;

	/** The flags representing whether data of parameters will be passed as references. */
	private boolean[] parameterReferencenesses = null;

	/** The data converter for converting data of the return value. */
	private DataConverter returnDataConverter = null;

	/** The data-type of the return value. */
	private DataType returnDataType = null;

	/** The array-rank of the return value. Note that, the array-rank of a scalar is 0. */
	@SuppressWarnings("unused")
	private int returnArrayRank = -1;

	/** The name of the namespace to which this function belongs. */
	private String namespaceName = null;


	/**
	 * Create an adapter converting the specified XFCI1 plugin to 
	 * an {@link org.vcssl.nano.interconnect.AbstractFunction AbstractFunction} type function object.
	 *
	 * @param xfciPlugin The XFCI1 plugin to be converted.
	 * @throws VnanoException Thrown when incompatible data-types, array-ranks, and so on have been detected.
	 */
	public Xfci1ToFunctionAdapter(
			ExternalFunctionConnectorInterface1 xfciPlugin)
					throws VnanoException {

		this.validate(xfciPlugin);
		this.xfciPlugin = xfciPlugin;
		this.functionName = xfciPlugin.getFunctionName();

		Class<?>[] parameterClasses = this.xfciPlugin.getParameterClasses();
		Class<?> returnClass = this.xfciPlugin.getReturnClass(parameterClasses);
		int parameterLength = parameterClasses.length;

		this.returnDataConverter = new DataConverter(this.xfciPlugin.getReturnClass(parameterClasses));
		this.returnDataType = this.returnDataConverter.getDataType();
		this.returnArrayRank = this.returnDataConverter.getRank();

		this.parameterDataConverters = new DataConverter[parameterLength];
		this.parameterDataTypes = new DataType[parameterLength];
		this.parameterArrayRanks = new int[parameterLength];
		this.parameterReferencenesses = xfciPlugin.getParameterReferencenesses();

		for (int parameterIndex=0; parameterIndex<parameterLength; parameterIndex++) {

			this.parameterDataConverters[parameterIndex] = new DataConverter(
					parameterClasses[parameterIndex]
			);

			this.parameterDataTypes[parameterIndex]
					= this.parameterDataConverters[parameterIndex].getDataType();

			this.parameterArrayRanks[parameterIndex]
					= this.parameterDataConverters[parameterIndex].getRank();
		}


		for (Class<?> parameterClass: parameterClasses) {
			if (DataConverter.getDataTypeOf(parameterClass)==DataType.ANY && xfciPlugin.isDataConversionNecessary()) {
				throw new VnanoException(
					ErrorType.DATA_CONVERSION_OF_FUNCTION_PLUGIN_USING_OBJECT_TYPE_SHOULD_BE_DISABLED,
					new String[] { xfciPlugin.getFunctionName() }
				);
			}
		}
		if (DataConverter.getDataTypeOf(returnClass)==DataType.ANY && xfciPlugin.isDataConversionNecessary()) {
			throw new VnanoException(
				ErrorType.DATA_CONVERSION_OF_FUNCTION_PLUGIN_USING_OBJECT_TYPE_SHOULD_BE_DISABLED,
				new String[] { xfciPlugin.getFunctionName() }
			);
		}
	}


	/**
	 * Returns the XFCI1 plugin to be converted by this adapter.
	 *
	 * @return The XFCI1 plugin to be converted by this adapter.
	 */
	public ExternalFunctionConnectorInterface1 getXfci1Plugin() {
		return this.xfciPlugin;
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
	 * 
	 * This method is used for setting an alias for an external function.
	 * 
	 * @param functionName The name of this function.
	 */
	@Override
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}


	/**
	 * Returns whether this function belongs to any namespace.
	 *
	 * @return Returns true if this function belongs to a namespace.
	 */
	@Override
	public final boolean hasNamespaceName() {
		return this.namespaceName != null;
	}


	/**
	 * Gets the name of the namespace to which this function belongs.
	 *
	 * @return The name of the namespace to which this function belongs.
	 */
	@Override
	public final String getNamespaceName() {
		return this.namespaceName;
	}


	/**
	 * Sets the name of the namespace to which this funcion belongs.
	 *
	 * @namespaceName The name of the namespace to which this funcion belongs.
	 */
	@Override
	public void setNamespaceName(String namespaceName) {
		this.namespaceName = namespaceName;
	}


	/**
	 * Gets mames of all parameters.
	 *
	 * @return The array storing all names of parameters.
	 */
	@Override
	public final String[] getParameterNames() {
		return this.xfciPlugin.getParameterNames();
	}


	/**
	 * Gets names of data-types of all parameters.
	 * In data-type names, array declaration parts [][]...[] aren't contained.
	 *
	 * @return The array storing names of data-types of all parameters.
	 */
	@Override
	public final String[] getParameterDataTypeNames() {
		int parameterLength = this.parameterDataTypes.length;

		String[] parameterDataTypeNames = new String[parameterLength];
		for (int parameterIndex=0; parameterIndex<parameterLength; parameterIndex++) {
			parameterDataTypeNames[parameterIndex] = DataTypeName.getDataTypeNameOf(
					this.parameterDataConverters[parameterIndex].getDataType()
			);
		}

		return parameterDataTypeNames;
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
		return this.parameterArrayRanks;
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
		return this.xfciPlugin.getParameterDataTypeArbitrarinesses();
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
		return this.xfciPlugin.getParameterArrayRankArbitrarinesses();
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
		return this.xfciPlugin.getParameterReferencenesses();
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
		return this.xfciPlugin.getParameterConstantnesses();
	}


	/**
	 * Returns whether the number of parameters of this function is arbitrary.
	 *
	 * @return Return true if the number of parameters of this function is arbitrary.
	 */
	@Override
	public final boolean isParameterCountArbitrary() {
		return this.xfciPlugin.isParameterCountArbitrary();
	}


	/**
	 * (Unsupported yet) Returns whether this function has variadic parameters.
	 *
	 * @return (Unsupported yet) Returns true if this function has variadic parameters.
	 */
	@Override
	public final boolean hasVariadicParameters() {
		return this.xfciPlugin.hasVariadicParameters();
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
		DataType[] argumentDataTypes;
		try {
			argumentDataTypes = DataTypeName.getDataTypesOf(argumentDataTypeNames);
		} catch (VnanoException e) {
			throw new VnanoFatalException(e);
		}
		Class<?>[] argumentClasses = DataConverter.getExternalClassesOf(argumentDataTypes, argumentArrayRanks);
		Class<?> returnValueClass = this.xfciPlugin.getReturnClass(argumentClasses);
		DataType returnDataType = DataConverter.getDataTypeOf(returnValueClass);
		return DataTypeName.getDataTypeNameOf(returnDataType);
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
		DataType[] argumentDataTypes;
		try {
			argumentDataTypes = DataTypeName.getDataTypesOf(argumentDataTypeNames);
		} catch (VnanoException e) {
			throw new VnanoFatalException(e);
		}
		Class<?>[] argumentClasses = DataConverter.getExternalClassesOf(argumentDataTypes, argumentArrayRanks);
		Class<?> returnValueClass = this.xfciPlugin.getReturnClass(argumentClasses);
		return DataConverter.getRankOf(returnValueClass);
	}


	/**
	 * Gets whether the data-type of the return value varies 
	 * depending on data-types and array-ranks of actual arguments.
	 * 
	 * @return Returns true if the data-type of the return value varies depending on actual arguments.
	 */
	@Override
	public final boolean isReturnDataTypeArbitrary() {
		return this.xfciPlugin.isReturnDataTypeArbitrary();
	}


	/**
	 * Gets whether the array-rank of the return value varies 
	 * depending on data-types and array-ranks of actual arguments.
	 * 
	 * @return Returns true if the array-rank of the return value varies depending on actual arguments.
	 */
	@Override
	public final boolean isReturnArrayRankArbitrary() {
		return this.xfciPlugin.isReturnArrayRankArbitrary();
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
	public void checkInvokability(String[] argumentDataTypeNames, int[] argumentArrayRanks)
			throws VnanoException {

		// If the automatic-data-conversion feature is disabled, 
		// check compatibility between data-types and data I/O interfaces, of parameters.
		if (!this.xfciPlugin.isDataConversionNecessary()) {
			Class<?>[] paramDataAccessorInterfaces = this.xfciPlugin.getParameterUnconvertedClasses();
			for (Class<?> paramDataAccessorInterface: paramDataAccessorInterfaces) {

				// Check compatibility.
				if (!paramDataAccessorInterface.isAssignableFrom(DataContainer.class)) {
					String errorWords[] = new String[] {
						paramDataAccessorInterface.getCanonicalName(), this.xfciPlugin.getClass().getCanonicalName()
					};
					throw new VnanoException(ErrorType.INCOMPATIBLE_DATA_ACCESSOR_INTERFACE, errorWords);
				}
			}
		}

		// If the automatic-data-conversion feature is disabled, 
		// check compatibility between the data-type and the data I/O interface, of the return value.
		if (!this.xfciPlugin.isDataConversionNecessary()) {

			// The data-type/array-rank of the return value may depend on the parameters, 
			// so firstly get the classes of the parameters.
			DataType[] argumentDataTypes;
			try {
				argumentDataTypes = DataTypeName.getDataTypesOf(argumentDataTypeNames);
			} catch (VnanoException e) {
				throw new VnanoFatalException(e);
			}
			Class<?>[] argumentClasses = DataConverter.getExternalClassesOf(argumentDataTypes, argumentArrayRanks);

			// Get the data I/O interface of the return value.
			Class<?> returnDataAccessorInterface = this.xfciPlugin.getReturnUnconvertedClass(argumentClasses);

			boolean isVoid = this.xfciPlugin.getReturnClass(argumentClasses).equals(void.class)
					|| this.xfciPlugin.getReturnClass(argumentClasses).equals(Void.class);

			// Check compatibility.
			if (!isVoid && !returnDataAccessorInterface.isAssignableFrom(DataContainer.class)) {
				String errorWords[] = new String[] {
					returnDataAccessorInterface.getCanonicalName(), this.xfciPlugin.getClass().getCanonicalName()
				};
				throw new VnanoException(ErrorType.INCOMPATIBLE_DATA_ACCESSOR_INTERFACE, errorWords);
			}
		}
	}


	/**
	 * Invoke this function.
	 *
	 * @param argumentDataUnits The array storing data units of all actual arguments.
	 * @param returnDataUnit The data unit to which the return value will be stored.
	 */
	@Override
	public final void invoke(DataContainer<?>[] argumentDataContainers, DataContainer<?> returnDataContainer)
			throws VnanoException {

		int argLength = argumentDataContainers.length;
		Object[] convertedArgs = new Object[argLength];

		// If the automatic-data-conversion feature is enabled:
		if (this.xfciPlugin.isDataConversionNecessary()) {

			// Convert data-types of parameters.
			for (int argIndex=0; argIndex<argLength; argIndex++) {
				boolean isVoid = false;
				DataConverter converter = null;

				// If the count of parameters is arbitrary:
				if (this.xfciPlugin.isParameterCountArbitrary()) {
					converter = this.parameterDataConverters[0];
					isVoid = this.parameterDataTypes[0].equals(DataType.VOID);

				// If the count of parameters is not arbitrary:
				} else {
					converter = this.parameterDataConverters[argIndex];
					isVoid = this.parameterDataTypes[argIndex].equals(DataType.VOID);
				}

				if (!isVoid) {
					try {
						convertedArgs[argIndex] = converter.convertToExternalObject(argumentDataContainers[argIndex]);
					} catch (VnanoException e) {
						throw new VnanoFatalException(e);
					}
				}
			}

			// Invoke the external function provided by the XFCI1 plugin.
			Object returnObject = null;
			try {
				returnObject = this.xfciPlugin.invoke(convertedArgs);

			// If any exception has occurred, re-throw it as a VnanoException,
			} catch (Exception e) { // Don't modify "Exception" to "Throwable". The latter is too wide for catching here.

				// Prepare information to be embedded in the error message.
				String[] errorWords = { this.xfciPlugin.getFunctionName(), null };
				if (e instanceof ConnectorException) {
					errorWords[1] = e.getMessage();
				}
				throw new VnanoException(ErrorType.EXTERNAL_FUNCTION_PLUGIN_CRASHED, errorWords, e);
			}

			// Convert data of the return value.
			if (!this.returnDataType.equals(DataType.VOID)) {
				try {
					this.returnDataConverter.convertToDataContainer(returnObject, returnDataContainer);
				} catch (VnanoException e) {
					throw new VnanoFatalException(e);
				}
			}

		// If the automatic-data-conversion feature is disabled:
		} else {

			// The return value will be stored to the data container of the first parameter, 
			// when the automatic-data-conversion feature is disabled.
			DataContainer<?>[] xfciArgContainers = new DataContainer<?>[argLength + 1];
			xfciArgContainers[0] = returnDataContainer;

			// Stores the values of the actual arguments to the data containers of the parameters.
			for (int argIndex=0; argIndex<argLength; argIndex++) {

				// The flag representing whether the data of the argument should be passed by reference.
				boolean isParamRef = this.xfciPlugin.isParameterCountArbitrary()
					? this.parameterReferencenesses[0]
					: this.parameterReferencenesses[argIndex];

				// Pass by reference: copy the reference of the data container.
				if (isParamRef) {
					xfciArgContainers[argIndex + 1] = argumentDataContainers[argIndex];

				// Pass by value: copy the content of the data container.
				} else {
					xfciArgContainers[argIndex + 1] = DataConverter.copyDataContainer(argumentDataContainers[argIndex]);
				}
			}

			// Invoke the external function provided by the XFCI1 plugin.
			try {
				this.xfciPlugin.invoke(xfciArgContainers);

				// Note: the return value will be stored to xfciArgContainers[0],
				// when the automatic-data-conversion feature is disabled.

			// If any exception has occurred, re-throw it as a VnanoException,
			} catch (Exception e) { // Don't modify "Exception" to "Throwable". The latter is too wide for catching here.

				// Prepare information to be embedded in the error message.
				String[] errorWords = { this.xfciPlugin.getFunctionName(), null };
				if (e instanceof ConnectorException) {
					errorWords[1] = e.getMessage();
				}

				throw new VnanoException(ErrorType.EXTERNAL_FUNCTION_PLUGIN_CRASHED, errorWords, e);
			}
		}
	}


	/**
	 * Validates whether the specified plug-in implements XFCI1 correctly, and it is available on the current version of Vnano Engine.
	 * If no issues are detected for the plug-in, nothing will occur.
	 * 
	 * @param plugin The plug-in to be validated.
	 * @throws VnanoException Thrown if the specified plug-in has an incorrect something.
	 */
	private void validate(ExternalFunctionConnectorInterface1 plugin) throws VnanoException {
		
		// getFunctionName()
		if (plugin.getFunctionName() == null) {
			String errorMessage = "getFunctionName(): The returned value is null.";
			throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });
		}
		
		// getParameterNames()
		if (plugin.hasParameterNames()) {
			if (plugin.getParameterNames() == null) {
				String errorMessage = "getParameterNames(): The returned array is null.";
				throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });			
			}
			for (String element: plugin.getParameterNames()) {
				if (element == null) {
					String errorMessage = "getParameterNames(): The returned array contains a null element.";
					throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });							
				}
			}
		}

		// getParameterClasses()
		if (plugin.getParameterClasses() == null) {
			String errorMessage = "getParameterNames(): The returned array is null.";
			throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });
		}
		for (Class<?> element: plugin.getParameterClasses()) {
			if (element == null) {
				String errorMessage = "getParameterNames(): The returned array contains a null element.";
				throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });							
			}
		}
		int paramCount = plugin.getParameterClasses().length;

		// 	getParameterUnconvertedClasses()
		if (!plugin.isDataConversionNecessary()) {
			if (plugin.getParameterUnconvertedClasses() == null) {
				String errorMessage = "getParameterUnconvertedClasses(): The returned array is null.";
				throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });
			}
			for (Class<?> element: plugin.getParameterUnconvertedClasses()) {
				if (element == null) {
					String errorMessage = "getParameterUnconvertedClasses(): The returned array contains a null element.";
					throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });							
				}
				if (!element.equals(ArrayDataAccessorInterface1.class)
						&& !element.equals(Int64ScalarDataAccessorInterface1.class)
						&& !element.equals(Float64ScalarDataAccessorInterface1.class)
						&& !element.equals(BoolScalarDataAccessorInterface1.class)
						&& !element.equals(StringScalarDataAccessorInterface1.class)
						&& !element.equals(DataContainer.class) ) {

					String errorMessage = "getParameterUnconvertedClasses(): The returned class/interface \""
							+ element.getName() + "\"is not supported on the current version of Vnano Engine.";
					throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });
				}
			}
		}
		
		// getParameterDataTypeArbitrarinesses()
		if (plugin.getParameterDataTypeArbitrarinesses() == null) {
			String errorMessage = "getParameterDataTypeArbitrarinesses(): The returned array is null.";
			throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });
		}
		if (plugin.getParameterDataTypeArbitrarinesses().length != paramCount) {
			String errorMessage = "getParameterDataTypeArbitrarinesses(): The number of elements of the returned array is "
					+ plugin.getParameterDataTypeArbitrarinesses().length + ", but must be " + paramCount
					+ ", as same as the returned value of getParameterClasses() method.";
			throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });
		}

		// getParameterArrayRankArbitrarinesses()
		if (plugin.getParameterArrayRankArbitrarinesses() == null) {
			String errorMessage = "getParameterArrayRankArbitrarinesses(): The returned array is null.";
			throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });
		}
		if (plugin.getParameterArrayRankArbitrarinesses().length != paramCount) {
			String errorMessage = "getParameterArrayRankArbitrarinesses(): The number of elements of the returned array is "
					+ plugin.getParameterArrayRankArbitrarinesses().length + ", but must be " + paramCount
					+ ", as same as the returned value of getParameterClasses() method.";
			throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });
		}

		// getParameterReferencenesses()
		if (plugin.getParameterReferencenesses() == null) {
			String errorMessage = "getParameterReferencenesses(): The returned array is null.";
			throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });
		}
		if (plugin.getParameterReferencenesses().length != paramCount) {
			String errorMessage = "getParameterReferencenesses(): The number of elements of the returned array is "
					+ plugin.getParameterReferencenesses().length + ", but must be " + paramCount
					+ ", as same as the returned value of getParameterClasses() method.";
			throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });
		}

		// getParameterConstantnesses()
		if (plugin.getParameterConstantnesses() == null) {
			String errorMessage = "getParameterConstantnesses(): The returned array is null.";
			throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });
		}
		if (plugin.getParameterConstantnesses().length != paramCount) {
			String errorMessage = "getParameterConstantnesses(): The number of elements of the returned array is "
					+ plugin.getParameterConstantnesses().length + ", but must be " + paramCount
					+ ", as same as the returned value of getParameterClasses() method.";
			throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });
		}

		// hasVariadicParameters()
		if (plugin.hasVariadicParameters()) {
			String errorMessage = "hasVariadicParameters(): Returned true, but this feature has not been supported yet on the current version of Vnano Engine.";
			throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });
		}
		
		// getReturnClass() and getReturnUnconvertedClass()
		if (plugin.isReturnDataTypeArbitrary() || plugin.isReturnArrayRankArbitrary()) {

			// In this case, the type/rank of the returned value depends on the actual argument passed from scripts, 
			// so we can not statically validate it.
		} else {
			Class<?>[] paramClasses = plugin.getParameterClasses();
			
			if (plugin.getReturnClass(paramClasses).equals(void.class) || plugin.getReturnClass(paramClasses).equals(Void.class)) {
				// If the data-type of the return value is "void", getParameterClasses() method will not be called.
				
			} else if (plugin.getReturnClass(paramClasses) == null) {
				String errorMessage = "getReturnClass(...): The returned value is null.";
				throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });
				
			} else if (!plugin.isDataConversionNecessary()) {
				if (plugin.getReturnUnconvertedClass(paramClasses) == null) {
					String errorMessage = "getReturnUnconvertedClass(): The returned value is null.";
					throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });	
				}
				Class<?> returnClass = plugin.getReturnUnconvertedClass(paramClasses);
				if (!returnClass.equals(ArrayDataAccessorInterface1.class)
						&& !returnClass.equals(Int64ScalarDataAccessorInterface1.class)
						&& !returnClass.equals(Float64ScalarDataAccessorInterface1.class)
						&& !returnClass.equals(BoolScalarDataAccessorInterface1.class)
						&& !returnClass.equals(StringScalarDataAccessorInterface1.class)
						&& !returnClass.equals(DataContainer.class) ) {

					String errorMessage = "getParameterUnconvertedClasses(): The returned class/interface \""
							+ returnClass.getName() + "\"is not supported on the current version of Vnano Engine.";
					throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });	
				}
			}
		}
		
		// getEngineConnectorClass()
		if (plugin.getEngineConnectorClass() == null) {
			String errorMessage = "getEngineConnectorClass(...): The returned value is null.";
			throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });
		}
		if (!plugin.getEngineConnectorClass().equals(EngineConnectorInterface1.class)) {
			String errorMessage = "getEngineConnectorClass(...): The specified engine connector \""
					+ plugin.getEngineConnectorClass().getName() + "\"is not suppoted on the current version of Vnano Engine.";
			throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });
		}
	}
}
