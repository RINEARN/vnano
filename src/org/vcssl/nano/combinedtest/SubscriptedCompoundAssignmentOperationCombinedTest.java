package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class SubscriptedCompoundAssignmentOperationCombinedTest extends CombinedTestElement {

	VnanoEngine engine = null;

	private static final String DECLVEC_INT_A = "int a[3]; a[0]=10; a[1]=50; a[2]=30; ";
	private static final String DECLVEC_INT_B = "int b[3]; b[0]=5; b[1]=50; b[2]=10; ";
	private static final String DECLVEC_FLOAT_A = "float a[3]; a[0]=2.5; a[1]=1.5; a[2]=8.25; ";
	private static final String DECLVEC_FLOAT_B = "float b[3]; b[0]=4.25; b[1]=2.25; b[2]=12.125; ";
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
			this.testAddAssignmentOperations();
			this.testSubAssignmentOperations();
			this.testMulAssignmentOperations();
			this.testDivAssignmentOperations();
			this.testRemAssignmentOperations();
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}

	private void testAddAssignmentOperations() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;
		String resultS;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + "a[2] += b[2]; a[2]; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 30l+10l, "int[i] += int[i]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + "a[2] += b[2]; a[2]; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (long)(30l+12.125d), "int[i] += float[i]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + "a[2] += b[2]; a[2]; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 8.25d+12.125d, "float[i] += float[i]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + "a[2] += b[2]; a[2]; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 8.25d+10l, "float[i] += int[i]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + "a[2] += b[2]; a[2]; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "ghi"+"sasisuseso", "string[i] += string[i]", scriptCode);
	}

	private void testSubAssignmentOperations() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + "a[2] -= b[2]; a[2]; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 30l-10l, "int[i] -= int[i]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + "a[2] -= b[2]; a[2]; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (long)(30l-12.125d), "int[i] -= float[i]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + "a[2] -= b[2]; a[2]; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 8.25d-12.125d, "float[i] -= float[i]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + "a[2] -= b[2]; a[2]; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 8.25d-10l, "float[i] -= int[i]", scriptCode);
	}

	private void testMulAssignmentOperations() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + "a[2] *= b[2]; a[2]; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 30l*10l, "int[i] *= int[i]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + "a[2] *= b[2]; a[2]; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (long)(30l*12.125d), "int[i] *= float[i]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + "a[2] *= b[2]; a[2]; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 8.25d*12.125d, "float[i] *= float[i]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + "a[2] *= b[2]; a[2]; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 8.25d*10l, "float[i] *= int[i]", scriptCode);
	}

	private void testDivAssignmentOperations() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + "a[2] /= b[2]; a[2]; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 30l/10l, "int[i] /= int[i]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + "a[2] /= b[2]; a[2]; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (long)(30l/12.125d), "int[i] /= float[i]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + "a[2] /= b[2]; a[2]; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 8.25d/12.125d, "float[i] /= float[i]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + "a[2] /= b[2]; a[2]; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 8.25d/10l, "float[i] /= int[i]", scriptCode);
	}

	private void testRemAssignmentOperations() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + "a[2] %= b[2]; a[2]; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 30l%10l, "int[i] %= int[i]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + "a[2] %= b[2]; a[2]; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (long)(30l%12.125d), "int[i] %= float[i]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + "a[2] %= b[2]; a[2]; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 8.25d%12.125d, "float[i] %= float[i]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + "a[2] %= b[2]; a[2]; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 8.25d%10l, "float[i] %= int[i]", scriptCode);
	}
}
