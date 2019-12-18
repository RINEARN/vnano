package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class WhileStatementCombinedTest extends CombinedTestElement {

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
			this.testWhileStatements();

		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}


	private void testWhileStatements() throws VnanoException {
		String scriptCode;
		long result;

		scriptCode =
			" int i = 0;       \n" +
			" while (i < 10) { \n" +
			"     i++;         \n" +
			" }                \n" +
			" i;               \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 10, "i=1; while(i<10){i++;}", scriptCode);

		scriptCode =
			" int i = 0;       \n" +
			" while (i > 10) { \n" +
			"     i++;         \n" +
			" }                \n" +
			" i;               \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 0, "i=1; while(i>10){i++;}", scriptCode);

		scriptCode =
			" int i = 0;           \n" +
			" while ((i++) < 10) { \n" +
			" }                    \n" +
			" i;                   \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 11, "i=1; while((i++)<10){}", scriptCode);

		scriptCode =
			" int i = 0;           \n" +
			" while ((++i) < 10) { \n" +
			" }                    \n" +
			" i;                   \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 10, "i=1; while((++i)<10){}", scriptCode);
	}
}
