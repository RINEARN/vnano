/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.vm.memory.DataContainer;


/**
 * <p>
 * Vnano処理系内部における関数の抽象クラスです。
 * </p>
 *
 * <p>
 * 各種の外部関数プラグイン・インターフェースも、
 * 最終的にこの抽象クラスを継承したアダプタクラスによってラップされて扱われます
 * （{@link Xfci1ToFunctionAdapter Xfci1ToFunctionAdapter} などを参照）。
 * </p>
 *
 * <p>
 * この抽象クラスの機能を、内部関数用に素直に実装したクラスとしては、
 * {@link InternalFunction InternalFunction} が存在します。
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
	 * 関数名を設定（変更）します。
	 *
	 * この機能は、外部関数などの接続時に、エイリアスを指定するために使用されます。
	 * 内部関数など、関数名を変更できない関数も存在し、その場合はこのメソッドは使用できません。
	 * そのような対象にこのメソッドが使用された場合、それはスクリプトの内容依存ではなく処理系実装上の問題であるため、
	 * VnanoFatalException が発生します。
	 *
	 * @param functionName 関数名
	 * @throws VnanoFatalException
	 * 		名称を変更できない関数（内部関数など）に対して使用された場合にスローされます。
	 */
	public abstract void setFunctionName(String functionName);


	/**
	 * 関数名を取得します。
	 *
	 * @return 関数名
	 */
	public abstract String getFunctionName();


	/**
	 * 所属している名前空間があるかどうかを判定します。
	 *
	 * @return 名前空間に所属していれば true
	 */
	public abstract boolean hasNamespaceName();


	/**
	 * 所属している名前空間の名称を返します。
	 *
	 * @return 名前空間の名称
	 */
	public abstract String getNamespaceName();


	/**
	 * 所属している名前空間の名称を設定します。
	 *
	 * @namespaceName 名前空間の名称
	 */
	public abstract void setNamespaceName(String namespaceName);


	/**
	 * 全ての仮引数の名称を配列として取得します。
	 *
	 * @return 各仮引数の名称を格納する配列
	 */
	public abstract String[] getParameterNames();


	/**
	 * 全ての仮引数のデータ型名を配列として取得します。
	 * 返される型名の表記内に、配列部分 [][]...[] は含まれません。
	 *
	 * @return 各仮引数のデータ型名を格納する配列
	 */
	public abstract String[] getParameterDataTypeNames();


	/**
	 * 全ての仮引数の配列次元数（スカラは0次元として扱う）を配列として取得します。
	 *
	 * @return 各仮引数の配列次元数を格納する配列
	 */
	public abstract int[] getParameterArrayRanks();


	/**
	 * 全ての仮引数において、データ型が可変であるかどうかを格納する配列を返します。
	 *
	 * @return 各仮引数のデータ型が可変であるかどうかを格納する配列
	 */
	public abstract boolean[] getParameterDataTypeArbitrarinesses();


	/**
	 * 全ての仮引数において、配列次元数が可変であるかどうかを格納する配列を返します。
	 *
	 * @return 各仮引数の配列次元数が可変であるかどうかを格納する配列
	 */
	public abstract boolean[] getParameterArrayRankArbitrarinesses();


	/**
	 * 全ての仮引数において、参照渡しであるかどうかを格納する配列を返します。
	 *
	 * @return 各仮引数が参照渡しであるかどうかを格納する配列
	 */
	public abstract boolean[] getParameterReferencenesses();


	/**
	 * 全ての仮引数において、定数であるかどうかを格納する配列を返します。
	 *
	 * @return 各仮引数が定数であるかどうかを格納する配列
	 */
	public abstract boolean[] getParameterConstantnesses();


	/**
	 * 仮引数の個数が任意であるかどうかを返します。
	 *
	 * @return 仮引数の個数が任意であるかどうか
	 */
	public abstract boolean isParameterCountArbitrary();


	/**
	 * （未サポート）可変長引数であるかどうかを判定します。
	 *
	 * @return 可変長引数であればtrue
	 */
	public abstract boolean hasVariadicParameters();


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
	 * {@link AbstractFunction#isReturnDataTypeArbitrary() isReturnDataTypeArbitrary()}
	 * メソッドが true を返す場合、
	 * このメソッドの argumentDataTypeNames および argumentArrayRanks には、
	 * スクリプト内での呼び出しにおける、実引数の型/次元情報が渡されます。
	 *
	 * 逆に、
	 * {@link AbstractFunction#isReturnDataTypeArbitrary() isReturnDataTypeArbitrary()}
	 * メソッドが false を返す場合、
	 * このメソッドの戻り値は、argumentDataTypeNames や argumentArrayRanks
	 * の内容によらず確定している必要があります。
	 * 従ってその場合には、それらの情報は不要であるため、有効な内容が渡される事は保証されず、
	 * 参照してはいけません。
	 *
	 * @param argumentDataTypeNames 呼び出し時の全引数の型名を格納する配列
	 * @param argumentArrayRanks 呼び出し時の全引数の配列次元数を格納する配列
	 * @return 戻り値のデータ型名
	 */
	public abstract String getReturnDataTypeName(String[] argumentDataTypeNames, int[] argumentArrayRanks);


	/**
	 * 戻り値の配列次元数を取得します。
	 *
	 * {@link AbstractFunction#isReturnArrayRankArbitrary() isReturnArrayRankArbitrary()}
	 * メソッドが true を返す場合、
	 * このメソッドの argumentDataTypeNames および argumentArrayRanks には、
	 * スクリプト内での呼び出しにおける、実引数の型/次元情報が渡されます。
	 *
	 * 逆に、
	 * {@link AbstractFunction#isReturnArrayRankArbitrary() isReturnArrayRankArbitrary()}
	 * メソッドが false を返す場合、
	 * このメソッドの戻り値は、argumentDataTypeNames や argumentArrayRanks
	 * の内容によらず確定している必要があります。
	 * 従ってその場合には、それらの情報は不要であるため、有効な内容が渡される事は保証されず、
	 * 参照してはいけません。
	 *
	 * @param argumentDataTypeNames 呼び出し時の全引数の型名を格納する配列
	 * @param argumentArrayRanks 呼び出し時の全引数の配列次元数を格納する配列
	 * @return 戻り値の配列次元数
	 */
	public abstract int getReturnArrayRank(String[] argumentDataTypeNames, int[] argumentArrayRanks);


	/**
	 * 戻り値のデータ型が可変であるかどうかを取得します。
	 *
	 * ただし、このメソッドが true を返す場合でも、
	 * 渡される実引数のデータ型や次元数が確定すると、それに応じて戻り値の型も確定する必要があります。
	 * 詳細は
	 * {@link AbstractFunction#getReturnDataTypeName(String[],int[]) getReturnDataTypeName(String[],int[])}
	 * メソッドの説明を参照してください。
	 */
	public abstract boolean isReturnDataTypeArbitrary();


	/**
	 * 戻り値の配列次元数が可変であるかどうかを取得します。
	 *
	 * ただし、このメソッドが true を返す場合でも、
	 * 渡される実引数のデータ型や次元数が確定すると、それに応じて戻り値の次元数も確定する必要があります。
	 * 詳細は
	 * {@link AbstractFunction#getReturnArrayRank(String[],int[]) getReturnArrayRank(String[],int[])}
	 * メソッドの説明を参照してください。
	 */
	public abstract boolean isReturnArrayRankArbitrary();


	/**
	 * 指定された引数で関数を実行する際に発生し得る、事前に検査可能な問題などを検査し、
	 * 問題があった場合には例外を発生させます。
	 *
	 * 例えば、外部関数プラグインが想定しているデータ入出力インターフェースと、
	 * この処理系のデータコンテナが実装しているデータ入出力インターフェースが異なる場合、
	 * そのままでは実行時にエラーが発生してしまいますが、それらは事前に検査可能です。
	 *
	 * なお、問題が検出されなかった場合には、このメソッドはただ処理を返すだけで、何も起こりません。
	 *
	 * @param argumentDataTypeNames 呼び出し時の全引数の型名を格納する配列
	 * @param argumentArrayRanks 呼び出し時の全引数の配列次元数を格納する配列
	 * @throws VnanoException
	 * 		上記の説明を参照してください。
	 */
	public abstract void checkInvokability(String[] argumentDataTypeNames, int[] argumentArrayRanks) throws VnanoException;


	/**
	 * 関数を実行します。
	 *
	 * @param argumentDataUnits 実引数のデータを保持するデータユニットの配列（各要素が個々の実引数に対応）
	 * @param returnDataUnit 戻り値のデータを格納するデータユニット
	 */
	public abstract void invoke(DataContainer<?>[] argumentDataUnits, DataContainer<?> returnDataUnit) throws VnanoException;

}
