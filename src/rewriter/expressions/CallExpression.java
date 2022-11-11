package rewriter.expressions;

import span.TextSpan;
import types.Type;

import java.util.List;

public final class CallExpression implements Expression {

	private final TextSpan span;
	private final Type type;

	public final Expression target;
	public final List<Expression> arguments;

	public CallExpression(Expression target, List<Expression> arguments, Type type, TextSpan span) {
		this.target = target;
		this.arguments = arguments;
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
