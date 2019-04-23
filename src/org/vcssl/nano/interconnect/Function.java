package org.vcssl.nano.lang;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public final class Function extends AbstractFunction {

	private String functionName = null;
	private String[] parameterNames = null;
	private String[] parameterDataTypeNames = null;
	private int[] parameterRanks = null;
	private String returnDataTypeName = null;
	private int returnRank = -1;

	public Function (String functionName,
			String[] parameterDataTypeNames, int[] parameterRanks,
			String returnDataTypeName, int returnRank) {

		this.functionName = functionName;
		this.returnDataTypeName = returnDataTypeName;
		this.returnRank = returnRank;
		this.parameterDataTypeNames = parameterDataTypeNames;
		this.parameterRanks = parameterRanks;
	}

	@Override
	public final String getFunctionName() {
		return this.functionName;
	}

	@Override
	public final String[] getParameterNames() {
		return this.parameterNames;
	}

	@Override
	public final String[] getParameterDataTypeNames() {
		return this.parameterDataTypeNames;
	}

	@Override
	public final int[] getParameterArrayRanks() {
		return this.parameterRanks;
	}

	@Override
	public final boolean isVariadic() {
		// 現在は未対応
		return false;
	}

	@Override
	public final String getReturnDataTypeName() {
		return this.returnDataTypeName;
	}

	@Override
	public final int getReturnArrayRank() {
		return this.returnRank;
	}

	@Override
	public final void invoke(DataContainer<?>[] argumentDataUnits, DataContainer<?> returnDataUnit) {
		// 現在は未対応
		throw new VnanoFatalException("The invocation of the internal function from the outside has not implemented yet.");
	}

}
