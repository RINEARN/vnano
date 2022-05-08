package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class SubscriptedArithmeticOperationCombinedTest extends CombinedTestElement {

	VnanoEngine engine = null;

	private static final String DECLVEC_INT_A = "int a[3]; a[0]=10; a[1]=50; a[2]=30; ";
	private static final String DECLVEC_INT_B = "int b[3]; b[0]=5; b[1]=50; b[2]=10; ";
	private static final String DECLVEC_FLOAT_A = "float a[3]; a[0]=2.2; a[1]=1.4; a[2]=8.2; ";
	private static final String DECLVEC_FLOAT_B = "float b[3]; b[0]=4.25; b[1]=2.8; b[2]=12.8; ";
	private static final String DECLVEC_STRING_A = "string a[3]; a[0]=\"abc\"; a[1]=\"def\"; a[2]=\"ghi\"; ";
	private static final String DECLVEC_STRING_B = "string b[3]; b[0]=\"aiueo\"; b[1]=\"kakikukeko\"; b[2]=\"sasisuseso\"; ";

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
			this.testSubtractions();
			this.testMultiplications();
			this.testDivisions();
			this.testRemainders();
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}

	private void testAdditions() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;
		String resultS;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + " a[1] + b[2] ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 50l+10l, "int[i] + int[j]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + " a[1] + b[2] ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 50l+12.8d, "int[i] + float[j]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_STRING_B + " a[1] + b[2] ; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, 50l+"sasisuseso", "int[i] + string[j]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + " a[1] + b[2] ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 1.4d+10l, "float[i] + int[j]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + " a[1] + b[2] ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 1.4d+12.8d, "float[i] + float[j]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_STRING_B + " a[1] + b[2] ; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, 1.4d+"sasisuseso", "float[i] + string[j]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_INT_B + " a[1] + b[2] ; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "def"+10l, "string[i] + int[j]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_FLOAT_B + " a[1] + b[2] ; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "def"+12.8d, "string[i] + float[j]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + " a[1] + b[2] ; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "def"+"sasisuseso", "string[i] + string[j]", scriptCode);
	}

	private void testSubtractions() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + " a[1] - b[2] ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 50l-10l, "int[i] - int[j]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + " a[1] - b[2] ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 50l-12.8d, "int[i] - float[j]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + " a[1] - b[2] ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 1.4d-10l, "float[i] - int[j]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + " a[1] - b[2] ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 1.4d-12.8d, "float[i] - float[j]", scriptCode);
	}

	private void testMultiplications() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + " a[1] * b[2] ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 50l*10l, "int[i] * int[j]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + " a[1] * b[2] ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 50l*12.8d, "int[i] * float[j]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + " a[1] * b[2] ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 1.4d*10l, "float[i] * int[j]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + " a[1] * b[2] ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 1.4d*12.8d, "float[i] * float[j]", scriptCode);
	}

	private void testDivisions() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + " a[1] / b[2] ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 50l/10l, "int[i] / int[j]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + " a[1] / b[2] ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 50l/12.8d, "int[i] / float[j]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + " a[1] / b[2] ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 1.4d/10l, "float[i] / int[j]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + " a[1] / b[2] ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 1.4d/12.8d, "float[i] / float[j]", scriptCode);
	}

	private void testRemainders() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + " a[1] % b[2] ; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 50l%10l, "int[i] % int[j]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + " a[1] % b[2] ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 50l%12.8d, "int[i] % float[j]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + " a[1] % b[2] ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 1.4d%10l, "float[i] % int[j]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + " a[1] % b[2] ; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 1.4d%12.8d, "float[i] % float[j]", scriptCode);
	}

}
