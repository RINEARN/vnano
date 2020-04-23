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
			this.test();
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}


	private void test() throws VnanoException {
		String scriptCode;
		long resultL;
		String resultS;

		// 変数にスコープ内でアクセスしている場合

		scriptCode =
			" int a = 0;       \n" +
			" {                \n" +
			"     a = 123;     \n" +
			" }                \n" +
			" a;               \n" ;

		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 123l, "int a=0; { a=123; } ", scriptCode);


		// 変数にスコープ外でアクセスしている場合

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


		// 変数にスコープ内のネストしたブロック内でアクセスしている場合

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
	}
}
