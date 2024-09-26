# Vnano Official Website

Welcome to the official website of the scripting engine/language "Vnano".


## What is the Vnano ?

### A Compact Scripting Engine/Language, Embeddable in Java&trade; Applications

Vnano (VCSSL nano) is a compact and high-speed scripting engine/language that can be embedded in Java applications. This allows you to easily implement scripting features in your apps.

&raquo; [An Example of an Application Using Vnano: RINPn (Scientific Calculator)](https://www.rinearn.com/en-us/rinpn/)

### Also Available for Standalone Use, Similar to Other Scripting Languages

You can also execute Vnano scripts on your PC by installing the [VCSSL Runtime](https://www.vcssl.org/en-us/download/).
Although Vnano's features are streamlined to prioritize embeddability, its operational speed is very high, making it particularly useful for calculation-intensive tasks.

&raquo; [Example: A Script Calculating Integral Values](https://www.vcssl.org/en-us/code/archive/0001/7800-vnano-integral-output/)

### Simple C-like Syntax

Vnano features a simple C-like syntax, making it easy for programmers accustomed to C or similar languages to write and read Vnano scripts.

\- Example of a script written in Vnano -

    int sum = 0;
    for (int i=1; i<=100; i++) {
        sum += i;
    }
    output(sum);

&raquo; [More Details: Features of Vnano as a Language](https://www.vcssl.org/en-us/vnano/doc/tutorial/language)

### Open Source, MIT License

The Vnano scripting engine is open-source software and is released under the MIT License.

&raquo; [Source Code Repository](https://github.com/RINEARN/vnano)


## How to Use

You can start using Vnano in just a few minutes. Let's get started!

### Step1. Download the Vnano Engine

First, download the pre-built Vnano package by clicking the following button, and extract the ZIP file. Within the extracted folder, you will find the JAR file "Vnano.jar," which is the scripting engine of Vnano (Vnano Engine).

[Vnano Official Website, Download button of the pre-built package](https://www.vcssl.org/en-us/vnano/)

    * Please accept the license agreement (MIT License) before use. &raquo; [License](https://github.com/RINEARN/vnano/blob/master/LICENSE)

    * This pre-built package is for trial purposes and may not function correctly depending on the JDK/JRE version in your environment. For more reliable usage, build the JAR file from the source code using the same JDK as your development environment. &raquo; [How to Build](https://www.vcssl.org/ja-jp/vnano/doc/tutorial/use)

Also, if you simply want to execute Vnano scripts, similar to other scripting languages, without embedding the engine in any applications, consider installing the [VCSSL Runtime](https://www.vcssl.org/en-us/download/) instead.


### Step2. Run the Example Script

Within the extracted folder, you'll find an example script file named "ExampleScript1.vnano" written in Vnano:

\- ExampleScript1.vnano -

    int sum = 0;
    for (int i=1; i<=100; i++) {
        sum += i;
    }
    output(sum);

This script calculates the sum of integers from 1 to 100. To run it, navigate to the extracted folder in your command-line terminal and enter the following command:

    java -jar Vnano.jar ExampleScript1.vnano

    # Note: Running this script requires the Java Development Kit: JDK.

Additionally, if the [VCSSL Runtime](https://www.vcssl.org/en-us/download/) is installed on your PC, you can execute this script easily by launching the runtime and selecting the script. You can also run it via the command line if you have configured the path settings.

The output will display the calculated value:

    5050


### Step3. Example Java Application

Within the extracted folder, you'll find an example of a Java application that uses the Vnano, named "ExampleApp1.java":

\- ApplicationExample.java -

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

This application executes a Vnano script that calculates the sum of "1.2 + 3.4".

To compile and run this application, navigate to the extracted folder and use the following commands:

    # For Windows
    javac -cp .;Vnano.jar ExampleApp1.java
    java -cp ".;Vnano.jar" ExampleApp1

    # For other operating systems
    javac -cp .:Vnano.jar ExampleApp1.java
    java -cp ".:Vnano.jar" ExampleApp1

    # Note: Running this requires the Java Development Kit: JDK.

The output should display:

    result: 4.6

This confirms the successful computation of "1.2 + 3.4" using the scripting engine.

For a more detailed explanation of how to use Vnano, refer to the following page:

&raquo; More Drtails: [How to Use Vnano](https://www.vcssl.org/ja-jp/vnano/doc/tutorial/)


## Contents of This Website

* [Vnano Tutorial Guide](https://www.vcssl.org/en-us/vnano/doc/tutorial/): A simple tutorial guide for using Vnano.
* [Specifications of Vnano Engine](https://www.vcssl.org/en-us/vnano/spec/): Documentation on the VnanoEngine class, options, and other technical details.
* [Standard Plug-ins](https://www.vcssl.org/en-us/vnano/plugin/): A list and detailed specifications of standard plug-ins that provide built-in functions and variables.
* [Plug-ins Interface](https://www.vcssl.org/ja-jp/doc/connect/): 
The interface for Plug-in development.
* [Source Code Repository (GitHub)](https://github.com/RINEARN/vnano): The remote repository where the source code of Vnano is managed.


## Code Written in Vnano

Various official Vnano code samples are available in the [VCSSL Code Archive](https://www.vcssl.org/en-us/code/). These may serve as useful examples for understanding Vnano.

* [Fizz Buzz Program](https://www.vcssl.org/en-us/code/archive/0002/0100-vnano-fizz-buzz/): A program printing the correct result of Fizz Buzz game.

* [Solve The Lorenz Equations Numerically](https://www.vcssl.org/en-us/code/archive/0001/8000-vnano-lorenz-attractor/): Solve the Lorenz equations, and output data to plot the solution curve (well-known as the "Lorenz Attractor") on a 3D graph.

* [Compute Integral Value Numerically](https://www.vcssl.org/en-us/code/archive/0001/7800-vnano-integral-output/): Example code computing integral values numerically by using rectangular method, trapezoidal method, and Simpson's rule.

* [Output Data of Numerical Integration For Plotting Graph](https://www.vcssl.org/en-us/code/archive/0001/7900-vnano-integral-for-plot-graph/): Example code computing integrated values numerically, and output data for plotting the integrated functions into graphs.


---

\- Credits and Trademarks -

* Oracle and Java are registered trademarks of Oracle and/or its affiliates.
* Microsoft Windows is either a registered trademarks or trademarks of Microsoft Corporation in the United States and/or other countries.
* Linux is a trademark of linus torvalds in the United States and/or other countries.
* Other names may be either a registered trademarks or trademarks of their respective owners.

