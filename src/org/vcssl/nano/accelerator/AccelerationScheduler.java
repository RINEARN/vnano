package org.vcssl.nano.accelerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.memory.Memory;
import org.vcssl.nano.memory.MemoryAccessException;
import org.vcssl.nano.processor.Instruction;
import org.vcssl.nano.processor.OperationCode;

public class AccelerationScheduler {


	List<AcceleratorInstruction> acceleratorInstructionList;
	AcceleratorInstruction[] buffer;
	Map<Integer,Integer> addressReorderingMap;
	int registerWrittenPointCount[];

	public AcceleratorInstruction[] schedule(Instruction[] instructions, Memory memory) {
		this.initializeeAcceleratorInstructionList(instructions, memory);
		this.createRegisterWrittenPointCount(memory);

		// ここから命令再配置

		this.reorderAllocInstructions();
		this.reduceMovInstructions();

		// ここまで命令再配置

		this.updateReorderedAddresses();
		this.generateAddressReorderingMap();
		this.resolveReorderedJumpAddress(memory);
		return this.acceleratorInstructionList.toArray(new AcceleratorInstruction[0]);
	}


	// フィールドの命令リストを生成して初期化
	private void initializeeAcceleratorInstructionList(Instruction[] instructions, Memory memory) {

		int instructionLength = instructions.length;
		this.acceleratorInstructionList = new ArrayList<AcceleratorInstruction>();
		this.buffer = new AcceleratorInstruction[instructionLength];

		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {

			// InstructionをAcceleratorInstruction（Instructionのサブクラス）に変換
			Instruction instruction = instructions[instructionIndex];
			AcceleratorInstruction acceleratorInstruction = new AcceleratorInstruction(instruction);

			// 再配置前の命令アドレスを書き込む
			acceleratorInstruction.setUnreorderedAddress(instructionIndex);

			// リストに追加
			this.acceleratorInstructionList.add(acceleratorInstruction);
		}
	}

	// レジスタ書き込み箇所カウンタ配列を生成して初期化
	private void createRegisterWrittenPointCount(Memory memory) {
		int registerLength = memory.getSize(Memory.Partition.REGISTER);
		this.registerWrittenPointCount = new int[registerLength];
		Arrays.fill(this.registerWrittenPointCount, 0);

		int instructionLength = acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			AcceleratorInstruction instruction = acceleratorInstructionList.get(instructionIndex);

			// 値が変更されている可能性を調べるために使うので、非書き換え命令や確保・解放はカウントから除外
			if (!this.isRegisterWritingInstruction(instruction)) {
				continue;
			}

			// オペランドが無い場合も除外（ただし、現時点ではそういった命令は無い）
			if (instruction.getOperandLength() == 0) {
				continue;
			}

			// 値の書き込み用である第0オペランドがレジスタではない場合も、この時点で除外
			Memory.Partition writingPartition = instruction.getOperandPartitions()[0];
			if (writingPartition != Memory.Partition.REGISTER) {
				continue;
			}

			// ここまで残るのはレジスタに書き込んでいる場合なので、レジスタ書き込み箇所カウンタを加算
			int writingRegisterAddress = instruction.getOperandAddresses()[0];
			this.registerWrittenPointCount[writingRegisterAddress]++;
		}

		//for (int i=0; i<registerLength; i++) {
		//	System.out.println("Written Count of R" + i + " = " + this.registerWrittenPointCount[i]);
		//}
	}

	// レジスタに対して書き込みを行う命令なら true を返す（ただしレジスタの確保・解放は除外）
	private boolean isRegisterWritingInstruction(AcceleratorInstruction instruction) {

		OperationCode opcode = instruction.getOperationCode();

		return opcode != OperationCode.ALLOC
		     && opcode != OperationCode.FREE
		     && opcode != OperationCode.NOP
		     && opcode != OperationCode.JMP
		     && opcode != OperationCode.JMPN   ;
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
		this.addressReorderingMap = new HashMap<Integer,Integer>();

		int instructionLength = acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			AcceleratorInstruction instruction = this.acceleratorInstructionList.get(instructionIndex);
			//System.out.println("Put reordering map: " + instruction.getUnreorderedAddress() + ", " + instruction.getReorderedAddress());
			addressReorderingMap.put(instruction.getUnreorderedAddress(), instruction.getReorderedAddress());
		}
	}


	// ジャンプ系命令のジャンプ先命令アドレスは、命令再配置によって変わるため、再配置後のアドレス情報を追加
	private void resolveReorderedJumpAddress(Memory memory) {
		int instructionLength = this.acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			AcceleratorInstruction instruction = this.acceleratorInstructionList.get(instructionIndex);
			OperationCode opcode = instruction.getOperationCode();

			if (opcode == OperationCode.JMP || opcode == OperationCode.JMPN) {

				// ジャンプ先アドレスの値を格納するオペランドのデータコンテナをメモリから取得
				DataContainer<?> addressContiner = null;
				try {
					addressContiner = memory.getDataContainer(
							instruction.getOperandPartitions()[1],
							instruction.getOperandAddresses()[1]
					);
				} catch (MemoryAccessException e) {
					// ジャンプ先アドレスを格納する定数にアクセスできないのは、アセンブラかメモリ初期化の異常
					throw new VnanoFatalException(e);
				}

				// データコンテナからジャンプ先アドレスの値を読む
				int jumpAddress = -1;
				Object addressData = addressContiner.getData();
				if (addressData instanceof long[]) {
					jumpAddress = (int)( ((long[])addressData)[0] );
				} else {
					throw new VnanoFatalException("Non-integer jump address detected.");
				}

				// ジャンプ先命令アドレスの、命令再配置前における位置に対応する、再配置後のジャンプ先命令アドレスを取得
				int reorderedJumpAddress = this.addressReorderingMap.get(jumpAddress);

				//System.out.println("Jump addr reordered: " + jumpAddress + " to " + reorderedJumpAddress);

				// 再配置後のジャンプ先アドレス情報をジャンプ命令に追加
				instruction.setReorderedJumpAddress(reorderedJumpAddress);
			}
		}
	}


	// スカラのALLOC命令をコード先頭に移す
	private void reorderAllocInstructions() {

		int instructionLength = this.acceleratorInstructionList.size();

		// 再配置済みの命令列を格納する配列（最後にacceleratorInstructionList移す）
		if (this.buffer.length < instructionLength) {
			this.buffer = new AcceleratorInstruction[instructionLength]; // 足りなければ再確保（余っていればそのまま使う）
		} else {
			Arrays.fill(this.buffer, null);
		}

		// 元の命令列 instructions の要素の内、再配置された要素のインデックスをマークする配列
		boolean[] reordered = new boolean[instructionLength];
		Arrays.fill(reordered, false);

		int reorderedInstructionIndex = 0;

		// acceleratorInstructionList を先頭から末尾までスイープし、スカラALLOC命令を reorderedInstruction に移す
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {

			AcceleratorInstruction instruction = acceleratorInstructionList.get(instructionIndex);
			OperationCode opcode = instruction.getOperationCode();
			int operandLength = instruction.getOperandLength();

			// 1オペランドのALLOC命令はスカラALLOC
			if (opcode == OperationCode.ALLOC && operandLength == 1) {

				// reorderedInstruction に積む
				this.buffer[reorderedInstructionIndex] = instruction;
				reorderedInstructionIndex++;

				// このインデックスの命令は再配列済みである事をマークする
				reordered[instructionIndex] = true;
			}
		}

		// 再び acceleratorInstructionList をスイープし、再配置されていない（スカラALLOC以外の）命令を移す
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			if (!reordered[instructionIndex]) {
				AcceleratorInstruction instruction = acceleratorInstructionList.get(instructionIndex);
				this.buffer[reorderedInstructionIndex] = instruction;
				reorderedInstructionIndex++;
			}
		}

		// バッファの中の完成した結果をacceleratorInstructionList移す
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			this.acceleratorInstructionList.set(instructionIndex, this.buffer[instructionIndex]);
		}
	}


	// レジスタへの無駄なMOV命令を削減し、算術演算の出力オペランド等で直接レジスタに代入するようにする
	private void reduceMovInstructions() {

		/*
			・前処理として、それぞれのレジスタに対して、それに値を書き込んでいる場所の数をカウントしておく

			・命令列の頭から演算命令をスイープしていく
			・演算命令で、一か所でしか書き込んでいないレジスタが出力オペランドに指定されていて
			・かつ、その直後にそのレジスタをMOVの入力にしている
			　↓
			・MOV先の変数を演算の出力オペランドに入れて
			・MOV命令を削る

			→ レジスタ使用回数は関係なく、演算結果を次の行で変数にMOVしてるような場合は移して削っていいのでは？
			   → 格納した演算結果を後で別に参照する場合もあり得るのでNG
		*/

		int instructionLength = this.acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength-1; instructionIndex++) { // 後でイテレータ使うループにする
			AcceleratorInstruction currentInstruction = this.acceleratorInstructionList.get(instructionIndex);
			AcceleratorInstruction nextInstruction = this.acceleratorInstructionList.get(instructionIndex+1);

			// 対象命令がレジスタ書き込みをしていない命令はスキップ
			if (!this.isRegisterWritingInstruction(currentInstruction)) {
				continue;
			}

			// 次にMOV命令が続いていなければスキップ
			if (nextInstruction.getOperationCode() != OperationCode.MOV) {
				continue;
			}

			// そのMOV命令のコピー元がレジスタではない場合はスキップ
			if (nextInstruction.getOperandPartitions()[1] != Memory.Partition.REGISTER) {
				continue;
			}

			// 対象命令で書き込んでいるレジスタ（ = 0番オペランド）のアドレスを取得
			int writingRegisterAddress = currentInstruction.getOperandAddresses()[0];

			// 次のMOV命令でのコピー元レジスタを取得
			int movingRegisterAddress = nextInstruction.getOperandAddresses()[1];

			// 書き込み先レジスタと次命令でのコピー元レジスタが異なる場合はスキップ
			if (writingRegisterAddress != movingRegisterAddress) {
				continue;
			}

			// そのレジスタに書き込んでいる箇所がそこだけ（1箇所だけ）でない場合はスキップ
			if (this.registerWrittenPointCount[writingRegisterAddress] != 1) {
				continue;
			}


			// ここまで到達するのは、演算結果をレジスタに格納し、次で別の領域にMOVしている場合であり、
			// かつ、そのレジスタを他のどこでも使用していない場合なので、
			// MOV先に演算結果を直接に格納するようにして、MOV命令を削る


			// 対象演算命令のオペランド部を複製
			int modifiedOperandLength = currentInstruction.getOperandLength();
			Memory.Partition[] modifiedOperandPartitions = new Memory.Partition[modifiedOperandLength];
			System.arraycopy(currentInstruction.getOperandPartitions(), 0, modifiedOperandPartitions, 0, modifiedOperandLength);
			int[] modifiedOperandAddresses = new int[modifiedOperandLength];
			System.arraycopy(currentInstruction.getOperandAddresses(), 0, modifiedOperandAddresses, 0, modifiedOperandLength);

			// 複製したオペランド部の出力オペランドに、MOV先オペランドを写す
			modifiedOperandPartitions[0] = nextInstruction.getOperandPartitions()[0];
			modifiedOperandAddresses[0] = nextInstruction.getOperandAddresses()[0];

			// それをオペランド部として持つ、対象演算命令のコピーを生成し、元の対象演算命令を置き換える
			AcceleratorInstruction modifiedInstruction = new AcceleratorInstruction(
				currentInstruction, modifiedOperandPartitions, modifiedOperandAddresses
			);
			this.acceleratorInstructionList.set(instructionIndex, modifiedInstruction);

			// MOV命令を null で置き換える（すぐ後で削除する）
			this.acceleratorInstructionList.set(instructionIndex+1, null);

			// 次の命令（= MOV）はもう削除したので、カウンタを1つ余計に進める
			instructionIndex++;
		}

		this.removeNullInstructions();
		// MOV命令を削除した位置の null を詰める
	}

	// 命令列内の null 要素を削除して詰める（removeを何度も行う処理を効率化するため、nullを置いて後でこのメソッドで一括で詰める）
	private void removeNullInstructions() {

		int instructionLength = this.acceleratorInstructionList.size();

		// 再配置済みの命令列を格納する配列（最後にacceleratorInstructionList移す）
		if (this.buffer.length < instructionLength) {
			this.buffer = new AcceleratorInstruction[instructionLength]; // 足りなければ再確保（余っていればそのまま使う）
		} else {
			Arrays.fill(this.buffer, null);
		}

		// acceleratorInstructionList 内の非null要素を詰めながらbufferに移す
		int bufferIndex = 0;
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			AcceleratorInstruction instruction = this.acceleratorInstructionList.get(instructionIndex);
			if (instruction != null) {
				this.buffer[bufferIndex] = instruction;
				bufferIndex++;
			}
		}

		instructionLength = bufferIndex;
		this.acceleratorInstructionList = new ArrayList<AcceleratorInstruction>(instructionLength);

		// bufferからacceleratorInstructionListに戻す
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			this.acceleratorInstructionList.add(buffer[instructionIndex]);
		}
	}

}
