package rewriter.expressions;

import span.TextSpan;
import types.PrimitiveType;
import types.Type;

public final class ErrorExpression implements Expression {

	private final TextSpan span;

	public ErrorExpression(TextSpan span) {
		this.span = span;
	}
	
	@Override
	public TextSpan span() {
		return span;
	}
	
	@Override
	public Type type() {
		return PrimitiveType.Error;
	}
}
