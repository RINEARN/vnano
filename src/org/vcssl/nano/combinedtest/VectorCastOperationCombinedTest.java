package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class VectorCastOperationCombinedTest extends CombinedTestElement {

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
			this.testCastInt1DToFloat1D();
			this.testCastInt1DToString1D();
			this.testCastFloat1DToInt1D();
			this.testCastFloat1DToString1D();
			this.testCastBool1DToString1D();
			this.testCastString1DToInt1D();
			this.testCastString1DToFloat1D();
			this.testCastString1DToBool1D();

			this.testCastInt2DToFloat2D();
			this.testCastInt2DToString2D();
			this.testCastFloat2DToInt2D();
			this.testCastFloat2DToString2D();
			this.testCastBool2DToString2D();
			this.testCastString2DToInt2D();
			this.testCastString2DToFloat2D();
			this.testCastString2DToBool2D();

			this.testCastBetweenIncompatibleArrays();

		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}

	private void testCastInt1DToFloat1D() throws VnanoException {
		String scriptCode = 
			"int i[3]; " +
			"i[0] = 1; i[1] = -2; i[2] = 3; " + 
			"(float[]) i; ";
		
		double[] result = (double[])this.engine.executeScript(scriptCode);
		double[] expected = new double[] { 1.0, -2.0, 3.0 };
		super.evaluateResult(result, expected, "cast int[] to float[]", scriptCode);
	}

	private void testCastInt1DToString1D() throws VnanoException {
		String scriptCode = 
			"int i[3]; " +
			"i[0] = 1; i[1] = -2; i[2] = 3; " + 
			"(string[]) i; ";
		
		String[] result = (String[])this.engine.executeScript(scriptCode);
		String[] expected = new String[] { "1", "-2", "3" };
		super.evaluateResult(result, expected, "cast int[] to string[]", scriptCode);
	}

	private void testCastFloat1DToInt1D() throws VnanoException {
		String scriptCode = 
			"float f[3]; " +
			"f[0] = 1.1; f[1] = -2.2; f[2] = 3.3; " + 
			"(int[]) f; ";
		
		long[] result = (long[])this.engine.executeScript(scriptCode);
		long[] expected = new long[] { 1L, -2L, 3L };
		super.evaluateResult(result, expected, "cast float[] to int[]", scriptCode);
	}

	private void testCastFloat1DToString1D() throws VnanoException {
		String scriptCode = 
			"float f[3]; " +
			"f[0] = 1.25; f[1] = -2.25; f[2] = 3.25; " + 
			"(string[]) f; ";
		
		String[] result = (String[])this.engine.executeScript(scriptCode);
		String[] expected = new String[] { "1.25", "-2.25", "3.25" };
		super.evaluateResult(result, expected, "cast float[] to sting[]", scriptCode);
	}

	private void testCastBool1DToString1D() throws VnanoException {
		String scriptCode = 
			"bool b[3]; " +
			"b[0] = true; b[1] = false; b[2] = true; " + 
			"(string[]) b; ";
		
		String[] result = (String[])this.engine.executeScript(scriptCode);
		String[] expected = new String[] { "true", "false", "true" };
		super.evaluateResult(result, expected, "cast bool[] to sting[]", scriptCode);
	}

	private void testCastString1DToInt1D() throws VnanoException {
		String scriptCode = 
			"string s[3]; " +
			"s[0] = \"1\"; s[1] = \"-2\"; s[2] = \"3\"; " + 
			"(int[]) s; ";
		
		long[] result = (long[])this.engine.executeScript(scriptCode);
		long[] expected = new long[] { 1L, -2L, 3L };
		super.evaluateResult(result, expected, "cast string[] to int[]", scriptCode);
	}

	private void testCastString1DToFloat1D() throws VnanoException {
		String scriptCode = 
			"string s[3]; " +
			"s[0] = \"1.25\"; s[1] = \"-2.25\"; s[2] = \"3.25\"; " + 
			"(float[]) s; ";
		
		double[] result = (double[])this.engine.executeScript(scriptCode);
		double[] expected = new double[] { 1.25, -2.25, 3.25 };
		super.evaluateResult(result, expected, "cast string[] to float[]", scriptCode);
	}

	private void testCastString1DToBool1D() throws VnanoException {
		String scriptCode = 
			"string s[3]; " +
			"s[0] = \"true\"; s[1] = \"false\"; s[2] = \"true\"; " + 
			"(bool[]) s; ";
		
		boolean[] result = (boolean[])this.engine.executeScript(scriptCode);
		boolean[] expected = new boolean[] { true, false, true };
		super.evaluateResult(result, expected, "cast string[] to bool[]", scriptCode);
	}



	private void testCastInt2DToFloat2D() throws VnanoException {
		String scriptCode = 
			"int i[2][3]; " +
			"i[0][0] = 1;  i[0][1] = -2;  i[0][2] = 3;  " + 
			"i[1][0] = 11; i[1][1] = -22; i[1][2] = 33;  " + 
			"(float[][]) i; ";
		
		double[][] result = (double[][])this.engine.executeScript(scriptCode);
		double[][] expected = new double[][] { { 1.0, -2.0, 3.0 }, { 11.0, -22.0, 33.0 } };
		super.evaluateResult(result, expected, "cast int[][] to float[][]", scriptCode);
	}

	private void testCastInt2DToString2D() throws VnanoException {
		String scriptCode = 
			"int i[2][3]; " +
			"i[0][0] = 1;  i[0][1] = -2;  i[0][2] = 3;  " + 
			"i[1][0] = 11; i[1][1] = -22; i[1][2] = 33;  " + 
			"(string[][]) i; ";
		
		String[][] result = (String[][])this.engine.executeScript(scriptCode);
		String[][] expected = new String[][] { { "1", "-2", "3" }, { "11", "-22", "33" } };
		super.evaluateResult(result, expected, "cast int[][] to string[][]", scriptCode);
	}

	private void testCastFloat2DToInt2D() throws VnanoException {
		String scriptCode = 
			"float f[2][3]; " +
			"f[0][0] = 1.1;  f[0][1] = -2.2;  f[0][2] = 3.3;  " + 
			"f[1][0] = 11.1; f[1][1] = -22.2; f[1][2] = 33.3;  " + 
			"(int[][]) f; ";
		
		long[][] result = (long[][])this.engine.executeScript(scriptCode);
		long[][] expected = new long[][] { { 1L, -2L, 3L }, { 11L, -22L, 33L } };
		super.evaluateResult(result, expected, "cast float[][] to int[][]", scriptCode);
	}

	private void testCastFloat2DToString2D() throws VnanoException {
		String scriptCode = 
			"float f[2][3]; " +
			"f[0][0] = 1.25;  f[0][1] = -2.25;  f[0][2] = 3.25;  " + 
			"f[1][0] = 11.25; f[1][1] = -22.25; f[1][2] = 33.25;  " + 
			"(string[][]) f; ";
		
		String[][] result = (String[][])this.engine.executeScript(scriptCode);
		String[][] expected = new String[][] { { "1.25", "-2.25", "3.25" }, { "11.25", "-22.25", "33.25" } };
		super.evaluateResult(result, expected, "cast float[][] to sting[][]", scriptCode);
	}

	private void testCastBool2DToString2D() throws VnanoException {
		String scriptCode = 
			"bool b[2][3]; " +
			"b[0][0] = true;  b[0][1] = false; b[0][2] = true;  " + 
			"b[1][0] = false; b[1][1] = true;  b[1][2] = false; " + 
			"(string[][]) b; ";
		
		String[][] result = (String[][])this.engine.executeScript(scriptCode);
		String[][] expected = new String[][] { { "true", "false", "true" }, { "false", "true", "false" } };
		super.evaluateResult(result, expected, "cast bool[][] to sting[][]", scriptCode);
	}

	private void testCastString2DToInt2D() throws VnanoException {
		String scriptCode = 
			"string s[2][3]; " +
			"s[0][0] = \"1\";  s[0][1] = \"-2\";  s[0][2] = \"3\";  " + 
			"s[1][0] = \"11\"; s[1][1] = \"-22\"; s[1][2] = \"33\"; " + 
			"(int[][]) s; ";
		
		long[][] result = (long[][])this.engine.executeScript(scriptCode);
		long[][] expected = new long[][] { { 1L, -2L, 3L }, { 11L, -22L, 33L } };
		super.evaluateResult(result, expected, "cast string[][] to int[][]", scriptCode);
	}

	private void testCastString2DToFloat2D() throws VnanoException {
		String scriptCode = 
			"string s[2][3]; " +
			"s[0][0] = \"1.25\";  s[0][1] = \"-2.25\";  s[0][2] = \"3.25\";  " + 
			"s[1][0] = \"11.25\"; s[1][1] = \"-22.25\"; s[1][2] = \"33.25\"; " + 
			"(float[][]) s; ";
		
		double[][] result = (double[][])this.engine.executeScript(scriptCode);
		double[][] expected = new double[][] { { 1.25, -2.25, 3.25 }, { 11.25, -22.25, 33.25 } };
		super.evaluateResult(result, expected, "cast string[] to float[]", scriptCode);
	}

	private void testCastString2DToBool2D() throws VnanoException {
		String scriptCode = 
			"string s[2][3]; " +
			"s[0][0] = \"true\";  s[0][1] = \"false\"; s[0][2] = \"true\";  " + 
			"s[1][0] = \"false\"; s[1][1] = \"true\";  s[1][2] = \"false\"; " + 
			"(bool[][]) s; ";
		
		boolean[][] result = (boolean[][])this.engine.executeScript(scriptCode);
		boolean[][] expected = new boolean[][] { { true, false, true }, { false, true, false } };
		super.evaluateResult(result, expected, "cast string[][] to bool[][]", scriptCode);
	}



	private void testCastBetweenIncompatibleArrays() throws VnanoException {

		// Scalar to 1D: NG
		try {
			String scriptCode = 
				"float f; " +
				"(int[]) f; ";
			this.engine.executeScript(scriptCode);
			super.missedExpectedError("cast between arrays having incompatible ranks 1; (should be failed) ", scriptCode);
		} catch (VnanoException vne) {
			// Expected to be thrown.
			super.succeeded("cast between arrays having incompatible ranks 1; (should be failed) ");
		}

		// 1D to scalar: NG
		try {
			String scriptCode = 
				"float f[3]; " +
				"(int) f; ";
			this.engine.executeScript(scriptCode);
			super.missedExpectedError("cast between arrays having incompatible ranks 2; (should be failed) ", scriptCode);
		} catch (VnanoException vne) {
			// Expected to be thrown.
			super.succeeded("cast between arrays having incompatible ranks 2; (should be failed) ");
		}

		// 2D to 1D: NG
		try {
			String scriptCode = 
				"float f[2][3]; " +
				"(int[]) f; ";
			this.engine.executeScript(scriptCode);
			super.missedExpectedError("cast between arrays having incompatible ranks 3; (should be failed) ", scriptCode);
		} catch (VnanoException vne) {
			// Expected to be thrown.
			super.succeeded("cast between arrays having incompatible ranks 3; (should be failed) ");
		}

		// 1D to 2D: NG
		try {
			String scriptCode = 
				"float f[3]; " +
				"(int[][]) f; ";
			this.engine.executeScript(scriptCode);
			super.missedExpectedError("cast between arrays having incompatible ranks 4; (should be failed) ", scriptCode);
		} catch (VnanoException vne) {
			// Expected to be thrown.
			super.succeeded("cast between arrays having incompatible ranks 4; (should be failed) ");
		}
	}

}
