/*
 * ==================================================
 * Class to XLCI Plug-in Adapter
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2019 by RINEARN (Fumihiro Matsui)
 * ==================================================
 */

package org.vcssl.connect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * ホスト言語側のクラスを、{@link org.vcssl.connect.ExternalLibraryConnector1 XLCI 1}
 * 形式の外部変数プラグイン仕様に変換し、XLCI 1 対応の言語処理系に接続するためのアダプタです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class ClassToXlci1Adapter implements ExternalLibraryConnector1 {


	/** デフォルトの必要パーミッション配列（値は { {@link ExternalPermission#NONE ExternalPermission.NONE} } ）です。 */
	private static final String[] DEFAULT_NECESSARY_PERMISSIONS = { ExternalPermission.NONE };

	/** デフォルトの不要パーミッション配列（値は { {@link ExternalPermission#ALL ExternalPermission.ALL} } ）です。 */
	private static final String[] DEFAULT_UNNECESSARY_PERMISSIONS = { ExternalPermission.ALL };


	/** ホスト言語側のクラスです。 */
	private Class<?> pluginClass = null;

	/** ホスト言語のメソッドが属するオブジェクトのインスタンスです。 */
	private Object pluginInstance = null;

	/** 必要パーミッション配列です。 */
	private String[] necessaryPermissions = null;

	/** 不要パーミッション配列です。 */
	private String[] unnecessaryPermissions = null;



	/**
	 * 指定されたホスト言語側のクラスを、インスタンスメソッドやインスタンスフィールドを含めて、
	 * XLCI準拠の外部変数プラグインへと変換するアダプタを生成します。
	 *
	 * @param pluginClass 対象クラス
	 * @param objectInstance 対象クラスのインスタンス
	 */
	public ClassToXlci1Adapter(Class<?> pluginClass, Object pluginInstance) {
		this.pluginClass = pluginClass;
		this.pluginInstance = pluginInstance;

		this.necessaryPermissions = Arrays.copyOf(
				DEFAULT_NECESSARY_PERMISSIONS, DEFAULT_NECESSARY_PERMISSIONS.length
		);
		this.unnecessaryPermissions = Arrays.copyOf(
				DEFAULT_UNNECESSARY_PERMISSIONS, DEFAULT_UNNECESSARY_PERMISSIONS.length
		);
	}


	/**
	 * 指定されたホスト言語側のクラスを、staticメソッドやstaticフィールドのみの範囲で、
	 * XLCI準拠の外部変数プラグインへと変換するアダプタを生成します。
	 *
	 * @param pluginClass 対象クラス
	 */
	public ClassToXlci1Adapter(Class<?> pluginClass) {
		this.pluginClass = pluginClass;

		this.necessaryPermissions = Arrays.copyOf(
				DEFAULT_NECESSARY_PERMISSIONS, DEFAULT_NECESSARY_PERMISSIONS.length
		);
		this.unnecessaryPermissions = Arrays.copyOf(
				DEFAULT_UNNECESSARY_PERMISSIONS, DEFAULT_UNNECESSARY_PERMISSIONS.length
		);
	}


	/**
	 * ライブラリ名を取得します。
	 *
	 * @return ライブラリ名
	 */
	@Override
	public String getLibraryName() {
		return null;
	}


	/**
	 * パーミッション設定ベースのセキュリティレイヤーを持つ処理系において、
	 * このライブラリの接続に必要な全てのパーミッションを、配列にまとめて設定します。
	 *
	 * @param necessaryPermissions 必要なパーミッションを格納する配列
	 */
	public void setNecessaryPermissions(String[] necessaryPermissions) {
		this.necessaryPermissions = necessaryPermissions;
	}


	/**
	 * パーミッション設定ベースのセキュリティレイヤーを持つ処理系において、
	 * このライブラリの接続に必要な全てのパーミッションを、配列にまとめて取得します。
	 *
	 * パーミッションベースのセキュリティレイヤ―を持たない処理系では、
	 * このメソッドは機能しません（呼び出されません）。
	 *
	 * このメソッドの戻り値に、
	 * {@link ExternalPermission#NONE ExternalPermission.NONE}
	 * のみを格納する配列を返す事で、全てのパーミッションが不要となります。
	 * 現状では、このライブラリに属する関数・変数のインターフェースである
	 * {@link ExternalFunctionConnector1 XFCI1}/{@link ExternalFunctionConnector1 XVCI1}
	 * の階層でもパーミッション指定機能を持っているため、このメソッドは冗長であり、
	 * 上記のように実装する以外の具体的な使い道は、あまり考えられません。
	 *
	 * このメソッドは、将来的に、ライブラリを接続する事そのものに対して、
	 * それに属する関数・変数とは独立にパーミッション設定を行いたい用途が生じた場合のために、
	 * 予約的に宣言されています。
	 *
	 * @return 必要なパーミッションを格納する配列
	 */
	@Override
	public String[] getNecessaryPermissions() {
		return this.necessaryPermissions;
	}


	/**
	 * パーミッション設定ベースのセキュリティレイヤーを持つ処理系において、
	 * この関数の実行に不要な全てのパーミッションを、配列にまとめて設定します。
	 *
	 * @param unnecessaryPermissions 不要なパーミッションを格納する配列
	 */
	public void setUnnecessaryPermissions(String[] unnecessaryPermissions) {
		this.unnecessaryPermissions = unnecessaryPermissions;
	}


	/**
	 * パーミッション設定ベースのセキュリティレイヤーを持つ処理系において、
	 * このライブラリの接続に不要な全てのパーミッションを、配列にまとめて取得します。
	 *
	 * パーミッションベースのセキュリティレイヤ―を持たない処理系では、
	 * このメソッドは機能しません（呼び出されません）。
	 *
	 * このメソッドの戻り値に
	 * {@link ExternalPermission#ALL ExternalPermission.ALL} のみを格納する配列を返す事で、
	 * 必要パーミッション配列に含まれているものを除いた、全てのパーミッションが不要となります。
	 * 現状では、このライブラリに属する関数・変数のインターフェースである
	 * {@link ExternalFunctionConnector1 XFCI1}/{@link ExternalFunctionConnector1 XVCI1}
	 * の階層でもパーミッション指定機能を持っているため、このメソッドは冗長であり、
	 * 上記のように実装する以外の具体的な使い道は、あまり考えられません。
	 *
	 * このメソッドは、将来的に、ライブラリを接続する事そのものに対して、
	 * それに属する関数・変数とは独立にパーミッション設定を行いたい用途が生じた場合のために、
	 * 予約的に宣言されています。
	 *
	 * @return 不要なパーミッションを格納する配列
	 */
	@Override
	public String[] getUnnecessaryPermissions() {
		return this.unnecessaryPermissions;
	}


	/**
	 * このライブラリに属する全ての関数を、配列にまとめて返します。
	 *
	 * @return このライブラリに属する関数をまとめた配列
	 */
	@Override
	public ExternalFunctionConnector1[] getFunctions() {

		// 変換対象クラスに属する全てのメソッドを取得
		Method[] methods = this.pluginClass.getDeclaredMethods();

		// XFCI1形式に変換したアダプタを格納するリスト
		List<ExternalFunctionConnector1> xfciList = new LinkedList<ExternalFunctionConnector1>();

		// メソッドを1つずつXFCI1形式に変換してリストに追加していく
		for (Method method: methods) {

			// メソッドの修飾子情報を表すフラグ
			int modifiers = method.getModifiers();

			// public でなければスクリプトエンジンからアクセスできないのでスキップ
			if (!Modifier.isPublic(modifiers)) {
				continue;
			}

			// 以下、public な場合

			// staticメソッドは全て変換
			if (Modifier.isStatic(modifiers)) {
				xfciList.add(new MethodToXfci1Adapter(method));

			// インスタンスメソッドは、このアダプタがインスタンスを保持している場合のみ変換
			} else if (this.pluginInstance != null) {
				xfciList.add(new MethodToXfci1Adapter(method, this.pluginInstance));
			}
		}

		// リスト内容を配列にまとめて返す
		return xfciList.toArray(new ExternalFunctionConnector1[0]);
	}


	/**
	 * このライブラリに属する全ての変数を、配列にまとめて返します。
	 *
	 * @return このライブラリに属する変数をまとめた配列
	 */
	@Override
	public ExternalVariableConnector1[] getVariables() {

		// 変換対象クラスに属する全てのフィールドを取得
		Field[] fields = this.pluginClass.getDeclaredFields();

		// XVCI1形式に変換したアダプタを格納するリスト
		List<ExternalVariableConnector1> xvciList = new LinkedList<ExternalVariableConnector1>();

		// フィールドを1つずつXVCI1形式に変換してリストに追加していく
		for (Field field: fields) {

			// フィールドの修飾子情報を表すフラグ
			int modifiers = field.getModifiers();

			// public でなければスクリプトエンジンからアクセスできないのでスキップ
			if (!Modifier.isPublic(modifiers)) {
				continue;
			}

			// 以下、public な場合

			// staticフィールドは全て変換
			if (Modifier.isStatic(modifiers)) {
				xvciList.add(new FieldToXvci1Adapter(field));

			// インスタンスフィールドは、このアダプタがインスタンスを保持している場合のみ変換
			} else if (this.pluginInstance != null) {
				xvciList.add(new FieldToXvci1Adapter(field, this.pluginInstance));
			}
		}

		// リスト内容を配列にまとめて返す
		return xvciList.toArray(new ExternalVariableConnector1[0]);
	}


	/**
	 * このプラグインが、スクリプトエンジンに接続された際に呼び出され、
	 * そのエンジンに依存するやり取りを行うためのオブジェクトが渡されます。
	 *
	 * 同オブジェクトは、恐らく {@link EngineConnector1 EngineConnector1}
	 * もしくはその後継の、抽象化されたインターフェースでラップされた形で渡されます。
	 *
	 * @param engineConnector エンジンに依存するやり取りを行うためのオブジェクト
	 */
	@Override
	public void setEngine(Object engineConnector) {
	}


	/**
	 * 処理系への接続時に必要な初期化処理を行います。
	 */
	@Override
	public void initializeForConnection() {
	}


	/**
	 * 処理系からの接続解除時に必要な終了時処理を行います。
	 */
	@Override
	public void finalizeForDisconnection() {
	}


	/**
	 * スクリプト実行毎の初期化処理を行います。
	 */
	@Override
	public void initializeForExecution() {
	}


	/**
	 * スクリプト実行毎の終了時処理を行います。
	 */
	@Override
	public void finalizeForTermination() {
	}

}
