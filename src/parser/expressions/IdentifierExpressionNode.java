package parser.expressions;

import span.TextSpan;
import tokens.Token;

public final class IdentifierExpressionNode implements ExpressionNode {

	public final Token identifier;
	
	public IdentifierExpressionNode(Token identifier) {
		this.identifier = identifier;
	}

	@Override
	public TextSpan span() {
		return identifier.span();
	}
}
