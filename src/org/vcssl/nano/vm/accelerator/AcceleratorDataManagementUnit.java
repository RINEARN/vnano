/*
 * Copyright(C) 2018-2021 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.interconnect.AbstractFunction;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.processor.Instruction;

public final class AcceleratorDataManagementUnit {

	// 実装メモ：
	//   ここでスカラかどうかを検出して演算時の最適化に使ってるけれど、
	//   そもそもVRILの命令に演算次元数情報を含めるようにしたほうがいいかもしれない。
	//
	//   >  各演算を最適化しているというよりは、各アドレスのI/Oそのものを最適化したくて、
	//      その結果として各演算処理もスカラかどうかで分派しているという感じなので、
	//      命令から演算がスカラかどうか分かったとしてもそれだけでは不十分だと思うし、
	//      ( 演算オペランドがスカラという事だけ分かっても、例えば配列要素と独立変数のスカラではI/O最適化の幅が全然違うし、
	//        関数引数なら参照渡しか値渡しかでも全然違う )
	//      結局はここでやっているように、命令列をある程度読んで
	//      各アドレスのI/Oの最適化可能性の度合いを判断する必要は残ると思う。
	//
	//   >  命令レベルで制約を加えるのは、慎重にしないと別の派生言語を載せる際に動的さを制限するデメリットになり得るかも。
	//      > 既にデータ型情報は命令内に持ってしまっているので、次元数情報が加わってもそこからマイナスにはならないような。
	//        影響するのは「データ型が静的で次元数が動的」みたいなな中途半端な演算子を作れるかどうか位で、そんなん作る事は無い気がする。
	//        なので、次元情報を実際使うかどうかはともかく、あるのは別にあっていいような。
	//
	//   >  というか、コンパイラ側のコードジェネレータは各アドレスの由来を知っているので、
	//      むしろ命令列とは別に、アドレスと（最適化に有用な）性質を対応させるテーブルを別途吐いたほうがいいかもしれない。
	//      > ただ最適化周りのコードは極力 accelerator パッケージ内のみに抑えられたほうがうれしいので、そういう点では微妙かもしれない。
	//
	// 一旦塩漬けして要検討

	private static final int REGISTER_PARTITION_ORDINAL = Memory.Partition.REGISTER.ordinal();
	private static final int LOCAL_PARTITION_ORDINAL = Memory.Partition.LOCAL.ordinal();
	private static final int GLOBAL_PARTITION_ORDINAL = Memory.Partition.GLOBAL.ordinal();
	private static final int CONSTANT_PARTITION_ORDINAL = Memory.Partition.CONSTANT.ordinal();
	private static final int STACK_PARTITION_ORDINAL = Memory.Partition.STACK.ordinal();
	private static final int NONE_PARTITION_ORDINAL = Memory.Partition.NONE.ordinal();
	private static final int PARTITION_LENGTH = Memory.Partition.values().length;

	// [Partition][Address]
	private ScalarCache[][] caches = null;
	private boolean[][] cachingEnabled = null;
	private boolean[][] scalar = null;

	// [Partition]
	CacheSynchronizer[] synchronizers;

	// 内部関数アドレスをキーとして、引数が参照渡しであるかどうかを格納するマップ
	private Map<Integer, boolean[]> internalFunctionAddrParamRefsMap = null;

	private int registerSize = -1;
	private int localSize = -1;
	private int globalSize = -1;
	private int constantSize = -1;
	private int stackSize = -1;
	private int noneSize = -1;

	public boolean isCachingEnabled(Memory.Partition partition, int address) {
		return this.cachingEnabled[ partition.ordinal() ][ address ];
	}

	public boolean[] getCachedFlags(Memory.Partition partition) {
		return this.cachingEnabled[ partition.ordinal() ];
	}

	public ScalarCache getCache(Memory.Partition partition, int address) {
		return this.caches[ partition.ordinal() ][ address ];
	}

	public ScalarCache[] getCaches(Memory.Partition partition) {
		return this.caches[ partition.ordinal() ];
	}

	public boolean isScalar(Memory.Partition partition, int address) {
		return this.scalar[ partition.ordinal() ][ address ];
	}

	public CacheSynchronizer getCacheSynchronizers(Memory.Partition partition) {
		return this.synchronizers[partition.ordinal()];
	}

	public void allocate(
			Instruction[] instructions, Memory memory, Interconnect interconnect,
			int optimizationLevel) {

		this.initializeFields(memory);
		this.analyzeInternalFunctionInformation(instructions, memory);
		this.detectScalarFromMemory(memory, Memory.Partition.CONSTANT);
		this.detectScalarFromMemory(memory, Memory.Partition.GLOBAL);
		this.detectScalarFromInstructions(instructions, memory, interconnect);

		// 最適化レベルが特定値以下)なら、キャッシュを使わないように無効化する
		if (optimizationLevel <= AcceleratorOptimizationUnit.OPT_LEVEL_CACHE_DISABLED) {
			this.disableAllCaches();
		}

		this.initializeCacheSynchronizers(memory);
	}

	private void initializeFields(Memory memory) {

		this.registerSize = memory.getSize(Memory.Partition.REGISTER);
		this.localSize = memory.getSize(Memory.Partition.LOCAL);
		this.globalSize = memory.getSize(Memory.Partition.GLOBAL);
		this.constantSize = memory.getSize(Memory.Partition.CONSTANT);
		this.stackSize = 0;
		this.noneSize = 1;

		this.internalFunctionAddrParamRefsMap = new HashMap<Integer, boolean[]>();

		// [Partition][Address]
		this.caches = new ScalarCache[PARTITION_LENGTH][];
		this.cachingEnabled = new boolean[PARTITION_LENGTH][];
		this.scalar = new boolean[PARTITION_LENGTH][];

		this.scalar[REGISTER_PARTITION_ORDINAL] = new boolean[registerSize];
		this.cachingEnabled[REGISTER_PARTITION_ORDINAL] = new boolean[registerSize];
		this.caches[REGISTER_PARTITION_ORDINAL] = new ScalarCache[registerSize];

		this.scalar[LOCAL_PARTITION_ORDINAL] = new boolean[localSize];
		this.cachingEnabled[LOCAL_PARTITION_ORDINAL] = new boolean[localSize];
		this.caches[LOCAL_PARTITION_ORDINAL] = new ScalarCache[localSize];

		this.scalar[GLOBAL_PARTITION_ORDINAL] = new boolean[globalSize];
		this.cachingEnabled[GLOBAL_PARTITION_ORDINAL] = new boolean[globalSize];
		this.caches[GLOBAL_PARTITION_ORDINAL] = new ScalarCache[globalSize];

		this.scalar[CONSTANT_PARTITION_ORDINAL] = new boolean[constantSize];
		this.cachingEnabled[CONSTANT_PARTITION_ORDINAL] = new boolean[constantSize];
		this.caches[CONSTANT_PARTITION_ORDINAL] = new ScalarCache[constantSize];

		this.scalar[STACK_PARTITION_ORDINAL] = new boolean[stackSize];
		this.cachingEnabled[STACK_PARTITION_ORDINAL] = new boolean[stackSize];
		this.caches[STACK_PARTITION_ORDINAL] = new ScalarCache[stackSize];

		this.scalar[NONE_PARTITION_ORDINAL] = new boolean[noneSize];
		this.cachingEnabled[NONE_PARTITION_ORDINAL] = new boolean[noneSize];
		this.caches[NONE_PARTITION_ORDINAL] = new ScalarCache[noneSize];

		for (int partitionIndex=0; partitionIndex<PARTITION_LENGTH; partitionIndex++) {
			Arrays.fill(this.scalar[partitionIndex], false);
			Arrays.fill(this.cachingEnabled[partitionIndex], false);
			Arrays.fill(this.caches[partitionIndex], null);
		}

		// NONEパーティションのオペランドは読み書きされないプレースホルダなので、最適化が効くようにキャッシュ済みとマークしておく
		Arrays.fill(this.scalar[NONE_PARTITION_ORDINAL], true);
		Arrays.fill(this.cachingEnabled[NONE_PARTITION_ORDINAL], true);
		Arrays.fill(this.caches[NONE_PARTITION_ORDINAL], new NoneCache());
	}


	// 全アドレスに対するキャッシュを無効化する
	// (キャッシュを使わない最適化レベルが指定された場合用)
	private void disableAllCaches() {
		for (int partitionIndex=0; partitionIndex<PARTITION_LENGTH; partitionIndex++) {
			Arrays.fill(this.cachingEnabled[partitionIndex], false);
			Arrays.fill(this.caches[partitionIndex], null);
		}
	}


	// 定数領域やグローバル領域など、メモリ上にデータが確保済みのものについて、
	// メモリを読みながらスカラかどうか等の性質判定を行い、スカラに対してはキャッシュ確保を行う。
	// （定数値のキャッシュへの書き込みは後でSynchronizerで行う）
	private void detectScalarFromMemory(Memory memory, Memory.Partition partition) {
		int partitionOrdinal = partition.ordinal();
		int partitionSize = memory.getSize(partition);

		for (int address=0; address<partitionSize; address++) {

			DataContainer<?> container = null;
			try {
				container = memory.getDataContainer(partition, address);

			// 存在するはずの定数アドレスにアクセスしているので、この例外が発生する場合は実装の異常
			} catch (VnanoFatalException e) {
				throw new VnanoFatalException(e);
			}

			this.scalar[partitionOrdinal][address] = (container.getArrayRank() == DataContainer.ARRAY_RANK_OF_SCALAR);

			if (!this.scalar[partitionOrdinal][address]) {
				continue;
			}

			switch (container.getDataType()) {
				case INT64 : {
					this.caches[partitionOrdinal][address] = new Int64ScalarCache();
					this.cachingEnabled[partitionOrdinal][address] = true;
					break;
				}
				case FLOAT64 : {
					this.caches[partitionOrdinal][address] = new Float64ScalarCache();
					this.cachingEnabled[partitionOrdinal][address] = true;
					break;
				}
				case BOOL : {
					this.caches[partitionOrdinal][address] = new BoolScalarCache();
					this.cachingEnabled[partitionOrdinal][address] = true;
					break;
				}
				default : {
					break;
				}
			}
		}
	}


	private boolean isCacheableDatatype(DataType dataType) {
		return dataType == DataType.INT64 || dataType == DataType.FLOAT64 || dataType == DataType.BOOL;
	}

	private ScalarCache generateScalarCache(DataType dataType) {
		switch (dataType) {
			case INT64 : {
				return new Int64ScalarCache();
			}
			case FLOAT64 : {
				return new Float64ScalarCache();
			}
			case BOOL : {
				return new BoolScalarCache();
			}
			default : {
				throw new VnanoFatalException("Uncacheable data type: " + dataType);
			}
		}
	}


	// ローカル領域やレジスタ領域など、実行前にはメモリ上にデータが確保されていないものについて、
	// 命令列の中の確保命令を読んでスカラかどうか等の性質判定を行う
	private void detectScalarFromInstructions(Instruction[] instructions, Memory memory, Interconnect interconnect) {


		// !!!!!!!!!!!!!!!!!!!!!!!!!
		// !!!!! 要リファクタ  !!!!!
		// !!!!!!!!!!!!!!!!!!!!!!!!!


		// アドレスに紐づけてキャッシュを持つ(同じデータコンテナに対して同じキャッシュが一意に対応するように)
		// 非キャッシュ演算ユニットはデータコンテナとキャッシュ要素を保持し、同期する

		// 注意：グローバル領域に置かれた外部変数は、
		//       実行対象コード内でALLOCされていない場合もある（外部変数など）ため、
		//       以下と同一の方法ではスカラかどうかの完全判定はできない。
		//       そのため detectScalarFromMemory の方法で判定する

		// 以下、命令列内でデータ確保命令を呼んでいる箇所などをスキャンし、
		// スカラかどうか、キャッシュ可能かどうか等のメタ情報を判定して控える。


		// まずは下ループでスカラである可能性が高いものを抽出してキャッシュ可能性判断＆結果をマークし、後のループで無効化する。
		// (VRILコードの処理フロー次第では、一回のループでは有効な情報を拾えない場合もあるため、何度かループする)

		// 最初に、ALLOC命令とALLOCT命令のオペランドを解析
		for (Instruction instruction: instructions) {
			Memory.Partition[] partitions = instruction.getOperandPartitions();
			int[] addresses = instruction.getOperandAddresses();
			if (partitions[0] != Memory.Partition.LOCAL && partitions[0] != Memory.Partition.REGISTER) {
				continue; // このメソッドが判定対象とするのはローカル領域とレジスタ領域のデータのみ
			}
			if (instruction.getOperationCode() != OperationCode.ALLOC && instruction.getOperationCode() != OperationCode.ALLOCT) {
				continue; // ALLOCとALLOCT以外は解析対象外
			}

			// 1-オペランドのALLOC命令は、0次元なのでスカラ
			if (addresses.length == 1) {

				// このALLOC命令の時点では、スカラであれば暫定的に cacheable と見なしておく。
				// このループはコードを命令順に読んでいってるので、ALLOC後に REFELM している場合などは、
				// 後でその REFELM を読んだ時点で uncacheable に訂正される

				this.scalar[ partitions[0].ordinal() ][ addresses[0] ] = true;
				if (this.isCacheableDatatype(instruction.getDataTypes()[0])) {
					this.cachingEnabled[ partitions[0].ordinal() ][ addresses[0] ] = true;
					this.caches[ partitions[0].ordinal() ][ addresses[0] ] = this.generateScalarCache(instruction.getDataTypes()[0]);
				}
			}
		}

		// 次にALLOCR命令のオペランドを解析
		// ( フローによっては、コードを上から読んでいった際にはオペランド[1]の確保処理が未登場で、
		//   でもそれを使ってオペランド[0]にALLOCRしている場合があり得るはずなので、
		//   上のALLOC/ALLOCTループと同じループ内では判定できない。)
		for (Instruction instruction: instructions) {
			Memory.Partition[] partitions = instruction.getOperandPartitions();
			int[] addresses = instruction.getOperandAddresses();
			if (partitions[0] != Memory.Partition.LOCAL && partitions[0] != Memory.Partition.REGISTER) {
				continue; // このメソッドが判定対象とするのはローカル領域とレジスタ領域のデータのみ
			}
			if (instruction.getOperationCode() != OperationCode.ALLOCR) {
				continue; // ALLOCR以外は解析対象外
			}

			// スカラの場合
			if (this.scalar[ partitions[1].ordinal() ][ addresses[1] ]) {
				this.scalar[ partitions[0].ordinal() ][ addresses[0] ] = true;
				if (this.isCacheableDatatype(instruction.getDataTypes()[0])) {
					this.cachingEnabled[ partitions[0].ordinal() ][ addresses[0] ] = true;
					this.caches[ partitions[0].ordinal() ][ addresses[0] ] = this.generateScalarCache(instruction.getDataTypes()[0]);
				}
			}
		}

		// 次にMOVELM/REFELM命令のオペランドを解析
		for (Instruction instruction: instructions) {
			Memory.Partition[] partitions = instruction.getOperandPartitions();
			int[] addresses = instruction.getOperandAddresses();
			if (partitions[0] != Memory.Partition.LOCAL && partitions[0] != Memory.Partition.REGISTER) {
				continue; // このメソッドが判定対象とするのはローカル領域とレジスタ領域のデータのみ
			}
			if (instruction.getOperationCode() != OperationCode.MOVELM && instruction.getOperationCode() != OperationCode.REFELM) {
				continue; // MOVELMとREFELM以外は解析対象外
			}

			// 現在の仕様では、MOVELM/REFELMで取り出したデータは必ずスカラ
			this.scalar[ partitions[0].ordinal() ][ addresses[0] ] = true;

			// REFELM命令は、ベクトルの要素（スカラ）への参照を第0オペランドのレジスタと同期するため、
			// 第0オペランドはスカラであるが、別の箇所で参照が共有されて書き換えられる可能性があるため、
			// キャッシュ可能ではない
			//（異なるアドレスのレジスタが同一データを参照を保持できるため、アドレスベースのキャッシュでは対応不可）
			if (instruction.getOperationCode() == OperationCode.REFELM) {
				this.cachingEnabled[ partitions[0].ordinal() ][ addresses[0] ] = false;

			// それ以外の場合は MOVELM 命令だが、こちらはキャッシュ可能
			} else {
				if (this.isCacheableDatatype(instruction.getDataTypes()[0])) {
					this.cachingEnabled[ partitions[0].ordinal() ][ addresses[0] ] = true;
					this.caches[ partitions[0].ordinal() ][ addresses[0] ] = this.generateScalarCache(instruction.getDataTypes()[0]);
				}
			}
		}

		// 次にCALLX命令のオペランドを解析
		for (Instruction instruction: instructions) {
			Memory.Partition[] partitions = instruction.getOperandPartitions();
			int[] addresses = instruction.getOperandAddresses();
			if (partitions[0] != Memory.Partition.LOCAL && partitions[0] != Memory.Partition.REGISTER) {
				continue; // このメソッドが判定対象とするのはローカル領域とレジスタ領域のデータのみ
			}
			if (instruction.getOperationCode() != OperationCode.CALLX) {
				continue; // CALLX以外は解析対象外
			}

			// CALLX のオペランド[0]は戻り値格納用で、戻り値の配列サイズは呼び出し側からは分からないため
			// VRILコード内ではALLOCされず、外部関数側で必要に応じてメモリ確保される。
			// そのため、戻り値オペランドがスカラかどうかは、ALLOC箇所を探しても見つからず、関数の宣言情報を調べる必要がある。

			// まずオペランド[1]を読んで外部関数アドレスを取得し、それを元にインターコネクトに問い合わせて外部関数を取得
			DataContainer<?> functionAddrContainer = memory.getDataContainer(partitions[1], addresses[1]);
			int functionAddr = (int)( (long[])functionAddrContainer.getArrayData() )[0];
			AbstractFunction calleeFunction = interconnect.getExternalFunctionTable().getFunctionByIndex(functionAddr);

			// 関数の宣言情報から、戻り値がスカラであるとすぐに分かる場合は、戻り値のアドレスをスカラと判定
			//（isReturnArrayRankArbitrary() が true なら、戻り値の型は実引数の型に依存して変わるけれど、
			//  そういう場合はそもそも最適化があまり効かないので調べず、isReturnArrayRankArbitrary() が false の場合のみ調べる）
			if (!calleeFunction.isReturnArrayRankArbitrary()
					&& calleeFunction.getReturnArrayRank(new String[0], new int[0]) == DataContainer.ARRAY_RANK_OF_SCALAR) {

				this.scalar[ partitions[0].ordinal() ][ addresses[0] ] = true;
				if (this.isCacheableDatatype(instruction.getDataTypes()[0])) {
					this.cachingEnabled[ partitions[0].ordinal() ][ addresses[0] ] = true;
					this.caches[ partitions[0].ordinal() ][ addresses[0] ] = this.generateScalarCache(instruction.getDataTypes()[0]);
				}
				// ※ 戻り値は、常に専用に確保されたレジスタで受け取るので、引数の参照渡しとは異なり、キャッシュしても問題ないはず
				//    (ただし最適化後は戻り値を変数で直接受け取るように再配置されたりするため、最適化前に調べる事が前提)
				//    -> 将来的なコード生成の変化に対して安定にするには、レジスタ書き込み数カウントとかを先に行ってここで確認した方がいいと思う。
				//       また後々で要実装
			}
		}



		// キャッシュ可能スカラと判定されたものについて、やっぱり不可能と判断できるものを抽出して無効化していく
		for (Instruction instruction: instructions) {
			Memory.Partition[] partitions = instruction.getOperandPartitions();
			int[] addresses = instruction.getOperandAddresses();

			OperationCode opcode = instruction.getOperationCode();
			switch (opcode) {

				case REF :
				case REFPOP : {

					// ELEMと同様、REF系の対象も他の箇所のデータと参照を共有するようになるため、
					// アドレスベースのキャッシュでは対応不可（アドレス-データが一対一対応にならない）
					this.caches[ partitions[0].ordinal() ][ addresses[0] ] = null;
					this.cachingEnabled[ partitions[0].ordinal() ][ addresses[0] ] = false;
					break;
				}

				// 関数コール
				case CALL :
				case CALLX : {

					// 参照渡しの引数に渡している実引数も、REF/REFPOPと同様の理由によりキャッシュ対応不可能
					// ただし外部関数に渡してるものについては、const 宣言されてる引数ならキャッシュ可能とする
					// (内部関数はconstかどうかの情報がVRIL上では落ちてしまってるし、将来的には宣言によらず、書き換えてなければ可能にするかもだし保留)
					// -> 外部関数の参照渡し引数につては const でなくてもキャッシュして問題ない気もするけれど後々で要検討

					DataContainer<?> functionAddrContainer = memory.getDataContainer(partitions[1], addresses[1]);
					int functionAddr = (int)( (long[])functionAddrContainer.getArrayData() )[0];
					int argN = partitions.length - 2; // 戻り値と関数アドレスを除いた（-2）オペランド数が引数の個数
					boolean[] areParamRefs = null;
					boolean[] areParamConsts = null;
					boolean isParamCountArbitrary = false;

					// 内部関数コール ... コード内で引数をスタックから MOVPOP or REFPOP する箇所を読んで参照の有無を判断する
					// -> どうにかして関数アドレスからAbstractFunctionを引っ張ってこられればもっと簡単に分かるし、const かも分かるので、後々で要検討
					if (opcode == OperationCode.CALL) {
						areParamRefs = this.internalFunctionAddrParamRefsMap.get(functionAddr);
						areParamConsts = new boolean[argN];
						isParamCountArbitrary = false; // Vnanoのスクリプト内関数では、任意個数の引数は未サポート
						Arrays.fill(areParamConsts, false); // 内部関数は先述のコメント参照な理由により const かどうかは見ない

					// 外部関数コール ... Interconnect から関数情報を取得して、参照渡し宣言されてるか、また定数かどうか調べる
					} else if (opcode == OperationCode.CALLX) {
						AbstractFunction function = interconnect.getExternalFunctionTable().getFunctionByIndex(functionAddr);
						areParamRefs = function.getParameterReferencenesses();
						areParamConsts = function.getParameterConstantnesses();
						isParamCountArbitrary = function.isParameterCountArbitrary();

					} else {
						// 上の switch-case からして、ここで上記以外の命令はあり得ない
						throw new VnanoFatalException("Unexpected operaton code: " + opcode);
					}

					// const でない参照渡しを行っている実引数をキャッシュ不可能とマークする
					for (int argIndex=0; argIndex<argN; argIndex++) {

						// 引数が任意個数に設定されている場合は、宣言上の仮引数は1個のみなので [0] 番引数の情報を取得し、
						// そうでなければ普通に [argIndex] 番引数の情報を取得
						boolean isParamRef = isParamCountArbitrary ? areParamRefs[0] : areParamRefs[argIndex];
						boolean isParamConst = isParamCountArbitrary ? areParamConsts[0] : areParamConsts[argIndex];

						// const でない参照渡しの場合
						if (isParamRef && !isParamConst) {
							int operandIndex = argIndex + 2;
							this.caches[ partitions[operandIndex].ordinal() ][ addresses[operandIndex] ] = null;
							this.cachingEnabled[ partitions[operandIndex].ordinal() ][ addresses[operandIndex] ] = false;
						}
					}
					break;
				}

				default : {
					break;
				}
			}
		}
	}


	// 全命令列をスキャンして、その中の内部関数について、最適化に役立ちそうな情報をそこそこ解析する
	private void analyzeInternalFunctionInformation(Instruction[] instructions, Memory memory) {

		// 命令を辿る
		for (Instruction instruction: instructions) {
			Memory.Partition[] partitions = instruction.getOperandPartitions();
			int[] addresses = instruction.getOperandAddresses();

			// オペレーショコードごとに分岐
			switch(instruction.getOperationCode()) {

				// 内部関数呼び出し
				case CALL : {

					DataContainer<?> functionAddrContainer = memory.getDataContainer(partitions[1], addresses[1]);
					int functionAddr = (int)( (long[])functionAddrContainer.getArrayData() )[0]; // 内部関数の命令アドレス
					int numberOfArgs = partitions.length - 2; // 戻り値と関数アドレスを除いた（-2）オペランド数が引数の個数

					// 既に解析済みでなければ解析
					if (!this.internalFunctionAddrParamRefsMap.containsKey(functionAddr)) {

						// 引数が参照渡しかどうかを解析
						List<Boolean> areParamRefList = getInternalFunctionParameterReferencenesses(
							instructions, functionAddr, numberOfArgs
						);
						// 結果をリストから配列に変換（上層で外部関数と処理を統一するため）して解析済みマップに登録
						boolean[] areParamRefs = new boolean[areParamRefList.size()];
						for (int i=0; i<areParamRefList.size(); i++) {
							areParamRefs[i] = areParamRefList.get(i);
						}
						this.internalFunctionAddrParamRefsMap.put(functionAddr, areParamRefs);
					}
					break;
				}
				default : {
					break;
				}
			}
		}
	}

	// 内部関数の命令列をトレースし、各引数が参照渡しかどうかを解析して、リストに格納して返します
	private List<Boolean> getInternalFunctionParameterReferencenesses(
			Instruction[] instructions, int functionAddress, int numberOfArgs) {

		// 内部関数テーブルを引っ張ってきてFunctionオブジェクト取得して引数情報確認する方がたぶん素直、後で要検討？
		// > 内部関数情報はコンパイラ内で閉じさせてるので、外に出すとそれに各部が依存して複雑化してしまう可能性があるかも。
		//   Acceleratorは最適化のための複雑さを閉じ込めておく場所でもあるので、その要求をあまりモジュール外に波及させたくない。
		// > 引数POPは関数先頭に並んでるので、こっちの方法でも関数アドレスに飛んで命令数個見るだけで済むし。
		// 現状は保留、将来的にもし内部関数テーブルを（最適化要求以外で）コンパイラ外に出した際にこのメソッドは要再検討

		List<Boolean> referencenessList = new ArrayList<Boolean>();
		if (numberOfArgs == 0) {
			return referencenessList;
		}

		int instructionLength = instructions.length;
		for (int instAddr=functionAddress; instAddr<instructionLength; instAddr++) {
			switch(instructions[instAddr].getOperationCode()) {
				case POP :
				case MOVPOP : {
					referencenessList.add(Boolean.FALSE);
					if (referencenessList.size() == numberOfArgs) {
						return referencenessList;
					}
					break;
				}
				case REFPOP : {
					referencenessList.add(Boolean.TRUE);
					if (referencenessList.size() == numberOfArgs) {
						return referencenessList;
					}
					break;
				}
				case ENDFUN : {
					// 実引数の個数POPされないのはそもそも callee と caller が整合してないのでリンクがおかしい
					//（冒頭ですぐ全部POPされるはず）
					throw new VnanoFatalException(
						"Number of parameters is deficient."
					);
				}
				default : {
					break;
				}
			}
		}

		// 関数始点から辿ってENDFUN命令に達しないのはアセンブラがおかしいか、アセンブリコード手書き編集でミスってるとか
		throw new VnanoFatalException(
			"No ENDFUN instruction found for the function beginning from the instruction address: " + functionAddress
		);
	}


	private void initializeCacheSynchronizers(Memory memory) {

		DataContainer<?>[] registerContainers = memory.getDataContainers(Memory.Partition.REGISTER);
		DataContainer<?>[] localContainers = memory.getDataContainers(Memory.Partition.LOCAL);
		DataContainer<?>[] globalContainers = memory.getDataContainers(Memory.Partition.GLOBAL);
		DataContainer<?>[] constantContainers = memory.getDataContainers(Memory.Partition.CONSTANT);

		this.synchronizers = new CacheSynchronizer[PARTITION_LENGTH];

		this.synchronizers[REGISTER_PARTITION_ORDINAL] = new GeneralScalarCacheSynchronizer(
				registerContainers, this.getCaches(Memory.Partition.REGISTER), this.getCachedFlags(Memory.Partition.REGISTER)
		);
		this.synchronizers[LOCAL_PARTITION_ORDINAL] = new GeneralScalarCacheSynchronizer(
				localContainers, this.getCaches(Memory.Partition.LOCAL), this.getCachedFlags(Memory.Partition.LOCAL)
		);
		this.synchronizers[GLOBAL_PARTITION_ORDINAL] = new GeneralScalarCacheSynchronizer(
				globalContainers, this.getCaches(Memory.Partition.GLOBAL), this.getCachedFlags(Memory.Partition.GLOBAL)
		);
		this.synchronizers[CONSTANT_PARTITION_ORDINAL] = new GeneralScalarCacheSynchronizer(
				constantContainers, this.getCaches(Memory.Partition.CONSTANT), this.getCachedFlags(Memory.Partition.CONSTANT)
		);
	}


}
