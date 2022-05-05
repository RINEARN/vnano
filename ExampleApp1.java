/*
 * A very simple example application using Vnano.
 * This code calculates the value of an expression inputted by the user.
 * 
 * How to Compile:
 *     javac -cp .;Vnano.jar ExampleApp1.java
 * 
 * How to Run:
 *     java -cp .;Vnano.jar ExampleApp1
 */

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

public class ExampleApp1 {

	public static void main(String[] args) throws VnanoException {

		// Create a scripting engine of Vnano (= Vnano Engine).
		VnanoEngine engine = new VnanoEngine();

		// Set an option, to handle all numeric literals as "float" (=double) type.
		// (Useful when calculate expressions, but don't enable when run scripts.)
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("EVAL_INT_LITERAL_AS_FLOAT", true);
		engine.setOptionMap(optionMap);

		// Get an expression from the user.
		System.out.println("Input an expression, e.g.:  1.2 + 3.4 * 5.6");
		Scanner scanner = new Scanner(System.in);
		String expression = scanner.nextLine();

		// Append ";" at the end of the expression, if it does not exist.
		if (!expression.trim().endsWith(";")) {
			expression += ";";
		}

		// Execute the inputted expression by Vnano Engine.
		double result = (Double)engine.executeScript(expression);
		System.out.println("result: " + result);

		// The type of "result" is "Object", 
		// and its actual type generally depends on the value of the expressions.
		// (May be Double, Long, Boolean, String, or double[], double[][], ...)
		// Check the type of the result if necessary.
	}
}
