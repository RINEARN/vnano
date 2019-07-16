/**
 * <p>
 * <span class="lang-en">
 * The top-layer (surface-layer) package of the language processing system of the Vnano
 * </span>
 * <span class="lang-ja">
 * Vnano の言語処理系の最上層（表層）となるパッケージです
 * </span>
 * .
 * </p>
 *
 * <p>
 * <span class="lang-en">
 * In this layer, outer-frame classes of script engine of the Vnano
 * {@link org.vcssl.nano.VnanoEngine VnanoEngine} /
 * {@link org.vcssl.nano.VnanoScriptEngine VnanoScriptEngine},
 * are provided.
 * In the normal usage, it is sufficient to grasp only classes in this layer
 * for using the script engine of the Vnano from the host application.
 * </span>
 *
 * <span class="lang-ja">
 * この階層では, Vnano のスクリプトエンジンの外枠となるクラスである
 * {@link org.vcssl.nano.VnanoEngine VnanoEngine} /
 * {@link org.vcssl.nano.VnanoScriptEngine VnanoScriptEngine}
 * などが提供されます.
 * 通常, ホストアプリケーションからVnanoのスクリプトエンジンを使用するには,
 * この階層のクラス群のみを把握すれば十分です.
 * </span>
 * </p>
 *
 * <p>
 * <span class="lang-en">
 * If you want to see classes composing the script engine more deeply,
 * see following internal-layer packages:
 * </span>
 *
 * <span class="lang-ja">
 * なお, もしもスクリプトエンジン内部を構成するクラス群を,
 * より深く見たい場合は, 以下の各パッケージをご参照ください:
 * </span>
 * </p>
 *
 * <div class="lang-en" style="border-style: solid; padding-left: 10px; line-height: 160%;">
 *   <dl>
 *     <dt>{@link org.vcssl.nano.compiler org.vcssl.nano.compiler} pakcage</dt>
 *        <dd>
 *        The package performing the function of a compiler, which compiles script code written in the Vnano
 *        to a kind of intermediate code, named as "VRIL" code.
 *        VRIL ― Vector Register Intermediate Language ― is a low-level (but readable text format) language
 *        designed as a virtual assembly code for the VM (Virtual Machine) layer of Vnano Engine.
 *        </dd>
 *     <dt>{@link org.vcssl.nano.vm org.vcssl.nano.vm} pakage</dt>
 *        <dd>
 *        <!-- The top-layer (surface-layer) of -->
 *        The package performing the function of the VM (Virtual Machine)
 *        which executes VRIL code compiled from script code.
 *        This VM internally assemble the VRIL code to more less-overhead format,
 *        and then executes it on a kind of register machines.
 *        </dd>
 *     <!--
 *     <dt>{@link org.vcssl.nano.vm.assembler org.vcssl.nano.vm.assembler} pakcage</dt>
 *        <dd>
 *        The package performing the function of an assembler in the VM, to translate VRIL code (text format)
 *        into more low level instruction objects (referred as "VRIL Instructions" in the above figure)
 *        which are directly executable by the VM layer.
 *        </dd>
 *     <dt>{@link org.vcssl.nano.vm.memory org.vcssl.nano.vm.memory} pakcage</dt>
 *        <dd>
 *        The package performing the function of a virtual memory in the VM,
 *        to store data for reading and writing from the virtual processor.
 *        Register for storing temporary data are also provided by this virtual memory.
 *        Most instructions of the virtual processor of Vnano are SIMD, so this virtual memory stores data
 *        by units of vector (array). One virtual data address corresponds one vector.
 *        </dd>
 *     <dt>{@link org.vcssl.nano.vm.processor org.vcssl.nano.vm.processor} pakcage</dt>
 *        <dd>
 *        The package performing the function of a virtual processor (CPU) in the VM,
 *        which executes instruction objects assembled from VRIL code.
 *        The architecture of this virtual processor is a SIMD-based Register Machine (Vector Register Machine).
 *        The implementation code of this virtual processor is simple and may be easy to customize,
 *        however, its processing speed is not so high.
 *        </dd>
 *     <dt>{@link org.vcssl.nano.vm.accelerator org.vcssl.nano.vm.accelerator} pakcage</dt>
 *        <dd>
 *        The package providing a high-speed (but complicated) implementation
 *        of the virtual processor in the VM referred above.
 *        Whether you use this component or don't is optional,
 *        and Vnano engine can run even under the condition of that this component is completely disabled.
 *        </dd>
 *     -->
 *     <dt>{@link org.vcssl.nano.interconnect org.vcssl.nano.interconnect} pakcage</dt>
 *        <dd>
 *        The package performing the function of a component which manages and provides some information shared
 *        between multiple components explained above. We refer this component as "Interconnect" in the Vnano Engine.
 *        For example, information to resolve references of variables and functions
 *        are managed by this interconnect component.
 *        Bindings to external functions/variables are intermediated by this interconnect component,
 *        so plug-ins of external functions/variables will be connected to this component.
 *        </dd>
 *     <dt>{@link org.vcssl.nano.spec org.vcssl.nano.spec} pakcage</dt>
 *        <dd>
 *        The package containing configures/definitions referred from various components in the script engine.
 *        </dd>
 *   </dl>
 * </div>
 *
 * <div class="lang-ja" style="border-style: solid; padding-left: 10px;">
 *   <dl>
 *     <dt>{@link org.vcssl.nano.compiler org.vcssl.nano.compiler} パッケージ</dt>
 *        <dd>
 *        Vnanoのスクリプトコードを, "VRILコード" と呼ぶ一種の中間コードへと変換する,
 *        コンパイラの機能を担うパッケージです.
 *        VRIL（Vector Register Intermediate Language; ベクトルレジスタ中間言語）は, Vnanoエンジンの
 *        VM（仮想マシン）層の単位動作に対応するレベルの低抽象度な命令を提供する,  仮想的なアセンブリ言語です.
 *        VRILコードは, 実在のアセンブリコードと同様に, 人間にとって可読なテキスト形式のコードです.
 *        </dd>
 *     <dt>{@link org.vcssl.nano.vm org.vcssl.nano.vm} パッケージ</dt>
 *        <dd>
 *        スクリプトコードからコンパイルされたVRILコードを実行する,
 *        VM（仮想マシン）の機能を担うパッケージです.
 *        <!-- VM（仮想マシン）の最上層（表層）となるパッケージです.-->
 *        このVMは, テキスト形式のVRILコードを, 内部でより低オーバーヘッドな形にアセンブルした上で,
 *        一種のレジスタマシン上で実行します.
 *        </dd>
 *     <!--
 *     <dt>{@link org.vcssl.nano.vm.assembler org.vcssl.nano.vm.assembler} パッケージ</dt>
 *        <dd>
 *        VM内で, テキスト形式のVRILコードを, VnanoのVM層で直接的に実行可能な命令オブジェクト列を含む
 *        "VMオブジェクトコード" へと変換する, アセンブラとしての機能を担うパッケージです.
 *        この命令オブジェクト列は、上図の中において "VRIL Instructions" として記述されています。
 *        </dd>
 *     <dt>{@link org.vcssl.nano.vm.memory org.vcssl.nano.vm.memory} パッケージ</dt>
 *        <dd>
 *        VM内で, 仮想プロセッサから読み書きされるデータを, アドレスに紐づけて保持する,
 *        仮想的なメモリとしての機能を担うパッケージです.
 *        仮想プロセッサが一時的なデータの保持に使用するレジスタも, この仮想メモリが提供します.
 *        先述の通り, Vnanoエンジンの仮想プロセッサはベクトルレジスタマシンのアーキテクチャを採用しているため,
 *        この仮想メモリはデータをベクトル（配列）単位で保持します.
 *        即ち、一つのデータアドレスに対して, 一つの配列データが紐づけられます。
 *        </dd>
 *     <dt>{@link org.vcssl.nano.vm.processor org.vcssl.nano.vm.processor} パッケージ</dt>
 *        <dd>
 *        VM内で, アセンブラによってVRILコードから変換された命令オブジェクト列を逐次的に実行する,
 *        仮想的なプロセッサ（CPU）としての機能を担うパッケージです.
 *        この仮想プロセッサは, SIMD演算を基本とする, ベクトルレジスタマシンのアーキテクチャを採用しています.
 *        このパッケージが提供する仮想プロセッサの実装は, 単純で改造が比較的容易ですが,
 *        その反面, 処理速度はあまり速くありません.
 *        </dd>
 *     <dt>{@link org.vcssl.nano.vm.accelerator org.vcssl.nano.vm.accelerator} パッケージ</dt>
 *        <dd>
 *        VM内で, 上記の仮想プロセッサのより高速な実装を提供するパッケージです.
 *        半面, 実装コードの内容もより複雑になっています.
 *        このコンポーネントを使用するかどうかはオプションで選択できます.
 *        Vnanoエンジンは, このコンポーネントの動作を完全に無効化しても, 機能上は欠損なく成立するようにできています.
 *        </dd>
 *     -->
 *     <dt>{@link org.vcssl.nano.interconnect org.vcssl.nano.interconnect} パッケージ</dt>
 *        <dd>
 *        これまでに列挙した各コンポーネント間で共有される, いくつかの情報を管理・提供する機能を担うパッケージです.
 *        この機能を担うコンポーネントを, Vnanoエンジンでは "インターコネクト" と呼びます.
 *        インターコネクトが管理・提供する情報の具体例としては, 関数・変数の参照解決のための情報などが挙げられます.
 *        外部変数・外部関数のバインディングも, インターコネクトを介して行われます.
 *        そのため, 外部変数・外部関数のプラグインは, Vnanoエンジン内でこのコンポーネントに接続されます.
 *        </dd>
 *     <dt>{@link org.vcssl.nano.spec org.vcssl.nano.spec} パッケージ</dt>
 *        <dd>
 *        スクリプトエンジンの各部から参照される, 設定・定義類がまとめられたパッケージです.
 *        </dd>
 *   </dl>
 * </div>
 */
package org.vcssl.nano;
