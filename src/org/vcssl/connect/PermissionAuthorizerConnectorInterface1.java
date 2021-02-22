/*
 * ==================================================
 * Permission Authorizer Connector Interface 1
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2020-2021 by RINEARN (Fumihiro Matsui)
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
	 * この操作は、常にエンジン側から直接呼び出され、スクリプトの処理から呼ばれる事はありません。
	 *
	 * @param permissionMap パーミッション項目の名前と値を格納するマップ（パーミッションマップ）
	 * @param setsToPermanent パーマネント値を設定したい場合は true、一時的な値を設定したい場合は false
	 */
	public abstract void setPermissionMap(Map<String, String> permissionMap, boolean setsToPermanent)
			throws ConnectorException;


	/**
	 * 最新の状態を表す、パーミッション項目の名前と値を格納するマップ（パーミッションマップ）を返します。
	 * この操作は、常にエンジン側から直接呼び出され、スクリプトの処理から呼ばれる事はありません。
	 *
	 * @param getsFromPermanent パーマネント値を取得したい場合は true、一時的な値を取得したい場合は false
	 * @return permissionMap パーミッション項目の名前と値を格納するマップ（パーミッションマップ）
	 */
	public abstract Map<String, String> getPermissionMap(boolean getsFromPermanent)
			throws ConnectorException;


	/**
	 * 指定された名称のパーミッションを要求し、許可されれば何もせず、却下されれば例外を発生させます。
	 * この操作は、スクリプトが呼び出した外部関数のプラグインから呼ばれます。
	 *
	 * @param permissionName パーミッションの名称
	 * @param requester パーミッションの要求元オブジェクト（要求元プラグイン）
	 * @param metaInformation ユーザーに通知するメッセージ内等で用いられるメタ情報
	 * @throws ConnectorException 要求したパーミッションが却下された場合にスローされます。
	 */
	public abstract void requestPermission(String permissionName, Object requester, Object metaInformation)
			throws ConnectorException;


	/**
	 * 指定された名称のパーミッションの状態値を設定します。
	 *
	 * この操作は、エンジンから直接呼ばれる場合と、
	 * スクリプトの処理から（操作用の関数を提供するプラグインを通して）呼ばれる場合の両方が想定されます。
	 *
	 * 後者の場合は、併せて事前に
	 * {@link PermissionAuthorizerConnectorInterface1#requestPermission(String,Object,Object) requestPermission(...)}
	 * メソッドによって、この操作に必要なパーミッションが検査/取得される事を前提とします。
	 * （処理系側において、このメソッドをラップして別プラグインに提供するエンジンコネクタが、そのように実装されます。）
	 * 従ってこのメソッド内では、そのような検査/取得処理は行いません。
	 *
	 * @param permissionName パーミッションの名称
	 * @param value パーミッションの状態値
	 * @param setsToPermanent パーマネント値を設定したい場合は true、一時的な値を設定したい場合は false
	 * @throws ConnectorException この機能がサポートされていない場合にスローされます。
	 */
	public abstract void setPermissionValue(String permissionName, String value, boolean setsToPermanent)
			throws ConnectorException;


	/**
	 * 指定された名称のパーミッションの状態値を取得します。
	 *
	 * この操作は、エンジンから直接呼ばれる場合と、
	 * スクリプトの処理から（操作用の関数を提供するプラグインを通して）呼ばれる場合の両方が想定されます。
	 *
	 * 後者の場合は、併せて事前に
	 * {@link PermissionAuthorizerConnectorInterface1#requestPermission(String,Object,Object) requestPermission(...)}
	 * メソッドによって、この操作に必要なパーミッションが検査/取得される事を前提とします。
	 * （処理系側において、このメソッドをラップして別プラグインに提供するエンジンコネクタが、そのように実装されます。）
	 * 従ってこのメソッド内では、そのような検査/取得処理は行いません。
	 *
	 * @param permissionName パーミッションの名称
	 * @return パーミッションの状態値
	 * @param getsFromPermanent パーマネント値を取得したい場合は true、一時的な値を取得したい場合は false
	 * @throws ConnectorException この機能がサポートされていない場合にスローされます。
	 */
	public abstract String getPermissionValue(String permissionName, boolean getsFromPermanent)
			throws ConnectorException;


	/**
	 * 実行中における一時的なパーミッション状態を保存します。
	 *
	 * この操作は、エンジンから直接呼ばれる場合と、
	 * スクリプトの処理から（操作用の関数を提供するプラグインを通して）呼ばれる場合の両方が想定されます。
	 *
	 * 後者の場合は、併せて事前に
	 * {@link PermissionAuthorizerConnectorInterface1#requestPermission(String,Object,Object) requestPermission(...)}
	 * メソッドによって、この操作に必要なパーミッションが検査/取得される事を前提とします。
	 * （処理系側において、このメソッドをラップして別プラグインに提供するエンジンコネクタが、そのように実装されます。）
	 * 従ってこのメソッド内では、そのような検査/取得処理は行いません。
	 *
	 * @param storesToPermanent パーマネント値として保存する場合に true、一時的に保存する場合は false
	 * @throws ConnectorException この機能がサポートされていない場合にスローされます。
	 */
	public abstract void storeTemporaryPermissionValues(boolean storesToPermanent)
			throws ConnectorException;


	/**
	 * 実行中における一時的なパーミッション状態を復元します。
	 *
	 * この操作は、エンジンから直接呼ばれる場合と、
	 * スクリプトの処理から（操作用の関数を提供するプラグインを通して）呼ばれる場合の両方が想定されます。
	 *
	 * 後者の場合は、併せて事前に
	 * {@link PermissionAuthorizerConnectorInterface1#requestPermission(String,Object,Object) requestPermission(...)}
	 * メソッドによって、この操作に必要なパーミッションが検査/取得される事を前提とします。
	 * （処理系側において、このメソッドをラップして別プラグインに提供するエンジンコネクタが、そのように実装されます。）
	 * 従ってこのメソッド内では、そのような検査/取得処理は行いません。
	 *
	 * @param restoresFromPermanent パーマネント値から復元する場合に true、一時的に store した値からの場合は false
	 * @throws ConnectorException この機能がサポートされていない場合にスローされます。
	 */
	public abstract void restoreTemporaryPermissionValues(boolean restoresFromPermanent)
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
