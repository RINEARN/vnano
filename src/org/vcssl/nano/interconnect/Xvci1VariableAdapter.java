/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import org.vcssl.connect.ExternalVariableConnector1;
import org.vcssl.connect.ExternalVariableException;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.vm.memory.DataContainer;

/**
 * <p>
 * {@link org.vcssl.connect.ExternalVariableConnector1 XVCI 1}
 * 形式の外部変数プラグインを、Vnano処理系内での変数仕様
 * （{@link org.vcssl.nano.interconnect.AbstractVariable AbstractVariable}）
 * に基づく変数オブジェクトへと変換し、
 * {@link Interconnect Interconnect} に接続するためのアダプタです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class Xvci1VariableAdapter extends AbstractVariable {

	/** XVCI準拠の外部変数プラグインです。 */
	private ExternalVariableConnector1 xvciPlugin = null;

	/** 外部変数と処理系内部の変数とで、データの型変換を行うコンバータです。 */
	private DataConverter dataConverter = null;


	/**
	 * 指定されたXVCI準拠の外部変数プラグインを、
	 * 処理系内部での仕様に準拠した変数へと変換するアダプタを生成します。
	 *
	 * @param xvciPlugin XVCI準拠の外部変数プラグイン
	 * @throws VnanoException
	 * 		外部変数のデータや型が、この処理系内部では変数として使用できない場合に発生します。
	 */
	public Xvci1VariableAdapter(ExternalVariableConnector1 xvciPlugin) throws VnanoException {
		this.xvciPlugin = xvciPlugin;
		this.dataConverter = new DataConverter(this.xvciPlugin.getDataClass());
	}


	/**
	 * 変数名を取得します。
	 *
	 * @return 変数名
	 */
	@Override
	public String getVariableName() {
		return this.xvciPlugin.getVariableName();
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
		return DataTypeName.getDataTypeNameOf(this.dataConverter.getDataType());
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
	public DataContainer<?> getDataContainer() {

		// 自動のデータ型変換が有効な場合
		if (this.xvciPlugin.isDataConversionNecessary()) {

			Object data = null;
			try {
				data = this.xvciPlugin.getData();
			} catch (ExternalVariableException e) {
				throw new VnanoFatalException(e);
			}

			try {
				return this.dataConverter.convertToDataContainer(data);
			} catch (VnanoException e) {
				throw new VnanoFatalException(e);
			}

		// 自動のデータ型変換が無効な場合
		} else {
			DataContainer<?> dataContainer = new DataContainer<>();
			try {
				this.xvciPlugin.getData(dataContainer);
				return dataContainer;
			} catch (ExternalVariableException e) {
				throw new VnanoFatalException(e);
			}
		}
	}


	/**
	 * 変数のデータを保持するデータコンテナを設定します。
	 *
	 * @param dataContainer 変数のデータコンテナ
	 */
	@Override
	public void setDataContainer(DataContainer<?> dataContainer) {

		// 自動のデータ型変換が有効な場合
		if (this.xvciPlugin.isDataConversionNecessary()) {

			Object data = null;
			try {
				data = this.dataConverter.convertToExternalObject(dataContainer);
			} catch (VnanoException e) {
				throw new VnanoFatalException(e);
			}

			try {
				this.xvciPlugin.setData(data);
			} catch (ExternalVariableException e) {
				throw new VnanoFatalException(e);
			}

		// 自動のデータ型変換が無効な場合
		}else {
			try {
				this.xvciPlugin.setData(dataContainer);
			} catch (ExternalVariableException e) {
				throw new VnanoFatalException(e);
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
