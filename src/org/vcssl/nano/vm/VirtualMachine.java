/*
 * Copyright(C) 2018-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm;

import java.io.PrintStream;
import java.util.Map;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.interconnect.DataConverter;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.spec.OptionKey;
import org.vcssl.nano.spec.OptionValue;
import org.vcssl.nano.vm.accelerator.Accelerator;
import org.vcssl.nano.vm.assembler.Assembler;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.processor.Instruction;
import org.vcssl.nano.vm.processor.Processor;

public class VirtualMachine {


	public Object eval(String assemblyCode, Interconnect interconnect, Map<String, Object> optionMap)
			throws VnanoException {

		// VRIL実行用途でアプリケーションから直接呼ばれる事も考えられるため、オプション内容の正規化を再度行っておく
		optionMap = OptionValue.normalizeValuesOf(optionMap);

		// オプションマップから指定内容を取得
		boolean acceleratorEnabled = (Boolean)optionMap.get(OptionKey.ACCELERATOR_ENABLED); // 高速版実装を使用するかどうか
		boolean shouldDump = (Boolean)optionMap.get(OptionKey.DUMPER_ENABLED);        // ダンプするかどうか
		String dumpTarget = (String)optionMap.get(OptionKey.DUMPER_TARGET);           // ダンプ対象
		boolean dumpTargetIsAll = dumpTarget.equals(OptionValue.DUMPER_TARGET_ALL);   // ダンプ対象が全てかどうか
		PrintStream dumpStream = (PrintStream)optionMap.get(OptionKey.DUMPER_STREAM); // ダンプ先ストリーム


		// アセンブラで中間アセンブリコード（VRILコード）から実行用のVMオブジェクトコードに変換
		Assembler assembler = new Assembler();
		VirtualMachineObjectCode intermediateCode = assembler.assemble(assemblyCode, interconnect);

		// VMオブジェクトコードをダンプ
		if (shouldDump && (dumpTargetIsAll || dumpTarget.equals(OptionValue.DUMPER_TARGET_OBJECT_CODE)) ) {
			if (dumpTargetIsAll) {
				dumpStream.println("================================================================================");
				dumpStream.println("= VM Object Code");
				dumpStream.println("= - Output of: org.vcssl.nano.vm.assembler.Assembler");
				dumpStream.println("= - Input  of: org.vcssl.nano.vm.processor.Processor");
				dumpStream.println("= -        or: org.vcssl.nano.vm.accelerator.Accelerator");
				dumpStream.println("================================================================================");
			}
			dumpStream.print(intermediateCode.dump());
			if (dumpTargetIsAll) {
				dumpStream.println("");
			}
		}


		// 実行用メモリー領域を確保し、外部変数のデータをロード
		Memory memory = new Memory();
		memory.allocate(intermediateCode, interconnect.getGlobalVariableTable());

		// プロセッサでVMオブジェクトコードの命令列を実行
		Instruction[] instructions = intermediateCode.getInstructions();
		Processor processor = new Processor();
		if (acceleratorEnabled) {
			Accelerator accelerator = new Accelerator();
			accelerator.process(instructions, memory, interconnect, processor, optionMap);
		} else {
			processor.process(instructions, memory, interconnect, optionMap);
		}

		// メモリーのデータをinterconnect経由で外部変数に書き戻す（このタイミングでBindings側が更新される）
		interconnect.writeback(memory, intermediateCode); // アドレスから変数名への逆変換に中間コードが必要

		// 処理結果（式の評価値やスクリプトの戻り値）を取り出し、外側のデータ型に変換して返す
		Object evalValue = this.getEvaluatedValue(memory, intermediateCode);
		return evalValue;
	}


	private Object getEvaluatedValue(Memory memory, VirtualMachineObjectCode intermediateCode) throws VnanoException {
		DataContainer<?> container = memory.getResultDataContainer();
		if (container == null) {
			return null;
		} else {
			return new DataConverter(container.getDataType(), container.getRank()).convertToExternalObject(container);
		}
	}

}
