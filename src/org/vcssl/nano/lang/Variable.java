/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.lang;

import org.vcssl.nano.memory.DataContainer;

/**
 * <p>
 * Vnano処理系内部における変数の実装クラスです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class Variable extends AbstractVariable {

	/** 変数名を保持します。 */
	private String variableName = null;

	/** データ型を保持します。 */
	private DataType dataType = null;

	/** 配列次元数（スカラは0次元として扱う）を保持します。 */
	private int rank = -1;

	/** この変数のデータを保持する、データユニットを保持します。 */
	private DataContainer<?> dataContainer;


	/**
	 * 変数名とデータ型、および配列次元数を指定して、変数を生成します。
	 * ただし、変数のデータを保持するデータユニットは、自動では生成されないため、
	 * 外部で生成したものを {@link Variable#setDataUnit dataUnit}
	 * メソッドで設定する必要がああります。
	 *
	 * @param variableName 変数名
	 * @param dataType データ型
	 * @param rank 恥列次元数（スカラは0次元として扱う）
	 */
	public Variable(String variableName, DataType dataType, int rank) {
		this.variableName = variableName;
		this.dataType = dataType;
		this.rank = rank;
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
	 * データ型を取得します。
	 *
	 * @return データ型
	 */
	@Override
	public DataType getDataType() {
		return this.dataType;
	}


	/**
	 * この変数のデータを保持するデータコンテナを取得します。
	 *
	 * @return データコンテナ
	 */
	@Override
	public DataContainer<?> getDataContainer() {
		return this.dataContainer;
	}

	/**
	 * この変数のデータを保持するデータコンテナを設定します。
	 *
	 * @param dataContainer データコンテナ
	 */
	@Override
	public void setDataContainer(DataContainer<?> dataContainer) {
		this.dataContainer = dataContainer;
	}


	/**
	 * 配列次元数（スカラは0次元として扱う）を取得します。
	 *
	 * @return 配列次元数
	 */
	@Override
	public int getRank() {
		return this.rank;
	}


	/**
	 * 書き換え不可能な定数であるかどうかを返します。
	 *
	 * @return 定数ならtrue
	 */
	public boolean isConstant() {
		return false;
	}
}
