package rewriter.expressions;

import rewriter.operators.UnaryOperator;
import span.TextSpan;
import types.Type;

public final class UnaryExpression implements Expression {

	private final TextSpan span;
	private final Type type;

	public final UnaryOperator operator;
	public final Expression operand;

	public UnaryExpression(UnaryOperator operator, Expression operand, Type type, TextSpan span) {
		this.operator = operator;
		this.operand = operand;
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
