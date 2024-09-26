
# Main Features of Vnano Engine, and Examples

&raquo; [Japanese](FEATURE_JAPANESE.md)

&raquo; [Ask the AI for help (ChatGPT account required)](https://chatgpt.com/g/g-10L5bfMjb-vnano-assistant)

## Table of Contents

- [Calculate Expressions](#calculate-expression)
- [Access to Fields/Methods of a Java&reg; Class (Plug-in)](#fields-and-methods)
- [Define a (Plug-in) Class Providing Methods/Fields as Another File](#plugin-import)
- [Specify Plug-ins to be Loaded, by a List File](#plugins-load)
- [Advanced Steps About Plug-ins (Standard Plug-ins, Less-Overhead Interfaces, etc.)](#plugins-advanced)
- [Execute Scripts](#scripting)
- [Load Library Scripts](#libraries)
- [Option Settings](#option)
- [Permission Settings](#permission)
- [Command-Line Mode](#command-line-mode)
- [Performance Benchmarking / Analysis](#performances)
- [Reduce Overhead-Costs for Repetitive Executions](#repetitive)
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

Note that, in the input content to Vnano Engine, regardless of whether it is an expression or a script, a semicolon ";" is required at the end of each lines. So in the above example, it appends ";" at the end of an expression, before passing it to Vnano Engine.

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

Also, when you want to connect all methods/fields belong to a class/instance, 
simply pass the class/instance to "connectPlugin" method:

    (in ExampleApp3.java, modified)
    ...
    AnyClass anyClassInstance = new AnyClass();
    engine.connectPlugin("AnyNamespace", anyClassInstance);
    ...

As a result of the above, you can access to "x" and "f(x)" from scripts/expression as completely the same as the previous way, in addition, you also can accsess to them with specifing the namespace "AnyNamespace." at  heads of their names.
It is helpful when you want to connect multiple instances of the same class (assign a unique namespace for each instance).

By the way, from scripts runs on Vnano Engine, you can modify the value of a field of a Java class (e.g.: "x" of the above example), but beware of the following behaviour of Vnano Engine: 
**Vnano Engine reads values of connected Java fields just before execution of scripts/expressions, and caches them internally. Then, when the execution has completed, cached values (may be modified by scripts) will be write-backed to connected Java fields.**

Hence, if you want to pass/receive values between Java-side and Script-side interactively, don't access to Java fields directly from scripts. For such purpose, create setter/getter methods of values and connect them to Vnano Engine, and use them from scripts.



<a id="plugin-import"></a>
# Define a (Plug-in) Class Providing Methods/Fields as Another File

Like as "AnyClass" in Example2, we call a class providing fields/methods to Vnano Engine as a "**plug-in**".
(In addition, sometimes we call fields/methods provided by plug-ins as "**external variables/functions**", 
to distinguish from internally declared variables/functions in scripts.)


In previous example, we defined "AnyClass" plug-in as an inner class of "ExampleApp3". 
If you want, you can define it as another file in any package:

    (in a file in any package)
    package anypackage;
    public class AnyClass {

        // A field and a method to be accessed from 
        // an expression/script runs on Vnano Engine.
        public double x = 3.4;
        public double f(double arg) {
            return arg * 5.6;
        }
    }

You can simply import and use it in usual way:

    (in ExampleApp3.java, modified)

    import anypackage.AnyClass;

    ...
    VnanoEngine engine = new VnanoEngine();

    AnyClass anyClassInstance = new AnyClass();
    engine.connectPlugin("AnyNamespace", anyClassInstance);
    ...

The result is the same as the previous example.


<a id="plugins-load"></a>
## Specify Plug-ins to be Loaded, by a List File

The previous example requires to decide "which plug-ins should be loaded/connected" before when the application is compiled. 
However, sometimes you may want to make it customizable by users. 
It becomes little complex than previous examples, but Let's try to do it.

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

In addition, create a text file (list file) "VnanoPluginList.txt" in "plugin" folder, and in there list-up compiled plug-ins to be loaded, as follows:

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
* [Documents of Vnano Plug-in Interfaces](https://www.vcssl.org/en-us/doc/connect/)

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
We call such script providing variables/functions for other scripts, as a "**library script**".

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
        String[] libPaths= scriptLoader.getLibraryScriptPaths(true);
        String[] libScripts = scriptLoader.getLibraryScriptContents();
        int libCount = libScripts.length;
        for (int ilib=0; ilib<libCount; ilib++) {
            engine.registerLibraryScript(libPaths[ilib], libScripts[ilib]);
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


<a id="option"></a>
## Option Settings

As we already seen in ExampleApp2, you can modify some behaviour of Vnano Engine by option settings.

For performing the option settings, 
create a Map<String, Object> storing the option values, with option names as keys. 
We refer to this Map as the "option map". 
Then, pass the option map to "setOptionMap(...)" method of the VnanoEngine instance.

For example:


    import java.util.Map;
    import java.util.HashMap;
    ...

    ( in the method using Vnano Engine )
    
    // Create a Map for storing option names/values (option map).
    Map<String, Object> optionMap = new HashMap<String, Object>();

    // Enable the option handling integer literals as floating point numbers.
    optionMap.put("EVAL_INT_LITERAL_AS_FLOAT", true);

    // Enable the option restricting types of operands to only floating point numbers.
    optionMap.put("EVAL_ONLY_FLOAT", true);

    // Enable the option restricting types of statements to only expressions.
    optionMap.put("EVAL_ONLY_EXPRESSION", true);

    // Set to the instance of Vnano Engine.
    try {
        engine.setOptionMap(optionMap);
    } catch (VnanoException e) {
        System.err.println("Incorrect option settings have been detected.");
        e.printStackTrace();
    }


The above example assumes that Vnano Engine is used for calculating expressions on calculator apps (On such apps, its depeloper may want to restrict integer operations, to avoid confusion about behaviour of so called integer-division).

About all available option items and their details, see [Specifications of Vnano Engine](SPEC.md).


<a id="permission"></a>
## Permission Settings

Some plug-ins and libraries perform actions which sometimes app-developers or users want to restrict it, depends on situations. For example, file-reading/writing/overwriting and so on. 
Sometimes users/developers want to allow it, and sometimes they want to deny it.

Vnano Engine is equipped with a permission-based mechanism for such situation. Plug-ins (or libraries using them) request Vnano Engine to allow an actions like above, and then the engine decides whether allow/deny it, depends on settings, or by asking users. 
We refer this process as "permission request", and settings for controlling it as "permission settings".

For performing the permission settings, 
create a Map<String, String> storing the setting values, with the names of the permission items as keys. 
We refer to this Map as the "permission map". 
Then, pass the permission map to "setPermissionMap(...)" method of the VnanoEngine instance.

For example:


    import org.vcssl.connect.ConnectorPermissionName;
    import org.vcssl.connect.ConnectorPermissionValue;
    import java.util.Map;
    import java.util.HashMap;
    ...

    ( in the method using Vnano Engine )
    
    // Create a Map for storing permission names/values (permission map).
    Map<String, String> permissionMap = new HashMap<String, String>();

    // Set the default value to "DENY".
    // (Applied for permission items which are not set explicitly.)
    permissionMap.put(ConnectorPermissionName.DEFAULT, ConnectorPermissionValue.DENY);

    // Set file-creation / writing (excluding overwriting) / reading to "ALLOW".
    permissionMap.put(ConnectorPermissionName.FILE_CREATE, ConnectorPermissionValue.ALLOW);
    permissionMap.put(ConnectorPermissionName.FILE_WRITE, ConnectorPermissionValue.ALLOW);
    permissionMap.put(ConnectorPermissionName.FILE_READ, ConnectorPermissionValue.ALLOW);

    // Set file-overwriting to "ASK", for asking users to decide whether allow/deny it.
    permissionMap.put(ConnectorPermissionName.FILE_OVERWRITE, ConnectorPermissionValue.ASK);

    // Set to the instance of Vnano Engine.
    try {
        engine.setPermissionMap(permissionMap);
    } catch (VnanoException e) {
        System.err.println("Incorrect permission settings have been detected.");
        e.printStackTrace();
    }

About all available permission items and their details, see [Specifications of Vnano Engine](SPEC.md).

Also, on the [command-line mode](#command-line-mode) which we will introduce in the latter section, use --permission option for switching permission settings, from some choices:

    java -jar Vnano.jar --permission askAll Script.vnano

In the above example, we specify "askAll", for asking users to decide whether allow/deny a permission request when it is requested, for every permission items. You can also specify "denyAll" or "allowAll" for denying/allowing every permission requests, or "balanced" for using the permission settings considering balance between userbility and protectividy. For details, see the explanation by --help option.

By the way, when you are developing your original plug-in, sometimes you may want to ask users to decide whether your plug-in shoud do some process or don't (e.g.: file-overwriting). Hence, you may want to support the permission controlling mechanism shown in this section, by your original plug-in. For supporting the mechanism, implement plug-in interfaces such as [XNCI1](https://www.vcssl.org/en-us/doc/connect/ExternalNamespaceConnectorInterface1_SPEC_ENGLISH), and request permissions to Vnano Engine by calling requestPermission(...) method of the [engine connector interface](https://www.vcssl.org/en-us/doc/connect/EngineConnectorInterface1_SPEC_ENGLISH), which is passed when the plug-in is intialized.

Supporting this mechanism by your original plug-ins is good way, because app-developpers and users can integratedly control whether allow/deny actions through the mecanism. For example, as an opposite way, imagine the situation that each plug-in has a permission setting for file-overwriting independently, or each plug-in asks an user to decide whether overwrites something independently. It should be difficult to manage them for app-developers/users, especially when many plug-ins are used.


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


<a id="performances"></a>
## Performance Benchmarking / Analysis

Vnano Engine focuses on processing speed, assuming use in data-analysis / calculation software. 
In this repository, benchmarking scripts for measuring processing speed of Vnano Engine are included in "benckmark" folder.

For measuring the performance of scalar (non-array) operations of 64-bit floating point (FP64) numbers:

    java -jar Vnano.jar benchmark/ScalarFlops.vnano

The result is (depends on you environment):

    OPERATING_SPEED = 704.6223224351747 [MFLOPS]
    ...

where [MFLOPS] is a unit of operating speed of floating point numbers. 1MFLOPS represents the speed that 1 million of operations are performed in 1 second. 
The above score means that, on Vnano Engine, about 700 millions of FP64 operations have performed in 1 second (measured on a mid-range laptop PC).

For measuring the performance of vector (array) operations of 64-bit floating point (FP64) numbers:

    java -jar Vnano.jar benchmark/VectorFlops.vnano

The result is (depends on you environment):

    OPERATING_SPEED = 15.400812152203338 [GFLOPS]
    ...

where [GFLOPS] is also a unit of operating speed of floating point numbers. 1GFLOPS represents the speed that 1 billion of operations are performed in 1 second. Hence the above result means that, on Vnano Engine, about 15 billions of FP64 operations have performed in 1 second.
Note that, performances of vector operations are greatly depend on the size of operand vectors, and cache size of your CPU.

Also, when you do performance tuning of your practical scripts, the command-line option "--perf all" may be helpful:

    java -jar Vnano.jar  --perf all YourScript.vnano

An example of result is:

    (printed for each second)

    ==================================================
    = Performance Monitor (2022-05-07 14:16:39.28)
    = - VM Speed  = 384.2 MHz (VRIL Instructions / sec)
    = - RAM Usage = 21.8 MiB (Max 16.0 GiB Available)
    = - Instruction Execution Frequency :
        -     MOV :  34.83 %   (938 count)
        -     MUL :  23.80 %   (641 count)
        -     ADD :  21.50 %   (579 count) 
        -     DIV :   7.69 %   (207 count)
        -     NEG :   5.46 %   (147 count)
        -     SUB :   4.57 %   (123 count)
        -    JMPN :   0.76 %   (20.5 count)
        -     REM :   0.63 %   (17 count)
        -      LT :   0.58 %   (15.5 count)
        -      EQ :   0.19 %   (5 count)
        (Total 3686 Samples)
    ==================================================

On the result of "Instruction Execution Frequency" section, if the total propotions of arithmetic operations (ADD, SUB, MUL, DIV, REM, NEG) and external function calls (CALLX) and MOV instructions are enough high, the script is well optimized.

In the contrast, if JMP/JMPN or LT/GT/EQ/GEQ/LEQ have large proportions, it means that "if/else" statements or small loops are being bottolnecks in your scripts, so maybe you can improve performance by modifying its processing flow.



<a id="repetitive"></a>
## Reduce Overhead-Costs for Repetitive Executions

In the previous section, we have measured performances in the cases that single script performs huge number of numerical operations.

In the contrast, there may be situation that an application repetitively requests to the engine to execution of a small math expression or script, in very high frequency. For example, when an application plots a math expression on a graph, or compute a value using a convergent algorithm depending on an user-input expression.

In this repository, an example code "ExampleApp7.java" which simulates such situation, is contained:

    (in ExampleApp7.java)

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
            
            //Create a scripting engine of Vnano (= Vnano Engine).
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

            // Execute the expression (or script) for REPETITION_COUNT times repetitively.
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

This example code execute the math expression "x * 0.5 + 3.2" repetitively for 100 million times repetitively, whth changing the value of "x". The required time to complete it is about 20 seconds. It means that Vnano Engine has processed requests of repetitive executions in a speed of hundreds of millions of times per second:

    result (sum): 3.1250031655706694E14
    repetition couunt: 100000000
    required time: 21.030388600000002 [sec]
    repetition speed: 4755023.880062777 [times/sec]

By the way, in general situation, executing one expression or script takes more than some milliseconds, caused by various overhead costs of the execution. Assumed from this fact, it seemes that the above requires over 1 day to complete.
However, as shown in the above, it actually requires only about 20 seconds! 

Why? For repetitive executions, Vnano Engine caches some resources used for the first execution, and reuse it for the latter executions. This allows to reduce overhead costs of the executions drastically, realizing the above processing speed.

Please note that, the processing speed depends on the load - number of repetitions.
If the load is far lighter than the above, e.g.: 10000 repetitions, the speed degradetes in some extent (It probably related to CPU's idling behaviour and so on). The above speed is realized under relatively heavy loads, e.g.: over 1 millions of repetitions. 

By the way, in the previous code, an additional  technique to reduce overheads is used.
Specifically, we disabled "automatic-activation" feature and activated/deactivated the engine manually as follows:

    (in ExampleApp7.java)

    ...

    // To reduce overheads, disable "automatic-activation" feature.
    Map<String, Object> optionMap = new HashMap<String, Object>();
    optionMap.put("AUTOMATIC_ACTIVATION_ENABLED", false);

    ...

    // Activate the engine manually.
    engine.activate();

    ...

    // Execute the expression (or script) repetitively with changing the value of "x".
    for (int i=0; i<REPETITION_COUNT; i++) {
        plugin.x = i * 0.125;
        double valueOfExpression = (double)engine.executeScript(expression);
        sum += valueOfExpression;
    }

    ...

    // Deactivate the engine manually.
    engine.deactivate();

Where "activation" is to switch the engine into the state ready for executions, and "deactivation" is to release the engine from the state.
By default, the engine is activated automatically when an execution is requested, and deactivated automatically when the execution is complete.
However, activating the engine requires a certain overhead cost, so it can be a cause of degradation of the speed, for heavy repetitive executions.

Hence automatic-activation is disabled in the above code, and we activated the engine manyally at only once, before the repetitive executions. And then, we deactivated the engine when all repetitive executions complete.
This technique is effective especially when many plug-ins are connected to the engine, or some plug-ins take relatively-long time to be initialized.



<a id="specifications"></a>
## Specifications

For detailed specifications of methods, options and so on of Vnano Engine, see the document: [Specifications of Vnano Engine](SPEC.md).


## Credits

- Oracle and Java are registered trademarks of Oracle and/or its affiliates. 

- ChatGPT is a trademark or a registered trademark of OpenAI OpCo, LLC in the United States and other countries.

- Other names may be either a registered trademarks or trademarks of their respective owners. 
