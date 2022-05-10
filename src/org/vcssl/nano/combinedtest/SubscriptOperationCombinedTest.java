package org.vcssl.nano.combinedtest;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class SubscriptOperationCombinedTest extends CombinedTestElement {

	VnanoEngine engine = null;
	ArrayPlugin arrayPlugin = null;

	// スクリプト内からアクセス可能な配列を提供するプラグイン
	//（この配列データを読み書きして値や位置の整合性などを検証する）
	public class ArrayPlugin {
		public long[]    i1d; // int array 1D
		public double[]  f1d; // float array 1D
		public boolean[] b1d; // bool array 1D
		public String[]  s1d; // string array 1D

		public long[][]    i2d; // int array 2D
		public double[][]  f2d; // float array 2D
		public boolean[][] b2d; // bool array 2D
		public String[][]  s2d; // string array 2D

		public long[][][]    i3d; // int array 3D
		public double[][][]  f3d; // float array 3D
		public boolean[][][] b3d; // bool array 3D
		public String[][][]  s3d; // string array 3D

		public ArrayPlugin () {
			this.i1d = new long[] { 9000, 9001, 9002, 9003 };
			this.f1d = new double[] { 9000.5, 9001.5, 9002.5, 9003.5 };
			this.b1d = new boolean[] { true, false, true, false };
			this.s1d = new String[] { "a0", "a1", "a2", "a3" };

			this.i2d = new long[][] {
				new long[] { 9000, 9001, 9002, 9003 },
				new long[] { 9010, 9011, 9012, 9013 },
				new long[] { 9020, 9021, 9022, 9023 }
			};
			this.f2d = new double[][] {
				new double[] { 9000.5, 9001.5, 9002.5, 9003.5 },
				new double[] { 9010.5, 9011.5, 9012.5, 9013.5 },
				new double[] { 9020.5, 9021.5, 9022.5, 9023.5 }
			};
			this.b2d = new boolean[][] {
				new boolean[] { true, false, true, false },
				new boolean[] { false, true, false, true },
				new boolean[] { true, true, false, false }
			};
			this.s2d = new String[][] {
				new String[] { "a0", "a1", "a2", "a3" },
				new String[] { "b0", "b1", "b2", "b3" },
				new String[] { "c0", "c1", "c2", "c3" }
			};

			this.i3d = new long[][][] {
				new long[][] {
					new long[] { 9000, 9001, 9002, 9003 },
					new long[] { 9010, 9011, 9012, 9013 },
					new long[] { 9020, 9021, 9022, 9023 }
				},
				new long[][] {
					new long[] { 9100, 9101, 9102, 9103 },
					new long[] { 9110, 9111, 9112, 9113 },
					new long[] { 9120, 9121, 9122, 9123 }
				}
			};

			this.f3d = new double[][][] {
				new double[][] {
					new double[] { 9000.5, 9001.5, 9002.5, 9003.5 },
					new double[] { 9010.5, 9011.5, 9012.5, 9013.5 },
					new double[] { 9020.5, 9021.5, 9022.5, 9023.5 }
				},
				new double[][] {
					new double[] { 9100.5, 9101.5, 9102.5, 9103.5 },
					new double[] { 9110.5, 9111.5, 9112.5, 9113.5 },
					new double[] { 9120.5, 9121.5, 9122.5, 9123.5 }
				}
			};

			this.b3d = new boolean[][][] {
				new boolean[][] {
					new boolean[] { true, false, true, false },
					new boolean[] { false, true, false, true },
					new boolean[] { true, true, false, false }
				},
				new boolean[][] {
					new boolean[] { false, false, true, true },
					new boolean[] { true, true, true, true },
					new boolean[] { false, false, false, false }
				}
			};

			this.s3d = new String[][][] {
				new String[][] {
					new String[] { "a0", "a1", "a2", "a3" },
					new String[] { "b0", "b1", "b2", "b3" },
					new String[] { "c0", "c1", "c2", "c3" }
				},
				new String[][] {
					new String[] { "d0", "d1", "d2", "d3" },
					new String[] { "e0", "e1", "e2", "e3" },
					new String[] { "f0", "f1", "f2", "f3" }
				}
			};

		}
	}

	@Override
	public void initializeTest(VnanoEngine engine) {
		this.engine = engine;
		this.arrayPlugin = new ArrayPlugin();
		try {
			this.engine.connectPlugin("ArrayPlugin", arrayPlugin);
		} catch (VnanoException e) {
			throw new CombinedTestException("Unexpected exception occurred", e);
		}
	}

	@Override
	public void finalizeTest() {
		try {
			this.engine.disconnectAllPlugins();
		} catch (VnanoException e) {
			throw new CombinedTestException("Unexpected exception occurred", e);
		}
		this.engine = null;
	}

	@Override
	public void executeTest() {
		try {
			this.testReadInt1D();
			this.testReadFloat1D();
			this.testReadBool1D();
			this.testReadString1D();

			this.testWriteInt1D();
			this.testWriteFloat1D();
			this.testWriteBool1D();
			this.testWriteString1D();

			this.testReadInt2D();
			this.testReadFloat2D();
			this.testReadBool2D();
			this.testReadString2D();

			this.testWriteInt2D();
			this.testWriteFloat2D();
			this.testWriteBool2D();
			this.testWriteString2D();

			this.testReadInt3D();
			this.testReadFloat3D();
			this.testReadBool3D();
			this.testReadString3D();

			this.testWriteInt3D();
			this.testWriteFloat3D();
			this.testWriteBool3D();
			this.testWriteString3D();

		} catch (VnanoException e) {
			throw new CombinedTestException(e);
		}
	}


	// --------------------------------------------------------------------------------
	// Read 1D Array
	// --------------------------------------------------------------------------------

	private void testReadInt1D() throws VnanoException {
		String scriptCode;
		String resultS;
		scriptCode = "string result = i1d[0] + \",\" + i1d[1] + \",\" + i1d[2] + \",\" + i1d[3]; result;";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "9000,9001,9002,9003", "read int[i]", scriptCode);
	}

	private void testReadFloat1D() throws VnanoException {
		String scriptCode;
		String resultS;
		scriptCode = "string result = f1d[0] + \",\" + f1d[1] + \",\" + f1d[2] + \",\" + f1d[3]; result;";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "9000.5,9001.5,9002.5,9003.5", "read float[i]", scriptCode);
	}

	private void testReadBool1D() throws VnanoException {
		String scriptCode;
		String resultS;
		scriptCode = "string result = b1d[0] + \",\" + b1d[1] + \",\" + b1d[2] + \",\" + b1d[3]; result;";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "true,false,true,false", "read bool[i]", scriptCode);
	}

	private void testReadString1D() throws VnanoException {
		String scriptCode;
		String resultS;
		scriptCode = "string result = s1d[0] + \",\" + s1d[1] + \",\" + s1d[2] + \",\" + s1d[3]; result;";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(resultS, "a0,a1,a2,a3", "read string[i]", scriptCode);
	}


	// --------------------------------------------------------------------------------
	// Write 1D Array
	// --------------------------------------------------------------------------------

	private void testWriteInt1D() throws VnanoException {
		String scriptCode;
		String resultS;
		scriptCode = "i1d[0]=-9000; i1d[1]=-9001; i1d[2]=-9002; i1d[3]=-9003;";
		this.engine.executeScript(scriptCode);
		resultS =
			this.arrayPlugin.i1d[0] + "," + this.arrayPlugin.i1d[1] + "," + this.arrayPlugin.i1d[2] + "," + this.arrayPlugin.i1d[3];
		super.evaluateResult(resultS, "-9000,-9001,-9002,-9003", "write int[i]", scriptCode);
	}

	private void testWriteFloat1D() throws VnanoException {
		String scriptCode;
		String resultS;
		scriptCode = "f1d[0]=-9000.5; f1d[1]=-9001.5; f1d[2]=-9002.5; f1d[3]=-9003.5;";
		this.engine.executeScript(scriptCode);
		resultS =
			this.arrayPlugin.f1d[0] + "," + this.arrayPlugin.f1d[1] + "," + this.arrayPlugin.f1d[2] + "," + this.arrayPlugin.f1d[3];
		super.evaluateResult(resultS, "-9000.5,-9001.5,-9002.5,-9003.5", "write float[i]", scriptCode);
	}

	private void testWriteBool1D() throws VnanoException {
		String scriptCode;
		String resultS;
		scriptCode = "b1d[0]=false; b1d[1]=true; b1d[2]=false; b1d[3]=true;"; // 初期値を反転した値
		this.engine.executeScript(scriptCode);
		resultS =
			this.arrayPlugin.b1d[0] + "," + this.arrayPlugin.b1d[1] + "," + this.arrayPlugin.b1d[2] + "," + this.arrayPlugin.b1d[3];
		super.evaluateResult(resultS, "false,true,false,true", "write bool[i]", scriptCode);
	}

	private void testWriteString1D() throws VnanoException {
		String scriptCode;
		String resultS;
		scriptCode = "s1d[0]=\"p0\"; s1d[1]=\"p1\"; s1d[2]=\"p2\"; s1d[3]=\"p3\";";
		this.engine.executeScript(scriptCode);
		resultS =
			this.arrayPlugin.s1d[0] + "," + this.arrayPlugin.s1d[1] + "," + this.arrayPlugin.s1d[2] + "," + this.arrayPlugin.s1d[3];
		super.evaluateResult(resultS, "p0,p1,p2,p3", "write string[i]", scriptCode);
	}


	// --------------------------------------------------------------------------------
	// Read 2D Array
	// --------------------------------------------------------------------------------

	private void testReadInt2D() throws VnanoException {
		String scriptCode;
		String resultS;
		scriptCode = "string result = "
			+ "i2d[0][0] + \",\" + i2d[0][1] + \",\" + i2d[0][2] + \",\" + i2d[0][3]" + "+ \"|\" + "
			+ "i2d[1][0] + \",\" + i2d[1][1] + \",\" + i2d[1][2] + \",\" + i2d[1][3]" + "+ \"|\" + "
			+ "i2d[2][0] + \",\" + i2d[2][1] + \",\" + i2d[2][2] + \",\" + i2d[2][3]"
			+ "; result;";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(
			resultS,

			"9000,9001,9002,9003" + "|" +
			"9010,9011,9012,9013" + "|" +
			"9020,9021,9022,9023",

			"read int[i][j]", scriptCode
		);
	}

	private void testReadFloat2D() throws VnanoException {
		String scriptCode;
		String resultS;
		scriptCode = "string result = "
			+ "f2d[0][0] + \",\" + f2d[0][1] + \",\" + f2d[0][2] + \",\" + f2d[0][3]" + "+ \"|\" + "
			+ "f2d[1][0] + \",\" + f2d[1][1] + \",\" + f2d[1][2] + \",\" + f2d[1][3]" + "+ \"|\" + "
			+ "f2d[2][0] + \",\" + f2d[2][1] + \",\" + f2d[2][2] + \",\" + f2d[2][3]"
			+ "; result;";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(
			resultS,

			"9000.5,9001.5,9002.5,9003.5" + "|" +
			"9010.5,9011.5,9012.5,9013.5" + "|" +
			"9020.5,9021.5,9022.5,9023.5",

			"read float[i][j]", scriptCode
		);
	}

	private void testReadBool2D() throws VnanoException {
		String scriptCode;
		String resultS;
		scriptCode = "string result = "
			+ "b2d[0][0] + \",\" + b2d[0][1] + \",\" + b2d[0][2] + \",\" + b2d[0][3]" + "+ \"|\" + "
			+ "b2d[1][0] + \",\" + b2d[1][1] + \",\" + b2d[1][2] + \",\" + b2d[1][3]" + "+ \"|\" + "
			+ "b2d[2][0] + \",\" + b2d[2][1] + \",\" + b2d[2][2] + \",\" + b2d[2][3]"
			+ "; result;";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(
			resultS,

			"true,false,true,false" + "|" +
			"false,true,false,true" + "|" +
			"true,true,false,false",

			"read bool[i][j]", scriptCode
		);
	}

	private void testReadString2D() throws VnanoException {
		String scriptCode;
		String resultS;
		scriptCode = "string result = "
			+ "s2d[0][0] + \",\" + s2d[0][1] + \",\" + s2d[0][2] + \",\" + s2d[0][3]" + "+ \"|\" + "
			+ "s2d[1][0] + \",\" + s2d[1][1] + \",\" + s2d[1][2] + \",\" + s2d[1][3]" + "+ \"|\" + "
			+ "s2d[2][0] + \",\" + s2d[2][1] + \",\" + s2d[2][2] + \",\" + s2d[2][3]"
			+ "; result;";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(
			resultS,

			"a0,a1,a2,a3" + "|" +
			"b0,b1,b2,b3" + "|" +
			"c0,c1,c2,c3",

			"read bool[i][j]", scriptCode
		);
	}

	private void testReadInt3D() throws VnanoException {
		String scriptCode;
		String resultS;
		scriptCode = "string result = "
			+ "i3d[0][0][0] + \",\" + i3d[0][0][1] + \",\" + i3d[0][0][2] + \",\" + i3d[0][0][3]" + "+ \"|\" + "
			+ "i3d[0][1][0] + \",\" + i3d[0][1][1] + \",\" + i3d[0][1][2] + \",\" + i3d[0][1][3]" + "+ \"|\" + "
			+ "i3d[0][2][0] + \",\" + i3d[0][2][1] + \",\" + i3d[0][2][2] + \",\" + i3d[0][2][3]" + "+ \"||\" + "
			+ "i3d[1][0][0] + \",\" + i3d[1][0][1] + \",\" + i3d[1][0][2] + \",\" + i3d[1][0][3]" + "+ \"|\" + "
			+ "i3d[1][1][0] + \",\" + i3d[1][1][1] + \",\" + i3d[1][1][2] + \",\" + i3d[1][1][3]" + "+ \"|\" + "
			+ "i3d[1][2][0] + \",\" + i3d[1][2][1] + \",\" + i3d[1][2][2] + \",\" + i3d[1][2][3]"
			+ "; result;";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(
			resultS,

			"9000,9001,9002,9003" + "|" +
			"9010,9011,9012,9013" + "|" +
			"9020,9021,9022,9023" + "||" +
			"9100,9101,9102,9103" + "|" +
			"9110,9111,9112,9113" + "|" +
			"9120,9121,9122,9123",

			"read int[i][j][k]", scriptCode
		);
	}


	// --------------------------------------------------------------------------------
	// Write 2D Array
	// --------------------------------------------------------------------------------

	private void testWriteInt2D() throws VnanoException {
		String scriptCode;
		String resultS;
		String expectedS;
		scriptCode =
			"i2d[0][0]=-9000; i2d[0][1]=-9001; i2d[0][2]=-9002; i2d[0][3]=-9003;" +
			"i2d[1][0]=-9010; i2d[1][1]=-9011; i2d[1][2]=-9012; i2d[1][3]=-9013;" +
			"i2d[2][0]=-9020; i2d[2][1]=-9021; i2d[2][2]=-9022; i2d[2][3]=-9023;" ;
		this.engine.executeScript(scriptCode);
		resultS =
			this.arrayPlugin.i2d[0][0] + "," + this.arrayPlugin.i2d[0][1] + "," + this.arrayPlugin.i2d[0][2] + "," + this.arrayPlugin.i2d[0][3] + "|"+
			this.arrayPlugin.i2d[1][0] + "," + this.arrayPlugin.i2d[1][1] + "," + this.arrayPlugin.i2d[1][2] + "," + this.arrayPlugin.i2d[1][3] + "|"+
			this.arrayPlugin.i2d[2][0] + "," + this.arrayPlugin.i2d[2][1] + "," + this.arrayPlugin.i2d[2][2] + "," + this.arrayPlugin.i2d[2][3] ;
		expectedS =
			"-9000,-9001,-9002,-9003|" +
			"-9010,-9011,-9012,-9013|" +
			"-9020,-9021,-9022,-9023" ;
		super.evaluateResult(resultS, expectedS, "write int[i][j]", scriptCode);
	}

	private void testWriteFloat2D() throws VnanoException {
		String scriptCode;
		String resultS;
		String expectedS;
		scriptCode =
			"f2d[0][0]=-9000.5; f2d[0][1]=-9001.5; f2d[0][2]=-9002.5; f2d[0][3]=-9003.5;" +
			"f2d[1][0]=-9010.5; f2d[1][1]=-9011.5; f2d[1][2]=-9012.5; f2d[1][3]=-9013.5;" +
			"f2d[2][0]=-9020.5; f2d[2][1]=-9021.5; f2d[2][2]=-9022.5; f2d[2][3]=-9023.5;" ;
		this.engine.executeScript(scriptCode);
		resultS =
			this.arrayPlugin.f2d[0][0] + "," + this.arrayPlugin.f2d[0][1] + "," + this.arrayPlugin.f2d[0][2] + "," + this.arrayPlugin.f2d[0][3] + "|"+
			this.arrayPlugin.f2d[1][0] + "," + this.arrayPlugin.f2d[1][1] + "," + this.arrayPlugin.f2d[1][2] + "," + this.arrayPlugin.f2d[1][3] + "|"+
			this.arrayPlugin.f2d[2][0] + "," + this.arrayPlugin.f2d[2][1] + "," + this.arrayPlugin.f2d[2][2] + "," + this.arrayPlugin.f2d[2][3] ;
		expectedS =
			"-9000.5,-9001.5,-9002.5,-9003.5|" +
			"-9010.5,-9011.5,-9012.5,-9013.5|" +
			"-9020.5,-9021.5,-9022.5,-9023.5" ;
		super.evaluateResult(resultS, expectedS, "write float[i][j]", scriptCode);
	}

	private void testWriteBool2D() throws VnanoException {
		String scriptCode;
		String resultS;
		String expectedS;
		scriptCode =
			"b2d[0][0]=false; b2d[0][1]=true;  b2d[0][2]=false; b2d[0][3]=true;" + // 初期値を反転した値
			"b2d[1][0]=true;  b2d[1][1]=false; b2d[1][2]=true;  b2d[1][3]=false;" +
			"b2d[2][0]=false; b2d[2][1]=false; b2d[2][2]=true;  b2d[2][3]=true;" ;
		this.engine.executeScript(scriptCode);
		resultS =
			this.arrayPlugin.b2d[0][0] + "," + this.arrayPlugin.b2d[0][1] + "," + this.arrayPlugin.b2d[0][2] + "," + this.arrayPlugin.b2d[0][3] + "|"+
			this.arrayPlugin.b2d[1][0] + "," + this.arrayPlugin.b2d[1][1] + "," + this.arrayPlugin.b2d[1][2] + "," + this.arrayPlugin.b2d[1][3] + "|"+
			this.arrayPlugin.b2d[2][0] + "," + this.arrayPlugin.b2d[2][1] + "," + this.arrayPlugin.b2d[2][2] + "," + this.arrayPlugin.b2d[2][3] ;
		expectedS =
			"false,true,false,true|" +
			"true,false,true,false|" +
			"false,false,true,true" ;
		super.evaluateResult(resultS, expectedS, "write bool[i][j]", scriptCode);
	}

	private void testWriteString2D() throws VnanoException {
		String scriptCode;
		String resultS;
		String expectedS;
		scriptCode =
			"s2d[0][0]=\"p0\"; s2d[0][1]=\"p1\"; s2d[0][2]=\"p2\"; s2d[0][3]=\"p3\";" + // 初期値を反転した値
			"s2d[1][0]=\"q0\"; s2d[1][1]=\"q1\"; s2d[1][2]=\"q2\"; s2d[1][3]=\"q3\";" +
			"s2d[2][0]=\"r0\"; s2d[2][1]=\"r1\"; s2d[2][2]=\"r2\"; s2d[2][3]=\"r3\";" ;
		this.engine.executeScript(scriptCode);
		resultS =
			this.arrayPlugin.s2d[0][0] + "," + this.arrayPlugin.s2d[0][1] + "," + this.arrayPlugin.s2d[0][2] + "," + this.arrayPlugin.s2d[0][3] + "|"+
			this.arrayPlugin.s2d[1][0] + "," + this.arrayPlugin.s2d[1][1] + "," + this.arrayPlugin.s2d[1][2] + "," + this.arrayPlugin.s2d[1][3] + "|"+
			this.arrayPlugin.s2d[2][0] + "," + this.arrayPlugin.s2d[2][1] + "," + this.arrayPlugin.s2d[2][2] + "," + this.arrayPlugin.s2d[2][3] ;
		expectedS =
			"p0,p1,p2,p3|" +
			"q0,q1,q2,q3|" +
			"r0,r1,r2,r3" ;
		super.evaluateResult(resultS, expectedS, "write string[i][j]", scriptCode);
	}


	// --------------------------------------------------------------------------------
	// Read 3D Array
	// --------------------------------------------------------------------------------

	private void testReadFloat3D() throws VnanoException {
		String scriptCode;
		String resultS;
		scriptCode = "string result = "
			+ "f3d[0][0][0] + \",\" + f3d[0][0][1] + \",\" + f3d[0][0][2] + \",\" + f3d[0][0][3]" + "+ \"|\" + "
			+ "f3d[0][1][0] + \",\" + f3d[0][1][1] + \",\" + f3d[0][1][2] + \",\" + f3d[0][1][3]" + "+ \"|\" + "
			+ "f3d[0][2][0] + \",\" + f3d[0][2][1] + \",\" + f3d[0][2][2] + \",\" + f3d[0][2][3]" + "+ \"||\" + "
			+ "f3d[1][0][0] + \",\" + f3d[1][0][1] + \",\" + f3d[1][0][2] + \",\" + f3d[1][0][3]" + "+ \"|\" + "
			+ "f3d[1][1][0] + \",\" + f3d[1][1][1] + \",\" + f3d[1][1][2] + \",\" + f3d[1][1][3]" + "+ \"|\" + "
			+ "f3d[1][2][0] + \",\" + f3d[1][2][1] + \",\" + f3d[1][2][2] + \",\" + f3d[1][2][3]"
			+ "; result;";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(
			resultS,

			"9000.5,9001.5,9002.5,9003.5" + "|" +
			"9010.5,9011.5,9012.5,9013.5" + "|" +
			"9020.5,9021.5,9022.5,9023.5" + "||" +
			"9100.5,9101.5,9102.5,9103.5" + "|" +
			"9110.5,9111.5,9112.5,9113.5" + "|" +
			"9120.5,9121.5,9122.5,9123.5",

			"read float[i][j][k]", scriptCode
		);
	}

	private void testReadBool3D() throws VnanoException {
		String scriptCode;
		String resultS;
		scriptCode = "string result = "
			+ "b3d[0][0][0] + \",\" + b3d[0][0][1] + \",\" + b3d[0][0][2] + \",\" + b3d[0][0][3]" + "+ \"|\" + "
			+ "b3d[0][1][0] + \",\" + b3d[0][1][1] + \",\" + b3d[0][1][2] + \",\" + b3d[0][1][3]" + "+ \"|\" + "
			+ "b3d[0][2][0] + \",\" + b3d[0][2][1] + \",\" + b3d[0][2][2] + \",\" + b3d[0][2][3]" + "+ \"||\" + "
			+ "b3d[1][0][0] + \",\" + b3d[1][0][1] + \",\" + b3d[1][0][2] + \",\" + b3d[1][0][3]" + "+ \"|\" + "
			+ "b3d[1][1][0] + \",\" + b3d[1][1][1] + \",\" + b3d[1][1][2] + \",\" + b3d[1][1][3]" + "+ \"|\" + "
			+ "b3d[1][2][0] + \",\" + b3d[1][2][1] + \",\" + b3d[1][2][2] + \",\" + b3d[1][2][3]"
			+ "; result;";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(
			resultS,

			"true,false,true,false" + "|" +
			"false,true,false,true" + "|" +
			"true,true,false,false" + "||" +
			"false,false,true,true" + "|" +
			"true,true,true,true" + "|" +
			"false,false,false,false",

			"read bool[i][j][k]", scriptCode
		);
	}

	private void testReadString3D() throws VnanoException {
		String scriptCode;
		String resultS;
		scriptCode = "string result = "
			+ "s3d[0][0][0] + \",\" + s3d[0][0][1] + \",\" + s3d[0][0][2] + \",\" + s3d[0][0][3]" + "+ \"|\" + "
			+ "s3d[0][1][0] + \",\" + s3d[0][1][1] + \",\" + s3d[0][1][2] + \",\" + s3d[0][1][3]" + "+ \"|\" + "
			+ "s3d[0][2][0] + \",\" + s3d[0][2][1] + \",\" + s3d[0][2][2] + \",\" + s3d[0][2][3]" + "+ \"||\" + "
			+ "s3d[1][0][0] + \",\" + s3d[1][0][1] + \",\" + s3d[1][0][2] + \",\" + s3d[1][0][3]" + "+ \"|\" + "
			+ "s3d[1][1][0] + \",\" + s3d[1][1][1] + \",\" + s3d[1][1][2] + \",\" + s3d[1][1][3]" + "+ \"|\" + "
			+ "s3d[1][2][0] + \",\" + s3d[1][2][1] + \",\" + s3d[1][2][2] + \",\" + s3d[1][2][3]"
			+ "; result;";
		resultS = (String)this.engine.executeScript(scriptCode);
		super.evaluateResult(
			resultS,

			"a0,a1,a2,a3" + "|" +
			"b0,b1,b2,b3" + "|" +
			"c0,c1,c2,c3" + "||" +
			"d0,d1,d2,d3" + "|" +
			"e0,e1,e2,e3" + "|" +
			"f0,f1,f2,f3",

			"read string[i][j][k]", scriptCode
		);
	}


	// --------------------------------------------------------------------------------
	// Write 3D Array
	// --------------------------------------------------------------------------------

	private void testWriteInt3D() throws VnanoException {
		String scriptCode;
		String resultS;
		String expectedS;
		scriptCode =
			"i3d[0][0][0]=-9000; i3d[0][0][1]=-9001; i3d[0][0][2]=-9002; i3d[0][0][3]=-9003;" +
			"i3d[0][1][0]=-9010; i3d[0][1][1]=-9011; i3d[0][1][2]=-9012; i3d[0][1][3]=-9013;" +
			"i3d[0][2][0]=-9020; i3d[0][2][1]=-9021; i3d[0][2][2]=-9022; i3d[0][2][3]=-9023;" +
			"i3d[1][0][0]=-9100; i3d[1][0][1]=-9101; i3d[1][0][2]=-9102; i3d[1][0][3]=-9103;" +
			"i3d[1][1][0]=-9110; i3d[1][1][1]=-9111; i3d[1][1][2]=-9112; i3d[1][1][3]=-9113;" +
			"i3d[1][2][0]=-9120; i3d[1][2][1]=-9121; i3d[1][2][2]=-9122; i3d[1][2][3]=-9123;" ;
		this.engine.executeScript(scriptCode);
		resultS =
			this.arrayPlugin.i3d[0][0][0] + "," + this.arrayPlugin.i3d[0][0][1] + "," + this.arrayPlugin.i3d[0][0][2] + "," + this.arrayPlugin.i3d[0][0][3] + "|"+
			this.arrayPlugin.i3d[0][1][0] + "," + this.arrayPlugin.i3d[0][1][1] + "," + this.arrayPlugin.i3d[0][1][2] + "," + this.arrayPlugin.i3d[0][1][3] + "|"+
			this.arrayPlugin.i3d[0][2][0] + "," + this.arrayPlugin.i3d[0][2][1] + "," + this.arrayPlugin.i3d[0][2][2] + "," + this.arrayPlugin.i3d[0][2][3] + "||" +
			this.arrayPlugin.i3d[1][0][0] + "," + this.arrayPlugin.i3d[1][0][1] + "," + this.arrayPlugin.i3d[1][0][2] + "," + this.arrayPlugin.i3d[1][0][3] + "|"+
			this.arrayPlugin.i3d[1][1][0] + "," + this.arrayPlugin.i3d[1][1][1] + "," + this.arrayPlugin.i3d[1][1][2] + "," + this.arrayPlugin.i3d[1][1][3] + "|"+
			this.arrayPlugin.i3d[1][2][0] + "," + this.arrayPlugin.i3d[1][2][1] + "," + this.arrayPlugin.i3d[1][2][2] + "," + this.arrayPlugin.i3d[1][2][3] ;
		expectedS =
			"-9000,-9001,-9002,-9003|" +
			"-9010,-9011,-9012,-9013|" +
			"-9020,-9021,-9022,-9023||" +
			"-9100,-9101,-9102,-9103|" +
			"-9110,-9111,-9112,-9113|" +
			"-9120,-9121,-9122,-9123" ;
		super.evaluateResult(resultS, expectedS, "write int[i][j][k]", scriptCode);
	}

	private void testWriteFloat3D() throws VnanoException {
		String scriptCode;
		String resultS;
		String expectedS;
		scriptCode =
			"f3d[0][0][0]=-9000.5; f3d[0][0][1]=-9001.5; f3d[0][0][2]=-9002.5; f3d[0][0][3]=-9003.5;" +
			"f3d[0][1][0]=-9010.5; f3d[0][1][1]=-9011.5; f3d[0][1][2]=-9012.5; f3d[0][1][3]=-9013.5;" +
			"f3d[0][2][0]=-9020.5; f3d[0][2][1]=-9021.5; f3d[0][2][2]=-9022.5; f3d[0][2][3]=-9023.5;" +
			"f3d[1][0][0]=-9100.5; f3d[1][0][1]=-9101.5; f3d[1][0][2]=-9102.5; f3d[1][0][3]=-9103.5;" +
			"f3d[1][1][0]=-9110.5; f3d[1][1][1]=-9111.5; f3d[1][1][2]=-9112.5; f3d[1][1][3]=-9113.5;" +
			"f3d[1][2][0]=-9120.5; f3d[1][2][1]=-9121.5; f3d[1][2][2]=-9122.5; f3d[1][2][3]=-9123.5;" ;
		this.engine.executeScript(scriptCode);
		resultS =
			this.arrayPlugin.f3d[0][0][0] + "," + this.arrayPlugin.f3d[0][0][1] + "," + this.arrayPlugin.f3d[0][0][2] + "," + this.arrayPlugin.f3d[0][0][3] + "|"+
			this.arrayPlugin.f3d[0][1][0] + "," + this.arrayPlugin.f3d[0][1][1] + "," + this.arrayPlugin.f3d[0][1][2] + "," + this.arrayPlugin.f3d[0][1][3] + "|"+
			this.arrayPlugin.f3d[0][2][0] + "," + this.arrayPlugin.f3d[0][2][1] + "," + this.arrayPlugin.f3d[0][2][2] + "," + this.arrayPlugin.f3d[0][2][3] + "||" +
			this.arrayPlugin.f3d[1][0][0] + "," + this.arrayPlugin.f3d[1][0][1] + "," + this.arrayPlugin.f3d[1][0][2] + "," + this.arrayPlugin.f3d[1][0][3] + "|"+
			this.arrayPlugin.f3d[1][1][0] + "," + this.arrayPlugin.f3d[1][1][1] + "," + this.arrayPlugin.f3d[1][1][2] + "," + this.arrayPlugin.f3d[1][1][3] + "|"+
			this.arrayPlugin.f3d[1][2][0] + "," + this.arrayPlugin.f3d[1][2][1] + "," + this.arrayPlugin.f3d[1][2][2] + "," + this.arrayPlugin.f3d[1][2][3] ;
		expectedS =
			"-9000.5,-9001.5,-9002.5,-9003.5|" +
			"-9010.5,-9011.5,-9012.5,-9013.5|" +
			"-9020.5,-9021.5,-9022.5,-9023.5||" +
			"-9100.5,-9101.5,-9102.5,-9103.5|" +
			"-9110.5,-9111.5,-9112.5,-9113.5|" +
			"-9120.5,-9121.5,-9122.5,-9123.5" ;
		super.evaluateResult(resultS, expectedS, "write float[i][j][k]", scriptCode);
	}

	private void testWriteBool3D() throws VnanoException {
		String scriptCode;
		String resultS;
		String expectedS;
		scriptCode =
			"b3d[0][0][0]=false; b3d[0][0][1]=true;  b3d[0][0][2]=false; b3d[0][0][3]=true;" + // 初期値を反転した値
			"b3d[0][1][0]=true;  b3d[0][1][1]=false; b3d[0][1][2]=true;  b3d[0][1][3]=false;" +
			"b3d[0][2][0]=false; b3d[0][2][1]=false; b3d[0][2][2]=true;  b3d[0][2][3]=true;" +
			"b3d[1][0][0]=true;  b3d[1][0][1]=true;  b3d[1][0][2]=false; b3d[1][0][3]=false;" +
			"b3d[1][1][0]=false; b3d[1][1][1]=false; b3d[1][1][2]=false; b3d[1][1][3]=false;" +
			"b3d[1][2][0]=true;  b3d[1][2][1]=true;  b3d[1][2][2]=true;  b3d[1][2][3]=true;" ;
		this.engine.executeScript(scriptCode);
		resultS =
			this.arrayPlugin.b3d[0][0][0] + "," + this.arrayPlugin.b3d[0][0][1] + "," + this.arrayPlugin.b3d[0][0][2] + "," + this.arrayPlugin.b3d[0][0][3] + "|"+
			this.arrayPlugin.b3d[0][1][0] + "," + this.arrayPlugin.b3d[0][1][1] + "," + this.arrayPlugin.b3d[0][1][2] + "," + this.arrayPlugin.b3d[0][1][3] + "|"+
			this.arrayPlugin.b3d[0][2][0] + "," + this.arrayPlugin.b3d[0][2][1] + "," + this.arrayPlugin.b3d[0][2][2] + "," + this.arrayPlugin.b3d[0][2][3] + "||" +
			this.arrayPlugin.b3d[1][0][0] + "," + this.arrayPlugin.b3d[1][0][1] + "," + this.arrayPlugin.b3d[1][0][2] + "," + this.arrayPlugin.b3d[1][0][3] + "|"+
			this.arrayPlugin.b3d[1][1][0] + "," + this.arrayPlugin.b3d[1][1][1] + "," + this.arrayPlugin.b3d[1][1][2] + "," + this.arrayPlugin.b3d[1][1][3] + "|"+
			this.arrayPlugin.b3d[1][2][0] + "," + this.arrayPlugin.b3d[1][2][1] + "," + this.arrayPlugin.b3d[1][2][2] + "," + this.arrayPlugin.b3d[1][2][3] ;
		expectedS =
			"false,true,false,true|" +
			"true,false,true,false|" +
			"false,false,true,true||" +
			"true,true,false,false|" +
			"false,false,false,false|" +
			"true,true,true,true" ;
		super.evaluateResult(resultS, expectedS, "write bool[i][j][k]", scriptCode);
	}

	private void testWriteString3D() throws VnanoException {
		String scriptCode;
		String resultS;
		String expectedS;
		scriptCode =
			"s3d[0][0][0]=\"p0\"; s3d[0][0][1]=\"p1\"; s3d[0][0][2]=\"p2\"; s3d[0][0][3]=\"p3\";" +
			"s3d[0][1][0]=\"q0\"; s3d[0][1][1]=\"q1\"; s3d[0][1][2]=\"q2\"; s3d[0][1][3]=\"q3\";" +
			"s3d[0][2][0]=\"r0\"; s3d[0][2][1]=\"r1\"; s3d[0][2][2]=\"r2\"; s3d[0][2][3]=\"r3\";" +
			"s3d[1][0][0]=\"s0\"; s3d[1][0][1]=\"s1\"; s3d[1][0][2]=\"s2\"; s3d[1][0][3]=\"s3\";" +
			"s3d[1][1][0]=\"t0\"; s3d[1][1][1]=\"t1\"; s3d[1][1][2]=\"t2\"; s3d[1][1][3]=\"t3\";" +
			"s3d[1][2][0]=\"u0\"; s3d[1][2][1]=\"u1\"; s3d[1][2][2]=\"u2\"; s3d[1][2][3]=\"u3\";" ;
		this.engine.executeScript(scriptCode);
		resultS =
			this.arrayPlugin.s3d[0][0][0] + "," + this.arrayPlugin.s3d[0][0][1] + "," + this.arrayPlugin.s3d[0][0][2] + "," + this.arrayPlugin.s3d[0][0][3] + "|"+
			this.arrayPlugin.s3d[0][1][0] + "," + this.arrayPlugin.s3d[0][1][1] + "," + this.arrayPlugin.s3d[0][1][2] + "," + this.arrayPlugin.s3d[0][1][3] + "|"+
			this.arrayPlugin.s3d[0][2][0] + "," + this.arrayPlugin.s3d[0][2][1] + "," + this.arrayPlugin.s3d[0][2][2] + "," + this.arrayPlugin.s3d[0][2][3] + "||" +
			this.arrayPlugin.s3d[1][0][0] + "," + this.arrayPlugin.s3d[1][0][1] + "," + this.arrayPlugin.s3d[1][0][2] + "," + this.arrayPlugin.s3d[1][0][3] + "|"+
			this.arrayPlugin.s3d[1][1][0] + "," + this.arrayPlugin.s3d[1][1][1] + "," + this.arrayPlugin.s3d[1][1][2] + "," + this.arrayPlugin.s3d[1][1][3] + "|"+
			this.arrayPlugin.s3d[1][2][0] + "," + this.arrayPlugin.s3d[1][2][1] + "," + this.arrayPlugin.s3d[1][2][2] + "," + this.arrayPlugin.s3d[1][2][3] ;
		expectedS =
			"p0,p1,p2,p3|" +
			"q0,q1,q2,q3|" +
			"r0,r1,r2,r3||" +
			"s0,s1,s2,s3|" +
			"t0,t1,t2,t3|" +
			"u0,u1,u2,u3" ;
		super.evaluateResult(resultS, expectedS, "write string[i][j][k]", scriptCode);
	}


}
