/*
 * Copyright(C) 2020-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.main;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.spec.PerformanceKey;
import org.vcssl.nano.vm.VirtualMachine;


/**
 * The class for printing measured performance values in realtime.
 */
public class PerformanceValuePrinter implements Runnable {

	// Note: The precision of the counter of executed instructions is "int", so its max value is about 2 Giga counts.
	// The max processing speed of this script engine is near the 1 Giga instructions per sec.
	// Hence we should measure performance values for 10 ~ 100 times per sec, and we should reset the counter for each measurement.
	// Printed results by this class are 1-sec averages of the above frequently measured values.

	/** The rough value of the measurement count per 1-sec. The value must be smaller than 1000. */
	private static final int PROC_CNT_GETS_PER_SEC = 100;

	/** The rough value of the measurement interval [millisec]. */
	private static final int PROC_CNT_INTERVAL_WAIT = 1000 / PROC_CNT_GETS_PER_SEC;

	/** The sum of measured IPS (Instruction per sec) score. This sum value will be used for computing 1-sec avelage. */
	private double procIpsSum = 0.0;

	/** The total count of sampled operation codes for the profiling. */
	private long operationCodeCountTotal;

	/**
	 * The Map mapping each operation code to detected count of it.
	 * Note that, when multiple operation codes are detected at the same moment,
	 * their count will be incremented 1/N, where N is the number of operation codes detected at the same moment.
	 */
	private HashMap<String, Double> operationCodeCountMap;

	/** The flag representing whether the VM speed should be printed. */
	private boolean printsVmSpeed = false;

	/** The flag representing whether the RAM usage should be printed. */
	private boolean printsRamUsage = false;

	/** The flag representing whether the instruction executed frequency should be printed. */
	private boolean printsInstructionFrequency = false;

	/** The flag representing whether we should continue to measure performance. Set false when quit to measure/print. */
	private volatile boolean continuable = true;

	/** Stores the reference to the script engine. */
	private VnanoEngine vnanoEngine = null;

	/** Stores the reference to the virtual machine. */
	private VirtualMachine virtualMachine = null;


	/**
	 * Create an instance for printing performances of the VnanoEngine.
	 *
	 * @param vnanoEngine The VnanoEngine of which performances will be measured/printed.
	 * @param printsVmSpeed Specify true to measure/print the VM drive speed.
	 * @param printsRamUsage Specify true to measure/print the RAM usage.
	 * @param printsInstructionFrequency Specify true to measure/print the executed frequencies of operation codes of instructions.
	 */
	public PerformanceValuePrinter(VnanoEngine vnanoEngine,
			boolean printsVmSpeed, boolean printsRamUsage, boolean printsInstructionFrequency) {

		this.printsVmSpeed = printsVmSpeed;
		this.printsRamUsage = printsRamUsage;
		this.printsInstructionFrequency = printsInstructionFrequency;
		this.operationCodeCountMap = new HashMap<String, Double>();
		this.operationCodeCountTotal = 0L;

		this.vnanoEngine = vnanoEngine;
	}


	/**
	 * Create an instance for printing performances of the VirtualMachine.
	 *
	 * @param vnanoEngine The VnanoEngine of which performances will be measured/printed.
	 * @param printsVmSpeed Specify true to measure/print the VM drive speed.
	 * @param printsRamUsage Specify true to measure/print the RAM usage.
	 * @param printsInstructionFrequency Specify true to measure/print the executed frequencies of operation codes of instructions.
	 */
	public PerformanceValuePrinter(VirtualMachine virtualMachine,
			boolean printsVmSpeed, boolean printsRamUsage, boolean printsInstructionFrequency) {

		this.printsVmSpeed = printsVmSpeed;
		this.printsRamUsage = printsRamUsage;
		this.printsInstructionFrequency = printsInstructionFrequency;
		this.operationCodeCountMap = new HashMap<String, Double>();
		this.operationCodeCountTotal = 0L;

		this.virtualMachine = virtualMachine;
	}


	/**
	 * Terminates measuring/printing performances.
	 */
	public void terminate() {
		this.continuable = false;
	}


	/**
	 * The measuring/printing loop, which runs on an independent thread.
	 */
	@Override
	public void run() {

		// The counter of processed cycles of the measuring/printing loop.
		int loopCount = 0;

		// Variables for storing counter/timer values measured at the last cycle.
		int lastProcCount = 0;
		long lastNanoTime = System.nanoTime();

		// The measuring/printing loop.
		while (this.continuable) {

			// Wait a short time (PROC_CNT_INTERVAL_WAIT) for each cycle.
			try {
				Thread.sleep(PROC_CNT_INTERVAL_WAIT);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
				break;
			}


			// The counter of the currently executed instructions (should be reset before overflowing).
			int currentProcCount = 0;

			// The names of the currently executed instructions.
			String[] currentOpcodeNames = new String[0];

			// When we should measure/print performances of the VnanoEngine.
			// (When we are executing a Vnano script.)
			if (this.vnanoEngine != null && this.vnanoEngine.hasPerformanceMap()) {
				Map<String, Object> performanceMap = this.vnanoEngine.getPerformanceMap();

				// Note: When no performance value has not been measured yet, no value is stored to the performance map.
				if (performanceMap.containsKey(PerformanceKey.EXECUTED_INSTRUCTION_COUNT_INT_VALUE)) {
					currentProcCount = (int)performanceMap.get(PerformanceKey.EXECUTED_INSTRUCTION_COUNT_INT_VALUE);
				}
				if (performanceMap.containsKey(PerformanceKey.CURRENTLY_EXECUTED_OPERATION_CODE)) {
					currentOpcodeNames = (String[])performanceMap.get(PerformanceKey.CURRENTLY_EXECUTED_OPERATION_CODE);
				}
			}

			// When we should measure/print performances of the VirtualMachine.
			// (When we are executing a VRIL assembly code.)
			if (this.virtualMachine != null) {

				// Note: The following methods of the VM always return values, even when no performance value has not been measured yet.
				currentProcCount = this.virtualMachine.getExecutedInstructionCountIntValue(); // Returns 0 when no value has not been measured.
				OperationCode[] currentOpcodes = this.virtualMachine.getCurrentlyExecutedOperationCodes(); // Returns an empty array when no value has not been measured.
				currentOpcodeNames = new String[ currentOpcodes.length ];
				for (int i=0; i<currentOpcodes.length; i++) {
					currentOpcodeNames[i] = currentOpcodes[i].toString();
				}
			}


			// Add the value of the counter of the instruction execution frequency (stored in the operationCodeCountMap).
			if (currentOpcodeNames.length != 0) {

				// When only 1 operation code isdetected, we add 1 to its counter.
				// When multiple operation codes are detected at the same moment,
				// we add 1/N to their counters, where N is the number of operation codes detected at the same moment.
				double opcodeCountWeight = 1.0 / currentOpcodeNames.length;

				for (String opcode: currentOpcodeNames) {
					double latestValue = this.operationCodeCountMap.containsKey(opcode) ?
							this.operationCodeCountMap.get(opcode).doubleValue() : 0.0;
					double updatedValue = latestValue + opcodeCountWeight;
					this.operationCodeCountMap.put(opcode, Double.valueOf(updatedValue));
				}
			}
			this.operationCodeCountTotal += currentOpcodeNames.length;


			// Calculate the value of Instructions-Per-Second (IPS).
			long currentNanoTime = System.nanoTime();
			double countIntervalSec = (currentNanoTime - lastNanoTime) * 1.0E-9;
			double procIps = (currentProcCount - lastProcCount) / countIntervalSec;

			// To prevent overflow, we get and reset the value of the counter frequently, and IPS value to the following variable.
			// Then, at each timing to print results, we calculate the average of IPS from the following sum value, and print it.
			this.procIpsSum += procIps;

			// Store values measured at this cycle.
			lastProcCount = currentProcCount;
			lastNanoTime = currentNanoTime;
			loopCount++;

			// When the processed cycles has reached to the printing interval cycles, print results and reset counters.
			if (loopCount == PROC_CNT_GETS_PER_SEC) {
				System.out.println("================================================================================");
				String timestamp = new Timestamp(System.currentTimeMillis()).toString();
				System.out.println("= Performance Monitor (" + timestamp + ")");

				if (this.printsVmSpeed) {
					double procIpsAverage = this.procIpsSum / PROC_CNT_GETS_PER_SEC;
					String formattedIps = this.formatIpsValue(procIpsAverage);
					System.out.println("= - VM Speed  = " + formattedIps);
				}

				if (this.printsRamUsage) {
					long ramTotalBytes = Runtime.getRuntime().totalMemory();
					long ramFreeBytes = Runtime.getRuntime().freeMemory();
					long ramMaxBytes = Runtime.getRuntime().maxMemory();
					String formattedRamUsage = this.formatRamBytes(ramTotalBytes - ramFreeBytes);
					String formattedRamMax = this.formatRamBytes(ramMaxBytes);
					System.out.println("= - RAM Usage = " + formattedRamUsage + " (Max " + formattedRamMax + " Available)");
				}

				if (this.printsInstructionFrequency) {
					String formattedInstructionFrequency = this.formatInstructionFrequency(this.operationCodeCountMap);
					System.out.println("= - Instruction Execution Frequency :");
					System.out.print(formattedInstructionFrequency);
					System.out.println("    (Total " + this.operationCodeCountTotal + " Samples)");
				}

				this.procIpsSum = 0.0;
				loopCount = 0;
				System.out.println("================================================================================");
			}
		}
	}

	// Format the value of the VM drive speed to be printed.
	private String formatIpsValue(double ipsRawValue) {
		double value = ipsRawValue;
		String unitPrefix = "";
		if (value > 1000L*1000L*1000L) {
			unitPrefix = "G";
			value /= 1000L*1000L*1000L;
		} else if (value > 1000L*1000L) {
			unitPrefix = "M";
			value /= 1000L*1000L;
		} else if (value > 1000L) {
			unitPrefix = "K";
			value /= 1000L;
		}
		DecimalFormat formatter = new DecimalFormat("0.0");
		String formattedValue = formatter.format(value) + " " + unitPrefix + "Hz (VRIL Instructions / sec)";
		return formattedValue;
	}

	// Format the value of the RAM usage to be printed..
	private String formatRamBytes(long ramBytes) {
		double value = ramBytes;
		String unitPrefix = "";
		if (value > 1024L*1024L*1024L) {
			unitPrefix = "Gi";
			value /= 1024L*1024L*1024L;
		} else if (value > 1024L*1024L) {
			unitPrefix = "Mi";
			value /= 1024L*1024L;
		} else if (value > 1024L) {
			unitPrefix = "Ki";
			value /= 1024L;
		}
		DecimalFormat formatter = new DecimalFormat("0.0");
		String formattedValue = formatter.format(value) + " " + unitPrefix + "B";
		return formattedValue;
	}

	/**
	 * Format the instruction execution frequencies to be printed.
	 *
	 * @param operationCodeCountMap The map in which detected counters of operation codes are stored.
	 */
	private String formatInstructionFrequency(HashMap<String, Double> operationCodeCountMap) {
		StringBuilder builder = new StringBuilder();
		String eol = System.getProperty("line.separator");

		// Clone the map in which detected counters of  operation codes are stored.
		@SuppressWarnings("unchecked")
		HashMap<String, Double> modifiedMap = (HashMap<String, Double>)operationCodeCountMap.clone();
		Set<Map.Entry<String, Double>> modifiedEntrySet = modifiedMap.entrySet();

		// Sort entries (key-value pairs) in the map in the descending order of counters.
		List<Map.Entry<String, Double>> modifiedEntryList = new ArrayList<>(modifiedEntrySet);
		Collections.sort(modifiedEntryList, new CountEntryComparator());

		// Convert conters to frequencies (from 0 to 1).
		double total = 0.0;
		for (Map.Entry<String, Double> modifiedEntry: modifiedEntryList) {
			total += modifiedEntry.getValue().doubleValue();
		}
		for (Map.Entry<String, Double> modifiedEntry: modifiedEntryList) {
			modifiedEntry.setValue( Double.valueOf(modifiedEntry.getValue().doubleValue() / total) );
		}

		// Formatters to format instruction execution frequencies / counters.
		DecimalFormat percentageFormatter = new DecimalFormat("0.00");
		DecimalFormat countFormatter = new DecimalFormat("0.#"); // Note: This value may have fraction part.

		// Convert and print frequencies.
		for (Map.Entry<String, Double> modifiedEntry: modifiedEntryList) {

			// Align lengths of operation codes to be printed, by appending spaces to end of them.
			// The length of the longest operation code is 7 chars (REFELEM and so on), so align to the 7 chars.
			String opcode = String.format("%7s", modifiedEntry.getKey().toString());

			// Convert to each frequency (from 0 to 1) to a percentage, and round it.
			String percentage = percentageFormatter.format( modifiedEntry.getValue().doubleValue() * 100.0 ); // Round the double value.
			percentage = String.format("%6s", percentage); // Align the length of chars.

			// Raw counter value.
			String count = countFormatter.format( operationCodeCountMap.get(modifiedEntry.getKey()) );

			// Print the above values.
			builder.append("    - " + opcode + " : " + percentage + " %   (" + count + " count)" + eol);
		}
		return builder.toString();
	}

	/**
	 * The comparator for sorting entries in the map storing counters of operation codes.
	 */
	class CountEntryComparator implements Comparator<Map.Entry<String, Double>> {
		@Override
		public int compare(Map.Entry<String, Double> a, Map.Entry<String, Double> b) {
			//double diff = a.getValue() - b.getValue(); // Ascending order.
			double diff = b.getValue() - a.getValue(); // Descending order.
			return (int)Math.signum(diff);

			// Note: The return value of the signum(...) is double,
			//   but it takes only values of 0 or 1, and it can be completely expressed in binary.
			//   So we can cast it to int without any numerical errors.
		}
	}

}
