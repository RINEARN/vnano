/*
 * A example accessing to fields/methods from an expression executed by Vnano Engine.
 *
 * How to Compile:
 *     javac -cp .;Vnano.jar ExampleApp3.java
 *
 * How to Run:
 *     java -cp .;Vnano.jar ExampleApp3
 */

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ExampleApp3 {

	public static class AnyClass {

		// A field and a method to be accessed from
		// an expression/script runs on Vnano Engine.
		public double x = 3.4;
		public double f(double arg) {
			return arg * 5.6;
		}
	}

	public static void main(String[] args)
			throws VnanoException, NoSuchFieldException, NoSuchMethodException {

		// Create a scripting engine of Vnano (= Vnano Engine).
		VnanoEngine engine = new VnanoEngine();

		// Connect a field/method of "AnyClass" class to Vnano Engine.
		Field field = AnyClass.class.getField("x");
		Method method = AnyClass.class.getMethod("f", double.class);
		AnyClass anyClassInstance = new AnyClass();
		engine.connectPlugin("x", new Object[]{ field, anyClassInstance });
		engine.connectPlugin("f", new Object[]{ method, anyClassInstance });

		// For staric field/method, you can connect it more simple as follows:
		// Field field = AnyClass.class.getField("x");
		// Method method = AnyClass.class.getMethod("f", double.class);
		// engine.connectPlugin("x", field);
		// engine.connectPlugin("f", method);

		// Set an option, to handle all numeric literals as "float" (=double) type.
		// (Useful when calculate expressions, but don't enable when run scripts.)
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("EVAL_INT_LITERAL_AS_FLOAT", true);
		engine.setOptionMap(optionMap);

		// Get an expression from the user.
		System.out.println("Input an expression, e.g.:  1.2 + f(x)");
		Scanner scanner = new Scanner(System.in);
		String expression = scanner.nextLine();

		// Append ";" at the end of the expression, if it does not exist.
		if (!expression.trim().endsWith(";")) {
			expression += ";";
		}

		// Execute the inputted expression by Vnano Engine.
		double result = (Double)engine.executeScript(expression);
		System.out.println("result: " + result);
	}
}
