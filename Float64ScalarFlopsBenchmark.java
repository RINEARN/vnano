/*
 * A benchmarking program for measuring the peak performance of 
 * 64-bit floating-point scalar operations of Vnano Engine.
 * Vnano処理系の倍精度浮動小数点スカラ演算ピーク性能計測用ベンチマークプログラム
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
	
	// A class which provides a field/method accessed from the script as external functions/variables.
	// スクリプト内から外部変数・外部関数としてアクセスされるフィールドとメソッドを提供するクラス
	public static class Plugin {
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
			
			// Note: The precision of "float" type in the Vnano is 64-bit, same with "double".
			// 備考: Vnano での float 型は、double 型と同様の64bit精度です。
			"  int LOOP_N = 100*1000*1000;                                             " + 
			"  int FLOP_PER_LOOP = 100;                                                " + 
			"  int TOTAL_FLOP = FLOP_PER_LOOP * LOOP_N;                                " + 
			"                                                                          " + 
			"  float x = 0.0;                                                          " + 
			"  float y = 1.0;                                                          " + 
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
			"  float requiredTime = (endTime - beginTime) / 1000.0;                    " + 
			"  float flops = TOTAL_FLOP / requiredTime;                                " + 
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
