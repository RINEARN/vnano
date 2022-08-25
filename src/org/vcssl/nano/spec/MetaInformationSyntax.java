/*
 * Copyright(C) 2020-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.processor.Instruction;

/**
 * The class to generate/interpret meta information attached to VM instructions.
 */
public class MetaInformationSyntax {

	/**
	 * Generates meta information in which specified line number and file name are embedded.
	 * 
	 * @param lineNumber The line number to be embedded in the meta information.
	 * @param lineNumber The file name to be embedded in the meta information.
	 * @return The generated meta information.
	 */
	public static String generateMetaInformation(int lineNumber, String fileName) {
		return "line=" + lineNumber + ", file=" + fileName;
	}


	/**
	 * Extracts the line number embedded in the meta information.
	 * 
	 * @param metaInformation The meta information in which the line number to be extracted is embedded.
	 * @return The extracted line number.
	 */
	public static int extractLineNumber(String metaInformation) {
		String[] items = metaInformation.split(",");
		for (String item: items) {
			if (item.trim().startsWith("line=")) {
				return Integer.parseInt(item.split("=")[1]);
			}
		}
		throw new VnanoFatalException("Invalid meta information: no line number found.");
	}


	/**
	 * Extracts the line number embedded in the meta information linked to the specified instruction.
	 * 
	 * @param instruction The instruction of which meta information contains the line number to be extracted.
	 * @param memory The memory in which data of meta information is stored.
	 * @return The extracted line number.
	 */
	public static int extractLineNumber(Instruction instruction, Memory memory) {
		DataContainer<?> metaContainer = memory.getDataContainer(
				instruction.getMetaPartition(), instruction.getMetaAddress()
		);
		String metaInformation = ((String[])metaContainer.getArrayData())[0];
		return extractLineNumber(metaInformation);
	}


	/**
	 * Extracts the file name embedded in the meta information.
	 * 
	 * @param metaInformation The meta information in which the file name to be extracted is embedded.
	 * @return The extracted file name.
	 */
	public static String extractFileName(String metaInformation) {
		String[] items = metaInformation.split(",");
		for (String item: items) {
			if (item.trim().startsWith("file=")) {
				return item.split("=")[1];
			}
		}
		throw new VnanoFatalException("Invalid meta information: no file name found.");
	}


	/**
	 * Extracts the file name embedded in the meta information linked to the specified instruction.
	 * 
	 * @param instruction The instruction of which meta information contains the file name to be extracted.
	 * @param memory The memory in which data of meta information is stored.
	 * @return The extracted file name.
	 */
	public static String extractFileName(Instruction instruction, Memory memory) {
		DataContainer<?> metaContainer = memory.getDataContainer(
				instruction.getMetaPartition(), instruction.getMetaAddress()
		);
		String metaInformation = ((String[])metaContainer.getArrayData())[0];
		return extractFileName(metaInformation);
	}

}
