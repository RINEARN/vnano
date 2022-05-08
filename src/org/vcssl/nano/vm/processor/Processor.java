/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.processor;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.MetaInformationSyntax;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.spec.OptionKey;
import org.vcssl.nano.spec.OptionValue;
import org.vcssl.nano.vm.memory.Memory;


/**
 * <p>
 * Vnano処理系内において、中間コードを解釈して実行する、仮想プロセッサ（VM）のクラスです。
 * </p>
 *
 * <p>
 * ここでの仮想プロセッサとは、
 * 仮想的な命令セットに基づく中間コードを解釈してプログラムを動作させる
 * 「プロセス仮想マシン」としてのVMの事を指しています。
 * 単にVMと言うと、
 * システム全体を仮想化して異なるOSを動作させる「システム仮想マシン」を指す事も多いため、
 * Vnano処理系においては、プロセス仮想マシンを明示的かつ短く指す意図で、
 * 仮想プロセッサという呼称を使用しています。
 * ただし、システム仮想マシン上で利用するCPUを仮想プロセッサと呼ぶ事もあるため、
 * いずれにしても混同には一定の留意が必要です。
 * </p>
 *
 * <p>
 * 現在のVnano処理系の仮想プロセッサは、レジスタマシンのアーキテクチャを採用しています。
 * ただし、レジスタを含むデータ類は、仮想プロセッサ内ではなく、
 * 仮想メモリーである {@link org.vcssl.nano.vm.memory.Memory Memory} オブジェクト側に保持されます
 * （ただし、高速化のためにプロセッサ内にキャッシュされる事はあります）。
 * </p>
 *
 * <p>
 * この仮想プロセッサにおける命令は、
 * 1個の命令が1個の {@link Instruction Instruction} オブジェクトとして表され、
 * それはオペコードに加えて、オペランドとして演算対象データの仮想メモリー内アドレスなどを保持しています。
 * 仮想プロセッサは、まずこの命令のオペコード部を解釈し、
 * そしてオペランド部でアドレス指定されている仮想メモリー内のデータに対して、
 * オペコードに対応する演算を行って書き換えます。
 * 命令列は {@link Instruction Instruction} オブジェクトの配列で表され、
 * 仮想プロセッサは、その命令列を先頭から終端まで、
 * ジャンプなどを除けば素直な順序で逐次実行します。
 * </p>
 *
 * <p>
 * 具体的な例として、{@link Instruction Instruction}
 * オブジェクトの内容を仮想アセンブリコードの書式で表した以下の命令：
 * </p>
 *
 * <div style="border-style: solid; padding-left: 10px; margin:10px;">
 * ADD &nbsp;&nbsp; INT64 &nbsp;&nbsp; R0 &nbsp;&nbsp; L24 &nbsp;&nbsp; L36 <br>
 * （オペコード、型情報、オペランド、オペランド、オペランド）
 * </div>
 *
 * <p>
 * を実行する場合、仮想プロセッサはまず仮想メモリー内の
 * {@link org.vcssl.nano.vm.memory.Memory.Partition#LOCAL LOCAL}
 * パーティション内の24番および36番アドレスからデータを読み込み、
 * それらを64bit符号付き整数と見なして {@link OperationCode#ADD ADD} 命令つまり加算を行って、
 * 最後に加算結果を仮想メモリー内の
 * {@link org.vcssl.nano.vm.memory.Memory.Partition#REGISTER REGISTER}
 * パーティション内の0番アドレス（即ち0番レジスタ）に書き込みます。
 * </p>
 *
 * <p>
 * なお、この仮想プロセッサは、ベクトル演算を主体とする命令セットを採用しています。
 * 命令の種類の一覧と詳細は、
 * オペコードを表す {@link OperationCode OperationCode} 列挙子の説明として記載されています。
 * </p>
 *
 * <p>
 * 例えば、上の例に登場した {@link OperationCode#ADD ADD} 命令は、
 * 2つの配列の各要素をそれぞれ加算し、結果を配列に格納するベクトル演算命令です。
 * そしてオペランドとして指定されている、R0 や L24、L36 などのアドレスの参照先にあるデータも、全て配列です。
 * {@link OperationCode#ADD ADD} 命令だけでなく、殆どの命令はベクトル演算命令です。
 * また、全ての命令のオペランドは配列です。
 * スカラの演算は、要素数が 1 の（かつ次元数パラメータが 0 の）配列の演算として実行されます。
 * </p>
 *
 * <p>
 * このような仮想プロセッサの仕様に合わせて、Vnano処理系内では、全てのデータが配列単位で扱われます。
 * 詳細は {@link org.vcssl.nano.vm.memory.DataContainer DataContainer} クラスや
 * {@link org.vcssl.nano.vm.memory.Memory Memory} クラスの説明を参照してください。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class Processor implements Processable {

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
	private int executedInstructionCount;

	/** 現在処理中の命令のオペレーションコードを保持します。 */
	// パフォーマンスモニタでオペレーションコードごとの命令実行頻度をなどを解析するため
	private OperationCode currentOperationCode;

	/** synchronized ブロック用のロック対象オブジェクトです。（主にスレッドキャッシュ剥がしに使うのでこのロック自体にあまり意味は無い） */
	private final Object lock;


	/**
	 * 新しい仮想プロセッサのインスタンスを生成します。
	 */
	public Processor() {
		this.continuable = true;
		this.executedInstructionCount = 0;
		this.currentOperationCode = null;
		this.lock = new Object();
	}


	/**
	 * 指定された命令列を実行します。
	 *
	 * 命令列は、原則として先頭から末尾までの順で、一つずつ逐次的に実行されます。
	 * ただし、{@link OperationCode#JMP JMP} や {@link OperationCode#JMPN JMPN}
	 * などの分岐命令が実行されると、実行対象の命令位置は別の場所にジャンプします。
	 * 次に実行すべき命令がもう無くなった時点で
	 * （つまりプログラムカウンタが命令例の境界外を指そうとした時点で）、
	 * このメソッドの実行は終了し、処理が戻されます。
	 *
	 * @param instructions 命令列
	 * @param memory 命令列の実行に使用する済み仮想メモリー
	 * @param interconnect 外部関数プラグインが接続されているインターコネクト（呼び出しに使用）
	 * @throws VnanoException
	 *   形式は問題なく、実行するまで成功・失敗が不明な命令（型変換など）の実行に失敗した際などにスローされます。
	 * @throws VnanoFatalException
	 *   命令の形式の異常や、仮想メモリーへのアクセスの異常、およびデータの内容や状態に異常があった場合などにスローされます。
	 */
	public void process(Instruction[] instructions, Memory memory, Interconnect interconnect) throws VnanoException {

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

		// 加減算やその他様々な演算処理を行う演算ユニット
		ExecutionUnit executionUnit = new ExecutionUnit();

		// 命令に対応する処理を演算ユニットに割り当てる（ディスパッチする）ディスパッチユニット
		DispatchUnit dispatchUnit = new DispatchUnit();

		// 次の実行対象命令位置を示すプログラムカウンタ（命令列の先頭から実行するので初期値0）
		int programCounter = 0;

		// 命令列の長さ（＝プログラムカウンタにとっては命令列の末尾+1を指すインデックス）
		int instructionLength = instructions.length;

		// この処理系では関数の再帰呼び出しをサポートしていないため、CALL命令で実行する関数が既に実行中かどうか確認し、
		// 実行中ならエラーにするためのテーブル（インデックスは関数先頭の命令アドレス）
		boolean[] functionRunningFlags = new boolean[instructionLength];
		Arrays.fill(functionRunningFlags, false);

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
		// (プログラムカウンタが命令列の末尾に達するまで、1個ずつ命令を演算ユニットにディスパッチして実行する)
		while (0 <= programCounter && programCounter < instructionLength) {
			if (terminatable && !this.continuable) {  // この continuable は volatile
				break;
			}

			// パフォーマンスモニタリング用に、最新の実行対象命令のオペレーションコードをフィールドに控える
			this.currentOperationCode = instructions[programCounter].getOperationCode();

			// 命令を1個実行し、プログラムカウンタの値を更新
			try {
				programCounter = dispatchUnit.dispatch(
					instructions[programCounter], memory, interconnect, executionUnit, functionRunningFlags, programCounter
				);

				// 性能計測関連の処理（現状は累積処理命令数のカウントのみ）
				if (monitorable) {
					// 注: 以下 executedInstructionCount の更新、
					// long だと 32bit x 2 書き込みになる場合の値化け予防のため synchronized で囲う必要があるが、int なので不要
					// (読んでから足して書き戻すまでの間のラグ誤差は、カウンタの精度仕様的に許容される。getterのコメント参照)
					// long 化する場合は素直に synchronized すると遅いので、数百回に一回 int 差分を synchronized 可算する等を要検討
					this.executedInstructionCount++;
				}

			} catch (Exception e) {

				// VnanoException はそのまま、それ以外は VnanoException でラップする
				VnanoException vne = null;
				if (e instanceof VnanoException) {
					vne = (VnanoException)e;
				} else {
					vne = new VnanoException(
						ErrorType.UNEXPECTED_PROCESSOR_CRASH, new String[] {Integer.toString(programCounter)}, e
					);
				}

				// 命令のメタ情報から、スクリプト内で命令に対応する箇所のファイル名や行番号を抽出
				int lineNumber = MetaInformationSyntax.extractLineNumber(instructions[programCounter], memory);
				String fileName = MetaInformationSyntax.extractFileName(instructions[programCounter], memory);

				// 抽出したスクリプト名や行番号を例外に持たせ、上層に投げる
				vne.setFileName(fileName);
				vne.setLineNumber(lineNumber);
				throw vne;
			}
		}
		this.currentOperationCode = null;


		// ダンプ内容に実行終了点を表す区切りを入れる
		if (shouldDump && dumpTargetIsAll) {
			dumpStream.println("");
			dumpStream.println("================================================================================");
			dumpStream.println("= End");
			dumpStream.println("================================================================================");
		}
	}


	/**
	 * 指定された単一の命令を実行し、指定されたプログラムカウンタの値を更新して返します。
	 *
	 * ただし、{@link OperationCode#CALL CALL} 命令や {@link OperationCode#RET RET} 命令など、
	 * 関数関連の命令をこのメソッドで実行した場合、関数の再帰呼び出し（未サポート）の検出やエラー処理は行われません。
	 *
	 * @param instruction 命令
	 * @param memory 命令の実行に使用する済み仮想メモリー
	 * @param interconnect 外部関数プラグインが接続されているインターコネクト（呼び出しに使用）
	 * @param programCounter 命令実行前のプログラムカウンタの値
	 * @throws VnanoException
	 *   形式は問題なく、実行するまで成功・失敗が不明な命令（型変換など）の実行に失敗した際などにスローされます。
	 * @throws VnanoFatalException
	 *   命令の形式の異常や、仮想メモリーへのアクセスの異常、およびデータの内容や状態に異常があった場合などにスローされます。
	 */
	public int process(Instruction instruction, Memory memory, Interconnect interconnect, int programCounter)
			throws VnanoException {

		// 加減算やその他様々な演算処理を行う演算ユニット
		ExecutionUnit executionUnit = new ExecutionUnit();

		// 命令に対応する処理を演算ユニットに割り当てる（ディスパッチする）ディスパッチユニット
		DispatchUnit dispatchUnit = new DispatchUnit();

		// 命令を1個実行し、更新されたプログラムカウンタの値を返す
		return dispatchUnit.dispatch(instruction, memory, interconnect, executionUnit, null, programCounter);
	}


	/**
	 * 現在実行中の命令の処理が終わった時点で、残りの命令列の実行を行わずに終了させます。
	 *
	 * 命令列の実行が複数同時に行われている場合、それら各々の実行時に Interconnect 経由で指定されたオプションにおいて、
	 * {@link org.vcssl.nano.spec.OptionKey#TERMINATOR_ENABLED TERMINATOR_ENABLED} が有効化されていたもののみが終了され、
	 * 無効であったものには何も起こらず継続して処理されます。
	 *
	 * なお、このメソッドで実行を終了させた後に、
	 * 再び {@link Processor#process(Instruction[], Memory, Interconnect) process(...)}
	 * メソッドによって(新規に)命令列を実行する際には、事前に
	 * {@link Processor#resetTerminator() resetTerminator()()} を呼び出す必要があります。
	 */
	public void terminate() {
		this.continuable = false;  // volatile
	}


	/**
	 * {@link Processor#terminate() terminate()} メソッドによって終了させた仮想プロセッサを、再び実行可能な状態に戻します。
	 */
	// process() メソッドが呼ばれてからその中で自動的に true にする方式だと、呼んだ直後に terminate() で false 化した場合に、
	// タイミングによっては process() 側での true 化の方が後になって、処理が続行されてしまう可能性があるので注意。
	// process() の最後に true に戻して次回実行に備える方式でも、terminate() のタイミングがまずいと false 化され、
	// 逆に次回実行不可能になってしまう可能性がある。従って、搭載アプリ側で適切な時にフラグをリセットしてもらう。
	public void resetTerminator() {
		this.continuable = true;  // volatile
	}


	/**
	 * この仮想プロセッサのインスタンス化時点から、現在までに処理された命令数を、おおまかな目安値として返します。
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
	 * @return この仮想プロセッサのインスタンス化時点から、現在までに処理された命令数の、おおまかな目安値
	 */
	// 名前が冗長なのは、将来的に値を long 型で取得可能なメソッドをサポートするかもしれないためなのと（その可能性自体は低い）、
	// メソッド名でそういう可能性をにおわせる事で、値の範囲が int で結構すぐ一周するという事に毎回気付けるようにするため
	public int getExecutedInstructionCountIntValue() {
		synchronized (this.lock) {
			return this.executedInstructionCount;
		}
	}


	/**
	 * この仮想プロセッサにおいて、現在実行されている命令のオペレーションコードを返します。
	 *
	 * 結果は配列として返されますが、この仮想プロセッサの実装においては、
	 * 複数の命令が 1 サイクルで一括実行される事は無いため、通常は要素数が 1 の配列が返されます。
	 * ただし、何の命令も実行されていない時には、空の配列が返されます。
	 *
	 * なお、複数のスレッドにおいて、この仮想プロセッサの同一インスタンスを用いて、
	 * 並列に実行を行った場合でも、このメソッドが返すオペレーションコードの数は増えません。
	 * このメソッドは、単に命令実行ループ毎にフィールドに控えられたオペレーションコードの最新値を返すだけであるため、
	 * そのような場合には、どれかのスレッド（常に同一とは限りません）が書き換えた値が返されます。
	 *
	 * @return この仮想プロセッサにおいて、現在実行されている命令のオペレーションコード
	 */
	public OperationCode[] getCurrentlyExecutedOperationCodes() {
		synchronized (this.lock) {
			if (this.currentOperationCode == null) {
				return new OperationCode[0];
			}
			return new OperationCode[] { this.currentOperationCode };
		}
	}

}
