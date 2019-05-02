/*
 * A simple example of host application code which calling script engine of Vnano.
 * Vnano のスクリプトエンジンを呼び出すホストアプリケーションの簡単なコード例
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
class ExamplePlugin {
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


	// Connect methods/fields of ExamplePlugin class to the script engine as external functions/variables.
	// ExamplePluginクラスのメソッド・フィールドを外部関数・変数としてスクリプトエンジンに接続
	val examplePlugin = ExamplePlugin();
	engine.put("ExamplePlugin", examplePlugin);

	// Or, if you want to connect each fields/methods to the engine individually:
	// または、もしフィールド/メソッドを個別にスクリプトエンジンに接続したい場合は：
	/*
	val loopMaxField = ExamplePlugin::class.java.getField("LOOP_MAX")
	val outputMethod = ExamplePlugin::class.java.getMethod("output", Int::class.java)
	val examplePlugin = ExamplePlugin()
	engine.put("LOOP_MAX", arrayOf(loopMaxField, examplePlugin));
	engine.put("output(int)", arrayOf(outputMethod, examplePlugin));
	*/


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

	// Note: You can also access to "LOOP_MAX" as "ExamplePlugin.LOOP_MAX",
	//       and can also call "output(sum)" as "ExamplePlugin.output(sum)",
	//       where "ExamplePlugin" is the strings specified to the "put" method of the script engine.
	//       It might be useful when multiple classes/instances are connected to the script engine.
	// 備考:「 LOOP_MAX 」へのアクセスを「 ExamplePlugin.LOOP_MAX 」と書いたり、
	//      「 output(sum) 」の呼び出しを「 ExamplePlugin.output(sum) 」と書く事もできます。
	//       ここで「 ExamplePlugin 」は、スクリプトエンジンの put メソッドに指定した文字列です。
	//       この書き方は、複数のクラス/インスタンスをスクリプトエンジンに接続している場合に便利です。


	// Run the script code by the script engine of Vnano.
	// Vnanoのスクリプトエンジンにスクリプトコードを渡して実行
	engine.eval(scriptCode)
}
