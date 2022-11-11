package rewriter.expressions;

import span.TextSpan;
import types.Type;

public final class IndexingExpression implements Expression {

	private final TextSpan span;
	private final Type type;
	
	public final Expression target;
	public final Expression index;

	public IndexingExpression(Expression target, Expression index, Type type, TextSpan span) {
		this.target = target;
		this.index = index;
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
