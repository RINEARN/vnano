package org.vcssl.nano.vm.accelerator;

import java.util.Arrays;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.processor.Instruction;
import org.vcssl.nano.vm.processor.Processor;

public class BypassUnit extends AcceleratorExecutionUnit {

	Processor processor = null;
	Memory memory = null;
	Interconnect interconnect = null;

	public BypassUnit(Processor processor, Memory memory, Interconnect interconnect) {
		this.processor = processor;
		this.memory = memory;
		this.interconnect = interconnect;
	}

	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		int operandLength = operandContainers.length;

		OperationCode operationCode = instruction.getOperationCode();
		switch (operationCode) {

			// ALLOC命令（メモリ確保）
			case ALLOC : {

				// ALLOC命令実行前は先頭オペランドは未確保なので、キャッシュのSynchronizerは先頭の同期を無効化して用いる
				// （先頭オペランドはキャッシュからメモリ方向は同期されなくなるが、どうせALLOCで初期化するので問題ない）
				boolean[] cacheSyncEnabled = new boolean[operandLength];
				Arrays.fill(cacheSyncEnabled, true);
				cacheSyncEnabled[0] = false;
				CacheSynchronizer preSynchronizer = new GeneralScalarCacheSynchronizer(
						operandContainers, operandCaches, cacheSyncEnabled
				);

				// 命令実行後は全オペランドを対象とするキャッシュSynchronizerを用いる
				CacheSynchronizer postSynchronizer = new GeneralScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);

				return new ProcessorCallNode(
					instruction, this.memory, this.interconnect, this.processor, preSynchronizer, postSynchronizer, nextNode
				);
			}

			// ALLOC以外の命令
			default : {
				CacheSynchronizer synchronizer = new GeneralScalarCacheSynchronizer(operandContainers, operandCaches, operandCached);
				return new ProcessorCallNode(
					instruction, this.memory, this.interconnect, this.processor, synchronizer, synchronizer, nextNode
				);
			}
		}

	}

	private final class ProcessorCallNode extends AcceleratorExecutionNode {
		private final Instruction instruction;
		private final Interconnect interconnect;
		private final Processor processor;
		private final Memory memory;
		private final CacheSynchronizer preSynchronizer;
		private final CacheSynchronizer postSynchronizer;

		public ProcessorCallNode(Instruction instruction, Memory memory, Interconnect interconnect, Processor processor,
				CacheSynchronizer preSynchronizer, CacheSynchronizer postSynchronizer, AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.instruction = instruction;
			this.interconnect = interconnect;
			this.processor = processor;
			this.memory = memory;
			this.preSynchronizer = preSynchronizer;
			this.postSynchronizer = postSynchronizer;
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			try {
				this.preSynchronizer.synchronizeFromCacheToMemory();

				int programCounter = 0; // 暫定的なダミー値
				this.processor.process(this.instruction, this.memory, this.interconnect, programCounter);
				this.postSynchronizer.synchronizeFromMemoryToCache();

				return this.nextNode;

			} catch (Exception e) {
				throw new VnanoFatalException(e);
			}
		}
	}


	// （暫定案）ExternalFunctionCallNode を追加する場合はここ

}
