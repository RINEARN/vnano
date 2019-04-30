/*
 * Copyright(C) 2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import org.vcssl.nano.vm.memory.DataContainer;


/**
 * <p>
 * 変数を、別名の変数としてラップするためのアダプタークラスです。
 * 主に、{@link Interconnect Interconnect} 内で外部変数プラグインを接続する際に、
 * スクリプト内からアクセスするための識別子を変更可能にするために使用されます。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class VariableAliasAdapter extends AbstractVariable {

	/** このアダプタで変換する変数を保持します。 */
	private AbstractVariable variable = null;

	/** 変数名を保持します。 */
	private String variableName = null;


	/**
	 * 指定された変数を、別名に変換するためのアダプターを生成します。
	 *
	 * @param variable 変換対象の変数
	 */
	public VariableAliasAdapter(AbstractVariable variable) {
		this.variable = variable;
		this.variableName = this.variable.getVariableName();
	}


	/**
	 * 変数名を取得します。
	 *
	 * @return 変数名
	 */
	public String getVariableName() {
		return this.variableName;
	}


	/**
	 * 変数名を上書き変更します。
	 *
	 * @param variableName 変数名
	 */
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}


	/**
	 * 所属している名前空間があるかどうかを判定します。
	 *
	 * @return 名前空間に所属していれば true
	 */
	public boolean hasNameSpace() {
		return this.variable.hasNameSpace();
	}


	/**
	 * 所属している名前空間を返します。
	 *
	 * @return 名前空間
	 */
	public String getNameSpace() {
		return this.variable.getNameSpace();
	}


	/**
	 * データ型の名称を取得します。
	 * 返される型名の表記内に、配列部分 [][]...[] は含まれません。
	 *
	 * @return この変数のデータ型名
	 */
	public String getDataTypeName() {
		return this.variable.getDataTypeName();
	}


	/**
	 * この変数のデータを保持するデータコンテナを取得します。
	 *
	 * @return この変数のデータコンテナ
	 */
	public DataContainer<?> getDataContainer() {
		return this.variable.getDataContainer();
	}


	/**
	 * この変数のデータを保持するデータコンテナを設定します。
	 *
	 * @param dataContainer この変数のデータコンテナ
	 */
	public void setDataContainer(DataContainer<?> dataContainer) {
		this.variable.setDataContainer(dataContainer);
	}


	/**
	 * 配列次元数（スカラは0次元として扱う）を返します。
	 *
	 * @return この変数の配列次元数
	 */
	public int getRank() {
		return this.variable.getRank();
	}


	/**
	 * 書き換え不可能な定数であるかどうかを返します。
	 *
	 * @return 定数ならtrue
	 */
	public boolean isConstant() {
		return this.variable.isConstant();
	}


	/**
	 * 同じ変数名の変数を区別するためのシリアルナンバーを保持しているかどうかを判定します。
	 *
	 * @return 保持していれば true
	 */
	public boolean hasSerialNumber() {
		return this.variable.hasSerialNumber();
	}


	/**
	 * 同じ変数名の変数を区別するためのシリアルナンバーを返します。
	 *
	 * @return シリアルナンバー
	 */
	public int getSerialNumber() {
		return this.variable.getSerialNumber();
	}
}
