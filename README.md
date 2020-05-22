# Vnano

Vnano (<a href="https://www.vcssl.org/">VCSSL</a> nano) is a simple scripting language and its interpreter for embedded use in Java&reg; applications.
( See also: An example of a practical application using the Vnano: <a href="https://github.com/RINEARN/rinpn">RINPn</a>. )

Vnano (<a href="https://www.vcssl.org/">VCSSL</a> nano) は、Java&reg; アプリケーションに組み込んで用いる簡易スクリプト言語＆インタープリタです。
( 実際に Vnano を使用した、実用目的のアプリケーションの例としては「 <a href="https://github.com/RINEARN/rinpn">RINPn</a> 」をご参照ください。 )



<div style="background-color:white; width: 890px; height: 470px; text-align:center; background-image: url('./logo.png'); background-repeat: no-repeat; background-size: contain;">
  <img src="https://github.com/RINEARN/vnano/blob/master/logo.png" alt="" width="890" />
</div>


### The Official Website - 公式サイト

- English: <a href="https://www.vcssl.org/en-us/vnano/">https://www.vcssl.org/en-us/vnano/</a>
- 日本語:   <a href="https://www.vcssl.org/ja-jp/vnano/">https://www.vcssl.org/ja-jp/vnano/</a>

### Tutorial - チュートリアル

- English: <a href="https://www.vcssl.org/en-us/vnano/doc/tutorial/">https://www.vcssl.org/en-us/vnano/doc/tutorial/</a>
- 日本語:   <a href="https://www.vcssl.org/ja-jp/vnano/doc/tutorial/">https://www.vcssl.org/ja-jp/vnano/doc/tutorial/</a>

### API References - API仕様書

- English: <a href="https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/">https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/</a>
- 日本語:   <a href="https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/">https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/</a>

### Current Build/Test Results - 現在のビルド/テスト検証結果

&nbsp;&nbsp;- master branch -
<br />
&nbsp;&nbsp;&nbsp;&nbsp;<img src="https://github.com/RINEARN/vnano/workflows/Standard%20Build%2FTest%20CI/badge.svg?branch=master" alt="Standard CI Result" /> on JDK 13
&nbsp;&nbsp;&nbsp;&nbsp;<img src="https://github.com/RINEARN/vnano/workflows/Old-Env%20Build%2FTest%20CI/badge.svg?branch=master" alt="Old-Env CI Result" /> on JDK 8
&nbsp;&nbsp;
<br />
&nbsp;&nbsp;- develop branch -
<br />
&nbsp;&nbsp;&nbsp;&nbsp;<img src="https://github.com/RINEARN/vnano/workflows/Standard%20Build%2FTest%20CI/badge.svg?branch=develop" alt="Standard CI Result" /> on JDK 13
&nbsp;&nbsp;&nbsp;&nbsp;<img src="https://github.com/RINEARN/vnano/workflows/Old-Env%20Build%2FTest%20CI/badge.svg?branch=develop" alt="Old-Env CI Result" /> on JDK 8
&nbsp;&nbsp;
<br />
&nbsp;&nbsp;&nbsp;&nbsp;( Green: OK / Red: NG )


<hr />



## Vnano README<br />- Index - 目次 -
- <a href="#caution">Caution - 注意</a>
- <a href="#license">License - ライセンス</a>
- <a href="#requirements">Requirements - 必要な環境</a>
- <a href="#example">Application Code Example - アプリケーションコード例</a>
- <a href="#how-to-build">How to Build - ビルド方法</a>
- <a href="#how-to-use-in-java">How to Use in Java&reg; - Java&reg;言語での使用方法</a>
- <a href="#how-to-use-in-kotlin">How to Use in Kotlin&reg; - Kotlin&reg;での使用方法</a>
- <a href="#how-to-use-in-command">How to Use in Command Line - コマンドラインでの使用方法</a>
- <a href="#performances">Performances - 演算速度</a>
- <a href="#architecture">Architecture - アーキテクチャ</a>
- <a href="#language">The Vnano as a Language - 言語としての Vnano</a>
  - <a href="#language-data-type">Data Types - データ型</a>
  - <a href="#language-variable">Variable Declaration Statements - 変数宣言文</a>
    - <a href="#language-variable-scalar">Daclaration of scalar variables - スカラ変数の宣言</a>
    - <a href="#language-variable-scalar">Daclaration of arrays - 配列の宣言</a>
  - <a href="#language-control">Control Statements - 制御文</a>
    - <a href="#language-control-if-else">if and else statements - if 文と else 文</a>
    - <a href="#language-control-for">for statement - for 文</a>
    - <a href="#language-control-while">while statement - while 文</a>
    - <a href="#language-control-break">break statement - break 文</a>
    - <a href="#language-control-continue">continue statement - continue 文</a>
  - <a href="#language-expression">Expressions - 式</a>
    - <a href="#language-expression-syntax">Syntax elements of expressions - 式の構文要素</a>
    - <a href="#language-expression-operator">Operators - 演算子</a>
  - <a href="#language-function">Functions - 関数</a>
	- <a href="#language-function-scalar">Scalar input/output functions - スカラを引数や戻り値とする関数</a>
	- <a href="#language-function-array">Array input/output functions - 配列を引数や戻り値とする関数</a>
	- <a href="#language-function-params-and-args">Formal parameters and actual arguments - 仮引数と実引数</a>
	- <a href="#language-function-call-by-value">Call by value - 引数の値渡し</a>
	- <a href="#language-function-call-by-reference">Call by reference - 引数の参照渡し</a>

- <a href="#plugin">Plug-in - プラグイン</a>
  - <a href="#plugin-external-function-variable">External Functions/Variables and Plug-in - 外部関数/変数とプラグイン</a>
  - <a href="#plugin-security">Point of attention about the security - セキュリティに関する留意点</a>
  - <a href="#plugin-external-variables-synchronization">Caution about the Synchronization of Values of External Variables - 外部変数の値の同期タイミングに関する注意</a>
  - <a href="#plugin-class">Connecting a Class or an Instance as an Plug-in to Use Its All Methods/Fields as External Functions/Variables - クラスやインスタンスをプラグインとして接続し、その全メソッド/フィールドを外部関数/変数として使用する</a>
  - <a href="#plugin-methods-and-fields">Connecting a Particular Method/Field Only as an External Function/Variable Individually - 特定のメソッド/フィールドのみを個別に外部関数/変数として接続する</a>
  - <a href="#plugin-interfaces">Implementation of Plug-ins of External Functions/Variables by Using Low-Overhead Plug-in Interfaces - 低オーバーヘッドのプラグイン用インターフェースを用いて外部関数/変数プラグインを実装する</a>
  - <a href="#plugin-correspondence-of-data-types">The Correspondence of the Data Type between the Vnano and the Data Container - Vnano内とデータコンテナ内でのデータ型の対応関係</a>
  - <a href="#plugin-pack">Packing of Multiple External Functions/Variables as a Plug-in - 複数の外部関数/変数プラグインを1つのプラグインにまとめる</a>
  - <a href="#plugin-official">Use Official Plug-ins - 公式プラグインを利用する</a>
- <a href="#about-us">About Us - 開発元について</a>
- <a href="#references">References - 関連記事</a>




<a id="caution"></a>
## Caution - 注意

Vnano is under development, so it has not practical quality yet.

Vnanoは開発の途中であり、現時点でまだ実用的な品質ではありません。


<a id="license"></a>
## License - ライセンス

This software is released under the MIT License.

このソフトウェアはMITライセンスで公開されています。


<a id="requirements"></a>
## Requirements - 必要な環境

1. Java&reg; Development Kit (JDK) 7 or later - Java&reg;開発環境 (JDK) 7以降
1. Java&reg; Runtime Environment (JRE) 7 or later - Java&reg;実行環境 (JRE) 7以降



<a id="example"></a>
## Application Code Example - アプリケーションコード例

The following is an example Java&reg; application code which executes 
a script code by using Vnano:

<a id="example-scripting-api"></a>
Vnano を使用してスクリプトを実行するJava&reg;アプリケーションのコード例は、以下の通りです：


	( Example.java )

	import javax.script.ScriptEngine;
	import javax.script.ScriptEngineManager;
	import javax.script.ScriptException;

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

			// Connect methods/fields of ExamplePlugin to the script engine as external functions/variables.
			// ExamplePluginクラスのメソッド・フィールドを外部関数・変数としてスクリプトエンジンに接続
			ExamplePlugin examplePlugin = new Example().new ExamplePlugin();
			engine.put("ExamplePlugin", examplePlugin);


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

( &raquo; <a href="#how-to-use-in-java-compile">How to compile - コンパイル方法</a> )

In the above code, we search and get a Vnano engine (scripting engine of the Vnano) by features of Java Scripting API, and use it through methods of javax.script.ScriptEngine interface.
In this way, you can keep independence high between your application and the script engine.
For example, you can compile the above code without the Vnano engine.

上記のコードでは、Java Scripting API の機能を用いてVnanoエンジン（Vnanoのスクリプトエンジン）を検索・取得し、javax.script.ScriptEngineインターフェースのメソッドを介して使用します。この方法では、アプリケーションとスクリプトエンジン間の分離性を高める事ができます。実際、上記のコードのコンパイル時にはVnanoエンジンは不要です。

<a id="example-direct"></a>
On the other hand, you can also create an instance of the Vnano engine directly and use it, as follows:

一方で、以下のように、直接的にVnanoエンジンのインスタンスを生成して使用する事もできます：


	( DirectExample.java )

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

( &raquo; <a href="#how-to-use-in-java-compile">How to compile - コンパイル方法</a> )


The above way may be useful for environments in which Java Scripting API (javax.script package) is not available.

上記の使い方は、Java Scripting API (javax.scriptパッケージ) が使用できない環境などで有用かもしれません。

By the way, the Vnano is also available for apprications written in Kotlin&reg;. An example code is:

<a id="example-kt"></a>
なお、Vnanoは、Kotlin&reg;で記述されたアプリケーションからも使用できます。コード例は以下の通りです：


	( Example.kt )

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

		// Connect methods/fields of ExamplePlugin to the script engine as external functions/variables.
		// ExamplePluginクラスのメソッド・フィールドを外部関数・変数としてスクリプトエンジンに接続
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

( &raquo; <a href="#how-to-use-in-kotlin-compile">How to compile - コンパイル方法</a> )


These example code are contained in this repository as "Example.java" (for Java&reg;) and "Example.kt" (for Kotlin&reg;).
We will actually execute these example code in the next section.

これらのサンプルコードは、"Example.java" (Java&reg;用) および "Example.kt" (Kotlin&reg;用) として、このリポジトリ内に含まれています。
次節では、実際にこのサンプルコードを実行してみます。




<a id="how-to-build"></a>
## How to Build - ビルド方法

### 1. Build the Vnano Engine - Vnanoエンジンのビルド

Firstly, build source code of the Vnano Engine (The script engine of the Vnano).
If you are using Microsoft&reg; Windows&reg;, please double-click "build.bat".
If you are using Linux&reg;, etc., please execute "build.sh" on the bash-compatible shell.
Alternatively, you can build the Vnano Engine by Apache Ant as:

はじめに、Vnanoエンジン（Vnanoのスクリプトエンジン）をビルドします。
Microsoft&reg; Windows&reg; をご使用の場合は、"build.bat" をダブルクリック実行してください。
Linux&reg; 等をご使用の場合は、bash互換シェル上で "build.sh" を実行してください。もしくは以下のように、Apache Ant を用いてVnanoエンジンをビルドする事もできます：

    ant -buildfile build.xml

If you succeeded to build the Vnano Engine, "Vnano.jar" will be generated in the same folder in the above files.
You can use Vnano on your Java applications by appending this JAR file to the classpath.

Vnanoエンジンのビルドが成功すると、"Vnano.jar" が上記ファイルと同じフォルダ内に生成されます。
Vnanoを使用したいJavaアプリケーションから、このJARファイルにクラスパスを通せば、それだけでVnanoが使用できます。

Before using it practically, 
Let's check briefly that the JAR file works expectedly, by following steps.

実際に使用する前に、生成されたJARファイルが正常かどかを、以下の手順で簡単に確認しておきましょう。

### 2. Check the Version - バージョンの確認

You can check the version on the command-line terminal as follows:

以下のようにコマンドラインからバージョンを確認できます。

	java -jar Vnano.jar --version

An example of the result is (depending on the version you use):

結果の例は（実際のバージョンに依存します）：

	RINEARN Vnano Engine 0.2.4

### 3. Execute Tests - テストの実行

In addition, you can execute combined-tests as follows:

また、同様に以下のように結合テストを実行できます：

	java -jar Vnano.jar --test

An example of the result is (depending on the version you use):

結果の例は（実際のバージョンに依存します）：

	- Vnano Engine Combined Test -
	----------------------------------------------------------------

	[ org.vcssl.nano.combinedtest.ArithmeticExpressionCombinedTest ]
	int + int: OK.
	int + float: OK.
	...

	[ org.vcssl.nano.combinedtest.ComparisonExpressionCombinedTest ]
	int == int (same values): OK.
	int == int (different values): OK.
	...

	[ org.vcssl.nano.combinedtest.LogicalExpressionCombinedTest ]
	true && true: OK.
	true && false: OK.
	...

	[ org.vcssl.nano.combinedtest.IfElseStatementCombinedTest ]
	if(true){...}: OK.
	if(false){...}: OK.
	...

	[ org.vcssl.nano.combinedtest.WhileStatementCombinedTest ]
	i=1; while(i<10){i++;}: OK.
	i=1; while(i>10){i++;}: OK.
	...

	[ org.vcssl.nano.combinedtest.ForStatementCombinedTest ]
	i=123; for(i=0;i<10;i++){}: OK.
	i=0; for(i=0;i>10;i++){}: OK.
	...
	for(...){ for(...){ if(x==y||x==z){continue;} ...} ...}: OK.
	for(...){... for(...){... if(x%y==0){continue;} ...} ...}: OK.
	very complicated combinations of continue-statements and deep for-loops: OK.

	...

	----------------------------------------------------------------
	All combined tests have been completed successfully.


For source code on this repository, above tests are executed automatically by CI.
However, if you use the Vnano Engine with modifications/customizations of source code, 
you should execute above tests manually.

リポジトリ上のソースコードに対しては、このテストは CI によって自動的に実行されています。
しかしながら、ソースコードを編集/改造してVnanoエンジンを用いる場合は、
手元で上記のようにテストを実行する必要があります。


<a id="how-to-use-in-java"></a>
## How to Use in Java&reg; - Java&reg;言語での使用方法

### 1. Build the Vnano Engine - Vnanoエンジンのビルド

Firstly, build source code of the Vnano Engine (The script engine of the Vnano) 
by steps of "<a href="#how-to-build">How to Build</a>" section.
Then a JAR file "Vnano.jar" will be generated, so put it into the working folder.

はじめに、「 <a href="#how-to-build">ビルド方法</a> 」の手順に従って
Vnanoエンジン（Vnanoのスクリプトエンジン）をビルドします。
ビルドするとJARファイル「 Vnano.jar 」が生成されるため、それを作業フォルダ内に配置してください。

### 2. Compile the Example Application - サンプルアプリケーションのコンパイル

Let's compile the simple example code of host Java application "<a href="#example-scripting-api">Example.java</a>" which executes a script code by using Vnano Engine: 

それでは、実際にVnanoエンジンを使用して、スクリプトを実行する、ホストアプリケーションのサンプルコード「 <a href="#example-scripting-api">Example.java</a> 」をコンパイルしてみましょう：

    javac -encoding UTF-8 Example.java


As the result of the compilation, "Example.class" will be generated in the same folder.

コンパイルが無事成功すると、同じフォルダ内に Example.class が生成されます。

However, if Java Scripting API (javax.script package) is not available for your environment, some errors will be occur for the compilation or the execution. 
In that case, instead of Example.java, use "<a href="#example-direct">DirectExample.java</a>" as follows.

しかし、Java Scripting API （javax.script パッケージ） が使用できない環境では、上記のコンパイル時か実行時にエラーになってしまいます。その場合は以下のように、Example.java の代わりに「 <a href="#example-direct">DirectExample.java</a> 」を使用してください。

If you are using Microsoft&reg; Windows&reg;:

Microsoft&reg; Windows&reg; の場合は：

    javac -encoding UTF-8 -classpath ".;Vnano.jar" DirectExample.java

If you are using Linux&reg;, etc.:

Linux&reg;等の場合は：

    javac -encoding UTF-8 -classpath ".:Vnano.jar" DirectExample.java



<a id="how-to-use-in-java-compile"></a>
### 3. Execute the Example Application - サンプルアプリケーションの実行

Then, execute the compiled example application with appending "Vnano.jar" to the classpath as follows. If you are using Microsoft&reg; Windows&reg;:

コンパイルしたサンプルアプリケーションは、Vnano.jar にクラスパスを通して実行します。
Microsoft&reg; Windows&reg; の場合は：

    java -classpath ".;Vnano.jar" Example

    or, 
	
	java -classpath ".;Vnano.jar" DirectExample

If you are using Linux&reg;, etc.:

Linux&reg;等の場合は：

    java -classpath ".:Vnano.jar" Example

	or, 

    java -classpath ".:Vnano.jar" DirectExample

As the result of the execution, the following line will be printed to the standard output:

正常に実行されると、以下の内容が標準出力に表示されます：

    Output from script: 5050

### 4. Create the JAR file of the Example Application - サンプルアプリケーションのJARファイル化

To create the JAR file of the example application, 
please create a manifest file "manifest.txt" in advance, 
and in there specify "Vnano.jar" to the Class-Path section as follows:

サンプルアプリケーションをJARファイル化するには、まずマニフェストファイル manifest.txt を作成し、
その中で通常のメインクラス指定に加えて、以下のようにVnano.jar のクラスパスを記載します：

    Main-Class: Example
    Class-Path: . Vnano.jar

Then create the JAR file with specifying the above manifest file as follows:

このマニフェストファイルを指定して、JARファイルを生成します：

    jar cvfm Example.jar manifest.txt Example.class

As the result of the above processing, "Example.jar" will be generated in the same folder.
It is necessary to locate "Vnano.jar" in the same folder to execute "Example.jar". 
If you want to locate "Vnano.jar" in the different folder (e.g. lib folder),
please rewrite the description "Vnano.jar" in "Class-Path" section of the manifest file
to the relative path (e.g. "lib/Vnano.jar").

これで Example.jar が生成されます。このJARファイルを実行する際に、
Vnano.jar を同じフォルダ内に置いておけば使用できます。
もし、Vnano.jar を別の階層のフォルダ（例：lib など）内に置きたい場合は、
マニフェストファイルの Class-Path 指定の「Vnano.jar」の箇所を、
Example.jar から見た相対パスで書き換えてください（例：lib/Vnano.jar ）。





<a id="how-to-use-in-kotlin"></a>
## How to Use in Kotlin&reg; - Kotlin&reg;での使用方法

### 1. Build the Vnano Engine - Vnanoエンジンのビルド

Firstly, build source code of the Vnano Engine (The script engine of the Vnano) 
by steps of "<a href="#how-to-build">How to Build</a>" section.
Then a JAR file "Vnano.jar" will be generated, so put it into the working folder.

はじめに、「 <a href="#how-to-build">ビルド方法</a> 」の手順に従って
Vnanoエンジン（Vnanoのスクリプトエンジン）をビルドします。
ビルドするとJARファイル「 Vnano.jar 」が生成されるため、それを作業フォルダ内に配置してください。

<a id="how-to-use-in-kotlin-compile"></a>
### 2. Compile the Example Application - サンプルアプリケーションのコンパイル

Let's compile the simple example code of host application written in Kotlin, "<a href="#example-kt">Example.kt</a>", which executes a script code by using Vnano Engine. It is necessary to compile the application with appending "Vnano.jar" to the classpath as follows. If you are using Microsoft&reg; Windows&reg;:

それでは、実際にVnanoエンジンを使用して、スクリプトを実行する、Kotlinで記述されたホストアプリケーションのサンプルコード「 <a href="#example-kt">Example.kt</a> 」をコンパイルしてみましょう。コンパイルは、Vnano.jar にクラスパスを通しながら行います。Microsoft&reg; Windows&reg; の場合は：

    kotlinc -classpath ".;Vnano.jar" Example.kt

If you are using Linux&reg;, etc.:

Linux等の場合は&reg;：

    kotlinc -classpath ".:Vnano.jar" Example.kt

As the result of the compilation, "ExampleKt.class" will be generated in the same folder.

コンパイルが成功すると、同じフォルダ内に ExampleKt.class が生成されます。

### 3. Execute the Example Application - サンプルアプリケーションの実行

Then, execute the compiled example application with appending "Vnano.jar" to the classpath as follows. If you are using Microsoft Windows:

コンパイルしたサンプルアプリケーションは、Vnano.jar にクラスパスを通して実行します。
Microsoft Windows の場合は：

    kotlin -classpath ".;Vnano.jar" ExampleKt

If you are using Linux, etc.:

Linux等の場合は：

    kotlin -classpath ".:Vnano.jar" ExampleKt

As the result of the execution, the following line will be printed to the standard output:

正常に実行されると、以下の内容が標準出力に表示されます：

    Output from script: 5050





<a id="how-to-use-in-command"></a>
## How to Use in Command Line - コマンドラインでの使用方法

### 1. About Command-Line Mode - コマンドラインモードについて

The main purpose of the Vnano is the embedded use in applications, 
however, for the development and the debugging usages, 
you can use the script engine of the Vnano on a command-line terminal 
and can directly run script code.
Please note that this command-line mode is NOT the feature for using Vnano alone for the practical purpose. 
It is NOT convenient at all. In such purpose, we recommend to use the 
<a href="https://www.vcssl.org/">VCSSL</a> instead of the Vnano.
The Vnano is a subset of the VCSSL for embedded use in apprications.

Vnano の本来の用途はアプリケーション組み込み用ですが、一方で開発時やデバッグ時などのため、
Vnanoのスクリプトエンジンをコマンドライン端末上で使用して、
Vnanoで記述されたスクリプトコードを直接実行する事ができます。
ただし、このコマンドラインモードは、Vnanoを単独で実用目的のスクリプト言語として使うための機能ではない事に注意してください。
そのような用途にはVnanoは全く便利ではありません。
Vnanoは、<a href="https://www.vcssl.org/">VCSSL</a>という言語から、アプリケーション組み込み用に機能を絞ったサブセットであるため、
単独での使用には、少なくともフル機能版であるVCSSLのご使用をおすすめします。


### 2. Build the Vnano Engine - Vnanoエンジンのビルド

Firstly, build source code of the Vnano Engine (The script engine of the Vnano) 
by steps of "<a href="#how-to-build">How to Build</a>" section.
Then a JAR file "Vnano.jar" will be generated, so put it into the working folder.

はじめに、「 <a href="#how-to-build">ビルド方法</a> 」の手順に従って
Vnanoエンジン（Vnanoのスクリプトエンジン）をビルドします。
ビルドするとJARファイル「 Vnano.jar 」が生成されるため、それを作業フォルダ内に配置してください。


### 3. Run the Example Script Code - サンプルスクリプトコードの実行

An example Vnano script code "Example.vnano" is contained in the repository.

このリポジトリ内には、Vnanoのサンプルスクリプトコードも含まれています。

    (Example.vnano)

	...

    int sum = 0;
    int n = 100;
    for (int i=1; i<=n; i++) {
        sum += i;
    }
    output(sum);

This sample script code calculates the value of summation from 1 to 100. Let's run it as follows:

このサンプルスクリプトコードは、1から100までの和を求めて出力するものです。以下のように実行できます：

    java -jar Vnano.jar Example.vnano

As the result, the following line will be printed to the standard output:

正常に実行されると、以下の内容が標準出力に表示されます：

    5050

Also, if you want to specify the text-encoding of the script file, use --encoding option:

なお、スクリプトコードの文字コードを指定したい場合は、以下のように --encoding オプションを使用します：

    java -jar Vnano.jar Example.vnano --encoding UTF-8
    java -jar Vnano.jar Example.vnano --encoding Shift_JIS

The default text-encoding of this command-line mode is UTF-8.

コマンドラインモードでのデフォルトの文字コードは UTF-8 です。


### 4. Dump the AST, Intermediate Code (VRIL), etc. - 抽象構文木(AST)や中間コード(VRIL)などのダンプ

If you want to dump the Abstract Syntax Tree (AST), Intermediate Code (VRIL Code) for VM, etc. 
for the analyzation or the debugging, 
use --dump option:

もしも抽象構文木(AST)やVM用の中間コード(VRILコード)をダンプして解析やデバッグを行いたい場合は、
--dump オプションを使用してください：

    java -jar Vnano.jar Example.vnano --dump

The (abbreviated) result is :

実行結果は（かなり省略しています）：

	...
	================================================================================
	= Preprocessed Code
	= - Output of: org.vcssl.nano.compiler.Preprocessor
	= - Input  of: org.vcssl.nano.compiler.LexicalAnalyzer
	================================================================================
	...
	int sum = 0;
	int n = 100;
	for (int i=1; i<=n; i++) {
		sum += i;
	}
	output(sum);

	================================================================================
	= Tokens
	= - Output of: org.vcssl.nano.compiler.LexicalAnalyzer
	= - Input  of: org.vcssl.nano.compiler.Parser
	================================================================================
	[Token word="int", lineNumber=70, fileName="Example.vnano", type=DATA_TYPE, priority=0]
	[Token word="sum", lineNumber=70, fileName="Example.vnano", type=LEAF, priority=0, LEAF_TYPE="variableIdentifier"]
	[Token word="=", lineNumber=70, fileName="Example.vnano", type=OPERATOR, priority=6000, OPERATOR_EXECUTOR="assignment", OPERATOR_SYNTAX="binary"]
	[Token word="0", lineNumber=70, fileName="Example.vnano", type=LEAF, priority=0, LEAF_TYPE="literal"]
	[Token word=";", lineNumber=70, fileName="Example.vnano", type=END_OF_STATEMENT, priority=0]
	...

	================================================================================
	= Parsed AST
	= - Output of: org.vcssl.nano.compiler.Parser
	= - Input  of: org.vcssl.nano.compiler.SemanticAnalyzer
	================================================================================
	<ROOT>
 	  <VARIABLE DATA_TYPE="int" IDENTIFIER_VALUE="sum" RANK="0">
	    <EXPRESSION>
	      <OPERATOR OPERATOR_SYNTAX="binary" OPERATOR_EXECUTOR="assignment" OPERATOR_SYMBOL="=" OPERATOR_PRIORITY="6000">
            <LEAF LEAF_TYPE="variableIdentifier" IDENTIFIER_VALUE="sum" />
            <LEAF LEAF_TYPE="literal" LITERAL_VALUE="0" />
   	      </OPERATOR>
  	    </EXPRESSION>
	...

	================================================================================
	= Analyzed AST
	= - Output of: org.vcssl.nano.compiler.SemanticAnalyzer
	= - Input  of: org.vcssl.nano.compiler.CodeGenerator
	================================================================================
	<ROOT>
	  <VARIABLE DATA_TYPE="int" IDENTIFIER_VALUE="sum" RANK="0" SCOPE="local" IDENTIFIER_SERIAL_NUMBER="0">
	    <EXPRESSION DATA_TYPE="int" RANK="0">
	      <OPERATOR OPERATOR_SYNTAX="binary" OPERATOR_EXECUTOR="assignment" OPERATOR_SYMBOL="=" OPERATOR_PRIORITY="6000" DATA_TYPE="int" OPERATOR_EXECUTION_DATA_TYPE="int" RANK="0">
	        <LEAF LEAF_TYPE="variableIdentifier" IDENTIFIER_VALUE="sum" IDENTIFIER_SERIAL_NUMBER="0" SCOPE="local" RANK="0" DATA_TYPE="int" />
	        <LEAF LEAF_TYPE="literal" LITERAL_VALUE="0" RANK="0" DATA_TYPE="int" />
	      </OPERATOR>
	    </EXPRESSION>
		...

	================================================================================
	= Assembly Code (VRIL Code)
	= - Output of: org.vcssl.nano.compiler.CodeGenerator
	= - Input  of: org.vcssl.nano.vm.assembler.Assembler
	================================================================================
	#ASSEMBLY_LANGUAGE_IDENTIFIER   "Vector Register Intermediate Language (VRIL)";
	#ASSEMBLY_LANGUAGE_VERSION      "0.0.1";
	#SCRIPT_LANGUAGE_IDENTIFIER     "Vnano";
	#SCRIPT_LANGUAGE_VERSION        "0.0.1";

	#GLOBAL_FUNCTION        _output(int);

	#LOCAL_VARIABLE _sum@0;
	#LOCAL_VARIABLE _n@1;
	#LOCAL_VARIABLE _i@2;

	#META   "line=70, file=Example.vnano";
	        ALLOC   int     _sum@0;
	        MOV     int     _sum@0  ~int:0;

	#META   "line=71, file=Example.vnano";
	        ALLOC   int     _n@1;
	        MOV     int     _n@1    ~int:100;
	        ...

	================================================================================
	= VM Object Code
	= - Output of: org.vcssl.nano.vm.assembler.Assembler
	================================================================================
	#INSTRUCTION
		0	ALLOC	INT64		L0	C0
		1	MOV	INT64		L0	C1	C0
		2	ALLOC	INT64		L1	C2
		3	MOV	INT64		L1	C3	C2
		4	ALLOC	INT64		L2	C4
		5	MOV	INT64		L2	C5	C4
		6	NOP	VOID		C4
		7	ALLOC	BOOL		R0	C4
		8	LEQ	INT64		R0	L2	L1	C4
		9	JMPN	BOOL		N0	C6	R0	C4
		10	ADD	INT64		L0	L0	L2	C7
	...

Sometimes the above content might be too long 
and you want to concentrate on only 1 section in the above content.
In such case, specify the name of the dump-target (see --help) as an argument of --dump option.
And more, you can specify to not to run the script code after dump by --run option, 
which is useful to prevent that output from the script mixes in the standard output.
For example: 

場合によっては、上記のようなダンプ内容の全体は長くなりすぎる場合がありますし、
その中の一つのセクションにのみ注目したい場合もあるでしょう。
そのような場合は、--dump オプションの引数にダンプ対象の名前（--help参照）を指定します。
さらに、--run オプションを指定する事で、ダンプ後のスクリプト実行を行わないように指定して、
余計な標準出力が混ざるのも防げます。
これらを用いたコマンド例は：

    java -jar Vnano.jar Example.vnano --dump assemblyCode --run false

The result is :

実行結果は：

	#ASSEMBLY_LANGUAGE_IDENTIFIER   "Vector Register Intermediate Language (VRIL)";
	#ASSEMBLY_LANGUAGE_VERSION      "0.0.1";
	#SCRIPT_LANGUAGE_IDENTIFIER     "Vnano";
	#SCRIPT_LANGUAGE_VERSION        "0.0.1";

	#GLOBAL_FUNCTION        _output(int);

	#LOCAL_VARIABLE _sum@0;
	#LOCAL_VARIABLE _n@1;
	#LOCAL_VARIABLE _i@2;

	#META   "line=70, file=Example.vnano";
	        ALLOC   int     _sum@0;
	        MOV     int     _sum@0  ~int:0;

	#META   "line=71, file=Example.vnano";
	        ALLOC   int     _n@1;
	        MOV     int     _n@1    ~int:100;

	#META   "line=72, file=Example.vnano";
	        ALLOC   int     _i@2;
	        MOV     int     _i@2    ~int:1;
	#LABEL  &LABEL0;
	        ALLOC   bool    R0;
	        LEQ     int     R0      _i@2     _n@1;
	        JMPN    bool    -       &LABEL2  R0;

	#META   "line=73, file=Example.vnano";
	        ADD     int     _sum@0  _sum@0   _i@2;
	#LABEL  &LABEL1;
	        ALLOC   int     R3;
	        MOV     int     R3      _i@2;
	        ALLOC   int     R1;
	        ADD     int     R1      _i@2     ~int:1;
	        MOV     int     _i@2    R1;
	        JMP     bool    -       &LABEL0  ~bool:true; 
	#LABEL  &LABEL2;

	#META   "line=75, file=Example.vnano";
	        CALLX   void    -       _output(int)   _sum@0;
	        END     void    -;


This is the compiled intermediate code of the script engine of Vnano, 
which is written in Vector Register Intermediate Language (VRIL).
See <a href="#architecture">Architecture</a> section for more details.
You can save this VRIL code to a file by using redirect:

これはVnanoのスクリプトエンジン内でコンパイルされた中間コードで、
ベクトルレジスタ中間言語（VRIL）で記述されています。
詳細は <a href="#architecture">アーキテクチャ</a> の項目を参照してください。
このVRILコードをファイルに保存するには、以下のようにリダイレクトを使用します：

    java -jar Vnano.jar Example.vnano --dump assemblyCode --run false > Example.vril

As the result of the above command, the compiled VRIL code is saved the file "Example.vril".
However, please Note that the text-encoding of that file depends on your environment.
By the way, you can run VRIL code as the same as Vnano script code:

これでコンパイル済みVRILコードが Example.vril として保存されます。
ただし、保存されるファイルの文字コードは環境に応じて異なる事に注意してくさい。
ところで、VRILコードもVnanoスクリプトコードと同じように実行できます：

    java -jar Vnano.jar Example.vril

or, on some environment, VRIL code will be saved by using Shift_JIS encoding, so

または、先ほどのコマンドでは環境によってはVRILコードが Shift_JIS で保存されるので：

    java -jar Vnano.jar Example.vril --encoding Shift_JIS

The result is :

実行結果は：

    5050

For other features of the command-line mode of the Vnano, please see the result of:

Vnanoのコマンドラインモードのその他の機能については、以下で出力される内容をご参照ください。

    java -jar Vnano.jar --help

The command-line mode we described in this section may assist you 
to customize the script engine of the Vnano to your applications. Good luck!

ここまでで説明したVnanoのコマンドラインモードは、
Vnanoのスクリプトエンジンを搭載アプリケーション等に合わせて改造する際に役立つかもしれません。
改造したくなったら、ぜひ活用して試してみてください。




<a id="performances"></a>
## Performances - 演算速度

In addition to the above example code, some benchmarking programs for measuring performances 
are also contained in this repository. Let's execute them in this section.
Please note that, those benchmarking programs measure maximum performances Vnano Engine can perform, 
not effective performances expected for general programs.

このリポジトリ内には、上記のサンプルコード類に加えて、性能計測用のベンチマークプログラムも含まれています。
以下では、実際にそれらを実行してみましょう。
ただし、これらのベンチマークプログラムが測定するのは、Vnanoエンジンが発揮し得る性能の上限値であり、
一般的なプログラムにおいて期待できる実効性能とは異なる事にあらかじめ留意が必要です。

### 64-bit Scalar Operation FLOPS Performance - 64-bitスカラ演算FLOPS性能

"Float64ScalarFlopsBenchmark.java" is a benchmarking program for measuring the peak performance of 
operations of 64-bit floating-point scalar data. The scripting part in this program is as follows:

「Float64ScalarFlopsBenchmark.java」は、
64-bit（倍精度）浮動小数点数のスカラ演算におけるピーク性能を計測するためのベンチマークプログラムです。
このプログラム内での、スクリプト記述部分は以下の通りです：

	String scriptCode =

	"  int LOOP_N = 100*1000*1000;                                      " + 
	"  int FLOP_PER_LOOP = 100;                                         " + 
	"  int TOTAL_FLOP = FLOP_PER_LOOP * LOOP_N;                         " + 
	"                                                                   " + 
	"  double x = 0.0;                                                  " + 
	"  double y = 1.0;                                                  " + 
	"                                                                   " + 
	"  int beginTime = time();                                          " + 
	"                                                                   " + 
	"  for (int i=0; i<LOOP_N; ++i) {                                   " + 
	"    x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y;    " + 
	"    x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y;    " + 
	"    x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y;    " + 
	"    x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y;    " + 
	"    x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y;    " + 
	"    x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y;    " + 
	"    x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y;    " + 
	"    x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y;    " + 
	"    x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y;    " + 
	"    x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y; x+=y;    " + 
	"  }                                                                " + 
	"                                                                   " + 
	"  int endTime = time();                                            " + 
	"  double requiredTime = (endTime - beginTime) / 1000.0;            " + 
	"  double flops = TOTAL_FLOP / requiredTime;                        " + 
	"                                                                   " + 
	"  output(\"OPERATING_SPEED\", flops/(1000.0*1000.0), \"MFLOPS\");  " + 
	"  output(\"REQUIRED_TIME\", requiredTime, \"SEC\");                " + 
	"  output(\"TOTAL_OPERATIONS\", TOTAL_FLOP, \"xFLOAT64_ADD\");      " + 
	"  output(\"OPERATED_VALUE\", x);                                   " ;

How to execute is:

実行方法は：

	javac -encoding UTF-8 Float64ScalarFlopsBenchmark.java
	java -classpath ".;Vnano.jar" Float64ScalarFlopsBenchmark  (for Microsoft Windows)
	java -classpath ".:Vnano.jar" Float64ScalarFlopsBenchmark  (for Linux, etc.)

and an example of results is as follows (it depends on your environment) :

実行結果の例は以下の通りです（環境に依存します）：

	OPERATING_SPEED = 417.6586058555736 [MFLOPS]
	REQUIRED_TIME = 23.943 [SEC]
	TOTAL_OPERATIONS = 10000000000 [xFLOAT64_ADD]
	OPERATED_VALUE = 1.0E10

"MFLOPS" is the unit of floating-point operating speed. 
If 1 mega (1000,000) floating-point operations done per 1 second, it's processing speed is 1MFLOPS.
The above result means that Vnano Engine performed about 417 million floating-point additions (64-bit precision) per second.

上記の結果において、「MFLOPS」は演算速度の単位です。1秒間に1M（100万）回の浮動小数点演算を行った場合に、
その演算速度はちょうど1MFLOPSになります。従って上の結果は、
Vnanoエンジンが概ね1秒間あたり約4億回のペースで浮動小数点加算（64-bit精度）を行った事を表しています。

### 64-bit Vector Operation FLOPS Performance - 64-bitベクトル演算FLOPS性能

"Float64VectorFlopsBenchmark.java" is a benchmarking program for measuring the peak performance of 
operations of 64-bit floating-point vector (array) data. The scripting part in this program is as follows:

「Float64VectorFlopsBenchmark.java」は、
64-bit（倍精度）浮動小数点数のベクトル（配列）演算におけるピーク性能を計測するためのベンチマークプログラムです。
このプログラム内での、スクリプト記述部分は以下の通りです：

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

How to execute is:

実行方法は：

	javac -encoding UTF-8 Float64VectorFlopsBenchmark.java
	java -classpath ".;Vnano.jar" Float64VectorFlopsBenchmark  (for Microsoft Windows)
	java -classpath ".:Vnano.jar" Float64VectorFlopsBenchmark  (for Linux, etc.)

and an example of results is as follows (it depends on your environment) :

実行結果の例は以下の通りです（環境に依存します）：

	OPERATING_SPEED = 4.8389764430687805 [GFLOPS]
	REQUIRED_TIME = 42.323 [SEC]
	TOTAL_OPERATIONS = 204800000000 [xFLOAT64_ADD]
	VECTOR_SIZE = 2048 [x64BIT]
	OPERATED_VALUES = { 1.0E8, 2.0E8, 3.0E8, ... , 2.047E11, 2.048E11 }

1GFLOPS is 1000MFLOPS, so the above result means that Vnano Engine performed 
about 4.8 billion floating-point additions (64-bit precision) per second. 
Note that the peak performance of vector operations greatly depends on 
total sizes of operating data and sizes of L1/L2/L3 caches of your CPU. 
The following graph represents vector-length dependency of performances on this benchmark program.

1GFLPPSは1000MFLOPSであり、従って上記の結果は、
Vnanoエンジンが概ね1秒間あたり48億回のペースで浮動小数点加算（64-bit精度）を行った事を表しています。
ただし、ベクトル演算の実行速度は、演算対象データのサイズと、CPUの1次/2次/3次キャッシュのサイズに大きく依存します。
以下の図は、ベクトルの要素数を横軸として、このベンチマークプログラムでの計測性能値を表したものです。

<div style="background-color:white; width: 700px; height: 612px; text-align:center; background-image: url('./vectorflops.png'); background-repeat: no-repeat; background-size: contain;">
	<img src="https://github.com/RINEARN/vnano/blob/master/vectorflops.png" alt="" width="700" />
</div>





<a id="architecture"></a>
## Architecture - アーキテクチャ

The architecture of Vnano Engine is a commonplace "compiler + VM" type.
The compiler compiles the script code to the intermediate code, and the virtual machine (VM) executes it.
Vnano Engine is composed of some packages, so we will explain roles of them in the following.

Vnanoエンジンは、内部でスクリプトコードを中間コードにコンパイルし、
それを仮想マシン(VM)上で実行する、オーソドックスなアーキテクチャを採用しています。
以下では、Vnanoエンジンを構成する各パッケージの役割について説明します。


<div style="background-color:black; width: 700px; height: 1150px; text-align:center; background-image: url('./architecture.jpg'); background-repeat: no-repeat; background-size: contain;">
	<img src="https://github.com/RINEARN/vnano/blob/master/architecture.jpg" alt="" width="700" />
</div>


### Compiler - コンパイラ

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/compiler">org.vcssl.nano.compiler</a> 
package performs the function as a compiler, 
which compiles script code written in the Vnano to a kind of intermediate code, 
named as "VRIL" code.
VRIL ― Vector Register Intermediate Language ― is a low-level (but readable text format) 
language designed as a virtual assembly code for the VM (Virtual Machine) layer of Vnano Engine.

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/compiler">org.vcssl.nano.compiler</a> 
パッケージは、Vnanoのスクリプトコードを、"VRILコード" と呼ぶ一種の中間コードへと変換する、コンパイラの機能を担います。
VRIL（Vector Register Intermediate Language; ベクトルレジスタ中間言語）は、
VnanoエンジンのVM（仮想マシン）層の単位動作に対応するレベルの低抽象度な命令を提供する、
仮想的なアセンブリ言語です。VRILコードは、実在のアセンブリコードと同様に、人間にとって可読なテキスト形式のコードです。



### Assembler - アセンブラ

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/vm/assembler">org.vcssl.nano.vm.assembler</a>
package performs the function as an assembler, 
to translate VRIL code (text format) into more low level instruction objects 
(referred as "VRIL Instructions" in the above figure)
which are directly executable by the VM layer.

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/vm/assembler">org.vcssl.nano.vm.assembler</a>
パッケージは、テキスト形式のVRILコードを、VnanoのVM層で直接的に実行可能な命令オブジェクト列を含む
"VMオブジェクトコード" へと変換する、アセンブラとしての機能を担います。
この命令オブジェクト列は、上図の中において "VRIL Instructions" として記述されています。



### Processor - プロセッサ

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/vm/processor">org.vcssl.nano.vm.processor</a>
package performs the function as a virtual processor (CPU), 
which executes instruction objects assembled from VRIL code.
The architecture of this virtual processor is a SIMD-based Register Machine (Vector Register Machine).
The implementation code of this virtual processor is simple and may be easy to customize, 
however, its processing speed is not so high.

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/vm/processor">org.vcssl.nano.vm.processor</a>
パッケージは、アセンブラによってVRILコードから変換された命令オブジェクト列を、
逐次的に実行する仮想的なプロセッサ（CPU）としての機能を担います。
この仮想プロセッサは、SIMD演算を基本とする、ベクトルレジスタマシンのアーキテクチャを採用しています。
このパッケージが提供する仮想プロセッサの実装は、単純で改造が比較的容易ですが、その反面、処理速度はあまり速くありません。



### Accelerator - アクセラレータ

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/vm/accelerator">org.vcssl.nano.vm.accelerator</a>
package provides a high-speed (but complicated) implementation of the virtual processor referred above.
Whether you use this component or don't is optional as follows,
and Vnano engine can run even under the condition of that this component is completely disabled.

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/vm/accelerator">org.vcssl.nano.vm.accelerator</a>
パッケージは、上記の仮想プロセッサの、より高速な実装を提供します。半面、実装コードの内容もより複雑になっています。
このコンポーネントを使用するかどうかは、下記のようにオプションで選択できます。
Vnanoエンジンは、このコンポーネントの動作を完全に無効化しても、機能上は欠損なく成立するようにできています。

		// Create an option-map to enable/disable the accelerator (fast version VM).
		// Accelerator（高速版VM）を有効化/無効化するためのオプションマップを用意
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("ACCELERATOR_ENABLED", true);     // Enable  - 有効化
		//optionMap.put("ACCELERATOR_ENABLED", false);  // Disable - 無効化

		// Set the option-map to the script engine of the Vnano
		// オプションマップをVnanoのスクリプトエンジンに設定
		engine.put("___VNANO_OPTION_MAP", optionMap);   // engine: javax.script.ScriptEngine

If you created an instance of the VnanoEngine class directly and are using it 
 ( as <a href="#example-direct">DirectExample.java</a> or <a href="#example-kt">Example.kt</a> ) :

もし（ <a href="#example-direct">DirectExample.java</a> や <a href="#example-kt">Example.kt</a> のように ）VnanoEngineクラスのインスタンスを直接使用している場合は：

		...

		// Set the option-map to the script engine of the Vnano
		// オプションマップをVnanoのスクリプトエンジンに設定
		engine.setOptionMap(optionMap);    // engine: org.vcssl.nano.VnanoEngine

### Memory - メモリ

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/vm/memory">org.vcssl.nano.vm.memory</a>
package performs the function as a virtual memory, to store data for reading and writing from the virtual processor.
Register for storing temporary data are also provided by this virtual memory.
Most instructions of the virtual processor of Vnano are SIMD, so this virtual memory stores data by units of vector (array).
One virtual data address corresponds one vector.

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/vm/memory">org.vcssl.nano.vm.memory</a>
パッケージは、仮想プロセッサから読み書きされるデータを、アドレスに紐づけて保持する、仮想的なメモリとしての機能を担います。
仮想プロセッサが一時的なデータの保持に使用するレジスタも、この仮想メモリが提供します。
先述の通り、Vnanoエンジンの仮想プロセッサはベクトルレジスタマシンのアーキテクチャを採用しているため、
この仮想メモリはデータをベクトル（配列）単位で保持します。即ち、一つのデータアドレスに対して、一つの配列データが紐づけられます。



### Interconnect - インターコネクト

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/interconnect">org.vcssl.nano.interconnect</a>
package performs the function as a component which manages and provides some information shared between multiple components explained above. 
We refer this component as "Interconnect" in the Vnano Engine.
For example, information to resolve references of variables and functions are managed by this interconnect component. 
Bindings to external functions/variables are intermediated by this interconnect component, so plug-ins of 
external functions/variables will be connected to this component.

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/interconnect">org.vcssl.nano.interconnect</a>
パッケージは、これまでに列挙した各コンポーネント間で共有される、いくつかの情報を管理・提供する機能を担います。
この機能を担うコンポーネントを、Vnanoエンジンでは "インターコネクト" と呼びます。
インターコネクトが管理・提供する情報の具体例としては、関数・変数の参照解決のための情報などが挙げられます。
外部変数・外部関数のバインディングも、インターコネクトを介して行われます。そのため、外部変数・外部関数のプラグインは、
Vnanoエンジン内でこのコンポーネントに接続されます。




<a id="language"></a>
## The Vnano as a Language - 言語としての Vnano

The language specification of the Vnano ( = VCSSL nano ) is a small subset of the 
<a href="https://www.vcssl.org/">VCSSL</a> 
which is a programming language having simple C-like syntax, so Vnano also has simple C-like syntax.
Vnano is specialized for embedded use in applications, 
so compared with other programming languages, many features not necessary for the embedded use are omitted 
for making the implementation of the script engine compact.
It is the concept for giving priority to customizability, maintainability, security, portability 
and so on than the functionality.

プログラミング言語としての Vnano ( = VCSSL nano ) の仕様は、その名前の通り、
<a href="https://www.vcssl.org/">VCSSL</a>という言語の仕様の小さなサブセットになっています。
VCSSLはC言語系の単純な文法を持つプログラミング言語であるため，VnanoもまたC言語系の単純な文法を持っています。
Vnanoはアプリケーション組み込み用途に焦点を絞った言語であるため、
一般的なプログラミング言語と比べると、用途的に必要性の低い機能は大幅に削られています。
これはスクリプトエンジンの実装規模をコンパクトに抑える事で、
機能性よりもカスタマイズ性や保守性、セキュリティ、および移植性などを優先的に高めるためです。


<a id="language-data-type"></a>
### Data Types - データ型

Vnano supports only int (=long), float (=double), bool, and string for data-types.

Vnano は、データ型として int (=long)、float (=double)、bool、および string 型のみをサポートしています。

| Type name - 型名 | Description - 説明 | 
| --- | --- |
| int (or long) | The 64-bit signed integer type - 64ビット精度符号付き整数型 |
| float (or double) | The 64-bit floating point number type - 64ビット精度浮動小数点数型 |
| bool | The boolean type - 論理型 |
| string | The character string type - 文字列型 |

Other primitive data types, pointer, struct and class are not supported.
On the other hand, array types of the data types in the above table are supported, 
and you can use it with C-like syntax.

上記以外の基本データ型や、ポインタ、構造体、およびクラスなどはサポートされません。
一方で、上記の表にあるデータ型の配列型はサポートされており、C言語系の記法で使用できます。

However, please note that arrays in the Vnano (and VCSSL) behaves as value types, not reference types or pointers.
The assignment operation (=) of an array behaves as the copy of all values of elements, not the copy of the reference to (address on) the memory.
It is the same for character strings. 
In the Vnano, the "string" type which is the data type to store character strings behaves as the value type, not reference type.
In short, Vnano has no reference types, so all data types in the Vnano are value types.
Therefore, the script engine of the Vnano has no garbage-collection (GC) modules.

ただし、Vnano（および VCSSL）における配列は、ポインタや参照型ではなく、値型として振舞う事に注意してください。
配列の代入演算（=）も、参照の代入ではなく、全要素値のコピー代入になります。
文字列についても同様で、Vnanoで文字列を扱う string 型は、参照型ではなく値型として振舞います。
つまるところ、Vnano に参照型は存在せず、全てのデータ型は値型になっています。
これにより、Vnanoのスクリプトエンジンではガベージコレクション（GC）を省略しています。

By the way, if sizes of arrays at the left-hand and the right-hand of the assignment operation (=) are different, 
the size of the left-hand array will be adjusted to the same size with the right-hand array, 
by re-allocating memory of the left-hand array automatically.

なお、配列に、要素数の異なる配列が代入される場合には、過不足なく全要素のコピーを行うために、
コピー先（代入演算子「=」の左辺）の配列のメモリー領域が自動で再確保され、
コピー元（右辺）と同じ要素数になるように調整されます。


<a id="language-variable"></a>
### Variable Declaration Statements - 変数宣言文

You can describe the variable declaration statements with C-like syntax.

以下のように、C言語系の表記で変数宣言文を記述できます。


<a id="language-variable-scalar"></a>
#### Declaration of scalar variables - スカラ変数の宣言

The following is an example code of declaration statements of scalar variables (non-array variables) :

以下は、スカラ変数（配列ではない普通の変数）を宣言する例のコードです：

	int    i = 1;
	float  f = 2.3;
	bool   b = true;
	string s = "Hello, World !";

	output(i);
	output(f);
	output(b);
	output(s);

The result on <a href="#how-to-use-in-command">the command-line mode</a> is: 

このコードを<a href="#how-to-use-in-command">コマンドラインモード</a>で実行すると、実行結果は：

	1
	2.3
	true
	Hello, World !

However, you can NOT declare multiple variable in 1 statement in the Vnano:

一方でVnanoでは、以下のように一つの文の中で複数の変数を宣言する事はできません：

	(!!! This code does not work - このコードは動作しません !!!)

	int i, j;
	int n = 1, m = 2;


<a id="language-variable-array"></a>
#### Declaration of arrays - 配列宣言

You can declare and use arrays as follows:

配列は以下のように宣言して使用できます：

	int a[8];
	a[2] = 123;
	output(a[2]);

The result on <a href="#how-to-use-in-command">the command-line mode</a> is: 

このコードを<a href="#how-to-use-in-command">コマンドラインモード</a>で実行すると、実行結果は：

	123

However, you can NOT use array initializers in the Vnano:

一方でVnanoでは、以下のような配列初期化子は使用できません：

	(!!! This code does not work - このコードは動作しません !!!)

	int a[8] = { 10, 20, 30, 40, 50, 60, 70, 80 };


<a id="language-control"></a>
### Control Statements - 制御文

In control statements of C-like languages, Vnano supports if / else / for / while / continue / break statements.

C言語系の制御文の中で、Vnano では if / else / for / while / break / continue 文がサポートされています。

<a id="language-control-if-else"></a>
#### if and else statements - if 文と else 文

The folloing is an example code of if and else statements:

以下は if 文と else 文の使用例です：

	int x = 1;
	if (x == 1) {
		output("x is 1.");
	} else {
		output("x is not 1.");
	}

The result is:

実行結果は：

	x is 1.

By the way, in the Vnano, after of if / else / for / while statements must be a block statement {...}.
Therefore, you can NOT write single statement which is not enclosed by braces { } after the if statement as follows:

ところでVnanoでは、if / else / for / while 文の後には必ずブロック文 {...} が続かなければいけません。
従って、以下のように if 文の後に、波括弧 { } で囲まれていない単文を記述する事はできません：

	(!!! This code does not work - このコードは動作しません !!!)

	int x = 1;
	if (x == 1) output("x is 1.");


<a id="language-control-for"></a>
### for statement - for 文

The folloing is an example code of for statement:

以下は for 文の使用例です：

	for (int i=1; i<=5; i++) {
		output("i=" + i);
	}


Please note that braces { } can not be omitted. The result is:

ここでも波括弧 { } は省略できない事に注意してください。実行結果は：

	i=1
	i=2
	i=3
	i=4
	i=5


<a id="language-control-while"></a>
### while statement - while 文

The folloing is an example code of while statement:

以下は while 文の使用例です：

	int a = 500;
	while (0 <= a) {
		output("a=" + a);
		a -= 123;
	}

Please note that braces { } can not be omitted. The result is:

ここでも波括弧 { } は省略できない事に注意してください。実行結果は：

	a=500
	a=377
	a=254
	a=131
	a=8


<a id="language-control-break"></a>
### break statement - break 文

The folloing is an example code of break statement:

以下は break 文の使用例です：

	for (int i=1; i<=10; i++) {
		output("i=" + i);
		if (i == 3) {
			break;
		}
	}

The result is:

実行結果は：

	i=1
	i=2
	i=3

<a id="language-control-continue"></a>
### continue statement - continue 文

The folloing is an example code of continue statement:

以下は continue 文の使用例です：

	for (int i=1; i<=10; i++) {
		if (i % 3 == 0) {
			continue;
		}
		output("i=" + i);
	}

The result is:

実行結果は：

	i=1
	i=2
	i=4
	i=5
	i=7
	i=8
	i=10


<a id="language-expression"></a>
### Expressions - 式

<a id="language-expression-syntax"></a>
#### Syntax elements of expressions - 式の構文要素

The expression is the set of tokens consists of operators, operands, and parentheses ( ), 
where operands are literals or identifiers (variable names and function names).
For example:

式は、演算子とオペランドおよび括弧 ( ) で構成される一連のトークン（字句）列です。
ここでオペランドはリテラルまたは識別子（変数名や関数名）です。例えば：

	(x + 2) * 3;

In the above expression, + and * are operators, x and 2 and 3 are operands, 
( ) are parentheses.
Please note that parentheses ( ) as syntax elements are 
different with the function-call operator ( ... , ... , ... ).
In the Vnano, as the same with the C programming language, 
the symbol of the assignment "=" is an operator, so the following is also expression:

上の式において、 + と * は演算子、x と 2 と 3 はオペランド、そして ( ) は括弧です。
なお、構文要素としての括弧 ( ) は、関数呼び出し演算子 ( ... , ... , ... ) とは別のものである事に注意が必要です。
VnanoではC言語と同様、代入の記号「=」も演算子なので、以下の内容も式になります：

	y = (x + 2) * 3;

An expression alone can be a statement as "expression statement". 
In addition, an expression can be described as a part of other statements, e.g., a condition expression of an if statement.

式は、単独でも「式文」として文となり得ます。加えて、if 文の条件式など、他の種類の文の構成要素にもなります。


<a id="language-expression-operator"></a>
#### Operators - 演算子

The following is the list of operators supported in the Vnano:

Vnano でサポートされている演算子は、以下の一覧の通りです：

| Operators<br>演算子 | Priority<br>優先度 | Syntax<br>構文 | Associativity<br>結合性の左右 | Type of Operands<br>オペランドの型 | Type of Operated Value<br>演算結果の値の型 |
| --- | --- | --- | --- | --- | --- |
| ( ... , ... , ... ) as call | 1000 | multiary | left | any | any |
| [ ... ][ ... ] ... as index | 1000 | multiary | left | int | any |
| ++ | 1000 | postfix | left | int | int |
| -- | 1000 | postfix | left | int | int |
| ++ | 2000 | prefix | right | int | int |
| -- | 2000 | prefix | right | int | int |
| + | 2000 | prefix | right | int | int |
| - | 2000 | prefix | right | int | int |
| ! | 2000 | prefix | right | bool | bool |
| * | 3000 | binary | left | int, float | int, float |
| / | 3000 | binary | left | int, float | int, float |
| % | 3000 | binary | left | int, float | int, float |
| + | 3100 | binary | left | int, float, string | int, float, string |
| - | 3100 | binary | left | int, float | int, float |
| < | 4000 | binary | left | int, float | bool |
| <= | 4000 | binary | left | int, float | bool |
| > | 4000 | binary | left | int, float | bool |
| >= | 4000 | binary | left | int, float | bool |
| == | 4100 | binary | left | any | bool |
| != | 4100 | binary | left | any | bool |
| && | 5000 | binary | left | bool | bool |
| \|\| | 5100 | binary | left | bool | bool |
| = | 6000 | binary | right | any | any |
| *= | 6000 | binary | right | int, float | int, float |
| /= | 6000 | binary | right | int, float | int, float |
| %= | 6000 | binary | right | int, float | int, float |
| += | 6000 | binary | right | int, float, string | int, float, string |
| -= | 6000 | binary | right | int, float | int, float |


The value type (the data-type of the operated value) of binary arithmetic operators (\*, /, %, +, -) 
and compound arithmetic assignment operators (*=, /=, %=, +=, -=) are decided by the following table:

算術演算子（\*, /, %, +, -）および算術複合代入演算子（*=, /=, %=, +=, -=）における値の型（演算された値のデータ型）は、以下の表の通りに決定されます：

| Type of Operand A<br>オペランドAの型 | Type of Operand B<br>オペランドBの型 | Type of Operated Value<br>演算結果の値の型 |
| --- | --- | --- |
| int | int | int |
| int | float | float |
| int | string | string |
| float | int | float |
| float | float | float |
| float | string | string |
| string | int | string |
| string | float | string |
| string | string | string |

Where you can choose the right or the left operand as the operand A (or operand B) freely in the above table.

上の表において、右と左のどちらのオペランドをオペランドA（またはB）に選んでも構いません。


<a id="language-function"></a>
### Functions - 関数

You can declare and call functions in the Vnano script code with C-like syntax. 
However, this script engine does not support recursive calls of functions, 
because allocations of local variables are implemented in very simple way.

Vnanoのスクリプトコード内で、C言語系の記法で関数を宣言し、呼び出す事ができます。
ただし、このスクリプトエンジンでは、ローカル変数が非常に単純な仕組みで実装されているため、
関数の再帰呼び出しには対応していません。

<a id="language-function-scalar"></a>
#### Scalar input/output functions - スカラを引数や戻り値とする関数


The following is an example code of the function of which arguments and the return value is scalar (non-array) values:

以下は、スカラ変数（配列ではない普通の変数）を引数や戻り値とする関数のコード例です：

	int fun(int a, int b) {
		return a + b;
	}

	int v = fun(1, 2);
	output(v);

The result on <a href="#how-to-use-in-command">the command-line mode</a> is: 

このコードを<a href="#how-to-use-in-command">コマンドラインモード</a>で実行すると、実行結果は：

	3


<a id="language-function-array"></a>
#### Array input/output functions - 配列を引数や戻り値とする関数

If you want to return an array, or get arrays as arguments, the following code is an example:

配列を引数や戻り値にしたい場合の例は、以下の通りです：

	int[] fun(int a[], int b[], int n) {
		int c[n];
		for (int i=0; i<n; i++) {
			c[i] = a[i] + b[i];
		}
		return c;
	}

	int x[3];
	x[0] = 0;
	x[1] = 1;
	x[2] = 2;

	int y[3];
	y[0] = 3;
	y[1] = 4;
	y[2] = 5;

	int z[] = fun(x, y, 3);

	output("z[0]=" + z[0] + "\n");
	output("z[1]=" + z[1] + "\n");
	output("z[2]=" + z[2] + "\n");

The result is:

実行結果は：

	z[0]=3
	z[1]=5
	z[2]=7


Please note that, as we mentioned in the section of <a href="#language-data-type">Data Types</a>, 
arrays in the Vnano (and VCSSL) behaves as value types, not reference types or pointers.
By default, assignment operations of arguments and the return value behaves as the copy of all values of elements, not the copy of the reference to (address on) the memory 
(See also: <a href="#language-function-call-by-value">Call by value</a> and  
 <a href="#language-function-call-by-reference">Call by reference</a>).
In addition, the size of the array will be adjusted automatically when an array having different size will copied to it, 
so we omitted to specify size of array declarations in several places in the above code, e.g.: "int a[]", "int b[]", and "int z[] = fun(x, y, 3)".

なお、<a href="#language-data-type">データ型</a>の項目でも触れた通り、
Vnano（および VCSSL）における配列は、ポインタや参照型ではなく、値型として振舞う事に注意してください。
この事により、配列の引数/戻り値の受け渡しは、デフォルトでは参照の代入ではなく、全要素値のコピー代入によって行われます
（<a href="#language-function-call-by-value">値渡し</a> と 
  <a href="#language-function-call-by-reference">参照渡し</a> の項目も参照）。
その際、要素数の異なる配列がコピーされる場合には、過不足なく全要素のコピーを行うために、コピー先（受け取り側）
の配列のメモリー領域が自動で再確保され、コピー元と同じ要素数になるように調整されます。
従って上記のコードでは、いくつかの場所で、配列宣言時に要素数を指定するのを省略しています（ "int a[]"、 "int b[]"、 および "int z[] = fun(x, y, 3)" の箇所 ）。


<a id="language-function-params-and-args"></a>
#### Formal parameters and actual arguments - 仮引数と実引数

The parameter-variable declared in a function declaration like as "a" in the following example is called as "formal parameter". In contrast, the value/variable passed to a function like as "x" in the following example is called as "actual argument".

以下の例の「 a 」のように、関数側で宣言されている引数の事を「仮引数」と呼びます。それに対して、以下の例の「 x 」のように、呼び出し元から関数に渡している引数の事を「実引数」と呼びます。

	void fun(int a) {
		...
	}

	...

	fun(x);


<a id="language-function-call-by-value"></a>
#### Call by value - 引数の値渡し

By default, change of values of formal parameters in functions don't affect to values of actual arguments of caller-side, For example:

デフォルトでは、関数内における仮引数の値の変更は、呼び出し元の実引数の値には反映されません。例えば：

	void fun(int a, int b[]) {
		a = 2;
		b[0] = 10;
		b[1] = 11;
		b[2] = 12;
	}

	int x = 0;
	int y[3];
	y[0] = 0;
	y[1] = 0;
	y[2] = 0;

	fun(x, y);

	output("x = " + x + "\n");
	output("y[0] = " + y[0] + "\n");
	output("y[1] = " + y[1] + "\n");
	output("y[2] = " + y[2] + "\n");

The result is:

実行結果は：

	x = 0
	y[0] = 0
	y[1] = 0
	y[2] = 0

As demonstrated by the above result, actual arguments of caller-side "a" and "b" have not changed although formal parameters "x" and "y" changed in the function "fun". This is because, by default, actual arguments will be simply copied once to formal parameters when the function is called. This behaviour is called as "call-by-value".

上の例の通り、関数「 fun 」内で仮引数「 x 」と「 y 」の値を変更していますが、呼び出し元の実引数「 a 」と「 b 」の値は変化していない事がわかります。これは、デフォルトでの関数の引数の受け渡しが、単純な値のコピーによって、呼び出し時に一度のみに行われるからです。このような引数の渡し方を「値渡し」と呼びます。


<a id="language-function-call-by-reference"></a>
#### Call by reference - 引数の参照渡し

If you want to affect changed values of formal parameters in functions to values of actual arguments of caller-side, describe the symbol "&" before the name of formal parameters in declarations of them. For example:

関数内での仮引数の値の変更を、呼び出し元の実引数の値に反映させたい場合は、仮引数の宣言において、名前の前に「 & 」記号を付加してください。例えば：

	void fun(int &a, int &b[]) {
		a = 2;
		b[0] = 10;
		b[1] = 11;
		b[2] = 12;
	}

	int x = 0;
	int y[3];
	y[0] = 0;
	y[1] = 0;
	y[2] = 0;

	fun(x, y);

	output("x = " + x + "\n");
	output("y[0] = " + y[0] + "\n");
	output("y[1] = " + y[1] + "\n");
	output("y[2] = " + y[2] + "\n");

The result is:

実行結果は：

	x = 2
	y[0] = 10
	y[1] = 11
	y[2] = 12

As demonstrated by the above result, the memory-reference to data of a formal parameter declared with "&" will be shared with reference to data of an actual argument, so after values of formal parameters "x" and "y" in the function "fun" changed, actual arguments "a" and "b" of caller-side also changed to same values with "x" and "y". This behaviour is called as "call-by-reference".

上の例の通り、関数「 fun 」内で仮引数「 x 」と「 y 」の値を変更した結果、呼び出し元の実引数「 a 」と「 b 」も、同じ値に変化した事がわかります。これは、「 & 」を付けて宣言された仮引数のデータへのメモリ参照が、実引数のデータへのメモリ参照と共有されるためです。このような引数の渡し方を「参照渡し」と呼びます。


<a id="plugin"></a>
## Plug-in - プラグイン

In this section, we will explain how to use plug-ins which provide external functions/variables to the script engine of the Vnano.
You can use <a href="#plugin-official">officially developed/provided plug-ins</a>, and you also can develop your own plug-ins.

このセクションでは、Vnanoのスクリプトエンジンに外部関数/変数を提供する、プラグインの使用方法について解説します。プラグインは、<a href="#plugin-official">公式に開発/提供されているもの</a>の他にも、自分で開発する事ができます。

<a id="plugin-external-function-variable"></a>
### External Functions/Variables and Plug-in - 外部関数/変数とプラグイン

The Vnano is the language for executing partial processings on host applications as scripts, 
so you can connect functions and variables of host applications to the script engine, and can access them from script code as so-called "built-in functions/variables".
In the Vnano, We refer them as "external functions/variables". 
In contrast to them, we refer functions and variables declared in the Vnano script code as "internal functions/variables".

Vnanoは、ホストアプリケーション上での部分的な処理をスクリプトとして実行する事に焦点を絞った言語なので、
ホストアプリケーション側の関数や変数を、いわゆる「組み込み関数/変数」としてスクリプトエンジンに接続し、
スクリプトコード内からアクセスする事ができます。
Vnanoでは、それらを「外部関数/変数」と呼びます。
それに対して、これまでのようにスクリプト内で宣言された関数および変数を「内部関数/変数」と呼びます。

All external functions/variables you want to access from the Vnano script code 
are necessary to be implemented on the host application by using Java&reg; (or alternative languages), 
and necessary to be connected to the script engine explicitly.
We call such program (implemented in Java&reg;) which provides external functions/variables to the Vnano as "Plug-in".
The script engine of the Vnano supports multiple plug-in format.
We will implement them in this section.

Vnanoのスクリプトコード内で使用したい全ての外部関数/変数は、
ホストアプリケーション側にJava&reg;言語（またはその代替言語）で実装し、
スクリプトエンジンに明示的に接続する必要があります。
そのように、Vnanoに外部関数/変数を提供するためのJava&reg;言語のプログラムを、プラグインと呼びます。
Vnano用のスクリプトエンジンでは、複数のプラグイン形式がサポートされています。
このセクションでは実際にそれらを実装します。


<a id="plugin-security"></a>
### Point of attention about the security - セキュリティに関する留意点

**PLEASE CONSIDER DEEPLY THE BALANCE BETWEEN THE FUNCTIONALITY AND THE SECURITY BEFORE CONNECTING EXTERNAL FUNCTIONS/VARIABLES TO THE SCRIPT ENGINE EMBEDDED IN THE APPLICATION.**
No external functions and variables are connected to the Vnano script engine by default, 
so script code can not access any information and systems locating outside of the script engine, 
e.g.: files in the PC, commands of the OS, networks, and so on. 
Therefore, from the viewpoint of the security, the default Vnano script engine is a kind of the sandbox.

**はじめに、アプリケーションに組み込まれたスクリプトエンジンに、外部関数/変数を接続する際には、
機能性とセキュリティのバランスについて深く検討を行う事をおすすめします。**
デフォルトでは、Vnanoスクリプトエンジンには外部関数/変数は一切接続されていないため、
スクリプトコード内からスクリプトエンジン外部の情報やシステム（例えば、PC内のファイルや、OSのコマンド、ネットワークなど）にはアクセスできない状態になっています。
つまり、セキュリティの観点からは、デフォルトのVnanoスクリプトエンジンは一種のサンドボックスになっています。

Connecting external functions/variables means making the sandbox weakened, 
or making holes on the sandbox, as the compensation for enhancing functionality of the scripting.
Therefore, at first, we recommend to figure out what kinds of external accesses 
from script code are necessary for the aim of the host-application, 
and to decide whether support them or not by considering deeply 
the balance between the functionality and the security.
**Depending on the kind of the host-application, 
please note that the user of the application may be different person with the author of the script code.**

外部関数/変数を接続する事は、スクリプトの機能性を拡張する代償として、
サンドボックスを弱める、もしくは穴をあける事を意味します。
そのため、まずはスクリプトエンジンを搭載するホストアプリケーション側の目的から見て、
スクリプトにどの程度の外部アクセスが必要になるのかを事前に吟味し、
実際にそれらをサポートするかどうかは、
ホストアプリケーションに要求される機能性とセキュリティのバランスを十分に考慮した上で行う事をおすすめします。
**ホストアプリケーションの種類によっては、スクリプトの記述者と、
アプリケーションのユーザーは必ずしも一致しない事にも留意しておく必要があります。**

If you want to support rich functions on the scripting feature of your application, 
it might be one compromise plan to implement the "security barrier" 
which requesting permissions to the user of the host-application, 
when the external functions which access to securitically critical resources (files, networks, etc.) are invoked.

スクリプト内で豊富な機能を利用できるようにしたい場合は、
ファイルやネットワークなどの、セキュリティ上重要なリソースにアクセスする外部関数が実行される際に、
ユーザーなどに許可を求める、いわゆる「関所」のようなものを実装する事なども、妥協案の一つになるかもしれません。


<a id="plugin-external-variables-synchronization"></a>
### Caution about the Synchronization of Values of External Variables - 外部変数の値の同期タイミングに関する注意

There is an important point about external variables.
That is, 
the changing of values of external variables during the script code is running 
DOES NOT affect to values of them in the Vnano script code.
The script engine of the Vnano loads values of all external variables to the virtual memory 
at the beginning of the execution of the script code, 
and write back values from the virtual memory to all external variables at the end of the execution.
Values of external variables will be synchronized between the host-application-side and script-side only when these two moments.
The aim of this specification is: 
it gives the big advantage for speeding up processings, 
that excluding changings of values on the virtual memory caused by operations from the outside of the script engine.

外部変数には、一つ注意が必要な点があります。
それは、スクリプトの実行中に、ホストアプリケーション側から外部変数の値を変更しても、
効果は無いという点です。
Vnanoのスクリプトエンジンは、スクリプトの実行直前に外部変数の値を仮想メモリーに一括で読み込み、
そしてスクリプトの実行完了時点で、仮想メモリーから外部変数へ値を一括で書き戻します。
つまり、ホストアプリケーション側とスクリプト側とで、外部変数の値が同期されるのは、
実行直前と実行完了時点の2つの瞬間のみです。
この仕様の理由は、実行中に仮想メモリーのデータが外部から変更される事を考慮しない方が、
スクリプトエンジンの高速化において大幅に有利であるためです。

If you want to make it possible to access the host-apprication-side value 
which changes during execution from the script-side, 
please make and connect so-called "setter" and "getter" of the value as external FUNCTIONS,
instead of the external variable.

もしも、スクリプト実行中にホストアプリケーション側で変更され得る値に、
スクリプト内からリアルタイムにアクセスしたい場合は、外部変数ではなく外部関数として、
その値に対する setter と getter を用意して接続してください。


<a id="plugin-class"></a>
### Connecting a Class or an Instance as an Plug-in to Use Its All Methods/Fields as External Functions/Variables - クラスやインスタンスをプラグインとして接続し、その全メソッド/フィールドを外部関数/変数として使用する

From here, we explain various ways to connect external functions/variables by taking "Example.java" in this repository as an example. 
By the way, if you want to connect external functions/variables to the <a href="#how-to-use-in-command">command-line mode</a>, 
implement plug-ins as classes in independent files and comple them, 
and then load them by using "--plugin" option (for details, read explanations in --help option).

ここからは、このリポジトリ内にある Example.java のコードを例にとって、実際に外部関数/変数を接続する色々な方法について解説します。
なお、もし<a href="#how-to-use-in-command">コマンドラインモード</a>に外部変数/外部関数を接続したい場合は、
プラグインを独立なファイルにクラスとして実装してコンパイルした上で、--plugin オプションを使用して読み込んでください（詳細は --help オプションの説明を参照してください）。

The most simple way to connect external functions/variable is to implement them as methods/fields in a class, and then connect the class or its instance to the Vnano engine as a plug-in. 
For example, see the following part in "Example.java":

外部関数/変数を接続する最も簡単な方法は、それらを適当なクラスのメソッド/フィールドとして実装し、そのクラスやインスタンスをそのままプラグインとしてVnanoエンジンに接続する事です。
例えば、Example.java を見てみると：

		(Example.java)

		// A class which provides a field/method accessed from the script as external functions/variables.
		// スクリプト内から外部変数・外部関数としてアクセスされるフィールドとメソッドを提供するクラス
		public class ExamplePlugin {
			public int LOOP_MAX = 100;
			public void output(int value) {
				System.out.println("Output from script: " + value);
			}
		}

		public static void main(String[] args) {

			...

			// Connect methods/fields of an instance of ExamplePlugin class as external functions/variables.
			// ExamplePluginクラスのインスタンスのメソッド・フィールドを外部関数・変数として接続
			ExamplePlugin examplePlugin = new Example().new ExamplePlugin();
			engine.put("ExamplePlugin", examplePlugin);
			
			...

In the above example, all public methods/fields of an instance "examplePlugin" of the "ExamplePlugin" class will be connected as external functions/variables.
In general, behaviour/values of methods/fields depend on the instance of the class to which they are belong. 
However, if methods/fields are declared as static (are not depending on the state of the instance), 
there is no need to instantiate the class for using them.
Actually, in the above code, "output" method and "LOOP_MAX" field do not depend on 
the state of the instance of "ExamplePlugin" class.
Therefore, we can append "static" to declarations of them, and connect the class instead of an instance as follows:


上記の例では、「ExamplePlugin」クラスのインスタンス「examplePlugin」の全てのメソッド/フィールドが、外部関数/変数として接続されます。
一般に、メソッドの振る舞いやフィールドの値は、所属するクラスのインスタンスの状態に依存します。
一方で、メソッドやフィールドが static として宣言されている（インスタンスの状態に依存しない）場合、それらを使用するのにインスタンス化は不要です。
実際に上の例のコードでは、output メソッドと LOOP_MAX フィールドはインスタンスの状態に依存していないため、
宣言に static を付加し、以下のようにインスタンスの代わりにクラスを接続する事ができます：

		(Example.java, modified code - 書き換えたコード)

		// A class which provides a field/method accessed from the script as external functions/variables.
		// スクリプト内から外部変数・外部関数としてアクセスされるフィールドとメソッドを提供するクラス
		public static class ExamplePlugin {
			public static int LOOP_MAX = 100;
			public static void output(int value) {
				System.out.println("Output from script: " + value);
			}
		}

		public static void main(String[] args) {

			...

			// Connect static methods/fields of ExamplePlugin as external functions/variables.
			// ExamplePluginクラスのstaticメソッド・フィールドを外部関数・変数として接続
			engine.put("ExamplePlugin", ExamplePlugin.class);
			
			...




<a id="plugin-methods-and-fields"></a>
### Connecting a Method/Field as an External Function/Variable Individually - 個々のメソッド/フィールドを個別に外部関数/変数として接続する


You can connect each methods and fields individually, by using reflection API：

リフレクションAPIを介して、各メソッド/フィールドを個別に接続する事もできます：

		(Example.java, modified code - 書き換えたコード)

		// A class which provides a field/method accessed from the script as external functions/variables.
		// スクリプト内から外部変数・外部関数としてアクセスされるフィールドとメソッドを提供するクラス
		public class ExamplePlugin {
			public int LOOP_MAX = 100;
			public void output(int value) {
				System.out.println("Output from script: " + value);
			}
		}

		public static void main(String[] args) {

			...

			// Connect each methods and fields individually by using reflection API.
			// リフレクションAPIを介して、各メソッド/フィールドを個別に接続
			try {
				Field loopMaxField  = ExamplePlugin.class.getField("LOOP_MAX");
				Method outputMethod = ExamplePlugin.class.getMethod("output",int.class);
				ExamplePlugin examplePlugin = new Example().new ExamplePlugin();

				engine.put("LOOP_MAX",    new Object[]{ loopMaxField, examplePlugin } );
				engine.put("output(int)", new Object[]{ outputMethod, examplePlugin } );

			} catch (NoSuchFieldException | NoSuchMethodException e){
				System.err.println("Method/field not found.");
				e.printStackTrace();
				return;
			}

			...


In the above code, we are getting "output" method and "LOOP_MAX" field of "ExamplePlugin" class by using reflection, 
and then connecting them by using the "put" method of the script engine.
As mentioned above, in general, behaviour/values of methods/fields depend on the instance of the class to which they are belong. Therefore, in the above code, we are packing the method/field and an instance of the "ExamplePlugin" class by Object[] { ... } and connecting it.
On the other hand, if methods/fields are declared as static (are not depending on the state of the instance), 
you can connect them more simply:


上記のコードでは、まず「ExamplePlugin」クラスに属する「output」メソッドと「LOOP_MAX」フィールドをリフレクションで取得し、
そしてスクリプトエンジンの put メソッドを使用して、それらを接続しています。
既に述べたように、一般にメソッドの振る舞いやフィールドの値は、所属するクラスのインスタンスの状態に依存します。
従って上では、メソッド/フィールドとExamplePluginクラスのインスタンスを、Object[]{ ... } でパックして接続しています。
一方で、メソッドやフィールドが static として宣言されている（インスタンスの状態に依存しない）場合、
以下のように、より単純に接続できます：


		(Example.java, modified code - 書き換えたコード)

		// A class which provides a field/method accessed from the script as external functions/variables.
		// スクリプト内から外部変数・外部関数としてアクセスされるフィールドとメソッドを提供するクラス
		public static class ExamplePlugin {
			public static int LOOP_MAX = 100;
			public static void output(int value) {
				System.out.println("Output from script: " + value);
			}
		}

		public static void main(String[] args) {

			...

			// Connect each methods and fields individually by using reflection API.
			// リフレクションAPIを介して、各メソッド/フィールドを個別に接続
			try {
				engine.put("LOOP_MAX",    ExamplePlugin.class.getField("LOOP_MAX") );
				engine.put("output(int)", ExamplePlugin.class.getMethod("output",int.class) );

			} catch (NoSuchFieldException | NoSuchMethodException e){
				System.err.println("Method/field not found.");
				e.printStackTrace();
				return;
			}

			...



<a id="plugin-interfaces"></a>
### Implementation of External Functions and Variables by Using Low-Overhead Plug-in Interfaces - 低オーバーヘッドのプラグイン用インターフェースを用いて外部関数や外部変数を実装する

!!! CAUTION: Specifications of plug-in interfaces we use in this section are not fixed yet,
so they may change before the release of Ver.1.0.0 of the Vnano.  !!!

!!! 注意: このセクションで扱うプラグインインターフェースの仕様はまだ完全には確定していないため、
Vnano の Ver.1.0.0 のリリースまでは細部が変更される可能性があります。 !!!

To connect methods/fields of a class as external functions/variables is easy, as we demonstrated in above sections.
However, it has a demerit that they have heavy overhead costs to access from the script code.
If you want to reduce extra processing costs as far as possible, you can implement external functions/variables by using low-overhead plug-in interfaces supported by the Vnano engine.
This way is especially appropriate to provide functions which are called from high-speed loops in the script code.
Interfaces to implement external functions/variables plug-ins are defined as 
"<a href="https://github.com/RINEARN/vnano/blob/master/src/org/vcssl/connect/ExternalFunctionConnectorInterface1.java">org/vcssl/connect/ExternalFunctionConnectorInterface1.java (XFCI1)</a>"
and 
"<a href="https://github.com/RINEARN/vnano/blob/master/src/org/vcssl/connect/ExternalVariableConnectorInterface1.java">org/vcssl/connect/ExternalVariableConnectorInterface1.java (XVCI1)</a>".
Let's implement them:

これまでのセクションで示した例の通り、クラスに実装したメソッド/フィールドを外部関数/変数として接続するのは、比較的手軽です。
一方で、そうして接続した外部関数/変数は、スクリプト側から使用する際に、処理のオーバーヘッドコストが大きいというデメリットもあります。
そのような余分な処理コストを可能な限り削りたい場合は、外部関数/変数を、Vnanoエンジンでサポートされている低オーバーヘッドなプラグイン用インターフェースを用いて実装する事もできます。
これは、特にスクリプトコード内で高速に回るループ内などから呼び出される関数を提供する場合に有効です。
外部関数および外部変数を実装するためのインターフェースは、それぞれ
"<a href="https://github.com/RINEARN/vnano/blob/master/src/org/vcssl/connect/ExternalFunctionConnectorInterface1.java">org/vcssl/connect/ExternalFunctionConnectorInterface1.java (XFCI1)</a>"
および 
"<a href="https://github.com/RINEARN/vnano/blob/master/src/org/vcssl/connect/ExternalVariableConnectorInterface1.java">org/vcssl/connect/ExternalVariableConnectorInterface1.java (XVCI1)</a>"
として定義されています。実際に実装してみましょう：

	(Example.java, modified codde - 書き換えたコード)

	...
	import org.vcssl.connect.ExternalFunctionConnectorInterface1;
	import org.vcssl.connect.ExternalVariableConnectorInterface1;
	import org.vcssl.connect.ConnectorException;

	public class Example {

		// A XFCI1 Plug-In which provides the external function "output(int)".
		// 外部関数 output(int) を提供するXFCI1形式のプラグイン
		public class OutputFunction implements ExternalFunctionConnectorInterface1 {

			@Override
			public String getFunctionName() { return "output"; }
			@Override
			public boolean hasParameterNames() { return true; }
			@Override
			public String[] getParameterNames() { return new String[]{ "value" }; }
			@Override
			public Class<?>[] getParameterClasses() { return new Class<?>[]{ int.class }; } 
			@Override
			public boolean[] getParameterClassArbitrarinesses() { return new boolean[]{ false }; }
			@Override
			public boolean[] getParameterRankArbitrarinesses() { return new boolean[]{ false }; }
			@Override
			public boolean[] getParameterConstantnesses() { return new boolean[]{ false }; }
			@Override
			public boolean[] getParameterReferencenesses() { return new boolean[]{ false }; }
			@Override
			public boolean isParameterCountArbitrary() { return false; }
			@Override
			public boolean hasVariadicParameters() { return false; }
			@Override
			public Class<?> getReturnClass(Class<?>[] parameterClasses) { return Void.class; }
			@Override
			public void initializeForConnection(Object engineConnector) { }
			@Override
			public void finalizeForDisconnection(Object engineConnector) { }
			@Override
			public void initializeForExecution(Object engineConnector) { }
			@Override
			public void finalizeForTermination(Object engineConnector) { }

			@Override
			public boolean isDataConversionNecessary() { return true; }

			@Override
			public Object invoke(Object[] arguments) throws ConnectorException {
				int value = (int)(arguments[0]);
				System.out.print(value);
				return null;
			}
		}

		// A XVCI1 Plug-In which provides the external variable "LOOP_MAX".
		// 外部変数 LOOP_MAX を提供するXVCI1形式のプラグイン
		public class LoopMaxVariable implements ExternalVariableConnectorInterface1 {

			private int value = 100;

			@Override
			public String getVariableName() { return "LOOP_MAX"; }
			@Override
			public Class<?> getDataClass() { return int.class; }
			@Override
			public boolean isConstant() { return false; }
			@Override
			public void initializeForConnection(Object engineConnector) { }
			@Override
			public void finalizeForDisconnection(Object engineConnector) { }
			@Override
			public void initializeForExecution(Object engineConnector) { }
			@Override
			public void finalizeForTermination(Object engineConnector) { }

			@Override
			public boolean isDataConversionNecessary() { return true; }
		
			@Override
			public Object getData() throws ConnectorException {
				return (Integer)this.value;
			}

			@Override
			public void getData(Object dataContainer) throws ConnectorException {
				// This method is for the case of the data conversion is disabled.
			}

			@Override
			public void setData(Object data) throws ConnectorException {
				this.value = (Integer)data;
			}
		}
	
		public static void main(String[] args) {
	
			...	

			// Connect plug-ins to the script engine as an external function/variable.
			// プラグインを外部関数・変数としてスクリプトエンジンに接続
			ExternalVariableConnectorInterface1 loopMaxVariable = new Example2().new LoopMaxVariable();
			ExternalFunctionConnectorInterface1 outputFunction = new Example2().new OutputFunction();
			engine.put("LOOP_MAX",    loopMaxVariable);
			engine.put("output(int)", outputFunction);
			...


The above code is a most simple example to implement XFCI1/XVCI1 plug-ins.
Overhead costs of accessings to external functions/variables provided by these plug-ins are relatively light, compared with accessing costs to methods/fields.
However, these implementations have still heavy overhead costs for the automatic data-type conversions 
(we enabled it in the above example code for simplicity)
between the inside and the outside of the script engine.
To reduce overhead costs of these plug-ins as far as possible, 
you can disable the automatic data-type conversions, 
although in such case it is necessary to handle data container objects of the script engine directly.

以上のコードが、XFCI1/XVCI1形式のプラグインを実装する、最も簡単な例です。
この例で実装したプラグインは、メソッド/フィールドを接続するよりは、いくらかオーバーヘッドの少ない外部関数/変数を提供します。
一方で上の例の実装では、スクリプトエンジンの内外の境界で、
自動でデータ型の変換を行う機能を有効にしているため、そこでまだ比較的大きなオーバーヘッドが発生します。
オーバーヘッドを可能な限り削りたい場合のために、上で述べた自動でのデータ型変換機能を無効にする事もできます。
ただしその場合、スクリプトエンジン内部で使用しているデータコンテナのオブジェクトを、プラグイン側でも直接操作する必要があります。

Therefore, to disable the automatic data-type conversions, 
it is required that you grasp how the script engine store data in the container object.
In addition, please note that it deeply depends on the implementation of the script engine 
so it may vary in the future. 
The source code of the data container class used in the script engine of the Vnano is "<a href="https://github.com/RINEARN/vnano/blob/master/src/org/vcssl/nano/vm/memory/DataContainer.java">src/org/vcssl/nano/vm/memory/DataContainer.java</a>", 
and this class is an implementation of a interface defined as "<a href="https://github.com/RINEARN/vnano/blob/master/src/org/vcssl/connect/ArrayDataContainerInterface1.java">src/org/vcssl/connect/ArrayDataContainerInterface1.java (ADCI1)</a>".
You can handle data container objects through APIs defined as this interface, 
to reduce dependency on the implementation of the script engine as much as possible.
About data-types of data in the data container, see the later section: 
"<a href="#plugin-correspondence-of-data-types">The Correspondence of the Data Type Between the Vnano and the Data Container</a>".

そのため、自動でのデータ型変換機能を無効化するためには、
スクリプトエンジンがどのようにデータをコンテナに格納しているかについて、ある程度把握する必要があります。
加えて、その内容は将来的なスクリプトエンジンの実装次第で変化するかもしれない事にも留意しておく必要があります。
Vnanoのスクリプトエンジン内部でデータを格納するコンテナのソースコードは
<a href="https://github.com/RINEARN/vnano/blob/master/src/org/vcssl/nano/vm/memory/DataContainer.java">src/org/vcssl/nano/vm/memory/DataContainer.java</a>
ですが、処理系に対するプラグインの依存度をできるだけ軽減するため、
このクラスは
<a href="https://github.com/RINEARN/vnano/blob/master/src/org/vcssl/connect/ArrayDataContainerInterface1.java">src/org/vcssl/connect/ArrayDataContainerInterface1.java (ADCI1)</a> 
として定義されるインターフェースを実装しています。
従ってプラグイン側では、このインターフェースのメソッドを通してデータコンテナを操作します。
データコンテナ内におけるデータの型については、後の
「 <a href="#plugin-correspondence-of-data-types">Vnano内とデータコンテナ内でのデータ型の対応関係</a> 」のセクションに記載しています。


Let's implement. The following is an example code:

実際に実装してみましょう。コードの例は以下の通りです：


	(Example.java, modified codde - 書き換えたコード)

	...
	import org.vcssl.connect.ExternalFunctionConnectorInterface1;
	import org.vcssl.connect.ExternalVariableConnectorInterface1;
	import org.vcssl.connect.ConnectorException;
	import org.vcssl.connect.ArrayDataContainerInterface1;

	public class Example {

		// A XFCI1 Plug-In which provides the external function "output(int)".
		// 外部関数 output(int) を提供するXFCI1形式のプラグイン
		public class OutputFunction implements ExternalFunctionConnectorInterface1 {

			@Override
			public String getFunctionName() { return "output"; }
			@Override
			public boolean hasParameterNames() { return true; }
			@Override
			public String[] getParameterNames() { return new String[]{ "value" }; }
			@Override
			public Class<?>[] getParameterClasses() { return new Class<?>[]{ int.class }; } 
			@Override
			public boolean[] getParameterClassArbitrarinesses() { return new boolean[]{ false }; }
			@Override
			public boolean[] getParameterRankArbitrarinesses() { return new boolean[]{ false }; }
			@Override
			public boolean[] getParameterConstantnesses() { return new boolean[]{ false }; }
			@Override
			public boolean[] getParameterReferencenesses() { return new boolean[]{ false }; }
			@Override
			public boolean isParameterCountArbitrary() { return false; }
			@Override
			public boolean hasVariadicParameters() { return false; }
			@Override
			public Class<?> getReturnClass(Class<?>[] parameterClasses) { return Void.class; }
			@Override
			public void initializeForConnection(Object engineConnector) { }
			@Override
			public void finalizeForDisconnection(Object engineConnector) { }
			@Override
			public void initializeForExecution(Object engineConnector) { }
			@Override
			public void finalizeForTermination(Object engineConnector) { }

			@Override
			public boolean isDataConversionNecessary() { return false; }
		
			@Override
			public Object invoke(Object[] arguments) throws ConnectorException {
				
				// check the type of the data container.
				// データコンテナの型を確認
				if (!(arguments instanceof ArrayDataContainerInterface1[])) {
					throw new ConnectorException(
						"The type of the data container is not supported by this plug-in."
					);
				}

				// When the data conversion is disabled,
				// the element [0] is to contain the return value, so the element [1] is the first argument.
				// データ変換が無効化されている場合、[0]番要素は戻り値格納用なので、[1]番要素が最初の引数
				@SuppressWarnings("unchecked")
				ArrayDataContainerInterface1<long[]> outputArgContainer = (ArrayDataContainerInterface1<long[]>)(arguments[1]);
				long[] outputArgData = outputArgContainer.getData();

				// print the value of the argument (behaviour of the output function).
				// 引数の内容を表示（output関数の動作）
				System.out.print(outputArgData[ outputArgContainer.getOffset() ]);

				return null;
			}
		}

		// A XVCI1 Plug-In which provides the external variable "LOOP_MAX".
		// 外部変数 LOOP_MAX を提供するXVCI1形式のプラグイン
		public class LoopMaxVariable implements ExternalVariableConnectorInterface1 {

			private long[] data = new long[]{ 100l };
		
			@Override
			public String getVariableName() { return "LOOP_MAX"; }
			@Override
			public Class<?> getDataClass() { return int.class; }
			@Override
			public boolean isConstant() { return false; }
			@Override
			public void initializeForConnection(Object engineConnector) { }
			@Override
			public void finalizeForDisconnection(Object engineConnector) { }
			@Override
			public void initializeForExecution(Object engineConnector) { }
			@Override
			public void finalizeForTermination(Object engineConnector) { }

			@Override
			public boolean isDataConversionNecessary() { return false; }

			@Override
			public Object getData() throws ConnectorException {
				// This method is for the case of the data conversion is enabled.
				return null;
			}

			@Override
			public void getData(Object dataContainer) throws ConnectorException {
				
				// check the type of the data container.
				// データコンテナの型を確認
				if (!(dataContainer instanceof ArrayDataContainerInterface1)) {
					throw new ConnectorException(
						"The type of the data container is not supported by this plug-in."
					);
				}
				@SuppressWarnings("unchecked")
				ArrayDataContainerInterface1<long[]> adci1Container = (ArrayDataContainerInterface1<long[]>)dataContainer;

				// store data to return it (0 is the array index at which the value is stored).
				// 戻り値用コンテナにデータを格納する (0はdata配列内での値の格納インデックス)
				adci1Container.setData(this.data, 0);
			}
		
			@Override
			public void setData(Object dataContainer) throws ConnectorException {
				
				// check the type of the data container.
				// データコンテナの型を確認
				if (!(dataContainer instanceof ArrayDataContainerInterface1)) {
					throw new ConnectorException(
						"The type of the data container is not supported by this plug-in."
					);
				}
				ArrayDataContainerInterface1 adci1Container = (ArrayDataContainerInterface1)dataContainer;
				Object targetData = adci1Container.getData();
			
				// check the data-type of data contained in the data container.
				// データコンテナに格納されているデータの型を確認
				if (targetData == null || !(targetData instanceof long[])) {
					throw new ConnectorException(
						"The data-type is not compatible for this plug-in."
					);
				}
			
				// set data to the field of this instance.
				// このインスタンスのフィールドにデータをセットする
				long[] longTargetData = (long[])targetData;
				this.data[0] = longTargetData[ adci1Container.getOffset() ];
			}
		}
	
		public static void main(String[] args) {
	
			...	

			// Connect plug-ins to the script engine as an external function/variable.
			// プラグインを外部関数・変数としてスクリプトエンジンに接続
			ExternalVariableConnectorInterface1 loopMaxVariable = new Example2().new LoopMaxVariable();
			ExternalFunctionConnectorInterface1 outputFunction = new Example2().new OutputFunction();
			engine.put("LOOP_MAX",    loopMaxVariable);
			engine.put("output(int)", outputFunction);
			...


Implementation of these plug-ins are little complicated, but overhead costs of accessings to external functions/variables provided by them are light as far as possible.

この例で実装したプラグインは、コード内容は少し複雑ですが、その代わりとして、可能な限りオーバーヘッドの少ない外部関数/変数を提供するものになっています。

For external variables, as we described at the top of this chapter, 
values of them will be synchronized between the host-application-side (plug-in-side) and script-side 
ONLY at the beginning and end of the execution of the script code, 
so it rarely has advantages to handle data container objects directly 
in the plug-in implementations of XVCI1 to reduce overhead costs of accessings
(however, there are advantages when you can't ignore overhead costs even for few times of conversions, 
for example: when you want to connect extremely large size arrays as external variables.).

外部変数については、先に述べた通り、スクリプト内と値が同期されるのは実行開始時/終了時のたった2回のみです。
そのため普通は、上のようにXVCI1プラグイン内で、
処理系依存のデータコンテナを直接操作してまでオーバーヘッドを削るメリットはそれほどありません
（非常に大きな配列データを外部変数とする場合など、数回でも軽視できないコストが発生する場合にはメリットがあります）。

In the contrast, for external functions, 
overhead costs will be burdens every time for each callings from the script code, 
so if those functions will be called frequently, it has big merit to reduce overhead costs of callings.
Therefore, although it is little complicated way, 
sometimes it gives great advantage to handle data container objects directly 
in the plug-in implementations of XFCI1, as the above example code.

一方で外部関数については、スクリプト内から呼び出す度に毎回オーバーヘッドが影響するため、
呼び出し頻度によってはそのオーバーヘッドがスクリプトの処理速度に大きく影響します。
従って、多少面倒な方法ではありますが、上記のようにXFCI1プラグイン内で処理系依存のデータコンテナを直接操作する事は、それに見合うだけの大きなメリットを生む場面がそれなりに見込めます。


<a id="plugin-correspondence-of-data-types"></a>
### The correspondence of the the data type between the Vnano and the data container - Vnano内とデータコンテナ内でのデータ型の対応関係

The correspondence table between data types in the Vnano script code and 
data types in data containers are as follows:

Vnanoのスクリプト内でのデータ型と、上記で扱ったデータコンテナ内でのデータ型の対応関係は、以下の表の通りです：

| The Data Type in the Vnano | The Data Type in the Data Container (Java&reg;) |
| --- | --- |
| int (or long) | long[ ? ] |
| float (or double) | doube[ ? ] |
| bool | boolean[ ? ] |
| string | String[ ? ] |
| int[ N ] | long[ N ] |
| float[ N ] | doube[ N ] |
| bool[ N ] | boolean[ N ] |
| string[ N ] | String[ N ] |
| int[ N1 ][ N2 ] | long[ N1 * N2 ] |
| float[ N1 ][ N2 ] | doube[ N1 * N2 ] |
| bool[ N1 ][ N2 ] | boolean[ N1 * N2 ] |
| string[ N1 ][ N2 ] | String[ N1 * N2 ] |
| int[ N1 ][ N2 ][ N3 ] | long[ N1 * N2 * N3 ] |
| float[ N1 ][ N2 ][ N3 ] | double[ N1 * N2 * N3 ] |
| bool[ N1 ][ N2 ][ N3 ] | boolean[ N1 * N2 * N3 ] |
| string[ N1 ][ N2 ][ N3 ] | String[ N1 * N2 * N3 ] |
| ... | ... |

Where "[ ? ]" in the above table means that the size of the array is undetermined, 
but the value is stored as an element with the index which is gotten by using getOffset() method 
of the data container.
By the way, a multi-dimentional array in the Vnano script code will be stored in a data container 
as a 1D array. The correspondence between indices of those arrays is:

ここで上の表の「 [ ? ] 」は、配列の長さが不確定である事を示すものですが、
対象の値は、データコンテナの getOffset() メソッドが返すインデックスが指す要素として格納されています。
また、上の表の後半部分の通り、Vnano内における多次元配列は、データコンテナ内では1次元化された配列として格納されています。両者の配列間のインデックス対応は、以下の式の通りです：

	arrayInDataContainer[ dcIndex ] = arrayInVnano[ vnanoIndex1 ][ vnanoIndex2 ][ vnanoIndex3 ]
	dcIndex = N3*N2*vnanoIndex1 + N3*vnanoIndex2 + vnanoIndex3

where the symbol "=" means the mathematical equal, not the assignment operator.

ここで「 = 」は代入演算子ではなく、数学的な等号の意味で用いています。




<a id="plugin-pack"></a>
### Packing of Multiple External Functions/Variables as a Plug-in - 複数の外部関数/変数プラグインを1つのプラグインにまとめる

By using 
"<a href="https://github.com/RINEARN/vnano/blob/master/src/org/vcssl/connect/ExternalNamespaceConnectorInterface1.java">org/vcssl/connect/ExternalNamespaceConnectorInterface1.java (XNCI1)</a>" 
interface, you can pack multiple plug-ins of external functions/variables into one plug-in:

複数の外部関数/変数プラグインを1つのプラグインにまとめたい場合は、
"<a href="https://github.com/RINEARN/vnano/blob/master/src/org/vcssl/connect/ExternalNamespaceConnectorInterface1.java">org/vcssl/connect/ExternalNamespaceConnectorInterface1.java (XNCI1)</a>" 
形式のインターフェースを用います：

	(in XnciExamplePlugin.java)

	import org.vcssl.connect.ExternalNamespaceConnectorInterface1;
	import org.vcssl.connect.ExternalFunctionConnectorInterface1;
	import org.vcssl.connect.ExternalVariableConnectorInterface1;
	import org.vcssl.connect.ConnectorException;

	public class XnciExamplePlugin implements ExternalNamespaceConnectorInterface1 {

		XnciExamplePlugin instance = new XnciExamplePlugin();


		// XFCI1 plug-ins for external functions.
		// XFCI1形式の外部関数プラグイン（複数）
		public class FunctionPluginA implements ExternalFunctionConnectorInterface1 {
			...
		}
		public class FunctionPluginB implements ExternalFunctionConnectorInterface1 {
			...
		}
		public class FunctionPluginC implements ExternalFunctionConnectorInterface1 {
			...
		}

		// XVCI1 plug-ins for external variables.
		// XVCI1形式の外部変数プラグイン（複数）
		public class VariablePluginA implements ExternalVariableConnectorInterface1 {
			...
		}
		public class VariablePluginB implements ExternalVariableConnectorInterface1 {
			...
		}
		public class VariablePluginC implements ExternalVariableConnectorInterface1 {
			...
		}


		// A method returns all XFCI plug-ins for providing exteral functions.
		// 全てのXFCI1形式の外部関数プラグインを一括提供するメソッド
		@Override
		public ExternalFunctionConnectorInterface1[] getFunctions() {
			return new ExternalFunctionConnectorInterface1[]{
				this.instance.new FunctionPluginA(),
				this.instance.new FunctionPluginB(),
				this.instance.new FunctionPluginC(),
			};
		}

		// A method returns all XVCI plug-ins for providing exteral variables.
		// 全てのXFCI1形式の外部関数プラグインを一括提供するメソッド
		@Override
		public ExternalVariableConnectorInterface1[] getVariables() {
			return new ExternalVariableConnectorInterface1[]{
				this.instance.new VariablePluginA(),
				this.instance.new VariablePluginB(),
				this.instance.new VariablePluginC(),
			};
		}


		@Override
		public String getNamespaceName() {
			return "XnciExamplePlugin";
		}
		@Override
		public void preInitializeForConnection(Object engineConnector) { }
		@Override
		public void postInitializeForConnection(Object engineConnector) { }
		@Override
		public void preFinalizeForDisconnection(Object engineConnector) { }
		@Override
		public void postFinalizeForDisconnection(Object engineConnector) { }
		@Override
		public void preInitializeForExecution(Object engineConnector) { }
		@Override
		public void postInitializeForExecution(Object engineConnector) { }
		@Override
		public void preFinalizeForTermination(Object engineConnector) { }
		@Override
		public void postFinalizeForTermination(Object engineConnector) { }
	}


and connect this plug-in to the script engine as follows:

これをスクリプトエンジンに接続するには：

	(in Example.java, modified codde - 書き換えたコード)

	...

		public static void main(String[] args) {
	
			...	

			// Connect the plug-in to the script engine.
			// プラグインをスクリプトエンジンに接続
			XnciExamplePlugin plugin = new XnciExamplePlugin();
			engine.put("XnciExamplePlugin", plugin);
			...


As the above, you can connect multiple functions/variables to the script engine very easily, if you pack them in one plug-in.
In addition, it help you to specify briefly the plug-in in a setting file of a software supporting Vnano (e.g. <a href="https://github.com/RINEARN/rinearn-processor-nano">RINEARN Processor nano</a>).

上記のように、複数の外部関数/変数プラグインを1つのプラグインにまとめると、スクリプトエンジンへの接続が手軽になります。
また、Vnanoを使用するソフトウェア（ 例えば<a href="https://github.com/RINEARN/rinearn-processor-nano">リニアンプロセッサー nano</a> など ）において、設定ファイルでプラグインを指定する際などにも、手軽に済むようになります。


<a id="plugin-official"></a>
### Use Official Plug-ins - 公式プラグインを利用する

Some official plug-ins are being developed/provided on the following repository:

以下のリポジトリで、いくつかの公式プラグインが開発/提供されています：

* <a href="https://github.com/RINEARN/vnano-plugin">https://github.com/RINEARN/vnano-plugin</a>

For building steps, read the README on the above repository. 
When the building of source code of official plug-ins succeeded, "plugin" folder will be generated, 
so put it in the working folder.

ビルド方法については、上記リポジトリのREADMEをご参照ください。
プラグインのソースコードのビルドが完了すると、「 plugin 」フォルダが生成されるので、
それを作業フォルダに配置してください。

For using above built plug-ins on the command-line mode, use "--plugin" option and "--pluginDir" option as follows:

ビルドしたプラグインをコマンドラインモードで使用するには、以下の例のように --plugin オプションと --pluginDir オプションを使用して読み込みます：

	java -jar Vnano.jar --pluginDir <PlugInFolder> --plugin <PlugInClass> <ScriptName>

For example:

例えば：

	java -jar Vnano.jar --pluginDir ./plugin --plugin org.vcssl.nano.plugin.calc.xci1.ScientificCalculatorPlugin Example.vnano

where "Example.vnano" is a script in which features provided by the plugin are used.

ここで「 Example.vnano 」は、プラグインの機能を使用する適当なスクリプトです。


On the other hand, if you use built plug-ins on the Vnano Engine embedded in your application, 
create instances of plug-ins and connect it as follows in code of your application (Example.java):

一方、ビルドしたプラグインを、アプリケーション内に組み込んだVnanoエンジンに接続して使用したい場合は、
例えば「 Example.java 」内で以下のようにインスタンスを生成して接続するように記述します。

	// Connect the lugin to the script engine.
	// プラグインをスクリプトエンジンに接続
	Object plugin = new org.vcssl.nano.plugin.calc.xci1.ScientificCalculatorPlugin();
	engine.put("ScientificCalculatorPlugin", plugin);               // engine: ScriptEngine
	// engine.connectPlugin("ScientificCalculatorPlugin", plugin);  // engine: VnanoEngine

Then compile/execute with adding path of plugin-folder to the classpath as follows.
If you are using Microsoft&reg; Windows&reg;:

そして、以下のようにプラグインのフォルダをクラスパスに追加しつつコンパイル/実行します。
Microsoft Windows&reg; をご使用の場合は：

	javac -classpath .;Vnano.jar;./plugin Example.java
	java -classpath .;Vnano.jar;./plugin Example

If you are using Linux&reg;, etc.:

Linux&reg; 等をご使用の場合は：

	javac -classpath .:Vnano.jar:./plugin Example.java
	java -classpath .:Vnano.jar:./plugin Example



<a id="about-us"></a>
## About Us - 開発元について

<div style="background-color:white; width: 890px; height: 356px; text-align:center; background-image: url('./rinearn.jpg'); background-repeat: no-repeat; background-size: contain;">
  <img src="https://github.com/RINEARN/vnano/blob/master/rinearn.jpg" alt="" width="890" />
</div>


The vnano is developed by <a href="https://www.rinearn.com/">RINEARN</a> 
which is a personal studio in Japan developing software for data-analysis, visualization, computation, and so on.
Please feel free to contact us if you have any question about the Vnano, or you are interested in the Vnano.

Vnanoは、日本の開発スタジオである <a href="https://www.rinearn.com/">RINEARN</a> が開発しています。
RINEARNでは、主にデータ解析や可視化、計算向けのソフトウェアを開発しています。
Vnanoに関するご質問や、Vnanoにご興味をお持ちの場合は、ご気軽にお問い合せください。

### Our website - ウェブサイト

- <a href="https://www.rinearn.com/">https://www.rinearn.com/</a>


---

<a id="references"></a>
## References - 関連記事

<dl>
	<dt style="display: list-item; margin-left:40px;">
		"オープンソースの小型・軽量スクリプトエンジン「 Vnano 」のコンセプト" - RINEARN Website (2018/06/02)
	</dt>
	<dd>
		<a href="https://www.rinearn.com/ja-jp/info/news/2018/0602-vnano-concept">https://www.rinearn.com/ja-jp/info/news/2018/0602-vnano-concept</a>
	</dd>
	<dt style="display: list-item; margin-left:40px;">
		"Vnanoのソースコードと開発リポジトリを公開" - RINEARN Website (2018/09/30)
	</dt>
	<dd>
		<a href="https://www.rinearn.com/ja-jp/info/news/2018/0930-vnano-repository-published">https://www.rinearn.com/ja-jp/info/news/2018/0930-vnano-repository-published</a>
	</dd>
	<dt style="display: list-item; margin-left:40px;">
		"Vnanoのスクリプトエンジンアーキテクチャ解説1: 全体像" - RINEARN Website (2019/05/28)
	</dt>
	<dd>
		<a href="https://www.rinearn.com/ja-jp/info/news/2019/0528-vnano-architecture">https://www.rinearn.com/ja-jp/info/news/2019/0528-vnano-architecture</a>
	</dd>
	<dt style="display: list-item; margin-left:40px;">
		"Vnanoのスクリプトエンジンアーキテクチャ解説2: コンパイラ" - RINEARN Website (2019/07/03)
	</dt>
	<dd>
		<a href="https://www.rinearn.com/ja-jp/info/news/2019/0703-vnano-compiler">https://www.rinearn.com/ja-jp/info/news/2019/0703-vnano-compiler</a>
	</dd>
</dl>

---

## Credits - 本文中の商標など

- Oracle and Java are registered trademarks of Oracle and/or its affiliates. 

- Kotlin is a trademark of Kotlin Foundation in the United States and/or other countries. 

- Microsoft Windows is either a registered trademarks or trademarks of Microsoft Corporation in the United States and/or other countries. 

- Linux is a trademark of linus torvalds in the United States and/or other countries. 

- Other names may be either a registered trademarks or trademarks of their respective owners. 

- OracleとJavaは、Oracle Corporation 及びその子会社、関連会社の米国及びその他の国における登録商標です。文中の社名、商品名等は各社の商標または登録商標である場合があります。

- Kotlin は、Kotlin Foundation の米国およびその他の国における商標または登録商標です。

- Windows は、米国 Microsoft Corporation の米国およびその他の国における登録商標です。

- Linux は、Linus Torvalds 氏の米国およびその他の国における商標または登録商標です。 

- その他、文中に使用されている商標は、その商標を保持する各社の各国における商標または登録商標です。


