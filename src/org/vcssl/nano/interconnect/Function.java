/*
 * Copyright(C) 2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import java.util.Arrays;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public final class Function extends AbstractFunction {

	private String functionName = null;
	private String[] parameterNames = null;
	private String[] parameterDataTypeNames = null;
	private int[] parameterRanks = null;
	private boolean[] parameterDataTypeArbitrarinesses = null;
	private boolean[] parameterRankArbitrarinesses = null;
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

		int numParameters = parameterDataTypeNames.length;
		this.parameterDataTypeArbitrarinesses = new boolean[numParameters];
		this.parameterRankArbitrarinesses = new boolean[numParameters];
		Arrays.fill(parameterDataTypeArbitrarinesses, false);
		Arrays.fill(parameterRankArbitrarinesses, false);
	}


	@Override
	public final String getFunctionName() {
		return this.functionName;
	}

	@Override
	public final boolean hasNameSpace() {
		return false; // スクリプト内での名前空間定義はサポートされていない
	}

	@Override
	public final String getNameSpace() {
		return null; // スクリプト内での名前空間定義はサポートされていない
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
	public final boolean[] getParameterDataTypeArbitrarinesses() {
		return this.parameterDataTypeArbitrarinesses;
	}

	@Override
	public final boolean[] getParameterArrayRankArbitrarinesses() {
		return this.parameterRankArbitrarinesses;
	}

	@Override
	public final boolean isParameterCountArbitrary() {
		return false;
	}

	@Override
	public final boolean hasVariadicParameters() {
		// 現在は未対応
		return false;
	}

	@Override
	public final String getReturnDataTypeName(String[] argumentDataTypeNames, int[] argumentArrayRanks) {
		return this.returnDataTypeName;
	}

	@Override
	public final int getReturnArrayRank(String[] argumentDataTypeNames, int[] argumentArrayRanks) {
		return this.returnRank;
	}

	@Override
	public final void invoke(DataContainer<?>[] argumentDataUnits, DataContainer<?> returnDataUnit) {
		// 現在は未対応
		throw new VnanoFatalException("The invocation of the internal function from the outside has not implemented yet.");
	}

}
