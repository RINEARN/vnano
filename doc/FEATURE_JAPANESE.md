
# Vnano Engine の主な機能

## 目次

( &raquo; [English](FEATURE.md) )

- [式を計算する](#calculate-expression)
- [Java&reg;製クラスのフィールド/メソッドにアクセスする（プラグイン）](#fields-and-methods)
- [プラグインを動的に読み込む](#plugins-load)
- [プラグインに関する発展的な事項 (標準プラグイン、高速インターフェース、等)](#plugins-advanced)
- [スクリプトを実行する](#scripting)
- [ライブラリ スクリプトを読み込む](#libraries)
- [コマンドラインモード](#command-line-mode)
- [仕様書](#specifications)

<hr />


<a id="calculate-expression"></a>
## 式を計算する

[README](../README_JAPANESE.md) でも見た通り、Vnano Engine を用いて式の値を計算する事ができます:

	(in ExampleApp1.java)

	import org.vcssl.nano.VnanoEngine;
	import org.vcssl.nano.VnanoException;
	import java.util.Map;
	import java.util.HashMap;
	import java.util.Scanner;

	public class ExampleApp1 {
		public static void main(String[] args) throws VnanoException {

			// Vnano Engine のインスタンスを生成
			VnanoEngine engine = new VnanoEngine();

			// 整数リテラルを float (=double) 型と見なすオプションを有効化
			// (式の計算用途に便利ですが、スクリプトの実行用途には適しません)
			Map<String, Object> optionMap = new HashMap<String, Object>();
			optionMap.put("EVAL_INT_LITERAL_AS_FLOAT", true);
			engine.setOptionMap(optionMap);

			// ユーザーに式を入力してもらう
			System.out.println("式を入力してください。例：  1.2 + 3.4 * 5.6");
			Scanner scanner = new Scanner(System.in);
			String expression = scanner.nextLine();

			// 入力内容が「 ; 」で終わっていない場合は、末尾に付ける
			if (!expression.trim().endsWith(";")) {
				expression += ";";
			}

			// Vnano Engine で式の値を計算し、結果を表示
			double result = (Double)engine.executeScript(expression);
			System.out.println("result: " + result);
		}
	}

上記のコードは、以下のようにコンパイルできます：

	javac -cp .;Vnano.jar ExampleApp1.java        (For Windows)
	javac -cp .:Vnano.jar ExampleApp1.java        (For Linux)

そして以下のように実行します：

	java -cp .;Vnano.jar ExampleApp1        (For Windows)
	java -cp .:Vnano.jar ExampleApp1        (For Linux)

ここで上記の「 ExampleApp1 」は、ユーザーに式を入力するようリクエストしてきます。
従って以下のように式を入力し、エンターキーを押します：

	1.2 + 3.4 * 5.6

すると、入力した式が Vnano Engine で計算され、結果が以下のように表示されます：

	20.24

なお、ここで式の代わりに、以下のようなスクリプトコードを入力しても動きます：

	float value=0.0; for (int i=0; i<10; i++) { value += 1.2; } value += 123.4; value;

結果は:

	result: 135.4

スクリプト実行に関する詳細は [スクリプトの実行](#scripting) の項目をご参照ください。

ところで、式ではなくスクリプトを実行する用途においては、以下のオプション指定の行は削除する事をおすすめします：

	optionMap.put("EVAL_INT_LITERAL_AS_FLOAT", true);

既に述べた通り、上記オプションは式内に書かれた整数値（整数リテラル）を float 型の値として扱うもので、
式の計算には便利です（指定しないと、整数同士の除算結果が整数になって厄介です）。一方で、スクリプトに対して効かせると混乱の元になりかねません。

ただし、上記オプションは executeScript メソッドで直接実行する内容に対してのみ作用し、[ライブラリスクリプト](#libraries) に対しては作用しないため、ライブラリの処理内容への影響を心配する必要はありません。

なお、設定可能なオプション類の一覧/詳細については、別途文書「 [Vnano Engine の各種使用](SPEC_JAPANESE.md) 」をご参照ください。


<a id="fields-and-methods"></a>
## Java製クラスのフィールド/メソッドにアクセスする（プラグイン）

Javaで実装した任意のクラスのフィールド/メソッドを Vnano Engine に接続して、
その上で実行する式やスクリプト内からアクセスする事ができます。

以下はその例です：

	(in ExampleApp2.java)

	...
	public static class AnyClass {

		// Vnano Engine 上で実行される式やスクリプト内から
		// アクセスしたいフィールド/メソッド
		public double x = 3.4;
		public double f(double arg) {
			return arg * 5.6;
		}
	}

	public static void main(String[] args)
			throws VnanoException, NoSuchFieldException, NoSuchMethodException {

		// Vnano Engine のインスタンスを生成
		VnanoEngine engine = new VnanoEngine();

		// 任意のクラス（ここでは上のAnyClass）のフィールド/メソッドを Vnano Engine に接続
		Field field = AnyClass.class.getField("x");
		Method method = AnyClass.class.getMethod("f", double.class);
		AnyClass anyClassInstance = new AnyClass();
		engine.connectPlugin("x", new Object[]{ field, anyClassInstance });
		engine.connectPlugin("f", new Object[]{ method, anyClassInstance });

		// staric なフィールド/メソッドは、以下のようにインスタンスなしで接続できます
		// Field field = AnyClass.class.getField("x");
		// Method method = AnyClass.class.getMethod("f", double.class);
		// engine.connectPlugin("x", field);
		// engine.connectPlugin("f", method);

		...
		(後は ExampleApp1.java と同じです)
	}


コンパイルして実行してみましょう:

	(For Windows)
	javac -cp .;Vnano.jar ExampleApp2.java
	java -cp .;Vnano.jar ExampleApp2

	(For Linux)
	javac -cp .:Vnano.jar ExampleApp2.java
	java -cp .:Vnano.jar ExampleApp2

実行すると式の入力を求められるので、下記の通りに入力します:

	1.2 + f(x)

今の ExampleApp2 内の AnyClass クラスの実装では、x は 3.4 で、f(x) は x * 5.6 を返すため、期待される結果は 1.2 + (3.4 * 5.6) = 20.24 です。実際に表示される結果も：

	20.24

ここでの AnyClass クラスのように、式やスクリプト内で使うフィールドやメソッド、その他諸機能などを提供するJava製クラスを、Vnano Engine では「 プラグイン 」と呼びます。

ところで、接続したJava側のフィールド値（上の例では「 x 」の値）を、スクリプト内から書き換える事も可能です。ただしそれに関して、以下のような Vnano Engine の挙動を意識しておく必要があります：
**Vnano Engine は、スクリプトや式の実行直前に、接続されたJavaフィールドの値を読み、それを内部にキャッシュします。そして実行処理が完了したタイミングで、キャッシュされた値（スクリプト内容によっては、書き換えられた値）が、Javaフィールドに書き戻されます。**

従って、Java側とスクリプト側の間で、実行中に何度も更新し合うような値については、フィールドとして直接 Vnano Engine に接続してアクセスするような事はしないでください。そのような場面では、その値を読み書きするための setter/getter メソッドを作成して接続し、スクリプト内からはそれを用いるようにします。


<a id="plugins-load"></a>
## プラグインを動的に読み込む

プラグインを独立なクラスファイルの形で用意して、動的に読み込む事もできます。
実際に「 plugin 」フォルダ内に、サンプルのプラグイン実装「 ExamplePlugin1.java 」が同梱されています：

	(in plugin/ExamplePlugin1.java)

	public class ExamplePlugin1 {
    
		// Vnano Engine 上で実行される式やスクリプト内から
		// アクセスしたいフィールド/メソッド
		public double x = 3.4;
		public double f(double arg) {
			return arg * 5.6;
		}
	}

以下のようにコンパイルします：

	cd plugin
	javac ExamplePlugin1.java
	cd ..

加えて、「 plugin 」フォルダ内にテキストファイル "VnanoPluginList.txt" を作成し, 
以下のように、読み込みたいプラグインを記載します：

	(in plugin/VnanoPlyginList.txt)

	ExamplePlugin1.class
	# ExamplePlugin2.class
	# ExamplePlugin3.class
	# ...

ここで「 # 」で始まる行は、コメント扱いで読み飛ばされます。

後述する [コマンドラインモード](#command-line-mode) では、この VnanoPluginList.txt はデフォルトで読み込まれます。一方で、アプリ内に Vnano Engine を組み込んで使う場合は、すぐ下で行うように、明示的に読み込む必要があります。

さて、下準備は以上です。Vnano Engine から読み込んで使っていましょう：

	(in ExampleApp3.java)
	
	import org.vcssl.nano.interconnect.PluginLoader;

	...
	public static void main(String[] args) throws VnanoException {

		// Vnano Engine のインスタンスを生成
		VnanoEngine engine = new VnanoEngine();

		// プラグインを動的に読み込んで Vnano Engine に接続
		PluginLoader pluginLoader = new PluginLoader("UTF-8");
		pluginLoader.setPluginListPath("./plugin/VnanoPluginList.txt");
		pluginLoader.load();
		for (Object plugin: pluginLoader.getPluginInstances()) {
			engine.connectPlugin("___VNANO_AUTO_KEY", plugin);
		}

		...
		(後は ExampleApp1.java と同じです)
	}

コンパイルして実行するには：

	(For Windows)
	javac -cp .;Vnano.jar ExampleApp3.java
	java -cp .;Vnano.jar ExampleApp3

	(For Linux)
	javac -cp .:Vnano.jar ExampleApp3.java
	java -cp .:Vnano.jar ExampleApp3

式の入力をリクエストされるので、以下のように入力します：

	1.2 + f(x)

結果は：

	20.24



<a id="plugins-advanced"></a>
## プラグインに関する発展的な事項 (標準プラグイン、高速インターフェース、等)

基本的な機能群を提供するプラグインは、「 Vnano標準プラグイン 」として公式に提供されています：

* [Vnano標準プラグインのソースコードリポジトリ](https://github.com/RINEARN/vnano-standard-plugin)
* [Vnano標準プラグインの一覧と仕様書](https://www.vcssl.org/en-us/vnano/plugin/)

基礎的な入出力、数学/統計関数、等々は上記の標準プラグインとして提供されているため、自作する必要はありません。

また、高速動作が必要なプラグインを実装したい場合のための、
低オーバーヘッドのプラグインインターフェースも、以下の通り用意されています：

* [Vnano用プラグイン インターフェース のソースコードリポジトリ](https://github.com/RINEARN/vcssl-plugin-interface)

スクリプト内で非常に高頻度に（高速なループ内などから）呼び出されるプラグインを作成する際、
オーバーヘッドを可能な限り削って高速化したい場合などには、上記インターフェースの使用を検討してください。
ただし、メソッドやフィールドをそのまま接続するのに比べて、実装はある程度複雑になります。


<a id="scripting"></a>
## スクリプトを実行する

最初の方でも少し触れましたが、式の代わりに、C言語系の文法で記述したスクリプトを実行する事もできます。
スクリプトの言語名は「 Vnano 」で、構文や言語機能などの詳細は、同梱の以下のドキュメントをご参照ください：

* [言語としての Vnano](LANGUAGE_JAPANESE.md).

実際に Vnano で記述したスクリプトを実行してみましょう：

	(in ExampleApp4.java)

	...
	public static void main(String[] args) throws VnanoException {

		// Vnano Engine のインスタンスを生成
		VnanoEngine engine = new VnanoEngine();

		// 実行するスクリプトの内容を用意
		String script =

			" int sum = 0;                 " +
			" for (int i=1; i<=100; i++) { " +
			"     sum += i;                " +
			" }                            " +
			" sum;                         " ;

		// Vnano Engine でスクリプトを実行
		long result = (Long)engine.executeScript(script);
		System.out.println("result: " + result);
	}

上のように、Vnano Engine の「 executeScript(script) 」メソッドにスクリプトを渡して実行すると、
その戻り値として、「 最終行に書かれた式文（あれば）の値 」が返されます。
従って上の ExampleApp4 の場合は、変数「 sum 」の値が返されるはずです。

実際にコンパイルして実行してみましょう：

	(For Windows)
	javac -cp .;Vnano.jar ExampleApp4.java
	java -cp .;Vnano.jar ExampleApp4

	(For Linux)
	javac -cp .:Vnano.jar ExampleApp4.java
	java -cp .:Vnano.jar ExampleApp4

実行結果は：

	5050

この結果は、1 から 100 までの合計 ( = 100 * 101 / 2 ) に等しいので、スクリプトが期待通りに処理された事がわかります。


<a id="libraries"></a>
## ライブラリ スクリプトを読み込む

Vnano は、言語機能として変数や関数の宣言をサポートしています。
従って、別のスクリプトや計算式で使用するための関数/変数群などをあらかじめ用意しておき、
必要に応じて「部品として」読み込んで使いたい、といった場面も考えられます。そのように、変数/関数群を提供するための部品的なスクリプトの事を、Vnano では「ライブラリ スクリプト」と呼びます。

簡単なライブラリスクリプトの例は、「 lib 」フォルダ内に「 ExampleLibrary1.vnano 」として同梱されています：

	(in lib/ExampleLibrary1.vnano)

	float x = 3.4;
	float f(float arg) {
		return arg * 5.6;
	}

これを読み込むには、同じく「 lib 」フォルダ内にテキストファイル「 VnanoLibraryList.txt 」を作成し、その中で、以下のように読み込みたいライブラリスクリプトを指定します：

	(in lib/VnanoLibraryList.txt)

	ExampleLibrary1.vnano
	# ExampleLibrary2.vnano
	# ExampleLibrary3.vnano

ここで「 # 」で始まる行は、コメント扱いで読み飛ばされます。

後述する [コマンドラインモード](#command-line-mode) では、この VnanoLibraryList.txt はデフォルトで読み込まれます。
一方、Vnano Engine をアプリ内に組み込んで使用する際は、以下のように明示的に指定して読み込む必要があります：

	(in ExampleApp5.java)
	
	import org.vcssl.nano.interconnect.ScriptLoader;

	...
	public static void main(String[] args) throws VnanoException {

		// Vnano Engine のインスタンスを生成
		VnanoEngine engine = new VnanoEngine();

		// ライブラ リスクリプトをファイルから読み込む
		ScriptLoader scriptLoader = new ScriptLoader("UTF-8");
		scriptLoader.setLibraryScriptListPath("./lib/VnanoLibraryList.txt");
		scriptLoader.load();

		// 読み込んだライブラリ スクリプトを VnanoEngine に登録
		String[] libNames = scriptLoader.getLibraryScriptNames();
		String[] libScripts = scriptLoader.getLibraryScriptContents();
		int libCount = libNames.length;
		for (int ilib=0; ilib<libCount; ilib++) {
			engine.includeLibraryScript(libNames[ilib], libScripts[ilib]);
		}

		// 実行するスクリプトの内容を用意
		String script =

			" float value = 1.2 + f(3.4); " +
			" value;                      " ;

		// Vnano Engine でスクリプトを実行
		double result = (Double)engine.executeScript(script);
		System.out.println("result: " + result);
	}

実際にコンパイルして実行してみましょう：

	(For Windows)
	javac -cp .;Vnano.jar ExampleApp5.java
	java -cp .;Vnano.jar ExampleApp5

	(For Linux)
	javac -cp .:Vnano.jar ExampleApp5.java
	java -cp .:Vnano.jar ExampleApp5

結果は：

	result: 20.24


今の場合の「 lib/ExampleLibrary1.vnano 」内の記述では、x は 3.4 で、f(x) は x * 5.6 を返すため、期待される結果は 1.2 + (3.4 * 5.6) = 20.24 です。従って、正しくライブラリスクリプトが読み込まれて処理された事がわかります。


<a id="command-line-mode"></a>
## コマンドラインモード

ここまで説明したアプリ内での組み込み用途に加えて、コマンドライン上で直接、Vnano のスクリプトを実行する事もできます。それには Vnano Engine の「 コマンドラインモード 」を使用します。
コマンドラインモードは、スクリプトやプラグインの作成/デバッグ時などに便利です。

以下が、単体で実行する用のスクリプトのサンプルで、「 ExampleScript1.vnano 」として同梱されています。

	(in ExampleScript1.vnano)

	int sum = 0;
	for (int i=1; i<=100; i++) {
    	sum += i;
	}
	output(sum);

以下のように実行できます：

	java -jar Vnano.jar ExampleScript1.vnano

上記のように引数にスクリプトを指定すると、Vnano.jar はコマンドラインモードで動作し、指定したスクリプトが実行されます。結果は：

	5050

上記のサンプルスクリプトの通り、コマンドラインモードでは、デバッグ等での最低限の利便性を確保するために、「 output 」関数がデフォルトで利用できるようにっています。これは値を出力する関数です。

一方で、アプリ内に組み込んだ Vnano Engine の "executeScript" メソッドでスクリプトを実行する場合は、上述の「 output 」関数も含めて、デフォルトでは何の関数も提供されません（セキュリティの観点から、デフォルトでは完全なサンドボックス状態にするためです）。この差異には留意が必要です。

コマンドラインモードを頻繁に利用する場合は、Vnano 標準プラグインを導入する事をおすすめします。
方法は：

	git clone https://github.com/RINEARN/vnano-standard-plugin
	cd vnano-standard-plugin/plugin
	javac -encoding UTF-8 @org/vcssl/connect/sourcelist.txt
	javac -encoding UTF-8 @org/vcssl/nano/plugin/sourcelist.txt

上記を実行した後に、「 vnano-standard-plugin 」フォルダ内の「 plugin 」フォルダをコピーして、Vnano.jar がある場所の「 plugin 」フォルダに丸ごと上書きペーストします。 
そして、その中の「 VnanoPluginList_AllStandards.txt 」を「 VnanoPluginList.txt 」に名前変更すれば、全ての標準プラグインがコマンドラインモードで使えるようになります。導入は以上です。

標準プラグインを正しく導入できていれば、以下の内容のスクリプトがエラーなく実行できるはずです：

	float value = mean(1.0, 2.0);  // Compute the mean value
	print(value);                  // Prints the value on the terminal

	// result: 1.5

また、[vcssl.org](https://www.vcssl.org/ja-jp/code/#vnano) において、より実践的な内容のサンプルスクリプト類がいくつか配布されていますが、それらも標準プラグインを全て導入した（フル機能の）状態なら実行できるはずです。

なお、コマンドラインモードには、色々と細かい機能があります。それらの一覧と詳細については、--help オプションを指定して、その内容をご参照ください。


<a id="specifications"></a>
## 仕様書

Vnano Engine の全メソッドの一覧/詳細説明や、オプション類などについては、別途文書「 [Vnano Engine の各種仕様](SPEC_JAPANESE.md) 」をご参照ください。


## 本文中の商標などについて

- OracleとJavaは、Oracle Corporation 及びその子会社、関連会社の米国及びその他の国における登録商標です。文中の社名、商品名等は各社の商標または登録商標である場合があります。

- その他、文中に使用されている商標は、その商標を保持する各社の各国における商標または登録商標です。

