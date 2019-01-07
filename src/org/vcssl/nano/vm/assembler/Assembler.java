/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.vm.assembler;

import org.vcssl.nano.lang.AbstractFunction;
import org.vcssl.nano.lang.AbstractVariable;
import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.lang.FunctionTable;
import org.vcssl.nano.lang.VariableTable;
import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.VnanoException;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.spec.AssemblyWord;
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
	 * このクラスは状態を保持するフィールドを持たないため、コンストラクタは何もしません。
	 */
	public Assembler() {
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

		// インターコネクトから外部変数・外部関数のテーブルを取得
		VariableTable globalVariableTable = interconnect.getGlobalVariableTable();
		FunctionTable functionTable = interconnect.getGlobalFunctionTable();

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
					= Character.toString(AssemblyWord.OPERAND_PREFIX_IMMEDIATE)
					+ DataTypeName.getDataTypeNameOf(DataType.STRING)
					+ AssemblyWord.VALUE_SEPARATOR
					+ originalStringLiteral;
				intermediateCode.addConstantData(metaImmediateValue, constantAddress);
				metaAddress = constantAddress;
				constantAddress++;

				// 後々でここで sourceFileName と sourceLineNumber の値の設定を行う（アセンブラのリファクタ時の予定）

				continue;

			// ラベルディレクティブ -> NOPを置く（ジャンプ先命令に、演算ではなく着地点の役割だけを担わせる事で、最適化を容易にする）
			} else if (line.startsWith(AssemblyWord.LABEL_DIRECTIVE)) {

				intermediateCode.addInstruction(
					new Instruction(
						OperationCode.NOP, new DataType[]{DataType.VOID},
						new Memory.Partition[0], new int[0], Memory.Partition.CONSTANT, metaAddress
					)
				);
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

				switch (prefix) {

					case AssemblyWord.OPERAND_PREFIX_IMMEDIATE : {
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
						break;
					}

					case AssemblyWord.OPERAND_PREFIX_REGISTER : {
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
						break;
					}

					case AssemblyWord.OPERAND_PREFIX_IDENTIFIER : {

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
									= Character.toString(AssemblyWord.OPERAND_PREFIX_IMMEDIATE)
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

						break;
					}

					// ! ラベル用の識別子プレフィックスは要再考
					//（現状は整数の即値としているが、命令アドレスである事を示すプレフィックスがあるべき？）
					case AssemblyWord.OPERAND_PREFIX_LABEL : {

						int labelAddress = intermediateCode.getLabelAddress(word);

						String functionAddressImmediateValue
								= Character.toString(AssemblyWord.OPERAND_PREFIX_IMMEDIATE)
								+ DataTypeName.getDataTypeNameOf(DataType.INT64)
								+ AssemblyWord.VALUE_SEPARATOR
								+ Integer.toString(labelAddress);

						operandAddressTypes[operandIndex] = Memory.Partition.CONSTANT;
						operandAddresses[operandIndex] = constantAddress;
						intermediateCode.addConstantData(functionAddressImmediateValue, constantAddress);
						constantAddress++;

						break;
					}

					case AssemblyWord.OPERAND_PREFIX_PLACEHOLDER : {
						operandAddresses[operandIndex] = 0;
						operandAddressTypes[operandIndex] = Memory.Partition.NONE;
						break;
					}
				}
			}

			intermediateCode.addInstruction(
				new Instruction(
					operationCode, dataTypes, operandAddressTypes, operandAddresses,
					Memory.Partition.CONSTANT, metaAddress
				)
			);

			// CALL命令の次の位置は、RET命令で飛んで来る着地点になるので、ラベル同様にNOPを置いておく
			//（ジャンプ先命令に、演算ではなく着地点の役割だけを担わせる事で、最適化を容易にする）
			if (operationCode == OperationCode.CALL) {

				intermediateCode.addInstruction(
					new Instruction(
						OperationCode.NOP, new DataType[]{DataType.VOID},
						new Memory.Partition[0], new int[0], Memory.Partition.CONSTANT, metaAddress
					)
				);
			}
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
				int globalAddress = globalVariableTable.indexOf(variable);
				assembledObject.addGlobalVariable(identifier, globalAddress);
			}

			// 関数ディレクティブ
			if (line.startsWith(AssemblyWord.GLOBAL_FUNCTION_DIRECTIVE)) {
				String identifier = words[1];
				AbstractFunction function = functionTable.getFunctionByAssemblyIdentifier(identifier);
				int functionAddress = functionTable.indexOf(function);
				assembledObject.addFunction(identifier, functionAddress);
			}

			// ラベルディレクティブ
			if (line.startsWith(AssemblyWord.LABEL_DIRECTIVE)) {
				String identifier = words[1];
				assembledObject.addLabel(identifier, instructionIndex);
				instructionIndex++; // 他のディレクティブとは異なり、ラベルの位置にはNOP命令を置くのでカウンタを進める
			}

			if (!line.startsWith(Character.toString(AssemblyWord.DIRECTIVE_PREFIX))) {
				instructionIndex++;
			}
		}
		return assembledObject;
	}

}
