package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class EmptyStatementCombinedTest extends CombinedTestElement {

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
			this.test();

		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}


	private void test() throws VnanoException {
		String scriptCode;

		// 空のスクリプト
		scriptCode = "" ;
		this.engine.executeScript(scriptCode);
		this.succeeded("an empty scrpt"); // 何も起こらなければOK

		// 空文
		scriptCode = ";" ;
		this.engine.executeScript(scriptCode);
		this.succeeded("an empty statement"); // 何も起こらなければOK

		// 複数の空文
		scriptCode = ";;;" ;
		this.engine.executeScript(scriptCode);
		this.succeeded("empty statements"); // 何も起こらなければOK
	}


}
