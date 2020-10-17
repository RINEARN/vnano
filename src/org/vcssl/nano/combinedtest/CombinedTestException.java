package org.vcssl.nano.combinedtest;

import java.util.Arrays;

import org.vcssl.nano.VnanoException;

@SuppressWarnings("serial")
public class CombinedTestException extends RuntimeException {

	CombinedTestException(VnanoException e) {
		super(e);
	}

	CombinedTestException(String errorMessage, Throwable cause) {
		super(errorMessage);
		System.err.println("");
		System.err.println("Error occurred on the combined test:");
		System.err.println("(" + errorMessage + ")");
		System.err.println("");
		System.err.println("- Cause - ");
		System.err.println("");
		cause.printStackTrace();
	}

	CombinedTestException(String errorMessage, String testName, String scriptCode) {
		super(errorMessage);
		System.err.println("");
		System.err.println("Error occurred on the combined test of: " + testName);
		System.err.println("(" + errorMessage + ")");
		System.err.println("");
		System.err.println("- Test Script Code - ");
		System.err.println("");
		System.err.println(scriptCode);
		System.err.println("");
	}

	CombinedTestException(String resultValue, String correctValue, String testName, String scriptCode) {
		super(
				"The result value \""+ resultValue + "\" should be \"" + correctValue + "\"."
		);
		System.err.println("");
		System.err.println("Error occurred on the combined test of: " + testName);
		System.err.println("(The result value \""+ resultValue + "\" should be \"" + correctValue + "\")");
		System.err.println("");
		System.err.println("- Test Script Code - ");
		System.err.println("");
		System.err.println(scriptCode);
		System.err.println("");
	}
	CombinedTestException(String[] resultValue, String[] correctValue, String testName, String scriptCode) {
		this(Arrays.toString(resultValue), Arrays.toString(correctValue), testName, scriptCode);
	}
	CombinedTestException(int resultValue, int correctValue, String testName, String scriptCode) {
		this(Integer.toString(resultValue), Integer.toString(correctValue), testName, scriptCode);
	}
	CombinedTestException(int[] resultValue, int[] correctValue, String testName, String scriptCode) {
		this(Arrays.toString(resultValue), Arrays.toString(correctValue), testName, scriptCode);
	}
	CombinedTestException(long resultValue, long correctValue, String testName, String scriptCode) {
		this(Long.toString(resultValue), Long.toString(correctValue), testName, scriptCode);
	}
	CombinedTestException(long[] resultValue, long[] correctValue, String testName, String scriptCode) {
		this(Arrays.toString(resultValue), Arrays.toString(correctValue), testName, scriptCode);
	}
	CombinedTestException(float resultValue, float correctValue, String testName, String scriptCode) {
		this(Float.toString(resultValue), Float.toString(correctValue), testName, scriptCode);
	}
	CombinedTestException(float[] resultValue, float[] correctValue, String testName, String scriptCode) {
		this(Arrays.toString(resultValue), Arrays.toString(correctValue), testName, scriptCode);
	}
	CombinedTestException(double resultValue, double correctValue, String testName, String scriptCode) {
		this(Double.toString(resultValue), Double.toString(correctValue), testName, scriptCode);
	}
	CombinedTestException(double[] resultValue, double[] correctValue, String testName, String scriptCode) {
		this(Arrays.toString(resultValue), Arrays.toString(correctValue), testName, scriptCode);
	}
	CombinedTestException(boolean resultValue, boolean correctValue, String testName, String scriptCode) {
		this(Boolean.toString(resultValue), Boolean.toString(correctValue), testName, scriptCode);
	}
	CombinedTestException(boolean[] resultValue, boolean[] correctValue, String testName, String scriptCode) {
		this(Arrays.toString(resultValue), Arrays.toString(correctValue), testName, scriptCode);
	}
	CombinedTestException(Object resultValue, Object correctValue, String testName, String scriptCode) {
		this(resultValue.toString(), correctValue.toString(), testName, scriptCode);
	}
}
