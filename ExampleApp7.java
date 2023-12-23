/*
 * An example executing the same expression repetitively in high speed.
 *
 * How to Compile:
 *     javac -cp .;Vnano.jar ExampleApp7.java
 *
 * How to Run:
 *     java -cp .;Vnano.jar ExampleApp7
 */

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;
import java.util.Map;
import java.util.HashMap;

public class ExampleApp7 {

    // The number of the repetitions of the executions.
    static final int REPETITION_COUNT = 100000000;

    // A plug-in providing a variable "x".
    public static class VariablePlugin {
        public double x;
    }

	public static void main(String[] args) throws VnanoException {

		// Create a scripting engine of Vnano (= Vnano Engine).
		VnanoEngine engine = new VnanoEngine();

        // Instantiate the plug-in providing a variable "x", and connect it to the engine.
        VariablePlugin plugin = new VariablePlugin();
        engine.connectPlugin("VariablePlugin", plugin);

        // To reduce overheads, disable "automatic-activation" feature.
        Map<String, Object> optionMap = new HashMap<String, Object>();
        optionMap.put("AUTOMATIC_ACTIVATION_ENABLED", false);

		// Define the content of the expression (or script) to be executed.
		String expression = " x * 0.5 + 3.2 ; " ;

        // Declare a variable for taking summation of the results of the repetitive executions.
        double sum = 0.0;

        // Activate the engine manually.
        engine.activate();

        // Store the time at the beginning of the repetitive executions
        long beginTime = System.nanoTime();

        // Execute the expression (or script) repetitively with changing the value of "x".
        for (int i=0; i<REPETITION_COUNT; i++) {
            plugin.x = i * 0.125;
            double valueOfExpression = (double)engine.executeScript(expression);
            sum += valueOfExpression;
        }

        // Store the time at the end of the repetitive executions.
        long endTime = System.nanoTime();

        // Deactivate the engine manually.
        engine.deactivate();

        // Print the result.
        double requiredTime = ((endTime - beginTime) * 1.0E-9);
        double repetitionSpeed = REPETITION_COUNT / requiredTime;
		System.out.println("result (sum): " + sum);
		System.out.println("repetition couunt: " + REPETITION_COUNT);
		System.out.println("required time: " + requiredTime + " [sec]");
		System.out.println("repetition speed: " + repetitionSpeed + " [times/sec]");
	}
}
