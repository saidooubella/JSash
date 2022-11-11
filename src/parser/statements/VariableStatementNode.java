package parser.statements;

import parser.wrappers.DeclarationNode;
import parser.wrappers.ValueNode;
import span.TextSpan;
import tokens.Token;

public final class VariableStatementNode implements StatementNode {

	private final TextSpan span;

	public final Token keyword;
	public final DeclarationNode declaration;
	public final ValueNode value;
	public final boolean isReadOnly;

	public VariableStatementNode(Token keyword, DeclarationNode declaration, ValueNode value, boolean isReadOnly) {
		this.span = keyword.span().plus(value.span());
		this.keyword = keyword;
		this.declaration = declaration;
		this.value = value;
		this.isReadOnly = isReadOnly;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
