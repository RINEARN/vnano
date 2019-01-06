package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.processor.Instruction;

public class InternalFunctionControlUnit {

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


	/**
	 * デフォルトの要素数のスタック領域（可変）を持つインスタンスを生成します。
	 */
	public InternalFunctionControlUnit () {
		this.addressStack = new int[ this.addressStackLength ];
		this.dataStack = new DataContainer<?>[ this.dataStackLength ];
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
		System.arraycopy(stock, 0, this.addressStack, 0, this.addressStackLength);
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
		System.arraycopy(stock, 0, this.dataStack, 0, this.dataStackLength);
	}

	public AccelerationExecutorNode generateExecutorNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			ScalarCache[] operandCaches, boolean[] operandCached, boolean[] operandScalar, CacheSynchronizer synchronizer,
			int reorderedAddressOfThisInstruction, AccelerationExecutorNode nextNode) {

		OperationCode opcode = instruction.getOperationCode();
		switch (opcode) {
			case CALL : {
				return new CallExecutorNode(operandContainers, synchronizer, reorderedAddressOfThisInstruction, nextNode);
			}
			case RET : {
				return new ReturnExecutorNode(operandContainers, synchronizer, nextNode);
			}
			case REFPOP : {
				return new RefpopExecutorNode(operandContainers, synchronizer, nextNode);
			}
			case MOVPOP : {
				return this.generateMovpopExecutorNode(
						instruction, operandContainers, operandCaches, operandCached, operandScalar, synchronizer,
						reorderedAddressOfThisInstruction, nextNode
				);
			}
			default : {
				throw new VnanoFatalException("Unsupported operation code for this unit: " + opcode);
			}
		}
	}

	private AccelerationExecutorNode generateMovpopExecutorNode(
			Instruction instruction, DataContainer<?>[] operandContainers,
			ScalarCache[] operandCaches, boolean[] operandCached, boolean[] operandScalar, CacheSynchronizer synchronizer,
			int reorderedAddressOfThisInstruction, AccelerationExecutorNode nextNode) {

		DataType dataType = instruction.getDataTypes()[0];

		if (operandCached[0] && operandScalar[0]) {

			switch (dataType) {
				case INT64 : {
					return new Int64CachedScalarMovpopExecutorNode((Int64ScalarCache)operandCaches[0], nextNode);
				}
				case FLOAT64 : {
					return new Float64CachedScalarMovpopExecutorNode((Float64ScalarCache)operandCaches[0], nextNode);
				}
				case BOOL : {
					return new BoolCachedScalarMovpopExecutorNode((BoolScalarCache)operandCaches[0], nextNode);
				}
				default : {
					return new MovpopExecutorNode(operandContainers, synchronizer, nextNode);
				}
			}

		} else if (operandScalar[0]) {

			switch (dataType) {
				case INT64 : {
					return new Int64ScalarMovpopExecutorNode(operandContainers, synchronizer, nextNode);
				}
				case FLOAT64 : {
					return new Float64ScalarMovpopExecutorNode(operandContainers, synchronizer, nextNode);
				}
				case BOOL : {
					return new BoolScalarMovpopExecutorNode(operandContainers, synchronizer, nextNode);
				}
				default : {
					return new MovpopExecutorNode(operandContainers, synchronizer, nextNode);
				}
			}

		} else {
			return new MovpopExecutorNode(operandContainers, synchronizer, nextNode);
		}
	}

	private final class CallExecutorNode extends AccelerationExecutorNode {
		private final DataContainer<?>[] operandContainers;
		private final CacheSynchronizer synchronizer;
		private AccelerationExecutorNode functionHeadNode;
		private int returnAddress;

		public CallExecutorNode(DataContainer<?>[] operandContainers, CacheSynchronizer synchronizer,
				int reorderedAddressOfThisInstruction, AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.synchronizer = synchronizer;
			this.operandContainers = operandContainers;
			this.returnAddress = reorderedAddressOfThisInstruction + 1;
		}

		@Override
		public final void setLaundingPointNodes(AccelerationExecutorNode ... nodes) {
			this.functionHeadNode = nodes[0];
		}

		@Override
		public final AccelerationExecutorNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();

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


	private final class ReturnExecutorNode extends AccelerationExecutorNode {
		private final DataContainer<?> returnValueContainer;
		private final CacheSynchronizer synchronizer;
		private AccelerationExecutorNode[] allNodes;

		public ReturnExecutorNode(DataContainer<?>[] operandContainers, CacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.synchronizer = synchronizer;
			this.returnValueContainer = operandContainers[0];
		}


		/**
		 * コード内の全命令に対応する、全ての演算ノードを格納する配列を指定します。
		 *
		 * 配列のインデックスは、その要素の演算ノードが対応する、
		 * （最適化による再配置済みの）命令アドレスに一致している必要があります。
		 * このメソッドで指定した演算ノードは、関数からのリターンの際に、
		 * 別の命令アドレスの処理に飛ぶために使用されます。
		 *
		 * @param nodes 全ての演算ノードを格納する配列
		 */
		@Override
		public final void setLaundingPointNodes(AccelerationExecutorNode ... nodes) {
			this.allNodes = nodes;
		}


		@Override
		public final AccelerationExecutorNode execute() {
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

			// 戻り先地点のノードを返す
			return allNodes[returnedPointAddress];
		}
	}


	private final class RefpopExecutorNode extends AccelerationExecutorNode {
		private final DataContainer<long[]>[] operandContainers;
		private final CacheSynchronizer synchronizer;

		@SuppressWarnings("unchecked")
		public RefpopExecutorNode(DataContainer<?>[] operandContainers, CacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.synchronizer = synchronizer;
			this.operandContainers = (DataContainer<long[]>[])operandContainers;
		}

		@Override
		public final void setLaundingPointNodes(AccelerationExecutorNode ... nodes) {
		}

		@SuppressWarnings("unchecked")
		@Override
		public final AccelerationExecutorNode execute() {
			--InternalFunctionControlUnit.this.dataStackPointer;
			DataContainer<?> src = InternalFunctionControlUnit.this.dataStack[ InternalFunctionControlUnit.this.dataStackPointer ];
			DataContainer<?> dest = this.operandContainers[0];
			((DataContainer<Object>)dest).setData(src.getData(), src.getLengths());

			// スタックの値をメモリに書き込んだので、そのメモリの値でキャッシュも更新しておく
			synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}



	private final class MovpopExecutorNode extends AccelerationExecutorNode {
		private final DataContainer<?>[] operandContainers;
		private final CacheSynchronizer synchronizer;

		public MovpopExecutorNode(DataContainer<?>[] operandContainers, CacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.synchronizer = synchronizer;
			this.operandContainers = operandContainers;
		}

		@Override
		public final void setLaundingPointNodes(AccelerationExecutorNode ... nodes) {
		}

		@Override
		public final AccelerationExecutorNode execute() {
			--InternalFunctionControlUnit.this.dataStackPointer;
			DataContainer<?> src = InternalFunctionControlUnit.this.dataStack[ InternalFunctionControlUnit.this.dataStackPointer ];
			DataContainer<?> dest = this.operandContainers[0];
			System.arraycopy(src.getData(), src.getOffset(), dest.getData(), dest.getOffset(), dest.getSize());

			// スタックの値をメモリに書き込んだので、そのメモリの値でキャッシュも更新しておく
			synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}


	private final class Int64ScalarMovpopExecutorNode extends AccelerationExecutorNode {
		private final DataContainer<?>[] operandContainers;
		private final CacheSynchronizer synchronizer;

		public Int64ScalarMovpopExecutorNode(DataContainer<?>[] operandContainers, CacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.synchronizer = synchronizer;
			this.operandContainers = operandContainers;
		}

		@Override
		public final void setLaundingPointNodes(AccelerationExecutorNode ... nodes) {
		}

		@Override
		public final AccelerationExecutorNode execute() {
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


	private final class Float64ScalarMovpopExecutorNode extends AccelerationExecutorNode {
		private final DataContainer<?>[] operandContainers;
		private final CacheSynchronizer synchronizer;

		public Float64ScalarMovpopExecutorNode(DataContainer<?>[] operandContainers, CacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.synchronizer = synchronizer;
			this.operandContainers = operandContainers;
		}

		@Override
		public final void setLaundingPointNodes(AccelerationExecutorNode ... nodes) {
		}

		@Override
		public final AccelerationExecutorNode execute() {
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


	private final class BoolScalarMovpopExecutorNode extends AccelerationExecutorNode {
		private final DataContainer<?>[] operandContainers;
		private final CacheSynchronizer synchronizer;

		public BoolScalarMovpopExecutorNode(DataContainer<?>[] operandContainers, CacheSynchronizer synchronizer,
				AccelerationExecutorNode nextNode) {

			super(nextNode);
			this.synchronizer = synchronizer;
			this.operandContainers = operandContainers;
		}

		@Override
		public final void setLaundingPointNodes(AccelerationExecutorNode ... nodes) {
		}

		@Override
		public final AccelerationExecutorNode execute() {
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


	private final class Int64CachedScalarMovpopExecutorNode extends AccelerationExecutorNode {
		private final Int64ScalarCache cache;

		public Int64CachedScalarMovpopExecutorNode(Int64ScalarCache cache, AccelerationExecutorNode nextNode) {
			super(nextNode);
			this.cache = cache;
		}

		@Override
		public final void setLaundingPointNodes(AccelerationExecutorNode ... nodes) {
		}

		@Override
		public final AccelerationExecutorNode execute() {
			--InternalFunctionControlUnit.this.dataStackPointer;
			@SuppressWarnings("unchecked")
			DataContainer<long[]> src = (DataContainer<long[]>)InternalFunctionControlUnit.this.dataStack[
			        InternalFunctionControlUnit.this.dataStackPointer
			];

			this.cache.value = src.getData()[ src.getOffset() ];

			return this.nextNode;
		}
	}


	private final class Float64CachedScalarMovpopExecutorNode extends AccelerationExecutorNode {
		private final Float64ScalarCache cache;

		public Float64CachedScalarMovpopExecutorNode(Float64ScalarCache cache, AccelerationExecutorNode nextNode) {
			super(nextNode);
			this.cache = cache;
		}

		@Override
		public final void setLaundingPointNodes(AccelerationExecutorNode ... nodes) {
		}

		@Override
		public final AccelerationExecutorNode execute() {
			--InternalFunctionControlUnit.this.dataStackPointer;
			@SuppressWarnings("unchecked")
			DataContainer<double[]> src = (DataContainer<double[]>)InternalFunctionControlUnit.this.dataStack[
			        InternalFunctionControlUnit.this.dataStackPointer
			];

			this.cache.value = src.getData()[ src.getOffset() ];

			return this.nextNode;
		}
	}


	private final class BoolCachedScalarMovpopExecutorNode extends AccelerationExecutorNode {
		private final BoolScalarCache cache;

		public BoolCachedScalarMovpopExecutorNode(BoolScalarCache cache, AccelerationExecutorNode nextNode) {
			super(nextNode);
			this.cache = cache;
		}

		@Override
		public final void setLaundingPointNodes(AccelerationExecutorNode ... nodes) {
		}

		@Override
		public final AccelerationExecutorNode execute() {
			--InternalFunctionControlUnit.this.dataStackPointer;
			@SuppressWarnings("unchecked")
			DataContainer<boolean[]> src = (DataContainer<boolean[]>)InternalFunctionControlUnit.this.dataStack[
			        InternalFunctionControlUnit.this.dataStackPointer
			];

			this.cache.value = src.getData()[ src.getOffset() ];

			return this.nextNode;
		}
	}

}
