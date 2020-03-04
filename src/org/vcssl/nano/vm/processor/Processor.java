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


	/**
	 * このクラスは状態を保持するフィールドを持たないため、コンストラクタは何もしません。
	 */
	public Processor() {
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
	 * @param optionMap オプション内容を保持するマップ
	 * @throws VnanoException
	 *   形式は問題なく、実行するまで成功・失敗が不明な命令（型変換など）の実行に失敗した際などにスローされます。
	 * @throws VnanoFatalException
	 *   命令の形式の異常や、仮想メモリーへのアクセスの異常、およびデータの内容や状態に異常があった場合などにスローされます。
	 */
	public void process(Instruction[] instructions, Memory memory, Interconnect interconnect, Map<String, Object> optionMap)
			throws VnanoException {

		// オプションマップから指定内容を取得
		boolean shouldDump = (Boolean)optionMap.get(OptionKey.DUMPER_ENABLED);        // ダンプするかどうか
		String dumpTarget = (String)optionMap.get(OptionKey.DUMPER_TARGET);           // ダンプ対象
		boolean dumpTargetIsAll = dumpTarget.equals(OptionValue.DUMPER_TARGET_ALL);   // ダンプ対象が全てかどうか
		PrintStream dumpStream = (PrintStream)optionMap.get(OptionKey.DUMPER_STREAM); // ダンプ先ストリーム
		boolean shouldRun = (Boolean)optionMap.get(OptionKey.RUNNING_ENABLED);        // コードを実行するかどうか


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

			// 命令を1個実行し、プログラムカウンタの値を更新
			try {
				programCounter = dispatchUnit.dispatch(
					instructions[programCounter], memory, interconnect, executionUnit, functionRunningFlags, programCounter
				);

			// 想定内の実行時例外は、下層で既に詳細情報等が埋め込まれているので、そのまま上層に投げる
			} catch (VnanoException vne) {
				throw vne;

			// 想定外の実行時例外は、原因箇所などの情報を補完した例外に変換し、上層に投げる
			} catch (Exception e) {

				// 命令のメタ情報から、スクリプト内で命令に対応する箇所のファイル名や行番号を抽出
				int lineNumber = MetaInformationSyntax.extractLineNumber(instructions[programCounter], memory);
				String fileName = MetaInformationSyntax.extractFileName(instructions[programCounter], memory);

				// 抽出したスクリプト名や行番号を持つ例外を生成して投げる
				throw new VnanoException(ErrorType.UNEXPECTED, e, fileName, lineNumber);
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

}
