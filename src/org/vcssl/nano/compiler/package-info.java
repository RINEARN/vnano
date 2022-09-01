/**
 * The package performing the function of a compiler in the script engine of the Vnano.
 *
 * The outer-frame of the compiler provided by this package is {@link Compiler} class,
 * and other classes are internal components.
 *
 * The compiler provided by this package compiles script code written in the Vnano
 * to a kind of intermediate code, named as "VRIL" assembly code.
 * VRIL (Vector Register Intermediate Language) is a low-level (but readable text format) language
 * designed as a virtual assembly code of the {@link org.vcssl.nano.vm VM} (Virtual Machine) layer of Vnano Engine.
 */
package org.vcssl.nano.compiler;
