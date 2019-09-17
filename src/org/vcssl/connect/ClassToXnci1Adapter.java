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
 * ホスト言語側のクラスを、{@link org.vcssl.connect.ExternalNamespaceConnectorInterface1 XNCI 1}
 * 形式の外部変数プラグイン仕様に変換し、XNCI 1 対応の言語処理系に接続するためのアダプタです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class ClassToXnci1Adapter implements ExternalNamespaceConnectorInterface1 {


	/** デフォルトの必要パーミッション配列（値は { {@link ConnectorPermissionName#NONE ConnectorPermissionName.NONE} } ）です。 */
	private static final String[] DEFAULT_NECESSARY_PERMISSIONS = { ConnectorPermissionName.NONE };

	/** デフォルトの不要パーミッション配列（値は { {@link ConnectorPermissionName#ALL ConnectorPermissionName.ALL} } ）です。 */
	private static final String[] DEFAULT_UNNECESSARY_PERMISSIONS = { ConnectorPermissionName.ALL };


	/** ホスト言語側のクラスです。 */
	private Class<?> pluginClass = null;

	/** ホスト言語のメソッドが属するオブジェクトのインスタンスです。 */
	private Object pluginInstance = null;

	/** 必要パーミッション配列です。 */
	private String[] necessaryPermissionNames = null;

	/** 不要パーミッション配列です。 */
	private String[] unnecessaryPermissionNames = null;



	/**
	 * 指定されたホスト言語側のクラスを、インスタンスメソッドやインスタンスフィールドを含めて、
	 * XNCI準拠の外部変数プラグインへと変換するアダプタを生成します。
	 *
	 * @param pluginClass 対象クラス
	 * @param objectInstance 対象クラスのインスタンス
	 */
	public ClassToXnci1Adapter(Class<?> pluginClass, Object pluginInstance) {
		this.pluginClass = pluginClass;
		this.pluginInstance = pluginInstance;

		this.necessaryPermissionNames = Arrays.copyOf(
				DEFAULT_NECESSARY_PERMISSIONS, DEFAULT_NECESSARY_PERMISSIONS.length
		);
		this.unnecessaryPermissionNames = Arrays.copyOf(
				DEFAULT_UNNECESSARY_PERMISSIONS, DEFAULT_UNNECESSARY_PERMISSIONS.length
		);
	}


	/**
	 * 指定されたホスト言語側のクラスを、staticメソッドやstaticフィールドのみの範囲で、
	 * XNCI準拠の外部変数プラグインへと変換するアダプタを生成します。
	 *
	 * @param pluginClass 対象クラス
	 */
	public ClassToXnci1Adapter(Class<?> pluginClass) {
		this.pluginClass = pluginClass;

		this.necessaryPermissionNames = Arrays.copyOf(
				DEFAULT_NECESSARY_PERMISSIONS, DEFAULT_NECESSARY_PERMISSIONS.length
		);
		this.unnecessaryPermissionNames = Arrays.copyOf(
				DEFAULT_UNNECESSARY_PERMISSIONS, DEFAULT_UNNECESSARY_PERMISSIONS.length
		);
	}


	/**
	 * 名前空間の名称を取得します。
	 *
	 * @return 名前空間の名称
	 */
	@Override
	public String getNamespaceName() {
		return null;
	}


	/**
	 * パーミッション設定ベースのセキュリティレイヤーを持つ処理系において、
	 * この名前空間へのアクセスに必要な全てのパーミッションの名称を、配列にまとめて設定します。
	 *
	 * @param necessaryPermissionNames 必要なパーミッションの名称を格納する配列
	 */
	public void setNecessaryPermissionNames(String[] necessaryPermissionNames) {
		this.necessaryPermissionNames = necessaryPermissionNames;
	}


	/**
	 * パーミッション設定ベースのセキュリティレイヤーを持つ処理系において、
	 * この名前空間へのアクセスに必要な全てのパーミッションの名称を、配列にまとめて取得します。
	 *
	 * パーミッションベースのセキュリティレイヤ―を持たない処理系では、
	 * このメソッドは機能しません（呼び出されません）。
	 *
	 * このメソッドの戻り値に、
	 * {@link ConnectorPermissionName#NONE ConnectorPermissionName.NONE}
	 * のみを格納する配列を返す事で、全てのパーミッションが不要となります。
	 * 現状では、この名前空間に属する関数・変数のインターフェースである
	 * {@link ExternalFunctionConnectorInterface1 XFCI1}/{@link ExternalFunctionConnectorInterface1 XVCI1}
	 * の階層でもパーミッション指定機能を持っているため、このメソッドは冗長であり、
	 * 上記のように実装する以外の具体的な使い道は、あまり考えられません。
	 *
	 * このメソッドは、将来的に、名前空間にアクセスする事そのものに対して、
	 * それに属する関数・変数とは独立にパーミッション設定を行いたい用途が生じた場合のために、
	 * 予約的に宣言されています。
	 *
	 * @return 必要なパーミッションの名称を格納する配列
	 */
	@Override
	public String[] getNecessaryPermissionNames() {
		return this.necessaryPermissionNames;
	}


	/**
	 * パーミッション設定ベースのセキュリティレイヤーを持つ処理系において、
	 * この関数の実行に不要な全てのパーミッションの名称を、配列にまとめて設定します。
	 *
	 * @param unnecessaryPermissionNames 不要なパーミッションの名称を格納する配列
	 */
	public void setUnnecessaryPermissionNames(String[] unnecessaryPermissionNames) {
		this.unnecessaryPermissionNames = unnecessaryPermissionNames;
	}


	/**
	 * パーミッション設定ベースのセキュリティレイヤーを持つ処理系において、
	 * この名前空間へのアクセスに不要な全てのパーミッションの名称を、配列にまとめて取得します。
	 *
	 * パーミッションベースのセキュリティレイヤ―を持たない処理系では、
	 * このメソッドは機能しません（呼び出されません）。
	 *
	 * このメソッドの戻り値に
	 * {@link ConnectorPermissionName#ALL ConnectorPermissionName.ALL} のみを格納する配列を返す事で、
	 * 必要パーミッション配列に含まれているものを除いた、全てのパーミッションが不要となります。
	 * 現状では、この名前空間に属する関数・変数のインターフェースである
	 * {@link ExternalFunctionConnectorInterface1 XFCI1}/{@link ExternalFunctionConnectorInterface1 XVCI1}
	 * の階層でもパーミッション指定機能を持っているため、このメソッドは冗長であり、
	 * 上記のように実装する以外の具体的な使い道は、あまり考えられません。
	 *
	 * このメソッドは、将来的に、名前空間にアクセスする事そのものに対して、
	 * それに属する関数・変数とは独立にパーミッション設定を行いたい用途が生じた場合のために、
	 * 予約的に宣言されています。
	 *
	 * @return 不要なパーミッションの名称を格納する配列
	 */
	@Override
	public String[] getUnnecessaryPermissionNames() {
		return this.unnecessaryPermissionNames;
	}


	/**
	 * この名前空間に属する全ての関数を、配列にまとめて返します。
	 *
	 * @return この名前空間に属する関数をまとめた配列
	 */
	@Override
	public ExternalFunctionConnectorInterface1[] getFunctions() {

		// 変換対象クラスに属する全てのメソッドを取得
		Method[] methods = this.pluginClass.getDeclaredMethods();

		// XFCI1形式に変換したアダプタを格納するリスト
		List<ExternalFunctionConnectorInterface1> xfciList = new LinkedList<ExternalFunctionConnectorInterface1>();

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
		return xfciList.toArray(new ExternalFunctionConnectorInterface1[0]);
	}


	/**
	 * この名前空間に属する全ての変数を、配列にまとめて返します。
	 *
	 * @return この名前空間に属する変数をまとめた配列
	 */
	@Override
	public ExternalVariableConnectorInterface1[] getVariables() {

		// 変換対象クラスに属する全てのフィールドを取得
		Field[] fields = this.pluginClass.getDeclaredFields();

		// XVCI1形式に変換したアダプタを格納するリスト
		List<ExternalVariableConnectorInterface1> xvciList = new LinkedList<ExternalVariableConnectorInterface1>();

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
		return xvciList.toArray(new ExternalVariableConnectorInterface1[0]);
	}


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
	public void initializeForConnection(Object engineConnector) throws ConnectorException {
	}


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
	public void finalizeForDisconnection(Object engineConnector) throws ConnectorException {
	}


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
	public void initializeForExecution(Object engineConnector) throws ConnectorException {
	}


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
	public void finalizeForTermination(Object engineConnector) throws ConnectorException {
	}

}
