package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;


public class RepetitiveExecutionCombinedTest extends CombinedTestElement {

	VnanoEngine engine = null;

	public class VariablePlugin {
		public int x = 2;
		public int y = 3;
	}

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

			int testId = 1;
			this.testRepetitiveExecitons("1 + 2 ;", 3, testId);

			testId = 2;
			this.testRepetitiveExecitons("1 + 2 * 3 ;", 7, testId);

			testId = 3;
			this.testRepetitiveExecitons("1 + 2 ;", 3, testId);

			testId = 4;
			this.testRepetitiveExecitons("1 + 2 * 3 ;", 7, testId);

			VariablePlugin variablePlugin = new VariablePlugin();
			engine.connectPlugin("VariablePlugin", variablePlugin);

			testId = 5;
			this.testRepetitiveExecitons("1 + x * y - 4;", 3, testId);

			variablePlugin.x = 20;
			variablePlugin.y = 30;

			testId = 7;
			this.testRepetitiveExecitons("1 + x * y - 4;", 597, testId);

			testId = 8;
			this.testRepetitiveExecitons("x * y ;", 600, testId);

		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}

	private void testRepetitiveExecitons(String scriptCode, int expectedValue, int testIdNumberInTitle)
			throws VnanoException{

		long resultL;

		for (int repetedCount=1; repetedCount<=5; repetedCount++) {
			resultL = (long)this.engine.executeScript(scriptCode);
			String title = "repetitive execition " + testIdNumberInTitle + "-" + repetedCount;
			super.evaluateResult(resultL, expectedValue, title, scriptCode);
		}
	}
}
