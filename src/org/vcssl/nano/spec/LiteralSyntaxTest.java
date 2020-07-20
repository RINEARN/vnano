package org.vcssl.nano.spec;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LiteralSyntaxTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testIntLiteralRegex() {
		LiteralSyntax literalSyntax = new LiteralSyntax(new DataTypeName());
		assertTrue("1".matches(literalSyntax.intLiteralRegex));      // 1個以上の数字列のみ（1個）
		assertTrue("123".matches(literalSyntax.intLiteralRegex));    // 1個以上の数字列のみ（複数個）
		assertTrue("123l".matches(literalSyntax.intLiteralRegex));   // long型サフィックス付き（Vnano の long は int のエイリアスなので int リテラル）
		assertTrue("123L".matches(literalSyntax.intLiteralRegex));   // long型サフィックス付き（Vnano の long は int のエイリアスなので int リテラル）
		assertTrue("0b1010".matches(literalSyntax.intLiteralRegex)); // 2進リテラルのプレフィックスが付いている場合
		assertTrue("0o123".matches(literalSyntax.intLiteralRegex));  // 8進リテラルのプレフィックスが付いている場合
		assertTrue("0x123".matches(literalSyntax.intLiteralRegex));  // 16進リテラルのプレフィックスが付いている場合
		assertTrue("0xabc".matches(literalSyntax.intLiteralRegex));  // 16進リテラルの場合は a ~ f までのアルファベットは使用可能
		assertTrue("0xABC".matches(literalSyntax.intLiteralRegex));  // 16進リテラルの場合は A ~ F までのアルファベットは使用可能
		assertTrue("0123".matches(literalSyntax.intLiteralRegex));   // 0のみで始まる8進リテラルも正規表現上は通す（言語的には非サポートなので SemanticAnalyzer で却下する。ここで除外するとエラーメッセージが分かりづらくなるため。）
		assertFalse("abc".matches(literalSyntax.intLiteralRegex));   // 数字じゃない場合はNG（16進リテラルは除く）
		assertFalse("ABC".matches(literalSyntax.intLiteralRegex));   // 数字じゃない場合はNG（16進リテラルは除く）
		assertFalse("0b222".matches(literalSyntax.intLiteralRegex)); // 2進リテラルの各桁が 0 ～ 1 の範囲外の場合はNG
		assertFalse("0o888".matches(literalSyntax.intLiteralRegex)); // 8進リテラルの各桁が 0 ～ 7 の範囲外の場合はNG
		assertFalse("0xXYZ".matches(literalSyntax.intLiteralRegex)); // 16進リテラルの各桁が 0 ～ F の範囲外の場合はNG
		assertFalse("".matches(literalSyntax.intLiteralRegex));      // 空文字の場合は場合はNG
		assertFalse(" ".matches(literalSyntax.intLiteralRegex));     // 空白の場合も場合はNG
	}

	@Test
	public void testFloatLiteralRegex() {
		LiteralSyntax literalSyntax = new LiteralSyntax(new DataTypeName());

		// 数字列 + float/double 型サフィックスの場合
		assertTrue("123f".matches(literalSyntax.floatLiteralRegex));
		assertTrue("123d".matches(literalSyntax.floatLiteralRegex));
		assertTrue("123F".matches(literalSyntax.floatLiteralRegex));
		assertTrue("123D".matches(literalSyntax.floatLiteralRegex));
		assertFalse("123".matches(literalSyntax.floatLiteralRegex)); // サフィックスが無いと整数になるのでNG
		assertFalse("123l".matches(literalSyntax.floatLiteralRegex)); // f/d/F/D以外のサフィックスなのでNG
		assertFalse("123L".matches(literalSyntax.floatLiteralRegex)); // f/d/F/D以外のサフィックスなのでNG

		// それ以外の一般的な場合、先にサフィックスの認識を検査（仮数部のパターンとは独立しているため）
		assertTrue("1.23f".matches(literalSyntax.floatLiteralRegex));
		assertTrue("1.23d".matches(literalSyntax.floatLiteralRegex));
		assertTrue("1.23F".matches(literalSyntax.floatLiteralRegex));
		assertTrue("1.23D".matches(literalSyntax.floatLiteralRegex));
		assertFalse("1.23l".matches(literalSyntax.floatLiteralRegex)); // f/d/F/D以外のサフィックスなのでNG
		assertFalse("1.23L".matches(literalSyntax.floatLiteralRegex)); // f/d/F/D以外のサフィックスなのでNG

		// 続いて指数部の認識を検査（仮数部のパターンとは独立しているため）
		assertTrue("1.23e45".matches(literalSyntax.floatLiteralRegex));
		assertTrue("1.23E45".matches(literalSyntax.floatLiteralRegex));
		assertTrue("1.23e+45".matches(literalSyntax.floatLiteralRegex));
		assertTrue("1.23E+45".matches(literalSyntax.floatLiteralRegex));
		assertTrue("1.23e-45".matches(literalSyntax.floatLiteralRegex));
		assertTrue("1.23E-45".matches(literalSyntax.floatLiteralRegex));
		assertFalse("1.23d45".matches(literalSyntax.floatLiteralRegex)); // e/E 以外の指数部プレフィックスはNG
		assertFalse("1.23D45".matches(literalSyntax.floatLiteralRegex)); // e/E 以外の指数部プレフィックスはNG

		// 色々なパターンの仮数部を検査
		assertTrue("1.23".matches(literalSyntax.floatLiteralRegex));    //「 1個以上の数字列 . 1個以上の数値列 」パターン（左が1個だけ）
		assertTrue("12.3".matches(literalSyntax.floatLiteralRegex));    //「 1個以上の数字列 . 1個以上の数値列 」パターン（右が1個だけ）
		assertTrue("123.456".matches(literalSyntax.floatLiteralRegex)); //「 1個以上の数字列 . 1個以上の数値列 」パターン（左右とも複数個）
		assertTrue(".1".matches(literalSyntax.floatLiteralRegex));      //「 0個以上の数字列 . 1個以上の数字列 」パターン（左が0個、右が1個）
		assertTrue("1.".matches(literalSyntax.floatLiteralRegex));      //「 0個以上の数字列 . 1個以上の数字列 」パターン（右が0個、左が1個）
		assertTrue(".123".matches(literalSyntax.floatLiteralRegex));    //「 0個以上の数字列 . 1個以上の数字列 」パターン（左が0個、右が複数個）
		assertTrue("123.".matches(literalSyntax.floatLiteralRegex));    //「 1個以上の数字列 . 0個以上の数字列 」パターン（右が0個、左が複数個）
		assertFalse("123".matches(literalSyntax.floatLiteralRegex));    // 小数点が無いと整数になるのでNG（上でも検査したけど流れ的に）
		assertFalse(".".matches(literalSyntax.floatLiteralRegex));      // 小数点があっても数字列が無い場合は NG
		assertFalse("a.bc".matches(literalSyntax.floatLiteralRegex));   // 小数点があっても数字の箇所が数字じゃない場合はNG
		assertFalse("1..23".matches(literalSyntax.floatLiteralRegex));  // 小数点が複数連続する場合はNG
		assertFalse("1.2.3".matches(literalSyntax.floatLiteralRegex));  // 小数点が（非連続でも）複数ある場合はNG
		assertFalse("+1.23".matches(literalSyntax.floatLiteralRegex));  // 符号は単項プラス/マイナス演算子と認識すべきなので、リテラルに含めて解釈しようとした場合はNG
		assertFalse("-1.23".matches(literalSyntax.floatLiteralRegex));  // 符号は単項プラス/マイナス演算子と認識すべきなので、リテラルに含めて解釈しようとした場合はNG
		assertFalse("".matches(literalSyntax.floatLiteralRegex));       // 空文字の場合はNG
		assertFalse(" ".matches(literalSyntax.floatLiteralRegex));      // 空白の場合もNG
	}

	@Test
	public void testBoolLiteralRegex() {
		LiteralSyntax literalSyntax = new LiteralSyntax(new DataTypeName());

		// true と false のみが OK
		assertTrue("true".matches(literalSyntax.boolLiteralRegex));
		assertTrue("false".matches(literalSyntax.boolLiteralRegex));

		// 他は NG
		assertFalse("TRUE".matches(literalSyntax.boolLiteralRegex));
		assertFalse("FALSE".matches(literalSyntax.boolLiteralRegex));
		assertFalse("abc".matches(literalSyntax.boolLiteralRegex));
		assertFalse("123".matches(literalSyntax.boolLiteralRegex));
		assertFalse("".matches(literalSyntax.boolLiteralRegex));
		assertFalse(" ".matches(literalSyntax.boolLiteralRegex));
	}
}
