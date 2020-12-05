/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.DataContainer;

public class BoolCachedScalarBranchUnit extends AcceleratorExecutionUnit {

	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		switch (instruction.getOperationCode()) {

			case JMP : {

				// 条件値が定数の場合は、常に成功か失敗のどちらか（固定）に飛ぶノードを生成
				if (operandConstant[2]) {
					boolean condition = ( (boolean[])operandContainers[2].getArrayData() )[0]; // 条件の低数値
					if (condition) {
						return new CachedScalarUnconditionalJmpNode(nextNode);
					} else {
						return new CachedScalarUnconditionalNeverJmpNode(nextNode);
					}
				// そうでない通常の場合は、スカラキャッシュから条件値を読んで飛ぶノードを生成
				} else {
					return new CachedScalarJmpNode((BoolScalarCache)operandCaches[2], nextNode);
				}
				// break; // 到達しない
			}
			case JMPN : {

				// 条件値が定数の場合は、常に成功か失敗のどちらか（固定）に飛ぶノードを生成
				if (operandConstant[2]) {
					boolean condition = ( (boolean[])operandContainers[2].getArrayData() )[0]; // 条件の低数値
					if (condition) {
						return new CachedScalarUnconditionalNeverJmpNode(nextNode);
					} else {
						return new CachedScalarUnconditionalJmpNode(nextNode);
					}
				// そうでない通常の場合は、スカラキャッシュから条件値を読んで飛ぶノードを生成
				} else {
					return new CachedScalarJmpnNode((BoolScalarCache)operandCaches[2], nextNode);
				}
				// break; // 到達しない
			}

			// 比較演算と分岐命令が1個に融合された拡張命令の場合
			case EX : {
				DataType comparisonDataType = instruction.getDataTypes()[0];              // 融合されている比較演算命令のデータ型
				OperationCode comparisonOpcode = instruction.getFusedOperationCodes()[0]; // 融合されている比較演算命令のオペコード
				OperationCode branchOpcode     = instruction.getFusedOperationCodes()[1]; // 融合されている分岐命令のオペコード

				if (branchOpcode == OperationCode.JMP) {
					if (comparisonDataType == DataType.INT64) {
						return this.generateInt64CachedScalarComparisonJmpNode(
							comparisonOpcode, (BoolScalarCache)operandCaches[0],
							(Int64ScalarCache)operandCaches[1], (Int64ScalarCache)operandCaches[2], nextNode
						);
					} else if (comparisonDataType == DataType.FLOAT64) {
						return this.generateFloat64CachedScalarComparisonJmpNode(
							comparisonOpcode, (BoolScalarCache)operandCaches[0],
							(Float64ScalarCache)operandCaches[1], (Float64ScalarCache)operandCaches[2], nextNode
						);
					} else {
						throw new VnanoFatalException("Invalid data type for fused comparison-branch operation: " + comparisonDataType);
					}

				} else if (branchOpcode == OperationCode.JMPN) {
					if (comparisonDataType == DataType.INT64) {
						return this.generateInt64CachedScalarComparisonJmpnNode(
							comparisonOpcode, (BoolScalarCache)operandCaches[0],
							(Int64ScalarCache)operandCaches[1], (Int64ScalarCache)operandCaches[2], nextNode
						);
					} else if (comparisonDataType == DataType.FLOAT64) {
						return this.generateFloat64CachedScalarComparisonJmpnNode(
							comparisonOpcode, (BoolScalarCache)operandCaches[0],
							(Float64ScalarCache)operandCaches[1], (Float64ScalarCache)operandCaches[2], nextNode
						);
					} else {
						throw new VnanoFatalException("Invalid data type for fused comparison-branch operation: " + comparisonDataType);
					}

				} else {
					throw new VnanoFatalException("Invalid fused branch operation code: " + branchOpcode);
				}
				// break; // 到達しない
			}

			default : {
				throw new VnanoFatalException(
						"Operation code " + instruction.getOperationCode() + " is invalid for " + this.getClass().getCanonicalName()
				);
			}
		}
	}


	private final class CachedScalarJmpNode extends AcceleratorExecutionNode {
		private final BoolScalarCache conditionCache;
		private AcceleratorExecutionNode branchedNode = null;

		public CachedScalarJmpNode(BoolScalarCache conditionCache, AcceleratorExecutionNode nextNode) {
			super(nextNode, 1);
			this.conditionCache = conditionCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.conditionCache.data) {
				return this.branchedNode;
			} else {
				return this.nextNode;
			}

		}
	}


	private final class CachedScalarJmpnNode extends AcceleratorExecutionNode {
		private final BoolScalarCache conditionCache;
		private AcceleratorExecutionNode branchedNode = null;

		public CachedScalarJmpnNode(BoolScalarCache conditionCache, AcceleratorExecutionNode nextNode) {
			super(nextNode, 1);

			this.conditionCache = conditionCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.conditionCache.data) {
				return this.nextNode;
			} else {
				return this.branchedNode;
			}
		}
	}


	private final class CachedScalarUnconditionalJmpNode extends AcceleratorExecutionNode {
		private AcceleratorExecutionNode branchedNode = null;

		public CachedScalarUnconditionalJmpNode(AcceleratorExecutionNode nextNode) {
			super(nextNode, 1);
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			return this.branchedNode;
		}
	}

	private final class CachedScalarUnconditionalNeverJmpNode extends AcceleratorExecutionNode {
		private AcceleratorExecutionNode branchedNode = null; // 実行時は使わないけれども setter/getter の挙動のために使う

		public CachedScalarUnconditionalNeverJmpNode(AcceleratorExecutionNode nextNode) {
			super(nextNode, 1);
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			return this.nextNode;
		}
	}





	// ================================================================================
	// Int64 Compare & JMP
	// ================================================================================

	private AcceleratorExecutionNode generateInt64CachedScalarComparisonJmpNode(
			OperationCode comparisonOpcode, BoolScalarCache comparisonDestCache,
			Int64ScalarCache comparisonLeftCache, Int64ScalarCache comparisonRightCache,
			AcceleratorExecutionNode nextNode) {

		switch (comparisonOpcode) {
			case LT : return new Int64CachedScalarLtJmpNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			case LEQ : return new Int64CachedScalarLeqJmpNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			case GT : return new Int64CachedScalarGtJmpNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			case GEQ : return new Int64CachedScalarGeqJmpNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			case EQ : return new Int64CachedScalarEqJmpNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			case NEQ : return new Int64CachedScalarNeqJmpNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			default : throw new VnanoFatalException("Unexpected comparison operation code: " + comparisonOpcode);
		}
	}


	private final class Int64CachedScalarLtJmpNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Int64ScalarCache comparisonLeftCache;
		private final Int64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Int64CachedScalarLtJmpNode(
				BoolScalarCache comparisonDestCache, Int64ScalarCache comparisonLeftCache, Int64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data < this.comparisonRightCache.data) {
				return this.branchedNode;
			} else {
				return this.nextNode;
			}
		}
	}


	private final class Int64CachedScalarLeqJmpNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Int64ScalarCache comparisonLeftCache;
		private final Int64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Int64CachedScalarLeqJmpNode(
				BoolScalarCache comparisonDestCache, Int64ScalarCache comparisonLeftCache, Int64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data <= this.comparisonRightCache.data) {
				return this.branchedNode;
			} else {
				return this.nextNode;
			}
		}
	}


	private final class Int64CachedScalarGtJmpNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Int64ScalarCache comparisonLeftCache;
		private final Int64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Int64CachedScalarGtJmpNode(
				BoolScalarCache comparisonDestCache, Int64ScalarCache comparisonLeftCache, Int64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data > this.comparisonRightCache.data) {
				return this.branchedNode;
			} else {
				return this.nextNode;
			}
		}
	}


	private final class Int64CachedScalarGeqJmpNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Int64ScalarCache comparisonLeftCache;
		private final Int64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Int64CachedScalarGeqJmpNode(
				BoolScalarCache comparisonDestCache, Int64ScalarCache comparisonLeftCache, Int64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data >= this.comparisonRightCache.data) {
				return this.branchedNode;
			} else {
				return this.nextNode;
			}
		}
	}


	private final class Int64CachedScalarEqJmpNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Int64ScalarCache comparisonLeftCache;
		private final Int64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Int64CachedScalarEqJmpNode(
				BoolScalarCache comparisonDestCache, Int64ScalarCache comparisonLeftCache, Int64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data == this.comparisonRightCache.data) {
				return this.branchedNode;
			} else {
				return this.nextNode;
			}
		}
	}


	private final class Int64CachedScalarNeqJmpNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Int64ScalarCache comparisonLeftCache;
		private final Int64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Int64CachedScalarNeqJmpNode(
				BoolScalarCache comparisonDestCache, Int64ScalarCache comparisonLeftCache, Int64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data != this.comparisonRightCache.data) {
				return this.branchedNode;
			} else {
				return this.nextNode;
			}
		}
	}



	// ================================================================================
	// Float64 Compare & JMP
	// ================================================================================

	private AcceleratorExecutionNode generateFloat64CachedScalarComparisonJmpNode(
			OperationCode comparisonOpcode, BoolScalarCache comparisonDestCache,
			Float64ScalarCache comparisonLeftCache, Float64ScalarCache comparisonRightCache,
			AcceleratorExecutionNode nextNode) {

		switch (comparisonOpcode) {
			case LT : return new Float64CachedScalarLtJmpNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			case LEQ : return new Float64CachedScalarLeqJmpNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			case GT : return new Float64CachedScalarGtJmpNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			case GEQ : return new Float64CachedScalarGeqJmpNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			case EQ : return new Float64CachedScalarEqJmpNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			case NEQ : return new Float64CachedScalarNeqJmpNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			default : throw new VnanoFatalException("Unexpected comparison operation code: " + comparisonOpcode);
		}
	}


	private final class Float64CachedScalarLtJmpNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Float64ScalarCache comparisonLeftCache;
		private final Float64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Float64CachedScalarLtJmpNode(
				BoolScalarCache comparisonDestCache, Float64ScalarCache comparisonLeftCache, Float64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data < this.comparisonRightCache.data) {
				return this.branchedNode;
			} else {
				return this.nextNode;
			}
		}
	}


	private final class Float64CachedScalarLeqJmpNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Float64ScalarCache comparisonLeftCache;
		private final Float64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Float64CachedScalarLeqJmpNode(
				BoolScalarCache comparisonDestCache, Float64ScalarCache comparisonLeftCache, Float64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data <= this.comparisonRightCache.data) {
				return this.branchedNode;
			} else {
				return this.nextNode;
			}
		}
	}


	private final class Float64CachedScalarGtJmpNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Float64ScalarCache comparisonLeftCache;
		private final Float64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Float64CachedScalarGtJmpNode(
				BoolScalarCache comparisonDestCache, Float64ScalarCache comparisonLeftCache, Float64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data > this.comparisonRightCache.data) {
				return this.branchedNode;
			} else {
				return this.nextNode;
			}
		}
	}


	private final class Float64CachedScalarGeqJmpNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Float64ScalarCache comparisonLeftCache;
		private final Float64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Float64CachedScalarGeqJmpNode(
				BoolScalarCache comparisonDestCache, Float64ScalarCache comparisonLeftCache, Float64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data >= this.comparisonRightCache.data) {
				return this.branchedNode;
			} else {
				return this.nextNode;
			}
		}
	}


	private final class Float64CachedScalarEqJmpNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Float64ScalarCache comparisonLeftCache;
		private final Float64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Float64CachedScalarEqJmpNode(
				BoolScalarCache comparisonDestCache, Float64ScalarCache comparisonLeftCache, Float64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data == this.comparisonRightCache.data) {
				return this.branchedNode;
			} else {
				return this.nextNode;
			}
		}
	}


	private final class Float64CachedScalarNeqJmpNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Float64ScalarCache comparisonLeftCache;
		private final Float64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Float64CachedScalarNeqJmpNode(
				BoolScalarCache comparisonDestCache, Float64ScalarCache comparisonLeftCache, Float64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data != this.comparisonRightCache.data) {
				return this.branchedNode;
			} else {
				return this.nextNode;
			}
		}
	}



	// ================================================================================
	// Int64 Compare & JMPN
	// ================================================================================

	private AcceleratorExecutionNode generateInt64CachedScalarComparisonJmpnNode(
			OperationCode comparisonOpcode, BoolScalarCache comparisonDestCache,
			Int64ScalarCache comparisonLeftCache, Int64ScalarCache comparisonRightCache,
			AcceleratorExecutionNode nextNode) {

		switch (comparisonOpcode) {
			case LT : return new Int64CachedScalarLtJmpnNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			case LEQ : return new Int64CachedScalarLeqJmpnNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			case GT : return new Int64CachedScalarGtJmpnNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			case GEQ : return new Int64CachedScalarGeqJmpnNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			case EQ : return new Int64CachedScalarEqJmpnNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			case NEQ : return new Int64CachedScalarNeqJmpnNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			default : throw new VnanoFatalException("Unexpected comparison operation code: " + comparisonOpcode);
		}
	}


	private final class Int64CachedScalarLtJmpnNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Int64ScalarCache comparisonLeftCache;
		private final Int64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Int64CachedScalarLtJmpnNode(
				BoolScalarCache comparisonDestCache, Int64ScalarCache comparisonLeftCache, Int64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data < this.comparisonRightCache.data) {
				return this.nextNode;

			// JMPN は条件が成り立たない場合に飛ぶ
			} else {
				return this.branchedNode;
			}
		}
	}


	private final class Int64CachedScalarLeqJmpnNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Int64ScalarCache comparisonLeftCache;
		private final Int64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Int64CachedScalarLeqJmpnNode(
				BoolScalarCache comparisonDestCache, Int64ScalarCache comparisonLeftCache, Int64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data <= this.comparisonRightCache.data) {
				return this.nextNode;

			// JMPN は条件が成り立たない場合に飛ぶ
			} else {
				return this.branchedNode;
			}
		}
	}


	private final class Int64CachedScalarGtJmpnNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Int64ScalarCache comparisonLeftCache;
		private final Int64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Int64CachedScalarGtJmpnNode(
				BoolScalarCache comparisonDestCache, Int64ScalarCache comparisonLeftCache, Int64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data > this.comparisonRightCache.data) {
				return this.nextNode;

			// JMPN は条件が成り立たない場合に飛ぶ
			} else {
				return this.branchedNode;
			}
		}
	}


	private final class Int64CachedScalarGeqJmpnNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Int64ScalarCache comparisonLeftCache;
		private final Int64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Int64CachedScalarGeqJmpnNode(
				BoolScalarCache comparisonDestCache, Int64ScalarCache comparisonLeftCache, Int64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data >= this.comparisonRightCache.data) {
				return this.nextNode;

			// JMPN は条件が成り立たない場合に飛ぶ
			} else {
				return this.branchedNode;
			}
		}
	}


	private final class Int64CachedScalarEqJmpnNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Int64ScalarCache comparisonLeftCache;
		private final Int64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Int64CachedScalarEqJmpnNode(
				BoolScalarCache comparisonDestCache, Int64ScalarCache comparisonLeftCache, Int64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data == this.comparisonRightCache.data) {
				return this.nextNode;

			// JMPN は条件が成り立たない場合に飛ぶ
			} else {
				return this.branchedNode;
			}
		}
	}


	private final class Int64CachedScalarNeqJmpnNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Int64ScalarCache comparisonLeftCache;
		private final Int64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Int64CachedScalarNeqJmpnNode(
				BoolScalarCache comparisonDestCache, Int64ScalarCache comparisonLeftCache, Int64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data != this.comparisonRightCache.data) {
				return this.nextNode;

			// JMPN は条件が成り立たない場合に飛ぶ
			} else {
				return this.branchedNode;
			}
		}
	}



	// ================================================================================
	// Float64 Compare & JMPN
	// ================================================================================

	private AcceleratorExecutionNode generateFloat64CachedScalarComparisonJmpnNode(
			OperationCode comparisonOpcode, BoolScalarCache comparisonDestCache,
			Float64ScalarCache comparisonLeftCache, Float64ScalarCache comparisonRightCache,
			AcceleratorExecutionNode nextNode) {

		switch (comparisonOpcode) {
			case LT : return new Float64CachedScalarLtJmpnNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			case LEQ : return new Float64CachedScalarLeqJmpnNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			case GT : return new Float64CachedScalarGtJmpnNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			case GEQ : return new Float64CachedScalarGeqJmpnNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			case EQ : return new Float64CachedScalarEqJmpnNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			case NEQ : return new Float64CachedScalarNeqJmpnNode(comparisonDestCache, comparisonLeftCache, comparisonRightCache, nextNode);
			default : throw new VnanoFatalException("Unexpected comparison operation code: " + comparisonOpcode);
		}
	}


	private final class Float64CachedScalarLtJmpnNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Float64ScalarCache comparisonLeftCache;
		private final Float64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Float64CachedScalarLtJmpnNode(
				BoolScalarCache comparisonDestCache, Float64ScalarCache comparisonLeftCache, Float64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data < this.comparisonRightCache.data) {
				return this.nextNode;

			// JMPN は条件が成り立たない場合に飛ぶ
			} else {
				return this.branchedNode;
			}
		}
	}


	private final class Float64CachedScalarLeqJmpnNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Float64ScalarCache comparisonLeftCache;
		private final Float64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Float64CachedScalarLeqJmpnNode(
				BoolScalarCache comparisonDestCache, Float64ScalarCache comparisonLeftCache, Float64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data <= this.comparisonRightCache.data) {
				return this.nextNode;

			// JMPN は条件が成り立たない場合に飛ぶ
			} else {
				return this.branchedNode;
			}
		}
	}


	private final class Float64CachedScalarGtJmpnNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Float64ScalarCache comparisonLeftCache;
		private final Float64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Float64CachedScalarGtJmpnNode(
				BoolScalarCache comparisonDestCache, Float64ScalarCache comparisonLeftCache, Float64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data > this.comparisonRightCache.data) {
				return this.nextNode;

			// JMPN は条件が成り立たない場合に飛ぶ
			} else {
				return this.branchedNode;
			}
		}
	}


	private final class Float64CachedScalarGeqJmpnNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Float64ScalarCache comparisonLeftCache;
		private final Float64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Float64CachedScalarGeqJmpnNode(
				BoolScalarCache comparisonDestCache, Float64ScalarCache comparisonLeftCache, Float64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data >= this.comparisonRightCache.data) {
				return this.nextNode;

			// JMPN は条件が成り立たない場合に飛ぶ
			} else {
				return this.branchedNode;
			}
		}
	}


	private final class Float64CachedScalarEqJmpnNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Float64ScalarCache comparisonLeftCache;
		private final Float64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Float64CachedScalarEqJmpnNode(
				BoolScalarCache comparisonDestCache, Float64ScalarCache comparisonLeftCache, Float64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data == this.comparisonRightCache.data) {
				return this.nextNode;

			// JMPN は条件が成り立たない場合に飛ぶ
			} else {
				return this.branchedNode;
			}
		}
	}


	private final class Float64CachedScalarNeqJmpnNode extends AcceleratorExecutionNode {
		private final BoolScalarCache comparisonDestCache;
		private final Float64ScalarCache comparisonLeftCache;
		private final Float64ScalarCache comparisonRightCache;
		private AcceleratorExecutionNode branchedNode = null;

		public Float64CachedScalarNeqJmpnNode(
				BoolScalarCache comparisonDestCache, Float64ScalarCache comparisonLeftCache, Float64ScalarCache comparisonRightCache,
				AcceleratorExecutionNode nextNode) {

			super(nextNode, 2);
			this.comparisonDestCache = comparisonDestCache;
			this.comparisonLeftCache = comparisonLeftCache;
			this.comparisonRightCache = comparisonRightCache;
		}

		@Override
		public void setLaundingPointNodes(AcceleratorExecutionNode ... branchedNode) {
			this.branchedNode = branchedNode[0];
		}

		@Override
		public AcceleratorExecutionNode[] getLaundingPointNodes() {
			return new AcceleratorExecutionNode[] { this.branchedNode };
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			if (this.comparisonDestCache.data = this.comparisonLeftCache.data != this.comparisonRightCache.data) {
				return this.nextNode;

			// JMPN は条件が成り立たない場合に飛ぶ
			} else {
				return this.branchedNode;
			}
		}
	}


}
