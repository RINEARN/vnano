/*
 * A simple example of host application code which calling script engine of Vnano.
 * Vnano のスクリプトエンジンを呼び出すホストアプリケーションの簡単なコード例
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

import org.vcssl.nano.VnanoEngine
import org.vcssl.nano.VnanoException

// A class which provides a field/method accessed from the script as external functions/variables.
// スクリプト内から外部変数・外部関数としてアクセスされるフィールドとメソッドを提供するクラス
class ExamplePlugin {
	@JvmField val LOOP_MAX: Int = 100

	fun output(value: Int) {
		println("Output from script: " + value)
	}
}

fun main(args: Array<String>) {

	// Create an instance of the script engine of the Vnano.
	// Vnanoのスクリプトエンジンを生成
	val engine = VnanoEngine()


	// Connect methods/fields of ExamplePlugin class as external functions/variables.
	// ExamplePluginクラスのメソッド・フィールドを外部関数・変数として接続
	val examplePlugin = ExamplePlugin();
	engine.connectPlugin("ExamplePlugin", examplePlugin);


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

	// Run the script code by the script engine of the Vnano.
	// Vnanoのスクリプトエンジンにスクリプトコードを渡して実行
	engine.executeScript(scriptCode)
}
