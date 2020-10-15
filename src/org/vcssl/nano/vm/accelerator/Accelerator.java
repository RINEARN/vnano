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
import org.vcssl.nano.spec.OperationCode;
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

	/** 処理を継続可能かどうかを表すフラグです。 */
	// 用途的には終了リクエストフラグだけども、素直に shouldTerminate だとループ条件で ! が要るので、反転したフラグにしている。
	// そういう事情で、このフラグに対する public なリセットメソッドは resetTerminator() なので注意。
	private volatile boolean continuable;

	/** インスタンスの生成時点から、処理した命令数（累積処理命令数）を保持します。 */
	// 関連個所を触る際の注意:
	// ・int の上限に達してもリセットはされず、負の端からまた加算され続ける仕様（getterの説明参照）。
	// ・long にすると書き込み箇所が 32 bit x 2 操作になり得て、どのタイミングで参照されるか不定なので
	//   値化け予防に synchronized 書き込み（と参照）が必要になるが、それは非常に遅いので int で我慢する。
	// ・同様に速度への影響を抑えるため volatile 修飾は行わず、
	//   スレッドキャッシュによるラグはカウンタの精度仕様で許容する（getterの説明参照）。
	private int processedInstructionCount;

	/** 現在処理中の演算ノードを保持します。 */
	// パフォーマンスモニタでオペレーションコードごとの命令実行頻度をなどを解析するため
	private AcceleratorExecutionNode currentExecutedNode;

	/** synchronized ブロック用のロック対象オブジェクトです。 */
	// （主にスレッドキャッシュ剥がしに使うのでこのロック自体にあまり意味は無い）
	private final Object lock;


	/**
	 * 新しいアクセラレータのインスタンスを生成します。
	 */
	public Accelerator() {
		this.continuable = true;
		this.processedInstructionCount = 0;
		this.currentExecutedNode = null;
		this.lock = new Object();
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
	 * @throws InvalidInstructionException
	 * 		このコントロールユニットが対応していない命令が実行要求された場合や、
	 * 		オペランドの数が期待値と異なる場合など、命令内容が不正である場合に発生します。
	 * @throws MemoryAccessException
	 * 		命令のオペランドに指定された仮想メモリーアドレスが使用領域外であった場合など、
	 * 		不正な仮想メモリーアクセスが生じた場合などに発生します。
	 */
	public void process(Instruction[] instructions, Memory memory, Interconnect interconnect, Processor processor)
			throws VnanoException {

		// 必要なオプション値を読み込む
		//（注: GUIアプリなどでのエンジンの使い方を考えると、このメソッドの処理の大元、つまりエンジンの eval のコールは、
		//      エンジンにオプションマップを put したスレッドとは別のスレッドで行われる可能性がある。スレッドキャッシュとかに注意。）
		//      -> そういうのは Interconnect 側で activate 時にまとめて前処理すべき? eval 呼んだスレッドで処理されるし。後々で要検討
		boolean terminatable, monitorable, shouldDump, dumpTargetIsAll, shouldRun;
		String dumpTarget;
		PrintStream dumpStream = null;
		synchronized (this.lock) {
			Map<String, Object> optionMap = interconnect.getOptionMap();                 // オプション値を持っているマップ
			shouldDump = (Boolean)optionMap.get(OptionKey.DUMPER_ENABLED);               // ダンプするかどうか
			dumpTarget = (String)optionMap.get(OptionKey.DUMPER_TARGET);                 // ダンプ対象
			dumpTargetIsAll = dumpTarget.equals(OptionValue.DUMPER_TARGET_ALL);          // ダンプ対象が全てかどうか
			dumpStream = (PrintStream)optionMap.get(OptionKey.DUMPER_STREAM);            // ダンプ先ストリーム
			shouldRun = (Boolean)optionMap.get(OptionKey.RUNNING_ENABLED);               // コードを実行するかどうか
			terminatable = (Boolean)optionMap.get(OptionKey.TERMINATOR_ENABLED);         // 処理中に終了可能にするかどうか
			monitorable = (Boolean)optionMap.get(OptionKey.PERFORMANCE_MONITOR_ENABLED); // 性能計測を行うかどうか
		}

		// スカラ判定やキャッシュ確保などの高速化用データ解析を実行
		AcceleratorDataManagementUnit dataManager = new AcceleratorDataManagementUnit();
		dataManager.allocate(instructions, memory, interconnect);

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
		AcceleratorExecutionNode nextNode = (nodes.length == 0) ? null : nodes[0];
		this.currentExecutedNode = nextNode;
		try {

			// 途中終了を可能にしつつ、性能計測も必要な場合のループ(最も重い)
			if (terminatable && monitorable) {
				while (nextNode != null && this.continuable) {  // この continuable は volatile
					// 注: 以下の processedInstructionCount の更新、この値は別スレッドから参照される可能性があり、
					// long だと 32bit x 2 書き込みになる場合の値化け予防で synchronized で囲う必要があるが、現状は int なので不要。
					// (読んで足して書き戻す間のラグやキャッシュのラグによる誤差は、カウンタの精度仕様で許容する。getterコメント参照)
					// long 化する場合は素直に synchronized すると遅いので、数百回に一回 int 差分を synchronized 可算する等を要検討。
					this.processedInstructionCount += nextNode.INSTRUCTIONS_PER_NODE;
					this.currentExecutedNode = nextNode;  // 順序に注意。間違うとプロファイラで隣の命令の頻度にカウントされてしまう
					nextNode = nextNode.execute();
				}
				this.currentExecutedNode = null;

			// 途中終了は不要で、性能計測が必要な場合のループ
			// (計測値を加算する処理などが追加されるため、スカラ演算の最大速度が 2～2.5 割ほど低下する模様)
			} else if(monitorable) {
				while (nextNode != null) {
					// 注: 以下の processedInstructionCount の更新、すぐ上の else if 内のコメント参照
					this.processedInstructionCount += nextNode.INSTRUCTIONS_PER_NODE;
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
	 * 現在実行中の命令の処理が終わった時点で、残りの命令列の実行を行わずに終了させます。
	 *
	 * 命令列の実行が複数同時に行われている場合、それら各々の実行時に Interconnect 経由で指定されたオプションにおいて、
	 * {@link org.vcssl.nano.spec.OptionKey#TERMINATOR_ENABLED TERMINATOR_ENABLED} オプションが有効であったもののみが終了され、
	 * 無効であったものには何も起こらず継続して処理されます。
	 *
	 * なお、このメソッドで実行を終了させた後に、
	 * 再び {@link Accelerator#process(Instruction[], Memory, Interconnect) process(...)}
	 * メソッドによって(新規に)命令列を実行する際には、事前に
	 * {@link Accelerator#resetTerminator() resetTerminator()()} を呼び出す必要があります。
	 */
	public void terminate() {
		this.continuable = false;  // volatile
	}


	/**
	 * {@link Accelerator#terminate() terminate()} メソッドによって終了させたアクセラレータを、再び実行可能な状態に戻します。
	 */
	// process() メソッドが呼ばれてからその中で自動的に true にする方式だと、呼んだ直後に terminate() で false 化した場合に、
	// タイミングによっては process() 側での true 化の方が後になって、処理が続行されてしまう可能性があるので注意。
	// process() の最後に true に戻して次回実行に備える方式でも、terminate() のタイミングがまずいと false 化され、
	// 逆に次回実行不可能になってしまう可能性がある。従って、搭載アプリ側で適切な時にフラグをリセットしてもらう。
	public void resetTerminator() {
		this.continuable = true;  // volatile
	}


	/**
	 * このアクセラレータのインスタンス化時点から、現在までに処理された命令数を、おおまかな目安値として返します。
	 *
	 * ただし、計測による性能低下をなるべく抑えるため、返される値の精度は完全ではなく、
	 * 呼び出し元スレッド等によってキャッシュされた少し前の値が返される可能性がある事に留意してください
	 * (具体的には、内部のカウンタ値が volatile 修飾されていません)。
	 * 特に、命令列の実行を複数同時に行った際には、カウントに排他処理は行われないため加算漏れが生じます。
	 *
	 * 加えて、値が int 型の上限に達してもリセットはされず、その後は負の端
	 * (int型で表現可能な数直線上の最小値)に至り、そこからまた加算され続ける事にも留意が必要です。
	 * そのため、取得値をそのまま使うのではなく、取得を十分な頻度
	 * （目安として、Vnano のコマンドラインモードの --perf オプションの処理では、この値の取得を毎秒 100 回程度行っています）
	 * で行って、前回からの差分を求めて使用する事などが推奨されます。
	 *
	 * なお、実行対象処理の中で、実行開始時に Interconnect 経由で指定されたオプションにおいて、
	 * {@link org.vcssl.nano.spec.OptionKey#PERFORMANCE_MONITOR_ENABLED PERFORMANCE_MONITOR_ENABLED}
	 * が有効化されていた処理の命令数のみがカウントされます。
	 * 同オプションが有効化された処理が実行中でない場合、または何の処理も実行中でない場合に、
	 * このメソッドをコールすると、単に変化していないカウンタ値が返されます。
	 *
	 * @return このアクセラレータのインスタンス化時点から、現在までに処理された命令数の、おおまかな目安値
	 */
	// 名前が冗長なのは、将来的に値を long 型で取得可能なメソッドをサポートするかもしれないためなのと（その可能性自体は低い）、
	// メソッド名でそういう可能性をにおわせる事で、値の範囲が int で結構すぐ一周するという事に毎回気付けるようにするため
	public int getProcessedInstructionCountIntValue() {
		synchronized (this.lock) {
			return this.processedInstructionCount;
		}
	}


	/**
	 * このアクセラレータ上において、現在実行中の命令のオペレーションコードを返します。
	 * アクセラレータは複数の命令を一括で同時に実行する場合があるため、結果は配列として返されます。
	 * 何の命令も実行されていない時には、空の配列が返されます。
	 *
	 * なお、複数のスレッドにおいて、このアクセラレータの同一インスタンスを用いて、
	 * 並列に実行を行った場合でも、このメソッドが返すオペレーションコードの数は増えません。
	 * このメソッドは、単に命令実行ループ毎にフィールドに控えられたオペレーションコード
	 * （厳密にはそれを含むオブジェクト）の最新値を返すだけであるため、そのような場合には、
	 * どれかのスレッド（常に同一とは限りません）が書き換えた値が返されます。
	 *
	 * @return このアクセラレータ上において、現在実行中の命令のオペレーションコード (複数あり得るため配列)
	 */
	public OperationCode[] getCurrentlyExecutedOperationCodes() {
		synchronized (this.lock) {

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
