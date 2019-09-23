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
			testIntIntAddExpression();
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}

	private void testIntIntAddExpression() throws VnanoException {
		String scriptCode = " 1 + 2 ; ";
		long result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, (1 + 2), "Expression of addition of [int,int]", scriptCode);
	}

}
