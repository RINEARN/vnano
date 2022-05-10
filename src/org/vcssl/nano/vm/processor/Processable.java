/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.processor;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.vm.memory.Memory;

public interface Processable {

	public void process(
		Instruction[] instructions, Memory memory, Interconnect interconnect
	) throws VnanoException;

	public int process(
		Instruction instruction, Memory memory, Interconnect interconnect, int programCounter
	) throws VnanoException;

}
