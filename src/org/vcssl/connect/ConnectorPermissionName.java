/*
 * ==================================================
 * Connector Parmission
 * ( for VCSSL / Vnano Plug-in Development )
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2017-2019 by RINEARN (Fumihiro Matsui)
 * ==================================================
 */

package org.vcssl.connect;

/**
 * <p>
 * パーミッション設定ベースのセキュリティレイヤーを持つ処理系のために、
 * 列挙的にパーミッションが定義されたクラスです。
 * </p>
 *
 * <p>
 * このクラスがフィールドとして提供する各パーミッションは、
 * 本来は列挙子の要素として定義されるのが素直であり、
 * その方が処理系側の検査オーバーヘッドも軽減が見込めますが、
 * 定義順序変更時のプラグイン側の再コンパイルを不要にするためや、
 * その他の互換性を考慮して、
 * public static final な文字列フィールドとして定義されています。
 * </p>
 *
 * <p>
 * その上で、検査オーバーヘッドを可能な限り軽減するために、
 * 各パーミッションの定義値は、明瞭さと引き換えに文字列長を短く抑えています。
 * そのため、各定義値をリテラルなどで直接使用する事は避け、
 * なるべくこのクラスのフィールドを参照して使用してください。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class ConnectorPermissionName {

	/** 全てのパーミッションを含む事を表します。 */
	public static final String ALL = "ALL";

	/** どのパーミッションも含まない事を表します。 */
	public static final String NONE = "NONE";

	/** 全パーミッション項目における、未設定時のデフォルト値を設定/取得する際に用います。 */
	public static final String DEFAULT = "DEFAULT";

	/** 実行対象スクリプトの終了を要求する事に対するパーミッションです。 */
	public static final String PROGRAM_EXIT = "PROGRAM_EXIT";

	/** 実行対象スクリプトのリセットを要求する事に対するパーミッションです。 */
	public static final String PROGRAM_RESET = "PROGRAM_RESET";

	/** 実行対象スクリプトの変更を要求する事に対するパーミッションです。 */
	public static final String PROGRAM_CHANGE = "PROGRAM_CHANGE";

	/** システム経由における外部プログラムやコマンドの実行（execの相当操作に対するパーミッションです。 */
	public static final String SYSTEM_PROCESS = "SYSTEM_PROCESS";

	/** ディレクトリの新規作成に対するパーミッションです。 */
	public static final String DIRECTORY_CREATE = "DIRECTORY_CREATE";

	/** ディレクトリの削除に対するパーミッションです。 */
	public static final String DIRECTORY_DELETE = "DIRECTORY_DELETE";

	/** ディレクトリ内のファイル一覧取得に対するパーミッションです。 */
	public static final String DIRECTORY_LIST = "DIRECTORY_LIST";

	/** ファイルの新規作成に対するパーミッションです。 */
	public static final String FILE_CREATE = "FILE_CREATE";

	/** ファイルの削除に対するパーミッションです。 */
	public static final String FILE_DELETE = "FILE_DELETE";

	/** ファイルの書き込みに対するパーミッションです。 */
	public static final String FILE_WRITE = "FILE_WRITE";

	/** ファイルの書き込みに対するパーミッションです。 */
	public static final String FILE_READ = "FILE_READ";

	/** ファイルの上書きに対するパーミッションです。 */
	public static final String FILE_OVERWRITE = "FILE_OVERWRITE";

	/** ファイルの情報変更に対するパーミッションです。 */
	public static final String FILE_INFORMATION_CHANGE = "FILE_INFORMATION_CHANGE";
}
