/*
 * Copyright(C) 2019-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.script.ScriptException;

import org.vcssl.connect.ConnectorException;
import org.vcssl.nano.compiler.Compiler;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.interconnect.MetaQualifiedFileLoader;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.LanguageSpecContainer;
import org.vcssl.nano.spec.OptionKey;
import org.vcssl.nano.vm.VirtualMachine;

//Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/VnanoEngine.html
//ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/VnanoEngine.html

/**
 * <span class="lang-en">The class of the script engine of the Vnano (Vnano Engine)</span>
 * <span class="lang-ja">Vnanoのスクリプトエンジン（Vnanoエンジン）のクラスです</span>
 * .
 *
 * <span class="lang-en">
 * {@link VnanoScriptEngine VnanoScriptEngine} class which wrapped by ScriptEngine interface is also available,
 * for using through Scripting API of the standard library.
 * </span>
 * <span class="lang-ja">
 * 標準ライブラリの Scripting API を介して使用したい場合は,
 * ScriptEngineインターフェースでラップされた {@link VnanoScriptEngine VnanoScriptEngine} クラスも使用できます.
 * </span>
 *
 * <p>
 * &raquo; <a href="../../../../src/org/vcssl/nano/VnanoEngine.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../api/org/vcssl/nano/VnanoEngine.html">Public Only</a>
 * | <a href="../../../../api-all/org/vcssl/nano/VnanoEngine.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public final class VnanoEngine {

	/** 各種の言語仕様設定類を格納するコンテナを保持します。 */
	private final LanguageSpecContainer LANG_SPEC;


	/**
	 * <span class="lang-en">An object to mediate information/connections between components, named as "interconnect"</span>
	 * <span class="lang-ja">処理系内の各部で共有する情報や接続を仲介するオブジェクト（インターコネクト）です</span>
	 * .
	 */
	Interconnect interconnect = null;


	/**
	 * <span class="lang-en">Stores contents of library scripts, with their names as keys</span>
	 * <span class="lang-ja">ライブラリスクリプトの内容を, ライブラリ名をキーとして保持します</span>
	 * .
	 */
	Map<String, String> libraryNameContentMap = null;


	/**
	 * <span class="lang-en">Create a Vnano Engine with default settings</span>
	 * <span class="lang-ja">標準設定のVnanoエンジンを生成します</span>
	 * .
	 */
	public VnanoEngine() {
		this(new LanguageSpecContainer());
	}

	/**
	 * <span class="lang-en">Create a Vnano Engine with the customized language specification settings</span>
	 * <span class="lang-ja">カスタマイズされた言語仕様設定で, Vnanoエンジンを生成します</span>
	 * .
	 * @param langSpec
	 *   <span class="lang-en">language specification settings.</span>
	 *   <span class="lang-ja">言語仕様設定.</span>
	 */
	public VnanoEngine(LanguageSpecContainer langSpec) {
		if (langSpec == null) {
			throw new NullPointerException();
		}
		this.LANG_SPEC = langSpec;
		this.libraryNameContentMap = new LinkedHashMap<String, String>();
		this.interconnect = new Interconnect(LANG_SPEC);
	}


	/**
	 * <span class="lang-en">Executes an expression or script code specified as an argument</span>
	 * <span class="lang-ja">引数に指定された式またはスクリプトコードを実行します</span>
	 * .
	 * @param script
	 *   <span class="lang-en">An expression or script code to be executed.</span>
	 *   <span class="lang-ja">実行対象の式またはスクリプトコード.</span>
	 *
	 * @return
	 *   <span class="lang-en">
	 *   The evaluated value of the expression, or the last expression statement in script code.
	 *   If there is no evaluated value, returns null.
	 *   </span>
	 *   <span class="lang-ja">
	 *   式, またはスクリプトコード内の最後の式文の評価値. もしも評価値が無かった場合は null が返されます.
	 *   </span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">Thrown when any error has detected for the content or the processing of the script.</span>
	 *   <span class="lang-ja">スクリプトの内容または実行過程にエラーが検出された場合にスローされます.</span>
	 */
	public Object executeScript(String script) throws VnanoException {
		if (script == null) {
			throw new NullPointerException();
		}

		try {

			// スクリプトに対し、処理系内で読み込んだライブラリファイル等と同様の後処理を実行（文字コード宣言削除や、環境依存内容の正規化など）
			try {
				script = MetaQualifiedFileLoader.postprocess(null, script, LANG_SPEC); // 第一引数はエラーメッセージで用いるファイル名（ある場合のみ）
			} catch (VnanoException vne) {
				String message = vne.getMessageWithoutLocation();
				throw new ScriptException(message);
			}

			// 全プラグインの初期化処理などを行い、インターコネクトをスクリプト実行可能な状態に移行
			this.interconnect.activate();

			// Contain an execution-target script and library scripts into an array.
			// 実行対象スクリプトと, ライブラリスクリプト（複数）を1つの配列にまとめる
			int libN = this.libraryNameContentMap.size();
			String[] scripts = new String[libN  + 1];
			String[] names   = new String[libN + 1];
			names[libN] = (String)this.interconnect.getOptionMap().get(OptionKey.MAIN_SCRIPT_NAME); // この内容は正規化済み
			scripts[libN] = script;
			int libIndex = 0;
			for (Map.Entry<String, String> nameContentPair: this.libraryNameContentMap.entrySet()) {
				names[libIndex] = LANG_SPEC.IDENTIFIER_SYNTAX.normalizeScriptIdentifier( nameContentPair.getKey() ); // 未正規化状態なので正規化する
				scripts[libIndex] = nameContentPair.getValue();
				// ライブラリ名と実行対象スクリプト名との重複は不可能（内部で実行対象スクリプト範囲を抽出しやすくするための実装上の都合）
				if (names[libIndex].equals(names[libN])) {
					throw new VnanoException(ErrorType.LIBRARY_SCRIPT_NAME_IS_CONFLICTING_WITH_MAIN_SCRIPT_NAME, names[libIndex]);
				}
				libIndex++;
			}

			// Translate scripts to a VRIL code (intermediate assembly code) by a compiler.
			// コンパイラでスクリプトコードからVRILコード（中間アセンブリコード）に変換
			Compiler compiler = new Compiler(LANG_SPEC);
			String assemblyCode = compiler.compile(scripts, names, this.interconnect);

			// Execute the VRIL code on the VM.
			// VMでVRILコードを実行
			VirtualMachine vm = new VirtualMachine(LANG_SPEC);
			Object evalValue = vm.executeAssemblyCode(assemblyCode, this.interconnect);

			// 全プラグインの終了時処理などを行い、インターコネクトを待機状態に移行
			this.interconnect.deactivate();

			return evalValue;

		// If any error is occurred for the content/processing of the script,
		// set the locale to switch the language of error messages, and re-throw the exception to upper layers.
		// スクリプト内容による例外は, エラーメッセージに使用する言語ロケールを設定してから上に投げる
		} catch (VnanoException e) {
			Locale locale = (Locale)this.interconnect.getOptionMap().get(OptionKey.LOCALE); // Type was already checked.
			e.setLocale(locale);

			// もしも VnanoException が、外部関数が投げる ConnectorException をラップしている場合で、
			// それが特別な対処を要するものの場合（メッセージが「 ___ 」で始まる）は、特別な対処を行う
			if (e.getCause() instanceof ConnectorException && ((ConnectorException)e.getCause()).getMessage().startsWith("___")) {
				this.handleSpecialConnectorException((ConnectorException)e.getCause(), e);
				return null; // 上の行で VnanoException が再スローされなかった場合は何もしない（ exit 関数での終了など ）
			} else {
				throw e;
			}

		// If unexpected exception is occurred, wrap it by the VnanoException and re-throw,
		// to prevent the stall of the host-application.
		// 実装の不備等による予期しない例外も, ホストアプリケーションを落とさないようにVnanoExceptionでラップする
		} catch (Exception unexpectedException) {
			throw new VnanoException(unexpectedException);
		}
	}


	/**
	 * <span class="lang-en">Handles a ConnectorException thrown in scripting, if it requires special handling</span>
	 * <span class="lang-ja">スクリプト実行中にスローされた, 特別な対処を要する ConnectorException に対処を行います</span>
	 * .
	 * @param exception
	 *   <span class="lang-en">ConnectorException thrown in scripting.</span>
	 *   <span class="lang-ja">スクリプト実行中にスローされた ConnectorException.</span>
	 *
	 * @param callerScriptName
	 *   <span class="lang-en">The VnanoException wrapping the thrown ConnectorException (provides line-number in the script and so on).</span>
	 *   <span class="lang-ja">スローされた ConnectorException をラップしている VnanoException (スクリプト内での行番号などを保持).</span>
	 */
	private void handleSpecialConnectorException(ConnectorException exception, VnanoException wrapperVnanoException)
			throws VnanoException {

		String message = exception.getMessage();

		// exit 関数が投げてくる例外は、ユーザーに伝えるべきエラーではないので、何もしない
		if (message.startsWith("___EXIT")) {
			return;
		}

		// error 関数が投げてくる例外は、スクリプトの記述者が書いたエラーメッセージを ConnectorException でラップしたもので、
		// 「 外部関数『 error 』でエラーが発生しました：(...エラーメッセージ...) 」等と表示するのは冗長なので、
		// エラーメッセージ部分のみを抜き出し、VnanoException のメッセージに再設定して投げる
		if (message.startsWith("___ERROR")) {
			String passedErrorMessage = message.split(":")[1]; // error 関数の引数に渡されたエラーメッセージ内容
			VnanoException vne = wrapperVnanoException.clone();
			vne.setErrorType(ErrorType.UNMODIFIED);
			vne.setErrorWords( new String[] { passedErrorMessage } );
			throw vne;
		}
	}


	/**
	 * <span class="lang-en">Connects various types of plug-ins which provides external functions/variables</span>
	 * <span class="lang-ja">外部関数/変数を提供する, 各種のプラグインを接続します</span>
	 * .
	 *
	 * @param bindingKey
	 *   <span class="lang-en">
	 *   An unique key to identify the plug-in.
	 *   It also works as an name (alias) of a connected external variable/functuion/namespace.
	 *   For a function, you can specify a signature containing parameter-declarations (e.g.: "foo(int,float)" )
	 *   instead of a name, if you want to avoid duplication of a key when you put overloaded functions
	 *   (note that, syntax, data-types, and so on for parameters will not be checked).
	 *   Also, you can specify "___VNANO_AUTO_KEY" for generate a valid key automatically.
	 *   </span>
	 *   <span class="lang-ja">
	 *   プラグインを一意に識別するためのキー.
	 *   キーの内容は, 接続される外部関数/変数/名前空間にスクリプト内からアクセスするための名称としても機能します.
	 *   関数に対しては、関数名のみの代わりに、引数部を含むシグネチャ（ 例えば "foo(int,float)" 等 ）を指定する事も可能です
	 *   （ただし、これは単にキーの重複を避けたい場合のためにサポートされており、引数部に対する構文や整合性の検査などは行われません）。
	 *   なお, "___VNANO_AUTO_KEY" を指定する事で, 有効なキーを自動生成する事もできます.
	 *   </span>
	 *
	 * @param plugin
	 *   <span class="lang-en">
	 *   The plug-in to be connected.
	 *   General "Object" type instances can be connected as a plug-in,
	 *   for accessing their methods/fields from the script code as external functions/variables.
	 *   For accessing only static methods and fields, "Class&lt;T&gt;" type instance can also be connected.
	 *   In add, if you want to choose a method/field to be accessible from script code,
	 *   a "Method"/"Field" type instance can be connected.
	 *   ( In that case, if the method/field is static, pass an Object type array for this argument,
	 *     and store the Method/Field type instance at [0],
	 *     and store "Class&lt;T&gt;" type instance of the class defining the method/field at [1] ).
	 *   Furthermore, the instance of the class implementing
	 *   {@link org.vcssl.connect.ExternalFunctionConnectorInterface1 XFCI1} /
	 *   {@link org.vcssl.connect.ExternalVariableConnectorInterface1 XVCI1} /
	 *   {@link org.vcssl.connect.ExternalNamespaceConnectorInterface1 XNCI1}
	 *   type less-overhead plug-in interface can be connected.
	 *   Also, this method is used for connecting
	 *   {@link org.vcssl.connect.PermissionAuthorizerConnectorInterface1 PACI1}
	 *   type plug-ins which is used for managing permissions.
	 *   </span>
	 *   <span class="lang-ja">
	 *   接続したいプラグイン.
	 *   一般の Object 型のインスタンスをプラグインとして接続して,
	 *   それに属するメソッド/フィールドに, スクリプト内から外部関数/変数としてアクセスする事もできます.
	 *   static なメソッド/フィールドのみにアクセスする場合には, Class&lt;T&gt; 型のインスタンスも接続できます.
	 *   また, 個々のメソッド/フィールドを選んで接続したい場合のために,
	 *   Method / Field 型のインスタンスを接続する事もできます
	 *   （ その際, もしそのフィールド・メソッドが static ではない場合には,
	 *      引数 plugin は Object 配列型とし、その [0] 番要素に Field または Method を格納し,
	 *      [1] 番要素にそのフィールド・メソッドが定義されたクラスの Class&lt;T&gt; 型インスタンスを格納してください ）.
	 *   加えて,
	 *   {@link org.vcssl.connect.ExternalFunctionConnectorInterface1 XFCI1} /
	 *   {@link org.vcssl.connect.ExternalVariableConnectorInterface1 XVCI1} /
	 *   {@link org.vcssl.connect.ExternalNamespaceConnectorInterface1 XNCI1}
	 *   形式の, 低オーバーヘッドなプラグインインターフェースを実装したクラスのインスタンスも接続できます.
	 *   また、パーミッションの管理を行う,
	 *   {@link org.vcssl.connect.PermissionAuthorizerConnectorInterface1 PACI1}
	 *   形式のプラグインの接続にも、このメソッドを用います.
	 *   </span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">
	 *   Thrown if the plug-in could not be connected,
	 *   caused by unsupported interfaces, incompatibility of data types, and so on.
	 *   </span>
	 *   <span class="lang-ja">
	 *   サポートされていないインターフェースの使用や, データ型の互換性などの原因により,
	 *   プラグインの接続に失敗した場合にスローされます.
	 *   </span>
	 */
	public void connectPlugin(String bindingKey, Object plugin) throws VnanoException {
		if (bindingKey == null || plugin == null) {
			throw new NullPointerException();
		}
		this.interconnect.connectPlugin(bindingKey, plugin);
	}


	/*
	// ファイル読み込み系の処理は最表層の VnanoScriptEngine 側で実装し、
	// この層や下層ではファイル含む一切のシステムリソースへの新規アクセスは行わない
	//（使用するのはインスタンスとして直接渡されたものに限る）
	// このクラスを直接用いつつ、
	// ファイルからライブラリを読み込みたい場合は、ScriptLoader で読み込んで setLibraryScripts へ渡す
	public void loadPlugins(String[] paths) {
	}
	*/


	/**
	 * <span class="lang-en">Disconnects all plug-ins</span>
	 * <span class="lang-ja">全てのプラグインの接続を解除します</span>
	 * .
	 * @throws VnanoException
	 *   <span class="lang-en">
	 *   Thrown when an exception occurred on the finalization of the plug-in to be disconnected.
	 *   </span>
	 *   <span class="lang-ja">
	 *   プラグインの終了時処理でエラーが発生した場合にスローされます.
	 *   </span>
	 */
	public void disconnectAllPlugins() throws VnanoException {
		this.interconnect.disconnectAllPlugins();
	}


	/**
	 * <span class="lang-en">Add a library script which will be "include"-ed at the head of a executed script</span>
	 * <span class="lang-ja">実行対象スクリプトの先頭に "include" される, ライブラリスクリプトを追加します</span>
	 * .
	 * @param libraryScriptName
	 *   <span class="lang-en">Names of the library script (displayed in error messages)</span>
	 *   <span class="lang-ja">ライブラリスクリプトの名称 (エラーメッセージ等で使用されます)</span>
	 *
	 * @param libraryScriptContents
	 *   <span class="lang-en">Contents (code) of the library script</span>
	 *   <span class="lang-ja">ライブラリスクリプトのコード内容</span>
	 */
	public void includeLibraryScript(String libraryScriptName, String libraryScriptContent) throws VnanoException {
		if (libraryScriptName == null || libraryScriptContent == null) {
			throw new NullPointerException();
		}
		if (this.libraryNameContentMap.containsKey(libraryScriptName)) {
			throw new VnanoException(ErrorType.LIBRARY_IS_ALREADY_INCLUDED, libraryScriptName);
		}
		this.libraryNameContentMap.put(libraryScriptName, libraryScriptContent);
	}


	/**
	 * <span class="lang-en">Uninclude all library scripts</span>
	 * <span class="lang-ja">全てのライブラリスクリプトの include 登録を解除します</span>
	 * .
	 * @throws VnanoException
	 *   <span class="lang-en">
	 *   Will not be thrown on the current implementation,
	 *   but it requires to be "catch"-ed for keeping compatibility in future.
	 *   </span>
	 *   <span class="lang-ja">
	 *   現状ではスローされませんが, 将来的な互換性維持のためのために catch する必要があります.
	 *   </span>
	 */
	public void unincludeAllLibraryScripts() throws VnanoException {
		this.libraryNameContentMap = new LinkedHashMap<String, String>();
	}


	/**
	 * <span class="lang-en">
	 * Sets options, by a Map (option map) storing names and values of options you want to set
	 * </span>
	 * <span class="lang-ja">
	 * オプションの名前と値を格納するマップ（オプションマップ）によって, オプションを設定します
	 * </span>
	 * .
	 * <span class="lang-en">
	 * Type of the option map is Map<String,Object>, and its keys represents option names.
	 * For details of option names and values,
	 * see {@link org.vcssl.nano.spec.OptionKey} and {@link org.vcssl.nano.spec.OptionValue}.
	 * </span>
	 *
	 * <span class="lang-ja">
	 * オプションマップは Map<String,Object> 型で, そのキーはオプション名に対応します.
	 * オプション名と値の詳細については,
	 * {@link org.vcssl.nano.spec.OptionKey} と {@link org.vcssl.nano.spec.OptioValue} の説明をご参照ください.
	 * </span>
	 *
	 * @param optionMap
	 *   <span class="lang-en">A Map (option map) storing names and values of options</span>
	 *   <span class="lang-ja">オプションの名前と値を格納するマップ（オプションマップ）</span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">Thrown if invalid option settings is detected.</span>
	 *   <span class="lang-ja">オプションの指定内容が正しくなかった場合にスローされます.</span>
	 */
	public void setOptionMap(Map<String,Object> optionMap) throws VnanoException {
		if (optionMap == null) {
			throw new NullPointerException();
		}
		this.interconnect.setOptionMap(optionMap);
	}


	/**
	 * <span class="lang-en">Gets the Map (option map) storing names and values of options</span>
	 * <span class="lang-ja">オプションの名前と値を格納するマップ（オプションマップ）を取得します</span>
	 * .
	 * <span class="lang-en">
	 * Type of the option map is Map<String,Object>, and its keys represents option names.
	 * For details of option names and values,
	 * see {@link org.vcssl.nano.spec.OptionKey} and {@link org.vcssl.nano.spec.OptionValue}.
	 * </span>
	 *
	 * <span class="lang-ja">
	 * オプションマップは Map<String,Object> 型で, そのキーはオプション名に対応します.
	 * オプション名と値の詳細については,
	 * {@link org.vcssl.nano.spec.OptionKey} と {@link org.vcssl.nano.spec.OptioValue} の説明をご参照ください.
	 * </span>
	 *
	 * @return
	 *   <span class="lang-en">A Map (option map) storing names and values of options</span>
	 *   <span class="lang-ja">オプションの名前と値を格納するマップ（オプションマップ）</span>
	 */
	public Map<String,Object> getOptionMap() {
		return this.interconnect.getOptionMap();
	}


	/**
	 * <span class="lang-en">
	 * Sets permissions, by a Map (permission map) storing names and values of permission items you want to set
	 * </span>
	 * <span class="lang-ja">
	 * パーミッション項目の名前と値を格納するマップ（パーミッションマップ）によって, 各パーミッションの値を設定します
	 * </span>
	 * .
	 * <span class="lang-en">
	 * Type of the permission map is Map<String,String>, and its keys represents names of permission items.
	 * For details of names and values of permission items,
	 * see {@link org.vcssl.connect.ConnectorPermissionName} and {@link org.vcssl.connect.ConnectorPermissionValue}.
	 * </span>
	 *
	 * <span class="lang-ja">
	 * パーミッションマップは Map<String,String> 型で, そのキーはパーミッション項目の名称に対応します.
	 * パーミッション項目の名称と値の詳細については,
	 * {@link org.vcssl.connect.ConnectorPermissionName} と {@link org.vcssl.connect.ConnectorPermissionValue}
	 * の説明をご参照ください.
	 * </span>
	 *
	 * @param permissionMap
	 *   <span class="lang-en">A Map (permission map) storing names and values of permission items</span>
	 *   <span class="lang-ja">パーミッション項目の名前と値を格納するマップ（パーミッションマップ）</span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">Thrown if invalid permission settings is detected.</span>
	 *   <span class="lang-ja">パーミッションの指定内容が正しくなかった場合にスローされます.</span>
	 */
	public void setPermissionMap(Map<String, String> permissionMap) throws VnanoException {
		if (permissionMap == null) {
			throw new NullPointerException();
		}
		this.interconnect.setPermissionMap(permissionMap);
	}


	/**
	 * <span class="lang-en">Gets the Map (permission map) storing names and values of permission items</span>
	 * <span class="lang-ja">パーミッション項目の名前と値を格納するマップ（パーミッションマップ）を取得します</span>
	 * .
	 * <span class="lang-en">
	 * Type of the permission map is Map<String,String>, and its keys represents names of permission items.
	 * For details of names and values of permission items,
	 * see {@link org.vcssl.connect.ConnectorPermissionName} and {@link org.vcssl.connect.ConnectorPermissionValue}.
	 * </span>
	 *
	 * <span class="lang-ja">
	 * パーミッションマップは Map<String,String> 型で, そのキーはパーミッション項目の名称に対応します.
	 * パーミッション項目の名称と値の詳細については,
	 * {@link org.vcssl.connect.ConnectorPermissionName} と {@link org.vcssl.connect.ConnectorPermissionValue}
	 * の説明をご参照ください.
	 * </span>
	 *
	 * @return
	 *   <span class="lang-en">A Map (permission map) storing names and values of permission items</span>
	 *   <span class="lang-ja">パーミッション項目の名前と値を格納するマップ（パーミッションマップ）</span>
	 */
	public Map<String, String> getPermissionMap() throws VnanoException {
		return this.interconnect.getPermissionMap();
	}
}
