package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class ArithmeticExpressionCombinedTest extends CombinedTestElement {

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
			this.testAddition();
			this.testMultiplication();
			this.testDivision();
			this.testSubtraction();
			this.testRemainder();
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

	private void testSubtraction() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;

		scriptCode = " 1 - 2 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (1 - 2), "int - int (negative result)", scriptCode);

		scriptCode = " 10 - 3 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (10 - 3), "int - int (positive result)", scriptCode);

		scriptCode = " 1 - 2.3 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1 - 2.3), "int - float", scriptCode);

		scriptCode = " 1.2 - 3 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 - 3), "float - int", scriptCode);

		scriptCode = " 1.2 - 3.4 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 - 3.4), "float - float (negative result)", scriptCode);

		scriptCode = " 5.6 - 3.4 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (5.6 - 3.4), "float - float (positive result)", scriptCode);
	}

	private void testMultiplication() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;

		scriptCode = " 2 * 3 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (2 * 3), "int * int", scriptCode);

		scriptCode = " 2 * 2.3 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (2 * 2.3), "int * float", scriptCode);

		scriptCode = " 2.3 * 2 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (2.3 * 2), "float * int", scriptCode);

		scriptCode = " 2.3 * 4.5 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (2.3 * 4.5), "float * float", scriptCode);
	}

	private void testDivision() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;

		scriptCode = " 10 / 2 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (10 / 2), "int / int", scriptCode);

		scriptCode = " 10 / 3 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (10 / 3), "int / int (indivisible)", scriptCode);

		scriptCode = " 10 / 2.3 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (10 / 2.3), "int / float", scriptCode);

		scriptCode = " 12.3 / 2 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (12.3 / 2), "float / int", scriptCode);

		scriptCode = " 1.2 / 3.4 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 / 3.4), "float / float", scriptCode);
	}

	private void testRemainder() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;

		scriptCode = " 10 % 3 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (10 % 3), "int % int", scriptCode);

		scriptCode = " 10 % 2.3 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (10 % 2.3), "int % float", scriptCode);

		scriptCode = " 12.3 % 2 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (12.3 % 2), "float % int", scriptCode);

		scriptCode = " 12.3 % 3.4 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (12.3 % 3.4), "float % float", scriptCode);
	}

}
