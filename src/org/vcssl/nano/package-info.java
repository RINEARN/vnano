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
 * <div style="border-style: solid; padding-left: 10px; line-height: 160%;">
 *   <p>
 *   - <span class="lang-en">Subpackages</span> <span class="lang-ja">サブパッケージ</span> -
 *   </p>
 *
 *   <dl>
 *     <dt>{@link org.vcssl.nano.compiler org.vcssl.nano.compiler}</dt>
 *        <dd>
 *        <p class="lang-en">
 *        The package performing the function of a compiler, which compiles script code written in the Vnano
 *        to a kind of intermediate code, named as "VRIL" code.
 *        VRIL ― Vector Register Intermediate Language ― is a low-level (but readable text format) language
 *        designed as a virtual assembly code for the VM (Virtual Machine) layer of Vnano Engine.
 *        </p>
 *        <p class="lang-ja">
 *        Vnanoのスクリプトコードを, "VRILコード" と呼ぶ一種の中間コードへと変換する,
 *        コンパイラの機能を担うパッケージです.
 *        VRIL（Vector Register Intermediate Language; ベクトルレジスタ中間言語）は, Vnanoエンジンの
 *        VM（仮想マシン）層の単位動作に対応するレベルの低抽象度な命令を提供する,  仮想的なアセンブリ言語です.
 *        VRILコードは, 実在のアセンブリコードと同様に, 人間にとって可読なテキスト形式のコードです.
 *        </p>
 *        </dd>
 *
 *     <dt>{@link org.vcssl.nano.vm org.vcssl.nano.vm}</dt>
 *        <dd>
 *        <p class="lang-en">
 *        The package performing the function of the VM (Virtual Machine)
 *        which executes VRIL code compiled from script code.
 *        This VM internally assemble the VRIL code to more less-overhead format,
 *        and then executes it on a kind of register machines.
 *        </p>
 *        <p class="lang-ja">
 *        スクリプトコードからコンパイルされたVRILコードを実行する,
 *        VM（仮想マシン）の機能を担うパッケージです.
 *        このVMは, テキスト形式のVRILコードを, 内部でより低オーバーヘッドな形にアセンブルした上で,
 *        一種のレジスタマシン上で実行します.
 *        </p>
 *        </dd>

 *     <dt>{@link org.vcssl.nano.interconnect org.vcssl.nano.interconnect}</dt>
 *        <dd>
 *        <p class="lang-en">
 *        The package performing the function of a component which manages and provides some information shared
 *        between multiple components explained above. We refer this component as "Interconnect" in the Vnano Engine.
 *        For example, information to resolve references of variables and functions
 *        are managed by this interconnect component.
 *        Bindings to external functions/variables are intermediated by this interconnect component,
 *        so plug-ins of external functions/variables will be connected to this component.
 *        </p>
 *        <p class="lang-ja">
 *        これまでに列挙した各コンポーネント間で共有される, いくつかの情報を管理・提供する機能を担うパッケージです.
 *        この機能を担うコンポーネントを, Vnanoエンジンでは "インターコネクト" と呼びます.
 *        インターコネクトが管理・提供する情報の具体例としては, 関数・変数の参照解決のための情報などが挙げられます.
 *        外部変数・外部関数のバインディングも, インターコネクトを介して行われます.
 *        そのため, 外部変数・外部関数のプラグインは, Vnanoエンジン内でこのコンポーネントに接続されます.
 *        </p>
 *        </dd>
 *
 *     <dt>{@link org.vcssl.nano.spec org.vcssl.nano.spec}</dt>
 *        <dd>
 *        <p class="lang-en">
 *        The package containing configures/definitions referred from various components in the script engine.
 *        </p>
 *        <p class="lang-ja">
 *        スクリプトエンジンの各部から参照される, 設定・定義類がまとめられたパッケージです.
 *        </p>
 *        </dd>
 *
 *   </dl>
 * </div>
 */
package org.vcssl.nano;
