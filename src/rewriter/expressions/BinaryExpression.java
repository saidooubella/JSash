package rewriter.expressions;

import rewriter.operators.BinaryOperator;
import span.TextSpan;
import types.Type;

public final class BinaryExpression implements Expression {

	private final TextSpan span;
	private final Type type;

	public final Expression left;
	public final BinaryOperator operator;
	public final Expression right;

	public BinaryExpression(Expression left, BinaryOperator operator, Expression right, Type type, TextSpan span) {
		this.span = span;
		this.left = left;
		this.operator = operator;
		this.right = right;
		this.type = type;
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
