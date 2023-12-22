/**
 * The package takes on the function of a processor, in the virtual machine of the Vnano.
 *
 * The outer-frame of the compiler provided by this package is {@link Processor} class,
 * and other classes are internal components.
 *
 * The processor provided by this package executes instructions assembled by a VRIL assembly code.
 * Each instruction is expressed by an instance of the {@link Instruction} class.
 * Data I/O will be performed from/to an instance of the {@link org.vcssl.nano.vm.memory.Memory} class.
 */
package org.vcssl.nano.vm.processor;
