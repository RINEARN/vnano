package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class ScalarAssignmentOperationCombinedTest extends CombinedTestElement {

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
			this.testAssignmentOperations();
			this.testMultipleAssignmentOperations();
			this.testAssignmentOperationsWithTypeConversions();
			this.testAssignmentOperationsToConstants();
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}

	private void testAssignmentOperations() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;
		boolean resultB;
		String resultS;

		scriptCode = " int x; x = 123; x; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 123l, "int = int", scriptCode);

		scriptCode = " float x; x = 1.25; x; "; // 注：2進表現で割り切れる値が望ましい
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 1.25, "float = float", scriptCode);

		scriptCode = " bool x; x = true; x; ";
		resultB = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultB, true, "bool = bool", scriptCode);

		scriptCode = " string x; x = \"abc\"; x; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "abc", "string = string", scriptCode);


		scriptCode = " int x = 123; x; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 123l, "int = int (init in variable declaration)", scriptCode);

		scriptCode = " float x = 1.25; x; "; // 注：2進表現で割り切れる値が望ましい
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 1.25, "float = float (init in variable declaration)", scriptCode);

		scriptCode = " bool x = true; x; ";
		resultB = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultB, true, "bool = bool (init in variable declaration)", scriptCode);

		scriptCode = " string x; x = \"abc\"; x; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "abc", "string = string (init in variable declaration)", scriptCode);
	}

	private void testAssignmentOperationsWithTypeConversions() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;
		boolean resultB;
		String resultS;

		scriptCode = " int x; x = 1.25; x; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 1l, "int = float", scriptCode);

		scriptCode = " int x; x = \"123\"; x; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 123l, "int = string", scriptCode);

		scriptCode = " float x; x = 123; x; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 123.0, "float = int", scriptCode);

		scriptCode = " float x; x = \"1.25\"; x; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 1.25, "float = string", scriptCode);

		scriptCode = " bool x=false; x = \"true\"; x; ";
		resultB = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultB, true, "bool = string (\"true\")", scriptCode);

		scriptCode = " bool x=true; x = \"false\"; x; ";
		resultB = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultB, false, "bool = string (\"false\")", scriptCode);

		scriptCode = " string x; x = 123; x; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "123", "string = int", scriptCode);

		scriptCode = " string x; x = 1.25; x; "; // 注：2進表現で割り切れる値が望ましい
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, Double.toString(1.25), "string = float", scriptCode);

		scriptCode = " string x; x = true; x; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "true", "string = bool (true)", scriptCode);

		scriptCode = " string x; x = false; x; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "false", "string = bool (false)", scriptCode);
	}

	private void testMultipleAssignmentOperations() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;
		boolean resultB;
		String resultS;

		// 代入系の演算子は右結合である事に注意

		scriptCode = " int x; int a=123; int b=456; int c=789; x = a = b = c; x; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 789l, "int = int = int = int", scriptCode);

		scriptCode = " float x; float a=1.23; float b=4.56; float c=7.89; x = a = b = c; x; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 7.89, "float = float = float = float", scriptCode);

		scriptCode = " bool x; bool a=false; bool b=false; bool c=true; x = a = b = c; x; ";
		resultB = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultB, true, "bool = bool = bool = bool", scriptCode);

		scriptCode = " string x; string a=\"abc\"; string b=\"def\"; string c=\"ghi\"; x = a = b = c; x; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "ghi", "string = string = string = string", scriptCode);
	}

	private void testAssignmentOperationsToConstants() throws VnanoException {
		String scriptCode;

		// 宣言文での代入は初期化子扱いなので可能なはず
		scriptCode = "const int x = 123;";
		this.engine.executeScript(scriptCode);
		this.succeeded("const int x = 123; "); // エラーにならず実行できた時点で成功

		// 宣言文以外での代入は不可能なはず
		scriptCode = "const int x; x = 123;";
		try {
			this.engine.executeScript(scriptCode);

			// 例外が投げられずにここに達するのは、期待されたエラーが検出されていないので失敗
			super.missedExpectedError("const int x; x = 123; (should be failed) ", scriptCode);
		} catch (VnanoException vne) {

			// 例外が投げられればエラーが検出されているので成功
			super.succeeded("const int x; x = 123; (should be failed) ");
		}
	}

}
