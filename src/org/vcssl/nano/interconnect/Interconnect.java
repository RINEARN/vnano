/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Bindings;

import org.vcssl.connect.ExternalFunctionConnector1;
import org.vcssl.connect.ExternalVariableConnector1;
import org.vcssl.connect.FieldXvci1Adapter;
import org.vcssl.connect.MethodXfci1Adapter;
import org.vcssl.nano.VnanoIntermediateCode;
import org.vcssl.nano.VnanoRuntimeException;
import org.vcssl.nano.lang.AbstractFunction;
import org.vcssl.nano.lang.AbstractVariable;
import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.lang.FunctionTable;
import org.vcssl.nano.lang.Variable;
import org.vcssl.nano.lang.VariableTable;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.memory.DataConverter;
import org.vcssl.nano.memory.DataException;
import org.vcssl.nano.memory.MemoryAccessException;
import org.vcssl.nano.memory.Memory;
import org.vcssl.nano.spec.IdentifierSyntax;


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
 *   <li>java.lang.reflect.Field (内部で {@link org.vcssl.connect.FieldXvci1Adapter FieldXvci1Adapter} を介し、XVCI 1 で接続されます。)</li>
 *   <li>java.lang.reflect.Method (内部で {@link org.vcssl.connect.MethodXfci1Adapter MethodXfci1Adapter} を介し、XFCI 1 で接続されます。)</li>
 * </ul>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class Interconnect {


	/** 外部関数の情報を保持する関数テーブルです。 */
	private FunctionTable functionTable = null;

	/** 外部変数の情報を保持するグローバル変数テーブルです。 */
	private VariableTable globalVariableTable = null;

	/** 処理系内における変数・関数の一意識別子を、バインディング内の名前に変換するマップです。 */
	private Map<String, String> identifierBindNameMap = null;

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
		this.identifierBindNameMap = new HashMap<String, String>();
	}


	/**
	 * 指定された Bindings オブジェクト内の要素を、
	 * Vnano処理系における変数・関数に変換して接続したインターコネクトを生成します。
	 *
	 * @throws DataException データ型の互換性により、接続に失敗した要素があった場合にスローされます。
	 * @throws IllegalArgumentException サポート対象の関数・変数の形式に準拠していない要素があった場合にスローされます。
	 */
	public Interconnect(Bindings bindings) throws DataException {
		this.functionTable = new FunctionTable();
		this.globalVariableTable = new VariableTable();
		this.identifierBindNameMap = new HashMap<String, String>();
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
	private void bind(Bindings bindings) throws DataException {
		if (this.bindings != null) {
			// 暫定的な簡易例外処理
			System.err.println("バインディング重複接続エラー");
			throw new VnanoRuntimeException();
		}
		this.bindings = bindings;
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
	 * {@link org.vcssl.nano.lang.AbstractVariable AbstractVariable} や
	 * {@link org.vcssl.nano.lang.AbstractVariable AbstractFunction} の継承クラス
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
	 * @throws IllegalArgumentException サポート対象の関数・変数の形式に準拠していないオブジェクトが渡された場合にスローされます。
	 */
	private void bind(String bindName, Object object) throws DataException {

		// 接続オブジェクトに処理系内で割り当てられる一意識別子
		String identifier = null;

		// 内部変数と互換の変数オブジェクト >> そのまま接続
		if (object instanceof AbstractVariable) {
			identifier = this.connect( (AbstractVariable)object );

		// 内部関数と互換の変数オブジェクト >> そのまま接続
		} else if (object instanceof AbstractFunction) {
			identifier = this.connect( (AbstractFunction)object );

		// XVCI 1 形式の外部変数プラグイン >> そのまま接続
		} else if (object instanceof ExternalVariableConnector1) {
			identifier = this.connect( (ExternalVariableConnector1)object );

		// XFCI 1 形式の外部関数プラグイン >> そのまま接続
		} else if (object instanceof ExternalVariableConnector1) {
			identifier = this.connect( (ExternalFunctionConnector1)object );

		// クラスフィールドの場合 >> アダプタで XVCI1 に変換して接続
		} else if (object instanceof Field) {
			FieldXvci1Adapter adapter = new FieldXvci1Adapter((Field)object);
			identifier = this.connect(adapter);

		// クラスメソッドの場合 >> アダプタで XFCI1 に変換して接続
		} else if (object instanceof Method) {
			MethodXfci1Adapter adapter = new MethodXfci1Adapter((Method)object);
			identifier = this.connect(adapter);

		// インスタンスフィールドやインスタンスメソッドは、所属インスタンスも格納する配列で渡される
		} else if (object instanceof Object[]) {

			Object[] objects = (Object[])object;

			// インスタンスフィールドの場合 >> 引数からFieldとインスタンスを取り出し、アダプタで XVCI1 に変換して接続
			if (objects.length == 2 && objects[0] instanceof Field) {
				Field field = (Field)objects[0]; // [0] はフィールドのリフレクション
				Object instance = objects[1];    // [1] はフィールドの所属インスタンス
				FieldXvci1Adapter adapter = new FieldXvci1Adapter(field, instance);
				identifier = this.connect(adapter);

			// インスタンスフィールドの場合 >> 引数からMethodとインスタンスを取り出し、アダプタで XFCI1 に変換して接続
			} else if (objects.length == 2 && objects[0] instanceof Method) {
				Method method = (Method)objects[0]; // [0] はメソッドのリフレクション
				Object instance = objects[1];       // [1] はメソッドの所属インスタンス
				MethodXfci1Adapter adapter = new MethodXfci1Adapter(method, instance);
				identifier = this.connect(adapter);
			}

		// プリミティブラッパー -> 変数を生成して登録
		} else if (DataConverter.isConvertible(object.getClass())) {
			DataConverter dataConverter;
			try {
				dataConverter = new DataConverter(object.getClass());
			} catch (DataException e) {
				throw new VnanoRuntimeException();
			}
			DataType dataType = dataConverter.getDataType();
			int rank = dataConverter.getRank();
			Variable variable = new Variable(bindName, dataType, rank);
			variable.setDataContainer(dataConverter.convertToDataContainer(object));
			identifier = this.connect(variable);
		} else {
			// 暫定的な簡易例外処理
			System.err.println("接続不能な形式: " + object.getClass());
			throw new VnanoRuntimeException();
		}

		if (identifier == null) {
			// 暫定的な簡易例外処理
			throw new IllegalArgumentException("接続不能な形式");
		} else {
			this.identifierBindNameMap.put(identifier, bindName);
			return;
		}
	}


	/**
	 * ホスト言語側のコード内で宣言されているフィールドを、
	 * スクリプト内からアクセスできる外部変数として接続します。
	 * （ここに同期タイミングの注意）
	 *
	 * @param field 外部変数として接続するフィールド
	 * @param instance フィールドの属するクラスのインスタンス
	 * @return プラグインと一意に対応する管理用キー
	 * @throws DataException 引数や戻り値のデータ型が非対応であった場合にスローされます。
	 */
	public String connect(Field field, Object instance) throws DataException {
		FieldXvci1Adapter adapter = new FieldXvci1Adapter(field, instance);
		return this.connect(adapter);
	}


	/**
	 * ホスト言語側のコード内で宣言されているメソッドを、
	 * スクリプト内からアクセスできる外部関数として接続します。
	 *
	 * @param method 外部関数として接続するメソッド
	 * @param instance メソッドの属するクラスのインスタンス
	 * @return プラグインと一意に対応する管理用キー
	 * @throws DataException データ型が非対応であった場合にスローされます。
	 */
	public String connect(Method method, Object instance) throws DataException {
		MethodXfci1Adapter adapter = new MethodXfci1Adapter(method,instance);
		return this.connect(adapter);
	}


	/**
	 * {@link org.vcssl.connect.ExternalVariableConnector1 XVCI 1}
	 * のプラグイン・インターフェースを用いて、
	 * ホスト言語で実装された外部変数オブジェクトを接続します。
	 *
	 * @param connector XVCI準拠の外部変数
	 * @return プラグインと一意に対応する管理用キー
	 * @throws DataException データ型が非対応であった場合にスローされます。
	 */
	public String connect(ExternalVariableConnector1 connector) throws DataException {
		connector.initializeForConnection();
		Xvci1VariableAdapter adapter = new Xvci1VariableAdapter(connector);
		this.globalVariableTable.addVariable(adapter);
		String identifier = IdentifierSyntax.getUniqueIdentifierOf(adapter);
		return identifier;
	}


	/**
	 * {@link org.vcssl.connect.ExternalFunctionConnector1 XFCI 1}
	 * のプラグイン・インターフェースを用いて、
	 * ホスト言語で実装された外部関数オブジェクトを接続します。
	 *
	 * @param connector XFCI準拠の外部関数
	 * @return プラグインと一意に対応する管理用キー
	 * @throws DataException 引数や戻り値のデータ型が非対応であった場合にスローされます。
	 */
	public String connect(ExternalFunctionConnector1 connector) throws DataException {
		connector.initializeForConnection();
		Xfci1FunctionAdapter adapter = new Xfci1FunctionAdapter(connector);
		this.functionTable.addFunction(adapter);
		String identifier = IdentifierSyntax.getUniqueIdentifierOf(adapter);
		return identifier;
	}


	/**
	 * Vnano処理系内部の変数形式に準拠した変数オブジェクトを接続します。
	 *
	 * @param variable 変数オブジェクト
	 * @return 変数オブジェクトと一意に対応する管理用キー
	 */
	public String connect(AbstractVariable variable) {
		this.globalVariableTable.addVariable(variable);
		String identifier = IdentifierSyntax.getUniqueIdentifierOf(variable);
		return identifier;
	}


	/**
	 * Vnano処理系内部の関数形式に準拠した関数オブジェクトを接続します。
	 *
	 * @param function 関数オブジェクト
	 * @return 関数オブジェクトと一意に対応する管理用キー
	 */
	public String connect(AbstractFunction function) {
		this.functionTable.addFunction(function);
		String identifier = IdentifierSyntax.getUniqueIdentifierOf(function);
		return identifier;
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
	public void writeback(Memory memory, VnanoIntermediateCode intermediateCode)
			throws MemoryAccessException, DataException {

		// グローバル変数の書き戻し
		int maxGlobalAddress = intermediateCode.getMaximumGlobalAddress();
		int minGlobalAddress = intermediateCode.getMinimumGlobalAddress();
		for (int address=minGlobalAddress; address<maxGlobalAddress; address++) {

			// 仮想メモリーを参照し、グローバル変数アドレスからデータコンテナを取得
			DataContainer<?> dataContainer = memory.getDataContainer(Memory.Partition.GLOBAL, address);

			// 中間コードのシンボルテーブルを参照し、グローバル変数アドレスから一意識別子を取得
			String identifier = intermediateCode.getGlobalVariableUniqueIdentifier(address);

			// グローバル変数テーブルを参照し、一意識別子から外部変数オブジェクトを取得
			AbstractVariable variable = this.globalVariableTable.getVariableByAssemblyIdentifier(identifier);

			// 外部変数オブジェクトにデータコンテナを渡して値を更新させる
			variable.setDataContainer(dataContainer);

			// バインディングオブジェクトに含まれていた変数の場合、必要に応じてそちらの値も更新
			if (this.identifierBindNameMap.containsKey(identifier)) {
				String bindName = this.identifierBindNameMap.get(identifier);
				Object bindData = this.bindings.get(bindName);
				Class<?> bindClass = bindData.getClass();

				// バインディング側にプリミティブラッパー型として保持されている場合は、値の更新が必要
				if (DataConverter.isConvertible(bindClass)) {

					// データコンテナが保持するデータを、バインディングデータと同じ型に変換
					DataConverter converter = new DataConverter(bindClass);
					Object updatedData = converter.convertToExternalObject(dataContainer);

					// バインディングの値を上書きして更新
					this.bindings.put(bindName, updatedData);
				}
			}
		}
	}

}
