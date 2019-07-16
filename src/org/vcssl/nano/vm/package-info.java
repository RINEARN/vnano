/**
 * <p>
 * <span class="lang-en">
 * The top-layer (surface-layer) of the VM of the script engine of the Vnano
 * </span>
 * <span class="lang-ja">
 * Vnanoのスクリプトエンジン内で, VM（仮想マシン）の最上層（表層）となるパッケージです
 * </span>
 * .
 * <span class="lang-en">
 * The outer-frame is {@link VirtualMachine VirtualMachine} class,
 * and others are internal components (packed in subpackages).
 * </span>
 * <span class="lang-ja">
 * 外枠となるのは {@link VirtualMachine VirtualMachine} クラスで,
 * その他（サブパッケージにまとめられています）は内部の構成クラス群です.
 * </span>
 * </p>
 *
 * <p>
 * <span class="lang-en">
 * The VM provided by this package executes a kind of intermediate code, named as "VRIL" code,
 * compiled from the script code of the Vnano by the {@link org.vcssl.nano.compiler compiler}.
 * VRIL ― Vector Register Intermediate Language ― is a low-level (but readable text format) language
 * designed as a virtual assembly code for this VM.
 * This VM internally assemble the VRIL code to more less-overhead format,
 * and then executes it on a kind of register machines.
 * </span>
 * <span class="lang-ja">
 * このパッケージが提供するVMは, Vnano のスクリプトコードから {@link org.vcssl.nano.compiler コンパイラ}
 * によってコンパイルされた, "VRILコード" と呼ぶ一種の中間コードを実行します.
 * VRIL（Vector Register Intermediate Language; ベクトルレジスタ中間言語）は,
 * このVMの単位動作に対応するレベルの低抽象度な命令を提供する,  仮想的なアセンブリ言語です.
 * VRILコードは, 実在のアセンブリコードと同様に, 人間にとって可読なテキスト形式のコードです.
 * このVMは, 内部でVRILコードをより低オーバーヘッドな形にアセンブルした上で,
 * 一種のレジスタマシン上で実行します.
 * </span>
 * </p>
 *
 * <p>
 * <span class="lang-en">
 * If you want to see internal classes composing the VM more deeply,
 * see following subpackages:
 * </span>
 *
 * <span class="lang-ja">
 * もしもVM内部を構成するクラス群を,
 * より深く見たい場合は, 以下のサブパッケージをご参照ください:
 * </span>
 * </p>
 *
 * <div class="lang-en" style="border-style: solid; padding-left: 10px; line-height: 160%;">
 *   <dl>
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
 *   </dl>
 * </div>
 *
 * <div class="lang-ja" style="border-style: solid; padding-left: 10px;">
 *   <dl>
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
 *   </dl>
 * </div>
 */
package org.vcssl.nano.vm;
