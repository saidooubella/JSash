package parser.expressions;

import span.TextSpan;
import tokens.Token;

public final class ParenthesizedExpressionNode implements ExpressionNode {
	
	private final TextSpan span;

	public final Token openParent;
	public final ExpressionNode expression;
	public final Token closeParent;

	public ParenthesizedExpressionNode(Token openParent, ExpressionNode expression, Token closeParent) {
		this.span = openParent.span().plus(closeParent.span());
		this.closeParent = closeParent;
		this.openParent = openParent;
		this.expression = expression;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
