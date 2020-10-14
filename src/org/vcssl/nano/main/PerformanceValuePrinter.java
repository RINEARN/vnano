package org.vcssl.nano.main;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Map;

import org.vcssl.nano.VnanoEngine;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.spec.PerformanceKey;
import org.vcssl.nano.vm.VirtualMachine;

public class PerformanceValuePrinter implements Runnable {

	// 累積命令カウンタの精度は int で、表現範囲が±2G回くらいなので、数秒ごとに1周する。
	// 従って、余裕をもって毎秒数十回くらいの頻度で取得して、平均をとりつつ1秒あたりのスコアを求める。
	private static final int PROC_CNT_GETS_PER_SEC = 10; // 毎秒取得回数（大まかな精度での値）
	private static final int PROC_CNT_INTERVAL_WAIT = 1000 / PROC_CNT_GETS_PER_SEC; // 取得ループ待ち時間（ミリ秒）
	private double procIpsSum = 0.0;

	VnanoEngine vnanoEngine = null;
	VirtualMachine virtualMachine = null;

	private boolean printsVmSpeed = false;
	private boolean printsRamUsage = false;

	private volatile boolean continuable = true;

	private PerformanceValuePrinter(boolean printsVmSpeed, boolean printsRamUsage) {
		this.printsVmSpeed = printsVmSpeed;
		this.printsRamUsage = printsRamUsage;
	}

	public PerformanceValuePrinter(VnanoEngine vnanoEngine, boolean printsVmSpeed, boolean printsRamUsage) {
		this(printsVmSpeed, printsRamUsage);
		this.vnanoEngine = vnanoEngine;
	}

	public PerformanceValuePrinter(VirtualMachine virtualMachine, boolean printsVmSpeed, boolean printsRamUsage) {
		this(printsVmSpeed, printsRamUsage);
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

			// VMでの現在の累積命令実行数を取得（int なので頻繁に一周する事に注意）
			int currentProcCount = 0;
			try {
				// 通常のスクリプト処理の場合
				if (this.vnanoEngine != null) {
					Map<String, Object> performanceMap = this.vnanoEngine.getPerformanceMap();

					// 実行開始後、計測可能なタイミングにまだ達していない時には、値が格納されていない(仕様)ので検査してから参照
					if (performanceMap.containsKey(PerformanceKey.PROCESSED_INSTRUCTION_COUNT_INT_VALUE)) {
						currentProcCount = (int)performanceMap.get(PerformanceKey.PROCESSED_INSTRUCTION_COUNT_INT_VALUE);
					}
				}
				// コンパイルしてファイルにダンプしたVRILコードを、コマンドラインモードで読み込んで処理している場合
				if (this.virtualMachine != null) {
					// こちらは常に何らかの値を返す（計測可能になるタイミングより前には 0 を返す）
					currentProcCount = this.virtualMachine.getProcessedInstructionCountIntValue();
				}
			} catch (VnanoException vne) {
				vne.printStackTrace();
			}

			// 前回取得したカウンタ値の差分と、取得間隔の時間から、1秒あたりのVM命令処理数（IPS）を求める
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
}
