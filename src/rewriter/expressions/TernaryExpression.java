package rewriter.expressions;

import span.TextSpan;
import types.Type;

public final class TernaryExpression implements Expression {

	private final TextSpan span;
	private final Type type;
	
	public final Expression condition;
	public final Expression thenExpr;
	public final Expression elseExpr;

	public TernaryExpression(Expression condition, Expression thenExpr, Expression elseExpr, Type type, TextSpan span) {
		this.condition = condition;
		this.thenExpr = thenExpr;
		this.elseExpr = elseExpr;
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
