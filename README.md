# Vnano

( &raquo; [Japanese README](./README_JAPANESE.md) )

![Logo](./logo.png)

Vnano is a simple scripting language with C-like syntax. The Vnano Engine, its interpreter, is designed to be embedded in Java&trade; applications. By using Vnano, you can execute scripts within your apps, enabling the development of highly customizable features.

* [Vnano Website](https://www.vcssl.org/en-us/vnano/)
* [List of Documents](doc/README.md)


## English README Index

( &raquo; [Japanese](./README_JAPANESE.md) )

- [License](#license)
- [Requirements](#requirements)
- [How to Build](#build)
    - [For Microsoft&reg; Windows&reg;](#build-win)
    - [For Linux, etc.](#build-lin)
    - [For Apache Ant](#build-ant)
- [How to Use in Your Apps](#use)
    - [How to Compile and Run an Application](#use-compile-and-run)
    - [How to Create a JAR file of an Application](#use-create-jar)
- [Main Features and Specifications](#features)
- [Vnano as a Language](#language)
- [Performances](#performances)
- [About Us](#about-us)
- [References and Links](#references)


<hr />




<a id="license"></a>
## License

This repository hosts the source code of the Vnano Engine, which is the interpreter for the Vnano language.

Vnano Engine is released under the MIT License.


<a id="requirements"></a>
## Requirements

- Java Development Kit (Java 8 or later required)
- Git


<a id="build"></a>
## How to Build

Here's how to build the Vnano Engine (interpreter).

<a id="build-win"></a>
### For Microsoft Windows

Clone this repository and execute the included batch file "build.bat":

    git clone https://github.com/RINEARN/vnano
    cd vnano
    .\build.bat

This will generate the built JAR file "Vnano.jar".

<a id="build-lin"></a>
### For Linux, etc.

Clone this repository and execute the included shell script "build.sh":

    git clone https://github.com/RINEARN/vnano
    cd vnano
    sudo chmod +x ./build.sh
    ./build.sh

This will generate the built JAR file "Vnano.jar".

<a id="build-ant"></a>
### For Apache Ant

If you are using Apache Ant, you can build the Vnano Engine as follows:

    git clone https://github.com/RINEARN/vnano
    cd vnano
    and -f build.xml

This will generate the built JAR file "Vnano.jar".


<a id="use"></a>
## How to Use in Your Apps

<a id="use-compile-and-run"></a>
### How to Compile and Run an Application

Let's try using the Vnano Engine practically by creating a very simple application. The source code for this application, "ExampleApp1.java", is included in this repository:

    (in ExampleApp1.java)

    import org.vcssl.nano.VnanoEngine;
    import org.vcssl.nano.VnanoException;

     public class ExampleApp1 {
        public static void main(String[] args) throws VnanoException {

            // Create a Vnano scripting engine.
            VnanoEngine engine = new VnanoEngine();

            // Execute a script using the Vnano Engine.
            String script = "double a = 1.2;  double b = 3.4;  double c = a + b;  c;";
            double result = (Double)engine.executeScript(script);

            // Display the result.
            System.out.println("result: " + result);
        }
    }

You can compile the above code as follows:

    javac -cp .;Vnano.jar ExampleApp1.java        # For Windows
    javac -cp .:Vnano.jar ExampleApp1.java        # For Linux

And run it as:

    java -cp .;Vnano.jar ExampleApp1        # For Windows
    java -cp .:Vnano.jar ExampleApp1        # For Linux

The result is:

    result: 4.6

This output from "ExampleApp1" confirms that the script was executed correctly by the Vnano Engine, calculating the sum of 1.2 and 3.4 as 4.6.

For more detailed information on using the features of the Vnano Engine, refer to the documentation on the [main feature of Vnano Engine and examples](doc/FEATURE.md).



<a id="use-create-jar"></a>
### How to Create a JAR file of an Application

To package the "ExampleApp1" application into a JAR file, first create a manifest file named "manifest.txt". In this file, specify "Vnano.jar" in the Class-Path section as follows:

    Main-Class: ExampleApp1
    Class-Path: . Vnano.jar

    (!!! Important note: This file should end with a blank line !!!)

If you need to place "Vnano.jar" in a different folder (e.g., in a "lib" folder), you must adjust the "Class-Path" in the manifest file accordingly (e.g., "Class-Path: . lib/Vnano.jar").

You can then create the JAR file using the command:

    jar cvfm ExampleApp1.jar manifest.txt ExampleApp1.class

To run the created JAR file "ExampleApp1.jar", use the following command:

    java -jar ExampleApp1.jar


<a id="features"></a>
## Main Features and Specifications

As demonstrated, the Vnano Engine allows you to execute expressions and scripts within your applications.

Additionally, you can register fields and methods from any Java class with the Vnano Engine and access them directly from expressions or scripts. Moreover, such Java classes (providing fields and methods) can be developed as independent "plug-ins" and loaded dynamically.

Alternatively, instead of using Java classes, you can define variables and functions directly within script files and load them dynamically as "library scripts."

These features enable you to develop highly customizable applications. For example, [RINPn](https://github.com/RINEARN/rinpn), a programmable calculator software, leverages these capabilities.

For more detailed information on the features, refer to the document: "[Main Features of Vnano Engine, and Examples](doc/FEATURE.md)."

Additionally, for a comprehensive list of methods, options, and specifications of the Vnano Engine, see the document: "[Specifications of Vnano Engine](doc/SPEC.md)."



<a id="language"></a>
## Vnano as a Language

The scripting language executable on the Vnano Engine is named "Vnano." It features a simple, C-like syntax. For example:

    int sum = 0;
    for (int i=1; i<=100; i++) {
        sum += i;
    }
    output(sum);

For details on the syntax and language features of Vnano, refer to the document: "[Vnano as a Language](doc/LANGUAGE.md)."


<a id="performances"></a>
## Performances

One of the primary purposes of developing the Vnano Engine is to use it in data analysis, calculation, and visualization applications where processing speed is crucial. Consequently, the Vnano Engine can execute scripts at a relatively high speed.

For instance, on a standard laptop PC under ideal conditions (measured by benchmarking scripts), the Vnano Engine can perform about 7 million operations per second (700 MFLOPS) for scalar values. Additionally, it can perform about 15 billion operations per second (15 GFLOPS) for values stored in arrays.

For more details, see "[Performance Benchmarking and Analysis](doc/FEATURE.md#performances)."


<a id="about-us"></a>
## About Us

Vnano is developed by [RINEARN](https://www.rinearn.com/), a Japanese software development studio. The author of the Vnano Engine is Fumihiro Matsui, the founder of RINEARN. Please feel free to contact us if you have any questions, feedback, or other inquiries.

## References and Links

The following webpages may be useful if you need more information about Vnano:

* [Vnano Website](https://www.vcssl.org/en-us/vnano/)
* [Vnano Standard Plug-ins](https://www.vcssl.org/en-us/vnano/plugin/)
* [Plug-in Interfaces for VCSSL/Vnano](https://github.com/RINEARN/vcssl-plugin-interface)


## Credits

- Oracle and Java are registered trademarks of Oracle and/or its affiliates. 

- Microsoft Windows is either a registered trademarks or trademarks of Microsoft Corporation in the United States and/or other countries. 

- Linux is a trademark of linus torvalds in the United States and/or other countries. 

- Other names may be either a registered trademarks or trademarks of their respective owners. 

