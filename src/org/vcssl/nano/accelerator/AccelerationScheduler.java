package org.vcssl.nano.accelerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.memory.Memory;
import org.vcssl.nano.memory.MemoryAccessException;
import org.vcssl.nano.processor.Instruction;
import org.vcssl.nano.processor.OperationCode;

public class AccelerationScheduler {


	private class AcceleratorInstructionReorderedAddressComparator
			implements Comparator<AcceleratorInstruction>{

		@Override
		public int compare(AcceleratorInstruction instruction1, AcceleratorInstruction instruction2) {
			return instruction1.getUnreorderedAddress() - instruction2.getUnreorderedAddress();
		}
	}




	List<AcceleratorInstruction> acceleratorInstructionList;
	Map<Integer,Integer> addressReorderingMap;

	public AcceleratorInstruction[] schedule(Instruction[] instructions, Memory memory) {
		this.createAcceleratorInstructionList(instructions, memory);

		// ここで命令再配置
		this.reorderAllocInstructions();

		this.updateReorderedAddresses();
		this.generateAddressReorderingMap();
		this.resolveReorderedJumpAddress(memory);
		return this.acceleratorInstructionList.toArray(new AcceleratorInstruction[0]);
	}

	public void createAcceleratorInstructionList(Instruction[] instructions, Memory memory) {

		int instructionLength = instructions.length;
		this.acceleratorInstructionList = new ArrayList<AcceleratorInstruction>();

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


	// 全命令に対して再配置済み命令アドレスを書き込む
	public void updateReorderedAddresses() {
		int instructionLength = acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			acceleratorInstructionList.get(instructionIndex).setReorderedAddress(instructionIndex);
		}
	}


	// 全命令アドレスの再配置前→再配置後の対応を格納したアドレス変換マップを生成
	public void generateAddressReorderingMap() {
		this.addressReorderingMap = new HashMap<Integer,Integer>();

		int instructionLength = acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			AcceleratorInstruction instruction = this.acceleratorInstructionList.get(instructionIndex);
			System.out.println("Put " + instruction.getUnreorderedAddress() + ", " + instruction.getReorderedAddress());
			addressReorderingMap.put(instruction.getUnreorderedAddress(), instruction.getReorderedAddress());
		}
	}


	// ジャンプ系命令のジャンプ先命令アドレスは、命令再配置によって変わるため、再配置後のアドレス情報を追加
	public void resolveReorderedJumpAddress(Memory memory) {
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

				System.out.println("Jump addr reordered: " + jumpAddress + " to " + reorderedJumpAddress);

				// 再配置後のジャンプ先アドレス情報をジャンプ命令に追加
				instruction.setReorderedJumpAddress(reorderedJumpAddress);
			}
		}
	}










	public void reorderAllocInstructions() {

		int instructionLength = this.acceleratorInstructionList.size();

		// 再配置済みの命令列を格納する配列
		AcceleratorInstruction[] reorderedInstruction = new AcceleratorInstruction[instructionLength];

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
				reorderedInstruction[reorderedInstructionIndex] = instruction;
				reorderedInstructionIndex++;

				// このインデックスの命令は再配列済みである事をマークする
				reordered[instructionIndex] = true;
			}
		}

		// 再び acceleratorInstructionList をスイープし、再配置されていない（スカラALLOC以外の）命令を移す
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			if (!reordered[instructionIndex]) {
				AcceleratorInstruction instruction = acceleratorInstructionList.get(instructionIndex);
				reorderedInstruction[reorderedInstructionIndex] = instruction;
				reorderedInstructionIndex++;
			}
		}

		this.acceleratorInstructionList = Arrays.asList(reorderedInstruction);
	}

}
