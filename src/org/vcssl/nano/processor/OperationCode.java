/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.processor;

/**
 * <p>
 * 仮想プロセッサにおける、命令のオペレーションコードが定義された列挙子です。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public enum OperationCode {

	/**
	 * 加算命令です。
	 *
	 * 中間アセンブリコード内での構文は「 ADD type output inputA inputB; 」です。
	 * この命令の実行により、inputA のデータと inputB のデータが加算され、
	 * 結果が output のデータに格納されます。
	 * <br >
	 * この命令はベクトル演算命令であり、全オペランドの配列要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 * また、全オペランドのデータ型は揃っている必要があり、
	 * そのデータ型を type に指定します。
	 */
	ADD,


	/**
	 * 減算命令です。
	 *
	 * 中間アセンブリコード内での構文は「 SUB type output inputA inputB; 」です。
	 * この命令の実行により、inputA のデータと inputB のデータが減算され、
	 * 結果が output のデータに格納されます。
	 * <br >
	 * この命令はベクトル演算命令であり、全オペランド（outputを含む）
	 * の配列要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 * また、全オペランドのデータ型は揃っている必要があり、
	 * そのデータ型を type に指定します。
	 */
	SUB,


	/**
	 * 乗算命令です。
	 *
	 * 中間アセンブリコード内での構文は「 SUB type output inputA inputB; 」です。
	 * この命令の実行により、inputA のデータと inputB のデータが乗算され、
	 * 結果が output のデータに格納されます。
	 * <br >
	 * この命令はベクトル演算命令であり、全オペランド（outputを含む）
	 * の配列要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 * また、全オペランドのデータ型は揃っている必要があり、
	 * そのデータ型を type に指定します。
	 */
	MUL,


	/**
	 * 除算命令です。
	 *
	 * 中間アセンブリコード内での構文は「 MUL type output inputA inputB; 」です。
	 * この命令の実行により、inputA のデータが inputB のデータで除算され、
	 * 結果が output のデータに格納されます。
	 * <br >
	 * この命令はベクトル演算命令であり、全オペランド（outputを含む）
	 * の配列要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 * また、全オペランドのデータ型は揃っている必要があり、
	 * そのデータ型を type に指定します。
	 */
	DIV,


	/**
	 * 剰余算命令です。
	 *
	 * 中間アセンブリコード内での構文は「 DIV type output inputA inputB; 」です。
	 * この命令の実行により、inputA のデータを、inputB のデータで除算した剰余が計算され、
	 * 結果が output のデータに格納されます。
	 * <br >
	 * この命令はベクトル演算命令であり、全オペランド（outputを含む）
	 * の配列要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 * また、全オペランドのデータ型は揃っている必要があり、
	 * そのデータ型を type に指定します。
	 */
	REM,


	/**
	 * 符号反転命令です。
	 *
	 * 中間アセンブリコード内での構文は「 NEG type output input; 」です。
	 * この命令の実行により、input のデータの符号反転を行った結果が、
	 * output のデータに格納されます。
	 * <br >
	 * この命令はベクトル演算命令であり、全オペランド（outputを含む）
	 * の配列要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 * また、全オペランドのデータ型は揃っている必要があり、
	 * そのデータ型を type に指定します。
	 */
	NEG,


	/**
	 * 等値比較命令です。
	 *
	 * 中間アセンブリコード内での構文は「 EQ type output inputA inputB; 」です。
	 * この命令の実行により、inputA のデータと、inputB のデータの等値比較が行われ、
	 * 結果が output のデータに格納されます。
	 * 演算結果は、inputA および inputB が一致している場合に true、一致しない場合に false となります。
	 * <br >
	 * この命令はベクトル演算命令であり、全オペランド（outputを含む）
	 * の配列要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 * また、inputA および inputB のデータ型は揃っている必要があり、
	 * そのデータ型を type に指定します。
	 * output のデータ型は必ず {@link DataType#BOOL BOOL} 型）である必要があります。
	 */
	EQ,


	/**
	 * 非等値比較命令です。
	 *
	 * 中間アセンブリコード内での構文は「 NEQ type output inputA inputB; 」です。
	 * この命令の実行により、inputA のデータと、inputB のデータの非等値比較が行われ、
	 * 結果が output のデータに格納されます。
	 * 演算結果は、inputA および inputB が一致しない場合に true、一致する場合に false となります。
	 * <br >
	 * この命令はベクトル演算命令であり、全オペランド（outputを含む）
	 * の配列要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 * また、inputA および inputB のデータ型は揃っている必要があり、
	 * そのデータ型を type に指定します。
	 * output のデータ型は必ず {@link DataType#BOOL BOOL} 型である必要があります。
	 */
	NEQ,


	/**
	 * 大なり比較命令です。
	 *
	 * 中間アセンブリコード内での構文は「 GT type output inputA inputB; 」です。
	 * この命令の実行により、inputA のデータと inputB のデータの大なり比較が行われ、
	 * 結果が output のデータに格納されます。
	 * 演算結果は、inputA のデータの値が、inputB のものより大きい（等値を含まない）場合に true、
	 * そうでしない場合に false となります。
	 * <br >
	 * この命令はベクトル演算命令であり、全オペランド（outputを含む）
	 * の配列要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 * また、inputA および inputB のデータ型は揃っている必要があり、
	 * そのデータ型を type に指定します。
	 * output のデータ型は必ず {@link DataType#BOOL BOOL} 型である必要があります。
	 */
	GT,


	/**
	 * 大なり比較命令です。
	 *
	 * 中間アセンブリコード内での構文は「 LT type output inputA inputB; 」です。
	 * この命令の実行により、inputA のデータと inputB のデータの小なり比較が行われ、
	 * 結果が output のデータに格納されます。
	 * 演算結果は、inputA のデータの値が、inputB のものより小さい（等値を含まない）場合に true、
	 * そうでしない場合に false となります。
	 * <br >
	 * この命令はベクトル演算命令であり、全オペランド（outputを含む）
	 * の配列要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 * また、inputA および inputB のデータ型は揃っている必要があり、
	 * そのデータ型を type に指定します。
	 * output のデータ型は必ず {@link DataType#BOOL BOOL} 型である必要があります。
	 */
	LT,


	/**
	 * 大なり等値比較命令です。
	 *
	 * 中間アセンブリコード内での構文は「 GEQ type output inputA inputB; 」です。
	 * この命令の実行により、inputA のデータと inputB のデータの大なり等値比較が行われ、
	 * 結果が output のデータに格納されます。
	 * 演算結果は、inputA のデータの値が、inputB のものより大きいか等しい場合に true、
	 * そうでしない場合に false となります。
	 * <br >
	 * この命令はベクトル演算命令であり、全オペランド（outputを含む）
	 * の配列要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 * また、inputA および inputB のデータ型は揃っている必要があり、
	 * そのデータ型を type に指定します。
	 * output のデータ型は必ず {@link DataType#BOOL BOOL} 型である必要があります。
	 */
	GEQ,


	/**
	 * 小なり等値比較命令です。
	 *
	 * 中間アセンブリコード内での構文は「 LEQ type output inputA inputB; 」です。
	 * この命令の実行により、inputA のデータと inputB のデータの小なり等値比較が行われ、
	 * 結果が output のデータに格納されます。
	 * 演算結果は、inputA のデータの値が、inputB のものより小さいか等しい場合に true、
	 * そうでしない場合に false となります。
	 * <br >
	 * この命令はベクトル演算命令であり、全オペランド（outputを含む）
	 * の配列要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 * また、inputA および inputB のデータ型は揃っている必要があり、
	 * そのデータ型を type に指定します。
	 * output のデータ型は必ず {@link DataType#BOOL BOOL} 型である必要があります。
	 */
	LEQ,


	/**
	 * 論理積命令です。
	 *
	 * 中間アセンブリコード内での構文は「 AND type output inputA inputB; 」です。
	 * この命令の実行により、inputA のデータと inputB のデータの論理積が計算が行われ、
	 * 結果が output のデータに格納されます。
	 * 演算結果は、inputA のデータの値と、inputB のデータの値が両者共に true である場合に true、
	 * そうでない場合に false となります。
	 * <br >
	 * この命令はベクトル演算命令であり、全オペランド（outputを含む）
	 * の配列要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 * また、全オペランドのデータ型は全て {@link DataType#BOOL BOOL} 型に揃っている必要があり、
	 * type にも BOOL が指定されている必要があります。
	 */
	AND,


	/**
	 * 論理和命令です。
	 *
	 * 中間アセンブリコード内での構文は「 OR type output inputA inputB; 」です。
	 * この命令の実行により、inputA のデータと inputB のデータの論理和が計算が行われ、
	 * 結果が output のデータに格納されます。
	 * 演算結果は、inputA のデータの値と、inputB のデータの値の、 少なくとも片方が true
	 * である場合に true、両者とも false である場合に false となります。
	 * <br >
	 * この命令はベクトル演算命令であり、全オペランド（outputを含む）
	 * の配列要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 * また、全オペランドのデータ型は全て {@link DataType#BOOL BOOL} 型に揃っている必要があり、
	 * type にも BOOL が指定されている必要があります。
	 */
	OR,


	/**
	 * 論理否定命令です。
	 *
	 * 中間アセンブリコード内での構文は「 NOT type output input; 」です。
	 * この命令の実行により、input のデータの論理否定値が計算され、
	 * 結果が output のデータに格納されます。
	 * 演算結果は、input のデータの値が true である場合に false、
	 * 逆に false である場合に true となります。
	 * <br >
	 * この命令はベクトル演算命令であり、全オペランド（outputを含む）
	 * の配列要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 * また、全オペランドのデータ型は全て {@link DataType#BOOL BOOL} 型に揃っている必要があり、
	 * type にも BOOL が指定されている必要があります。
	 */
	NOT,


	/**
	 * 代入命令です。
	 *
	 * 中間アセンブリコード内での構文は「 MOV type output input; 」です。
	 * この命令の実行により、input のデータの複製値が、
	 * output のデータに代入されます。
	 * <br >
	 * この命令はベクトル演算命令であり、全オペランド（outputを含む）
	 * の配列要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 * また、全オペランドのデータ型は、
	 * type に指定されたものに揃っている必要があります。
	 */
	MOV,


	/**
	 * メモリー確保命令です。
	 *
	 * この命令には、中間アセンブリコード内で、オペランド指定が異なる3通りの構文あります。
	 * <br >
	 * 1つめは「 ALLOC type target; 」で、これは実行により、
	 * target がスカラ値（0次元かつ要素数1の配列）を保持できるようにメモリーが確保されます。
	 * <br >
	 * 2つめは「 ALLOC type target size; 」で、これは実行により、
	 * target が要素数 size の1次元配列を保持できるようにメモリーが確保されます。
	 * <br >
	 * 3つめは「 ALLOC type target size lengths; 」で、これは実行によって target が、
	 * 総要素数 size の多次元配列を保持できるようにメモリーが確保され、
	 * その各次元ごとの要素数は、1次元配列オペランド lengths の各要素値によって指定されます。
	 * <br >
	 * 上記3通りの全ての場合において、type に target のデータ型を指定する必要があります
	 * （この命令の実行によって、はじめて target のデータに型付けが行われます）。
	 * size および lengths のデータ型は必ず {@link DataType#INT64 INT64} 型である必要があります。
	 */
	ALLOC,


	/**
	 * メモリー解放命令です。
	 *
	 * 中間アセンブリコード内での構文は「 FREE type target; 」です。
	 * この命令の実行により、target のデータを保持するメモリー領域が解放されます。
	 * type には target のデータ型を指定します。
	 */
	FREE,


	/**
	 * 型変換命令です。
	 *
	 * 中間アセンブリコード内での構文は「 CAST toType:fromType output input; 」です。
	 * この命令の実行により、inputのデータが型変換されつつ、outputのデータに代入されます。
	 * input のデータ型を fromType に、output のデータ型を toType に指定します。
	 * <br >
	 * この命令はベクトル演算命令であり、全オペランド（outputを含む）
	 * の配列要素数が同一に揃っている必要があります
	 * （スカラは要素数1かつ0次元の配列として演算されます）。
	 */
	CAST,


	/**
	 * （暫定）整列代入命令です。
	 */
	REORD,


	/**
	 * （暫定）全要素代入命令です。
	 */
	FILL,


	/**
	 * （暫定）配列化命令です。
	 */
	VEC, // 複数のスカラを1次元の配列にする。INDEXの引数を1-clockで作るため。あと配列初期化子に使えるかも。


	/**
	 * （暫定）要素参照命令です。
	 */
	ELEM, // REFERの亜種。要素refer。data は同一参照化するが、index 値を設定する。そうするとエンジン側で要素referと解釈される。配列要素の参照渡し仮引数にそのまま渡せる。値渡しの場合は CAST して渡す必要がある。


	/**
	 * （暫定）次元数取得命令です。
	 */
	//RANK,


	/**
	 * （暫定）要素数取得命令です。
	 */
	LEN,


	/**
	 * 真値分岐命令です。
	 */
	JMP, // オペランドが true の場合に飛ぶ。else などで使用。あと命名規則的にもJMPNのNじゃない版として必要。


	/**
	 * 偽値分岐命令です。
	 */
	JMPN, // オペランドがfalse時の飛ぶ。普通の利用場面ではこちらの方が多い。


	/**
	 * 関数呼び出し命令です。
	 */
	CALL,


	/**
	 * （暫定）戻り命令です。
	 */
	//RET,


	/**
	 * 何も行わない命令です。
	 * ただし、処理系内部では、他の命令と同様に、命令ディスパッチの段階までの処理は行われます。
	 * そこでディスパッチされる処理が、何もしない内容になっています。
	 * 処理系の高速化などにおいて、命令ディスパッチの段階までの処理コストを見積もるためなどに使用されます。
	 */
	NOP,

}