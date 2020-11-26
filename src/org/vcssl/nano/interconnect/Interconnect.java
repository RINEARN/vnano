/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.vcssl.connect.ClassToXnci1Adapter;
import org.vcssl.connect.ConnectorException;
import org.vcssl.connect.ConnectorPermissionName;
import org.vcssl.connect.ConnectorPermissionValue;
import org.vcssl.connect.ExternalFunctionConnectorInterface1;
import org.vcssl.connect.ExternalNamespaceConnectorInterface1;
import org.vcssl.connect.ExternalVariableConnectorInterface1;
import org.vcssl.connect.FieldToXvci1Adapter;
import org.vcssl.connect.MethodToXfci1Adapter;
import org.vcssl.connect.PermissionAuthorizerConnectorInterface1;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.IdentifierSyntax;
import org.vcssl.nano.spec.LanguageSpecContainer;
import org.vcssl.nano.spec.OptionValue;
import org.vcssl.nano.spec.SpecialBindingKey;
import org.vcssl.nano.vm.VirtualMachineObjectCode;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.VnanoFatalException;

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
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/interconnect/Interconnect.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/interconnect/Interconnect.html">All</a> |
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

	/** プラグインのパーミッションを管理する、PACI1形式のセキュリティプラグイン（複数同時登録は不可）です。 */
	private PermissionAuthorizerConnectorInterface1 permissionAuthorizer = null;


	/** 初期化/終了時処理のため、接続されているXNCI形式のプラグインを一括で保持するリストです。 */
	private List<ExternalNamespaceConnectorInterface1> xnci1PluginList = null;

	/** 初期化/終了時処理のため、接続されているXFCI形式のプラグインを一括で保持するリストです。 */
	private List<ExternalFunctionConnectorInterface1> xfci1PluginList = null;

	/** 初期化/終了時処理のため、接続されているXVCI形式のプラグインを一括で保持するリストです。 */
	private List<ExternalVariableConnectorInterface1> xvci1PluginList = null;


	/**
	 * <span class="lang-en">A map to store all names and values of option items</span>
	 * <span class="lang-ja">全てのオプション項目の名称と値を保持するマップです</span>
	 * .
	 */
	private Map<String, Object> optionMap = null;


	/**
	 * <span class="lang-en">A map to store all names and values of permission items</span>
	 * <span class="lang-ja">全てのパーミッション項目の名称と値を保持するマップです</span>
	 * .
	 */
	private Map<String, String> permissionMap = null;


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
		this.engineConnector = new EngineConnector();
		this.externalFunctionTable = new FunctionTable(LANG_SPEC);
		this.externalVariableTable = new VariableTable(LANG_SPEC);
		this.xnci1PluginList = new ArrayList<ExternalNamespaceConnectorInterface1>();
		this.xfci1PluginList = new ArrayList<ExternalFunctionConnectorInterface1>();
		this.xvci1PluginList = new ArrayList<ExternalVariableConnectorInterface1>();

		// Create an option map and set default values, and reflect to the engine connector.
		// オプションマップを生成し, 必須項目をデフォルト値で補完した上で、エンジンコネクタに反映
		this.optionMap = new LinkedHashMap<String, Object>();
		this.optionMap = OptionValue.normalizeValuesOf(optionMap, langSpec);
		this.engineConnector = this.engineConnector.createOptionMapUpdatedInstance(this.optionMap);

		// Create an empty permission map and set "DENY" to the default value, and reflect to the engine connector.
		// パーミッションマップを生成し, デフォルト値を「DENY」に設定した上で、エンジンコネクタに反映
		this.permissionMap = new LinkedHashMap<String, String>();
		this.permissionMap.put(ConnectorPermissionName.DEFAULT, ConnectorPermissionValue.DENY);
		try {
			this.engineConnector = this.engineConnector.createPermissionMapUpdatedInstance(this.permissionMap);
		} catch (VnanoException vne) {
			// この時点ではパーミッション認可プラグイン(permission authorizer)が未ロードなため、
			// 上記はパーミッションマップの参照を控えるのみで、例外が発生するはずはないので、発生した場合は実装の異常
			throw new VnanoFatalException("Unexpected exception occurred", vne);
		}
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
	public void setOptionMap(Map<String, Object> optionMap) throws VnanoException {

		// Supplement some option items by default values, and store the map to the field of this class.
		// 必須項目をデフォルト値で補完した上で、このクラスのフィールドに設定
		this.optionMap = OptionValue.normalizeValuesOf(optionMap, LANG_SPEC);

		// Check the content of option settings.
		// オプション設定の内容を検査
		OptionValue.checkContentsOf(this.optionMap);

		// Reflect to the engine connector, because option values may be referred from plug-ins.
		// オプションはプラグインからも参照されるので, エンジンコネクタにも反映させる
		this.engineConnector = this.engineConnector.createOptionMapUpdatedInstance(this.optionMap);
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
	public Map<String, Object> getOptionMap() {
		return this.optionMap;
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
		this.permissionMap = permissionMap;

		// Reflect to the engine connector, because permission values may be referred from plug-ins.
		// パーミッションはプラグインからも参照されるので, エンジンコネクタにも反映させる
		this.engineConnector = this.engineConnector.createPermissionMapUpdatedInstance(permissionMap);
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
	public Map<String, String> getPermissionMap() {
		return this.permissionMap;
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
	 *   Also, this method is used for connecting
	 *   {@link org.vcssl.connect.PermissionAuthorizerConnectorInterface1 PACI1}
	 *   type plug-ins which is used for managing permissions (permission authorizer).
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
	 *   形式のプラグイン（パーミッション許可プラグイン, permission authorizer）の接続にも、このメソッドを用います.
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

		try {
			// Replace the binding key with auto-generated one if, it it is requested.
			// キーを自動生成するよう設定されている場合は、キーを置き換え
			if (bindingName.equals(SpecialBindingKey.AUTO_KEY)) {
				bindingName = this.generateBindingNameOf(plugin);
				// これ、ここでシグネチャ求める前にプラグインを init する必要がある？ (XNCI1など)
				// -> init するまで名前やシグネチャが定まらない実装は XFCI1 的にはNGでいいのでは。init タイミングを早めてもいいけど、それはそれで制約が緩すぎて後でエンジン側でネックになる仕様になりそう(GPCIみたいな)。
			}

			// bindingName 内の空白や「 ( 」以降は無視する仕様なので、含まれていればカットする
			bindingName = bindingName.split("\\s|\\(")[0];

			// PACI 1 形式のセキュリティプラグイン
			if (plugin instanceof PermissionAuthorizerConnectorInterface1) {
				this.connectPaci1Plugin( (PermissionAuthorizerConnectorInterface1)plugin ); //このプラグインは役割的にバインディング情報は不要

			// XVCI 1 形式の外部変数プラグイン
			} else if (plugin instanceof ExternalVariableConnectorInterface1) {
				this.connectXvci1Plugin( (ExternalVariableConnectorInterface1)plugin, true, bindingName, false, null );

			// XFCI 1 形式の外部関数プラグイン
			} else if (plugin instanceof ExternalFunctionConnectorInterface1) {
				this.connectXfci1Plugin( (ExternalFunctionConnectorInterface1)plugin, true, bindingName, false, null);

			// XNCI 1 形式の外部関数プラグイン
			} else if (plugin instanceof ExternalNamespaceConnectorInterface1) {
				this.connectXnci1Plugin( (ExternalNamespaceConnectorInterface1)plugin, true, bindingName, false );

			// クラスフィールドの場合
			} else if (plugin instanceof Field) {
				this.connectFieldAsPlugin( (Field)plugin, null, true, bindingName );

			// クラスメソッドの場合
			} else if (plugin instanceof Method) {
				this.connectMethodAsPlugin( (Method)plugin, null, true, bindingName );

			// クラスの場合
			} else if (plugin instanceof Class) {
				this.connectClassAsPlugin( (Class<?>)plugin, null, true, bindingName );

			// インスタンスフィールドやインスタンスメソッド等は、所属インスタンスも格納する配列で渡される
			} else if (plugin instanceof Object[]) {

				Object[] objects = (Object[])plugin;

				// インスタンスフィールドの場合 >> 引数からFieldとインスタンスを取り出し、外部変数として接続
				if (objects.length == 2 && objects[0] instanceof Field) {
					Field field = (Field)objects[0]; // [0] はフィールドのリフレクション
					Object instance = objects[1];    // [1] はフィールドの所属インスタンス
					this.connectFieldAsPlugin( field, instance, true, bindingName );

				// インスタンスメソッドの場合 >> 引数からMethodとインスタンスを取り出し、外部関数として接続
				} else if (objects.length == 2 && objects[0] instanceof Method) {
					Method method = (Method)objects[0]; // [0] はメソッドのリフレクション
					Object instance = objects[1];       // [1] はメソッドの所属インスタンス
					this.connectMethodAsPlugin( method, instance, true, bindingName );

				// クラスの場合 >> 引数からClassとインスタンスを取り出し、外部ライブラリとして接続
				} else if (objects.length == 2 && objects[0] instanceof Class) {
					Class<?> pluginClass = (Class<?>)objects[0];
					Object instance = objects[1];
					this.connectClassAsPlugin( pluginClass, instance, true, bindingName );

				} else {
					throw new VnanoException(
						ErrorType.UNSUPPORTED_PLUGIN, new String[] {objects[0].getClass().getCanonicalName()}
					);
				}

			// その他のオブジェクトは、Classを取得して外部ライブラリとして接続
			} else {
				Class<?> pluginClass = plugin.getClass();
				this.connectClassAsPlugin( pluginClass, plugin, true, bindingName );
			}

		// 内部で VnanoException が発生した場合は、原因プラグインを特定できるメッセージを持たせた VnanoException でラップして投げる
		} catch (VnanoException vne) {
			throw new VnanoException(ErrorType.PLUGIN_CONNECTION_FAILED, bindingName, vne);
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
		this.permissionAuthorizer = null;
	}


	/**
	 * <span class="lang-en">
	 * Generate the value of the argument "bindingName" of
	 * {@link Interconnect#connectPlugin connectPlugin(String bindingKey, Object plugin)}
	 * method automatically
	 * </span>
	 * <span class="lang-ja">
	 * {@link Interconnect#connectPlugin connectPlugin(String bindingKey, Object plugin)}
	 * メソッドの引数 bindingName の値を自動生成します
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
	private String generateBindingNameOf(Object plugin) throws VnanoException {

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
				return generateBindingNameOf(field);

			// インスタンスメソッドの場合
			} else if (objects.length == 2 && objects[0] instanceof Method) {
				Method method = (Method)objects[0]; // [0] はメソッドのリフレクション
				return generateBindingNameOf(method);

			// クラスの場合 >> 引数からClassとインスタンスを取り出し、外部ライブラリとして接続
			} else if (objects.length == 2 && objects[0] instanceof Class) {
				Class<?> pluginClass = (Class<?>)objects[0];
				return generateBindingNameOf(pluginClass);
			} else {
				throw new VnanoException(
					ErrorType.UNSUPPORTED_PLUGIN, new String[] {objects[0].getClass().getCanonicalName()}
				);
			}

		// その他のオブジェクトは、Classを取得して外部ライブラリとして接続
		} else {
			Class<?> pluginClass = plugin.getClass();
			return generateBindingNameOf(pluginClass);
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
		this.connectXvci1Plugin(adapter, aliasingRequired, aliasName, false, null);
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
		this.connectXfci1Plugin(adapter, aliasingRequired, aliasSignature, false, null);
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
	 * パーミッションの認可機能を提供する, {@link org.vcssl.connect.PermissionAuthorizerConnectorInterface1 PACI1}
	 * 形式のプラグインを接続します
	 * </span>
	 * <span class="lang-en">
	 * Connects the plug-in of {@link org.vcssl.connect.PermissionAuthorizerConnectorInterface1 PACI1} format,
	 * for authorization of permissions
	 * </span>
	 * .
	 * @param plugin
	 *   <span class="lang-en">The plug-in to be connected.</span>
	 *   <span class="lang-ja">接続するプラグイン.</span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">
	 *   Thrown if the plug-in could not be connected, caused by initialization errors and so on.
	 *   </span>
	 *   <span class="lang-ja">
	 *   初期化処理の異常などにより, プラグインの接続に失敗した場合にスローされます.
	 *   </span>
	 */
	private void connectPaci1Plugin(PermissionAuthorizerConnectorInterface1 plugin) throws VnanoException {

		// パーミッション許可プラグインは1個しか同時接続できないため、既に接続されている場合はエラー
		if (this.permissionAuthorizer != null) {
			throw new VnanoException(
				ErrorType.MULTIPLE_PERMISSION_AUTHORIZERS_ARE_CONNECTED,
				new String[]{ plugin.getClass().getCanonicalName(), this.permissionAuthorizer.getClass().getCanonicalName() }
			);
		}

		// 接続時の初期化処理に成功すれば接続する
		try {
			plugin.initializeForConnection(this.engineConnector);
		} catch (ConnectorException e) {
			throw new VnanoException(
				ErrorType.PLUGIN_INITIALIZATION_FAILED, plugin.getClass().getCanonicalName(), e
			);
		}
		this.permissionAuthorizer = (PermissionAuthorizerConnectorInterface1) plugin;

		// Reflect to the engine connector, because permissions may be requested from other plug-ins.
		// パーミッションは他のプラグインからも要求されるので, エンジンコネクタにも反映させる
		// (パーミッション認可プラグインの参照を差し替えたインスタンスを生成)
		this.engineConnector = this.engineConnector.createPermissionAuthorizerUpdatedInstance(
			this.permissionAuthorizer
		);
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
	 * @param belongsToNamespace
	 *   <span class="lang-en">Whether the variable provided by the plug-in belongs to any namespaces.</span>
	 *   <span class="lang-ja">プラグインが提供する変数が属する名前空間が, 名前空間に属するかどうか（する場合に true）.</span>
	 *
	 * @param namespaceName
	 *   <span class="lang-en">The name of the namespace to which the variable provided by the plug-in belongs.</span>
	 *   <span class="lang-ja">プラグインが提供する変数が属する名前空間の名称.</span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">
	 *   Thrown if the plug-in could not be connected, caused by incompatibility of data types, and so on.
	 *   </span>
	 *   <span class="lang-ja">
	 *   データ型の互換性などの原因により, プラグインの接続に失敗した場合にスローされます.
	 *   </span>
	 */
	private void connectXvci1Plugin(ExternalVariableConnectorInterface1 plugin,
			boolean aliasingRequired, String aliasName, boolean belongsToNamespace, String namespaceName) throws VnanoException {

		// 接続時の初期化処理を実行
		try {
			plugin.initializeForConnection(this.engineConnector);
		} catch (ConnectorException e) {
			throw new VnanoException(
				ErrorType.PLUGIN_INITIALIZATION_FAILED, plugin.getClass().getCanonicalName(), e
			);
		}

		// その他の初期化/終了時処理などの時に呼ぶため、XVCI1形式のままのインスタンスもフィールドのリストに登録
		this.xvci1PluginList.add(plugin);

		// コンパイラやVMでは各種変数は AbstractVariable に抽象化した形で扱うので、
		// XVCI1 から AbstractVariable へ変換するアダプタ（AbstractVariableを継承している）を生成
		AbstractVariable adapter = new Xvci1ToVariableAdapter(plugin, LANG_SPEC);

		// 所属名前空間やエイリアス（別名）などが必要なら設定
		if (belongsToNamespace) {
			adapter.setNamespaceName(namespaceName);
		}
		if (aliasingRequired) {
			adapter.setVariableName(aliasName);
		}

		// 設定を終えた AbstractVariable 継承アダプタを接続
		this.connectVariable(adapter);
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
	 * @param belongsToNamespace
	 *   <span class="lang-en">Whether the function provided by the plug-in belongs to any namespaces.</span>
	 *   <span class="lang-ja">プラグインが提供する関数が属する名前空間が, 名前空間に属するかどうか（する場合に true）.</span>
	 *
	 * @param namespaceName
	 *   <span class="lang-en">The name of the namespace to which the function provided by the plug-in belongs.</span>
	 *   <span class="lang-ja">プラグインが提供する関数が属する名前空間.</span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">
	 *   Thrown if the plug-in could not be connected, caused by incompatibility of data types, and so on.
	 *   </span>
	 *   <span class="lang-ja">
	 *   データ型の互換性などの原因により, プラグインの接続に失敗した場合にスローされます.
	 *   </span>
	 */
	private void connectXfci1Plugin(ExternalFunctionConnectorInterface1 plugin,
			boolean aliasingRequired, String aliasName, boolean belongsToNamespace, String namespaceName) throws VnanoException {

		// 接続時の初期化処理を実行
		try {
			plugin.initializeForConnection(this.engineConnector);
		} catch (ConnectorException e) {
			throw new VnanoException(
				ErrorType.PLUGIN_INITIALIZATION_FAILED, plugin.getClass().getCanonicalName(), e
			);
		}

		// その他の初期化/終了時処理などの時に呼ぶため、XFCI1形式のままのインスタンスもフィールドのリストに登録
		this.xfci1PluginList.add(plugin);

		// コンパイラやVMでは各種関数は AbstractFunction に抽象化した形で扱うので、
		// XFCI1 から AbstractFunction へ変換するアダプタ（AbstractFunctionを継承している）を生成
		AbstractFunction adapter = new Xfci1ToFunctionAdapter(plugin, LANG_SPEC);

		// 所属名前空間やエイリアス（別名）などが必要なら設定
		if (belongsToNamespace) {
			adapter.setNamespaceName(namespaceName);
		}
		if (aliasingRequired) {
			adapter.setFunctionName(aliasName);
		}

		// 設定を終えた AbstractFunction 継承アダプタを接続
		this.connectFunction(adapter);
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
	private void connectXnci1Plugin(
			ExternalNamespaceConnectorInterface1 plugin, boolean aliasingRequired, String aliasName,
				boolean ignoreIncompatibles) throws VnanoException {

		// 本体の pre connect 初期化処理
		try {
			plugin.preInitializeForConnection(this.engineConnector);
		} catch (ConnectorException e) {
			throw new VnanoException(
				ErrorType.PLUGIN_INITIALIZATION_FAILED, plugin.getClass().getCanonicalName(), e
			);
		}

		// 名前空間の名称を取得、ただしエイリアスが指定されている場合はそちらを用いる
		String nameapaceName = plugin.getNamespaceName();
		if (aliasingRequired) {
			nameapaceName = aliasName;
		}

		// 所属関数プラグインを接続（この中で関数の connect 初期化処理が行われる）
		ExternalFunctionConnectorInterface1[] xfciPlugins = plugin.getFunctions();
		for (ExternalFunctionConnectorInterface1 xfciPlugin: xfciPlugins) {
			try {
				this.connectXfci1Plugin(xfciPlugin, false, null, true, nameapaceName);
			} catch (VnanoException e) {
				if (!ignoreIncompatibles) {
					throw e;
				}
			}
		}

		// 所属変数プラグインを接続（この中で変数の connect 初期化処理が行われる）
		ExternalVariableConnectorInterface1[] xvciPlugins = plugin.getVariables();
		for (ExternalVariableConnectorInterface1 xvciPlugin: xvciPlugins) {
			try {
				this.connectXvci1Plugin(xvciPlugin, false, null, true, nameapaceName);
			} catch (VnanoException e) {
				if (!ignoreIncompatibles) {
					throw e;
				}
			}
		}

		// 本体の post connect 初期化処理
		try {
			plugin.postInitializeForConnection(this.engineConnector);
		} catch (ConnectorException e) {
			throw new VnanoException(
				ErrorType.PLUGIN_INITIALIZATION_FAILED, plugin.getClass().getCanonicalName(), e
			);
		}

		// 先のほうの初期化だけでなく、後のほうの初期化も正常に完了した段階でリストに登録する
		this.xnci1PluginList.add(plugin);
	}


	/**
	 * Vnano処理系内部の変数形式に準拠した変数オブジェクトを接続します。
	 * @param variable 変数オブジェクト
	 */
	private void connectVariable(AbstractVariable variable) {
		this.externalVariableTable.addVariable(variable);
	}


	/**
	 * Vnano処理系内部の関数形式に準拠した関数オブジェクトを接続します。
	 *
	 * @param function 関数オブジェクト
	 */
	private void connectFunction(AbstractFunction function) {
		this.externalFunctionTable.addFunction(function);
	}


	private void finalizeAllPluginsForDisconnection() throws VnanoException {
		Object finalizingPlugin = null; // 例外発生時のため、終了時処理中のプラグインを控えておく
		try {
			// 関数/変数系は、モジュールの preFinalize -> 関数/変数の init -> モジュールの postFinalize の順で破棄
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

			// セキュリティ系プラグインは最後に破棄
			//（EngineConnector経由で、他プラグインの接続解除処理内で呼ばれる可能性があるため）
			if (this.permissionAuthorizer != null) {
				this.permissionAuthorizer.finalizeForDisconnection(this.engineConnector);
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
			// セキュリティ系プラグインは最初に初期化
			//（EngineConnector経由で、他プラグインの初期化処理内で呼ばれる可能性があるため）
			if (this.permissionAuthorizer != null) {
				this.permissionAuthorizer.initializeForExecution(this.engineConnector);
			}

			// 関数/変数系は、モジュールの preInit -> 関数/変数の init -> モジュールの postInit の順で初期化
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
			// 関数/変数系は、モジュールの preFinalize -> 関数/変数の init -> モジュールの postFinalize の順で破棄
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

			// セキュリティ系プラグインは最後に破棄
			//（EngineConnector経由で、他プラグインの終了時処理内で呼ばれる可能性があるため）
			if (this.permissionAuthorizer != null) {
				this.permissionAuthorizer.finalizeForTermination(this.engineConnector);
			}

		} catch (ConnectorException e) {
			throw new VnanoException(
				ErrorType.PLUGIN_FINALIZATION_FAILED, finalizingPlugin.getClass().getCanonicalName(), e
			);
		}
	}
}
