/*
 * Copyright(C) 2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import org.vcssl.nano.spec.ScriptWord;

/**
 * <p>
 * コンパイラ内において、コメント削除を含む前処理を行う、プリプロセッサのクラスです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class Preprocessor {

	/**
	 * ソースコードに対して前処理を実行し、結果を返します。
	 * この処理の結果として、コード内の全てのコメントが削除されます。
	 * また、コード内の改行コードは LF (0x0A) に統一されます。
	 *
	 * @param sourceCode ソースコードの文字列
	 * @return 処理結果
	 */
	public String preprocess(String sourceCode) {

		// 改行コードを LF (0x0A) に統一
		sourceCode = this.replaceEndOfLineCode(sourceCode);

		// コメントを削除
		sourceCode = this.removeAllComments(sourceCode);

		return sourceCode;
	}


	/**
	 * ソースコード内の改行コードを全て LF (0x0A) に統一します。
	 *
	 * @param sourceCode ソースコードの文字列
	 * @return 改行コードをLFに統一した文字列
	 */
	private String replaceEndOfLineCode(String sourceCode) {
		sourceCode = sourceCode.replace("\r\n", "\n");
		sourceCode = sourceCode.replace("\n\r", "\n");
		sourceCode = sourceCode.replace("\r", "\n");
		return sourceCode;
	}


	/**
	 * ソースコード内のコメントを全て削除します。
	 * ただし、ソースコード内の改行コードは、あらかじめ LF (0x0A) に統一されている必要があります。
	 *
	 * @param sourceCode ソースコードの文字列
	 * @return コメントを全て削除した文字列
	 */
	private String removeAllComments(String sourceCode) {

		// まず行コメントを正規表現で削除
		sourceCode = sourceCode.replaceAll( ScriptWord.LINE_COMMENT_PREFIX + ".*", "");

		// コンパイルエラーのメッセージに行番号を表示する都合上、
		// ブロックコメントは中の各行を削除した後に改行コードのみを残して
		// 行ずれを防ぐ必要があるため、正規表現での一括置換は都合が悪いので行わない

		StringBuilder codeBuilder = new StringBuilder(sourceCode);

		// ブロックコメントの開始位置を取得
		int commentBegin = codeBuilder.indexOf(ScriptWord.BLOCK_COMMENT_BEGIN);

		// 開始位置が0以上 = ブロックコメントが残っている間、空行置換処理をくり返す
		while (0 <= commentBegin) {

			// ブロックコメントの終了位置を取得
			int commentEnd = codeBuilder.indexOf(
				ScriptWord.BLOCK_COMMENT_END, commentBegin + ScriptWord.BLOCK_COMMENT_BEGIN.length()
			);
			// 上で取得できるのはブロックコメント終了文字列が出現する先頭位置なので、終了文字列の長さ分を補正
			commentEnd += ScriptWord.BLOCK_COMMENT_END.length();

			// ブロックコメントで挟まれた中身を抜き出す（ブロックコメントの開始・終了文字列を含む）
			String commentInner = codeBuilder.substring(commentBegin, commentEnd);

			// 抜き出したブロックコメントの中身を、改行だけ残して全削除したものを用意
			String commentInnerLineFeeds = commentInner.replaceAll(".*", "");

			// コード内のブロックコメントの区間を、改行のみ残して全削除した内容で置き換える
			codeBuilder.replace(commentBegin, commentEnd, commentInnerLineFeeds);

			// ブロックコメントの開始位置を更新
			commentBegin = codeBuilder.indexOf(ScriptWord.BLOCK_COMMENT_BEGIN);
		}

		sourceCode = codeBuilder.toString();
		return sourceCode;
	}

}
