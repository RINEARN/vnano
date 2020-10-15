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
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.spec.OptionKey;
import org.vcssl.nano.spec.PerformanceKey;
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

	// 実装メモ：
	// ・複数スレッドから同一インスタンスの executeScript を同時に呼べるようにするための対処等はこのクラス内には実装しない。
	//   将来的にそういう使い方に対応したくなった場合は、ParallelVnanoEngine 等の別クラスを作成してそちらでサポートするようにする。
	//   (現実的な必要性があまり無さそうなのと、そのためにエンジン最表層のこのクラス内で無駄にトリッキーな記述をしたくないため)


	/** 各種の言語仕様設定類を格納するコンテナを保持します。 */
	private final LanguageSpecContainer LANG_SPEC;


	/**
	 * <span class="lang-en">Stores a process virtual machine executing the currently running intermediate code</a>
	 * <span class="lang-ja">現在実行中の中間コードを実行しているプロセス仮想マシン（VM）を保持します</a>
	 * .
	 */
	private VirtualMachine virtualMachine = null;


	/**
	 * <span class="lang-en">Stores an object to mediate information/connections between components ("interconnect")</span>
	 * <span class="lang-ja">処理系内の各部で共有する情報や接続を仲介するオブジェクト（インターコネクト）を保持します</span>
	 * .
	 */
	private Interconnect interconnect = null;


	/**
	 * <span class="lang-en">Stores contents of library scripts, with their names as keys</span>
	 * <span class="lang-ja">ライブラリスクリプトの内容を, ライブラリ名をキーとして保持します</span>
	 * .
	 */
	// ライブラリ周りもインターコネクト内で持つようにすべき?
	// なんで他にも数多くのあれこれが存在する中でこれだけがエンジン直下フィールドとして居るのっていう違和感が。たとえ private でも。
	// > 以前はオプションマップとか他のものも結構エンジン直下にあった気がする。
	//   それが次第にインターコネクト内に移っていって、これがまだ残っているだけ ?
	//   > これをインターコネクトに移すなら併せて LanguageSpecContainer も移したい。
	//     むしろそっちの方があちこちでコンストラクタに渡してフィールドに持ってるのでなんとかしたい。
	// また今度要検討
	private Map<String, String> libraryNameContentMap = null;


	/**
	 * <span class="lang-en">An object for using as a lock of synchronized blocks in an instance of this class</span>
	 * <span class="lang-ja">このクラスの個々のインスタンスにおける synchronized ブロックのロック用オブジェクトです</span>
	 * .
	 */
	// ※ 主にスレッドキャッシュ剥がし用に使うのでこのロック自体にあまり深い意味は無い
	private final Object lock;


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
		this.virtualMachine = new VirtualMachine(LANG_SPEC);
		this.lock = new Object();
	}


	/**
	 * <span class="lang-en">Executes an expression or script code specified as an argument</span>
	 * <span class="lang-ja">引数に指定された式またはスクリプトコードを実行します</span>
	 * .
	 * <span class="lang-en">
	 * Please note that,
	 * you must not call this method of the same instance at the same time from multiple threads,
	 * for processing multiple scripts in parallel.
	 * For such parallel executions, create an independent instance of the engine for each thread and use them.
	 * </span>
	 * <span class="lang-ja">
	 * なお, 同一インスタンスにおけるこのメソッドを複数のスレッドから呼び出し,
	 * 複数のスクリプトを同時に（並行して）実行する事には対応していません.
	 * そのような並列処理を行いたい場合には, スレッドごとに独立なエンジンのインスタンスを生成して使用してください.
	 * </span>
	 *
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
			// > インターコネクト側でやったほうがいい気がする。プラグインに対してはそっちでエイリアスとか名前空間の対処とかもやってるし。
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
			String assemblyCode = new Compiler(LANG_SPEC).compile(scripts, names, this.interconnect);

			// Execute the VRIL code on a VM.
			// VMでVRILコードを実行
			Object evalValue = this.virtualMachine.executeAssemblyCode(assemblyCode, this.interconnect);

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
	 * <span class="lang-en">
	 * Terminates the currently running script as soon as possible
	 * </span>
	 * <span class="lang-ja">
	 * 現在実行中のスクリプトの処理を, 可能な限り早期に放棄して終了させます
	 * </saam>
	 * .
	 * <span class="lang-en">
	 * To be precise, the {@link org.vcssl.nano.vm.VirtualMachine VirtualMachine}
	 * (which is processing instructions compiled from the script) in the engine
	 * will be terminated after when the processing of a currently executed instruction has been completed,
	 * without processing remained instructions.
	 * </span>
	 * <span class="lang-ja">
	 * より正確には, スクリプトからコンパイルされた命令列を処理している,
	 * エンジン内の {@link org.vcssl.nano.vm.VirtualMachine VirtualMachine} が,
	 * 現在実行中の命令(1個)の処理が完了した時点で, 残りの命令列の実行を放棄して終了します.
	 * </saam>
	 *
	 * <span class="lang-en">
	 * Also, if you used this method, call {@link VnanoEngine#resetTerminator() resetTerminator()}
	 * method before the next execution of a new script,
	 * otherwise the next execution will end immediately without processing any instructions.
	 * </span>
	 * <span class="lang-ja">
	 * なお、このメソッドを呼び出して実行を終了させた後に、再び(新規に)スクリプトを実行する際には、事前に
	 * {@link VnanoEngine#resetTerminator() resetTerminator()} メソッドを呼び出す必要があります.
	 * 前者の呼び出しから後者の呼び出しまでの間, 実行が要求されたスクリプトはすぐに終了します.
	 * </span>
	 *
	 * <span class="lang-en">
	 * By the above behavior, even if a termination request by this method and
	 * an execution request by another thread are conflict, the execution will be terminated certainly
	 * (unless {@link VnanoEngine#resetTerminator() resetTerminator()} will be called before
	 * when the execution will have been terminated).
	 * </span>
	 * <span class="lang-ja">
	 * 上記の仕様により, このメソッドの呼び出しと新規実行リクエストが,
	 * 別スレッドからシビアに競合したタイミングで行われた場合においても,
	 * (終了前に {@link VnanoEngine#resetTerminator() resetTerminator()} が呼ばれない限り)
	 * スクリプトは確実に終了します.
	 * </span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">
	 *   Thrown when the option {@link org.vcssl.spec.OptionKey#TERMINATOR_ENABLED TERMINATOR_ENABLED} is disabled.
	 *   </span>
	 *   <span class="lang-ja">
	 *   {@link org.vcssl.nano.spec.OptionKey#TERMINATOR_ENABLED TERMINATOR_ENABLED}
	 *   オプションが無効化されていた場合にスローされます.
	 *   </span>
	 */
	public void terminateScript() throws VnanoException {
		if (! (boolean)this.interconnect.getOptionMap().get(OptionKey.TERMINATOR_ENABLED) ) {
			throw new VnanoException(ErrorType.TERMINATOR_IS_DISABLED);
		}
		this.virtualMachine.terminate();
	}


	/**
	 * <span class="lang-en">
	 * Resets the engine which had terminated by {@link VnanoEngine#terminate() terminate()}
	 * method, for processing new scripts
	 * </span>
	 * <span class="lang-ja">
	 * {@link VnanoEngine#terminate() terminate()} メソッドによって終了させたエンジンを,
	 * 再び(新規)スクリプト実行可能な状態に戻します
	 * </span>
	 * .
	 * <span class="lang-en">
	 * Please note that, if an execution of code is requested by another thread
	 * when this method is being processed, the execution request might be missed.
	 * </span>
	 * <span class="lang-ja">
	 * なお, このメソッドの呼び出しと新規実行リクエストが, 別スレッドからシビアに競合したタイミングで行われた場合には,
	 * スクリプトは実行されない可能性がある事に留意してください.
	 * </span>
	 */
	// 名前、disableTerminator だと TERMINATOR_ENABLED オプションを false にする的な挙動と勘違いを招くので、あくまで reset
	public void resetTerminator() throws VnanoException {
		if (! (boolean)this.interconnect.getOptionMap().get(OptionKey.TERMINATOR_ENABLED) ) {
			throw new VnanoException(ErrorType.TERMINATOR_IS_DISABLED);
		}
		this.virtualMachine.resetTerminator();
	}


	/**
	 * <span class="lang-en">Connects various types of plug-ins which provides external functions/variables</span>
	 * <span class="lang-ja">外部関数/変数を提供する, 各種のプラグインを接続します</span>
	 * .
	 *
	 * @param bindingName
	 *   <span class="lang-en">
	 *   A name in scripts of the variable/function/namespace provided by the connected plug-in.
	 *   If the passed argument contains a white space or a character "(", the content after it will be ignored.
	 *   By the above specification, for a function plug-in,
	 *   you can specify a signature containing parameter-declarations like "foo(int,float)"
	 *   (note that, syntax or correctness of parameter-declarations will not be checked).
	 *   In addition, for plug-ins providing elements belonging to the same namespace "Bar",
	 *   you can specify "Bar 1", "Bar 2", and so on.
	 *   This is helpful to avoid the duplication of keys when you use
	 *   {@link org.vcssl.nano.VnanoScriptEngine#put(String, Object) VnanoScriptEngine.put(String, Object) }
	 *   method which wraps this method.
	 *   Also, you can specify "___VNANO_AUTO_KEY" for using a valid value generated automatically.
	 *   </span>
	 *   <span class="lang-ja">
	 *   接続されるプラグインが提供する変数/関数/名前空間の, スクリプト内での名前.
	 *   内容に空白または「 ( 」が含まれている場合、それ以降は名前としては無視されます.
	 *   これにより, 例えば関数 "foo" に対して引数部を含めて "foo(int,float)" 等と指定したり
	 *   (引数部の構文や整合性検査などは行われません）,
	 *   同じ名前空間 "Bar" の要素を提供するプラグイン群に対して "Bar 1", "Bar 2", ... のように指定する事ができます.
	 *   これは, このメソッドをラップしている
	 *   {@link org.vcssl.nano.VnanoScriptEngine#put(String, Object) VnanoScriptEngine.put(String, Object) }
	 *   メソッドにおいて, キーの重複を避けたい場合に有効です.
	 *   なお, "___VNANO_AUTO_KEY" を指定する事で, 有効な値の指定を自動で行う事もできます.
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
	public void connectPlugin(String bindingName, Object plugin) throws VnanoException {
		if (bindingName == null || plugin == null) {
			throw new NullPointerException();
		}
		this.interconnect.connectPlugin(bindingName, plugin);
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


	/**
	 * <span class="lang-en">Gets the Map (performance map) storing names and values of performance monitoring items</span>
	 * <span class="lang-ja">パフォーマンスモニタの計測項目名と値を格納するマップ（パフォーマンスマップ）を取得します</span>
	 * .
	 * <span class="lang-en">
	 * Note that, when some measured values for some monitoring items don't exist
	 * (e.g.: when any scripts are not running, or running but their performance values are not measualable yet),
	 * the returned performance map does not contain values for such monitoring items,
	 * so sometimes the returned performance map is incomplete (missing values for some items) or empty.
	 * Please be careful of the above point when you "get" measured performance values from the returned performance map.
	 * </span>
	 * <span class="lang-ja">
	 * なお, スクリプトを実行していない時や, 実行開始後でも性能を有効に計測可能な段階にまだ達していない時など,
	 * 一部の計測値が存在しないタイミングでは, それらの値は戻り値のパフォーマンスマップ内には格納されません.
	 * そのような「欠けた」または「空の」パフォーマンスマップが有り得る事には, 計測値を取り出す際に留意する必要があります.
	 * </span>
	 *
	 * @return
	 *   <span class="lang-en">A Map (performance map) storing names and values of performance monitoring items</span>
	 *   <span class="lang-ja">パフォーマンスモニタの計測項目名と値を格納するマップ（パフォーマンスマップ）</span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">
	 *   Thrown when the option
	 *   {@link org.vcssl.nano.spec.OptionKey#PERFORMANCE_MONITOR_ENABLED MONITOR_ENABLED} is disabled.
	 *   </span>
	 *   <span class="lang-ja">
	 *   {@link org.vcssl.nano.spec.OptionKey#PERFORMANCE_MONITOR_ENABLED MONITOR_ENABLED}
	 *   オプションが無効化されていた場合にスローされます.
	 *   </span>
	 */
	public Map<String, Object> getPerformanceMap() throws VnanoException {
		synchronized (this.lock) {
			if (! (boolean)this.interconnect.getOptionMap().get(OptionKey.PERFORMANCE_MONITOR_ENABLED) ) {
				throw new VnanoException(ErrorType.PERFORMANCE_MONITOR_IS_DISABLED);
			}

			Map<String, Object> performanceMap = new LinkedHashMap<String, Object>();

			// VMのインスタンス生成が済んでいれば、VM関連の計測値を取得して格納
			if (this.virtualMachine != null) {

				// インスタンス生成時点から処理された命令数の累積値を取得して格納
				int instructionCount = this.virtualMachine.getProcessedInstructionCountIntValue();
				performanceMap.put(PerformanceKey.PROCESSED_INSTRUCTION_COUNT_INT_VALUE, instructionCount);

				// 実行中の命令のオペコードを取得し、列挙型から文字列表現に変換して格納
				//（アイドリング時は空配列が返るので、その場合は「値無し」扱いでマップに詰めない）
				OperationCode[] currentOpcodes = this.virtualMachine.getCurrentlyExecutedOperationCodes();
				if (currentOpcodes.length != 0) {
					String[] opcodeStrings = new String[ currentOpcodes.length ];
					for (int i=0; i<currentOpcodes.length; i++) {
						opcodeStrings[i] = currentOpcodes[i].toString();
					}
					performanceMap.put(PerformanceKey.CURRENTLY_EXECUTED_OPERATION_CODE, opcodeStrings);
				}
			}

			return performanceMap;
		}
	}
}
