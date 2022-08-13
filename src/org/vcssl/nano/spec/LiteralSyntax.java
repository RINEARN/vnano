/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import java.util.List;
import java.util.ArrayList;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.VnanoException;


/**
 * The class to define syntax of literals, and performs validations/detections of literals.
 */
public class LiteralSyntax {

	/** The value of the bool type literal: "true". */
	public static final String TRUE = "true";


	/** The value of the bool type literal: "false". */
	public static final String FALSE = "false";


	/** The prefix of hexadecimal int literals. */
	public static final String INT_LITERAL_HEX_PREFIX = "0x";


	/** The prefix of octal int literals. */
	public static final String INT_LITERAL_OCT_PREFIX = "0o";


	/** The prefix of binary int literals. */
	public static final String INT_LITERAL_BIN_PREFIX = "0b";


	/** The regular expression of the prefix of an exponent part of "float" type literals. */
	public static final String FLOAT_LITERAL_EXPONENT_PREFIX = "e|E";


	/**
	 * The regular expression of "int" type literals.
	 * Note that, literals don't contain signs (+/-) because they will be parsed as sign operators, not parts of literals.
	 */
	protected static final String INT_LITERAL_REGEX =
			// The beginning of the regex.
			"^"
			+
			// Hex literals: Begins with "0x", and one or multiple 0~9/A~F continue after it.
			"(0x[0-9a-zA-F]+)"
			+
			// Or
			"|"
			+
			// Octal literals: Begins with "0o", and one or multiple 0~7 continue after it.
			"(0o[0-7]+)"
			+
			// Or
			"|"
			+
			// Binary literals: Begins with "0b", and one or multiple 0~1 continue after it.
			"(0b[0-1]+)"
			+
			// または
			"|"
			+
			// Decimal literals: One or multiple 0~9, without any prefix.
			"([0-9]+)"
			+
			// A type-suffix (l or L) may be specified at the end of a literal.
			"(l|L)?"
			+
			// The end of the regex.
			"$";


	/**
	 * The regular expression of "float" type literals.
	 * Note that, literals don't contain signs (+/-) because they will be parsed as sign operators, not parts of literals.
	 */
	protected static final String FLOAT_LITERAL_REGEX =
			// The beginning of the regex.
			"^"
			+
			// Simple cases: begins with one or multiple 0~9, and ends with type-suffix (d/f/D/F).
			// Note that, if it has no suffix in this case, it should be be parsed as an "int" literal.
			"([0-9]+(d|f|D|F))"
			+
			// Or
			"|"
			+
			// General cases:
			"("
				+
				// Begins with one or multiple 0~9, and the next is a floating point (.), and one or multiple 0~9 continues after it again.
				// Note that, in Vnano, can not omit numbers before/after the floating point, though some languages allow it to be omitted.
				"(([0-9]+\\.[0-9]*)|([0-9]*\\.[0-9]+))"
				+
				// An exponent part may continues after the above.
				"((e|E)(\\+|-)?[0-9]+)?"
				+
				// A type-suffix (d/f/D/F) part may continues at the end of a literal.
				"(d|f|D|F)?"
				+
			")"
			+
			// The end of the regex.
			"$";


	/** The regular expression of "bool" type literals. */
	protected static final String BOOL_LITERAL_REGEX = "^" + TRUE + "|" + FALSE + "$";


	/** The prefix character of escape sequences in string literals. */
	private static final char STEING_LITERAL_ESCAPE = '\\';


	/** The beginning/end character of string-literals. */
	public static final char STRING_LITERAL_QUOT = '"';


	/** The beginning/end character of char-literals (unsupported). */
	public static final char CHAR_LITERAL_QUOT = '\'';


	/**
	 * Checks whether the specified token can be interpreted as the literal or not.
	 * 
	 * @param token The token to be checked.
	 * @return The check result ("true" if it can be interpreted as the literal).
	 */
	public static final boolean isValidLiteral(String token) {
		try {
			getDataTypeNameOfLiteral(token);
			return true;
		} catch (VnanoFatalException e) {
			return false;
		}
	}


	/**
	 * Determines the data type of the specified literal and returns its name.
	 * 
	 * @param literal The literal for which get the name of the data type.
	 * @return The name of the data type of the literal.
	 * @throws VnanoFatalException Thrown when the specified literal could not be interpreted.
	 */
	public static final String getDataTypeNameOfLiteral(String literal) throws VnanoFatalException {

		int literalLength = literal.length();

		if (literal.matches(INT_LITERAL_REGEX)) {
			return DataTypeName.DEFAULT_INT;
		}

		if (literal.matches(FLOAT_LITERAL_REGEX)) {
			return DataTypeName.DEFAULT_FLOAT;
		}

		if (literal.matches(BOOL_LITERAL_REGEX)) {
			return DataTypeName.BOOL;
		}

		if (literal.charAt(0) == STRING_LITERAL_QUOT
				&& literal.charAt(literalLength-1) == STRING_LITERAL_QUOT
				&& literal.length()>=2) {
			return DataTypeName.STRING;
		}

		throw new VnanoFatalException("Invalid literal: " + literal);
	}


	/**
	 * Replaces some escape-sequences (\n, \r, \t, and so on) 
	 * contained in the specified string literal to corresponding characters.
	 * 
	 * @param stringLiteral The string literal (must begins/ends with a double-quotation) to be processed.
	 * @return The processed string literal.
	 */
	public static final String decodeEscapeSequences(String stringLiteral) {

		// Get UTF-16 characters composing the string literal.
		char[] chars = stringLiteral.toCharArray();
		int charLength = chars.length;

		// Create a buffer to build the result string.
		StringBuilder resultBuilder = new StringBuilder(charLength);

		// The flag representing whether the last character is the escape symbol '\'.
		boolean previousIsEscapeSymbol = false; // 直前がエスケープ文字だったかどうかのフラグ

		for (int i=0; i<charLength; i++) {

			// If the last char is the escape symbol '\', decode the escape sequence, and push the result to the buffer.
			if (previousIsEscapeSymbol) {
				switch (chars[i]) {
					case 't' : resultBuilder.append('\t'); break;
					case 'n' : resultBuilder.append('\n'); break;
					case 'r' : resultBuilder.append('\r'); break;
					case '"' : resultBuilder.append('"'); break;
					default : throw new VnanoFatalException("Unknown escape sequence: " + chars[i]);
				}

				// Resets the flag when it has completed to decode the escape sequence.
				previousIsEscapeSymbol = false;
				continue;
			}

			// If the current character is the escape symbol '\', enable the flag, 
			// and decode the escape sequence at the next cycle of the loop.
			// However, if the last character is also the escape symbol '\',
			// it means that "a escape symbol is escaped", so it should be decoded simply as the character '\'.
			// So in such case, we disable the flag, to read the '\' as a regular character.
			if (chars[i] == STEING_LITERAL_ESCAPE && !previousIsEscapeSymbol) {
				previousIsEscapeSymbol = true;
				continue;
			} else {
				previousIsEscapeSymbol = false;
			}

			// Regular character: simply append to the buffer.
			resultBuilder.append(chars[i]);
		}

		return resultBuilder.toString();
	}


	/**
	 * Extracts all string literals in the specified code, and replace them in the code to "numberized literals".
	 * 
	 * Where "numberized literals" are string literals consists of serial numbers, e.g.: "1", "2", ...
	 * The serial number in a numberized literal is the same as 
	 * the array index of the corresponding (original) string literal in the returned array of this method.
	 * 
	 * In the returned array of this method, the literal-replaced script code will is stored at [0]. 
	 * And extracted (original) string literals are stored at [1], [2], ... in the order of apparence in the script code.
	 *
	 * @param code The script code which may contain string literals.
	 * @return The array storing the literal-replaced code and extracted litarals. See the above description.
	 * @throws VnanoException Thrown when an unclosed string literal has been found. 
	 */
	public static final String[] extractStringLiterals(String code) throws VnanoException {
		char[] chars = code.toCharArray();
		int charLength = chars.length;

		StringBuilder resultCodeBuilder = new StringBuilder(charLength);
		StringBuilder literalBuilder = null;
		List<String> literalList = new ArrayList<String>();
		int literalNumber = 1;

		// The flag representing whether the last character is the escape symbol '\'.
		boolean previousIsEscapeChar = false;

		// The flag representing whether the current character is locating in a string literal.
		boolean inLiteral = false;

		// Traverse all characters in the code.
		for (int i=0; i<charLength; i++) {

			// Detections of the beginning/ending of a string literal.
			if (chars[i] == STRING_LITERAL_QUOT && !previousIsEscapeChar) {

				// The end of the current string literal.
				if (inLiteral) {
					inLiteral = false;

					// Push the literal-ending symbol to a buffer to store the content of the literal, 
					// and extract the content of the literal from the buffer, and register it to the list.
					literalBuilder.append(STRING_LITERAL_QUOT);
					String literal = literalBuilder.toString();
					literalBuilder = null;
					literalList.add(literal);

					// Push a numberized literal to the buffer to build the literal-escaped code, 
					//  and increment the serial number which will be assigned to the next numberized literal.
					resultCodeBuilder.append(STRING_LITERAL_QUOT);
					resultCodeBuilder.append(literalNumber);
					resultCodeBuilder.append(STRING_LITERAL_QUOT);
					literalNumber++;

				// The beginning of a new string literal.
				} else {
					inLiteral = true;

					// Create a buffer to store the content of the literal,
					// and push the literal-beginning symbol to it.
					literalBuilder = new StringBuilder();
					literalBuilder.append(STRING_LITERAL_QUOT);
				}
				continue;
			}

			// If the escape-symbol '\' has been detected,
			// register it to the flag because it may affect to the detection of the end of the current literal.
			// However, it the '\' is escaped by the previous '\', it don't affect to the detection of the end of the literal,
			// so cancel the flag in such case.
			if (inLiteral && chars[i] == STEING_LITERAL_ESCAPE && !previousIsEscapeChar) {
				literalBuilder.append(STEING_LITERAL_ESCAPE);
				previousIsEscapeChar = true;
				continue;
			} else {
				previousIsEscapeChar = false;
			}

			// General characters:
			//   Append it to the literal buffer if it is contained in a literal, 
			//   otherwise append it to the code buffer.
			if (inLiteral) {
				literalBuilder.append(chars[i]);
			} else {
				resultCodeBuilder.append(chars[i]);
			}
		}

		// If the literal detection flag is enabled when the all characters in the code has been read,
		// there is/are an unclosed literal(s) in the code.
		if (inLiteral) {
			throw new VnanoException(
					ErrorType.STRING_LITERAL_IS_NOT_CLOSED
			);
		}

		// Store results to an array, and return it.

		String[] result = new String[literalList.size() + 1];
		result[0] = resultCodeBuilder.toString();

		int resultIndex = 1;
		for (String literal: literalList) {
			result[resultIndex] = literal;
			resultIndex++;
		}

		return result;
	}


	/**
	 * Returns the serial number of the specified numberized string literal.
	 * 
	 * @param numberedLiteral The numberized string literal.
	 * @return The serial number of the numberized literal.
	 */
	public static final int getIndexOfNumberedStringLiteral(String numberedLiteral) {

		// Remove double-quatations at the beginning/end of the literal.
		numberedLiteral = numberedLiteral.substring(1, numberedLiteral.length()-1);

		// Convert the serial number part (string) to an int-type value, and return it.
		int index = -1;
		try {
			index = Integer.parseInt(numberedLiteral);
		} catch (NumberFormatException e) {
			throw new VnanoFatalException("Invalid numbered string literal: " + numberedLiteral, e);
		}
		return index;
	}
}
