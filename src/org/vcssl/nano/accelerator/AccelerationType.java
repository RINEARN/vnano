package org.vcssl.nano.accelerator;

public enum AccelerationType {

	Unsupported,

	Int64VectorArithmetic,
	Int64ScalarArithmetic,
	Int64CachedScalarArithmetic,
	Float64VectorArithmetic,
	Float64ScalarArithmetic,
	Float64CachedScalarArithmetic,

	Int64VectorComparison,
	Int64ScalarComparison,
	Int64CachedScalarComparison,
	Float64VectorComparison,
	Float64ScalarComparison,
	Float64CachedScalarComparison,

	Int64VectorTransfer,
	Int64ScalarTransfer,
	Int64CachedScalarTransfer,
	Float64VectorTransfer,
	Float64ScalarTransfer,
	Float64CachedScalarTransfer,
	BoolVectorTransfer,
	BoolScalarTransfer,
	BoolCachedScalarTransfer,

	BoolVectorLogical,
	BoolScalarLogical,
	BoolCachedScalarLogical,

	BoolScalarBranch,
	BoolCachedScalarBranch,

	ScalarAlloc,
	Nop,

}
