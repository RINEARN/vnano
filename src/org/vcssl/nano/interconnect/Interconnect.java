/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.vcssl.connect.ClassToXnci1Adapter;
import org.vcssl.connect.ConnectorException;
import org.vcssl.connect.EngineConnector1;
import org.vcssl.connect.ExternalFunctionConnector1;
import org.vcssl.connect.ExternalNamespaceConnector1;
import org.vcssl.connect.ExternalVariableConnector1;
import org.vcssl.connect.FieldToXvci1Adapter;
import org.vcssl.connect.MethodToXfci1Adapter;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.IdentifierSyntax;
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

	/** 外部関数の情報を保持する関数テーブルです。 */
	private FunctionTable externalFunctionTable = null;

	/** 外部変数の情報を保持するグローバル変数テーブルです。 */
	private VariableTable externalVariableTable = null;

	/** プラグインからスクリプトエンジンにアクセスする際に使用するコネクタです。 */
	private EngineConnector1 engineConnector = null;


	/**
	 * <span class="lang-en">Creates a blank interconnect to which nothing are connected</span>
	 * <span class="lang-ja">何も接続されていない, 空のインターコネクトを生成します</span>
	 * .
	 */
	public Interconnect() {
		this.externalFunctionTable = new FunctionTable();
		this.externalVariableTable = new VariableTable();
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
	public void setEngineConnector(EngineConnector1 engineConnector) {
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
	public void callExternalFunction(int functionIndex, DataContainer<?>[] arguments, DataContainer<?> returnData) {
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
	public void writebackExternalVariables(Memory memory, VirtualMachineObjectCode intermediateCode) {

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
	 *   {@link org.vcssl.connect.ExternalFunctionConnector1 XFCI1} /
	 *   {@link org.vcssl.connect.ExternalVariableConnector1 XVCI1} /
	 *   {@link org.vcssl.connect.ExternalNamespaceConnector1 XNCI1}
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
	 *   {@link org.vcssl.connect.ExternalFunctionConnector1 XFCI1} /
	 *   {@link org.vcssl.connect.ExternalVariableConnector1 XVCI1} /
	 *   {@link org.vcssl.connect.ExternalNamespaceConnector1 XNCI1}
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

		// Replace the binding key with auto-generated one if, it it is requested.
		// キーを自動生成するよう設定されている場合は、キーを置き換え
		if (bindingKey.equals(SpecialBindingKey.AUTO_KEY)) {
			bindingKey = this.generateBindingKeyOf(plugin);
		}

		// XVCI 1 形式の外部変数プラグイン
		if (plugin instanceof ExternalVariableConnector1) {
			this.connectXvci1Plugin( (ExternalVariableConnector1)plugin, true, bindingKey );

		// XFCI 1 形式の外部関数プラグイン
		} else if (plugin instanceof ExternalFunctionConnector1) {
			this.connectXfci1Plugin( (ExternalFunctionConnector1)plugin, true, bindingKey );

		// XNCI 1 形式の外部関数プラグイン
		} else if (plugin instanceof ExternalNamespaceConnector1) {
			this.connectXnci1Plugin( (ExternalNamespaceConnector1)plugin, true, bindingKey, false );

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
			return IdentifierSyntax.getSignatureOf((AbstractFunction)plugin);

		// XVCI 1 形式の外部変数プラグイン
		} else if (plugin instanceof ExternalVariableConnector1) {
			return ((ExternalVariableConnector1)plugin).getVariableName();

		// XFCI 1 形式の外部関数プラグイン
		} else if (plugin instanceof ExternalFunctionConnector1) {
			AbstractFunction functionAdapter = new Xfci1ToFunctionAdapter((ExternalFunctionConnector1)plugin);
			return IdentifierSyntax.getSignatureOf(functionAdapter);

		// XNCI 1 形式の外部関数プラグイン
		} else if (plugin instanceof ExternalNamespaceConnector1) {
			return ((ExternalNamespaceConnector1)plugin).getNamespaceName();

		// クラスフィールドの場合
		} else if (plugin instanceof Field) {
			return ((Field)plugin).getName();

		// クラスメソッドの場合
		} else if (plugin instanceof Method) {
			ExternalFunctionConnector1 xfci1Adapter = new MethodToXfci1Adapter((Method)plugin);
			AbstractFunction functionAdapter = new Xfci1ToFunctionAdapter(xfci1Adapter);
			return IdentifierSyntax.getSignatureOf(functionAdapter);

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
	 * 外部変数を提供する, {@link org.vcssl.connect.ExternalVariableConnector1 XVCI1}
	 * 形式のプラグインを接続します
	 * </span>
	 * <span class="lang-en">
	 * Connects the plug-in of {@link org.vcssl.connect.ExternalVariableConnector1 XVCI1} format,
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
	private void connectXvci1Plugin(ExternalVariableConnector1 plugin, boolean aliasingRequired, String aliasName)
			throws VnanoException {

		try {
			plugin.initializeForConnection(this.engineConnector);
		} catch (ConnectorException e) {
			throw new VnanoException(
				ErrorType.PLUGIN_NITIALIZATION_FAILED, plugin.getClass().getCanonicalName(), e
			);
		}
		Xvci1ToVariableAdapter adapter = new Xvci1ToVariableAdapter(plugin);
		this.connectVariable(adapter, aliasingRequired, aliasName);
	}


	/**
	 * <span class="lang-ja">
	 * 外部関数を提供する, {@link org.vcssl.connect.ExternalFunctionConnector1 XFCI1}
	 * 形式のプラグインを接続します
	 * </span>
	 * <span class="lang-en">
	 * Connects the plug-in of {@link org.vcssl.connect.ExternalFunctionConnector1 XFCI1} format,
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
	private void connectXfci1Plugin(ExternalFunctionConnector1 plugin, boolean aliasingRequired, String aliasSignature)
			throws VnanoException {

		try {
			plugin.initializeForConnection(this.engineConnector);
		} catch (ConnectorException e) {
			throw new VnanoException(
				ErrorType.PLUGIN_NITIALIZATION_FAILED, plugin.getClass().getCanonicalName(), e
			);
		}
		Xfci1ToFunctionAdapter adapter = new Xfci1ToFunctionAdapter(plugin);
		this.connectFunction(adapter, aliasingRequired, aliasSignature);
	}


	/**
	 * <span class="lang-ja">
	 * 複数の外部変数や外部関数をまとめて提供する,
	 * {@link org.vcssl.connect.ExternalNamespaceConnector1 XNCI1} 形式のプラグインを接続します
	 * </span>
	 * <span class="lang-en">
	 * Connects the plug-in of {@link org.vcssl.connect.ExternalNamespaceConnector1 XNCI1} format,
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
	private void connectXnci1Plugin(ExternalNamespaceConnector1 connector, boolean aliasingRequired, String aliasName,
			boolean ignoreIncompatibles) throws VnanoException {

		try {
			connector.initializeForConnection(this.engineConnector);
		} catch (ConnectorException e) {
			throw new VnanoException(
				ErrorType.PLUGIN_NITIALIZATION_FAILED, connector.getClass().getCanonicalName(), e
			);
		}

		// 名前空間（の名前）を取得し、エイリアスが指定されている場合はその名前で置き換える
		String nameSpace = connector.getNamespaceName();
		if (aliasingRequired) {
			nameSpace = aliasName;
		}

		// 関数をアダプタで変換して接続
		ExternalFunctionConnector1[] xfciConnectors = connector.getFunctions();
		for (ExternalFunctionConnector1 xfciConnector: xfciConnectors) {
			try {
				xfciConnector.initializeForConnection(this.engineConnector);
				AbstractFunction adapter = new Xfci1ToFunctionAdapter(xfciConnector, nameSpace);
				this.connectFunction(adapter, false, null);

			} catch (ConnectorException e) {
				throw new VnanoException(
					ErrorType.PLUGIN_NITIALIZATION_FAILED, connector.getClass().getCanonicalName(), e
				);

			} catch (VnanoException e) {
				if (!ignoreIncompatibles) {
					throw e;
				}
			}
		}

		// 変数をアダプタで変換して接続
		ExternalVariableConnector1[] xvciConnectors = connector.getVariables();
		for (ExternalVariableConnector1 xvciConnector: xvciConnectors) {
			try {
				xvciConnector.initializeForConnection(this.engineConnector);
				AbstractVariable adapter = new Xvci1ToVariableAdapter(xvciConnector, nameSpace);
				this.connectVariable(adapter, false, null);

			} catch (ConnectorException e) {
				throw new VnanoException(
					ErrorType.PLUGIN_NITIALIZATION_FAILED, connector.getClass().getCanonicalName(), e
				);

			} catch (VnanoException e) {
				if (!ignoreIncompatibles) {
					throw e;
				}
			}
		}
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
			function = new FunctionAliasAdapter(function);
			((FunctionAliasAdapter)function).setCallSignature(aliasSignature);
		}
		this.externalFunctionTable.addFunction(function);
	}

}
