package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class BlockStatementCombinedTest extends CombinedTestElement {

	VnanoEngine engine = null;

	@Override
	public void initializeTest(VnanoEngine engine) {
		this.engine = engine;
	}

	@Override
	public void finalizeTest() {
		this.engine = null;
	}

	@Override
	public void executeTest() {
		try {
			this.testScopesOfVariablesAndBlocks();
			this.testDuplicateVariableDeclarationsAndBlocks();
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}


	private void testScopesOfVariablesAndBlocks() throws VnanoException {
		String scriptCode;
		long resultL;
		String resultS;

		// 以下、変数にスコープ内でアクセスしている場合

		scriptCode =
			" int a = 0;       \n" +
			" {                \n" +
			"     a = 123;     \n" +
			" }                \n" +
			" a;               \n" ;

		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 123l, "int a=0; { a=123; } ", scriptCode);


		// 以下、変数にスコープ外でアクセスしている場合

		scriptCode =
			" {                 \n" +
			"     int a = 0;    \n" +
			" }                 \n" +
			" a = 123;          \n" ;

		try {
			this.engine.executeScript(scriptCode);

			// 例外が投げられずにここに達するのは、期待されたエラーが検出されていないので失敗
			super.missedExpectedError("{ int a=0; } a=123; (should be failed) ", scriptCode);
		} catch (VnanoException vne) {

			// 例外が投げられればエラーが検出されているので成功
			super.succeeded("{ int a=0; } a=123; (should be failed) ");
		}

		scriptCode =
			" {                 \n" +
			"     int a = 0;    \n" +
			" }                 \n" +
			"                   \n" +
			" {                 \n" +
			"     a = 123;      \n" +
			" }                 \n" ;

		try {
			this.engine.executeScript(scriptCode);

			// 例外が投げられずにここに達するのは、期待されたエラーが検出されていないので失敗
			super.missedExpectedError("{ int a=0; } { a=123; } (should be failed) ", scriptCode);
		} catch (VnanoException vne) {

			// 例外が投げられればエラーが検出されているので成功
			super.succeeded("{ int a=0; } a=123; (should be failed) ");
		}


		// 以下、変数にスコープ内のネストしたブロック内でアクセスしている場合

		scriptCode =
			" int a = 0;        \n" +
			" {                 \n" +
			"     a++;          \n" +
			"     {             \n" +
			"         a++;      \n" +
			"     }             \n" +
			" }                 \n" +
			" a;                \n" ;

		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 2l, "int a=0; { a++; { a++; } } ", scriptCode);

		scriptCode =
			" int a = 0;        \n" +
			" int b = 0;        \n" +
			" {                 \n" +
			"     a++;          \n" +
			"     {             \n" +
			"         a++;      \n" +
			"     }             \n" +
			"     a++;          \n" +
			"     b++;          \n" +
			" }                 \n" +
			" a + \",\" + b;    \n" ;

		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "3,1", "int a=0; int b=0; { a++; { a++; } a++; b++; } ", scriptCode);

		scriptCode =
			" {                   \n" +
			"     int a = 0;      \n" +
			"     {               \n" +
			"         int b = 0;  \n" +
			"         a++;        \n" +
			"     }               \n" +
			"     b++;            \n" +
			" }                   \n" ;

		try {
			this.engine.executeScript(scriptCode);

			// 例外が投げられずにここに達するのは、期待されたエラーが検出されていないので失敗
			super.missedExpectedError("{ int a=0; { int b=0; a++; } b++; } (should be failed) ", scriptCode);
		} catch (VnanoException vne) {

			// 例外が投げられればエラーが検出されているので成功
			super.succeeded("{ int a=0; { int b=0; a++; } b++; } (should be failed) ");
		}


		scriptCode =
			" {                   \n" +
			"     int a = 0;      \n" +
			"     {               \n" +
			"         int b = 0;  \n" +
			"     }               \n" + // 連続閉じブロックで、正しくスコープを2階層降りているかどうか
			" }                   \n" + // (意味解析での実装上の特殊パターン)
			" a = 123;            \n" ;

		try {
			this.engine.executeScript(scriptCode);

			// 例外が投げられずにここに達するのは、期待されたエラーが検出されていないので失敗
			super.missedExpectedError("{ int a=0; { int b=0; } } a=123; (should be failed) ", scriptCode);
		} catch (VnanoException vne) {

			// 例外が投げられればエラーが検出されているので成功
			super.succeeded("{ int a=0; { int b=0; } } a=123; (should be failed) ");
		}
	}


	void testDuplicateVariableDeclarationsAndBlocks() throws VnanoException {
		String scriptCode;

		// 以下、スコープが重ならない場所で同じ識別子の変数を宣言した場合

		scriptCode =
			" {                \n" +
			"     int a = 1;   \n" +
			" }                \n" +
			" {                \n" +
			"     int a = 2;   \n" +
			" }                \n" ;

		this.engine.executeScript(scriptCode);
		this.succeeded("{ int a=1; } { int a=2; } "); // エラーにならず実行できた時点で成功


		// 以下、スコープが重なる場所で同じ識別子の変数を宣言した場合

		scriptCode =
			" {                \n" +
			"     int a = 1;   \n" +
			"     int a = 2;   \n" +
			" }                \n" ;

		try {
			this.engine.executeScript(scriptCode);

			// 例外が投げられずにここに達するのは、期待されたエラーが検出されていないので失敗
			super.missedExpectedError("{ int a=1; int a=2; } (should be failed) ", scriptCode);
		} catch (VnanoException vne) {

			// 例外が投げられればエラーが検出されているので成功
			super.succeeded("{ int a=1; int a=2; } (should be failed) ");
		}

		scriptCode =
			" {                   \n" +
			"     int a = 1;      \n" +
			"     {               \n" +
			"         int a = 2;  \n" +  // ※ より深いブロックで、浅いブロックと同名の変数を宣言する事は、
			"     }               \n" +  //    現状の VCSSL では許容しているけれど Vnano では厳しめにエラー扱いにする
			" }                   \n" ;

		try {
			this.engine.executeScript(scriptCode);

			// 例外が投げられずにここに達するのは、期待されたエラーが検出されていないので失敗
			super.missedExpectedError("{ int a=1; { int a=2; } } (should be failed) ", scriptCode);
		} catch (VnanoException vne) {

			// 例外が投げられればエラーが検出されているので成功
			super.succeeded("{ int a=1; { int a=2; } } (should be failed) ");
		}

	}
}
