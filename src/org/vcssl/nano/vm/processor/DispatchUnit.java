/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.processor;


import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.OperationCode;
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
	 * @param memory データの入出力に用いる仮想メモリー（実行によって書き換えられます）
	 * @param interconnect 外部関数が接続されたインターコネクト
	 * @param executionUnit 命令実行に用いる演算ユニット
	 * @param functionRunningFlags 関数の実行中に、その先頭の命令アドレス番目の要素がtrueになるテーブル（実行によって書き換えられます）
	 * @param programCounter 命令実行前におけるプログラムカウンタの値
	 * @return 命令実行後におけるプログラムカウンタの値
	 * @throws VnanoException
	 * 		このコントロールユニットが対応していない命令が実行要求された場合や、
	 * 		オペランド数やオペランド内容（アドレス値やデータ型など）が不正であった場合、
	 * 		もしくはこの処理系でサポートされていない操作（関数の再帰呼び出しなど）
	 * 		が行われた場合に発生します。
	 */
	public final int dispatch(Instruction instruction, Memory memory, Interconnect interconnect,
			ExecutionUnit executionUnit, boolean[] functionRunningFlags, int programCounter)
					throws VnanoException {

		OperationCode opcode = instruction.getOperationCode();
		DataType[] dataTypes = instruction.getDataTypes();

		// 仮想メモリ―からデータを取り寄せる
		DataContainer<?>[] operands = this.loadOperandData(instruction, memory);
		int operandLength = operands.length;

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
			case ANDM : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.and(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case ORM : {
				this.checkNumberOfOperands(instruction, 3);
				executionUnit.or(dataTypes[0], operands[0], operands[1], operands[2]);
				return programCounter + 1;
			}
			case NOT : {
				this.checkNumberOfOperands(instruction, 2);
				executionUnit.not(dataTypes[0], operands[0], operands[1]);
				return programCounter + 1;
			}

			// メモリ確保
			case ALLOC : {

				// スカラの場合
				if (operands.length == 1) {
					executionUnit.allocScalar(dataTypes[0], operands[0]);

				// 配列の場合
				} else {
					DataContainer<?>[] lengths = new DataContainer<?>[operands.length-1 ];
					System.arraycopy(operands, 1, lengths, 0, operands.length-1);
					executionUnit.allocVector(dataTypes[0], operands[0], lengths);
				}
				return programCounter + 1;
			}

			// 第2オペランドと同じ配列要素数で、第1オペランドをメモリ確保
			case ALLOCR : {
				executionUnit.allocSameLengths(dataTypes[0], operands[0], operands[1]);
				return programCounter + 1;
			}

			// スタック上の先端に積まれているデータと同じ配列要素数で、第1オペランドをメモリ確保
			case ALLOCP : {
				executionUnit.allocSameLengths(dataTypes[0], operands[0], memory.peek());
				return programCounter + 1;
			}

			// 可読性や最適化のための型宣言命令なので何もしない（Processor は最適化を行わないので無くても動作する）
			case ALLOCT : {
				return programCounter + 1;
			}

			// コピー代入、同型かつ同要素数の場合のみ可能
			case MOV : {
				this.checkNumberOfOperands(instruction, 2);
				executionUnit.mov(dataTypes[0], operands[0], operands[1]);
				return programCounter + 1;
			}

			// 参照代入
			case REF : {
				this.checkNumberOfOperands(instruction, 2);
				executionUnit.ref(dataTypes[0], operands[0], operands[1]);
				return programCounter + 1;
			}

			// スタックからデータコンテナを1つ取って何もしない（void関数の戻り値取り出し用）
			case POP : {
				this.checkNumberOfOperands(instruction, 1);
				memory.pop();
				return programCounter + 1;
			}

			// スタックからデータコンテナを1つ取ってコピー代入
			case MOVPOP : {
				this.checkNumberOfOperands(instruction, 1);
				DataContainer<?> src = memory.pop();
				executionUnit.mov(dataTypes[0], operands[0], src);
				return programCounter + 1;
			}

			// スタックからデータコンテナを1つ取って参照代入
			case REFPOP : {
				this.checkNumberOfOperands(instruction, 1);
				DataContainer<?> src = memory.pop();
				executionUnit.ref(dataTypes[0], operands[0], src);
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
				this.checkNumberOfOperands(instruction, 2);
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

			// 配列要素コピー
			case MOVELM : {
				executionUnit.movelm(dataTypes[0], operands[0], operands[1], operands, 2);
				return programCounter + 1;
			}

			// 配列要素参照
			case REFELM : {
				executionUnit.refelm(dataTypes[0], operands[0], operands[1], operands, 2);
				return programCounter + 1;
			}

			// メモリ解放
			case FREE : {
				this.checkNumberOfOperands(instruction, 1);
				operands[0].initialize();
				return programCounter + 1;
			}

			// 条件が true の場合に飛ぶ分岐命令
			case JMP : {

				 // 以下、0番オペランドを書き込み対象に統一した際に要変更
				this.checkNumberOfOperands(instruction, 3); // オペランド[0]はプレースホルダなので、オペランドは3個ある
				boolean[] conditions = (boolean[])operands[2].getData(); // オペランド[2] が分岐条件（[0]はプレースホルダ）

				// 以下、飛ぶべきかどうかの判定。オペランド[2]の値がtrueなら飛ぶ。
				// ただしオペランド[2]はスカラに限らず、ベクトルの場合もあり、その場合は全要素がtrueなら飛ぶものと定義する。
				// そう定義する事で、中間コードにおいて、ベクトル論理演算とスカラ論理演算の短絡評価処理を統一的かつ簡潔に表現できる。

				boolean shouldJump = true; // 飛ぶべき場合にtrue、飛んではいけない場合にfalseにする
				for (boolean condition: conditions) {
					shouldJump &= condition; // オペランド[2]の要素に1つでもfalseがあればfalseになり、飛ばなくなる
				}

				// 飛ぶべき場合： 分岐先命令に飛ぶ
				if (shouldJump) {
					return (int)( (long[])operands[1].getData() )[0]; // オペランド[1]に分岐先の命令アドレスが入っている

				// 飛んではいけない場合： 次の命令に進む
				} else {
					return programCounter + 1;
				}
			}

			// 条件が false の場合に飛ぶ分岐命令
			case JMPN : {

				 // 以下、0番オペランドを書き込み対象に統一した際に要変更
				this.checkNumberOfOperands(instruction, 3); // オペランド[0]はプレースホルダなので、オペランドは3個ある
				boolean[] conditions = (boolean[])operands[2].getData(); // オペランド[2] が分岐条件（[0]はプレースホルダ）

				// 以下、飛ぶべきかどうかの判定。オペランド[2]の値がfalseなら飛ぶ。
				// ただしオペランド[2]はスカラに限らず、ベクトルの場合もあり、その場合は全要素がfalseなら飛ぶものと定義する。
				// そう定義する事で、中間コードにおいて、ベクトル論理演算とスカラ論理演算の短絡評価処理を統一的かつ簡潔に表現できる。

				boolean shouldNotJump = false; // 飛んではいけない場合にtrue、飛ぶべき場合にfalseにする
				for (boolean condition: conditions) {
					shouldNotJump |= condition; // オペランド[2]の要素に1つでもtrueがあればtrueになり、飛ばなくなる
				}

				// 飛んではいけない場合： 次の命令に進む
				if (shouldNotJump) {
					return programCounter + 1;

				// 飛ぶべき場合： 分岐先命令に飛ぶ
				} else {
					return (int)( (long[])operands[1].getData() )[0]; // オペランド[1]に分岐先の命令アドレスが入っている
				}
			}

			case CALL : {

				// 関数から戻ってくる命令アドレス（現在の命令アドレス+1）を戻り値スタックに詰む
				int returnAddress = programCounter + 1;
				DataContainer<long[]> returnAddressContainer = new DataContainer<long[]>();
				returnAddressContainer.setData(new long[] { returnAddress }, 0, DataContainer.SCALAR_LENGTHS);
				memory.push(returnAddressContainer);

				// 引数（オペランド[2]以降に並んでいる）を引数スタックに積む
				for (int operandIndex=2; operandIndex<operandLength; operandIndex++) {
					memory.push(operands[operandIndex]);
				}

				// オペランドから関数の先頭の命令アドレスを取得
				int functionAddress = (int)( (long[])operands[1].getData() )[0];

				// この処理系では関数の再帰/多重呼び出しをサポートしていないため、呼び出し対象の関数が既に実行中であればエラーとする
				if(functionRunningFlags[functionAddress]) {
					throw new VnanoException(ErrorType.RECURSIVE_FUNCTION_CALL);
				}
				functionRunningFlags[functionAddress] = true;

				// 関数先頭の命令アドレスに飛ぶ
				return functionAddress;
			}

			case RET : {
				// スタックから戻り先の命令アドレスを取り出す
				//（関数先頭で引数を正しい個数取り出していれば、スタック末尾には、CALL命令で引数より前に積んだ戻り先アドレスが積まれている）
				DataContainer<?> returnAddressContainer = memory.pop();
				int returnAddress = (int)( (long[])returnAddressContainer.getData() )[0];

				// 戻り値が無い場合は、戻り値がある場合とスタック上の順序を合わせるため、空のデータコンテナを積む
				if (operands.length <= 2) { // 先頭オペランドはプレースホルダ、その次は関数アドレスなので、戻り値が無くてもオペランドは2個ある
					memory.push(new DataContainer<Void>());

				// 戻り値がある場合は、それをスタック上に積む
				} else {
					memory.push(operands[2]);
				}

				// 再帰呼び出し判定用のテーブルから、対象関数の値を解除する
				int functionAddress = (int)( (long[])operands[1].getData() )[0];
				functionRunningFlags[functionAddress] = false;

				// 戻り先の命令アドレスに飛ぶ
				return returnAddress;
			}

			case CALLX : {
				int externalFunctionIndex = (int)( (long[])operands[1].getData() )[0];
				int argumentLength = operands.length - 2;
				DataContainer<?>[] arguments = new DataContainer[argumentLength];
				System.arraycopy(operands, 2, arguments, 0, argumentLength);
				interconnect.callExternalFunction(externalFunctionIndex, arguments, operands[0]);
				return programCounter + 1;
			}

			case ENDFUN : {
				String functionName = ( (String[])operands[0].getData() )[0];
				throw new VnanoException(ErrorType.FUNCTION_ENDED_WITHOUT_RETURNING_VALUE, functionName);
			}

			case END : {
				// スクリプトエンジンの eval メソッドの評価値とするデータがオペランドに指定されていれば、それをメモリに格納
				if (operandLength == 2) {

					// 将来的に、スクリプト終了時にリソースを即時解放するようにした場合に対応するため、データをコピーしてから格納する
					DataContainer<?> result = new DataContainer<Object>();
					executionUnit.allocSameLengths(dataTypes[0], result, operands[1]); // データコンテナの領域確保
					executionUnit.mov(dataTypes[0], result, operands[1]); // データをコピー代入
					memory.setResultDataContainer(operands[1]); // 仮想メモリの評価値格納用コンテナに格納
				}
				return -1; // この命令でコード実行は終了するので、プログラムカウンタを命令列の領域外に飛ばす（すると終了する）
			}

			case NOP : {
				this.checkNumberOfOperands(instruction, 1);
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

		Memory.Partition[] operandPartitions = instruction.getOperandPartitions();
		int[] operandAddress = instruction.getOperandAddresses();
		int operandLength = operandAddress.length;
		DataContainer<?>[] operandVariable = new DataContainer<?>[operandLength];
		for (int i=0; i<operandLength; i++) {
			operandVariable[i] = memory.getDataContainer(operandPartitions[i], operandAddress[i]);
		}
		return operandVariable;
	}

}
