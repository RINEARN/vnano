/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.spec;

// Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/spec/OperationCode.html
// ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/spec/OperationCode.html

/**
 * <p>
 * <span>
 * <span class="lang-en">
 * The enum to define operation code of instructions of the VM (Virtual Machine)
 * in the script engine of the Vnano
 * </span>
 * <span class="lang-ja">
 * Vnanoのスクリプトエンジン内のVM（仮想マシン）における, 命令のオペレーションコードが定義された列挙子です
 * </span>
 * .
 *
 * <span class="lang-en">
 * Names of elements of this enum are also used for mnemonics of the
 *   <span style="font-weight:bold">VRIL (Vector Register Intermediate Language)</span>,
 * which is the virtual assembly language for the VM.
 * </span>
 *
 * <span class="lang-ja">
 * この列挙子の要素の名称は, 同VM用の仮想的なアセンブリ言語である
 *   <span style="font-weight:bold;">VRIL（Vector Register Intermediate Language）</span>
 * のニーモニックとしても使用されます.
 * </span>
 * </p>
 *
 * <p>
 * &raquo; <a href="../../../../../src/org/vcssl/nano/spec/OperationCode.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/spec/OperationCode.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/spec/OperationCode.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public enum OperationCode {

	/**
	 * <p>
	 * <span class="lang-en">The instruction to perform the addition operation</span>
	 * <span class="lang-ja">加算を行う命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * ADD type output inputA inputB;
	 * </div>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により、「 inputA + inputB 」の計算が行われ,
	 * 結果が output に格納されます.
	 * 全オペランドのデータ型は揃っている必要があり, そのデータ型の名称を type に指定します.
	 * </span>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * the calculation of "inputA + inputB" will be performed,
	 * and then result data will be stored in the "output".
	 * The data type of all operands should be the same.
	 * Specify the name of the data type to "type".
	 * </span>
	 * </p>
	 *
	 * <p>
	 * <span class="lang-ja">
	 * なお, この命令はベクトル演算命令であり, 全オペランドの配列要素数が同一に揃っている必要があります
	 * （VM内では, スカラ値も要素数1かつ0次元の配列として扱われます）.
	 * </span>
	 *
	 * <span class="lang-en">
	 * Also, this instruction is a vector operation instruction, so
	 * the number of the array-length and the array-rank of data of all operands should be the same
	 * (A scalar value is handled as an array of length=1 and rank=0, in the VM).
	 * </span>
	 * </p>
	 */
	ADD,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to perform the subtraction operation</span>
	 * <span class="lang-ja">減算を行う命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * SUB type output inputA inputB;
	 * </div>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により、「 inputA - inputB 」の計算が行われ,
	 * 結果が output に格納されます.
	 * 全オペランドのデータ型は揃っている必要があり, そのデータ型の名称を type に指定します.
	 * </span>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * the calculation of "inputA - inputB" will be performed,
	 * and then result data will be stored in the "output".
	 * The data type of all operands should be the same.
	 * Specify the name of the data type to "type".
	 * </span>
	 * </p>
	 *
	 * <p>
	 * <span class="lang-ja">
	 * なお, この命令はベクトル演算命令であり, 全オペランドの配列要素数が同一に揃っている必要があります
	 * （VM内では, スカラ値も要素数1かつ0次元の配列として扱われます）.
	 * </span>
	 *
	 * <span class="lang-en">
	 * Also, this instruction is a vector operation instruction, so
	 * the number of the array-length and the array-rank of data of all operands should be the same
	 * (A scalar value is handled as an array of length=1 and rank=0, in the VM).
	 * </span>
	 * </p>
	 */
	SUB,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to perform the multiplication operation</span>
	 * <span class="lang-ja">乗算を行う命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * MUL type output inputA inputB;
	 * </div>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により、「 inputA * inputB 」の計算が行われ,
	 * 結果が output に格納されます.
	 * 全オペランドのデータ型は揃っている必要があり, そのデータ型の名称を type に指定します.
	 * </span>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * the calculation of "inputA * inputB" will be performed,
	 * and then result data will be stored in the "output".
	 * The data type of all operands should be the same.
	 * Specify the name of the data type to "type".
	 * </span>
	 * </p>
	 *
	 * <p>
	 * <span class="lang-ja">
	 * なお, この命令はベクトル演算命令であり, 全オペランドの配列要素数が同一に揃っている必要があります
	 * （VM内では, スカラ値も要素数1かつ0次元の配列として扱われます）.
	 * </span>
	 *
	 * <span class="lang-en">
	 * Also, this instruction is a vector operation instruction, so
	 * the number of the array-length and the array-rank of data of all operands should be the same
	 * (A scalar value is handled as an array of length=1 and rank=0, in the VM).
	 * </span>
	 * </p>
	 */
	MUL,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to perform the division operation</span>
	 * <span class="lang-ja">除算を行う命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * DIV type output inputA inputB;
	 * </div>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により、「 inputA / inputB 」の計算が行われ,
	 * 結果が output に格納されます.
	 * 全オペランドのデータ型は揃っている必要があり, そのデータ型の名称を type に指定します.
	 * </span>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * the calculation of "inputA / inputB" will be performed,
	 * and then result data will be stored in the "output".
	 * The data type of all operands should be the same.
	 * Specify the name of the data type to "type".
	 * </span>
	 * </p>
	 *
	 * <p>
	 * <span class="lang-ja">
	 * なお, この命令はベクトル演算命令であり, 全オペランドの配列要素数が同一に揃っている必要があります
	 * （VM内では, スカラ値も要素数1かつ0次元の配列として扱われます）.
	 * </span>
	 *
	 * <span class="lang-en">
	 * Also, this instruction is a vector operation instruction, so
	 * the number of the array-length and the array-rank of data of all operands should be the same
	 * (A scalar value is handled as an array of length=1 and rank=0, in the VM).
	 * </span>
	 * </p>
	 */
	DIV,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to perform the remainder operation</span>
	 * <span class="lang-ja">剰余演算を行う命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * REM type output inputA inputB;
	 * </div>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により、「 inputA % inputB 」の計算が行われ,
	 * 結果が output に格納されます.
	 * 全オペランドのデータ型は揃っている必要があり, そのデータ型の名称を type に指定します.
	 * </span>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * the calculation of "inputA % inputB" will be performed,
	 * and then result data will be stored in the "output".
	 * The data type of all operands should be the same.
	 * Specify the name of the data type to "type".
	 * </span>
	 * </p>
	 *
	 * <p>
	 * <span class="lang-ja">
	 * なお, この命令はベクトル演算命令であり, 全オペランドの配列要素数が同一に揃っている必要があります
	 * （VM内では, スカラ値も要素数1かつ0次元の配列として扱われます）.
	 * </span>
	 *
	 * <span class="lang-en">
	 * Also, this instruction is a vector operation instruction, so
	 * the number of the array-length and the array-rank of data of all operands should be the same
	 * (A scalar value is handled as an array of length=1 and rank=0, in the VM).
	 * </span>
	 * </p>
	 */
	REM,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to perform the sign inversion operation</span>
	 * <span class="lang-ja">符号反転演算を行う命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * NEG type output input;
	 * </div>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により、「 - input 」の計算が行われ,
	 * 結果が output に格納されます.
	 * 全オペランドのデータ型は揃っている必要があり, そのデータ型の名称を type に指定します.
	 * </span>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * the calculation of "- input" will be performed,
	 * and then result data will be stored in the "output".
	 * The data type of all operands should be the same.
	 * Specify the name of the data type to "type".
	 * </span>
	 * </p>
	 *
	 * <p>
	 * <span class="lang-ja">
	 * なお, この命令はベクトル演算命令であり, 全オペランドの配列要素数が同一に揃っている必要があります
	 * （VM内では, スカラ値も要素数1かつ0次元の配列として扱われます）.
	 * </span>
	 *
	 * <span class="lang-en">
	 * Also, this instruction is a vector operation instruction, so
	 * the number of the array-length and the array-rank of data of all operands should be the same
	 * (A scalar value is handled as an array of length=1 and rank=0, in the VM).
	 * </span>
	 * </p>
	 */
	NEG,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to perform the equality comparison operation</span>
	 * <span class="lang-ja">等値比較演算を行う命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * EQ type output inputA inputB;
	 * </div>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により、「 inputA == inputB 」の比較演算が行われ,
	 * 結果が output に格納されます.
	 * inputA と inputB のデータ型は揃っている必要があり, そのデータ型の名称を type に指定します.
	 * 加えて, output のデータ型は {@link DataType#BOOL BOOL} 型である必要があります.
	 * </span>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * the comparison operation of "inputA == inputB" will be performed,
	 * and then result data will be stored in the "output".
	 * The data type of "inputA" and "inputB" operands should be the same.
	 * Specify the name of the data type to "type".
	 * In addition, the data type of "output" should be {@link DataType#BOOL BOOL}.
	 * </span>
	 * </p>
	 *
	 * <p>
	 * <span class="lang-ja">
	 * なお, この命令はベクトル演算命令であり, 全オペランドの配列要素数が同一に揃っている必要があります
	 * （VM内では, スカラ値も要素数1かつ0次元の配列として扱われます）.
	 * </span>
	 *
	 * <span class="lang-en">
	 * Also, this instruction is a vector operation instruction, so
	 * the number of the array-length and the array-rank of data of all operands should be the same
	 * (A scalar value is handled as an array of length=1 and rank=0, in the VM).
	 * </span>
	 * </p>
	 */
	EQ,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to perform the "non-equality" comparison operation</span>
	 * <span class="lang-ja">非等値比較演算を行う命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * NEQ type output inputA inputB;
	 * </div>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により、「 inputA &#33= inputB 」の比較演算が行われ,
	 * 結果が output に格納されます.
	 * inputA と inputB のデータ型は揃っている必要があり, そのデータ型の名称を type に指定します.
	 * 加えて, output のデータ型は {@link DataType#BOOL BOOL} 型である必要があります.
	 * </span>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * the comparison operation of "inputA &#33= inputB" will be performed,
	 * and then result data will be stored in the "output".
	 * The data type of "inputA" and "inputB" operands should be the same.
	 * Specify the name of the data type to "type".
	 * In addition, the data type of "output" should be {@link DataType#BOOL BOOL}.
	 * </span>
	 * </p>
	 *
	 * <p>
	 * <span class="lang-ja">
	 * なお, この命令はベクトル演算命令であり, 全オペランドの配列要素数が同一に揃っている必要があります
	 * （VM内では, スカラ値も要素数1かつ0次元の配列として扱われます）.
	 * </span>
	 *
	 * <span class="lang-en">
	 * Also, this instruction is a vector operation instruction, so
	 * the number of the array-length and the array-rank of data of all operands should be the same
	 * (A scalar value is handled as an array of length=1 and rank=0, in the VM).
	 * </span>
	 * </p>
	 */
	NEQ,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to perform the "grater-than" comparison operation</span>
	 * <span class="lang-ja">大なり比較演算を行う命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * GT type output inputA inputB;
	 * </div>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により、「 inputA &gt; inputB 」の比較演算が行われ,
	 * 結果が output に格納されます.
	 * inputA と inputB のデータ型は揃っている必要があり, そのデータ型の名称を type に指定します.
	 * 加えて, output のデータ型は {@link DataType#BOOL BOOL} 型である必要があります.
	 * </span>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * the comparison operation of "inputA &gt; inputB" will be performed,
	 * and then result data will be stored in the "output".
	 * The data type of "inputA" and "inputB" operands should be the same.
	 * Specify the name of the data type to "type".
	 * In addition, the data type of "output" should be {@link DataType#BOOL BOOL}.
	 * </span>
	 * </p>
	 *
	 * <p>
	 * <span class="lang-ja">
	 * なお, この命令はベクトル演算命令であり, 全オペランドの配列要素数が同一に揃っている必要があります
	 * （VM内では, スカラ値も要素数1かつ0次元の配列として扱われます）.
	 * </span>
	 *
	 * <span class="lang-en">
	 * Also, this instruction is a vector operation instruction, so
	 * the number of the array-length and the array-rank of data of all operands should be the same
	 * (A scalar value is handled as an array of length=1 and rank=0, in the VM).
	 * </span>
	 * </p>
	 */
	GT,



	/**
	 * <p>
	 * <span class="lang-en">The instruction to perform the "less-than" comparison operation</span>
	 * <span class="lang-ja">小なり比較演算を行う命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * LT type output inputA inputB;
	 * </div>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により、「 inputA &lt; inputB 」の比較演算が行われ,
	 * 結果が output に格納されます.
	 * inputA と inputB のデータ型は揃っている必要があり, そのデータ型の名称を type に指定します.
	 * 加えて, output のデータ型は {@link DataType#BOOL BOOL} 型である必要があります.
	 * </span>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * the comparison operation of "inputA &lt; inputB" will be performed,
	 * and then result data will be stored in the "output".
	 * The data type of "inputA" and "inputB" operands should be the same.
	 * Specify the name of the data type to "type".
	 * In addition, the data type of "output" should be {@link DataType#BOOL BOOL}.
	 * </span>
	 * </p>
	 *
	 * <p>
	 * <span class="lang-ja">
	 * なお, この命令はベクトル演算命令であり, 全オペランドの配列要素数が同一に揃っている必要があります
	 * （VM内では, スカラ値も要素数1かつ0次元の配列として扱われます）.
	 * </span>
	 *
	 * <span class="lang-en">
	 * Also, this instruction is a vector operation instruction, so
	 * the number of the array-length and the array-rank of data of all operands should be the same
	 * (A scalar value is handled as an array of length=1 and rank=0, in the VM).
	 * </span>
	 * </p>
	 */
	LT,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to perform the "grater-equal" comparison operation</span>
	 * <span class="lang-ja">大なり等値（以上）比較演算を行う命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * GEQ type output inputA inputB;
	 * </div>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により、「 inputA &gt;= inputB 」の比較演算が行われ,
	 * 結果が output に格納されます.
	 * inputA と inputB のデータ型は揃っている必要があり, そのデータ型の名称を type に指定します.
	 * 加えて, output のデータ型は {@link DataType#BOOL BOOL} 型である必要があります.
	 * </span>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * the comparison operation of "inputA &gt;= inputB" will be performed,
	 * and then result data will be stored in the "output".
	 * The data type of "inputA" and "inputB" operands should be the same.
	 * Specify the name of the data type to "type".
	 * In addition, the data type of "output" should be {@link DataType#BOOL BOOL}.
	 * </span>
	 * </p>
	 *
	 * <p>
	 * <span class="lang-ja">
	 * なお, この命令はベクトル演算命令であり, 全オペランドの配列要素数が同一に揃っている必要があります
	 * （VM内では, スカラ値も要素数1かつ0次元の配列として扱われます）.
	 * </span>
	 *
	 * <span class="lang-en">
	 * Also, this instruction is a vector operation instruction, so
	 * the number of the array-length and the array-rank of data of all operands should be the same
	 * (A scalar value is handled as an array of length=1 and rank=0, in the VM).
	 * </span>
	 * </p>
	 */
	GEQ,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to perform the "less-equal" comparison operation</span>
	 * <span class="lang-ja">小なり等値（以下）比較演算を行う命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * LEQ type output inputA inputB;
	 * </div>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により、「 inputA &lt;= inputB 」の比較演算が行われ,
	 * 結果が output に格納されます.
	 * inputA と inputB のデータ型は揃っている必要があり, そのデータ型の名称を type に指定します.
	 * 加えて, output のデータ型は {@link DataType#BOOL BOOL} 型である必要があります.
	 * </span>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * the comparison operation of "inputA &lt;= inputB" will be performed,
	 * and then result data will be stored in the "output".
	 * The data type of "inputA" and "inputB" operands should be the same.
	 * Specify the name of the data type to "type".
	 * In addition, the data type of "output" should be {@link DataType#BOOL BOOL}.
	 * </span>
	 * </p>
	 *
	 * <p>
	 * <span class="lang-ja">
	 * なお, この命令はベクトル演算命令であり, 全オペランドの配列要素数が同一に揃っている必要があります
	 * （VM内では, スカラ値も要素数1かつ0次元の配列として扱われます）.
	 * </span>
	 *
	 * <span class="lang-en">
	 * Also, this instruction is a vector operation instruction, so
	 * the number of the array-length and the array-rank of data of all operands should be the same
	 * (A scalar value is handled as an array of length=1 and rank=0, in the VM).
	 * </span>
	 * </p>
	 */
	LEQ,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to perform the logical-and comparison operation</span>
	 * <span class="lang-ja">論理積演算を行う命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * AND type output inputA inputB;
	 * </div>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により、「 inputA &amp;&amp; inputB 」の論理演算が行われ,
	 * 結果が output に格納されます.
	 * 全てのオペランドのデータ型は {@link DataType#BOOL BOOL} 型に揃っている必要があり,
	 * type にも "bool" を指定します.
	 * </span>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * the logical operation of "inputA &amp;&amp; inputB" will be performed,
	 * and then result data will be stored in the "output".
	 * The data type of all operands should be {@link DataType#BOOL BOOL},
	 * and "bool" should be specified to "type".
	 * </span>
	 * </p>
	 *
	 * <p>
	 * <span class="lang-ja">
	 * なお, この命令はベクトル演算命令であり, 全オペランドの配列要素数が同一に揃っている必要があります
	 * （VM内では, スカラ値も要素数1かつ0次元の配列として扱われます）.
	 * </span>
	 *
	 * <span class="lang-en">
	 * Also, this instruction is a vector operation instruction, so
	 * the number of the array-length and the array-rank of data of all operands should be the same
	 * (A scalar value is handled as an array of length=1 and rank=0, in the VM).
	 * </span>
	 * </p>
	 */
	AND,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to perform the logical-or comparison operation</span>
	 * <span class="lang-ja">論理和演算を行う命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * OR type output inputA inputB;
	 * </div>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により、「 inputA || inputB 」の論理演算が行われ,
	 * 結果が output に格納されます.
	 * 全てのオペランドのデータ型は {@link DataType#BOOL BOOL} 型に揃っている必要があり,
	 * type にも "bool" を指定します.
	 * </span>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * the logical operation of "inputA || inputB" will be performed,
	 * and then result data will be stored in the "output".
	 * The data type of all operands should be {@link DataType#BOOL BOOL},
	 * and "bool" should be specified to "type".
	 * </span>
	 * </p>
	 *
	 * <p>
	 * <span class="lang-ja">
	 * なお, この命令はベクトル演算命令であり, 全オペランドの配列要素数が同一に揃っている必要があります
	 * （VM内では, スカラ値も要素数1かつ0次元の配列として扱われます）.
	 * </span>
	 *
	 * <span class="lang-en">
	 * Also, this instruction is a vector operation instruction, so
	 * the number of the array-length and the array-rank of data of all operands should be the same
	 * (A scalar value is handled as an array of length=1 and rank=0, in the VM).
	 * </span>
	 * </p>
	 */
	OR,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to perform the logical-not comparison operation</span>
	 * <span class="lang-ja">論理否定演算を行う命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * NOT type output input;
	 * </div>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により、「 &#33; input 」の論理演算が行われ,
	 * 結果が output に格納されます.
	 * 全てのオペランドのデータ型は {@link DataType#BOOL BOOL} 型に揃っている必要があり,
	 * type にも "bool" を指定します.
	 * </span>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * the logical operation of "&#33 input" will be performed,
	 * and then result data will be stored in the "output".
	 * The data type of all operands should be {@link DataType#BOOL BOOL},
	 * and "bool" should be specified to "type".
	 * </span>
	 * </p>
	 *
	 * <p>
	 * <span class="lang-ja">
	 * なお, この命令はベクトル演算命令であり, 全オペランドの配列要素数が同一に揃っている必要があります
	 * （VM内では, スカラ値も要素数1かつ0次元の配列として扱われます）.
	 * </span>
	 *
	 * <span class="lang-en">
	 * Also, this instruction is a vector operation instruction, so
	 * the number of the array-length and the array-rank of data of all operands should be the same
	 * (A scalar value is handled as an array of length=1 and rank=0, in the VM).
	 * </span>
	 * </p>
	 */
	NOT,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to perform the copy-assignment operation</span>
	 * <span class="lang-ja">コピー代入演算を行う命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * MOV type output input;
	 * </div>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により, input のデータがコピーされ, output に格納されます.
	 * 全オペランドのデータ型は揃っている必要があり, そのデータ型の名称を type に指定します.
	 * </span>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * the data of "input" will be copied and stored in the "output".
	 * The data type of all operands should be the same.
	 * Specify the name of the data type to "type".
	 * </span>
	 * </p>
	 *
	 * <p>
	 * <span class="lang-ja">
	 * なお, この命令はベクトル演算命令であり, 全オペランドの配列要素数が同一に揃っている必要があります
	 * （VM内では, スカラ値も要素数1かつ0次元の配列として扱われます）.
	 * </span>
	 *
	 * <span class="lang-en">
	 * Also, this instruction is a vector operation instruction, so
	 * the number of the array-length and the array-rank of data of all operands should be the same
	 * (A scalar value is handled as an array of length=1 and rank=0, in the VM).
	 * </span>
	 * </p>
	 */
	MOV,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to perform the reference-assignment operation</span>
	 * <span class="lang-ja">参照代入演算を行う命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * REF type output input;
	 * </div>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により, output が, input と同一のデータを参照するようになります.
	 * 全オペランドのデータ型は揃っている必要があり, そのデータ型の名称を type に指定します.
	 * </span>
	 *
	 * <span class="lang-en">
	 * After this instruction is executed,
	 * the data reference of "output" will be the same with it of "input".
	 * The data type of all operands should be the same.
	 * Specify the name of the data type to "type".
	 * </span>
	 * </p>
	 */
	REF,


	/**
	 * <p>
	 * <span class="lang-en">
	 * The instruction to pop data from the stack, but does not store it in anywhere
	 * </span>
	 * <span class="lang-ja">スタックからデータを取り出しつつ, どこにも格納しない命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * POP -;
	 * </div>
	 * </p>
	 */
	POP,


	/**
	 * <p>
	 * <span class="lang-en">
	 * The instruction to pop data from the stack and performs the copy-assignment operation
	 * </span>
	 * <span class="lang-ja">スタックからデータを取り出してコピー代入演算を行う命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * MOVPOP type output;
	 * </div>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により, スタックから取りだされたデータがコピーされ, output に格納されます.
	 * output のデータ型の名称を type に指定します.
	 * </span>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * the data poped from the stack will be copied and stored in the "output".
	 * Specify the name of the data type of "output" to "type".
	 * </span>
	 * </p>
	 *
	 * <p>
	 * <span class="lang-ja">
	 * なお, この命令はベクトル演算命令であり,
	 * スタックから取り出すデータと output の配列要素数が同一に揃っている必要があります
	 * （VM内では, スカラ値も要素数1かつ0次元の配列として扱われます）.
	 * </span>
	 *
	 * <span class="lang-en">
	 * Also, this instruction is a vector operation instruction,
	 * so data popped from the stack and "output" should have the same array-length and the array-rank.
	 * (A scalar value is handled as an array of length=1 and rank=0, in the VM).
	 * </span>
	 * </p>
	 */
	MOVPOP,


	/**
	 * <p>
	 * <span class="lang-en">
	 * The instruction to pop data from the stack and performs the reference-assignment operation
	 * </span>
	 * <span class="lang-ja">スタックからデータを取り出して参照代入演算を行う命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * REFPOP type output;
	 * </div>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により, output が, スタックから取りだされたデータを参照するようになります.
	 * output のデータ型の名称を type に指定します.
	 * </span>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * the data reference of "output" will set to the data popped from the stack.
	 * </span>
	 * </p>
	 */
	REFPOP,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to allocate memory</span>
	 * <span class="lang-ja">メモリー確保命令です</span>
	 * .
	 *
	 * <span class="lang-en">
	 * The number of operands of this instruction is variable. The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * この命令は可変長オペランドを取り、VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * ALLOC type target len1 len2 len3 ... lenN;
	 * </div>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed, a data container for storing data of "target"
	 * of which data type is "type" will be created in the virtual memory.
	 * </span>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により, target が type 型のデータを格納できるように, 仮想メモリー内にデータコンテナが確保されます。
	 * </span>
	 * </p>
	 *
	 * <p>
	 * <span class="lang-en">
	 * For operands len1 ... lenN, specify array-lengths of each dimensions of data to be stored.
	 * For example, specify R0 R1 R2 for storing array of which lengths are [R0][R1][R2].
	 * Scalar data is handled as a 0-dimension array, so to store scalar data, specify no operands for len1 ... lenN.
	 * </span>
	 *
	 * <span class="lang-ja">
	 * len1 ... lenN オペランドには, 格納するデータの配列要素数を, 次元ごとに指定します.
	 * 例えば [R0][R1][R2] の長さの配列を格納するには, R0 R1 R2 を指定します.
	 * スカラデータは0次元配列として扱われるため, 格納するデータがスカラの場合には,
	 * len1 ... lenN には何も指定しません.
	 * </span>
	 * </p>
	 */
	ALLOC,


	/**
	 * <span class="lang-en">
	 * A variation of the {@link OperationCode#ALLOC ALLOC} instruction to allocate memory
	 * of the same size of the other data
	 * </span>
	 * <span class="lang-ja">
	 * 他のデータと同サイズのメモリ領域を確保するための, {@link OperationCode#ALLOC ALLOC} 命令の派生命令です
	 * </span>
	 * .
	 *
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * ALLOCR type target sample;
	 * </div>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed, a data container for storing data of "target"
	 * of which data type is "type" and having the same size with "sample" will be created in the virtual memory.
	 * </span>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により, target が, type 型で sample と同サイズのデータを格納できるように, 仮想メモリー内にデータコンテナが確保されます。
	 * </span>
	 * </p>
	 *
	 * <p>
	 * <span class="lang-en">
	 * As named as "-R",
	 * this instruction is mainly used for allocating a register for storing a result of a vector operation,
	 * but this instruction is also available for allocating non-register data.
	 * </span>
	 * <span class="lang-ja">
	 * 命令名の末尾に "-R" が付いている通り,
	 * この命令は主に, ベクトル演算の結果を格納するレジスタを確保するために用いられます.
	 * ただし、レジスタ領域専用というわけではなく, 他の領域の確保にも使用できます.
	 * </span>
	 * </p>
	 */
	ALLOCR,


	/**
	 * <span class="lang-en">
	 * A variation of the {@link OperationCode#ALLOC ALLOC} instruction to allocate memory
	 * of the same size of the data which is at the top of the stack
	 * </span>
	 * <span class="lang-ja">
	 * スタック先頭にあるデータと同サイズのメモリ領域を確保するための,
	 * {@link OperationCode#ALLOC ALLOC} 命令の派生命令です
	 * </span>
	 * .
	 *
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * ALLOCP type target;
	 * </div>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed, a data container for storing data of "target"
	 * of which data type is "type" and having the same size with data which is at the top of the stack
	 * will be created in the virtual memory.
	 * </span>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により, target が, スタック先頭のデータと同サイズかつ type 型のデータを格納できるように,
	 * 仮想メモリー内にデータコンテナが確保されます.
	 * </span>
	 * </p>
	 */
	ALLOCP,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to release memory</span>
	 * <span class="lang-ja">メモリー解放命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * FREE type target;
	 * </div>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed, a data container for storing data of "target"
	 * in the virtual memory will be released.
	 * </span>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により, 仮想メモリー内に確保されていた,
	 * target のデータを格納するためのデータコンテナが解放されます.
	 * </span>
	 * </p>
	 */
	FREE,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to perform the type-cast operation</span>
	 * <span class="lang-ja">型変換命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * CAST toType:fromType output input;
	 * </div>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * a data of "input" of which data-type is "fromType" type will be casted to "toType" type,
	 * and then result data will be stored to "output".
	 * </span>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により, データ型が fromType 型である input のデータが,
	 * toType 型に変換された上で, output に代入されます.
	 * </span>
	 * </p>
	 *
	 * <p>
	 * <span class="lang-ja">
	 * なお, この命令はベクトル演算命令であり, 全オペランドの配列要素数が同一に揃っている必要があります
	 * （VM内では, スカラ値も要素数1かつ0次元の配列として扱われます）.
	 * </span>
	 *
	 * <span class="lang-en">
	 * Also, this instruction is a vector operation instruction, so
	 * the number of the array-length and the array-rank of data of all operands should be the same
	 * (A scalar value is handled as an array of length=1 and rank=0, in the VM).
	 * </span>
	 * </p>
	 */
	CAST,


	/**
	 * <p>
	 * <span class="lang-en">
	 * (Unused) The instruction to perform assignment operations of elements
	 * of which indices is the same between multi-dimensional arrays having different lengths
	 * </span>
	 * <span class="lang-ja">
	 * （未使用）次元ごとの要素数が異なる多次元配列間で, 同じインデックスの要素間で代入を行う命令です.
	 * </span>
	 * .
	 */
	REORD,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to fill all elements of the array with the same value</span>
	 * <span class="lang-ja">配列データの全要素に, 同一の値を代入する命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * FILL type output input;
	 * </div>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * the scalar value "input" will be copied to all elements of the array "output".
	 * The data type of all operands should be the same.
	 * Specify the name of the data type to "type".
	 * </span>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により, スカラ値 input が, 配列 output の全要素に代入されます.
	 * 全オペランドのデータ型は揃っている必要があり, そのデータ型の名称を type に指定します.
	 * </span>
	 * </p>
	 */
	FILL,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to refer to an element of the array.</span>
	 * <span class="lang-ja">配列要素を参照する命令です</span>
	 * .
	 * <span class="lang-en">
	 * The number of operands of this instruction is variable. The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * この命令は可変長オペランドを取り、VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * ELEM type output input index1 index2 index3 ... indexN ;
	 * </div>
	 *
	 * <span class="lang-en">
	 * After this instruction is executed,
	 * the reference of data of "output" points to the element with
	 * [index1][index2][index3]...[indexN] of the array "output".
	 * The data type of all operands should be the same.
	 * Specify the name of the data type to "type".
	 * </span>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により, output のデータの参照が, 配列 input の要素
	 * [index1][index2][index3]...[indexN] を指すようになります。
	 * 全オペランドのデータ型は揃っている必要があり, そのデータ型の名称を type に指定します.
	 * </span>
	 * </p>
	 */
	ELEM,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to jump when the condition is true</span>
	 * <span class="lang-ja">条件が真の場合に飛ぶ分岐命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * JMP bool - label condition;
	 * </div>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * the processing flow will jump to the instruction at the next of the "label",
	 * if the value of "condition" is true.
	 * (When "condition" is an array, it will jump if all elements of "condition" are true.)
	 * The data-type of "condition" should be "bool" type.
	 * </span>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により, condition に指定された値（配列の場合は全要素）が true の場合に,
	 * label に指定されたラベルの次の位置にある命令に処理が飛びます.
	 * condition のデータ型は bool 型である必要があります.
	 * </span>
	 * </p>
	 * </p>
	 */
	JMP,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to jump when the condition is false</span>
	 * <span class="lang-ja">条件が偽の場合に飛ぶ分岐命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * JMPN bool - label condition;
	 * </div>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * the processing flow will jump to the instruction at the next of the "label",
	 * if the value of "condition" is false.
	 * (When "condition" is an array, it will jump if all elements of "condition" are false.)
	 * The data-type of "condition" should be "bool" type.
	 * </span>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により, condition に指定された値（配列の場合は全要素）が false の場合に,
	 * label に指定されたラベルの次の位置にある命令に処理が飛びます.
	 * condition のデータ型は bool 型である必要があります.
	 * </span>
	 * </p>
	 * </p>
	 */
	JMPN,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to call the internal function</span>
	 * <span class="lang-ja">内部関数呼び出し命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * CALL returnType - identifier arg1 arg2 arg3 ... argN
	 * </div>
	 * <!-- returnType の存在は動作上は冗長で、省略可能とする事も考えられるが、最適化の事を考えるとスタック上の型判断のためにあった方がよい（それ以外に判断材料がない）。また、省略すると逆にアセンブルやその他で省略を考慮に入れる複雑性が生じる。 -->
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * the internal function of which identifier (mungled name) is "identifier" will be called
	 * with arguments [arg1, arg2, arg3, ..., argN].
	 * Specifically, at first, the instruction-address of the next instruction of this instruction (return address)
	 * will be pushed to the stack.
	 * Then, all arguments will be pushed to the stack from the left to the right.
	 * Finally, the processing flow will be jumped to the next instruction of the label
	 * which is declared by the same name as "identifier".
	 * To "returnType", specify the name of the data type of the return value which will be put on the stack.
	 * </span>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により, identifier に指定された（名前修飾済みの）識別子の内部関数が,
	 * [arg1, arg2, arg3, ..., argN] を引数として呼び出されます.
	 * 具体的には, まずこの命令の次の命令アドレス（戻り先アドレス）がスタックに積まれ,
	 * 続いて全ての引数が, 左から順にスタックに積まれます.
	 * そして, identifier と同名で宣言されているラベルの次の命令に処理が飛びます.
	 * returnType には、戻り値としてスタックに積まれるデータの型名を指定します。
	 * </span>
	 * </p>
	 *
	 * <p>
	 * <span class="lang-en">
	 * At the end of the internal function, the return value will be pushed at the top of the stack,
	 * and then the processing flow will be returned to the instruction at the return address by RET instruction.
	 * Therefore it requires to pop the return value from the stack after this instruction.
	 * </span>
	 * <span class="lang-ja">
	 * 内部関数の実行が終了すると, {@link RET} 命令によって, 戻り値がスタックの先頭に積まれた状態で,
	 * 戻り先アドレスの位置の命令に処理が戻されます.
	 * 従って, この命令の後方で, スタックから戻り値を取り出す必要があります.
	 * </span>
	 * </p>
	 */
	CALL,


	/**
	 * <p>
	 * <span class="lang-en">The instruction to call the external function</span>
	 * <span class="lang-ja">外部関数呼び出し命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * CALLX returnType returnValue identifier arg1 arg2 arg3 ... argN
	 * </div>
	 *
	 * <span class="lang-en">
	 * When this instruction is executed,
	 * the external function of which identifier (mungled name) is "identifier" will be called
	 * with arguments [arg1, arg2, arg3, ..., argN], and then returned value will be stored to "returnValue".
	 * Specify the name of the data type of the return value to "returnType".
	 * </span>
	 *
	 * <span class="lang-ja">
	 * この命令の実行により, identifier に指定された（名前修飾済みの）識別子の外部関数が,
	 * [arg1, arg2, arg3, ..., argN] を引数として呼び出され, 戻り値が returnValue に格納されます.
	 * 戻り値のデータ型名を returnType に指定します.
	 * </span>
	 * </p>
	 */
	CALLX,


	/**
	 * <p>
	 * <span class="lang-en">
	 * The instruction to return from the internal function
	 * </span>
	 * <span class="lang-ja">内部関数から戻るための命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 *
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * RET type returnData
	 * </div>
	 *
	 * <span class="lang-en">
	 * Specify the data of the return value of the function to "result",
	 * and specify the name of its data-type to "type".
	 * If the return value does not exist, specify nothing to "result",
	 * and specify the place-holder "-" to "type".
	 * </span>
	 *
	 * <span class="lang-ja">
	 * returnData には, 関数の戻り値に相当する値があればそれを指定し, 無ければ省略します.
	 * type にはその値のデータ型名を指定しますが, 値が無い場合にはプレースホルダ「 - 」を指定します.
	 * </span>
	 * </p>
	 *
	 * <p>
	 * <span class="lang-en">
	 * When this instruction is executed, the instruction-address (return address)
	 * which is at the top of the stack will be poped,
	 * and the return value of the function will be pushed.
	 * If there is no return value, a blank data container will be pushed.
	 * Then, the processing flow will be jumped to the instruction at the popped instruction-address.
	 * </span>
	 *
	 * <span class="lang-ja">
	 * この命令が実行されると, スタック先頭に積まれている命令アドレス（戻り先アドレス）が取り出され,
	 * 代わりに戻り値がスタックの先頭に積まれます. 戻り値が無い場合でも, 空のデータコンテナが珠琢に積まれます。
	 * そして, 取り出された命令アドレスの位置に処理が飛びます.
	 * </span>
	 * </p>
	 */
	RET,


	/**
	 * <span class="lang-en">The special instruction which is put at the end of code</span>
	 * <span class="lang-ja">コード終端に置かれる特別な命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * END type result
	 * </div>
	 *
	 * <span class="lang-ja">
	 * result には, コードの評価値に相当する値があればそれを指定し, 無ければ省略します.
	 * type にはその値のデータ型名を指定しますが, 値が無い場合にはプレースホルダ「 - 」を指定します.
	 * </span>
	 * <span class="lang-en">
	 * Specify the data to be regarded as the evaluated value of code to "result",
	 * and specify the name of its data-type to "type".
	 * If the evaluated value does not exist, specify nothing to "result",
	 * and specify the place-holder "-" to "type".
	 * </span>
	 */
	END,


	/**
	 * <span class="lang-ja">拡張命令を作成するためのオペレーションコードです</span>
	 * <span class="lang-en">The operation code to make extended instructions</span>
	 * .
	 * <span class="lang-ja">
	 * この命令は、{@link org.vcssl.nano.vm.processor} パッケージが提供する標準の仮想プロセッサでは処理されません。
	 * より上層に機能拡張された仮想プロセッサを設けて、標準命令（この列挙子に定義されている、他の全ての命令）
	 * の範囲を超えた処理を行いたい場合に使用します。
	 * <br />
	 * 拡張命令のオペレーションコードなどは、
	 * {@link org.vcssl.nano.vm.processor.Instruction Instruction} クラスの
	 * {@link org.vcssl.nano.vm.processor.Instruction#extension extension}
	 * フィールドに保持される拡張命令情報オブジェクトに格納して指定します。
	 * <br />
	 * 例として、{@link org.vcssl.nano.vm.accelerator} パッケージによって提供される高速版の仮想プロセッサ実装では、
	 * 複数の算術演算を一括して行う命令などに、この拡張命令の仕組みが使用されています。
	 * </span>
	 */
	EX,


	/**
	 * <span class="lang-en">The instruction to perform nothing</span>
	 * <span class="lang-ja">何も行わない命令です</span>
	 * .
	 * <span class="lang-en">
	 * The syntax in the VRIL code is as follows:
	 * </span>
	 * <span class="lang-ja">
	 * VRILコード内での構文は以下の通りです：
	 * </span>
	 *
	 * <div style="border: 1px solid #000000; margin:15px; padding:5px;">
	 * NOP -
	 * </div>
	 */
	NOP,

}
