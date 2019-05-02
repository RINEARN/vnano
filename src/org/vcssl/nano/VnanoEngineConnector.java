/*
 * Copyright(C) 2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano;

import java.util.Map;

import org.vcssl.connect.EngineConnector1;

/**
 * <p>
 * このクラスは、
 * プラグイン等からVnanoのスクリプトエンジンの情報を参照したり、
 * 機能を呼び出したりする際に使用されます。
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
	 * 指定された名称のオプションの値を保持しているかどうかを判定します。
	 *
	 * @param optionName オプションの名称
	 * @return 判定結果（保持していれば true）
	 */
	@Override
	public boolean hasOptionValue(String optionName) {
		return this.optionMap.containsKey(optionName);
	}


	/**
	 * 指定された名称のオプションの値を取得します。
	 *
	 * @param optionName オプションの名称
	 * @return オプションの値
	 */
	@Override
	public Object getOptionValue(String optionName) {
		return this.optionMap.get(optionName);
	}

}
