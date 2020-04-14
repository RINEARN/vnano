package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class SubscriptedLogicalOperationCombinedTest extends CombinedTestElement {

	VnanoEngine engine = null;

	private static final String DECLVEC_A =
		"bool a[6]; a[0]=true; a[1]=true; a[2]=false; a[3]=false; a[4]=false; a[5]=false; ";

	private static final String DECLVEC_B =
		"bool b[6]; b[0]=true; b[1]=false; b[2]=true; b[3]=false; b[4]=false; b[5]=false; ";

	private static final boolean T = true;
	private static final boolean F = false;


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
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}


	private void testAndOperations() throws VnanoException {
		String scriptCode;
		boolean result;

		scriptCode = DECLVEC_A + DECLVEC_B + " a[0] && b[0] ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, T&T, "bool[i] && bool[i]   (case 1)", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + " a[1] && b[1] ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, T&F, "bool[i] && bool[i]   (case 2)", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + " a[2] && b[2] ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, F&T, "bool[i] && bool[i]   (case 3)", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + " a[3] && b[3] ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, F&F, "bool[i] && bool[i]   (case 4)", scriptCode);
	}

	private void testOrOperations() throws VnanoException {
		String scriptCode;
		boolean result;

		scriptCode = DECLVEC_A + DECLVEC_B + " a[0] || b[0] ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, T|T, "bool[i] || bool[i]   (case 1)", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + " a[1] || b[1] ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, T|F, "bool[i] || bool[i]   (case 2)", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + " a[2] || b[2] ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, F|T, "bool[i] || bool[i]   (case 3)", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + " a[3] || b[3] ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, F|F, "bool[i] || bool[i]   (case 4)", scriptCode);
	}

	private void testNotOperations() throws VnanoException {
		String scriptCode;
		boolean result;

		scriptCode = DECLVEC_A + DECLVEC_B + " ! a[1] ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, F, "! bool[i]   (case 1)", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + " ! a[2] ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, T, "! bool[i]   (case 2)", scriptCode);
	}
}
