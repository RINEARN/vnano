package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class ScalarComparisonOperationCombinedTest extends CombinedTestElement {

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
			this.testEqualOperations();
			this.testNotEqualOperations();
			this.testLessThanOperations();
			this.testLessEqualOperations();
			this.testGreaterThanOperations();
			this.testGreaterEqualOperations();
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}

	@SuppressWarnings("all")
	private void testEqualOperations() throws VnanoException {
		String scriptCode;
		boolean result;

		scriptCode = " 1 == 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "int == int (same values)", scriptCode);

		scriptCode = " 1 == 2 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "int == int (different values)", scriptCode);

		scriptCode = " 1 == 1.0 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "int == float (same values)", scriptCode);

		scriptCode = " 1 == 1.25 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "int == float (different values)", scriptCode);

		scriptCode = " 1 == \"1\" ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "int == string (same values)", scriptCode);

		scriptCode = " 1 == \"2\" ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "int == string (different values)", scriptCode);


		scriptCode = " 1.5 == 1.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "float == float (same values)", scriptCode);

		scriptCode = " 1.5 == 2.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "float == float (different values)", scriptCode);

		scriptCode = " 1.0 == 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "float == int (same values)", scriptCode);

		scriptCode = " 1.25 == 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "float == int (different values)", scriptCode);

		scriptCode = " 1.5 == \"1.5\" ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "float == string (same values)", scriptCode);

		scriptCode = " 1.5 == \"2.5\" ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "float == string (different values)", scriptCode);


		scriptCode = " true == true ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "bool == bool (same values)", scriptCode);

		scriptCode = " true == false ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "bool == bool (different values)", scriptCode);

		scriptCode = " true == \"true\" ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "bool == string (same values)", scriptCode);

		scriptCode = " true == \"false\" ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "bool == string (different values)", scriptCode);


		scriptCode = " \"abc\" == \"abc\" ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "string == string (same values)", scriptCode);

		scriptCode = " \"abc\" == \"def\" ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "string == string (different values)", scriptCode);

		scriptCode = " \"1\" == 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "string == int (same values)", scriptCode);

		scriptCode = " \"2\" == 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "string == int (different values)", scriptCode);

		scriptCode = " \"1.5\" == 1.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "string == float (same values)", scriptCode);

		scriptCode = " \"2.5\" == 1.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "string == int (different values)", scriptCode);

		scriptCode = " \"true\" == true ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "string == bool (same values)", scriptCode);

		scriptCode = " \"false\" == true ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "string == bool (different values)", scriptCode);
	}


	@SuppressWarnings("all")
	private void testNotEqualOperations() throws VnanoException {
		String scriptCode;
		boolean result;

		scriptCode = " 1 != 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "int != int (same values)", scriptCode);

		scriptCode = " 1 != 2 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "int != int (different values)", scriptCode);

		scriptCode = " 1 != 1.0 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "int != float (same values)", scriptCode);

		scriptCode = " 1 != 1.25 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "int != float (different values)", scriptCode);

		scriptCode = " 1 != \"1\" ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "int != string (same values)", scriptCode);

		scriptCode = " 1 != \"2\" ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "int != string (different values)", scriptCode);


		scriptCode = " 1.5 != 1.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "float != float (same values)", scriptCode);

		scriptCode = " 1.5 != 2.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "float != float (different values)", scriptCode);

		scriptCode = " 1.0 != 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "float != int (same values)", scriptCode);

		scriptCode = " 1.25 != 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "float != int (different values)", scriptCode);

		scriptCode = " 1.5 != \"1.5\" ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "float != string (same values)", scriptCode);

		scriptCode = " 1.5 != \"2.5\" ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "float != string (different values)", scriptCode);


		scriptCode = " true != true ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "bool != bool (same values)", scriptCode);

		scriptCode = " true != false ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "bool != bool (different values)", scriptCode);

		scriptCode = " true != \"true\" ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "bool != string (same values)", scriptCode);

		scriptCode = " true != \"false\" ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "bool != string (different values)", scriptCode);


		scriptCode = " \"abc\" != \"abc\" ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "string != string (same values)", scriptCode);

		scriptCode = " \"abc\" != \"def\" ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "string != string (different values)", scriptCode);

		scriptCode = " \"1\" != 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "string != int (same values)", scriptCode);

		scriptCode = " \"2\" != 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "string != int (different values)", scriptCode);

		scriptCode = " \"1.5\" != 1.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "string != float (same values)", scriptCode);

		scriptCode = " \"2.5\" != 1.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "string != int (different values)", scriptCode);

		scriptCode = " \"true\" != true ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "string != bool (same values)", scriptCode);

		scriptCode = " \"false\" != true ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "string != bool (different values)", scriptCode);
	}


	@SuppressWarnings("all")
	private void testLessThanOperations() throws VnanoException {
		String scriptCode;
		boolean result;

		scriptCode = " 1 < 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "int < int (same values)", scriptCode);

		scriptCode = " 1 < 2 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "int < int (different values, right is greater)", scriptCode);

		scriptCode = " 2 < 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "int < int (different values, left is greater)", scriptCode);


		scriptCode = " 1.5 < 1.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "float < float (same values)", scriptCode);

		scriptCode = " 1.5 < 2.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "float < float (different values, right is greater)", scriptCode);

		scriptCode = " 2.5 < 1.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "float < float (different values, left is greater)", scriptCode);


		scriptCode = " 1 < 1.0 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "int < float (same values)", scriptCode);

		scriptCode = " 1 < 2.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "int < float (different values, right is greater)", scriptCode);

		scriptCode = " 2 < 1.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "int < float (different values, left is greater)", scriptCode);


		scriptCode = " 1.0 < 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "float < int (same values)", scriptCode);

		scriptCode = " 1.5 < 2 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "float < int (different values, right is greater)", scriptCode);

		scriptCode = " 2.5 < 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "float < int (different values, left is greater)", scriptCode);
	}


	@SuppressWarnings("all")
	private void testLessEqualOperations() throws VnanoException {
		String scriptCode;
		boolean result;

		scriptCode = " 1 <= 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "int <= int (same values)", scriptCode);

		scriptCode = " 1 <= 2 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "int <= int (different values, right is greater)", scriptCode);

		scriptCode = " 2 <= 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "int <= int (different values, left is greater)", scriptCode);


		scriptCode = " 1.5 <= 1.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "float <= float (same values)", scriptCode);

		scriptCode = " 1.5 <= 2.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "float <= float (different values, right is greater)", scriptCode);

		scriptCode = " 2.5 <= 1.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "float <= float (different values, left is greater)", scriptCode);


		scriptCode = " 1 <= 1.0 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "int <= float (same values)", scriptCode);

		scriptCode = " 1 <= 2.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "int <= float (different values, right is greater)", scriptCode);

		scriptCode = " 2 <= 1.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "int <= float (different values, left is greater)", scriptCode);


		scriptCode = " 1.0 <= 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "float <= int (same values)", scriptCode);

		scriptCode = " 1.5 <= 2 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "float <= int (different values, right is greater)", scriptCode);

		scriptCode = " 2.5 <= 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "float <= int (different values, left is greater)", scriptCode);
	}


	@SuppressWarnings("all")
	private void testGreaterThanOperations() throws VnanoException {
		String scriptCode;
		boolean result;

		scriptCode = " 1 > 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "int > int (same values)", scriptCode);

		scriptCode = " 1 > 2 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "int > int (different values, right is greater)", scriptCode);

		scriptCode = " 2 > 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "int > int (different values, left is greater)", scriptCode);


		scriptCode = " 1.5 > 1.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "float > float (same values)", scriptCode);

		scriptCode = " 1.5 > 2.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "float > float (different values, right is greater)", scriptCode);

		scriptCode = " 2.5 > 1.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "float > float (different values, left is greater)", scriptCode);


		scriptCode = " 1 > 1.0 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "int > float (same values)", scriptCode);

		scriptCode = " 1 > 2.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "int > float (different values, right is greater)", scriptCode);

		scriptCode = " 2 > 1.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "int > float (different values, left is greater)", scriptCode);


		scriptCode = " 1.0 > 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "float > int (same values)", scriptCode);

		scriptCode = " 1.5 > 2 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "float > int (different values, right is greater)", scriptCode);

		scriptCode = " 2.5 > 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "float > int (different values, left is greater)", scriptCode);
	}


	@SuppressWarnings("all")
	private void testGreaterEqualOperations() throws VnanoException {
		String scriptCode;
		boolean result;

		scriptCode = " 1 >= 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "int > int (same values)", scriptCode);

		scriptCode = " 1 >= 2 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "int > int (different values, right is greater)", scriptCode);

		scriptCode = " 2 >= 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "int > int (different values, left is greater)", scriptCode);


		scriptCode = " 1.5 >= 1.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "float > float (same values)", scriptCode);

		scriptCode = " 1.5 >= 2.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "float > float (different values, right is greater)", scriptCode);

		scriptCode = " 2.5 >= 1.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "float > float (different values, left is greater)", scriptCode);


		scriptCode = " 1 >= 1.0 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "int > float (same values)", scriptCode);

		scriptCode = " 1 >= 2.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "int > float (different values, right is greater)", scriptCode);

		scriptCode = " 2 >= 1.5 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "int > float (different values, left is greater)", scriptCode);


		scriptCode = " 1.0 >= 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "float > int (same values)", scriptCode);

		scriptCode = " 1.5 >= 2 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, false, "float > int (different values, right is greater)", scriptCode);

		scriptCode = " 2.5 >= 1 ; ";
		result = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, true, "float > int (different values, left is greater)", scriptCode);
	}

}
