/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;


import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.memory.MemoryAccessException;
import org.vcssl.nano.vm.processor.Instruction;
import org.vcssl.nano.vm.processor.InvalidInstructionException;
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
	 * @param processor 高速実行の対象外の命令を処理する仮想プロセッサ
	 * @throws InvalidInstructionException
	 * 		このコントロールユニットが対応していない命令が実行要求された場合や、
	 * 		オペランドの数が期待値と異なる場合など、命令内容が不正である場合に発生します。
	 * @throws MemoryAccessException
	 * 		命令のオペランドに指定された仮想メモリーアドレスが使用領域外であった場合など、
	 * 		不正な仮想メモリーアクセスが生じた場合などに発生します。
	 */
	public void process(Instruction[] instructions, Memory memory, Interconnect interconnect, Processor processor)
					throws MemoryAccessException, InvalidInstructionException {

		// スカラ判定やキャッシュ確保などの高速化用データ解析を実行
		AccelerationDataManager dataManager = new AccelerationDataManager();
		dataManager.allocate(instructions, memory);

		/*
		System.out.println("===== INPUT INSTRUCTIONS =====");
		for (int i=0; i<instructions.length; i++) {
			System.out.println("[" + i + "]\t" + instructions[i]);
		}
		*/

		// 命令スケジューラで命令列を高速化用に再配置・変換
		AccelerationScheduler scheduler = new AccelerationScheduler();
		AcceleratorInstruction[] acceleratorInstructions = scheduler.schedule(instructions, memory, dataManager);

		/*
		System.out.println("===== SCHEDULED INSTRUCTIONS =====");
		for (int i=0; i<acceleratorInstructions.length; i++) {
			AcceleratorInstruction instruction = acceleratorInstructions[i];
			System.out.println("[" + instruction.getReorderedAddress() + "(" + instruction.getUnreorderedAddress() + ")" + "]\t" + instruction);
		}
		*/

		// 命令列を演算器に割り当てて演算実行ノード列を生成
		AccelerationDispatcher dispatcher = new AccelerationDispatcher();
		AccelerationExecutorNode[] executorNodes = dispatcher.dispatch(
				processor, memory, interconnect, acceleratorInstructions, dataManager
		);

		// キャッシュにメモリのデータを書き込む
		dataManager.getCacheSynchronizers(Memory.Partition.CONSTANT).writeCache();
		dataManager.getCacheSynchronizers(Memory.Partition.GLOBAL).writeCache();


		// 以下、命令の逐次実行ループ
		AccelerationExecutorNode nextNode = null;
		if (executorNodes.length != 0) {
			nextNode = executorNodes[0];
		}
		try {
			while (nextNode != null) {
				nextNode = nextNode.execute();
			}
		} catch (Exception causeException) {
			AcceleratorInstruction causeInstruction = nextNode.getSourceInstruction();
			int unreorderedAddress = causeInstruction.getUnreorderedAddress();
			int reorderedAddress = causeInstruction.getReorderedAddress();
			throw new VnanoFatalException(
					"Accelerator crashed at:"
					+ " address=" + unreorderedAddress
					+ " reorderedAddress=" + reorderedAddress
					+ " instruction=" + causeInstruction,
					causeException
			);
		}
	}
}