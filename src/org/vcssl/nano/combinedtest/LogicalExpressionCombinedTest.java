package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class LogicalExpressionCombinedTest extends CombinedTestElement {

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
			this.testAnd();
			this.testOr();
			this.testNot();
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}

	@SuppressWarnings("unused")
	private void testAnd() throws VnanoException {
		String scriptCode;
		boolean result;

		scriptCode = " true && true ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, (true && true), "true && true", scriptCode);

		scriptCode = " true && false ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, (true && false), "true && false", scriptCode);

		scriptCode = " false && true ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, (false && true), "false && true", scriptCode);

		scriptCode = " false && false ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, (false && false), "false && false", scriptCode);
	}

	@SuppressWarnings("unused")
	private void testOr() throws VnanoException {
		String scriptCode;
		boolean result;

		scriptCode = " true || true ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, (true || true), "true || true", scriptCode);

		scriptCode = " true || false ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, (true || false), "true || false", scriptCode);

		scriptCode = " false || true ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, (false || true), "false || true", scriptCode);

		scriptCode = " false || false ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, (false || false), "false || false", scriptCode);
	}

	private void testNot() throws VnanoException {
		String scriptCode;
		boolean result;

		scriptCode = " ! true ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, (! true), "! true", scriptCode);

		scriptCode = " ! false ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, (! false), "! false", scriptCode);
	}

}
