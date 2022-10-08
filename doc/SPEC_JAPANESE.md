
# Vnano Engine の各種仕様

## 目次
( &raquo; [English](SPEC.md) )

* [Vnano Engine のメソッド](#methods)
* [オプション項目](#options)
* [パーミッション項目](#permissions)

<hr />


<a id="methods"></a>
## Vnano Engine のメソッド

下記は、Vnano Engine (org.vcssl.nano.VnanoEngine クラス) の全メソッドのリストです。

| 形式 |Object executeScript(String script) |
|:---|:---|
| 説明 | 引数に指定された式またはスクリプトコードを実行します。 |
| 引数 | script: 実行対象の式またはスクリプトコード |
| <span style="white-space: nowrap;">戻り値</span> | 式、またはスクリプトコード内の最後の式文の評価値。 もしも評価値が無かった場合は null が返されます。 |
| 例外 | スクリプトの内容または実行過程にエラーが検出された場合に VnanoException がスローされます。 |


| 形式 | void terminateScript() |
|:---|:---|
| 説明 | <p>現在実行中のスクリプトの処理を、可能な限り早期に放棄して終了させます。</p> <p>より正確には、スクリプトからコンパイルされた命令列を処理している、エンジン内の VirtualMachine が、現在実行中の命令(1個)の処理を完了した時点で, 残りの命令列の実行を放棄して終了します。これは通常は一瞬で終わりますが、プラグインが提供する外部関数などを実行している最中の場合は、その外部関数の処理が完了するまでの時間を要します。</p> <p>なお、このメソッドを呼び出して実行を終了させた後に、再び（新規に）スクリプトを実行する際には、事前に resetTerminator() メソッドを呼び出す必要があります。前者の呼び出しから後者の呼び出しまでの間、実行が要求されたスクリプトは全てすぐに終了します。（この仕様により、このメソッドの呼び出しと新規実行リクエストが、別スレッドからシビアに競合したタイミングで行われた場合においても、スクリプトは確実に終了します。）</p> |
| 引数 | なし |
| <span style="white-space: nowrap;">戻り値</span> | なし |
| 例外 | TERMINATOR_ENABLED オプションが無効化されていた場合に VnanoException がスローされます。 |

| 形式 | void connectPlugin(String bindingName, Object plugin) |
|:---|:---|
| 説明 | 外部関数/変数などを提供する、各種のプラグインを接続します。 |
| 引数 | <p>bindingName:  接続されるプラグインが提供する変数/関数/名前空間の、スクリプト内での名前。なお、"___VNANO_AUTO_KEY" を指定する事で、有効な値の指定を自動で行う事もできます。</p><p>plugin: 外部関数/変数などを提供するプラグイン。型は java.lang.reflect.Field や Method、 java.lang.Class や Object、 および org.vcssl.connect.ExternalVariableConnectorInterface1、 ExternalFunctionConnectorInterface1、 ExternalNamespaceConnectorInterface1、 PermissionAuthorizerConnectorInterface1 がサポートされています。</p> |
| <span style="white-space: nowrap;">戻り値</span> | なし |
| 例外 | サポートされていないプラグインが渡された場合や、接続時の初期化処理に失敗した場合に VnanoException がスローされます。 |


| 形式 | void disconnectAllPlugins() |
|:---|:---|
| 説明 | 全てのプラグインの接続を解除します。 |
| 引数 | なし |
| <span style="white-space: nowrap;">戻り値</span> | なし |
| 例外 | プラグインの終了時処理でエラーが発生した場合に VnanoException がスローされます。 |


| 形式 | void includeLibraryScript(String libraryScriptName, String libraryScriptContent) |
|:---|:---|
| 説明 | 実行対象スクリプトの先頭に "include" される, ライブラリスクリプトを追加します。 |
| 引数 | <p>libraryScriptName: ライブラリスクリプトの名称 (エラーメッセージ等で使用されます)</p> <p>libraryScriptContent: ライブラリスクリプトのコード内容</p> |
| <span style="white-space: nowrap;">戻り値</span> | なし |
| 例外 | 指定されたライブラリに誤った内容が見つかった場合などに VnanoException がスローされます。 |


| 形式 | void unincludeAllLibraryScripts() |
|:---|:---|
| 説明 | 全てのライブラリスクリプトの include 登録を解除します。 |
| 引数 | なし |
| <span style="white-space: nowrap;">戻り値</span> | なし |
| 例外 | 現状ではスローされませんが, 将来的な互換性維持のためのために VnanoException を catch する必要があります。 |


| 形式 | void setOptionMap(Map&lt;String,Object&gt; optionMap) |
|:---|:---|
| 説明 | <p>オプションの名前と値を格納するマップ（オプションマップ）によって, オプションを設定します。</p> <p>オプションマップの型は Map&lt;String,Object&gt; で、オプション項目の名前をキーにします。項目の一覧と詳細は [オプション項目](#options) のセクションを参照してください。<p> |
| 引数 | optionMap: オプションの名前と値を格納するマップ（オプションマップ） |
| <span style="white-space: nowrap;">戻り値</span> | なし |
| 例外 | オプションの指定内容が正しくなかった場合に VnanoException がスローされます。 |


| 形式 | boolean hasOptionMap() |
|:---|:---|
| 説明 | 「getOptionMap() メソッドがマップを返せるかどうか」を判定します。 |
| 引数 | なし |
| <span style="white-space: nowrap;">戻り値</span> | getOptionMap() メソッドがマップを返せる場合に true、返せない場合は false |
| 例外 | なし |


| 形式 | Map&lt;String,Object&gt; getOptionMap() |
|:---|:---|
| 説明 | オプションの名前と値を格納するマップ（オプションマップ）を取得します。 |
| 引数 | なし |
| <span style="white-space: nowrap;">戻り値</span> | オプションの名前と値を格納するマップ（オプションマップ） |
| 例外 | なし |



| 形式 | void setPermissionMap(Map&lt;String, String&gt; permissionMap) |
|:---|:---|
| 説明 | <p>パーミッション項目の名前と値を格納するマップ（パーミッションマップ）によって, 各パーミッションの値を設定します。</p> <p>パーミッションマップの型は Map&lt;String,String&gt; で、パーミッション項目の名前をキーにします。項目の一覧と詳細は [パーミッション項目](#permissions) のセクションを参照してください。<p>  |
| 引数 | permissionMap: パーミッション項目の名前と値を格納するマップ（パーミッションマップ） |
| <span style="white-space: nowrap;">戻り値</span> | なし |
| 例外 | パーミッションの指定内容が正しくなかった場合に VnanoException がスローされます。 |


| 形式 | Map&lt;String,String&gt; getPermissionMap() |
|:---|:---|
| 説明 | パーミッション項目の名前と値を格納するマップ（パーミッションマップ）を取得します。 |
| 引数 | なし |
| <span style="white-space: nowrap;">戻り値</span> | パーミッション項目の名前と値を格納するマップ（パーミッションマップ） |
| 例外 | なし |



| 形式 | Map&lt;String, Object&gt; getPerformanceMap() |
|:---|:---|
| 説明 | <p>パフォーマンスモニタの計測項目名と値を格納するマップ（パフォーマンスマップ）を取得します。</p> <p>なお、スクリプトを実行していない時や, 実行開始後でも性能を有効に計測可能な段階にまだ達していない時など、一部の計測値が存在しないタイミングでは、それらの値は戻り値のパフォーマンスマップ内には格納されません。そのような「欠けた」または「空の」パフォーマンスマップが有り得る事には、計測値を取り出す際に留意する必要があります。</p> |
| 引数 | なし |
| <span style="white-space: nowrap;">戻り値</span> | パフォーマンスモニタの計測項目名と値を格納するマップ（パフォーマンスマップ） |
| 例外 | PERFORMANCE_MONITOR_ENABLED オプションが無効化されていた場合に VnanoException がスローされます。 |




<a id="options"></a>
## オプション項目

下記は、Vnano Engine で設定可能なオプション項目のリストです。なお、項目名の型は全て「 String 」です。

| 項目名 | 値の型 | デフォルト値 | 説明 |
|:--|:--|:--|:--|
| LOCALE | java.util.Locale | 環境依存 | エラーメッセージの言語を決めるロケールです。 |
| MAIN_SCRIPT_NAME  | String | "main script" | 実行対象スクリプトの名前です。 |
| MAIN_SCRIPT_DIRECTORY  | String | "." | 実行対象スクリプトがあるディレクトリ（フォルダ）のパスです。 |
| EVAL_INT_LITERAL_AS_FLOAT  | Boolean | FALSE | 実行/評価対象の式やスクリプト（ライブラリ以外）内に出現する整数リテラルを、float型と見なして扱うオプションです。 |
| EVAL_ONLY_EXPRESSION  | Boolean | FALSE | 実行対象のスクリプト（ライブラリ以外）内で使用可能な文を、式文のみに制限するオプションです。 |
| EVAL_ONLY_FLOAT  | Boolean | FALSE | 実行対象のスクリプト（ライブラリ以外）内で使用可能な演算子やオペランドの型を、float 型のみに制限するオプションです。 |
| ACCELERATOR_ENABLED | Boolean | TRUE | VM内の高速な仮想プロセッサ実装である 「 Accelerator（org.vcssl.nano.vm.accelerator.Accelerator）」 の有効/無効を切り替えるためのオプションです。 |
| ACCELERATOR_OPTIMIZATION_LEVEL | Integer | 3 | <p>Accelerator 内での、処理の最適化レベルを指定するためのオプションです。指定する値は下記の通りです：</p> <p>0: 最適化を行いません。</p> <p>1: データアクセスのオーバーヘッドを削減する最適化（演算値のキャッシュなど）を行います。</p> <p>2: 上記に加えて、コードの構造が概ね保たれるレベルでの、局所的な命令列の最適化（複数の命令を並べ替えて1個に結合するなど）を行います。</p> <p>3: 上記に加えて、コードの大きな構造変更を伴うレベルの最適化（インライン展開など）を行います。</p> |
| TERMINATOR_ENABLED | Boolean | FALSE | <p>実行中のスクリプトを終了させる機能の、有効/無効を切り替えるためのオプションです。</p> <p>このオプションを有効化すると、スクリプトを「 実行途中でエンジン操作によって 」終了させる事が可能になる代わりに、処理速度が若干低下してしまう可能性があります。多くの場合は、恐らくほぼ気付かない程度の速度差しか生じませんが、高度に最適化された数値演算系スクリプトなどでは 10% 程度、場合によってはそれ以上の速度低下が見込まれます。なお、このオプションの有効/無効に関わらず、スクリプトの処理が全て終わった際や、スクリプト内でエラーが発生した際、またはスクリプト内で exit() 関数が呼ばれた際などには、スクリプト実行は(必然的に)終了する事にご注意ください。</p> |
| PERFORMANCE_MONITOR_ENABLED | Boolean | FALSE | <p>実測性能計測に用いるパフォーマンスモニタの有効/無効を切り替えるためのオプションです。</p> <p>このオプションを有効化すると、エンジンの実測性能値を取得可能になる代わりに、処理速度が若干低下してしまう可能性があります。多くの場合は, 恐らくそれほど大きな速度差は生じませんが、高度に最適化された数値演算系スクリプトなどでは 25% 程度、場合によってはそれ以上の速度低下が見込まれます。</p> |
| DUMPER_ENABLED | Boolean | FALSE | コンパイラやVM内などでの状態や中間表現をダンプするためのオプションです。 |
| DUMPER_TARGET | String | "ALL" | <p>ダンプ対象を指定します。値は下記の通りです：</p> <p>"ALL": 全ての内容をダンプします。</p> <p>"INPUTTED_CODE": 入力されたままの形のスクリプトコードをダンプします。</p> <p>"PREPROCESSED_CODE": コメント削除などの前処理が行われたスクリプトコードをダンプします。</p> <p>"TOKEN": 字句解析結果のトークン配列をダンプします。LexicalAnalyzer の出力値検証用です。</p> <p>"PARSED_AST": 構文解析結果の AST（抽象構文木）をダンプします。Parser の出力値検証用です。</p> <p>"ANALYZED_AST": 意味解析結果の AST（抽象構文木）をダンプします。SemanticAnalyzer の出力値検証用です。</p> <p>"ASSEMBLY_CODE": コンパイル結果の、VM用中間コードである「VRILコード」をダンプします。CodeGenerator の出力検証用です。</p> <p>"OBJECT_CODE": アセンブル結果の「VMオブジェクトコード（未最適化）」をダンプします。Assembler の出力検証用です。</p> <p>"ACCELERATOR_CODE": Accelerator での実行用の命令列（最適化済み）をダンプします。AcceleratorOptimizationUnit の出力検証用です。</p> <p>"ACCELERATOR_STATE": Accelerator の内部状態をダンプします。各演算ユニットへのディスパッチ状況などの検証用です。</p> |
| DUMPER_STREAM | java.io.PrintStream | System.out | ダンプの出力に用いるストリームを指定します。 |
| RUNNING_ENABLED  | Boolean | TRUE | <p>スクリプトを実行するかしないかを指定するためのオプションです。</p> <p>このオプションは、コンパイルした結果をデバッグ用にダンプしつつ、実行はしたくない場合などに有用かもしれません。</p> |
| UI_MODE | String | "GUI" | <p>値の入出力などに用いるユーザーインターフェースの形式を指定します。</p> <p>値は "GUI" か "CUI" を指定します。デフォルトは "GUI" ですが、コマンドラインモードでは "CUI" に自動設定されます。</p> <p>このオプションは、入出力系のプラグインが接続されている場合に、それらによって参照されます。</p> |
| ENVIRONMENT_EOL | String | 環境依存 | <p>環境における、デフォルトの改行コードを指定します。</p> <p>このオプションは、環境依存の値を提供するプラグインが接続されている場合に、それらによって参照されます。</p> |
| FILE_IO_EOL | String | 環境依存 | <p>ファイルの入出力に用いる、デフォルトの改行コードを指定します。</p> <p>このオプションは、入出力系のプラグインが接続されている場合に、それらによって参照されます。</p> |
| TERMINAL_IO_EOL | String | 環境依存 | <p>端末との入出力に用いる、デフォルトの改行コードを指定します。</p> <p>このオプションは、入出力系のプラグインが接続されている場合に、それらによって参照されます。</p> |
| FILE_IO_ENCODING | String | "UTF-8" | <p>スクリプト内でのファイルの読み書きに用いる、デフォルトの文字コード名を指定します。</p> <p>このオプションは、入出力系のプラグインが接続されている場合に、それらによって参照されます。</p> |
| STDIN_STREAM | Java.io.InputStream | System.in | <p>TERMINAL_IO_UI が "CUI" に設定されている際に、標準入力に用いるストリームを指定します。</p> <p>このオプションは、入出力系のプラグインが接続されている場合に、それらによって参照されます。</p> |
| STDOUT_STREAM | Java.io.PrintStream | System.out | <p>TERMINAL_IO_UI が "CUI" に設定されている際に、標準出力に用いるストリームを指定します。</p> <p>このオプションは、入出力系のプラグインが接続されている場合に、それらによって参照されます。</p> |
| STDERR_STREAM | Java.io.PrintStream | System.err | <p>TERMINAL_IO_UI が "CUI" に設定されている際に、標準エラー出力に用いるストリームを指定します。</p> <p>このオプションは、入出力系のプラグインが接続されている場合に、それらによって参照されます。</p> |



<a id="permissions"></a>
## パーミッション項目

下記は、設定可能なパーミッション項目のリストです。
項目名と値の型は全て「 String 」で、値は "ALLOW"、"DENY"、"ASK" から選んで指定してください。
なお、"ASK" を指定すると、「 ユーザーに許可/拒否を訪ねて決定する 」という挙動になります。デフォルトでは、全ての項目の値は "DENY" に設定されています。

| 項目名 | 説明 |
|:--|:--|
| PROGRAM_EXIT | 現在実行中のプログラム（スクリプト）を終了させる事に対するパーミッションです。 |
| PROGRAM_RESET | 現在実行中のプログラム（スクリプト）をリセット/再実行する事に対するパーミッションです。 |
| PROGRAM_CHANGE | 現在実行中のプログラム（スクリプト）を変更する事に対するパーミッションです。 |
| SYSTEM_PROCESS | オペレーティングシステム等を介して、コマンドや別のプログラム等を実行する事に対するパーミッションです。 |
| DIRECTORY_CREATE | ディレクトリ（フォルダ）の新規作成に対するパーミッションです。 |
| DIRECTORY_DELETE | ディレクトリ（フォルダ）の削除に対するパーミッションです。 |
| DIRECTORY_LIST | ディレクトリ（フォルダ）内のファイル一覧取得に対するパーミッションです。 |
| FILE_CREATE | ファイルの新規作成に対するパーミッションです。 |
| FILE_DELETE | ファイルの削除に対するパーミッションです。 |
| FILE_WRITE | ファイルへの書き込みに対するパーミッションです。 |
| FILE_READ | ファイルからの読み込みに対するパーミッションです。 |
| FILE_OVERWRITE | ファイルへの上書きに対するパーミッションです。 |
| FILE_INFORMATION_CHANGE | ファイルの情報変更（更新日時など）に対するパーミッションです。 |
| ALL | 「全てのパーミッション項目」を表すメタ項目名です。 |
| NONE | 「パーミッション項目が無い事」を表すメタ項目名です。 |
| DEFAULT  | <p>「全パーミッション項目における未設定時のデフォルト値」を保持するメタ項目名です。</p> <p>明示的に設定されていないパーミッション項目には, デフォルト値（例えば "DENY" など）が自動的に設定されます。このデフォルト値は、このメタパーミッション項目「 DEFAULT 」の値を変える事で変更できます。例えばこの「 DEFAULT 」項目に値 "ASK" を設定すると、スクリプトエンジンは、未設定のパーミッションが要求された際に、ユーザーに許可/拒否を尋ねるようになります。</p> |

