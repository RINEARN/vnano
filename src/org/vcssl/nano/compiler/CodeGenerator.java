/*
 * Copyright(C) 2017-2018 RINEARN (Fumihiro Matsui)
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
import org.vcssl.nano.spec.ScriptWord;


/**
 * <p>
 * コンパイラ内において、
 * {@link SemanticAnalyzer SemanticAnalyzer}（意味解析器）
 * が出力した情報補間済みAST（抽象構文木）から、
 * {@link org.vcssl.nano.vm.assembler.Assembler Assembler}
 * が解釈可能な中間アセンブリコードへと変換する、コード生成器のクラスです。
 * </p>
 *
 * <p>
 * 中間アセンブリコードは、この処理系のコンパイラである {@link Compiler Compiler}
 * クラスの最終的な出力データです。
 * データ形式は文字列であるため、内容を直接目視で確認する事ができます。
 * </p>
 *
 * @author RINEARN (Fumihiro Matsui)
 */
public class CodeGenerator {

	private static final int RANK_OF_SCALAR = 0;

	/** ラベルの名称において、末尾の番号を除いた部分に使用される文字列です。 */
	private static final String LABEL_NAME = "LABEL";

	/** 生成コード内で bool 型の true を表す即値であり、無条件ジャンプのコード生成などに使用されます。 */
	private static final String IMMEDIATE_TRUE
			= AssemblyWord.OPERAND_PREFIX_IMMEDIATE + DataTypeName.BOOL + AssemblyWord.VALUE_SEPARATOR + LiteralSyntax.TRUE;

	/** 生成コード内の各レジスタに、固有のアドレスを割り当てるためのカウンタです。 */
	private int registerCounter;

	/** 生成コード内の各ラベルに、固有の名称を割り当てるためのカウンタです。 */
	private int labelCounter;

	/**
	 * 各種カウンタ値が 0 で初期化されたインスタンスを作成します。
	 */
	public CodeGenerator() {
		this.registerCounter = 0;
		this.labelCounter = 0;
	}



	/**
	 * 文を逐次的にコードに変換していく際に必要な、
	 * 前後の文に依存する情報（コンテキスト）を保持するクラスであり、
	 * {@link CodeGenerator#trackAllStatements CodeGenerator.trackAllStatements}
	 * メソッドや
	 * {@link CodeGenerator#generateStatementCode CodeGenerator.generateStatementCode}
	 * メソッド内で使用されます。
	 *
	 * 例えば、if 文や while 文 および for 文では、その後に続く文のコード生成が完了した後に、
	 * 条件不成立時やループ脱出時のジャンプ先となるラベルを配置する必要があります。
	 * また、else 文では、直前の if 文の条件式の結果が、
	 * 仮想メモリ上のどのアドレスに保持されているのかという情報が必要です。
	 */
	private class StatementTrackingContext implements Cloneable {

		private String beginPointLabel = null; // ループ文などで、後の文の生成後の地点に先頭に戻るコードを置いてほしい場合、これに先頭地点のラベル値を入れる
		private String updatePointLabel = null; // ループ文などで、ループ毎の更新処理の直前にラベルを置いてほしい場合、これにラベル値を入れる
		private String updatePointStatement = null; // for文の更新式の文
		private LinkedList<String> endPointLabelList = null; // IF文など、後の文の生成後の地点にラベルを置いてほしい場合、これにラベル値を入れる(ifの後にelseが続く場合のみ要素が2個になり得る)

		private String lastIfConditionRegister = null; // 直前の if 文の条件式結果を格納するレジスタを控えて、else 文で使う
		private String lastLoopBeginPointLabel = null; // 最後に踏んだループの始点ラベル
		private String lastLoopUpdatePointLabel = null; // 最後に踏んだループの更新ラベル
		private String lastLoopEndPointLabel = null;   // 最後に踏んだループの終点ラベル

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

			clone.lastIfConditionRegister = this.lastIfConditionRegister;
			clone.lastLoopBeginPointLabel = this.lastLoopBeginPointLabel;
			clone.lastLoopUpdatePointLabel = this.lastLoopUpdatePointLabel;
			clone.lastLoopEndPointLabel = this.lastLoopEndPointLabel;

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

		public String getUpdatePointStatement() {
			return this.updatePointStatement;
		}
		public void setUpdatePointStatement(String updatePointStatement) {
			this.updatePointStatement = updatePointStatement;
		}
		public boolean hasUpdatePointStatement() {
			return this.updatePointStatement != null;
		}
		public void clearEndPointStatement() {
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
		/*
		public void clearLastIfConditionValue() {
			this.lastIfConditionRegister = null;
		}
		*/

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
	 * 意味解析済みのAST(抽象構文木)を読み込み、中間アセンブリコードを生成して返します。
	 *
	 * @param inputAst 意味解析済みのAST(抽象構文木)のルートノードて
	 * @return 中間アセンブリコード
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

		AstNode currentNode = inputAst.getPostorderTraversalFirstNode();
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
					String assemblyValue = IdentifierSyntax.getAssemblyIdentifierOfCalleeFunctionOf(callOperatorNode);
					currentNode.addAttribute(AttributeKey.ASSEMBLY_VALUE, assemblyValue);

				// 変数または変数識別子
				} else if(isVariable || (isLeaf && leafType == AttributeValue.VARIABLE_IDENTIFIER) ) {
					String identifier = currentNode.getAttribute(AttributeKey.IDENTIFIER_VALUE);
					String assemblyValue = IdentifierSyntax.getAssemblyIdentifierOf(identifier);
					if (currentNode.hasAttribute(AttributeKey.IDENTIFIER_SERIAL_NUMBER)) {
						assemblyValue += AssemblyWord.IDENTIFIER_SERIAL_NUMBER_SEPARATOR
						              + currentNode.getAttribute(AttributeKey.IDENTIFIER_SERIAL_NUMBER);
					}
					currentNode.addAttribute(AttributeKey.ASSEMBLY_VALUE, assemblyValue);

				// リテラル
				} else if(isLeaf && leafType == AttributeValue.LITERAL) {

					String dataTypeName = currentNode.getAttribute(AttributeKey.DATA_TYPE);
					String literal = currentNode.getAttribute(AttributeKey.LITERAL_VALUE);
					String assemblyValue = AssemblyWord.getImmediateValueOf(dataTypeName, literal);
					currentNode.addAttribute(AttributeKey.ASSEMBLY_VALUE, assemblyValue);

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
						currentNode.addAttribute(AttributeKey.ASSEMBLY_VALUE, value);
						break;
					}
					case AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT : {
						String value = currentNode.getChildNodes()[0].getAttribute(AttributeKey.ASSEMBLY_VALUE);
						currentNode.addAttribute(AttributeKey.ASSEMBLY_VALUE, value);
						break;
					}
					case AttributeValue.ARITHMETIC : {
						if (syntaxType.equals(AttributeValue.PREFIX)) {
							if (operatorSymbol.equals(ScriptWord.INCREMENT) || operatorSymbol.equals(ScriptWord.DECREMENT)) {
								String value = currentNode.getChildNodes()[0].getAttribute(AttributeKey.ASSEMBLY_VALUE);
								currentNode.addAttribute(AttributeKey.ASSEMBLY_VALUE, value);
								break;
							}
						}
					}
					default : {
						String register = AssemblyWord.OPERAND_PREFIX_REGISTER + Integer.toString(registerCounter);
						currentNode.addAttribute(AttributeKey.ASSEMBLY_VALUE, register);
						this.registerCounter++;
						break;
					}
				}
			}

			// 式ノード: 直下にあるルート演算子の結果 = 式の結果
			if (currentNode.getType() == AstNode.Type.EXPRESSION) {
				String value = currentNode.getChildNodes()[0].getAttribute(AttributeKey.ASSEMBLY_VALUE);
				currentNode.addAttribute(AttributeKey.ASSEMBLY_VALUE, value);
			}

			currentNode = currentNode.getPostorderTraversalNextNode();
		}
	}



	/**
	 * AST(抽象構文木)内の各ノードに対して、
	 * 中間アセンブリコード内で使用するラベルを割りふり、
	 * それらを各ノードの {@link AttributeKey#ASSEMBLY_VALUE ASSEMBLY_VALUE} 属性値に設定します
	 * （従って、このメソッドは破壊的メソッドです）。
	 *
	 * @param inputAst 解析対象のAST(抽象構文木)
	 */
	private void assignLabels(AstNode inputAst) {

		AstNode currentNode = inputAst.getPostorderTraversalFirstNode();
		while (currentNode != inputAst) {

			if (currentNode.getType() == AstNode.Type.IF) {
				currentNode.addAttribute(AttributeKey.END_LABEL, this.generateLabelOperandCode());
			}
			if (currentNode.getType() == AstNode.Type.ELSE) {
				currentNode.addAttribute(AttributeKey.END_LABEL, this.generateLabelOperandCode());
			}
			if (currentNode.getType() == AstNode.Type.FOR) {
				currentNode.addAttribute(AttributeKey.BEGIN_LABEL, this.generateLabelOperandCode());
				currentNode.addAttribute(AttributeKey.UPDATE_LABEL, this.generateLabelOperandCode());
				currentNode.addAttribute(AttributeKey.END_LABEL, this.generateLabelOperandCode());
			}
			if (currentNode.getType() == AstNode.Type.WHILE) {
				currentNode.addAttribute(AttributeKey.BEGIN_LABEL, this.generateLabelOperandCode());
				currentNode.addAttribute(AttributeKey.END_LABEL, this.generateLabelOperandCode());
			}

			// 演算子ノード
			if (currentNode.getType() == AstNode.Type.OPERATOR) {
				String symbol = currentNode.getAttribute(AttributeKey.OPERATOR_SYMBOL);
				if (symbol.equals(ScriptWord.AND) || symbol.equals(ScriptWord.OR)) {
					// 短絡評価で第二オペランドの演算をスキップする場合のラベル
					currentNode.addAttribute(AttributeKey.END_LABEL, this.generateLabelOperandCode());
				}
			}

			currentNode = currentNode.getPostorderTraversalNextNode();
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

					// break や continue のために引き継ぐ必要がある情報をコピー
					context.setLastLoopBeginPointLabel(contextStack.peek().getLastLoopBeginPointLabel());
					context.setLastLoopUpdatePointLabel(contextStack.peek().getLastLoopUpdatePointLabel());
					context.setLastLoopEndPointLabel(contextStack.peek().getLastLoopEndPointLabel());

					// ブロック内の文を、読み込み対象として展開する
					statementNodes = currentNode.getChildNodes();
					statementLength = statementNodes.length;
					statementIndex = 0;
					break;
				}

				// ブロック文以外の文の場合
				case VARIABLE :
				case IF :
				case ELSE :
				case WHILE :
				case FOR :
				case BREAK :
				case CONTINUE :
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
					context.clearEndPointStatement();
				}

				// ループ文などで先頭に戻りたい場合のコードがあれば置く
				if (context.hasBeginPointLabel()) {
					String jumpCode = this.generateInstruction(
							OperationCode.JMP.name(), DataTypeName.BOOL, IMMEDIATE_TRUE, context.getBeginPointLabel()
					);
					codeBuilder.append(jumpCode);
					context.clearBeginPointLabel();
				}

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
				code = this.generateIfStatementCode(node, lastIfConditionRegister, lastIfConditionRegisterAllocRequired);
				break;
			}
			// else 文
			case ELSE : {
				code = this.generateElseStatementCode(node, context.getLastIfConditionRegister());
				context.addEndPointLabel(node.getAttribute(AttributeKey.END_LABEL));
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
				break;
			}
			// for 文
			case FOR : {
				code = this.generateForStatementCode(node);
				context.setBeginPointLabel(node.getAttribute(AttributeKey.BEGIN_LABEL));
				context.setUpdatePointLabel(node.getAttribute(AttributeKey.UPDATE_LABEL));
				context.addEndPointLabel(node.getAttribute(AttributeKey.END_LABEL));

				// 更新式（for文の丸括弧内での3つ目の式）
				context.setUpdatePointStatement(this.generateExpressionCode(node.getChildNodes()[2]));

				context.setLastLoopBeginPointLabel( context.getBeginPointLabel() );
				context.setLastLoopUpdatePointLabel( context.getUpdatePointLabel() );
				context.setLastLoopEndPointLabel( context.getEndPointLabels()[0] ); // for文の場合は endPointLabels は1要素のはず
				break;
			}
			// break 文: ループ末端へのジャンプ命令そのもの
			case BREAK : {
				code = this.generateInstruction(
						OperationCode.JMP.name(), DataTypeName.BOOL, IMMEDIATE_TRUE,
						context.getLastLoopEndPointLabel()
				);
				break;
			}
			// continue 文: ループ先頭か更新式の位置へのジャンプ命令そのもの
			case CONTINUE : {
				String continueJumpPointLabel = context.getLastLoopBeginPointLabel(); // ループ先頭に飛ぶラベル
				if (context.hasLastLoopUpdatePointLabel()) {
					continueJumpPointLabel = context.getLastLoopUpdatePointLabel(); // for文は更新式の位置に飛ぶ
				}
				code = this.generateInstruction(
						OperationCode.JMP.name(), DataTypeName.BOOL, IMMEDIATE_TRUE,
						continueJumpPointLabel
				);
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

		/*
		// 2次元以上は要素数をVEC命令で配列にまとめてレジスタに置き、それを要素数オペランドに渡す
		} else {

			// まずレジスタにインデックス配列を格納するコードを生成
			String indexRegister = this.generateRegisterOperandCode();
			String[] arrayInstructionOperands = new String[rank + 1];
			arrayInstructionOperands[0] = indexRegister;
			for (int dim=0; dim<rank; dim++) {
				arrayInstructionOperands[dim+1] = arrayLengthValues[dim];
			}
			codeBuilder.append(
				this.generateInstruction(OperationCode.VEC.name(), DataTypeName.INT, arrayInstructionOperands)
			);

			// 2オペランドのALLOC命令を発行
			codeBuilder.append(
				this.generateInstruction(OperationCode.ALLOC.name(), variableType, variableOperand, indexRegister)
			);
		*/
		}

		// 初期化式があればコード生成
		AstNode[] initExprNodes = node.getChildNodes(AstNode.Type.EXPRESSION);
		if (initExprNodes.length == 1) {
			codeBuilder.append( this.generateExpressionCode(initExprNodes[0]) );
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
			this.generateInstruction(OperationCode.JMPN.name(), DataTypeName.BOOL, conditionExprValue, endLabel)
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
			this.generateInstruction(OperationCode.JMP.name(), DataTypeName.BOOL, lastIfConditionValue, endLabel)
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
			// 条件式が配列の場合は弾くべき
			return null;
		}

		// 条件不成立時はループ外に脱出するコードを生成
		String conditionExprValue = conditionExprNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);
		codeBuilder.append(
			this.generateInstruction(OperationCode.JMPN.name(), DataTypeName.BOOL, conditionExprValue, endLabel)
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

		// 初期化文、条件式、更新式の評価コードを生成
		AstNode[] childNodes =  node.getChildNodes();

		String initStatementCode = null;
		if (childNodes[0].getType() == AstNode.Type.VARIABLE) {
			initStatementCode = this.generateVariableDeclarationStatementCode(childNodes[0]);
		} else {
			initStatementCode = this.generateExpressionCode(childNodes[0]);
		}

		String conditionExpressionCode = this.generateExpressionCode(childNodes[1]);


		// 初期化文のコードを生成
		codeBuilder.append(initStatementCode);

		// その後に、ループで戻って来る地点のラベルを配置
		codeBuilder.append(this.generateLabelDirectiveCode(beginLabel));


		// 条件式の評価コードを生成
		codeBuilder.append(conditionExpressionCode);

		// 条件不成立時はループ外に脱出するコードを生成
		String conditionValue = childNodes[1].getAttribute(AttributeKey.ASSEMBLY_VALUE);
		codeBuilder.append(
			this.generateInstruction(OperationCode.JMPN.name(), DataTypeName.BOOL, conditionValue, endLabel)
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

		AstNode currentNode = exprRootNode.getPostorderTraversalFirstNode();
		while(currentNode != exprRootNode) {

			// リーフノードは演算コードを生成する必要がない
			if (currentNode.getType() != AstNode.Type.OPERATOR) {
				currentNode = currentNode.getPostorderTraversalNextNode();
				continue;
			}

			// 以下は演算子ノードなので演算コードを生成

			String operatorSyntax = currentNode.getAttribute(AttributeKey.OPERATOR_SYNTAX);
			String operatorExecution = currentNode.getAttribute(AttributeKey.OPERATOR_EXECUTOR);

			switch (operatorExecution) {
				case AttributeValue.CALL : {
					codeBuilder.append( this.generateCallOperatorCode(currentNode) );
					break;
				}
				case AttributeValue.ASSIGNMENT : {
					codeBuilder.append( this.generateAsignmentOperatorCode(currentNode) );
					break;
				}
				case AttributeValue.ARITHMETIC : {
					switch (operatorSyntax) {
						case AttributeValue.BINARY : {
							codeBuilder.append( this.generateArithmeticBinaryOperatorCode(currentNode) );
							break;
						}
						case AttributeValue.PREFIX : {
							codeBuilder.append( this.generateArithmeticPrefixOperatorCode(currentNode) );
							break;
						}
						case AttributeValue.POSTFIX : {
							codeBuilder.append( this.generateArithmeticPostfixOperatorCode(currentNode) );
							break;
						}
						default : {
							throw new VnanoFatalException("Invalid operator syntax for arithmetic operators: " + operatorSyntax);
						}
					}
					break;
				}
				case AttributeValue.ARITHMETIC_COMPOUND_ASSIGNMENT : {
					codeBuilder.append( this.generateArithmeticCompoundAssignmentOperatorCode(currentNode) );
					break;
				}
				case AttributeValue.COMPARISON : {
					codeBuilder.append( this.generateComparisonBinaryOperatorCode(currentNode) );
					break;
				}
				case AttributeValue.LOGICAL : {
					switch (operatorSyntax) {
						case AttributeValue.BINARY : {
							codeBuilder.append( this.generateLogicalBinaryOperatorCode(currentNode) );
							break;
						}
						case AttributeValue.PREFIX : {
							codeBuilder.append( this.generateLogicalPrefixOperatorCode(currentNode) );
							break;
						}
						default : {
							throw new VnanoFatalException("Invalid operator syntax for logical operators: " + operatorSyntax);
						}
					}
					break;
				}
				case AttributeValue.INDEX : {
					codeBuilder.append( this.generateIndexOperatorCode(currentNode) );
					break;
				}
				default : {
					throw new VnanoFatalException("Unknown operator execution type: " + operatorExecution);
				}
			}

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
								this.generateInstruction(jumpOpcode, DataTypeName.BOOL, leftOperandValue, jumpLabel)
						);

					// 右オペランド場合: スキップ地点のラベルを置く
					} else {
						codeBuilder.append(
								this.generateLabelDirectiveCode(parentNode.getAttribute(AttributeKey.END_LABEL))
						);
					}
				}
			}

			currentNode = currentNode.getPostorderTraversalNextNode();
		}

		return codeBuilder.toString();
	}






	/**
	 * 算術二項演算子の演算を実行するコードを生成して返します。
	 *
	 * 具体的な演算子としては、中置記法の可算、減算、乗算、除算、剰余算が該当します。
	 *
	 * @param operatorNode 算術二項演算子のASTノード
	 * （{@link AstNode.Type#OPERATOR OPERATOR}タイプ、
	 *   {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTION}属性値が{@link AttributeValue#ARITHMETIC ARITHMETIC}、
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
	 *   {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTION}属性値が{@link AttributeValue#ARITHMETIC ARITHMETIC}、
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

		// 演算前の値をレジスタに控える
		String storageRegister = null; // ここで確保するとlengthRegisterと番号が逆転する
		if (variableNode.getRank() == RANK_OF_SCALAR) {
			storageRegister = this.generateRegisterOperandCode();
			codeBuilder.append(
				this.generateInstruction(OperationCode.ALLOC.name(), executionDataType, storageRegister)
			);
		} else {
			/*
			String lengthRegister = this.generateRegisterOperandCode();
			codeBuilder.append(
				this.generateInstruction(OperationCode.LEN.name(), DataTypeName.INT, lengthRegister, variableValue)
			);
			*/
			storageRegister = this.generateRegisterOperandCode();
			codeBuilder.append(
				//this.generateInstruction(OperationCode.ALLOC.name(), executionDataType, storageRegister, lengthRegister)
				this.generateInstruction(OperationCode.ALLOCR.name(), executionDataType, storageRegister, variableValue)
			);
		}

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

		stepNode.addAttribute(AttributeKey.DATA_TYPE, executionDataType);
		stepNode.addAttribute(AttributeKey.RANK, Integer.toString(RANK_OF_SCALAR));
		switch (executionDataType) {
			case DataTypeName.INT : {
				String immediateValue = this.generateImmediateOperandCode(executionDataType, "1");
				stepNode.addAttribute(AttributeKey.ASSEMBLY_VALUE, immediateValue);
				break;
			}
			case DataTypeName.FLOAT : {
				String immediateValue = this.generateImmediateOperandCode(executionDataType, "1.0");
				stepNode.addAttribute(AttributeKey.ASSEMBLY_VALUE, immediateValue);
				break;
			}
		}


		String binaryOperationCode = this.generateBinaryOperatorCode(operatorNode, opcode, variableNode, stepNode);
		codeBuilder.append(binaryOperationCode);

		String movCode = this.generateInstruction(OperationCode.MOV.name(), executionDataType, variableValue, resultValue);
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
	 *   {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTION}属性値が{@link AttributeValue#ARITHMETIC ARITHMETIC}、
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
			stepNode.addAttribute(AttributeKey.DATA_TYPE, executionDataType);
			stepNode.addAttribute(AttributeKey.RANK, Integer.toString(RANK_OF_SCALAR));
			switch (executionDataType) {
				case DataTypeName.INT : {
					String immediateValue = this.generateImmediateOperandCode(executionDataType, "1");
					stepNode.addAttribute(AttributeKey.ASSEMBLY_VALUE, immediateValue);
					break;
				}
				case DataTypeName.FLOAT : {
					String immediateValue = this.generateImmediateOperandCode(executionDataType, "1.0");
					stepNode.addAttribute(AttributeKey.ASSEMBLY_VALUE, immediateValue);
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

		// 演算後の値を格納するレジスタ
		String accumulatorRegister = operatorNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);

		// 符号操作の対象
		AstNode operandNode = operatorNode.getChildNodes()[0];
		String operandValue = operandNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);

		// ってかこういうアキュームレータに適切なALLOCを行うのはメソッドに切り分けて流用すべき
		if (operandNode.getRank() == RANK_OF_SCALAR) {
			codeBuilder.append(
				this.generateInstruction(OperationCode.ALLOC.name(), operandNode.getDataTypeName(), accumulatorRegister)
			);
		} else {
			/*
			String lengthRegister = this.generateRegisterOperandCode();
			codeBuilder.append(
				this.generateInstruction(OperationCode.LEN.name(), DataTypeName.INT, lengthRegister, operandValue)
			);
			*/
			codeBuilder.append(
				//this.generateInstruction(OperationCode.ALLOC.name(), operandNode.getDataTypeName(), accumulatorRegister, lengthRegister)
				this.generateInstruction(OperationCode.ALLOCR.name(), operandNode.getDataTypeName(), accumulatorRegister, operandValue)
			);
		}

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
	 *   {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTION}属性値が{@link AttributeValue#LOGICAL LOGICAL}、
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
	 *   {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTION}属性値が{@link AttributeValue#LOGICAL LOGICAL}、
	 *   {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX}属性値が{@link AttributeValue#PREFIX PREFIX}）
	 * @return 生成コード
	 */
	private String generateLogicalPrefixOperatorCode(AstNode operatorNode) {
		StringBuilder codeBuilder = new StringBuilder();

		AstNode operandNode = operatorNode.getChildNodes()[0];
		String operandValue = operandNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);

		// 演算後の値を格納するレジスタを確保
		String accumulatorRegister = this.generateRegisterOperandCode();
		if (operandNode.getRank() == RANK_OF_SCALAR) {
			codeBuilder.append(
				this.generateInstruction(OperationCode.ALLOC.name(), DataTypeName.BOOL, accumulatorRegister)
			);
		} else {
			/*
			String lengthRegister = this.generateRegisterOperandCode();
			codeBuilder.append(
				this.generateInstruction(OperationCode.LEN.name(), DataTypeName.INT, lengthRegister, operandValue)
			);
			*/
			codeBuilder.append(
				//this.generateInstruction(OperationCode.ALLOC.name(), DataTypeName.BOOL, accumulatorRegister, lengthRegister)
				this.generateInstruction(OperationCode.ALLOCR.name(), DataTypeName.BOOL, accumulatorRegister, operandValue)
			);
		}


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
	 *   {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTION}属性値が{@link AttributeValue#COMPARISON COMPARISON}、
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
	 *   {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTION}属性値が{@link AttributeValue#ASSIGNMENT ASSIGNMENT}、
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
		if (!operandNodes[0].getDataTypeName().equals(operandNodes[1].getDataTypeName())) {

			// キャスト先レジスタの用意
			String castedRegister = this.generateRegisterOperandCode();

			// キャスト元がスカラの場合の確保
			if (operandNodes[1].getRank() == RANK_OF_SCALAR) {
				codeBuilder.append(
					this.generateInstruction(OperationCode.ALLOC.name(), operandNodes[0].getDataTypeName(), castedRegister)
				);
			// キャスト元が配列の場合の確保
			} else {
				/*
				String lengthRegister = this.generateRegisterOperandCode();
				codeBuilder.append(
					this.generateInstruction(OperationCode.LEN.name(), DataTypeName.INT, lengthRegister, operandValues[1])
				);
				*/
				codeBuilder.append(
					//this.generateInstruction(OperationCode.ALLOC.name(), operandNodes[0].getDataTypeName(), castedRegister, lengthRegister)
					this.generateInstruction(OperationCode.ALLOCR.name(), operandNodes[0].getDataTypeName(), castedRegister, operandValues[1])
				);
			}

			// レジスタに右辺値をキャスト
			codeBuilder.append(
				this.generateInstruction(
						OperationCode.CAST.name(),
						operandNodes[0].getDataTypeName() + AssemblyWord.VALUE_SEPARATOR + operandNodes[1].getDataTypeName(),
						castedRegister, operandValues[1]
				)
			);

			// 代入値は、キャスト済み値を格納するレジスタで置き換える
			rightHandValue = castedRegister;
		}

		// MOV命令の発行
		codeBuilder.append(
			this.generateInstruction(OperationCode.MOV.name(), operandNodes[0].getDataTypeName(), operandValues[0], rightHandValue)
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
	 *   {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTION}属性値が{@link AttributeValue#ARITHMETIC_COMPOUND_ASSIGNMENT ARITHMETIC_COMPOUND_ASSIGNMENT}、
	 *   {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX}属性値が{@link AttributeValue#BINARY BINARY}）
	 * @return 生成コード
	 */
	private String generateArithmeticCompoundAssignmentOperatorCode(AstNode operatorNode) {
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

		// これ、形名だけでいい気もする
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


		// 入力オペランドの内、最初に出現するベクトルオペランドを取得（ベクトルレジスタの確保時などに要素数情報を使用する）
		String firstVectorInput = null;
		for (int inputIndex=0; inputIndex<inputLength; inputIndex++) {
			if (inputNodes[inputIndex].getRank() != RANK_OF_SCALAR) {
				firstVectorInput = input[inputIndex];
				break;
			}
		}


		// 入力値の型が演算結果の型と異なる場合は、型変換を行う
		if (castNecessary) {

			for (int inputIndex=0; inputIndex<inputLength; inputIndex++) {
				String operandDataType = inputNodes[inputIndex].getDataTypeName();

				if (!operandDataType.equals(executionDataType)) {

					// レジスタを確保してそこにキャスト
					String castedRegister = null; // ここで確保するとレジスタ番号が前後逆転してしまう

					// ALLOC命令で、変換後の値を格納するレジスタを確保（スカラ値を格納する場合は要素数指定は不要）
					if (inputNodes[inputIndex].getRank() == RANK_OF_SCALAR) {
						castedRegister = this.generateRegisterOperandCode();
						codeBuilder.append(
							this.generateInstruction(OperationCode.ALLOC.name(), executionDataType, castedRegister)
						);

					// 変換元の値がベクトルの場合は、LEN命令で要素数を取得した上で、同要素数のレジスタをALLOC命令で確保
					} else {
						/*
						String lengthRegister = this.generateRegisterOperandCode();
						codeBuilder.append(
							this.generateInstruction(OperationCode.LEN.name(), DataTypeName.INT, lengthRegister, input[inputIndex])
						);
						*/
						castedRegister = this.generateRegisterOperandCode();
						codeBuilder.append(
							this.generateInstruction(
								//OperationCode.ALLOC.name(), executionDataType, castedRegister, lengthRegister
								OperationCode.ALLOCR.name(), executionDataType, castedRegister, input[inputIndex]
							)
						);
					}

					// CAST命令で型変換を実行
					codeBuilder.append(
						this.generateInstruction(
							OperationCode.CAST.name(),
							executionDataType + AssemblyWord.VALUE_SEPARATOR + operandDataType,
							castedRegister, input[inputIndex]
						)
					);
					input[inputIndex] = castedRegister;
				}
			}
		}


		// ベクトルとスカラの混合演算において必要な処理
		if (vectorScalarMixed) {
			// ベクトル演算の入力値にスカラを含む場合は、ALLOCとFILLで配列に昇格させる
			for (int inputIndex=0; inputIndex<inputLength; inputIndex++) {
				if (inputNodes[inputIndex].getRank() == RANK_OF_SCALAR) {

					// ベクトルレジスタを確保するために要素数情報をLEN命令で取得し、別のレジスタに格納
					/*
					String lengthRegister = this.generateRegisterOperandCode();
					codeBuilder.append(
						this.generateInstruction(OperationCode.LEN.name(), DataTypeName.INT, lengthRegister, firstVectorInput)
					);
					*/

					// ベクトルレジスタをALLOCR命令で確保
					String filledRegister = this.generateRegisterOperandCode();
					codeBuilder.append(
						//this.generateInstruction(OperationCode.ALLOC.name(), executionDataType, filledRegister, lengthRegister)
						this.generateInstruction(OperationCode.ALLOCR.name(), executionDataType, filledRegister, firstVectorInput)
					);

					// FILL命令でベクトルレジスタの中身にスカラ値を詰める
					codeBuilder.append(
						this.generateInstruction(OperationCode.FILL.name(), executionDataType, filledRegister, input[inputIndex])
					);

					input[inputIndex] = filledRegister;
				}
			}
		}


		// 演算結果の格納先がレジスタの場合はメモリ確保
		if (outputIsRegister) {
			if (rank == RANK_OF_SCALAR) {
				// ALLOC命令でベクトルレジスタを確保（要素数指定を省略しているので、要素数はスカラ値格納用の1、次元は0になる）
				codeBuilder.append(
						this.generateInstruction(OperationCode.ALLOC.name(), resultDataType, output)
				);
			} else {
				/*
				// ベクトルレジスタを確保するために要素数情報をLEN命令で取得し、別のレジスタに格納
				String lengthRegister = this.generateRegisterOperandCode();
				codeBuilder.append(
					this.generateInstruction(OperationCode.LEN.name(), DataTypeName.INT, lengthRegister, firstVectorInput)
				);
				*/

				// ALLOC命令でベクトルレジスタを確保
				codeBuilder.append(
						//this.generateInstruction(OperationCode.ALLOC.name(), resultDataType, output, lengthRegister)
						this.generateInstruction(OperationCode.ALLOCR.name(), resultDataType, output, firstVectorInput)
				);
			}
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
	 *   {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTION}属性値が{@link AttributeValue#CALL CALLX}、
	 *   {@link AttributeKey#OPERATOR_SYNTAX OPERATOR_SYNTAX}属性値が{@link AttributeValue#MULTIARY MULTIARY}）
	 * @return 生成コード
	 */
	private String generateCallOperatorCode(AstNode operatorNode) {
		StringBuilder codeBuilder = new StringBuilder();

		String returnRegister = operatorNode.getAttribute(AttributeKey.ASSEMBLY_VALUE);

		AstNode[] childNodes = operatorNode.getChildNodes();
		int childNLength = childNodes.length;

		int operandLength = childNLength + 1;
		String[] operands = new String[operandLength];

		operands[0] = returnRegister;
		for (int operandIndex=1; operandIndex<operandLength; operandIndex++) {
			operands[operandIndex] = childNodes[operandIndex-1].getAttribute(AttributeKey.ASSEMBLY_VALUE);
		}

		codeBuilder.append(
				this.generateInstruction(OperationCode.CALLX.name(), operatorNode.getDataTypeName(), operands)
		);

		return codeBuilder.toString();
	}



	/**
	 * 配列要素アクセス演算子の演算を実行するコードを生成して返します。
	 *
	 * @param operatorNode 関数呼び出し演算子のASTノード
	 * （{@link AstNode.Type#OPERATOR OPERATOR}タイプ、
	 *   {@link AttributeKey#OPERATOR_EXECUTOR OPERATOR_EXECUTION}属性値が{@link AttributeValue#INDEX INDEX}、
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

		/*
		// インデックスが1個ならスカラとしてそのまま渡せる
		if (rank == 1) {
			indexOperand = inputNodes[1].getAttribute(AttributeKey.ASSEMBLY_VALUE);

		// インデックスが複数あれば、VEC命令で配列にまとめてレジスタに置き、それを渡す
		} else {
			int rank = rank;
			indexOperand = this.generateRegisterOperandCode();
			String[] arrayInstructionOperands = new String[rank + 1];
			arrayInstructionOperands[0] = indexOperand;
			for (int dim=0; dim<rank; dim++) {
				arrayInstructionOperands[dim+1] =
						inputNodes[dim + 1].getAttribute(AttributeKey.ASSEMBLY_VALUE);
			}
			codeBuilder.append(
				this.generateInstruction(OperationCode.VEC.name(), DataTypeName.INT, arrayInstructionOperands)
			);
		}
		*/

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
		AstNode currentNode = inputAst.getPostorderTraversalFirstNode();

		// 出力済みのものを控える
		Set<String> generatedSet = new HashSet<String>();

		// ボトムアップで全ノードを辿って処理、移動中の注目ノードは currentNode
		while(currentNode != inputAst) {

			// 関数を参照している呼び出し演算子の場合
			if (currentNode.getType() == AstNode.Type.OPERATOR
				&& currentNode.getAttribute(AttributeKey.OPERATOR_EXECUTOR).equals(AttributeValue.CALL)) {

				String identifier = IdentifierSyntax.getAssemblyIdentifierOfCalleeFunctionOf(currentNode);

				// 既に出力済みでなければ出力
				if (!generatedSet.contains(identifier)) {
					generatedSet.add(identifier);
					codeBuilder.append(AssemblyWord.GLOBAL_FUNCTION_DIRECTIVE);
					codeBuilder.append(AssemblyWord.WORD_SEPARATOR);
					codeBuilder.append(identifier);
					codeBuilder.append(AssemblyWord.INSTRUCTION_SEPARATOR);
					codeBuilder.append(AssemblyWord.LINE_SEPARATOR);
				}
			}

			currentNode = currentNode.getPostorderTraversalNextNode();
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
		AstNode currentNode = inputAst.getPostorderTraversalFirstNode();

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

			currentNode = currentNode.getPostorderTraversalNextNode();
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
		codeBuilder.append(node.getFileName());
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



}



