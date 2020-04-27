package org.vcssl.nano.spec;


/**
 * 各種の言語仕様設定クラスのインスタンスを、まとめて格納するためのコンテナです。
 * {@link org.vcssl.nano.VnanoEngine VnanoEngine} クラスのコンストラクタに、
 * カスタマイズした言語仕様設定を渡す際は、このコンテナに格納した状態で渡します。
 *
 * 各設定クラスのインスタンスは、このクラスの public なフィールドとして保持されます。
 * なお、いくつかの設定クラス間には依存関係があり、
 * 個別に差し替えると不整合な設定集合になる可能性があるため、
 * 初期化後は差し替え不可能となるなように、このクラスのフィールドは final 宣言されています。
 */
public class LanguageSpecContainer {

	public final ScriptWord SCRIPT_WORD;
	public final AssemblyWord ASSEMBLY_WORD;
	public final DataTypeName DATA_TYPE_NAME;
	public final IdentifierSyntax IDENTIFIER_SYNTAX;
	public final LiteralSyntax LITERAL_SYNTAX;
	public final OperatorPrecedence OPERATOR_PRECEDENCE;


	/**
	 * デフォルトの言語仕様設定を保持するコンテナを生成します。
	 */
	public LanguageSpecContainer() {
		this.SCRIPT_WORD = new ScriptWord();
		this.ASSEMBLY_WORD = new AssemblyWord();
		this.DATA_TYPE_NAME = new DataTypeName();
		this.IDENTIFIER_SYNTAX = new IdentifierSyntax(DATA_TYPE_NAME, ASSEMBLY_WORD);
		this.LITERAL_SYNTAX = new LiteralSyntax(DATA_TYPE_NAME);
		this.OPERATOR_PRECEDENCE = new OperatorPrecedence();
	}


	/**
	 * カスタマイズされた言語仕様設定を保持するコンテナを生成します。
	 */
	public LanguageSpecContainer(
			ScriptWord scriptWord, AssemblyWord assemblyWord,
			DataTypeName dataTypeName, IdentifierSyntax identifierSyntax, LiteralSyntax literalSyntax,
			OperatorPrecedence operatorPrecedence) {

		this.SCRIPT_WORD = scriptWord;
		this.ASSEMBLY_WORD = assemblyWord;
		this.DATA_TYPE_NAME = dataTypeName;
		this.IDENTIFIER_SYNTAX = identifierSyntax;
		this.LITERAL_SYNTAX = literalSyntax;
		this.OPERATOR_PRECEDENCE = operatorPrecedence;
	}
}
