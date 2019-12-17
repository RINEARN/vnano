package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class IfElseStatementCombinedTest extends CombinedTestElement {

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
			this.testIfStatement();
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}

	private void testIfStatement() throws VnanoException {
		String scriptCode;
		long result;

		scriptCode =
			"int a = 1;   \n" +
			"if (true) {  \n" +
			"	a = 2;    \n" +
			"}            \n" +
			"a;           \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 2, "if(true)", scriptCode);

		scriptCode =
			"int a = 1;   \n" +
			"if (false) { \n" +
			"	a = 2;    \n" +
			"}            \n" +
			"a;           \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 1, "if(false)", scriptCode);
	}
}
