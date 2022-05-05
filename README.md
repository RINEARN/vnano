# Vnano

( &raquo; [Japanese README](./README_JAPANESE.md) )

![Logo](./logo.png)


Vnano is a simple scripting language having C-like syntax. 
Its interpreter, Vnano Engine, is designed to be embedded in Java&reg; applications.
By using Vnano, you can execute scripts on you apps, so it enable you to develop highly customizable features.

* [Vnano Website](https://www.vcssl.org/en-us/vnano/)

See also: An example app using Vnano: [RINPn](https://github.com/RINEARN/rinpn).


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
- [Main Features](#features)
- [Vnano as a Language](#language)
- [Performances](#performances)
- [About Us](#about-us)
- [References and Links](#references)


<hr />




<a id="license"></a>
## License

This repository is the source code repository of Vnano Engine, which is the interpreter of Vnano.

Vnano Engine is released under the MIT License.


<a id="requirements"></a>
## Requirements

- Java Development Kit (Version 8 or later is required. The latest version Java 18 is already available.)


<a id="build"></a>
## How to Build

First of all, necessary to build Vnano Engine (interpreter).

<a id="build-win"></a>
### For Microsoft Windows

Clone this repository, and execute a batch file "build.bat" included in it:

	git clone https://github.com/RINEARN/vnano
	cd vnano
	.\build.bat

Then the built JAR file "Vnano.jar" will be generated.

<a id="build-lin"></a>
### For Linux, etc.

Clone this repository, and execute a shell script "build.sh" included in it:

	git clone https://github.com/RINEARN/vnano
	cd vnano
	sudo chmod +x ./build.sh
	./build.sh

Then the built JAR file "Vnano.jar" will be generated.

<a id="build-ant"></a>
### For Apache Ant

Also, if you are using Ant, you can build Vnano Engine as follows:

	git clone https://github.com/RINEARN/vnano
	cd vnano
	and -f build.xml

Then the built JAR file "Vnano.jar" will be generated.


<a id="use"></a>
## How to Use in Your Apps

<a id="use-compile-and-run"></a>
### How to Compile and Run an Application

Then, Let's use Vnano Engine practically, by making a simple expression-calculator application. The source code of it is included in this repository as "ExampleApp1.java":

	(in ExampleApp1.java)

	...
	public static void main(String[] args) throws VnanoException {

		// Create a scripting engine of Vnano (= Vnano Engine).
		VnanoEngine engine = new VnanoEngine();

		// Set an option, to handle all numeric literals as "float" (=double) type.
		// (Useful when calculate expressions, but don't enable when run scripts.)
		Map<String, Object> optionMap = new HashMap<String, Object>();
		optionMap.put("EVAL_INT_LITERAL_AS_FLOAT", true);
		engine.setOptionMap(optionMap);

		// Get an expression from the user.
		System.out.println("Input an expression, e.g.:");
		System.out.println("1.2 + 3.4 * 5.6 ;");
		Scanner scanner = new Scanner(System.in);
		String expression = scanner.nextLine();

		// Execute the inputted expression by Vnano Engine.
		double result = (Double)engine.executeScript(expression);
		System.out.println("result: " + result);
	}

You can compile the above code as follows:

	javac -cp .;Vnano.jar ExampleApp1.java        (For Windows)
	javac -cp .:Vnano.jar ExampleApp1.java        (For Linux)

And run it as:

	java -cp .;Vnano.jar ExampleApp1        (For Windows)
	java -cp .:Vnano.jar ExampleApp1        (For Linux)

The above "ExampleApp1" application requests you to input the expression to be calculated.
So input as follows:

	1.2 + 3.4 * 5.6 ;

(!!! Don't forget to put ";" at the end !!!)

Then the expression will be calculated by using the Vnano Engine, and the result will be displayed as:

	20.24

Also, you can input script code instead of an expression as:

	float value=0.0; for (int i=0; i<10; i++) { value += 1.2; } value += 123.4; value;

The result is:

	result: 135.4

For more details, see [Execute Scripts](FEATURE.md#scripting).

Please note that, when you execute scripts, we strongly recommend to remove the line of:

	optionMap.put("EVAL_INT_LITERAL_AS_FLOAT", true);

If the above option is enabled, all numeric literals (including integer literals) are handled as float-type values. It should be a cause of confusion. By the way, above option does not affect to contents of [library scripts](FEATURE.md#libraries).


<a id="use-create-jar"></a>
### How to Create a JAR file of an Application

To create a JAR file of the above "ExampleApp1" application, create a manifest file "manifest.txt" in advance, and in there specify "Vnano.jar" to the Class-Path section as follows:

	Main-Class: ExampleApp1
	Class-Path: . Vnano.jar

	(!!! Important note: This file should ends with a blank line !!!)

Note that, if you want to put "Vnano.jar" in the different folder (e.g. in "lib" folder), you are required to modify the "Class-Path" section of the above manifest file accordingly (e.g. "Class-Path: . lib/Vnano.jar").

Then you can creaet a JAR file as:

	jar cvfm ExampleApp1.jar manifest.txt ExampleApp1.class

And you can run the created JAR file "ExampleApp1.jar" as:

	java -jar ExampleApp1.jar


<a id="features"></a>
## Main Features

As shown above, by using Vnano Engine, you can execute expression and scripts on your apps.

In addition, you can register fealds and methods of any Java classes to Vnano Engine, 
and access to them from expressions/scripts.
Furthermore, you can implement such Java classes (providing fields and methods) as independent files (called "plug-ins"), and can load them dynamically.

Instead of Java classes, you can define variables and functions in any script files, and can load them dynamically as "library scripts".

By using these features of Vnano Engine, you can develop highly customizable apps
(for example, see [RINPn](https://github.com/RINEARN/rinpn), which is a programmable calculator software).


For details of features, see the document: [Main Features of Vnano Engine](FEATURE.md).


<a id="language"></a>
## Vnano as a Language

The name of the scripting language executable on Vnano Engine is "Vnano".

Vnano having simple C-like syntax. For example:

	int sum = 0;
	for (int i=1; i<=100; i++) {
    	sum += i;
	}
	output(sum);

For details of syntax and language feature of Vnano, see the document: [Vnano as a Language](LANGUAGE.md).

<a id="performances"></a>
## Performances

Our main purpose to develop Vnano Engine is, using it in data-analysis, calculation, and visualization apps. Processing speed is important on such kind of apps, so Vnano Engine can execute scripts at relatively high speed.
In this repository, benchmarking scripts for measuring processing speed of Vnano Engine are included in "benckmark" folder.

For measuring the performance of scalar (non-array) operations of 64-bit float values:

	java -jar Vnano.jar benchmark/ScalarFlops.vnano --accelerator true --optLevel 3

The result is (depends on you environment):

	OPERATING_SPEED = 704.6223224351747 [MFLOPS]
	REQUIRED_TIME = 14.192 [SEC]
	TOTAL_OPERATIONS = 10000000000 [xFLOAT64_ADD]
	OPERATED_VALUE = 1.0E10

The above score had been measured on a mid-range laptop PC.

For measuring the performance of vector (array) operations of 64-bit float values:

	java -jar Vnano.jar benchmark/VectorFlops.vnano --accelerator true --optLevel 3

The result is (depends on you environment):

	OPERATING_SPEED = 15.400812152203338 [GFLOPS]
	REQUIRED_TIME = 13.298 [SEC]
	TOTAL_OPERATIONS = 204800000000 [xFLOAT64_ADD]
	VECTOR_SIZE = 2048 [x64BIT]
	OPERATED_VALUES = { 1.0E8, 2.0E8, 3.0E8, ... 2.047E11, 2.048E11 }

Note that, performances of vector operations are greatly depend on the size of operand vectors, and cache size of your CPU.


<a id="about-us"></a>
## About Us

Vnano is developed by a Japanese software development studio: [RINEARN](https://www.rinearn.com/). The author is Fumihiro Matsui. Please free to contact us if you have any questions, feedbacks, and so on.


## References and Links

Following webpages may be useful if you need more information about Vnano.

* [Vnano Website](https://www.vcssl.org/en-us/vnano/)
* [Vnano Standard Plug-ins](https://www.vcssl.org/en-us/vnano/plugin/)
* [Plug-in Interfaces for VCSSL/Vnano](https://github.com/RINEARN/vcssl-plugin-interface)


## Credits

- Oracle and Java are registered trademarks of Oracle and/or its affiliates. 

- Microsoft Windows is either a registered trademarks or trademarks of Microsoft Corporation in the United States and/or other countries. 

- Linux is a trademark of linus torvalds in the United States and/or other countries. 

- Other names may be either a registered trademarks or trademarks of their respective owners. 

