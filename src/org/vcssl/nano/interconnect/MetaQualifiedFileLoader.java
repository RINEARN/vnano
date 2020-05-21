/*
 * Copyright(C) 2020 RINEARN (Fumihiro Matsui)
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
import org.vcssl.nano.spec.LanguageSpecContainer;


/**
 * <span class="lang-en">
 * The class to perform loading of text file which is qualified with meta information for loadings,
 * such as encoding-declaration
 * </span>
 * <span class="lang-ja">
 * 文字コード宣言などのメタ情報が付加されたテキストファイルを読み込むローダです
 * </span>
 * .
 * <span class="lang-en">
 * If the content of the file starts with "coding", the first line of the file will be regarded as "encoding-declaration".
 * The syntax of encoding declarations is: "coding encodingName;".
 * Also, "#" can be appended at the head of encoding declarations.
 * In addition, "encode" or "encoding" can be used instead of "coding",
 * for keeping compatibility with the old VCSSL code, although they are not recommended.
 * As the specification to make it easy to detect/parse a encoding-declaration,
 * it should be described as the single line at the top of the file,
 * and no comments allowed before the end of the encoding-declaration.
 * </span>
 *
 * <span class="lang-ja">
 * このローダがファイルを読み込む際, ファイルの先頭が文字列「 coding 」で始まっている場合には,
 * そのファイルの先頭行は文字コード宣言であると見なされます。文字コード宣言の記法は「 coding 文字コード名; 」です.
 * 文字コード宣言の先頭には「 # 」を付加する事もでき,
 * また, VCSSLでの歴史的経緯との関係で, 「 coding 」の代わりに「 encode 」「 encoding 」を使用する事も可能です (推奨はされません).
 * 検出を容易にするため, 文字コード宣言は必ず先頭行内で完結している必要があり,
 * また, 文字コード宣言の終端よりも前にコメントを含む事はできません.
 * </span>
 *
 * <span class="lang-en">
 * If the encoding-declaration exists in the file, it will be used for decoding the content of the file.
 * Otherwise, the specified default encoding will be used.
 * Also, the normalization of environment-dependency and encoding-dependency will be performed
 * to the loaded content, so all line-feed codes in the content will be replaces to LF (\n).
 * </span>
 * <span class="lang-ja">
 * ファイル内に文字コード宣言がある場合, このローダはその文字コードを使用してファイル内容を読み込みます.
 * 文字コード宣言が無い場合には, デフォルトの文字コードが使用されます.
 * なお, 読み込まれたファイルの内容は, 環境依存やエンコーディング/デコーディング依存による内容の揺れが正規化され,
 * 従って改行コードは必ず LF (\n) に統一されます.
 * </span>
 */
public class MetaQualifiedFileLoader {

	/**
	 * <span class="lang-en">
	 * The prefix of the encoding-declaration line in files
	 * </span>
	 * <span class="lang-ja">
	 * ファイル内の文字コード宣言行のプレフィックスです
	 * </span>
	 * .
	 */
	private static final String[] ENCODING_DECLARATION_LINE_HEAD = {
		"coding",
		"#coding", // スペースは詰めた状態で判定されるので挟まなくてもいい
		"encoding",
		"#encoding",
		"encode",
		"#encode",
	};


	/**
	 * <span class="lang-en">
	 * Loads the content of the file
	 * </span>
	 * <span class="lang-ja">
	 * 指定されたファイルの内容を読み込みます
	 * </span>
	 * .
	 * <span class="lang-en">
	 * If the encoding-declaration exists in the file, it will be used for decoding the file.
	 * Otherwise, the specified default encoding will be used.
	 * The encoding-declaration will be removed from the loaded content,
	 * so it will not be contained in the returned value of this method.
	 * Also, the normalization of environment-dependency and encoding-dependency will be performed
	 * to the loaded content, so all line-feed codes in the content will be replaces to LF (\n).
	 * </span>
	 *
	 * <span class="lang-ja">
	 * ファイル内に文字コード宣言が記述されている場合, その文字コードが, ファイルの読み込みに使用されます.
	 * 文字コード宣言が無い場合には, 指定されたデフォルトの文字コードが使用されます.
	 * 文字コード宣言は, 読み込んだ内容からは削除されるため, このメソッドの戻り値の内容には含まれません.
	 * なお, 読み込まれたファイルの内容は,
	 * 環境依存やエンコーディング/デコーディング依存による内容の揺れが正規化されており,
	 * 従って改行コードは必ず LF (\n) に統一されています.
	 * </span>
	 *
	 * @param filePath
	 *   <span class="lang-en">The path of the file to be loaded</span>
	 *   <span class="lang-ja">読み込むファイルのパス</span>
	 *
	 * @param defaultEncodingName
	 *   <span class="lang-en">The name of the default encoding</span>
	 *   <span class="lang-ja">デフォルトの文字コードの名称</span>
	 *
	 * @param langSpec
	 *   <span class="lang-en">language specification settings</span>
	 *   <span class="lang-ja">言語仕様設定</span>
	 *
	 * @return
	 *   <span class="lang-en">The loaded and normalized content</span>
	 *   <span class="lang-ja">読み込まれて正規化された内容</span>
	 */
	public static final String load(String filePath, String defaultEncoding, LanguageSpecContainer langSpec)
			throws VnanoException {

		if (!new File(filePath).exists()) {
			throw new VnanoException(ErrorType.GENERAL_FILE_DOES_NOT_EXIST, filePath);
		}

		// ファイル内に文字コード宣言があればその文字コード、無ければデフォルトの文字コードから Charset を生成
		Charset charset = determinCharset(filePath, defaultEncoding, langSpec);

		// ファイルの全行を読み込む
		List<String> lineList = null;
		try {
			lineList = Files.readAllLines(Paths.get(filePath), charset);
		} catch (IOException ioe) {
			throw new VnanoException(ErrorType.GENERAL_FILE_IS_NOT_ACCESSIBLE, filePath, ioe);
		}

		// 改行コード LF (\n) で結合（それ以外の改行コードを用いても、後の normalize で LF に統一される）
		String content = String.join("\n", lineList.toArray(new String[0]) );

		// 環境依存やエンコーディング/デコーディング依存による内容の揺れの正規化や、文字コード宣言の削除を行う
		content = postprocess(filePath, content, langSpec);

		return content;
	}


	/**
	 * <span class="lang-en">
	 * Performs the same post-processing as the content loaded by
	 * {MetaQualifiedFileLoader#load(String, String, LanguageSpecContainer) load}
	 * method of this class, to the content loaded independently at outside this class
	 * </span>
	 * <span class="lang-ja">
	 * このクラス外などで別途ファイルから読み込んだ内容に対して, このクラスの
	 * {MetaQualifiedFileLoader#load(String, String, LanguageSpecContainer) load}
	 * メソッドで読み込んだ場合と同様の後処理を行います
	 * </span>
	 *
	 * @param fileName
	 *   <span class="lang-en">The name of the loaded file (will be used in error messages)</span>
	 *   <span class="lang-ja">読み込まれたファイルの名前（エラーメッセージ等で使用されます）</span>
	 *
	 * @param fileContent
	 *   <span class="lang-en">The content of the loaded file</span>
	 *   <span class="lang-ja">読み込まれたファイルの内容</span>
	 *
	 * @param langSpec
	 *   <span class="lang-en">language specification settings</span>
	 *   <span class="lang-ja">言語仕様設定</span>
	 *
	 * @return
	 *   <span class="lang-en">The postprocessed content</span>
	 *   <span class="lang-ja">後処理を行った内容</span>
	 */
	public static final String postprocess(String fileName, String fileContent, LanguageSpecContainer langSpec)
			throws VnanoException {

		fileContent = normalize(fileContent);
		fileContent = removeEncodingDeclaration(fileName, fileContent, langSpec);
		return fileContent;
	}


	/**
	 * <span class="lang-en">
	 * Determins/returns the appropriate Charset from the encoding-declaration in the specified file
	 * </span>
	 * <span class="lang-ja">
	 * 指定されたファイル内の文字コード宣言などに基づいて, 適切な Charset を生成して返します
	 * </span>
	 * .
	 * @param filePath
	 *   <span class="lang-en">The path of the file to be loaded</span>
	 *   <span class="lang-ja">読み込むファイルのパス</span>
	 *
	 * @param defaultEncodingName
	 *   <span class="lang-en">The name of the default encoding</span>
	 *   <span class="lang-ja">デフォルトの文字コードの名称</span>
	 *
	 * @param langSpec
	 *   <span class="lang-en">language specification settings</span>
	 *   <span class="lang-ja">言語仕様設定</span>
	 *
	 * @return
	 *   <span class="lang-en">
	 *   If the encoding-declaration exists, Charset corresponding with it will be returned.
	 *   If the encoding-declaration does not exist, Charset corresponding with the default encoding will be returned.
	 *   </span>
	 *   <span class="lang-ja">
	 *   文字コード宣言がある場合, 宣言されている文字コードに対応する Charset が返されます.
	 *   文字コード宣言が無い場合, デフォルトの文字コードに対応する Charset が返されます.
	 *   </span>
	 */
	private static final Charset determinCharset(String filePath, String defaultEncodingName, LanguageSpecContainer langSpec)
			throws VnanoException {

		// 文字コード宣言を読み、宣言されている文字コードの名称を取得
		String declEncodingName = readDeclaredEncodingName(filePath, defaultEncodingName, langSpec);

		// 文字コードが宣言されていなければ、デフォルトの文字コードを返す
		if (declEncodingName == null) {
			return Charset.forName(defaultEncodingName);

		// 文字コードが宣言されていれば、それを返す
		} else {
			try {
				return Charset.forName(declEncodingName);

			// 無効な文字コード名だった場合
			} catch (IllegalCharsetNameException | UnsupportedCharsetException ce) {
				throw new VnanoException(
					ErrorType.DECLARED_ENCODING_IS_UNSUPPORTED, new String[] { declEncodingName, filePath }, ce
				);
			}
		}
	}


	/**
	 * <span class="lang-en">
	 * Reads the name of the declared encoding from the specified file and returns it,
	 * if the encoding-declaration exists in the specified file
	 * </span>
	 * <span class="lang-ja">
	 * 指定されたファイル内に、文字コード宣言が記述されている場合, 宣言されている文字コード名を読んで返します
	 * </span>
	 * .
	 * @param fileName
	 *   <span class="lang-en">The path of the file</span>
	 *   <span class="lang-ja">対象ファイルのパス</span>
	 *
	 * @param encodingNameForReading
	 *   <span class="lang-en">The name of the encoding for reading the file</span>
	 *   <span class="lang-ja">ファイルを読み込む際の文字コードの名称</span>
	 *
	 * @param langSpec
	 *   <span class="lang-en">language specification settings</span>
	 *   <span class="lang-ja">言語仕様設定</span>
	 *
	 * @return
	 *   <span class="lang-en">The name of the declared encoding (or, null if there is no encoding-declaration in the file)</span>
	 *   <span class="lang-ja">宣言されている文字コードの名称 (文字コード宣言が無い場合は null)</span>
	 */
	private static final String readDeclaredEncodingName(String filePath, String encodingNameForReading, LanguageSpecContainer langSpec)
			throws VnanoException {

		// 有効な文字コード宣言は先頭行で完結しているため、ファイルの先頭行のみを読み込む
		// （その後の本文パートには、読み込み用文字コードで解釈できない文字バイト列も存在し得るので、そこまで読もうとするとエラーになり得る）
		String firstLine = null;
		Charset charset = Charset.forName(encodingNameForReading);
		try (BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( new FileInputStream(filePath), charset) ) ) {
			firstLine = bufferedReader.readLine();
		} catch (IOException ioe) {
			throw new VnanoException(ErrorType.GENERAL_FILE_IS_NOT_ACCESSIBLE, filePath, ioe);
		}
		firstLine = normalize(firstLine);

		// 文字コード宣言を解釈して文字コード名を取得（無い場合はnull）
		return extractDeclaredEncodingName(filePath, firstLine, langSpec);
	}


	/**
	 * <span class="lang-en">
	 * Returns the name of the declared encoding, if the encoding-declaration exists in the specified file content
	 * </span>
	 * <span class="lang-ja">
	 * 指定されたファイル内容の中に文字コード宣言が記述されている場合, 宣言されている文字コード名を抽出して返します
	 * </span>
	 * .
	 * @param fileName
	 *   <span class="lang-en">The name of the file</span>
	 *   <span class="lang-ja">対象ファイルの名称</span>
	 *
	 * @param fileContent
	 *   <span class="lang-en">The content of the file</span>
	 *   <span class="lang-ja">対象ファイルの内容</span>
	 *
	 * @param langSpec
	 *   <span class="lang-en">language specification settings</span>
	 *   <span class="lang-ja">言語仕様設定</span>
	 *
	 * @return
	 *   <span class="lang-en">The name of the declared encoding (or, null if there is no encoding-declaration in the file)</span>
	 *   <span class="lang-ja">宣言されている文字コードの名称 (文字コード宣言が無い場合は null)</span>
	 */
	private static final String extractDeclaredEncodingName(String fileName, String fileContent, LanguageSpecContainer langSpec)
			throws VnanoException {

		// 内容が無い場合は明らかに文字コード宣言も無いので終了
		if (fileContent.length() == 0) {
			return null;
		}

		// 環境依存やエンコーディング依存の内容を統一（改行コードも \n に統一される）
		fileContent = normalize(fileContent);

		// 文字コード宣言がある場合、その位置は先頭行に限られるので、先頭行を抽出する
		String firstLine = fileContent.split("\\n", -1)[0];  // 上で内容が無い場合を弾いているので、[0] が領域外になる事はあり得ない

		// 先頭行の中にある空白の類を詰める
		firstLine = firstLine.replaceAll("\\s", "").replaceAll("\\t", "");

		// 先頭行が文字コード宣言のキーワードで始まっている場合、解釈して文字コード名を抽出
		String encodingName = null;
		for (String declLineHead: ENCODING_DECLARATION_LINE_HEAD) {
			if (firstLine.startsWith(declLineHead)) {

				// 文字コード宣言のキーワードと文末記号との間に囲まれた部分（空白は除去済み）が文字コード名なので、抽出する
				int encodingDeclEnd = firstLine.indexOf(langSpec.SCRIPT_WORD.endOfStatement);
				if (encodingDeclEnd != -1) {
					encodingName = firstLine.substring(declLineHead.length(), encodingDeclEnd);
					break;

				// 文字コード宣言キーワードで始まっている行内に、文末記号が無い場合はエラーとする
				//（文字コード宣言の抽出はファイル読み込みの最初の一歩なので、抽出処理を簡単にできるようにそういう仕様にする）
				} else {
					throw new VnanoException(ErrorType.NO_ENCODING_DECLARATION_END, fileName);
				}
			}
		}

		// 文字コード宣言内にコメントや文字列リテラルを使用できると、
		// ファイル読み込みの最初の一歩の段階で、かなり複雑な解析が必要になってしてしまう。
		// そのため、文字コード宣言内では上記のようなものは使えないものとし、実際に使っていない事を検査しておく。
		// （処理系の解釈の仕方によって挙動が変わるのを避けるため）
		String[] invalidSymbols = new String[] {
			langSpec.SCRIPT_WORD.lineCommentPrefix,
			langSpec.SCRIPT_WORD.blockCommentBegin,
			langSpec.SCRIPT_WORD.blockCommentEnd,
			Character.toString(langSpec.LITERAL_SYNTAX.stringLiteralQuot),
			Character.toString(langSpec.LITERAL_SYNTAX.charLiteralQuot),
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
	 * <span class="lang-en">
	 * Checks whether the encoding-declaration exists in the specified file content or not
	 * </span>
	 * <span class="lang-ja">
	 * 指定されたファイル内容の中に、文字コード宣言が記述されているかどうかを判定します
	 * </span>
	 * .
	 * @param fileName
	 *   <span class="lang-en">The name of the file</span>
	 *   <span class="lang-ja">対象ファイルの名称</span>
	 *
	 * @param fileContent
	 *   <span class="lang-en">The content of the file</span>
	 *   <span class="lang-ja">対象ファイルの内容</span>
	 *
	 * @param langSpec
	 *   <span class="lang-en">language specification settings</span>
	 *   <span class="lang-ja">言語仕様設定</span>
	 *
	 * @return
	 *   <span class="lang-en">true if the encoding declaration exists</span>
	 *   <span class="lang-ja">文字コード宣言が存在すれば true</span>
	 */
	private static final boolean existsEncodingDeclaration(String fileName, String fileContent, LanguageSpecContainer langSpec)
			throws VnanoException {

		return extractDeclaredEncodingName(fileName, fileContent, langSpec) != null;
	}


	/**
	 * <span class="lang-en">
	 * Removes the encoding-declaration from the file content
	 * </span>
	 * <span class="lang-ja">
	 * 指定されたファイル内容から、文字コード宣言を削除して返します
	 * </span>
	 * .
	 * @param fileName
	 *   <span class="lang-en">The name of the file</span>
	 *   <span class="lang-ja">対象ファイルの名称</span>
	 *
	 * @param fileContent
	 *   <span class="lang-en">The content of the file</span>
	 *   <span class="lang-ja">対象ファイルの内容</span>
	 *
	 * @param langSpec
	 *   <span class="lang-en">language specification settings</span>
	 *   <span class="lang-ja">言語仕様設定</span>
	 *
	 * @return
	 *   <span class="lang-en">the content of the file from which the encoding declaration is removed</span>
	 *   <span class="lang-ja">文字コード宣言が削除された, 対象ファイルの内容</span>
	 */
	private static final String removeEncodingDeclaration(String fileName, String fileContent, LanguageSpecContainer langSpec)
			throws VnanoException {

		// 文字コード宣言がある場合： 最初の文末記号までが文字コード宣言なので、その後の残りを返す
		if (existsEncodingDeclaration(fileName, fileContent, langSpec)) {
			int encodingDeclEnd = fileContent.indexOf(langSpec.SCRIPT_WORD.endOfStatement); // 文字コード宣言がある場合は、文末記号は必ずあるはず
			return fileContent.substring(encodingDeclEnd + 1, fileContent.length()); // 同様に、このインデックスが範囲外になる事はあり得ないはず

		// 文字コード宣言が無い場合： 全体をそのまま返す
		} else {
			return fileContent;
		}
	}


	/**
	 * <span class="lang-en">
	 * Normalize the environment-dependent / encoding-dependent content loaded from a file
	 * </span>
	 * <span class="lang-ja">
	 * ファイルから読み込んだ, 環境やエンコーディングに依存する内容を正規化します
	 * </span>
	 * .
	 * <span class="lang-en">
	 * For example, by this method,
	 * all environment-dependent line-feed codes in the content will be replaced to LF (\n).
	 * In addition, this method normalizes encoding/decoding-dependent differences of the content.
	 * </span>
	 * <span class="lang-ja">
	 * 例えば, このメソッドによって, 環境依存の改行コードは全てLF (\n) に統一されます.
	 * また, エンコーディング/デコーディング由来の内容の差異なども正規化されます.
	 * </span>
	 *
	 * @param content
	 *   <span class="lang-en">The content to be normalized</span>
	 *   <span class="lang-ja">正規化する内容</span>
	 *
	 * @return
	 *   <span class="lang-en">The normalized content</span>
	 *   <span class="lang-ja">正規化された内容</span>
	 */
	private static final String normalize(String content) {

		// 改行コードは環境に依存して CRLF (\r\n), CR (\r), LF (\n) があるが、LF (\n) のみに統一する。
		content = content.replaceAll("\\r\\n", "\n");
		content = content.replaceAll("\\r", "\n");

		// Unicodeの空白文字の一種 U+FEFF は、通常この処理系が想定する正しいコードには混ざっていない（混ざっていてはいけない）。
		// しかしながら、読み込み元ファイルが UTF-8 でBOMバイト列 0xEF 0xBB 0xBF で始まっていた場合、
		// それは通常の読み込み方法では、BOMではなくこの U+FEFF 文字のUTF-8表現と見なしてデコードされる（解釈に任意性がある）。
		// そして、文字列の内部表現はUTF-16なため、上記文字は文字列先頭において、値が 0xFEFF の char 一文字として格納されている。
		// 従って、内容の先頭がこの文字であれば削除する。
		// （読み込み元が UTF-8 以外でも、この文字で始まるコードはそもそもこの処理系では正しくないため、統一的に削除する仕様にして問題ないと思う）
		if (0 < content.length() && content.charAt(0) == (char)0xFEFF) {
			content = content.substring(1, content.length());
		}

		return content;
	}
}