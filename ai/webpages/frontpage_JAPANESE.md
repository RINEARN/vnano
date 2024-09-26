# Vnano 公式サイト

ようこそ。ここはスクリプトエンジン/言語「 Vnano 」の公式サイトです。


## Vnanoとは？

### Java&trade;製アプリ内に組み込める、コンパクトなスクリプトエンジン/言語

Vnano（VCSSL nano）は、小型で高速なスクリプトエンジン、およびその上で動作するスクリプト言語です。Java製のソフトウェア内に簡単に組み込めるため、アプリ内スクリプト機能の実装に利用できます。

&raquo; [Vnanoを用いたアプリケーション例： RINPn (関数電卓)](https://www.rinearn.com/ja-jp/rinpn/)

### 普通のスクリプト言語として使う事も可能で、特に演算処理が高速

[VCSSLランタイム](https://www.vcssl.org/ja-jp/download/) という実行環境を用いて、普通のスクリプト言語として使う事もできます。 アプリ内組み込みを重視した言語なため、機能は限定的ですが、演算処理が速いため、計算用途などに便利かもしれません。

&raquo; [例： 積分値を計算するスクリプト](https://www.vcssl.org/ja-jp/code/archive/0001/7800-vnano-integral-output/)

### C言語風のオーソドックスな文法

文法面から見たVnanoは、アプリケーション上でのちょっとしたスクリプト処理に焦点を合わせた、シンプルな簡易言語です。 オーソドックスなC言語風のコードで、C系言語ユーザーが "雰囲気で" 読み書きできます。

\- スクリプト記述例 -

    int sum = 0;
    for (int i=1; i<=100; i++) {
        sum += i;
    }
    output(sum);

&raquo; [詳しく: Vnano の言語の主な文法・仕様](https://www.vcssl.org/ja-jp/vnano/doc/tutorial/language)

### オープンソース、MITライセンス

Vnanoのスクリプトエンジンはオープンソースで、ユーザーはMITライセンスに基づき、商用・非商用問わず無償で利用できます。 また、アプリケーションに合わせて改造したり、派生版を作って公開する事も可能です。

&raquo; [ソースコードリポジトリ](https://github.com/RINEARN/vnano)


## 導入はすぐ完了！ いまここで試してみよう

Vnanoを使い始めるのはとても簡単で、恐らく数分で試せます。実際にここで試してみましょう！

### Step1. Vnanoのスクリプトエンジンをダウンロード

まずは、以下からVnanoのビルド済みパッケージをダウンロード・展開します。 展開されたフォルダ内に、Vnano のスクリプトエンジンである「 Vnano.jar 」が入っています。

[公式サイトのトップページ、ビルド済み版のダウンロードリンクあり](https://www.vcssl.org/ja-jp/vnano/)

    ※ ご使用前に、MITライセンスの免責事項にご同意ください。 &raquo; [ライセンス文書](https://github.com/RINEARN/vnano/blob/master/LICENSE)

    ※ 本格的な利用では、アプリ開発に使うバージョンのJDKで、エンジンもソースコードからビルドするのが無難です。 &raquo; [方法](https://www.vcssl.org/ja-jp/vnano/doc/tutorial/use)

なお、スクリプトエンジンを自作アプリ内に組み込んだりせずに、単にVnanoのスクリプトを（他のスクリプト言語のように）実行して使うだけの場合は、代わりに [VCSSLランタイム](https://www.vcssl.org/ja-jp/download/) を導入してください。

### Step-2. スクリプトファイルを実行してみる

展開フォルダ内には、Vnano で記述されたスクリプトファイルが同梱されています：

\- ExampleScript1.vnano -

    int sum = 0;
    for (int i=1; i<=100; i++) {
        sum += i;
    }
    output(sum);

これは、1から100までの和を計算する内容になっています。 展開フォルダ内にコマンドライン端末で cd し、以下のように実行できます：

    java -jar Vnano.jar ExampleScript1.vnano

    ※ Java開発環境（JDK）が必要です。

なお、[VCSSLランタイム](https://www.vcssl.org/ja-jp/download/) を導入したPC上では、ランタイムを起動して上記スクリプトを選択するだけで、簡単に実行できます （パス設定をすればコマンドでの実行も可能です）。

実行すると以下のように、計算結果が出力されます：

    5050


### Step3. Javaアプリケーションから呼び出してみる

展開フォルダ内には、Vnano のスクリプトエンジンを、 Javaアプリケーションから呼び出して使うサンプルコード類も同梱されています：

\- ExampleApp1.java -

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

これは、「 1.2 + 3.4 」を計算する簡単なスクリプトを処理する内容になっています。 展開フォルダ内にコマンドライン端末で cd し、以下のようにコンパイルして実行できます：

    (Windows の場合)
    javac -cp .;Vnano.jar ExampleApp1.java
    java -cp ".;Vnano.jar" ExampleApp1

    (Linux 等での場合)
    javac -cp .:Vnano.jar ExampleApp1.java
    java -cp ".:Vnano.jar" ExampleApp1

    ※ Java開発環境（JDK）が必要です。

実行結果は：

    result: 4.6

この通り、Vnano のスクリプトエンジンは簡単に導入/使用できます。本格的な使い方は以下をご参照ください：

&raquo; 詳しく: [Vnano チュートリアル ガイド](https://www.vcssl.org/ja-jp/vnano/doc/tutorial/)


## Vnano公式サイト コンテンツ一覧

このVnano公式サイトでは、ガイド類や仕様書等のコンテンツを配信しています（していく予定です）。 上の試用で、Vnanoにより深く触れてみたくなった方は、ぜひご活用ください。

* [Vnano チュートリアル ガイド](https://www.vcssl.org/ja-jp/vnano/doc/tutorial/): Vnano を実際に使用しながら、使い方や特徴などを解説する、実践形式のチュートリアル ガイドです。
* [Vnano Engine の各種仕様](https://www.vcssl.org/ja-jp/vnano/spec/)
Vnano のスクリプトエンジンである VnanoEngine クラスのメソッド群や、オプション項目などの仕様を掲載しています。
* [標準プラグイン](https://www.vcssl.org/ja-jp/vnano/plugin/): 各種の組み込み関数や変数を提供する、標準プラグインの一覧や各機能などの情報を掲載しています。
* [プラグイン開発用インターフェース](https://www.vcssl.org/ja-jp/doc/connect/): プラグインを開発（自作）するための、各種インターフェースの仕様書などを掲載しています。
* [ソースコードリポジトリ (GitHub)](https://github.com/RINEARN/vnano): Vnano のソースコードを管理しているリポジトリです。


## Vnano製のコード一覧

以下では、[VCSSLコードアーカイブ](https://www.vcssl.org/ja-jp/code/)で配信しているコードの中から、Vnanoで記述されているものをリストアップしています。 サンプルコードや、Vnanoを搭載するソフト用のコンテンツとして、ご自由にご利用いただけます。

* [FizzBuzz の答えを表示するプログラム](https://www.vcssl.org/ja-jp/code/archive/0002/0100-vnano-fizz-buzz/): プログラミングの練習問題としても有名な、FizzBuzz 問題の答えを表示するプログラムの例です。

* [ローレンツ方程式を数値的に解くプログラム](https://www.vcssl.org/ja-jp/code/archive/0001/8000-vnano-lorenz-attractor/): ローレンツ方程式を4次ルンゲ=クッタ法によって解き、グラフ描画用のデータを出力するプログラムです。

* [積分値を求めるプログラム (数値積分)](https://www.vcssl.org/ja-jp/code/archive/0001/7800-vnano-integral-output/): 矩形法/台形法/シンプソン法を用いて、積分の値を数値的に求めるコードです。

* [積分値のグラフ描画用データを出力するプログラム](https://www.vcssl.org/ja-jp/code/archive/0001/7900-vnano-integral-for-plot-graph/): 数値的に積分を行い、結果の関数をグラフに描くためのデータを出力するコードです。


---

\- 本文中の商標等について -

* OracleとJavaは、Oracle Corporation 及びその子会社、関連会社の米国及びその他の国における登録商標です。文中の社名、商品名等は各社の商標または登録商標である場合があります。
* Microsoft Windowsは、米国 Microsoft Corporation の米国およびその他の国における登録商標です。
* Linux は、Linus Torvalds 氏の米国およびその他の国における商標または登録商標です。
* その他、文中に使用されている商標は、その商標を保持する各社の各国における商標または登録商標です。

