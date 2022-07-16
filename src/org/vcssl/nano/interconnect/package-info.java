/**
 * The package performing functions to manage and to provide some information
 * shared between multiple components in the script engine of the Vnano.
 * We refer this component as "Interconnect" in the script engine of the Vnano Engine.
 *
 * The outer-frame of the interconnect provided by this package is {@link Interconnect Interconnect} class,
 * and others are internal components.
 *
 * For example, classes and interfaces to resolve references of variables and functions
 * are managed by this interconnect package.
 * Bindings to external functions/variables are intermediated by {@link Interconnect Interconnect} class,
 * so plug-ins of external functions/variables will be connected to it.
 */
package org.vcssl.nano.interconnect;

