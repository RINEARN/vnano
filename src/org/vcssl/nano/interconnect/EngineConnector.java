/*
 * Copyright(C) 2019-2021 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import java.util.Locale;
import java.util.Map;

import org.vcssl.connect.ConnectorException;
import org.vcssl.connect.EngineConnectorInterface1;
import org.vcssl.connect.PermissionAuthorizerConnectorInterface1;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.spec.ErrorMessage;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.OptionKey;

//Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/EngineConnector.html
//ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/EngineConnector.html

/**
 * <span class="lang-en">
 * The connector class to access the script engine of the Vnano from plug-ins
 * </span>
 * <span class="lang-ja">
 * プラグイン等からVnanoのスクリプトエンジンにアクセスするためのコネクタークラスです
 * </span>
 * .
 *
 * <p>
 * &raquo; <a href="../../../../../src/org/vcssl/nano/interconnect/EngineConnector.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/interconnect/EngineConnector.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/interconnect/EngineConnector.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public final class EngineConnector implements EngineConnectorInterface1 {

	// プラグイン側からフィールドを差し替えられると、想定外の問題の原因になったりデバッグの難しい挙動を招き得るので、
	// フィールドの setter は設けず、差し替えたい場合は create～Instance 系メソッドに渡して別インスタンスを再生成する方針
	// (リフレクション経由での変更可能性については、必要に応じてホストアプリケーション側で実行環境設定等で対応)

	private final Map<String, Object> optionMap;
	private final Map<String, String> permissionMap;
	private final PermissionAuthorizerConnectorInterface1 permissionAuthorizer;


	public EngineConnector() {
		this.optionMap = null;
		this.permissionMap = null;
		this.permissionAuthorizer = null;
	}

	// このコンストラクタは参照を控えるのみで、
	// それらの間の関連性の更新などは行わない（例外を投げる場合があるため）ので、
	// 別途 reflectPermissionSettings() 等を呼ぶ必要がある。
	// create～Instance 系メソッドはそのような必要な処理も済ませたインスタンスを返す。
	private EngineConnector(
			Map<String, Object> optionMap, Map<String, String> permissionMap,
			PermissionAuthorizerConnectorInterface1 permissionAuthorizer) {

		this.optionMap = optionMap;
		this.permissionMap = permissionMap;
		this.permissionAuthorizer = permissionAuthorizer;
	}

	// permissionMap の設定内容を permissionAuthorizer に反映させる
	// (例外を投げる可能性があるので、コンストラクタから別メソッドに切り分けている / コンストラクタの説明参照)
	private final void reflectPermissionSettings() throws VnanoException {
		if (this.permissionAuthorizer != null) {
			try {
				this.permissionAuthorizer.setPermissionMap(permissionMap, true);
			} catch (ConnectorException e) {
				throw new VnanoException(
					ErrorType.PERMISSION_AUTHORIZER_PLUGIN_CRASHED,
					new String[] { this.permissionAuthorizer.getClass().getCanonicalName(), e.getMessage() }, e
				);
			}
		}
	}


	// オプションマップを差し替えたインスタンスを生成して返す
	public final EngineConnector createOptionMapUpdatedInstance(Map<String, Object> updatedOptionMap) {
		return new EngineConnector(updatedOptionMap, this.permissionMap, this.permissionAuthorizer);
	}


	// パーミッションマップを差し替えたインスタンスを生成して返す
	public final EngineConnector createPermissionMapUpdatedInstance(Map<String, String> updatedPermissionMap)
			throws VnanoException {

		EngineConnector updatedEngineConnector = new EngineConnector(this.optionMap, updatedPermissionMap, this.permissionAuthorizer);
		updatedEngineConnector.reflectPermissionSettings();
		return updatedEngineConnector;
	}


	// パーミッション認可プラグイン(permission authorizer)を差し替えたインスタンスを生成して返す
	public final EngineConnector createPermissionAuthorizerUpdatedInstance(PermissionAuthorizerConnectorInterface1 updatedPermissionAuthorizer)
			throws VnanoException {

		EngineConnector updatedEngineConnector = new EngineConnector(this.optionMap, this.permissionMap, updatedPermissionAuthorizer);
		updatedEngineConnector.reflectPermissionSettings();
		return updatedEngineConnector;
	}



	/**
	 * <span class="lang-en">
	 * Checks whether the option is set or not
	 * </span>
	 * <span class="lang-ja">
	 * 指定された名称のオプションが設定されているかどうかを判定します
	 * </span>
	 * .
	 *
	 * @param optionKey
	 *   <span class="lang-en">The key of the option (option name) to be checked.</span>
	 *   <span class="lang-ja">判定するオプションのキー（オプション名）.</span>
	 *
	 * @return
	 *   <span class="lang-en">The chech result (if the option is set, then returns true)</span>
	 *   <span class="lang-ja">判定結果（保持していれば true）.</span>
	 */
	@Override
	public final boolean hasOptionValue(String optionKey) {
		return this.optionMap.containsKey(optionKey);
	}


	/**
	 * <span class="lang-en">
	 * Gets the value of the option
	 * </span>
	 * <span class="lang-ja">
	 * 指定されたオプションの値を取得します
	 * </span>
	 * .
	 *
	 * @param optionKey
	 *   <span class="lang-en">The key of the option (option name).</span>
	 *   <span class="lang-ja">オプションのキー（オプション名）.</span>
	 *
	 * @return
	 *   <span class="lang-en">The option value.</span>
	 *   <span class="lang-ja">オプションの値.</span>
	 */
	@Override
	public final Object getOptionValue(String optionName) {
		return this.optionMap.get(optionName);
	}


	/**
	 * 指定された名称のパーミッションを要求します。
	 *
	 * @param permissionName パーミッションの名称
	 * @param requester パーミッションの要求元プラグイン
	 * @param metaInformation ユーザーに通知するメッセージ内等で用いられるメタ情報
	 * @throws 要求したパーミッションが却下された場合にスローされます。
	 */
	@Override
	public final void requestPermission(String permissionName, Object requester, Object metaInformation)
			throws ConnectorException {

		// 接続されているパーミッション許可プラグインに要求を投げる
		if (this.permissionAuthorizer != null) {

			// (許可されれば何も起こらず、拒否されれば ConnectorException が発生する)
			this.permissionAuthorizer.requestPermission(permissionName, requester, metaInformation);

		// パーミッション許可プラグインが接続されていない場合はエラー
		} else {
			String errorMessage = ErrorMessage.generateErrorMessage(
				ErrorType.NO_PERMISSION_AUTHORIZER_IS_CONNECTED, (Locale)this.optionMap.get(OptionKey.LOCALE)
			);
			throw new ConnectorException(errorMessage);
		}
	}
}
