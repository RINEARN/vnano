/*
 * Copyright(C) 2019-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import java.util.Arrays;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

public final class InternalFunction extends AbstractFunction {

	private String functionName = null;
	private String[] parameterNames = null;
	private String[] parameterDataTypeNames = null;
	private int[] parameterRanks = null;
	private boolean[] parameterDataTypeArbitrarinesses = null;
	private boolean[] parameterRankArbitrarinesses = null;
	private boolean[] parameterReferencenesses = null;
	private boolean[] parameterConstantnesses = null;
	private String returnDataTypeName = null;
	private int returnRank = -1;


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


	@Override
	public final String getFunctionName() {
		return this.functionName;
	}

	/**
	 * このメソッドは使用できません。
	 *
	 * このメソッドは、外部関数などの接続時に、エイリアスを指定するための機能として AbstractVariable に宣言されています。
	 * 一方、現在のVnanoでは、内部関数にエイリアスを付ける言語機能はサポートしていないため、
	 * もしも内部関数に対してこのメソッドが呼ばれた場合、それは実装上のミスによるものと考えられます。
	 * 従って、このメソッドは呼ばれると VnanoFatalException を発生させます。
	 *
	 * @param functionName 関数名
	 */
	@Override
	public void setFunctionName(String functionName) {
		throw new VnanoFatalException("Names of internal functions should not be changed.");
	}

	@Override
	public final boolean hasNamespaceName() {
		return false; // スクリプト内での名前空間定義はサポートされていない
	}

	@Override
	public final String getNamespaceName() {
		throw new VnanoFatalException("Internal functions can not belongs to any namespaces.");
	}

	@Override
	public final void setNamespaceName(String namespaceName) {
		throw new VnanoFatalException("Internal functions can not belongs to any namespaces.");
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
		// 現在は未対応なので常に全要素falseの配列を返す
		return this.parameterDataTypeArbitrarinesses;
	}

	@Override
	public final boolean[] getParameterArrayRankArbitrarinesses() {
		// 現在は未対応なので常に全要素falseの配列を返す
		return this.parameterRankArbitrarinesses;
	}

	@Override
	public boolean[] getParameterConstantnesses() {
		// 現在は未対応なので常に全要素falseの配列を返す
		return this.parameterConstantnesses;
	}

	public void setParameterReferencenesses(boolean[] parameterReferencenesses) {
		this.parameterReferencenesses = parameterReferencenesses;
	}

	@Override
	public boolean[] getParameterReferencenesses() {
		return this.parameterReferencenesses;
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
