/*
 * ==================================================
 * External Function Connector Interface 1 (XFCI 1)
 * ( for VCSSL / Vnano Plug-in Development )
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2017-2020 by RINEARN (Fumihiro Matsui)
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
 * <span style="font-weight: bold;">
 * ※ このインターフェースは未確定であり、
 * このインターフェースをサポートする処理系が正式にリリースされるまでの間、
 * 一部仕様が変更される可能性があります。
 * </span>
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
 * XFCI は、{@link GeneralProcessConnectorInterface2 GPCI 2}
 * における型制約の緩さやオーバーヘッドの大きさなどを解消するため、
 * GPCIよりも抽象化レイヤーの薄い外部関数プラグイン用インターフェースとして、
 * 新たに定義されたものです。
 * <br>
 * そのため、XFCI 準拠のプラグインが提供する外部関数は、
 * 一般の関数と同様に、スクリプト側での型システムによる保護や判別の対象となります。
 * これにより、引数には型検査が行われ、同じ関数名でも異なるシグネチャを持つ関数は共存する事ができます。
 * <br>
 * また、必要に応じて言語処理系-プラグイン間でのデータの自動変換を無効化し、
 * 処理系内部で使用されているデータコンテナを直接やり取りする事で、
 * 関数呼び出しのオーバーヘッドを軽減する機能などもサポートされています
 * (詳細は {@link ExternalFunctionConnectorInterface1#isDataConversionNecessary isDataConversionNecessary}
 * メソッドおよび
 * {@link ArrayDataContainerInterface1 ArrayDataContainerInterface1} クラスを参照してください)。
 * </p>
 *
 * <p>
 * 半面、XFCI 準拠の外部関数プラグインの開発は、
 * GPCI と比べれば煩雑となります。
 * そのため、簡素なプラグインを手短に開発するためには、GPCI の方が適しています。
 * <br>
 * ただし、現時点ではまだ GPCI と XFCI の両方をサポートしている処理系は存在しないため、
 * 単純に処理系が対応している方を使用する必要があります。
 * </p>
 *
 * <p>
 * 現時点で XFCI 1 準拠のプラグイン接続をサポートしている処理系は、以下の通りです:
 * </p>
 *
 * <ul>
 *   <li>RINEARN Vnano Engine</li>
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
public interface ExternalFunctionConnectorInterface1 {


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
	 * {@link ExternalFunctionConnectorInterface1 getParameterNames}
	 * メソッドによって取得可能であるかどうかを返します。
	 *
	 * @return 全仮引数の名称を取得可能かどうか
	 */
	public abstract boolean hasParameterNames();


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
	 * @return 各仮引数のデータ型のClassインスタンスを格納する配列
	 */
	public abstract Class<?>[] getParameterClasses();


	/**
	 * 全ての仮引数において、データ型が可変であるかどうかを格納する配列を返します。
	 *
	 * @return 各仮引数のデータ型が可変であるかどうかを格納する配列
	 */
	public boolean[] getParameterClassArbitrarinesses();


	/**
	 * 全ての仮引数において、配列次元数が可変であるかどうかを格納する配列を返します。
	 *
	 * @return 各仮引数の配列次元数が可変であるかどうかを格納する配列
	 */
	public boolean[] getParameterRankArbitrarinesses();


	/**
	 * 全ての仮引数において、参照渡しとみなすべきかどうかを格納する配列を返します。
	 *
	 * {@link ExternalFunctionConnectorInterface1#isDataConversionNecessary() isDataConversionNecessary()}
	 * が true を返すように実装した場合、呼び出し時の引数は、処理系依存のデータコンテナとして渡されますが、
	 * このメソッドが返す値により、その際の挙動が変化します。
	 *
	 * このメソッドが true を返すようにした引数については、呼び出し元の実引数のデータコンテナがそのまま渡されます。
	 * 従ってその場合、データコンテナのデータを書き換える事で、実引数の値に変更を反映させる事ができます。
	 * また、引数の受け渡しのオーバーヘッドコストが最小限に抑えられます。
	 * その反面、変数（配列やその要素を含む）以外を引数に渡す事はできなくなります。
	 *
	 * このメソッドが false を返すようにした引数については、呼び出し元の実引数のデータコンテナが、
	 * コピーされた上で渡されます。そのため、その値を書き変えても、実引数には反映されませんが、
	 * 変数以外（リテラル、定数、式の評価値など）を引数に渡す事が可能になります。
	 *
	 * @return 各仮引数が参照渡しであるかどうかを格納する配列
	 */
	public abstract boolean[] getParameterReferencenesses();


	/**
	 * 全ての仮引数において、定数であるかどうかを格納する配列を返します。
	 *
	 * {@link ExternalFunctionConnectorInterface1#isDataConversionNecessary() isDataConversionNecessary()}
	 * が true を返すように実装し、かつ
	 * {@link ExternalFunctionConnectorInterface1#getParameterReferencenesses() getParameterReferencenesses()}
	 * が true を返すように実装した場合、
	 * 通常は変数（配列やその要素を含む）以外を引数に渡す事はできなくなります。
	 *
	 * これは、参照渡しを通じて呼び出し元の値が書き変わるような引数に、
	 * スクリプト内で、リテラルなどの書き換え不可能な値を誤って渡そうとしている箇所を、
	 * 実行前にエラーとして検出するための仕様です。
	 *
	 * そこでこのメソッドで true を返すように実装すると、「渡された引数の値を書き換えない」
	 * と宣言した事になり、変数以外も引数として渡せるようになります。
	 * ただしその場合は実際に、受け取った引数の値を書き換えてはいけません。
	 *
	 * @return 各仮引数が定数であるかどうかを格納する配列
	 */
	public abstract boolean[] getParameterConstantnesses();


	/**
	 * <p>
	 * 仮引数の個数が任意であるかどうかを返します。
	 * </p>
	 *
	 * <p>
	 * この機能を有効にすると、呼び出しにおいて、実引数が 1 個から任意個まで許容されるようになります。
	 * この機能を有効にした場合は、{@link getParameterClasses() } や
	 * {@link getParameterRanks() } などでは引数1個分の仕様を指定してください。
	 * 全ての実引数が、その仕様と適合する場合に、呼び出し可能であると判断されます。
	 * 呼び出しの際は、{@link invoke(Object[] arguments)} メソッドの引数 arguments に、
	 * 任意個の実引数がそのままの形で渡されます。
	 * </p>
	 *
	 * <p>
	 * なお、この機能を有効にした際の挙動は、{@link hasVariadicParameters() }
	 * を有効にした際の挙動とは少し異なる事に留意してください。
	 * 後者では、引数の型の適合性判断は、VCSSLの可変長引数の仕様に基づいて、
	 * {@link invoke(Object[] arguments)} メソッドへの実引数の渡され方も異なります。
	 * </p>
	 *
	 * @return 各仮引数の個数が任意であるかどうか
	 */
	public boolean isParameterCountArbitrary();


	/**
	 * <p>
	 * （この機能は、処理系側ではまだ対応していません）可変長引数であるかを判定します。
	 * </p>
	 *
	 * <p>
	 * この機能を有効にすると、仮引数の中で最後の要素が、可変長要素であると判断されます。
	 * 可変長要素以外の引数に対しては、通常の関数通り、型や並び方の適合性検査が行われます。
	 * 可変長要素の引数に対しては、複数の同じ型のスカラーか、または1個の配列を渡す事が許容されます。
	 * 呼び出し時の実引数においては、可変長要素の引数のデータは 1 個の配列にまとめて渡されます。
	 * {@link invoke(Object[] arguments)} メソッドの引数 arguments においても同様です。
	 * </p>
	 *
	 * @return 可変長引数であればtrue
	 */
	public abstract boolean hasVariadicParameters();


	/**
	 * 戻り値のデータの型を表すClassインスタンスを取得します。
	 *
	 * parameterClasses には、スクリプト内での呼び出しにおける、引数のデータ型情報が渡されます。
	 * これにより、引数の型によって戻り値の型が異なるだけの、
	 * 複数の関数に相当する処理を、まとめて提供する事ができます。
	 *
	 * @param parameterClasses 全引数のデータ型のClassインスタンスを格納する配列
	 * @return 戻り値のデータ型のClassインスタンス
	 */
	public abstract Class<?> getReturnClass(Class<?>[] parameterClasses);


	/**
	 * データの自動変換が必要かどうかを返します。
	 *
	 * このメソッドがtrueを返すようにプラグインを実装すると、
	 * {@link ExternalFunctionConnectorInterface1#invoke invoke}
	 * メソッドでの引数や戻り値のやり取りに際して、
	 * プラグインを記述しているホスト言語と処理系内部との間でのデータの型変換などを、
	 * 処理系側が自動で行うようになります。
	 *
	 * 逆に、メソッドがfalseを返すようにプラグインを実装すると、
	 * 処理系側ではデータの変換は行われず、上述のような場面においては、
	 * 処理系依存のデータコンテナ
	 * （{@link ArrayDataContainerInterface1 ArrayDataContainerInterface1} 参照）
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
	 * 関数を実行します。
	 *
	 * データの自動変換が無効である場合には、
	 * 引数は、処理系依存のデータコンテナオブジェクトとして渡されます。
	 * その場合、関数の戻り値は、このメソッドの戻り値として返す代わりに、
	 * 最初の引数のデータコンテナオブジェクトに格納してください。
	 *
	 * @param arguments 全ての実引数を格納する配列（データの自動変換が無効の場合、最初の要素は戻り値格納用）
	 * @return 実行結果の戻り値
	 * @throws ConnectorException 何らかの問題により、関数の実行を完了できなかった場合にスローします。
	 */
	public abstract Object invoke(Object[] arguments) throws ConnectorException ;


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
	public abstract void initializeForConnection(Object engineConnector) throws ConnectorException;


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
	public abstract void finalizeForDisconnection(Object engineConnector) throws ConnectorException;


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
	public abstract void initializeForExecution(Object engineConnector) throws ConnectorException;


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
	public abstract void finalizeForTermination(Object engineConnector) throws ConnectorException;

}
