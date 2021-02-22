/*
 * ==================================================
 * Class to XLCI Plug-in Adapter
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2019-2021 by RINEARN (Fumihiro Matsui)
 * ==================================================
 */

package org.vcssl.connect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
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

	/** ホスト言語側のクラスです。 */
	private Class<?> pluginClass = null;

	/** ホスト言語のメソッドが属するオブジェクトのインスタンスです。 */
	private Object pluginInstance = null;


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
	}


	/**
	 * 指定されたホスト言語側のクラスを、staticメソッドやstaticフィールドのみの範囲で、
	 * XNCI準拠の外部変数プラグインへと変換するアダプタを生成します。
	 *
	 * @param pluginClass 対象クラス
	 */
	public ClassToXnci1Adapter(Class<?> pluginClass) {
		this.pluginClass = pluginClass;
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
	 * スクリプト内で、この名前空間に属する関数/変数にアクセスする際に、名前空間の指定が必須かどうかを返します。
	 *
	 * なお、このメソッドに対応する機能は、処理系によってはサポートされません。
	 * サポートされていない処理系では、恐らく名前空間は常に省略可能（つまりこのメソッドの値が false であるのと同等）となります。
	 * そのため現状では、特に必要性がなければ、このメソッドは false を返すように実装する事が推奨されます。
	 * このメソッドは将来的には有用になる可能性があるため、予約的に定義されています。
	 *
	 * @return 名前空間の明示指定が必須である場合は true、省略可能な場合は false
	 */
	@Override
	public boolean isMandatoryToAccessMembers() {
		return false;
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
		List<ExternalFunctionConnectorInterface1> xfciList = new ArrayList<ExternalFunctionConnectorInterface1>();

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
		List<ExternalVariableConnectorInterface1> xvciList = new ArrayList<ExternalVariableConnectorInterface1>();

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
	 * この名前空間に属する全ての構造体を、配列にまとめて返します。
	 *
	 * @return この名前空間に属する構造体をまとめた配列
	 */
	public ExternalStructConnectorInterface1[] getStructs() {
		return new ExternalStructConnectorInterface1[0];
	}


	// 以下、インターフェースで定義されている名前空間レベルでの初期化/終了時処理
	// （このクラスでは特に何もしない）

	@Override
	public void preInitializeForConnection(Object engineConnector) throws ConnectorException {
	}

	@Override
	public void postInitializeForConnection(Object engineConnector) throws ConnectorException {
	}

	@Override
	public void preFinalizeForDisconnection(Object engineConnector) throws ConnectorException {
	}

	@Override
	public void postFinalizeForDisconnection(Object engineConnector) throws ConnectorException {
	}

	@Override
	public void preInitializeForExecution(Object engineConnector) throws ConnectorException {
	}

	@Override
	public void postInitializeForExecution(Object engineConnector) throws ConnectorException {
	}

	@Override
	public void preFinalizeForTermination(Object engineConnector) throws ConnectorException {
	}

	@Override
	public void postFinalizeForTermination(Object engineConnector) throws ConnectorException {
	}
}
