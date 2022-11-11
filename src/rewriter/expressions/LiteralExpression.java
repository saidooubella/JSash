package rewriter.expressions;

import span.TextSpan;
import types.Type;

public final class LiteralExpression implements Expression {

	private final TextSpan span;
	private final Type type;

	public final Object value;

	public LiteralExpression(Object value, Type type, TextSpan span) {
		this.value = value;
		this.type = type;
		this.span = span;
	}

	@Override
	public TextSpan span() {
		return span;
	}

	@Override
	public Type type() {
		return type;
	}
}
