package org.vcssl.nano.combinedtest;

public class CombinedTestElement {

	// Override on subclasses
	public void initialize() { };

	// Override on subclasses
	public void finalize() { };

	// Override on subclasses
	public void test() { };


	@SuppressWarnings("unused")
	private void evaluateResult(Object resultValue, Object expectedValue, String succeededTestName) {
		if (resultValue.equals(expectedValue)) {
			System.out.println(succeededTestName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, expectedValue);
		}
	}

	@SuppressWarnings("unused")
	private void evaluateResult(int resultValue, int expectedValue, String succeededTestName) {
		if (resultValue == expectedValue) {
			System.out.println(succeededTestName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, expectedValue);
		}
	}

	@SuppressWarnings("unused")
	private void evaluateResult(long resultValue, long expectedValue, String succeededTestName) {
		if (resultValue == expectedValue) {
			System.out.println(succeededTestName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, expectedValue);
		}
	}

	@SuppressWarnings("unused")
	private void evaluateResult(float resultValue, float expectedValue, String succeededTestName) {
		if (resultValue == expectedValue) {
			System.out.println(succeededTestName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, expectedValue);
		}
	}

	@SuppressWarnings("unused")
	private void evaluateResult(double resultValue, double expectedValue, String succeededTestName) {
		if (resultValue == expectedValue) {
			System.out.println(succeededTestName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, expectedValue);
		}
	}

	@SuppressWarnings("unused")
	private void evaluateResult(boolean resultValue, boolean expectedValue, String succeededTestName) {
		if (resultValue == expectedValue) {
			System.out.println(succeededTestName + ": OK.");
		} else {
			throw new CombinedTestException(resultValue, expectedValue);
		}
	}
}
