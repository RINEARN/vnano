/*
 * ==================================================
 * Permission Authorizer Connector Interface 1
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2020 by RINEARN (Fumihiro Matsui)
 * ==================================================
 */

package org.vcssl.connect;

import java.util.Map;

/**
 * <p>
 * PACI 1 (Permission Authorizer Connector Interface 1)
 * 形式のセキュリティプラグインを開発するための、
 * プラグイン側のコネクター・インターフェースです。
 * </p>
 *
 * <p>
 * <span style="font-weight: bold;">
 * ※ このインターフェースは未確定であり、
 * このインターフェースをサポートする処理系が正式にリリースされるまでの間、
 * 一部仕様が変更される可能性があります。
 * </span>
 * </p>
 *
 * <p>
 * PACI 1 形式のセキュリティプラグインは、
 * パーミッションベースのセキュリティシステムを備えるスクリプト処理系において、
 * 他のプラグイン（例えばファイル入出力機能を提供する外部関数プラグイン）などからのパーミッションの要求に応じ、
 * 許可や拒否、またはユーザーに判断を求める役割などを担います。
 * </p>
 *
 * <p>
 * パーミッションの設定/管理方法や、ユーザーに判断を求めるUIなどは、
 * スクリプト処理系を搭載するアプリケーションの種類によって、好ましい形が異なります。
 * そこで、このインターフェースをサポートしている処理系においては、
 * 搭載アプリケーションの開発側が、好ましい形のパーミッション制御層を独自に設計・実装できます。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public interface PermissionAuthorizerConnectorInterface1 {


	/**
	 * パーミッション項目の名前と値を格納するマップ（パーミッションマップ）によって, 各パーミッションの値を設定します。
	 *
	 * @param permissionMap パーミッション項目の名前と値を格納するマップ（パーミッションマップ）
	 */
	public abstract void setPermissionMap(Map<String, String> permissionMap)
			throws ConnectorException;


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


	/**
	 * 処理系への接続時に必要な初期化処理を行います。
	 *
	 * 引数には、スクリプトエンジンに依存するやり取りを行うためのオブジェクトが渡されます。
	 * このオブジェクトは、恐らく {@link EngineConnectorInterface1 EngineConnectorInterface1}
	 * もしくはその後継の、抽象化されたインターフェースでラップされた形で渡されます。
	 *
	 * @param engineConnector エンジンに依存するやり取りを行うためのオブジェクト
	 * @throws ConnectorException 初期化処理に失敗した場合にスローされます。
	 */
	public abstract void initializeForConnection(Object engineConnector) throws ConnectorException;


	/**
	 * 処理系からの接続解除時に必要な終了時処理を行います。
	 *
	 * 引数には、スクリプトエンジンに依存するやり取りを行うためのオブジェクトが渡されます。
	 * このオブジェクトは、恐らく {@link EngineConnectorInterface1 EngineConnectorInterface1}
	 * もしくはその後継の、抽象化されたインターフェースでラップされた形で渡されます。
	 *
	 * @param engineConnector エンジンに依存するやり取りを行うためのオブジェクト
	 * @throws ConnectorException 終了時処理に失敗した場合にスローされます。
	 */
	public abstract void finalizeForDisconnection(Object engineConnector) throws ConnectorException;


	/**
	 * スクリプト実行毎の初期化処理を行います。
	 *
	 * 引数には、スクリプトエンジンに依存するやり取りを行うためのオブジェクトが渡されます。
	 * このオブジェクトは、恐らく {@link EngineConnectorInterface1 EngineConnectorInterface1}
	 * もしくはその後継の、抽象化されたインターフェースでラップされた形で渡されます。
	 *
	 * @param engineConnector エンジンに依存するやり取りを行うためのオブジェクト
	 * @throws ConnectorException 初期化処理に失敗した場合にスローされます。
	 */
	public abstract void initializeForExecution(Object engineConnector) throws ConnectorException;


	/**
	 * スクリプト実行毎の終了時処理を行います。
	 *
	 * 引数には、スクリプトエンジンに依存するやり取りを行うためのオブジェクトが渡されます。
	 * このオブジェクトは、恐らく {@link EngineConnectorInterface1 EngineConnectorInterface1}
	 * もしくはその後継の、抽象化されたインターフェースでラップされた形で渡されます。
	 *
	 * @param engineConnector エンジンに依存するやり取りを行うためのオブジェクト
	 * @throws ConnectorException 終了時処理に失敗した場合にスローされます。
	 */
	public abstract void finalizeForTermination(Object engineConnector) throws ConnectorException;
}
