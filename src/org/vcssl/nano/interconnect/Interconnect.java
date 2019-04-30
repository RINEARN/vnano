/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map.Entry;

import javax.script.Bindings;

import org.vcssl.connect.ClassToXlci1Adapter;
import org.vcssl.connect.ExternalFunctionConnector1;
import org.vcssl.connect.ExternalLibraryConnector1;
import org.vcssl.connect.ExternalVariableConnector1;
import org.vcssl.connect.FieldToXvci1Adapter;
import org.vcssl.connect.MethodToXfci1Adapter;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.VirtualMachineObjectCode;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.VnanoException;


/**
 * <p>
 * 処理系内の各部の間や、処理系内側と外側との間で、
 * 関数や変数の紐づけやアクセスなどを仲介する、
 * 接続レイヤーの諸機能を提供するクラスです。
 * </p>
 *
 * <p>
 * グローバルスコープの変数テーブルや関数テーブルは、
 * このクラスが内部で保持しています。
 * 関数の呼び出しや、
 * 外部変数とのデータの同期（処理終了後の書き戻しなど）も、
 * このクラスを介して行われます。
 * <br />
 * 外部変数・外部関数プラグインの接続インターフェースをサポートし、
 * （必要によりアダプタなどを介して）
 * 処理系内で最終的にプラグインが接続されるハブとなるのも、このクラスです。
 * <br />
 * VCSSL/Vnano処理系では、このような役割を提供するオブジェクトをインターコネクトと呼びます。
 * このクラスは、Vnano用のシンプルなインターコネクト機能を提供します。
 * </p>
 *
 * <p>
 * このインターコネクトでは、以下の外部インターフェースがサポートされています:
 * </p>
 *
 * <ul>
 *   <li>XVCI 1 ({@link org.vcssl.connect.ExternalVariableConnector1 org.vcssl.connector.ExternalVariableConnector1})</li>
 *   <li>XFCI 1 ({@link org.vcssl.connect.ExternalFunctionConnector1 org.vcssl.connector.ExternalFunctionConnector1})</li>
 *   <li>XLCI 1 ({@link org.vcssl.connect.ExternalLibraryConnector1 org.vcssl.connector.ExternalLibraryConnector1})</li>
 *   <li>java.lang.reflect.Field (内部で {@link org.vcssl.connect.FieldToXvci1Adapter FieldToXvci1Adapter} を介し、XVCI 1 で接続されます。)</li>
 *   <li>java.lang.reflect.Method (内部で {@link org.vcssl.connect.MethodToXfci1Adapter MethodToXfci1Adapter} を介し、XFCI 1 で接続されます。)</li>
 * </ul>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class Interconnect {


	/** 外部関数の情報を保持する関数テーブルです。 */
	private FunctionTable functionTable = null;

	/** 外部変数の情報を保持するグローバル変数テーブルです。 */
	private VariableTable globalVariableTable = null;

	/** コンストラクタに渡された、外部変数・外部関数の情報を格納する Bindings オブジェクトを保持します。 */
	private Bindings bindings = null;

	/** 実行結果（式の評価結果やスクリプトの戻り値）を保持するデータコンテナです。 */
	DataContainer<?> returnedDataContainer;


	/**
	 * 何も接続されていないインターコネクトのインスタンスを生成します。
	 */
	public Interconnect() {
		this.functionTable = new FunctionTable();
		this.globalVariableTable = new VariableTable();
	}


	/**
	 * 指定された Bindings オブジェクト内の要素を、
	 * Vnano処理系における変数・関数に変換して接続したインターコネクトを生成します。
	 *
	 * @throws DataException データ型の互換性により、接続に失敗した要素があった場合にスローされます。
	 * @throws IllegalArgumentException サポート対象の関数・変数の形式に準拠していない要素があった場合にスローされます。
	 */
	public Interconnect(Bindings bindings) throws VnanoException {
		this.functionTable = new FunctionTable();
		this.globalVariableTable = new VariableTable();
		this.bind(bindings);
	}


	/**
	 * 外部関数の情報を保持する関数テーブルを返します。
	 *
	 * @return 関数テーブル
	 */
	public FunctionTable getGlobalFunctionTable() {
		return this.functionTable;
	}


	/**
	 * 外部変数の情報を保持するグローバル変数テーブルを返します。
	 *
	 * @return 変数テーブル
	 */
	public VariableTable getGlobalVariableTable() {
		return this.globalVariableTable;
	}


	/**
	 * 外部変数・外部関数の情報を保持するバインディング内の全ての要素を、
	 * 必要に応じて適切な形式に変換した上で接続します。
	 * なお、一つのインターコネクトに対して、バインディングは一つしか接続できず、
	 * 接続済みのバインディングを差し替える事もできません。
	 * そのため、このメソッドはコンストラクタからのみ呼ばれます。
	 *
	 * このメソッドは、バインディング内の全ペアに対して、
	 * {@link Interconnect#connectElementInBindings(String,Object) connectElementInBindings(String,Object)}
	 * メソッドを呼び出して接続します。
	 * バインディング内の各要素が、どう解釈・変換されて接続されるかは、
	 * 上記のメソッドの説明を参照してください。
	 *
	 * @param bindings 外部変数・外部関数の情報を保持するバインディング
	 * @throws DataException データ型の互換性により、接続に失敗した要素があった場合にスローされます。
	 * @throws IllegalArgumentException サポート対象の関数・変数の形式に準拠していない要素があった場合にスローされます。
	 */
	private void bind(Bindings bindings) throws VnanoException {
		if (this.bindings != null) {
			throw new VnanoFatalException("Bindings can be set ONLY ONCE for an instance of the Interconnect.");
		}
		this.bindings = bindings;

		// Bindings から1個ずつ全ての要素を取り出して接続
		// 注: 要素を取り出す順序については、登録順と一致する事は保証されていない模様（実際にしばしば異なる）
		// -> SimpleBindingsを使う場合は、コンストラクタで LinkedHashMap を指定する等して対応可能、
		//    しかしBindingsはインターフェースなので、実際に外側からどのような実装が渡されるかは未知
		//    -> また後の段階で要検討
		for (Entry<String,Object> pair: bindings.entrySet()) {
			this.bind(pair.getKey(), pair.getValue());
		}
	}


	/**
	 * Bindingsに含まれるキーとオブジェクトの組が、
	 * Vnano処理系に外部変数・外部関数として接続可能であれば、
	 * 必要に応じて適切な形式に変換した上で接続します。
	 *
	 * 引数 object がプリミティブ型のラッパーであった場合は、
	 * Vnano処理系内部の変数形式に基づいた変数を新規生成（宣言）し、
	 * 引数 bindName をスクリプト内での変数名に設定して接続します。
	 *
	 * 引数 object が関数・変数プラグイン（
	 * {@link org.vcssl.connect.ExternalVariableConnector1 XVCI 1} や
	 * {@link org.vcssl.connect.ExternalFunctionConnector1 XFCI 2} の実装クラス、
	 * または
	 * {@link org.vcssl.nano.interconnect.AbstractVariable AbstractVariable} や
	 * {@link org.vcssl.nano.interconnect.AbstractVariable AbstractFunction} の継承クラス
	 * ）であった場合は、そのプラグインが保持している名称の変数・関数として接続します
	 * （この場合、引数 bindName は単にプラグインを一意に識別するためのキー名と見なされ、
	 * スクリプト内における変数・関数名とは無関係となります）。
	 *
	 * 引数 object が java.lang.reflect.Field または java.lang.reflect.Method
	 * である場合は、そのフィールド・メソッドを変数・関数プラグインに変換して接続します
	 * （この場合も、引数 bindName はプラグイン識別用のキー名となり、スクリプト内での変数・関数名とは無関係です）。
	 * ただし、static ではないフィールド・メソッドを接続したい場合には、
	 * そのフィールド・メソッドが定義されたクラスのインスタンスが必要であるため、
	 * 引数 object は Object 配列型とし、その [0] 番要素に Field または Method、
	 * [1] 番要素に定義クラスのインスタンスを格納して渡してください。
	 *
	 * @param bindName 変数名またはプラグイン識別用キー名
	 * @param object 外部変数・外部関数として接続したいオブジェクト
	 * @throws DataException データ型の互換性により、接続に失敗した場合にスローされます。
	 * @throws VnanoException サポートしていない形式のプラグインが渡された場合や、接続時にエラーが生じた場合にスローされます。
	 */
	private void bind(String bindName, Object object) throws VnanoException {

		// 内部変数と互換の変数オブジェクト
		if (object instanceof AbstractVariable) {
			this.connect( (AbstractVariable)object, true, bindName );

		// 内部関数と互換の変数オブジェクト
		} else if (object instanceof AbstractFunction) {
			this.connect( (AbstractFunction)object, true, bindName );

		// XVCI 1 形式の外部変数プラグイン
		} else if (object instanceof ExternalVariableConnector1) {
			this.connect( (ExternalVariableConnector1)object, true, bindName );

		// XFCI 1 形式の外部関数プラグイン
		} else if (object instanceof ExternalFunctionConnector1) {
			this.connect( (ExternalFunctionConnector1)object, true, bindName );

		// XLCI 1 形式の外部関数プラグイン
		} else if (object instanceof ExternalLibraryConnector1) {
			this.connect( (ExternalLibraryConnector1)object, true, bindName );

		// クラスフィールドの場合
		} else if (object instanceof Field) {
			this.connect( (Field)object, null, true, bindName );

		// クラスメソッドの場合
		} else if (object instanceof Method) {
			this.connect( (Method)object, null, true, bindName );

		// クラスの場合
		} else if (object instanceof Class) {
			this.connect( (Class<?>)object, null, true, bindName );

		// インスタンスフィールドやインスタンスメソッド等は、所属インスタンスも格納する配列で渡される
		} else if (object instanceof Object[]) {

			Object[] objects = (Object[])object;

			// インスタンスフィールドの場合 >> 引数からFieldとインスタンスを取り出し、外部変数として接続
			if (objects.length == 2 && objects[0] instanceof Field) {
				Field field = (Field)objects[0]; // [0] はフィールドのリフレクション
				Object instance = objects[1];    // [1] はフィールドの所属インスタンス
				this.connect( field, instance, true, bindName );

			// インスタンスフィールドの場合 >> 引数からMethodとインスタンスを取り出し、外部関数として接続
			} else if (objects.length == 2 && objects[0] instanceof Method) {
				Method method = (Method)objects[0]; // [0] はメソッドのリフレクション
				Object instance = objects[1];       // [1] はメソッドの所属インスタンス
				this.connect( method, instance, true, bindName );

			// クラスの場合 >> 引数からClassとインスタンスを取り出し、外部ライブラリとして接続
			} else if (objects.length == 2 && objects[0] instanceof Class) {
				Class<?> pluginClass = (Class<?>)objects[0];
				Object instance = objects[1];
				this.connect( pluginClass, instance, true, bindName );
			}

		// その他のオブジェクトは、Classを取得して外部ライブラリとして接続
		} else {
			Class<?> pluginClass = object.getClass();
			this.connect( pluginClass, object, true, bindName );

		/*
		} else {
			throw new VnanoException(
				ErrorType.UNSUPPORTED_PLUGIN, new String[] {object.getClass().getCanonicalName()}
			);
		*/
		}
	}


	/**
	 * ホスト言語側のコード内で宣言されているフィールドを、
	 * スクリプト内からアクセスできる外部変数として接続します。
	 * （ここに同期タイミングの注意）
	 *
	 * @param field 外部変数として接続するフィールド
	 * @param instance フィールドの属するクラスのインスタンス
	 * @param aliasingRequired スクリプト内から別名でアクセスするかどうか（する場合にtrue）
	 * @param aliasName スクリプト内での別名（aliasingRequiredがtrueの場合のみ参照されます）
	 * @throws DataException 引数や戻り値のデータ型が非対応であった場合にスローされます。
	 */
	public void connect(Field field, Object instance, boolean aliasingRequired, String aliasName)
			throws VnanoException {

		FieldToXvci1Adapter adapter = new FieldToXvci1Adapter(field, instance);
		this.connect(adapter, aliasingRequired, aliasName);
	}


	/**
	 * ホスト言語側のコード内で宣言されているメソッドを、
	 * スクリプト内からアクセスできる外部関数として接続します。
	 *
	 * @param method 外部関数として接続するメソッド
	 * @param instance メソッドの属するクラスのインスタンス
	 * @param aliasingRequired スクリプト内から別名でアクセスするかどうか（する場合にtrue）
	 * @param aliasSignature 別名アクセスのためのコールシグネチャ（aliasingRequiredがtrueの場合のみ参照されます）
	 * @throws DataException データ型が非対応であった場合にスローされます。
	 */
	public void connect(Method method, Object instance, boolean aliasingRequired, String aliasSignature)
			throws VnanoException {

		MethodToXfci1Adapter adapter = new MethodToXfci1Adapter(method,instance);
		this.connect(adapter, aliasingRequired, aliasSignature);
	}


	/**
	 * ホスト言語側のコード内で宣言されているクラスを、
	 * スクリプト内からアクセスできる外部関数/変数を提供するライブラリとして接続します。
	 *
	 * @param pluginClass 外部ライブラリとして接続するクラス
	 * @param instance クラスのインスタンス
	 * @param aliasingRequired スクリプト内から別名でアクセスするかどうか（する場合にtrue）
	 * @param aliasName 別名アクセスのためのライブラリ名（aliasingRequiredがtrueの場合のみ参照されます）
	 * @throws DataException データ型などが非対応であった場合にスローされます。
	 */
	public void connect(Class<?> pluginClass, Object instance, boolean aliasingRequired, String aliasName)
			throws VnanoException {

		ClassToXlci1Adapter adapter = new ClassToXlci1Adapter(pluginClass,instance);
		this.connect(adapter, aliasingRequired, aliasName);
	}


	/**
	 * {@link org.vcssl.connect.ExternalVariableConnector1 XVCI 1}
	 * のプラグイン・インターフェースを用いて、
	 * ホスト言語で実装された外部変数オブジェクトを接続します。
	 *
	 * @param connector XVCI準拠の外部変数
	 * @param aliasingRequired スクリプト内から別名でアクセスするかどうか（する場合にtrue）
	 * @param aliasName スクリプト内での別名（aliasingRequiredがtrueの場合のみ参照されます）
	 * @throws VnanoException データ型が非対応であった場合にスローされます。
	 */
	public void connect(ExternalVariableConnector1 connector, boolean aliasingRequired, String aliasName)
			throws VnanoException {

		connector.initializeForConnection();
		Xvci1ToVariableAdapter adapter = new Xvci1ToVariableAdapter(connector);
		this.connect(adapter, aliasingRequired, aliasName);
	}


	/**
	 * {@link org.vcssl.connect.ExternalFunctionConnector1 XFCI 1}
	 * のプラグイン・インターフェースを用いて、
	 * ホスト言語で実装された外部関数オブジェクトを接続します。
	 *
	 * @param connector XFCI準拠の外部関数
	 * @param aliasingRequired スクリプト内から別名でアクセスするかどうか（する場合にtrue）
	 * @param aliasSignature 別名アクセスのためのコールシグネチャ（aliasingRequiredがtrueの場合のみ参照されます）
	 * @throws VnanoException 引数や戻り値のデータ型が非対応であった場合にスローされます。
	 */
	public void connect(ExternalFunctionConnector1 connector, boolean aliasingRequired, String aliasSignature)
			throws VnanoException {

		connector.initializeForConnection();
		Xfci1ToFunctionAdapter adapter = new Xfci1ToFunctionAdapter(connector);
		this.connect(adapter, aliasingRequired, aliasSignature);
	}


	/**
	 * {@link org.vcssl.connect.ExternalLibraryConnector1 XLCI 1}
	 * のプラグイン・インターフェースを用いて、
	 * ホスト言語で実装された外部ライブラリオブジェクトを接続します。
	 *
	 * @param connector XLCI準拠の外部ライブラリ
	 * @param aliasingRequired スクリプト内から別名でアクセスするかどうか（する場合にtrue）
	 * @param aliasName 別名アクセスのためのライブラリ名（aliasingRequiredがtrueの場合のみ参照されます）
	 * @throws VnanoException データ型などが非対応であった場合にスローされます。
	 */
	public void connect(ExternalLibraryConnector1 connector, boolean aliasingRequired, String aliasName)
			throws VnanoException {

		connector.initializeForConnection();

		// デフォルトではライブラリ名を名前空間とし、エイリアスが指定されている場合はそちらを使用する
		String nameSpace = connector.getLibraryName();
		if (aliasingRequired) {
			nameSpace = aliasName;
		}

		// 関数をアダプタで変換して接続
		ExternalFunctionConnector1[] xfciConnectors = connector.getFunctions();
		for (ExternalFunctionConnector1 xfciConnector: xfciConnectors) {
			xfciConnector.initializeForConnection();
			AbstractFunction adapter = new Xfci1ToFunctionAdapter(xfciConnector, nameSpace);
			this.connect(adapter, false, null);
		}

		// 変数をアダプタで変換して接続
		ExternalVariableConnector1[] xvciConnectors = connector.getVariables();
		for (ExternalVariableConnector1 xvciConnector: xvciConnectors) {
			xvciConnector.initializeForConnection();
			AbstractVariable adapter = new Xvci1ToVariableAdapter(xvciConnector, nameSpace);
			this.connect(adapter, false, null);
		}
	}


	/**
	 * Vnano処理系内部の変数形式に準拠した変数オブジェクトを接続します。
	 * @param aliasingRequired スクリプト内から別名でアクセスするかどうか（する場合にtrue）
	 * @param aliasName スクリプト内での別名（aliasingRequiredがtrueの場合のみ参照されます）
	 *
	 * @param variable 変数オブジェクト
	 */
	public void connect(AbstractVariable variable, boolean aliasingRequired, String aliasName) {
		if (aliasingRequired) {
			variable = new VariableAliasAdapter(variable);
			((VariableAliasAdapter)variable).setVariableName(aliasName);
		}
		this.globalVariableTable.addVariable(variable);
	}


	/**
	 * Vnano処理系内部の関数形式に準拠した関数オブジェクトを接続します。
	 *
	 * @param function 関数オブジェクト
	 * @param aliasingRequired スクリプト内から別名でアクセスするかどうか（する場合にtrue）
	 * @param aliasSignature 別名アクセスのためのコールシグネチャ（aliasingRequiredがtrueの場合のみ参照されます）
	 * @throws VnanoException 引数に指定されたコールシグネチャが正しくない場合にスローされます。
	 */
	public void connect(AbstractFunction function, boolean aliasingRequired, String aliasSignature)
			throws VnanoException {

		if (aliasingRequired) {
			function = new FunctionAliasAdapter(function);
			((FunctionAliasAdapter)function).setCallSignature(aliasSignature);
		}
		this.functionTable.addFunction(function);
	}


	/**
	 * 指定されたインデックスの外部関数をコールします。
	 *
	 * @param functionIndex 実行する関数のインデックス
	 * @param arguments 引数を格納するデータユニット配列
	 * @param returnData 戻り値を格納するデータユニット
	 */
	public void call(int functionIndex, DataContainer<?>[] arguments, DataContainer<?> returnData) {
		this.functionTable.getFunctionByIndex(functionIndex).invoke(arguments, returnData);
	}


	/**
	 * スクリプトの実行によって変化したデータを、外部変数に書き戻します。
	 *
	 * @param memory スクリプトの実行に使用した仮想メモリー
	 * @throws MemoryAccessException
	 *  	外部変数に対応するデータユニットが、仮想メモリー内に保持されていない場合にスローされます。
	 *  	外部変数からバインディングへ値を書き戻す際に、データの変換に失敗（型の非互換など）した場合にスローされます。
	 */
	public void writeback(Memory memory, VirtualMachineObjectCode intermediateCode) throws VnanoException {

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
			AbstractVariable variable = this.globalVariableTable.getVariableByAssemblyIdentifier(identifier);

			// 書き換え不可能な定数の場合はスキップ
			if (variable.isConstant()) {
				continue;
			}

			// 外部変数オブジェクトにデータコンテナを渡して値を更新させる
			variable.setDataContainer(dataContainer);
		}
	}

}
