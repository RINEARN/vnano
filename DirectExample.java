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
		ExamplePlugin examplePlugin = new DirectExample().new ExamplePlugin();
		try {
			engine.connectPlugin("ExamplePlugin", examplePlugin);
		} catch (VnanoException e) {
			System.err.println("Connection error occurred.");
			e.printStackTrace();
		}

		// Or, if connect only static fields/methods of a class:
		// もしクラスのstaticなフィールド/メソッドのみを接続する場合は：
		// engine.connectPlugin("ExamplePlugin", ExamplePlugin.class);


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
			engine.executeScript(scriptCode);

		} catch (VnanoException e) {
			System.err.println("Scripting error occurred.");
			e.printStackTrace();
			return;
		}
	}
}
