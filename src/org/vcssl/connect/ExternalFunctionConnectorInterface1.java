/*
 * ==================================================
 * External Function Connector Interface 1 (XFCI 1)
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2017-2022 by RINEARN
 * ==================================================
 */

// THE SPECIFICATION OF THIS INTERFACE HAD BEEN FINALIZED AT 2022/08/31.
// NO MODIFICATIONS WILL BE APPLIED FOR THIS INTERFACE, EXCLUDING DOCUMENTS/COMMENTS.

package org.vcssl.connect;


/**
 * An interface (abbreviated as XFCI1) for implementing external function plug-ins 
 * which provide functions available in scripts
 * 
 * Currently, this interface is supported on the Vnano Engine, 
 * and has not been supported on the VCSSL Engine yet.
 */
public interface ExternalFunctionConnectorInterface1 {

	/** The type ID of this interface (value: "XFCI") referred when the plug-in will be loaded. */
	public static final String INTERFACE_TYPE_ID = "XFCI";

	/** The generation of this interface (value: "1"). */
	public static final String INTERFACE_GENERATION = "1";


	/**
	 * Gets the name of the function.
	 * 
	 * @return The name of the function.
	 */
	public abstract String getFunctionName();


	/**
	 * Returns whether parameter names can be gotten.
	 * 
	 * @return Returns true if parameter names can be gotten.
	 */
	public abstract boolean hasParameterNames();


	/**
	 * Returns names of all parameters.
	 * 
	 * @return The array storing names of all parameters.
	 */
	public abstract String[] getParameterNames();


	/**
	 * Returns the instance of "Class" class representing data-types and array-ranks of all parameters.
	 * 
	 * For example, returns { double.class, long[][].class } when parameters of this function are: (double, long[][]).
	 * (Note that, "float" type values in scripts are handled as "double" type values in plug-ins, 
	 * and "int" type values in scripts are handled as "long" type values in plug-ins.)
	 * 
	 * @return The "Class" type array, each element represents the data-type and array-rank of each parameter.
	 */
	public abstract Class<?>[] getParameterClasses();


	/**
	 * Returns the instance of "Class" class representing data-I/O interfaces for accessing to data of parameters, 
	 * when {@link isDataConversionNecessary() data-conversion feature} is disabled.
	 * 
	 * As interfaces for accessing to a scalar parameter, 
	 * {@link Int64ScalarDataAccessorInterface1 Int64 SDAI} (for long-type param),
	 * {@link Float64ScalarDataAccessorInterface1 Float64 SDAI} (for double-type param),
	 * {@link BoolScalarDataAccessorInterface1 Bool SDAI} (for boolean-type param),
	 * {@link StringScalarDataAccessorInterface1 String SDAI} (for String-type param),
	 * and
	 * {@link ArrayDataAccessorInterface1 ADAI} (generic) 
	 * are available.
	 * For an array parameter, only 
	 * {@link ArrayDataAccessorInterface1 ADAI}
	 * is available.
	 * 
	 * @return The "Class" type array, each element represents the I/O interface for accessing to the data of each parameter.
	 */
	public abstract Class<?>[] getParameterUnconvertedClasses();


	/**
	 * Returns arbitrarinesses of data-types of parameters.
	 * 
	 * When the data-type arbitrariness is "true" for a parameter, 
	 * any type of value can be passed as an actual argument for the parameter.
	 *
	 * @return The array storing data-type arbitrarinesses of all parameters.
	 *         (Each element represents the data-type arbitrariness of each paremeter.)
	 */
	public boolean[] getParameterDataTypeArbitrarinesses();


	/**
	 * Returns arbitrarinesses of array-ranks of parameters.
	 * 
	 * When the array-rank arbitrariness is "true" for a parameter, 
	 * an array having any rank (number of dimensions) can be passed as an actual argument for the parameter.
	 *
	 * @return The array storing array-rank arbitrarinesses of all parameters.
	 *         (Each element represents the array-rank arbitrariness of each paremeter.)
	 */
	public boolean[] getParameterArrayRankArbitrarinesses();


	/**
	 * Returns referencenesses of parameters.
	 * 
	 * When the referenceness is "true" for a parameter, 
	 * an actual argument of the parameter will be passed by reference.
	 *
	 * @return The array storing referencenesses of all parameters.
	 *         (Each element represents the referenceness of each paremeter.)
	 */
	public abstract boolean[] getParameterReferencenesses();


	/**
	 * Returns constantnesses of parameters.
	 * 
	 * When the constantness is "true" for a parameter, 
	 * it is regarded that the value of an actual argument of the parameter will not be modified 
	 * in the process of this function.
	 *
	 * @return The array storing constantnesses of all parameters.
	 *         (Each element represents the constantness of each paremeter.)
	 */
	public abstract boolean[] getParameterConstantnesses();


	/**
	 * Returns whether the number of parameters is arbitrary.
	 *
	 * When this feature is enabled (when this method returns "true"), 
	 * by default, it is regarded that all parameters have the same data-type and array-rank.
	 * Hence, {@link getParameterClasses()} method should return an array of which length is 1, 
	 * and it represents the data-type/array-rank of all parameters.
	 * However, if {@link getParameterDataTypeArbitrarinesses()} method or 
	 * {@link getParameterArrayRankArbitrarinesses()} method returns { true }, 
	 * data-types / array-ranks of parameters are arbitrary, so they may different each other.
	 *
	 * @return Returns true if the number of parameters is arbitrary.
	 */
	public boolean isParameterCountArbitrary();


	/**
	 * (Unsupported feature on the current version of VCSSL/Vnano Engine)
	 * 
	 * @return (Unsupported feature on the current version of VCSSL/Vnano Engine)
	 */
	public abstract boolean hasVariadicParameters();


	/**
	 * Returns the instance of "Class" class, representing a data-type and an array-rank of the return value of this function.
	 * 
	 * For example, returns double.class for double-type return value, 
	 * returns long[][].class for long[][]-type return value, and so on.
	 * (Note that, "float" type values in scripts are handled as "double" type values in plug-ins, 
	 * and "int" type values in scripts are handled as "long" type values in plug-ins.)
	 * 
	 * @param parameterClasses The "Class" type array, each element represents the data-type and array-rank of each actual argument.
	 * @return The "Class" type value representing the data-type and the array-rank of the return value of this function.
	 */
	public abstract Class<?> getReturnClass(Class<?>[] parameterClasses);


	/**
	 * Returns Class-instances representing data-I/O interfaces for accessing to data 
	 * of return value of this function, when {@link isDataConversionNecessary() data-conversion feature} is disabled.
	 * 
	 * As interfaces for accessing to a scalar return value, 
	 * {@link Int64ScalarDataAccessorInterface1 Int64 SDAI} (for long-type value),
	 * {@link Float64ScalarDataAccessorInterface1 Float64 SDAI} (for double-type value),
	 * {@link BoolScalarDataAccessorInterface1 Bool SDAI} (for boolean-type value),
	 * {@link StringScalarDataAccessorInterface1 String SDAI} (for String-type value),
	 * and
	 * {@link ArrayDataAccessorInterface1 ADAI} (generic) 
	 * are available.
	 * For an array return value, only 
	 * {@link ArrayDataAccessorInterface1 ADAI}
	 * is available.
	 * 
	 * Also, information of data-types and array-ranks of actual arguments will be passed to "parameterClasses".
	 * The above information enable to implement a kind of "generic" function 
	 * of which the data-type/array-rank varies depending on data-types/array-ranks of parameters.
	 * (To implement such functions, set the data-type/array-rank of the return value arbitrary,
	 *  by implementing {@link isReturnDataTypeArbitrary()} and {@link isReturnArrayRankArbitrary()} 
	 *  methods to return "true".)
	 * 
	 * @param parameterClasses The "Class" type array, each element represents the data-type/array-rank of each actual argument.
	 * @return The "Class" type value representing the I/O interface for accessing to the data of return value.
	 */
	public abstract Class<?> getReturnUnconvertedClass(Class<?>[] parameterClasses);


	/**
	 * Returns whether the data-type of the return value of this function varies arbitrary.
	 * 
	 * This feature is for implementing a kind of "generic" function of which type of return value 
	 * varies depending on types of actual arguments.
	 * Hence, even if this feature is enabled (even if this method returns "true"), 
	 * the type of the return value must be determined at when types of actual arguments are determined.
	 * See also: exprenation of {@link getReturnClass(Class[])} method.
	 * 
	 * @return Returns true if the data-type of the return value varies arbitrary.
	 */
	public abstract boolean isReturnDataTypeArbitrary();


	/**
	 * Returns whether the array-rank (number of dimensions) of the return value of this function varies arbitrary.
	 * 
	 * This feature is for implementing a kind of "generic" function of which array-rank of return value 
	 * varies depending on types/ranks of actual arguments.
	 * Hence, even if this feature is enabled (even if this method returns "true"), 
	 * the rank of the return value must be determined at when types/ranks of actual arguments are determined.
	 * See also: exprenation of {@link getReturnClass(Class[])} method.
	 * 
	 * @return Returns true if the array-rank of the return value varies arbitrary.
	 */
	public abstract boolean isReturnArrayRankArbitrary();


	/**
	 * Returns whether the data-conversions for arguments and return value are necessary.
	 * 
	 * When this feature is enabled (when this method returns "true"),
	 * you can receive data of arguments by using simple data-types.
	 * For example, you can receive Double instance for double-type parameter, 
	 * long[][] instance for long[][]-type parameter, and so on.
	 * Also, as same as the arguments, 
	 * you can return the simple data-type value as the return value of the function.
	 *
	 * On the other hand, when this feature is disabled (when this method retunrs "false"),
	 * it is necessary to access to data of parameters and the return value through data-I/O interfaces, 
	 * which are specified as return values of {@link getParameterUnconvertedClasses()} 
	 * and {@link getReturnUnconvertedClass(Class<?>[])} method. 
	 *
	 * Enabling this feature make the implementation of the plugin simple.
	 * However, this feature takes overhead processing costs for converting data of parameters and return value.
	 * Therefore, if you want to implement a function plug-in which will be called frequently from scripts 
	 * of which processing speed is required to be fast in some degree,
	 * disabling this feature gives some advantage to reduce overhead processing costs of function calls.
	 * 
	 * @return Returns true if the data-conversions are necessary.
	 */
	public abstract boolean isDataConversionNecessary();


	/**
	 * Invokes the process of this function.
	 *
	 * The data of the actual arguments of the function will be passed to the argument of this method: "arguments", 
	 * which is an Object[] type array storing data of each actual argument as an element.
	 * 
	 * When data-conversion feature is enabled (see: {@link isDataConversionNecessary()} method), 
	 * each actual argument will be stored as a simple type of data in "arguments".
	 * For example, Double instance for double-type parameter, long[][] instance for long[][]-type parameter, and so on.
	 * 
	 * On the other hand, when data-conversion feature is disabled, in "arguments",
	 * each actual argument will be stored as a data-container object implementing data-I/O interface,
	 * specified as each element of return values of {@link getParameterUnconvertedClasses()} method.
	 * In addition, in this case, 
	 * "arguments[0]" will be a data-container object for storing data of the return value of this function,
	 * so the data of first argument will be stored at "arguments[1]", and the next arg will be stored at "argument[2]". 
	 *
	 * @param arguments The array storing all actual arguments, which are passed from the caller-side.
	 * @return The return value of the function call.
	 *         (When data-conversion feature is disabled, store the return value to arguments[0] instead.)
	 * 
	 * @throws ConnectorException Thrown when any error occurred in the process of this function.
	 */
	public abstract Object invoke(Object[] arguments) throws ConnectorException ;


	/**
	 * Returns the instance of "Class" class, representing the interface or the class of the engine connector, 
	 * which is an object for communicating with the scripting engine.
	 * 
	 * The instance of the specified interface/class by this method will be passed to the argument of 
	 * {@link initializeForConnection(Object)}, {@link initializeForExecution(Object)},
	 * {@link finalizeForTermination(Object)}, {@link finalizeForDisconnection(Object)} methods.
	 * 
	 * What type of interfaces are available depend on the implementation of the scripting engine, but at least, 
	 * {@link EngineConnectorInterface1 ECI1} is guaranteed to be available by the specification of XFCI1.
	 * 
	 * @return The Class representing the interface/class for communicating with the scripting engine.
	 */
	public abstract Class<?> getEngineConnectorClass();


	/**
	 * Performs the initialization process necessary when this plug-in is connected to the scripting engine.
	 *
	 * @param engineConnector The engine connector (see: {@link ExternalVariableConnectorInterface1#getEngineConnectorClass()} method).
	 * @throws ConnectorException Thrown when the initialization has failed.
	 */
	public abstract void initializeForConnection(Object engineConnector) throws ConnectorException;


	/**
	 * Performs the finalization process necessary when this plug-in is disconnected from the scripting engine.
	 *
	 * @param engineConnector The engine connector (see: {@link ExternalVariableConnectorInterface1#getEngineConnectorClass()} method).
	 * @throws ConnectorException Thrown when the finalization has failed.
	 */
	public abstract void finalizeForDisconnection(Object engineConnector) throws ConnectorException;


	/**
	 * Performs the initialization process necessary for each execution of a script.
	 *
	 * @param engineConnector The engine connector (see: {@link ExternalVariableConnectorInterface1#getEngineConnectorClass()} method).
	 * @throws ConnectorException Thrown when the initialization has failed.
	 */
	public abstract void initializeForExecution(Object engineConnector) throws ConnectorException;


	/**
	 * Performs the finalization process necessary for each execution of a script.
	 *
	 * @param engineConnector The engine connector (see: {@link ExternalVariableConnectorInterface1#getEngineConnectorClass()} method).
	 * @throws ConnectorException Thrown when the finalization has failed.
	 */
	public abstract void finalizeForTermination(Object engineConnector) throws ConnectorException;

}
