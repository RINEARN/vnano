package org.vcssl.nano.combinedtest;

public class CombinedTestElement {

	// Override on subclasses
	public void initializeTest() { };

	// Override on subclasses
	public void finalizeTest() { };

	// Override on subclasses
	public void executeTest() { };


	@SuppressWarnings("unused")
	protected void evaluateResult(
			Object resultValue, Object correctValue, String testName, String scriptCode) {

		if (resultValue.equals(correctValue)) {
			System.out.println(testName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, correctValue, testName, scriptCode);
		}
	}

	@SuppressWarnings("unused")
	protected void evaluateResult(
			int resultValue, int correctValue, String testName, String scriptCode) {

		if (resultValue == correctValue) {
			System.out.println(testName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, correctValue, testName, scriptCode);
		}
	}

	@SuppressWarnings("unused")
	protected void evaluateResult(
			long resultValue, long correctValue, String testName, String scriptCode) {

		if (resultValue == correctValue) {
			System.out.println(testName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, correctValue, testName, scriptCode);
		}
	}

	@SuppressWarnings("unused")
	protected void evaluateResult(
			float resultValue, float correctValue, String testName, String scriptCode) {

		if (resultValue == correctValue) {
			System.out.println(testName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, correctValue, testName, scriptCode);
		}
	}

	@SuppressWarnings("unused")
	protected void evaluateResult(
			double resultValue, double correctValue, String testName, String scriptCode) {

		if (resultValue == correctValue) {
			System.out.println(testName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, correctValue, testName, scriptCode);
		}
	}

	@SuppressWarnings("unused")
	protected void evaluateResult(
			boolean resultValue, boolean correctValue, String testName, String scriptCode) {

		if (resultValue == correctValue) {
			System.out.println(testName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, correctValue, testName, scriptCode);
		}
	}
}
