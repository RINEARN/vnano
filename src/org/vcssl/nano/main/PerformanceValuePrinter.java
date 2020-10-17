/*
 * Copyright(C) 2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.main;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.spec.PerformanceKey;
import org.vcssl.nano.vm.VirtualMachine;

public class PerformanceValuePrinter implements Runnable {

	// 累積命令カウンタの精度は int で、表現範囲が±2G回くらいなので、数秒ごとに1周する。
	// 従って、余裕をもって毎秒数十回～百回くらいの頻度で取得して、平均をとりつつ1秒あたりのスコアを求める。
	private static final int PROC_CNT_GETS_PER_SEC = 100; // 毎秒取得回数（1000以下で指定 / ウェイトの精度からして大まかな目安値程度）
	private static final int PROC_CNT_INTERVAL_WAIT = 1000 / PROC_CNT_GETS_PER_SEC; // 取得ループ待ち時間（ミリ秒）
	private double procIpsSum = 0.0;


	// サンプリングしたオペコードの総数
	private long operationCodeCountTotal;

	// サンプリングしたオペコードの名前をキーとして、
	// 重み付き検出回数（複数命令が同時に検出された時に 1/N 回とカウント）を格納するマップ
	private HashMap<String, Double> operationCodeCountMap; // clone するので抽象的な Map ではなく HashMap

	VnanoEngine vnanoEngine = null;
	VirtualMachine virtualMachine = null;

	private boolean printsVmSpeed = false;
	private boolean printsRamUsage = false;
	private boolean printsInstructionFrequency = false;

	private volatile boolean continuable = true;

	private PerformanceValuePrinter(boolean printsVmSpeed, boolean printsRamUsage, boolean printsInstructionFrequency) {
		this.printsVmSpeed = printsVmSpeed;
		this.printsRamUsage = printsRamUsage;
		this.printsInstructionFrequency = printsInstructionFrequency;
		this.operationCodeCountMap = new HashMap<String, Double>();
		this.operationCodeCountTotal = 0L;
	}

	public PerformanceValuePrinter(VnanoEngine vnanoEngine,
			boolean printsVmSpeed, boolean printsRamUsage, boolean printsInstructionFrequency) {

		this(printsVmSpeed, printsRamUsage, printsInstructionFrequency);
		this.vnanoEngine = vnanoEngine;
	}

	public PerformanceValuePrinter(VirtualMachine virtualMachine,
			boolean printsVmSpeed, boolean printsRamUsage, boolean printsInstructionFrequency) {

		this(printsVmSpeed, printsRamUsage, printsInstructionFrequency);
		this.virtualMachine = virtualMachine;
	}

	public void terminate() {
		this.continuable = false;
	}

	@Override
	public void run() {
		int lastProcCount = 0;
		int loopCount = 0;
		long lastNanoTime = System.nanoTime();
		while (this.continuable) {

			// ループウェイト
			try {
				Thread.sleep(PROC_CNT_INTERVAL_WAIT);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
				break;
			}


			// 以下、エンジンから最新の計測値を取得
			int currentProcCount = 0;// VMでの現在の累積命令実行数（int なので頻繁に一周する事に注意）
			String[] currentOpcodeNames = new String[0]; // 現在の実行対象命令のオペコード（複数あり得るので配列）の文字列表現
			try {

				// 通常のスクリプト処理の場合
				if (this.vnanoEngine != null) {
					Map<String, Object> performanceMap = this.vnanoEngine.getPerformanceMap();

					// 実行開始後、計測可能なタイミングにまだ達していない時には、値が格納されていない(仕様)ので検査してから参照
					if (performanceMap.containsKey(PerformanceKey.EXECUTED_INSTRUCTION_COUNT_INT_VALUE)) {
						currentProcCount = (int)performanceMap.get(PerformanceKey.EXECUTED_INSTRUCTION_COUNT_INT_VALUE);
					}
					if (performanceMap.containsKey(PerformanceKey.CURRENTLY_EXECUTED_OPERATION_CODE)) {
						currentOpcodeNames = (String[])performanceMap.get(PerformanceKey.CURRENTLY_EXECUTED_OPERATION_CODE);
					}
				}

				// コンパイルしてファイルにダンプしたVRILコードを、コマンドラインモードで読み込んで処理している場合
				if (this.virtualMachine != null) {

					// virtualMachine は本来は表層ではなく内側のクラスなので、上でのパフォーマンスマップのように
					//「無い値は詰めない」という対処はされておらず、常に何らかの値を返す。
					// 従って中身で判断する必要がある。型も汎用的な型ではなくエンジン内部の実装用クラスのままだったりするので変換する。

					currentProcCount = this.virtualMachine.getExecutedInstructionCountIntValue(); // まだ何も実行していない場合は 0
					OperationCode[] currentOpcodes = this.virtualMachine.getCurrentlyExecutedOperationCodes(); // まだ値が無い場合は空配列
					currentOpcodeNames = new String[ currentOpcodes.length ];
					for (int i=0; i<currentOpcodes.length; i++) {
						currentOpcodeNames[i] = currentOpcodes[i].toString();
					}
				}

			} catch (VnanoException vne) {
				vne.printStackTrace();
			}


			// オペコードごとの検出回数を控える（重み付き）カウンタマップに、現在実行中の命令のオペコードの分（下記参照）を加算
			if (currentOpcodeNames.length != 0) {
				// カウンタの増分は通常は 1 で、N個の命令の同時に実行されている場合にはN等分する
				double opcodeCountWeight = 1.0 / currentOpcodeNames.length;
				for (String opcode: currentOpcodeNames) {
					double latestValue = this.operationCodeCountMap.containsKey(opcode) ?
							this.operationCodeCountMap.get(opcode).doubleValue() : 0.0;
					double updatedValue = latestValue + opcodeCountWeight;
					this.operationCodeCountMap.put(opcode, Double.valueOf(updatedValue));
				}
			}
			this.operationCodeCountTotal += currentOpcodeNames.length;


			// 前回取得した累積命令数カウンタ値の差分と、取得間隔の時間から、1秒あたりのVM命令処理数（IPS）を求める
			long currentNanoTime = System.nanoTime();
			double countIntervalSec = (currentNanoTime - lastNanoTime) * 1.0E-9;    // 秒単位の取得間隔時間(1ナノ秒は10のマイナス9乗秒)
			double procIps = (currentProcCount - lastProcCount) / countIntervalSec; // 1秒あたり命令処理数

			// カウンタが int の都合上、取得は頻度に行うが、すぐ表示するには高頻度すぎるので、平均しつつたまに表示する
			this.procIpsSum += procIps;

			// 通常は上での取得値を「前回の値」として控えて、そのままループの次サイクルへ
			lastProcCount = currentProcCount;
			lastNanoTime = currentNanoTime;
			loopCount++;

			// ただし、だいたい1秒ごとくらいのタイミング（ラグあり）で性能値を表示し、平均用変数やループカウンタなどもリセットする
			if (loopCount == PROC_CNT_GETS_PER_SEC) {
				System.out.println("================================================================================");
				String timestamp = new Timestamp(System.currentTimeMillis()).toString();
				System.out.println("= Performance Monitor (" + timestamp + ")");

				if (this.printsVmSpeed) {
					double procIpsAverage = this.procIpsSum / PROC_CNT_GETS_PER_SEC;
					String formattedIps = this.formatIpsValue(procIpsAverage);
					System.out.println("= - VM Speed  = " + formattedIps);
				}

				if (this.printsRamUsage) {
					long ramTotalBytes = Runtime.getRuntime().totalMemory();
					long ramFreeBytes = Runtime.getRuntime().freeMemory();
					long ramMaxBytes = Runtime.getRuntime().maxMemory();
					String formattedRamUsage = this.formatRamBytes(ramTotalBytes - ramFreeBytes);
					String formattedRamMax = this.formatRamBytes(ramMaxBytes);
					System.out.println("= - RAM Usage = " + formattedRamUsage + " (Max " + formattedRamMax + " Available)");
				}

				if (this.printsInstructionFrequency) {
					String formattedInstructionFrequency = this.formatInstructionFrequency(this.operationCodeCountMap);
					System.out.println("= - Instruction Execution Frequency :");
					System.out.print(formattedInstructionFrequency);
					System.out.println("    (Total " + this.operationCodeCountTotal + " Samples)");
				}

				this.procIpsSum = 0.0;
				loopCount = 0;
				System.out.println("================================================================================");
			}
		}
	}

	// VM動作速度の double 値を、「 1.23 GHz 」とか表示用のいい感じの形に調整する
	private String formatIpsValue(double ipsRawValue) {
		double value = ipsRawValue;
		String unitPrefix = "";
		if (value > 1000L*1000L*1000L) {
			unitPrefix = "G";
			value /= 1000L*1000L*1000L;
		} else if (value > 1000L*1000L) {
			unitPrefix = "M";
			value /= 1000L*1000L;
		} else if (value > 1000L) {
			unitPrefix = "K";
			value /= 1000L;
		}
		DecimalFormat formatter = new DecimalFormat("0.0");
		String formattedValue = formatter.format(value) + " " + unitPrefix + "Hz (VRIL Instructions / sec)";
		return formattedValue;
	}

	// RAM使用量とかで使うバイト数の long 値を、「 1.23 GiB 」とか表示用のいい感じの形に調整する
	private String formatRamBytes(long ramBytes) {
		double value = ramBytes;
		String unitPrefix = "";
		if (value > 1024L*1024L*1024L) {
			unitPrefix = "Gi";
			value /= 1024L*1024L*1024L;
		} else if (value > 1024L*1024L) {
			unitPrefix = "Mi";
			value /= 1024L*1024L;
		} else if (value > 1024L) {
			unitPrefix = "Ki";
			value /= 1024L;
		}
		DecimalFormat formatter = new DecimalFormat("0.0");
		String formattedValue = formatter.format(value) + " " + unitPrefix + "B";
		return formattedValue;
	}

	// 命令のオペコードの検出回数マップを、頻度分布として表示用のいい感じの形に調整する
	private String formatInstructionFrequency(HashMap<String, Double> operationCodeCountMap) {
		StringBuilder builder = new StringBuilder();
		String eol = System.getProperty("line.separator");

		// 値の加工用にマップを clone し、その キーと値のセット（加工用）を取得
		@SuppressWarnings("unchecked")
		HashMap<String, Double> modifiedMap = (HashMap<String, Double>)operationCodeCountMap.clone();
		Set<Map.Entry<String, Double>> modifiedEntrySet = modifiedMap.entrySet();

		// 各要素（オペコードと検出回数をまとめたEntry）を検出回数基準で降順ソート
		List<Map.Entry<String, Double>> modifiedEntryList = new ArrayList<>(modifiedEntrySet);
		Collections.sort(modifiedEntryList, new CountEntryComparator());

		// 検出回数を総和で割って 0 ～ 1 の頻度値に規格化する（パーセンテージ変換は後で丸めと一緒に行う）
		double total = 0.0;
		for (Map.Entry<String, Double> modifiedEntry: modifiedEntryList) {
			total += modifiedEntry.getValue().doubleValue();
		}
		for (Map.Entry<String, Double> modifiedEntry: modifiedEntryList) {
			modifiedEntry.setValue( Double.valueOf(modifiedEntry.getValue().doubleValue() / total) );
		}

		// 頻度値をパーセンテージに変換しながら、丸めて文字列化し、バッファに書き込んでいく
		DecimalFormat percentageFormatter = new DecimalFormat("0.00");
		DecimalFormat countFormatter = new DecimalFormat("0.#"); // 複数同時検出の際に 1/N する重み付きカウントなので、小数部が付く場合もある
		for (Map.Entry<String, Double> modifiedEntry: modifiedEntryList) {

			// 表示用のオペレーションコード名を、空白で同じ長さに揃える (REFELEMが最長で7文字なので、7文字に揃える)
			String opcode = String.format("%7s", modifiedEntry.getKey().toString());

			// 加工済み頻度値（パーセンテージ）
			String percentage = percentageFormatter.format( modifiedEntry.getValue().doubleValue() * 100.0 ); // double の桁数を丸める
			percentage = String.format("%6s", percentage); // 文字列の長さを揃える

			// 未加工の頻度値（カウント数）
			String count = countFormatter.format( operationCodeCountMap.get(modifiedEntry.getKey()) );

			// 表示（バッファ書き込み）
			builder.append("    - " + opcode + " : " + percentage + " %   (" + count + " count)" + eol);
		}
		return builder.toString();
	}

	class CountEntryComparator implements Comparator<Map.Entry<String, Double>> {
		@Override
		public int compare(Map.Entry<String, Double> a, Map.Entry<String, Double> b) {
			//double diff = a.getValue() - b.getValue(); // 昇順
			double diff = b.getValue() - a.getValue(); // 降順(最頻命令を上にしたいのでこっちの方がいい)
			return (int)Math.signum(diff);
			// ※ signum の戻り値は double だが絶対値が 1.0 か 0.0 のみで2進で完全表現できるので、int に精度落ちなくキャストできる
		}
	}

}
