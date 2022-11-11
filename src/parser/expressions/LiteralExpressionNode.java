package parser.expressions;

import span.TextSpan;

public final class LiteralExpressionNode implements ExpressionNode {

	private final TextSpan span;

	public final Object value;

	public LiteralExpressionNode(Object value, TextSpan span) {
		this.value = value;
		this.span = span;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
