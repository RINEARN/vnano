/*
 * An example executing a script using Vnano Engine.
 * 
 * How to Compile:
 *     javac -cp .;Vnano.jar ExampleApp5.java
 * 
 * How to Run:
 *     java -cp .;Vnano.jar ExampleApp5
 */

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

public class ExampleApp5 {

	public static void main(String[] args) throws VnanoException {

		// Create a scripting engine of Vnano (= Vnano Engine).
		VnanoEngine engine = new VnanoEngine();

		// Prepare the content of the script to be executed.
		String script =

			" int sum = 0;                 " +
			" for (int i=1; i<=100; i++) { " +
			"     sum += i;                " +
			" }                            " +
			" sum;                         " ;

		// Execute a script by Vnano Engine.
		long result = (Long)engine.executeScript(script);
		System.out.println("result: " + result);
	}
}
