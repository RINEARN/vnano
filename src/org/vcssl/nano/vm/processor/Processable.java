/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.processor;

import org.vcssl.nano.VnanoSyntaxException;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.memory.MemoryAccessException;

public interface Processable {

	public void process(Instruction[] instructions, Memory memory, Interconnect interconnect)
			throws VnanoSyntaxException, MemoryAccessException;

	public int process(Instruction instruction, Memory memory, Interconnect interconnect, int programCounter)
			throws VnanoSyntaxException, MemoryAccessException;

}
