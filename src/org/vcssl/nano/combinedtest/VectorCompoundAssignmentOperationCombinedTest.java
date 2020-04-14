package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class VectorCompoundAssignmentOperationCombinedTest extends CombinedTestElement {

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
			this.testVectorScalarMixedOperations();
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}

	private void testAddAssignmentOperations() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		String[] resultS;
		long[] expectedL;
		double[] expectedD;
		String[] expectedS;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + "a += b; a; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[] { 10l+5l, 50l+50l, 30l+10l };
		super.evaluateResult(resultL, expectedL, "int[] += int[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + "a += b; a; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[] { (long)(10l+4.25d), (long)(50l+2.25d), (long)(30l+12.125d) };
		super.evaluateResult(resultL, expectedL, "int[] += float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + "a += b; a; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[] { 2.5d+4.25d, 1.5d+2.25d, 8.25d+12.125d };
		super.evaluateResult(resultD, expectedD, "float[] += float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + "a += b; a; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[] { 2.5d+5l, 1.5d+50l, 8.25d+10l };
		super.evaluateResult(resultD, expectedD, "float[] += int[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + "a += b; a; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[] { "abc"+"aiueo", "def"+"kakikukeko", "ghi"+"sasisuseso" };
		super.evaluateResult(resultS, expectedS, "string[] += string[]", scriptCode);
	}

	private void testSubAssignmentOperations() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		long[] expectedL;
		double[] expectedD;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + "a -= b; a; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[] { 10l-5l, 50l-50l, 30l-10l };
		super.evaluateResult(resultL, expectedL, "int[] -= int[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + "a -= b; a; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[] { (long)(10l-4.25d), (long)(50l-2.25d), (long)(30l-12.125d) };
		super.evaluateResult(resultL, expectedL, "int[] -= float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + "a -= b; a; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[] { 2.5d-4.25d, 1.5d-2.25d, 8.25d-12.125d };
		super.evaluateResult(resultD, expectedD, "float[] -= float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + "a -= b; a; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[] { 2.5d-5l, 1.5d-50l, 8.25d-10l };
		super.evaluateResult(resultD, expectedD, "float[] -= int[]", scriptCode);
	}

	private void testMulAssignmentOperations() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		long[] expectedL;
		double[] expectedD;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + "a *= b; a; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[] { 10l*5l, 50l*50l, 30l*10l };
		super.evaluateResult(resultL, expectedL, "int[] *= int[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + "a *= b; a; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[] { (long)(10l*4.25d), (long)(50l*2.25d), (long)(30l*12.125d) };
		super.evaluateResult(resultL, expectedL, "int[] *= float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + "a *= b; a; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[] { 2.5d*4.25d, 1.5d*2.25d, 8.25d*12.125d };
		super.evaluateResult(resultD, expectedD, "float[] *= float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + "a *= b; a; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[] { 2.5d*5l, 1.5d*50l, 8.25d*10l };
		super.evaluateResult(resultD, expectedD, "float[] *= int[]", scriptCode);
	}

	private void testDivAssignmentOperations() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		long[] expectedL;
		double[] expectedD;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + "a /= b; a; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[] { 10l/5l, 50l/50l, 30l/10l };
		super.evaluateResult(resultL, expectedL, "int[] /= int[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + "a /= b; a; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[] { (long)(10l/4.25d), (long)(50l/2.25d), (long)(30l/12.125d) };
		super.evaluateResult(resultL, expectedL, "int[] /= float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + "a /= b; a; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[] { 2.5d/4.25d, 1.5d/2.25d, 8.25d/12.125d };
		super.evaluateResult(resultD, expectedD, "float[] /= float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + "a /= b; a; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[] { 2.5d/5l, 1.5d/50l, 8.25d/10l };
		super.evaluateResult(resultD, expectedD, "float[] /= int[]", scriptCode);
	}

	private void testRemAssignmentOperations() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		long[] expectedL;
		double[] expectedD;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + "a %= b; a; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[] { 10l%5l, 50l%50l, 30l%10l };
		super.evaluateResult(resultL, expectedL, "int[] %= int[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + "a %= b; a; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[] { (long)(10l%4.25d), (long)(50l%2.25d), (long)(30l%12.125d) };
		super.evaluateResult(resultL, expectedL, "int[] %= float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + "a %= b; a; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[] { 2.5d%4.25d, 1.5d%2.25d, 8.25d%12.125d };
		super.evaluateResult(resultD, expectedD, "float[] %= float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + "a %= b; a; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[] { 2.5d%5l, 1.5d%50l, 8.25d%10l };
		super.evaluateResult(resultD, expectedD, "float[] %= int[]", scriptCode);
	}

	private void testVectorScalarMixedOperations() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		String[] resultS;
		long[] expectedL;
		double[] expectedD;
		String[] expectedS;

		scriptCode = DECLVEC_INT_A + "a += 123; a; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[] { 10l+123l, 50l+123l, 30l+123l };
		super.evaluateResult(resultL, expectedL, "int[] += int", scriptCode);

		scriptCode = DECLVEC_INT_A + "a += 2.125; a; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[] { (long)(10l+2.125d), (long)(50l+2.125d), (long)(30l+2.125d) };
		super.evaluateResult(resultL, expectedL, "int[] += float", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + "a += 2.125; a; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[] { 2.5d+2.125d, 1.5d+2.125d, 8.25d+2.125d };
		super.evaluateResult(resultD, expectedD, "float[] += float", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + "a += 123; a; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[] { 2.5d+123l, 1.5d+123l, 8.25d+123l };
		super.evaluateResult(resultD, expectedD, "float[] += int", scriptCode);

		scriptCode = DECLVEC_STRING_A + "a += \"xyz\"; a; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[] { "abc"+"xyz", "def"+"xyz", "ghi"+"xyz" };
		super.evaluateResult(resultS, expectedS, "string[] += string", scriptCode);
	}
}
