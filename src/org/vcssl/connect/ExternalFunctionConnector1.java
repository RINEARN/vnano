/*
 * ==================================================
 * External Function Connector Interface 1 (XFCI 1)
 * ( for VCSSL / Vnano Plug-in Development )
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2017-2018 by RINEARN (Fumihiro Matsui)
 * ==================================================
 */

package org.vcssl.connect;

/**
 * <p>
 * XFCI 1 (External Function Connector Interface 1)
 * 形式の外部関数プラグインを開発するための、
 * プラグイン側のコネクター・インターフェースです。
 * </p>
 *
 * <p>
 * XFCI は新しいインターフェースであるため、
 * 現時点ではまだVCSSL処理系では対応していません。
 * ただし、VCSSLのサブセットであるVnano (VCSSL nano) の処理系では、
 * 開発順序の都合で世代が新しいため、既に XFCI (Gen1) に対応しています。
 * 時期は未定ですが、将来的にはVCSSL処理系も XFCI に対応する予定です。
 * </p>
 *
 * <p>
 * XFCI は、{@link GeneralProcessConnector2 GPCI 2}
 * における型制約の緩さやオーバーヘッドの大きさなどを解消するため、
 * GPCIよりも抽象化レイヤーの薄い外部関数プラグイン用インターフェースとして、
 * 新たに定義されたものです。
 * <br />
 * そのため、XFCI 準拠のプラグインが提供する外部関数は、
 * 一般の関数と同様に、スクリプト側での型システムによる保護や判別の対象となります。
 * これにより、引数には型検査が行われ、同じ関数名でも異なるシグネチャを持つ関数は共存する事ができます。
 * <br />
 * また、必要に応じて言語処理系-プラグイン間でのデータの自動変換を無効化し、
 * 処理系内部で使用されているデータコンテナを直接やり取りする事で、
 * 関数呼び出しのオーバーヘッドを軽減する機能などもサポートされています
 * (詳細は {@link ExternalFunctionConnector1#isDataConversionNecessary isDataConversionNecessary}
 * メソッドおよび
 * {@link ArrayDataContainer1 ArrayDataContainer1} クラスを参照してください)。
 * </p>
 *
 * <p>
 * 半面、XFCI 準拠の外部関数プラグインの開発は、
 * GPCI と比べれば煩雑となります。
 * そのため、簡素なプラグインを手短に開発するためには、GPCI の方が適しています。
 * <br />
 * ただし、現時点ではまだ GPCI と XFCI の両方をサポートしている処理系は存在しないため、
 * 単純に処理系が対応している方を使用する必要があります。
 * </p>
 *
 * <p>
 * 現時点で XFCI 1 準拠のプラグイン接続をサポートしている処理系は、以下の通りです:
 * </p>
 * 
 * <ul>
 *   <li>{@link org.vcssl.nano.VnanoEngine VnanoEngine} (パーミッション関連機能には未対応)</li>
 * </ul>
 * 
 * <p>
 * 将来的に XFCI 1 準拠のプラグイン接続をサポートする可能性がある処理系は、以下の通りです:
 * </p>
 * 
 * <ul>
 *   <li>RINEARN VCSSL Runtime</li>
 * </ul>
 * 
 * @author RINEARN (Fumihiro Matsui)
 */
public interface ExternalFunctionConnector1 {


	/** 動的ロード時などに処理系側から参照される、インターフェースの形式名（値は"XFCI"）です。 */
	public static String INTERFACE_TYPE = "XFCI";

	/** 動的ロード時などに処理系側から参照される、インターフェースの世代名（値は"1"）です。*/
	public static String INTERFACE_GENERATION = "1";


	/**
	 * 関数名を取得します。
	 *
	 * @return 関数名
	 */
	public abstract String getFunctionName();


	/**
	 * 全ての仮引数の名称情報を保持しており、
	 * {@link ExternalFunctionConnector1 getParameterNames}
	 * メソッドによって取得可能であるかどうかを返します。
	 *
	 * @return 全仮引数の名称を取得可能かどうか
	 */
	public boolean hasParameterNames();


	/**
	 * 全ての仮引数の名称が取得可能であれば、配列として取得します。
	 *
	 * @return 全ての仮引数の名称を格納する配列
	 */
	public abstract String[] getParameterNames();


	/**
	 * 全ての仮引数における、
	 * データの型を表すClassインスタンスを格納する配列を取得します。
	 *
	 * @return 全引数のデータ型のClassインスタンスを格納する配列
	 */
	public abstract Class<?>[] getParameterClasses(); // Object を指定すると any 的な挙動にする?


	/**
	 * 戻り値のデータの型を表すClassインスタンスを取得します。
	 *
	 * @return データ型のClassインスタンス
	 */
	public abstract Class<?> getReturnClass();


	/**
	 * 可変長引数であるかどうかを判定します。
	 *
	 * @return 可変長引数であればtrue
	 */
	public abstract boolean isVariadic(); // ジェネリックはプリプロセッサで処理するのでこのレイヤーでは考慮しないでいいはず -> そもそも型を問わずデータユニットそのまま受け取る事もできるからジェネリックなプラグイン関数は要らない


	/**
	 * データの自動変換が必要かどうかを返します。
	 *
	 * このメソッドがtrueを返すようにプラグインを実装すると、
	 * {@link ExternalFunctionConnector1#invoke invoke}
	 * メソッドでの引数や戻り値のやり取りに際して、
	 * プラグインを記述しているホスト言語と処理系内部との間でのデータの型変換などを、
	 * 処理系側が自動で行うようになります。
	 *
	 * 逆に、メソッドがfalseを返すようにプラグインを実装すると、
	 * 処理系側ではデータの変換は行われず、上述のような場面においては、
	 * 処理系依存のデータコンテナ
	 * （{@link ArrayDataContainer1 ArrayDataContainer1} 参照）
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
	 * この関数の実行に必要な全てのパーミッションを、配列にまとめて取得します。
	 *
	 * パーミッションベースのセキュリティレイヤ―を持たない処理系では、
	 * このメソッドは機能しません（呼び出されません）。
	 *
	 * このメソッドが返す必要パーミッション配列と、
	 * {@link ExternalFunctionConnector1#getNesessaryParmissions getUnnesessaryParmissions}
	 * メソッドが返す不要パーミッション配列において、重複している要素がある場合は、
	 * 前者の方が優先されます（つまり、そのパーミッションは必要と判断されます）。
	 *
	 * なお、このメソッドの戻り値に、
	 * {@link ExternalParmission#ALL ExternalParmission.NONE}
	 * のみを格納する配列を返す事で、全てのパーミッションが不要となります。
	 * ただし、そのような事は、
	 * この関数が一切のシステムリソースやネットワークにアクセスしない場合など、
	 * スクリプト内で閉じた処理と同等以上のセキュリティが確保されている場合のみ行ってください。
	 *
	 * @return 必要なパーミッションを格納する配列
	 */
	public abstract String[] getNecessaryParmissions();


	/**
	 * パーミッション設定ベースのセキュリティレイヤーを持つ処理系において、
	 * この関数の実行に不要な全てのパーミッションを、配列にまとめて取得します。
	 *
	 * パーミッションベースのセキュリティレイヤ―を持たない処理系では、
	 * このメソッドは機能しません（呼び出されません）。
	 *
	 * このメソッドが返す不要パーミッション配列と、
	 * {@link ExternalFunctionConnector1#getNesessaryParmissions getNesessaryParmissions}
	 * メソッドが返す必要パーミッション配列において、重複している要素がある場合は、
	 * 後者の方が優先されます（つまり、そのパーミッションは必要と判断されます）。
	 *
	 * なお、このメソッドの戻り値に
	 * {@link ExternalParmission#ALL ExternalParmission.ALL} のみを格納する配列を返す事で、
	 * 必要パーミッション配列に含まれているものを除いた、全てのパーミッションが不要となります。
	 * これは、将来的に新しいパーミッションが追加された場合に、
	 * そのパーミッションによって、この関数の実行が拒否される事を回避する事ができます。
	 *
	 * ただし、セキュリティが重要となる用途に使用するプラグインの開発においては、
	 * そのような事自体がそもそも好ましくない事に留意する必要があります。
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
	public abstract String[] getUnnecessaryParmissions();


	/**
	 * 関数を実行します。
	 *
	 * @param arguments 全ての実引数を格納する配列
	 * @return 実行結果の戻り値
	 * @throws ExternalFunctionException
	 * 		何らかの問題により、関数の実行を完了できなかった場合にスローします。
	 */
	public abstract Object invoke(Object[] arguments) throws ExternalFunctionException ;


	/**
	 * (これはホストアプリケーション側の役目では？)処理系への接続時に必要な初期化処理を行います。
	 */
	public abstract void initializeForConnection();


	/**
	 * (これはホストアプリケーション側の役目では？)処理系からの接続解除時に必要な終了時処理を行います。
	 */
	public abstract void finalizeForDisconnection();


	/**
	 * スクリプト実行毎の初期化処理を行います。
	 */
	public abstract void initializeForScript();


	/**
	 * (名前をfinalizeにするとまずいので、なんか考える必要がある)スクリプト実行毎の終了時処理を行います。
	 */
	public abstract void finalizeForScript();
}
