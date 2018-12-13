package org.vcssl.nano.vm;

import org.vcssl.nano.VnanoSyntaxException;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.vm.accelerator.Accelerator;
import org.vcssl.nano.vm.assembler.Assembler;
import org.vcssl.nano.vm.assembler.AssemblyCodeException;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.DataConverter;
import org.vcssl.nano.vm.memory.DataException;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.memory.MemoryAccessException;
import org.vcssl.nano.vm.processor.Instruction;
import org.vcssl.nano.vm.processor.InvalidInstructionException;
import org.vcssl.nano.vm.processor.Processor;

public class VirtualMachine {

	private boolean acceleratorEnabled = true;

	public Object eval(String assemblyCode, Interconnect interconnect)
			throws VnanoSyntaxException, AssemblyCodeException, DataException, MemoryAccessException, InvalidInstructionException {

		// アセンブラで中間アセンブリコード（VRILコード）から実行用の中間コードに変換
		Assembler assembler = new Assembler();
		VirtualMachineObjectCode intermediateCode = assembler.assemble(assemblyCode, interconnect);

		// 実行用メモリー領域を確保し、外部変数のデータをロード
		Memory memory = new Memory();
		memory.allocate(intermediateCode, interconnect.getGlobalVariableTable());

		// VMで中間コードの命令列を実行
		Instruction[] instructions = intermediateCode.getInstructions();
		Processor processor = new Processor();
		if (this.acceleratorEnabled) {
			Accelerator accelerator = new Accelerator();
			accelerator.process(instructions, memory, interconnect, processor);
		} else {
			processor.process(instructions, memory, interconnect);
		}

		// メモリーのデータをinterconnect経由で外部変数に書き戻す（このタイミングでBindings側が更新される）
		interconnect.writeback(memory, intermediateCode); // アドレスから変数名への逆変換に中間コードが必要

		// 処理結果（式の評価値やスクリプトの戻り値）を取り出し、外側のデータ型に変換して返す
		Object evalValue = this.getEvaluatedValue(memory, intermediateCode);
		return evalValue;
	}



	private Object getEvaluatedValue(Memory memory, VirtualMachineObjectCode intermediateCode)
			throws MemoryAccessException, DataException {

		if (intermediateCode.hasEvalValue()) {
			int evalValueAddress = intermediateCode.getEvalValueAddress();
			DataContainer<?> container = memory.getDataContainer(Memory.Partition.LOCAL, evalValueAddress);
			return new DataConverter(container.getDataType(), container.getRank()).convertToExternalObject(container);
		} else {
			return null;
		}
	}

}