/* 
 * An example of a plug-in class.
 * 
 * Plug-ins are classes for providing methods/fields
 * to scripts/expressions runs on Vnano Engine.
 * 
 * To load this plug-in, modify "VnanoPluginList.txt", 
 * an then load it by using PluginLoader. See: "ExampleApp4.java".
 * 
 * How to compile:
 * 
 *     cd <this_folder>
 *     javac ExamplePlugin1.java
 */

public class ExamplePlugin1 {
    
	// A field and a method to be accessed from 
	// an expression/script runs on Vnano Engine.
	public double x = 3.4;
	public double f(double arg) {
		return arg * 5.6;
	}
}
