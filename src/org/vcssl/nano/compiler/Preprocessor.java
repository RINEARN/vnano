/*
 * Copyright(C) 2018-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import org.vcssl.nano.spec.ScriptWord;
import org.vcssl.nano.spec.LanguageSpecContainer;

//Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/compiler/Preprocessor.html
//ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/compiler/Preprocessor.html

/**
 * <p>
 * <span class="lang-en">
 * The class performing the function of the preprocessor in the compiler of the Vnano
 * </span>
 * <span class="lang-ja">
 * Vnano のコンパイラ内において, プリプロセッサの機能を担うクラスです
 * </span>
 * .
 * </p>
 * <span class="lang-en">
 * This preprocessor are used for removing comments in the script,
 * unifying line feed code, and so on as a first stage of the compilation.
 * </span>
 * <span class="lang-ja">
 * このプリプロセッサは, コンパイルの最初のステップとして,
 * スクリプト内のコメントを除去したり, 改行コードを統一したりします.
 * </span>
 * <p>
 *
 * <p>
 * &raquo; <a href="../../../../../src/org/vcssl/nano/compiler/Preprocessor.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/compiler/Preprocessor.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/compiler/Preprocessor.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class Preprocessor {

	/** スクリプト言語の語句が定義された設定オブジェクトを保持します。 */
	private final ScriptWord SCRIPT_WORD;


	/**
	 * <span class="lang-en">
	 * Create a new preprocessor with the specified language specification settings
	 * </span>
	 * <span class="lang-ja">
	 * 指定された言語仕様設定で, プリプロセッサを生成します
	 * </span>
	 * .
	 * @param langSpec
	 *   <span class="lang-en">language specification settings.</span>
	 *   <span class="lang-ja">言語仕様設定.</span>
	 */
	public Preprocessor(LanguageSpecContainer langSpec) {
		this.SCRIPT_WORD = langSpec.SCRIPT_WORD;
	}


	/**
	 * <span class="lang-en">Preprocess the script</span>
	 * <span class="lang-ja">スクリプトに前処理を施します</span>
	 * .
	 * <span class="lang-en">
	 * As the result of this preprocessing, comments in the script will be removed,
	 * and line feed code will be unified to LF (0x0A).
	 * </span>
	 * <span class="lang-ja">
	 * この処理の結果として, コード内の全てのコメントが削除されます.
	 * また, コード内の改行コードは LF (0x0A) に統一されます.
	 * </span>
	 *
	 * @param script
	 *   <span class="lang-en">The script to be preprocessed.</span>
	 *   <span class="lang-ja">前処理を施すスクリプト.</span>
	 *
	 * @return
	 *   <span class="lang-en">The preprocessed script.</span>
	 *   <span class="lang-ja">前処理が施されたスクリプト.</span>
	 */
	public String preprocess(String script) {

		// Unify line feed code to LF (0x0A).
		// 改行コードを LF (0x0A) に統一
		script = this.replaceEndOfLineCode(script);

		// Remove comments.
		// コメントを削除
		script = this.removeAllComments(script);

		return script;
	}


	/**
	 * <span class="lang-en">Unifies line feed code to LF (0x0A)</span>
	 * <span class="lang-ja">スクリプト内の改行コードを全て LF (0x0A) に統一します</span>
	 * .
	 * @param script
	 *   <span class="lang-en">The script to be processed.</span>
	 *   <span class="lang-ja">処理対象のスクリプト.</span>
	 *
	 * @return
	 *   <span class="lang-en">The script in which line feed code have unified.</span>
	 *   <span class="lang-ja">改行コードが統一されたスクリプト.</span>
	 */
	private String replaceEndOfLineCode(String script) {
		script = script.replace("\r\n", "\n");
		script = script.replace("\n\r", "\n");
		script = script.replace("\r", "\n");
		return script;
	}


	/**
	 * <span class="lang-en">Removes all comments in the script</span>
	 * <span class="lang-ja">スクリプト内のコメントを全て除去します</span>
	 * .
	 * <span class="lang-en">
	 * Line feed code in the passed script to this method should be unified to LF (0x0A).
	 * </span>
	 * <span class="lang-ja">
	 * このメソッドに渡されるスクリプト内の改行コードは,
	 * あらかじめ LF (0x0A) に統一されている必要があります.
	 * </span>
	 *
	 * @param script
	 *   <span class="lang-en">The script to be processed.</span>
	 *   <span class="lang-ja">処理対象のスクリプト.</span>
	 *
	 * @return
	 *   <span class="lang-en">The script in which all comments have been removed.</span>
	 *   <span class="lang-ja">コメントが除去されたスクリプト.</span>
	 */
	private String removeAllComments(String script) {

		// Remove line-comments by using the replacement by the regular expression.
		// まず行コメントを正規表現で削除
		script = script.replaceAll( SCRIPT_WORD.lineCommentPrefix + ".*", "");

		// Caution: To prevent indicating wrong line numbers in error messages,
		// the position of each line should be kept in the processing of this method.
		// Therefore, we don't use the replacement by the regular expression
		// for removing all block-comments at once.

		// コンパイルエラーのメッセージに行番号を表示する都合上、
		// ブロックコメントは中の各行を削除した後に改行コードのみを残して
		// 行ずれを防ぐ必要があるため、正規表現での一括置換は都合が悪いので行わない

		StringBuilder codeBuilder = new StringBuilder(script);

		// Find the beginning of the first block comment.
		// 最初のブロックコメントの開始位置を取得
		int commentBegin = codeBuilder.indexOf(SCRIPT_WORD.blockCommentBegin);

		// Repeat removing a block comment by replacing each line in the block comment with a blank line.
		// ブロックコメントが残っている間、ブロックコメント内の各行を空行で置き換える処理をくり返す
		while (0 <= commentBegin) {

			// Find the end of the current block comment.
			// ブロックコメントの終了位置を取得
			int commentEnd = codeBuilder.indexOf(
					SCRIPT_WORD.blockCommentEnd, commentBegin + SCRIPT_WORD.blockCommentBegin.length()
			);
			// Offset to contain the block-comment-end token into the removing-range.
			// 上で取得できるのはブロックコメント終端トークンが出現する先頭位置なので、終端トークンの長さ分を補正
			commentEnd += SCRIPT_WORD.blockCommentEnd.length();

			// Extract the code in the block-comment (containing the beginning/end token of the block comment).
			// ブロックコメントで挟まれた中身を抜き出す（ブロックコメントの開始・終了文字列を含む）
			String commentInner = codeBuilder.substring(commentBegin, commentEnd);

			// Remove all characters of the extracted code excluding line feed characters (line-feeds).
			// 抜き出したブロックコメントの中身を、改行だけ残して全削除したものを用意
			String commentInnerLineFeeds = commentInner.replaceAll(".*", "");

			// Replace the block-comment with the line-feeds.
			// コード内のブロックコメントの区間を、改行のみ残して全削除した内容で置き換える
			codeBuilder.replace(commentBegin, commentEnd, commentInnerLineFeeds);

			// Go to the next block-comment.
			// 処理対象を次のブロックコメントに更新
			commentBegin = codeBuilder.indexOf(SCRIPT_WORD.blockCommentBegin);
		}

		script = codeBuilder.toString();
		return script;
	}
}
