/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.processor;

import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.Memory;

/**
 * <p>
 * 仮想プロセッサが直接的に解釈・実行可能な、命令のクラスです。
 * </p>
 *
 * <p>
 * 1つの命令オブジェクトが、仮想プロセッサーにおける1つの単位処理
 * （算術・比較・論理演算や代入、データ領域確保や型付け、関数呼び出しなど）に対応します。
 * 仮想プロセッサーは、この命令オブジェクトの配列を、1個ずつ逐次的に解釈して実行します。
 * </p>
 *
 * <p>
 * この命令オブジェクトの中身は、
 * 実行対象処理を指定するオペレーションコード（ {@link OperationCode OperationCode} 列挙子の要素 ）に加えて、
 * オペランドのアドレス（ int型整数 ）
 * とパーティション（ {@link org.vcssl.nano.vm.memory.Memory.Partition Memory.Partition} 列挙子の要素 ）の組、
 * およびスクリプトコード内の対応位置などを保持するメタ情報で構成されています。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class Instruction implements Cloneable {

	/** 命令のオペレーションコードを保持します。 */
	private final OperationCode operationCode;

	/** 命令の演算を行う際のデータ型情報を保持します。 */
	private final DataType[] dataTypes;

	/** 命令オペランドのデータが格納されている、仮想メモリー内でのパーティションを保持します。 */
	private final Memory.Partition[] operandPartitions;

	/** 命令オペランドのデータが格納されている、仮想メモリー内でのアドレスを保持します。 */
	private final int[] operandAddresses;

	/** スクリプト内での対応位置などのメタ情報が格納されている、仮想メモリー内でのパーティションを保持します。 */
	private final Memory.Partition metaPartition;

	/** スクリプト内での対応位置などのメタ情報が格納されている、仮想メモリー内でのアドレスを保持します。 */
	private final int metaAddress;

	/** 拡張命令（ {@link OperationCode.EX EX} 命令 ）の情報を格納するオブジェクトです。 */
	private final Object extension;

	/**
	 * 引数に指定された内容を持つ、命令のインスタンスを生成します。
	 *
	 * @param operationCode オペレーションコード
	 * @param dataTypes 演算を行う際のデータ型
	 * @param operandPartitions オペランドのデータが格納されている仮想メモリー内パーティション
	 * @param operandAddresses オペランドのデータが格納されている仮想メモリー内アドレス
	 * @param metaPartition メタ情報が格納されている仮想メモリー内パーティション
	 * @param metaAddress メタ情報が格納されている仮想メモリー内アドレス
	 */
	public Instruction(
			OperationCode operationCode, DataType[] dataTypes,
			Memory.Partition[] operandPartitions, int[] operandAddresses,
			Memory.Partition metaPartition, int metaAddress) {

		this(operationCode, dataTypes, operandPartitions, operandAddresses, metaPartition, metaAddress, null);
	}

	/**
	 * 引数に指定された内容を持つ、命令のインスタンスを生成します（拡張命令用）。
	 *
	 * @param operationCode オペレーションコード
	 * @param dataTypes 演算を行う際のデータ型
	 * @param operandPartitions オペランドのデータが格納されている仮想メモリー内パーティション
	 * @param operandAddresses オペランドのデータが格納されている仮想メモリー内アドレス
	 * @param metaPartition メタ情報が格納されている仮想メモリー内パーティション
	 * @param metaAddress メタ情報が格納されている仮想メモリー内アドレス
	 * @param extension 格闘命令の情報を格納するオブジェクト
	 */
	public Instruction(
			OperationCode operationCode, DataType[] dataTypes,
			Memory.Partition[] operandPartitions, int[] operandAddresses,
			Memory.Partition metaPartition, int metaAddress,
			Object extension) {

		this.operationCode = operationCode;
		this.dataTypes = dataTypes;
		this.operandPartitions = operandPartitions;
		this.operandAddresses = operandAddresses;
		this.metaPartition = metaPartition;
		this.metaAddress = metaAddress;
		this.extension = extension;
	}


	/**
	 * 命令の演算を行う際のデータ型情報を返します。
	 *
	 * ほとんどの命令では、オペランドのデータ型は同一のものに揃っている必要があるため、
	 * 通常はそのデータ型が、単要素の配列として返されます。
	 * ただし、{@link org.vcssl.nano.spec.OperationCode#EQ EQ} 命令や
	 * {@link org.vcssl.nano.spec.OperationCode#LT LT} 命令などの比較演算命令では、
	 * 結果を格納するオペランドの型は常に {@link org.vcssl.nano.spec.DataType#BOOL BOOL}
	 * 型であるため、それ以外の比較対象オペランドのデータ型が、単要素配列として返されます。
	 * 型変換を行う {@link org.vcssl.nano.spec.OperationCode#CAST CAST} 命令では、
	 * 変換後と変換前のデータ型をそれぞれ要素[0]と[1]に保持する、要素数2の配列が返されます。
	 * 関数呼び出しを行う {@link org.vcssl.nano.spec.OperationCode#CALLX CALLX} 命令では、
	 * 戻り値のデータ型を格納する単要素の配列が返されます（引数の型情報は含まれません）。
	 *
	 * @return 命令の演算を行う際のデータ型情報
	 */
	public DataType[] getDataTypes() {
		return this.dataTypes;
	}


	/**
	 * 命令のオペレーションコードを返します。
	 *
	 * オペレーションコードとは、オペランドのデータに対して、
	 * 仮想プロセッサにどのような演算処理を行わせるかを指定するものです。
	 * 具体的な演算処理と、オペレーションコードの値との対応については、
	 * {@link org.vcssl.nano.spec.AssemblyWord Mnemonic.OperationCode}
	 * 列挙子の説明を参照してください。
	 *
	 * @return 命令のオペレーションコード
	 */
	public OperationCode getOperationCode() {
		return this.operationCode;
	}


	/**
	 * 命令オペランドの個数を返します。
	 *
	 * @return 命令オペランドの個数
	 */
	public int getOperandLength() {
		return this.operandPartitions.length;
	}


	/**
	 * 命令オペランドのデータが格納されている、仮想メモリー内でのパーティションを返します。
	 *
	 * この処理系では、処理対象データはこの命令オブジェクト内には直接保持されず、
	 * 仮想メモリーである {@link org.vcssl.nano.vm.memory.Memory Memory} オブジェクト内に保持されます。
	 * 仮想メモリー内は用途に応じた区画に区切られており、その区画をパーティションと呼びます。
	 * 各パーティション内で、個々のデータに整数のアドレスが割りふられます。
	 * 仮想メモリー内のデータを1個指定するには、パーティションとアドレスの計2つの値が必要です。
	 * このメソッドは、各オペランドのデータが保持されているパーティションを配列にまとめて返します。
	 *
	 * @return オペランドが格納されている仮想メモリー内パーティション
	 */
	public Memory.Partition[] getOperandPartitions() {
		return this.operandPartitions;
	}


	/**
	 * 命令オペランドのデータが格納されている、仮想メモリー内でのアドレスを返します。
	 *
	 * この処理系では、処理対象データはこの命令オブジェクト内には直接保持されず、
	 * 仮想メモリーである {@link org.vcssl.nano.vm.memory.Memory Memory} オブジェクト内に保持されます。
	 * 仮想メモリー内は用途に応じた区画に区切られており、その区画をパーティションと呼びます。
	 * 各パーティション内で、個々のデータに整数のアドレスが割りふられます。
	 * 仮想メモリー内のデータを1個指定するには、パーティションとアドレスの計2つの値が必要です。
	 * このメソッドは、各オペランドのデータが保持されているアドレスを配列にまとめて返します。
	 *
	 * @return オペランドが格納されている仮想メモリー内アドレス
	 */
	public int[] getOperandAddresses() {
		return this.operandAddresses;
	}


	/**
	 * スクリプト内での対応位置などのメタ情報が格納されている、仮想メモリー内でのパーティションを保持します。
	 *
	 * メタ情報は、命令の実行には通常全く使用されませんが、
	 * エラー発生時にデバッグ情報を拾うためなどに使用されます。
	 *
	 * @return メタ情報が格納されている仮想メモリー内パーティション
	 */
	public Memory.Partition getMetaPartition() {
		return this.metaPartition;
	}


	/**
	 * スクリプト内での対応位置などのメタ情報が格納されている、仮想メモリー内でのアドレスを保持します。
	 *
	 * メタ情報は、命令の実行には通常全く使用されませんが、
	 * エラー発生時にデバッグ情報を拾うためなどに使用されます。
	 *
	 * @return メタ情報が格納されている仮想メモリー内アドレス
	 */
	public int getMetaAddress() {
		return this.metaAddress;
	}


	/**
	 * 拡張命令の情報を格納しているかどうかを判断して返します。
	 *
	 * @return 拡張命令の情報を格納しているかどうか（格納していればtrue）
	 */
	public boolean hasExtention() {
		return this.extension != null;
	}


	/**
	 * 拡張命令の情報を格納するオブジェクトを返します。
	 *
	 * @return 拡張命令の情報を保持するオブジェクト
	 */
	public Object getExtension() {
		return this.extension;
	}


	/**
	 * この命令を複製します。
	 * 原則として、フィールドはディープコピーされます。
	 * ただし、拡張命令の情報を格納する extension フィールドについては、
	 * Object 型であるため参照のみがシャローコピーされます。
	 *
	 * @return 複製された命令
	 */
	public Instruction clone() {

		DataType[] cloneDataTypes = new DataType[this.dataTypes.length];
		System.arraycopy(this.dataTypes, 0, cloneDataTypes, 0, this.dataTypes.length);

		Memory.Partition[] cloneOperandPartitions = new Memory.Partition[this.operandPartitions.length];
		System.arraycopy(this.operandPartitions, 0, cloneOperandPartitions, 0, this.operandPartitions.length);

		int[] cloneOperandAddresses = new int[this.operandAddresses.length];
		System.arraycopy(this.operandAddresses, 0, cloneOperandAddresses, 0, this.operandAddresses.length);

		Instruction cloneInstruction = new Instruction(
				this.operationCode, cloneDataTypes,
				cloneOperandPartitions, cloneOperandAddresses,
				this.metaPartition, this.metaAddress,
				this.extension
		);

		return cloneInstruction;
	}


	/**
	 * 拡張命令の情報を格納するオブジェクトを指定して、この命令を複製します。
	 *
	 * @return 複製された命令
	 */
	public Instruction clone(Object extention) {

		DataType[] cloneDataTypes = new DataType[this.dataTypes.length];
		System.arraycopy(this.dataTypes, 0, cloneDataTypes, 0, this.dataTypes.length);

		Memory.Partition[] cloneOperandPartitions = new Memory.Partition[this.operandPartitions.length];
		System.arraycopy(this.operandPartitions, 0, cloneOperandPartitions, 0, this.operandPartitions.length);

		int[] cloneOperandAddresses = new int[this.operandAddresses.length];
		System.arraycopy(this.operandAddresses, 0, cloneOperandAddresses, 0, this.operandAddresses.length);

		Instruction cloneInstruction = new Instruction(
				this.operationCode, cloneDataTypes,
				cloneOperandPartitions, cloneOperandAddresses,
				this.metaPartition, this.metaAddress,
				extention
		);

		return cloneInstruction;
	}


	/**
	 * 命令の内容を表す文字列を返します（デバッグ用）。
	 *
	 * @return 命令の内容を表す文字列
	 */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[ ");
		builder.append(this.operationCode);
		builder.append("\t");
		int dataTypeLength = this.dataTypes.length;
		for (int i=0; i<dataTypeLength; i++) {
			builder.append(this.dataTypes[i]);
			if (i != dataTypeLength - 1) {
				builder.append(":");
			}
		}
		builder.append("\t");
		int operandLength = this.operandAddresses.length;
		for (int i=0; i<operandLength; i++) {
			builder.append(this.operandPartitions[i].toString().charAt(0));
			builder.append(this.operandAddresses[i]);
			builder.append("\t");
		}
		builder.append(this.metaPartition.toString().charAt(0));
		builder.append(this.metaAddress);

		if (this.extension != null) {
			builder.append("\t extension={ ");
			builder.append(this.extension.toString());
			builder.append(" }");
		}

		builder.append(" ]");
		return builder.toString();
	}
}
