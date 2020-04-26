package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class SubscriptedAssignmentOperationCombinedTest extends CombinedTestElement {

	VnanoEngine engine = null;

	private static final String DECLVEC_INT_A = "int a[3]; a[0]=10; a[1]=50; a[2]=30; ";
	private static final String DECLVEC_INT_B = "int b[3]; b[0]=5; b[1]=50; b[2]=10; ";
	private static final String DECLVEC_INT_C = "int c[3]; c[0]=2; c[1]=20; c[2]=32; ";
	private static final String DECLVEC_INT_D = "int d[3]; d[0]=1; d[1]=82; d[2]=64; ";
	private static final String DECLVEC_FLOAT_A = "float a[3]; a[0]=2.5; a[1]=1.25; a[2]=8.125; ";
	private static final String DECLVEC_FLOAT_B = "float b[3]; b[0]=4.25; b[1]=2.8; b[2]=12.125; ";
	private static final String DECLVEC_FLOAT_C = "float c[3]; c[0]=1.125; c[1]=32.4; c[2]=22.8; ";
	private static final String DECLVEC_FLOAT_D = "float d[3]; d[0]=8.25; d[1]=22.8; d[2]=64.25; ";
	private static final String DECLVEC_BOOL_A = "bool a[3]; a[0]=true; a[1]=false; a[2]=false; ";
	private static final String DECLVEC_BOOL_B = "bool b[3]; b[0]=false; b[1]=true; b[2]=false; ";
	private static final String DECLVEC_BOOL_C = "bool c[3]; c[0]=false; c[1]=false; c[2]=true; ";
	private static final String DECLVEC_BOOL_D = "bool d[3]; d[0]=false; d[1]=true; d[2]=true; ";
	private static final String DECLVEC_STRING_A = "string a[3]; a[0]=\"abc\"; a[1]=\"def\"; a[2]=\"ghi\"; ";
	private static final String DECLVEC_STRING_B = "string b[3]; b[0]=\"aiueo\"; b[1]=\"kakikukeko\"; b[2]=\"sasisuseso\"; ";
	private static final String DECLVEC_STRING_C = "string c[3]; c[0]=\"NaCl\"; c[1]=\"H2O\"; c[2]=\"CaCO3\"; ";
	private static final String DECLVEC_STRING_D = "string d[3]; d[0]=\"123\"; d[1]=\"456\"; d[2]=\"789\"; ";
	private static final String DECLVEC_STRING_E = "string e[3]; e[0]=\"2.125\"; e[1]=\"2.25\"; e[2]=\"2.5\"; ";
	private static final String DECLVEC_STRING_F = "string f[3]; f[0]=\"true\"; f[1]=\"false\"; f[2]=\"true\"; ";

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

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + " a[2] = b[2]; a[2]; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 10l, "int[i] = int[i]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + " a[2] = b[2]; a[2]; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 12.125d, "float[i] = float[i]", scriptCode);

		scriptCode = DECLVEC_BOOL_A + DECLVEC_BOOL_B + " a[1] = b[1]; a[1]; ";
		resultB = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultB, true, "bool[i] = bool[i]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + " a[2] = b[2]; a[2]; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "sasisuseso", "string[i] = string[i]", scriptCode);
	}

	private void testAssignmentOperationsWithTypeConversions() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;
		boolean resultB;
		String resultS;

		scriptCode = DECLVEC_INT_A + DECLVEC_FLOAT_B + " a[2] = b[2]; a[2]; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 12l, "int[i] = float[j]", scriptCode);

		scriptCode = DECLVEC_INT_A + DECLVEC_STRING_D + " a[2] = d[2]; a[2]; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 789l, "int[i] = string[i]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_INT_B + " a[2] = b[2]; a[2]; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 10.0d, "float[i] = int[i]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_STRING_E+ " a[2] = e[2]; a[2]; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 2.5d, "float[i] = string[i]", scriptCode);


		scriptCode = DECLVEC_BOOL_A + DECLVEC_STRING_F+ " a[2] = f[2]; a[2]; ";
		resultB = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultB, true, "bool[i] = string[i] (\"true\")", scriptCode);

		scriptCode = DECLVEC_BOOL_A + DECLVEC_STRING_F+ " a[1] = f[0]; a[1]; ";
		resultB = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultB, true, "bool[i] = string[j] (\"false\")", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_INT_B+ " a[2] = b[2]; a[2]; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "10", "string = int", scriptCode);

		// b[2] は2進表現で割り切れる値である必要がある（そうでないと文字列化で10進に逆変換される際に微小誤差が入る）
		scriptCode = DECLVEC_STRING_A + DECLVEC_FLOAT_B+ " a[2] = b[2]; a[2]; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, Double.toString(12.125d), "string[i] = float[i]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_BOOL_B+ " a[2] = b[1]; a[2]; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "true", "string[i] = bool[j] (true)", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_BOOL_B+ " a[2] = b[0]; a[2]; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "false", "string[i] = bool[j] (false)", scriptCode);
	}

	private void testMultipleAssignmentOperations() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;
		boolean resultB;
		String resultS;

		// 代入系の演算子は右結合である事に注意

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + DECLVEC_INT_C + DECLVEC_INT_D
				+ " a[2] = b[2] = c[2] = d[2]; a[2]; ";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 64l, "int[i] = int[i] = int[i] = int[i]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + DECLVEC_FLOAT_C + DECLVEC_FLOAT_D
				+ " a[2] = b[2] = c[2] = d[2]; a[2]; ";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 64.25d, "float[i] = float[i] = float[i] = float[i]", scriptCode);

		scriptCode = DECLVEC_BOOL_A + DECLVEC_BOOL_B + DECLVEC_BOOL_C + DECLVEC_BOOL_D
				+ " a[2] = b[2] = c[2] = d[2]; a[2]; ";
		resultB = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultB, true, "bool[i] = bool[i] = bool[i] = bool[i] (true)", scriptCode);

		scriptCode = DECLVEC_BOOL_A + DECLVEC_BOOL_B + DECLVEC_BOOL_C + DECLVEC_BOOL_D
				+ " a[0] = b[0] = c[0] = d[0]; a[0]; ";
		resultB = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultB, false, "bool[i] = bool[i] = bool[i] = bool[i] (false)", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + DECLVEC_STRING_C + DECLVEC_STRING_D
				+ " a[2] = b[2] = c[2] = d[2]; a[2]; ";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "789", "string[i] = string[i] = string[i] = string[i]", scriptCode);
	}

	private void testAssignmentOperationsToConstants() throws VnanoException {

		String scriptCode;

		// const を付けて宣言された変数は書き換え不可能なはずなので検査

		// ※ ただし、現状では配列初期化子をサポートしていないので、実質的に const な配列を宣言する意味はない
		//    しかしながら、もし配列初期化子をサポートした場合は意味が出てくるので、とりあえずテストは用意しておく

		scriptCode = "const int a[3]; a[1] = 2;";
		try {
			this.engine.executeScript(scriptCode);

			// 例外が投げられずにここに達するのは、期待されたエラーが検出されていないので失敗
			super.missedExpectedError("const int a[3]; a[1] = 2; (should be failed) ", scriptCode);
		} catch (VnanoException vne) {

			// 例外が投げられればエラーが検出されているので成功
			super.succeeded("const int a[3]; a[1] = 2; (should be failed) ");
		}
	}

}
