/*
 * Copyright(C) 2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.vcssl.nano.compiler.Compiler;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.spec.OptionKey;
import org.vcssl.nano.spec.OptionValue;
import org.vcssl.nano.vm.VirtualMachine;


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
 * &raquo <a href="../../../../src/org/vcssl/nano/VnanoEngine.java">Source code</a>
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class VnanoEngine {

	/**
	 * <span class="lang-en">A map to store all names and values of specified options</span>
	 * <span class="lang-ja">指定された全てのオプション名と値を保持するマップです</span>
	 * .
	 */
	private Map<String, Object> optionMap = null;


	/**
	 * <span class="lang-en">An object to mediate information/connections between components, named as "interconnect"</span>
	 * <span class="lang-ja">処理系内の各部で共有する情報や接続を仲介するオブジェクト（インターコネクト）です</span>
	 * .
	 */
	Interconnect interconnect = null;


	/**
	 * <span class="lang-en">Create a Vnano Engine with default settings</span>
	 * <span class="lang-ja">標準設定のVnanoエンジンを生成します</span>
	 * .
	 */
	public VnanoEngine() {

		// Create an option map and set default values.
		// オプションマップを生成し, 必須項目をデフォルト値で補完
		this.optionMap = new LinkedHashMap<String, Object>();
		optionMap = OptionValue.normalizeValuesOf(optionMap);

		// Create a blank interconnect nothing is binded to.
		// 何もバインディングされていない, 空のインターコネクトを生成
		this.interconnect = new Interconnect();

		// Create a connector to access information of the engine from plug-ins, and set it to the interconnect.
		// プラグインからエンジン側の情報（オプション含む）にアクセスするためのコネクタを生成し, インターコネクトに設定
		VnanoEngineConnector engineConnector = new VnanoEngineConnector(this.optionMap);
		this.interconnect.setEngineConnector(engineConnector);
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
	 *   <span class="lang-en">Thrown when an error will be detected for the content or the processing of the script.</span>
	 *   <span class="lang-ja">スクリプトの内容または実行過程にエラーが検出された場合にスローされます.</span>
	 */
	public Object executeScript(String script) throws VnanoException {
		try {
			// Get some option items from the option map (Types of values were already checked).
			// オプションマップからいくつかの設定値を取り出す（値の型は既に検査済み）
			String evalScriptName = (String)this.optionMap.get(OptionKey.EVAL_SCRIPT_NAME);
			String[] libraryScriptNames = (String[])this.optionMap.get(OptionKey.LIBRARY_SCRIPT_NAMES);
			String[] libraryScripts = (String[])this.optionMap.get(OptionKey.LIBRARY_SCRIPTS);

			// Contain an execution-target script and library scripts into an array.
			// 実行対象スクリプトと, ライブスクリプト（複数）を1つの配列にまとめる
			String[] scripts = new String[libraryScripts.length  + 1];
			String[] names   = new String[libraryScriptNames.length + 1];
			System.arraycopy(libraryScripts,  0, scripts, 0, libraryScripts.length );
			System.arraycopy(libraryScriptNames, 0, names,   0, libraryScriptNames.length);
			scripts[libraryScripts.length] = script;
			names[libraryScriptNames.length ] = evalScriptName;

			// Translate scripts to a VRIL code (intermediate assembly code) by a compiler.
			// コンパイラでスクリプトコードからVRILコード（中間アセンブリコード）に変換
			Compiler compiler = new Compiler();
			String assemblyCode = compiler.compile(scripts, names, this.interconnect, this.optionMap);

			// Execute the VRIL code on the VM.
			// VMでVRILコードを実行
			VirtualMachine vm = new VirtualMachine();
			Object evalValue = vm.eval(assemblyCode, this.interconnect, this.optionMap);
			return evalValue;

		// If any error is occurred for the content/processing of the script,
		// set the locale to switch the language of error messages, and re-throw the exception to upper layers.
		// スクリプト内容による例外は, エラーメッセージに使用する言語ロケールを設定してから上に投げる
		} catch (VnanoException e) {
			Locale locale = (Locale)this.optionMap.get(OptionKey.LOCALE); // Type was already checked.
			e.setLocale(locale);
			throw e;

		// If unexpected exception is occurred, wrap it by the VnanoException and re-throw,
		// to prevent the stall of the host-application.
		// 実装の不備等による予期しない例外も, ホストアプリケーションを落とさないようにVnanoExceptionでラップする
		} catch (Exception unexpectedException) {
			throw new VnanoException(unexpectedException);
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

		// Connect the plug-in to the interconnect.
		// プラグインをインターコネクトに接続
		this.interconnect.connectPlugin(bindingKey, plugin);
	}


	/**
	 * <span class="lang-en">Sets options, by a Map (option map) storing names and values of options you want to set</span>
	 * <span class="lang-ja">オプションの名前と値を格納するマップ（オプションマップ）によって, オプションを設定します</span>
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

		// Supplement some option items by default values, and store the map to the field of this class.
		// 必須項目をデフォルト値で補完した上で、このクラスのフィールドに設定
		this.optionMap = OptionValue.normalizeValuesOf(optionMap);

		// Check the content of option settings.
		// オプション設定の内容を検査
		OptionValue.checkValuesOf(this.optionMap);

		// Create a connector to access information of the engine from plug-ins, and set it to the interconnect.
		// プラグインからエンジン側の情報（オプション含む）にアクセスするためのコネクタを生成し, インターコネクトに設定
		VnanoEngineConnector engineConnector = new VnanoEngineConnector(this.optionMap);
		this.interconnect.setEngineConnector(engineConnector);
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
		return this.optionMap;
	}

}
