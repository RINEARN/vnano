/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;


import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.memory.DataException;
import org.vcssl.nano.memory.MemoryAccessException;
import org.vcssl.nano.processor.Instruction;
import org.vcssl.nano.processor.InvalidInstructionException;
import org.vcssl.nano.processor.OperationCode;
import org.vcssl.nano.processor.Processor;
import org.vcssl.nano.memory.Memory;


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

	private class AccelerationResource {
		public AccelerationExecutorNode[] accelerationUnits = null;

		public AccelerationDataManager dataManager = null; // [partitionIndex]
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

		AccelerationResource resource = this.generateResource(
				instructions, memory, interconnect, processor
		);

		resource.dataManager.getCacheSynchronizers(Memory.Partition.CONSTANT).writeCache();
		resource.dataManager.getCacheSynchronizers(Memory.Partition.GLOBAL).writeCache();

		AccelerationExecutorNode[] AccelerationUnits = resource.accelerationUnits;

		// 命令の逐次実行ループ
		AccelerationExecutorNode nextNode = AccelerationUnits[0];
		while (nextNode != null) {
			nextNode = nextNode.execute();
		}

	}


	/**
	 * 命令列を事前解釈し、高速実行に必要なリソースを確保した上で、実行用のコプロセッサユニットを一括生成します。
	 *
	 * @param instructions 実行対象の命令配列
	 * @param memory データの入出力に用いる仮想メモリー
	 * @param interconnect 外部変数・外部関数が接続されているインターコネクト
	 * @param processor 高速実行の対象外の命令を処理する仮想プロセッサ
	 * @return 実行用のコプロセッサユニットの配列
	 * @throws MemoryAccessException
	 * 		命令のオペランドに指定された仮想メモリーアドレスが使用領域外であった場合など、
	 * 		不正な仮想メモリーアクセスが生じた場合などに発生します。
	 */
	private AccelerationResource generateResource (Instruction[] instructions, Memory memory,
			Interconnect interconnect, Processor processor)
					throws MemoryAccessException, InvalidInstructionException {

		// アドレスに紐づけてキャッシュを持つ(同じデータコンテナに対して同じキャッシュが一意に対応するように)
		// 非キャッシュ演算ユニットはデータコンテナとキャッシュ要素を保持し、同期する


		// スカラ判定やキャッシュ確保などの高速化用データ解析を実行
		AccelerationDataManager dataManager = new AccelerationDataManager();
		dataManager.allocate(instructions, memory);


		System.out.println("===== INPUT INSTRUCTIONS =====");
		for (int i=0; i<instructions.length; i++) {
			System.out.println("[" + i + "]\t" + instructions[i]);
			//System.out.println(i + ":\t" + instructions[i]);
		}


		// 命令スケジューラで命令列を高速化用に再配置・変換
		AccelerationScheduler scheduler = new AccelerationScheduler();
		AcceleratorInstruction[] acceleratorInstructions = scheduler.schedule(instructions, memory, dataManager);



		System.out.println("===== SCHEDULED INSTRUCTIONS =====");
		for (int i=0; i<acceleratorInstructions.length; i++) {
			AcceleratorInstruction instruction = acceleratorInstructions[i];
			System.out.println("[" + instruction.getReorderedAddress() + "(" + instruction.getUnreorderedAddress() + ")" + "]\t" + instruction);
			//System.out.println(instruction.getReorderedAddress() + ":\t" + instruction);
		}



		AccelerationDispatcher dispatcher = new AccelerationDispatcher();
		AccelerationExecutorNode[] executors = dispatcher.dispatch(
				processor, memory, interconnect, acceleratorInstructions, dataManager
		);

		AccelerationResource resource = new AccelerationResource();
		resource.accelerationUnits = executors;
		resource.dataManager = dataManager;

		return resource;
	}

}
