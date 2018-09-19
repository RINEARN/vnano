/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.linker;

/*
 * 設計中。
 * 
 * アセンブリはコンパイル時の外部テーブルのインデックスをアドレスとして割り当てるため、
 * コンパイルと実行を一度に行う場合は、そのまま走らせる事が可能。
 * しかし、コンパイルと実行を分けて行う場合、
 * 外部テーブル内のシンボル <-> インデックス対応が変わってしまう可能性がある。
 * 
 * そのような場合に、このリンカの link を通せば、
 * 引数に渡されたのテーブル内インデックスと一致するようにアドレスを割りふり直す。
 * 
 * 恐らく名前解決においてそれなりのオーバーヘッドが生じるので、
 * CompiledScript#eval(String) ではデフォルトのバインドインターコネクトをそのまま用いるのみに留めてスキップすべき？
 * CompiledScript#eval(String, Bindings) ではリンクが絶対に必要。
 * 
 * @author RINEARN (Fumihiro Matsui)
 */
public class Linker {

}
