/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import org.vcssl.connect.ConnectorException;
import org.vcssl.connect.ExternalVariableConnectorInterface1;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.LanguageSpecContainer;
import org.vcssl.nano.vm.memory.DataContainer;

/**
 * <p>
 * 指定された {@link org.vcssl.connect.ExternalVariableConnectorInterface1 XVCI 1}
 * 形式の外部変数プラグインを、Vnano処理系内での変数仕様
 * （{@link org.vcssl.nano.interconnect.AbstractVariable AbstractVariable}）
 * に基づく変数オブジェクトへと変換し、
 * {@link Interconnect Interconnect} に接続するためのアダプタです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class Xvci1ToVariableAdapter extends AbstractVariable {

	/** データ型名が定義された設定オブジェクトを保持します。 */
	private final DataTypeName DATA_TYPE_NAME;

	/** XVCI準拠の外部変数プラグインです。 */
	private ExternalVariableConnectorInterface1 xvciPlugin = null;

	/** 変数名を保持します。 */
	private String variableName = null;

	/** 外部変数と処理系内部の変数とで、データの型変換を行うコンバータです。 */
	private DataConverter dataConverter = null;

	/** 所属している名前空間の名称を保持します。 */
	private String namespaceName = null;


	/**
	 * 指定されたXVCI準拠の外部変数プラグインを、
	 * 処理系内部での仕様に準拠した変数へと変換するアダプタを生成します。
	 *
	 * @param xvciPlugin XVCI準拠の外部変数プラグイン
	 * @param scriptWordSetting スクリプト言語の語句が定義された設定オブジェクト
	 * @param langSpec 言語仕様設定
	 * @throws VnanoException
	 * 		外部変数のデータや型が、この処理系内部では変数として使用できない場合に発生します。
	 */
	public Xvci1ToVariableAdapter(
			ExternalVariableConnectorInterface1 xvciPlugin, LanguageSpecContainer langSpec)
					throws VnanoException {

		this.DATA_TYPE_NAME = langSpec.DATA_TYPE_NAME;
		this.xvciPlugin = xvciPlugin;
		this.variableName = xvciPlugin.getVariableName();
		this.dataConverter = new DataConverter(this.xvciPlugin.getDataClass(), langSpec);

		// 自動データ型変換が無効化されている場合は、やり取りするデータ入出力インターフェースの互換性を検査する
		// ( XVCI1 の getDataUnconvertedClass() が返すインターフェースを、この処理系のデータコンテナが実装していなければエラー ）
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
	 * 変数名を取得します。
	 *
	 * @return 変数名
	 */
	@Override
	public String getVariableName() {
		return this.variableName;
	}


	/**
	 * 変数名を設定（変更）します。
	 *
	 * この機能は、外部変数に、エイリアス（別名）を指定しつつ接続する際に使用されます。
	 *
	 * variableName 変数名
	 */
	@Override
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}


	/**
	 * 所属している名前空間があるかどうかを判定します。
	 *
	 * @return 名前空間に所属していれば true
	 */
	@Override
	public boolean hasNamespaceName() {
		return this.namespaceName != null;
	}


	/**
	 * 所属している名前空間の名称を返します。
	 *
	 * @return 名前空間の名称
	 */
	@Override
	public String getNamespaceName() {
		return this.namespaceName;
	}


	/**
	 * 所属している名前空間の名称を設定します。
	 *
	 * @namespaceName 名前空間の名称
	 */
	@Override
	public void setNamespaceName(String namespaceName) {
		this.namespaceName = namespaceName;
	}


	/**
	 * データ型を取得します。
	 *
	 * @return 変数のデータ型
	 */
	/*
	@Override
	public DataType getDataType() {
		return this.dataConverter.getDataType();
	}
	*/


	/**
	 * データ型の名称を取得します。
	 * 返される型名の表記内に、配列部分 [][]...[] は含まれません。
	 *
	 * @return 変数のデータ型名
	 */
	@Override
	public String getDataTypeName() {
		return DATA_TYPE_NAME.getDataTypeNameOf(this.dataConverter.getDataType());
	}


	/**
	 * 配列次元数（スカラの場合は0次元として扱う）を取得します。
	 *
	 * @return 変数の配列次元数
	 */
	@Override
	public int getRank() {
		return this.dataConverter.getRank();
	}


	/**
	 * 変数のデータを保持するデータコンテナを取得します。
	 *
	 * @return 変数のデータコンテナ
	 */
	@Override
	public DataContainer<?> getDataContainer() throws VnanoException {
		try {

			// 自動のデータ型変換が有効な場合
			if (this.xvciPlugin.isDataConversionNecessary()) {

				Object data = null;
				data = this.xvciPlugin.getData();
				return this.dataConverter.convertToDataContainer(data);

			// 自動のデータ型変換が無効な場合
			} else {
				DataContainer<?> dataContainer = new DataContainer<>();
				this.xvciPlugin.getData(dataContainer);
				return dataContainer;
			}

		// プラグイン側でデータアクセス時に発生した例外は VnanoException でラップする。
		// 検査例外の ConnectorException 以外にも、プラグイン実装側の想定外の例外も発生し得るので、全種の Exception をラップする。
		// ただし Throwable 全体までの範囲はカバーしない。
		// これは、Throwable 全体の範囲には、対処困難な（通常の用途ならもう停止するのが自然な）エラーも含まれるためで、
		// それを catch するかどうかの判断はアプリケーション側に委ねるため。
		} catch (Exception e) {

			// VnanoException のメッセージ内で用いる情報を用意
			String[] errorWords = { this.xvciPlugin.getVariableName(), null };

			// ConnectorException のメッセージは、ユーザーに向けたメッセージなので、VnanoException のメッセージ内にも表示する
			if (e instanceof ConnectorException) {
				errorWords[1] = e.getMessage();
			}

			throw new VnanoException(
				ErrorType.EXTERNAL_VARIABLE_PLUGIN_CRASHED, errorWords, e
			);
		}
	}


	/**
	 * 変数のデータを保持するデータコンテナを設定します。
	 *
	 * @param dataContainer 変数のデータコンテナ
	 */
	@Override
	public void setDataContainer(DataContainer<?> dataContainer) throws VnanoException {

		// 自動のデータ型変換が有効な場合
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

		// 自動のデータ型変換が無効な場合
		}else {
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
	 * 書き換え不可能な定数であるかどうかを返します。
	 *
	 * @return 定数ならtrue
	 */
	@Override
	public boolean isConstant() {
		return this.xvciPlugin.isConstant();
	}


	/**
	 * 同じ変数名の変数を区別するためのシリアルナンバーを保持しているかどうかを判定します。
	 *
	 * @return 保持していれば true
	 */
	@Override
	public boolean hasSerialNumber() {
		return false;
	}


	/**
	 * 同じ変数名の変数を区別するためのシリアルナンバーを返します。
	 *
	 * @return シリアルナンバー
	 */
	@Override
	public int getSerialNumber() {
		return -1;
	}
}
