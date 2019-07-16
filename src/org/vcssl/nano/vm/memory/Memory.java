/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.memory;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;

import org.vcssl.nano.spec.AssemblyWord;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.ErrorType;
import org.vcssl.nano.spec.LiteralSyntax;
import org.vcssl.nano.vm.VirtualMachineObjectCode;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.interconnect.AbstractVariable;
import org.vcssl.nano.interconnect.VariableTable;
import org.vcssl.nano.VnanoException;


/**
 * <p>
 * プログラムの実行に必要な記憶領域を提供します。
 * </p>
 *
 * <p>
 * Vnano処理系内において、中間コードの実行に必要なデータ領域を確保し、
 * 各データにアドレスを割り当てて管理する、仮想メモリーのクラスです。
 * </p>
 *
 * <p>
 * この仮想メモリーは、Vnano処理系内においてデータを扱う単位となる、
 * データを格納するコンテナ（データコンテナ）である
 * {@link DataContainer} オブジェクトを、実行に必要な数だけ、内部で List として保持します。
 * この List 内でのインデックスが、そのまま各データに割り当てられるアドレスとなります。
 * </p>
 *
 * <p>
 * ただし、この List は一つではなく複数存在し、用途に応じて使い分けられます。
 * つまり、この仮想メモリーは複数の独立な（ 異なる List に属する ）データ保持領域を持ち、
 * これをパーティションと呼びます。
 * データアクセスの際には、アドレスに加えて、
 * {@link Memory.Partition Memory.Partition} 列挙子の要素を用いて、
 * アクセス対象データが属するパーティションを指定する必要があります。
 * パーティションには、例えばローカル変数のデータを保持する
 * {@link Memory.Partition#LOCAL LOCAL} パーティションなどがあります。
 * また、Vnano処理系の仮想プロセッサー（
 * {@link org.vcssl.nano.vm.processor.Processor Processor}: プロセス仮想マシンとしてのVM
 * ）はレジスタマシンであり、そのレジスタ領域も、この仮想メモリーが
 * {@link Memory.Partition#REGISTER REGISTER} パーティションとして提供します。
 * </p>
 *
 * <p>
 * 仮想メモリーの外部からデータにアクセスするには、
 * まず {@link Memory#getDataContainer getDataContainer} メソッドを呼び出し、
 * 引数に対象データが属するパーティションとアドレスを渡します。
 * このメソッドの戻り値には、データを格納しているデータコンテナである
 * {@link DataContainer} オブジェクトがそのまま返されます。
 * このデータコンテナには、格納しているデータを設定・取得するメソッドがあり、
 * それらを使用して対象データを読み書きできます。
 * </p>
 *
 * <p>
 * なお、現在のVnano処理系の仮想プロセッサー（{@link org.vcssl.nano.vm.processor.Processor}）
 * はベクトル演算主体の命令セットを採用しているため、
 * Vnano処理系内では全てのデータが配列単位で扱われます。
 * そのため、データコンテナの内部には配列データが格納されます。
 * つまりこの仮想メモリーでは、1つの配列データまるごとに対して、
 * 1つのアドレスが割り当てられる事になります。アドレスで配列データ内の要素を指す事はできません。
 * そのような操作は、データコンテナ側で
 * {@link DataContainer#offset DataContainer.offset} フィールドが担います。
 * <p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public final class Memory {

	/**
	 * {@link Memory Memory} 内を、用途に応じた領域（パーティション）に分類して使用するための列挙子です。
	 *
	 * @author RINEARN (Fumihiro Matsui)
	 */
	public static enum Partition {

		/** グローバル領域を表します。この領域には、外部変数などのデータが保持されます。 */
		GLOBAL,

		/** ローカル領域を表します。この領域には、スクリプト内で宣言された変数などのデータが保持されます。 */
		LOCAL,

		/** 定数領域を表します。この領域には、リテラル値などの固定値データが保持されます。 */
		CONSTANT,

		/** レジスタ領域を表します。この領域には、演算用の一時データなどが保持されます。 */
		REGISTER,

		/** スタック領域を表します。この領域は、関数コール時の引数や戻り値の受け渡しなど、データの一時的な保持と取り出しに使用されます。 */
		STACK,

		/** 何もデータが無い領域を表します。この領域は、命令でのオペランドの順序を統一するため、プレースホルダとしての空オペランドなどに使用されます。 */
		NONE,
	}


	/** グローバル領域のデータを保持するリストです。この領域には、外部変数などのデータが保持されます。 */
	private List<DataContainer<?>> globalList;

	/** ローカル領域のデータを保持するリストです。この領域には、スクリプト内で宣言された変数などのデータが保持されます。 */
	private List<DataContainer<?>> localList;

	/** 定数領域のデータを保持するリストです。この領域には、リテラル値などの固定値データが保持されます。 */
	private List<DataContainer<?>> constantList;

	/** レジスタ領域のデータを保持するリストです。この領域には、演算用の一時データなどが保持されます。 */
	private List<DataContainer<?>> registerList;

	/** スタック領域として使用する双方向キューです。この領域は、関数コール時の引数や戻り値の受け渡しなど、データの一時的な保持と取り出しに使用されます。 */
	private Deque<DataContainer<?>> stack;

	/** スクリプトエンジンの eval メソッドの評価値に対応する値を格納するデータコンテナです。 */
	private DataContainer<?> resultContainer;

	/** NONEオペランドへのアクセスで返される、空のデータコンテナです。プレースホルダとしての空オペランドなどに使用されます。 */
	private DataContainer<Void> voidContainer;

	/** 分岐なしで各パーティションのリストにアクセスするためのマップです。 */
	private HashMap<Partition, List<DataContainer<?>>> containerListMap;

	/**
	 * 何もデータを保持していない、空の仮想メモリーのインスタンスを生成します。
	 */
	public Memory() {
		this.registerList = new ArrayList<DataContainer<?>>();
		this.localList = new ArrayList<DataContainer<?>>();
		this.globalList = new ArrayList<DataContainer<?>>();
		this.constantList = new ArrayList<DataContainer<?>>();
		this.stack = new ArrayDeque<DataContainer<?>>();

		this.containerListMap = new HashMap<Partition, List<DataContainer<?>>>();
		this.containerListMap.put(Memory.Partition.REGISTER, this.registerList);
		this.containerListMap.put(Memory.Partition.LOCAL, this.localList);
		this.containerListMap.put(Memory.Partition.GLOBAL, this.globalList);
		this.containerListMap.put(Memory.Partition.CONSTANT, this.constantList);

		this.voidContainer = new DataContainer<Void>();
	}




	/**
	 * 指定されたパーティションにおけるサイズ（データコンテナの数）を取得します。
	 *
	 * @param partition 対象パーティション
	 * @return サイズ（データコンテナの数）
	 * @throws MemoryAccessException
	 */
	public final int getSize(Memory.Partition partition) {
		if (partition == Memory.Partition.STACK) {
			return this.stack.size();
		} else {
			return this.containerListMap.get(partition).size();
		}
	}



	/**
	 * 指定されたアドレスに格納されているデータコンテナを取得します。
	 * なお、引数 partition に {@link Memory.Partition.NONE NONE} が指定された場合は、空のデータコンテナを返します。
	 *
	 * @param partition 対象データコンテナが属するパーティション
	 * @param address 対象のデータコンテナのアドレス
	 * @return 取得したデータコンテナ
	 * @throws VnanoFatalException
	 * 		指定されたアドレスが、使用領域外であった場合にスローされます。
	 */
	public final DataContainer<?> getDataContainer(Partition partition, int address) {
		if (partition == Memory.Partition.NONE) {
			return this.voidContainer;
		}
		if (!containerListMap.containsKey(partition)) {
			throw new VnanoFatalException("Unsupported operation for " + partition + " partition.");
		}
		List<DataContainer<?>> list = this.containerListMap.get(partition);
		try {
			return list.get(address);
		} catch (IndexOutOfBoundsException e){
			throw new VnanoFatalException("Address " + address + " is out of bounds of the " + partition + " partition.");
		}
	}

	public final void setDataContainers(Partition partition, DataContainer<?>[] containers) {
		if (!containerListMap.containsKey(partition)) {
			throw new VnanoFatalException("Unsupported operation for " + partition + " partition.");
		}
		List<DataContainer<?>> list = this.containerListMap.get(partition);
		list.clear();
		for (DataContainer<?> container: containers) {
			list.add(container);
		}
	}

	public final DataContainer<?>[] getDataContainers(Memory.Partition partition) {
		if (!containerListMap.containsKey(partition)) {
			throw new VnanoFatalException("Unsupported operation for " + partition + " partition.");
		}
		List<DataContainer<?>> list = this.containerListMap.get(partition);
		return list.toArray(new DataContainer<?>[]{});
	}


	public final void setDataContainer(Partition partition, int address, DataContainer<?> container) {
		if (!containerListMap.containsKey(partition)) {
			throw new VnanoFatalException("Unsupported operation for " + partition + " partition.");
		}
		List<DataContainer<?>> list = this.containerListMap.get(partition);
		if (address < list.size()) {
			list.set(address, container);
		} else if (address == list.size()) {
			list.add(container);
		} else {
			this.paddList(list, address-list.size());
			list.add(container);
		}
	}

	private final void paddList(List<DataContainer<?>> list, int n) {
		for (int i=0; i<n; i++) {
			list.add(new DataContainer<Object>());
		}
	}


	/**
	 * スタック領域の先端にデータを追加します。
	 *
	 * @param dataContainer 追加するデータ
	 */
	public final void push(DataContainer<?> dataContainer) {
		this.stack.push(dataContainer);
	}


	/**
	 * スタック領域の先端からデータを取り出します。
	 *
	 * @return 取り出したデータ
	 */
	public final DataContainer<?> pop() {
		return this.stack.pop();
	}


	/**
	 * スタック領域の先端にあるデータを、取り出さずに参照します。
	 *
	 * @return スタック領域の先端のデータ
	 */
	public final DataContainer<?> peek() {
		return this.stack.peek();
	}


	/**
	 * スクリプトエンジンの eval メソッドの評価値に対応するデータを設定します。
	 *
	 * @param resultContainer 評価値に対応するデータ
	 */
	public final void setResultDataContainer(DataContainer<?> resultContainer) {
		this.resultContainer = resultContainer;
	}


	/**
	 * スクリプトエンジンの eval メソッドの評価値に対応するデータを取得します。
	 *
	 * @param resultContainer 評価値に対応するデータ
	 */
	public final DataContainer<?> getResultDataContainer() {
		return this.resultContainer;
	}


	/**
	 * スクリプトエンジンの eval メソッドの評価値に対応するデータが、存在するか確認します。
	 *
	 * @param resultContainer 評価値に対応するデータが存在すれば true
	 */
	public final boolean hasResultDataContainer() {
		return (this.resultContainer != null);
	}


	/**
	 * 指定された中間コードの実行のために、必要なデータを確保します。
	 *
	 * @param intermediateCode この仮想メモリーを用いて実行する中間コード
	 * @param globalVariableTable グローバル領域に保持させる外部変数の変数テーブル
	 * @throws VnanoException これはアセンブラでやるべき
	 * @throws DataException これもかな
	 */
	public final void allocate(VirtualMachineObjectCode intermediateCode, VariableTable globalVariableTable)
			throws VnanoException {

		// レジスタ確保の確保
		int maxRegisterAddress = intermediateCode.getMaximumRegisterAddress();
		for (int registerAddress=0; registerAddress<=maxRegisterAddress; registerAddress++) {
			this.registerList.add(new DataContainer<Void>());
		}

		// ローカルデータ領域の確保
		int maxLocalAddress = intermediateCode.getMaximumLocalAddress();
		for (int localAddress=0; localAddress<=maxLocalAddress; localAddress++) {
			this.localList.add(new DataContainer<Void>());
		}

		// グローバルデータ領域の確保
		int globalSize = globalVariableTable.size();
		for (int globalIndex=0; globalIndex<globalSize; globalIndex++) {
			AbstractVariable variable = globalVariableTable.getVariableByIndex(globalIndex);
			this.globalList.add(variable.getDataContainer());
		}

		// 定数データ領域の確保
		int maxConstantAddress = intermediateCode.getMaximumConstantAddress();
		String[] immediateValues = intermediateCode.getConstantImmediateValues();
		for (int constantAddress=0; constantAddress<=maxConstantAddress; constantAddress++) {

			String immediate = immediateValues[constantAddress];
			int separatorIndex = immediate.indexOf(AssemblyWord.VALUE_SEPARATOR);
			String dataTypeName = immediate.substring(1, separatorIndex);
			String valueText = immediate.substring(separatorIndex+1, immediate.length());

			DataType dataType = DataTypeName.getDataTypeOf(dataTypeName);


			// ! ここのパースは後々でアセンブラ側に移し、
			//   VertualMachineObjectCode 内に Object 配列として値を保持しておくようにすべき

			switch (dataType) {
				case INT64 : {
					DataContainer<long[]> data = new DataContainer<long[]>();
					try {
						data.setData(new long[]{ Long.parseLong(valueText) });
					} catch(NumberFormatException e) {
						VnanoException vse = new VnanoException(ErrorType.INVALID_IMMEDIATE_VALUE, new String[] { valueText});
						throw vse;
					}
					this.constantList.add(data);
					break;
				}
				case FLOAT64 : {
					DataContainer<double[]> data = new DataContainer<double[]>();
					try {
						data.setData(new double[]{ Double.parseDouble(valueText) });
					} catch(NumberFormatException e) {
						VnanoException vse = new VnanoException(ErrorType.INVALID_IMMEDIATE_VALUE, new String[] { valueText});
						throw vse;
					}
					this.constantList.add(data);
					break;
				}
				case BOOL : {
					DataContainer<boolean[]> data = new DataContainer<boolean[]>();
					if (valueText.equals(LiteralSyntax.TRUE)) {
						data.setData(new boolean[]{ true });
					} else if (valueText.equals(LiteralSyntax.FALSE)) {
						data.setData(new boolean[]{ false });
					} else {
						VnanoException vse = new VnanoException(ErrorType.INVALID_IMMEDIATE_VALUE, new String[] { valueText});
						throw vse;
					}
					this.constantList.add(data);
					break;
				}
				case STRING : {
					DataContainer<String[]> data = new DataContainer<String[]>();
					valueText = valueText.substring(1, valueText.length()-1); // ダブルクォーテーションの除去（後でもっとちゃんとやるべき）
					data.setData(new String[]{ valueText });
					this.constantList.add(data);
					break;
				}
				default: {
					throw new VnanoFatalException("Unknown literal data type: " + dataType);
				}
			}
		}
	}

}
