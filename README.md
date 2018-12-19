# Vnano



Vnano (VCSSL nano) is a simple scripting language and its interpreter for embedded use in Java&reg; applications.

Vnano (VCSSL nano) は、Java&reg; アプリケーションに組み込んで用いる簡易スクリプト言語＆インタープリタです。



<div style="background-color:white; width: 720px; height: 395px; text-align:center; background-image: url('./logo.png'); background-repeat: no-repeat; background-size: contain;">
  <img src="https://github.com/RINEARN/vnano/blob/master/logo.png" alt="" width="720" />
</div>



## Index - 目次
- <a href="#caution">Caution - 注意</a>
- <a href="#license">License - ライセンス</a>
- <a href="#requirements">Requirements - 必要な環境</a>
- <a href="#example">Application Code Example - アプリケーションコード例</a>
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
  - <a href="#language-external">External Functions and External Variables - 外部関数と外部変数</a>
    - <a href="#language-external-functions">外部関数</a>
    - <a href="#language-external-variables">外部変数</a>




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

Vnano を使用してスクリプトを実行するJava&reg;アプリケーションのコード例は、以下の通りです：


	( Example.java )

	import javax.script.ScriptEngine;
	import javax.script.ScriptEngineManager;
	import javax.script.ScriptException;
	import java.lang.reflect.Field;
	import java.lang.reflect.Method;

	public class Example {

		// A class which provides a field/method accessed from the script as external functions/variables.
		// スクリプト内から外部変数・外部関数としてアクセスされるフィールドとメソッドを提供するクラス
		public class ScriptIO {
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

			// Connect a method/field to the script engine as an external function/variable.
			// メソッド・フィールドを外部関数・変数としてスクリプトエンジンに接続
			try {
				Field loopMaxField  = ScriptIO.class.getField("LOOP_MAX");
				Method outputMethod = ScriptIO.class.getMethod("output",int.class);
				ScriptIO ioInstance = new Example().new ScriptIO();

				engine.put("LOOP_MAX",    new Object[]{ loopMaxField, ioInstance } );
				engine.put("output(int)", new Object[]{ outputMethod, ioInstance } );

				// see "Float64ScalarFlopsBenchmark.java" to connect STATIC methods/fields.
				// メソッド/フィールドがstaticな場合の接続例は Float64ScalarFlopsBenchmark.java 参照

			} catch (NoSuchFieldException | NoSuchMethodException e){
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


These example code are contained in this repository as "Example.java" (for Java&reg;) and "Example.kt" (for Kotlin&reg;).
We will actually execute these example code in the next section.

これらのサンプルコードは、"Example.java" (Java&reg;用) および "Example.kt" (Kotlin&reg;用) として、このリポジトリ内に含まれています。
次節では、実際にこのサンプルコードを実行してみます。





<a id="how-to-use-in-java"></a>
## How to Use in Java&reg; - Java&reg;言語での使用方法

### 1. Build Vnano Engine - Vnanoエンジンのビルド

Firstly, build source code of Vnano Engine (The script engine of the Vnano).
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

Firstly, build source code of Vnano Engine (The script engine of the Vnano).
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





<a id="how-to-use-in-command"></a>
## How to Use in Command Line - コマンドラインでの使用方法

### 1. About Command-Line Mode - コマンドラインモードについて

The main purpose of the Vnano is the embedded use in applications, 
however, for the development and the debugging usages, 
you can use the script engine of the Vnano on a command-line terminal 
and can directly run script code.
Please note that this command-line mode is NOT the feature for using Vnano alone for the practical purpose. 
It is NOT convenient at all. In such purpose, we recommend to use the VCSSL instead of the Vnano.
The Vnano is a subset of the VCSSL for embedded use in apprications.

Vnano の本来の用途はアプリケーション組み込み用ですが、一方で開発時やデバッグ時などのため、
Vnanoのスクリプトエンジンをコマンドライン端末上で使用して、
Vnanoで記述されたスクリプトコードを直接実行する事ができます。
ただし、このコマンドラインモードは、Vnanoを単独で実用目的のスクリプト言語として使うための機能ではない事に注意してください。
そのような用途にはVnanoは全く便利ではありません。
Vnanoは、VCSSLという言語から、アプリケーション組み込み用に機能を絞ったサブセットであるため、
単独での使用にはフル機能版であるVCSSLのご使用をおすすめします。


### 2. Build Vnano Engine - Vnanoエンジンのビルド

Firstly, build source code of Vnano Engine (The script engine of the Vnano).
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

### 3. Run the Example Script Code - サンプルスクリプトコードの実行

An example Vnano script code "Example.vnano" is contained in the repository.

このリポジトリ内には、Vnanoのサンプルスクリプトコードも含まれています。

    (Example.vnano)

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


### 4. Dump AST, Intermediate Code (VRIL), etc. - 抽象構文木(AST)や中間コード(VRIL)などのダンプ

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
 	  <VARIABLE DATA_TYPE="int" IDENTIFIER_VALUE="sum" RANK="0">
        <EXPRESSION DATA_TYPE="int" RANK="0">
          <OPERATOR OPERATOR_SYNTAX="binary" OPERATOR_EXECUTOR="assignment" OPERATOR_SYMBOL="=" OPERATOR_PRIORITY="6000" DATA_TYPE="int" OPERATOR_EXECUTION_DATA_TYPE="int" RANK="0">
            <LEAF LEAF_TYPE="variableIdentifier" IDENTIFIER_VALUE="sum" SCOPE="local" RANK="0" DATA_TYPE="int" />
            <LEAF LEAF_TYPE="literal" LITERAL_VALUE="0" RANK="0" DATA_TYPE="int" />
          </OPERATOR>
        </EXPRESSION>
	...

	================================================================================
	= Assembly Code (VRIL Code)
	= - Output of: org.vcssl.nano.compiler.CodeGenerator
	= - Input  of: org.vcssl.nano.vm.assembler.Assembler
	================================================================================
	#FUNCTION	_output(int);
	#META	"line=70, file=Example.vnano";
	#LOCAL	_sum;
		ALLOC	int	_sum;
		MOV	int	_sum	~int:0;
	#META	"line=71, file=Example.vnano";
	#LOCAL	_n;
		ALLOC	int	_n;
		MOV	int	_n	~int:100;
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

	#FUNCTION	_output(int);
	#META	"line=70, file=Example.vnano";
	#LOCAL	_sum;
		ALLOC	int	_sum;
		MOV	int	_sum	~int:0;
	#META	"line=71, file=Example.vnano";
	#LOCAL	_n;
		ALLOC	int	_n;
		MOV	int	_n	~int:100;
	#META	"line=72, file=Example.vnano";
	#LOCAL	_i;
		ALLOC	int	_i;
		MOV	int	_i	~int:1;
	#LABEL	&LABEL0;
		ALLOC	bool	R0;
		LEQ	int	R0	_i	_n;
		JMPN	bool	R0	&LABEL2;
	#META	"line=73, file=Example.vnano";
		ADD	int	_sum	_sum	_i;
	#LABEL	&LABEL1;
		ALLOC	int	R3;
		MOV	int	R3	_i;
		ALLOC	int	R1;
		ADD	int	R1	_i	~int:1;
		MOV	int	_i	R1;
		JMP	bool	~bool:true	&LABEL0;
	#LABEL	&LABEL2;
	#META	"line=75, file=Example.vnano";
		CALL	void	R2	_output(int)	_sum;

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

    java -jar Vnano --help

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
which compiles script code written in the Vnano to a kind of intermediate code, 
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

The language specification of the Vnano ( = VCSSL nano ) is a small subset of the VCSSL 
which is a programming language having simple C-like syntax, so Vnano also has simple C-like syntax.
Vnano is specialized for embedded use in applications, 
so compared with other programming languages, many features not necessary for the embedded use are omitted 
for making the implementation of the script engine compact.
It is the concept for giving priority to customizability, maintainability, security, portability 
and so on than the functionality.

プログラミング言語としての Vnano ( = VCSSL nano ) の仕様は、その名前の通り、VCSSLという言語の仕様の小さなサブセットになっています。
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
	output("\n");
	output(f);
	output("\n");
	output(b);
	output("\n");
	output(s);
	output("\n");

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
		output("i=" + i + "\n");
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
		output("a=" + a + "\n");
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
		output("i=" + i + "\n");
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
		output("i=" + i + "\n");
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

| Operators - 演算子 | Priority - 優先度 | Syntax - 構文 | Operand Types - オペランドの型 | Value Type - 値の型 |
| --- | --- | --- | --- | --- |
| ( ... , ... , ... ) as call | 1000 | multiary | any, any, ... | any |
| [ ... ][ ... ] ... as index | 1000 | multiary | int, int, ... | any |
| ++ | 1000 | postfix | int | int |
| -- | 1000 | postfix | int | int |
| ++ | 2000 | prefix | int | int |
| -- | 2000 | prefix | int | int |
| + | 2000 | prefix | int | int |
| - | 2000 | prefix | int | int |
| ! | 2000 | prefix | bool | bool |
| * | 3000 | binary | int, float | int, float |
| / | 3000 | binary | int, float | int, float |
| % | 3000 | binary | int, float | int, float |
| + | 3100 | binary | int, float, string | int, float, string |
| - | 3100 | binary | int, float | int, float |
| < | 4000 | binary | int, float | bool |
| <= | 4000 | binary | int, float | bool |
| > | 4000 | binary | int, float | bool |
| >= | 4000 | binary | int, float | bool |
| == | 4100 | binary | any | bool |
| != | 4100 | binary | any | bool |
| && | 5000 | binary | bool | bool |
| \|\| | 5100 | binary | bool | bool |
| = | 6000 | binary | any | any |
| *= | 6000 | binary | int, float | int, float |
| /= | 6000 | binary | int, float | int, float |
| %= | 6000 | binary | int, float | int, float |
| += | 6000 | binary | int, float, string | int, float, string |
| -= | 6000 | binary | int, float | int, float |


The value type (the data-type of the operated value) of binary arithmetic operators (\*, /, %, +, -) 
and compound arithmetic assignment operators (*=, /=, %=, +=, -=) are decided by the following table:

算術演算子（\*, /, %, +, -）および算術複合代入演算子（*=, /=, %=, +=, -=）における値の型（演算された値のデータ型）は、以下の表の通りに決定されます：

| Operand type A - オペランドAの型 | Operand type B - オペランドBの型 | Value type - 値の型 |
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



<a id="language-external"></a>
### External Functions and External Variables - 外部関数と外部変数

<a id="language-external-functions"></a>
#### External functions - 外部関数

You can not declare functions in script code written in the Vnano, at least for the current version.
This is for making the script engine compact, and for giving priority to maintainability and security.
The Vnano is the language for executing partial processings on host applications as scripts, 
so functions called from scripts are provided by host applications as so-called "built-in functions".
In the Vnano, we refer them as "external functions".
Therefore, all functions you want to call from the Vnano script code 
are necessary to be implemented on the host application by using Java&reg; (or alternative languages), 
and necessary to be connected to the script engine as external functions.

Vnanoでは、少なくとも現時点において、スクリプト内でユーザーが関数を宣言する事はできません。
これは、用途的に必要性が低い機能は削る事で、スクリプトエンジンをなるべくコンパクトに抑え、
保守性やセキュリティ等を優先させるためです。
Vnanoは、ホストアプリケーション上での部分的な処理をスクリプトとして実行する事に焦点を絞った言語なので、
関数はホストアプリケーション側から、いわゆる「組み込み関数」として提供されます。
Vnanoでは、それらを「外部関数」と呼びます。
従って、Vnanoのスクリプトコード内で使用したい全ての関数は、
ホストアプリケーション側にJava&reg;言語（またはその代替言語）で実装し、
スクリプトエンジンに外部関数として接続する必要があります。

<a id="language-external-variables"></a>
#### External variables - 外部変数

For variables, you can declare them in the script code written the Vnano by using <a href="#language-variable">variable declaration statements</a>.
In addition, host applications can provide so-called "build-in variables", and we refer it as "external variables" in the Vnano.

変数については、<a href="#language-variable">変数宣言文</a>を用いて、
Vnanoのスクリプトコード内で宣言する事ができます。
一方で、ホストアプリケーション側も、スクリプト内から読み書きできる変数（いわゆる組み込み変数）を提供する事ができ、Vnanoではそれらを「外部変数」と呼びます。


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


