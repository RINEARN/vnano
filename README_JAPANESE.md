# Vnano

( &raquo; [English README](./README.md) )

![ロゴ](./logo.png)


Vnano は、C言語系のシンプルな文法を持つスクリプト言語です。 
Vnano のインタープリタは、特にJava&reg;製のアプリケーション内に組み込んで使う事を想定して開発されています。
Vnano を用いる事で、自作アプリ上でスクリプトを実行可能になるため、カスタマイズ性の高いアプリの開発が可能になります。

* [Vnano 公式サイト](https://www.vcssl.org/ja-jp/vnano/)

参考： Vnano を用いたアプリケーションの例: [RINPn](https://github.com/RINEARN/rinpn).


## 目次

( &raquo; [English](./README.md) )

- [ライセンス](#license)
- [必要なもの](#requirements)
- [ビルド方法](#build)
	- [Microsoft&reg; Windows&reg; での場合](#build-win)
	- [Linux 等での場合](#build-lin)
	- [Apache Ant を使用する場合](#build-ant)
- [アプリケーションへの組み込み/使用方法](#use)
	- [アプリケーションのコンパイル/実行方法](#use-compile-and-run)
	- [アプリケーションのJARファイルの作成方法](#use-create-jar)
- [主な機能と仕様](#features)
- [言語としての Vnano](#language)
- [処理速度](#performances)
- [開発元について](#about-us)
- [参考情報やリンク](#references)


<hr />




<a id="license"></a>
## ライセンス

このリポジトリは、Vnano のインタープリタである Vnano Engine のソースコードリポジトリです。

Vnano Engine はMITライセンスの下でリリースされています。


<a id="requirements"></a>
## 必要なもの

- Java Development Kit (バージョン 8 以降が必須、最新版の Java 18 は対応済み)


<a id="build"></a>
## ビルド方法

まずはじめに、Vnano Engine をビルドしましょう。

<a id="build-win"></a>
### Microsoft Windows での場合

このリポジトリを clone して、同梱されているバッチファイル「 build.bat 」を実行します:

	git clone https://github.com/RINEARN/vnano
	cd vnano
	.\build.bat

すると、ビルド結果のJARファイル「 Vnano.jar 」が生成されます。

<a id="build-lin"></a>
### Linux 等での場合

このリポジトリを clone して、同梱されているシェルスクリプト「 build.sh 」を実行します：

	git clone https://github.com/RINEARN/vnano
	cd vnano
	sudo chmod +x ./build.sh
	./build.sh

すると、ビルド結果のJARファイル「 Vnano.jar 」が生成されます。


<a id="build-ant"></a>
### Apache Ant を使用する場合

Ant 用のビルドファイルも同梱されています：

	git clone https://github.com/RINEARN/vnano
	cd vnano
	ant -f build.xml

すると、ビルド結果のJARファイル「 Vnano.jar 」が生成されます。


<a id="use"></a>
## アプリケーションへの組み込み/使用方法

<a id="use-compile-and-run"></a>
### アプリケーションのコンパイル/実行方法

続いて、実際にアプリケーションに Vnano Engine を組み込んで使ってみましょう。
ここでは例として、入力された式を計算する簡単なアプリケーションを作成します。
ソースコードはリポジトリ内に「 ExampleApp1.java 」として同梱されています：

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

スクリプト実行に関する詳細は [スクリプトの実行](FEATURE_JAPANESE.md#scripting) の項目をご参照ください。

ところで、式ではなくスクリプトを実行する用途においては、以下のオプション指定の行は削除する事をおすすめします：

	optionMap.put("EVAL_INT_LITERAL_AS_FLOAT", true);

既に述べた通り、上記オプションは式内に書かれた整数値（整数リテラル）を float 型の値として扱うもので、
式の計算には便利です（指定しないと、整数同士の除算結果が整数になって厄介です）。一方で、スクリプトに対して効かせると混乱の元になりかねません。

ただし、上記オプションは executeScript メソッドで直接実行する内容に対してのみ作用し、[ライブラリスクリプト](FEATURE_JAPANESE.md#libraries) に対しては作用しないため、ライブラリの処理内容への影響を心配する必要はありません。


<a id="use-create-jar"></a>
### アプリケーションのJARファイルの作成方法

続いて、上で実行した ExampleApp1 アプリケーションを、JARファイル化してみましょう。
それには、まずマニフェストファイル「 manifest.txt 」を作成して、
その中で「 Vnano.jar 」にクラスパスを通します:

	Main-Class: ExampleApp1
	Class-Path: . Vnano.jar

	(!!! 重要: このファイルの内容は空行で終わっている必要があります !!!)

なお、もし「 Vnano.jar 」をどこか別の場所（例えば lib フォルダ内など）に配置する事を想定している場合は、上記の Class-Path セクションに書くパスも適切に合わせてください（「Class-Path: . lib/Vnano.jar」など）。

以上が済んだら、以下のようにJARファイルを生成します：

	jar cvfm ExampleApp1.jar manifest.txt ExampleApp1.class

生成したJARファイルは、以下のように実行できます：

	java -jar ExampleApp1.jar


<a id="features"></a>
## 主な機能と仕様

上でも見てきたように、Vnano Engine を用いると、アプリ上で式やスクリプトを実行する事ができます。

加えて、Javaで記述した任意のクラスのフィールドやメソッドを Vnano Engine 登録して、式やスクリプト内からアクセスする事もできます。さらに、そのようなフィールド/メソッド提供用のクラスを独立なファイルの形で実装して（「プラグイン」と呼びます）、動的に読み込む事もできます。

Javaで記述したクラスの代わりに、スクリプトファイルとして変数/関数群を定義して、それを「ライブラリ スクリプト」として読み込んで使う事もできます。

このように、Vnano Engine の色々な機能を使うと、カスタマイズ性の高いアプリを開発する事ができます
（具体例としては、プログラム関数電卓ソフトの「 [RINPn](https://github.com/RINEARN/rinpn) 」をご参照ください）。


各機能の詳細については、別途文書「 [Vnano Engine の主な機能](FEATURE_JAPANESE.md) 」をご参照ください。

また、Vnano Engine の全メソッドの一覧/詳細説明や、オプション類などについては、別途文書「 [Vnano Engine の各種仕様](SPEC_JAPANESE.md) 」をご参照ください。


<a id="language"></a>
## 言語としての Vnano

Vnano Engine 上で実行可能なスクリプト言語の名前は、そのまま「 Vnano 」と言います。
Vnano は C言語系のシンプルな文法を持つ言語です。例えば：

	int sum = 0;
	for (int i=1; i<=100; i++) {
    	sum += i;
	}
	output(sum);

上記コードのような具合です。
Vnano の構文や言語機能について詳しくは、別途文書「 [言語としての Vnano](LANGUAGE_JAPANESE.md) 」をご参照ください。


<a id="performances"></a>
## 処理速度

Vnano は、データ解析ソフトや計算ソフト、および可視化ソフトなど、演算量の多い用途も想定して開発されています。
そのため、処理速度は恐らくそれなりに高速な部類に入ります。
このリポジトリには、実際に処理速度を計測するためのベンチマークスクリプト類も、「 benchmark 」フォルダ内に同梱されています。

例えば、64-bit 浮動小数点数によるスカラ（非配列）演算のベンチマークを実行するには：

	java -jar Vnano.jar benchmark/ScalarFlops.vnano --accelerator true --optLevel 3

結果は以下の通りです：

	OPERATING_SPEED = 704.6223224351747 [MFLOPS]
	REQUIRED_TIME = 14.192 [SEC]
	TOTAL_OPERATIONS = 10000000000 [xFLOAT64_ADD]
	OPERATED_VALUE = 1.0E10

上記はミドルスペックのノートPCでの実測値です。

続いて、64-bit 浮動小数点数によるベクトル（配列）演算のベンチマークを実行するには：

	java -jar Vnano.jar benchmark/VectorFlops.vnano --accelerator true --optLevel 3

結果は：

	OPERATING_SPEED = 15.400812152203338 [GFLOPS]
	REQUIRED_TIME = 13.298 [SEC]
	TOTAL_OPERATIONS = 204800000000 [xFLOAT64_ADD]
	VECTOR_SIZE = 2048 [x64BIT]
	OPERATED_VALUES = { 1.0E8, 2.0E8, 3.0E8, ... 2.047E11, 2.048E11 }

以上の通りです。なお、配列演算の速度は、演算対象の配列サイズ、およびCPUのキャッシュサイズ等に大きく依存する事に留意が必要です。


<a id="about-us"></a>
## 開発元について

Exevalator は、日本の個人運営の開発スタジオ [RINEARN](https://www.rinearn.com/) が開発しています。著者は松井文宏です。ご質問やフィードバックなどをお持ちの方は、ぜひ御気軽にどうぞ。


<a id="references"></a>
## 参考情報

Vnano についての情報をもっと知りたい場合は、以下のウェブサイトや記事などが参考になるかもしれません。

* [Vnano 公式サイト](https://www.vcssl.org/ja-jp/vnano/)
* [Vnano 標準プラグイン](https://www.vcssl.org/ja-jp/vnano/plugin/)
* [VCSSL/Vnano 用プラグイン インターフェース](https://github.com/RINEARN/vcssl-plugin-interface)
* [Vnanoのスクリプトエンジンアーキテクチャ解説1: 全体像](https://www.rinearn.com/ja-jp/info/news/2019/0528-vnano-architecture) - RINEARN お知らせ 2019/07/03
* [Vnanoのスクリプトエンジンアーキテクチャ解説2: コンパイラ](https://www.rinearn.com/ja-jp/info/news/2019/0703-vnano-compiler) - RINEARN お知らせ 2019/07/03


## 本文中の商標などについて

- OracleとJavaは、Oracle Corporation 及びその子会社、関連会社の米国及びその他の国における登録商標です。文中の社名、商品名等は各社の商標または登録商標である場合があります。

- Windows、C#、Visual Studio は米国 Microsoft Corporation の米国およびその他の国における登録商標です。

- Linux は、Linus Torvalds 氏の米国およびその他の国における商標または登録商標です。

- その他、文中に使用されている商標は、その商標を保持する各社の各国における商標または登録商標です。

