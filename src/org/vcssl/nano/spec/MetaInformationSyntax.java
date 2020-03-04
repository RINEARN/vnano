/*
 * Copyright(C) 2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import org.vcssl.nano.VnanoFatalException;

// Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/spec/MetaInformationSyntax.html
// ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/spec/MetaInformationSyntax.html

/**
 * <p>
 * <span>
 * <span class="lang-en">
 * The class performing functions to generate/interpret meta information attached to VM instructions
 * </span>
 * <span class="lang-ja">
 * Vnano のVM命令に添付される、メタ情報の生成や解釈を行うクラスです
 * </span>
 * .
 * </p>
 *
 * <p>
 * &raquo; <a href="../../../../../src/org/vcssl/nano/spec/MetaInformationSyntax.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/spec/MetaInformationSyntax.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/spec/MetaInformationSyntax.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class MetaInformationSyntax {

	/**
	 * <span class="lang-en">Generates meta information in which specified line number and file name are embedded</span>
	 * <span class="lang-ja">指定されたスクリプトファイル名と行番号が埋め込まれた、メタ情報を生成して返します</span>
	 * .
	 * @param lineNumber
	 *   <span class="lang-en">The line number to be embedded in the meta information.</span>
	 *   <span class="lang-en">メタ情報に埋め込む行番号.</span>
	 *
	 * @param lineNumber
	 *   <span class="lang-en">The file name to be embedded in the meta information.</span>
	 *   <span class="lang-en">メタ情報に埋め込むスクリプトファイル名.</span>
	 *
	 * @return
	 *   <span class="lang-en">The generated meta information.</span>
	 *   <span class="lang-en">生成されたメタ情報.</span>
	 */
	public static String generateMetaInformation(int lineNumber, String fileName) {
		return "line=" + lineNumber + ", file=" + fileName;
	}


	/**
	 * <span class="lang-en">Extracts the line number embedded in the meta information</span>
	 * <span class="lang-ja">メタ情報に埋め込まれた行番号を抽出して返します</span>
	 * .
	 * @param metaInformation
	 *   <span class="lang-en">The meta information in which the line number to be extracted is embedded.</span>
	 *   <span class="lang-en">抽出する行番号が埋め込まれているメタ情報.</span>
	 *
	 * @return
	 *   <span class="lang-en">The extracted line number.</span>
	 *   <span class="lang-en">抽出された行番号.</span>
	 */
	public static int extractLineNumber(String metaInformation) {
		String[] items = metaInformation.split(",");
		for (String item: items) {
			if (item.trim().startsWith("line=")) {
				return Integer.parseInt(item.split("=")[1]);
			}
		}
		throw new VnanoFatalException("Invalid meta information: no line number found.");
	}


	/**
	 * <span class="lang-en">Extracts the file name embedded in the meta information</span>
	 * <span class="lang-ja">メタ情報に埋め込まれたファイル名を抽出して返します</span>
	 * .
	 * @param metaInformation
	 *   <span class="lang-en">The meta information in which the file name to be extracted is embedded.</span>
	 *   <span class="lang-en">抽出するファイル名が埋め込まれているメタ情報.</span>
	 *
	 * @return
	 *   <span class="lang-en">The extracted file name.</span>
	 *   <span class="lang-en">抽出されたファイル名.</span>
	 */
	public static String extractFileName(String metaInformation) {
		String[] items = metaInformation.split(",");
		for (String item: items) {
			if (item.trim().startsWith("file=")) {
				return item.split("=")[1];
			}
		}
		throw new VnanoFatalException("Invalid meta information: no file name found.");
	}
}
