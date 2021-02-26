/*
 * Copyright(C) 2017-2021 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.processor.Instruction;

public class AcceleratorSchedulingUnit {

	// List 内の null 要素を removeAll する際に渡す (removeAllの引数は Collection インスタンスであるべきなので素の null は渡せない)
	private static final List<Object> LIST_OF_NULL = Arrays.asList((Object)null);

	private List<AcceleratorInstruction> acceleratorInstructionList;
	private Map<Integer,Integer> addressReorderingMap;
	private Map<Integer,Integer> expandedAddressReorderingMap;

	public AcceleratorInstruction[] schedule(
			AcceleratorInstruction[] instructions, Memory memory, AcceleratorDataManagementUnit dataManager) {

		this.acceleratorInstructionList = new ArrayList<AcceleratorInstruction>();
		for (AcceleratorInstruction instruction: instructions) {
			this.acceleratorInstructionList.add( instruction.clone() );
		}


		// 連続する算術スカラ演算命令2個を融合させて1個の拡張命令に置き換える
		this.fuseArithmeticInstructions( // Float64 Cached-Scalar Arithmetic
				AcceleratorExecutionType.F64CS_ARITHMETIC, AcceleratorExecutionType.F64CS_DUAL_ARITHMETIC
		);
		this.fuseArithmeticInstructions( // Int64 Cached-Scalar Arithmetic
				AcceleratorExecutionType.I64CS_ARITHMETIC, AcceleratorExecutionType.I64CS_DUAL_ARITHMETIC
		);

		// 連続する転送命令を融合させて1個の拡張命令に置き換える
		this.fuseTransferInstructions();

		// 連続する比較命令と分岐命令を融合させて1個の拡張命令に置き換える（for文のループ継続判定処理で存在）
		this.fuseComparisonAndBranchInstructions();

		// 最新の（再配列後の）命令アドレスを設定し、新旧の対応を保持するマップを更新
		// -> このあたりの処理は AcceleratorOptimizationUnit と重複しているので、きりのいい時になんとかしたい
		this.updateReorderedAddresses();
		this.generateAddressReorderingMap();

		// 分岐系命令の飛び先アドレスを補正したものを求めて設定
		// -> ここも AcceleratorOptimizationUnit と重複しているので、きりのいい時になんとかしたい
		this.resolveReorderedLabelAddress(memory);

		// 分岐系命令の飛び先にあるLABEL命令（何もしない）は、実際には演算ユニットに割り当てなくても問題ないので、削除して命令列を詰める
		// (それらのLABELは、他の命令並べ替えや融合/削除において、分岐の着地点が移動してしまわないように置かれているものなので、
		//  上記のような作業が全て終わる前に削除してはならない。削除は本当に最後の最後、実行命令列を確定させる直前に。
		//  なお、分岐系命令の飛び先アドレスは下記メソッド内で再補正されるので、別途補正は必要ない。)
		this.removeLabelInstructions();
		this.updateReorderedAddresses();

		return this.acceleratorInstructionList.toArray(new AcceleratorInstruction[0]);
	}





	// 全命令に対して再配置済み命令アドレスを書き込む
	private void updateReorderedAddresses() {
		int instructionLength = acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			acceleratorInstructionList.get(instructionIndex).setReorderedAddress(instructionIndex);
		}
	}


	// 全命令アドレスの再配置前→再配置後の対応を格納したアドレス変換マップを生成
	private void generateAddressReorderingMap() {

		// addressReorderingMap は、再配置前のアドレス unreorderedAddress をキーとして、
		// 再配置後のアドレス reorderedAddress を返すマップで、
		// JMP/JMPN/CALLの着地点ラベル（由来のLABEL命令）の位置を補正するために使用される。
		// 最適化で命令1個だった所が複数命令になっている箇所がある場合 (CALLの引数直接MOV化とか) など、
		// 複数命令が同じ unreorderedAddress を持っていると、addressReorderingMap はそれらの最後の命令の reorderedAddress を返す。
		//
		// なお、インライン展開された関数内の命令も、複数箇所で同じ unreorderedAddress を持っているため、
		// そのまま素直に addressReorderingMap でアドレス変換すると、展開後の関数コード内の分岐命令の飛び先が同一地点に収束してしまう。
		// そのためインライン展開されたコードに対しては、展開後のアドレス（こちらは重複しないはず）をキーとする
		// expandedAddressReorderingMap を用意し、アドレス変換時にそちらを用いるようにする。
		//
		// > もっとキーやタイミングを上手い形に整理すればマップは1個で済ませられるはずなので、きりのいい時に要検討
		//   > というか再配置周りはマップ類を直接使うよりもそういう役割のクラスに包んでその中で管理した方がいいかも
		//     今後の別の最適化とか次第では単純な形では対応し切れない場合もありそうだし

		this.addressReorderingMap = new HashMap<Integer,Integer>();
		this.expandedAddressReorderingMap = new HashMap<Integer,Integer>();
		int instructionLength = acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			AcceleratorInstruction instruction = this.acceleratorInstructionList.get(instructionIndex);
			if (instruction.isExpanded()) {
				this.expandedAddressReorderingMap.put(instruction.getExpandedAddress(), instruction.getReorderedAddress());
			} else {
				this.addressReorderingMap.put(instruction.getUnreorderedAddress(), instruction.getReorderedAddress());
			}
		}
	}


	// JMP命令など、ラベルオペランドを持つ命令は、ラベルの命令アドレスが命令再配置によって変わるため、再配置後のアドレス情報を追加
	private void resolveReorderedLabelAddress(Memory memory) {
		int instructionLength = this.acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			AcceleratorInstruction instruction = this.acceleratorInstructionList.get(instructionIndex);
			OperationCode opcode = instruction.getOperationCode();
			OperationCode[] fusedOpcodes = instruction.isFused() ? instruction.getFusedOperationCodes() : null;

			// 分岐系命令かどうかを確認して控える。
			// JMP & JMPN & CALL は静的に設定されたラベルに飛ぶ。
			// RET は動的にスタックから取ったアドレスに飛ぶが、IFCU制御用に所属関数のアドレスも持ってる（普通にオペランド[1]がそう）
			boolean isBranchOperation = opcode == OperationCode.JMP || opcode == OperationCode.JMPN
					|| opcode == OperationCode.CALL || opcode == OperationCode.RET;

			// 比較演算などと融合された分岐系命令かどうかを確認して控える（融合対象になり得るのは JMP と JMPN のみ）。
			boolean isFusedBranchOperation = instruction.isFused()
					&& (fusedOpcodes[1] == OperationCode.JMP || fusedOpcodes[1] == OperationCode.JMPN);

			// 分岐系命令の場合（融合されている場合を含む）
			if (isBranchOperation || isFusedBranchOperation) {

				// 分岐先が、インライン展開で生成された命令列の中にある場合
				// (unreorderedLabelAddress は複数展開時に重複が生じてキーに使えないため、展開直後の一意なラベルアドレスをキーとするマップで変換)
				if (instruction.isLabelAddressExpanded()) {
					int expandedLabelAddress = instruction.getExpandedLabelAddress();
					int reorderedLabelAddress = this.expandedAddressReorderingMap.get(expandedLabelAddress);
					instruction.setReorderedLabelAddress(reorderedLabelAddress);

				// それ以外の通常の場合
				// (unreordered address をキーとするマップで変換する)
				} else {

					// ラベルの命令アドレスの値を格納するオペランドのデータコンテナをメモリから取得し、以下の変数に控える
					DataContainer<?> addressContiner = null;

					// 普通の命令では必ずオペランド[1]にあるよう統一されている
					if (opcode != OperationCode.EX) {
						addressContiner = memory.getDataContainer(
							instruction.getOperandPartitions()[1], instruction.getOperandAddresses()[1]
						);

					// 融合された命令のオペランド列は、各命令のオペランド列を単純に並べたものなので、
					// [3]以降が分岐命令のオペランドであり、従って[4]がラベルの命令アドレス
					} else {
						addressContiner = memory.getDataContainer(
							instruction.getOperandPartitions()[4], instruction.getOperandAddresses()[4]
						);
					}

					// データコンテナから飛び先ラベル（アセンブル後はLABEL命令になっている）の命令アドレスの値を読む
					int labelAddress = -1;
					Object addressData = addressContiner.getArrayData();
					if (addressData instanceof long[]) {
						labelAddress = (int)( ((long[])addressData)[ addressContiner.getArrayOffset()] );
					} else {
						throw new VnanoFatalException("Non-integer instruction address (label) operand detected.");
					}

					// 上で読んだラベル（由来のLABEL命令）アドレスの、再配置後の位置の命令アドレスを取得し、命令に持たせる
					int reorderedLabelAddress = this.addressReorderingMap.get(labelAddress);
					instruction.setReorderedLabelAddress(reorderedLabelAddress);
				}
			}
		}
	}



	// 連続する2つの演算命令を融合させて1つの拡張命令にする
	private void fuseArithmeticInstructions(
			AcceleratorExecutionType fromAccelerationType, AcceleratorExecutionType toAccelerationType) {

		int instructionLength = this.acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength-1; instructionIndex++) { // 同時に2個読む可能性があるので -1 まで

			AcceleratorInstruction currentInstruction = this.acceleratorInstructionList.get(instructionIndex);
			AcceleratorInstruction nextInstruction = this.acceleratorInstructionList.get(instructionIndex+1);
			AcceleratorExecutionType currentAccelType = currentInstruction.getAccelerationType();
			AcceleratorExecutionType nextAccelType = nextInstruction.getAccelerationType();

			// 対象命令と次の命令が指定された演算タイプでなければ、その時点でスキップ
			if (currentAccelType != fromAccelerationType
					|| nextAccelType != fromAccelerationType) {

				continue;
			}

			// DualArithmetic系演算ユニットでサポートしていないオペランドの場合はスキップ (例えば符号反転など)
			if (fromAccelerationType == AcceleratorExecutionType.F64CS_ARITHMETIC) {
				if( !Float64CachedScalarDualArithmeticUnit.AVAILABLE_OPERAND_SET.contains(currentInstruction.getOperationCode())
				    ||
				    !Float64CachedScalarDualArithmeticUnit.AVAILABLE_OPERAND_SET.contains(nextInstruction.getOperationCode()) ) {
					continue;
				}
			}
			if (fromAccelerationType == AcceleratorExecutionType.I64CS_ARITHMETIC) {
				if( !Int64CachedScalarDualArithmeticUnit.AVAILABLE_OPERAND_SET.contains(currentInstruction.getOperationCode())
				    ||
				    !Int64CachedScalarDualArithmeticUnit.AVAILABLE_OPERAND_SET.contains(nextInstruction.getOperationCode()) ) {
					continue;
				}
			}

			// 対象命令のオペランドを全て取得
			Memory.Partition[] currentOperandPartitions = currentInstruction.getOperandPartitions();
			int[] currentOperandAddresses = currentInstruction.getOperandAddresses();

			// 次の命令のオペランドを全て取得
			int nextOperandLength = nextInstruction.getOperandLength();
			Memory.Partition[] nextOperandPartitions = nextInstruction.getOperandPartitions();
			int[] nextOperandAddresses = nextInstruction.getOperandAddresses();

			// 次の命令の入力オペランド（1番以降）の中から、対象命令の出力オペランド（0番）と一致するものを探す
			int sameInputIndex = -1;
			int sameInputCount = 0;
			for (int operandIndex=1; operandIndex<nextOperandLength; operandIndex++) {
				if (nextOperandPartitions[operandIndex] == currentOperandPartitions[0]
						&& nextOperandAddresses[operandIndex] == currentOperandAddresses[0]) {

					sameInputIndex = operandIndex;
					sameInputCount++;
				}
			}

			// 対象命令の出力オペランドと次命令の入力オペランドが、全く一致しなかった場合や、複数一致した場合はスキップ
			if (sameInputCount != 1) {
				continue;
			}


			// ここまで到達するのは、対象命令と次命令がキャッシュ可能な算術スカラ演算であり、かつ、
			// 対象命令の出力オペランドを、次命令の入力オペランドに一回だけ使っている場合なので、
			// それら2命令を1つに融合した拡張命令に変換する


			// 対象命令と次の命令を融合した拡張命令を生成
			AcceleratorInstruction fusedInstruction = currentInstruction.fuse(
				nextInstruction, toAccelerationType
			);
			fusedInstruction.setFusedInputOperandIndices(new int[]{ sameInputIndex });

			// リスト内の対象命令を融合拡張命令で置き換える
			this.acceleratorInstructionList.set(instructionIndex, fusedInstruction);

			// 次の命令は既に融合したので、リストにnullを置く（後処理で効率的に削除して詰める）
			this.acceleratorInstructionList.set(instructionIndex + 1, null);

			// 2命令分処理したので、カウンタを1つ余計に進める
			instructionIndex++;
		}

		// リスト内で空いた要素（上でnullを置いている）を削除して詰める
		this.acceleratorInstructionList.removeAll(LIST_OF_NULL);
	}


	// 連続する転送命令を融合させて1つの拡張命令にする
	private void fuseTransferInstructions() {

		// 元の命令列 ( this.acceleratorInstructionList ) を読みながら、必要に応じて拡張命令への置き換えを行いつつ、
		// このリストに積んでいき、最後に this.acceleratorInstructionList をこれで置き換える
		List<AcceleratorInstruction> resultInstructionList = new ArrayList<AcceleratorInstruction>();

		// 元の命令列 ( this.acceleratorInstructionList ) を読みながら、融合対象になり得る転送命令を一時的に溜めておくバッファ
		// (コード内の連続転送領域の終わりや、溜めている数が一括転送のMAX個数になったタイミングで、一括転送する拡張命令を生成する)
		List<AcceleratorInstruction> transferInstructionBuffer = new ArrayList<AcceleratorInstruction>();

		// データ型の異なるデータ転送は融合できないので、最後にバッファに詰めた転送データ型をこれに控えて、変化点の検出などに使う
		DataType lastTransferDataType = null;

		// 一括転送ユニットで対応しているオペランドセット、および一括演算可能な最大命令数を取得
		Set<OperationCode> float64FusibleOpcodeSet = Float64CachedScalarMultipleTransferUnit.AVAILABLE_OPERAND_SET;
		Set<OperationCode> int64FusibleOpcodeSet = Int64CachedScalarMultipleTransferUnit.AVAILABLE_OPERAND_SET;
		Set<OperationCode> boolFusibleOpcodeSet = BoolCachedScalarMultipleTransferUnit.AVAILABLE_OPERAND_SET;
		int float64MaxFusibleCount = Float64CachedScalarMultipleTransferUnit.MAX_AVAILABLE_TRANSFER_COUNT;
		int int64MaxFusibleCount = Int64CachedScalarMultipleTransferUnit.MAX_AVAILABLE_TRANSFER_COUNT;
		int boolMaxFusibleCount = BoolCachedScalarMultipleTransferUnit.MAX_AVAILABLE_TRANSFER_COUNT;

		for (AcceleratorInstruction instruction: this.acceleratorInstructionList) {
			AcceleratorExecutionType accelType = instruction.getAccelerationType();
			OperationCode opcode = instruction.getOperationCode();

			// 融合可能な命令かどうか（一括処理できる演算ユニットがあるかどうか）を判断
			boolean isFloat64Fusible = (accelType == AcceleratorExecutionType.F64CS_TRANSFER) && float64FusibleOpcodeSet.contains(opcode);
			boolean isInt64Fusible = (accelType == AcceleratorExecutionType.I64CS_TRANSFER) && int64FusibleOpcodeSet.contains(opcode);
			boolean isBoolFusible = (accelType == AcceleratorExecutionType.BCS_TRANSFER) && boolFusibleOpcodeSet.contains(opcode);
			boolean isFusible = isFloat64Fusible || isInt64Fusible || isBoolFusible;

			// バッファしている命令数が、一括演算可能な上限数に達しているかを判断
			int bufferedCount = transferInstructionBuffer.size();
			boolean isFloat64FusibleCapacifyFull = lastTransferDataType == DataType.FLOAT64 && bufferedCount == float64MaxFusibleCount;
			boolean isInt64FusibleCapacifyFull = lastTransferDataType == DataType.INT64 && (bufferedCount == int64MaxFusibleCount);
			boolean isBoolFusibleCapacityFull = lastTransferDataType == DataType.BOOL && (bufferedCount == boolMaxFusibleCount);
			boolean isFusibleCapacityFull = isFloat64FusibleCapacifyFull || isInt64FusibleCapacifyFull || isBoolFusibleCapacityFull;

			// 以下の条件が満たされた瞬間に、まずバッファ内の転送命令列を融合＆拡張命令に変換して出力し、一旦バッファを空にしておく
			// ・一括転送対象ではない命令が来た場合
			// ・一括転送対象の命令が来た場合でも、バッファ内の命令とデータ型が異なる場合
			// ・バッファ内の命令数が、一括処理可能な上限数に達した場合
			if (!isFusible || instruction.getDataTypes()[0] != lastTransferDataType || isFusibleCapacityFull) {
				if (transferInstructionBuffer.size() != 0) {
					resultInstructionList.add( this.toFusedTransferInstruction(transferInstructionBuffer) );
					transferInstructionBuffer.clear();
				}
			}

			// 一括転送対象にできる命令なら、変換結果の命令列には積まずにバッファに溜める
			if (isFusible) {
				transferInstructionBuffer.add(instruction);
				lastTransferDataType = instruction.getDataTypes()[0];

			// それ以外の命令はそのまま変換結果の命令列に積む
			} else {
				resultInstructionList.add(instruction);
			}
		}

		// バッファ内に最後まで溜まったまま出力タイミングが来なかった転送命令列を、拡張命令に変換して出力
		if (transferInstructionBuffer.size() != 0) {
			resultInstructionList.add( this.toFusedTransferInstruction(transferInstructionBuffer) );
			transferInstructionBuffer.clear();
		}

		this.acceleratorInstructionList = resultInstructionList;
	}

	// 複数の転送命令を融合した単一の拡張命令にして返す
	//（処理可能な演算ユニットが存在する事や、データ型が揃っている事などは、呼び出し側で確認済みである事を前提とする）
	private AcceleratorInstruction toFusedTransferInstruction(List<AcceleratorInstruction> transferInstructionList) {
		int transferCount = transferInstructionList.size(); // 連続する転送命令の個数

		// 0個の場合は呼び出し側がおかしい
		if (transferCount == 0) {
			throw new VnanoFatalException( "The passed transfer instruction list for fusing is empty." );
		}

		// 1個だけの場合はそのまま返す
		if (transferCount == 1) {
			return transferInstructionList.get(0);
		}

		// 以下、複数の転送命令を融合させた拡張命令を生成する
		// -> このあたりは AcceleratorInstruction の fuse メソッドを拡張してそちらを使うようにした方がいいかもしれない。後々で要検討

		// 元の転送命令の dest と src を交互に並べた、拡張命令用のオペランド配列を用意
		Memory.Partition[] fusedOperandParts = new Memory.Partition[ transferCount * 2 ]; // dest & src のペアが transferCount 個あるので *2
		int[] fusedOperandAddrs = new int[ transferCount * 2 ];
		int fusedOperandPointer = 0;
		for (AcceleratorInstruction instruction: transferInstructionList) {
			System.arraycopy(instruction.getOperandPartitions(), 0, fusedOperandParts, fusedOperandPointer, 2);
			System.arraycopy(instruction.getOperandAddresses(),  0, fusedOperandAddrs, fusedOperandPointer, 2);
			fusedOperandPointer += 2;
		}

		// 融合する命令のオペレーションコードを配列にまとめる（拡張命令に情報として持たせる必要がある）
		OperationCode[] fusedOpcodes = new OperationCode[transferCount];
		for (int instructionIndex=0; instructionIndex<transferCount; instructionIndex++) {
			fusedOpcodes[instructionIndex] = transferInstructionList.get(instructionIndex).getOperationCode();
		}

		// 先頭の転送命令から、データ型やメタ情報などを流用する
		AcceleratorInstruction firstTransferInstruction = transferInstructionList.get(0);

		// 拡張命令を生成し、それを Accelerator 用の継承型に変換
		Instruction fusedInstruction = new Instruction(
			OperationCode.EX, firstTransferInstruction.getDataTypes(),
			fusedOperandParts, fusedOperandAddrs,
			firstTransferInstruction.getMetaPartition(), firstTransferInstruction.getMetaAddress()
		);

		// 必要な情報を登録
		AcceleratorInstruction fusedAccelInstruction = new AcceleratorInstruction(fusedInstruction);
		fusedAccelInstruction.setUnreorderedAddress(firstTransferInstruction.getUnreorderedAddress());
		fusedAccelInstruction.setFusedOperationCodes(fusedOpcodes);
		switch(firstTransferInstruction.getDataTypes()[0]) {
			case INT64:   fusedAccelInstruction.setAccelerationType(AcceleratorExecutionType.I64CS_MULTIPLE_TRANSFER); break;
			case FLOAT64: fusedAccelInstruction.setAccelerationType(AcceleratorExecutionType.F64CS_MULTIPLE_TRANSFER); break;
			case BOOL:    fusedAccelInstruction.setAccelerationType(AcceleratorExecutionType.BCS_MULTIPLE_TRANSFER); break;
			default: throw new VnanoFatalException("Infusible data type detected: " + firstTransferInstruction.getDataTypes()[0]);
		}

		return fusedAccelInstruction;
	}


	// 連続する比較命令と分岐命令を融合させて1個の拡張命令に置き換える（for文のループ継続判定処理で存在）
	private void fuseComparisonAndBranchInstructions() {

		// 命令列の中の命令を辿っていくループ
		int instructionLength = this.acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength-1; instructionIndex++) { // 2個読み進む場合があるので -1 まで
			AcceleratorInstruction currentInstruction = this.acceleratorInstructionList.get(instructionIndex);
			AcceleratorInstruction nextInstruction = this.acceleratorInstructionList.get(instructionIndex + 1);

			// currentInstruction がキャッシュ可能なスカラ演算でない場合はスキップ
			if (currentInstruction.getAccelerationType() != AcceleratorExecutionType.F64CS_COMPARISON
					&& currentInstruction.getAccelerationType() != AcceleratorExecutionType.I64CS_COMPARISON) {
				continue;
			}

			// nextInstruction が「キャッシュ可能なスカラを条件とする分岐命令」でない場合はスキップ
			if (nextInstruction.getAccelerationType() != AcceleratorExecutionType.BCS_BRANCH) {
				continue;
			}

			// オペランドのアドレスを取得
			Memory.Partition[] currentParts = currentInstruction.getOperandPartitions();
			int[] currentAddrs = currentInstruction.getOperandAddresses();
			Memory.Partition[] nextParts = nextInstruction.getOperandPartitions();
			int[] nextAddrs = nextInstruction.getOperandAddresses();

			// currentInstruction の演算結果が、nextInstruction の分岐条件オペランドになっていない場合はスキップ
			if (currentParts[0] != nextParts[2] || currentAddrs[0] != nextAddrs[2]) {
				continue;
			}

			// ここまで到達するのは、キャッシュ可能なスカラの比較命令と、その結果に応じて分岐する命令が連続している場合なので、
			// それらを融合させて1個の拡張命令に変換する
			AcceleratorInstruction fusedInstruction = currentInstruction.fuse(
				nextInstruction, AcceleratorExecutionType.BCS_BRANCH
			);

			// 前の命令の演算結果が、次の命令において何番目のオペランドになっているかを設定
			fusedInstruction.setFusedInputOperandIndices(new int[]{ 2 });

			// 分岐命令の飛び先ラベルのリオーダリング用情報を設定
			fusedInstruction.setReorderedLabelAddress(nextInstruction.getReorderedLabelAddress());
			fusedInstruction.setExpandedLabelAddress(nextInstruction.getExpandedLabelAddress());

			// リスト内の対象命令を融合拡張命令で置き換える
			this.acceleratorInstructionList.set(instructionIndex, fusedInstruction);

			// 次の命令は既に融合したので、リストにnullを置く（後で削除して詰める）
			this.acceleratorInstructionList.set(instructionIndex + 1, null);

			// 2命令分処理したので、カウンタを1つ余計に進める
			instructionIndex++;
		}

		// 命令列内で、融合で空いた箇所に置いてある null を詰める
		this.acceleratorInstructionList.removeAll(LIST_OF_NULL);
	}


	// 分岐の着地点等に置かれているLABEL命令（何もしない）は、実際には演算ユニットに割り当てなくても問題ないので、削除して命令列を詰める。
	// その際、分岐系命令の着地点の補正も行うが、事前に resolveReorderedLabelAddress() で他の影響の補正を済ませておく必要がある。
	// なお、NOP命令もLABEL命令同様に何もしないが、そちらは最適化で削除されないという仕様になっているので削除してはならない。
	// (NOPは意図的にVMを特定サイクル空回しさせたいような場合に用いられる。)
	private void removeLabelInstructions() {
		int instructionLength = this.acceleratorInstructionList.size();

		// LABEL命令を削除し、そのアドレスと、LABEL削除後の着地先アドレスとの対応付けを行うマップを作製
		// (更新用命令リスト updatedInstructionList に this.acceleratorInstructionList 内の命令を詰めていき、
		//  併せて brancDestAddrUpdateMap に、削除したNOPの命令アドレスと、そこへ飛ぶ分岐命令の新しい飛び先アドレスを格納していく)
		List<AcceleratorInstruction> updatedInstructionList = new ArrayList<AcceleratorInstruction>();
		Map<Integer, Integer> brancDestAddrUpdateMap = new HashMap<Integer, Integer>();
		for (int instructionAddr=0; instructionAddr<instructionLength; instructionAddr++) {
			AcceleratorInstruction instruction = this.acceleratorInstructionList.get(instructionAddr);

			// LABEL命令の場合、着地点のアドレス補正情報を登録し、更新用命令列には積まない
			if(instruction.getOperationCode() == OperationCode.LABEL) {

				// このLABELがあった場所への分岐は、LABEL削除済み命令列において、
				// 削除したLABELの後に最初に出現する非LABEL命令に着地するようにマップに登録
				int updatedDestAddr = updatedInstructionList.size(); // size は次に add される非LABEL命令のインデックスに一致する
				brancDestAddrUpdateMap.put(instructionAddr, updatedDestAddr);

				// ※ 命令列終端には必ずEND命令つまりLABELではない命令があるので、updatedDestAddr が命令列範囲内からあふれる事は無い

			// それ以外の命令は、削除せずそのまま更新用命令列に積む
			} else {
				updatedInstructionList.add(instruction);
			}
		}

		// 削除したLABELに着地していた分岐系命令の飛び先アドレスを更新
		for (AcceleratorInstruction instruction: this.acceleratorInstructionList) {
			OperationCode opcode = instruction.getOperationCode();
			OperationCode[] fusedOpcodes = instruction.isFused() ? instruction.getFusedOperationCodes() : null;

			// 分岐系命令かどうかを確認して控える。
			// JMP & JMPN & CALL は静的に設定されたラベルに飛ぶ。
			// RET は動的にスタックから取ったアドレスに飛ぶが、IFCU制御用に所属関数のアドレスも持ってる。
			boolean isBranchOperation = opcode == OperationCode.JMP || opcode == OperationCode.JMPN
					|| opcode == OperationCode.CALL || opcode == OperationCode.RET;

			// 比較演算などと融合された分岐系命令かどうかを確認して控える（融合対象になり得るのは JMP と JMPN のみ）。
			boolean isFusedBranchOperation = instruction.isFused()
					&& (fusedOpcodes[1] == OperationCode.JMP || fusedOpcodes[1] == OperationCode.JMPN);

			// 飛び先アドレスを更新
			if (isBranchOperation || isFusedBranchOperation) {
				int destAddr = instruction.getReorderedLabelAddress();
				if (brancDestAddrUpdateMap.containsKey(destAddr)) {
					int updatedDestAddr = brancDestAddrUpdateMap.get(destAddr);
					instruction.setReorderedLabelAddress(updatedDestAddr);

					// ラベルが連続していた場合など、一見すると補正済みアドレスの再補正が必要な場合がありそうに思えるが、
					// 上でマップを作っている際の updatedDestAddr の値は「 次に出現する『 非LABEL命令 』の（補正済み）アドレス 」
					// なので、連続ラベル領域はマップ作製時点で補正に反映されているし、補正先が別のLABEL由来アドレスになる事もない。
					// 従って変換マップは一回通せば十分で、逆に複数回通すとまずいので、「念のため」とかで行ってはいけない。
					// (変換マップのキーと値は、それぞれLABEL削除前と後の命令列におけるアドレスなので、土台がずれていて複数回通せない。)
				}
			}
		}

		// 命令列をLABEL削除済みのものに差し替え
		this.acceleratorInstructionList = updatedInstructionList;
	}
}
