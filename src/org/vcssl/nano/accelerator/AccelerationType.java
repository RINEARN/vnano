package org.vcssl.nano.accelerator;

public enum AccelerationType {

	Unsupported,

	ScalarAlloc,
	Nop,

	BoolVectorBranch,
	BoolVectorLogical,
	BoolVectorTransfer,

	BoolScalarBranch,
	BoolScalarLogical,
	BoolScalarTransfer,

	BoolCachedScalarBranch,
	BoolCachedScalarLogical,
	BoolCachedScalarTransfer,



	Int64VectorArithmetic,
	Int64VectorComparison,
	Int64VectorTransfer,

	Int64ScalarArithmetic,
	Int64ScalarComparison,
	Int64ScalarTransfer,

	Int64CachedScalarArithmetic,
	Int64CachedScalarComparison,
	Int64CachedScalarTransfer,



	Float64VectorArithmetic,
	Float64VectorComparison,
	Float64VectorTransfer,

	Float64ScalarArithmetic,
	Float64ScalarComparison,
	Float64ScalarTransfer,

	Float64CachedScalarArithmetic,
	Float64CachedScalarComparison,
	Float64CachedScalarTransfer,


}
