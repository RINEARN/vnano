package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.processor.Instruction;
import org.vcssl.nano.vm.processor.Processor;

public class UnacceleratedUnit extends AcceleratorExecutionUnit {

	Processor processor = null;
	Memory memory = null;
	Interconnect interconnect = null;
	CacheSynchronizer synchronizer = null;

	public UnacceleratedUnit(Processor processor, Memory memory, Interconnect interconnect, CacheSynchronizer synchronizer) {
		this.processor = processor;
		this.memory = memory;
		this.interconnect = interconnect;
		this.synchronizer = synchronizer;
	}

	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		return new PassThroughExecutorNode(
			instruction, this.memory, this.interconnect, this.processor, this.synchronizer, nextNode
		);

	}

	private final class PassThroughExecutorNode extends AcceleratorExecutionNode {
		private final Instruction instruction;
		private final Interconnect interconnect;
		private final Processor processor;
		private final Memory memory;
		private final CacheSynchronizer synchronizer;

		public PassThroughExecutorNode(Instruction instruction, Memory memory, Interconnect interconnect,
				Processor processor, CacheSynchronizer synchronizer,
				AcceleratorExecutionNode nextNode) {

			super(nextNode);
			this.instruction = instruction;
			this.interconnect = interconnect;
			this.processor = processor;
			this.memory = memory;
			this.synchronizer = synchronizer;
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			try {

				this.synchronizer.synchronizeFromCacheToMemory();
				int programCounter = 0; // 暫定的なダミー値
				this.processor.process(this.instruction, this.memory, this.interconnect, programCounter);
				this.synchronizer.synchronizeFromMemoryToCache();

				// スカラ変数のALLOCの場合は
				if (instruction.getOperationCode() == OperationCode.ALLOC) {

				}

				return this.nextNode;

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

}
