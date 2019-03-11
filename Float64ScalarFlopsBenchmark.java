/*
 * A benchmarking program for measuring the peak performance of 
 * 64-bit floating-point scalar operations of Vnano Engine.
 * Vnano処理系の倍精度浮動小数点スカラ演算ピーク性能計測用ベンチマークプログラム
 * --------------------------------------------------------------------------------
 * This file is released under CC0.
 * Written in 2018 by RINEARN (Fumihiro Matsui)
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
 *     javac Float64ScalarFlopsBenchmark.java -encoding UTF-8
 * 
 * 
 * How to execute this code - このコードの実行方法 :
 * 
 *     java -classpath ".;Vnano.jar" Float64ScalarFlopsBenchmark
 * 
 *         or, (depending on your environment)
 * 
 *     java -classpath ".:Vnano.jar" Float64ScalarFlopsBenchmark
 * 
 * 
 * Expected result - 正常な実行結果 :
 * 
 *     OPERATING_SPEED = 417.6586058555736 [MFLOPS]
 *     REQUIRED_TIME = 23.943 [SEC]
 *     TOTAL_OPERATIONS = 10000000000 [xFLOAT64_ADD]
 *     OPERATED_VALUE = 1.0E10
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

public class Float64ScalarFlopsBenchmark {
	
	// Methods accessed from the script as an external functions.
	// スクリプト側から外部関数としてアクセスするメソッドとフィールド
	public static void output(String paramName, long score, String unit) {
		System.out.println(paramName + " = " + score + " [" + unit + "]");
	}
	public static void output(String paramName, double score, String unit) {
		System.out.println(paramName + " = " + score + " [" + unit + "]");
	}
	public static void output(String paramName, double value) {
		System.out.println(paramName + " = " + value);
	}
	public static long time() {
		return System.nanoTime() / 1000000l;
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
		engine.put("VNANO_OPTION", optionMap);

		// Connect methods to the script engine as external functions.
		// メソッドを外部関数としてスクリプトエンジンに接続
		try {
			engine.put(
				"output(string,int,string)", 
				Float64ScalarFlopsBenchmark.class.getMethod("output", String.class, long.class, String.class)
			);
			engine.put(
				"output(string,double,string)", 
				Float64ScalarFlopsBenchmark.class.getMethod("output", String.class, double.class, String.class)
			);
			engine.put(
				"output(string,double)", 
				Float64ScalarFlopsBenchmark.class.getMethod("output", String.class, double.class)
			);
			engine.put(
				"time()", 
				Float64ScalarFlopsBenchmark.class.getMethod("time")
			);

		} catch (NoSuchMethodException e){
			System.err.println("Method not found.");
			e.printStackTrace();
			return;
		}


		// Create a script code.
		// スクリプトコードを用意
		String scriptCode =

			"  int LOOP_N = 100*1000*1000;                                             " + 
			"  int FLOP_PER_LOOP = 100;                                                " + 
			"  int TOTAL_FLOP = FLOP_PER_LOOP * LOOP_N;                                " + 
			"                                                                          " + 
			"  double x = 0.0;                                                         " + 
			"  double y = 1.0;                                                         " + 
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
			"  output(\"OPERATING_SPEED\", flops/(1000.0*1000.0), \"MFLOPS\");         " + 
			"  output(\"REQUIRED_TIME\", requiredTime, \"SEC\");                       " + 
			"  output(\"TOTAL_OPERATIONS\", TOTAL_FLOP, \"xFLOAT64_ADD\");             " + 
			"  output(\"OPERATED_VALUE\", x);                                          " ;


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
