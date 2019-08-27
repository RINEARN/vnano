/*
 * A benchmarking program for measuring the peak performance of 
 * 64-bit floating-point vector operations of Vnano Engine.
 * Vnano処理系の倍精度浮動小数点ベクトル演算ピーク性能計測用ベンチマークプログラム
 * --------------------------------------------------------------------------------
 * This file is released under CC0.
 * Written in 2018-2019 by RINEARN (Fumihiro Matsui)
 * --------------------------------------------------------------------------------
 * 
 * Preparing - 事前準備
 * 
 *     Execute "build.bat" or "build.sh" in advance to generate "Vnano.jar".
 *     あらかじめ build.bat か build.sh を実行し、Vnano.jar を生成しておいて下さい。
 * 
 * 
 * How to compile this code - このコードのコンパイル方法 :
 * 
 *     javac Float64VectorFlopsBenchmark.java -encoding UTF-8
 * 
 * 
 * How to execute this code - このコードの実行方法 :
 * 
 *     java -classpath ".;Vnano.jar" Float64VectorFlopsBenchmark
 * 
 *         or, (depending on your environment)
 * 
 *     java -classpath ".:Vnano.jar" Float64VectorFlopsBenchmark
 * 
 * 
 * Expected result - 正常な実行結果 :
 * 
 *     OPERATING_SPEED = 4.8389764430687805 [GFLOPS]
 *     REQUIRED_TIME = 42.323 [SEC]
 *     TOTAL_OPERATIONS = 204800000000 [xFLOAT64_ADD]
 *     VECTOR_SIZE = 2048 [x64BIT]
 *     OPERATED_VALUES = { 1.0E8, 2.0E8, 3.0E8, ... , 2.047E11, 2.048E11 }
 * 
 *     * Values of OPERATING_SPEED and REQUIRED_TIME are dependent on your environment.
 *     ※OPERATING_SPEED と REQUIRED_TIME の値は環境に依存します。
 * 
 */

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Map;
import java.util.HashMap;

public class Float64VectorFlopsBenchmark {
	
	// A class which provides a field/method accessed from the script as external functions/variables.
	// スクリプト内から外部変数・外部関数としてアクセスされるフィールドとメソッドを提供するクラス
	public static class Plugin {
		public static void output(String paramName, long score, String unit) {
			System.out.println(paramName + " = " + score + " [" + unit + "]");
		}
		public static void output(String paramName, double score, String unit) {
			System.out.println(paramName + " = " + score + " [" + unit + "]");
		}
		public static void output(String paramName, double[] values) {
			int n = values.length;
			System.out.print(paramName + " = { ");
			System.out.print(values[0] + ", " + values[1] + ", " + values[2] + ", ");
			System.out.print("... , " + values[n-2] + ", " + values[n-1]);
			System.out.println(" }");
		}
		public static long time() {
			return System.nanoTime() / 1000000l;
		}
	}

	public static void main(String[] args) {

		// Get ScriptEngine of Vnano from ScriptEngineManager.
		// ScriptEngineManagerでVnanoのスクリプトエンジンを検索して取得
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("vnano");
		if (engine == null) {
			System.err.println("Script engine not found.");
			return;
		}

		// Enable the accelerator (fast version VM) by the option setting.
		// オプション設定でAccelerator（高速版VM）を有効化
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("ACCELERATOR", true);
		engine.put("___VNANO_OPTION_MAP", optionMap);

		// Connect methods/fields of Plugin class to the script engine as external functions/variables.
		// Pluginクラスのメソッド・フィールドを外部関数・変数としてスクリプトエンジンに接続
		engine.put("Plugin", Plugin.class);


		// Create a script code.
		// スクリプトコードを用意
		String scriptCode =

			"  int VECTOR_SIZE = 2048;                                                 " + 
			"  int LOOP_N = 1000*1000;                                                 " + 
			"  int FLOP_PER_LOOP = VECTOR_SIZE * 100;                                  " + 
			"  int TOTAL_FLOP = FLOP_PER_LOOP * LOOP_N;                                " + 
			"                                                                          " + 
			"  double x[VECTOR_SIZE];                                                  " + 
			"  double y[VECTOR_SIZE];                                                  " + 
			"  for (int i=0; i<VECTOR_SIZE; i++) {                                     " + 
			"    x[i] = 0.0;                                                           " + 
			"    y[i] = i + 1.0;                                                       " + 
			"  }                                                                       " + 
			"                                                                          " + 
			"  int beginTime = time();                                                 " + 
			"                                                                          " + 
			"  for (int i=0; i<LOOP_N; ++i) {                                          " + 
			"                                                                          " + 
			"    x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y;           " + 
			"    x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y;           " + 
			"    x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y;           " + 
			"    x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y;           " + 
			"    x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y;           " + 
			"    x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y;           " + 
			"    x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y;           " + 
			"    x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y;           " + 
			"    x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y;           " + 
			"    x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y;           " + 
			"                                                                          " + 
			"  }                                                                       " + 
			"                                                                          " + 
			"  int endTime = time();                                                   " + 
			"  double requiredTime = (endTime - beginTime) / 1000.0;                   " + 
			"  double flops = TOTAL_FLOP / requiredTime;                               " + 
			"                                                                          " + 
			"  output(\"OPERATING_SPEED\", flops/(1000.0*1000.0*1000.0), \"GFLOPS\");  " + 
			"  output(\"REQUIRED_TIME\", requiredTime, \"SEC\");                       " + 
			"  output(\"TOTAL_OPERATIONS\", TOTAL_FLOP, \"xFLOAT64_ADD\");             " + 
			"  output(\"VECTOR_SIZE\", VECTOR_SIZE, \"x64BIT\");                       " + 
			"  output(\"OPERATED_VALUES\", x);                                         " ;


		// Run the script code by the script engine of Vnano.
		// Vnanoのスクリプトエンジンにスクリプトコードを渡して実行
		try{
			engine.eval(scriptCode);
			
		} catch (ScriptException e) {
			System.err.println("Scripting error occurred.");
			e.printStackTrace();
			return;
		}
	}
}
