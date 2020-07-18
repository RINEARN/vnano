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
}
