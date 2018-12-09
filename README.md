# Vnano



Vnano (VCSSL nano) is a simple scripting language and its interpreter for embedded use in Java&reg; applications.

Vnano (VCSSL nano) は、Java&reg; アプリケーションに組み込んで用いる簡易スクリプト言語＆インタープリタです。



<div style="background-color:white; width: 720px; height: 395px; text-align:center; background-image: url('./logo.png'); background-repeat: no-repeat; background-size: contain;">
  <img src="https://github.com/RINEARN/vnano/blob/master/logo.png" alt="" width="720" />
</div>



<div style="border: solid 1px #ff8800; width:700px; margin-top: 30px; margin-bottom: 30px; padding: 0px 10px; 0px 10px">

## Caution - 注意

Vnano is under development, so it has not practical quality yet.

Vnanoは開発の途中であり、現時点でまだ実用的な品質ではありません。

</div>



<div style="border: solid 1px #777777; width:700px; margin-top: 30px; margin-bottom: 30px; padding: 10px;">
	- Index - 目次 - 
	<ul>
		<li><a href="#requirements">Requirements - 必要な環境</a></li>
		<li><a href="#example">Application Code Example - アプリケーションコード例</a></li>
		<li><a href="#how-to-use-in-java">How to Use in Java&reg; - Java&reg;言語での使用方法</a></li>
		<li><a href="#how-to-use-in-kotlin">How to Use in Kotlin&reg; - Kotlin&reg;での使用方法</a></li>
		<li><a href="#performances">Performances - 演算速度</a></li>
		<li><a href="#architecture">Architecture - アーキテクチャ</a></li>
		<li><a href="#license">License - ライセンス</a></li>
	</ul>
</div>





<a id="requirements"></a>
## Requirements - 必要な環境

1. Java&reg; Development Kit (JDK) 7 or later - Java&reg;開発環境 (JDK) 7以降
1. Java&reg; Runtime Environment (JRE) 7 or later - Java&reg;実行環境 (JRE) 7以降



<a id="example"></a>
## Application Code Example - アプリケーションコード例

The following is an example Java&reg; application code which executes 
a script code by using Vnano:

Vnano を使用してスクリプトを実行するJava&reg;アプリケーションのコード例は、以下の通りです：


	( Example.java )

	import javax.script.ScriptEngine;
	import javax.script.ScriptEngineManager;
	import javax.script.ScriptException;
	
	public class Example {
		
		// A method/field accessed from the script as an external function/variable.
		// スクリプト側から外部変数・外部関数としてアクセスするメソッドとフィールド
		public static int LOOP_MAX = 100;
		public static void output(int value) {
			System.out.println("Output from script: " + value);
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
			
			// Connect a method/field to the script engine as an external function/variable.
			// メソッド・フィールドを外部関数・変数としてスクリプトエンジンに接続
			try {
				engine.put("LOOP_MAX", Example.class.getField("LOOP_MAX"));
				engine.put("output(int)", Example.class.getMethod("output",int.class));
				
			} catch (NoSuchFieldException|NoSuchMethodException e){
				System.err.println("Method/field not found.");
				e.printStackTrace();
				return;
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
				engine.eval(scriptCode);
				
			} catch (ScriptException e) {
				System.err.println("Scripting error occurred.");
				e.printStackTrace();
				return;
			}
		}
	}


The following is the same example written in Kotlin&reg;:

また、Kotlin&reg;で記述された同様のアプリケーションのコード例は以下の通りです：


	( Example.kt )

	import javax.script.ScriptEngine
	import org.vcssl.nano.VnanoEngineFactory

	// A class which provides a field/method accessed from the script.
	// スクリプト内からアクセスされるフィールドとメソッドを提供するクラス
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


These example code are contained in this repository as "Example.java" (for Java&reg;) and "Example.kt" (for Kotlin&reg;).
We will actually execute these example code in the next section.

これらのサンプルコードは、"Example.java" (Java&reg;用) および "Example.kt" (Kotlin&reg;用) として、このリポジトリ内に含まれています。
次節では、実際にこのサンプルコードを実行してみます。





<a id="how-to-use-in-java"></a>
## How to Use in Java&reg; - Java&reg;言語での使用方法

### 1. Build Vnano Engine - Vnanoエンジンのビルド

Firstly, build source code of Vnano Engine (The script engine of Vnano).
If you are using Microsoft&reg; Windows&reg;, please double-click "build.bat".
If you are using Linux&reg;, etc., please execute "build.sh" on the bash-compatible shell.
Alternatively, you can build Vnano Engine by Apache Ant as:

はじめに、Vnanoエンジン（Vnanoのスクリプトエンジン）をビルドします。
Microsoft&reg; Windows&reg; をご使用の場合は、"build.bat" をダブルクリック実行してください。
Linux&reg; 等をご使用の場合は、bash互換シェル上で "build.sh" を実行してください。もしくは以下のように、Apache Ant を用いてVnanoエンジンをビルドする事もできます：

    ant -buildfile build.xml

If you succeeded to build Vnano Engine, "Vnano.jar" will be generated in the same folder in the above files.
You can use Vnano on your Java applications by appending this JAR file to the classpath.

Vnanoエンジンのビルドが成功すると、"Vnano.jar" が上記ファイルと同じフォルダ内に生成されます。
Vnanoを使用したいJavaアプリケーションから、このJARファイルにクラスパスを通せば、それだけでVnanoが使用できます。

### 2. Compile the Example Application - サンプルアプリケーションのコンパイル

Let's compile the simple example code of host Java application which executes a script code by using Vnano Engine: 

それでは、実際にVnanoエンジンを使用して、スクリプトを実行する、ホストアプリケーションのサンプルコードをコンパイルしてみましょう：

    javac Example.java

As the result of the compilation, "Example.class" will be generated in the same folder.

コンパイルが成功すると、同じフォルダ内に Example.class が生成されます。

### 3. Execute the Example Application - サンプルアプリケーションの実行

Then, execute the compiled example application with appending "Vnano.jar" to the classpath as follows. If you are using Microsoft&reg; Windows&reg;:

コンパイルしたサンプルアプリケーションは、Vnano.jar にクラスパスを通して実行します。
Microsoft&reg; Windows&reg; の場合は：

    java -classpath ".;Vnano.jar" Example

If you are using Linux&reg;, etc.:

Linux&reg;等の場合は：

    java -classpath ".:Vnano.jar" Example

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

### 1. Build Vnano Engine - Vnanoエンジンのビルド

Firstly, build source code of Vnano Engine (The script engine of Vnano).
If you are using Microsoft&reg; Windows&reg;, please double-click "build.bat".
If you are using Linux, etc., please execute "build.sh" on the bash-compatible shell.
Alternatively, you can build Vnano Engine by Apache Ant as:

はじめに、Vnanoエンジン（Vnanoのスクリプトエンジン）をビルドします。
Microsoft&reg; Windows&reg; をご使用の場合は、"build.bat" をダブルクリック実行してください。
Linux 等をご使用の場合は、bash互換シェル上で "build.sh" を実行してください。もしくは以下のように、Apache Ant を用いてVnanoエンジンをビルドする事もできます：

    ant -buildfile build.xml

If you succeeded to build Vnano Engine, "Vnano.jar" will be generated in the same folder in the above files.
You can use Vnano on your Java applications by appending this JAR file to the classpath.

Vnanoエンジンのビルドが成功すると、"Vnano.jar" が上記ファイルと同じフォルダ内に生成されます。
Vnanoを使用したいJavaアプリケーションから、このJARファイルにクラスパスを通せば、それだけでVnanoが使用できます。

### 2. Compile the Example Application - サンプルアプリケーションのコンパイル

Let's compile the simple example code of host application written in Kotlin, which executes a script code by using Vnano Engine. It is necessary to compile the application with appending "Vnano.jar" to the classpath as follows. If you are using Microsoft&reg; Windows&reg;:

それでは、実際にVnanoエンジンを使用して、スクリプトを実行する、Kotlinで記述されたホストアプリケーションのサンプルコードをコンパイルしてみましょう。コンパイルは、Vnano.jar にクラスパスを通しながら行います。Microsoft&reg; Windows&reg; の場合は：

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





<a id="performances"></a>
## Performances - 演算速度

In addition to the above example application, some benchmarking programs for measuring performances 
are also contained in this repository. Let's execute them in this section.
Please note that, those benchmarking programs measure maximum performances Vnano Engine can perform, 
not effective performances expected for general programs.

このリポジトリ内には、上記のサンプルアプリケーションに加えて、性能計測用のベンチマークプログラムも含まれています。
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

	javac Float64ScalarFlopsBenchmark.java -encoding UTF-8
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

### 64-bit vector Operation FLOPS Performance - 64-bitベクトル演算FLOPS性能

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

	javac Float64VectorFlopsBenchmark.java -encoding UTF-8
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
which compiles script code written in Vnano to a kind of intermediate code, 
named as "VRIL" code.
VRIL ― Vector Register Intermediate Language ― is a low-level (but readable text format) 
language designed as a virtual assembly code of the VM (Virtual Machine) layer of Vnano Engine.

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
パッケージは、テキスト形式のVRILコードを、VnanoのVM層で直接的に実行可能な命令オブジェクト列（より厳密には、
それを内部に含む実行用オブジェクト）へと変換する、アセンブラとしての機能を担います。
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
Whether you use this component or don't is optional,
so Vnano engine can run even under the condition of that this component is completely disabled.

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/vm/accelerator">org.vcssl.nano.vm.accelerator</a>
パッケージは、上記の仮想プロセッサの、より高速な実装を提供します。半面、実装コードの内容もより複雑になっています。
このコンポーネントを使用するかどうかは、任意に選択できます。
Vnanoエンジンは、このコンポーネントの動作を完全に無効化しても、機能上は欠損なく成立するようにできています。



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
We refer this component as "Interconnect" in Vnano Engine.
For example, information to resolve references of variables and functions are managed by this interconnect component. 
Bindings to external functions/variables are intermediated by this interconnect component, so plug-ins of 
external functions/variables will be connected to this component.

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/interconnect">org.vcssl.nano.interconnect</a>
パッケージは、これまでに列挙した各コンポーネント間で共有される、いくつかの情報を管理・提供する機能を担います。
この機能を担うコンポーネントを、Vnanoエンジンでは "インターコネクト" と呼びます。
インターコネクトが管理・提供する情報の具体例としては、関数・変数の参照解決のための情報などが挙げられます。
外部変数・外部関数のバインディングも、インターコネクトを介して行われます。そのため、外部変数・外部関数のプラグインは、
Vnanoエンジン内でこのコンポーネントに接続されます。





<a id="license"></a>
## License - ライセンス

This software is released under the MIT License.

このソフトウェアはMITライセンスで公開されています。



---

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


