/*
 * Copyright(C) 2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

// Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/spec/PerformanceKey.html
// ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/spec/PerformanceKey.html

/**
 * <p>
 * <span class="lang-en">The class to define keys of the performance map (performance monitoring item names)</span>
 * <span class="lang-ja">パフォーマンスマップのキー（パフォーマンス計測項目名）が定義されたクラスです</span>
 * .
 * </p>
 *
 * <p>
 * &raquo; <a href="../../../../../src/org/vcssl/nano/spec/PerformanceKey.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/spec/PerformanceKey.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/spec/PerformanceKey.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class PerformanceKey {

	/**
	 * <span class="lang-en">
	 * The total number of processed instructions from when the monitoring target engine was instantiated
	 * </span>
	 * <span class="lang-ja">
	 * 計測対象エンジンのインスタンス生成時点から, 現在までに処理された命令数（累積処理命令数）を表します
	 * </span>
	 * .
	 * <span class="lang-en">The measured value of this monitoring item is "Integer" type.</span>
	 * <span class="lang-ja">この項目の計測値は "Integer" 型です.</span>
	 *
	 * <span class="en">
	 * Note that, to lighten the decreasing of the performance caused by the counting/monitoring,
	 * the gotten value of this monitoring item may be a cached old value of the counter by the caller thread,
	 * so the precision of it is not perfect.
	 * Also, please note that, when the counter value exceeds the positive maximum value of the int-type,
	 * it will not be reset to 0, and it will be the negative maximum value (minimum value on the number line)
	 * of the int-type, and will continue to be incremented from that value.
	 * For the above reason, it is recommended to get this value frequently enough
	 * (for example, --perf option of the command-line mode of the Vnano gets this value some ten times per second or more),
	 * and use differences between them, not a raw value.
	 * </span>
	 * <span class="ja">
	 * ただし, 計測による性能低下をなるべく抑えるため, 取得したカウンタ値の精度は完全ではなく,
	 * 呼び出し元スレッド等によってキャッシュされた少し前の値である可能性がある事に留意してください.
	 * また、値が int 型の上限に達してもリセットはされず,
	 * その後は負の端(int型で表現可能な数直線上の最小値)に至り,
	 * そこからまた加算され続ける事にも留意が必要です.
	 * そのため, 取得値をそのまま使うのではなく, 取得を十分な頻度
	 * （目安として, Vnano のコマンドラインモードの --perf オプションの処理では, この値の取得を毎秒数十回以上行っています）
	 * で行って, 前回からの差分を求めて使用する事などが推奨されます.
	 * </span>
	 */
	// 名前が冗長なのは、将来的に値を long 型で取得可能なキーをサポートするかもしれないためなのと、
	// 名前でそういう可能性をにおわせる事で、値の範囲が int で結構すぐ一周するという事に毎回気付けるようにするため
	public static final String PROCESSED_INSTRUCTION_COUNT_INT_VALUE = "PROCESSED_INSTRUCTION_COUNT_INT_VALUE";
}
