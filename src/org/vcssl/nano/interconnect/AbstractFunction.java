/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import org.vcssl.nano.vm.memory.DataContainer;


/**
 * <p>
 * Vnano処理系内部における関数の抽象クラスです。
 * </p>
 *
 * <p>
 * 各種の外部関数プラグイン・インターフェースも、
 * 最終的にこの抽象クラスを継承したアダプタクラスによってラップされて扱われます
 * （{@link Xfci1FunctionAdapter Xfci1FunctionAdapter} などを参照）。
 * </p>
 *
 * <p>
 * この抽象クラスの機能を、内部関数用に素直に実装したクラスとしては、
 * {@link Function Function} が存在します。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public abstract class AbstractFunction {


	/**
	 * この抽象クラスを継承するサブクラスのコンストラクタ実装のための、
	 * 空のコンストラクタです。
	 */
	protected AbstractFunction() {}


	/**
	 * 関数名を取得します。
	 *
	 * @return 関数名
	 */
	public abstract String getFunctionName();


	/**
	 * 全ての仮引数の名称を配列として取得します。
	 *
	 * @return 全ての仮引数の名称を格納する配列
	 */
	public abstract String[] getParameterNames();


	/**
	 * 全ての仮引数のデータ型を配列として取得します。
	 *
	 * @return 全ての仮引数のデータ型を格納する配列
	 * @throws VnanoException
	 * 		このインスタンスが保持するデータ型名から、
	 * 		この処理系でサポートされているデータ型に変換できなかった場合にスローされます。
	 */
	//public abstract DataType[] getParameterDataTypes() throws VnanoException;


	/**
	 * 全ての仮引数のデータ型名を配列として取得します。
	 * 返される型名の表記内に、配列部分 [][]...[] は含まれません。
	 *
	 * @return 仮引数のデータ型名を格納する配列
	 */
	public abstract String[] getParameterDataTypeNames();

	/**
	 * 全ての仮引数の配列次元数（スカラは0次元として扱う）を配列として取得します。
	 *
	 * @return 全ての仮引数の配列次元数を格納する配列
	 */
	public abstract int[] getParameterArrayRanks();


	/**
	 * 可変長引数であるかどうかを判定します。
	 *
	 * @return 可変長引数であればtrue
	 */
	public abstract boolean isVariadic();


	/**
	 * 戻り値のデータ型を取得します。
	 *
	 * @return 戻り値のデータ型
	 * @throws VnanoException
	 * 		このインスタンスが保持するデータ型名から、
	 * 		この処理系でサポートされているデータ型に変換できなかった場合にスローされます。
	 */
	//public abstract DataType getReturnDataType() throws VnanoException;


	/**
	 * 戻り値のデータ型名を取得します。
	 * 返される型名の表記内に、配列部分 [][]...[] は含まれません。
	 *
	 * @return 戻り値のデータ型名
	 */
	public abstract String getReturnDataTypeName();


	/**
	 * 戻り値の配列次元数を取得します。
	 *
	 * @return 戻り値の配列次元数
	 */
	public abstract int getReturnArrayRank();


	// このメソッドは例外を投げる必要あり。XfciFunctionConector#invoke参照
	/**
	 * 関数を実行します。
	 *
	 * @param argumentDataUnits 実引数のデータを保持するデータユニットの配列（各要素が個々の実引数に対応）
	 * @param returnDataUnit 戻り値のデータを格納するデータユニット
	 */
	public abstract void invoke(DataContainer<?>[] argumentDataUnits, DataContainer<?> returnDataUnit);

}