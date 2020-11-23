/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;

/**
 * <p>
 * Vnano処理系内部における変数の実装クラスです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class InternalVariable extends AbstractVariable {

	/** 変数名を保持します。 */
	private String variableName = null;

	// コンパイルの段階では、なるべくデータ型をサポート範囲内には絞らず拡張性を持たせるため、DataType列挙子ではなくデータ型名を保持する
	/** データ型名を保持します。 */
	private String dataTypeName = null;

	/** 配列次元数（スカラは0次元として扱う）を保持します。 */
	private int rank = -1;

	/** 書き換え不可能な定数であるかどうかを保持します。 */
	private boolean constantness = false;

	/** この変数のデータを保持する、データユニットを保持します。 */
	private DataContainer<?> dataContainer;

	/** 変数名が競合している変数を区別するためのシリアルナンバーを保持します。 */
	private int serialNumber = -1;

	/** 変数名が競合している変数を区別するためのシリアルナンバーを保持しているかどうかを表すフラグです。 */
	private boolean hasSerialNumber = false;


	/**
	 * 変数名とデータ型、および配列次元数を指定して、変数を生成します。
	 * ただし、変数のデータを保持するデータユニットは、自動では生成されないため、
	 * 外部で生成したものを {@link InternalVariable#setDataUnit dataUnit}
	 * メソッドで設定する必要がああります。
	 *
	 * @param variableName 変数名
	 * @param dataTypeName データ型名（配列部分 [][]...[] は含まない）
	 * @param rank 配列次元数（スカラは0次元として扱う）
	 * @param constantness 定数かどうか
	 */
	public InternalVariable(String variableName, String dataTypeName, int rank, boolean constantness) {
		this.variableName = variableName;
		this.dataTypeName = dataTypeName;
		this.rank = rank;
		this.constantness = constantness;
	}

	/**
	 * 変数名とデータ型、配列次元数、およびシリアルナンバーを指定して、変数を生成します。
	 * ただし、変数のデータを保持するデータユニットは、自動では生成されないため、
	 * 外部で生成したものを {@link InternalVariable#setDataUnit dataUnit}
	 * メソッドで設定する必要がああります。
	 *
	 * @param variableName 変数名
	 * @param dataTypeName データ型名（配列部分 [][]...[] は含まない）
	 * @param rank 配列次元数（スカラは0次元として扱う）
	 * @param constantness 定数かどうか
	 * @param int serialNumber 変数名が競合している変数を区別するためのシリアルナンバー
	 */
	public InternalVariable(String variableName, String dataTypeName, int rank, boolean isConstant, int serialNumber) {
		this(variableName, dataTypeName, rank, isConstant);
		this.serialNumber = serialNumber;
		this.hasSerialNumber = true;
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
	 * このメソッドは使用できません。
	 *
	 * このメソッドは、外部変数などの接続時に、エイリアスを指定するための機能として AbstractVariable に宣言されています。
	 * 一方、現在のVnanoでは、内部変数にエイリアスを付ける言語機能はサポートしていないため、
	 * もしも内部変数に対してこのメソッドが呼ばれた場合、それは実装上のミスによるものと考えられます。
	 * 従って、このメソッドは呼ばれると VnanoFatalException を発生させます。
	 *
	 * @param variableName 変数名
	 */
	@Override
	public void setVariableName(String variableName) {
		throw new VnanoFatalException("Names of internal variables should not be changed.");
	}


	/**
	 * 所属している名前空間があるかどうかを判定します。
	 *
	 * @return 名前空間に所属していれば true
	 */
	@Override
	public boolean hasNamespaceName() {
		return false; // スクリプト内での名前空間定義はサポートされていない
	}

	@Override
	public final String getNamespaceName() {
		throw new VnanoFatalException("Internal variables can not belongs to any namespaces.");
	}

	@Override
	public final void setNamespaceName(String namespaceName) {
		throw new VnanoFatalException("Internal variables can not belongs to any namespaces.");
	}


	/**
	 * データ型を取得します。
	 *
	 * @return データ型
	 * @throws VnanoException
	 * 		このインスタンスが保持するデータ型名から、
	 * 		この処理系でサポートされているデータ型に変換できなかった場合にスローされます。
	 */
	/*
	@Override
	public DataType getDataType() throws VnanoException {
		//return this.dataType;
		return DataTypeName.getDataTypeOf(this.dataTypeName);
	}
	*/


	/**
	 * データ型の名称を取得します。
	 *
	 * @return データ型の名称
	 */
	@Override
	public String getDataTypeName() {
		return this.dataTypeName;
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
		return this.constantness;
	}


	/**
	 * 同じ変数名の変数を区別するためのシリアルナンバーを保持しているかどうかを判定します。
	 *
	 * @return 保持していれば true
	 */
	public boolean hasSerialNumber() {
		return this.hasSerialNumber;
	}


	/**
	 * 同じ変数名の変数を区別するためのシリアルナンバーを返します。
	 *
	 * @return シリアルナンバー
	 */
	public int getSerialNumber() {
		return this.serialNumber;
	}
}
