
# Main Features of Vnano Engine, and Examples

## Index

( &raquo; [Japanese](FEATURE_JAPANESE.md) )

- [Calculate Expressions](#calculate-expression)
- [Access to Fields/Methods of a Java&reg; Class (Plug-in)](#fields-and-methods)
- [Load Plug-ins Dynamically](#plugins-load)
- [Advanced Steps About Plug-ins (Standard Plug-ins, Less-Overhead Interfaces, etc.)](#plugins-advanced)
- [Execute Scripts](#scripting)
- [Load Library Scripts](#libraries)
- [Command-Line Mode](#command-line-mode)
- [Specifications](#specifications)

<hr />

<a id="calculate-expression"></a>
## Calculate Expressions

Vnano Engine is an interpreter for processing scripts, but it is also available for calculating expressions. Probably there are many times you want to calculate expressions than scripts, so Let's start this guide from: how to calculate expressions.

The following is an example application, calculating an expression inputted by the user:

    (in ExampleApp2.java)

    import org.vcssl.nano.VnanoEngine;
    import org.vcssl.nano.VnanoException;
    import java.util.Map;
    import java.util.HashMap;
    import java.util.Scanner;

    public class ExampleApp2 {
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
        }
    }

How to compile and run is:

    (For Windows)
    javac -cp .;Vnano.jar ExampleApp2.java
    java -cp .;Vnano.jar ExampleApp2

    (For Linux)
    javac -cp .:Vnano.jar ExampleApp2.java
    java -cp .:Vnano.jar ExampleApp2

When you have executed the above "ExampleApp2", it requests you to input the expression to be calculated.
So input as follows:

    1.2 + 3.4 * 5.6

Then the expression will be calculated by using the Vnano Engine, and the result will be displayed as:

    result: 20.24

Also, you can input script code instead of an expression as:

    float value=0.0; for (int i=0; i<10; i++) { value += 1.2; } value += 123.4; value;

The result is:

    result: 135.4

For more details, see [Execute Scripts](#scripting).

Please note that, when you execute scripts, we strongly recommend to remove the line of:

    optionMap.put("EVAL_INT_LITERAL_AS_FLOAT", true);

If the above option is enabled, all numeric literals (including integer literals) are handled as float-type values. It should be a cause of confusion. By the way, above option does not affect to contents of [library scripts](#libraries).

Also, for the list and details of available option items, see the document: [Specifications of Vnano Engine](SPEC.md).


<a id="fields-and-methods"></a>
## Access to Fields/Methods of a Java Class (Plug-in)

You can connect fields and methods of any class to Vnano Engine, and can access to them from an expression/script runs on the engine.

For example:

    (in ExampleApp3.java)

    ...
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

        // For staric field/method, you can connect without an instance as follows:
        // Field field = AnyClass.class.getField("x");
        // Method method = AnyClass.class.getMethod("f", double.class);
        // engine.connectPlugin("x", field);
        // engine.connectPlugin("f", method);

        ...
        (same as ExampleApp2.java)
    }


Let's compile and run:

    (For Windows)
    javac -cp .;Vnano.jar ExampleApp3.java
    java -cp .;Vnano.jar ExampleApp3

    (For Linux)
    javac -cp .:Vnano.jar ExampleApp3.java
    java -cp .:Vnano.jar ExampleApp3

Then input the following expression:

    1.2 + f(x)

Now the value of x is 3.4 and f(x) = x * 5.6, so we should get the result of 1.2 + (3.4 * 5.6) = 20.24.
The actual result is:

    result: 20.24

Like as "AnyClass" in Example2, we call a class providing fields/methods to Vnano Engine as a "plug-in".

By the way, from scripts runs on Vnano Engine, you can modify the value of a field of a Java class (e.g.: "x" of the above example), but beware of the following behaviour of Vnano Engine: 
**Vnano Engine reads values of connected Java fields just before execution of scripts/expressions, and caches them internally. Then, when the execution has completed, cached values (may be modified by scripts) will be write-backed to connected Java fields.**

Hence, if you want to pass/receive values between Java-side and Script-side interactively, don't access to Java fields directly from scripts. For such purpose, create setter/getter methods of values and connect them to Vnano Engine, and use them from scripts.


<a id="plugins-load"></a>
## Load Plug-ins Dynamically

You can implement plug-ins as independent class files, and can load them dynamically.
An example plug-in class "ExamplePlugin1.java" is included in "plugin" folder:

    (in plugin/ExamplePlugin1.java)

    public class ExamplePlugin1 {
    
        // A field and a method to be accessed from 
        // an expression/script runs on Vnano Engine.
        public double x = 3.4;
        public double f(double arg) {
            return arg * 5.6;
        }
    }

Compile it as follows:

    cd plugin
    javac ExamplePlugin1.java
    cd ..

In addition, create a text file "VnanoPluginList.txt" in "plugin" folder, and in there list-up compiled plug-ins to be loaded, as follows:

    (in plugin/VnanoPlyginList.txt)

    ExamplePlugin1.class
    # ExamplePlugin2.class
    # ExamplePlugin3.class
    # ...

where lines starts with "#" will be ignored.

On the [command-line mode](#command-line-mode), the above list file will be referred by default. On the other hand, when you use Vnano Engine on you applications, it is necessary to specify the list file explicitly, as we do in the following example.

Now you are all set. Let's load plug-ins dynamically and connect them to Vnano Engine:

    (in ExampleApp4.java)
    
    import org.vcssl.nano.interconnect.PluginLoader;

    ...
    public static void main(String[] args) throws VnanoException {

        // Create a scripting engine of Vnano (= Vnano Engine).
        VnanoEngine engine = new VnanoEngine();

        // Load a plug-in classes dynamically, and connect them to Vnano Engine.
        PluginLoader pluginLoader = new PluginLoader("UTF-8");
        pluginLoader.setPluginListPath("./plugin/VnanoPluginList.txt");
        pluginLoader.load();
        for (Object plugin: pluginLoader.getPluginInstances()) {
            engine.connectPlugin("___VNANO_AUTO_KEY", plugin);
        }

        ...
        (same as ExampleApp2.java)
    }

Let's compile and run:

    (For Windows)
    javac -cp .;Vnano.jar ExampleApp4.java
    java -cp .;Vnano.jar ExampleApp4

    (For Linux)
    javac -cp .:Vnano.jar ExampleApp4.java
    java -cp .:Vnano.jar ExampleApp4

Input the expression:

    1.2 + f(x)

And you can get the result:

    result: 20.24



<a id="plugins-advanced"></a>
## Advanced Steps About Plug-ins (Standard Plug-ins, Less-Overhead Interfaces, etc.)

A fundamental set of plug-ins are officially provided as "Vnano Standard Plug-ins":

* [Source Code Repository of Vnano Standard Plug-Ins](https://github.com/RINEARN/vnano-standard-plugin)
* [Specification Documents of Vnano Standard Plug-Ins](https://www.vcssl.org/en-us/vnano/plugin/)

Basic I/O functions, math and statistical functions, and so on are provided by the above standard plug-ins, so you are not required to implement them by yourself.

Also, interfaces for developing less-overhead plug-ins are provided:

* [Source Code Repository of Vnano Plug-in Interfaces](https://github.com/RINEARN/vcssl-plugin-interface)

When you develop a plug-in which is very frequently accessed from scripts, and you want to make it as faster as possible, consider to use above interfaces.


<a id="scripting"></a>
## Execute Scripts

Instead of an expression, you can execute script code written in C-like language.
The name of the scripting language is "Vnano".
For details of syntax and language features of Vnano, see the following document included in this repository:

* [Vnano as a Language](LANGUAGE.md).

Let's execute a Vnano script:

    (in ExampleApp5.java)

    ...
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

When "executeScript(script)" method has executed a script, it returns: the value of the expression statement (if exists) at the last line in the script.
So we will get the value of the variable "sum", when we run the above Example4.

Compile the above example and run:

    (For Windows)
    javac -cp .;Vnano.jar ExampleApp5.java
    java -cp .;Vnano.jar ExampleApp5

    (For Linux)
    javac -cp .:Vnano.jar ExampleApp5.java
    java -cp .:Vnano.jar ExampleApp5

And you can get the result:

    result: 5050

This value equals to the summation from 1 to 100 ( = 100 * 101 / 2 ), so we have gotten the correct result.



<a id="libraries"></a>
## Load Library Scripts

Vnano (as a scripting language) supports declarations of variables and functions.
So sometimes you may want to make a script in there utility functions and variables are declared, for using them in other scripts.
We call such script providing variables/functions for other scripts, as a "library script".

An example of a library script is:

    (in lib/ExampleLibrary1.vnano)

    float x = 3.4;
    float f(float arg) {
        return arg * 5.6;
    }

To load the above library, create a text file "lib/VnanoLibraryList.txt" and in there list-up library scripts:

    (in lib/VnanoLibraryList.txt)

    ExampleLibrary1.vnano
    # ExampleLibrary2.vnano
    # ExampleLibrary3.vnano

where lines starts with "#" will be ignored.

On the [command-line mode](#command-line-mode), the above list file will be referred by default. On the other hand, when you use Vnano Engine on you applications, it is necessary to specify the list file explicitly as:

    (in ExampleApp6.java)
    
    import org.vcssl.nano.interconnect.ScriptLoader;

    ...
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

            " float value = 1.2 + f(x); " +
            " value;                      " ;

        // Execute a scriptby Vnano Engine.
        double result = (Double)engine.executeScript(script);
        System.out.println("result: " + result);
    }

How to compile and run is:

    javac -cp .;Vnano.jar ExampleApp6.java
    java -cp .;Vnano.jar ExampleApp6

The result is:

    result: 20.24

As declared in "lib/ExampleLibrary1.vnano", the value of x is 3.4 and f(x) = x * 5.6, so we should get the result of 1.2 + (3.4 * 5.6) = 20.24. Hence the above result is correct.


<a id="command-line-mode"></a>
## Command-Line Mode

In addition to the embedded use in apps, you can directly execute a Vnano script file on a command line terminal, by using "command-line mode" of Vnano Engine. 
It may be helpful for debugging scripts/plug-ins when you are developing them.

The following is an example of Vnano script:

    (in ExampleScript1.vnano)

    int sum = 0;
    for (int i=1; i<=100; i++) {
        sum += i;
    }
    output(sum);

Let's execute it as:

    java -jar Vnano.jar ExampleScript1.vnano

As the above, when a script is specified as an argument, "Vnano.jar" works in the command-line mode, and executes the specified script. The result is:

    5050

As the above example script, on the command-line mode, "output" function is provided by default for the convenience.
Please note that, it is NOT provided by default when you execute scripts 
by using "executeScript" method of Vnano Enginem, embedded in your Java applications.

We recommend to introduce Vnano Standard Plug-ins if you use this command-line mode frequently. How to:

    git clone https://github.com/RINEARN/vnano-standard-plugin
    cd vnano-standard-plugin/plugin
    javac -encoding UTF-8 @org/vcssl/connect/sourcelist.txt
    javac -encoding UTF-8 @org/vcssl/nano/plugin/sourcelist.txt

And then copy & paste "vnano-standard-plugin/plugin" folder to "vnano/plugin" folder, 
and rename "VnanoPluginList_AllStandards.txt" in the folder to "VnanoPluginList.txt" (it will be referred by default on the command-line mode).

If you have succeeded to introduce standard plug-ins, following script should run without any errors:

    float value = mean(1.0, 2.0);  // Compute the mean value
    print(value);                  // Prints the value on the terminal

    // result: 1.5

Now you can use [all features of all standard plug-ins](https://www.vcssl.org/ja-jp/vnano/plugin/), on the command-line mode. Also, some practical example scripts are [provided on vcssl.org](https://www.vcssl.org/en-us/code/#vnano). Now you should able to execute all of them.

For more details of the command-line mode, specify --help option.


<a id="specifications"></a>
## Specifications

For detailed specifications of methods, options and so on of Vnano Engine, see the document: [Specifications of Vnano Engine](SPEC.md).


## Credits

- Oracle and Java are registered trademarks of Oracle and/or its affiliates. 

- Other names may be either a registered trademarks or trademarks of their respective owners. 
