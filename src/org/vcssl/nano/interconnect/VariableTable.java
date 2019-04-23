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

	/** 変数名と、変数とを対応付けるマップです。同じキーの要素を複数格納するため、値をリスト化して保持します。 */
	Map<String, LinkedList<AbstractVariable>> nameVariableMap = null;

	/** 中間アセンブリコード識別子と、変数とを対応付けるマップです。同じキーの要素を複数格納するため、値をリスト化して保持します。 */
	Map<String, LinkedList<AbstractVariable>> assemblyIdentifierVariableMap = null;


	// 後で、名前空間指定に対応しつつ省略可能にする場合は、名前空間込みのMapと、名前空間省略のMapを持つようにする（暫定案）


	/**
	 * 空の変数テーブルを生成します。
	 */
	public VariableTable() {
		this.variableList = new LinkedList<AbstractVariable>();
		this.nameVariableMap = new LinkedHashMap<String, LinkedList<AbstractVariable>>();
		this.assemblyIdentifierVariableMap = new LinkedHashMap<String, LinkedList<AbstractVariable>>();
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
		IdentifierMapManager.putToMap(this.nameVariableMap, varName, variable); // 重複キーに対応するためのマップ操作メソッド
		IdentifierMapManager.putToMap(this.assemblyIdentifierVariableMap, asmName, variable);
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
		IdentifierMapManager.removeLastFromMap(this.nameVariableMap, varName); // 重複キーに対応するためのマップ操作メソッド
		IdentifierMapManager.removeLastFromMap(this.assemblyIdentifierVariableMap, asmName);
	}


	/**
	 * この変数テーブルに登録されている、全ての変数を配列として返します。
	 *
	 * @return 登録されている全ての変数を格納する配列
	 */
	public AbstractVariable[] getVariables() {

		// このリストに変数を全部リストアップして、配列に変換して返す
		List<AbstractVariable> variableList = new ArrayList<AbstractVariable>();

		Set<Entry<String, LinkedList<AbstractVariable>>> entrySet = this.nameVariableMap.entrySet();
		for (Entry<String, LinkedList<AbstractVariable>> entry: entrySet) {
			variableList.addAll(entry.getValue());
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
	 * 同名の要素が複数存在する場合は、最後に登録されたものを返します。
	 *
	 * @param name 対象変数の名称
	 * @return 対象の変数
	 */
	public AbstractVariable getVariableByName(String name) {
		return IdentifierMapManager.getLastFromMap(this.nameVariableMap, name);
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
	 * 同名の要素が複数存在する場合は、最後に登録されたものを返します。
	 *
	 * @param identifier 対象変数のアセンブリコード識別子
	 * @return 対象の変数
	 */
	public AbstractVariable getVariableByAssemblyIdentifier(String identifier) {
		return IdentifierMapManager.getLastFromMap(this.assemblyIdentifierVariableMap, identifier);
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
