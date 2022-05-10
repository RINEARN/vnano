package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class VectorLogicalOperationCombinedTest extends CombinedTestElement {

	VnanoEngine engine = null;

	private static final String DECLVEC_A =
		"bool a[6]; a[0]=true; a[1]=true; a[2]=false; a[3]=false; a[4]=false; a[5]=false; ";

	private static final String DECLVEC_B =
		"bool b[6]; b[0]=true; b[1]=false; b[2]=true; b[3]=false; b[4]=false; b[5]=false; ";

	private static final String DECLVEC_C =
		"bool c[6]; c[0]=true; c[1]=true; c[2]=true; c[3]=true; c[4]=false; c[5]=false; ";

	private static final String DECLVEC_D =
		"bool d[6]; d[0]=true; d[1]=true; d[2]=true; d[3]=false; d[4]=true; d[5]=false; ";

	// 全要素が true の配列
	private static final String DECLVEC_T =
			"bool t[6]; t[0]=true; t[1]=true; t[2]=true; t[3]=true; t[4]=true; t[5]=true; ";

	// 全要素が false の配列
	private static final String DECLVEC_F =
			"bool f[6]; f[0]=false; f[1]=false; f[2]=false; f[3]=false; f[4]=false; f[5]=false; ";

	// 短絡評価が実行されたかどうかの確認のために上書きする配列
	private static final String DECLVEC_X =
			"bool x[6]; x[0]=false; x[1]=false; x[2]=false; x[3]=false; x[4]=false; x[5]=false; ";

	private static final boolean T = true;
	private static final boolean F = false;


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
			this.testSingleOperations();
			this.testDualOperations();
			this.testTripleOperations();
			this.testVectorScalarMixedOperations();
			this.testShortCircuitEvaluations();
		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}


	private void testSingleOperations() throws VnanoException {
		String scriptCode;
		boolean[] result;
		boolean[] expected;

		scriptCode = DECLVEC_A + DECLVEC_B + " a && b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T&T, T&F, F&T, F&F, F&F, F&F };
		super.evaluateResult(result, expected, "bool[] && bool[]", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + " a || b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T|T, T|F, F|T, F|F, F|F, F|F };
		super.evaluateResult(result, expected, "bool[] || bool[]", scriptCode);

		scriptCode = DECLVEC_A + " ! a ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ !T, !T, !F, !F, !F, !F };
		super.evaluateResult(result, expected, "! bool[]", scriptCode);
	}


	private void testDualOperations() throws VnanoException {
		String scriptCode;
		boolean[] result;
		boolean[] expected;

		scriptCode = DECLVEC_A + DECLVEC_B + DECLVEC_C + " a && b && c ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T&T&T, T&F&T, F&T&T, F&F&T, F&F&F, F&F&F };
		super.evaluateResult(result, expected, "bool[] && bool[] && bool[]", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + DECLVEC_C + " a || b || c ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T|T|T, T|F|T, F|T|T, F|F|T, F|F|F, F|F|F };
		super.evaluateResult(result, expected, "bool[] || bool[] || bool[]", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + DECLVEC_C + " a && b || c ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T&T|T, T&F|T, F&T|T, F&F|T, F&F|F, F&F|F };
		super.evaluateResult(result, expected, "bool[] && bool[] || bool[]", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + DECLVEC_C + " a || b && c ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T|T&T, T|F&T, F|T&T, F|F&T, F|F&F, F|F&F };
		super.evaluateResult(result, expected, "bool[] || bool[] && bool[]", scriptCode);

		scriptCode = DECLVEC_A + " !! a ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ !!T, !!T, !!F, !!F, !!F, !!F };
		super.evaluateResult(result, expected, "!! bool[]", scriptCode);
	}


	private void testTripleOperations() throws VnanoException {
		String scriptCode;
		boolean[] result;
		boolean[] expected;

		scriptCode = DECLVEC_A + DECLVEC_B + DECLVEC_C + DECLVEC_D + " a && b && c && d ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T&T&T&T, T&F&T&T, F&T&T&T, F&F&T&F, F&F&F&T, F&F&F&F };
		super.evaluateResult(result, expected, "bool[] && bool[] && bool[] && bool[]", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + DECLVEC_C + DECLVEC_D + " a || b || c || d ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T|T|T|T, T|F|T|T, F|T|T|T, F|F|T|F, F|F|F|T, F|F|F|F };
		super.evaluateResult(result, expected, "bool[] || bool[] || bool[] || bool[]", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + DECLVEC_C + DECLVEC_D + " a && b || c || d ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T&T|T|T, T&F|T|T, F&T|T|T, F&F|T|F, F&F|F|T, F&F|F|F };
		super.evaluateResult(result, expected, "bool[] && bool[] || bool[] || bool[]", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + DECLVEC_C + DECLVEC_D + " a || b && c || d ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T|T&T|T, T|F&T|T, F|T&T|T, F|F&T|F, F|F&F|T, F|F&F|F };
		super.evaluateResult(result, expected, "bool[] || bool[] && bool[] || bool[]", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + DECLVEC_C + DECLVEC_D + " a || b || c && d ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T|T|T&T, T|F|T&T, F|T|T&T, F|F|T&F, F|F|F&T, F|F|F&F };
		super.evaluateResult(result, expected, "bool[] || bool[] || bool[] && bool[]", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + DECLVEC_C + DECLVEC_D + " a && b && c || d ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T&T&T|T, T&F&T|T, F&T&T|T, F&F&T|F, F&F&F|T, F&F&F|F };
		super.evaluateResult(result, expected, "bool[] && bool[] && bool[] || bool[]", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + DECLVEC_C + DECLVEC_D + " a && b || c && d ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T&T|T&T, T&F|T&T, F&T|T&T, F&F|T&F, F&F|F&T, F&F|F&F };
		super.evaluateResult(result, expected, "bool[] && bool[] || bool[] && bool[]", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + DECLVEC_C + DECLVEC_D + " a || b && c && d ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T|T&T&T, T|F&T&T, F|T&T&T, F|F&T&F, F|F&F&T, F|F&F&F };
		super.evaluateResult(result, expected, "bool[] || bool[] && bool[] && bool[]", scriptCode);

		scriptCode = DECLVEC_A + " !!! a ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ !!!T, !!!T, !!!F, !!!F, !!!F, !!!F };
		super.evaluateResult(result, expected, "!!! bool[]", scriptCode);
	}


	// ベクトルとスカラの混合演算（スカラが FILL 命令でベクトルに変換されてから演算される）
	private void testVectorScalarMixedOperations() throws VnanoException {
		String scriptCode;
		boolean[] result;
		boolean[] expected;

		scriptCode = DECLVEC_A + DECLVEC_B + " a && true ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T&T, T&T, F&T, F&T, F&T, F&T };
		super.evaluateResult(result, expected, "bool[] && bool   (case 1)", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + " a && false ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T&F, T&F, F&F, F&F, F&F, F&F };
		super.evaluateResult(result, expected, "bool[] && bool   (case 2)", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + " true && b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T&T, T&F, T&T, T&F, T&F, T&F };
		super.evaluateResult(result, expected, "bool && bool[]   (case 1)", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + " false && b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ F&T, F&F, F&T, F&F, F&F, F&F };
		super.evaluateResult(result, expected, "bool && bool[]   (case 2)", scriptCode);


		scriptCode = DECLVEC_A + DECLVEC_B + " a || true ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T|T, T|T, F|T, F|T, F|T, F|T };
		super.evaluateResult(result, expected, "bool[] || bool   (case 1)", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + " a || false ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T|F, T|F, F|F, F|F, F|F, F|F };
		super.evaluateResult(result, expected, "bool[] || bool   (case 2)", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + " true || b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T|T, T|F, T|T, T|F, T|F, T|F };
		super.evaluateResult(result, expected, "bool || bool[]   (case 1)", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + " false || b ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ F|T, F|F, F|T, F|F, F|F, F|F };
		super.evaluateResult(result, expected, "bool || bool[]   (case 2)", scriptCode);


		scriptCode = DECLVEC_A + DECLVEC_B + DECLVEC_C + DECLVEC_D + " true && b && c && d ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T&T&T&T, T&F&T&T, T&T&T&T, T&F&T&F, T&F&F&T, T&F&F&F };
		super.evaluateResult(result, expected, "bool && bool[] && bool[] && bool[]", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + DECLVEC_C + DECLVEC_D + " a && true && c && d ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T&T&T&T, T&T&T&T, F&T&T&T, F&T&T&F, F&T&F&T, F&T&F&F };
		super.evaluateResult(result, expected, "bool[] && bool && bool[] && bool[]", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + DECLVEC_C + DECLVEC_D + " a && b && true && d ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T&T&T&T, T&F&T&T, F&T&T&T, F&F&T&F, F&F&T&T, F&F&T&F };
		super.evaluateResult(result, expected, "bool[] && bool[] && bool && bool[]", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + DECLVEC_C + DECLVEC_D + " a && b && c && true ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T&T&T&T, T&F&T&T, F&T&T&T, F&F&T&T, F&F&F&T, F&F&F&T };
		super.evaluateResult(result, expected, "bool[] && bool[] && bool[] && bool", scriptCode);


		scriptCode = DECLVEC_A + DECLVEC_B + DECLVEC_C + DECLVEC_D + " true || b || c || d ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T|T|T|T, T|F|T|T, T|T|T|T, T|F|T|F, T|F|F|T, T|F|F|F };
		super.evaluateResult(result, expected, "bool || bool[] || bool[] || bool[]", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + DECLVEC_C + DECLVEC_D + " a || true || c || d ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T|T|T|T, T|T|T|T, F|T|T|T, F|T|T|F, F|T|F|T, F|T|F|F };
		super.evaluateResult(result, expected, "bool[] || bool || bool[] || bool[]", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + DECLVEC_C + DECLVEC_D + " a || b || true || d ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T|T|T|T, T|F|T|T, F|T|T|T, F|F|T|F, F|F|T|T, F|F|T|F };
		super.evaluateResult(result, expected, "bool[] || bool[] || bool || bool[]", scriptCode);

		scriptCode = DECLVEC_A + DECLVEC_B + DECLVEC_C + DECLVEC_D + " a || b || c || true ; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T|T|T|T, T|F|T|T, F|T|T|T, F|F|T|T, F|F|F|T, F|F|F|T };
		super.evaluateResult(result, expected, "bool[] || bool[] || bool[] || bool", scriptCode);
	}


	// 短絡評価のテスト
	private void testShortCircuitEvaluations() throws VnanoException {

		String scriptCode;
		boolean[] result;
		boolean[] expected;

		// 左辺が全要素falseなので、&&の結果は自明に全要素falseであり、従って短絡評価が発生し、x に a は代入されないはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "f && (x=a); x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ F, F, F, F, F, F }; // x の初期値と同じ
		super.evaluateResult(result, expected, "bool[] && (bool[] = bool[])   (short-circuit evaluation occurs)", scriptCode);

		// 左辺が全要素trueなので、&&の結果は右辺に依存し、従って短絡評価は行われず、x に a が代入されるはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "t && (x=a); x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T, T, F, F, F, F }; // a の内容と同じ
		super.evaluateResult(result, expected, "bool[] && (bool[] = bool[])   (short-circuit evaluation does not occur)", scriptCode);

		// 左辺が全要素trueなので、||の結果は自明に全要素trueであり、従って短絡評価が発生し、x に a は代入されないはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "t || (x=a); x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ F, F, F, F, F, F }; // x の初期値と同じ
		super.evaluateResult(result, expected, "bool[] || (bool[] = bool[])   (short-circuit evaluation occurs)", scriptCode);

		// 左辺が全要素falseなので、||の結果は右辺に依存し、従って短絡評価は行われず、x に a が代入されるはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "f || (x=a); x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T, T, F, F, F, F }; // a の内容と同じ
		super.evaluateResult(result, expected, "bool[] || (bool[] = bool[])   (short-circuit evaluation does not occur)", scriptCode);


		// 以下、上記の複雑な組み合わせ


		// x に a は代入されないはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "(f && t) && (x=a); x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ F, F, F, F, F, F }; // x の初期値と同じ
		super.evaluateResult(result, expected, "(bool[] && bool[]) && (bool[] = bool[])   (case 1)", scriptCode);

		// x に a は代入されないはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "(f && f) && (x=a); x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ F, F, F, F, F, F }; // x の初期値と同じ
		super.evaluateResult(result, expected, "(bool[] && bool[]) && (bool[] = bool[])   (case 2)", scriptCode);

		// x に a は代入されるはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "(t && t) && (x=a); x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T, T, F, F, F, F }; // a の内容と同じ
		super.evaluateResult(result, expected, "(bool[] && bool[]) && (bool[] = bool[])   (case 3)", scriptCode);

		// x に a は代入されないはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "f && ( t && (x=a) ); x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ F, F, F, F, F, F }; // x の初期値と同じ
		super.evaluateResult(result, expected, "bool[] && ( bool[] && (bool[] = bool[]) )   (case 1)", scriptCode);

		// x に a は代入されないはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "t && ( f && (x=a) ); x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ F, F, F, F, F, F }; // x の初期値と同じ
		super.evaluateResult(result, expected, "bool[] && ( bool[] && (bool[] = bool[]) )   (case 2)", scriptCode);

		// x に a は代入されるはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "t && ( t && (x=a) ); x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T, T, F, F, F, F }; // a の内容と同じ
		super.evaluateResult(result, expected, "bool[] && ( bool[] && (bool[] = bool[]) )   (case 3)", scriptCode);

		// x に a は代入されないはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "f && ( (x=a) && t ); x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ F, F, F, F, F, F }; // x の初期値と同じ
		super.evaluateResult(result, expected, "bool[] && ( (bool[] = bool[]) && bool[] )   (case 1)", scriptCode);

		// x に a は代入されるはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "t && ( (x=a) && t ); x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T, T, F, F, F, F }; // a の内容と同じ
		super.evaluateResult(result, expected, "bool[] && ( (bool[] = bool[]) && bool[] )   (case 2)", scriptCode);

		// x に a は代入されないはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "f && t && t && (x=a) && t && t; x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ F, F, F, F, F, F }; // x の初期値と同じ
		super.evaluateResult(result, expected, "bool[] && bool[] && bool[] && (bool[] = bool[]) && bool[] && bool[]   (case 1)", scriptCode);

		// x に a は代入されないはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "t && t && f && (x=a) && t && t; x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ F, F, F, F, F, F }; // x の初期値と同じ
		super.evaluateResult(result, expected, "bool[] && bool[] && bool[] && (bool[] = bool[]) && bool[] && bool[]   (case 2)", scriptCode);

		// x に a は代入されるはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "t && t && t && (x=a) && t && t; x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T, T, F, F, F, F }; // a の内容と同じ
		super.evaluateResult(result, expected, "bool[] && bool[] && bool[] && (bool[] = bool[]) && bool[] && bool[]   (case 3)", scriptCode);

		// x に a は代入されるはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "t && t && t && (x=a) && f && t; x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T, T, F, F, F, F }; // a の内容と同じ
		super.evaluateResult(result, expected, "bool[] && bool[] && bool[] && (bool[] = bool[]) && bool[] && bool[]   (case 4)", scriptCode);


		// x に a は代入されないはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "(t || f) || (x=a); x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ F, F, F, F, F, F }; // x の初期値と同じ
		super.evaluateResult(result, expected, "(bool[] || bool[]) || (bool[] = bool[])   (case 1)", scriptCode);

		// x に a は代入されないはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "(t || t) || (x=a); x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ F, F, F, F, F, F }; // x の初期値と同じ
		super.evaluateResult(result, expected, "(bool[] || bool[]) || (bool[] = bool[])   (case 2)", scriptCode);

		// x に a は代入されるはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "(f || f) || (x=a); x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T, T, F, F, F, F }; // a の内容と同じ
		super.evaluateResult(result, expected, "(bool[] || bool[]) || (bool[] = bool[])   (case 3)", scriptCode);

		// x に a は代入されないはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "t || ( f || (x=a) ); x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ F, F, F, F, F, F }; // x の初期値と同じ
		super.evaluateResult(result, expected, "bool[] || ( bool[] || (bool[] = bool[]) )   (case 1)", scriptCode);

		// x に a は代入されないはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "f || ( t || (x=a) ); x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ F, F, F, F, F, F }; // x の初期値と同じ
		super.evaluateResult(result, expected, "bool[] || ( bool[] || (bool[] = bool[]) )   (case 2)", scriptCode);

		// x に a は代入されるはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "f || ( f || (x=a) ); x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T, T, F, F, F, F }; // a の内容と同じ
		super.evaluateResult(result, expected, "bool[] || ( bool[] || (bool[] = bool[]) )   (case 3)", scriptCode);

		// x に a は代入されないはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "t || ( (x=a) || f ); x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ F, F, F, F, F, F }; // x の初期値と同じ
		super.evaluateResult(result, expected, "bool[] || ( (bool[] = bool[]) || bool[] )   (case 1)", scriptCode);

		// x に a は代入されるはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "f || ( (x=a) || f ); x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T, T, F, F, F, F }; // a の内容と同じ
		super.evaluateResult(result, expected, "bool[] || ( (bool[] = bool[]) || bool[] )   (case 2)", scriptCode);

		// x に a は代入されないはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "t || f || f || (x=a) || f || f; x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ F, F, F, F, F, F }; // x の初期値と同じ
		super.evaluateResult(result, expected, "bool[] || bool[] || bool[] || (bool[] = bool[]) || bool[] || bool[]   (case 1)", scriptCode);

		// x に a は代入されないはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "f || f || t || (x=a) || f || f; x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ F, F, F, F, F, F }; // x の初期値と同じ
		super.evaluateResult(result, expected, "bool[] || bool[] || bool[] || (bool[] = bool[]) || bool[] || bool[]   (case 2)", scriptCode);

		// x に a は代入されるはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "f || f || f || (x=a) || f || f; x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T, T, F, F, F, F }; // a の内容と同じ
		super.evaluateResult(result, expected, "bool[] || bool[] || bool[] || (bool[] = bool[]) || bool[] || bool[]   (case 3)", scriptCode);

		// x に a は代入されるはず
		scriptCode = DECLVEC_T + DECLVEC_F + DECLVEC_A + DECLVEC_X + "f || f || f || (x=a) || t || f; x; ";
		result = (boolean[])this.engine.executeScript(scriptCode);
		expected = new boolean[]{ T, T, F, F, F, F }; // a の内容と同じ
		super.evaluateResult(result, expected, "bool[] || bool[] || bool[] || (bool[] = bool[]) || bool[] || bool[]   (case 4)", scriptCode);
	}
}
