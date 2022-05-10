package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class LiteralCombinedTest extends CombinedTestElement {

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
			this.testIntLiterals();
			this.testFloatLiterals();
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}


	private void testFloatLiterals() throws VnanoException {
		String scriptCode;
		double resultD;

		// 符号付き指数部のテスト
		scriptCode = "1.23E-45;";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 1.23E-45, "float literal (1.23E-45)", scriptCode);

	}


	private void testIntLiterals() throws VnanoException {
		String scriptCode;
		long resultL;

		// 16進数リテラルのテスト
		scriptCode = "0x123;";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 291l, "int literal (0x123)", scriptCode);

		// 10進数リテラルのテスト
		scriptCode = "123;";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 123l, "int literal (123)", scriptCode);

		// 8進数リテラルのテスト
		scriptCode = "0o123;";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 83l, "int literal (0o123)", scriptCode);

		// 2進数リテラルのテスト
		scriptCode = "0b1010;";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 10l, "int literal (0b1010)", scriptCode);
	}
}
