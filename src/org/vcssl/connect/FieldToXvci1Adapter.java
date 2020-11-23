/*
 * ==================================================
 * Field to XVCI Plug-in Adapter
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2017-2020 by RINEARN (Fumihiro Matsui)
 * ==================================================
 */

package org.vcssl.connect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;


/**
 * <p>
 * ホスト言語側のフィールドを、{@link org.vcssl.connect.ExternalVariableConnectorInterface1 XVCI 1}
 * 形式の外部変数プラグイン仕様に変換し、XVCI 1 対応の言語処理系に接続するためのアダプタです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class FieldToXvci1Adapter implements ExternalVariableConnectorInterface1 {

	/** ホスト言語側のフィールドへの、リフレクションによるアクセスを提供するFieldインスタンスです。 */
	private Field field = null;

	/** ホスト言語のフィールドが属するオブジェクトのインスタンスです。 */
	private Object objectInstance = null;


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
	}


	/**
	 * 変数名を取得します。
	 *
	 * @return 変数名
	 */
	@Override
	public String getVariableName() {
		return this.field.getName();
	}


	/**
	 * 変数のデータの型を表すClassインスタンスを取得します。
	 *
	 * @return データ型のClassインスタンス
	 */
	@Override
	public Class<?> getDataClass() {
		return this.field.getType();
	}


	/**
	 * データの自動変換を無効化している場合
	 * ({@link ExternalFunctionConnectorInterface1#isDataConversionNecessary()} が false を返す場合)
	 * において、データのやり取りに使用するデータコンテナの型を表すClassインスタンスを取得します。
	 *
	 * ただし、この実装では上記メソッドは true を返すため、このメソッドの戻り値は参照されません。
	 *
	 * @return データのやり取りに使用するデータコンテナの型を表すClassインスタンス
	 */
	@Override
	public Class<?> getDataUnconvertedClass() {
		return null;
	}


	/**
	 * 書き換え不可能な定数であるかどうかを判定します。
	 *
	 * @return 定数であればtrue
	 */
	@Override
	public boolean isConstant() {
		return Modifier.isFinal(field.getModifiers());
	}


	/**
	 * この変数が、別の変数の参照であるかどうかを返します。
	 *
	 * 現在の処理系では、この機能は言語仕様においてサポートされていませんが、
	 * 将来的な拡張の可能性を考慮して、予約的に定義されています。
	 * そのため、現状では常に false を返します。
	 *
	 * @return 参照であれば true
	 */
	public boolean isReference() {
		return false;
	}


	/**
	 * データ型が可変であるかどうかを返します。
	 *
	 * 現在の処理系では、この機能は言語仕様においてサポートされていませんが、
	 * 将来的な拡張の可能性を考慮して、予約的に定義されています。
	 * そのため、現状では常に false を返します。
	 *
	 * @return データ型が可変であれば true
	 */
	public boolean isDataClassArbitrary() {
		return false;
	}


	/**
	 * 配列次元数が可変であるかどうかを返します。
	 *
	 * 現在の処理系では、この機能は言語仕様においてサポートされていませんが、
	 * 将来的な拡張の可能性を考慮して、予約的に定義されています。
	 * そのため、現状では常に false を返します。
	 *
	 * @return 配列次元数が可変であれば true
	 */
	public boolean isDataRankArbitrary() {
		return false;
	}


	/**
	 * データの自動変換が必要かどうかを返します。
	 * このアダプタではデータ変換が必須であるため、常にtrueを返します。
	 *
	 * @return 常にtrue
	 */
	@Override
	public boolean isDataConversionNecessary() {
		return true;
	}


	/**
	 * 変数のデータを取得します。
	 */
	@Override
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
	@Override
	public void getData(Object dataContainer) throws ConnectorException {
	}


	/**
	 * 変数のデータを設定します。
	 *
	 * @param data 変数のデータ
	 */
	@Override
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
	@Override
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
	@Override
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
	@Override
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
	@Override
	public void finalizeForTermination(Object engineConnector) throws ConnectorException {
	}

}
