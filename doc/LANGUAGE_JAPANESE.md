# 言語としての Vnano

## - 目次 -

( &raquo; [English](LANGUAGE.md) )

- [Vnano とは](#what-is)
- [データ型](#data-type)
- [変数宣言文](#variable)
    - [スカラ変数の宣言](#variable-scalar)
    - [配列の宣言](#variable-array)
- [制御文](#control)
    - [if 文と else 文](#control-if-else)
    - [for 文](#control-for)
    - [while 文](#control-while)
    - [break 文](#control-break)
    - [continue 文](#control-continue)
- [式](#expression)
    - [式の構文要素](#expression-syntax)
    - [演算子](#expression-operator)
- [関数](#function)
    - [スカラを引数や戻り値とする関数](#function-scalar)
    - [配列を引数や戻り値とする関数](#function-array)
    - [仮引数と実引数](#function-params-and-args)
    - [引数の値渡し](#function-call-by-value)
    - [引数の参照渡し](#function-call-by-reference)

<hr />


<a id="what-is"></a>
## Vnano とは

Vnano (VCSSL nano) は、C言語系の文法を持つ、シンプルなプログラミング言語です。
特に、アプリケーション組み込み用途に焦点を絞った言語であるため、
一般的なプログラミング言語と比べると、用途的に必要性の低い機能は大幅に削られています。
これはスクリプトエンジンの実装規模をコンパクトに抑える事で、
機能性よりもカスタマイズ性や保守性、セキュリティ、および移植性などを優先的に高めるためです。


<a id="data-type"></a>
## データ型

Vnano は、データ型として int (=long)、float (=double)、bool、および string 型のみをサポートしています。

| 型名 | 説明 | 
| --- | --- |
| int (or long) | 64ビット精度符号付き整数型 |
| float (or double) | 64ビット精度浮動小数点数型 |
| bool | 論理型 |
| string | 文字列型 |

上記以外の基本データ型や、ポインタ、構造体、およびクラスなどはサポートされません。
一方で、上記の表にあるデータ型の配列型はサポートされており、C言語系の記法で使用できます。

ただし、Vnano（および VCSSL）における配列は、ポインタや参照型ではなく、値型として振舞う事に注意してください。
配列の代入演算（=）も、参照の代入ではなく、全要素値のコピー代入になります。
文字列についても同様で、Vnanoで文字列を扱う string 型は、参照型ではなく値型として振舞います。
つまるところ、Vnano に参照型は存在せず、全てのデータ型は値型になっています。
これにより、Vnanoのスクリプトエンジンではガベージコレクション（GC）を省略しています。

なお、配列に、要素数の異なる配列が代入される場合には、過不足なく全要素のコピーを行うために、
コピー先（代入演算子「=」の左辺）の配列のメモリー領域が自動で再確保され、
コピー元（右辺）と同じ要素数になるように調整されます。


<a id="variable"></a>
## 変数宣言文

以下のように、C言語系の表記で変数宣言文を記述できます。


<a id="variable-scalar"></a>
## スカラ変数の宣言

以下は、スカラ変数（配列ではない普通の変数）を宣言する例のコードです：

    int    i = 1;
    float  f = 2.3;
    bool   b = true;
    string s = "Hello, World !";

    print(i, f, b, s);

このコードを[コマンドラインモード](FEATURE_JAPANESE.md#command-line-mode)で実行すると（標準プラグインが必要です）、実行結果は：

    1    2.3    true    Hello, World !

一方でVnanoでは、以下のように一つの文の中で複数の変数を宣言する事はできません：

    (!!! このコードは動作しません !!!)

    int i, j;
    int n = 1, m = 2;


<a id="variable-array"></a>
## 配列宣言

配列は以下のように宣言して使用できます：

    int a[8];
    a[2] = 123;
    print(a[2]);

このコードを [コマンドラインモード](FEATURE_JAPANESE.md#command-line-mode) で実行すると、実行結果は：

    123

一方でVnanoでは、以下のような配列初期化子は使用できません：

    (!!! このコードは動作しません !!!)

    int a[8] = { 10, 20, 30, 40, 50, 60, 70, 80 };


<a id="control"></a>
## 制御文

C言語系の制御文の中で、Vnano では if / else / for / while / break / continue 文がサポートされています。


<a id="control-if-else"></a>
## if 文と else 文

以下は if 文と else 文の使用例です：

    int x = 1;
    if (x == 1) {
        print("x is 1.");
    } else {
        print("x is not 1.");
    }

実行結果は：

    x is 1.

ところでVnanoでは、if / else / for / while 文の後には必ずブロック文 {...} が続かなければいけません。
従って、以下のように if 文の後に、波括弧 { } で囲まれていない単文を記述する事はできません：

    (!!! このコードは動作しません !!!)

    int x = 1;
    if (x == 1) print("x is 1.");


<a id="control-for"></a>
## for 文

以下は for 文の使用例です：

    for (int i=1; i<=5; i++) {
        println("i=" + i);
    }


ここでも波括弧 { } は省略できない事に注意してください。実行結果は：

    i=1
    i=2
    i=3
    i=4
    i=5


<a id="control-while"></a>
## while 文

以下は while 文の使用例です：

    int a = 500;
    while (0 <= a) {
        println("a=" + a);
        a -= 123;
    }

ここでも波括弧 { } は省略できない事に注意してください。実行結果は：

    a=500
    a=377
    a=254
    a=131
    a=8


<a id="control-break"></a>
## break 文

以下は break 文の使用例です：

    for (int i=1; i<=10; i++) {
        println("i=" + i);
        if (i == 3) {
            break;
        }
    }

実行結果は：

    i=1
    i=2
    i=3

<a id="control-continue"></a>
## continue 文

以下は continue 文の使用例です：

    for (int i=1; i<=10; i++) {
        if (i % 3 == 0) {
            continue;
        }
        println("i=" + i);
    }

実行結果は：

    i=1
    i=2
    i=4
    i=5
    i=7
    i=8
    i=10


<a id="expression"></a>
## 式

<a id="expression-syntax"></a>
## 式の構文要素

式は、演算子と末端オペランドおよび括弧 ( ) で構成される一連のトークン（字句）列です。
ここでオペランドはリテラルや識別子などです。例えば：

    (x + 2) * 3;

上の式において、 + と * は演算子、x と 2 と 3 は末端オペランド、そして ( ) は括弧です。
VnanoではC言語と同様、代入の記号「=」も演算子なので、以下の内容も式になります：

    y = (x + 2) * 3;

式は、単独でも「式文」として文となり得ます。加えて、if 文の条件式など、他の種類の文の構成要素にもなります。


<a id="expression-operator"></a>
## 演算子

Vnano でサポートされている演算子は、以下の一覧の通りです：

| 演算子 | 優先度 | 構文 | 結合性の左右 | オペランドの型 | 演算結果の値の型 |
| --- | --- | --- | --- | --- | --- |
| 関数コールの ( ... , ... , ... ) | 1000 | 多項 | 左 | 関数による | 関数による |
| 配列参照の [ ... ][ ... ] ... | 1000 | 多項 | 左 | int | 配列による |
| ++ | 1000 | 後置 | 左 | int | int |
| -- | 1000 | 後置 | 左 | int | int |
| ++ | 2000 | 前置 | 右 | int | int |
| -- | 2000 | 前置 | 右 | int | int |
| + | 2000 | 前置 | 右 | int | int |
| - | 2000 | 前置 | 右 | int | int |
| ! | 2000 | 前置 | 右 | bool | bool |
| キャストの (...) | 2000 | prefix | 右 | 任意 | 記述による |
| * | 3000 | 二項 | 左 | int, float | int, float |
| / | 3000 | 二項 | 左 | int, float | int, float |
| % | 3000 | 二項 | 左 | int, float | int, float |
| + | 3100 | 二項 | 左 | int, float, string | int, float, string |
| - | 3100 | 二項 | 左 | int, float | int, float |
| < | 4000 | 二項 | 左 | int, float | bool |
| <= | 4000 | 二項 | 左 | int, float | bool |
| > | 4000 | 二項 | 左 | int, float | bool |
| >= | 4000 | 二項 | 左 | int, float | bool |
| == | 4100 | 二項 | 左 | 任意 | bool |
| != | 4100 | 二項 | 左 | 任意 | bool |
| && | 5000 | 二項 | 左 | bool | bool |
| \|\| | 5100 | 二項 | 左 | bool | bool |
| = | 6000 | 二項 | 右 | 任意 | 左辺の型 |
| *= | 6000 | 二項 | 右 | int, float | int, float |
| /= | 6000 | 二項 | 右 | int, float | int, float |
| %= | 6000 | 二項 | 右 | int, float | int, float |
| += | 6000 | 二項 | 右 | int, float, string | int, float, string |
| -= | 6000 | 二項 | 右 | int, float | int, float |


ここで算術演算子（\*, /, %, +, -）および算術複合代入演算子（*=, /=, %=, +=, -=）における値の型（演算された値のデータ型）は、以下の表の通りに決定されます：

| オペランドAの型 | オペランドBの型 | 演算結果の値の型 |
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

上の表において、右と左のどちらのオペランドをオペランドA（またはB）に選んでも構いません。


<a id="function"></a>
## 関数

Vnanoのスクリプトコード内で、C言語系の記法で関数を宣言し、呼び出す事ができます。
ただし、このスクリプトエンジンでは、ローカル変数が非常に単純な仕組みで実装されているため、
関数の再帰呼び出しには対応していません。


<a id="function-scalar"></a>
## スカラを引数や戻り値とする関数

以下は、スカラ変数（配列ではない普通の変数）を引数や戻り値とする関数のコード例です：

    int fun(int a, int b) {
        return a + b;
    }

    int v = fun(1, 2);
    print(v);

このコードを [コマンドラインモード](FEATURE_JAPANESE.md#command-line-mode) で実行すると、実行結果は：

    3


<a id="function-array"></a>
## 配列を引数や戻り値とする関数

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

    println("z[0]=" + z[0]);
    println("z[1]=" + z[1]);
    println("z[2]=" + z[2]);

The result is:

実行結果は：

    z[0]=3
    z[1]=5
    z[2]=7


なお、<a href="#data-type">データ型</a>の項目でも触れた通り、
Vnano（および VCSSL）における配列は、ポインタや参照型ではなく、値型として振舞う事に注意してください。
この事により、配列の引数/戻り値の受け渡しは、デフォルトでは参照の代入ではなく、全要素値のコピー代入によって行われます
（<a href="#function-call-by-value">値渡し</a> と 
  <a href="#function-call-by-reference">参照渡し</a> の項目も参照）。
その際、要素数の異なる配列がコピーされる場合には、過不足なく全要素のコピーを行うために、コピー先（受け取り側）
の配列のメモリー領域が自動で再確保され、コピー元と同じ要素数になるように調整されます。
従って上記のコードでは、いくつかの場所で、配列宣言時に要素数を指定するのを省略しています（ "int a[]"、 "int b[]"、 および "int z[] = fun(x, y, 3)" の箇所 ）。


<a id="function-params-and-args"></a>
## 仮引数と実引数

以下の例の「 a 」のように、関数側で宣言されている引数の事を「仮引数」と呼びます。それに対して、以下の例の「 x 」のように、呼び出し元から関数に渡している引数の事を「実引数」と呼びます。

    void fun(int a) {
        ...
    }

    ...

    fun(x);


<a id="function-call-by-value"></a>
## 引数の値渡し

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

    println("x = " + x);
    println("y[0] = " + y[0]);
    println("y[1] = " + y[1]);
    println("y[2] = " + y[2]);

実行結果は：

    x = 0
    y[0] = 0
    y[1] = 0
    y[2] = 0

上の例の通り、関数「 fun 」内で仮引数「 x 」と「 y 」の値を変更していますが、呼び出し元の実引数「 a 」と「 b 」の値は変化していない事がわかります。これは、デフォルトでの関数の引数の受け渡しが、単純な値のコピーによって、呼び出し時に一度のみに行われるからです。このような引数の渡し方を「値渡し」と呼びます。


<a id="function-call-by-reference"></a>
## 引数の参照渡し

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

    println("x = " + x);
    println("y[0] = " + y[0]);
    println("y[1] = " + y[1]);
    println("y[2] = " + y[2]);

実行結果は：

    x = 2
    y[0] = 10
    y[1] = 11
    y[2] = 12

上の例の通り、関数「 fun 」内で仮引数「 x 」と「 y 」の値を変更した結果、呼び出し元の実引数「 a 」と「 b 」も、同じ値に変化した事がわかります。これは、「 & 」を付けて宣言された仮引数のデータへのメモリ参照が、実引数のデータへのメモリ参照と共有されるためです。このような引数の渡し方を「参照渡し」と呼びます。