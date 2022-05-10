package org.vcssl.nano.spec;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.vcssl.connect.ConnectorPermissionName;
import org.vcssl.nano.VnanoFatalException;

public class ConfirmationMessage {

	private static final Map<String, String> PERMISSION_JAJP_NAME_MAP;
	static {
		PERMISSION_JAJP_NAME_MAP = new HashMap<String, String>();
		PERMISSION_JAJP_NAME_MAP.put(ConnectorPermissionName.FILE_CREATE, "ファイルの新規作成");
		PERMISSION_JAJP_NAME_MAP.put(ConnectorPermissionName.FILE_WRITE, "ファイルの書き込み");
		PERMISSION_JAJP_NAME_MAP.put(ConnectorPermissionName.FILE_READ, "ファイルの読み込み");
		PERMISSION_JAJP_NAME_MAP.put(ConnectorPermissionName.FILE_OVERWRITE, "ファイルの上書き");
		PERMISSION_JAJP_NAME_MAP.put(ConnectorPermissionName.FILE_DELETE, "ファイルの削除");
		PERMISSION_JAJP_NAME_MAP.put(ConnectorPermissionName.FILE_INFORMATION_CHANGE, "ファイルの情報変更");
		PERMISSION_JAJP_NAME_MAP.put(ConnectorPermissionName.DIRECTORY_CREATE, "フォルダの新規作成");
		PERMISSION_JAJP_NAME_MAP.put(ConnectorPermissionName.DIRECTORY_LIST, "フォルダ内の一覧取得");
		PERMISSION_JAJP_NAME_MAP.put(ConnectorPermissionName.DIRECTORY_DELETE, "フォルダの削除");
	}

	private static final Map<String, String> PERMISSION_ENUS_NAME_MAP;
	static {
		PERMISSION_ENUS_NAME_MAP = new HashMap<String, String>();
		PERMISSION_ENUS_NAME_MAP.put(ConnectorPermissionName.FILE_CREATE, "Creating a File");
		PERMISSION_ENUS_NAME_MAP.put(ConnectorPermissionName.FILE_WRITE, "Writing to a File");
		PERMISSION_ENUS_NAME_MAP.put(ConnectorPermissionName.FILE_READ, "Reading from a File");
		PERMISSION_ENUS_NAME_MAP.put(ConnectorPermissionName.FILE_OVERWRITE, "Overwriting to a File");
		PERMISSION_ENUS_NAME_MAP.put(ConnectorPermissionName.FILE_DELETE, "Deleting a File");
		PERMISSION_ENUS_NAME_MAP.put(ConnectorPermissionName.FILE_INFORMATION_CHANGE, "Changing of Information of a File");
		PERMISSION_JAJP_NAME_MAP.put(ConnectorPermissionName.DIRECTORY_CREATE, "Creating a directory");
		PERMISSION_JAJP_NAME_MAP.put(ConnectorPermissionName.DIRECTORY_LIST, "Listing-up Files in a Directory");
		PERMISSION_JAJP_NAME_MAP.put(ConnectorPermissionName.DIRECTORY_DELETE, "Deliting a directory");
	}

	public static String generateConfirmationMessage(ConfirmationType confirmType) {
		return generateConfirmationMessage(confirmType, null, Locale.getDefault());
	}

	public static String generateConfirmationMessage(ConfirmationType confirmType, String[] words) {
		return generateConfirmationMessage(confirmType, words, Locale.getDefault());
	}

	public static String generateConfirmationMessage(ConfirmationType confirmType, String[] words, Locale locale) {

		// ロケール言語に応じた確認メッセージを生成して返す
		if (   ( locale.getLanguage()!=null && locale.getLanguage().equals("ja") )
			   || ( locale.getCountry()!=null && locale.getCountry().equals("JP") )   ) {

			return generateConfirmationMessageJaJP(confirmType, words);
		} else {
			return generateConfirmationMessageEnUS(confirmType, words);
		}
	}

	public static String generateConfirmationMessageJaJP(ConfirmationType confirmType, String[] words) {

		switch (confirmType) {

			case PERMISSION_REQUESTED : {
				String permissionName = PERMISSION_JAJP_NAME_MAP.containsKey(words[0])
						? PERMISSION_JAJP_NAME_MAP.get(words[0]) : words[0];

				String message = "現在実行中の処理が、「 " + permissionName + " 」の許可を求めています。許可しますか ?\n"
								+ "( 要求元プラグイン： " + words[1] + " )";
				if (3 <= words.length) {
					message += "\n\n" + "対象： " + words[2];
				}

				return message;
			}

			case ALLOW_SAME_PERMISSION_AUTOMATICALLY : {
				String permissionName = PERMISSION_JAJP_NAME_MAP.containsKey(words[0])
						? PERMISSION_JAJP_NAME_MAP.get(words[0]) : words[0];

				return "現在実行中の処理の間、同じ種類の要求（ " + permissionName + " ）を自動的に許可しますか ?";
			}

			default : {
				throw new VnanoFatalException("予期しないメッセージタイプ: " + confirmType);
			}
		}
	}

	public static String generateConfirmationMessageEnUS(ConfirmationType confirmType, String[] words) {

		switch (confirmType) {

			case PERMISSION_REQUESTED : {
				String permissionName = PERMISSION_ENUS_NAME_MAP.containsKey(words[0])
						? PERMISSION_ENUS_NAME_MAP.get(words[0]) : words[0];

				String message = "The permission for \"" + permissionName + "\" has been requested. Do you allow it?\n"
								+ "( Requesting Plug-in： " + words[1] + " )";
				if (3 <= words.length) {
					message += "\n\n" + "Target： " + words[2];
				}

				return message;
			}

			case ALLOW_SAME_PERMISSION_AUTOMATICALLY : {
				String permissionName = PERMISSION_ENUS_NAME_MAP.containsKey(words[0])
						? PERMISSION_ENUS_NAME_MAP.get(words[0]) : words[0];

				String message = "Do you want to allow the same type request ( " + permissionName
						+ " ) automatically during the current processing ?";

				return message;
			}

			default : {
				throw new VnanoFatalException("Unexpected confirmation message type: " + confirmType);
			}
		}
	}
}
