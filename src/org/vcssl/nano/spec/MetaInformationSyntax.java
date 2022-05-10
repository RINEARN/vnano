/*
 * Copyright(C) 2020-2021 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.processor.Instruction;

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

	// 命令のメタ情報は、エラー原因箇所の取得など、VMの処理上の都合で用いられるものであるため、
	// 現状では実行時に（編集して再ビルドする事なしに）外から記法を変えられるようにはなっていません。

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
	 * <span class="lang-en">Extracts the line number embedded in the meta information linked to the specified instruction</span>
	 * <span class="lang-ja">指定された命令に紐づけられた、メタ情報に埋め込まれた行番号を抽出して返します</span>
	 * .
	 * @param instruction
	 *   <span class="lang-en">The instruction of which meta information contains the line number to be extracted.</span>
	 *   <span class="lang-en">行番号が埋め込まれているメタ情報が添付された命令.</span>
	 *
	 * @param memory
	 *   <span class="lang-en">The memory in which data of meta information is stored.</span>
	 *   <span class="lang-en">メタ情報のデータが格納されているメモリ.</span>
	 *
	 * @return
	 *   <span class="lang-en">The extracted line number.</span>
	 *   <span class="lang-en">抽出された行番号.</span>
	 */
	public static int extractLineNumber(Instruction instruction, Memory memory) {

		// 命令が持っているメタ情報アドレスでメモリを参照し、命令に対応するスクリプト名や行番号などを抽出
		DataContainer<?> metaContainer = memory.getDataContainer(
				instruction.getMetaPartition(), instruction.getMetaAddress()
		);
		String metaInformation = ((String[])metaContainer.getArrayData())[0];
		return extractLineNumber(metaInformation);
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


	/**
	 * <span class="lang-en">Extracts the file name embedded in the meta information linked to the specified instruction</span>
	 * <span class="lang-ja">指定された命令に紐づけられた、メタ情報に埋め込まれたファイル名を抽出して返します</span>
	 * .
	 * @param instruction
	 *   <span class="lang-en">The instruction of which meta information contains the file name to be extracted.</span>
	 *   <span class="lang-en">ファイル名が埋め込まれているメタ情報が添付された命令.</span>
	 *
	 * @param memory
	 *   <span class="lang-en">The memory in which data of meta information is stored.</span>
	 *   <span class="lang-en">メタ情報のデータが格納されているメモリ.</span>
	 *
	 * @return
	 *   <span class="lang-en">The extracted file name.</span>
	 *   <span class="lang-en">抽出されたファイル名.</span>
	 */
	public static String extractFileName(Instruction instruction, Memory memory) {

		// 命令が持っているメタ情報アドレスでメモリを参照し、命令に対応するスクリプト名や行番号などを抽出
		DataContainer<?> metaContainer = memory.getDataContainer(
				instruction.getMetaPartition(), instruction.getMetaAddress()
		);
		String metaInformation = ((String[])metaContainer.getArrayData())[0];
		return extractFileName(metaInformation);
	}

}
