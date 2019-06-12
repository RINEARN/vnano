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
import org.vcssl.nano.spec.SpecialBindingKey;
import org.vcssl.nano.spec.OptionKey;
import org.vcssl.nano.spec.OptionValue;
import org.vcssl.nano.vm.VirtualMachine;


/**
 * Vnanoで記述されたコードを実行するためのスクリプトエンジン（Vnanoエンジン）であり、
 * Vnano処理系の最上階層となるクラスです。
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class VnanoEngine {

	private static final String DEFAULT_EVAL_SCRIPT_NAME = "EVAL_SCRIPT";
	private static final String DEFAULT_LIBRARY_SCRIPT_NAME = "LIBRARY_SCRIPT";

	/** 全てのオプション名と値を保持するマップです。 */
	private Map<String, Object> optionMap = null;

	/** エラーメッセージの言語を決めるロケール設定を保持します。 */
	private Locale locale = Locale.getDefault();

	private String[] libraryScriptNames = new String[0];
	private String[] libraryScriptCode = new String[0];
	private String evalScriptName = DEFAULT_EVAL_SCRIPT_NAME;

	/** 処理系内の接続仲介オブジェクト（インターコネクト）です。 */
	Interconnect interconnect = null;


	/**
	 * 標準設定のVnanoエンジンを生成します。
	 */
	public VnanoEngine() {

		// オプションマップを生成し、必須項目をデフォルト値で補完
		this.optionMap = new LinkedHashMap<String, Object>();
		optionMap = OptionValue.supplementDefaultValuesOf(optionMap);

		// 何もバインディングされていない、空のインターコネクトを生成
		this.interconnect = new Interconnect();

		// プラグインからエンジン側の情報（オプション含む）にアクセスするためのコネクタを生成し、インターコネクトに設定
		VnanoEngineConnector engineConnector = new VnanoEngineConnector(this.optionMap);
		this.interconnect.setEngineConnector(engineConnector);
	}


	public Object executeScript(String scriptCode) throws VnanoException {
		try {

			// eval対象のコードとライブラリコードを配列にまとめる
			String[] scripts = new String[this.libraryScriptCode.length  + 1];
			String[] names   = new String[this.libraryScriptNames.length + 1];
			System.arraycopy(this.libraryScriptCode,  0, scripts, 0, this.libraryScriptCode.length );
			System.arraycopy(this.libraryScriptNames, 0, names,   0, this.libraryScriptNames.length);
			scripts[this.libraryScriptCode.length] = scriptCode;
			names[this.libraryScriptNames.length ] = this.evalScriptName;

			// コンパイラでVnanoスクリプトから中間アセンブリコード（VRILコード）に変換
			Compiler compiler = new Compiler();
			String assemblyCode = compiler.compile(scripts, names, this.interconnect, this.optionMap);

			// VMで中間アセンブリコード（VRILコード）を実行
			VirtualMachine vm = new VirtualMachine();
			Object evalValue = vm.eval(assemblyCode, this.interconnect, this.optionMap);
			return evalValue;

		// スクリプト内容による例外は、エラーメッセージに使用する言語ロケールを設定してから上に投げる
		} catch (VnanoException e) {
			e.setLocale(this.locale);
			throw e;

		// 実装の不備等による予期しない例外も VnanoException でラップする（上層を落としたくない用途のため）
		} catch (Exception unexpectedException) {
			throw new VnanoException(unexpectedException);
		}
	}



	public void connectPlugin(String bindingKey, Object bindingPlugin) throws VnanoException {

		// キーを自動生成するよう設定されている場合は、キーを置き換え
		if (bindingKey.equals(SpecialBindingKey.AUTO_KEY)) {
			try {
				bindingKey = Interconnect.generateBindingKeyOf(bindingPlugin);
			} catch (VnanoException e) {
				// インターフェースの制約が無くなったので、後で検査例外に置き換える？
				throw new VnanoFatalException(
					"A binding key of \"" + bindingPlugin.getClass().getCanonicalName()
					+ "\" could not be generted automatically."
				);
			}
		}

		this.interconnect.connect(bindingKey, bindingPlugin);
	}



	/**
	 * 全オプションの名前と値を格納するマップによって、オプションを設定します。
	 *
	 * オプションマップは Map<String,Object> 型で、そのキーにはオプション名を指定します。
	 * オプション名の具体的な値は {@link org.vcssl.nano.spec.OptionKey} クラスに文字列定数として定義されています。
	 * オプションマップの値は Object 型ですが、実際の値は対応するオプション名によって異なります。
	 * こちらも、具体的な内容は {@link org.vcssl.nano.spec.OptionKey} クラスに記載されている説明を参照してください。
	 *
	 * @param optionMap 全オプションの名前と値を格納するマップ
	 * @throws VnanoException オプションの指定内容が正しくなかった場合にスローされます。
	 */
	public void setOptionMap(Map<String,Object> optionMap) throws VnanoException {

		// 必須項目をデフォルト値で補完し、フィールドに設定
		this.optionMap = OptionValue.supplementDefaultValuesOf(optionMap);

		// オプション内容を検査
		OptionValue.checkValuesOf(this.optionMap);


		// 以下、この階層で行うオプション反映処理

		// プラグインからエンジン側の情報（オプション含む）にアクセスするためのコネクタを生成し、インターコネクトに設定
		VnanoEngineConnector engineConnector = new VnanoEngineConnector(this.optionMap);
		this.interconnect.setEngineConnector(engineConnector);

		// ロケール設定を、このインスタンスの locale フィールドに反映
		this.locale = OptionValue.valueOf(OptionKey.LOCALE, this.optionMap, Locale.class);

		// eval対象スクリプト名の設定を、このインスタンスの evalScriptName フィールドに反映
		this.evalScriptName = OptionValue.valueOf(OptionKey.EVAL_SCRIPT_NAME, this.optionMap, String.class);

		// ライブラリ名の設定を、このインスタンスの libraryScriptNames フィールドに反映
		if (this.optionMap.containsKey(OptionKey.LIBRARY_SCRIPT_NAMES)) {
			Object value = this.optionMap.get(OptionKey.LIBRARY_SCRIPT_NAMES);
			if (value instanceof String) {
				this.libraryScriptNames = new String[] { (String)value };
			} else if (value instanceof String[]) {
				this.libraryScriptNames = (String[])this.optionMap.get(OptionKey.LIBRARY_SCRIPT_NAMES);
			} else {
				// インターフェースの制約が無くなったので、後で検査例外に置き換える？
				throw new VnanoFatalException(
					"The type of \"" + OptionKey.LIBRARY_SCRIPT_NAMES + "\" option should be \"String\" or \"String[]\""
				);
			}
		}

		// ライブラリコードの設定を、このインスタンスの libraryScriptCode フィールドに反映
		if (this.optionMap.containsKey(OptionKey.LIBRARY_SCRIPTS)) {
			Object value = this.optionMap.get(OptionKey.LIBRARY_SCRIPTS);
			if (value instanceof String) {
				this.libraryScriptCode = new String[] { (String)value };
			} else if (value instanceof String[]) {
				this.libraryScriptCode = (String[])this.optionMap.get(OptionKey.LIBRARY_SCRIPTS);
			} else {
				// インターフェースの制約が無くなったので、後で検査例外に置き換える？
				throw new VnanoFatalException(
					"The type of \"" + OptionKey.LIBRARY_SCRIPTS + "\" option should be \"String\" or \"String[]\""
				);
			}

			// ライブラリ名が指定されていない場合は、デフォルト値 + "[ライブラリインデックス]" として生成しておく
			if (!this.optionMap.containsKey(OptionKey.LIBRARY_SCRIPT_NAMES)) {
				int libraryLength = this.libraryScriptCode.length;
				this.libraryScriptNames = new String[libraryLength];
				for (int libraryIndex=0; libraryIndex<libraryLength; libraryIndex++) {
					this.libraryScriptNames[libraryIndex] = DEFAULT_LIBRARY_SCRIPT_NAME + "[" + libraryIndex + "]";
				}
			}
		}
	}


	/**
	 * 全オプションの名前と値を、対応付けて格納するマップを返します。
	 * オプションマップは Map<String,Object> 型で、そのキーにはオプション名を指定します。
	 * オプション名の具体的な値は {@link org.vcssl.nano.spec.OptionKey} クラスに文字列定数として定義されています。
	 * オプションマップの値は Object 型ですが、実際の値は対応するオプション名によって異なります。
	 * こちらも、具体的な内容は {@link org.vcssl.nano.spec.OptionKey} クラスに記載されている説明を参照してください。
	 *
	 * @return 全オプションの名前と値を格納するマップ
	 */
	public Map<String,Object> getOptionMap() {
		return this.optionMap;
	}

}
