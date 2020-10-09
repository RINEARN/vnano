/*
 * ==================================================
 * External Namespace Connector Interface 1 (XNCI 1)
 * ( for VCSSL / Vnano Plug-in Development )
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2019-2020 by RINEARN (Fumihiro Matsui)
 * ==================================================
 */

package org.vcssl.connect;

/**
 * <p>
 * XNCI 1 (External Namespace Connector Interface 1) 形式のプラグインを開発するための、
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
 * XNCIは、関数や変数の集合を、共通の名前空間に属する形で1つにまとめ、
 * いわゆるモジュラープログラミングにおけるモジュールを構成するために用います。
 * つまり、このインターフェースの抽象化対象は「固有の名前空間を持つモジュール」です。
 * ただし、「モジュール」という語が指す概念や粒度の大きさは、プログラミング言語や文脈によって大きく異なるため、
 * 混乱を避ける目的で、このインターフェースの名称はModuleという語を含まないように命名されました。
 * 代わりに、集合の単位を区切るという機能面を重視して、Namespaceの語が採用されました。
 * （ ただし、概念上は必ずしもモジュールと名前空間の単位は一致する必要は無いため、
 * 将来的には、このインターフェースの子要素または親要素として、
 * さらにモジュール相当のインターフェースが新設される可能性もあり得ます。 ）
 * </p>
 *
 * <p>
 * XNCI 1 では、関数に {@link ExternalFunctionConnectorInterface1 XFCI 1} 形式、
 * 変数に {@link ExternalVariableConnectorInterface1 XVCI 1} 形式のインターフェースを採用しています。
 * それらの形式で実装された関数/変数プラグインの集合（配列）を、この名前空間に属するものとして保持し、
 * それぞれ {@link ExternalNamespaceConnectorInterface1#getFunctions() getFunctions()} メソッドおよび
 * {@link ExternalNamespaceConnectorInterface1#getVariables() getVariables()} メソッドの戻り値として、
 * 処理系に提供します。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public interface ExternalNamespaceConnectorInterface1 {


	/** 動的ロード時などに処理系側から参照される、インターフェースの形式名（値は"XNCI"）です。 */
	public static final String INTERFACE_TYPE = "XNCI";

	/** 動的ロード時などに処理系側から参照される、インターフェースの世代名（値は"1"）です。*/
	public static final String INTERFACE_GENERATION = "1";


	/**
	 * 名前空間の名称を取得します。
	 *
	 * @return 名前空間の名称
	 */
	public abstract String getNamespaceName();


	/**
	 * スクリプト内で、この名前空間に属する関数/変数に対して、名前空間を省略してアクセスできるかどうかを返します。
	 *
	 * なお、このメソッドによる省略可能性の選択機能は、処理系によってはサポートされません。
	 * サポートされていない処理系では、恐らく名前空間は常に省略可能（つまりこのメソッドの値が true であるのと同等）となります。
	 * そのため現状では、特に必要性がなければ、このメソッドは true を返すように実装する事が推奨されます。
	 * このメソッドは将来的には有用になる可能性があるため、予約的に定義されています。
	 *
	 * @return 名前空間を省略可能な場合は true、常に完全名でアクセスする必要がある場合は false
	 */
	public abstract boolean isAbbreviatable();


	/**
	 * この名前空間に属する全ての関数を、配列にまとめて返します。
	 *
	 * @return この名前空間に属する関数をまとめた配列
	 */
	public abstract ExternalFunctionConnectorInterface1[] getFunctions();


	/**
	 * この名前空間に属する全ての変数を、配列にまとめて返します。
	 *
	 * @return この名前空間に属する変数をまとめた配列
	 */
	public abstract ExternalVariableConnectorInterface1[] getVariables();


	/**
	 * 処理系への接続時に必要な初期化処理を行います。
	 * この処理は、この名前空間に属する全ての変数/関数の初期化処理よりも前に呼び出されます。
	 *
	 * 引数には、スクリプトエンジンに依存するやり取りを行うためのオブジェクトが渡されます。
	 * このオブジェクトは、恐らく {@link EngineConnectorInterface1 EngineConnectorInterface1}
	 * もしくはその後継の、抽象化されたインターフェースでラップされた形で渡されます。
	 *
	 * @param engineConnector エンジンに依存するやり取りを行うためのオブジェクト
	 * @throws ConnectorException 初期化処理に失敗した場合にスローされます。
	 */
	public abstract void preInitializeForConnection(Object engineConnector) throws ConnectorException;


	/**
	 * 処理系への接続時に必要な初期化処理を行います。
	 * この処理は、この名前空間に属する全ての変数/関数の初期化処理よりも後に呼び出されます。
	 *
	 * 引数には、スクリプトエンジンに依存するやり取りを行うためのオブジェクトが渡されます。
	 * このオブジェクトは、恐らく {@link EngineConnectorInterface1 EngineConnectorInterface1}
	 * もしくはその後継の、抽象化されたインターフェースでラップされた形で渡されます。
	 *
	 * @param engineConnector エンジンに依存するやり取りを行うためのオブジェクト
	 * @throws ConnectorException 初期化処理に失敗した場合にスローされます。
	 */
	public abstract void postInitializeForConnection(Object engineConnector) throws ConnectorException;


	/**
	 * 処理系からの接続解除時に必要な終了時処理を行います。
	 * この処理は、この名前空間に属する全ての変数/関数の終了時処理よりも前に呼び出されます。
	 *
	 * 引数には、スクリプトエンジンに依存するやり取りを行うためのオブジェクトが渡されます。
	 * このオブジェクトは、恐らく {@link EngineConnectorInterface1 EngineConnectorInterface1}
	 * もしくはその後継の、抽象化されたインターフェースでラップされた形で渡されます。
	 *
	 * @param engineConnector エンジンに依存するやり取りを行うためのオブジェクト
	 * @throws ConnectorException 終了時処理に失敗した場合にスローされます。
	 */
	public abstract void preFinalizeForDisconnection(Object engineConnector) throws ConnectorException;


	/**
	 * 処理系からの接続解除時に必要な終了時処理を行います。
	 * この処理は、この名前空間に属する全ての変数/関数の終了時処理よりも後に呼び出されます。
	 *
	 * 引数には、スクリプトエンジンに依存するやり取りを行うためのオブジェクトが渡されます。
	 * このオブジェクトは、恐らく {@link EngineConnectorInterface1 EngineConnectorInterface1}
	 * もしくはその後継の、抽象化されたインターフェースでラップされた形で渡されます。
	 *
	 * @param engineConnector エンジンに依存するやり取りを行うためのオブジェクト
	 * @throws ConnectorException 終了時処理に失敗した場合にスローされます。
	 */
	public abstract void postFinalizeForDisconnection(Object engineConnector) throws ConnectorException;


	/**
	 * スクリプト実行毎の初期化処理を行います。
	 * この処理は、この名前空間に属する全ての変数/関数の初期化処理よりも前に呼び出されます。
	 *
	 * 引数には、スクリプトエンジンに依存するやり取りを行うためのオブジェクトが渡されます。
	 * このオブジェクトは、恐らく {@link EngineConnectorInterface1 EngineConnectorInterface1}
	 * もしくはその後継の、抽象化されたインターフェースでラップされた形で渡されます。
	 *
	 * @param engineConnector エンジンに依存するやり取りを行うためのオブジェクト
	 * @throws ConnectorException 初期化処理に失敗した場合にスローされます。
	 */
	public abstract void preInitializeForExecution(Object engineConnector) throws ConnectorException;


	/**
	 * スクリプト実行毎の初期化処理を行います。
	 * この処理は、この名前空間に属する全ての変数/関数の初期化処理よりも後に呼び出されます。
	 *
	 * 引数には、スクリプトエンジンに依存するやり取りを行うためのオブジェクトが渡されます。
	 * このオブジェクトは、恐らく {@link EngineConnectorInterface1 EngineConnectorInterface1}
	 * もしくはその後継の、抽象化されたインターフェースでラップされた形で渡されます。
	 *
	 * @param engineConnector エンジンに依存するやり取りを行うためのオブジェクト
	 * @throws ConnectorException 初期化処理に失敗した場合にスローされます。
	 */
	public abstract void postInitializeForExecution(Object engineConnector) throws ConnectorException;


	/**
	 * スクリプト実行毎の終了時処理を行います。
	 * この処理は、この名前空間に属する全ての変数/関数の終了時処理よりも前に呼び出されます。
	 *
	 * 引数には、スクリプトエンジンに依存するやり取りを行うためのオブジェクトが渡されます。
	 * このオブジェクトは、恐らく {@link EngineConnectorInterface1 EngineConnectorInterface1}
	 * もしくはその後継の、抽象化されたインターフェースでラップされた形で渡されます。
	 *
	 * @param engineConnector エンジンに依存するやり取りを行うためのオブジェクト
	 * @throws ConnectorException 終了時処理に失敗した場合にスローされます。
	 */
	public abstract void preFinalizeForTermination(Object engineConnector) throws ConnectorException;


	/**
	 * スクリプト実行毎の終了時処理を行います。
	 * この処理は、この名前空間に属する全ての変数/関数の終了時処理よりも後に呼び出されます。
	 *
	 * 引数には、スクリプトエンジンに依存するやり取りを行うためのオブジェクトが渡されます。
	 * このオブジェクトは、恐らく {@link EngineConnectorInterface1 EngineConnectorInterface1}
	 * もしくはその後継の、抽象化されたインターフェースでラップされた形で渡されます。
	 *
	 * @param engineConnector エンジンに依存するやり取りを行うためのオブジェクト
	 * @throws ConnectorException 終了時処理に失敗した場合にスローされます。
	 */
	public abstract void postFinalizeForTermination(Object engineConnector) throws ConnectorException;

}
