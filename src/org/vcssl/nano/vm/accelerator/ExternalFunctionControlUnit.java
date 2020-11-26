/*
 * Copyright(C) 2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import org.vcssl.connect.ConnectorException;
import org.vcssl.connect.ExternalFunctionConnectorInterface1;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.interconnect.AbstractFunction;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.interconnect.Xfci1ToFunctionAdapter;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.processor.ExecutionUnit;

public class ExternalFunctionControlUnit extends AcceleratorExecutionUnit {

	private Interconnect interconnect;

	public ExternalFunctionControlUnit(Interconnect interconnect) {
		this.interconnect = interconnect;
	}

	@Override
	public AcceleratorExecutionNode generateNode(
			AcceleratorInstruction instruction, DataContainer<?>[] operandContainers,
			Object[] operandCaches, boolean[] operandCachingEnabled, boolean[] operandScalar, boolean[] operandConstant,
			AcceleratorExecutionNode nextNode) {

		if (instruction.getOperationCode() != OperationCode.CALLX) {
			throw new VnanoFatalException(
				"Operation code " + instruction.getOperationCode() + " is invalid for " + this.getClass().getCanonicalName()
			);
		}

		int calleeFunctionIndex = (int)( (long[])operandContainers[1].getArrayData() )[0];
		AbstractFunction calleeFunction = this.interconnect.getExternalFunctionTable().getFunctionByIndex(calleeFunctionIndex);

		// 外部関数のインターフェースがXFCI1形式かどうか判定
		boolean isXfci1 = calleeFunction instanceof Xfci1ToFunctionAdapter;

		// XFCI1形式の場合は、呼び出しパターンに応じて最適化したノードを生成
		if (isXfci1) {
			ExternalFunctionConnectorInterface1 xfci1CalleeFunction = ((Xfci1ToFunctionAdapter)calleeFunction).getXfci1Plugin();
			return this.generateXfci1CallxNode(
				instruction, calleeFunction, xfci1CalleeFunction,
				operandContainers, operandCaches, operandCachingEnabled, operandScalar, operandConstant, nextNode
			);

		// それ以外の一般の場合には、どのようなパターンでも対応可能なノードを生成する。
		} else {
			CacheSynchronizer synchronizer = new GeneralScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled);
			boolean isReturnValueCachedScalar = operandCachingEnabled[0] && operandScalar[0];
			DataType returnValueDataType = instruction.getDataTypes()[0];
			return new GeneralCallxNode(
				operandContainers, synchronizer, returnValueDataType, isReturnValueCachedScalar, calleeFunction, nextNode
			);
		}
	}


	private AcceleratorExecutionNode generateXfci1CallxNode(
			AcceleratorInstruction instruction,
			AbstractFunction calleeFunction, ExternalFunctionConnectorInterface1 calleeXfci1Function,
			DataContainer<?>[] operandContainers, Object[] operandCaches, boolean[] operandCachingEnabled,
			boolean[] operandScalar, boolean[] operandConstant, AcceleratorExecutionNode nextNode) {

		// オペランドのキャッシュ可能性やスカラかどうかを判定
		boolean areAllOperandsCachedScalar = true;
		for (int i=0; i<operandContainers.length; i++) {
			if (!operandCachingEnabled[i] || !operandScalar[i]) {
				areAllOperandsCachedScalar = false;
				break;
			}
		}

		// 戻り値のキャッシュ可能性やスカラかどうか、およびデータ型を判定
		boolean isReturnValueCachedScalar = operandCachingEnabled[0] && operandScalar[0];
		DataType returnValueDataType = instruction.getDataTypes()[0];

		// 自動データ変換を行うよう設定されているかどうか判定
		boolean isDataConversionNecessary = calleeXfci1Function.isDataConversionNecessary();

		// 自動データ変換が無効化されている場合は、キャッシュオブジェクトを直接渡せるか判定
		boolean areAllCachesPassable = true;
		if (!isDataConversionNecessary && areAllOperandsCachedScalar) {

			// 実引数のデータ型クラスを格納する配列を用意（外部関数の戻り値仕様は引数仕様に依存するため）
			int argLength = operandContainers.length - 2; //[0]は戻り値、[1]は関数アドレス、[2]以降が引数
			Class<?>[] argClasses = new Class<?>[argLength];
			for (int i=0; i<argLength; i++) {
				if (operandCaches[i+2] instanceof Float64ScalarCache) {
					argClasses[i] = double.class;
				} else if (operandCaches[i+2] instanceof Int64ScalarCache) {
					argClasses[i] = long.class;
				} else if (operandCaches[i+2] instanceof BoolScalarCache) {
					argClasses[i] = boolean.class;
				} else {
					throw new VnanoFatalException("Unexpected Cache Object: " + operandCaches[i+2].getClass().getCanonicalName());
				}
			}

			// プラグインが対応しているデータ入出力インターフェースを取得
			Class<?>[] paramDataAccessorInterfaces = calleeXfci1Function.getParameterUnconvertedClasses();
			Class<?> returnDataAccessorInterface = calleeXfci1Function.getReturnUnconvertedClass(argClasses);

			// 上記インターフェースに基づいて、引数にキャッシュをそのまま渡せるかどうか検査していく
			// (キャッシュオブジェクトは Float64ScalarDataAccessorInterface などのスカラデータ入出力インターフェースを実装している)
			for (int i=0; i<argLength; i++) {
				if (!paramDataAccessorInterfaces[i].isAssignableFrom(operandCaches[i+2].getClass())) {
					areAllCachesPassable = false;
					break;
				}
			}

			// 同様に戻り値についてもキャッシュをそのまま渡せるか検査する
			if (!returnDataAccessorInterface.isAssignableFrom(operandCaches[0].getClass())) {
				areAllCachesPassable = false;
			}

		// 自動データ変換が有効化されている場合や、そもそもオペランドがキャッシュ可能なスカラではない場合
		} else {
			areAllCachesPassable = false;
		}

		// 全オペランドが参照渡しかどうか判定
		boolean shouldPassAllArgsByReference = true;
		boolean[] paramReferencenesses = calleeXfci1Function.getParameterReferencenesses();
		for (int i=0; i<paramReferencenesses.length; i++) {
			shouldPassAllArgsByReference &= paramReferencenesses[i]; // 右辺が一回でも false なら左辺はその後常に false になる
		}

		// XFCI1形式の外部関数プラグインでは、自動データ変換が無効化されている場合、処理系のデータコンテナを直接渡せる。
		// さらに全オペランドがキャッシュ可能なスカラな場合、プラグイン側が対応しているデータ入出力インターフェース次第では、
		// キャッシュオブジェクトを直接渡せるため、メモリ-キャッシュ間の同期も省略でき、呼び出しオーバーヘッドを大きく削れる。
		// 従って、上記のパターンに該当する場合は、専用に最適化したノードを生成する。
		if (!isDataConversionNecessary && areAllOperandsCachedScalar && areAllCachesPassable) {

			// 全引数が参照渡しの場合（キャッシュをそのまま渡せるのでオーバーヘッド最小）
			if (shouldPassAllArgsByReference) {
				return new CachedScalarReferenceXfci1CallxNode(
					operandCaches, operandContainers[0], returnValueDataType, isReturnValueCachedScalar, calleeXfci1Function, nextNode
				);

			// 値渡しの引数を含む場合（キャッシュをコピーするオーバーヘッドが加わる）
			} else {
				return new CachedScalarXfci1CallxNode(
					operandCaches, operandContainers[0], returnValueDataType, isReturnValueCachedScalar, calleeXfci1Function, nextNode
				);
			}

		// それ以外の一般の場合には、どのようなパターンでも対応可能なノードを生成する。
		} else {
			CacheSynchronizer synchronizer = new GeneralScalarCacheSynchronizer(operandContainers, operandCaches, operandCachingEnabled);
			return new GeneralCallxNode(
				operandContainers, synchronizer, returnValueDataType, isReturnValueCachedScalar, calleeFunction, nextNode
			);
		}
	}


	// 全オペランドがキャッシュ可能スカラで、かつ参照渡しの場合の、XFCI1外部関数呼び出しノード
	private final class CachedScalarReferenceXfci1CallxNode extends AcceleratorExecutionNode {
		private final ScalarCache[] xfci1ArgCaches; // XFCI1 の invoke に渡す引数
		private ExternalFunctionConnectorInterface1 function;

		public CachedScalarReferenceXfci1CallxNode(Object[] operandCaches,
				DataContainer<?> returnValueContainer, DataType returnValueDataType, boolean isReturnValueCachedScalar,
				ExternalFunctionConnectorInterface1 function, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.function = function;

			// XFCI1 の invoke に渡すキャッシュ配列を用意
			int operandLength = operandCaches.length;
			int xfci1ArgLength = operandLength - 1; // オペランド[1] は関数アドレスなのでXFCI1のinvokeに渡す必要はなく、従って -1
			this.xfci1ArgCaches = new ScalarCache[ operandLength - 1 ];
			this.xfci1ArgCaches[0] = (ScalarCache)operandCaches[0]; // 戻り値格納用
			for (int i=1; i<xfci1ArgLength; i++) {
				this.xfci1ArgCaches[i] = (ScalarCache)operandCaches[i + 1]; // 引数格納用
			}

			// 戻り値格納用のデータコンテナは、外部関数側でメモリ確保される前提で、VRILコード上では alloc されない場合が普通にある。
			// しかしこのノードではデータコンテナの代わりにキャッシュオブジェクトが外部関数に渡されるので、
			// そこでもデータコンテナはメモリ確保されない。そのため、ここでスカラを格納できる形に確保しておく。
			// それを行わないと、accelerator 内のどこかで、戻り値格納アドレスに対してキャッシュ同期しようとした際に失敗する。
			if (isReturnValueCachedScalar && returnValueDataType != DataType.VOID) {
				new ExecutionUnit().alloc(
					returnValueDataType, returnValueContainer, DataContainer.ARRAY_SIZE_OF_SCALAR, DataContainer.ARRAY_LENGTHS_OF_SCALAR
				);
			}
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			try {
				this.function.invoke(this.xfci1ArgCaches);
			} catch (ConnectorException e) {
				// 上層で拾うので実行時例外として上に投げる
				throw new RuntimeException(e);
			}
			return this.nextNode;
		}
	}

	// 全オペランドがキャッシュ可能スカラで、値渡しの引数を含む場合の、XFCI1外部関数呼び出しノード
	private final class CachedScalarXfci1CallxNode extends AcceleratorExecutionNode {
		private final ScalarCache[] xfci1ArgCaches; // XFCI1 の invoke に渡す引数
		private boolean[] xfci1ArgShouldCopied;
		private ExternalFunctionConnectorInterface1 function;

		public CachedScalarXfci1CallxNode(Object[] operandCaches,
				DataContainer<?> returnValueContainer, DataType returnValueDataType, boolean isReturnValueCachedScalar,
				ExternalFunctionConnectorInterface1 function, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.function = function;

			// XFCI1 の invoke に渡すキャッシュ配列を用意
			int operandLength = operandCaches.length;
			int xfci1ArgLength = operandLength - 1; // オペランド[1] は関数アドレスなのでXFCI1のinvokeに渡す必要はなく、従って -1
			this.xfci1ArgCaches = new ScalarCache[ operandLength - 1 ];
			this.xfci1ArgCaches[0] = (ScalarCache)operandCaches[0]; // 戻り値格納用
			for (int i=1; i<xfci1ArgLength; i++) {
				this.xfci1ArgCaches[i] = (ScalarCache)operandCaches[i + 1]; // 引数格納用
			}

			// 上記キャッシュを渡す際にコピーする必要があるかどうかを控える配列を用意
			boolean[] paramReferencenesses = function.getParameterReferencenesses();
			xfci1ArgShouldCopied = new boolean[ paramReferencenesses.length + 1 ]; // +1 は戻り値格納用の要素が加わるため
			xfci1ArgShouldCopied[0] = false; // [0] は戻り値格納用で、コピーは不要（コピーすると参照が切れて、結果が呼び出し元に反映されない）
			for (int i=1; i<xfci1ArgLength; i++) {
				xfci1ArgShouldCopied[i] = !(paramReferencenesses[i - 1]); // 引数が参照渡しでなければ、その引数はコピーが必要
			}

			// 戻り値格納用のデータコンテナは、外部関数側でメモリ確保される前提で、VRILコード上では alloc されない場合が普通にある。
			// しかしこのノードではデータコンテナの代わりにキャッシュオブジェクトが外部関数に渡されるので、
			// そこでもデータコンテナはメモリ確保されない。そのため、ここでスカラを格納できる形に確保しておく。
			// それを行わないと、accelerator 内のどこかで、戻り値格納アドレスに対してキャッシュ同期しようとした際に失敗する。
			if (isReturnValueCachedScalar && returnValueDataType != DataType.VOID) {
				new ExecutionUnit().alloc(
					returnValueDataType, returnValueContainer, DataContainer.ARRAY_SIZE_OF_SCALAR, DataContainer.ARRAY_LENGTHS_OF_SCALAR
				);
			}
		}

		@Override
		public final AcceleratorExecutionNode execute() {

			// XFCI1 の invoke に渡す用の引数キャッシュ配列 xfci1ArgCachesForPassing を用意し、
			// caller から渡される引数キャッシュ xfci1ArgCaches の各要素を、
			// 参照渡しか値渡しかに応じて参照代入 or コピーする
			int xfci1ArgLength = xfci1ArgCaches.length;
			ScalarCache[] xfci1ArgCachesForPassing = new ScalarCache[ xfci1ArgLength ];
			for (int i=0; i<xfci1ArgLength; i++) {
				if (this.xfci1ArgShouldCopied[i]) {
					xfci1ArgCachesForPassing[i] = this.xfci1ArgCaches[i].clone();
				} else {
					xfci1ArgCachesForPassing[i] = this.xfci1ArgCaches[i];
				}
			}

			try {
				this.function.invoke(xfci1ArgCachesForPassing);
			} catch (ConnectorException e) {
				// 上層で拾うので実行時例外として上に投げる
				throw new RuntimeException(e);
			}
			return this.nextNode;
		}
	}

	// どのようなパターンに対応できる外部関数呼び出しノード（その代わりオーバーヘッドは比較的大きい）
	private final class GeneralCallxNode extends AcceleratorExecutionNode {
		private final DataContainer<?>[] argumentContainers;
		private final DataContainer<?> returnContainer;
		private final CacheSynchronizer synchronizer;
		private AbstractFunction function;

		public GeneralCallxNode(DataContainer<?>[] operandContainers, CacheSynchronizer synchronizer,
				DataType returnValueDataType, boolean isReturnValueCachedScalar,
				AbstractFunction function, AcceleratorExecutionNode nextNode) {

			super(nextNode, 1);
			this.synchronizer = synchronizer;
			this.function = function;

			// オペランド配列から戻り値と引数のデータコンテナを抽出（[0]は戻り値、[1]は関数アドレス、[2]以降がargs）
			this.returnContainer = operandContainers[0];
			this.argumentContainers = new DataContainer<?>[operandContainers.length - 2];
			System.arraycopy(operandContainers, 2, this.argumentContainers, 0, operandContainers.length - 2);

			// オペランド[0] は戻り値格納用なので、外部関数側でメモリ確保される前提で、VRILコード上では alloc されない場合が普通にある。
			// そのような場合で、さらに戻り値がキャッシュ可能スカラの場合は、
			// 以下の execute() 内の最初に走る synchronizeFromCacheToMemory() で書き込み先が null のため失敗する。
			// 従って、ここでスカラを格納できる形に確保しておく。
			// ( AcceleratorではなくProcessorで処理する際にこの点が問題にならないのは、Processorにはそもそもキャッシュの仕組み無いため。 )
			if (isReturnValueCachedScalar && returnValueDataType != DataType.VOID) {
				new ExecutionUnit().alloc(
					returnValueDataType, operandContainers[0], DataContainer.ARRAY_SIZE_OF_SCALAR, DataContainer.ARRAY_LENGTHS_OF_SCALAR
				);
			}
		}

		@Override
		public final AcceleratorExecutionNode execute() {
			this.synchronizer.synchronizeFromCacheToMemory();
			try {
				this.function.invoke(this.argumentContainers, this.returnContainer);
			} catch (VnanoException e) {
				// 上層で拾うので実行時例外として上に投げる
				throw new RuntimeException(e);
			}
			this.synchronizer.synchronizeFromMemoryToCache();
			return this.nextNode;
		}
	}

}
