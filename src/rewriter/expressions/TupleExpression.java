package rewriter.expressions;

import span.TextSpan;
import types.Type;

import java.util.List;

public final class TupleExpression implements Expression {

	private final TextSpan span;
	private final Type type;

	public final List<Expression> values;

	public TupleExpression(List<Expression> values, Type type, TextSpan span) {
		this.values = values;
		this.type = type;
		this.span = span;
	}

	@Override
	public Type type() {
		return type;
	}

	@Override
	public TextSpan span() {
		return span;
	}
}
