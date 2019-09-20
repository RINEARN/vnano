/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import org.vcssl.connect.ConnectorException;
import org.vcssl.connect.ExternalFunctionConnectorInterface1;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.vm.memory.DataContainer;


/**
 * <p>
 * {@link org.vcssl.connect.ExternalFunctionConnectorInterface1 XFCI 1}
 * 形式の外部関数プラグインを、Vnano処理系内での関数仕様
 * （{@link org.vcssl.nano.interconnect.AbstractVariable AbstractFunction}）
 * に基づく関数オブジェクトへと変換し、
 * {@link Interconnect Interconnect} に接続するためのアダプタです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public final class Xfci1ToFunctionAdapter extends AbstractFunction {

	/** XFCI準拠の外部変数プラグインです。 */
	private ExternalFunctionConnectorInterface1 xfciPlugin = null;

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
	@SuppressWarnings("unused")
	private int returnArrayRank = -1;

	/** 所属している名前空間があるかどうかを保持します。 */
	private boolean hasNameSpace = false;

	/** 所属している名前空間を保持します。 */
	private String nameSpace = null;


	/**
	 * 指定されたXFCI準拠の外部変数プラグインを、
	 * 処理系内部での仕様に準拠した関数へと変換するアダプタを生成します。
	 *
	 * @param xfciPlugin XFCI準拠の外部変数プラグイン
	 * @throws VnanoException
	 * 		引数のデータや型が、この処理系内部では使用できない場合に発生します。
	 */
	public Xfci1ToFunctionAdapter(ExternalFunctionConnectorInterface1 xfciPlugin) throws VnanoException {

		this.xfciPlugin = xfciPlugin;

		Class<?>[] parameterClasses = this.xfciPlugin.getParameterClasses();
		Class<?> returnClass = this.xfciPlugin.getReturnClass(parameterClasses);
		int parameterLength = parameterClasses.length;

		this.returnDataConverter = new DataConverter(this.xfciPlugin.getReturnClass(parameterClasses));
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
	 * 指定されたXFCI準拠の外部変数プラグインを、名前空間に所属させつつ、
	 * 処理系内部での仕様に準拠した関数へと変換するアダプタを生成します。
	 *
	 * @param xfciPlugin XFCI準拠の外部変数プラグイン
	 * @param nameSpace 名前空間
	 * @throws VnanoException
	 * 		引数のデータや型が、この処理系内部では使用できない場合に発生します。
	 */
	public Xfci1ToFunctionAdapter(ExternalFunctionConnectorInterface1 xfciPlugin, String nameSpace)
			throws VnanoException {

		this(xfciPlugin);
		this.hasNameSpace = true;
		this.nameSpace = nameSpace;
	}


	/**
	 * 関数名を取得します。
	 *
	 * @return 関数名
	 */
	@Override
	public String getFunctionName() {
		return this.xfciPlugin.getFunctionName();
	}


	/**
	 * 所属している名前空間があるかどうかを判定します。
	 *
	 * @return 名前空間に所属していれば true
	 */
	@Override
	public boolean hasNameSpace() {
		return this.hasNameSpace;
	}


	/**
	 * 所属している名前空間を返します。
	 *
	 * @return 名前空間
	 */
	@Override
	public String getNameSpace() {
		return this.nameSpace;
	}


	/**
	 * 全ての仮引数の名称を配列として取得します。
	 *
	 * @return 全ての仮引数の名称を格納する配列
	 */
	@Override
	public String[] getParameterNames() {
		return this.xfciPlugin.getParameterNames();
	}


	/**
	 * 全ての仮引数のデータ型名を配列として取得します。
	 *
	 * @return 全ての仮引数のデータ型を格納する配列
	 */
	@Override
	public String[] getParameterDataTypeNames() {
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
	 * 全ての仮引数の配列次元数（スカラは0次元として扱う）を配列として取得します。
	 *
	 * @return 全ての仮引数の配列次元数を格納する配列
	 */
	@Override
	public int[] getParameterArrayRanks() {
		return this.parameterArrayRanks;
	}


	/**
	 * 全ての仮引数において、データ型が可変であるかどうかを格納する配列を返します。
	 *
	 * @return 全引数のデータ型が可変であるかどうかを格納する配列
	 */
	public boolean[] getParameterDataTypeArbitrarinesses() {
		return this.xfciPlugin.getParameterClassArbitrarinesses();
	}


	/**
	 * 全ての仮引数において、配列次元数が可変であるかどうかを格納する配列を返します。
	 *
	 * @return 全引数の配列次元数が可変であるかどうかを格納する配列
	 */
	public boolean[] getParameterArrayRankArbitrarinesses() {
		return this.xfciPlugin.getParameterRankArbitrarinesses();
	}


	/**
	 * 可変長引数であるかどうかを判定します。
	 *
	 * @return 可変長引数であればtrue
	 */
	@Override
	public boolean isVariadic() {
		return this.xfciPlugin.isVariadic();
	}


	/**
	 * 戻り値のデータ型を取得します。
	 *
	 * @return 戻り値のデータ型
	 */
	/*
	public DataType getReturnDataType() {
		return this.returnDataType;
	}
	*/


	/**
	 * 戻り値のデータ型名を取得します。
	 * 返される型名の表記内に、配列部分 [][]...[] は含まれません。
	 *
	 * @return 戻り値のデータ型名
	 */
	@Override
	public String getReturnDataTypeName(String[] argumentDataTypeNames, int[] argumentArrayRanks) {
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
	 * 戻り値の配列次元数を取得します。
	 *
	 * @return 戻り値の配列次元数
	 */
	@Override
	public int getReturnArrayRank(String[] argumentDataTypeNames, int[] argumentArrayRanks) {
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
	 * 関数を実行します。
	 *
	 * @param argumentDataContainers 実引数のデータを保持するデータコンテナの配列（各要素が個々の実引数に対応）
	 * @param returnDataContainer 戻り値のデータを格納するデータコンテナ
	 */
	@Override
	public void invoke(DataContainer<?>[] argumentDataContainers, DataContainer<?> returnDataContainer) {

		int argLength = argumentDataContainers.length;
		Object[] convertedArgs = new Object[argLength];

		// 自動のデータ型変換が有効な場合
		if (this.xfciPlugin.isDataConversionNecessary()) {

			// 引数のデータ型を変換
			for (int argIndex=0; argIndex<argLength; argIndex++) {
				if (!this.parameterDataTypes[argIndex].equals(DataType.VOID)) {
					try {
						convertedArgs[argIndex] = this.parameterDataConverters[argIndex].convertToExternalObject(argumentDataContainers[argIndex]);
					} catch (VnanoException e) {
						throw new VnanoFatalException(e);
					}
				}
			}

			// プラグインの関数を実行
			Object returnObject = null;
			try {
				returnObject = this.xfciPlugin.invoke(convertedArgs);
			} catch (ConnectorException e) {
				throw new VnanoFatalException(e);
			}

			// 戻り値のデータ型を変換
			if (!this.returnDataType.equals(DataType.VOID)) {
				try {
					this.returnDataConverter.convertToDataContainer(returnObject, returnDataContainer);
				} catch (VnanoException e) {
					throw new VnanoFatalException(e);
				}
			}

		// 自動のデータ型変換が無効な場合
		} else {
			try {
				// この場合、プラグインに渡す引数の最初の要素が、戻り値格納用コンテナになる
				DataContainer<?>[] xfciArgContainers = new DataContainer<?>[argLength + 1];
				xfciArgContainers[0] = returnDataContainer;
				System.arraycopy(argumentDataContainers, 0, xfciArgContainers, 1, argLength);
				this.xfciPlugin.invoke(xfciArgContainers);
			} catch (ConnectorException e) {
				throw new VnanoFatalException(e);
			}
		}

	}

}
