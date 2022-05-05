/*
 * A example accessing to fields/methods of dynamically loaded classes,
 * from an expression executed by Vnano Engine.
 * 
 * How to Compile:
 *     javac -cp .;Vnano.jar ExampleApp3.java
 * 
 * How to Run:
 *     java -cp .;Vnano.jar ExampleApp3
 */

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.interconnect.PluginLoader;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ExampleApp3 {

	public static void main(String[] args)
			throws VnanoException, NoSuchFieldException, NoSuchMethodException {

		// Create a scripting engine of Vnano (= Vnano Engine).
		VnanoEngine engine = new VnanoEngine();

		// Load a plug-in classes dynamically, and connect them to Vnano Engine.
		PluginLoader pluginLoader = new PluginLoader("UTF-8");
		pluginLoader.setPluginListPath("./plugin/VnanoPluginList.txt");
		pluginLoader.load();
		for (Object plugin: pluginLoader.getPluginInstances()) {
			engine.connectPlugin("___VNANO_AUTO_KEY", plugin);
		}

		// Set an option, to handle all numeric literals as "float" (=double) type.
		// (Useful when calculate expressions, but don't enable when run scripts.)
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("EVAL_INT_LITERAL_AS_FLOAT", true);
		engine.setOptionMap(optionMap);

		// Get an expression from the user.
		System.out.println("Input an expression, e.g.:");
		System.out.println("1.2 + f(x) ;");
		Scanner scanner = new Scanner(System.in);
		String expression = scanner.nextLine();

		// Execute the inputted expression by Vnano Engine.
		double result = (Double)engine.executeScript(expression);
		System.out.println("result: " + result);
	}
}
