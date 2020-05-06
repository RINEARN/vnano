/*
 * A simple example of host application code which calling script engine of Vnano.
 * Vnano のスクリプトエンジンを呼び出すホストアプリケーションの簡単なコード例
 * --------------------------------------------------------------------------------
 * This file is released under CC0.
 * Written in 2017-2018 by RINEARN (Fumihiro Matsui)
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
 *     javac Example.java -encoding UTF-8
 * 
 * 
 * How to execute this code - このコードの実行方法 :
 * 
 *     java -classpath ".;Vnano.jar" Example
 * 
 *         or, (depending on your environment)
 * 
 *     java -classpath ".:Vnano.jar" Example
 * 
 * 
 * Expected result - 正常な実行結果 :
 * 
 *     "Output from script: 5050"
 * 
 */

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Example {

	// A class which provides a field/method accessed from the script as external functions/variables.
	// スクリプト内から外部変数・外部関数としてアクセスされるフィールドとメソッドを提供するクラス
	public class ExamplePlugin {
		public int LOOP_MAX = 100;
		public void output(int value) {
			System.out.println("Output from script: " + value);
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

		// Connect methods/fields of an instance of ExamplePlugin class as external functions/variables.
		// ExamplePluginクラスのインスタンスのメソッド・フィールドを外部関数・変数として接続
		ExamplePlugin examplePlugin = new Example().new ExamplePlugin();
		engine.put("ExamplePlugin", examplePlugin);

		// Or, if connect only static fields/methods of a class:
		// もしクラスのstaticなフィールド/メソッドのみを接続する場合は：
		// engine.put("ExamplePlugin", ExamplePlugin.class);


		// Create a script code (calculates the value of summation from 1 to 100).
		// スクリプトコードを用意（1から100までの和を求める）
		String scriptCode = 
				"  int sum = 0;                " +
				"  int n = LOOP_MAX;           " +
				"  for (int i=1; i<=n; i++) {  " +
				"      sum += i;               " +
				"  }                           " +
				"  output(sum);                " ;


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
