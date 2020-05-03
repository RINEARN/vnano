/*
 * Copyright(C) 2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.LanguageSpecContainer;

public class ScriptLoader {

	/**
	 * <span class="lang-en">
	 * Stores language-specification settings
	 * </span>
	 * <span class="lang-ja">
	 * 各種の言語仕様設定類を格納するコンテナを保持します
	 * </span>
	 * .
	 */
	private final LanguageSpecContainer LANG_SPEC;

	/**
	 * <span class="lang-en">
	 * The prefix of comment lines in the list file
	 * </span>
	 * <span class="lang-ja">
	 * リストファイル内のコメント行のプレフィックスです
	 * </span>
	 * .
	 */
	private static final String LIST_FILE_COMMENT_LINE_HEAD = "#";

	/**
	 * <span class="lang-en">
	 * The prefix of the encoding-declaration line in script files
	 * </span>
	 * <span class="lang-ja">
	 * スクリプトファイル内の文字コード宣言行のプレフィックスです
	 * </span>
	 * .
	 */
	private static final String[] ENCODING_DECLARATION_LINE_HEAD = {
		"coding",
		"#coding",
		"encoding",
		"#encoding",
		"encode",
		"#encode",
	};

	/**
	 * <span class="lang-en">
	 * Stores the end-of-line code for concatenating lines of loaded scripts
	 * </span>
	 * <span class="lang-ja">
	 * スクリプトから読み込んだ行を結合するための改行コードを保持します
	 * </span>
	 * .
	 */
	private final String EOL;

	/**
	 * <span class="lang-en">
	 * Stores the default encoding of the list file and script files
	 * </span>
	 * <span class="lang-ja">
	 * リストファイルやスクリプトを読み込む際の, デフォルトの文字コードを保持します
	 * </span>
	 * .
	 */
	private final String DEFAULT_ENCODING;


	// Following fields are caches for skipping storage-accessings when file has not been updated.
	// 以下、ファイル無更新ならストレージアクセスをスキップして前回読み込み内容を流用するためのキャッシュ類

	private String mainScriptName = null;
	private String mainScriptPath = null;
	private String mainScriptContent = null;
	private long mainScriptLastMod = -1;

	private String libraryScriptListPath = null;
	private long libraryScriptListLastMod = -1;
	private List<String> libraryScriptNameList = null;
	private List<String> libraryScriptPathList = null;
	private List<String> libraryScriptContentList = null;
	private List<Long> libraryScriptLastModList = null;


	/**
	 * <span class="lang-en">
	 * Create new script loader under the settings of specified encoding and end-line-code
	 * </span>
	 * <span class="lang-ja">
	 * 指定されたデフォルト文字コードと改行コードの設定で、スクリプトローダを生成します
	 * </span>
	 * .
	 * @param defaultEncoding
	 *   <span class="lang-en">The default encoding (e.g.: "UTF-8")</span>
	 *   <span class="lang-ja">デフォルトの文字コード (例: "UTF-8")</span>
	 *
	 * @param langSpec
	 *   <span class="lang-en">language specification settings.</span>
	 *   <span class="lang-ja">言語仕様設定.</span>
	 */
	public ScriptLoader(String defaultEncoding, LanguageSpecContainer langSpec) {
		this.LANG_SPEC = langSpec;
		this.DEFAULT_ENCODING = defaultEncoding;
		this.EOL = System.getProperty("line.separator");
	}


	/**
	 * <span class="lang-en">
	 * Registers the path of the main script file to be loaded
	 * </span>
	 * <span class="lang-ja">
	 * 読み込むメインスクリプトファイルのパスを登録します
	 * </span>
	 * .
	 * @param defaultEncoding
	 *   <span class="lang-en">The path of the main script file</span>
	 *   <span class="lang-ja">メインスクリプトファイルのパス</span>
	 */
	public void setMainScriptPath (String scriptFilePath) {
		this.mainScriptPath = scriptFilePath;
	}


	/**
	 * <span class="lang-en">
	 * Registers the path of the library list file in which file paths of library scripts are described
	 * </span>
	 * <span class="lang-ja">
	 * 読み込むライブラリのファイルパスが記載された, ライブラリリストファイルのパスを登録します
	 * </span>
	 * .
	 * @param listFilePath
	 *   <span class="lang-en">The path of the library list file</span>
	 *   <span class="lang-ja">ライブラリリストファイルのパス</span>
	 */
	public void setLibraryScriptListPath(String listFilePath) {
		this.libraryScriptListPath = listFilePath;
		this.libraryScriptListLastMod = -1;
	}


	/**
	 * <span class="lang-en">
	 * Loads/updates scripts from registered files
	 * </span>
	 * <span class="lang-ja">
	 * 登録されたファイルから, スクリプトを読み込み/更新します
	 * </span>
	 * .
	 * @throws VnanoException
	 *   <span class="lang-en">Thrown when scripts could not be loaded successfully.</span>
	 *   <span class="lang-ja">スクリプトが正常に読み込めなかった際にスローされます</span>
	 */
	public void load() throws VnanoException {
		if (this.mainScriptPath != null) {
			this.loadMainScriptContent();
		}
		if (this.libraryScriptListPath != null) {
			this.loadLibraryScriptPaths();
		}
		if (this.libraryScriptPathList != null) {
			this.loadLibraryScriptContents();
		}
	}


	/**
	 * <span class="lang-en">
	 * Returns whether this loader has a loaded main script
	 * </span>
	 * <span class="lang-ja">
	 * このローダが, 読み込み済みのメインスクリプトを保持しているかどうかを判定します
	 * </span>
	 * .
	 * @return
	 *   <span class="lang-en">True if this loader has a main script.</span>
	 *   <span class="lang-ja">メインスクリプトを保持していた場合に true が返されます.</span>
	 */
	public boolean hasMainScript() {
		return this.mainScriptContent != null;
	}


	/**
	 * <span class="lang-en">
	 * Returns the name of the loaded main script
	 * </span>
	 * <span class="lang-ja">
	 * 読み込まれたメインスクリプトの名称を返します
	 * </span>
	 * .
	 * @return
	 *   <span class="lang-en">The name of the main script.</span>
	 *   <span class="lang-ja">メインスクリプトの名称.</span>
	 */
	public String getMainScriptName() {
		return this.mainScriptName;
	}


	/**
	 * <span class="lang-en">
	 * Returns the content of the loaded main script
	 * </span>
	 * <span class="lang-ja">
	 * 読み込まれたメインスクリプトの内容を返します
	 * </span>
	 * .
	 * @return
	 *   <span class="lang-en">The content of the main script.</span>
	 *   <span class="lang-ja">メインスクリプトの内容.</span>
	 */
	public String getMainScriptContent() {
		return this.mainScriptContent;
	}


	/**
	 * <span class="lang-en">
	 * Returns whether this loader has loaded library scripts
	 * </span>
	 * <span class="lang-ja">
	 * このローダが, 読み込み済みのライブラリスクリプトを保持しているかどうかを判定します
	 * </span>
	 * .
	 * @return
	 *   <span class="lang-en">True if this loader has library scripts.</span>
	 *   <span class="lang-ja">ライブラリスクリプトを保持していた場合に true が返されます.</span>
	 */
	public boolean hasLibraryScripts() {
		return this.libraryScriptContentList != null && this.libraryScriptContentList.size() != 0;
	}


	/**
	 * <span class="lang-en">
	 * Returns names of the loaded library scripts
	 * </span>
	 * <span class="lang-ja">
	 * 読み込まれたライブラリスクリプトの名称を返します
	 * </span>
	 * .
	 * @return
	 *   <span class="lang-en">Names of the library scripts.</span>
	 *   <span class="lang-ja">ライブラリスクリプトの名称.</span>
	 */
	public String[] getLibraryScriptNames() {
		return this.libraryScriptNameList.toArray(new String[0]);
	}


	/**
	 * <span class="lang-en">
	 * Returns contents of the loaded library scripts
	 * </span>
	 * <span class="lang-ja">
	 * 読み込まれたライブラリスクリプトの内容を返します
	 * </span>
	 * .
	 * @return
	 *   <span class="lang-en">Contents of the library scripts.</span>
	 *   <span class="lang-ja">ライブラリスクリプトの内容.</span>
	 */
	public String[] getLibraryScriptContents() {
		return this.libraryScriptContentList.toArray(new String[0]);
	}


	/**
	 * <span class="lang-en">
	 * Loads/updates paths of library script files from the content of the library list file
	 * </span>
	 * <span class="lang-ja">
	 * ライブラリリストファイルから, ライブラリスクリプトファイルのパスを読み込み（または更新し）ます
	 * </span>
	 * .
	 */
	private void loadLibraryScriptPaths() throws VnanoException {
		File listFile = new File(this.libraryScriptListPath);
		if (!listFile.exists()) {
			throw new VnanoException(ErrorType.LIBRARY_LIST_FILE_DOES_NOT_EXIST, this.libraryScriptListPath);
		}

		// 前回読み込み時から更新日時が変わっていなければ、内容も変わっていないと見なして、再読み込みせず終了
		if (listFile.lastModified() == this.libraryScriptListLastMod) {
			return;
		}

		// リストが変わったので、既存のリスト内容を控えているフィールドを一旦リセット
		this.libraryScriptNameList = new ArrayList<String>();
		this.libraryScriptPathList = new ArrayList<String>();
		this.libraryScriptContentList = new ArrayList<String>();
		this.libraryScriptLastModList = new ArrayList<Long>();

		// リストファイルのあるディレクトリを、ライブラリパスの基準ディレクトリとする
		File libDirectory = listFile.getParentFile();

		// リストファイルから、ライブラリのパス一覧を再読み込み（または初回読み込み）
		List<String> libPaths = null;
		try {
			libPaths = Files.readAllLines(Paths.get(this.libraryScriptListPath), Charset.forName(DEFAULT_ENCODING));
		} catch (IOException ioe) {
			throw new VnanoException(ErrorType.LIBRARY_LIST_FILE_IS_NOT_ACCESSIBLE, this.libraryScriptListPath, ioe);
		}

		// 読み込んだライブラリパスの一覧をフィールドに反映
		for (String libPath: libPaths) {

			// 空行やコメント行は無視
			if (libPath.trim().isEmpty() || libPath.trim().startsWith(LIST_FILE_COMMENT_LINE_HEAD)) {
				continue;
			}

			// リスト内に記載されたパスが相対パスの場合は、リストファイルのディレクトリ基準に変換
			File libFile = new File(libPath);
			if (!libFile.isAbsolute() ) {
				libFile = new File(libDirectory, libPath);
			}

			// スクリプト名の中で使用できない記号等を置き換えて正規化する
			String libraryName = LANG_SPEC.IDENTIFIER_SYNTAX.normalizeScriptIdentifier( libFile.getName() );

			// フィールドにライブラリファイルの情報を登録
			this.libraryScriptPathList.add(libFile.getPath());
			this.libraryScriptNameList.add(libraryName);
			this.libraryScriptContentList.add("");
			this.libraryScriptLastModList.add(-1l);
		}

		// 読み込みが正常に完了したら、ファイルの更新日時を控える（次回で不変なら読み込みスキップするため）
		this.libraryScriptListLastMod = listFile.lastModified();
	}


	/**
	 * <span class="lang-en">
	 * Loads/updates contents (code) of the main script from the path of the main script file
	 * </span>
	 * <span class="lang-ja">
	 * メインスクリプトファイルのパスから, コード内容を読み込み（または更新し）ます
	 * </span>
	 * .
	 */
	private void loadMainScriptContent() throws VnanoException {
		File scriptFile = new File(this.mainScriptPath);

		if (!scriptFile.exists()) {
			throw new VnanoException(ErrorType.SCRIPT_FILE_DOES_NOT_EXIST, this.mainScriptPath);
		}

		// 前回読み込み時から更新日時が変わっていなければ、内容も変わっていないと見なして読み込みスキップ
		if (scriptFile.lastModified() == this.mainScriptLastMod) {
			return;
		} else {
			this.mainScriptName = scriptFile.getName();
			try {
				this.mainScriptContent = this.loadScriptContent(this.mainScriptPath);
			} catch (IOException ioe) {
				this.mainScriptName = null;
				this.mainScriptContent = null;
				this.mainScriptLastMod = -1;
				throw new VnanoException(ErrorType.SCRIPT_FILE_IS_NOT_ACCESSIBLE, this.mainScriptPath, ioe);
			} catch (VnanoException vne) {
				this.mainScriptName = null;
				this.mainScriptContent = null;
				this.mainScriptLastMod = -1;
				throw vne;
			}
		}

		// 読み込みが正常に完了したら、ファイルの更新日時を控える（次回で不変なら読み込みスキップするため）
		this.mainScriptLastMod = scriptFile.lastModified();
	}


	/**
	 * <span class="lang-en">
	 * Loads/updates contents (code) of library scripts from paths of library script files
	 * </span>
	 * <span class="lang-ja">
	 * ライブラリスクリプトファイルのパスから, ライブラリのコード内容を読み込み（または更新し）ます
	 * </span>
	 * .
	 */
	private void loadLibraryScriptContents() throws VnanoException {

		// 読み込み処理に失敗したライブラリはここに追記していく（即例外を投げると後続プラグインを読み込めないため）
		String notExistLibraries = "";
		String loadFailedLibraries = "";
		Throwable loadFailedCause = null;

		// ライブラリを 1 個ずつ読んでいく
		int libN = this.libraryScriptPathList.size();
		for (int libIndex=0; libIndex<libN; libIndex++) {
			String libPath = this.libraryScriptPathList.get(libIndex);
			File libFile = new File(libPath);
			if (!libFile.exists()) {
				notExistLibraries += (!notExistLibraries.isEmpty() ? ", " : "") + libFile.getName();
				//throw new VnanoException(ErrorType.SCRIPT_FILE_DOES_NOT_EXIST, libPath);
			}

			// 前回読み込み時から更新日時が変わっていなければ、内容も変わっていないと見なして読み込みスキップ
			if (libFile.lastModified() == this.libraryScriptLastModList.get(libIndex)) {
				continue;
			}

			// ライブラリファイルの中身を読み込み、フィールドに登録
			String libContent = null;
			try {
				libContent = this.loadScriptContent(libPath);
			} catch (Exception e) {
				loadFailedLibraries += (!loadFailedLibraries.isEmpty() ? ", " : "") + libFile.getName();
				loadFailedCause = e;
			}
			this.libraryScriptContentList.set(libIndex, libContent);
			this.libraryScriptNameList.set(libIndex, libFile.getName());

			// 読み込みが正常に完了したら、ファイルの更新日時を控える（次回で不変なら読み込みスキップするため）
			this.libraryScriptLastModList.set(libIndex, libFile.lastModified());
		}

		// 読み込みに失敗したものは libraryScriptContentList の要素が null になっているので、それを目印にリストから削除する
		if (!notExistLibraries.isEmpty() || !loadFailedLibraries.isEmpty()) {
			List<String> succeededLibraryPathList = new ArrayList<String>();
			List<String> succeededLibraryNameList = new ArrayList<String>();
			List<String> succeededLibraryContentList = new ArrayList<String>();
			List<Long> succeededLibraryLastModList = new ArrayList<Long>();
			for (int libIndex=0; libIndex<libN; libIndex++) {
				if (this.libraryScriptContentList.get(libIndex) != null) {
					succeededLibraryPathList.add(this.libraryScriptPathList.get(libIndex));
					succeededLibraryNameList.add(this.libraryScriptNameList.get(libIndex));
					succeededLibraryContentList.add(this.libraryScriptContentList.get(libIndex));
					succeededLibraryLastModList.add(this.libraryScriptLastModList.get(libIndex));
				}
			}
			this.libraryScriptPathList = succeededLibraryPathList;
			this.libraryScriptNameList = succeededLibraryNameList;
			this.libraryScriptContentList = succeededLibraryContentList;
			this.libraryScriptLastModList = succeededLibraryLastModList;
		}

		// 読み込みに失敗したものが 1 つでもあれば例外を投げる
		if (!notExistLibraries.isEmpty()) {
			throw new VnanoException(ErrorType.SCRIPT_FILE_DOES_NOT_EXIST, notExistLibraries);
		}
		if (!loadFailedLibraries.isEmpty()) {
			throw new VnanoException(ErrorType.SCRIPT_FILE_IS_NOT_ACCESSIBLE, loadFailedLibraries, loadFailedCause);
		}
	}


	/**
	 * <span class="lang-en">
	 * Loads content of a script file
	 * </span>
	 * <span class="lang-ja">
	 * スクリプトファイルの内容を読み込みます
	 * </span>
	 * .
	 * @param scriptPath
	 *   <span class="lang-en">The path of the script file to be loaded</span>
	 *   <span class="lang-ja">読み込むスクリプトファイルのパス</span>
	 *
	 * @return
	 *   <span class="lang-en">The content of the loaded script file</span>
	 *   <span class="lang-ja">読み込まれたスクリプトファイルの内容</span>
	 */
	private String loadScriptContent(String scriptPath) throws IOException, VnanoException {
		// ファイルの存在は上層で確認済みの想定

		List<String> contentLines = null;

		// 先頭行で文字コードが宣言されていれば読む
		Charset charset = this.getDeclaredCharset(scriptPath);
		boolean charsetIsDeclared = (charset != null);

		// 文字コードに応じてライブラリの内容を読む
		if (charsetIsDeclared) {
			contentLines = Files.readAllLines(Paths.get(scriptPath), charset);
			contentLines.set(0, ""); // 文字コードが宣言されていた場合、宣言行を消しておく（スクリプトとしては解釈できない）
		} else {
			contentLines = Files.readAllLines(Paths.get(scriptPath), Charset.forName(DEFAULT_ENCODING));
		}

		// 読み込んだ内容をフィールドに登録
		String content = String.join(EOL, contentLines.toArray(new String[0]));
		return content;
	}


	/**
	 * <span class="lang-en">
	 * Gets the declared charset in the specified script file, if it is declared
	 * </span>
	 * <span class="lang-ja">
	 * 指定されたスクリプトファイル内に, 文字コードが宣言されていた場合, その文字コードを抽出して返します
	 * </span>
	 * .
	 * @param scriptPath
	 *   <span class="lang-en">The path of the script file to be loaded</span>
	 *   <span class="lang-ja">読み込むスクリプトファイルのパス</span>
	 *
	 * @return
	 *   <span class="lang-en">The declared charset (or, null if it is not declared)</span>
	 *   <span class="lang-ja">宣言されている文字コード (宣言されていない場合は null)</span>
	 */
	private Charset getDeclaredCharset(String scriptPath) throws IOException, VnanoException {

		// 先頭行に文字コード宣言があるかもしれないので、先頭行を読む
		FileReader fileReader = new FileReader(new File(scriptPath));
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String firstLine = bufferedReader.readLine();
		bufferedReader.close();
		fileReader.close();

		// 先頭行が無ければ明らかに何も宣言されていないので null を返す
		if (firstLine == null) {
			return null;
		}

		// 文字コード宣言があれば解釈して文字コード名を抽出
		firstLine = firstLine.replaceAll("\\s", "");
		firstLine = firstLine.replaceAll("\\t", "");
		firstLine = firstLine.replaceAll(";", "");
		String encodingName = null;
		for (String declLineHead: ENCODING_DECLARATION_LINE_HEAD) {
			if (firstLine.startsWith(declLineHead)) {
				encodingName = firstLine.substring(declLineHead.length(), firstLine.length());
			}
		}

		// 文字コードが宣言されていなければ null を返す
		if (encodingName == null) {
			return null;
		}

		// 文字コード名を Charset に変換して返す
		Charset charset = null;
		try {
			charset = Charset.forName(encodingName);
		} catch (IllegalCharsetNameException | UnsupportedCharsetException ce) {
			throw new VnanoException(
				ErrorType.DECLARED_ENCODING_IS_UNSUPPORTED, new String[] { encodingName, scriptPath }, ce
			);
		}
		return charset;
	}
}
