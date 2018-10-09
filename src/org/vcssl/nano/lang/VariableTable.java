/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	List<AbstractVariable> variableList = null;

	/** 変数名と、変数とを対応付けるマップです。 */
	Map<String, AbstractVariable> nameVariableMap = null;

	/** 中間アセンブリコード識別子と、変数とを対応付けるマップです。 */
	Map<String, AbstractVariable> assemblyIdentifierVariableMap = null;

	/** 中間アセンブリコード識別子と、仮想メモリーでのアドレスとを対応付けるマップです。 */
	Map<String, Integer> assemblyIdentifierAddressMap = null;


	/**
	 * 空の変数テーブルを生成します。
	 */
	public VariableTable() {
		this.variableList = new ArrayList<AbstractVariable>();
		this.assemblyIdentifierVariableMap = new HashMap<String, AbstractVariable>();
		this.nameVariableMap = new HashMap<String, AbstractVariable>();
	}


	/**
	 * 変数を追加登録します。
	 *
	 * @param variable 対象の変数
	 */
	public void addVariable(AbstractVariable variable) {
		this.variableList.add(variable);
		this.nameVariableMap.put(variable.getVariableName(), variable);
		this.assemblyIdentifierVariableMap.put(
				IdentifierSyntax.getAssemblyIdentifierOf(variable), variable
		);
	}


	/**
	 * 指定された変数に対応する、仮想メモリー内でのアドレスを登録します。
	 *
	 * @param variable 対象の変数
	 * @param address 仮想メモリー内でのアドレス
	 */
	public void setAddress(AbstractVariable variable, int address) {
		String assemblyIdentifier = IdentifierSyntax.getAssemblyIdentifierOf(variable);
		this.assemblyIdentifierAddressMap.put(assemblyIdentifier, address);
	}


	/**
	 * この変数テーブルに登録されている、全ての変数を配列として返します。
	 *
	 * @return 登録されている全ての変数を格納する配列
	 */
	public AbstractVariable[] getVariables() {
		return this.variableList.toArray(new AbstractVariable[0]);
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
	public AbstractVariable getVariableByName(String name) {
		return this.nameVariableMap.get(name);
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
	public AbstractVariable getVariableByAssemblyIdentifier(String identifier) {
		return this.assemblyIdentifierVariableMap.get(identifier);
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
