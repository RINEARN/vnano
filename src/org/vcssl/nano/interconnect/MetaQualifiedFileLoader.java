/*
 * Copyright(C) 2020-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.LiteralSyntax;
import org.vcssl.nano.spec.ScriptWord;


/**
 * The class to perform loading of text file which is qualified with meta information for loadings,
 * such as encoding-declaration.
 * 
 * If the content of the file starts with "coding", the first line of the file will be regarded as "encoding-declaration".
 * The syntax of encoding declarations is: "coding encodingName;".
 * Also, "#" can be appended at the head of encoding declarations.
 * In addition, "encode" or "encoding" can be used instead of "coding",
 * for keeping compatibility with the old VCSSL code, although they are not recommended.
 * As the specification to make it easy to detect/parse a encoding-declaration,
 * it should be described as the single line at the top of the file,
 * and no comments allowed before the end of the encoding-declaration.
 * 
 * If the encoding-declaration exists in the file, it will be used for decoding the content of the file.
 * Otherwise, the specified default encoding will be used.
 * Also, the normalization of environment-dependency and encoding-dependency will be performed
 * to the loaded content, so all line-feed codes in the content will be replaces to LF (\n).
 */
public class MetaQualifiedFileLoader {

	/**
	 * The prefix of the encoding-declaration line in files.
	 */
	private static final String[] ENCODING_DECLARATION_LINE_HEAD = {
		// White-spaces will be removed before the detection of the encoding declaration,
		// so don't include white spaces into following values.
		"coding",
		"#coding",
		"encoding",
		"#encoding",
		"encode",
		"#encode",
	};


	/**
	 * Loads the content of the file.
	 * 
	 * If the encoding-declaration exists in the file, it will be used for decoding the file.
	 * Otherwise, the specified default encoding will be used.
	 * The encoding-declaration will be removed from the loaded content,
	 * so it will not be contained in the returned value of this method.
	 * Also, the normalization of environment-dependency and encoding-dependency will be performed
	 * to the loaded content, so all line-feed codes in the content will be replaces to LF (\n).
	 *
	 * @param filePath The path of the file to be loaded.
	 * @param defaultEncodingName The name of the default encoding.
	 * @return The loaded and normalized content.
	 */
	public static final String load(String filePath, String defaultEncoding)
			throws VnanoException {

		if (!new File(filePath).exists()) {
			throw new VnanoException(ErrorType.META_QUALIFIED_FILE_DOES_NOT_EXIST, filePath);
		}

		// If there is an encoding declaration line in the file, detect the encoding (charset) from it.
		// Otherwise use the default encoding (charset).
		Charset charset = determinCharset(filePath, defaultEncoding);

		// Load all lines from the file.
		List<String> lineList = null;
		try {
			lineList = Files.readAllLines(Paths.get(filePath), charset);
		} catch (IOException ioe) {
			throw new VnanoException(ErrorType.META_QUALIFIED_FILE_IS_NOT_ACCESSIBLE, filePath, ioe);
		}

		// Join all lines with line feeds (code: LF, \n).
		String content = String.join("\n", lineList.toArray(new String[0]) );

		// Normalize the loaded content.
		content = postprocess(filePath, content);

		return content;
	}


	/**
	 * Performs the same post-processing as the content loaded by
	 * {MetaQualifiedFileLoader#load(String, String, LanguageSpecContainer) load}
	 * method of this class, to the content loaded independently at outside this class
	 *
	 * @param fileName The name of the loaded file (will be used in error messages).
	 * @param fileContent The content of the loaded file.
	 * @return The postprocessed content.
	 */
	public static final String postprocess(String fileName, String fileContent)
			throws VnanoException {

		fileContent = normalize(fileContent);
		fileContent = removeEncodingDeclaration(fileName, fileContent);
		return fileContent;
	}


	/**
	 * Determins/returns the appropriate Charset from the encoding-declaration in the specified file.
	 * 
	 * @param filePath The path of the file to be loaded.
	 * @param defaultEncodingName The name of the default encoding.
	 * @return
	 *   If the encoding-declaration exists, Charset corresponding with it will be returned.
	 *   If the encoding-declaration does not exist, Charset corresponding with the default encoding will be returned.
	 */
	private static final Charset determinCharset(String filePath, String defaultEncodingName)
			throws VnanoException {

		// Detect the encoding from the encoding declaration line.
		String declEncodingName = readDeclaredEncodingName(filePath, defaultEncodingName);

		// If there is no encoding declaration line, return the default encoding (charset).
		if (declEncodingName == null) {
			return Charset.forName(defaultEncodingName);

		// If the encoding (charset) is declared, return it.
s		} else {
			try {
				return Charset.forName(declEncodingName);

			} catch (IllegalCharsetNameException | UnsupportedCharsetException ce) {
				throw new VnanoException(
					ErrorType.DECLARED_ENCODING_IS_UNSUPPORTED, new String[] { declEncodingName, filePath }, ce
				);
			}
		}
	}


	/**
	 * Reads the name of the declared encoding from the specified file and returns it,
	 * if the encoding-declaration exists in the specified file.
	 * 
	 * @param fileName The path of the file.
	 * @param encodingNameForReading The name of the encoding for reading the file.
	 * @return The name of the declared encoding (or, null if there is no encoding-declaration in the file).
	 */
	private static final String readDeclaredEncodingName(String filePath, String encodingNameForReading)
			throws VnanoException {

		// Valid encoding declaration must be at the first line in a file, so read the first line.
		// Latter lines may contain unrepresentable characters in the specified encoding (charset), so read ONLY the first line.
		String firstLine = null;
		Charset charset = Charset.forName(encodingNameForReading);
		try (BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( new FileInputStream(filePath), charset) ) ) {
			firstLine = bufferedReader.readLine();
		} catch (IOException ioe) {
			throw new VnanoException(ErrorType.META_QUALIFIED_FILE_IS_NOT_ACCESSIBLE, filePath, ioe);
		}
		firstLine = normalize(firstLine);

		// Parse and extract the declared encoding name, from the encoding declaration line.
		return extractDeclaredEncodingName(filePath, firstLine);
	}


	/**
	 * Returns the name of the declared encoding, if the encoding-declaration exists in the specified file content.
	 * 
	 * @param fileName The name of the file.
	 * @param fileContent The content of the file.
	 * @return The name of the declared encoding (or, null if there is no encoding-declaration in the file).
	 */
	private static final String extractDeclaredEncodingName(String fileName, String fileContent)
			throws VnanoException {

		if (fileContent.length() == 0) {
			return null;
		}

		// Normalize encoding/environment dependent characters and so on.
		// The line feed code will also be replaced to LF (\n) at here.
		fileContent = normalize(fileContent);

		// The first line may be an encoding declaration line, so extract the first line.
		String firstLine = fileContent.split("\\n", -1)[0];

		// Remove white spaces in the extracted first line.
		firstLine = firstLine.replaceAll("\\s", "").replaceAll("\\t", "");

		// If the extracted first line is an encoding declaration, detect the name of the declared encoding (charset).
		String encodingName = null;
		for (String declLineHead: ENCODING_DECLARATION_LINE_HEAD) {
			if (firstLine.startsWith(declLineHead)) {

				// The encoding name is described between 
				// the encoding declaration keyword "coding" (etc.) and the end-of-statement symbol ";", so extract it.
				int encodingDeclEnd = firstLine.indexOf(ScriptWord.END_OF_STATEMENT);
				if (encodingDeclEnd != -1) {
					encodingName = firstLine.substring(declLineHead.length(), encodingDeclEnd);
					break;

				// If there is no end-of-statement symbol ";" in the encoding declaration line: Error
				//（文字コード宣言の抽出はファイル読み込みの最初の一歩なので、抽出処理を簡単にできるようにそういう仕様にする）
				} else {
					throw new VnanoException(ErrorType.NO_ENCODING_DECLARATION_END, fileName);
				}
			}
		}

		// To make simple and sure the detection of the encoding, 
		// can not use comments and string literals in the encoding declaration line.
		// So check that they aren't used.
		String[] invalidSymbols = new String[] {
			ScriptWord.LINE_COMMENT_PREFIX,
			ScriptWord.BLOCK_COMMENT_BEGIN,
			ScriptWord.BLOCK_COMMENT_END,
			Character.toString(LiteralSyntax.STRING_LITERAL_QUOT),
			Character.toString(LiteralSyntax.CHAR_LITERAL_QUOT),
		};
		for (String invalidSymbol: invalidSymbols) {
			if (encodingName != null && encodingName.contains(invalidSymbol)) {
				throw new VnanoException(
					ErrorType.ENCODING_DECLARATION_CONTAINS_INVALID_SYMBOL,
					new String[] { invalidSymbol, fileName }
				);
			}
		}

		return encodingName;
	}


	/**
	 * Checks whether the encoding-declaration exists in the specified file content or not.
	 * 
	 * @param fileName The name of the file.
	 * @param fileContent The content of the file.
	 * @return true if the encoding declaration exists.
	 */
	private static final boolean existsEncodingDeclaration(String fileName, String fileContent)
			throws VnanoException {

		return extractDeclaredEncodingName(fileName, fileContent) != null;
	}


	/**
	 * Removes the encoding-declaration from the file content.
	 * 
	 * @param fileName The name of the file.
	 * @param fileContent The content of the file.
	 * @return The content of the file from which the encoding declaration is removed.
	 */
	private static final String removeEncodingDeclaration(String fileName, String fileContent)
			throws VnanoException {

		// If there is an encoding declaration line exists: remove the line.
		if (existsEncodingDeclaration(fileName, fileContent)) {
			int encodingDeclEnd = fileContent.indexOf(ScriptWord.END_OF_STATEMENT);
			return fileContent.substring(encodingDeclEnd + 1, fileContent.length());

		// If there is no encoding declaration line: do nothing.
		} else {
			return fileContent;
		}
	}


	/**
	 * Normalize the environment-dependent / encoding-dependent content loaded from a file.
	 * 
	 * For example, by this method,
	 * all environment-dependent line-feed codes in the content will be replaced to LF (\n).
	 * In addition, this method normalizes encoding/decoding-dependent differences of the content.
	 *
	 * @param content The content to be normalized.
	 * @return The normalized content.
	 */
	private static final String normalize(String content) {

		// If the loaded file had contained only EOF, the read result is null.
		// Normalize such result to the embly string.
		if (content == null) {
			return "";
		}

		// Normalize line feed codes (CRLF, CR, LF) to LF.
		content = content.replaceAll("\\r\\n", "\n");
		content = content.replaceAll("\\r", "\n");

		// A kind of whitespace U+FEFF (unicode) should not be contained in strings read by using encodings (charsets) supported by this script engine.
		// However, when the read file is encoded in UTF-8, and its contents starts with the byte-order-mark bytes "0xEF 0xBB 0xBF", 
		// they will be decoded as the UTF-8 representation of a kind of whitespace U+FEFF.
		// And then, the decoded "U+FEFF" char will be stored at the top of the read result string, as a char of which value is 0xFEFF.
		// (Note that, the internal encoding of "char" type is UTF-16.)
		// Hence, remove the first char if its value is 0xFEFF.
		if (0 < content.length() && content.charAt(0) == (char)0xFEFF) {
			content = content.substring(1, content.length());
		}

		return content;
	}

}
