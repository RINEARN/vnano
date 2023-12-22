/*
 * The most simple example using Vnano Engine.
 *
 * How to Compile:
 *     javac -cp .;Vnano.jar ExampleApp1.java
 *
 * How to Run:
 *     java -cp .;Vnano.jar ExampleApp1
 */

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;

 public class ExampleApp1 {
    public static void main(String[] args) throws VnanoException {

        // Create a scripting engine of Vnano (= Vnano Engine).
        VnanoEngine engine = new VnanoEngine();

        // Execute a script by using Vnano Engine.
        String script = "double a = 1.2;  double b = 3.4;  double c = a + b;  c;";
        double result = (Double)engine.executeScript(script);

        // Display the result.
        System.out.println("result: " + result);
    }
}

