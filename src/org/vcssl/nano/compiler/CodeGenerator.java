/*
 * Copyright(C) 2017-2019 RINEARN (Fumihiro Matsui)
 * This software is released under the MIT License.
 */

package org.vcssl.nano.compiler;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.vcssl.nano.VnanoFatalException;
import org.vcssl.nano.spec.AssemblyWord;
import org.vcssl.nano.spec.DataTypeName;
import org.vcssl.nano.spec.IdentifierSyntax;
import org.vcssl.nano.spec.LiteralSyntax;
import org.vcssl.nano.spec.OperationCode;
import org.vcssl.nano.spec.OptionValue;
import org.vcssl.nano.spec.ScriptWord;

//Documentation:  https://www.vcssl.org/en-us/dev/code/main-jimpl/api/org/vcssl/nano/compiler/CodeGenerator.html
//ドキュメント:   https://www.vcssl.org/ja-jp/dev/code/main-jimpl/api/org/vcssl/nano/compiler/CodeGenerator.html

/**
 * <p>
 * <span class="lang-en">
 * The class performing the function of the code generator in the compiler of the Vnano
 * </span>
 * <span class="lang-ja">
 * Vnano のコンパイラ内において, コードジェネレータ（コード生成器）の機能を担うクラスです
 * </span>
 * .
 * </p>
 *
 * <p>
 * <span class="lang-en">
 * The processing of this class takes the semantic-analyzed AST as the input,
 * and outputs a kind of the intermediate code written in the VRIL (Vector Register Intermediate Language)
 * which is a virtual assembly language interpreted by the VM
 * ({@link org.vcssl.nano.vm.VirtualMachine}) of the Vnano.
 * </span>
 * <span class="lang-ja">
 * このクラスが行う処理は, 入力として意味解析済みのASTを受け取り,
 * それを Vnano のVM ({@link org.vcssl.nano.vm.VirtualMachine}) で解釈可能な、
 * 一種の中間コードに変換して出力します.
 * この中間コードは、Vnano のVM用に設計された仮想的なアセンブリ言語である,
 * VRIL（Vector Register Intermediate Language）で記述されます.
 * </span>
 * </p>
 *
 * <p>
 * &raquo; <a href="../../../../../src/org/vcssl/nano/compiler/CodeGenerator.java">Source code</a>
 * </p>
 *
 * <hr>
 *
 * <p>
 * | <a href="../../../../../api/org/vcssl/nano/compiler/CodeGenerator.html">Public Only</a>
 * | <a href="../../../../../api-all/org/vcssl/nano/compiler/CodeGenerator.html">All</a> |
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class CodeGenerator {

	/**
	 * <span class="lang-ja">スカラの配列次元数です</span>
	 * <span class="lang-en">The array-rank of the scalar</span>
	 * .
	 */
	private static final int RANK_OF_SCALAR = 0;

	/**
	 * <span class="lang-en">The string used as the name of labels excluding numbers</span>
	 * <span class="lang-ja">ラベルの名称において, 末尾の番号を除いた部分に使用される文字列です<span>
	 * .
	 */
	private static final String LABEL_NAME = "LABEL";

	/**
	 * <span class="lang-en">The immediate value of "true", used in code of jump instructions</span>
	 * <span class="lang-ja">ジャンプ命令のコード生成などに使用される, true を表す即値の文字列です<span>
	 * .
	 */
	private static final String IMMEDIATE_TRUE
			= AssemblyWord.OPERAND_PREFIX_IMMEDIATE + DataTypeName.BOOL + AssemblyWord.VALUE_SEPARATOR + LiteralSyntax.TRUE;

	/**
	 * <span class="lang-en">The string of the placeholder which is put at the position of the unused operand for some instructions</span>
	 * <span class="lang-ja">命令仕様上、使用しない位置のオペランドの箇所に置くプレースホルダ</span>
	 * .
	 */
	private static final String PLACE_HOLDER = Character.toString(AssemblyWord.OPERAND_PREFIX_PLACEHOLDER);


	/**
	 * <span class="lang-en">The counter to assign new registers</span>
	 * <span class="lang-ja">新規レジスタの割り当てに用いるカウンタです</span>
	 * .
	 */
	private int registerCounter;

	/**
	 * <span class="lang-en">The counter to assign new labels</span>
	 * <span class="lang-ja">新規ラベルの割り当てに用いるカウンタです</span>
	 * .
	 */
	private int labelCounter;

	/**
	 * <span class="lang-en">
	 * Creates an new code generator of which counters (e.g. counter to assign registers) are initialized by 0
	 * </span>
	 * <span class="lang-ja">
	 * 各種カウンタ値（レジスタの割り当てカウンタなど）が 0 で初期化されたコードジェネレータを作成します
	 * </span>
	 * .
	 */
	public CodeGenerator() {
		this.registerCounter = 0;
		this.labelCounter = 0;
	}



	/**
	 * <span class="lang-ja">
	 * The class for storing information (context) depending on before/after statements which are
	 * necessary for transforming sequentially a statement to the (virtual) assembly code from the top.
	 * </span>
	 * <span class="lang-ja">
	 * 文を逐次的にコードに変換していく際に必要な, 前後の文に依存する情報（コンテキスト）を保持するクラスです
	 * </span>
	 * .
	 * <span class="lang-en">
	 * For example, labels for breaking from the loop should be put at the end the instructions
	 * corresponding with statements in the loop,
	 * but should be determined when the instructions at the top of the loop are generated.
	 * It is because instructions at the top of the loop contains a jump instruction
	 * to jump to outside of the loop when the condition is false,
	 * and the jump instruction takes the label as an operand.
	 * </span>
	 * </span>
	 * <span class="lang-ja">
	 * 例えば、if 文や while 文 および for 文では、その後に続く文のコード生成が完了した後に、
	 * 条件不成立時やループ脱出時のジャンプ先となるラベルを配置する必要があります。
	 * また、else 文では、直前の if 文の条件式の結果が、
	 * 仮想メモリ上のどのアドレスに保持されているのかという情報が必要です。
	 * </span>
	 *
	 * <span class="lang-en">
	 * This class is used in
	 * {@link CodeGenerator#trackAllStatements CodeGenerator.trackAllStatements}
	 * and
	 * {@link CodeGenerator#generateStatementCode CodeGenerator.generateStatementCode}
	 * methods.
	 * </span>
	 * <span class="lang-ja">
	 * {@link CodeGenerator#trackAllStatements CodeGenerator.trackAllStatements}
	 * メソッドや
	 * {@link CodeGenerator#generateStatementCode CodeGenerator.generateStatementCode}
	 * メソッド内で使用されます。
	 * </span>
	 */
	private class StatementTrackingContext implements Cloneable {

		private String beginPointLabel = null; // ループ文などで、後の文の生成後の地点に先頭に戻るコードを置いてほしい場合、これに先頭地点のラベル値を入れる
		private String updatePointLabel = null; // ループ文などで、ループ毎の更新処理の直前にラベルを置いてほしい場合、これにラベル値を入れる
		private String updatePointStatement = null; // for文の更新式の文
		private LinkedList<String> endPointLabelList = null; // IF文など、後の文の生成後の地点にラベルを置いてほしい場合、これにラベル値を入れる(ifの後にelseが続く場合のみ要素が2個になり得る)
		private String endPointStatement = null; // 関数など、ブロック末尾にデフォルトの return 文などを置きたい場合、これに文を入れる

		private String lastIfConditionRegister = null; // 直前の if 文の条件式結果を格納するレジスタを控えて、else 文で使う
		private String lastLoopBeginPointLabel = null; // 最後に踏んだループの始点ラベル
		private String lastLoopUpdatePointLabel = null; // 最後に踏んだループの更新ラベル
		private String lastLoopEndPointLabel = null;   // 最後に踏んだループの終点ラベル
		private String lastFunctionLabel = null; // 最後に踏んだ関数の先頭ラベル

		private String outerLoopBeginPointLabel = null; // 1階層外のループの始点ラベル
		private String outerLoopUpdatePointLabel = null; // 1階層外のループの更新ラベル
		private String outerLoopEndPointLabel = null; // 1階層外のループの終点ラベル

		private boolean isNextBlockLoop = false;

		private AstNode[] statementNodes = null;
		private int statementLength = -1;
		private int statementIndex = -1;
		private String lastStatementCode = null;

		public StatementTrackingContext() {
			this.endPointLabelList = new LinkedList<String>();
		}

		@SuppressWarnings("unchecked")
		public StatementTrackingContext clone() {
			StatementTrackingContext clone = new StatementTrackingContext();

			clone.beginPointLabel = this.beginPointLabel;
			clone.updatePointLabel = this.updatePointLabel;
			clone.updatePointStatement = this.updatePointStatement;
			clone.endPointLabelList = (LinkedList<String>)this.endPointLabelList.clone();
			clone.endPointStatement = this.endPointStatement;

			// （これらの状態変数は無駄が多い気がするのでそのうちリファクタリングしたい。lastLoop系を削ってouterLoop系のみに。）
			clone.lastIfConditionRegister = this.lastIfConditionRegister;
			clone.lastLoopBeginPointLabel = this.lastLoopBeginPointLabel;
			clone.lastLoopUpdatePointLabel = this.lastLoopUpdatePointLabel;
			clone.lastLoopEndPointLabel = this.lastLoopEndPointLabel;
			clone.lastFunctionLabel = this.lastFunctionLabel;
			clone.outerLoopBeginPointLabel = this.outerLoopBeginPointLabel;
			clone.outerLoopUpdatePointLabel = this.outerLoopUpdatePointLabel;
			clone.outerLoopEndPointLabel = this.outerLoopEndPointLabel;

			clone.isNextBlockLoop = this.isNextBlockLoop;

			clone.statementNodes = this.statementNodes;
			clone.statementIndex = this.statementIndex;
			clone.statementLength = this.statementLength;
			clone.lastStatementCode = this.lastStatementCode;

			return clone;
		}

		public String getBeginPointLabel() {
			return this.beginPointLabel;
		}
		public void setBeginPointLabel(String beginPointLabel) {
			this.beginPointLabel = beginPointLabel;
		}
		public boolean hasBeginPointLabel() {
			return this.beginPointLabel != null;
		}
		public void clearBeginPointLabel() {
			this.beginPointLabel = null;
		}

		public String getUpdatePointLabel() {
			return this.updatePointLabel;
		}
		public void setUpdatePointLabel(String updatePointLabel) {
			this.updatePointLabel = updatePointLabel;
		}
		public boolean hasUpdatePointLabel() {
			return this.updatePointLabel != null;
		}
		public void clearUpdatePointLabel() {
			this.updatePointLabel = null;
		}

		public String[] getEndPointLabels() {
			return this.endPointLabelList.toArray(new String[0]);
		}
		public void addEndPointLabel(String endPointLabel) {
			this.endPointLabelList.add(endPointLabel);
		}
		public boolean hasEndPointLabel() {
			return !this.endPointLabelList.isEmpty();
		}
		public void clearEndPointLabel() {
			this.endPointLabelList.clear();
		}

		public boolean hasEndPointStatement() {
			return this.endPointStatement != null;
		}
		public void setEndPointStatement(String endPointStatement) {
			this.endPointStatement = endPointStatement;
		}
		public String getEndPointStatement() {
			return this.endPointStatement;
		}
		public void clearEndPointStatement() {
			this.endPointStatement = null;
		}

		public String getUpdatePointStatement() {
			return this.updatePointStatement;
		}
		public void setUpdatePointStatement(String updatePointStatement) {
			this.updatePointStatement = updatePointStatement;
		}
		public boolean hasUpdatePointStatement() {
			return this.updatePointStatement != null;
		}
		public void clearUpdatePointStatement() {
			this.updatePointStatement = null;
		}

		public boolean hasLastIfConditionRegister() {
			return this.lastIfConditionRegister != null;
		}
		public String getLastIfConditionRegister() {
			return this.lastIfConditionRegister;
		}
		public void setLastIfConditionValue(String lastIfConditionValue) {
			this.lastIfConditionRegister = lastIfConditionValue;
		}

		public String getLastLoopBeginPointLabel() {
			return this.lastLoopBeginPointLabel;
		}

		public void setLastLoopBeginPointLabel(String lastLoopBeginPointLabel) {
			this.lastLoopBeginPointLabel = lastLoopBeginPointLabel;
		}

		public boolean hasLastLoopUpdatePointLabel() {
			return this.lastLoopUpdatePointLabel != null;
		}

		public String getLastLoopUpdatePointLabel() {
			return this.lastLoopUpdatePointLabel;
		}

		public void setLastLoopUpdatePointLabel(String lastLoopUpdatePointLabel) {
			this.lastLoopUpdatePointLabel = lastLoopUpdatePointLabel;
		}

		public String getLastLoopEndPointLabel() {
			return lastLoopEndPointLabel;
		}

		public void setLastLoopEndPointLabel(String lastLoopEndPointLabel) {
			this.lastLoopEndPointLabel = lastLoopEndPointLabel;
		}


		public String getOuterLoopBeginPointLabel() {
			return this.outerLoopBeginPointLabel;
		}

		public void setOuterLoopBeginPointLabel(String outerLoopBeginPointLabel) {
			this.outerLoopBeginPointLabel = outerLoopBeginPointLabel;
		}

		public String getOuterLoopEndPointLabel() {
			return this.outerLoopEndPointLabel;
		}

		public void setOuterLoopEndPointLabel(String outerLoopEndPointLabel) {
			this.outerLoopEndPointLabel = outerLoopEndPointLabel;
		}

		public String getOuterLoopUpdatePointLabel() {
			return this.outerLoopUpdatePointLabel;
		}

		public void setOuterLoopUpdatePointLabel(String outerLoopUpdatePointLabel) {
			this.outerLoopUpdatePointLabel = outerLoopUpdatePointLabel;
		}


		public String getLastFunctionLabel() {
			return this.lastFunctionLabel;
		}

		public void setLastFunctionLabel(String lastFunctionLabel) {
			this.lastFunctionLabel = lastFunctionLabel;
		}


		public void setNextBlockLoop(boolean isLoop) {
			this.isNextBlockLoop = isLoop;
		}

		public boolean isNextBlockLoop() {
			return this.isNextBlockLoop;
		}


		public AstNode[] getStatementNodes() {
			return this.statementNodes;
		}

		public void setStatementNodes(AstNode[] statementNodes) {
			this.statementNodes = statementNodes;
		}

		public int getStatementLength() {
			return this.statementLength;
		}

		public void setStatementLength(int statementLength) {
			this.statementLength = statementLength;
		}

		public int getStatementIndex() {
			return this.statementIndex;
		}

		public void setStatementIndex(int statementIndex) {
			this.statementIndex = statementIndex;
		}
		public String getLastStatementCode() {
			return this.lastStatementCode;
		}
		public void setLastStatementCode(String lastStatementCode) {
			this.lastStatementCode = lastStatementCode;
		}
	}


	/**
	 * <span class="lang-en">Generates intermediate code written in the VRIL from the semantic-analyzed AST</span>
	 * <span class="lang-ja">意味解析済みのASTから、VRILで記述された中間コードを生成して返します</span>
	 * .
	 * <span class="lang-en">
	 * Intermediate code generated by this method can be executed on the VM
	 * ({@link org.vcssl.nano.vm.VirtualMachine}) of the Vnano.
	 * </span>
	 * <span class="lang-ja">
	 * このメソッドによって生成された中間コードは、Vnano のVM
	 * ({@link org.vcssl.nano.vm.VirtualMachine}) によって実行できます.
	 * </span>
	 *
	 * @param inputAst
	 *   <span class="lang-en">The root node of the sematic-analyzed AST.</span>
	 *   <span class="lang-ja">意味解析済みのASTのルートノード.</span>
	 *
	 * @return
	 *   <span class="lang-en">Intermediate code written in the VRIL.</span>
	 *   <span class="lang-ja">VRILで記述された中間コード.</span>
	 */
	public String generate(AstNode inputAst) {

		StringBuilder codeBuilder = new StringBuilder();

		// 言語の識別用情報関連のディレクティブ（言語名やバージョンなど）を一括生成
		codeBuilder.append( this.generateLanguageInformationDirectives() );


		// 引数のASTに破壊的変更を加えないように複製
		AstNode cloneAst = inputAst.clone();

		// レジスタや識別子、即値、ラベルなどの値を、ASTノードに属性値として割りふる
		this.assignAssemblyValues(cloneAst);
		this.assignLabels(cloneAst);

		// 関数ディレクティブを一括生成
		codeBuilder.append( generateFunctionIdentifierDirectives(cloneAst) );

		// グローバル変数ディレクティブを一括生成
		codeBuilder.append( generateGlobalIdentifierDirectives(cloneAst) );

		// 全ての文を辿ってコード生成
		codeBuilder.append( this.trackAllStatements(cloneAst) );

		// 終了処理のコードを生成
		// （スクリプトエンジンのevalメソッドの評価値に対応するデータをメモリに設定して終了する）
		codeBuilder.append( generateFinalizationCode(cloneAst) );

		// ここまでの内容で、コードは動作上は完成しているので取得
		String code = codeBuilder.toString();

		// 可読性を上げるため、生成コードの再整形を行う（行の並べ替えなどの軽い範囲）
		String realignedCode = this.realign(code);
		return realignedCode;
	}


	/**
	 * AST(抽象構文木)内の各ノードに対して、
	 * 中間アセンブリコード内でそのノードに対応する値（レジスタ、即値、識別子など）を求め、
	 * それらを各ノードの {@link AttributeKey#ASSEMBLY_VALUE ASSEMBLY_VALUE} 属性値に設定します
	 * （従って、このメソッドは破壊的メソッドです）。
	 *
	 * 演算子ノードに対する、演算結果を格納するレジスタ（アキュームレータ）
	 * のアドレス割り当ても、このメソッド内で行われます。
	 *
	 * @param inputAst 解析対象のAST(抽象構文木)
	 */
	private void assignAssemblyValues(AstNode inputAst) {

		AstNode currentNode = inputAst.getPostorderDftFirstNode();
		while (currentNode != inputAst) {

			AstNode.Type nodeType = currentNode.getType();


			// 変数ノードやリーフ（リテラルや識別子などの末端）ノード: アセンブリ用識別子や即値に変換
			if (nodeType == AstNode.Type.VARIABLE || nodeType == AstNode.Type.LEAF) {

				boolean isVariable = nodeType == AstNode.Type.VARIABLE;
				boolean isLeaf = nodeType == AstNode.Type.LEAF;
				String leafType = currentNode.getAttribute(AttributeKey.LEAF_TYPE);

				// 関数識別子
				if(isLeaf && leafType == AttributeValue.FUNCTION_IDENTIFIER) {
					AstNode callOperatorNode = currentNode.getParentNode();
					String calleeSignature = callOperatorNode.getAttribute(AttributeKey.CALLEE_SIGNATURE);
					String assemblyValue = AssemblyWord.OPERAND_PREFIX_IDENTIFIER + calleeSignature;
					//String assemblyValue = IdentifierSyntax.getAssemblyIdentifierOfCalleeFunctionOf(callOperatorNode);
					currentNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, assemblyValue);

				// 変数または変数識別子
				} else if(isVariable || (isLeaf && leafType == AttributeValue.VARIABLE_IDENTIFIER) ) {
					String identifier = currentNode.getAttribute(AttributeKey.IDENTIFIER_VALUE);
					String assemblyValue = IdentifierSyntax.getAssemblyIdentifierOf(identifier);
					if (currentNode.hasAttribute(AttributeKey.IDENTIFIER_SERIAL_NUMBER)) {
						assemblyValue += AssemblyWord.IDENTIFIER_SERIAL_NUMBER_SEPARATOR
						              + currentNode.getAttribute(AttributeKey.IDENTIFIER_SERIAL_NUMBER);
					}
					currentNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, assemblyValue);

				// リテラル
				} else if(isLeaf && leafType == AttributeValue.LITERAL) {

					String dataTypeName = currentNode.getAttribute(AttributeKey.DATA_TYPE);
					String literal = currentNode.getAttribute(AttributeKey.LITERAL_VALUE);
					String assemblyValue = AssemblyWord.getImmediateValueOf(dataTypeName, literal);
					currentNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, assemblyValue);

				} else {
					throw new VnanoFatalException("Unknown leaf type: " + leafType);
				}
			}

			// 演算子ノード
			if (currentNode.getType() == AstNode.Type.OPERATOR) {

				String execType = currentNode.getAttribute(AttributeKey.OPERATOR_EXECUTOR);
				String syntaxType = currentNode.getAttribute(AttributeKey.OPERATOR_SYNTAX);
				String operatorSymbol = currentNode.getAttribute(AttributeKey.OPERATOR_SYMBOL);
				switch (execType) {
					case AttributeValue.ASSIGNMENT : {
						String value = currentNode.getChildNodes()[0].getAttribute(AttributeKey.ASSEMBLY_VALUE);
						currentNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, value);
						break;
					}
					case AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT : {
						String value = currentNode.getChildNodes()[0].getAttribute(AttributeKey.ASSEMBLY_VALUE);
						currentNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, value);
						break;
					}
					case AttributeValue.ARITHMETIC : {
						if (syntaxType.equals(AttributeValue.PREFIX)) {
							if (operatorSymbol.equals(ScriptWord.INCREMENT) || operatorSymbol.equals(ScriptWord.DECREMENT)) {
								String value = currentNode.getChildNodes()[0].getAttribute(AttributeKey.ASSEMBLY_VALUE);
								currentNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, value);
								break;
							}
						}
					}
					default : {
						String register = AssemblyWord.OPERAND_PREFIX_REGISTER + Integer.toString(registerCounter);
						currentNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, register);
						this.registerCounter++;
						break;
					}
				}
			}

			// 式ノード: 直下にあるルート演算子の結果 = 式の結果
			if (currentNode.getType() == AstNode.Type.EXPRESSION) {
				String value = currentNode.getChildNodes()[0].getAttribute(AttributeKey.ASSEMBLY_VALUE);
				currentNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, value);
			}

			currentNode = currentNode.getPostorderDftNextNode();
		}
	}



	/**
	 * AST(抽象構文木)内の各ノードに対して、
	 * 中間アセンブリコード内で使用するラベルを割りふり、
	 * それらを各ノードのラベル関連の属性値に設定します
	 * （従って、このメソッドは破壊的メソッドです）。
	 *
	 * @param inputAst 解析対象のAST(抽象構文木)
	 */
	private void assignLabels(AstNode inputAst) {

		AstNode currentNode = inputAst.getPostorderDftFirstNode();
		while (currentNode != inputAst) {

			if (currentNode.getType() == AstNode.Type.IF) {
				currentNode.setAttribute(AttributeKey.END_LABEL, this.generateLabelOperandCode());
			}
			if (currentNode.getType() == AstNode.Type.ELSE) {
				currentNode.setAttribute(AttributeKey.END_LABEL, this.generateLabelOperandCode());
			}
			if (currentNode.getType() == AstNode.Type.FOR) {
				currentNode.setAttribute(AttributeKey.BEGIN_LABEL, this.generateLabelOperandCode());
				currentNode.setAttribute(AttributeKey.UPDATE_LABEL, this.generateLabelOperandCode());
				currentNode.setAttribute(AttributeKey.END_LABEL, this.generateLabelOperandCode());
			}
			if (currentNode.getType() == AstNode.Type.WHILE) {
				currentNode.setAttribute(AttributeKey.BEGIN_LABEL, this.generateLabelOperandCode());
				currentNode.setAttribute(AttributeKey.END_LABEL, this.generateLabelOperandCode());
			}
			if (currentNode.getType() == AstNode.Type.FUNCTION) {
				currentNode.setAttribute(AttributeKey.END_LABEL, this.generateLabelOperandCode());
			}

			// 演算子ノード
			if (currentNode.getType() == AstNode.Type.OPERATOR) {
				String symbol = currentNode.getAttribute(AttributeKey.OPERATOR_SYMBOL);
				if (symbol.equals(ScriptWord.AND) || symbol.equals(ScriptWord.OR)) {
					// 短絡評価で第二オペランドの演算をスキップする場合のラベル
					currentNode.setAttribute(AttributeKey.END_LABEL, this.generateLabelOperandCode());
				}
			}

			currentNode = currentNode.getPostorderDftNextNode();
		}
	}


	/**
	 * AST(抽象構文)内の全ての文ノードをトップダウン順で辿りながら、
	 * 各文に対するコード生成メソッドを実行し、
	 * それらを逐次実行用の順序で結合したコードを返します。
	 *
	 * @param inputAst AST(抽象構文木)全体のルートノード
	 * @return 生成コード
	 */
	private String trackAllStatements(AstNode inputAst) {

		StringBuilder codeBuilder = new StringBuilder();

		AstNode[] statementNodes = inputAst.getChildNodes();
		int statementLength = statementNodes.length;
		int statementIndex = 0;

		StatementTrackingContext context = new StatementTrackingContext();
		Deque<StatementTrackingContext> contextStack = new ArrayDeque<StatementTrackingContext>();

		while (statementIndex < statementLength) {

			AstNode currentNode = statementNodes[ statementIndex ];
			AstNode.Type nodeType = currentNode.getType();

			// ブロック文以外ではメタディレクティブを生成
			if (!nodeType.equals(AstNode.Type.BLOCK)) {
				String metaDirective = this.generateMetaDirectiveCode(currentNode);
				codeBuilder.append(metaDirective);
			}

			// 文の種類に応じてコードを生成する
			switch (nodeType) {

				// ブロック文に突入する場合
				case BLOCK : {

					// 現在のコンテキスト（状態変数の集合）をスタックに退避して新規生成
					context.setStatementIndex(statementIndex);
					context.setStatementLength(statementLength);
					context.setStatementNodes(statementNodes);
					contextStack.push(context);
					context = new StatementTrackingContext();

					// 外のブロックから引き継ぐ必要がある情報をコピー
					// （これらの状態変数は無駄が多い気がするのでそのうちリファクタリングしたい）
					context.setLastLoopBeginPointLabel(contextStack.peek().getLastLoopBeginPointLabel());
					context.setLastLoopUpdatePointLabel(contextStack.peek().getLastLoopUpdatePointLabel());
					context.setLastLoopEndPointLabel(contextStack.peek().getLastLoopEndPointLabel());
					context.setOuterLoopBeginPointLabel(contextStack.peek().getOuterLoopBeginPointLabel());
					context.setOuterLoopUpdatePointLabel(contextStack.peek().getOuterLoopUpdatePointLabel());
					context.setOuterLoopEndPointLabel(contextStack.peek().getOuterLoopEndPointLabel());
					context.setLastFunctionLabel(contextStack.peek().getLastFunctionLabel());

					// 突入するブロックがループの場合は、最後に踏んだループを、1階層外のループとして登録
					if (contextStack.peek().isNextBlockLoop()) {
						context.setOuterLoopBeginPointLabel(contextStack.peek().getLastLoopBeginPointLabel());
						context.setOuterLoopUpdatePointLabel(contextStack.peek().getLastLoopUpdatePointLabel());
						context.setOuterLoopEndPointLabel(contextStack.peek().getLastLoopEndPointLabel());
					}

					// ブロック内の文を、読み込み対象として展開する
					statementNodes = currentNode.getChildNodes();
					statementLength = statementNodes.length;
					statementIndex = 0;
					break;
				}

				// ブロック文以外の文の場合
				case VARIABLE :
				case FUNCTION :
				case IF :
				case ELSE :
				case WHILE :
				case FOR :
				case BREAK :
				case CONTINUE :
				case RETURN :
				case EXPRESSION : {
					context = this.generateStatementCode(currentNode, context);
					codeBuilder.append( context.getLastStatementCode() );
					statementIndex++;
					break;
				}

				// 文ではない場合: 式中の演算子やリーフノードなので、上の式文のケース内で処理されるため無視
				default : {
					statementIndex++;
					break;
				}
			}

			// ブロック終端まで処理終了 ... ブロック階層を降りる処理を行う
			//（if文ではなくwhile文なのは、降りた地点がさらにブロック終端の場合もあるため）
			while (statementIndex == statementLength) {

				// コンテキスト（状態変数の集合）のスタックが空なら、ルートブロックまでコード生成できているので、処理を終了
				if (contextStack.isEmpty()) {
					break;
				}

				// コンテキスト（状態変数の集合）をスタックから復元し、ブロックに入る前の状態に戻る
				context = contextStack.pop();
				statementIndex = context.getStatementIndex();
				statementLength = context.getStatementLength();
				statementNodes = context.getStatementNodes();
				statementIndex++;

				// for文の更新式と更新式位置ラベルを置く必要があれば置く（先頭に戻るラベルより先に）
				if (context.hasUpdatePointLabel()) {
					codeBuilder.append(this.generateLabelDirectiveCode(context.getUpdatePointLabel()));
					context.clearUpdatePointLabel();
				}
				if (context.hasUpdatePointStatement()) {
					codeBuilder.append(context.getUpdatePointStatement());
					context.clearUpdatePointStatement();
				}

				// 以下、ループの先頭に戻るコードなど、ブロック末尾に置きたいコードがあれば置く

				// ループの先頭に戻るJMP命令など
				if (context.hasBeginPointLabel()) {
					String jumpCode = this.generateInstruction(
							OperationCode.JMP.name(), DataTypeName.BOOL, PLACE_HOLDER, context.getBeginPointLabel(), IMMEDIATE_TRUE
					);
					codeBuilder.append(jumpCode);
					context.clearBeginPointLabel();
				}

				// 関数のデフォルトの return 文など
				if (context.hasEndPointStatement()) {
					codeBuilder.append(context.getEndPointStatement());
					context.clearEndPointStatement();
				}

				// ループ脱出ラベルや、関数終端ラベル（命令列の逐次実行で上から関数内に突入しないように避けているJMP命令の着地点）など
				if (context.hasEndPointLabel()) {
					String[] endPointLabels = context.getEndPointLabels();
					for (String endPointLabel: endPointLabels) {
						codeBuilder.append(this.generateLabelDirectiveCode(endPointLabel));
					}
					context.clearEndPointLabel();
				}
			}
		}

		return codeBuilder.toString();
	}


	/**
	 * 単文の処理を実行するコードを生成し、その内容を含む、更新されたコンテキストを生成して返します。
	 *
	 * ここでのコンテキストとは、文のコード生成に必要な、
	 * それまでのコード生成過程に依存する情報
	 * （例えば、対象とする文よりも前にある制御文のジャンプ先ラベルや、条件式の結果など）
	 * の事を指します。
	 *
	 * 文のコード生成処理においては、単に生成されたコードを返すだけではなく、
	 * 後の文のコード生成のために、コンテキストを更新する事が必要となります。
	 * 従って、このメソッドは、引数に受け取ったコンテキストに基づいて文のコード生成を行った上で、
	 * それを更新した新しいコンテキストを返します。
	 * その中に、文の生成コードも含まれており、
	 * {@link StatementTrackingContext#getLastStatementCode StatementTrackingContext.getLastStatementCode}
	 * メソッドで取り出す事ができます。
	 *
	 * @param node 単文のASTノード
	 * @param context 直前の文のコード生成が完了した時点のコンテキスト
	 * @return 生成コードを含む、更新されたコンテキスト
	 */
	private StatementTrackingContext generateStatementCode(AstNode node, StatementTrackingContext context) {
		context = context.clone();

		AstNode.Type nodeType = node.getType();

		// 文の種類に応じてコードを生成する
		String code = null;
		switch (nodeType) {

			// 変数宣言文
			case VARIABLE : {
				code = this.generateVariableDeclarationStatementCode(node);
				break;
			}

			// 関数宣言文
			case FUNCTION : {
				// 関数先頭のラベルを生成 ... 先頭はラベルである事を示すプレフィックス、その後に識別子プレフィックス + 関数シグネチャ
				String functionLabelName
						= Character.toString(AssemblyWord.OPERAND_PREFIX_LABEL)
						+ Character.toString(AssemblyWord.OPERAND_PREFIX_IDENTIFIER)
						+ IdentifierSyntax.getSignatureOf(node);
				code = this.generateFunctionDeclarationStatementCode(node, functionLabelName);
				context.setLastFunctionLabel(functionLabelName);
				context.addEndPointLabel(node.getAttribute(AttributeKey.END_LABEL));
				context.setEndPointStatement(
						this.generateInstruction(OperationCode.RET.name(), DataTypeName.VOID, PLACE_HOLDER, functionLabelName)
				);
				context.setNextBlockLoop(false);
				break;
			}

			// if 文
			case IF : {
				context.addEndPointLabel(node.getAttribute(AttributeKey.END_LABEL));
				String lastIfConditionRegister = null;
				boolean lastIfConditionRegisterAllocRequired = false;
				if( context.hasLastIfConditionRegister() ) {
					lastIfConditionRegister = context.getLastIfConditionRegister();
				} else {
					lastIfConditionRegister = this.generateRegisterOperandCode();
					lastIfConditionRegisterAllocRequired = true;
				}
				context.setLastIfConditionValue(lastIfConditionRegister);
				context.setNextBlockLoop(false);
				code = this.generateIfStatementCode(node, lastIfConditionRegister, lastIfConditionRegisterAllocRequired);
				break;
			}

			// else 文
			case ELSE : {
				code = this.generateElseStatementCode(node, context.getLastIfConditionRegister());
				context.addEndPointLabel(node.getAttribute(AttributeKey.END_LABEL));
				context.setNextBlockLoop(false);
				//context.clearLastIfConditionValue(); // else if 対応後はさらに else 文が続く場合があるので消してはいけない
				break;
			}

			// while 文
			case WHILE : {
				code = this.generateWhileStatementCode(node);
				context.setBeginPointLabel(node.getAttribute(AttributeKey.BEGIN_LABEL));
				context.addEndPointLabel(node.getAttribute(AttributeKey.END_LABEL));
				context.setLastLoopBeginPointLabel( context.getBeginPointLabel() );
				context.setLastLoopUpdatePointLabel( context.getUpdatePointLabel() );
				context.setLastLoopEndPointLabel( context.getEndPointLabels()[0] ); // while文の場合は endPointLabels は1要素のはず
				context.setNextBlockLoop(true);
				break;
			}

			// for 文
			case FOR : {
				code = this.generateForStatementCode(node);
				context.setBeginPointLabel(node.getAttribute(AttributeKey.BEGIN_LABEL));
				context.setUpdatePointLabel(node.getAttribute(AttributeKey.UPDATE_LABEL));
				context.addEndPointLabel(node.getAttribute(AttributeKey.END_LABEL));

				// 更新文（for文の丸括弧内での3つ目の式）
				if (node.getChildNodes()[2].getType() == AstNode.Type.EXPRESSION) { // 式文の場合は評価コード生成
					context.setUpdatePointStatement(this.generateExpressionCode(node.getChildNodes()[2]));
				} // 他に空文の場合もあり得るが、その場合は何もしない

				context.setLastLoopBeginPointLabel( context.getBeginPointLabel() );
				context.setLastLoopUpdatePointLabel( context.getUpdatePointLabel() );
				context.setLastLoopEndPointLabel( context.getEndPointLabels()[0] ); // for文の場合は endPointLabels は1要素のはず
				context.setNextBlockLoop(true);
				break;
			}

			// break 文: 1階層外のループ末端へのジャンプ命令そのもの
			case BREAK : {
				code = this.generateInstruction(
					OperationCode.JMP.name(), DataTypeName.BOOL, PLACE_HOLDER, context.getOuterLoopEndPointLabel(), IMMEDIATE_TRUE
				);
				break;
			}

			// continue 文: 1階層外のループ先頭か更新式の位置へのジャンプ命令そのもの
			case CONTINUE : {
				String continueJumpPointLabel = context.getOuterLoopBeginPointLabel(); // ループ先頭に飛ぶラベル
				if (context.hasLastLoopUpdatePointLabel()) {
					continueJumpPointLabel = context.getOuterLoopUpdatePointLabel(); // for文は更新式の位置に飛ぶ
				}
				code = this.generateInstruction(
					OperationCode.JMP.name(), DataTypeName.BOOL, PLACE_HOLDER, continueJumpPointLabel, IMMEDIATE_TRUE
				);
				break;
			}

			// return 文
			case RETURN : {
				code = this.generateReturnStatementCode(node, context.getLastFunctionLabel());
				break;
			}

			// 式文
			case EXPRESSION : {
				code = this.generateExpressionCode(node);
				break;
			}

			// 文以外のノードは式中の演算子やリーフノードなので、上の式文のケース内で処理される
			default : {
				break;
			}
		}
		context.setLastStatementCode(code);

		return context;
	}


	/**
	 * 変数宣言文の処理を実行するコードを生成して返します。
	 *
	 * @param node 変数宣言文のASTノード（{@link AstNode.Type#VARIABLE VARIABLE}タイプ）
	 * @return 生成コード
	 */
	private String generateVariableDeclarationStatementCode (AstNode node) {

		StringBuilder codeBuilder = new StringBuilder();
		//String variableName= node.getAttribute(AttributeKey.IDENTIFIER_VALUE);
		//String variableOperand = AssemblyWord.OPERAND_PREFIX_IDENTIFIER+variableName;
		String variableOperand = node.getAttribute(AttributeKey.ASSEMBLY_VALUE);

		// 識別子ディレクティブを生成
		codeBuilder.append(AssemblyWord.LOCAL_VARIABLE_DIRECTIVE);
		codeBuilder.append(AssemblyWord.WORD_SEPARATOR);
		codeBuilder.append(variableOperand);
		codeBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);
		codeBuilder.append(AssemblyWord.LINE_SEPARATOR);

		// 先に子ノードを処理し、型情報や配列要素数などを取得
		String variableType = node.getDataTypeName();

		int rank = RANK_OF_SCALAR;
		String[] arrayLengthValues = null;

		if (node.hasChildNodes(AstNode.Type.LENGTHS)) {
			AstNode[] lengthExprNodes
				= node.getChildNodes(AstNode.Type.LENGTHS)[0].getChildNodes(AstNode.Type.EXPRESSION);

			rank = lengthExprNodes.length;
			arrayLengthValues = new String[rank];
			for (int dim=0; dim<rank; dim++) {
				arrayLengthValues[dim] = lengthExprNodes[dim].getAttribute(AttributeKey.ASSEMBLY_VALUE);
				codeBuilder.append( this.generateExpressionCode(lengthExprNodes[dim]) );
			}
		}

		// 以下、メモリ確保のためのALLOC命令を生成

		// スカラなら1オペランドのALLOC命令を生成
		if (rank == RANK_OF_SCALAR) {

			codeBuilder.append(
				this.generateInstruction(OperationCode.ALLOC.name(), variableType, variableOperand)
			);

		// 配列確保なら要素数オペランドを付けたALLOC命令を生成
		} else {

			String[] allocOperands = new String[ arrayLengthValues.length + 1 ];
			allocOperands[0] = variableOperand;
			System.arraycopy(arrayLengthValues, 0, allocOperands, 1, arrayLengthValues.length);
			codeBuilder.append(
				this.generateInstruction(OperationCode.ALLOC.name(), variableType, allocOperands)
			);
		}

		// 初期化式があればコード生成
		AstNode[] initExprNodes = node.getChildNodes(AstNode.Type.EXPRESSION);
		if (initExprNodes.length == 1) {
			codeBuilder.append( this.generateExpressionCode(initExprNodes[0]) );
		}

		return codeBuilder.toString();
	}


	/**
	 * 関数宣言文の処理を実行するコードを生成して返します。
	 *
	 * @param node 関数宣言文のASTノード（{@link AstNode.Type#FUNCTION FUNCTION}タイプ）
	 * @param functionLabelName 関数の先頭に配置するラベル名
	 * @return 生成コード
	 */
	private String generateFunctionDeclarationStatementCode (AstNode node, String functionLabelName) {

		StringBuilder codeBuilder = new StringBuilder();

		// 関数の外側のコードを上から逐次実行されている時に、関数内のコードを実行せず読み飛ばすためのJMP命令を生成
		String skipLabel = node.getAttribute(AttributeKey.END_LABEL);
		codeBuilder.append(
			this.generateInstruction(OperationCode.JMP.name(), DataTypeName.BOOL, PLACE_HOLDER, skipLabel, IMMEDIATE_TRUE)
		);

		// 関数先頭ラベルを配置
		codeBuilder.append( this.generateLabelDirectiveCode(functionLabelName) );

		// 子ノードは引数のノードなので、それらに対してスタック上のデータを取りだして格納するコードを生成
		// 注意；引数は宣言順にスタックに積まれてて、取り出す際は逆順で出てくるので、逆順で受け取るコードを生成する必要がある
		AstNode[] argNodes = node.getChildNodes();
		int argLength = argNodes.length;
		for (int argIndex=argLength-1; 0 <= argIndex; argIndex--) {

			AstNode argNode = argNodes[argIndex];

			String argIdentifier = argNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);
			String argDataType = argNode.getAttribute(AttributeKey.DATA_TYPE);
			int argRank = argNode.getRank();

			// 引数のローカル変数ディレクティブを生成
			codeBuilder.append(AssemblyWord.LOCAL_VARIABLE_DIRECTIVE);
			codeBuilder.append(AssemblyWord.WORD_SEPARATOR);
			codeBuilder.append(argIdentifier);
			codeBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);
			codeBuilder.append(AssemblyWord.LINE_SEPARATOR);

			// スカラの場合は固定サイズなので、普通にALLOC命令を生成
			if (argRank == RANK_OF_SCALAR) {
				codeBuilder.append(
					this.generateInstruction(OperationCode.ALLOC.name(), argDataType, argIdentifier)
				);

			// 配列の場合は、スタック上のデータが収まるサイズでメモリーを確保するために、ALLOCP命令を生成
			} else {
				codeBuilder.append(
					this.generateInstruction(OperationCode.ALLOCP.name(), argDataType, argIdentifier)
				);
			}

			// スタックから引数に値を取り出すMOVPOP命令のコードを生成
			codeBuilder.append(
				this.generateInstruction(OperationCode.MOVPOP.name(), argDataType, argIdentifier)
			);
		}

		return codeBuilder.toString();
	}


	/**
	 * return文の処理を実行するコードを生成して返します。
	 *
	 * @param node return文のASTノード（{@link AstNode.Type#RETURN RETURN}タイプ）
	 * @param functionLabelName このreturn文が属する関数のラベル名
	 * @return 生成コード
	 */
	private String generateReturnStatementCode(AstNode node, String functionLabelName) {

		StringBuilder codeBuilder = new StringBuilder();

		AstNode[] childNodes = node.getChildNodes();

		// 戻り値が無い場合
		if (childNodes.length == 0) {
			codeBuilder.append(
				this.generateInstruction(OperationCode.RET.name(), DataTypeName.VOID, PLACE_HOLDER, functionLabelName)
			);

		// 戻り値がある場合 ... 戻り値の式を解釈し、その結果をオペランドに追加したRET命令を生成
		} else {
			AstNode exprNode = childNodes[0];
			String exprValue = exprNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);
			String exprCode = this.generateExpressionCode(exprNode);
			codeBuilder.append(exprCode);
			codeBuilder.append(
				this.generateInstruction(OperationCode.RET.name(), DataTypeName.VOID, PLACE_HOLDER, functionLabelName, exprValue)
			);
		}

		return codeBuilder.toString();
	}


	/**
	 * if文の処理を実行するコードを生成して返します。
	 *
	 * 中間アセンブリコード内におけるif文の処理は、
	 * 条件成立時に実行される範囲の終端にラベルを配置し、
	 * 条件不成立時はそこへジャンプ系命令で移動する
	 * （つまり、if文の実行対象範囲のコードを実行せずに飛ばす）
	 * 事によって実現されます。
	 * この終端ラベルは、呼び出し側で用意した上で、このメソッドの引数に渡す必要があります。
	 *
	 * なお、このメソッドは、指定された終端ラベルをジャンプ系命令の移動先に使用しますが、
	 * 終端ラベルの配置は行いません。
	 * 従って、if文の実行対象範囲のコード生成が終わった後に、
	 * 終端ラベルを配置する必要があります。
	 *
	 * @param node if文のASTノード（{@link AstNode.Type#IF IF}タイプ）
	 * @param lastIfConditionRegister 文を実行していく過程で、最後（直前）のif文の条件式結果を控えておくレジスタ
	 * @param lastIfConditionRegisterAllocRequired 条件式結果を控えておくレジスタのメモリ確保(ALLOC)処理が必要かどうか
	 * @return 生成コード
	 */
	private String generateIfStatementCode(AstNode node,
			String lastIfConditionRegister, boolean lastIfConditionRegisteAllocRequired) {

		StringBuilder codeBuilder = new StringBuilder();

		// 条件式の評価コードを生成
		AstNode conditionExprNode = node.getChildNodes(AstNode.Type.EXPRESSION)[0]; // 後で検査が必要
		codeBuilder.append( this.generateExpressionCode(conditionExprNode) );

		if (conditionExprNode.getRank() != RANK_OF_SCALAR) {
			// 条件式が配列の場合は弾くべき
			return null;
		}

		// 条件式の結果を lastIfConditionRegister に控える (else文のコード生成で必要)
		if (lastIfConditionRegisteAllocRequired) {
			codeBuilder.append( this.generateInstruction(OperationCode.ALLOC.name(), DataTypeName.BOOL, lastIfConditionRegister));
		}
		String lastIfConditionMovCode = this.generateInstruction(
				OperationCode.MOV.name(), DataTypeName.BOOL,
				lastIfConditionRegister, conditionExprNode.getAttribute(AttributeKey.ASSEMBLY_VALUE)
		);
		codeBuilder.append(lastIfConditionMovCode);

		// 条件不成立の時に終端ラベルに飛ぶコードを生成
		String endLabel = node.getAttribute(AttributeKey.END_LABEL);
		String conditionExprValue = conditionExprNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);
		codeBuilder.append(
			this.generateInstruction(OperationCode.JMPN.name(), DataTypeName.BOOL, PLACE_HOLDER, endLabel, conditionExprValue)
		);

		return codeBuilder.toString();
	}


	/**
	 * else文の処理を実行するコードを生成して返します。
	 *
	 * 中間アセンブリコード内におけるelse文の処理は、
	 * else文の実行対象範囲の終端にラベルを配置し、
	 * 直前のif文の条件が成立していた場合のみ、
	 * 終端ラベルへジャンプ系命令で移動する
	 * （つまり、else文の実行対象範囲のコードを実行せずに飛ばす）
	 * 事によって実現されます。
	 * この終端ラベルは、呼び出し側で用意した上で、このメソッドの引数に渡す必要があります。
	 *
	 * なお、このメソッドは、指定された終端ラベルをジャンプ系命令の移動先に使用しますが、
	 * 終端ラベルの配置は行いません。
	 * 従って、else文の実行対象範囲のコード生成が終わった後に、
	 * 終端ラベルを配置する必要があります。
	 *
	 * @param lastIfConditionRegister 直前のif文における条件式の値（レジスタや変数の識別子、または即値）
	 * @return 生成コード
	 */
	private String generateElseStatementCode(AstNode node, String lastIfConditionValue) {
		StringBuilder codeBuilder = new StringBuilder();

		String endLabel = node.getAttribute(AttributeKey.END_LABEL);

		// 条件成立の時に末端ラベルへ飛ぶコードを生成
		//（ else は直前の if 文が不成立だった場合に実行するので、成立していた場合は逆にelse末尾まで飛ぶ ）
		codeBuilder.append(
			this.generateInstruction(OperationCode.JMP.name(), DataTypeName.BOOL, PLACE_HOLDER, endLabel, lastIfConditionValue)
		);

		return codeBuilder.toString();
	}


	/**
	 * while文の処理を実行するコードを生成して返します。
	 *
	 * 中間アセンブリコード内におけるwhile文やfor文などのループ処理は、
	 * ループ先頭と終端の2箇所にラベルを配置し、
	 * ループ継続時では前者のラベル、ループ終了時では後者のラベルへと、
	 * ジャンプ系命令で移動する事によって実現されます。
	 * そのため、それら2つのラベルを呼び出し側で用意した上で、
	 * このメソッドの引数に渡す必要があります。
	 *
	 * なお、このメソッドはループ先頭ラベルの配置は行いますが、
	 * ループ終端ラベルは配置せず、ジャンプ系命令の移動先に使用するだけです。
	 * 従って、ループ対象範囲のコード生成が終わった後に、ループ終端ラベルを配置する必要があります。
	 *
	 * @param node while文のASTノード（{@link AstNode.Type#WHILE WHILE}タイプ）
	 * @return 生成コード
	 */
	private String generateWhileStatementCode(AstNode node) {
		StringBuilder codeBuilder = new StringBuilder();

		String beginLabel = node.getAttribute(AttributeKey.BEGIN_LABEL);
		String endLabel = node.getAttribute(AttributeKey.END_LABEL);

		// ループで戻って来るラベルを配置
		codeBuilder.append(this.generateLabelDirectiveCode(beginLabel));

		// 条件式の評価コードを生成
		AstNode conditionExprNode = node.getChildNodes(AstNode.Type.EXPRESSION)[0]; // 後で検査が必要
		codeBuilder.append( this.generateExpressionCode(conditionExprNode) );

		if (conditionExprNode.getRank() != RANK_OF_SCALAR) {
			// 条件式が配列の場合は意味解析の段階で弾くべき
			return null;
		}

		// 条件不成立時はループ外に脱出するコードを生成
		String conditionExprValue = conditionExprNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);
		codeBuilder.append(
			this.generateInstruction(OperationCode.JMPN.name(), DataTypeName.BOOL, PLACE_HOLDER, endLabel, conditionExprValue)
		);

		return codeBuilder.toString();
	}


	/**
	 * for文の処理を実行するコードを生成して返します。
	 *
	 * 中間アセンブリコード内におけるwhile文やfor文などのループ処理は、
	 * ループ先頭と終端の2箇所にラベルを配置し、
	 * ループ継続時では前者のラベル、ループ終了時では後者のラベルへと、
	 * ジャンプ系命令で移動する事によって実現されます。
	 * そのため、それら2つのラベルを呼び出し側で用意した上で、
	 * このメソッドの引数に渡す必要があります。
	 *
	 * なお、このメソッドはループ先頭ラベルの配置は行いますが、
	 * ループ終端ラベルは配置せず、ジャンプ系命令の移動先に使用するだけです。
	 * 従って、ループ対象範囲のコード生成が終わった後に、ループ終端ラベルを配置する必要があります。
	 *
	 * また、for文の括弧内にセミコロンで区切られた、3番目の式（通常はカウンタの更新式）のコードは、
	 * このメソッドでは生成されません。
	 * それについては、ループ対象範囲のコード生成が終わった後に、別途
	 * {@link CodeGenerator#generateExpressionCode generateExpressionCode} メソッド
	 * で生成して配置する必要があります。
	 *
	 * @param node for文のASTノード（{@link AstNode.Type#FOR FOR}タイプ）
	 * @return 生成コード
	 */
	private String generateForStatementCode(AstNode node) {
		StringBuilder codeBuilder = new StringBuilder();

		String beginLabel = node.getAttribute(AttributeKey.BEGIN_LABEL);
		String endLabel = node.getAttribute(AttributeKey.END_LABEL);

		// 初期化文、条件文、更新文の評価コードを生成
		AstNode[] childNodes =  node.getChildNodes();

		String initStatementCode = null;  // 初期化文の生成コードを控える
		String conditionStatementCode = null; // 条件分の生成コードを控える
		String conditionValue = null; // 条件文の結果の値を格納するレジスタ、または即値を控える


		// 初期化文のコード生成 ... 変数宣言文の場合は宣言処理（初期化処理含む）のコードを生成
		if (childNodes[0].getType() == AstNode.Type.VARIABLE) {
			initStatementCode = this.generateVariableDeclarationStatementCode(childNodes[0]);
		// そうでなければ式文なので式の評価コードを生成
		} else if (childNodes[0].getType() == AstNode.Type.EXPRESSION) {
			initStatementCode = this.generateExpressionCode(childNodes[0]);
		// 空文の場合は何もしない
		} else if (childNodes[0].getType() == AstNode.Type.EMPTY) {
			initStatementCode = "";
		}

		// 条件分のコード生成 ... 式文の場合は式の評価コードを生成
		if (childNodes[1].getType() == AstNode.Type.EXPRESSION) {
			conditionStatementCode = this.generateExpressionCode(childNodes[1]);
			conditionValue = childNodes[1].getAttribute(AttributeKey.ASSEMBLY_VALUE);
		// 空文の場合は常に true と見なす
		} else if (childNodes[1].getType() == AstNode.Type.EMPTY) {
			conditionStatementCode = "";
			conditionValue = IMMEDIATE_TRUE;
		}


		// 初期化文のコードを出力
		codeBuilder.append(initStatementCode);

		// その後に、ループで戻って来る地点のラベルを配置
		codeBuilder.append(this.generateLabelDirectiveCode(beginLabel));


		// 条件文の評価コードを出力
		codeBuilder.append(conditionStatementCode);

		// 条件不成立時はループ外に脱出するコードを生成
		codeBuilder.append(
			this.generateInstruction(OperationCode.JMPN.name(), DataTypeName.BOOL, PLACE_HOLDER, endLabel, conditionValue)
		);

		return codeBuilder.toString();
	}





	/**
	 * 式のAST(抽象構文木)内をボトムアップ順で辿り、その式内の全ての演算を逐次実行するコードを生成して返します。
	 *
	 * @param exprRootNode 式のASTのルートノード({@link AstNode.Type#EXPRESSION EXPRESSION}タイプ、または式の構成要素になり得るタイプ)
	 * @return 生成コード
	 */
	private String generateExpressionCode(AstNode exprRootNode) {

		StringBuilder codeBuilder = new StringBuilder();

		AstNode currentNode = exprRootNode.getPostorderDftFirstNode();
		while(currentNode != exprRootNode) {

			// 演算子ノードに対する演算コード生成処理
			if (currentNode.getType() == AstNode.Type.OPERATOR) {
				String operatorSyntax = currentNode.getAttribute(AttributeKey.OPERATOR_SYNTAX);
				String operatorExecution = currentNode.getAttribute(AttributeKey.OPERATOR_EXECUTOR);

				switch (operatorExecution) {
					case AttributeValue.CALL : { // 関数呼び出し演算子
						codeBuilder.append( this.generateCallOperatorCode(currentNode) );
						break;
					}
					case AttributeValue.ASSIGNMENT : { // 代入演算子
						codeBuilder.append( this.generateAsignmentOperatorCode(currentNode) );
						break;
					}
					case AttributeValue.ARITHMETIC : { // 算術演算子
						switch (operatorSyntax) {
							case AttributeValue.BINARY : { // 二項
								codeBuilder.append( this.generateArithmeticBinaryOperatorCode(currentNode) );
								break;
							}
							case AttributeValue.PREFIX : { // 前置
								codeBuilder.append( this.generateArithmeticPrefixOperatorCode(currentNode) );
								break;
							}
							case AttributeValue.POSTFIX : { // 後置
								codeBuilder.append( this.generateArithmeticPostfixOperatorCode(currentNode) );
								break;
							}
							default : {
								throw new VnanoFatalException("Invalid operator syntax for arithmetic operators: " + operatorSyntax);
							}
						}
						break;
					}
					case AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT : { // 複合代入演算子
						codeBuilder.append( this.generateArithmeticCompoundAssignmentOperatorCode(currentNode) );
						break;
					}
					case AttributeValue.COMPARISON : { // 比較演算子
						codeBuilder.append( this.generateComparisonBinaryOperatorCode(currentNode) );
						break;
					}
					case AttributeValue.LOGICAL : { // 論理演算子
						switch (operatorSyntax) {
							case AttributeValue.BINARY : { // 二項
								codeBuilder.append( this.generateLogicalBinaryOperatorCode(currentNode) );
								break;
							}
							case AttributeValue.PREFIX : { // 前置
								codeBuilder.append( this.generateLogicalPrefixOperatorCode(currentNode) );
								break;
							}
							default : {
								throw new VnanoFatalException("Invalid operator syntax for logical operators: " + operatorSyntax);
							}
						}
						break;
					}
					case AttributeValue.INDEX : { // 配列インデックス演算子
						codeBuilder.append( this.generateIndexOperatorCode(currentNode) );
						break;
					}
					case AttributeValue.CAST : { // キャスト演算子
						codeBuilder.append( this.generateCastOperatorCode(currentNode) );
						break;
					}
					default : {
						throw new VnanoFatalException("Unknown operator execution type: " + operatorExecution);
					}
				}
			} // 演算子ノードに対する演算コード生成処理


			// 部分式の評価後に、親ノードの演算子に応じて追加処理が必要な場合
			AstNode parentNode = currentNode.getParentNode();
			if (parentNode.getType() == AstNode.Type.OPERATOR) {
				String parentOperatorSymbol = currentNode.getParentNode().getAttribute(AttributeKey.OPERATOR_SYMBOL);

				//「&&」と「||」演算子: 短絡評価により、左オペランドの値によっては右オペランドの演算をスキップする必要がある
				if (parentOperatorSymbol.equals(ScriptWord.AND) || parentOperatorSymbol.equals(ScriptWord.OR)) {

					String jumpLabel = parentNode.getAttribute(AttributeKey.END_LABEL);
					String leftOperandValue = currentNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);

					//スキップ用のジャンプ命令: || なら左オペランドがtrueの時に省略するのでJMP命令、&& ならその逆で JMPN 命令
					String jumpOpcode = OperationCode.JMP.name();
					if (parentOperatorSymbol.equals(ScriptWord.AND)) {
						jumpOpcode = OperationCode.JMPN.name();
					}

					// 左オペランドの場合: スキップ用のジャンプ系命令を置く
					if (currentNode == parentNode.getChildNodes()[0]) {
						codeBuilder.append(
								this.generateInstruction(jumpOpcode, DataTypeName.BOOL, PLACE_HOLDER, jumpLabel, leftOperandValue)
						);

					// 右オペランド場合: スキップ地点のラベルを置く
					} else {
						codeBuilder.append(
								this.generateLabelDirectiveCode(parentNode.getAttribute(AttributeKey.END_LABEL))
						);
					}
				}
			}

			currentNode = currentNode.getPostorderDftNextNode();
		}

		return codeBuilder.toString();
	}



	/**
	 * 次元や要素数に応じて、最適なレジスタ確保コードを生成します。
	 *
	 * 本来は、スカラかベクトルかを問わず {@link OperationCode#ALLOCR ALLOCR} 命令でレジスタ確保を行う事が可能ですが、
	 * 確保対象がスカラである場合には、{@link OperationCode#ALLOCR ALLOCR} 命令の要素数参照オペランドは冗長になります。
	 * そのためこのメソッドでは、確保対象がスカラの場合には、
	 * 最も単純な1オペランドの {@link OperationCode#ALLOC ALLOC} 命令を使用します。
	 *
	 * @param dataType 確保するレジスタのデータ型名
	 * @param target 確保対象のレジスタ
	 * @param lengthsDeterminer レジスタがベクトルの場合の要素数参照オペランド（スカラの場合には使用されません）
	 * @param rank レジスタの次元数
	 * @return レジスタを確保するコード
	 */
	private String generateRegisterAllocationCode(String dataType, String target, String lengthsDeterminer, int rank) {
		if (rank == RANK_OF_SCALAR) {
			return this.generateInstruction(OperationCode.ALLOC.name(), dataType, target);
		} else {
			return this.generateInstruction(OperationCode.ALLOCR.name(), dataType, target, lengthsDeterminer);
		}
	}


	/**
	 * 算術二項演算子の演算を実行するコードを生成して返します。
	 *
	 * 具体的な演算子としては、中置記法の可算、減算、乗算、除算、剰余算が該当します。
	 *
	 * @param operatorNode 算術二項演算子のASTノード
	 * （{@link AstNode.Type#OPERATOR OPERATOR}タイプ、
	 *   {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTOR}属性値が{@link AttributeValue#ARITHMETIC ARITHMETIC}、
	 *   {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX}属性値が{@link AttributeValue#BINARY BINARY}）
	 * @return 生成コード
	 */
	private String generateArithmeticBinaryOperatorCode(AstNode operatorNode) {
		String operatorSymbol = operatorNode.getAttribute(AttributeKey.OPERATOR_SYMBOL);

		String opcode = null;
		switch (operatorSymbol) {
			case ScriptWord.PLUS :            opcode = OperationCode.ADD.name(); break;
			case ScriptWord.MINUS :           opcode = OperationCode.SUB.name(); break;
			case ScriptWord.MULTIPLICATION :  opcode = OperationCode.MUL.name(); break;
			case ScriptWord.DIVISION :        opcode = OperationCode.DIV.name(); break;
			case ScriptWord.REMAINDER :       opcode = OperationCode.REM.name(); break;
			default : {
				throw new VnanoFatalException("Invalid operator symbol for logical operators: " + operatorSymbol);
			}
		}

		return this.generateBinaryOperatorCode(operatorNode, opcode, operatorNode.getChildNodes());
	}


	/**
	 * 算術後置演算子の演算を実行するコードを生成して返します。
	 *
	 * 具体的な演算子としては、後置インクメントと後置デクリメントが該当します。
	 *
	 * @param operatorNode 算術後置演算子のASTノード
	 * （{@link AstNode.Type#OPERATOR OPERATOR}タイプ、
	 *   {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTOR}属性値が{@link AttributeValue#ARITHMETIC ARITHMETIC}、
	 *   {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX}属性値が{@link AttributeValue#POSTFIX POSTFIX}）
	 * @return 生成コード
	 */
	private String generateArithmeticPostfixOperatorCode(AstNode operatorNode) {
		StringBuilder codeBuilder = new StringBuilder();

		String operatorSymbol = operatorNode.getAttribute(AttributeKey.OPERATOR_SYMBOL);
		String executionDataType = operatorNode.getAttribute(AttributeKey.OPERATOR_EXECUTION_DATA_TYPE);

		// インクリメント/デクリメントの対象変数
		AstNode variableNode = operatorNode.getChildNodes()[0];
		String variableValue = variableNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);
		String resultValue = operatorNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);

		// レジスタを確保し、演算前の値をそこに控える
		String storageRegister = this.generateRegisterOperandCode();
		codeBuilder.append(
			this.generateRegisterAllocationCode(executionDataType, storageRegister, variableValue, operatorNode.getRank())
		);
		codeBuilder.append(
			this.generateInstruction(OperationCode.MOV.name(), executionDataType, storageRegister, variableValue)
		);


		String opcode = null;
		switch (operatorSymbol) {
			case ScriptWord.INCREMENT : {
				opcode = OperationCode.ADD.name();
				break;
			}
			case ScriptWord.DECREMENT : {
				opcode = OperationCode.SUB.name();
				break;
			}
		}

		// インクリメント/デクリメントの変化幅
		AstNode stepNode = new AstNode(AstNode.Type.LEAF, variableNode.getLineNumber(), variableNode.getFileName());

		stepNode.setAttribute(AttributeKey.DATA_TYPE, executionDataType);
		stepNode.setAttribute(AttributeKey.RANK, Integer.toString(RANK_OF_SCALAR));
		switch (executionDataType) {
			case DataTypeName.INT : {
				String immediateValue = this.generateImmediateOperandCode(executionDataType, "1");
				stepNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, immediateValue);
				break;
			}
			case DataTypeName.FLOAT : {
				String immediateValue = this.generateImmediateOperandCode(executionDataType, "1.0");
				stepNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, immediateValue);
				break;
			}
		}


		String binaryOperationCode = this.generateBinaryOperatorCode(operatorNode, opcode, variableNode, stepNode);
		codeBuilder.append(binaryOperationCode);

		String movCode;

		// 変数に演算結果レジスタの値をMOVする
		movCode = this.generateInstruction(OperationCode.MOV.name(), executionDataType, variableValue, resultValue);
		codeBuilder.append(movCode);

		// 演算結果レジスタに演算前の値をMOVする
		//（後置インクリメント/デクリメントは、式の中での参照値は演算前の値となるため）
		movCode = this.generateInstruction(OperationCode.MOV.name(), executionDataType, resultValue, storageRegister);
		codeBuilder.append(movCode);

		return codeBuilder.toString();
	}


	/**
	 * 算術前置演算子の演算を実行するコードを生成して返します。
	 *
	 * 具体的な演算子としては、前置インクメント、前置デクリメント、単項プラス、単項マイナスが該当します。
	 * ただし、単項マイナスのコード生成には、内部で
	 * {@link CodeGenerator#generateNegateOperatorCode generateNegateOperatorCode}
	 * メソッドがそのまま使用されます。
	 *
	 * @param operatorNode 算術前置演算子のASTノード
	 * （{@link AstNode.Type#OPERATOR OPERATOR}タイプ、
	 *   {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTOR}属性値が{@link AttributeValue#ARITHMETIC ARITHMETIC}、
	 *   {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX}属性値が{@link AttributeValue#POSTFIX PREFIX}）
	 * @return 生成コード
	 */
	private String generateArithmeticPrefixOperatorCode(AstNode operatorNode) {

		String operatorSymbol = operatorNode.getAttribute(AttributeKey.OPERATOR_SYMBOL);

		// 単項プラスは対象変数をそのまま結果とする
		if (operatorSymbol.equals(ScriptWord.PLUS)) {
			return operatorNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);

		// 単項マイナスは別メソッド
		} else if (operatorSymbol.equals(ScriptWord.MINUS)) {
			return this.generateNegateOperatorCode(operatorNode);

		// それ以外は前置インクリメントまたはデクリメント
		} else {

			String opcode = null;
			switch (operatorSymbol) {
				case ScriptWord.INCREMENT : opcode = OperationCode.ADD.name(); break;
				case ScriptWord.DECREMENT : opcode = OperationCode.SUB.name(); break;
			}

			AstNode variableNode = operatorNode.getChildNodes()[0];

			// インクリメント/デクリメントの変化幅
			AstNode stepNode = new AstNode(AstNode.Type.LEAF, variableNode.getLineNumber(), variableNode.getFileName());

			String executionDataType = operatorNode.getAttribute(AttributeKey.OPERATOR_EXECUTION_DATA_TYPE);
			stepNode.setAttribute(AttributeKey.DATA_TYPE, executionDataType);
			stepNode.setAttribute(AttributeKey.RANK, Integer.toString(RANK_OF_SCALAR));
			switch (executionDataType) {
				case DataTypeName.INT : {
					String immediateValue = this.generateImmediateOperandCode(executionDataType, "1");
					stepNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, immediateValue);
					break;
				}
				case DataTypeName.FLOAT : {
					String immediateValue = this.generateImmediateOperandCode(executionDataType, "1.0");
					stepNode.setAttribute(AttributeKey.ASSEMBLY_VALUE, immediateValue);
					break;
				}
			}

			// 二項演算のADDやSUBで演算実行
			return this.generateBinaryOperatorCode(operatorNode, opcode, variableNode, stepNode);
		}
	}


	/**
	 * 単項マイナス演算子の演算を実行するコードを生成して返します。
	 *
	 * このメソッドは、
	 * {@link CodeGenerator#generateArithmeticPrefixOperatorCode generateArithmeticPrefixOperatorCode}
	 * メソッド内において使用されます。
	 *
	 * @param operatorNode 単項マイナス演算子のASTノード
	 * @return 生成コード
	 */
	private String generateNegateOperatorCode(AstNode operatorNode) {
		StringBuilder codeBuilder = new StringBuilder();

		// 符号操作の対象
		AstNode operandNode = operatorNode.getChildNodes()[0];
		String operandValue = operandNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);

		// 演算結果を格納するレジスタを確保
		String accumulatorRegister = operatorNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);
		codeBuilder.append(
			this.generateRegisterAllocationCode(operatorNode.getDataTypeName(), accumulatorRegister, operandValue, operatorNode.getRank())
		);

		// 演算
		codeBuilder.append(
			this.generateInstruction(OperationCode.NEG.name(), operandNode.getDataTypeName(), accumulatorRegister, operandValue)
		);

		return codeBuilder.toString();
	}




	/**
	 * 論理二項演算子の演算を実行するコードを生成して返します。
	 *
	 * 具体的な演算子としては、論理積（&amp;&amp;）および論理和（||）が該当します。
	 *
	 * @param operatorNode 論理二項演算子のASTノード
	 * （{@link AstNode.Type#OPERATOR OPERATOR}タイプ、
	 *   {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTOR}属性値が{@link AttributeValue#LOGICAL LOGICAL}、
	 *   {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX}属性値が{@link AttributeValue#BINARY BINARY}）
	 * @return 生成コード
	 */
	private String generateLogicalBinaryOperatorCode(AstNode operatorNode) {
		String operatorSymbol = operatorNode.getAttribute(AttributeKey.OPERATOR_SYMBOL);

		String opcode = null;
		switch (operatorSymbol) {
			case ScriptWord.AND :       opcode = OperationCode.AND.name(); break;
			case ScriptWord.OR :    opcode = OperationCode.OR.name(); break;
			default : {
				throw new VnanoFatalException("Invalid operator symbol for logical binary operators: " + operatorSymbol);
			}
		}

		return this.generateBinaryOperatorCode(operatorNode, opcode, operatorNode.getChildNodes());
	}


	/**
	 * 論理前置演算子の演算を実行するコードを生成して返します。
	 *
	 * 具体的な演算子としては、論理否定（!）のみが該当します。
	 *
	 * @param operatorNode 論理前置演算子のASTノード
	 * （{@link AstNode.Type#OPERATOR OPERATOR}タイプ、
	 *   {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTOR}属性値が{@link AttributeValue#LOGICAL LOGICAL}、
	 *   {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX}属性値が{@link AttributeValue#PREFIX PREFIX}）
	 * @return 生成コード
	 */
	private String generateLogicalPrefixOperatorCode(AstNode operatorNode) {
		StringBuilder codeBuilder = new StringBuilder();

		AstNode operandNode = operatorNode.getChildNodes()[0];
		String operandValue = operandNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);

		// 演算結果を格納するレジスタを確保
		String accumulatorRegister = operatorNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);
		codeBuilder.append(
			this.generateRegisterAllocationCode(DataTypeName.BOOL, accumulatorRegister, operandValue, operatorNode.getRank())
		);

		// 演算
		codeBuilder.append(
			this.generateInstruction(OperationCode.NOT.name(), DataTypeName.BOOL, accumulatorRegister, operandValue)
		);

		return codeBuilder.toString();
	}


	/**
	 * 比較二項演算子の演算を実行するコードを生成して返します。
	 *
	 * 具体的な演算子としては、等号（==）、不等号（!=）、
	 * 大なり（&gt;）、小なり（&lt;）、以上（&gt;=）、以下（&lt;=）が該当します。。
	 *
	 * @param operatorNode 比較二項演算子のASTノード
	 * （{@link AstNode.Type#OPERATOR OPERATOR}タイプ、
	 *   {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTOR}属性値が{@link AttributeValue#COMPARISON COMPARISON}、
	 *   {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX}属性値が{@link AttributeValue#BINARY BINARY}）
	 * @return 生成コード
	 */
	private String generateComparisonBinaryOperatorCode(AstNode operatorNode) {
		String operatorSymbol = operatorNode.getAttribute(AttributeKey.OPERATOR_SYMBOL);

		String opcode = null;
		switch (operatorSymbol) {
			case ScriptWord.EQUAL :       opcode = OperationCode.EQ.name(); break;
			case ScriptWord.NOT_EQUAL :    opcode = OperationCode.NEQ.name(); break;
			case ScriptWord.LESS_THAN : opcode = OperationCode.LT.name(); break;
			case ScriptWord.LESS_EQUAL :        opcode = OperationCode.LEQ.name(); break;
			case ScriptWord.GRATER_THAN : opcode = OperationCode.GT.name(); break;
			case ScriptWord.GRATER_EQUAL :        opcode = OperationCode.GEQ.name(); break;
			default : {
				throw new VnanoFatalException("Invalid operator symbol for comparison operators: " + operatorSymbol);
			}
		}

		return this.generateBinaryOperatorCode(operatorNode, opcode, operatorNode.getChildNodes());
	}


	/**
	 * 代入演算子の演算を実行するコードを生成して返します。
	 *
	 * @param operatorNode 代入演算子のASTノード
	 * （{@link AstNode.Type#OPERATOR OPERATOR}タイプ、
	 *   {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTOR}属性値が{@link AttributeValue#ASSIGNMENT ASSIGNMENT}、
	 *   {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX}属性値が{@link AttributeValue#BINARY BINARY}）
	 * @return 生成コード
	 */
	private String generateAsignmentOperatorCode(AstNode operatorNode) {
		StringBuilder codeBuilder = new StringBuilder();

		AstNode[] operandNodes = operatorNode.getChildNodes();
		int operandLength = operandNodes.length;

		String[] operandValues = new String[operandLength];
		for (int operandIndex=0; operandIndex<operandLength; operandIndex++) {
			operandValues[operandIndex] = operandNodes[operandIndex].getAttribute(AttributeKey.ASSEMBLY_VALUE);
		}

		// 型が違う場合はキャストが必要
		String rightHandValue = operandValues[1];
		String toType = operandNodes[0].getDataTypeName();  // キャスト先のデータ型名
		String fromType = operandNodes[1].getDataTypeName(); // キャスト元のデータ型名
		if (!toType.equals(fromType)) {

			// キャスト先レジスタを確保
			String castedRegister = this.generateRegisterOperandCode();
			codeBuilder.append(
				this.generateRegisterAllocationCode(toType, castedRegister, operandValues[1], operatorNode.getRank())
			);

			// レジスタに右辺値をキャスト
			String typeSpecification = toType + AssemblyWord.VALUE_SEPARATOR + fromType;
			codeBuilder.append(
				this.generateInstruction(
					OperationCode.CAST.name(), typeSpecification, castedRegister, operandValues[1]
				)
			);

			// 代入値は、キャスト済み値を格納するレジスタで置き換える
			rightHandValue = castedRegister;
		}

		// 配列の場合は要素数の同期処理が必要
		if (operatorNode.getRank() != RANK_OF_SCALAR) {
			codeBuilder.append(
				this.generateInstruction(OperationCode.ALLOCR.name(), toType, operandValues[0], rightHandValue)
			);
		}

		// MOV命令の発行
		codeBuilder.append(
			this.generateInstruction(OperationCode.MOV.name(), toType, operandValues[0], rightHandValue)
		);
		return codeBuilder.toString();
	}


	/**
	 * 算術複合代入演算子の演算を実行するコードを生成して返します。
	 *
	 * 具体的な演算子としては、可算代入（+=）、減算代入（-=）、
	 * 乗算代入（*=）、除算代入（/=）、剰余算代入（%=）が該当します。
	 *
	 * @param operatorNode 代入演算子のASTノード
	 * （{@link AstNode.Type#OPERATOR OPERATOR}タイプ、
	 *   {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTOR}属性値が{@link AttributeValue#ARITHMETIC_COMPOUND_ASSIGNMENT ARITHMETIC_COMPOUND_ASSIGNMENT}、
	 *   {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX}属性値が{@link AttributeValue#BINARY BINARY}）
	 * @return 生成コード
	 */
	private String generateArithmeticCompoundAssignmentOperatorCode(AstNode operatorNode) {

		// 複合代入演算は、右辺と左辺で配列要素数が揃っている事を前提とする。
		// 自動で配列要素数の同期などを行うようにしても、拡張された部分には単純に 0 などの初期値が詰まっているし、
		// それに対して、元から意味ある値が詰まっている要素と一緒に算術演算などを行えても嬉しい場面はないし、
		// むしろエラーとして弾いてくれた方が嬉しいと思うので。配列要素数の同期は、純粋な代入演算子のみとする。

		String operatorSymbol = operatorNode.getAttribute(AttributeKey.OPERATOR_SYMBOL);

		String opcode = null;
		switch (operatorSymbol) {
			case ScriptWord.ADDITION_ASSIGNMENT :       opcode = OperationCode.ADD.name(); break;
			case ScriptWord.SUBTRACTION_ASSIGNMENT :    opcode = OperationCode.SUB.name(); break;
			case ScriptWord.MULTIPLICATION_ASSIGNMENT : opcode = OperationCode.MUL.name(); break;
			case ScriptWord.DIVISION_ASSIGNMENT :        opcode = OperationCode.DIV.name(); break;
			case ScriptWord.REMAINDER_ASSIGNMENT :       opcode = OperationCode.REM.name(); break;
			default : {
				throw new VnanoFatalException("Invalid operator symbol for arithmetic compound assignment operators: " + operatorSymbol);
			}
		}

		AstNode[] childNodes = operatorNode.getChildNodes();
		AstNode[] operandNodes = { childNodes[0], childNodes[1] };

		return this.generateBinaryOperatorCode(operatorNode, opcode, operandNodes);

		// 型が違う場合のオペランドのキャストをどういうルールにするかは、どこかのタイミングで再検討すべきかも。
		// 現状では int += float; の加算は、両オペランドを代入先の型である int に揃えてから行うようにしている。
		// しかし単純な算術二項演算子の加算のコンパイル結果では、両オペランドを float に揃えてから行うので、
		// 算術複合代入演算子でも、算術二項演算子と同じようにキャストして演算してから、代入先の型に再キャストして代入すべき？
		// 仮に int += string; とかの場合は、結合文字列を整数に再変換するのか、それとも右辺を整数に変換してから加算するのかで結果が違う。
		// それとも、型が異なる複合代入演算は解釈が紛らわしいのでコンパイラで弾くべき？
	}



	/**
	 * 二項演算子の演算を実行するコードを生成して返します。
	 *
	 * このメソッドは、算術二項演算子のコード生成を行う
	 * {@link CodeGenerator#generateArithmeticBinaryOperatorCode generateArithmeticBinaryOperatorCode}
	 * メソッドなど、より細かい演算子の種類に応じたメソッドの内部で使用されます。
	 *
	 * @param operatorNode 二項演算子のASTノード
	 * （{@link AstNode.Type#OPERATOR OPERATOR}タイプ、
	 *   {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX}属性値が{@link AttributeValue#BINARY BINARY}）
	 * @param operationCode 中間アセンブリコードにおけるオペレーションコード
	 * @param inputNodes オペランドとなるASTノード
	 * @return 生成コード
	 */
	private String generateBinaryOperatorCode(AstNode operatorNode, String operationCode, AstNode ...inputNodes) {


		// ここは暫定的にベタ書きなため、後で色々と切り出して要リファクタリング



		StringBuilder codeBuilder = new StringBuilder();

		String executionDataType = operatorNode.getAttribute(AttributeKey.OPERATOR_EXECUTION_DATA_TYPE);
		String resultDataType = operatorNode.getAttribute(AttributeKey.DATA_TYPE);

		int rank = operatorNode.getRank();
		int inputLength = inputNodes.length;

		String[] input = new String[inputLength];
		for (int inputIndex=0; inputIndex<inputLength; inputIndex++) {
			input[inputIndex] = inputNodes[inputIndex].getAttribute(AttributeKey.ASSEMBLY_VALUE);
		}


		// 演算結果の出力先となる命令オペランドをASTノードから取得
		String output = operatorNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);


		// 出力先がレジスタかどうかを調べる
		boolean outputIsRegister =
			(operatorNode.getAttribute(AttributeKey.ASSEMBLY_VALUE).charAt(0) == AssemblyWord.OPERAND_PREFIX_REGISTER);


		// ベクトルとスカラの混合演算かどうかを調べる（スカラの配列への昇格が必要になる）
		boolean vectorScalarMixed = false;
		if (rank != RANK_OF_SCALAR) {
			for (AstNode inputNode: inputNodes) {
				if (inputNode.getRank() == RANK_OF_SCALAR) {
					vectorScalarMixed = true;
					break;
				}
			}
		}


		// 型変換が必要かどうかを調べる
		boolean castNecessary = false;
		for (int inputIndex=0; inputIndex<inputLength; inputIndex++) {
			if (!inputNodes[inputIndex].getDataTypeName().equals(executionDataType)) {
				castNecessary = true;
				break;
			}
		}


		// 入力オペランドの内、演算のデータ要素数の基準となるオペランドを探す
		//（ベクトルオペランドがあればそれを採用、無ければ単に最初のオペランドを採用）
		String lengthsDeterminer = input[0];
		for (int inputIndex=0; inputIndex<inputLength; inputIndex++) {
			if (inputNodes[inputIndex].getRank() != RANK_OF_SCALAR) {
				lengthsDeterminer = input[inputIndex];
				break;
			}
		}


		// 入力値の型が演算結果の型と異なる場合は、型変換を行う
		if (castNecessary) {

			for (int inputIndex=0; inputIndex<inputLength; inputIndex++) {
				String operandDataType = inputNodes[inputIndex].getDataTypeName();

				if (!operandDataType.equals(executionDataType)) {

					// レジスタを確保してそこにキャスト
					String castTarget = input[inputIndex];
					int castTargetRank = inputNodes[inputIndex].getRank();
					String castedRegister = this.generateRegisterOperandCode();
					codeBuilder.append(
						this.generateRegisterAllocationCode(executionDataType, castedRegister, castTarget, castTargetRank)
					);

					// CAST命令で型変換を実行
					String typeSpecification = executionDataType + AssemblyWord.VALUE_SEPARATOR + operandDataType;
					codeBuilder.append(
						this.generateInstruction(OperationCode.CAST.name(), typeSpecification, castedRegister, castTarget
						)
					);
					input[inputIndex] = castedRegister;
				}
			}
		}


		// ベクトルとスカラの混合演算において必要な処理
		if (vectorScalarMixed) {
			for (int inputIndex=0; inputIndex<inputLength; inputIndex++) {

				// ベクトル演算の入力値にスカラを含む場合は、ALLOCとFILLで配列に昇格させる
				if (inputNodes[inputIndex].getRank() == RANK_OF_SCALAR) {

					// ベクトルレジスタを確保
					String filledRegister = this.generateRegisterOperandCode();
					codeBuilder.append(
						this.generateRegisterAllocationCode(executionDataType, filledRegister, lengthsDeterminer, rank)
					);

					// FILL命令でベクトルレジスタの中身にスカラ値を詰める
					codeBuilder.append(
						this.generateInstruction(OperationCode.FILL.name(), executionDataType, filledRegister, input[inputIndex])
					);

					input[inputIndex] = filledRegister;
				}
			}
		}


		// 演算結果の格納先がレジスタの場合は、そのレジスタを確保
		if (outputIsRegister) {
			codeBuilder.append(
				this.generateRegisterAllocationCode(resultDataType, output, lengthsDeterminer, rank)
			);
		}


		// 演算実行コード生成
		codeBuilder.append(
			this.generateInstruction(operationCode, executionDataType, output, input[0], input[1])
		);

		return codeBuilder.toString();
	}


	/**
	 * 関数呼び出し演算子の演算を実行するコードを生成して返します。
	 *
	 * @param operatorNode 関数呼び出し演算子のASTノード
	 * （{@link AstNode.Type#OPERATOR OPERATOR}タイプ、
	 *   {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTOR}属性値が{@link AttributeValue#CALL CALLX}、
	 *   {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX}属性値が{@link AttributeValue#MULTIARY MULTIARY}）
	 * @return 生成コード
	 */
	private String generateCallOperatorCode(AstNode operatorNode) {
		StringBuilder codeBuilder = new StringBuilder();

		String returnRegister = operatorNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);
		String returnDataTypeName = operatorNode.getDataTypeName();

		AstNode[] childNodes = operatorNode.getChildNodes();
		int childNLength = childNodes.length;

		String scope = operatorNode.getAttribute(AttributeKey.SCOPE);

		int operandLength = childNLength + 1;
		String[] operands = new String[operandLength];
		for (int operandIndex=1; operandIndex<operandLength; operandIndex++) {
			operands[operandIndex] = childNodes[operandIndex-1].getAttribute(AttributeKey.ASSEMBLY_VALUE);
		}

		// 外部関数: CALLX命令を生成
		if (scope.equals(AttributeValue.GLOBAL)) {
			if (returnDataTypeName.equals(DataTypeName.VOID)) {
				operands[0] = PLACE_HOLDER;
			} else {
				operands[0] = returnRegister;
			}
			codeBuilder.append(
				this.generateInstruction(OperationCode.CALLX.name(), operatorNode.getDataTypeName(), operands)
			);

		// 内部関数: CALL命令と、戻り値を取得して格納するコードを生成
		} else if (scope.equals(AttributeValue.LOCAL)) {

			// CALL命令は戻り値をスタックに積むので、第0オペランドには何も書き込まないため、プレースホルダを置く。
			operands[0] = Character.toString(AssemblyWord.OPERAND_PREFIX_PLACEHOLDER);

			// CALL命令の対象関数指定オペランド値はラベルなので、ラベルのプレフィックスを付加
			operands[1] = AssemblyWord.OPERAND_PREFIX_LABEL + operands[1];

			// CALL命令を生成
			codeBuilder.append(
				this.generateInstruction(OperationCode.CALL.name(), operatorNode.getDataTypeName(), operands)
			);

			// 戻り値の型が void の場合は、スタック上の仮の戻り値（スタック順序維持用のために積まれている）を捨てるコードを生成
			if (returnDataTypeName.equals(DataTypeName.VOID)) {
				codeBuilder.append(
					this.generateInstruction(OperationCode.POP.name(), operatorNode.getDataTypeName(), PLACE_HOLDER)
				);

			// 戻り値の型が void でない場合は、戻り値を受け取るコードを生成
			} else {

				// 戻り値の格納先のメモリー領域を確保するコードを生成
				if(operatorNode.getRank() == RANK_OF_SCALAR) {
					// スカラの場合はサイズが固定なので、普通にALLOC命令で確保する
					codeBuilder.append(
						this.generateInstruction(OperationCode.ALLOC.name(), operatorNode.getDataTypeName(), returnRegister)
					);
				} else {
					// 配列の場合は、スタック上のデータがちょうど収まるサイズになるように、ALLOCP命令で確保する
					codeBuilder.append(
						this.generateInstruction(OperationCode.ALLOCP.name(), operatorNode.getDataTypeName(), returnRegister)
					);
				}

				// MOVPOP命令で、スタック上のデータを戻り値の格納先にコピーするコードを生成
				codeBuilder.append(
					this.generateInstruction(OperationCode.MOVPOP.name(), operatorNode.getDataTypeName(), returnRegister)
				);
			}

		} else {
			throw new VnanoFatalException("Unknown function scope: " + scope);
		}

		return codeBuilder.toString();
	}


	/**
	 * 配列要素アクセス演算子の演算を実行するコードを生成して返します。
	 *
	 * @param operatorNode 関数呼び出し演算子のASTノード
	 * （{@link AstNode.Type#OPERATOR OPERATOR}タイプ、
	 *   {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTOR}属性値が{@link AttributeValue#INDEX INDEX}、
	 *   {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX}属性値が{@link AttributeValue#MULTIARY MULTIARY}）
	 * @return 生成コード
	 */
	private String generateIndexOperatorCode(AstNode operatorNode) {
		StringBuilder codeBuilder = new StringBuilder();

		AstNode[] inputNodes = operatorNode.getChildNodes();
		int rank = inputNodes.length - 1;

		String targetOperand = inputNodes[0].getAttribute(AttributeKey.ASSEMBLY_VALUE);

		// ELEM命令のインデックス指定部に渡すオペランド
		String[] indexOperands = new String[rank];
		for (int dim=0; dim<rank; dim++) {
			indexOperands[dim] = inputNodes[dim + 1].getAttribute(AttributeKey.ASSEMBLY_VALUE);
		}

		// 結果を格納するレジスタを用意
		String accumulator = operatorNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);

		// INDEX命令を発行
		String[] allOperands = new String[indexOperands.length + 2];
		allOperands[0] = accumulator;
		allOperands[1] = targetOperand;
		System.arraycopy(indexOperands, 0, allOperands, 2, indexOperands.length);
		codeBuilder.append(
			this.generateInstruction(
					OperationCode.ELEM.name(), inputNodes[0].getDataTypeName(), allOperands
			)
		);

		return codeBuilder.toString();
	}


	/**
	 * キャスト演算子の演算を実行するコードを生成して返します。
	 *
	 * @param operatorNode キャスト演算子のASTノード
	 * （{@link AstNode.Type#OPERATOR OPERATOR}タイプ、
	 *   {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTOR}属性値が{@link AttributeValue#CAST CAST}、
	 *   {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX}属性値が{@link AttributeValue#POSTFIX PREFIX}）
	 * @return 生成コード
	 */
	private String generateCastOperatorCode(AstNode operatorNode) {
		StringBuilder codeBuilder = new StringBuilder();

		// キャスト対象のASTノードとデータ型を取得
		AstNode targetNode = operatorNode.getChildNodes()[0];
		String fromDataType = targetNode.getAttribute(AttributeKey.DATA_TYPE);
		String fromValue = targetNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);
		int fromRank = targetNode.getRank();

		// キャスト後のデータ型を取得
		String toDataType = operatorNode.getAttribute(AttributeKey.DATA_TYPE);

		// キャスト結果を格納するレジスタを確保
		String castedRegister = operatorNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);
		codeBuilder.append(
			this.generateRegisterAllocationCode(toDataType, castedRegister, fromValue, fromRank) // fromValue は length determiner
		);

		// CAST命令で型変換を実行
		String typeSpecification = toDataType + AssemblyWord.VALUE_SEPARATOR + fromDataType;
		codeBuilder.append(
			this.generateInstruction(OperationCode.CAST.name(), typeSpecification, castedRegister, fromValue
			)
		);

		return codeBuilder.toString();
	}


	/**
	 * 言語の名称およびバージョンなどを記載したディレクティブを一括生成して返します。
	 *
	 * @return 言語の識別用情報関連のディレクティブ
	 */
	private String generateLanguageInformationDirectives() {
		StringBuilder codeBuilder = new StringBuilder();

		// 中間言語名ディレクティブ
		codeBuilder.append(AssemblyWord.ASSEMBLY_LANGUAGE_IDENTIFIER_DIRECTIVE);
		codeBuilder.append(AssemblyWord.WORD_SEPARATOR);
		codeBuilder.append("\"");
		codeBuilder.append(AssemblyWord.ASSEMBLY_LANGUAGE_NAME);
		codeBuilder.append("\"");
		codeBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);
		codeBuilder.append(AssemblyWord.LINE_SEPARATOR);

		// 中間言語バージョンディレクティブ
		codeBuilder.append(AssemblyWord.ASSEMBLY_LANGUAGE_VERSION_DIRECTIVE);
		codeBuilder.append(AssemblyWord.WORD_SEPARATOR);
		codeBuilder.append("\"");
		codeBuilder.append(AssemblyWord.ASSEMBLY_LANGUAGE_VERSION);
		codeBuilder.append("\"");
		codeBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);
		codeBuilder.append(AssemblyWord.LINE_SEPARATOR);


		// スクリプト言語名ディレクティブ
		codeBuilder.append(AssemblyWord.SCRIPT_LANGUAGE_IDENTIFIER_DIRECTIVE);
		codeBuilder.append(AssemblyWord.WORD_SEPARATOR);
		codeBuilder.append("\"");
		codeBuilder.append(ScriptWord.SCRIPT_LANGUAGE_NAME);
		codeBuilder.append("\"");
		codeBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);
		codeBuilder.append(AssemblyWord.LINE_SEPARATOR);

		// スクリプト言語バージョンディレクティブ
		codeBuilder.append(AssemblyWord.SCRIPT_LANGUAGE_VERSION_DIRECTIVE);
		codeBuilder.append(AssemblyWord.WORD_SEPARATOR);
		codeBuilder.append("\"");
		codeBuilder.append(ScriptWord.SCRIPT_LANGUAGE_VERSION);
		codeBuilder.append("\"");
		codeBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);
		codeBuilder.append(AssemblyWord.LINE_SEPARATOR);

		return codeBuilder.toString();
	}


	/**
	 * AST(抽象構文木)全体の中で、呼び出している関数をリストアップし、
	 * 関数識別子ディレクティブを一括生成して返します。
	 * （中間アセンブリコード内では、オペランドに識別子を使用する箇所よりも前に、
	 * その識別子を種類に応じたディレクティブで宣言しておく必要があります。）
	 *
	 * @param inputAst 対象AST(抽象構文木)のルートノード
	 * @return AST内で呼び出している全関数の識別子ディレクティブ
	 */
	private String generateFunctionIdentifierDirectives(AstNode inputAst) {
		StringBuilder codeBuilder = new StringBuilder();
		AstNode currentNode = inputAst.getPostorderDftFirstNode();

		// 出力済みのものを控える
		Set<String> generatedSet = new HashSet<String>();

		// ボトムアップで全ノードを辿って処理、移動中の注目ノードは currentNode
		while(currentNode != inputAst) {

			// 関数を参照している呼び出し演算子の場合
			if (currentNode.getType() == AstNode.Type.OPERATOR
				&& currentNode.getAttribute(AttributeKey.OPERATOR_EXECUTOR).equals(AttributeValue.CALL)) {

				// 呼び出し対象関数のアセンブリコード用識別子を生成
				String calleeSignature = currentNode.getAttribute(AttributeKey.CALLEE_SIGNATURE);
				String identifier = AssemblyWord.OPERAND_PREFIX_IDENTIFIER + calleeSignature;
				//String identifier = IdentifierSyntax.getAssemblyIdentifierOfCalleeFunctionOf(currentNode);

				// 呼び出し対象関数のスコープを取得
				String scope = currentNode.getAttribute(AttributeKey.SCOPE);

				// 既に出力済みでなければ出力
				if (!generatedSet.contains(identifier)) {
					generatedSet.add(identifier);
					if (scope.equals(AttributeValue.GLOBAL)) {
						codeBuilder.append(AssemblyWord.GLOBAL_FUNCTION_DIRECTIVE);
					} else if (scope.equals(AttributeValue.LOCAL)) {
						codeBuilder.append(AssemblyWord.LOCAL_FUNCTION_DIRECTIVE);
					} else {
						throw new VnanoFatalException("Unknown function scope: " + currentNode.getAttribute(AttributeKey.SCOPE));
					}
					codeBuilder.append(AssemblyWord.WORD_SEPARATOR);
					codeBuilder.append(identifier);
					codeBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);
					codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
				}
			}

			currentNode = currentNode.getPostorderDftNextNode();
		}
		return codeBuilder.toString();
	}


	/**
	 * AST(抽象構文木)全体の中で、アクセスしているグローバル変数をリストアップし、
	 * グローバル変数識別子ディレクティブを一括生成して返します。
	 * （中間アセンブリコード内では、オペランドに識別子を使用する箇所よりも前に、
	 * その識別子を種類に応じたディレクティブで宣言しておく必要があります。）
	 *
	 * @param inputAst 対象AST(抽象構文木)のルートノード
	 * @return AST内で呼び出している全グローバル変数の識別子ディレクティブ
	 */
	private String generateGlobalIdentifierDirectives(AstNode inputAst) {
		StringBuilder codeBuilder = new StringBuilder();
		AstNode currentNode = inputAst.getPostorderDftFirstNode();

		// 出力済みのものを控える
		Set<String> generatedSet = new HashSet<String>();

		// ボトムアップで全ノードを辿って処理、移動中の注目ノードは currentNode
		while(currentNode != inputAst) {

			// グローバル変数の場合
			if (currentNode.getType()==AstNode.Type.LEAF
				&&
				currentNode.getAttribute(AttributeKey.LEAF_TYPE).equals(AttributeValue.VARIABLE_IDENTIFIER)
				&&
				currentNode.getAttribute(AttributeKey.SCOPE).equals(AttributeValue.GLOBAL) ){


				String variableName = currentNode.getAttribute(AttributeKey.IDENTIFIER_VALUE);
				String identifier = IdentifierSyntax.getAssemblyIdentifierOf(variableName);

				// 既に出力済みでなければ出力
				if (!generatedSet.contains(identifier)) {
					codeBuilder.append(AssemblyWord.GLOBAL_VARIABLE_DIRECTIVE);
					codeBuilder.append(AssemblyWord.WORD_SEPARATOR);
					codeBuilder.append(identifier);
					codeBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);
					codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
				}
			}

			currentNode = currentNode.getPostorderDftNextNode();
		}
		return codeBuilder.toString();
	}



	/**
	 * メタ情報ディレクティブを生成して返します。
	 *
	 * @param node 任意のASTノード（行番号およびファイル名の情報が使用されます）
	 * @return メタ情報ディレクティブ
	 */
	private String generateMetaDirectiveCode(AstNode node) {

		StringBuilder codeBuilder = new StringBuilder();

		codeBuilder.append(AssemblyWord.META_DIRECTIVE);
		codeBuilder.append(AssemblyWord.WORD_SEPARATOR);
		codeBuilder.append("\"");
		codeBuilder.append("line=");
		codeBuilder.append(node.getLineNumber());
		codeBuilder.append(", ");
		codeBuilder.append("file=");

		// オプションマップの正規化時に既にエスケープされているはずだが、念のため出力直前にもエスケープしておく
		String escapedFileName = OptionValue.escapeScriptName(node.getFileName());
		codeBuilder.append(escapedFileName);

		codeBuilder.append("\"");
		codeBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);
		codeBuilder.append(AssemblyWord.LINE_SEPARATOR);

		return codeBuilder.toString();
	}



	/**
	 * ラベルディレクティブを生成して返します。
	 *
	 * この処理系の中間アセンブリコードにおいて、ラベルの配置は、
	 * このメソッドが返すディレクティブを配置する事によって行います。
	 *
	 * @param labelName ラベルの値
	 * @return ラベルディレクティブ
	 */
	private String generateLabelDirectiveCode(String labelName) {
		StringBuilder labelBuilder = new StringBuilder();
		labelBuilder.append(AssemblyWord.LABEL_DIRECTIVE);
		labelBuilder.append(AssemblyWord.WORD_SEPARATOR);
		labelBuilder.append(labelName);
		labelBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);
		labelBuilder.append(AssemblyWord.LINE_SEPARATOR);
		return labelBuilder.toString();
	}


	/**
	 * これまでに生成したラベルと重複しない、新しいラベルの値を生成して返します。
	 *
	 * @return 新規生成したラベルの値
	 */
	private String generateLabelOperandCode() {
		StringBuilder labelBuilder = new StringBuilder();
		labelBuilder.append(AssemblyWord.OPERAND_PREFIX_LABEL);
		labelBuilder.append(CodeGenerator.LABEL_NAME);
		labelBuilder.append(Integer.toString(this.labelCounter));
		this.labelCounter++;
		return labelBuilder.toString();
	}


	/**
	 * これまでに生成したレジスタと重複しない、新しいレジスタの値を生成して返します。
	 *
	 * @return 新規生成したレジスタの値
	 */
	private String generateRegisterOperandCode() {

		StringBuilder returnBuilder = new StringBuilder();
		returnBuilder.append(AssemblyWord.OPERAND_PREFIX_REGISTER);
		returnBuilder.append(this.registerCounter);
		this.registerCounter++;

		return returnBuilder.toString();
	}


	/**
	 * ソースコード内の書式に準拠するリテラルの値を、
	 * 中間アセンブリコードの書式に準拠する即値に変換して返します。
	 *
	 * @return 変換された即値
	 */
	private String generateImmediateOperandCode(String typeName, String literal) {
		StringBuilder returnBuilder = new StringBuilder();
		returnBuilder.append(AssemblyWord.OPERAND_PREFIX_IMMEDIATE);
		returnBuilder.append(typeName);
		returnBuilder.append(AssemblyWord.VALUE_SEPARATOR);
		returnBuilder.append(literal);

		return returnBuilder.toString();
	}


	/**
	 * この処理系の中間アセンブリコードの書式に準拠した、1行の命令コードを生成して返します。
	 *
	 * @param opcode オペレーションコード
	 * @param dataType データ型指定部の内容
	 * @param operands オペランド（任意個）
	 * @return 命令コード(1行)
	 */
	private String generateInstruction(String opcode, String dataType, String... operands) {
		StringBuilder codeBuilder = new StringBuilder();
		codeBuilder.append(AssemblyWord.INDENT);
		codeBuilder.append(opcode);
		codeBuilder.append(AssemblyWord.WORD_SEPARATOR);
		codeBuilder.append(dataType);
		for (String operand: operands) {
			codeBuilder.append(AssemblyWord.WORD_SEPARATOR);
			codeBuilder.append(operand);
		}
		codeBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);
		codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
		return codeBuilder.toString();
	}


	/**
	 * 生成済みのコードに対して、可読性を向上させるための際整形を行います。
	 *
	 * @param code 元のコード
	 * @return 際整形済みのコード
	 */
	private String realign(String code) {
		StringBuilder codeBuilder = new StringBuilder();

		String[] lines = code.split(AssemblyWord.LINE_SEPARATOR_REGEX);
		int lineLength = lines.length;

		// 言語情報ディレクティブの抽出/配置
		boolean languageDirectiveExist = false;
		for (int lineIndex=0; lineIndex<lineLength; lineIndex++) {
			if (lines[lineIndex].startsWith(AssemblyWord.ASSEMBLY_LANGUAGE_IDENTIFIER_DIRECTIVE)
					|| lines[lineIndex].startsWith(AssemblyWord.ASSEMBLY_LANGUAGE_VERSION_DIRECTIVE)
					|| lines[lineIndex].startsWith(AssemblyWord.SCRIPT_LANGUAGE_IDENTIFIER_DIRECTIVE)
					|| lines[lineIndex].startsWith(AssemblyWord.SCRIPT_LANGUAGE_VERSION_DIRECTIVE)) {

				languageDirectiveExist = true;
				codeBuilder.append(lines[lineIndex]);
				codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
				lines[lineIndex] = "";
			}
		}

		// ディレクティブの種類が変わる箇所で空白行を挟む
		if (languageDirectiveExist) {
			codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
		}

		// グローバル関数ディレクティブの抽出/配置
		boolean globalFunctionDirectiveExist = false;
		for (int lineIndex=0; lineIndex<lineLength; lineIndex++) {
			if (lines[lineIndex].startsWith(AssemblyWord.GLOBAL_FUNCTION_DIRECTIVE)) {
				globalFunctionDirectiveExist = true;
				codeBuilder.append(lines[lineIndex]);
				codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
				lines[lineIndex] = "";
			}
		}

		// ディレクティブの種類が変わる箇所で空白行を挟む
		if (globalFunctionDirectiveExist) {
			codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
		}

		// ローカル関数ディレクティブの抽出/配置
		boolean localFunctionDirectiveExist = false;
		for (int lineIndex=0; lineIndex<lineLength; lineIndex++) {
			if (lines[lineIndex].startsWith(AssemblyWord.LOCAL_FUNCTION_DIRECTIVE)) {
				localFunctionDirectiveExist = true;
				codeBuilder.append(lines[lineIndex]);
				codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
				lines[lineIndex] = "";
			}
		}

		// ディレクティブの種類が変わる箇所で空白行を挟む
		if (localFunctionDirectiveExist) {
			codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
		}

		// グローバル変数ディレクティブの抽出/配置
		boolean globalVariableDirectiveExist = false;
		for (int lineIndex=0; lineIndex<lineLength; lineIndex++) {
			if (lines[lineIndex].startsWith(AssemblyWord.GLOBAL_VARIABLE_DIRECTIVE)) {
				globalVariableDirectiveExist = true;
				codeBuilder.append(lines[lineIndex]);
				codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
				lines[lineIndex] = "";
			}
		}

		// ディレクティブの種類が変わる箇所で空白行を挟む
		if (globalVariableDirectiveExist) {
			codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
		}

		// ローカル変数ディレクティブの抽出/配置
		@SuppressWarnings("unused")
		boolean localVariableDirectiveExist = false;
		for (int lineIndex=0; lineIndex<lineLength; lineIndex++) {
			if (lines[lineIndex].startsWith(AssemblyWord.LOCAL_VARIABLE_DIRECTIVE)) {
				localVariableDirectiveExist = true;
				codeBuilder.append(lines[lineIndex]);
				codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
				lines[lineIndex] = "";
			}
		}

		// 直後に続くメタディレクティブの空白行と重複するので、ここでは挟まない
		/*
		// ディレクティブの種類が変わる箇所で空白行を挟む
		if (localVariableDirectiveExist) {
			codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
		}
		*/

		// その他の行の抽出/配置
		for (int lineIndex=0; lineIndex<lineLength; lineIndex++) {

			// 元のコード内にあった空白行は削る
			if (lines[lineIndex].length() == 0) {
				continue;
			}

			// メタディレクティブの直前には空白行を挟む
			if (lines[lineIndex].startsWith(AssemblyWord.META_DIRECTIVE)) {
				codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
			}

			codeBuilder.append(lines[lineIndex]);
			codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
			lines[lineIndex] = "";
		}

		return codeBuilder.toString();
	}



	// （スクリプトエンジンのevalメソッドの評価値に対応するデータをメモリに設定して終了する）
	/**
	 * 終了処理のコードを生成します。
	 *
	 * 具体的には、必要に応じてスクリプトエンジンのevalメソッドの評価値に対応するデータをメモリに設定して、
	 * 実行を終了する処理のコードを生成します。
	 *
	 * @param inputAst AST(抽象構文木)全体のルートノード
	 * @return 生成コード
	 */
	private String generateFinalizationCode(AstNode inputAst) {
		StringBuilder codeBuilder = new StringBuilder();

		// 最上階層の文のノードを全て取得
		AstNode[] topLevelStatementNodes = inputAst.getChildNodes();
		int statementLength = topLevelStatementNodes.length;

		// 最後の文が式文なら、その値をスクリプトエンジンのevalメソッドの評価結果とし、型と値の格納先を取得
		String evalDataType = null;
		String evalValue = null;
		if (statementLength != 0) {
			AstNode lastStatementNode = topLevelStatementNodes[statementLength-1];
			if (lastStatementNode.getType() == AstNode.Type.EXPRESSION) {
				evalDataType = lastStatementNode.getAttribute(AttributeKey.DATA_TYPE);
				evalValue = lastStatementNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);
			}
		}

		// 評価結果とすべき値が無いか、もしくはあってもvoid型な場合は、無指定のEND命令を生成
		if (evalDataType == null || evalDataType.equals(DataTypeName.VOID)) {
			codeBuilder.append(
				this.generateInstruction(OperationCode.END.name(), DataTypeName.VOID, PLACE_HOLDER)
			);

		// 評価値がある場合は、それをオペランドに指定してEND命令を生成
		} else {
			codeBuilder.append(
				this.generateInstruction(OperationCode.END.name(), evalDataType, PLACE_HOLDER, evalValue)
			);
		}

		return codeBuilder.toString();
	}


}



