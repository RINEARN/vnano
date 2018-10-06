/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.memory.DataContainer;

public class BoolCachedScalarLogicalUnit extends AccelerationUnit {

	@Override
	public AccelerationExecutorNode generateExecutorNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		BoolCachedScalarLogicalExecutorNode executor = null;
		switch (instruction.getOperationCode()) {
			case AND : {
				executor = new BoolCachedScalarAndExecutorNode(
						(BoolScalarCache)operandCaches[0],
						(BoolScalarCache)operandCaches[1],
						(BoolScalarCache)operandCaches[2],
						nextNode);
				break;
			}
			case OR : {
				executor = new BoolCachedScalarOrExecutorNode(
						(BoolScalarCache)operandCaches[0],
						(BoolScalarCache)operandCaches[1],
						(BoolScalarCache)operandCaches[2],
						nextNode);
				break;
			}
			case NOT : {
				executor = new BoolCachedScalarNotExecutorNode(
						(BoolScalarCache)operandCaches[0],
						(BoolScalarCache)operandCaches[1],
						nextNode);
				break;
			}
			default : {
				throw new VnanoFatalException(
						"Operation code " + instruction.getOperationCode() + " is invalid for " + this.getClass().getCanonicalName()
				);
			}
		}
		return executor;
	}

	private abstract class BoolCachedScalarLogicalExecutorNode extends AccelerationExecutorNode {
		protected final BoolScalarCache cache0;
		protected final BoolScalarCache cache1;
		protected final BoolScalarCache cache2;

		public BoolCachedScalarLogicalExecutorNode(BoolScalarCache cache0, BoolScalarCache cache1, BoolScalarCache cache2, AccelerationExecutorNode nextNode) {
			super(nextNode);
			this.cache0 = cache0;
			this.cache1 = cache1;
			this.cache2 = cache2;
		}

		public BoolCachedScalarLogicalExecutorNode(BoolScalarCache cache0, BoolScalarCache cache1, AccelerationExecutorNode nextNode) {
			super(nextNode);
			this.cache0 = cache0;
			this.cache1 = cache1;
			this.cache2 = null;
		}
	}

	private final class BoolCachedScalarAndExecutorNode extends BoolCachedScalarLogicalExecutorNode {
		public BoolCachedScalarAndExecutorNode(BoolScalarCache cache0, BoolScalarCache cache1, BoolScalarCache cache2, AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value & this.cache2.value;
			return this.nextNode;
		}
	}

	private final class BoolCachedScalarOrExecutorNode extends BoolCachedScalarLogicalExecutorNode {
		public BoolCachedScalarOrExecutorNode(BoolScalarCache cache0, BoolScalarCache cache1, BoolScalarCache cache2, AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value | this.cache2.value;
			return this.nextNode;
		}
	}

	private final class BoolCachedScalarNotExecutorNode extends BoolCachedScalarLogicalExecutorNode {
		public BoolCachedScalarNotExecutorNode(BoolScalarCache cache0, BoolScalarCache cache1, AccelerationExecutorNode nextNode) {
			super(cache0, cache1, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = !this.cache1.value;
			return this.nextNode;
		}
	}
}
