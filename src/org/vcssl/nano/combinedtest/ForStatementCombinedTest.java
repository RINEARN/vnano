package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class ForStatementCombinedTest extends CombinedTestElement {

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
			this.testForStatements();

		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}


	private void testForStatements() throws VnanoException {
		String scriptCode;
		long result;

		scriptCode =
			" int i = 123;           \n" +
			" for (i=0; i<10; i++) { \n" +
			" }                      \n" +
			" i;                     \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 10, "i=123; for(i=0;i<10;i++){}", scriptCode);

		scriptCode =
			" int i = 123;           \n" +
			" for (i=0; i>10; i++) { \n" +
			" }                      \n" +
			" i;                     \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 0, "i=0; for(i=0;i>10;i++){}", scriptCode);

		scriptCode =
			" int a = 123;               \n" +
			" for (int i=0; i<10; i++) { \n" +
			"     a++;                   \n" +
			" }                          \n" +
			" a;                         \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 133, "a=123; for(int i=0;i<10;i++){a++;}", scriptCode);

		scriptCode =
			" int a = 123;               \n" +
			" for (int i=0; i>10; i++) { \n" +
			"     a++;                   \n" +
			" }                          \n" +
			" a;                         \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 123, "a=123; for(int i=0;i>10;i++){a++;}", scriptCode);

		scriptCode =
			" int i = 0;     \n" +
			" for (;i<10;) { \n" +
			"     i++;       \n" +
			" }              \n" +
			" i;             \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 10, "i=0; for(;i<10;){ i++; }", scriptCode);

		scriptCode =
			" int i = 0;         \n" +
			" for (;(i++)<10;) { \n" +
			" }                  \n" +
			" i;                 \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 11, "i=0; for(;(i++)<10;){}", scriptCode);
		// 後置インクリメントは値を読んだ後に加算されるので、
		// iが10の時に条件式が評価されて10<10の判定になってループを抜けつつ、
		// i は加算されて最終的に11になる。

		scriptCode =
			" int i = 0;         \n" +
			" for (;(++i)<10;) { \n" +
			" }                  \n" +
			" i;                 \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 10, "i=0; for(;(++i)<10;){}", scriptCode);
		// 前置インクリメントは値を読む前に加算されるので、
		// iが9の時に条件式が評価されて、加算された後に10<10の判定になってループを抜ける。
		// なので脱出時の i は10。
	}

}
