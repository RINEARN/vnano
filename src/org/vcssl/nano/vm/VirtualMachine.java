/*
 * Copyright(C) 2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm;

import java.util.Map;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.interconnect.DataConverter;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.spec.OptionName;
import org.vcssl.nano.spec.OptionValue;
import org.vcssl.nano.vm.accelerator.Accelerator;
import org.vcssl.nano.vm.assembler.Assembler;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.processor.Instruction;
import org.vcssl.nano.vm.processor.Processor;

public class VirtualMachine {

	/** Accelerator（高速版VM）の有効/無効設定のデフォルト値です。 */
	private static final boolean ACCELERATOR_ENABLED_DEFAULT_VALUE = true;


	public Object eval(String assemblyCode, Interconnect interconnect, Map<String, Object> optionMap)
			throws VnanoException {

		// オプションマップから、Accelerator（高速VM）を使用するかどうかの設定を取得
		// （デフォルト値は、もっと上の層でオプションマップ自体に入れておくべき？ デフォルト値の定義も別の場所で。後で要検討）
		boolean acceleratorEnabled = ACCELERATOR_ENABLED_DEFAULT_VALUE;
		if (optionMap.containsKey(OptionName.ACCELERATOR)) {
			OptionValue.booleanValueOf(OptionName.ACCELERATOR, optionMap);
		}

		// アセンブラで中間アセンブリコード（VRILコード）から実行用の中間コードに変換
		Assembler assembler = new Assembler();
		VirtualMachineObjectCode intermediateCode = assembler.assemble(assemblyCode, interconnect);

		// 実行用メモリー領域を確保し、外部変数のデータをロード
		Memory memory = new Memory();
		memory.allocate(intermediateCode, interconnect.getGlobalVariableTable());

		// VMで中間コードの命令列を実行
		Instruction[] instructions = intermediateCode.getInstructions();
		Processor processor = new Processor();
		if (acceleratorEnabled) {
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


	private Object getEvaluatedValue(Memory memory, VirtualMachineObjectCode intermediateCode) throws VnanoException {
		DataContainer<?> container = memory.getResultDataContainer();
		if (container == null) {
			return null;
		} else {
			return new DataConverter(container.getDataType(), container.getRank()).convertToExternalObject(container);
		}
	}

}
