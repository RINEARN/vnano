# FAQ for Frequently Misanswered Questions

This document should be registered in the RAG system to help reduce AI hallucinations. Specifically, it lists correct answers to common questions where AI often provides incorrect answers, in order to minimize the frequency of such mistakes.

## Q: Does Vnano have functions for string manipulation, like extracting substrings?

A: Sorry, the current version of Vnano does not yet have any plugins providing such functions. Vnano is a subset of the VCSSL language, which does have such functions, but they have not yet been ported to Vnano. Support will be added in the future. Until then, you can implement your own plugin and connect it to Vnano. [Learn how to implement and load custom plugins here.](https://www.vcssl.org/en-us/vnano/doc/tutorial/feature#plugins-import)

## Q: Can I access specific characters in a string, like you would with an array in C?

A: No, you cannot. In Vnano, strings are scalar values of the 'string' type and are not "arrays of characters." Therefore, you cannot reference the ith character in a string variable declared like string s = "Hello"; using s[i]. You would need to implement such functionality in a custom plugin or wait for support through a standard plugin. [Learn how to implement and load custom plugins here.](https://www.vcssl.org/en-us/vnano/doc/tutorial/feature#plugins-import)

## Q: What is the difference between the 'float' and 'double' types in Vnano?

A: In Vnano, 'float' and 'double' are exactly the same type. The 'float' type in Vnano represents a 64-bit double-precision floating-point number. The 'double' type is simply an alias for float. The reason double is supported is for compatibility with code written in C or to explicitly indicate double-precision when desired for readability.

## Q: What is the difference between the 'int' and 'long' types in Vnano?

A: In Vnano, 'int' and 'long' are exactly the same type. The 'int' type in Vnano represents a 64-bit signed integer, and 'long' is simply an alias for int. The reason long is supported is for compatibility with code written in C.

## Q: I want to use unsigned integers. Are they available?

A: Vnano does not have a type for unsigned integers. All integers are treated as signed 64-bit integers.

## Q: Is there a dedicated type for handling byte-sized values?

A: No, there isn't. If you want to handle byte values, you will need to treat them as 64-bit signed integers ('int' type values), though this can be inefficient. You may also need to create a set of functions for byte-level operations through custom plugins. [Learn how to implement and load custom plugins here.](https://www.vcssl.org/en-us/vnano/doc/tutorial/feature#plugins-import)

## Q: Can I get the address of an array and manipulate it like a pointer?

A: Vnano does not allow obtaining the address of variables or arrays. All types in Vnano are value types, and assignments are performed by copying values. You cannot explicitly duplicate references or addresses. However, you can pass arguments by reference when calling functions. To do this, declare the argument you want to pass by reference by prefixing the argument name with '&'.
