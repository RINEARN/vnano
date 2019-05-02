/*
 * ==================================================
 * External Variable Connector Interface 1 (XVCI 1)
 * ( for VCSSL / Vnano Plug-in Development )
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2017-2019 by RINEARN (Fumihiro Matsui)
 * ==================================================
 */

package org.vcssl.connect;

/**
 * <p>
 * XVCI 1 (External Variable Connector Interface 1)
 * 形式の外部変数プラグインを開発するための、
 * プラグイン側のコネクター・インターフェースです。
 * </p>
 *
 * <p>
 * <span style="font-weight: bold;">
 * ※ このインターフェースは未確定であり、
 * このインターフェースをサポートする処理系が正式にリリースされるまでの間、
 * 一部仕様が変更される可能性があります。
 * </span>
 * </p>
 *
 * <p>
 * XVCI 1 では、外部関数プラグイン用のインターフェースである
 * XFCI 1 ({@link ExternalFunctionConnector1 External Function Connector Interface Gen.1})
 * ベースとしています。
 * <br>
 * XFCI と同様に、XVCI は現時点ではまだVCSSL処理系では対応していませんが、
 * VCSSLのサブセットであるVnano (VCSSL nano) の処理系では、
 * 開発順序の都合で世代が新しいため、既に XVCI (Gen.1) に対応しています。
 * 時期は未定ですが、将来的にはVCSSL処理系も XFCI に対応する予定です。
 * </p>
 *
 * <p>
 * 現時点で XVCI 1 準拠のプラグイン接続をサポートしている処理系は、以下の通りです:
 * </p>
 *
 * <ul>
 *   <li>RINEARN Vnano Engine (パーミッション関連機能には未対応)</li>
 * </ul>
 *
 * <p>
 * 将来的に XVCI 1 準拠のプラグイン接続をサポートする可能性がある処理系は、以下の通りです:
 * </p>
 *
 * <ul>
 *   <li>RINEARN VCSSL Runtime</li>
 * </ul>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public interface ExternalVariableConnector1 {


	/** 動的ロード時などに処理系側から参照される、インターフェースの形式名（値は"XVCI"）です。 */
	public static String INTERFACE_TYPE = "XVCI";

	/** 動的ロード時などに処理系側から参照される、インターフェースの世代名（値は"1"）です。*/
	public static String INTERFACE_GENERATION = "1";


	/**
	 * 変数名を取得します。
	 *
	 * @return 変数名
	 */
	public abstract String getVariableName();


	/**
	 * 変数のデータの型を表すClassインスタンスを取得します。
	 *
	 * @return データ型のClassインスタンス
	 */
	public abstract Class<?> getDataClass();


	/**
	 * 書き換え不可能な定数であるかどうかを判定します。
	 *
	 * @return 定数であればtrue
	 */
	public abstract boolean isConstant();


	/**
	 * データの自動変換が必要かどうかを返します。
	 *
	 * このメソッドがtrueを返すようにプラグインを実装すると、
	 * {@link ExternalVariableConnector1#getData getData} メソッドや
	 * {@link ExternalVariableConnector1#setData setData}
	 * メソッドでのデータのやり取りに際して、
	 * プラグインを記述しているホスト言語と処理系内部との間でのデータの型変換などを、
	 * 処理系側が自動で行うようになります。
	 *
	 * 逆に、メソッドがfalseを返すようにプラグインを実装すると、
	 * 処理系側ではデータの変換は行われず、上述のような場面においては、
	 * 処理系依存のデータコンテナ
	 * （{@link org.vcssl.connect.ArrayDataContainer1 ArrayDataContainer1} 参照）
	 * を直接やり取りするようになります。
	 *
	 * データの自動変換を利用すると、プラグインの実装が容易になりますが、
	 * 変換のオーバーヘッドがネックになる可能性があります。
	 * 自動変換を利用しない場合は、オーバーヘッドを大幅に削れる代わりに、
	 * プラグインの実装が複雑化する可能性があります。
	 *
	 * @return 自動変換が必要ならtrue
	 */
	public abstract boolean isDataConversionNecessary();


	/**
	 * パーミッション設定ベースのセキュリティレイヤーを持つ処理系において、
	 * この変数のデータの読み書きに必要な全てのパーミッションを、配列にまとめて取得します。
	 *
	 * パーミッションベースのセキュリティレイヤ―を持たない処理系では、
	 * このメソッドは機能しません（呼び出されません）。
	 *
	 * このメソッドが返す必要パーミッション配列と、
	 * {@link ExternalFunctionConnector1#getUnnecessaryPermissions getUnnecessaryPermissions}
	 * メソッドが返す不要パーミッション配列において、重複している要素がある場合は、
	 * 前者の方が優先されます（つまり、そのパーミッションは必要と判断されます）。
	 *
	 * なお、このメソッドの戻り値に、
	 * {@link ConnectorPermission#NONE ConnectorPermission.NONE}
	 * のみを格納する配列を返す事で、全てのパーミッションが不要となります。
	 * ただし、そのような事は、
	 * この関数が一切のシステムリソースやネットワークにアクセスしない場合など、
	 * スクリプト内で閉じた処理と同等以上のセキュリティが確保されている場合のみ行ってください。
	 *
	 * @return 必要なパーミッションを格納する配列
	 */
	public abstract String[] getNecessaryPermissions();


	/**
	 * パーミッション設定ベースのセキュリティレイヤーを持つ処理系において、
	 * この変数のデータの読み書きに不要な全てのパーミッションを、配列にまとめて取得します。
	 *
	 * パーミッションベースのセキュリティレイヤ―を持たない処理系では、
	 * このメソッドは機能しません（呼び出されません）。
	 *
	 * このメソッドが返す不要パーミッション配列と、
	 * {@link ExternalFunctionConnector1#getNecessaryPermissions getNecessaryPermissions}
	 * メソッドが返す必要パーミッション配列において、重複している要素がある場合は、
	 * 後者の方が優先されます（つまり、そのパーミッションは必要と判断されます）。
	 *
	 * なお、このメソッドの戻り値に
	 * {@link ConnectorPermission#ALL ConnectorPermission.ALL} のみを格納する配列を返す事で、
	 * 必要パーミッション配列に含まれているものを除いた、全てのパーミッションが不要となります。
	 * これは、将来的に新しいパーミッションが追加された場合に、
	 * そのパーミッションによって、この関数の実行が拒否される事を回避する事ができます。
	 *
	 * ただし、セキュリティが重要となる用途に使用するプラグインの開発においては、
	 * そのような事自体がそもそも好ましくない事に注意する必要があります。
	 * そのようなセキュリティ重要度の高い用途に向けたプラグインの開発に際しては、
	 * 開発時点で存在する個々のパーミッションについて、
	 * 不要である事が判明しているものだけを返すようにしてください。
	 *
	 * そうすれば、必要・不要のどちらにも含まれない、
	 * 開発時点で未知のパーミッションの扱いについては、
	 * 処理系側やユーザー側の判断に委ねる事ができます。
	 *
	 * @return 不要なパーミッションを格納する配列
	 */
	public abstract String[] getUnnecessaryPermissions();


	/**
	 * 変数のデータを取得します。
	 *
	 * このメソッドは、データの自動変換が有効である場合、
	 * つまり {@link ExternalVariableConnector1#isDataConversionNecessary()} メソッドが
	 * true を返すよう実装されている場合に使用されます。
	 *
	 * @return 変数のデータ
	 * @throws ConnectorException 何らかの問題により、データへのアクセスが行えない場合にスローします。
	 */
	public abstract Object getData() throws ConnectorException;


	/**
	 * 変数のデータを取得します。
	 *
	 * このメソッドは、データの自動変換が無効である場合、
	 * つまり {@link ExternalVariableConnector1#isDataConversionNecessary()} メソッドが
	 * false を返すよう実装されている場合に使用されます。
	 *
	 * データは、戻り値として返す代わりに、
	 * 引数に渡される処理系依存のデータコンテナオブジェクトに格納してください。
	 *
	 * @param dataContainer データを格納する、処理系依存のデータコンテナオブジェクト
	 * @throws ConnectorException 何らかの問題により、データへのアクセスが行えない場合にスローします。
	 */
	public abstract void getData(Object dataContainer) throws ConnectorException;


	/**
	 * 変数のデータを設定します。
	 *
	 * データの自動変換が有効である場合、
	 * つまり {@link ExternalVariableConnector1#isDataConversionNecessary()} メソッドが
	 * true を返すよう実装されている場合には、引数には適切に型変換されたデータが渡されます。
	 *
	 * データの自動変換が無効である場合には、引数には処理系依存のデータコンテナオブジェクトが渡されます。
	 *
	 * @param data 変数のデータ
	 * @throws ConnectorException 何らかの問題により、データへのアクセスが行えない場合にスローします。
	 */
	public abstract void setData(Object data) throws ConnectorException;


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
	public abstract void initializeForConnection(Object engineConnector) throws ConnectorException;


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
	public abstract void finalizeForDisconnection(Object engineConnector) throws ConnectorException;


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
	public abstract void initializeForExecution(Object engineConnector) throws ConnectorException;


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
	public abstract void finalizeForTermination(Object engineConnector) throws ConnectorException;

}
