package rewriter.expressions;

import span.TextSpan;
import types.PrimitiveType;
import types.Type;

public final class NoneExpression implements Expression {

	private final TextSpan span;

	public NoneExpression(TextSpan span) {
		this.span = span;
	}

	@Override
	public Type type() {
		return PrimitiveType.None;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
