package parser.statements;

import parser.wrappers.ParametersNode;
import span.TextSpan;
import tokens.Token;

public final class RecordStatementNode implements StatementNode {

	private final TextSpan span;

	public final ParametersNode fields;
	public final Token recordKeyword;
	public final Token identifier;
	
	public RecordStatementNode(Token recordKeyword, Token identifier, ParametersNode fields) {
		this.span = recordKeyword.span().plus(fields.span());
		this.recordKeyword = recordKeyword;
		this.identifier = identifier;
		this.fields = fields;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
