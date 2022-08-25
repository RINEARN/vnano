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
		assertTrue("1".matches(LiteralSyntax.INT_LITERAL_REGEX));      // A number.
		assertTrue("123".matches(LiteralSyntax.INT_LITERAL_REGEX));    // Multiple numbers.
		assertTrue("123l".matches(LiteralSyntax.INT_LITERAL_REGEX));   // Multiple numbers with long-type suffix. (In Vnano, long is an alias of int, so the result is int-type literal.)
		assertTrue("123L".matches(LiteralSyntax.INT_LITERAL_REGEX));   // See the above.
		assertTrue("0b1010".matches(LiteralSyntax.INT_LITERAL_REGEX)); // Binary literal.
		assertTrue("0o123".matches(LiteralSyntax.INT_LITERAL_REGEX));  // Octal literal.
		assertTrue("0x123".matches(LiteralSyntax.INT_LITERAL_REGEX));  // Hex literal.
		assertTrue("0xabc".matches(LiteralSyntax.INT_LITERAL_REGEX));  // Hex literal.
		assertTrue("0xABC".matches(LiteralSyntax.INT_LITERAL_REGEX));  // Hex literal.
		assertTrue("0123".matches(LiteralSyntax.INT_LITERAL_REGEX));   // Octal literal beginning only with 0 (not 0o) is valid in LiteralSyntax, though it will be declined in SemanticAnalyzer. This inconsistency is to make the error message readable.
		assertFalse("abc".matches(LiteralSyntax.INT_LITERAL_REGEX));   // Invalid decimal literal.
		assertFalse("ABC".matches(LiteralSyntax.INT_LITERAL_REGEX));   // Invalid decimal literal.
		assertFalse("0b222".matches(LiteralSyntax.INT_LITERAL_REGEX)); // Invalid binaryl literal (numbers must be in the range of 0-1).
		assertFalse("0o888".matches(LiteralSyntax.INT_LITERAL_REGEX)); // Invalid octal literal (numbers must be in the range of 0-7).
		assertFalse("0xXYZ".matches(LiteralSyntax.INT_LITERAL_REGEX)); // Invalid octal literal (numbers must be in the range of 0-7 or A-F).
		assertFalse("".matches(LiteralSyntax.INT_LITERAL_REGEX));      // Invalid literal (empty).
		assertFalse(" ".matches(LiteralSyntax.INT_LITERAL_REGEX));     // Invalid literal (white space).
	}

	@Test
	public void testFloatLiteralRegex() {

		// Numbers without floating point & float/double-type suffix.
		assertTrue("123f".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertTrue("123d".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertTrue("123F".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertTrue("123D".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertFalse("123".matches(LiteralSyntax.FLOAT_LITERAL_REGEX)); // This is parsed as an int-type literal.
		assertFalse("123l".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertFalse("123L".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));

		// Numbers with floating point & float/double-type suffix.
		assertTrue("1.23f".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertTrue("1.23d".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertTrue("1.23F".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertTrue("1.23D".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertFalse("1.23l".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertFalse("1.23L".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));

		// With exponentail part.
		assertTrue("1.23e45".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertTrue("1.23E45".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertTrue("1.23e+45".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertTrue("1.23E+45".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertTrue("1.23e-45".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertTrue("1.23E-45".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertFalse("1.23d45".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertFalse("1.23D45".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));

		// Other patterns.
		assertTrue("1.23".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertTrue("12.3".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertTrue("123.456".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertTrue(".1".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertTrue("1.".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertTrue(".123".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertTrue("123.".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertFalse("123".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertFalse(".".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertFalse("a.bc".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertFalse("1..23".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertFalse("1.2.3".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertFalse("+1.23".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertFalse("-1.23".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
		assertFalse("".matches(LiteralSyntax.FLOAT_LITERAL_REGEX))
		assertFalse(" ".matches(LiteralSyntax.FLOAT_LITERAL_REGEX));
	}

	@Test
	public void testBoolLiteralRegex() {

		// Only true or false are valid.
		assertTrue("true".matches(LiteralSyntax.BOOL_LITERAL_REGEX));
		assertTrue("false".matches(LiteralSyntax.BOOL_LITERAL_REGEX));

		// Any other literals are NG.
		assertFalse("TRUE".matches(LiteralSyntax.BOOL_LITERAL_REGEX));
		assertFalse("FALSE".matches(LiteralSyntax.BOOL_LITERAL_REGEX));
		assertFalse("abc".matches(LiteralSyntax.BOOL_LITERAL_REGEX));
		assertFalse("123".matches(LiteralSyntax.BOOL_LITERAL_REGEX));
		assertFalse("".matches(LiteralSyntax.BOOL_LITERAL_REGEX));
		assertFalse(" ".matches(LiteralSyntax.BOOL_LITERAL_REGEX));
	}
}
