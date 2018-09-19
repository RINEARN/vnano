/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.assembler;

import org.vcssl.nano.VnanoIntermediateCode;
import org.vcssl.nano.lang.AbstractFunction;
import org.vcssl.nano.lang.AbstractVariable;
import org.vcssl.nano.lang.DataType;
import org.vcssl.nano.lang.FunctionTable;
import org.vcssl.nano.lang.VariableTable;
import org.vcssl.nano.memory.DataException;
import org.vcssl.nano.memory.Memory;
import org.vcssl.nano.VnanoRuntimeException;
import org.vcssl.nano.interconnect.Interconnect;
import org.vcssl.nano.processor.Instruction;
import org.vcssl.nano.processor.OperationCode;
import org.vcssl.nano.spec.AssemblyWord;
import org.vcssl.nano.spec.DataTypeName;


/**
 * <p>
 * {@link org.vcssl.nano.compiler.Compiler} が出力した仮想アセンブリコード（文字列）を、
 * 実行用中間コードである {@link org.vcssl.nano.VnanoIntermediateCode}
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
	 * {@link org.vcssl.nano.VnanoIntermediateCode}
	 * オブジェクトに変換して返します。
	 *
	 * @param assemblyCode 仮想アセンブリコード
	 * @param Intterconnect interconnect 外部変数・関数の情報を保持しているインターコネクト
	 * @return 実行用中間コード
	 * @throws AssemblyCodeException 仮想アセンブリコードの内容に異常があった場合にスローされます。
	 */
	public VnanoIntermediateCode assemble(String assemblyCode, Interconnect interconnect)
			throws AssemblyCodeException, DataException {

		// インターコネクトから外部変数・外部関数のテーブルを取得
		VariableTable globalVariableTable = interconnect.getGlobalVariableTable();
		FunctionTable functionTable = interconnect.getGlobalFunctionTable();

		VnanoIntermediateCode intermediateCode = this.preprocessDirectives(assemblyCode, globalVariableTable, functionTable);
		int registerMaxAddress = 0;

		int constantAddress = 0;

		String[] lines = assemblyCode.split(AssemblyWord.INSTRUCTION_SEPARATOR_REGEX);
		int lineLength = lines.length;

		int metaAddress = -1;

		for (int i=0; i<lineLength; i++) {

			String line = lines[i].trim();
			if (line.length() == 0) {
				continue;
			}

			String[] words = line.split(AssemblyWord.WORD_SEPARATOR_REGEX);
			int wordLength = words.length;


			// メタディレクティブ -> 内容を控える定数データを生成
			if (line.startsWith(AssemblyWord.META_DIRECTIVE)) {
				String metaImmediateValue
					= Character.toString(AssemblyWord.OPERAND_PREFIX_IMMEDIATE)
					+ DataTypeName.getDataTypeNameOf(DataType.STRING)
					+ AssemblyWord.VALUE_SEPARATOR
					+ words[1].trim();
				intermediateCode.addConstantData(metaImmediateValue, constantAddress);
				metaAddress = constantAddress;
				constantAddress++;
				continue;
			} else if (line.startsWith(Character.toString(AssemblyWord.DIRECTIVE_PREFIX))) {
				continue;
			}


			//Instruction.OperationCode operationCode = Mnemonic.OPERATION_CODE_MAP.get(words[0]);
			OperationCode operationCode = OperationCode.valueOf(words[0]);

			String[] dataTypeNames = words[1].split(AssemblyWord.VALUE_SEPARATOR_REGEX);
			int dataTypeLength = dataTypeNames.length;
			DataType[] dataTypes = new DataType[dataTypeLength];
			for (int dataTypeIndex=0; dataTypeIndex<dataTypeLength; dataTypeIndex++) {
				dataTypes[dataTypeIndex] = DataTypeName.getDataTypeOf(dataTypeNames[dataTypeIndex]);
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
							// 暫定的な簡易例外処理
							throw new VnanoRuntimeException();
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
				}
			}

			Instruction instruction = new Instruction(
					operationCode, dataTypes, operandAddressTypes, operandAddresses,
					Memory.Partition.CONSTANT, metaAddress
			);
			intermediateCode.addInstruction(instruction);
		}

		return intermediateCode;
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
	private VnanoIntermediateCode preprocessDirectives(String assemblyCode, VariableTable globalVariableTable, FunctionTable functionTable) {

		VnanoIntermediateCode assembledObject = new VnanoIntermediateCode();

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
			if (line.startsWith(AssemblyWord.FUNCTION_DIRECTIVE)) {
				String identifier = words[1];
				AbstractFunction function = functionTable.getFunctionByAssemblyIdentifier(identifier);
				int functionAddress = functionTable.indexOf(function);
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
