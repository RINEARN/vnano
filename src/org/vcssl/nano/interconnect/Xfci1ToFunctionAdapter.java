/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
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
import org.vcssl.nano.spec.LanguageSpecContainer;
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

	/** データ型名が定義された設定オブジェクトを保持します。 */
	private final DataTypeName DATA_TYPE_NAME;

	/** XFCI準拠の外部変数プラグインです。 */
	private ExternalFunctionConnectorInterface1 xfciPlugin = null;

	/** 外部関数の引数と処理系内部の関数の引数とで、データの型変換を行うコンバータです。 */
	private DataConverter[] parameterDataConverters = null;

	/** 処理系内部側での全引数のデータ型を配列として保持します。 */
	private DataType[] parameterDataTypes = null;

	/** 処理系内部側での全引数の配列次元数（スカラは0次元として扱う）を配列として保持します。 */
	private int[] parameterArrayRanks = null;

	/** 各引数を参照渡しすべきかどうかを、配列として保持します。 */
	private boolean[] parameterReferencenesses = null;

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
	 * @param langSpec 言語仕様設定
	 * @throws VnanoException
	 * 		引数のデータや型が、この処理系内部では使用できない場合に発生します。
	 */
	public Xfci1ToFunctionAdapter(
			ExternalFunctionConnectorInterface1 xfciPlugin, LanguageSpecContainer langSpec)
					throws VnanoException {

		this.DATA_TYPE_NAME = langSpec.DATA_TYPE_NAME;
		this.xfciPlugin = xfciPlugin;

		Class<?>[] parameterClasses = this.xfciPlugin.getParameterClasses();
		Class<?> returnClass = this.xfciPlugin.getReturnClass(parameterClasses);
		int parameterLength = parameterClasses.length;

		this.returnDataConverter = new DataConverter(this.xfciPlugin.getReturnClass(parameterClasses), langSpec);
		this.returnDataType = this.returnDataConverter.getDataType();
		this.returnArrayRank = this.returnDataConverter.getRank();

		this.parameterDataConverters = new DataConverter[parameterLength];
		this.parameterDataTypes = new DataType[parameterLength];
		this.parameterArrayRanks = new int[parameterLength];
		this.parameterReferencenesses = xfciPlugin.getParameterReferencenesses();

		for (int parameterIndex=0; parameterIndex<parameterLength; parameterIndex++) {

			this.parameterDataConverters[parameterIndex] = new DataConverter(
					parameterClasses[parameterIndex], langSpec
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
	 * @param spec 言語仕様設定
	 * @throws VnanoException
	 * 		引数のデータや型が、この処理系内部では使用できない場合に発生します。
	 */
	public Xfci1ToFunctionAdapter(
			ExternalFunctionConnectorInterface1 xfciPlugin, String nameSpace, LanguageSpecContainer spec)
					throws VnanoException {

		this(xfciPlugin, spec);
		this.hasNameSpace = true;
		this.nameSpace = nameSpace;
	}


	/**
	 * 関数名を取得します。
	 *
	 * @return 関数名
	 */
	@Override
	public final String getFunctionName() {
		return this.xfciPlugin.getFunctionName();
	}


	/**
	 * 所属している名前空間があるかどうかを判定します。
	 *
	 * @return 名前空間に所属していれば true
	 */
	@Override
	public final boolean hasNameSpace() {
		return this.hasNameSpace;
	}


	/**
	 * 所属している名前空間を返します。
	 *
	 * @return 名前空間
	 */
	@Override
	public final String getNameSpace() {
		return this.nameSpace;
	}


	/**
	 * 全ての仮引数の名称を配列として取得します。
	 *
	 * @return 各仮引数の名称を格納する配列
	 */
	@Override
	public final String[] getParameterNames() {
		return this.xfciPlugin.getParameterNames();
	}


	/**
	 * 全ての仮引数のデータ型名を配列として取得します。
	 *
	 * @return 各仮引数のデータ型を格納する配列
	 */
	@Override
	public final String[] getParameterDataTypeNames() {
		int parameterLength = this.parameterDataTypes.length;

		String[] parameterDataTypeNames = new String[parameterLength];
		for (int parameterIndex=0; parameterIndex<parameterLength; parameterIndex++) {
			parameterDataTypeNames[parameterIndex] = DATA_TYPE_NAME.getDataTypeNameOf(
					this.parameterDataConverters[parameterIndex].getDataType()
			);
		}

		return parameterDataTypeNames;
	}


	/**
	 * 全ての仮引数の配列次元数（スカラは0次元として扱う）を配列として取得します。
	 *
	 * @return 各仮引数の配列次元数を格納する配列
	 */
	@Override
	public final int[] getParameterArrayRanks() {
		return this.parameterArrayRanks;
	}


	/**
	 * 全ての仮引数において、データ型が可変であるかどうかを格納する配列を返します。
	 *
	 * @return 各仮引数のデータ型が可変であるかどうかを格納する配列
	 */
	@Override
	public final boolean[] getParameterDataTypeArbitrarinesses() {
		return this.xfciPlugin.getParameterClassArbitrarinesses();
	}


	/**
	 * 全ての仮引数において、配列次元数が可変であるかどうかを格納する配列を返します。
	 *
	 * @return 各仮引数の配列次元数が可変であるかどうかを格納する配列
	 */
	@Override
	public final boolean[] getParameterArrayRankArbitrarinesses() {
		return this.xfciPlugin.getParameterRankArbitrarinesses();
	}


	/**
	 * 全ての仮引数において、参照渡しであるかどうかを格納する配列を返します。
	 *
	 * @return 各仮引数が参照渡しであるかどうかを格納する配列
	 */
	@Override
	public boolean[] getParameterReferencenesses() {
		return this.xfciPlugin.getParameterReferencenesses();
	}


	/**
	 * 全ての仮引数において、定数であるかどうかを格納する配列を返します。
	 *
	 * @return 各仮引数が定数であるかどうかを格納する配列
	 */
	@Override
	public boolean[] getParameterConstantnesses() {
		return this.xfciPlugin.getParameterConstantnesses();
	}


	// hasVariadicParameters との違いは XFCI1 の同名メソッドの説明参照
	/**
	 * 仮引数の個数が任意であるかどうかを返します。
	 *
	 * @return 仮引数の個数が任意であるかどうか
	 */
	@Override
	public final boolean isParameterCountArbitrary() {
		return this.xfciPlugin.isParameterCountArbitrary();
	}


	// isParameterCountArbitrary との違いは XFCI1 の同名メソッドの説明参照
	/**
	 * （未サポート）可変長引数であるかどうかを判定します。
	 *
	 * @return 可変長引数であればtrue
	 */
	@Override
	public final boolean hasVariadicParameters() {
		return this.xfciPlugin.hasVariadicParameters();
	}


	/**
	 * 戻り値のデータ型名を取得します。
	 * 返される型名の表記内に、配列部分 [][]...[] は含まれません。
	 *
	 * @return 戻り値のデータ型名
	 */
	@Override
	public final String getReturnDataTypeName(String[] argumentDataTypeNames, int[] argumentArrayRanks) {
		DataType[] argumentDataTypes;
		try {
			argumentDataTypes = DATA_TYPE_NAME.getDataTypesOf(argumentDataTypeNames);
		} catch (VnanoException e) {
			throw new VnanoFatalException(e);
		}
		Class<?>[] argumentClasses = DataConverter.getExternalClassesOf(argumentDataTypes, argumentArrayRanks);
		Class<?> returnValueClass = this.xfciPlugin.getReturnClass(argumentClasses);
		DataType returnDataType = DataConverter.getDataTypeOf(returnValueClass);
		return DATA_TYPE_NAME.getDataTypeNameOf(returnDataType);
	}


	/**
	 * 戻り値の配列次元数を取得します。
	 *
	 * @return 戻り値の配列次元数
	 */
	@Override
	public final int getReturnArrayRank(String[] argumentDataTypeNames, int[] argumentArrayRanks) {
		DataType[] argumentDataTypes;
		try {
			argumentDataTypes = DATA_TYPE_NAME.getDataTypesOf(argumentDataTypeNames);
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
	public final void invoke(DataContainer<?>[] argumentDataContainers, DataContainer<?> returnDataContainer) {

		int argLength = argumentDataContainers.length;
		Object[] convertedArgs = new Object[argLength];

		// 自動のデータ型変換が有効な場合
		if (this.xfciPlugin.isDataConversionNecessary()) {

			// 引数のデータ型を変換
			for (int argIndex=0; argIndex<argLength; argIndex++) {
				boolean isVoid = false;
				DataConverter converter = null;

				// 引数が任意個数に設定されている場合は、宣言上の仮引数は1個のみなので、0番目の宣言型に変換（仕様）
				if (this.xfciPlugin.isParameterCountArbitrary()) {
					converter = this.parameterDataConverters[0];
					isVoid = this.parameterDataTypes[0].equals(DataType.VOID); // 実用上はあり得ないが宣言上はあり得る

				// 通常の引数の場合
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

			// この場合、プラグインに渡す引数の最初の要素が、戻り値格納用コンテナになる
			DataContainer<?>[] xfciArgContainers = new DataContainer<?>[argLength + 1];
			xfciArgContainers[0] = returnDataContainer;

			// 実引数を格納
			for (int argIndex=0; argIndex<argLength; argIndex++) {

				// 参照渡しの場合はそのまま
				if (this.parameterReferencenesses[argIndex]) {
					xfciArgContainers[argIndex + 1] = argumentDataContainers[argIndex];

				// 値渡しの場合はコピーする
				} else {
					xfciArgContainers[argIndex + 1] = DataConverter.copyDataContainer(argumentDataContainers[argIndex]);
				}
			}

			// プラグインの関数を実行
			try {
				this.xfciPlugin.invoke(xfciArgContainers);
			} catch (ConnectorException e) {
				throw new VnanoFatalException(e);
			}
		}

	}
}
