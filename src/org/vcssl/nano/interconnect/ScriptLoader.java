/*
 * Copyright(C) 2020-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.interconnect;

import java.util.List;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.IdentifierSyntax;


/**
 * The class to perform loading of scripts from text files.
 * 
 * How to register scripts to this loader depends on whether it is a "main script" or "library scripts".
 * The path of the main script file is simply specified by
 * {@link ScriptLoader#setMainScriptPath(String) setMainScriptPath(String) } method.
 * On the other hand, paths of library script files are specified by describing them in a text file,
 * which is referred as "library list file".
 * The path of the library list file is specified by
 * {@link ScriptLoader#setLibraryListPath(String) setLibraryListPath(String) } method.
 * In the library list file, describe a path of a library script file for each line.
 * Lines starts with "#" will be regarded as comment lines. Empty lines are also ignored.
 *
 * After registering scripts to be loaded, they will be loaded by {@link ScriptLoader#load() } method.
 */
public class ScriptLoader {

	/** The prefix of comment lines in the list file. */
	private static final String LIST_FILE_COMMENT_LINE_HEAD = "#";

	/** The default encoding of the list file and script files. */
	private final String DEFAULT_ENCODING;


	/** The name of the main script. */
	private String mainScriptName = null;

	/** The file path of the main script. */
	private String mainScriptPath = null;

	/** The content of the main script. */
	private String mainScriptContent = null;

	/** The last modified time of the main script. */
	private long mainScriptLastMod = -1;


	/** The file path of the library script list file. */
	private String libraryScriptListPath = null;

	/** The last modified time of the library list file. */
	private long libraryScriptListLastMod = -1;

	/** The list of the names of the library scripts. */
	private List<String> libraryScriptNameList = null;

	/** The list of the file paths of the library scripts. */
	private List<String> libraryScriptPathList = null;

	/** The list of the file paths of the library scripts, . */
	private List<String> libraryScriptRawPathList = null;

	/** The list of the contents of the library scripts. */
	private List<String> libraryScriptContentList = null;

	/** The list of last modified time of the library scripts. */
	private List<Long> libraryScriptLastModList = null;


	/**
	 * Create new script loader under the settings of specified encoding and end-line-code.
	 * 
	 * @param defaultEncoding The default encoding ("UTF-8" and so on).
	 */
	public ScriptLoader(String defaultEncoding) {
		this.DEFAULT_ENCODING = defaultEncoding;
	}


	/**
	 * Registers the path of the main script file to be loaded.
	 * 
	 * @param defaultEncoding The path of the main script file.
	 */
	public void setMainScriptPath(String scriptFilePath) {
		this.mainScriptPath = scriptFilePath;
	}


	/**
	 * Registers the path of the library list file in which file paths of library scripts are described.
	 * 
	 * @param listFilePath The path of the library list file.
	 */
	public void setLibraryScriptListPath(String listFilePath) {
		this.libraryScriptListPath = listFilePath;
		this.libraryScriptListLastMod = -1;
	}


	/**
	 * Loads/updates scripts from registered files.
	 * 
	 * @throws VnanoException Thrown when scripts could not be loaded successfully.
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
	 * Returns whether this loader has a loaded main script.
	 * 
	 * @return True if this loader has a main script.
	 */
	public boolean hasMainScript() {
		return this.mainScriptContent != null;
	}


	/**
	 * Returns the name of the loaded main script.
	 * 
	 * @return The name of the main script.
	 */
	public String getMainScriptName() {
		return this.mainScriptName;
	}


	/**
	 * Returns the file path of the loaded main script.
	 * 
	 * @return The file path of the main script.
	 */
	public String getMainScriptPath() {
		return this.mainScriptPath;
	}


	/**
	 * Returns the content of the loaded main script.
	 * 
	 * @return The content of the main script.
	 */
	public String getMainScriptContent() {
		return this.mainScriptContent;
	}


	/**
	 * Returns whether this loader has loaded library scripts.
	 * 
	 * @return True if this loader has library scripts.
	 */
	public boolean hasLibraryScripts() {
		return this.libraryScriptContentList != null && this.libraryScriptContentList.size() != 0;
	}


	/**
	 * Returns names of the loaded library scripts.
	 * 
	 * @return Names of the library scripts.
	 */
	public String[] getLibraryScriptNames() {
		return this.libraryScriptNameList.toArray(new String[0]);
	}


	/**
	 * Returns file paths of the loaded library scripts.
	 * 
	 * @return File paths of the library scripts.
	 */
	public String[] getLibraryScriptPaths() {
		return this.libraryScriptPathList.toArray(new String[0]);
	}


	/**
	 * Returns contents of the loaded library scripts.
	 * 
	 * @return Contents of the library scripts.
	 */
	public String[] getLibraryScriptContents() {
		return this.libraryScriptContentList.toArray(new String[0]);
	}


	/**
	 * Loads/updates paths of library script files from the content of the library list file.
	 */
	private void loadLibraryScriptPaths() throws VnanoException {
		File listFile = new File(this.libraryScriptListPath);
		if (!listFile.exists()) {
			throw new VnanoException(ErrorType.LIBRARY_LIST_FILE_DOES_NOT_EXIST, this.libraryScriptListPath);
		}

		// If the content of the library list file has not been modified, skip reloading.
		if (listFile.lastModified() == this.libraryScriptListLastMod) {
			return;
		}

		// Clear lists.
		this.libraryScriptNameList = new ArrayList<String>();
		this.libraryScriptPathList = new ArrayList<String>();
		this.libraryScriptContentList = new ArrayList<String>();
		this.libraryScriptLastModList = new ArrayList<Long>();

		// Set the directory in which the library list file is located, to the base directory of libraries.
		File libDirectory = listFile.getParentFile();

		// Read file paths of libraries from the library list file.
		String listFileContent = MetaQualifiedFileLoader.load(this.libraryScriptListPath, DEFAULT_ENCODING);
		String[] libPaths = listFileContent.split("\\n"); // Note: line-feed-code in the loaded content is normalized to LF (\n).

		// Analyze file paths of libraries, and set results to fields (Lists) of this class.
		for (String libPath: libPaths) {

			// Ignore empty/comment lines.
			if (libPath.trim().isEmpty() || libPath.trim().startsWith(LIST_FILE_COMMENT_LINE_HEAD)) {
				continue;
			}

			// The file path of the library may be described as the relative file path 
			// from the directory in which the library list file is located, so then convert it to the absolute path.
			File libFile = new File(libPath);
			if (!libFile.isAbsolute() ) {
				libFile = new File(libDirectory, libPath);
			}

			// Normalize the name of the library (may contain non-available characters).
			String libraryName = IdentifierSyntax.normalizeScriptIdentifier( libFile.getName() );

			// Register above analyzed results to fields (Lists) of this class.
			this.libraryScriptPathList.add(libFile.getPath());
			this.libraryScriptNameList.add(libraryName);
			this.libraryScriptContentList.add("");
			this.libraryScriptLastModList.add(-1l);
		}

		// Stores the last-modified time of the library list file, when it has no error.
		this.libraryScriptListLastMod = listFile.lastModified();
	}


	/**
	 * Loads/updates contents (code) of the main script from the path of the main script file.
	 */
	private void loadMainScriptContent() throws VnanoException {
		File scriptFile = new File(this.mainScriptPath);

		if (!scriptFile.exists()) {
			throw new VnanoException(ErrorType.SCRIPT_FILE_DOES_NOT_EXIST, this.mainScriptPath);
		}

		// If the content of the library has not been modified, skip reloading.
		if (scriptFile.lastModified() == this.mainScriptLastMod) {
			return;

		// Load/reload the library.
		} else {
			this.mainScriptName = scriptFile.getName();
			try {
				this.mainScriptContent = MetaQualifiedFileLoader.load(this.mainScriptPath, DEFAULT_ENCODING);
			} catch (VnanoException vne) {
				this.mainScriptName = null;
				this.mainScriptContent = null;
				this.mainScriptLastMod = -1;
				throw vne;
			}
		}

		// Stores the last-modified time of the library, if it has no error.
		this.mainScriptLastMod = scriptFile.lastModified();
	}


	/**
	 * Loads/updates contents (code) of library scripts from paths of library script files.
	 */
	private void loadLibraryScriptContents() throws VnanoException {

		int libN = this.libraryScriptPathList.size();

		// If any error occurred when loading a library, skip loading it and go to the next library.
		// So store error information to the following variables, and throw it after when all libraries have loaded.
		String notExistLibraries = "";
		String loadingFailedLibraries = "";
		Throwable loadingFailedCause = null;
		boolean[] isLaodingFailed = new boolean[libN];
		Arrays.fill(isLaodingFailed, false);

		// Load each library:
		for (int libIndex=0; libIndex<libN; libIndex++) {

			// Get the file of the library.
			String libPath = this.libraryScriptPathList.get(libIndex);
			File libFile = new File(libPath);
			if (!libFile.exists()) {
				notExistLibraries += (!notExistLibraries.isEmpty() ? ", " : "") + libFile.getName();
				isLaodingFailed[libIndex] = true;
				continue;
			}

			// If the file has not been modified, skip loading it.
			if (libFile.lastModified() == this.libraryScriptLastModList.get(libIndex)) {
				continue;
			}

			// Load the content of the file.
			String libContent = null;
			try {
				libContent = MetaQualifiedFileLoader.load(libPath, DEFAULT_ENCODING);
			} catch (Exception e) {
				loadingFailedLibraries += (!loadingFailedLibraries.isEmpty() ? ", " : "") + libFile.getName();
				loadingFailedCause = e;
				isLaodingFailed[libIndex] = true;
				continue;
			}
			this.libraryScriptContentList.set(libIndex, libContent);
			this.libraryScriptNameList.set(libIndex, libFile.getName());

			// If the library has been loaded successfully, store the last modified time of the library file.
			this.libraryScriptLastModList.set(libIndex, libFile.lastModified());
		}

		// Remove failed library from lists.
		if (!notExistLibraries.isEmpty() || !loadingFailedLibraries.isEmpty()) {
			List<String> succeededLibraryPathList = new ArrayList<String>();
			List<String> succeededLibraryNameList = new ArrayList<String>();
			List<String> succeededLibraryContentList = new ArrayList<String>();
			List<Long> succeededLibraryLastModList = new ArrayList<Long>();
			for (int libIndex=0; libIndex<libN; libIndex++) {
				if (isLaodingFailed[libIndex]) {
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

		// Throw an exception if any library could not be loaded successfully.
		if (!notExistLibraries.isEmpty()) {
			throw new VnanoException(ErrorType.SCRIPT_FILE_DOES_NOT_EXIST, notExistLibraries);
		}
		if (!loadingFailedLibraries.isEmpty()) {
			throw new VnanoException(ErrorType.SCRIPT_FILE_IS_NOT_ACCESSIBLE, loadingFailedLibraries, loadingFailedCause);
		}
	}

}

