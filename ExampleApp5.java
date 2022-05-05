/*
 * An example executing a script, in which 
 * a function/variable provided by "lib/ExampleLibrary1.vnano" are used.
 * To load the above library script, it is necessary to modify
 * the content of "lib/VnanoLibraryList.txt".
 * 
 * How to Compile:
 *     javac -cp .;Vnano.jar ExampleApp5.java
 * 
 * How to Run:
 *     java -cp .;Vnano.jar ExampleApp5
 */

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.interconnect.ScriptLoader;

public class ExampleApp5 {

	public static void main(String[] args) throws VnanoException {

		// Create a scripting engine of Vnano (= Vnano Engine).
		VnanoEngine engine = new VnanoEngine();

		// Load library scripts from files.
		ScriptLoader scriptLoader = new ScriptLoader("UTF-8");
		scriptLoader.setLibraryScriptListPath("./lib/VnanoLibraryList.txt");
		scriptLoader.load();

		// Register library scripts to Vnano Engine.
		String[] libNames = scriptLoader.getLibraryScriptNames();
		String[] libScripts = scriptLoader.getLibraryScriptContents();
		int libCount = libNames.length;
		for (int ilib=0; ilib<libCount; ilib++) {
			engine.includeLibraryScript(libNames[ilib], libScripts[ilib]);
		}

		// Prepare the content of the script to be executed.
		String script =

			" float value = 1.2 + f(3.4); " +
			" value;                      " ;

		// Execute a scriptby Vnano Engine.
		double result = (Double)engine.executeScript(script);
		System.out.println("result: " + result);
	}
}
