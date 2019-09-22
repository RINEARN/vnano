/*
 * ==================================================
 * Field to XVCI Plug-in Adapter
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2017-2019 by RINEARN (Fumihiro Matsui)
 * ==================================================
 */

package org.vcssl.connect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;


/**
 * <p>
 * ホスト言語側のフィールドを、{@link org.vcssl.connect.ExternalVariableConnectorInterface1 XVCI 1}
 * 形式の外部変数プラグイン仕様に変換し、XVCI 1 対応の言語処理系に接続するためのアダプタです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class FieldToXvci1Adapter implements ExternalVariableConnectorInterface1 {


	/** デフォルトの必要パーミッション配列（値は { {@link ConnectorPermissionName#NONE ConnectorPermissionName.NONE} } ）です。 */
	private static final String[] DEFAULT_NECESSARY_PERMISSIONS = { ConnectorPermissionName.NONE };

	/** デフォルトの不要パーミッション配列（値は { {@link ConnectorPermissionName#ALL ConnectorPermissionName.ALL} } ）です。 */
	private static final String[] DEFAULT_UNNECESSARY_PERMISSIONS = { ConnectorPermissionName.ALL };


	/** ホスト言語側のフィールドへの、リフレクションによるアクセスを提供するFieldインスタンスです。 */
	private Field field = null;

	/** ホスト言語のフィールドが属するオブジェクトのインスタンスです。 */
	private Object objectInstance = null;

	/** 必要パーミッション配列です。 */
	private String[] necessaryPermissionNames = null;

	/** 不要パーミッション配列です。 */
	private String[] unnecessaryPermissionNames = null;


	/**
	 * 指定されたホスト言語側のインスタンスフィールドを、
	 * XVCI準拠の外部変数プラグインへと変換するアダプタを生成します。
	 *
	 * @param field 対象フィールドへのリフレクションによるアクセスを提供するFieldインスタンス
	 * @param objectInstance 対象フィールドが属するオブジェクトのインスタンス
	 */
	public FieldToXvci1Adapter (Field field, Object objectInstance) {
		this.field = field;
		this.objectInstance = objectInstance;

		this.necessaryPermissionNames = Arrays.copyOf(
				DEFAULT_NECESSARY_PERMISSIONS, DEFAULT_NECESSARY_PERMISSIONS.length
		);
		this.unnecessaryPermissionNames = Arrays.copyOf(
				DEFAULT_UNNECESSARY_PERMISSIONS, DEFAULT_UNNECESSARY_PERMISSIONS.length
		);
	}


	/**
	 * 指定されたホスト言語側のクラスフィールドを、
	 * XVCI準拠の外部変数プラグインへと変換するアダプタを生成します。
	 *
	 * @param field 対象フィールドへのリフレクションによるアクセスを提供するFieldインスタンス
	 */
	public FieldToXvci1Adapter (Field field) {
		this.field = field;
		this.objectInstance = null;

		this.necessaryPermissionNames = Arrays.copyOf(
				DEFAULT_NECESSARY_PERMISSIONS, DEFAULT_NECESSARY_PERMISSIONS.length
		);
		this.unnecessaryPermissionNames = Arrays.copyOf(
				DEFAULT_UNNECESSARY_PERMISSIONS, DEFAULT_UNNECESSARY_PERMISSIONS.length
		);
	}


	/**
	 * 変数名を取得します。
	 *
	 * @return 変数名
	 */
	public String getVariableName() {
		return this.field.getName();
	}


	/**
	 * 変数のデータの型を表すClassインスタンスを取得します。
	 *
	 * @return データ型のClassインスタンス
	 */
	public Class<?> getDataClass() {
		return this.field.getType();
	}


	/**
	 * 書き換え不可能な定数であるかどうかを判定します。
	 *
	 * @return 定数であればtrue
	 */
	public boolean isConstant() {
		return Modifier.isFinal(field.getModifiers());
	}


	/**
	 * データの自動変換が必要かどうかを返します。
	 * このアダプタではデータ変換が必須であるため、常にtrueを返します。
	 *
	 * @return 常にtrue
	 */
	public boolean isDataConversionNecessary() {
		return true;
	}


	/**
	 * この変数のデータの読み書きに必要な全てのパーミッションの名称を、配列にまとめて設定します。
	 *
	 * このメソッドで設定される必要パーミッション配列と、
	 * {@link FieldToXvci1Adapter#setUnnecessaryPermissions setUnnecessaryPermissions}
	 * メソッドで設定される不要パーミッション配列において、重複している要素がある場合は、
	 * 前者の方が優先されます（つまり、そのパーミッションは必要と判断されます）。
	 *
	 * なお、このメソッドの引数に、
	 * {@link ConnectorPermissionName#ALL ConnectorPermissionName.NONE}
	 * のみを格納する配列を渡す事で、全てのパーミッションが不要となります。
	 * ただし、そのような事は、
	 * この関数が一切のシステムリソースやネットワークにアクセスしない場合など、
	 * スクリプト内で閉じた処理と同等以上のセキュリティが確保されている場合のみ行ってください。
	 *
	 * @param necessaryPermissionNames 必要なパーミッションの名称を格納する配列
	 */
	public void setNecessaryPermissionNames(String[] necessaryPermissionNames) {
		this.necessaryPermissionNames = necessaryPermissionNames;
	}


	/**
	 * この変数のデータの読み書きに必要な全てのパーミッションの名称を、配列にまとめて返します。
	 *
	 * デフォルトでは、パーミッションが不要である事を意味する
	 * { {@link ConnectorPermissionName#NONE ConnectorPermissionName.NONE}
	 * が返されます。
	 *
	 * @return 必要なパーミッションの名称を格納する配列
	 */
	public String[] getNecessaryPermissionNames() {
		return this.necessaryPermissionNames;
	}


	/**
	 * この変数のデータの読み書きに不要な全てのパーミッションの名称を、配列にまとめて設定します。
	 *
	 * このメソッドで設定される不要パーミッション配列と、
	 * {@link FieldToXvci1Adapter#getNecessaryPermissions getNecessaryPermissions}
	 * メソッドで設定される必要パーミッション配列において、重複している要素がある場合は、
	 * 後者の方が優先されます（つまり、そのパーミッションは必要と判断されます）。
	 *
	 * なお、このメソッドの引数に
	 * {@link ConnectorPermissionName#ALL ConnectorPermissionName.ALL} のみを格納する配列を返す事で、
	 * 必要パーミッション配列に含まれているものを除いた、全てのパーミッションが不要となります。
	 * これは、将来的に新しいパーミッションが追加された場合に、
	 * そのパーミッションによって、この関数の実行が拒否される事を回避する事ができます。
	 *
	 * ただし、セキュリティが重要となる用途に使用するプラグインの開発においては、
	 * そのような事自体がそもそも好ましくない事に注意する必要があります。
	 * そのようなセキュリティ重要度の高い用途に向けたプラグインの開発に際しては、
	 * 開発時点で存在する個々のパーミッションについて、
	 * 不要である事が判明しているものだけを設定するようにしてください。
	 *
	 * そうすれば、必要・不要のどちらにも含まれない、
	 * 開発時点で未知のパーミッションの扱いについては、
	 * 処理系側やユーザー側の判断に委ねる事ができます。
	 *
	 * @param unnecessaryPermissionNames 不要なパーミッションの名称を格納する配列
	 */
	public void setUnnecessaryPermissionNames(String[] unnecessaryPermissionNames) {
		this.unnecessaryPermissionNames = unnecessaryPermissionNames;
	}


	/**
	 * この変数のデータの読み書きに不要な全てのパーミッションの名称を、配列にまとめて取得します。
	 *
	 * デフォルトでは、パーミッションが不要である事を意味する
	 * { {@link ConnectorPermissionName#NONE ConnectorPermissionName.NONE}
	 * が返されます。
	 *
	 * @return 不要なパーミッションの名称を格納する配列
	 */
	public String[] getUnnecessaryPermissionNames() {
		return this.unnecessaryPermissionNames;
	}


	/**
	 * 変数のデータを取得します。
	 */
	public Object getData() throws ConnectorException {
		try {
			return this.field.get(this.objectInstance);

		// アクセス修飾子などが原因で取得できない場合
		} catch (IllegalArgumentException illegalArgumentException) {
			throw new ConnectorException(
					objectInstance.getClass().getCanonicalName() + " class has no field named \"" + this.field.getName() + "\"",
					illegalArgumentException
			);

		// そもそもインスタンスが対象フィールドを持っていない場合
		} catch (IllegalAccessException illegalAccessException) {
			throw new ConnectorException(
					"The field \"" + this.field.getName() + "\" of " + objectInstance.getClass().getCanonicalName()
					+ " class is not accessable (probably it is private or protected).",
					illegalAccessException
			);
		}
	}


	/**
	 * データの自動変換が無効化されている場合において、変数のデータを取得します。
	 * このアダプタでは、この機能は使用されません。
	 */
	public void getData(Object dataContainer) throws ConnectorException {
	}


	/**
	 * 変数のデータを設定します。
	 *
	 * @param data 変数のデータ
	 */
	public void setData(Object data) throws ConnectorException {
		try {
			this.field.set(this.objectInstance, data);

		// アクセス修飾子などが原因で設定できない場合
		} catch (IllegalArgumentException illegalArgumentException) {
			throw new ConnectorException(
					objectInstance.getClass().getCanonicalName() + " class has no field named \"" + this.field.getName() + "\"",
					illegalArgumentException
			);

		// そもそもインスタンスが対象フィールドを持っていない場合
		} catch (IllegalAccessException illegalAccessException) {
			throw new ConnectorException(
					"The field \"" + this.field.getName() + "\" of " + objectInstance.getClass().getCanonicalName()
					+ " class is not accessable (probably it is private or protected).",
					illegalAccessException
			);
		}
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
