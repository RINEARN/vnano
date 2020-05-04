/*
 * ==================================================
 * Engine Connector Interface 1
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2018-2020 by RINEARN (Fumihiro Matsui)
 * ==================================================
 */

package org.vcssl.connect;

/**
 * <p>
 * このインターフェースは、
 * プラグイン側から処理系（スクリプトエンジン）側の機能を呼び出せるようにするために、
 * 処理系をラップしてプラグインに渡すために使用されます。
 * </p>
 *
 * <p>
 * 従って、このインターフェースの実装は、処理系側で行われます。
 * 処理系用のプラグインを開発する側が、
 * このインターフェースの実装クラスを作成する必要はありません。
 * プラグイン側では、処理系から渡された実装クラスのメソッドを必要応じて呼び出して、
 * 処理系側から情報を取得したり、その他の必要な処理を実行したりします。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public interface EngineConnectorInterface1 {


	/**
	 * 指定された名称のオプションの値を保持しているかどうかを判定します。
	 *
	 * @param optionName オプションの名称
	 * @return 判定結果（保持していれば true）
	 */
	public abstract boolean hasOptionValue(String optionName);


	/**
	 * 指定された名称のオプションの値を取得します。
	 *
	 * @param optionName オプションの名称
	 * @return オプションの値
	 */
	public abstract Object getOptionValue(String optionName);


	/**
	 * 指定された名称のパーミッションを要求します。
	 *
	 * @param permissionName パーミッションの名称
	 * @param requester パーミッションの要求元プラグイン
	 * @param metaInformation ユーザーに通知するメッセージ内等で用いられるメタ情報
	 * @throws 要求したパーミッションが却下された場合にスローされます。
	 */
	public abstract void requestPermission(String permissionName, Object requester, Object metaInformation)
			throws ConnectorException;
}
