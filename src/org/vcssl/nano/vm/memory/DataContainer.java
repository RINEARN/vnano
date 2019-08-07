/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.memory;

import java.util.HashMap;

import org.vcssl.connect.ArrayDataContainer1;
import org.vcssl.nano.spec.DataType;


/**
 * <p>
 * Vnano処理系内において、データを格納する単位となる、データコンテナのクラスです。
 * </p>
 *
 * <p>
 * 現在のVnano処理系の仮想プロセッサー（
 * {@link org.vcssl.nano.vm.processor.Processor}: プロセス仮想マシンとしてのVM
 * ）は、ベクトル演算主体の命令セットを採用しているため、
 * レジスタや仮想メモリー（{@link Memory Memory}）のデータ単位は、
 * スカラ値も含めて全て配列として扱われます。
 * その配列のデータを格納し、処理系内部でのデータのやり取りの単位や、
 * 演算のオペランドとなるのが、このデータコンテナクラスです。
 * </p>
 *
 * <p>
 * 上記の理由により、このデータコンテナは、
 * 多次元の配列データを表現する事を前提とした仕様になっています。
 * ただし内部では、多次元配列データは右端次元の要素が連続的に並ぶ形式で1次元化した上で
 * {@link DataContainer#data data} フィールドに保持されます。
 * オーバーヘッドを避けるため、このデータコンテナ側において次元変換などは一切行われないため、
 * 格納データを設定する {@link DataContainer#setData setData}
 * メソッドには、上記のように1次元化した配列を設定する必要があります。
 * 格納データを取得する {@link DataContainer#getData getData} メソッドも、
 * 1次元化された配列を返します。
 * これらの getter / setter は、{@link DataContainer#data data} フィールドをそのまま設定・取得します。
 * コピーやアライメント調整などは行われません。
 * </p>
 *
 * <p>
 * 1次元化されたデータが、多次元配列のどの要素に対応するかを計算で求めるためには、
 * 多次元配列の各次元ごとの長さ（次元長）が必要ですが、
 * それは {@link DataContainer#lengths lengths} フィールドに保持され、
 * {@link DataContainer#getLengths getLengths} メソッドによって取得できます
 * （左端次元の長さが [0] 番要素となります）。
 * 配列次元数は、この次元長配列 lengths の要素数、
 * 即ち {@link DataContainer#getLengths getLengths()}.length によって求められますが、
 * 可読性のために {@link DataContainer#getRank getRank} メソッドによって得る事もできます。
 * 多次元配列の総要素数は {@link DataContainer#size size} フィールドに保持され、
 * {@link DataContainer#getSize getSize()} メソッドによって取得できます。
 * </p>
 *
 * <p>
 * 例えば、次元長が { len0, len1, len2 } の3次元配列
 * array[ len0 ][ len1 ][ len2 ] のデータが、1次元化されて
 * {@link DataContainer#data data} 配列内に保持されているとします。
 * この場合、3次元インデックス [ dimIndex0 ][ dimIndex1 ][ dimIndex2 ]
 * によって参照される要素が、{@link DataContainer#data data}
 * 配列内で格納される1次元化インデックスを serialIndex とします。即ち：
 *
 * <div style="border-style: solid; padding-left: 10px; margin:10px;">
 * data[ serialIndex ] = array[ dimIndex0 ][ dimIndex1 ][ dimIndex2 ]
 * </div>
 *
 * （ここでの = は代入演算子ではなく、数学的な意味での等号です。）
 * この場合において、各インデックスは以下の関係に従います:
 *
 * <div style="border-style: solid; padding-left: 10px; margin:10px;">
 * serialIndex = len2*len1*dimIndex0 + len2*dimIndex1 + dimIndex2
 * </div>
 *
 * つまり、
 * 「ある次元のインデックス * それよりも右側にある全次元の次元長の積（無い場合は1とする）」
 * の項を、全て足したものが、
 * 1次元化インデックスとなります。
 * </p>
 *
 * <p>
 * 一般には配列とは見なされない、スカラのデータは、
 * このデータコンテナでは0次元の配列と見なして扱います。
 * これは、配列の次元数を、データを並べられる独立な方向の数と解釈した場合に、
 * スカラは並べられる方向を持たない配列であると見なす考え方に基づいています。
 * この考え方は、多次元配列を、何らかの（配列次元数とテンソル階数が一致する）
 * 基底におけるテンソルの配列表現と見なした場合に、
 * 数学的にはスカラは0階のテンソルである事と対応しています。
 * 従って、スカラデータを格納している場合、
 * {@link DataContainer#getRank getRank} メソッドは 0 を返し、
 * {@link DataContainer#getLengths getLengths()} メソッドは要素数0の配列を返します。
 * ただし、スカラは0次元であっても値を1個持っているため、
 * データの総要素数を表す {@link DataContainer#size size} フィールドの値は 1 となり、
 * {@link DataContainer#getSize getSize()} メソッドも 1 を返します。
 * </p>
 *
 * <p>
 * スカラデータもまた、内部では1次元化した配列として
 * {@link DataContainer#data data} フィールドに保持されます。
 * ただし、要素数 1 の配列に格納されるとは限らず、
 * より長い配列のどこかに格納される場合もあります。
 * その格納位置のインデックスは、
 * {@link DataContainer#offset offset} フィールドによって保持され、
 * {@link DataContainer#getOffset getOffset()} メソッドによって取得できます。
 * この仕組みは、スクリプトコード側において、
 * 配列変数の要素を配列アクセス演算子（ [ ] ）によって参照する事に対応する処理を、
 * 仮想マシン側で効率的に行うためのものです。
 * 具体的には、スクリプト側での配列変数のデータを {@link DataContener#data data} フィールドに保持し、
 * {@link DataContainer#offset offset} フィールドの値を操作する事によって、
 * 配列変数への要素アクセスを単純かつ低オーバーヘッドな方法で行いながら、
 * 処理上は通常のスカラデータと区別せず統一的に扱う実装を可能にするという利点があります。
 * ところで、上述ような仕組みで配列要素へのアクセスを実現している事から、
 * この処理系では、複数のデータコンテナが同じデータ保持領域（{@link DataContener#data data} フィールドの参照先）
 * を共有する場合が生じる事には留意する必要があります。
 * なお、このデータコンテナが表現する対象がスカラではない場合は、
 * {@link DataContainer#offset offset} フィールドの値は常に 0 である事が保証されます。
 * </p>
 *
 * <p>
 * このデータコンテナは、外部変数・外部関数プラグインとVnano処理系内との間で、
 * オーバーヘッドの無いデータの受け渡しをサポートするため、
 * {@link org.vcssl.connect.ArrayDataContainer1 ADCI 1}
 * 形式のデータコンテナ・インターフェースを実装しています。
 * {@link org.vcssl.connect.ExternalVariableConnector1 XVCI 1} 形式や
 * {@link org.vcssl.connect.ExternalFunctionConnector1 XFCI 1} 形式のプラグインにおいて、
 * 自動データ変換機能を無効化した上でVnano処理系に接続した場合、
 * このデータコンテナが無変換で直接受け渡しされるようになります。
 * プラグイン側において、処理系の実装への依存度を抑えたい場合、
 * {@link org.vcssl.connect.ArrayDataContainer1 ADCI 1}
 * のインターフェースに定義されたメソッドを通してアクセスする事ができます。
 * </p>
 *
 * @param <T> 保持するデータの型
 * @author RINEARN (Fumihiro Matsui)
 */
public class DataContainer<T> implements ArrayDataContainer1<T> {

	/** スカラデータを格納する場合における、多次元配列としての次元数（値は0）です。*/
	public static final int   RANK_OF_SCALAR = 0;

	/** スカラデータを格納する場合における、多次元配列としての各次元の長さを表す配列（値は要素無しの int[0]）です。*/
	public static final int[] LENGTHS_OF_SCALAR = new int[0];

	/** スカラデータを格納する場合における、データの総要素数（値は1）です。 */
	public static final int   SIZE_OF_SCALAR = 1;

	/** 引数 data に格納するデータのクラスと、{@link org.vcssl.nano.spec.DataType DataType} 列挙子の要素との対応関係を表すマップです。 */
	private static final HashMap<Class<?>, DataType> CLASS_DATA_TYPE_MAP = new HashMap<Class<?>, DataType>();
	static {
		CLASS_DATA_TYPE_MAP.put(long[].class, DataType.INT64);
		CLASS_DATA_TYPE_MAP.put(double[].class, DataType.FLOAT64);
		CLASS_DATA_TYPE_MAP.put(boolean[].class, DataType.BOOL);
		CLASS_DATA_TYPE_MAP.put(String[].class, DataType.STRING);
	}

	/**
	 * このデータコンテナが格納対象とするデータの型を保持します。
	 */
	private DataType dataType;


	/**
	 * このデータコンテナが格納するデータです。
	 *
	 * Vnano処理系では全てのデータを配列として扱うため、このフィールドは常に配列であり、
	 * [{@link DataContainer#offset offset}] 番目から
	 * [{@link DataContainer#offset offset}+{@link DataContainer#size size}-1]
	 * 番目までを使用し、配列の要素を保持します。
	 *
	 * 多次元配列は、右端次元の要素が連続的に並ぶように1次元化され、
	 * このフィールドには1次元配列として保持されます。
	 *
	 * スカラ値を保持する場合でも、このフィールドは配列であり、
	 * その場合は [{@link DataContainer#offset offset}] 番目にスカラの値が格納されます。
	 */
	private T  data;


	/**
	 * データの総要素数です。
	 *
	 * ただし、データの各要素は{@link DataContainer#data data} 配列において、[{@link DataContainer#offset offset}]番目を先頭として並ぶため、
	 * この値は必ずしも {@link DataContainer#data data} 配列の要素数そのものと一致はしません。
	 */
	private int size;


	/**
	 * 多次元配列の各次元の長さを表す配列です。
	 *
	 * 実行プログラムのソースコードにおける、多次元配列の宣言の要素数との対応では、
	 * この[0]番要素が左端次元の要素数、[1]番要素はその1つ右の次元の要素数に一致します。
	 * （つまり、int a[10][20][30]; と宣言された多次元配列に対して、このフィールドの[0]番要素は10、[1]番要素は20です。）
	 *
	 * なお、データがスカラ（単一要素の0次元配列）の場合には要素なしとなります。
	 */
	private int[] lengths;


	/**
	 * {@link DataContainer#data data} 配列内において、
	 * データの先頭要素が格納されている位置（オフセット値）を示します。
	 *
	 * 特に、このデータコンテナが、配列変数の要素（必ずスカラー）を表現する際に使用されます。
	 * そのような場面では、{@link DataContainer#data data} フィールドに配列変数のデータが格納され、
	 * このオフセット値に対象要素の（{@link DataContainer#data data} 配列内の）インデックスが保持されます。
	 */
	private int offset;


	/**
	 * スカラ（単一要素の0次元配列）のデータを保持するためのデフォルトの設定値で、データコンテナを生成します。
	 *
	 * データの保持領域が確保されるわけではないため、保持させるデータは外部で用意し、
	 * {@link DataContainer#setData(Object) set} メソッドを使用して渡す必要があります。
	 *
	 * なお、スカラではない（1次元以上の）多次元配列データを保持させる場合は、追加で
	 * {@link DataContainer#setSize(int) setSize} および {@link DataContainer#setLengths(int[]) setLengths}
	 * メソッドを使用し、総要素数と各次元の長さを設定する必要があります。
	 */
	public DataContainer() {
		this.initialize();
	}


	/**
	 * 生成直後のデフォルトの状態に初期化します。
	 * 初期化直後は、スカラ（単一要素の0次元配列）のデータを保持する設定がされた状態となります。
	 */
	public void initialize() {
		this.data = null;
		this.size = SIZE_OF_SCALAR;
		this.lengths = LENGTHS_OF_SCALAR;
		this.offset = 0;
		this.dataType = DataType.VOID;
	}


	/**
	 * このデータコンテナにデータを格納します。
	 *
	 * Vnano処理系では全てのデータを配列として扱うため、引数 data は配列型である必要があります。
	 * ただし、多次元配列やスカラのデータは 1 次元化して渡す必要があります。
	 * 詳細はこのクラスの説明を参照してください。
	 *
	 * @param data 格納するデータ
	 */
	//public final void setData(T data) throws DataException { // ArrayDataContainer1の仕様上DataExceptionはスロー不可
	public final void setData(T data) {

		// データの格納
		this.data = data;

		// 以下、データ型の設定

		// 格納するデータが無い場合はVOID型にする
		if (this.data == null) {
			this.dataType = DataType.VOID;
			return;
		}

		// データのクラスからこの処理系でのデータ型へ変換して設定
		this.dataType = CLASS_DATA_TYPE_MAP.get(this.data.getClass());

		// 未知の型の場合もVOID型にする（こちらはUNKNOWNなどの別の型を追加すべきかもしれない）
		if (this.dataType == null) {
			this.dataType = DataType.VOID;
		}
	}


	/**
	 * このデータコンテナが格納するデータを、次元ごとの長さ情報と共に設定します。
	 * 同時に {@link DataContainer#size size} フィールドの値も、全次元の長さの積に設定されます。
	 *
	 * @param data 格納するデータ
	 * @param arrayLengths 次元ごとの長さを格納する配列
	 */
	public final void setData(T data, int[] lengths) {
		this.setData(data);
		this.lengths = lengths;
		int dataSize = 1;
		for (int length: lengths) {
			dataSize *= length;
		}
		this.size = dataSize;
	}


	/**
	 * このデータコンテナが格納するデータをスカラ値と見なしたい場合において、
	 * そのスカラ値を中に含んでいる配列データと、
	 * その中でスカラ値が保持されているオフセット値を指定します。
	 * 同時に {@link DataContainer#size size} フィールドや {@link DataContainer#lengths lengths}
	 * フィールドも、スカラ値用の値に設定されます。
	 *
	 * @param data 格納するデータ
	 * @param offset オフセット値（データ内でスカラ値が存在する配列インデックス）
	 */
	public final void setData(T data, int offset) {
		this.setData(data);
		this.offset = offset;
		this.lengths = LENGTHS_OF_SCALAR;
		this.size = SIZE_OF_SCALAR;
	}


	/**
	 * このデータコンテナが格納しているデータを取得します。
	 *
	 * ただし、多次元配列やスカラのデータは 1 次元化して返されます。
	 * 詳細はこのクラスの説明を参照してください。
	 *
	 * @return data 保持しているデータ
	 */
	public final T getData() {
		return this.data;
	}


	/**
	 * スカラ値を保持している場合のオフセット値を取得します。
	 *
	 * オフセット値とは、このデータコンテナの格納対象データが、
	 * {@link DataContainer#data data} 配列内で格納されている領域の先頭インデックスを意味します。
	 * 詳細はこのクラスの説明を参照してください。
	 *
	 * @return オフセット値
	 */
	public int getOffset() {
		return this.offset;
	}


	/**
	 * このデータコンテナが格納するデータのサイズを取得します。
	 *
	 * ここでのサイズとは、このデータコンテナが格納対象とするデータの総要素数の事です。
	 * 具体的には、データがスカラではない場合には、
	 * サイズは多次元配列の総要素数、即ち各次元長の積に一致します。
	 * データがスカラである場合には、サイズは常に 1 となります。
	 * 例えば、{@link DataContainer#data} 配列の要素数が 1 よりもずっと大きく、
	 * その配列内に要素として（オフセット値で指定される位置に）格納対象のスカラ値が保持されている場合でも、
	 * サイズは 1 になります。
	 *
	 * @return データの総要素数
	 */
	public final int getSize() {
		return this.size;
	}


	/**
	 * このデータコンテナが保持するデータの、
	 * 多次元配列における各次元ごとの次元長（要素数）を、配列にまとめて設定します。
	 *
	 * スクリプトコード側での多次元配列との対応では、左端次元の要素数を[0]番要素、
	 * その一つ右隣りにある次元の要素数を[1]番要素 ... という順で配列に格納し、
	 * 引数に渡してください。
	 *
	 * データがスカラ値の場合は、0次元の配列と見なし、要素数 0 の配列が返されます。
	 *
	 * @return 各次元の次元長を格納する配列
	 */
	public final int[] getLengths() {
		return this.lengths;
	}


	/**
	 * このデータコンテナが保持するデータの、多次元配列における次元数を取得します。
	 * これは、スクリプトコード側での多次元配列の次元数と一致します。
	 * データがスカラ値の場合は、次元数 0 の配列と見なし、0 が返されます。
	 *
	 * @return 各次元の次元長を格納する配列
	 */
	public final int getRank() {
		return this.lengths.length;
	}


	/**
	 * このデータコンテナが格納しているデータの型を返します。
	 * 何もデータを格納していない場合は {@link org.vcssl.nano.spec.DataType#VOID VOID} を返します。
	 *
	 * @return データ型
	 */
	public final DataType getDataType() {
		return this.dataType;
	}

}
