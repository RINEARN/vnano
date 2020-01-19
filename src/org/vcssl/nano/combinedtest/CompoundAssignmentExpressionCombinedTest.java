package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class CompoundAssignmentExpressionCombinedTest extends CombinedTestElement {

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
			this.testAddAssignmentOperations();
			this.testSubAssignmentOperations();
			this.testMulAssignmentOperations();
			this.testDivAssignmentOperations();
			this.testRemAssignmentOperations();
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}

	private void testAddAssignmentOperations() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;
		String resultS;

		scriptCode = " int x = 123; x += 456; x; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 123l + 456l, "int += int", scriptCode);

		scriptCode = " int x = 123; x += 4.56; x; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (long)(123l + 4.56), "int += float", scriptCode);

		scriptCode = " float x = 1.25; x += 2.5; x; "; // 注：2進表現で割り切れる値が望ましい
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 1.25 + 2.5, "float += float", scriptCode);

		scriptCode = " float x = 1.25; x += 123; x; "; // 注：2進表現で割り切れる値が望ましい
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 1.25 + 123l, "float += int", scriptCode);

		scriptCode = " string x = \"abc\"; x += \"de\"; x; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "abcde", "string += string", scriptCode);
	}

	private void testSubAssignmentOperations() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;

		scriptCode = " int x = 123; x -= 456; x; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 123l - 456l, "int += int", scriptCode);

		scriptCode = " int x = 123; x -= 4.56; x; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (long)(123l - 4.56), "int += float", scriptCode);

		scriptCode = " float x = 1.25; x -= 2.5; x; "; // 注：2進表現で割り切れる値が望ましい
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 1.25 - 2.5, "float += float", scriptCode);

		scriptCode = " float x = 1.25; x -= 123; x; "; // 注：2進表現で割り切れる値が望ましい
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 1.25 - 123l, "float += int", scriptCode);
	}

	private void testMulAssignmentOperations() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;

		scriptCode = " int x = 123; x *= 456; x; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 123l * 456l, "int += int", scriptCode);

		scriptCode = " int x = 123; x *= 4.56; x; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (long)(123l * 4.56), "int += float", scriptCode);

		scriptCode = " float x = 1.25; x *= 2.5; x; "; // 注：2進表現で割り切れる値が望ましい
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 1.25 * 2.5, "float += float", scriptCode);

		scriptCode = " float x = 1.25; x *= 123; x; "; // 注：2進表現で割り切れる値が望ましい
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 1.25 * 123l, "float += int", scriptCode);
	}

	private void testDivAssignmentOperations() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;

		scriptCode = " int x = 123; x /= 2; x; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 123l / 2l, "int /= int", scriptCode);

		scriptCode = " int x = 123; x /= 2.5; x; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (long)(123l / 2.5), "int /= float", scriptCode);

		scriptCode = " float x = 1.25; x /= 2.5; x; "; // 注：2進表現で割り切れる値が望ましい
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 1.25 / 2.5, "float /= float", scriptCode);

		scriptCode = " float x = 2.5; x /= 2; x; "; // 注：2進表現で割り切れる値が望ましい
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 2.5 / 2l, "float /= int", scriptCode);
	}

	private void testRemAssignmentOperations() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;

		scriptCode = " int x = 123; x %= 2; x; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 123l % 2l, "int %= int", scriptCode);

		scriptCode = " int x = 123; x %= 100.5; x; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, (long)(123l % 100.5), "int %= float", scriptCode);

		scriptCode = " float x = 2.25; x %= 1.0; x; "; // 注：2進表現で割り切れる値が望ましい
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 2.25 % 1.0, "float %= float", scriptCode);

		scriptCode = " float x = 2.5; x %= 2; x; "; // 注：2進表現で割り切れる値が望ましい
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 2.5 % 2l, "float %= int", scriptCode);
	}

}
