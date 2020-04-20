/*
 * Copyright(C) 2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import java.util.Arrays;

import org.vcssl.nano.VnanoException;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.processor.ExecutionUnit;
import org.vcssl.nano.vm.processor.Instruction;

public class InternalFunctionControlUnit extends AcceleratorExecutionUnit {

	/* 関数の戻り先地点の命令アドレスを詰む、命令アドレススタックのデフォルトの要素数です。 */
	private static final int DEFAULT_ADDRESS_STACK_LENGTH = 1024;

	/* 関数の引数や戻り値などのデータを積む、データスタックのデフォルトの要素数です。 */
	private static final int DEFAULT_DATA_STACK_LENGTH = 1024;

	/* 関数の戻り先地点の命令アドレスを詰む、命令アドレススタックです。 */
	private int[] addressStack = null;

	/* 関数の引数や戻り値などのデータを積む、データスタックです。 */
	private DataContainer<?>[] dataStack = null;

	/** 命令アドレススタックの要素数です。 */
	private int addressStackLength = DEFAULT_ADDRESS_STACK_LENGTH;

	/** データスタックの要素数です。 */
	private int dataStackLength = DEFAULT_DATA_STACK_LENGTH;

	/** 命令アドレススタックの先頭位置です。 */
	private int addressStackPointer = 0;

	/** データスタックの先頭位置です。 */
	private int dataStackPointer = 0;

	/** 全命令（順序は最適化による再配置済み）に対応するノードを格納する配列です。関数からのリターン時に参照します。 */
	private AcceleratorExecutionNode[] allNodes;

	/** 関数先頭の命令アドレスをインデックスとして、関数が実行中に、対応する要素の値が true になるテーブルです。 */
	private boolean[] functionRunningFlags;


	/**
	 * デフォルトの要素数のスタック領域（可変）を持つインスタンスを生成します。
	 */
	public InternalFunctionControlUnit () {
		this.addressStack = new int[ this.addressStackLength ];
		this.dataStack = new DataContainer<?>[ this.dataStackLength ];
	}


	/**
	 * 全命令（順序は最適化による再配置済み）に対応するノードを設定します。
	 * 設定したノードに対応できるように、関数実行判定テーブルなどのリソースも初期化されます。
	 *
	 * 関数からのリターン時には、スタック上に動的に積まれた命令アドレスに戻る必要があるため、
	 * 実行時にこのメソッドで設定されたノード配列を参照し、該当するノードに処理が戻ります。
	 *
	 * @param allNodes 全命令に対応するノードを格納する配列
	 */
	public void setNodes(AcceleratorExecutionNode[] allNodes) {
		this.allNodes = allNodes;
		this.functionRunningFlags = new boolean[ allNodes.length ];
		Arrays.fill(functionRunningFlags, false);
	}


	/**
	 * 命令アドレススタックの要素数を2倍に拡張します。
	 */
	private void expandAddressStack() {

		// 現在のスタックの中身を仮の配列に退避
		int[] stock = new int[ this.addressStackLength ];
		System.arraycopy(this.addressStack, 0, stock, 0, this.addressStackLength);

		// スタックの要素数を倍に拡張
		this.addressStackLength *= 2;
		this.addressStack = new int[ this.addressStackLength ];

		// 仮の配列から現在のスタックの中身を復元
		System.arraycopy(stock, 0, this.addressStack, 0, stock.length);
	}


	/**
	 * データスタックの要素数を2倍に拡張します。
	 */
	private void expandDataStack() {

		// 現在のスタックの中身を仮の配列に退避
		int[] stock = new int[ this.dataStackLength ];
		System.arraycopy(this.dataStack, 0, stock, 0, this.dataStackLength);

		// スタックの要素数を倍に拡張
		this.dataStackLength *= 2;
		this.dataStack = new DataContainer<?>[ this.dataStackLength ];

		// 仮の配列から現在のスタックの中身を復元
		System.arraycopy(stock, 0, this.dataStack, 0, stock.length);
	}

	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		CacheSynchronizer synchronizer = new GeneralScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled);

		OperationCode opcode = instruction.getOperationCode();
		DataType dataType = instruction.getDataTypes()[0];
		switch (opcode) {
			case CALL : {
				int reorderedAddressOfThisInstruction = instruction.getReorderedAddress();
				int reorderdFunctionAddress = instruction.getReorderedLabelAddress();
				return new CallNode(
					operandContainers, synchronizer, reorderedAddressOfThisInstruction, reorderdFunctionAddress, nextNode
				);
			}
			case RET : {
				int reorderdFunctionAddress = instruction.getReorderedLabelAddress();
				return new ReturnNode(operandContainers, synchronizer, reorderdFunctionAddress, nextNode);
			}
			case ALLOCP : {
				return new AllocpNode(operandContainers, dataType, nextNode);
			}
			case POP : {
				return new PopNode(nextNode);
			}
			case REFPOP : {
				return this.generateRefpopNode(instruction, operandContainers, operandScalar, synchronizer, nextNode);
			}
			case MOVPOP : {
				return this.generateMovpopNode(
					(Instruction)instruction, operandContainers,
					(ScalarCache[])operandCaches, operandCachingEnabled, operandScalar, synchronizer, nextNode
				);
			}
			case EX : {
				AcceleratorExtendedOperationCode extendedOpcode = instruction.getExtendedOperationCode();
				if (extendedOpcode == AcceleratorExtendedOperationCode.RETURNED) {
					return new ReturnedNode(synchronizer, nextNode);
				} else {
					throw new VnanoFatalException("Unsupported extended operation code for this unit: " + extendedOpcode);
				}
			}
			default : {
				throw new VnanoFatalException("Unsupported operation code for this unit: " + opcode);
			}
		}
	}

	private AcceleratorExecutionNode generateMovpopNode(
			Instruction instruction, DataContainer<?>[] operandContainers,
			ScalarCache[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar,
			CacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

		DataType dataType = instruction.getDataTypes()[0];

		if (operandCachingEnabled[0] && operandScalar[0]) {

			switch (dataType) {
				case INT64 : {
					return new Int64CachedScalarMovpopNode((Int64ScalarCache)operandCaches[0], nextNode);
				}
				case FLOAT64 : {
					return new Float64CachedScalarMovpopNode((Float64ScalarCache)operandCaches[0], nextNode);
				}
				case BOOL : {
					return new BoolCachedScalarMovpopNode((BoolScalarCache)operandCaches[0], nextNode);
				}
				default : {
					return new GeneralMovpopNode(operandContainers, synchronizer, nextNode);
				}
			}

		} else if (operandScalar[0]) {

			switch (dataType) {
				case INT64 : {
					return new Int64ScalarMovpopNode(operandContainers, synchronizer, nextNode);
				}
				case FLOAT64 : {
					return new Float64ScalarMovpopNode(operandContainers, synchronizer, nextNode);
				}
				case BOOL : {
					return new BoolScalarMovpopNode(operandContainers, synchronizer, nextNode);
				}
				default : {
					return new GeneralMovpopNode(operandContainers, synchronizer, nextNode);
				}
			}

		} else {
			return new GeneralMovpopNode(operandContainers, synchronizer, nextNode);
		}
	}

	private AcceleratorExecutionNode generateRefpopNode(
			Instruction instruction, DataContainer<?>[] operandContainers, boolean[] operandScalar,
			CacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

		if (operandScalar[0]) {
			return new ScalarRefpopNode(operandContainers, synchronizer, nextNode);
		} else {
			return new VectorRefpopNode(operandContainers, synchronizer, nextNode);
		}
	}

	private final class CallNode extends AcceleratorExecutionNode {
		private final DataContainer<?>[] operandContainers;
		private final CacheSynchronizer synchronizer;
		private AcceleratorExecutionNode functionHeadNode;
		private int functionAddress;
		private int returnAddress;

		public CallNode(DataContainer<?>[] operandContainers, CacheSynchronizer synchronizer,
				int reorderedAddressOfThisInstruction, int functionAddress, AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.synchronizer = synchronizer;
			this.operandContainers = operandContainers;
			this.returnAddress = reorderedAddressOfThisInstruction + 1;
			this.functionAddress = functionAddress;
		}

		@Override
		public final void setLaundingPointNodes(AcceleratorExecutionNode ... nodes) {
			this.functionHeadNode = nodes[0];
		}

		@Override
		public final AcceleratorExecutionNode execute() throws VnanoException {
			this.synchronizer.synchronizeFromCacheToMemory();

			// このスクリプトエンジンでは再帰呼び出しをサポートしていないため、関数が既に実行中ならエラーとし、そうでなければマークする
			if (InternalFunctionControlUnit.this.functionRunningFlags[this.functionAddress]) {
				throw new VnanoException(ErrorType.RECURSIVE_FUNCTION_CALL);
			}
			InternalFunctionControlUnit.this.functionRunningFlags[this.functionAddress] = true;

			// 戻り先地点の命令アドレスを、アドレススタックに積む
			if (InternalFunctionControlUnit.this.addressStackLength <= InternalFunctionControlUnit.this.addressStackPointer) {
				InternalFunctionControlUnit.this.expandAddressStack();
			}
			InternalFunctionControlUnit.this.addressStack[ InternalFunctionControlUnit.this.addressStackPointer ] = this.returnAddress;
			InternalFunctionControlUnit.this.addressStackPointer++;

			// 引数をデータスタックに積む
			int operandLength = this.operandContainers.length;
			while (InternalFunctionControlUnit.this.dataStackLength <= InternalFunctionControlUnit.this.dataStackPointer + operandLength) {
				InternalFunctionControlUnit.this.expandDataStack();
			}
			for (int i=2; i<operandLength; i++) { // [0]はプレースホルダ、[1]は飛び先ラベルアドレス、なので[2]からが引数
				InternalFunctionControlUnit.this.dataStack[ InternalFunctionControlUnit.this.dataStackPointer ] = this.operandContainers[i];
				InternalFunctionControlUnit.this.dataStackPointer++;
			}

			// 関数の先頭の命令に飛ぶ
			return this.functionHeadNode;
		}
	}


	private final class ReturnNode extends AcceleratorExecutionNode {
		private final DataContainer<?> returnValueContainer;
		private final CacheSynchronizer synchronizer;
		private int functionAddress;

		public ReturnNode(DataContainer<?>[] operandContainers, CacheSynchronizer synchronizer,
				int reorderedAddressOfFunction, AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.synchronizer = synchronizer;

			// 戻り値のデータコンテナをこのインスタンスに保持しておく
			if (2 < operandContainers.length) {
				this.returnValueContainer = operandContainers[2]; // オペランド[0]はプレースホルダ、[1]は関数アドレスなので、[2]が戻り値

			// 戻り値が無い場合でも、戻り値がある場合とスタックの積み下ろしを同じ順序に統一するため、戻る時に積む空のデータコンテナを用意
			} else {
				this.returnValueContainer = new DataContainer<Void>();
			}

			this.functionAddress = reorderedAddressOfFunction;
		}


		@Override
		public final void setLaundingPointNodes(AcceleratorExecutionNode ... nodes) {
		}


		@Override
		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();

			// 戻り先地点の命令アドレスを、アドレススタックから取り出す
			--InternalFunctionControlUnit.this.addressStackPointer;
			int returnedPointAddress = InternalFunctionControlUnit.this.addressStack[ InternalFunctionControlUnit.this.addressStackPointer ];

			// 戻り値をデータスタックに積む
			if (InternalFunctionControlUnit.this.dataStackLength <= InternalFunctionControlUnit.this.dataStackPointer) {
				InternalFunctionControlUnit.this.expandDataStack();
			}
			InternalFunctionControlUnit.this.dataStack[ InternalFunctionControlUnit.this.dataStackPointer ] = this.returnValueContainer;
			InternalFunctionControlUnit.this.dataStackPointer++;

			// 再帰呼び出し検出用のマークを解除する
			InternalFunctionControlUnit.this.functionRunningFlags[this.functionAddress] = false;

			// 戻り先地点のノードを返す
			return InternalFunctionControlUnit.this.allNodes[returnedPointAddress];
		}
	}

	public final class ReturnedNode extends AcceleratorExecutionNode {
		private final CacheSynchronizer synchronizer;

		public ReturnedNode(CacheSynchronizer synchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.synchronizer = synchronizer;
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			// 実引数と仮引数の仮想メモリアドレスは異なるため、
			// 関数の引数を参照渡ししていた場合、関数内で引数の値が書き換わっても、
			// 呼び出し側で実引数に渡したアドレスに紐づいているキャッシュはそれに気付かず更新されない。
			// （仮想メモリ上のデータは、実引数と仮引数のアドレスのデータが参照リンクされるため更新される。）
			// そのため、関数から戻ってきた時点で、仮想メモリの値を読んでキャッシュに反映させる必要がある。
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}

	}


	private final class AllocpNode extends AcceleratorExecutionNode {
		private final DataContainer<?> allocTargetContainer;
		private final DataType dataType;

		public AllocpNode(DataContainer<?>[] operandContainers, DataType dataType, AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.allocTargetContainer = operandContainers[0];
			this.dataType = dataType;
		}


		@Override
		public final void setLaundingPointNodes(AcceleratorExecutionNode ... nodes) {
		}


		@Override
		public final AcceleratorExecutionNode execute() {
			DataContainer<?> src = InternalFunctionControlUnit.this.dataStack[ InternalFunctionControlUnit.this.dataStackPointer - 1 ];
			new ExecutionUnit().allocSameLengths(dataType, allocTargetContainer, src);
			return this.nextNode;
		}
	}


	private final class PopNode extends AcceleratorExecutionNode {

		public PopNode(AcceleratorExecutionNode nextNode) {
			super(nextNode);
		}

		@Override
		public final void setLaundingPointNodes(AcceleratorExecutionNode ... nodes) {
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			--InternalFunctionControlUnit.this.dataStackPointer;
			return this.nextNode;
		}
	}




	private final class GeneralMovpopNode extends AcceleratorExecutionNode {
		private final DataContainer<?>[] operandContainers;
		private final CacheSynchronizer synchronizer;

		public GeneralMovpopNode(DataContainer<?>[] operandContainers, CacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.synchronizer = synchronizer;
			this.operandContainers = operandContainers;
		}

		@Override
		public final void setLaundingPointNodes(AcceleratorExecutionNode ... nodes) {
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			--InternalFunctionControlUnit.this.dataStackPointer;
			DataContainer<?> src = InternalFunctionControlUnit.this.dataStack[ InternalFunctionControlUnit.this.dataStackPointer ];
			DataContainer<?> dest = this.operandContainers[0];
			System.arraycopy(src.getData(), src.getOffset(), dest.getData(), dest.getOffset(), dest.getSize());

			// スタックの値をメモリに書き込んだので、そのメモリの値でキャッシュも更新しておく
			synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}


	private final class Int64ScalarMovpopNode extends AcceleratorExecutionNode {
		private final DataContainer<?>[] operandContainers;
		private final CacheSynchronizer synchronizer;

		public Int64ScalarMovpopNode(DataContainer<?>[] operandContainers, CacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.synchronizer = synchronizer;
			this.operandContainers = operandContainers;
		}

		@Override
		public final void setLaundingPointNodes(AcceleratorExecutionNode ... nodes) {
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			--InternalFunctionControlUnit.this.dataStackPointer;
			@SuppressWarnings("unchecked")
			DataContainer<long[]> src = (DataContainer<long[]>)InternalFunctionControlUnit.this.dataStack[
			        InternalFunctionControlUnit.this.dataStackPointer
			];
			@SuppressWarnings("unchecked")
			DataContainer<long[]> dest = (DataContainer<long[]>)this.operandContainers[0];

			dest.getData()[ dest.getOffset() ] = src.getData()[ src.getOffset() ];

			// スタックの値をメモリに書き込んだので、そのメモリの値でキャッシュも更新しておく
			synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}


	private final class Float64ScalarMovpopNode extends AcceleratorExecutionNode {
		private final DataContainer<?>[] operandContainers;
		private final CacheSynchronizer synchronizer;

		public Float64ScalarMovpopNode(DataContainer<?>[] operandContainers, CacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.synchronizer = synchronizer;
			this.operandContainers = operandContainers;
		}

		@Override
		public final void setLaundingPointNodes(AcceleratorExecutionNode ... nodes) {
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			--InternalFunctionControlUnit.this.dataStackPointer;
			@SuppressWarnings("unchecked")
			DataContainer<double[]> src = (DataContainer<double[]>)InternalFunctionControlUnit.this.dataStack[
			        InternalFunctionControlUnit.this.dataStackPointer
			];
			@SuppressWarnings("unchecked")
			DataContainer<double[]> dest = (DataContainer<double[]>)this.operandContainers[0];

			dest.getData()[ dest.getOffset() ] = src.getData()[ src.getOffset() ];

			// スタックの値をメモリに書き込んだので、そのメモリの値でキャッシュも更新しておく
			synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}


	private final class BoolScalarMovpopNode extends AcceleratorExecutionNode {
		private final DataContainer<?>[] operandContainers;
		private final CacheSynchronizer synchronizer;

		public BoolScalarMovpopNode(DataContainer<?>[] operandContainers, CacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.synchronizer = synchronizer;
			this.operandContainers = operandContainers;
		}

		@Override
		public final void setLaundingPointNodes(AcceleratorExecutionNode ... nodes) {
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			--InternalFunctionControlUnit.this.dataStackPointer;
			@SuppressWarnings("unchecked")
			DataContainer<boolean[]> src = (DataContainer<boolean[]>)InternalFunctionControlUnit.this.dataStack[
			        InternalFunctionControlUnit.this.dataStackPointer
			];
			@SuppressWarnings("unchecked")
			DataContainer<boolean[]> dest = (DataContainer<boolean[]>)this.operandContainers[0];

			dest.getData()[ dest.getOffset() ] = src.getData()[ src.getOffset() ];

			// スタックの値をメモリに書き込んだので、そのメモリの値でキャッシュも更新しておく
			synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}


	private final class Int64CachedScalarMovpopNode extends AcceleratorExecutionNode {
		private final Int64ScalarCache cache;

		public Int64CachedScalarMovpopNode(Int64ScalarCache cache, AcceleratorExecutionNode nextNode) {
			super(nextNode);
			this.cache = cache;
		}

		@Override
		public final void setLaundingPointNodes(AcceleratorExecutionNode ... nodes) {
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			--InternalFunctionControlUnit.this.dataStackPointer;
			@SuppressWarnings("unchecked")
			DataContainer<long[]> src = (DataContainer<long[]>)InternalFunctionControlUnit.this.dataStack[
			        InternalFunctionControlUnit.this.dataStackPointer
			];

			this.cache.value = src.getData()[ src.getOffset() ];

			return this.nextNode;
		}
	}


	private final class Float64CachedScalarMovpopNode extends AcceleratorExecutionNode {
		private final Float64ScalarCache cache;

		public Float64CachedScalarMovpopNode(Float64ScalarCache cache, AcceleratorExecutionNode nextNode) {
			super(nextNode);
			this.cache = cache;
		}

		@Override
		public final void setLaundingPointNodes(AcceleratorExecutionNode ... nodes) {
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			--InternalFunctionControlUnit.this.dataStackPointer;
			@SuppressWarnings("unchecked")
			DataContainer<double[]> src = (DataContainer<double[]>)InternalFunctionControlUnit.this.dataStack[
			        InternalFunctionControlUnit.this.dataStackPointer
			];

			this.cache.value = src.getData()[ src.getOffset() ];

			return this.nextNode;
		}
	}


	private final class BoolCachedScalarMovpopNode extends AcceleratorExecutionNode {
		private final BoolScalarCache cache;

		public BoolCachedScalarMovpopNode(BoolScalarCache cache, AcceleratorExecutionNode nextNode) {
			super(nextNode);
			this.cache = cache;
		}

		@Override
		public final void setLaundingPointNodes(AcceleratorExecutionNode ... nodes) {
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			--InternalFunctionControlUnit.this.dataStackPointer;
			@SuppressWarnings("unchecked")
			DataContainer<boolean[]> src = (DataContainer<boolean[]>)InternalFunctionControlUnit.this.dataStack[
			        InternalFunctionControlUnit.this.dataStackPointer
			];

			this.cache.value = src.getData()[ src.getOffset() ];

			return this.nextNode;
		}
	}




	private final class ScalarRefpopNode extends AcceleratorExecutionNode {
		private final DataContainer<long[]>[] operandContainers;
		private final CacheSynchronizer synchronizer;

		@SuppressWarnings("unchecked")
		public ScalarRefpopNode(DataContainer<?>[] operandContainers, CacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.synchronizer = synchronizer;
			this.operandContainers = (DataContainer<long[]>[])operandContainers;
		}

		@Override
		public final void setLaundingPointNodes(AcceleratorExecutionNode ... nodes) {
		}

		@SuppressWarnings("unchecked")
		@Override
		public final AcceleratorExecutionNode execute() {
			--InternalFunctionControlUnit.this.dataStackPointer;
			DataContainer<?> src = InternalFunctionControlUnit.this.dataStack[ InternalFunctionControlUnit.this.dataStackPointer ];
			DataContainer<?> dest = this.operandContainers[0];
			((DataContainer<Object>)dest).setData(src.getData(), src.getOffset());

			// スタックの値をメモリに書き込んだので、そのメモリの値でキャッシュも更新しておく
			synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

	private final class VectorRefpopNode extends AcceleratorExecutionNode {
		private final DataContainer<long[]>[] operandContainers;
		private final CacheSynchronizer synchronizer;

		@SuppressWarnings("unchecked")
		public VectorRefpopNode(DataContainer<?>[] operandContainers, CacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.synchronizer = synchronizer;
			this.operandContainers = (DataContainer<long[]>[])operandContainers;
		}

		@Override
		public final void setLaundingPointNodes(AcceleratorExecutionNode ... nodes) {
		}

		@SuppressWarnings("unchecked")
		@Override
		public final AcceleratorExecutionNode execute() {
			--InternalFunctionControlUnit.this.dataStackPointer;
			DataContainer<?> src = InternalFunctionControlUnit.this.dataStack[ InternalFunctionControlUnit.this.dataStackPointer ];
			DataContainer<?> dest = this.operandContainers[0];
			((DataContainer<Object>)dest).setData(src.getData(), src.getLengths());

			// スタックの値をメモリに書き込んだので、そのメモリの値でキャッシュも更新しておく
			synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

}
