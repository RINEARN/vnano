package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class VariableDeclarationStatementCombinedTest extends CombinedTestElement {

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
			this.testScalarVariablesWithoutInitexpr();
			this.testScalarVariablesWithInitexpr();
			this.testScalarVariablesInLoopWithoutInitexpr();
			this.testScalarVariablesInLoopWithInitexpr();

			this.testArrayVariablesWithoutInitexpr();
			this.testArrayVariablesWithInitexpr();
			this.testArrayVariablesInLoopWithoutInitexpr();
			this.testArrayVariablesInLoopWithInitexpr();

		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}


	// 初期化式なしでのスカラ変数宣言
	private void testScalarVariablesWithoutInitexpr() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;
		boolean resultB;
		String resultS;

		scriptCode = "int v; v;" ;
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 0L, "int v;", scriptCode);

		scriptCode = "float v; v;" ;
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 0.0, "float v;", scriptCode);

		scriptCode = "bool v; v;" ;
		resultB = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultB, false, "bool v;", scriptCode);

		scriptCode = "string v; v;" ;
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "", "string v;", scriptCode);
	}


	// 初期化式ありでのスカラ変数宣言
	private void testScalarVariablesWithInitexpr() throws VnanoException {
		String scriptCode;
		long resultL;
		double resultD;
		boolean resultB;
		String resultS;

		scriptCode = "int v = 123; v;" ;
		resultL = (long)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, 123L, "int v = 123;", scriptCode);

		scriptCode = "float v = 2.5; v;" ; // 2進表現で割り切れる値
		resultD = (double)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, 2.5, "float v = 2.5;", scriptCode);

		scriptCode = "bool v = true; v;" ;
		resultB = (boolean)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultB, true, "bool v = true;", scriptCode);

		scriptCode = "string v = \"abc\"; v;" ;
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "abc", "string v = \"abc\";", scriptCode);
	}


	// 初期化式なしでのスカラ変数宣言、ループ内での場合
	private void testScalarVariablesInLoopWithoutInitexpr() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		boolean[] resultB;
		String[] resultS;

		scriptCode = "int history[3]; for(int i=0; i<3; i++){ int v; history[i]=v; v=-1; } history;" ;
		resultL = (long[])this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, new long[] { 0L, 0L, 0L }, "for(int i=0; i<3; i++){ int v; history[i]=v; v=-1; };", scriptCode);

		scriptCode = "float history[3]; for(int i=0; i<3; i++){ float v; history[i]=v; v=-1.0; } history;" ;
		resultD = (double[])this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, new double[] { 0.0, 0.0, 0.0 }, "for(int i=0; i<3; i++){ float v; history[i]=v; v=-1.0; };", scriptCode);

		scriptCode = "bool history[3]; for(int i=0; i<3; i++){ bool v; history[i]=v; v=true; } history;" ;
		resultB = (boolean[])this.engine.executeScript(scriptCode);
		super.evaluateResult(resultB, new boolean[] { false, false, false }, "for(int i=0; i<3; i++){ bool v; history[i]=v; v=true; };", scriptCode);

		scriptCode = "string history[3]; for(int i=0; i<3; i++){ string v; history[i]=v; v=\"updated\"; } history;" ;
		resultS = (String[])this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, new String[] { "", "", "" }, "for(int i=0; i<3; i++){ string v; history[i]=v; v=\"updated\"; };", scriptCode);
	}


	// 初期化式ありでのスカラ変数宣言、ループ内での場合
	private void testScalarVariablesInLoopWithInitexpr() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		boolean[] resultB;
		String[] resultS;

		scriptCode = "int history[3]; for(int i=0; i<3; i++){ int v=123; history[i]=v; v=-1; } history;" ;
		resultL = (long[])this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, new long[] { 123L, 123L, 123L }, "for(int i=0; i<3; i++){ int v=123; history[i]=v; v=-1; };", scriptCode);

		scriptCode = "float history[3]; for(int i=0; i<3; i++){ float v=2.5; history[i]=v; v=-1.0; } history;" ;
		resultD = (double[])this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, new double[] { 2.5, 2.5, 2.5 }, "for(int i=0; i<3; i++){ float v=2.5; history[i]=v; v=-1.0; };", scriptCode);

		scriptCode = "bool history[3]; for(int i=0; i<3; i++){ bool v=true; history[i]=v; v=false; } history;" ;
		resultB = (boolean[])this.engine.executeScript(scriptCode);
		super.evaluateResult(resultB, new boolean[] { true, true, true }, "for(int i=0; i<3; i++){ bool v=true; history[i]=v; v=false; };", scriptCode);

		scriptCode = "string history[3]; for(int i=0; i<3; i++){ string v=\"abc\"; history[i]=v; v=\"updated\"; } history;" ;
		resultS = (String[])this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, new String[] { "abc", "abc", "abc" }, "for(int i=0; i<3; i++){ string v=\"abc\"; history[i]=v; v=\"updated\"; };", scriptCode);
	}


	// 初期化式なしでの配列変数宣言
	private void testArrayVariablesWithoutInitexpr() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		boolean[] resultB;
		String[] resultS;

		scriptCode = "int v[3]; v;" ;
		resultL = (long[])this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, new long[]{ 0L, 0L, 0L }, "int v[3];", scriptCode);

		scriptCode = "float v[3]; v;" ;
		resultD = (double[])this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, new double[]{ 0.0, 0.0, 0.0 }, "float v[3];", scriptCode);

		scriptCode = "bool v[3]; v;" ;
		resultB = (boolean[])this.engine.executeScript(scriptCode);
		super.evaluateResult(resultB, new boolean[]{ false, false, false }, "bool v[3];", scriptCode);

		scriptCode = "string v[3]; v;" ;
		resultS = (String[])this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, new String[]{ "", "", "" }, "string v[3];", scriptCode);
	}


	// 初期化式ありでの配列変数宣言
	private void testArrayVariablesWithInitexpr() throws VnanoException {
		String scriptCode;
		long[] resultL;
		double[] resultD;
		boolean[] resultB;
		String[] resultS;

		scriptCode = "int v[3] = 123; v;" ;
		resultL = (long[])this.engine.executeScript(scriptCode);
		super.evaluateResult(resultL, new long[]{ 123L, 123L, 123L }, "int v[3] = 123;", scriptCode);

		scriptCode = "float v[3] = 2.5; v;" ;
		resultD = (double[])this.engine.executeScript(scriptCode);
		super.evaluateResult(resultD, new double[]{ 2.5, 2.5, 2.5 }, "float v[3] = 2.5;", scriptCode);

		scriptCode = "bool v[3] = true; v;" ;
		resultB = (boolean[])this.engine.executeScript(scriptCode);
		super.evaluateResult(resultB, new boolean[]{ true, true, true }, "bool v[3] = true;", scriptCode);

		scriptCode = "string v[3] = \"abc\"; v;" ;
		resultS = (String[])this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, new String[]{ "abc", "abc", "abc" }, "string v[3] = \"abc\";", scriptCode);
	}


	// 初期化式なしでの配列変数宣言、ループ内での場合
	private void testArrayVariablesInLoopWithoutInitexpr() throws VnanoException {
		String scriptCode;
		long[][] resultL;
		double[][] resultD;
		boolean[][] resultB;
		String[][] resultS;

		scriptCode = "int history[3][3]; for(int i=0; i<3; i++){ int v[3]; history[i][0]=v[0]; history[i][1]=v[1]; history[i][2]=v[2]; v=-1; } history;" ;
		resultL = (long[][])this.engine.executeScript(scriptCode);
		super.evaluateResult(
			resultL,
			new long[][] { {0L, 0L, 0L}, {0L, 0L, 0L}, {0L, 0L, 0L} },
			"for(int i=0; i<3; i++){ int v[3]; history[i][0]=v[0]; history[i][1]=v[1]; history[i][2]=v[2]; v=-1; };",
			scriptCode
		);

		scriptCode = "float history[3][3]; for(int i=0; i<3; i++){ float v[3]; history[i][0]=v[0]; history[i][1]=v[1]; history[i][2]=v[2]; v=-1.0; } history;" ;
		resultD = (double[][])this.engine.executeScript(scriptCode);
		super.evaluateResult(
			resultD,
			new double[][] { {0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {0.0, 0.0, 0.0} },
			"for(int i=0; i<3; i++){ float v[3]; history[i][0]=v[0]; history[i][1]=v[1]; history[i][2]=v[2]; v=-1.0; };",
			scriptCode
		);

		scriptCode = "bool history[3][3]; for(int i=0; i<3; i++){ bool v[3]; history[i][0]=v[0]; history[i][1]=v[1]; history[i][2]=v[2]; v=true; } history;" ;
		resultB = (boolean[][])this.engine.executeScript(scriptCode);
		super.evaluateResult(
			resultB,
			new boolean[][] { {false, false, false}, {false, false, false}, {false, false, false} },
			"for(int i=0; i<3; i++){ bool v[3]; history[i][0]=v[0]; history[i][1]=v[1]; history[i][2]=v[2]; v=true; };",
			scriptCode
		);

		scriptCode = "string history[3][3]; for(int i=0; i<3; i++){ string v[3]; history[i][0]=v[0]; history[i][1]=v[1]; history[i][2]=v[2]; v=\"updated\"; } history;" ;
		resultS = (String[][])this.engine.executeScript(scriptCode);
		super.evaluateResult(
			resultS,
			new String[][] { {"", "", ""}, {"", "", ""}, {"", "", ""} },
			"for(int i=0; i<3; i++){ string v[3]; history[i][0]=v[0]; history[i][1]=v[1]; history[i][2]=v[2]; v=\"updated\"; };",
			scriptCode
		);
	}


	// 初期化式ありでの配列変数宣言、ループ内での場合
	private void testArrayVariablesInLoopWithInitexpr() throws VnanoException {
		String scriptCode;
		long[][] resultL;
		double[][] resultD;
		boolean[][] resultB;
		String[][] resultS;

		scriptCode = "int history[3][3]; for(int i=0; i<3; i++){ int v[3]=123; history[i][0]=v[0]; history[i][1]=v[1]; history[i][2]=v[2]; v=-1; } history;" ;
		resultL = (long[][])this.engine.executeScript(scriptCode);
		super.evaluateResult(
			resultL,
			new long[][] { {123L, 123L, 123L}, {123L, 123L, 123L}, {123L, 123L, 123L} },
			"for(int i=0; i<3; i++){ int v[3]=123; history[i][0]=v[0]; history[i][1]=v[1]; history[i][2]=v[2]; v=-1; };",
			scriptCode
		);

		scriptCode = "float history[3][3]; for(int i=0; i<3; i++){ float v[3]=2.5; history[i][0]=v[0]; history[i][1]=v[1]; history[i][2]=v[2]; v=-1.0; } history;" ;
		resultD = (double[][])this.engine.executeScript(scriptCode);
		super.evaluateResult(
			resultD,
			new double[][] { {2.5, 2.5, 2.5}, {2.5, 2.5, 2.5}, {2.5, 2.5, 2.5} },
			"for(int i=0; i<3; i++){ float v[3]=2.5; history[i][0]=v[0]; history[i][1]=v[1]; history[i][2]=v[2]; v=-1.0; };",
			scriptCode
		);

		scriptCode = "bool history[3][3]; for(int i=0; i<3; i++){ bool v[3]=true; history[i][0]=v[0]; history[i][1]=v[1]; history[i][2]=v[2]; v=false; } history;" ;
		resultB = (boolean[][])this.engine.executeScript(scriptCode);
		super.evaluateResult(
			resultB,
			new boolean[][] { {true, true, true}, {true, true, true}, {true, true, true} },
			"for(int i=0; i<3; i++){ bool v[3]=true; history[i][0]=v[0]; history[i][1]=v[1]; history[i][2]=v[2]; v=false; };",
			scriptCode
		);

		scriptCode = "string history[3][3]; for(int i=0; i<3; i++){ string v[3]=\"abc\"; history[i][0]=v[0]; history[i][1]=v[1]; history[i][2]=v[2]; v=\"updated\"; } history;" ;
		resultS = (String[][])this.engine.executeScript(scriptCode);
		super.evaluateResult(
			resultS,
			new String[][] { {"abc", "abc", "abc"}, {"abc", "abc", "abc"}, {"abc", "abc", "abc"} },
			"for(int i=0; i<3; i++){ string v[3]=\"abc\"; history[i][0]=v[0]; history[i][1]=v[1]; history[i][2]=v[2]; v=\"updated\"; };",
			scriptCode
		);
	}

}
