/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;


import java.io.PrintStream;
import java.util.Map;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.MetaInformationSyntax;
import org.vcssl.nano.spec.OptionKey;
import org.vcssl.nano.spec.OptionValue;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.processor.Instruction;
import org.vcssl.nano.vm.processor.Processor;


/**
 * <p>
 * <span class="lang-en">
 * The class of accelerator to process some instructions faster
 * at the upper layer of {@link Processor Processor}
 * in in case of that some options are enabled
 * </span>
 * <span class="lang-ja">
 * オプション設定に応じて使用される、
 * 特定の条件が揃った命令を {@link Processor Processor}
 * よりも上流で高速実行するアクセラレータのクラスです。
 * </span>
 * </p>
 *
 * <p>
 * Vnano処理系は、全体的にはパフォーマンスよりも実装の簡素さを優先していますが、
 * このクラスは、それでは処理速度が不足してしまう用途に対応するために存在します。
 * そのため、このクラスの実装コードでは、簡素さや保守性よりも処理速度が最も優先されています。
 * その代わりとして、Vnano処理系は、このクラスを全く使用しなくても、機能上は完全に成立するようにできています。
 * Vnano処理系をカスタマイズする際などには、このクラスは除外しておく方が無難です。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class Accelerator {

	/**
	 * このクラスは定数以外のフィールドを持たないため、コンストラクタは何もしません。
	 */
	public Accelerator() {
	}


	/**
	 * 命令配列に含まれる各命令を逐次実行します。
	 *
	 * 呼び出し側から見た機能としては、{@link ControllUnit#process} メソッドと同様です。
	 * ただし、高速実行の対象外となる命令に対しては、このクラス内では処理を行わず、
	 * そのまま下層の {@link ControllUnit#dispatch} メソッドに投げます。
	 *
	 * @param instructions 実行対象の命令配列
	 * @param memory データの入出力に用いる仮想メモリー
	 * @param interconnect 外部関数プラグインが接続されているインターコネクト（呼び出しに使用）
	 * @param processor 高速実行の対象外の命令を処理する仮想プロセッサ
	 * @param optionMap オプション内容を保持するマップ
	 * @throws InvalidInstructionException
	 * 		このコントロールユニットが対応していない命令が実行要求された場合や、
	 * 		オペランドの数が期待値と異なる場合など、命令内容が不正である場合に発生します。
	 * @throws MemoryAccessException
	 * 		命令のオペランドに指定された仮想メモリーアドレスが使用領域外であった場合など、
	 * 		不正な仮想メモリーアクセスが生じた場合などに発生します。
	 */
	public void process(Instruction[] instructions, Memory memory, Interconnect interconnect, Processor processor,
			Map<String, Object> optionMap) throws VnanoException {

		// オプションマップから指定内容を取得
		boolean shouldDump = (Boolean)optionMap.get(OptionKey.DUMPER_ENABLED);        // ダンプするかどうか
		String dumpTarget = (String)optionMap.get(OptionKey.DUMPER_TARGET);           // ダンプ対象
		boolean dumpTargetIsAll = dumpTarget.equals(OptionValue.DUMPER_TARGET_ALL);   // ダンプ対象が全てかどうか
		PrintStream dumpStream = (PrintStream)optionMap.get(OptionKey.DUMPER_STREAM); // ダンプ先ストリーム
		boolean shouldRun = (Boolean)optionMap.get(OptionKey.RUNNING_ENABLED);        // コードを実行するかどうか


		// スカラ判定やキャッシュ確保などの高速化用データ解析を実行
		AcceleratorDataManagementUnit dataManager = new AcceleratorDataManagementUnit();
		dataManager.allocate(instructions, memory);

		// 命令スケジューラで命令列を高速化用に再配置・変換
		AcceleratorSchedulingUnit scheduler = new AcceleratorSchedulingUnit();
		AcceleratorInstruction[] acceleratorInstructions = scheduler.schedule(instructions, memory, dataManager);

		// 変換後の命令列をダンプ
		if (shouldDump && (dumpTargetIsAll || dumpTarget.equals(OptionValue.DUMPER_TARGET_ACCELERATOR_CODE)) ) {
			if (dumpTargetIsAll) {
				dumpStream.println("================================================================================");
				dumpStream.println("= Accelerator Code");
				dumpStream.println("= - Output of: org.vcssl.nano.vm.accelerator.AcceleratorSchedulingUnit");
				dumpStream.println("= - Input  of: org.vcssl.nano.vm.accelerator.AcceleratorDispatchUnit");
				dumpStream.println("================================================================================");
			}
			int acceleratorInstructionLength = acceleratorInstructions.length;
			for (int i=0; i<acceleratorInstructionLength; i++) {
				int unreorderedAddr = acceleratorInstructions[i].getUnreorderedAddress();
				dumpStream.println("[" + i + "] <- [" + unreorderedAddr + "]\t" + acceleratorInstructions[i]);
			}
			if (dumpTargetIsAll) {
				dumpStream.println("");
			}
		}

		// アクセラレータで対応していない命令を、そのまま下層の仮想プロセッサに投げる、バイパス演算ユニットを生成
		BypassUnit bypassUnit = new BypassUnit(processor, memory, interconnect);

		// コールスタックやアドレスなどを統合的に管理しつつ、内部関数関連の命令を実行する、内部関数演算ユニットを生成
		InternalFunctionControlUnit functionControlUnit = new InternalFunctionControlUnit();

		// 命令列をアクセラレータ内の演算器に割り当てて演算実行ノード列を生成
		AcceleratorDispatchUnit dispatcher = new AcceleratorDispatchUnit();
		AcceleratorExecutionNode[] nodes = dispatcher.dispatch(
				processor, memory, interconnect, acceleratorInstructions, dataManager, bypassUnit, functionControlUnit
		);

		// 演算実行ノード列をダンプ
		if (shouldDump && (dumpTargetIsAll || dumpTarget.equals(OptionValue.DUMPER_TARGET_ACCELERATOR_STATE)) ) {
			if (dumpTargetIsAll) {
				dumpStream.println("================================================================================");
				dumpStream.println("= Accelerator State (Execution Unit Dispatchment)");
				dumpStream.println("= - Output of: org.vcssl.nano.vm.accelerator.AcceleratorDispatchUnit");
				dumpStream.println("================================================================================");
			}
			for (int i=0; i<nodes.length; i++) {
				dumpStream.println("[" + i + "]\t" + nodes[i]);
			}
			if (dumpTargetIsAll) {
				dumpStream.println("");
			}
		}


		// 内部関数のリターンは、スタック上に動的に積まれた命令アドレスに飛ぶため、全命令のノードを保持する必要がある
		functionControlUnit.setNodes(nodes);

		// キャッシュにメモリのデータを書き込む
		dataManager.getCacheSynchronizers(Memory.Partition.CONSTANT).synchronizeFromMemoryToCache();
		dataManager.getCacheSynchronizers(Memory.Partition.GLOBAL).synchronizeFromMemoryToCache();

		// オプションでコード実行が無効化されていた場合はここで終了
		if (!shouldRun) {
			return;
		}

		// ダンプ内容に実行開始点を表す区切りを入れる
		if (shouldDump && dumpTargetIsAll) {
			dumpStream.println("================================================================================");
			dumpStream.println("= Run");
			dumpStream.println("================================================================================");
		}


		// 以下、命令の逐次実行ループ
		AcceleratorExecutionNode nextNode = null;
		if (nodes.length != 0) {
			nextNode = nodes[0];
		}
		try {
			while (nextNode != null) {
				nextNode = nextNode.execute();
			}

		} catch (Exception e) {

			// 命令のメタ情報から、スクリプト内で命令に対応する箇所のファイル名や行番号を抽出
			AcceleratorInstruction causeInstruction = nextNode.getSourceInstruction();
			int lineNumber = MetaInformationSyntax.extractLineNumber(causeInstruction, memory);
			String fileName = MetaInformationSyntax.extractFileName(causeInstruction, memory);

			// 例外が VnanoException の場合は、既に原因情報を持っているので、ファイル名と行番号を持たせて上層に投げる
			if (e instanceof VnanoException) {
				VnanoException vne = (VnanoException)e;
				vne.setFileName(fileName);
				vne.setLineNumber(lineNumber);
				throw vne;

			// それ以外の例外は、想定外で原因情報が不明なので、
			// 命令アドレスなどのデバッグ情報を持たせるため、VnanoException でラップして投げる
			} else {
				int unreorderedAddress = causeInstruction.getUnreorderedAddress();
				int reorderedAddress = causeInstruction.getReorderedAddress();
				String[] errorWords = {
						Integer.toString(unreorderedAddress), Integer.toString(reorderedAddress), causeInstruction.toString()
				};
				throw new VnanoException(ErrorType.UNEXPECTED_ACCELERATOR_CRASH, errorWords, e, fileName, lineNumber);
			}
		}

		// ダンプ内容に実行終了点を表す区切りを入れる
		if (shouldDump && dumpTargetIsAll) {
			dumpStream.println("");
			dumpStream.println("================================================================================");
			dumpStream.println("= End");
			dumpStream.println("================================================================================");
		}
	}
}
