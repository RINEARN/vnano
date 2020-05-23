/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.vcssl.connect.ClassToXnci1Adapter;
import org.vcssl.connect.ConnectorException;
import org.vcssl.connect.ExternalFunctionConnectorInterface1;
import org.vcssl.connect.ExternalNamespaceConnectorInterface1;
import org.vcssl.connect.ExternalVariableConnectorInterface1;
import org.vcssl.connect.FieldToXvci1Adapter;
import org.vcssl.connect.MethodToXfci1Adapter;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.IdentifierSyntax;
import org.vcssl.nano.spec.LanguageSpecContainer;
import org.vcssl.nano.spec.SpecialBindingKey;
import org.vcssl.nano.vm.VirtualMachineObjectCode;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.VnanoException;

/**
 * <p>
 * <span>
 * <span class="lang-en">
 * The class performing functions to manage and to provide some information
 * shared between multiple components in the script engine of the Vnano
 * </span>
 * <span class="lang-ja">
 * Vnanoのスクリプトエンジン内の各コンポーネント間で共有される,
 * いくつかの情報を管理・提供する機能を担うクラスです
 * </span>
 * .
 * </p>
 *
 * <p>
 * <span class="lang-en">
 * For example, information to resolve references of variables and functions
 * are managed by this class in the script engine.
 * Also, calling of external functions and synchronization of data of external variables
 * are taken through this class.
 * This class supports various types of plug-in connection interfaces,
 * and plug-ins are internally connected to this class in the script engine.
 * This kind of object is referred as "Interconnect" in the script engine of Vnano.
 * </span>
 *
 * <span class="lang-ja">
 * 関数・変数の参照解決のための情報は, スクリプトエンジン内で, このクラスが管理・提供します.
 * 外部関数の呼び出しや、外部変数とのデータの同期（処理終了後の書き戻しなど）も,
 * このクラスを介して行われます.
 * 従って, 外部変数・外部関数プラグインの接続インターフェースをサポートし,
 * （必要によりアダプタなどを介して）
 * 処理系内で最終的にプラグインが接続されるハブとなるのも、このクラスです.
 * VCSSL/Vnano処理系では、このような役割を提供するオブジェクトをインターコネクトと呼びます.
 * </span>
 * </p>
 *
 * <p>
 * &raquo <a href="../../../../../src/org/vcssl/nano/interconnect/Interconnect.java">Source code</a>
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class Interconnect {

	/** 各種の言語仕様設定類を格納するコンテナを保持します。 */
	private final LanguageSpecContainer LANG_SPEC;

	/** 上記コンテナ内の、識別子の判定規則類が定義された設定オブジェクトを保持します。 */
	private final IdentifierSyntax IDENTIFIER_SYNTAX;


	/** 外部関数の情報を保持する関数テーブルです。 */
	private FunctionTable externalFunctionTable = null;

	/** 外部変数の情報を保持するグローバル変数テーブルです。 */
	private VariableTable externalVariableTable = null;

	/** プラグインからスクリプトエンジンにアクセスする際に使用するコネクタです。 */
	private EngineConnector engineConnector = null;


	/** 初期化/終了時処理のため、接続されているXNCI形式のプラグインを一括で保持するリストです。 */
	private List<ExternalNamespaceConnectorInterface1> xnci1PluginList = null;

	/** 初期化/終了時処理のため、接続されているXFCI形式のプラグインを一括で保持するリストです。 */
	private List<ExternalFunctionConnectorInterface1> xfci1PluginList = null;

	/** 初期化/終了時処理のため、接続されているXVCI形式のプラグインを一括で保持するリストです。 */
	private List<ExternalVariableConnectorInterface1> xvci1PluginList = null;


	/**
	 * <span class="lang-en">
	 * Creates a blank interconnect to which nothing are connected,
	 * with the specified language specification settings
	 * </span>
	 * <span class="lang-ja">
	 * 指定された言語仕様設定で, 何も接続されていない空のインターコネクトを生成します
	 * </span>
	 * .
	 * @param langSpec
	 *   <span class="lang-en">language specification settings.</span>
	 *   <span class="lang-ja">言語仕様設定.</span>
	 */
	public Interconnect(LanguageSpecContainer langSpec) {
		this.LANG_SPEC = langSpec;
		this.IDENTIFIER_SYNTAX = LANG_SPEC.IDENTIFIER_SYNTAX;
		this.externalFunctionTable = new FunctionTable(LANG_SPEC);
		this.externalVariableTable = new VariableTable(LANG_SPEC);
		this.xnci1PluginList = new ArrayList<ExternalNamespaceConnectorInterface1>();
		this.xfci1PluginList = new ArrayList<ExternalFunctionConnectorInterface1>();
		this.xvci1PluginList = new ArrayList<ExternalVariableConnectorInterface1>();
	}


	/**
	 * <span class="lang-en">
	 * Turns to the active state on which scripts are executable, with initializing all connected plug-ins
	 * </span>
	 * <span class="lang-ja">
	 * 接続されている全てのプラグインの初期化処理を行い, スクリプトを実行可能な待機状態に遷移します
	 * </span>
	 * .
	 */
	public void activate() throws VnanoException {

		// 実行時用パーミッションマップのデフォルト初期化処理などを実行
		if (this.engineConnector != null) {
			this.engineConnector.activate();
		}

		// 接続されている全プラグインの、スクリプト実行毎の初期化処理を実行
		this.initializeAllPluginsForExecution();
	}


	/**
	 * <span class="lang-en">
	 * Turns to the idle state (on which scripts are not executable), with finalizing all connected plug-ins
	 * </span>
	 * <span class="lang-ja">
	 * 接続されている全てのプラグインの終了時処理を行い, 待機状態（スクリプトは実行不可能）に遷移します.
	 * </span>
	 * .
	 */
	public void deactivate() throws VnanoException {

		// 接続されている全プラグインの、スクリプト実行毎の終了時処理を実行
		this.finalizeAllPluginsForTermination();

		// 実行時用パーミッションマップのクリアなどを実行
		if (this.engineConnector != null) {
			this.engineConnector.deactivate();
		}
	}


	/**
	 * <span class="lang-en">
	 * Sets the connector to access the engine from plug-ins (engine connector)
	 * </span>
	 * <span class="lang-ja">
	 * プラグインからスクリプトエンジンにアクセスするための, エンジンコネクタを指定します
	 * </span>
	 * .
	 * @param engineConnector
	 *   <span class="lang-en">The engine connector to be passed to plug-ins</span>
	 *   <span class="lang-ja">プラグインに渡すエンジンコネクタ</span>
	 */
	public void setEngineConnector(EngineConnector engineConnector) {
		this.engineConnector = engineConnector;
	}


	/**
	 * <span class="lang-en">
	 * Returns the table storing information of external functions (external function table)
	 * </span>
	 * <span class="lang-ja">
	 * 外部関数の情報を保持する関数テーブル（外部関数テーブル）を返します
	 * </span>
	 * .
	 * @return
	 *   <span class="lang-en">The external function table</span>
	 *   <span class="lang-ja">外部関数テーブル</span>
	 */
	public FunctionTable getExternalFunctionTable() {
		return this.externalFunctionTable;
	}


	/**
	 * <span class="lang-en">
	 * Returns the table storing information of external variable (external variable table)
	 * </span>
	 * <span class="lang-ja">
	 * 外部変数の情報を保持する変数テーブル（外部変数テーブル）を返します
	 * </span>
	 * .
	 * @return
	 *   <span class="lang-ja">外部変数テーブル</span>
	 *   <span class="lang-en">The external variable table</span>
	 */
	public VariableTable getExternalVariableTable() {
		return this.externalVariableTable;
	}


	/**
	 * <span class="lang-en">
	 * Calls the external function registered at the element with specified index in the external function table
	 * </span>
	 * <span class="lang-ja">
	 * 外部関数テーブル内の, 指定されたインデックスの要素に登録されている外部関数をコールします
	 * </span>
	 * .
	 *
	 * @param functionIndex
	 *   <span class="lang-ja">The index of the extenral function to be called.</span>
	 *   <span class="lang-ja">コールする外部関数のインデックス.</span>
	 *
	 * @param arguments
	 *   <span class="lang-en">Data containers storing arguments to be passed to the callee function.</span>
	 *   <span class="lang-ja">呼び出す関数に渡す引数を格納するデータユニット配列.</span>
	 *
	 * @param returnData
	 *   <span class="lang-en">Data container storing returned value from the callee function.</span>
	 *   <span class="lang-ja">呼び出した関数からの戻り値を格納するデータユニット.</span>
	 */
	public void callExternalFunction(int functionIndex, DataContainer<?>[] arguments, DataContainer<?> returnData)
			throws VnanoException {

		this.externalFunctionTable.getFunctionByIndex(functionIndex).invoke(arguments, returnData);
	}


	/**
	 * <span class="lang-en">
	 * Writebacks data to external variables from the virtual memory of VM.
	 * </span>
	 * <span class="lang-ja">
	 * VMの仮想メモリーから外部変数にデータを書き戻します
	 * </span>
	 * .
	 * <span class="lang-en">
	 * This method will be used for synchronization of data after execution of scripts.
	 * </span>
	 *
	 * <span class="lang-ja">
	 * このメソッドは, スクリプト実行終了後にデータを同期するために使用されます.
	 * </span>
	 *
	 * @param memory
	 *   <span class="lang-en">The virtual memory which was used for execution of the script.</span>
	 *   <span class="lang-ja">スクリプトの実行に使用した仮想メモリー.</span>
	 */
	public void writebackExternalVariables(Memory memory, VirtualMachineObjectCode intermediateCode)
			throws VnanoException {

		// グローバル変数の書き戻し
		int maxGlobalAddress = intermediateCode.getMaximumGlobalAddress();
		int minGlobalAddress = intermediateCode.getMinimumGlobalAddress();
		for (int address=minGlobalAddress; address<=maxGlobalAddress; address++) {

			// 注目アドレスのグローバル変数に、スクリプト内のどこからもアクセスしていない場合はスキップ
			if (!intermediateCode.hasGlobalVariableRegisteredAt(address)) {
				continue;
			}

			// 仮想メモリーを参照し、グローバル変数アドレスからデータコンテナを取得
			DataContainer<?> dataContainer = memory.getDataContainer(Memory.Partition.GLOBAL, address);

			// 中間コードのシンボルテーブルを参照し、グローバル変数アドレスから一意識別子を取得
			String identifier = intermediateCode.getGlobalVariableUniqueIdentifier(address);

			// グローバル変数テーブルを参照し、一意識別子から外部変数オブジェクトを取得
			AbstractVariable variable = this.externalVariableTable.getVariableByAssemblyIdentifier(identifier);

			// 書き換え不可能な定数の場合はスキップ
			if (variable.isConstant()) {
				continue;
			}

			// 外部変数オブジェクトにデータコンテナを渡して値を更新させる
			variable.setDataContainer(dataContainer);
		}
	}


	/**
	 * <span class="lang-en">Connects various types of plug-ins which provides external functions/variables</span>
	 * <span class="lang-ja">外部関数/変数を提供する, 各種のプラグインを接続します</span>
	 * .
	 * @param bindingKey
	 *   <span class="lang-en">
	 *   An unique key to identify the plug-in.
	 *   It also works as an identifier of a connected external variable/functuion/namespace,
	 *   where identifiers of functions should be described as the format of prototypes, e.g.: "foo(int,float)".
	 *   Also, you can specify "___VNANO_AUTO_KEY" for generate key automatically.
	 *   </span>
	 *   <span class="lang-ja">
	 *   プラグインを一意に識別するためのキー.
	 *   キーの内容は, 接続される外部関数/変数/名前空間にスクリプト内からアクセスするための識別子としても機能します.
	 *   そのためには, 関数についてはプロトタイプの書式（ 例えば "foo(int,float)" 等 ）で記述される必要があります.
	 *   なお, "___VNANO_AUTO_KEY" を指定する事で, キーを自動生成する事もできます.
	 *   </span>
	 *
	 * @param plugin
	 *   <span class="lang-en">
	 *   The plug-in to be connected.
	 *   General "Object" type instances can be connected as a plug-in,
	 *   for accessing their methods/fields from the script code as external functions/variables.
	 *   For accessing only static methods and fields, "Class&lt;T&gt;" type instance can also be connected.
	 *   In addition, if you want to choose a method/field to be accessible from script code,
	 *   a "Method"/"Field" type instance can be connected.
	 *   ( In that case, if the method/field is static, pass an Object type array for this argument,
	 *     and store the Method/Field type instance at [0],
	 *     and store "Class&lt;T&gt;" type instance of the class defining the method/field at [1] ).
	 *   Furthermore, the instance of the class implementing
	 *   {@link org.vcssl.connect.ExternalFunctionConnectorInterface1 XFCI1} /
	 *   {@link org.vcssl.connect.ExternalVariableConnectorInterface1 XVCI1} /
	 *   {@link org.vcssl.connect.ExternalNamespaceConnectorInterface1 XNCI1}
	 *   type less-overhead plug-in interface can be connected.
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

		try {
			// 初期化が必要なプラグインの場合は、全てのアクセスよりも前にここで初期化する
			this.initializePluginForConnection(plugin);

			// Replace the binding key with auto-generated one if, it it is requested.
			// キーを自動生成するよう設定されている場合は、キーを置き換え
			if (bindingKey.equals(SpecialBindingKey.AUTO_KEY)) {
				bindingKey = this.generateBindingKeyOf(plugin);     // これ、ここでシグネチャ求める前にプラグインを init する必要がる？
			}

			// XVCI 1 形式の外部変数プラグイン
			if (plugin instanceof ExternalVariableConnectorInterface1) {
				this.connectXvci1Plugin( (ExternalVariableConnectorInterface1)plugin, true, bindingKey );

			// XFCI 1 形式の外部関数プラグイン
			} else if (plugin instanceof ExternalFunctionConnectorInterface1) {
				this.connectXfci1Plugin( (ExternalFunctionConnectorInterface1)plugin, true, bindingKey );

			// XNCI 1 形式の外部関数プラグイン
			} else if (plugin instanceof ExternalNamespaceConnectorInterface1) {
				this.connectXnci1Plugin( (ExternalNamespaceConnectorInterface1)plugin, true, bindingKey, false );

			// クラスフィールドの場合
			} else if (plugin instanceof Field) {
				this.connectFieldAsPlugin( (Field)plugin, null, true, bindingKey );

			// クラスメソッドの場合
			} else if (plugin instanceof Method) {
				this.connectMethodAsPlugin( (Method)plugin, null, true, bindingKey );

			// クラスの場合
			} else if (plugin instanceof Class) {
				this.connectClassAsPlugin( (Class<?>)plugin, null, true, bindingKey );

			// インスタンスフィールドやインスタンスメソッド等は、所属インスタンスも格納する配列で渡される
			} else if (plugin instanceof Object[]) {

				Object[] objects = (Object[])plugin;

				// インスタンスフィールドの場合 >> 引数からFieldとインスタンスを取り出し、外部変数として接続
				if (objects.length == 2 && objects[0] instanceof Field) {
					Field field = (Field)objects[0]; // [0] はフィールドのリフレクション
					Object instance = objects[1];    // [1] はフィールドの所属インスタンス
					this.connectFieldAsPlugin( field, instance, true, bindingKey );

				// インスタンスメソッドの場合 >> 引数からMethodとインスタンスを取り出し、外部関数として接続
				} else if (objects.length == 2 && objects[0] instanceof Method) {
					Method method = (Method)objects[0]; // [0] はメソッドのリフレクション
					Object instance = objects[1];       // [1] はメソッドの所属インスタンス
					this.connectMethodAsPlugin( method, instance, true, bindingKey );

				// クラスの場合 >> 引数からClassとインスタンスを取り出し、外部ライブラリとして接続
				} else if (objects.length == 2 && objects[0] instanceof Class) {
					Class<?> pluginClass = (Class<?>)objects[0];
					Object instance = objects[1];
					this.connectClassAsPlugin( pluginClass, instance, true, bindingKey );

				} else {
					throw new VnanoException(
						ErrorType.UNSUPPORTED_PLUGIN, new String[] {objects[0].getClass().getCanonicalName()}
					);
				}

			// その他のオブジェクトは、Classを取得して外部ライブラリとして接続
			} else {
				Class<?> pluginClass = plugin.getClass();
				this.connectClassAsPlugin( pluginClass, plugin, true, bindingKey );
			}

		// 内部で VnanoException が発生した場合は、原因プラグインを特定できるメッセージを持たせた VnanoException でラップして投げる
		} catch (VnanoException vne) {
			throw new VnanoException(ErrorType.PLUGIN_CONNECTION_FAILED, bindingKey, vne);
		}
	}


	/**
	 * <span class="lang-en">Disconnects all plug-ins</span>
	 * <span class="lang-ja">全てのプラグインの接続を解除します</span>
	 * .
	 * <span class="lang-en">
	 * If the finalization (for disconnection) method is implemented on the plug-in,
	 * it will be called when the plug-in will be disconnected by this method.
	 * </span>
	 * <span class="lang-ja">
	 * その際, 接続解除用の終了時処理が実装されているプラグインに対しては, その終了時処理が実行されます.
	 * </span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">
	 *   Thrown when an exception occurred on the finalization of the plug-in to be disconnected.
	 *   </span>
	 *   <span class="lang-ja">
	 *   プラグインの終了時処理でエラーが発生した場合にスローされます.
	 *   </span>
	 */
	public void disconnectAllPlugins() throws VnanoException {
		this.finalizeAllPluginsForDisconnection();
		this.externalFunctionTable = new FunctionTable(LANG_SPEC);
		this.externalVariableTable = new VariableTable(LANG_SPEC);
		this.xnci1PluginList = new ArrayList<ExternalNamespaceConnectorInterface1>();
		this.xfci1PluginList = new ArrayList<ExternalFunctionConnectorInterface1>();
		this.xvci1PluginList = new ArrayList<ExternalVariableConnectorInterface1>();
	}


	/**
	 * <span class="lang-en">
	 * Generate the value of the argument "bindingKey" of
	 * {@link Interconnect#connectPlugin connectPlugin(String bindingKey, Object plugin)}
	 * method automatically
	 * </span>
	 * <span class="lang-ja">
	 * {@link Interconnect#connectPlugin connectPlugin(String bindingKey, Object plugin)}
	 * メソッドの引数 bindingKey の値を自動生成します
	 * </span>
	 * .
	 * @param plugin
	 *   <span class="lang-en">
	 *   The value passed as the argument "plugin" of
	 *   {@link Interconnect#connectPlugin connectPlugin(String bindingKey, Object plugin)}
	 *   method.
	 *   </span>
	 *   <span class="lang-ja">
	 *   {@link Interconnect#connectPlugin connectPlugin(String bindingKey, Object plugin)}
	 *   メソッドに渡された引数 plugin の値.
	 *   </span>
	 *
	 * @return
	 *   <span class="lang-en">The generated value</span>
	 *   <span class="lang-ja">自動生成された値</span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">
	 *   Thrown if the plug-in could not be analyzed,
	 *   caused by unsupported interfaces, incompatibility of data types, and so on.
	 *   </span>
	 *   <span class="lang-ja">
	 *   サポートされていないインターフェースの使用や, データ型の互換性などの原因により,
	 *   プラグインの解析に失敗した場合にスローされます.
	 *   </span>
	 */
	private String generateBindingKeyOf(Object plugin) throws VnanoException {

		// 内部変数と互換の変数オブジェクト
		if (plugin instanceof AbstractVariable) {
			return ((AbstractVariable)plugin).getVariableName();

		// 内部関数と互換の変数オブジェクト
		} else if (plugin instanceof AbstractFunction) {
			return IDENTIFIER_SYNTAX.getSignatureOf((AbstractFunction)plugin);

		// XVCI 1 形式の外部変数プラグイン
		} else if (plugin instanceof ExternalVariableConnectorInterface1) {
			return ((ExternalVariableConnectorInterface1)plugin).getVariableName();

		// XFCI 1 形式の外部関数プラグイン
		} else if (plugin instanceof ExternalFunctionConnectorInterface1) {
			AbstractFunction functionAdapter =
					new Xfci1ToFunctionAdapter((ExternalFunctionConnectorInterface1)plugin, LANG_SPEC);
			return IDENTIFIER_SYNTAX.getSignatureOf(functionAdapter);

		// XNCI 1 形式の外部関数プラグイン
		} else if (plugin instanceof ExternalNamespaceConnectorInterface1) {
			return ((ExternalNamespaceConnectorInterface1)plugin).getNamespaceName();

		// クラスフィールドの場合
		} else if (plugin instanceof Field) {
			return ((Field)plugin).getName();

		// クラスメソッドの場合
		} else if (plugin instanceof Method) {
			ExternalFunctionConnectorInterface1 xfci1Adapter = new MethodToXfci1Adapter((Method)plugin);
			AbstractFunction functionAdapter = new Xfci1ToFunctionAdapter(xfci1Adapter, LANG_SPEC);
			return IDENTIFIER_SYNTAX.getSignatureOf(functionAdapter);

		// クラスの場合
		} else if (plugin instanceof Class) {
			return ((Class<?>)plugin).getCanonicalName();

		// インスタンスフィールドやインスタンスメソッド等は、所属インスタンスも格納する配列で渡される
		} else if (plugin instanceof Object[]) {

			Object[] objects = (Object[])plugin;

			// インスタンスフィールドの場合
			if (objects.length == 2 && objects[0] instanceof Field) {
				Field field = (Field)objects[0]; // [0] はフィールドのリフレクション
				return generateBindingKeyOf(field);

			// インスタンスメソッドの場合
			} else if (objects.length == 2 && objects[0] instanceof Method) {
				Method method = (Method)objects[0]; // [0] はメソッドのリフレクション
				return generateBindingKeyOf(method);

			// クラスの場合 >> 引数からClassとインスタンスを取り出し、外部ライブラリとして接続
			} else if (objects.length == 2 && objects[0] instanceof Class) {
				Class<?> pluginClass = (Class<?>)objects[0];
				return generateBindingKeyOf(pluginClass);
			} else {
				throw new VnanoException(
					ErrorType.UNSUPPORTED_PLUGIN, new String[] {objects[0].getClass().getCanonicalName()}
				);
			}

		// その他のオブジェクトは、Classを取得して外部ライブラリとして接続
		} else {
			Class<?> pluginClass = plugin.getClass();
			return generateBindingKeyOf(pluginClass);
		}
	}


	/**
	 * <span class="lang-ja">Field 型のインスタンスを外部変数プラグインとして接続します</span>
	 * <span class="lang-en">Connects a Field type instance as a plug-in</span>
	 * .
	 * <span class="lang-en">
	 * This connection makes the field reflected by the passed Field type instance accessible
	 * from scripts as the external variable.
	 * </span>
	 * <span class="lang-ja">
	 * この接続により、指定した Field 型インスタンスによって参照されているフィールドに,
	 * スクリプト内から外部変数としてアクセス可能になります.
	 * </span>
	 *
	 * @param field
	 *   <span class="lang-en">
	 *   The Field type instance reflecting the field to be accessed from scripts.
	 *   </span>
	 *   <span class="lang-ja">
	 *   スクリプト内からアクセスしたいフィールドを参照しているField型インスタンス.
	 *   </span>
	 *
	 * @param instance
	 *   <span class="lang-en">
	 *   The instance of the class in which the method is defined.
	 *   If the method is static, it allows null to be passed.
	 *   </span>
	 *   <span class="lang-ja">
	 *   メソッドが属するクラスのインスタンス. static メソッドについては null を指定できます.
	 *   </span>
	 *
	 * @param aliasingRequired
	 *   <span class="lang-en">Whether use the alias for accessing from scripts or not ("true" for use).</span>
	 *   <span class="lang-ja">スクリプト内から別名でアクセスするかどうか（する場合にtrue）.</span>
	 *
	 * @param aliasName
	 *   <span class="lang-en">The alias for accessing from scripts.</span>
	 *   <span class="lang-ja">スクリプト内での別名.</span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">
	 *   Thrown if the plug-in could not be connected, caused by incompatibility of data types, and so on.
	 *   </span>
	 *   <span class="lang-ja">
	 *   データ型の互換性などの原因により, プラグインの接続に失敗した場合にスローされます.
	 *   </span>
	 */
	private void connectFieldAsPlugin(Field field, Object instance, boolean aliasingRequired, String aliasName)
			throws VnanoException {

		FieldToXvci1Adapter adapter = new FieldToXvci1Adapter(field, instance);
		this.connectXvci1Plugin(adapter, aliasingRequired, aliasName);
	}


	/**
	 * <span class="lang-ja">Method 型のインスタンスを外部変数プラグインとして接続します</span>
	 * <span class="lang-en">Connects a Method type instance as a plug-in</span>
	 * .
	 * <span class="lang-en">
	 * This connection makes the method reflected by the passed Method type instance accessible
	 * from scripts as the external function.
	 * </span>
	 * <span class="lang-ja">
	 * この接続により、指定した Method 型インスタンスによって参照されているメソッドに,
	 * スクリプト内から外部関数としてアクセス可能になります.
	 * </span>
	 *
	 * @param method
	 *   <span class="lang-en">
	 *   The Method type instance reflecting the method to be accessed from scripts.
	 *   </span>
	 *   <span class="lang-ja">
	 *   スクリプト内からアクセスしたいメソッドを参照しているMethod型インスタンス.
	 *   </span>
	 *
	 * @param instance
	 *   <span class="lang-en">
	 *   The instance of the class to which the method belongs.
	 *   If the method is static, it allows null to be passed.
	 *   </span>
	 *   <span class="lang-ja">
	 *   メソッドが属するクラスのインスタンス. static メソッドについては null を指定できます.
	 *   </span>
	 *
	 * @param aliasingRequired
	 *   <span class="lang-en">Whether use the alias for accessing from scripts or not ("true" for use).</span>
	 *   <span class="lang-ja">スクリプト内から別名でアクセスするかどうか（する場合にtrue）.</span>
	 *
	 * @param aliasName
	 *   <span class="lang-en">The alias for accessing from scripts.</span>
	 *   <span class="lang-ja">スクリプト内での別名.</span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">
	 *   Thrown if the plug-in could not be connected, caused by incompatibility of data types, and so on.
	 *   </span>
	 *   <span class="lang-ja">
	 *   データ型の互換性などの原因により, プラグインの接続に失敗した場合にスローされます.
	 *   </span>
	 */
	private void connectMethodAsPlugin(Method method, Object instance, boolean aliasingRequired, String aliasSignature)
			throws VnanoException {

		MethodToXfci1Adapter adapter = new MethodToXfci1Adapter(method,instance);
		this.connectXfci1Plugin(adapter, aliasingRequired, aliasSignature);
	}


	/**
	 * <span class="lang-ja">Class&lt;T&gt; 型のインスタンスを外部変数プラグインとして接続します</span>
	 * <span class="lang-en">Connects a Class&lt;T&gt; type instance as a plug-in</span>
	 * .
	 * <span class="lang-en">
	 * This connection makes methods and fields which belong to
	 * "the class T reflected by the passed Class&lt;T&gt; type instance"
	 * accessible from scripts as the external functions and variables.
	 * </span>
	 * <span class="lang-ja">
	 * この接続により、「 指定した Class&lt;T&gt; 型インスタンスによって参照されているクラス（T） 」
	 * に属するメソッドやフィールド（複数）に, スクリプト内から外部関数や外部変数としてアクセス可能になります.
	 * </span>
	 *
	 * @param pluginClass
	 *   <span class="lang-en">
	 *   The Class&lt;T&gt; type instance reflecting the class T to which
	 *   methods and fields to be accessed from scripts belong.
	 *   </span>
	 *   <span class="lang-ja">
	 *   スクリプト内からアクセスしたいメソッドやフィールドが属するクラス T を参照している
	 *   Class&lt;T&gt; 型インスタンス.
	 *   </span>
	 *
	 * @param instance
	 *   <span class="lang-en">
	 *   The instance of the class of T.
	 *   If all methods and fields to be accessed are static, it allows null to be passed.
	 *   </span>
	 *   <span class="lang-ja">
	 *   クラス T のインスタンス. static なメソッド/フィールドのみにアクセスする場合は null を指定できます.
	 *   </span>
	 *
	 * @param aliasingRequired
	 *   <span class="lang-en">Whether use the alias for accessing from scripts or not ("true" for use).</span>
	 *   <span class="lang-ja">スクリプト内から別名でアクセスするかどうか（する場合にtrue）.</span>
	 *
	 * @param aliasName
	 *   <span class="lang-en">The alias for accessing from scripts.</span>
	 *   <span class="lang-ja">スクリプト内での別名.</span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">
	 *   Thrown if the plug-in could not be connected, caused by incompatibility of data types, and so on.
	 *   </span>
	 *   <span class="lang-ja">
	 *   データ型の互換性などの原因により, プラグインの接続に失敗した場合にスローされます.
	 *   </span>
	 */
	private void connectClassAsPlugin(Class<?> pluginClass, Object instance, boolean aliasingRequired, String aliasName)
			throws VnanoException {
		ClassToXnci1Adapter adapter = new ClassToXnci1Adapter(pluginClass,instance);
		this.connectXnci1Plugin(adapter, aliasingRequired, aliasName, true);
	}


	/**
	 * <span class="lang-ja">
	 * 外部変数を提供する, {@link org.vcssl.connect.ExternalVariableConnectorInterface1 XVCI1}
	 * 形式のプラグインを接続します
	 * </span>
	 * <span class="lang-en">
	 * Connects the plug-in of {@link org.vcssl.connect.ExternalVariableConnectorInterface1 XVCI1} format,
	 * which provides an external variable
	 * </span>
	 * .
	 * @param plugin
	 *   <span class="lang-en">The plug-in to be connected.</span>
	 *   <span class="lang-ja">接続するプラグイン.</span>
	 *
	 * @param aliasingRequired
	 *   <span class="lang-en">Whether use the alias for accessing from scripts or not ("true" for using).</span>
	 *   <span class="lang-ja">スクリプト内から別名でアクセスするかどうか（する場合にtrue）.</span>
	 *
	 * @param aliasName
	 *   <span class="lang-en">The alias for accessing from scripts.</span>
	 *   <span class="lang-ja">スクリプト内からのアクセスに使用する別名.</span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">
	 *   Thrown if the plug-in could not be connected, caused by incompatibility of data types, and so on.
	 *   </span>
	 *   <span class="lang-ja">
	 *   データ型の互換性などの原因により, プラグインの接続に失敗した場合にスローされます.
	 *   </span>
	 */
	private void connectXvci1Plugin(ExternalVariableConnectorInterface1 plugin, boolean aliasingRequired, String aliasName)
			throws VnanoException {
		this.xvci1PluginList.add(plugin);
		Xvci1ToVariableAdapter adapter = new Xvci1ToVariableAdapter(plugin, LANG_SPEC);
		this.connectVariable(adapter, aliasingRequired, aliasName);
	}


	/**
	 * <span class="lang-ja">
	 * 外部関数を提供する, {@link org.vcssl.connect.ExternalFunctionConnectorInterface1 XFCI1}
	 * 形式のプラグインを接続します
	 * </span>
	 * <span class="lang-en">
	 * Connects the plug-in of {@link org.vcssl.connect.ExternalFunctionConnectorInterface1 XFCI1} format,
	 * which provides an external function
	 * </span>
	 * .
	 * @param plugin
	 *   <span class="lang-en">The plug-in to be connected.</span>
	 *   <span class="lang-ja">接続するプラグイン.</span>
	 *
	 * @param aliasingRequired
	 *   <span class="lang-en">Whether use the alias for accessing from scripts or not ("true" for using).</span>
	 *   <span class="lang-ja">スクリプト内から別名でアクセスするかどうか（する場合にtrue）.</span>
	 *
	 * @param aliasName
	 *   <span class="lang-en">The alias for accessing from scripts.</span>
	 *   <span class="lang-ja">スクリプト内からのアクセスに使用する別名.</span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">
	 *   Thrown if the plug-in could not be connected, caused by incompatibility of data types, and so on.
	 *   </span>
	 *   <span class="lang-ja">
	 *   データ型の互換性などの原因により, プラグインの接続に失敗した場合にスローされます.
	 *   </span>
	 */
	private void connectXfci1Plugin(ExternalFunctionConnectorInterface1 plugin, boolean aliasingRequired, String aliasSignature)
			throws VnanoException {
		this.xfci1PluginList.add(plugin);
		Xfci1ToFunctionAdapter adapter = new Xfci1ToFunctionAdapter(plugin, LANG_SPEC);
		this.connectFunction(adapter, aliasingRequired, aliasSignature);
	}


	/**
	 * <span class="lang-ja">
	 * 複数の外部変数や外部関数をまとめて提供する,
	 * {@link org.vcssl.connect.ExternalNamespaceConnectorInterface1 XNCI1} 形式のプラグインを接続します
	 * </span>
	 * <span class="lang-en">
	 * Connects the plug-in of {@link org.vcssl.connect.ExternalNamespaceConnectorInterface1 XNCI1} format,
	 * which provides a set of external variables and external functions
	 * </span>
	 * .
	 * @param plugin
	 *   <span class="lang-en">The plug-in to be connected.</span>
	 *   <span class="lang-ja">接続するプラグイン.</span>
	 *
	 * @param aliasingRequired
	 *   <span class="lang-en">Whether use the alias for accessing from scripts or not ("true" for using).</span>
	 *   <span class="lang-ja">スクリプト内から別名でアクセスするかどうか（する場合にtrue）.</span>
	 *
	 * @param aliasName
	 *   <span class="lang-en">The alias for accessing from scripts.</span>
	 *   <span class="lang-ja">スクリプト内からのアクセスに使用する別名.</span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">
	 *   Thrown if the plug-in could not be connected, caused by incompatibility of data types, and so on.
	 *   </span>
	 *   <span class="lang-ja">
	 *   データ型の互換性などの原因により, プラグインの接続に失敗した場合にスローされます.
	 *   </span>
	 */
	private void connectXnci1Plugin(ExternalNamespaceConnectorInterface1 plugin, boolean aliasingRequired, String aliasName,
			boolean ignoreIncompatibles) throws VnanoException {

		// 名前空間（の名前）を取得し、エイリアスが指定されている場合はその名前で置き換える
		String nameSpace = plugin.getNamespaceName();
		if (aliasingRequired) {
			nameSpace = aliasName;
		}

		// 関数をアダプタで変換して接続
		ExternalFunctionConnectorInterface1[] xfciConnectors = plugin.getFunctions();
		for (ExternalFunctionConnectorInterface1 xfciConnector: xfciConnectors) {
			try {
				this.xfci1PluginList.add(xfciConnector); // 初期化/終了時処理で呼ぶ用の登録
				AbstractFunction adapter = new Xfci1ToFunctionAdapter(xfciConnector, nameSpace, LANG_SPEC);
				this.connectFunction(adapter, false, null); // スクリプトから呼ぶ用の接続

			} catch (VnanoException e) {
				if (!ignoreIncompatibles) {
					throw e;
				}
			}
		}

		// 変数をアダプタで変換して接続
		ExternalVariableConnectorInterface1[] xvciConnectors = plugin.getVariables();
		for (ExternalVariableConnectorInterface1 xvciConnector: xvciConnectors) {
			try {
				this.xvci1PluginList.add(xvciConnector); // 初期化/終了時処理で呼ぶ用の登録
				AbstractVariable adapter = new Xvci1ToVariableAdapter(xvciConnector, nameSpace, LANG_SPEC);
				this.connectVariable(adapter, false, null); // スクリプトから呼ぶ用の接続

			} catch (VnanoException e) {
				if (!ignoreIncompatibles) {
					throw e;
				}
			}
		}

		// 先のほうの初期化だけでなく、後のほうの初期化も正常に完了した段階でリストに登録する
		this.xnci1PluginList.add(plugin);
	}


	/**
	 * Vnano処理系内部の変数形式に準拠した変数オブジェクトを接続します。
	 * @param aliasingRequired スクリプト内から別名でアクセスするかどうか（する場合にtrue）
	 * @param aliasName スクリプト内での別名（aliasingRequiredがtrueの場合のみ参照されます）
	 *
	 * @param variable 変数オブジェクト
	 */
	private void connectVariable(AbstractVariable variable, boolean aliasingRequired, String aliasName) {
		if (aliasingRequired) {
			variable = new VariableAliasAdapter(variable);
			((VariableAliasAdapter)variable).setVariableName(aliasName);
		}
		this.externalVariableTable.addVariable(variable);
	}


	/**
	 * Vnano処理系内部の関数形式に準拠した関数オブジェクトを接続します。
	 *
	 * @param function 関数オブジェクト
	 * @param aliasingRequired スクリプト内から別名でアクセスするかどうか（する場合にtrue）
	 * @param aliasSignature 別名アクセスのためのコールシグネチャ（aliasingRequiredがtrueの場合のみ参照されます）
	 * @throws VnanoException 引数に指定されたコールシグネチャが正しくない場合にスローされます。
	 */
	private void connectFunction(AbstractFunction function, boolean aliasingRequired, String aliasSignature)
			throws VnanoException {

		if (aliasingRequired) {
			function = new FunctionAliasAdapter(function, LANG_SPEC);
			((FunctionAliasAdapter)function).setCallSignature(aliasSignature);
		}
		this.externalFunctionTable.addFunction(function);
	}


	private void initializePluginForConnection(Object plugin) throws VnanoException {
		Object initializingPlugin = null; // 例外発生時のため、初期化中のプラグインを控えておく
		try {

			// XNCI1の場合
			if (plugin instanceof ExternalNamespaceConnectorInterface1) {
				ExternalNamespaceConnectorInterface1 xnci1Plugin = (ExternalNamespaceConnectorInterface1)plugin;

				// 本体の preInit
				initializingPlugin = xnci1Plugin;
				((ExternalNamespaceConnectorInterface1)plugin).preInitializeForConnection(this.engineConnector);

				// 所属関数の init
				for (ExternalFunctionConnectorInterface1 function: xnci1Plugin.getFunctions()) {
					initializingPlugin = function;
					function.initializeForConnection(this.engineConnector);
				}

				// 所属変数の init
				for (ExternalVariableConnectorInterface1 variable: xnci1Plugin.getVariables()) {
					initializingPlugin = variable;
					variable.initializeForConnection(this.engineConnector);
				}

				// 本体の postInit
				initializingPlugin = xnci1Plugin;
				((ExternalNamespaceConnectorInterface1)plugin).postInitializeForConnection(this.engineConnector);

			// XFCI1の場合
			} else if (plugin instanceof ExternalFunctionConnectorInterface1) {
				initializingPlugin = plugin;
				((ExternalFunctionConnectorInterface1)plugin).initializeForConnection(this.engineConnector);

			// XVCI1の場合
			} else if (plugin instanceof ExternalVariableConnectorInterface1) {
				initializingPlugin = plugin;
				((ExternalVariableConnectorInterface1)plugin).initializeForConnection(this.engineConnector);
			}

		} catch (ConnectorException e) {
			throw new VnanoException(
				ErrorType.PLUGIN_INITIALIZATION_FAILED, initializingPlugin.getClass().getCanonicalName(), e
			);
		}

		// 初期化が必要ないオブジェクトの場合は何もしない
	}


	private void finalizeAllPluginsForDisconnection() throws VnanoException {
		Object finalizingPlugin = null; // 例外発生時のため、終了時処理中のプラグインを控えておく
		try {
			// モジュールの preInit -> 関数/変数の init -> モジュールの postInit の順で初期化
			for (ExternalNamespaceConnectorInterface1 plugin: xnci1PluginList) {
				finalizingPlugin = plugin;
				plugin.preFinalizeForDisconnection(this.engineConnector);
			}
			for (ExternalFunctionConnectorInterface1 plugin: xfci1PluginList) {
				finalizingPlugin = plugin;
				plugin.finalizeForDisconnection(this.engineConnector);
			}
			for (ExternalVariableConnectorInterface1 plugin: xvci1PluginList) {
				finalizingPlugin = plugin;
				plugin.finalizeForDisconnection(this.engineConnector);
			}
			for (ExternalNamespaceConnectorInterface1 plugin: xnci1PluginList) {
				finalizingPlugin = plugin;
				plugin.postFinalizeForDisconnection(this.engineConnector);
			}
		} catch (ConnectorException e) {
			throw new VnanoException(
				ErrorType.PLUGIN_FINALIZATION_FAILED, finalizingPlugin.getClass().getCanonicalName(), e
			);
		}
	}


	private void initializeAllPluginsForExecution() throws VnanoException {
		Object initializingPlugin = null; // 例外発生時のため、初期化中のプラグインを控えておく
		try {
			// モジュールの preInit -> 関数/変数の init -> モジュールの postInit の順で初期化
			for (ExternalNamespaceConnectorInterface1 plugin: xnci1PluginList) {
				initializingPlugin = plugin;
				plugin.preInitializeForExecution(this.engineConnector);
			}
			for (ExternalFunctionConnectorInterface1 plugin: xfci1PluginList) {
				initializingPlugin = plugin;
				plugin.initializeForExecution(this.engineConnector);
			}
			for (ExternalVariableConnectorInterface1 plugin: xvci1PluginList) {
				initializingPlugin = plugin;
				plugin.initializeForExecution(this.engineConnector);
			}
			for (ExternalNamespaceConnectorInterface1 plugin: xnci1PluginList) {
				initializingPlugin = plugin;
				plugin.postInitializeForExecution(this.engineConnector);
			}
		} catch (ConnectorException e) {
			throw new VnanoException(
				ErrorType.PLUGIN_INITIALIZATION_FAILED, initializingPlugin.getClass().getCanonicalName(), e
			);
		}
	}


	private void finalizeAllPluginsForTermination() throws VnanoException {
		Object finalizingPlugin = null; // 例外発生時のため、終了時処理中のプラグインを控えておく
		try {
			// モジュールの preInit -> 関数/変数の init -> モジュールの postInit の順で初期化
			for (ExternalNamespaceConnectorInterface1 plugin: xnci1PluginList) {
				finalizingPlugin = plugin;
				plugin.preFinalizeForTermination(this.engineConnector);
			}
			for (ExternalFunctionConnectorInterface1 plugin: xfci1PluginList) {
				finalizingPlugin = plugin;
				plugin.finalizeForTermination(this.engineConnector);
			}
			for (ExternalVariableConnectorInterface1 plugin: xvci1PluginList) {
				finalizingPlugin = plugin;
				plugin.finalizeForTermination(this.engineConnector);
			}
			for (ExternalNamespaceConnectorInterface1 plugin: xnci1PluginList) {
				finalizingPlugin = plugin;
				plugin.postFinalizeForTermination(this.engineConnector);
			}
		} catch (ConnectorException e) {
			throw new VnanoException(
				ErrorType.PLUGIN_FINALIZATION_FAILED, finalizingPlugin.getClass().getCanonicalName(), e
			);
		}
	}
}
