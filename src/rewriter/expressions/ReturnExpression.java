package rewriter.expressions;

import span.TextSpan;
import types.PrimitiveType;
import types.Type;

import java.util.Optional;

public final class ReturnExpression implements Expression {

	private final TextSpan span;
	
	public final Optional<Expression> value;

	public ReturnExpression(Optional<Expression> value, TextSpan span) {
		this.value = value;
		this.span = span;
	}

	@Override
	public TextSpan span() {
		return span;
	}

	@Override
	public Type type() {
		return PrimitiveType.Nothing;
	}
}
