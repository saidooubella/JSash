package parser.expressions;

import span.TextSpan;
import tokens.Token;

public final class SelfExpressionNode implements ExpressionNode {

	private final TextSpan span;
	
	public final Token selfKeyword;

	public SelfExpressionNode(Token selfKeyword) {
		this.span = selfKeyword.span();
		this.selfKeyword = selfKeyword;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
