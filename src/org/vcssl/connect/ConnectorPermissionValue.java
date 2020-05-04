/*
 * ==================================================
 * Connector Parmission
 * ( for VCSSL / Vnano Plug-in Development )
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2020 by RINEARN (Fumihiro Matsui)
 * ==================================================
 */

package org.vcssl.connect;

/**
 * <p>
 * パーミッション設定ベースのセキュリティレイヤーを持つ処理系のために、
 * 列挙的にパーミッション値が定義されたクラスです。
 * </p>
 *
 * <p>
 * このクラスがフィールドとして提供する各パーミッション値は、
 * 本来は列挙子の要素として定義されるのが素直であり、
 * その方が処理系側の検査オーバーヘッドも軽減が見込めますが、
 * 定義順序変更時のプラグイン側の再コンパイルを不要にするためや、
 * その他の互換性を考慮して、
 * public static final な文字列フィールドとして定義されています。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class ConnectorPermissionValue {

	/**
	 * <span class="jang-en">
	 * Requests for permission items having this value will always be allowed
	 * </span>
	 * <span class="jang-ja">
	 * この値を持つパーミッション項目は, リクエストが常に許可されます
	 * </span>
	 * .
	 */
	public static final String ALLOW = "ALLOW";

	/**
	 * <span class="jang-en">
	 * Requests for permission items having this value will always be denied
	 * </span>
	 * <span class="jang-ja">
	 * この値を持つパーミッション項目は, リクエストが常に拒否されます
	 * </span>
	 * .
	 */
	public static final String DENY = "DENY";

	/**
	 * <span class="jang-en">
	 * When permission items having this value are requested,
	 * the script engine asks the user whether allows it or not
	 * </span>
	 * <span class="jang-ja">
	 * この値を持つパーミッション項目においては, スクリプトエンジンがユーザーに対して,
	 * リクエストを許可するかどうかを尋ねた上で決定します
	 * </span>
	 * .
	 */
	public static final String ASK = "ASK";
}
