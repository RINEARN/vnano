# The Vnano as a Language

## - Index -

( &raquo; [Japanese](LANGUAGE_JAPANESE.md) )

- [What is Vnano?](#what-is)
- [Data Types](#data-type)
- [Variable Declaration Statements](#variable)
    - [Daclaration of scalar variables](#variable-scalar)
    - [Daclaration of arrays](#variable-array)
- [Control Statements](#control)
    - [if and else statements](#control-if-else)
    - [for statement](#control-for)
    - [while statement](#control-while)
    - [break statement](#control-break)
    - [continue statement](#control-continue)
- [Expressions](#expression)
    - [Syntax elements of expressions](#expression-syntax)
    - [Operators](#expression-operator)
- [Functions](#function)
    - [Scalar input/output functions](#function-scalar)
    - [Array input/output functions](#function-array)
    - [Formal parameters and actual arguments](#function-params-and-args)
    - [Call by value](#function-call-by-value)
    - [Call by reference](#function-call-by-reference)

<hr />


<a id="what-is"></a>
## What is Vnano?

The Vnano (VCSSL nano) is a programming language having simple C-like syntax.
Language specifications of the Vnano is specialized for embedded use in applications, 
so compared with other programming languages, many features not necessary for the embedded use are omitted 
for making the implementation of the script engine compact.
It is the concept for giving priority to customizability, maintainability, security, portability 
and so on than the functionality.


<a id="data-type"></a>
## Data Types

Vnano supports only int (=long), float (=double), bool, and string for data-types.

| Type name | Description | 
| --- | --- |
| int (or long) | The 64-bit signed integer type |
| float (or double) | The 64-bit floating point number type |
| bool | The boolean type |
| string | The character string type |

Other primitive data types, pointer, struct and class are not supported.
On the other hand, array types of the data types in the above table are supported, 
and you can use it with C-like syntax.

However, please note that arrays in the Vnano (and VCSSL) behaves as value types, not reference types or pointers.
The assignment operation (=) of an array behaves as the copy of all values of elements, not the copy of the reference to (address on) the memory.
It is the same for character strings. 
In the Vnano, the "string" type which is the data type to store character strings behaves as the value type, not reference type.
In short, Vnano has no reference types, so all data types in the Vnano are value types.
Therefore, the script engine of the Vnano has no garbage-collection (GC) modules.

By the way, if sizes of arrays at the left-hand and the right-hand of the assignment operation (=) are different, 
the size of the left-hand array will be adjusted to the same size with the right-hand array, 
by re-allocating memory of the left-hand array automatically.


<a id="variable"></a>
## Variable Declaration Statements

You can describe the variable declaration statements with C-like syntax.


<a id="variable-scalar"></a>
## Declaration of scalar variables

The following is an example code of declaration statements of scalar variables (non-array variables) :

    int    i = 1;
    float  f = 2.3;
    bool   b = true;
    string s = "Hello, World !";

    print(i, f, b, s);

The result on [the command-line mode](FEATURE.md#command-line-mode) (standard plug-ins are required) is: 

    1    2.3    true    Hello, World !

However, you can NOT declare multiple variable in 1 statement in the Vnano:

    (!!! This code does not work !!!)

    int i, j;
    int n = 1, m = 2;


<a id="variable-array"></a>
## Declaration of arrays

You can declare and use arrays as follows:

    int a[8];
    a[2] = 123;
    print(a[2]);

The result on [the command-line mode](FEATURE.md#command-line-mode) is: 

    123

However, you can NOT use array initializers in the Vnano:

    (!!! This code does not work !!!)

    int a[8] = { 10, 20, 30, 40, 50, 60, 70, 80 };


<a id="control"></a>
## Control Statements

In control statements of C-like languages, Vnano supports if / else / for / while / continue / break statements.

<a id="control-if-else"></a>
## if and else statements

The folloing is an example code of if and else statements:

    int x = 1;
    if (x == 1) {
        print("x is 1.");
    } else {
        print("x is not 1.");
    }

The result is:

    x is 1.

By the way, in the Vnano, after of if / else / for / while statements must be a block statement {...}.
Therefore, you can NOT write single statement which is not enclosed by braces { } after the if statement as follows:

    (!!! This code does not work !!!)

    int x = 1;
    if (x == 1) print("x is 1.");


<a id="control-for"></a>
## for statement

The folloing is an example code of for statement:

    for (int i=1; i<=5; i++) {
        println("i=" + i);
    }


Please note that braces { } can not be omitted. The result is:

    i=1
    i=2
    i=3
    i=4
    i=5


<a id="control-while"></a>
## while statement

The folloing is an example code of while statement:

    int a = 500;
    while (0 <= a) {
        println("a=" + a);
        a -= 123;
    }

Please note that braces { } can not be omitted. The result is:

    a=500
    a=377
    a=254
    a=131
    a=8


<a id="control-break"></a>
## break statement

The folloing is an example code of break statement:

    for (int i=1; i<=10; i++) {
        println("i=" + i);
        if (i == 3) {
            break;
        }
    }

The result is:

    i=1
    i=2
    i=3

<a id="control-continue"></a>
## continue statement

The folloing is an example code of continue statement:

    for (int i=1; i<=10; i++) {
        if (i % 3 == 0) {
            continue;
        }
        println("i=" + i);
    }

The result is:

    i=1
    i=2
    i=4
    i=5
    i=7
    i=8
    i=10


<a id="expression"></a>
## Expressions

<a id="expression-syntax"></a>
## Syntax elements of expressions

The expression is the set of tokens consists of operators, leaf operands, and parentheses ( ), 
where leaf operands are literals, identifiers, and so on.
For example:

    (x + 2) * 3;

In the above expression, + and * are operators, x and 2 and 3 are leaf operands, 
( ) are parentheses.
In the Vnano, as the same with the C programming language, 
the symbol of the assignment "=" is an operator, so the following is also expression:

    y = (x + 2) * 3;

An expression alone can be a statement as "expression statement". 
In addition, an expression can be described as a part of other statements, e.g., a condition expression of an if statement.


<a id="expression-operator"></a>
## Operators

The following is the list of operators supported in the Vnano:

| Operator | Precedence | Syntax | Associativity | Type of Operands | Type of Operated Value |
| --- | --- | --- | --- | --- | --- |
| ( ... , ... , ... ) as call | 1000 | multiary | left | any | any |
| [ ... ][ ... ] ... as index | 1000 | multiary | left | int | any |
| ++ | 1000 | postfix | left | int | int |
| -- | 1000 | postfix | left | int | int |
| ++ | 2000 | prefix | right | int | int |
| -- | 2000 | prefix | right | int | int |
| + | 2000 | prefix | right | int | int |
| - | 2000 | prefix | right | int | int |
| ! | 2000 | prefix | right | bool | bool |
| (...) as cast | 2000 | prefix | right | any | any |
| * | 3000 | binary | left | int, float | int, float |
| / | 3000 | binary | left | int, float | int, float |
| % | 3000 | binary | left | int, float | int, float |
| + | 3100 | binary | left | int, float, string | int, float, string |
| - | 3100 | binary | left | int, float | int, float |
| < | 4000 | binary | left | int, float | bool |
| <= | 4000 | binary | left | int, float | bool |
| > | 4000 | binary | left | int, float | bool |
| >= | 4000 | binary | left | int, float | bool |
| == | 4100 | binary | left | any | bool |
| != | 4100 | binary | left | any | bool |
| && | 5000 | binary | left | bool | bool |
| \|\| | 5100 | binary | left | bool | bool |
| = | 6000 | binary | right | any | any |
| *= | 6000 | binary | right | int, float | int, float |
| /= | 6000 | binary | right | int, float | int, float |
| %= | 6000 | binary | right | int, float | int, float |
| += | 6000 | binary | right | int, float, string | int, float, string |
| -= | 6000 | binary | right | int, float | int, float |


The value type (the data-type of the operated value) of binary arithmetic operators (\*, /, %, +, -) 
and compound arithmetic assignment operators (*=, /=, %=, +=, -=) are decided by the following table:

| Type of Operand A | Type of Operand B | Type of Operated Value |
| --- | --- | --- |
| int | int | int |
| int | float | float |
| int | string | string |
| float | int | float |
| float | float | float |
| float | string | string |
| string | int | string |
| string | float | string |
| string | string | string |

Where you can choose the right or the left operand as the operand A (or operand B) freely in the above table.


<a id="function"></a>
## Functions

You can declare and call functions in the Vnano script code with C-like syntax. 
However, this script engine does not support recursive calls of functions, 
because allocations of local variables are implemented in very simple way.

<a id="function-scalar"></a>
## Scalar input/output functions


The following is an example code of the function of which arguments and the return value is scalar (non-array) values:

    int fun(int a, int b) {
        return a + b;
    }

    int v = fun(1, 2);
    print(v);

The result on [the command-line mode](FEATURE.md#command-line-mode) is: 

    3


<a id="function-array"></a>
## Array input/output functions

If you want to return an array, or get arrays as arguments, the following code is an example:

    int[] fun(int a[], int b[], int n) {
        int c[n];
        for (int i=0; i<n; i++) {
            c[i] = a[i] + b[i];
        }
        return c;
    }

    int x[3];
    x[0] = 0;
    x[1] = 1;
    x[2] = 2;

    int y[3];
    y[0] = 3;
    y[1] = 4;
    y[2] = 5;

    int z[] = fun(x, y, 3);

    println("z[0]=" + z[0]);
    println("z[1]=" + z[1]);
    println("z[2]=" + z[2]);

The result is:


    z[0]=3
    z[1]=5
    z[2]=7


Please note that, as we mentioned in the section of <a href="#data-type">Data Types</a>, 
arrays in the Vnano (and VCSSL) behaves as value types, not reference types or pointers.
By default, assignment operations of arguments and the return value behaves as the copy of all values of elements, not the copy of the reference to (address on) the memory 
(See also: <a href="#function-call-by-value">Call by value</a> and  
 <a href="#function-call-by-reference">Call by reference</a>).
In addition, the size of the array will be adjusted automatically when an array having different size will copied to it, 
so we omitted to specify size of array declarations in several places in the above code, e.g.: "int a[]", "int b[]", and "int z[] = fun(x, y, 3)".


<a id="function-params-and-args"></a>
## Formal parameters and actual arguments

The parameter-variable declared in a function declaration like as "a" in the following example is called as "formal parameter". In contrast, the value/variable passed to a function like as "x" in the following example is called as "actual argument".

    void fun(int a) {
        ...
    }

    ...

    fun(x);


<a id="function-call-by-value"></a>
## Call by value

By default, change of values of formal parameters in functions don't affect to values of actual arguments of caller-side, For example:

    void fun(int a, int b[]) {
        a = 2;
        b[0] = 10;
        b[1] = 11;
        b[2] = 12;
    }

    int x = 0;
    int y[3];
    y[0] = 0;
    y[1] = 0;
    y[2] = 0;

    fun(x, y);

    println("x = " + x);
    println("y[0] = " + y[0]);
    println("y[1] = " + y[1]);
    println("y[2] = " + y[2]);

The result is:

    x = 0
    y[0] = 0
    y[1] = 0
    y[2] = 0

As demonstrated by the above result, actual arguments of caller-side "a" and "b" have not changed although formal parameters "x" and "y" changed in the function "fun". This is because, by default, actual arguments will be simply copied once to formal parameters when the function is called. This behaviour is called as "call-by-value".


<a id="function-call-by-reference"></a>
## Call by reference

If you want to affect changed values of formal parameters in functions to values of actual arguments of caller-side, describe the symbol "&" before the name of formal parameters in declarations of them. For example:

    void fun(int &a, int &b[]) {
        a = 2;
        b[0] = 10;
        b[1] = 11;
        b[2] = 12;
    }

    int x = 0;
    int y[3];
    y[0] = 0;
    y[1] = 0;
    y[2] = 0;

    fun(x, y);

    println("x = " + x);
    println("y[0] = " + y[0]);
    println("y[1] = " + y[1]);
    println("y[2] = " + y[2]);

The result is:

    x = 2
    y[0] = 10
    y[1] = 11
    y[2] = 12

As demonstrated by the above result, the memory-reference to data of a formal parameter declared with "&" will be shared with reference to data of an actual argument, so after values of formal parameters "x" and "y" in the function "fun" changed, actual arguments "a" and "b" of caller-side also changed to same values with "x" and "y". This behaviour is called as "call-by-reference".
