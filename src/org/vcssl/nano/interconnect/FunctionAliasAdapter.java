/*
 * Copyright(C) 2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.compiler.AstNode;
import org.vcssl.nano.compiler.AttributeKey;
import org.vcssl.nano.compiler.LexicalAnalyzer;
import org.vcssl.nano.compiler.Parser;
import org.vcssl.nano.compiler.Token;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.IdentifierSyntax;
import org.vcssl.nano.spec.ScriptWord;
import org.vcssl.nano.spec.LanguageSpecContainer;
import org.vcssl.nano.vm.memory.DataContainer;


/**
 * <p>
 * 関数を、別名の関数としてラップするためのアダプタークラスです。
 * 主に、{@link Interconnect Interconnect} 内で外部関数プラグインを接続する際に、
 * スクリプト内からアクセスするための識別子を変更可能にするために使用されます。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class FunctionAliasAdapter extends AbstractFunction {

	/** 各種の言語仕様設定類を格納するコンテナを保持します。 */
	private final LanguageSpecContainer LANG_SPEC;

	/** 上記コンテナ内の、スクリプト言語の語句が定義された設定オブジェクトを保持します。 */
	private final ScriptWord SCRIPT_WORD;

	/** 上記コンテナ内の、識別子の判定規則類が定義された設定オブジェクトを保持します。 */
	private final IdentifierSyntax IDENTIFIER_SYNTAX;

	/** 上記コンテナ内の、データ型名が定義された設定オブジェクトを保持します。 */
	private final DataTypeName DATA_TYPE_NAME;


	/** このアダプタで変換する関数を保持します。 */
	private AbstractFunction function = null;

	/** 関数名を保持します。 */
	private String functionName;


	/**
	 * 指定された関数を、別名に変換するためのアダプターを生成します。
	 *
	 * @param function 変換対象の関数
	 * @param langSpec 言語仕様設定
	 */
	public FunctionAliasAdapter(AbstractFunction function, LanguageSpecContainer langSpec) {
		this.LANG_SPEC = langSpec;
		this.SCRIPT_WORD = langSpec.SCRIPT_WORD;
		this.IDENTIFIER_SYNTAX = langSpec.IDENTIFIER_SYNTAX;
		this.DATA_TYPE_NAME = langSpec.DATA_TYPE_NAME;

		this.function = function;
		this.functionName = this.function.getFunctionName();
	}


	/**
	 * 関数名や引数情報など、スクリプト内から呼び出し対象関数を識別するために必要な情報を上書きします。
	 * これにより、このアダプタがラップする外部関数に対して、スクリプト内から別名でアクセスする事ができます。
	 *
	 * 引数 signature は、コールシグネチャ表記で指定してください。
	 * コールシグネチャ表記では、例えばその関数をスクリプトの文法で宣言すると
	 * void fun(int a, int b, float c[]) {...} となるような場合、fun(int,int,float[]) と表記します。
	 * 引数名の有無は任意です。
	 *
	 * @param signature 上書きする関数情報のコールシグネチャ表記
	 * @throws シグネチャ表記に文法エラーがあった場合にスローされます
	 */
	public final void setCallSignature(String signature) throws VnanoException {

		// プラグインから素直に求めたコールシグネチャを用意（エラーメッセージで使用）
		String expectedSignature = IDENTIFIER_SYNTAX.getSignatureOf(this);

		// コールシグネチャに仮の戻り値とブロックを付けて関数宣言のコードにする
		String functionDeclarationCode
			= DATA_TYPE_NAME.voidPlaceholder + " " + signature + SCRIPT_WORD.blockBegin + SCRIPT_WORD.blockEnd;

		// 字句解析でトークン列に変換
		Token[] tokens = new LexicalAnalyzer(LANG_SPEC).analyze(functionDeclarationCode, "");

		// 構文解析で関数宣言のASTに変換
		AstNode rootAst = new Parser(LANG_SPEC).parse(tokens);
		if (!rootAst.hasChildNodes()) {
			throw new VnanoException(
				ErrorType.INVALID_EXTERNAL_FUNCTION_SIGNATURE, new String[] { signature, expectedSignature }
			);
		}
		AstNode functionAst = rootAst.getChildNodes()[0];

		// 関数のASTとして解釈できていなければ、表記が文法的に正しくないのでエラー
		if (functionAst.getType() != AstNode.Type.FUNCTION) {
			throw new VnanoException(
				ErrorType.INVALID_EXTERNAL_FUNCTION_SIGNATURE, new String[] { signature, expectedSignature }
			);
		}

		// 関数名を上書き
		this.setFunctionName( functionAst.getAttribute(AttributeKey.IDENTIFIER_VALUE) );

		// 以下、引数情報などの不一致が無いか検査

		boolean errorDetected = false; // 異常があれば true にする

		AstNode[] childNodes = functionAst.getChildNodes();
		int paramLength = childNodes.length;

		String[] parameterDataTypeNames = this.function.getParameterDataTypeNames();
		int[] parameterArrayRanks = this.function.getParameterArrayRanks();

		// 子ノードの数が引数の個数が一致していなければエラー
		errorDetected |= (paramLength != parameterDataTypeNames.length);

		// 引数を1個ずつ検査
		for (int paramIndex=0; paramIndex<paramLength; paramIndex++) {
			AstNode paramNode = childNodes[paramIndex];

			// 引数の変数ノードがあるべき箇所に別の種類のノードがあればエラー
			errorDetected |= (paramNode.getType() != AstNode.Type.VARIABLE);

			// 引数のデータ型が、非互換なものに変わっていればエラー
			errorDetected |= ( !this.isCompatibleDataTypeName(
				paramNode.getDataTypeName(), parameterDataTypeNames[paramIndex]
			));

			// 引数の配列次元数の検査
			if (function.isParameterCountArbitrary()) {
				// 引数が任意個に設定されている場合は、「 ... type unnamed[...] 」表記なので構文上は1次元
				errorDetected |= ( paramNode.getRank() != 1 );
			} else {
				// 通常の引数の場合は、違っていればエラー
				errorDetected |= ( paramNode.getRank() != parameterArrayRanks[paramIndex] );
			}
		}

		// 上で異常が見つかっていればエラー処理
		if (errorDetected) {
			throw new VnanoException(
				ErrorType.INVALID_EXTERNAL_FUNCTION_SIGNATURE, new String[] { signature, expectedSignature }
			);
		}
	}


	/**
	 * コールシグネチャ表記の変更おいて、新しいシグネチャ内での引数のデータ型が、
	 * 元のデータ型に対して互換（同型か、同型のエイリアス）であるかどうかを判定します。
	 *
	 * @param typeA 引数の型（変更前と変更後のどちらか一方）
	 * @param typeB 引数の型（typeAの他方）
	 * @return 互換ならtrue
	 */
	private final boolean isCompatibleDataTypeName(String typeA, String typeB) {
		if (typeA.equals(typeB)) {
			return true;
		}

		if (typeA.equals(DATA_TYPE_NAME.defaultInt) && typeB.equals(DATA_TYPE_NAME.longInt)) {
			return true;
		}
		if (typeA.equals(DATA_TYPE_NAME.longInt) && typeB.equals(DATA_TYPE_NAME.defaultInt)) {
			return true;
		}

		if (typeA.equals(DATA_TYPE_NAME.defaultFloat) && typeB.equals(DATA_TYPE_NAME.doubleFloat)) {
			return true;
		}
		if (typeA.equals(DATA_TYPE_NAME.doubleFloat) && typeB.equals(DATA_TYPE_NAME.defaultFloat)) {
			return true;
		}

		return false;
	}


	/**
	 * 関数名を取得します。
	 *
	 * @return 関数名
	 */
	@Override
	public final String getFunctionName() {
		return this.functionName;
	}


	/**
	 * 関数名を上書き変更します。
	 *
	 * @param functionName 関数名
	 */
	public final void setFunctionName(String functionName) {
		this.functionName = functionName;
	}


	/**
	 * 所属している名前空間があるかどうかを判定します。
	 *
	 * @return 名前空間に所属していれば true
	 */
	@Override
	public final boolean hasNameSpace() {
		return this.function.hasNameSpace();
	}


	/**
	 * 所属している名前空間を返します。
	 *
	 * @return 名前空間
	 */
	@Override
	public final String getNameSpace() {
		return this.function.getNameSpace();
	}


	/**
	 * 全ての仮引数の名称を配列として取得します。
	 *
	 * @return 各仮引数の名称を格納する配列
	 */
	@Override
	public final String[] getParameterNames() {
		return this.function.getParameterNames();
	}


	/**
	 * 全ての仮引数のデータ型名を配列として取得します。
	 * 返される型名の表記内に、配列部分 [][]...[] は含まれません。
	 *
	 * @return 各仮引数のデータ型名を格納する配列
	 */
	@Override
	public final String[] getParameterDataTypeNames() {
		return this.function.getParameterDataTypeNames();
	}


	/**
	 * 全ての仮引数の配列次元数（スカラは0次元として扱う）を配列として取得します。
	 *
	 * @return 各仮引数の配列次元数を格納する配列
	 */
	@Override
	public final int[] getParameterArrayRanks() {
		return this.function.getParameterArrayRanks();
	}


	/**
	 * 全ての仮引数において、データ型が可変であるかどうかを格納する配列を返します。
	 *
	 * @return 各仮引数のデータ型が可変であるかどうかを格納する配列
	 */
	@Override
	public final boolean[] getParameterDataTypeArbitrarinesses() {
		return this.function.getParameterDataTypeArbitrarinesses();
	}


	/**
	 * 全ての仮引数において、配列次元数が可変であるかどうかを格納する配列を返します。
	 *
	 * @return 各仮引数の配列次元数が可変であるかどうかを格納する配列
	 */
	@Override
	public final boolean[] getParameterArrayRankArbitrarinesses() {
		return this.function.getParameterArrayRankArbitrarinesses();
	}


	/**
	 * 全ての仮引数において、参照渡しであるかどうかを格納する配列を返します。
	 *
	 * @return 各仮引数が参照渡しであるかどうかを格納する配列
	 */
	@Override
	public boolean[] getParameterReferencenesses() {
		return this.function.getParameterReferencenesses();
	}


	/**
	 * 全ての仮引数において、定数であるかどうかを格納する配列を返します。
	 *
	 * @return 各仮引数が定数であるかどうかを格納する配列
	 */
	@Override
	public boolean[] getParameterConstantnesses() {
		return this.function.getParameterConstantnesses();
	}


	/**
	 * 仮引数の個数が任意であるかどうかを返します。
	 *
	 * @return 仮引数の個数が任意であるかどうか
	 */
	@Override
	public final boolean isParameterCountArbitrary() {
		return this.function.isParameterCountArbitrary();
	}


	/**
	 * 可変長引数であるかどうかを判定します。
	 *
	 * @return 可変長引数であればtrue
	 */
	@Override
	public final boolean hasVariadicParameters() {
		return this.function.hasVariadicParameters();
	}


	/**
	 * 戻り値のデータ型名を取得します。
	 * 返される型名の表記内に、配列部分 [][]...[] は含まれません。
	 *
	 * @return 戻り値のデータ型名
	 */
	@Override
	public final String getReturnDataTypeName(String[] argumentDataTypeNames, int[] argumentArrayRanks) {
		return this.function.getReturnDataTypeName(argumentDataTypeNames, argumentArrayRanks);
	}


	/**
	 * 戻り値の配列次元数を取得します。
	 *
	 * @return 戻り値の配列次元数
	 */
	@Override
	public final int getReturnArrayRank(String[] argumentDataTypeNames, int[] argumentArrayRanks) {
		return this.function.getReturnArrayRank(argumentDataTypeNames, argumentArrayRanks);
	}


	/**
	 * 関数を実行します。
	 *
	 * @param argumentDataUnits 実引数のデータを保持するデータユニットの配列（各要素が個々の実引数に対応）
	 * @param returnDataUnit 戻り値のデータを格納するデータユニット
	 */
	@Override
	public final void invoke(DataContainer<?>[] argumentDataUnits, DataContainer<?> returnDataUnit) throws VnanoException {
		this.function.invoke(argumentDataUnits, returnDataUnit);
	}
}
