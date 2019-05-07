/**
 * <p>
 * Vnano の処理系の最上階層である、
 * {@link org.vcssl.nano.VnanoScriptEngine VnanoScriptEngine} およびその周辺クラスなどを提供するパッケージです。
 * </p>
 *
 * <p>
 * Vnano処理系の内部を構成するパッケージとしては、下記のものがあります:
 * </p>
 *
 * <div style="border-style: solid; padding-left: 10px;">
 *   <dl>
 *     <dt>{@link org.vcssl.nano.compiler org.vcssl.nano.compiler}</dt>
 *        <dd>スクリプトから仮想アセンブリコードへの変換を行う、コンパイラを構成するパッケージ</dd>
 *     <dt>{@link org.vcssl.nano.vm.assembler org.vcssl.nano.vm.assembler}</dt>
 *        <dd>仮想アセンブリコードから実行用中間コードへの変換を行う、アセンブラを構成するパッケージ</dd>
 *     <dt>{@link org.vcssl.nano.vm.memory org.vcssl.nano.vm.memory}</dt>
 *        <dd>データの保持や受け渡し、および変換などの機能を提供する、仮想メモリーを構成するパッケージ</dd>
 *     <dt>{@link org.vcssl.nano.vm.processor org.vcssl.nano.vm.processor}</dt>
 *        <dd>中間コードを実行する、仮想プロセッサー（プロセス仮想マシンとしてのVM）を構成するパッケージ</dd>
 *     <dt>{@link org.vcssl.nano.interconnect org.vcssl.nano.interconnect}</dt>
 *        <dd>外部変数・外部関数プラグインの接続や、関数・変数の参照解決および仲介など、接続レイヤーを構成するパッケージ</dd>
 *     <dt>{@link org.vcssl.nano.lang org.vcssl.nano.lang}</dt>
 *        <dd>変数・関数やデータ型など、 言語と紐づいたものを表現するクラスなどを提供するパッケージ</dd>
 *     <dt>{@link org.vcssl.nano.spec org.vcssl.nano.spec}</dt>
 *        <dd>処理系および処理対象言語に関する設定・定義類がまとめられたパッケージ</dd>
 *   </dl>
 * </div>
 */
package org.vcssl.nano;
