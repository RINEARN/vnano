/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.vcssl.nano.spec.IdentifierSyntax;

/**
 * <p>
 * 変数テーブルの機能を提供するクラスです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class VariableTable implements Cloneable {

	/** 変数を保持するリストです。 */
	LinkedList<AbstractVariable> variableList = null;

	/** 変数名と、変数とを対応付けるマップです。 */
	Map<String, Object> nameVariableMap = null;

	/** 中間アセンブリコード識別子と、変数とを対応付けるマップです。 */
	Map<String, Object> assemblyIdentifierVariableMap = null;


	// 後で、名前空間指定に対応しつつ省略可能にする場合は、名前空間込みのMapと、名前空間省略のMapを持つようにする（暫定案）


	/**
	 * 空の変数テーブルを生成します。
	 */
	public VariableTable() {
		this.variableList = new LinkedList<AbstractVariable>();
		this.assemblyIdentifierVariableMap = new LinkedHashMap<String, Object>();
		this.nameVariableMap = new LinkedHashMap<String, Object>();
	}


	/**
	 * 変数を追加登録します。
	 *
	 * @param variable 対象の変数
	 */
	public void addVariable(AbstractVariable variable) {
		String varName = variable.getVariableName();
		String asmName = IdentifierSyntax.getAssemblyIdentifierOf(variable);

		// リストとマップに変数を追加
		this.variableList.add(variable);
		this.putVariableToMap(this.nameVariableMap, varName, variable); // 重複キーに対応するためのマップ操作メソッド
		this.putVariableToMap(this.assemblyIdentifierVariableMap, asmName, variable);
	}


	/**
	 * Mapに変数を登録します。
	 *
	 * その際、同じキーを持つ変数が存在する場合は、
	 * Mapの要素をLinkedListによってリスト化した上で、そのリストの末尾に登録します。
	 *
	 * @param map 登録先のMap
	 * @param key 登録する際のキー
	 * @param variable 登録する変数
	 */
	private void putVariableToMap(Map<String, Object> map, String key, AbstractVariable variable) {

		// HashMapは重複キーに対する挙動が上書きなので、以前から登録されている同名の変数が消えてしまう。
		// 従って、同じキーが既に登録されている場合は、その要素をリスト化してその末尾に登録する
		if (map.containsKey(key)) {
			Object element = map.get(key);

			// 重複キーがあり、既にリスト化されている場合 ... 末尾に追加
			if (element instanceof LinkedList) {
				@SuppressWarnings("unchecked")
				LinkedList<AbstractVariable> duplicatedList = (LinkedList<AbstractVariable>) element;
				duplicatedList.add(variable);

			// 重複キーがあるが、まだリスト化されていない場合 ... リスト化して末尾に追加
			} else if (element instanceof AbstractVariable) {

				LinkedList<AbstractVariable> duplicatedList = new LinkedList<AbstractVariable>();
				duplicatedList.add((AbstractVariable) element);
				duplicatedList.add(variable);
				map.put(key, duplicatedList);
			}

		// 重複キーが無い場合 ... そのまま追加
		} else {
			map.put(key, variable);
		}
	}


	/**
	 * 指定された変数名の変数（複数存在する場合は最後に登録されたもの）を削除します。
	 *
	 * @param variableName 削除する変数名
	 */
	public void removeLastVariable() {
		AbstractVariable variable = this.variableList.getLast();
		String varName = variable.getVariableName();
		String asmName = IdentifierSyntax.getAssemblyIdentifierOf(variable);

		// リストとマップから変数を削除
		this.variableList.removeLast();
		this.removeLastVariableFromMap(this.nameVariableMap, varName); // 重複キーに対応するためのマップ操作メソッド
		this.removeLastVariableFromMap(this.assemblyIdentifierVariableMap, asmName);
	}


	/**
	 * マップから要素を削除します。
	 *
	 * 重複キーを持つ変数が複数登録されている場合は、要素がLinkedListによってリスト化されているため、
	 * そのリストから末尾の要素を削除します。
	 * それによってリストが空になった場合は、リスト自体がマップから削除されます。
	 *
	 * @param map
	 * @param key
	 */
	private void removeLastVariableFromMap(Map<String, Object> map, String key) {

		if (!map.containsKey(key)) {
			return;
		}

		// 単純なHashMapだと重複キーに対する挙動が上書きなので、
		// マップから変数を消す際に、素直にHashMapのremoveを呼ぶと、重複キー登録の同名変数が全て消える。
		// そのため、重複キー要素はリスト化して登録するようにしているので、その場合はそのリスト末尾の要素のみ削除する。

		Object element = map.get(key);

		// 重複キーがある場合はリスト化されていので、リスト末尾から削除
		if (element instanceof LinkedList) {

			// リストの末尾を取得
			@SuppressWarnings("unchecked")
			LinkedList<AbstractVariable> duplicatedList = (LinkedList<AbstractVariable>) element;
			duplicatedList.removeLast();

			// リストが空になったらマップから消しておく
			if (duplicatedList.size() == 0) {
				map.remove(key);
			}

		// 重複キーが無い場合は普通にマップから削除
		} else {
			map.remove(key);
		}
	}


	/**
	 * この変数テーブルに登録されている、全ての変数を配列として返します。
	 *
	 * @return 登録されている全ての変数を格納する配列
	 */
	public AbstractVariable[] getVariables() {

		// このリストに変数を全部リストアップして、最後に配列に変換して返す
		List<AbstractVariable> variableList = new ArrayList<AbstractVariable>();

		Set<Entry<String, Object>> entrySet = this.nameVariableMap.entrySet();
		for (Entry<String, Object> entry: entrySet) {

			Object element = entry.getValue();
			if (element instanceof AbstractVariable) {
				variableList.add( (AbstractVariable)element );

			// 重複キーがあり、マップ要素がリスト化されている場合は、その中身を全て抽出
			} else {
				@SuppressWarnings("unchecked")
				LinkedList<AbstractVariable> duplicatedList = (LinkedList<AbstractVariable>)element;
				variableList.addAll(duplicatedList);
			}
		}
		return variableList.toArray(new AbstractVariable[0]);
	}


	/**
	 * 指定された名称の変数が、この変数テーブルに登録されているかどうかを判定します。
	 *
	 * @param name 対象変数の名称
	 * @return 含まれていればtrue
	 */
	public boolean containsVariableWithName(String name) {
		return this.nameVariableMap.containsKey(name);
	}


	/**
	 * 指定された名称の変数を取得します。
	 *
	 * @param name 対象変数の名称
	 * @return 対象の変数
	 */
	@SuppressWarnings("unchecked")
	public AbstractVariable getVariableByName(String name) {

		Object element = this.nameVariableMap.get(name);
		if (element instanceof AbstractVariable) {
			return (AbstractVariable)element;

		// 重複キーがあり、マップ要素がリスト化されている場合
		} else {
			return ((LinkedList<AbstractVariable>) element).getLast();
		}
	}


	/**
	 * 指定された中間アセンブリコード識別子に対応する変数が、
	 * この変数テーブルに登録されているかどうかを判定します。
	 *
	 * @param identifier 対象変数のアセンブリコード識別子
	 * @return 登録されていればtrue
	 */
	public boolean containsVariableWithAssemblyIdentifier(String identifier) {
		return this.assemblyIdentifierVariableMap.containsKey(identifier);
	}


	/**
	 * 指定された中間アセンブリコード識別子に対応する変数を取得します。
	 *
	 * @param identifier 対象変数のアセンブリコード識別子
	 * @return 対象の変数
	 */
	@SuppressWarnings("unchecked")
	public AbstractVariable getVariableByAssemblyIdentifier(String identifier) {

		Object element = this.assemblyIdentifierVariableMap.get(identifier);
		if (element instanceof AbstractVariable) {
			return (AbstractVariable)element;

		// 重複キーがあり、マップ要素がリスト化されている場合
		} else {
			return ((LinkedList<AbstractVariable>) element).getLast();
		}
	}


	/**
	 * 指定されたインデックスに対応する変数を取得します。
	 * インデックスは、変数の登録順に、0から昇順で割りふられます。
	 *
	 * @param index インデックス
	 * @return 対象の変数
	 */
	public AbstractVariable getVariableByIndex(int index) {
		return this.variableList.get(index);
	}


	/**
	 * 指定された変数の、この変数テーブル内でのインデックスを取得します。
	 * インデックスは、変数の登録順に、0から昇順で割りふられます。
	 *
	 * @param variable 対象の変数
	 * @return インデックス
	 */
	public int indexOf(AbstractVariable variable) {
		return this.variableList.indexOf(variable);
	}


	/**
	 * この変数テーブルに登録されている、変数の数を取得します。
	 *
	 * @return 登録されている変数の数
	 */
	public int size() {
		return this.variableList.size();
	}
}
