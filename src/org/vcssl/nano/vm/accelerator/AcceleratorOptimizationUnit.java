/*
 * Copyright(C) 2017-2020 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.accelerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.memory.DataContainer;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.processor.Instruction;

public class AcceleratorOptimizationUnit {

	// List 内の null 要素を removeAll する際に渡す (removeAllの引数は Collection インスタンスであるべきなので素の null は渡せない)
	private static final List<Object> LIST_OF_NULL = Arrays.asList((Object)null);

	// インライン展開を行う内部関数のコード長（命令数）の上限値。
	//   インライン展開は、最適化後のコード量の増大と引き換えに、呼び出しオーバーヘッド（短い関数の場合に相対的に無駄が大きい）を削る。
	//   そのため、展開する関数のコード長が大きいほど、デメリットは大きくなるのに、メリットは低下して、あまりうまみが無くなってしまう。
	//   従って、下記の値を超えるコード長の関数は、インライン展開の条件を満たしても、実際には展開されないようにしている。
	//   なお、以下の値は、引数の取り出し部やENDFUN命令は除く長さを設定する（それらは展開後のコードには現れないため）。
	private static final int MAX_INLINE_EXPANSIBLE_FUNCTION_CODE_LENGTH = 64;

	private List<AcceleratorInstruction> acceleratorInstructionList;
	private Map<Integer,Integer> addressReorderingMap;
	private Map<Integer,Integer> expandedAddressReorderingMap;
	private int registerWrittenPointCount[];
	private int registerReadPointCount[];
	private boolean registerReferenceMaybeLinked[];
	private Map<Integer, InternalFunctionInfo> functionInfoMap;
	private Set<Integer> unnecessaryRegisterSet;

	// 演算結果格納レジスタからのMOV命令でのコピーを削る最適化を、適用してもよい命令の集合
	// (算術演算命令などは可能、ELEM命令などのメモリー関連命令では不可能)
	private static final HashSet<OperationCode> movReducableOpcodeSet = new HashSet<OperationCode>();
	static {
		movReducableOpcodeSet.add(OperationCode.ADD);
		movReducableOpcodeSet.add(OperationCode.SUB);
		movReducableOpcodeSet.add(OperationCode.MUL);
		movReducableOpcodeSet.add(OperationCode.DIV);
		movReducableOpcodeSet.add(OperationCode.REM);
		movReducableOpcodeSet.add(OperationCode.NEG);

		movReducableOpcodeSet.add(OperationCode.EQ);
		movReducableOpcodeSet.add(OperationCode.NEQ);
		movReducableOpcodeSet.add(OperationCode.GT);
		movReducableOpcodeSet.add(OperationCode.LT);
		movReducableOpcodeSet.add(OperationCode.GEQ);
		movReducableOpcodeSet.add(OperationCode.LEQ);

		movReducableOpcodeSet.add(OperationCode.ANDM);
		movReducableOpcodeSet.add(OperationCode.ORM);
		movReducableOpcodeSet.add(OperationCode.NOT);
		movReducableOpcodeSet.add(OperationCode.CAST);

		movReducableOpcodeSet.add(OperationCode.MOVPOP); // MOVPOPでスタックから取って直後にコピーするだけのは削っても安全（REFPOPは無理）
		movReducableOpcodeSet.add(OperationCode.MOVELM); // MOVELMは要素の単純コピーなのでその直後にMOVするのは削っても安全（REFELMは無理）
	}

	private class InternalFunctionInfo {
		private int functionAddress = -1; // 関数の先頭アドレス
		private int bodyBeginAddress = -1; // 引数取り出し部やENDPRM命令を除いた、関数内部処理の先頭アドレス
		private int bodyEndAddress = -1;   // ENDFUN命令を除いた、関数内部処理の終端アドレス
		private int retCount = 0; // 関数内部におけるRET命令の数
		private int callCount = 0; // 関数内部におけるCALL命令の数
		private boolean isLastInstructionRet = false;
		private boolean hasReferenceParameters = false;
		private List<DataType> parameterDataTypeList = new ArrayList<DataType>();
		private List<Memory.Partition> parameterPartitionList = new ArrayList<Memory.Partition>(); // 仮引数変数の仮想メモリパーティション
		private List<Integer> parameterAddressList = new ArrayList<Integer>(); // 仮引数変数の仮想メモリアドレス
		private List<Boolean> parameterReferencenessList = new ArrayList<Boolean>(); // 参照渡しかどうか
		private List<Boolean> parameterScalarityList = new ArrayList<Boolean>(); // スカラかどうか
		private List<Integer> parameterRankList = new ArrayList<Integer>(); // 配列次元数
		private Memory.Partition lastReturnValuePartition = null;
		private int lastReturnValueAddress = -1;

		public InternalFunctionInfo(int functionAddress) {
			this.functionAddress = functionAddress;
		}

		public void setBodyBeginAddress(int functionBodyBeginAddress) {
			this.bodyBeginAddress = functionBodyBeginAddress;
		}

		public int getBodyBeginAddress() {
			return this.bodyBeginAddress;
		}

		public void setBodyEndAddress(int functionBodyEndAddress) {
			this.bodyEndAddress = functionBodyEndAddress;
		}

		public int getBodyEndAddress() {
			return this.bodyEndAddress;
		}

		public int incrementRetCount() {
			return this.retCount++;
		}

		public int getRetCount() {
			return this.retCount;
		}

		public void setLastRetValue(Memory.Partition partition, int address) {
			lastReturnValuePartition = partition;
			lastReturnValueAddress = address;
		}

		public boolean hasRetValue() {
			return this.lastReturnValuePartition != null;
		}

		public Memory.Partition getLastRetValuePartition() {
			return lastReturnValuePartition;
		}

		public int getLastRetValueAddress() {
			return lastReturnValueAddress;
		}

		public int incrementCallCount() {
			return this.callCount++;
		}

		public int getCallCount() {
			return this.callCount;
		}

		public boolean isLastInstructionRet() {
			return this.isLastInstructionRet;
		}
		public void setLastInstructionRet(boolean isLastInstructionRet) {
			this.isLastInstructionRet = isLastInstructionRet;
		}

		public void addParameter(DataType dataType, Memory.Partition partition, int address,
				boolean referenceness, boolean scalarity, int rank) {
			this.parameterDataTypeList.add(dataType);
			this.parameterPartitionList.add(partition);
			this.parameterAddressList.add(address);
			this.parameterReferencenessList.add(referenceness);
			this.parameterScalarityList.add(scalarity);
			this.parameterRankList.add(rank);
			if (referenceness) {
				this.hasReferenceParameters = true;
			}
		}

		// 引数は詰む順とは逆順でスタックから取り出されるので、
		// スタックからの取り出し部の命令列を読みながらaddParameterで追加していった場合、
		// 全引数の追加後にこのメソッドで順序を逆転させる必要がある
		public void reverseParameterOrder() {
			Collections.reverse(this.parameterDataTypeList);
			Collections.reverse(this.parameterPartitionList);
			Collections.reverse(this.parameterAddressList);
			Collections.reverse(this.parameterReferencenessList);
			Collections.reverse(this.parameterScalarityList);
			Collections.reverse(this.parameterRankList);
		}

		public DataType getParameterDataType(int parameterIndex) {
			return this.parameterDataTypeList.get(parameterIndex);
		}

		public Memory.Partition getParameterPertition(int parameterIndex) {
			return this.parameterPartitionList.get(parameterIndex);
		}

		public int getParameterAddress(int parameterIndex) {
			return this.parameterAddressList.get(parameterIndex);
		}

		public boolean isParameterReference(int parameterIndex) {
			return this.parameterReferencenessList.get(parameterIndex);
		}

		public boolean isParameterScalar(int parameterIndex) {
			return this.parameterScalarityList.get(parameterIndex);
		}

		public int getParameterRank(int parameterIndex) {
			return this.parameterRankList.get(parameterIndex);
		}

		public boolean hasReferenceParameters() {
			return this.hasReferenceParameters;
		}

		@Override
		public String toString() {
			int parameterLength = this.parameterAddressList.size();
			StringBuilder builder = new StringBuilder();
			builder.append("[ InternalFunctionInfo address=");
			builder.append(functionAddress);
			builder.append(" bodyBegin=");
			builder.append(bodyBeginAddress);
			builder.append(" bodyEnd=");
			builder.append(bodyEndAddress);
			builder.append(" param={ ");
			for (int i=0; i<parameterLength; i++) {
				if (this.parameterReferencenessList.get(i)) {
					builder.append('&');
				}
				builder.append(this.parameterPartitionList.get(i).toString().charAt(0));
				builder.append(this.parameterAddressList.get(i));
				builder.append(' ');
			}
			builder.append("} ]");
			return builder.toString();
		}
	}

	public AcceleratorInstruction[] optimize(
			AcceleratorInstruction[] instructions, Memory memory, AcceleratorDataManagementUnit dataManager) {

		// ※ 注意：
		//    現在の Accelerator の実装では、データの cacheability を変えるような最適化を行ってはならない。
		//    例えば、関数の仮引数を、インライン展開の際に実引数のアドレスで直接置き換えてしまう最適化は、
		//    単純に動作上の課題としては可能なケースが有り得るが、
		//    しかしそれは仮引数が uncacheabe で実引数が cacheable だった場合に、後者の cacheability を変えてしまう事に注意。
		//    ただし、レジスタへの冗長なMOV命令を削って、そのレジスタを他のどこからも使わなくなるような場合は問題は生じない
		//    (その場合は念のためそのレジスタのALLOCも削って、そのレジスタ自体がコードに登場しなくなるようにした方がいい)。

		this.acceleratorInstructionList = new ArrayList<AcceleratorInstruction>();
		for (AcceleratorInstruction instruction: instructions) {
			this.acceleratorInstructionList.add( instruction.clone() );
		}
		this.unnecessaryRegisterSet = new HashSet<Integer>();

		// 内部関数の最適化情報を抽出する（実際にCALLされているもののみ）
		this.extractInternalFunctionInfo(memory);

		// 内部関数呼び出しでの、スタックを介する引数の受け渡しを、呼び出し前に実引数から仮引数に直接代入するようにする
		// (命令アドレスがずれるため、this.functionInfoMap のbodyBegin/bodyEnd値も書き換わる)
		this.modifyCodeToTransferArgumentsDirectly(memory);

		// CALL命令の直後（アセンブル後はLABEL命令が置かれている）の箇所に、RETURNED命令(※)を生成して置き換える
		//（※ 参照渡し経由で関数処理で書き替えた値のキャッシュ同期などを行うAccelerator拡張命令）
		this.generateReturnedInstructions();

		// ここまでで命令のアドレスがずれた分の補正に使うため、最新の（再配列後の）命令アドレスを設定し、新旧の対応を保持するマップを生成
		this.updateReorderedAddresses();
		this.generateAddressReorderingMap();

		// 内部関数の最適化情報のアドレス類を更新（関数アドレスは実質的な識別子の意味を持つため変えず、bodyBegin と bodyEnd を変える）
		this.updateInternalFunctionInfo();

		// 条件を満たす関数のインライン展開を行う (先に modifyCodeToTransferArgumentsDirectly で引数転送の MOV/REF 化が必要)
		this.expandFunctionCodeInline(memory);

		// 全てのレジスタに対し、それぞれ書き込み・読み込み箇所の数をカウントする（最適化に使用）
		this.countRegisterWrittenPoints(memory);
		this.countRegisterReadPoints(memory);
		this.detectRegisterReferenceLinks(memory);

		// スカラのALLOC命令をコード先頭に並べ替える（メモリコストが低いので、ループ内に混ざるよりも利点が多い）
		this.reorderAllocAndAllocrInstructions(dataManager);

		// どこからも読んでいないレジスタに対するMOV命令を削る（後置インクリメント/デクリメントなどで発生）
		this.removeMovInstructionsToUnreadRegisters(dataManager);

		// 演算命令の結果を直後にMOVしている箇所のオペランドを並び替え、不要になったMOV命令を削る
		this.reduceMovInstructionsCopyingOperationResults(dataManager);

		// 上記のMOV削りによって使用されなくなったレジスタの確保処理を削る（そのレジスタはもうコード上で登場しなくなるはず）
		this.removeAllocInstructionsToUnusedRegisters();

		// 念のため上で削除したレジスタにアクセスしている箇所を探し、もし見つかればエラーにする（見つからなければ何もしない）
		this.checkRemovedRegistersAreUnused();

		// 最新の（再配列後の）命令アドレスを設定し、新旧の対応を保持するマップを更新
		// -> このあたりの処理は AcceleratorSchedulingUnit と重複しているので、きりのいい時になんとかしたい
		this.updateReorderedAddresses();
		this.generateAddressReorderingMap();

		// ジャンプ命令の飛び先アドレスを補正したものを求めて設定
		// -> ここも AcceleratorSchedulingUnit と重複しているので、きりのいい時になんとかしたい
		this.resolveReorderedLabelAddress(memory);

		return this.acceleratorInstructionList.toArray(new AcceleratorInstruction[0]);
	}


	// コード内での内部関数（実際に呼ばれているもののみ）をスキャンし、
	// 最適化用に有用な情報を調べて、関数アドレスをキーとするマップにまとめて返す
	private void extractInternalFunctionInfo(Memory memory) {

		int instructionLength = this.acceleratorInstructionList.size();

		this.functionInfoMap = new LinkedHashMap<Integer, InternalFunctionInfo>();
		List<Integer> functionAddressList = new ArrayList<Integer>();

		// アセンブリコードの段階ではラベルで関数の先頭行を判別できるが、アセンブル後はラベルは無情報のLABEL命令になってしまう。
		// そこで、まずコード内のCALL命令の箇所をスキャンし、そのオペランドから、関数先頭の命令アドレスのリストを作る。
		// -> 関数先頭に、それが関数先頭とわかる（目印用の）命令を置けば、こういった事前スキャンは不要になるけど…
		//    -> しかしこの方法も、どこからも呼んでいない関数の情報解析は自然と省略できるので、これはこれで逆にいいかも
		// 別の最適化などで、やはり全関数の命令アドレスや引数情報が要る、となった際は要再検討
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {

			AcceleratorInstruction instruction = this.acceleratorInstructionList.get(instructionIndex);
			if (instruction.getOperationCode() == OperationCode.CALL) {
				// 関数アドレスは、CALL命令のオペランド[1]のアドレスを参照した先に、整数値として格納されている
				DataContainer<?> functionAddrContainer = memory.getDataContainer(
					instruction.getOperandPartitions()[1], instruction.getOperandAddresses()[1]
				);
				if (!(functionAddrContainer.getArrayData() instanceof long[])) {
					throw new VnanoFatalException("Unexpected data type of the function address detected.");
				}
				int calleeFunctionAddress = (int)( (long[])(functionAddrContainer.getArrayData()) )[0];
				functionAddressList.add(calleeFunctionAddress);
			}
		}

		// 上で検出した各関数に対して、先頭部分をそれぞれスキャンし、引数などの情報を調べてマップに格納する
		int functionLength = functionAddressList.size();
		for (int functionIndex=0; functionIndex<functionLength; functionIndex++) { // ここでの functionIndex は、上のリスト内でのインデックス
			int functionAddress = functionAddressList.get(functionIndex);      // ここでの functionAddress は、命令列の中でのインデックス

			// 既に解析済みの関数ならスキップ
			if (this.functionInfoMap.containsKey(functionAddress)) {
				continue;
			}

			// 解析した関数情報を控える InternalFunctionInfo インスタンスを用意
			InternalFunctionInfo functionInfo = new InternalFunctionInfo(functionAddress);

			// ALLOCT命令が出現した時点で、次の引数がスカラかどうかや、次元数を調べてて以下の変数に控える
			boolean isNextOperandScalar = false;
			int nextOperandRank = -1;

			// 関数先頭から命令を辿り、引数情報を解析する
			for (int instructionIndex=functionAddress; true; instructionIndex++) {

				AcceleratorInstruction instruction = this.acceleratorInstructionList.get(instructionIndex);
				OperationCode opcode = instruction.getOperationCode();

				if (opcode == OperationCode.MOVPOP || opcode == OperationCode.REFPOP) {

					// MOVPOP/REFPOP命令は、スタックに積まれた実引数を、仮引数に受け取る命令なので、
					// そのオペランド[0]に仮引数が指定されている。従ってそのオペランドの情報を抽出する。
					DataType dataType = instruction.getDataTypes()[0];
					Memory.Partition paramPartition = instruction.getOperandPartitions()[0];
					int paramAddress = instruction.getOperandAddresses()[0];
					boolean paramReferenceness = (opcode == OperationCode.REFPOP); // REFPOPで受け取るなら参照渡し、MOVPOPなら値渡し
					boolean paramScalarity = isNextOperandScalar;
					int paramRank = nextOperandRank;
					isNextOperandScalar = false;
					nextOperandRank = -1;

					// 関数情報に登録する
					functionInfo.addParameter(dataType, paramPartition, paramAddress, paramReferenceness, paramScalarity, paramRank);

					// 次の引数の解析へ
					continue;

				// 引数の取り出し先の型情報を宣言する命令（最適化補助用の命令）
				} else if (opcode == OperationCode.ALLOCT) {

					// ALLOCTのオペランドが1個なら、次に取り出される引数はスカラで、そうでないなら配列
					isNextOperandScalar = instruction.getOperandLength() == 1;

					// ALLOCTのオペランド数 - 1 が配列次元数 ([0] はALLOC対象)
					nextOperandRank = instruction.getOperandLength() - 1;

					// 次の引数の解析へ
					continue;

				// 引数の取り出し先の確保に使う命令の場合 (現状では何も拾いたい情報は無いので何もしない)
				} else if (opcode == OperationCode.ALLOC || opcode == OperationCode.ALLOCP || opcode == OperationCode.ALLOCR) {
					continue;

				// アセンブリコードで関数ラベルだった地点には、アセンブル後はLABEL命令があるが、何も拾う情報は無いので何もしない
				//（関数アドレス +1 の命令からスキャンを始めてもいいが、後の最適化でLABEL命令を削った時にややこしいので、一応ある事を想定する）
				} else if (opcode == OperationCode.LABEL) {
					continue;

				// ENDPRM命令が来た時点で、引数の受け取り処理は終わったので解析終了
				} else if (opcode == OperationCode.ENDPRM) {
					functionInfo.setBodyBeginAddress(instructionIndex + 1); // ENDPRM命令の次(+1)から関数の本体処理が始まる
					break;

				// それ以外の命令が ENDPRM より前にある事は想定していないので、もしあったらエラー
				//（将来的なコードジェネレータの変更で別の命令を吐くようになるかもしれないし、その際に気付かないとまずいので）
				} else {
					throw new VnanoFatalException("Unexpected instruction detected before ENDPRM instruction: " + opcode);
				}
			}

			// 引数は詰む順とは逆順でスタックから取り出されるので、
			// スタックからの取り出し部の命令列を読みながらaddParameterで追加していった場合(今の場合はそう)、
			// 全引数の追加後にこのメソッドで順序を逆転させる必要がある
			functionInfo.reverseParameterOrder();

			// 関数本体（引数取り出し部を除く）の先頭から命令を辿り、本体末尾の位置や、それまでの return 数、内部関数呼び出しの有無などを調査
			for (int instructionIndex=functionInfo.getBodyBeginAddress(); true; instructionIndex++) {
				AcceleratorInstruction instruction = this.acceleratorInstructionList.get(instructionIndex);
				OperationCode opcode = instruction.getOperationCode();

				// 関数末端の命令の場合
				if (opcode == OperationCode.ENDFUN) {
					functionInfo.setBodyEndAddress(instructionIndex - 1); // 関数本体処理の末端は ENDFUN 命令を除いたものなので -1
					functionInfo.setLastInstructionRet(
						this.acceleratorInstructionList.get(instructionIndex-1).getOperationCode() == OperationCode.RET
					);
					break;

				// CALL命令の場合は、その数が最適化に有用なので、カウンタを加算する
				} else if (opcode == OperationCode.CALL) {
					functionInfo.incrementCallCount();
					continue;

				// RET命令の場合は、同様にカウンタを加算した上で、戻り値に指定しているオペランドのアドレスを控える
				} else if (opcode == OperationCode.RET) {
					functionInfo.incrementRetCount();
					if (2 < instruction.getOperandAddresses().length) {
						functionInfo.setLastRetValue(instruction.getOperandPartitions()[2], instruction.getOperandAddresses()[2]);
					}
					continue;
				}
			}

			// 関数情報をマップに登録
			this.functionInfoMap.put(functionAddress, functionInfo);
		}
	}


	// 内部関数の最適化情報のアドレス類を、命令のずれを補正したものに更新
	//（関数アドレスは実質的な識別子の意味を持つため変えず、bodyBegin と bodyEnd を変える。
	//  また、事前に generateAddressReorderingMap でリオーダリングマップを更新しておく必要がある。）
	void updateInternalFunctionInfo() {
		Set<Map.Entry<Integer, InternalFunctionInfo>> functionEntrySet = this.functionInfoMap.entrySet();
		for (Map.Entry<Integer, InternalFunctionInfo> functionEntry: functionEntrySet) {
			InternalFunctionInfo functionInfo = functionEntry.getValue();
			int bodyBegin = functionInfo.getBodyBeginAddress();
			int bodyEnd = functionInfo.getBodyEndAddress();
			bodyBegin = this.addressReorderingMap.get(bodyBegin);
			bodyEnd = this.addressReorderingMap.get(bodyEnd);
			functionInfo.setBodyBeginAddress(bodyBegin);
			functionInfo.setBodyEndAddress(bodyEnd);
		}
	}


	// 内部関数呼び出しにおける、(本来スタックを介する)引数の受け渡しを、呼び出し前に実引数から仮引数に直接MOV/REFするようにする
	void modifyCodeToTransferArgumentsDirectly(Memory memory) {
		int instructionLength = this.acceleratorInstructionList.size();

		// CALL命令1個だったのが複数命令に変化する箇所が、関数呼び出し地点ごとに発生するので、全体の命令列のリストを再構成する
		// （元のリストに add(index,element) で詰めていくのを繰り返すよりも、先頭から読みながらこの新しいリストに置いていった方が効率的）
		List<AcceleratorInstruction> modifiedInstructionList = new ArrayList<AcceleratorInstruction>();

		// コードをスキャンして内部関数呼び出し部を探す
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			AcceleratorInstruction instruction = this.acceleratorInstructionList.get(instructionIndex);

			// 内部関数呼び出し部
			if (instruction.getOperationCode() == OperationCode.CALL) {

				Memory.Partition[] opParts = instruction.getOperandPartitions();
				int[] opAddrs = instruction.getOperandAddresses();
				int operandLength = instruction.getOperandLength();

				int argLength = operandLength - 2; // 引数の数 / オペランド [0] はプレースホルダ、[1] は関数アドレス なので -2

				// 関数アドレスは、CALL命令のオペランド[1]のアドレスを参照した先に、整数値として格納されている
				DataContainer<?> functionAddrContainer = memory.getDataContainer(
					instruction.getOperandPartitions()[1], instruction.getOperandAddresses()[1]
				);
				if (!(functionAddrContainer.getArrayData() instanceof long[])) {
					throw new VnanoFatalException("Unexpected data type of the function address detected.");
				}
				int calleeFunctionAddress = (int)( (long[])(functionAddrContainer.getArrayData()) )[0];

				// 解析済みの関数情報を取得
				InternalFunctionInfo functionInfo = this.functionInfoMap.get(calleeFunctionAddress);

				// 実引数を仮引数に直接転送する命令列を生成して積んでいく
				// (実引数は arg～、仮引数は param～ のように名前を付け分けている)
				for (int paramIndex=0; paramIndex<argLength; paramIndex++) {
					int argOpIndex = paramIndex + 2; // 実引数のオペランドインデックス (+2しているのは引数以外のオペランドの分)

					// 参照渡しの場合や、値渡しでも配列の場合は、後の最適化で仮引数の次元数情報が有用なので、それを解析できるように ALLOCT 命令を置いてておく
					//（ALLOCTはそういうメタ情報をコード内で表して最適化を補助するための命令で、本来はスタックから引数をMOVPOP/REFPOPする箇所にコンパイラが吐いている）
					if (functionInfo.isParameterReference(paramIndex) || !functionInfo.isParameterScalar(paramIndex)) {
						int rank = functionInfo.getParameterRank(paramIndex);
						Memory.Partition[] alloctOpParts = new Memory.Partition[rank + 1];
						int[] alloctOpAddrs = new int[rank + 1];
						Arrays.fill(alloctOpParts, Memory.Partition.NONE); // ALLOCTのオペランドは[0]以外はプレースホルダで、その個数のみが次元数としての意味を持つ
						Arrays.fill(alloctOpAddrs, 0);
						alloctOpParts[0] = functionInfo.getParameterPertition(paramIndex);
						alloctOpAddrs[0] = functionInfo.getParameterAddress(paramIndex);
						Instruction alloctInstruction = new Instruction(
							OperationCode.ALLOCT, new DataType[] { functionInfo.getParameterDataType(paramIndex) },
							alloctOpParts, alloctOpAddrs, instruction.getMetaPartition(), instruction.getMetaAddress()
						);
						AcceleratorInstruction accelAlloctInstruction = new AcceleratorInstruction(alloctInstruction);
						accelAlloctInstruction.setUnreorderedAddress(instructionIndex);
						modifiedInstructionList.add(accelAlloctInstruction);
					}

					// 転送命令のオペランドを用意 / [0]:dest, [1]:src
					Memory.Partition[] transParts = new Memory.Partition[] { functionInfo.getParameterPertition(paramIndex), opParts[argOpIndex] };
					int[] transAddrs = new int[] { functionInfo.getParameterAddress(paramIndex), opAddrs[argOpIndex] };
					DataType[] transDataTypes = new DataType[] { functionInfo.getParameterDataType(paramIndex) }; // 転送命令はデータ型は1種のみ指定

					// 参照渡しならREF命令を生成
					if (functionInfo.isParameterReference(paramIndex)) {
						Instruction refInstruction = new Instruction(
							OperationCode.REF, transDataTypes,
							transParts, transAddrs, instruction.getMetaPartition(), instruction.getMetaAddress()
						);
						AcceleratorInstruction accelRefInstruction = new AcceleratorInstruction(refInstruction);
						accelRefInstruction.setUnreorderedAddress(instructionIndex);
						modifiedInstructionList.add(accelRefInstruction);

					// そうでなければ値渡しなのでMOV命令を生成
					// この場合は併せて、ALLOC/ALLOCR命令でコピー先領域確保も必要
					} else {

						// 最適化が効の効き方を考慮して、スカラでは 1 オペランドのALLOCを用い、配列ではsrcと同サイズでdestを確保するALLOCRを使用
						Instruction allocInstruction = null;
						if (functionInfo.isParameterScalar(paramIndex)) {
							allocInstruction = new Instruction(
								OperationCode.ALLOC, transDataTypes,
								new Memory.Partition[] { transParts[0] }, new int[] { transAddrs[0] }, // MOV の dest 先を確保する
								instruction.getMetaPartition(), instruction.getMetaAddress()
							);
						} else {
							allocInstruction = new Instruction(
								OperationCode.ALLOCR, transDataTypes,
								transParts, transAddrs, // この場合のオペランドは dest, src なのでMOVのオペランドと全く同じ
								instruction.getMetaPartition(), instruction.getMetaAddress()
							);
						}
						AcceleratorInstruction accelAllocInstruction = new AcceleratorInstruction(allocInstruction);
						accelAllocInstruction.setUnreorderedAddress(instructionIndex);
						modifiedInstructionList.add(accelAllocInstruction);

						// 続いてMOV命令を生成
						Instruction movInstruction = new Instruction(
							OperationCode.MOV, transDataTypes,
							transParts, transAddrs, instruction.getMetaPartition(), instruction.getMetaAddress()
						);
						AcceleratorInstruction accelMovInstruction = new AcceleratorInstruction(movInstruction);
						accelMovInstruction.setUnreorderedAddress(instructionIndex);
						modifiedInstructionList.add(accelMovInstruction);
					}

				} // 引数転送ループ

				// 上記の実引数>仮引数転送によって、引数をスタックに積む必要は無くなるため、引数オペランドを除去したCALL命令を生成
				// (CALLのオペランドは [0] がプレースホルダ、[1] が関数アドレス)
				Instruction callInstruction = new Instruction(
					OperationCode.CALL, instruction.getDataTypes(),
					new Memory.Partition[] { opParts[0], opParts[1] }, new int[] { opAddrs[0], opAddrs[1] },
					instruction.getMetaPartition(), instruction.getMetaAddress()
				);
				AcceleratorInstruction accelCallInstruction = new AcceleratorInstruction(callInstruction);
				accelCallInstruction.setUnreorderedAddress(instructionIndex);
				modifiedInstructionList.add(accelCallInstruction);

			// それ以外の命令はそのまま元の順で置いていく
			} else {
				modifiedInstructionList.add(instruction);
			}
		}

		// 元の関数定義部において、引数をスタックから取りだすコードはもう不要（というよりも実行してはいけない）ので除去する
		// (まず削る場所に null を詰めて、その後に null を無視しながら別のリストに移し替える)
		Set<Map.Entry<Integer, InternalFunctionInfo>> functionInfoEntrySet = this.functionInfoMap.entrySet();
		for (Map.Entry<Integer, InternalFunctionInfo> functionInfoEntry: functionInfoEntrySet) {
			InternalFunctionInfo functionInfo = functionInfoEntry.getValue();
			int functionAddress = functionInfoEntry.getKey();
			int functionBodyAddress = functionInfo.getBodyBeginAddress();
			for (int i=functionAddress+1; i<functionBodyAddress; i++) { // 始点が +1 なのは、元の着地点そのものを消すと他の最適化でのリオーダリング後の飛び先解決時に困るから
				modifiedInstructionList.set(i, null);
			}
		}
		this.acceleratorInstructionList = new ArrayList<AcceleratorInstruction>();
		int modifiedLength = modifiedInstructionList.size();
		for (int i=0; i<modifiedLength; i++) {
			AcceleratorInstruction instruction = modifiedInstructionList.get(i);
			if (instruction != null) {
				this.acceleratorInstructionList.add(instruction);
			}
		}
	}


	// インライン化できそうな＆した方が良さそうな内部関数呼び出しをインライン化する
	private void expandFunctionCodeInline(Memory memory) {
		int instructionLength = this.acceleratorInstructionList.size();

		// CALL命令1個だったのが複数命令に変化する箇所が、関数呼び出し地点ごとに発生するので、全体の命令列のリストを再構成する
		// （元のリストに add(index,element) で詰めていくのを繰り返すよりも、先頭から読みながらこの新しいリストに置いていった方が効率的）
		List<AcceleratorInstruction> modifiedInstructionList = new ArrayList<AcceleratorInstruction>();

		// スクリプト全体のコードを先頭からスキャンし、CALL命令を探す
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			AcceleratorInstruction instruction = this.acceleratorInstructionList.get(instructionIndex);

			// CALL 命令以外は飛ばして次のループへ
			if (instruction.getOperationCode() != OperationCode.CALL) {
				modifiedInstructionList.add(instruction);
				continue;
			}

			// CALLで呼んでいる関数アドレスは、オペランド[1]のアドレスを参照した先に、整数値として格納されているので読む
			DataContainer<?> functionAddrContainer = memory.getDataContainer(
				instruction.getOperandPartitions()[1], instruction.getOperandAddresses()[1]
			);
			if (!(functionAddrContainer.getArrayData() instanceof long[])) {
				throw new VnanoFatalException("Unexpected data type of the function address detected.");
			}
			int calleeFunctionAddress = (int)( (long[])(functionAddrContainer.getArrayData()) )[0];

			// 解析済みの関数情報を取得する
			InternalFunctionInfo functionInfo = this.functionInfoMap.get(calleeFunctionAddress);


			// 以下、インライン展開条件の一致確認


			// 長すぎる関数は、展開のメリットが少ないのにデメリットは大きいためスキップ
			if (MAX_INLINE_EXPANSIBLE_FUNCTION_CODE_LENGTH < functionInfo.getBodyEndAddress() - functionInfo.getBodyBeginAddress()) {
				modifiedInstructionList.add(instruction);
				continue;
			}


			// 内部で別の内部関数を呼び出している場合は、
			// きちんと対応するには再帰/循環呼び出しの検査や、その他整合性の検査をしないといけないのでスキップ
			//（外部関数は完全にスクリプト外に独立した処理フローなので別にいい）
			if (1 <= functionInfo.getCallCount()) {
				modifiedInstructionList.add(instruction);
				continue;
			}

			// return している箇所が1箇所の場合は、最後の命令が RET 命令の場合のみ展開するものとし、そうでなければスキップ
			// (理由は else の場合と基本的に同じで、最後がRETならMOV/REFに置き換えれば飛ぶ必要も無く、MOVも別最適化のMOV削りが効く事が見込める)
			if (functionInfo.getRetCount() == 1) {
				if (!functionInfo.isLastInstructionRet()) {
					modifiedInstructionList.add(instruction);
					continue;
				}

			// return している箇所が複数ある場合は、
			// 戻り値のコピー後に関数の終端へ飛ぶコードを吐く必要があり、展開のデメリットに比べてメリットが薄いのでスキップ
			// (展開のデメリットは、最適化後のコードの複雑性や規模が増加する事で、メリットは飛んだり積んだりのオーバーヘッドを削れる事)
			} else {
				modifiedInstructionList.add(instruction);
				continue;
			}
			// 注: void 型関数も最後にRET命令があるので、getRetCount が 0 という場合は無い



			// 以下、インライン展開を行う

			int functionBegin = functionInfo.getBodyBeginAddress(); // 関数本体の始点命令アドレス（引数取り出し部は除く）
			int functionEnd = functionInfo.getBodyEndAddress();     // 関数本体の終端命令アドレス（ENDFUN命令は除く）

			// CALL命令があった場所に、最後のRET命令を除いて、関数本体の命令列をそのままコピーする
			// (引数の受け渡しは、先の最適化で既に MOV/REF コード化されていて、CALL命令より前に済んでいるはず)
			for (int functionInstructionIndex=functionBegin; functionInstructionIndex<functionEnd; functionInstructionIndex++) { // 条件が < なので末端のRETはコピーされない

				// 関数内の命令をコピーして展開用命令を生成し、展開直後のアドレスを命令に設定
				AcceleratorInstruction expandedInstruction = this.acceleratorInstructionList.get(functionInstructionIndex).clone();
				expandedInstruction.setExpandedAddress(modifiedInstructionList.size());

				// 分岐命令の場合は、飛び先ラベル（アセンブル後はLABEL命令）位置の命令アドレスを、展開後のアドレスに補正する必要がある
				OperationCode opcode = expandedInstruction.getOperationCode();
				if (opcode == OperationCode.JMP || opcode == OperationCode.JMPN) { // CALLも分岐的な挙動をするが、内部にCALLを含む関数は展開対象外なのでここには存在しないはず

					// オペランド [1] に格納されている、展開前の飛び先ラベル位置の命令アドレスを取得
					Memory.Partition[] operandParts = expandedInstruction.getOperandPartitions();
					int[] operandAddrs = expandedInstruction.getOperandAddresses();
					DataContainer<?> labelAddrContainer = memory.getDataContainer(operandParts[1], operandAddrs[1]);
					int labelAddr = (int)( (long[])labelAddrContainer.getArrayData() )[0];

					// 飛び先ラベルの位置は、他の最適化によって既に移動している可能性があるため、まずそれを補正する
					labelAddr = this.addressReorderingMap.get(labelAddr);

					// 分岐命令から見た、飛び先ラベルのオフセットアドレスを求める
					int labedAddrOffset = labelAddr - functionInstructionIndex;

					// 上記オフセット量を、インライン展開後の分岐命令のアドレスに足して、展開後の飛び先ラベル位置の命令アドレスを求める
					int expandedLabelAddr = expandedInstruction.getExpandedAddress() + labedAddrOffset;

					// 展開直後の飛び先ラベル位置を命令に設定（リオーダリング後のアドレス補正処理等で参照）
					expandedInstruction.setExpandedLabelAddress(expandedLabelAddr);
				}

				// 命令リストに追加
				modifiedInstructionList.add(expandedInstruction);
			}

			// RET命令で戻り値が指定されなかった場合（void関数）は、展開後は何もする必要が無いので、次の CALL 命令へ
			if (!functionInfo.hasRetValue()) {
				continue;
			}


			// ここに到達したケースでは、元のコードの戻り値を受け取る箇所を、RET命令の指定値をコピーするコードに変更する必要がある。
			// 以下でそれを行う。
			// (※元の命令列を辿っている for 文のカウンタ変数 instructionIndex を読み進めるので注意)


			// CALL命令の後続の命令に読み進む
			instructionIndex++;

			// CALL命令の直後には、参照渡し先で書き換えられたデータとキャッシュ値を同期するRETURNED拡張命令が置かれている。
			// 参照渡しの場合はそれをコピーし、値渡しの場合は何もせず読み飛ばす
			if (functionInfo.hasReferenceParameters()) {
				modifiedInstructionList.add( this.acceleratorInstructionList.get(instructionIndex).clone() );
			}
			instructionIndex++;

			// 戻り値相当の値をコピーする処理を出力
			for (; instructionIndex<instructionLength; instructionIndex++) {
				AcceleratorInstruction retGetterInstruction = this.acceleratorInstructionList.get(instructionIndex);
				OperationCode retGetterOpcode = retGetterInstruction.getOperationCode();

				// 元の戻り値受け取りコードから、変更が必要なのは以下の2命令:
				// ・戻り値の格納先をスタック上の値と同じサイズだけ確保する ALLOCP 命令は、dest を src と同サイズにする ALLOCR に置き換える
				// ・スタックから戻り値を取り出すMOVPOP命令は、dest から src にコピーする MOV に置き換える
				if (retGetterOpcode == OperationCode.ALLOCP || retGetterOpcode == OperationCode.MOVPOP) {

					// 変更後の命令のオペランドは量命令で共通
					Memory.Partition[] parts = new Memory.Partition[] {
						retGetterInstruction.getOperandPartitions()[0], functionInfo.getLastRetValuePartition() // dest, src
					};
					int[] addrs = new int[] {
						retGetterInstruction.getOperandAddresses()[0], functionInfo.getLastRetValueAddress() // dest, src
					};

					// 変更後の命令を生成して登録
					OperationCode newOpcode = retGetterOpcode == OperationCode.ALLOCP ? OperationCode.ALLOCR : OperationCode.MOV;
					AcceleratorInstruction newInstruction = new AcceleratorInstruction( new Instruction(
						newOpcode, retGetterInstruction.getDataTypes(), parts, addrs,
						retGetterInstruction.getMetaPartition(), retGetterInstruction.getMetaAddress()
					));
					newInstruction.setUnreorderedAddress(retGetterInstruction.getUnreorderedAddress());
					modifiedInstructionList.add(newInstruction);

					// 扱ったのがMOVPOP命令だった場合は、戻り値の受け取りが完了したので展開終わり（その後の列は全く別の処理）
					if (retGetterOpcode == OperationCode.MOVPOP) {
						break;
					}

				// 戻り値を使わずに取り出すだけの場合は、それ以上何もせずに展開終わり
				} else if (retGetterOpcode == OperationCode.POP) {
					break;

				// それ以外の命令はそのままコピー
				} else {
					modifiedInstructionList.add(retGetterInstruction);
				}

			} // 戻り値を受け取るコードを編集するループ

		} // 全命令を辿ってCALL命令の箇所を追うループ

		this.acceleratorInstructionList = modifiedInstructionList;
	}


	// CALL命令の直後（アセンブル後はLABEL命令が置かれている）の箇所に、RETURNED拡張命令を生成して置き換える
	private void generateReturnedInstructions() {
		int instructionLength = acceleratorInstructionList.size();

		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			AcceleratorInstruction instruction = acceleratorInstructionList.get(instructionIndex);
			if (instruction.getOperationCode() == OperationCode.CALL) {

				// オペランドのパーティションとアドレスを取り出す
				Memory.Partition[] operandPartitions = instruction.getOperandPartitions();
				int[] operandAddresses = instruction.getOperandAddresses();
				int functionArgN = operandAddresses.length - 2; // 先頭2個はプレースホルダと関数アドレス

				// 上記から、関数に渡す引数のもののみを抜き出す
				//（ただし命令の基本仕様により、先頭オペランドは書き込み先と決まっているので、プレースホルダを置く）
				Memory.Partition[] functionArgPartitions = new Memory.Partition[functionArgN+1];
				int[] functionArgAddresses = new int[functionArgN+1];
				functionArgPartitions[0] = Memory.Partition.NONE;
				functionArgAddresses[0] = 0;
				for (int argIndex=0; argIndex<functionArgN; argIndex++) {
					functionArgPartitions[argIndex+1] = operandPartitions[argIndex + 2]; // 右辺の先頭2個はプレースホルダと関数アドレス
					functionArgAddresses[argIndex+1] = operandAddresses[argIndex + 2];
				}

				// それらをオペランドとして、RETURNED拡張命令を生成
				AcceleratorInstruction returnedInstruction = new AcceleratorInstruction(
					new Instruction(
						OperationCode.EX, instruction.getDataTypes(),
						functionArgPartitions, functionArgAddresses,
						instruction.getMetaPartition(), instruction.getMetaAddress()
					)
				);
				returnedInstruction.setExtendedOperationCode(AcceleratorExtendedOperationCode.RETURNED);
				returnedInstruction.setUnreorderedAddress(instruction.getUnreorderedAddress() + 1);

				// この命令直後にあるはずのLABEL命令を、生成したRETURNED拡張命令で置き換える
				acceleratorInstructionList.set(instructionIndex+1, returnedInstruction);
			}
		}
	}


	// データに対して書き込みを行う命令なら true を返す（ただしレジスタの確保・解放は除外）
	private boolean isDataWritingOperationCode(OperationCode operationCode) {

		// 書き込みを「行わない」ものを以下に列挙し、それ以外なら true を返す
		return operationCode != OperationCode.ALLOC
		     && operationCode != OperationCode.ALLOCT
		     && operationCode != OperationCode.ALLOCP
		     && operationCode != OperationCode.ALLOCR
		     && operationCode != OperationCode.FREE
		     && operationCode != OperationCode.JMP
		     && operationCode != OperationCode.JMPN
		     && operationCode != OperationCode.RET
		     && operationCode != OperationCode.POP      // POP はスタックからデータを取り出すだけで何もしない
		     && operationCode != OperationCode.NOP      // NOP は何もしないので明らかに何も読まない
		     && operationCode != OperationCode.LABEL    // NOP 同様
		     && operationCode != OperationCode.END
		     && operationCode != OperationCode.ENDFUN
		     ;

		// CALL & RET 命令について：
		//   関数の引数や戻り値はスタックに積まれるので、CALL命令やRET命令自体はオペランドには何も書き込まないが、
		//   スタックに積まれた後のデータがどこで取り出されて読み書きされるかを静的に解析するのは複雑なので、
		//   CALL & RET 命令のオペランドは便宜的に、書き込み箇所カウントに含める（最適化予防のため）
	}


	// 各レジスタについて、書き込んでいる箇所を検出して数える
	private void countRegisterWrittenPoints(Memory memory) {
		int registerLength = memory.getSize(Memory.Partition.REGISTER);
		this.registerWrittenPointCount = new int[registerLength];
		Arrays.fill(this.registerWrittenPointCount, 0);

		int instructionLength = acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			AcceleratorInstruction instruction = acceleratorInstructionList.get(instructionIndex);

			// 値が変更されている可能性を調べるために使うので、非書き換え命令や確保・解放はカウントから除外
			if (!this.isDataWritingOperationCode(instruction.getOperationCode())) {
				continue;
			}

			// 値の書き込み用である第0オペランドがレジスタではない場合も、この時点で除外
			Memory.Partition writingPartition = instruction.getOperandPartitions()[0];
			if (writingPartition != Memory.Partition.REGISTER) {
				continue;
			}

			// ここまで残るのはレジスタに書き込んでいる場合なので、レジスタ書き込み箇所カウンタを加算
			int writingRegisterAddress = instruction.getOperandAddresses()[0];
			this.registerWrittenPointCount[writingRegisterAddress]++;
		}
	}



	// オペランドからデータの読み込みを行う命令なら true を返す
	//（ただしメモリの確保・解放やなどは除外）
	private boolean isDataReadingOperationCode(OperationCode operationCode) {

		// 読み込みを「行わない」ものを以下に列挙し、それ以外なら true を返す
		return operationCode != OperationCode.ALLOC
		     && operationCode != OperationCode.ALLOCT
		     && operationCode != OperationCode.ALLOCP
		     && operationCode != OperationCode.ALLOCR
		     && operationCode != OperationCode.FREE
		     && operationCode != OperationCode.POP      // POP はスタックからデータを取り出すだけで何もしない
		     && operationCode != OperationCode.MOVPOP   // MOVPOP はスタックからデータを読むので、オペランドからは読まない（書く）
		     && operationCode != OperationCode.REFPOP   // REFPOP はスタックからデータを読むので、オペランドからは読まない（書く）
		     && operationCode != OperationCode.NOP      // NOP は何もしないので明らかに何も読まない
		     && operationCode != OperationCode.LABEL    // NOP 同様
		     && operationCode != OperationCode.ENDFUN
		     ;

		// スタックとデータをやり取りする CALL & RET 命令は要注意
		// ( isDataWritingOperationCode のコメント参照 )
	}


	// 各レジスタについて、読み込んでいる箇所を検出して数える
	private void countRegisterReadPoints(Memory memory) {
		int registerLength = memory.getSize(Memory.Partition.REGISTER);
		this.registerReadPointCount = new int[registerLength];
		Arrays.fill(this.registerReadPointCount, 0);

		int instructionLength = acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			AcceleratorInstruction instruction = acceleratorInstructionList.get(instructionIndex);
			OperationCode opcode = instruction.getOperationCode();

			// 値が変更されている可能性を調べるために使うので、非書き換え命令や確保・解放はカウントから除外
			if (!this.isDataReadingOperationCode(opcode)) {
				continue;
			}

			// オペランドの個数
			int operandLength = instruction.getOperandLength();

			// 入力オペランドが始まるインデックス ... 命令の仕様上、先頭は必ず書き込みオペランドで、入力はその次（1番）から始まる
			int inputOperandsBeginIndex = 1;

			// 入力オペランドは個数可変や省略可能な命令もあるため、オペランド数が2未満の場合＝入力オペランドが無い場合はスキップする
			if (operandLength < 2) {
				continue;
			}

			// オペランドのメモリパーティションとアドレスを取得
			Memory.Partition[] operandPartitions = instruction.getOperandPartitions();
			int[] operandAddresses = instruction.getOperandAddresses();

			// 2個目以降（インデックス1以上）のオペランドがレジスタなら、レジスタ読み込み箇所カウンタを加算
			for (int operandIndex=inputOperandsBeginIndex; operandIndex<operandLength; operandIndex++) {
				if (operandPartitions[operandIndex] == Memory.Partition.REGISTER) {
					int readingRegisterAddress = operandAddresses[operandIndex];
					this.registerReadPointCount[readingRegisterAddress]++;
				}
			}
		}
	}


	// 各レジスタについて、参照リンクされている可能性があるかどうかを検出する
	private void detectRegisterReferenceLinks(Memory memory) {
		int registerLength = memory.getSize(Memory.Partition.REGISTER);

		// レジスタ番号をインデックスとして、参照リンクされている可能性があるレジスタに対してはこの配列の値を true にする
		this.registerReferenceMaybeLinked = new boolean[registerLength];
		Arrays.fill(this.registerReferenceMaybeLinked, false);

		int instructionLength = acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			AcceleratorInstruction instruction = acceleratorInstructionList.get(instructionIndex);
			OperationCode opcode = instruction.getOperationCode();
			Memory.Partition[] parts = instruction.getOperandPartitions();
			int[] addrs = instruction.getOperandAddresses();

			// REF命令はオペランド [0] と [1] を参照リンクするので、それぞれレジスタなら true とマーク
			if (opcode == OperationCode.REF) {
				if (parts[0] == Memory.Partition.REGISTER) {
					this.registerReferenceMaybeLinked[addrs[0]] = true;
				}
				if (parts[1] == Memory.Partition.REGISTER) {
					this.registerReferenceMaybeLinked[addrs[1]] = true;
				}

			// REFELM 命令はオペランド[1]の配列要素をオペランド[0]に参照リンクするので、それぞれレジスタなら true とマーク
			// (オペランド[1]は普通は配列変数だが、関数の戻り値や配列演算結果などに直接 [i] を付けて要素参照する場合など、レジスタもあり得る)
			} else if (opcode == OperationCode.REFELM) {
				if (parts[0] == Memory.Partition.REGISTER) {
					this.registerReferenceMaybeLinked[addrs[0]] = true;
				}
				if (parts[1] == Memory.Partition.REGISTER) {
					this.registerReferenceMaybeLinked[addrs[1]] = true;
				}

			// REFPOP 命令はスタックのデータをオペランド[0]に参照リンクするので、それがレジスタなら true とマーク
			} else if (opcode == OperationCode.REFPOP) {
				if (parts[0] == Memory.Partition.REGISTER) {
					this.registerReferenceMaybeLinked[addrs[0]] = true;
				}

			// 内部関数に渡している実引数は、スタック経由で REFPOP で取り出される可能性があるため、レジスタなら true とマーク
			// > VRILアセンブリコード内の変数宣言ディレクティブで #VARIABLE _arg CONST REF みたいに識別子後に修飾子が付くようにして、
			//   その情報も考慮に入れて判定するとかした方が総合的にはいいかもしれない。実際には参照リンクされない引数も多いので。
			//   また後々で要検討
			} else if (opcode == OperationCode.CALL) {
				for (int i=2; i<addrs.length; i++) { // [0]はプレースホルダ、[1]は関数アドレス、[2]以降が実引数なので2からループ
					if (parts[i] == Memory.Partition.REGISTER) {
						this.registerReferenceMaybeLinked[addrs[i]] = true;
					}
				}

			// 内部関数の戻り値については、VRIL上は引数同様にREFPOPで受け取られる可能性があるが、
			// 現状のVnanoの文法では参照で受け取る方法が存在しないので、検出せずスキップ
			} else if (opcode == OperationCode.RET) {
				continue;

			// 外部関数は引数の参照が渡される場合があるが、
			// それをスクリプト内の別のアドレスに参照「リンク」される事は無い（仕様上してはいけない）のでスキップ
			} else if (opcode == OperationCode.CALLX) {
				continue;

			// それ以外の命令では、スクリプト内で別アドレスに参照リンク経由でされる事は無いのでスキップ
			} else {
				continue;
			}
		}
	}


	// 全命令に対して再配置済み命令アドレスを書き込む
	private void updateReorderedAddresses() {
		int instructionLength = acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			acceleratorInstructionList.get(instructionIndex).setReorderedAddress(instructionIndex);
		}
	}


	// 全命令アドレスの再配置前→再配置後の対応を格納したアドレス変換マップを生成
	private void generateAddressReorderingMap() {

		// addressReorderingMap は、再配置前のアドレス unreorderedAddress をキーとして、
		// 再配置後のアドレス reorderedAddress を返すマップで、
		// JMP/JMPN/CALLの着地点ラベル（由来のLABEL命令）の位置を補正するために使用される。
		// 最適化で命令1個だった所が複数命令になっている箇所がある場合 (CALLの引数直接MOV化とか) など、
		// 複数命令が同じ unreorderedAddress を持っていると、addressReorderingMap はそれらの最後の命令の reorderedAddress を返す。
		//
		// なお、インライン展開された関数内の命令も、複数箇所で同じ unreorderedAddress を持っているため、
		// そのまま素直に addressReorderingMap でアドレス変換すると、展開後の関数コード内の分岐命令の飛び先が同一地点に収束してしまう。
		// そのためインライン展開されたコードに対しては、展開後のアドレス（こちらは重複しないはず）をキーとする
		// expandedAddressReorderingMap を用意し、アドレス変換時にそちらを用いるようにする。
		//
		// > もっとキーやタイミングを上手い形に整理すればマップは1個で済ませられるはずなので、きりのいい時に要検討
		//   > というか再配置周りはマップ類を直接使うよりもそういう役割のクラスに包んでその中で管理した方がいいかも
		//     今後の別の最適化とか次第では単純な形では対応し切れない場合もありそうだし

		this.addressReorderingMap = new HashMap<Integer,Integer>();
		this.expandedAddressReorderingMap = new HashMap<Integer,Integer>();
		int instructionLength = acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			AcceleratorInstruction instruction = this.acceleratorInstructionList.get(instructionIndex);
			if (instruction.isExpanded()) {
				this.expandedAddressReorderingMap.put(instruction.getExpandedAddress(), instruction.getReorderedAddress());
			} else {
				this.addressReorderingMap.put(instruction.getUnreorderedAddress(), instruction.getReorderedAddress());
			}
		}
	}


	// JMP命令など、ラベルオペランドを持つ命令は、ラベルの命令アドレスが命令再配置によって変わるため、再配置後のアドレス情報を追加
	private void resolveReorderedLabelAddress(Memory memory) {
		int instructionLength = this.acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			AcceleratorInstruction instruction = this.acceleratorInstructionList.get(instructionIndex);
			OperationCode opcode = instruction.getOperationCode();

			// JMP & JMPN & CALL はオペランドアドレスに飛ぶ。RETはスタックから取ったアドレスに飛ぶが、オペランドに所属関数ラベルを持っている
			if (opcode == OperationCode.JMP || opcode == OperationCode.JMPN || opcode == OperationCode.CALL || opcode == OperationCode.RET) {

				// 分岐先が、インライン展開で生成された命令列の中にある場合
				// (unreorderedLabelAddress は複数展開時に重複が生じてキーに使えないため、展開直後の一意なラベルアドレスをキーとするマップで変換)
				if (instruction.isLabelAddressExpanded()) {
					int expandedLabelAddress = instruction.getExpandedLabelAddress();
					int reorderedLabelAddress = this.expandedAddressReorderingMap.get(expandedLabelAddress);
					instruction.setReorderedLabelAddress(reorderedLabelAddress);

				// それ以外の通常の場合
				// (unreordered address をキーとするマップで変換する)
				} else {

					// ラベルの命令アドレスの値を格納するオペランドのデータコンテナをメモリから取得（必ずオペランド[1]にあるよう統一されている）
					DataContainer<?> addressContiner = null;
					addressContiner = memory.getDataContainer(
						instruction.getOperandPartitions()[1],
						instruction.getOperandAddresses()[1]
					);

					// データコンテナから飛び先ラベル（アセンブル後はLABEL命令になっている）の命令アドレスの値を読む
					int labelAddress = -1;
					Object addressData = addressContiner.getArrayData();
					if (addressData instanceof long[]) {
						labelAddress = (int)( ((long[])addressData)[0] );
					} else {
						throw new VnanoFatalException("Non-integer instruction address (label) operand detected.");
					}

					// 上で読んだラベル（由来のLABEL命令）アドレスの、再配置後の位置の命令アドレスを取得し、命令に持たせる
					int reorderedLabelAddress = this.addressReorderingMap.get(labelAddress);
					instruction.setReorderedLabelAddress(reorderedLabelAddress);
				}
			}
		}
	}


	// スカラのALLOC/ALLOCR命令をコード先頭に移す
	private void reorderAllocAndAllocrInstructions(AcceleratorDataManagementUnit dataManager) {

		int instructionLength = this.acceleratorInstructionList.size();

		// 再配置済みの命令列を格納するリスト（最後にacceleratorInstructionListをこれで置き換える）
		List<AcceleratorInstruction> reorderedInstructionList = new ArrayList<AcceleratorInstruction>();

		// 元の命令列 instructions の要素の内、再配置された要素のインデックスをマークする配列
		boolean[] reordered = new boolean[instructionLength];
		Arrays.fill(reordered, false);

		//int reorderedInstructionIndex = 0;

		// acceleratorInstructionList を先頭から末尾まで辿り、スカラALLOC/ALLOCR命令を reorderedInstruction に移す
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {

			AcceleratorInstruction instruction = acceleratorInstructionList.get(instructionIndex);
			OperationCode opcode = instruction.getOperationCode();

			if ( (opcode == OperationCode.ALLOC || opcode == OperationCode.ALLOCR)
					&& dataManager.isScalar(instruction.getOperandPartitions()[0], instruction.getOperandAddresses()[0])) {

				// reorderedInstructionList に積む
				reorderedInstructionList.add(instruction);

				// このインデックスの命令は再配列済みである事をマークする
				reordered[instructionIndex] = true;
			}
		}

		// 再び acceleratorInstructionList を辿り、再配置されていない（スカラALLOC以外の）命令を移す
		for (int instructionIndex=0; instructionIndex<instructionLength; instructionIndex++) {
			if (!reordered[instructionIndex]) {
				AcceleratorInstruction instruction = acceleratorInstructionList.get(instructionIndex);
				reorderedInstructionList.add(instruction);
			}
		}

		this.acceleratorInstructionList = reorderedInstructionList;
	}


	// どこからも値を読まれていないレジスタへのMOV命令を削る
	// (コードジェネレータの実装簡易化のために、値が実際に使われるかどうかに関わらず、とりあえずレジスタに置いておくようなケースがある。
	//  典型例としては後置インクリメント/デクリメント演算子で、それらは式中での値が加減算前のものであるべきなので、
	//  加減算前の値をレジスタに控えておく処理が発生するが、実際にそれらの演算子を式中で複雑に組み合わせて使う場面は多くないため、
	//  大半が無駄なMOVになる。特に for 文のカウンタ更新などの箇所ではパフォーマンス的に結構もったいない事になる。従って削る。)
	private void removeMovInstructionsToUnreadRegisters(AcceleratorDataManagementUnit dataManager) {
		int instructionLength = this.acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength-1; instructionIndex++) {
			AcceleratorInstruction instruction = this.acceleratorInstructionList.get(instructionIndex);

			// MOV命令以外はスキップ
			if (instruction.getOperationCode() != OperationCode.MOV) {
				continue;
			}

			// MOV命令の書き込み先（0番オペランド）がレジスタでない場合はスキップ
			if (instruction.getOperandPartitions()[0] != Memory.Partition.REGISTER) {
				continue;
			}

			// MOV命令で書き込んでいるレジスタ（ = 0番オペランド）の値を読んでいる箇所がある場合や、
			// ここ以外に書き込んでいる箇所がある場合はスキップ
			//（注: 参照リンクされているものは正確には数えられないので、すぐ後で除外する）
			int writingRegisterAddress = instruction.getOperandAddresses()[0];
			if (this.registerReadPointCount[writingRegisterAddress] != 0
					|| this.registerWrittenPointCount[writingRegisterAddress] != 1) {
					// 上の後者の条件はMOV削りのためには不要だが、後でレジスタそのものを削除するには必要
				continue;
			}

			// 参照リンクされている可能性があるレジスタは、読み書きポイント数を正確に数えるのが難しいのでスキップ
			if (this.registerReferenceMaybeLinked[writingRegisterAddress]) {
				continue;
			}

			// ここまで到達するのは、どこからも読んでいないレジスタにMOVしている箇所なので、削る
			// (ここでは命令列リスト内の該当位置を null に置き換えておいて、後で一括で削る)
			this.acceleratorInstructionList.set(instructionIndex, null);

			// MOVを削った後、書き込み先レジスタはもうどこからも読み書きしていないはずなので、削除登録しておく（後で別の最適化で削る）
			this.unnecessaryRegisterSet.add(writingRegisterAddress);
		}

		// MOV命令を削除した位置の null を詰める
		this.acceleratorInstructionList.removeAll(LIST_OF_NULL);
	}



	// 演算結果のレジスタ値をコピーしているMOV命令を、オペランドを並び替える事によって削る
	// (例えば加算結果を一旦レジスタに置いて、直後にそれを変数アドレスにMOVするような処理があった場合、
	//  加算結果を変数アドレスに直接出力するようにオペランドを並べ替えて、不要になったレジスタへのMOVを削る。)
	private void reduceMovInstructionsCopyingOperationResults(AcceleratorDataManagementUnit dataManager) {

		int instructionLength = this.acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength-1; instructionIndex++) { // 後でイテレータ使うループにする
			AcceleratorInstruction currentInstruction = this.acceleratorInstructionList.get(instructionIndex);
			AcceleratorInstruction nextInstruction = this.acceleratorInstructionList.get(instructionIndex+1);

			// 対象命令がデータ書き込みをしない場合は命令はスキップ
			if (!this.isDataWritingOperationCode(currentInstruction.getOperationCode())) {
				continue;
			}

			// 対象命令の書き込み先（0番オペランド）がレジスタでない場合はスキップ
			if (currentInstruction.getOperandPartitions()[0] != Memory.Partition.REGISTER) {
				continue;
			}

			// 次にMOV命令が続いていなければスキップ
			if (nextInstruction.getOperationCode() != OperationCode.MOV) {
				continue;
			}

			// そのMOV命令のコピー元（1番オペランド）がレジスタではない場合はスキップ
			if (nextInstruction.getOperandPartitions()[1] != Memory.Partition.REGISTER) {
				continue;
			}

			// 対象命令で書き込んでいるレジスタ（ = 0番オペランド）のアドレスを取得
			int writingRegisterAddress = currentInstruction.getOperandAddresses()[0];

			// 次のMOV命令でのコピー元レジスタを取得
			int movingInputRegisterAddress = nextInstruction.getOperandAddresses()[1];

			// 書き込み先レジスタと次命令でのMOVコピー「元」レジスタが異なる場合はスキップ
			if (writingRegisterAddress != movingInputRegisterAddress) {
				continue;
			}

			// そのレジスタに読み書きする箇所がそこだけでない（書き込みが複数、または読み込みが複数ある）場合はスキップ
			//（注: 参照リンクされているものは正確には数えられないので、すぐ後で除外する）
			if (this.registerWrittenPointCount[writingRegisterAddress] != 1
					|| this.registerReadPointCount[writingRegisterAddress] != 1) {

				continue;
			}

			// 参照リンクされている可能性があるレジスタは、上記の読み書きポイント数を正確に数えるのが難しいのでスキップ
			// (その場合はすぐ後のキャッシュ可能性の検査でも弾かれるはずだが、逆に言えばここでこの条件を加えても
			//  パフォーマンス的にマイナスにはならず、コード上では慎重な方向にできるので、将来の改修時の事を考慮して条件に加えている。)
			if (this.registerReferenceMaybeLinked[writingRegisterAddress]) {
				continue;
			}

			// 書き込み先とMOVコピー「先」が、スカラであるかやキャッシュ可能か等の特性が一致していない場合はスキップ
			// (例えば参照リンクされているなどのキャッシュ不可能なもの（=他の箇所への影響解析の難度が高い）に出力していた箇所を、
			//  直後のMOVの出力先で置き換えてしまうと、元の出力先に他の地点から別アドレス経由でアクセスしていた場合などにまずい。
			//  MOV削り条件では対象アドレスへの読み書き箇所を追ってはいるが、別アドレスと参照リンクされているものは追えないため。)
			Memory.Partition movOutputPartition = nextInstruction.getOperandPartitions()[0];
			int movOutputAddress = nextInstruction.getOperandAddresses()[0];
			if (dataManager.isScalar(Memory.Partition.REGISTER, writingRegisterAddress)
					!= dataManager.isScalar(movOutputPartition, movOutputAddress)) {
				continue;
			}
			if (dataManager.isCachingEnabled(Memory.Partition.REGISTER, writingRegisterAddress)
					!= dataManager.isCachingEnabled(movOutputPartition, movOutputAddress)) {
				continue;
			}

			// (以下最適化案、全部のCached系演算周りの実装規模を倍増させてもいいくらいの気力がある時に要検討）
			//
			// 上記の if 文、この条件の影響で、例えば
			//
			//   REFELM  R0(dest) L0(src) C0(index);  // L0[C0] の参照をR0にリンク
			//   MOVELM  R1(dest) L1(src) C1(index);  // L1[C1] の値をR1に単純コピー
			//   MOV     R0(dest) R1(src)             // R1の値をR0に単純コピー (リンクされているL0[C0]も書き換わる)
			//
			// の場合に、R1 への他からの直接（そのアドレスで）アクセスさえ無ければ、
			//
			//   REFELM  R0(dest) L0(src) C0(index);  // L0[C0] の参照をR0にリンク
			//   MOVELM  R0(dest) R1(src) C1(index);  // L1[C1] の値をR0に単純コピー (リンクされているL0[C0]も書き換わる)
			//
			// に削る事は可能なはず。
			// しかし REFELM の dest は参照リンク対象のため常に uncacheable と判定されるので、
			// 上の if 文の条件では movOutputAddress が uncachable となり、現状のままではMOV削りが行われない。
			// なので現状の除外条件はもう少し絞り込めるはずで、可能性の検出があまり大がかりにならなければ絞り込みたい。
			//
			//   > MOV直前命令が uncacheable な dest（参照リンクされたレジスタとか）に書き込んでいる場合は置き換えて dest 削るとまずいけど、
			//     MOV直前命令が cacheable な dest（非リンクなスカラレジスタとか / 上の例でのR1）に書き込んでいて、
			//     それを直後に uncachable な別のもの dest2 (上の例での R0) へと MOV している場合は、
			//     MOV直前命令の dest を dest2 にして元の dest 削っても大丈夫だと思う(他のMOV削り条件を満たしていれば)。
			//     cacheable 判定されているという事は、本来の dest 先を別アドレス経由で読もうとする事はされていないはずなので。
			//     とすると上のif文は、cacheability に関してはやはり writingRegister... の条件のみ必要で、movOutput... に対しては不要だと思う。
			//     > ただそれによって、MOV直前命令が非Cached系のユニットで処理されるようになるので、それが果たして総合的に特になるかどうか ?
			//       MOV直前命令をCached系ユニットで処理して、直後に非CachedなMOVでコピーする方が総合的には速いかもしれない。
			//       MOV直前命令の処理は恐らくMOVよりは複雑な実装なので、それがCachedから非Cachedになるオーバーヘッド増が、
			//       果たして非Cached MOVを1個削れる分を上回るかどうか? 全種の命令において。微妙な所かもしれない。
			//       > MOVELM/REFELM は dest が uncacheable でも index が cacheable ならCached系ユニットで処理される。
			//         その場合は dest アクセスのみ仮想メモリに直書きされるだけなので、非Cached MOVを削った分以上に処理コストが増える事はないはず。
			//         > 上の例だけではなく一般に他の命令との組み合わせも有り得るので、命令とそのユニットの実装によっては遅くなる場合もあるはず。
			//           例えばADDの非Cached演算は全オペランドをキャッシュ介さず仮想メモリから読むので、Cached演算よりだいぶオーバーヘッド食う。
			//           > それなら全Cached系ユニットで、dest のみ uncacheable な場合を(最小コスト増で済む実装で)サポートすればデメリット無くMOV削り可能?
			//             例えばa[i] = b + c みたいなパターンだとa[i]部のみが(REFELMで参照した) uncacheable なレジスタになるけど、
			//             b と c は cacheable な普通のスカラである場合、CachedScalarArithmeticUnit 内で Float64CachedScalarAddNode の
			//             代入左辺のみ仮想メモリアクセスに直書きするような Float64SemiCached...Node みたいなのを作って、それを割り当てるなど。
			//             > 今思いつく限りではそうすればオーバーヘッド関連の懸念は解決しそうだけれど、全Cached系ユニットの実装規模がほぼ倍増する…
			//               普通にキャッシュ内で閉じた演算して直後に非Cached MOVで同期 兼 書き込みするのと比べて、それやるだけのメリットがあるかどうか…
			//               > でも右辺で単純な算術演算やって左辺の配列要素に格納するという場合は結構たくさんあると思うので、効く頻度は高そうな気がする。
			//                 パターンが累乗で増える Dual/TrippleArithmetic とかと比べれば2倍で済んで効果期待できそうなら意外とありかも。
			//                 (ただ他の優先度高い最適化案が一通り済んだ後に)
			//
			// またそのうち要検討

			// 次に続くMOV命令を削るとまずい命令ならスキップ（ELEM命令など）
			if(!movReducableOpcodeSet.contains(currentInstruction.getOperationCode())) {
				continue;
			}


			// --------------------------------------------------------------------------------
			// ここまで到達するのは：
			//
			// ・演算結果をレジスタに格納し、
			// ・次で別の領域にMOVしていて、
			// ・そのレジスタを他のどこでも使用しておらず、
			// ・命令の演算結果を、MOV先に直接格納するように改変しても問題ない場合
			//
			// なので、オペランドを入れ替えて冗長なMOV命令を削る
			// --------------------------------------------------------------------------------


			// 対象演算命令のオペランド部を複製
			int modifiedOperandLength = currentInstruction.getOperandLength();
			Memory.Partition[] modifiedOperandPartitions = new Memory.Partition[modifiedOperandLength];
			System.arraycopy(currentInstruction.getOperandPartitions(), 0, modifiedOperandPartitions, 0, modifiedOperandLength);
			int[] modifiedOperandAddresses = new int[modifiedOperandLength];
			System.arraycopy(currentInstruction.getOperandAddresses(), 0, modifiedOperandAddresses, 0, modifiedOperandLength);

			// 複製したオペランド部の出力オペランドに、MOVコピー「先」オペランドを写す
			modifiedOperandPartitions[0] = movOutputPartition;
			modifiedOperandAddresses[0] = movOutputAddress;

			// それをオペランド部として持つ、対象演算命令のコピーを生成し、元の対象演算命令を置き換える
			AcceleratorInstruction modifiedInstruction = new AcceleratorInstruction(
				currentInstruction, modifiedOperandPartitions, modifiedOperandAddresses
			);
			this.acceleratorInstructionList.set(instructionIndex, modifiedInstruction);

			// MOV命令を null で置き換える（すぐ後で削除する）
			this.acceleratorInstructionList.set(instructionIndex+1, null);

			// 対象命令が元々書き込んでいたレジスタはもうどこからも読み書きしていないはずなので、削除登録しておく（後で別の最適化で削る）
			this.unnecessaryRegisterSet.add(writingRegisterAddress);

			// 次の命令（= MOV）はもう削除したので、カウンタを1つ余計に進める
			instructionIndex++;

		}

		// MOV命令を削除した位置の null を詰める
		this.acceleratorInstructionList.removeAll(LIST_OF_NULL);
	}


	// 上のMOV削りによって使用されなくなったレジスタの確保処理（ALLOC命令）を削る
	//（これにより、そのレジスタはもうコード上で登場しなくなるはず）
	private void removeAllocInstructionsToUnusedRegisters() {
		int instructionLength = this.acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength-1; instructionIndex++) {
			AcceleratorInstruction instruction = this.acceleratorInstructionList.get(instructionIndex);

			// ALLOC系命令以外はスキップ
			if (instruction.getOperationCode() != OperationCode.ALLOC
					&& instruction.getOperationCode() != OperationCode.ALLOCR
					&& instruction.getOperationCode() != OperationCode.ALLOCP
					&& instruction.getOperationCode() != OperationCode.ALLOCT) {
				continue;
			}

			// メモリ確保対象（0番オペランド）がレジスタでない場合はスキップ
			if (instruction.getOperandPartitions()[0] != Memory.Partition.REGISTER) {
				continue;
			}

			// メモリ確保対象レジスタが削除リストに登録されていない場合はスキップ
			int allocRegisterAddress = instruction.getOperandAddresses()[0];
			if (!this.unnecessaryRegisterSet.contains(allocRegisterAddress)) {
				continue;
			}

			// ここまで到達するのは、使っていないレジスタをALLOCしている箇所なので、削る
			// (ここでは命令列リスト内の該当位置を null に置き換えておいて、後で一括で削る)
			this.acceleratorInstructionList.set(instructionIndex, null);
		}

		// MOV命令を削除した位置の null を詰める
		this.acceleratorInstructionList.removeAll(LIST_OF_NULL);
	}


	// 念のため上でALLOCを削ったレジスタにアクセスしている箇所を探し、もし見つかればエラーにする（見つからなければ何もしない）
	private void checkRemovedRegistersAreUnused() {
		int instructionLength = this.acceleratorInstructionList.size();
		for (int instructionIndex=0; instructionIndex<instructionLength-1; instructionIndex++) {
			AcceleratorInstruction instruction = this.acceleratorInstructionList.get(instructionIndex);
			Memory.Partition[] parts = instruction.getOperandPartitions();
			int[] addrs = instruction.getOperandAddresses();
			int operandLength = instruction.getOperandLength();

			for (int i=0; i<operandLength; i++) {

				// オペランドがレジスタで、かつ unnecessaryRegisterSet に登録されていれば、
				// 既に削除済みの（もう確保されない）レジスタのはずなのでエラー
				if (parts[i] == Memory.Partition.REGISTER && this.unnecessaryRegisterSet.contains(addrs[i])) {
					throw new VnanoFatalException("Optimization error (removed register \"R" +addrs[i] + "\" is accessed)");
				}
			}
		}
	}

}
