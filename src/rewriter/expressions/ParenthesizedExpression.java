package rewriter.expressions;

import span.TextSpan;
import types.Type;

public final class ParenthesizedExpression implements Expression {

	private final TextSpan span;
	
	public final Expression expression;

	public ParenthesizedExpression(Expression expression, TextSpan span) {
		this.expression = expression;
		this.span = span;
	}

	@Override
	public TextSpan span() {
		return span;
	}

	@Override
	public Type type() {
		return expression.type();
	}
}
