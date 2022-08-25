/*
 * Copyright(C) 2020-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

/**
 * The class to defining keys of the performance map (performance monitoring item names).
 */
public class PerformanceKey {

	/**
	 * The total number of processed instructions from when the monitoring target engine was instantiated.
	 * 
	 * The measured value of this monitoring item is "Integer" type.
	 *
	 * Note that, to lighten the decreasing of the performance caused by the counting/monitoring,
	 * the gotten value of this monitoring item may be a cached old value of the counter by the caller thread,
	 * so the precision of it is not perfect.
	 * Also, please note that, when the counter value exceeds the positive maximum value of the int-type,
	 * it will not be reset to 0, and it will be the negative maximum value (minimum value on the number line)
	 * of the int-type, and will continue to be incremented from that value.
	 * For the above reason, it is recommended to get this value frequently enough
	 * (for example, --perf option of the command-line mode of the Vnano gets this value about 100 times per second),
	 * and use differences between them, not a raw value.
	 */
	// Note: Don't remove _INT_, because maybe we will support _LONG_VALUE in future.
	public static final String EXECUTED_INSTRUCTION_COUNT_INT_VALUE = "EXECUTED_INSTRUCTION_COUNT_INT_VALUE";


	/**
	 * Operation code(s) of currently executed instruction(s) on the VM in the monitoring target engine.
	 * 
	 * The measured value of this monitoring item is "String[]" type.
	 * The value of this item is an array, because generary a VM may execute multiple instructions in 1 cycle.
	 * Also, when no instructins are being executed, the value of this item is an empty array.
	 */
	public static final String CURRENTLY_EXECUTED_OPERATION_CODE = "CURRENTLY_EXECUTED_OPERATION_CODE";

}
