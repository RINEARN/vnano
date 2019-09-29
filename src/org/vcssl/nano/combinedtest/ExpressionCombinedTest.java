package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class ExpressionCombinedTest extends CombinedTestElement {

	VnanoEngine engine = null;

	@Override
	public void initializeTest() {
		this.engine = new VnanoEngine();
	}

	@Override
	public void finalizeTest() {
		this.engine = null;
	}

	@Override
	public void executeTest() {
		try {
			testAddition();
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}

	private void testAddition() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;
		String resultS;

		scriptCode = " 1 + 2 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (1 + 2), "int + int", scriptCode);

		scriptCode = " 1 + 2.2 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1 + 2.2), "int + float", scriptCode);

		scriptCode = " 1 + \"str\" ; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "1str", "int + string", scriptCode);

		scriptCode = " 1.1 + 2 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.1 + 2), "float + int", scriptCode);

		scriptCode = " 1.1 + 2.2 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.1 + 2.2), "float + float", scriptCode);

		scriptCode = " 1.1 + \"str\" ; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "1.1str", "float + string", scriptCode);

		scriptCode = " \"str\" + 2 ; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, ("str2"), "string + int", scriptCode);

		scriptCode = " \"str\" + 2.2 ; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, ("str2.2"), "string + float", scriptCode);

		scriptCode = " \"str\" + \"ing\" ; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, ("string"), "string + string", scriptCode);
	}

}
