/*
 * ==================================================
 * General Process Connector Interface 3 (GPCI 3)
 * ( for VCSSL / Vnano Plug-in Development )
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2017-2018 by RINEARN (Fumihiro Matsui)
 * ==================================================
 */

package org.vcssl.connect;

/**
 * <p>
 * GPCI 3 (General Process Connector Interface 3)
 * 形式の外部関数プラグインを開発するための、
 * プラグイン側のコネクター・インターフェースです。
 * <br />
 * GPCI 3 は GPCI 2 以前の仕様を全て含むため、
 * このインターフェースを実装するクラスは、必要に応じて
 * {@link GeneralProcessConnector2 GeneralProcessConnector2}
 * および
 * {@link GeneralProcessConnector1 GeneralProcessConnector1}
 * インターフェースも同時に実装する事が可能です。
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
 * GPCI は、初期のVCSSLからサポートされている古いインターフェースですが、
 * 現在のVCSSL処理系においても利用できます。
 * Vnano (VCSSL nano) 処理系においては現時点では未サポートですが、
 * 将来的には利用可能になる可能性があります。
 * </p>
 *
 * <p>
 * GCPIでは、引数や戻り値を全て文字列配列として受け渡しするため、
 * 型変換などのオーバーヘッドが大きい事がネックになりますが、
 * その代わり手短に外部関数プラグインを開発する事ができます。
 * スクリプト側からは任意型の可変長引数の関数として認識されるなど、
 * 型の制約が緩いのも特徴です。
 * <br />
 * GPCIにおける型の制約の緩さは、print 関数のような、
 * そもそも任意型の可変長引数の関数を作りたい場合にはメリットになり得ます。
 * しかしながら、引数が数値である事を想定しているプラグイン関数に、
 * 実際には数値でない引数を渡す事が可能になってしまうなど、
 * 静的型付けの利益を享受できないという面ではデメリットにもなります。
 * <br />
 * GPCIのプラグインが提供する関数に対して、
 * 型システムによる区別や保護を効かせたい場合には、
 * 適切なシグネチャの関数をスクリプト内で定義し、
 * その中でプラグイン関数を呼び出すようにラッピングする事が推奨されます。
 * </p>
 *
 * <p>
 * GPCI 3 は、{@link GeneralProcessConnector1 GPCI 2} の全機能に加えて、
 * パーミッション設定ベースのセキュリティレイヤーを持つ処理系のため、
 * 必要・不要パーミッションの通知機能がサポートされています。
 * <br />
 * ただし、この機能に対応している処理系は現時点では存在しないため、
 * 現状のVCSSL処理系などでは、
 * GPCI 3 準拠のプラグインはGPCI 2 準拠のものと認識されて接続され、
 * パーミッション通知機能は呼び出されません。
 * </p>
 *
 * <p>
 * 現時点でGPCI 3 準拠のプラグイン接続をサポートしている処理系は、以下の通りです:
 * </p>
 * <ul>
 *   <li>RINEARN VCSSL Runtime (現時点では GPCI 2 準拠と認識して接続)</li>
 * </ul>
 *
 * <p>
 * 将来的にGPCI 3 準拠のプラグイン接続をサポートする可能性がある処理系は、以下の通りです:
 * </p>
 * <ul>
 *   <li>{@link org.vcssl.nano.VnanoEngine VnanoEngine}</li>
 * </ul>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public interface GeneralProcessConnector3 extends GeneralProcessConnector2 {


	/** 動的ロード時などに処理系側から参照される、インターフェースの形式名（値は"GPCI"）です。 */
	public static String INTERFACE_TYPE = "GPCI";

	/** 動的ロード時などに処理系側から参照される、インターフェースの世代名（値は"3"）です。*/
	public static String INTERFACE_GENERATION = "3";


	/**
	 * スクリプト実行毎の初期化処理を行います。
	 */
	public void init();


	/**
	 * スクリプト実行毎の終了時処理を行います。
	 */
	public void dispose();


	/**
	 * 指定された関数名が、このプラグインで実行可能な関数のものかどうかを判定します。
	 *
	 * このメソッドは、処理系側から、スクリプト実行前の段階で呼び出されます。
	 * そこで渡された関数名に対して true を返すと、スクリプト実行時に該当関数の処理が、
	 * このプラグインの {@link GeneralProcessConnector1#process process}
	 * メソッドで実行するように紐づけられます。
	 *
	 * 関数の引数情報は渡されず、
	 * 関数名だけで処理可能かどうか決定する必要がある事に注意してください。
	 * GPCIでは、同名で異なるシグネチャのプラグイン関数をサポートする事はできません。
	 * そのような事を行いたい場合は、
	 * スクリプト側の関数でプラグイン関数をラッピングする必要があります
	 *
	 * @param functionName 判定対象関数の関数名
	 * @return 処理可能であれば true
	 */
	public boolean isProcessable(String functionName);


	/**
	 * 指定された関数名の関数を、このプラグインで実行します。
	 *
	 * スクリプト側で渡された引数のデータ型に関わらず、
	 * 引数の内容は全て文字列に変換され、全引数の内容を格納した配列として
	 * args に渡されます。
	 *
	 * 戻り値は文字列配列として返し、スクリプト側からも文字列配列として受け取ります。
	 * スカラを返したい場合は、要素数1の配列として返す必要があります。
	 *
	 * @param functionName 実行対象関数の関数名
	 * @param args 実引数
	 * @return 実行結果の戻り値
	 */
	public String[] process( String functionName, String[] args );


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
	 * @param functionName 実行対象関数の関数名
	 * @return 必要なパーミッションを格納する配列
	 */
	public abstract String[] getNecessaryParmissions(String functionName);


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
	 * @param functionName 実行対象関数の関数名
	 * @return 不要なパーミッションを格納する配列
	 */
	public abstract String[] getUnnecessaryParmissions(String functionName);


	/**
	 * このプラグインが、スクリプトエンジンに接続された際に呼び出され、
	 * そのエンジンに依存するやり取りを行うためのオブジェクトが渡されます。
	 *
	 * 同オブジェクトは、恐らく {@link EngineConnector1 EngineConnector1}
	 * もしくはその後継の、抽象化されたインターフェースでラップされた形で渡されます。
	 *
	 * @param engineConnector エンジンに依存するやり取りを行うためのオブジェクト
	 */
	public abstract void setEngine(Object engineConnector);

}
