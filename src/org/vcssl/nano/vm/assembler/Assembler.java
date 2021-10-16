/*
 * Copyright(C) 2017-2021 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.assembler;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.interconnect.AbstractFunction;
import org.vcssl.nano.interconnect.AbstractVariable;
import org.vcssl.nano.interconnect.FunctionTable;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.interconnect.VariableTable;
import org.vcssl.nano.spec.AssemblyWord;
import org.vcssl.nano.spec.DataType;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.LiteralSyntax;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.vm.VirtualMachineObjectCode;
import org.vcssl.nano.vm.memory.Memory;
import org.vcssl.nano.vm.processor.Instruction;


/**
 * <p>
 * {@link org.vcssl.nano.compiler.Compiler} が出力した仮想アセンブリコード（文字列）を、
 * 実行用中間コードである {@link org.vcssl.nano.vm.VirtualMachineObjectCode}
 * オブジェクトに変換する、アセンブラのクラスです。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class Assembler {

	/**
	 * <span class="lang-en">
	 * Create a new assembler
	 * </span>
	 * <span class="lang-ja">
	 * アセンブラを生成します
	 * </span>
	 * .
	 * @param langSpec
	 *   <span class="lang-en">language specification settings.</span>
	 *   <span class="lang-ja">言語仕様設定.</span>
	 */
	public Assembler() {
	}

	/**
	 * ラベルや{@link OperationCode#CALL CALL}命令の直後など、
	 * 別の命令から飛ぶ着地点になり得る場所に、{@link OperationCode#LABEL LABEL}命令を配置したコードを返します。
	 * これは、各命令において、本来の演算の役割と、ジャンプの着地点としての役割を分離させる事で、
	 * 最適化を用意にするための処理です。
	 *
	 * @param assemblyCode 元の仮想アセンブリコード
	 * @return 着地点にLABEL命令を配置した仮想アセンブリコード
	 */
	private String appendLabelInstructions(String assemblyCode) {

		StringBuilder codeBuilder = new StringBuilder();

		String[] lines = assemblyCode.split(AssemblyWord.INSTRUCTION_SEPARATOR_REGEX);
		int lineLength = lines.length;

		for (int lineIndex=0; lineIndex<lineLength; lineIndex++) {

			String line = lines[lineIndex];
			codeBuilder.append(line);
			codeBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);

			line = line.trim();
			String labelCode
				= AssemblyWord.LINE_SEPARATOR
				+ AssemblyWord.WORD_SEPARATOR
				+ OperationCode.LABEL.name()
				+ AssemblyWord.WORD_SEPARATOR
				+ DataTypeName.VOID
				+ AssemblyWord.WORD_SEPARATOR
				+ AssemblyWord.PLACEHOLDER_OPERAND_PREFIX
				+ AssemblyWord.INSTRUCTION_SEPARATOR
				;

			// 空行
			if (line.length() == 0) {
				continue;
			}

			// ラベルディレクティブの場合はLABEL命令を置く
			if (line.startsWith(AssemblyWord.LABEL_DIRECTIVE)) {
				codeBuilder.append(labelCode);
				continue;
			}

			String[] words = line.split(AssemblyWord.WORD_SEPARATOR_REGEX);
			String operationCode = words[0];

			// CALL命令の直後にLABEL命令を置く（戻り先の着地点になるため）
			if (operationCode.equals(OperationCode.CALL.name())) {
				codeBuilder.append(labelCode);
			}
		}
		return codeBuilder.toString();
	}


	/**
	 * 仮想アセンブリコードを解釈し、より実行に適した中間コードである、
	 * {@link org.vcssl.nano.vm.VirtualMachineObjectCode}
	 * オブジェクトに変換して返します。
	 *
	 * @param assemblyCode 仮想アセンブリコード
	 * @param Intterconnect interconnect 外部変数・関数の情報を保持しているインターコネクト
	 * @return 実行用中間コード
	 * @throws VnanoException 仮想アセンブリコードの内容に異常があった場合にスローされます。
	 */
	public VirtualMachineObjectCode assemble(String assemblyCode, Interconnect interconnect)
			throws VnanoException { // 例外は後で一本化すべき


		// !!!  1メソッドに突っ込みすぎなので分割して要リファクタ


		// 最初に、コード内の文字列リテラルを全て "1", "2", ... などのように番号化リテラルで置き換える
		String[] stringLiteralExtractResult = LiteralSyntax.extractStringLiterals(assemblyCode);
		assemblyCode = stringLiteralExtractResult[0]; // [0] 番に置き換え済みコードが格納されている（1番以降はリテラル内容）

		// ラベルやCALL命令の着地点の位置にLABEL命令を配置（最適化しやすくするため）
		assemblyCode = this.appendLabelInstructions(assemblyCode);


		// インターコネクトから外部変数・外部関数のテーブルを取得
		VariableTable globalVariableTable = interconnect.getExternalVariableTable();
		FunctionTable functionTable = interconnect.getExternalFunctionTable();

		VirtualMachineObjectCode intermediateCode = this.preprocessDirectives(assemblyCode, globalVariableTable, functionTable);
		int registerMaxAddress = 0;

		int constantAddress = 0;

		String[] lines = assemblyCode.split(AssemblyWord.INSTRUCTION_SEPARATOR_REGEX);
		int lineLength = lines.length;

		int metaAddress = -1;

		String sourceFileName = "(none)";
		int sourceLineNumber = -1;

		for (int i=0; i<lineLength; i++) {

			String line = lines[i].trim();
			if (line.length() == 0) {
				continue;
			}

			String[] words = line.split(AssemblyWord.WORD_SEPARATOR_REGEX);
			int wordLength = words.length;

			// メタディレクティブ -> 内容を控える定数データを生成
			if (line.startsWith(AssemblyWord.META_DIRECTIVE)) {

				// 前処理で番号化された文字列リテラルから、元の文字列リテラルに戻す
				int stringLiteralIndex = LiteralSyntax.getIndexOfNumberedStringLiteral(words[1].trim());
				String originalStringLiteral = stringLiteralExtractResult[ stringLiteralIndex ];

				String metaImmediateValue
					= Character.toString(AssemblyWord.IMMEDIATE_OPERAND_PREFIX)
					+ DataTypeName.getDataTypeNameOf(DataType.STRING)
					+ AssemblyWord.VALUE_SEPARATOR
					+ originalStringLiteral;
				intermediateCode.addConstantData(metaImmediateValue, constantAddress);
				metaAddress = constantAddress;
				constantAddress++;

				// 後々でここで sourceFileName と sourceLineNumber の値の設定を行う（アセンブラのリファクタ時の予定）

				continue;

			} else if (line.startsWith(Character.toString(AssemblyWord.DIRECTIVE_PREFIX))) {
				continue;
			}


			OperationCode operationCode = OperationCode.valueOf(words[0]);

			String[] dataTypeNames = words[1].split(AssemblyWord.VALUE_SEPARATOR_REGEX);
			int dataTypeLength = dataTypeNames.length;
			DataType[] dataTypes = new DataType[dataTypeLength];
			for (int dataTypeIndex=0; dataTypeIndex<dataTypeLength; dataTypeIndex++) {
				try {
					dataTypes[dataTypeIndex] = DataTypeName.getDataTypeOf(dataTypeNames[dataTypeIndex]);
				} catch (VnanoException e) {
					e.setFileName(sourceFileName);
					e.setLineNumber(sourceLineNumber);
					throw e;
				}
			}


			// データオペランド数 = ワード数 - オペコード1 - 型情報1 = ワード数 - 2
			int operandLength = wordLength - 2;
			Memory.Partition[] operandAddressTypes = new Memory.Partition[ operandLength ];
			int operandAddresses[] = new int[operandLength];


			for (int operandIndex=0; operandIndex<operandLength; operandIndex++) {

				// データオペランドは2番目から始まる
				int wordIndex = operandIndex + 2;
				String word = words[wordIndex].trim();
				char prefix = word.charAt(0); // 先頭の文字がアドレスタイプに対応している

				// switch 文は使えない
				if (prefix == AssemblyWord.IMMEDIATE_OPERAND_PREFIX) {
						if (intermediateCode.containsConstantData(word)) {
							operandAddresses[operandIndex] = intermediateCode.getConstantDataAddress(word);
						} else {

							// 文字列リテラルの場合は、前処理で番号化されているので元に戻し、エスケープシーケンスも処理
							if (this.getDataTypeOfImmediateValueLiteral(word) == DataType.STRING) {
								String literalValue = this.getValuePartOfImmediateValueLiteral(word);
								int stringLiteralIndex = LiteralSyntax.getIndexOfNumberedStringLiteral(literalValue);
								literalValue = stringLiteralExtractResult[stringLiteralIndex];
								literalValue = LiteralSyntax.decodeEscapeSequences(literalValue);
								word = this.replaceImmediateValue(word, literalValue);
							}

							operandAddresses[operandIndex] = constantAddress;
							intermediateCode.addConstantData(word, constantAddress);
							constantAddress++;
						}
						operandAddressTypes[operandIndex] = Memory.Partition.CONSTANT;

				} else if (prefix == AssemblyWord.REGISTER_OPERAND_PREFIX) {

						String addressWord = word.substring(1, word.length());
						int registerAddress = Integer.parseInt(addressWord);
						operandAddresses[operandIndex] = registerAddress;
						if (!intermediateCode.containsRegister(registerAddress)) {
							intermediateCode.addRegister(registerAddress);
						}
						operandAddressTypes[operandIndex] = Memory.Partition.REGISTER;
						if (registerMaxAddress < operandAddresses[operandIndex]) {
							registerMaxAddress = operandAddresses[operandIndex];
						}

				} else if (prefix == AssemblyWord.IDENTIFIER_OPERAND_PREFIX) {

						// ローカル変数の場合はローカルデータアドレスに変換(グローバルよりも優先)
						if (intermediateCode.containsLocalVariable(word)) {

							operandAddresses[operandIndex] = intermediateCode.getLocalVariableAddress(word);
							operandAddressTypes[operandIndex] = Memory.Partition.LOCAL;

						// グローバル変数の場合はグローバルデータアドレスに変換
						} else if (intermediateCode.containsGlobalVariable(word)) {

							operandAddresses[operandIndex] = intermediateCode.getGlobalVariableAddress(word);
							operandAddressTypes[operandIndex] = Memory.Partition.GLOBAL;

						// 関数の場合は、ルーチンアドレスを格納する定数データのアドレスに変換
						} else if (intermediateCode.containsFunction(word)) {

							int functionAddress = intermediateCode.getFunctionAddress(word);

							String functionAddressImmediateValue
									= Character.toString(AssemblyWord.IMMEDIATE_OPERAND_PREFIX)
									+ DataTypeName.getDataTypeNameOf(DataType.INT64)
									+ AssemblyWord.VALUE_SEPARATOR
									+ Integer.toString(functionAddress);

							operandAddressTypes[operandIndex] = Memory.Partition.CONSTANT;
							operandAddresses[operandIndex] = constantAddress;
							intermediateCode.addConstantData(functionAddressImmediateValue, constantAddress);
							constantAddress++;

						} else {
							throw new VnanoFatalException("Undefined identifier has detected in operands: " + word);
						}

				} else if (prefix == AssemblyWord.LABEL_OPERAND_PREFIX) {

						int labelAddress = intermediateCode.getLabelAddress(word);

						String functionAddressImmediateValue
								= Character.toString(AssemblyWord.IMMEDIATE_OPERAND_PREFIX)
								+ DataTypeName.getDataTypeNameOf(DataType.INT64)
								+ AssemblyWord.VALUE_SEPARATOR
								+ Integer.toString(labelAddress);

						operandAddressTypes[operandIndex] = Memory.Partition.CONSTANT;
						operandAddresses[operandIndex] = constantAddress;
						intermediateCode.addConstantData(functionAddressImmediateValue, constantAddress);
						constantAddress++;

				} else if (prefix == AssemblyWord.PLACEHOLDER_OPERAND_PREFIX) {
						operandAddresses[operandIndex] = 0;
						operandAddressTypes[operandIndex] = Memory.Partition.NONE;
				}
			}

			intermediateCode.addInstruction(
				new Instruction(
					operationCode, dataTypes, operandAddressTypes, operandAddresses,
					Memory.Partition.CONSTANT, metaAddress
				)
			);
		}

		return intermediateCode;
	}

	/**
	 * 即値リテラルのデータ型部を読み取って返します。
	 *
	 * @param immediateValueWord 即値リテラル
	 * @return 読みとったデータ型
	 * @throws AssemblyCodeException 不明なデータ型が記述されていた場合にスローされます。
	 */
	private DataType getDataTypeOfImmediateValueLiteral(String immediateValueLiteral) throws VnanoException {

		int separatorIndex = immediateValueLiteral.indexOf(AssemblyWord.VALUE_SEPARATOR);
		String dataTypeName = immediateValueLiteral.substring(1, separatorIndex);

		DataType dataType = DataTypeName.getDataTypeOf(dataTypeName);
		return dataType;
	}

	/**
	 * 即値リテラル内の値部分を読み取り、文字列のまま返します。
	 *
	 * @param immediateValueLiteral 即値リテラル
	 * @return 読みとった値部分
	 */
	private String getValuePartOfImmediateValueLiteral(String immediateValueLiteral) {
		int separatorIndex = immediateValueLiteral.indexOf(AssemblyWord.VALUE_SEPARATOR);
		String valuePart = immediateValueLiteral.substring(separatorIndex+1, immediateValueLiteral.length());
		return valuePart;
	}

	/**
	 * 即値リテラル内の値部分を、指定された値で置き換えたものを返します。
	 *
	 * @param immediateValueLiteral 元の即値リテラル
	 * @param newValue 即値リテラルの値部分を置き換える値
	 * @return 置き換え後の即値リテラル（データ型部は保たれます）
	 */
	private String replaceImmediateValue(String immediateValueLiteral, String newValue) {
		int separatorIndex = immediateValueLiteral.indexOf(AssemblyWord.VALUE_SEPARATOR);
		String frontPart = immediateValueLiteral.substring(0, separatorIndex);
		String newLiteral = frontPart + AssemblyWord.VALUE_SEPARATOR + newValue;
		return newLiteral;
	}


	/**
	 * 中間アセンブリコード内の各種ディレクティブを解釈し、識別子やラベルなど、
	 * シンボルテーブル情報を設定された中間オブジェクトコードを生成して返します。
	 *
	 * このメソッドは、{@link Assembler#assemble assemble} メソッド内において、
	 * 命令アセンブル処理に先立って、前処理として実行されます。
	 * 従って、このメソッドが返す中間オブジェクトコードは、
	 * まだ命令オブジェクト列を含んでいない、未完成の状態のものです。
	 *
	 * @param assemblyCode 中間アセンブリコード
	 * @return シンボルテーブル情報を設定された（未完成の）中間オブジェクトコード
	 */
	private VirtualMachineObjectCode preprocessDirectives(String assemblyCode, VariableTable globalVariableTable, FunctionTable functionTable) {

		VirtualMachineObjectCode assembledObject = new VirtualMachineObjectCode();

		int localAddress = 0;

		String[] lines = assemblyCode.split(AssemblyWord.INSTRUCTION_SEPARATOR_REGEX);
		int lineLength = lines.length;

		int instructionIndex = 0;

		for (int lineIndex=0; lineIndex<lineLength; lineIndex++) {

			String line = lines[lineIndex].trim();
			if (line.length() == 0) {
				continue;
			}
			String[] words = line.split(AssemblyWord.WORD_SEPARATOR_REGEX);

			// ローカルディレクティブ
			if (line.startsWith(AssemblyWord.LOCAL_VARIABLE_DIRECTIVE)) {
				String identifier = words[1];
				assembledObject.addLocalVariable(identifier, localAddress);
				localAddress++;
			}

			// グローバルディレクティブ
			if (line.startsWith(AssemblyWord.GLOBAL_VARIABLE_DIRECTIVE)) {
				String identifier = words[1];
				AbstractVariable variable = globalVariableTable.getVariableByAssemblyIdentifier(identifier);
				int globalAddress = globalVariableTable.getIndexOf(variable);
				assembledObject.addGlobalVariable(identifier, globalAddress);
			}

			// 関数ディレクティブ
			if (line.startsWith(AssemblyWord.GLOBAL_FUNCTION_DIRECTIVE)) {
				String identifier = words[1];
				String signature = identifier.substring(1, identifier.length()); // 先頭文字は識別子プレフィックスなので除去
				AbstractFunction function = functionTable.getFunctionBySignature(signature);
				int functionAddress = functionTable.getIndexOf(function);
				assembledObject.addFunction(identifier, functionAddress);
			}

			// ラベルディレクティブ
			if (line.startsWith(AssemblyWord.LABEL_DIRECTIVE)) {
				String identifier = words[1];
				assembledObject.addLabel(identifier, instructionIndex);
			}

			if (!line.startsWith(Character.toString(AssemblyWord.DIRECTIVE_PREFIX))) {
				instructionIndex++;
			}
		}
		return assembledObject;
	}

}
