package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class ScalarLogicalOperationCombinedTest extends CombinedTestElement {

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
			this.testAndOperations();
			this.testOrOperations();
			this.testNotOperations();
			this.testDualOperations();
			this.testTripleOperations();
			this.testQuadOperations();
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}

	private void testAndOperations() throws VnanoException {
		String scriptCode;
		boolean result;

		scriptCode = " true && true ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "true && true", scriptCode);

		scriptCode = " true && false ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "true && false", scriptCode);

		scriptCode = " false && true ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "false && true", scriptCode);

		scriptCode = " false && false ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "false && false", scriptCode);
	}

	private void testOrOperations() throws VnanoException {
		String scriptCode;
		boolean result;

		scriptCode = " true || true ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "true || true", scriptCode);

		scriptCode = " true || false ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "true || false", scriptCode);

		scriptCode = " false || true ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "false || true", scriptCode);

		scriptCode = " false || false ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "false || false", scriptCode);
	}

	private void testNotOperations() throws VnanoException {
		String scriptCode;
		boolean result;

		scriptCode = " ! true ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "! true", scriptCode);

		scriptCode = " ! false ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "! false", scriptCode);
	}

	private void testDualOperations() throws VnanoException {
		String scriptCode;
		boolean result;

		scriptCode = " true && true || true ; "; // == (true && true) || true
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "true && true || true", scriptCode);

		scriptCode = " false && true || true ; "; // == (false && true) || true == false || true == true
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "false && true || true", scriptCode);

		scriptCode = " false && (true || true) ; "; // == false && true == false
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "false && (true || true)", scriptCode);

		scriptCode = " true && false || false ; "; // == (true && false) || false
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "true && false || false", scriptCode);

		scriptCode = " false && (true || true) ; "; // 短絡評価が発生
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "false && (true || true)", scriptCode);

		scriptCode = " true && (false || false); ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "true && (false || false)", scriptCode);

		scriptCode = " true || (false && false); "; // 短絡評価が発生
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "true || (false && false)", scriptCode);
	}

	private void testTripleOperations() throws VnanoException {
		String scriptCode;
		boolean result;

		scriptCode = " true && true || true && true ; "; // == (true && true) || (true && true)
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "true && true || true && true", scriptCode);

		scriptCode = " true && false || true && true ; "; // == (true && false) || (true && true)
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "true && false || true && true", scriptCode);

		scriptCode = " true && (false || true) && true ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "true && (false || true) && true", scriptCode);

		scriptCode = " true || true && true || true ; "; // == true || (true && true) || true
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "true || true && true || true", scriptCode);

		scriptCode = " true || false && true || false ; "; // == true || (false && true) || false == true || false || false == true || false == true
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "true || false && true || false", scriptCode);

		scriptCode = " false || false && true || false ; "; // == false || (false && true) || false == false || false || false == false
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "false || false && true || false", scriptCode);
	}

	private void testQuadOperations() throws VnanoException {
		String scriptCode;
		boolean result;

		scriptCode = " true && true && true && true && true ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "true && true && true && true && true", scriptCode);

		scriptCode = " true && true || true && false || false ; "; // == (t&&t) || (t&&f) || f == (t||f) || f == t||f == t
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "true && true || true && false || false", scriptCode);

		scriptCode = " true && (true || true) && (false || false) ; "; // == t && (t||f) && (f||f) == (t&&t) && f == t&&f == f
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "true && (true || true) && (false || false)", scriptCode);
	}
}
