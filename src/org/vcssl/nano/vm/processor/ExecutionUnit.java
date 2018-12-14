/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.processor;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.vm.accelerator.Accelerator;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.VnanoException;


/**
 * <p>
 * 仮想プロセッサ内において、{@link DispatchUnit ControlUnit}（制御ユニット）
 * から命令に該当する演算処理をディスパッチされて実行する、
 * 演算ユニットのクラスです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class ExecutionUnit {


	/**
	 * このクラスは状態を保持するフィールドを持たないため、コンストラクタは何もしません。
	 */
	public ExecutionUnit() {
	}


	/**
	 * {@link org.vcssl.nano.vm.processor.OperationCode#ADD ADD} 命令を実行します。
	 *
	 * この命令により、引数 inputA のデータと、引数 inputB のデータが加算され、
	 * 結果が引数 output のデータに格納されます。
	 *
	 * この命令はSIMD命令であり、
	 * オペランド（outputを含む）のデータは配列で、
	 * 全オペランドにおいて要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 *
	 * また、すべてのオペランド（outputを含む）のデータは、
	 * 引数 type に指定されたデータ型のものに揃っている必要があります。
	 *
	 * @param type 演算データ型
	 * @param output 結果を格納するデータ
	 * @param inputA 演算対象データ（中置記法における左側）
	 * @param inputB 演算対象データ（中置記法における右側）
	 * @throws VnanoFatalException
	 *   この命令が対応していないデータ型が指定された場合や、
	 *   指定データ型とオペランドの実際のデータ型が一致しない場合に発生します。
	 */
	public void add(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		int outputOffset = output.getOffset();
		int inputAOffset = inputA.getOffset();
		int inputBOffset = inputB.getOffset();
		int dataLength = output.getSize();

		this.checkDataType(output, type);
		this.checkDataType(inputA, type);
		this.checkDataType(inputB, type);

		switch(type) {
			case INT64 : {
				long[] inputDataA = (long[])inputA.getData();
				long[] inputDataB = (long[])inputB.getData();
				long[] outputData = (long[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] + inputDataB[inputBOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputDataA = (double[])inputA.getData();
				double[] inputDataB = (double[])inputB.getData();
				double[] outputData = (double[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] + inputDataB[inputBOffset+i];
				}
				return;
			}
			case STRING : {
				String[] inputDataA = (String[])inputA.getData();
				String[] inputDataB = (String[])inputB.getData();
				String[] outputData = (String[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] + inputDataB[inputBOffset+i];
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	/**
	 * {@link org.vcssl.nano.vm.processor.OperationCode#SUB SUB} 命令を実行します。
	 *
	 * この命令により、引数 inputA のデータから、引数 inputB のデータが減算され、
	 * 結果が引数 output のデータに格納されます。
	 *
	 * この命令はSIMD命令であり、
	 * オペランド（outputを含む）のデータは配列で、
	 * 全オペランドにおいて要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 *
	 * また、すべてのオペランド（outputを含む）のデータは、
	 * 引数 type に指定されたデータ型のものに揃っている必要があります。
	 *
	 * @param type 演算データ型
	 * @param output 結果を格納するデータ
	 * @param inputA 演算対象データ（中置記法における左側）
	 * @param inputB 演算対象データ（中置記法における右側）
	 * @throws VnanoFatalException
	 *   この命令が対応していないデータ型が指定された場合や、
	 *   指定データ型とオペランドの実際のデータ型が一致しない場合に発生します。
	 */
	public void sub(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		int outputOffset = output.getOffset();
		int inputAOffset = inputA.getOffset();
		int inputBOffset = inputB.getOffset();
		int dataLength = output.getSize();

		this.checkDataType(output, type);
		this.checkDataType(inputA, type);
		this.checkDataType(inputB, type);

		switch(type) {
			case INT64 : {
				long[] inputDataA = (long[])inputA.getData();
				long[] inputDataB = (long[])inputB.getData();
				long[] outputData = (long[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] - inputDataB[inputBOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputDataA = (double[])inputA.getData();
				double[] inputDataB = (double[])inputB.getData();
				double[] outputData = (double[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] - inputDataB[inputBOffset+i];
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	/**
	 * {@link org.vcssl.nano.vm.processor.OperationCode#MUL MUL} 命令を実行します。
	 *
	 * この命令により、引数 inputA のデータに、引数 inputB のデータが乗算され、
	 * 結果が引数 output のデータに格納されます。
	 *
	 * この命令はSIMD命令であり、
	 * オペランド（outputを含む）のデータは配列で、
	 * 全オペランドにおいて要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 *
	 * また、すべてのオペランド（outputを含む）のデータは、
	 * 引数 type に指定されたデータ型のものに揃っている必要があります。
	 *
	 * @param type 演算データ型
	 * @param output 結果を格納するデータ
	 * @param inputA 演算対象データ（中置記法における左側）
	 * @param inputB 演算対象データ（中置記法における右側）
	 * @throws VnanoFatalException
	 *   この命令が対応していないデータ型が指定された場合や、
	 *   指定データ型とオペランドの実際のデータ型が一致しない場合に発生します。
	 */
	public void mul(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		int outputOffset = output.getOffset();
		int inputAOffset = inputA.getOffset();
		int inputBOffset = inputB.getOffset();
		int dataLength = output.getSize();

		this.checkDataType(output, type);
		this.checkDataType(inputA, type);
		this.checkDataType(inputB, type);

		switch(type) {
			case INT64 : {
				long[] inputDataA = (long[])inputA.getData();
				long[] inputDataB = (long[])inputB.getData();
				long[] outputData = (long[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] * inputDataB[inputBOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputDataA = (double[])inputA.getData();
				double[] inputDataB = (double[])inputB.getData();
				double[] outputData = (double[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] * inputDataB[inputBOffset+i];
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	/**
	 * {@link org.vcssl.nano.vm.processor.OperationCode#DIV DIV} 命令を実行します。
	 *
	 * この命令により、引数 inputA のデータが、引数 inputB のデータで除算され、
	 * 結果が引数 output のデータに格納されます。
	 *
	 * この命令はSIMD命令であり、
	 * オペランド（outputを含む）のデータは配列で、
	 * 全オペランドにおいて要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 *
	 * また、すべてのオペランド（outputを含む）のデータは、
	 * 引数 type に指定されたデータ型のものに揃っている必要があります。
	 *
	 * @param type 演算データ型
	 * @param output 結果を格納するデータ
	 * @param inputA 演算対象データ（中置記法における左側）
	 * @param inputB 演算対象データ（中置記法における右側）
	 * @throws VnanoFatalException
	 *   この命令が対応していないデータ型が指定された場合や、
	 *   指定データ型とオペランドの実際のデータ型が一致しない場合に発生します。
	 */
	public void div(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		int outputOffset = output.getOffset();
		int inputAOffset = inputA.getOffset();
		int inputBOffset = inputB.getOffset();
		int dataLength = output.getSize();

		this.checkDataType(output, type);
		this.checkDataType(inputA, type);
		this.checkDataType(inputB, type);

		switch(type) {
			case INT64 : {
				long[] inputDataA = (long[])inputA.getData();
				long[] inputDataB = (long[])inputB.getData();
				long[] outputData = (long[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] / inputDataB[inputBOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputDataA = (double[])inputA.getData();
				double[] inputDataB = (double[])inputB.getData();
				double[] outputData = (double[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] / inputDataB[inputBOffset+i];
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	/**
	 * {@link org.vcssl.nano.vm.processor.OperationCode#REM REM} 命令を実行します。
	 *
	 * この命令により、引数 inputA のデータを、引数 inputB のデータで除算した剰余が計算され、
	 * 結果が引数 output のデータに格納されます。
	 *
	 * この命令はSIMD命令であり、
	 * オペランド（outputを含む）のデータは配列で、
	 * 全オペランドにおいて要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 *
	 * また、すべてのオペランド（outputを含む）のデータは、
	 * 引数 type に指定されたデータ型のものに揃っている必要があります。
	 *
	 * @param type 演算データ型
	 * @param output 結果を格納するデータ
	 * @param inputA 演算対象データ（中置記法における左側）
	 * @param inputB 演算対象データ（中置記法における右側）
	 * @throws VnanoFatalException
	 *   この命令が対応していないデータ型が指定された場合や、
	 *   指定データ型とオペランドの実際のデータ型が一致しない場合に発生します。
	 */
	public void rem(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		int outputOffset = output.getOffset();
		int inputAOffset = inputA.getOffset();
		int inputBOffset = inputB.getOffset();
		int dataLength = output.getSize();

		this.checkDataType(output, type);
		this.checkDataType(inputA, type);
		this.checkDataType(inputB, type);

		switch(type) {
			case INT64 : {
				long[] inputDataA = (long[])inputA.getData();
				long[] inputDataB = (long[])inputB.getData();
				long[] outputData = (long[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] % inputDataB[inputBOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputDataA = (double[])inputA.getData();
				double[] inputDataB = (double[])inputB.getData();
				double[] outputData = (double[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] % inputDataB[inputBOffset+i];
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}




	/**
	 * {@link org.vcssl.nano.vm.processor.OperationCode#NEG NEG} 命令を実行します。
	 *
	 * この命令により、引数 input のデータの符号反転を行った結果が、
	 * 引数 output のデータに格納されます。
	 *
	 * この命令はSIMD命令であり、
	 * オペランド（outputを含む）のデータは配列で、
	 * 全オペランドにおいて要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 *
	 * また、オペランドのデータは、引数 type に指定されたデータ型のものと一致している必要があります。
	 *
	 * @param type 演算データ型
	 * @param output 結果を格納するデータ
	 * @param input 演算対象データ
	 * @throws VnanoFatalException
	 *   この命令が対応していないデータ型が指定された場合や、
	 *   指定データ型とオペランドの実際のデータ型が一致しない場合に発生します。
	 */
	public void neg(DataType type, DataContainer<?> output, DataContainer<?> input) {

		int outputOffset = output.getOffset();
		int inputOffset = input.getOffset();
		int dataLength = output.getSize();

		this.checkDataType(output, type);
		this.checkDataType(input, type);

		switch(type) {
			case INT64 : {
				long[] inputData = (long[])input.getData();
				long[] outputData = (long[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = -inputData[inputOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputData = (double[])input.getData();
				double[] outputData = (double[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = -inputData[inputOffset+i];
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	/**
	 * {@link org.vcssl.nano.vm.processor.OperationCode#EQ EQ} 命令を実行します。
	 *
	 * この命令により、引数 inputA のデータと、引数 inputB のデータの等値比較が行われ、
	 * 結果が output のデータに格納されます。
	 * 演算結果は、inputA および inputB が一致している場合に true、一致しない場合に false となります。
	 *
	 * この命令はSIMD命令であり、
	 * オペランド（outputを含む）のデータは配列で、
	 * 全オペランドにおいて要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 *
	 * また、inputA および inputB のデータは、引数 type に指定されたデータ型のものに揃っている必要があります。
	 * output のデータ型は、必ず {@link org.vcssl.nano.lang.DataType#BOOL BOOL} 型である事が必要です。
	 *
	 * @param type 演算データ型
	 * @param output 結果を格納するデータ
	 * @param inputA 演算対象データ（中置記法における左側）
	 * @param inputB 演算対象データ（中置記法における右側）
	 * @throws VnanoFatalException
	 *   この命令が対応していないデータ型が指定された場合や、
	 *   指定データ型とオペランドの実際のデータ型が一致しない場合に発生します。
	 */
	public void eq(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		int outputOffset = output.getOffset();
		int inputAOffset = inputA.getOffset();
		int inputBOffset = inputB.getOffset();
		int dataLength = output.getSize();

		this.checkDataType(output, DataType.BOOL);
		this.checkDataType(inputA, type);
		this.checkDataType(inputB, type);

		switch(type) {
			case INT64 : {
				long[] inputDataA = (long[])inputA.getData();
				long[] inputDataB = (long[])inputB.getData();
				boolean[] outputData = (boolean[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] == inputDataB[inputBOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputDataA = (double[])inputA.getData();
				double[] inputDataB = (double[])inputB.getData();
				boolean[] outputData = (boolean[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] == inputDataB[inputBOffset+i];
				}
				return;
			}
			case BOOL : {
				boolean[] inputDataA = (boolean[])inputA.getData();
				boolean[] inputDataB = (boolean[])inputB.getData();
				boolean[] outputData = (boolean[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] == inputDataB[inputBOffset+i];
				}
				return;
			}
			case STRING : {
				String[] inputDataA = (String[])inputA.getData();
				String[] inputDataB = (String[])inputB.getData();
				boolean[] outputData = (boolean[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i].equals(inputDataB[inputBOffset+i]);
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	/**
	 * {@link org.vcssl.nano.vm.processor.OperationCode#NEQ NEQ} 命令を実行します。
	 *
	 * この命令により、引数 inputA のデータと、引数 inputB のデータの非等値比較が行われ、
	 * 結果が output のデータに格納されます。
	 * 演算結果は、inputA および inputB が一致しない場合に true、一致する場合に false となります。
	 *
	 * この命令はSIMD命令であり、
	 * オペランド（outputを含む）のデータは配列で、
	 * 全オペランドにおいて要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 *
	 * また、inputA および inputB のデータは、引数 type に指定されたデータ型のものに揃っている必要があります。
	 * output のデータ型は、必ず {@link org.vcssl.nano.lang.DataType#BOOL BOOL} 型である事が必要です。
	 *
	 * @param type 演算データ型
	 * @param output 結果を格納するデータ
	 * @param inputA 演算対象データ（中置記法における左側）
	 * @param inputB 演算対象データ（中置記法における右側）
	 * @throws VnanoFatalException
	 *   この命令が対応していないデータ型が指定された場合や、
	 *   指定データ型とオペランドの実際のデータ型が一致しない場合に発生します。
	 */
	public void neq(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		int outputOffset = output.getOffset();
		int inputAOffset = inputA.getOffset();
		int inputBOffset = inputB.getOffset();
		int dataLength = output.getSize();

		this.checkDataType(output, DataType.BOOL);
		this.checkDataType(inputA, type);
		this.checkDataType(inputB, type);

		switch(type) {
			case INT64 : {
				long[] inputDataA = (long[])inputA.getData();
				long[] inputDataB = (long[])inputB.getData();
				boolean[] outputData = (boolean[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] != inputDataB[inputBOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputDataA = (double[])inputA.getData();
				double[] inputDataB = (double[])inputB.getData();
				boolean[] outputData = (boolean[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] != inputDataB[inputBOffset+i];
				}
				return;
			}
			case BOOL : {
				boolean[] inputDataA = (boolean[])inputA.getData();
				boolean[] inputDataB = (boolean[])inputB.getData();
				boolean[] outputData = (boolean[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] != inputDataB[inputBOffset+i];
				}
				return;
			}
			case STRING : {
				String[] inputDataA = (String[])inputA.getData();
				String[] inputDataB = (String[])inputB.getData();
				boolean[] outputData = (boolean[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = !(inputDataA[inputAOffset+i].equals(inputDataB[inputBOffset+i]));
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}



	/**
	 * {@link org.vcssl.nano.vm.processor.OperationCode#GEQ GEQ} 命令を実行します。
	 *
	 * この命令により、引数 inputA のデータと、引数 inputB のデータの大小比較が行われ、
	 * 結果が output のデータに格納されます。
	 * 演算結果は、inputA のデータの値が、inputB のもの以上（等値を含む）である場合に true、
	 * そうでない場合に false となります。
	 *
	 * この命令はSIMD命令であり、
	 * オペランド（outputを含む）のデータは配列で、
	 * 全オペランドにおいて要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 *
	 * また、inputA および inputB のデータは、引数 type に指定されたデータ型のものに揃っている必要があります。
	 * output のデータ型は、必ず {@link org.vcssl.nano.lang.DataType#BOOL BOOL} 型である事が必要です。
	 *
	 * @param type 演算データ型
	 * @param output 結果を格納するデータ
	 * @param inputA 演算対象データ（中置記法における左側）
	 * @param inputB 演算対象データ（中置記法における右側）
	 * @throws VnanoFatalException
	 *   この命令が対応していないデータ型が指定された場合や、
	 *   指定データ型とオペランドの実際のデータ型が一致しない場合に発生します。
	 */
	public void geq(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		int outputOffset = output.getOffset();
		int inputAOffset = inputA.getOffset();
		int inputBOffset = inputB.getOffset();
		int dataLength = output.getSize();

		this.checkDataType(output, DataType.BOOL);
		this.checkDataType(inputA, type);
		this.checkDataType(inputB, type);

		switch(type) {
			case INT64 : {
				long[] inputDataA = (long[])inputA.getData();
				long[] inputDataB = (long[])inputB.getData();
				boolean[] outputData = (boolean[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] >= inputDataB[inputBOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputDataA = (double[])inputA.getData();
				double[] inputDataB = (double[])inputB.getData();
				boolean[] outputData = (boolean[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] >= inputDataB[inputBOffset+i];
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	/**
	 * {@link org.vcssl.nano.vm.processor.OperationCode#LEQ LEQ} 命令を実行します。
	 *
	 * この命令により、引数 inputA のデータと、引数 inputB のデータの大小比較が行われ、
	 * 結果が output のデータに格納されます。
	 * 演算結果は、inputA のデータの値が、inputB のもの以下（等値を含む）である場合に true、
	 * そうでない場合に false となります。
	 *
	 * この命令はSIMD命令であり、
	 * オペランド（outputを含む）のデータは配列で、
	 * 全オペランドにおいて要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 *
	 * また、inputA および inputB のデータは、引数 type に指定されたデータ型のものに揃っている必要があります。
	 * output のデータ型は、必ず {@link org.vcssl.nano.lang.DataType#BOOL BOOL} 型である事が必要です。
	 *
	 * @param type 演算データ型
	 * @param output 結果を格納するデータ
	 * @param inputA 演算対象データ（中置記法における左側）
	 * @param inputB 演算対象データ（中置記法における右側）
	 * @throws VnanoFatalException
	 *   この命令が対応していないデータ型が指定された場合や、
	 *   指定データ型とオペランドの実際のデータ型が一致しない場合に発生します。
	 */
	public void leq(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		int outputOffset = output.getOffset();
		int inputAOffset = inputA.getOffset();
		int inputBOffset = inputB.getOffset();
		int dataLength = output.getSize();

		this.checkDataType(output, DataType.BOOL);
		this.checkDataType(inputA, type);
		this.checkDataType(inputB, type);

		switch(type) {
			case INT64 : {
				long[] inputDataA = (long[])inputA.getData();
				long[] inputDataB = (long[])inputB.getData();
				boolean[] outputData = (boolean[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] <= inputDataB[inputBOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputDataA = (double[])inputA.getData();
				double[] inputDataB = (double[])inputB.getData();
				boolean[] outputData = (boolean[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] <= inputDataB[inputBOffset+i];
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	/**
	 * {@link org.vcssl.nano.vm.processor.OperationCode#GT GT} 命令を実行します。
	 *
	 * この命令により、引数 inputA のデータと、引数 inputB のデータの大小比較が行われ、
	 * 結果が output のデータに格納されます。
	 * 演算結果は、inputA のデータの値が、inputB のものよりも大きい（等値を含まない）場合に true、
	 * そうでない場合に false となります。
	 *
	 * この命令はSIMD命令であり、
	 * オペランド（outputを含む）のデータは配列で、
	 * 全オペランドにおいて要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 *
	 * また、inputA および inputB のデータは、引数 type に指定されたデータ型のものに揃っている必要があります。
	 * output のデータ型は、必ず {@link org.vcssl.nano.lang.DataType#BOOL BOOL} 型である事が必要です。
	 *
	 * @param type 演算データ型
	 * @param output 結果を格納するデータ
	 * @param inputA 演算対象データ（中置記法における左側）
	 * @param inputB 演算対象データ（中置記法における右側）
	 * @throws VnanoFatalException
	 *   この命令が対応していないデータ型が指定された場合や、
	 *   指定データ型とオペランドの実際のデータ型が一致しない場合に発生します。
	 */
	public void gt(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		int outputOffset = output.getOffset();
		int inputAOffset = inputA.getOffset();
		int inputBOffset = inputB.getOffset();
		int dataLength = output.getSize();

		this.checkDataType(output, DataType.BOOL);
		this.checkDataType(inputA, type);
		this.checkDataType(inputB, type);

		switch(type) {
			case INT64 : {
				long[] inputDataA = (long[])inputA.getData();
				long[] inputDataB = (long[])inputB.getData();
				boolean[] outputData = (boolean[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] > inputDataB[inputBOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputDataA = (double[])inputA.getData();
				double[] inputDataB = (double[])inputB.getData();
				boolean[] outputData = (boolean[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] > inputDataB[inputBOffset+i];
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	/**
	 * {@link org.vcssl.nano.vm.processor.OperationCode#LT LT} 命令を実行します。
	 *
	 * この命令により、引数 inputA のデータと、引数 inputB のデータの大小比較が行われ、
	 * 結果が output のデータに格納されます。
	 * 演算結果は、inputA のデータの値が、inputB のものよりも小さい（等値を含まない）場合に true、
	 * そうでない場合に false となります。
	 *
	 * この命令はSIMD命令であり、
	 * オペランド（outputを含む）のデータは配列で、
	 * 全オペランドにおいて要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 *
	 * また、inputA および inputB のデータは、引数 type に指定されたデータ型のものに揃っている必要があります。
	 * output のデータ型は、必ず {@link org.vcssl.nano.lang.DataType#BOOL BOOL} 型である事が必要です。
	 *
	 * @param type 演算データ型
	 * @param output 結果を格納するデータ
	 * @param inputA 演算対象データ（中置記法における左側）
	 * @param inputB 演算対象データ（中置記法における右側）
	 * @throws VnanoFatalException
	 *   この命令が対応していないデータ型が指定された場合や、
	 *   指定データ型とオペランドの実際のデータ型が一致しない場合に発生します。
	 */
	public void lt(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		int outputOffset = output.getOffset();
		int inputAOffset = inputA.getOffset();
		int inputBOffset = inputB.getOffset();
		int dataLength = output.getSize();

		this.checkDataType(output, DataType.BOOL);
		this.checkDataType(inputA, type);
		this.checkDataType(inputB, type);

		switch(type) {
			case INT64 : {
				long[] inputDataA = (long[])inputA.getData();
				long[] inputDataB = (long[])inputB.getData();
				boolean[] outputData = (boolean[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] < inputDataB[inputBOffset+i];
				}
				return;
			}
			case FLOAT64 : {
				double[] inputDataA = (double[])inputA.getData();
				double[] inputDataB = (double[])inputB.getData();
				boolean[] outputData = (boolean[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = inputDataA[inputAOffset+i] < inputDataB[inputBOffset+i];
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}



	/**
	 * {@link org.vcssl.nano.vm.processor.OperationCode#AND AND} 命令を実行します。
	 *
	 * この命令により、引数 inputA のデータと、引数 inputB との論理積が計算され、
	 * 結果が output のデータに格納されます。
	 * 演算結果は、inputA のデータの値と、inputB のデータの値が両者共に
	 * true である場合に true、そうでない場合に false となります。
	 *
	 * この命令はSIMD命令であり、
	 * オペランド（outputを含む）のデータは配列で、
	 * 全オペランドにおいて要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 *
	 * また、すべてのオペランド（outputを含む）のデータは、
	 * {@link org.vcssl.nano.lang.DataType#BOOL BOOL}
	 * 型のものに揃っている必要があります。
	 * 引数 type にも、必ず
	 * {@link org.vcssl.nano.lang.DataType#BOOL BOOL}
	 * 型を指定しなければなりません。
	 *
	 * ただし、（ここに短絡評価に関する説明）
	 *
	 * @param type 演算データ型
	 * @param output 結果を格納するデータ
	 * @param inputA 演算対象データ（中置記法における左側）
	 * @param inputB 演算対象データ（中置記法における右側）
	 * @throws VnanoFatalException
	 *   この命令が対応していないデータ型が指定された場合や、
	 *   指定データ型とオペランドの実際のデータ型が一致しない場合に発生します。
	 */
	public void and(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		if (type != DataType.BOOL) {
			throw new VnanoFatalException("Unoperatable data type: " + type);
		}

		int outputOffset = output.getOffset();
		int inputAOffset = inputA.getOffset();
		int inputBOffset = inputB.getOffset();
		int dataLength = output.getSize();

		this.checkDataType(output, type);
		this.checkDataType(inputA, type);

		boolean allLeftValuesAreFalse = true; // 短絡評価の判定用

		boolean[] inputDataA = (boolean[])inputA.getData();
		boolean[] outputData = (boolean[])output.getData();
		for (int i=0; i<dataLength; i++) {
			outputData[outputOffset+i] = inputDataA[inputAOffset+i];

			// inputDataA要素が一個でもtrueになれば、その後allLeftValuesAreFalseは常にfalse
			allLeftValuesAreFalse &= !inputDataA[inputAOffset+i];
		}

		if (allLeftValuesAreFalse) {
			return;
		}

		boolean[] inputDataB = (boolean[])inputB.getData();
		this.checkDataType(inputB, type);
		for (int i=0; i<dataLength; i++) {
			outputData[outputOffset+i] = outputData[outputOffset+i] && inputDataB[inputBOffset+i];
		}
	}


	/**
	 * {@link org.vcssl.nano.vm.processor.OperationCode#OR OR} 命令を実行します。
	 *
	 * この命令により、引数 inputA のデータと、引数 inputB との論理和が計算され、
	 * 結果が output のデータに格納されます。
	 * 演算結果は、inputA のデータの値と、inputB のデータの値の、
	 * 少なくとも片方が true である場合に true、両者とも false である場合に false となります。
	 *
	 * この命令はSIMD命令であり、
	 * オペランド（outputを含む）のデータは配列で、
	 * 全オペランドにおいて要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 *
	 * また、すべてのオペランド（outputを含む）のデータは、
	 * {@link org.vcssl.nano.lang.DataType#BOOL BOOL}
	 * 型のものに揃っている必要があります。
	 * 引数 type にも、必ず
	 * {@link org.vcssl.nano.lang.DataType#BOOL BOOL}
	 * 型を指定しなければなりません。
	 *
	 * ただし、（ここに短絡評価に関する説明）
	 *
	 * @param type 演算データ型
	 * @param output 結果を格納するデータ
	 * @param inputA 演算対象データ（中置記法における左側）
	 * @param inputB 演算対象データ（中置記法における右側）
	 * @throws VnanoFatalException
	 *   この命令が対応していないデータ型が指定された場合や、
	 *   指定データ型とオペランドの実際のデータ型が一致しない場合に発生します。
	 */
	// 短絡評価により、inputB は null の場合がある
	public void or(DataType type, DataContainer<?> output, DataContainer<?> inputA, DataContainer<?> inputB) {

		if (type != DataType.BOOL) {
			throw new VnanoFatalException("Unoperatable data type: " + type);
		}

		int outputOffset = output.getOffset();
		int inputAOffset = inputA.getOffset();
		int inputBOffset = inputB.getOffset();
		int dataLength = output.getSize();

		this.checkDataType(output, type);
		this.checkDataType(inputA, type);

		boolean allLeftValuesAreTrue = true; // 短絡評価の判定用

		boolean[] inputDataA = (boolean[])inputA.getData();
		boolean[] outputData = (boolean[])output.getData();
		for (int i=0; i<dataLength; i++) {
			outputData[outputOffset+i] = inputDataA[inputAOffset+i];

			// inputDataA要素が一個でもtrueになれば、その後allLeftValuesAreFalseは常にfalse
			allLeftValuesAreTrue &= inputDataA[inputAOffset+i];
		}

		if (allLeftValuesAreTrue) {
			return;
		}

		boolean[] inputDataB = (boolean[])inputB.getData();
		this.checkDataType(inputB, type);
		for (int i=0; i<dataLength; i++) {
			outputData[outputOffset+i] = outputData[outputOffset+i] || inputDataB[inputBOffset+i];
		}
	}



	/**
	 * {@link org.vcssl.nano.vm.processor.OperationCode#NOT NOT} 命令を実行します。
	 *
	 * この命令により、引数 input のデータの論理否定値が計算され、
	 * 結果が output のデータに格納されます。
	 * 演算結果は、input のデータの値が true である場合に false、
	 * 逆に false である場合に true となります。
	 *
	 * この命令はSIMD命令であり、
	 * オペランド（outputを含む）のデータは配列で、
	 * 全オペランドにおいて要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 *
	 * また、すべてのオペランド（outputを含む）のデータは、
	 * {@link org.vcssl.nano.lang.DataType#BOOL BOOL}
	 * 型のものに揃っている必要があります。
	 * 引数 type にも、必ず
	 * {@link org.vcssl.nano.lang.DataType#BOOL BOOL}
	 * 型を指定しなければなりません。
	 *
	 * @param type 演算データ型
	 * @param output 結果を格納するデータ
	 * @param input 演算対象データ
	 * @throws VnanoFatalException
	 *   この命令が対応していないデータ型が指定された場合や、
	 *   指定データ型とオペランドの実際のデータ型が一致しない場合に発生します。
	 */
	public void not(DataType type, DataContainer<?> output, DataContainer<?> input) {

		int outputOffset = output.getOffset();
		int inputOffset = input.getOffset();
		int dataLength = output.getSize();

		this.checkDataType(output, type);
		this.checkDataType(input, type);

		switch(type) {
			case BOOL : {
				boolean[] inputData = (boolean[])input.getData();
				boolean[] outputData = (boolean[])output.getData();
				for (int i=0; i<dataLength; i++) {
					outputData[outputOffset+i] = !inputData[inputOffset+i];
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}






	/**
	 * 1オペランドの {@link org.vcssl.nano.vm.processor.OperationCode#ALLOC ALLOC} 命令（スカラ確保用）を実行します。
	 *
	 * この命令により、引数 target のデータの格納領域が、
	 * 引数 type に指定されたデータ型のスカラ値を保持できるように確保されます。
	 *
	 * @param type 確保するデータの型
	 * @param target 対象データ
	 * @throws VnanoException 無効なデータ型が指定された場合に発生します。
	 */
	@SuppressWarnings("unchecked")
	public void alloc(DataType type, DataContainer<?> target) {
		switch (type) {
			case INT64 : {
				((DataContainer<long[]>)target).setData(new long[DataContainer.SIZE_OF_SCALAR]);
				return;
			}
			case FLOAT64 : {
				((DataContainer<double[]>)target).setData(new double[DataContainer.SIZE_OF_SCALAR]);
				return;
			}
			case BOOL : {
				((DataContainer<boolean[]>)target).setData(new boolean[DataContainer.SIZE_OF_SCALAR]);
				return;
			}
			case STRING : {
				((DataContainer<String[]>)target).setData(new String[DataContainer.SIZE_OF_SCALAR]);
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	/**
	 * 2オペランドの {@link org.vcssl.nano.vm.processor.OperationCode#ALLOC ALLOC} 命令（配列確保用）を実行します。
	 *
	 * この命令により、引数 target のデータの格納領域が、
	 * 引数 type に指定されたデータ型の、任意次元の配列値を保持できるように確保されます。
	 * 配列の次元および各次元の要素数は、引数 len で指定します。
	 * len に格納されている配列データの要素数が、確保対象データの配列次元となります。
	 * また、len に格納されている配列データの各要素の値が、確保対象データの次元ごとの要素数となります。
	 *
	 * @param type 確保するデータの型
	 * @param target 対象データ
	 * @param len 確保する配列の、各次元ごとの要素数を格納する配列
	 * @throws VnanoFatalException 無効なデータ型が指定された場合に発生します。
	 */
	@SuppressWarnings("unchecked")
	public void alloc(DataType type, DataContainer<?> target, DataContainer<?> len) {

		long[] lenData = ((DataContainer<long[]>)len).getData();
		int[] arrayLength = new int[lenData.length];
		int dataLength = 1;
		for (int dim=0; dim<lenData.length; dim++) {
			arrayLength[dim] = (int)lenData[dim];
			dataLength *= lenData[dim];
		}

		this.alloc(type, target, dataLength, arrayLength);
	}


	/**
	 * !!!!!これは引数の型からして内部用では？ 上の2オペランドのもので多次元配列の対応も必要なのでは？
	 * 恐らくコメントの書き直しが必要。
	 *
	 * 3オペランドの {@link org.vcssl.nano.vm.processor.OperationCode#ALLOC ALLOC} 命令（多次元配列確保用）を実行します。
	 *
	 * この命令により、引数 target のデータの格納領域が、
	 * 引数 type に指定されたデータ型の多次元配列値を保持できるように確保されます。
	 * 配列の各次元の要素数は、引数 arrayLength で指定します。
	 *
	 * また、多次元配列の総要素数を、引数 dataLength に指定する必要があります。
	 * これは通常、arrayLength の全要素値の積として求められます。
	 * ただし、この処理系では、スカラは0次元の配列として扱われるため、
	 * 0次元の場合は dataLength に 1 を指定する必要があります。
	 * しかしながら、その場合は1オペランドの
	 * {@link org.vcssl.nano.vm.processor.OperationCode#ALLOC ALLOC}
	 * 命令を実行する
	 * {@link ExecutionUnit#alloc(org.vcssl.nano.vm.memory.DataContainer.DataType, DataContainer) alloc(Data.Type, Data)}
	 * メソッドを使用する方が簡潔です。
	 *
	 * @param type 確保するデータの型
	 * @param target 対象データ
	 * @param dataLength 多次元配列の総要素数
	 * @param arrayLength 多次元配列における、各次元の要素数を格納する配列
	 * @throws VnanoFatalException 無効なデータ型が指定された場合に発生します。
	 */
	@SuppressWarnings("unchecked")
	public void alloc(DataType type, DataContainer<?> target, int dataLength, int[] arrayLength) {

		target.setLengths(arrayLength);

		Object currentData = target.getData();
		int currentDataLength = target.getSize();
		if (currentDataLength != dataLength) {
			target.setSize(dataLength);
		}
		switch (type) {
			case INT64 : {
				if (!(currentData instanceof long[]) || currentDataLength != dataLength) {
					((DataContainer<long[]>)target).setData(new long[dataLength]);
				}
				return;
			}
			case FLOAT64 : {
				if (!(currentData instanceof double[]) || currentDataLength != dataLength) {
					((DataContainer<double[]>)target).setData(new double[dataLength]);
				}
				return;
			}
			case BOOL : {
				if (!(currentData instanceof boolean[]) || currentDataLength != dataLength) {
					((DataContainer<boolean[]>)target).setData(new boolean[dataLength]);
				}
				return;
			}
			case STRING : {
				if (!(currentData instanceof String[]) || currentDataLength != dataLength) {
					((DataContainer<String[]>)target).setData(new String[dataLength]);
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	/**
	 * {@link org.vcssl.nano.vm.processor.OperationCode#MOV MOV} 命令を実行します。
	 *
	 * この命令の実行により、引数 src のデータが、引数 dest にコピーされます。
	 * その際、データの参照がコピーされるのではなく、内容がコピーされます。
	 * また、コピー先とコピー元のデータサイズは等しい事が前提とされます。
	 *
	 * この命令によるデータのコピーでは、型変換は行われないため、
	 * コピー元とコピー先のデータ型が共に、
	 * 引数 type に指定された型に揃っていなければなりません。
	 *
	 * コピー元とコピー先の型が異なる場合は、この命令の代わりに、
	 * {@link org.vcssl.nano.vm.processor.OperationCode#CAST CAST}
	 * 命令を使用する事で対応できます。
	 *
	 * しかしながら、この命令は型変換が不要な分だけ、一般に
	 * （特に {@link Accelerator Accelerator}を有効化した場合において）
	 * {@link org.vcssl.nano.vm.processor.OperationCode#CAST CAST}
	 * 命令よりも処理速度面で有利です。
	 *
	 * @param type オペランドのデータ型
	 * @param dest コピー先データ
	 * @param src コピー元データ
	 * @throws VnanoFatalException
	 *   指定データ型とオペランドの実際のデータ型が一致しない場合に発生します。
	 */
	public void mov(DataType type, DataContainer<?> dest, DataContainer<?> src) {

		this.checkDataType(dest, type);
		this.checkDataType(src, type);

		switch (type) {
			case INT64:
			case FLOAT64:
			case BOOL:
			case STRING: {

				System.arraycopy(src.getData(), src.getOffset(), dest.getData(), dest.getOffset(), dest.getSize());
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}



	/**
	 * {@link org.vcssl.nano.vm.processor.OperationCode#REORD REORD} 命令を実行します。
	 *
	 * この命令の実行により、引数 src のデータが、引数 dest にコピーされます。
	 * ただし、両者が1次元以上の配列である事が前提であり、
	 * 同じ多次元配列インデックスで参照される要素が、同じ値となるよう、
	 * コピー先の要素の配置が整列されます。
	 *
	 * @param type オペランドのデータ型
	 * @param dest コピー先データ
	 * @param src コピー元データ
	 * @throws VnanoFatalException
	 *   指定データ型とオペランドの実際のデータ型が一致しない場合に発生します。
	 */
	/*
	// 現時点では不要
	public void reord(DataType type, DataContainer<?> dest, DataContainer<?> src)
			throws VnanoException {

		this.checkDataType(dest, type);
		this.checkDataType(src, type);

		int[] srcArrayLength = src.getLengths();
		int[] destArrayLength = dest.getLengths();
		int srcRank = src.getRank();
		int destRank = dest.getRank();
		if (srcRank != destRank) {
			// エラー
		}

		int srcDataLength = src.getSize();


		int address[] = new int[ srcRank ];  // oldの1次元indexに対応するaddressを求める
		int scale[] = new int[ srcRank ];  // 各次元が1インクリメントされた際に、1次元indexがどれだけ増えるかの単位
		scale[0] = 1;
		int currentScale = 1;
		for( int i=srcRank-1; 1<=i; i-- ){
			currentScale *= srcArrayLength[i];
			scale[i] = currentScale;
		}

		// fromData の1次元インデックスに関するループ
		for(int fromDataIndex=0; fromDataIndex<srcDataLength; fromDataIndex++){

			// fromData の1次元インデックスに対応するインデックス配列
			int mod = fromDataIndex;
			for( int dim=srcRank-1; 0<=dim; dim-- ){
				address[srcRank-1-dim] = mod / scale[dim];
				mod = mod % scale[dim];
			}

			// 各次元のインデックスが、データの容量内か確認
			for(int dim=0; dim<srcRank; dim++){
				if( destArrayLength[dim]-1 < address[dim] ){
					// 容量外なので、この要素はスキップ
					continue;
				}
			}

			// このインスタンスでのデータの1次元インデックスをもとめる
			int toScale = 1;
			int toDataIndex = 0;
			for(int dim=srcRank-1; 0<=dim; dim-- ){
				toDataIndex += toScale * address[ dim ];
				toScale *= destArrayLength[ dim ];
			}

			// 要素をコピー
			System.arraycopy(src.getData(), fromDataIndex, dest.getData(), toDataIndex, 1);
		}
	}
	*/


	/**
	 * {@link org.vcssl.nano.vm.processor.OperationCode#FILL FILL} 命令を実行します。
	 *
	 * この命令により、引数 output のデータの全要素値に、
	 * 引数 filler のデータの値がコピーされます。
	 *
	 * ただし、前者のデータは配列であり、
	 * かつ後者の配列はスカラ（要素数1かつ0次元の配列）である事が前提です。
	 * また、すべてのオペランドのデータは、
	 * 引数 type に指定されたデータ型のものに揃っている必要があります
	 *
	 * @param type オペランドのデータ型
	 * @param dest コピー先データ
	 * @param src コピー元データ
	 * @throws VnanoFatalException
	 *   この命令が対応していないデータ型が指定された場合や、
	 *   指定データ型とオペランドの実際のデータ型が一致しない場合に発生します。
	 */
	public void fill(DataType type, DataContainer<?> dest, DataContainer<?> src) {

		int destOffset = dest.getOffset();
		int fillerOffset = src.getOffset();
		int destSize = dest.getSize();

		this.checkDataType(dest, type);
		this.checkDataType(src, type);

		switch(type) {
			case INT64 : {
				long fillerValue = ( (long[])src.getData() )[fillerOffset];
				long[] outputData = (long[])dest.getData();
				for (int i=0; i<destSize; i++) {
					outputData[destOffset + i] = fillerValue;
				}
				return;
			}
			case FLOAT64 : {
				double fillerValue = ( (double[])src.getData() )[fillerOffset];
				double[] outputData = (double[])dest.getData();
				for (int i=0; i<destSize; i++) {
					outputData[destOffset + i] = fillerValue;
				}
				return;
			}
			case BOOL : {
				boolean fillerValue = ( (boolean[])src.getData() )[fillerOffset];
				boolean[] outputData = (boolean[])dest.getData();
				for (int i=0; i<destSize; i++) {
					outputData[destOffset + i] = fillerValue;
				}
				return;
			}
			case STRING : {
				String fillerValue = ( (String[])src.getData() )[fillerOffset];
				String[] outputData = (String[])dest.getData();
				for (int i=0; i<destSize; i++) {
					outputData[destOffset + i] = fillerValue;
				}
				return;
			}
			default : {
				throw new VnanoFatalException("Unoperatable data type: " + type);
			}
		}
	}


	// これ可変長引数にするべきかも。多次元配列要素アクセスでVEC命令と交互に呼ぶのはコストが大きすぎる
	/**
	 * {@link org.vcssl.nano.vm.processor.OperationCode#ELEM ELEM} 命令を実行します。
	 *
	 * この命令の実行により、引数 dest が、引数 src のデータの配列要素を参照するようになります。
	 *
	 * @param type オペランドのデータ型
	 * @param dest 配列要素とするデータ
	 * @param src 配列のデータ
	 * @throws VnanoFatalException
	 *   この命令が対応していないデータ型が指定された場合や、
	 *   指定データ型とオペランドの実際のデータ型が一致しない場合に発生します。
	 */
	@SuppressWarnings("unchecked")
	public void elem(DataType type, DataContainer<?> dest, DataContainer<?> src, DataContainer<?> index) {

		this.checkDataType(src, type);

		int arrayRank = index.getSize();
		long[] arrayIndices = (long[])(index.getData());
		int[] arrayLength = src.getLengths();

		// 1 次元化されたインデックスに変換する
		int dataIndex = 0;
		int scale = 1;
		for (int i=arrayRank-1; 0 <= i; i--) {
			dataIndex += arrayIndices[i] * scale;
			scale *= arrayLength[i];
		}

		switch (type) {
			case INT64 : {
				((DataContainer<long[]>)dest).setData(((DataContainer<long[]>)src).getData());
				break;
			}
			case FLOAT64 : {
				((DataContainer<double[]>)dest).setData(((DataContainer<double[]>)src).getData());
				break;
			}
			case BOOL : {
				((DataContainer<boolean[]>)dest).setData(((DataContainer<boolean[]>)src).getData());
				break;
			}
			case STRING : {
				((DataContainer<String[]>)dest).setData(((DataContainer<String[]>)src).getData());
				break;
			}
			default : {
				throw new VnanoFatalException("Unknown data type: " + type);
			}
		}
		dest.setOffset(dataIndex);
		dest.setSize(DataContainer.SIZE_OF_SCALAR);
		dest.setLengths(DataContainer.LENGTHS_OF_SCALAR);
	}


	/**
	 * {@link org.vcssl.nano.vm.processor.OperationCode#VEC VEC} 命令を実行します。
	 *
	 * この命令の実行により、引数 dest の配列データの各要素に、
	 * 引数 elements の各要素のデータのスカラ値がコピーされます。
	 *
	 * すべてのオペランド（destを含む）のデータは、
	 * 引数 type に指定されたデータ型のものに揃っている必要があります。
	 *
	 * @param type オペランドのデータ型
	 * @param dest コピー先データ
	 * @param elements コピー元データ（スカラ値）の配列
	 * @throws VnanoFatalException
	 *   この命令が対応していないデータ型が指定された場合や、
	 *   指定データ型とオペランドの実際のデータ型が一致しない場合に発生します。
	 */
	@SuppressWarnings("unchecked")
	public void vec(DataType type, DataContainer<?> dest, DataContainer<?>[] elements) {

		int dataLength = elements.length;
		int[] arrayLength = new int[]{dataLength};

		this.checkDataType(dest, type);
		for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
			this.checkDataType(elements[dataIndex], type);
		}

		dest.setSize(dataLength);
		dest.setLengths(arrayLength);

		switch (type) {
			case INT64 : {
				long[] data = new long[dataLength];
				for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
					int fromDataIndex = elements[dataIndex].getOffset();
					data[dataIndex] = ((long[])(elements[dataIndex].getData()))[fromDataIndex];
				}
				((DataContainer<long[]>)dest).setData(data);
				return;
			}
			case FLOAT64 : {
				double[] data = new double[dataLength];
				for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
					int fromDataIndex = elements[dataIndex].getOffset();
					data[dataIndex] = ((double[])(elements[dataIndex].getData()))[fromDataIndex];
				}
				((DataContainer<double[]>)dest).setData(data);
				return;
			}
			case BOOL : {
				boolean[] data = new boolean[dataLength];
				for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
					int fromDataIndex = elements[dataIndex].getOffset();
					data[dataIndex] = ((boolean[])(elements[dataIndex].getData()))[fromDataIndex];
				}
				((DataContainer<boolean[]>)dest).setData(data);
				return;
			}
			case STRING : {
				String[] data = new String[dataLength];
				for (int dataIndex=0; dataIndex<dataLength; dataIndex++) {
					int fromDataIndex = elements[dataIndex].getOffset();
					data[dataIndex] = ((String[])(elements[dataIndex].getData()))[fromDataIndex];
				}
				((DataContainer<String[]>)dest).setData(data);
				return;
			}
			default : {
				throw new VnanoFatalException("Unknown data type: " + type);
			}
		}
	}




	/**
	 * {@link org.vcssl.nano.vm.processor.OperationCode#CAST CAST} 命令を実行します。
	 *
	 * この命令の実行により、引数 src のデータが、
	 * 型変換をされた上で、引数 dest のデータへとコピーされます。
	 * 前者のデータ型を引数 srcType に、後者のデータ型を引数 destType に指定する必要があります。
	 * コピー先とコピー元のデータサイズは等しい事が前提とされます。
	 *
	 * @param destType コピー先データのデータ型
	 * @param srcType コピー元データのデータ型
	 * @param src コピー元データ
	 * @param dest コピー先データ
	 * @param src コピー元データ
	 * @throws VnanoFatalException
	 *   この命令が対応していないデータ型が指定された場合や、
	 *   指定データ型とオペランドの実際のデータ型が一致しない場合に発生します。
	 * @throws VnanoException
	 *   型変換に失敗した場合にスローされます。
	 */
	public void cast(DataType destType, DataType srcType, DataContainer<?> dest, DataContainer<?> src)
			throws VnanoException {

		int outputOffset = dest.getOffset();
		int targetIndex = src.getOffset();
		int dataLength = dest.getSize();
		// A,B がスカラで dataLength != 1 の場合はあふれるので、ここでエラー検出が必要

		this.checkDataType(dest, destType);
		this.checkDataType(src, srcType);

		switch(destType) {
			case INT64 : {
				long[] outputData = (long[])dest.getData();
				switch(srcType) {
					case INT64 : {
						long[] targetData = (long[])src.getData();
						for (int i=0; i<dataLength; i++) {
							outputData[outputOffset+i] = targetData[targetIndex + i];
						}
						return;
					}
					case FLOAT64 : {
						double[] targetData = (double[])src.getData();
						for (int i=0; i<dataLength; i++) {
							outputData[outputOffset+i] = (int)targetData[targetIndex + i];
						}
						return;
					}
					case STRING : {
						String[] targetData = (String[])src.getData();
						for (int i=0; i<dataLength; i++) {
							try {
								// 小数点がある場合は直接 parseLong はできないので、小数点以下を除去して変換
								if (0 < targetData[targetIndex + i].indexOf(".")) {
									outputData[outputOffset+i] = Long.parseLong(
										targetData[targetIndex + i].split("\\.")[0]
									);
								} else {
									outputData[outputOffset+i] = Long.parseLong(targetData[targetIndex + i]);
								}
							} catch (NumberFormatException nfe){
								VnanoException e = new VnanoException(ErrorType.CAST_FAILED_DUE_TO_VALUE);
								e.setErrorWords(new String[] {targetData[targetIndex + i], "int" });
								throw e;
							}
						}
						return;
					}
					default : {
						VnanoException e = new VnanoException(ErrorType.CAST_FAILED_DUE_TO_TYPE);
						e.setErrorWords(new String[] {DataTypeName.getDataTypeNameOf(srcType), "int" });
						throw e;
					}
				}
			}
			case FLOAT64 : {
				double[] outputData = (double[])dest.getData();
				switch(srcType) {
					case INT64 : {
						long[] targetData = (long[])src.getData();
						for (int i=0; i<dataLength; i++) {
							outputData[outputOffset+i] = (double)targetData[targetIndex + i];
						}
						return;
					}
					case FLOAT64 : {
						double[] targetData = (double[])src.getData();
						for (int i=0; i<dataLength; i++) {
							outputData[outputOffset+i] = targetData[targetIndex + i];
						}
						return;
					}
					case STRING : {
						String[] targetData = (String[])src.getData();
						for (int i=0; i<dataLength; i++) {
							try {
								outputData[outputOffset+i] = Double.parseDouble(targetData[targetIndex + i]);
							} catch (NumberFormatException nfe){
								VnanoException e = new VnanoException(ErrorType.CAST_FAILED_DUE_TO_VALUE);
								e.setErrorWords(new String[] {targetData[targetIndex + i], "float" });
								throw e;
							}
						}
						return;
					}
					default : {
						VnanoException e = new VnanoException(ErrorType.CAST_FAILED_DUE_TO_TYPE);
						e.setErrorWords(new String[] {DataTypeName.getDataTypeNameOf(srcType), "float" });
						throw e;
					}
				}
			}
			case BOOL : {
				boolean[] outputData = (boolean[])dest.getData();
				switch(srcType) {
					case BOOL : {
						boolean[] targetData = (boolean[])src.getData();
						for (int i=0; i<dataLength; i++) {
							outputData[outputOffset+i] = targetData[targetIndex + i];
						}
						return;
					}
					case STRING : {
						final String trueString = "true";
						final String falseString = "false";
						String[] targetData = (String[])src.getData();
						for (int i=0; i<dataLength; i++) {
							if (targetData[targetIndex + i].equals(trueString)) {
								outputData[outputOffset+i] = true;
							} else if (targetData[targetIndex + i].equals(falseString)) {
								outputData[outputOffset+i] = false;
							} else {
								VnanoException e = new VnanoException(ErrorType.CAST_FAILED_DUE_TO_VALUE);
								e.setErrorWords(new String[] {targetData[targetIndex + i], "bool" });
								throw e;
							}
						}
						return;
					}
					default : {
						VnanoException e = new VnanoException(ErrorType.CAST_FAILED_DUE_TO_TYPE);
						e.setErrorWords(new String[] {DataTypeName.getDataTypeNameOf(srcType), "bool" });
						throw e;
					}
				}
			}
			case STRING : {
				String[] outputData = (String[])dest.getData();
				switch(srcType) {
					case INT64 : {
						long[] targetData = (long[])src.getData();
						for (int i=0; i<dataLength; i++) {
							outputData[outputOffset+i] = Long.toString(targetData[targetIndex + i]);
						}
						return;
					}
					case FLOAT64 : {
						double[] targetData = (double[])src.getData();
						for (int i=0; i<dataLength; i++) {
							outputData[outputOffset+i] = Double.toString(targetData[targetIndex + i]);
						}
						return;
					}
					case BOOL : {
						boolean[] targetData = (boolean[])src.getData();
						for (int i=0; i<dataLength; i++) {
							outputData[outputOffset+i] = Boolean.toString(targetData[targetIndex + i]);
						}
						return;
					}
					case STRING : {
						String[] targetData = (String[])src.getData();
						for (int i=0; i<dataLength; i++) {
							outputData[outputOffset+i] = targetData[targetIndex + i];
						}
						return;
					}
					default : {
						VnanoException e = new VnanoException(ErrorType.CAST_FAILED_DUE_TO_TYPE);
						e.setErrorWords(new String[] {DataTypeName.getDataTypeNameOf(srcType), "to string" });
						throw e;
					}
				}
			}
			default : {
				VnanoException e = new VnanoException(ErrorType.CAST_FAILED_DUE_TO_TYPE);
				e.setErrorWords(new String[] {DataTypeName.getDataTypeNameOf(srcType), DataTypeName.getDataTypeNameOf(destType) });
				throw e;
			}
		}
	}




	/**
	 * {@link org.vcssl.nano.vm.memory.DataContainer Data} オブジェクトが保持するデータの型を検査し、
	 * 期待された型と異なれば
	 * {@link org.vcssl.nano.vm.memory.VnanoException InvalidDataTypeException}
	 * 例外をスローします。
	 *
	 * @param data 型検査するデータ
	 * @param type 期待されるデータ型
	 * @throws VnanoFatalException 実際のデータ型が、期待された型と異なる場合に発生します。
	 */
	private void checkDataType(DataContainer<?> data, DataType type) {

		switch(type) {
			case INT64 : {
				if (data.getData() instanceof long[]) {
					return;
				}
				break;
			}
			case FLOAT64 : {
				if (data.getData() instanceof double[]) {
					return;
				}
				break;
			}
			case BOOL : {
				if (data.getData() instanceof boolean[]) {
					return;
				}
				break;
			}
			case STRING : {
				if (data.getData() instanceof String[]) {
					return;
				}
				break;
			}
			// void は保持データに意味が無いコンテナを示すプレースホルダなので、実際のデータの内容に依存せず通す
			case VOID : {
				return;
			}
			// 将来的に DataType 列挙子の要素が増えた場合のため
			default : {
				break;
			}
		}
		throw new VnanoFatalException("Unexpected data type: " + type);
	}

}

