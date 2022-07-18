/*
 * Copyright(C) 2020-2022 RINEARN
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
 * The class to perform loading of plugins from class files.
 * 
 * Paths of class files to be loaded can be specified by a text file: "plugin list file".
 * In the plugin list file, describe a path of a class file for each line.
 * Lines starts with "#" will be regarded as comment lines. Empty lines are also ignored.
 *
 * The plugin list file is specified by
 * {@link PluginLoader#setPluginListPath(String) setPluginListPath(String) } method,
 * and plugin class files will be loaded by
 * {@link PluginLoader#load() } method.
 */
public class PluginLoader {

	/** The prefix of comment lines in the list file. */
	private static final String LIST_FILE_COMMENT_LINE_HEAD = "#";

	/** The default encoding of list files. */
	private final String DEFAULT_ENCODING;


	/** The file path of the plugin list file. */
	private String pluginListPath = null;

	/** The last-modified time of the plugin list file. */
	private long pluginListLastMod = -1;

	/** The list of names of plugins. */
	private List<String> pluginNameList = null;

	/** The list of file paths of plugins. */
	private List<String> pluginFilePathList = null;

	/** The list of class paths of plugins. */
	private List<String> pluginClassPathList = null;

	/** The list of instances of plugins. */
	private List<Object> pluginInstanceList = null;

	/** The list of last-modified time of plugins. */
	private List<Long> pluginLastModList = null;

	/** The base directory of plugins. */
	private File pluginDirectory = null;


	/**
	 * Create new script loader under the settings of specified encoding.
	 * 
	 * @param encoding The encoding of the list file ("UTF-8" and so on).
	 */
	public PluginLoader(String encoding) {
		this.DEFAULT_ENCODING = encoding;
	}


	/**
	 * Registers the path of the plugin list file in which file paths of plugins are described.
	 * 
	 * @param listFilePath The path of the plugin list file.
	 */
	public void setPluginListPath(String listFilePath) {
		this.pluginListPath = listFilePath;
		this.pluginListLastMod = -1;
	}


	/**
	 * Loads/updates scripts from registered files.
	 * 
	 * @throws VnanoException Thrown when plugins could not be loaded successfully.
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
	 * Returns whether this loader has loaded plugins.
	 * 
	 * @return True if this loader has plugins.
	 */
	public boolean hasPlugins() {
		return this.pluginInstanceList != null && this.pluginInstanceList.size() != 0;
	}


	/**
	 * Returns names of the loaded plugins.
	 * 
	 * @return Names of the plugins.
	 */
	public String[] getPluginNames() {
		return this.pluginNameList.toArray(new String[0]);
	}


	/**
	 * Returns instances of the loaded plugins.
	 * 
	 * @return Instances of plugins.
	 */
	public Object[] getPluginInstances() {
		return this.pluginInstanceList.toArray(new Object[0]);
	}


	/**
	 * Loads/updates paths of plugin class files from the content of the plugin list file.
	 */
	private void loadPluginPaths() throws VnanoException {
		File listFile = new File(this.pluginListPath);
		if (!listFile.exists()) {
			throw new VnanoException(ErrorType.PLUGIN_LIST_FILE_DOES_NOT_EXIST, this.pluginListPath);
		}

		// If the content of the plugin list file has not been modified, skip reloading.
		if (listFile.lastModified() == this.pluginListLastMod) {
			return;
		}

		// Clear lists.
		this.pluginNameList = new ArrayList<String>();
		this.pluginFilePathList = new ArrayList<String>();
		this.pluginClassPathList = new ArrayList<String>();
		this.pluginInstanceList = new ArrayList<Object>();
		this.pluginLastModList = new ArrayList<Long>();

		// Set the directory in which the plugin list file is located, to the base directory of plugins.
		this.pluginDirectory = listFile.getParentFile();

		// Read file paths of plugins from the plugin list file.
		String listFileContent = MetaQualifiedFileLoader.load(this.pluginListPath, DEFAULT_ENCODING);
		String[] pluginPaths = listFileContent.split("\\n"); // Note: line-feed-code in the loaded content is normalized to LF (\n).

		// Analyze file paths of plugins, and set results to fields (Lists) of this class.
		for (String pluginPath: pluginPaths) {

			// Ignore empty/comment lines.
			if (pluginPath.trim().isEmpty() || pluginPath.trim().startsWith(LIST_FILE_COMMENT_LINE_HEAD)) {
				continue;
			}

			// Prepare the class path of the plugin from its file path.
			String pluginClassPath = pluginPath;
			if (pluginClassPath.endsWith(".class")) {
				pluginClassPath = pluginClassPath.substring(0, pluginClassPath.length() - 6);
			}
			if (pluginClassPath.startsWith("./") || pluginClassPath.startsWith(".\\")) {
				pluginClassPath = pluginClassPath.substring(2, pluginClassPath.length());
			}
			pluginClassPath = pluginClassPath.replace('/', '.');
			pluginClassPath = pluginClassPath.replace('\\', '.');

			// The file path of the plugin is described as the relative file path 
			// from the directory in which the plugin list file is located, so then convert it to the absolute path.
			File pluginFile = new File(pluginPath);
			if (!pluginFile.isAbsolute() ) {
				pluginFile = new File(this.pluginDirectory, pluginPath);
			}
			String pluginFilePath = pluginFile.getPath();

			// Extract the name of the plugin.
			String pluginName = pluginFile.getName();
			if (pluginName.endsWith(".class")) {
				pluginName = pluginName.substring(0, pluginName.length() - 6);
			}

			// Normalize the name of the plugin (may contain non-available characters).
			pluginName = IdentifierSyntax.normalizeScriptIdentifier(pluginName);

			// Register above analyzed results to fields (Lists) of this class.
			this.pluginFilePathList.add(pluginFilePath);
			this.pluginClassPathList.add(pluginClassPath);
			this.pluginNameList.add(pluginName);
			this.pluginInstanceList.add(null);
			this.pluginLastModList.add(-1l);
		}

		// Stores the last-modified time of the plugin list file, if it has no error.
		this.pluginListLastMod = listFile.lastModified();
	}



	/**
	 * Loads/updates class files of plugins from paths of them, and instantiate them.
	 */
	private void loadPlugins() throws VnanoException {

		int pluginN = this.pluginFilePathList.size();

		// Create a class loader, to load classes of plugins.
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

		// If any error occurred when loading a plugin, skip loading it and go to the next plugin.
		// So store error information to the following variables, and throw it after when all plugin have loaded.
		String notExistPlugis = "";
		String initFailedPlugis = "";
		Throwable initFailedCause = null;
		boolean[] isLoadingFailed = new boolean[pluginN];
		Arrays.fill(isLoadingFailed, false);

		// Load each plugin:
		for (int pluginIndex=0; pluginIndex<pluginN; pluginIndex++) {

			// Get the file of the plugin.
			String pluginPath = this.pluginFilePathList.get(pluginIndex);
			File pluginFile = new File(pluginPath);
			if (!pluginFile.exists()) {
				notExistPlugis += (!notExistPlugis.isEmpty() ? ", " : "") + pluginFile.getName();
				isLoadingFailed[pluginIndex] = true;
				continue;
			}

			// If the (class) file has not been modified, skip loading it.
			if (pluginFile.lastModified() == this.pluginLastModList.get(pluginIndex)) {
				continue;
			}

			// Load the class from the file, and instantiate it.
			ConnectorImplementationContainer pluginContainer;
			String pluginClassPath = this.pluginClassPathList.get(pluginIndex);
			try {
				pluginContainer = loader.load(pluginClassPath);
			} catch (ConnectorException e) {
				initFailedPlugis += (!initFailedPlugis.isEmpty() ? ", " : "") + pluginFile.getName();
				initFailedCause = e;
				isLoadingFailed[pluginIndex] = true;
				continue;
			}
			Object pluginInstance = pluginContainer.getConnectorImplementation();
			this.pluginInstanceList.set(pluginIndex, pluginInstance);
			this.pluginNameList.set(pluginIndex, pluginFile.getName());

			// If the plugin has been instantiated successfully, store the last modified time of the plugin (class) file.
			this.pluginLastModList.set(pluginIndex, pluginFile.lastModified());
		}

		// Remove failed plugins from lists.
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

		// Throw an exception if any plugin could not be loaded/instantiated successfully.
		if (!notExistPlugis.isEmpty()) {
			throw new VnanoException(ErrorType.PLUGIN_FILE_DOES_NOT_EXIST, notExistPlugis);
		}
		if (!notExistPlugis.isEmpty()) {
			throw new VnanoException(ErrorType.PLUGIN_INITIALIZATION_FAILED, notExistPlugis, initFailedCause);
		}
	}

}

