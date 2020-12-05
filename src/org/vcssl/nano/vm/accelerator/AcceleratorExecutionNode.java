/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */
package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.VnanoFatalException;

public abstract class AcceleratorExecutionNode {

	// このノードの次に実行されるべきノードを保持する
	// ( 元々 final 付きだったが、final を外してみても有意なパフォーマンス低下はほぼ無さそうだったので外した。
	//   分岐処理などでは execute() がこれとは別の非 final ノードを返すので、これが final でも有効な最適化はできないと思う。)
	protected AcceleratorExecutionNode nextNode;

	// このノードの execute() が何個の命令（融合された拡張命令ではない、元のVRIL命令）に相当する処理を行うかを表す値
	// ( こちらは外から直接参照するので final な方が原理的には最適化可能性が広そうに思えるけど、影響は未比較で、念のため。)
	protected final int INSTRUCTIONS_PER_NODE;

	// このノードが実行する処理に対応する命令を保持する
	// ( 演算には使用しないが、エラー発生時に実行対象命令を辿れるように保持 )
	AcceleratorInstruction sourceInstruction;

	// nextNode の final 化をやめたので、もうコンストラクタで渡す必要もない気がする
	// (このコンストラクタ仕様のためにAcceleratorDispatchUnit側でノード列の生成順序が少しややこしくなってる)。
	// あと、引数に instructionsPerNode の代わりに sourceInstruction を取れば、
	// そこから前者の値も求まるし、別途 sourceInstruction を set する必要も無くなるしで、そうした方がいい気がする。
	// またきりのいい時に要改修
	public AcceleratorExecutionNode(AcceleratorExecutionNode nextNode, int instructionsPerNode) {
		this.nextNode = nextNode;
		this.INSTRUCTIONS_PER_NODE = instructionsPerNode;
	}

	public void setNextNode(AcceleratorExecutionNode nextNode) {
		this.nextNode = nextNode;
	}

	public void setSourceInstruction(AcceleratorInstruction instruction) {
		this.sourceInstruction = instruction;
	}
	public AcceleratorInstruction getSourceInstruction() {
		return this.sourceInstruction;
	}

	public abstract AcceleratorExecutionNode execute() throws VnanoException;

	// 名前変えた方がいいかもしれない。setBranchDestinationNodes とか。きりのいい時に要検討
	public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
		throw new VnanoFatalException("This method is unavailable for this node");
	}
	public AcceleratorExecutionNode[] getLaundingPointNodes() {
		throw new VnanoFatalException("This method is unavailable for this node");
	}
}
