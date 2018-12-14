/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import org.vcssl.connect.ExternalFunctionConnector1;
import org.vcssl.connect.ExternalFunctionException;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.VnanoRuntimeException;
import org.vcssl.nano.VnanoSyntaxException;
import org.vcssl.nano.lang.AbstractFunction;
import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.DataConverter;

/**
 * <p>
 * {@link org.vcssl.connect.ExternalFunctionConnector1 XFCI 1}
 * 形式の外部関数プラグインを、Vnano処理系内での関数仕様
 * （{@link org.vcssl.nano.lang.AbstractVariable AbstractFunction}）
 * に基づく関数オブジェクトへと変換し、
 * {@link Interconnect Interconnect} に接続するためのアダプタです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class Xfci1FunctionAdapter extends AbstractFunction {

	/** XFCI準拠の外部変数プラグインです。 */
	private ExternalFunctionConnector1 xfciPlugin = null;

	/** 外部関数の引数と処理系内部の関数の引数とで、データの型変換を行うコンバータです。 */
	private DataConverter[] parameterDataConverters = null;

	/** 処理系内部側での全引数のデータ型を配列として保持します。 */
	private DataType[] parameterDataTypes = null;

	/** 処理系内部側での全引数の配列次元数（スカラは0次元として扱う）を配列として保持します。 */
	private int[] parameterArrayRanks = null;

	/** 外部関数の戻り値と処理系内部の関数の戻り値とで、データの型変換を行うコンバータです。 */
	private DataConverter returnDataConverter = null;

	/** 処理系内部側での戻り値のデータ型を保持します。 */
	private DataType returnDataType = null;

	/** 処理系内部側での戻り値の配列次元数（スカラは0次元として扱う）を保持します。 */
	private int returnArrayRank = -1;


	/**
	 * 指定されたXFCI準拠の外部変数プラグインを、
	 * 処理系内部での仕様に準拠した関数へと変換するアダプタを生成します。
	 *
	 * @param xfciPlugin XFCI準拠の外部変数プラグイン
	 * @throws VnanoSyntaxException
	 * 		引数のデータや型が、この処理系内部では使用できない場合に発生します。
	 */
	public Xfci1FunctionAdapter(ExternalFunctionConnector1 xfciPlugin) throws VnanoSyntaxException {

		this.xfciPlugin = xfciPlugin;

		Class<?>[] parameterClasses = this.xfciPlugin.getParameterClasses();
		int parameterLength = parameterClasses.length;

		this.returnDataConverter = new DataConverter(this.xfciPlugin.getReturnClass());
		this.returnDataType = this.returnDataConverter.getDataType();
		this.returnArrayRank = this.returnDataConverter.getRank();

		this.parameterDataConverters = new DataConverter[parameterLength];
		this.parameterDataTypes = new DataType[parameterLength];
		this.parameterArrayRanks = new int[parameterLength];

		for (int parameterIndex=0; parameterIndex<parameterLength; parameterIndex++) {

			this.parameterDataConverters[parameterIndex] = new DataConverter(
					parameterClasses[parameterIndex]
			);

			this.parameterDataTypes[parameterIndex]
					= this.parameterDataConverters[parameterIndex].getDataType();

			this.parameterArrayRanks[parameterIndex]
					= this.parameterDataConverters[parameterIndex].getRank();
		}
	}


	/**
	 * 関数名を取得します。
	 *
	 * @return 関数名
	 */
	public String getFunctionName() {
		return this.xfciPlugin.getFunctionName();
	}


	/**
	 * 全ての仮引数の名称を配列として取得します。
	 *
	 * @return 全ての仮引数の名称を格納する配列
	 */
	public String[] getParameterNames() {
		return this.xfciPlugin.getParameterNames();
	}


	/**
	 * 全ての仮引数のデータ型を配列として取得します。
	 *
	 * @return 全ての仮引数のデータ型を格納する配列
	 */
	public DataType[] getParameterDataTypes() {
		return this.parameterDataTypes;
	}


	/**
	 * 全ての仮引数の配列次元数（スカラは0次元として扱う）を配列として取得します。
	 *
	 * @return 全ての仮引数の配列次元数を格納する配列
	 */
	public int[] getParameterArrayRanks() {
		return this.parameterArrayRanks;
	}


	/**
	 * 可変長引数であるかどうかを判定します。
	 *
	 * @return 可変長引数であればtrue
	 */
	public boolean isVariadic() {
		return this.xfciPlugin.isVariadic();
	}


	/**
	 * 戻り値のデータ型を取得します。
	 *
	 * @return 戻り値のデータ型
	 */
	public DataType getReturnDataType() {
		return this.returnDataType;
	}


	/**
	 * 戻り値の配列次元数を取得します。
	 *
	 * @return 戻り値の配列次元数
	 */
	public int getReturnArrayRank() {
		return this.returnArrayRank;
	}


	/**
	 * 関数を実行します。
	 *
	 * @param argumentDataUnits 実引数のデータを保持するデータユニットの配列（各要素が個々の実引数に対応）
	 * @param returnDataUnit 戻り値のデータを格納するデータユニット
	 */
	public void invoke(DataContainer<?>[] argumentDataUnits, DataContainer<?> returnDataUnit) {

		int argLength = argumentDataUnits.length;
		Object[] convertedArgs = new Object[argLength];

		for (int argIndex=0; argIndex<argLength; argIndex++) {
			if (!this.parameterDataTypes[argIndex].equals(DataType.VOID)) {
				try {
					convertedArgs[argIndex] = this.parameterDataConverters[argIndex].convertToExternalObject(argumentDataUnits[argIndex]);
				} catch (VnanoSyntaxException e) {
					// 暫定的な簡易例外処理
					throw new VnanoFatalException(e);
				}
			}
		}

		Object returnObject = null;
		try {
			returnObject = this.xfciPlugin.invoke(convertedArgs);
		} catch (ExternalFunctionException e) {
			// 暫定的な簡易例外処理
			throw new VnanoRuntimeException();
		}

		if (!this.returnDataType.equals(DataType.VOID)) {
			try {
				this.returnDataConverter.convertToDataContainer(returnObject, returnDataUnit);
			} catch (VnanoSyntaxException e) {
				// 暫定的な簡易例外処理
				throw new VnanoFatalException(e);
			}
		}

	}

}
