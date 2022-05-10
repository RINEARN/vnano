/*
 * Copyright(C) 2020-2021 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.vcssl.connect.ConnectorException;
import org.vcssl.connect.ConnectorImplementationContainer;
import org.vcssl.connect.ConnectorImplementationLoader;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.IdentifierSyntax;


/**
 * <span class="lang-en">
 * The class to perform loading of plug-ins from class files
 * </span>
 * <span class="lang-ja">
 * クラスファイルからプラグインを読み込むためのローダです
 * </span>
 * .
 * <span class="lang-en">
 * Paths of class files to be loaded can be specified by a text file: "plugin list file".
 * In the plugin list file, describe a path of a class file for each line.
 * Lines starts with "#" will be regarded as comment lines. Empty lines are also ignored.
 * </span>
 *
 * <span class="lang-ja">
 * 読み込むクラスファイルは, 一覧をテキストファイルに記述して指定します.
 * そのテキストファイルの事を「プラグインリストファイル」と呼びます.
 * プラグインリストファイル内には, 1行につき1個のクラスファイルのパスを記述してください.
 * 「 # 」で始まる行はコメント行として読み飛ばされます. 空白行も読み飛ばされます.
 * </span>
 *
 * <span class="lang-en">
 * The plugin list file is specified by
 * {@link PluginLoader#setPluginListPath(String) setPluginListPath(String) } method,
 * and plugin class files will be loaded by
 * {@link PluginLoader#load() } method.
 * </span>
 *
 * <span class="lang-en">
 * プラグインリストファイルの指定は
 * {@link PluginLoader#setPluginListPath(String) setPluginListPath(String) } メソッドによって行い,
 * その後のプラグインクラスファイルの読み込みは
 * {@link PluginLoader#load() } メソッドによって行います.
 * </span>
 */
public class PluginLoader {

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
	 * Stores the default encoding of list files
	 * </span>
	 * <span class="lang-ja">
	 * リストファイルを読み込む際の, デフォルトの文字コードを保持します
	 * </span>
	 * .
	 */
	private final String DEFAULT_ENCODING;


	// Following fields are caches for skipping storage-accessings when file has not been updated.
	// 以下、ファイル無更新ならストレージアクセスをスキップして前回読み込み内容を流用するためのキャッシュ類

	private String pluginListPath = null;
	private long pluginListLastMod = -1;
	private List<String> pluginNameList = null;
	private List<String> pluginFilePathList = null;
	private List<String> pluginClassPathList = null;
	private List<Object> pluginInstanceList = null;
	private List<Long> pluginLastModList = null;

	private File pluginDirectory = null;


	/**
	 * <span class="lang-en">
	 * Create new script loader under the settings of specified encoding
	 * </span>
	 * <span class="lang-ja">
	 * 指定された文字コードの設定で、スクリプトローダを生成します
	 * </span>
	 * .
	 * @param encoding
	 *   <span class="lang-en">The encoding of the list file (e.g.: "UTF-8")</span>
	 *   <span class="lang-ja">リストファイルの読み込みに用いる文字コード (例: "UTF-8")</span>
	 */
	public PluginLoader(String encoding) {
		this.DEFAULT_ENCODING = encoding;
	}


	/**
	 * <span class="lang-en">
	 * Registers the path of the plugin list file in which file paths of plugins are described
	 * </span>
	 * <span class="lang-ja">
	 * 読み込むプラグインのファイルパスが記載された, プラグインリストファイルのパスを登録します
	 * </span>
	 * .
	 * @param listFilePath
	 *   <span class="lang-en">The path of the plugin list file</span>
	 *   <span class="lang-ja">プラグインリストファイルのパス</span>
	 */
	public void setPluginListPath(String listFilePath) {
		this.pluginListPath = listFilePath;
		this.pluginListLastMod = -1;
	}


	/**
	 * <span class="lang-en">
	 * Loads/updates scripts from registered files
	 * </span>
	 * <span class="lang-ja">
	 * 登録されたファイルから, プラグインを読み込み/更新します
	 * </span>
	 * .
	 * @throws VnanoException
	 *   <span class="lang-en">Thrown when plugins could not be loaded successfully.</span>
	 *   <span class="lang-ja">プラグインが正常に読み込めなかった際にスローされます</span>
	 */
	public void load() throws VnanoException {
		if (this.pluginListPath != null) {
			this.loadPluginPaths();
		}
		if (this.pluginFilePathList != null) {
			this.loadPlugins();
		}
	}


	/**
	 * <span class="lang-en">
	 * Returns whether this loader has loaded plugins
	 * </span>
	 * <span class="lang-ja">
	 * このローダが, 読み込み済みのプラグインを保持しているかどうかを判定します
	 * </span>
	 * .
	 * @return
	 *   <span class="lang-en">True if this loader has plugins.</span>
	 *   <span class="lang-ja">プラグインを保持していた場合に true が返されます.</span>
	 */
	public boolean hasPlugins() {
		return this.pluginInstanceList != null && this.pluginInstanceList.size() != 0;
	}


	/**
	 * <span class="lang-en">
	 * Returns names of the loaded plugins
	 * </span>
	 * <span class="lang-ja">
	 * 読み込まれたプラグインの名称を返します
	 * </span>
	 * .
	 * @return
	 *   <span class="lang-en">Names of the plugins.</span>
	 *   <span class="lang-ja">プラグインの名称.</span>
	 */
	public String[] getPluginNames() {
		return this.pluginNameList.toArray(new String[0]);
	}


	/**
	 * <span class="lang-en">
	 * Returns instances of the loaded plugins
	 * </span>
	 * <span class="lang-ja">
	 * 読み込まれたプラグインのインスタンスを返します
	 * </span>
	 * .
	 * @return
	 *   <span class="lang-en">Instances of plugins.</span>
	 *   <span class="lang-ja">プラグインのインスタンス.</span>
	 */
	public Object[] getPluginInstances() {
		return this.pluginInstanceList.toArray(new Object[0]);
	}


	/**
	 * <span class="lang-en">
	 * Loads/updates paths of plugin class files from the content of the plugin list file
	 * </span>
	 * <span class="lang-ja">
	 * プラグインリストファイルから, プラグインクラスファイルのパスを読み込み（または更新し）ます
	 * </span>
	 * .
	 */
	private void loadPluginPaths() throws VnanoException {
		File listFile = new File(this.pluginListPath);
		if (!listFile.exists()) {
			throw new VnanoException(ErrorType.PLUGIN_LIST_FILE_DOES_NOT_EXIST, this.pluginListPath);
		}

		// 前回読み込み時から更新日時が変わっていなければ、内容も変わっていないと見なして、再読み込みせず終了
		if (listFile.lastModified() == this.pluginListLastMod) {
			return;
		}

		// リストが変わったので、既存のリスト内容を控えているフィールドを一旦リセット
		this.pluginNameList = new ArrayList<String>();
		this.pluginFilePathList = new ArrayList<String>();
		this.pluginClassPathList = new ArrayList<String>();
		this.pluginInstanceList = new ArrayList<Object>();
		this.pluginLastModList = new ArrayList<Long>();

		// リストファイルのあるディレクトリを、クラスパスの基準ディレクトリとする
		this.pluginDirectory = listFile.getParentFile();

		// リストファイルから、プラグインのクラスパス一覧を再読み込み（または初回読み込み）
		String listFileContent = MetaQualifiedFileLoader.load(this.pluginListPath, DEFAULT_ENCODING);
		String[] pluginPaths = listFileContent.split("\\n"); // 上記の load で読んだ内容は、改行コードがLF (\n) に正規化済み

		// 読み込んだクラスパスの一覧をフィールドに反映
		for (String pluginPath: pluginPaths) {

			// 空行やコメント行は無視
			if (pluginPath.trim().isEmpty() || pluginPath.trim().startsWith(LIST_FILE_COMMENT_LINE_HEAD)) {
				continue;
			}

			// ファイルパスからクラスパスを用意（クラスパスはリストファイルのディレクトリ基準にしなくてもいい）
			String pluginClassPath = pluginPath;
			if (pluginClassPath.endsWith(".class")) {
				pluginClassPath = pluginClassPath.substring(0, pluginClassPath.length() - 6);
			}
			if (pluginClassPath.startsWith("./") || pluginClassPath.startsWith(".\\")) {
				pluginClassPath = pluginClassPath.substring(2, pluginClassPath.length());
			}
			pluginClassPath = pluginClassPath.replace('/', '.');
			pluginClassPath = pluginClassPath.replace('\\', '.');

			// ファイルパスについては、リスト内に相対パスで記載されている場合、リストファイルのディレクトリ基準に変換
			File pluginFile = new File(pluginPath);
			if (!pluginFile.isAbsolute() ) {
				pluginFile = new File(this.pluginDirectory, pluginPath);
			}
			String pluginFilePath = pluginFile.getPath();

			// プラグイン名を抽出（ファイル名から拡張子を除いた部分）
			String pluginName = pluginFile.getName();
			if (pluginName.endsWith(".class")) {
				pluginName = pluginName.substring(0, pluginName.length() - 6);
			}

			// プラグイン名の中で使用できない記号等を、スクリプト名と同じ名前で正規化する
			pluginName = IdentifierSyntax.normalizeScriptIdentifier(pluginName);

			// フィールドにライブラリファイルの情報を登録
			this.pluginFilePathList.add(pluginFilePath);
			this.pluginClassPathList.add(pluginClassPath);
			this.pluginNameList.add(pluginName);
			this.pluginInstanceList.add(null);
			this.pluginLastModList.add(-1l);
		}

		// 読み込みが正常に完了したら、ファイルの更新日時を控える（次回で不変なら読み込みスキップするため）
		this.pluginListLastMod = listFile.lastModified();
	}



	/**
	 * <span class="lang-en">
	 * Loads/updates class files of plugins from paths of them, and instantiate them
	 * </span>
	 * <span class="lang-ja">
	 * プラグインクラスファイルのパスから, プラグインを読み込んで（または更新して）インスタンス化します
	 * </span>
	 * .
	 */
	private void loadPlugins() throws VnanoException {

		int pluginN = this.pluginFilePathList.size();

		// プラグインのクラスローダ周りを生成
		URL pluginDirURL;
		try {
			pluginDirURL = this.pluginDirectory.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new VnanoException(
				ErrorType.PLUGIN_DIRECTORY_IS_NOT_ACCESSIBLE, this.pluginDirectory.getPath(), e
			);
		}
		URLClassLoader classLoader = new URLClassLoader(new URL[] { pluginDirURL });
		ConnectorImplementationLoader loader = new ConnectorImplementationLoader(classLoader);

		// 後続の読み込み処理に失敗したプラグインはここに追記していく（即例外を投げると後続プラグインを読み込めないため）
		String notExistPlugis = "";
		String initFailedPlugis = "";
		Throwable initFailedCause = null;
		boolean[] isLoadingFailed = new boolean[pluginN];
		Arrays.fill(isLoadingFailed, false);

		// プラグインを一個ずつ読んでいく
		for (int pluginIndex=0; pluginIndex<pluginN; pluginIndex++) {
			String pluginPath = this.pluginFilePathList.get(pluginIndex);
			File pluginFile = new File(pluginPath);
			if (!pluginFile.exists()) {
				//throw new VnanoException(ErrorType.PLUGIN_FILE_DOES_NOT_EXIST, pluginPath);
				notExistPlugis += (!notExistPlugis.isEmpty() ? ", " : "") + pluginFile.getName();
				isLoadingFailed[pluginIndex] = true;
				continue;
			}

			// 前回読み込み時から更新日時が変わっていなければ、内容も変わっていないと見なして読み込みスキップ
			if (pluginFile.lastModified() == this.pluginLastModList.get(pluginIndex)) {
				continue;
			}

			// プラグインクラスファイルを読み込んでインスタンス化し、フィールドに登録
			ConnectorImplementationContainer pluginContainer;
			String pluginClassPath = this.pluginClassPathList.get(pluginIndex);
			try {
				pluginContainer = loader.load(pluginClassPath);
			} catch (ConnectorException e) {
				//throw new VnanoException(ErrorType.PLUGIN_INITIALIZATION_FAILED, pluginClassPath, e);
				initFailedPlugis += (!initFailedPlugis.isEmpty() ? ", " : "") + pluginFile.getName();
				initFailedCause = e;
				isLoadingFailed[pluginIndex] = true;
				continue;
			}
			Object pluginInstance = pluginContainer.getConnectorImplementation();
			this.pluginInstanceList.set(pluginIndex, pluginInstance);
			this.pluginNameList.set(pluginIndex, pluginFile.getName());

			// 読み込みが正常に完了したら、ファイルの更新日時を控える（次回で不変なら読み込みスキップするため）
			this.pluginLastModList.set(pluginIndex, pluginFile.lastModified());
		}

		// 読み込みに失敗したものはリストから削除する
		if (!notExistPlugis.isEmpty() || !notExistPlugis.isEmpty()) {
			List<String> succeededPluginFilePathList = new ArrayList<String>();
			List<String> succeededPluginClassPathList = new ArrayList<String>();
			List<String> succeededPluginNameList = new ArrayList<String>();
			List<Object> succeededPluginInstanceList = new ArrayList<Object>();
			List<Long> succeededPluginLastModList = new ArrayList<Long>();
			for (int pluginIndex=0; pluginIndex<pluginN; pluginIndex++) {
				if (isLoadingFailed[pluginIndex]) {
					succeededPluginFilePathList.add(this.pluginFilePathList.get(pluginIndex));
					succeededPluginClassPathList.add(this.pluginClassPathList.get(pluginIndex));
					succeededPluginNameList.add(this.pluginNameList.get(pluginIndex));
					succeededPluginInstanceList.add(this.pluginInstanceList.get(pluginIndex));
					succeededPluginLastModList.add(this.pluginLastModList.get(pluginIndex));
				}
			}
			this.pluginFilePathList = succeededPluginFilePathList;
			this.pluginClassPathList = succeededPluginClassPathList;
			this.pluginNameList = succeededPluginNameList;
			this.pluginInstanceList = succeededPluginInstanceList;
			this.pluginLastModList = succeededPluginLastModList;
		}

		// 読み込みに失敗したものが 1 つでもあれば例外を投げる
		if (!notExistPlugis.isEmpty()) {
			throw new VnanoException(ErrorType.PLUGIN_FILE_DOES_NOT_EXIST, notExistPlugis);
		}
		if (!notExistPlugis.isEmpty()) {
			throw new VnanoException(ErrorType.PLUGIN_INITIALIZATION_FAILED, notExistPlugis, initFailedCause);
		}
	}

}
