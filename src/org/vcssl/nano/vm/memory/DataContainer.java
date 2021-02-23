/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.memory;

import java.util.HashMap;

import org.vcssl.connect.ArrayDataAccessorInterface1;
import org.vcssl.connect.BoolScalarDataAccessorInterface1;
import org.vcssl.connect.Float64ScalarDataAccessorInterface1;
import org.vcssl.connect.Int64ScalarDataAccessorInterface1;
import org.vcssl.connect.StringScalarDataAccessorInterface1;
import org.vcssl.nano.VnanoFatalException;
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
 * 格納データを設定する {@link DataContainer#setArrayData(Object, int, int[]) setArrayData(Object,int,int[])}
 * メソッドには、上記のように1次元化した配列を設定する必要があります。
 * 格納データを取得する {@link DataContainer#getArrayData getArrayData()} メソッドも、
 * 1次元化された配列を返します。
 * これらの getter / setter は、{@link DataContainer#data data} フィールドをそのまま設定・取得します。
 * コピーやアライメント調整などは行われません。
 * </p>
 *
 * <p>
 * 1次元化されたデータが、多次元配列のどの要素に対応するかを計算で求めるためには、
 * 多次元配列の各次元ごとの長さ（次元長）が必要ですが、
 * それは {@link DataContainer#lengths lengths} フィールドに保持され、
 * {@link DataContainer#getArrayLengths getArrayLengths()} メソッドによって取得できます
 * （左端次元の長さが [0] 番要素となります）。
 * 配列次元数は、この次元長配列 lengths の要素数、
 * 即ち {@link DataContainer#getArrayLengths getArrayLengths()}.length によって求められますが、
 * 可読性のために {@link DataContainer#getArrayRank getArrayRank()} メソッドによって得る事もできます。
 * 多次元配列の総要素数は {@link DataContainer#size size} フィールドに保持され、
 * {@link DataContainer#getArraySize getArraySize()} メソッドによって取得できます。
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
 * {@link DataContainer#getArrayRank getArrayRank()} メソッドは 0 を返し、
 * {@link DataContainer#getArrayLengths getArrayLengths()} メソッドは要素数0の配列を返します。
 * ただし、スカラは0次元であっても値を1個持っているため、
 * データの総要素数を表す {@link DataContainer#size size} フィールドの値は 1 となり、
 * {@link DataContainer#getArraySize getArraySize()} メソッドも 1 を返します。
 * </p>
 *
 * <p>
 * スカラデータもまた、内部では1次元化した配列として
 * {@link DataContainer#data data} フィールドに保持されます。
 * ただし、要素数 1 の配列に格納されるとは限らず、
 * より長い配列のどこかに格納される場合もあります。
 * その格納位置のインデックスは、
 * {@link DataContainer#offset offset} フィールドによって保持され、
 * {@link DataContainer#getArrayOffset getArrayOffset()} メソッドによって取得できます。
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
 * {@link org.vcssl.connect.ArrayDataAccessorInterface1 ADAI 1}
 * 形式のデータコンテナ・インターフェースを実装しています。
 * {@link org.vcssl.connect.ExternalVariableConnectorInterface1 XVCI 1} 形式や
 * {@link org.vcssl.connect.ExternalFunctionConnectorInterface1 XFCI 1} 形式のプラグインにおいて、
 * 自動データ変換機能を無効化した上でVnano処理系に接続した場合、
 * このデータコンテナが無変換で直接受け渡しされるようになります。
 * プラグイン側において、処理系の実装への依存度を抑えたい場合、
 * {@link org.vcssl.connect.ArrayDataAccessorInterface1 ADAI 1}
 * のインターフェースに定義されたメソッドを通してアクセスする事ができます。
 * </p>
 *
 * @param <T> 保持するデータの型
 * @author RINEARN (Fumihiro Matsui)
 */
public class DataContainer<T> implements ArrayDataAccessorInterface1<T>,
		Float64ScalarDataAccessorInterface1, Int64ScalarDataAccessorInterface1,
		BoolScalarDataAccessorInterface1, StringScalarDataAccessorInterface1 {

	/** スカラデータを格納する場合における、多次元配列としての次元数（値は0）です。*/
	public static final int   ARRAY_RANK_OF_SCALAR = 0;

	/** スカラデータを格納する場合における、多次元配列としての各次元の長さを表す配列（値は要素無しの int[0]）です。*/
	public static final int[] ARRAY_LENGTHS_OF_SCALAR = { };

	/** スカラデータを格納する場合における、データの総要素数（値は1）です。 */
	public static final int   ARRAY_SIZE_OF_SCALAR = 1;


	/** 引数 data に格納するデータのクラスと、{@link org.vcssl.nano.spec.DataType DataType} 列挙子の要素との対応関係を表すマップです。 */
	private static final HashMap<Class<?>, DataType> CLASS_DATA_TYPE_MAP = new HashMap<Class<?>, DataType>();
	static {
		CLASS_DATA_TYPE_MAP.put(long[].class, DataType.INT64);
		CLASS_DATA_TYPE_MAP.put(double[].class, DataType.FLOAT64);
		CLASS_DATA_TYPE_MAP.put(boolean[].class, DataType.BOOL);
		CLASS_DATA_TYPE_MAP.put(String[].class, DataType.STRING);
	}


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
	// 注意: alloc 等で要素数配列の内容を変える時は、そのまま代入せずに new で参照を切る事 (同要素数の複数コンテナで共用されている場合がある)
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
	 * このデータコンテナが、別のデータコンテナと参照を共有する場合に、
	 * 参照ツリーのルート（根）に位置するデータコンテナを控えます。
	 */
	private DataContainer<T> referenceTreeRoot;


	/**
	 * スカラ（単一要素の0次元配列）のデータを保持するためのデフォルトの設定値で、データコンテナを生成します。
	 *
	 * データの保持領域が確保されるわけではないため、保持させるデータは外部で用意し、
	 * {@link DataContainer#setArrayData(Object, int, int[]) setArrayData(Object,int,int[])} メソッドを使用して渡す必要があります。
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
		this.referenceTreeRoot = null;
		this.size = ARRAY_SIZE_OF_SCALAR;
		this.lengths = ARRAY_LENGTHS_OF_SCALAR;
		this.offset = 0;
	}


	/**
	 * このデータコンテナが格納するデータを、そのデータに関する必須情報（オフセット値、各次元の長さ）と共に設定します。
	 *
	 * Vnano処理系では全てのデータを配列として扱うため、引数 data は配列型である必要があります。
	 * 多次元配列やスカラのデータも、1 次元配列として格納する必要があります。
	 * 詳細はこのクラスの説明を参照してください。
	 *
	 * なお、引数 lengths に関しては、別のデータコンテナに対して {@link DataContainer#getArrayLengths() getLengths()}
	 * メソッドで取得した要素数配列をそのまま（使い回して）渡した方が、手短で高効率な場面がしばしばあります。
	 * しかしこれは、「 取得した要素数配列の格納値を変更しなくても済む場合 」のみ可能です。
	 * なぜなら、そのようにして複数のデータコンテナが要素数配列の参照を共有している状態で、
	 * どこかで要素数配列の格納値を変更すると、それを参照する全てのデータコンテナの要素数情報が変わり、不整合が生じるためです。
	 * 従って、同内容の要素数配列をどこかから取得できない場合は、引数 length には新規に生成した配列を渡してください。
	 *
	 * @param data 格納するデータ（1次元配列）
	 * @param offset オフセット値（データ内で値が格納されている要素のインデックスで、スカラ以外を格納する場合は常に 0 を指定します）
	 * @param arrayLengths 各次元ごとの長さを格納する配列（スカラを格納する場合は、長さ 0 の配列を指定します）
	 */
	@Override
	public final void setArrayData(T data, int offset, int[] lengths) {

		// 各次元の長さの積（= size）の値を求める
		int productOfLengths = 1;
		for (int length: lengths) {
			productOfLengths *= length;
		}

		// 別のコンテナを参照していない場合： このコンテナの情報を更新
		if (this.referenceTreeRoot == null) {
			this.data = data;
			this.offset = offset;
			this.lengths = lengths;
			this.size = productOfLengths;

		// 別のコンテナを参照している場合： 参照ツリーのルートは必ず実データを持っているはずなので、その情報を更新
		} else {
			// ※ ここで referenceTreeRoot.setArrayData(...) を呼ぶと一階層でも再帰コールになってしまうので注意
			this.referenceTreeRoot.data = data;
			this.referenceTreeRoot.offset = offset;
			this.referenceTreeRoot.lengths = lengths;
			this.referenceTreeRoot.size = productOfLengths;
		}
	}


	/**
	 * このデータコンテナが格納しているデータを取得します。
	 *
	 * ただし、多次元配列やスカラのデータは 1 次元化して返されます。
	 * 詳細はこのクラスの説明を参照してください。
	 *
	 * @return data 保持しているデータ
	 */
	@Override
	public final T getArrayData() {
		return (this.referenceTreeRoot == null) ? this.data : this.referenceTreeRoot.data;
	}


	/**
	 * このデータコンテナが、配列データを格納しているかどうかを返します。
	 *
	 * なお、このデータコンテナは、スカラも0次元かつ要素数1の配列データと見なして扱うため、
	 * 格納しているデータがスカラであっても true が返されます。
	 * このメソッドが true を返す場合に、格納しているデータがスカラかどうかを確認するには、
	 * 併せて {@link DataContainer#getArrayRank() getArrayRank()} を呼び出し、
	 * そのメソッドの戻り値が 0 かどうかによって確認してください。
	 *
	 * @return 格納していれば true
	 */
	@Override
	public final boolean hasArrayData() {
		if (this.referenceTreeRoot == null) {
			return this.data != null;
		} else {
			return this.referenceTreeRoot.data != null;
		}
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
	@Override
	public int getArrayOffset() {
		return (this.referenceTreeRoot == null) ? this.offset : this.referenceTreeRoot.offset;
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
	@Override
	public final int getArraySize() {
		return (this.referenceTreeRoot == null) ? this.size : this.referenceTreeRoot.size;
	}


	/**
	 * このデータコンテナが保持するデータの、
	 * 多次元配列における各次元ごとの次元長（要素数）を、配列にまとめて取得します。
	 *
	 * スクリプトコード側での多次元配列との対応では、左端次元の要素数を[0]番要素、
	 * その一つ右隣りにある次元の要素数を[1]番要素 ... という順で配列に格納されています。
	 * データがスカラ値の場合は、0次元の配列と見なし、要素数 0 の配列が返されます。
	 *
	 * なお、このメソッドで取得した要素数配列の格納値は、変更しないでください。
	 * 要素数情報を変更したい場合は、
	 * {@link DataContainer#setArrayData(Object, int, int[]) setArrayData(Object data, int offset, int[] lengths)}
	 * メソッドを使用して、データと共に再設定してください。
	 *
	 * @return 各次元の次元長を格納する配列
	 */
	@Override
	public final int[] getArrayLengths() {
		return (this.referenceTreeRoot == null) ? this.lengths : this.referenceTreeRoot.lengths;
	}


	/**
	 * このデータコンテナが保持するデータの、多次元配列における次元数を取得します。
	 * これは、スクリプトコード側での多次元配列の次元数と一致します。
	 * データがスカラ値の場合は、次元数 0 の配列と見なし、0 が返されます。
	 *
	 * @return 各次元の次元長を格納する配列
	 */
	@Override
	public final int getArrayRank() {
		return (this.referenceTreeRoot == null) ? this.lengths.length : this.referenceTreeRoot.lengths.length;
	}


	/**
	 * このデータコンテナが格納しているデータの型を返します。
	 * 何もデータを格納していない場合は {@link org.vcssl.nano.spec.DataType#VOID VOID} を返します。
	 *
	 * @return データ型
	 */
	public final DataType getDataType() {

		// 格納しているデータのクラスを、データ型との対応関係を保持するハッシュマップに投げて、型を判定する
		T storingData = (this.referenceTreeRoot == null) ? this.data : this.referenceTreeRoot.data;
		DataType dataType = DataType.VOID;
		if (storingData != null) {
			dataType = CLASS_DATA_TYPE_MAP.get(storingData.getClass());

			// 未知の型の場合もVOID型にする（こちらはUNKNOWNなどの別の型を追加すべきかもしれない）
			if (dataType == null) {
				dataType = DataType.VOID;
			}
		}

		return dataType;

		// この方式は、あらかじめ型情報をフィールドに控えて参照する方式よりも、
		// 毎回ハッシュテーブルを引く処理コストが多少かかるが、しかしながらこのメソッドは
		// 主にVMの実行前処理で呼ばれるもので、実行中にはほぼ呼ばれないので問題ない。
		// (VRILは命令自体が型情報を静的に持っているので、実行中に動的にオペランドの型情報を調べる必要はほぼ無い)

		// もし型情報をこのクラスのフィールドに控えて参照するようにする場合、
		// データの set と同時に set するのは実行時に頻繁に処理コストを食うので好ましくない（元々はそうだった）。
		// データと独立に set する場合は、例えば ALLOC/ELEM 命令でのデータ確保/参照変更時が考えられるが、
		// しかしALLOC/ELEM命令は実行頻度が高いので、やはり多少は実行時の処理コストを食ってしまう。
	}


	/**
	 * このデータコンテナの状態が、引数に渡されたデータコンテナの状態をそのまま参照するように設定します。
	 * ただし、引数のコンテナが多重参照状態（あるコンテナがさらに別のコンテナを参照している状態）である場合、
	 * 設定される参照先は、参照関係のツリー構造（参照ツリー）のルートに位置するコンテナとなります。
	 *
	 * このメソッドによる参照設定後には、
	 * このコンテナに対する状態取得操作は、参照先（多重参照の場合はそのルート）のコンテナの状態を返すようになります。
	 * これにより、参照ツリーに属する全てのコンテナにおいて、状態取得操作で同じ結果を得られるようになります。
	 * また、このコンテナに対する状態設定操作は、
	 * 参照ツリーに属する全てのコンテナに対する状態取得結果に反映されるようになります。
	 *
	 * このメソッドは、複数回呼び出す事で、参照先を変更できます。
	 * ただし、このメソッドによる参照設定時点よりも後に、参照先のコンテナが、
	 * 別のコンテナを参照するようになった場合、その事はこのコンテナの挙動には反映されません。
	 * 反映させるには、再度このメソッドでそのコンテナを参照先として再設定する必要があります。
	 *
	 * @param referencedDataContainer
	 */
	public void refer(DataContainer<T> referencedDataContainer) {
		this.referenceTreeRoot = referencedDataContainer;
		while (this.referenceTreeRoot.referenceTreeRoot != null) {
			this.referenceTreeRoot = this.referenceTreeRoot.referenceTreeRoot;
		}
	}


	/**
	 * このデータコンテナが、別のデータコンテナの状態を参照するよう設定されている場合、その設定を解除します。
	 */
	public void derefer() {
		this.referenceTreeRoot = null;
	}


	/**
	 * double ({@link org.vcssl.nano.spec.DataType#FLOAT64 FLOAT64}) 型のスカラ値を格納します。
	 *
	 * ただし、このデータコンテナのデータ ({@link DataContainer#data data} フィールド) が、
	 * あらかじめ double 型配列として初期化されているか、または null である必要があります。
	 * 別の型で初期化されている場合には例外が発生します。
	 *
	 * @param data 格納する値
	 * @throws VnanoFatalException
	 *		別の型のデータが格納されている場合にスローされます。
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setFloat64ScalarData(double data) {

		// このコンテナのデータ格納用配列を取得
		// 注: 別のコンテナを参照している場合もあるため、
		//     this.data に直接アクセスするのでは不十分で、getter/setterを通す
		Object arrayData = this.getArrayData();

		// データ格納用配列が既に確保されている場合は、そのままセットする
		if (arrayData instanceof double[]) {
			( (double[])arrayData )[ this.getArrayOffset() ] = data;

		// 未確保の場合は、スカラを格納できるだけの配列を確保してセットする
		} else if (arrayData == null) {
			try {
				T newArrayData = (T)( new double[]{ data } );
				this.setArrayData(newArrayData, 0, ARRAY_LENGTHS_OF_SCALAR);
			} catch (ClassCastException cce) {
				throw new VnanoFatalException("Data type is incorrect");
			}

		// それ以外の場合は、型が違うため格納できない
		// (そういう場合は setArrayData でデータ格納用配列そのものを差し替えないとだめ)
		} else {
			throw new VnanoFatalException("Data type is incorrect");
		}
	}


	/**
	 * 格納されている、double ({@link org.vcssl.nano.spec.DataType#FLOAT64 FLOAT64}) 型のスカラ値を取得します。
	 *
	 * @return 取得値
	 * @throws VnanoFatalException
	 *		別の型のデータが格納されているか、何も格納されていない場合にスローされます。
	 */
	@Override
	public double getFloat64ScalarData() {

		// このコンテナのデータ格納用配列を取得
		// 注: 別のコンテナを参照している場合もあるため、
		//     this.data に直接アクセスするのでは不十分で、getter/setterを通す
		Object arrayData = this.getArrayData();

		if (arrayData instanceof double[]) {
			return ( (double[])arrayData )[ this.getArrayOffset() ];
		} else if (arrayData == null) {
			throw new VnanoFatalException("No data is stored");
		} else {
			throw new VnanoFatalException("Data type is incorrect");
		}
	}


	/**
	 * このデータコンテナが、double ({@link org.vcssl.nano.spec.DataType#FLOAT64 FLOAT64})
	 * 型のスカラ値を格納しているかどうかを返します。
	 *
	 * @return 格納していれば true
	 */
	@Override
	public boolean hasFloat64ScalarData() {
		if (this.getArrayData() == null) {
			return false;
		}
		if (!(this.getArrayData() instanceof double[])) {
			return false;
		}
		if (this.getArrayRank() == ARRAY_RANK_OF_SCALAR) {
			return false;
		}
		return true;
	}


	/**
	 * long ({@link org.vcssl.nano.spec.DataType#INT64 INT64}) 型のスカラ値を格納します。
	 *
	 * ただし、このデータコンテナのデータ ({@link DataContainer#data data} フィールド) が、
	 * あらかじめ long 型配列として初期化されているか、または null である必要があります。
	 * 別の型で初期化されている場合には例外が発生します。
	 *
	 * @param data 格納する値
	 * @throws VnanoFatalException
	 *		別の型のデータが格納されている場合にスローされます。
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setInt64ScalarData(long data) {

		// このコンテナのデータ格納用配列を取得
		// 注: 別のコンテナを参照している場合もあるため、
		//     this.data に直接アクセスするのでは不十分で、getter/setterを通す
		Object arrayData = this.getArrayData();

		// データ格納用配列が既に確保されている場合は、そのままセットする
		if (arrayData instanceof long[]) {
			( (long[])arrayData )[ this.getArrayOffset() ] = data;

		// 未確保の場合は、スカラを格納できるだけの配列を確保してセットする
		} else if (arrayData == null) {
			try {
				T newArrayData = (T)( new long[]{ data } );
				this.setArrayData(newArrayData, 0, ARRAY_LENGTHS_OF_SCALAR);
			} catch (ClassCastException cce) {
				throw new VnanoFatalException("Data type is incorrect");
			}

		// それ以外の場合は、型が違うため格納できない
		// (そういう場合は setArrayData でデータ格納用配列そのものを差し替えないとだめ)
		} else {
			throw new VnanoFatalException("Data type is incorrect");
		}
	}


	/**
	 * 格納されている、long ({@link org.vcssl.nano.spec.DataType#INT64 INT64}) 型のスカラ値を取得します。
	 *
	 * @return 取得値
	 * @throws VnanoFatalException
	 *		別の型のデータが格納されているか、何も格納されていない場合にスローされます。
	 */
	@Override
	public long getInt64ScalarData() {

		// このコンテナのデータ格納用配列を取得
		// 注: 別のコンテナを参照している場合もあるため、
		//     this.data に直接アクセスするのでは不十分で、getter/setterを通す
		Object arrayData = this.getArrayData();

		if (arrayData instanceof long[]) {
			return ( (long[])arrayData )[ this.getArrayOffset() ];
		} else if (arrayData == null) {
			throw new VnanoFatalException("No data is stored");
		} else {
			throw new VnanoFatalException("Data type is incorrect");
		}
	}


	/**
	 * このデータコンテナが、long ({@link org.vcssl.nano.spec.DataType#INT64 INT64})
	 * 型のスカラ値を格納しているかどうかを返します。
	 *
	 * @return 格納していれば true
	 */
	@Override
	public boolean hasInt64ScalarData() {
		if (this.getArrayData() == null) {
			return false;
		}
		if (!(this.getArrayData() instanceof long[])) {
			return false;
		}
		if (this.getArrayRank() == ARRAY_RANK_OF_SCALAR) {
			return false;
		}
		return true;
	}


	/**
	 * boolean ({@link org.vcssl.nano.spec.DataType#BOOL BOOL}) 型のスカラ値を格納します。
	 * ただし、このデータコンテナのデータ ({@link DataContainer#data data} フィールド) が、
	 * あらかじめ boolean 型配列として初期化されているか、または null である必要があります。
	 * 別の型で初期化されている場合には例外が発生します。
	 *
	 * @param data 格納する値
	 * @throws VnanoFatalException
	 *		別の型のデータが格納されている場合にスローされます。
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setBoolScalarData(boolean data) {

		// このコンテナのデータ格納用配列を取得
		// 注: 別のコンテナを参照している場合もあるため、
		//     this.data に直接アクセスするのでは不十分で、getter/setterを通す
		Object arrayData = this.getArrayData();

		// データ格納用配列が既に確保されている場合は、そのままセットする
		if (arrayData instanceof boolean[]) {
			( (boolean[])arrayData )[ this.getArrayOffset() ] = data;

		// 未確保の場合は、スカラを格納できるだけの配列を確保してセットする
		} else if (arrayData == null) {
			try {
				T newArrayData = (T)( new boolean[]{ data } );
				this.setArrayData(newArrayData, 0, ARRAY_LENGTHS_OF_SCALAR);
			} catch (ClassCastException cce) {
				throw new VnanoFatalException("Data type is incorrect");
			}

		// それ以外の場合は、型が違うため格納できない
		// (そういう場合は setArrayData でデータ格納用配列そのものを差し替えないとだめ)
		} else {
			throw new VnanoFatalException("Data type is incorrect");
		}
	}


	/**
	 * 格納されている、boolean ({@link org.vcssl.nano.spec.DataType#BOOL BOOL}) 型のスカラ値を取得します。
	 *
	 * @return 取得値
	 * @throws VnanoFatalException
	 *		別の型のデータが格納されているか、何も格納されていない場合にスローされます。
	 */
	@Override
	public boolean getBoolScalarData() {

		// このコンテナのデータ格納用配列を取得
		// 注: 別のコンテナを参照している場合もあるため、
		//     this.data に直接アクセスするのでは不十分で、getter/setterを通す
		Object arrayData = this.getArrayData();

		if (arrayData instanceof boolean[]) {
			return ( (boolean[])arrayData )[ this.getArrayOffset() ];
		} else if (arrayData == null) {
			throw new VnanoFatalException("No data is stored");
		} else {
			throw new VnanoFatalException("Data type is incorrect");
		}
	}


	/**
	 * このデータコンテナが、boolean ({@link org.vcssl.nano.spec.DataType#BOOL BOOL})
	 * 型のスカラ値を格納しているかどうかを返します。
	 *
	 * @return 格納していれば true
	 */
	@Override
	public boolean hasBoolScalarData() {
		if (this.getArrayData() == null) {
			return false;
		}
		if (!(this.getArrayData() instanceof boolean[])) {
			return false;
		}
		if (this.getArrayRank() == ARRAY_RANK_OF_SCALAR) {
			return false;
		}
		return true;
	}


	/**
	 * String ({@link org.vcssl.nano.spec.DataType#STRING STRING}) 型のスカラ値を格納します。
	 * ただし、このデータコンテナのデータ ({@link DataContainer#data data} フィールド) が、
	 * あらかじめ String 型配列として初期化されているか、または null である必要があります。
	 * 別の型で初期化されている場合には例外が発生します。
	 *
	 * @param data 格納する値
	 * @throws VnanoFatalException
	 *		別の型のデータが格納されている場合にスローされます。
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setStringScalarData(String data) {

		// このコンテナのデータ格納用配列を取得
		// 注: 別のコンテナを参照している場合もあるため、
		//     this.data に直接アクセスするのでは不十分で、getter/setterを通す
		Object arrayData = this.getArrayData();

		// データ格納用配列が既に確保されている場合は、そのままセットする
		if (arrayData instanceof String[]) {
			( (String[])arrayData )[ this.getArrayOffset() ] = data;

		// 未確保の場合は、スカラを格納できるだけの配列を確保してセットする
		} else if (arrayData == null) {
			try {
				T newArrayData = (T)( new String[]{ data } );
				this.setArrayData(newArrayData, 0, ARRAY_LENGTHS_OF_SCALAR);
			} catch (ClassCastException cce) {
				throw new VnanoFatalException("Data type is incorrect");
			}

		// それ以外の場合は、型が違うため格納できない
		// (そういう場合は setArrayData でデータ格納用配列そのものを差し替えないとだめ)
		} else {
			throw new VnanoFatalException("Data type is incorrect");
		}
	}


	/**
	 * 格納されている、String ({@link org.vcssl.nano.spec.DataType#STRING STRING}) 型のスカラ値を取得します。
	 *
	 * @return 取得値
	 * @throws VnanoFatalException
	 *		別の型のデータが格納されているか、何も格納されていない場合にスローされます。
	 */
	@Override
	public String getStringScalarData() {

		// このコンテナのデータ格納用配列を取得
		// 注: 別のコンテナを参照している場合もあるため、
		//     this.data に直接アクセスするのでは不十分で、getter/setterを通す
		Object arrayData = this.getArrayData();

		if (arrayData instanceof String[]) {
			return ( (String[])arrayData )[ this.getArrayOffset() ];
		} else if (arrayData == null) {
			throw new VnanoFatalException("No data is stored");
		} else {
			throw new VnanoFatalException("Data type is incorrect");
		}
	}


	/**
	 * このデータコンテナが、String ({@link org.vcssl.nano.spec.DataType#STRING STRING})
	 * 型のスカラ値を格納しているかどうかを返します。
	 *
	 * @return 格納していれば true
	 */
	@Override
	public boolean hasStringScalarData() {
		if (this.getArrayData() == null) {
			return false;
		}
		if (!(this.getArrayData() instanceof String[])) {
			return false;
		}
		if (this.getArrayRank() == ARRAY_RANK_OF_SCALAR) {
			return false;
		}
		return true;
	}

}
