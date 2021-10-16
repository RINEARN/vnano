/*
 * Copyright(C) 2018-2021 RINEARN (Fumihiro Matsui)
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
import org.vcssl.nano.spec.OperationCode;
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

	/** 命令を実行する仮想プロセッサを保持します。 */
	// （終了リクエストや実測性能値の取得のためにフィールドに参照を保持）
	private Processor processor = null;

	/** 命令を実行するアクセラレータ（仮想プロセッサの高速実装）を保持します。 */
	// （終了リクエストや実測性能値の取得のためにフィールドに参照を保持）
	private Accelerator accelerator = null;

	/** インスタンスの生成時点から、処理した命令数（累積処理命令数）を保持します。 */
	// 注意点:
	// ・int の上限に達してもリセットはされず、負の端からまた加算され続ける仕様（このクラス内の getter の説明参照）。
	// ・long にしないのは processor / accelerator 側でのパフォーマンス上の理由から（それぞれの getter の説明参照）。
	private int vmProcessedInstructionCount;

	/** 仮想プロセッサによる累積処理命令数の、前回参照した値を保持します。 */
	private int processorLastProcCount;

	/** アクセラレータによる累積処理命令数の、前回参照した値を保持します。 */
	private int acceleratorLastProcCount;

	/** synchronized ブロック用のロック対象オブジェクトです。（主にスレッドキャッシュ剥がしに使うのでこのロック自体にあまり意味は無い） */
	private final Object lock;


	/**
	 * <span class="lang-en">
	 * Create a new VM
	 * </span>
	 * <span class="lang-ja">
	 * VMを生成します
	 * </span>
	 * .
	 */
	public VirtualMachine() {
		this.processor = new Processor();
		this.accelerator = new Accelerator();
		this.vmProcessedInstructionCount = 0;
		this.processorLastProcCount = 0;
		this.acceleratorLastProcCount = 0;
		this.lock = new Object();
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
	public Object executeAssemblyCode(String assemblyCode, Interconnect interconnect)
			throws VnanoException {

		// 必要なオプション値を読み込む
		//（注: GUIアプリなどでのエンジンの使い方を考えると、このメソッドの処理の大元、つまりエンジンの eval のコールは、
		//      エンジンにオプションマップを put したスレッドとは別のスレッドで行われる可能性がある。スレッドキャッシュとかに注意。）
		//      -> そういうのは Interconnect 側で activate 時にまとめて前処理すべき? eval 呼んだスレッドで処理されるし。後々で要検討
		boolean acceleratorEnabled, shouldDump, dumpTargetIsAll;
		String dumpTarget;
		PrintStream dumpStream = null;
		synchronized (this.lock) {
			Map<String, Object> optionMap = interconnect.getOptionMap();                // オプション値を持っているマップ
			acceleratorEnabled = (Boolean)optionMap.get(OptionKey.ACCELERATOR_ENABLED); // 高速版実装を使用するかどうか
			shouldDump = (Boolean)optionMap.get(OptionKey.DUMPER_ENABLED);              // ダンプするかどうか
			dumpTarget = (String)optionMap.get(OptionKey.DUMPER_TARGET);                // ダンプ対象
			dumpTargetIsAll = dumpTarget.equals(OptionValue.DUMPER_TARGET_ALL);         // ダンプ対象が全てかどうか
			dumpStream = (PrintStream)optionMap.get(OptionKey.DUMPER_STREAM);           // ダンプ先ストリーム
		}


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
		if (acceleratorEnabled) {
			this.accelerator.process(instructions, memory, interconnect, this.processor);
		} else {
			this.processor.process(instructions, memory, interconnect);
		}

		// メモリーのデータをinterconnect経由で外部変数に書き戻す（このタイミングでBindings側が更新される）
		interconnect.writebackExternalVariables(memory, intermediateCode); // アドレスから変数名への逆変換に中間コードが必要

		// 処理結果（式の評価値やスクリプトの戻り値）を取り出し、外側のデータ型に変換して返す
		if (memory.hasResultDataContainer()) {
			DataContainer<?> resultDataContainer = memory.getResultDataContainer();
			DataConverter converter = new DataConverter(
				resultDataContainer.getDataType(), resultDataContainer.getArrayRank()
			);
			return converter.convertToExternalObject(resultDataContainer);
		} else {
			return null;
		}
	}


	/**
	 * <span class="lang-en">
	 * Terminates the currently running code after when the processing of the current instruction ends,
	 * without processing remained instructions after it in code
	 * </span>
	 * <span class="lang-ja">
	 * 現在実行中の命令の処理が終わった時点で, コード内の残りの命令列の実行を行わずに, コード実行を終了します.
	 * </saam>
	 * .
	 * <span class="lang-en">
	 * If multiple code are being processed, only processes executed under the condition that
	 * {@link org.vcssl.nano.spec.OptionKey#TERMINATOR_ENABLED TERMINATOR_ENABLED}
	 * option is true will be terminated, and other processes will continue.
	 * Also, if you used this method, call {@link VirtualMachine#resetTerminator() resetTerminator()}
	 * method before the next execution of code,
	 * otherwise the next execution will end immediately without processing any instructions.
	 * </span>
	 * <span class="lang-ja">
	 * 複数のコードが同時に実行されている場合, それら各々の実行時に Interconnect 経由で指定されたオプションにおいて,
	 * {@link org.vcssl.nano.spec.OptionKey#TERMINATOR_ENABLED TERMINATOR_ENABLED} が有効化されていたもののみが終了され,
	 * 無効であったものには何も起こらず継続して処理されます.
	 * なお、このメソッドを呼び出して実行を終了させた後に、再び(新規に)コードを実行する際には、事前に
	 * {@link VirtualMachine#resetTerminator() resetTerminator()} メソッドを呼び出す必要があります.
	 * 前者の呼び出しから後者の呼び出しまでの間, 実行が要求されたコードはすぐに終了します.
	 * </span>
	 *
	 * <span class="lang-en">
	 * By the above behavior, even if a termination request by this method and
	 * an execution request by another thread are conflict, the execution will be terminated certainly
	 * (unless {@link VirtualMachine#resetTerminator() resetTerminator()} will be called before
	 * when the execution will have been terminated).
	 * </span>
	 * <span class="lang-ja">
	 * 上記の仕様により, このメソッドの呼び出しと新規実行リクエストが,
	 * 別スレッドからシビアに競合したタイミングで行われた場合においても,
	 * (終了前に {@link VirtualMachine#resetTerminator() resetTerminator()} が呼ばれない限り)
	 * スクリプトは確実に終了します.
	 * </span>
	 */
	public void terminate() {

		// この処理要求は恐らくコード実行スレッドとは別スレッドから投げられるのでスレッドキャッシュが影響する可能性がある
		synchronized (this.lock) {

			if (this.accelerator != null) {
				this.accelerator.terminate();
			}
			if (this.processor != null) {
				this.processor.terminate();
			}
		}
	}


	/**
	 * <span class="lang-en">
	 * Resets the VM which had terminated by {@link VirtualMachine#terminate() terminate()} method, for processing new code
	 * </span>
	 * <span class="lang-ja">
	 * {@link VirtualMachine#terminate() terminate()} メソッドによって終了させたVMを, 再び(新規)コード実行可能な状態に戻します
	 * </span>
	 * .
	 * <span class="lang-en">
	 * Please note that, if an execution of code is requested by another thread
	 * when this method is being processed, the execution request might be missed.
	 * </span>
	 * <span class="lang-ja">
	 * なお, このメソッドの呼び出しと新規実行リクエストが, 別スレッドからシビアに競合したタイミングで行われた場合には,
	 * スクリプトは実行されない可能性がある事に留意してください.
	 * </span>
	 */
	public void resetTerminator() {
		synchronized (this.lock) {
			if (this.accelerator != null) {
				this.accelerator.resetTerminator();
			}
			if (this.processor != null) {
				this.processor.resetTerminator();
			}
		}
	}


	/**
	 * <span class="en">Returns the total number of processed instructions from when this VM was instantiated</span>
	 * <span class="ja">このVMのインスタンス生成時点から, 現在までに処理された命令数（累積処理命令数）を返します</span>
	 * .
	 * <span class="en">
	 * Note that, to lighten the decreasing of the performance caused by the counting/monitoring,
	 * the cached old value of the counter by the caller thread may be returned,
	 * so the precision of the returned value is not perfect.
	 * Especially when multiple processes are running on this VM,
	 * the counting will not be performed as exclusive process, so miscounting will occur to some extent.
	 * Also, please note that, when the counter value exceeds the positive maximum value of the int-type,
	 * it will not be reset to 0, and it will be the negative maximum value (minimum value on the number line)
	 * of the int-type, and will continue to be incremented from that value.
	 * For the above reason, it is recommended to get the value frequently enough
	 * (for example, --perf option of the command-line mode of the Vnano gets this value about 100 times per second),
	 * and use differences between them, not a raw value returned by this method.
	 * </span>
	 * <span class="ja">
	 * ただし, 計測による性能低下をなるべく抑えるため, 返されるカウンタ値の精度は完全ではなく,
	 * 呼び出し元スレッド等によってキャッシュされた少し前の値が返される可能性がある事に留意してください.
	 * 特に、命令列の実行を複数同時に行った際には、カウントに排他処理は行われないため加算漏れが生じます.
	 * また、値が int 型の上限に達してもリセットはされず,
	 * その後は負の端(int型で表現可能な数直線上の最小値)に至り,
	 * そこからまた加算され続ける事にも留意が必要です.
	 * そのため, 取得値をそのまま使うのではなく, 取得を十分な頻度
	 * （目安として, Vnano のコマンドラインモードの --perf オプションの処理では, この値の取得を毎秒 100 回程度行っています）
	 * で行って, 前回からの差分を求めて使用する事などが推奨されます.
	 * </span>
	 *
	 * @return
	 *   <span class="lang-en">The total number of processed instructions from when this VM was instantiated.</span>
	 *   <span class="lang-ja">このVMのインスタンス生成時点から, 現在までに処理された命令数.</span>
	 */
	// 名前が冗長なのは、将来的に値を long 型で取得可能なメソッドをサポートするかもしれないためなのと（その可能性自体は低い）、
	// メソッド名でそういう可能性をにおわせる事で、値の範囲が int で結構すぐ一周するという事に毎回気付けるようにするため
	public int getExecutedInstructionCountIntValue() {

		// この処理要求は恐らくコード実行スレッドとは別スレッドから投げられるのでスレッドキャッシュが影響する可能性がある
		synchronized (this.lock) {

			// Processor での前回参照時からの命令処理数（増分）をVMの累積命令数カウンタに加算
			if (this.processor != null) {
				int processorCurrentProcCount = this.processor.getExecutedInstructionCountIntValue();
				this.vmProcessedInstructionCount += processorCurrentProcCount - this.processorLastProcCount;
				this.processorLastProcCount = processorCurrentProcCount;
			}

			// Accelerator での前回参照時からの命令処理数（増分）をVMの累積命令数カウンタに加算
			if (this.accelerator != null) {
				int acceleratorCurrentProcCount = this.accelerator.getExecutedInstructionCountIntValue();
				this.vmProcessedInstructionCount += acceleratorCurrentProcCount - this.acceleratorLastProcCount;
				this.acceleratorLastProcCount = acceleratorCurrentProcCount;
			}

			// VMの累積命令数カウンタ値を返す
			return this.vmProcessedInstructionCount;
		}
	}


	/**
	 * <span class="lang-en">
	 * Returns operation code(s) of currently executed instruction(s) on this instance of the VM
	 * </span>
	 * <span class="lang-ja">
	 * このVMのインスタンス上において, 現在処理されている命令のオペレーションコードを返します
	 * </span>
	 * .
	 * <span class="lang-en">
	 * This method returns an array, because generary this VM may execute multiple instructions in 1 cycle.
	 * Also, when no instructins are being executed, an empty array will be returned.
	 * </span>
	 *
	 * <span class="lang-ja">
	 * 複数の命令が一括で同時に処理される場合があるため, 結果は配列として返されます.
	 * なお、何の命令も実行されていない場合には, 空の配列が返されます.
	 * </span>
	 *
	 * @return
	 *   <span class="lang-en">The opecode(s) of currently executed instruction(s) on this VM</span>
	 *   <span class="lang-ja">このVMにおいて現在処理されている命令のオペレーションコード (複数あり得るため配列)</span>
	 */
	public OperationCode[] getCurrentlyExecutedOperationCodes() {
		synchronized (this) {

			// 以下の processor と accelerator のインスタンスはこのクラスのコンストラクタで初期化されるが、
			// そのどちらかでも未初期化という事は、まだこのVMインスタンスの生成途中 ＝ 実行中の命令は無いという事なので、
			// (現実的に呼ばれるかどうかはともかくとして) 一応は仕様から素直に空配列を返しておく
			if (this.processor == null || this.accelerator == null) {
				return new OperationCode[0];
			}

			// Accelerator と Processor のそれぞれの最新オペコードの値を取得（有効な値が無い時は空配列が返る）
			OperationCode[] acceleratorOperationCodes = this.accelerator.getCurrentlyExecutedOperationCodes();
			OperationCode[] prpcessorOperationCodes = this.processor.getCurrentlyExecutedOperationCodes();

			// Accelerator 側に値がある場合は、処理は基本的に Accelerator で実行されていて、
			// Processor はその未対応命令を投げて処理してもらう用で、そのオペコードも既に含まれているので、
			// Accelerator 側の値をそのまま返す
			if (acceleratorOperationCodes.length != 0) {
				return acceleratorOperationCodes;

			// Accelerator 側に値が無く、Processor 側に値があった場合は、
			// オプションで Accelerator が無効化されているパターンで、処理は Processor のみで実行されているので、
			// Processor 側の値を返す
			} else if (prpcessorOperationCodes.length != 0) {
				return prpcessorOperationCodes;

			// Accelerator と Processor のどちらも値を持っていない場合は、両者とも何も実行していないという事になり、
			// つまるところVM全体として何も実行中ではないという事になるので、仕様に基づいて空配列を返す
			} else {
				return new OperationCode[0];
			}
		}
	}
}
