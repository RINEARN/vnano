/*
 * ==================================================
 * General Process Connector Interface 1 (GPCI 1)
 * ( for VCSSL / Vnano Plug-in Development )
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2017-2018 by RINEARN (Fumihiro Matsui)
 * ==================================================
 */

package org.vcssl.connect;

/**
 * <p>
 * GPCI 1 (General Process Connector Interface 1)
 * 形式の外部関数プラグインを開発するための、
 * プラグイン側のコネクター・インターフェースです。
 * </p>
 *
 * <p>
 * このコネクター・インターフェースは、最初期の VCSSL においてサポートされたものですが、
 * スクリプトの開始および終了のタイミングをプラグイン側から検知する仕組みが無く、
 * 初期化および終了時処理を行いたい場合に不都合が生じるため、
 * 後継として拡張された
 * {@link GeneralProcessConnectorInterface2 GPCI 2}
 * および
 * {@link GeneralProcessConnectorInterface3 GPCI 3}
 * が存在します。
 * 最初期のVCSSL処理系を使用するなどの特別な事情が無い場合は、そちらの方を使用してください。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public interface GeneralProcessConnectorInterface1 {


	/**
	 * 指定された関数名が、このプラグインで実行可能な関数のものかどうかを判定します。
	 *
	 * このメソッドは、処理系側から、スクリプト実行前の段階で呼び出されます。
	 * そこで関数名に対して true を返すと、スクリプト実行時に該当関数の処理が
	 * このプラグインの {@link GeneralProcessConnectorInterface1#process process}
	 * メソッドに一任されます。
	 *
	 * 関数の引数情報は渡されず、
	 * 関数名だけで処理可能かどうか決定する必要がある事に注意してください。
	 * GPCIでは、同名で異なるシグネチャのプラグイン関数をサポートする事はできません。
	 * そのような事を行いたい場合は、スクリプト側で、
	 * プラグイン関数をラッピングした関数を用意する必要があります
	 * （{@link GeneralProcessConnectorInterface1#process process} のコメント参照）。
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
	 * このように、GPCIでは引数や戻り値の型に関する制約が緩いため、
	 * 型システムによる区別や保護を効かせたい場合には、
	 * 適切なシグネチャの関数をスクリプト内で定義し、
	 * その中でプラグイン関数を呼び出すようにラッピングする事が推奨されます。
	 *
	 * @param functionName 実行対象関数の関数名
	 * @param args 実引数
	 * @return 実行結果の戻り値
	 */
	public String[] process( String functionName, String[] args );
}
