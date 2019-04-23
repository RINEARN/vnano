/*
 * Copyright(C) 2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import java.util.LinkedList;
import java.util.Map;

/**
 * <p>
 * このクラスは、変数テーブルや関数テーブル内で名前解決に用いるハッシュマップに対して、
 * 同じキーを持つ要素の重複を可能にするために、要素の追加・削除機能を提供します。
 * </p>
 *
 * <p>
 * 例えば変数や関数の名前解決のために HashMap や LinkedHashMap をそのまま用いると、
 * 要素を新たに追加する際、既に同じキーを持つ要素が存在した場合、
 * その要素が上書きされ、後から取り出せなくなってしまいます。
 * </p>
 *
 * <p>
 * 一方で、例えば {@link org.vcssl.nano.compiler.SemanticAnalyze SemanticAnalyzer} 内において、
 * スコープを辿りながら変数の名前解決をしていく際などは、
 * （既により広いスコープで宣言された）同名の変数があってもMapに格納し、
 * スコープを脱出する際に、その名前を持つ最後の変数のみを削除する、等の処理が必要になります。
 * このクラスでは、Mapに対してそのような操作を行うためのメソッドを提供します。
 * </p>
 *
 * <p>
 * 具体的には、Mapに対して要素を追加する際に、LinkedList で包んでから追加します。
 * 以後、さらに同じキーの要素の追加が必要になった際には、そのリストの末尾に追加されていきます。
 * 同様に、要素の削除はリストから行われ、それによってリストが空になった場合はMapからも削除されます。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 *
 * @param <Type>
 */
public class IdentifierMapManager {


	/**
	 * マップに要素を追加します。
	 *
	 * @param map 操作対象のマップ
	 * @param key 追加する要素のキー
	 * @param element 追加する要素
	 */
	public static <KeyType, ValueType> void putToMap(
			Map<KeyType, LinkedList<ValueType>> map, KeyType key, ValueType element) {

		if (!map.containsKey(key)) {
			map.put(key, new LinkedList<ValueType>());
		}
		map.get(key).add(element);
	}


	/**
	 * マップから要素を削除します。
	 * 同じキーを持つ複数存在する場合は、最後に追加されたものを削除します。
	 *
	 * @param map 操作対象のマップ
	 * @param key 削除する要素のキー
	 */
	public static <KeyType, ValueType> void removeLastFromMap(
			Map<KeyType, LinkedList<ValueType>> map, KeyType key) {

		map.get(key).removeLast();
		if (map.get(key).size() == 0) {
			map.remove(key);
		}
	}


}
