package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class VectorArithmeticOperationCombinedTest extends CombinedTestElement {

	VnanoEngine engine = null;

	private static final String DECLVEC_INT_A = "int a[3]; a[0]=10; a[1]=50; a[2]=30; ";
	private static final String DECLVEC_INT_B = "int b[3]; b[0]=5; b[1]=50; b[2]=10; ";
	private static final String DECLVEC_INT_C = "int c[3]; c[0]=2; c[1]=20; c[2]=32; ";
	private static final String DECLVEC_INT_D = "int d[3]; d[0]=1; d[1]=2; d[2]=3; ";
	private static final String DECLVEC_FLOAT_A = "float a[3]; a[0]=2.2; a[1]=1.4; a[2]=8.2; ";
	private static final String DECLVEC_FLOAT_B = "float b[3]; b[0]=4.25; b[1]=2.8; b[2]=12.8; ";
	private static final String DECLVEC_FLOAT_C = "float c[3]; c[0]=1.125; c[1]=32.4; c[2]=22.8; ";
	private static final String DECLVEC_FLOAT_D = "float d[3]; d[0]=0.5; d[1]=64.2; d[2]=256.125; ";
	private static final String DECLVEC_STRING_A = "string a[3]; a[0]=\"abc\"; a[1]=\"def\"; a[2]=\"ghi\"; ";
	private static final String DECLVEC_STRING_B = "string b[3]; b[0]=\"aiueo\"; b[1]=\"kakikukeko\"; b[2]=\"sasisuseso\"; ";
	private static final String DECLVEC_STRING_C = "string c[3]; c[0]=\"NaCl\"; c[1]=\"H2O\"; c[2]=\"CaCO3\"; ";
	private static final String DECLVEC_STRING_D = "string d[3]; d[0]=\"Si\"; d[1]=\"Ni\"; d[2]=\"Al\"; ";

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
			this.testDualOperations();
			this.testTripleOperations();
			this.testVectorScalarMixedOperations();
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}

	private void testAdditions() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		String[] resultS;
		long[] expectedL;
		double[] expectedD;
		String[] expectedS;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + " a + b ; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[]{ 10l+5l, 50l+50l, 30l+10l };
		super.evaluateResult(resultL, expectedL, "int[] + int[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + " a + b ; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 10l+4.25d, 50l+2.8d, 30l+12.8d };
		super.evaluateResult(resultD, expectedD, "int[] + float[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_STRING_B + " a + b ; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[]{ 10l+"aiueo", 50l+"kakikukeko", 30l+"sasisuseso" };
		super.evaluateResult(resultS, expectedS, "int[] + string[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + " a + b ; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d+5l, 1.4d+50l, 8.2d+10l };
		super.evaluateResult(resultD, expectedD, "float[] + int[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + " a + b ; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d+4.25d, 1.4d+2.8d, 8.2d+12.8d };
		super.evaluateResult(resultD, expectedD, "float[] + float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_STRING_B + " a + b ; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[]{ 2.2d+"aiueo", 1.4d+"kakikukeko", 8.2d+"sasisuseso" };
		super.evaluateResult(resultS, expectedS, "float[] + string[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_INT_B + " a + b ; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[]{ "abc"+5l, "def"+50l, "ghi"+10l };
		super.evaluateResult(resultS, expectedS, "string[] + int[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_FLOAT_B + " a + b ; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[]{ "abc"+4.25d, "def"+2.8d, "ghi"+12.8d };
		super.evaluateResult(resultS, expectedS, "string[] + float[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + " a + b ; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[]{ "abc"+"aiueo", "def"+"kakikukeko", "ghi"+"sasisuseso" };
		super.evaluateResult(resultS, expectedS, "string[] + string[]", scriptCode);
	}

	private void testSubtractions() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		long[] expectedL;
		double[] expectedD;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + " a - b ; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[]{ 10l-5l, 50l-50l, 30l-10l };
		super.evaluateResult(resultL, expectedL, "int[] - int[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + " a - b ; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 10l-4.25d, 50l-2.8d, 30l-12.8d };
		super.evaluateResult(resultD, expectedD, "int[] - float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + " a - b ; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d-5l, 1.4d-50l, 8.2d-10l };
		super.evaluateResult(resultD, expectedD, "float[] - int[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + " a - b ; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d-4.25d, 1.4d-2.8d, 8.2d-12.8d };
		super.evaluateResult(resultD, expectedD, "float[] - int[]", scriptCode);
	}

	private void testMultiplications() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		long[] expectedL;
		double[] expectedD;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + " a * b ; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[]{ 10l*5l, 50l*50l, 30l*10l };
		super.evaluateResult(resultL, expectedL, "int[] * int[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + " a * b ; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 10l*4.25d, 50l*2.8d, 30l*12.8d };
		super.evaluateResult(resultD, expectedD, "int[] * float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + " a * b ; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d*5l, 1.4d*50l, 8.2d*10l };
		super.evaluateResult(resultD, expectedD, "float[] + int[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + " a * b ; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d*4.25d, 1.4d*2.8d, 8.2d*12.8d };
		super.evaluateResult(resultD, expectedD, "float[] * int[]", scriptCode);
	}

	private void testDivisions() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		long[] expectedL;
		double[] expectedD;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + " a / b ; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[]{ 10l/5l, 50l/50l, 30l/10l };
		super.evaluateResult(resultL, expectedL, "int[] / int[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + " a / b ; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 10l/4.25d, 50l/2.8d, 30l/12.8d };
		super.evaluateResult(resultD, expectedD, "int[] / float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + " a / b ; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d/5l, 1.4d/50l, 8.2d/10l };
		super.evaluateResult(resultD, expectedD, "float[] / int[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + " a / b ; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d/4.25d, 1.4d/2.8d, 8.2d/12.8d };
		super.evaluateResult(resultD, expectedD, "float[] / int[]", scriptCode);
	}

	private void testRemainders() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		long[] expectedL;
		double[] expectedD;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + " a % b ; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[]{ 10l%5l, 50l%50l, 30l%10l };
		super.evaluateResult(resultL, expectedL, "int[] % int[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + " a % b ; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 10l%4.25d, 50l%2.8d, 30l%12.8d };
		super.evaluateResult(resultD, expectedD, "int[] % float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + " a % b ; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d%5l, 1.4d%50l, 8.2d%10l };
		super.evaluateResult(resultD, expectedD, "float[] % int[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + " a % b ; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d%4.25d, 1.4d%2.8d, 8.2d%12.8d };
		super.evaluateResult(resultD, expectedD, "float[] % int[]", scriptCode);
	}

	private void testDualOperations() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		String[] resultS;
		long[] expectedL;
		double[] expectedD;
		String[] expectedS;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + DECLVEC_INT_C + " a + b + c; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[]{ 10l+5l+2l, 50l+50l+20l, 30l+10l+32l };
		super.evaluateResult(resultL, expectedL, "int[] + int[] + int[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + DECLVEC_INT_C + " a + b * c; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[]{ 10l+5l*2l, 50l+50l*20l, 30l+10l*32l };
		super.evaluateResult(resultL, expectedL, "int[] + int[] * int[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + DECLVEC_INT_C + " a / b - c; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[]{ 10l/5l-2l, 50l/50l-20l, 30l/10l-32l };
		super.evaluateResult(resultL, expectedL, "int[] / int[] - int[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + DECLVEC_INT_C + " a * b % c; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[]{ 10l*5l%2l, 50l*50l%20l, 30l*10l%32l };
		super.evaluateResult(resultL, expectedL, "int[] * int[] % int[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + DECLVEC_FLOAT_C + " a + b + c; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d+4.25d+1.125d, 1.4d+2.8d+32.4d, 8.2d+12.8d+22.8d };
		super.evaluateResult(resultD, expectedD, "float[] + float[] + float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + DECLVEC_FLOAT_C + " a + b * c; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d+4.25d*1.125d, 1.4d+2.8d*32.4d, 8.2d+12.8d*22.8d };
		super.evaluateResult(resultD, expectedD, "float[] + float[] * float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + DECLVEC_FLOAT_C + " a / b - c; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d/4.25d-1.125d, 1.4d/2.8d-32.4d, 8.2d/12.8d-22.8d };
		super.evaluateResult(resultD, expectedD, "float[] / float[] - float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + DECLVEC_FLOAT_C + " a * b % c; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d*4.25d%1.125d, 1.4d*2.8d%32.4d, 8.2d*12.8d%22.8d };
		super.evaluateResult(resultD, expectedD, "float[] * float[] % float[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + DECLVEC_STRING_C + " a + b + c; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[]{ "abc"+"aiueo"+"NaCl", "def"+"kakikukeko"+"H2O", "ghi"+"sasisuseso"+"CaCO3" };
		super.evaluateResult(resultS, expectedS, "string[] + string[] + string[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + DECLVEC_INT_C + " a + b + c; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d+5l+2l, 1.4d+50l+20l, 8.2d+10l+32l };
		super.evaluateResult(resultD, expectedD, "float[] + int[] + int[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + DECLVEC_INT_C + " a + b + c; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 10l+4.25d+2l, 50l+2.8d+20l, 30l+12.8d+32l };
		super.evaluateResult(resultD, expectedD, "int[] + float[] + int[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + DECLVEC_FLOAT_C + " a + b + c; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 10l+5l+1.125d, 50l+50l+32.4d, 30l+10l+22.8d };
		super.evaluateResult(resultD, expectedD, "int[] + int[] + float[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + DECLVEC_FLOAT_C + " a + b + c; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 10l+4.25d+1.125d, 50l+2.8d+32.4d, 30l+12.8d+22.8d };
		super.evaluateResult(resultD, expectedD, "int[] + float[] + float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + DECLVEC_FLOAT_C + " a + b + c; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d+5l+1.125d, 1.4d+50l+32.4d, 8.2d+10l+22.8d };
		super.evaluateResult(resultD, expectedD, "float[] + int[] + float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + DECLVEC_INT_C + " a + b + c; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d+4.25d+2l, 1.4d+2.8d+20l, 8.2d+12.8d+32l };
		super.evaluateResult(resultD, expectedD, "float[] + float[] + int[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_INT_B + DECLVEC_STRING_C + " a + b + c; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[]{ "abc"+5l+"NaCl", "def"+50l+"H2O", "ghi"+10l+"CaCO3" };
		super.evaluateResult(resultS, expectedS, "string[] + int[] + string[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_FLOAT_B + DECLVEC_STRING_C + " a + b + c; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[]{ "abc"+4.25d+"NaCl", "def"+2.8d+"H2O", "ghi"+12.8d+"CaCO3" };
		super.evaluateResult(resultS, expectedS, "string[] + float[] + string[]", scriptCode);
	}

	private void testTripleOperations() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		String[] resultS;
		long[] expectedL;
		double[] expectedD;
		String[] expectedS;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + DECLVEC_INT_C + DECLVEC_INT_D + " a + b + c + d; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[]{ 10l+5l+2l+1l, 50l+50l+20l+2l, 30l+10l+32l+3l };
		super.evaluateResult(resultL, expectedL, "int[] + int[] + int[] + int[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + DECLVEC_INT_C + DECLVEC_INT_D + " a * b - c / d; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[]{ 10l*5l-2l/1l, 50l*50l-20l/2l, 30l*10l-32l/3l };
		super.evaluateResult(resultL, expectedL, "int[] * int[] - int[] / int[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + DECLVEC_FLOAT_C + DECLVEC_FLOAT_D + " a + b + c + d; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d+4.25d+1.125d+0.5d, 1.4d+2.8d+32.4d+64.2d, 8.2d+12.8d+22.8d+256.125d };
		super.evaluateResult(resultD, expectedD, "float[] + float[] + float[] + float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + DECLVEC_FLOAT_C + DECLVEC_FLOAT_D + " a * b - c / d; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d*4.25d-1.125d/0.5d, 1.4d*2.8d-32.4d/64.2d, 8.2d*12.8d-22.8d/256.125d };
		super.evaluateResult(resultD, expectedD, "float[] * float[] - float[] / float[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + DECLVEC_STRING_C + DECLVEC_STRING_D + " a + b + c + d; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[]{ "abc"+"aiueo"+"NaCl"+"Si", "def"+"kakikukeko"+"H2O"+"Ni", "ghi"+"sasisuseso"+"CaCO3"+"Al" };
		super.evaluateResult(resultS, expectedS, "string[] + string[] + string[] + string[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + DECLVEC_INT_C + DECLVEC_FLOAT_D + " a * b - c / d; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 10l*4.25d-2l/0.5d, 50l*2.8d-20l/64.2d, 30l*12.8d-32l/256.125d };
		super.evaluateResult(resultD, expectedD, "int[] * float[] - int[] / float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + DECLVEC_FLOAT_C + DECLVEC_INT_D + " a + b + c + d; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d+5l+1.125d+1l, 1.4d+50l+32.4d+2l, 8.2d+10l+22.8d+3l };
		super.evaluateResult(resultD, expectedD, "float[] + int[] + float[] + int[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_INT_B + DECLVEC_STRING_C + DECLVEC_INT_D + " a + b + c + d; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[]{ "abc"+5l+"NaCl"+1l, "def"+50l+"H2O"+2l, "ghi"+10l+"CaCO3"+3l };
		super.evaluateResult(resultS, expectedS, "string[] + int[] + string[] + int[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_FLOAT_B + DECLVEC_STRING_C + DECLVEC_FLOAT_D + " a + b + c + d; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[]{ "abc"+4.25d+"NaCl"+0.5d, "def"+2.8d+"H2O"+64.2d, "ghi"+12.8d+"CaCO3"+256.125d };
		super.evaluateResult(resultS, expectedS, "string[] + float[] + string[] + float[]", scriptCode);
	}

	private void testVectorScalarMixedOperations() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		String[] resultS;
		long[] expectedL;
		double[] expectedD;
		String[] expectedS;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + DECLVEC_INT_C + DECLVEC_INT_D + " 123 + b + c + d; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[]{ 123l+5l+2l+1l, 123l+50l+20l+2l, 123l+10l+32l+3l };
		super.evaluateResult(resultL, expectedL, "int + int[] + int[] + int[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + DECLVEC_INT_C + DECLVEC_INT_D + " a + 123 + c + d; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[]{ 10l+123l+2l+1l, 50l+123l+20l+2l, 30l+123l+32l+3l };
		super.evaluateResult(resultL, expectedL, "int[] + int + int[] + int[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + DECLVEC_INT_C + DECLVEC_INT_D + " a + b + 123 + d; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[]{ 10l+5l+123l+1l, 50l+50l+123l+2l, 30l+10l+123l+3l };
		super.evaluateResult(resultL, expectedL, "int[] + int[] + int + int[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + DECLVEC_INT_C + DECLVEC_INT_D + " a + b + c + 123; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[]{ 10l+5l+2l+123l, 50l+50l+20l+123l, 30l+10l+32l+123l };
		super.evaluateResult(resultL, expectedL, "int[] + int[] + int[] + int", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + DECLVEC_INT_C + DECLVEC_INT_D + " 123 + 456 + c + d; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[]{ 123l+456l+2l+1l, 123l+456l+20l+2l, 123l+456l+32l+3l };
		super.evaluateResult(resultL, expectedL, "int + int + int[] + int[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + DECLVEC_INT_C + DECLVEC_INT_D + " 123 + b + 456 + d; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[]{ 123l+5l+456l+1l, 123l+50l+456l+2l, 123l+10l+456l+3l };
		super.evaluateResult(resultL, expectedL, "int + int[] + int + int[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + DECLVEC_INT_C + DECLVEC_INT_D + " 123 + b + c + 456; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[]{ 123l+5l+2l+456l, 123l+50l+20l+456l, 123l+10l+32l+456l };
		super.evaluateResult(resultL, expectedL, "int + int[] + int[] + int", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + DECLVEC_INT_C + DECLVEC_INT_D + " a + 123 + 456 + d; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[]{ 10l+123l+456l+1l, 50l+123l+456l+2l, 30l+123l+456l+3l };
		super.evaluateResult(resultL, expectedL, "int[] + int + int + int[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + DECLVEC_INT_C + DECLVEC_INT_D + " a + 123 + c + 456; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[]{ 10l+123l+2l+456l, 50l+123l+20l+456l, 30l+123l+32l+456l };
		super.evaluateResult(resultL, expectedL, "int[] + int + int[] + int", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + DECLVEC_INT_C + DECLVEC_INT_D + " a + 123 + 456 + 789; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[]{ 10l+123l+456l+789l, 50l+123l+456l+789l, 30l+123l+456l+789l };
		super.evaluateResult(resultL, expectedL, "int[] + int + int + int", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + DECLVEC_INT_C + DECLVEC_INT_D + " 123 + 456 + 789 + d; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[]{ 123l+456l+789l+1l, 123l+456l+789l+2l, 123l+456l+789l+3l };
		super.evaluateResult(resultL, expectedL, "int + int + int + int[]", scriptCode);


		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + DECLVEC_FLOAT_C + DECLVEC_FLOAT_D + " 1.23 + b + c + d; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 1.23d+4.25d+1.125d+0.5d, 1.23d+2.8d+32.4d+64.2d, 1.23d+12.8d+22.8d+256.125d };
		super.evaluateResult(resultD, expectedD, "float + float[] + float[] + float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + DECLVEC_FLOAT_C + DECLVEC_FLOAT_D + " a + 1.23 + c + d; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d+1.23d+1.125d+0.5d, 1.4d+1.23d+32.4d+64.2d, 8.2d+1.23d+22.8d+256.125d };
		super.evaluateResult(resultD, expectedD, "float[] + float + float[] + float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + DECLVEC_FLOAT_C + DECLVEC_FLOAT_D + " a + b + 1.23 + d; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d+4.25d+1.23d+0.5d, 1.4d+2.8d+1.23d+64.2d, 8.2d+12.8d+1.23d+256.125d };
		super.evaluateResult(resultD, expectedD, "float[] + float[] + float + float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + DECLVEC_FLOAT_C + DECLVEC_FLOAT_D + " a + b + c + 1.23; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d+4.25d+1.125+1.23d, 1.4d+2.8d+32.4d+1.23d, 8.2d+12.8d+22.8d+1.23d };
		super.evaluateResult(resultD, expectedD, "float[] + float[] + float[] + float", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + DECLVEC_FLOAT_C + DECLVEC_FLOAT_D + " 1.23 + 4.56 + c + d; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 1.23d+4.56d+1.125d+0.5d, 1.23d+4.56d+32.4d+64.2d, 1.23d+4.56d+22.8d+256.125d };
		super.evaluateResult(resultD, expectedD, "float + float + float[] + float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + DECLVEC_FLOAT_C + DECLVEC_FLOAT_D + " 1.23 + b + 4.56 + d; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 1.23d+4.25d+4.56d+0.5d, 1.23d+2.8d+4.56d+64.2d, 1.23d+12.8d+4.56d+256.125d };
		super.evaluateResult(resultD, expectedD, "float + float[] + float + float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + DECLVEC_FLOAT_C + DECLVEC_FLOAT_D + " 1.23 + b + c + 4.56; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 1.23d+4.25d+1.125d+4.56, 1.23d+2.8d+32.4d+4.56d, 1.23d+12.8d+22.8d+4.56d };
		super.evaluateResult(resultD, expectedD, "float + float[] + float[] + float", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + DECLVEC_FLOAT_C + DECLVEC_FLOAT_D + " a + 1.23 + 4.56 + d; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d+1.23d+4.56d+0.5d, 1.4d+1.23d+4.56d+64.2d, 8.2d+1.23d+4.56d+256.125d };
		super.evaluateResult(resultD, expectedD, "float[] + float + float + float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + DECLVEC_FLOAT_C + DECLVEC_FLOAT_D + " a + 1.23 + c + 4.56; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d+1.23d+1.125d+4.56d, 1.4d+1.23d+32.4d+4.56d, 8.2d+1.23d+22.8d+4.56 };
		super.evaluateResult(resultD, expectedD, "float[] + float + float[] + float", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + DECLVEC_FLOAT_C + DECLVEC_FLOAT_D + " a + 1.23 + 4.56 + 7.89; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 2.2d+1.23d+4.56d+7.89d, 1.4d+1.23d+4.56d+7.89d, 8.2d+1.23d+4.56d+7.89d };
		super.evaluateResult(resultD, expectedD, "float[] + float + float + float", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + DECLVEC_FLOAT_C + DECLVEC_FLOAT_D + " 1.23 + 4.56 + 7.89 + d; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[]{ 1.23d+4.56d+7.89d+0.5d, 1.23d+4.56d+7.89d+64.2d, 1.23d+4.56d+7.89d+256.125d };
		super.evaluateResult(resultD, expectedD, "float + float + float + float[]", scriptCode);


		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + DECLVEC_STRING_C + DECLVEC_STRING_D + " \"x\" + b + c + d; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[]{ "x"+"aiueo"+"NaCl"+"Si", "x"+"kakikukeko"+"H2O"+"Ni", "x"+"sasisuseso"+"CaCO3"+"Al" };
		super.evaluateResult(resultS, expectedS, "string + string[] + string[] + string[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + DECLVEC_STRING_C + DECLVEC_STRING_D + " a + \"x\" + c + d; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[]{ "abc"+"x"+"NaCl"+"Si", "def"+"x"+"H2O"+"Ni", "ghi"+"x"+"CaCO3"+"Al" };
		super.evaluateResult(resultS, expectedS, "string[] + string + string[] + string[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + DECLVEC_STRING_C + DECLVEC_STRING_D + " a + b + \"x\" + d; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[]{ "abc"+"aiueo"+"x"+"Si", "def"+"kakikukeko"+"x"+"Ni", "ghi"+"sasisuseso"+"x"+"Al" };
		super.evaluateResult(resultS, expectedS, "string[] + string[] + string + string[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + DECLVEC_STRING_C + DECLVEC_STRING_D + " a + b + c + \"x\"; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[]{ "abc"+"aiueo"+"NaCl"+"x", "def"+"kakikukeko"+"H2O"+"x", "ghi"+"sasisuseso"+"CaCO3"+"x" };
		super.evaluateResult(resultS, expectedS, "string[] + string[] + string[] + string", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + DECLVEC_STRING_C + DECLVEC_STRING_D + " \"x\" + \"y\" + c + d; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[]{ "x"+"y"+"NaCl"+"Si", "x"+"y"+"H2O"+"Ni", "x"+"y"+"CaCO3"+"Al" };
		super.evaluateResult(resultS, expectedS, "string + string + string[] + string[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + DECLVEC_STRING_C + DECLVEC_STRING_D + " \"x\" + b + \"y\" + d; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[]{ "x"+"aiueo"+"y"+"Si", "x"+"kakikukeko"+"y"+"Ni", "x"+"sasisuseso"+"y"+"Al" };
		super.evaluateResult(resultS, expectedS, "string + string[] + string + string[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + DECLVEC_STRING_C + DECLVEC_STRING_D + " \"x\" + b + c + \"y\"; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[]{ "x"+"aiueo"+"NaCl"+"y", "x"+"kakikukeko"+"H2O"+"y", "x"+"sasisuseso"+"CaCO3"+"y" };
		super.evaluateResult(resultS, expectedS, "string + string[] + string[] + string", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + DECLVEC_STRING_C + DECLVEC_STRING_D + " a + \"x\" + \"y\" + d; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[]{ "abc"+"x"+"y"+"Si", "def"+"x"+"y"+"Ni", "ghi"+"x"+"y"+"Al" };
		super.evaluateResult(resultS, expectedS, "string[] + string + string + string[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + DECLVEC_STRING_C + DECLVEC_STRING_D + " a + \"x\" + c + \"y\"; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[]{ "abc"+"x"+"NaCl"+"y", "def"+"x"+"H2O"+"y", "ghi"+"x"+"CaCO3"+"y" };
		super.evaluateResult(resultS, expectedS, "string[] + string + string[] + string", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + DECLVEC_STRING_C + DECLVEC_STRING_D + " a + \"x\" + \"y\" + \"z\"; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[]{ "abc"+"x"+"y"+"z", "def"+"x"+"y"+"z", "ghi"+"x"+"y"+"z" };
		super.evaluateResult(resultS, expectedS, "string[] + string + string + string", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + DECLVEC_STRING_C + DECLVEC_STRING_D + " \"x\" + \"y\" + \"z\" + d; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[]{ "x"+"y"+"z"+"Si", "x"+"y"+"z"+"Ni", "x"+"y"+"z"+"Al" };
		super.evaluateResult(resultS, expectedS, "string[] + string + string + string", scriptCode);
	}
}
