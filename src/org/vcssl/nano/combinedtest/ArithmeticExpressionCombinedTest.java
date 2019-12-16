package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class ArithmeticExpressionCombinedTest extends CombinedTestElement {

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
			this.testAdditions();
			this.testMultiplications();
			this.testDivisions();
			this.testSubtractions();
			this.testRemainders();
			this.testDualOperations();
			this.testTripleOperations();
			this.testQuadOperations();
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}

	private void testAdditions() throws VnanoException {
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

	private void testSubtractions() throws VnanoException {
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

	private void testMultiplications() throws VnanoException {
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

	private void testDivisions() throws VnanoException {
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

	private void testRemainders() throws VnanoException {
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

	private void testDualOperations() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;

		scriptCode = " 1 + 2 + 3 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (1 + 2 + 3), "int + int + int", scriptCode);

		scriptCode = " 1.2 + 2.3 + 3.4 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 + 2.3 + 3.4), "float + float + float", scriptCode);

		scriptCode = " 1.2 + 2 + 3 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 + 2 + 3), "float + int + int", scriptCode);

		scriptCode = " 1 + 2.3 + 3 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1 + 2.3 + 3), "int + float + int", scriptCode);

		scriptCode = " 1 + 2 + 3.4 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1 + 2 + 3.4), "int + int + float", scriptCode);

		scriptCode = " 1.2 + 2 + 3.4 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 + 2 + 3.4), "float + int + float", scriptCode);

		scriptCode = " 1.2 + 2.3 + 3 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 + 2.3 + 3), "float + float + int", scriptCode);

		scriptCode = " 1 + 2.3 + 3.4 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1 + 2.3 + 3.4), "int + float + float", scriptCode);


		scriptCode = " 1 - 2 + 3 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (1 - 2 + 3), "int - int + int", scriptCode);

		scriptCode = " (1 - 2) + 3 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, ( (1 - 2) + 3), "(int - int) + int", scriptCode);

		scriptCode = " 1 - (2 + 3) ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, ( 1 - (2 + 3) ), "int - (int + int)", scriptCode);

		scriptCode = " 1.2 - 2.3 + 3.4 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 - 2.3 + 3.4), "float - float + float", scriptCode);

		scriptCode = " (1.2 - 2.3) + 3.4 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, ( (1.2 - 2.3) + 3.4), "(float - float) + float", scriptCode);

		scriptCode = " 1.2 - (2.3 + 3.4) ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, ( 1.2 - (2.3 + 3.4) ), "float - (float + float)", scriptCode);


		scriptCode = " 1 + 2 * 3 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (1 + 2 * 3), "int + int * int", scriptCode);

		scriptCode = " (1 + 2) * 3 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, ( (1 + 2) * 3), "(int + int) * int", scriptCode);

		scriptCode = " 1 + (2 * 3) ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (1 + (2 * 3) ), "int + (int * int)", scriptCode);

		scriptCode = " 1.2 + 2.3 * 3.4 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 + 2.3 * 3.4), "float + float * float", scriptCode);

		scriptCode = " (1.2 + 2.3) * 3.4 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, ( (1.2 + 2.3) * 3.4), "(float + float) * float", scriptCode);

		scriptCode = " 1.2 + (2.3 * 3.4) ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 + (2.3 * 3.4) ), "float + (float * float)", scriptCode);
	}

	private void testTripleOperations() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;

		scriptCode = " 1 + 2 + 3 + 4 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (1 + 2 + 3 + 4), "int + int + int + int", scriptCode);

		scriptCode = " 1 - 2 + 3 - 4 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (1 - 2 + 3 - 4), "int - int + int - int", scriptCode);

		scriptCode = " 1 - (2 + 3) - 4 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, ( 1 - (2 + 3) - 4), "int - (int + int) - int", scriptCode);

		scriptCode = " 1 - (2 + 3 - 4) ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, ( 1 - (2 + 3 - 4)), "int - (int + int - int)", scriptCode);


		scriptCode = " 1.2 + 2.3 + 3.4 + 4.5 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 + 2.3 + 3.4 + 4.5), "float + float + float + float", scriptCode);

		scriptCode = " 1.2 - 2.3 + 3.4 - 4.5 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 - 2.3 + 3.4 - 4.5), "float - float + float - float", scriptCode);

		scriptCode = " 1.2 - (2.3 + 3.4) - 4.5 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, ( 1.2 - (2.3 + 3.4) - 4.5), "float - (float + float) - float", scriptCode);

		scriptCode = " 1.2 - (2.3 + 3.4 - 4.5) ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, ( 1.2 - (2.3 + 3.4 - 4.5)), "float - (float + float - float)", scriptCode);


		scriptCode = " 1 * 2 * 3 * 4 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (1 * 2 * 3 * 4), "int * int * int * int", scriptCode);

		scriptCode = " 1 + 2 * 3 + 4 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (1 + 2 * 3 + 4), "int + int * int + int", scriptCode);

		scriptCode = " 1 * 2 + 3 * 4 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (1 * 2 + 3 * 4), "int * int + int * int", scriptCode);

		scriptCode = " 1 + 2 * 3 * 4 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (1 + 2 * 3 * 4), "int + int * int * int", scriptCode);

		scriptCode = " 1 * 2 * 3 + 4 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (1 * 2 * 3 + 4), "int * int * int + int", scriptCode);

		scriptCode = " 1 * 2 * (3 + 4) ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (1 * 2 * (3 + 4)), "int * int * (int + int)", scriptCode);

		scriptCode = " 1 * (2 * 3 + 4) ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (1 * (2 * 3 + 4)), "int * (int * int + int)", scriptCode);


		scriptCode = " 1.2 * 2.3 * 3.4 * 4.5 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 * 2.3 * 3.4 * 4.5), "float * float * float * float", scriptCode);

		scriptCode = " 1.2 + 2.3 * 3.4 + 4.5 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 + 2.3 * 3.4 + 4.5), "float + float * float + float", scriptCode);

		scriptCode = " 1.2 * 2.3 + 3.4 * 4.5 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 * 2.3 + 3.4 * 4.5), "float * float + float * float", scriptCode);

		scriptCode = " 1.2 + 2.3 * 3.4 * 4.5 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 + 2.3 * 3.4 * 4.5), "float + float * float * float", scriptCode);

		scriptCode = " 1.2 * 2.3 * 3.4 + 4.5 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 * 2.3 * 3.4 + 4.5), "float * float * float + float", scriptCode);

		scriptCode = " 1.2 * 2.3 * (3.4 + 4.5) ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 * 2.3 * (3.4 + 4.5)), "float * float * (float + float)", scriptCode);

		scriptCode = " 1.2 * (2.3 * 3.4 + 4.5) ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 * (2.3 * 3.4 + 4.5)), "float * (float * float + float)", scriptCode);
	}

	private void testQuadOperations() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;

		scriptCode = " 1 + 2 + 3 + 4 + 5 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (1 + 2 + 3 + 4 + 5), "int + int + int + int + int", scriptCode);

		scriptCode = " 1 + 2 + 3 * 4 + 5 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (1 + 2 + 3 * 4 + 5), "int + int + int * int + int", scriptCode);

		scriptCode = " 1 + 2 * 3 * 4 + 5 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (1 + 2 * 3 * 4 + 5), "int + int * int * int + int", scriptCode);

		scriptCode = " 1 + 2 * 3 + 4 * 5 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (1 + 2 * 3 + 4 * 5), "int + int * int + int * int", scriptCode);

		scriptCode = " 1 + 2 * 3 * 4 * 5 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (1 + 2 * 3 * 4 * 5), "int + int * int * int * int", scriptCode);

		scriptCode = " 1 * 2 + 3 + 4 * 5 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (1 * 2 + 3 + 4 * 5), "int * int + int + int * int", scriptCode);

		scriptCode = " 1 * 2 * 3 * 4 * 5 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (1 * 2 * 3 * 4 * 5), "int * int * int * int * int", scriptCode);

		scriptCode = " 10 + 20 * 30 - 40 / 5 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (10 + 20 * 30 - 40 / 5), "int + int * int - int / int", scriptCode);

		scriptCode = " ((10 + 20) * 30 - 40) / 5 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (((10 + 20) * 30 - 40) / 5), "((int + int) * int - int) / int", scriptCode);

		scriptCode = " 10 + 20 * (30 - 40 / 5) ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (10 + 20 * (30 - 40 / 5)), "(int + int * (int - int / int)", scriptCode);

		scriptCode = " 10 + 20 * ((30 - 40) / 5) ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (10 + 20 * ((30 - 40) / 5)), "(int + int * ((int - int) / int)", scriptCode);

		scriptCode = " 10 + (20 * (30 - 40)) / 5 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (10 + (20 * (30 - 40)) / 5), "(int + (int * (int - int)) / int", scriptCode);

		scriptCode = " (10 + (20 * (30 - 40))) / 5 ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, ((10 + (20 * (30 - 40))) / 5), "((int + (int * (int - int))) / int", scriptCode);


		scriptCode = " 1.2 + 2.3 + 3.4 + 4.5 + 5.6 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 + 2.3 + 3.4 + 4.5 + 5.6), "float + float + float + float + float", scriptCode);

		scriptCode = " 1.2 + 2.3 + 3.4 * 4.5 + 5.6 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 + 2.3 + 3.4 * 4.5 + 5.6), "float + float + float * float + float", scriptCode);

		scriptCode = " 1.2 + 2.3 * 3.4 * 4.5 + 5.6 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 + 2.3 * 3.4 * 4.5 + 5.6), "float + float * float * float + float", scriptCode);

		scriptCode = " 1.2 + 2.3 * 3.4 + 4.5 * 5.6 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 + 2.3 * 3.4 + 4.5 * 5.6), "float + float * float + float * float", scriptCode);

		scriptCode = " 1.2 + 2.3 * 3.4 * 4.5 * 5.6 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 + 2.3 * 3.4 * 4.5 * 5.6), "float + float * float * float * float", scriptCode);

		scriptCode = " 1.2 * 2.3 + 3.4 + 4.5 * 5.6 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 * 2.3 + 3.4 + 4.5 * 5.6), "float * float + float + float * float", scriptCode);

		scriptCode = " 1.2 * 2.3 * 3.4 * 4.5 * 5.6 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 * 2.3 * 3.4 * 4.5 * 5.6), "float * float * float * float * float", scriptCode);

		scriptCode = " 1.2 + 2.3 * 3.4 - 4.5 / 5.6 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 + 2.3 * 3.4 - 4.5 / 5.6), "float + float * float - float / float", scriptCode);

		scriptCode = " ((1.2 + 2.3) * 3.4 - 4.5) / 5.6 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (((1.2 + 2.3) * 3.4 - 4.5) / 5.6), "((float + float) * float - float) / float", scriptCode);

		scriptCode = " 1.2 + 2.3 * (3.4 - 4.5 / 5.6) ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 + 2.3 * (3.4 - 4.5 / 5.6)), "float + float * (float - float / float)", scriptCode);

		scriptCode = " 1.2 + 2.3 * ((3.4 - 4.5) / 5.6) ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 + 2.3 * ((3.4 - 4.5) / 5.6)), "float + float * ((float - float) / float)", scriptCode);

		scriptCode = " 1.2 + (2.3 * (3.4 - 4.5)) / 5.6 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, (1.2 + (2.3 * (3.4 - 4.5)) / 5.6), "float + (float * ((float - float) / float", scriptCode);

		scriptCode = " (1.2 + (2.3 * (3.4 - 4.5))) / 5.6 ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, ((1.2 + (2.3 * (3.4 - 4.5))) / 5.6), "(float + (float * ((float - float)) / float", scriptCode);
	}
}
