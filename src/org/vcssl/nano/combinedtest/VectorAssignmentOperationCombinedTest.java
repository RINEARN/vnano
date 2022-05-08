package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class VectorAssignmentOperationCombinedTest extends CombinedTestElement {

	VnanoEngine engine = null;

	private static final String DECLVEC_INT_A = "int a[3]; a[0]=10; a[1]=50; a[2]=30; ";
	private static final String DECLVEC_INT_B = "int b[3]; b[0]=5; b[1]=50; b[2]=10; ";
	private static final String DECLVEC_INT_C = "int c[3]; c[0]=2; c[1]=20; c[2]=32; ";
	private static final String DECLVEC_FLOAT_A = "float a[3]; a[0]=2.5; a[1]=1.25; a[2]=8.125; ";
	private static final String DECLVEC_FLOAT_B = "float b[3]; b[0]=4.25; b[1]=2.8; b[2]=12.8; ";
	private static final String DECLVEC_FLOAT_C = "float c[3]; c[0]=1.125; c[1]=32.4; c[2]=22.8; ";
	private static final String DECLVEC_BOOL_A = "bool a[3]; a[0]=true; a[1]=false; a[2]=false; ";
	private static final String DECLVEC_BOOL_B = "bool b[3]; b[0]=false; b[1]=true; b[2]=false; ";
	private static final String DECLVEC_BOOL_C = "bool c[3]; c[0]=false; c[1]=false; c[2]=true; ";
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
			this.testVectorScalarMixedOperations();
			this.testAssignmentOperationsToConstants();
			this.testAssignmentOperationsToScalarsFromArrays();
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}

	private void testAssignmentOperations() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		boolean[] resultB;
		String[] resultS;
		long[] expectedL;
		double[] expectedD;
		boolean[] expectedB;
		String[] expectedS;

		scriptCode = DECLVEC_INT_A + " int x[]; x = a; x; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[] { 10l, 50l, 30l }; // a の内容と同じ
		super.evaluateResult(resultL, expectedL, "int[] = int[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + " float x[]; x = a; x; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[] { 2.5d, 1.25d, 8.125d }; // a の内容と同じ
		super.evaluateResult(resultD, expectedD, "float[] = float[]", scriptCode);

		scriptCode = DECLVEC_BOOL_A + " bool x[]; x = a; x; ";
		resultB = (boolean[])this.engine.executeScript(scriptCode);
		expectedB = new boolean[] { true, false, false }; // a の内容と同じ
		super.evaluateResult(resultB, expectedB, "bool[] = bool[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + " string x[]; x = a; x; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[] { "abc", "def", "ghi" }; // a の内容と同じ
		super.evaluateResult(resultS, expectedS, "string[] = string[]", scriptCode);
	}

	private void testMultipleAssignmentOperations() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		boolean[] resultB;
		String[] resultS;
		long[] expectedL;
		double[] expectedD;
		boolean[] expectedB;
		String[] expectedS;

		// 代入系の演算子は右結合である事に注意

		scriptCode = DECLVEC_INT_A + DECLVEC_INT_B + DECLVEC_INT_C + " int x[]; x = a = b = c; x; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[] { 2l, 20l, 32l }; // c の内容と同じ
		super.evaluateResult(resultL, expectedL, "int[] = int[] = int[] = int[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + DECLVEC_FLOAT_B + DECLVEC_FLOAT_C + " float x[]; x = a = b = c; x; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[] { 1.125d, 32.4d, 22.8d }; // c の内容と同じ
		super.evaluateResult(resultD, expectedD, "float[] = float[] = float[] = float[]", scriptCode);

		scriptCode = DECLVEC_BOOL_A + DECLVEC_BOOL_B + DECLVEC_BOOL_C + " bool x[]; x = a = b = c; x; ";
		resultB = (boolean[])this.engine.executeScript(scriptCode);
		expectedB = new boolean[] { false, false, true }; // c の内容と同じ
		super.evaluateResult(resultB, expectedB, "bool[] = bool[] = bool[] = bool[]", scriptCode);

		scriptCode = DECLVEC_STRING_A + DECLVEC_STRING_B + DECLVEC_STRING_C + " string x[]; x = a = b = c; x; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[] { "NaCl", "H2O", "CaCO3" }; // c の内容と同じ
		super.evaluateResult(resultS, expectedS, "string[] = string[] = string[] = string[]", scriptCode);
	}

	private void testAssignmentOperationsWithTypeConversions() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		boolean[] resultB;
		String[] resultS;
		long[] expectedL;
		double[] expectedD;
		boolean[] expectedB;
		String[] expectedS;

		scriptCode = DECLVEC_FLOAT_A + " int x[]; x = a; x; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[] { 2l, 1l, 8l };
		super.evaluateResult(resultL, expectedL, "int[] = float[]", scriptCode);

		scriptCode = DECLVEC_STRING_D + " int x[]; x = d; x; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[] { 123l, 456l, 789l };
		super.evaluateResult(resultL, expectedL, "int[] = string[]", scriptCode);

		scriptCode = DECLVEC_INT_A + " float x[]; x = a; x; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[] { 10.0d, 50.0d, 30.0d };
		super.evaluateResult(resultD, expectedD, "float[] = int[]", scriptCode);

		scriptCode = DECLVEC_STRING_E + " float x[]; x = e; x; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[] { 2.125d, 2.25d, 2.5d };
		super.evaluateResult(resultD, expectedD, "float[] = string[]", scriptCode);

		scriptCode = DECLVEC_STRING_F + " bool x[]; x = f; x; ";
		resultB = (boolean[])this.engine.executeScript(scriptCode);
		expectedB = new boolean[] { true, false, true };
		super.evaluateResult(resultB, expectedB, "bool[] = string[]", scriptCode);

		scriptCode = DECLVEC_INT_A + " string x[]; x = a; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[] { "10", "50", "30" };
		super.evaluateResult(resultS, expectedS, "string[] = int[]", scriptCode);

		scriptCode = DECLVEC_FLOAT_A + " string x[]; x = a; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[] { "2.5", "1.25", "8.125" }; // 変換元が2進表現で割り切れる値なので、微小誤差は出ない
		super.evaluateResult(resultS, expectedS, "string[] = float[]", scriptCode);

		scriptCode = DECLVEC_BOOL_A + " string x[]; x = a; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[] { "true", "false", "false" };
		super.evaluateResult(resultS, expectedS, "string[] = bool[]", scriptCode);
	}

	private void testVectorScalarMixedOperations() throws VnanoException {
		String scriptCode;

		long[] resultL;
		double[] resultD;
		boolean[] resultB;
		String[] resultS;
		long[] expectedL;
		double[] expectedD;
		boolean[] expectedB;
		String[] expectedS;

		// スカラからベクトルへの代入の場合、全要素が同一のベクトルに変換されてから代入される

		scriptCode = " int x[3]; x = 123; x; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[] { 123l, 123l, 123l };
		super.evaluateResult(resultL, expectedL, "int[] = int", scriptCode);

		scriptCode = " int x[3]; x = 1.23; x; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[] { 1l, 1l, 1l };
		super.evaluateResult(resultL, expectedL, "int[] = float", scriptCode);

		scriptCode = " int x[3]; x = \"123\"; x; ";
		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[] { 123l, 123l, 123l };
		super.evaluateResult(resultL, expectedL, "int[] = string", scriptCode);

		scriptCode = " float x[3]; x = 2.125; x; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[] { 2.125d, 2.125d, 2.125d };
		super.evaluateResult(resultD, expectedD, "float[] = float", scriptCode);

		scriptCode = " float x[3]; x = 2; x; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[] { 2.0d, 2.0d, 2.0d };
		super.evaluateResult(resultD, expectedD, "float[] = int", scriptCode);

		scriptCode = " float x[3]; x = \"2.125\"; x; ";
		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[] { 2.125d, 2.125d, 2.125d };
		super.evaluateResult(resultD, expectedD, "float[] = string", scriptCode);

		scriptCode = " bool x[3]; x = true; x; ";
		resultB = (boolean[])this.engine.executeScript(scriptCode);
		expectedB = new boolean[] { true, true, true };
		super.evaluateResult(resultB, expectedB, "bool[] = bool", scriptCode);

		scriptCode = " bool x[3]; x = \"true\"; x; ";
		resultB = (boolean[])this.engine.executeScript(scriptCode);
		expectedB = new boolean[] { true, true, true };
		super.evaluateResult(resultB, expectedB, "bool[] = string", scriptCode);

		scriptCode = " string x[3]; x = \"abc\"; x; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[] { "abc", "abc", "abc" };
		super.evaluateResult(resultS, expectedS, "string[] = string", scriptCode);

		scriptCode = " string x[3]; x = 123; x; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[] { "123", "123", "123" };
		super.evaluateResult(resultS, expectedS, "string[] = int", scriptCode);

		scriptCode = " string x[3]; x = 2.125; x; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[] { "2.125", "2.125", "2.125" };
		super.evaluateResult(resultS, expectedS, "string[] = float", scriptCode);

		scriptCode = " string x[3]; x = true; x; ";
		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[] { "true", "true", "true" };
		super.evaluateResult(resultS, expectedS, "string[] = bool", scriptCode);
	}

	private void testAssignmentOperationsToConstants() throws VnanoException {

		String scriptCode;

		// const を付けて宣言された変数は書き換え不可能なはずなので検査

		// ※ ただし、現状では配列初期化子をサポートしていないので、実質的に const な配列を宣言する意味はない
		//    しかしながら、もし配列初期化子をサポートした場合は意味が出てくるので、とりあえずテストは用意しておく

		scriptCode = "const int a[3]; int b[3]; a = b;";
		try {
			this.engine.executeScript(scriptCode);

			// 例外が投げられずにここに達するのは、期待されたエラーが検出されていないので失敗
			super.missedExpectedError("const int a[3]; int b[3]; a = b; (should be failed) ", scriptCode);
		} catch (VnanoException vne) {

			// 例外が投げられればエラーが検出されているので成功
			super.succeeded("const int a[3]; int b[3]; a = b; (should be failed) ");
		}
	}

	private void testAssignmentOperationsToScalarsFromArrays() throws VnanoException {
		String scriptCode;

		// 配列からスカラへの代入は、配列の要素数が 1　の場合のみ可能で、そのテスト

		long resultL;
		double resultD;
		boolean resultB;
		String resultS;

		// 以下、右辺の要素数が 1 の場合: 成功するはず

		scriptCode = "int a[1]; a[0]=123; int s=0; s=a; s;";
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 123L, "int = int[] (size is 1) ", scriptCode);

		scriptCode = "float a[1]; a[0]=1234.5; float s=0; s=a; s;";
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 1234.5, "float = float[] (size is 1) ", scriptCode);

		scriptCode = "bool a[1]; a[0]=true; bool s=false; s=a; s;";
		resultB = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultB, true, "bool = bool[] (size is 1) ", scriptCode);

		scriptCode = "string a[1]; a[0]=\"abc\"; string s=\"\"; s=a; s;";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "abc", "string = string[] (size is 1) ", scriptCode);

		// 以下、右辺の要素数が 3 の場合: 失敗するはず

		scriptCode = "int a[3]; a[1]=123; int s=0; s=a; s;";
		try {
			resultL = (long)this.engine.executeScript(scriptCode);

			// 例外が投げられずにここに達するのは、期待されたエラーが検出されていないので失敗
			super.missedExpectedError("int = int[] (size is 3, should be failed) ", scriptCode);

		} catch (VnanoException vne) {

			// 例外が投げられればエラーが検出されているので成功
			super.succeeded("int = int[] (size is 3, should be failed) ");
		}

		scriptCode = "float a[3]; a[1]=1234.5; float s=0; s=a; s;";
		try {
			resultD = (double)this.engine.executeScript(scriptCode);

			// 例外が投げられずにここに達するのは、期待されたエラーが検出されていないので失敗
			super.missedExpectedError("float = float[] (size is 3, should be failed) ", scriptCode);

		} catch (VnanoException vne) {

			// 例外が投げられればエラーが検出されているので成功
			super.succeeded("float = float[] (size is 3, should be failed) ");
		}

		scriptCode = "bool a[3]; a[1]=true; bool s=false; s=a; s;";
		try {
			resultB = (boolean)this.engine.executeScript(scriptCode);

			// 例外が投げられずにここに達するのは、期待されたエラーが検出されていないので失敗
			super.missedExpectedError("bool = bool[] (size is 3, should be failed) ", scriptCode);

		} catch (VnanoException vne) {

			// 例外が投げられればエラーが検出されているので成功
			super.succeeded("bool = bool[] (size is 3, should be failed) ");
		}

		scriptCode = "string a[3]; a[1]=\"abc\"; string s=\"\"; s=a; s;";
		try {
			resultS = (String)this.engine.executeScript(scriptCode);

			// 例外が投げられずにここに達するのは、期待されたエラーが検出されていないので失敗
			super.missedExpectedError("string = string[] (size is 3, should be failed) ", scriptCode);

		} catch (VnanoException vne) {

			// 例外が投げられればエラーが検出されているので成功
			super.succeeded("string = string[] (size is 3, should be failed) ");
		}
	}

}
