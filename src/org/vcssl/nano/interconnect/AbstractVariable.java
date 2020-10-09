/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.vm.memory.DataContainer;


/**
 * <p>
 * Vnano処理系内部における変数の抽象クラスです。
 * </p>
 *
 * <p>
 * 各種の外部変数プラグイン・インターフェースも、
 * 最終的にこの抽象クラスを継承したアダプタクラスによってラップされて扱われます
 * （{@link Xvci1ToVariableAdapter Xvci1ToVariableAdapter} などを参照）。
 * </p>
 *
 * <p>
 * この抽象クラスの機能を、内部変数用に素直に実装したクラスとしては、
 * {@link InternalVariable InternalVariable} が存在します。
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
	 * 変数名を設定（変更）します。
	 *
	 * この機能は、外部変数などの接続時に、エイリアスを指定するために使用されます。
	 * 内部変数など、変数名を変更できない変数も存在し、その場合はこのメソッドは使用できません。
	 * そのような対象にこのメソッドが使用された場合、それはスクリプトの内容依存ではなく処理系実装上の問題であるため、
	 * VnanoFatalException が発生します。
	 *
	 * @param variableName 変数名
	 * @throws VnanoFatalException
	 * 		名称を変更できない変数（内部変数など）に対して使用された場合にスローされます。
	 */
	public abstract void setVariableName(String variableName);


	/**
	 * 変数名を取得します。
	 *
	 * @return 変数名
	 */
	public abstract String getVariableName();


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
	 * データ型の名称を取得します。
	 * 返される型名の表記内に、配列部分 [][]...[] は含まれません。
	 *
	 * @return この変数のデータ型名
	 */
	public abstract String getDataTypeName();


	/**
	 * この変数のデータを保持するデータコンテナを取得します。
	 *
	 * @return この変数のデータコンテナ
	 */
	public abstract DataContainer<?> getDataContainer() throws VnanoException;


	/**
	 * この変数のデータを保持するデータコンテナを設定します。
	 *
	 * @param dataContainer この変数のデータコンテナ
	 */
	public abstract void setDataContainer(DataContainer<?> dataContainer) throws VnanoException;


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

