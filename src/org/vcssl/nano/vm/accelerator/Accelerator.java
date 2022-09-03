/*
 * Copyright(C) 2017-2022 RINEARN
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;


import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.MetaInformationSyntax;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.spec.OptionKey;
import org.vcssl.nano.spec.OptionValue;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.processor.Instruction;
import org.vcssl.nano.vm.processor.Processor;


/**
 * The high-speed implementation of {@link org.vcssl.nano.vm.processor.Processor Processor} class, called the accelerator.
 */
public class Accelerator {

	/**
	 * The flag representing whether the process should continue.
	 * 
	 * If this flag is turned to false,
	 * the process of {@link Processor#process(Instruction[], Memory, Interconnect)} method will be terminated immediately, 
	 * just after when the process of the currently executed instruction completed.
	 * 
	 * To turn to true/false this flag, use terminate() / resetTerminator() methods.
	 */
	private volatile boolean continuable;

	/**
	 * The counter of the number of the instructions executed by this instance.
	 * This value is useful for performance monitoring/analysis.
	 * 
	 * Note that, when this counter has reached to the maximum limit of the int type, 
	 * the next counted value jumps to the minimum (negative) limit value of the int type, by so-called "overflow" behaviour.
	 * 
	 * This counter frequently overflows, so the caller-side should monitor this value frequently, 
	 * and should accumulate differentials of monitored values (current value - last monitored value) to more long-precision counter.
	 * 
	 * Why we use "int" for this value, not "long", is to avoid to make the updating process of this counter "synchromized".
	 * If this counter is "long", the writing to it may consists of two operations (32bit x 2), depends on environment.
	 * So if the value is read from an other thread just when its value is being written, the broken value may be read, 
	 * if we don't make the writting/reading action "synchronized".
	 * However, "synchronized" might be a bottleneck of performance, so we avoid the above by using "int" type for this counter.
	 * 
	 * For the same reason, we don't make this value "volatile", 
	 * so some time lag may occurs for this value, by the effect of the "thread cache" mechanism.
	 */
	private int executedInstructionCount;

	/**
	 * Stores the execution node of the currently executed instruction.
	 */
	private AcceleratorExecutionNode currentExecutedNode;


	/**
	 * Creates an new accelerator.
	 */
	public Accelerator() {
		this.continuable = true;
		this.executedInstructionCount = 0;
		this.currentExecutedNode = null;
	}


	/**
	 * Processes the list of instructions.
	 * 
	 * The processing-flow begins with the top of the list of the instructions.
	 * Ordinary, the flow goes towards the end of the list,
	 * with processing each instruction in the list in the serial order.
	 * However, sometimes the processing-flow jumps to any point in the list, 
	 * by the effect of JMP instruction and so on.
	 * 
	 * This method ends when the instruction at the end of the list has been processed,
	 * or when the flow has jumped to out of bounds of the list of the instructions.
	 *
	 * @param instructions The list of the instructions to be processed.
	 * @param memory The memory to which data I/O will be performed.
	 * @param interconnect The interconnect having the external function plug-ins which may be called by the instructions.
	 * @param processor The processor for processing some instructions unsupported by this accelerator.
	 * @throws VnanoException Thrown when any normal run-time error has been occurred (errors of cast, array indexing, and so on).
	 * @throws VnanoFatalException Thrown when any abnormal error (might be a bug of the VM or the compiler) occurred.
	 */
	public void process(Instruction[] instructions, Memory memory, Interconnect interconnect, Processor processor)
			throws VnanoException {

		// 必要なオプション値を読み込む
		boolean terminatable, monitorable, shouldDump, dumpTargetIsAll, shouldRun;
		String dumpTarget;
		PrintStream dumpStream = null;
		int optimizationLevel = -1;
		synchronized (this) {
			Map<String, Object> optionMap = interconnect.getOptionMap();                 // オプション値を持っているマップ
			shouldDump = (Boolean)optionMap.get(OptionKey.DUMPER_ENABLED);               // ダンプするかどうか
			dumpTarget = (String)optionMap.get(OptionKey.DUMPER_TARGET);                 // ダンプ対象
			dumpTargetIsAll = dumpTarget.equals(OptionValue.DUMPER_TARGET_ALL);          // ダンプ対象が全てかどうか
			dumpStream = (PrintStream)optionMap.get(OptionKey.DUMPER_STREAM);            // ダンプ先ストリーム
			shouldRun = (Boolean)optionMap.get(OptionKey.RUNNING_ENABLED);               // コードを実行するかどうか
			terminatable = (Boolean)optionMap.get(OptionKey.TERMINATOR_ENABLED);         // 処理中に終了可能にするかどうか
			monitorable = (Boolean)optionMap.get(OptionKey.PERFORMANCE_MONITOR_ENABLED); // 性能計測を行うかどうか
			optimizationLevel = (Integer)optionMap.get(OptionKey.ACCELERATOR_OPTIMIZATION_LEVEL); // 最適化レベル
		}

		// スカラ判定やキャッシュ可能性判断などの高速化用データ解析を実行
		// (命令列に操作を加える前に、最初に済ませる必要がある)
		AcceleratorDataManagementUnit dataManager = new AcceleratorDataManagementUnit();
		dataManager.allocate(instructions, memory, interconnect, optimizationLevel);

		// 命令列を、Accelerator用に継承された型の命令列に変換
		List<AcceleratorInstruction> acceleratorInstructionList = new ArrayList<AcceleratorInstruction>();
		for (int instructionIndex=0; instructionIndex<instructions.length; instructionIndex++) {
			acceleratorInstructionList.add( new AcceleratorInstruction(instructions[instructionIndex], instructionIndex) );
		}
		AcceleratorInstruction[] acceleratorInstructions = acceleratorInstructionList.toArray(new AcceleratorInstruction[0]);

		// 各命令がどの演算ユニットへ割り当てるべきかを解析し、結果を各命令に設定したものを取得
		//（その情報は最適化のステージ内でも参照されるので、最適化前に一度済ませておく必要がある）
		AcceleratorDispatchUnit dispatcher = new AcceleratorDispatchUnit();
		acceleratorInstructions = dispatcher.preDispatch(acceleratorInstructions, memory, dataManager);

		// 命令の並び替えや削除、インライン展開などを行って、命令列を最適化する
		// (複数命令の一括処理化は、最適化というよりも演算ユニット割り当てによる効率化なので、ここではなく後のスケジューラが行う)
		AcceleratorOptimizationUnit optimizer = new AcceleratorOptimizationUnit();
		acceleratorInstructions = optimizer.optimize(acceleratorInstructions, memory, dataManager, optimizationLevel);

		// 最適化で生成された命令等があるため、もう一度演算ユニット割り当てを解析
		acceleratorInstructions = dispatcher.preDispatch(acceleratorInstructions, memory, dataManager);

		// 変換後の命令列をダンプ
		if (shouldDump && (dumpTargetIsAll || dumpTarget.equals(OptionValue.DUMPER_TARGET_ACCELERATOR_CODE)) ) {
			if (dumpTargetIsAll) {
				dumpStream.println("================================================================================");
				dumpStream.println("= Accelerator Code (Optimized)");
				dumpStream.println("= - Output of: org.vcssl.nano.vm.accelerator.AcceleratorOptimizationUnit");
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

		// コールスタックやアドレスなどを統合的に管理しつつ、内部関数関連の命令を実行する、内部関数制御ユニットを生成
		InternalFunctionControlUnit internalFunctionControlUnit = new InternalFunctionControlUnit();

		// 外部関数の呼び出しを低オーバーヘッドで行う、外部関数制御ユニットを生成
		ExternalFunctionControlUnit externalFunctionControlUnit = new ExternalFunctionControlUnit(interconnect);

		// 命令列をアクセラレータ内の演算器に割り当てて演算実行ノード列を生成
		AcceleratorExecutionNode[] nodes = dispatcher.dispatch(
			processor, memory, interconnect, acceleratorInstructions, dataManager, bypassUnit,
			internalFunctionControlUnit, externalFunctionControlUnit
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
		internalFunctionControlUnit.setNodes(nodes);

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
		AcceleratorExecutionNode nextNode = (nodes.length == 0) ? null : nodes[0];
		this.currentExecutedNode = nextNode;
		try {

			// 途中終了を可能にしつつ、性能計測も必要な場合のループ(最も重い)
			if (terminatable && monitorable) {
				while (nextNode != null && this.continuable) {  // この continuable は volatile
					// 注: 以下の executedInstructionCount の更新、この値は別スレッドから参照される可能性があり、
					// long だと 32bit x 2 書き込みになる場合の値化け予防で synchronized で囲う必要があるが、現状は int なので不要。
					// (読んで足して書き戻す間のラグやキャッシュのラグによる誤差は、カウンタの精度仕様で許容する。getterコメント参照)
					// long 化する場合は素直に synchronized すると遅いので、数百回に一回 int 差分を synchronized 可算する等を要検討。
					this.executedInstructionCount += nextNode.INSTRUCTIONS_PER_NODE;
					this.currentExecutedNode = nextNode;  // 順序に注意。間違うとプロファイラで隣の命令の頻度にカウントされてしまう
					nextNode = nextNode.execute();
				}
				this.currentExecutedNode = null;

			// 途中終了は不要で、性能計測が必要な場合のループ
			// (計測値を加算する処理などが追加されるため、スカラ演算の最大速度が 2～2.5 割ほど低下する模様)
			} else if(monitorable) {
				while (nextNode != null) {
					// 注: 以下の executedInstructionCount の更新、すぐ上の else if 内のコメント参照
					this.executedInstructionCount += nextNode.INSTRUCTIONS_PER_NODE;
					this.currentExecutedNode = nextNode;
					nextNode = nextNode.execute();
				}
				this.currentExecutedNode = null;

			// 途中終了は可能にしつつ、性能計測は不要な場合のループ
			//（while文に条件が追加されるため、スカラ演算の最大速度が 1～2 割ほど低下する模様）
			} else if (terminatable) {
				while (nextNode != null && this.continuable) {  // この continuable は volatile
					nextNode = nextNode.execute();
				}

			// 途中終了も性能計測も必要ない場合の実行ループ
			//（最も単純なため、最も高速）
			} else {
				while (nextNode != null) {
					nextNode = nextNode.execute();
				}
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


	/**
	 * Terminates the process of {@link Accelerator#process(Instruction[], Memory, Interconnect)} method immediately, 
	 * just after when the process of the currently executed instruction completed.
	 * 
	 * However, if the {@link org.vcssl.nano.spec.OptionKey#TERMINATOR_ENABLED TERMINATOR_ENABLED} option had been enabled
	 * when the process started, the process will not be terminated even if this method is called.
	 * 
	 * Also, after terminated the process by this method, if you want to process new instructions, 
	 * it requires to reset the flag for the termination by calling {@link Accelerator#resetTerminator()} method.
	 */
	public void terminate() {
		this.continuable = false;  // volatile
	}


	/**
	 * Resets the flag for the termination.
	 * 
	 * When you want to process new instructions after using {@link Accelerator#terminate()} method, 
	 * it requires to call this method before call {@link Accelerator#process(Instruction[], Memory, Interconnect)} method.
	 * If you don't do it, the process of new instructons will be terminated immediately.
	 */
	public void resetTerminator() {
		this.continuable = true;  // volatile
	}


	/**
	 * Gets the counter value of the instructions executed by this instance.
	 * This value is useful for performance monitoring/analysis.
	 * 
	 * Note that, when the counter has reached to the maximum limit of the int type, 
	 * the next counted value jumps to the minimum (negative) limit value of the int type, by so-called "overflow" behaviour.
	 * This counter frequently overflows, so the caller-side should monitor this value frequently, 
	 * and should accumulate differentials of monitored values (current value - last monitored value) to more long-precision counter.
	 * 
	 * Also, some time lag may occurs for the updating of the counter, by the effect of the "thread cache" mechanism.
	 * So please consider that the returned value is a rough value.
	 *
	 * @return The counter value of the instructions executed by this instance (a rough value, frequently overflows).
	 */
	public int getExecutedInstructionCountIntValue() {
		synchronized (this) {
			return this.executedInstructionCount;
		}
	}



	/**
	 * Gets the operation code of the currently executed instructions.
	 * This value is useful for performance monitoring/analysis.
	 *
	 * The return value is an array, because an accelerator can multiple instruction at once.
	 * 
	 * Note that, when multiple threads are running in parallel on this instance, 
	 * Only operation codes of the most recently executed instructions by any one thread will be returned.
	 *
	 * @return The operation codes of the currently executed instructions.
	 */
	public OperationCode[] getCurrentlyExecutedOperationCodes() {
		synchronized (this) {

			// this.currentExecutedNode の参照はこのメソッドの処理中にも高速で切り替わるので、最初にローカルに控えてから使う
			AcceleratorExecutionNode currentNodeStock = this.currentExecutedNode;

			// 何の命令も実行されていない時
			if (currentNodeStock == null) {
				return new OperationCode[0];
			}

			// 現在実行中の演算ノードから、実行対象の命令（※）を取得
			// (この命令は、VRIL命令を extends した、演算ノードと一対一で対応するアクセラレータ拡張命令)
			AcceleratorInstruction currentAccelInstruction = currentNodeStock.getSourceInstruction();

			// 単一のVRIL命令を実行する演算ノードの場合は、そのオペコードを要素数1の配列に格納して返す
			if (currentNodeStock.INSTRUCTIONS_PER_NODE == 1) {
				return new OperationCode[] { currentAccelInstruction.getOperationCode() };

			// 複数のVRIL命令を一括実行する演算ノードの場合
			//（例えば Float64CachedScalarDualArithmeticUnit の所属ノードとかは2個の算術演算VRIL命令を1ターンで一括実行する）
			} else {

				// この場合はアクセラレータ拡張命令に、由来のVRILオペコードを配列で返す getter があるので結果をコピーして返す
				int operationCodeN = currentNodeStock.INSTRUCTIONS_PER_NODE;
				OperationCode[] returnOperationCodes = new OperationCode[ operationCodeN ];
				System.arraycopy(
					currentAccelInstruction.getFusedOperationCodes(), 0, returnOperationCodes, 0, operationCodeN
				);
				return returnOperationCodes;
			}
		}
	}

}
