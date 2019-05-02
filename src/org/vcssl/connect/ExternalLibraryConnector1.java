/*
 * ==================================================
 * External Library Connector Interface 1 (XLCI 1)
 * ( for VCSSL / Vnano Plug-in Development )
 * --------------------------------------------------
 * This file is released under CC0.
 * Written in 2019 by RINEARN (Fumihiro Matsui)
 * ==================================================
 */

package org.vcssl.connect;

/**
 * <p>
 * XLCI 1 (External Library Connector Interface 1)
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
 * XLCI は、複数の関数や変数を、1つのライブラリとしてまとめるためのプラグインインターフェースです。
 * XLCI 1 では、関数に {@link ExternalFunctionConnector1 XFCI 1} 形式、
 * 変数に {@link ExternalVariableConnector1 XVCI 1} 形式のインターフェースを採用しています。
 * それらの形式で実装された関数/変数プラグインの集合（配列）を、このライブラリに属するものとして保持し、
 * それぞれ {@link ExternalLibraryConnector1#getFunctions() getFunctions()} メソッドおよび
 * {@link ExternalLibraryConnector1#getVariables() getVariables()} メソッドの戻り値として、
 * 処理系に提供します。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public interface ExternalLibraryConnector1 {


	/**
	 * ライブラリ名を取得します。
	 *
	 * @return ライブラリ名
	 */
	public abstract String getLibraryName();


	/**
	 * パーミッション設定ベースのセキュリティレイヤーを持つ処理系において、
	 * このライブラリの接続に必要な全てのパーミッションを、配列にまとめて取得します。
	 *
	 * パーミッションベースのセキュリティレイヤ―を持たない処理系では、
	 * このメソッドは機能しません（呼び出されません）。
	 *
	 * このメソッドの戻り値に、
	 * {@link ConnectorPermission#NONE ConnectorPermission.NONE}
	 * のみを格納する配列を返す事で、全てのパーミッションが不要となります。
	 * 現状では、このライブラリに属する関数・変数のインターフェースである
	 * {@link ExternalFunctionConnector1 XFCI1}/{@link ExternalFunctionConnector1 XVCI1}
	 * の階層でもパーミッション指定機能を持っているため、このメソッドは冗長であり、
	 * 上記のように実装する以外の具体的な使い道は、あまり考えられません。
	 *
	 * このメソッドは、将来的に、ライブラリを接続する事そのものに対して、
	 * それに属する関数・変数とは独立にパーミッション設定を行いたい用途が生じた場合のために、
	 * 予約的に宣言されています。
	 *
	 * @return 必要なパーミッションを格納する配列
	 */
	public abstract String[] getNecessaryPermissions();


	/**
	 * パーミッション設定ベースのセキュリティレイヤーを持つ処理系において、
	 * このライブラリの接続に不要な全てのパーミッションを、配列にまとめて取得します。
	 *
	 * パーミッションベースのセキュリティレイヤ―を持たない処理系では、
	 * このメソッドは機能しません（呼び出されません）。
	 *
	 * このメソッドの戻り値に
	 * {@link ConnectorPermission#ALL ConnectorPermission.ALL} のみを格納する配列を返す事で、
	 * 必要パーミッション配列に含まれているものを除いた、全てのパーミッションが不要となります。
	 * 現状では、このライブラリに属する関数・変数のインターフェースである
	 * {@link ExternalFunctionConnector1 XFCI1}/{@link ExternalFunctionConnector1 XVCI1}
	 * の階層でもパーミッション指定機能を持っているため、このメソッドは冗長であり、
	 * 上記のように実装する以外の具体的な使い道は、あまり考えられません。
	 *
	 * このメソッドは、将来的に、ライブラリを接続する事そのものに対して、
	 * それに属する関数・変数とは独立にパーミッション設定を行いたい用途が生じた場合のために、
	 * 予約的に宣言されています。
	 *
	 * @return 不要なパーミッションを格納する配列
	 */
	public abstract String[] getUnnecessaryPermissions();


	/**
	 * このライブラリに属する全ての関数を、配列にまとめて返します。
	 *
	 * @return このライブラリに属する関数をまとめた配列
	 */
	public abstract ExternalFunctionConnector1[] getFunctions();


	/**
	 * このライブラリに属する全ての変数を、配列にまとめて返します。
	 *
	 * @return このライブラリに属する変数をまとめた配列
	 */
	public abstract ExternalVariableConnector1[] getVariables();


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
