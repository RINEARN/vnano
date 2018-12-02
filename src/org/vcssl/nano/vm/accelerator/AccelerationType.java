package org.vcssl.nano.vm.accelerator;

public enum AccelerationType {

	Unsupported,

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
	F64V_TRANSFER,  // Float64VectorTransfer
	F64S_TRANSFER,  // Float64ScalarTransfer
	F64CS_TRANSFER, // Float64CachedScalarTransfer
	BV_TRANSFER,    // BoolVectorTransfer
	BS_TRANSFER,    // BoolScalarTransfer
	BCS_TRANSFER,   // BoolCachedScalarTransfer

	BV_LOGICAL,  // BoolVectorLogical
	BS_LOGICAL,  // BoolScalarLogical
	BCS_LOGICAL, // BoolCachedScalarLogical

	BS_BRANCH,   // BoolScalarBranch
	BCS_BRANCH,  // BoolCachedScalarBranch

	S_ALLOC, // ScalarAlloc
	NOP, // Nop
}
