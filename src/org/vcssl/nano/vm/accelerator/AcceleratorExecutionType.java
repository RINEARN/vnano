package org.vcssl.nano.vm.accelerator;

public enum AcceleratorExecutionType {

	I64V_ARITHMETIC,    // Int64VectorArithmetic
	I64S_ARITHMETIC,    // Int64ScalarArithmetic
	I64CS_ARITHMETIC,   // Int64CachedScalarArithmetic
	F64V_ARITHMETIC,    // Float64VectorArithmetic
	F64S_ARITHMETIC,    // Float64ScalarArithmetic
	F64CS_ARITHMETIC,   // Float64CachedScalarArithmetic

	I64V_DUAL_ARITHMETIC, // Int64VectorDualFusedArithmetic
	F64V_DUAL_ARITHMETIC, // Float64VectorDualFusedArithmetic

	I64CS_DUAL_ARITHMETIC, // Int64CachedScalarDualFusedArithmetic
	F64CS_DUAL_ARITHMETIC, // Float64CachedScalarDualFusedArithmetic

	I64V_COMPARISON,    // Int64VectorComparison
	I64S_COMPARISON,    // Int64ScalarComparison
	I64CS_COMPARISON,   // Int64CachedScalarComparison
	F64V_COMPARISON,    // Float64VectorComparison
	F64S_COMPARISON,    // Float64ScalarComparison
	F64CS_COMPARISON,   // Float64CachedScalarComparison

	I64V_TRANSFER,  // Int64VectorTransfer
	I64S_TRANSFER,  // Int64ScalarTransfer
	I64CS_TRANSFER, // Int64CachedScalarTransfer
	I64VS_TRANSFER, // Int64VectorFromScalarTransfer
	F64V_TRANSFER,  // Float64VectorTransfer
	F64S_TRANSFER,  // Float64ScalarTransfer
	F64CS_TRANSFER, // Float64CachedScalarTransfer
	F64VS_TRANSFER, // Float64VectorFromScalarTransfer
	BV_TRANSFER,    // BoolVectorTransfer
	BS_TRANSFER,    // BoolScalarTransfer
	BCS_TRANSFER,   // BoolCachedScalarTransfer
	BVS_TRANSFER,   // BoolVectorFromScalarTransfer

	BV_LOGICAL,  // BoolVectorLogical
	BS_LOGICAL,  // BoolScalarLogical
	BCS_LOGICAL, // BoolCachedScalarLogical

	BV_BRANCH,   // BoolVectorBranch
	BS_BRANCH,   // BoolScalarBranch
	BCS_BRANCH,  // BoolCachedScalarBranch

	// スカラのALLOC命令は、スケジューリングでコード先頭に移動させて最初に行うようにしたため、複数回実行のための高速化はもう不要？
	//SCALAR_ALLOC, // ScalarAlloc

	FUNCTION_CONTROL,  // FunctionControl
	NOP, // Nop

	BYPASS,
}
