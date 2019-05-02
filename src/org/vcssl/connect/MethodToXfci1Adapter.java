/*
 * ==================================================
 * Method to XFCI Plug-in Adapter
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2017-2019 by RINEARN (Fumihiro Matsui)
 * ==================================================
 */

package org.vcssl.connect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;


/**
 * <p>
 * ホスト言語側のメソッドを、{@link org.vcssl.connect.ExternalFunctionConnector1 XFCI 1}
 * 形式の外部変数プラグイン仕様に変換し、XFCI 1 対応の言語処理系に接続するためのアダプタです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class MethodToXfci1Adapter implements ExternalFunctionConnector1 {


	/** デフォルトの必要パーミッション配列（値は { {@link ExternalPermission#NONE ExternalPermission.NONE} } ）です。 */
	private static final String[] DEFAULT_NECESSARY_PERMISSIONS = { ExternalPermission.NONE };

	/** デフォルトの不要パーミッション配列（値は { {@link ExternalPermission#ALL ExternalPermission.ALL} } ）です。 */
	private static final String[] DEFAULT_UNNECESSARY_PERMISSIONS = { ExternalPermission.ALL };


	/** ホスト言語側のメソッドへの、リフレクションによるアクセスを提供するMethodインスタンスです。 */
	private Method method = null;

	/** ホスト言語のメソッドが属するオブジェクトのインスタンスです。 */
	private Object objectInstance = null;

	/** 必要パーミッション配列です。 */
	private String[] necessaryPermissions = null;

	/** 不要パーミッション配列です。 */
	private String[] unnecessaryPermissions = null;


	/**
	 * 指定されたホスト言語側のインスタンスメソッドを、
	 * XFCI準拠の外部変数プラグインへと変換するアダプタを生成します。
	 *
	 * @param method 対象メソッドへのリフレクションによるアクセスを提供するMethodインスタンス
	 * @param objectInstance 対象メソッドが属するオブジェクトのインスタンス
	 */
	public MethodToXfci1Adapter(Method method, Object objectInstance) {
		this.method = method;
		this.objectInstance = objectInstance;

		this.necessaryPermissions = Arrays.copyOf(
				DEFAULT_NECESSARY_PERMISSIONS, DEFAULT_NECESSARY_PERMISSIONS.length
		);
		this.unnecessaryPermissions = Arrays.copyOf(
				DEFAULT_UNNECESSARY_PERMISSIONS, DEFAULT_UNNECESSARY_PERMISSIONS.length
		);
	}


	/**
	 * 指定されたホスト言語側のクラスメソッドを、
	 * XFCI準拠の外部変数プラグインへと変換するアダプタを生成します。
	 *
	 * @param method 対象メソッドへのリフレクションによるアクセスを提供するMethodインスタンス
	 */
	public MethodToXfci1Adapter(Method method) {
		this.method = method;
		this.objectInstance = null;

		this.necessaryPermissions = Arrays.copyOf(
				DEFAULT_NECESSARY_PERMISSIONS, DEFAULT_NECESSARY_PERMISSIONS.length
		);
		this.unnecessaryPermissions = Arrays.copyOf(
				DEFAULT_UNNECESSARY_PERMISSIONS, DEFAULT_UNNECESSARY_PERMISSIONS.length
		);
	}


	/**
	 * 関数名を取得します。
	 *
	 * @return 関数名
	 */
	@Override
	public String getFunctionName() {
		return this.method.getName();
	}


	/**
	 * 全ての仮引数の名称情報を保持しており、
	 * {@link ExternalFunctionConnector1 getParameterNames}
	 * メソッドによって取得可能であるかどうかを返しますが、
	 * このアダプタは常に false を返します。
	 *
	 * @return 全仮引数の名称を取得可能かどうか
	 */
	public boolean hasParameterNames() {
		return false;
	}


	/**
	 * 全ての仮引数の名称を配列として取得するメソッドですが、
	 * このアダプタではサポートしない（
	 * {@link MethodToXfci1Adapter#hasParameterNames hasParameterNames}
	 * の戻り値が false）ため常に null を返します。
	 *
	 * @return null
	 */
	@Override
	public String[] getParameterNames() {
		return null;
	}


	/**
	 * 全ての仮引数における、
	 * データの型を表すClassインスタンスを格納する配列を取得します。
	 *
	 * @return 全引数のデータ型のClassインスタンスを格納する配列
	 */
	@Override
	public Class<?>[] getParameterClasses() {
		return this.method.getParameterTypes();
	}


	/**
	 * 戻り値のデータの型を表すClassインスタンスを取得します。
	 *
	 * @return データ型のClassインスタンス
	 */
	@Override
	public Class<?> getReturnClass() {
		return this.method.getReturnType();
	}


	/**
	 * 可変長引数であるかどうかを判定します。
	 *
	 * @return 可変長引数であればtrue
	 */
	@Override
	public boolean isVariadic() {
		return this.method.isVarArgs();
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
	 * この関数の実行に必要な全てのパーミッションを、配列にまとめて設定します。
	 *
	 * このメソッドで設定される必要パーミッション配列と、
	 * {@link FieldToXvci1Adapter#setUnnecessaryPermissions setUnnesessaryPermissions}
	 * メソッドで設定される不要パーミッション配列において、重複している要素がある場合は、
	 * 前者の方が優先されます（つまり、そのパーミッションは必要と判断されます）。
	 *
	 * なお、このメソッドの引数に、
	 * {@link ExternalPermission#ALL ExternalPermission.NONE}
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
	 * この関数の実行に必要な全てのパーミッションを、配列にまとめて返します。
	 *
	 * デフォルトでは、パーミッションが不要である事を意味する
	 * { {@link ExternalPermission#NONE ExternalPermission.NONE}
	 * が返されます。
	 *
	 * @return 必要なパーミッションを格納する配列
	 */
	public String[] getNecessaryPermissions() {
		return this.necessaryPermissions;
	}


	/**
	 * この関数の実行に不要な全てのパーミッションを、配列にまとめて設定します。
	 *
	 * このメソッドで設定される不要パーミッション配列と、
	 * {@link FieldToXvci1Adapter#setNecessaryPermissions setNecessaryPermissions}
	 * メソッドで設定される必要パーミッション配列において、重複している要素がある場合は、
	 * 後者の方が優先されます（つまり、そのパーミッションは必要と判断されます）。
	 *
	 * なお、このメソッドの引数に
	 * {@link ExternalPermission#ALL ExternalPermission.ALL} のみを格納する配列を返す事で、
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
	 * この関数の実行に不要な全てのパーミッションを、配列にまとめて取得します。
	 *
	 * デフォルトでは、パーミッションが不要である事を意味する
	 * { {@link ExternalPermission#NONE ExternalPermission.NONE}
	 * が返されます。
	 *
	 * @return 不要なパーミッションを格納する配列
	 */
	public String[] getUnnecessaryPermissions() {
		return this.unnecessaryPermissions;
	}


	/**
	 * 関数を実行します。
	 *
	 * @param arguments 全ての実引数を格納する配列
	 */
	@Override
	public Object invoke(Object[] arguments) throws ConnectorException {
		try {
			return this.method.invoke(objectInstance, arguments);

		// アクセス修飾子などが原因で呼び出せない場合
		} catch (IllegalArgumentException illegalArgumentException) {
			throw new ConnectorException(
					objectInstance.getClass().getCanonicalName() + " class has no method named \"" + this.method.getName()
					+ "\" with expected parameters.",
					illegalArgumentException
			);

		// そもそもインスタンスが対象メソッドを持っていない場合
		} catch (IllegalAccessException illegalAccessException) {
			throw new ConnectorException(
					"The method \"" + this.method.getName() + "\" of " + objectInstance.getClass().getCanonicalName()
					+ " class is not accessable (probably it is private or protected).",
					illegalAccessException
			);

		// 呼び出し対象のメソッドが、実行中に内部から例外をスローしてきた場合
		} catch (InvocationTargetException invocationTargetException) {
			throw new ConnectorException(invocationTargetException);
		}
	}


	/**
	 * 処理系への接続時に必要な初期化処理を行います。
	 *
	 * 引数には、スクリプトエンジンに依存するやり取りを行うためのオブジェクトが渡されます。
	 * このオブジェクトは、恐らく {@link EngineConnector1 EngineConnector1}
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
	 * このオブジェクトは、恐らく {@link EngineConnector1 EngineConnector1}
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
	 * このオブジェクトは、恐らく {@link EngineConnector1 EngineConnector1}
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
	 * このオブジェクトは、恐らく {@link EngineConnector1 EngineConnector1}
	 * もしくはその後継の、抽象化されたインターフェースでラップされた形で渡されます。
	 *
	 * @param engineConnector エンジンに依存するやり取りを行うためのオブジェクト
	 * @throws ConnectorException 終了時処理に失敗した場合にスローされます。
	 */
	public void finalizeForTermination(Object engineConnector) throws ConnectorException {
	}

}
