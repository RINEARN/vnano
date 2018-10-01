/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.accelerator;

import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.memory.DataContainer;
import org.vcssl.nano.processor.OperationCode;

public class BoolCachedScalarLogicalUnit extends AccelerationUnit {

	@Override
	public AccelerationExecutorNode generateExecutor(
			OperationCode opcode, DataType[] dataTypes, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCached, boolean[] operandScalar, boolean[] operandConstant,
			AccelerationExecutorNode nextNode) {

		BoolCachedScalarLogicalExecutor executor = null;
		switch (opcode) {
			case AND : {
				executor = new BoolCachedScalarAndExecutor(
						(BoolCache)operandCaches[0],
						(BoolCache)operandCaches[1],
						(BoolCache)operandCaches[2],
						nextNode);
				break;
			}
			case OR : {
				executor = new BoolCachedScalarOrExecutor(
						(BoolCache)operandCaches[0],
						(BoolCache)operandCaches[1],
						(BoolCache)operandCaches[2],
						nextNode);
				break;
			}
			case NOT : {
				executor = new BoolCachedScalarNotExecutor(
						(BoolCache)operandCaches[0],
						(BoolCache)operandCaches[1],
						nextNode);
				break;
			}
			default : {
				break;
			}
		}
		return executor;
	}

	private abstract class BoolCachedScalarLogicalExecutor extends AccelerationExecutorNode {
		protected final BoolCache cache0;
		protected final BoolCache cache1;
		protected final BoolCache cache2;

		public BoolCachedScalarLogicalExecutor(BoolCache cache0, BoolCache cache1, BoolCache cache2, AccelerationExecutorNode nextNode) {
			super(nextNode);
			this.cache0 = cache0;
			this.cache1 = cache1;
			this.cache2 = cache2;
		}

		public BoolCachedScalarLogicalExecutor(BoolCache cache0, BoolCache cache1, AccelerationExecutorNode nextNode) {
			super(nextNode);
			this.cache0 = cache0;
			this.cache1 = cache1;
			this.cache2 = null;
		}
	}

	private final class BoolCachedScalarAndExecutor extends BoolCachedScalarLogicalExecutor {
		public BoolCachedScalarAndExecutor(BoolCache cache0, BoolCache cache1, BoolCache cache2, AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value & this.cache2.value;
			return this.nextNode;
		}
	}

	private final class BoolCachedScalarOrExecutor extends BoolCachedScalarLogicalExecutor {
		public BoolCachedScalarOrExecutor(BoolCache cache0, BoolCache cache1, BoolCache cache2, AccelerationExecutorNode nextNode) {
			super(cache0, cache1, cache2, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = this.cache1.value | this.cache2.value;
			return this.nextNode;
		}
	}

	private final class BoolCachedScalarNotExecutor extends BoolCachedScalarLogicalExecutor {
		public BoolCachedScalarNotExecutor(BoolCache cache0, BoolCache cache1, AccelerationExecutorNode nextNode) {
			super(cache0, cache1, nextNode);
		}
		public final AccelerationExecutorNode execute() {
			this.cache0.value = !this.cache1.value;
			return this.nextNode;
		}
	}
}
