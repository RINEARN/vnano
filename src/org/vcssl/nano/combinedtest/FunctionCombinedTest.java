package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class FunctionCombinedTest extends CombinedTestElement {

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
			this.testVoidArgVoidRetFunction();
			this.testSingleScalarArgFunctions();
			this.testDualScalarArgFunctions();
			this.testSingleVectorArgFunctions();
			this.testDualVectorArgFunctions();
			this.testScalarReturnFunctions();
			this.testVectorReturnFunctions();
			this.testMultiReturnFunctions();
			this.testFunctionCallsByScalarReferences();
			this.testFunctionCallsByVectorReferences();
			this.testFunctionCallsBySubscriptReferences();
			this.testSequentialFunctionCalls();
			this.testNestedFunctionCalls();
			this.testArgumentScopes();
			this.testDuplicateFunctionDeclarations();
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}


	private void testVoidArgVoidRetFunction() throws VnanoException {
		String scriptCode;
		long result;

		scriptCode =
			" int a = 0;             \n" +
			"                        \n" +
			" void fun() {           \n" +
			"     a = 2;             \n" +
			" }                      \n" +
			"                        \n" +
			" fun();                 \n" +
			" a;                     \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 2l, "int a; int fun() { a=2; } ", scriptCode);

		scriptCode =
			" int a = 0;             \n" +
			"                        \n" +
			" void fun() {           \n" +
			"     a = 2;             \n" +
			"     return;            \n" +
			" }                      \n" +
			"                        \n" +
			" fun();                 \n" +
			" a;                     \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 2l, "int a; int fun() { a=2; return; } ", scriptCode);

		scriptCode =
			" int a = 0;             \n" +
			"                        \n" +
			" void fun() {           \n" +
			"     return;            \n" +
			"     a = 2;             \n" +
			" }                      \n" +
			"                        \n" +
			" fun();                 \n" +
			" a;                     \n" ;

		result = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(result, 0l, "int a; int fun() { return; a=2; } ", scriptCode);
	}


	private void testSingleScalarArgFunctions() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;
		boolean resultB;
		String resultS;

		scriptCode =
			" int a = 0;             \n" +
			"                        \n" +
			" void fun(int x) {      \n" +
			"     a = x;             \n" +
			" }                      \n" +
			"                        \n" +
			" fun(5);                \n" +
			" a;                     \n" ;

		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 5l, "int a; void fun(int x) { a=x; } ", scriptCode);

		scriptCode =
			" float a = 0.0;         \n" +
			"                        \n" +
			" void fun(float x) {    \n" +
			"     a = x;             \n" +
			" }                      \n" +
			"                        \n" +
			" fun(32.25);            \n" +
			" a;                     \n" ;

		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 32.25d, "float a; void fun(float x) { a=x; } ", scriptCode);

		scriptCode =
			" bool a = false;        \n" +
			"                        \n" +
			" void fun(bool x) {     \n" +
			"     a = x;             \n" +
			" }                      \n" +
			"                        \n" +
			" fun(true);             \n" +
			" a;                     \n" ;

		resultB = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultB, true, "bool a; void fun(bool x) { a=x; } ", scriptCode);

		scriptCode =
			" string a = \"\";           \n" +
			"                            \n" +
			" void fun(string x) {       \n" +
			"     a = x;                 \n" +
			" }                          \n" +
			"                            \n" +
			" fun(\"abc\");              \n" +
			" a;                         \n" ;

		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "abc", "string a; fun(string x) { a=x; } ", scriptCode);
	}


	private void testDualScalarArgFunctions() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;
		boolean resultB;
		String resultS;

		scriptCode =
			" int a = 0;                \n" +
			"                           \n" +
			" void fun(int x, int y) {  \n" +
			"     a = x + y;            \n" +
			" }                         \n" +
			"                           \n" +
			" fun(5, 22);               \n" +
			" a;                        \n" ;

		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 5l+22l, "int a; void fun(int x, int y) { a=x+y; } ", scriptCode);

		scriptCode =
			" float a = 0.0;                \n" +
			"                               \n" +
			" void fun(float x, float y) {  \n" +
			"     a = x + y;                \n" +
			" }                             \n" +
			"                               \n" +
			" fun(32.25, 64.125);           \n" +
			" a;                            \n" ;

		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 32.25d+64.125d, "float a; void fun(float x, float y) { a=x+y; } ", scriptCode);

		scriptCode =
			" bool a = false;             \n" +
			"                             \n" +
			" void fun(bool x, bool y) {  \n" +
			"     a = x && y;             \n" +
			" }                           \n" +
			"                             \n" +
			" fun(true, false);           \n" +
			" a;                          \n" ;

		resultB = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultB, true&false, "bool a; void fun(bool x, bool y) { a=x&&y; } ", scriptCode);

		scriptCode =
			" bool a = false;             \n" +
			"                             \n" +
			" void fun(bool x, bool y) {  \n" +
			"     a = x || y;             \n" +
			" }                           \n" +
			"                             \n" +
			" fun(true, false);           \n" +
			" a;                          \n" ;

		resultB = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultB, true|false, "bool a; void fun(bool x, bool y) { a=x||y; } ", scriptCode);

		scriptCode =
			" string a = \"\";                \n" +
			"                                 \n" +
			" void fun(string x, string y) {  \n" +
			"     a = x+y;                    \n" +
			" }                               \n" +
			"                                 \n" +
			" fun(\"abc\", \"def\");          \n" +
			" a;                              \n" ;

		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "abc"+"def", "string a; fun(string x, string y) { a=x+y; } ", scriptCode);
	}


	private void testSingleVectorArgFunctions() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		boolean[] resultB;
		String[] resultS;
		long[] expectedL;
		double[] expectedD;
		boolean[] expectedB;
		String[] expectedS;

		scriptCode =
			" int a[3];            \n" +
			" int b[3];            \n" +
			"                      \n" +
			" a[0] = 0;            \n" +
			" a[1] = 0;            \n" +
			" a[2] = 0;            \n" +
			"                      \n" +
			" b[0] = 1;            \n" +
			" b[1] = 2;            \n" +
			" b[2] = 3;            \n" +
			"                      \n" +
			" void fun(int x[]) {  \n" +
			"     a = x;           \n" +
			" }                    \n" +
			"                      \n" +
			" fun(b);              \n" +
			" a;                   \n" ;

		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[] { 1l, 2l, 3l };
		super.evaluateResult(
			resultL, expectedL,
			"int a[3]; int b[3]; ... void fun(int x[]){ a=x; }  fun(b); ",
			scriptCode
		);

		scriptCode =
			" float a[3];            \n" +
			" float b[3];            \n" +
			"                        \n" +
			" a[0] = 0.0;            \n" +
			" a[1] = 0.0;            \n" +
			" a[2] = 0.0;            \n" +
			"                        \n" +
			" b[0] = 1.25;           \n" +
			" b[1] = 2.25;           \n" +
			" b[2] = 3.25;           \n" +
			"                        \n" +
			" void fun(float x[]) {  \n" +
			"     a = x;             \n" +
			" }                      \n" +
			"                        \n" +
			" fun(b);                \n" +
			" a;                     \n" ;

		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[] { 1.25d, 2.25d, 3.25d };
		super.evaluateResult(
			resultD, expectedD,
			"float a[3]; float b[3]; ... void fun(float x[]){ a=x; }  fun(b); ",
			scriptCode
		);

		scriptCode =
			" bool a[3];            \n" +
			" bool b[3];            \n" +
			"                       \n" +
			" a[0] = false;         \n" +
			" a[1] = false;         \n" +
			" a[2] = false;         \n" +
			"                       \n" +
			" b[0] = true;          \n" +
			" b[1] = false;         \n" +
			" b[2] = true;          \n" +
			"                       \n" +
			" void fun(bool x[]) {  \n" +
			"     a = x;            \n" +
			" }                     \n" +
			"                       \n" +
			" fun(b);               \n" +
			" a;                    \n" ;

		resultB = (boolean[])this.engine.executeScript(scriptCode);
		expectedB = new boolean[] { true, false, true };
		super.evaluateResult(
			resultB, expectedB,
			"bool a[3]; bool b[3]; ... void fun(bool x[]){ a=x; }  fun(b); ",
			scriptCode
		);

		scriptCode =
			" string a[3];            \n" +
			" string b[3];            \n" +
			"                         \n" +
			" a[0] = \"\";            \n" +
			" a[1] = \"\";            \n" +
			" a[2] = \"\";            \n" +
			"                         \n" +
			" b[0] = \"abc\";         \n" +
			" b[1] = \"def\";         \n" +
			" b[2] = \"ghi\";         \n" +
			"                         \n" +
			" void fun(string x[]) {  \n" +
			"     a = x;              \n" +
			" }                       \n" +
			"                         \n" +
			" fun(b);                 \n" +
			" a;                      \n" ;

		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[] { "abc", "def", "ghi" };
		super.evaluateResult(
			resultS, expectedS,
			"string a[3]; string b[3]; ... string fun(string x[]){ a=x; }  a = fun(b); ",
			scriptCode
		);
	}



	private void testDualVectorArgFunctions() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		boolean[] resultB;
		String[] resultS;
		long[] expectedL;
		double[] expectedD;
		boolean[] expectedB;
		String[] expectedS;

		scriptCode =
			" int a[3];                     \n" +
			" int b[3];                     \n" +
			" int c[3];                     \n" +
			"                               \n" +
			" a[0] = 0;                     \n" +
			" a[1] = 0;                     \n" +
			" a[2] = 0;                     \n" +
			"                               \n" +
			" b[0] = 1;                     \n" +
			" b[1] = 2;                     \n" +
			" b[2] = 3;                     \n" +
			"                               \n" +
			" c[0] = 4;                     \n" +
			" c[1] = 5;                     \n" +
			" c[2] = 6;                     \n" +
			"                               \n" +
			" void fun(int x[], int y[]) {  \n" +
			"     a = x + y;                \n" +
			" }                             \n" +
			"                               \n" +
			" fun(b, c);                    \n" +
			" a;                            \n" ;

		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[] { 1l+4l, 2l+5l, 3l+6l };
		super.evaluateResult(
			resultL, expectedL,
			"int a[3]; int b[3]; int c[3]; ... void fun(int x[], int y[]){ a=x+y; }  fun(b,c); ",
			scriptCode
		);

		scriptCode =
			" float a[3];                       \n" +
			" float b[3];                       \n" +
			" float c[3];                       \n" +
			"                                   \n" +
			" a[0] = 0.0;                       \n" +
			" a[1] = 0.0;                       \n" +
			" a[2] = 0.0;                       \n" +
			"                                   \n" +
			" b[0] = 1.25;                      \n" +
			" b[1] = 2.25;                      \n" +
			" b[2] = 3.25;                      \n" +
			"                                   \n" +
			" c[0] = 4.25;                      \n" +
			" c[1] = 5.25;                      \n" +
			" c[2] = 6.25;                      \n" +
			"                                   \n" +
			" void fun(float x[], float y[]) {  \n" +
			"     a = x + y;                    \n" +
			" }                                 \n" +
			"                                   \n" +
			" fun(b, c);                        \n" +
			" a;                                \n" ;

		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[] { 1.25d+4.25d, 2.25d+5.25d, 3.25d+6.25d };
		super.evaluateResult(
			resultD, expectedD,
			"float a[3]; float b[3]; float c[3]; ... void fun(float x[], float y[]){ a=x+y; }  fun(b,c); ",
			scriptCode
		);

		scriptCode =
			" bool a[3];                      \n" +
			" bool b[3];                      \n" +
			" bool c[3];                      \n" +
			"                                 \n" +
			" a[0] = false;                   \n" +
			" a[1] = false;                   \n" +
			" a[2] = false;                   \n" +
			"                                 \n" +
			" b[0] = true;                    \n" +
			" b[1] = true;                    \n" +
			" b[2] = false;                   \n" +
			"                                 \n" +
			" c[0] = false;                   \n" +
			" c[1] = true;                    \n" +
			" c[2] = true;                    \n" +
			"                                 \n" +
			" void fun(bool x[], bool y[]) {  \n" +
			"     a = x && y;                 \n" +
			" }                               \n" +
			"                                 \n" +
			" fun(b, c);                      \n" +
			" a;                              \n" ;

		resultB = (boolean[])this.engine.executeScript(scriptCode);
		expectedB = new boolean[] { true&false, true&true, false&true };
		super.evaluateResult(
			resultB, expectedB,
			"bool a[3]; bool b[3]; bool c[3]; ... void fun(bool x[], bool y[]){ a=x&&y; }  fun(b,c); ",
			scriptCode
		);

		scriptCode =
			" string a[3];                        \n" +
			" string b[3];                        \n" +
			" string c[3];                        \n" +
			"                                     \n" +
			" a[0] = \"\";                        \n" +
			" a[1] = \"\";                        \n" +
			" a[2] = \"\";                        \n" +
			"                                     \n" +
			" b[0] = \"abc\";                     \n" +
			" b[1] = \"def\";                     \n" +
			" b[2] = \"ghi\";                     \n" +
			"                                     \n" +
			" c[0] = \"aiueo\";                   \n" +
			" c[1] = \"kakikukeko\";              \n" +
			" c[2] = \"sasisuseso\";              \n" +
			"                                     \n" +
			" void fun(string x[], string y[]) {  \n" +
			"     a = x + y;                      \n" +
			" }                                   \n" +
			"                                     \n" +
			" fun(b, c);                          \n" +
			" a;                                  \n" ;

		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[] { "abc"+"aiueo", "def"+"kakikukeko", "ghi"+"sasisuseso" };
		super.evaluateResult(
			resultS, expectedS,
			"string a[3]; string b[3]; string c[3]; ... string fun(string x[], string y[]){ a=x+y; }  a = fun(b,c); ",
			scriptCode
		);
	}


	private void testScalarReturnFunctions() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;
		boolean resultB;
		String resultS;

		scriptCode =
			" int fun() {            \n" +
			"     return 2;          \n" +
			" }                      \n" +
			" fun();                 \n" ;

		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 2l, "int fun() { return 2; } ", scriptCode);

		scriptCode =
			" float fun() {          \n" +
			"     return 2.5;        \n" +
			" }                      \n" +
			" fun();                 \n" ;

		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 2.5d, "float fun() { return 2.5; } ", scriptCode);

		scriptCode =
			" bool fun() {           \n" +
			"     return true;       \n" +
			" }                      \n" +
			" fun();                 \n" ;

		resultB = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultB, true, "bool fun() { return true; } ", scriptCode);

		scriptCode =
			" string fun() {         \n" +
			"     return \"abc\";    \n" +
			" }                      \n" +
			" fun();                 \n" ;

		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "abc", "string fun() { return \"abc\"; } ", scriptCode);
	}


	private void testVectorReturnFunctions() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		boolean[] resultB;
		String[] resultS;
		long[] expectedL;
		double[] expectedD;
		boolean[] expectedB;
		String[] expectedS;

		scriptCode =
			" int[] fun() {          \n" +
			"     int a[3];          \n" +
			"     a[0] = 1;          \n" +
			"     a[1] = 2;          \n" +
			"     a[2] = 3;          \n" +
			"     return a;          \n" +
			" }                      \n" +
			" fun();                 \n" ;

		resultL = (long[])this.engine.executeScript(scriptCode);
		expectedL = new long[] { 1l, 2l, 3l };
		super.evaluateResult(resultL, expectedL, "int[] fun() { int a[3]; ... return a; } ", scriptCode);

		scriptCode =
			" float[] fun() {        \n" +
			"     float a[3];        \n" +
			"     a[0] = 1.25;       \n" +
			"     a[1] = 2.5;        \n" +
			"     a[2] = 3.125;      \n" +
			"     return a;          \n" +
			" }                      \n" +
			" fun();                 \n" ;

		resultD = (double[])this.engine.executeScript(scriptCode);
		expectedD = new double[] { 1.25d, 2.5d, 3.125d };
		super.evaluateResult(resultD, expectedD, "float[] fun() { float a[3]; ... return a; } ", scriptCode);

		scriptCode =
			" bool[] fun() {         \n" +
			"     bool a[3];         \n" +
			"     a[0] = true;       \n" +
			"     a[1] = false;      \n" +
			"     a[2] = true;       \n" +
			"     return a;          \n" +
			" }                      \n" +
			" fun();                 \n" ;

		resultB = (boolean[])this.engine.executeScript(scriptCode);
		expectedB = new boolean[] { true, false, true };
		super.evaluateResult(resultB, expectedB, "bool[] fun() { bool a[3]; ... return a; } ", scriptCode);

		scriptCode =
			" string[] fun() {       \n" +
			"     string a[3];       \n" +
			"     a[0] = \"abc\";    \n" +
			"     a[1] = \"def\";    \n" +
			"     a[2] = \"ghi\";    \n" +
			"     return a;          \n" +
			" }                      \n" +
			" fun();                 \n" ;

		resultS = (String[])this.engine.executeScript(scriptCode);
		expectedS = new String[] { "abc", "def", "ghi" };
		super.evaluateResult(resultS, expectedS, "string[] fun() { string s[3]; ... return a; } ", scriptCode);
	}


	private void testMultiReturnFunctions() throws VnanoException {
		String scriptCode;
		String resultS;

		String baseScriptCode =
			" string fun(int x) {       \n" +
			"     if (x == 1) {         \n" +
			"         return \"abc\";   \n" +
			"     } else if (x == 2) {  \n" +
			"         return \"def\";   \n" +
			"     } else {              \n" +
			"         return \"ghi\";   \n" +
			"     }                     \n" +
			" }                         \n" ;

		scriptCode = baseScriptCode + "fun(1);";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(
			resultS, "abc",
			"string fun() { if(...){return ...} else if(...){return ...} else{return ...} }   (case 1) ",
			scriptCode
		);

		scriptCode = baseScriptCode + "fun(2);";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(
			resultS, "def",
			"string fun() { if(...){return ...} else if(...){return ...} else{return ...} }   (case 2) ",
			scriptCode
		);

		scriptCode = baseScriptCode + "fun(3);";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(
			resultS, "ghi",
			"string fun() { if(...){return ...} else if(...){return ...} else{return ...} }   (case 3) ",
			scriptCode
		);
	}

	private void testFunctionCallsByScalarReferences() throws VnanoException {
		String scriptCode;
		long resultLS;
		long[] resultLV;
		long[] expectedLV;
		boolean resultBS;
		boolean[] resultBV;
		boolean[] expectedBV;

		// スカラの値渡し (call by value of a scalar)
		scriptCode =
			" void fun(int x) {         \n" +
			"     x = 2;                \n" +
			" }                         \n" +
			" int a = 0;                \n" +
			" fun(a);                   \n" +
			" a;                        \n" ;

		resultLS = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultLS, 0l, "void fun(int x) { x=2; } int a=0; fun(a);", scriptCode);

		// スカラの参照渡し (call by value of a scalar)
		// 単純な場合
		scriptCode =
			" void fun(int &x) {        \n" +
			"     x = 2;                \n" +
			" }                         \n" +
			" int a = 0;                \n" +
			" fun(a);                   \n" +
			" a;                        \n" ;

		resultLS = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultLS, 2l, "void fun(int &x) { x=2; } int a=0; fun(a);", scriptCode);

		// スカラの参照渡し (call by value of a scalar)
		// 上の例の引数の個数を増やした場合
		scriptCode =
			" void fun(int &x, int &y, int &z) {  \n" +
			"     x = 2;                          \n" +
			"     y = 3;                          \n" +
			"     z = 4;                          \n" +
			" }                                   \n" +
			"                                     \n" +
			" int a = 0;                          \n" +
			" int b = 0;                          \n" +
			" int c = 0;                          \n" +
			"                                     \n" +
			" fun(a, b, c);                       \n" +
			"                                     \n" +
			" int result[3];                      \n" +
			" result[0] = a;                      \n" +
			" result[1] = b;                      \n" +
			" result[2] = c;                        \n" +
			"                                     \n" +
			" result;                             \n" ;

		resultLV = (long[])this.engine.executeScript(scriptCode);
		expectedLV = new long[] { 2l, 3l, 4l };
		super.evaluateResult(
			resultLV, expectedLV,
			"void fun(int &x, int &y, int &z) { x=2; y=3; z=4; } int a=0; int b=0; int c=0; fun(a,b,c);",
			scriptCode
		);


		// スカラの参照渡し (call by value of a scalar)
		// グローバル変数を参照渡しし、その関数の実行中に、関数内からグローバル変数を直接書き変えた場合
		//（単純なProcessorレベルの動作の検証）
		scriptCode =
			" int a;                    \n" +
			"                           \n" +
			" bool fun(int &x) {        \n" +
			"     a = 2;                \n" +
			"     return a == x;        \n" + // a と x は等しいはず
			" }                         \n" +
			"                           \n" +
			" a = 1;                    \n" +
			" bool b = fun(a);          \n" +
			" b;                        \n" ;

		resultBS = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultBS, true, "int a=0; bool fun(int &x) { a=2; return a==x; } bool b=fun(a);", scriptCode);

		// スカラの参照渡し (call by value of a scalar)
		// 上の例の a, x を一旦変数 as, xs に控えてから比較した場合
		//（キャッシュ等のAcceleratorレベルの動作検証)
		//
		// ※ 現在の Accelerator ではアドレスベースのスカラ値キャッシュ系を採用しているので、
		//    参照経由で複数アドレス(A1,A2)にリンクされるデータはキャッシュ不可能と判定されるべきで、
		//    判定が誤っているとアドレスA1,A2間のキャッシュコヒーレンシが保てないはず。
		//    以下では、誤判定している事を想定してコヒーレンシが破れる場合を検証している。
		scriptCode =
			" int a = 0;                \n" +
			"                           \n" +
			" bool fun(int &x) {        \n" +
			"     a = 2;                \n" +
			"     int xs = x;           \n" + // Accelerator 有効時、もし x や y がキャッシュ可能と判定されていれば、
			"     int as = a;           \n" + // キャッシュ可能要素のみの演算になって未更新の値のまま代入されたりするはず。
			"     return as == xs;      \n" + // ちゃんと a と x がキャッシュ不可能と判定されていれば as と xs は等しいはず。
			" }                         \n" +
			"                           \n" + // ※ 一つ前のテストでは、どちらか片方でもキャッシュ不可能判定されていれば
			" bool r = fun(a);          \n" + //    演算「 a == x 」が、がキャッシュを活用しない演算ユニットで演算されるので、
			" r;                        \n" ; //    他方がキャッシュ可能と誤判定されていてもパスしてしまう

		resultBS = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultBS, true, "int a=0; bool fun(int &x) { a=2; int xs=x; int as=a; return as==xs; } bool r=fun(a);", scriptCode);

		// スカラの参照渡し (call by value of a scalar)
		// 上の例の引数の個数を増やした場合
		scriptCode =
			" int a = 0;                            \n" +
			" int b = 0;                            \n" +
			" int c = 0;                            \n" +
			"                                       \n" +
			" bool[] fun(int &x, int &y, int &z) {  \n" +
			"     a = 2;                            \n" +
			"     b = 3;                            \n" +
			"     c = 4;                            \n" +
			"                                       \n" +
			"     int xs = x;                       \n" +
			"     int ys = y;                       \n" +
			"     int zs = z;                       \n" +
			"                                       \n" +
			"     int as = a;                       \n" +
			"     int bs = b;                       \n" +
			"     int cs = c;                       \n" +
			"                                       \n" +
			"     bool result[3];                   \n" +
			"     result[0] = (as == xs);           \n" +
			"     result[1] = (bs == ys);           \n" +
			"     result[2] = (cs == zs);           \n" +
			"                                       \n" +
			"     return result;                    \n" +
			" }                                     \n" +
			"                                       \n" +
			" bool r[] = fun(a, b, c);              \n" +
			" r;                                    \n" ;

		resultBV = (boolean[])this.engine.executeScript(scriptCode);
		expectedBV = new boolean[] {true, true, true};
		super.evaluateResult(
			resultBV, expectedBV,
			"int a=0; ... int c=0; string fun(int &x, ... int &z) { a=2; ... int xs=x; ... int as=a; ... int xs=x; bool result[3]; result[0]=(as==xs); ... result[2]=(cs==zs); return result;} bool r[]=fun(a,b,c);",
			scriptCode
		);
	}


	private void testFunctionCallsByVectorReferences() throws VnanoException {
		String scriptCode;
		long[] resultLV;
		long[] expectedLV;
		boolean[] resultBV;
		boolean[] expectedBV;

		// ベクトルの値渡し (call by value of a vector)
		scriptCode =
			" void fun(int x[]) {       \n" +
			"     x[0] = 1;             \n" +
			"     x[1] = 2;             \n" +
			"     x[2] = 3;             \n" +
			" }                         \n" +
			"                           \n" +
			" int a[3];                 \n" +
			" a[0] = 0;                 \n" +
			" a[1] = 0;                 \n" +
			" a[2] = 0;                 \n" +
			" fun(a);                   \n" +
			" a;                        \n" ;

		resultLV = (long[])this.engine.executeScript(scriptCode);
		expectedLV = new long[] { 0l, 0l, 0l };
		super.evaluateResult(
			resultLV, expectedLV, "void fun(int x[]) { x[0]=1; x[1]=2; x[2]=3; } int a[3]; ... fun(a); ", scriptCode
		);

		// ベクトルの参照渡し (call by reference of a vector)
		// 単純な場合
		scriptCode =
			" void fun(int &x[]) {      \n" +
			"     x[0] = 1;             \n" +
			"     x[1] = 2;             \n" +
			"     x[2] = 3;             \n" +
			" }                         \n" +
			"                           \n" +
			" int a[3];                 \n" +
			" a[0] = 0;                 \n" +
			" a[1] = 0;                 \n" +
			" a[2] = 0;                 \n" +
			" fun(a);                   \n" +
			" a;                        \n" ;

		resultLV = (long[])this.engine.executeScript(scriptCode);
		expectedLV = new long[] { 1l, 2l, 3l };
		super.evaluateResult(
			resultLV, expectedLV, "void fun(int &x[]) { x[0]=1; x[1]=2; x[2]=3; } int a[3]; ... fun(a); ", scriptCode
		);


		// ベクトルの参照渡し (call by reference of a vector)
		// グローバル変数を参照渡しし、その関数の実行中に、関数内からグローバル変数を直接書き変えた場合
		scriptCode =
			" int a[3];                        \n" +
			"                                  \n" +
			" bool[] fun(int &x[]) {           \n" +
			"     a[0] = 1;                    \n" +
			"     a[1] = 2;                    \n" +
			"     a[2] = 3;                    \n" +
			"                                  \n" +
			"     bool result[] = (a==x);      \n" +
			"     return result;               \n" +
			" }                                \n" +
			"                                  \n" +
			" a[0] = 0;                        \n" +
			" a[1] = 0;                        \n" +
			" a[2] = 0;                        \n" +
			" bool r[] = fun(a);               \n" +
			" r;                               \n" ;

		resultBV = (boolean[])this.engine.executeScript(scriptCode);
		expectedBV = new boolean[] { true, true, true };
		super.evaluateResult(
			resultBV, expectedBV, "int a[3]; bool[] fun(int &x[]) { x[0]=1; ... x[2]=3; bool result[] = (a==x); return result; } bool r[]=fun(a); ", scriptCode
		);


		// ベクトルの参照渡し (call by reference of a vector)
		// 参照渡しされた配列に対して、データ領域が再確保される演算を行った場合
		scriptCode =
			" void fun(int &x[]) {             \n" +
			"     int y[4];                    \n" +
			"     y[0] = 1;                    \n" +
			"     y[1] = 2;                    \n" +
			"     y[2] = 3;                    \n" +
			"     y[3] = 4;                    \n" +
			"     x = y;                       \n" +  // 代入先と代入元の要素数が異なるので、左辺のデータ領域が再確保される
			" }                                \n" +
			"                                  \n" +
			" int a[3];                        \n" +
			" a[0] = 0;                        \n" +
			" a[1] = 0;                        \n" +
			" a[2] = 0;                        \n" +
			" fun(a);                          \n" +  // もしも上述のデータ領域再確保で参照リンクが切れていなければ、
			" a;                               \n" ;  // 関数内での x の変更が、ちゃんと呼び出し元の a に反映されているはず

		resultLV = (long[])this.engine.executeScript(scriptCode);
		expectedLV = new long[] { 1l, 2l, 3l, 4l };
		super.evaluateResult(
			resultLV, expectedLV, "void fun(int &x[]) { int y[4]; y[0]=1; ... y[3]=4; x=y; } int a[3]; a[0]=0; ... a[2]=0; fun(a); ", scriptCode
		);

		// ベクトルの参照渡し (call by reference of a vector)
		// グローバル配列を参照渡しし、その関数の実行中に、
		// 関数内からグローバル配列に対して、データ領域が再確保される演算を行った場合
		scriptCode =
			" int a[3];                        \n" +
			"                                  \n" +
			" bool[] fun(int &x[]) {           \n" +
			"     int y[4];                    \n" +
			"     y[0] = 1;                    \n" +
			"     y[1] = 2;                    \n" +
			"     y[2] = 3;                    \n" +
			"     y[3] = 4;                    \n" +
			"     a = y;                       \n" +  // 代入先と代入元の要素数が異なるので、左辺のデータ領域が再確保される
			"                                  \n" +
			"     bool result[] = (a == x);    \n" +  // もしも上述のデータ領域再確保で参照リンクが切れていなければ、
			"     return result;               \n" +  // グローバルの a の変更がちゃんと x に反映されていて、比較結果は等しいはず
			" }                                \n" +
			"                                  \n" +
			" a[0] = 0;                        \n" +
			" a[1] = 0;                        \n" +
			" a[2] = 0;                        \n" +
			" bool r[] = fun(a);               \n" +
			" r;                               \n" ;

		resultBV = (boolean[])this.engine.executeScript(scriptCode);
		expectedBV = new boolean[] { true, true, true, true };
		super.evaluateResult(
			resultLV, expectedLV, "int a[3]; bool[] fun(int &x[]) { int y[4]; y[0]=1; ... y[3]=4; a=y; bool result[]=(a==y); return result; } a[0]=0; ... a[2]=0; bool r[]=fun(a); ", scriptCode
		);
	}


	private void testFunctionCallsBySubscriptReferences() throws VnanoException {
		String scriptCode;
		long resultLS;

		// 配列要素の値渡し (call by value of an element of a vector)
		scriptCode =
			" void fun(int x) {         \n" +
			"     x = 2;                \n" +
			" }                         \n" +
			"                           \n" +
			" int a[3];                 \n" +
			" a[0] = 0;                 \n" +
			" a[1] = 0;                 \n" +
			" a[2] = 0;                 \n" +
			" fun(a[1]);                \n" +
			" a[1];                     \n" ;

		resultLS = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(
			resultLS, 0l, "void fun(int x) { x=2; } int a[3]; ... fun(a[1]); ", scriptCode
		);

		// 配列要素の参照渡し (call by reference of an element of a vector)
		scriptCode =
			" void fun(int &x) {        \n" +
			"     x = 2;                \n" +
			" }                         \n" +
			"                           \n" +
			" int a[3];                 \n" +
			" a[0] = 0;                 \n" +
			" a[1] = 0;                 \n" +
			" a[2] = 0;                 \n" +
			" fun(a[1]);                \n" +
			" a[1];                     \n" ;

		resultLS = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(
			resultLS, 2l, "void fun(int &x) { x=2; } int a[3]; ... fun(a[1]); ", scriptCode
		);
	}


	private void testSequentialFunctionCalls() throws VnanoException {
		String scriptCode;
		long resultLS;
		long[] resultLV;
		long[] expectedLV;

		scriptCode =
			" int fun1() {                                 \n" +
			"     return 1;                                \n" +
			" }                                            \n" +
			" int fun2() {                                 \n" +
			"     return 2;                                \n" +
			" }                                            \n" +
			" int fun3() {                                 \n" +
			"     return 3;                                \n" +
			" }                                            \n" +
			" int fun4() {                                 \n" +
			"     return 4;                                \n" +
			" }                                            \n" +
			" int fun5() {                                 \n" +
			"     return 5;                                \n" +
			" }                                            \n" +
			" fun1() + fun2() + fun3() + fun4() + fun5();  \n" ;

		resultLS = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(
			resultLS, 1l + 2l + 3l + 4l + 5l,
			"int fun1(){...} int fun2(){...} ... int fun5(){...}  fun1()+fun2()+fun3()+fun4()+fun5(); ",
			scriptCode
		);

		scriptCode =
			" int fun1(int x) {                                         \n" +
			"     return x + 1;                                         \n" +
			" }                                                         \n" +
			" int fun2(int x) {                                         \n" +
			"     return x * 2 - 32;                                    \n" +
			" }                                                         \n" +
			" int fun3(int x) {                                         \n" +
			"     return x * x * x;                                     \n" +
			" }                                                         \n" +
			" int fun4(int x) {                                         \n" +
			"     return x * x - x / 2;                                 \n" +
			" }                                                         \n" +
			" int fun5(int x) {                                         \n" +
			"     return x % 7;                                         \n" +
			" }                                                         \n" +
			" fun1(123) + fun2(456) + fun3(789) + fun4(10) + fun5(20);  \n" ;

		resultLS = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(
			resultLS, (123l + 1l) + (456l * 2l - 32l) + (789l * 789l * 789l) + (10l * 10l - 10l / 2l) + (20l % 7l),
			"int fun1(int x){...} int fun2(int x){...} ... int fun5(int x){...}  fun1(...)+fun2(...)+fun3(...)+fun4(...)+fun5(...); ",
			scriptCode
		);

		scriptCode =
			" int[] fun1() {                               \n" +
			"     int a[3];                                \n" +
			"     a[0] = 1;                                \n" +
			"     a[1] = 2;                                \n" +
			"     a[2] = 3;                                \n" +
			"     return a;                                \n" +
			" }                                            \n" +
			" int[] fun2() {                               \n" +
			"     int a[3];                                \n" +
			"     a[0] = 10;                               \n" +
			"     a[1] = 20;                               \n" +
			"     a[2] = 30;                               \n" +
			"     return a;                                \n" +
			" }                                            \n" +
			" int[] fun3() {                               \n" +
			"     int a[3];                                \n" +
			"     a[0] = 100;                              \n" +
			"     a[1] = 200;                              \n" +
			"     a[2] = 300;                              \n" +
			"     return a;                                \n" +
			" }                                            \n" +
			" int[] fun4() {                               \n" +
			"     int a[3];                                \n" +
			"     a[0] = 1000;                             \n" +
			"     a[1] = 2000;                             \n" +
			"     a[2] = 3000;                             \n" +
			"     return a;                                \n" +
			" }                                            \n" +
			" int[] fun5() {                               \n" +
			"     int a[3];                                \n" +
			"     a[0] = 10000;                            \n" +
			"     a[1] = 20000;                            \n" +
			"     a[2] = 30000;                            \n" +
			"     return a;                                \n" +
			" }                                            \n" +
			" fun1() + fun2() + fun3() + fun4() + fun5();  \n" ;

		resultLV = (long[])this.engine.executeScript(scriptCode);
		expectedLV = new long[] { 11111l, 22222l, 33333l };
		super.evaluateResult(
			resultLV, expectedLV,
			"int[] fun1(){...} int[] fun2(){...} ... int[] fun5(){...}  fun1()+fun2()+fun3()+fun4()+fun5(); ",
			scriptCode
		);
	}


	private void testNestedFunctionCalls() throws VnanoException {
		String scriptCode;
		long resultL;
		long expectedL;

		// この処理系では関数の再帰呼び出しをサポートしていないので（循環呼び出しも不可）、
		// ベタ書きで深いネスト or 複雑なネストの関数呼び出し状況を書いてテストする


		// 以下は直列状の深い関数ネスト
		scriptCode =
			" int fun0() {                         \n" +
			"     return fun1();                   \n" +
			" }                                    \n" +
			" int fun1() {                         \n" +
			"     return fun2();                   \n" +
			" }                                    \n" +
			" int fun2() {                         \n" +
			"     return fun3();                   \n" +
			" }                                    \n" +
			" int fun3() {                         \n" +
			"     return fun4();                   \n" +
			" }                                    \n" +
			" int fun4() {                         \n" +
			"     return fun5();                   \n" +
			" }                                    \n" +
			" int fun5() {                         \n" +
			"     return fun6();                   \n" +
			" }                                    \n" +
			" int fun6() {                         \n" +
			"     return fun7();                   \n" +
			" }                                    \n" +
			" int fun7() {                         \n" +
			"     return fun8();                   \n" +
			" }                                    \n" +
			" int fun8() {                         \n" +
			"     return fun9();                   \n" +
			" }                                    \n" +
			" int fun9() {                         \n" +
			"     return fun10();                  \n" +
			" }                                    \n" +
			" int fun10() {                        \n" +
			"     return fun11();                  \n" +
			" }                                    \n" +
			" int fun11() {                        \n" +
			"     return fun12();                  \n" +
			" }                                    \n" +
			" int fun12() {                        \n" +
			"     return fun13();                  \n" +
			" }                                    \n" +
			" int fun13() {                        \n" +
			"     return fun14();                  \n" +
			" }                                    \n" +
			" int fun14() {                        \n" +
			"     return fun15();                  \n" +
			" }                                    \n" +
			" int fun15() {                        \n" +
			"     return fun16();                  \n" +
			" }                                    \n" +
			" int fun16() {                        \n" +
			"     return fun17();                  \n" +
			" }                                    \n" +
			" int fun17() {                        \n" +
			"     return fun18();                  \n" +
			" }                                    \n" +
			" int fun18() {                        \n" +
			"     return fun19();                  \n" +
			" }                                    \n" +
			" int fun19() {                        \n" +
			"     return fun20();                  \n" +
			" }                                    \n" +
			" int fun20() {                        \n" +
			"     return 123;                      \n" +
			" }                                    \n" +
			" fun0();                              \n" ;

		resultL = (long)this.engine.executeScript(scriptCode);
		expectedL = 123l;
		super.evaluateResult(
			resultL, expectedL,
			"Nested functin calls (case 1) ",
			scriptCode
		);


		// 以下はツリー状の関数ネスト
		scriptCode =
			" int fun0() {                         \n" +
			"     return fun1() + fun2();          \n" +
			" }                                    \n" +
			"                                      \n" +
			" int fun1() {                         \n" +
			"     return fun11() + fun12();        \n" +
			" }                                    \n" +
			"                                      \n" +
			" int fun11() {                        \n" +
			"     return fun111() + fun112();      \n" +
			" }                                    \n" +
			" int fun12() {                        \n" +
			"     return fun121() + fun122();      \n" +
			" }                                    \n" +
			"                                      \n" +
			" int fun111() {                       \n" +
			"     return fun1111() + fun1112();    \n" +
			" }                                    \n" +
			" int fun112() {                       \n" +
			"     return fun1121() + fun1122();    \n" +
			" }                                    \n" +
			" int fun121() {                       \n" +
			"     return fun1211() + fun1212();    \n" +
			" }                                    \n" +
			" int fun122() {                       \n" +
			"     return fun1221() + fun1222();    \n" +
			" }                                    \n" +
			"                                      \n" +
			" int fun1111() {                      \n" +
			"     return 1111;                     \n" +
			" }                                    \n" +
			" int fun1112() {                      \n" +
			"     return 1112;                     \n" +
			" }                                    \n" +
			" int fun1121() {                      \n" +
			"     return 1121;                     \n" +
			" }                                    \n" +
			" int fun1122() {                      \n" +
			"     return 1122;                     \n" +
			" }                                    \n" +
			" int fun1211() {                      \n" +
			"     return 1211;                     \n" +
			" }                                    \n" +
			" int fun1212() {                      \n" +
			"     return 1212;                     \n" +
			" }                                    \n" +
			" int fun1221() {                      \n" +
			"     return 1221;                     \n" +
			" }                                    \n" +
			" int fun1222() {                      \n" +
			"     return 1222;                     \n" +
			" }                                    \n" +
			"                                      \n" +
			" int fun2() {                         \n" +
			"     return fun21() + fun22();        \n" +
			" }                                    \n" +
			"                                      \n" +
			" int fun21() {                        \n" +
			"     return fun211() + fun212();      \n" +
			" }                                    \n" +
			" int fun22() {                        \n" +
			"     return fun221() + fun222();      \n" +
			" }                                    \n" +
			"                                      \n" +
			" int fun211() {                       \n" +
			"     return fun2111() + fun2112();    \n" +
			" }                                    \n" +
			" int fun212() {                       \n" +
			"     return fun2121() + fun2122();    \n" +
			" }                                    \n" +
			" int fun221() {                       \n" +
			"     return fun2211() + fun2212();    \n" +
			" }                                    \n" +
			" int fun222() {                       \n" +
			"     return fun2221() + fun2222();    \n" +
			" }                                    \n" +
			"                                      \n" +
			" int fun2111() {                      \n" +
			"     return 2111;                     \n" +
			" }                                    \n" +
			" int fun2112() {                      \n" +
			"     return 2112;                     \n" +
			" }                                    \n" +
			" int fun2121() {                      \n" +
			"     return 2121;                     \n" +
			" }                                    \n" +
			" int fun2122() {                      \n" +
			"     return 2122;                     \n" +
			" }                                    \n" +
			" int fun2211() {                      \n" +
			"     return 2211;                     \n" +
			" }                                    \n" +
			" int fun2212() {                      \n" +
			"     return 2212;                     \n" +
			" }                                    \n" +
			" int fun2221() {                      \n" +
			"     return 2221;                     \n" +
			" }                                    \n" +
			" int fun2222() {                      \n" +
			"     return 2222;                     \n" +
			" }                                    \n" +
			"                                      \n" +
			" fun0();                              \n" ;

		resultL = (long)this.engine.executeScript(scriptCode);
		expectedL =
			1111l + 1112l + 1121l + 1122l +
			1211l + 1212l + 1221l + 1222l +
			2111l + 2112l + 2121l + 2122l +
			2211l + 2212l + 2221l + 2222l ;

		super.evaluateResult(
			resultL, expectedL,
			"Nested functin calls (case 2) ",
			scriptCode
		);
	}


	private void testArgumentScopes() throws VnanoException {
		String scriptCode;

		// 引数 x は関数外からはアクセスできないはず
		scriptCode =
			" void fun(int x) {  \n" +
			" }                  \n" +
			"                    \n" +
			" fun(0);            \n" + // 引数のメモリ確保処理を走らせるため
			" x = 123;           \n" ; // (一度も走っていないと、参照できてしまっていても想定と別のエラーが起きる)

		try {
			this.engine.executeScript(scriptCode);

			// 例外が投げられずにここに達するのは、期待されたエラーが検出されていないので失敗
			super.missedExpectedError("void fun(int x){ } fun(0); x=123; (should be failed) ", scriptCode);
		} catch (VnanoException vne) {

			// 例外が投げられればエラーが検出されているので成功
			super.succeeded("void fun(int x){ } fun(0); x=123; (should be failed) ");
		}
	}


	private void testDuplicateFunctionDeclarations() throws VnanoException {
		String scriptCode;

		// 同じコールシグネチャの関数は重複宣言できないはず
		scriptCode =
			" void fun(int x) {  \n" +
			" }                  \n" +
			" void fun(int x) {  \n" +
			" }                  \n" ;

		try {
			this.engine.executeScript(scriptCode);

			// 例外が投げられずにここに達するのは、期待されたエラーが検出されていないので失敗
			super.missedExpectedError("void fun(int x){ } void fun(int x){ } (should be failed) ", scriptCode);
		} catch (VnanoException vne) {

			// 例外が投げられればエラーが検出されているので成功
			super.succeeded("void fun(int x){ } void fun(int x){ } (should be failed) ");
		}


		// 名前が同じでも引数の仕様が異なれば、コールシグネチャが異なるので重複宣言できるはず
		scriptCode =
			" void fun(int x) {    \n" +
			" }                    \n" +
			" void fun(float x) {  \n" +
			" }                    \n" ;

		this.engine.executeScript(scriptCode);
		this.succeeded("void fun(int x){ } void fun(float x){ } "); // エラーにならず実行できた時点で成功
	}
}
