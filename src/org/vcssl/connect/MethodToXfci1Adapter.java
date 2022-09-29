/*
 * ==================================================
 * Method to XFCI Plug-in Adapter
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2017-2022 by RINEARN
 * ==================================================
 */

package org.vcssl.connect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;


/**
 * An adapter class converting a host-language-side method to a 
 * {@link org.vcssl.connect.ExternalFunctionConnectorInterface1 XFCI 1} plug-in, 
 * to call it from scripts.
 */
public class MethodToXfci1Adapter implements ExternalFunctionConnectorInterface1 {

	/** The method to be called from scripts. */
	private Method method = null;

	/** The object instance to which the method belongs to. */
	private Object objectInstance = null;


	/**
	 * Creates a new adapter to access to a non-static method.
	 * 
	 * @param method The non-static method to be accessed in scripts.
	 * @param objectInstance The object instance to which the method belongs to.
	 */
	public MethodToXfci1Adapter(Method method, Object objectInstance) {
		this.method = method;
		this.objectInstance = objectInstance;
	}


	/**
	 * Creates a new adapter to access to a static method.
	 * 
	 * @param method The static method to be accessed in scripts.
	 */
	public MethodToXfci1Adapter(Method method) {
		this.method = method;
		this.objectInstance = null;
	}


	@Override
	public String getFunctionName() {
		return this.method.getName();
	}

	@Override
	public boolean hasParameterNames() {
		return false;
	}

	@Override
	public String[] getParameterNames() {
		return null;
	}

	@Override
	public Class<?>[] getParameterClasses() {
		return this.method.getParameterTypes();
	}

	@Override
	public Class<?>[] getParameterUnconvertedClasses() {
		return null;
	}

	@Override
	public boolean[] getParameterDataTypeArbitrarinesses() {
		int numParameters = this.method.getParameterCount();
		boolean[] result = new boolean[numParameters];
		Arrays.fill(result, false);
		return result;
	}

	@Override
	public boolean[] getParameterArrayRankArbitrarinesses() {
		int numParameters = this.method.getParameterCount();
		boolean[] result = new boolean[numParameters];
		Arrays.fill(result, false);
		return result;
	}

	@Override
	public boolean[] getParameterReferencenesses() {
		int numParameters = this.method.getParameterCount();
		boolean[] result = new boolean[numParameters];
		Arrays.fill(result, false);
		return result;
	}

	@Override
	public boolean[] getParameterConstantnesses() {
		int numParameters = this.method.getParameterCount();
		boolean[] result = new boolean[numParameters];
		Arrays.fill(result, false);
		return result;
	}

	@Override
	public boolean isParameterCountArbitrary() {
		return false;
	}

	@Override
	public boolean hasVariadicParameters() {
		return this.method.isVarArgs();
	}

	@Override
	public Class<?> getReturnClass(Class<?>[] parameterClasses) {
		return this.method.getReturnType();
	}

	@Override
	public Class<?> getReturnUnconvertedClass(Class<?>[] parameterClasses) {
		return null;
	}

	@Override
	public boolean isReturnDataTypeArbitrary() {
		return false;
	}

	@Override
	public boolean isReturnArrayRankArbitrary() {
		return false;
	}

	@Override
	public boolean isDataConversionNecessary() {
		return true;
	}

	@Override
	public Object invoke(Object[] arguments) throws ConnectorException {
		try {
			return this.method.invoke(objectInstance, arguments);
		} catch (IllegalArgumentException illegalArgumentException) {
			throw new ConnectorException(
					objectInstance.getClass().getCanonicalName() + " class has no method named \"" + this.method.getName()
					+ "\" with expected parameters.",
					illegalArgumentException
			);
		} catch (IllegalAccessException illegalAccessException) {
			throw new ConnectorException(
					"The method \"" + this.method.getName() + "\" of " + objectInstance.getClass().getCanonicalName()
					+ " class is not accessable (probably it is private or protected).",
					illegalAccessException
			);
		} catch (InvocationTargetException invocationTargetException) {
			throw new ConnectorException(invocationTargetException);
		}
	}

	@Override
	public Class<?> getEngineConnectorClass() {
		return EngineConnectorInterface1.class;
	}

	@Override
	public void initializeForConnection(Object engineConnector) throws ConnectorException {
	}

	@Override
	public void finalizeForDisconnection(Object engineConnector) throws ConnectorException {
	}

	@Override
	public void initializeForExecution(Object engineConnector) throws ConnectorException {
	}

	@Override
	public void finalizeForTermination(Object engineConnector) throws ConnectorException {
	}
}
