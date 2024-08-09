# Vnano

( &raquo; [English README](./README.md) )

![ロゴ](./logo.png)


Vnano は、C言語系のシンプルな文法を持つスクリプト言語です。 
Vnano のインタープリタは、特にJava&trade;製のアプリケーション内に組み込んで使う事を想定して開発されています。
Vnano を用いる事で、自作アプリ上でスクリプトを実行可能になるため、カスタマイズ性の高いアプリの開発が可能になります。

* [Vnano 公式サイト](https://www.vcssl.org/ja-jp/vnano/)
* [ドキュメントの一覧](doc/README_JAPANESE.md)


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

- Java Development Kit (バージョン 8 以降が必須)
- Git


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
ここでは例として、非常に単純な内容のアプリケーションを作成します。
ソースコードはリポジトリ内に「 ExampleApp1.java 」として同梱されています：

    (in ExampleApp1.java)

    import org.vcssl.nano.VnanoEngine;
    import org.vcssl.nano.VnanoException;

     public class ExampleApp1 {
        public static void main(String[] args) throws VnanoException {

            // Vnano のスクリプト実行エンジン（Vnano Engine）のインスタンスを生成
            VnanoEngine engine = new VnanoEngine();

            // 単純な内容のスクリプトを用意し、Vnano Engine で実行
            String script = "double a = 1.2;  double b = 3.4;  double c = a + b;  c;";
            double result = (Double)engine.executeScript(script);

            // 結果を表示
            System.out.println("result: " + result);
        }
    }

上記のコードは、以下のようにコンパイルできます：

    javac -cp .;Vnano.jar ExampleApp1.java        (For Windows)
    javac -cp .:Vnano.jar ExampleApp1.java        (For Linux)

そして以下のように実行します：

    java -cp .;Vnano.jar ExampleApp1        (For Windows)
    java -cp .:Vnano.jar ExampleApp1        (For Linux)

実行結果は：

    result: 5.6

上記の ExampleApp1 アプリは、Vnano Engine を用いてスクリプトを処理しますが、そのスクリプトは 1.2 + 3.4 の値（ = 5.6 ）を計算する内容になっています。従って、無事 Vnano Engine を使って、スクリプトを正しく実行できた事がわかります。

Vnano Engine の各機能に関する詳細は、別途文書 [Vnano Engine の主な機能](doc/FEATURE_JAPANESE.md) の項目をご参照ください。


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

加えて、Javaで記述した任意のクラスのフィールドやメソッドを Vnano Engine に登録して、式やスクリプト内からアクセスする事もできます。さらに、そのようなフィールド/メソッド提供用のクラスを独立なファイルの形で実装して（「プラグイン」と呼びます）、動的に読み込む事もできます。

Javaで記述したクラスの代わりに、スクリプトファイルとして変数/関数群を定義して、それを「ライブラリ スクリプト」として読み込んで使う事もできます。

このように、Vnano Engine の色々な機能を使うと、カスタマイズ性の高いアプリを開発する事ができます
（具体例としては、プログラム関数電卓ソフトの「 [RINPn](https://github.com/RINEARN/rinpn) 」をご参照ください）。


各機能の詳細については、別途文書「 [Vnano Engine の主な機能と用例](doc/FEATURE_JAPANESE.md) 」をご参照ください。

また、Vnano Engine の全メソッドの一覧/詳細説明や、オプション類などについては、別途文書「 [Vnano Engine の各種仕様](doc/SPEC_JAPANESE.md) 」をご参照ください。


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
Vnano の構文や言語機能について詳しくは、別途文書「 [言語としての Vnano](doc/LANGUAGE_JAPANESE.md) 」をご参照ください。


<a id="performances"></a>
## 処理速度

Vnano は、データ解析ソフトや計算ソフト、および可視化ソフトなど、演算量の多い用途も想定して開発されています。そのため、スクリプト言語としては、処理速度は恐らくそれなりに速い部類に入ります。

例えば、一般のノートPC上において、理想条件下での上限値として、スカラ（非配列）演算で約7億回/秒（700MFLOPS）、配列演算で約150億回/秒（15GFLOPS）程度の実測スコアが得られています。

計測方法やパフォーマンスチューニングなどに関する詳細は「 [パフォーマンス計測と解析](doc/FEATURE_JAPANESE.md#performances) 」の項目をご参照ください。



<a id="about-us"></a>
## 開発元について

Vnano は、日本の個人運営の開発スタジオ [RINEARN](https://www.rinearn.com/) が開発しています。Vnano Engine の著者は松井文宏（RINEARN代表）です。ご質問やフィードバックなどをお持ちの方は、ぜひ御気軽にどうぞ。


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

