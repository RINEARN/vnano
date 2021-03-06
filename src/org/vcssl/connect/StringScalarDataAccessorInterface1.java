/*
 * ================================================================
 * Scalar Data Accessor Interface 1 (SDAI 1) for String Type Data
 * ( for VCSSL / Vnano Plug-in Development )
 * ----------------------------------------------------------------
 * This file is released under CC0.
 * Written in 2020-2021 by RINEARN (Fumihiro Matsui)
 * ================================================================
 */

package org.vcssl.connect;

/**
 * <p>
 * 主に処理系のデータコンテナが実装してサポートする、
 * 文字列型用のSDAI 1 (Scalar Data Accessor Interface 1) 形式のデータ入出力インターフェースです。
 * </p>
 */
public interface StringScalarDataAccessorInterface1 {

	/** 動的ロード時などに処理系側から参照される、インターフェースの形式名（値は"STRING_SDAI"）です。*/
	public static final String INTERFACE_TYPE = "STRING_SDAI";

	/** 動的ロード時などに処理系側から参照される、インターフェースの世代名（値は"1"）です。*/
	public static final String INTERFACE_GENERATION = "1";


	/**
	 * 値を設定します。
	 *
	 * @param data 設定値
	 */
	public abstract void setStringScalarData(String data);


	/**
	 * 値を取得します。
	 *
	 * @return 取得値
	 */
	public abstract String getStringScalarData();


	/**
	 * 値を保持しているかどうかを返します。
	 *
	 * @return 保持していれば true
	 */
	public abstract boolean hasStringScalarData();
}
