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
import org.vcssl.nano.compiler.AttributeKey;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.IdentifierSyntax;
import org.vcssl.nano.spec.ScriptWord;

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

	/** 関数名と、関数とを対応付けるマップです。同じキーの要素を複数格納するため、値をリスト化して保持します。 */
	Map<String, LinkedList<AbstractFunction>> nameFunctionMap = null;

	/** 名前空間付きの関数シグネチャと、関数とを対応付けるマップです。同じキーの要素を複数格納するため、値をリスト化して保持します。 */
	Map<String, LinkedList<AbstractFunction>> fullSignatureFunctionMap = null;

	/** 名前空間付きの関数名と、関数とを対応付けるマップです。同じキーの要素を複数格納するため、値をリスト化して保持します。 */
	Map<String, LinkedList<AbstractFunction>> fullNameFunctionMap = null;


	/**
	 * 空の関数テーブルを生成します。
	 */
	public FunctionTable() {
		this.functionList = new ArrayList<AbstractFunction>();

		this.signatureFunctionMap = new LinkedHashMap<String, LinkedList<AbstractFunction>>();
		this.nameFunctionMap = new LinkedHashMap<String, LinkedList<AbstractFunction>>();

		this.fullSignatureFunctionMap = new LinkedHashMap<String, LinkedList<AbstractFunction>>();
		this.fullNameFunctionMap = new LinkedHashMap<String, LinkedList<AbstractFunction>>();
	}


	/**
	 * 関数を追加登録します。
	 *
	 * @param function 対象の関数
	 */
	public void addFunction(AbstractFunction function) {

		// 全関数リストに追加
		this.functionList.add(function);

		String nameSpacePrefix = IdentifierSyntax.getNameSpacePrefixOf(function);

		String signature = IdentifierSyntax.getSignatureOf(function);
		String fullSignature = IdentifierSyntax.getSignatureOf(function, nameSpacePrefix);

		String functionName = function.getFunctionName();
		String fullFunctionName = nameSpacePrefix + ScriptWord.NAME_SPACE_SEPARATOR + functionName;

		IdentifierMapManager.putToMap(this.signatureFunctionMap, signature, function);
		IdentifierMapManager.putToMap(this.nameFunctionMap, functionName, function);

		IdentifierMapManager.putToMap(this.fullSignatureFunctionMap, fullSignature, function);
		IdentifierMapManager.putToMap(this.fullNameFunctionMap, fullFunctionName, function);
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
	public AbstractFunction getFunctionBySignature(
			String functionName, DataType[] parameterDataTypes, int[] parameterArrayRanks,
			boolean[] parameterDataTypeArbitrarinesses, boolean[] parameterArrayRankArbitrarinesses) {

		String[] parameterDataTypeNames = DataTypeName.getDataTypeNamesOf(parameterDataTypes);
		String signature = IdentifierSyntax.getSignatureOf(
				functionName, parameterDataTypeNames, parameterArrayRanks,
				parameterDataTypeArbitrarinesses, parameterArrayRankArbitrarinesses
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
		if (this.signatureFunctionMap.containsKey(signature)) {
			return IdentifierMapManager.getLastFromMap(this.signatureFunctionMap, signature);
		}
		if (this.fullSignatureFunctionMap.containsKey(signature)) {
			return IdentifierMapManager.getLastFromMap(this.fullSignatureFunctionMap, signature);
		}
		throw new VnanoFatalException("Function not found: " + signature);
	}


	/**
	 * 指定されたシグネチャ（関数の名称と引数情報）に対応する関数が、
	 * この関数テーブルに登録されているかどうかを判定します。
	 *
	 * @param signature 対象関数のシグネチャの文字列表現
	 * @return 登録されていればtrue
	 */
	public boolean hasFunctionWithSignature(String signature) {
		return this.signatureFunctionMap.containsKey(signature)
				|| this.fullSignatureFunctionMap.containsKey(signature);
	}


	/**
	 * 指定された関数呼び出し演算子のAST（抽象構文木）ノードにおける、
	 * 呼び出し対象の関数が、この関数テーブルに登録されているかどうかを判定します。
	 *
	 * @param callerNode 関数呼び出し演算子のASTノード
	 * @return 登録されていればtrue
	 */
	public boolean hasCalleeFunctionOf(AstNode callerNode) {
		return this.getCalleeFunctionOf(callerNode) != null;
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

		// まずコールシグネチャと宣言シグネチャが一致するものがあるか検索（あれば最も検索が速い）
		String signature = IdentifierSyntax.getSignatureOfCalleeFunctionOf(callerNode);
		if (this.hasFunctionWithSignature(signature)) {
			return this.getFunctionBySignature(signature);
		}

		// 無ければ名前だけでも一致するものがあるか検索
		String functionName = callerNode.getChildNodes()[0].getAttribute(AttributeKey.IDENTIFIER_VALUE);
		List<AbstractFunction> functionList = null;
		if (this.nameFunctionMap.containsKey(functionName)) {
			functionList = IdentifierMapManager.getAllFromMap(this.nameFunctionMap, functionName);
		} else if (this.fullNameFunctionMap.containsKey(functionName)) {
			functionList = IdentifierMapManager.getAllFromMap(this.fullNameFunctionMap, functionName);
		}

		// 名前が一致するものが無い場合は、明らかに呼び出し可能な関数は無いため検索終了
		if (functionList == null) {
			return null;
		}

		// 名前が一致する関数がある場合は、その中から引数の型のマッチを確認していく
		// （可変長引数や任意型など、コールシグネチャと宣言シグネチャが異なっても呼び出せるものが有り得る）

		AstNode[] childNodes = callerNode.getChildNodes();
		int argumentLength = childNodes.length - 1;
		int[] argumentRanks = new int[argumentLength];
		String[] argumentDataTypeNames = new String[argumentLength];
		for (int argumentIndex=0; argumentIndex<argumentLength; argumentIndex++) {
			argumentRanks[argumentIndex] = childNodes[argumentIndex+1].getRank();
			argumentDataTypeNames[argumentIndex] = childNodes[argumentIndex+1].getDataTypeName();
		}

		int functionN = functionList.size();
		for (int functionIndex=functionN-1; 0<=functionIndex; functionIndex--) {

			AbstractFunction function = functionList.get(functionIndex);
			int[] parameterRanks = function.getParameterArrayRanks();
			String[] parameterDataTypeNames = function.getParameterDataTypeNames();
			boolean[] parameterDataTypeArbitrarinesses = function.getParameterDataTypeArbitrarinesses(); // 仮引数が任意型かどうか
			boolean[] parameterArrayRankArbitrarinesses = function.getParameterArrayRankArbitrarinesses(); // 仮引数が任意次元かどうか
			int parameterLength = parameterRanks.length;

			// 引数の個数が違えばスキップ（現状では可変長引数はサポートしていないため）
			if (parameterLength != argumentLength) {
				continue;
			}

			// 仮引数と実引数の型の互換性を先頭から比較し、呼び出し可能か判断する
			boolean isCallable = true; // 呼び出し可能でなければ無ければこの値を false にする
			for (int parameterIndex=0; parameterIndex<parameterLength; parameterIndex++) {

				String paramTypeName = parameterDataTypeNames[parameterIndex];
				String argTypeName = argumentDataTypeNames[parameterIndex];
				int paramRank = parameterRanks[parameterIndex];
				int argRank = argumentRanks[parameterIndex];

				boolean isParamAnyType = parameterDataTypeArbitrarinesses[parameterIndex];
				boolean isParamAnyRank = parameterArrayRankArbitrarinesses[parameterIndex];
				boolean isDataTypeSame = paramTypeName.equals(argTypeName);
				boolean isRankSame = (paramRank == argRank);

				// 以下、parameterIndex 番目の仮引数と実引数を比較する。
				// この引数に互換性があれば continue して次の引数の比較へ移行、
				// 互換性が無ければその時点で呼び出し不可と判断して break する。

				// データ型と次元数の両方が完全に一致している場合は、その時点でOK
				if (isDataTypeSame && isRankSame) {
					continue;
				}

				// 仮引数が任意型かつ任意次元の場合は、実引数が何であっても互換性があるのでOK
				if (isParamAnyType && isParamAnyRank) {
					continue;
				}

				// 仮引数が任意型であり、かつ任意次元ではない場合は、次元数が一致していればOK
				if (isParamAnyType && !isParamAnyRank && isRankSame) {
					continue;
				}

				// 仮引数が任意次元であり、かつ任意型ではない場合は、データ型数が一致していればOK
				if (isParamAnyRank && !isParamAnyType && isDataTypeSame) {
					continue;
				}

				// ここまででOKでなければ、引数に互換性が無いので、この関数は呼び出し可能ではない
				isCallable = false;
				break;
			}

			// 呼び出し可能と判断されていれば、その関数を返して終了
			if (isCallable) {
				return function;
			}
		}

		// 呼び出し可能な関数が見つからなければ便宜的に null を返すが、
		// その場合は hasCalleeFunctionOf が false になるので、そちらで検査可能
		return null;
	}

}
