package org.vcssl.nano.combinedtest;

@SuppressWarnings("serial")
public class CombinedTestException extends RuntimeException {
	CombinedTestException(Object resultValue, Object actualValue) {
		super(
				"The result value \"" + resultValue.toString()
				+
				"\" shoult be \"" + actualValue.toString() + "\"."
		);
	}
	CombinedTestException(int resultValue, int actualValue) {
		super(
				"The result value \"" + Integer.toString(resultValue)
				+
				"\" shoult be \"" + Integer.toString(actualValue) + "\"."
		);
	}
	CombinedTestException(long resultValue, long actualValue) {
		super(
				"The result value \"" + Long.toString(resultValue)
				+
				"\" shoult be \"" + Long.toString(actualValue) + "\"."
		);
	}
	CombinedTestException(float resultValue, float actualValue) {
		super(
				"The result value \"" + Float.toString(resultValue)
				+
				"\" shoult be \"" + Float.toString(actualValue) + "\"."
		);
	}
	CombinedTestException(double resultValue, double actualValue) {
		super(
				"The result value \"" + Double.toString(resultValue)
				+
				"\" shoult be \"" + Double.toString(actualValue) + "\"."
		);
	}
	CombinedTestException(boolean resultValue, boolean actualValue) {
		super(
				"The result value \"" + Boolean.toString(resultValue)
				+
				"\" shoult be \"" + Boolean.toString(actualValue) + "\"."
		);
	}
}
