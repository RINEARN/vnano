package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class VectorComparisonOperationCombinedTest extends CombinedTestElement {

	VnanoEngine engine = null;

	private static final String DECLVEC_INT_A = "int a[5]; a[0]=-100; a[1]=-10; a[2]=1; a[3]=10; a[4]=100; ";
	private static final String DECLVEC_INT_B = "int b[5]; b[0]=-200; b[1]=-2; b[2]=1; b[3]=2; b[4]=200; ";

	private static final String DECLVEC_FLOAT_A = "float a[5]; a[0]=-100.2; a[1]=-10.2; a[2]=1.0; a[3]=10.2; a[4]=100.2; ";
	private static final String DECLVEC_FLOAT_B = "float b[5]; b[0]=-200.2; b[1]=-2.2; b[2]=1.0; b[3]=2.2; b[4]=200.2; ";

	private static final String DECLVEC_BOOL_A = "bool a[5]; a[0]=true; a[1]=false; a[2]=true; a[3]=false; a[4]=true; ";
	private static final String DECLVEC_BOOL_B = "bool b[5]; b[0]=false; b[1]=false; b[2]=true; b[3]=true; b[4]=true; ";

	private static final String DECLVEC_STRING_A = "string a[5]; a[0]=\"a\"; a[1]=\"b\"; a[2]=\"1\"; a[3]=\"c\"; a[4]=\"d\"; ";
	private static final String DECLVEC_STRING_B = "string b[5]; b[0]=\"a\"; b[1]=\"i\"; b[2]=\"1.0\"; b[3]=\"u\"; b[4]=\"d\"; ";

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
			this.testEqualOperations();
			this.testNotEqualOperations();
			this.testLessThanOperations();
			this.testLessEqualOperations();
			this.testGreaterThanOperations();
			this.testGreaterEqualOperations();
			this.testVectorScalarMixedOperations();
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}

	private void testEqualOperations() throws VnanoException {
		String scriptCode;
		boolean[] result;
		boolean[] expected;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + " a == b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, false, true, false, false };
		super.evaluateResult(result, expected, "int[] == int[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + " a == b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, false, true, false, false };
		super.evaluateResult(result, expected, "float[] == float[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + " a == b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, false, true, false, false };
		super.evaluateResult(result, expected, "int[] == float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + " a == b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, false, true, false, false };
		super.evaluateResult(result, expected, "float[] == int[]", scriptCode);

		scriptCode = DECLVEC_BOOL_A + DECLVEC_BOOL_B + " a == b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, true, true, false, true };
		super.evaluateResult(result, expected, "bool[] == bool[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + " a == b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ true, false, false, false, true };
		super.evaluateResult(result, expected, "string[] == string[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_INT_B + " a == b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, false, true, false, false };
		super.evaluateResult(result, expected, "string[] == int[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_STRING_B + " a == b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, false, true, false, false };
		super.evaluateResult(result, expected, "float[] == string[]", scriptCode);
	}

	private void testNotEqualOperations() throws VnanoException {
		String scriptCode;
		boolean[] result;
		boolean[] expected;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + " a != b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ true, true, false, true, true };
		super.evaluateResult(result, expected, "int[] != int[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + " a != b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ true, true, false, true, true };
		super.evaluateResult(result, expected, "float[] != float[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + " a != b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ true, true, false, true, true };
		super.evaluateResult(result, expected, "int[] != float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + " a != b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ true, true, false, true, true };
		super.evaluateResult(result, expected, "float[] != int[]", scriptCode);

		scriptCode = DECLVEC_BOOL_A + DECLVEC_BOOL_B + " a != b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ true, false, false, true, false };
		super.evaluateResult(result, expected, "bool[] != bool[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + " a != b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, true, true, true, false };
		super.evaluateResult(result, expected, "string[] != string[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_INT_B + " a != b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ true, true, false, true, true };
		super.evaluateResult(result, expected, "string[] != int[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_STRING_B + " a != b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ true, true, false, true, true };
		super.evaluateResult(result, expected, "float[] != string[]", scriptCode);
	}

	private void testLessThanOperations() throws VnanoException {
		String scriptCode;
		boolean[] result;
		boolean[] expected;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + " a < b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, true, false, false, true };
		super.evaluateResult(result, expected, "int[] < int[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + " a < b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, true, false, false, true };
		super.evaluateResult(result, expected, "float[] < float[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + " a < b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, true, false, false, true };
		super.evaluateResult(result, expected, "int[] < float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + " a < b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, true, false, false, true };
		super.evaluateResult(result, expected, "float[] < int[]", scriptCode);
	}

	private void testLessEqualOperations() throws VnanoException {
		String scriptCode;
		boolean[] result;
		boolean[] expected;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + " a <= b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, true, true, false, true };
		super.evaluateResult(result, expected, "int[] <= int[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + " a <= b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, true, true, false, true };
		super.evaluateResult(result, expected, "float[] <= float[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + " a <= b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, true, true, false, true };
		super.evaluateResult(result, expected, "int[] <= float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + " a <= b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, true, true, false, true };
		super.evaluateResult(result, expected, "float[] <= int[]", scriptCode);
	}

	private void testGreaterThanOperations() throws VnanoException {
		String scriptCode;
		boolean[] result;
		boolean[] expected;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + " a > b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ true, false, false, true, false };
		super.evaluateResult(result, expected, "int[] > int[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + " a > b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ true, false, false, true, false };
		super.evaluateResult(result, expected, "float[] > float[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + " a > b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ true, false, false, true, false };
		super.evaluateResult(result, expected, "int[] > float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + " a > b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ true, false, false, true, false };
		super.evaluateResult(result, expected, "float[] > int[]", scriptCode);
	}

	private void testGreaterEqualOperations() throws VnanoException {
		String scriptCode;
		boolean[] result;
		boolean[] expected;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + " a >= b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ true, false, true, true, false };
		super.evaluateResult(result, expected, "int[] >= int[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + " a >= b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ true, false, true, true, false };
		super.evaluateResult(result, expected, "float[] >= float[]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + " a >= b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ true, false, true, true, false };
		super.evaluateResult(result, expected, "int[] >= float[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + " a >= b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ true, false, true, true, false };
		super.evaluateResult(result, expected, "float[] >= int[]", scriptCode);
	}

	private void testVectorScalarMixedOperations() throws VnanoException {
		String scriptCode;
		boolean[] result;
		boolean[] expected;

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + " a == 1 ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, false, true, false, false };
		super.evaluateResult(result, expected, "int[] == int", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + " 1 == b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, false, true, false, false };
		super.evaluateResult(result, expected, "int == int[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + " a == 1.0 ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, false, true, false, false };
		super.evaluateResult(result, expected, "float[] == float", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + " 1.0 == b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, false, true, false, false };
		super.evaluateResult(result, expected, "float == float[]", scriptCode);

		scriptCode = DECLVEC_BOOL_A + DECLVEC_INT_B + " a == true ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ true, false, true, false, true };
		super.evaluateResult(result, expected, "bool[] == bool", scriptCode);

		scriptCode = DECLVEC_BOOL_A + DECLVEC_BOOL_B + " true == b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, false, true, true, true };
		super.evaluateResult(result, expected, "bool == bool[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + " a == \"c\" ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, false, false, true, false };
		super.evaluateResult(result, expected, "string[] == string", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + " \"u\" == b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, false, false, true, false };
		super.evaluateResult(result, expected, "string == string[]", scriptCode);


		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + " a == 1.0 ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, false, true, false, false };
		super.evaluateResult(result, expected, "int[] == float", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + " 1.0 == b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, false, true, false, false };
		super.evaluateResult(result, expected, "float == int[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + " a == 1 ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, false, true, false, false };
		super.evaluateResult(result, expected, "float[] == int", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + " 1 == b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, false, true, false, false };
		super.evaluateResult(result, expected, "int == float[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + " a == 1 ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, false, true, false, false };
		super.evaluateResult(result, expected, "string[] == int", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + " 1 == a ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, false, true, false, false };
		super.evaluateResult(result, expected, "int == string[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + " b == 1.0 ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, false, true, false, false };
		super.evaluateResult(result, expected, "string[] == float", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + " 1.0 == b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ false, false, true, false, false };
		super.evaluateResult(result, expected, "float == string[]", scriptCode);
	}
}
