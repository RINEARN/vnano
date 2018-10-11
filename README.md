# Vnano

Vnano (VCSSL nano) is a simple scripting language and its interpreter for embedded use in Java&reg; applications.

Vnano (VCSSL nano) は、Java&reg; アプリケーション上に搭載して用いる簡易スクリプト言語＆インタープリタです。

<div style="background-color:white; width:100%; text-align:center;" />
  <img src="https://github.com/RINEARN/vnano/blob/master/logo.png" alt="logo.png" />
</div>



## Caution - 注意

Vnano is under development, so it have not practical quality yet.

Vnanoは開発の途中であり、現時点でまだ実用的な品質ではありません。



## Requirements - 必要な環境

1. Java Development Kit (JDK) 7 or later - Java開発環境 (JDK) 7以降
1. Java Runtime Environment (JRE) 7 or later - Java実行環境 (JRE) 7以降



## Application Code Example - アプリケーションコード例

The following is an example Java application code which executes 
a script code by using Vnano:

Vnano を使用してスクリプトを実行するJavaアプリケーションのコード例は、以下の通りです：

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

This example code is contained in this repository as "Example.jar".
We will actually execute this example code in the next section.

このサンプルコードは Example.java として、このリポジトリ内に含まれています。
次節では、実際にこのサンプルコードを実行してみます。


## How to Use - 使用方法

※ 日本語は後方

### 1. Build Vnano Engine

Firstly, build source code of Vnano Engine (The script engine of Vnano).
If you are using Microsoft&reg; Windows&reg;, please double-click "build.bat".
If you are using Linux, etc., please execute "build.sh" on the bash-compatible shell.

Alternatively, you can build Vnano Engine by Apache Ant as:

    ant -buildfile build.xml

If you succeeded to build Vnano Engine, "Vnano.jar" will be generated in the same folder in the above files.
You can use Vnano on your Java applications by appending this JAR file to the classpath.

### 2. Compile the Example Application

Let us compile the simple example code of host Java application which executes a script code by using Vnano Engine: 

    javac Example.java

As the result of the compilation, "Example.class" will be generated in the same folder.

### 3. Execute the Example Application

Then, execute the compiled example application with appending "Vnano.jar" to the classpath as follows.

If you are using Microsoft Windows:

    java -classpath ".;Vnano.jar" Example

If you are using Linux, etc.:

    java -classpath ".:Vnano.jar" Example

As the result of the execution, the following line will be printed to the standard output:

    Output from script: 5050

### 4. Create the JAR file of the Example Application

To create the JAR file of the example application, 
please create a manifest file "manifest.txt" in advance, 
and in there specify "Vnano.jar" to the Class-Path section as follows:

    Main-Class: Example
    Class-Path: . Vnano.jar

Then create the JAR file with specifying the above manifest file as follows:

    jar cvfm Example.jar manifest.txt Example.class

As the result of the above processing, "Example.jar" will be generated in the same folder.

It is necessary to locate "Vnano.jar" in the same folder to execute "Example.jar". 
If you want to locate "Vnano.jar" in the different folder (e.g. lib folder),
please rewrite the description "Vnano.jar" in "Class-Path" section of the manifest file
to the relative path (e.g. "lib/Vnano.jar").



### 1. Vnanoエンジンのビルド

はじめに、Vnanoエンジン（Vnanoのスクリプトエンジン）をビルドします。
Microsoft&reg; Windows&reg; をご使用の場合は、"build.bat" をダブルクリック実行してください。
Linux 等をご使用の場合は、bash互換シェル上で "build.sh" を実行してください。

もしくは以下のように、Apache Ant を用いてVnanoエンジンをビルドする事もできます：

    ant -buildfile build.xml

Vnanoエンジンのビルドが成功すると、"Vnano.jar" が上記ファイルと同じフォルダ内に生成されます。
Vnanoを使用したいJavaアプリケーションから、このJARファイルにクラスパスを通せば、それだけでVnanoが使用できます。

### 2. サンプルアプリケーションのコンパイル

それでは、実際にVnanoエンジンを使用して、スクリプトを実行する、ホストアプリケーションのサンプルコードをコンパイルしてみましょう：

    javac Example.java

コンパイルが成功すると、同じフォルダ内に Example.class が生成されます。

### 3. サンプルアプリケーションの実行

コンパイルしたサンプルアプリケーションは、Vnano.jar にクラスパスを通して実行します。

Microsoft Windows の場合は：

    java -classpath ".;Vnano.jar" Example

Linux等の場合は：

    java -classpath ".:Vnano.jar" Example

正常に実行されると、以下の内容が標準出力に表示されます：

    Output from script: 5050

### 4. サンプルアプリケーションのJARファイル化

サンプルアプリケーションをJARファイル化するには、まずマニフェストファイル manifest.txt を作成し、
その中で通常のメインクラス指定に加えて、以下のようにVnano.jar のクラスパスを記載します：

    Main-Class: Example
    Class-Path: . Vnano.jar

このマニフェストファイルを指定して、JARファイルを生成します：

    jar cvfm Example.jar manifest.txt Example.class

これで Example.jar が生成されます。このJARファイルを実行する際に、
Vnano.jar を同じフォルダ内に置いておけば使用できます。
もし、Vnano.jar を別の階層のフォルダ（例：lib など）内に置きたい場合は、
マニフェストファイルの Class-Path 指定の「Vnano.jar」の箇所を、
Example.jar から見た相対パスで書き換えてください（例：lib/Vnano.jar ）。



## Performances - 演算速度

<img src="https://github.com/RINEARN/vnano/blob/master/vectorflops.jpg" alt="vectorflops.jpg" />



## Architecture - アーキテクチャ

The architecture of Vnano Engine is a commonplace "compiler + VM" type.
The compiler compiles the script code to the intermediate code, and the virtual machine (VM) executes it.
Vnano Engine is composed of some packages, so we will explain roles of them in the following.

Vnanoエンジンは、内部でスクリプトコードを中間コードにコンパイルし、
それを仮想マシン(VM)上で実行する、オーソドックスなアーキテクチャを採用しています。
以下では、Vnanoエンジンを構成する各パッケージの役割について説明します。

<img src="https://github.com/RINEARN/vnano/blob/master/architecture.jpg" alt="architecture.jpg" width="700" />



※日本語は後方

### Compiler

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/compiler">org.vcssl.nano.compiler</a> 
package performs the function as a compiler, 
which compiles script code written in Vnano to a kind of intermediate code, 
named as "VRIL" code.

VRIL ― Vector Register Intermediate Language ― is a low-level (but readable text format) language designed as a virtual assembly code of the VM (Virtual Machine) layer of Vnano Engine.


### Assembler

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/assembler">org.vcssl.nano.assembler</a>
package performs the function as an assembler, 
to translate VRIL code (text format) into more low level instruction objects 
(referred as "VRIL Instructions" in the above figure)
which are directly executable by the VM layer.


### Processor

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/processor">org.vcssl.nano.processor</a>
package performs the function as a virtual processor (CPU), 
which executes instruction objects assembled from VRIL code.
The architecture of this virtual processor is a SIMD-based Register Machine (Vector Register Machine).

The implementation code of this virtual processor is simple and may be easy to customize, 
however, its processing speed is not so high.


### Accelerator

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/accelerator">org.vcssl.nano.accelerator</a>
package provides a high-speed (but complicated) implementation of the virtual processor referred above.

Whether you use this component or don't is optional,
so Vnano engine can run even under the condition of that this component is completely disabled.


### Memory

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/memory">org.vcssl.nano.memory</a>
package performs the function as a virtual memory, to store data for reading and writing from the virtual processor.
Register for storing temporary data are also provided by this virtual memory.

Most instructions of the virtual processor of Vnano are SIMD, so this virtual memory stores data by units of vector (array).
One virtual data address corresponds one vector.


### Interconnect

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/interconnect">org.vcssl.nano.interconnect</a>
package performs the function as a component which manages and provides some information shared between multiple components explained above. 
We refer this component as "Interconnect" in Vnano Engine.
For example, information to resolve references of variables and functions are managed by this interconnect component. 

Bindings to external functions/variables are intermediated by this interconnect component, so plug-ins of external functions/variables will be connected to this component.





### コンパイラ

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/compiler">org.vcssl.nano.compiler</a> 
パッケージは、Vnanoのスクリプトコードを、"VRILコード" と呼ぶ一種の中間コードへと変換する、コンパイラの機能を担います。

VRIL（Vector Register Intermediate Language; ベクトルレジスタ中間言語）は、
VnanoエンジンのVM（仮想マシン）層の単位動作に対応するレベルの低抽象度な命令を提供する、
仮想的なアセンブリ言語です。VRILコードは、実在のアセンブリコードと同様に、人間にとって可読なテキスト形式のコードです。


### アセンブラ

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/assembler">org.vcssl.nano.assembler</a>
パッケージは、テキスト形式のVRILコードを、VnanoのVM層で直接的に実行可能な命令オブジェクト列（より厳密には、それを内部に含む実行用オブジェクト）へと変換する、アセンブラとしての機能を担います。
この命令オブジェクト列は、上図の中において "VRIL Instructions" として記述されています。


### プロセッサ

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/processor">org.vcssl.nano.processor</a>
パッケージは、アセンブラによってVRILコードから変換された命令オブジェクト列を、逐次的に実行する仮想的なプロセッサ（CPU）としての機能を担います。この仮想プロセッサは、SIMD演算を基本とする、ベクトルレジスタマシンのアーキテクチャを採用しています。

このパッケージが提供する仮想プロセッサの実装は、単純で改造が比較的容易ですが、その反面、処理速度はあまり速くありません。


### アクセラレータ

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/accelerator">org.vcssl.nano.accelerator</a>
パッケージは、上記の仮想プロセッサの、より高速な実装を提供します。半面、実装コードの内容もより複雑になっています。

このコンポーネントを使用するかどうかは、任意に選択できます。
Vnanoエンジンは、このコンポーネントの動作を完全に無効化しても、機能上は欠損なく成立するようにできています。


### メモリ

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/memory">org.vcssl.nano.memory</a>
パッケージは、仮想プロセッサから読み書きされるデータを、アドレスに紐づけて保持する、仮想的なメモリとしての機能を担います。仮想プロセッサが一時的なデータの保持に使用するレジスタも、この仮想メモリが提供します。

先述の通り、Vnanoエンジンの仮想プロセッサはベクトルレジスタマシンのアーキテクチャを採用しているため、この仮想メモリはデータをベクトル（配列）単位で保持します。即ち、一つのデータアドレスに対して、一つの配列データが紐づけられます。


### インターコネクト

<a href="https://github.com/RINEARN/vnano/tree/master/src/org/vcssl/nano/interconnect">org.vcssl.nano.interconnect</a>
パッケージは、これまでに列挙した各コンポーネント間で共有される、いくつかの情報を管理・提供する機能を担います。この機能を担うコンポーネントを、Vnanoエンジンでは "インターコネクト" と呼びます。
インターコネクトが管理・提供する情報の具体例としては、関数・変数の参照解決のための情報などが挙げられます。

外部変数・外部関数のバインディングも、インターコネクトを介して行われます。そのため、外部変数・外部関数のプラグインは、Vnanoエンジン内でこのコンポーネントに接続されます。





## License - ライセンス

This software is released under the MIT License.

このソフトウェアはMITライセンスで公開されています。



---

- Oracle and Java are registered trademarks of Oracle and/or its affiliates. 

- Microsoft Windows is either a registered trademarks or trademarks of Microsoft Corporation in the United States and/or other countries. 

- Linux is a trademark of linus torvalds in the United States and/or other countries. 

- Other names may be either a registered trademarks or trademarks of their respective owners. 

- OracleとJavaは、Oracle Corporation 及びその子会社、関連会社の米国及びその他の国における登録商標です。文中の社名、商品名等は各社の商標または登録商標である場合があります。

- Windows は、米国 Microsoft Corporation の米国およびその他の国における登録商標です。

- Linux は、Linus Torvalds 氏の米国およびその他の国における商標または登録商標です。 

- その他、文中に使用されている商標は、その商標を保持する各社の各国における商標または登録商標です。


