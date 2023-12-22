
/**
 * The package takes on the memory in the virtual machine of the Vnano.
 *
 * The outer-frame of the memory provided by this package is {@link Memory} class.
 * The architecture of the VM of the Vnano is a kind of a vector processor,
 * so the unit of the memory provided by this package is an array, not a scalar.
 *
 * The memory internally has lists of data-containers (instances of {@link DataContainer} class),
 * and each data-container can store an array data.
 * An unique address is assigned for each data-container.
 *
 * The {@link DataContainer} class is also used for passing/receiving data
 * between various components in the scripting engine of the Vnano.
 */
package org.vcssl.nano.vm.memory;
