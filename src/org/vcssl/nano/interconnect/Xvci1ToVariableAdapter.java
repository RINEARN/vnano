/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import org.vcssl.connect.ArrayDataAccessorInterface1;
import org.vcssl.connect.BoolScalarDataAccessorInterface1;
import org.vcssl.connect.ConnectorException;
import org.vcssl.connect.EngineConnectorInterface1;
import org.vcssl.connect.ExternalVariableConnectorInterface1;
import org.vcssl.connect.Float64ScalarDataAccessorInterface1;
import org.vcssl.connect.Int64ScalarDataAccessorInterface1;
import org.vcssl.connect.StringScalarDataAccessorInterface1;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.vm.memory.DataContainer;


/**
 * The adapter class for converting 
 * a {@link org.vcssl.connect.ExternalVariableConnectorInterface1 XVCI1} type external variable plugin
 * to an {@link org.vcssl.nano.interconnect.AbstractVariable AbstractVariable} type variable object.
 */
public class Xvci1ToVariableAdapter extends AbstractVariable {

	/** The XVCI1 type external variable plugin to be converted. */
	private ExternalVariableConnectorInterface1 xvciPlugin = null;

	/** The name of the variable. */
	private String variableName = null;

	/** The data converters for converting data of this variable. */
	private DataConverter dataConverter = null;

	/** The name of the namespace to which this variable belongs. */
	private String namespaceName = null;


	/**
	 * Create an adapter converting the specified XVCI1 plugin to 
	 * an {@link org.vcssl.nano.interconnect.AbstractVariable AbstractVariable} type variable object.
	 *
	 * @param xvciPlugin The XVCI1 plugin to be converted.
	 * @throws VnanoException Thrown when incompatible data-types, array-ranks, and so on have been detected.
	 */
	public Xvci1ToVariableAdapter(ExternalVariableConnectorInterface1 xvciPlugin) throws VnanoException {
		this.validate(xvciPlugin);
		this.xvciPlugin = xvciPlugin;
		this.variableName = xvciPlugin.getVariableName();
		this.dataConverter = new DataConverter(this.xvciPlugin.getDataClass());

		if (!this.xvciPlugin.isDataConversionNecessary()) {
			Class<?> dataAccessorInterface = this.xvciPlugin.getDataUnconvertedClass();
			if (!dataAccessorInterface.isAssignableFrom(DataContainer.class)) {
				String errorWords[] = new String[] {
					dataAccessorInterface.getCanonicalName(), this.xvciPlugin.getClass().getCanonicalName()
				};
				throw new VnanoException(ErrorType.INCOMPATIBLE_DATA_ACCESSOR_INTERFACE, errorWords);
			}
		}
	}


	/**
	 * Returns the XVCI1 plugin to be converted by this adapter.
	 *
	 * @return The XVCI1 plugin to be converted by this adapter.
	 */
	public ExternalVariableConnectorInterface1 getXvci1Plugin() {
		return this.xvciPlugin;
	}


	/**
	 * Gets the name of this variable.
	 *
	 * @return The name of this variable.
	 */
	@Override
	public String getVariableName() {
		return this.variableName;
	}


	/**
	 * Sets the name of this variable.
	 * 
	 * This method is used for setting an alias for an external variable.
	 * 
	 * @param variableName The name of this variable.
	 */
	@Override
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}


	/**
	 * Returns whether this variable belongs to any namespace.
	 *
	 * @return Returns true if this variable belongs to a namespace.
	 */
	@Override
	public boolean hasNamespaceName() {
		return this.namespaceName != null;
	}


	/**
	 * Gets the name of the namespace to which this variable belongs.
	 *
	 * @return The name of the namespace to which this variable belongs.
	 */
	@Override
	public String getNamespaceName() {
		return this.namespaceName;
	}


	/**
	 * Sets the name of the namespace to which this variable belongs.
	 *
	 * @namespaceName The name of the namespace to which this variable belongs.
	 */
	@Override
	public void setNamespaceName(String namespaceName) {
		this.namespaceName = namespaceName;
	}


	/**
	 * Gets the name of the data-type of this variable.
	 * In the data-type name, array declaration parts [][]...[] aren't contained.
	 *
	 * @return The name of the data-type of this variable.
	 */
	@Override
	public String getDataTypeName() {
		return DataTypeName.getDataTypeNameOf(this.dataConverter.getDataType());
	}


	/**
	 * Gets the array-rank of this variable.
	 * 
	 * Note that, the array-rank of an scalar is 0.
	 *
	 * @return The array-rank of this variable.
	 */
	@Override
	public int getRank() {
		return this.dataConverter.getRank();
	}
	// TO DO: rename to: getArrayRank()


	/**
	 * Gets the data container storing data of this variable.
	 *
	 * @return The data container storing data of this variable.
	 */
	@Override
	public DataContainer<?> getDataContainer() throws VnanoException {
		try {

			// If the automatic-data-conversion feature is enabled:
			if (this.xvciPlugin.isDataConversionNecessary()) {
				Object data = null;
				data = this.xvciPlugin.getData();
				return this.dataConverter.convertToDataContainer(data);

			// If the automatic-data-conversion feature is disabled:
			} else {
				DataContainer<?> dataContainer = new DataContainer<>();
				this.xvciPlugin.getData(dataContainer);
				return dataContainer;
			}

		// If any exception has occurred, re-throw it as a VnanoException,
		} catch (Exception e) { // Don't modify "Exception" to "Throwable". The latter is too wide for catching here.

			// Prepare information to be embedded in the error message.
			String[] errorWords = { this.xvciPlugin.getVariableName(), null };
			if (e instanceof ConnectorException) {
				errorWords[1] = e.getMessage();
			}
			throw new VnanoException(
				ErrorType.EXTERNAL_VARIABLE_PLUGIN_CRASHED, errorWords, e
			);
		}
	}


	/**
	 * Sets the data container storing data of this variable.
	 *
	 * @param dataContainer The data container storing data of this variable.
	 */
	@Override
	public void setDataContainer(DataContainer<?> dataContainer) throws VnanoException {

		// If the automatic-data-conversion feature is enabled:
		if (this.xvciPlugin.isDataConversionNecessary()) {

			Object data = null;
			try {
				data = this.dataConverter.convertToExternalObject(dataContainer);
			} catch (VnanoException e) {
				throw new VnanoException(
					ErrorType.EXTERNAL_VARIABLE_PLUGIN_CRASHED,
					new String[] { this.xvciPlugin.getVariableName(), e.getMessage() }, e
				);
			}

			try {
				this.xvciPlugin.setData(data);
			} catch (ConnectorException e) {
				throw new VnanoException(
					ErrorType.EXTERNAL_VARIABLE_PLUGIN_CRASHED,
					new String[] { this.xvciPlugin.getVariableName(), e.getMessage() }, e
				);
			}

		// If the automatic-data-conversion feature is disabled:
		} else {
			try {
				this.xvciPlugin.setData(dataContainer);
			} catch (ConnectorException e) {
				throw new VnanoException(
					ErrorType.EXTERNAL_VARIABLE_PLUGIN_CRASHED,
					new String[] { this.xvciPlugin.getVariableName(), e.getMessage() }, e
				);
			}
		}
	}


	/**
	 * Returns whether this variable is constant.
	 * 
	 * @return Returns true if this variable is constant.
	 */
	@Override
	public boolean isConstant() {
		return this.xvciPlugin.isConstant();
	}


	/**
	 * Returns whether this variable has a serial number,
	 * which is a number to distinguish multiple variables having the same name.
	 *
	 * @return Returns true if this variable has a serial number.
	 */
	@Override
	public boolean hasSerialNumber() {
		return false;
	}


	/**
	 * Gets the serial number which is a number to distinguish multiple variables having the same name.
	 * 
	 * @return The serial number.
	 */
	@Override
	public int getSerialNumber() {
		return -1;
	}


	/**
	 * Validates whether the specified plug-in implements XVCI1 correctly, and it is available on the current version of Vnano Engine.
	 * If no issues are detected for the plug-in, nothing will occur.
	 * 
	 * @param plugin The plug-in to be validated.
	 * @throws VnanoException Thrown if the specified plug-in has an incorrect something.
	 */
	private void validate(ExternalVariableConnectorInterface1 plugin) throws VnanoException {
		
		// getVariableName()
		if (plugin.getVariableName() == null) {
			String errorMessage = "getVariableName(): The returned value is null.";
			throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });
		}
		
		// getDataClass()
		if (plugin.getDataClass() == null) {
			String errorMessage = "getDataClass(): The returned value is null.";
			throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });
		}

		// 	getDataUnconvertedClass()
		if (!plugin.isDataConversionNecessary()) {
			if (plugin.getDataUnconvertedClass() == null) {
				String errorMessage = "getDataUnconvertedClasses(): The returned value is null.";
				throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });
			}
			Class<?> dataClass = plugin.getDataUnconvertedClass();
			if (!dataClass.equals(ArrayDataAccessorInterface1.class)
					&& !dataClass.equals(Int64ScalarDataAccessorInterface1.class)
					&& !dataClass.equals(Float64ScalarDataAccessorInterface1.class)
					&& !dataClass.equals(BoolScalarDataAccessorInterface1.class)
					&& !dataClass.equals(StringScalarDataAccessorInterface1.class)
					&& !dataClass.equals(DataContainer.class) ) {

				String errorMessage = "getParameterUnconvertedClasses(): The returned class/interface \""
						+ dataClass.getName() + "\"is not supported on the current version of Vnano Engine.";
				throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });	
			}
		}

		// isDataTypeArbitrary()()
		if (plugin.isDataTypeArbitrary()) {
			String errorMessage = "isDataTypeArbitrary(): Returned true, but this feature has not been supported yet on the current version of Vnano Engine.";
			throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });
		}
		
		// isArrayRankArbitrary()
		if (plugin.isArrayRankArbitrary()) {
			String errorMessage = "isArrayRankArbitrary(): Returned true, but this feature has not been supported yet on the current version of Vnano Engine.";
			throw new VnanoException(ErrorType.PLUGIN_VALIDATION_FAILED, new String[] { plugin.getClass().getName(), errorMessage });
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

