/**
 * <p>
 * <span class="lang-en">
 * The package performing the function of a compiler in the script engine of the Vnano
 * </span>
 * <span class="lang-ja">
 * Vnanoのスクリプトエンジン内で, コンパイラの機能を担うパッケージです
 * </span>
 * .
 * </p>
 *
 * <p>
 * &laquo; <a href="../package-summary.html">Upper layer package</a>
 * </p>
 *
 * <p>
 * <span class="lang-en">
 * The outer-frame of the compiler provided by this package is {@link Compiler Compiler} class,
 * and other classes are internal components.
 * </span>
 * <span class="lang-ja">
 * このパッケージが提供するコンパイラの外枠となるのは {@link Compiler Compiler} クラスで,
 * 他は内部の構成クラスです.
 * </span>
 * </p>
 *
 * <p>
 * <span class="lang-en">
 * The compiler provided by this package compiles script code written in the Vnano
 * to a kind of intermediate code, named as "VRIL" code.
 * VRIL ― Vector Register Intermediate Language ― is a low-level (but readable text format) language
 * designed as a virtual assembly code of the {@link org.vcssl.nano.vm VM} (Virtual Machine) layer of Vnano Engine.
 * </span>
 *
 * <span class="lang-ja">
 * このパッケージが提供するコンパイラは,
 * Vnanoのスクリプトコードを, "VRILコード" と呼ぶ一種の中間コードへと変換します.
 * VRIL（Vector Register Intermediate Language; ベクトルレジスタ中間言語）は, Vnanoエンジンの
 * {@link org.vcssl.nano.vm VM}（仮想マシン）層の単位動作に対応するレベルの低抽象度な命令を提供する,  仮想的なアセンブリ言語です.
 * VRILコードは, 実在のアセンブリコードと同様に, 人間にとって可読なテキスト形式のコードです.
 * </span>
 * </p>
 */
package org.vcssl.nano.compiler;
