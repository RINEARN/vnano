/**
 * The package of the top-layer (surface-layer) of the VM, of the scripting engine of the Vnano.
 *
 * The outer-frame of the VM provided by this package is {@link VirtualMachine VirtualMachine} class,
 * and other classes are internal components (packed in subpackages).
 *
 * The VM provided by this package executes a kind of intermediate code, named as "VRIL" code,
 * compiled from the script code of the Vnano by the {@link org.vcssl.nano.compiler compiler}.
 * VRIL ― Vector Register Intermediate Language ― is a low-level (but readable text format) language
 * designed as a virtual assembly code for this VM.
 * This VM internally assemble the VRIL code to more less-overhead format,
 * and then executes it on a kind of register machines.
 */
package org.vcssl.nano.vm;
