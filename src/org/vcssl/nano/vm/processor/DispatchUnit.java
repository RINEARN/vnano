/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.processor;


import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.VnanoSyntaxException;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;


/**
 * <p>
 * 仮想プロセッサ内において、命令を解釈し、
 * {@link org.vcssl.nano.vm.memory.Memory Memory}（仮想メモリー）からのデータのロードや、
 * {@link ExecutionUnit ExecutionUnit} （演算ユニット）への命令ディスパッチなどを行う、
 * 制御ユニットのクラスです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 *
 */
public class DispatchUnit {

	/**
	 * このクラスは状態を保持するフィールドを持たないため、コンストラクタは何もしません。
	 */
	public DispatchUnit() {
	}


	/**
	 * 1個の命令を演算ユニットにディスパッチして実行し、実行後におけるプログラムカウンタの値を返します。
	 *
	 * ここでの命令のディスパッチとは、命令のオペレーションコードに応じて、
	 * その処理に対応する演算ユニットのメソッドを特定し、
	 * 引数にオペランドのデータを渡して実行させる事を意味します。
	 *
	 * その際、仮想メモリーからオペランドのデータを取り寄せる処理も行います。
	 *
	 * @param instruction 実行対象の命令
	 * @param memory データの入出力に用いる仮想メモリー
	 * @param interconnect 外部関数が接続されたインターコネクト
	 * @param executionUnit 命令実行に用いる演算ユニット
	 * @param programCounter 命令実行前におけるプログラムカウンタの値
	 * @return 命令実行後におけるプログラムカウンタの値
	 * @throws InvalidInstructionException
	 * 		このコントロールユニットが対応していない命令が実行要求された場合や、
	 * 		オペランドの数が期待値と異なる場合など、命令内容が不正である場合に発生します。
	 * @throws MemoryAccessException
	 * 		命令のオペランドに指定された仮想メモリーアドレスが使用領域外であった場合など、
	 * 		不正な仮想メモリーアクセスが生じた場合などに発生します。
	 * @throws DataException
	 * 		命令のオペランドに期待されるデータ型と、
	 * 		仮想メモリー上のデータの実際の型が異なる場合などに発生します。
	 */
	@SuppressWarnings("unchecked")
	public final int dispatch(Instruction instruction, Memory memory, Interconnect interconnect,
			ExecutionUnit executionUnit, int programCounter)
					throws VnanoSyntaxException {

		OperationCode opcode = instruction.getOperationCode();
		DataType[] dataTypes = instruction.getDataTypes();

		// 仮想メモリ―からデータを取り寄せる
		DataContainer<?>[] operands = this.loadOperandData(instruction, memory);

		// 演算器で演算実行
		switch (opcode) {

			// 算術演算
			case ADD : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.add(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case SUB : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.sub(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case MUL : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.mul(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case DIV : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.div(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case REM : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.rem(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case NEG : {
				this.checkNumberOfOperands(instruction, 2);
				executionUnit.neg(dataTypes[0], operands[0], operands[1]);
				return programCounter + 1;
			}

			// 比較演算
			case EQ : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.eq(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case NEQ : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.neq(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case GEQ : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.geq(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case LEQ : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.leq(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case GT : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.gt(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case LT : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.lt(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}

			// 論理演算
			case AND : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.and(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case OR : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.or(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case NOT : {
				this.checkNumberOfOperands(instruction, 2);
				executionUnit.not(dataTypes[0], operands[0], operands[1]);
				return programCounter + 1;
			}

			// 配列確保
			case ALLOC : {

				// スカラの型付け
				if (operands.length == 1) {
					executionUnit.alloc(dataTypes[0], operands[0]);

				// 配列の型付け
				} else if (operands.length == 2) {
					executionUnit.alloc(dataTypes[0], operands[0], operands[1]);

				} else {
					throw new VnanoFatalException("Invalid number of operands: " + Integer.toString(operands.length));
				}
				return programCounter + 1;
			}

			// スカラを配列にパックする
			case VEC : {
				int dataLength = operands.length - 1;
				int[] arrayLength = new int[]{dataLength};
				executionUnit.alloc(dataTypes[0], operands[0], dataLength, arrayLength);
				DataContainer<?>[] elements = new DataContainer<?>[dataLength];
				System.arraycopy(operands, 1, elements, 0, dataLength);
				executionUnit.vec(dataTypes[0], operands[0], elements);
				return programCounter + 1;
			}

			// コピー代入、同型かつ同要素数の場合のみ可能
			case MOV : {
				this.checkNumberOfOperands(instruction, 2);
				System.arraycopy(operands[1].getData(), operands[1].getOffset(), operands[0].getData(), operands[0].getOffset(), operands[0].getSize());
				return programCounter + 1;
			}

			// 型変換付きでコピー
			case CAST : {
				this.checkNumberOfOperands(instruction, 2);
				executionUnit.cast(dataTypes[0], dataTypes[1], operands[0], operands[1]);
				return programCounter + 1;
			}

			/*
			// 現時点では不要
			// 同じインデックスの要素同士を同じ値に保って配列コピー
			case REORD : {
				this.checkNumberOfOperands(operands.length, 2);
				executionUnit.reord(dataTypes[0], operands[0], operands[1]);
				return programCounter + 1;
			}
			*/

			// 配列全要素をスカラでfill
			case FILL : {
				this.checkNumberOfOperands(instruction, 2);
				executionUnit.fill(dataTypes[0], operands[0], operands[1]);
				return programCounter + 1;
			}

			// 要素数を取得する
			case LEN : {
				int[] length = operands[1].getLengths();
				int rank = length.length;
				long[] data = new long[rank];
				for (int dim=0; dim<rank; dim++) {
					data[dim] = length[dim];
				}
				((DataContainer<long[]>)operands[0]).setData(data);
				operands[0].setSize(data.length);
				operands[0].setLengths(new int[]{data.length});
				return programCounter + 1;
			}

			// 配列要素参照
			case ELEM : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.elem(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}

			// メモリ解放
			case FREE : {
				this.checkNumberOfOperands(instruction, 1);
				operands[0].initialize();
				return programCounter + 1;
			}

			case JMP : {
				this.checkNumberOfOperands(instruction, 2);
				boolean condition = ((boolean[])operands[0].getData())[0];
				if (condition) {
					return (int)((long[])operands[1].getData())[0]; // オペランドに分岐先の命令番地が入っている
				} else {
					return programCounter + 1;
				}
			}
			case JMPN : {
				this.checkNumberOfOperands(instruction, 2);
				boolean condition = ((boolean[])operands[0].getData())[0];
				if (condition) {
					return programCounter + 1;
				} else {
					return (int)((long[])operands[1].getData())[0]; // オペランドに分岐先の命令番地が入っている
				}
			}

			case CALL : {
				int functionIndex = (int)( (long[])operands[1].getData() )[0];
				int argumentLength = operands.length - 2;
				DataContainer<?>[] arguments = new DataContainer[argumentLength];
				for (int argumentIndex=0; argumentIndex<argumentLength; argumentIndex++) {
					arguments[argumentIndex] = operands[argumentIndex + 2];
				}
				interconnect.call(functionIndex, arguments, operands[0]);
				return programCounter + 1;
			}

			case NOP : {
				this.checkNumberOfOperands(instruction, 0);
				return programCounter + 1;
			}

			// このディスパッチユニットで未対応の命令（上層で処理すべき拡張命令など）
			default : {
				throw new VnanoFatalException("Unsupported operation code: " +  opcode);
			}
		}
	}


	/**
	 * 命令におけるオペランドの個数が、期待される個数と一致するか確認し、
	 * 一致しない場合は例外を発生させます。
	 *
	 * @param instruction 確認対象の命令
	 * @param expectedValue 期待されるオペランドの個数
	 * @throws VnanoFatalException
	 * 		実際の個数が、期待される個数と異なる場合に発生します。
	 */
	private void checkNumberOfOperands(Instruction instruction, int numberOfOperands) {

		int partitionLength = instruction.getOperandPartitions().length;
		int addressLength = instruction.getOperandPartitions().length;

		if (addressLength != numberOfOperands) {
			throw new VnanoFatalException("Invalid number of operands: " + Integer.toString(addressLength));
		}

		if (partitionLength != numberOfOperands) {
			throw new VnanoFatalException("Invalid number of operands: " + Integer.toString(addressLength));
		}
	}


	/**
	 * 命令のオペランドアドレスが指し示すデータを、仮想メモリーから取り寄せて返します。
	 *
	 * @param instruction 命令
	 * @param memoryController 仮想メモリー
	 * @return データ
	 * @throws VnanoFatalException
	 * 		命令のオペランドに指定された仮想メモリーアドレスが使用領域外であった場合など、
	 * 		異常な仮想メモリーアクセスが生じた場合などに発生します。
	 */
	private DataContainer<?>[] loadOperandData(Instruction instruction, Memory memory) {

		Memory.Partition[] operandAddressType = instruction.getOperandPartitions();
		int[] operandAddress = instruction.getOperandAddresses();
		int operandLength = operandAddress.length;
		DataContainer<?>[] operandVariable = new DataContainer<?>[operandLength];
		for (int i=0; i<operandLength; i++) {
			operandVariable[i] = memory.getDataContainer(operandAddressType[i], operandAddress[i]);
		}
		return operandVariable;
	}

}
