
# Vnano Engine の主な機能と用例

## 目次

( &raquo; [English](FEATURE.md) )

- [式を計算する](#calculate-expression)
- [Java&reg;製クラスのフィールド/メソッドにアクセスする](#fields-and-methods)
- [メソッド/フィールドを提供するクラス（プラグイン）を、独立なファイルに定義する](#plugin-import)
- [リストファイルで指定したプラグインを読み込む](#plugins-load)
- [プラグインに関する発展的な事項 (標準プラグイン、高速インターフェース、等)](#plugins-advanced)
- [スクリプトを実行する](#scripting)
- [ライブラリ スクリプトを読み込む](#libraries)
- [オプション設定](#option)
- [パーミッション設定](#permission)
- [コマンドラインモード](#command-line-mode)
- [パフォーマンス計測と解析](#performances)
- [反復実行時のオーバーヘッドを削る](#repetitive)
- [仕様書](#specifications)

<hr />


<a id="calculate-expression"></a>
## 式を計算する

Vnano Engine はスクリプトを実行できるエンジンですが、単に、式の値を計算するのにも使用できます。
現実の用途においては、式の計算の方が多いかもしれません。そのため、式の計算から解説を始めましょう。

以下は、ユーザーが入力した式の値を計算するサンプルコードです：

    (in ExampleApp2.java)

    import org.vcssl.nano.VnanoEngine;
    import org.vcssl.nano.VnanoException;
    import java.util.Map;
    import java.util.HashMap;
    import java.util.Scanner;

    public class ExampleApp2 {
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

ここで注意が必要なのは、Vnano Engine では、式でもスクリプトでも、行の最後に「 ; 」が必要な事です。そのため、上では自動的に付けるようにしています。

さて、上記のコードは、以下のようにコンパイルできます：

    javac -cp .;Vnano.jar ExampleApp2.java        (For Windows)
    javac -cp .:Vnano.jar ExampleApp2.java        (For Linux)

そして以下のように実行します：

    java -cp .;Vnano.jar ExampleApp2        (For Windows)
    java -cp .:Vnano.jar ExampleApp2        (For Linux)

ここで上記の「 ExampleApp2 」は、ユーザーに式を入力するようリクエストしてきます。
従って以下のように式を入力し、エンターキーを押します：

    1.2 + 3.4 * 5.6

すると、入力した式が Vnano Engine で計算され、結果が以下のように表示されます：

    result: 20.24

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
## Java製クラスのフィールド/メソッドにアクセスする

Javaで実装した任意のクラスのフィールド/メソッドを Vnano Engine に接続して、
その上で実行する式やスクリプト内からアクセスする事ができます。

以下はその例です：

    (in ExampleApp3.java)

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
        (後は ExampleApp2.java と同じです)
    }


コンパイルして実行してみましょう:

    (For Windows)
    javac -cp .;Vnano.jar ExampleApp3.java
    java -cp .;Vnano.jar ExampleApp3

    (For Linux)
    javac -cp .:Vnano.jar ExampleApp3.java
    java -cp .:Vnano.jar ExampleApp3

実行すると式の入力を求められるので、下記の通りに入力します:

    1.2 + f(x)

今の ExampleApp3 内の AnyClass クラスの実装では、x は 3.4 で、f(x) は x * 5.6 を返すため、期待される結果は 1.2 + (3.4 * 5.6) = 20.24 です。実際に表示される結果も：

    result: 20.24

なお、以下のようにクラス/インスタンス自体を connectPlugin メソッドに渡す事で、それに属する全てのフィールド/メソッドを一括で接続する事もできます：

    (in ExampleApp3.java, 変更内容)

    ...
    VnanoEngine engine = new VnanoEngine();

    AnyClass anyClassInstance = new AnyClass();
    engine.connectPlugin("AnyNamespace", anyClassInstance);
    ...

こうすると、スクリプト内で先と全く同様に x と f(x) にアクセスできる他に、必要に応じて、それらの名前の頭に名前空間「 AnyNamespace. 」を付けてアクセスする事もできます。同じクラスのインスタンスを複数接続する場合に、競合を防ぐために別々の名前空間を割り当てるなどの用途があります。

ところで、接続したJava側のフィールド値（上の例では「 x 」の値）を、スクリプト内から書き換える事も可能です。ただしそれに関して、以下のような Vnano Engine の挙動を意識しておく必要があります：
**Vnano Engine は、スクリプトや式の実行直前に、接続されたJavaフィールドの値を読み、それを内部にキャッシュします。そして実行処理が完了したタイミングで、キャッシュされた値（スクリプト内容によっては、書き換えられた値）が、Javaフィールドに書き戻されます。**

従って、Java側とスクリプト側の間で、実行中に何度も更新し合うような値については、フィールドとして直接 Vnano Engine に接続してアクセスするような事はしないでください。そのような場面では、その値を読み書きするための setter/getter メソッドを作成して接続し、スクリプト内からはそれを用いるようにします。


<a id="plugins-import"></a>
## メソッド/フィールドを提供するクラス（プラグイン）を、独立なファイルに定義する

上の例での AnyClass クラスのように、式やスクリプト内からアクセスするフィールドやメソッド、その他諸機能などを提供するJava製クラスを、Vnano Engine では「 **プラグイン** 」と呼びます。（また、スクリプト内で宣言された変数/関数と区別するため、プラグインが提供するフィールド/メソッドの事を「 **外部変数/関数** 」と呼ぶ事もあります。）

先の例では、AnyClass プラグインを、ExampleApp3 クラスの内部クラスとして作成していました。
しかしもちろん、適当なパッケージ内の独立なクラスとして定義する事もできます：

    (適当なパッケージ内の独立なクラスとして)
    package anypackage;
    public class AnyClass {

        // Vnano Engine 上で実行される式やスクリプト内から
        // アクセスしたいフィールド/メソッド
        public double x = 3.4;
        public double f(double arg) {
            return arg * 5.6;
        }
    }

このような場合は、アプリケーションから普通に import して使用すれば OK です：

    (in ExampleApp3.java, 変更内容)

    import anypackage.AnyClass;

    ...
    VnanoEngine engine = new VnanoEngine();

    AnyClass anyClassInstance = new AnyClass();
    engine.connectPlugin("AnyNamespace", anyClassInstance);
    ...

動作は前の例と同じです。


<a id="plugins-load"></a>
## リストファイルで指定したプラグインを読み込む

上の例では、「どのプラグインを import して接続するか」が、アプリケーションのコンパイル時点で決まっている必要があります。
一方で、読み込むプラグインを、ユーザーが自由に決められるようにしたい場合もあります。
少し複雑になりますが、実際に行ってみましょう。

まず「 plugin 」フォルダ内に、サンプルのプラグイン実装「 ExamplePlugin1.java 」が同梱されています：

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

加えて、「 plugin 」フォルダ内にテキストファイル（リストファイル）「 VnanoPluginList.txt 」を作成し, 
以下のように、読み込みたいプラグインを記載します：

    (in plugin/VnanoPlyginList.txt)

    ExamplePlugin1.class
    # ExamplePlugin2.class
    # ExamplePlugin3.class
    # ...

ここで「 # 」で始まる行は、コメント扱いで読み飛ばされます。

後述する [コマンドラインモード](#command-line-mode) では、この VnanoPluginList.txt はデフォルトで読み込まれます。一方で、アプリ内に Vnano Engine を組み込んで使う場合は、すぐ下で行うように、明示的に読み込む必要があります。

さて、下準備は以上です。Vnano Engine から読み込んで使っていましょう：

    (in ExampleApp4.java)
    
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
        (後は ExampleApp2.java と同じです)
    }

コンパイルして実行するには：

    (For Windows)
    javac -cp .;Vnano.jar ExampleApp4.java
    java -cp .;Vnano.jar ExampleApp4

    (For Linux)
    javac -cp .:Vnano.jar ExampleApp4.java
    java -cp .:Vnano.jar ExampleApp4

式の入力をリクエストされるので、以下のように入力します：

    1.2 + f(x)

結果は：

    result: 20.24



<a id="plugins-advanced"></a>
## プラグインに関する発展的な事項 (標準プラグイン、高速インターフェース、等)

基本的な機能群を提供するプラグインは、「 Vnano標準プラグイン 」として公式に提供されています：

* [Vnano標準プラグインのソースコードリポジトリ](https://github.com/RINEARN/vnano-standard-plugin)
* [Vnano標準プラグインの一覧と仕様書](https://www.vcssl.org/ja-jp/vnano/plugin/)

基礎的な入出力、数学/統計関数、等々は上記の標準プラグインとして提供されているため、自作する必要はありません。

また、高速動作が必要なプラグインを実装したい場合のための、
低オーバーヘッドのプラグインインターフェースも、以下の通り用意されています：

* [Vnano用プラグイン インターフェース のソースコードリポジトリ](https://github.com/RINEARN/vcssl-plugin-interface)
* [Vnano用プラグイン インターフェース のドキュメント類](https://www.vcssl.org/ja-jp/doc/connect/)

スクリプト内で非常に高頻度に（高速なループ内などから）呼び出されるプラグインを作成する際、
オーバーヘッドを可能な限り削って高速化したい場合などには、上記インターフェースの使用を検討してください。
ただし、メソッドやフィールドをそのまま接続するのに比べて、実装はある程度複雑になります。


<a id="scripting"></a>
## スクリプトを実行する

最初の方でも少し触れましたが、式の代わりに、C言語系の文法で記述したスクリプトを実行する事もできます。
スクリプトの言語名は「 Vnano 」で、構文や言語機能などの詳細は、同梱の以下のドキュメントをご参照ください：

* [言語としての Vnano](LANGUAGE_JAPANESE.md).

実際に Vnano で記述したスクリプトを実行してみましょう：

    (in ExampleApp5.java)

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
従って上の ExampleApp5 の場合は、変数「 sum 」の値が返されるはずです。

実際にコンパイルして実行してみましょう：

    (For Windows)
    javac -cp .;Vnano.jar ExampleApp5.java
    java -cp .;Vnano.jar ExampleApp5

    (For Linux)
    javac -cp .:Vnano.jar ExampleApp5.java
    java -cp .:Vnano.jar ExampleApp5

実行結果は：

    result: 5050

この結果は、1 から 100 までの合計 ( = 100 * 101 / 2 ) に等しいので、スクリプトが期待通りに処理された事がわかります。


<a id="libraries"></a>
## ライブラリ スクリプトを読み込む

Vnano は、言語機能として変数や関数の宣言をサポートしています。
従って、別のスクリプトや計算式で使用するための関数/変数群などをあらかじめ用意しておき、
必要に応じて「部品として」読み込んで使いたい、といった場面も考えられます。そのように、変数/関数群を提供するための部品的なスクリプトの事を、Vnano では「**ライブラリ スクリプト**」と呼びます。

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

    (in ExampleApp6.java)
    
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
        String[] libPaths = scriptLoader.getLibraryScriptPaths(true);
        String[] libScripts = scriptLoader.getLibraryScriptContents();
        int libCount = libScripts.length;
        for (int ilib=0; ilib<libCount; ilib++) {
            engine.registerLibraryScript(libPaths[ilib], libScripts[ilib]);
        }

        // 実行するスクリプトの内容を用意
        String script =

            " float value = 1.2 + f(x); " +
            " value;                      " ;

        // Vnano Engine でスクリプトを実行
        double result = (Double)engine.executeScript(script);
        System.out.println("result: " + result);
    }

実際にコンパイルして実行してみましょう：

    (For Windows)
    javac -cp .;Vnano.jar ExampleApp6.java
    java -cp .;Vnano.jar ExampleApp6

    (For Linux)
    javac -cp .:Vnano.jar ExampleApp6.java
    java -cp .:Vnano.jar ExampleApp6

結果は：

    result: 20.24


今の場合の「 lib/ExampleLibrary1.vnano 」内の記述では、x は 3.4 で、f(x) は x * 5.6 を返すため、期待される結果は 1.2 + (3.4 * 5.6) = 20.24 です。従って、正しくライブラリスクリプトが読み込まれて処理された事がわかります。


<a id="option"></a>
## オプション設定

これまでのサンプルコード内にも何度か登場しましたが、Vnano Engine はオプション設定項目を持っています。
その設定を行うには、設定項目名と設定値を格納する Map<String, Object> を作成して（これをオプションマップと呼びます）、それを VnanoEngine クラスの setOptionMap(Map<String, Object>) に渡します：

    import java.util.Map;
    import java.util.HashMap;
    ...

    ( 以下、Vnano Engine を使うメソッド内 )
    
    // オプションの項目名と設定値を格納する Map を作成（オプションマップ）
    Map<String, Object> optionMap = new HashMap<String, Object>();

    // 整数リテラルを浮動小数点数として解釈するオプションを有効化
    optionMap.put("EVAL_INT_LITERAL_AS_FLOAT", true);

    // 数値演算などに関して、浮動小数点数の値/変数のみ使用可能にするオプションを有効化
    optionMap.put("EVAL_ONLY_FLOAT", true);

    // 式のみ実行可能にするオプションを有効化
    optionMap.put("EVAL_ONLY_EXPRESSION", true);

    // Vnano Engine のインスタンスに設定
    try {
        engine.setOptionMap(optionMap);
    } catch (VnanoException e) {
        System.err.println("オプション設定内容に関するエラーが見つかりました。");
        e.printStackTrace();
    }

上記は、計算用途のソフトなどで、入力された式を Vnano Engine で計算する際、整数同士の演算をさせたくない（いわゆる整数除算で混乱させたくない）場合を想定した設定です。
設定可能なオプションの項目名と設定値については、[Vnano Engine の各種仕様](SPEC_JAPANESE.md) をご参照ください。


<a id="permission"></a>
## パーミッション設定

プラグインやライブラリの中には、例えばファイルの読み書きなど、「 用途や場面によっては拒否したいかもしれない 」機能を提供するものもあります。そういった場面のために、Vnano Engine では「 パーミッション制御 」の仕組みを供えています。

上記のような処理を行うプラグインや、それを用いるライブラリは、実行時に Vnano Engine に処理の種類を伝えて、許可を要求します。この事を「 パーミッションを要求する/される 」などと言います。

標準では、Vnano Engine は全てのパーミッション要求を拒否するように設定されています。従って、何らかのパーミッションが必要な機能を使うには、それを許可するように設定変更する必要があります。パーミッションの設定は、設定項目名と設定値を格納する Map<String, String> を作成して（これをパーミッションマップと呼びます）、それを VnanoEngine クラスの setPermissionMap(Map<String, String>) に渡します：

    import org.vcssl.connect.ConnectorPermissionName;
    import org.vcssl.connect.ConnectorPermissionValue;
    import java.util.Map;
    import java.util.HashMap;
    ...

    ( 以下、Vnano Engine を使うメソッド内 )

    // パーミッションの項目名と設定値を格納する Map を作成（パーミッションマップ）
    Map<String, String> permissionMap = new HashMap<String, String>();

    // デフォルトは拒否に設定（未設定項目に適用される）
    permissionMap.put(ConnectorPermissionName.DEFAULT, ConnectorPermissionValue.DENY);

    // ファイルの新規作成、（上書き以外の）書き込み、読み込みは許可する
    permissionMap.put(ConnectorPermissionName.FILE_CREATE, ConnectorPermissionValue.ALLOW);
    permissionMap.put(ConnectorPermissionName.FILE_WRITE, ConnectorPermissionValue.ALLOW);
    permissionMap.put(ConnectorPermissionName.FILE_READ, ConnectorPermissionValue.ALLOW);

    // ファイルの上書きは、実行時にユーザーに尋ねて決める
    permissionMap.put(ConnectorPermissionName.FILE_OVERWRITE, ConnectorPermissionValue.ASK);

    // Vnano Engine のインスタンスに設定
    try {
        engine.setPermissionMap(permmissionMap);
    } catch (VnanoException e) {
        System.err.println("パーミッション設定内容に関するエラーが見つかりました。");
        e.printStackTrace();
    }

以上のような具合です。パーミッションの項目名と設定値については、[Vnano Engine の各種仕様](SPEC_JAPANESE.md) をご参照ください。

なお、後の節で扱う[コマンドラインモード](#command-line-mode)では、--permission オプションを用いて、いくつかの既定の設定から選んで指定できます：

    java -jar Vnano.jar --permission askAll Script.vnano

上記では、全てのパーミッション要求について、ユーザーに訪ねて決める設定である「 askAll 」を指定しています。他にも、全て拒否/許可する「 denyAll 」「 allowAll 」や、一般的な用途を想定してバランスを取った「 balanced 」などが指定できます。詳細は --help オプションの説明を参照してください。

ところで、自作のプラグインなどで、パーミッション制御をサポートしたい場合（ユーザー側で処理の実行/拒否を判断してほしい場合）は、[XFCI1](https://www.vcssl.org/ja-jp/doc/connect/ExternalFunctionConnectorInterface1_SPEC_JAPANESE) や [XNCI1](https://www.vcssl.org/ja-jp/doc/connect/ExternalNamespaceConnectorInterface1_SPEC_JAPANESE) などのプラグイン実装用インターフェースを実装した上で、初期化時に渡される [エンジンコネクター インターフェース](https://www.vcssl.org/ja-jp/doc/connect/EngineConnectorInterface1_SPEC_JAPANESE) の requestPermission(...) メソッドを呼び出してください。
特に、ファイルの上書き等はよくある処理なので、プラグインごとに個別に確認処理を実装するよりも、パーミッションを要求する方が、アプリ側やユーザー側にとって管理しやすくなります。


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

    float value = mean(1.0, 2.0);  // 平均値を計算
    print(value);                  // 端末上に表示

    // result: 1.5

また、[vcssl.org](https://www.vcssl.org/ja-jp/code/#vnano) において、より実践的な内容のサンプルスクリプト類がいくつか配布されていますが、それらも標準プラグインを全て導入した（フル機能の）状態なら実行できるはずです。

なお、コマンドラインモードには、色々と細かい機能があります。それらの一覧と詳細については、--help オプションを指定して、その内容をご参照ください。


<a id="performances"></a>
## パフォーマンス計測と解析

Vnano Engine では、データ解析/計算ソフトなどでの使用も想定して、処理速度を重視した設計を採用しています。
このリポジトリ内には、実際に使用している環境における、Vnano Engine の処理速度の上限値を計測するためのベンチマークスクリプト類も同梱されています。

例えば、64-bit 浮動小数点数（FP64）によるスカラ（非配列）演算のベンチマークを実行するには：

    java -jar Vnano.jar benchmark/ScalarFlops.vnano

結果は以下の通りです：

    OPERATING_SPEED = 704.6223224351747 [MFLOPS]
    ...

ここで「 MFLOPS 」は浮動小数点数の演算速度の単位で、1MFLOPS = 100万回演算/秒です。
従って上記の結果は、FP64演算が Vnano Engine 上で約7億回/秒の速度で実行された事を表しています（ミドルスペックのノートPC上での計測値です）。

続いて、64-bit 浮動小数点数（FP64）によるベクトル（配列）演算のベンチマークを実行するには：

    java -jar Vnano.jar benchmark/VectorFlops.vnano

結果は：

    OPERATING_SPEED = 15.400812152203338 [GFLOPS]
    ...

以上の通りです。「 GFLOPS 」も浮動小数点数の演算速度の単位で、1GFLOPS = 10億回演算/秒です。
従って上記の結果は、FP64演算が Vnano Engine 上で約150億回/秒の速度で実行された事を表しています。
なお、配列演算の速度は、演算対象の配列サイズ、およびCPUのキャッシュサイズ等に大きく依存する事に留意が必要です。

ところで、何らかの目的を持つ、実際のスクリプトのパフォーマンスチューニングを行う際には、そのための解析を行うコマンドラインオプション「 --perf all 」が有用です：

    java -jar Vnano.jar  --perf all 解析対象のスクリプト.vnano

実行結果の例は：

    (概ね 1 秒に 1 回表示されます)

    ==================================================
    = Performance Monitor (2022-05-07 14:16:39.28)
    = - VM Speed  = 384.2 MHz (VRIL Instructions / sec)
    = - RAM Usage = 21.8 MiB (Max 16.0 GiB Available)
    = - Instruction Execution Frequency :
        -     MOV :  34.83 %   (938 count)
        -     MUL :  23.80 %   (641 count)
        -     ADD :  21.50 %   (579 count) 
        -     DIV :   7.69 %   (207 count)
        -     NEG :   5.46 %   (147 count)
        -     SUB :   4.57 %   (123 count)
        -    JMPN :   0.76 %   (20.5 count)
        -     REM :   0.63 %   (17 count)
        -      LT :   0.58 %   (15.5 count)
        -      EQ :   0.19 %   (5 count)
        (Total 3686 Samples)
    ==================================================

結果の「 Instruction Execution Frequency 」セクションの内容において、
算術演算（ADD, SUB, MUL, DIV, REM, NEG）と外部関数呼び出し（CALLX）とMOV命令を合計した比率が十分大きい場合には、そのスクリプトは十分に最適化されています。

それに対して、JMP/JMPN や LT/GT/EQ/GEQ/LEQ などが大きな割合を占める場合、それはスクリプト内の if/else 文や小さなループがボトルネックになっている事を示唆しています。
従ってその場合、処理フローを見直す事で、速度を大きく改善できる余地があるかもしれません。


<a id="repetitive"></a>
## 反復実行時のオーバーヘッドを削る

ところで、上で計測した処理速度は、一つのスクリプト内で重い計算を行った場合のものです。
しかし、アプリ内での用途によっては、もっと細切れな処理を、Vnano Engine に何度も反復的にリクエストする場面も多いでしょう。

その典型例が、「 ある数式の値を、変数 x の値を小刻みに変えつつ、何度も計算したい 」といった場面です。このような処理は、例えば数式のグラフを描く際に登場しますし、数値計算でも収束アルゴリズムなどを使う場合に必要です。

そのような場合を想定したサンプルコードが、「 ExampleApp7.java 」として同梱されています：

    (in ExampleApp7.java)

    import org.vcssl.nano.VnanoEngine;
    import org.vcssl.nano.VnanoException;
    import java.util.Map;
    import java.util.HashMap;

    public class ExampleApp7 {

        // 反復実行の回数
        static final int REPETITION_COUNT = 100000000;

        // 変数「x」を提供するプラグイン
        public static class VariablePlugin {
            public double x;
        }

    	public static void main(String[] args) throws VnanoException {
            
            // Vnano Engine のインスタンスを生成
		    VnanoEngine engine = new VnanoEngine();

            // 変数「x」を提供するプラグインを生成して接続
            VariablePlugin plugin = new VariablePlugin();
            engine.connectPlugin("VariablePlugin", plugin);

            // オーバーヘッドを削るため、自動アクティベーション機能を無効化する
            Map<String, Object> optionMap = new HashMap<String, Object>();
            optionMap.put("AUTOMATIC_ACTIVATION_ENABLED", false);

    		// 反復的に処理する計算式（またはスクリプト）を用意
	    	String expression = " x * 0.5 + 3.2 ; " ;

            // 反復実行の各ターンでの結果を足し上げる変数
            double sum = 0.0;

            // 手動でエンジンをアクティベーション（実行スタンバイ状態に）する
            engine.activate();

            // 反復実行の開始時間を控える
            long beginTime = System.nanoTime();

            // 変数 x の値を小刻みに変えながら、計算式（またはスクリプト）を反復実行する
            for (int i=0; i<REPETITION_COUNT; i++) {
                plugin.x = i * 0.125;
                double valueOfExpression = (double)engine.executeScript(expression);
                sum += valueOfExpression;
            }

            // 反復実行の終了時間を控える
            long endTime = System.nanoTime();

            // エンジンのアクティベーションを解除する（何もせず待つ時の状態に戻す）
            engine.deactivate();

            // 結果を出力（所要時間や反復実行の速さなども）
            double requiredTime = ((endTime - beginTime) * 1.0E-9);
            double repetitionSpeed = REPETITION_COUNT / requiredTime;
		    System.out.println("result (sum): " + sum);
		    System.out.println("repetition couunt: " + REPETITION_COUNT);
		    System.out.println("required time: " + requiredTime + " [sec]");
		    System.out.println("repetition speed: " + repetitionSpeed + " [times/sec]");
	    }
    }

このコードは、式「 x * 0.5 + 3.2 」の値を、x を小刻みに変えつつ、反復的に1億回計算する内容になっています。所要時間は20秒ほどで、概ね数百万回/秒のスピードで計算式を反復実行できた事になります：

    result (sum): 3.1250031655706694E14
    repetition couunt: 100000000
    required time: 21.030388600000002 [sec]
    repetition speed: 4755023.880062777 [times/sec]

ふつう、計算式やスクリプトを1回実行するためにはミリ秒単位のオーバーヘッドがかかるため、1億回も反復実行すると、途方もない時間（単純な見積もりでも丸一日以上）がかかりそうに思えます。しかし、このように同じ式やスクリプトを反復実行する場合、Vnano Engine は内部でキャッシュを活用するため、上述の通りかなり速く処理できます。

ただし、このスピードは反復回数の多さ（負荷）にも依存します。1万回程度の低負荷な状況ではもう少しスピードが遅く（所要時間は短いですが）、百万回以上の高負荷な状況になると上記のようなスピードになります。この現象は恐らく近年のCPUの特性なども関連しているため、環境によって異なるかもしれません。

ところで上のコードでは、単に Vnano Engine に処理を反復リクエストするだけでなく、特別な工夫をしている点があります。それは下記の箇所の通り、「自動アクティベーション」機能を無効化して、手動でアクティベーション/解除している点です：

    (in ExampleApp7.java)

    ...

    // オーバーヘッドを削るため、自動アクティベーション機能を無効化する
    Map<String, Object> optionMap = new HashMap<String, Object>();
    optionMap.put("AUTOMATIC_ACTIVATION_ENABLED", false);

    ...

    // 手動でエンジンをアクティベーションする（実行スタンバイ状態にする）
    engine.activate();

    ...

    // 変数 x の値を小刻みに変えながら、計算式（またはスクリプト）を反復実行する
    for (int i=0; i<REPETITION_COUNT; i++) {
        plugin.x = i * 0.125;
        double valueOfExpression = (double)engine.executeScript(expression);
        sum += valueOfExpression;
    }

    ...

    // エンジンのアクティベーションを解除する（何もせず待つ時の状態に戻す）
    engine.deactivate();

ここで「アクティベーション」とは、Vnano Engine が計算式やスクリプトを実行できる状態にする事です。通常は、 executeScript(script) メソッドを呼んだ時点で自動的にアクティベーションされ、実行が完了すると自動で解除されます。しかし、高頻度で反復実行を行う場合、このアクティベーション/解除のオーバーヘッドコストが積み重なり、スピードが遅くなってしまう場合があります。

そのため、上記のように自動アクティベーションを無効化した上で、反復実行の前に一回だけ手動でアクティベーションし、反復実行が終わったら手動で解除する、という工夫が有効です。これは、特にプラグインをたくさん接続している場合に顕著に効きます（具体的にどれだけ効くかは、接続されているプラグインの初期化の重さによります）。


<a id="specifications"></a>
## 仕様書

Vnano Engine の全メソッドの一覧/詳細説明や、オプション類などについては、別途文書「 [Vnano Engine の各種仕様](SPEC_JAPANESE.md) 」をご参照ください。


## 本文中の商標などについて

- OracleとJavaは、Oracle Corporation 及びその子会社、関連会社の米国及びその他の国における登録商標です。文中の社名、商品名等は各社の商標または登録商標である場合があります。

- その他、文中に使用されている商標は、その商標を保持する各社の各国における商標または登録商標です。

