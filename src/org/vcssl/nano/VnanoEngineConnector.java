/*
 * Copyright(C) 2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano;

import java.util.Map;

import org.vcssl.connect.EngineConnector1;

/**
 * <span class="lang-en">
 * The connector class to access the script engine of the Vnano from plug-ins
 * </span>
 * <span class="lang-ja">
 * プラグイン等からVnanoのスクリプトエンジンにアクセスするためのコネクタークラスです
 * </span>
 * .
 *
 * <p>
 * &raquo <a href="../../../../src/org/vcssl/nano/VnanoEngineConnector.java">Source code</a>
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public final class VnanoEngineConnector implements EngineConnector1 {

	Map<String,Object> optionMap = null;

	public VnanoEngineConnector(Map<String,Object> optionMap) {
		this.optionMap = optionMap;
	}

	/**
	 * <span class="lang-en">
	 * Checks whether the option is set or not
	 * </span>
	 * <span class="lang-ja">
	 * 指定された名称のオプションが設定されているかどうかを判定します
	 * </span>
	 * .
	 *
	 * @param optionKey
	 *   <span class="lang-en">The key of the option (option name) to be checked.</span>
	 *   <span class="lang-ja">判定するオプションのキー（オプション名）.</span>
	 *
	 * @return
	 *   <span class="lang-en">The chech result (if the option is set, then returns true)</span>
	 *   <span class="lang-ja">判定結果（保持していれば true）.</span>
	 */
	@Override
	public boolean hasOptionValue(String optionKey) {
		return this.optionMap.containsKey(optionKey);
	}


	/**
	 * <span class="lang-en">
	 * Gets the value of the option
	 * </span>
	 * <span class="lang-ja">
	 * 指定されたオプションの値を取得します
	 * </span>
	 * .
	 *
	 * @param optionKey
	 *   <span class="lang-en">The key of the option (option name).</span>
	 *   <span class="lang-ja">オプションのキー（オプション名）.</span>
	 *
	 * @return
	 *   <span class="lang-en">The option value.</span>
	 *   <span class="lang-ja">オプションの値.</span>
	 */
	@Override
	public Object getOptionValue(String optionName) {
		return this.optionMap.get(optionName);
	}

}
