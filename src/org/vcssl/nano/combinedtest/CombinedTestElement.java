package org.vcssl.nano.combinedtest;

import java.util.Arrays;

import org.vcssl.nano.VnanoEngine;

public class CombinedTestElement {

	// Override on subclasses
	public void initializeTest(VnanoEngine engine) { };

	// Override on subclasses
	public void finalizeTest() { };

	// Override on subclasses
	public void executeTest() { };

	protected void succeeded(String testName) {
		System.out.println(testName + ": OK.");
	}

	protected void missedExpectedError(String testName, String scriptCode) {
		throw new CombinedTestException(
			"The incorrect script has finished without detecting the error to be detected",
			testName, scriptCode
		);
	}

	protected void evaluateResult(
			Object resultValue, Object correctValue, String testName, String scriptCode) {

		if (resultValue.equals(correctValue)) {
			System.out.println(testName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, correctValue, testName, scriptCode);
		}
	}

	protected void evaluateResult(
			int resultValue, int correctValue, String testName, String scriptCode) {

		if (resultValue == correctValue) {
			System.out.println(testName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, correctValue, testName, scriptCode);
		}
	}

	protected void evaluateResult(
			int[] resultValue, int[] correctValue, String testName, String scriptCode) {

		if (Arrays.equals(resultValue, correctValue)) {
			System.out.println(testName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, correctValue, testName, scriptCode);
		}
	}

	protected void evaluateResult(
			long resultValue, long correctValue, String testName, String scriptCode) {

		if (resultValue == correctValue) {
			System.out.println(testName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, correctValue, testName, scriptCode);
		}
	}

	protected void evaluateResult(
			long[] resultValue, long[] correctValue, String testName, String scriptCode) {

		if (Arrays.equals(resultValue, correctValue)) {
			System.out.println(testName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, correctValue, testName, scriptCode);
		}
	}

	protected void evaluateResult(
			float resultValue, float correctValue, String testName, String scriptCode) {

		if (resultValue == correctValue) {
			System.out.println(testName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, correctValue, testName, scriptCode);
		}
	}

	protected void evaluateResult(
			float[] resultValue, float[] correctValue, String testName, String scriptCode) {

		if (Arrays.equals(resultValue, correctValue)) {
			System.out.println(testName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, correctValue, testName, scriptCode);
		}
	}

	protected void evaluateResult(
			double resultValue, double correctValue, String testName, String scriptCode) {

		if (resultValue == correctValue) {
			System.out.println(testName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, correctValue, testName, scriptCode);
		}
	}

	protected void evaluateResult(
			double[] resultValue, double[] correctValue, String testName, String scriptCode) {

		if (Arrays.equals(resultValue, correctValue)) {
			System.out.println(testName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, correctValue, testName, scriptCode);
		}
	}

	protected void evaluateResult(
			boolean resultValue, boolean correctValue, String testName, String scriptCode) {

		if (resultValue == correctValue) {
			System.out.println(testName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, correctValue, testName, scriptCode);
		}
	}

	protected void evaluateResult(
			boolean[] resultValue, boolean[] correctValue, String testName, String scriptCode) {

		if (Arrays.equals(resultValue, correctValue)) {
			System.out.println(testName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, correctValue, testName, scriptCode);
		}
	}

	protected void evaluateResult(
			String resultValue, String correctValue, String testName, String scriptCode) {

		if (resultValue.equals(correctValue)) {
			System.out.println(testName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, correctValue, testName, scriptCode);
		}
	}

	protected void evaluateResult(
			String[] resultValue, String[] correctValue, String testName, String scriptCode) {

		if (Arrays.equals(resultValue, correctValue)) {
			System.out.println(testName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, correctValue, testName, scriptCode);
		}
	}
}
