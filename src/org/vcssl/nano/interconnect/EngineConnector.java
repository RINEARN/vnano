/*
 * Copyright(C) 2019-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.JOptionPane;

import org.vcssl.connect.ConnectorException;
import org.vcssl.connect.ConnectorPermissionName;
import org.vcssl.connect.ConnectorPermissionValue;
import org.vcssl.connect.EngineConnectorInterface1;
import org.vcssl.nano.spec.ConfirmationMessage;
import org.vcssl.nano.spec.ConfirmationType;
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

	private final Map<String, Object> optionMap;
	private final Map<String, String> originalPermissionMap;
	private Map<String, String> modifiedPermissionMap = null;

	public EngineConnector(Map<String, Object> optionMap, Map<String, String> permissionMap) {
		this.optionMap = optionMap;
		this.originalPermissionMap = permissionMap;
	}

	public void activate() {
		// 実行時用パーミッションマップを、実行時前のパーミッション内容で初期化
		this.modifiedPermissionMap = new HashMap<String, String>(this.originalPermissionMap);
	}

	public void deactivate() {
		// 実行時用パーミッションマップを、実行時前のパーミッション内容で初期化
		this.modifiedPermissionMap = new HashMap<String, String>(this.originalPermissionMap);
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
	public boolean hasOptionValue(String optionKey) {
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
	public Object getOptionValue(String optionName) {
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
	public void requestPermission(String permissionName, Object requester, Object metaInformation)
			throws ConnectorException {

		// メッセージのロケール設定を取得
		Locale locale = this.optionMap.containsKey(OptionKey.LOCALE)
				? (Locale)this.optionMap.get(OptionKey.LOCALE) : Locale.getDefault();

		// 以下、指定されたパーミッション名に対応した、パーミッション設定値を取得

		String permissionValue = null;

		// 指定されたパーミッション名がマップに登録されている場合は、その設定値を用いる
		if (this.modifiedPermissionMap.containsKey(permissionName)) {
			permissionValue = this.modifiedPermissionMap.get(permissionName);

		// 上記以外で、メタパーミッション名 "ALL" がマップに登録されている場合は、その設定値を用いる
		// （分岐順序に注意: 上の分岐のように、指定パーミッション名が明示的に登録されている場合は、そちらを優先すべき）
		} else if (this.modifiedPermissionMap.containsKey(ConnectorPermissionName.ALL)) {
			permissionValue = this.modifiedPermissionMap.get(ConnectorPermissionName.ALL);

		// 指定パーミッション名も "ALL" もどちらも登録されていない場合はエラー
		} else {
			String errorMessage = ErrorMessage.generateErrorMessage(
				ErrorType.UNSUPPORTED_PERMISSION_NAME, new String[] { permissionName }, locale
			);
			throw new ConnectorException(errorMessage);
		}


		// 以下、パーミッション設定値に応じて許可するか拒否するかを判断する

		// "ALLOW" の場合は常に許可
		if (permissionValue.equals(ConnectorPermissionValue.ALLOW)) {
			return;

		// "DENY" の場合は常に拒否
		} else if (permissionValue.equals(ConnectorPermissionValue.DENY)) {
			String errorMessage = ErrorMessage.generateErrorMessage(
				ErrorType.PERMISSION_DENIED, new String[] { permissionName }, locale
			);
			throw new ConnectorException(errorMessage);

		// "ASK" の場合はユーザーに尋ねる
		} else if (permissionValue.equals(ConnectorPermissionValue.ASK)) {

			// 尋ねるメッセージを用意
			String requesterName = requester.getClass().getCanonicalName();
			String[] confirmMessageWords = metaInformation instanceof String
					? new String[] { permissionName, requesterName, (String)metaInformation }
					: new String[] { permissionName, requesterName };

			String confirmMessage = ConfirmationMessage.generateConfirmationMessage(
				ConfirmationType.PERMISSION_REQUESTED, confirmMessageWords, locale
			);

			// ユーザーに尋ねて結果を取得
			int userDecision = JOptionPane.showConfirmDialog(null, confirmMessage, "!", JOptionPane.YES_NO_OPTION);

			// 許可された場合は、同種の処理をスクリプト完了までの間自動的に許可したいか聞いて控えた上で return する
			if (userDecision == JOptionPane.YES_OPTION) {
				confirmMessage = ConfirmationMessage.generateConfirmationMessage(
					ConfirmationType.ALLOW_SAME_PERMISSION_AUTOMATICALLY, new String[] { permissionName }, locale
				);
				userDecision = JOptionPane.showConfirmDialog(null, confirmMessage, "!", JOptionPane.YES_NO_OPTION);
				if (userDecision == JOptionPane.YES_OPTION) {
					// 自動許可がOKされた場合は、実行時用パーミッションマップの値を ALLOW に変更（実行毎にリセットされる）
					this.modifiedPermissionMap.put(permissionName, ConnectorPermissionValue.ALLOW);
				}
				return;

			// 拒否された場合はエラー
			} else {
				String errorMessage = ErrorMessage.generateErrorMessage(
					ErrorType.PERMISSION_DENIED, new String[] { permissionName }, locale
				);
				throw new ConnectorException(errorMessage);
			}

		// それ以外はこの処理系ではサポートしていないハンドリング
		} else {
			String errorMessage = ErrorMessage.generateErrorMessage(
				ErrorType.UNSUPPORTED_PERMISSION_VALUE, new String[] { permissionName, permissionValue }, locale
			);
			throw new ConnectorException(errorMessage);
		}
	}
}
