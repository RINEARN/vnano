/*
 * ==================================================
 * General Process Connector Interface 2 (GPCI 2)
 * ( for VCSSL / Vnano Plug-in Development )
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2017-2019 by RINEARN (Fumihiro Matsui)
 * ==================================================
 */

package org.vcssl.connect;

/**
 * <p>
 * GPCI 2 (General Process Connector Interface 2)
 * 形式の外部関数プラグインを開発するための、
 * プラグイン側のコネクター・インターフェースです。
 * <br>
 * GPCI 2 は GPCI 1 の仕様を全て含むため、
 * このインターフェースを実装するクラスは、必要に応じて
 * {@link GeneralProcessConnectorInterface1 GeneralProcessConnectorInterface1}
 * インターフェースも同時に実装する事が可能です。
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
 * <br>
 * GPCIにおける型の制約の緩さは、print 関数のような、
 * そもそも任意型の可変長引数の関数を作りたい場合にはメリットになり得ます。
 * しかしながら、引数が数値である事を想定しているプラグイン関数に、
 * 実際には数値でない引数を渡す事が可能になってしまうなど、
 * 静的型付けの利益を享受できないという面ではデメリットにもなります。
 * <br>
 * GPCIのプラグインが提供する関数に対して、
 * 型システムによる区別や保護を効かせたい場合には、
 * 適切なシグネチャの関数をスクリプト内で定義し、
 * その中でプラグイン関数を呼び出すようにラッピングする事が推奨されます。
 * </p>
 *
 * <p>
 * GPCI 2 は、{@link GeneralProcessConnectorInterface1 GPCI 1} の全機能に加えて、
 * スクリプトの実行毎に初期化・終了時処理を行える機能がサポートされています。
 * なお、より新しい {@link GeneralProcessConnectorInterface3 GPCI 3} が既に定義されていますが、
 * そちらにはまだ完全対応している処理系はありません。
 * ただし、GPCI 3 準拠のプラグインは GPCI 2 準拠と接続する事が可能なため、
 * GPCIプラグインの新規開発では GPCI 3 への準拠が推奨されます。
 * </p>
 *
 * <p>
 * 現時点でGPCI 2 準拠のプラグイン接続をサポートしている処理系は、以下の通りです:
 * </p>
 *
 * <ul>
 *   <li>RINEARN VCSSL Runtime</li>
 * </ul>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public interface GeneralProcessConnectorInterface2 {


	/**
	 * 指定された関数名が、このプラグインで実行可能な関数のものかどうかを判定します。
	 *
	 * このメソッドは、処理系側から、スクリプト実行前の段階で呼び出されます。
	 * そこで渡された関数名に対して true を返すと、スクリプト実行時に該当関数の処理が、
	 * このプラグインの {@link GeneralProcessConnectorInterface2#process process}
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
	 * スクリプト実行毎の初期化処理を行います。
	 */
	public void init();


	/**
	 * スクリプト実行毎の終了時処理を行います。
	 */
	public void dispose();
}
