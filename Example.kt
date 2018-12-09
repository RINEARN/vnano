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
 *     kotlinc -classpath ".;Vnano.jar" Example.kt
 * 
 *         or, (depending on your environment)
 * 
 *     kotlinc -classpath ".:Vnano.jar" Example.kt
 * 
 * 
 * How to execute this code - このコードの実行方法 :
 * 
 *     kotlin -classpath ".;Vnano.jar" ExampleKt
 * 
 *         or, (depending on your environment)
 * 
 *     kotlin -classpath ".:Vnano.jar" ExampleKt
 * 
 * 
 * Expected result - 正常な実行結果 :
 * 
 *     "Output from script: 5050"
 * 
 */

import javax.script.ScriptEngine
import org.vcssl.nano.VnanoEngineFactory

// A class which provides a field/method accessed from the script as external functions/variables.
// スクリプト内から外部変数・外部関数としてアクセスされるフィールドとメソッドを提供するクラス
class ScriptIO {
	@JvmField val LOOP_MAX: Int = 100

	fun output(value: Int) {
		println("Output from script: " + value)
	}
}

fun main(args: Array<String>) {

	// Get a script engine of Vnano.
	// Vnanoのスクリプトエンジンを取得
	val factory = VnanoEngineFactory()
	val engine = factory.getScriptEngine()

	// Connect a field/method to the engine as an external variable/function.
	// フィールドとメソッドを外部関数・変数としてスクリプトエンジンに接続
	val loopMaxField = ScriptIO::class.java.getField("LOOP_MAX")
	val outputMethod = ScriptIO::class.java.getMethod("output", Int::class.java)
	val ioInstance = ScriptIO()
	engine.put("LOOP_MAX", arrayOf(loopMaxField, ioInstance));
	engine.put("output(int)", arrayOf(outputMethod, ioInstance));

	// Create a script code (calculates the value of summation from 1 to 100).
	// スクリプトコードを用意（1から100までの和を求める）
	val scriptCode = """
			int sum = 0;
			int n = LOOP_MAX;
			for (int i=1; i<=n; i++) {
				sum += i;
			}
			output(sum);
	"""

	// Run the script code by the script engine of Vnano.
	// Vnanoのスクリプトエンジンにスクリプトコードを渡して実行
	engine.eval(scriptCode)
}
