/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import org.vcssl.nano.vm.memory.DataContainer;


/**
 * <p>
 * Vnano処理系内部における変数の抽象クラスです。
 * </p>
 *
 * <p>
 * 外部変数プラグインが提供する関数は、
 * 処理系内部ではこの抽象クラスのサブクラスとして扱われます。
 * 各種の外部変数プラグイン・インターフェースも、
 * 最終的にこの抽象クラスを継承したアダプタクラスによってラップされて扱われます
 * （{@link Xvci1VariableAdapter Xvci1VariableAdapter} などを参照）。
 * </p>
 *
 * <p>
 * この抽象クラスの機能を、内部変数用に素直に実装したクラスとしては、
 * {@link Variable Variable} が存在します。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public abstract class AbstractVariable {


	/**
	 * この抽象クラスを継承するサブクラスのコンストラクタ実装のための、
	 * 空のコンストラクタです。
	 */
	protected AbstractVariable(){}


	/**
	 * 変数名を取得します。
	 *
	 * @return 変数名
	 */
	public abstract String getVariableName();


	// -> これ、String の型名にしたほうがいいかも。抽象性からして。あとInvalidDataTypeException出まくる
	// -> いや、外部公開インターフェースじゃないから別にいい
	/**
	 * データ型を取得します。
	 *
	 * @return この変数のデータ型
	 * @throws VnanoException
	 * 		このインスタンスが保持するデータ型名から、
	 * 		この処理系でサポートされているデータ型に変換できなかった場合にスローされます。
	 */
	//public abstract DataType getDataType() throws VnanoException;


	/**
	 * データ型の名称を取得します。
	 * 返される型名の表記内に、配列部分 [][]...[] は含まれません。
	 *
	 * @return この変数のデータ型名
	 */
	public abstract String getDataTypeName();


	// これ例外を色々と投げる必要がある。XvciFunctionConector#getData参照
	/**
	 * この変数のデータを保持するデータコンテナを取得します。
	 *
	 * @return この変数のデータコンテナ
	 */
	public abstract DataContainer<?> getDataContainer();


	// このメソッドは例外を投げる必要あり。XvciFunctionConector#setData参照
	/**
	 * この変数のデータを保持するデータコンテナを設定します。
	 *
	 * @param dataContainer この変数のデータコンテナ
	 */
	public abstract void setDataContainer(DataContainer<?> dataContainer);


	/**
	 * 配列次元数（スカラは0次元として扱う）を返します。
	 *
	 * @return この変数の配列次元数
	 */
	public abstract int getRank();


	/**
	 * 書き換え不可能な定数であるかどうかを返します。
	 *
	 * @return 定数ならtrue
	 */
	public abstract boolean isConstant();


	/**
	 * 同じ変数名の変数を区別するためのシリアルナンバーを保持しているかどうかを判定します。
	 *
	 * @return 保持していれば true
	 */
	public abstract boolean hasSerialNumber();


	/**
	 * 同じ変数名の変数を区別するためのシリアルナンバーを返します。
	 *
	 * @return シリアルナンバー
	 */
	public abstract int getSerialNumber();
}

