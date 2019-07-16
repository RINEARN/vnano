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

/**
 * <p>
 * <span class="lang-en">
 * The class performing the function of the VM of the script engine of the Vnano
 * </span>
 * <span class="lang-ja">
 * Vnanoのスクリプトエンジン内で, VM（仮想マシン）の機能を担うクラスです
 * </span>
 * .
 * </p>
 *
 * <p>
 * <span class="lang-en">
 * This class executes a kind of intermediate code, named as "VRIL" code,
 * compiled from the script code of the Vnano by the {@link org.vcssl.nano.compiler compiler}.
 * VRIL ― Vector Register Intermediate Language ― is a low-level (but readable text format) language
 * designed as a virtual assembly code for this VM.
 * This class internally assemble the VRIL code to more less-overhead format,
 * and then executes it on a kind of register machines.
 * </span>
 *
 * <span class="lang-ja">
 * このクラスは, Vnano のスクリプトコードから {@link org.vcssl.nano.compiler コンパイラ}
 * によってコンパイルされた, "VRILコード" と呼ぶ一種の中間コードを実行します.
 * VRIL（Vector Register Intermediate Language; ベクトルレジスタ中間言語）は,
 * このVMの単位動作に対応するレベルの低抽象度な命令を提供する,  仮想的なアセンブリ言語です.
 * VRILコードは, 実在のアセンブリコードと同様に, 人間にとって可読なテキスト形式のコードです.
 * このクラスは, 内部でVRILコードをより低オーバーヘッドな形にアセンブルした上で,
 * 一種のレジスタマシン上で実行します.
 * </span>
 * </p>
 *
 * <p>
 * &raquo <a href="../../../../../src/org/vcssl/nano/vm/VirtualMachine.java">Source code</a>
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class VirtualMachine {

	/**
	 * <span class="lang-en">
	 * This constructor does nothing, because this class has no fields for storing state
	 * </span>
	 * <span class="lang-ja">
	 * このクラスは状態を保持するフィールドを持たないため, コンストラクタは何もしません
	 * </span>
	 * .
	 */
	public VirtualMachine() {
	}


	/**
	 * <span class="lang-en">
	 * Executes virtual assembly code written in VRIL (VRIL code)
	 * </span>
	 * <span class="lang-ja">
	 * VRILで記述された仮想アセンブリコード（VRILコード）を実行します
	 * </span>
	 * .
	 *
	 * @param assemblyCode
	 *   <span class="lang-en">Virtual assembly code written in VRIL (VRIL code) to be executed.</span>
	 *   <span class="lang-ja">実行対象の, VRILで記述された仮想アセンブリコード（VRILコード）.</span>
	 *

	 * @param interconnect
	 *   <span class="lang-en">The interconnect to which external functions/variables are connected.</span>
	 *   <span class="lang-ja">外部変数/関数が接続されているインターコネクト.</span>
	 *
	 * @param optionMap
	 *   <span class="lang-en">The Map (option map) storing names and values of options.</span>
	 *   <span class="lang-ja">オプションの名前と値を格納するマップ（オプションマップ）.</span>
	 *
	 * @return
	 *   <span class="lang-en">
	 *   The value specified by {@link org.vcssl.nano.spec.OperationCode#END END} instruction at the end of VRIL code.
	 *   If no value is specified, returns null.
	 *   </span>
	 *   <span class="lang-ja">
	 *   VRILコード内の終端にある {@link org.vcssl.nano.spec.OperationCode#END END} 命令で指定された値.
	 *   値が指定されなかった場合は null が返されます.
	 *   </span>
	 *
	 * @throws VnanoException
	 *   <span class="lang-en">Thrown when a runtime error is occurred.</span>
	 *   <span class="lang-ja">実行時エラーが発生した場合にスローされます.</span>
	 */
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
		memory.allocate(intermediateCode, interconnect.getExternalVariableTable());

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
		interconnect.writebackExternalVariables(memory, intermediateCode); // アドレスから変数名への逆変換に中間コードが必要

		// 処理結果（式の評価値やスクリプトの戻り値）を取り出し、外側のデータ型に変換して返す
		if (memory.hasResultDataContainer()) {
			DataContainer<?> resultDataContainer = memory.getResultDataContainer();
			DataConverter converter = new DataConverter(resultDataContainer.getDataType(), resultDataContainer.getRank());
			return converter.convertToExternalObject(resultDataContainer);
		} else {
			return null;
		}
	}

}
