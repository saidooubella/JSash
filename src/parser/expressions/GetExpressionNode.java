package parser.expressions;

import span.TextSpan;
import tokens.Token;

public final class GetExpressionNode implements ExpressionNode {

	private final TextSpan span;
	
	public final ExpressionNode target;
	public final Token identifier;
	public final Token dot;

	public GetExpressionNode(ExpressionNode target, Token dot, Token identifier) {
		this.span = target.span().plus(identifier.span());
		this.identifier = identifier;
		this.target = target;
		this.dot = dot;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
