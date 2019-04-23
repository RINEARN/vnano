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

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.compiler.AstNode;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.IdentifierSyntax;

/**
 * <p>
 * 関数テーブルの機能を提供するクラスです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class FunctionTable {

	/** 関数を保持するリストです。 */
	List<AbstractFunction> functionList = null;

	/** 関数シグネチャと、関数とを対応付けるマップです。同じキーの要素を複数格納するため、値をリスト化して保持します。 */
	Map<String, LinkedList<AbstractFunction>> signatureFunctionMap = null;

	/** 中間アセンブリコード識別子と、関数とを対応付けるマップです。同じキーの要素を複数格納するため、値をリスト化して保持します。 */
	Map<String, LinkedList<AbstractFunction>> assemblyIdentifierFunctionMap = null;


	/**
	 * 空の関数テーブルを生成します。
	 */
	public FunctionTable() {
		this.functionList = new ArrayList<AbstractFunction>();
		this.signatureFunctionMap = new LinkedHashMap<String, LinkedList<AbstractFunction>>();
		this.assemblyIdentifierFunctionMap = new LinkedHashMap<String, LinkedList<AbstractFunction>>();
	}


	/**
	 * 関数を追加登録します。
	 *
	 * @param function 対象の関数
	 */
	public void addFunction(AbstractFunction function) {

		// 全関数リストに追加
		this.functionList.add(function);

		// シグネチャリストに追加
		String signature = IdentifierSyntax.getSignatureOf(function);
		IdentifierMapManager.putToMap(this.signatureFunctionMap, signature, function);

		// アセンブリ識別子リストに追加
		String assemblyIdentifier = IdentifierSyntax.getAssemblyIdentifierOf(function);
		IdentifierMapManager.putToMap(this.assemblyIdentifierFunctionMap, assemblyIdentifier, function);
	}


	/**
	 * この関数テーブルに登録されている、全ての関数を配列として返します。
	 *
	 * @return 登録されている全ての関数を格納する配列
	 */
	public AbstractFunction[] getFunctions() {
		return this.functionList.toArray(new AbstractFunction[0]);
	}


	/**
	 * 指定されたインデックスに対応する関数を取得します。
	 * インデックスは、変数の登録順に、0から昇順で割りふられます。
	 *
	 * @param index インデックス
	 * @return 対象の関数
	 */
	public AbstractFunction getFunctionByIndex(int index) {
		return this.functionList.get(index);
	}


	/**
	 * 指定された関数の、この変数テーブル内でのインデックスを取得します。
	 * インデックスは、関数の登録順に、0から昇順で割りふられます。
	 *
	 * @param function 対象の関数
	 * @return インデックス
	 */
	public int indexOf(AbstractFunction function) {
		return this.functionList.indexOf(function);
	}


	/**
	 * 指定されたシグネチャ（関数の名称と引数情報）に該当する関数を取得します。
	 *
	 * このメソッドは、関数がこのテーブルに存在する事を前提としており、存在しない場合は
	 * {@link org.vcssl.nano.VnanoFatalException VnanoFatalException} を発生させます。
	 *
	 * 存在しない場合にこの例外を発生させたくない場合は、事前に
	 * {@link FunctionTable#hasFunctionWithAssemblyIdentifier hasFunctionWithAssemblyIdentifier}
	 * メソッド等を使用し、関数がこのテーブルに存在する事を確認した上で使用してください。
	 *
	 * @param functionName 関数名
	 * @param parameterDataTypes 全引数のデータ型を格納する配列（各要素が各引数に対応）
	 * @param parameterArrayRanks 全引数の配列次元数を格納する配列
	 * 		（各要素が各引数に対応、スカラは0次元として扱う）
	 * @return 対象の関数
	 * @throws VnanoFatalException
	 *   指定された関数がこのテーブルに存在しなかった場合にスローされます。
	 */
	public AbstractFunction getFunctionBySignature(String functionName, DataType[] parameterDataTypes, int[] parameterArrayRanks) {

		String[] parameterDataTypeNames = DataTypeName.getDataTypeNamesOf(parameterDataTypes);
		String signature = IdentifierSyntax.getSignatureOf(
				functionName, parameterDataTypeNames, parameterArrayRanks
		);
		return this.getFunctionBySignature(signature);
	}


	/**
	 * 指定されたシグネチャ（関数の名称と引数情報）に該当する関数を取得します。
	 * 複数存在する場合は、最後に登録されたものを返します。
	 *
	 * @param signature 対象関数のシグネチャの文字列表現
	 * @return 登録されていればtrue
	 */
	public AbstractFunction getFunctionBySignature(String signature) {
		return this.signatureFunctionMap.get(signature).getLast();
	}


	/**
	 * 指定されたシグネチャ（関数の名称と引数情報）に対応する関数が、
	 * この関数テーブルに登録されているかどうかを判定します。
	 *
	 * @param signature 対象関数のシグネチャの文字列表現
	 * @return 登録されていればtrue
	 */
	public boolean hasFunctionWithSignature(String signature) {
		return this.signatureFunctionMap.containsKey(signature);
	}


	/**
	 * 指定された中間アセンブリコード識別子に対応する関数を取得します。
	 * 複数存在する場合は、最後に登録されたものを返します。
	 *
	 * @param assemblyIdentifier 対象関数のアセンブリコード識別子
	 * @return 対象の関数
	 */
	public AbstractFunction getFunctionByAssemblyIdentifier(String assemblyIdentifier) {
		return this.assemblyIdentifierFunctionMap.get(assemblyIdentifier).getLast();
	}


	/**
	 * 指定された中間アセンブリコード識別子に対応する関数が、
	 * この関数テーブルに登録されているかどうかを判定します。
	 *
	 * @param signature 対象関数のアセンブリコード識別子
	 * @return 登録されていればtrue
	 */
	public boolean hasFunctionWithAssemblyIdentifier(String assemblyIdentifier) {
		return this.assemblyIdentifierFunctionMap.containsKey(assemblyIdentifier);
	}


	/**
	 * 指定された関数呼び出し演算子のAST（抽象構文木）ノードにおける、
	 * 呼び出し対象の関数が、この関数テーブルに登録されているかどうかを判定します。
	 *
	 * @param callerNode 関数呼び出し演算子のASTノード
	 * @return 登録されていればtrue
	 */
	public boolean hasCalleeFunctionOf(AstNode callerNode) {
		String signature = IdentifierSyntax.getSignatureOfCalleeFunctionOf(callerNode);
		return this.hasFunctionWithSignature(signature);
	}


	/**
	 * 指定された関数呼び出し演算子のAST（抽象構文木）ノードにおける、
	 * 呼び出し対象の関数を取得します。
	 *
	 * @param callerNode 関数呼び出し演算子のASTノード
	 * @return 対象の関数
	 * @throws DataException 引数などのデータ型名が非対応のものであった場合にスローされます。
	 */
	public AbstractFunction getCalleeFunctionOf(AstNode callerNode) {
		String signature = IdentifierSyntax.getSignatureOfCalleeFunctionOf(callerNode);
		return this.getFunctionBySignature(signature);
	}

}
