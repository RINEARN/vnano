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
 *     javac -classpath ".;Vnano.jar" DirectExample.java -encoding UTF-8
 * 
 * 
 * How to execute this code - このコードの実行方法 :
 * 
 *     java -classpath ".;Vnano.jar" DirectExample
 * 
 *         or, (depending on your environment)
 * 
 *     java -classpath ".:Vnano.jar" DirectExample
 * 
 * 
 * Expected result - 正常な実行結果 :
 * 
 *     "Output from script: 5050"
 * 
 */

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DirectExample {

	// A class which provides a field/method accessed from the script as external functions/variables.
	// スクリプト内から外部変数・外部関数としてアクセスされるフィールドとメソッドを提供するクラス
	public class ExamplePlugin {
		public int LOOP_MAX = 100;
		public void output(int value) {
			System.out.println("Output from script: " + value);
		}
	}

	public static void main(String[] args) {

		// Create an instance of the script engine of the Vnano.
		// Vnanoのスクリプトエンジンを生成
		VnanoEngine engine = new VnanoEngine();

		// Connect methods/fields of an instance of ExamplePlugin class as external functions/variables.
		// ExamplePluginクラスのインスタンスのメソッド・フィールドを外部関数・変数として接続
		ExamplePlugin examplePlugin = new Example().new ExamplePlugin();
		engine.connectPlugin("ExamplePlugin", examplePlugin);

		// Or, if connect only static fields/methods of a class:
		// もしクラスのstaticなフィールド/メソッドのみを接続する場合は：
		/*
		engine.connectPlugin("ExamplePlugin", ExamplePlugin.class);
		*/


		// Or, if you want to connect each fields/methods to the engine individually:
		// もしくは、フィールド/メソッドを個別にスクリプトエンジンに接続したい場合は：
		/*
		try {
			Field loopMaxField  = ExamplePlugin.class.getField("LOOP_MAX");
			Method outputMethod = ExamplePlugin.class.getMethod("output",int.class);
			ExamplePlugin examplePlugin = new Example().new ExamplePlugin();

			engine.connectPlugin("LOOP_MAX",    new Object[]{ loopMaxField, examplePlugin } );
			engine.connectPlugin("output(int)", new Object[]{ outputMethod, examplePlugin } );

		} catch (NoSuchFieldException | NoSuchMethodException e){
			System.err.println("Method/field not found.");
			e.printStackTrace();
			return;
		}
		*/

		// For static fields/methods:
		// staticなフィールド/メソッドの場合は：
		/*
		try {
			engine.connectPlugin("LOOP_MAX",    ExamplePlugin.class.getField("LOOP_MAX") );
			engine.connectPlugin("output(int)", ExamplePlugin.class.getMethod("output",int.class) );

		} catch (NoSuchFieldException | NoSuchMethodException e){
			System.err.println("Method/field not found.");
			e.printStackTrace();
			return;
		}
		*/


		// Create a script code (calculates the value of summation from 1 to 100).
		// スクリプトコードを用意（1から100までの和を求める）
		String scriptCode = 
				"  int sum = 0;                " +
				"  int n = LOOP_MAX;           " +
				"  for (int i=1; i<=n; i++) {  " +
				"      sum += i;               " +
				"  }                           " +
				"  output(sum);                " ;

		// Note: You can also access to the external variable "LOOP_MAX" as "ExamplePlugin.LOOP_MAX",
		//       and can also call the external function "output(sum)" as "ExamplePlugin.output(sum)",
		//       where "ExamplePlugin" is the strings specified to the "put" method of the script engine.
		//       It might be useful when multiple classes/instances are connected to the script engine.
		// 備考: 外部変数「 LOOP_MAX 」へのアクセスを「 ExamplePlugin.LOOP_MAX 」と書いたり、
		//       外部関数「 output(sum) 」の呼び出しを「 ExamplePlugin.output(sum) 」と書く事もできます。
		//       ここで「 ExamplePlugin 」は、スクリプトエンジンの put メソッドに指定した文字列です。
		//       この書き方は、複数のクラス/インスタンスをスクリプトエンジンに接続している場合に便利です。


		// Run the script code by the script engine of Vnano.
		// Vnanoのスクリプトエンジンにスクリプトコードを渡して実行
		try{
			engine.executeScript(scriptCode);

		} catch (VnanoException e) {
			System.err.println("Scripting error occurred.");
			e.printStackTrace();
			return;
		}
	}
}
