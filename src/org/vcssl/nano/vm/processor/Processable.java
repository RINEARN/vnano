/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.processor;

import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.memory.DataException;
import org.vcssl.nano.memory.Memory;
import org.vcssl.nano.memory.MemoryAccessException;

public interface Processable {

	public void process(Instruction[] instructions, Memory memory, Interconnect interconnect)
			throws InvalidInstructionException, DataException, MemoryAccessException;

	public int process(Instruction instruction, Memory memory, Interconnect interconnect, int programCounter)
			throws InvalidInstructionException, DataException, MemoryAccessException;

}
