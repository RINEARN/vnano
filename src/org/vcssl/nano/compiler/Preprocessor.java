/*
 * Copyright(C) 2018-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import org.vcssl.nano.spec.ScriptWord;


/**
 * The class performing the function of the preprocessor in the compiler of the Vnano.
 * 
 * This preprocessor are used for removing comments in the script,
 * unifying line feed code, and so on as a first stage of the compilation.
 */
public class Preprocessor {

	/**
	 * Create a new preprocessor.
	 */
	public Preprocessor() {
	}


	/**
	 * Preprocess the script.
	 * 
	 * As the result of this preprocessing, comments in the script will be removed,
	 * and line feed code will be unified to LF (0x0A).
	 *
	 * @param script The script to be preprocessed.
	 * @return The preprocessed script.
	 */
	public String preprocess(String script) {

		// Unify line feed code to LF (0x0A).
		script = this.replaceEndOfLineCode(script);

		// Remove comments.
		script = this.removeAllComments(script);

		return script;
	}


	/**
	 * Unifies line feed code to LF (0x0A).
	 * 
	 * @param script The script to be processed.
	 * @return The script in which line feed code have unified.
	 */
	private String replaceEndOfLineCode(String script) {
		script = script.replace("\r\n", "\n");
		script = script.replace("\n\r", "\n");
		script = script.replace("\r", "\n");
		return script;
	}


	/**
	 * Removes all comments in the script.
	 * 
	 * Line feed code in the passed script to this method should be unified to LF (0x0A).
	 *
	 * @param script The script to be processed.
	 * @return The script in which all comments have been removed.
	 */
	private String removeAllComments(String script) {

		// Remove line-comments by using the replacement by the regular expression.
		script = script.replaceAll(ScriptWord.LINE_COMMENT_PREFIX + ".*", "");

		// Caution: To prevent indicating wrong line numbers in error messages,
		// the position of each line should be kept in the processing of this method.
		// Therefore, we don't use the replacement by the regular expression
		// for removing all block-comments at once.

		StringBuilder codeBuilder = new StringBuilder(script);

		// Find the beginning of the first block comment.
		int commentBegin = codeBuilder.indexOf(ScriptWord.BLOCK_COMMENT_BEGIN);

		// Repeat removing a block comment by replacing each line in the block comment with a blank line.
		while (0 <= commentBegin) {

			// Find the end of the current block comment.
			int commentEnd = codeBuilder.indexOf(
					ScriptWord.BLOCK_COMMENT_END, commentBegin + ScriptWord.BLOCK_COMMENT_BEGIN.length()
			);
			// Offset to contain the block-comment-end token into the removing-range.
			commentEnd += ScriptWord.BLOCK_COMMENT_END.length();

			// Extract the code in the block-comment (containing the beginning/end token of the block comment).
			String commentInner = codeBuilder.substring(commentBegin, commentEnd);

			// Remove all characters of the extracted code excluding line feed characters (line-feeds).
			String commentInnerLineFeeds = commentInner.replaceAll(".*", "");

			// Replace the block-comment with the line-feeds.
			codeBuilder.replace(commentBegin, commentEnd, commentInnerLineFeeds);

			// Go to the next block-comment.
			commentBegin = codeBuilder.indexOf(ScriptWord.BLOCK_COMMENT_BEGIN);
		}

		script = codeBuilder.toString();
		return script;
	}
}

