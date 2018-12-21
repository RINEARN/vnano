/*
 * ==================================================
 * Field to XVCI Plug-in Adapter
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2017-2018 by RINEARN (Fumihiro Matsui)
 * ==================================================
 */

package org.vcssl.connect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;


/**
 * <p>
 * ホスト言語側のフィールドを、{@link org.vcssl.connect.ExternalVariableConnector1 XVCI 1}
 * 形式の外部変数プラグイン仕様に変換し、XVCI 1 対応の言語処理系に接続するためのアダプタです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class FieldXvci1Adapter implements ExternalVariableConnector1 {


	/** デフォルトの必要パーミッション配列（値は { {@link ExternalPermission#NONE ExternalPermission.NONE} } ）です。 */
	private static final String[] DEFAULT_NECESSARY_PERMISSIONS = { ExternalPermission.NONE };

	/** デフォルトの不要パーミッション配列（値は { {@link ExternalPermission#ALL ExternalPermission.ALL} } ）です。 */
	private static final String[] DEFAULT_UNNECESSARY_PERMISSIONS = { ExternalPermission.ALL };


	/** ホスト言語側のフィールドへの、リフレクションによるアクセスを提供するFieldインスタンスです。 */
	private Field field = null;

	/** ホスト言語のフィールドが属するオブジェクトのインスタンスです。 */
	private Object objectInstance = null;

	/** 必要パーミッション配列です。 */
	private String[] necessaryPermissions = null;

	/** 不要パーミッション配列です。 */
	private String[] unnecessaryPermissions = null;


	/**
	 * 指定されたホスト言語側のインスタンスフィールドを、
	 * XVCI準拠の外部変数プラグインへと変換するアダプタを生成します。
	 *
	 * @param field 対象フィールドへのリフレクションによるアクセスを提供するFieldインスタンス
	 * @param objectInstance 対象フィールドが属するオブジェクトのインスタンス
	 */
	public FieldXvci1Adapter (Field field, Object objectInstance) {
		this.field = field;
		this.objectInstance = objectInstance;

		this.necessaryPermissions = Arrays.copyOf(
				DEFAULT_NECESSARY_PERMISSIONS, DEFAULT_NECESSARY_PERMISSIONS.length
		);
		this.unnecessaryPermissions = Arrays.copyOf(
				DEFAULT_UNNECESSARY_PERMISSIONS, DEFAULT_UNNECESSARY_PERMISSIONS.length
		);
	}


	/**
	 * 指定されたホスト言語側のクラスフィールドを、
	 * XVCI準拠の外部変数プラグインへと変換するアダプタを生成します。
	 *
	 * @param field 対象フィールドへのリフレクションによるアクセスを提供するFieldインスタンス
	 */
	public FieldXvci1Adapter (Field field) {
		this.field = field;
		this.objectInstance = null;

		this.necessaryPermissions = Arrays.copyOf(
				DEFAULT_NECESSARY_PERMISSIONS, DEFAULT_NECESSARY_PERMISSIONS.length
		);
		this.unnecessaryPermissions = Arrays.copyOf(
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
	 * この変数のデータの読み書きに必要な全てのパーミッションを、配列にまとめて設定します。
	 *
	 * このメソッドで設定される必要パーミッション配列と、
	 * {@link ExternalFunctionConnector1#setNesessaryParmissions setUnnesessaryParmissions}
	 * メソッドで設定される不要パーミッション配列において、重複している要素がある場合は、
	 * 前者の方が優先されます（つまり、そのパーミッションは必要と判断されます）。
	 *
	 * なお、このメソッドの引数に、
	 * {@link ExternalParmission#ALL ExternalParmission.NONE}
	 * のみを格納する配列を渡す事で、全てのパーミッションが不要となります。
	 * ただし、そのような事は、
	 * この関数が一切のシステムリソースやネットワークにアクセスしない場合など、
	 * スクリプト内で閉じた処理と同等以上のセキュリティが確保されている場合のみ行ってください。
	 *
	 * @param necessaryPermissions 必要なパーミッションを格納する配列
	 */
	public void setNecessaryPermissions(String[] necessaryPermissions) {
		this.necessaryPermissions = necessaryPermissions;
	}


	/**
	 * この変数のデータの読み書きに必要な全てのパーミッションを、配列にまとめて返します。
	 *
	 * デフォルトでは、パーミッションが不要である事を意味する
	 * { {@link ExternalPermission#NONE ExternalPermission.NONE}
	 * が返されます。
	 *
	 * @return 必要なパーミッションを格納する配列
	 */
	public String[] getNecessaryParmissions() {
		return this.necessaryPermissions;
	}


	/**
	 * この変数のデータの読み書きに不要な全てのパーミッションを、配列にまとめて設定します。
	 *
	 * このメソッドで設定される不要パーミッション配列と、
	 * {@link ExternalFunctionConnector1#getNesessaryParmissions getNesessaryParmissions}
	 * メソッドで設定される必要パーミッション配列において、重複している要素がある場合は、
	 * 後者の方が優先されます（つまり、そのパーミッションは必要と判断されます）。
	 *
	 * なお、このメソッドの引数に
	 * {@link ExternalParmission#ALL ExternalParmission.ALL} のみを格納する配列を返す事で、
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
	 * @param unnecessaryPermissions 不要なパーミッションを格納する配列
	 */
	public void setUnnecessaryPermissions(String[] unnecessaryPermissions) {
		this.unnecessaryPermissions = unnecessaryPermissions;
	}


	/**
	 * この変数のデータの読み書きに不要な全てのパーミッションを、配列にまとめて取得します。
	 *
	 * デフォルトでは、パーミッションが不要である事を意味する
	 * { {@link ExternalPermission#NONE ExternalPermission.NONE}
	 * が返されます。
	 *
	 * @return 不要なパーミッションを格納する配列
	 */
	public String[] getUnnecessaryParmissions() {
		return this.unnecessaryPermissions;
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
	public void setEngine(Object engineConnector) {
	}


	/**
	 * 変数のデータを取得します。
	 */
	public Object getData() throws ExternalVariableException {
		try {
			return this.field.get(this.objectInstance);

		// アクセス修飾子などが原因で取得できない場合
		} catch (IllegalArgumentException illegalArgumentException) {
			throw new ExternalVariableException(
					objectInstance.getClass().getCanonicalName() + " class has no field named \"" + this.field.getName() + "\"",
					illegalArgumentException
			);

		// そもそもインスタンスが対象フィールドを持っていない場合
		} catch (IllegalAccessException illegalAccessException) {
			throw new ExternalVariableException(
					"The field \"" + this.field.getName() + "\" of " + objectInstance.getClass().getCanonicalName()
					+ " class is not accessable (probably it is private or protected).",
					illegalAccessException
			);
		}
	}


	/**
	 * データの自動変換が無効化されている場合において、変数のデータを取得します。
	 * このアダプタでは、この機能は使用されません。
	 *
	 * @return 変数のデータ
	 */
	public void getData(Object dataContainer) throws ExternalVariableException {
	}


	/**
	 * 変数のデータを設定します。
	 *
	 * @param data 変数のデータ
	 */
	public void setData(Object data) throws ExternalVariableException {
		try {
			this.field.set(this.objectInstance, data);

		// アクセス修飾子などが原因で設定できない場合
		} catch (IllegalArgumentException illegalArgumentException) {
			throw new ExternalVariableException(
					objectInstance.getClass().getCanonicalName() + " class has no field named \"" + this.field.getName() + "\"",
					illegalArgumentException
			);

		// そもそもインスタンスが対象フィールドを持っていない場合
		} catch (IllegalAccessException illegalAccessException) {
			throw new ExternalVariableException(
					"The field \"" + this.field.getName() + "\" of " + objectInstance.getClass().getCanonicalName()
					+ " class is not accessable (probably it is private or protected).",
					illegalAccessException
			);
		}
	}


	/**
	 * XVCIに定義されたスクリプト実行毎の初期化処理ですが、
	 * このアダプタでは不要なため何も行いません。
	 */
	public void initializeForScript() {
	}


	/**
	 * XVCIに定義されたスクリプト実行毎の終了時処理ですが、
	 * このアダプタでは不要なため何も行いません。
	 */
	public void finalizeForScript() {
	}


	/**
	 * XVCIに定義された処理系への接続時の初期化処理ですが、
	 * このアダプタでは不要なため何も行いません。
	 */
	public void initializeForConnection() {
	}


	/**
	 * XVCIに定義された処理系からの接続解除時の終了時処理ですが、
	 * このアダプタでは不要なため何も行いません。
	 */
	public void finalizeForDisconnection() {
	}

}
